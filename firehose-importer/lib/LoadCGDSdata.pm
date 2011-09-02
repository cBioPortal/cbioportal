package LoadCGDSdata;

require Exporter;
@ISA = qw(Exporter);
@EXPORT = qw( run set_up_classpath loadCGDScancerMutationDataFile ); 

use strict;
use warnings;
use Getopt::Long;
use File::Spec;
use File::Util;
use Data::Dumper;
use Env::Path;
use FirehoseEnv;

use Utilities;

# SUMMARY
# Load CGDS files into dbms (LoadCGDSdata.pm)
# Uses CGDS Java library to load CGDS input files into dbms
# Issues: 
# Testing: No unit testing; LoadCGDSdata.t does nothing

# Uses CGDS to load dbms from CGDS input files
# Step 2 in Firehose data processing pipeline
# convertFirehoseData.pl => loadCGDSdata.pl => load Tomcat => view in web portal
 
# structure of $CGDSinputData data:
# each cancer has its own directory; 
# data
# data/cancers
# data/cancers/gbm
# data/cancers/gbm/case_lists
# data/cancers/gbm/case_lists/cases_CGH.txt
# o o o  other case lists
# data/cancers/gbm/data_CNA.txt
# data/cancers/gbm/data_expression_median.txt
# data/cancers/gbm/gbm.txt
# data/cancers/gbm/meta_CNA.txt
# data/cancers/gbm/meta_miRNA.txt
# data/cancers/gbm/meta_expression_median.txt
# data/cancers/stad
# o o o  data for stad
# o o o  other cancers
# human gene list
# data/human_gene_info_2011_01_19

# o o o  important dirs in $cgdsHome
# build: compiled code, web stuff
# lib: jars
# bin: scripts
# web: web source
# testData: test data files

# globals
my $fileUtil;

# Given a set of well-structured CGDS input files, load them into the CGDS database. Works for files converted from Firehose.
sub run{
	
	my ($class, 
		$cgdsHome,            # directory of CGDS code
		$CGDSinputData,       # directory of CGDS input data
		$Cancers,            # full filename of cancers file
		$GeneFile,            # full filename of gene file
		$miRNAfile,           # full filename of miRNAs file
        $nameOfPerCancerGermlineWhitelist,  # base filename of per cancer germline whitelist file, if any
        $nameOfPerCancerSomaticWhitelist,   # base filename of per cancer somatic whitelist file, if any
		$loadMutationArguments,   # global mutation loading arguments, passed without modification to org.mskcc.cgds.scripts.ImportProfileData
		                          # includes optional --acceptRemainingMutations and 
		                          # --somaticWhiteList <full filename of universal somatic whitelist file> if supplied
		) = @_;

	# check that required options are set
	my $util = Utilities->new( "" );
	$util->verifyArgumentsAreDefined( $cgdsHome, $CGDSinputData, $GeneFile, $miRNAfile );

	my $cmdLineCP = set_up_classpath( $cgdsHome );
	
	# Clear the Database
	# database parameters found in build.properties
	my $c = "$JAVA_HOME/bin/java -Xmx1524M -cp $cmdLineCP -DCGDS_HOME='$cgdsHome' org.mskcc.cgds.scripts.ResetDatabase";
	print "Running: $c\n";
	system ($c);

	$fileUtil = File::Util->new();

    # Load up cancers
    system ("$JAVA_HOME/bin/java -Xmx1524M -cp $cmdLineCP -DCGDS_HOME='$cgdsHome' org.mskcc.cgds.scripts.ImportTypesOfCancers " . $Cancers ); 

	# TODO: control or surpress progress messages
	# Load up Entrez Genes
	system ("$JAVA_HOME/bin/java -Xmx1524M -cp $cmdLineCP -DCGDS_HOME='$cgdsHome' org.mskcc.cgds.scripts.ImportGeneData " . $GeneFile ); 
	
	# Load up all microRNA IDs
	system ("$JAVA_HOME/bin/java -Xmx1524M -cp $cmdLineCP -DCGDS_HOME='$cgdsHome' org.mskcc.cgds.scripts.ImportMicroRnaData " . $miRNAfile );  
	    
    load_cancer_data( $cgdsHome, $CGDSinputData, $cmdLineCP, $nameOfPerCancerGermlineWhitelist, 
        $nameOfPerCancerSomaticWhitelist, $loadMutationArguments );
    
}

