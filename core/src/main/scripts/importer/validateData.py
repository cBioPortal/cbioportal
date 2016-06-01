#!/usr/bin/env python2.7

# ------------------------------------------------------------------------------
# Data validation script - validates files before import into portal.
# ------------------------------------------------------------------------------


# imports
import sys
import os
import logging.handlers
from collections import OrderedDict
import argparse
import re
import csv
import itertools
import requests
import json

import cbioportal_common


# ------------------------------------------------------------------------------
# globals

# Only supported reference genome build number and name
NCBI_BUILD_NUMBER = 37
GENOMIC_BUILD_NAME = 'hg19'

# study-specific globals
DEFINED_SAMPLE_IDS = None
DEFINED_SAMPLE_ATTRIBUTES = None
PATIENTS_WITH_SAMPLES = None
DEFINED_CANCER_TYPES = None


# ----------------------------------------------------------------------------

VALIDATOR_IDS = {
    cbioportal_common.MetaFileTypes.CNA:'CNAValidator',
    cbioportal_common.MetaFileTypes.CNA_LOG2:'ContinuousValuesValidator',
    cbioportal_common.MetaFileTypes.CNA_CONTINUOUS:'ContinuousValuesValidator',
    cbioportal_common.MetaFileTypes.EXPRESSION:'ContinuousValuesValidator',
    cbioportal_common.MetaFileTypes.METHYLATION:'ContinuousValuesValidator',
    cbioportal_common.MetaFileTypes.MUTATION:'MutationsExtendedValidator',
    cbioportal_common.MetaFileTypes.CANCER_TYPE:'CancerTypeValidator',
    cbioportal_common.MetaFileTypes.SAMPLE_ATTRIBUTES:'SampleClinicalValidator',
    cbioportal_common.MetaFileTypes.PATIENT_ATTRIBUTES:'PatientClinicalValidator',
    cbioportal_common.MetaFileTypes.SEG:'SegValidator',
    cbioportal_common.MetaFileTypes.FUSION:'FusionValidator',
    cbioportal_common.MetaFileTypes.RPPA:'RPPAValidator',
    cbioportal_common.MetaFileTypes.GISTIC_GENES: 'GisticGenesValidator',
    cbioportal_common.MetaFileTypes.TIMELINE:'TimelineValidator',
    cbioportal_common.MetaFileTypes.MUTATION_SIGNIFICANCE:'MutationSignificanceValidator'
}


# ----------------------------------------------------------------------------
# class definitions

class MaxLevelTrackingHandler(logging.Handler):

    """Handler that does nothing but track the maximum msg level emitted."""

    def __init__(self):
        """Initialize the handler with an attribute to track the level."""
        super(MaxLevelTrackingHandler, self).__init__()
        self.max_level = logging.NOTSET

    def emit(self, record):
        """Update the maximum level with a new record."""
        self.max_level = max(self.max_level, record.levelno)

    def get_exit_status(self):
        """Return an exit status for the validator script based on max_level."""
        if self.max_level <= logging.INFO:
            return 0
        elif self.max_level == logging.WARNING:
            return 3
        elif self.max_level == logging.ERROR:
            return 1
        else:
            return 2

class LineCountHandler(logging.Handler):

    """Handler that does nothing but track the number of lines with error and warnings."""

    def __init__(self):
        """Initialize the handler with an attribute to track the lines."""
        super(LineCountHandler, self).__init__()
        self.warning_lines = set()
        self.error_lines = set()

    def emit(self, record):
        """Update the line sets."""
        if hasattr(record, 'line_number'):
            if record.levelno == logging.WARNING:
                self.warning_lines.add(record.line_number)
            if record.levelno == logging.ERROR:
                self.error_lines.add(record.line_number)

    def get_nr_lines_with_error(self):
        """Return the number of lines with an error."""
        return len(self.error_lines)
    
    def get_nr_lines_with_warning(self):
        """Return the number of lines with an warning."""
        return len(self.warning_lines)
    
    def get_nr_lines_with_issue(self):
        """Return the number of lines with an error or warning."""
        return len(self.error_lines | self.warning_lines)
    

class Jinja2HtmlHandler(logging.handlers.BufferingHandler):

    """Logging handler that formats aggregated HTML reports using Jinja2."""

    def __init__(self, study_dir, output_filename, *args, **kwargs):
        """Set study directory name, output filename and buffer size."""
        self.study_dir = study_dir
        self.output_filename = output_filename
        self.max_level = logging.NOTSET
        self.closed = False
        # get the directory name of the currently running script
        self.template_dir = os.path.dirname(__file__)
        super(Jinja2HtmlHandler, self).__init__(*args, **kwargs)

    def emit(self, record):
        """Buffer a message if the buffer is not full."""
        self.max_level = max(self.max_level, record.levelno)
        if len(self.buffer) < self.capacity:
            return super(Jinja2HtmlHandler, self).emit(record)

    def flush(self):
        """Do nothing; emit() caps the buffer and close() renders output."""
        pass

    def shouldFlush(self, record):
        """Never flush; emit() caps the buffer and close() renders output."""
        return False

    def generateHtml(self):
        """Render the HTML page for the current content in self.buffer """
        # require Jinja2 only if it is actually used
        import jinja2
        j_env = jinja2.Environment(
            loader=jinja2.FileSystemLoader(self.template_dir),
            # trim whitespace around Jinja2 operators
            trim_blocks=True,
            lstrip_blocks=True)
        # refer to this function so that it can be used in the template:
        j_env.filters['os.path.relpath'] = os.path.relpath
        template = j_env.get_template('validation_report_template.html.jinja')
        doc = template.render(
            study_dir=self.study_dir,
            record_list=self.buffer,
            max_level=logging.getLevelName(self.max_level))
        with open(self.output_filename, 'w') as f:
            f.write(doc)


class ErrorFileFormatter(cbioportal_common.ValidationMessageFormatter):

    """Fasta-like formatter listing lines on which error messages occurred."""

    def __init__(self, study_dir):
        """Initialize a logging Formatter with an appropriate format string."""
        super(ErrorFileFormatter, self).__init__(
            '>%(rel_filename)s | %(levelname)s: %(message)s\n%(line_string)s')
        self.study_dir = study_dir

    def format(self, record):
        """Aggregate line numbers to a line_string and format the record."""
        record.line_string = self.format_aggregated(
            record, 'line_number',
            single_fmt='%d',
            multiple_fmt='%s', join_string=',', max_join=None,
            optional=False)
        record.rel_filename = os.path.relpath(record.filename_,
                                              self.study_dir)
        return super(ErrorFileFormatter, self).format(record)


class LineMessageFilter(logging.Filter):
    """Filter that selects only validation messages about a line in a file."""
    def filter(self, record):
        return int(hasattr(record, 'filename_') and
                   hasattr(record, 'line_number'))


class CombiningLoggerAdapter(logging.LoggerAdapter):
    """LoggerAdapter that combines its own context info with that in calls."""
    def process(self, msg, kwargs):
        """Add contextual information from call to that from LoggerAdapter."""
        extra = self.extra.copy()
        if 'extra' in kwargs:
            # add elements from the call, possibly overwriting
            extra.update(kwargs['extra'])
        kwargs["extra"] = extra
        return msg, kwargs


class PortalInstance(object):

    """Represent a portal instance, storing the data needed for validation.

    This holds a number of dictionaries representing the particular
    datatypes queried from the portal, each of which may be None
    if the checks are to be skipped.
    """

    def __init__(self, cancer_type_dict, clinical_attribute_dict,
                 hugo_entrez_map, alias_entrez_map):
        """Represent a portal instance with the given dictionaries."""
        self.cancer_type_dict = cancer_type_dict
        self.clinical_attribute_dict = clinical_attribute_dict
        self.hugo_entrez_map = hugo_entrez_map
        self.alias_entrez_map = alias_entrez_map
        self.entrez_set = set()
        for entrez_map in (hugo_entrez_map, alias_entrez_map):
            if entrez_map is not None:
                for entrez_list in entrez_map.values():
                    for entrez_id in entrez_list:
                        self.entrez_set.add(entrez_id)


