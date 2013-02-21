#!/usr/bin/perl
require "../scripts/env.pl";

system ("$JAVA_HOME/bin/java -Xmx1524M -cp $cp PORTAL_HOME='$portalHome' org.mskcc.cbio.cgds.scripts.ImportGisticData @ARGV");
