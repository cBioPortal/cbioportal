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

import org.mskcc.cbio.portal.util.GlobalProperties;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.util.SparkConfiguration;

import org.springframework.context.annotation.PropertySource;
import org.springframework.beans.factory.annotation.Value;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.*;
import static org.apache.spark.sql.functions.*;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Command Line tool to Write Parquet Files.
 */
@PropertySource("classpath:portal.properties")
public class ParquetWriter extends ConsoleRunnable {

    @Value("${data.parquet.folder}")
    private String parquetDir;
    @Value("${data.tsv.folder}")
    private String datatDir;
        

    private void write(SparkSession spark, String destination, String txtFile) throws IOException {
        Dataset<Row> df = spark.read()
            .format("csv")
            .option("delimiter", "\t")
            .option("header", "true")
            .option("comment","#")
            .load(datatDir + txtFile);
        df.write().parquet(destination + "/" + txtFile + ".parquet");
    }
   
   public void run () {
      try {
    	  // check args
	      if (args.length < 2) {
	         // an extra --noprogress option can be given to avoid the messages regarding memory usage and % complete
             throw new UsageException(
                     "Parquet Writer script ",
                     null,
                     "<study id>",
                     "<data txt file>");
	      }
          String destinationFolder = parquetDir + "/" + args[0];
          File directory = new File(destinationFolder);
          if (!directory.exists()){
              directory.mkdir();
          }

          SparkConfiguration sc = new SparkConfiguration();
          SparkSession spark = sc.sparkSession();
          
          for (int i=1; i<args.length; i++) {
              String txtFile = args[i];
              ProgressMonitor.logDebug("Writing " + txtFile + ".parquet");
              this.write(spark, destinationFolder, txtFile);
          }
	      
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
