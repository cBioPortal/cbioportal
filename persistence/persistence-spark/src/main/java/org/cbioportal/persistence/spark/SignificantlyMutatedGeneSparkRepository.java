package org.cbioportal.persistence.spark;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.cbioportal.model.MutSig;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.SignificantlyMutatedGeneRepository;
import org.cbioportal.persistence.spark.util.ParquetConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;


@Component
@Qualifier("significantlyMutatedGeneSparkRepository")
public class SignificantlyMutatedGeneSparkRepository implements SignificantlyMutatedGeneRepository {

    @Autowired
    private SparkSession spark;

    @Value("${data.parquet.folder}")
    private String PARQUET_DIR;

    private static Log logger = LogFactory.getLog(SignificantlyMutatedGeneSparkRepository.class);

    @Override
    public List<MutSig> getSignificantlyMutatedGenes(String studyId, String projection, Integer pageSize, Integer pageNumber, String sortBy, String direction) {
        Dataset<Row> mutationDf = spark.read()
            .option("mergeSchema", true)
            .parquet(PARQUET_DIR + "/" + studyId + "/" + ParquetConstants.DATA_MUTATIONS);
        mutationDf.createOrReplaceTempView("mutation");

        StringBuilder sb = new StringBuilder("select first(Hugo_Symbol),");
        sb.append("Entrez_Gene_Id, ");
        sb.append("COUNT(*), ");
        sb.append("COUNT(DISTINCT(Tumor_Sample_Barcode)) ");
        sb.append("from mutation ");
        sb.append("group by Entrez_Gene_Id ");
        if (sortBy != null && direction != null) {
            sb.append("sort by " + sortBy + " " + direction);
        }
        if (pageNumber != null && pageSize != null) {
            sb.append("LIMIT " + pageSize + " OFFSET " + (pageSize * pageNumber));
        }

        Dataset<Row> result = spark.sql(sb.toString());
        List<Row> resultls = result.collectAsList();
        List<MutSig> mutGenes = resultls.stream()
            .map(r -> mapToMutSig(r)).collect(Collectors.toList());

        return mutGenes;
    }

    @Override
    public BaseMeta getMetaSignificantlyMutatedGenes(String studyId) {
        throw new UnsupportedOperationException();
    }

    private MutSig mapToMutSig(Row row) {
        MutSig ms = new MutSig();
        ms.setHugoGeneSymbol(String.valueOf(row.get(0)));
        ms.setEntrezGeneId(row.get(1) == null ? null : Integer.valueOf(row.getString(1)));
        ms.setpValue(row.get(2) == null ? null : BigDecimal.valueOf(row.getLong(2)));
        ms.setqValue(row.get(3) == null ? null : BigDecimal.valueOf(row.getLong(3)));
        return ms;
    }
}