class Validator(object):

    """Abstract validator class for tab-delimited data files.

    Subclassed by validators for specific data file types, which
    should define a 'REQUIRED_HEADERS' attribute listing the required
    column headers and a `REQUIRE_COLUMN_ORDER` boolean stating
    whether their position is significant. Unless ALLOW_BLANKS is
    set to True, empty cells in lines below the column header will
    be reported as errors.

    The methods `processTopLines`, `checkHeader`, `checkLine` and `onComplete`
    may be overridden (calling their superclass methods) to perform any
    appropriate validation tasks. The superclass `checkHeader` method sets
    self.cols to the list of column names found in the header of the file
    and self.numCols to the number of columns.
    """

    REQUIRED_HEADERS = []
    REQUIRE_COLUMN_ORDER = True
    ALLOW_BLANKS = False

    def __init__(self, study_dir, meta_dict, portal_instance, logger):
        """Initialize a validator for a particular data file.

        :param study_dir: the path at which the study files can be found
        :param meta_dict: dictionary of fields found in corresponding meta file
                         (such as stable id and data file name)
        :param portal_instance: a PortalInstance object for which to validate
        :param logger: logger instance for writing the log messages
        """
        self.filename = os.path.join(study_dir, meta_dict['data_filename'])
        self.filenameShort = os.path.basename(self.filename)
        self.line_number = 0
        self.cols = []
        self.numCols = 0
        self.newlines = ('',)
        self.studyId = ''
        self.headerWritten = False
        # This one is set to True if file could be parsed/read until the end (happens in onComplete)
        self.fileCouldBeParsed = False
        self.portal = portal_instance
        self.logger = CombiningLoggerAdapter(
            logger,
            extra={'filename_': self.filename})
        self.line_count_handler = None
        self.meta_dict = meta_dict

    def validate(self):
        """Validate the data file."""
        # add a handler to keep track of the number of lines with errors
        self.line_count_handler = LineCountHandler()
        self.logger.logger.addHandler(self.line_count_handler)
        try:
            # actually validate the data file
            self._validate_file()
        finally:
            self.logger.logger.removeHandler(self.line_count_handler)

    def _validate_file(self):
        """Read through the data file and validate as much as can be parsed."""

        self.logger.debug('Starting validation of file')

        with open(self.filename, 'rU') as data_file:

            # parse any block of start-of-file comment lines and the tsv header
            top_comments = []
            line_number = 0
            for line_number, line in enumerate(data_file,
                                               start=line_number + 1):
                self.line_number = line_number
                if line.startswith('#'):
                    top_comments.append(line)
                else:
                    header_line = line
                    # end of the file's header
                    break
            # if the loop wasn't broken by a non-commented line
            else:
                self.logger.error('No column header or data found in file',
                                  extra={'line_number': self.line_number})
                return

            # parse start-of-file comment lines, if any
            if not self.processTopLines(top_comments):
                self.logger.error(
                    'Invalid header comments, file cannot be parsed')
                return

            # read five data lines to detect quotes in the tsv file
            first_data_lines = []
            for i, line in enumerate(data_file):
                first_data_lines.append(line)
                if i >= 4:
                    break
            sample_content = header_line + ''.join(first_data_lines)
            try:
                dialect = csv.Sniffer().sniff(sample_content, delimiters='\t')
            except csv.Error:
                self.logger.error('Not a valid tab separated file. Check if all lines have the same number of columns and if all separators are tabs.') 
                return
            # sniffer assumes " if no quote character exists
            if dialect.quotechar == '"' and not (
                    dialect.delimiter + '"' in sample_content or
                    '"' + dialect.delimiter in sample_content):
                dialect.quoting = csv.QUOTE_NONE
            if not self._checkTsvDialect(dialect):
                self.logger.error(
                    'Invalid file format, file cannot be parsed')
                return

            # parse the first non-commented line as the tsv header
            header_cols = csv.reader([header_line], dialect).next()
            if self.checkHeader(header_cols) > 0:
                self.logger.error(
                    'Invalid column header, file cannot be parsed')
                return

            # read through the data lines of the file
            csvreader = csv.reader(itertools.chain(first_data_lines,
                                                   data_file),
                                   dialect)
            for line_number, fields in enumerate(csvreader,
                                                 start=line_number + 1):
                self.line_number = line_number
                if all(x.strip() == '' for x in fields):
                    self.logger.error(
                        'Blank line',
                        extra={'line_number': self.line_number})
                elif fields[0].startswith('#'):
                    self.logger.error(
                        "Data line starting with '#' skipped",
                        extra={'line_number': self.line_number})
                else:
                    self.checkLine(fields)

            # (tuple of) string(s) of the newlines read (for 'rU' mode files)
            self.newlines = data_file.newlines

        # after the entire file has been read
        self.onComplete()

    def onComplete(self):
        """Perform final validations after all lines have been checked.

        Overriding methods should call this superclass method *after* their own
        validations, as it logs the message that validation was completed.
        """
        self._checkLineBreaks()
        # finalize
        self.fileCouldBeParsed = True
        self.logger.info('Validation of file complete')
        self.logger.info('Read %d lines. '
                         'Lines with warning: %d. Lines with error: %d',
                         self.line_number,
                         self.line_count_handler.get_nr_lines_with_warning(),
                         self.line_count_handler.get_nr_lines_with_error())

    def processTopLines(self, line_list):
        """Hook to validate any list of comment lines above the TSV header.

        Return False if these lines are invalid and the file cannot be
        parsed, True otherwise.
        """
        return True

    def checkHeader(self, cols):

        """Check that the header has the correct items and set self.cols.

        :param cols: The list of column headers to be validated

        :return the number of errors found.
        """

        num_errors = 0

        # TODO check for end-of-line whitespace

        self.cols = cols
        self.numCols = len(self.cols)

        num_errors += self._checkRepeatedColumns()

        if self.REQUIRE_COLUMN_ORDER:
            num_errors += self._checkOrderedRequiredColumns()
        else:
            num_errors += self._checkUnorderedRequiredColumns()

        return num_errors

    def checkLine(self, data):
        """Check data values from a line after the file header.

        :param data: The list of values parsed from the line
        """

        if data[:self.numCols] == self.cols:
            if self.logger.isEnabledFor(logging.ERROR):
                self.logger.error(
                    'Repeated header',
                    extra={'line_number': self.line_number,
                           'cause': ', '.join(data[:self.numCols])})

        line_col_count = len(data)

        if line_col_count != self.numCols:
            self.logger.error('Expected %d columns based on header, '
                              'found %d',
                              self.numCols, line_col_count,
                              extra={'line_number': self.line_number})

        if not self.ALLOW_BLANKS:
            for col_index, col_name in enumerate(self.cols):
                if col_index < line_col_count and data[col_index].strip() == '':
                    self.logger.error(
                        'Blank cell found in column',
                        extra={'line_number': self.line_number,
                               'column_number': col_index + 1,
                               'cause': "'%s' (in column '%s')" % (
                                    data[col_index], col_name)})

    def _checkUnorderedRequiredColumns(self):
        """Check for missing column headers, independent of their position.

        Return the number of errors encountered.
        """
        num_errors = 0
        for col_name in self.REQUIRED_HEADERS:
            if col_name not in self.cols:
                self.logger.error(
                    'Missing column: %s',
                    col_name,
                    extra={'line_number': self.line_number,
                           'cause': ', '.join(
                                    self.cols[:len(self.REQUIRED_HEADERS)]) +
                                ', (...)'})
                num_errors += 1
        return num_errors

    def _checkOrderedRequiredColumns(self):
        """Check if the column header for each position is correct.

        Return the number of errors encountered.
        """
        num_errors = 0
        for col_index, col_name in enumerate(self.REQUIRED_HEADERS):
            if col_index >= self.numCols:
                num_errors += 1
                self.logger.error(
                    "Invalid header: expected '%s' in column %d,"
                    " found end of line",
                    col_name, col_index + 1,
                    extra={'line_number': self.line_number})
            elif self.cols[col_index] != col_name:
                num_errors += 1
                self.logger.error(
                    "Invalid header: expected '%s' in this column",
                    col_name,
                    extra={'line_number': self.line_number,
                           'column_number': col_index + 1,
                           'cause': self.cols[col_index]})
        return num_errors

    def _checkTsvDialect(self, dialect):
        """Check if a csv.Dialect subclass describes a valid cBio data file."""
        if dialect.delimiter != '\t':
            self.logger.error('Not a tab-delimited file',
                              extra={'cause': 'delimiters of type: %s' %
                                              repr(dialect.delimiter)})
            return False
        if dialect.quoting != csv.QUOTE_NONE:
            self.logger.error('Found quotation marks around field(s) in the first rows of the file. '
                              'Fields and values should not be surrounded by quotation marks.',
                              extra={'cause': 'quotation marks of type: [%s] ' %
                                              repr(dialect.quotechar)[1:-1]})
        return True

    def _checkLineBreaks(self):
        """Checks line breaks, reports to user."""
        if self.newlines not in("\r\n","\r","\n"):
            self.logger.error('No line breaks recognized in file',
                              extra={'cause': repr(self.newlines)[1:-1]})

    def checkInt(self, value):
        """Checks if a value is an integer."""
        try:
            int(value)
            return True
        except ValueError:
            return False

    def checkFloat(self, value):
        """Check if a string represents a floating-point numeral."""
        try:
            float(value)
            return True
        except ValueError:
            return False

    def checkSampleId(self, sample_id, column_number):
        """Check whether a sample id is defined, logging an error if not.

        Return True if the sample id was valid, False otherwise.
        """
        if sample_id not in DEFINED_SAMPLE_IDS:
            self.logger.error(
                'Sample ID not defined in clinical file',
                extra={'line_number': self.line_number,
                       'column_number': column_number,
                       'cause': sample_id})
            return False
        return True

    # TODO: let this function know the column numbers for logging messages
    def checkGeneIdentification(self, gene_symbol=None, entrez_id=None):
        """Attempt to resolve a symbol-Entrez pair, logging any issues.

        It will fail to resolve in these cases:
            1. (error) Entrez id and symbol are both missing (None)
        If self.portal.hugo_entrez_map and self.portal.alias_entrez_map are
        defined:
            2. (warning) Only one of the identifiers is supplied, and its value
               cannot be found in the portal
            3. (error) The gene symbol maps to multiple Entrez ids
            4. (error) The gene alias maps to multiple Entrez ids

        Furthermore, the function logs a warning in the following cases, if
        self.portal.hugo_entrez_map and self.portal.alias_entrez_map are
        defined:
            1. (warning) Entrez ID exists, but the gene symbol specified is not
               known to the portal
            2. (warning) Gene symbol and Entrez identifier do not match
            3. (warning) The Hugo gene symbol maps to a single Entrez gene ID,
               but is also associated to other genes as an alias.

        Return the Entrez gene id (or symbol if the PortalInstance maps are
        undefined and the mapping step is skipped), or None if no gene could be
        unambiguously identified.
        """
        # set to upper, as both maps contain symbols in upper
        if gene_symbol is not None:
            gene_symbol = gene_symbol.upper()

        if entrez_id is not None:
            try:
                entrez_as_int = int(entrez_id)
            except ValueError:
                entrez_as_int = None
            if entrez_as_int is None:
                self.logger.warning(
                    'Entrez gene identifier is not an integer; '
                    'this gene will not be loaded',
                    extra={'line_number': self.line_number,
                           'cause': entrez_id})
                return None
            elif entrez_as_int <= 0:
                self.logger.error(
                    'Entrez gene identifier is non-positive',
                    extra={'line_number': self.line_number,
                           'cause': entrez_id})
                return None

        # check whether at least one is present
        if entrez_id is None and gene_symbol is None:
            self.logger.error(
                'No Entrez id or gene symbol provided for gene',
                extra={'line_number': self.line_number})
            return None

        # if portal information is absent, skip the rest of the checks
        if (self.portal.hugo_entrez_map is None or
                self.portal.alias_entrez_map is None):
            return entrez_id or gene_symbol

        # try to use the portal maps to resolve to a single Entrez id
        identified_entrez_id = None
        if entrez_id is not None:
            if entrez_id in self.portal.entrez_set:
                # set the value to be returned
                identified_entrez_id = entrez_id
                # some warnings if the gene symbol is specified too
                if gene_symbol is not None:
                    if (gene_symbol not in self.portal.hugo_entrez_map and
                            gene_symbol not in self.portal.alias_entrez_map):
                        self.logger.warning(
                            'Entrez ID exists, but the gene symbol specified '
                            'is not known to the cBioPortal instance. The '
                            'symbol will be ignored',
                            extra={'line_number': self.line_number,
                                   'cause': gene_symbol})
                    elif entrez_id not in itertools.chain(
                            self.portal.hugo_entrez_map.get(gene_symbol, []),
                            self.portal.alias_entrez_map.get(gene_symbol, [])):
                        self.logger.warning(
                            'Gene symbol and Entrez identifier do not match, '
                            'the symbol will be ignored',
                            extra={'line_number': self.line_number,
                                   'cause': '(%s, %s)' % (gene_symbol,
                                                          entrez_id)})
            else:
                self.logger.warning(
                    'Entrez gene id not known to the cBioPortal instance. '
                    'This gene will not be loaded',
                    extra={'line_number': self.line_number,
                           'cause': entrez_id})
        # no Entrez id, only a symbol
        elif gene_symbol is not None:
            # count canonical HUGO symbols and aliases that map this symbol to
            # a gene
            num_entrezs_for_hugo = len(
                self.portal.hugo_entrez_map.get(gene_symbol, []))
            num_entrezs_for_alias = len(
                self.portal.alias_entrez_map.get(gene_symbol, []))
            if num_entrezs_for_hugo == 1:
                # set the value to be returned
                identified_entrez_id = \
                    self.portal.hugo_entrez_map[gene_symbol][0]
                # check if there are other *different* entrez ids associated
                # with this symbol
                other_entrez_ids_in_aliases = [
                    x for x in
                    self.portal.alias_entrez_map.get(gene_symbol, []) if
                    x != identified_entrez_id]
                if len(other_entrez_ids_in_aliases) >= 1:
                    # give a warning, as the symbol may have been used to refer
                    # to different entrez_ids over time
                    self.logger.warning(
                        'This Hugo gene symbol maps to a single Entrez gene '
                        'ID, but is also associated to other genes as an '
                        'alias. The system will assume the official Hugo '
                        'symbol to be the intended one.',
                        extra={'line_number': self.line_number,
                               'cause': gene_symbol})
            elif num_entrezs_for_hugo > 1:
                # nb: this should actually never occur, see also https://github.com/cBioPortal/cbioportal/issues/799
                self.logger.error(
                    'Gene symbol maps to multiple Entrez ids (%s), '
                    'please specify which one you mean',
                    '/'.join(self.portal.hugo_entrez_map[gene_symbol]),
                    extra={'line_number': self.line_number,
                          'cause': gene_symbol})
            # no canonical symbol, but a single unambiguous alias
            elif num_entrezs_for_alias == 1:
                # set the value to be returned
                identified_entrez_id = \
                    self.portal.alias_entrez_map[gene_symbol][0]
            # no canonical symbol, and multiple different aliases
            elif num_entrezs_for_alias > 1:
                # Loader deals with this, so give warning
                # TODO: move matched IDs out of the message for collapsing
                self.logger.warning(
                    'Gene alias maps to multiple Entrez ids (%s), '
                    'please specify which one you mean or choose a non-ambiguous symbol',
                    '/'.join(self.portal.alias_entrez_map[gene_symbol]),
                    extra={'line_number': self.line_number,
                           'cause': gene_symbol})
            # no canonical symbol and no alias
            else:
                self.logger.warning(
                    'Gene symbol not known to the cBioPortal instance. This '
                    'gene will not be loaded',
                    extra={'line_number': self.line_number,
                           'cause': gene_symbol})

        return identified_entrez_id

    def _checkRepeatedColumns(self):
        num_errors = 0
        seen = set()
        for col_num, col in enumerate(self.cols):
            if col not in seen:
                seen.add(col)
            else:
                num_errors += 1
                self.logger.error('Repeated column header',
                                  extra={'line_number': self.line_number,
                                         'column_number': col_num,
                                         'cause': col})
        return num_errors


