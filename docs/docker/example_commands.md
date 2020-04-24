### Importing data ###

Use this command to validate a dataset. Specify the study directory by replacing 
`<path_to_study_directory>` with the absolute path to the study folder. The
command will connect to the web API of the container `cbioportal-container`, and import 
the study in its associated database. Make sure to replace `<path_to_report_folder>` with 
the absolute path were the html report of the validation will be saved.

```shell
docker run -it --rm --net cbio-net \
    -v /<path_to_config_file>/portal.properties:/cbioportal/portal.properties:ro \
    -v "<path_to_study_directory>:/study:ro" \
    -v "<path_to_report_folder>:/report" \
    cbioportal/cbioportal:latest \
    metaImport.py -u http://cbioportal-container:8080 -s /study --html=/report/report.html
```
:warning: after importing a study, remember to restart `cbioportal-container`
to see the study on the home page. Run `docker restart cbioportal-container`.

#### Using cached portal side-data ####

In some setups the data validation step may not have direct access to the web API, for instance when the web API is only accessible to authenticated browser sessions. You can use this command to generate a cached folder of files that the validation script can use instead. Make sure to replace `<path_to_portalinfo>` with the absolute path where the cached folder is going to be generated.

```shell
docker run --rm --net cbio-net \
    -v /<path_to_config_file>/portal.properties:/cbioportal/portal.properties:ro \
    -v "<path_to_portalinfo>/portalinfo:/portalinfo" \
    -w /cbioportal/core/src/main/scripts \
    cbioportal/cbioportal:latest \
    ./dumpPortalInfo.pl /portalinfo
```

Then, grant the validation/loading command access to this folder and tell the script it to use it instead of the API:

```shell
docker run -it --rm --net cbio-net \
    -v /<path_to_config_file>/portal.properties:/cbioportal/portal.properties:ro \
    -v "<path_to_study_directory>:/study:ro" \
    -v "<path_to_report_folder>:/report" \
    -v "<path_to_portalinfo>/portalinfo:/portalinfo:ro" \
    cbioportal/cbioportal:latest \
    metaImport.py -p /portalinfo -s /study --html=/report/report.html
```

### Inspecting or adjusting the database ###

When creating the database container, you can map a port on the
local host to port 3306 of the container running the MySQL database,
by adding an option such as `-p 127.0.0.1:8306:3306` to the `docker
run` command before the name of the image (`mysql:5.7`).  You can then
connect to this port (port 8306 in this example) using [MySQL
Workbench](https://www.mysql.com/products/workbench/) or another
MySQL client.

If you have not opened a port, the following command can still
connect a command-line client to the container (`cbioDB` here)
using the `--net` option:

```shell
docker run -it --rm \
    --net=cbio-net \
    -e MYSQL_HOST=cbioDB \
    -e MYSQL_USER=cbio \
    -e MYSQL_PASSWORD=P@ssword1 \
    -e MYSQL_DATABASE=cbioportal \
    mysql:5.7 \
    sh -c 'mysql -h"$MYSQL_HOST" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE"'
```

### Deleting a study ###

To remove a study, run:

```shell
docker run -it --rm --net cbio-net \
    -v /<path_to_config_file>/portal.properties:/cbioportal/portal.properties:ro \
    cbioportal/cbioportal:latest \
    cbioportalImporter.py -c remove-study -id study_id
```

Where `study_id` is the `cancer_study_identifier` of the study you would like to remove.
