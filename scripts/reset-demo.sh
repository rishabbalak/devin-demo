#!/usr/bin/env bash
#
# Restores the repository to its pristine demo starting state.
#
# This is destructive on purpose: it exists so the same demo can be run repeatedly after an
# agent (or a person) has changed the code. It discards tracked modifications and deletes
# untracked files, so it always shows exactly what will be lost and asks before doing it.
#
#   ./scripts/reset-demo.sh            interactive, asks for confirmation
#   ./scripts/reset-demo.sh --force    no prompt, for unattended use
#   ./scripts/reset-demo.sh --no-build skip the rebuild afterwards
#
set -euo pipefail

TAG="demo-start"
FORCE=0
BUILD=1

for arg in "$@"; do
    case "$arg" in
        --force)    FORCE=1 ;;
        --no-build) BUILD=0 ;;
        -h|--help)
            sed -n '2,12p' "$0" | sed 's/^# \{0,1\}//'
            exit 0
            ;;
        *)
            echo "reset-demo: unknown option '$arg'" >&2
            exit 2
            ;;
    esac
done

cd "$(dirname "$0")/.."

if ! git rev-parse --git-dir >/dev/null 2>&1; then
    echo "reset-demo: not inside a git repository." >&2
    exit 1
fi

if ! git rev-parse -q --verify "refs/tags/${TAG}" >/dev/null; then
    echo "reset-demo: tag '${TAG}' does not exist." >&2
    echo "            Create it on the pristine commit first:  git tag ${TAG} <commit>" >&2
    exit 1
fi

TRACKED="$(git diff --stat "${TAG}" -- . || true)"
# -fd leaves ignored paths such as target/ alone; the rebuild handles those.
UNTRACKED="$(git clean -nd | sed 's/^Would remove //')"

if [ -z "${TRACKED}" ] && [ -z "${UNTRACKED}" ]; then
    echo "Already at ${TAG} with a clean tree.  Nothing to reset."
    exit 0
fi

echo "Resetting to '${TAG}'.  The following will be permanently discarded:"
echo
if [ -n "${TRACKED}" ]; then
    echo "  Tracked changes:"
    echo "${TRACKED}" | sed 's/^/    /'
    echo
fi
if [ -n "${UNTRACKED}" ]; then
    echo "  Untracked files to delete:"
    echo "${UNTRACKED}" | sed 's/^/    /'
    echo
fi

if [ "${FORCE}" -ne 1 ]; then
    # Prefer the controlling terminal so the prompt still works when stdin is a pipe, but
    # fall back to stdin, and refuse outright rather than resetting unattended by accident.
    reply=""
    if [ -r /dev/tty ] && { : >/dev/tty; } 2>/dev/null; then
        printf "Proceed? [y/N] "
        read -r reply </dev/tty
    elif [ -t 0 ]; then
        printf "Proceed? [y/N] "
        read -r reply
    else
        echo "reset-demo: no terminal available to confirm on." >&2
        echo "            Re-run with --force to reset without prompting." >&2
        exit 1
    fi

    case "${reply}" in
        [yY]|[yY][eE][sS]) ;;
        *) echo "Aborted.  Nothing was changed."; exit 1 ;;
    esac
fi

git reset --hard "${TAG}"
git clean -fd

if [ "${BUILD}" -eq 1 ]; then
    echo
    echo "Rebuilding..."
    ./mvnw -q clean package
    echo "Reset complete.  Start the application with:"
    echo "  java -jar costco-api/target/costco-api-1.0.0-SNAPSHOT.jar"
else
    echo "Reset complete (build skipped)."
fi
