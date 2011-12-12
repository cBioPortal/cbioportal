#!/usr/bin/perl
# file: convertFirehoseData.pl
# author: Arthur Goldberg, goldberg@cbio.mskcc.org

# SUMMARY 
# Main program that converts Firehose data into CGDS input (convertFirehoseData.pl)
# Driver to clean up custom data added to a Firehose TCGA dataset, in preparation for running convertFirehoseData.pl
# Uses Workflow dependencies to decide which files to try to download
# Issues: Contains too much code; these routines should be moved to other modules and unit tested:
# download_from_firehose, and clean_up_after_download to DownloadFromFirehose.pm 
# convert_to_full_pathnames, and localVerifyArgumentsAreDefined to Utilities.pm
# create_copy_of_firehose_data to new module
# No unit testing or end-to-end tests
# main() is a mess with 2 exit() calls
# Doesn't recover from download that obtains file with bad MD5 checksum
# Testing: no unit testing

# Step2 1 & 2 in Firehose data processing pipeline
# convertFirehoseData.pl => loadCGDSdata.pl => load Tomcat => view on web
# downloads Firehose data from NCI (optionally), transforms Firehose files into CGDS file formats, and
# loads all downloaded and converted Firehose data into CGDS

# todo: run taint perl (perl -T) to protect against user input
use strict;
use warnings;
use Getopt::Long;
use File::Spec;
use File::Util;
use File::Path qw(make_path remove_tree);
use Data::Dumper;
use Digest::MD5;
use Data::CTable;

use GeneIdentifiers;
use Utilities;
use FirehoseFileMetadata;
use CreateDataFiles;
use FirehoseTransformationWorkflow;
use ConvertFirehoseData;

# typical args: --RootDir /Users/goldbera/Data/firehose/data/current --Cancers cancers --FirehoseDirectory FirehoseOutput 
# --CGDS /Users/goldbera/Documents/workspace/cgds/data/cancers  --Genes human_gene_info_2011_01_19 --miRNAfile microRNAs.txt 
# --firehoseTransformationWorkflowFile FirehoseTransformationWorkflow.yaml
# --codeForCGDS /Users/goldbera/Documents/workspace/cgds --Clean 
# if downloading, add: --FirehoseURL https://tcga-data.nci.nih.gov/tcgafiles/ftp_auth/distro_ftpusers/tcga4yeo/other/gdacs/gdacbroad/ --FirehoseURLUserid SanderC --FirehoseURLPassword 'Nci$2012' 
# if creating small copy of data, can say, e.g.: --CreateCopyOfFirehoseData /Users/goldbera/Data/firehose/data/copyOfCurrent --Limit 100

# todo: more documentation
# todo: automate all unit tests
# todo: end-to-end tests
# todo: allow numeric geneIDs in oncoSpec lang 

my $usage = <<EOT;
usage:
convertFirehoseData.pl
--FirehoseURL <URL>                             # optional URL of firehose data; download from Firehose if provided
--FirehoseURLUserid <Userid>                    # optional userid for Firehose URL
--FirehoseURLPassword <Password>                # optional password for same
--RootDir <root directory>                      # optional root directory; all other file names can be absolute or relative -- relative files rooted here
--FirehoseDirectory <Firehose Directory>        # required; directory which stores firehose data
--CGDSDataDirectory <CGDS data directory>       # required; directory into which CGDS data should be stored
--Clean                                         # if set, remove exising output from CGDSDataDirectory 
--Cancers <file containing cancers to process and their meta data>
                                                # required; name of file listing cancers to process, and their descriptions; other cancers will be ignored
--OverlappingCancers <file containing cancers that overlap between gdac and public portals>
--Genes <gene file>                             # required; name of file listing genes with Symbol to ID mapping; typically gene_info at ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/gene_info.gz 
--miRNAfile <name of miRNA file>                # file containing miRNA mappings
--firehoseTransformationWorkflowFile            # file containing Workflow dependencies
--codeForCGDS <directory>                       # directory containing CGDS code              
--CreateCopyOfFirehoseData                      # optional directory; if provided, copy of necessary Firehose data will be created and stored here
--Limit                                         # lines per file for copy of Firehose data
--Summary                                       # file in which to output run data summary; relative to CGDSDataDirectory; optional -- if not provided, is convertFirehoseData.out
EOT

