'''Integration tests for the GCMS project'''

from pkg_resources import resource_filename  # @UnresolvedImport # pylint: disable=E0611
import unittest
import logging
import sys
from hugoEntrezMap import parse_ncbi_file
from validateData import SegValidator, LogfileStyleFormatter

# globals:
hugo_mapping_file = 'Homo_sapiens.gene_info.gz'
ncbi_file = open(hugo_mapping_file)
hugo_entrez_map = parse_ncbi_file(ncbi_file)

def get_logger():
    # set default message handler
    text_handler = logging.StreamHandler(sys.stdout)
    # use the formatter also used in the validation code:
    text_handler.setFormatter(LogfileStyleFormatter())
    mem_handler = logging.handlers.MemoryHandler(
        capacity=1e6,
        flushLevel=logging.CRITICAL,
        target=text_handler)
    logger = logging.getLogger(__name__)
    logger.addHandler(mem_handler)
    return logger

# Test cases
class ValidateDataTester(unittest.TestCase):
    
        
    def test_column_order_validation(self):
        '''
        Some file types should have their columns in specific order and some are less strict. 
        This test checks if the validator catches this properly. 
        '''
        # seg validator needs its columns in specific order. 
        # Here we serve a file with wrong order and expect validator to log this:
        meta_dict = {
                    'data_file_path': 'seg_data_wrong_order.txt',
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
        logger = get_logger()
        logger.setLevel(logging.ERROR)
        seg_validator = SegValidator(hugo_entrez_map,logger,meta_dict)
        seg_validator.validate()
        # we expect 1 error about first column that is wrong:
        self.assertEqual(1, len(logger.handlers[0].buffer))
        
