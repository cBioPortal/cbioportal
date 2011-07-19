#!/usr/bin/perl
require "../scripts/env.pl";

system ("$JAVA_HOME/bin/java -Xmx1524M -cp $cp -DCGDS_HOME='$cgdsHome' org.mskcc.cgds.scripts.ResetDatabase @ARGV");
system( "../scripts/importTypesOfCancer.pl ../data/cancers.txt" );
