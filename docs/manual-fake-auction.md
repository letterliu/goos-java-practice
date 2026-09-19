# 手動啟動 Auction Sniper 與 FakeAuction

當同時執行 `run-app.sh` 與 `fake-auction.sh` 時，兩個 script 都會執行 `docker compose up ... toolbox`。由於兩個 interactive session 共用同一個 `docker-toolbox-1` container，後啟動的 script 可能影響先啟動的 interactive session。

遇到這種情況時，可以先手動啟動 Openfire 與 toolbox，再讓兩個 terminal 都使用 `docker compose exec` 進入已存在的 toolbox container。

這個流程不修改 `run-app.sh` 或 `fake-auction.sh`，只是用來隔離 toolbox container lifecycle，確認 Auction Sniper 與 FakeAuction 本身是否正常運作。

## 1. 關閉目前環境

先關閉 Auction Sniper、FakeAuction，以及其他正在執行的相關 command。

然後執行：

```bash
docker compose -f docker/docker-compose.yml down
```

確認：

```bash
docker compose -f docker/docker-compose.yml ps
```

沒有 running container。

## 2. 啟動 Openfire

執行：

```bash
docker compose -f docker/docker-compose.yml up -d openfire
```

確認：

```bash
docker compose -f docker/docker-compose.yml ps
```

Openfire 必須處於 `Up` 狀態。

## 3. 只啟動一次 toolbox

執行：

```bash
docker compose -f docker/docker-compose.yml up -d --build --no-deps toolbox
```

確認：

```bash
docker compose -f docker/docker-compose.yml ps
```

應該看到 Openfire 與 toolbox 都處於 running 狀態。

> 這一步完成後，不要再從其他 terminal 執行 `docker compose up toolbox`。後續兩個 interactive session 都只使用 `docker compose exec toolbox`。

## 4. 設定 macOS XQuartz

執行：

```bash
xhost +localhost
```

確認：

```bash
xhost
```

輸出中應包含：

```text
INET:localhost
INET6:localhost
```

## 5. Terminal A：啟動 Auction Sniper

開啟一個新的 VS Code Terminal，執行：

```bash
docker compose -f docker/docker-compose.yml exec \
  -e DISPLAY=host.docker.internal:0 \
  -e ITEM_ID=item-54321 \
  -e SNIPER_USERNAME=sniper \
  -e SNIPER_PASSWORD=sniper \
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
```

應看到：

```text
== compiling app ==
== launching auctionsniper.Main ==
```

並開啟 Auction Sniper Swing UI。

UI 此時應顯示：

```text
Joining
```

先不要關閉這個 terminal。

## 6. Terminal B：啟動 FakeAuction

開啟另一個 VS Code Terminal，執行：

```bash
docker compose -f docker/docker-compose.yml exec toolbox bash -c '
  set -euo pipefail
  PROJ=/app
  TOOLS=$PROJ/docker/tools
  APP_CP=$(ls "$PROJ"/lib/deploy/*.jar | tr "\n" ":")

  javac -cp "$APP_CP" -d "$TOOLS" "$TOOLS/FakeAuction.java"
  java -cp "$TOOLS:$APP_CP" FakeAuction item-54321
'
```

應看到：

```text
Selling item item-54321 as auction-item-54321@localhost/Auction. Waiting for a sniper to join...

Type a SOL message body (without the "SOLVersion: 1.1; " prefix) to send it, e.g.:
  Event: PRICE; CurrentPrice: 90; Increment: 5; Bidder: other bidder;
  Event: CLOSE;
Type "quit" to disconnect and exit.

>>>
```

接著應收到：

```text
Sniper joined: sniper@localhost/Auction
```

此時應同時確認：

* Terminal A 的 Auction Sniper UI 顯示 `Joining`
* Terminal B 顯示 `Sniper joined: sniper@localhost/Auction`
* 兩個 terminal 都仍然保持執行

## 7. 測試 CLOSE

在 Terminal B 的 `>>>` 提示字元輸入：

```text
Event: CLOSE;
```

應看到：

```text
sent: SOLVersion: 1.1; Event: CLOSE;
```

Auction Sniper UI 應變成：

```text
Lost
```

這可以確認 FakeAuction 發出的 `CLOSE` event 能被 Auction Sniper 正常接收與處理。

## 8. 測試 PRICE

在 Terminal B 輸入：

```text
Event: PRICE; CurrentPrice: 90; Increment: 5; Bidder: other bidder;
```

應看到：

```text
sent: SOLVersion: 1.1; Event: PRICE; CurrentPrice: 90; Increment: 5; Bidder: other bidder;
```

目前這個 commit 階段不需要期待 UI 變成 `Bidding`。

`Main.currentPrice(int, int)` 尚未實作完整的 UI 狀態轉換，因此 PRICE event 可以正常送出與解析，但 UI 不會因此進入 `Bidding`。

## 9. 結束 FakeAuction

在 Terminal B 輸入：

```text
quit
```

FakeAuction 應正常離開。

然後在 Terminal A 使用：

```text
Ctrl+C
```

結束 Auction Sniper。

## 10. 關閉 Docker 環境

確認：

```bash
docker compose -f docker/docker-compose.yml ps
```

如果不再需要測試環境，可以執行：

```bash
docker compose -f docker/docker-compose.yml down
```

## 預期結果

完整流程應為：

```text
Openfire
   ↓
toolbox（只啟動一次）
   ↓
   ├── Terminal A：Auction Sniper
   │        ↓
   │      Joining
   │
   └── Terminal B：FakeAuction
            ↓
       Sniper joined
            ↓
       Event: CLOSE;
            ↓
       Auction Sniper → Lost
```

如果這個手動流程可以正常完成，表示 Auction Sniper、FakeAuction、Openfire 與目前的 X11 設定可以正常協同運作；兩個 interactive script 直接同時執行時遇到的 terminal 干擾，則可以視為 toolbox container lifecycle 的問題，而不是 Auction Sniper 或 FakeAuction 功能本身的問題。
