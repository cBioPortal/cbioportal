#  Set up Environment for Running cBio Portal Java Tools

use File::Spec;
use Cwd 'abs_path';

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

# Set up Classpath to use the scripts jar
sub locate_root {
    # isolate the directory this code file is in
    my ($volume, $script_dir, undef) = File::Spec->splitpath(__FILE__);
    # go up one level
    my $root_dir = File::Spec->catdir($script_dir, File::Spec->updir());
    # reassamble the path and resolve updirs (/../)
    return abs_path(File::Spec->catpath($volume, $root_dir));
}
$root_dir = locate_root();
@jar_files = glob("$root_dir/core-*.jar");
if (scalar @jar_files != 1) {
    die "Expected to find 1 core-*.jar, but found: " . scalar @jar_files;
}
$cp = pop @jar_files;

return 1;
