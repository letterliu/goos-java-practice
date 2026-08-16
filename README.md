# GOOS Java Practice

參考 [GOOS（Growing Object-Oriented Software, Guided by Tests）](https://www.growing-object-oriented-software.com/) 書中的 Auction Sniper 範例，重新用 TDD 刻一次，盡量比照作者原始的 [goos-code](https://github.com/sf105/goos-code) repo 的簡單管理方式：依賴用 vendor jar（見下方）、手動 `javac`/`java`、不用額外的建置工具（不用 Maven/Gradle/Ant）。

## Repo 結構

本專案使用一般 Git repository 管理。

## Vendor dependencies

第三方 Java libraries 直接以 vendor JAR 的方式放在 `lib/` 中，不使用 Maven / Gradle 管理版本。

- `lib/deploy/`：runtime 所需的 dependencies。
- `lib/develop/`：僅開發與測試所需的 dependencies。
- 編譯 app 時只使用 `lib/deploy/`。
- 編譯與執行測試時使用 `lib/deploy/` + `lib/develop/`。
- `*-src.jar` 與 `*-sources.jar` 僅供原始碼閱讀，不作為編譯 dependency。

新增 dependency 時，直接將 JAR 放入對應目錄。

## Java 一律用 Docker 執行

本專案的 Java（編譯、執行、測試）一律透過
`docker/docker-compose.yml` 定義的 `toolbox` container 執行，
不在 host 直接執行 `javac` / `java`。

執行 end-to-end 測試：

```bash
docker/scripts/test.sh
```
