package org.cbioportal.persistence.sparkparquet;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCountByPosition;
import org.cbioportal.model.MutationCountByGene;
import org.cbioportal.model.meta.MutationMeta;
import org.cbioportal.persistence.MutationRepository;

import org.apache.livy.*;
import org.apache.spark.sql.*;

import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import com.google.common.base.Strings;

import java.util.*;
import java.net.URI;
import javax.annotation.PreDestroy;
import static java.lang.Math.toIntExact;

@Repository
@Qualifier("sparkParquetMutationRepository")
public class MutationSparkParquetRepository implements MutationRepository {

    private static SparkSession spark;

    MutationSparkParquetRepository() {
        if (spark == null) {
            System.out.println("!!!!! Initializing spark context Start !!!!!");
            //spark = SparkSession.builder().appName("cBioPortalSpark").master("local")
            spark = SparkSession.builder().appName("cBioPortalSpark").master("spark://dashi-dev.cbio.mskcc.org:7077")
                .config("spark.sql.warehouse.dir", "/data/tmp").config("spark.yarn.submit.waitAppCompletion","false").getOrCreate();
            System.out.println("!!!!! Initializing spark context End !!!!!");
            if (spark == null) {
                throw new RuntimeException("!!!!! Cannot initialize spark context. !!!!!");
            }
        }
    }

