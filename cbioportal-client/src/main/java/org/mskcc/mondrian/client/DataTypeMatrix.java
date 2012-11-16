package org.mskcc.mondrian.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.mskcc.mondrian.client.GeneticProfile.GENETIC_PROFILE_TYPE;

/**
 * A matrix that contains data from the cBio portal response of cmd getProfileData
 * 
 * 
 * @author Dazhi Jiao
 */
public class DataTypeMatrix {
	private static final String DEFAULT_KEY_ATTRIBUTE = "DEFAULT";
	protected int significanceType = 3;

	private Map<String, List<String>> infoMap;
	private Map<String, List<? extends Object>> dataMap;
	
	private List<String> dataColNames;
	private List<String> infoColNames;
	
	private GeneticProfile.GENETIC_PROFILE_TYPE profileType;
	
	public static enum DATA_TYPE {
		GENETIC_PROFILE_MULTI_GENE, GENETIC_PROFILE_SINGLE_GENE
	}
	
	public Class<?> dataClass;
	
	private String keyAttributeName = DEFAULT_KEY_ATTRIBUTE;
	
	int numRows;
	int numCols;
	
	public DataTypeMatrix(InputStream input, DATA_TYPE dataType, GeneticProfile.GENETIC_PROFILE_TYPE profileType) throws IOException {
		this(input, dataType, profileType, DEFAULT_KEY_ATTRIBUTE);		
	}
	
	public DataTypeMatrix(InputStream input, DATA_TYPE dataType, GeneticProfile.GENETIC_PROFILE_TYPE profileType, String keyAttributeName) throws IOException {
		numRows = 0;
		numCols = 0;
		this.profileType = profileType;
		this.keyAttributeName = keyAttributeName;
		this.initDataStructures();
		this.loadData(input, dataType);
	}

	/**
	 * Initializes all data structures.
	 */
	private void initDataStructures() {
		if (dataColNames != null) {
			dataColNames.clear();
		} else {
			dataColNames = new ArrayList<String>();
		}

		if (infoColNames != null) {
			infoColNames.clear();
		} else {
			infoColNames = new ArrayList<String>();
		}
		
		if (infoMap != null) {
			infoMap.clear();
		} else {
			infoMap = new HashMap<String, List<String>>();
		}
		
		if (dataMap != null) {
			dataMap.clear();
		} else {
			dataMap = new HashMap<String, List<? extends Object>>();
		}
	}

	/**
	 * Loads the data from the specified input stream into memory.
	 *
	 * @param filename Name of Expression Data File.
	 * @return always returns true, indicating succesful load.
	 * @throws IOException Error loading / parsing the Expression Data File.
	 */
	public boolean loadData(InputStream input, DATA_TYPE dataType) throws IOException {
		if (dataType == DATA_TYPE.GENETIC_PROFILE_MULTI_GENE) {
			infoColNames.add("GENE_ID");
			infoColNames.add("COMMON");
			if (DEFAULT_KEY_ATTRIBUTE.equals(keyAttributeName)) keyAttributeName = "COMMON";
		} else if (dataType == DATA_TYPE.GENETIC_PROFILE_SINGLE_GENE) {
			infoColNames.add("GENETIC_PROFILE_ID");
			infoColNames.add("ALTERATION_TYPE");
			infoColNames.add("GENE_ID");
			infoColNames.add("COMMON");
			if (DEFAULT_KEY_ATTRIBUTE.equals(keyAttributeName)) keyAttributeName = "COMMON";
		} 

		if (input == null)
			return false;

		Scanner scanner = new Scanner(input);

		String headerLine = scanner.nextLine(); 
		while (headerLine.startsWith("#")) {  // skip all the comments
			headerLine = scanner.nextLine();
		}

		if ((headerLine == null) || (headerLine.length() == 0))
			return false;
		
		StringTokenizer headerTok = new StringTokenizer(headerLine, "\t");

		/* eat the first tokens from the header line */
		for (int i = 0; i < infoColNames.size(); i++) {
			headerTok.nextToken();
		}
		
		/* the rest tokens are the data column names */
		while (headerTok.hasMoreTokens())
			dataColNames.add(headerTok.nextToken());

		while (scanner.hasNext()) {
			parseOneLine(scanner.nextLine(), keyAttributeName);
		}
		
		if (input != null) {
			try { input.close(); } catch (IOException ex) {}
		}
		return true;
	}

	private void parseOneLine(String oneLine, String keyAttributeName)
	    throws IOException {
		// 
		// Step 1: divide the line into input tokens, and parse through
		// the input tokens.
		//
		StringTokenizer strtok = new StringTokenizer(oneLine);
		int numTokens = strtok.countTokens();

		if (numTokens == 0) {
			return;
		}
		
		String key = null;
		List<String> infoList = new ArrayList<String>();
		/* first few tokens are info tokens */
		for (int i = 0 ; i < infoColNames.size(); i++) {
			String tok = strtok.nextToken();
			infoList.add(tok);
			if (infoColNames.get(i).equals(keyAttributeName)) 
				key = tok;
		}
		infoMap.put(key, infoList);		
		
		if (this.profileType == GENETIC_PROFILE_TYPE.MUTATION_EXTENDED) {
			List<String> dataList = new ArrayList<String>();
			while (strtok.hasMoreTokens()) {
				String tok = strtok.nextToken();
				if (this.profileType == GENETIC_PROFILE_TYPE.MUTATION_EXTENDED) {
					dataList.add(tok);
				}
			}
			dataMap.put(key, dataList);
		} else {
			List<Double> dataList = new ArrayList<Double>();
			/* the rest tokens are data */
			while (strtok.hasMoreTokens()) {
				String tok = strtok.nextToken();

				if ("NA".equals(tok)) {
					dataList.add(Double.NaN);
				} else {
					try {
						dataList.add(Double.parseDouble(tok));
					} catch (Exception ex) {
						System.out.println("Token wrong format: " + tok);
						//ex.printStackTrace();
						dataList.add(Double.NaN); // not a number
					}
				}
			}
			dataMap.put(key, dataList);
		}
		
	} // parseOneLine
	
	public int getNumRows() {
		return dataMap.keySet().size();
	}
	
	public List<String> getRowNames() {
		return new ArrayList<String>(dataMap.keySet());
	}
	
	public List<? extends Object> getDataRow(String key) {
		return dataMap.get(key);
	}
	
	public List<String> getDataColNames() {
		return this.dataColNames;
	}
}
