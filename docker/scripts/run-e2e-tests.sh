#!/usr/bin/env bash
# Compiles and runs the end-to-end tests inside the toolbox container.
# 預設用 xvfb-run（虛擬 display，畫面不會顯示出來）；加上 --headed 參數則
# 改用真實的 DISPLAY，讓測試執行時的 Swing UI 能顯示在主機螢幕上，方便除錯
# （要搭配 docker/scripts/test.sh --headed 一起用，負責把 DISPLAY 傳進 container）。
set -euo pipefail

PROJ=/app
BUILD=$PROJ/build-docker

APP_CP=$(ls "$PROJ"/lib/deploy/*.jar | tr '\n' ':')
DEV_CP=$(ls "$PROJ"/lib/develop/*.jar | grep -v -- '-src.jar' | tr '\n' ':')

compile() {
  local src=$1 out=$2 cp=$3
  mkdir -p "$out"
  javac -d "$out" -cp "$cp" -sourcepath "$src" $(find "$src" -name '*.java')
}

echo "== compiling app =="
compile "$PROJ/src" "$BUILD/app" "$APP_CP"

echo "== compiling end-to-end tests =="
compile "$PROJ/test/end-to-end" "$BUILD/e2e-test" "$BUILD/app:$APP_CP$DEV_CP"

echo "== waiting for Openfire (localhost:9090) =="
for i in $(seq 1 30); do
  curl -sf http://localhost:9090 > /dev/null && break
  sleep 1
done

echo "== running end-to-end tests =="
classes=$(cd "$BUILD/e2e-test" && find . -name '*.class' ! -name '*\$*' | sed 's|^\./||; s|\.class$||; s|/|.|g' | grep -E 'Test$')

if [[ "${1:-}" == "--headed" ]]; then
  java -cp "$BUILD/e2e-test:$BUILD/app:$APP_CP$DEV_CP" org.junit.runner.JUnitCore $classes
else
  xvfb-run -a java -cp "$BUILD/e2e-test:$BUILD/app:$APP_CP$DEV_CP" org.junit.runner.JUnitCore $classes
fi
