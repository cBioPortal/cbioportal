package ConvertFirehoseData;

# core functionality to convert Firehose data to CGDS input files
# driver program that iterates over cancers and 
# calls modules to convert files, generate metadata, create case lists and meta files
# author: Arthur Goldberg, goldberg@cbio.mskcc.org

# this package assumes Firehose data is stored in the following directory structure:
# $CancerDataDir/<cancer_name>/$runDirectory/<data_type_dir>

# SUMMARY
# Driver program (ConvertFirehoseData.pm)
# Iterate over cancers
# Controlled by cancer list file and firehose workflow configuration
# Calls modules to convert Firehose to CGDS files, generate metadata, create case lists and meta files
# Issues: move some properties to config files;
# move case list creation to separate module
# Testing: substantial coverage, but not case list creation

require Exporter;
@ISA = qw(Exporter);
@EXPORT = qw( create_cgds_input_files createMetaFile ); 

use strict;
use warnings;
use File::Spec;
use File::Util;
use Data::Dumper;
use Data::CTable;

use GeneIdentifiers;
use Utilities;
use FirehoseFileMetadata;
use CaseIDs;
use CreateDataFiles;
use FirehoseTransformationWorkflow;

########################
# CREATE CGDS INPUT FILES

# params:
# $Cancers              pathname of file storing list of cancers
# $Summary              pathname of file in which to store summary of ConvertFirehoseData run
# $CGDSDataDirectory    directory storing cgds files
# $CancerDataDir        directory containing Firehose data about all the cancers
# $runDirectory         name of directory in $CancerDataDir/<cancer> containing data type dirs (that's the Firehose structure)
# $FirehoseXformWorkflow     handle to FirehoseTransformationWorkflow object 
# $codeForCGDS          directory storing cgds code
# $GenesFile            file containing genes (gene info file from NCBI)
# $runDate              date of file run - name for the suffix of the data dirs
sub create_cgds_input_files{
	my( $Cancers, $Summary, $CGDSDataDirectory, $CancerDataDir, $runDirectory, $FirehoseXformWorkflow, $codeForCGDS, 
	$GenesFile, $runDate ) = @_;
    
	my $fileUtil = File::Util->new();

    # iterate through cancers specified in Cancers file
    my @cancersFile = $fileUtil->load_file( $Cancers, '--as-lines');
    
    # $globalHash holds data that's communicated BETWEEN invocations of different cancers or subroutines
    my $globalHash = {}; 
    $globalHash->{ RUN_SUMMARY } = {};
    # load the gene map
    $globalHash->{ GENE_MAP } = GeneIdentifiers->new( $GenesFile );

    # hash storing summary of cgds file creation
    my $runSummary = $globalHash->{ RUN_SUMMARY };
    
    foreach my $line (@cancersFile){
        my( $cancer, $name ) = split( /\s*:\s*/, $line );
        print "\nProcessing $cancer ($name):\n";

        # summary: create column with abbreviation
        $runSummary->{$cancer} = {};
        
        # create directories for CGDS input files
        $fileUtil->make_dir( File::Spec->catfile( $CGDSDataDirectory, $cancer, 'case_lists' ), '--if-not-exists' );
        
        # process the cancer
        CreateCancersCGDSinput( $globalHash, $cancer, $name,
           File::Spec->catfile( $CancerDataDir, $cancer, $runDirectory ),
           File::Spec->catfile( $CGDSDataDirectory, $cancer), 
           $FirehoseXformWorkflow, $runSummary, $codeForCGDS, $runDate );
    }
    clean_up( $runSummary, $Summary ); 
    print "\n";
}

# create a cancer's CGDS input files
# params:

