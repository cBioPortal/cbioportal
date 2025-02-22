#!/bin/bash
HOOK_FILE=".git/hooks/pre-commit"

# Ensure hooks directory exists
mkdir -p .git/hooks

# If the pre-commit hook doesn't exist, create it with a shebang line
if [[ ! -f "$HOOK_FILE" ]]; then
    echo "#!/bin/bash" > "$HOOK_FILE"
    echo "set -e" >> "$HOOK_FILE"
fi

# Check if Spotless is already in the pre-commit hook, and add it if missing
if ! grep -qxF "mvn spotless:apply" "$HOOK_FILE"; then
    echo "echo 'Running Spotless on staged files...'" >> "$HOOK_FILE"
    echo "mvn spotless:apply" >> "$HOOK_FILE"
fi

# Check if `git add -u` is already in the pre-commit hook, and add it if missing
if ! grep -qxF "git add -u" "$HOOK_FILE"; then
    echo "echo 'Adding formatted files back to commit...'" >> "$HOOK_FILE"
    echo "git add -u" >> "$HOOK_FILE"
fi

# Make sure the pre-commit hook is executable
chmod +x "$HOOK_FILE"

echo "✅ Spotless pre-commit hook installed successfully!"
