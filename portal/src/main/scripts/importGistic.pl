#!/usr/bin/perl
require "../scripts/env.pl";

my $cmd = join(' ', @ARGV);
$cmd = 'mvn -f $PORTAL_HOME/pom.xml exec:java -Dexec.mainClass="org.mskcc.cbio.cgds.scripts.ImportGisticData" -Dexec.args="' . $cmd . '"';

system($cmd);
