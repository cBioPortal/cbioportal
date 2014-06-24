#!/usr/bin/perl
use strict;
use warnings;
use File::Util;
use Cwd 'abs_path'; # unfortunately, File::Util doesn't find absolute paths
use Data::Dumper;
use Path::Class;
use Getopt::Long;

# compareDirectAndBulkDBMSload.pl
# automate performance comparison of cgds upload scripts
# test performance of upload of all cgds shellscripts, and plot results

# author: Arthur Goldberg
# date: Oct 2010
# this program compares the performance and results of direct load (record insert via SQL INSERT) and bulkload (record insert via
# MySQL LOAD FILE). It executes a set of shell scripts in cgds/bin, provided by the --cgdsScripts option or cgdsLoadScripts().
# each script is executed twice, once using direct load and once using bulk load. We compare 
# 1) the performance of each execution, and 
# 2) the DBMS results of each execution
# performance results are written to a file named by $outFileName, structured for import into a spreadsheed and plotting
# DBMS results are compared via MySQL checksum, and written to a file named by $checksumCompareOutput; direct and bulk load should
# produce identical results in each table; as of Oct 2010, they do.

# on laptop: ./compareDirectAndBulkDBMSload.pl --outputDir "/Users/goldbera/Documents/Projects/TCGA Portal/Fast DBMS load/data/" --MySQLdbParams h=localhost,u=root,p=anOKpwd --database cgds
# on toro: nohup ./compareDirectAndBulkDBMSload.pl --outputDir "/home/goldberg/data" --MySQLdbParams h=localhost,u=cbio,p=cbio --database cgds_arthur &> stdout.txt&
# on toro: uses ./build/WEB-INF/classes/org/mskcc/cbio/portal/util/portal.properties


# CONSTANTS
my $loadTypeFile = '/tmp/loadTypeFile.txt'; # TODO: put this file in a private (hidden) directory for cgds, so it won't be removed by other code
my $resetDBcmd = 'resetDb.pl';
my $LOAD_FILE_TYPE = 'LOAD_FILE'; # name for LOAD FILE timing, and a hash key for storing the LOAD FILE timings
my $RECORD_COUNT = 'RECORD_COUNT'; # a hash key for storing the RECORD_COUNT info

my @loadTypes = qw( directLoad bulkLoad );
my $totalLoadTimeName = "Total load time";

# COMMAND LINE OPTIONS
my $cgdsScriptsDir; # cgds root directory 
my $outputDir;  # results output directory
my $MySQLdbParams; # params used by checksum
my $database;  # database name

# GLOBALS
my $checksumCompareOutput;

my $cgdsScripts; 

my $usage = <<EndOfUsage;
compareDirectAndBulkDBMSload.pl 
--cgdsScriptsDir <cgds scripts directory>
--outputDir <results output directory>
--MySQLdbParams <dbms access params, used by checksum; see format in http://www.maatkit.org/doc/mk-table-checksum.html#specifying_hosts>
--database <database name>
--cgdsScripts <list of scripts to run>
EndOfUsage

main();
sub main{
	
	my $result = GetOptions (
		"cgdsScriptsDir=s" => \$cgdsScriptsDir,
		"outputDir=s" => \$outputDir,
		"MySQLdbParams=s" => \$MySQLdbParams,
		"database=s" => \$database,
		"cgdsScripts=s" => \$cgdsScripts,
	);
	unless( $result and allDefined( $outputDir, $MySQLdbParams, $database ) ){
		# TODO: more detailed errors on command line options
		print STDERR "Problem with command line options:\n";
		print STDERR $usage;
		exit(1);
	}	

	# use current dir if $cgdsScriptsDir opt not provided
	unless( defined( $cgdsScriptsDir ) ){
		$cgdsScriptsDir = File::Spec->rel2abs( File::Spec->curdir() );
		# print $cgdsScriptsDir, "\n";
	}

	$checksumCompareOutput = File::Spec->catfile( $outputDir, "checksumCompare.txt" );
	measureDBMSload();
	collectAndOutputDBMSloadMeasures();	
}

# return true iff all arguments are defined
sub allDefined{
	my( @args ) = @_;
	foreach my $a (@args){
		if( !defined($a) ){
			return 0;
		}
	}
	return 1;
}

# get list of scripts used by cgds; *.sh in the scripts directory
sub cgdsLoadScripts {
	if( defined($cgdsScripts) ){
		# print "cgdsLoadScripts returning ", Dumper( split( ' ', $cgdsScripts) );
		return split( ' ', $cgdsScripts);
	} else {
		my $f = File::Util->new();
		my @dirs_and_files = $f->list_dir( $cgdsScriptsDir,'--files-only', '--pattern=\.sh$');
		# print "cgdsLoadScripts returning ", Dumper( @dirs_and_files );
		return @dirs_and_files
	}
#	return qw( all-breast.sh		all-ovarian.sh		all-test.sh all-gbm.sh		all-prostate.sh		);
}

