#!/usr/bin/env bash
# Does stuff to initialize project dependencies & database for the first time

# Exit on error
set -e

# Prompt the user if they want to install Java Temurin SDK 23
echo "Do you want to install Java Temurin SDK 23? (y/n)"
read -r INSTALL_JAVA

if [[ "$INSTALL_JAVA" == y* ]]; then
  # If so, install the Temurin SDK.
  wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo gpg --dearmor | sudo tee /etc/apt/trusted.gpg.d/adoptium.gpg > /dev/null
  echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
  sudo apt update && sudo apt install -y temurin-23-jdk
fi

echo "Initializing the project..."

# Find the root directory of the bash script file
DIR="$(dirname "$0")"

"$DIR/gradlew" build
"$DIR/gradlew" updateDatabase