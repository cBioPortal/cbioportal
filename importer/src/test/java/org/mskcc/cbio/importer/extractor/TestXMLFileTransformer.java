/*
 *  Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * 
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 *  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 *  documentation provided hereunder is on an "as is" basis, and
 *  Memorial Sloan-Kettering Cancer Center 
 *  has no obligations to provide maintenance, support,
 *  updates, enhancements or modifications.  In no event shall
 *  Memorial Sloan-Kettering Cancer Center
 *  be liable to any party for direct, indirect, special,
 *  incidental or consequential damages, including lost profits, arising
 *  out of the use of this software and its documentation, even if
 *  Memorial Sloan-Kettering Cancer Center 
 *  has been advised of the possibility of such damage.
 */
package org.mskcc.cbio.importer.extractor;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.foundation.extractor.FileDataSource;
import org.mskcc.cbio.importer.foundation.transformer.*;
import org.mskcc.cbio.importer.model.*;
import org.mskcc.cbio.importer.persistence.staging.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TestXMLFileTransformer {

    private final static Logger logger = Logger.getLogger(TestXMLFileTransformer.class);

    public TestXMLFileTransformer() {}

    private void performTests() {
        try {
            Config config = new TransformConfig();
            Predicate xmlFileExtensionFilter = new Predicate<Path>() {
                @Override
                public boolean apply(Path input) {
                    return (input.toString().endsWith("xml"));
                }
            };
            FileDataSource fds = new FileDataSource("/tmp/foundation/dlbc/mskcc/foundation", xmlFileExtensionFilter);

            TsvStagingFileHandler svtFileHandler = new MutationFileHandlerImpl();
            CnvFileHandler cnVFileHandler = new CnvFileHandlerImpl();
            ClinicalDataFileHandler clinicalDataFileHandler = new ClinicalDataFileHandlerImpl();
            TsvStagingFileHandler fusionFileHandler = new FusionFileHandlerImpl();

            FoundationXMLTransformer transformer = new FoundationXMLTransformer(config,
                    new FoundationShortVariantTransformer(svtFileHandler),
                    new FoundationCnvTransformer(cnVFileHandler),
                    new FoundationClinicalDataTransformer(clinicalDataFileHandler),
                    new FoundationFusionTransformer(fusionFileHandler)

            );
            transformer.transform(fds);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    public static void main(String... args) {
        TestXMLFileTransformer test = new TestXMLFileTransformer();
        test.performTests();
    }

    public class TransformConfig implements Config {

        @Override
        public Collection<TumorTypeMetadata> getTumorTypeMetadata(String tumorType) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String[] getTumorTypesToDownload() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Collection<DatatypeMetadata> getDatatypeMetadata(String datatype) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Collection<DatatypeMetadata> getDatatypeMetadata(PortalMetadata portalMetadata, CancerStudyMetadata cancerStudyMetadata) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String[] getDatatypesToDownload(DataSourcesMetadata dataSourcesMetadata) throws Exception {
            return null;
        }

        @Override
        public Collection<DatatypeMetadata> getFileDatatype(DataSourcesMetadata dataSourcesMetadata, String filename) throws Exception {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Collection<CaseIDFilterMetadata> getCaseIDFilterMetadata(String filterName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Collection<CaseListMetadata> getCaseListMetadata(String caseListFilename) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Collection<ClinicalAttributesNamespace> getClinicalAttributesNamespace(String clinicalAttributesNamespaceColumnHeader) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Collection<ClinicalAttributesMetadata> getClinicalAttributesMetadata(String clinicalAttributeColumnHeader) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Map<String, ClinicalAttributesMetadata> getClinicalAttributesMetadata(Collection<String> externalColumnHeaders) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void importBCRClinicalAttributes(Collection<BCRDictEntry> bcrs) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void flagMissingClinicalAttributes(String cancerStudy, String tumorType, Collection<String> missingAttributeColumnHeaders) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Collection<PortalMetadata> getPortalMetadata(String portalName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Collection<ReferenceMetadata> getReferenceMetadata(String referenceType) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Collection<DataSourcesMetadata> getDataSourcesMetadata(String dataSource) {

            DataSourcesMetadata meta1 = new DataSourcesMetadata(new String[]{"foundation", "/tmp/foundation",
                    "TRUE", "foundationFetcher"});
            DataSourcesMetadata meta2 = new DataSourcesMetadata(new String[]{"darwin", "/tmp/darwin", "TRUE", "darwinFetcher"});
            if (dataSource.equalsIgnoreCase("foundation")) {
                return Lists.newArrayList(meta1);
            }
            if (dataSource.equalsIgnoreCase("darwin")) {
                return Lists.newArrayList(meta2);
            }
            return Lists.newArrayList();
        }

        @Override
        public Collection<CancerStudyMetadata> getCancerStudyMetadata(String portalName) {
            CancerStudyMetadata meta1 = new CancerStudyMetadata(
                    new String[]{"dlbc/mskcc/foundation",	"dlbc",	"dlbc_13-081_mskcc_foundation",	"DLBCL MYC/BCL2 13-081 & 13-158 (MSKCC / Foundation Medicine)",
                            "DLBCL samples, profiled by Foundation Medicine","","","LEVINER",	"DLBCL (MSKCC/Foundation)",	"FALSE",	"TRUE",	"FALSE",	"FALSE",
                            "x"			}
            );
            CancerStudyMetadata meta2 = new CancerStudyMetadata(
                    new String[]{"dlbc/13-081/mskcc/foundation",	"dlbc",	"dlbc_13-081_mskcc_foundation",	"DLBCL MYC/BCL2 13-081 & 13-158 (MSKCC / Foundation Medicine)",
                            "DLBCL samples, profiled by Foundation Medicine","","","LEVINER",	"DLBCL (MSKCC/Foundation)",	"FALSE",	"TRUE",	"FALSE",	"FALSE",
                            "x"			}
            );
            return Lists.newArrayList(meta1,meta2);
        }

        @Override
        public CancerStudyMetadata getCancerStudyMetadataByName(String cancerStudyName) {
            for (CancerStudyMetadata csmd : getCancerStudyMetadata("x")){
                logger.info("Study name " +cancerStudyName +" metadata name " +csmd.getStudyPath() + " " +csmd.getCancerStudyMetadataFilename());
                if (csmd.getStudyPath().equalsIgnoreCase(cancerStudyName)){


                    return  csmd;
                }
            }
            return null;
        }

        @Override
        public Collection<FoundationMetadata> getFoundationMetadata() {
            FoundationMetadata meta1 = new FoundationMetadata(new String[]{"lcll/mskcc/foundation", "13-156;13-079",
           "CLL" });
            FoundationMetadata meta2 = new FoundationMetadata(new String[]{"dlbc/mskcc/foundation", "13-158;13-081",
                    "MYC/BCL",
                    "RD0345;RD0351;RD0354;RD0355;RD0374;RD0375;RD0376;RD0377;RD0378;RD0383;RD0384;TRF031674.01;TRF031934.01",
                    "unknown","unknown"});
            FoundationMetadata meta3 = new FoundationMetadata(new String[]{"mds_aml/mskcc/foundation", "13-157;13-080",
            "transplant"});
            FoundationMetadata meta4 = new FoundationMetadata(new String[]{"dlbcl/mskcc/foundation", "13-159;13-082",
            "DLBCL"});
            FoundationMetadata meta5 = new FoundationMetadata(new String[]{"aml-all/mskcc/foundation", "13-160;13-083",
            "xenotransplant"});
            FoundationMetadata meta6 = new FoundationMetadata(new String[]{"dac-atra_mds/mskcc/foundation", "13-161;13-084",
            "DAC/ATRA"});
            FoundationMetadata meta7 = new FoundationMetadata(new String[]{"jak2-neg_mpn/mskcc/foundation", "13-162;13-085",
            "JAK2-neg"});
            FoundationMetadata meta8 = new FoundationMetadata(new String[]{"post-mpn_aml/mskcc/foundation", "13-163;13-086",
            "Post-MPN"});
            FoundationMetadata meta9 = new FoundationMetadata(new String[]{"carfilzomib-mm/mskcc/foundation", "13-164;13-087",
            "Carflizomib"});
            FoundationMetadata meta10 = new FoundationMetadata(new String[]{"lymphoma/mskcc/foundation", "CLINICAL-HEME-COMPLETE",
            "CLL" });
            FoundationMetadata meta11 = new FoundationMetadata(new String[]{"lymphoma-sa_therp/mskcc/foundation", "14-036",
                    "Lymphona-SA"});
            return Lists.newArrayList(meta1, meta2,meta3,meta4,meta5,meta6,meta7,meta8,meta9, meta10,meta11);

        }

        @Override
        public List<String> findCancerStudiesBySubstring(String substring) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Collection<IcgcMetadata> getIcgcMetadata() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void updateCancerStudyAttributes(String cancerStudy, Map<String,String> properties) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void insertCancerStudyAttributes(Map<String,String> properties) {
            throw new UnsupportedOperationException();
        }

    }
}
