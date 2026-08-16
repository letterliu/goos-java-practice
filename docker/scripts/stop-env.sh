#!/usr/bin/env bash
# 停止並移除目前跑起來的 container（openfire + toolbox），並清空 Openfire
# 的資料 volume。之後要重新啟動用 docker/scripts/start-env.sh。
set -euo pipefail

cd "$(dirname "$0")/.."

echo "== stopping containers =="
sudo docker compose down

echo "== wiping openfire data volume =="
sudo docker volume rm docker_openfire-data