# run all the scripts provided by cgdsLoadScripts, and run each script with the old, direct dbms writing approach 
# and our new, bulk LOAD FILE approach. For each run of each script
# 1. measure its overall execution time and the execution time of some of its components, and 
# 2. check that the run creates the same database
sub measureDBMSload {	

	foreach my $loadScript (cgdsLoadScripts()) {

		# pair of checksum files for each dbms writing approach
		my @checksumOutputFiles = ();

		# run script with direct and bulk load, saving output to file named 'script.<loadType>.perf.txt'
		foreach my $loadType ( @loadTypes ) {

			# empty the dbms; don't want to time that
			my $cmd = File::Spec->catfile( $cgdsScriptsDir, $resetDBcmd );
			print "Running '$cmd'.\n";
			system( $cmd );

			# write to load script and load type dependent file
			my $outFileName = 'performance.' . $loadScript . '.' . $loadType . '.out.txt';

			# to control type of load, have importProfileData.pl read it from a file
			system( "echo $loadType > $loadTypeFile" );

			# measure total time
			my $startTime = time();
			my $now_string = localtime;
			
			my $outputDirEscapeForShell = $outputDir;
			# TODO: make this a little, portable sub
			$outputDirEscapeForShell =~ s/ /\\ /g;

			my $outFile = File::Spec->catfile( $outputDirEscapeForShell, $outFileName );
			# was my $outFile = $outputDirEscapeForShell . $outFileName;

			$cmd = File::Spec->catfile( $cgdsScriptsDir, $loadScript )  . " > $outFile";
			# was $cmd = $cgdsScriptsDir  . $loadScript . " > $outFile";
			print "Running '$cmd'.\n";
			system( $cmd );
			my $endTime = time();
			my $executionTimeMins = ($endTime - $startTime)/60;

			# remove $loadTypeFile
			system( "rm $loadTypeFile" );

			# take checkpoint of database, and save it into file with name that indicates the $loadScript and $loadType
			my $checkSumCmd = "mk-table-checksum " . $MySQLdbParams . ' --databases ' . $database;

			my $checkSumOutfile = $outFile . ".checksum";
			system( "$checkSumCmd > $checkSumOutfile" );
			push @checksumOutputFiles, $checkSumOutfile;

			my $resultsFile = file( $outputDir, $outFileName ); # Path::Class::File object
			open(my $resultsFH,  ">>",  $resultsFile )  or die "Can't open '$resultsFile' for appending: $!";
			
			# save total time in same file, to be grabbed by collectAndOutputDBMSloadMeasures();
			print $resultsFH sprintf("%.1f", $executionTimeMins), "\t$totalLoadTimeName", "\t$now_string\t$loadScript\t$loadType\n";
			close( $resultsFH );

		}

		# compare checkpoints from two different $loadType(s)
		my $compareCheckSums = "mk-checksum-filter " . join( ' ', @checksumOutputFiles );
		print "Comparing checksums: $compareCheckSums\n";
		my $checksumComparison = `$compareCheckSums`;

		# if result is empty string, report good news, otherwise report that DBMSes differ
		my $msg = "Compare checksums for '$loadScript':\n";
		if( $checksumComparison eq '' ){
			$msg .= "EXCELLENT, databases are identical.\n"; 
		} else {
			$msg .= "ERROR, DATABASES DIFFER.\n"; 	
			$msg .= $checksumComparison;
		}
		# output report to STDERR and append to file that tracks results of from multiple 
		print STDERR $msg;

		# label checksum comparison report with date timestamp
		system( "date >> " . '"' . $checksumCompareOutput . '"' );
		open(my $checksumCompareFH,  ">>",  $checksumCompareOutput )  
			or die "Can't open '$checksumCompareOutput' for appending: $!";
		print $checksumCompareFH $msg, "\n";
		close( $checksumCompareFH );
		
	}
}

