#!/usr/bin/env bash
set -e

YARN_BUILD_DIR=tools/yarn
YARN=${YARN_BUILD_DIR}/node_modules/yarn/bin/yarn
if ! [ -f ${YARN} ]; then
    echo Installing yarn...
    npm install --no-package-lock --no-save --prefix ${YARN_BUILD_DIR} yarn
fi

${YARN} "$@"
