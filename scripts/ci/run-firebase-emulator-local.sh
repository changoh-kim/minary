#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "${ROOT_DIR}"

FIREBASE_EMULATOR_HOST="${FIREBASE_EMULATOR_HOST:-10.0.2.2}"
FIREBASE_EMULATOR_TEST_CLASSES="kr.co.data.firebase.FirebaseEmulatorIntegrationSmokeTest,kr.co.data.feature.diary.sync.FirestoreDiarySyncManagerEmulatorTest,kr.co.data.feature.profile.sync.FirestoreUserProfileSyncManagerEmulatorTest,kr.co.data.feature.setting.sync.FirestoreUserSettingsSyncManagerEmulatorTest"

./gradlew :data:connectedDebugAndroidTest \
  "-Pandroid.testInstrumentationRunnerArguments.class=${FIREBASE_EMULATOR_TEST_CLASSES}" \
  -Pandroid.testInstrumentationRunnerArguments.firebaseEmulatorEnabled=true \
  "-Pandroid.testInstrumentationRunnerArguments.firebaseEmulatorHost=${FIREBASE_EMULATOR_HOST}"
