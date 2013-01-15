#!/usr/bin/perl
require "../scripts/env.pl";

$startTime = time;
my $loadTypeFile = '/tmp/loadTypeFile.txt';
# $loadTypeFile is used by compareDirectAndBulkDBMSload.pl to control the load type used by *.sh files it tests
# it writes the load type into $loadTypeFile, which is read here.
# $loadTypeFile is awkward; perhaps a standard importProfileData.pl could ignore it, and then, if we want to make DBMS load measurements, 
# we could temporarily replace importProfileData.pl; but that's awkward too

unless( 2 <= $#ARGV ){
	die "Insufficient number of arguments in '", join( ' ', @ARGV ), "'\n";
}
my $args = join( ' ', @ARGV );

# if $loadTypeFile exists then use it to determine $loadType, else do bulkload
if( -e $loadTypeFile ){
	open(my $in,  "<",  $loadTypeFile )  or die "Can't open $loadTypeFile: $!";
	my $loadType = <$in>;
	print "Load type is '$loadType'.\n";
	system ("$JAVA_HOME/bin/java -Xmx1524M -cp $cp -DCGDS_HOME='$cgdsHome' org.mskcc.cbio.cgds.scripts.ImportProfileData $args --loadMode $loadType");
	$elapsedMin = (time - $startTime)/60;
	print sprintf("%.1f", $elapsedMin), "\t(min) to run ImportProfileData on\t", "$args --loadMode $loadType";
}else{
	# $loadTypeFile does not exist, so compareDirectAndBulkDBMSload.pl is not being run
	my $cmd = "$JAVA_HOME/bin/java -Xmx1524M -cp $cp -DCGDS_HOME='$cgdsHome' org.mskcc.cbio.cgds.scripts.ImportProfileData $args --loadMode bulkLoad";
	print "-DCGDS_HOME='$cgdsHome' org.mskcc.cbio.cgds.scripts.ImportProfileData $args --loadMode bulkLoad\n";
	system( $cmd );
}
