/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/


package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import java.io.*;
import java.util.*;
import java.util.regex.*;

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
        
        FileReader reader = new FileReader(arrayData);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine();
        String[] sampleIds = line.split("\t");

        CancerStudy cancerStudy = DaoCancerStudy.getCancerStudyByInternalId(cancerStudyId);
        ImportDataUtil.addPatients(sampleIds, cancerStudy);
        ImportDataUtil.addSamples(sampleIds, cancerStudy);
        
        ArrayList<Integer> internalSampleIds = new ArrayList<Integer>();
        while ((line=buf.readLine()) != null) {
            if (pMonitor != null) {
                pMonitor.incrementCurValue();
                ConsoleUtil.showProgress(pMonitor);
            }
            
            String[] strs = line.split("\t");
            String arrayInfo = strs[0];
            String arrayId = importArrayInfo(arrayInfo);
           
            double[] zscores = convertToZscores(strs);
            for (int i=0; i<zscores.length; i++) {
                Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudyId, StableIdUtil.getSampleId(sampleIds[i]));
                ProteinArrayData pad = new ProteinArrayData(cancerStudyId, arrayId, sample.getInternalId(), zscores[i]);
                daoPAD.addProteinArrayData(pad);
                internalSampleIds.add(sample.getInternalId());
            }
            
        }
        
        // import profile
        addRPPAProfile(internalSampleIds);
    }

    private double[] convertToZscores(String[] strs) {
        double[] data = new double[strs.length-1];
        for (int i=1; i<strs.length; i++) {
            data[i-1] = Double.parseDouble(strs[i]);
        }
        
        DescriptiveStatistics ds = new DescriptiveStatistics(data);
        double mean = ds.getMean();
        double std = ds.getStandardDeviation();
        
        for (int i=0; i<data.length; i++) {
            data[i] = (data[i]-mean)/std;
        }
        return data;
    }
    
    private String importArrayInfo(String info) throws DaoException {
        DaoProteinArrayInfo daoPAI = DaoProteinArrayInfo.getInstance();
        DaoProteinArrayTarget daoPAT = DaoProteinArrayTarget.getInstance();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        
        String[] parts = info.split("\\|");
        String[] genes = parts[0].split(" ");
        fixSymbols(genes);
        String arrayId = parts[1];
        
        Pattern p = Pattern.compile("(p[STY][0-9]+)");
        Matcher m = p.matcher(arrayId);
        String type, residue;
        if (m.find()) {
            type = "phosphorylation";
            residue = m.group(1);
            importPhosphoGene(genes, residue, arrayId);
        } else {
            type = "protein_level";
            p = Pattern.compile("(cleaved[A-Z][0-9]+)");
            m = p.matcher(arrayId);
            residue = m.find() ? m.group(1) : null;
            importRPPAProteinAlias(genes);
        }
        
        if (daoPAI.getProteinArrayInfo(arrayId)==null) {
            ProteinArrayInfo pai = new ProteinArrayInfo(arrayId, type,  
                            StringUtils.join(genes, "/"), residue, null);
            daoPAI.addProteinArrayInfo(pai);
            for (String symbol : genes) {
                CanonicalGene gene = daoGene.getNonAmbiguousGene(symbol);
                if (gene==null) {
                    System.err.println(symbol+" not exist");
                    continue;
                }

                long entrez = gene.getEntrezGeneId();
                daoPAT.addProteinArrayTarget(arrayId, entrez);
            }
        }
        
        if (!daoPAI.proteinArrayCancerStudyAdded(arrayId, cancerStudyId)) {
            daoPAI.addProteinArrayCancerStudy(arrayId, Collections.singleton(cancerStudyId));
        }
        
        return arrayId;
    }
    
    private void fixSymbols(String[] genes) {
        int n = genes.length;
        for (int i=0; i<n; i++) {
            if (genes[i].equalsIgnoreCase("CDC2")) {
                genes[i] = "CDK1";
            }
        }
    }
    
    private void importPhosphoGene(String[] genes, String residue, String arrayId) throws DaoException {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        String phosphoSymbol = StringUtils.join(genes, "/")+"_"+residue;

        Set<String> aliases = new HashSet<String>();
        aliases.add(arrayId);
        aliases.add("rppa-phospho");
        aliases.add("phosphoprotein");
        for (String gene : genes) {
            aliases.add("phospho"+gene);
        }

        CanonicalGene phosphoGene = new CanonicalGene(phosphoSymbol, aliases);
        phosphoGene.setType(CanonicalGene.PHOSPHOPROTEIN_TYPE);
        daoGene.addGene(phosphoGene);
    }
    
    private void importRPPAProteinAlias(String[] genes) throws DaoException {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        
        for (String gene : genes) {
            CanonicalGene existingGene = daoGene.getGene(gene);
            if (existingGene!=null) {
                Set<String> aliases = new HashSet<String>();
                aliases.add("rppa-protein");
                existingGene.setAliases(aliases);
                daoGene.addGene(existingGene);
            }
        }
    }
    
    private void addRPPAProfile(ArrayList<Integer> sampleIds) throws DaoException {
        // add profile
        String idProfProt = cancerStudyStableId+"_RPPA_protein_level";
        if (DaoGeneticProfile.getGeneticProfileByStableId(idProfProt)==null) {
            GeneticProfile gpPro = new GeneticProfile(idProfProt, cancerStudyId,
													  GeneticAlterationType.PROTEIN_ARRAY_PROTEIN_LEVEL, "Z-SCORE",
													  "protein/phosphoprotein level (RPPA)",
													  "Protein or phosphoprotein level (Z-scores) measured by reverse phase protein array (RPPA)",
													  true);
            DaoGeneticProfile.addGeneticProfile(gpPro);
            DaoGeneticProfileSamples.addGeneticProfileSamples(
                    DaoGeneticProfile.getGeneticProfileByStableId(idProfProt).getGeneticProfileId(), sampleIds);
        }
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("command line usage:  importRPPAData.pl <RPPA_data.txt> <Cancer study identifier>");
            return;
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
        DaoPatientList daoPatientList = new DaoPatientList();
        ArrayList<CancerStudy> studies = DaoCancerStudy.getAllCancerStudies();
        for (CancerStudy study : studies) {
            int studyId = study.getInternalId();
            PatientList patientlist = daoPatientList.getPatientListByStableId(study.getCancerStudyStableId()+"_RPPA");
            if (patientlist==null) continue;
            List<Integer> sampleIds = InternalIdUtil.getInternalSampleIds(studyId, patientlist.getPatientList());
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
                                phosphoArray.getGene(),phosphoArray.getResidue(),null);
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
                                studyId, Collections.singleton(phosphoArray.getId()), sampleIds);
                        ArrayList<ProteinArrayData> proteinData = daoPAD.getProteinArrayData(
                                studyId, Collections.singleton(proteinArray.getId()), sampleIds);
                        HashMap<Integer,ProteinArrayData> mapProteinData = new HashMap<Integer,ProteinArrayData>();
                        for (ProteinArrayData pad : proteinData) {
                            mapProteinData.put(pad.getSampleId(), pad);
                        }
                        
                        for (ProteinArrayData pad : phosphoData) {
                            Integer sampleId = pad.getSampleId();
                            ProteinArrayData proteinPAD = mapProteinData.get(sampleId);
                            if (proteinPAD==null) {
                                System.err.println("no data: "+proteinPAD.getArrayId()+" "+sampleId);
                                continue;
                            }
                            double abud = pad.getAbundance() - proteinPAD.getAbundance(); // minus
                            ProteinArrayData norm = new ProteinArrayData(studyId, id, sampleId, abud);
                            daoPAD.addProteinArrayData(norm);
                        }
                        
                        //break;
                    }
                }
            }
        }
    }
}
