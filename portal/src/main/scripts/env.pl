#  Set up Environment for Running CGDS Java Tools

# check for JAVA_HOME
$JAVA_HOME = $ENV{JAVA_HOME};
if ($JAVA_HOME eq "") {
	die "JAVA_HOME Environment Variable is not set.  Please set, and try again.\n";
}

# Check to see if CGDS_HOME is set via command line arguments
if ($#ARGV >= 0) {
	$arg0 = $ARGV[0];
	$index = index($arg0, "CGDS_HOME");
	if ($index >= 0) {
		$home = substr($arg0, 11);
		$ENV{CGDS_HOME}=$home;
	}
}

$cgdsHome = $ENV{CGDS_HOME};
$cgdsDataHome = $ENV{CGDS_DATA_HOME};
my $osCheck = $ENV{OS};
my $pathDelim;

if( $osCheck =~ /win/i){
    $pathDelim=";";
}else{
    $pathDelim=":";
}

if ($cgdsHome eq "") {
	die "CGDS_HOME Environment Variable is not set.  Please set, and try again.\n";
}

if ($cgdsDataHome eq "") {
	die "CGDS_DATA_HOME Environment Variable is not set.  Please set, and try again.\n";
}

# Set up Classpath to use all JAR files in lib dir.
# print "Using CGDS_HOME $cgdsHome\n";
$cp="$cgdsHome/build/WEB-INF/classes";
@jar_files = glob ("$cgdsHome/lib/*.jar");
foreach my $jar (@jar_files) {
  $cp="$cp$pathDelim$jar"
}

return 1;
