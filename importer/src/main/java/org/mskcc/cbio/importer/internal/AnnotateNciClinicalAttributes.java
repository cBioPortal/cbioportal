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

package org.mskcc.cbio.importer.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.cgds.model.ClinicalAttribute;
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.DatabaseUtils;
import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.Importer;
import org.mskcc.cbio.importer.model.ReferenceMetadata;
import org.mskcc.cbio.importer.util.Shell;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class AnnotateNciClinicalAttributes implements Importer {

    // our logger
    private static final Log LOG = LogFactory.getLog(ImporterImpl.class);

    // ref to configuration
    private Config config;

    // ref to file utils
    private FileUtils fileUtils;

    // ref to database utils
    private DatabaseUtils databaseUtils;

    public static final String DICT_ENTRY = "dictEntry";
    public static final String NAME = "name";

    /**
     * Constructor.
     *
     * @param config Config
     * @param fileUtils FileUtils
     * @param databaseUtils DatabaseUtils
     */
    public AnnotateNciClinicalAttributes(Config config, FileUtils fileUtils, DatabaseUtils databaseUtils) {

        // set members
        this.config = config;
        this.fileUtils = fileUtils;
        this.databaseUtils = databaseUtils;
    }

    @Override
    public void importData(String portal, Boolean initPortalDatabase, Boolean initTumorTypes, Boolean importReferenceData) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void importReferenceData(ReferenceMetadata referenceMetadata) throws Exception {

        String bcrXmlFilename = referenceMetadata.getImporterArgs().get(0);

        FileInputStream in = new FileInputStream(bcrXmlFilename);

        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

        while (eventReader.hasNext()) {
            XMLEvent xmlEvent = eventReader.nextEvent();
            String attributeId;
            String displayName;
            String description;
            String datatype;

            if (xmlEvent.isStartElement()) {
                StartElement startElement = xmlEvent.asStartElement();

                Iterator<Attribute> attributes =  startElement.getAttributes();

                while (attributes.hasNext()) {
                    Attribute attr = attributes.next();

                    if (attr.getName().getLocalPart().equals(NAME)) {
                        displayName = attr.getValue();
                    }
                }

                if (xmlEvent.isStartElement()) {
                    if (xmlEvent.asStartElement().getName().getLocalPart().equals("caDSRdefinition")) {
                        System.out.println("Hello World");
                    }
                }
            }
        }
    }
}
