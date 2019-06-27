/*
 * Copyright (c) 2019 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.cbioportal.persistence.spark.util.SparkConfiguration;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.*;
import static org.apache.spark.sql.functions.*;
import joptsimple.*;

import java.io.IOException;

/**
 * Command Line tool to Write Parquet Files.
 */
@Component
public class ParquetWriter extends ConsoleRunnable {

    private void write(SparkSession spark, String inputFile, String outputFile) throws IOException {
        Dataset<Row> df = spark.read()
            .format("csv")
            .option("delimiter", "\t")
            .option("header", "true")
            .option("comment","#")
            .load(inputFile);
        df.write().parquet(outputFile);
    }
   
   public void run () { 
      try {
          String progName = "ParquetWriter";
          String description = "Write parquet files.";
          
          OptionParser parser = new OptionParser();
          OptionSpec<String> inputFile = parser.accepts( "input-file",
              "tsv file" ).withRequiredArg().describedAs( "path-to-input-file" ).ofType( String.class );
          OptionSpec<String> outputFile = parser.accepts( "output-file",
              "parquet file" ).withRequiredArg().describedAs( "path-to-output-file" ).ofType( String.class );

          OptionSet options = null;
          try {
              options = parser.parse(args);
          } catch (OptionException e) {
              throw new UsageException(
                  progName, description, parser,
                  e.getMessage());
          }
          if  (!options.has(inputFile)) {
              throw new UsageException(
                  progName, description, parser,
                  "'input-file' argument required.");
          }
          if  (!options.has(outputFile)) {
              throw new UsageException(
                  progName, description, parser,
                  "'output-file' argument required.");
          }
          String outputFilePath = options.valueOf(outputFile);

          SparkConfiguration sc = new SparkConfiguration();
          SparkSession spark = sc.sparkSession();

          String inputFilePath = options.valueOf(inputFile);
          ProgressMonitor.logDebug("Writing " + inputFilePath);
          this.write(spark, inputFilePath, outputFilePath);

        } catch (RuntimeException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public ParquetWriter() { super(null);}
    /**
     * Makes an instance to run with the given command line arguments.
     *
     * @param args  the command line arguments to be used
     */
    public ParquetWriter(String[] args) { super(args); }

    /**
     * Runs the command as a script and exits with an appropriate exit code.
     *
     * @param args  the arguments given on the command line
     */
    public static void main(String[] args) {
        ConsoleRunnable runner = new ParquetWriter(args);
        runner.runInConsole();
    }
}
