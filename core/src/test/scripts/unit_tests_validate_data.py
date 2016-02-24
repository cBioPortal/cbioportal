#!/usr/bin/env python2.7

"""
Copyright (c) 2016 The Hyve B.V.
This code is licensed under the GNU Affero General Public License (AGPL),
version 3, or (at your option) any later version.
"""

import unittest
import sys
import logging.handlers
from importer import cbioportal_common
from importer import validateData


# globals for mock data used throughout the module
DEFINED_SAMPLE_IDS = None
PORTAL_INSTANCE = None


def setUpModule():
    """Initialise mock data used throughout the module."""
    global DEFINED_SAMPLE_IDS
    global PORTAL_INSTANCE
    # mock-code sample ids defined in a study
    DEFINED_SAMPLE_IDS = ["TCGA-A1-A0SB-01", "TCGA-A1-A0SD-01", "TCGA-A1-A0SE-01", "TCGA-A1-A0SH-01", "TCGA-A2-A04U-01",
    "TCGA-B6-A0RS-01", "TCGA-BH-A0HP-01", "TCGA-BH-A18P-01", "TCGA-BH-A18H-01", "TCGA-C8-A138-01", "TCGA-A2-A0EY-01", "TCGA-A8-A08G-01"]
    # these two files contain the contents of the /api/genes and /api/genesaliases, respectively:
    logger = logging.getLogger(__name__)
    # parse mock API results from a local directory
    PORTAL_INSTANCE = validateData.load_portal_info('test_data/api_json_unit_tests/',
                                                    logger,
                                                    offline=True)


class LogBufferTestCase(unittest.TestCase):

    """Superclass for testcases that want to capture log records emitted.

    Defines a self.logger to log to, and a method get_log_records() to
    collect the list of LogRecords emitted by this logger. In addition,
    defines a function print_log_records to format a list of LogRecords
    to standard output.
    """

    def setUp(self):
        """Set up a logger with a buffering handler."""
        self.logger = logging.getLogger(self.__class__.__name__)
        self.logger.setLevel(logging.INFO)
        # add a handler to buffer log records for validation
        self.buffer_handler = logging.handlers.BufferingHandler(capacity=1e6)
        self.logger.addHandler(self.buffer_handler)
        # add a handler to pretty-print log messages to the output
        self.output_handler = logging.StreamHandler(sys.stdout)
        self.output_handler.setFormatter(
            cbioportal_common.LogfileStyleFormatter())
        self.logger.addHandler(self.output_handler)

    def tearDown(self):
        """Remove the logger handlers (and any buffers they may have)."""
        self.logger.removeHandler(self.output_handler)
        self.logger.removeHandler(self.buffer_handler)

    def get_log_records(self):
        """Get the log records written to the logger since the last call."""
        recs = self.logger.handlers[0].buffer
        self.logger.handlers[0].flush()
        return recs

    @staticmethod
    def print_log_records(record_list):
        """Pretty-print a list of log records to standard output.

        This can be used if, while writing unit tests, you want to see
        what the messages currently are. The final unit tests committed
        to version control should not actively print log messages.
        """
        formatter = cbioportal_common.LogfileStyleFormatter()
        for record in record_list:
            print formatter.format(record)


class DataFileTestCase(LogBufferTestCase):

    """Superclass for testcases validating a particular data file.

    Provides a validate() method to validate the data file with a
    particular validator class and collect the log records emitted.
    """

    def validate(self, data_filename, validator_class, extra_meta_fields=None):
        """Validate a file with a Validator and return the log records."""
        meta_dict = {'data_filename': data_filename}
        if extra_meta_fields is not None:
            meta_dict.update(extra_meta_fields)
        validator = validator_class('test_data', meta_dict,
                                    PORTAL_INSTANCE,
                                    self.logger)
        validator.validate()
        return self.get_log_records()


class PostClinicalDataFileTestCase(DataFileTestCase):

    """Superclass for validating data files to be read after clinical files.

    I.e. DEFINED_SAMPLE_IDS will be initialised with a list of sample
    identifiers defined in the study.
    """

    def setUp(self):
        """Prepare for validating a file by setting the samples defined."""
        super(PostClinicalDataFileTestCase, self).setUp()
        self.orig_defined_sample_ids = validateData.DEFINED_SAMPLE_IDS
        validateData.DEFINED_SAMPLE_IDS = DEFINED_SAMPLE_IDS

    def tearDown(self):
        """Restore the environment to before setUp() was called."""
        validateData.DEFINED_SAMPLE_IDS = self.orig_defined_sample_ids
        super(PostClinicalDataFileTestCase, self).tearDown()


