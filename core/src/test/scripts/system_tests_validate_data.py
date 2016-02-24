#!/usr/bin/env python2.7

'''
Copyright (c) 2016 The Hyve B.V.
This code is licensed under the GNU Affero General Public License (AGPL),
version 3, or (at your option) any later version.
'''

import unittest
import logging
import tempfile
import os
import shutil
from importer import validateData

# globals:
PORTAL_INFO_DIR = 'test_data/api_json_system_tests'

# Test cases around running the complete validateData script (such as "does it return the correct exit status?" 
# or "does it generate the html report when requested?", etc)
class ValidateDataSystemTester(unittest.TestCase):

    def tearDown(self):
        """Close logging handlers after running validator."""
        # get the logger used in validateData.main_validate()
        validator_logger = logging.getLogger(validateData.__name__)
        # flush and close all handlers of this logger
        for logging_handler in validator_logger.handlers:
            logging_handler.close()
        # remove the handlers from the logger to reset it
        validator_logger.handlers = []
        super(ValidateDataSystemTester, self).tearDown()

    def test_exit_status_success(self):
        '''study 0 : no errors, expected exit_status = 0.

        If there are errors, the script should return 
                0: 'succeeded',
                1: 'failed',
                2: 'not performed as problems occurred',
                3: 'succeeded with warnings'
        '''


        #Build up arguments and run
        print "===study 0"
        args = ['--study_directory','test_data/study_es_0/', 
                '--portal_info_dir', PORTAL_INFO_DIR, '-v']
        args = validateData.interface(args)
        # Execute main function with arguments provided through sys.argv
        exit_status = validateData.main_validate(args)
        self.assertEquals(0, exit_status)

    def test_exit_status_failure(self):
        '''study 1 : errors, expected exit_status = 1.'''
        #Build up arguments and run
        print "===study 1"
        args = ['--study_directory','test_data/study_es_1/', 
                '--portal_info_dir', PORTAL_INFO_DIR, '-v']
        args = validateData.interface(args)
        # Execute main function with arguments provided through sys.argv
        exit_status = validateData.main_validate(args)
        self.assertEquals(1, exit_status)

    def test_exit_status_invalid(self):
        '''test to fail: give wrong hugo file, or let a meta file point to a non-existing data file, expected exit_status = 2.'''
        #Build up arguments and run
        print "===study invalid"
        args = ['--study_directory','test_data/study_es_invalid/', 
                '--portal_info_dir', PORTAL_INFO_DIR, '-v']
        args = validateData.interface(args)
        # Execute main function with arguments provided through sys.argv
        exit_status = validateData.main_validate(args)
        self.assertEquals(2, exit_status)

    def test_exit_status_warnings(self):
        '''study 3 : warnings only, expected exit_status = 3.'''
        # data_filename: test
        #Build up arguments and run
        print "===study 3"
        args = ['--study_directory','test_data/study_es_3/', 
                '--portal_info_dir', PORTAL_INFO_DIR, '-v']
        args = validateData.interface(args)
        # Execute main function with arguments provided through sys.argv
        exit_status = validateData.main_validate(args)
        self.assertEquals(3, exit_status)

    def test_html_output(self):
        '''
        Test if html file is correctly generated when 'html_table' is given
        '''
        #Build up arguments and run
        args = ['--study_directory','test_data/study_es_0/', 
                '--portal_info_dir', PORTAL_INFO_DIR, '-v',
                '--html_table', 'test_data/study_es_0/result_report.html']
        args = validateData.interface(args)
        # Execute main function with arguments provided through sys.argv
        exit_status = validateData.main_validate(args)
        # TODO - assert if html file is present
        self.assertEquals(0, exit_status)


    def test_errorline_output(self):
        '''Test if error file is generated when '--error_file' is given.'''
        temp_dir_path = tempfile.mkdtemp()
        try:
            out_file_name = os.path.join(temp_dir_path, 'error_file.txt')
            # build up arguments and run
            argv = ['--study_directory','test_data/study_maf_test/',
                    '--portal_info_dir', PORTAL_INFO_DIR,
                    '--error_file', out_file_name]
            parsed_args = validateData.interface(argv)
            exit_status = validateData.main_validate(parsed_args)
            # flush logging handlers used in validateData
            validator_logger = logging.getLogger(validateData.__name__)
            for logging_handler in validator_logger.handlers:
                logging_handler.flush()
            # assert that the results are as expected
            self.assertEquals(1, exit_status)
            self.assertTrue(os.path.exists(out_file_name))
            with open(out_file_name, 'rU') as out_file, \
                 open('test_data/study_maf_test/error_file.txt', 'rU') as ref_file:
                for ref_line in ref_file:
                    out_line = out_file.readline()
                    self.assertEquals(out_line, ref_line)
                self.assertEquals(out_file.readline(), '')
        finally:
            shutil.rmtree(temp_dir_path)

    def test_portal_mismatch(self):
        '''Test if validation fails when data contradicts the portal.'''
        # build up arguments and run
        argv = ['--study_directory', 'test_data/study_portal_mismatch',
                '--portal_info_dir', PORTAL_INFO_DIR, '--verbose']
        parsed_args = validateData.interface(argv)
        exit_status = validateData.main_validate(parsed_args)
        # flush logging handlers used in validateData
        validator_logger = logging.getLogger(validateData.__name__)
        for logging_handler in validator_logger.handlers:
            logging_handler.flush()
        # expecting only warnings (about the skipped checks), no errors
        self.assertEquals(exit_status, 1)

    def test_no_portal_checks(self):
        '''Test if validation skips portal-specific checks when instructed.'''
        # build up arguments and run
        argv = ['--study_directory', 'test_data/study_portal_mismatch',
                '--verbose',
                '--no_portal_checks']
        parsed_args = validateData.interface(argv)
        exit_status = validateData.main_validate(parsed_args)
        # flush logging handlers used in validateData
        validator_logger = logging.getLogger(validateData.__name__)
        for logging_handler in validator_logger.handlers:
            logging_handler.flush()
        # expecting only warnings (about the skipped checks), no errors
        self.assertEquals(exit_status, 3)

    def test_problem_in_clinical(self):
        '''
        When clinical file has a problem, we want the program to abort and give just this error 
        before validating other files (because other files cannot be validated in case clinical is wrong).
        Here we validate if script is giving proper error. 
        '''
        #Build up arguments and run
        print '==test_problem_in_clinical=='
        args = ['--study_directory','test_data/study_wr_clin/', 
                '--portal_info_dir', PORTAL_INFO_DIR, '-v',
                '--html_table', 'test_data/study_wr_clin/result_report.html']
        args = validateData.interface(args)
        # Execute main function with arguments provided through sys.argv
        exit_status = validateData.main_validate(args)
        self.assertEquals(1, exit_status)
        # TODO - set logger in main_validate and read out buffer here to assert on nr of errors

    def test_normal_samples_list_in_maf(self):
        '''
        For mutations MAF files there is a column called "Matched_Norm_Sample_Barcode". 
        In the respective meta file it is possible to give a list of sample codes against which this 
        column "Matched_Norm_Sample_Barcode" is validated. Here we test if this 
        validation works well.
        '''
        #Build up arguments and run
        print '==test_normal_samples_list_in_maf=='
        args = ['--study_directory','test_data/study_maf_test/', 
                '--portal_info_dir', PORTAL_INFO_DIR, '-v',
                '--html_table', 'test_data/study_maf_test/result_report.html']
        args = validateData.interface(args)
        # Execute main function with arguments provided through sys.argv
        exit_status = validateData.main_validate(args)
        # should fail because of errors with invalid Matched_Norm_Sample_Barcode values
        self.assertEquals(1, exit_status)


if __name__ == '__main__':
    unittest.main(buffer=True)
