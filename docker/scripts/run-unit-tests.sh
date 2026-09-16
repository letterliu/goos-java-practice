#!/usr/bin/env bash
# Compiles and runs the unit tests inside the toolbox container.
set -euo pipefail

PROJ=/app
BUILD=$PROJ/build-docker

APP_CP=$(ls "$PROJ"/lib/deploy/*.jar | tr '\n' ':')
DEV_CP=$(ls "$PROJ"/lib/develop/*.jar | grep -v -- '-src.jar' | tr '\n' ':')

compile() {
  local src=$1 out=$2 cp=$3
  rm -rf "$out"
  mkdir -p "$out"
  javac -d "$out" -cp "$cp" -sourcepath "$src" $(find "$src" -name '*.java')
}

echo "== compiling app =="
compile "$PROJ/src" "$BUILD/app" "$APP_CP"

echo "== compiling unit tests =="
compile "$PROJ/test/unit" "$BUILD/unit-test" "$BUILD/app:$APP_CP$DEV_CP"

echo "== running unit tests =="
classes=$(cd "$BUILD/unit-test" && find . -name '*.class' ! -name '*\$*' | sed 's|^\./||; s|\.class$||; s|/|.|g' | grep -E 'Tests?$')

java -cp "$BUILD/app:$BUILD/unit-test:$APP_CP$DEV_CP" org.junit.runner.JUnitCore $classes
