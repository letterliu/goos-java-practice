#!/usr/bin/env bash
# 管理 Docker 環境（toolbox + Openfire）。
#
# Usage:
#   scripts/env.sh up     啟動環境。還沒有 Openfire 資料時，先用 Playwright 跑完
#                         setup wizard、建立測試帳號。其他腳本會自動呼叫，不用
#                         手動跑。不加 --build：Java 程式碼是 bind mount 進來的，
#                         不在 image 裡；image 不存在時 Compose 會自動 build。
#   scripts/env.sh start  重新 build toolbox image 並啟動環境，改了
#                         toolbox.Dockerfile 後要跑這個。
#   scripts/env.sh stop   移除 container、toolbox image 與 Openfire 資料 volume。
#                         不再使用時要跑這個，否則這些資源會留在 Docker 裡。
#   scripts/env.sh reset  stop 再 start（會重新跑一次 setup wizard）。
set -euo pipefail

cd "$(dirname "$0")/.."

openfire_volume_exists() {
  local project
  project=$(docker compose config | sed -n '1s/^name: //p')
  docker volume inspect "${project}_openfire-data" > /dev/null 2>&1
}

# host port 由 Docker 分配，偶爾會分配到已被佔用的 port，重試時會換一個
compose_up() {
  local output
  for attempt in 1 2 3 4 5; do
    if output=$(docker compose --progress quiet up -d "$@" 2>&1); then
      return 0
    fi
    grep -q 'address already in use' <<< "$output" || break
  done
  echo "$output" >&2
  return 1
}

setup_openfire() {
  if [ ! -f node_modules/playwright/package.json ]; then
    echo "== installing playwright (first run) =="
    npm install
  fi
  npx playwright install chromium

  # 中途失敗或被中斷時，不留下沒設定完成的 Openfire，下次執行時才會重跑 wizard
  trap 'docker compose down --volumes' EXIT

  echo "== starting openfire =="
  compose_up openfire

  # host port 由 Docker 分配，要查詢才知道
  local admin_port
  admin_port=$(docker compose port openfire 9090)
  export OPENFIRE_ADMIN_URL="http://localhost:${admin_port##*:}"

  echo "== waiting for $OPENFIRE_ADMIN_URL =="
  for i in $(seq 1 60); do
    curl -sf "$OPENFIRE_ADMIN_URL" > /dev/null && break
    sleep 1
  done

  echo "== running Openfire setup wizard + test accounts =="
  node scripts/setup-openfire.js

  trap - EXIT
}

up() {
  if ! openfire_volume_exists; then
    setup_openfire
  fi

  compose_up toolbox
}

start() {
  echo "== building toolbox =="
  docker compose build toolbox

  up

  echo "== environment ready. run scripts/test.sh to execute the tests =="
}

stop() {
  echo "== removing containers, toolbox image and openfire data volume =="
  docker compose down --volumes --rmi local
}

case "${1:-}" in
  up) up ;;
  start) start ;;
  stop) stop ;;
  reset)
    stop
    start
    ;;
  *)
    echo "usage: $0 up | start | stop | reset" >&2
    exit 1
    ;;
esac