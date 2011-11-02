
package org.mskcc.cgds.scripts;

import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoCaseList;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneticProfile;
import org.mskcc.cgds.dao.DaoGeneticProfileCases;
import org.mskcc.cgds.dao.DaoProteinArrayInfo;
import org.mskcc.cgds.dao.DaoProteinArrayData;
import org.mskcc.cgds.dao.MySQLbulkLoader;
import org.mskcc.cgds.model.ProteinArrayData;
import org.mskcc.cgds.model.CaseList;
import org.mskcc.cgds.model.GeneticAlterationType;
import org.mskcc.cgds.model.GeneticProfile;
import org.mskcc.cgds.util.ConsoleUtil;
import org.mskcc.cgds.util.FileUtil;
import org.mskcc.cgds.util.ProgressMonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;

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
        MySQLbulkLoader.bulkLoadOff();
        
        // import array data
        DaoProteinArrayData daoPAD = DaoProteinArrayData.getInstance();
        DaoProteinArrayInfo daoPAI = DaoProteinArrayInfo.getInstance();
        
        FileReader reader = new FileReader(arrayData);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine();
        String[] arrayIds = line.split("\t");
        for (int i=1; i<arrayIds.length; i++) {
            if (daoPAI.getProteinArrayInfo(arrayIds[i])==null) {
                System.err.println("missing protein array information of " + arrayIds[i]
                        + ". Please load antibody annotation.");
            }
            daoPAI.addProteinArrayCancerStudy(arrayIds[i], Collections.singleton(cancerStudyId));
        }
        
        ArrayList<String> cases = new ArrayList<String>();
        
        while ((line=buf.readLine()) != null) {
            if (pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
            
            String[] strs = line.split("\t");
            String caseId = strs[0];
            cases.add(caseId);
            for (int i=1; i<strs.length; i++) {
                double data = Double.parseDouble(strs[i]);
                ProteinArrayData pad = new ProteinArrayData(arrayIds[i], caseId, data);
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
        if (daoCaseList.getCaseListByStableId(cancerStudyStableId+"_RPPA")!=null)
            return;
        
        CaseList caseList = new CaseList(cancerStudyStableId+"_RPPA",-1,cancerStudyId,"All tumors with RPPA data");
        caseList.setDescription("All tumor samples with protein/phosphoprotein levels determined by reverse phase protein array.");
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
                    GeneticAlterationType.PROTEIN_ARRAY_PROTEIN_LEVEL, "RPPA protein level",
                    "Reverse phase protein array data (protein level)", true);
            daoGeneticProfile.addGeneticProfile(gpPro);
            daoGeneticProfileCases.addGeneticProfileCases(
                    daoGeneticProfile.getGeneticProfileByStableId(idProfProt).getGeneticProfileId(), cases);
        }
        
        String idProfPhos = cancerStudyStableId+"_RPPA_phosphorylation";
        if (daoGeneticProfile.getGeneticProfileByStableId(idProfPhos)==null) {
            GeneticProfile gpPhos = new GeneticProfile(idProfPhos, cancerStudyId,
                    GeneticAlterationType.PROTEIN_ARRAY_PHOSPHORYLATION, "RPPA phosphorylation",
                    "Reverse phase protein array data (phosphorylation)", false);
            daoGeneticProfile.addGeneticProfile(gpPhos);
            daoGeneticProfileCases.addGeneticProfileCases(
                    daoGeneticProfile.getGeneticProfileByStableId(idProfPhos).getGeneticProfileId(), cases);
        }
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("command line usage:  importRPPAData.pl <RPPT_data.txt> <Cancer study identifier>");
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
}
