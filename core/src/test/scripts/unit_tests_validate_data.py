#!/usr/bin/env python2.7

"""
Copyright (c) 2016 The Hyve B.V.
This code is licensed under the GNU Affero General Public License (AGPL),
version 3, or (at your option) any later version.
"""

import unittest
import sys
import logging.handlers
from importer import cbioportal_common
from importer import validateData


# globals for mock data used throughout the module
DEFINED_SAMPLE_IDS = None
DEFINED_SAMPLE_ATTRIBUTES = None
PATIENTS_WITH_SAMPLES = None
PORTAL_INSTANCE = None
GSVA_SAMPLE_IDS = None
GSVA_GENESET_IDS = None

def setUpModule():
    """Initialise mock data used throughout the module."""
    global DEFINED_SAMPLE_IDS
    global DEFINED_SAMPLE_ATTRIBUTES
    global PATIENTS_WITH_SAMPLES
    global PORTAL_INSTANCE
    # mock information parsed from the sample attribute file
    DEFINED_SAMPLE_IDS = ["TCGA-A1-A0SB-01", "TCGA-A1-A0SD-01", "TCGA-A1-A0SE-01", "TCGA-A1-A0SH-01", "TCGA-A2-A04U-01", "TCGA-B6-A0RS-01", "TCGA-BH-A0HP-01", "TCGA-BH-A18P-01", "TCGA-BH-A18H-01", "TCGA-C8-A138-01", "TCGA-A2-A0EY-01", "TCGA-A8-A08G-01"]
    DEFINED_SAMPLE_ATTRIBUTES = {'PATIENT_ID', 'SAMPLE_ID', 'SUBTYPE', 'CANCER_TYPE', 'CANCER_TYPE_DETAILED'}
    PATIENTS_WITH_SAMPLES = set("TEST-PAT{}".format(num) for
                                num in range(1, 10) if
                                num != 8)
    logger = logging.getLogger(__name__)
    # parse mock API results from a local directory
    PORTAL_INSTANCE = validateData.load_portal_info('test_data/api_json_unit_tests/',
                                                    logger,
                                                    offline=True)


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
        self.logger.setLevel(logging.DEBUG)
        # add a handler to buffer log records for validation
        self.buffer_handler = logging.handlers.BufferingHandler(capacity=1e6)
        self.logger.addHandler(self.buffer_handler)
        # add a handler to pretty-print log messages to the output
        self.output_handler = logging.StreamHandler(sys.stdout)
        self.output_handler.setFormatter(
            cbioportal_common.LogfileStyleFormatter(
                'test_data/'))
        self.logger.addHandler(self.output_handler)

    def tearDown(self):
        """Remove the logger handlers (and any buffers they may have)."""
        self.logger.removeHandler(self.output_handler)
        self.logger.removeHandler(self.buffer_handler)

    def get_log_records(self):
        """Get the log records written to the logger since the last call."""
        recs = self.buffer_handler.buffer
        self.buffer_handler.flush()
        return recs


class DataFileTestCase(LogBufferTestCase):

    """Superclass for testcases validating a particular data file.

    Provides a validate() method to validate the data file with a
    particular validator class and collect the log records emitted.
    """

    def validate(self, data_filename, validator_class, extra_meta_fields=None, relaxed_mode=False):
        """Validate a file with a Validator and return the log records."""
        meta_dict = {'data_filename': data_filename}
        if extra_meta_fields is not None:
            meta_dict.update(extra_meta_fields)
        validator = validator_class('test_data', meta_dict,
                                    PORTAL_INSTANCE,
                                    self.logger, relaxed_mode)
        validator.validate()
        return self.get_log_records()


class PostClinicalDataFileTestCase(DataFileTestCase):

    """Superclass for validating data files to be read after sample attr files.

    I.e. DEFINED_SAMPLE_IDS will be initialised with a list of sample
    identifiers defined in the study.
    """

    def setUp(self):
        """Prepare for validating a file by setting the samples defined."""
        super(PostClinicalDataFileTestCase, self).setUp()
        self.orig_defined_sample_ids = validateData.DEFINED_SAMPLE_IDS
        validateData.DEFINED_SAMPLE_IDS = DEFINED_SAMPLE_IDS
        self.orig_defined_sample_attributes = validateData.DEFINED_SAMPLE_IDS
        validateData.DEFINED_SAMPLE_ATTRIBUTES = DEFINED_SAMPLE_ATTRIBUTES
        self.orig_patients_with_samples = validateData.PATIENTS_WITH_SAMPLES
        validateData.PATIENTS_WITH_SAMPLES = PATIENTS_WITH_SAMPLES
        
        # reset all GSVA global variables when starting a test
        self.orig_gsva_sample_ids = validateData.GSVA_SAMPLE_IDS
        validateData.GSVA_SAMPLE_IDS = GSVA_SAMPLE_IDS
        self.orig_gsva_geneset_ids = validateData.GSVA_GENESET_IDS
        validateData.GSVA_GENESET_IDS = GSVA_GENESET_IDS

    def tearDown(self):
        """Restore the environment to before setUp() was called."""
        validateData.DEFINED_SAMPLE_IDS = self.orig_defined_sample_ids
        validateData.DEFINED_SAMPLE_ATTRIBUTES = self.orig_defined_sample_attributes
        validateData.PATIENTS_WITH_SAMPLES = self.orig_patients_with_samples
        validateData.GSVA_SAMPLE_IDS = self.orig_gsva_sample_ids
        validateData.GSVA_GENESET_IDS = self.orig_gsva_geneset_ids
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
                                    validateData.SegValidator,
                                    extra_meta_fields={'reference_genome_id':
                                                           'hg19'})
        # we expect 2 errors about columns in wrong order,
        # and one about the file not being parseable:
        self.assertEqual(len(record_list), 3)
        # check if both messages come from checkOrderedRequiredColumns:
        for error in record_list[:2]:
            self.assertEqual("ERROR", error.levelname)
            self.assertEqual("_checkOrderedRequiredColumns", error.funcName)

    def test_column_order_validation_ClinicalValidator(self):
        """Sample attributes do NOT need their columns in a specific order.

        Here we serve files with different order and no errors or warnings
        """
        # set level according to this test case:
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_clin_order1.txt',
                                    validateData.SampleClinicalValidator)
        # we expect no errors or warnings
        self.assertEqual(0, len(record_list))
        # if the file has another order, this is also OK:
        record_list = self.validate('data_clin_order2.txt',
                                    validateData.SampleClinicalValidator)
        # again, we expect no errors or warnings
        self.assertEqual(0, len(record_list))


