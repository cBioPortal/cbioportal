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

package org.mskcc.cbio.cgds.scripts;

import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneticProfile;
import org.mskcc.cbio.cgds.dao.DaoGeneticProfileCases;
import org.mskcc.cbio.cgds.dao.DaoGeneticAlteration;
import org.mskcc.cbio.cgds.model.GeneticProfile;
import org.mskcc.cbio.cgds.model.CanonicalGene;
import org.mskcc.cbio.cgds.util.ProgressMonitor;
import org.mskcc.cbio.cgds.util.ConsoleUtil;

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
            System.exit(1);
        }
        String stableGeneticProfileId = args[0];
        System.out.println("Using genetic profile ID:  " + stableGeneticProfileId);
        GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileByStableId(stableGeneticProfileId);
        if (geneticProfile == null) {
            System.out.println("Genetic Profile not recognized:  " + stableGeneticProfileId);
            System.exit(1);
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
        DaoGeneticProfileCases daoGeneticProfileCases = new DaoGeneticProfileCases();
        ArrayList<String> caseList = daoGeneticProfileCases.getOrderedCaseList(profile.getGeneticProfileId());
        writer.write("SYMBOL" + TAB);
        writer.write("ENTREZ_GENE_ID");
        for (String caseId:  caseList) {
            writer.write(TAB + caseId);
        }
        writer.write(NEW_LINE);
        return caseList;
    }
}