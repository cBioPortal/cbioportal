#!/usr/bin/env perl
require "../scripts/envSimple.pl";

exec("$JAVA_HOME/bin/java -Xmx1524M -Dspring.profiles.active=dbcp -cp $cp -DPORTAL_HOME='$portalHome' org.mskcc.cbio.portal.scripts.AddCaseList @ARGV");