class FeaturewiseFileValidator(Validator):

    """Validates a file with rows for features and columns for ids and samples.

    The first few columns (collectively defined in the class attributes
    REQUIRED_HEADERS and OPTIONAL_HEADERS) identify the features
    (e.g. genes) and the rest correspond to the samples.

    Subclasses should override the parseFeatureColumns(self,nonsample_col_vals)
    method to check the non-sample columns preceding them, returning the unique
    id of the feature. The method can find the names of the columns recognized
    in the file in self.nonsample_cols. checkValue(self, value, col_index)
    should also be overridden to check a value in a sample column.
    """

    OPTIONAL_HEADERS = []
    REQUIRE_COLUMN_ORDER = False

    def __init__(self, *args, **kwargs):
        super(FeaturewiseFileValidator, self).__init__(*args, **kwargs)
        self.nonsample_cols = []
        self.num_nonsample_cols = 0
        self.sampleIds = []
        self._feature_id_lines = {}

    def checkHeader(self, cols):
        """Validate the header and read sample IDs from it.

        Return the number of fatal errors.
        """
        num_errors = super(FeaturewiseFileValidator, self).checkHeader(cols)
        # collect non-sample columns:
        for col_name in self.cols:
            if col_name in self.REQUIRED_HEADERS + self.OPTIONAL_HEADERS:
                # add it to the list of non-sample columns in the file:
                self.nonsample_cols.append(col_name)
            else:
                # reached samples group
                break
        self.num_nonsample_cols = len(self.nonsample_cols)
        num_errors += self._set_sample_ids_from_columns()
        return num_errors

    def checkLine(self, data):
        """Check the feature and sample columns in a data line."""
        super(FeaturewiseFileValidator, self).checkLine(data)
        # parse and check the feature identifiers (implemented by subclasses)
        feature_id = self.parseFeatureColumns(data[:self.num_nonsample_cols])
        # skip line if no feature was identified
        if feature_id is None:
            return
        # skip line with an error if the feature was encountered before
        if feature_id in self._feature_id_lines:
            self.logger.warning(
                'Duplicate line for a previously listed feature/gene, '
                'this line will be ignored',
                extra={
                    'line_number': self.line_number,
                    'cause': '%s (already defined on line %d)' % (
                            feature_id,
                            self._feature_id_lines[feature_id])})
            return
        # remember the feature id and check the value for each sample
        self._feature_id_lines[feature_id] = self.line_number
        for column_index, value in enumerate(data):
            if column_index >= len(self.nonsample_cols):
                # checkValue() should be implemented by subclasses
                self.checkValue(value, column_index)

    def parseFeatureColumns(self, nonsample_col_vals):
        """Override to check vals in the non-sample cols and return the id."""
        raise NotImplementedError('The {} class did not provide a method to '
                                  'validate values in sample columns.'.format(
                                      self.__class__.__name__))

    def checkValue(self, value, column_index):
        """Override to validate a value in a sample column."""
        raise NotImplementedError('The {} class did not provide a method to '
                                  'validate values in sample columns.'.format(
                                      self.__class__.__name__))

    def _set_sample_ids_from_columns(self):
        """Extracts sample IDs from column headers and set self.sampleIds."""
        num_errors = 0
        # check whether any sample columns are present
        if len(self.cols[self.num_nonsample_cols:]) == 0:
            self.logger.error('No sample columns found',
                              extra={'line_number': self.line_number})
            num_errors += 1
        # set self.sampleIds to the list of sample column names
        self.sampleIds = self.cols[self.num_nonsample_cols:]
        # validate each sample id
        for index, sample_id in enumerate(self.sampleIds):
            if not self.checkSampleId(
                    sample_id,
                    column_number=self.num_nonsample_cols + index + 1):
                num_errors += 1
            if ' ' in sample_id:
                self.logger.error(
                    'White space in SAMPLE_ID is not supported',
                    extra={'line_number': self.line_number,
                           'cause': sample_id})
                num_errors += 1
                
        return num_errors


class GenewiseFileValidator(FeaturewiseFileValidator):

    """FeatureWiseValidator that has Hugo and/or Entrez as feature columns."""

    REQUIRED_HEADERS = []
    OPTIONAL_HEADERS = ['Hugo_Symbol', 'Entrez_Gene_Id']
    ALLOW_BLANKS = True
    NULL_VALUES = ["NA"]

    def checkHeader(self, cols):
        """Validate the header and read sample IDs from it.

        Return the number of fatal errors.
        """
        num_errors = super(GenewiseFileValidator, self).checkHeader(cols)
        # see if at least one of the gene identifiers is in the right place
        
        
        if ('Hugo_Symbol' in self.sampleIds or
                  'Entrez_Gene_Id' in self.sampleIds):
            self.logger.error('Hugo_Symbol or Entrez_Gene_Id need to be placed before the '
                              'sample ID columns of the file.',
                              extra={'line_number': self.line_number})
            num_errors += 1
        elif not ('Hugo_Symbol' in self.nonsample_cols or
                  'Entrez_Gene_Id' in self.nonsample_cols):
            self.logger.error('At least one of the columns Hugo_Symbol or '
                              'Entrez_Gene_Id needs to be present.',
                              extra={'line_number': self.line_number})
            num_errors += 1
        elif ('Entrez_Gene_Id' not in self.nonsample_cols):
            self.logger.warning('The recommended column Entrez_Gene_Id was not found. '
                                'Using Hugo_Symbol for all gene parsing',
                                extra={'line_number': self.line_number})
        return num_errors

    def parseFeatureColumns(self, nonsample_col_vals):
        """Check the gene identifier columns."""
        hugo_symbol = None
        entrez_id = None
        if 'Hugo_Symbol' in self.nonsample_cols:
            hugo_index = self.nonsample_cols.index('Hugo_Symbol')
            hugo_symbol = nonsample_col_vals[hugo_index].strip()
            # treat empty string as a missing value
            if hugo_symbol == '':
                hugo_symbol = None
        if 'Entrez_Gene_Id' in self.nonsample_cols:
            entrez_index = self.nonsample_cols.index('Entrez_Gene_Id')
            entrez_id = nonsample_col_vals[entrez_index].strip()
            # treat the empty string as a missing value
            if entrez_id == '':
                entrez_id = None
        return self.checkGeneIdentification(hugo_symbol, entrez_id)

class CNAValidator(GenewiseFileValidator):

    """Sub-class CNA validator."""
    ALLOWED_VALUES = ['-2', '-1', '0', '1', '2'] + GenewiseFileValidator.NULL_VALUES

    def checkValue(self, value, col_index):
        """Check a value in a sample column."""
        if value.strip() not in self.ALLOWED_VALUES:
            if self.logger.isEnabledFor(logging.ERROR):
                self.logger.error(
                    'Invalid CNA value: possible values are [%s]',
                    ', '.join(self.ALLOWED_VALUES),
                    extra={'line_number': self.line_number,
                           'column_number': col_index + 1,
                           'cause': value})


class MutationsExtendedValidator(Validator):

    """Sub-class mutations_extended validator."""

    # TODO - maybe this should comply to https://wiki.nci.nih.gov/display/TCGA/Mutation+Annotation+Format+%28MAF%29+Specification ? 
    REQUIRED_HEADERS = [
        'Tumor_Sample_Barcode',
        'Hugo_Symbol', # Required to initialize the Mutation Mapper tabs
        'Variant_Classification', # seems to be important during loading/filtering step.
    ]
    REQUIRE_COLUMN_ORDER = False
    ALLOW_BLANKS = True

    # MutationFilter.java filters these types. Therefore, there is no reason to add warnings and errors for them 
    SKIP_VARIANT_TYPES = [
        'Silent', 
        'Intron', 
        '3\'UTR', 
        '3\'Flank', 
        '5\'UTR',
        '5\'Flank',
        'IGR',
        'RNA'
    ]

    # Used for mapping column names to the corresponding function that does a check on the value.
    CHECK_FUNCTION_MAP = {
        'Matched_Norm_Sample_Barcode':'checkMatchedNormSampleBarcode',
        'NCBI_Build':'checkNCBIbuild',
        'Verification_Status':'checkVerificationStatus',
        'Validation_Status':'checkValidationStatus',
        't_alt_count':'check_t_alt_count',
        't_ref_count':'check_t_ref_count',
        'n_alt_count':'check_n_alt_count',
        'n_ref_count':'check_n_ref_count',
        'Tumor_Sample_Barcode': 'checkNotBlank',
        'Hugo_Symbol': 'checkNotBlank', 
        'HGVSp_Short': 'checkHgvspShort',
        'Amino_Acid_Change': 'checkAminoAcidChange',
        'Variant_Classification': 'checkNotBlank',
        'SWISSPROT': 'checkSwissProt'
    }

    def __init__(self, *args, **kwargs):
        super(MutationsExtendedValidator, self).__init__(*args, **kwargs)
        # FIXME: consider making this attribute a local var in in checkLine(),
        # it really only makes sense there
        self.extraCols = []
        self.extra_exists = False
        self.extra = ''

    def checkHeader(self, cols):
        """Validate header, requiring at least one gene id column."""
        num_errors = super(MutationsExtendedValidator, self).checkHeader(cols)
        if not ('Hugo_Symbol' in self.cols or 'Entrez_Gene_Id' in self.cols):
            self.logger.error('At least one of the columns Hugo_Symbol or '
                              'Entrez_Gene_Id needs to be present.',
                              extra={'line_number': self.line_number})
            num_errors += 1
        elif ('Entrez_Gene_Id' not in self.cols):
            self.logger.warning('The recommended column Entrez_Gene_Id was not found. '
                                'Using Hugo_Symbol for all gene parsing',
                                extra={'line_number': self.line_number})

        if not 'SWISSPROT' in self.cols:
            self.logger.warning(
                'Including the SWISSPROT column is recommended to make sure '
                'that the Uniprot canonical isoform is used when drawing Pfam '
                'domains in the mutations view',
                extra={'line_number': self.line_number})

        # one of these columns should be present:
        if not ('HGVSp_Short' in self.cols or 'Amino_Acid_Change' in self.cols):
            self.logger.error('At least one of the columns HGVSp_Short or '
                              'Amino_Acid_Change needs to be present.',
                              extra={'line_number': self.line_number})
            num_errors += 1
            
        return num_errors

    def checkLine(self, data):

        """Each value in each line is checked individually.

        From the column name (stored in self.cols), the
        corresponding function to check the value is selected from
        CHECK_FUNCTION_MAP. Will emit a generic warning
        message if this function returns False. If the function sets
        self.extra_exists to True, self.extra will be used in this
        message.
        """

        super(MutationsExtendedValidator, self).checkLine(data)
        if self.skipValidation(data):
            return

        for col_name in self.CHECK_FUNCTION_MAP:
            # if optional column was found, validate it:
            if col_name in self.cols:
                col_index = self.cols.index(col_name)
                value = data[col_index]
                # get the checking method for this column if available, or None
                checking_function = getattr(
                    self,
                    self.CHECK_FUNCTION_MAP[col_name])
                # FIXME: remove the 'data' argument, it's spaghetti
                if not checking_function(value, data):
                    self.printDataInvalidStatement(value, col_index)
                elif self.extra_exists or self.extra:
                    raise RuntimeError(('Checking function %s set an error '
                                        'message but reported no error') %
                                       checking_function.__name__)

        # validate Tumor_Sample_Barcode value to make sure it exists in study sample list:
        sample_id_column_index = self.cols.index('Tumor_Sample_Barcode')
        value = data[sample_id_column_index]
        self.checkSampleId(value, column_number=sample_id_column_index + 1)

        # parse hugo and entrez to validate them together
        hugo_symbol = None
        entrez_id = None
        if 'Hugo_Symbol' in self.cols:
            hugo_symbol = data[self.cols.index('Hugo_Symbol')].strip()
            # treat the empty string or 'Unknown' as a missing value
            if hugo_symbol in ('', 'Unknown'):
                hugo_symbol = None
        if 'Entrez_Gene_Id' in self.cols:
            entrez_id = data[self.cols.index('Entrez_Gene_Id')].strip()
            # treat the empty string or 0 as a missing value
            if entrez_id in ('', '0'):
                entrez_id = None
        # validate hugo and entrez together:
        self.checkGeneIdentification(hugo_symbol, entrez_id)

    def printDataInvalidStatement(self, value, col_index):
        """Prints out statement for invalid values detected."""
        message = ("Value in column '%s' is invalid" %
                   self.cols[col_index])
        if self.extra_exists:
            message = self.extra
            self.extra = ''
            self.extra_exists = False
        self.logger.error(
            message,
            extra={'line_number': self.line_number,
                   'column_number': col_index + 1,
                   'cause': value})

    # These functions check values of the MAF according to their name.
    # The mapping of which function checks which value is a global value
    # at the top of the script. If any other checks need to be added for
    # another field name, add the map in the global corresponding to
    # the function name that is created to check it.


    def checkNCBIbuild(self, value, data):
        if self.checkInt(value) and value != '':
            if int(value) != NCBI_BUILD_NUMBER:
                return False
        return True
    
    def checkMatchedNormSampleBarcode(self, value, data):
        if value != '':
            if 'normal_samples_list' in self.meta_dict and self.meta_dict['normal_samples_list'] != '':
                normal_samples_list = [x.strip() for x in self.meta_dict['normal_samples_list'].split(',')]
                if value not in normal_samples_list:
                    self.extra = "Normal sample id not in list of sample ids configured in corresponding metafile. " \
                    "Please check your metafile field 'normal_samples_list'." 
                    self.extra_exists = True
                    return False
        return True
    
    
    def checkVerificationStatus(self, value, data):
        # if value is not blank, then it should be one of these:
        if self.checkNotBlank(value, data) and value.lower() not in ('verified', 'unknown'):
            return False
        return True
    
    def checkValidationStatus(self, value, data):
        # if value is not blank, then it should be one of these:
        if self.checkNotBlank(value, data) and value.lower() not in ('untested', 'inconclusive',
                                 'valid', 'invalid'):
            return False
        return True
    
    def check_t_alt_count(self, value, data):
        if not self.checkInt(value) and value != '':
            return False
        return True
    
    def check_t_ref_count(self, value, data):
        if not self.checkInt(value) and value != '':
            return False
        return True
    
    def check_n_alt_count(self, value, data):
        if not self.checkInt(value) and value != '':
            return False
        return True

    def check_n_ref_count(self, value, data):
        if not self.checkInt(value) and value != '':
            return False
        return True

    def isValidAminoAcidChange(self, value, data):
        """Test whether a string is a valid amino acid change specification."""
        # TODO implement this test, may require bundling the hgvs package:
        # https://pypi.python.org/pypi/hgvs/
        
        # for now, we will only check as follows: 
        if self.checkNotBlank(value, data):
            return True
        else:
            # is blank, so check:
            # if Variant_Classification in ["Splice_Site", ....] 
            # then it is allowed to be blank, 
            # otherwise it should not be blank 
            variant_classification = data[self.cols.index('Variant_Classification')]
            if variant_classification in ('Splice_Site'):
                return True
            else:
                return False
            
            
    def checkHgvspShort(self, value, data):
        """Test whether HGVSp_Short can be parsed as an amino acid change."""
        return self.checkAminoAcidChange(value, data, column_name = 'HGVSp_Short') 


    def checkAminoAcidChange(self, value, data, column_name = 'Amino_Acid_Change'):
        """Test whether the amino acid change value is 'valid' according to isValidAminoAcidChange."""
        if not self.isValidAminoAcidChange(value, data):
            # we give a warning if value is not valid telling user his 
            # record will get a default value "MUTATED" when loaded in the DB.
            self.logger.warning('Amino acid change cannot be parsed from %s column value. '
                                'This mutation record will get a generic "MUTATED" flag',
                                column_name,
                              extra={'line_number': self.line_number,
                                     'cause': 'empty value found'}) 
        
        # it is just a warning, so we can return True always:
        return True

    def skipValidation(self, data):
        """Test whether the mutation is silent and should be skipped."""
        is_silent = False
        variant_classification = data[self.cols.index('Variant_Classification')]
        
        hugo_symbol = data[self.cols.index('Hugo_Symbol')]
        entrez_id = '0'
        if 'Entrez_Gene_Id' in self.cols:
            entrez_id = data[self.cols.index('Entrez_Gene_Id')]
        if hugo_symbol == 'Unknown' and entrez_id == '0' and variant_classification != 'IGR':
            # the MAF specification documents the use of Unknown and 0 here
            # for intergenic mutations, and since the Variant_Classification
            # column is often invalid, cBioPortal interprets this combination
            # (or just the symbol if the Entrez column is absent) as such, 
            # but with a warning:
            self.logger.warning(
                "Gene specification for this mutation implies "
                "intergenic even though Variant_Classification is "
                "not 'IGR'; this variant will be filtered out",
                extra={'line_number': self.line_number,
                       'cause': "Hugo Symbol 'Unknown', Entrez ID 0"})
            is_silent = True
        elif variant_classification in self.SKIP_VARIANT_TYPES:
            self.logger.info("Validation of line skipped due to cBioPortal's filtering. "
                             "Filtered types: [%s]",
                             ', '.join(self.SKIP_VARIANT_TYPES),
                             extra={'line_number': self.line_number,
                                    'cause': variant_classification})
            is_silent = True

        return is_silent

    def checkNotBlank(self, value, data):
        """Test whether a string is blank."""
        if value is None or value.strip() == '':
            return False
        return True
    
    def checkSwissProt(self, value, data):
        """Test whether SWISSPROT string is blank and give warning if blank."""
        if value is None or value.strip() == '':
            self.logger.warning(
                'Missing value in SWISSPROT column; this column is '
                'recommended to make sure that the Uniprot canonical isoform '
                'is used when drawing Pfam domains in the mutations view',
                extra={'line_number': self.line_number,
                       'cause':'blank value in SWISSPROT column'})
            
        # it is just a warning, so we can return True always:
        return True


