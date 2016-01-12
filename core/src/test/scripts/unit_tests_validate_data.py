'''
Copyright (c) 2016 The Hyve B.V.
This code is licensed under the GNU Affero General Public License (AGPL),
version 3, or (at your option) any later version.
'''

import unittest
import logging
import sys
from import_data_validator.hugoEntrezMap import parse_ncbi_file
from import_data_validator import validateData
from import_data_validator.validateData import SegValidator, LogfileStyleFormatter, ClinicalValidator
from uuid import uuid4


# globals:
hugo_mapping_file = 'test_data/Homo_sapiens.gene_info.gz'
ncbi_file = open(hugo_mapping_file)
hugo_entrez_map = parse_ncbi_file(ncbi_file)
# hard-code known clinical attributes
KNOWN_PATIENT_ATTRS = {
    "PATIENT_ID": {"display_name":"Patient Identifier","description":"Identifier to uniquely specify a patient.","datatype":"STRING","is_patient_attribute":"1","priority":"1"},
    "OS_STATUS": {"display_name":"Overall Survival Status","description":"Overall patient survival status.","datatype":"STRING","is_patient_attribute":"1","priority":"1"},
    "OS_MONTHS": {"display_name":"Overall Survival (Months)","description":"Overall survival in months since initial diagnosis.","datatype":"NUMBER","is_patient_attribute":"1","priority":"1"},
    "DFS_STATUS": {"display_name":"Disease Free Status","description":"Disease free status since initial treatment.","datatype":"STRING","is_patient_attribute":"1","priority":"1"},
    "DFS_MONTHS": {"display_name":"Disease Free (Months)","description":"Disease free (months) since initial treatment.","datatype":"NUMBER","is_patient_attribute":"1","priority":"1"}
}
KNOWN_SAMPLE_ATTRS = {
    "SAMPLE_ID": {"display_name":"Sample Identifier","description":"A unique sample identifier.","datatype":"STRING","is_patient_attribute":"0","priority":"1"},
}


def getNewLogger():
    # set default message handler
    text_handler = logging.StreamHandler(sys.stdout)
    # use the formatter also used in the validation code:
    text_handler.setFormatter(LogfileStyleFormatter())
    mem_handler = logging.handlers.MemoryHandler(
        capacity=1e6,
        flushLevel=logging.CRITICAL,
        target=text_handler)
    # get new logger
    logger = logging.getLogger(uuid4().get_urn())
    logger.addHandler(mem_handler)
    logger.handlers[0].flush()
    return logger


# Test cases for the various Validator classes found in validateData script


class ValidateDataTester(unittest.TestCase):

    def test_column_order_validation_SegValidator(self):
        '''
        seg validator needs its columns in a specific order.
        Here we serve a file with wrong order and expect validator to log this:
        '''
        validateData.STUDY_DIR = "test_data"
        meta_dict = {
                    'data_file_path': 'data_seg_wrong_order.txt',
                    'stable_id': 'test_column_order_validation_segment',
                    }

        # set level according to this test case:
        logger = getNewLogger()
        try:
            logger.setLevel(logging.ERROR)
            validator = SegValidator(hugo_entrez_map,logger,meta_dict)
            validator.validate()
            # we expect 2 errors about columns in wrong order:
            self.assertEqual(2, len(logger.handlers[0].buffer))
            # check if both messages come from checkOrderedRequiredColumns:
            for error in logger.handlers[0].buffer:
                self.assertEqual("ERROR", error.levelname)
                self.assertEqual("_checkOrderedRequiredColumns", error.funcName)
        finally:
            logger.handlers = []

    def test_column_order_validation_ClinicalValidator(self):
        '''
        ClinicalValidator does NOT need its columns in a specific order.
        Here we serve files with different order and no errors or warnings
        '''
        validateData.STUDY_DIR = "test_data"
        meta_dict = {
                    'data_file_path': 'data_clin_order1.txt',
                    'stable_id': 'test_column_order_validation_segment',
                    }

        # set level according to this test case:
        logger = getNewLogger()
        try:
            logger.setLevel(logging.WARNING)
            validator = ClinicalValidator(hugo_entrez_map,logger,meta_dict)
            validator.validate()
            # we expect no errors or warnings
            self.assertEqual(0, len(logger.handlers[0].buffer))
            # if the file has another order, this is also OK:
            meta_dict['data_file_path'] = 'data_clin_order2.txt'
            validator = ClinicalValidator(hugo_entrez_map,logger,meta_dict)
            validator.validate()
            # again, we expect no errors or warnings
            self.assertEqual(0, len(logger.handlers[0].buffer))
        finally:
            logger.handlers = []