# $cancer: the cancer's abbreviated name
# $name: the cancer's descriptive name
# $CancersFirehoseDataDir: the cancer's data directory containing Firehose data
# $CancersCGDSinputDir: the directory into which CGDS input data will be written
# $FirehoseXformWorkflow: a handle to a FirehoseTransformationWorkflow object
# $runSummary: filename of file for storing the case counts summary 
# $codeForCGDS: dir of CGDS code 
# $GenesFile: file with gene data
# $runDate: [optional] rundate of Firehose (dir name below $cancer)
sub CreateCancersCGDSinput{
    my( $globalHash, $cancer, $name, $CancersFirehoseDataDir, $CancersCGDSinputDir, $FirehoseXformWorkflow, $runSummary, 
        $codeForCGDS, $runDate ) = @_;
        
    # create CGDS input data and meta files
    my $FirehoseFileMetadata_objects = create_data_and_meta_files( $globalHash, $cancer, $CancersFirehoseDataDir, $CancersCGDSinputDir, 
        $FirehoseXformWorkflow, $codeForCGDS, $runDate );

    # create CGDS case lists
    # TODO put in config file
    my $case_list_FileProperties = {
        # a case list file is generated from most Firehose files
    	# this hash defines the properties of the case list files generated
    	# key: name of case list file
    	# FirehoseFile: suffix of Firehose file name
        # in the following fields, these patterns are replaced: 
            # <cancer> -> 'the cancer's abbreviation'
            # <cases> -> 'the number of cases in the file'
        # xformFunc: a function to call that transforms each case name

        # these fields (keys) describe the given field in the case list file: stable_id, cancer_study_identifier, case_list_name, case_list_description
        'cases_CGH.txt' => {
            'FirehoseFile'          => 'all_thresholded.by_genes.txt',
            'xformFunc'               => undef,
            'stable_id'             => '<cancer>_acgh',
            'cancer_study_identifier'        => '<cancer>',
            'case_list_name'        => 'Tumors aCGH',
            'case_list_description' =>
              'All tumors with aCGH data (<cases> samples)',
        },
        'cases_mRNA.txt' => {
            'FirehoseFile'          => '<CANCER>.transcriptome__agilentg4502a_07_3__unc_edu__Level_3__unc_lowess_normalization_gene_level__data.data.txt',
            'xformFunc'               => undef,
            'stable_id'             => '<cancer>_mrna',
            'cancer_study_identifier'        => '<cancer>',
            'case_list_name'        => 'Tumors with mRNA data',
            'case_list_description' =>
              'All samples with mRNA expression data (<cases> samples)',
        },
        'cases_normal_mRNA.txt' => {
            'FirehoseFile'          => '<CANCER>.transcriptome__agilentg4502a_07_3__unc_edu__Level_3__unc_lowess_normalization_gene_level__data.data.txt',
            'xformFunc'               => \&matchedNormalCaseID,  # the CASE-IDs sub that identifies normals            
            # todo: someday firehose will include normals data

            'stable_id'             => '<cancer>_normal_mrna',
            'cancer_study_identifier'        => '<cancer>',
            'case_list_name'        => 'Normal Samples with mRNA',
            'case_list_description' =>
              'All normal samples with mRNA expression data (<cases> samples)',
        },
        'cases_sequenced.txt' => {
            'FirehoseFile'          => '<CANCER>.maf.annotated',
            'xformFunc'               => \&tumorCaseID,     # the CASE-IDs sub that identifies solid tumors, e.g. TCGA-04-1331-01 
            # todo: eventually treat recurrent tumors differently

            'stable_id'             => '<cancer>_sequenced',
            'cancer_study_identifier'        => '<cancer>',
            'case_list_name'        => 'Sequenced Tumors',
            'case_list_description' =>
              'All (Next-Gen) sequenced samples (<cases> samples)',
        },
    };
        
    create_one_to_one_case_lists( $FirehoseFileMetadata_objects, $runSummary, File::Spec->catfile( $CancersCGDSinputDir, 'case_lists'), 
        $cancer, $case_list_FileProperties, 
        [qw( stable_id cancer_study_identifier case_list_name case_list_description )] ); 
    
    # create cases_all.txt
    create_many_to_one_case_lists( 
        $FirehoseFileMetadata_objects,
        $runSummary,
        File::Spec->catfile( $CancersCGDSinputDir, 'case_lists'),
        'cases_all.txt',
        $cancer,
        # todo: make these table/config file driven
        [ qw( 
            all_thresholded.by_genes.txt
            <CANCER>.transcriptome__agilentg4502a_07_3__unc_edu__Level_3__unc_lowess_normalization_gene_level__data.data.txt
         ) ],
        'union',
        {
            cancer_study_identifier =>  '<cancer>',
            stable_id => '<cancer>_all',
            case_list_name =>  'All Tumors',
            case_list_description =>  'All tumor samples (<cases> samples)',
        }
    );

    # create cases_complete.txt
    create_many_to_one_case_lists( 
        $FirehoseFileMetadata_objects,
        $runSummary,
        File::Spec->catfile( $CancersCGDSinputDir, 'case_lists'),
        'cases_complete.txt',
        $cancer,
        # todo: make these table/config file driven
        [ qw( 
            <CANCER>.transcriptome__agilentg4502a_07_3__unc_edu__Level_3__unc_lowess_normalization_gene_level__data.data.txt
            all_thresholded.by_genes.txt
            <CANCER>.maf.annotated
         ) ],
        'intersection',
        {
            cancer_study_identifier =>  '<cancer>',
            stable_id => '<cancer>_3way_complete',
            case_list_name =>  'All Complete Tumors',
            case_list_description =>  'All tumor samples that have mRNA, CNA and sequencing data (<cases> samples)',
        }
    );

    # create cancer-type name file
    createCancerTypeNameFile( $FirehoseFileMetadata_objects, $cancer, $name, $CancersCGDSinputDir );    
}

