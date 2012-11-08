package org.mskcc.mondrian.client;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.junit.Test;
import org.mskcc.mondrian.client.CBioPortalClient;
import org.mskcc.mondrian.client.CancerStudy;
import org.mskcc.mondrian.client.CaseList;
import org.mskcc.mondrian.client.GeneticProfile;

public class CBioPortalClientTest {
	
	private Logger log = Logger.getLogger(CBioPortalClientTest.class.getName());

	@Test
	public void test() throws Exception {
		Random random = new Random();
		// private static String[] genes = {"XXXX"};
		String[] genes = { "TP53", "EGFR", "MDM2", "BRCA1", "POLE", "GAPDH",
				"ACTB", "AR", "AKT1", "AKT2", "AKT3", "KLK3", "XXXX" };
		CBioPortalClient portalClient = new CBioPortalClient();

		int i = 0;

		// List all available cancer studies
		List<CancerStudy> cancerStudies = portalClient.getCancerStudies();
		assertTrue(cancerStudies.size() > 0);

		// Select a random cancer study and then list case lists associated with
		// this study
		// CancerStudy cancerStudy =
		// cancerStudies.get(random.nextInt(cancerStudies.size()));
		CancerStudy cancerStudy = cancerStudies.get(16);
		assertNotNull(cancerStudy);
		assertNotNull(cancerStudy.getName());
		portalClient.setCurrentCancerStudy(cancerStudy);

		i = 0;
		List<CaseList> caseListsForCurrentStudy = portalClient
				.getCaseListsForCurrentStudy();
		assertTrue(caseListsForCurrentStudy.size() > 0);
		for (CaseList caseList : caseListsForCurrentStudy) {
			assertNotNull(caseList.getName());
		}

		// Now list all geneticProfiles available for current study
		List<GeneticProfile> geneticProfilesForCurrentStudy = portalClient.getGeneticProfilesForCurrentStudy();
		assertTrue(geneticProfilesForCurrentStudy.size() > 0);
		for (GeneticProfile geneticProfile : geneticProfilesForCurrentStudy) {
			assertNotNull(geneticProfile.getName());
			assertNotNull(geneticProfile.getType());
		}
		
		DataTypeMatrix matrix = portalClient.getProfileData(caseListsForCurrentStudy.get(0), geneticProfilesForCurrentStudy.get(0), Arrays.asList(genes));
		assertEquals(12, matrix.getNumRows());
	}
}
