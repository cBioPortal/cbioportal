### Importing data ###

Use this command to validate a dataset in the folder `./study-dir`, connecting
to the web API of the container `cbioportal-container`, and import it into the
database configured in the image, saving an html report of the validation to
`~/Desktop/report.html`. Note that the paths passed to the `-v` option must be
absolute paths.

```shell
docker run -it --rm --net cbio-net \
    -v /<path_to_config_file>/portal.properties:/cbioportal/portal.properties:ro \
    -v "$PWD/study-dir:/study:ro" \
    -v "$HOME/Desktop:/outdir" \
    cbioportal/cbioportal:latest \
    metaImport.py -u http://cbioportal-container:8080 -s /study --html=/outdir/report.html
```
:warning: after importing a study, remember to restart `cbioportal-container`
to see the study on the home page. Run `docker restart cbioportal-container`.

#### Using cached portal side-data ####

In some setups the data validation step may not have direct access to the web API, for instance when the web API is only accessible to authenticated browser sessions. You can use this command to generate a cached folder of files that the validation script can use instead:

```shell
docker run --rm --net cbio-net \
    -v /<path_to_config_file>/portal.properties:/cbioportal/portal.properties:ro \
    -v "$PWD/portalinfo:/portalinfo" \
    -w /cbioportal/core/src/main/scripts \
    cbioportal/cbioportal:latest \
    ./dumpPortalInfo.pl /portalinfo
```

Then, grant the validation/loading command access to this folder and tell the script it to use it instead of the API:

```shell
docker run -it --rm --net cbio-net \
    -v /<path_to_config_file>/portal.properties:/cbioportal/portal.properties:ro \
    -v "$PWD/study-dir:/study:ro" \
    -v "$HOME/Desktop:/outdir" \
    -v "$PWD/portalinfo:/portalinfo:ro" \
    cbioportal/cbioportal:latest \
    metaImport.py -p /portalinfo -s /study --html=/outdir/report.html
```

### Importing data (method 2) ###

Similar to the method above, but here you open a bash shell in an otherwise idle container and run the commands there.

#### Step 1 (one time only for a specific image) ####

Set up the container `importer-container` mapping the input and
output dirs with `-v` parameters, and keep it running idle in the
background:

```shell
docker run -d --name="importer-container" \
    --restart=always \
    --net=cbio-net \
    -v /<path_to_config_file>/portal.properties:/cbioportal/portal.properties:ro \
    -v "$PWD"/study-dir:/study:ro \
    -v "$HOME"/Desktop:/outdir \
    cbioportal/cbioportal:latest tail -f /dev/null
```

#### Step 2 ####

Run bash in the container and execute the import command.

```shell
docker exec -it importer-container bash
```
The import command:
```shell
metaImport.py -u http://cbioportal-container:8080 -s /study --html=/outdir/report.html
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