# ----------------------------------------------------------------------------
# Test cases for the various Validator classes found in validateData script

class ColumnOrderTestCase(DataFileTestCase):

    """Test if column order requirements are appropriately validated."""

    def test_column_order_validation_SegValidator(self):
        """seg validator needs its columns in a specific order.

        Here we serve a file with wrong order and expect validator to log this:
        """
        # set level according to this test case:
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_seg_wrong_order.txt',
                                    validateData.SegValidator,
                                    extra_meta_fields={'reference_genome_id':
                                                           'hg19'})
        # we expect 2 errors about columns in wrong order,
        # and one about the file not being parseable:
        self.assertEqual(len(record_list), 3)
        # check if both messages come from checkOrderedRequiredColumns:
        for error in record_list[:2]:
            self.assertEqual("ERROR", error.levelname)
            self.assertEqual("_checkOrderedRequiredColumns", error.funcName)

    def test_column_order_validation_ClinicalValidator(self):
        """ClinicalValidator does NOT need its columns in a specific order.

        Here we serve files with different order and no errors or warnings
        """
        # set level according to this test case:
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_clin_order1.txt',
                                    validateData.ClinicalValidator)
        # we expect no errors or warnings
        self.assertEqual(0, len(record_list))
        # if the file has another order, this is also OK:
        record_list = self.validate('data_clin_order2.txt',
                                    validateData.ClinicalValidator)
        # again, we expect no errors or warnings
        self.assertEqual(0, len(record_list))


class ClinicalColumnDefsTestCase(DataFileTestCase):

    """Tests for validations of the column definitions in a clinical file."""

    def test_correct_definitions(self):
        """Test when all record definitions match with portal."""
        record_list = self.validate('data_clin_coldefs_correct.txt',
                                    validateData.ClinicalValidator)
        # expecting two info messages: at start and end of file
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.INFO)

    def test_wrong_definitions(self):
        """Test when record definitions do not match with portal."""
        record_list = self.validate('data_clin_coldefs_wrong_display_name.txt',
                                    validateData.ClinicalValidator)
        # expecting an info message followed by the error, and another error as
        # the rest of the file cannot be parsed
        self.assertEqual(len(record_list), 3)
        # error about the display name of OS_MONTHS
        self.assertEqual(record_list[1].levelno, logging.ERROR)
        self.assertEqual(record_list[1].column_number, 3)
        self.assertIn('display_name', record_list[1].getMessage().lower())

    def test_unknown_attribute(self):
        """Test when a new attribute is defined in the data file."""
        record_list = self.validate('data_clin_coldefs_unknown_attribute.txt',
                                    validateData.ClinicalValidator)
        # expecting two info messages with a warning in between
        self.assertEqual(len(record_list), 3)
        self.assertEqual(record_list[1].levelno, logging.WARNING)
        self.assertEqual(record_list[1].column_number, 7)
        self.assertIn('will be added', record_list[1].getMessage().lower())

    def test_invalid_definitions(self):
        """Test when new attributes are defined with invalid properties."""
        record_list = self.validate('data_clin_coldefs_invalid_priority.txt',
                                    validateData.ClinicalValidator)
        # expecting an info message followed by the error, and another error as
        # the rest of the file cannot be parsed
        self.assertEqual(len(record_list), 3)
        # error about the non-numeric priority of the SAUSAGE column
        self.assertEqual(record_list[1].levelno, logging.ERROR)
        self.assertEqual(record_list[1].line_number, 5)
        self.assertEqual(record_list[1].column_number, 7)


