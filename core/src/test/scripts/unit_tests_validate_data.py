"""
Copyright (c) 2016 The Hyve B.V.
This code is licensed under the GNU Affero General Public License (AGPL),
version 3, or (at your option) any later version.
"""

import unittest
import logging
import hugoEntrezMap
from import_data_validator import validateData


# globals:
hugo_mapping_file = 'test_data/Homo_sapiens.gene_info.gz'
ncbi_file = open(hugo_mapping_file)
hugo_entrez_map = hugoEntrezMap.parse_ncbi_file(ncbi_file)
# hard-code known clinical attributes
KNOWN_PATIENT_ATTRS = {
    "PATIENT_ID": {"display_name":"Patient Identifier","description":"Identifier to uniquely specify a patient.","datatype":"STRING","is_patient_attribute":"1","priority":"1"},
    "OS_STATUS": {"display_name":"Overall Survival Status","description":"Overall patient survival status.","datatype":"STRING","is_patient_attribute":"1","priority":"1"},
    "OS_MONTHS": {"display_name":"Overall Survival (Months)","description":"Overall survival in months since initial diagnosis.","datatype":"NUMBER","is_patient_attribute":"1","priority":"1"},
    "DFS_STATUS": {"display_name":"Disease Free Status","description":"Disease free status since initial treatment.","datatype":"STRING","is_patient_attribute":"1","priority":"1"},
    "DFS_MONTHS": {"display_name":"Disease Free (Months)","description":"Disease free (months) since initial treatment.","datatype":"NUMBER","is_patient_attribute":"1","priority":"1"},
    "SUBTYPE": {"display_name":"Subtype","description":"Subtype description.","datatype":"STRING","is_patient_attribute":"0","priority":"1"},
    "CANCER_TYPE": {"display_name":"Cancer Type","description":"Disease type.","datatype":"STRING","is_patient_attribute":"0","priority":"1"},
    "CANCER_TYPE_DETAILED": {"display_name":"Cancer Type Detailed","description":"Cancer Type Detailed.","datatype":"STRING","is_patient_attribute":"0","priority":"1"}
}
KNOWN_SAMPLE_ATTRS = {
    "SAMPLE_ID": {"display_name":"Sample Identifier","description":"A unique sample identifier.","datatype":"STRING","is_patient_attribute":"0","priority":"1"},
}

# hard-code known cancer types
KNOWN_CANCER_TYPES = {
    "brca": {"name":"Invasive Breast Carcinoma","color":"HotPink"},
    "prad": {"name":"Prostate Adenocarcinoma","color":"Cyan"}
}

# mock-code sample ids defined in a study
DEFINED_SAMPLE_IDS = ["TCGA-A1-A0SB-01", "TCGA-A1-A0SD-01", "TCGA-A1-A0SE-01", "TCGA-A1-A0SH-01", "TCGA-A2-A04U-01",
"TCGA-B6-A0RS-01", "TCGA-BH-A0HP-01", "TCGA-BH-A18P-01", "TCGA-BH-A18H-01", "TCGA-C8-A138-01", "TCGA-A2-A0EY-01", "TCGA-A8-A08G-01"]


# TODO - something like this could be done for a web-services stub:
# def dumy_request_from_portal_api(service_url, logger):
#     """Send a request to the portal API and return the decoded JSON object."""
#     if logger.isEnabledFor(logging.INFO):
#         url_split = service_url.split('/api/', 1)
#         logger.info("Requesting %s from portal at '%s'",
#                     url_split[1], url_split[0])
#     response = requests.get(service_url)
#     try:
#         response.raise_for_status()
#     except requests.exceptions.HTTPError as e:
#         raise IOError(
#             'Connection error for URL: {url}. Administrator: please check if '
#             '[{url}] is accessible. Message: {msg}'.format(url=service_url,
#                                                            msg=e.message))
#     return response.json()
# 
# validateData.request_from_portal_api = dumy_request_from_portal_api