class ClinicalColumnDefsTestCase(PostClinicalDataFileTestCase):

    """Tests for validations of the column definitions in a clinical file."""

    def test_correct_definitions(self):
        """Test when all record definitions match with expectations."""
        record_list = self.validate('data_clin_coldefs_correct.txt',
                                    validateData.PatientClinicalValidator)
        # expecting only status messages about the file being validated
        self.assertEqual(len(record_list), 3)
        for record in record_list:
            self.assertLessEqual(record.levelno, logging.INFO)

    def test_invalid_definitions(self):
        """Test when attributes are defined with unparseable properties."""
        record_list = self.validate('data_clin_coldefs_invalid_priority.txt',
                                    validateData.PatientClinicalValidator)
        # expecting an info message followed by the error, and another error as
        # the rest of the file cannot be parsed
        self.assertEqual(len(record_list), 3)
        # error about the non-numeric priority of the SAUSAGE column
        self.assertEqual(record_list[1].levelno, logging.ERROR)
        self.assertEqual(record_list[1].line_number, 4)
        self.assertEqual(record_list[1].column_number, 6)

    def test_hardcoded_attributes(self):

        """Test requirements on the data type or level of some attributes."""

        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_clin_coldefs_hardcoded_attrs.txt',
                                    validateData.PatientClinicalValidator)
        self.assertEqual(len(record_list), 3)
        osmonths_records = []
        other_sid_records = []
        other_warn_records = []
        for record in record_list:
            self.assertNotIn('portal', record.getMessage().lower())
            if 'OS_MONTHS' in record.getMessage():
                osmonths_records.append(record)
            if hasattr(record, 'cause') and record.cause == 'OTHER_SAMPLE_ID':
                other_sid_records.append(record)
            if 'details will be missing' in record.getMessage():
                other_warn_records.append(record)

        self.assertEqual(len(osmonths_records), 1)
        record = osmonths_records.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 3)
        self.assertEqual(record.column_number, 2)

        self.assertEqual(len(other_sid_records), 1)
        record = other_sid_records.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 5)
        self.assertEqual(record.column_number, 6)

        self.assertEqual(len(other_warn_records), 1)
        record = other_warn_records.pop()
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertEqual(record.line_number, 5)
        self.assertEqual(record.column_number, 7)



class ClinicalValuesTestCase(DataFileTestCase):

    """Tests for validations on the values of clinical attributes."""

    def test_sample_twice_in_one_file(self):
        """Test when a sample is defined twice in the same file."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_clin_repeated_sample.txt',
                                    validateData.SampleClinicalValidator)
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 11)
        self.assertEqual(record.column_number, 2)

    def test_tcga_sample_twice_in_one_file(self):
        """Test when a TCGA sample is defined twice in the same file."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_clin_repeated_tcga_sample.txt',
                                    validateData.SampleClinicalValidator)
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertEqual(record.line_number, 11)
        self.assertEqual(record.column_number, 2)


