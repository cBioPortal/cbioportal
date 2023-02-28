#!/usr/bin/env perl
require "../scripts/env.pl";

system ("$JAVA_HOME/bin/java -Xmx1524M -cp $cp -DPORTAL_HOME='$portalHome' org.mskcc.cbio.portal.scripts.drug.ImportPiHelperData $portalDataHome/reference-data/pihelper/drugs.tsv $portalDataHome/reference-data/pihelper/drugtargets.tsv");

