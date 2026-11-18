#!/usr/bin/env bash
# 在 toolbox container 內編譯一次 app 再跑測試，兩種測試都跑同一份編譯結果。
# 不指定 unit、e2e 時，先跑單元測試再跑 E2E 測試。
#
# E2E 測試預設用 xvfb-run（虛擬 display，畫面不會顯示出來）；加上 --headed 參數
# 則改用真實的 DISPLAY，讓 Swing UI 顯示在主機螢幕上，方便除錯。
#
# Usage: scripts/test.sh [unit|e2e] [--headed]
set -euo pipefail

cd "$(dirname "$0")/.."

run_unit=true
run_e2e=true
case "${1:-}" in
  unit) run_e2e=false; shift ;;
  e2e) run_unit=false; shift ;;
esac

headed=false
case "${1:-}" in
  "") ;;
  --headed) headed=true ;;
  *)
    echo "usage: $0 [unit|e2e] [--headed]" >&2
    exit 1
    ;;
esac

exec_opts=()
if [[ "$run_e2e" == true && "$headed" == true ]]; then
  xhost +local:docker > /dev/null 2>&1 || true
  exec_opts=(-e DISPLAY=host.docker.internal:0)
fi

scripts/env.sh up

docker compose exec ${exec_opts[@]+"${exec_opts[@]}"} toolbox bash -c '
  set -euo pipefail
  run_unit=$1
  run_e2e=$2
  headed=$3

  PROJ=/app
  BUILD=$PROJ/build-docker
  APP_CP=$(ls "$PROJ"/lib/deploy/*.jar | tr "\n" ":")
  DEV_CP=$(ls "$PROJ"/lib/develop/*.jar | grep -v -- "-src.jar" | tr "\n" ":")

  compile() {
    local src=$1 out=$2 cp=$3
    rm -rf "$out"
    mkdir -p "$out"
    javac -d "$out" -cp "$cp" -sourcepath "$src" $(find "$src" -name "*.java")
  }

  # $1：測試 class 編譯結果的目錄，$2：測試 class 名稱的 regex
  test_classes() {
    (cd "$1" && find . -name "*.class" ! -name "*\$*" | sed "s|^\./||; s|\.class$||; s|/|.|g" | grep -E "$2")
  }

  echo "== compiling app =="
  compile "$PROJ/src" "$BUILD/app" "$APP_CP"

  if [[ "$run_unit" == true ]]; then
    echo "== compiling unit tests =="
    compile "$PROJ/test/unit" "$BUILD/unit-test" "$BUILD/app:$APP_CP$DEV_CP"

    echo "== running unit tests =="
    classes=$(test_classes "$BUILD/unit-test" "Tests?$")
    java -cp "$BUILD/app:$BUILD/unit-test:$APP_CP$DEV_CP" org.junit.runner.JUnitCore $classes
  fi

  if [[ "$run_e2e" == true ]]; then
    echo "== compiling end-to-end tests =="
    compile "$PROJ/test/end-to-end" "$BUILD/e2e-test" "$BUILD/app:$APP_CP$DEV_CP"

    echo "== running end-to-end tests =="
    display_wrapper=(xvfb-run -a)
    if [[ "$headed" == true ]]; then
      display_wrapper=()
    fi
    classes=$(test_classes "$BUILD/e2e-test" "Test$")
    "${display_wrapper[@]}" java -cp "$BUILD/e2e-test:$BUILD/app:$APP_CP$DEV_CP" org.junit.runner.JUnitCore $classes
  fi
' _ "$run_unit" "$run_e2e" "$headed"
