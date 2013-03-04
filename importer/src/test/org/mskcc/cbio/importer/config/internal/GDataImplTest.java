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
package org.mskcc.cbio.importer.config.internal;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.BaseFeed;
import com.google.gdata.data.IFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.util.ServiceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.stubbing.OngoingStubbing;
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.model.BcrClinicalAttributeEntry;
import org.mskcc.cbio.importer.model.ClinicalAttributesMetadata;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class GDataImplTest {
//    @Test
//    public void updateClinicalAttributeTest() throws IOException, ServiceException {
//        SpreadsheetService mockService = mock(SpreadsheetService.class);
//        String gdataUser = "test_user";
//        String gdataPassword = "test_password";
//        String gdataSpreadsheet = "gdata_spreadsheet";
//        String tumorTypesWorksheet = "test_tumor_types_spreadsheet";
//        String datatypesWorksheet = "datatypes_worksheet";
//        String caseIDFiltersWorksheet = "case_id_filter_worksheet";
//        String caseListWorksheet = "caselist_worksheet";
//        String clinicalAttributesWorksheet = "clinical_attributes_worksheet";
//        String portalsWorksheet = "portals_worksheet";
//        String referenceDataWorksheet = "reference_data_worksheet";
//        String dataSourcesWorksheet = "datasource_worksheet";
//        String cancerStudiesWorksheet = "cancerstudies_worksheet";
//
//        GDataImpl testGDataImpl = new GDataImpl(gdataUser, gdataPassword, mockService,
//                gdataSpreadsheet, tumorTypesWorksheet, datatypesWorksheet,
//                caseIDFiltersWorksheet, caseListWorksheet, clinicalAttributesWorksheet,
//                portalsWorksheet, referenceDataWorksheet, dataSourcesWorksheet, cancerStudiesWorksheet);
//
//        GDataImpl gdataSpy = spy(testGDataImpl);
//
//        gdataSpy.clinicalAttributesMatrix = new ArrayList<ArrayList<String>>();
//        Collection<ClinicalAttributesMetadata> metadatas = new ArrayList<ClinicalAttributesMetadata>();
//        when(gdataSpy.getMetadataCollection(gdataSpy.clinicalAttributesMatrix, "org")).thenReturn(metadatas);
//    }
}
