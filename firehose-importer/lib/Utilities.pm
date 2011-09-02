package Utilities;

require Exporter;
@ISA = qw(Exporter);
@EXPORT = qw( runSystem hashToFile CheckOnFile CheckOnDirectory 
    CreateFullPathname quit numUniq timing englishList removeDupes verifyArgumentsAreDefined existingCols 
    checkError compareFiles listCancers ); 

# put before use strict;/ use warnings; so they're in the Utilities package symbol table
$dataFilePrefix = 'data_';  # prefix for all data files that are 1) created from TCGA Firehose data, and 2) loaded via importProfileData into the CGDS dbms
$metaFilePrefix = 'meta_';  # prefix for all metadata files that are paired 1-to-1 with data files

use File::Spec;
use strict;
use warnings;
use Data::Dumper;
use Data::CTable;
use Test::More;

sub new {
    unless( scalar(@_) == 2 ){
        warn "Utilities::new() takes the usage string";
        return;
    }
    
    my $class = shift;
    
    my $self  = {
        USAGE => shift,
    };
    
    bless( $self, $class );
    return $self;
}

# run system() through a sub that reports execution and reports errors
# todo: also 1) debugs, 2) finds executabiles
sub runSystem{
    my( $cmd, $msg, @args ) = @_;
    
    print "$msg\n" if defined( $msg );
    # print "PATH is $ENV{PATH}\n";
    
    system( $cmd, @args ) == 0
       or die "system $cmd, @args failed: $?";
}

# given a 2D hash of the form $hash->{key}->{col} = value
# write a table to file $fn with keys as row headers and cols as column headers
# $keyCol is column name of the key column
# todo: add this to cTable
# todo: add names for row headers, order of row headers
# todo: don't report columns that are all 0s or undefined
# todo: optionally sum columns and/or rows
sub hashToFile{
	my( $hash, $keyCol, $fn ) = @_;
	
    my $cTable = Data::CTable->new( {_FDelimiter => "\t"} ) || 
        warn "Utilities::hashToFile: cTable not created";
    
    # add columns
    my $keys = [keys %{$hash}];
    $cTable->col( $keyCol => $keys);
    
    my %cols; 
    foreach my $c (keys %{$hash}){
        foreach my $field (keys %{$hash->{$c}}){
            $cols{$field}=1;
        }
    }
    
    foreach my $column (keys %cols){
        my @data;
        foreach my $c (keys %{$hash}){
            push @data, $hash->{$c}->{$column};
        }
        $cTable->col( $column => \@data );
    }
    # put $keyCol first
    $cTable->fieldlist( [ ($keyCol, keys %cols) ] );
    # sort by $keyCol
    $cTable->sort( [( $keyCol )]);
    $cTable->write( $fn );
    return $cTable;
}

# check on access to file
# $mode in ('read', 'write' )
sub CheckOnFile{
	my( $self, $mode, $f ) = @_;
	if( $mode eq 'read' ){
	    unless( -r $f ){
	        $self->quit( "'$f' is not a readable file");
	    }
	    return 1;
	}else{
	    if( $mode eq 'write' ){
	    	if( -e $f ){
	            unless( -w $f ){
	                $self->quit( "'$f' is not a writable file");
	            }
	    	}else{
	    		my($volume,$directories,$file) = File::Spec->splitpath( $f );
                unless( -w $directories ){
                    $self->quit( "'$f' is not in a writable directory");
                }
	    	}
	    }else{
	    	warn "mode is '$mode', but must be 'read' or 'write'.";
	    	return undef;
	    }
	}
    return 1;
}

# check that argument is a writable directory, and print contents
sub CheckOnDirectory{
    my( $self, $dir, $name)  = @_;
    #   print "Checking on $dir\n";
    if( !(-w $dir && -d $dir) ){
        quit( "'$dir' is not a writable directory");
    } else{
	    if( defined( $name ) ){
	        opendir( my $dh, $dir );
	        my @entries = readdir $dh; 
	        if( scalar( @entries ) == 2 ){
	            print "\$$name is empty.\n";
	        }else{
	            print "\$$name contains:\n";
	            foreach (@entries){
	                if( $_ ne '.' && $_ ne '..'){
	                    print " $_\n";
	                }
	            }
	        }
	    }
    }
}

# combine rootDir and filename
# if filename is absolute, return it
# else, if filename is relative and rootDir is set, then return filename in rootDir
# otherwise die
sub CreateFullPathname{
    my $self = shift;
    my( $rootDir, $filename, $fileUse ) = @_;
    
    unless( defined( $filename ) ){
        return;
    }
    
    my $rv;
    if( File::Spec->file_name_is_absolute( $filename ) ){
        $rv = $filename;
    }else{
        if( defined( $rootDir ) ){
            $rv = File::Spec->catfile( $rootDir, $filename );
        }
    }

    if( defined( $fileUse )){
        print "\$$fileUse is '$rv'.\n";
    }
    return $rv;
    #    die "Cannot combine '$rootDir' and '$filename' into a full pathname.\n";
}

# todo: make this work
# see http://www.webmasterkb.com/Uwe/Forum.aspx/perl/31984/FAQ-7-26-How-can-I-find-out-my-current-or-calling-package
# sub verifyArgumentsAreDefined2{
#     my $self = shift;
#     my( $package, $filename, $line ) = caller;
#     foreach my $arg (@_){
#         if( !eval 'defined( $' . "$package::$arg" . ')'  ){
#             $self->quit( "$arg is required" );
#         }
#     }
#     return 1;
# }

