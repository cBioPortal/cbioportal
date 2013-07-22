GENERAL INFORMATION

This script requires a platform dependent executable binary called "liftOver". It is available at
http://hgdownload.cse.ucsc.edu/admin/exe/. The script also requires a chain file named
"hg18ToHg19.over.chain". Detailed information and required files can be found at
http://genome.ucsc.edu/cgi-bin/hgLiftOver.

Note that the etc directory of this module contains an executable binary for linux platforms and
a chain file. However, it is strongly recommended to check the official liftOver page for
the latest versions of these files.

USAGE

Put the executable binary file (liftOver), the chain file (hg18ToHg19.over.chain),
and the bash script (hg18to19.sh) together with the executable jar (with dependencies)
into the same directory.

Run the bash script as:

	./hg18to19.sh <input_maf_file> <output_maf_file>

Note that the bash script requires the name of the jar file to be liftover.jar.
So, rename the jar file if necessary. Alternatively, you can also run the executable jar
directly by using java -jar command:

	java -jar <jar_file> <input_maf_file> <output_maf_file>