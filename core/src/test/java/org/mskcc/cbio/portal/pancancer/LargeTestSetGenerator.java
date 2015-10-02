package org.mskcc.cbio.portal.pancancer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This class is not a Test class, but rather a class that can be used 
 * to generate a large test set for load/performance testing purposes.
 *  
 * @author plukasse
 *
 */
public class LargeTestSetGenerator {

	
	public static void main(String[] args) throws IOException {
		int size = Integer.parseInt(args[0]);
		String outputDir = args[1];
		String studyId = args[2];
		String dataCNAfile = args[3];
		String dataMutationsfile = args[4];
		int nrCancerTypes = Integer.parseInt(args[5]);
		int nrSubTypes = Integer.parseInt(args[6]);
		
		//0- make output dir:
		if (!new File(outputDir).exists() && !new File(outputDir).mkdirs())
			throw new RuntimeException("Could not make output dir : " + outputDir);
		
		//1- generate the desired number of samples to be generated in this test set
		System.out.println("Generating sample list for " + size + " samples....");
		List<String> sampleList = getSampleList(size);
		
		// Generate data_CNA.txt :
		System.out.println("Generating CNA data....");
		generateDataCNA(dataCNAfile, outputDir, sampleList);

		System.out.println("Generating cases_all.txt....");
		generateCasesAll(outputDir, sampleList, studyId);
		
		System.out.println("Generating MUTATION data....");
		generateMutationData(dataMutationsfile, outputDir, sampleList);

		System.out.println("Generating data_clinical.txt....");
        //Generate clinical data:
        generateClinicalData(outputDir, sampleList, nrCancerTypes, nrSubTypes);
        
        System.out.println("Generating cancer_type.txt....");
        generateCancerType(outputDir, studyId);
        
        System.out.println("Generating meta_study.txt....");
        generateMetaStudy(outputDir, studyId);
        
        System.out.println("Generating meta_clinical.txt....");
        generateMetaClinical(outputDir, studyId);
        
        System.out.println("Generating meta_mutations_extended.txt....");
        generateMetaMutExt(outputDir, studyId);
        
        System.out.println("Generating meta_CNA.txt....");
        generateMetaCna(outputDir, studyId);
        
        
        System.out.println("Done. You will find the files in " + outputDir);
	}

	private static void generateCancerType(String outputDir, String studyId) throws IOException {
		FileWriter resultFile = new FileWriter(outputDir + "/cancer_type.txt");
		resultFile.write("pan_cancer	" + studyId+ "	PANCAN	Yellow	Breast");
		resultFile.close();
	}

	private static void generateMetaStudy(String outputDir, String studyId) throws IOException {
		FileWriter resultFile = new FileWriter(outputDir + "/meta_study.txt");
		resultFile.write("type_of_cancer: pan_cancer\n"+
			"cancer_study_identifier: " + studyId+ "\n"+
			"name: " + studyId+ "\n"+
			"description: <a >" + studyId+ "</a>.\n"+
			"citation: The Hyve, Pieter Lukasse\n"+
			"pmid: 23000897\n"+
			"groups: PUBLIC;GDAC;SU2C-PI3K\n"+
			"dedicated_color: Yellow\n"+
			"short_name: " + studyId+ " (TEST)");
		resultFile.close();
	}

	private static void generateMetaClinical(String outputDir, String studyId) throws IOException {
		FileWriter resultFile = new FileWriter(outputDir + "/meta_clinical.txt");
		resultFile.write("cancer_study_identifier: " + studyId+ "\n"+
			"genetic_alteration_type: CLINICAL\n"+
			"datatype: ;:FREE-FORM\n"+
			"stable_id: " + studyId+ "_clinical\n"+
			"show_profile_in_analysis_tab: false\n"+
			"profile_description: Sample clinical data for this study. \n"+
			"profile_name: Sample clinical data for " + studyId+ ".");
		resultFile.close();
	}

	private static void generateMetaMutExt(String outputDir, String studyId) throws IOException {
		FileWriter resultFile = new FileWriter(outputDir + "/meta_mutations_extended.txt");
		resultFile.write("cancer_study_identifier: " + studyId+ "\n"+
			"genetic_alteration_type: MUTATION_EXTENDED\n"+
			"datatype: MAF\n"+
			"stable_id: " + studyId+ "_mutations\n"+
			"show_profile_in_analysis_tab: true\n"+
			"profile_description: Mutation data from whole exome sequencing.\n"+
			"profile_name: Mutations " + studyId);
		resultFile.close();
	}