class ClinicalValidator(Validator):

    """Abstract Validator class for clinical data files.

    Subclasses define the columns that must be present in REQUIRED_HEADERS,
    and the value of the 'is_patient_attribute' property for attributes
    defined in this file in PROP_IS_PATIENT_ATTRIBUTE.
    """

    REQUIRE_COLUMN_ORDER = False
    PROP_IS_PATIENT_ATTRIBUTE = None
    NULL_VALUES = ["[not applicable]", "[not available]", "[pending]", "[discrepancy]","[completed]","[null]", ""]
    ALLOW_BLANKS = True

    def __init__(self, *args, **kwargs):
        super(ClinicalValidator, self).__init__(*args, **kwargs)
        self.attr_defs = []
        self.newly_defined_attributes = set()

    def processTopLines(self, line_list):

        """Parse the the attribute definitions above the column header."""

        LINE_NAMES = ('display_name',
                      'description',
                      'datatype',
                      'priority')

        if not line_list:
            self.logger.error(
                'No data type definition headers found in clinical data file',
                extra={'line_number': self.line_number})
            return False
        if len(line_list) != len(LINE_NAMES):
            self.logger.error(
                '%d comment lines at start of clinical data file, expected %d',
                len(line_list),
                len(LINE_NAMES))
            return False

        # remove the # signs
        line_list = [line[1:] for line in line_list]

        attr_defs = None
        num_attrs = 0
        csvreader = csv.reader(line_list,
                               delimiter='\t',
                               quoting=csv.QUOTE_NONE,
                               strict=True)
        invalid_values = False
        for line_index, row in enumerate(csvreader):

            if attr_defs is None:
                # make a list of as many dictionaries as there are columns
                num_attrs = len(row)
                attr_defs = [OrderedDict() for i in range(num_attrs)]
            elif len(row) != num_attrs:
                self.logger.error(
                    'Varying numbers of columns in clinical header (%d, %d)',
                    num_attrs,
                    len(row),
                    extra={'line_number': line_index + 1})
                return False

            for col_index, value in enumerate(row):

                # test for invalid values in these columns
                if value.strip().lower() in self.NULL_VALUES:
                    self.logger.error(
                        'Empty %s field in clinical attribute definition',
                        LINE_NAMES[line_index],
                        extra={'line_number': line_index + 1,
                               'column_number': col_index + 1,
                               'cause': value})
                    invalid_values = True
                if LINE_NAMES[line_index] in ('display_name', 'description'):
                    pass
                elif LINE_NAMES[line_index] == 'datatype':
                    VALID_DATATYPES = ('STRING', 'NUMBER', 'BOOLEAN')
                    if value not in VALID_DATATYPES:
                        self.logger.error(
                            'Invalid data type definition, must be one of '
                            '[%s]',
                            ', '.join(VALID_DATATYPES),
                            extra={'line_number': line_index + 1,
                                   'colum_number': col_index + 1,
                                   'cause': value})
                        invalid_values = True
                        invalid_values = True
                elif LINE_NAMES[line_index] == 'priority':
                    try:
                        if int(value) < 1:
                            raise ValueError()
                    except ValueError:
                        self.logger.error(
                            'Priority definition is not a positive integer',
                            extra={'line_number': line_index + 1,
                                   'column_number': col_index + 1,
                                   'cause': value})
                        invalid_values = True
                else:
                    raise RuntimeError('Unknown clinical header line name')

                attr_defs[col_index][LINE_NAMES[line_index]] = value

        self.attr_defs = attr_defs
        return not invalid_values

    def checkHeader(self, cols):

        """Validate the attributes defined in the column headers and above."""

        num_errors = super(ClinicalValidator, self).checkHeader(cols)

        if self.numCols != len(self.attr_defs):
            self.logger.error(
                'Varying numbers of columns in clinical header (%d, %d)',
                len(self.attr_defs),
                len(self.cols),
                extra={'line_number': self.line_number})
            num_errors += 1
        for col_index, col_name in enumerate(self.cols):
            if not col_name.isupper():
                self.logger.warning(
                    "Clinical attribute name not in all caps",
                    extra={'line_number': self.line_number,
                           'cause': col_name})
            # do not check the special ID columns as attributes against the db,
            # just parse them with the correct data type
            if col_name in ('PATIENT_ID', 'SAMPLE_ID'):
                self.attr_defs[col_index] = {'display_name': '',
                                             'description': '',
                                             'datatype': 'STRING',
                                             'priority': '0'}
                continue
            # skip all further checks for this column if portal info is absent
            if self.portal.clinical_attribute_dict is None:
                continue
            # look up how the attribute is defined in the portal
            srv_attr_properties = self.portal.clinical_attribute_dict.get(
                                      col_name)
            if srv_attr_properties is None:
                self.logger.warning(
                    'New %s-level attribute will be added to the portal',
                    {'0': 'sample', '1': 'patient'}[
                            self.PROP_IS_PATIENT_ATTRIBUTE],
                    extra={'line_number': self.line_number,
                           'column_number': col_index + 1,
                           'cause': col_name})
                self.newly_defined_attributes.add(col_name)
            # disallow homonymous patient-level and sample-level attributes,
            # except for the patient ID by which samples reference a patient
            elif col_name != 'PATIENT_ID' and (
                    srv_attr_properties['is_patient_attribute'] !=
                    self.PROP_IS_PATIENT_ATTRIBUTE):
                self.logger.error(
                    'Attribute is defined in the portal installation as a '
                    '%s-level attribute',
                    {'0': 'sample', '1': 'patient'}[
                            srv_attr_properties['is_patient_attribute']],
                    extra={'line_number': self.line_number,
                           'column_number': col_index + 1,
                           'cause': col_name, 
                           'show_all_values': True})
            else:
                # compare values defined in the file with the existing ones
                for attr_property in self.attr_defs[col_index]:
                    value = self.attr_defs[col_index][attr_property]
                    if value != srv_attr_properties[attr_property]:
                        self.logger.warning(
                            "%s definition for attribute '%s' does not match "
                            "the portal, and will be loaded as '%s'",
                            attr_property,
                            col_name,
                            srv_attr_properties[attr_property],
                            extra={'line_number': self.attr_defs[col_index].keys().index(attr_property) + 1,
                                   'column_number': col_index + 1,
                                   'cause': value})
                        # continue validation assuming the value in the portal
                        self.attr_defs[col_index][attr_property] = \
                            srv_attr_properties[attr_property]

        return num_errors

    def checkLine(self, data):
        """Check the values in a line of data."""
        super(ClinicalValidator, self).checkLine(data)
        for col_index, col_name in enumerate(self.cols):
            # treat cells beyond the end of the line as blanks,
            # super().checkLine() has already logged an error
            value = ''
            if col_index < len(data):
                value = data[col_index].strip()
            
            according_to_portal = ''
            data_type = self.attr_defs[col_index]['datatype']
            if col_name not in self.newly_defined_attributes:
                # Extra info for existing fields to make it clear that the 
                # check is being done based on the definition found in the portal:
                according_to_portal = 'According to portal, attribute should be loaded as %s. '%(data_type)
            
            # if not blank, check if values match the datatype
            if value.strip().lower() in self.NULL_VALUES:
                pass
            elif data_type == 'NUMBER':
                if not self.checkFloat(value):
                    self.logger.error(
                        according_to_portal + 'Value of attribute to be loaded as NUMBER is not a real number',                        
                        extra={'line_number': self.line_number,
                               'column_number': col_index + 1,
                               'column_name': col_name,
                               'cause': value})
            elif data_type == 'BOOLEAN':
                # TODO: check whether these are the values understood by portal
                VALID_BOOLEANS = ('TRUE', 'FALSE')
                if not value in VALID_BOOLEANS:
                    self.logger.error(
                        according_to_portal + 'Invalid value of attribute to be loaded as BOOLEAN, must be one '
                        'of [%s]',
                        ', '.join(VALID_BOOLEANS),
                        extra={'line_number': self.line_number,
                               'column_number': col_index + 1,
                               'column_name': col_name,
                               'cause': value})
            # make sure that PATIENT_ID is present
            if col_name == 'PATIENT_ID':
                if value.strip().lower() in self.NULL_VALUES:
                    self.logger.error(
                        'Missing PATIENT_ID',
                        extra={'line_number': self.line_number,
                               'column_number': col_index + 1,
                               'cause': value})


