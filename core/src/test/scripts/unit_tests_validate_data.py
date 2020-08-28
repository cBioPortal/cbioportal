#!/usr/bin/env python3

"""
Copyright (c) 2016 The Hyve B.V.
This code is licensed under the GNU Affero General Public License (AGPL),
version 3, or (at your option) any later version.
"""

import unittest
from unittest.mock import Mock
import sys
import logging.handlers
import textwrap
from pathlib import Path
from contextlib import contextmanager, suppress
from tempfile import TemporaryDirectory

from importer import cbioportal_common
from importer import validateData


# globals for mock data used throughout the module
DEFINED_SAMPLE_IDS = None
DEFINED_SAMPLE_ATTRIBUTES = None
PATIENTS_WITH_SAMPLES = None
PORTAL_INSTANCE = None
prior_validated_sample_ids = None
prior_validated_geneset_ids = None
mutation_sample_ids = None

def setUpModule():
    """Initialise mock data used throughout the module."""
    global DEFINED_SAMPLE_IDS
    global DEFINED_SAMPLE_ATTRIBUTES
    global PATIENTS_WITH_SAMPLES
    global PORTAL_INSTANCE
    global mutation_sample_ids
    # mock information parsed from the sample attribute file
    DEFINED_SAMPLE_IDS = ["TCGA-A1-A0SB-01", "TCGA-A1-A0SD-01", "TCGA-A1-A0SE-01", "TCGA-A1-A0SH-01", "TCGA-A2-A04U-01", "TCGA-B6-A0RS-01", "TCGA-BH-A0HP-01", "TCGA-BH-A18P-01", "TCGA-BH-A18H-01", "TCGA-C8-A138-01", "TCGA-A2-A0EY-01", "TCGA-A8-A08G-01"]
    DEFINED_SAMPLE_ATTRIBUTES = {'PATIENT_ID', 'SAMPLE_ID', 'SUBTYPE', 'CANCER_TYPE', 'CANCER_TYPE_DETAILED'}
    PATIENTS_WITH_SAMPLES = set("TEST-PAT{}".format(num) for
                                num in list(range(1, 10)) if
                                num != 8)
    mutation_sample_ids = ["TCGA-A1-A0SB-01", "TCGA-A1-A0SD-01"]
    logger = logging.getLogger(__name__)
    # parse mock API results from a local directory
    PORTAL_INSTANCE = validateData.load_portal_info('test_data/api_json_unit_tests',
                                                    logger,
                                                    offline=True)


@contextmanager
def temp_inputfolder(file_dict):
    """Context manager that creates the specified files in a temporary folder.

    The keys of the dictionary (pathlikes or strings) are used as the file
    names, and the values as the corresponding file contents.
    """
    with TemporaryDirectory() as study_dir_name:
        study_dir = Path(study_dir_name)
        for filename, contents in file_dict.items():
            # create the directory to make the file in, if it's not there yet
            file_directory = (study_dir / filename).parent
            with suppress(FileExistsError):
                file_directory.mkdir(parents=True)
            # write the contents to the file
            with (study_dir / filename).open('w', encoding='utf-8') as f:
                f.write(contents)
        yield str(study_dir)


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

    def validate(self, data_filename, validator_class,
                 extra_meta_fields=None,
                 relaxed_mode=False, strict_maf_checks=False,
                 *, portal_instance=None):
        """Validate a file with a Validator and return the log records."""
        if portal_instance is None:
            portal_instance = PORTAL_INSTANCE
        meta_dict = {'data_filename': data_filename}
        if extra_meta_fields is not None:
            meta_dict.update(extra_meta_fields)
        validator = validator_class('test_data', meta_dict,
                                    portal_instance,
                                    self.logger, relaxed_mode,
                                    strict_maf_checks)
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

        # Prepare global variables related to gene sets
        self.orig_prior_validated_sample_ids = validateData.prior_validated_sample_ids
        validateData.prior_validated_sample_ids = prior_validated_sample_ids
        self.orig_prior_validated_geneset_ids = validateData.prior_validated_geneset_ids
        validateData.prior_validated_geneset_ids = prior_validated_geneset_ids

        # Prepare global variables related to sample profiled for mutations and gene panels
        self.mutation_sample_ids = validateData.mutation_sample_ids
        validateData.mutation_sample_ids = mutation_sample_ids


    def tearDown(self):
        """Restore the environment to before setUp() was called."""
        validateData.DEFINED_SAMPLE_IDS = self.orig_defined_sample_ids
        validateData.DEFINED_SAMPLE_ATTRIBUTES = self.orig_defined_sample_attributes
        validateData.PATIENTS_WITH_SAMPLES = self.orig_patients_with_samples
        validateData.prior_validated_sample_ids = self.orig_prior_validated_sample_ids
        validateData.prior_validated_geneset_ids = self.orig_prior_validated_geneset_ids
        validateData.mutation_sample_ids = self.mutation_sample_ids
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

class UniqueColumnTestCase(PostClinicalDataFileTestCase):

    # create dummy class from  FeaturewiseFileValidator
    # set the `name` column to be validated for uniqueness
    class DummyFeaturewiseFileValidator(validateData.FeaturewiseFileValidator):
        REQUIRED_HEADERS = ['id']
        OPTIONAL_HEADERS = ['name']
        def parseFeatureColumns(self, nonsample_col_vals):
            return
        def checkValue(self, value, column_index):
            return 

    def test_uniquecolumns_are_accepted(self):
        self.logger.setLevel(logging.ERROR)
        UniqueColumnTestCase.DummyFeaturewiseFileValidator.UNIQUE_COLUMNS = ['id']
        record_list = self.validate('data_unique_column_test.txt', UniqueColumnTestCase.DummyFeaturewiseFileValidator)
        self.assertEqual(len(record_list), 0)

    def test_uniquecolumns_(self):
        self.logger.setLevel(logging.ERROR)
        UniqueColumnTestCase.DummyFeaturewiseFileValidator.UNIQUE_COLUMNS = ['name']
        record_list = self.validate('data_unique_column_test.txt', UniqueColumnTestCase.DummyFeaturewiseFileValidator)
        self.assertEqual(len(record_list), 1)

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

    def test_lowercase_attribute(self):
        """Test when attribute is in lower case."""
        record_list = self.validate('data_clin_coldefs_lowercase_attribute.txt',
                                    validateData.PatientClinicalValidator)
        # expecting an debug message followed by the error, and rest 2 info messages
        self.assertEqual(len(record_list), 4)
        # error about the lowercase value of sausage column
        self.assertEqual(record_list[1].levelno, logging.ERROR)
        self.assertEqual(record_list[1].line_number, 5)
        self.assertEqual(record_list[1].column_number, 6)
        self.assertIn('Attribute name not in upper case', record_list[1].getMessage())

    def test_hardcoded_attributes(self):

        """Test requirements on the data type or level of some attributes."""

        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_clin_coldefs_hardcoded_attrs.txt',
                                    validateData.PatientClinicalValidator)
        self.assertEqual(len(record_list), 3)
        record_iterator = iter(record_list)

        # Expect error for OS_MONTHS being a STRING instead of NUMBER
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 3)
        self.assertEqual(record.column_number, 2)
        self.assertIn(record.cause, 'STRING')

        # Expect error for sample attribute in patient clinical data
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 5)
        self.assertEqual(record.column_number, 6)
        self.assertIn(record.cause, 'OTHER_SAMPLE_ID')

        # Expect warning for sample attribute in patient clinical data
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 5)
        self.assertEqual(record.column_number, 7)
        self.assertIn(record.cause, 'METASTATIC_SITE')

    def test_banned_attribute(self):
        """Test when attribute is is not allowed."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_clin_coldefs_banned_attribute.txt',
                                    validateData.PatientClinicalValidator)
        # expecting 1 error message
        self.assertEqual(len(record_list), 1)
        record_iterator = iter(record_list)
        record = next(record_iterator)

        # error about the banned value of mutation count column
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 5)
        self.assertEqual(record.column_number, 6)
        self.assertIn('MUTATION_COUNT and FRACTION_GENOME_ALTERED are calculated in cBioPortal',
                      record.getMessage())

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

    def test_sample_with_invalid_characters_in_sample_id(self):
        """Test when a invalid characters are found in SAMPLE_ID."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_clin_wrong_ids.txt',
                                    validateData.SampleClinicalValidator)
        self.assertEqual(len(record_list), 6)
        record_iterator = iter(record_list)
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 6)
        self.assertIn('can only contain letters, numbers, points, underscores and/or hyphens', record.getMessage())
        # last one:
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 12)
        self.assertIn('can only contain letters, numbers, points, underscores and/or hyphens', record.getMessage())



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
        self.assertEqual(len(record_list), 6)
        record_iterator = iter(record_list)
        # OS_STATUS not 0:LIVING
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 6)
        self.assertEqual(record.column_number, 3)
        self.assertEqual(record.cause, '0:ALIVE')
        # DFS_STATUS having an OS_STATUS value
        record = next(record_iterator)	
        self.assertEqual(record.levelno, logging.ERROR)	
        self.assertEqual(record.line_number, 7)	
        self.assertEqual(record.column_number, 5)	
        self.assertEqual(record.cause, '0:LIVING')
        # PFS_STATUS not start with 0 / 1
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 8)
        self.assertEqual(record.column_number, 7)
        self.assertEqual(record.cause, 'PROGRESSION')
        # wrong casing for OS_STATUS	
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 9)
        self.assertEqual(record.column_number, 3)
        self.assertEqual(record.cause, '0:living')
        # DFS_STATUS not 1:Recurred/Progressed
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 11)
        self.assertEqual(record.column_number, 5)
        self.assertEqual(record.cause, '1:recurred/progressed')
        # unspecified OS_MONTHS while OS_STATUS is 1:DECEASED
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertEqual(record.line_number, 13)
        self.assertIn('OS_MONTHS is not specified for deceased patient. Patient '
                      'will be excluded from survival curve and month of death '
                      'will not be shown on patient view timeline.',
                      record.getMessage())
        
    def test_patient_with_invalid_characters_in_patient_id(self):
        """Test when a invalid characters are found in PATIENT_ID."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_clin_wrong_patient_id.txt',
                                    validateData.PatientClinicalValidator)
        self.assertEqual(len(record_list), 24)
        record_iterator = iter(record_list)
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 6)
        self.assertIn('can only contain letters, numbers, points, underscores and/or hyphens', record.getMessage())

    def test_date_in_nondate_column(self):
        """Test when a sample is defined twice in the same file."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_clin_date_in_nondate_column.txt',
                                    validateData.PatientClinicalValidator)
        self.assertEqual(len(record_list), 2)
        record_iterator = iter(record_list)
        # First record contains 'Jan-14'
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 8)
        self.assertEqual(record.column_number, 6)
        self.assertEqual(record.cause, 'Jan-14')
        self.assertIn('Date found when no date was expected', record.getMessage())
        # Second record contains '4-Oct'
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 9)
        self.assertEqual(record.column_number, 6)
        self.assertEqual(record.cause, '4-Oct')
        self.assertIn('Date found when no date was expected', record.getMessage())


