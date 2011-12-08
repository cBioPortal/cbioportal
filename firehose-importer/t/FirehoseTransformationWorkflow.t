#!/usr/bin/perl
# author: Arthur Goldberg, goldberg@cbio.mskcc.org

use strict;
use warnings;
use File::Spec;
use Test::More tests => 68;
use Data::Dumper; 
use File::Util;

use Utilities;
use FirehoseTransformationWorkflow;

# Verify module can be included via "use" pragma
BEGIN { use_ok('FirehoseTransformationWorkflow') }

# Verify module can be included via "require" pragma
require_ok('FirehoseTransformationWorkflow');

# sample config file
my $conf_file = File::Spec->catfile( File::Spec->curdir(), 
    qw( data Test_Workflow FIREHOSE_CONVERSION_WORKFLOW.txt ) );

my $data_root_dir = File::Spec->catfile( File::Spec->curdir(), 
    qw( data Test_Workflow Test_Data ) );

my $testName = 'test FirehoseTransformationWorkflow->new';
checkError( 'FirehoseTransformationWorkflow', 'new', 
    "FirehoseTransformationWorkflow::new: Error: Cannot read FirehoseTransformationWorkflow file: 'no such file'...", 
    undef, $testName . ' 1', ( 'no such file', undef ) );

my $input_and_error_pairs = <<'END_OF_ALL';
- subC:
    NOT_outfile: outfileC
    dirsAndFiles:
        - dirC1 fileC1
EOT
FirehoseTransformationWorkflow::_load_subroutine: Error: 'outfile' not a defined scalar for subroutine 'subC' at...
EOT
- subC:
    NOT_outfile: [ foo ]
    dirsAndFiles:
        - dirC1 fileC1
EOT
FirehoseTransformationWorkflow::_load_subroutine: Error: 'outfile' not a defined scalar for subroutine 'subC' at...
EOT
- subC:
    outfile: out For C
    NOT_dirsAndFiles: 
        - dirC1 fileC1
EOT
FirehoseTransformationWorkflow::_load_subroutine: Error: 'dirsAndFiles' not a defined list for subroutine 'subC' at...
EOT
- subC:
    outfile: out For C
    dirsAndFiles: not a list 
EOT
FirehoseTransformationWorkflow::_load_subroutine: Error: 'dirsAndFiles' not a defined list for subroutine 'subC' at...
EOT
- subC:
    outfile: out For C
    dirsAndFiles:
        - ~
EOT
FirehoseTransformationWorkflow::_load_subroutine: Error: 'dirsAndFiles' contains an undefined string for subroutine 'subC' at...
EOT
- subC:
    outfile: out For C
    dirsAndFiles:
        - dir file extra more
EOT
FirehoseTransformationWorkflow::_load_subroutine: Error: 'dir file extra more' in 'dirsAndFiles' not a pair of strings for subroutine 'subC' at...
EOT
- subC:
    outfile: out For C
    dirsAndFiles:
        - dir
EOT
FirehoseTransformationWorkflow::_load_subroutine: Error: 'dir' in 'dirsAndFiles' not a pair of strings for subroutine 'subC' at...
EOT
- subC:
    outfile: out_for_C
    dirsAndFiles:
        - dir file

---
- [ foo ]
EOT
FirehoseTransformationWorkflow::_load_dependency: Error: dependencies not a defined scalar at...
EOT
- subC:
    outfile: out_for_C
    dirsAndFiles:
        - dir file
---
- ~
EOT
FirehoseTransformationWorkflow::_load_dependency: Error: dependencies not a defined scalar at...
EOT
- subC:
    outfile: out_for_C
    dirsAndFiles:
        - dir file
---
- sub1 requ
EOT
FirehoseTransformationWorkflow::_load_dependency: Error: dependencies 'sub1 requ' is not 3 fields at...
EOT
- subC:
    outfile: out_for_C
    dirsAndFiles:
        - dir file
---
- sub1 requ sub2 xxx
EOT
FirehoseTransformationWorkflow::_load_dependency: Error: dependencies 'sub1 requ sub2 xxx' is not 3 fields at...
EOT
- subC:
    outfile: out_for_C
    dirsAndFiles:
        - dir file