# TODO: HIGH: IMPORTANT: UNIT TESTS
# todo: documentation

# todo: delete empty dirs: scripts/, notes/, cyto/, results/

# Set up Classpath to use all JAR files in lib dir
# should be O/S portable, but not tested
sub set_up_classpath{
	my $theCGDShome = shift;
	unless( defined( $theCGDShome ) ){
		warn "\$theCGDShome not defined ";
	}
	my $classpath = Env::Path->CLASSPATH;
	$classpath->Append( File::Spec->catfile( $theCGDShome, qw(  build WEB-INF classes ) ) );
	
	my @jar_files = glob( File::Spec->catfile( $theCGDShome, qw(  lib *.jar ) ) );
	foreach my $jar (@jar_files) {
	    $classpath->Append( $jar );
	}
	$classpath->Uniqify;
	my $cmdLineCP = join( Env::Path->PathSeparator, $classpath->List );
    # print "jars and dirs:\n", join( "\n", $classpath->List), "\n";
	return $cmdLineCP;
}

# load all cancer data
sub load_cancer_data{
	my( $cgdsHome, $theCGDSinputFiles, $cmdLineCP, $nameOfPerCancerGermlineWhitelist, 
        $nameOfPerCancerSomaticWhitelist, $loadMutationArguments ) = @_;

	# for each cancer
	my @cancerDataDirs = $fileUtil->list_dir( File::Spec->catfile( $theCGDSinputFiles ), '--dirs-only', '--no-fsdots' );
	foreach my $cancerDataDir (@cancerDataDirs){
	    
	    print "Loading $cancerDataDir.\n";
	
	    # import all data
	    # import cancer's name and metadata
	    if( importCancerStudy( $cgdsHome, $theCGDSinputFiles, $cancerDataDir, $cmdLineCP ) ){
          
	        # import case lists, e.g., ./importCaseList.pl $CGDS_HOME/data/ovarian/case_lists   
	        importCaseLists( $cgdsHome, $theCGDSinputFiles, $cancerDataDir, $cmdLineCP );
	        
	        # TODO: import clinical data
	        
	        # import a cancer's data
	        importCancersData( $cgdsHome, $theCGDSinputFiles, File::Spec->catfile( $theCGDSinputFiles, $cancerDataDir ),
	           $cancerDataDir, $cmdLineCP, $nameOfPerCancerGermlineWhitelist,
	           $nameOfPerCancerSomaticWhitelist, $loadMutationArguments  );
	        print "timestamp: ", timing(), "Loading $cancerDataDir complete.\n";

	    }
	}
}   

# load a cancer's data; load each file prefixed by $Utilities::dataFilePrefix
sub importCancersData{
    my( $cgdsHome, $theCGDSinputFiles, $directory, $cancerDataDir, $cmdLineCP, $nameOfPerCancerGermlineWhitelist,
        $nameOfPerCancerSomaticWhitelist, $loadMutationArguments ) = @_;

    print "importCancersData: load data from '$directory'.\n";
    
    my @dataFiles = grep( /^$Utilities::dataFilePrefix/, $fileUtil->list_dir( $directory, '--files-only' ) );  
    print "importCancersData: data files: ", join( " ", @dataFiles ), "\n";
    foreach my $dataFile (@dataFiles){
        # handle mutation files separately
        if( $dataFile =~ /mutation/i ){
            loadCGDScancerMutationDataFile( $cgdsHome, $directory, File::Spec->catfile( $directory, $dataFile ), 
                $cmdLineCP, 
                $nameOfPerCancerGermlineWhitelist, $nameOfPerCancerSomaticWhitelist, $loadMutationArguments );
        }else{
            loadCGDScancerProfileDataFile( $cgdsHome, File::Spec->catfile( $directory, $dataFile ), $cmdLineCP );
        }
    }
}