# create matrix of data types vs. load type, save to data directory
# suitable for inserting into a spreadsheet
sub collectAndOutputDBMSloadMeasures {
	
	# create hash of timings as fn of data types, load type, & script
	my $timings = {};
	
	# hash of total response times, as fn of load type, & script
	my $totalTimes = {};

	my $resultsFile = file( $outputDir, 'performance.out.txt' ); 
	my $summarizedTimingsFH = $resultsFile->openw();
	# for debugging: 	$summarizedTimingsFH = *STDOUT;

	foreach my $loadScript (cgdsLoadScripts()) {

		my @dataTypes = ();
		# COLLECT DATA
		foreach my $loadType ( @loadTypes ) {

			# read file for this script, loadType
			my $fileContainingMeasurements = File::Spec->catfile( $outputDir, 'performance.' . $loadScript . '.' . $loadType . '.out.txt' );
			# was: my $fileContainingMeasurements = $outputDir . 'performance.' . $loadScript . '.' . $loadType . '.out.txt';
			
			open(my $in,  "<",  $fileContainingMeasurements ) 
				or die "Can't open '$fileContainingMeasurements' for reading: $!";
				
			my $loadFileTime = 0;
			my $tableName = 0;
			my $numRecords = 0;

			while( <$in> ){
				chomp;
				my $line = $_;

				# get right lines from output files
				if( $line =~ /ImportProfileData/ ) {
					my( $minutes, $constant, $measuredDataType, @rest) = split( "\t", $line );
					my $cgdsRoot = abs_path( File::Spec->updir($cgdsScriptsDir) );
					
					$measuredDataType =~ s|$cgdsRoot|..|g; # get rid of dir
					my @tmp = split( ' ', $measuredDataType ); # keep just first dir
					$measuredDataType = $tmp[0];
				
					$measuredDataType =~ s/ clobber.*$//g; # get rid of other args to org.mskcc.cbio.portal.scripts.ImportProfileData

					$timings->{$loadScript}->{$measuredDataType}->{$loadType} = $minutes;

					# sum "LOAD FILE" timings into $timings
					$timings->{$loadScript}->{$measuredDataType}->{ $LOAD_FILE_TYPE } = $loadFileTime;
					$loadFileTime = 0;

					$timings->{$loadScript}->{$measuredDataType}->{ $RECORD_COUNT } = $numRecords;
					$numRecords = 0;
				
					# keep @dataTypes in order, but don't duplicate for each loadType
					unless( grep( /$measuredDataType/, @dataTypes ) ){
						push @dataTypes, $measuredDataType;
					}
				}
				
				if( $line =~ /$totalLoadTimeName/ ) {
					my( $minutes, $constant, @rest) = split( "\t", $line );
					$totalTimes->{$loadScript}->{$loadType} = $minutes;
				}
				
				# sum "LOAD FILE" timings 
				if( $line =~ /$LOAD_FILE_TYPE/ ) {
					# parse output from line ~146 of MySQLbulkLoader.java
					my( $minutes, $constant, $table, $constant2, $recordCount, @rest ) = split( "\t", $line );
					$loadFileTime += $minutes;
					$numRecords += $recordCount;
					$tableName = $table;
				}
				
			}
		}

		# OUTPUT DATA
		# START A NEW SET OF ROWS; OUTPUT HEADER WITH $loadScript
		print $summarizedTimingsFH "\n$loadScript\n";
		
		# add $LOAD_FILE_TYPE to the @loadTypes; each corresponds to a measurement in a column
		my @loadTypesAndSubtypes = (@loadTypes, $LOAD_FILE_TYPE, $RECORD_COUNT);

		# OUTPUT HEADER WITH @loadTypesAndSubtypes; 
		print $summarizedTimingsFH "Data type\t", join( "\t", @loadTypesAndSubtypes ), "\n";

		foreach my $dataType ( @dataTypes ) {
			
			print $summarizedTimingsFH $dataType;
			foreach my $loadType ( @loadTypesAndSubtypes ) {

				# OUTPUT timing
				print $summarizedTimingsFH "\t", $timings->{$loadScript}->{$dataType}->{$loadType};

			}

			print $summarizedTimingsFH "\n";
		}

		# output TOTAL across @dataTypes
		my $total = {};
		foreach my $loadType ( @loadTypesAndSubtypes ) {
			$total->{$loadType} = 0.0;
			foreach my $dataType ( @dataTypes ) {
				$total->{$loadType} += $timings->{$loadScript}->{$dataType}->{$loadType};
			}
		}
		print $summarizedTimingsFH "Total";
		foreach my $loadType ( @loadTypesAndSubtypes ) {
			print $summarizedTimingsFH "\t", $total->{$loadType};
		}
		print $summarizedTimingsFH "\n";

		# output measured TOTAL
		print $summarizedTimingsFH "Total time for $loadScript";
		foreach my $loadType ( @loadTypesAndSubtypes ) {
			if( defined( $totalTimes->{$loadScript}->{$loadType} )){
				print $summarizedTimingsFH "\t", $totalTimes->{$loadScript}->{$loadType};
			}else{
				print $summarizedTimingsFH "\t";
			}
		}
		print $summarizedTimingsFH "\n";

	}
	
	# Close file
	close( $summarizedTimingsFH );
	# open in OO and draw chart	
	print "Results written to '$resultsFile'\n";
}
