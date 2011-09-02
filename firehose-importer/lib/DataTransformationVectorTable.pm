package DataTransformationVectorTable;

require Exporter;
@ISA = qw(Exporter);
@EXPORT = qw( parseTransformationVectorTable prepareFilenameForGrep getLastestVersionOfFile listCancers ); 

use strict;
use warnings;
use File::Util;
use Data::Dumper;
use YAML; # would prefer to use YAML:XS, but it doesn't run

use Utilities;

# todo: perl docs
# todo: consistently use underscore prefixes
# author: Arthur Goldberg, goldberg@cbio.mskcc.org

sub new {
    my( $class, $dataTransformationVectorFile ) = @_;

    unless( -r  $dataTransformationVectorFile ){
        my($package, $filename, $line, $subr)= caller(0);
        warn "$package\:\:$subr: Cannot read DataTransformationVectorTable file: '$dataTransformationVectorFile'.\n";
        return undef;
    }
    my $transformationVectorTable = File::Util->new()->load_file( $dataTransformationVectorFile );
    
    my $self = _parseTransformationVectorTable( $transformationVectorTable );
    bless( $self, $class );
    return $self;
}

# returns a ref to a hash from firehose to cgds transformation subroutine
sub _parseTransformationVectorTable{
    my( $transformationVectorTable ) = @_;  
    
    my $utilities = Utilities->new( "" );

    # remove comments
    $transformationVectorTable =~ s/#.*\n//g;
    
    # get entries
    my @transformationVectorTableEntries = split( /;/, $transformationVectorTable );
    # remove empty entries
    @transformationVectorTableEntries = grep( length, @transformationVectorTableEntries );
    
    # get records of the form: subroutine (FirehoseDirectory FirehoseFile)+ CGDSfile
    my $self = {};
    
    # maintain sequence of subroutines from the transformation file
    $self->{_SEQUENCE_OF_SUBROUTINES} = [];
    
    # put all transformation info in a hash keyed by __FirehoseToCGDSmap
    $self->{__FirehoseToCGDSmap} = {};
    my $transformationHash = $self->{__FirehoseToCGDSmap};

    foreach my $entry (@transformationVectorTableEntries){

        # remove leading & trailing ws
        $entry =~ s/^\s*//;
        $entry =~ s/\s*$//;
        if( $entry eq '' ){
        	next;
        }

        my @fields = split( /\s+/, $entry );
        # remove empty fields
        @fields = grep( length, @fields );
        
        unless( 4 <= scalar( @fields )){ 
        	$utilities->quit( "Entry '$entry' in DATA TRANSFORMATION VECTOR TABLE not of the form 'subroutine (FirehoseDirectory FirehoseFile)+ CGDSfile'" );
        }
        
        my $sub = shift @fields;
        
        push @{$self->{_SEQUENCE_OF_SUBROUTINES}}, $sub;
        
        my $dirsAndFiles = [];
        while( 2 < scalar( @fields ) ){
        	push @{$dirsAndFiles}, [ shift @fields, shift @fields ];
        }
        $transformationHash->{$sub}->{DirectoriesAndFiles} = $dirsAndFiles;
        $transformationHash->{$sub}->{CGDS_data_file} = shift @fields;
        my $extra_argument = shift @fields;
        if( defined( $extra_argument )){
            $transformationHash->{$sub}->{extra_argument} = $extra_argument;
        }
    }
    return $self;
}

# get the subroutines
# return the sequence of subroutines in the same order they appear in the file
sub getSubroutines{
    my( $self ) = @_;
    return @{$self->{_SEQUENCE_OF_SUBROUTINES}};
}

# sets the home directory for the data
sub _setHomeDirectory{
    my( $self, $homeDir ) = @_;
    $self->{HOME_DIRECTORY} = $homeDir;
    
}

# return the directory and file pairs
# returns a ref to a list of refs to pairs of the form [directory, file]
sub getFirehoseDirectoriesAndFiles{
    my($self, $sub) = @_;
    return $self->_getField( $sub, 'DirectoriesAndFiles' );
}

# get the components
sub getCGDS_data_file{
    my($self, $sub) = @_;
    return $self->_getField( $sub, 'CGDS_data_file' );
}

# get the extra argument
sub getCGDS_extra_argument{
    my($self, $sub) = @_;
    return $self->_getField( $sub, 'extra_argument' );
}

