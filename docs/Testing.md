# Introduction
This page is about how to run and debug, in an IDE agnostic way,
any test classes added to the backend (Java layer).

# Steps

1. make sure you have a test database with name `cgds_test` available in mysql (this can be an empty db schema)
2. make sure you have built the project once with e.g. `mvn clean install -DskipTests`
3. Run integration tests with 
```
mvn integration-test -Ddb.test.username=cbio -Ddb.test.password=<your_db_password>
```
This will create the tables in `cgds_test` and populate your schema with test data.

4. you can run all tests with
```
mvn test -Ddb.test.username=cbio -Ddb.test.password=<your_db_password>
```

5. you can run a specific test with
```
mvn test -pl core -Dtest=TestIntegrationTest -Ddb.test.username=cbio -Ddb.test.password= <your_db_password>
```
where `-pl` is the name of the module (in this example `core`) and `-Dtest` is the name of the Java test
class (in this example `TestIntegrationTest`).

6. you can debug a specific test with
```
mvn integration-test -Dmaven.surefire.debug -pl core test -Dtest=TestIntegrationTest -Ddb.test.username=cbio -Ddb.test.password=<your_db_password> 
```
where `-pl` is the name of the module (in this example `core`) and `-Dtest` is the name of the Java test
class (in this example `TestIntegrationTest`).
This command will pause execution and wait for you to connect your IDE to the listening port,
reporting something like below:
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
Once you connected your IDE (after setting a breakpoint in the code), the execution will continue
in your debugger view.
