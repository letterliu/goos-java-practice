#!/usr/bin/env bash
# 互動式假拍賣工具 -- 手動送出 PRICE/CLOSE 事件，驅動已加入拍賣的 sniper。
# 可用指令見 docker/tools/FakeAuction.java 裡的說明（等它印出 ">>> " 提示字元
# 後就可以輸入）。
#
# Usage:
#   docker/scripts/fake-auction.sh <itemId>
#   e.g. docker/scripts/fake-auction.sh item-54321
set -euo pipefail

cd "$(dirname "$0")/.."

if [ $# -lt 1 ]; then
  echo "usage: $0 <itemId>" >&2
  exit 1
fi

docker compose up -d --build --no-deps toolbox
docker compose exec toolbox bash -c '
  set -euo pipefail
  PROJ=/app
  TOOLS=$PROJ/docker/tools
  APP_CP=$(ls "$PROJ"/lib/deploy/*.jar | tr "\n" ":")

  javac -cp "$APP_CP" -d "$TOOLS" "$TOOLS/FakeAuction.java"
  java -cp "$TOOLS:$APP_CP" FakeAuction '"$@"'
'
