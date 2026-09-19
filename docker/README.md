# Docker 環境

## Java

Java 的編譯、執行、測試一律透過 `docker/docker-compose.yml` 定義的 `toolbox` container 執行，不在 host 直接跑 `javac` / `java`。

| 腳本 | 用途 |
| --- | --- |
| `scripts/test.sh` | 依序執行 `test-unit-tests.sh` 與 `test-e2e-tests.sh`（`test.sh --headed` 會轉給 `test-e2e-tests.sh`） |
| `scripts/test-unit-tests.sh` | 編譯並執行 unit 測試（不用 XMPP server，也沒有 Swing UI，不需要 `xvfb-run`） |
| `scripts/test-e2e-tests.sh` | 編譯並執行 end-to-end 測試（`test-e2e-tests.sh --headed` 會讓測試執行時的 Swing UI 顯示在主機螢幕上，方便除錯；預設不加參數是用 `xvfb-run` 虛擬 display，畫面不會顯示出來） |
| `scripts/run-app.sh <itemId> [username] [password]` | 編譯並執行 Auction Sniper 這個 Swing app，視窗顯示在主機的 X display 上 |
| `scripts/fake-auction.sh <itemId>` | 編譯並執行 `tools/FakeAuction.java`，用假拍賣工具手動扮演賣家，驅動 `run-app.sh` 開起來的 app（用法見 [docs/fake-auction.md](../docs/fake-auction.md)） |

## Openfire（XMPP）

E2E 測試需要連到真的 XMPP server，跟 goos-code 一樣用 Docker 跑 Openfire。目前固定使用 `nasqueron/openfire:4.7.4`，以確保開發環境可以穩定重現。

`docker/docker-compose.yml` 裡的 `toolbox` 使用 `network_mode: service:openfire`，讓 container 內的 `localhost` 直接連到 Openfire。測試與 production code 裡寫死 `XMPP_HOSTNAME = "localhost"`，不用修改 source code。

Openfire 第一次啟動要跑過 setup wizard、建立測試帳號才能使用。這件事沒有 API 能直接完成，因此 `setup-openfire.js` 使用 Playwright 自動完成 setup wizard 並建立 3 組測試帳號：

| 帳號                   | 密碼        |
| -------------------- | --------- |
| `sniper`             | `sniper`  |
| `auction-item-54321` | `auction` |
| `auction-item-65432` | `auction` |

### 第一次建立環境（或 Openfire 資料被清空後）

```bash
docker/scripts/start-env.sh
```

會依序：

1. 啟動 Openfire。
2. 等待 9090 有回應。
3. build / 啟動 toolbox。
4. 安裝 `docker/package.json` 中指定的 Playwright dependency。
5. 執行 `setup-openfire.js`。
6. 建立測試帳號。

完成後即可直接執行：

```bash
docker/scripts/test.sh
```

### 停止環境

```bash
docker/scripts/stop-env.sh
```

停止並移除 container（Openfire + toolbox），並清空 Openfire 的資料 volume。

### 環境設定壞掉、想砍掉重建

```bash
docker/scripts/reset-env.sh
```

等於 `stop-env.sh` 接 `start-env.sh`，會清空 Openfire 資料並重新執行 setup wizard。

## 假拍賣工具

用 `docker/tools/FakeAuction.java`（`scripts/fake-auction.sh`）這個假拍賣工具手動扮演賣家、驅動 `run-app.sh` 開起來的 app，完整操作步驟見 [docs/fake-auction.md](../docs/fake-auction.md)。
