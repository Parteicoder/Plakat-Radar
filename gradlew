#!/usr/bin/env sh
set -e

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
GRADLE_VERSION="8.10.2"
GRADLE_ZIP="gradle-${GRADLE_VERSION}-bin.zip"
GRADLE_URL="https://services.gradle.org/distributions/${GRADLE_ZIP}"
GRADLE_DIR="$APP_HOME/.gradle/bootstrap/gradle-${GRADLE_VERSION}"
GRADLE_BIN="$GRADLE_DIR/bin/gradle"
ZIP_PATH="$APP_HOME/.gradle/bootstrap/${GRADLE_ZIP}"

if [ ! -x "$GRADLE_BIN" ]; then
  mkdir -p "$APP_HOME/.gradle/bootstrap"
  if [ ! -f "$ZIP_PATH" ]; then
    echo "Downloading Gradle ${GRADLE_VERSION}..."
    if command -v curl >/dev/null 2>&1; then
      curl -L -o "$ZIP_PATH" "$GRADLE_URL"
    elif command -v wget >/dev/null 2>&1; then
      wget -O "$ZIP_PATH" "$GRADLE_URL"
    else
      echo "curl or wget is required to download Gradle." >&2
      exit 1
    fi
  fi
  echo "Extracting Gradle ${GRADLE_VERSION}..."
  unzip -q -o "$ZIP_PATH" -d "$APP_HOME/.gradle/bootstrap"
fi

exec "$GRADLE_BIN" "$@"
