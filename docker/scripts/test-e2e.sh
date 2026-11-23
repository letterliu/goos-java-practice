#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

cd "$PROJECT_ROOT"

docker compose -f docker/docker-compose.yml exec -T toolbox bash -c '
  rm -rf out &&
  mkdir -p out &&
  javac -cp "lib/develop/*:lib/deploy/*" \
    -d out \
    $(find src test -type f -name "*.java") &&
  xvfb-run -a \
    java -cp "out:lib/develop/*:lib/deploy/*" \
    org.junit.runner.JUnitCore \
    test.endtoend.auctionsniper.AuctionSniperEndToEndTest
'