# todo: HIGH: should make 1) download and cleanup stand-alone (Niki wants a modified one to get all), 2) make copy firehose data stand-alone

# globals
# command line options
my( $FirehoseURL, $FirehoseURLUserid, $FirehoseURLPassword, 
    $RootDir, $FirehoseDirectory, $CGDSDataDirectory, $Clean,
	$Cancers, $OverlappingCancers, $Genes, $miRNAfile,
	$firehoseTransformationWorkflowFile, $codeForCGDS, 
    $CreateCopyOfFirehoseData, $Limit, $Summary );

# todo: document
my $fileUtil;
my $CancerDataDir;
my $firehoseTransformationWorkflow;   # Workflow dependencies
my $runSummary = {};            # hash handle to a summary of the run; entries addressed $runSummary->{cancer}->{column}
my $utilities;                  # instance handle to a Utilities object
my $runDate;                    # date of Firehose run, as read from the Firehose data
my $runDirectory;               # directory containing the Firehose run, as read from the Firehose data

# TODO: clean this up;
# should have 3 separate programs: download, create_copy and convert
# especially rediculous that currently download and create_copy require CLI params that aren't needed!
main();
sub main{	
	
	print timing(), "Starting.\n";
	
	pre_initialize();
	process_command_line();
    initialize();

    # download from NCI, if URL set
    if( defined( $FirehoseURL )){
    	download_from_firehose();
    	clean_up_after_download();
        print timing(), "Download complete.\n";
        exit;
    }

    getCancerDataDir();

    if( defined( $CreateCopyOfFirehoseData )){
    	
    	# todo: make this a stand=alone program
		# make selected, reduced copy of firehose data to ease browsing and testing
        # todo: in create_copy_of_firehose_data copy miRNA data to the copy  
		create_copy_of_firehose_data( $CancerDataDir, $CreateCopyOfFirehoseData, $Limit );
        # todo: move to create_copy_of_firehose_data()
        # copy cancer and gene_list data to the copy  
        runSystem( "cp $Cancers $CreateCopyOfFirehoseData", "running: cp $Cancers $CreateCopyOfFirehoseData" );
        runSystem( "cp $Genes $CreateCopyOfFirehoseData" );
        # todo: now, work with the copy of the data, and get rid of exit; have $FirehoseDirectory point to $CreateCopyOfFirehoseData, and keep going
		exit;
    }

	create_cgds_input_files( $Cancers, $Summary, $CGDSDataDirectory, $CancerDataDir, $runDirectory, $firehoseTransformationWorkflow, $codeForCGDS, 
        $Genes, $runDate );
	print timing(), "create_cgds_input_files complete.\n";
}

sub pre_initialize{
	$utilities = Utilities->new( $usage );
}

