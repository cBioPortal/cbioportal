/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.service.GenePanelService;

import java.io.*;
import java.util.*;

/**
 *
 * @author dongli
 */
public class ImportGenePanel {        
    public static void importData(File dataFile, ProgressMonitor pMonitor) throws Exception
	{
		pMonitor.setCurrentMessage("Read data from:  " + dataFile.getAbsolutePath());
		Properties properties = new Properties();
		properties.load(new FileInputStream(dataFile));

		String stableId = getPropertyValue("stable_id", properties, true);
		String cancerStudyId = getPropertyValue("cancer_study_identifier", properties, true);
		String description = getPropertyValue("description", properties, false);
		List<String> geneSymbols = getGeneSymbols("gene_list", properties);

		GenePanelService genePanelService = SpringUtil.getGenePanelService();
		genePanelService.insertGenePanel(stableId, description, cancerStudyId, geneSymbols);
		GenePanel gp = genePanelService.getByStableId(stableId);

		pMonitor.setCurrentMessage(" --> internal ID: " + gp.internalId);
		pMonitor.setCurrentMessage(" --> stable ID: " + gp.stableId);
		pMonitor.setCurrentMessage(" --> cancer study ID: " + gp.cancerStudyId);
		pMonitor.setCurrentMessage(" --> number of gene symbols: " + gp.geneList.size());
    }

	private static String getPropertyValue(String propertyName, Properties properties, boolean noSpaceAllowed) throws IllegalArgumentException
	{
		String propertyValue = properties.getProperty(propertyName).trim();

		if (propertyValue == null || propertyValue.length() == 0) {
			throw new IllegalArgumentException(propertyValue + " is not specified.");
		}
		if (noSpaceAllowed && propertyValue.contains(" ")) {
			throw new IllegalArgumentException(propertyValue + " cannot contain spaces:  " + propertyValue);
		}
		return propertyValue;
	}

	private static List<String> getGeneSymbols(String propertyName, Properties properties) throws IllegalArgumentException
	{
		String propertyValue = properties.getProperty(propertyName).trim();
		if (propertyValue == null || propertyValue.length() == 0) {
			throw new IllegalArgumentException(propertyValue + " is not specified.");
		}
		String[] symbols = propertyValue.split("\t");
		return new ArrayList<String>(new HashSet<String>(Arrays.asList(symbols)));
	}  
	
	public static void main(String[] args) throws Exception {
		// check args
      	if (args.length < 1) {
			System.out.println("missing required argument: gene panel file or director");
			return;
		}
		ProgressMonitor pMonitor = new ProgressMonitor();
		pMonitor.setConsoleMode(true);
		File dataFile = new File(args[0]);
		if (dataFile.isDirectory()) {
			File files[] = dataFile.listFiles();
			for (File file : files) {
				if (file.getName().endsWith("txt")) {
					ImportGenePanel.importData(file, pMonitor);
				}
			}
			if (files.length == 0) {
				pMonitor.setCurrentMessage("No gene panels found in directory, skipping import: " + dataFile.getCanonicalPath());
			}
		}
		else {
			ImportGenePanel.importData(dataFile, pMonitor);
      	}
	}
}
