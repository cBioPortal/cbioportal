# End-to-End Tests
Make some screenshots of the cbioportal website using selenium. First compile
portal with the public database as done in `.travis.yml`. Then get the version
number of the compiled war file:
```bash
# export FINAL_WAR_NAME for use in docker-compose.yml
export FINAL_WAR_NAME=$(mvn -q \
                            -Dexec.executable="echo"
                            -Dexec.args='${final.war.name}'
                            --non-recursive
                            org.codehaus.mojo:exec-maven-plugin:1.3.1:exec)
```
boot cbioportal and the selenium grid:
```bash
docker-compose up
```
Then run the tests:
```bash
bash test_make_screenshots.sh screenshots.yml
```
