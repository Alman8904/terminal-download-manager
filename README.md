# termget

A Java-based terminal download manager that supports parallel downloads when the server supports HTTP range requests, with sequential fallback when it doesn't.

## 🐧 Linux

### Installation

**Requirements:** Java 21 or newer and `curl`.

Open a terminal and run:

```
curl -fsSL https://raw.githubusercontent.com/Alman8904/terminal-download-manager/master/install.sh | bash
```

The installer downloads and installs termget automatically. No administrator privileges are required.

### Usage

To download a file, run:

```
termget "https://example.com/file.zip"
```

Replace the example URL with a direct download link to the file you want.

Downloaded files are saved in `~/Downloads`.

### Uninstallation

Run:

```
rm -rf "$HOME/.local/share/termget" "$HOME/.local/bin/termget"
```

This removes termget and its launcher but does not delete your downloaded files.

### Troubleshooting

See the [Troubleshooting Guide](https://github.com/Alman8904/terminal-download-manager/blob/master/TROUBLESHOOTING.md#linux).

## 🪟 Windows

### Installation

**Requirement:** Java 21 or newer.

Open PowerShell and run:

```
irm https://raw.githubusercontent.com/Alman8904/terminal-download-manager/master/install.ps1 | iex
```

The installer downloads and installs termget without requiring administrator privileges.

### Usage

To download a file, run this in Command Prompt or PowerShell:

```
termget "https://example.com/file.zip"
```

Replace the example URL with a direct download link to the file you want.

Downloaded files are saved in your Windows Downloads folder.

### Uninstallation

Open PowerShell and run:

```
Remove-Item "$env:LOCALAPPDATA\termget" -Recurse -Force
Remove-Item "$env:USERPROFILE\.local\bin\termget.cmd" -Force -ErrorAction SilentlyContinue
```

This removes termget and its launcher but does not delete your downloaded files. The install directory may remain in your user PATH.

### Troubleshooting

See the [Troubleshooting Guide](https://github.com/Alman8904/terminal-download-manager/blob/master/TROUBLESHOOTING.md#windows).

## Features

- Parallel downloads using Java virtual threads
- Sequential fallback when the server doesn't support range requests
- Live download progress, speed, and ETA
- Crash recovery for parallel downloads
- Automatic saving to the user's Downloads folder

## Requirements

### Linux

- Java 21 or newer
- `curl`
- A compatible Linux distribution

### Windows

- Java 21 or newer
- Windows PowerShell or PowerShell 7 for installation
- Command Prompt or PowerShell for usage
