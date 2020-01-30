/*
 * Copyright (c) 2015 - 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mskcc.cbio.portal.scripts;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

/**
 * Import protein array data into database
 * @author jj
 */
public class ImportProteinArrayData {
    private int cancerStudyId;
    private String cancerStudyStableId;
    private File arrayData;

    public ImportProteinArrayData(
        File arrayData,
        int cancerStudyId,
        String cancerStudyStableId
    ) {
        this.arrayData = arrayData;
        this.cancerStudyId = cancerStudyId;
        this.cancerStudyStableId = cancerStudyStableId;
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
        GeneticProfile profile = addRPPAProfile();
        FileReader reader = new FileReader(arrayData);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine();
        String[] sampleIds = line.split("\t");
        Sample[] samples = new Sample[sampleIds.length - 1];
        for (int i = 1; i < sampleIds.length; i++) {
            samples[i - 1] =
                DaoSample.getSampleByCancerStudyAndSampleId(
                    cancerStudyId,
                    StableIdUtil.getSampleId(sampleIds[i])
                );
        }
        ArrayList<Integer> internalSampleIds = new ArrayList<Integer>();
        while ((line = buf.readLine()) != null) {
            ProgressMonitor.incrementCurValue();
            ConsoleUtil.showProgress();
            String[] strs = line.split("\t");
            String arrayInfo = strs[0];
            String arrayId = importArrayInfo(arrayInfo);
            double[] zscores = convertToZscores(strs);
            for (int i = 0; i < zscores.length; i++) {
                if (samples[i] == null || Double.isNaN(zscores[i])) {
                    continue;
                }
                int sampleId = samples[i].getInternalId();
                ProteinArrayData pad = new ProteinArrayData(
                    cancerStudyId,
                    arrayId,
                    sampleId,
                    zscores[i]
                );
                daoPAD.addProteinArrayData(pad);
                internalSampleIds.add(sampleId);
            }
        }
        // add samples to profile
        DaoGeneticProfileSamples.addGeneticProfileSamples(
            profile.getGeneticProfileId(),
            internalSampleIds
        );
    }

    private double[] convertToZscores(String[] strs) {
        double[] data = new double[strs.length - 1];
        boolean nan = false;
        for (int i = 1; i < strs.length; i++) { // ignore the first column
            try {
                data[i - 1] = Double.parseDouble(strs[i]);
            } catch (Exception e) {
                data[i - 1] = Double.NaN;
                nan = true;
            }
        }
        DescriptiveStatistics ds = new DescriptiveStatistics(
            nan ? copyWithNoNaN(data) : data
        );
        double mean = ds.getMean();
        double std = ds.getStandardDeviation();
        for (int i = 0; i < data.length; i++) {
            if (!Double.isNaN(data[i])) {
                data[i] = (data[i] - mean) / std;
            }
        }
        return data;
    }

    private double[] copyWithNoNaN(double[] data) {
        List<Double> list = new ArrayList<Double>();
        for (double d : data) {
            if (!Double.isNaN(d)) {
                list.add(d);
            }
        }
        double[] ret = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            ret[i] = list.get(i);
        }
        return ret;
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

        if (daoPAI.getProteinArrayInfo(arrayId) == null) {
            ProteinArrayInfo pai = new ProteinArrayInfo(
                arrayId,
                type,
                StringUtils.join(genes, "/"),
                residue,
                null
            );
            daoPAI.addProteinArrayInfo(pai);
            for (String symbol : genes) {
                CanonicalGene gene = daoGene.getNonAmbiguousGene(symbol, true);
                if (gene == null) {
                    System.err.println(symbol + " not exist");
                    continue;
                }

                long entrez = gene.getEntrezGeneId();
                daoPAT.addProteinArrayTarget(arrayId, entrez);
            }
        }

        if (!daoPAI.proteinArrayCancerStudyAdded(arrayId, cancerStudyId)) {
            daoPAI.addProteinArrayCancerStudy(
                arrayId,
                Collections.singleton(cancerStudyId)
            );
        }

