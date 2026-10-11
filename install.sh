
#!/usr/bin/env bash
set -euo pipefail

REPO="Alman8904/terminal-download-manager"
JAR_URL="https://github.com/${REPO}/releases/latest/download/termget.jar"
INSTALL_DIR="$HOME/.local/share/termget"
BIN_DIR="$HOME/.local/bin"

if ! command -v java >/dev/null 2>&1; then
    echo "Error: Java 21 or later is required." >&2
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 |
    sed -nE 's/.*version "([0-9]+).*/\1/p' |
    head -n 1)

if [[ -z "$JAVA_VERSION" ]] || ! [[ "$JAVA_VERSION" =~ ^[0-9]+$ ]]; then
    echo "Error: Could not determine the Java version." >&2
    exit 1
fi

if (( JAVA_VERSION < 21 )); then
    echo "Error: Java 21 or later is required. Found Java $JAVA_VERSION." >&2
    exit 1
fi

if ! command -v curl >/dev/null 2>&1; then
    echo "Error: curl is required." >&2
    exit 1
fi

mkdir -p "$INSTALL_DIR" "$BIN_DIR"

TEMP_JAR="$INSTALL_DIR/termget.jar.tmp"
trap 'rm -f "$TEMP_JAR"' EXIT

echo "Downloading termget..."
curl --fail --location --silent --show-error \
    "$JAR_URL" -o "$TEMP_JAR"

mv "$TEMP_JAR" "$INSTALL_DIR/termget.jar"

cat > "$BIN_DIR/termget" <<'LAUNCHER'
#!/usr/bin/env bash
exec java -jar "$HOME/.local/share/termget/termget.jar" "$@"
LAUNCHER

chmod +x "$BIN_DIR/termget"

echo "termget installed successfully."

if [[ ":$PATH:" != *":$BIN_DIR:"* ]]; then
    echo "$BIN_DIR is not currently on PATH."
    echo 'For Bash, add this line to ~/.bashrc:'
    echo 'export PATH="$HOME/.local/bin:$PATH"'
    echo "Then reopen your terminal."
else
    echo 'Usage: termget "https://example.com/file.zip"'
fi
