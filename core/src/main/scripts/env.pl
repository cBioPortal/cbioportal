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
$portalDataHome = $ENV{PORTAL_DATA_HOME};
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

if ($portalDataHome eq "") {
	die "PORTAL_DATA_HOME Environment Variable is not set.  Please set, and try again.\n";
}

# Set up Classpath to use all JAR files in lib dir.
$cp="$portalHome/portal/target/portal/WEB-INF/classes";
@jar_files = glob ("$portalHome/portal/target/portal/WEB-INF/lib/*.jar");
foreach my $jar (@jar_files) {
  $cp="$cp$pathDelim$jar"
}

return 1;
