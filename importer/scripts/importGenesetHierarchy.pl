#!/usr/bin/env perl
use FindBin;
require "$FindBin::Bin/envSimple.pl";

exec("$JAVA_HOME/bin/java -Xmx1524M -Dspring.profiles.active=dbcp -cp $cp -DPORTAL_HOME='$portalHome' org.mskcc.cbio.portal.scripts.ImportGenesetHierarchy @ARGV");
