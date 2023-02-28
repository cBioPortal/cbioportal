#!/usr/bin/env perl
require "../scripts/env.pl";

system ("$JAVA_HOME/bin/java -Xmx1192M -cp $cp -DPORTAL_HOME='$portalHome' org.mskcc.cbio.portal.scripts.GenerateMutationData @ARGV");
