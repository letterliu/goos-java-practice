# Docker 環境

## Java

Java 的編譯、執行、測試一律透過 `docker-compose.yml` 定義的 `toolbox` container 執行，不在 host 直接跑 `javac` / `java`。

| 腳本 | 用途 |
| --- | --- |
| `scripts/test.sh` | 依序執行 `test-unit-tests.sh` 與 `test-e2e-tests.sh`（`test.sh --headed` 會轉給 `test-e2e-tests.sh`） |
| `scripts/test-unit-tests.sh` | 編譯並執行 unit 測試（不用 XMPP server，也沒有 Swing UI，不需要 `xvfb-run`） |
| `scripts/test-e2e-tests.sh` | 編譯並執行 end-to-end 測試（`test-e2e-tests.sh --headed` 會讓測試執行時的 Swing UI 顯示在主機螢幕上，方便除錯；預設不加參數是用 `xvfb-run` 虛擬 display，畫面不會顯示出來） |
| `scripts/run-app.sh <itemId> [username] [password]` | 編譯並執行 Auction Sniper 這個 Swing app，視窗顯示在主機的 X display 上 |
| `scripts/fake-auction.sh <itemId>` | 編譯並執行 `tools/FakeAuction.java`，用假拍賣工具手動扮演賣家，驅動 `run-app.sh` 開起來的 app（用法見 [fake-auction.md](fake-auction.md)） |

## Openfire（XMPP）

E2E 測試需要連到真的 XMPP server，跟 goos-code 一樣用 Docker 跑 Openfire。目前固定使用 `nasqueron/openfire:4.7.4`，以確保開發環境可以穩定重現。

`docker-compose.yml` 裡的 `toolbox` 使用 `network_mode: service:openfire`，讓 container 內的 `localhost` 直接連到 Openfire。測試與 production code 裡寫死 `XMPP_HOSTNAME = "localhost"`，不用修改 source code。

Openfire 第一次啟動要跑過 setup wizard、建立測試帳號才能使用。這件事沒有 API 能直接完成，因此 `setup-openfire.js` 使用 Playwright 自動完成 setup wizard 並建立 3 組測試帳號：

| 帳號                   | 密碼        |
| -------------------- | --------- |
| `sniper`             | `sniper`  |
| `auction-item-54321` | `auction` |
| `auction-item-65432` | `auction` |

### 第一次建立環境

不用手動建立。還沒有 Openfire 資料時，第一次跑任何測試或 app 腳本會自動依序：

1. 安裝 Playwright（在專案根目錄執行 `npm install`、`npx playwright install chromium`）。
2. 啟動 Openfire，並等待 admin console 有回應。
3. 執行 `scripts/setup-openfire.js`，建立測試帳號，並將 resource conflict policy 設成 `Never kick`（同一組帳號 + resource 重複登入時拒絕新的登入，回 `conflict(409)`，對應 GOOS ch12 的情境；Openfire 預設是 `Always kick`）。
4. 啟動 toolbox（image 不存在時會自動 build）。

wizard 失敗或被中斷（例如按 Ctrl-C）時，會清除尚未完成初始化的 Openfire 資料，下次執行腳本時會重新跑 wizard。

Openfire 的 host port 由 Docker 自動分配。需要從 host 開啟 admin console 時，可以用以下指令查詢實際分配的 port：

```bash
docker compose port openfire 9090
```

### 改了 toolbox.Dockerfile

```bash
scripts/env.sh start
```

### 移除環境

```bash
scripts/env.sh stop
```

移除 container（Openfire + toolbox）、toolbox image 與 Openfire 資料 volume。

### 環境設定壞掉、想砍掉重建

```bash
scripts/env.sh reset
```

等於 `scripts/env.sh stop` 再跑 `scripts/env.sh start`，會重新 build toolbox 並重新跑一次 wizard。

## 假拍賣工具

用 `tools/FakeAuction.java`（`scripts/fake-auction.sh`）這個假拍賣工具手動扮演賣家、驅動 `run-app.sh` 開起來的 app，完整操作步驟見 [fake-auction.md](fake-auction.md)。
