#!/usr/bin/perl
require "../scripts/env.pl";

$cp="/Users/dresdneg/dev/cbio-cancer-genomics-portal/core/target/classes";

system ("$JAVA_HOME/bin/java -Xmx1524M -cp $cp -DCGDS_HOME='$cgdsHome' org.mskcc.cbio.cgds.scripts.ImportClinical @ARGV");
