#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p out/main
find src/main/java -name '*.java' > out/main-sources.txt
javac --release 21 -encoding UTF-8 -d out/main @out/main-sources.txt
cp -R src/main/resources/. out/main/
exec java -cp out/main com.minicoach.Main "$@"
