# End-to-End Tests

This section contains some tips that have been accumulated over time of debugging and troubleshooting pernicious issues with end-to-end (e2e) tests.

## "boundingRects.reduce is not a function"
This error occurs when an e2e test tries to take a screenshot of an element that doesn't exist.

## "There are some read requests waitng on finished stream"
This error occurs in CircleCI when the reference screenshot file is somehow corrupted. It can be fixed by deleting and updating the reference screenshot.