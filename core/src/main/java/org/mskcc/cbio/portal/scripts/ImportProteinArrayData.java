/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/


package org.mskcc.cbio.portal.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.FileUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;

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
        String[] caseIds = line.split("\t");
        ArrayList<String> cases = new ArrayList<String>();
        Pattern p = Pattern.compile("(TCGA-..-....)");
        for (int i=1; i<caseIds.length; i++) {
            String caseId = caseIds[i];
            Matcher m = p.matcher(caseId);
            if (m.find()) {
                cases.add(m.group(1));
            }
        }
        
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
                ProteinArrayData pad = new ProteinArrayData(cancerStudyId, arrayId, cases.get(i), zscores[i]);
                daoPAD.addProteinArrayData(pad);
            }
            
        }
        
        // import profile
        addRPPAProfile(cases);
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
        DaoGeneticProfileCases daoGeneticProfileCases = new DaoGeneticProfileCases();
        String idProfProt = cancerStudyStableId+"_RPPA_protein_level";
        if (DaoGeneticProfile.getGeneticProfileByStableId(idProfProt)==null) {
            GeneticProfile gpPro = new GeneticProfile(idProfProt, cancerStudyId,
													  GeneticAlterationType.PROTEIN_ARRAY_PROTEIN_LEVEL, "Z-SCORE",
													  "RPPA protein/phosphoprotein level",
													  "Protein or phosphoprotein level (Z-scores) measured by reverse phase protein array (RPPA)",
													  true);
            DaoGeneticProfile.addGeneticProfile(gpPro);
            daoGeneticProfileCases.addGeneticProfileCases(
                    DaoGeneticProfile.getGeneticProfileByStableId(idProfProt).getGeneticProfileId(), cases);
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
                                studyId, Collections.singleton(phosphoArray.getId()), cases);
                        ArrayList<ProteinArrayData> proteinData = daoPAD.getProteinArrayData(
                                studyId, Collections.singleton(proteinArray.getId()), cases);
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
                            ProteinArrayData norm = new ProteinArrayData(studyId, id, caseid, abud);
                            daoPAD.addProteinArrayData(norm);
                        }
                        
                        //break;
                    }
                }
            }
        }
    }
}
