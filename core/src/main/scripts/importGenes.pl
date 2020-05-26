#!/usr/bin/env perl
require "../scripts/envSimple.pl";

system ("$JAVA_HOME/bin/java -Xmx1524M -Dspring.profiles.active=dbcp -cp $cp -DPORTAL_HOME='$portalHome' org.mskcc.cbio.portal.scripts.ImportGeneData @ARGV");
