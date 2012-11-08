package org.mskcc.mondrian.client.example;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.mskcc.mondrian.client.CBioPortalClient;
import org.mskcc.mondrian.client.CancerStudy;
import org.mskcc.mondrian.client.CaseList;
import org.mskcc.mondrian.client.GeneticProfile;

public class CBioPortalClientExample {
	
	private Logger log = Logger.getLogger(CBioPortalClientExample.class.getName());

	public static void main(String[] args) throws Exception {
		Random random = new Random();
		// private static String[] genes = {"XXXX"};
		String[] genes = { "TP53", "EGFR", "MDM2", "BRCA1", "POLE", "GAPDH",
				"ACTB", "AR", "AKT1", "AKT2", "AKT3", "KLK3", "XXXX" };
		CBioPortalClient cBioPortalAccessor = new CBioPortalClient();

		int i = 0;

		// List all available cancer studies
		List<CancerStudy> cancerStudies = cBioPortalAccessor.getCancerStudies();
		for (CancerStudy cancerStudy : cancerStudies) {
			System.out.println((i++) + "\tcancerStudy = "
					+ cancerStudy.getName());
		}

		System.out.println("**");

		// Select a random cancer study and then list case lists associated with
		// this study
		// CancerStudy cancerStudy =
		// cancerStudies.get(random.nextInt(cancerStudies.size()));
		CancerStudy cancerStudy = cancerStudies.get(16);
		System.out.println("Using cancerStudy = " + cancerStudy.getName());
		cBioPortalAccessor.setCurrentCancerStudy(cancerStudy);

		System.out.println("**");

		i = 0;

		System.out.println("Case lists:");
		List<CaseList> caseListsForCurrentStudy = cBioPortalAccessor
				.getCaseListsForCurrentStudy();
		for (CaseList caseList : caseListsForCurrentStudy) {
			System.out.println((i++) + "\tcaseList = "
					+ caseList.getDescription());
		}

		// Now use the first one on the list
		// cBioPortalAccessor.setCurrentCaseList(caseListsForCurrentStudy.get(random.nextInt(caseListsForCurrentStudy.size())));
		cBioPortalAccessor.setCurrentCaseList(caseListsForCurrentStudy.get(18));
		System.out.println("**");
		System.out.println("Current case list: "
				+ cBioPortalAccessor.getCurrentCaseList().getDescription());

		i = 0;

		// Now list all geneticProfiles available for current study
		System.out.println("**");
		System.out.println("Genetic Profiles for the study:");
		List<GeneticProfile> geneticProfilesForCurrentStudy = cBioPortalAccessor.getGeneticProfilesForCurrentStudy();
		for (GeneticProfile geneticProfile : geneticProfilesForCurrentStudy) {
			System.out.println((i++) + "\tgeneticProfile = "
					+ geneticProfile.getName() + " ("
					+ geneticProfile.getType() + ")");
		}

		// Pick a random genetic profile from the list and set it.
		List<GeneticProfile> geneticProfiles = new ArrayList<GeneticProfile>();
		// geneticProfiles.add(geneticProfilesForCurrentStudy.get(random.nextInt(geneticProfilesForCurrentStudy.size())));
		geneticProfiles.add(geneticProfilesForCurrentStudy.get(7));

		cBioPortalAccessor.setCurrentGeneticProfiles(geneticProfiles);
		System.out.println("**");
		System.out.println("Current genetic profile: ");

		for (GeneticProfile geneticProfile : cBioPortalAccessor
				.getCurrentGeneticProfiles()) {
			System.out.println("\tgeneticProfile = " + geneticProfile.getName()
					+ " (" + geneticProfile.getType() + ")");
		}

		// Save alteration type and numOfCases for minimalistic oncoprints
		GeneticProfile geneticProfile = cBioPortalAccessor
				.getCurrentGeneticProfiles().iterator().next();
		Integer numOfCases = cBioPortalAccessor.getCurrentCaseList().getCases().length;

		// Headers
		System.out.println("**");
		System.out.println("Inferred alterations:\n");
		System.out.println("Gene Name\tStatus\t\tOncoPrint(" + numOfCases
				+ " cases)");

		
	}

}
