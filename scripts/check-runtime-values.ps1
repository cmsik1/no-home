[CmdletBinding()]
param(
    [string]$RepositoryRoot,
    [string[]]$Paths
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($RepositoryRoot)) {
    $RepositoryRoot = Join-Path $PSScriptRoot '..'
}
$RepositoryRoot = [System.IO.Path]::GetFullPath($RepositoryRoot)

if (-not $Paths) {
    $Paths = @(
        '.env.example',
        'docker-compose.yml',
        'render.yaml',
        'Backend/Dockerfile',
        'Backend/src/main',
        'Frontend/.env.example',
        'Frontend/Dockerfile',
        'Frontend/deployment',
        'Frontend/src',
        'Frontend/vercel.ts'
    )
}

$textExtensions = @(
    '.css', '.html', '.java', '.js', '.json', '.jsx', '.properties',
    '.sql', '.ts', '.tsx', '.xml', '.yaml', '.yml'
)
$files = [System.Collections.Generic.List[System.IO.FileInfo]]::new()

foreach ($relativePath in $Paths) {
    $candidate = Join-Path $RepositoryRoot $relativePath
    if (-not (Test-Path -LiteralPath $candidate)) {
        throw "Runtime scan path does not exist: $relativePath"
    }

    # PowerShell on Unix treats dotfiles as hidden and requires -Force even for Get-Item.
    $item = Get-Item -LiteralPath $candidate -Force
    if ($item.PSIsContainer) {
        Get-ChildItem -LiteralPath $candidate -Recurse -File -Force | Where-Object {
            $textExtensions -contains $_.Extension.ToLowerInvariant()
        } | ForEach-Object { $files.Add($_) }
    } else {
        $files.Add($item)
    }
}

# Report only the rule and path. Printing the matched value would leak the secret into CI logs.
$rules = [ordered]@{
    'OpenAI-style API token' = '(?<![A-Za-z0-9_-])sk-(?:proj-)?[A-Za-z0-9_-]{20,}'
    'GitHub token' = '(?<![A-Za-z0-9_])gh[pousr]_[A-Za-z0-9]{20,}'
    'Google API key' = '(?<![A-Za-z0-9_-])AIza[0-9A-Za-z_-]{35}'
    'AWS access key' = '(?<![A-Z0-9])(?:AKIA|ASIA)[A-Z0-9]{16}(?![A-Z0-9])'
    'Private key material' = '-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----'
    'Hardcoded cloud deployment host' = '(?i)(?:[a-z0-9-]+\.)+(?:onrender\.com|vercel\.app|neon\.tech)'
}

$failures = [System.Collections.Generic.List[string]]::new()
foreach ($file in ($files | Sort-Object FullName -Unique)) {
    $content = [System.IO.File]::ReadAllText($file.FullName)
    foreach ($rule in $rules.GetEnumerator()) {
        if ([regex]::IsMatch($content, $rule.Value)) {
            $relativeFile = $file.FullName
            if ($relativeFile.StartsWith($RepositoryRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
                $relativeFile = $relativeFile.Substring($RepositoryRoot.Length).TrimStart(
                    [System.IO.Path]::DirectorySeparatorChar,
                    [System.IO.Path]::AltDirectorySeparatorChar
                )
            }
            $failures.Add("${relativeFile}: $($rule.Key)")
        }
    }
}

if ($failures.Count -gt 0) {
    Write-Host "Unsafe runtime values: $($failures.Count)"
    $failures | ForEach-Object { Write-Host "- $_" }
    exit 1
}

Write-Host "OK: $($files.Count) runtime files contain no known secret or deployment-host patterns"
