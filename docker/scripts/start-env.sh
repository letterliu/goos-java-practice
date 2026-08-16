#!/usr/bin/env bash
# 啟動 openfire + toolbox container，跑 Playwright 腳本把 Openfire 的
# setup wizard 跑完並建立測試帳號，讓環境準備好可以直接跑 scripts/test.sh。
set -euo pipefail

cd "$(dirname "$0")/.."

echo "== starting openfire =="
sudo docker compose up -d openfire

echo "== waiting for http://localhost:9090 =="
for i in $(seq 1 30); do
  curl -sf http://localhost:9090 > /dev/null && break
  sleep 1
done

echo "== building/starting toolbox =="
sudo docker compose build toolbox
sudo docker compose up -d toolbox

if [ ! -d node_modules ]; then
  echo "== installing playwright (first run) =="
  npm install
fi
npx playwright install chromium

echo "== running Openfire setup wizard + test accounts =="
node setup-openfire.js

echo "== environment ready. run docker/scripts/test.sh to execute the tests =="
