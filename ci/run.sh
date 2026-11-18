#!/usr/bin/env bash
set -euo pipefail
if [ -n "${RELEASE_TAG:-}" ]; then
  mkdir -p /tmp/rel
  gh release download "$RELEASE_TAG" --repo "$GITHUB_REPOSITORY" --dir /tmp/rel --pattern '*.apk' --pattern '*.sha256'
  (cd /tmp/rel && sha256sum -c ./*.sha256)
  adb install -r /tmp/rel/tumbuh-v*.apk
else
  adb install -r app/build/outputs/apk/debug/tumbuh-v*-debug.apk
fi
python3 ci/smoke.py