class TimelineValuesDataValidationTest(DataFileTestCase):
    
    """Test values specific to timeline files are appropriately validated."""
    
    def test_start_date_validation_TimelineValidator(self):
        """timeline validator requires START_DATE in timeline values to
           have a non-'NA' value.
        """
        
        # set level according to this test case:
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_timeline_invalid_start_date.txt',
                                     validateData.TimelineValidator)
        self.assertEqual(len(record_list), 2)
        for error in record_list:
            self.assertEqual("ERROR", error.levelname)
            self.assertIn("Invalid START_DATE", error.getMessage())

        
# TODO: make tests in this testcase check the number of properly defined types
class CancerTypeFileValidationTestCase(DataFileTestCase):

    """Tests for validations of cancer type files in a study."""

    def test_new_cancer_type(self):
        """Test when a study defines a new cancer type."""
        # {"id":"luad","name":"Lung Adenocarcinoma","color":"Gainsboro"}
        self.logger.setLevel(logging.INFO)
        record_list = self.validate('data_cancertype_lung.txt',
                                    validateData.CancerTypeValidator)
        # expecting an info message being about a new cancer type being added
        self.assertEqual(len(record_list), 3)
        record = record_list.pop()
        record = record_list.pop()
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.INFO)
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
                                    validateData.CNADiscreteValidator)
        # expecting only status messages about the file being validated
        self.assertEqual(len(record_list), 3)
        for record in record_list:
            self.assertLessEqual(record.levelno, logging.INFO)

    def test_name_only(self):
        """Test when a file has a Hugo name column but none for Entrez IDs."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_cna_genecol_presence_hugo_only.txt',
                                    validateData.CNADiscreteValidator)
        # expecting 1 warning
        self.assertEqual(len(record_list), 1)
        for record in record_list:
            self.assertEqual(record.levelno, logging.WARNING)

    def test_entrez_only(self):
        """Test when a file has an Entrez ID column but none for Hugo names."""
        record_list = self.validate('data_cna_genecol_presence_entrez_only.txt',
                                    validateData.CNADiscreteValidator)
        # expecting only status messages about the file being validated
        self.assertEqual(len(record_list), 3)
        for record in record_list:
            self.assertLessEqual(record.levelno, logging.INFO)

    def test_neither_name_nor_entrez(self):
        """Test when a file lacks both the Entrez ID and Hugo name columns."""
        record_list = self.validate('data_cna_genecol_presence_neither.txt',
                                    validateData.CNADiscreteValidator)
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
                                    validateData.CNADiscreteValidator)
        # expecting two error messages:
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.WARNING)
        # expecting these to be the cause:
        self.assertEqual(record_list[0].cause, 'XXACAP3')
        self.assertEqual(record_list[1].cause, 'XXAGRN')

    def test_both_name_and_entrez_but_hugo_starts_with_integer(self):
        """Test when gene symbol is invalid because starts with integer.

        Test when a file has both the Hugo name and Entrez ID columns, but gene symbol is invalid because it starts with
        an integer. Also '20MER2', '3.8-1.4' and "5'URS" are added to this dataset, which are some of the few exceptions
        that are allowed to start with an integer. This validation step was added to catch unintentional gene conversion
         by Excel, for example SEPT9 -> 9-Sep
        """
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_cna_genecol_presence_both_invalid_hugo_integer.txt',
                                    validateData.CNADiscreteValidator,
                                    extra_meta_fields={'meta_file_type': 'CNA_DISCRETE'})
        # expecting two error messages:
        self.assertEqual(len(record_list), 0)
        for record in record_list:
            self.assertEqual(record.levelno, logging.WARNING)
        # expecting these to be the cause:
        #self.assertEqual(record_list[0].cause, '1-ACAP3')
        #self.assertEqual(record_list[1].cause, '9-SEP')

    def test_both_name_and_entrez_but_invalid_entrez(self):
        """Test when a file has both the Hugo name and Entrez ID columns, but entrez is invalid."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_cna_genecol_presence_both_invalid_entrez.txt',
                                    validateData.CNADiscreteValidator)
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
                                    validateData.CNADiscreteValidator)
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
                                    validateData.CNADiscreteValidator)
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
                                    validateData.CNADiscreteValidator)
        # expecting one error message
        self.assertEqual(len(record_list), 2)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.WARNING)
        # expecting this gene to be the cause
        self.assertEqual(record.cause, 'TRAPPC2P1')

    def test_entrez_only_but_invalid(self):
        """Test when a file has an Entrez ID column but none for Hugo names, and entrez is wrong."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_cna_genecol_presence_entrez_only_invalid.txt',
                                    validateData.CNADiscreteValidator)
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
                                    validateData.CNADiscreteValidator)
        # expecting one error message
        self.assertEqual(len(record_list), 2)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.WARNING)
        # expecting this gene to be the cause
        self.assertEqual(record.cause, 'ACT')

    def test_blank_column_heading(self):
        """Test whether an error is issued if a column has a blank name."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_cna_blank_heading.txt',
                                    validateData.CNADiscreteValidator)
        self.assertEqual(len(record_list), 4)
        for record in record_list:
            self.assertEqual(record.levelno, logging.ERROR)
        record_iterator = iter(record_list)
        record = next(record_iterator)
        self.assertEqual(record.line_number, 1)
        self.assertEqual(record.column_number, 2)
        self.assertEqual(record.cause, '')
        record = next(record_iterator)
        self.assertEqual(record.line_number, 1)
        self.assertEqual(record.column_number, 8)
        self.assertEqual(record.cause, '  ')
        record = next(record_iterator)
        self.assertIn('white space in sample_id', record.getMessage().lower())
        record = next(record_iterator)
        self.assertIn('cannot be parsed', record.getMessage().lower())

    def test_cytoband_column(self):
        """Test that the validator will not fail for a column for Cytoband. This column is default outputted by GISTIC2
         and ignored in the importer."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_cna_cytoband.txt',
                                    validateData.CNADiscreteValidator)
        # expecting zero warning messages:
        self.assertEqual(len(record_list), 0)


    # TODO - add extra unit tests for the genesaliases scenarios (now only test_name_only_but_ambiguous tests part of this)


class FeatureWiseValuesTestCase(PostClinicalDataFileTestCase):

    """Verify that values are being checked in feature/sample matrix files."""

    def test_valid_discrete_cna(self):
        """Check a valid discrete CNA file that should yield no errors."""
        self.logger.setLevel(logging.DEBUG)
        record_list = self.validate('data_cna_genecol_presence_both.txt',
                                    validateData.CNADiscreteValidator)
        # expecting only status messages about the file being validated
        self.assertEqual(len(record_list), 3)
        for record in record_list:
            self.assertLessEqual(record.levelno, logging.INFO)

    def test_repeated_gene(self):
        """Test if a warning is issued and the line is skipped if duplicate.

        In the test data, the Entrez ID in line 6 is removed. Therefore the gene symbol and gene alias table will be
        used to look up this gene in the database. ENTB5 is an alias for Entrez 116983 (ACAP3). This gene was defined
        earlier in the file, so the CENTB5 entry will be skipped. There are invalid values in this row, but because the
        entry is skipped, the values should not be validated."""

        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_cna_duplicate_gene.txt',
                                    validateData.CNADiscreteValidator)
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
                                    validateData.CNADiscreteValidator)
        # expecting various errors about data values, about one per line
        self.assertEqual(len(record_list), 6)
        for record in record_list:
            self.assertEqual(record.levelno, logging.ERROR)
        record_iterator = iter(record_list)
        record = next(record_iterator)
        self.assertEqual(record.line_number, 3)
        self.assertEqual(record.column_number, 7)
        # self.assertEqual(record.cause, ' ') if blank cells had a 'cause'
        record = next(record_iterator)
        self.assertEqual(record.line_number, 5)
        self.assertEqual(record.column_number, 4)
        # self.assertEqual(record.cause, '') if blank cells had a 'cause'
        record = next(record_iterator)
        self.assertEqual(record.line_number, 6)
        self.assertEqual(record.column_number, 3)
        self.assertEqual(record.cause, '3')
        record = next(record_iterator)
        self.assertEqual(record.line_number, 7)
        self.assertEqual(record.column_number, 6)
        self.assertEqual(record.cause, 'AURKAIP1')
        # Only "NA" is supported, anything else should be an error:
        record = next(record_iterator)
        self.assertEqual(record.line_number, 8)
        self.assertEqual(record.column_number, 5)
        self.assertEqual(record.cause, '[Not Available]')
        # Only -2, -1.5, -1, 0, 1, 2 are supported, anything else should be an error:
        record = next(record_iterator)
        self.assertEqual(record.line_number, 9)
        self.assertEqual(record.column_number, 6)
        self.assertEqual(record.cause, '1.5')

    def test_valid_rppa(self):
        """Check a valid RPPA file that should yield no errors."""
        self.logger.setLevel(logging.DEBUG)
        record_list = self.validate('data_rppa_valid.txt',
                                    validateData.ProteinLevelValidator)
        # expecting only status messages about the file being validated
        self.assertEqual(len(record_list), 3)
        for record in record_list:
            self.assertLessEqual(record.levelno, logging.INFO)

    def test_invalid_rppa(self):
        """Check an RPPA file with values that should yield errors."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_rppa_invalid_values.txt',
                                    validateData.ProteinLevelValidator)
        # expecting several errors
        self.assertEqual(len(record_list), 3)
        for record in record_list:
            self.assertEqual(record.levelno, logging.ERROR)
        record_iterator = iter(record_list)
        record = next(record_iterator)
        self.assertEqual(record.line_number, 3)
        self.assertEqual(record.column_number, 3)
        self.assertEqual(record.cause, 'spam')
        record = next(record_iterator)
        self.assertEqual(record.line_number, 6)
        self.assertEqual(record.column_number, 5)
        self.assertEqual(record.cause, '')
        record = next(record_iterator)
        self.assertEqual(record.line_number, 9)
        self.assertEqual(record.column_number, 3)
        self.assertEqual(record.cause, ' ')

    def test_repeated_rppa_entry(self):
        """Test if a warning is issued and the line is skipped if duplicate."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_rppa_duplicate_entries.txt',
                                    validateData.ProteinLevelValidator)
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
                                    validateData.ProteinLevelValidator)
        # expecting only a warning for each NA line
        self.assertEqual(len(record_list), 9)
        for record in record_list:
            self.assertEqual(record.levelno, logging.WARNING)
        for record, expected_line in zip(record_list, list(range(14, 23))):
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
        record = next(record_iterator)
        self.assertEqual(record.line_number, 2)
        self.assertEqual(record.column_number, 5)
        self.assertEqual(record.cause, '1.5')
        record = next(record_iterator)
        self.assertEqual(record.line_number, 5)
        self.assertEqual(record.column_number, 2)
        self.assertEqual(record.cause, '2.371393691351566')
        record = next(record_iterator)
        self.assertEqual(record.line_number, 7)
        self.assertEqual(record.column_number, 3)
        self.assertEqual(record.cause, '-12')


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
        record = next(record_iterator)
        self.assertEqual(record.line_number, 4)
        self.assertEqual(record.column_number, 2)
        self.assertEqual(record.cause, '1.5')
        record = next(record_iterator)
        self.assertEqual(record.line_number, 6)
        self.assertEqual(record.column_number, 4)
        self.assertEqual(record.cause, '1e3')
        record = next(record_iterator)
        self.assertEqual(record.line_number, 8)
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
        record = next(record_iterator)
        self.assertEqual(record.line_number, 1)
        self.assertIn('headers', record.getMessage().lower())
        self.assertIn('different', record.getMessage().lower())
        record = next(record_iterator)
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
        self.assertEqual("Gene sets column in score and p-value file are not equal. The same set of gene sets should be used in the score and p-value files for this study. Please ensure that all gene set id's of one file are present in the other gene set data file.", record.getMessage())
        return
        
class MultipleDataFileValidatorTestCase(unittest.TestCase):

    def feature_id_is_accepted(self):
        mockval = Mock()
        validateData.MultipleDataFileValidator.parseFeatureColumns(mockval, ["id-without_whitespace"])
        mockval.logger.error.assert_not_called()
        mockval.logger.warning.assert_not_called()

    def test_illegal_character_in_feature_id_issues_error(self):
        mockval = Mock()
        validateData.MultipleDataFileValidator.parseFeatureColumns(mockval, ["id with whitespace"])
        mockval.logger.error.assert_called()

    def test_comma_in_feature_id_issues_error(self):
        mockval = Mock()
        validateData.MultipleDataFileValidator.parseFeatureColumns(mockval, ["id,with-comma"])
        mockval.logger.error.assert_called()


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
        record = next(record_iterator)
        self.assertEqual(record.cause, 'n.a.')
        record = next(record_iterator)
        self.assertEqual(record.cause, '')
        record = next(record_iterator)
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

    def test_maf_file_checks(self):
        """Test errors for MAF file which is not according MAF file checks #6, #10 and #11"""
        # set level according to this test case:
        self.logger.setLevel(logging.ERROR)
        # Test file is a variant on test_output.custom_isoforms.maf from vcf2maf
        record_list = self.validate('mutations/data_mutations_test_variant_types.maf',
                                    validateData.MutationsExtendedValidator, None, True, True)
        # we expect 11 errors
        self.assertEqual(len(record_list), 11)
        record_iterator = iter(record_list)
        # expect error for bigger start than end position
        record = next(record_iterator)
        self.assertEqual(record.line_number, 2)
        self.assertIn('Start_Position should be smaller than or equal to End_Position.', record.getMessage())
        # expect error for not correct start and end position in INS
        record = next(record_iterator)
        self.assertEqual(record.line_number, 2)
        self.assertIn('Variant_Type indicates insertion, but difference in Start_Position and End_Position does '
                      'not equal to 1 or the length or the Reference_Allele.', record.getMessage())
        # expect error for incorrect length of reference allele with variant_type INS
        record = next(record_iterator)
        self.assertEqual(record.line_number, 3)
        self.assertIn('Variant_Type indicates insertion, but length of Reference_Allele is bigger than the length '
                      'of the Tumor_Seq_Allele1 and/or 2 and therefore indicates deletion.', record.getMessage())
        # expect error for not correct start and end position in DEL
        record = next(record_iterator)
        self.assertEqual(record.line_number, 4)
        self.assertIn('Variant_Type indicates deletion, but the difference between Start_Position and End_Position '
                      'are not equal to the length of the Reference_Allele.', record.getMessage())
        # expect error for incorrect length of  allele with variant_type DEL
        record = next(record_iterator)
        self.assertEqual(record.line_number, 5)
        self.assertIn('Variant_Type indicates deletion, but length of Reference_Allele is smaller than the length '
                      'of Tumor_Seq_Allele1 and/or Tumor_Seq_Allele2, indicating an insertion.', record.getMessage())
        # expect error for incorrect length of allele with variant_type SNP
        record = next(record_iterator)
        self.assertEqual(record.line_number, 6)
        self.assertIn('Variant_Type indicates a SNP, but length of Reference_Allele, Tumor_Seq_Allele1 '
                      'and/or Tumor_Seq_Allele2 do not equal 1.', record.getMessage())
        # expect error for incorrect Allele with variant_type SNP
        record = next(record_iterator)
        self.assertEqual(record.line_number, 7)
        self.assertIn('Variant_Type indicates a SNP, but Reference_Allele, Tumor_Seq_Allele1 '
                      'and/or Tumor_Seq_Allele2 contain deletion (-).', record.getMessage())
        # expect error for incorrect length of allele with variant_type DNP
        record = next(record_iterator)
        self.assertEqual(record.line_number, 8)
        self.assertIn('Variant_Type indicates a DNP, but length of Reference_Allele, Tumor_Seq_Allele1 '
                      'and/or Tumor_Seq_Allele2 do not equal 2.', record.getMessage())
        # expect error for incorrect length of allele with variant_type TNP
        record = next(record_iterator)
        self.assertEqual(record.line_number, 9)
        self.assertIn('Variant_Type indicates a TNP, but length of Reference_Allele, Tumor_Seq_Allele1 '
                      'and/or Tumor_Seq_Allele2 do not equal 3.', record.getMessage())
        # expect error for incorrect length of allele with variant_type ONP
        record = next(record_iterator)
        self.assertEqual(record.line_number, 10)
        self.assertIn('Variant_Type indicates a ONP, but length of Reference_Allele, '
                      'Tumor_Seq_Allele1 and 2 are not bigger than 3 or are of unequal lengths.', record.getMessage())
        record = next(record_iterator)
        self.assertEqual(record.line_number, 11)
        self.assertIn('Allele Based column Reference_Allele contains invalid character.', record.getMessage())
        
    def test_special_allele_cases(self):
        """Test errors in mutation file which contain special cases of the Allele Based columns"""
        # set level according to this test case:
        self.logger.setLevel(logging.ERROR)
        # Test file is a variant on test_output.custom_isoforms.maf from vcf2maf
        record_list = self.validate('mutations/data_mutations_check_special_cases_allele.maf',
                                    validateData.MutationsExtendedValidator, None, True, True)
        
        # We expect 3 errors
        self.assertEqual(len(record_list), 3)
        record_iterator = iter(record_list)
        # expect error for the same values in Reference_Allele, Tumor_Seq_Allele1 and Tumor_Seq_Allele2 columns
        record = next(record_iterator)
        self.assertEqual(record.line_number, 2)
        self.assertIn('All Values in columns Reference_Allele, Tumor_Seq_Allele1 and Tumor_Seq_Allele2 are equal.',
                      record.getMessage())
        # expect error for deletion, Tumor Seq allele columns do not contain -
        # even though the lengths of the sequences are equal
        record = next(record_iterator)
        self.assertEqual(record.line_number, 4)
        self.assertIn('Variant_Type indicates a deletion, Allele based columns are the same length, '
                      'but Tumor_Seq_Allele columns do not contain -, indicating a SNP.', record.getMessage())
        # expect error for ONP, lengths of sequences in Reference_Allele,
        # Tumor_Seq_Allele1 and Tumor_Seq_Allele2 are not equal
        record = next(record_iterator)
        self.assertEqual(record.line_number, 5)
        self.assertIn('Variant_Type indicates a ONP, but length of Reference_Allele, '
                      'Tumor_Seq_Allele1 and 2 are not bigger than 3 or are of unequal lengths.', record.getMessage())

    def test_validation_status_MAF_checks(self):
        """Test errors for MAF file which is not according MAF file checks #7, #8, #9 and #13"""
        # set level according to this test case:
        self.logger.setLevel(logging.ERROR)
        # Test file is a variant on mutation file from study_es_0
        record_list = self.validate('mutations/data_mutations_validation_status.maf',
                                    validateData.MutationsExtendedValidator, None, True, True)
        
        # We expect 6 errors
        self.assertEqual(len(record_list), 6)
        record_iterator = iter(record_list)
        # expect error for empty validation allele columns
        record = next(record_iterator)
        self.assertEqual(record.line_number, 2)
        self.assertIn('Validation Status is valid, but Validation Allele columns are empty.', record.getMessage())
        # expect error for invalid character in allele based column
        record = next(record_iterator)
        self.assertEqual(record.line_number, 3)
        self.assertIn('At least one of the Validation Allele Based columns (Tumor_Validation_Allele1, '
                      'Tumor_Validation_Allele2, Match_Norm_Validation_Allele1, Match_Norm_Validation_Allele2) '
                      'contains invalid character.', record.getMessage())
        # expect error for not equal tumor and normal allele columns when Validation_Status is invalid
        record = next(record_iterator)
        self.assertEqual(record.line_number, 4)
        self.assertIn('When Validation_Status is invalid the Tumor_Validation_Allele and Match_Norm_'
                      'Validation_Allele columns should be equal.', record.getMessage())
        # expect error for undefined Validation_Method when Validation_Status is valid or invalid
        record = next(record_iterator)
        self.assertEqual(record.line_number, 5)
        self.assertIn('Validation Status is invalid, but Validation_Method is not defined.', record.getMessage())
        # expect error for incorrect Validation Allele Columns when Mutation_Status is Germline
        record = next(record_iterator)
        self.assertEqual(record.line_number, 6)
        self.assertIn('When Validation_Status is valid and Mutation_Status is Germline, the Tumor_Validation_Allele '
                      'should be equal to the Match_Norm_Validation_Allele.', record.getMessage())
        # expect error for incorrect Validation Allele Columns when Mutation_Status is Germline
        record = next(record_iterator)
        self.assertEqual(record.line_number, 7)
        self.assertIn('When Validation_Status is valid and Mutation_Status is Somatic, the Match_Norm_Validation_Allele'
                      ' columns should be equal to the Reference Allele and one of the Tumor_Validation_Allele columns'
                      ' should not be.', record.getMessage())

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
        """Test warnings for invalid and unknown accessions under SWISSPROT."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate(
                'mutations/data_mutations_invalid_swissprot.maf',
                validateData.MutationsExtendedValidator,
                extra_meta_fields={
                    'swissprot_identifier': 'accession'})
        self.assertEqual(len(record_list), 2)
        record_iterator = iter(record_list)
        # used a name instead of an accession
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertEqual(record.line_number, 3)
        self.assertEqual(record.cause, 'A1CF_HUMAN')
        self.assertNotIn('portal', record.getMessage().lower())
        # neither a name nor an accession
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.WARNING)
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
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertEqual(record.line_number, 3)
        self.assertEqual(record.cause, 'Q9NQ94')
        self.assertNotIn('portal', record.getMessage().lower())
        # neither a name nor an accession
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertEqual(record.line_number, 5)
        self.assertEqual(record.cause, 'A1CF_HUMAN,HBB_YEAST')
        self.assertNotIn('portal', record.getMessage().lower())

    def test_invalid_swissprot_identifier_type(self):
        """Test if the validator rejects files with nonsensical id types."""
        self.logger.setLevel(logging.ERROR)
        meta_dictionary = validateData.cbioportal_common.parse_metadata_file(
                'test_data/mutations/meta_mutations_invalid_swissprot_idspec.txt',
                self.logger,
                study_id='spam')
        record_list = self.get_log_records()
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.cause, 'namelessly')
        self.assertIsNone(meta_dictionary['meta_file_type'], 'metadata file was not rejected as invalid')

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
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertIn('Amino_Acid_Change', record.getMessage())
        self.assertIn('HGVSp_Short', record.getMessage())
        self.assertEqual(record.line_number, 2)
        # multiple specifications
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('p.', record.getMessage())
        self.assertEqual(record.cause, 'p.A195V;p.I167I')
        # comma in the string
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('comma', record.getMessage().lower())
        self.assertEqual(record.cause, 'p.N851,Y1055delinsCC')
        # haplotype specification of multiple mutations
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('allele', record.getMessage().lower())
        self.assertEqual(record.cause, 'p.[N851N];[Y1055C]')
        # NULL (and no HGVSp_Short column)
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertIn('Amino_Acid_Change', record.getMessage())
        self.assertIn('HGVSp_Short', record.getMessage())
        self.assertEqual(record.line_number, 8)

    def test_isValidVariantClassification(self):
        """Test if proper warnings/errors are given for wrong/blank Variant_Classification change vals."""
        # set level according to this test case:
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate(
                'mutations/data_mutations_invalid_variant_classification.maf',
                validateData.MutationsExtendedValidator,
                extra_meta_fields={'swissprot_identifier': 'name'})
        # we expect 1 warning and 1 error:
        self.assertEqual(len(record_list), 2)
        record_iterator = iter(record_list)
        # first is a warning about wrong value:
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertIn('not one of the expected values', record.getMessage())
        # second is an error about empty value (not allowed):
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('is invalid', record.getMessage())

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
        self.logger.setLevel(logging.INFO)
        record_list = self.validate('mutations/data_mutations_silent_alternative.maf',
                                    validateData.MutationsExtendedValidator,
                                    extra_meta_fields={
                                            'swissprot_identifier': 'name'})
        # we expect 1 ERROR, 2 WARNINGs and 5 INFO:
        self.assertEqual(len(record_list), 8)

        # ERROR should be something like: "No Entrez id or gene symbol provided for gene"
        self.assertIn("no entrez gene id or gene symbol provided", record_list[1].getMessage().lower())
        self.assertEqual(record_list[1].levelno, logging.ERROR)
        # WARNING should be something like: "Gene specification for this mutation implies intergenic..."
        self.assertIn("implies intergenic", record_list[2].getMessage().lower())
        self.assertEqual(record_list[2].levelno, logging.WARNING)
        self.assertIn("implies intergenic", record_list[3].getMessage().lower())
        self.assertEqual(record_list[3].levelno, logging.WARNING)
        # INFO should be: "this variant (gene symbol 'unknown', entrez gene id 0) will be filtered out"
        self.assertIn("this variant (gene symbol 'unknown', entrez gene id 0) will be filtered out", record_list[4].getMessage().lower())
        self.assertEqual(record_list[4].levelno, logging.INFO)

    def test_customized_variants_skipped(self):
        
        """Test if customized mutations are skipped with a message."""
        # set level according to this test case:
        self.logger.setLevel(logging.INFO)
        old_variant_types = validateData.MutationsExtendedValidator.SKIP_VARIANT_TYPES
        validateData.MutationsExtendedValidator.SKIP_VARIANT_TYPES = ["5'Flank", "Frame_Shift_Del", "Frame_Shift_Ins"]
        record_list = self.validate('mutations/data_mutations_some_silent.maf',
                                    validateData.MutationsExtendedValidator,
                                    extra_meta_fields={
                                            'swissprot_identifier': 'name'})
        # we expect 6 infos: 4 about filtered mutations, 2 general info messages:
        self.assertEqual(len(record_list), 6)
        # First 3 INFO messages should be something like: "Line will not be loaded due to the variant classification filter. Filtered types:"
        for record in record_list[:4]: 
            self.assertIn("filtered types", record.getMessage().lower())
        
        # restore the default skipped variant types
        validateData.MutationsExtendedValidator.SKIP_VARIANT_TYPES = old_variant_types
               
    def test_isValidGenePosition(self):
        """Test if proper warnings/errors are given for wrong/blank gene positions 
        (Start_Position and End_Position) change vals."""
        # set level according to this test case:
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate(
                'mutations/data_mutations_wrong_gene_position.maf',
                validateData.MutationsExtendedValidator,
                extra_meta_fields={'swissprot_identifier': 'name'})
        # we expect 4 errors:
        self.assertEqual(len(record_list), 4)
        record_iterator = iter(record_list)
        # first is an error about wrong value in Start_Position:
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('The start position of this variant is not '
                      'an integer', record.getMessage())
        # second is an error about wrong value in End_Position:
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('The end position of this variant is not '
                      'an integer', record.getMessage())
        # third is an error about no value in Start_Position:
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('The start position of this variant is not '
                      'an integer', record.getMessage())
        # forth is an error about no value in End_Position:
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('The end position of this variant is not '
                      'an integer', record.getMessage())
        
    def test_absence_custom_values_columns_when_custom_annotation_columns(self):
        """Test that the validator raises an error when the 
        cbp_driver_annotation and the cbp_driver_tiers_annotation
        columns are present but the cbp_driver and the 
        cbp_driver_tiers columns are not.
        """
        # set level according to this test case:
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('mutations/data_mutations_absence_custom_values_columns_when_custom_annotation_columns.maf',
                                    validateData.MutationsExtendedValidator,
                                    extra_meta_fields={
                                            'swissprot_identifier': 'name'})
        # we expect 5 ERRORs :
        self.assertEqual(len(record_list), 3)
        
        # First 2 ERRORs should be something like: "Column X found without any X column"
        self.assertIn("Column cbp_driver_annotation found without any cbp_driver column.", record_list[0].getMessage())
        self.assertEqual(record_list[0].levelno, logging.ERROR)
        self.assertIn("Column cbp_driver_tiers_annotation found without any cbp_driver_tiers column.", record_list[1].getMessage())
        self.assertEqual(record_list[1].levelno, logging.ERROR)

    def test_absence_custom_annotation_columns_when_custom_values_columns(self):
        """Test that the validator raises an error when the 
        cbp_driver and the cbp_driver_tiers columns are present 
        but the cbp_driver_annotation and the 
        cbp_driver_tiers_annotation columns are not.
        """
        # set level according to this test case:
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('mutations/data_mutations_absence_custom_annotation_columns_when_custom_values_columns.maf',
                                    validateData.MutationsExtendedValidator,
                                    extra_meta_fields={
                                            'swissprot_identifier': 'name'})
        # we expect 5 ERRORs :
        self.assertEqual(len(record_list), 3)
        
        # First 2 ERRORs should be something like: "Column X found without any Y column"
        self.assertIn('Column cbp_driver found without any cbp_driver_annotation column.', record_list[0].getMessage())
        self.assertEqual(record_list[0].levelno, logging.ERROR)
        self.assertIn('Column cbp_driver_tiers found without any cbp_driver_tiers_annotation column.', record_list[1].getMessage())
        self.assertEqual(record_list[1].levelno, logging.ERROR)
        
    def test_empty_custom_annotation_fields(self):
        """Test that the validator raises errors when one multiclass
        column is empty and the other is full, and that the binary
        annotation column is full when the binary label column contains
        "Putative_Driver" or "Putative_Passenger".
        """
        # set level according to this test case:
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('mutations/data_mutations_empty_custom_annotation_fields.maf',
                                    validateData.MutationsExtendedValidator,
                                    extra_meta_fields={
                                            'swissprot_identifier': 'name'})
        self.assertEqual(len(record_list), 1)
        record = record_list[0]
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('This line has no value for cbp_driver_tiers and a value for cbp_driver_tiers_annotation. Please, fill the cbp_driver_tiers column.', record.getMessage())

    def test_warning_more_than_10_types_in_driver_class(self):
        """Test that the validator raises a warning when the column
        cbp_driver_tiers contains more than 10 types.
        """
        # set level according to this test case:
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('mutations/data_mutations_more_than_10_types_in_driver_class.maf',
                                    validateData.MutationsExtendedValidator,
                                    extra_meta_fields={
                                            'swissprot_identifier': 'name'})
        # we expect 3 WARNINGs :
        self.assertEqual(len(record_list), 3)

        # WARNINGs should be something like: "cbp_driver_tiers contains more than 10 different values"
        self.assertIn("cbp_driver_tiers contains more than 10 different tiers.", record_list[0].getMessage().lower())
        self.assertEqual(record_list[0].levelno, logging.WARNING)
        self.assertIn("cbp_driver_tiers contains more than 10 different tiers.", record_list[1].getMessage().lower())
        self.assertEqual(record_list[1].levelno, logging.WARNING)
        self.assertIn("cbp_driver_tiers contains more than 10 different tiers.", record_list[2].getMessage().lower())
        self.assertEqual(record_list[2].levelno, logging.WARNING)
        
    def test_annotation_more_than_80_characters_in_custom_annotation_columns(self):
        """Test if the validator raises an error if any value of the annotation
        columns has more than 80 characters.
        """
        # set level according to this test case:
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('mutations/data_mutations_more_than_80_characters_in_custom_annotation_columns.maf',
                                    validateData.MutationsExtendedValidator,
                                    extra_meta_fields={
                                            'swissprot_identifier': 'name'})
        # we expect 1 ERROR :
        self.assertEqual(len(record_list), 1)
        
        # ERROR should be something like: "cbp_driver_annotation and cbp_driver_tiers_annotation columns do not support annotations longer than 80 characters"
        self.assertIn("columns do not support annotations longer than 80 characters", record_list[0].getMessage().lower())
        self.assertEqual(record_list[0].levelno, logging.ERROR)
        
    def test_not_supported_custom_driver_annotation_values(self):
        """Test if the validator raises an error if any value of the
        cbp_driver is not Putative_Passenger or
        Putative_Driver.
        """
        # set level according to this test case:
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('mutations/data_mutations_not_supported_custom_driver_annotation_values.maf',
                                    validateData.MutationsExtendedValidator,
                                    extra_meta_fields={
                                            'swissprot_identifier': 'name'})
        # we expect 1 ERROR :
        self.assertEqual(len(record_list), 1)
        
        # ERROR should be something like: "Only "Putative_Passenger", "Putative_Driver", "NA", "Unknown" and "" (empty) are allowed."
        self.assertIn('only "putative_passenger", "putative_driver", "na", "unknown" and "" (empty) are allowed.', record_list[0].getMessage().lower())
        self.assertEqual(record_list[0].levelno, logging.ERROR)
        
    def test_custom_driver_column_more_than_50_characters(self):
        """Test if the validator raises an error if any value of the 
        cbp_driver_tiers column has more than 50 characters.
        """
        # set level according to this test case:
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('mutations/data_mutations_custom_tiers_column_more_than_50_characters.maf',
                                    validateData.MutationsExtendedValidator,
                                    extra_meta_fields={
                                            'swissprot_identifier': 'name'})
        # we expect 1 ERROR :
        self.assertEqual(len(record_list), 1)
        
        # ERROR should be something like: "cbp_driver_tiers column does not support values longer than 50 characters"
        self.assertIn("does not support values longer than 50 characters", record_list[0].getMessage().lower())
        self.assertEqual(record_list[0].levelno, logging.ERROR)

    def test_validation_verification_status(self):

        """Assert names are validated in data_mutations_vs.maf."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('mutations/data_mutations_vs.maf',
                                    validateData.MutationsExtendedValidator,
                                    extra_meta_fields={'swissprot_identifier': 'name'})

        # we expect 2 errors:
        self.assertEqual(len(record_list), 2)

        record_iterator = iter(record_list)
        # Validation Status error
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertEqual(record.line_number, 2)
        self.assertEqual(record.cause, '---')
        self.assertEqual(record.getMessage(), "Value in 'Validation_Status' not in MAF format")
        # Verification status error
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertEqual(record.line_number, 3)
        self.assertEqual(record.cause, 'Test')
        self.assertEqual(record.getMessage(), "Value in 'Verification_Status' not in MAF format")

    def test_validation_ms(self):

        """Test if warning is given when Mutation_Status column in mutation
        file contains other than allowed values from MAF format or Wildtype."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('mutations/data_mutations_ms.maf',
                                    validateData.MutationsExtendedValidator,
                                    extra_meta_fields={'swissprot_identifier': 'name'})
        # we expect 1 warning
        self.assertEqual(len(record_list), 1)

        # Check warning message
        self.assertEqual(record_list[0].levelno, logging.WARNING)
        self.assertEqual(record_list[0].line_number, 2)
        self.assertEqual(record_list[0].cause, 'test')
        self.assertEqual(record_list[0].getMessage(), "Mutation_Status value is not in MAF format")

    def test_mutation_not_loaded_ms(self):

        """Test if info message is given when Mutation_Status columns have either
        LOH, None or Wildtype. In these cases the mutation is not loaded in cBioPortal."""
        self.logger.setLevel(logging.INFO)
        record_list = self.validate('mutations/data_mutations_not_loaded_ms.maf',
                                    validateData.MutationsExtendedValidator,
                                    extra_meta_fields={'swissprot_identifier': 'name'})

        # We expect 5 info messages, 3 from not loaded mutations and 2 general lines
        self.assertEqual(len(record_list), 5)
        record_iterator = iter(record_list)
        # Expected info message due to value "None" in Mutation_Status
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.INFO)
        self.assertEqual(record.line_number, 3)
        self.assertEqual(record.cause, 'None')
        self.assertEqual(record.getMessage(), "Mutation will not be loaded due to value in Mutation_Status")
        # Expected info message due to value "loh" in Mutation_Status
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.INFO)
        self.assertEqual(record.line_number, 5)
        self.assertEqual(record.cause, 'loh')
        self.assertEqual(record.getMessage(), "Mutation will not be loaded due to value in Mutation_Status")
        # Expected info message due to value "Wildtype" in Mutation_Status
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.INFO)
        self.assertEqual(record.line_number, 9)
        self.assertEqual(record.cause, 'Wildtype')
        self.assertEqual(record.getMessage(), "Mutation will not be loaded due to value in Mutation_Status")

    def test_mutation_invalid_utf8(self):
        """Test that the validator raises an error when a data file contains invalid UTF-8 bytes".
        """
        # set level according to this test case:
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('mutations/data_mutations_invalid_utf8.maf',
                                    validateData.MutationsExtendedValidator)
        # we expect 1 ERROR:
        self.assertEqual(len(record_list), 1)

        # The ERROR should be: "UTF-8 codec can't decode byte"
        self.assertIn("File contains invalid UTF-8 bytes. Please check values in file", record_list[0].getMessage())
        self.assertEqual(record_list[0].levelno, logging.ERROR)


class FusionValidationTestCase(PostClinicalDataFileTestCase):

    """Tests for the various validations of data in Fusion data files."""

    def test_duplicate_line(self):
        """Test if duplicate lines are detected"""
        # set level according to this test case:
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_fusions_duplicate_entry.txt',
                                    validateData.FusionValidator)

        self.assertEqual(len(record_list), 1)
        self.assertIn("duplicate entry in fusion data", record_list[0].getMessage().lower())


class StructuralVariantValidationTestCase(PostClinicalDataFileTestCase):
    """Tests for the various validations of data in structural variant data files."""

    def test_missing_columns(self):
        """Test whether the exons are found in the transcript"""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_structural_variants_missing_columns.txt',
                                    validateData.StructuralVariantValidator)
        self.assertEqual(3, len(record_list))
        record_iterator = iter(record_list)

        # Expected ERROR message due to missing Ensembl transcript column
        record = next(record_iterator)
        self.assertEqual(logging.ERROR, record.levelno)
        self.assertEqual(1, record.line_number)
        self.assertEqual('Fusion event requires "Site1_Exon" and "Site2_Exon" columns', record.message)

        # Expected ERROR message due to missing Exon column
        record = next(record_iterator)
        self.assertEqual(logging.ERROR, record.levelno)
        self.assertEqual(1, record.line_number)
        self.assertEqual('Fusion event requires "Site1_Ensembl_Transcript_Id" and "Site2_Ensembl_Transcript_Id" '
                         'columns', record.message)

        # Expected generic ERROR message due to invalid column header
        record = next(record_iterator)
        self.assertEqual(logging.ERROR, record.levelno)
        self.assertEqual('Invalid column header, file cannot be parsed', record.message)

    def test_missing_values(self):
        """Test whether the exons are found in the transcript"""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_structural_variants_missing_values.txt',
                                    validateData.StructuralVariantValidator)
        self.assertEqual(4, len(record_list))
        record_iterator = iter(record_list)

        # Expected ERROR message due to missing Ensembl transcript column
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(2, record.line_number)
        self.assertEqual(11, record.column_number)
        self.assertIn("No Ensembl transcript ID found.", record.message)

        # Expected ERROR message due to missing Exon column
        record = next(record_iterator)
        self.assertEqual(logging.ERROR, record.levelno)
        self.assertEqual(3, record.line_number)
        self.assertEqual(4, record.column_number)
        self.assertIn("No Ensembl transcript ID found.", record.message)

        # Expected ERROR message due to missing Ensembl transcript column
        record = next(record_iterator)
        self.assertEqual(logging.ERROR, record.levelno)
        self.assertEqual(6, record.line_number)
        self.assertEqual(5, record.column_number)
        self.assertIn("No exon found.", record.message)

        # Expected ERROR message due to missing Exon column
        record = next(record_iterator)
        self.assertEqual(logging.ERROR, record.levelno)
        self.assertEqual(8, record.line_number)
        self.assertEqual(12, record.column_number)
        self.assertIn("No exon found.", record.message)

    def test_transcript_not_in_genome_nexus(self):
        """Test whether the transcripts are validated correctly by checking Genome Nexus"""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_structural_variants_transcript_not_in_genome_nexus.txt',
                                    validateData.StructuralVariantValidator)

        self.assertEqual(1, len(record_list))
        record_iterator = iter(record_list)

        # Expected ERROR message due to value "1500" in Site1_Exon in line 2
        record = next(record_iterator)
        self.assertEqual(logging.ERROR, record.levelno)
        self.assertEqual("TEST_TRANSCRIPT", record.cause)
        self.assertIn("Ensembl transcript not found in Genome Nexus.", record.getMessage())

    def test_exon_not_in_transcript(self):
        """Test whether the exons are found in the transcript"""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_structural_variants_exon_not_in_transcript.txt',
                                    validateData.StructuralVariantValidator)

        self.assertEqual(len(record_list), 2)
        record_iterator = iter(record_list)

        # Expected ERROR message due to value "1500" in Site1_Exon in line 2
        record = next(record_iterator)
        self.assertEqual(logging.ERROR, record.levelno)
        self.assertEqual(2, record.line_number)
        self.assertEqual("1500 not in ENST00000242365", record.cause)
        self.assertIn("Exon is not found in rank of transcript", record.getMessage())

        # Expected ERROR message due to value "2000" in Site2_Exon in line 4
        record = next(record_iterator)
        self.assertEqual(logging.ERROR, record.levelno)
        self.assertEqual(4, record.line_number)
        self.assertEqual("2000 not in ENST00000389048", record.cause)
        self.assertIn("Exon is not found in rank of transcript", record.getMessage())


class SegFileValidationTestCase(PostClinicalDataFileTestCase):

    """Tests for the various validations of data in segment CNA data files."""

    @classmethod
    def setUpClass(cls):
        """Override a static method to skip a UCSC HTTP query in each test."""
        super(SegFileValidationTestCase, cls).setUpClass()
        @staticmethod
        def load_chromosome_lengths(genome_build, _):
            if genome_build == 'hg19':
                return {'1': 249250621, '10': 135534747, '11': 135006516,
                        '12': 133851895, '13': 115169878, '14': 107349540,
                        '15': 102531392, '16': 90354753, '17': 81195210,
                        '18': 78077248, '19': 59128983, '2': 243199373,
                        '20': 63025520, '21': 48129895, '22': 51304566,
                        '3': 198022430, '4': 191154276, '5': 180915260,
                        '6': 171115067, '7': 159138663, '8': 146364022,
                        '9': 141213431, 'X': 155270560, 'Y': 59373566}
            else:
                raise ValueError(
                    "load_chromosome_lengths() called with genome build '{}'".format(
                        genome_build))
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

    def test_invalid_ref_genome_hg18(self):
        """Test validation of a segment file which has hg18 data but is submitted as hg19"""
        self.logger.setLevel(logging.ERROR)

        # The input file contains a genomic position from hg18 (chr19, 63811651)
        record_list = self.validate('data_seg_invalid_hg18.seg',
                                    validateData.SegValidator,
                                    extra_meta_fields={'reference_genome_id':
                                                       'hg19'})
        # Expect 1 error
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertLessEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.cause, '63811651')
        self.assertEqual(record.message, 'Genomic position beyond end of chromosome (chr19:0-59128983)')

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
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 11)
        # zero-length segment
        record = next(record_iterator)
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
        self.assertEqual(len(record_list), 4)
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
        self.assertEqual(len(record_list), 4)
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
        record = next(record_iterator)
        self.assertEqual(record.line_number, 2)
        self.assertEqual(record.column_number, 6)
        # mismatch between chromosome number in chromosome and cytoband cols
        record = next(record_iterator)
        self.assertEqual(record.line_number, 2)
        self.assertEqual(record.cause, '(1p36.13, 2)')
        # q-value not a real number
        record = next(record_iterator)
        self.assertEqual(record.line_number, 3)
        self.assertEqual(record.column_number, 8)
        # reversed start and end positions
        record = next(record_iterator)
        self.assertEqual(record.line_number, 3)
        self.assertIn('not lower', record.getMessage())
        # incorrect 'amp' value
        record = next(record_iterator)
        self.assertEqual(record.line_number, 4)
        self.assertEqual(record.column_number, 6)
        # no p or q in cytoband
        record = next(record_iterator)
        self.assertEqual(record.line_number, 4)
        self.assertEqual(record.column_number, 7)
        # missing chromosome
        record = next(record_iterator)
        self.assertEqual(record.line_number, 5)
        self.assertEqual(record.column_number, 2)
        # missing chromosome in cytoband
        record = next(record_iterator)
        self.assertEqual(record.line_number, 5)
        self.assertEqual(record.column_number, 7)
        # blank gene in list
        record = next(record_iterator)
        self.assertEqual(record.line_number, 6)
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertEqual(record.cause, '')

class GenePanelMatrixValidationTestCase(PostClinicalDataFileTestCase):

    """Test for validations in Gene Panel Matrix."""

    def test_duplicate_sample(self):
        """Test if duplicate samples are detected"""
        # set level according to this test case:
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_gene_matrix_duplicate_sample.txt',
                                    validateData.GenePanelMatrixValidator)

        self.assertEqual(len(record_list), 1)
        self.assertIn("duplicated sample id.", record_list[0].getMessage().lower())

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
            self.logger, False, False)
        record_list = self.get_log_records()
        # expecting two errors: one about the two cancer type files, and
        # about the cancer type of the study not having been defined
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.ERROR)
        # compare filenames mentioned in the 1st error independent of ordering
        filenames_in_cause_string = set(record_list[0].cause.split(', ', 1))
        self.assertEqual(filenames_in_cause_string,
                         {'cancer_type_luad.txt', 'cancer_type_lung.txt'})
        # assert that the second error complains about the cancer type
        self.assertEqual(record_list[1].cause, 'luad')
        
    def test_invalid_tags_file(self):
        """Test if an error is reported when giving a study tags file with wrong format."""
        with temp_inputfolder({
            'meta_study.txt': textwrap.dedent('''\
                cancer_study_identifier: spam
                type_of_cancer: brca
                name: Spam (spam)
                description: Baked beans
                short_name: Spam
                add_global_case_list: true
                tags_file: study_tags.yml
                '''),
            'meta_samples.txt': textwrap.dedent('''\
                cancer_study_identifier: spam
                genetic_alteration_type: CLINICAL
                datatype: SAMPLE_ATTRIBUTES
                data_filename: data_samples.txt
                '''),
            'data_samples.txt': textwrap.dedent('''\
                #Patient Identifier\tSample Identifier
                #PatID\tSampId
                #STRING\tSTRING
                #1\t1
                PATIENT_ID\tSAMPLE_ID
                Patient1\tPatient1-Sample1
                '''),
            'study_tags.yml': textwrap.dedent('''\
                Invalid:
                {
                ''')
        }) as study_dir:
            self.logger.setLevel(logging.WARNING)
            validateData.validate_study(
                study_dir,
                PORTAL_INSTANCE,
                self.logger,
                relaxed_mode=False,
                strict_maf_checks=False)
            record_list = self.get_log_records()
            self.assertEqual(len(record_list), 1)
            for record in record_list:
                self.assertEqual(record.levelno, logging.ERROR)
                self.assertIn('yaml', record.getMessage().lower())
                
    def test_valid_JSON_tags_file(self):
        """Test if no errors are reported when giving a study tags file in JSON format."""
        with temp_inputfolder({
            'meta_study.txt': textwrap.dedent('''\
                cancer_study_identifier: spam
                type_of_cancer: brca
                name: Spam (spam)
                description: Baked beans
                short_name: Spam
                add_global_case_list: true
                tags_file: study_tags.yml
                '''),
            'meta_samples.txt': textwrap.dedent('''\
                cancer_study_identifier: spam
                genetic_alteration_type: CLINICAL
                datatype: SAMPLE_ATTRIBUTES
                data_filename: data_samples.txt
                '''),
            'data_samples.txt': textwrap.dedent('''\
                #Patient Identifier\tSample Identifier
                #PatID\tSampId
                #STRING\tSTRING
                #1\t1
                PATIENT_ID\tSAMPLE_ID
                Patient1\tPatient1-Sample1
                '''),
            'study_tags.yml': textwrap.dedent('''\
                { name: study_name }
                ''')
        }) as study_dir:
            self.logger.setLevel(logging.WARNING)
            validateData.validate_study(
                study_dir,
                PORTAL_INSTANCE,
                self.logger,
                relaxed_mode=False,
                strict_maf_checks=False)
            record_list = self.get_log_records()
            self.assertEqual(len(record_list), 0)
            for record in record_list:
                self.assertEqual(record.levelno, logging.ERROR)
    
    def test_valid_YAML_tags_file(self):
        """Test if no errors are reported when giving a study tags file in YAML format."""
        with temp_inputfolder({
            'meta_study.txt': textwrap.dedent('''\
                cancer_study_identifier: spam
                type_of_cancer: brca
                name: Spam (spam)
                description: Baked beans
                short_name: Spam
                add_global_case_list: true
                tags_file: study_tags.yml
                '''),
            'meta_samples.txt': textwrap.dedent('''\
                cancer_study_identifier: spam
                genetic_alteration_type: CLINICAL
                datatype: SAMPLE_ATTRIBUTES
                data_filename: data_samples.txt
                '''),
            'data_samples.txt': textwrap.dedent('''\
                #Patient Identifier\tSample Identifier
                #PatID\tSampId
                #STRING\tSTRING
                #1\t1
                PATIENT_ID\tSAMPLE_ID
                Patient1\tPatient1-Sample1
                '''),
            'study_tags.yml': textwrap.dedent('''\
                name: study_name
                ''')
        }) as study_dir:
            self.logger.setLevel(logging.WARNING)
            validateData.validate_study(
                study_dir,
                PORTAL_INSTANCE,
                self.logger,
                relaxed_mode=False,
                strict_maf_checks=False)
            record_list = self.get_log_records()
            self.assertEqual(len(record_list), 0)
            for record in record_list:
                self.assertEqual(record.levelno, logging.ERROR)

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

    def test_duplicated_sample_id(self):
        """Test if an warning is issued when one list has duplicate sample id"""
        self.logger.setLevel(logging.WARNING)
        validateData.processCaseListDirectory(
            'test_data/case_lists_duplicated_sampleid',
            'brca_tcga_pub',
            self.logger)
        record_list = self.get_log_records()
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertIn('duplicate', record.getMessage().lower())

    def test_invalid_category(self):
        """Test if an error is issued for an invalid case list category"""
        self.logger.setLevel(logging.ERROR)
        validateData.processCaseListDirectory(
            'test_data/case_lists_invalid_category',
            'brca_tcga_pub',
            self.logger)
        record_list = self.get_log_records()
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('invalid', record.getMessage().lower())

    def test_duplicate_category(self):
        """Test if an error is issued for an duplicate case list category"""
        self.logger.setLevel(logging.WARNING)
        validateData.processCaseListDirectory(
            'test_data/case_lists_duplicate_category',
            'brca_tcga_pub',
            self.logger)
        record_list = self.get_log_records()
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertIn('used in other case list', record.getMessage().lower())

    def test_missing_caselists(self):
        """Test if errors are issued if certain case lists are not defined."""
        self.logger.setLevel(logging.ERROR)
        validateData.validate_study(
                'test_data/study_missing_caselists',
                PORTAL_INSTANCE,
                self.logger,
                False, False)
        record_list = self.get_log_records()

        # Test if there are 3 warnings, for 3 missing case lists
        self.assertEqual(len(record_list), 3)
        record_iterator = iter(record_list)

        # Test the missing global case list
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('spam_all', record.getMessage())
        self.assertIn('add_global_case_list', record.getMessage())

        # Test the missing global _sequenced list
        record = next(record_iterator)
        self.assertIn('spam_sequenced', record.getMessage())
        self.assertIn('please add this case list', record.getMessage())

        # Test the missing global _cna list
        record = next(record_iterator)
        self.assertIn('spam_cna', record.getMessage())
        self.assertIn('please add this case list', record.getMessage())

    def test_undefined_cases_listed_in_file_order(self):
        """Test if undefined cases are reported in the order encountered."""
        with temp_inputfolder({
            'meta_study.txt': textwrap.dedent('''\
                cancer_study_identifier: spam
                type_of_cancer: brca
                name: Spam (spam)
                description: Baked beans
                short_name: Spam
                '''),
            'meta_samples.txt': textwrap.dedent('''\
                cancer_study_identifier: spam
                genetic_alteration_type: CLINICAL
                datatype: SAMPLE_ATTRIBUTES
                data_filename: data_samples.txt
                '''),
            'data_samples.txt': textwrap.dedent('''\
                #Patient Identifier\tSample Identifier
                #PatID\tSampId
                #STRING\tSTRING
                #1\t1
                PATIENT_ID\tSAMPLE_ID
                Patient1\tPatient1-Sample1
                '''),
            Path('case_lists', 'cases_all.txt'): textwrap.dedent('''\
                cancer_study_identifier: spam
                stable_id: spam_all
                case_list_name: All tumors
                case_list_description: All tumor samples (4 samples)
                case_list_ids: Patient0-Sample1\tPatient2-Sample3\tPatient1-Sample1\tPatient1-Sample2
                ''')
        }) as study_dir:
            self.logger.setLevel(logging.WARNING)
            validateData.validate_study(
                study_dir,
                PORTAL_INSTANCE,
                self.logger,
                relaxed_mode=False,
                strict_maf_checks=False)
            record_list = self.get_log_records()
            reported_sample_ids = [record.cause for record in record_list]
            self.assertEqual(
                reported_sample_ids,
                ['Patient0-Sample1', 'Patient2-Sample3', 'Patient1-Sample2'])

class MetaFilesTestCase(LogBufferTestCase):

    """Tests for the contents of the meta files."""

    def test_unnecessary_and_wrong_stable_id(self):
        """Tests to check behavior when stable_id is not needed (warning) or wrong(error)."""
        self.logger.setLevel(logging.WARNING)
        validateData.process_metadata_files(
            'test_data/study_metastableid',
            PORTAL_INSTANCE,
            self.logger, False, False)
        record_list = self.get_log_records()
        # expecting 1 warning, 3 errors:
        self.assertEqual(len(record_list), 4)
        # get both into a variable to avoid dependency on order:
        errors = []
        for record in record_list:
            if record.levelno == logging.ERROR:
                errors.append(record.cause)
            else:
                warning = record

        # expecting one error about wrong stable_id in meta_expression:
        self.assertEqual(len(errors), 3)
        self.assertIn('mrna_test', errors)
        self.assertIn('gistic', errors)
        self.assertIn('treatment ic50', errors)

        # expecting one warning about stable_id not being recognized in _samples
        self.assertEqual(warning.levelno, logging.WARNING)
        self.assertEqual(warning.cause, 'stable_id')

    def test_exceed_maximum_length_meta_attribute_value(self):
        """Test to check whether the validator throws a warning for invalid length of attributes in meta files."""
        self.logger.setLevel(logging.WARNING)
        validateData.process_metadata_files(
            'test_data/meta_study/exceed_maximum_length_meta_attribute_value',
            PORTAL_INSTANCE,
            self.logger, False, False)
        record_list = self.get_log_records()
        # expecting 1 error:
        self.assertEqual(len(record_list), 1)

        # expecting one error about the maximum length of 'short_name' meta_study
        record = record_list.pop()
        self.assertEqual("The maximum length of the 'short_name' value is 64", record.getMessage())

    def test_invalid_pmid_values(self):
        """Test to check whether the validator throws an error for invalid PMID values in meta_study.txt."""
        self.logger.setLevel(logging.ERROR)
        validateData.process_metadata_files(
            'test_data/meta_study/invalid_pmid_values',
            PORTAL_INSTANCE,
            self.logger, False, False)

        # expecting three errors about invalid PMID
        record_list = self.get_log_records()
        self.assertEqual(len(record_list), 3)
        record = record_list.pop()
        self.assertEqual('The PMID field in meta_study should be a comma separated list of integers', record.getMessage())
        self.assertEqual('29617662 29625055', record.cause)
        record = record_list.pop()
        self.assertEqual('The PMID field in meta_study should be a comma separated list of integers', record.getMessage())
        self.assertEqual('29622463A', record.cause)
        record = record_list.pop()
        self.assertEqual('The PMID field in meta_study should not contain any embedded whitespace', record.getMessage())
        self.assertEqual('29625048, 29596782, 29622463A, 29617662 29625055, 29625050', record.cause)

    def test_show_profile_setting_for_cna(self):
        """Test the `show_profile_in_analysis_tab: false` setting for continuous CNA data"""
        self.logger.setLevel(logging.ERROR)
        validateData.process_metadata_files(
            'test_data/meta_study/invalid_show_profile_setting_cna',
            PORTAL_INSTANCE,
            self.logger, False, False)
        record_list = self.get_log_records()
        # expecting 1 error:
        self.assertEqual(len(record_list), 1)
        
        # Should raise an error when show_profile_in_analysis_tab is not false for non-discrete CNA metafile
        record = record_list.pop()
        self.assertEqual("The 'show_profile_in_analysis_tab' setting must be 'false', as this is only applicable for CNA data of the DISCRETE type.", record.getMessage())
                
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

def _resetMultipleFileHandlerClassVars():
    """Reset the state of classes that check mulitple files of the same type.
    
    GsvaWiseFileValidator classes check 
    consistency between multiple data files by collecting information in class variables.
    This implementation is not consistent with the unit test environment that simulates
    different studies to be loaded. To ensure real-world fucntionality the class variables 
    should be reset before each unit test that tests multi file consistency."""

    for c in [ validateData.GsvaWiseFileValidator ]:
        c.prior_validated_sample_ids = None
        c.prior_validated_feature_ids = None
        c.prior_validated_header = None

# --------------------------- resources wise test ------------------------------

class ResourceDefinitionWiseTestCase(PostClinicalDataFileTestCase):

    def test_resource_definition_missing_resourceId(self):

        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_resource_definition_missing_resourceId.txt',
                            validateData.ResourceDefinitionValidator)

        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('Missing RESOURCE_ID', record.getMessage())

class ResourceWiseTestCase(PostClinicalDataFileTestCase):
    def test_resource_is_not_url(self):
        # set RESOURCE_DEFINITION_DICTIONARY (which should be initialized before validate resource data)
        validateData.RESOURCE_DEFINITION_DICTIONARY = {'PATHOLOGY_SLIDE': ['SAMPLE']}
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_resource_is_not_url.txt',
                            validateData.ResourceValidator)

        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('not an url', record.getMessage())
        # reset RESOURCE_DEFINITION_DICTIONARY
        validateData.RESOURCE_DEFINITION_DICTIONARY = {}

    # sample resources tests
    def test_sample_resource_should_have_definition(self):
        # reset RESOURCE_DEFINITION_DICTIONARY
        validateData.RESOURCE_DEFINITION_DICTIONARY = {}
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_resource_sample_valid.txt',
                            validateData.SampleResourceValidator)

        self.assertEqual(len(record_list), 6)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('sample resource is not defined correctly', record.getMessage())

    def test_sample_resource_has_definition_in_different_type(self):
        # set RESOURCE_DEFINITION_DICTIONARY (which should be initialized before validate resource data)
        # PATHOLOGY_SLIDE is duplicated in SAMPLE and PATIENT
        validateData.RESOURCE_DEFINITION_DICTIONARY = {'PATHOLOGY_SLIDE': ['SAMPLE', 'PATIENT']}
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_resource_sample_valid.txt',
                            validateData.SampleResourceValidator)

        self.assertEqual(len(record_list), 3)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertIn('sample resource has been used by more than one RESOURCE_TYPE', record.getMessage())

    def test_sample_resource_has_duplication(self):
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_resource_sample_duplicate.txt',
                            validateData.SampleResourceValidator)

        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('Duplicated resources found', record.getMessage())

    # patient resources tests
    def test_patient_resource_should_have_definition(self):
        # reset global variables
        validateData.RESOURCE_DEFINITION_DICTIONARY = {}
        validateData.RESOURCE_PATIENTS_WITH_SAMPLES = set(["TCGA-A2-A04P", "TCGA-A1-A0SK", "TCGA-A2-A0CM"])
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_resource_patient_valid.txt',
                            validateData.PatientResourceValidator)

        self.assertEqual(len(record_list), 6)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('patient resource is not defined correctly', record.getMessage())

        # reset global variables
        validateData.RESOURCE_PATIENTS_WITH_SAMPLES = None

    def test_patient_resource_has_definition_in_different_type(self):
        # set RESOURCE_DEFINITION_DICTIONARY (which should be initialized before validate resource data)
        # PATHOLOGY_SLIDE is duplicated in SAMPLE and PATIENT
        validateData.RESOURCE_DEFINITION_DICTIONARY = {'PATIENT_NOTES': ['SAMPLE', 'PATIENT']}
        validateData.RESOURCE_PATIENTS_WITH_SAMPLES = set(["TCGA-A2-A04P", "TCGA-A1-A0SK", "TCGA-A2-A0CM"])
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_resource_patient_valid.txt',
                            validateData.PatientResourceValidator)

        self.assertEqual(len(record_list), 3)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertIn('patient resource has been used by more than one RESOURCE_TYPE', record.getMessage())

        # reset global variables
        validateData.RESOURCE_PATIENTS_WITH_SAMPLES = None

    def test_patient_resource_has_duplication(self):
        validateData.RESOURCE_PATIENTS_WITH_SAMPLES = set(["TCGA-A2-A04P", "TCGA-A1-A0SK", "TCGA-A2-A0CM"])
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_resource_patient_duplicate.txt',
                            validateData.PatientResourceValidator)

        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('Duplicated resources found', record.getMessage())

        # reset global variables
        validateData.RESOURCE_PATIENTS_WITH_SAMPLES = None

    # study resources tests
    def test_study_resource_should_have_definition(self):
        # reset global variables
        validateData.RESOURCE_DEFINITION_DICTIONARY = {}
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_resource_study_valid.txt',
                            validateData.StudyResourceValidator)

        self.assertEqual(len(record_list), 4)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('study resource is not defined correctly', record.getMessage())

    def test_study_resource_has_definition_in_different_type(self):
        # set RESOURCE_DEFINITION_DICTIONARY (which should be initialized before validate resource data)
        # PATHOLOGY_SLIDE is duplicated in SAMPLE and STUDY
        validateData.RESOURCE_DEFINITION_DICTIONARY = {'STUDY_SPONSORS': ['STUDY', 'SAMPLE']}
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_resource_study_valid.txt',
                            validateData.StudyResourceValidator)

        self.assertEqual(len(record_list), 2)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertIn('study resource has been used by more than one RESOURCE_TYPE', record.getMessage())

    def test_study_resource_has_duplication(self):
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_resource_study_duplicate.txt',
                            validateData.StudyResourceValidator)

        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('Duplicated resources found', record.getMessage())
# -------------------------- end resource definition wise test ----------------------------

# --------------------------- generic assay test ------------------------------
class GenericAssayWiseTestCase(PostClinicalDataFileTestCase):
    def test_generic_assay_missing_entity_id_column(self):
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_generic_assay_without_entity_id_column.txt',
                            validateData.GenericAssayWiseFileValidator,
                            extra_meta_fields={
                                'generic_entity_meta_properties': 'name,description,url'})

        self.assertEqual(len(record_list), 2)
        record_iterator = iter(record_list)
        # Invalid column header
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('Missing column: ENTITY_STABLE_ID', record.getMessage())
        # Missing column: ENTITY_STABLE_ID
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('Invalid column header', record.getMessage())

class GenericAssayContinuousTestCase(PostClinicalDataFileTestCase):
    def test_generic_assay_with_valid_continuous_data(self):
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_generic_assay_valid_continuous.txt',
                            validateData.GenericAssayContinuousValidator,
                            extra_meta_fields={
                                'generic_entity_meta_properties': 'name,description,url'})

        self.assertEqual(len(record_list), 0)

    def test_generic_assay_with_non_numerical_data(self):
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_generic_assay_with_non_numerical_data.txt',
                            validateData.GenericAssayContinuousValidator,
                            extra_meta_fields={
                                'generic_entity_meta_properties': 'name,description,url'})

        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn(record.cause, 'NON_NUMERIC')

class GenericAssayCategoricalTestCase(PostClinicalDataFileTestCase):
    def test_generic_assay_with_valid_categorical_data(self):
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_generic_assay_valid_categorical.txt',
                            validateData.GenericAssayCategoricalValidator,
                            extra_meta_fields={
                                'generic_entity_meta_properties': 'name,description,url'})

        self.assertEqual(len(record_list), 0)

    def test_generic_assay_with_enpty_cell_data(self):
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_generic_assay_empty_cell.txt',
                            validateData.GenericAssayCategoricalValidator,
                            extra_meta_fields={
                                'generic_entity_meta_properties': 'name,description,url'})

        self.assertEqual(len(record_list), 2)
        record_iterator = iter(record_list)
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('Blank cell found in column', record.getMessage())
        record = next(record_iterator)
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('Cell is empty. A categorical value is expected.', record.getMessage())

class GenericAssayBinaryTestCase(PostClinicalDataFileTestCase):
    def test_generic_assay_with_with_valid_binary_data(self):
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_generic_assay_valid_binary.txt',
                            validateData.GenericAssayBinaryValidator,
                            extra_meta_fields={
                                'generic_entity_meta_properties': 'name,description,url'})

        self.assertEqual(len(record_list), 0)

    def test_generic_assay_with_not_defined_data(self):
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_generic_assay_with_not_defined_data.txt',
                            validateData.GenericAssayBinaryValidator,
                            extra_meta_fields={
                                'generic_entity_meta_properties': 'name,description,url'})

        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn(record.cause, 'NOT_DEFINED')
# -------------------------- end generic assay test ----------------------------

class CNADiscretePDAAnnotationsValidatorTestCase(PostClinicalDataFileTestCase):

    def test_required_gene_columns(self):

        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_pd_annotation_missing_col_gene_ids.txt',
                                    validateData.CNADiscretePDAAnnotationsValidator)

        self.assertEqual(len(record_list), 2)
        record = record_list[0]
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('Hugo_Symbol or Entrez_Gene_Id column needs to be present in the file.', record.getMessage())

    def test_required_sample_columns(self):

        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_pd_annotation_missing_col_sampleid.txt',
                                    validateData.CNADiscretePDAAnnotationsValidator)

        self.assertEqual(len(record_list), 2)
        record = record_list[0]
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('Missing column: SAMPLE_ID', record.getMessage())

    def test_required_driver_columns(self):

        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_pd_annotation_missing_col_driver.txt',
                                    validateData.CNADiscretePDAAnnotationsValidator)

        self.assertEqual(len(record_list), 2)
        record = record_list[0]
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('Column cbp_driver_annotation found without any cbp_driver column.', record.getMessage())

    def test_required_field_permutations(self):

        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_pd_annotation_missing_fields.txt',
                                    validateData.CNADiscretePDAAnnotationsValidator)

        self.assertEqual(len(record_list), 3)
        record = record_list[0]
        self.assertIn('Only "Putative_Passenger", "Putative_Driver", "NA", "Unknown" and "" (empty) are allowed.', record.getMessage())
        self.assertEqual(record.levelno, logging.ERROR)
        record = record_list[1]
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('No Entrez gene id or gene symbol provided for gene.', record.getMessage())
        record = record_list[2]
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('This line has no value for cbp_driver_tiers and a value for cbp_driver_tiers_annotation. Please, fill the cbp_driver_tiers column.', record.getMessage())

if __name__ == '__main__':
    unittest.main(buffer=True)
