#!/usr/bin/env bash
# Runs unit tests then end-to-end tests inside the toolbox container.
# 加上 --headed 參數會轉給 test-e2e-tests.sh，讓測試執行時的 Swing UI 顯示
# 在主機螢幕上（預設是 xvfb 虛擬 display，看不到畫面），方便除錯。
set -euo pipefail

cd "$(dirname "$0")"

./test-unit-tests.sh
./test-e2e-tests.sh "$@"
