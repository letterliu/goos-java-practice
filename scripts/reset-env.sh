#!/usr/bin/env bash
# 砍掉 container + 清空 Openfire volume（下次會重新跑一次 setup wizard），
# 然後透過 start-env.sh 重建整個環境。
set -euo pipefail

cd "$(dirname "$0")/.."

./scripts/stop-env.sh
./scripts/start-env.sh
