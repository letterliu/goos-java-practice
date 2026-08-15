# GOOS Java Practice

## Repo 結構

本專案使用一般 Git repository 管理。

## Java 一律用 Docker 執行

本專案的 Java（編譯、執行、測試）一律透過
`docker/docker-compose.yml` 定義的 `toolbox` container 執行，
不在 host 直接執行 `javac` / `java`。

執行 end-to-end 測試：

```bash
docker/scripts/test.sh
```
