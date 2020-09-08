#!/usr/bin/env python3

"""
Copyright (c) 2018 The Hyve B.V.
This code is licensed under the GNU Affero General Public License (AGPL),
version 3, or (at your option) any later version.
"""

import unittest
import sys
import os
import glob
from contextlib import contextmanager
from io import StringIO
import logging.handlers
import tempfile
import shutil

from importer import validateStudies, cbioportal_common


# globals:
PORTAL_INFO_DIR = 'test_data/api_json_system_tests'


# FIXME: replace by contextlib.redirect_stdout when moving to Python 3.4+
@contextmanager
def redirect_stdout(new_target):
    """Temporarily re-bind sys.stdout to a different file-like object."""
    old_target = sys.stdout
    sys.stdout = new_target
    try:
        yield
    finally:
        sys.stdout = old_target


# FIXME: replace by tempfile.TemporaryDirectory when moving to Python 3.2+
@contextmanager
def TemporaryDirectory():
    """Create a temporary directory and remove it after use."""
    path = tempfile.mkdtemp()
    try:
        yield path
    finally:
        shutil.rmtree(path)


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
        print("===study 0")
        args = ['--list-of-studies', 'test_data/study_es_0/',
                '--portal_info_dir', PORTAL_INFO_DIR]
        args = validateStudies.interface(args)
        exit_status = validateStudies.main(args)
        self.assertEqual(0, exit_status)

    def test_exit_status_failure(self):
        """study 1 : errors, expected exit_status = 1."""

        # Build up arguments and run
        print("===study 1")
        args = ['--list-of-studies', 'test_data/study_es_1/',
                '--portal_info_dir', PORTAL_INFO_DIR]
        args = validateStudies.interface(args)
        exit_status = validateStudies.main(args)
        self.assertEqual(1, exit_status)

    @unittest.skip("Study test_data/study_es_invalid is not implemented")
    def test_exit_status_invalid(self):
        """test to fail: study directory not existing, so cannot run validation, expected exit_status = 1."""

        # Build up arguments and run
        print("===study invalid")
        args = ['--list-of-studies', 'test_data/study_es_invalid/',
                '--portal_info_dir', PORTAL_INFO_DIR]
        args = validateStudies.interface(args)
        exit_status = validateStudies.main(args)
        self.assertEqual(1, exit_status)

    def test_exit_status_warnings(self):
        """study 3 : warnings only, expected exit_status = 0."""

        # Build up arguments and run
        print("===study 3")
        args = ['--list-of-studies', 'test_data/study_es_3/',
                '--portal_info_dir', PORTAL_INFO_DIR]
        args = validateStudies.interface(args)
        exit_status = validateStudies.main(args)
        self.assertEqual(0, exit_status)

    def test_exit_status_multiple_studies(self):
        """Running validateStudies for four studies tested above, expected exit_status = 1."""

        # Build up arguments and run
        print("===study0,1,invalid,3")
        args = ['--root-directory', 'test_data',
                '--list-of-studies', 'study_es_0,study_es_1,study_es_invalid,study_es_3',
                '--portal_info_dir', PORTAL_INFO_DIR]
        args = validateStudies.interface(args)
        exit_status = validateStudies.main(args)
        self.assertEqual(1, exit_status)

    def test_logs_study_label_before_validation_messages(self):
        """The log file should start with a line describing the study.

        A subsequent study should have its own header line.
        """
        # given
        with TemporaryDirectory() as out_dir_path:
            args = [
                '--root-directory', 'test_data',
                '--list-of-studies', 'study_various_issues,study_es_0',
                '--portal_info_dir', PORTAL_INFO_DIR,
                '--html-folder', out_dir_path
                ]
            # when
            with redirect_stdout(StringIO()):
                parsed_args = validateStudies.interface(args)
                validateStudies.main(parsed_args)
            # then
            log_file_path = glob.glob(os.path.join(out_dir_path, 'log*.txt'))[0]
            with open(log_file_path) as log_file:
                log_file_lines = log_file.readlines()
            self.assertIn('study_various_issues', log_file_lines[0])
            last_line_of_first_study = next(
                index
                for index, line
                in enumerate(log_file_lines)
                if 'Validation complete' in line)
            self.assertIn(
                'study_es_0',
                log_file_lines[last_line_of_first_study + 1])


class ValidateStudiesWithEagerlyFlushingCollapser(unittest.TestCase):
    """Test validation with the collapser flushing due to buffer capacity.

    When validating very large studies, it will flush partway through a study.
    This can be simulated with a smaller study by lowering the buffer capacity.
    """

    def setUp(self):
        """Make the collapsing log message handler flush more eagerly."""
        class EagerFlusher(logging.handlers.MemoryHandler):
            def __init__(self, *args, **kwargs):
                """Set the buffer capacity to 3 regardless of args."""
                # leave out any capacity argument from args and kwargs
                args = args[1:]
                kwargs = {k: v for k, v in list(kwargs.items()) if k != 'capacity'}
                # pass 3 as the capacity argument
                super(EagerFlusher, self).__init__(3, *args, **kwargs)
        class EagerFlushingCollapser(
                cbioportal_common.CollapsingLogMessageHandler,
                EagerFlusher):
            """CollapsingLogMessageHandler with EagerFlusher overrides."""
            pass
        self.original_collapser = cbioportal_common.CollapsingLogMessageHandler
        cbioportal_common.CollapsingLogMessageHandler = EagerFlusher

    def tearDown(self):
        """Restore the unmodified collapsing log message handler."""
        cbioportal_common.CollapsingLogMessageHandler = self.original_collapser

    def test_leaves_stdout_uncluttered_if_validation_produces_errors(self):
        """Test flushing the collapsing logger halfway through a study.

        This should not spill the validation messages to stdout as it previously
        did, even crashing with a KeyError sometimes because non-validator
        log messages got flushed into the collapsing logic.
        """
        output_stream = StringIO()
        with redirect_stdout(output_stream):
            args = validateStudies.interface([
                '--root-directory', 'test_data',
                '--list-of-studies', 'study_various_issues/',
                '--portal_info_dir', PORTAL_INFO_DIR])
            validateStudies.main(args)
        self.assertNotIn(
            'ERROR',
            output_stream.getvalue(),
            'The validation errors should not be printed to the console.')


if __name__ == '__main__':
    unittest.main(buffer=True)
