# GOOS Java Practice

參考 [GOOS（Growing Object-Oriented Software, Guided by Tests）](https://www.growing-object-oriented-software.com/) 書中的 Auction Sniper 範例，重新用 TDD 刻一次，盡量比照作者原始的 [goos-code](https://github.com/sf105/goos-code) repo 的簡單管理方式：依賴用 vendor JAR、手動 `javac`/`java`，不用額外的建置工具（不用 Maven/Gradle/Ant）。

## Repo 結構

本專案使用一般 Git repository 管理。

## Vendor dependencies

第三方 Java libraries 直接以 vendor JAR 的方式放在 `lib/` 中，不使用 Maven / Gradle 管理版本。

* `lib/deploy/`：runtime 所需的 dependencies。
* `lib/develop/`：僅開發與測試所需的 dependencies。
* 編譯 app 時只使用 `lib/deploy/`。
* 編譯與執行測試時使用 `lib/deploy/` + `lib/develop/`。
* `*-src.jar` 與 `*-sources.jar` 僅供原始碼閱讀，不作為編譯 dependency。

新增 dependency 時，直接將 JAR 放入對應目錄。

### IDE library 設定

* **VS Code**：安裝 Java extension 後，`.vscode/settings.json` 的 `java.project.referencedLibraries` 包含 `lib/deploy/*.jar`、`lib/develop/*.jar`，並排除 `-src.jar` / `-sources.jar`。此設定檔會被 Git 追蹤，讓 team 成員開啟專案後即可使用。

## Java 一律用 Docker 執行

本專案的 Java 編譯、執行與測試一律透過 `docker/docker-compose.yml` 定義的 `toolbox` container 執行，不在 host 直接執行 `javac` / `java`。

### 執行 Swing app

執行 Auction Sniper Swing app：

```bash
docker/scripts/run-app.sh
```

預設會將 Swing 視窗顯示在主機的 X display 上。

### End-to-end 測試

E2E 測試需要連接實際的 XMPP server，因此 Openfire 也透過 Docker 執行。

`toolbox` 使用：

```yaml
network_mode: service:openfire
```

與 Openfire 共用 network namespace，因此測試中的 `XMPP_HOSTNAME` 使用 `localhost` 即可。

第一次建立環境，或 Openfire 資料被清空後，需要先執行：

```bash
docker/scripts/start-env.sh
```

環境準備完成後，再執行：

```bash
docker/scripts/test.sh
```

### Headed E2E 測試

E2E 測試預設使用 `xvfb-run` 提供虛擬 display，因此不會顯示 Swing UI。

若需要除錯並讓測試執行時的 Swing UI 顯示在主機螢幕上：

```bash
docker/scripts/test.sh --headed
```

### 重置 Openfire 環境

若需要清除 Openfire 的設定與測試帳號，重新建立完整環境：

```bash
docker/scripts/reset-env.sh
```

這會停止並移除 containers、刪除 Openfire data volume，然後重新執行環境初始化。

### 停止環境

只需要停止目前的 Openfire 與 toolbox：

```bash
docker/scripts/stop-env.sh
```

`stop-env.sh` 同時會清除 Openfire data volume，因此下次啟動時會重新執行 Openfire setup wizard。

## Openfire（XMPP）

E2E 測試需要連到真正的 XMPP server。

Openfire 透過 Docker Compose 啟動，`toolbox` 與 Openfire 共用 network namespace，因此專案中的 XMPP host 使用 `localhost`。

第一次建立環境時，`start-env.sh` 會：

1. 啟動 Openfire。
2. 等待 Openfire Web Console 可連線。
3. 建立並啟動 `toolbox` container。
4. 安裝 Playwright 與 Chromium。
5. 使用 `setup-openfire.js` 自動完成 Openfire setup wizard。
6. 建立 E2E 測試所需的 XMPP 帳號。

因此正常的 E2E 測試流程為：

```text
docker/scripts/start-env.sh
        ↓
Openfire 初始化
        ↓
建立測試帳號
        ↓
docker/scripts/test.sh
        ↓
編譯並執行 E2E tests
```

完整的 Docker 環境細節請參考 [`docker/README.md`](docker/README.md)。
