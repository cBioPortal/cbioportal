package FirehoseTransformationWorkflow;

require Exporter;
@ISA = qw(Exporter);
@EXPORT = qw( next_sub getLastestVersionOfFile prepareFilenameForGrep 
    getFirehoseDirectoriesAndFiles get_subroutine_sequence reset ); 

use strict;
use warnings;
use File::Util;
use Data::Dumper;
use YAML qw( Load LoadFile ); # todo: would prefer to use YAML:XS, but it doesn't run

use Utilities;

# SUMMARY
# Workflow dependencies of the conversion process (FirehoseTransformationWorkflow.pm)
# Interprets a config file (in YAML) that describes
# 1) list of subroutines to be executed, patterns describing their inputs directories and files, and the output file, and
# 2) interdependencies between the subroutines
# next_sub provides sequence of subroutines to analyze in a cancer
# uses patterns and actual files to find Firehose input files and indicated whether subroutines should be called
# Issues: Workaround of YAML bug in _test_parse_workflow
# Would prefer to use YAML:XS, but it doesn't run
# Testing: Full coverage

# encapsulates the workflow dependencies of the Firehose conversion process.
# reads a configuration file (in YAML format) that describes subroutines to be executed,
# their inputs and output, and interdependencies between the subroutines

# dependency rules take the form
# sub2 requires sub1    # unless sub1 is executed sub2 cannot be executed; sub1 must preceed sub2
# sub2 precluded_by sub1 # if sub1 is executed then sub2 cannot be executed; sub1 must preceed sub2
# Subject to these dependency rules, next_sub returns subroutines to be executed in the sequence 
# that they appear in the configuration file.

# author: Arthur Goldberg, goldberg@cbio.mskcc.org
sub new {
    my( $class, $workflow_file ) = @_;
    
    my( undef, undef, undef, $subr)= caller(0);
    unless( -r $workflow_file ){
        warn "$subr: Error: Cannot read FirehoseTransformationWorkflow file: '$workflow_file'.";
        return undef;
    }
    
    my $self = _parse_workflow( LoadFile( $workflow_file ) );
    unless( defined( $self ) ){
    	return undef;
    }
 
    bless( $self, $class );
    return $self;
}

# find the next subroutine which 
# 1) meets the dependency criteria, and 
# 2) has the necessary files
# return undef if no such subroutine exists
# also mark the subroutine as having been executed
#
# $rootDir is the pathname of the directory which may contain the directories and files needed 
# @args_for_file_processing are arguments needed for prepareFilenameForGrep 
sub next_sub{
	my( $self, $rootDir, @args_for_file_processing ) = @_;
	
	# get next subroutine
	while( 1 ){
		my $next_sub = $self->_next_consistent_with_dependencies();
		unless( defined( $next_sub ) ){
			return undef;
		}
		if( $self->_required_files_exist( $next_sub, $rootDir, @args_for_file_processing ) ){

            # mark the subroutine as having been executed
            $self->__was_executed( $next_sub );
			return $next_sub;
		}
	}
}

# reset the sequence of subroutines, so that next_sub can be called from the beginning again,
# i.e., that all of a cancer's data can be processed again
sub reset{
    my( $self ) = @_;

    $self->{EXECUTED} = {};         # hash of subroutines that have been executed
    $self->{INDEX_OF_NEXT} = 0;     # index of next subroutine
}

# return true if the data set contains the
# files required to execute $sub; otherwise return undef 
#
# $rootDir: the pathname of the directory which may contain the directories and files needed
sub _required_files_exist{
	my( $self, $sub, $rootDir, @args_for_file_processing ) = @_;
	
	my $FirehoseDirectoriesAndFiles = $self->getFirehoseDirectoriesAndFiles($sub);
	foreach my $dirAndFile ( @{$FirehoseDirectoriesAndFiles} ) {
		
		my( $firehoseDir, $firehoseFile ) = @{$dirAndFile};
		my $FullFirehoseFile = getLastestVersionOfFile( $rootDir, $firehoseDir, $firehoseFile, @args_for_file_processing );
		  
		unless( defined( $FullFirehoseFile ) ){
		    return undef;
		}
	}
	return 1;
}

