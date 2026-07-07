#!/usr/bin/env bash

set -euo pipefail

FOUND=0

while IFS= read -r -d '' file
do
    if git show ":$file" | grep -nE '^(<<<<<<<|=======|>>>>>>>)'; then
        echo ""
        echo "Git conflict marker found in: $file"
        FOUND=1
    fi
done < <(git diff --cached --name-only -z --diff-filter=ACMR)

if [ "$FOUND" -eq 1 ]; then
    echo ""
    echo "Remove Git conflict markers before committing."
    exit 1
fi