class SampleClinicalValidator(ClinicalValidator):

    """Validator for files defining and setting sample-level attributes."""

    REQUIRED_HEADERS = ['SAMPLE_ID', 'PATIENT_ID']
    PROP_IS_PATIENT_ATTRIBUTE = '0'

    def __init__(self, *args, **kwargs):
        """Initialize the validator to track sample ids defined."""
        super(SampleClinicalValidator, self).__init__(*args, **kwargs)
        self.sample_id_lines = {}
        self.sampleIds = self.sample_id_lines.viewkeys()
        self.patient_ids = set()

    def checkLine(self, data):
        """Check the values in a line of data."""
        super(SampleClinicalValidator, self).checkLine(data)
        for col_index, col_name in enumerate(self.cols):
            # treat cells beyond the end of the line as blanks,
            # super().checkLine() has already logged an error
            value = ''
            if col_index < len(data):
                value = data[col_index].strip()
            if col_name == 'SAMPLE_ID':
                if value.strip().lower() in self.NULL_VALUES:
                    self.logger.error(
                        'Missing SAMPLE_ID',
                        extra={'line_number': self.line_number,
                               'column_number': col_index + 1,
                               'cause': value})
                    continue
                if ' ' in value:
                    self.logger.error(
                        'White space in SAMPLE_ID is not supported',
                        extra={'line_number': self.line_number,
                               'column_number': col_index + 1,
                               'cause': value})
                if value in self.sample_id_lines:
                    if value.startswith('TCGA-'):
                        self.logger.warning(
                            'TCGA sample defined twice in clinical file, this '
                            'line will be ignored assuming truncated barcodes',
                            extra={
                                'line_number': self.line_number,
                                'column_number': col_index + 1,
                                'cause': '%s (already defined on line %d)' % (
                                        value,
                                        self.sample_id_lines[value])})
                    else:
                        self.logger.error(
                            'Sample defined twice in clinical file',
                            extra={
                                'line_number': self.line_number,
                                'column_number': col_index + 1,
                                'cause': '%s (already defined on line %d)' % (
                                    value,
                                    self.sample_id_lines[value])})
                else:
                    self.sample_id_lines[value] = self.line_number
            elif col_name == 'PATIENT_ID':
                self.patient_ids.add(value)
            # TODO: check the values in the other documented columns


class PatientClinicalValidator(ClinicalValidator):

    """Validator for files defining and setting patient-level attributes."""

    REQUIRED_HEADERS = ['PATIENT_ID']
    PROP_IS_PATIENT_ATTRIBUTE = '1'

    def __init__(self, *args, **kwargs):
        """Initialize the validator to track patient IDs referenced."""
        super(PatientClinicalValidator, self).__init__(*args, **kwargs)
        self.patient_id_lines = {}

    def checkHeader(self, cols):
        """Validate headers in patient-specific clinical data files."""
        num_errors = super(PatientClinicalValidator, self).checkHeader(cols)
        # do not allow the SAMPLE_ID column in this file
        if 'SAMPLE_ID' in self.cols:
            self.logger.error(
                'SAMPLE_ID column found in a patient attribute file',
                extra={'line_number': self.line_number,
                       'column_number': self.cols.index('SAMPLE_ID'),
                       'cause': 'SAMPLE_ID'})
        # refuse to define attributes also defined in the sample-level file
        for new_attribute in self.newly_defined_attributes:
            if new_attribute in DEFINED_SAMPLE_ATTRIBUTES:
                # log this as a file-aspecific error, using the base logger
                self.logger.logger.error(
                    'New clinical attribute defined both as sample-level and '
                    'as patient-level',
                    extra={'cause': new_attribute})
        # warnings about missing optional columns
        if 'OS_MONTHS' not in self.cols or 'OS_STATUS' not in self.cols:
            self.logger.warning(
                'Columns OS_MONTHS and/or OS_STATUS not found. Overall '
                'survival analysis feature will not be available for this '
                'study.')
        if 'DFS_MONTHS' not in self.cols or 'DFS_STATUS' not in self.cols:
            self.logger.warning(
                'Columns DFS_MONTHS and/or DFS_STATUS not found. Disease '
                'free analysis feature will not be available for this study.')
        return num_errors

    def checkLine(self, data):
        """Check the values in a line of data."""
        super(PatientClinicalValidator, self).checkLine(data)
        for col_index, col_name in enumerate(self.cols):
            # treat cells beyond the end of the line as blanks,
            # super().checkLine() has already logged an error
            value = ''
            if col_index < len(data):
                value = data[col_index].strip()
            if col_name == 'PATIENT_ID':
                if ' ' in value:
                    self.logger.error(
                        'White space in PATIENT_ID is not supported',
                        extra={'line_number': self.line_number,
                               'column_number': col_index + 1,
                               'cause': value})
                if value in self.patient_id_lines:
                    self.logger.error(
                        'Patient defined multiple times in file',
                        extra={
                            'line_number': self.line_number,
                            'column_number': self.cols.index('PATIENT_ID') + 1,
                            'cause': '%s (already defined on line %d)' % (
                                    value,
                                    self.patient_id_lines[value])})
                else:
                    self.patient_id_lines[value] = self.line_number
                    if value not in PATIENTS_WITH_SAMPLES:
                        self.logger.warning(
                            'Clinical data defined for a patient with '
                            'no samples',
                            extra={'line_number': self.line_number,
                                   'column_number': col_index + 1,
                                   'cause': value})
            # TODO: check the values for other documented columns

    def onComplete(self):
        """Perform final validations based on the data parsed."""
        for patient_id in PATIENTS_WITH_SAMPLES:
            if patient_id not in self.patient_id_lines:
                self.logger.warning(
                    'Missing clinical data for a patient associated with '
                    'samples',
                    extra={'cause': patient_id})
        super(PatientClinicalValidator, self).onComplete()


class SegValidator(Validator):
    """Validator for .seg files."""

    REQUIRED_HEADERS = [
        'ID',
        'chrom',
        'loc.start',
        'loc.end',
        'num.mark',
        'seg.mean']
    REQUIRE_COLUMN_ORDER = True

    def __init__(self, *args, **kwargs):
        """Initialize validator to track coverage of the genome."""
        super(SegValidator, self).__init__(*args, **kwargs)
        self.chromosome_lengths = self.load_chromosome_lengths(
            self.meta_dict['reference_genome_id'],
            self.logger.logger)
        # add 23 and 24 "chromosomes" as aliases to X and Y, respectively:
        self.chromosome_lengths['23'] = self.chromosome_lengths['X']
        self.chromosome_lengths['24'] = self.chromosome_lengths['Y']

    def checkLine(self, data):
        super(SegValidator, self).checkLine(data)

        parsed_coords = {}
        for col_index, col_name in enumerate(self.cols):
            value = data[col_index].strip()
            if col_name == 'ID':
                self.checkSampleId(value, column_number=col_index + 1)
            elif col_name == 'chrom':
                if value in self.chromosome_lengths:
                    parsed_coords[col_name] = value
                else:
                    self.logger.error(
                        ('Unknown chromosome, must be one of (%s)' %
                         '|'.join(self.chromosome_lengths.keys())),
                        extra={'line_number': self.line_number,
                               'column_number': col_index + 1,
                               'cause': value})
            elif col_name in ('loc.start', 'loc.end'):
                try:
                    parsed_coords[col_name] = int(value)
                except ValueError:
                    self.logger.error(
                        'Genomic position is not an integer',
                        extra={'line_number': self.line_number,
                               'column_number': col_index + 1,
                               'cause': value})
                    # skip further validation specific to this column
                    continue
                # 0 is the first base, and loc.end is not part of the segment
                # 'chrom' has already been read, as column order is fixed
                if parsed_coords[col_name] < 0 or (
                        'chrom' in parsed_coords and
                        parsed_coords[col_name] > self.chromosome_lengths[
                                                      parsed_coords['chrom']]):
                    self.logger.error(
                        'Genomic position beyond end of chromosome '
                        '(chr%s:0-%s)',
                        parsed_coords['chrom'],
                        self.chromosome_lengths[parsed_coords['chrom']],
                        extra={'line_number': self.line_number,
                               'column_number': col_index + 1,
                               'cause': value})
                    # not a valid coordinate usable in further validations
                    del parsed_coords[col_name]
            elif col_name == 'num.mark':
                if not self.checkInt(value):
                    self.logger.error(
                        'Number of probes is not an integer',
                        extra={'line_number': self.line_number,
                               'column_number': col_index + 1,
                               'cause': value})
            elif col_name == 'seg.mean':
                if not self.checkFloat(value):
                    self.logger.error(
                        'Mean segment copy number is not a number',
                        extra={'line_number': self.line_number,
                               'column_number': col_index + 1,
                               'cause': value})
            else:
                raise RuntimeError('Could not validate column type: ' +
                                   col_name)

        if 'loc.start' in parsed_coords and 'loc.end' in parsed_coords:
            # the convention for genomic coordinates (at least at UCSC) is that
            # the chromosome starts at 0 and end positions are excluded.
            # see also https://groups.google.com/forum/#!topic/igv-help/LjffjxPul2M
            if parsed_coords['loc.start'] == parsed_coords['loc.end']:
                self.logger.warning(
                    'Segment is zero bases wide and will not be loaded',
                    extra={'line_number': self.line_number,
                           'cause': '{}-{}'.format(parsed_coords['loc.start'],
                                                   parsed_coords['loc.end'])})
            elif parsed_coords['loc.start'] > parsed_coords['loc.end']:
                self.logger.error(
                    'Start position of segment is greater than end position',
                    extra={'line_number': self.line_number,
                           'cause': '{}-{}'.format(parsed_coords['loc.start'],
                                                   parsed_coords['loc.end'])})

        # TODO check for overlap and low genome coverage
        # this could be implemented by sorting the segments for a patient
        # by (chromosome and) start position and checking if the start position
        # of each segment comes after the end position of the previous one,
        # meanwhile adding up the number of (non-overlapping) bases covered on
        # that chromosome in that patient.

    @staticmethod
    def load_chromosome_lengths(genome_build, logger):

        """Get the length of each chromosome from USCS and return a dict.

        The dict will not include unplaced contigs, alternative haplotypes or
        the mitochondrial chromosome.
        """

        chrom_size_dict = {}
        chrom_size_url = (
            'http://hgdownload.cse.ucsc.edu'
            '/goldenPath/{build}/bigZips/{build}.chrom.sizes').format(
                build=genome_build)
        logger.debug("Retrieving chromosome lengths from '%s'",
                     chrom_size_url)
        r = requests.get(chrom_size_url)
        try:
            r.raise_for_status()
        except requests.exceptions.HTTPError as e:
            raise IOError('Error retrieving chromosome lengths from UCSC: ' +
                          e.message)
        for line in r.text.splitlines():
            try:
                # skip comment lines
                if line.startswith('#'):
                    continue
                cols = line.split('\t', 1)
                if not (len(cols) == 2 and
                        cols[0].startswith('chr')):
                    raise IOError()
                # skip unplaced sequences
                if cols[0].endswith('_random') or cols[0].startswith('chrUn_'):
                    continue
                # skip entries for alternative haplotypes
                if re.search(r'_hap[0-9]+$', cols[0]):
                    continue
                # skip the mitochondrial chromosome
                if cols[0] == 'chrM':
                    continue

                # remove the 'chr' prefix
                chrom_name = cols[0][3:]
                try:
                    chrom_size = int(cols[1])
                except ValueError:
                    raise IOError()
                chrom_size_dict[chrom_name] = chrom_size
            except IOError:
                raise IOError(
                    "Unexpected response from {url}: {line}".format(
                        url=chrom_size_url, line=repr(line)))
        return chrom_size_dict


class ContinuousValuesValidator(GenewiseFileValidator):
    """Validator for matrix files mapping floats to gene/sample combinations.

    Allowing missing values indicated by GenewiseFileValidator.NULL_VALUES.
    """
    
    def checkValue(self, value, col_index):
        """Check a value in a sample column."""
        stripped_value = value.strip()
        if stripped_value not in self.NULL_VALUES and not self.checkFloat(stripped_value):
            self.logger.error("Value is neither a real number nor " + ', '.join(self.NULL_VALUES),
                              extra={'line_number': self.line_number,
                                     'column_number': col_index + 1,
                                     'cause': value})


class FusionValidator(Validator):

    REQUIRED_HEADERS = [
        'Hugo_Symbol',
        'Entrez_Gene_Id',
        'Center',
        'Tumor_Sample_Barcode',
        'Fusion',
        'DNA support',
        'RNA support',
        'Method',
        'Frame']
    REQUIRE_COLUMN_ORDER = True

    def checkLine(self, data):
        super(FusionValidator, self).checkLine(data)
        # TODO check the values


class MutationSignificanceValidator(Validator):

    # TODO add checks for mutsig files
    ALLOW_BLANKS = True
    pass


