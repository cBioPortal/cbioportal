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

import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoGeneticProfileCases;
import org.mskcc.cbio.portal.dao.DaoGeneticAlteration;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.util.ConsoleUtil;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;
import java.util.HashMap;

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
        ArrayList<String> caseList = outputHeader(profile, writer);

        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);
        Set<CanonicalGene> geneSet = daoGeneticAlteration.getGenesInProfile(profile.getGeneticProfileId());
        pMonitor.setMaxValue(geneSet.size());
        Iterator<CanonicalGene> geneIterator = geneSet.iterator();
        outputProfileData(profile, writer, caseList, daoGeneticAlteration, pMonitor, geneIterator);
        System.out.println ("\nProfile data written to:  " + fileName);
    }

    private static void outputProfileData(GeneticProfile profile, FileWriter writer,
            ArrayList<String> caseList, DaoGeneticAlteration daoGeneticAlteration,
            ProgressMonitor pMonitor, Iterator<CanonicalGene> geneIterator) throws IOException, DaoException {
        while (geneIterator.hasNext()) {
            ConsoleUtil.showProgress(pMonitor);
            pMonitor.incrementCurValue();
            CanonicalGene currentGene = geneIterator.next();
            writer.write(currentGene.getHugoGeneSymbolAllCaps() + TAB);
            writer.write(Long.toString(currentGene.getEntrezGeneId()));
            HashMap<String, String> valueMap = daoGeneticAlteration.getGeneticAlterationMap
                    (profile.getGeneticProfileId(), currentGene.getEntrezGeneId());
            for (String caseId:  caseList) {
                writer.write(TAB + valueMap.get(caseId));
            }
            writer.write(NEW_LINE);
        }
        writer.close();
    }

    private static ArrayList<String> outputHeader(GeneticProfile profile, FileWriter writer) throws DaoException, IOException {
        ArrayList<String> caseList = DaoGeneticProfileCases.getOrderedCaseList(profile.getGeneticProfileId());
        writer.write("SYMBOL" + TAB);
        writer.write("ENTREZ_GENE_ID");
        for (String caseId:  caseList) {
            writer.write(TAB + caseId);
        }
        writer.write(NEW_LINE);
        return caseList;
    }
}