sub process_command_line{

	# process arg list
	GetOptions (
	    "FirehoseURL=s" => \$FirehoseURL,
	    "FirehoseURLUserid=s" => \$FirehoseURLUserid,
	    "FirehoseURLPassword=s" => \$FirehoseURLPassword,
	    "RootDir=s" => \$RootDir,    
	    "FirehoseDirectory=s" => \$FirehoseDirectory,    
	    "CGDSDataDirectory=s" => \$CGDSDataDirectory,      
	    "Clean" => \$Clean,      
	    "Cancers=s" => \$Cancers, 
	    "OverlappingCancers=s" => \$OverlappingCancers, 
        "Genes=s" => \$Genes,
        "miRNAfile=s" => \$miRNAfile,
        "firehoseTransformationWorkflowFile=s" => \$firehoseTransformationWorkflowFile,
        "codeForCGDS=s" => \$codeForCGDS,
        "CreateCopyOfFirehoseData=s" => \$CreateCopyOfFirehoseData,
        "Limit=i" => \$Limit,
        "Summary=s" => \$Summary,
	);

	# make sure necessary arguments are set	
    if( defined( $FirehoseURL )){
        localVerifyArgumentsAreDefined( qw( FirehoseURLUserid FirehoseURLPassword ) );
    }
    
    localVerifyArgumentsAreDefined( qw( FirehoseDirectory CGDSDataDirectory Cancers Genes miRNAfile firehoseTransformationWorkflowFile codeForCGDS ) );
    convert_to_full_pathnames( $RootDir, 'read', qw( FirehoseDirectory CGDSDataDirectory Cancers Genes miRNAfile firehoseTransformationWorkflowFile codeForCGDS ) );

    if( defined( $CreateCopyOfFirehoseData )){

        localVerifyArgumentsAreDefined( qw( CreateCopyOfFirehoseData ) );
	    # convert these all to full pathnames
	    convert_to_full_pathnames( $RootDir, 'read', qw( CreateCopyOfFirehoseData ) );		
	}else{

	    # summary: optionally provided filename
	    unless( defined( $Summary )){
	        $Summary = 'convertFirehoseData.out';
	    }
	    convert_to_full_pathnames( $CGDSDataDirectory, 'write', qw( Summary ) );      
	}
}

sub initialize{
	
	# if --Clean is set, remove all existing CGDS (output) data
	if( defined( $Clean )){
	    print "Removing existing output from '$CGDSDataDirectory'.\n";
	    remove_tree( $CGDSDataDirectory, {keep_root => 1 } ); 
	}
	
	$fileUtil = File::Util->new( );
	$firehoseTransformationWorkflow = FirehoseTransformationWorkflow->new( $firehoseTransformationWorkflowFile );

}

