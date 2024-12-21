#!/bin/bash

set -e

ALL_PROJECTS=(
  ":compiler-plugin"
  ":compiler-plugin-gradle"
  ":runtime"
)

for PROJECT in "${ALL_PROJECTS[@]}"; do
  echo -e "Publishing $PROJECT"
  ./gradlew "${PROJECT}:publishToMavenCentral"
done
