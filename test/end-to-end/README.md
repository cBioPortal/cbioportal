# End-to-End Tests
Make some screenshots of the cbioportal website using phantomjs. First, spin up
an instance of the portal that uses the amazon public db. For instance from the
cbioportal repo root directory:
```
export PORTAL_HOME=`pwd` &&  mvn -e -Ppublic -DskipTests -Ddb.user=cbio_user \
    -Ddb.password=cbio_pass -Ddb.portal_db_name=public_test \
    -Ddb.connection_string=jdbc:mysql://cbioportal-public.c1xhhbwn8izk.us-east-1.rds.amazonaws.com:3306/ \
    -Ddb.host=cbioportal-public.c1xhhbwn8izk.us-east-1.rds.amazonaws.com clean install
java -Ddbconnector=dbcp \
     -jar portal/target/dependency/webapp-runner.jar \
     --expand-war portal/target/cbioportal.war
```
Then run the tests:
```
bash test/end-to-end/test_make_screenshots.sh
```
Notice that the resulting screenshots are probably going to be slightly
different from the ones included in the repo. PhantomJS, the library used for
making the screenshots produces different screenhots on different machines. The
ones included in the repo were taken on Travis CI.
