[CmdletBinding()]
param(
    [string]$RepositoryRoot,
    [string]$ArtifactDirectory,
    [ValidateRange(30, 600)]
    [int]$StartupTimeoutSeconds = 180,
    [ValidateRange(10, 300)]
    [int]$TransitionTimeoutSeconds = 60
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

if ([string]::IsNullOrWhiteSpace($RepositoryRoot)) {
    $RepositoryRoot = Join-Path $PSScriptRoot '..'
}
$RepositoryRoot = [System.IO.Path]::GetFullPath($RepositoryRoot)
$composeFile = Join-Path $RepositoryRoot 'docker-compose.yml'
if (-not (Test-Path -LiteralPath $composeFile -PathType Leaf)) {
    throw "Docker Compose file does not exist: $composeFile"
}
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw 'Docker CLI is required for the Compose smoke test'
}

$projectName = 'nohome-smoke-' + [Guid]::NewGuid().ToString('N').Substring(0, 12)
if ($projectName -notmatch '^nohome-smoke-[a-f0-9]{12}$') {
    throw 'Refusing to use an unsafe Compose project name'
}

$environmentFile = Join-Path ([System.IO.Path]::GetTempPath()) "$projectName.env"
if ([string]::IsNullOrWhiteSpace($ArtifactDirectory)) {
    $ArtifactDirectory = Join-Path ([System.IO.Path]::GetTempPath()) "$projectName-logs"
}
$ArtifactDirectory = [System.IO.Path]::GetFullPath($ArtifactDirectory)
[System.IO.Directory]::CreateDirectory($ArtifactDirectory) | Out-Null

function Get-ComposeArguments {
    param([Parameter(Mandatory)][string[]]$Arguments)

    return @(
        'compose',
        '--project-name', $projectName,
        '--project-directory', $RepositoryRoot,
        '--env-file', $environmentFile,
        '--file', $composeFile
    ) + $Arguments
}

function Invoke-Compose {
    param([Parameter(Mandatory)][string[]]$Arguments)

    $commandArguments = Get-ComposeArguments -Arguments $Arguments
    & docker @commandArguments
    if ($LASTEXITCODE -ne 0) {
        throw "Docker Compose failed with exit code ${LASTEXITCODE}: $($Arguments -join ' ')"
    }
}

function Invoke-ComposeOutput {
    param([Parameter(Mandatory)][string[]]$Arguments)

    $commandArguments = Get-ComposeArguments -Arguments $Arguments
    $output = @(& docker @commandArguments)
    if ($LASTEXITCODE -ne 0) {
        throw "Docker Compose failed with exit code ${LASTEXITCODE}: $($Arguments -join ' ')"
    }
    return $output
}

function Get-PublishedPort {
    param(
        [Parameter(Mandatory)][string]$Service,
        [Parameter(Mandatory)][int]$ContainerPort
    )

    $lines = Invoke-ComposeOutput -Arguments @('port', $Service, $ContainerPort.ToString())
    $endpoint = $lines | Where-Object { -not [string]::IsNullOrWhiteSpace($_) } | Select-Object -Last 1
    if ($null -eq $endpoint -or $endpoint.ToString().Trim() -notmatch ':(\d+)$') {
        throw "Could not resolve the published port for ${Service}:${ContainerPort}"
    }
    return [int]$Matches[1]
}

Add-Type -AssemblyName System.Net.Http
$httpClient = [System.Net.Http.HttpClient]::new()
$httpClient.Timeout = [TimeSpan]::FromSeconds(5)

function Get-HttpObservation {
    param([Parameter(Mandatory)][string]$Url)

    $response = $httpClient.GetAsync($Url).GetAwaiter().GetResult()
    try {
        $body = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
        $cacheControl = ''
        if ($null -ne $response.Headers.CacheControl) {
            $cacheControl = $response.Headers.CacheControl.ToString()
        }
        return [pscustomobject]@{
            StatusCode = [int]$response.StatusCode
            Body = $body
            CacheControl = $cacheControl
        }
    } finally {
        $response.Dispose()
    }
}

