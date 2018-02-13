#!/usr/bin/env python2.7

"""
Copyright (c) 2018 The Hyve B.V.
This code is licensed under the GNU Affero General Public License (AGPL),
version 3, or (at your option) any later version.
"""

import unittest
from importer import validateStudies


# globals:
PORTAL_INFO_DIR = 'test_data/api_json_system_tests'

class ValidateStudiesSystemTester(unittest.TestCase):
    """Test cases around running the validateStudies script

    (such as "does it return the correct exit status?")
    """

    def test_exit_status_success(self):
        """study 0 : no errors, expected exit_status = 0.

        Possible exit statuses:
                0: 'VALID',
                1: 'INVALID'
        """

        # Build up arguments and run
        print "===study 0"
        args = ['--list-of-studies', 'test_data/study_es_0/',
                '--portal_info_dir', PORTAL_INFO_DIR]
        args = validateStudies.interface(args)
        exit_status = validateStudies.main(args)
        self.assertEquals(0, exit_status)

    def test_exit_status_failure(self):
        """study 1 : errors, expected exit_status = 1."""

        # Build up arguments and run
        print "===study 1"
        args = ['--list-of-studies', 'test_data/study_es_1/',
                '--portal_info_dir', PORTAL_INFO_DIR]
        args = validateStudies.interface(args)
        exit_status = validateStudies.main(args)
        self.assertEquals(1, exit_status)

    def test_exit_status_invalid(self):
        """test to fail: study directory not existing, so cannot run validation, expected exit_status = 1."""

        # Build up arguments and run
        print "===study invalid"
        args = ['--list-of-studies', 'test_data/study_es_invalid/',
                '--portal_info_dir', PORTAL_INFO_DIR]
        args = validateStudies.interface(args)
        exit_status = validateStudies.main(args)
        self.assertEquals(1, exit_status)

    def test_exit_status_warnings(self):
        """study 3 : warnings only, expected exit_status = 0."""

        # Build up arguments and run
        print "===study 3"
        args = ['--list-of-studies','test_data/study_es_3/',
                '--portal_info_dir', PORTAL_INFO_DIR]
        args = validateStudies.interface(args)
        exit_status = validateStudies.main(args)
        self.assertEquals(0, exit_status)

    def test_exit_status_multiple_studies(self):
        """Running validateStudies for four studies tested above, expected exit_status = 1."""

        # Build up arguments and run
        print "===study0,1,invalid,3"
        args = ['--root-directory', 'test_data',
                '--list-of-studies', 'study_es_0,study_es_1,study_es_invalid,study_es_3',
                '--portal_info_dir', PORTAL_INFO_DIR]
        args = validateStudies.interface(args)
        exit_status = validateStudies.main(args)
        self.assertEquals(1, exit_status)


if __name__ == '__main__':
    unittest.main(buffer=True)
