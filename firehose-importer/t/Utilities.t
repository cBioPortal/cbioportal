#!/usr/bin/perl
# author: Arthur Goldberg, goldberg@cbio.mskcc.org

use strict;
use warnings;
use Test::More tests => 31;
use File::Spec;
use File::Util;
use Data::Dumper;
use Data::CTable;

use Utilities;

# Verify module can be included via "use" pragma
BEGIN { use_ok('Utilities') }

# Verify module can be included via "require" pragma
require_ok('Utilities');

my $usage = <<END;
test usage
END

my $utilities = Utilities->new( $usage );

is( $Utilities::dataFilePrefix, 'data_', 'dataFilePrefix' );
is( $Utilities::metaFilePrefix, 'meta_', 'metaFilePrefix' );

my $t = {};
my $tf = '/tmp/people.out';
hashToFile( {}, 'Person', $tf . '2');

$t->{joe}->{height} = 60;
$t->{mary}->{height} = 50;
# empty and partially filled columns
$t->{mary}->{color} = 'red';
$t->{sam} = {};
my $o = hashToFile( $t, 'Person', $tf );

# argh!, very, very difficult to read/write individual fields with cTable
# todo: add read/write individual fields to cTable
my $table = Data::CTable->new( $tf );
my $StringRef = $table->format();
my $data = <<EOT;
 +--------+-------+--------+
 | Person | color | height |
 +--------+-------+--------+
 | joe    |       | 60     |
 | mary   | red   | 50     |
 | sam    |       |        |
 +--------+-------+--------+
EOT
 
is( $$StringRef, $data, 'hashToFile' );

# $utilities->quit( );
my $dirName = '/tmp/testdir';
my $testDir = File::Util->make_dir( $dirName, '--if-not-exists' );
my $testFile = 'test_file.txt';
is( $utilities->CreateFullPathname( $testDir  ), undef, 'test CreateFullPathname' );
# todo: make OS portable
my $fn = $utilities->CreateFullPathname( $testDir, File::Spec->catfile( $testDir, $testFile ));
is( $fn, '/tmp/testdir/test_file.txt', 'test CreateFullPathname' );
is( $utilities->CreateFullPathname( $testDir, $testFile ), '/tmp/testdir/test_file.txt', 'test CreateFullPathname' );
is( $utilities->CheckOnDirectory( $testDir, "dirName" ), '', 'test CheckOnDirectory' );
is( $utilities->CheckOnDirectory( $testDir, ), '', 'test CheckOnDirectory 2' );

# make the file
File::Util->write_file('file' => $fn, 'content' => 'testing');

checkError( $utilities, 'CheckOnFile', "mode is '/tmp/testdir/test_file.txt', but must be 'read' or 'write'. at ...", 
    undef, "testing CheckOnFile( )", $fn );

is( $utilities->CheckOnFile( 'read', $fn ), 1, 'test CheckOnFile read' );
# check on current file in writable directory
is( $utilities->CheckOnFile( 'write', $fn ), 1, 'test CheckOnFile write 1' );
my $fn2 = $utilities->CreateFullPathname( $testDir, File::Spec->catfile( $testDir, $testFile . '2' ));

# check on non-existant file in writable directory
is( $utilities->CheckOnFile( 'write', $fn2 ), 1, 'test CheckOnFile write 2' );

is( numUniq( () ), 0, 'test numUniq' );
is( numUniq( ), 0, 'test numUniq' );
is( numUniq( qw( a b ) ), 2, 'test numUniq 2' );

is( runSystem( 'tar', undef, qw( -cPf /tmp/test.tar /tmp ) ), 1, 'test run system 1' );
is( runSystem( 'rm', undef, ('/tmp/test.tar') ), 1, 'test run system 2' );

# todo: make this work
# my( $t1, $t2 ) = ( 'x', 'y' );
# is( $utilities->convert_to_full_pathnames( $testDir, 'read', qw( ), ), 'test convert_to_full_pathnames' );

# todo: to test these failures need to redefine exit()!
# check on non-existant file in non-existant directory
# print STDERR "no problem: testing will print 'Error: '/no_such_dir/file' is not in a writable directory.'\n";
# is( $utilities->CheckOnFile( 'write', '/no_such_dir/file'), undef, 'test CheckOnFile write 3' );

rmdir( $dirName  );

is( englishList( qw( a b c d ) ), "a, b, c and d", 'test englishList' );
is( englishList( qw( ) ), "", 'test englishList');
is( englishList( qw( a b ) ), "a and b", 'test englishList');
is( englishList( qw( a ) ), "a");

is_deeply( [ removeDupes( qw( a b c d ) ) ],  [ qw( a b c d ) ], "test removeDupes");
is_deeply( [ removeDupes( qw( a a a) ) ],  [ qw( a ) ], "test removeDupes");
is_deeply( [ removeDupes( qw( a b c a b c a b c d d ) ) ],  [ qw( a b c d ) ], "test removeDupes");
is_deeply( [ removeDupes( () ) ],  [], "test removeDupes");
is_deeply( [ removeDupes( qw( c d ), undef ) ],  [ qw( c d ) ], "test removeDupes with undef");

my $x = 'foo';
is( $utilities->verifyArgumentsAreDefined( $x ), 1, "verifyArgumentsAreDefined" );
my $y = 'z';
is( $utilities->verifyArgumentsAreDefined( $x, $y ), 1, "verifyArgumentsAreDefined" );

