#!/usr/bin/perl
require "../scripts/env.pl";

system ("$JAVA_HOME/bin/java -Xmx1524M -cp $cp -DCGDS_HOME='$cgdsHome' org.mskcc.cbio.cgds.scripts.drug.ImportPiHelperData $cgdsDataHome/reference-data/pihelper/drugs.tsv $cgdsDataHome/reference-data/pihelper/drugtargets.tsv");

