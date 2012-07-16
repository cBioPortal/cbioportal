#!/usr/bin/perl
require "../scripts/env.pl";

################################################################
# Script to convert MAF files with ncbi build hg18 to hg19.
# This script requires an executable binary file named liftOver.
# It is available at http://hgdownload.cse.ucsc.edu/admin/exe/
# Detailed information about liftOver tool can be found at
# http://genome.ucsc.edu/cgi-bin/hgLiftOver
#
################################################################

# extract required information from the MAF file
print ("[info] Creating input files for lift over tool...\n");
system ("$JAVA_HOME/bin/java -Xmx1192M -cp $cp -DCGDS_HOME='$cgdsHome' org.mskcc.cgds.scripts.PreLiftOver $ARGV[0]");

# run the liftOver tool for conversion
print ("[info] Running liftOver tool...\n");
system ("./liftOver oldfile.txt hg18ToHg19.over.chain newfile.txt unmapped.txt");

# process files created by liftOver to update old MAF
print ("[info] Updating positions and creating the new MAF...\n");
system ("$JAVA_HOME/bin/java -Xmx1192M -cp $cp -DCGDS_HOME='$cgdsHome' org.mskcc.cgds.scripts.PostLiftOver $ARGV[0] newfile.txt unmapped.txt auxfile.txt $ARGV[1]");

# clean intermediate files
unlink("oldfile.txt");
unlink("auxfile.txt");
unlink("newfile.txt");
unlink("unmapped.txt");
