#!/usr/bin/perl
require "../scripts/env.pl";

$cmd = join(' ', @ARGV);
$cmd = 'mvn -f $PORTAL_HOME/pom.xml exec:java -Dexec.mainClass="org.mskcc.cbio.cgds.scripts.GenerateMutationData" -Dexec.args="' . $cmd . '"';
system($cmd);