sub _getField{
    my($self, $sub, $field ) = @_;
    if( defined( $sub ) && exists( $self->{__FirehoseToCGDSmap} ) && 
        exists( $self->{__FirehoseToCGDSmap}->{$sub} ) ){
            return $self->{__FirehoseToCGDSmap}->{$sub}->{$field};
        };
    return undef;
}

# prepare a directory or file name from the DATA TRANSFORMATION VECTOR TABLE
# for search with grep on stored files
sub prepareFilenameForGrep{
    my( $filename, $cancer, $runDate ) = @_;

    # components in <>, such as <cancer>, <date> and <version> indicate varying substrings
    # substitute <cancer> with this cancer
    $filename =~ s/<cancer>/$cancer/g;

    # substitute <CANCER> with this cancer, uppercased 
    my $cancerUC = uc( $cancer );
    $filename =~ s/<CANCER>/$cancerUC/g;        
    
    # substitute <date> with date of this Firehose run
    if( defined( $runDate )){
        $filename =~ s/<date>/$runDate/g;  
    }else{
        $filename =~ s/<date>/.*/g;  
    }
    
    # substitute <version>s with RE any string
    $filename =~ s/<version>/.*/g;
    return $filename;
}

# if a unique file of roughly the form 
# catfile( $CancersFirehoseDataDir, $directoryNamePattern, $cancer, $runDate, $fileNamePattern ) 
# exists, then return the full filename; otherwise return undef
# 
# match patterns as needed in the input
# 
# some data types are provided in multiple versions, e.g.: Merge_mirna
# since multiple versions is a bug, just use last version 
# 
# params:
# $CancersFirehoseDataDir: directory storing all Firehose data
# $directoryNamePattern: pattern, from data_transformation_vector_table.conf, that describes the directory containing the file for which we seek the latest version
# $fileNamePattern: pattern, also from data_transformation_vector_table.conf, that describes the file for which we seek the latest version
# $cancer: the name of the directory containing data for the particular cancer
# $runDate: the date on which the Broad processed the data
sub getLastestVersionOfFile{
	my( $CancersFirehoseDataDir, $directoryNamePattern, $fileNamePattern, $cancer, $runDate, ) =@_;

	my $fileUtil = File::Util->new( );	
	my @allDirs = $fileUtil->list_dir( $CancersFirehoseDataDir, '--dirs-only', '--no-fsdots' );          
	my $FirehoseDir = prepareFilenameForGrep( $directoryNamePattern, $cancer, $runDate );
	my @versions = grep( m|$FirehoseDir|, @allDirs );
	# some data types are provided in multiple versions, e.g.: Merge_mirna
	# since multiple versions is a bug, just use last version 
	# todo: need to pick latest version if versions stay; remove this when the multiple versions bug is fixed
	my $someDirectoryVersion = pop @versions;

	# if the Firehose directory exists look for the file
	my $FullFirehoseFile;
	unless( defined( $someDirectoryVersion )){
		return undef;
	}
	
	# get the file in the directory
	my @allFiles = $fileUtil->list_dir( 
	    File::Spec->catfile( $CancersFirehoseDataDir, $someDirectoryVersion ), '--files-only', '--recurse' );
	my $FirehoseFile = prepareFilenameForGrep( $fileNamePattern, $cancer, $runDate );
	my @files = grep( m|$FirehoseFile|, @allFiles );
	# give up if there isn't a unique firehose file
	if( scalar(@files) == 0 ){
	    warn "Cannot find Firehose file $FirehoseFile in $someDirectoryVersion";
        return undef;
	}
	if( 1 < scalar(@files) ){
	    warn "Error: Found multiple Firehose $FirehoseFile files in $someDirectoryVersion";
	    warn "Found:\n", join( "\n", @files), "\n";
        return undef;
	}
	
	$FullFirehoseFile = pop @files;
	unless( -r $FullFirehoseFile ){
	    warn "Cannot read Firehose file $FullFirehoseFile";
        return undef;
	}
	return $FullFirehoseFile;
}

# get list of cancers from cancers file, which is ':' separated
sub listCancers{
    my( $CancersFilename ) = @_;

    my @cancers;
    my @lines = File::Util->new()->load_file( $CancersFilename, '--as-lines');
    foreach my $line (@lines){
        my( $cancer ) = split( /\s*:\s*/, $line );
        push @cancers, $cancer;
    }
    return @cancers;

    # todo: enable comments in $CancersFilename
}

1;