class RPPAValidator(FeaturewiseFileValidator):

    REQUIRED_HEADERS = ['Composite.Element.REF']
    ALLOW_BLANKS = True
    NULL_VALUES = ["NA"]

    def parseFeatureColumns(self, nonsample_col_vals):
        """Check the IDs in the first column."""
        # the ID consists of a space-separated list of gene symbols and/or
        # Entrez identifiers, separated by a pipe symbol from the name of the
        # antibody probe used to detect these genes. The values on the line
        # will be loaded for each gene in the list, or for fictional genes that
        # encode specific phosphorylated versions of the genes' protein
        # products if the antibody name has a particular format.
        value = nonsample_col_vals[0].strip()
        if '|' not in value:
            self.logger.error('No pipe symbol in RPPA probe column',
                              extra={'line_number': self.line_number,
                                     'column_number': 1,
                                     'cause': nonsample_col_vals[0]})
            return None
        symbol_element, antibody = value.split('|', 1)
        symbol_list = symbol_element.split(' ')
        for symbol in symbol_list:
            entrez_id = None
            if symbol.strip() == 'NA':
                self.logger.warning(
                'Gene symbol NA will be ignored, assuming Not Available',
                extra={'line_number': self.line_number,
                       'column_number': 1,
                       'cause': nonsample_col_vals[0]})
            elif self.checkInt(symbol):
                entrez_id = self.checkGeneIdentification(entrez_id=symbol)
            else:
                entrez_id = self.checkGeneIdentification(gene_symbol=symbol)
            # TODO: return a value for (this phospo-version of) each gene
        return antibody

    def checkValue(self, value, col_index):
        """Check a value in a sample column."""
        stripped_value = value.strip()
        if stripped_value not in self.NULL_VALUES and not self.checkFloat(stripped_value):
            self.logger.error("Value is neither a real number nor " + ', '.join(self.NULL_VALUES),
                              extra={'line_number': self.line_number,
                                     'column_number': col_index + 1,
                                     'cause': value})


class TimelineValidator(Validator):

    REQUIRED_HEADERS = [
        'PATIENT_ID',
        'START_DATE',
        'STOP_DATE',
        'EVENT_TYPE']
    REQUIRE_COLUMN_ORDER = True

    def checkLine(self, data):
        super(TimelineValidator, self).checkLine(data)
        # TODO check the values

class CancerTypeValidator(Validator):

    """Validator for tab-separated cancer type definition files."""

    REQUIRED_HEADERS = []
    REQUIRE_COLUMN_ORDER = True

    COLS = (
        'type_of_cancer',
        'name',
        'clinical_trial_keywords',
        'color',
        'parent_type_of_cancer'
    )

    def __init__(self, *args, **kwargs):
        """Initialize a file validator with a defined_cancer_types field."""
        super(CancerTypeValidator, self).__init__(*args, **kwargs)
        self.cols = self.__class__.COLS
        self.numCols = len(self.cols)
        self.defined_cancer_types = []

    def checkHeader(self, cols):
        """Check the first uncommented line just like any other data line."""
        return self.checkLine(cols)

    def checkLine(self, data):
        """Check a data line in a cancer type file."""
        # track whether any errors are emitted while validating this line
        tracking_handler = MaxLevelTrackingHandler()
        self.logger.logger.addHandler(tracking_handler)
        try:
            super(CancerTypeValidator, self).checkLine(data)
            if len(data) != 5:
                self.logger.error('Lines in cancer type files must have these '
                                  '5 columns, in order: [%s]',
                                  ', '.join(self.cols),
                                  extra={'line_number': self.line_number,
                                         'cause': '<%d columns>' % len(data)})
                # no assumptions can be made about the meaning of each column
                return
            line_cancer_type = data[self.cols.index('type_of_cancer')].lower().strip()
            # check each column
            for col_index, field_name in enumerate(self.cols):
                # TODO validate whether the color field is one of the
                # keywords on https://www.w3.org/TR/css3-color/#svg-color
                if field_name == 'parent_type_of_cancer':
                    parent_cancer_type = data[col_index].lower().strip()
                    # if parent_cancer_type is not 'tissue' (which is a special case when building the oncotree), 
                    # then give error if the given parent is not found in the DB or in the given cancer types of the
                    # current study:
                    if (parent_cancer_type != 'tissue' and 
                        self.portal.cancer_type_dict is not None and not
                            (parent_cancer_type in self.portal.cancer_type_dict or
                             parent_cancer_type in self.defined_cancer_types)):
                        self.logger.error(
                            "Unknown parent for cancer type '%s'",
                            line_cancer_type,
                            extra={'line_number': self.line_number,
                                   'column_number': col_index + 1,
                                   'cause': data[col_index]})
            # check for duplicated (possibly inconsistent) cancer types
            if line_cancer_type in self.defined_cancer_types:
                self.logger.error(
                    'Cancer type defined a second time in this file',
                    extra={'line_number': self.line_number,
                           'cause': line_cancer_type})
            # compare the cancer_type definition with the portal instance
            if (self.portal.cancer_type_dict is not None and
                    line_cancer_type in self.portal.cancer_type_dict):
                existing_info = self.portal.cancer_type_dict[line_cancer_type]
                # depending on version, the API may not return this field
                if 'short_name' in existing_info:
                    if existing_info['short_name'].lower() != line_cancer_type:
                        self.logger.error(
                            "Attempting to validate against invalid cancer type "
                            "in portal: short name '%s' does not match id '%s'",
                            existing_info['short_name'],
                            line_cancer_type,
                            extra={'line_number': self.line_number})
                        return
                for col_index, field_name in enumerate(self.cols):
                    value = data[col_index]
                    # this field is loaded into the database in lowercase
                    if field_name == 'parent_type_of_cancer':
                        value = value.lower()
                    if (
                            field_name in existing_info and
                            value != existing_info[field_name]):
                        self.logger.error(
                            "'%s' field of cancer type '%s' does not match "
                            "the portal, '%s' expected",
                            field_name,
                            line_cancer_type,
                            existing_info[field_name],
                            extra={'line_number': self.line_number,
                                   'column_number': col_index + 1,
                                   'cause': value})
            elif self.portal.cancer_type_dict is not None:
                self.logger.warning(
                    'New disease type will be added to the portal',
                    extra={'line_number': self.line_number,
                           'cause': line_cancer_type})
            # if no errors have been emitted while validating this line
            if tracking_handler.max_level < logging.ERROR:
                # add the cancer type defined on this line to the list
                self.defined_cancer_types.append(line_cancer_type)
        finally:
            self.logger.logger.removeHandler(tracking_handler)


class GisticGenesValidator(Validator):

    """Validator for files with information aggregated from GISTIC output.

    This file type is produced by the cBioPortal data transformation pipelines,
    based on the `table_{amp|del}.conf_*.txt` files in combination with data
    from `{amp|del}_genes_conf_*.txt`.
    """

    REQUIRED_HEADERS = [
        'chromosome',
        'peak_start',
        'peak_end',
        'genes_in_region',
        'amp',
        'cytoband',
        'q_value']

    REQUIRE_COLUMN_ORDER = False
    ALLOW_BLANKS = True
    NULL_VALUES = ['']

    def __init__(self, *args, **kwargs):
        """Initialize a GisticGenesValidator with the given parameters."""
        super(GisticGenesValidator, self).__init__(*args, **kwargs)
        # checkLine() expects particular values here, for the 'amp' column
        if not self.meta_dict['reference_genome_id'].startswith('hg'):
            raise RuntimeError(
                    "GisticGenesValidator requires the metadata field "
                    "reference_genome_id to start with 'hg'")
        if self.meta_dict['genetic_alteration_type'] not in (
                'GISTIC_GENES_AMP', 'GISTIC_GENES_DEL'):
            raise RuntimeError(
                    "Genetic alteration type '{}' not supported by "
                    "GisticGenesValidator.".format(
                    self.meta_dict['genetic_alteration_type']))

    def checkLine(self, data):

        """Check the values on a data line."""

        super(GisticGenesValidator, self).checkLine(data)
        # properties to be validated in relation to each other if
        # individually sensible values are found
        parsed_chromosome = None
        parsed_peak_start = None
        parsed_peak_end = None
        parsed_gene_list = None
        cytoband_chromosome = None
        parsed_cytoband = None

        # perform specific validations for each known column
        for col_index, col_name in enumerate(self.cols):
            # treat cells beyond the end of the line as blanks,
            # super().checkLine() has already logged an error
            value = ''
            if col_index < len(data):
                value = data[col_index]
            # of the required columns, only genes_in_region can be blank
            if ((col_name in self.REQUIRED_HEADERS and
                        col_name != 'genes_in_region') and
                    value.strip() in self.NULL_VALUES):
                self.logger.error("Empty cell in column '%s'",
                                  col_name,
                                  extra={'line_number': self.line_number,
                                         'column_number': col_index + 1,
                                         'cause': value})
                # skip to the next column
                continue
            if col_name == 'chromosome':
                parsed_chromosome = self.parse_chromosome_num(
                        value, column_number=col_index + 1)
            elif col_name == 'peak_start':
                parsed_peak_start = self.parse_genomic_coord(
                        value, column_number=col_index + 1)
            elif col_name == 'peak_end':
                parsed_peak_end = self.parse_genomic_coord(
                        value, column_number=col_index + 1)
            elif col_name == 'genes_in_region':
                parsed_gene_list = self.parse_gene_list(
                        value, column_number=col_index + 1)
            elif col_name == 'amp':
                self.parse_amp_value(
                    value, column_number=col_index + 1)
            elif col_name == 'cytoband':
                cytoband_chromosome, parsed_cytoband = self.parse_cytoband(
                        value, column_number=col_index + 1)
            elif col_name == 'q_value':
                self.parse_q_value(
                    value, column_number=col_index + 1)

        # check if the start and the end of the peak are in the right order
        if parsed_peak_start is not None and parsed_peak_end is not None:
            if parsed_peak_start > parsed_peak_end:
                # is an error according to UCSC "0" convention, end location excluded.
                # see also https://groups.google.com/forum/#!topic/igv-help/LjffjxPul2M
                self.logger.error(
                    'Start position of peak is not lower than end position',
                    extra={'line_number': self.line_number,
                           'cause': '{}/{}'.format(parsed_peak_start,
                                                     parsed_peak_end)})
            elif parsed_peak_end == parsed_peak_start:
                # cBioPortal seems to filter out regions in which the narrow
                # peak (based on all samples) is 0 bases wide. I have seen
                # examples of peaks of length 0 at the end position of the
                # corresponding `wide peak' in Firehose data.
                self.logger.warning(
                    'Peak is 0 bases wide and will not be shown in cBioPortal',
                    extra={'line_number': self.line_number,
                           'cause': '{}-{}'.format(parsed_peak_start,
                                                     parsed_peak_end)})


        # check coordinates with the cytoband specification
        if cytoband_chromosome and parsed_cytoband:
            if parsed_chromosome:
                if cytoband_chromosome != parsed_chromosome:
                    self.logger.error(
                        'Cytoband and chromosome specifications do not match',
                        extra={'line_number': self.line_number,
                               'cause': '(%s%s, %s)' %
                                   (cytoband_chromosome,
                                    parsed_cytoband,
                                    parsed_chromosome)})
            # TODO: validate band/coord sets with the UCSC cytoband definitions (using 
            # parsed_gene_list and some of the other parsed_*list variables 

    def parse_chromosome_num(self, value, column_number):
        """Parse a chromosome number, logging any errors for this column

        Return the parsed value if valid, None otherwise.
        """
        # TODO: check if the chromosome exists in the UCSC cytobands file
        return value

    def parse_genomic_coord(self, value, column_number):
        """Parse a genomic coordinate, logging any errors for this column.

        Return the parsed value if valid, None otherwise.
        """
        parsed_value = None
        try:
            parsed_value = int(value)
        except ValueError:
            self.logger.error("Genomic position is not an integer",
                              extra={'line_number': self.line_number,
                                     'column_number': column_number,
                                     'cause': value})
        return parsed_value

    def parse_gene_list(self, value, column_number):
        """Parse a csv gene symbol list, logging any errors for this column.

        Return the parsed value if valid, None otherwise.
        """
        comma_sep_list = value.strip()
        # ignore any trailing comma
        if comma_sep_list.endswith(','):
            comma_sep_list = comma_sep_list[:-1]
        # list to collect parseable gene symbols
        parsed_gene_list = []
        # give a custom warning if the list is empty
        if comma_sep_list.strip() == '':
            self.logger.warning(
                "No genes listed in GISTIC copy-number altered region",
                extra={'line_number': self.line_number,
                       'column_number': column_number,
                       'cause': value})
        else:
            # loop over the comma-separated list of gene symbols. Example of such a 
            # list: RNA5SP149,snoU13|ENSG00000239096.1,GNB4
            for symbol in comma_sep_list.split(','):
                symbol = symbol.strip()
                # remove the | and trailing part if any (e.g. 
                # remove |ENSG00000239096.1 from snoU13|ENSG00000239096.1):
                symbol = symbol.split('|')[0]
                # add valid, unambiguous gene symbols to the list,
                # while logging errors about unresolvable ones
                # TODO: allow blanks if possible after this fix:
                # https://github.com/cBioPortal/cbioportal/issues/884
                if self.checkGeneIdentification(symbol, entrez_id=None):
                    parsed_gene_list.append(symbol)

    def parse_amp_value(self, value, column_number):
        """Parse an `amp` column flag, logging any errors for this column.

        Return the parsed value if valid, None otherwise.
        """
        # 1 for _AMP, 0 for _DEL
        expected_value = str(int(
                self.meta_dict['genetic_alteration_type'] ==
                    'GISTIC_GENES_AMP'))
        if value != expected_value:
            self.logger.error(
                "'amp' column must be '%s' in files of genetic "
                "alteration type '%s'",
                expected_value,
                self.meta_dict['genetic_alteration_type'],
                extra={'line_number': self.line_number,
                       'column_number': column_number,
                       'cause': value})
            return None
        else:
            return int(value)

    def parse_cytoband(self, value, column_number):
        """Parse a cytoband with chromosome, logging any errors for this col.

        Return a tuple of the chromosome number and the cytoband specification
        if valid, a tuple of Nones otherwise.
        """
        chromosome_num = None
        cytoband = None
        # find the index of the (first) p or otherwise q, the arm
        arm_index = value.find('p')
        if arm_index == -1:
            arm_index = value.find('q')
        if arm_index == -1:
            self.logger.error(
                "Cytoband specification contains no 'p' or 'q'",
                extra={'line_number': self.line_number,
                       'column_number': column_number,
                       'cause': value})
        else:
            chromosome_num = value[:arm_index]
            cytoband = value[arm_index:]
        if chromosome_num is not None and chromosome_num == '':
            self.logger.error(
                'Cytoband specification does not include the chromosome',
                extra={'line_number': self.line_number,
                       'column_number': column_number,
                       'cause': value})
            chromosome_num, cytoband = None, None
        # TODO: check if the cytoband exists in the UCSC cytobands file
        return chromosome_num, cytoband

    def parse_q_value(self, value, column_number):
        """Parse a q-value (numeral), logging any errors for this colum.

        Return the parsed value if valid, None otherwise.
        """
        parsed_value = None
        value_invalid = False
        try:
            parsed_value = float(value)
        except ValueError:
            self.logger.error('q-value is not a real number',
                              extra={'line_number': self.line_number,
                                     'column_number': column_number,
                                     'cause': value})
            value_invalid = True
        if not value_invalid and (not 0 <= parsed_value <= 1):
            self.logger.error('q-value is not between 0 and 1',
                              extra={'line_number': self.line_number,
                                     'column_number': column_number,
                                     'cause': value})
        if value_invalid:
            return None
        else:
            return parsed_value


