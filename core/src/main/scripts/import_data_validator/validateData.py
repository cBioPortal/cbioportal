#! /usr/bin/env python

# ------------------------------------------------------------------------------
# Data validation script - validates files before import into portal.
# If create-corrected set to true, the script will create a new version of all the files it detects
#   and ensure the newlines are correct and that no data is enclosed in quotes. It will also
#   add entrez IDs if they are not present and the user either provides the file or sets ftp
#   Also checks for duplicate column headers, repeated header rows
# ------------------------------------------------------------------------------

# imports
import sys
import os
import logging
import logging.handlers
from collections import OrderedDict
import argparse
import re
import csv
import itertools
import requests


# ------------------------------------------------------------------------------
# globals

# Current NCBI build and build counterpart - used in one of the maf checks as well as .seq filename check
NCBI_BUILD_NUMBER = 37
GENOMIC_BUILD_COUNTERPART = 'hg19'

# study-specific globals
STUDY_DIR = None
DEFINED_SAMPLE_IDS = None
DEFINED_CANCER_TYPES = None

SERVER_URL = 'http://localhost/cbioportal'
PORTAL_CANCER_TYPES = None

# ----------------------------------------------------------------------------
# how we differentiate between data types based on the meta_file_type field

SEG_META_PATTERN = 'meta_segment'
STUDY_META_PATTERN = 'meta_study'
CANCER_TYPE_META_PATTERN = 'meta_cancer_type'
MUTATION_META_PATTERN = 'meta_mutations_extended'
CNA_META_PATTERN = 'meta_CNA'
CLINICAL_META_PATTERN = 'meta_clinical'
LOG2_META_PATTERN = 'meta_log2CNA'
EXPRESSION_META_PATTERN = 'meta_expression'
FUSION_META_PATTERN = 'meta_fusions'
METHYLATION_META_PATTERN = 'meta_methylation'
RPPA_META_PATTERN = 'meta_rppa'
TIMELINE_META_PATTERN = 'meta_timeline'

META_FILE_PATTERNS = [
    STUDY_META_PATTERN,
    CANCER_TYPE_META_PATTERN,
    SEG_META_PATTERN,
    MUTATION_META_PATTERN,
    CNA_META_PATTERN,
    CLINICAL_META_PATTERN,
    LOG2_META_PATTERN,
    EXPRESSION_META_PATTERN,
    FUSION_META_PATTERN,
    METHYLATION_META_PATTERN,
    RPPA_META_PATTERN,
    TIMELINE_META_PATTERN
]

VALIDATOR_IDS = {CNA_META_PATTERN:'CNAValidator',
                 MUTATION_META_PATTERN:'MutationsExtendedValidator',
                 CLINICAL_META_PATTERN:'ClinicalValidator',
                 SEG_META_PATTERN:'SegValidator',
                 LOG2_META_PATTERN:'Log2Validator',
                 EXPRESSION_META_PATTERN:'ExpressionValidator',
                 FUSION_META_PATTERN:'FusionValidator',
                 METHYLATION_META_PATTERN:'MethylationValidator',
                 RPPA_META_PATTERN:'RPPAValidator',
                 TIMELINE_META_PATTERN:'TimelineValidator'}

# ----------------------------------------------------------------------------
# fields allowed in each meta file type, maps to True if required

CNA_META_FIELDS = {
    'cancer_study_identifier': True,
    'genetic_alteration_type': True,
    'datatype': True,
    'stable_id': True,
    'show_profile_in_analysis_tab': True,
    'profile_name': True,
    'profile_description': True,
    'meta_file_type': True,
    'data_file_path': True
}

MUTATION_META_FIELDS = {
    'cancer_study_identifier': True,
    'genetic_alteration_type': True,
    'datatype': True,
    'stable_id': True,
    'show_profile_in_analysis_tab': True,
    'profile_name': True,
    'profile_description': True,
    'meta_file_type': True,
    'data_file_path': True,
    'normal_samples_list': False
}

SEG_META_FIELDS = {
    'cancer_study_identifier': True,
    'genetic_alteration_type': True,
    'datatype': True,
    'stable_id': True,
    'show_profile_in_analysis_tab': True,
    'profile_name': True,
    'profile_description': True,
    'reference_genome_id': True,
    'data_filename': True,
    'description': True,
    'meta_file_type': True,
    'data_file_path': True
}

LOG2_META_FIELDS = {
    'cancer_study_identifier': True,
    'genetic_alteration_type': True,
    'datatype': True,
    'stable_id': True,
    'show_profile_in_analysis_tab': True,
    'profile_name': True,
    'profile_description': True,
    'meta_file_type': True,
    'data_file_path': True
}

EXPRESSION_META_FIELDS = {
    'cancer_study_identifier': True,
    'genetic_alteration_type': True,
    'datatype': True,
    'stable_id': True,
    'show_profile_in_analysis_tab': True,
    'profile_name': True,
    'profile_description': True,
    'meta_file_type': True,
    'data_file_path': True
}

METHYLATION_META_FIELDS = {
    'cancer_study_identifier': True,
    'genetic_alteration_type': True,
    'datatype': True,
    'stable_id': True,
    'show_profile_in_analysis_tab': True,
    'profile_name': True,
    'profile_description': True,
    'meta_file_type': True,
    'data_file_path': True
}

FUSION_META_FIELDS = {
    'cancer_study_identifier': True,
    'genetic_alteration_type': True,
    'datatype': True,
    'stable_id': True,
    'show_profile_in_analysis_tab': True,
    'profile_name': True,
    'profile_description': True,
    'meta_file_type': True,
    'data_file_path': True
}

RPPA_META_FIELDS = {
    'cancer_study_identifier': True,
    'genetic_alteration_type': True,
    'datatype': True,
    'stable_id': True,
    'show_profile_in_analysis_tab': True,
    'profile_name': True,
    'profile_description': True,
    'meta_file_type': True,
    'data_file_path': True
}

TIMELINE_META_FIELDS = {
    'cancer_study_identifier': True,
    'genetic_alteration_type': True,
    'meta_file_type': True,
    'data_file_path': True
}

CASE_LIST_FIELDS = {
    'cancer_study_identifier': True,
    'stable_id': True,
    'case_list_name': True,
    'case_list_description': True,
    'case_list_ids': True,
    'case_list_category': False
}

