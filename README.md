# termget

A Java-based terminal download manager that supports parallel downloads when the server supports HTTP range requests, with sequential fallback when it doesn't.

## Linux

### Installation

**Requirements:** Java 21 or newer and `curl`.

Open a terminal and run:

```bash
curl -fsSL https://raw.githubusercontent.com/Alman8904/terminal-download-manager/master/install.sh | bash
```

The installer downloads and installs termget automatically. No administrator privileges are required.

### Usage

To download a file, run:

```bash
termget "https://example.com/file.zip"
```

Replace the example URL with a direct download link to the file you want.

Downloaded files are saved in `~/Downloads`.

### Uninstallation

Run:

```bash
rm -rf "$HOME/.local/share/termget" "$HOME/.local/bin/termget"
```

This removes termget and its launcher but does not delete your downloaded files.

### Troubleshooting

See the [Troubleshooting Guide](TROUBLESHOOTING.md#linux).

## Windows

### Installation

Windows installation instructions will be added when Windows support is ready.

### Usage

Windows usage instructions will be added with the Windows release.

### Uninstallation

Windows uninstallation instructions will be added with the Windows release.

### Troubleshooting

Windows troubleshooting instructions will be added with the Windows release.

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

Requirements will be documented with the Windows release.

