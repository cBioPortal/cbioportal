#!/usr/bin/perl
require "../scripts/env.pl";

exec("$JAVA_HOME/bin/java -Xmx1524M -cp $cp -DPORTAL_HOME='$portalHome' org.mskcc.cbio.portal.scripts.ImportTimelineData @ARGV");