# create CGDS input data files (and their meta files) as indicated in FirehoseTransformationWorkflow config file
sub create_data_and_meta_files{
    my( $globalHash, $cancer, $CancersFirehoseDataDir, $CancersCGDSinputDir, $FirehoseXformWorkflow, $codeForCGDS, $runDate ) = @_;
#print Dumper $cancer, $CancersFirehoseDataDir, $CancersCGDSinputDir, $FirehoseXformWorkflow, $codeForCGDS, $runDate;
    unless( -d $CancersFirehoseDataDir ){
        warn "\nNo data available for $cancer.\n";
        return;
    }
    
    $FirehoseXformWorkflow->reset();

    my $createDataFiles = CreateDataFiles->new( $globalHash->{ GENE_MAP } );

    # iterate through entries in the FirehoseTransformationWorkflow config file
    # and identify data provided by Firehose
    
    # create a list of FirehoseFileMetadata objects, one for each Firehose file
    my $FirehoseFileMetadata_objects = [];
    
    # if there's a maf file, the number of sequenced cases for the current cancer type
    $globalHash->{NUM_SEQUENCED_CASES} = undef; 
    
    dataTransformationEntry:
	# iterate over the subroutines which 
	# 1) meet the dependency criteria, and 
	# 2) have the necessary files
	# in the order they appear in the FirehoseTransformationWorkflow config file
	my $sub;
	while( $sub = $FirehoseXformWorkflow->next_sub( $CancersFirehoseDataDir, $cancer, $runDate ) ) {

        print "create_data_and_meta_files: subroutine: $sub\n";     
        # if the Firehose file(s) exist, call the sub that will make the CGDS input file
        
        # returns a ref to a list of refs to pairs of the form [directory, file], needed because an analysis may take multiple files
        my $FirehoseDirectoriesAndFiles = $FirehoseXformWorkflow->getFirehoseDirectoriesAndFiles($sub);

        my $firehoseFiles = [];
        my $cTables = [];
        
        my @explicitFiles;

        my $numCases = 0;
        my $numGenes = 0;

        foreach my $dirAndFile ( @{$FirehoseDirectoriesAndFiles} ) {
            my( $firehoseDir, $firehoseFile ) = @{$dirAndFile}; 
            my $FullFirehoseFile = getLastestVersionOfFile( $CancersFirehoseDataDir, $firehoseDir, $firehoseFile, $cancer, $runDate );

            unless( defined( $FullFirehoseFile ) ){
                warn "Strange, \$FullFirehoseFile not defined";
            }
            push @{$firehoseFiles}, $FullFirehoseFile;
            
            # make list of compact filenames for output
            push @explicitFiles, prepareFilenameForGrep( $firehoseFile, $cancer, $runDate ) . ' in ' .
                prepareFilenameForGrep( $firehoseDir, $cancer, $runDate );

            # open and read firehose file only once; putting it in a CTable object 
            # read tabbed file, with caching turned off
            # todo: configure less frequent progress notices to terminal
            # todo: should turn CTable caching on, but cache files in a tmp dir
            # todo: important optimization; since some firehose files are used multiple times, avoid duplicating them in $FirehoseFileMetadata_objects

            # first disable CTable progress messages; sigh, doesn't work
            # Data::CTable->progress_class(0); 
            my $cTable = Data::CTable->new( { _CacheOnRead   => 0 }, $FullFirehoseFile );

            push @{$cTables}, $cTable;

            # union and intersection of cases will need each case list;
            # make input file meta data objects which cache file metadata -- such as 1) case lists, and 2) gene lists -- for cgds files prefixed with $Utilities::dataFilePrefix 
            my $ffmHandle;
            if( $FirehoseXformWorkflow->getOutputFile($sub) =~ /^$Utilities::dataFilePrefix/ ){

                # get a FirehoseFileMetadata object, which provides the set of genes and cases for the Firehose file
                $ffmHandle = FirehoseFileMetadata->new( $firehoseFile, $FullFirehoseFile, $cTable );
                # for some Firehose files, the set of genes and cases is not meaningful, and undef is returned
                if( defined( $ffmHandle )){
                    push @{ $FirehoseFileMetadata_objects }, $ffmHandle;

	                # num cases and genes for the last FirehoseFileMetadata object
	                # todo: when a sub uses multiple input files num cases and genes are set to last file that has a FirehoseFileMetadata;
	                # is that right? no
	                $numCases = $ffmHandle->numCases();
	                $numGenes = $ffmHandle->numGenes();
                }
                
            }
        }
        
        my $FirehoseDirAndFiles =  englishList( @explicitFiles );            

        print "create_data_and_meta_files: creating ", $FirehoseXformWorkflow->getOutputFile($sub), " from ", $FirehoseDirAndFiles, ".\n";
        my $cgdsFile = File::Spec->catfile( $CancersCGDSinputDir, $FirehoseXformWorkflow->getOutputFile($sub) ); 

        # $codeForCGDS passed because create_data_mRNA_median_Zscores is a java program, and needs to get the CGDS libs
        # $argForMakingWhiteList contains argument from Firehose Transformation Workflow table (q-value, percent threshold)
        # so that create_mutation_white_list can get % of cases with gene mutated
        my $argForMakingWhiteList;
        if( defined( $FirehoseXformWorkflow->getArgs($sub) )){
            $argForMakingWhiteList = $FirehoseXformWorkflow->getArgs($sub);
        }
        callCGDSsub( $createDataFiles, $sub, $globalHash, $firehoseFiles, $cTables, $cgdsFile, $codeForCGDS, $argForMakingWhiteList ); 
        print "create_data_and_meta_files: wrote: ", $cgdsFile, "\n";
        
        # create meta file to describe each 'data' file
        # TODO: pass more information; needs ffm object for (each) file
        if( $FirehoseXformWorkflow->getOutputFile($sub) =~ /^$Utilities::dataFilePrefix/ ){
            createMetaFile( $cancer, $CancersCGDSinputDir, $FirehoseXformWorkflow->getOutputFile($sub), $numCases, $numGenes );
        }
    }
    return $FirehoseFileMetadata_objects;
}

