#!/usr/bin/env bash
# Does stuff to initialize project dependencies & database for the first time

# Exit on error
set -e

# Execute the "java --version" command and put the output in a variable
# If the output contains "openjdk 23", then skip it!
if JAVA_VERSION=$(java --version 2>&1) && { [[ "$JAVA_VERSION" == *"openjdk 23"* ]] || [[ "$JAVA_VERSION" == *"build 23"* ]]; }; then
  echo "✅ Java Temurin SDK 23 is already installed (and is default)!"
else
  # Prompt the user if they want to install Java Temurin SDK 23
  echo -n "🔍 Do you want to install Java Temurin SDK 23? (Y/N) "
  read -r INSTALL_JAVA

  if [[ "$INSTALL_JAVA" == y* ]] || [[ "$INSTALL_JAVA" == Y* ]]; then
    # If so, install the Temurin SDK.
    echo "🚀 Installing JDK 23... You may be asked to input your password!"
    wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo gpg --dearmor | sudo tee /etc/apt/trusted.gpg.d/adoptium.gpg > /dev/null
    echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
    sudo apt update && sudo apt install -y temurin-23-jdk
    echo "✅ Java Temurin SDK 23 is now installed as the default JDK!"
  fi
fi

# Check if npm is installed
if command -v npm &> /dev/null; then
  echo "✅ NPM is already installed!"
else
  # Prompt the user if they want to install NPM
  echo -n "🔍 Do you want to install Node.js and NPM? (Y/N) "
  read -r INSTALL_NPM

  if [[ "$INSTALL_NPM" == y* ]] || [[ "$INSTALL_NPM" == Y* ]]; then
    # Install npm which includes nodejs
    echo "🚀 Installing Node.js and NPM... You may be asked to input your password!"
    sudo apt-get install -y npm
    echo "✅ Node.js and NPM are now installed!"
  fi
fi

# Find the root directory of the bash script file
DIR=$(realpath "$(dirname "$0")")

# Change the current directory to $DIR; we could probably change it back but honestly... not worth the hassle
cd "$DIR" || exit

echo "🏗️ Initializing the Java project (src/back)..."
./gradlew classes
echo "✅ Java project initialized!"

echo "🏗️ Initializing the Vue project (src/front)..."
cd "$DIR/src/front" || exit
npm install
echo "✅ Vue project initialized!"

echo "🏗️ Initializing the database..."
cd "$DIR" || exit
if ! ./gradlew updateDatabase; then
  echo "❌ Failed to initialize the database! Make sure the username and the password are correct in src/back/resources/config-dev-local.properties"
  exit 1
else
  echo "✅ Database initialized!"
fi

echo "🚀 Setup complete! 🎉"