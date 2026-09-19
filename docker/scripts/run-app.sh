#!/usr/bin/env bash
# 在 toolbox container 內編譯並執行 Auction Sniper 這個 Swing app，
# 視窗顯示在主機的 X display 上。沿用同一個 toolbox container，
# 不用另外開新的 Docker 環境。
#
# auctionsniper.Main 目前一定要收到 hostname/username/password/itemId 四個
# 參數（見 src/auctionsniper/Main.java），username/password 預設用
# test/end-to-end 那組 sniper/sniper 測試帳號。
#
# Usage:
#   docker/scripts/run-app.sh <itemId> [username] [password]
#   e.g. docker/scripts/run-app.sh item-54321
set -euo pipefail

cd "$(dirname "$0")/.."

if [ $# -lt 1 ]; then
  echo "usage: $0 <itemId> [username] [password]" >&2
  exit 1
fi

ITEM_ID=$1
SNIPER_USERNAME=${2:-sniper}
SNIPER_PASSWORD=${3:-sniper}

xhost +localhost > /dev/null 2>&1 || true

docker compose up -d --build --no-deps toolbox
docker compose exec \
  -e DISPLAY=host.docker.internal:0 \
  -e ITEM_ID="$ITEM_ID" \
  -e SNIPER_USERNAME="$SNIPER_USERNAME" \
  -e SNIPER_PASSWORD="$SNIPER_PASSWORD" \
  toolbox bash -c '
  set -euo pipefail
  PROJ=/app
  BUILD=$PROJ/build-docker
  APP_CP=$(ls "$PROJ"/lib/deploy/*.jar | tr "\n" ":")

  echo "== compiling app =="
  mkdir -p "$BUILD/app"
  javac -d "$BUILD/app" -cp "$APP_CP" -sourcepath "$PROJ/src" $(find "$PROJ/src" -name "*.java")

  echo "== launching auctionsniper.Main =="
  java -cp "$BUILD/app:$APP_CP" auctionsniper.Main localhost "$SNIPER_USERNAME" "$SNIPER_PASSWORD" "$ITEM_ID"
'
