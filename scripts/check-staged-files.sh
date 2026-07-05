#!/usr/bin/env bash

set -eu

echo "Checking staged files..."

MAX_SIZE=$((10 * 1024 * 1024))
FAILED=0

while IFS= read -r -d '' file
do
    case "$file" in
        .env.example|*/.env.example)
            ;;
        .env|.env.*|*/.env|*/.env.*|*.pem|*.key|*.p12|*.jks|*.keystore)
            echo "Forbidden secret file: $file"
            FAILED=1
            ;;
    esac

    case "$file" in
        *.log|hs_err_pid*.log|replay_pid*.log|*.hprof|*.tmp|*.bak|*.swp|*.DS_Store|Thumbs.db)
            echo "Temporary or dump file: $file"
            FAILED=1
            ;;
    esac

    case "$file" in
        target/*|*/target/*|build/*|*/build/*|out/*|*/out/*)
            echo "Build artifact: $file"
            FAILED=1
            ;;
    esac

    size=$(git cat-file -s ":$file")

    if [ "$size" -gt "$MAX_SIZE" ]; then
        echo "File is larger than 10 MB: $file"
        FAILED=1
    fi
done < <(git diff --cached --name-only -z --diff-filter=ACMR)

if [ "$FAILED" -ne 0 ]; then
    echo
    echo "Commit aborted."
    exit 1
fi

echo "Staged files check passed."