# import a mutation data file into the CGDS dbms
# assumes a corresponding meta_ file exists; see $Utilities::metaFilePrefix
sub loadCGDScancerMutationDataFile{
    my( $cgdsHome, $directory, $dataFile, $cmdLineCP, 
        $nameOfPerCancerGermlineWhitelist, $nameOfPerCancerSomaticWhitelist, $loadMutationArguments ) = @_;

    print "loading: $dataFile\n";
    my $action;
    my $metaFile = $dataFile;
    $metaFile =~ s/$Utilities::dataFilePrefix/$Utilities::metaFilePrefix/;  
    print "using: $metaFile\n";
    
    # remove: append no longer supported
    
    # todo: check existence of files
    my $cmd = "$JAVA_HOME/bin/java -Xmx1524M -cp $cmdLineCP -DCGDS_HOME='$cgdsHome' org.mskcc.cgds.scripts.ImportProfileData " .
        " --data $dataFile --meta $metaFile --loadMode bulkLoad " . 
        ( defined( $loadMutationArguments ) ? $loadMutationArguments : '');
    if( defined( $nameOfPerCancerGermlineWhitelist ) && -r File::Spec->catfile( $directory, $nameOfPerCancerGermlineWhitelist ) ){
        $cmd .= " --germlineWhiteList " . File::Spec->catfile( $directory, $nameOfPerCancerGermlineWhitelist );
    }
    if( defined( $nameOfPerCancerSomaticWhitelist ) && -r File::Spec->catfile( $directory, $nameOfPerCancerSomaticWhitelist ) ){
        $cmd .= " --somaticWhiteList " . File::Spec->catfile( $directory, $nameOfPerCancerSomaticWhitelist );
    }
    print "loadCGDScancerMutationDataFile: ", "$cmd\n";
    system( $cmd );
}

# import a profile data file into the CGDS dbms
# assumes a corresponding meta_ file exists; see $Utilities::metaFilePrefix
#           loadCGDScancerProfileDataFile( $cgdsHome, File::Spec->catfile( $directory, $dataFile ), $cmdLineCP );

sub loadCGDScancerProfileDataFile{
    my( $cgdsHome, $dataFile, $cmdLineCP ) = @_;
    
    print "loading: $dataFile\n";
    my $action;
    my $metaFile = $dataFile;
    $metaFile =~ s/$Utilities::dataFilePrefix/$Utilities::metaFilePrefix/; 
    print "using: $metaFile\n";
    
    # removed 'append'
      
    # todo: check existence of files
    my $cmd = "$JAVA_HOME/bin/java -Xmx1524M -cp $cmdLineCP -DCGDS_HOME='$cgdsHome' " . 
        " org.mskcc.cgds.scripts.ImportProfileData --data $dataFile --meta $metaFile --loadMode bulkLoad ";
    system( $cmd );
}

# Load Cases
sub importCancerStudy{
    my( $cgdsHome, $theCGDSinputFiles, $cancer, $cmdLineCP ) = @_;

    my $cancerTypeFile = File::Spec->catfile( $theCGDSinputFiles, $cancer, $cancer . '.txt' );
    if( -r $cancerTypeFile ){
	    print "\$cancerTypeFile $cancerTypeFile\n";
	    system ("$JAVA_HOME/bin/java -Xmx1524M -cp $cmdLineCP -DCGDS_HOME='$cgdsHome' org.mskcc.cgds.scripts.ImportCancerStudy " . $cancerTypeFile );
	    return 1;
    }
    return 0;
}

# Load Cases
sub importCaseLists{
    my( $cgdsHome, $theCGDSinputFiles, $cancer, $cmdLineCP ) = @_;

    my $cancerCaseListsDir = File::Spec->catfile( $theCGDSinputFiles, $cancer, 'case_lists' );
	system ("$JAVA_HOME/bin/java -Xmx1524M -cp $cmdLineCP -DCGDS_HOME='$cgdsHome' org.mskcc.cgds.scripts.ImportCaseList " . $cancerCaseListsDir );
	
}

1;