class LogBufferTestCase(unittest.TestCase):

    '''Superclass for testcases that want to capture log records emitted.

    Defines a self.logger to log to, and a method get_log_records() to
    collect the list of LogRecords emitted by this logger. In addition,
    defines a function print_log_records to format a list of LogRecords
    to standard output.
    '''

    def setUp(self):
        '''Set up a logger with a buffering handler.'''
        self.logger = logging.getLogger(self.__class__.__name__)
        self.logger.setLevel(logging.INFO)
        handler = logging.handlers.BufferingHandler(capacity=1e6)
        self.orig_handlers = self.logger.handlers
        self.logger.handlers = [handler]

    def tearDown(self):
        '''Remove the logger handler (and any buffer it may have).'''
        self.logger.handlers = self.orig_handlers

    def get_log_records(self):
        '''Get the log records written to the logger since the last call.'''
        recs = self.logger.handlers[0].buffer
        self.logger.handlers[0].flush()
        return recs

    @staticmethod
    def print_log_records(record_list):
        '''Pretty-print a list of log records to standard output.

        This can be used if, while writing unit tests, you want to see
        what the messages currently are. The final unit tests committed
        to version control should not print log messages.
        '''
        formatter = validateData.LogfileStyleFormatter()
        for record in record_list:
            print formatter.format(record)


class DataFileTestCase(LogBufferTestCase):

    '''Superclass for testcases validating a particular data file.

    Provides a validate() method to validate the data file with a
    particular validator class and collect the log records emitted.
    '''

    def setUp(self):
        '''Set up for validating a file in the test_data directory.'''
        super(DataFileTestCase, self).setUp()
        self.orig_study_dir = validateData.STUDY_DIR
        validateData.STUDY_DIR = 'test_data'
        # hard-code known clinical attributes instead of contacting a portal
        self.orig_srv_attrs = validateData.ClinicalValidator.srv_attrs
        mock_srv_attrs = dict(KNOWN_PATIENT_ATTRS)
        mock_srv_attrs.update(KNOWN_SAMPLE_ATTRS)
        validateData.ClinicalValidator.srv_attrs = mock_srv_attrs

    def tearDown(self):
        '''Restore the environment to before setUp() was called.'''
        validateData.STUDY_DIR = self.orig_study_dir
        validateData.ClinicalValidator.srv_attrs = self.orig_srv_attrs
        super(DataFileTestCase, self).tearDown()

    def validate(self, data_filename, validator_class, extra_meta_fields=None):
        '''Validate a file with a Validator and return the log records.'''
        meta_dict = {'data_file_path': data_filename}
        if extra_meta_fields is not None:
            meta_dict.update(extra_meta_fields)
        validator = validator_class(hugo_entrez_map, self.logger, meta_dict)
        validator.validate()
        return self.get_log_records()


class ClinicalColumnDefsTestCase(DataFileTestCase):

    '''Tests for validations of the column definitions in a clinical file.'''

    def test_correct_definitions(self):
        '''Test when all record definitions match with portal.'''
        record_list = self.validate('data_clin_coldefs_correct.txt',
                                    validateData.ClinicalValidator)
        # expecting two info messages: at start and end of file
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.INFO)

    def test_wrong_definitions(self):
        '''Test when record definitions do not match with portal.'''
        record_list = self.validate('data_clin_coldefs_wrong_display_name.txt',
                                    validateData.ClinicalValidator)
        # expecting two info messages with an error in between
        self.assertEqual(len(record_list), 3)
        # error about the display name of OS_MONTHS
        self.assertEqual(record_list[1].levelno, logging.ERROR)
        self.assertEqual(record_list[1].column_number, 3)
        self.assertIn('display_name', record_list[1].getMessage())

    def test_unknown_attribute(self):
        '''Test when a new attribute is defined in the data file.'''
        record_list = self.validate('data_clin_coldefs_unknown_attribute.txt',
                                    validateData.ClinicalValidator)
        # expecting two info messages with a warning in between
        self.assertEqual(len(record_list), 3)
        self.assertEqual(record_list[1].levelno, logging.WARNING)
        self.assertEqual(record_list[1].column_number, 7)
        self.assertIn('will be added', record_list[1].getMessage().lower())

    def test_invalid_definitions(self):
        '''Test when new attributes are defined with invalid properties.'''
        record_list = self.validate('data_clin_coldefs_invalid_priority.txt',
                                    validateData.ClinicalValidator)
        # expecting two info messages with an error in between
        self.assertEqual(len(record_list), 3)
        # error about the non-numeric priority of the SAUSAGE column
        self.assertEqual(record_list[1].levelno, logging.ERROR)
        self.assertEqual(record_list[1].line_number, 5)
        self.assertEqual(record_list[1].column_number, 7)
