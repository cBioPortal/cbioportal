package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.util.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.service.GenePanelService;

import java.io.*;
import java.util.*;

/**
 *
 * @author zheins
 */
public class ImportGenePanel {        
    public static void importData(File dataFile) throws Exception
    {
        ProgressMonitor.setCurrentMessage("Read data from:  " + dataFile.getAbsolutePath());
        Properties properties = new Properties();
        properties.load(new FileInputStream(dataFile));

        String stableId = getPropertyValue("stable_id", properties, true);
        String description = getPropertyValue("description", properties, false);
        String cancerStudyId = getPropertyValue("cancer_study_identifier", properties, true);
        List<String> geneSymbols = getGeneSymbols("gene_list", properties);

        GenePanelService genePanelService = SpringUtil.getGenePanelService();
        GenePanel existingGenePanel = genePanelService.getByStableId(stableId);
        if (existingGenePanel != null) {
            ProgressMonitor.setCurrentMessage("Deleting existing gene panel: " + stableId);
            genePanelService.deleteGenePanel(stableId);
            ProgressMonitor.setCurrentMessage("Deleted!");
        }
        genePanelService.insertGenePanel(stableId, description, cancerStudyId, geneSymbols);
        GenePanel gp = genePanelService.getByStableId(stableId);

        ProgressMonitor.setCurrentMessage("Successfuly imported gene panel..");
        ProgressMonitor.setCurrentMessage(" --> internal ID: " + gp.internalId);
        ProgressMonitor.setCurrentMessage(" --> stable ID: " + gp.stableId);
        ProgressMonitor.setCurrentMessage(" --> study ID: " + gp.cancerStudyId);
        ProgressMonitor.setCurrentMessage(" --> number of gene symbols: " + gp.geneList.size());
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
        ProgressMonitor.setConsoleMode(true);
        File dataFile = new File(args[0]);
        if (dataFile.isDirectory()) {
            File files[] = dataFile.listFiles();
            for (File file : files) {
                if (file.getName().endsWith("txt")) {
                    ImportGenePanel.importData(file);
                }
            }
            if (files.length == 0) {
                ProgressMonitor.setCurrentMessage("No gene panels found in directory, skipping import: " + dataFile.getCanonicalPath());
            }
        }
        else {
            ImportGenePanel.importData(dataFile);
        }
    }
}
