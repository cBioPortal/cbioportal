#!/usr/bin/perl
require "../scripts/env.pl";

system ("$JAVA_HOME/bin/java -Xmx1524M -cp $cp -DPORTAL_HOME='$portalHome' org.mskcc.cbio.cgds.scripts.drug.ImportDrugBank $cgdsDataHome/reference-data/drugbank/drugbank.xml $cgdsDataHome/reference-data/drugbank/target_links.csv");
