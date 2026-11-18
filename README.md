# GOOS Java Practice

參考 [GOOS（Growing Object-Oriented Software）](https://www.growing-object-oriented-software.com/) 書中的 Auction Sniper 範例，重新用 TDD 刻一次，盡量比照作者原始的 [goos-code](https://github.com/sf105/goos-code) repo 的簡單管理方式：依賴用 vendor JAR、手動 `javac` / `java`，不用 Maven / Gradle / Ant 等額外建置工具。

## Repo 結構

本專案使用一般 Git repository 管理。

## TDD Commit Message 規範

照書中章節逐步進行 TDD：先寫 test code，再寫 production code。Git commit message 使用 Conventional Commits，並標示對應的書中出處。

格式：

```text
test(<scope>): red - <紅燈描述> [<書中出處>]
feat(<scope>): green - <綠燈描述> [<書中出處>]
refactor(<scope>): <重構描述> [<書中出處>]
```

* `<scope>`：測試層級（`unit` / `integration` / `e2e`）或模組名稱（`ui` / `api` / `redis` 等），選擇對這次改動辨識度較高的名稱。跨越多個模組時可省略 scope。
* `<紅燈描述>`/`<綠燈描述>`：精簡描述這次紅燈/綠燈的重點，不是完整測試方法名稱（完整測試方法名稱長，放進 subject 容易超過 Conventional Commits 建議的 50～72 字元上限）。
* `<book reference>`：可使用章節（`ch10`）、小節（`3.6`）或頁碼（`p42`），視情況組合，例如 `[3.6]`、`[p42]`、`[ch10 p85]`、`[3.6 p42]`。
* 書中出處代表這個 commit 的內容涵蓋到書中該章節或頁碼為止，不代表精確定位到單一段落。

`test`/`feat` 的 commit body 一定要加一行 `Test case: <測試案例名稱>`，補上被 subject 省略的完整測試方法名稱：

```text
Test case: sniperJoinsAuctionUntilAuctionCloses
```

範例：

```text
test(e2e): red - missing "Lost" status on close [11.2.1 p96]

Test case: sniperJoinsAuctionUntilAuctionCloses
```

```text
feat(e2e): green - shows "Lost" when auction closes [11.2.4 p102]

Test case: sniperJoinsAuctionUntilAuctionCloses
```

```text
refactor(ui): extract AuctionEventListener [p42]
```

整個 commit message（subject 與 body，包含 `<紅燈描述>`/`<綠燈描述>`/`<重構描述>`）一律只能用英文，不能出現中文字（避免混用中英文的怪 commit）。

如果使用者貼了書中內文當補充說明，body 除了 `Test case:` 那行以外，一定要加一行精簡摘要，不可省略、不可照抄書中原文。

## Vendor dependencies

第三方 Java libraries 直接以 vendor JAR 的方式放在 `lib/` 中，不使用 Maven / Gradle 管理版本。

* `lib/deploy/`：runtime 所需的 dependencies。
* `lib/develop/`：僅開發與測試所需的 dependencies。
* 編譯 app 時只使用 `lib/deploy/`。
* 編譯與執行測試時使用 `lib/deploy/` + `lib/develop/`。
* `*-src.jar` 與 `*-sources.jar` 僅供原始碼閱讀，不作為編譯 dependency。

新增 dependency 時，直接將 JAR 放入對應目錄。

### IDE 設定

**IntelliJ**

`.idea/libraries/lib.xml` 的 library 使用兩個 `jarDirectory`，分別指向：

```text
lib/deploy
lib/develop
```

新增或移除 JAR 後重新整理 IDE，即可自動反映。

**VS Code**

安裝 Java extension 後，`.vscode/settings.json` 的 `java.project.referencedLibraries` 設定包含：

```text
lib/deploy/*.jar
lib/develop/*.jar
```

並排除 `-src.jar` / `-sources.jar`。

`.vscode/settings.json` 會被 Git 追蹤，讓 team 成員開啟專案後即可使用相同的 Java library 設定。

## Java 一律用 Docker 執行

本專案的 Java 編譯、執行、測試一律透過 `docker-compose.yml` 定義的 `toolbox` container 執行，不在 host 直接執行 `javac` / `java`。

## Docker 環境

Java 的編譯、執行、測試都跑在 Docker 裡，Openfire（XMPP）測試環境也是透過 Docker 提供。

詳細的 Docker 環境說明見 [docs/docker.md](docs/docker.md)。

用 `tools/FakeAuction.java` 這個互動式假拍賣工具模擬賣家，見 [docs/fake-auction.md](docs/fake-auction.md)。

如果同時執行 `run-app.sh` 與 `fake-auction.sh` 時，兩個 interactive session 互相干擾，可以改用手動方式啟動 toolbox，再分別使用 `docker compose exec` 執行 Auction Sniper 與 FakeAuction。完整步驟見 [docs/manual-fake-auction.md](docs/manual-fake-auction.md)。

### 常用腳本

| 腳本 | 用途 |
| --- | --- |
| `scripts/test.sh [unit\|e2e] [--headed]` | 編譯 App，並依參數執行 Unit Tests、E2E Tests 或兩者 |
| `scripts/run-app.sh <itemId> [username] [password]` | 編譯並執行 Auction Sniper Swing app |
| `scripts/env.sh up` | 啟動環境；首次執行時自動初始化 Openfire |
| `scripts/env.sh start` | 重新 build toolbox image 並啟動環境 |
| `scripts/env.sh stop` | 移除 containers、toolbox image 與 Openfire data volume |
| `scripts/env.sh reset` | 清除環境後重新建立 Openfire |

#### 測試腳本

執行全部測試（先 unit，再 end-to-end）：

```bash
bash scripts/test.sh
```

只執行 unit 測試：

```bash
bash scripts/test.sh unit
```

只執行 end-to-end 測試：

```bash
bash scripts/test.sh e2e
```

執行 headed E2E 測試，讓 Swing UI 顯示在主機螢幕上：

```bash
bash scripts/test.sh e2e --headed
```

執行測試腳本時，會透過 `scripts/env.sh up` 自動確保所需環境已啟動。

Unit Tests 雖然不需要連線到 Openfire，但 toolbox 使用 `network_mode: service:openfire`，因此仍需要 Openfire container 作為網路環境。

如果 Openfire 尚未初始化，會先自動執行 setup wizard、建立測試帳號，再啟動 toolbox；如果已經初始化，則直接啟動 toolbox。

### macOS XQuartz 設定

`--headed` 模式需要 XQuartz 提供 X11 display。由於 XQuartz 的 X server 存取權限可能在 macOS 重新開機後需要重新設定，因此執行 headed 測試前，建議先確認目前狀態。

先確認目前 X server 的存取控制：

```bash
xhost
```

如果沒有看到 INET:localhost 或 INET6:localhost，可以執行：

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

再確認 XQuartz 是否啟用 XTEST extension：

```bash
xdpyinfo | grep -i XTEST
```

如果兩項檢查都正常，即可執行：

```bash
bash scripts/test.sh e2e --headed
```

`scripts/run-app.sh` 會在啟動 Swing app 前自動執行 `xhost +localhost`，因此直接執行：

```bash
bash scripts/run-app.sh <itemId> [username] [password]
```

時，不需要另外手動設定 X11 access control。

如果仍出現：

```text
Authorization required, but no authorization protocol specified
```

或：

```text
Can't connect to X11 window server using 'host.docker.internal:0'
```

可以再次執行：

```bash
xhost +localhost
```

如果 `xdpyinfo | grep -i XTEST` 沒有輸出，啟用 XQuartz 的 XTEST extension：

```bash
defaults write org.xquartz.X11 enable_test_extensions -bool true
```

設定後完全關閉並重新啟動 XQuartz：

```bash
osascript -e 'quit app "XQuartz"'
open -a XQuartz
```

重新啟動後再次確認：

```bash
xdpyinfo | grep -i XTEST
```

確認 XTEST 存在後，再執行：

```bash
bash scripts/test.sh --headed
```

這些設定屬於 macOS/XQuartz 的開發環境設定，不是 GOOS 書中 Auction Sniper production code 的一部分。

## Openfire（XMPP）

E2E 測試需要連到真正的 XMPP server。跟 `goos-code` 一樣使用 Docker 執行 Openfire。

`docker-compose.yml` 中的 `toolbox` 使用：

```yaml
network_mode: service:openfire
```

因此 toolbox container 裡的 `localhost` 會直接指向 Openfire container。

測試與 production code 使用固定的：

```text
XMPP_HOSTNAME = "localhost"
```

因此不需要修改 source code 來配合 Docker network。

### Openfire 版本

原始 commit 使用：

```text
ghcr.io/igniterealtime/openfire:latest
```

本專案為了確保在目前的 macOS 12.7.6 環境中能夠穩定且可重現地執行，固定使用：

```text
nasqueron/openfire:4.7.4
```

因此本專案的 Openfire setup selector 會依照 Openfire 4.7.4 的實際 setup wizard HTML 調整。

### Playwright 版本

原始設定使用較新的 Playwright 版本，但目前 macOS 12.7.6 無法使用較新的 Chromium binary。

因此本專案固定使用與目前 macOS 環境相容的 Playwright 版本，並將其版本記錄在：

```text
package.json
package-lock.json
```

首次建立 Openfire 環境時，`scripts/env.sh up` 會自動檢查並安裝 Playwright dependencies，以及 Chromium browser binary。

Playwright 的版本仍由 `package.json` 與 `package-lock.json` 管理。

### 測試帳號

Openfire 第一次啟動要先跑 setup wizard，並建立測試帳號才能執行 E2E 測試。

`scripts/setup-openfire.js` 使用 Playwright 自動完成 setup wizard，並建立以下測試帳號：

| 帳號                   | 密碼        |
| -------------------- | --------- |
| `sniper`             | `sniper`  |
| `auction-item-54321` | `auction` |
| `auction-item-65432` | `auction` |

### 第一次建立環境

不用手動建立。還沒有 Openfire 資料時，第一次執行任何會使用 toolbox 的測試或 app 腳本，會自動依序：

1. 安裝 Playwright（在專案根目錄執行 `npm install`、`npx playwright install chromium`）。
2. 啟動 Openfire，並等待 admin console 有回應。
3. 執行 `scripts/setup-openfire.js`，自動完成 setup wizard、建立測試帳號，並將 resource conflict policy 設成 `Never kick`。
4. 啟動 toolbox（image 不存在時會自動 build）。

wizard 失敗或被中斷（例如按 Ctrl-C）時，會清除尚未完成初始化的 Openfire 資料，下次執行腳本時會重新跑 wizard。

Openfire 的 host port 由 Docker 自動分配。需要從 host 開啟 admin console 時，可以用以下指令查詢實際分配的 port：

```bash
docker compose port openfire 9090
```

### 停止環境

```bash
scripts/env.sh stop
```

移除 Openfire 與 toolbox containers、toolbox image，以及 Openfire data volume。

### 重建環境

如果 Openfire 環境設定壞掉，或需要從乾淨狀態重新建立：

```bash
scripts/env.sh reset
```

等同於先執行 `scripts/env.sh stop`，再執行 `scripts/env.sh start`。

這會清空 Openfire data volume，重新執行 setup wizard 並建立測試帳號。

**提醒：** 這些是文件範例，不要實際執行 `stop` 或 `reset`，因為會刪除目前的 Openfire volume。

## Docker 與 macOS

本專案的 Docker workflow 以 macOS + Docker Desktop 為主要開發環境。

Docker container 內的 Java 執行環境與 host Java 環境分離，因此 host 不需要安裝或使用與專案相同版本的 JDK。

Docker container 內負責：

* Java compilation
* Java application execution
* JUnit tests
* End-to-end tests
* Openfire integration environment

host 主要負責：

* Git
* Docker Desktop
* VS Code / IntelliJ
* XQuartz（需要 headed Swing UI 時）

## 開發流程

本專案按照 GOOS 書中的 TDD 演進逐步實作。

基本流程：

```text
先寫 failing test
       ↓
test commit（red）
       ↓
寫最少 production code 讓測試通過
       ↓
feat commit（green）
       ↓
必要時進行 refactoring
       ↓
refactor commit
       ↓
進入下一個書中步驟
```

每個 commit 盡量對應書中的一個小步驟，避免一次引入大量未經測試的 production code。

環境相關的修改，例如 Docker image、Playwright 版本或 macOS/XQuartz 相容性調整，應與 GOOS 書中 production code 的演進區分開來，並在 commit message 或 documentation 中明確說明。

## 目前的測試方式

執行全部測試（先 unit，再 end-to-end），E2E 預設使用 headless 模式：

```bash
bash scripts/test.sh
```

只執行 headless E2E 測試：

```bash
bash scripts/test.sh e2e
```

執行 headed E2E 測試：

```bash
bash scripts/test.sh e2e --headed
```

目前專案的 headed 模式主要提供後續 Swing UI 測試除錯使用；如果當前 E2E test 本身沒有啟動或保持 Swing UI，測試即使使用 --headed 也不一定會看到視窗。

## 參考資料

* [Growing Object-Oriented Software, Guided by Tests](https://www.growing-object-oriented-software.com/)
* [goos-code](https://github.com/sf105/goos-code)
* [Openfire](https://www.igniterealtime.org/projects/openfire/)
* [Playwright](https://playwright.dev/)