# vector to a sub to transform the Firehose file into a CGDS input file
sub callCGDSsub{
    my( $createDataFiles, $subroutine, $globalHash, $firehoseFiles, $cTables, $CGDSfile, @args ) = @_;

    # todo: for security, vector to an entry in a dispatch table, not an arbitrary string
    $createDataFiles->$subroutine( $globalHash, $firehoseFiles, $cTables, $CGDSfile, @args );
}

########################
# DATA PROCESSING SUBS

# CREATE META FILE for a given data type
# meta_* files contain, e.g. in meta_mRNA_median.txt; see $Utilities::metaFilePrefix
# cancer_study_identifier: ova
# stable_id: ova_mrna_median
# profile_name: mRNA and microRNA expression (all genes)
# profile_description: mRNA and microRNA expression values, median values from all 3 mRNA expression platforms, or value from the microRNA platform (log2 transformed, normalized using AgiMicroRna package in R)
# genetic_alteration_type: MRNA_EXPRESSION
# show_profile_in_analysis_tab: true
sub createMetaFile{
    my( $cancer, $CancersCGDSinputDir, $dataFilename, $cases, $genes ) = @_;
    
    # this hash maps from datatype to default meta file property values
    # tags <genes>, <cases> and <cancer> are replaced with the corresponding info
    # stable_id is the unique identifier that maps the data to a collection called a Profile in the database; we name it <cancer>_'genetic_alteration_type'
    # thus, we can map multiple data files to a single profile by giving them the same 'genetic_alteration_type', as we do here for mRNA_median and miRNA
    my $metaFileData = {
        'expression_median' => {
            'stable_id'                    => '<cancer>_mrna',  # todo: change to _rna; might work
            'genetic_alteration_type'      => 'MRNA_EXPRESSION',
            'show_profile_in_analysis_tab' => 'false',
            'profile_description'          => 'Expression levels for <genes> genes in <cases> <cancer> cases',
            'profile_name'                 => 'mRNA and microRNA expression (all genes)'
        },
        'mRNA_median_Zscores' => {
            'stable_id'                    => '<cancer>_mrna_median_Zscores',
            'genetic_alteration_type'      => 'MRNA_EXPRESSION',
            'show_profile_in_analysis_tab' => 'true',
            'profile_description'          => 'mRNA z-Scores compared to the expression distribution of each gene in diploid tumors.',
            'profile_name'                 => 'mRNA Expression z-Scores'
        },
        'mutations_extended' => {
            'stable_id'               => '<cancer>_mutations',
            'genetic_alteration_type' => 'MUTATION_EXTENDED',
            'profile_description'     => 'Mutation data from whole exome sequencing.',
            'profile_name'            => 'Mutations',
            'show_profile_in_analysis_tab' => 'true',
        },
        'CNA' => {
            'stable_id'               => '<cancer>_gistic',
            'genetic_alteration_type' => 'COPY_NUMBER_ALTERATION',
            'profile_description'     =>
                'Putative copy-number calls on <cases> cases determined using GISTIC 2.0. Values: -2 = homozygous deletion; -1 = hemizygous deletion; 0 = neutral / no change; 1 = gain; 2 = high level amplification.',
            'profile_name' => 'Putative copy-number alterations from GISTIC',
            'show_profile_in_analysis_tab' => 'true',
        },
        'methylation' => {
            'stable_id'                     => '<cancer>_methylation',
            'genetic_alteration_type'      => 'METHYLATION',
            'show_profile_in_analysis_tab' => 'false',
            'profile_description'          => 'Methylation beta-values for genes in <cases> cases. For genes with multiple methylation probes, the probe least correlated with expression.',
            'profile_name'                 => 'Methylation'
        },

    };

    my $metaFilename = $dataFilename;
    $metaFilename =~ s/$Utilities::dataFilePrefix/$Utilities::metaFilePrefix/; 

    my $key = $dataFilename;
    $key =~ s/$Utilities::dataFilePrefix//;  
    $key =~ s/\.txt//;
    
    my $metaProperties = $metaFileData->{ $key };
    unless( defined( $metaProperties ) ){
        warn "Error: No properties for type '$key'";
        return;
    }
    my $fileContent = 'cancer_study_identifier: ' . $cancer . "\n";
    foreach my $prop (keys %{$metaProperties}){
        $fileContent .= "$prop: " . $metaProperties -> {$prop} . "\n";
    }
    $fileContent =~ s/<cancer>/$cancer/g;
    $fileContent =~ s/<cases>/$cases/g;
    $fileContent =~ s/<genes>/$genes/g;
    
    print "wrote: ", File::Spec->catfile( $CancersCGDSinputDir, $metaFilename ), "\n";
    #    print "with content: \n$fileContent\n";
    File::Util->new()->write_file( 'file' => File::Spec->catfile( $CancersCGDSinputDir, $metaFilename ), 'content' => $fileContent, 'bitmask' => 0644 );
}

