#!/bin/bash

# Function to display usage instructions
usage() {
    echo "Usage: $0 <source_directory> <destination_directory>"
    exit 1
}

# Check if the correct number of arguments is provided
if [ "$#" -ne 2 ]; then
    usage
fi

# Remove trailing slashes from paths if they exist
SOURCE_DIR="${1%/}"
DEST_DIR="${2%/}"

# Check if the source directory exists
if [ ! -d "$SOURCE_DIR" ]; then
    echo "Error: Source directory does not exist."
    exit 1
fi

# Create the destination directory if it does not exist
mkdir -p "$DEST_DIR"

# Copy files and sort text files
find "$SOURCE_DIR" -type f | while read -r FILE; do
    REL_PATH="${FILE#$SOURCE_DIR/}"  # Get relative path
    DEST_FILE="$DEST_DIR/$REL_PATH"  # Destination file path
    DEST_DIR_PATH="$(dirname "$DEST_FILE")"  # Destination directory path

    # Create the destination directory if it does not exist
    mkdir -p "$DEST_DIR_PATH"

    # Check if the file is a text file and sort its contents
    if file "$FILE" | grep -q "text"; then
        sort "$FILE" > "$DEST_FILE"
    else
        cp "$FILE" "$DEST_FILE"
    fi

done

echo "Copy and sort operation completed successfully."