	private static void generateMetaCna(String outputDir, String studyId) throws IOException {
		FileWriter resultFile = new FileWriter(outputDir + "/meta_CNA.txt");
		resultFile.write("cancer_study_identifier: " + studyId+ "\n"+
			"genetic_alteration_type: COPY_NUMBER_ALTERATION\n"+
			"datatype: DISCRETE\n"+
			"stable_id: " + studyId+ "_gistic\n"+
			"show_profile_in_analysis_tab: true\n"+
			"profile_description: Putative copy-number from GISTIC 2.0. Values: -2 = homozygous deletion; -1 = hemizygous deletion; 0 = neutral / no change; 1 = gain; 2 = high level amplification.\n"+
			"profile_name: Putative copy-number alterations from GISTIC " + studyId);
		resultFile.close();
	}

	private static void generateClinicalData(String outputDir, List<String> sampleList, int nrCancerTypes, int nrSubTypes) throws IOException {
		FileWriter resultFile = new FileWriter(outputDir + "/data_clinical.txt");
		//write header, with CANCER_TYPE, CANCER_TYPE_DETAILED in pos 3 and 4:
		if (nrSubTypes > 0) {
			resultFile.write("#1\t1\t1\t\t\t1\t1\t1\t1\n");
			resultFile.write("#PATIENT\tSAMPLE\tSAMPLE\tSAMPLE\tSAMPLE\tPATIENT\tPATIENT\tPATIENT\tPATIENT\n");
			resultFile.write("#Patient Identifier\t#Sample Identifier\tSubtype\tCancer Type\tCancer Type Detailed\tOverall Survival Status\tOverall Survival (Months)\tDisease Free Status\tDisease Free (Months)\n");
			resultFile.write("#Patient identifier\t#Sample identifier\tSubtype description\tCancer Type\tCancer Type Detailed\tOverall survival status\tOverall survival in months since diagnosis\tDisease free status\tDisease free in months since treatment\n");
			resultFile.write("#STRING\t#STRING\tSTRING\tSTRING\tSTRING\tSTRING\tNUMBER\tSTRING\tNUMBER\n");
			resultFile.write("PATIENT_ID\tSAMPLE_ID\tSUBTYPE\tCANCER_TYPE\tCANCER_TYPE_DETAILED\tOS_STATUS\tOS_MONTHS\tDFS_STATUS\tDFS_MONTHS\n");
		}
		else {
			//shorter, without CANCER_TYPE_DETAILED
			resultFile.write("#1\t1\t1\t\t\t1\t1\t1\n");
			resultFile.write("#PATIENT\tSAMPLE\tSAMPLE\tSAMPLE\tPATIENT\tPATIENT\tPATIENT\tPATIENT\n");
			resultFile.write("#Patient Identifier\t#Sample Identifier\tSubtype\tCancer Type\tOverall Survival Status\tOverall Survival (Months)\tDisease Free Status\tDisease Free (Months)\n");
			resultFile.write("#Patient identifier\t#Sample identifier\tSubtype description\tCancer Type\tOverall survival status\tOverall survival in months since diagnosis\tDisease free status\tDisease free in months since treatment\n");
			resultFile.write("#STRING\t#STRING\tSTRING\tSTRING\tSTRING\tNUMBER\tSTRING\tNUMBER\n");
			resultFile.write("PATIENT_ID\tSAMPLE_ID\tSUBTYPE\tCANCER_TYPE\tOS_STATUS\tOS_MONTHS\tDFS_STATUS\tDFS_MONTHS\n");
		}
		
		//iterate over sampleList, write the lines:
		for (String sample: sampleList) {
			String patientId = sample.split("-SAMPLE")[0]; //see getSampleList method
			resultFile.write(patientId + "\t");
			resultFile.write(sample + "\t");
			
			String [] subtype = {"Luminal A", "basal-like", "Luminal B", "Her2 enriched"};
			resultFile.write(subtype[random(0,3)] + "\t");
			
			int cancerType = random(1,nrCancerTypes);
			resultFile.write("Cancer_type" + cancerType + "\t");
			
			if (nrSubTypes > 0) {
				int cancerTypeDetailed = random(1,nrSubTypes);
				resultFile.write("Cancer_type" + cancerType + "_Sub" +cancerTypeDetailed + "\t");
			}
			
			String [] osStatus = {"DECEASED", "LIVING"};
			resultFile.write(osStatus[random(0,1)] + "\t");
			//os_months
			int osMonths = random(0,300);
			resultFile.write(osMonths + "\t");
			
			String [] dfsStatus = {"NA","Recurred/Progressed","DiseaseFree"};
			resultFile.write(dfsStatus[random(0,2)] + "\t");
			//repeat os_months
			resultFile.write(osMonths + "");			

			resultFile.write("\n");
		}
		resultFile.close();
		
	}
	
	private static int random(int start, int end) {
		return (int)Math.round(Math.random()*(end-start))+start;
	}
	