        return arrayId;
    }

    private void fixSymbols(String[] genes) {
        int n = genes.length;
        for (int i = 0; i < n; i++) {
            if (genes[i].equalsIgnoreCase("CDC2")) {
                genes[i] = "CDK1";
            }
        }
    }

    private void importPhosphoGene(
        String[] genes,
        String residue,
        String arrayId
    )
        throws DaoException {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        String phosphoSymbol = StringUtils.join(genes, "/") + "_" + residue;

        if (null != daoGene.getGene(phosphoSymbol)) {
            return;
        }

        Set<String> aliases = new HashSet<String>();
        //aliases.add(arrayId);
        aliases.add("rppa-phospho");
        aliases.add("phosphoprotein");
        for (String gene : genes) {
            aliases.add("phospho" + gene);
        }

        CanonicalGene phosphoGene = new CanonicalGene(phosphoSymbol, aliases);
        phosphoGene.setType(CanonicalGene.PHOSPHOPROTEIN_TYPE);
        daoGene.addGene(phosphoGene);
    }

    private void importRPPAProteinAlias(String[] genes) throws DaoException {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

        for (String gene : genes) {
            CanonicalGene existingGene = daoGene.getGene(gene);
            if (existingGene != null) {
                Set<String> aliases = new HashSet<String>(
                    existingGene.getAliases()
                );
                if (!aliases.contains("rppa-protein")) {
                    aliases.add("rppa-protein");
                    existingGene.setAliases(aliases);
                    daoGene.addGene(existingGene);
                }
            }
        }
    }

    private GeneticProfile addRPPAProfile() throws DaoException {
        // add profile
        String idProfProt = cancerStudyStableId + "_RPPA_protein_level";
        GeneticProfile gpPro = DaoGeneticProfile.getGeneticProfileByStableId(
            idProfProt
        );
        if (gpPro == null) {
            gpPro =
                new GeneticProfile(
                    idProfProt,
                    cancerStudyId,
                    GeneticAlterationType.PROTEIN_ARRAY_PROTEIN_LEVEL,
                    "Z-SCORE",
                    "protein/phosphoprotein level (RPPA)",
                    "Protein or phosphoprotein level (Z-scores) measured by reverse phase protein array (RPPA)",
                    true
                );
            DaoGeneticProfile.addGeneticProfile(gpPro);
            // get id
            gpPro =
                DaoGeneticProfile.getGeneticProfileByStableId(
                    gpPro.getStableId()
                );
        }
        return gpPro;
    }

    public static void main(String[] args) throws Exception {
        //        args = new String[] {"/Users/jgao/projects/cbio-portal-data/studies/cellline/douglevine_ccl/data_rppa.txt","cellline_douglevine_ccl"};
        if (args.length < 2) {
            System.out.println(
                "command line usage:  importRPPAData.pl <RPPA_data.txt> <Cancer study identifier>"
            );
            // an extra --noprogress option can be given to avoid the messages regarding memory usage and % complete
            return;
        }
        SpringUtil.initDataSource();
        int cancerStudyId = DaoCancerStudy
            .getCancerStudyByStableId(args[1])
            .getInternalId();

        ProgressMonitor.setConsoleModeAndParseShowProgress(args);

        File file = new File(args[0]);
        System.out.println("Reading data from:  " + file.getAbsolutePath());
        int numLines = FileUtil.getNumLines(file);
        System.out.println(" --> total number of lines:  " + numLines);
        ProgressMonitor.setMaxValue(numLines);
        ImportProteinArrayData parser = new ImportProteinArrayData(
            file,
            cancerStudyId,
            args[1]
        );
        parser.importData();
        ConsoleUtil.showWarnings();
        System.err.println("Done.");
    }

    /**
     * add extra antibodies of normalized phosphoprotein data
     * @param args
     * @throws Exception
     * TODO - apparently not used...REMOVE??
     */
    public static void main_normalize_phospho(String[] args) throws Exception {
        DaoProteinArrayData daoPAD = DaoProteinArrayData.getInstance();
        DaoProteinArrayInfo daoPAI = DaoProteinArrayInfo.getInstance();
        DaoProteinArrayTarget daoPAT = DaoProteinArrayTarget.getInstance();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        DaoSampleList daoSampleList = new DaoSampleList();
        ArrayList<CancerStudy> studies = DaoCancerStudy.getAllCancerStudies();
        for (CancerStudy study : studies) {
            int studyId = study.getInternalId();
            SampleList sampleList = daoSampleList.getSampleListByStableId(
                study.getCancerStudyStableId() + "_RPPA"
            );
            if (sampleList == null) continue;
            List<Integer> sampleIds = InternalIdUtil.getInternalSampleIds(
                studyId,
                sampleList.getSampleList()
            );
            ArrayList<ProteinArrayInfo> phosphoArrays = daoPAI.getProteinArrayInfoForType(
                studyId,
                Collections.singleton("phosphorylation")
            );
            ArrayList<ProteinArrayInfo> proteinArrays = daoPAI.getProteinArrayInfoForType(
                studyId,
                Collections.singleton("protein_level")
            );
            for (ProteinArrayInfo phosphoArray : phosphoArrays) {
                for (ProteinArrayInfo proteinArray : proteinArrays) {
                    if (proteinArray.getGene().equals(phosphoArray.getGene())) {
                        String id =
                            phosphoArray.getId() + "-" + proteinArray.getId();
                        if (id.matches(".+-.+-.+")) continue;
                        System.out.println(
                            study.getCancerStudyStableId() +
                            " " +
                            id +
                            " " +
                            phosphoArray.getGene() +
                            " " +
                            phosphoArray.getResidue()
                        );
                        ProteinArrayInfo pai = new ProteinArrayInfo(
                            id,
                            "phosphorylation",
                            phosphoArray.getGene(),
                            phosphoArray.getResidue(),
                            null
                        );
                        daoPAI.addProteinArrayInfo(pai);
                        for (String symbol : phosphoArray
                            .getGene()
                            .split("/")) {
                            CanonicalGene gene = daoGene.getNonAmbiguousGene(
                                symbol
                            );
                            if (gene == null) {
                                System.err.println(symbol + " not exist");
                                continue;
                            }

                            long entrez = gene.getEntrezGeneId();
                            try {
                                daoPAT.addProteinArrayTarget(id, entrez);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        daoPAI.addProteinArrayCancerStudy(
                            id,
                            Collections.singleton(studyId)
                        );

                        ArrayList<ProteinArrayData> phosphoData = daoPAD.getProteinArrayData(
                            studyId,
                            Collections.singleton(phosphoArray.getId()),
                            sampleIds
                        );
                        ArrayList<ProteinArrayData> proteinData = daoPAD.getProteinArrayData(
                            studyId,
                            Collections.singleton(proteinArray.getId()),
                            sampleIds
                        );
                        HashMap<Integer, ProteinArrayData> mapProteinData = new HashMap<Integer, ProteinArrayData>();
                        for (ProteinArrayData pad : proteinData) {
                            mapProteinData.put(pad.getSampleId(), pad);
                        }

                        for (ProteinArrayData pad : phosphoData) {
                            Integer sampleId = pad.getSampleId();
                            ProteinArrayData proteinPAD = mapProteinData.get(
                                sampleId
                            );
                            if (proteinPAD == null) {
                                System.err.println(
                                    "no data: " +
                                    proteinPAD.getArrayId() +
                                    " " +
                                    sampleId
                                );
                                continue;
                            }
                            double abud =
                                pad.getAbundance() - proteinPAD.getAbundance(); // minus
                            ProteinArrayData norm = new ProteinArrayData(
                                studyId,
                                id,
                                sampleId,
                                abud
                            );
                            daoPAD.addProteinArrayData(norm);
                        }
                    }
                }
            }
        }
    }
}
