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
