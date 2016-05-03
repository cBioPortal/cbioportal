Once you have initialized MySQL with the seed database, you are ready to import a sample cancer study.  This is recommended, so that you can verify everything is working.

# Download the Sample Study

To get started, download the sample study:  [brca-example-study.tar.gz](http://cbio.mskcc.org/cancergenomics/public-portal/downloads/brca-example-study.tar.gz).

Then, unpack and move the directory into your root cbio portal development directory.  The directory should be called `portal-study`.

# Import the Sample Study

This is done in a few steps:

First set the path to the jar of the JDBC Connector:

    CONNECTOR_JAR=$(echo $CATALINA_HOME/lib/mysql-connector-java-*-bin.jar)

Make sure the ``CONNECTOR_JAR`` variable is pointing to one jar:

    echo $CONNECTOR_JAR
    # output e.g. /Library/Tomcat/lib/mysql-connector-java-5.1.35-bin.jar

Second, set the path to the core jar of cbioportal:

    CORE_JAR=$(echo $PORTAL_HOME/core/target/core-*.jar)

Again, make sure the ``CORE_JAR`` variable is pointing to one jar:

    echo $CORE_JAR
    # output e.g. /Users/inodb/git/cbioportal/core/target/core-0.1.2-SNAPSHOT.jar   

Then, load the meta-data for the sample study:

    $PORTAL_HOME/core/src/main/scripts/cbioportalImporter.py --jvm-args "-Dspring.profiles.active=dbcp -cp $CONNECTOR_JAR:$CORE_JAR" --command import-study --meta-filename portal-study/meta_study.txt

Then, load copy number, mutation data and expression data:

    $PORTAL_HOME/core/src/main/scripts/cbioportalImporter.py --jvm-args "-Dspring.profiles.active=dbcp -cp $CONNECTOR_JAR:$CORE_JAR" --command import-study-data --meta-filename portal-study/meta_CNA.txt --data-filename portal-study/data_CNA.txt
    $PORTAL_HOME/core/src/main/scripts/cbioportalImporter.py --jvm-args "-Dspring.profiles.active=dbcp -cp $CONNECTOR_JAR:$CORE_JAR" --command import-study-data --meta-filename portal-study/meta_mutations_extended.txt --data-filename portal-study/data_mutations_extended.txt
    $PORTAL_HOME/core/src/main/scripts/cbioportalImporter.py --jvm-args "-Dspring.profiles.active=dbcp -cp $CONNECTOR_JAR:$CORE_JAR" --command import-study-data --meta-filename portal-study/meta_expression_median.txt --data-filename portal-study/data_expression_median.txt

Lastly, load the case sets and clinical attributes:

    $PORTAL_HOME/core/src/main/scripts/cbioportalImporter.py --jvm-args "-Dspring.profiles.active=dbcp -cp $CONNECTOR_JAR:$CORE_JAR" --command import-case-list --meta-filename portal-study/case_lists
    $PORTAL_HOME/core/src/main/scripts/cbioportalImporter.py --jvm-args "-Dspring.profiles.active=dbcp -cp $CONNECTOR_JAR:$CORE_JAR" --command import-study-data --meta-filename portal-study/meta_clinical.txt --data-filename portal-study/data_clinical.txt

# Important

Please note that these data files are intentionally small, and do not contain all genes or all samples.  Therefore, when you deploy in the next step, and search for, e.g. BRCA1, you may not see any mutations.  If you want to see mutations, try searching for:  ACVR1B.

[Next Step: Deploying the Web Application](Deploying.md)