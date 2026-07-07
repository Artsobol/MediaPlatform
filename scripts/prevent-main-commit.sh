#!/usr/bin/env sh

set -e

BRANCH=$(git branch --show-current)

case "$BRANCH" in
    main|master)
        echo "Direct commits to '$BRANCH' are forbidden."
        echo "Create a feature branch first."
        exit 1
        ;;
esac