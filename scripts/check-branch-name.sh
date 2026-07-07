#!/usr/bin/env sh

set -e

BRANCH_NAME=$(git rev-parse --abbrev-ref HEAD)

case "$BRANCH_NAME" in
    main|master|develop|dev)
        exit 0
        ;;
esac

if ! echo "$BRANCH_NAME" | grep -Eq '^(feature|bugfix|hotfix|docs|chore)/[a-z0-9._-]+$'; then
    echo "Invalid branch name: $BRANCH_NAME"
    echo ""
    echo "Allowed examples:"
    echo "  feature/add-user-auth"
    echo "  bugfix/fix-refresh-token"
    echo "  hotfix/fix-prod-login"
    echo "  docs/update-readme"
    echo "  chore/configure-husky"
    exit 1
fi