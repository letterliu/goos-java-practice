# Fake Auction：兩種啟動與手動測試流程

本文件記錄目前 `FakeAuction` 的兩種啟動方式。

目前已驗證兩種方式都可以正常執行：

1. **Script 流程（目前建議）**：由 `start-env.sh` 負責建立 Docker 環境，`run-app.sh` 與 `fake-auction.sh` 只負責在既有的 `toolbox` container 中啟動各自的程式。
2. **手動流程**：完全手動管理 Docker environment，再使用 `docker compose exec` 分別啟動 Auction Sniper 與 FakeAuction。

兩種流程目前都保留，之後如果 Script 流程長期穩定，可以再視需要移除手動流程。

---

## 一、Script 流程（目前建議）

### 目的

同時執行：

```bash
./docker/scripts/run-app.sh item-54321
```

以及：

```bash
./docker/scripts/fake-auction.sh item-54321
```

讓 Auction Sniper 與 FakeAuction 同時存在於同一個 `toolbox` container 中，但各自擁有獨立的 interactive `docker compose exec` session。

### 為什麼目前兩支 script 不再執行 `docker compose up`

原本兩支 script 都會執行：

```bash
docker compose up -d --build --no-deps toolbox
```

這讓 script 可以獨立執行，但當兩個 interactive script 同時執行時，會產生問題：

```text
Terminal A
run-app.sh
    ↓
docker compose up toolbox
    ↓
docker compose exec toolbox
    ↓
Auction Sniper


Terminal B
fake-auction.sh
    ↓
docker compose up toolbox
    ↓
重新啟動 toolbox
    ↓
Terminal A 的 exec session 被中斷
```

因此目前的責任分工改成：

```text
start-env.sh
    ↓
負責 Docker environment lifecycle
    ↓
Openfire + toolbox


run-app.sh
    ↓
docker compose exec toolbox
    ↓
Auction Sniper


fake-auction.sh
    ↓
docker compose exec toolbox
    ↓
FakeAuction
```

`--no-deps` 原本解決的是 `docker compose up toolbox` 不要順便啟動／重建 Openfire；它並不能阻止 `toolbox` 自己被重新建立或重啟。

因此目前兩支 interactive script 都不再執行 `docker compose up`。

---

### Step 1：啟動完整環境

第一次使用或環境已被關閉時：

```bash
./docker/scripts/start-env.sh
```

確認：

```bash
docker compose -f docker/docker-compose.yml ps
```

應該看到：

```text
docker-openfire-1   ...   Up
docker-toolbox-1    ...   Up
```

---

### Step 2：啟動 Auction Sniper

Terminal A：

```bash
./docker/scripts/run-app.sh item-54321
```

正常情況下會開啟 Auction Sniper Swing UI，並進入：

```text
Joining
```

此 script 不會建立或重啟 `toolbox`，只會使用：

```bash
docker compose exec toolbox
```

---

### Step 3：啟動 FakeAuction

Terminal B：

```bash
./docker/scripts/fake-auction.sh item-54321
```

正常情況下：

```text
Selling item item-54321 as auction-item-54321@localhost/Auction.
Waiting for a sniper to join...
```

接著應看到：

```text
Sniper joined: sniper@localhost/Auction
```

並進入：

```text
>>>
```

此時 Terminal A 的 Auction Sniper 視窗應該仍然存在。

**Terminal B 不應出現：**

```text
✔ toolbox Built
✔ Container docker-toolbox-1 Started
```

因為 `fake-auction.sh` 不再負責啟動 toolbox。

---

### Step 4：送出 CLOSE event

在 Terminal B：

```text
Event: CLOSE;
```

Auction Sniper 應從：

```text
Joining
```

變成：

```text
Lost
```

目前這個 commit 的功能範圍只需要驗證這個行為。

---

### Step 5：結束程式

FakeAuction：

```text
quit
```

Auction Sniper：

```text
Ctrl+C
```

如果要完整關閉 Docker environment：

```bash
docker compose -f docker/docker-compose.yml down
```

---

## 二、手動流程

這個流程保留作為目前的 fallback / troubleshooting 方法。

它的主要用途是：

* 排查 `docker compose` lifecycle 問題
* 確認兩個 interactive `exec` session 是否可以同時存在
* 在 Script 流程出現問題時，隔離「Docker environment 問題」與「Java 程式問題」

---

### Step 1：關閉現有 environment

```bash
docker compose -f docker/docker-compose.yml down
```

---

### Step 2：只啟動 Openfire

```bash
docker compose -f docker/docker-compose.yml up -d openfire
```

等待 Openfire 啟動完成。

---

### Step 3：建立並啟動 toolbox

```bash
docker compose -f docker/docker-compose.yml up -d --build --no-deps toolbox
```

這裡的 `--no-deps` 是為了避免啟動 toolbox 時操作 Openfire。

---

### Step 4：準備 XQuartz

在 macOS host：

```bash
xhost +localhost
```

可以用：

```bash
xhost
```

確認看到類似：