CLINICAL_META_FIELDS = {
    'cancer_study_identifier': True,
    'genetic_alteration_type': True,
    'datatype': True,
    'stable_id': True,
    'show_profile_in_analysis_tab': True,
    'profile_name': True,
    'profile_description': True,
    'meta_file_type': True,
    'data_file_path': True
}

STUDY_META_FIELDS = {
    'cancer_study_identifier': True,
    'type_of_cancer': True,
    'name': True,
    'description': True,
    'groups': True,
    'dedicated_color': True,
    'short_name': True,
    'meta_file_type': True,
    'citation': False,
    'pmid': False
}

CANCER_TYPE_META_FIELDS = {
    'type_of_cancer': True,
    'name': True,
    'clinical_trial_keywords': True,
    'dedicated_color': True,
    'short_name': True
}

META_FIELD_MAP = {
    STUDY_META_PATTERN:STUDY_META_FIELDS,
    CANCER_TYPE_META_PATTERN:CANCER_TYPE_META_FIELDS,
    CNA_META_PATTERN:CNA_META_FIELDS,
    CLINICAL_META_PATTERN:CLINICAL_META_FIELDS,
    LOG2_META_PATTERN:LOG2_META_FIELDS,
    MUTATION_META_PATTERN:MUTATION_META_FIELDS,
    SEG_META_PATTERN:SEG_META_FIELDS,
    EXPRESSION_META_PATTERN:EXPRESSION_META_FIELDS,
    METHYLATION_META_PATTERN:EXPRESSION_META_FIELDS,
    FUSION_META_PATTERN:FUSION_META_FIELDS,
    RPPA_META_PATTERN:RPPA_META_FIELDS,
    TIMELINE_META_PATTERN:TIMELINE_META_FIELDS,
    'case_list': CASE_LIST_FIELDS
}


# ----------------------------------------------------------------------------
# class definitions

class ValidationMessageFormatter(logging.Formatter):

    """Logging formatter with optional fields for data validation messages.

    These fields are:
    data_filename - the name of the file the message is about (if applicable)
    line_number - a line number within the above file (if applicable)
    column_number - a column number within the above file (if applicable)
    cause - the unexpected value found in the input (if applicable)

    If instead a message pertains to multiple values of one of these
    fields (as the result of aggregation by CollapsingLogMessageHandler),
    these will be expected in the field <fieldname>_list.
    """

    def format(self, record, *args, **kwargs):
        """Check consistency of expected fields and format the record."""
        if (
                (
                    self.format_aggregated(record,
                                           'line_number',
                                           optional=True) or
                    self.format_aggregated(record,
                                           'column_number',
                                           optional=True))
                and not self.format_aggregated(record,
                                               'data_filename',
                                               optional=True)):
            raise ValueError(
                'Tried to log about a line/column with no filename')
        return super(ValidationMessageFormatter, self).format(record,
                                                              *args,
                                                              **kwargs)

    @staticmethod
    def format_aggregated(record,
                          attr_name,
                          single_fmt='%s',
                          multiple_fmt='[%s]',
                          join_string=', ',
                          max_join=3,
                          optional=False):
        """Format a human-readable string for a field or its <field>_list.

        As would be generated when using the CollapsingLogMessageHandler.
        If `optional` is True and both the field and its list are absent,
        return an empty string.
        """
        attr_val = getattr(record, attr_name, None)
        attr_list = getattr(record, attr_name + '_list', None)
        if attr_val is not None:
            attr_indicator = single_fmt % attr_val
        elif attr_list is not None:
            string_list = list(str(val) for val in attr_list[:max_join])
            num_skipped = len(attr_list) - len(string_list)
            if num_skipped != 0:
                string_list.append('(%d more)' % num_skipped)
            attr_indicator = multiple_fmt % join_string.join(string_list)
        elif optional:
            attr_indicator = ''
        else:
            raise ValueError(
                "Tried to format an absent non-optional log field: '%s'" %
                attr_name)
        return attr_indicator


class LogfileStyleFormatter(ValidationMessageFormatter):

    """Formatter for validation messages in a simple one-per-line format."""

    def __init__(self):
        """Initialize a logging Formatter with an appropriate format string."""
        super(LogfileStyleFormatter, self).__init__(
            fmt='%(levelname)s: %(file_indicator)s:'
                '%(line_indicator)s%(column_indicator)s'
                ' %(message)s%(cause_indicator)s')

    def format(self, record):

        """Generate descriptions for optional fields and format the record."""


        record.file_indicator = self.format_aggregated(record,
                                                       'data_filename',
                                                       optional=True)
        if not record.file_indicator:
            record.file_indicator = '-'
        record.line_indicator = self.format_aggregated(
            record,
            'line_number',
            ' line %d:',
            ' lines [%s]:',
            optional=True)
        record.column_indicator = self.format_aggregated(
            record,
            'column_number',
            ' column %d:',
            ' columns [%s]:',
            optional=True)
        record.cause_indicator = self.format_aggregated(
            record,
            'cause',
            "; found in file: '%s'",
            "; found in file: ['%s']",
            join_string="', '",
            optional=True)

        return super(LogfileStyleFormatter, self).format(record)


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
        template = j_env.get_template('validation_report_template.html.jinja')
        doc = template.render(
            study_dir=self.study_dir,
            record_list=self.buffer,
            max_level=logging.getLevelName(self.max_level))
        with open(self.output_filename, 'w') as f:
            f.write(doc)


class CollapsingLogMessageHandler(logging.handlers.MemoryHandler):

    """Logging handler that aggregates repeated log messages into one.

    This collapses validation LogRecords based on the source code line that
    emitted them and their formatted message, and flushes the resulting
    records to another handler.
    """

    def flush(self):

        """Aggregate LogRecords by message and send them to the target handler.

        Fields that occur with multiple different values in LogRecords
        emitted from the same line with the same message will be
        collected in a field named <field_name>_list.
        """

        # group buffered LogRecords by their source code line and message
        grouping_dict = OrderedDict()
        for record in self.buffer:
            identifying_tuple = (record.module,
                                 record.lineno,
                                 record.getMessage())
            if identifying_tuple not in grouping_dict:
                grouping_dict[identifying_tuple] = []
            grouping_dict[identifying_tuple].append(record)

        aggregated_buffer = []
        # for each list of same-message records
        for record_list in grouping_dict.values():
            # make a dict to collect the fields for the aggregate record
            aggregated_field_dict = {}
            # for each field found in (the first of) the records
            for field_name in record_list[0].__dict__:
                # collect the values found for this field across the records.
                # Use the keys of an OrderedDict, as OrderedSet is for some
                # reason not to be found in the Python standard library.
                field_values = OrderedDict((record.__dict__[field_name], None)
                                           for record in record_list)
                # if this field has the same value in all records
                if len(field_values) == 1:
                    # use that value in the new dict
                    aggregated_field_dict[field_name] = field_values.popitem()[0]
                else:
                    # set a <field>_list field instead
                    aggregated_field_dict[field_name + '_list'] = \
                        list(field_values.keys())

            # add a new log record with these fields tot the output buffer
            aggregated_buffer.append(
                logging.makeLogRecord(aggregated_field_dict))

        # replace the buffer with the aggregated one and flush
        self.buffer = aggregated_buffer
        super(CollapsingLogMessageHandler, self).flush()

    def shouldFlush(self, record):
        """Flush when emitting an INFO message or a message without a file."""
        return ((record.levelno == logging.INFO) or
                ('data_filename' not in record.__dict__) or
                super(CollapsingLogMessageHandler, self).shouldFlush(record))


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