    @PreDestroy
    public void cleanUpSpark() {
        if (spark != null) {
            System.out.println("!!!!! Closing spark context Start !!!!!");
            spark.stop();
            spark = null;
            System.out.println("!!!!! Closing spark context End !!!!!");
            System.out.println("MutationSparkParquetRepository going to sleep....");
            try {
                java.util.concurrent.TimeUnit.SECONDS.sleep(10);
            }
            catch(InterruptedException e) {
                throw new RuntimeException("!!!!! Error cloning spark context !!!!!");
            }
            System.out.println("MutationSparkParquetRepository waking up....");
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
        return getSampleCountInMultipleMolecularProfilesDirect(molecularProfileIds, sampleIds, entrezGeneIds);
        //return getSampleCountInMultipleMolecularProfilesLivyProgrammaticAPI(molecularProfileIds, sampleIds, entrezGeneIds);
        //return getSampleCountInMultipleMolecularProfilesLivyRestfulAPI(molecularProfileIds, sampleIds, entrezGeneIds);
    }

    private List<MutationCountByGene> getSampleCountInMultipleMolecularProfilesLivyRestfulAPI(List<String> molecularProfileIds,
                                                                                              List<String> sampleIds, List<Integer> entrezGeneIds) {
        List<MutationCountByGene> toReturn = new ArrayList();
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            Map<String, String> data = new HashMap();
            data.put("kind", "spark");
            HttpEntity requestEntity = new HttpEntity<>(data, headers);
            ResponseEntity<String> response = restTemplate.exchange("http://localhost:8998/sessions", HttpMethod.POST, requestEntity, String.class);
            if (!Strings.isNullOrEmpty(response.getBody())) {
                System.out.println("!!!!! Spark-Livy sessions Start !!!!!");
                // get the session id
                System.out.println("Getting a livy session...");
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> map = mapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>(){});
                Integer livySessionId = (Integer)map.get("id");
                System.out.println("livy session id: " + livySessionId);
                // wait until idle state
                String livySessionState;
                System.out.println("Waiting on livy session idle state...");
                do {
                    requestEntity = new HttpEntity<>(headers);
                    response = restTemplate.exchange("http://localhost:8998/sessions/" + livySessionId + "/state", HttpMethod.GET, requestEntity, String.class);
                    map = mapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>(){});
                    livySessionState = (String)map.get("state");
                } while(!livySessionState.equals("idle"));
                // get the data
                System.out.println("Executing scala code via livy session...");
                // see https://stackoverflow.com/questions/41127041/livy-server-return-a-dataframe-as-json
                String code = String.join(System.getProperty("line.separator"),
                                          "val sqlContext = new org.apache.spark.sql.SQLContext(sc)",
                                          "val df = sqlContext.read.parquet(\"file:///home/grossb/tmp/parquet-output-java/\")",
                                          "df.createOrReplaceTempView(\"mutations\")",
                                          "println(\"start-query-results\")",
                                          "println(sqlContext.sql(\"select first(Hugo_Symbol) as hugoGeneSymbol,Entrez_Gene_Id as entrezGeneId,COUNT(*) as totalCount,COUNT(DISTINCT(Tumor_Sample_Barcode)) as countByEntity from mutations group by Entrez_Gene_Id\").toJSON.collect.mkString(\"[\", \",\", \"]\"))",
                                          "println(\"end-query-results\")");
                data.put("code", code);
                requestEntity = new HttpEntity<>(data, headers);
                response = restTemplate.exchange("http://localhost:8998/sessions/" + livySessionId + "/statements", HttpMethod.POST, requestEntity, String.class);
                map = mapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>(){});
                Integer livyStatementId = (Integer)map.get("id");
                System.out.println("livy statement id: " + livyStatementId);
                // wait until statement response is available
                String livyStatementState;
                System.out.println("Waiting on livy statement available state...");
                do {
                    requestEntity = new HttpEntity<>(headers);
                    response = restTemplate.exchange("http://localhost:8998/sessions/" + livySessionId + "/statements/" + livyStatementId, HttpMethod.GET, requestEntity, String.class);
                    map = mapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>(){});
                    livyStatementState = (String)map.get("state");
                } while(!livyStatementState.equals("available"));
                System.out.println("Processing livy statement results...");
                String results = response.getBody().substring(response.getBody().lastIndexOf("start-query-results") + 21,
                                                              response.getBody().lastIndexOf("end-query-results")-2);
                results = results.replaceAll(java.util.regex.Pattern.quote("\\\""), "\"");
                toReturn = mapper.readValue(results, new TypeReference<List<MutationCountByGene>>(){});
                // delete the session
                System.out.println("Deleting the livy session...");
                requestEntity = new HttpEntity<>(headers);
                response = restTemplate.exchange("http://localhost:8998/sessions/" + livySessionId, HttpMethod.DELETE, requestEntity, String.class);
                System.out.println("!!!!! Spark-Livy sessions End !!!!!");
            }
        }
        catch (Exception e) {
            System.out.println("!!!!! Error Spark-Livy REST Start !!!!!");
            System.out.println(e);
            System.out.println("!!!!! Error Spark-Livy REST End   !!!!!");
        } 
        return toReturn;
    }

    private List<MutationCountByGene> getSampleCountInMultipleMolecularProfilesLivyProgrammaticAPI(List<String> molecularProfileIds,
                                                                                                   List<String> sampleIds, List<Integer> entrezGeneIds) {
        LivyClient client = null;
        List<MutationCountByGene> toReturn = new ArrayList();
        try {
            client = new LivyClientBuilder().setURI(new URI("http://localhost:8998")).build();
            Dataset<Row> result = client.submit(new SampleCountJob()).get();
            for (Row r : result.collectAsList()) {
                MutationCountByGene mc = new MutationCountByGene();
                mc.setHugoGeneSymbol(r.getString(0));
                mc.setEntrezGeneId(Integer.valueOf(r.getString(1)));
                mc.setTotalCount(toIntExact(r.getLong(2)));
                mc.setCountByEntity(toIntExact(r.getLong(3)));
                toReturn.add(mc);
            }
            result.show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (client != null) {
                client.stop(true);
            }
        }
        return toReturn;
    }

    private List<MutationCountByGene> getSampleCountInMultipleMolecularProfilesDirect(List<String> molecularProfileIds,
                                                                                      List<String> sampleIds, List<Integer> entrezGeneIds) {
        List<MutationCountByGene> toReturn = new ArrayList();
        try {
            Dataset<Row> df = spark.read().parquet("file:///home/grossb/tmp/parquet-output-java/");
            df.createOrReplaceTempView("mutations");
            Dataset<Row> result = spark.sql("select first(Hugo_Symbol)," +
                                            "Entrez_Gene_Id," +
                                            "COUNT(*)," +
                                            "COUNT(DISTINCT(Tumor_Sample_Barcode)) " +
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
            System.out.println("!!!!! Error Spark-Parquet End   !!!!!");
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
