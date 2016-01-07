'''
Copyright (c) 2016 The Hyve B.V.
This code is licensed under the GNU Affero General Public License (AGPL),
version 3, or (at your option) any later version.
'''

import unittest
from import_data_validator import validateData

# globals:
server = 'http://localhost:8080/cbioportal'

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
        
        # ======= study 1 : no errors, expected exit_status = 0
        
        #Build up arguments and run
        args = ['--study_directory','test_data/study_es_1/', 
                    '--url_server', server, '-v'] # -q instead ??
        args = validateData.interface(args)
        # Execute main function with arguments provided through sys.argv
        exit_status = validateData.main_validate(args)
        self.assertEquals(0, exit_status)
        
        #//PA - http://localhost:8080/cbioportal/api/genes TO validate genes
        
        
        # ======= study 2 : errors, expected exit_status = 1
        
        # ======= test to fail: give wrong hugo file, or let a meta file point to a non-existing data file, expected exit_status = 2
        
        # ======= study 3 : warnings only, expected exit_status = 3
        
        
