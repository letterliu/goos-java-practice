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

本專案的 Java 編譯、執行、測試一律透過 `docker/docker-compose.yml` 定義的 `toolbox` container 執行，不在 host 直接執行 `javac` / `java`。

## Docker 環境

Java 的編譯、執行、測試都跑在 Docker 裡，Openfire（XMPP）測試環境也是透過 Docker 提供。

詳細的 Docker 環境說明見 [docker/README.md](docker/README.md)。

### 常用腳本

| 腳本                                | 用途                                    |
| --------------------------------- | ------------------------------------- |
| `docker/scripts/test.sh`          | 編譯並執行 end-to-end 測試                   |
| `docker/scripts/run-e2e-tests.sh` | 在 toolbox container 內編譯並執行 E2E tests  |
| `docker/scripts/run-app.sh`       | 編譯並執行 Auction Sniper Swing app        |
| `docker/scripts/start-env.sh`     | 建立並初始化 Openfire 測試環境                  |
| `docker/scripts/stop-env.sh`      | 停止 container 並清除 Openfire data volume |
| `docker/scripts/reset-env.sh`     | 清除環境後重新建立 Openfire                    |

執行 end-to-end 測試：

```bash
bash docker/scripts/test.sh
```

## Swing UI 與 X11

預設執行 E2E 測試時使用 `xvfb-run` 提供虛擬 X display，因此 Swing UI 不會顯示在主機螢幕上。

需要顯示 Swing UI 進行除錯時，可以使用：

```bash
bash docker/scripts/test.sh --headed
```

`--headed` 模式會將主機的 X display 傳入 toolbox container，讓 Swing UI 可以透過主機的 X server 顯示。

在 macOS 上使用 XQuartz，Docker container 透過：

```text
host.docker.internal:0
```

連線到主機的 X display。

另外，也可以直接執行 Auction Sniper：

```bash
bash docker/scripts/run-app.sh
```

## Openfire（XMPP）

E2E 測試需要連到真正的 XMPP server。跟 `goos-code` 一樣使用 Docker 執行 Openfire。

`docker/docker-compose.yml` 中的 `toolbox` 使用：

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
docker/package.json
docker/package-lock.json
```

首次建立環境時，在 `docker/` 目錄執行：

```bash
npm install
npx playwright install chromium
```

### 測試帳號

Openfire 第一次啟動要先跑 setup wizard，並建立測試帳號才能執行 E2E 測試。

`docker/setup-openfire.js` 使用 Playwright 自動完成 setup wizard，並建立以下測試帳號：

| 帳號                   | 密碼        |
| -------------------- | --------- |
| `sniper`             | `sniper`  |
| `auction-item-54321` | `auction` |
| `auction-item-65432` | `auction` |

### 第一次建立環境

第一次建立環境，或 Openfire data volume 被清空後：

```bash
bash docker/scripts/start-env.sh
```

它會依序：

1. 啟動 Openfire。
2. 等待 `localhost:9090` 可以連線。
3. build 並啟動 toolbox container。
4. 安裝 Docker 目錄中的 Playwright dependencies。
5. 安裝 Chromium。
6. 執行 `setup-openfire.js`。
7. 自動完成 Openfire setup wizard。
8. 建立測試帳號。

完成後即可執行：

```bash
bash docker/scripts/test.sh
```

### 停止環境

```bash
bash docker/scripts/stop-env.sh
```

停止並移除 Openfire 與 toolbox container，同時清空 Openfire data volume。

### 重建環境

如果 Openfire 環境設定壞掉，或需要從乾淨狀態重新建立：

```bash
bash docker/scripts/reset-env.sh
```

等同於：

```text
stop-env.sh
↓
start-env.sh
```

會清空 Openfire data volume，重新執行 setup wizard 並建立測試帳號。

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

執行預設的 headless E2E 測試：

```bash
bash docker/scripts/test.sh
```

執行 headed E2E 測試：

```bash
bash docker/scripts/test.sh --headed
```

目前專案的 headed 模式主要提供後續 Swing UI 測試除錯使用；如果當前 E2E test 本身沒有啟動或保持 Swing UI，測試即使使用 `--headed` 也不一定會看到視窗。

## 參考資料

* [Growing Object-Oriented Software, Guided by Tests](https://www.growing-object-oriented-software.com/)
* [goos-code](https://github.com/sf105/goos-code)
* [Openfire](https://www.igniterealtime.org/projects/openfire/)
* [Playwright](https://playwright.dev/)