class PatientAttrFileTestCase(PostClinicalDataFileTestCase):

    """Tests for validation of values specific to patient attribute files."""

    def test_patient_without_samples(self):
        """Test if a warning is issued for patients absent in the sample file."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_clin_patient_without_samples.txt',
                                    validateData.PatientClinicalValidator)
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertEqual(record.line_number, 10)
        self.assertEqual(record.cause, 'CASPER')
        self.assertIn('sample', record.getMessage().lower())

    def test_patient_without_attributes(self):
        """Test if a warning is issued for patients absent in the patient file."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_clin_missing_patient.txt',
                                    validateData.PatientClinicalValidator)
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertFalse(hasattr(record, 'line_number'),
                         'logrecord is about a specific line')
        self.assertEqual(record.cause, 'TEST-PAT4')
        self.assertIn('missing', record.getMessage().lower())

    def test_hardcoded_attr_values(self):
        """Test if attributes with set meanings have recognized values."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_clin_hardcoded_attr_vals.txt',
                                    validateData.PatientClinicalValidator)
        self.assertEqual(len(record_list), 5)
        record_iterator = iter(record_list)
        # OS_STATUS not in controlled vocabulary
        record = record_iterator.next()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 6)
        self.assertEqual(record.column_number, 3)
        self.assertEqual(record.cause, 'ALIVE')
        # DFS_STATUS having an OS_STATUS value
        record = record_iterator.next()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 7)
        self.assertEqual(record.column_number, 5)
        self.assertEqual(record.cause, 'LIVING')
        # wrong casing for OS_STATUS
        record = record_iterator.next()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 9)
        self.assertEqual(record.column_number, 3)
        self.assertEqual(record.cause, 'living')
        # DFS_STATUS not in controlled vocabulary (wrong casing)
        record = record_iterator.next()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 11)
        self.assertEqual(record.column_number, 5)
        self.assertEqual(record.cause, 'recurred/progressed')
        # unspecified OS_MONTHS while OS_STATUS is DECEASED
        record = record_iterator.next()
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertEqual(record.line_number, 13)
        self.assertIn('OS_MONTHS is not specified for deceased patient. Patient '
                      'will be excluded from survival curve and month of death '
                      'will not be shown on patient view timeline.',
                      record.getMessage())


# TODO: make tests in this testcase check the number of properly defined types
class CancerTypeFileValidationTestCase(DataFileTestCase):

    """Tests for validations of cancer type files in a study."""

    def test_new_cancer_type(self):
        """Test when a study defines a new cancer type."""
        # {"id":"luad","name":"Lung Adenocarcinoma","color":"Gainsboro"}
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_cancertype_lung.txt',
                                    validateData.CancerTypeValidator)
        # expecting a warning about a new cancer type being added
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertEqual(record.cause, 'luad')
        self.assertIn('will be added', record.getMessage().lower())

    def test_cancer_type_missing_column(self):
        """Test when a new cancer type file misses a column."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_cancertype_missing_color_col.txt',
                                    validateData.CancerTypeValidator)
        # expecting an error about the number of columns
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.ERROR)
            self.assertIn('columns', record.getMessage())

    def test_cancer_type_missing_value(self):
        """Test when a new cancer type has a blank required field."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_cancertype_blank_color_col.txt',
                                    validateData.CancerTypeValidator)
        # expecting an error about the missing value
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.column_number, 4)
        self.assertIn('blank', record.getMessage().lower())

    def test_cancer_type_undefined_parent(self):
        """Test when a new cancer type's parent cancer type is not known."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_cancertype_undefined_parent.txt',
                                    validateData.CancerTypeValidator)
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.column_number, 5)

    def test_cancer_type_invalid_color(self):
        """Test error if a cancer type's color is not a web color name."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_cancertype_invalid_color.txt',
                                    validateData.CancerTypeValidator)
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.column_number, 4)

    def test_cancer_type_matching_portal(self):
        """Test when an existing cancer type is defined exactly as known."""
        record_list = self.validate('data_cancertype_confirming_existing.txt',
                                    validateData.CancerTypeValidator)
        # expecting only status messages about the file being validated
        self.assertEqual(len(record_list), 3)
        for record in record_list:
            self.assertLessEqual(record.levelno, logging.INFO)

    def test_cancer_type_disagreeing_with_portal(self):
        """Test when an existing cancer type is redefined by a study."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_cancertype_redefining.txt',
                                    validateData.CancerTypeValidator)
        # expecting an error message about the cancer type file
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.column_number, 2)
        self.assertEqual(record.cause, 'Breast Cancer')

    def test_cancer_type_defined_twice(self):
        """Test when a study defines the same cancer type id twice.

        No difference is assumed between matching and disagreeing definitions;
        this validation just fails as it never makes sense to do this.
        """
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_cancertype_lung_twice.txt',
                                    validateData.CancerTypeValidator)
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
        # expecting only status messages about the file being validated
        self.assertEqual(len(record_list), 3)
        for record in record_list:
            self.assertLessEqual(record.levelno, logging.INFO)

    def test_name_only(self):
        """Test when a file has a Hugo name column but none for Entrez IDs."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_cna_genecol_presence_hugo_only.txt',
                                    validateData.CNAValidator)
        # expecting 1 warning
        self.assertEqual(len(record_list), 1)
        for record in record_list:
            self.assertEqual(record.levelno, logging.WARNING)

    def test_entrez_only(self):
        """Test when a file has an Entrez ID column but none for Hugo names."""
        record_list = self.validate('data_cna_genecol_presence_entrez_only.txt',
                                    validateData.CNAValidator)
        # expecting only status messages about the file being validated
        self.assertEqual(len(record_list), 3)
        for record in record_list:
            self.assertLessEqual(record.levelno, logging.INFO)

    def test_neither_name_nor_entrez(self):
        """Test when a file lacks both the Entrez ID and Hugo name columns."""
        record_list = self.validate('data_cna_genecol_presence_neither.txt',
                                    validateData.CNAValidator)
        # two errors after the info: the first makes the file unparsable
        self.assertEqual(len(record_list), 3)
        for record in record_list[1:]:
            self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record_list[1].line_number, 1)

    # Tests validating gene-wise files with different combinations of gene id columns,
    # now with invalid Entrez ID and/or Hugo names """

    def test_both_name_and_entrez_but_invalid_hugo(self):
        """Test when a file has both the Hugo name and Entrez ID columns, but hugo is invalid."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_cna_genecol_presence_both_invalid_hugo.txt',
                                    validateData.CNAValidator)
        # expecting two error messages:
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.WARNING)
        # expecting these to be the cause:
        self.assertEqual(record_list[0].cause, 'XXACAP3')
        self.assertEqual(record_list[1].cause, 'XXAGRN')

    def test_both_name_and_entrez_but_invalid_entrez(self):
        """Test when a file has both the Hugo name and Entrez ID columns, but entrez is invalid."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_cna_genecol_presence_both_invalid_entrez.txt',
                                    validateData.CNAValidator)
        # expecting two warning messages:
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.WARNING)
        # expecting these to be the cause:
        self.assertEqual(record_list[0].cause, '999999999')
        self.assertEqual(record_list[1].cause, '888888888')

    def test_both_name_and_entrez_but_invalid_couple(self):
        """Test when a file has both the Hugo name and Entrez ID columns, both valid, but association is invalid."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_cna_genecol_presence_both_invalid_couple.txt',
                                    validateData.CNAValidator)
        # expecting two error messages:
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.WARNING)
        # expecting these to be the cause:
        self.assertIn('ACAP3', record_list[0].cause)
        self.assertIn('116983', record_list[1].cause)

    def test_name_only_but_invalid(self):
        """Test when a file has a Hugo name column but none for Entrez IDs, and hugo is wrong."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_cna_genecol_presence_hugo_only_invalid.txt',
                                    validateData.CNAValidator)
        # expecting two warning messages:
        self.assertEqual(len(record_list), 3)
        for record in record_list:
            self.assertEqual(record.levelno, logging.WARNING)
        # expecting these to be the cause:
        self.assertIn('The recommended column Entrez_Gene_Id', record_list[0].message)
        self.assertEqual(record_list[1].cause, 'XXATAD3A')
        self.assertEqual(record_list[2].cause, 'XXATAD3B')

    def test_name_only_but_ambiguous(self):
        """Test when a file has a Hugo name column but none for Entrez IDs, and hugo maps to multiple Entrez ids.
        This test is also an indirect test of the aliases functionality as this is now the only place
        where ambiguity could arise (in gene table Hugo symbol is now unique)"""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_cna_genecol_presence_hugo_only_ambiguous.txt',
                                    validateData.CNAValidator)
        # expecting one error message
        self.assertEqual(len(record_list), 2)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.WARNING)
        # expecting this gene to be the cause
        self.assertEquals(record.cause, 'TRAPPC2P1')

    def test_entrez_only_but_invalid(self):
        """Test when a file has an Entrez ID column but none for Hugo names, and entrez is wrong."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_cna_genecol_presence_entrez_only_invalid.txt',
                                    validateData.CNAValidator)
        # expecting two warning messages:
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.WARNING)
        # expecting these to be the cause:
        self.assertEqual(record_list[0].cause, '1073741824')
        self.assertEqual(record_list[1].cause, '2147483647')

    def test_unambiguous_hugo_also_used_as_alias(self):
        """Test referencing a gene by a Hugo symbol occurring as an alias too.

        This should yield a warning, as the gene for which it is an alias
        might be the gene intended by the user.
        """
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_cna_genecol_presence_hugo_only_possible_alias.txt',
                                    validateData.CNAValidator)
        # expecting one error message
        self.assertEqual(len(record_list), 2)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.WARNING)
        # expecting this gene to be the cause
        self.assertEquals(record.cause, 'ACT')

    def test_blank_column_heading(self):
        """Test whether an error is issued if a column has a blank name."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_cna_blank_heading.txt',
                                    validateData.CNAValidator)
        self.assertEqual(len(record_list), 4)
        for record in record_list:
            self.assertEqual(record.levelno, logging.ERROR)
        record_iterator = iter(record_list)
        record = record_iterator.next()
        self.assertEqual(record.line_number, 1)
        self.assertEqual(record.column_number, 2)
        self.assertEqual(record.cause, '')
        record = record_iterator.next()
        self.assertEqual(record.line_number, 1)
        self.assertEqual(record.column_number, 8)
        self.assertEqual(record.cause, '  ')
        record = record_iterator.next()
        self.assertIn('white space in sample_id', record.getMessage().lower())
        record = record_iterator.next()
        self.assertIn('cannot be parsed', record.getMessage().lower())

    # TODO - add extra unit tests for the genesaliases scenarios (now only test_name_only_but_ambiguous tests part of this)


