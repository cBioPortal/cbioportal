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

import java.io.*;
import java.util.*;

/**
 * Export all Data Associated with a Single Genomic Profile.
 *
 * @author Ethan Cerami.
 */
public class ExportProfileData {
    private static final String TAB = "\t";
    private static final String NEW_LINE = "\n";

    public static void main(String[] args) throws DaoException, IOException {
        // check args
        if (args.length < 1) {
            System.out.println("command line usage:  exportProfileData.pl " + "<stable_genetic_profile_id>");
            return;
        }
        String stableGeneticProfileId = args[0];
        System.out.println("Using genetic profile ID:  " + stableGeneticProfileId);
        GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileByStableId(stableGeneticProfileId);
        if (geneticProfile == null) {
            System.out.println("Genetic Profile not recognized:  " + stableGeneticProfileId);
            return;
        } else {
            System.out.println(geneticProfile.getProfileName());
            export(geneticProfile);
        }
    }

    public static void export(GeneticProfile profile) throws IOException, DaoException {
        String fileName = profile.getStableId() + ".txt";
        FileWriter writer = new FileWriter (fileName);
        ArrayList<Integer> sampleList = outputHeader(profile, writer);

        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);
        Set<CanonicalGene> geneSet = daoGeneticAlteration.getGenesInProfile(profile.getGeneticProfileId());
        pMonitor.setMaxValue(geneSet.size());
        Iterator<CanonicalGene> geneIterator = geneSet.iterator();
        outputProfileData(profile, writer, sampleList, daoGeneticAlteration, pMonitor, geneIterator);
        System.out.println ("\nProfile data written to:  " + fileName);
    }

    private static void outputProfileData(GeneticProfile profile, FileWriter writer,
            ArrayList<Integer> sampleList, DaoGeneticAlteration daoGeneticAlteration,
            ProgressMonitor pMonitor, Iterator<CanonicalGene> geneIterator) throws IOException, DaoException {
        while (geneIterator.hasNext()) {
            ConsoleUtil.showProgress(pMonitor);
            pMonitor.incrementCurValue();
            CanonicalGene currentGene = geneIterator.next();
            writer.write(currentGene.getHugoGeneSymbolAllCaps() + TAB);
            writer.write(Long.toString(currentGene.getEntrezGeneId()));
            HashMap<Integer, String> valueMap = daoGeneticAlteration.getGeneticAlterationMap
                    (profile.getGeneticProfileId(), currentGene.getEntrezGeneId());
            for (Integer sampleId:  sampleList) {
                writer.write(TAB + valueMap.get(sampleId));
            }
            writer.write(NEW_LINE);
        }
        writer.close();
    }

    private static ArrayList<Integer> outputHeader(GeneticProfile profile, FileWriter writer) throws DaoException, IOException {
        ArrayList<Integer> sampleList = DaoGeneticProfileSamples.getOrderedSampleList(profile.getGeneticProfileId());
        writer.write("SYMBOL" + TAB);
        writer.write("ENTREZ_GENE_ID");
        for (Integer sampleId : sampleList) {
            Sample s = DaoSample.getSampleById(sampleId);
            writer.write(TAB + s.getStableId());
        }
        writer.write(NEW_LINE);
        return sampleList;
    }
}