function Wait-HttpState {
    param(
        [Parameter(Mandatory)][string]$Name,
        [Parameter(Mandatory)][string]$Url,
        [Parameter(Mandatory)][int]$ExpectedStatus,
        [Parameter(Mandatory)][int]$TimeoutSeconds,
        [bool]$RequireNoStore = $false,
        [object]$ExpectedDatabaseConnected = $null,
        [string]$ExpectedApplicationStatus
    )

    $deadline = [DateTime]::UtcNow.AddSeconds($TimeoutSeconds)
    $lastResult = 'no response'
    while ([DateTime]::UtcNow -lt $deadline) {
        try {
            $observation = Get-HttpObservation -Url $Url
            $lastResult = "HTTP $($observation.StatusCode)"
            $matches = $observation.StatusCode -eq $ExpectedStatus
            if ($matches -and $RequireNoStore) {
                $matches = $observation.CacheControl -match '(^|,)\s*no-store\s*(,|$)'
                if (-not $matches) {
                    $lastResult += ", Cache-Control=$($observation.CacheControl)"
                }
            }
            if ($matches -and $null -ne $ExpectedDatabaseConnected) {
                $payload = $observation.Body | ConvertFrom-Json
                $matches = $payload.data.database.connected -eq [bool]$ExpectedDatabaseConnected
                if ($matches -and -not [string]::IsNullOrWhiteSpace($ExpectedApplicationStatus)) {
                    $matches = $payload.data.status -eq $ExpectedApplicationStatus
                }
                if ($matches -and [bool]$ExpectedDatabaseConnected) {
                    $matches = $payload.data.database.probe -eq 1
                }
                if (-not $matches) {
                    $lastResult += ', unexpected health payload'
                }
            }
            if ($matches) {
                Write-Host "OK: $Name returned HTTP $ExpectedStatus"
                return
            }
        } catch {
            $lastResult = $_.Exception.Message
        }
        Start-Sleep -Seconds 2
    }
    throw "Timed out waiting for $Name at $Url. Last result: $lastResult"
}

function Save-ComposeDiagnostics {
    $statusPath = Join-Path $ArtifactDirectory 'compose-ps.txt'
    $logsPath = Join-Path $ArtifactDirectory 'compose-logs.txt'
    $encoding = [System.Text.UTF8Encoding]::new($false)
    $previousErrorActionPreference = $ErrorActionPreference
    try {
        # Diagnostics must preserve Docker stderr instead of turning it into a second failure.
        $ErrorActionPreference = 'Continue'
        $statusArguments = Get-ComposeArguments -Arguments @('ps', '--all', '--no-trunc')
        $statusOutput = @(@(& docker @statusArguments 2>&1) | ForEach-Object { $_.ToString() })
        [System.IO.File]::WriteAllLines($statusPath, [string[]]$statusOutput, $encoding)

        $logArguments = Get-ComposeArguments -Arguments @('logs', '--no-color', '--timestamps')
        $logOutput = @(@(& docker @logArguments 2>&1) | ForEach-Object { $_.ToString() })
        [System.IO.File]::WriteAllLines($logsPath, [string[]]$logOutput, $encoding)
    } finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }
}

