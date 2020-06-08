package org.mskcc.cbio.portal.pancancer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.ClinicalAttribute;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.model.TypeOfCancer;
import org.mskcc.cbio.portal.scripts.ImportCancerStudy;
import org.mskcc.cbio.portal.scripts.ImportClinicalData;
import org.mskcc.cbio.portal.scripts.ImportSampleList;
import org.mskcc.cbio.portal.scripts.ImportProfileData;
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.web_api.GetSampleLists;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestPanCancerStudySummaryImport {

    @Before
    public void setUp() throws DaoException {
        //clear cache to ensure this test is not affected by other tests (i.e. some tests add the same
        //samples to the DB and these remain in the cache after tests are done...if tests don't implement
        //teardown properly).
        DaoPatient.reCache();
        DaoSample.reCache();
        DaoGeneticProfile.reCache();
        DaoGeneOptimized.getInstance().reCache();
    }

    @After
    public void tearDown() {
        //clear any cached data:
        DaoPatient.reCache();
        DaoSample.reCache();
        DaoGeneticProfile.reCache();
        DaoGeneOptimized.getInstance().reCache();
    }

    @Test
    public void testPanCancerImport() throws Exception {
        ProgressMonitor.setConsoleMode(true);

        addGenes();

        // have to do add the pan_cancer cancer type first
        addCancerType("pan_cancer", "Pan-Cancer", "other", "Red", "PANCAN", "tissue");

        // ImportCancerStudy portal-study/meta_study.txt
        addCancerStudy("src/test/resources/panCancerStudySummary/meta_study.txt");

        //ImportClinicalData portal-study/data_clinical.txt multi_cancer_study
        addClinicalData("src/test/resources/panCancerStudySummary/meta_clinical.txt",
            "src/test/resources/panCancerStudySummary/data_clinical.txt");

        // ImportProfileData --meta portal-study/meta_CNA.txt --loadMode bulkload --data portal-study/data_CNA.txt
        addProfileData("src/test/resources/panCancerStudySummary/meta_CNA.txt", "src/test/resources/panCancerStudySummary/data_CNA.txt");

        // ImportProfileData --meta portal-study/meta_mutations_extended.txt --loadMode bulkload --data portal-study/data_mutations_extended.txt
        addProfileData("src/test/resources/panCancerStudySummary/meta_mutations_extended.txt", "src/test/resources/panCancerStudySummary/data_mutations_extended.txt");

        // ImportSampleList portal-study/case_lists/cases_all.txt
        // we're passing a single file, not the directory
        addSampleLists("src/test/resources/panCancerStudySummary/cases_all.txt");

        // test getCancerTypeInfo
        runChecksForGetCancerTypeInfo();

        // TODO : add more checks for profile data retrieved via pancancer queries (if any) 
    }

    private void addGenes() throws DaoException {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        //genes used in test data:
        daoGene.addGene(new CanonicalGene(999929974,"TESTA1CF"));
        daoGene.addGene(new CanonicalGene(999999992,"TESTA2M"));
        daoGene.addGene(new CanonicalGene(999144568,"TESTA2ML1"));
        daoGene.addGene(new CanonicalGene(999344752,"TESTAADACL2"));
        daoGene.addGene(new CanonicalGene(999922848,"TESTAAK1"));
        daoGene.addGene(new CanonicalGene(999132949,"TESTAASDH"));
        daoGene.addGene(new CanonicalGene(999910157,"TESTAASS"));
        daoGene.addGene(new CanonicalGene(999999919,"TESTABCA1"));
        daoGene.addGene(new CanonicalGene(999910058,"TESTABCB6"));
        daoGene.addGene(new CanonicalGene(999955289,"TESTACOXL"));
        daoGene.addGene(new CanonicalGene(999910880,"TESTACTL7B"));
    }

    private void addCancerType(String cancerID, String name, String keyword, String color, String shortName, String parentID) throws Exception{
        TypeOfCancer aTypeOfCancer = new TypeOfCancer();
        aTypeOfCancer.setTypeOfCancerId(cancerID);
        aTypeOfCancer.setName(name);
        aTypeOfCancer.setClinicalTrialKeywords(keyword);
        aTypeOfCancer.setDedicatedColor(color);
        aTypeOfCancer.setShortName(shortName);
        aTypeOfCancer.setParentTypeOfCancerId(parentID);
        DaoTypeOfCancer.addTypeOfCancer(aTypeOfCancer);
    }

    private void addCancerStudy(String fileName) throws Exception{
        ImportCancerStudy runner = new ImportCancerStudy(new String[]{fileName});
        runner.run();
    }

    private void addProfileData(String descriptorFileName, String dataFileName) throws Exception{
        try {
            ImportProfileData runner = new ImportProfileData(new String[]{"--meta", descriptorFileName, "--loadMode", "bulkload", "--data", dataFileName});
            runner.run();
        }
        catch (Throwable e) {
            //useful info for when this fails:
            ConsoleUtil.showMessages();
            throw e;
        }
    }

    private void addSampleLists(String fileName) throws Exception{
        ImportSampleList runner = new ImportSampleList(new String[]{fileName});
        runner.run();
    }

    private void addClinicalData(String descriptorFileName, String dataFileName) throws Exception{
        ImportClinicalData runner = new ImportClinicalData(new String[]{"--meta", descriptorFileName, "--loadMode", "bulkload", "--data", dataFileName});
        runner.run();
    }

    public void runChecksForGetCancerTypeInfo() throws Exception{
        String studyName="multi_cancer_study";
        CancerStudy study = DaoCancerStudy.getCancerStudyByStableId(studyName);
        List<Sample> samples = DaoSample.getSamplesByCancerStudy(study.getInternalId());
        List<String> sampleList = new ArrayList<>();
        for(Sample sample:samples){
            sampleList.add(sample.getStableId());
        }

        // retrieve the cancerTypeInfoMap for the study
        // this should contain:
        // 'CANCER_TYPE' - 'cns', 'cervix'
        // 'CANCER_TYPE_DETAILED' - 'byst', 'bimt', 'cead', 'cene', 'cacc'
        Map<String, Set<String>> cancerTypeInfoMap = DaoClinicalData.getCancerTypeInfoBySamples(sampleList);
        assertEquals(cancerTypeInfoMap.keySet().size(), 2);
        assertEquals(cancerTypeInfoMap.containsKey(ClinicalAttribute.CANCER_TYPE), true);
        assertEquals(cancerTypeInfoMap.containsKey(ClinicalAttribute.CANCER_TYPE_DETAILED), true);
        assertEquals(cancerTypeInfoMap.get(ClinicalAttribute.CANCER_TYPE).size(), 2);
        assertEquals(cancerTypeInfoMap.get(ClinicalAttribute.CANCER_TYPE_DETAILED).size(), 5);

    }
}
