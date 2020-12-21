#!/usr/bin/env bash
cd "$(dirname "$0")"
pwd
./gradlew publishToMavenLocal
