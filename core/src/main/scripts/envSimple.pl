#  Set up Environment for Running cBio Portal Java Tools

# check for JAVA_HOME
$JAVA_HOME = $ENV{JAVA_HOME};
if ($JAVA_HOME eq "") {
	die "JAVA_HOME Environment Variable is not set.  Please set, and try again.\n";
}

# Check to see if PORTAL_HOME is set via command line arguments
if ($#ARGV >= 0) {
	$arg0 = $ARGV[0];
	$index = index($arg0, "PORTAL_HOME");
	if ($index >= 0) {
		$home = substr($arg0, 11);
		$ENV{PORTAL_HOME}=$home;
	}
}

$portalHome = $ENV{PORTAL_HOME};
my $osCheck = $ENV{OS};
my $pathDelim;

if( $osCheck =~ /win/i){
    $pathDelim=";";
}else{
    $pathDelim=":";
}

if ($portalHome eq "") {
	die "PORTAL_HOME Environment Variable is not set.  Please set, and try again.\n";
}

# Set up Classpath to use the scripts jar
@jar_files = glob("$portalHome/scripts/target/scripts-*.jar");
if (scalar @jar_files != 1) {
	die "Expected to find 1 scripts-*.jar, but found: " . scalar @jar_files;
}
$cp = pop @jar_files;

return 1;