########################
# SUBS TO CREATE CASE LISTS

# TODO: HIGH: UNIT TEST

# given a set of Firehose files, create a corresponding case list file that contains the intersection or union of cases in the firehose files
sub create_many_to_one_case_lists{
    my(
        $FirehoseFileMetadata_objects,              # ref to a list containing a FirehoseFileMetadata object for each firehose file 
        $runSummary,                                # hash summarizing run stats
        $CGDS_case_list_dir,                        # CGDS directory for case list file
        $CGDS_filename,                             # name of CGDS case list file
        $cancer,
        $FirehoseFilesToProcess,                    # ref to list of firehose files which, if available, should have case lists combined;
                                                    # these contain <cancer> and <CANCER> patterns
        $operation,                                 # operation to conduct, intersection or union
        $actual_case_list_properties                # hash from property to value, in which value needs <cancer>, <cases> patterns completed
    ) = @_;

    # hash from firehose file to FirehoseFileMetadata objects
    my %file_to_FirehoseFileMetadata_object;
    foreach my $ffmo (@{$FirehoseFileMetadata_objects}){
        $file_to_FirehoseFileMetadata_object{ $ffmo->getFilename() } = $ffmo;
    }

    # get files available in firehose files $FirehoseFileMetadata_objects
    my @FirehoseFileMetadata_objects_of_interest;
    foreach my $possibleFirehoseFile (@{$FirehoseFilesToProcess}){
         
        $possibleFirehoseFile =~ s/<cancer>/$cancer/;
        # substitute <CANCER> with this cancer, uppercased 
        my $cancerUC = uc( $cancer );
        $possibleFirehoseFile =~ s/<CANCER>/$cancerUC/g;
        
        if( exists( $file_to_FirehoseFileMetadata_object{ $possibleFirehoseFile} ) ){
            push @FirehoseFileMetadata_objects_of_interest, $file_to_FirehoseFileMetadata_object{ $possibleFirehoseFile}; 
            # print "Found $possibleFirehoseFile\n";
        }
    }    

    # perform set operation on their (converted) case lists
    my @cases;
    # union of case lists
    if( $operation eq 'union'){
        @cases = sort( FirehoseFileMetadata::union_of_case_lists( @FirehoseFileMetadata_objects_of_interest) );
    }

    # intersection of case lists
    if( $operation eq 'intersection'){
        # for intersections, all desired Firehose files must be present
        if( scalar( @FirehoseFileMetadata_objects_of_interest ) < scalar( @{ $FirehoseFilesToProcess }) ){
            return;
        }
        @cases = sort( FirehoseFileMetadata::intersection_of_case_lists( @FirehoseFileMetadata_objects_of_interest) );
    }
    
    my $numCases = scalar( @cases );
    
    # summary: enter size of case list
    $runSummary->{$cancer}->{$CGDS_filename} = $numCases;
    
    # don't make empty case lists
    if( 0 == $numCases){
        return;
    }

    # replace patterns on the properties
    my $fileContent;
    
    foreach my $prop (keys %{$actual_case_list_properties}){
        $fileContent .= "$prop: " . $actual_case_list_properties->{$prop} . "\n";
    }
    
    $fileContent =~ s/<cancer>/$cancer/g;   
    # <cases> is the number of matching cases
    $fileContent =~ s/<cases>/$numCases/g;
    
    $fileContent .= "case_list_ids: " . join( "\t", @cases ) . "\n";

    # create the CGDS filename
    my $CGDSfile = File::Spec->catfile( $CGDS_case_list_dir, $CGDS_filename );
    
    # write the file
    File::Util->new()->write_file( 'file' => $CGDSfile, 'content' => $fileContent, 'bitmask' => 0644 );
    print "wrote: $CGDSfile\n";
}