# ------------------------------------------------------------------------------
# Functions

# FIXME: returning simple valid (meta_fn, data_fn) pairs would be cleaner,
# Validator objects can be instantiated with a portal instance elsewhere
def process_metadata_files(directory, portal_instance, logger):

    """Parse the meta files in a directory and create data file validators.

    Return a tuple of:
        1. a dict listing the data file validator (or None) for each meta file
            by file type,
        2. a dict mapping any case list IDs defined *outside* of the case list
            directory to paths of the files in which they were defined
        3. the cancer type of the study, and
        4. the study id

    Possible file types are listed in cbioportal_common.MetaFileTypes.
    """

    # get filenames for all meta files in the directory
    filenames = [os.path.join(directory, f) for
                 f in os.listdir(directory) if
                 re.search(r'(\b|_)meta(\b|[_0-9])', f,
                           flags=re.IGNORECASE) and
                 not f.startswith('.') and 
                 not f.endswith('~')]
    
    if len(filenames) == 0:
        logger.critical(
                    'No meta files found in ' + directory +'. Please make sure the directory '\
                    'is the path to the folder containing the files.')

    study_id = None
    study_cancer_type = None
    validators_by_type = {}
    case_list_suffix_fns = {}
    stable_ids = []

    for filename in filenames:

        meta, meta_file_type = cbioportal_common.parse_metadata_file(
            filename, logger, study_id, GENOMIC_BUILD_NAME)
        if meta_file_type is None:
            continue
        # validate stable_id to be unique (check can be removed once we deprecate this field):
        if 'stable_id' in meta:
            stable_id = meta['stable_id']
            if stable_id in stable_ids:
                # stable id already used in other meta file, give error:
                logger.error(
                    'stable_id repeated. It should be unique across all files in a study',
                    extra={'filename_': filename,
                           'cause': stable_id})
            else:
                stable_ids.append(stable_id)
        if study_id is None and 'cancer_study_identifier' in meta:
            study_id = meta['cancer_study_identifier']
        if meta_file_type == cbioportal_common.MetaFileTypes.STUDY:
            if study_cancer_type is not None:
                logger.error(
                    'Encountered a second meta_study file',
                    extra={'filename_': filename})
            else:
                study_cancer_type = meta['type_of_cancer']
                if ('add_global_case_list' in meta and
                        meta['add_global_case_list'].lower() == 'true'):
                    case_list_suffix_fns['all'] = filename

        # create a list for the file type in the dict
        if meta_file_type not in validators_by_type:
            validators_by_type[meta_file_type] = []
        # check if data_filename is set AND if data_filename is a supported field according to META_FIELD_MAP:
        if 'data_filename' in meta and 'data_filename' in cbioportal_common.META_FIELD_MAP[meta_file_type]:
            validator_class = globals()[VALIDATOR_IDS[meta_file_type]]
            validator = validator_class(directory, meta,
                                        portal_instance, logger)
            validators_by_type[meta_file_type].append(validator)
        else:
            validators_by_type[meta_file_type].append(None)

    if study_cancer_type is None:
        logger.error(
            'Cancer type needs to be defined for a study. Verify that you have a study file '
            'and have defined the cancer type correctly.')

    # prepend the cancer study id to any case list suffixes
    defined_case_list_fns = {}
    if study_id is not None:
        for suffix in case_list_suffix_fns:
            defined_case_list_fns[study_id + '_' + suffix] = \
                case_list_suffix_fns[suffix]

    return (validators_by_type, defined_case_list_fns,
            study_cancer_type, study_id)


def processCaseListDirectory(caseListDir, cancerStudyId, logger,
                             stableid_files=None):
    """Validate the case lists in a directory and log findings.

    Args:
        caseListDir (str): path to the case list directory.
        cancerStudyId (str): cancer_study_identifier expected in the files.
        logger: logging.Logger instance through which to send output.
        stableid_files (Optional): dict mapping the stable ids of any case
            lists already defined to the files they were defined in.
    """

    logger.debug('Validating case lists')

    # start with an empty dictionary if none was given
    # (using mutable objects as default arguments directly is confusing)
    if stableid_files == None:
        stableid_files = {}

    # TODO: include ids based on the defined profiles here
    required_id_suffixes = ('all', )
    required_stable_ids = [cancerStudyId + '_' + suffix for suffix in
                           required_id_suffixes]

    case_list_fns = [os.path.join(caseListDir, fn) for
                     fn in os.listdir(caseListDir) if
                     not (fn.startswith('.') or fn.endswith('~'))]

    for case in case_list_fns:

        case_data, meta_file_type = cbioportal_common.parse_metadata_file(
            case, logger, cancerStudyId, case_list=True)
        # skip if invalid, errors have already been emitted
        if meta_file_type is None:
            continue

        # check for duplicated stable ids
        stable_id = case_data['stable_id']
        if not stable_id.startswith(cancerStudyId + '_'):
            logger.error('Stable_id of case list does not start with the '
                         'study id (%s) followed by an underscore',
                         cancerStudyId,
                         extra={'filename_': case,
                                'cause': stable_id})
        elif stable_id in stableid_files:
            logger.error('Multiple case lists with this stable_id defined '
                         'in the study',
                extra={'filename_': case,
                       'cause': '%s (already defined in %s)' % (
                                stable_id,
                                os.path.relpath(stableid_files[stable_id],
                                                os.path.dirname(caseListDir)))})
        else:
            stableid_files[stable_id] = case

        sampleIds = case_data['case_list_ids']
        sampleIds = set([x.strip() for x in sampleIds.split('\t')])
        for value in sampleIds:
            if value not in DEFINED_SAMPLE_IDS:
                logger.error(
                    'Sample id not defined in clinical file',
                    extra={'filename_': case,
                           'cause': value})
            if ' ' in value:
                logger.error(
                    'White space in sample id is not supported',
                    extra={'filename_': case,
                           'cause': value})
                

    for required_id in required_stable_ids:
        if required_id not in stableid_files:
            if required_id == cancerStudyId + '_all':
                suggestion = ("Consider adding 'add_global_case_list: true' "
                              "to the study metadata file")
            else:
                suggestion = "Please define it in the 'case_lists' folder"
            logger.error("No  case list found for stable_id '%s'. %s",
                         required_id,
                         suggestion)

    logger.info('Validation of case lists complete')


def request_from_portal_api(server_url, api_name, logger):
    """Send a request to the portal API and return the decoded JSON object."""
    service_url = server_url + '/api/' + api_name
    logger.debug("Requesting %s from portal at '%s'",
                api_name, server_url)
    # this may raise a requests.exceptions.RequestException subclass,
    # usually because the URL provided on the command line was invalid or
    # did not include the http:// part
    response = requests.get(service_url)
    try:
        response.raise_for_status()
    except requests.exceptions.HTTPError as e:
        raise IOError(
            'Connection error for URL: {url}. Administrator: please check if '
            '[{url}] is accessible. Message: {msg}'.format(url=service_url,
                                                           msg=e.message))
    return response.json()


def read_portal_json_file(dir_path, api_name, logger):
    """Parse a JSON file named `api_name`.json in `dir_path`.

    Replacing any forward slashes in the API name by underscores.
    """
    parsed_json = None
    json_fn = os.path.join(dir_path, '{}.json'.format(
                                         api_name.replace('/', '_')))
    if os.path.isfile(json_fn):
        logger.debug('Reading portal information from %s',
                    json_fn)
        with open(json_fn, 'rU') as json_file:
            parsed_json = json.load(json_file)
    return parsed_json


def index_api_data(parsed_json, id_field):
    """Transform a list of dicts into a dict indexed by one of their fields.

    >>> index_api_data([{'id': 'eggs', 'val1': 42, 'foo': True},
    ...                     {'id': 'spam', 'val1': 1, 'foo': True}], 'id')
    {'eggs': {'val1': 42, 'foo': True}, 'spam': {'val1': 1, 'foo': True}}
    >>> index_api_data([{'id': 'eggs', 'val1': 42, 'foo': True},
    ...                     {'id': 'spam', 'val1': 1, 'foo': True}], 'val1')
    {1: {'foo': True, 'id': 'spam'}, 42: {'foo': True, 'id': 'eggs'}}
    """
    transformed_dict = {}
    for attr in parsed_json:
        # make a copy of the attr dict
        # remove id field:
        if not id_field in attr:
            raise RuntimeError("Field '{}' not found in json object".format(
                                   id_field))
        id_val = attr[id_field]
        if id_val in transformed_dict:
            raise RuntimeError("Identifier '{}' found more than once in json "
                               "object".format(id_val))
        # make a copy of the sub-dictionary without the id field
        attr_dict = dict(attr)
        del attr_dict[id_field]
        transformed_dict[id_val] = attr_dict
    return transformed_dict


def transform_symbol_entrez_map(json_data,
                                id_field='hugo_gene_symbol',
                                values_field='entrez_gene_id'):
    """Transform a list of homogeneous dicts into a dict of lists.

    Using the values of the `id_field` entries as the keys, mapping to lists
    of corresponding `values_field` entries.

    >>> transform_symbol_entrez_map(
    ...     [{"hugo_gene_symbol": "A1BG", "entrez_gene_id": 1},
    ...      {"hugo_gene_symbol": "A2M", "entrez_gene_id": 2}])
    {'A2M': [2], 'A1BG': [1]}
    >>> transform_symbol_entrez_map(
    ...     [{"gene_alias": "A1B", "entrez_gene_id": 1},
    ...      {"gene_alias": "ANG3", "entrez_gene_id": 738},
    ...      {"gene_alias": "ANG3", "entrez_gene_id": 9068}],
    ...     id_field="gene_alias")
    {'ANG3': [738, 9068], 'A1B': [1]}
    """
    result_dict = {}
    for data_item in json_data:
        symbol = data_item[id_field].upper()
        if symbol not in result_dict:
            result_dict[symbol] = []
        result_dict[symbol].append(
                data_item['entrez_gene_id'])
    return result_dict


