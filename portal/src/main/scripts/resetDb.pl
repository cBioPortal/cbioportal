#!/usr/bin/perl
require "env.pl";

$cmd = join(' ', @ARGV);
$cmd = 'mvn -f $PORTAL_HOME/pom.xml exec:java -Dexec.mainClass="org.mskcc.cbio.cgds.scripts.ResetDatabase" -Dexec.args="' . $cmd . '"';
system($cmd);

print "$cp\n";
