#!/usr/bin/perl
require "env.pl";

system ("java -Xmx1524M -cp $cp org.mskcc.portal.tool.ExecuteBundle");