class LogBufferTestCase(unittest.TestCase):

    """Superclass for testcases that want to capture log records emitted.

    Defines a self.logger to log to, and a method get_log_records() to
    collect the list of LogRecords emitted by this logger. In addition,
    defines a function print_log_records to format a list of LogRecords
    to standard output.
    """

    def setUp(self):
        """Set up a logger with a buffering handler."""
        self.logger = logging.getLogger(self.__class__.__name__)
        self.logger.setLevel(logging.INFO)
        handler = logging.handlers.BufferingHandler(capacity=1e6)
        self.orig_handlers = self.logger.handlers
        self.logger.handlers = [handler]

    def tearDown(self):
        """Remove the logger handler (and any buffer it may have)."""
        self.logger.handlers = self.orig_handlers

    def get_log_records(self):
        """Get the log records written to the logger since the last call."""
        recs = self.logger.handlers[0].buffer
        self.logger.handlers[0].flush()
        return recs

    @staticmethod
    def print_log_records(record_list):
        """Pretty-print a list of log records to standard output.

        This can be used if, while writing unit tests, you want to see
        what the messages currently are. The final unit tests committed
        to version control should not print log messages.
        """
        formatter = validateData.LogfileStyleFormatter()
        for record in record_list:
            print formatter.format(record)


class DataFileTestCase(LogBufferTestCase):

    """Superclass for testcases validating a particular data file.

    Provides a validate() method to validate the data file with a
    particular validator class and collect the log records emitted.
    """

    def setUp(self):
        """Set up for validating a file in the test_data directory."""
        super(DataFileTestCase, self).setUp()
        self.orig_study_dir = validateData.STUDY_DIR
        validateData.STUDY_DIR = 'test_data'
        # hard-code known clinical attributes instead of contacting a portal
        self.orig_srv_attrs = validateData.ClinicalValidator.srv_attrs
        mock_srv_attrs = dict(KNOWN_PATIENT_ATTRS)
        mock_srv_attrs.update(KNOWN_SAMPLE_ATTRS)
        validateData.ClinicalValidator.srv_attrs = mock_srv_attrs

    def tearDown(self):
        """Restore the environment to before setUp() was called."""
        validateData.STUDY_DIR = self.orig_study_dir
        validateData.ClinicalValidator.srv_attrs = self.orig_srv_attrs
        super(DataFileTestCase, self).tearDown()

    def validate(self, data_filename, validator_class, extra_meta_fields=None):
        """Validate a file with a Validator and return the log records."""
        meta_dict = {'data_file_path': data_filename}
        if extra_meta_fields is not None:
            meta_dict.update(extra_meta_fields)
        validator = validator_class(hugo_entrez_map, self.logger, meta_dict)
        validator.validate()
        return self.get_log_records()


class PostClinicalDataFileTestCase(DataFileTestCase):

    """Superclass for validating data files to be read after clinical files.

    I.e. DEFINED_SAMPLE_IDS will be initialised with a list of sample
    identifiers defined in the study.
    """

    def setUp(self):
        """Prepare for validating a file by setting the samples defined."""
        super(PostClinicalDataFileTestCase, self).setUp()
        self.orig_defined_sample_ids = validateData.DEFINED_SAMPLE_IDS
        validateData.DEFINED_SAMPLE_IDS = DEFINED_SAMPLE_IDS

    def tearDown(self):
        """Restore the environment to before setUp() was called."""
        validateData.DEFINED_SAMPLE_IDS = self.orig_defined_sample_ids
        super(PostClinicalDataFileTestCase, self).tearDown()


# ----------------------------------------------------------------------------
# Test cases for the various Validator classes found in validateData script

class ColumnOrderTestCase(DataFileTestCase):

    """Test if column order requirements are appropriately validated."""

    def test_column_order_validation_SegValidator(self):
        """seg validator needs its columns in a specific order.

        Here we serve a file with wrong order and expect validator to log this:
        """
        # set level according to this test case:
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_seg_wrong_order.txt',
                                    validateData.SegValidator)
        # we expect 2 errors about columns in wrong order,
        # and one about the file not being parseable:
        self.assertEqual(len(record_list), 3)
        # check if both messages come from checkOrderedRequiredColumns:
        for error in record_list[:2]:
            self.assertEqual("ERROR", error.levelname)
            self.assertEqual("_checkOrderedRequiredColumns", error.funcName)

    def test_column_order_validation_ClinicalValidator(self):
        """ClinicalValidator does NOT need its columns in a specific order.

        Here we serve files with different order and no errors or warnings
        """
        # set level according to this test case:
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_clin_order1.txt',
                                    validateData.ClinicalValidator)
        # we expect no errors or warnings
        self.assertEqual(0, len(record_list))
        # if the file has another order, this is also OK:
        record_list = self.validate('data_clin_order2.txt',
                                    validateData.ClinicalValidator)
        # again, we expect no errors or warnings
        self.assertEqual(0, len(record_list))


