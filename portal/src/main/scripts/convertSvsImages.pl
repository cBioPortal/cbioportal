#!/usr/bin/perl

system ("$JAVA_HOME/bin/java -Xmx1524M -cp $cp org.mskcc.cbio.cgds.scripts.ConvertSvsImages @ARGV");
