param(
    [switch]$IncludeArchive
)

$ErrorActionPreference = 'Stop'
$repositoryRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$docsRoot = Join-Path $repositoryRoot 'docs'
$archiveRoot = [System.IO.Path]::GetFullPath((Join-Path $docsRoot 'archive'))
$linkPattern = [regex]'!?\[[^\]]*\]\((?<target><[^>]+>|[^)\s]+)(?:\s+[^)]*)?\)'

$markdownFiles = @((Join-Path $repositoryRoot 'README.md'))
$markdownFiles += Get-ChildItem -LiteralPath $docsRoot -Recurse -File -Filter '*.md' |
    ForEach-Object { $_.FullName }

if (-not $IncludeArchive) {
    $markdownFiles = $markdownFiles | Where-Object {
        -not [System.IO.Path]::GetFullPath($_).StartsWith(
            $archiveRoot + [System.IO.Path]::DirectorySeparatorChar,
            [System.StringComparison]::OrdinalIgnoreCase
        )
    }
}

$failures = [System.Collections.Generic.List[string]]::new()
$checkedLinks = 0

foreach ($source in ($markdownFiles | Sort-Object -Unique)) {
    $content = Get-Content -LiteralPath $source -Raw -Encoding utf8
    foreach ($match in $linkPattern.Matches($content)) {
        $rawTarget = $match.Groups['target'].Value.Trim('<', '>')
        if ($rawTarget -match '^[A-Za-z][A-Za-z0-9+.-]*:' -or $rawTarget.StartsWith('//')) {
            continue
        }

        $pathPart = ($rawTarget -split '[?#]', 2)[0]
        if ([string]::IsNullOrWhiteSpace($pathPart)) {
            continue
        }

        $checkedLinks++
        $decodedPath = [System.Uri]::UnescapeDataString($pathPart).Replace('/', [System.IO.Path]::DirectorySeparatorChar)
        if ($decodedPath.StartsWith([System.IO.Path]::DirectorySeparatorChar)) {
            $target = Join-Path $repositoryRoot $decodedPath.TrimStart([System.IO.Path]::DirectorySeparatorChar)
        } else {
            $target = Join-Path (Split-Path -Parent $source) $decodedPath
        }

        if (-not (Test-Path -LiteralPath $target)) {
            $relativeSource = $source.Substring($repositoryRoot.Length).TrimStart(
                [System.IO.Path]::DirectorySeparatorChar
            )
            $failures.Add("${relativeSource}: $rawTarget")
        }
    }
}

if ($failures.Count -gt 0) {
    Write-Host "Broken local links: $($failures.Count)"
    $failures | ForEach-Object { Write-Host "- $_" }
    exit 1
}

$scope = if ($IncludeArchive) { 'all documents' } else { 'current documents' }
Write-Host "OK: $checkedLinks local links across $scope"