class ClinicalValuesTestCase(DataFileTestCase):

    """Tests for validations on the values of clinical attributes."""

    def test_sample_twice_in_one_file(self):
        """Test when a sample is defined twice in the same file."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_clin_repeated_sample.txt',
                                    validateData.ClinicalValidator)
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 12)
        self.assertEqual(record.column_number, 2)


class CancerTypeFileValidationTestCase(DataFileTestCase):

    """Tests for validations of cancer type files in a study."""

    def test_new_cancer_type(self):
        """Test when a study defines a new cancer type."""
        # {"id":"luad","name":"Lung Adenocarcinoma","color":"Gainsboro"}
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_cancertype_lung.txt',
                                    validateData.CancerTypeValidator)
        # expecting a warning about a new cancer type being added
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.WARNING)
        self.assertEqual(record.cause, 'luad')
        self.assertIn('will be added', record.getMessage().lower())

    def test_cancer_type_missing_column(self):
        """Test when a new cancer type file misses a column."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_cancertype_missing_color_col.txt',
                                    validateData.CancerTypeValidator)
        # expecting an error about the number of columns
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertIn('columns', record.getMessage())


    def test_cancer_type_missing_value(self):
        """Test when a new cancer type has a blank required field."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_cancertype_blank_color_col.txt',
                                    validateData.CancerTypeValidator)
        # expecting an error about the missing value
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.column_number, 4)

    def test_cancer_type_undefined_parent(self):
        """Test when a new cancer type's parent cancer type is not known."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_cancertype_undefined_parent.txt',
                                    validateData.CancerTypeValidator)
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.column_number, 5)

    def test_cancer_type_matching_portal(self):
        """Test when an existing cancer type is defined exactly as known."""
        record_list = self.validate('data_cancertype_confirming_existing.txt',
                                    validateData.CancerTypeValidator)
        # expecting only the two info messages about the file being validated
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.INFO)

    def test_cancer_type_disagreeing_with_portal(self):
        """Test when an existing cancer type is redefined by a study."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_cancertype_redefining.txt',
                                    validateData.CancerTypeValidator)
        # expecting an error message about the cancer type file
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.column_number, 2)
        self.assertEqual(record.cause, 'Breast Cancer')

    def test_cancer_type_defined_twice(self):
        """Test when a study defines the same cancer type id twice.

        No difference is assumed between matching and disagreeing definitions;
        this validation just fails as it never makes sense to do this.
        """
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_cancertype_lung_twice.txt',
                                    validateData.CancerTypeValidator)
        # expecting an error message about the doubly-defined cancer type
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.cause, 'luad')
        self.assertIn('defined a second time', record.getMessage())

class GeneIdColumnsTestCase(PostClinicalDataFileTestCase):

    """Tests validating gene-wise files with different combinations of gene id columns,
    now with invalid Entrez ID and/or Hugo names """

    def test_both_name_and_entrez(self):
        """Test when a file has both the Hugo name and Entrez ID columns."""
        record_list = self.validate('data_cna_genecol_presence_both.txt',
                                    validateData.CNAValidator)
        # expecting two info messages: at start and end of file
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.INFO)

    def test_name_only(self):
        """Test when a file has a Hugo name column but none for Entrez IDs."""
        record_list = self.validate('data_cna_genecol_presence_hugo_only.txt',
                                    validateData.CNAValidator)
        # expecting two info messages: at start and end of file
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.INFO)

    def test_entrez_only(self):
        """Test when a file has an Entrez ID column but none for Hugo names."""
        record_list = self.validate('data_cna_genecol_presence_entrez_only.txt',
                                    validateData.CNAValidator)
        # expecting two info messages: at start and end of file
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.INFO)

    def test_neither_name_nor_entrez(self):
        """Test when a file lacks both the Entrez ID and Hugo name columns."""
        record_list = self.validate('data_cna_genecol_presence_neither.txt',
                                    validateData.CNAValidator)
        # two errors after the info: the first makes the file unparsable
        self.assertEqual(len(record_list), 3)
        for record in record_list[1:]:
            self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record_list[1].line_number, 1)

    """Tests validating gene-wise files with different combinations of gene id columns,
    now with invalid Entrez ID and/or Hugo names """

    def test_both_name_and_entrez_but_invalid_hugo(self):
        """Test when a file has both the Hugo name and Entrez ID columns, but hugo is invalid."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_cna_genecol_presence_both_invalid_hugo.txt',
                                    validateData.CNAValidator)
        # expecting two error messages:
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.WARNING)
        # expecting these to be the cause:
        self.assertEqual(record_list[0].cause, 'XXACAP3')
        self.assertEqual(record_list[1].cause, 'XXAGRN')

    def test_both_name_and_entrez_but_invalid_entrez(self):
        """Test when a file has both the Hugo name and Entrez ID columns, but entrez is invalid."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_cna_genecol_presence_both_invalid_entrez.txt',
                                    validateData.CNAValidator)
        # expecting two error messages:
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.ERROR)
        # expecting these to be the cause:
        self.assertIn('-116983', record_list[0].cause)
        self.assertIn('-375790', record_list[1].cause)

    def test_both_name_and_entrez_but_invalid_couple(self):
        """Test when a file has both the Hugo name and Entrez ID columns, both valid, but association is invalid."""
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_cna_genecol_presence_both_invalid_couple.txt',
                                    validateData.CNAValidator)
        # expecting two error messages:
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.WARNING)
        # expecting these to be the cause:
        self.assertIn('ACAP3', record_list[0].cause)
        self.assertIn('116983', record_list[1].cause)

    def test_name_only_but_invalid(self):
        """Test when a file has a Hugo name column but none for Entrez IDs, and hugo is wrong."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_cna_genecol_presence_hugo_only_invalid.txt',
                                    validateData.CNAValidator)
        # expecting two error messages:
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.ERROR)
        # expecting these to be the cause:
        self.assertEqual(record_list[0].cause, 'XXATAD3A')
        self.assertEqual(record_list[1].cause, 'XXATAD3B')

    def test_name_only_but_ambiguous(self):
        """Test when a file has a Hugo name column but none for Entrez IDs, and hugo maps to multiple Entrez ids.
        This test is also an indirect test of the aliases functionality as this is now the only place
        where ambiguity could arise (in gene table Hugo symbol is now unique)"""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_cna_genecol_presence_hugo_only_ambiguous.txt',
                                    validateData.CNAValidator)
        # expecting one error message
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        # expecting this gene to be the cause
        self.assertEquals(record.cause, 'TRAPPC2P1')

    def test_entrez_only_but_invalid(self):
        """Test when a file has an Entrez ID column but none for Hugo names, and entrez is wrong."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_cna_genecol_presence_entrez_only_invalid.txt',
                                    validateData.CNAValidator)
        # expecting two error messages:
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.ERROR)
        # expecting these to be the cause:
        self.assertEqual(record_list[0].cause, '-54998')
        self.assertEqual(record_list[1].cause, '-126792')

    def test_unambiguous_hugo_also_used_as_alias(self):
        """Test referencing a gene by a Hugo symbol occurring as an alias too.

        This should yield a warning, as the gene for which it is an alias
        might be the gene intended by the user.
        """
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('data_cna_genecol_presence_hugo_only_possible_alias.txt',
                                    validateData.CNAValidator)
        # expecting one error message
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.WARNING)
        # expecting this gene to be the cause
        self.assertEquals(record.cause, 'ACT')


class MutationsSpecialCasesTestCase(PostClinicalDataFileTestCase):

    def test_normal_samples_list_in_maf(self):
        '''
        For mutations MAF files there is a column called "Matched_Norm_Sample_Barcode".
        In the respective meta file it is possible to give a list of sample codes against which this
        column "Matched_Norm_Sample_Barcode" is validated. Here we test if this
        validation works well.
        '''
        # set level according to this test case:
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('mutations/data_mutations_invalid_norm_samples.maf',
                                    validateData.MutationsExtendedValidator,
                                    {'normal_samples_list':
                                     'TCGA-B6-A0RS-10,TCGA-BH-A0HP-10,TCGA-BH-A18P-11, TCGA-BH-A18H-10'})
        # we expect 3 errors about invalid normal samples:
        self.assertEqual(len(record_list), 3)
        # check if both messages come from printDataInvalidStatement:
        found_one_of_the_expected = False
        for error in record_list:
            self.assertEqual("ERROR", error.levelname)
            self.assertEqual("printDataInvalidStatement", error.funcName)
            if "TCGA-C8-A138-10" == error.cause:
                found_one_of_the_expected = True

        self.assertEqual(True, found_one_of_the_expected)

    
    def test_missing_aa_change_column(self):
        """One of Amino_Acid_Change or HGVSp_Short is required, so
        there should be a warning if both Amino_Acid_Change and HGVSp_Short are missing"""
        # set level according to this test case:
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('mutations/data_mutations_missing_aa_change_column.maf',
                                    validateData.MutationsExtendedValidator)
        # we expect 2 errors, something like:
        # ERROR: data_mutations_missing_aa_change_column.maf: line 1: At least one of the columns HGVSp_Short or Amino_Acid_Change needs to be present.
        # ERROR: data_mutations_missing_aa_change_column.maf: Invalid column header, file cannot be parsed
        self.assertEqual(len(record_list), 2)
        # check if both messages come from printDataInvalidStatement:
        self.assertIn("hgvsp_short", record_list[0].getMessage().lower())
        self.assertIn("invalid column header", record_list[1].getMessage().lower())
    
    
    def test_warning_for_missing_SWISSPROT(self):
        """If SWISSPROT is missing (or present and empty), user should be warned about it"""
        # set level according to this test case:
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('mutations/data_mutations_missing_swissprot.maf',
                                    validateData.MutationsExtendedValidator)
        # we expect 1 warning, something like
        # WARNING: data_mutations_missing_swissprot.maf: line 1: SWISSPROT column is recommended if you want to make sure that a specific isoform is used for the PFAM domains drawing in the mutations view.; wrong value: 'SWISSPROT column not found'
        self.assertEqual(len(record_list), 1)
        # check if both messages come from printDataInvalidStatement:
        self.assertIn("swissprot", record_list[0].getMessage().lower())
        
    
    def test_isValidAminoAcidChange(self):
        """Tests if proper warning is given if aa change column is present, but contains wrong (blank) value"""
        # set level according to this test case:
        self.logger.setLevel(logging.WARNING)
        record_list = self.validate('mutations/data_mutations_empty_aa_change_column.maf',
                                    validateData.MutationsExtendedValidator)
        # we expect 1 warning, something like
        # WARNING: data_mutations_empty_aa_change_column.maf: line 2: Amino acid change cannot be parsed from Amino_Acid_Change column value. This mutation record will get a generic "MUTATED" flag; wrong value: 'empty value found'
        self.assertEqual(len(record_list), 2)
        # check if both messages come from printDataInvalidStatement:
        self.assertIn("amino acid change cannot be parsed", record_list[0].getMessage().lower())
    
    
class SegFileValidationTestCase(PostClinicalDataFileTestCase):

    """Tests for the various validations of data in segment CNA data files."""

    @classmethod
    def setUpClass(cls):
        """Override a static method to skip a UCSC HTTP query in each test."""
        super(SegFileValidationTestCase, cls).setUpClass()
        @staticmethod
        def load_chromosome_lengths(genome_build):
            if genome_build != 'hg19':
                raise ValueError(
                        "load_chromosome_lengths() called with genome build '{}'".format(
                            genome_build))
            return {u'1': 249250621, u'10': 135534747, u'11': 135006516,
                    u'12': 133851895, u'13': 115169878, u'14': 107349540,
                    u'15': 102531392, u'16': 90354753, u'17': 81195210,
                    u'18': 78077248, u'19': 59128983, u'2': 243199373,
                    u'20': 63025520, u'21': 48129895, u'22': 51304566,
                    u'3': 198022430, u'4': 191154276, u'5': 180915260,
                    u'6': 171115067, u'7': 159138663, u'8': 146364022,
                    u'9': 141213431, u'X': 155270560, u'Y': 59373566}
        cls.orig_chromlength_method = validateData.SegValidator.load_chromosome_lengths
        validateData.SegValidator.load_chromosome_lengths = load_chromosome_lengths

    @classmethod
    def tearDownClass(cls):
        """Restore the environment to before setUpClass() was called."""
        validateData.SegValidator.load_chromosome_lengths = cls.orig_chromlength_method
        super(SegFileValidationTestCase, cls).tearDownClass()

    def test_valid_seg(self):
        """Validate a segment file without file format errors."""
        record_list = self.validate('data_seg_valid.seg',
                                    validateData.SegValidator,
                                    extra_meta_fields={'reference_genome_id':
                                                           'hg19'})
        # expecting nothing but the info messages at start and end of file
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.INFO)


    def test_unparsable_seg_columns(self):
        """Validate .seg files with non-numeric values and an unsupported chromosome."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_seg_nonsense_values.seg',
                                    validateData.SegValidator,
                                    extra_meta_fields={'reference_genome_id':
                                                           'hg19'})
        self.assertEqual(len(record_list), 3)
        for record in record_list:
            self.assertEqual(record.levelno, logging.ERROR)
            self.assertEqual(record.line_number, 28)
        # unknown chromosome
        self.assertEqual(record_list[0].column_number, 2)
        # non-integral start position
        self.assertEqual(record_list[1].column_number, 3)
        # non-rational mean copy number
        self.assertEqual(record_list[2].column_number, 6)

    def test_negative_length_segment(self):
        """Validate a .seg where a start position is lower than its end position."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_seg_end_before_start.seg',
                                    validateData.SegValidator,
                                    extra_meta_fields={'reference_genome_id':
                                                           'hg19'})
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 11)

    def test_out_of_bounds_coordinates(self):
        """Validate .seg files with regions spanning outside of the chromosome."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_seg_out_of_bounds.seg',
                                    validateData.SegValidator,
                                    extra_meta_fields={'reference_genome_id':
                                                           'hg19'})
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.ERROR)
        # start position before start of chromosome
        self.assertEqual(record_list[0].line_number, 36)
        self.assertEqual(record_list[0].column_number, 3)
        # end position after end of chromosome
        self.assertEqual(record_list[1].line_number, 41)
        self.assertEqual(record_list[1].column_number, 4)

    def test_blank_line(self):
        """Validate a .seg with a blank data line."""
        self.logger.setLevel(logging.ERROR)
        record_list = self.validate('data_seg_blank_line.seg',
                                    validateData.SegValidator,
                                    extra_meta_fields={'reference_genome_id':
                                                           'hg19'})
        self.assertEqual(len(record_list), 1)
        record = record_list.pop()
        self.assertEqual(record.levelno, logging.ERROR)
        self.assertEqual(record.line_number, 35)
        self.assertIn('blank', record.getMessage().lower())