class FeatureWiseValuesTestCase(PostClinicalDataFileTestCase):

    """Verify that values are being checked in feature/sample matrix files."""

    def test_valid_discrete_cna(self):
        """Check a valid discrete CNA file that should yield no errors."""
        self.logger.setLevel(logging.DEBUG)
        record_list = self.validate('data_cna_genecol_presence_both.txt',
                                    validateData.CNAValidator)
        # expecting only status messages about the file being validated
        self.assertEqual(len(record_list), 3)
        for record in record_list:
            self.assertLessEqual(record.levelno, logging.INFO)

    def test_repeated_gene(self):
        """Test if a warning is issued and the line is skipped if duplicate."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_cna_duplicate_gene.txt',
                                    validateData.CNAValidator)
        # expecting a warning about the duplicate gene,
        # but no errors about values
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertEqual(record.line_number, 6)
        self.assertTrue(record.cause.startswith('116983'))

    def test_invalid_discrete_cna(self):
        """Check a discrete CNA file with values that should yield errors."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_cna_invalid_values.txt',
                                    validateData.CNAValidator)
        # expecting various errors about data values, about one per line
        self.assertEqual(len(record_list), 5)
        for record in record_list:
            self.assertEqual(record.levelno, logging.ERROR)
        record_iterator = iter(record_list)
        record = record_iterator.next()
        self.assertEqual(record.line_number, 3)
        self.assertEqual(record.column_number, 7)
        # self.assertEqual(record.cause, ' ') if blank cells had a 'cause'
        record = record_iterator.next()
        self.assertEqual(record.line_number, 5)
        self.assertEqual(record.column_number, 4)
        # self.assertEqual(record.cause, '') if blank cells had a 'cause'
        record = record_iterator.next()
        self.assertEqual(record.line_number, 6)
        self.assertEqual(record.column_number, 3)
        self.assertEqual(record.cause, '3')
        record = record_iterator.next()
        self.assertEqual(record.line_number, 7)
        self.assertEqual(record.column_number, 6)
        self.assertEqual(record.cause, 'AURKAIP1')
        # Only "NA" is supported, anything else should be an error:
        record = record_iterator.next()
        self.assertEqual(record.line_number, 8)
        self.assertEqual(record.column_number, 5)
        self.assertEqual(record.cause, '[Not Available]')

    def test_valid_rppa(self):
        """Check a valid RPPA file that should yield no errors."""
        self.logger.setLevel(logging.DEBUG)
        record_list = self.validate('data_rppa_valid.txt',
                                    validateData.RPPAValidator)
        # expecting only status messages about the file being validated
        self.assertEqual(len(record_list), 3)
        for record in record_list:
            self.assertLessEqual(record.levelno, logging.INFO)

    def test_invalid_rppa(self):
        """Check an RPPA file with values that should yield errors."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_rppa_invalid_values.txt',
                                    validateData.RPPAValidator)
        # expecting several errors
        self.assertEqual(len(record_list), 3)
        for record in record_list:
            self.assertEqual(record.levelno, logging.ERROR)
        record_iterator = iter(record_list)
        record = record_iterator.next()
        self.assertEqual(record.line_number, 3)
        self.assertEqual(record.column_number, 3)
        self.assertEqual(record.cause, 'spam')
        record = record_iterator.next()
        self.assertEqual(record.line_number, 6)
        self.assertEqual(record.column_number, 5)
        self.assertEqual(record.cause, '')
        record = record_iterator.next()
        self.assertEqual(record.line_number, 9)
        self.assertEqual(record.column_number, 3)
        self.assertEqual(record.cause, ' ')

    def test_repeated_rppa_entry(self):
        """Test if a warning is issued and the line is skipped if duplicate."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_rppa_duplicate_entries.txt',
                                    validateData.RPPAValidator)
        # expecting only a warning
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertEqual(record.line_number, 14)
        self.assertTrue(record.cause.startswith('B-Raf'))

    def test_na_gene_in_rppa(self):
        """Test if a warning is issued if the gene symbol NA occurs in RPPA."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_rppa_na_gene.txt',
                                    validateData.RPPAValidator)
        # expecting only a warning for each NA line
        self.assertEqual(len(record_list), 9)
        for record in record_list:
            self.assertEqual(record.levelno, logging.WARNING)
        for record, expected_line in zip(record_list, range(14, 23)):
            self.assertEqual(record.line_number, expected_line)
            self.assertEqual(record.column_number, 1)
            self.assertIn('NA', record.getMessage())
 
    def test_gsva_range_gsva_scores(self):
        """Test if an error is issued if the score is outside GSVA scoring range"""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_gsva_scores_outrange.txt',
                                    validateData.GsvaScoreValidator)
        # expecting an error for each line that contains value not within -1 and 1
        self.assertEqual(len(record_list), 3)
        for record in record_list:
            self.assertEqual(record.levelno, logging.ERROR)
        record_iterator = iter(record_list)
        record = record_iterator.next()
        self.assertEqual(record.line_number, 5)
        self.assertEqual(record.column_number, 2)
        self.assertEqual(record.cause, '2.371393691351566')
        record = record_iterator.next()
        self.assertEqual(record.line_number, 8)
        self.assertEqual(record.column_number, 3)
        self.assertEqual(record.cause, '-12')
        record = record_iterator.next()
        self.assertEqual(record.line_number, 9)
        self.assertEqual(record.column_number, 2)
        self.assertEqual(record.cause, '1.5')
        return
         
    def test_range_gsva_pvalues(self):
        """Test if an error is issued if the score is outside pvalue range"""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_gsva_pvalues_outrange.txt',
                                    validateData.GsvaPvalueValidator)
        # expecting an error for each line that contains value not within 0 and 1
        self.assertEqual(len(record_list), 3)
        for record in record_list:
            self.assertEqual(record.levelno, logging.ERROR)
        record_iterator = iter(record_list)
        record = record_iterator.next()
        self.assertEqual(record.line_number, 4)
        self.assertEqual(record.column_number, 2)
        self.assertEqual(record.cause, '1.5')
        record = record_iterator.next()
        self.assertEqual(record.line_number, 6)
        self.assertEqual(record.column_number, 4)
        self.assertEqual(record.cause, '1e3')
        record = record_iterator.next()
        self.assertEqual(record.line_number, 10)
        self.assertEqual(record.column_number, 3)
        self.assertEqual(record.cause, '-0.00000000000000005')
        return

    def test_missing_column_gsva(self):
        #Test if an error is issued if the score and pvalue tables do not have same header
        self.logger.setLevel(logging.ERROR)
        
        ### Error should appear when the second file is validated
        record_list1 = self.validate('data_gsva_pvalues_missing_column.txt',
                                    validateData.GsvaPvalueValidator)
        
        record_list2 = self.validate('data_gsva_scores_missing_column.txt',
                                    validateData.GsvaScoreValidator)
        self.assertEqual(len(record_list1), 0)
        self.assertEqual(len(record_list2), 2)
        for record in record_list2:
            self.assertEqual(record.levelno, logging.ERROR)
        record_iterator = iter(record_list2)
        record = record_iterator.next()
        self.assertEqual(record.line_number, 1)
        self.assertIn('headers', record.getMessage().lower())
        self.assertIn('different', record.getMessage().lower())
        record = record_iterator.next()
        self.assertIn('invalid', record.getMessage().lower())
        self.assertIn('column', record.getMessage().lower())
        return


    def test_missing_row_gsva(self):
        #Test if an error is issued if the score and pvalue table does not have same rownames
        self.logger.setLevel(logging.ERROR)
        
        ### Error should appear when the second file is validated
        record_list1 = self.validate('data_gsva_pvalues_missing_row.txt',
                                    validateData.GsvaPvalueValidator)

        record_list2 = self.validate('data_gsva_scores_missing_row.txt',
                                    validateData.GsvaScoreValidator)
        self.assertEqual(len(record_list1), 0)
        self.assertEqual(len(record_list2), 1)
        for record in record_list2:
            self.assertEqual(record.levelno, logging.ERROR)        
        self.assertIn('first column', record.getMessage().lower())
        self.assertIn('not equal', record.getMessage().lower())
        return
#  
#          

    # TODO: test other subclasses of FeatureWiseValidator

class ContinuousValuesTestCase(PostClinicalDataFileTestCase):

    """Verify that values are being checked in feature/sample matrix files with float values."""

    def test_invalid_methylation(self):
        """Check an invalid methylation file that should yield errors."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_methylation_invalid_values.txt',
                                    validateData.ContinuousValuesValidator)
        # expecting 3 errors
        self.assertEqual(len(record_list), 3)
        for record in record_list:
            self.assertEqual(record.levelno, logging.ERROR)
        record_iterator = iter(record_list)
        record = record_iterator.next()
        self.assertEqual(record.cause, 'n.a.')
        record = record_iterator.next()
        self.assertEqual(record.cause, '')
        record = record_iterator.next()
        self.assertEqual(record.cause, 'Na')
        

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
        record_list = self.validate('mutations/data_mutations_invalid_norm_samples.maf',
                                    validateData.MutationsExtendedValidator,
                                    {'normal_samples_list':
                                        'TCGA-BH-A18H-10,'
                                        'TCGA-B6-A0RS-10,'
                                        ''   # TCGA-BH-A0HP-10
                                        'TCGA-BH-A18P-11, '
                                        'TCGA-C8-A138-10'
                                        'TCGA-A2-A0EY-10,'
                                        '',  # TCGA-A8-A08G-10
                                     'swissprot_identifier': 'accession'})
        # we expect 2 errors about invalid normal samples
        self.assertEqual(len(record_list), 2)
        # check if both messages come from printDataInvalidStatement:
        found_one_of_the_expected = False
        for error in record_list:
            self.assertEqual("ERROR", error.levelname)
            self.assertEqual("printDataInvalidStatement", error.funcName)
            if error.cause == 'TCGA-BH-A0HP-10':
                found_one_of_the_expected = True
        self.assertTrue(found_one_of_the_expected)

    def test_missing_aa_change_column(self):
        """One of Amino_Acid_Change or HGVSp_Short is required, so
        there should be a warning if both Amino_Acid_Change and HGVSp_Short are missing"""
        # set level according to this test case:
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('mutations/data_mutations_missing_aa_change_column.maf',
                                    validateData.MutationsExtendedValidator)
        # we expect 2 errors, something like:
        # ERROR: data_mutations_missing_aa_change_column.maf: line 1: At least one of the columns HGVSp_Short or Amino_Acid_Change needs to be present.
        # ERROR: data_mutations_missing_aa_change_column.maf: Invalid column header, file cannot be parsed
        self.assertEqual(len(record_list), 2)
        # check if both messages come from printDataInvalidStatement:
        self.assertIn("hgvsp_short", record_list[0].getMessage().lower())
        self.assertIn("invalid column header", record_list[1].getMessage().lower())

    def test_warning_for_missing_SWISSPROT(self):
        """If SWISSPROT is missing (or present and empty), user should be warned about it"""
        # set level according to this test case:
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('mutations/data_mutations_missing_swissprot.maf',
                                    validateData.MutationsExtendedValidator)
        # we expect 1 warning, something like
        # WARNING: data_mutations_missing_swissprot.maf: line 1: SWISSPROT column is recommended if you want to make sure that a specific isoform is used for the PFAM domains drawing in the mutations view.; wrong value: 'SWISSPROT column not found'
        self.assertEqual(len(record_list), 1)
        # check if both messages come from printDataInvalidStatement:
        self.assertIn("swissprot column is recommended",
                      record_list[0].getMessage().lower())

    def test_unknown_or_invalid_swissprot(self):
        """Test errors for invalid and unknown accessions under SWISSPROT."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate(
                'mutations/data_mutations_invalid_swissprot.maf',
                validateData.MutationsExtendedValidator,
                extra_meta_fields={
                    'swissprot_identifier': 'accession'})
        self.assertEqual(len(record_list), 2)
        record_iterator = iter(record_list)
        # used a name instead of an accession
        record = record_iterator.next()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 3)
        self.assertEqual(record.cause, 'A1CF_HUMAN')
        self.assertNotIn('portal', record.getMessage().lower())
        # neither a name nor an accession
        record = record_iterator.next()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 5)
        self.assertEqual(record.cause, 'P99999,Z9ZZZ9ZZZ9')
        self.assertNotIn('portal', record.getMessage().lower())

    def test_name_as_swissprot_identifier(self):
        """Test if the SWISSPROT column is parsed as a name if meta says so."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate(
                'mutations/data_mutations_name_swissprot.maf',
                validateData.MutationsExtendedValidator,
                extra_meta_fields={'swissprot_identifier': 'name'})
        # the same errors as in test_implicit_name_as_swissprot_identifier()
        self.assert_swissprotname_validated(record_list)

    def test_implicit_name_as_swissprot_identifier(self):
        """Test if the SWISSPROT column is parsed as a name if unspecified."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate(
                'mutations/data_mutations_name_swissprot.maf',
                validateData.MutationsExtendedValidator)
        # warning about the implicit Swiss-Prot identifier type
        record = record_list[0]
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertIn('swissprot_identifier', record.getMessage())
        # the same errors as in test_name_as_swissprot_identifier()
        self.assert_swissprotname_validated(record_list[1:])

    def assert_swissprotname_validated(self, record_list):
        """Assert names are validated in data_mutations_name_swissprot.maf."""
        self.assertEqual(len(record_list), 2)
        record_iterator = iter(record_list)
        # used an accession instead of a name
        record = record_iterator.next()
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertEqual(record.line_number, 3)
        self.assertEqual(record.cause, 'Q9NQ94')
        self.assertNotIn('portal', record.getMessage().lower())
        # neither a name nor an accession
        record = record_iterator.next()
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertEqual(record.line_number, 5)
        self.assertEqual(record.cause, 'A1CF_HUMAN,HBB_YEAST')
        self.assertNotIn('portal', record.getMessage().lower())

    def test_invalid_swissprot_identifier_type(self):
        """Test if the validator rejects files with nonsensical id types."""
        self.logger.setLevel(logging.ERROR)
        mvals, mtype = validateData.cbioportal_common.parse_metadata_file(
                'test_data/mutations/meta_mutations_invalid_swissprot_idspec.txt',
                self.logger,
                study_id='spam')
        record_list = self.get_log_records()
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.cause, 'namelessly')
        self.assertIsNone(mtype, 'metadata file was not rejected as invalid')

    def test_isValidAminoAcidChange(self):
        """Test if proper warnings are given for wrong/blank AA change vals."""
        # set level according to this test case:
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate(
                'mutations/data_mutations_wrong_aa_change.maf',
                validateData.MutationsExtendedValidator,
                extra_meta_fields={'swissprot_identifier': 'accession'})
        self.assertEqual(len(record_list), 5)
        record_iterator = iter(record_list)
        # empty field (and no HGVSp_Short column)
        record = record_iterator.next()
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertIn('Amino_Acid_Change', record.getMessage())
        self.assertIn('HGVSp_Short', record.getMessage())
        self.assertEqual(record.line_number, 2)
        # multiple specifications
        record = record_iterator.next()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('p.', record.getMessage())
        self.assertEqual(record.cause, 'p.A195V p.I167I')
        # comma in the string
        record = record_iterator.next()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('comma', record.getMessage().lower())
        self.assertEqual(record.cause, 'p.N851,Y1055delinsCC')
        # haplotype specification of multiple mutations
        record = record_iterator.next()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('allele', record.getMessage().lower())
        self.assertEqual(record.cause, 'p.[N851N];[Y1055C]')
        # NULL (and no HGVSp_Short column)
        record = record_iterator.next()
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertIn('Amino_Acid_Change', record.getMessage())
        self.assertIn('HGVSp_Short', record.getMessage())
        self.assertEqual(record.line_number, 8)

    def test_silent_mutation_skipped(self):
        """Test if silent mutations are skipped with a message.

        Silent mutations being ones that have no direct effect on amino acid
        sequence, which is predicted in the Variant_Classification column.
        """
        # set level according to this test case:
        self.logger.setLevel(logging.INFO)
        record_list = self.validate('mutations/data_mutations_some_silent.maf',
                                    validateData.MutationsExtendedValidator,
                                    extra_meta_fields={
                                            'swissprot_identifier': 'name'})
        # we expect 5 infos: 3 about silent mutations, 2 general info messages:
        self.assertEqual(len(record_list), 5)
        # First 3 INFO messages should be something like: "Validation of line skipped due to cBioPortal's filtering. Filtered types:"
        for record in record_list[:3]: 
            self.assertIn("filtered types", record.getMessage().lower())
        

    def test_alternative_notation_for_intergenic_mutation(self):
        """Test alternative 'notation' for intergenic mutations.

        The MAF specification documents the use of the 'gene' Unknown / 0 for
        intergenic mutations, and since the Variant_Classification column is
        often invalid, cBioPortal assumes it to mean that and skips it.
        (even if the Entrez column is absent).
        Here we test whether the 'gene' Unknown / 0 records are skipped 
        with a warning when Variant_Classification!='IGR'
        """
        # set level according to this test case:
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('mutations/data_mutations_silent_alternative.maf',
                                    validateData.MutationsExtendedValidator,
                                    extra_meta_fields={
                                            'swissprot_identifier': 'name'})
        # we expect 1 ERROR and 2 WARNINGs :
        self.assertEqual(len(record_list), 3)
        
        # ERROR should be something like: "No Entrez id or gene symbol provided for gene"
        self.assertIn("no entrez gene id or gene symbol provided", record_list[0].getMessage().lower())
        self.assertEqual(record_list[0].levelno, logging.ERROR)
        # WARNING should be something like: "Gene specification for this mutation implies intergenic..."
        self.assertIn("implies intergenic", record_list[1].getMessage().lower())
        self.assertEqual(record_list[1].levelno, logging.WARNING)
        self.assertIn("implies intergenic", record_list[2].getMessage().lower())
        self.assertEqual(record_list[2].levelno, logging.WARNING)


class SegFileValidationTestCase(PostClinicalDataFileTestCase):

    """Tests for the various validations of data in segment CNA data files."""

    @classmethod
    def setUpClass(cls):
        """Override a static method to skip a UCSC HTTP query in each test."""
        super(SegFileValidationTestCase, cls).setUpClass()
        @staticmethod
        def load_chromosome_lengths(genome_build, _):
            if genome_build != 'hg19':
                raise ValueError(
                        "load_chromosome_lengths() called with genome build '{}'".format(
                            genome_build))
            return {u'1': 249250621, u'10': 135534747, u'11': 135006516,
                    u'12': 133851895, u'13': 115169878, u'14': 107349540,
                    u'15': 102531392, u'16': 90354753, u'17': 81195210,
                    u'18': 78077248, u'19': 59128983, u'2': 243199373,
                    u'20': 63025520, u'21': 48129895, u'22': 51304566,
                    u'3': 198022430, u'4': 191154276, u'5': 180915260,
                    u'6': 171115067, u'7': 159138663, u'8': 146364022,
                    u'9': 141213431, u'X': 155270560, u'Y': 59373566}
        cls.orig_chromlength_method = validateData.SegValidator.load_chromosome_lengths
        validateData.SegValidator.load_chromosome_lengths = load_chromosome_lengths

    @classmethod
    def tearDownClass(cls):
        """Restore the environment to before setUpClass() was called."""
        validateData.SegValidator.load_chromosome_lengths = cls.orig_chromlength_method
        super(SegFileValidationTestCase, cls).tearDownClass()

    def test_valid_seg(self):
        """Validate a segment file without file format errors."""
        record_list = self.validate('data_seg_valid.seg',
                                    validateData.SegValidator,
                                    extra_meta_fields={'reference_genome_id':
                                                           'hg19'})
        # expecting only status messages about the file being validated
        self.assertEqual(len(record_list), 3)
        for record in record_list:
            self.assertLessEqual(record.levelno, logging.INFO)

    def test_unparsable_seg_columns(self):
        """Validate .seg files with non-numeric values and an unsupported chromosome."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_seg_nonsense_values.seg',
                                    validateData.SegValidator,
                                    extra_meta_fields={'reference_genome_id':
                                                           'hg19'})
        self.assertEqual(len(record_list), 3)
        for record in record_list:
            self.assertEqual(record.levelno, logging.ERROR)
            self.assertEqual(record.line_number, 28)
        # unknown chromosome
        self.assertEqual(record_list[0].column_number, 2)
        # non-integral start position
        self.assertEqual(record_list[1].column_number, 3)
        # non-rational mean copy number
        self.assertEqual(record_list[2].column_number, 6)

    def test_negative_length_segment(self):
        """Validate a .seg where a start position is no higher than its end."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_seg_end_before_start.seg',
                                    validateData.SegValidator,
                                    extra_meta_fields={'reference_genome_id':
                                                           'hg19'})
        self.assertEqual(len(record_list), 2)
        record_iterator = iter(record_list)
        # negative-length segment
        record = record_iterator.next()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 11)
        # zero-length segment
        record = record_iterator.next()
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertEqual(record.line_number, 31)

    def test_out_of_bounds_coordinates(self):
        """Validate .seg files with regions spanning outside of the chromosome."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_seg_out_of_bounds.seg',
                                    validateData.SegValidator,
                                    extra_meta_fields={'reference_genome_id':
                                                           'hg19'})
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.ERROR)
        # start position before start of chromosome
        self.assertEqual(record_list[0].line_number, 36)
        self.assertEqual(record_list[0].column_number, 3)
        # end position after end of chromosome
        self.assertEqual(record_list[1].line_number, 41)
        self.assertEqual(record_list[1].column_number, 4)

    def test_blank_line(self):
        """Validate a .seg with a blank data line."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_seg_blank_line.seg',
                                    validateData.SegValidator,
                                    extra_meta_fields={'reference_genome_id':
                                                           'hg19'})
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 35)
        self.assertIn('blank', record.getMessage().lower())


class GisticGenesValidationTestCase(PostClinicalDataFileTestCase):

    """Tests for validations of data in aggregated GISTIC genes files.

    See validateData.GisticGenesValidator for more information.
    """

    def test_valid_amp_file(self):
        """Test validation of an amp file that should yield no warnings."""
        self.logger.setLevel(logging.DEBUG)
        record_list = self.validate(
                'data_gisticgenes_amp_valid.txt',
                validateData.GisticGenesValidator,
                extra_meta_fields={
                    'genetic_alteration_type': 'GISTIC_GENES_AMP',
                    'reference_genome_id': 'hg19'})
        # expecting only status messages about the file being validated
        self.assertEqual(len(record_list), 3)
        for record in record_list:
            self.assertLessEqual(record.levelno, logging.INFO)

    def test_valid_del_file(self):
        """Test validation of a del file that should yield no warnings."""
        self.logger.setLevel(logging.DEBUG)
        record_list = self.validate(
                'data_gisticgenes_del_valid.txt',
                validateData.GisticGenesValidator,
                extra_meta_fields={
                    'genetic_alteration_type': 'GISTIC_GENES_DEL',
                    'reference_genome_id': 'hg19'})
        # expecting only status messages about the file being validated
        self.assertEqual(len(record_list), 3)
        for record in record_list:
            self.assertLessEqual(record.levelno, logging.INFO)

    def test_region_without_genes(self):
        """Test validation of regions with no genes."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate(
                'data_gisticgenes_del_region_without_genes.txt',
                validateData.GisticGenesValidator,
                extra_meta_fields={
                    'genetic_alteration_type': 'GISTIC_GENES_DEL',
                    'reference_genome_id': 'hg19'})
        # expecting warnings about the empty gene lists on two lines
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.WARNING)
            self.assertEqual(record.column_number, 5)
        self.assertEqual(record_list[0].line_number, 4)
        self.assertEqual(record_list[1].line_number, 5)

    def test_zero_length_peak(self):
        """Test validation of a zero-bases-short peak."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate(
                'data_gisticgenes_del_zero_length_peak.txt',
                validateData.GisticGenesValidator,
                extra_meta_fields={
                    'genetic_alteration_type': 'GISTIC_GENES_DEL',
                    'reference_genome_id': 'hg19'})
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertEqual(record.line_number, 5)
        self.assertIn('242476062', record.cause)

    def test_format_errors(self):
        """Test validation of a file with genome-unspecific errors."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate(
                'data_gisticgenes_del_format_errors.txt',
                validateData.GisticGenesValidator,
                extra_meta_fields={
                    'genetic_alteration_type': 'GISTIC_GENES_DEL',
                    'reference_genome_id': 'hg19'})
        # expecting various errors, about two per line
        self.assertEqual(len(record_list), 9)
        for record in record_list[:8]:
            self.assertEqual(record.levelno, logging.ERROR)
        record_iterator = iter(record_list)
        # invalid 'amp' value
        record = record_iterator.next()
        self.assertEqual(record.line_number, 2)
        self.assertEqual(record.column_number, 6)
        # mismatch between chromosome number in chromosome and cytoband cols
        record = record_iterator.next()
        self.assertEqual(record.line_number, 2)
        self.assertEqual(record.cause,'(1p36.13, 2)')
        # q-value not a real number
        record = record_iterator.next()
        self.assertEqual(record.line_number, 3)
        self.assertEqual(record.column_number, 8)
        # reversed start and end positions
        record = record_iterator.next()
        self.assertEqual(record.line_number, 3)
        self.assertIn('not lower', record.getMessage())
        # incorrect 'amp' value
        record = record_iterator.next()
        self.assertEqual(record.line_number, 4)
        self.assertEqual(record.column_number, 6)
        # no p or q in cytoband
        record = record_iterator.next()
        self.assertEqual(record.line_number, 4)
        self.assertEqual(record.column_number, 7)
        # missing chromosome
        record = record_iterator.next()
        self.assertEqual(record.line_number, 5)
        self.assertEqual(record.column_number, 2)
        # missing chromosome in cytoband
        record = record_iterator.next()
        self.assertEqual(record.line_number, 5)
        self.assertEqual(record.column_number, 7)
        # blank gene in list
        record = record_iterator.next()
        self.assertEqual(record.line_number, 6)
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertEqual(record.cause, '')


