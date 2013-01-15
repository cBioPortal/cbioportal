/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
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
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package org.mskcc.cbio.cgds.scripts;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.cgds.dao.DaoClinicalTrial;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.model.ClinicalTrial;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

/**
 * Imports clinical trial summaries and creates a keyword -> id index.
 * Assumes the data is downloaded from cancer.gov through a content dissemination license agreement.
 * The first argument should be the full path of the 'CTGovProtocol' folder.
 *
 * @author Arman
 * @author JJ
 */
public class ImportClinicalTrialData {
    private static final Log log = LogFactory.getLog(ImportClinicalTrialData.class);
    private static DaoClinicalTrial daoClinicalTrial;

    public static void main(String[] args) throws Exception {
        if(args.length < 1) {
            System.err.println(
                    "Missing options!\nUsage: "
                            + ImportClinicalTrialData.class.getSimpleName()
                            + " path/to/CTGovProtocol/"
            );
            System.exit(-1);
        }

        String path = args[0].trim();
        File folder = new File(path);
        if(!folder.isDirectory()) {
            System.err.println("Error: could not read " + path);
            System.exit(-1);
        }

        importFilesFromFolder(folder);
    }

    /**
     * Iterates over the files within a given folder and imports them to the database.
     *
     * @param folder folder containing clinical trial data XMLs
     * @throws DaoException if cannot connect to the database
     * @throws IOException if cannot get/read the files
     * @throws SAXException if XML file(s) are mis-formatted
     * @throws ParserConfigurationException if there is a problem parsing the files
     */
    public static void importFilesFromFolder(File folder)
            throws ParserConfigurationException, IOException, SAXException, DaoException
    {
        daoClinicalTrial = DaoClinicalTrial.getInstance();

        log.debug("Reseting the clinical trials table. ");
        daoClinicalTrial.deleteAllRecords();

        for (File file : folder.listFiles()) {
            if(file.getName().startsWith(".")) continue; // Skip hidden files
            processXmlFile(file);
        }

        log.debug("Clinical trial information import is done! (" + daoClinicalTrial.countClinicalStudies() + " trials imported)");
    }

    private static void processXmlFile(File file)
            throws ParserConfigurationException, IOException, SAXException, DaoException
    {
        ClinicalTrial clinicalTrial = new ClinicalTrial();
        String secondaryId = file.getName().split("\\.")[0].replace("CDR", "");
        clinicalTrial.setSecondaryId(secondaryId);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();

        Document doc = db.parse(file);
        Element docEle = doc.getDocumentElement();

        clinicalTrial.setId(
                ((Element) docEle.getElementsByTagName("IDInfo").item(0))
                        .getElementsByTagName("NCTID").item(0).getChildNodes().item(0).getNodeValue())
        ;

        clinicalTrial.setTitle(
                docEle.getElementsByTagName("BriefTitle").item(0).getChildNodes().item(0).getNodeValue()

        );

        clinicalTrial.setStatus(
                docEle.getElementsByTagName("CurrentProtocolStatus").item(0).getChildNodes().item(0).getNodeValue()
        );

        NodeList protocolPhase = docEle.getElementsByTagName("ProtocolPhase");
        clinicalTrial.setPhase(
                protocolPhase.item(protocolPhase.getLength()-1).getChildNodes().item(0).getNodeValue()
        );

        clinicalTrial.setLocation("N/A");
        NodeList location = docEle.getElementsByTagName("Location");
        if(location != null && location.getLength() > 0) {
            Element locItem = (Element) location.item(0);
            NodeList facilityItem = locItem.getElementsByTagName("Facility");
            if(facilityItem != null && facilityItem.getLength() > 0) {
                Element facility = (Element) facilityItem.item(0);
                String city = facility.getElementsByTagName("City").item(0).getChildNodes().item(0).getNodeValue();
                String country
                        = facility.getElementsByTagName("CountryName").item(0).getChildNodes().item(0).getNodeValue();
                clinicalTrial.setLocation(city + ", " + country);
            }
        }

        HashSet<String> keywords = new HashSet<String>();
        extractDrugNames(keywords,
                ((Element) ((Element) docEle.getElementsByTagName("ProtocolDetail").item(0))
                        .getElementsByTagName("StudyCategory").item(0)).getElementsByTagName("Intervention")
        );

        extractCancerTypes(keywords,
                ((Element) docEle.getElementsByTagName("Eligibility").item(0)).getElementsByTagName("Diagnosis")
        );

        daoClinicalTrial.addClinicalTrial(clinicalTrial, keywords);
    }

    private static void extractCancerTypes(HashSet<String> keywords, NodeList elementsByTagName) {
        if(elementsByTagName != null && elementsByTagName.getLength() > 0) {
            for(int i=0; i < elementsByTagName.getLength(); i++) {
                Element intItem = (Element) elementsByTagName.item(i);
                NodeList diagnosisParent = intItem.getElementsByTagName("DiagnosisParent");
                if(diagnosisParent != null && diagnosisParent.getLength() > 0) {
                    for(int j=0; j < diagnosisParent.getLength(); j++) {
                        keywords.add(diagnosisParent.item(j).getChildNodes().item(0).getNodeValue());
                    }
                }

                NodeList specificDiagnosis = intItem.getElementsByTagName("SpecificDiagnosis");
                if(specificDiagnosis != null && specificDiagnosis.getLength() > 0) {
                    for(int j=0; j < specificDiagnosis.getLength(); j++) {
                        keywords.add(specificDiagnosis.item(j).getChildNodes().item(0).getNodeValue());
                    }
                }
            }
        }
    }

    private static void extractDrugNames(HashSet<String> keywords, NodeList elementsByTagName) {
        if(elementsByTagName != null && elementsByTagName.getLength() > 0) {
            for(int i=0; i < elementsByTagName.getLength(); i++) {
                Element intItem = (Element) elementsByTagName.item(i);
                NodeList interventionNameLink = intItem.getElementsByTagName("InterventionNameLink");
                if(interventionNameLink != null && interventionNameLink.getLength() > 0) {
                    for(int j=0; j < interventionNameLink.getLength(); j++) {
                        keywords.add(interventionNameLink.item(j).getChildNodes().item(0).getNodeValue());
                    }
                }
            }
        }
    }
}
