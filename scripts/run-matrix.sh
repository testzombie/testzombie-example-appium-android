#!/usr/bin/env bash
set -euo pipefail
APK="${1:?Usage: ./scripts/run-matrix.sh /path/to/app-debug.apk}"
mvn -Dapp.apk="$APK" -Dtest=MutationLevelMatrixTest test
