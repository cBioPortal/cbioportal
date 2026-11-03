#!/usr/bin/env python3

#
# Copyright (c) 2016 The Hyve B.V.
# Modifications copyright (c) 2025 SE4BIO.
#
# SPDX-License-Identifier: Apache-2.0
#

import unittest
import os
import difflib
import subprocess
import sys
from pathlib import Path

# globals:
PROJECT_ROOT = Path(__file__).resolve().parents[1]
TESTS_DIR = PROJECT_ROOT / 'tests'
TEST_DATA_DIR = TESTS_DIR / 'test_data'
PORTAL_INFO_DIR = TEST_DATA_DIR / 'api_json_system_tests'
SCRIPTS_DIR = PROJECT_ROOT / 'scripts'


class ValidateDataSystemTester(unittest.TestCase):
    '''Test cases around running the complete validateData script

    (such as "does it return the correct exit status?" or "does it generate
    the html report when requested?", etc)
    '''

    def relpath(self, path):
        """Convert an absolute path under tests/ to a relative path for CLI output."""
        return os.path.relpath(path, TESTS_DIR)

    def relpath_dir(self, path):
        """Relative path formatted with a trailing separator, matching legacy tests."""
        rel = self.relpath(path)
        if not rel.endswith(os.sep):
            rel += os.sep
        return rel

    def run_validator(self, args):
        """Run validateData via subprocess with provided arguments."""
        env = os.environ.copy()
        scripts_path = str(SCRIPTS_DIR)
        env['PYTHONPATH'] = scripts_path + os.pathsep + env.get('PYTHONPATH', '')
        cmd = [sys.executable, '-m', 'importer.validateData'] + args
        print("validateData args:", args)
        completed = subprocess.run(
            cmd,
            cwd=TESTS_DIR,
            env=env,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True)
        print("validateData stdout:", completed.stdout)
        print("validateData stderr:", completed.stderr)
        return completed.returncode

    def assertFileGenerated(self, tmp_file_name, expected_file_name):
        """Assert that a file has been generated with the expected contents."""
        tmp_file = Path(tmp_file_name)
        expected_file = Path(expected_file_name)
        self.assertTrue(tmp_file.exists())
        with tmp_file.open('r') as out_file, expected_file.open('r') as ref_file:
            base_filename = tmp_file.name
            diff_result = difflib.context_diff(
                    ref_file.readlines(),
                    out_file.readlines(),
                    fromfile='Expected {}'.format(base_filename),
                    tofile='Generated {}'.format(base_filename))
        diff_line_list = list(diff_result)
        self.assertEqual(diff_line_list, [],
                         msg='\n' + ''.join(diff_line_list))
        # remove temp file if all is fine:
        tmp_file.unlink()

    def test_exit_status_success(self):
        '''study 0 : no errors, expected exit_status = 0.

        If there are errors, the script should return
                0: 'succeeded',
                1: 'failed',
                2: 'not performed as problems occurred',
                3: 'succeeded with warnings'
        '''

        # build up the argument list
        args = ['--study_directory', self.relpath_dir(TEST_DATA_DIR / 'study_es_0'),
                '--portal_info_dir', self.relpath(PORTAL_INFO_DIR), '-v']
        exit_status = self.run_validator(args)
        self.assertEqual(0, exit_status)

    def test_exit_status_failure(self):
        '''study 1 : errors, expected exit_status = 1.'''
        #Build up arguments and run
        args = ['--study_directory', self.relpath_dir(TEST_DATA_DIR / 'study_es_1'),
                '--portal_info_dir', self.relpath(PORTAL_INFO_DIR), '-v']
        exit_status = self.run_validator(args)
        self.assertEqual(1, exit_status)

    def test_exit_status_invalid(self):
        '''test to fail: give wrong hugo file, or let a meta file point to a non-existing data file, expected exit_status = 2.'''
        #Build up arguments and run
        args = ['--study_directory', self.relpath_dir(TEST_DATA_DIR / 'study_es_invalid'),
                '--portal_info_dir', self.relpath(PORTAL_INFO_DIR), '-v']
        exit_status = self.run_validator(args)
        self.assertEqual(2, exit_status)

    def test_exit_status_warnings(self):
        '''study 3 : warnings only, expected exit_status = 3.'''
        # data_filename: test
        #Build up arguments and run
        args = ['--study_directory', self.relpath_dir(TEST_DATA_DIR / 'study_es_3'),
                '--portal_info_dir', self.relpath(PORTAL_INFO_DIR), '-v']
        exit_status = self.run_validator(args)
        self.assertEqual(3, exit_status)

    def test_html_output(self):
        '''
        Test if html file is correctly generated when 'html_table' is given
        '''
        #Build up arguments and run
        out_file_name = TEST_DATA_DIR / 'study_es_0' / 'result_report.html~'
        args = ['--study_directory', self.relpath_dir(TEST_DATA_DIR / 'study_es_0'),
                '--portal_info_dir', self.relpath(PORTAL_INFO_DIR), '-v',
                '--html_table', self.relpath(out_file_name)]
        exit_status = self.run_validator(args)
        self.assertEqual(0, exit_status)
        self.assertFileGenerated(out_file_name,
                                 TEST_DATA_DIR / 'study_es_0' / 'result_report.html')

    def test_portal_mismatch(self):
        '''Test if validation fails when data contradicts the portal.'''
        # build up arguments and run
        argv = ['--study_directory', self.relpath_dir(TEST_DATA_DIR / 'study_portal_mismatch'),
                '--portal_info_dir', self.relpath(PORTAL_INFO_DIR), '--verbose']
        exit_status = self.run_validator(argv)
        self.assertEqual(exit_status, 1)

    def test_no_portal_checks(self):
        '''Test if validation skips portal-specific checks when instructed.'''
        argv = ['--study_directory', self.relpath_dir(TEST_DATA_DIR / 'study_portal_mismatch'),
                '--verbose',
                '--no_portal_checks']
        exit_status = self.run_validator(argv)
        self.assertEqual(exit_status, 3)

    def test_problem_in_clinical(self):
        '''Test whether the script aborts if the sample file cannot be parsed.

        Further files cannot be validated in this case, as all sample IDs will
        be undefined. Validate if the script is giving the proper error.
        '''
        out_file_name = TEST_DATA_DIR / 'study_wr_clin' / 'result_report.html~'
        args = ['--study_directory', self.relpath_dir(TEST_DATA_DIR / 'study_wr_clin'),
                '--portal_info_dir', self.relpath(PORTAL_INFO_DIR), '-v',
                '--html_table', self.relpath(out_file_name)]
        exit_status = self.run_validator(args)
        self.assertEqual(1, exit_status)
        self.assertFileGenerated(out_file_name,
                                 TEST_DATA_DIR / 'study_wr_clin' / 'result_report.html')

    def test_various_issues(self):
        '''Test if output is generated for a mix of errors and warnings.

        This includes HTML ouput, the error line file and the exit status.
        '''
        html_file_name = TEST_DATA_DIR / 'study_various_issues' / 'result_report.html~'
        error_file_name = TEST_DATA_DIR / 'study_various_issues' / 'error_file.txt~'
        args = ['--study_directory', self.relpath_dir(TEST_DATA_DIR / 'study_various_issues'),
                '--portal_info_dir', self.relpath(PORTAL_INFO_DIR), '-v',
                '--html_table', self.relpath(html_file_name),
                '--error_file', self.relpath(error_file_name)]
        exit_status = self.run_validator(args)
        self.assertEqual(1, exit_status)
        self.assertFileGenerated(
                html_file_name,
                TEST_DATA_DIR / 'study_various_issues' / 'result_report.html')
        self.assertFileGenerated(
                error_file_name,
                TEST_DATA_DIR / 'study_various_issues' / 'error_file.txt')

    def test_files_with_quotes(self):
        '''
        Tests the scenario where data files contain quotes. This should give errors.
        '''
        out_file_name = TEST_DATA_DIR / 'study_quotes' / 'result_report.html~'
        args = ['--study_directory', self.relpath_dir(TEST_DATA_DIR / 'study_quotes'),
                '--portal_info_dir', self.relpath(PORTAL_INFO_DIR), '-v',
                '--html_table', self.relpath(out_file_name)]
        exit_status = self.run_validator(args)
        self.assertEqual(1, exit_status)
        self.assertFileGenerated(out_file_name,
                                 TEST_DATA_DIR / 'study_quotes' / 'result_report.html')

    def test_incremental_upload(self):
        '''
        Test happy path for the incremental upload
        '''
        args = ['--data_directory', self.relpath_dir(TEST_DATA_DIR / 'study_es_0_inc'),
                '--portal_info_dir', self.relpath(PORTAL_INFO_DIR), '-v']
        exit_status = self.run_validator(args)
        self.assertEqual(0, exit_status)

if __name__ == '__main__':
    unittest.main(buffer=True)