class ValidatorFactory(object):

    """Factory for creating validation objects of various types."""

    @staticmethod
    def createValidator(validator_type, hugo_entrez_map, logger, meta_dict):
        ValidatorClass = globals()[validator_type]
        return ValidatorClass(hugo_entrez_map, logger, meta_dict)


class Validator(object):

    """Abstract validator class for tab-delimited data files.

    Subclassed by validators for specific data file types, which should
    define a 'REQUIRED_HEADERS' attribute listing the required column
    headers and a `REQUIRE_COLUMN_ORDER` boolean stating whether their
    position is significant.

    The methods `processTopLines`, `checkHeader`, `checkLine` and `onComplete`
    may be overridden (calling their superclass methods) to perform any
    appropriate validation tasks.
    """

    REQUIRED_HEADERS = []
    REQUIRE_COLUMN_ORDER = True

    def __init__(self,hugo_entrez_map,logger,meta_dict):
        """Initialize a validator for a particular data file.

        :param hugo_entrez_map: path Entrez to Hugo mapping file
        :param logger: logger instance for writing the log messages
        :param meta_dict: dictionary of fields found in corresponding meta file
                         (such as stable id and data file name)
        """
        self.filename = os.path.join(STUDY_DIR, meta_dict['data_file_path'])
        self.filenameShort = os.path.basename(self.filename)
        self.line_number = 0
        self.cols = []
        self.numCols = 0
        self.hugo_entrez_map = hugo_entrez_map
        self.newlines = ('',)
        self.studyId = ''
        self.headerWritten = False
        # This one is set to True if file could be parsed/read until the end (happens in onComplete)
        self.fileCouldBeParsed = False
        self.logger = CombiningLoggerAdapter(
            logger,
            extra={'data_filename': self.filenameShort})
        self.meta_dict = meta_dict
        self.badChars = [' ']

    def validate(self):

        """Validate the data file."""

        self.logger.info('Starting validation of file')

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
            dialect = csv.Sniffer().sniff(sample_content)
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
                if fields[0].startswith('#'):
                    self.logger.error(
                        "Data line starting with '#' skipped",
                        extra={'line_number': self.line_number})
                    continue
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
        # finalize:
        self.fileCouldBeParsed = True
        self.logger.info('Validation of file complete')

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
        num_errors += self.checkBadChar()

        if self.REQUIRE_COLUMN_ORDER:
            num_errors += self._checkOrderedRequiredColumns()
        else:
            num_errors += self._checkUnorderedRequiredColumns()

        return num_errors

    def checkLine(self, data):
        """Check data values from a line after the file header.

        :param data: The list of values parsed from the line
        """

        if all(x == '' for x in data):
            self.logger.error("Blank line",
                              extra={'line_number': self.line_number})

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

        for col_index, col_name in enumerate(self.cols):
            if col_index < line_col_count and data[col_index] == '':
                self.logger.error("Blank cell found in column '%s'",
                                  col_name,
                                  extra={'line_number': self.line_number,
                                         'column_number': col_index + 1})

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
        # TODO document these requirements
        if "\r\n" in self.newlines:
            self.logger.error('DOS-style line breaks detected (\\r\\n), '
                              'should be Unix-style (\\n)')
        elif "\r" in self.newlines:
            self.logger.error('Classic Mac OS-style line breaks detected '
                              '(\\r), should be Unix-style (\\n)')
        elif self.newlines != '\n':
            self.logger.error('No line breaks recognized in file',
                              extra={'cause': repr(self.newlines)[1:-1]})

    def checkInt(self, value):
        """Checks if a value is an integer."""
        try:
            int(value)
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

    def checkGeneIdentification(self, gene_symbol=None, entrez_id=None):
        """Check if a symbol-Entrez pair is valid, logging an error if not.

        It is considered valid in these three cases:
            1. only the Entrez id is not None, and it is defined in the portal
            2. only the symbol is not None, and it is unambiguously defined in
               the portal
            3. both are given, and the symbol is defined in the portal to match
               the Entrez id

        Return True if the pair was valid, False otherwise.
        """
        if entrez_id is not None:
            if gene_symbol is not None:
                if gene_symbol not in self.hugo_entrez_map:
                    self.logger.error(
                        'Gene symbol not known to the cBioPortal instance.',
                        extra={'line_number': self.line_number,
                               'cause': gene_symbol})
                    return False
                elif entrez_id not in self.hugo_entrez_map[gene_symbol]:
                    self.logger.error(
                        'Gene symbol does not match given Entrez id',
                        extra={'line_number': self.line_number,
                               'cause': gene_symbol + ', ' + entrez_id})
                    return False
            else:
                if entrez_id not in (gid for
                                     gid in (self.hugo_entrez_map[sym] for
                                             sym in self.hugo_entrez_map)):
                    self.logger.error(
                        'Entrez gene id not known to the cBioPortal instance.',
                        extra={'line_number': self.line_number,
                               'cause': entrez_id})
                    return False
        elif gene_symbol is not None:
            if gene_symbol not in self.hugo_entrez_map:
                self.logger.error(
                    'Gene symbol not known to the cBioPortal instance.',
                    extra={'line_number': self.line_number,
                           'cause': gene_symbol})
                return False
            elif len(gene_symbol) > 1:
                self.logger.error(
                    'Ambiguous gene symbol, please use a synonym or, if '
                    'possible, provide an Entrez id',
                    extra={'line_number': self.line_number,
                           'cause': gene_symbol})
                return False
        else:
            self.logger.error(
                'No Entrez id or gene symbol provided for gene',
                extra={'line_number': self.line_number})
            return False
        return True

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

    def checkBadChar(self):
        """Check for bad things in a header, such as spaces, etc."""
        num_errors = 0
        for col_num, col_name in enumerate(self.cols):
            for bc in self.badChars:
                if bc in col_name:
                    num_errors += 1
                    self.logger.error("Bad character '%s' detected in header",
                                      bc,
                                      extra={'line_number': self.line_number,
                                             'column_number': col_num,
                                             'cause': col_name})
        return num_errors


