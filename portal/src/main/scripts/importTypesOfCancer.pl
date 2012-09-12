#!/usr/bin/perl
# author: Arthur Goldberg, goldberg@cbio.mskcc.org
require "../scripts/env.pl";

$cmd = join(' ', @ARGV);
$cmd = 'mvn -f $PORTAL_HOME/pom.xml exec:java -Dexec.mainClass="org.mskcc.cbio.cgds.scripts.ImportTypesOfCancers" -Dexec.args="' . $cmd . '"';
system($cmd);