class ClinicalColumnDefsTestCase(DataFileTestCase):

    """Tests for validations of the column definitions in a clinical file."""

    def test_correct_definitions(self):
        """Test when all record definitions match with portal."""
        record_list = self.validate('data_clin_coldefs_correct.txt',
                                    validateData.ClinicalValidator)
        # expecting two info messages: at start and end of file
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.INFO)

    def test_wrong_definitions(self):
        """Test when record definitions do not match with portal."""
        record_list = self.validate('data_clin_coldefs_wrong_display_name.txt',
                                    validateData.ClinicalValidator)
        # expecting an info message followed by the error, and another error as
        # the rest of the file cannot be parsed
        self.assertEqual(len(record_list), 3)
        # error about the display name of OS_MONTHS
        self.assertEqual(record_list[1].levelno, logging.ERROR)
        self.assertEqual(record_list[1].column_number, 3)
        self.assertIn('display_name', record_list[1].getMessage().lower())

    def test_unknown_attribute(self):
        """Test when a new attribute is defined in the data file."""
        record_list = self.validate('data_clin_coldefs_unknown_attribute.txt',
                                    validateData.ClinicalValidator)
        # expecting two info messages with a warning in between
        self.assertEqual(len(record_list), 3)
        self.assertEqual(record_list[1].levelno, logging.WARNING)
        self.assertEqual(record_list[1].column_number, 7)
        self.assertIn('will be added', record_list[1].getMessage().lower())

    def test_invalid_definitions(self):
        """Test when new attributes are defined with invalid properties."""
        record_list = self.validate('data_clin_coldefs_invalid_priority.txt',
                                    validateData.ClinicalValidator)
        # expecting an info message followed by the error, and another error as
        # the rest of the file cannot be parsed
        self.assertEqual(len(record_list), 3)
        # error about the non-numeric priority of the SAUSAGE column
        self.assertEqual(record_list[1].levelno, logging.ERROR)
        self.assertEqual(record_list[1].line_number, 5)
        self.assertEqual(record_list[1].column_number, 7)


class CancerTypeValidationTestCase(LogBufferTestCase):

    """Tests for validations of cancer type meta files in a study."""

    def setUp(self):
        """Initialize environment, hard-coding known cancer types."""
        super(CancerTypeValidationTestCase, self).setUp()
        self.orig_portal_cancer_types = validateData.PORTAL_CANCER_TYPES
        validateData.PORTAL_CANCER_TYPES = KNOWN_CANCER_TYPES

    def tearDown(self):
        """Restore the environment to before setUp() was called."""
        super(CancerTypeValidationTestCase, self).tearDown()
        validateData.PORTAL_CANCER_TYPES = self.orig_portal_cancer_types

    def test_new_cancer_type(self):
        """Test when a study defines a new cancer type."""
        # {"id":"luad","name":"Lung Adenocarcinoma","color":"Gainsboro"}
        validateData.process_metadata_files(
            'test_data/study_metacancertype_lung',
            self.logger, hugo_entrez_map)
        record_list = self.get_log_records()
        # expecting a warning about a new cancer type being added
        self.assertEqual(len(record_list), 1)
        self.assertEqual(record_list[0].levelno, logging.WARNING)
        self.assertEqual(record_list[0].cause, 'luad')

    def test_cancer_type_file_format_error(self):
        """Test when a new cancer type file does not make sense."""
        self.logger.setLevel(logging.ERROR)
        validateData.process_metadata_files(
            'test_data/study_metacancertype_missing_color',
            self.logger, hugo_entrez_map)
        record_list = self.get_log_records()
        # expecting two errors: one about the missing field and one about the
        # undefined cancer type of the study
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record_list[0].data_filename, 'meta_cancer_type.txt')
        self.assertIn('dedicated_color', record_list[0].getMessage())
        self.assertEqual(record_list[1].cause, 'luad')

    def test_cancer_type_matching_portal(self):
        """Test when an existing cancer type is defined exactly as known."""
        validateData.process_metadata_files(
            'test_data/study_metacancertype_confirming_existing',
            self.logger, hugo_entrez_map)
        record_list = self.get_log_records()
        # expecting no messages, warnings or errors
        self.assertEqual(len(record_list), 0)

    def test_cancer_type_disagreeing_with_portal(self):
        """Test when an existing cancer type is redefined by a study."""
        validateData.process_metadata_files(
            'test_data/study_metacancertype_redefining',
            self.logger, hugo_entrez_map)
        record_list = self.get_log_records()
        # expecting an error message about the cancer type file, but none about
        # the rest of the study as the portal defines a valid cancer type
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.data_filename, 'meta_cancer_type.txt')
        self.assertEqual(record.cause, 'Breast Cancer')

    def test_cancer_type_defined_twice(self):
        """Test when a study defines the same cancer type id twice.

        No difference is assumed between matching and disagreeing definitions;
        this validation just fails as it never makes sense to do this.
        """
        self.logger.setLevel(logging.ERROR)
        validateData.process_metadata_files(
            'test_data/study_metacancertype_lung_twice',
            self.logger, hugo_entrez_map)
        record_list = self.get_log_records()
        # expecting an error message about the doubly-defined cancer type
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.cause, 'luad')
        self.assertIn('defined a second time', record.getMessage())

