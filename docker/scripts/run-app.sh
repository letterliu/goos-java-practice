#!/usr/bin/env bash
# 在 toolbox container 內編譯並執行 Auction Sniper 這個 Swing app，
# 視窗顯示在主機的 X display 上。沿用同一個 toolbox container，
# 不用另外開新的 Docker 環境。
set -euo pipefail

cd "$(dirname "$0")/.."

xhost +local:docker > /dev/null 2>&1 || true

docker compose up -d --build --no-deps toolbox
docker compose exec \
  -e DISPLAY=host.docker.internal:0 \
  -e XMPP_HOSTNAME \
  -e XMPP_USERNAME \
  -e XMPP_PASSWORD \
  -e ITEM_ID \
  toolbox bash -c '
  set -euo pipefail
  PROJ=/app
  BUILD=$PROJ/build-docker
  APP_CP=$(ls "$PROJ"/lib/deploy/*.jar | tr "\n" ":")

  echo "== compiling app =="
  mkdir -p "$BUILD/app"
  javac -d "$BUILD/app" -cp "$APP_CP" -sourcepath "$PROJ/src" $(find "$PROJ/src" -name "*.java")

  echo "== launching auctionsniper.Main =="
	java -cp "$BUILD/app:$APP_CP" auctionsniper.Main \
  "$XMPP_HOSTNAME" \
  "$XMPP_USERNAME" \
  "$XMPP_PASSWORD" \
  "$ITEM_ID"
'
