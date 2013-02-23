#!/usr/bin/perl
require "$PORTAL_HOME/core/src/main/scripts/env.pl";

system ("$JAVA_HOME/bin/java -Xmx1524M -cp $cp -DPORTAL_HOME='$portalHome' org.mskcc.cbio.cgds.scripts.drug.ImportDrugBank $portalDataHome/reference-data/drugbank/drugbank.xml $portalDataHome/reference-data/drugbank/target_links.csv");
