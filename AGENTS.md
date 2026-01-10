# Quickstart Guide

## Project Layout
- libraries/bdui: client-side mobile backend driven ui library
- libraries/bdui-backend:  backend-side BDUI library
- composeApp: base multiplatform app
- server/bdui-server: base BDUI server for ui
- server/rent-control-server: base server with APIs

## Building and Testing
- After the changes always run tests via scripts/run-bdui-tests.sh or scripts/run-rent-control-server-tests.sh

## Main rules
- The file sizes should not be huge, divide into different files if possible.
- Add tests whenever possible if you add new functionality.
- Always write the commit name in English at the end of the final changes.
