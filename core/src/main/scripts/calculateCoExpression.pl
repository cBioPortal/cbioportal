#!/usr/bin/perl
require "../scripts/env.pl";

system ("$JAVA_HOME/bin/java -Xmx2048M -XX:-UseGCOverheadLimit -cp $cp -DPORTAL_HOME='$portalHome' org.mskcc.cbio.portal.scripts.CalculateCoexpression @ARGV");
