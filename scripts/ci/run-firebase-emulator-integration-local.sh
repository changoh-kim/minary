#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "${ROOT_DIR}"

FIREBASE_EMULATOR_HOST="${FIREBASE_EMULATOR_HOST:-10.0.2.2}"
FIREBASE_EMULATOR_LOG="${ROOT_DIR}/firebase-emulator-local.log"
FIREBASE_EMULATOR_PID=""

cleanup() {
  if [[ -n "${FIREBASE_EMULATOR_PID}" ]] && kill -0 "${FIREBASE_EMULATOR_PID}" 2>/dev/null; then
    kill "${FIREBASE_EMULATOR_PID}" 2>/dev/null || true
    wait "${FIREBASE_EMULATOR_PID}" 2>/dev/null || true
  fi
}
trap cleanup EXIT

npm --prefix firebase-server/functions ci
npm --prefix firebase-server/functions run lint
npm --prefix firebase-server/functions run build

(
  cd firebase-server
  firebase emulators:start \
    --only auth,firestore,functions,storage,pubsub \
    --project minary-2c818
) > "${FIREBASE_EMULATOR_LOG}" 2>&1 &
FIREBASE_EMULATOR_PID="$!"

for port in 9099 8080 5001 9199 8085; do
  echo "Waiting for Firebase emulator port ${port}"
  timeout 180 bash -c "until echo > /dev/tcp/127.0.0.1/${port}; do sleep 2; done" 2>/dev/null || {
    tail -200 "${FIREBASE_EMULATOR_LOG}" || true
    exit 1
  }
done

FIREBASE_EMULATOR_HOST="${FIREBASE_EMULATOR_HOST}" \
  "${ROOT_DIR}/scripts/ci/run-firebase-emulator-local.sh"
