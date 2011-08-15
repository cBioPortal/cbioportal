#!/usr/bin/perl
# author: Arthur Goldberg, goldberg@cbio.mskcc.org
require "../scripts/env.pl";

system ("$JAVA_HOME/bin/java -Xmx1524M -cp $cp -DCGDS_HOME='$cgdsHome' org.mskcc.cgds.scripts.ImportTypesOfCancers @ARGV");