---
- sub1 NOT_requires sub2
EOT
FirehoseTransformationWorkflow::_load_dependency: Error: dependencies 'sub1 NOT_requires sub2' does not use 'requires' or 'precluded_by' at...
EOT
- subC:
    outfile: out_for_C
    dirsAndFiles:
        - dir file
---
- subX requires subX
EOT
FirehoseTransformationWorkflow::_load_dependency: Error: dependencies 'subX requires subX' has undefined subroutine 'subX' at...
EOT
- subC:
    outfile: out_for_C
    dirsAndFiles:
        - dir file
---
- subC requires subC
EOT
FirehoseTransformationWorkflow::_load_dependency: Error: in dependency 'subC requires subC'; subroutine 'subC' does not preceed 'subC' in the workflow at...
EOT
- subA:
    outfile: out_for_A
    dirsAndFiles:
        - dir file
- subC:
    outfile: out_for_C
    dirsAndFiles:
        - dir file
---
- subA requires subC
EOT
FirehoseTransformationWorkflow::_load_dependency: Error: in dependency 'subA requires subC'; subroutine 'subC' does not preceed 'subA' in the workflow at...
EOT
- subC:
    outfile: out_for_C
    dirsAndFiles:
        - dir file
- subC:
    outfile: out_for_C
    dirsAndFiles:
        - dir file
EOT
FirehoseTransformationWorkflow::_load_subroutine: Error: duplicate of subroutine 'subC' at...
EOT
- subC:
    outfile: out_for_C
    arguments: [ foo ]
    dirsAndFiles:
        - dir file
EOT
FirehoseTransformationWorkflow::_load_subroutine: Error: 'arguments' not a string for subroutine 'subC' at...
END_OF_ALL

my @inputs_and_errors = split( /EOT/, $input_and_error_pairs );

test_errors( @inputs_and_errors );

# test all the errors in the list
sub test_errors{
	my( @inputs_and_errors ) = @_;
	my $test_num = 1;
	for( my $i=0; $i < scalar( @inputs_and_errors); $i += 2 ){
		my $input = $inputs_and_errors[$i];
		my $error = $inputs_and_errors[$i+1];
        $error =~ s/\n//g;
	    checkError( 'FirehoseTransformationWorkflow', '_test_parse_workflow',  $error,
	       undef,  'FirehoseTransformationWorkflow test - _test_parse_workflow num ' . $test_num++, ( $input ) );
	}
}

# good config:
my $good_config = <<'EOT';
- subA:
    outfile: out_for_A
    dirsAndFiles:
        - dirA fileA
- subB:
    outfile: out_for_B
    dirsAndFiles:
        - dirB fileB
- subC:
    outfile: out_for_C
    dirsAndFiles:
        - dirC fileC
---
- subB requires subA
- subC precluded_by subB
EOT

my $fileUtil = File::Util->new();
my $correct_dump_good_config = File::Spec->catfile( File::Spec->curdir(), qw( data Test_Workflow Correct_Dumped_good_config.txt ) );
is_deeply( FirehoseTransformationWorkflow->_test_parse_workflow_new( $good_config ), 
    eval( $fileUtil->load_file( $correct_dump_good_config ) ), 'test good config');
my $good_FTW = FirehoseTransformationWorkflow->_test_parse_workflow_new( $good_config );
is( $good_FTW->getOutputFile( 'subC', 'Generic_Cancer' ), 'out_for_C', 'test getOutputFile' );
is( $good_FTW->getOutputFile( 'no such sub', 'Generic_Cancer' ), undef, 'test getOutputFile 2' );

# - subB requires subA
# - subC precluded_by subB
# executed : sequence
my $execution_dependencies = <<'EOT';
: A C
A : A B C
B : A
C : A C
A B : A B
A C : A B C 
B : A
B C : A
C : A  C
EOT