$databasePassword = 'db-' + [Guid]::NewGuid().ToString('N')
$jwtSecret = 'jwt-' + [Guid]::NewGuid().ToString('N') + [Guid]::NewGuid().ToString('N')
$composeEnvironmentPath = $environmentFile.Replace('\', '/')
$environmentValues = @(
    "NOHOME_ENV_FILE=$composeEnvironmentPath",
    'POSTGRES_DB=no_home_smoke',
    'POSTGRES_USER=no_home_smoke',
    "POSTGRES_PASSWORD=$databasePassword",
    'POSTGRES_PORT=0',
    'BACKEND_PORT=0',
    'FRONTEND_PORT=0',
    'SPRING_PROFILES_ACTIVE=prod',
    'DB_URL=jdbc:postgresql://postgres:5432/no_home_smoke',
    'DB_USERNAME=no_home_smoke',
    "DB_PASSWORD=$databasePassword",
    'DB_MAX_POOL_SIZE=5',
    'DB_MIN_IDLE=0',
    "JWT_SECRET=$jwtSecret",
    'JWT_COOKIE_SECURE=true',
    'PUBLIC_DATA_SERVICE_KEY=',
    'PUBLIC_DATA_APT_RENT_SERVICE_KEY=',
    'KAKAO_MAP_API_KEY=',
    'SSAFY_GMS_API_KEY=',
    'VITE_KAKAO_MAP_API_KEY='
) -join [Environment]::NewLine
[System.IO.File]::WriteAllText(
    $environmentFile,
    $environmentValues,
    [System.Text.UTF8Encoding]::new($false)
)

$failure = $null
$cleanupFailure = $null
try {
    Write-Host "Starting isolated Compose smoke project: $projectName"
    Invoke-Compose -Arguments @('config', '--quiet')
    $existingContainers = @(
        (Invoke-ComposeOutput -Arguments @('ps', '--quiet')) |
            Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
    )
    if ($existingContainers.Count -ne 0) {
        throw "The generated Compose project unexpectedly already has containers: $projectName"
    }

    Invoke-Compose -Arguments @('up', '--build', '--detach')
    $backendPort = Get-PublishedPort -Service 'backend' -ContainerPort 8080
    $frontendPort = Get-PublishedPort -Service 'frontend' -ContainerPort 5173
    $backendHealthUrl = "http://127.0.0.1:$backendPort/api/health"
    $frontendUrl = "http://127.0.0.1:$frontendPort/"
    $frontendHealthUrl = "http://127.0.0.1:$frontendPort/api/health"

    Wait-HttpState -Name 'Backend health' -Url $backendHealthUrl -ExpectedStatus 200 `
        -TimeoutSeconds $StartupTimeoutSeconds -RequireNoStore $true `
        -ExpectedDatabaseConnected $true -ExpectedApplicationStatus 'UP'
    Wait-HttpState -Name 'Frontend root' -Url $frontendUrl -ExpectedStatus 200 `
        -TimeoutSeconds $StartupTimeoutSeconds
    Wait-HttpState -Name 'Frontend health proxy' -Url $frontendHealthUrl -ExpectedStatus 200 `
        -TimeoutSeconds $StartupTimeoutSeconds -RequireNoStore $true `
        -ExpectedDatabaseConnected $true -ExpectedApplicationStatus 'UP'

    Invoke-Compose -Arguments @('stop', '--timeout', '10', 'postgres')
    Wait-HttpState -Name 'Backend degraded health' -Url $backendHealthUrl -ExpectedStatus 503 `
        -TimeoutSeconds $TransitionTimeoutSeconds -RequireNoStore $true `
        -ExpectedDatabaseConnected $false -ExpectedApplicationStatus 'DEGRADED'
    Wait-HttpState -Name 'Frontend degraded health proxy' -Url $frontendHealthUrl -ExpectedStatus 503 `
        -TimeoutSeconds $TransitionTimeoutSeconds -RequireNoStore $true `
        -ExpectedDatabaseConnected $false -ExpectedApplicationStatus 'DEGRADED'

    Invoke-Compose -Arguments @('start', 'postgres')
    Wait-HttpState -Name 'Backend recovered health' -Url $backendHealthUrl -ExpectedStatus 200 `
        -TimeoutSeconds $StartupTimeoutSeconds -RequireNoStore $true `
        -ExpectedDatabaseConnected $true -ExpectedApplicationStatus 'UP'
    Wait-HttpState -Name 'Frontend recovered health proxy' -Url $frontendHealthUrl -ExpectedStatus 200 `
        -TimeoutSeconds $StartupTimeoutSeconds -RequireNoStore $true `
        -ExpectedDatabaseConnected $true -ExpectedApplicationStatus 'UP'

    Write-Host 'OK: Docker Compose smoke test passed'
} catch {
    $failure = $_.Exception
} finally {
    try {
        Save-ComposeDiagnostics
    } catch {
        Write-Warning "Could not save complete Compose diagnostics: $($_.Exception.Message)"
    }
    try {
        Invoke-Compose -Arguments @('down', '--volumes', '--remove-orphans', '--rmi', 'local', '--timeout', '10')
    } catch {
        $cleanupFailure = $_.Exception
    }
    if (Test-Path -LiteralPath $environmentFile) {
        Remove-Item -LiteralPath $environmentFile -Force
    }
    $httpClient.Dispose()
}

$resourceFilters = @(
    @('ps', '--all', '--quiet', '--filter', "label=com.docker.compose.project=$projectName"),
    @('network', 'ls', '--quiet', '--filter', "label=com.docker.compose.project=$projectName"),
    @('volume', 'ls', '--quiet', '--filter', "label=com.docker.compose.project=$projectName")
)
foreach ($arguments in $resourceFilters) {
    $remainingResources = @(
        @(& docker @arguments) | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
    )
    if ($remainingResources.Count -ne 0 -and $null -eq $cleanupFailure) {
        $cleanupFailure = [InvalidOperationException]::new(
            "Compose cleanup left resources for project ${projectName}: $($remainingResources -join ', ')"
        )
    }
}

if ($null -ne $failure) {
    Write-Host "Compose diagnostics: $ArtifactDirectory"
    throw $failure
}
if ($null -ne $cleanupFailure) {
    Write-Host "Compose diagnostics: $ArtifactDirectory"
    throw $cleanupFailure
}
Write-Host "Compose diagnostics: $ArtifactDirectory"
