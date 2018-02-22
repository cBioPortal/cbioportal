#!/usr/bin/perl
require "../scripts/envSimple.pl";

system ("$JAVA_HOME/bin/java -Xmx1512M -Dspring.profiles.active=dbcp -cp $cp -DPORTAL_HOME='$portalHome' org.mskcc.cbio.portal.scripts.ImportReferenceGenome @ARGV");