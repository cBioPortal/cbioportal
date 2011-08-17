package org.mskcc.portal.tool;

import org.mskcc.portal.tool.bundle.BundleFactory;
import org.mskcc.portal.tool.bundle.Bundle;
import org.mskcc.portal.tool.bundle.GeneRequest;
import org.mskcc.portal.model.ProfileData;
import org.mskcc.portal.model.ExtendedMutation;
import org.mskcc.portal.remote.GetGeneticProfiles;
import org.mskcc.portal.remote.GetCaseSets;
import org.mskcc.portal.remote.GetProfileData;
import org.mskcc.portal.remote.GetMutationData;
import org.mskcc.portal.util.XDebug;
import org.mskcc.portal.util.GlobalProperties;
import org.mskcc.cgds.model.CaseList;
import org.mskcc.cgds.model.GeneticProfile;
import org.mskcc.cgds.model.GeneticAlterationType;
import org.mskcc.cgds.dao.DaoException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class ExecuteBundle {
    private static final String OVARIAN_CANCER = "ova";
    private static NumberFormat percentFormat = DecimalFormat.getPercentInstance();

    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException, IOException, DaoException {

        XDebug xdebug = new XDebug();
        GlobalProperties.setCgdsUrl("http://localhost:8080/cgds-public/webservice.do");

        System.out.println ("Retrieving genetic profiles...");

        //  Get Genetic Profiles
        ArrayList<GeneticProfile> profileList = GetGeneticProfiles.getGeneticProfiles
                (OVARIAN_CANCER);

        //  Get Case Sets
        System.out.println ("Retrieving case sets...");
        ArrayList<CaseList> caseSetList = GetCaseSets.getCaseSets(OVARIAN_CANCER);

        Bundle bundle = BundleFactory.createBundle("BRCA");

        ArrayList<GeneRequest> geneRequestList = bundle.getGeneRequestList();
        String caseIds = getCaseIds (bundle.getCaseSetId(), caseSetList);
        ArrayList <String> caseIdList = getCaseList (bundle.getCaseSetId(), caseSetList);

        if (caseIds == null) {
            throw new NullPointerException ("Case Set ID:  " + bundle.getCaseSetId()
                + " does not exist.");
        }

        HashMap <String, ArrayList<ExtendedMutation>> mutationMap = new HashMap<String, ArrayList<ExtendedMutation>>();
        HashMap <String, ProfileData> cnaMap = new HashMap<String, ProfileData>();
        HashMap <String, ProfileData> binaryMethylationMap = new HashMap<String, ProfileData>();

        //  Get the Genomic Data
        for (GeneRequest geneRequest: geneRequestList) {
            GeneticProfile geneticProfile = getProfile(geneRequest.getGenomicProfileId(), profileList);
            if (geneticProfile == null) {
                throw new NullPointerException ("Genetic Profile ID:  "
                        + geneRequest.getGenomicProfileId()
                        + " does not exist.");
            }
            System.out.println("Retrieving data for:  " + geneRequest.getGeneSymbol()
                    + ":  " + geneticProfile.getProfileName() + ", " + geneRequest.getAlterationType());
            ArrayList <String> geneList = new ArrayList <String>();
            geneList.add(geneRequest.getGeneSymbol());
            if (geneticProfile.getGeneticAlterationType()
                    == GeneticAlterationType.MUTATION_EXTENDED) {
                GetMutationData remoteCall = new GetMutationData();
                ArrayList <ExtendedMutation> mutationList =
                        remoteCall.getMutationData(geneticProfile, geneList, caseIds, xdebug);
                mutationMap.put(geneRequest.getGeneSymbol(), mutationList);
            } else if (geneticProfile.getGeneticAlterationType()
                    == GeneticAlterationType.COPY_NUMBER_ALTERATION){
                GetProfileData remoteCall = new GetProfileData();
                ProfileData pData = remoteCall.getProfileData(geneticProfile, geneList, caseIds, xdebug);
                cnaMap.put (geneRequest.getGeneSymbol(), pData);
            } else if (geneticProfile.getGeneticAlterationType()
                    == GeneticAlterationType.METHYLATION_BINARY) {
                GetProfileData remoteCall = new GetProfileData();
                ProfileData pData = remoteCall.getProfileData(geneticProfile, geneList, caseIds, xdebug);
                binaryMethylationMap.put (geneRequest.getGeneSymbol(), pData);
            } else {
                System.out.println ("Cannot process:  " + geneticProfile.getGeneticAlterationType());
            }
        }

        int counter = 0;

        FileWriter caseWriter = new FileWriter ("cases.txt");
        ArrayList <BinnedCase> binnedCaseList = new ArrayList<BinnedCase>();
        for (String caseId: caseIdList) {
            int accept = bundle.binAccept(caseId, mutationMap, cnaMap, binaryMethylationMap);
            if (accept > 0) {
                binnedCaseList.add(new BinnedCase(caseId, accept));
                counter++;
                caseWriter.write(caseId + "\n");
            }
        }
        caseWriter.close();
        
        double percent = counter / (double) caseIdList.size();
        System.out.println ("total number of cases that pass:  " + counter
                + " (" + percentFormat.format(percent) +")");
        Collections.sort(binnedCaseList, new BinnedCaseComparator());

        FingerPrint fingerprint = new FingerPrint (bundle, binnedCaseList, mutationMap, cnaMap,
                binaryMethylationMap);
        String svg = fingerprint.getSvg();
        FileWriter writer = new FileWriter("fingerprint.svg");
        writer.write(svg);
        writer.flush();
        writer.close();

        String summary = bundle.getSummary();
        System.out.println ("\n\nSummary:\n---------------------");
        System.out.println (summary);
        System.out.println ("Fingerprint written to fingerpint.svg");
    }


    private static GeneticProfile getProfile(String profileId,
                                             ArrayList<GeneticProfile> profileList) {
        for (GeneticProfile profile : profileList) {
            if (profile.getStableId().equals(profileId)) {
                return profile;
            }
        }
        return null;
    }

    private static String getCaseIds (String caseSetId, ArrayList<CaseList> caseSetList) {
        for (CaseList caseSet : caseSetList) {
            if (caseSet.getStableId().equals(caseSetId)) {
                return caseSet.getCaseListAsString();
            }
        }
        return null;
    }

    private static ArrayList<String> getCaseList
            (String caseSetId, ArrayList<CaseList> caseSetList) {
        for (CaseList caseSet : caseSetList) {
            if (caseSet.getStableId().equals(caseSetId)) {
                return caseSet.getCaseList();
            }
        }
        return null;
    }
}

class BinnedCase {
    private String caseId;
    private int binNumber;

    public BinnedCase (String caseId, int binNumber) {
        this.caseId = caseId;
        this.binNumber = binNumber;
    }

    public String getCaseId() {
        return caseId;
    }

    public int getBinNumber() {
        return binNumber;
    }
}

class BinnedCaseComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        BinnedCase case0 = (BinnedCase) o1;
        BinnedCase case1 = (BinnedCase) o2;
        return new Integer(case0.getBinNumber()).compareTo(case1.getBinNumber());
    }
}