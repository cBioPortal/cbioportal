#!/usr/bin/perl

use strict;
use warnings;
my $cp = '/home/goldberg/workspace/sander/cgds/build/WEB-INF/classes:' . 
	'/home/goldberg/workspace/sander/cgds/lib/collections-generic-4.01.jar:' . 
	'/home/goldberg/workspace/sander/cgds/lib/colt.jar:' . 
	'/home/goldberg/workspace/sander/cgds/lib/commons-collections-3.2.1.jar:' . 
	'/home/goldberg/workspace/sander/cgds/lib/commons-dbcp-1.2.1.jar:' . 
	'/home/goldberg/workspace/sander/cgds/lib/commons-lang-2.4.jar:' . 
	'/home/goldberg/workspace/sander/cgds/lib/commons-logging-1.0.4.jar:' . 
	'/home/goldberg/workspace/sander/cgds/lib/commons-math-1.2.jar:' . 
	'/home/goldberg/workspace/sander/cgds/lib/commons-pool-1.1.jar:' . 
	'/home/goldberg/workspace/sander/cgds/lib/concurrent.jar:' . 
	'/home/goldberg/workspace/sander/cgds/lib/jopt-simple-3.2.jar:' . 
	'/home/goldberg/workspace/sander/cgds/lib/jung-1.7.6.jar:' . 
	'/home/goldberg/workspace/sander/cgds/lib/junit.jar:' . 
	'/home/goldberg/workspace/sander/cgds/lib/log4j.jar:' . 
	'/home/goldberg/workspace/sander/cgds/lib/mysql-connector-java-3.1.10-bin.jar:' . 
	'/home/goldberg/workspace/sander/cgds/lib/servlet.jar';
	system( "java -Xmx1524M -cp $cp -DCGDS_HOME='/home/goldberg/workspace/sander/cgds' org.mskcc.cgds.scripts.ImportProfileData  " . 
	"--data /home/goldberg/Data/cgdsData/brca/data_mutations_extended.txt " . 
	"--meta /home/goldberg/Data/cgdsData/brca/meta_mutations_extended.txt " . 
	"--dbmsAction clobber " . 
	"--loadMode bulkLoad  " . 
	"--somaticWhiteList /home/goldberg/workspace/sander/import_and_convert_Firehose_data/reference_data/universalSomaticGeneWhitelist.txt  " . 
	"--germlineWhiteList /home/goldberg/Data/cgdsData/brca/germlineWhiteList.txt " . 
	"--somaticWhiteList /home/goldberg/Data/cgdsData/brca/significantlyMutatedSomaticGenesWhitelist.txt");
	system( "java -Xmx1524M -cp $cp -DCGDS_HOME='/home/goldberg/workspace/sander/cgds' org.mskcc.cgds.scripts.ImportProfileData  " . 
	"--data /home/goldberg/Data/cgdsData/coadread/data_mutations_extended.txt " . 
	"--meta /home/goldberg/Data/cgdsData/coadread/meta_mutations_extended.txt " . 
	"--dbmsAction clobber " . 
	"--loadMode bulkLoad  " . 
	"--somaticWhiteList /home/goldberg/workspace/sander/import_and_convert_Firehose_data/reference_data/universalSomaticGeneWhitelist.txt  " . 
	"--somaticWhiteList /home/goldberg/Data/cgdsData/coadread/significantlyMutatedSomaticGenesWhitelist.txt");
	system( "java -Xmx1524M -cp $cp -DCGDS_HOME='/home/goldberg/workspace/sander/cgds' org.mskcc.cgds.scripts.ImportProfileData  " . 
	"--data /home/goldberg/Data/cgdsData/gbm/data_mutations_extended.txt " . 
	"--meta /home/goldberg/Data/cgdsData/gbm/meta_mutations_extended.txt " . 
	"--dbmsAction clobber " . 
	"--loadMode bulkLoad  " . 
	"--somaticWhiteList /home/goldberg/workspace/sander/import_and_convert_Firehose_data/reference_data/universalSomaticGeneWhitelist.txt  " . 
	"--somaticWhiteList /home/goldberg/Data/cgdsData/gbm/significantlyMutatedSomaticGenesWhitelist.txt");
	system( "java -Xmx1524M -cp $cp -DCGDS_HOME='/home/goldberg/workspace/sander/cgds' org.mskcc.cgds.scripts.ImportProfileData  " . 
	"--data /home/goldberg/Data/cgdsData/laml/data_mutations_extended.txt " . 
	"--meta /home/goldberg/Data/cgdsData/laml/meta_mutations_extended.txt " . 
	"--dbmsAction clobber " . 
	"--loadMode bulkLoad  " . 
	"--somaticWhiteList /home/goldberg/workspace/sander/import_and_convert_Firehose_data/reference_data/universalSomaticGeneWhitelist.txt  " . 
	"--somaticWhiteList /home/goldberg/Data/cgdsData/laml/significantlyMutatedSomaticGenesWhitelist.txt");
	system( "java -Xmx1524M -cp $cp -DCGDS_HOME='/home/goldberg/workspace/sander/cgds' org.mskcc.cgds.scripts.ImportProfileData  " . 
	"--data /home/goldberg/Data/cgdsData/lusc/data_mutations_extended.txt " . 
	"--meta /home/goldberg/Data/cgdsData/lusc/meta_mutations_extended.txt " . 
	"--dbmsAction clobber " . 
	"--loadMode bulkLoad  " . 
	"--somaticWhiteList /home/goldberg/workspace/sander/import_and_convert_Firehose_data/reference_data/universalSomaticGeneWhitelist.txt  " . 
	"--somaticWhiteList /home/goldberg/Data/cgdsData/lusc/significantlyMutatedSomaticGenesWhitelist.txt");
	system( "java -Xmx1524M -cp $cp -DCGDS_HOME='/home/goldberg/workspace/sander/cgds' org.mskcc.cgds.scripts.ImportProfileData  " . 
	"--data /home/goldberg/Data/cgdsData/ov/data_mutations_extended.txt " . 
	"--meta /home/goldberg/Data/cgdsData/ov/meta_mutations_extended.txt " . 
	"--dbmsAction clobber " . 
	"--loadMode bulkLoad  " . 
	"--somaticWhiteList /home/goldberg/workspace/sander/import_and_convert_Firehose_data/reference_data/universalSomaticGeneWhitelist.txt  " . 
	"--germlineWhiteList /home/goldberg/Data/cgdsData/ov/germlineWhiteList.txt " . 
	"--somaticWhiteList /home/goldberg/Data/cgdsData/ov/significantlyMutatedSomaticGenesWhitelist.txt");