```text
INET:localhost
INET6:localhost
LOCAL:
```

---

### Step 5：Terminal A 啟動 Auction Sniper

進入 toolbox：

```bash
docker compose -f docker/docker-compose.yml exec \
  -e DISPLAY=host.docker.internal:0 \
  -e ITEM_ID=item-54321 \
  -e SNIPER_USERNAME=sniper \
  -e SNIPER_PASSWORD=sniper \
  toolbox bash
```

進入 container 後：

```bash
PROJ=/app
BUILD=$PROJ/build-docker
APP_CP=$(ls "$PROJ"/lib/deploy/*.jar | tr "\n" ":")

mkdir -p "$BUILD/app"

javac \
  -d "$BUILD/app" \
  -cp "$APP_CP" \
  -sourcepath "$PROJ/src" \
  $(find "$PROJ/src" -name "*.java")
```

接著：

```bash
java \
  -cp "$BUILD/app:$APP_CP" \
  auctionsniper.Main \
  localhost \
  "$SNIPER_USERNAME" \
  "$SNIPER_PASSWORD" \
  "$ITEM_ID"
```

Auction Sniper 應該開啟 Swing UI。

---

### Step 6：Terminal B 啟動 FakeAuction

另外開一個 terminal：

```bash
docker compose -f docker/docker-compose.yml exec toolbox bash
```

進入 container 後：

```bash
PROJ=/app
TOOLS=$PROJ/docker/tools
APP_CP=$(ls "$PROJ"/lib/deploy/*.jar | tr "\n" ":")

javac \
  -cp "$APP_CP" \
  -d "$TOOLS" \
  "$TOOLS/FakeAuction.java"
```

接著：

```bash
java \
  -cp "$TOOLS:$APP_CP" \
  FakeAuction \
  item-54321
```

正常情況下會看到：

```text
Selling item item-54321 as auction-item-54321@localhost/Auction.
Waiting for a sniper to join...
```

以及：

```text
Sniper joined: sniper@localhost/Auction
```

---

### Step 7：驗證 CLOSE

在 FakeAuction terminal：

```text
Event: CLOSE;
```

Auction Sniper 應該從：

```text
Joining
```

變成：

```text
Lost
```

---

### Step 8：驗證 PRICE

可以輸入：

```text
Event: PRICE; CurrentPrice: 90; Increment: 5; Bidder: other bidder;
```

目前這個 commit 的 `Main.currentPrice()` 尚未實作 UI 狀態轉換，因此 **PRICE event 會被送出，但不應期待 UI 進入 `Bidding`**。

這是目前程式進度的預期行為，不是環境錯誤。

---

### Step 9：結束

FakeAuction：

```text
quit
```

Auction Sniper：

```text
Ctrl+C
```

最後關閉 environment：

```bash
docker compose -f docker/docker-compose.yml down
```

---

## 三、兩種流程的差異

| 項目                    | Script 流程         | 手動流程                       |
| --------------------- | ----------------- | -------------------------- |
| 啟動 Docker environment | `start-env.sh`    | 手動 `docker compose`        |
| Openfire              | `start-env.sh` 負責 | 手動啟動                       |
| toolbox               | `start-env.sh` 負責 | 手動啟動                       |
| Auction Sniper        | `run-app.sh`      | 手動 `docker compose exec`   |
| FakeAuction           | `fake-auction.sh` | 手動 `docker compose exec`   |
| Interactive session   | 各自獨立              | 各自獨立                       |
| 是否適合日常使用              | **是**             | Troubleshooting / fallback |
| 是否保留                  | **保留**            | **目前保留**                   |

---

## 四、目前推薦的日常流程

環境已關閉時：

```bash
./docker/scripts/start-env.sh
```

然後 Terminal A：

```bash
./docker/scripts/run-app.sh item-54321
```

Terminal B：

```bash
./docker/scripts/fake-auction.sh item-54321
```

FakeAuction：

```text
Event: CLOSE;
```

預期：

```text
Auction Sniper
Joining → Lost
```

---

## 五、目前驗證結果

已實際驗證以下情境：

1. `docker compose exec toolbox` 可以建立多個同時存在的 interactive sessions。
2. `run-app.sh` 不再啟動／重建 toolbox。
3. `fake-auction.sh` 不再啟動／重建 toolbox。
4. `run-app.sh item-54321` 與 `fake-auction.sh item-54321` 可以同時執行。
5. FakeAuction 可以收到：

   ```text
   Sniper joined: sniper@localhost/Auction
   ```
6. Auction Sniper 視窗在 FakeAuction 啟動後仍然存在。
7. FakeAuction 發送：

   ```text
   Event: CLOSE;
   ```

   後，Auction Sniper 正常進入：

   ```text
   Lost
   ```
8. 從完全關閉的 Docker environment 開始，先執行 `start-env.sh`，再執行兩支 script，整個流程也已驗證成功。

因此目前兩種流程都可以保留；日後若 Script 流程已穩定且不再需要 troubleshooting，再考慮移除手動流程。
