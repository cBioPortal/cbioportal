package FirehoseEnv;

require Exporter;
@ISA = qw(Exporter);
@EXPORT = qw($JAVA_HOME);

# check for JAVA_HOME
$JAVA_HOME = $ENV{JAVA_HOME};
if ($JAVA_HOME eq "") {
	die "JAVA_HOME Environment Variable is not set.  Please set, and try again.\n";
}

1;
