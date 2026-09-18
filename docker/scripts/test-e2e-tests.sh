#!/usr/bin/env bash
# Runs the end-to-end tests inside the toolbox container.
# 加上 --headed 參數可以讓測試執行時的 Swing UI 顯示在主機螢幕上（預設是
# xvfb 虛擬 display，看不到畫面），方便除錯。
set -euo pipefail

cd "$(dirname "$0")/.."

docker compose up -d --build --no-deps toolbox

if [[ "${1:-}" == "--headed" ]]; then
  xhost +local:docker > /dev/null 2>&1 || true
  docker compose exec \
    -e DISPLAY=host.docker.internal:0 \
    toolbox bash /app/docker/scripts/run-e2e-tests.sh --headed
else
  docker compose exec toolbox bash /app/docker/scripts/run-e2e-tests.sh
fi