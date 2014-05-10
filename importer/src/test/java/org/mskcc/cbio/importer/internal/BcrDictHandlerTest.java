/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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
package org.mskcc.cbio.importer.internal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.assertTrue;

import org.mskcc.cbio.importer.model.BcrClinicalAttributeEntry;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class BcrDictHandlerTest {

    @Test
    public void parseTest() throws ParserConfigurationException, SAXException, IOException {

        List<BcrClinicalAttributeEntry> bcrs = new ArrayList<BcrClinicalAttributeEntry>();

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        BcrDictHandler handler = new BcrDictHandler(bcrs);

        String testXml = "<dictionary></dictionary>";
        InputStream stream = new ByteArrayInputStream(testXml.getBytes());
        saxParser.parse(stream, handler);
        assertTrue(bcrs.isEmpty());

        testXml = "<dictionary>" +
                "<dictEntry name=\"The OFFICIAL name\">" +
                "<caDSRdefinition>the great thing about standards is that there are so many of them</caDSRdefinition>" +
                "<XMLeltInfo xml_elt_name=\"an_attribute_id\"/>" +
                "<studies><study>TCGA1</study><study>TCGA2</study></studies>" +
                "<random name=\"nonesense\"/>" +
                "<random>Lots of sense </random>" +
                "</dictEntry>" +
                "</dictionary>";
        stream = new ByteArrayInputStream(testXml.getBytes());
        saxParser.parse(stream, handler);

        assertTrue(bcrs.size() == 1);

        BcrClinicalAttributeEntry bcr = bcrs.get(0);

        assertTrue(bcr.getId().equals("an_attribute_id"));
        assertTrue(bcr.getDescription().equals("the great thing about standards is that there are so many of them"));
        assertTrue(bcr.getDisplayName().equals("The OFFICIAL name"));
        assertTrue(bcr.getDiseaseSpecificity().equals("TCGA1 TCGA2"));
    }
}