def merge_clinical_attributes(patient_attr_dict, sample_attr_dict):
    """Merge two dicts, raising an exception if the keys overlap.

    >>> merge_clinical_attributes({"SEX":
    ...                                {"is_patient_attribute": 1},
    ...                            "AGE":
    ...                                {"is_patient_attribute": 1}},
    ...                           {"GLEASON_SCORE":
    ...                                {"is_patient_attribute": 0}})
    {'AGE': {'is_patient_attribute': 1}, 'GLEASON_SCORE': {'is_patient_attribute': 0}, 'SEX': {'is_patient_attribute': 1}}
    """
    # if this happens, the database structure has changed and this script
    # needs to be updated
    id_overlap = patient_attr_dict.viewkeys() & sample_attr_dict.viewkeys()
    if id_overlap:
        raise ValueError(
            'Portal data listed these clinical attributes '
            'both for samples and for patients: {}'.format(
                ', '.join(id_overlap)))
    # merge the sample attributes into the first dict
    patient_attr_dict.update(sample_attr_dict)
    return patient_attr_dict


def load_portal_info(path, logger, offline=False):
    """Create a PortalInstance object based on a server API or offline dir.

    If `offline` is True, interpret `path` as the path to a directory of JSON
    files. Otherwise expect `path` to be the URL of a cBioPortal server and
    use its web API.
    """
    portal_dict = {}
    for api_name, transform_function in (
            ('cancertypes',
                lambda json_data: index_api_data(json_data, 'id')),
            ('clinicalattributes/patients',
                lambda json_data: index_api_data(json_data, 'attr_id')),
            ('clinicalattributes/samples',
                lambda json_data: index_api_data(json_data, 'attr_id')),
            ('genes',
                lambda json_data: transform_symbol_entrez_map(
                                        json_data, 'hugo_gene_symbol')),
            ('genesaliases',
                lambda json_data: transform_symbol_entrez_map(
                                        json_data, 'gene_alias'))):
        if offline:
            parsed_json = read_portal_json_file(path, api_name, logger)
        else:
            parsed_json = request_from_portal_api(path, api_name, logger)
        if parsed_json is not None and transform_function is not None:
            parsed_json = transform_function(parsed_json)
        portal_dict[api_name] = parsed_json
    if all(d is None for d in portal_dict.values()):
        raise IOError('No portal information found at {}'.format(
                          path))
    # merge clinical attributes into a single dictionary
    clinical_attr_dict = None
    if (portal_dict['clinicalattributes/patients'] is not None and
            portal_dict['clinicalattributes/samples'] is not None):
        clinical_attr_dict = merge_clinical_attributes(
            portal_dict['clinicalattributes/patients'],
            portal_dict['clinicalattributes/samples'])
    return PortalInstance(cancer_type_dict=portal_dict['cancertypes'],
                          clinical_attribute_dict=clinical_attr_dict,
                          hugo_entrez_map=portal_dict['genes'],
                          alias_entrez_map=portal_dict['genesaliases'])


# ------------------------------------------------------------------------------
def interface(args=None):
    parser = argparse.ArgumentParser(description='cBioPortal study validator')
    parser.add_argument('-s', '--study_directory',
                        type=str, required=True, help='path to directory.')
    portal_mode_group = parser.add_mutually_exclusive_group()
    portal_mode_group.add_argument('-u', '--url_server',
                                   type=str,
                                   default='http://localhost/cbioportal',
                                   help='URL to cBioPortal server. You can '
                                        'set this if your URL is not '
                                        'http://localhost/cbioportal')
    portal_mode_group.add_argument('-p', '--portal_info_dir',
                                   type=str,
                                   help='Path to a directory of cBioPortal '
                                        'info files to be used instead of '
                                        'contacting a server')
    portal_mode_group.add_argument('-n', '--no_portal_checks',
                                   action='store_true',
                                   help='Skip tests requiring information '
                                        'from the cBioPortal installation')
    parser.add_argument('-html', '--html_table', type=str, required=False,
                        help='path to html report output file')
    parser.add_argument('-e', '--error_file', type=str, required=False,
                        help='File to which to write line numbers on which '
                             'errors were found, for scripts')
    parser.add_argument('-v', '--verbose', required=False, action='store_true',
                        help='report status info messages in addition '
                             'to errors and warnings')

    parser = parser.parse_args(args)
    return parser


def validate_study(study_dir, portal_instance, logger):

    """Validate the study in `study_dir`, logging messages to `logger`.

    This will verify that the study is compatible with the portal configuration
    represented by the PortalInstance object `portal_instance`, if its
    attributes are not None.
    """

    global DEFINED_CANCER_TYPES
    global DEFINED_SAMPLE_IDS
    global DEFINED_SAMPLE_ATTRIBUTES
    global PATIENTS_WITH_SAMPLES

    if portal_instance.cancer_type_dict is None:
        logger.warning('Skipping validations relating to cancer types '
                       'defined in the portal')
    if portal_instance.clinical_attribute_dict is None:
        logger.warning('Skipping validations relating to clinical attributes '
                       'defined in the portal')
    if (portal_instance.hugo_entrez_map is None or
            portal_instance.alias_entrez_map is None):
        logger.warning('Skipping validations relating to gene identifiers and '
                       'aliases defined in the portal')

    # walk over the meta files in the dir and get properties of the study
    (validators_by_meta_type,
     defined_case_list_fns,
     study_cancer_type,
     study_id) = process_metadata_files(study_dir, portal_instance, logger)

    # first parse and validate cancer type files
    studydefined_cancer_types = []
    if cbioportal_common.MetaFileTypes.CANCER_TYPE in validators_by_meta_type:
        cancer_type_validators = validators_by_meta_type[
                cbioportal_common.MetaFileTypes.CANCER_TYPE]
        if len(cancer_type_validators) > 1:
            logger.error(
                'Multiple cancer type files detected',
                extra={'cause': ', '.join(
                    validator.filenameShort for validator in
                    validators_by_meta_type[
                            cbioportal_common.MetaFileTypes.CANCER_TYPE])})
        else:
            cancer_type_validators[0].validate()
            studydefined_cancer_types = (
                cancer_type_validators[0].defined_cancer_types)
    DEFINED_CANCER_TYPES = studydefined_cancer_types

    # next check the cancer type of the meta_study file
    if cbioportal_common.MetaFileTypes.STUDY not in validators_by_meta_type:
        logger.error('No valid study file detected')
        return
    if portal_instance.cancer_type_dict is not None and not (
                study_cancer_type in portal_instance.cancer_type_dict or
                study_cancer_type in DEFINED_CANCER_TYPES):
        logger.error(
            'Cancer type of study is neither known to the portal nor defined '
            'in a cancer_type file',
            extra={'cause': study_cancer_type})

    # then validate the clinical data
    if cbioportal_common.MetaFileTypes.SAMPLE_ATTRIBUTES not in \
            validators_by_meta_type:
        logger.error('No sample attribute file detected')
        return
    if len(validators_by_meta_type[
               cbioportal_common.MetaFileTypes.SAMPLE_ATTRIBUTES]) > 1:
        logger.error(
            'Multiple sample attribute files detected',
            extra={'cause': ', '.join(
                validator.filenameShort for validator in
                validators_by_meta_type[
                    cbioportal_common.MetaFileTypes.SAMPLE_ATTRIBUTES])})
    # parse the data file(s) that define sample IDs valid for this study
    defined_sample_ids = None
    for sample_validator in validators_by_meta_type[
            cbioportal_common.MetaFileTypes.SAMPLE_ATTRIBUTES]:
        sample_validator.validate()
        if sample_validator.fileCouldBeParsed:
            if defined_sample_ids is None:
                defined_sample_ids = set()
            # include parsed sample IDs in the set (union)
            defined_sample_ids |= sample_validator.sampleIds
    # this will be set if a file was successfully parsed
    if defined_sample_ids is None:
        logger.error("Sample file could not be parsed. Please fix "
                     "the problems found there first before continuing.")
        return
    DEFINED_SAMPLE_IDS = defined_sample_ids
    DEFINED_SAMPLE_ATTRIBUTES = sample_validator.newly_defined_attributes
    PATIENTS_WITH_SAMPLES = sample_validator.patient_ids

    if len(validators_by_meta_type.get(
               cbioportal_common.MetaFileTypes.PATIENT_ATTRIBUTES,
               [])) > 1:
        logger.error(
            'Multiple patient attribute files detected',
            extra={'cause': ', '.join(
                validator.filenameShort for validator in
                validators_by_meta_type[
                    cbioportal_common.MetaFileTypes.PATIENT_ATTRIBUTES])})

    # next validate all other data files
    for meta_file_type in validators_by_meta_type:
        # skip cancer type and clinical files, they have already been validated
        if meta_file_type in (cbioportal_common.MetaFileTypes.CANCER_TYPE,
                              cbioportal_common.MetaFileTypes.SAMPLE_ATTRIBUTES):
            continue
        for validator in validators_by_meta_type[meta_file_type]:
            # if there was no validator for this meta file
            if validator is None:
                continue
            validator.validate()

    # finally validate case lists if present
    case_list_dirname = os.path.join(study_dir, 'case_lists')
    if not os.path.isdir(case_list_dirname):
        logger.info("No directory named 'case_lists' found, so assuming no custom case lists.")
    else:
        processCaseListDirectory(case_list_dirname, study_id, logger,
                                 stableid_files=defined_case_list_fns)

    logger.info('Validation complete')


def main_validate(args):

    """Main function: process parsed arguments and validate the study."""

    # get a logger to emit messages
    logger = logging.getLogger(__name__)
    logger.setLevel(logging.DEBUG)
    exit_status_handler = MaxLevelTrackingHandler()
    logger.addHandler(exit_status_handler)

    # process the options
    study_dir = args.study_directory
    server_url = args.url_server

    html_output_filename = args.html_table

    # determine the log level for terminal and html output
    output_loglevel = logging.INFO
    if args.verbose:
        output_loglevel = logging.DEBUG

    # check existence of directory
    if not os.path.exists(study_dir):
        print >> sys.stderr, 'directory cannot be found: ' + study_dir
        return 2

    # set default message handler
    text_handler = logging.StreamHandler(sys.stdout)
    text_handler.setFormatter(
        cbioportal_common.LogfileStyleFormatter(study_dir))
    collapsing_text_handler = cbioportal_common.CollapsingLogMessageHandler(
        capacity=1e6,
        flushLevel=logging.CRITICAL,
        target=text_handler)
    collapsing_text_handler.setLevel(output_loglevel)
    logger.addHandler(collapsing_text_handler)

    collapsing_html_handler = None
    html_handler = None
    # add html table handler if applicable
    if html_output_filename:
        # just to make sure users get dependency error at start:
        import jinja2  # pylint: disable=import-error

        html_handler = Jinja2HtmlHandler(
            study_dir,
            html_output_filename,
            capacity=1e5)
        # TODO extend CollapsingLogMessageHandler to flush to multiple targets,
        # and get rid of the duplicated buffering of messages here
        collapsing_html_handler = cbioportal_common.CollapsingLogMessageHandler(
            capacity=1e6,
            flushLevel=logging.CRITICAL,
            target=html_handler)
        collapsing_html_handler.setLevel(output_loglevel)
        logger.addHandler(collapsing_html_handler)

    if args.error_file:
        errfile_handler = logging.FileHandler(args.error_file, 'w')
        errfile_handler.setFormatter(ErrorFileFormatter(study_dir))
        # TODO extend CollapsingLogMessageHandler to flush to multiple targets,
        # and get rid of the duplicated buffering of messages here
        coll_errfile_handler = cbioportal_common.CollapsingLogMessageHandler(
            capacity=1e6,
            flushLevel=logging.CRITICAL,
            target=errfile_handler)
        coll_errfile_handler.setLevel(logging.WARNING)
        coll_errfile_handler.addFilter(LineMessageFilter())
        logger.addHandler(coll_errfile_handler)

    # load portal-specific information
    if args.no_portal_checks:
        portal_instance = PortalInstance(cancer_type_dict=None,
                                         clinical_attribute_dict=None,
                                         hugo_entrez_map=None,
                                         alias_entrez_map=None)
    elif args.portal_info_dir:
        portal_instance = load_portal_info(args.portal_info_dir, logger,
                                           offline=True)
    else:
        portal_instance = load_portal_info(server_url, logger)

    validate_study(study_dir, portal_instance, logger)

    if html_handler is not None:
        collapsing_html_handler.flush()
        html_handler.generateHtml()

    return exit_status_handler.get_exit_status()


# ------------------------------------------------------------------------------
# vamanos

if __name__ == '__main__':
    try:
        # parse command line options
        parsed_args = interface()
        # run the script
        exit_status = main_validate(parsed_args)
    finally:
        logging.shutdown()
        del logging._handlerList[:]  # workaround for harmless exceptions on exit
    print >>sys.stderr, ('Validation of study {status}.'.format(
        status={0: 'succeeded',
                1: 'failed',
                2: 'not performed as problems occurred',
                3: 'succeeded with warnings'}.get(exit_status, 'unknown')))
