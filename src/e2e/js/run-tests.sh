#!/bin/bash

# Test runner script for cBioPortal E2E tests

# Set default server URL if not provided
export CBIOPORTAL_URL="${CBIOPORTAL_URL:-http://localhost:8080}"

echo "Running E2E tests against: $CBIOPORTAL_URL"
echo "==========================================="
echo ""

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo "Installing dependencies..."
    npm install
    echo ""
fi

# Run the tests
npm test