#!/usr/bin/env bash
set -euo pipefail

# Run all unit/common tests across the BDUI modules in one go.
# Add connectedAndroidTest separately if you need device/emulator coverage.

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd "${script_dir}/.." && pwd)"

cd "${repo_root}"

./gradlew \
  :libraries:bdui:contract:check \
  :libraries:bdui:actions:check \
  :libraries:bdui:core:check \
  :libraries:bdui:navigation:check \
  :libraries:bdui:renderer:check \
  :libraries:bdui:runtime:check \
  :libraries:bdui:engine:check \
  :libraries:bdui:logger:check \
  :libraries:bdui:testing:check \
  :libraries:bdui:components:check \
  :libraries:bdui:network:check \
  :libraries:bdui:cache:check \
  :libraries:bdui:demo:check \
  :libraries:bdui-backend:contract:check \
  :libraries:bdui-backend:core:check \
  :libraries:bdui-backend:dsl:check \
  :libraries:bdui-backend:mapper:check \
  :libraries:bdui-backend:renderer:check \
  :libraries:bdui-backend:runtime:check

# Example for UI/device tests (uncomment when emulator available):
 ./gradlew :libraries:bdui:demo:connectedAndroidTest