class FeaturewiseFileValidator(Validator):

    """Validates a file with rows for features and columns for ids and samples.

    The first few columns (collectively defined in the class attributes
    REQUIRED_HEADERS and OPTIONAL_HEADERS) identify the features
    (e.g. genes) and the rest correspond to the samples.

    Subclasses should override the checkValue(self, value, col_index)
    function to check value in a sample column, and check the non-sample
    columns by overriding and extending checkLine(self, data). The method
    can find the headers of these columns in self.nonsample_cols.
    """

    OPTIONAL_HEADERS = []
    REQUIRE_COLUMN_ORDER = True

    def __init__(self, *args, **kwargs):
        super(FeaturewiseFileValidator, self).__init__(*args, **kwargs)
        self.nonsample_cols = []
        self.num_nonsample_cols = 0
        self.sampleIds = []

    def checkHeader(self, cols):
        """Validate the header and read sample IDs from it.

        Return the number of fatal errors.
        """
        num_errors = super(FeaturewiseFileValidator, self).checkHeader(cols)
        supported_headers = self.REQUIRED_HEADERS + self.OPTIONAL_HEADERS
        # collect the non-sample columns headers, assuming order is required
        for col_index, col_name in enumerate(self.cols):
            if (
                    col_index < len(supported_headers) and
                    col_name == supported_headers[col_index]):
                self.nonsample_cols.append(col_name)
            else:
                # reached the sample id columns
                break
        self.num_nonsample_cols = len(self.nonsample_cols)
        num_errors += self._set_sample_ids_from_columns()
        return num_errors

    def checkLine(self, data):
        """Check the values in a data line."""
        super(FeaturewiseFileValidator, self).checkLine(data)
        for column_index, value in enumerate(data):
            if column_index >= len(self.nonsample_cols):
                # checkValue() should be implemented by subclasses
                self.checkValue(value, column_index)

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
        return num_errors


class GenewiseFileValidator(FeaturewiseFileValidator):

    OPTIONAL_HEADERS = ['Hugo_Symbol', 'Entrez_Gene_Id']

    def __init__(self, *args, **kwargs):
        super(GenewiseFileValidator, self).__init__(*args, **kwargs)

    def checkHeader(self, cols):
        """Validate the header and read sample IDs from it.

        Return the number of fatal errors.
        """
        num_errors = super(GenewiseFileValidator, self).checkHeader(cols)
        if not ('Hugo_Symbol' in self.nonsample_cols or
                'Entrez_Gene_Id' in self.nonsample_cols):
            self.logger.error('At least one of the columns Hugo_Symbol or '
                              'Entrez_Gene_Id needs to be present.',
                              extra={'line_number': self.line_number})
            num_errors += 1
        return num_errors

    def checkLine(self, data):
        """Check the values in a data line."""
        super(GenewiseFileValidator, self).checkLine(data)
        hugo_symbol = data[self.nonsample_cols.index('Hugo_Symbol')]
        entrez_id = data[self.nonsample_cols.index('Entrez_Gene_Id')]
        # treat NA or the empty string as a missing value
        if hugo_symbol in ('NA', ''):
            hugo_symbol = None
        if entrez_id in ('NA', ''):
            entrez_id = None
        self.checkGeneIdentification(hugo_symbol, entrez_id)


class CNAValidator(GenewiseFileValidator):

    """Sub-class CNA validator."""

    ALLOWED_VALUES = ['-2','-1','0','1','2','','NA']

    def checkValue(self, value, col_index):
        """Check a value in a sample column."""
        if value not in self.ALLOWED_VALUES:
            if self.logger.isEnabledFor(logging.ERROR):
                self.logger.error(
                    'Invalid CNA value: possible values are [%s]',
                    ', '.join(self.ALLOWED_VALUES),
                    extra={'line_number': self.line_number,
                           'column_number': col_index + 1,
                           'cause': value})