my @execution_dependency_tests = split( /\n/, $execution_dependencies );
foreach my $execution_dependency_test (@execution_dependency_tests){
	my( $executed, $correct_next_sequence ) = split( /:/, $execution_dependency_test );

	my @executed = clean_list_from_string( $executed );
	my $good_FTW = FirehoseTransformationWorkflow->_test_parse_workflow_new( $good_config );
	
    map {$good_FTW->__was_executed( 'sub' . $_ )} @executed;

    my @correct_next_sequence_list = map {'sub' . $_} clean_list_from_string( $correct_next_sequence );

    my $next;
    my $actual_nexts;
	while( $next = $good_FTW->_next_consistent_with_dependencies() ){
		push @{$actual_nexts}, $next;
	}
    
    is_deeply( $actual_nexts, \@correct_next_sequence_list, "test _next_consistent_with_dependencies with '$executed' executing" );
}

sub clean_list_from_string{
	my( $s ) = @_;
    my @s = split( /\s+/, $s );	
    my @rv = ();
    foreach (@s){
    	if( $_ ne '' ){
    		push @rv, $_;
    	}
    }
	return @rv;
}

# test with the file system
# remember:
# - subB requires subA
# - subC precluded_by subB
# test name         : files     : correct sequence of nexts
my $execution_with_file_testing = <<'EOT';
B requires A        : B C       : C
A and C             : A C       : A C
no data             :           : 
C precluded_by B    : A B C     : A B
no C                : A B       : A B
A alone             : A         : A 
B alone             : B         :
C alone             : C         : C
EOT

my @execution_with_file_testing = split( /\n/, $execution_with_file_testing );
# tmp dir for test files
my $root_dir = '/tmp/test_FirehoseTransformationWorkflow';
$fileUtil->make_dir( $root_dir, '--if-not-exists' );

foreach my $execution_with_file_test (@execution_with_file_testing){
    my( $test_name, $executed, $correct_next_sequence ) = split( /:/, $execution_with_file_test );

    # create a FTW
    my $FTW = FirehoseTransformationWorkflow->new( $conf_file );
    
    # run twice, second time to test reset()
    foreach my $iter ('original', 'reset' ){
	    # create the dirs and files
        # print "removing all files in $root_dir\n";
	    my $cmd = "rm -rf $root_dir/*"; 
	    system( $cmd ) == 0
	        or die "system '$cmd' failed: $?";
	
	    # for each sub in $executed, for each dir and file, create file in dir
	    foreach my $sub ( map {'sub' . $_} clean_list_from_string( $executed ) ){
	        my $dirs_and_files = $FTW->getFirehoseDirectoriesAndFiles( $sub );
	        foreach my $dir_and_file (@{$dirs_and_files}){
	            my( $dir, $file ) = @{$dir_and_file};
	            $fileUtil->make_dir( File::Spec->catfile( $root_dir, $dir ), '--if-not-exists' );
	            $fileUtil->write_file( 'file' => File::Spec->catfile( $root_dir, $dir, $file ), 'content' => 'test', 'bitmask' => 0644 );
	        }
	    }

	    # get nexts from the FTW, compare with expected
	    my @actual_nexts = ();
	    my $next;
       while( $next = $FTW->next_sub( $root_dir ) ){
	        push @actual_nexts, $next;
	    }
	    my @correct_next_sequence_list = map {'sub' . $_} clean_list_from_string( $correct_next_sequence );
	    $test_name =~ s/\s+$//;
	    my $is_deeply = is_deeply( \@actual_nexts, \@correct_next_sequence_list, "test next_sub " . $test_name . ' ' . $iter);
	    
	    # test reset by resetting and running again
	    $FTW->reset();
    }
}

# test extra arguments
my $config_with_extra_arg = <<'EOT';
- subA:
    outfile: out_for_A
    arguments: args_for_A
    dirsAndFiles:
        - dirA fileA 
EOT

my $FTW = FirehoseTransformationWorkflow->_test_parse_workflow_new( $config_with_extra_arg );
my $args = $FTW->getArgs( 'subA' );
is( $args, 'args_for_A', 'test extra arguments' );

# test with real config file
my $f = File::Spec->catfile( File::Spec->curdir(), 
    qw( data Test_Workflow FirehoseTransformationWorkflow.yaml ) );
$FTW = FirehoseTransformationWorkflow->new( $f );
my $correct_dump = File::Spec->catfile( File::Spec->curdir(), 
    qw( data Test_Workflow Correct_Dumped_FirehoseTransformationWorkflow.txt ) );
is_deeply( $FTW, eval( $fileUtil->load_file( $correct_dump ) ), 'test with real config file' );

