
package org.mskcc.cgds.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import org.mskcc.cgds.dao.*;
import org.mskcc.cgds.model.*;
import org.mskcc.cgds.util.ConsoleUtil;
import org.mskcc.cgds.util.FileUtil;
import org.mskcc.cgds.util.ProgressMonitor;

/**
 * Import protein array data into database
 * @author jj
 */
public class ImportProteinArrayData {
    private ProgressMonitor pMonitor;
    private int cancerStudyId;
    private String cancerStudyStableId;
    private File arrayData;
    
    public ImportProteinArrayData(File arrayData, int cancerStudyId, 
            String cancerStudyStableId, ProgressMonitor pMonitor) {
        this.arrayData = arrayData;
        this.cancerStudyId = cancerStudyId;
        this.cancerStudyStableId = cancerStudyStableId;
        this.pMonitor = pMonitor;
    }
    
    /**
     * Import RPPA data. Profiles and a case list will also be added here.
     * @throws IOException
     * @throws DaoException 
     */
    public void importData() throws IOException, DaoException {
        // import array data
        DaoProteinArrayData daoPAD = DaoProteinArrayData.getInstance();
        DaoProteinArrayInfo daoPAI = DaoProteinArrayInfo.getInstance();
        
        FileReader reader = new FileReader(arrayData);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine();
        String[] caseIds = line.split("\t");
        ArrayList<String> cases = new ArrayList<String>();
        for (int i=1; i<caseIds.length; i++) {
            cases.add(caseIds[i]);
        }
        
        while ((line=buf.readLine()) != null) {
            if (pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
            
            String[] strs = line.split("\t");
            String arrayId = strs[0];
            if (daoPAI.getProteinArrayInfo(arrayId)==null) {
                System.err.println("missing protein array information of " + arrayId
                        + ". Please load antibody annotation.");
            }
            daoPAI.addProteinArrayCancerStudy(arrayId, Collections.singleton(cancerStudyId));
            
            for (int i=1; i<strs.length; i++) {
                double data = Double.parseDouble(strs[i]);
                ProteinArrayData pad = new ProteinArrayData(arrayId, caseIds[i], data);
                daoPAD.addProteinArrayData(pad);
            }
            
        }
        
        // import profile
        addRPPAProfile(cases);
        
        // import case list
        addRPPACaseList(cases);
    }
    
    private void addRPPACaseList(ArrayList<String> cases) throws DaoException {
        DaoCaseList daoCaseList = new DaoCaseList();
        if (daoCaseList.getCaseListByStableId(cancerStudyStableId+"_RPPA")!=null) {
            return;
        }
        
        CaseList caseList = new CaseList(cancerStudyStableId+"_RPPA",-1,cancerStudyId,"Tumors with RPPA data",
                CaseListCategory.ALL_CASES_WITH_RPPA_DATA);
        caseList.setDescription("All tumor samples with protein/phosphoprotein levels determined by " +
                "reverse phase protein array.");
        caseList.setCaseList(cases);
        
        daoCaseList.addCaseList(caseList);
    }
    
    private void addRPPAProfile(ArrayList<String> cases) throws DaoException {
        // add profile
        DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();
        DaoGeneticProfileCases daoGeneticProfileCases = new DaoGeneticProfileCases();
        String idProfProt = cancerStudyStableId+"_RPPA_protein_level";
        if (daoGeneticProfile.getGeneticProfileByStableId(idProfProt)==null) {
            GeneticProfile gpPro = new GeneticProfile(idProfProt, cancerStudyId,
                    GeneticAlterationType.PROTEIN_ARRAY_PROTEIN_LEVEL, "RPPA protein/phosphoprotein level",
                    "Protein or phosphoprotein level measured by reverse phase protein array (RPPA)", true);
            daoGeneticProfile.addGeneticProfile(gpPro);
            daoGeneticProfileCases.addGeneticProfileCases(
                    daoGeneticProfile.getGeneticProfileByStableId(idProfProt).getGeneticProfileId(), cases);
        }
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("command line usage:  importRPPAData.pl <RPPA_data.txt> <Cancer study identifier>");
            System.exit(1);
        }
        
        int cancerStudyId = DaoCancerStudy.getCancerStudyByStableId(args[1]).getInternalId();
        
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        File file = new File(args[0]);
        System.out.println("Reading data from:  " + file.getAbsolutePath());
        int numLines = FileUtil.getNumLines(file);
        System.out.println(" --> total number of lines:  " + numLines);
        pMonitor.setMaxValue(numLines);
        ImportProteinArrayData parser = new ImportProteinArrayData(file, cancerStudyId, args[1], pMonitor);
        parser.importData();
        ConsoleUtil.showWarnings(pMonitor);
        System.err.println("Done.");
    }
    
