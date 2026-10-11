
$ErrorActionPreference = "Stop"

$Repo = "Alman8904/terminal-download-manager"
$JarUrl = "https://github.com/$Repo/releases/latest/download/termget.jar"

$InstallDir = Join-Path $env:LOCALAPPDATA "termget"
$BinDir = Join-Path $env:USERPROFILE ".local\bin"
$JarPath = Join-Path $InstallDir "termget.jar"
$TempJar = Join-Path $InstallDir "termget.jar.tmp"
$LauncherPath = Join-Path $BinDir "termget.cmd"

try {
    # Locate Java without printing its output.
    $JavaCommand = Get-Command java -ErrorAction SilentlyContinue

    if (-not $JavaCommand) {
        throw "Java 21 or newer is required. Install Java and reopen your terminal."
    }

    # Capture stdout and stderr directly through .NET.
    $StartInfo = New-Object System.Diagnostics.ProcessStartInfo
    $StartInfo.FileName = $JavaCommand.Source
    $StartInfo.Arguments = "-version"
    $StartInfo.UseShellExecute = $false
    $StartInfo.CreateNoWindow = $true
    $StartInfo.RedirectStandardOutput = $true
    $StartInfo.RedirectStandardError = $true

    $JavaProcess = New-Object System.Diagnostics.Process
    $JavaProcess.StartInfo = $StartInfo

    [void]$JavaProcess.Start()

    $StdOutTask = $JavaProcess.StandardOutput.ReadToEndAsync()
    $StdErrTask = $JavaProcess.StandardError.ReadToEndAsync()

    $JavaProcess.WaitForExit()

    $VersionOutput = $StdOutTask.Result + "`n" + $StdErrTask.Result
    $JavaProcess.Dispose()

    $VersionMatch = [regex]::Match(
        $VersionOutput,
        'version "(\d+)'
    )

    if (-not $VersionMatch.Success) {
        throw "Could not determine the installed Java version."
    }

    $JavaVersion = [int]$VersionMatch.Groups[1].Value

    if ($JavaVersion -lt 21) {
        throw "Java 21 or newer is required. Found Java $JavaVersion."
    }

    # Prepare installation directories.
    New-Item -ItemType Directory -Force -Path $InstallDir, $BinDir |
        Out-Null

    Remove-Item $TempJar -Force -ErrorAction SilentlyContinue

    # Download the release JAR.
    Write-Host "Downloading termget..."
    Invoke-WebRequest -Uri $JarUrl -OutFile $TempJar

    if (-not (Test-Path $TempJar) -or
        (Get-Item $TempJar).Length -eq 0) {
        throw "The downloaded JAR is empty or missing."
    }

    # Validate the JAR manifest before replacing the installed JAR.
    Add-Type -AssemblyName System.IO.Compression
    Add-Type -AssemblyName System.IO.Compression.FileSystem

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

    # Install the validated JAR.
    Move-Item -Path $TempJar -Destination $JarPath -Force

    # Create the command launcher.
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
        }
        else {
            "$UserPath;$BinDir"
        }

        [Environment]::SetEnvironmentVariable("Path", $NewUserPath, "User")
    }

    # Update PATH in the current PowerShell session.
    $CurrentEntries = @(
        $env:Path -split ";" |
        Where-Object {
            $_.TrimEnd("\") -ieq $BinDir.TrimEnd("\")
        }
    )

    if ($CurrentEntries.Count -eq 0) {
        $env:Path = "$BinDir;$env:Path"
    }

    Write-Host ""
    Write-Host "termget installed successfully."
    Write-Host 'Usage: termget "https://example.com/file.zip"'
    Write-Host "Downloads are saved in your Downloads folder."
}
catch {
    Write-Host "Installation failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
finally {
    Remove-Item $TempJar -Force -ErrorAction SilentlyContinue
}
