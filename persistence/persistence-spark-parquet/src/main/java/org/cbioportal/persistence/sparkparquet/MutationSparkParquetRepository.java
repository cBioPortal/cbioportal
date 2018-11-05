package org.cbioportal.persistence.sparkparquet;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByPosition;
import org.cbioportal.model.MutationCountByGene;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.persistence.MutationRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.apache.spark.sql.*;

import java.util.List;
import java.util.ArrayList;
import static java.lang.Math.toIntExact;

@Repository
@Qualifier("sparkParquetMutationRepository")
public class MutationSparkParquetRepository implements MutationRepository {

    private SparkSession spark;

    MutationSparkParquetRepository() {
        spark = SparkSession.builder().appName("cBioPortalSpark").master("spark://dashi-dev.cbio.mskcc.org:7077")
            .config("spark.sql.warehouse.dir", "/data/tmp").getOrCreate();
        if (spark == null) {
            throw new RuntimeException("!!!!!!!!!!!!!!!!!!!!!!!!! Cannot initialize spark context. !!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    @Override
    public void finalize() {
        try {
            if (spark != null) {
                spark.close();
            }
        }
        catch(Exception e) {
            throw new RuntimeException("!!!!!!!!!!!!!!!!!!!!!!!!! Error closing spark context !!!!!!!!!!!!!!!!!!!!!!!!!");
        }
    }

    @Override
    public List<Mutation> getMutationsInMolecularProfileBySampleListId(String molecularProfileId, String sampleListId,
                                                                       List<Integer> entrezGeneIds, Boolean snpOnly,
                                                                       String projection, Integer pageSize,
                                                                       Integer pageNumber, String sortBy,
                                                                       String direction) {

        throw new UnsupportedOperationException();
    }

    @Override
    public MutationMeta getMetaMutationsInMolecularProfileBySampleListId(String molecularProfileId, String sampleListId,
                                                                         List<Integer> entrezGeneIds) {

        throw new UnsupportedOperationException();
    }

    @Override
    public List<Mutation> getMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds, 
                                                                  List<String> sampleIds, List<Integer> entrezGeneIds, 
                                                                  String projection, Integer pageSize, 
                                                                  Integer pageNumber, String sortBy, String direction) {

        throw new UnsupportedOperationException();
    }

    @Override
    public MutationMeta getMetaMutationsInMultipleMolecularProfiles(List<String> molecularProfileIds,
                                                                    List<String> sampleIds, 
                                                                    List<Integer> entrezGeneIds) {

        throw new UnsupportedOperationException();
    }

    @Override
    public List<Mutation> fetchMutationsInMolecularProfile(String molecularProfileId, List<String> sampleIds,
                                                           List<Integer> entrezGeneIds, Boolean snpOnly,
                                                           String projection, Integer pageSize, Integer pageNumber,
                                                           String sortBy, String direction) {

        throw new UnsupportedOperationException();
    }

    @Override
    public MutationMeta fetchMetaMutationsInMolecularProfile(String molecularProfileId, List<String> sampleIds,
                                                             List<Integer> entrezGeneIds) {

        throw new UnsupportedOperationException();
    }

    @Override
    public List<MutationCountByGene> getSampleCountByEntrezGeneIdsAndSampleIds(String molecularProfileId,
                                                                               List<String> sampleIds,
                                                                               List<Integer> entrezGeneIds) {

        throw new UnsupportedOperationException();
    }

	@Override
	public List<MutationCountByGene> getSampleCountInMultipleMolecularProfiles(List<String> molecularProfileIds,
                                                                             List<String> sampleIds, List<Integer> entrezGeneIds) {
      List<MutationCountByGene> toReturn = new ArrayList();
      try {
          Dataset<Row> df = spark.read().parquet("file:///home/grossb/tmp/parquet-output-java/");
          df.createOrReplaceTempView("mutations");
          Dataset<Row> result = spark.sql("select first(Hugo_Symbol) as hugoGeneSymbol," +
                                          "Entrez_Gene_Id as entrezGeneId," +
                                          "COUNT(*) as totalCount, " +
                                          "COUNT(DISTINCT(Tumor_Sample_Barcode)) as countByEntity " +
                                          "from mutations " +
                                          "group by Entrez_Gene_Id");
          for (Row r : result.collectAsList()) {
              MutationCountByGene mc = new MutationCountByGene();
              mc.setHugoGeneSymbol(r.getString(0));
              mc.setEntrezGeneId(Integer.valueOf(r.getString(1)));
              mc.setTotalCount(toIntExact(r.getLong(2)));
              mc.setCountByEntity(toIntExact(r.getLong(3)));
              toReturn.add(mc);
          }
      }
      catch (Exception e) {
          System.out.println("!!!!! Error Spark-Parquet Start: !!!!!");
          System.out.println(e);
          System.out.println("!!!!! Error Spark-Parquet End:   !!!!!");
      }

      return toReturn;
	}

    @Override
    public List<MutationCountByGene> getPatientCountByEntrezGeneIdsAndSampleIds(String molecularProfileId,
                                                                                List<String> patientIds,
                                                                                List<Integer> entrezGeneIds) {

        throw new UnsupportedOperationException();
    }

    @Override
    public MutationCountByPosition getMutationCountByPosition(Integer entrezGeneId, Integer proteinPosStart,
                                                              Integer proteinPosEnd) {

        throw new UnsupportedOperationException();
    }
}