class MutationsExtendedValidator(Validator):

    """Sub-class mutations_extended validator."""

    REQUIRED_HEADERS = [
        'Tumor_Sample_Barcode',
        'Amino_Acid_Change'
    ]
    REQUIRE_COLUMN_ORDER = False

    # Used for mapping column names to the corresponding function that does a check on the value.
    # This can be done for other filetypes besides maf - not currently implemented.
    CHECK_FUNCTION_MAP = {
        'Matched_Norm_Sample_Barcode':'checkMatchedNormSampleBarcode',
        'NCBI_Build':'checkNCBIbuild',
        'Verification_Status':'checkVerificationStatus',
        'Validation_Status':'checkValidationStatus',
        't_alt_count':'check_t_alt_count',
        't_ref_count':'check_t_ref_count',
        'n_alt_count':'check_n_alt_count',
        'n_ref_count':'check_n_ref_count',
        'Amino_Acid_Change': 'checkAminoAcidChange'
    }

    def __init__(self,hugo_entrez_map,logger,meta_dict):
        super(MutationsExtendedValidator, self).__init__(hugo_entrez_map,logger,meta_dict)
        # TODO consider making this attribute a local var in in checkLine(),
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

        for col_name in self.CHECK_FUNCTION_MAP:
            # if optional column was found, validate it:
            if col_name in self.cols:
                col_index = self.cols.index(col_name)
                value = data[col_index]
                if col_name == 'Tumor_Sample_Barcode':
                    self.checkSampleId(value, column_number=col_index + 1)
                # get the checking method for this column if available, or None
                checking_function = getattr(
                    self,
                    self.CHECK_FUNCTION_MAP[col_name])
                if not checking_function(value):
                    self.printDataInvalidStatement(value, col_index)
                elif self.extra_exists or self.extra:
                    raise RuntimeError(('Checking function %s set an error '
                                        'message but reported no error') %
                                       checking_function.__name__)
        hugo_symbol = None
        entrez_id = None
        if 'Hugo_Symbol' in self.cols:
            hugo_symbol = data[self.cols.index('Hugo_Symbol')]
            # treat the empty string as a missing value
            if hugo_symbol == '':
                hugo_symbol = None
        if 'Entrez_Gene_Id' in self.cols:
            entrez_id = data[self.cols.index('Entrez_Gene_Id')]
            # treat the empty string as a missing value
            if entrez_id == '':
                entrez_id = None
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


    def checkNCBIbuild(self, value):
        if self.checkInt(value) and value != '':
            if int(value) != NCBI_BUILD_NUMBER:
                return False
        return True
    
    def checkMatchedNormSampleBarcode(self, value):
        if value != '':
            if 'normal_samples_list' in self.meta_dict and self.meta_dict['normal_samples_list'] != '':
                normal_samples_list = [x.strip() for x in self.meta_dict['normal_samples_list'].split(',')]
                if value not in normal_samples_list:
                    self.extra = "Normal sample id not in list of sample ids configured in corresponding metafile. " \
                    "Please check your metafile field 'normal_samples_list'." 
                    self.extra_exists = True
                    return False
        return True
    
    
    def checkVerificationStatus(self, value):
        if value.lower() not in ('', 'verified', 'unknown'):
            return False
        return True
    
    def checkValidationStatus(self, value):
        if value == '':
            return True
        if value.lower() not in ('valid', 'unknown', 'na', 'untested'):
            return False
        return True
    
    def check_t_alt_count(self, value):
        if not self.checkInt(value) and value != '':
            return False
        return True
    
    def check_t_ref_count(self, value):
        if not self.checkInt(value) and value != '':
            return False
        return True
    
    def check_n_alt_count(self, value):
        if not self.checkInt(value) and value != '':
            return False
        return True

    def check_n_ref_count(self, value):
        if not self.checkInt(value) and value != '':
            return False
        return True

    def checkAminoAcidChange(self, value):
        """Test whether a string is a valid amino acid change specification."""
        # TODO implement this test, may require bundling the hgvs package:
        # https://pypi.python.org/pypi/hgvs/
        return True


class ClinicalValidator(Validator):

    """Validator for clinical data files."""

    REQUIRED_HEADERS = [
        'PATIENT_ID',
        'SAMPLE_ID'
    ]
    REQUIRE_COLUMN_ORDER = False

    srv_attrs = None

    def __init__(self, *args, **kwargs):
        super(ClinicalValidator, self).__init__(*args, **kwargs)
        self.sampleIds = set()
        self.attr_defs = []

    def processTopLines(self, line_list):

        """Parse the the attribute definitions above the column header."""

        LINE_NAMES = ('display_name',
                      'description',
                      'datatype',
                      'attribute_type',
                      'priority')

        if not line_list:
            self.logger.error(
                'No data type header comments found in clinical data file',
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
                # make a list of as many lists as long as there are columns
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

                # test for invalid values in these (otherwise parseable) lines
                if value in ('', 'NA'):
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
                            'Invalid data type definition, must be one of'
                            ' [%s]',
                            ', '.join(VALID_DATATYPES),
                            extra={'line_number': line_index + 1,
                                   'colum_number': col_index + 1,
                                   'cause': value})
                        invalid_values = True
                elif LINE_NAMES[line_index] == 'attribute_type':
                    VALID_ATTR_TYPES = ('PATIENT', 'SAMPLE')
                    if value not in VALID_ATTR_TYPES:
                        self.logger.error(
                            'Invalid attribute type definition, must be one of'
                            ' [%s]',
                            ', '.join(VALID_ATTR_TYPES),
                            extra={'line_number': line_index + 1,
                                   'colum_number': col_index + 1,
                                   'cause': value})
                        invalid_values = True
                elif LINE_NAMES[line_index] == 'priority':
                    try:
                        if int(value) < 1:
                            raise ValueError()
                    except ValueError:
                        self.logger.error(
                            'Priority definition must be a positive integer',
                            extra={'line_number': line_index + 1,
                                   'column_number': col_index + 1,
                                   'cause': value})
                        invalid_values = True
                else:
                    raise Exception('Unknown clinical header line name')

                attr_defs[col_index][LINE_NAMES[line_index]] = value

        self.attr_defs = attr_defs
        return not invalid_values

    def checkHeader(self, cols):

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
                    "Clinical header not in all caps",
                    extra={'line_number': self.line_number,
                           'cause': col_name})
            # look up how the attribute is defined in the portal
            srv_attr_properties = self.srv_attrs.get(col_name)
            if srv_attr_properties is None:
                self.logger.warning(
                    'New %s-level attribute will be added to the portal',
                    self.attr_defs[col_index]['attribute_type'].lower(),
                    extra={'line_number': self.line_number,
                           'column_number': col_index + 1,
                           'cause': col_name})
            else:
                # translate one property having a different format in the API
                transl_attr_properties = {}
                for prop in srv_attr_properties:
                    # define the 'attribute_type' property as it is found in
                    # files, based on 'is_patient_attribute' from the API
                    if prop == 'is_patient_attribute':
                        if srv_attr_properties[prop] == '1':
                            transl_attr_properties['attribute_type'] = 'PATIENT'
                        else:
                            transl_attr_properties['attribute_type'] = 'SAMPLE'
                    # all of the other properties just match the file format
                    elif prop in ('display_name', 'description',
                                  'datatype', 'priority'):
                        transl_attr_properties[prop] = srv_attr_properties[prop]
                # compare values defined in the file with the existing ones
                for attr_property in self.attr_defs[col_index]:
                    value = self.attr_defs[col_index][attr_property]
                    if value != transl_attr_properties[attr_property]:
                        self.logger.error(
                            "%s definition for attribute '%s' does not match "
                            "the portal, '%s' expected",
                            attr_property,
                            col_name,
                            transl_attr_properties[attr_property],
                            extra={'line_number': self.attr_defs[col_index].keys().index(attr_property) + 1,
                                   'column_number': col_index + 1,
                                   'cause': value})
                        num_errors += 1

        return num_errors

    def checkLine(self, data):
        super(ClinicalValidator, self).checkLine(data)
        for col_index, value in enumerate(data):
            # TODO check the values in the other cols, required and optional
            # TODO check if cancer types in clinical attributes are defined
            if col_index == self.cols.index('SAMPLE_ID'):
                if DEFINED_SAMPLE_IDS and value not in DEFINED_SAMPLE_IDS:
                    self.logger.error(
                        'Defining new sample id in secondary clinical file',
                        extra={'line_number': self.line_number,
                               'column_number': col_index + 1,
                               'cause': value})
                self.sampleIds.add(value.strip())

    @classmethod
    def request_attrs(cls, server_url, logger):
        """Initialize cls.srv_attrs using the portal API."""
        cls.srv_attrs = request_from_portal_api(
            server_url + '/api/clinicalattributes/patients',
            logger,
            id_field='attr_id')
        srv_sample_attrs = request_from_portal_api(
            server_url + '/api/clinicalattributes/samples',
            logger,
            id_field='attr_id')
        # if this happens, the database structure has changed and this script
        # needs to be updated
        id_overlap = (set(cls.srv_attrs.keys()) &
                      set(srv_sample_attrs.keys()))
        if id_overlap:
            raise ValueError(
                'The portal at {url} returned these clinical attributes '
                'both for samples and for patients: {attrs}'.format(
                    url=server_url,
                    attrs=', '.join(id_overlap)))
        else:
            cls.srv_attrs.update(srv_sample_attrs)


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

    def checkLine(self, data):
        super(SegValidator, self).checkLine(data)

        # TODO check values in all other columns too
        for col_index, value in enumerate(data):
            if col_index == self.cols.index(self.REQUIRED_HEADERS[0]):
                self.checkSampleId(value, column_number=col_index + 1)


