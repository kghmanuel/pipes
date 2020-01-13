# use this sceipt to generate the JAR file containing the Pipes UI
# it will appear in java-middle-tier/build/libs
# the version is controlled in java-middle-tier/build.gradle

#!/bin/sh

# build the front end SPA UI
quasar build

# create static resource directory first
mkdir -p java-middle-tier/src/main/resources/static

# deploy to resources/static to be picked up by jar builder
cp -r dist/spa/* java-middle-tier/src/main/resources/static/.


# create dhf resource directory first
mkdir -p java-middle-tier/src/main/resources/dhf

# deploy backend modules to be picked up by jar builder
cp -r dist_user/dhf/* java-middle-tier/src/main/resources/dhf/.

java-middle-tier/gradlew bootJar -p java-middle-tier