class StudyCompositionTestCase(LogBufferTestCase):

    """Tests for validations of the number of files of certain types."""

    def setUp(self):
        """Store validateData globals changed by running validate_study()."""
        super(StudyCompositionTestCase, self).setUp()
        self.orig_defined_cancer_types = validateData.DEFINED_CANCER_TYPES
        self.orig_defined_sample_ids = validateData.DEFINED_SAMPLE_IDS

    def tearDown(self):
        """Restore the environment to before setUp() was called."""
        validateData.DEFINED_CANCER_TYPES = self.orig_defined_cancer_types
        validateData.DEFINED_SAMPLE_IDS = self.orig_defined_sample_ids
        super(StudyCompositionTestCase, self).tearDown()

    # TODO test whether proper metadata files generate only INFO and DEBUG messages

    def test_double_cancer_type_file(self):
        """Check behavior when two cancer type files are supplied."""
        self.logger.setLevel(logging.ERROR)
        validateData.validate_study(
            'test_data/study_cancertype_two_files',
            PORTAL_INSTANCE,
            self.logger, False)
        record_list = self.get_log_records()
        # expecting two errors: one about the two cancer type files, and
        # about the cancer type of the study not having been defined
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.ERROR)
        # compare filenames mentioned in the 1st error independent of ordering
        filenames_in_cause_string = set(record_list[0].cause.split(', ', 1))
        self.assertEqual(filenames_in_cause_string,
                         set(['cancer_type_luad.txt', 'cancer_type_lung.txt']))
        # assert that the second error complains about the cancer type
        self.assertEqual(record_list[1].cause, 'luad')


