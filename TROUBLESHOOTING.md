# Troubleshooting

## Linux

### `termget: command not found`

Close and reopen your terminal, then try again.

If it still doesn't work, run:

```bash
export PATH="$HOME/.local/bin:$PATH"
```

Try `termget` again. This change lasts only for the current terminal session. For a permanent fix, add the same line to `~/.bashrc`.

### Java is not installed

termget requires Java 21 or newer.

Check your Java version:

```bash
java -version
```

If Java is missing or older than 21, install a compatible Java runtime and rerun the installer.

### Installation failed

Check that you have an internet connection and `curl` is installed:

```bash
curl --version
```

Then rerun the installation command from the README.

### Download failed

Make sure you're using a **direct download link** to a file, not a webpage, search result, or preview page.

For example:

```bash
termget "https://example.com/file.zip"
```

Some websites don't support parallel downloads. termget should fall back to sequential downloading when the server doesn't support HTTP range requests.

### Where are my downloads?

Downloaded files are saved in:

```text
~/Downloads
```

### Still having problems?

Open an issue in the GitHub repository and include the error message from your terminal. Do not post private download links, passwords, or access tokens.


## Windows

### `termget` is not recognized

Close and reopen Command Prompt or PowerShell, then try again.

### Java is not installed

termget requires Java 21 or newer.

Check your Java version:

```powershell
java -version
```

If Java is missing or older than 21, install a compatible Java runtime and rerun the installer.

### Installation failed

Check that you have an internet connection, then rerun the installation command from the README.

### Download failed

Make sure you're using a **direct download link** to a file, not a webpage, search result, or preview page.

For example:

```powershell
termget "https://example.com/file.zip"
```

Some websites don't support parallel downloads. termget should fall back to sequential downloading when the server doesn't support HTTP range requests.

### Where are my downloads?

Downloaded files are saved in your Windows **Downloads** folder.

### Still having problems?

Open an issue in the GitHub repository and include the error message from your terminal. Do not post private download links, passwords, or access tokens.
