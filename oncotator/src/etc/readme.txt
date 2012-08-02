GENERAL INFORMATION

This tool is designed to oncotate MAF files by using the Oncotator web service
provided by the Broad Institute (http://www.broadinstitute.org/oncotator/).

This command line tool enhances a MAF file by adding 5 new columns for each line
by processing the result of a oncotator query.

USAGE

Oncotator needs a properties file (db.properties) for database settings. You can find
an example properties file (db.properties.EXAMPLE) under the resources directory of this module.
Put db.properties together with the bash script (oncotateMaf.sh) and the executable jar file
(oncotator.jar) into the same directory.

Run the bash script as:

	./oncotateMaf.sh <input_maf_file> <output_maf_file>

Note that the bash script requires the name of the jar file to be oncotator.jar.
So, rename the jar file if necessary. Alternatively, you can also run the executable jar
directly by using java -jar command:

	java -jar <jar_file> <input_maf_file> <output_maf_file>