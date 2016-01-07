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

# Test cases around running the complete validateData script (such as "does it return the correct exit status?" 
# or "does it generate the html report when requested?", etc)
class ValidateDataSystemTester(unittest.TestCase):
    
        
    def test_exit_status(self):
        '''
        If there are errors, the script should return 
                0: 'succeeded',
                1: 'failed',
                2: 'not performed as problems occurred',
                3: 'succeeded with warnings'}.get(exit_status, 'unknown')))
        '''
        
        # study 1 : no errors, expected exit_status = 0
        
        # study 2 : errors, expected exit_status = 1
        
        # test to fail: give wrong hugo file, or let a meta file point to a non-existing data file, expected exit_status = 2
        
        # study 3 : warnings only, expected exit_status = 3
        
        
        
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
        
    