# if a unique file of roughly the form catfile( $rootDir, $directoryNamePattern, $fileNamePattern )
# exists, then return the full filename; otherwise return undef
# 
# match patterns as needed in the input
# 
# some data types are provided in multiple versions, e.g.: Merge_mirna
# use last version 
# 
# params:
# $rootDir: the pathname of the directory which may contain the directories and files needed
# $directoryNamePattern: pattern, from FirehoseTransformationWorkflow file, that describes the directory containing the file for which we seek the latest version
# $fileNamePattern: pattern, also from FirehoseTransformationWorkflow file, that describes the file for which we seek the latest version
sub getLastestVersionOfFile{
    my( $rootDir, $directoryNamePattern, $fileNamePattern, @args_for_file_processing ) = @_;

    my $fileUtil = File::Util->new( );  
    my @allDirs = $fileUtil->list_dir( $rootDir, '--dirs-only', '--no-fsdots' );
    my $FirehoseDir = prepareFilenameForGrep( $directoryNamePattern, @args_for_file_processing );
    my @versions = grep( m|$FirehoseDir|, @allDirs );

    # some data types are provided in multiple versions, e.g.: Merge_mirna
    # use last version 
    my $someDirectoryVersion = pop @versions;

    # if the Firehose directory exists look for the file
    my $FullFirehoseFile;
    unless( defined( $someDirectoryVersion )){
        return undef;
    }
    
    # get the file in the directory
    my @allFiles = $fileUtil->list_dir( 
        File::Spec->catfile( $rootDir, $someDirectoryVersion ), '--files-only', '--recurse' );
    my $FirehoseFile = prepareFilenameForGrep( $fileNamePattern, @args_for_file_processing );
    my @files = grep( m|$FirehoseFile|, @allFiles );

    # give up if there isn't a unique file
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

# prepare a directory or file name from the FirehoseTransformationWorkflow file
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

# return the directory, file and argument triples (argument optional)
# returns a ref to a list of refs to triples of the form [directory, file, argument]
sub getFirehoseDirectoriesAndFiles{
    my($self, $sub) = @_;
    return $self->_getField( $sub, 'DIRS_AND_FILES' );
}

# return the output filename
# to support igv linking, we now
# may output files of type <CANCER>.seg
sub getOutputFile{
    my($self, $sub, $cancer) = @_;
	my $outputFile = $self->_getField( $sub, 'OUTPUT_FILE' );
    # substitute <CANCER> with this cancer
	$outputFile =~ s/<CANCER>/$cancer/g;
    return $outputFile;
}

# return the (optional) arguments
sub getArgs{
    my($self, $sub) = @_;
    return $self->_getField( $sub, 'ARGUMENTS' );
}

# given a $sub and $field, get the data structure provided in that $field for that $sub
# if the field isn't instantiated, return undef
sub _getField{
    my($self, $sub, $field ) = @_;
    unless( defined( $sub ) and defined( $field ) and
        # all exists tests required, as per http://perldoc.perl.org/functions/exists.html 
        exists( $self->{HASH_OF_SUBROUTINES} ) and
        exists( $self->{HASH_OF_SUBROUTINES}->{$sub} ) and 
        exists( $self->{HASH_OF_SUBROUTINES}->{$sub}->{$field} ) ){
        	return undef;
        }
    return $self->{HASH_OF_SUBROUTINES}->{$sub}->{$field};
}

# return the sequence of all subroutines
sub get_subroutine_sequence{
    my($self) = @_;
	return @{$self->{SEQUENCE_OF_SUBROUTINES}};
}

# like a new, but for testing
sub _test_parse_workflow{
	my( $class, $yaml_string ) = @_;
    # bug in YAML; different results from Load and LoadFile!
    # todo: report this
    my $f = '/tmp/test.yaml';
    File::Util->new()->write_file( 'file' => $f, 'content' => $yaml_string, 'bitmask' => 0644 );
    my( $subroutines, $dependencies ) = LoadFile( $f );
    my $self = _parse_workflow( $subroutines, $dependencies );
}

# for some reason cannot call this (the bless() part) with the sub test_errors in FirehoseTransformationWorkflow.t
# todo: figure that out, and combine this with _test_parse_workflow 
sub _test_parse_workflow_new{
    my( $class, $yaml_string ) = @_;
    my $self = _test_parse_workflow( $class, $yaml_string );
    unless( defined( $self ) ){
        return undef;
    }
    bless( $self, $class );
    return $self;
}

sub _parse_workflow{
    my( $subroutines, $dependencies ) = @_;

    my $self = {};
    $self->{HASH_OF_SUBROUTINES} = {};          # properties of each subroutine
    $self->{SEQUENCE_OF_SUBROUTINES} = [];      # list of the subroutines in order
    $self->{SUBROUTINE_NUMBER} = {};            # hash from name to index in sequence
    $self->{DEPENDENCIES} = {};     # hash of dependencies
    FirehoseTransformationWorkflow::reset( $self );     # resets execution list and INDEX

    # $self->{DEPENDENCIES}->{REQUIRES}->{sub2}->{sub1}   iff sub2 requires sub1   
    $self->{DEPENDENCIES}->{REQUIRES} = {};     

    # $self->{DEPENDENCIES}->{PRECLUDED_BY}->{sub2}->{sub1}   iff sub2 precluded_by sub1
    $self->{DEPENDENCIES}->{PRECLUDED_BY} = {};     # hash of dependencies
    
    # parse the subroutines
    foreach my $subroutineConf (@{$subroutines}){
    	unless( _load_subroutine( $self, $subroutineConf ) ){
    		return undef;
    	}
    } 

    foreach my $dependency (@{$dependencies}){
        unless( _load_dependency( $self, $dependency ) ){
            return undef;
        }
    }
    return $self;
}

# load a subroutine's configuration from the DS obtained by YAML
# reports many kinds of errors
# return undef if the DS contains errors
# else returns true
sub _load_subroutine{
	my( $self, $subroutineConf ) = @_;

	my( $sub ) = keys %{$subroutineConf};
    my $thisSub = $subroutineConf->{$sub};

    my(undef, undef, undef, $subr)= caller(0);
    
    if( exists( $self->{HASH_OF_SUBROUTINES}->{$sub} ) ){
        warn "$subr: Error: duplicate of subroutine '$sub'";
        return undef;
    }

    push @{$self->{SEQUENCE_OF_SUBROUTINES}}, $sub;
    $self->{SUBROUTINE_NUMBER}->{$sub} = scalar( @{$self->{SEQUENCE_OF_SUBROUTINES}} ) - 1;
    $self->{HASH_OF_SUBROUTINES}->{$sub} = {};
    my $this_node_in_self = $self->{HASH_OF_SUBROUTINES}->{$sub};
    $this_node_in_self->{DIRS_AND_FILES} = [];

    unless( exists( $thisSub->{outfile} ) and ref( \$thisSub->{outfile} ) eq 'SCALAR' ){
        warn "$subr: Error: 'outfile' not a defined scalar for subroutine '$sub'";
    	return undef;
    }
    $this_node_in_self->{OUTPUT_FILE} = $subroutineConf->{$sub}->{outfile};
    
    unless( exists( $thisSub->{dirsAndFiles} ) and ref( $thisSub->{dirsAndFiles} ) eq 'ARRAY' ){
        warn "$subr: Error: 'dirsAndFiles' not a defined list for subroutine '$sub'";
        return undef;
    }
    
    for my $dirAndFile (@{$thisSub->{dirsAndFiles}}){
    	if( defined( $dirAndFile ) and ref \$dirAndFile eq 'SCALAR' ){
            my( $dir, $file, $rest ) = split( / +/, $dirAndFile ); 
            if( defined( $dir ) and defined( $file ) and !defined($rest) ){
            	push @{$this_node_in_self->{DIRS_AND_FILES}}, [$dir, $file ];
            	next;
            }
	        warn "$subr: Error: '$dirAndFile' in 'dirsAndFiles' not a pair of strings for subroutine '$sub'";
	        return undef;
    		
    	}
		warn "$subr: Error: 'dirsAndFiles' contains an undefined string for subroutine '$sub'";
		return undef;
    }

    if( exists( $subroutineConf->{$sub}->{arguments} ) ){
    	unless( ref( \$subroutineConf->{$sub}->{arguments} ) eq 'SCALAR' ){
	        warn "$subr: Error: 'arguments' not a string for subroutine '$sub'";
	        return undef;
    	}
        $this_node_in_self->{ARGUMENTS} = $subroutineConf->{$sub}->{arguments};
    }

    return 1;
}

# load a dependency
# either 
# sub2 requires sub1
# or
# sub2 precluded_by sub1
sub _load_dependency{
	my( $self, $dependency ) = @_;
    my(undef, undef, undef, $subr) = caller(0);

	# three fields?
    unless( defined( $dependency ) and ref \$dependency eq 'SCALAR' ){
        warn "$subr: Error: dependencies not a defined scalar";
    	return undef;
    }
	
	my( $sub2, $relationship, $sub1, $rest ) = split( /\s+/, $dependency );
	unless( defined( $sub2) and defined( $relationship ) and defined( $sub1 ) and 
	   !defined( $rest ) ){
	        warn "$subr: Error: dependencies '$dependency' is not 3 fields";
	        return undef;
	   }

	# valid keyword?
	if( ($relationship !~ m/^requires$/) and ($relationship !~ m/^precluded_by$/) ){
        warn "$subr: Error: dependencies '$dependency' does not use 'requires' or 'precluded_by'";
		return undef;
	}
	
    # known subroutines
    foreach my $sub ($sub1, $sub2){
    	unless( exists( $self->{HASH_OF_SUBROUTINES}->{$sub} ) ){
	        warn "$subr: Error: dependencies '$dependency' has undefined subroutine '$sub'";
	        return undef;
    	}
    }
    
    # sub2 must follow sub1
	unless( $self->{SUBROUTINE_NUMBER}->{$sub1} < $self->{SUBROUTINE_NUMBER}->{$sub2} ){
		warn "$subr: Error: in dependency '$dependency'; subroutine '$sub1' does not preceed '$sub2' in the workflow";
		return undef;
	}

    if( $relationship =~ m/^precluded_by$/ ){
    	_createPrecludedBy( $self, $sub2, $sub1 );
        return 1;
    }
    
    if( $relationship =~ m/^requires$/ ){
    	_createRequires( $self, $sub2, $sub1 );
    	return 1;
    }
    
    # unreachable
	return 'unreachable';
}

# sub2 requires sub1    # unless sub1 is executed sub2 cannot be executed; sub1 must preceed sub2
sub _createRequires{
    my( $self, $sub2, $sub1 ) = @_;
    $self->{DEPENDENCIES}->{REQUIRES}->{$sub2}->{$sub1} = 1;
}


# sub2 precluded_by sub1 # if sub1 is executed then sub2 cannot be executed; sub1 must preceed sub2
sub _createPrecludedBy{
    my( $self, $sub2, $sub1 ) = @_;
    $self->{DEPENDENCIES}->{PRECLUDED_BY}->{$sub2}->{$sub1} = 1;   
}

# get the next subroutine in the workflow
# return the sequence of subroutines in the same order they appear in the file, 
# subject to adherence to the dependency rules
sub _next_consistent_with_dependencies{
    my( $self ) = @_;
    my $next_sub = $self->_nextInSeq();
    while( defined( $next_sub )){
    	if( $self->__satisfies_requires( $next_sub ) and $self->__satisfies_precluded_by( $next_sub ) ){
            return $next_sub;
    	}
    	$next_sub = $self->_nextInSeq();
    } 
    return undef;
}

# return true if the $sub satisfies all its requires dependencies, undef otherwise
sub __satisfies_requires{
    my( $self, $next_sub ) = @_;
	foreach my $required ( keys %{$self->{DEPENDENCIES}->{REQUIRES}->{$next_sub}} ){
	    unless( $self->{EXECUTED}->{$required} ){
	        return undef;
	    }
	}
	return 1;
}

# return true if the $sub satisfies all its precluded_by dependencies, undef otherwise
sub __satisfies_precluded_by{
    my( $self, $next_sub ) = @_;
	foreach my $preclusion ( keys %{$self->{DEPENDENCIES}->{PRECLUDED_BY}->{$next_sub}} ){
	    if( $self->{EXECUTED}->{$preclusion} ){
	    	return undef;
	    }
	}
	return 1;
}

# indicate that a sub was executed
sub __was_executed{
    my( $self, $sub ) = @_;
	$self->{EXECUTED}->{$sub} = 1;
}

sub _nextInSeq{
    my( $self ) = @_;
    
    my(undef, undef, undef, $subr) = caller(0);
    if( scalar( @{$self->{SEQUENCE_OF_SUBROUTINES}} ) <= $self->{INDEX_OF_NEXT} ){
        return undef;
    }
    my $i = $self->{INDEX_OF_NEXT}++;
    return $self->{SEQUENCE_OF_SUBROUTINES}->[$i];
}

1;
