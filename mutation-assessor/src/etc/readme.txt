GENERAL INFORMATION

This tool is designed to add Mutation Assessor information to MAF files by using the data
provided by the Mutation Assessor (http://mutationassessor.org/).

USAGE

Mutation Assessor tool needs a properties file (db.properties) for database settings.
You can find an example properties file (db.properties.EXAMPLE) under the resources
directory of this module.

In order to add new data into existing MAF files, put db.properties together with
the bash script (addMaInfo.sh) and the executable jar file (ma.jar) into the same directory.

Run the bash script as:

	./addMaInfo.sh <input_maf_file> <output_maf_file>

Note that the bash script requires the name of the jar file to be ma.jar. So, rename
the jar file if necessary.  Alternatively, you can also run the executable jar directly
by using java -jar command:

	java -jar <jar_file> <input_maf_file> <output_maf_file>

Mutation Assessor tool can also be used to build the mutation assessor cache (as a database).
You need the source Mutation Assessor files in order to build the cache. You can download
the latest versions of these files from http://mutationassessor.org/. Extract the downloaded
file into a directory and run the script as follows:

	./buildMaCache.sh <input_directory> [<output_sql_file>]

Alternatively, you can also run the executable jar directly by using java -jar command:

	java -jar <jar_file> -db <input_directory> [<output_sql_file>]

Note that, output sql file is optional. If no output file is provided, then the tool will
insert all records directly into the database provided in the db.properties file. However,
specifying an sql file is recommended, since it is much faster. You can then use the
generated sql file to import data into any database.

INPUT FORMAT

In order to execute successfully, this tool needs the input MAF to satisfy the following conditions.

	- It must have these 6 standard column headers: Chromosome, Start_Position, End_Position,
	Reference_Allele, Tumor_Seq_Allele1, and Tumor_Seq_Allele2.
	- The data for each entry under these 6 data columns should conform to standard MAF specification.
	See https://wiki.nci.nih.gov/display/TCGA/Mutation+Annotation+Format+%28MAF%29+Specification
	for details.

OUTPUT FORMAT

The output produced by this tool will contain 5 additional columns named: MA:link.var,
MA:protein.change, MA:link.MSA, MA:link.PDB, and MA:FImpact.

If the input file already contains these mutation assessor columns, the tool will overwrite
the data under these columns. Additionally, if the input MAF file contains any other column
starting with the prefix "MA:", it will not be included in the output MAF file.

PROGRAM TERMINATION

The tool terminates with a non-zero code if there is no error during the process.