class CaseListDirTestCase(PostClinicalDataFileTestCase):

    """Test validations of the case list directory."""

    def test_duplicated_stable_id(self):
        """Test if an error is issued when two lists have the same id."""
        self.logger.setLevel(logging.ERROR)
        validateData.processCaseListDirectory(
            'test_data/case_lists_duplicated',
            'brca_tcga_pub',
            self.logger)
        record_list = self.get_log_records()
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('multiple', record.getMessage().lower())
        self.assertTrue(record.cause.startswith('brca_tcga_pub_all'),
                        "Error is not about the id 'brca_tcga_pub_all'")

    def test_missing_caselists(self):
        """Test if errors are issued if certain case lists are not defined."""
        self.logger.setLevel(logging.ERROR)
        validateData.validate_study(
                'test_data/study_missing_caselists',
                PORTAL_INSTANCE,
                self.logger,
                False)
        record_list = self.get_log_records()
        self.assertEqual(len(record_list), 1)
        # <study ID>_all
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('spam_all', record.getMessage())
        self.assertIn('add_global_case_list', record.getMessage())


class StableIdValidationTestCase(LogBufferTestCase):

    """Tests to ensure stable_id validation works correctly."""

    def test_unnecessary_and_wrong_stable_id(self):
        """Tests to check behavior when stable_id is not needed (warning) or wrong(error)."""
        self.logger.setLevel(logging.WARNING)
        validateData.process_metadata_files(
            'test_data/study_metastableid',
            PORTAL_INSTANCE,
            self.logger, False)
        record_list = self.get_log_records()
        # expecting 1 warning, 1 error:
        self.assertEqual(len(record_list), 3)
        # get both into a variable to avoid dependency on order:
        errors = []
        for record in record_list:
            if record.levelno == logging.ERROR:
                errors.append(record.cause)
            else:
                warning = record

        # expecting one error about wrong stable_id in meta_expression:
        self.assertEqual(len(errors), 2)
        self.assertIn('mrna_test', errors)
        self.assertIn('gistic', errors)

        # expecting one warning about stable_id not being recognized in _samples
        self.assertEqual(warning.levelno, logging.WARNING)
        self.assertEqual(warning.cause, 'stable_id')


