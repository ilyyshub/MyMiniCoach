#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p out/test
find src/main/java src/test/java -name '*.java' > out/test-sources.txt
javac --release 21 -encoding UTF-8 -d out/test @out/test-sources.txt
cp -R src/main/resources/. out/test/
exec java -cp out/test com.minicoach.CoachTest