# create a set of case list files
# 
# each potential list file is described by an entry in $case_list_FileProperties
# create those lists for which data is available for the given cancer
# each list is based on a single Firehose data file
sub create_one_to_one_case_lists{
    my(
        $FirehoseFileMetadata_objects,          # ref to a list containing a FirehoseFileMetadata object for most firehose files 
        $runSummary,                            # hash summarizing run stats
        $CGDS_case_list_dir,                    # CGDS directory for case list files
        $cancer,
        $case_list_FileProperties,              # a hash that maps from potential case list file to its properties
        $actual_case_list_properties            # properties that belong in the case list files
    ) = @_;
    
    # make hash from Firehose filename to Firehose MetaData object
    my $MetaData_objects = {};
    # print "Firehose files:\n";
    foreach my $ffmo (@{$FirehoseFileMetadata_objects}){
        $MetaData_objects->{ $ffmo->getFilename() } = $ffmo;
        # print $ffmo->getFilename(), "\n";
    } 
    
    my $fileUtil = File::Util->new();

    # foreach potential case list file
    foreach my $potential_case_list_file (keys %{$case_list_FileProperties}){
        
        # print "\$potential_case_list_file: $potential_case_list_file\n";
        my $case_list_props = $case_list_FileProperties->{$potential_case_list_file}; 

        # take FirehoseFile, do substitution, look for it in Firehose MetaData object hash
        my $FirehoseFile = $case_list_props->{FirehoseFile};
        $FirehoseFile =~ s/<cancer>/$cancer/;
        # substitute <CANCER> with this cancer, uppercased 
        my $cancerUC = uc( $cancer );
        $FirehoseFile =~ s/<CANCER>/$cancerUC/g;
        
        # print "Looking for firehose file: $FirehoseFile\n";

        if( exists( $MetaData_objects->{$FirehoseFile}) ){
            # if found, create CGDS case list file (key is filename) according to case_list_FileProperties
            
            # get cases from the FirehoseFileMetadata_object
            my @cases = @{ $MetaData_objects->{$FirehoseFile}->cases() };
            
            # apply xformFunc, if any, from $case_list_FileProperties
            if( defined( $case_list_props->{xformFunc} )){  
                # print "trying to write $potential_case_list_file\n"; 
                my $xformFunc = $case_list_props->{xformFunc};
                @cases = grep( {&$xformFunc( $_ )} @cases ); # to filter case list calls CASE-IDs sub that identifies proper cases 
            }
            
            # convert cases to simplified IDs
            @cases = map {convertCaseID( $_ )} @cases;
            
            # eliminate duplicate case-IDs
            # (there's at least one duplicate case-ID: TCGA-06-0168-01A-01R and TCGA-06-0168-01A-02R appear in GBM.medianexp.txt. )
            my %seen = ();
            my @uniqueCases;
            foreach my $c (@cases){
                if( exists($seen{$c}) ){
                    warn "Duplicate case ID: $c.\n";
                }else{
                    push @uniqueCases, $c;
                    $seen{$c}++;
                }
            }
            @cases = @uniqueCases;
            
            my $numCases = scalar( @cases );
            
            # summary: enter size of case list
            $runSummary->{$cancer}->{$potential_case_list_file} = $numCases;
            
            # don't make 0-length lists
            if( 0 == $numCases ){
                next;
            }

            # replace patterns on the properties
            my $fileContent;

            foreach my $prop (keys %{$case_list_props}){
                if( grep( /$prop/, @{$actual_case_list_properties} ) ){
                    $fileContent .= "$prop: " . $case_list_props->{$prop} . "\n";
                }
            }

            $fileContent =~ s/<cancer>/$cancer/g;
            # <cases> is the number of matching cases
            $fileContent =~ s/<cases>/$numCases/g;

            $fileContent .= "case_list_ids: " . join( "\t", @cases ) . "\n";

            # create the CGDS filename
            my $CGDSfile = File::Spec->catfile( $CGDS_case_list_dir, $potential_case_list_file );

            # write the file
            $fileUtil->write_file( 'file' => $CGDSfile, 'content' => $fileContent, 'bitmask' => 0644 );
            print "wrote: $CGDSfile\n";
        }
    }
}