	private static void generateMutationData(String dataMutationsfile, String outputDir, List<String> sampleList) throws IOException {
		Map<String, String> lineTemplates = new HashMap<String, String>();
		
		//read the dataMutationsfile and find the unique line templates based on Hugo_Symbol (1st column)
		BufferedReader originalFile = new BufferedReader(new FileReader(dataMutationsfile));
		FileWriter resultFile = new FileWriter(outputDir + "/" + new File(dataMutationsfile).getName());
		//read header:
		String line = originalFile.readLine();
		//write header:
		resultFile.write(line + "\n");
		int pos_Tumor_Sample_Barcode = findPos(line, "Tumor_Sample_Barcode");
		int pos_Matched_Norm_Sample_Barcode = findPos(line, "Matched_Norm_Sample_Barcode"); 		
		//read first data line:
		line = originalFile.readLine();
		while ( line != null) {
			String[] splitLine = line.split("\t");
			String hugoSymbol = splitLine[0];
			lineTemplates.put(hugoSymbol, line);
			line = originalFile.readLine();
		}
		//iterate over the samples, giving each sample a random number of mutations (from the line templates)
		Object[] lines = lineTemplates.values().toArray();
		for (String sample : sampleList) {
			//get a random number of lineTemplates 
			int numberOfMutations = random(0, lines.length);
			//get the numberOfMutations random indexes:
			int[] indexes = getRandomIdx(lines.length, numberOfMutations);
			for (int index: indexes){
				line = (String)lines[index];
			
				//replace sample code:
				String[] splitLine = line.split("\t");
				splitLine[pos_Tumor_Sample_Barcode] = sample;
				splitLine[pos_Matched_Norm_Sample_Barcode] = sample;
				for (String field : splitLine)
					resultFile.write(field + "\t");
					
				resultFile.write(line + "\n");
			}
		}
		resultFile.close();
		originalFile.close();
	}
	
	
	private static int findPos(String headerLine, String fieldName) {
		String[] splitLine = headerLine.split("\t");
		for (int i = 0; i < splitLine.length; i++) {
			String field = splitLine[i];
			if (field.equals(fieldName))
				return i;
		}
		throw new RuntimeException("Field [" + fieldName + "] not found in input data file");
	}


	private static int[] getRandomIdx(int size, int nrItems) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i=0; i<size; i++) {
            list.add(new Integer(i));
        }
        int[] result = new int[nrItems];
        Collections.shuffle(list);
        for (int i=0; i<nrItems; i++) {
            result[i] = list.get(i).intValue();
        }
        return result;
    }

	private static void generateDataCNA(String dataCNAfile, String outputDir, List<String> sampleList) throws IOException {
		// Hugo_Symbol	Entrez_Gene_Id sample1 sample2 ....
		BufferedReader originalFile = new BufferedReader(new FileReader(dataCNAfile));
		FileWriter resultFile = new FileWriter(outputDir + "/" + new File(dataCNAfile).getName());
		//write header: 
		resultFile.write("Hugo_Symbol\tEntrez_Gene_Id");
		for (String sample : sampleList)
			resultFile.write("\t" + sample);
		resultFile.write("\n");

		//read header/skip:
		String line = originalFile.readLine();
		//read first data line:
		line = originalFile.readLine();
		while ( line != null)	{
			//write Hugo_Symbol\tEntrez_Gene_Id:
			String[] splitLine = line.split("\t");
			resultFile.write(splitLine[0] + "\t" + splitLine[1]);
			//write the CNA data: 
			for (String sample : sampleList)
				resultFile.write("\t" + random(-2,2));
			
			//new line
			resultFile.write("\n");
			line = originalFile.readLine();
		}
		originalFile.close();
		resultFile.close();
		
		
	}
	
	private static void generateCasesAll(String outputDir, List<String> sampleList, String studyId) throws IOException {
		
		FileWriter resultFile = new FileWriter(outputDir + "/cases_all.txt");
		//write header: 

		resultFile.write("cancer_study_identifier: " + studyId + "\n");
		resultFile.write("stable_id: " + studyId + "_all\n");
		resultFile.write("case_list_name: All Tumors\n");
		resultFile.write("case_list_description: All tumor samples (" + sampleList.size() + " samples)\n");
		resultFile.write("case_list_ids: ");
		for (String sample : sampleList)
			resultFile.write("\t" + sample);
		resultFile.write("\n");
		resultFile.close();
	}
	

	private static List<String> getSampleList(int sampleSetSize) {
		List<String> result = new ArrayList<String>();
		
		int maxSampleListForPatient = 10;
		//size between 1 and maxSampleListForPatient
		int sizeSampleListForPatient = random(1,maxSampleListForPatient);
		int countSize = 0;
		int sampleId = 0;
		int patientId = 1;
		for (int i = 0; i < sampleSetSize; i++) {
			countSize++;
			if (countSize > sizeSampleListForPatient) {
				countSize = 0;
				//get new random size:
				sizeSampleListForPatient = random(1,maxSampleListForPatient);
				patientId++;
				sampleId = 0;
			}
			sampleId++;		
			result.add("TEST-PAT"+patientId+"-SAMPLE"+sampleId);
		}
		return result;
	}
	
}