class Log2Validator(GenewiseFileValidator):

    def checkValue(self, value, col_index):
        """Check a value in a sample column."""
        # TODO check these values
        pass


class ExpressionValidator(GenewiseFileValidator):

    def checkValue(self, value, col_index):
        """Check a value in a sample column."""
        # TODO check these values
        pass


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


class MethylationValidator(GenewiseFileValidator):

    def checkValue(self, value, col_index):
        """Check a value in a sample column."""
        # TODO check these values
        pass


class RPPAValidator(FeaturewiseFileValidator):

    REQUIRED_HEADERS = ['Composite.Element.REF']

    def checkLine(self, data):
        super(RPPAValidator, self).checkLine(data)
        # TODO check the values in the first column
        # for rppa, first column should be hugo|antibody, everything after should be sampleIds

    def checkValue(self, value, col_index):
        """Check a value in a sample column."""
        # TODO check these values
        pass


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


# ------------------------------------------------------------------------------
# Functions

def parse_metadata_file(filename, logger, study_id=None, case_list=False):

    """Validate a metafile and return a dictionary of values read from it.

    Return `None` if the file is invalid. If `case_list` is True,
    validate the file as a case list instead of a meta file.
    
    :param filename: name of the meta file
    :param logger: the logging.Logger instance to log warnings and errors to
    :param study_id: cancer study id found in previous files (or None). All subsequent
                     meta files should comply to this in the field 'cancer_study_identifier'
    :param case_list: whether this meta file is a case list (special case)
    """

    metaDictionary = {}
    with open(filename, 'rU') as metafile:
        for line_index, line in enumerate(metafile):
            if ':' not in line:
                logger.error(
                    "Invalid %s file entry, no ':' found",
                    {True: 'case list', False: 'meta'}[case_list],
                    extra={'data_filename': getFileFromFilepath(filename),
                           'line_number': line_index + 1})
                return None
            key_value = line.split(':', 1)
            if len(key_value) == 2:
                metaDictionary[key_value[0]] = key_value[1].strip()

    if case_list:
        meta_file_type = 'case_list'
    else:
        if 'meta_file_type' not in metaDictionary:
            logger.error("Missing field 'meta_file_type' in meta file'",
                         extra={'data_filename': getFileFromFilepath(filename)})
            # skip this file (can't validate unknown file types)
            return None

        meta_file_type = metaDictionary["meta_file_type"]
        if meta_file_type not in META_FILE_PATTERNS:
            logger.error('Unknown meta_file_type',
                         extra={'data_filename': getFileFromFilepath(filename),
                                'cause': meta_file_type})
            # skip this file (can't validate unknown file types)
            return None

    missing_fields = []
    for field in META_FIELD_MAP[meta_file_type]:
        mandatory = META_FIELD_MAP[meta_file_type][field]
        if field not in metaDictionary and mandatory:
            logger.error("Missing field '%s' in %s file",
                         field,
                         {True: 'case list', False: 'meta'}[case_list],
                         extra={'data_filename': getFileFromFilepath(filename)})
            missing_fields.append(field)

    if missing_fields:
        # skip this file (the fields may be required for validation)
        return None

    for field in metaDictionary:
        if field not in META_FIELD_MAP[meta_file_type]:
            logger.warning(
                'Unrecognized field in %s file',
                {True: 'case list', False: 'meta'}[case_list],
                extra={'data_filename': getFileFromFilepath(filename),
                       'cause': field})

    # check that cancer study identifiers across files so far are consistent.
    if (
            study_id is not None and
            study_id != metaDictionary['cancer_study_identifier']):
        logger.error(
            "Cancer study identifier is not consistent across "
            "files, expected '%s'",
            study_id,
            extra={'data_filename': getFileFromFilepath(filename),
                   'cause': metaDictionary['cancer_study_identifier']})
        return None

    # compare a meta_cancer_type file with the portal instance
    if meta_file_type == CANCER_TYPE_META_PATTERN:
        file_cancer_type = metaDictionary.get('type_of_cancer')
        if file_cancer_type not in PORTAL_CANCER_TYPES:
            logger.warning(
                'New disease type will be added to the portal',
                extra={'data_filename': getFileFromFilepath(filename),
                       'cause': file_cancer_type})
        else:
            existing_info = PORTAL_CANCER_TYPES[file_cancer_type]
            invalid_fields_found = False
            for field in metaDictionary:
                if (
                        field in CANCER_TYPE_META_FIELDS and
                        field != 'cancer_type_id' and
                        metaDictionary[field] != existing_info[field]):
                    logger.error(
                        "%s field of cancer type does not match the "
                        "portal, '%s' expected",
                        field,
                        existing_info[field],
                        extra={'data_filename': getFileFromFilepath(filename),
                               'cause': metaDictionary[field]})
                    invalid_fields_found = True
            if invalid_fields_found:
                return None

    # check fields specific to seg meta file
    if meta_file_type == SEG_META_PATTERN:

        if metaDictionary['data_filename'] != metaDictionary['data_file_path']:
            logger.error(
                'data_filename and data_file_path differ in seg data file',
                extra={'data_filename': getFileFromFilepath(filename),
                       'cause': (metaDictionary['data_filename'] + ', ' +
                                 metaDictionary['data_file_path'])})
            return None
        if metaDictionary['reference_genome_id'] != GENOMIC_BUILD_COUNTERPART:
            logger.error(
                'Reference_genome_id is not %s',
                GENOMIC_BUILD_COUNTERPART,
                extra={'data_filename': getFileFromFilepath(filename),
                       'cause': metaDictionary['reference_genome_id']})
            return None

    # if this file type doesn't take a data file, make sure one isn't parsed
    if (
            'data_file_path' in metaDictionary and
            'data_file_path' not in META_FIELD_MAP[meta_file_type]):
        logger.warning(
            "File '%s' referenced by meta file will not be processed as the "
            "attribute data_file_path is not expected in this meta file",
            metaDictionary['data_file_path'],
            extra={'data_filename': getFileFromFilepath(filename),
                   'cause': metaDictionary['data_file_path']})

    return metaDictionary


