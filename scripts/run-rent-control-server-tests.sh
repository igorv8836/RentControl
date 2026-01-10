#!/usr/bin/env bash
set -euo pipefail

# Run all tests for the RentControl API service (server/rent-control-server).

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
repo_root="$(cd "${script_dir}/.." && pwd)"

cd "${repo_root}"

./gradlew :server:rent-control-server:test