########################
# create cancer-type name file
# cancer-type name files contain, e.g. in ovarian.txt: 
# cancer_type_id: ova
# cancer_study_identifier:  ova
# name:  Serous Ovarian Cancer (TCGA)
# description:  <a href="http://cancergenome.nih.gov/">The Cancer Genome Atlas (TCGA)</a> Serous Ovarian Cancer project. 489 cases.<br> <i>Manuscript under review.</i> <a href="http://tcga-data.nci.nih.gov/tcga/">Raw data via the TCGA Data Portal</a>.
sub createCancerTypeNameFile{
    my( $FirehoseFileMetadata_objects, $cancer, $name, $CancersCGDSinputDir ) = @_;
    
    # do not produce a <cancer>.txt file for cancers that don't have cgds data_* files
    unless( defined( $FirehoseFileMetadata_objects) && 0 < scalar( @{$FirehoseFileMetadata_objects}) ){
        # todo: this test should also examine the zScore data
        print( "No CGDS input data for $cancer.\n");
        return;
    }
    
    my $fileContent = "type_of_cancer: " . $cancer . "\n"; 
    $fileContent .= "cancer_study_identifier: " . $cancer . "\n";
    $fileContent .= 'name: ';
    if( defined( $name ) ){
        $fileContent .= $name . "\n"; 
        
        # TCGA<full cancer name here>.<# of samples>   samples. 
        my @cases = FirehoseFileMetadata::union_of_case_lists( @{$FirehoseFileMetadata_objects});
        my $cases = scalar( @cases );
        my $url = "\"http://tcga-data.nci.nih.gov/tcga/tcgaCancerDetails.jsp?diseaseType=" . uc( $cancer ) . "&diseaseName=$name\""; 
        $fileContent .= "description: TCGA $name, containing $cases samples; raw data at the <A HREF=$url>NCI</A>.\n";
    }else{
        $fileContent .= 'TBD' . "\n"; 
        $fileContent .= "description: TBD\n";
    }
    
    my $filename = File::Spec->catfile( $CancersCGDSinputDir, $cancer . '.txt' );
    # write with permissions -rw-r--r--
    File::Util->new()->write_file( 'file' => $filename, 'content' => $fileContent, 'bitmask' => 0644 );
    print "wrote: $filename\n";
}

sub clean_up{
	my(
        $runSummary,                                # hash summarizing run stats
        $Summary                                    # filename of summary 
    ) = @_; 
    
    # write summary table
    my $t = hashToFile( $runSummary, 'Cancers', $Summary );
    print "Writing summary to '$Summary'.\n";
    $t->out();
}

1;
