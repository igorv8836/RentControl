#!/usr/bin/env bash
set -euo pipefail

# Run all unit/common tests across the BDUI modules in one go.
# Add connectedAndroidTest separately if you need device/emulator coverage.

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd "${script_dir}/.." && pwd)"

cd "${repo_root}"

./gradlew \
  :libraries:bdui:contract:allTests \
  :libraries:bdui:actions:allTests \
  :libraries:bdui:core:allTests \
  :libraries:bdui:navigation:allTests \
  :libraries:bdui:renderer:allTests \
  :libraries:bdui:runtime:allTests \
  :libraries:bdui:engine:allTests \
  :libraries:bdui:logger:allTests \
  :libraries:bdui:testing:allTests

# Example for UI/device tests:
# ./gradlew :libraries:bdui:demo:connectedAndroidTest