def process_metadata_files(directory, logger, hugo_entrez_map):

    """Parse the meta files in a directory and create data file validators.

    Return a tuple of:
        1. a dict listing the data file validator (or None) for each meta file
           by file type,
        2. a list of cancer type ids that have been defined in this study, and
        3. the study id

    Possible file types are listed in META_FILE_PATTERNS.
    """

    # get filenames for all meta files in the directory
    filenames = [os.path.join(directory, f) for
                 f in os.listdir(directory) if
                 re.search(r'(\b|_)meta(\b|_)', f)]

    study_id = None
    study_cancer_type = None
    validators_by_type = {}
    defined_cancer_types = []

    for filename in filenames:

        meta = parse_metadata_file(filename, logger, study_id)
        if meta is None:
            continue
        if study_id is None:
            study_id = meta['cancer_study_identifier']
        meta_file_type = meta['meta_file_type']
        if meta_file_type == STUDY_META_PATTERN:
            if study_cancer_type is not None:
                logger.error(
                    'Encountered a second meta_study file',
                    extra={'data_filename': getFileFromFilepath(filename)})
            study_cancer_type = meta['type_of_cancer']
        if meta_file_type == CANCER_TYPE_META_PATTERN:
            file_cancer_type = meta['type_of_cancer']
            if file_cancer_type in defined_cancer_types:
                logger.error(
                    'Cancer type defined a second time in study',
                    extra={'data_filename': getFileFromFilepath(filename),
                           'cause': file_cancer_type})
            defined_cancer_types.append(meta['type_of_cancer'])
        # create a list for the file type in the dict
        if meta_file_type not in validators_by_type:
            validators_by_type[meta_file_type] = []
        # check if data_file_path is set AND if data_file_path is a supported field according to META_FIELD_MAP:
        if 'data_file_path' in meta and 'data_file_path' in META_FIELD_MAP[meta_file_type]:
            validators_by_type[meta_file_type].append(
                ValidatorFactory.createValidator(
                    VALIDATOR_IDS[meta_file_type],
                    hugo_entrez_map,
                    logger,
                    meta))
        else:
            validators_by_type[meta_file_type].append(None)

    if study_cancer_type is None:
        logger.error(
            'Cancer type needs to be defined for a study. Verify that you have a study file '
            'and have defined the cancer type correctly.')
    elif not (study_cancer_type in PORTAL_CANCER_TYPES or
            study_cancer_type in defined_cancer_types):
        logger.error(
            'Cancer type of study is neither known to the portal nor defined '
            'in a meta_cancer_type file',
            extra={'cause': study_cancer_type})

    return validators_by_type, defined_cancer_types, study_id


def getFileFromFilepath(f):
    return os.path.basename(f.strip())


def processCaseListDirectory(caseListDir, cancerStudyId, logger):

    logger.info('Validating case lists')

    case_lists = [os.path.join(caseListDir, x) for x in os.listdir(caseListDir)]

    for case in case_lists:

        case_data = parse_metadata_file(case, logger, cancerStudyId,
                                        case_list=True)
        if case_data is None:
            continue

        sampleIds = case_data['case_list_ids']
        sampleIds = set([x.strip() for x in sampleIds.split('\t')])
        for value in sampleIds:
            if value not in DEFINED_SAMPLE_IDS:
                logger.error(
                    'Sample id not defined in clinical file',
                    extra={'data_filename': getFileFromFilepath(case),
                           'cause': value})

    logger.info('Validation of case lists complete')


def request_from_portal_api(service_url, logger, id_field=None):
    """Send a request to the portal API and return the decoded JSON object.

    If id_field is specified, expect the object to be a list of dicts,
    and instead return a dict indexed by the specified field of said
    dictionaries. E.g.:
    [{'id': 'spam', 'val1': 1}, {'id':'eggs', 'val1':42}] ->
    {'spam': {'val1': 1}, 'eggs': {'val1': 42}}
    """
    url_split = service_url.split('/api/', 1)
    logger.info("Requesting %s from portal at '%s'",
                url_split[1], url_split[0])
    response = requests.get(service_url)
    try:
        response.raise_for_status()
    except requests.exceptions.HTTPError as e:
        raise IOError(
            'Connection error for URL: {url}. Administrator: please check if '
            '[{url}] is accessible. Message: {msg}'.format(url=service_url,
                                                           msg=e.message))
    json_data = response.json()
    if id_field is None:
        return json_data
    else:
        transformed_dict = {}
        # return a dict indexed by the specified field of said
        # dictionaries. E.g.:
        #[{'id': 'spam', 'val1': 1}, {'id':'eggs', 'val1':42}] ->
        # {'spam': {'val1': 1}, 'eggs': {'val1': 42}}
        for attr in json_data:
            # make a copy of the attr dict
            attr_dict = dict(attr)
            # remove id field:
            if not id_field in attr_dict:
                raise RuntimeError('Unexpected error while calling web-service. '
                                   'Please check if given {url} is correct'.format(url=service_url))
            del attr_dict[id_field]
            transformed_dict[attr[id_field]] = attr_dict
        return transformed_dict


