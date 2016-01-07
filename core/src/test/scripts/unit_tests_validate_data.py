'''
Copyright (c) 2016 The Hyve B.V.
This code is licensed under the GNU Affero General Public License (AGPL),
version 3, or (at your option) any later version.
'''

from pkg_resources import resource_filename  # @UnresolvedImport # pylint: disable=E0611
import unittest
import logging
import sys
from hugoEntrezMap import parse_ncbi_file
from validateData import SegValidator, LogfileStyleFormatter, ClinicalValidator
from uuid import uuid4


# globals:
hugo_mapping_file = 'Homo_sapiens.gene_info.gz'
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
        meta_dict = {
                    'data_file_path': 'data_seg_wrong_order.txt',
                    'cancer_study_identifier': 'test_column_order_validation',
                    'genetic_alteration_type': 'SEGMENT',
                    'datatype': 'SEGMENT',
                    'stable_id': 'test_column_order_validation_segment',
                    'show_profile_in_analysis_tab': 'false',
                    'profile_description': 'test_column_order_validation',
                    'profile_name': 'test_column_order_validation',
                    'reference_genome_id': 'hg19',
                    'description': 'Segment data, wrong order, testing the validator'        
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
            self.assertEqual("checkOrderedRequiredColumns", error.funcName)
        
    def test_column_order_validation_ClinicalValidator(self):
        '''
        ClinicalValidator does NOT need its columns in a specific order. 
        Here we serve files with different order and no errors or warnings
        '''
        meta_dict = {
                    'data_file_path': 'data_clin_order1.txt',
                    'cancer_study_identifier': 'test_column_order_validation',
                    'genetic_alteration_type': 'CLINICAL',
                    'datatype': ';:FREE-FORM',
                    'stable_id': 'test_column_order_validation_segment',
                    'show_profile_in_analysis_tab': 'false',
                    'profile_description': 'test_column_order_validation',
                    'profile_name': 'test_column_order_validation',
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
        
    