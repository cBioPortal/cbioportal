
package org.mskcc.cgds.scripts;

import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoGeneticProfile;
import org.mskcc.cgds.dao.DaoProteinArrayData;
import org.mskcc.cgds.dao.MySQLbulkLoader;
import org.mskcc.cgds.model.ProteinArrayData;
import org.mskcc.cgds.util.ConsoleUtil;
import org.mskcc.cgds.util.FileUtil;
import org.mskcc.cgds.util.ProgressMonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.mskcc.cgds.model.GeneticAlterationType;
import org.mskcc.cgds.model.GeneticProfile;

/**
 *
 * @author jj
 */
public class ImportProteinArrayData {
    private ProgressMonitor pMonitor;
    private int cancerStudyId;
    private File arrayData;
    
    public ImportProteinArrayData(File arrayData, int cancerStudyId, ProgressMonitor pMonitor) {
        this.arrayData = arrayData;
        this.cancerStudyId = cancerStudyId;
        this.pMonitor = pMonitor;
    }
    
    public void importData() throws IOException, DaoException {
        MySQLbulkLoader.bulkLoadOff();
        
        DaoProteinArrayData daoPAD = DaoProteinArrayData.getInstance();
        daoPAD.deleteRecordsOfOneStudy(cancerStudyId);
        
        FileReader reader = new FileReader(arrayData);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine();
        String[] arrayIds = line.split("\t");
        
        while ((line=buf.readLine()) != null) {
            if (pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
            
            String[] strs = line.split("\t");
            String caseId = strs[0];
            for (int i=1; i<strs.length; i++) {
                double data = Double.parseDouble(strs[i]);
                ProteinArrayData pad = new ProteinArrayData(arrayIds[i], caseId, cancerStudyId, data);
                daoPAD.addProteinArrayData(pad);
            }
            
        }
        if (MySQLbulkLoader.isBulkLoad()) {
            //daoPAD.flushProteinArrayInfoesToDatabase();
        }
    }
    
    public static void main(String[] args) throws Exception {
//        DaoProteinArrayData daoPAD = DaoProteinArrayData.getInstance();
//        daoPAD.deleteAllRecords();
        if (args.length < 2) {
            System.out.println("command line usage:  importRPPAData.pl <RPPT_data.txt> <Cancer study identifier>");
            System.exit(1);
        }
        
        int cancerStudyId = DaoCancerStudy.getCancerStudyByStableId(args[1]).getInternalId();
        
        DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();
        String idProfProt = args[1]+"_RPPA_protein_level";
        if (daoGeneticProfile.getGeneticProfileByStableId(idProfProt)==null) {
            GeneticProfile gpPro = new GeneticProfile(idProfProt, cancerStudyId,
                    GeneticAlterationType.PROTEIN_ARRAY_PROTEIN_LEVEL, "RPPA protein level",
                    "Reverse phase protein array data (protein level)", true);
            daoGeneticProfile.addGeneticProfile(gpPro);
        }
        String idProfPhos = args[1]+"_RPPA_phosphorylation";
        if (daoGeneticProfile.getGeneticProfileByStableId(idProfPhos)==null) {
            GeneticProfile gpPhos = new GeneticProfile(idProfPhos, cancerStudyId,
                    GeneticAlterationType.PROTEIN_ARRAY_PHOSPHORYLATION, "RPPA phosphorylation",
                    "Reverse phase protein array data (phosphorylation)", false);
            daoGeneticProfile.addGeneticProfile(gpPhos);
        }
        
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);

        File file = new File(args[0]);
        System.out.println("Reading data from:  " + file.getAbsolutePath());
        int numLines = FileUtil.getNumLines(file);
        System.out.println(" --> total number of lines:  " + numLines);
        pMonitor.setMaxValue(numLines);
        ImportProteinArrayData parser = new ImportProteinArrayData(file, cancerStudyId, pMonitor);
        parser.importData();
        ConsoleUtil.showWarnings(pMonitor);
        System.err.println("Done.");
    }
}
