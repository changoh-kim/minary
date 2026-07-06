#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "${ROOT_DIR}"

./gradlew -p build-logic check

./gradlew :core:common:test :core:di:test :domain:test

./gradlew \
  :core:database:testDebugUnitTest \
  :core:datastore:testDebugUnitTest \
  :core:firebase:testDebugUnitTest \
  :core:storage:testDebugUnitTest \
  :core:ui:common:testDebugUnitTest \
  :core:ui:design:testDebugUnitTest

./gradlew \
  :data:testDebugUnitTest \
  :presentation:testDebugUnitTest \
  :app:testDebugUnitTest

./gradlew \
  :core:database:compileDebugAndroidTestKotlin \
  :core:datastore:compileDebugAndroidTestKotlin \
  :core:firebase:compileDebugAndroidTestKotlin \
  :core:storage:compileDebugAndroidTestKotlin \
  :core:ui:common:compileDebugAndroidTestKotlin \
  :core:ui:design:compileDebugAndroidTestKotlin

./gradlew \
  :data:compileDebugAndroidTestKotlin \
  :presentation:compileDebugAndroidTestKotlin \
  :app:compileDebugAndroidTestKotlin

./gradlew :app:assembleDebug

npm --prefix firebase-server/functions ci
npm --prefix firebase-server/functions run lint
npm --prefix firebase-server/functions run build
