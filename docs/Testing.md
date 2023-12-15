# Introduction

This page is about how to run and debug, in an IDE agnostic way, any test classes added to the backend (Java layer).

# Steps

## Set up test instance of MySQL server

### System installed (assumes Linux OS)

1. Start mysql:

```
sudo systemctl start mysql
```

2. Log into database:

```shell
sudo mysql -u root
```

or

```shell
mysql -u root -p
```

3. In the MySQL console run:

```shell
SET GLOBAL local_infile=1;
CREATE DATABASE cgds_test;
CREATE USER 'cbio_user'@'localhost' IDENTIFIED BY 'somepassword';
GRANT ALL ON cgds_test.* TO 'cbio_user'@'localhost';
FLUSH PRIVILEGES;
SET default_storage_engine=InnoDB;
SET SESSION sql_mode = 'ANSI_QUOTES';
```

4. (Optional) Load schema and seed:

When running tests through Maven this step can be skipped. In MySQL console run:

```shell
source <path-to-file>/cgds-test.sql
source <path-to-file>/seed_mini.sql
```

#### Note

_Cgds-test.sql_ should be generated with `/db-scripts/src/main/resources/gen-cgds-test-schema.sh`.

## Docker container

1. Create file `init.sql` with the following contents:

```shell
SET GLOBAL local_infile=1;
CREATE DATABASE cgds_test;
CREATE USER 'cbio_user'@'localhost' IDENTIFIED BY 'somepassword';
GRANT ALL ON cgds_test.* TO 'cbio_user'@'localhost'";
FLUSH PRIVILEGES;
SET default_storage_engine=InnoDB;
SET SESSION sql_mode = 'ANSI_QUOTES';
```

2. Run (making sure the file paths are correct):

```shell
 docker run -ti --rm \
  --name testdb \
  --env MYSQL_ROOT_PASSWORD=root \
  --env MYSQL_USER=cbio_user \
  --env MYSQL_PASSWORD=somepassword \
  --env MYSQL_DATABASE=cgds_test \
  -v <path-to-file>/init.sql:/docker-entrypoint-initdb.d/first_file.sql \
  -v <path-to-file>/cgds-test.sql:/docker-entrypoint-initdb.d/second_file.sql \
  -v <path-to-file>/seed_mini.sql:/docker-entrypoint-initdb.d/third_file.sql \
  -p 3306:3306 mysql:5.7
  --ssl=0
```

#### Notes

- When running tests through Maven, the line with volume mounts (starting with `-v`) to the schema (_cgds-test.sql_) and
  seed (_seed_mini.sql_) should be skipped.
- _Cgds-test.sql_ should be generated with `/db-scripts/src/main/resources/gen-cgds-test-schema.sh`.

## Running the tests with Maven

1. Make sure you have built the project once with e.g. `mvn clean install -DskipTests`
2. Run integration tests with

```
mvn integration-test -Ddb.test.username=cbio_user -Ddb.test.password=somepassword
```

This will create the tables in `cgds_test` and populate your schema with test data.

3. you can run all other tests with

```
mvn test -Ddb.test.username=cbio_user -Ddb.test.password=somepassword
```

4. you can run a specific test with

```
mvn test -pl core -Dtest=TestIntegrationTest -Ddb.test.username=cbio -Ddb.test.password= <your_db_password>
```

where `-pl` is the name of the module (in this example `core`) and `-Dtest` is the name of the Java test class (in this
example `TestIntegrationTest`).

5. you can debug a specific test with

```
mvn integration-test -Dmaven.surefire.debug -pl core test -Dtest=TestIntegrationTest -Ddb.test.username=cbio -Ddb.test.password=<your_db_password> 
```

where `-pl` is the name of the module (in this example `core`) and `-Dtest` is the name of the Java test class (in this
example `TestIntegrationTest`). This command will pause execution and wait for you to connect your IDE to the listening
port, reporting something like below:

```
[INFO] Surefire report directory: /cbioportal/core/target/surefire-reports

-------------------------------------------------------
 T E S T S
-------------------------------------------------------

-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Listening for transport dt_socket at address: 5005
```

Once you connected your IDE (after setting a breakpoint in the code), the execution will continue in your debugger view.
