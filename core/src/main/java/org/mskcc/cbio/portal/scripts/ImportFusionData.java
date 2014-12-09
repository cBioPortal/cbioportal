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
import org.mskcc.cbio.maf.*;

import java.io.*;
import java.util.*;

/**
 * Imports a fusion file.
 * Columns may be in any order.
 * Creates an ExtendedMutation instances for each row.
 *
 * @author Selcuk Onur Sumer
 */
public class ImportFusionData
{
	public static final String FUSION = "Fusion";

	private ProgressMonitor pMonitor;
	private File fusionFile;
	private int geneticProfileId;

	public ImportFusionData(File fusionFile,
			int geneticProfileId,
			ProgressMonitor pMonitor)
	{
		this.fusionFile = fusionFile;
		this.geneticProfileId = geneticProfileId;
		this.pMonitor = pMonitor;
	}

	public void importData() throws IOException, DaoException
	{
		Map<ExtendedMutation.MutationEvent, ExtendedMutation.MutationEvent> existingEvents =
				new HashMap<ExtendedMutation.MutationEvent, ExtendedMutation.MutationEvent>();

		for (ExtendedMutation.MutationEvent event : DaoMutation.getAllMutationEvents())
		{
			existingEvents.put(event, event);
		}

		long mutationEventId = DaoMutation.getLargestMutationEventId();

		FileReader reader = new FileReader(this.fusionFile);
		BufferedReader buf = new BufferedReader(reader);
		DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();

		//  The MAF File Changes fairly frequently, and we cannot use column index constants.
		String line = buf.readLine();
		line = line.trim();

		FusionFileUtil fusionUtil = new FusionFileUtil(line);

		boolean addEvent;

        GeneticProfile geneticProfile = DaoGeneticProfile.getGeneticProfileById(geneticProfileId);
		while ((line = buf.readLine()) != null)
		{
			if( pMonitor != null)
			{
				pMonitor.incrementCurValue();
				ConsoleUtil.showProgress(pMonitor);
			}

			if( !line.startsWith("#") && line.trim().length() > 0)
			{
				FusionRecord record = fusionUtil.parseRecord(line);

				// process case id
				String barCode = record.getTumorSampleID();
                ImportDataUtil.addPatients(new String[] { StableIdUtil.getPatientId(barCode) }, geneticProfileId);
                ImportDataUtil.addSamples(new String[] { StableIdUtil.getSampleId(barCode) }, geneticProfileId);
		        Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(geneticProfile.getCancerStudyId(),
                                                                            StableIdUtil.getSampleId(barCode));
		        if (sample == null) {
		        	assert StableIdUtil.isNormal(barCode);
		        	line = buf.readLine();
		        	continue;
		        }
				if (!DaoSampleProfile.sampleExistsInGeneticProfile(sample.getInternalId(), geneticProfileId))
				{
					DaoSampleProfile.addSampleProfile(sample.getInternalId(), geneticProfileId);
				}

				//  Assume we are dealing with Entrez Gene Ids (this is the best / most stable option)
				String geneSymbol = record.getHugoGeneSymbol();
				long entrezGeneId = record.getEntrezGeneId();
				CanonicalGene gene = null;

				if (entrezGeneId != TabDelimitedFileUtil.NA_LONG)
				{
					gene = daoGene.getGene(entrezGeneId);
				}

				if (gene == null) {
					// If Entrez Gene ID Fails, try Symbol.
					gene = daoGene.getNonAmbiguousGene(geneSymbol);
				}

				if(gene == null)
				{
					pMonitor.logWarning("Gene not found:  " + geneSymbol + " ["
					                    + entrezGeneId + "]. Ignoring it "
					                    + "and all fusion data associated with it!");
				}
				else
				{
					// create a mutation instance with default values
					ExtendedMutation mutation = ExtendedMutationUtil.newMutation();

					mutation.setGeneticProfileId(geneticProfileId);
					mutation.setSampleId(sample.getInternalId());
					mutation.setGene(gene);
					mutation.setSequencingCenter(record.getCenter());
					mutation.setProteinChange(record.getFusion());

					// TODO we may need get mutation type from the file
					// instead of defining a constant
					mutation.setMutationType(FUSION);

					ExtendedMutation.MutationEvent event = existingEvents.get(mutation.getEvent());

					if (event != null)
					{
						mutation.setEvent(event);
						addEvent = false;
					}
					else
					{
						mutation.setMutationEventId(++mutationEventId);
						existingEvents.put(mutation.getEvent(), mutation.getEvent());
						addEvent = true;
					}

					// add fusion (as a mutation)
					DaoMutation.addMutation(mutation, addEvent);
				}
			}
		}

		buf.close();

		if( MySQLbulkLoader.isBulkLoad())
		{
			MySQLbulkLoader.flushAll();
		}
	}
}