    /**
     * add extra antibodies of normalized phosphoprotein data
     * @param args
     * @throws Exception 
     */
    public static void main_normalize_phospho(String[] args) throws Exception {
        DaoProteinArrayData daoPAD = DaoProteinArrayData.getInstance();
        DaoProteinArrayInfo daoPAI = DaoProteinArrayInfo.getInstance();
        DaoProteinArrayTarget daoPAT = DaoProteinArrayTarget.getInstance();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        DaoCaseList daoCaseList = new DaoCaseList();
        ArrayList<CancerStudy> studies = DaoCancerStudy.getAllCancerStudies();
        for (CancerStudy study : studies) {
            int studyId = study.getInternalId();
            CaseList caselist = daoCaseList.getCaseListByStableId(study.getCancerStudyStableId()+"_RPPA");
            if (caselist==null) continue;
            ArrayList<String> cases = caselist.getCaseList();
            ArrayList<ProteinArrayInfo> phosphoArrays = daoPAI.getProteinArrayInfoForType(
                    studyId, Collections.singleton("phosphorylation"));
            ArrayList<ProteinArrayInfo> proteinArrays = daoPAI.getProteinArrayInfoForType(
                    studyId, Collections.singleton("protein_level"));
            for (ProteinArrayInfo phosphoArray : phosphoArrays) {
                for (ProteinArrayInfo proteinArray : proteinArrays) {
                    if (proteinArray.getGene().equals(phosphoArray.getGene())) {
                        String id = phosphoArray.getId()+"-"+proteinArray.getId();
                        if (id.matches(".+-.+-.+")) continue;
                        System.out.println(study.getCancerStudyStableId()+" "+id+" "
                                +phosphoArray.getGene()+" "+phosphoArray.getResidue());
                        ProteinArrayInfo pai = new ProteinArrayInfo(id,"phosphorylation",
                                phosphoArray.getSource(),phosphoArray.getGene(),
                                phosphoArray.getResidue(),phosphoArray.isValidated(),null);
                        daoPAI.addProteinArrayInfo(pai);
                        for (String symbol : phosphoArray.getGene().split("/")) {
                            CanonicalGene gene = daoGene.getNonAmbiguousGene(symbol);
                            if (gene==null) {
                                System.err.println(symbol+" not exist");
                                continue;
                            }

                            long entrez = gene.getEntrezGeneId();
                            try {
                                daoPAT.addProteinArrayTarget(id, entrez);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        daoPAI.addProteinArrayCancerStudy(id, Collections.singleton(studyId));
                        
                        ArrayList<ProteinArrayData> phosphoData = daoPAD.getProteinArrayData(
                                Collections.singleton(phosphoArray.getId()), cases);
                        ArrayList<ProteinArrayData> proteinData = daoPAD.getProteinArrayData(
                                Collections.singleton(proteinArray.getId()), cases);
                        HashMap<String,ProteinArrayData> mapProteinData = new HashMap<String,ProteinArrayData>();
                        for (ProteinArrayData pad : proteinData) {
                            mapProteinData.put(pad.getCaseId(), pad);
                        }
                        
                        for (ProteinArrayData pad : phosphoData) {
                            String caseid = pad.getCaseId();
                            ProteinArrayData proteinPAD = mapProteinData.get(caseid);
                            if (proteinPAD==null) {
                                System.err.println("no data: "+proteinPAD.getArrayId()+" "+caseid);
                                continue;
                            }
                            double abud = pad.getAbundance() - proteinPAD.getAbundance(); // minus
                            ProteinArrayData norm = new ProteinArrayData(id, caseid, abud);
                            daoPAD.addProteinArrayData(norm);
                        }
                        
                        //break;
                    }
                }
            }
        }
    }
}
