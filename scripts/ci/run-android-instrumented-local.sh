#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "${ROOT_DIR}"

./gradlew \
  :core:database:connectedDebugAndroidTest \
  :core:datastore:connectedDebugAndroidTest \
  :core:firebase:connectedDebugAndroidTest \
  :core:storage:connectedDebugAndroidTest \
  :core:ui:common:connectedDebugAndroidTest \
  :core:ui:design:connectedDebugAndroidTest

./gradlew :data:connectedDebugAndroidTest
./gradlew :presentation:connectedDebugAndroidTest
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.annotation=androidx.test.filters.LargeTest
