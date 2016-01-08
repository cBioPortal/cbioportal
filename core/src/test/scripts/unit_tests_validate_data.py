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
        logger.setLevel(logging.ERROR)
        validator = SegValidator(hugo_entrez_map,logger,meta_dict)
        validator.validate()
        # we expect 2 errors about columns in wrong order:
        self.assertEqual(2, len(logger.handlers[0].buffer))
        # check if both messages come from checkOrderedRequiredColumns: 
        for error in logger.handlers[0].buffer:
            self.assertEqual("ERROR", error.levelname)
            self.assertEqual("_checkOrderedRequiredColumns", error.funcName)

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


class LogBufferTestCase(unittest.TestCase):

    def setUp(self):
        self.logger = logging.getLogger(self.__class__.__name__)
        self.logger.setLevel(logging.INFO)
        handler = logging.handlers.BufferingHandler(capacity=1e6)
        self.logger.handlers = [handler]

    def tearDown(self):
        self.logger.handlers = []

    def get_log_records(self):
        '''Get the log records written to the logger since the last call.'''
        recs = self.logger.handlers[0].buffer
        self.logger.handlers[0].flush()
        return recs

    @staticmethod
    def print_log_records(record_list):
        '''Pretty-print a list of log records to standard output.'''
        formatter = validateData.LogfileStyleFormatter()
        for record in record_list:
            print formatter.format(record)


class DataFileTestCase(LogBufferTestCase):

    def setUp(self):
        super(DataFileTestCase, self).setUp()
        self.orig_study_dir = validateData.STUDY_DIR
        validateData.STUDY_DIR = 'test_data'

    def tearDown(self):
        super(DataFileTestCase, self).tearDown()
        validateData.STUDY_DIR = self.orig_study_dir

    def validate(self, data_filename, ValidatorClass, extra_meta_fields=None):
        meta_dict = {'data_file_path': data_filename}
        if extra_meta_fields is not None:
            meta_dict.update(extra_meta_fields)
        validator = ValidatorClass(hugo_entrez_map, self.logger, meta_dict)
        validator.validate()
        return self.get_log_records()


class ClinicalColumnDefsTestCase(DataFileTestCase):

    '''Tests for validations of the column definitions in a clinical file.'''

    def test_correct_definitions(self):
        record_list = self.validate('data_clin_coldefs_correct.txt',
                                    validateData.ClinicalValidator)
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.INFO)

    def test_wrong_displayname(self):
        record_list = self.validate('data_clin_coldefs_wrong_display_name.txt',
                                    validateData.ClinicalValidator)
        self.assertEqual(len(record_list), 3)
        self.assertEqual(record_list[1].levelno, logging.ERROR)
        self.assertEqual(record_list[1].column_number, 4)
        self.assertEqual(record_list[1].line_number, 1)

    def test_unknown_attribute(self):
        record_list = self.validate('data_clin_coldefs_unknown_attribute.txt',
                                    validateData.ClinicalValidator)
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.INFO)

    def test_invalid_priority(self):
        record_list = self.validate('data_clin_coldefs_invalid_priority.txt',
                                    validateData.ClinicalValidator)
        self.assertEqual(len(record_list), 3)
        self.assertEqual(record_list[1].levelno, logging.ERROR)
        self.assertEqual(record_list[1].column_number, 4)
        self.assertEqual(record_list[1].line_number, 5)