sub quit{
    my $self = shift;
    my $error = shift;
    
    if( defined( $error )){
        print STDERR "Error: $error.\n";      
    }else{
        print STDERR "Unknown error.\n";
    }
    print STDERR $self->{USAGE}; 
    exit( 1 );
}

sub quitSimple{
    my $error = shift;
    
    if( defined( $error )){
        print STDERR "Error: $error.\n";      
    }else{
        print STDERR "Unknown error.\n";
    }
    exit( 1 );
}

# returns number of unique items in a list
sub numUniq{
    my $h = {};
    foreach (@_){
        $h->{$_}=1;
    }
    return scalar(keys %{$h});
}

# todo: elapsed timing
sub timing{

    my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
    # todo: hh:mm
    return "$hour:$min: ";
}

# convert list (a, b, c, d) into, e.g., "a, b, c and d"
# (a b ) => "a and b"
# (a) => "a"
sub englishList{
    my $fileListString = "";
    if( scalar( @_ ) ){
        $fileListString = pop @_;
    }
	if( scalar( @_ ) ){
	    $fileListString = (pop @_) . " and $fileListString";
	}
	foreach my $f (reverse @_){
	    $fileListString = $f . ", $fileListString";
	}
	return $fileListString; 
}

# remove duplicates from a list
# the list can contain undef members
# the list remains in order
sub removeDupes{
    my %seen;
    my @uniques; # so members stay in order
    foreach (@_){
    	if( defined( $_ ) ){
    		unless( exists( $seen{$_} ) ){
    			$seen{$_}++;
    			push @uniques, $_;
    		}
    	}
    }
    return @uniques;
}

# verify that all arguments are defined
# well, darn, this doesn't work, because 'my' variables defined in caller aren't visible here
sub BadVerifyArgumentsAreDefined{
    my( $self, @args) = @_;
    
    my($package, $filename, $line) = caller;

    if( defined( $package )){
        foreach my $arg (@args){
            my $var = $package . '::' . $arg;
            print Dumper "\$$var", eval "\$$var";

            if( !eval 'defined( $' . $var . ')'  ){
                $self->quit( "\$$var is required" );
            }
        }
    }

    return 1;
}

sub verifyArgumentsAreDefined{
    my( $self, @args) = @_;
    
	foreach my $arg (@args){
	
	    unless( defined( $arg  )){
	        $self->quit( "a required argument is not defined" );
	    }
	}

    return 1;
}

## CAREFUL! listing COLUMN IN needed fields BRINGS ITS INTO EXISTANCE; AUTOVIVIFICATION!
# find list of existing columns in a CTable table
sub existingCols{
    my( $table, @columns ) = @_;
    my $existingCols = [];
    no strict 'refs';
    foreach my $col (@columns){
        if( $table->col_exists($col) ){
            push @{$existingCols}, $col;
        }
    }
#     if( scalar( @columns) != scalar( @{$existingCols}) ){
#       print "cols: '", join ("', '", @columns ), "'\n";
#       print "existing cols: ", join (', ', @{$existingCols } ), "\n";
#     }
    return $existingCols;
}

# test whether an error is produced and test whether the right return value is produced
# return value can be arbitrary Perl data structure
# 
# pattern match the error message just up to a '...' in $error, so that, for example, code 
# that prints a warn warning with a line # can be moved without breaking its tests!
# returns the call's rv
sub checkError{
    my( $object, $sub, $error, $expectedRV, $testName, @args ) = @_;

    # First, save away STDERR
    my $stderrOutput;
    no warnings; # repress warning: 'Name "main::SAVEERR" used only once: possible typo at ... '
    open SAVEERR, ">&STDERR"; 
    use warnings;
    close STDERR;
    open STDERR, ">", \$stderrOutput or die "What the heck?\n";

    # call sub, with output redirected to $stderrOutput
    my $rv = $object->$sub( @args );

    # todo: option to NOT check rv; e.g., useful in first lines of GeneIdentifier.t
    # check with is_deeply, in case $rv is a complex ds
    is_deeply( $rv, $expectedRV, 'Return value for ' . $testName );
    
    # Close and restore STDERR to original condition.
    close STDERR;
    open STDERR, ">&SAVEERR";
    
    # pattern match the error message up to a '...' in $error, so code can move without killing tests!
    # find '...'
    if( $error =~ s/\.\.\..*$// ){
        # truncate $stderrOutput to same length
        $stderrOutput = substr $stderrOutput,0,length( $error );
    }

    # test
    is( $stderrOutput, $error, 'Error output for ' . $testName );   
    return $rv;
}

# use diff to compare actual output file with correct output file
# using Test::More
sub compareFiles{
    my( $actualFile, $correctFile, $testName ) = @_;
    # don't say ok unless diff runs
    # print "diff $actualFile $correctFile\n";
    my $cmd      = "diff -C 5 $actualFile $correctFile;";
    if ( 0 == system($cmd) ) {
        my $diffSays = `diff $actualFile $correctFile;`;
        is( $diffSays, '', $testName );
    } else {
        fail($testName);
    }
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
