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

package org.cbioportal.persistence.spark.util;

import org.apache.commons.lang3.StringUtils;
import org.cbioportal.model.Gene;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.GeneRepository;
import org.apache.spark.sql.*;
import joptsimple.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.*;


/**
 * Command Line tool to Write Parquet Files.
 */
public class ParquetWriter {

    private static final String HUGO_SYMBOL = "Hugo_Symbol";
    private static final String ENTREZ_GENE_ID = "Entrez_Gene_Id";
    
    private static GeneRepository geneRepository;

    private static void write(String inputFile, String outputFile, String typeOfData) {
        SparkSession spark = SparkSession.builder()
            .appName("cBioPortal")
            .master("local[*]")
            .getOrCreate();
        
        Dataset<Row> df = null;
        Iterator it = null;
        switch (typeOfData) {
            case "case": case "panel":
                df = spark.read()
                    .format("csv")
                    .option("delimiter", ":")
                    .load(inputFile);
                
                String listCol = "case".equalsIgnoreCase(typeOfData) ? "case_list_ids" : "gene_list";
                it = df.toLocalIterator();
                while (it.hasNext()) {
                    Row row = (Row) it.next();
                    String colName = row.getString(0);
                    if (listCol.equalsIgnoreCase(colName)) {
                        df = df.withColumn(colName, explode(lit(row.getString(1).trim().split("\\s+"))));
                    } else {
                        df = df.withColumn(colName, lit(row.getString(1).trim()));
                    }
                }
                df = df.drop("_c0", "_c1").distinct();
                break;

            case "meta":
                df = spark.read()
                    .format("csv")
                    .option("delimiter", ":")
                    .load(inputFile);

                it = df.toLocalIterator();
                while (it.hasNext()) {
                    Row row = (Row) it.next();
                    String colName = row.getString(0);
                    df = df.withColumn(colName, lit(row.getString(1).trim()));
                }
                df = df.drop("_c0", "_c1").distinct();
                break;

            case "cna": // data_CNA
                df = spark.read()
                    .format("csv")
                    .option("delimiter", "\t")
                    .option("header", "true")
                    .option("comment","#")
                    .load(inputFile);

                // sparse matrix
                List<String> pids = new LinkedList<>(Arrays.asList(df.columns()));
                boolean entrezGeneIdExists = pids.remove(ENTREZ_GENE_ID);
                boolean hugoSymbolExists = pids.remove(HUGO_SYMBOL);
                StringBuilder sb = new StringBuilder();
                for (String pid : pids) {
                    sb.append(", '" + pid + "', `" + pid + "`");
                }
                String mainCol = ENTREZ_GENE_ID;
                if (!entrezGeneIdExists) {
                    mainCol = HUGO_SYMBOL;
                    df = df.withColumn(HUGO_SYMBOL, regexp_replace(df.col(HUGO_SYMBOL), "\\|.*", ""));
                }
                String[] mainColArr = {mainCol};
                df = df.na().drop(mainColArr);
                // get Gene Details
                List<String> geneIds = df.select(mainCol).collectAsList()
                    .stream().map(r -> r.getString(0)).collect(Collectors.toList());
                SpringApplicationContext.init();
                geneRepository = SpringApplicationContext.getGeneRepository();
                List<Gene> genes;
                if (ENTREZ_GENE_ID.equalsIgnoreCase(mainCol)) {
                    genes = geneRepository.fetchGenesByEntrezGeneIds(geneIds.stream().filter(id -> StringUtils.isNumeric(id))
                        .map(Integer::valueOf).collect(Collectors.toList()), PersistenceConstants.SUMMARY_PROJECTION);
                } else {
                    genes = geneRepository.fetchGenesByHugoGeneSymbols(geneIds, PersistenceConstants.SUMMARY_PROJECTION);
                }
                
                Dataset<Row> geneDf = spark.createDataFrame(genes, Gene.class);
                geneDf = geneDf.withColumn("entrezGeneId", geneDf.col("entrezGeneId").cast("string"))
                    .withColumnRenamed("entrezGeneId", ENTREZ_GENE_ID)
                    .withColumnRenamed("hugoGeneSymbol", HUGO_SYMBOL);
                if (entrezGeneIdExists && hugoSymbolExists) {
                    if (ENTREZ_GENE_ID.equalsIgnoreCase(mainCol)) {
                        geneDf = geneDf.drop(HUGO_SYMBOL);
                    } else {
                        geneDf = geneDf.drop(ENTREZ_GENE_ID);
                    }
                }
                df = df.join(geneDf, mainCol);
                
                df = df.selectExpr(ENTREZ_GENE_ID, HUGO_SYMBOL, 
                    "cytoband", "stack(" + pids.size() + sb.toString() + ") as (SAMPLE_ID, VALUE)");

                // ImportTabDelimData "temporary solution -- change partial deletion back to full deletion"
                df = df.withColumn("VALUE", when(df.col("VALUE").equalTo("-1.5"), "-2")
                    .otherwise(df.col("VALUE")));
                break;
                
            default:
                df = spark.read()
                    .format("csv")
                    .option("delimiter", "\t")
                    .option("header", "true")
                    .option("comment","#")
                    .load(inputFile);
                
                for (String col : df.columns()) {
                    if (col.contains(" ")) {
                        df = df.withColumnRenamed(col, col.replaceAll(" ", "_"));
                    }
                }
                break;
        }
        df.write()
            .mode("append").parquet(outputFile);
    }

    public static void main(String[] args) {
        try {
            String progName = "ParquetWriter";

            OptionParser parser = new OptionParser();
            OptionSpec<String> inputFile = parser.accepts( "input-file",
                "tsv file" ).withRequiredArg().describedAs( "path-to-input-file" ).ofType( String.class );
            OptionSpec<String> outputFile = parser.accepts( "output-file",
                "parquet file" ).withRequiredArg().describedAs( "path-to-output-file" ).ofType( String.class );
            OptionSpec<String> type = parser.accepts( "input-file-type",
                "input file type" ).withOptionalArg()
                .describedAs("case for case_lists, meta for meta, panel for gene_panel, cna for data_CNA, otherwise data.").ofType( String.class );
            
            OptionSet options = null;
            try {
                options = parser.parse(args);
            } catch (Exception e) {
                throw e;
            }

            String outputFilePath = options.valueOf(outputFile);
            String inputFilePath = options.valueOf(inputFile);
            String typeOfData = "data";
            if (options.valueOf(type) != null) {
                typeOfData = options.valueOf(type);
            }
            
            write(inputFilePath, outputFilePath, typeOfData);

        } catch (RuntimeException e) {
            throw e;
        }
    }
}