def get_hugo_entrez_map(server_url, logger):
    """
    Returns a dict with hugo symbols and respective entrezId, e.g.:
    # dict: {'LOC105377913': '105377913', 'LOC105377912': '105377912',  hugo: entrez, hugo: entrez...
    """
    # TODO implement an API call for gene aliases and include those in the map
    json_data = request_from_portal_api(server_url + '/api/genes', logger)
    # json_data is list of dicts, each entry containing e.g. dict: {'hugo_gene_symbol': 'SRXN1', 'entrez_gene_id': '140809'}
    # We want to transform this to the format dict: {hugo: entrez, hugo: entrez...
    result_dict = {}
    for data_item in json_data:
        result_dict[data_item['hugo_gene_symbol']] = data_item['entrez_gene_id']
    return result_dict


# ------------------------------------------------------------------------------
def interface(args=None):
    parser = argparse.ArgumentParser(description='cBioPortal meta Validator')
    parser.add_argument('-s', '--study_directory', type=str, required=True,
                        help='path to directory.')
    parser.add_argument('-u', '--url_server', type=str, required=False,
                        default='http://localhost/cbioportal',
                        help='URL to cBioPortal server. You can set this if '
                             'your URL is not http://localhost/cbioportal')
    parser.add_argument('-html', '--html_table', type=str, required=False,
                        help='path to html report output file')
    parser.add_argument('-v', '--verbose', required=False, action="store_true",
                        help='list warnings in addition to fatal errors')

    parser = parser.parse_args(args)
    return parser


def main_validate(args):

    """Main function."""

    # global study properties
    global STUDY_DIR
    global DEFINED_SAMPLE_IDS
    global DEFINED_CANCER_TYPES
    # global portal properties
    global SERVER_URL
    global PORTAL_CANCER_TYPES

    # get a logger to emit messages
    logger = logging.getLogger(__name__)
    logger.handlers = []
    logger.setLevel(logging.INFO)
    exit_status_handler = MaxLevelTrackingHandler()
    logger.addHandler(exit_status_handler)

    # process the options
    STUDY_DIR = args.study_directory
    SERVER_URL = args.url_server

    html_output_filename = args.html_table

    verbose = False
    if args.verbose:
        verbose = True

    # check existence of directory
    if not os.path.exists(STUDY_DIR):
        print >> sys.stderr, 'directory cannot be found: ' + STUDY_DIR
        return 2

    # set default message handler
    text_handler = logging.StreamHandler(sys.stdout)
    text_handler.setFormatter(LogfileStyleFormatter())
    collapsing_text_handler = CollapsingLogMessageHandler(
        capacity=1e6,
        flushLevel=logging.CRITICAL,
        target=text_handler)
    if not verbose:
        collapsing_text_handler.setLevel(logging.ERROR)
    logger.addHandler(collapsing_text_handler)

    collapsing_html_handler = None
    html_handler = None
    # add html table handler if applicable
    if html_output_filename:
        # just to make sure users get dependency error at start:
        import jinja2  # pylint: disable=import-error

        html_handler = Jinja2HtmlHandler(
            STUDY_DIR,
            html_output_filename,
            capacity=1e5)
        # TODO extend CollapsingLogMessageHandler to flush to multiple targets,
        # and get rid of the duplicated buffering of messages here
        collapsing_html_handler = CollapsingLogMessageHandler(
            capacity=1e6,
            flushLevel=logging.CRITICAL,
            target=html_handler)
        if not verbose:
            collapsing_html_handler.setLevel(logging.ERROR)
        logger.addHandler(collapsing_html_handler)

    # retrieve cancer types defined in the portal
    PORTAL_CANCER_TYPES = request_from_portal_api(
        SERVER_URL + '/api/cancertypes',
        logger,
        id_field='id')
    # retrieve clinical attributes defined in the portal
    ClinicalValidator.request_attrs(SERVER_URL, logger)

    # Entrez values for Hugo symbols in the portal
    hugo_entrez_map = get_hugo_entrez_map(SERVER_URL, logger)
    # walk over the meta files in the dir and get properties of the study
    (validators_by_meta_type,
     DEFINED_CANCER_TYPES,
     study_id) = process_metadata_files(STUDY_DIR, logger, hugo_entrez_map)

    if CLINICAL_META_PATTERN not in validators_by_meta_type:
        logger.error('No clinical file detected')
        return exit_status_handler.get_exit_status()

    if STUDY_META_PATTERN not in validators_by_meta_type:
        logger.error('No study file detected')
        return exit_status_handler.get_exit_status()
    
    if len(validators_by_meta_type[CLINICAL_META_PATTERN]) != 1:
        if logger.isEnabledFor(logging.ERROR):
            logger.error(
                'Multiple clinical files detected',
                extra={'cause': ', '.join(
                    validator.filenameShort for validator in
                    validators_by_meta_type[CLINICAL_META_PATTERN])})

    # get the validator for the clinical data file
    clinvalidator = validators_by_meta_type[CLINICAL_META_PATTERN][0]
    # parse the clinical data file to get defined sample ids for this study
    clinvalidator.validate()
    if not clinvalidator.fileCouldBeParsed:
        logger.error("Clinical file could not be parsed. Please fix the problems found there first before continuing.")
        return exit_status_handler.get_exit_status()
    
    DEFINED_SAMPLE_IDS = clinvalidator.sampleIds

    # validate non-clinical data files
    for meta_file_type in validators_by_meta_type:
        # skip clinical files, they have already been validated
        if meta_file_type == CLINICAL_META_PATTERN:
            continue
        for validator in validators_by_meta_type[meta_file_type]:
            # if there was no validator for this meta file
            if validator is None:
                continue
            validator.validate()

    case_list_dirname = os.path.join(STUDY_DIR, 'case_lists')
    if not os.path.isdir(case_list_dirname):
        logger.warning("No directory named 'case_lists' found")
    else:
        processCaseListDirectory(case_list_dirname, study_id, logger)

    logger.info('Validation complete')
    exit_status = exit_status_handler.get_exit_status()
    
    if html_handler is not None:
        collapsing_html_handler.flush()
        html_handler.generateHtml()

    return exit_status

# ------------------------------------------------------------------------------
# vamanos 

if __name__ == '__main__':
    try:
        # parse command line options
        args = interface()
        # run the script
        exit_status = main_validate(args)
        print >>sys.stderr, ('Validation of study {status}.'.format(
            status={0: 'succeeded',
                    1: 'failed',
                    2: 'not performed as problems occurred',
                    3: 'succeeded with warnings'}.get(exit_status, 'unknown')))
    finally:
        logging.shutdown()
        del logging._handlerList[:]  # workaround for harmless exceptions on exit
