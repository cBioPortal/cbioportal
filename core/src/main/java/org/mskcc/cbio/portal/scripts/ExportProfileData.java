/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
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
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

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
            System.out.println(
                "command line usage:  exportProfileData.pl " +
                "<stable_genetic_profile_id>"
            );
            // an extra --noprogress option can be given to avoid the messages regarding memory usage and % complete
            return;
        }
        String stableGeneticProfileId = args[0];
        System.out.println(
            "Using genetic profile ID:  " + stableGeneticProfileId
        );
        GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileByStableId(
            stableGeneticProfileId
        );
        if (geneticProfile == null) {
            System.out.println(
                "Genetic Profile not recognized:  " + stableGeneticProfileId
            );
            return;
        } else {
            System.out.println(geneticProfile.getProfileName());
            ProgressMonitor.setConsoleModeAndParseShowProgress(args);
            export(geneticProfile);
        }
    }

    public static void export(GeneticProfile profile)
        throws IOException, DaoException {
        String fileName = profile.getStableId() + ".txt";
        FileWriter writer = new FileWriter(fileName);
        ArrayList<Integer> sampleList = outputHeader(profile, writer);

        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();
        Set<CanonicalGene> geneSet = daoGeneticAlteration.getGenesInProfile(
            profile.getGeneticProfileId()
        );
        ProgressMonitor.setMaxValue(geneSet.size());
        Iterator<CanonicalGene> geneIterator = geneSet.iterator();
        outputProfileData(
            profile,
            writer,
            sampleList,
            daoGeneticAlteration,
            geneIterator
        );
        System.out.println("\nProfile data written to:  " + fileName);
    }

    private static void outputProfileData(
        GeneticProfile profile,
        FileWriter writer,
        ArrayList<Integer> sampleList,
        DaoGeneticAlteration daoGeneticAlteration,
        Iterator<CanonicalGene> geneIterator
    )
        throws IOException, DaoException {
        while (geneIterator.hasNext()) {
            ConsoleUtil.showProgress();
            ProgressMonitor.incrementCurValue();
            CanonicalGene currentGene = geneIterator.next();
            writer.write(currentGene.getHugoGeneSymbolAllCaps() + TAB);
            writer.write(Long.toString(currentGene.getEntrezGeneId()));
            HashMap<Integer, String> valueMap = daoGeneticAlteration.getGeneticAlterationMap(
                profile.getGeneticProfileId(),
                currentGene.getEntrezGeneId()
            );
            for (Integer sampleId : sampleList) {
                writer.write(TAB + valueMap.get(sampleId));
            }
            writer.write(NEW_LINE);
        }
        writer.close();
    }

    private static ArrayList<Integer> outputHeader(
        GeneticProfile profile,
        FileWriter writer
    )
        throws DaoException, IOException {
        ArrayList<Integer> sampleList = DaoGeneticProfileSamples.getOrderedSampleList(
            profile.getGeneticProfileId()
        );
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
