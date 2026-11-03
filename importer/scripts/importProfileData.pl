#!/usr/bin/env perl
use FindBin;
require "$FindBin::Bin/env.pl";

$startTime = time;

unless( 2 <= $#ARGV ){
	die "Insufficient number of arguments in '", join( ' ', @ARGV ), "'\n";
}
my $args = join( ' ', @ARGV );

my $cmd = "$JAVA_HOME/bin/java -Xmx1524M -cp $cp -DPORTAL_HOME='$portalHome' org.mskcc.cbio.portal.scripts.ImportProfileData $args --loadMode bulkLoad";
print "-DPORTAL_HOME='$portalHome' org.mskcc.cbio.portal.scripts.ImportProfileData $args --loadMode bulkLoad\n";
system( $cmd );
