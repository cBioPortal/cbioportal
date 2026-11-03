#!/usr/bin/env perl
use FindBin;
require "$FindBin::Bin/env.pl";

exec("$JAVA_HOME/bin/java -Xmx1524M -cp $cp -DPORTAL_HOME='$portalHome' org.mskcc.cbio.portal.scripts.ImportSampleList @ARGV");
