[CmdletBinding()]
param(
    [string]$RepositoryRoot
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($RepositoryRoot)) {
    $RepositoryRoot = Join-Path $PSScriptRoot '..'
}
$RepositoryRoot = [System.IO.Path]::GetFullPath($RepositoryRoot)

function Assert-Command {
    param([Parameter(Mandatory)][string]$Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Required command is not available: $Name"
    }
}

function Assert-Pattern {
    param(
        [Parameter(Mandatory)][string]$Content,
        [Parameter(Mandatory)][string]$Pattern,
        [Parameter(Mandatory)][string]$Message
    )

    if (-not [regex]::IsMatch($Content, $Pattern)) {
        throw $Message
    }
}

function Invoke-External {
    param(
        [Parameter(Mandatory)][string]$Command,
        [Parameter(Mandatory)][string[]]$Arguments
    )

    & $Command @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Command failed with exit code ${LASTEXITCODE}: $Command $($Arguments -join ' ')"
    }
}

$requiredFiles = @(
    '.env.example',
    'docker-compose.yml',
    'render.yaml',
    'Frontend/.env.example',
    'Frontend/Dockerfile',
    'Frontend/deployment/vercelConfig.test.js',
    'Frontend/package.json',
    'scripts/check-markdown-links.ps1',
    'scripts/check-runtime-values.ps1'
)
foreach ($relativeFile in $requiredFiles) {
    if (-not (Test-Path -LiteralPath (Join-Path $RepositoryRoot $relativeFile) -PathType Leaf)) {
        throw "Required deployment file is missing: $relativeFile"
    }
}

$rootEnvironment = Get-Content -LiteralPath (Join-Path $RepositoryRoot '.env.example') -Raw -Encoding utf8
foreach ($requiredVariable in @('DB_URL', 'DB_USERNAME', 'DB_PASSWORD', 'JWT_SECRET', 'JWT_COOKIE_SECURE')) {
    Assert-Pattern -Content $rootEnvironment -Pattern "(?m)^${requiredVariable}=" `
        -Message ".env.example must declare $requiredVariable"
}
foreach ($optionalVariable in @(
    'PUBLIC_DATA_SERVICE_KEY',
    'PUBLIC_DATA_APT_RENT_SERVICE_KEY',
    'KAKAO_MAP_API_KEY',
    'SSAFY_GMS_API_KEY'
)) {
    Assert-Pattern -Content $rootEnvironment -Pattern "(?m)^${optionalVariable}=\s*$" `
        -Message ".env.example must keep optional key $optionalVariable empty"
}

$frontendEnvironment = Get-Content -LiteralPath (Join-Path $RepositoryRoot 'Frontend/.env.example') -Raw -Encoding utf8
Assert-Pattern -Content $frontendEnvironment -Pattern '(?m)^BACKEND_ORIGIN=\s*$' `
    -Message 'Frontend/.env.example must declare an empty BACKEND_ORIGIN placeholder'

$frontendPackage = Get-Content -LiteralPath (Join-Path $RepositoryRoot 'Frontend/package.json') -Raw -Encoding utf8 |
    ConvertFrom-Json
if ($frontendPackage.engines.node -ne '24.x') {
    throw 'Frontend package.json must declare engines.node as 24.x for Vercel and local tooling'
}

$frontendDockerfile = Get-Content -LiteralPath (Join-Path $RepositoryRoot 'Frontend/Dockerfile') -Raw -Encoding utf8
Assert-Pattern -Content $frontendDockerfile -Pattern '(?m)^FROM\s+node:24-alpine\s*$' `
    -Message 'Frontend Dockerfile must use the Node 24 Alpine runtime'
Write-Host 'OK: Frontend Node 24 runtime contract'