class StudyCompositionTestCase(LogBufferTestCase):

    """Tests for validations of the number of files of certain types."""

    def setUp(self):
        """Store validateData globals changed by running validate_study()."""
        super(StudyCompositionTestCase, self).setUp()
        self.orig_defined_cancer_types = validateData.DEFINED_CANCER_TYPES
        self.orig_defined_sample_ids = validateData.DEFINED_SAMPLE_IDS

    def tearDown(self):
        """Restore the environment to before setUp() was called."""
        validateData.DEFINED_CANCER_TYPES = self.orig_defined_cancer_types
        validateData.DEFINED_SAMPLE_IDS = self.orig_defined_sample_ids
        super(StudyCompositionTestCase, self).tearDown()

    def test_double_cancer_type_file(self):
        """Check behavior when two cancer type files are supplied."""
        self.logger.setLevel(logging.ERROR)
        validateData.validate_study(
            'test_data/study_cancertype_two_files',
            PORTAL_INSTANCE,
            self.logger)
        record_list = self.get_log_records()
        # expecting two errors: one about the two cancer type files, and
        # about the cancer type of the study not having been defined
        self.assertEqual(len(record_list), 2)
        for record in record_list:
            self.assertEqual(record.levelno, logging.ERROR)
        # compare filenames mentioned in the 1st error independent of ordering
        filenames_in_cause_string = set(record_list[0].cause.split(', ', 1))
        self.assertEqual(filenames_in_cause_string,
                         set(['cancer_type_luad.txt', 'cancer_type_lung.txt']))
        # assert that the second error complains about the cancer type
        self.assertEqual(record_list[1].cause, 'luad')


class StableIdValidationTestCase(LogBufferTestCase):

    """Tests to ensure stable_id validation works correctly."""

    def test_unnecessary_and_wrong_stable_id(self):
        """Tests to check behavior when stable_id is not needed (warning) or wrong(error)."""
        validateData.process_metadata_files(
            'test_data/study_metastableid',
            PORTAL_INSTANCE,
            self.logger)
        record_list = self.get_log_records()
        # expecting 1 warning, 1 error:
        self.assertEqual(len(record_list), 3)
        # get both into a variable to avoid dependency on order:
        errors = []
        for record in record_list:
            if record.levelno == logging.ERROR:
                errors.append(record.cause)
            else:
                warning = record

        # expecting one error about wrong stable_id in meta_expression:
        self.assertEqual(len(errors), 2)
        self.assertIn('mrna_test', errors)
        self.assertIn('gistic', errors)

        # expecting one warning about stable_id not being recognized in clinical:
        self.assertEqual(warning.levelno, logging.WARNING)
        self.assertEqual(warning.cause, 'stable_id')


# TODO - add extra unit tests for the genesaliases scenarios (now only test_name_only_but_ambiguous tests part of this)

if __name__ == '__main__':
    unittest.main(buffer=True)