class GeneIdColumnsTestCase(PostClinicalDataFileTestCase):

    """Tests validating gene-wise files with different combinations of gene id columns,
    now with invalid Entrez ID and/or Hugo names """

    def test_both_name_and_entrez(self):
        """Test when a file has both the Hugo name and Entrez ID columns."""
        record_list = self.validate('data_cna_genecol_presence_both.txt',
                                    validateData.CNAValidator)
        # expecting two info messages: at start and end of file
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.INFO)

    def test_name_only(self):
        """Test when a file has a Hugo name column but none for Entrez IDs."""
        record_list = self.validate('data_cna_genecol_presence_hugo_only.txt',
                                    validateData.CNAValidator)
        # expecting two info messages: at start and end of file
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.INFO)

    def test_entrez_only(self):
        """Test when a file has an Entrez ID column but none for Hugo names."""
        record_list = self.validate('data_cna_genecol_presence_entrez_only.txt',
                                    validateData.CNAValidator)
        # expecting two info messages: at start and end of file
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.INFO)

    def test_neither_name_nor_entrez(self):
        """Test when a file lacks both the Entrez ID and Hugo name columns."""
        record_list = self.validate('data_cna_genecol_presence_neither.txt',
                                    validateData.CNAValidator)
        # two errors after the info: the first makes the file unparsable
        self.assertEqual(len(record_list), 3)
        for record in record_list[1:]:
            self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record_list[1].line_number, 1)

    def test_both_name_and_entrez_but_invalid_hugo(self):
        """Test when a file has both the Hugo name and Entrez ID columns, but hugo is invalid."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_cna_genecol_presence_both_invalid_hugo.txt',
                                    validateData.CNAValidator)
        # expecting two info messages: 
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.ERROR)
        # expecting these to be the cause:    
        self.assertEqual(record_list[0].cause, 'xxACAP3')
        self.assertEqual(record_list[1].cause, 'xxAGRN')

    def test_both_name_and_entrez_but_invalid_entrez(self):
        """Test when a file has both the Hugo name and Entrez ID columns, but entrez is invalid."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_cna_genecol_presence_both_invalid_entrez.txt',
                                    validateData.CNAValidator)
        # expecting two error messages: 
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.ERROR)
        # expecting these to be the cause:    
        self.assertIn('-116983', record_list[0].cause)
        self.assertIn('-375790', record_list[1].cause)


class MutationsSpecialCasesTestCase(PostClinicalDataFileTestCase):

    def test_normal_samples_list_in_maf(self):
        '''
        For mutations MAF files there is a column called "Matched_Norm_Sample_Barcode". 
        In the respective meta file it is possible to give a list of sample codes against which this 
        column "Matched_Norm_Sample_Barcode" is validated. Here we test if this 
        validation works well.
        '''
        # set level according to this test case:
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_mutations_invalid_norm_samples.maf',
                                    validateData.MutationsExtendedValidator, 
                                    {'normal_samples_list': 
                                     'TCGA-B6-A0RS-10,TCGA-BH-A0HP-10,TCGA-BH-A18P-11, TCGA-BH-A18H-10'})
        # we expect 2 errors about columns in wrong order,
        # and one about the file not being parseable:
        self.assertEqual(len(record_list), 3)
        # check if both messages come from printDataInvalidStatement:
        found_one_of_the_expected = False
        for error in record_list[:2]:
            self.assertEqual("ERROR", error.levelname)
            self.assertEqual("printDataInvalidStatement", error.funcName)
            if "TCGA-C8-A138-10" == error.cause: 
                found_one_of_the_expected = True

        self.assertEqual(True, found_one_of_the_expected)
