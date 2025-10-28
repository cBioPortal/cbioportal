#!/bin/bash

# Test runner script for cBioPortal E2E tests

# Set default server URL if not provided
export CBIOPORTAL_URL="${CBIOPORTAL_URL:-http://localhost:8082}"

# Check if the server is responding
echo "Checking if cBioPortal is running at $CBIOPORTAL_URL..."

# Try to fetch the /api/health endpoint (or root)
if curl -f -s -o /dev/null --max-time 5 "$CBIOPORTAL_URL/api/health" 2>/dev/null || \
   curl -f -s -o /dev/null --max-time 5 "$CBIOPORTAL_URL" 2>/dev/null; then
    echo "✓ cBioPortal is responding at $CBIOPORTAL_URL"
    echo ""
else
    echo ""
    echo "✗ ERROR: cBioPortal is not responding at $CBIOPORTAL_URL"
    echo ""
    echo "Please ensure:"
    echo "  1. cBioPortal is running"
    echo "  2. The server is accessible at $CBIOPORTAL_URL"
    echo "  3. The server is connected to the cgds_public_2025_06_24 database"
    echo ""
    echo "To run against a different URL, set the CBIOPORTAL_URL environment variable:"
    echo "  CBIOPORTAL_URL=http://localhost:8080 npm run test:script"
    echo ""
    exit 1
fi

echo "Running E2E tests against: $CBIOPORTAL_URL"
echo "==========================================="
echo ""

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo "Installing dependencies..."
    npm install
    echo ""
fi

# Run the tests with all arguments passed through
# Disable Node.js warnings about module type
exec node --no-warnings=MODULE_TYPELESS_PACKAGE_JSON node_modules/.bin/mocha --require ts-node/register "$@" 