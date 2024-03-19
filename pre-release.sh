#!/bin/bash

# Get the version from the pom.xml file
version=$(./mvnw -q -pl client -Dexec.executable="echo" -Dexec.args="\${project.version}" exec:exec)

# Check if the version is empty
if [ -z "$version" ]; then
  echo "Error: Unable to get version from pom.xml file."
  exit 1
fi

# Set version in the client file
sed -i "s/const val CLIENT_VERSION = \".*/const val CLIENT_VERSION = \"${version}\"/" client/src/main/kotlin/dev/medzik/librepass/client/Client.kt

echo "Version: ${version}"
