package org.mskcc.mondrian.client;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * @author Arman Aksoy
 * @author Dazhi Jiao
 */

public class CBioPortalClient {
    protected final static String PORTAL_URL = "http://www.cbioportal.org/public-portal/webservice.do?";
    protected final static String COMMAND = "cmd=";
    protected final static String DELIMITER = "\t";

    private List<CancerStudy> cancerStudies = new ArrayList<CancerStudy>();
    private CancerStudy currentCancerStudy = null;

    private Map<CancerStudy, List<GeneticProfile>> geneticProfilesCache
            = new HashMap<CancerStudy, List<GeneticProfile>>();
    private Map<CancerStudy, List<CaseList>> caseListCache = new HashMap<CancerStudy, List<CaseList>>();
    private CaseList currentCaseList = null;
    private List<GeneticProfile> currentGeneticProfiles = new ArrayList<GeneticProfile>();

    public CBioPortalClient() throws IOException {
        initializeStudies();
        assert !cancerStudies.isEmpty();
        setCurrentCancerStudy(cancerStudies.get(0));
    }

    private void initializeStudies() throws IOException {
        String urlStr = "getCancerStudies";

        for (String[] result : parseURL(urlStr)) {
            assert result.length == 3;
            cancerStudies.add(new CancerStudy(result[0], result[1], result[2]));
        }
    }
	
    public List<CaseList> getCaseListsForCurrentStudy() throws IOException {
        List<CaseList> caseLists = caseListCache.get(getCurrentCancerStudy());
        if(caseLists != null)
            return caseLists;

        caseLists = new ArrayList<CaseList>();
        String url = "getCaseLists&cancer_study_id=" + getCurrentCancerStudy().getStudyId();
        for (String[] results : parseURL(url)) {
            assert results.length == 5;
            String[] cases = results[4].split(" ");
            assert cases.length > 1;

            caseLists.add(new CaseList(results[0], results[1], results[2], cases));
        }

        caseListCache.put(getCurrentCancerStudy(), caseLists);
        return caseLists;
    }

    private List<String[]> parseURL(String urlPostFix) throws IOException {
        return parseURL(urlPostFix, true);
    }

    private List<String[]> parseURL(String urlPostFix, boolean skipHeader) throws IOException {
        List<String[]> list = new ArrayList<String[]>();

        String urlStr = PORTAL_URL + COMMAND + urlPostFix;
        URL url = new URL(urlStr);
        URLConnection urlConnection = url.openConnection();
        Scanner scanner = new Scanner(urlConnection.getInputStream());

        int lineNum = 0;
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            lineNum++;

            if(line.startsWith("#") || line.length() == 0 || (skipHeader && lineNum == 2))
                continue;

            list.add(line.split(DELIMITER));
        }

        return list;
    }

    public List<CancerStudy> getCancerStudies() {
        return cancerStudies;
    }

    public CancerStudy getCurrentCancerStudy() {
        return currentCancerStudy;
    }

    public void setCurrentCancerStudy(CancerStudy currentCancerStudy) {
        if(!cancerStudies.contains(currentCancerStudy))
            throw new IllegalArgumentException("This cancer study is not available through the initialized list.");

        this.currentCancerStudy = currentCancerStudy;
        setCurrentCaseList(null);
        getCurrentGeneticProfiles().clear();
    }

    public List<GeneticProfile> getGeneticProfilesForCurrentStudy() throws IOException {
        List<GeneticProfile> geneticProfiles = geneticProfilesCache.get(getCurrentCancerStudy());
        if(geneticProfiles != null)
            return geneticProfiles;

        geneticProfiles = new ArrayList<GeneticProfile>();

        String url = "getGeneticProfiles" + "&cancer_study_id=" + getCurrentCancerStudy().getStudyId();
        for (String[] results : parseURL(url)) {
            assert results.length == 6;
            geneticProfiles.add(new GeneticProfile(results[0], results[1], results[2], results[4]));
        }

        assert !geneticProfiles.isEmpty();
        geneticProfilesCache.put(getCurrentCancerStudy(), geneticProfiles);
        return geneticProfiles;
    }

    public void setCurrentCaseList(CaseList caseList) {
        try {
            List<CaseList> caseListsForCurrentStudy = getCaseListsForCurrentStudy();
            if(caseList == null || caseListsForCurrentStudy.contains(caseList)) {
                currentCaseList = caseList;
            } else {
                throw new IllegalArgumentException("The case list is not available for current cancer study.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot obtain the case lists for the current study.");
        }
    }

    public CaseList getCurrentCaseList() {
        return currentCaseList;
    }

    public void setCurrentGeneticProfiles(List<GeneticProfile> geneticProfiles) {
        currentGeneticProfiles = geneticProfiles;
    }

    public List<GeneticProfile> getCurrentGeneticProfiles() {
        return currentGeneticProfiles;
    }
    
    public DataTypeMatrix getProfileData(CaseList caseList, List<GeneticProfile> profiles, String gene) {
    	throw new UnsupportedOperationException("Method not implemented yet");
    }
    
    public DataTypeMatrix getProfileData(CaseList caseList, GeneticProfile profile, List<String> genes) throws IOException {
    	String urlStr = PORTAL_URL + COMMAND + "getProfileData&case_set_id=" + caseList.getId() + "&genetic_profile_id=" + profile.getId() + "&gene_list=";
    	Iterator<String> it = genes.iterator();
    	while (it.hasNext()) {
    		urlStr += it.next();
    		if (it.hasNext()) urlStr+= "+";
    	}
        URL url = new URL(urlStr);
        URLConnection urlConnection = url.openConnection();    
        return new DataTypeMatrix(urlConnection.getInputStream(), DataTypeMatrix.DATA_TYPE.GENETIC_PROFILE_MULTI_GENE, profile.getType());
    }
    
    public List<ClinicalData> getClinicalData(CaseList caseList) {
    	
    	return null;
    }
}