class HeaderlessClinicalDataValidationTest(PostClinicalDataFileTestCase):

    """Tests for validation of clinical data files without metadata headers.

    When the script is run in relaxed mode, files with incorrect
    attribute metadata headers should be validated until the end
    rather than considered unparseable from the header on.
    """

    def test_headerless_clinical_sample(self):
        """Test relaxed validation of sample attr files without metadata."""
        self.logger.setLevel(logging.INFO)
        record_list = self.validate('data_clinical_sam_no_hdr.txt',
                                    validateData.SampleClinicalValidator, None, True)
        # we expect a list of records ending in an info message about all lines
        # being parsed -- if the file had been declared unparseable before the
        # header it would have issued an error instead.
        final_record = record_list[-1]
        self.assertEqual(final_record.levelno, logging.INFO)
        self.assertTrue(final_record.getMessage().lower().startswith(
            'read 13 lines'))

    def test_nonrelaxed_headerless_clinical_sample(self):
        """Test regular validation of sample attr files without metadata."""
        self.logger.setLevel(logging.INFO)
        record_list = self.validate('data_clinical_sam_no_hdr.txt',
                                    validateData.SampleClinicalValidator, None, False)
        # test if the list of records logged ends in an error about the file
        # being unparseable, rather than an info about it being read to the end
        final_record = record_list[-1]
        self.assertEqual(final_record.levelno, logging.ERROR)
        self.assertIn('cannot be parsed', final_record.getMessage().lower())

    def test_headerless_clinical_patient(self):
        """Test relaxed validation of patient attr files without metadata."""
        self.logger.setLevel(logging.INFO)
        record_list = self.validate('data_clinical_pat_no_hdr.txt',
                                    validateData.PatientClinicalValidator, None, True)
        # we expect a list of records ending in an info message about all lines
        # being parsed -- if the file had been declared unparseable before the
        # header it would have issued an error instead.
        final_record = record_list[-1]
        self.assertEqual(final_record.levelno, logging.INFO)
        self.assertTrue(final_record.getMessage().lower().startswith(
            'read 13 lines'))

    def test_nonrelaxed_headerless_clinical_patient(self):
        """Test regular validation of patient attr files without metadata."""
        self.logger.setLevel(logging.INFO)
        record_list = self.validate('data_clinical_pat_no_hdr.txt',
                                    validateData.PatientClinicalValidator, None, False)
        # test if the list of records logged ends in an error about the file
        # being unparseable, rather than an info about it being read to the end
        final_record = record_list[-1]
        self.assertEqual(final_record.levelno, logging.ERROR)
        self.assertIn('cannot be parsed', final_record.getMessage().lower())


class DataFileIOTestCase(PostClinicalDataFileTestCase):
    """Test if the right behavior occurs if study files cannot be read."""

    def test_missing_datafile(self):
        """Test the error if files referenced from meta files do not exist."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('filename-that-does-not-exist.txt',
                                    validateData.ContinuousValuesValidator)
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('file', record.getMessage().lower())


if __name__ == '__main__':
    unittest.main(buffer=True)
