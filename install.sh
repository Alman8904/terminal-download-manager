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

JAVA_VERSION=$(java -XshowSettings:properties -version 2>&1 |
    awk '/java.specification.version =/ {print $3; exit}')

if [[ -z "$JAVA_VERSION" ]] || ! [[ "$JAVA_VERSION" =~ ^[0-9]+([.][0-9]+)*$ ]]; then
    echo "Error: Could not determine the Java version." >&2
    exit 1
fi

JAVA_MAJOR="${JAVA_VERSION%%.*}"
if (( JAVA_MAJOR < 21 )); then
    echo "Error: Java 21 or later is required. Found Java $JAVA_VERSION." >&2
    exit 1
fi

command -v curl >/dev/null 2>&1 || {
    echo "Error: curl is required." >&2
    exit 1
}

mkdir -p "$INSTALL_DIR" "$BIN_DIR"

echo "Downloading termget..."
curl --fail --location --silent --show-error \
    "$JAR_URL" -o "$INSTALL_DIR/termget.jar"

cat > "$BIN_DIR/termget" <<'LAUNCHER'
#!/usr/bin/env bash
exec java -jar "$HOME/.local/share/termget/termget.jar" "$@"
LAUNCHER

chmod +x "$BIN_DIR/termget"

echo "termget installed."
if [[ ":$PATH:" != *":$BIN_DIR:"* ]]; then
    echo 'Add this to ~/.bashrc:'
    echo 'export PATH="$HOME/.local/bin:$PATH"'
    echo 'Then reopen your terminal.'
else
    echo 'Usage: termget "https://example.com/file.zip"'
fi