sub download_from_firehose{

    localVerifyArgumentsAreDefined( qw( FirehoseURLUserid FirehoseURLPassword OverlappingCancers) );
    
    # get just the tar-zipped directories we need, which is somewhat complicated
    # 1) get the date of the most recent run; we assume all cancers were processed on the most recent date
    # 2) get the list of cancers available by parsing the index file
    # 3) for each cancer, get the files we need from the most recent run
    
    # 1) get the date of the most recent run; we assume all cancers were processed on the most recent date
    my $latestRunFile = 'LATEST_RUN';
    my $URLofLATEST_RUN = "$FirehoseURL/$latestRunFile";
    # login and override 'robot' exclusion
    my @args = ( '--recursive', '-e', 'robots=off', '--no-parent', '--no-verbose', 
        "--user=$FirehoseURLUserid", "--password=$FirehoseURLPassword", 
        "--directory-prefix=$FirehoseDirectory", "$URLofLATEST_RUN");
    my @overlappingCancers = listCancers( $OverlappingCancers );
    
    # todo: figure out why this fails to login when run in Eclipse, but works OK on the command line
    runSystem( 'wget', undef, @args ); # /usr/local/bin/    
    
    my @latestRunFiles = grep( /$latestRunFile/, $fileUtil->list_dir( $FirehoseDirectory, '--recurse', '--files-only'  ) );
    unless( defined( $latestRunFiles[0] )){
    	die "Could not find $latestRunFile";
    }
    my @d = $fileUtil->load_file( $latestRunFiles[0], '--as-lines');
    my $mostRecentRunDate = $d[1];

    # 2) get the list of cancers available by parsing the index file
    @args = ( '-e', 'robots=off', '--no-parent', '--no-verbose', 
        "--user=$FirehoseURLUserid", "--password=$FirehoseURLPassword", 
        "--directory-prefix=$FirehoseDirectory", "$FirehoseURL");
    runSystem( 'wget', undef, @args );
    # TODO: get rid of the old index files
    my @indexFile = $fileUtil->load_file(  File::Spec->catfile( $FirehoseDirectory, 'index.html' ), '--as-lines' );
    my @TCGAcancers;
    # look for pattern like: <a href="brca/">brca/</a>; use RE backreference
    foreach my $line (@indexFile){
    	if( $line =~ m|<a href="(\w+)/">\1/</a>| ){
    		push @TCGAcancers, $1;
    	}
    }
    print "TCGA cancers: ", join ' ', @TCGAcancers, "\n";

    # 3) for each cancer, get the files we need from the most recent run
	# files to get
	# make pattern that will get file that is a gzipped tar of a directory, AND the file's md5
	my @filesToGet;
    foreach my $sub ( $firehoseTransformationWorkflow->get_subroutine_sequence() ) {
	
        my $FirehoseDirectoriesAndFiles = $firehoseTransformationWorkflow->getFirehoseDirectoriesAndFiles($sub);

        foreach my $dirAndFile ( @{$FirehoseDirectoriesAndFiles} ) {
            my( $FirehoseDirPattern, $FirehoseFilePattern ) = @{$dirAndFile}; 

	        # wildcard for each '<name>' pattern
	        $FirehoseDirPattern =~ s/<\w+>/*/g;
	        # wildcard at the end to get md5
	        $FirehoseDirPattern =~ s/$/*/;
	        # remove adjacent wildcards
	        $FirehoseDirPattern =~ s/\*+/*/g;
	        # print "pattern for wget: $file\n";
	        push @filesToGet, $FirehoseDirPattern;
        }
	}

	# eliminate duplicate directory name patterns
	my $filesToGet = join( ',', removeDupes( @filesToGet ) );

	# server directory root is URL without hostname
	my $serverDir = $FirehoseURL;
	$serverDir =~ s|.*//[^/]+/||;

    foreach my $cancer (@TCGAcancers){
	    
    # e.g., this works (on the command line): wget --recursive -e robots=off --no-parent --user=SanderC --password='NCI$2011' 
	    # --directory-prefix=/tmp/firehoseData --accept=gdac.broadinstitute.org_*.Gistic2.Level_4.*,gdac.broadinstitute.org_*.Merge_mirna__h_mirna_8x15k*__unc_edu__Level_3__unc_DWD_Batch_adjusted__data.Level_3.*,gdac.broadinstitute.org_*.GDAC_median_mRNA_Expression.Level_4.*,gdac.broadinstitute.org_*.MutationAssessor.Level_4.* 
	    # --include-directories='tcgafiles/ftp_auth/distro_ftpusers/tcga4yeo/other/gdacs/gdacbroad/read/2011011400' 
	    # https://tcga-data.nci.nih.gov/tcgafiles/ftp_auth/distro_ftpusers/tcga4yeo/other/gdacs/gdacbroad/read/2011011400

	    my $cancersAnalysesURL = "$FirehoseURL/$cancer/analyses/$mostRecentRunDate";
	    my $cancersStddataURL = "$FirehoseURL/$cancer/stddata/$mostRecentRunDate";

	    # only get from those directories with the most recent run date
	    my $includeDirectories = "$serverDir/$cancer/analyses/$mostRecentRunDate,$serverDir/$cancer/stddata/$mostRecentRunDate"; 

	    # grab data that lives in analyses subdir
	    @args = ( '--recursive', '-e', 'robots=off', '--no-verbose', '--no-parent', # # '--debug', 
	        "--user=$FirehoseURLUserid", "--password=$FirehoseURLPassword", 
	        "--directory-prefix=$FirehoseDirectory", "--accept=$filesToGet", "--include-directories=$includeDirectories", "$cancersAnalysesURL" );     
	        print 'wget ', join ' ', @args, "\n";

	    runSystem( 'wget', "Downloading from $cancersAnalysesURL\nThis may take a while.\n", @args ); # was in /usr/local/bin/

	    # grab data that lives in stddata subdir
	    @args = ( '--recursive', '-e', 'robots=off', '--no-verbose', '--no-parent', # # '--debug', 
	        "--user=$FirehoseURLUserid", "--password=$FirehoseURLPassword", 
	        "--directory-prefix=$FirehoseDirectory", "--accept=$filesToGet", "--include-directories=$includeDirectories", "$cancersStddataURL" );     
	        print 'wget ', join ' ', @args, "\n";

	    runSystem( 'wget', "Downloading from $cancersStddataURL\nThis may take a while.\n", @args ); # was in /usr/local/bin/

		# to avoid rewriting other parts of firehose to work with new directory structure we do the following:
		
		# create oldstyle $cancer/$mostRecentRunDate subdir
		my $downloadLocation = "$FirehoseDirectory/" . substr $FirehoseURL, 8;
		my $cancerSubdir = "$downloadLocation/$cancer/$mostRecentRunDate";
		make_path( $cancerSubdir );
		
		# move from analyses/rundate -> cancer/rundate
		my $fromAnalyses = "$downloadLocation/$cancer/analyses/$mostRecentRunDate";
		system( "mv $fromAnalyses/* $cancerSubdir" );

		# move from stddata/rundate -> cancer/rundate
		my $fromStddata = "$downloadLocation/$cancer/stddata/$mostRecentRunDate";
		system( "mv $fromStddata/* $cancerSubdir" );

		# get rid of unused subdirs
		remove_tree( "$downloadLocation/$cancer/analyses", "$downloadLocation/$cancer/stddata/" ); 

		# if this is an overlapping cancer, we need to rename cancer type to tumor_type_gdac
		unless ($cancer  ~~ @overlappingCancers) {
			system( "mv $cancerSubdir $cancerSubdir_gdac" );
		}
    }
}

sub getCancerDataDir{
	
	# to avoid another command line argument, find Firehose directory containing all the cancer data directories
    my %dirsContainingCancerData;
    my @allDirs = $fileUtil->list_dir( $FirehoseDirectory, '--recurse', '--dirs-only'  );    
    my @cancers = listCancers( $Cancers );
    foreach my $cancer (@cancers){
        my $pattern = '\\' . $fileUtil->SL . $cancer . '$'; 
        foreach my $d (grep( /$pattern/, @allDirs )){
            $dirsContainingCancerData{ $fileUtil->return_path( $d ) } = 1;
        }
    }
    my @dirs = keys %dirsContainingCancerData;
    if( scalar( @dirs ) != 1 ){
        print STDERR join( "\n", @dirs ), "\n";
        die "\n" . scalar( @dirs ) . " directories contain cancer data, but should be exactly 1.\n";
    }
    $CancerDataDir = pop( @dirs );

    print "Firehose cancer data in '$CancerDataDir'.\n";        
    
	# the cancer's data will be in the subdirectory named by the most recent run date; s/b only directory
	@dirs = $fileUtil->list_dir( File::Spec->catfile( $CancerDataDir, $cancers[0] ), '--dirs-only', '--no-fsdots', );
	if( scalar(@dirs) != 1 ){
		die "Multiple Firehose runs for ", $cancers[0], "\n";
	}
	$runDirectory = $dirs[0];
	# remove the last 2 chars of the $runDirectory to get the run date 
    $runDate = $dirs[0];
	$runDate =~ s/\d\d$//;
    print "Run date: $runDate\n";
    print "Run directory: $runDirectory\n";
}

# CLEAN UP AND CHECK FIREHOSE DATA after a download
sub clean_up_after_download{

    # --pattern doesn't work with --recurse; bug reported 1/14/2011
    my @allFiles = $fileUtil->list_dir( $FirehoseDirectory, '--recurse', '--files-only'  ); 
    
    my $i = 0;

    # check all MD5 checksums
    my @md5Files = grep( /md5/, @allFiles );
    print "Checking MD5 checksums for " . scalar( @md5Files ) . " files. This may take a while.\n";
    foreach my $md5file ( @md5Files ) {
        # get checksummed File
        my $checksummedFile = $md5file; $checksummedFile =~ s/\.md5$//;
        open( FILE, '<', $checksummedFile) or die "Can't open '$checksummedFile' for reading: $!";
        binmode FILE or die $!;
    
        my $computedMD5wFilename = Digest::MD5->new->addfile(*FILE)->hexdigest . ' ' . $fileUtil->strip_path( $checksummedFile );
        my $sentMD5 = $fileUtil->load_file( $md5file );
        chomp $sentMD5;
        
        if( $computedMD5wFilename ne $sentMD5 ){

            warn "Error: Removing bad file: $checksummedFile ";
            unlink $checksummedFile or warn "Could not remove $checksummedFile: $!";
            unlink $md5file or warn "Could not remove $md5file: $!";
            # todo: recover from bad file
            print STDERR "Checksum sent:\n$sentMD5\n";
            print STDERR "Checksum computed:\n$computedMD5wFilename\n";
        }
        print ".";
        unless( ++$i % 50 ){
            print ": $i\n";
        }
    }
    print "Checksum checking complete.\n";
    # TODO: perhaps get rid of md5 files
    
    # recalculate list, since some files may have been deleted
    @allFiles = $fileUtil->list_dir( $FirehoseDirectory, '--recurse', '--files-only'  );

    # tar xzf all *tar.gz files
    my @tarGzFiles = grep( /\.tar\.gz$/, @allFiles );
    print "Uncompressing and unpacking ", scalar( @tarGzFiles ), " tar.gz files. This may take a while.\n";
    foreach my $tarGzFile ( @tarGzFiles ) {
        my @args = ('xzf', $tarGzFile, "-C", $fileUtil->return_path( $tarGzFile ) ); 
        runSystem( 'tar', undef, @args ); # was in /usr/bin/
        print ".";
        unless( ++$i % 50 ){
        	print ": $i\n";
        }
    }
    print "Uncompressing and unpacking complete.\n";	
}

# create a copy of the firehose data to be used for testing
# makes small files so that conversion code can process them quickly
# truncate files if requested
# 
# parameters:
# $FirehoseDataDir:     directory containing all firehose data
# $CopyDestinationDir:  directory into which copy willl be written; caution: erased initially
# $lines:               if provided, maximum number of lines in output files 
sub create_copy_of_firehose_data{
    my( $FirehoseDataDir, $CopyDestinationDir, $lines ) = @_;
    
    print "\nCopying essential Firehose data from $FirehoseDataDir to $CopyDestinationDir.\n";

    # iterate through entries in the FirehoseTransformationWorkflow file
    # and identify data provided by Firehose
        
    # clear out destination dir
    remove_tree( $CopyDestinationDir, {keep_root => 1 } );     
    
    my $dataDirName = 'data';
        
    # make all the cancer directories (need dir for each cancer, even if it has no useful data)
    my @allDirs = $fileUtil->list_dir( $FirehoseDataDir, '--dirs-only', '--no-fsdots' );
    foreach my $d (@allDirs) {
        my $destFn = File::Spec->catfile( $CopyDestinationDir, $dataDirName, $d );
        $fileUtil->make_dir( $destFn  );
    }
    
    my @wantedFiles;
    foreach my $sub ( $firehoseTransformationWorkflow->get_subroutine_sequence() ) {
    
        # find directories and files containing data we'll use
        my @allFiles = $fileUtil->list_dir( $FirehoseDataDir, '--files-only', '--recurse' );          
        
        # filter for dirs
        # replace <cancer>, <CANCER>, <version>, <date> with wildcards
        my @patterns = qw( <cancer> <CANCER> <version> <date> );
        my $FirehoseDirectoriesAndFiles = $firehoseTransformationWorkflow->getFirehoseDirectoriesAndFiles($sub);

        foreach my $dirAndFile ( @{$FirehoseDirectoriesAndFiles} ) {
            my( $FirehoseDirPattern, $FirehoseFilePattern ) = @{$dirAndFile}; 
            
            # filter for directories    
	        foreach my $p (@patterns){
	            $FirehoseDirPattern =~ s/$p/.*/g;
	        }
	        $FirehoseDirPattern =~ s/\.\*\.\*/.*/g;
	        # print "\$FirehoseDirPattern: $FirehoseDirPattern\n";
            my @wantedDirs = grep( m|$FirehoseDirPattern|, @allFiles );

            # filter for file
            foreach my $p (@patterns){
                $FirehoseFilePattern =~ s/$p/.*/g;
            }
            $FirehoseFilePattern =~ s/\.\*\.\*/.*/g;
            # print "\$FirehoseFilePattern: $FirehoseFilePattern\n";
            push @wantedFiles, grep( m|$FirehoseFilePattern|, @wantedDirs );

        }
    }
    
    @wantedFiles = removeDupes( @wantedFiles );
    print "Copying " . scalar( @wantedFiles ) . " Firehose files to $CopyDestinationDir.\n";
    
    my @cancersFound;
	foreach my $f ( @wantedFiles ){
        # read file
        # print "reading $f\n";
		
		# write to new location
		# get last 2 dirs and filename
	    my($volume,$directories,$file) = File::Spec->splitpath( $f );
	    my @dirs = File::Spec->splitdir( $directories ); 
        my $destDir = File::Spec->catfile( $CopyDestinationDir, $dataDirName, @dirs[ $#dirs-3 .. $#dirs-1 ] );
        my $destFn = File::Spec->catfile( $destDir, $file );

	    # if necessary, make the file's directory
	    unless( -r $destDir ){
            $fileUtil->make_dir( $destDir );
	    }

        # print "writing $destFn\n";
        # todo: some systems use -n, others --lines? make portable; head --lines=$lines $f > $destFn
        my $args = "head -n $lines $f > $destFn";
        runSystem( $args, $args );
	    push @cancersFound, $dirs[ $#dirs-3];
	}
}

# todo: add a Firehose run timestamp

###############
# UTILITIES

# todo: figure out how to put these into a module and move to Utilities.pm; need to be able to eval $arg in other package ...
# see http://www.webmasterkb.com/Uwe/Forum.aspx/perl/31984/FAQ-7-26-How-can-I-find-out-my-current-or-calling-package

# subs that check input arguments, directories and files

# convert all filenames and directories in @allInputPaths to full pathnames
# @allInputPaths is a list of text values of variable names 
# if an input path is already absolute, leave alone
# else if RootDir defined
#         make relative to RootDir
# check that a readable directory
# $mode is 'read' or 'write'; defaults to 'read'
# todo: HIGH: move to utilities, and unit test
sub convert_to_full_pathnames{

    my( $RootDir, $mode, @allInputPaths ) = @_;
    unless( defined( $mode ) ){
        $mode = 'read';
    }
    if( defined( $RootDir ) ){
        
        foreach my $inputPath (@allInputPaths){
            # was, e.g.:     $FullFirehoseDirectory = CreateFullPathname( $RootDir, $FirehoseDirectory, 'FullFirehoseDirectory' );
            my $v = '$' . $inputPath; 
            my $toEval = "$v = \$utilities->CreateFullPathname( '$RootDir', $v, '$inputPath' );";
            eval $toEval;

            # check on dirs and files
            $toEval = "if( defined( $v) ) {
                if( -d $v ){  \$utilities->CheckOnDirectory( $v, '$inputPath' ); }else{  \$utilities->CheckOnFile( '$mode', $v ); }
            }";
            eval $toEval;
        }
    }
}

# verify that all arguments are defined
# todo: move to Utilities.pm; needs package and symbol table manipulation 
sub localVerifyArgumentsAreDefined{
    foreach my $arg (@_){
        if( !eval 'defined( $' . $arg . ')'  ){
            $utilities->quit( "$arg is required" );
        }
    }
    return 1;
}

# todo: logging: better reporting of errors / let firehose folks know
# todo: some provenance tracking
