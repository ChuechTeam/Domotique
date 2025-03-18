#!/usr/bin/env bash
# Does stuff to initialize project dependencies & database for the first time

# Exit on error
set -e

# Execute the "java --version" command and put the output in a variable
# If the output contains "openjdk 23", then skip it!
if JAVA_VERSION=$(java --version 2>&1) && { [[ "$JAVA_VERSION" == *"openjdk 23"* ]] || [[ "$JAVA_VERSION" == *"build 23"* ]]; }; then
  echo "Java Temurin SDK 23 is already installed (and is default)!"
else
  # Prompt the user if they want to install Java Temurin SDK 23
  echo -n "Do you want to install Java Temurin SDK 23? (Y/N) "
  read -r INSTALL_JAVA

  if [[ "$INSTALL_JAVA" == y* ]] || [[ "$INSTALL_JAVA" == Y* ]]; then
    # If so, install the Temurin SDK.
    echo "Installing JDK 23..."
    wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo gpg --dearmor | sudo tee /etc/apt/trusted.gpg.d/adoptium.gpg > /dev/null
    echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
    sudo apt update && sudo apt install -y temurin-23-jdk
  fi
fi

echo "Initializing the project and setting up the database..."

# Find the root directory of the bash script file
DIR="$(dirname "$0")"

"$DIR/gradlew" assemble
"$DIR/gradlew" updateDatabase