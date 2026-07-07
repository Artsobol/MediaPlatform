#!/usr/bin/env bash

set -euo pipefail

FOUND=0

while IFS= read -r -d '' file
do
    if git show ":$file" | grep -nE "System\.(out|err)\.(print|println|printf)\("; then
        echo ""
        echo "Forbidden debug output found in $file"
        FOUND=1
    fi
done < <(git diff --cached --name-only -z --diff-filter=ACMR -- '*.java')

if [ "$FOUND" -eq 1 ]; then
    echo ""
    echo "Remove System.out.* before committing."
    exit 1
fi
