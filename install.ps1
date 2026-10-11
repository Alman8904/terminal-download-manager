```powershell
$ErrorActionPreference = "Stop"

$Repo = "Alman8904/terminal-download-manager"
$JarUrl = "https://github.com/$Repo/releases/latest/download/termget.jar"

$InstallDir = Join-Path $env:LOCALAPPDATA "termget"
$BinDir = Join-Path $env:USERPROFILE ".local\bin"
$JarPath = Join-Path $InstallDir "termget.jar"
$TempJar = Join-Path $InstallDir "termget.jar.tmp"
$LauncherPath = Join-Path $BinDir "termget.cmd"

try {
    # Check Java availability.
    if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
        throw "Java 21 or newer is required. Install Java and reopen your terminal."
    }

    $VersionOutput = (& java -version 2>&1 | Out-String)

    if ($VersionOutput -notmatch 'version "(\d+)') {
        throw "Could not determine the Java version."
    }

    $JavaVersion = [int]$Matches[1]

    if ($JavaVersion -lt 21) {
        throw "Java 21 or newer is required. Found Java $JavaVersion."
    }

    # Create installation directories.
    New-Item -ItemType Directory -Force -Path $InstallDir, $BinDir |
        Out-Null

    # Download to a temporary file first.
    Remove-Item $TempJar -Force -ErrorAction SilentlyContinue

    Write-Host "Downloading termget..."
    Invoke-WebRequest -Uri $JarUrl -OutFile $TempJar

    if (-not (Test-Path $TempJar) -or
        (Get-Item $TempJar).Length -eq 0) {
        throw "The downloaded JAR is empty or missing."
    }

    # Verify that the file is a readable JAR with the expected main class.
    Add-Type -AssemblyName System.IO.Compression
    $Archive = [System.IO.Compression.ZipFile]::OpenRead($TempJar)

    try {
        $ManifestEntry = $Archive.GetEntry("META-INF/MANIFEST.MF")

        if (-not $ManifestEntry) {
            throw "The downloaded file has no JAR manifest."
        }

        $Reader = [System.IO.StreamReader]::new($ManifestEntry.Open())
        try {
            $Manifest = $Reader.ReadToEnd()
        }
        finally {
            $Reader.Dispose()
        }

        if ($Manifest -notmatch '(?m)^Main-Class:\s*Main\s*$') {
            throw "The JAR manifest does not declare Main as its main class."
        }
    }
    finally {
        $Archive.Dispose()
    }

    # Replace the application JAR only after validation.
    Move-Item -Path $TempJar -Destination $JarPath -Force

    # Create a launcher usable from CMD and PowerShell.
    @'
@echo off
java -jar "%LOCALAPPDATA%\termget\termget.jar" %*
exit /b %ERRORLEVEL%
'@ | Set-Content -Path $LauncherPath -Encoding ASCII

    # Add the launcher directory to the user's persistent PATH.
    $UserPath = [Environment]::GetEnvironmentVariable("Path", "User")
    $PathEntries = @(
        $UserPath -split ";" |
        Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
    )

    $AlreadyPresent = @(
        $PathEntries |
        Where-Object {
            $_.TrimEnd("\") -ieq $BinDir.TrimEnd("\")
        }
    ).Count -gt 0

    if (-not $AlreadyPresent) {
        $NewUserPath = if ([string]::IsNullOrWhiteSpace($UserPath)) {
            $BinDir
        } else {
            "$UserPath;$BinDir"
        }

        [Environment]::SetEnvironmentVariable("Path", $NewUserPath, "User")
    }

    # Make termget available in the current PowerShell session.
    $CurrentEntries = @(
        $env:Path -split ";" |
        Where-Object { $_.TrimEnd("\") -ieq $BinDir.TrimEnd("\") }
    )

    if ($CurrentEntries.Count -eq 0) {
        $env:Path = "$BinDir;$env:Path"
    }

    Write-Host ""
    Write-Host "termget installed successfully."
    Write-Host 'Usage: termget "https://example.com/file.zip"'
    Write-Host "Downloaded files are saved in your Downloads folder."
    Write-Host "If the command is not recognized in a new terminal, reopen it."
}
catch {
    Write-Error $_
    exit 1
}
finally {
    Remove-Item $TempJar -Force -ErrorAction SilentlyContinue
}
```