$renderConfig = Get-Content -LiteralPath (Join-Path $RepositoryRoot 'render.yaml') -Raw -Encoding utf8
$renderContracts = [ordered]@{
    'Render service must use the web type' = '(?m)^\s*-\s+type:\s*web\s*$'
    'Render service must use Docker' = '(?m)^\s+runtime:\s*docker\s*$'
    'Render health check must use /api/health' = '(?m)^\s+healthCheckPath:\s*/api/health\s*$'
    'Render automatic deployment must stay disabled through M2' = '(?m)^\s+autoDeployTrigger:\s*["'']?off["'']?\s*$'
    'Render must activate the prod profile' = '(?ms)^\s*-\s+key:\s*SPRING_PROFILES_ACTIVE\s*\r?\n\s+value:\s*prod\s*$'
    'Render must use secure authentication cookies' = '(?ms)^\s*-\s+key:\s*JWT_COOKIE_SECURE\s*\r?\n\s+value:\s*["'']?true["'']?\s*$'
}
foreach ($contract in $renderContracts.GetEnumerator()) {
    Assert-Pattern -Content $renderConfig -Pattern $contract.Value -Message $contract.Key
}
foreach ($secretVariable in @('DB_URL', 'DB_USERNAME', 'DB_PASSWORD', 'JWT_SECRET')) {
    Assert-Pattern -Content $renderConfig `
        -Pattern "(?ms)^\s*-\s+key:\s*${secretVariable}\s*\r?\n\s+sync:\s*false\s*$" `
        -Message "Render secret $secretVariable must be entered through the Dashboard (sync: false)"
}
Write-Host 'OK: environment examples and Render deployment contract'

Assert-Command -Name 'docker'
$temporaryEnvironment = [System.IO.Path]::GetTempFileName()
try {
    $composeEnvironmentPath = $temporaryEnvironment.Replace('\', '/')
    $temporaryValues = @(
        "NOHOME_ENV_FILE=$composeEnvironmentPath",
        'POSTGRES_DB=no_home_preflight',
        'POSTGRES_USER=no_home_preflight',
        'POSTGRES_PASSWORD=preflight-only-database-password',
        'DB_URL=jdbc:postgresql://postgres:5432/no_home_preflight',
        'DB_USERNAME=no_home_preflight',
        'DB_PASSWORD=preflight-only-database-password',
        'JWT_SECRET=preflight-only-jwt-secret-at-least-32-characters',
        'JWT_COOKIE_SECURE=false',
        'PUBLIC_DATA_SERVICE_KEY=',
        'PUBLIC_DATA_APT_RENT_SERVICE_KEY=',
        'KAKAO_MAP_API_KEY=',
        'SSAFY_GMS_API_KEY=',
        'VITE_KAKAO_MAP_API_KEY='
    ) -join [Environment]::NewLine
    [System.IO.File]::WriteAllText(
        $temporaryEnvironment,
        $temporaryValues,
        [System.Text.UTF8Encoding]::new($false)
    )

    Invoke-External -Command 'docker' -Arguments @(
        'compose',
        '--project-directory', $RepositoryRoot,
        '--env-file', $temporaryEnvironment,
        '-f', (Join-Path $RepositoryRoot 'docker-compose.yml'),
        'config', '--quiet'
    )
    Write-Host 'OK: Docker Compose configuration and required interpolation values'
} finally {
    if (Test-Path -LiteralPath $temporaryEnvironment) {
        Remove-Item -LiteralPath $temporaryEnvironment -Force
    }
}

Assert-Command -Name 'node'
Push-Location (Join-Path $RepositoryRoot 'Frontend')
try {
    Invoke-External -Command 'node' -Arguments @('--test', 'deployment/vercelConfig.test.js')
} finally {
    Pop-Location
}
Write-Host 'OK: Vercel rewrite contract'

& (Join-Path $PSScriptRoot 'check-runtime-values.ps1') -RepositoryRoot $RepositoryRoot
if ($LASTEXITCODE -ne 0) {
    throw 'Runtime secret and deployment-host scan failed'
}

& (Join-Path $PSScriptRoot 'check-markdown-links.ps1')
if ($LASTEXITCODE -ne 0) {
    throw 'Current documentation link scan failed'
}

Write-Host 'OK: deployment preflight checks passed'
