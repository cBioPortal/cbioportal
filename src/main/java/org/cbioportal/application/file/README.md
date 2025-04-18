# Study Data Export

This package contains the code for exporting study data from the database to a file format.  The export process involves several steps, including:
1. Retrieving the study data from the database.
2. Transforming the data into a suitable format for export.
3. Writing the transformed data to a file.

The implementation is done with minimum dependencies on the rest of the code to ensure that the code is lightweight, performant and easy to move to a separate web application if needed.
To make export process take less RAM, the code uses a streaming approach to read and write data. On the database side, the code uses a cursor to read data in chunks, and on the web controller side, the code uses a streaming response to write data in chunks.
This allows the code to handle large datasets without running out of memory. 

## Usage

Set `dynamic_study_export_mode` to `true` in the application properties file to enable the dynamic study export mode. 
This mode allows the user to export study with `/export/study/{studyId}.zip` link.