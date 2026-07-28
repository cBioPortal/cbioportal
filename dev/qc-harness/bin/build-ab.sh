#!/usr/bin/env bash
# Build two cbioportal "exec" jars from two git refs for an A/B comparison.
# Uses throwaway worktrees so your main checkout is untouched.
#
# Usage: build-ab.sh <old-ref> <new-ref>
#   e.g. build-ab.sh master my-pr-branch
#
# Output: <WORK_DIR>/cbioportal-OLD.jar and <WORK_DIR>/cbioportal-NEW.jar
#
# Gotchas baked in:
#  - the runnable artifact is target/cbioportal-EXEC.jar, not cbioportal.jar
#  - -Dmaven.gitcommitid.skip=true is required when building inside a git worktree
#  - application.properties is gitignored, so we seed it from the source checkout
#    (or .EXAMPLE) — runtime datasource is overridden by run-backend.sh anyway
set -euo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "${HERE}/config.env"
REPO="$(git -C "$HERE" rev-parse --show-toplevel)"
mkdir -p "$WORK_DIR"

seed_props() {  # $1 = worktree dir
  local p="$1/src/main/resources/application.properties"
  [ -f "$p" ] && return 0
  if [ -f "$REPO/src/main/resources/application.properties" ]; then
    cp "$REPO/src/main/resources/application.properties" "$p"
  else
    cp "$1/src/main/resources/application.properties.EXAMPLE" "$p"
  fi
}

build_ref() {  # $1 = git ref, $2 = OLD|NEW
  local ref="$1" label="$2" wt="$WORK_DIR/wt-$2"
  echo ">>> building $label from '$ref'"
  git -C "$REPO" worktree remove --force "$wt" 2>/dev/null || true
  git -C "$REPO" worktree add -f "$wt" "$ref" >/dev/null
  seed_props "$wt"
  ( cd "$wt" && mvn -q -o package -DskipTests \
      -Dspotless.check.skip=true -Dmaven.gitcommitid.skip=true )
  cp "$wt/target/cbioportal-exec.jar" "$WORK_DIR/cbioportal-$label.jar"
  git -C "$REPO" worktree remove --force "$wt" 2>/dev/null || true
  echo "    -> $WORK_DIR/cbioportal-$label.jar"
}

build_ref "$1" OLD
build_ref "$2" NEW
echo "md5:"; md5sum "$WORK_DIR"/cbioportal-OLD.jar "$WORK_DIR"/cbioportal-NEW.jar
