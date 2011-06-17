#  Set up Environment for Running Portal Tools

my $pathDelim;

if( $osCheck =~ /win/i){
    $pathDelim=";";
}else{
    $pathDelim=":";
}

# Set up Classpath to use all JAR files in lib dir.
# print "Using CGDS_HOME $cgdsHome\n";
$cp="../build/WEB-INF/classes";
@jar_files = glob ("../lib/*.jar");
foreach my $jar (@jar_files) {
  $cp="$cp$pathDelim$jar"
}

return 1;
