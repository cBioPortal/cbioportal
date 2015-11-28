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
import getopt
import os
import logging
import logging.handlers
from collections import OrderedDict
from cgi import escape as html_escape


# ------------------------------------------------------------------------------
# globals

# allows script to run with or without the hugoEntrezMap module
hugoEntrezMapPresent = True
try:
    from hugoEntrezMap import ftp_NCBI, parse_ncbi_file
except ImportError:
    print >> sys.stderr, 'Could not find hugoEntrezMap, skipping'
    hugoEntrezMapPresent = False

# Current NCBI build and build counterpart - used in one of the maf checks as well as .seq filename check
NCBI_BUILD_NUMBER = 37
GENOMIC_BUILD_COUNTERPART = 'hg19'


# how we differentiate between files. Names are important!! 
# meta files are checked before the corresponding file
SEG_FILE_PATTERN = '_data_cna_' + GENOMIC_BUILD_COUNTERPART + '.seg'
SEG_META_PATTERN = '_meta_cna_' + GENOMIC_BUILD_COUNTERPART + '_seg.txt'

MUTATION_FILE_PATTERN = '_mutations_extended.txt'
MUTATION_META_PATTERN = 'meta_mutations_extended.txt'

CNA_FILE_PATTERN = '_CNA'
CNA_META_PATTERN = 'meta_CNA'

CLINICAL_FILE_PATTERN = '_clinical'
CLINICAL_META_PATTERN = 'meta_clinical'

LOG2_FILE_PATTERN = '_log2CNA'
LOG2_META_PATTERN = 'meta_log2CNA'

EXPRESSION_FILE_PATTERN = '_expression'
EXPRESSION_META_PATTERN = 'meta_expression'

FUSION_FILE_PATTERN = '_fusions'
FUSION_META_PATTERN = 'meta_fusions'

METHYLATION_FILE_PATTERN = '_methylation'
METHYLATION_META_PATTERN = 'meta_methylation'

RPPA_FILE_PATTERN = '_rppa'
RPPA_META_PATTERN = 'meta_rppa'

TIMELINE_FILE_PATTERN = '_timeline_'
TIMELINE_META_PATTERN = 'meta_timeline'

FILE_PATTERNS = [SEG_FILE_PATTERN,
    MUTATION_FILE_PATTERN,
    CNA_FILE_PATTERN,
    CLINICAL_FILE_PATTERN,
    LOG2_FILE_PATTERN,
    EXPRESSION_FILE_PATTERN,
    FUSION_FILE_PATTERN,
    METHYLATION_FILE_PATTERN,
    RPPA_FILE_PATTERN,
    TIMELINE_FILE_PATTERN
]

META_PATTERNS = [SEG_META_PATTERN,
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


VALIDATOR_IDS = {CNA_FILE_PATTERN:'CNAValidator',
                 MUTATION_FILE_PATTERN:'MutationsExtendedValidator',
                 CLINICAL_FILE_PATTERN:'ClinicalValidator',
                 SEG_FILE_PATTERN:'SegValidator',
                 LOG2_FILE_PATTERN:'Log2Validator',
                 EXPRESSION_FILE_PATTERN:'ExpressionValidator',
                 FUSION_FILE_PATTERN:'FusionValidator',
                 METHYLATION_FILE_PATTERN:'MethylationValidator',
                 RPPA_FILE_PATTERN:'RPPAValidator',
                 TIMELINE_FILE_PATTERN:'TimelineValidator'
                 }


VALIDATOR_META_MAP = {
    VALIDATOR_IDS[MUTATION_FILE_PATTERN]:MUTATION_META_PATTERN,
    VALIDATOR_IDS[CNA_FILE_PATTERN]:CNA_META_PATTERN,
    VALIDATOR_IDS[CLINICAL_FILE_PATTERN]:CLINICAL_META_PATTERN,
    VALIDATOR_IDS[SEG_FILE_PATTERN]:SEG_META_PATTERN,
    VALIDATOR_IDS[LOG2_FILE_PATTERN]:LOG2_META_PATTERN,
    VALIDATOR_IDS[EXPRESSION_FILE_PATTERN]:EXPRESSION_META_PATTERN,
    VALIDATOR_IDS[FUSION_FILE_PATTERN]:FUSION_META_PATTERN,
    VALIDATOR_IDS[METHYLATION_FILE_PATTERN]:METHYLATION_META_PATTERN,
    VALIDATOR_IDS[RPPA_FILE_PATTERN]:RPPA_META_PATTERN,
    VALIDATOR_IDS[TIMELINE_FILE_PATTERN]:TIMELINE_META_PATTERN
}


CNA_META_FIELDS = [
    'cancer_study_identifier',
    'genetic_alteration_type',
    'datatype',
    'stable_id',
    'show_profile_in_analysis_tab',
    'profile_name',
    'profile_description'
]

MUTATION_META_FIELDS = [
    'cancer_study_identifier',
    'genetic_alteration_type',
    'datatype',
    'stable_id',
    'show_profile_in_analysis_tab',
    'profile_name',
    'profile_description'
]

SEG_META_FIELDS = [
    'cancer_study_identifier',
    'genetic_alteration_type',
    'datatype',
    'stable_id',
    'show_profile_in_analysis_tab',
    'profile_name',
    'profile_description',
    'reference_genome_id',
    'data_filename',
    'description'
]

LOG2_META_FIELDS = [
    'cancer_study_identifier',
    'genetic_alteration_type',
    'datatype',
    'stable_id',
    'show_profile_in_analysis_tab',
    'profile_name',
    'profile_description'
]

EXPRESSION_META_FIELDS = [
    'cancer_study_identifier',
    'genetic_alteration_type',
    'datatype',
    'stable_id',
    'show_profile_in_analysis_tab',
    'profile_name',
    'profile_description'
]

METHYLATION_META_FIELDS = [
    'cancer_study_identifier',
    'genetic_alteration_type',
    'datatype',
    'stable_id',
    'show_profile_in_analysis_tab',
    'profile_name',
    'profile_description'
]

FUSION_META_FIELDS = [
    'cancer_study_identifier',
    'genetic_alteration_type',
    'datatype',
    'stable_id',
    'show_profile_in_analysis_tab',
    'profile_name',
    'profile_description'
]

RPPA_META_FIELDS = [
    'cancer_study_identifier',
    'genetic_alteration_type',
    'datatype',
    'stable_id',
    'show_profile_in_analysis_tab',
    'profile_name',
    'profile_description'
]

TIMELINE_META_FIELDS = [
    'cancer_study_identifier',
    'genetic_alteration_type'
]

CASE_LIST_FIELDS = [
    'cancer_study_identifier',
    'stable_id',
    'case_list_name',
    'case_list_description',
    'case_list_ids',
    'case_list_category'
]

CLINICAL_META_FIELDS = [
    'cancer_study_identifier',
    'genetic_alteration_type',
    'datatype',
    'stable_id',
    'show_profile_in_analysis_tab',
    'profile_name',
    'profile_description'
]

META_FIELD_MAP = {
    CNA_META_PATTERN:CNA_META_FIELDS,
    CLINICAL_META_PATTERN:CLINICAL_META_FIELDS,
    LOG2_META_PATTERN:LOG2_META_FIELDS,
    MUTATION_META_PATTERN:MUTATION_META_FIELDS,
    SEG_META_PATTERN:SEG_META_FIELDS,
    EXPRESSION_META_PATTERN:EXPRESSION_META_FIELDS,
    METHYLATION_META_PATTERN:EXPRESSION_META_FIELDS,
    FUSION_META_PATTERN:FUSION_META_FIELDS,
    RPPA_META_PATTERN:RPPA_META_FIELDS,
    TIMELINE_META_PATTERN:TIMELINE_META_FIELDS
}

# allows pass/fail to be passed programatically throughout program. If failure condition found, set to 1
exitcode = 0


# ------------------------------------------------------------------------------
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
            "; found '%s'",
            "; found ['%s']",
            join_string="', '",
            optional=True)

        return super(LogfileStyleFormatter, self).format(record)


class SimpleHtmlTableFormatter(ValidationMessageFormatter):
    """Formatter writing messages to an order-of-appearance HTML table."""

    def __init__(self):
        """Initialize a logging Formatter with an appropriate format string."""
        super(SimpleHtmlTableFormatter, self).__init__(
            fmt='  <tr class="%(level_string)s">\n'
                '    <td>%(file_string)s</td>\n'
                '    <td>%(line_string)s</td>\n'
                '    <td>%(column_string)s</td>\n'
                '    <td>%(message_string)s</td>\n'
                '    <td>%(cause_string)s</td>\n'
                '  </tr>')

    def format(self, record):

        """Generate descriptions for optional fields and format the record."""

        record.level_string = html_escape(
            record.levelname.lower(),
            quote=True)
        record.message_string = html_escape(
            record.getMessage())
        record.file_string = html_escape(
            self.format_aggregated(record,
                                   'data_filename',
                                   optional=True))
        record.line_string = html_escape(
            self.format_aggregated(record,
                                   'line_number',
                                   single_fmt='%d',
                                   multiple_fmt='%s',
                                   optional=True))
        record.column_string = html_escape(
            self.format_aggregated(record,
                                   'column_number',
                                   single_fmt='%d',
                                   multiple_fmt='%s:',
                                   optional=True))
        record.cause_string = html_escape(
            self.format_aggregated(record,
                                   'cause',
                                   single_fmt="%s",
                                   multiple_fmt="%s",
                                   optional=True))

        return super(SimpleHtmlTableFormatter, self).format(record)


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
                # collect the values found for this field across the records
                field_values = set(getattr(record, field_name)
                                   for record in record_list)
                # if this field has the same value in all records
                if len(field_values) == 1:
                    # use that value in the new dict
                    aggregated_field_dict[field_name] = field_values.pop()
                else:
                    # set a <field>_list field instead
                    aggregated_field_dict[field_name + '_list'] = \
                        list(field_values)

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

    # TODO remove this singleton factory, multiple files are multiple files

    factories = {}

    @classmethod
    def createValidator(cls, validator_type, filename, hugo_entrez_map, fix, logger, stableId):
        if validator_type not in cls.factories:
            # instantiate a factory for the given validator type
            factory = globals()[validator_type].Factory()
            cls.factories[validator_type] = factory
        return cls.factories[validator_type].create(filename,hugo_entrez_map,fix,logger,stableId)


class Validator(object):

    """Abstract validator class.

    Subclassed by validators for specific data file types, which should
    define a 'REQUIRED_HEADERS' attribute listing the required column
    headers and a `REQUIRE_COLUMN_ORDER` boolean stating whether their
    position is significant, and may implement a processTopLine method
    to handle lines prefixed with '#'.
    """

    def __init__(self,filename,hugo_entrez_map,fix,logger,stableId):
        self.filename = filename
        self.filenameShort = os.path.basename(filename)
        self.file = open(filename, 'rU')
        self.line_number = 0
        self.sampleIds = set()
        self.cols = []
        self.numCols = 0
        self.hugo_entrez_map = hugo_entrez_map
        self.lineEndings = ''
        self.fileRead = self.file.read()
        self.file.seek(0,0)
        self.end = False
        self.fix = fix
        self.studyId = ''
        self.headerWritten = False
        self.logger = CombiningLoggerAdapter(
            logger,
            extra={'data_filename': self.filenameShort})
        self.stableId = stableId
        self.badChars = [' ']

        if fix:
            self.correctedFilename = '{basename}_{stable_id}.txt'.format(
                basename=os.path.splitext(os.path.basename(self.filename))[0],
                stable_id=self.stableId)
            # TODO consider opening the file in validate()
            self.correctedFile = open(self.correctedFilename,'w')

    def validate(self):

        """Validate method - initiates validation of file."""

        self.logger.info('Starting validation of file')

        self.checkLineBreaks()
        self.checkQuotes()

        uncommented_line_number = 0
        for line_index, line in enumerate(self.file):
            self.line_number = line_index + 1
            # TODO test for # lines after non-# lines
            if not line.startswith('#'):
                uncommented_line_number += 1
                if uncommented_line_number == 1:
                    self.checkHeader(line)
                elif not self.end:
                    self.checkLine(line)
            else:
                # TODO make a function to parse initial multi-line comments,
                # as these are required in clinical data files

                # This method may or may not be implemented by subclasses
                processTopLine = getattr(self, 'processTopLine', None)
                if processTopLine is not None:
                    processTopLine(line)

        self.file.close()
        if self.fix:
            self.correctedFile.close()

    def printComplete(self):
        self.logger.info('Validation of file complete')

    def checkHeader(self,line):

        """Header check function. Checks that header has the correct items, removes any quotes."""

        # TODO check for end-of-line whitespace
        # TODO verify that this really is the desired behavior
        self.cols = [x.strip().replace('"','').replace('\'','') for x in line.strip().split('\t')]
        self.numCols = len(self.cols)

        self.checkRepeatedColumns()

        self.checkBadChar()
        # 'REQUIRE_COLUMN_ORDER' should have been defined by the subclass
        if self.REQUIRE_COLUMN_ORDER:  # pylint: disable=no-member
            self.checkOrderedRequiredColumns()
        else:
            self.checkUnorderedRequiredColumns()


    def checkLine(self,line):

        """Checks lines after header, removing quotes."""

        # TODO check for end-of-line whitespace
        # TODO verify that this is really the desired behavior
        data = [x.strip().replace('"','').replace('\'','') for x in line.split('\t')]

        if all(x == '' for x in data):
            self.logger.error("Blank line",
                              extra={'line_number': self.line_number})

        if (
                data[:self.numCols] == self.cols or
                data[:len(self.REQUIRED_HEADERS)] == self.REQUIRED_HEADERS):  # pylint: disable=no-member
            if self.logger.isEnabledFor(logging.ERROR):
                self.logger.error(
                    'Repeated header',
                    extra={'line_number': self.line_number,
                           'cause': ', '.join(data[:self.numCols])})
            exitcode = 1

        line_col_count = len(data)

        if line_col_count != self.numCols:
            self.logger.error('Expected %d columns based on header, '
                              'found %d',
                              self.numCols, line_col_count,
                              extra={'line_number': self.line_number})
            exitcode = 1

        for col_index, col_name in enumerate(self.REQUIRED_HEADERS):  # pylint: disable=no-member
            if col_index < line_col_count and data[col_index] == '':
                self.logger.error("Blank cell found in column '%s'",
                                  col_name,
                                  extra={'line_number': self.line_number,
                                         'column_number': col_index + 1})

        data = [self.fixCase(x) for x in data]

        return data

    def checkUnorderedRequiredColumns(self):
        """Check for missing column headers, independent of their position."""
        num_errors = 0
        # 'REQUIRED_HEADERS' should have been defined by the subclass
        for col_name in self.REQUIRED_HEADERS:  # pylint: disable=no-member
            if col_name not in self.cols:
                num_errors += 1
                if self.logger.isEnabledFor(logging.ERROR):
                    self.logger.error(
                        'Missing column: %s',
                        col_name,
                        extra={'line_number': self.line_number,
                               'cause': ', '.join(self.cols[:len(self.REQUIRED_HEADERS)]) +  # pylint: disable=no-member
                                        ', (...)'})
        return num_errors

    def checkOrderedRequiredColumns(self):
        """Check if the column header for each position is correct."""
        num_errors = 0
        # 'REQUIRED_HEADERS' should have been defined by the subclass
        for col_index, col_name in enumerate(self.REQUIRED_HEADERS):  # pylint: disable=no-member
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

    def checkQuotes(self):
        if '"' in self.fileRead or '\'' in self.fileRead:
            self.logger.warning('Found quotation marks in file')
            exitcode = 0

    def checkLineBreaks(self):
        """Checks line breaks, reports to user."""
        # TODO document these requirements
        if "\r\n" in self.fileRead:
            self.lineEndings = "\r\n"
            exitcode = 1
            self.logger.error('DOS-style line breaks detected (\\r\\n), '
                              'should be Unix-style (\\n)')
            if self.fix:
                self.logger.info('Corrected file will have Unix (\\n) line breaks')
        elif "\r" in self.fileRead:
            self.lineEndings = "\r"
            exitcode = 1
            self.logger.error('Classic Mac OS-style line breaks detected '
                              '(\\r), should be Unix-style (\\n)')
            if self.fix:
                self.logger.info('Corrected file will have Unix (\\n) line breaks')
        elif "\n" in self.fileRead:
            self.lineEndings = "\n"
        else:
            self.logger.error('No line breaks recognized in file')
            exitcode = 1


    def checkInt(self,value):
        """Checks if a value is an integer."""
        try:
            int(value)
            return True
        except ValueError:
            return False

    def writeNewLine(self, data):
        """Write a line of data to the corrected file."""
        # replace blanks with 'NA'
        data = [x if x != '' else 'NA' for x in data]
        self.correctedFile.write('\t'.join(data) + '\n')

    def writeHeader(self, data):
        """Write a column header to the corrected file."""
        self.correctedFile.write('\t'.join(data) + '\n')

    def checkRepeatedColumns(self):
        seen = set()
        for col_num, col in enumerate(self.cols):
            if col not in seen:
                seen.add(col)
            else:
                self.logger.error('Repeated column header',
                                  extra={'line_number': self.line_number,
                                         'column_number': col_num,
                                         'cause': col})
                exitcode = 1

    def checkBadChar(self):
        """Check for bad things in a header, such as spaces, etc."""
        for col_num, col_name in enumerate(self.cols):
            for bc in self.badChars:
                if bc in col_name:
                    self.logger.error("Bad character '%s' detected in header",
                                      bc,
                                      extra={'line_number': self.line_number,
                                             'column_number': col_num,
                                             'cause': col_name})

    def fixCase(self,x):
        """Correct yes no to Yes and No."""
        # TODO document these requirements
        if x.lower() == 'yes':
            return 'Yes'
        elif x.lower() == 'no':
            return 'No'
        elif x.lower() == 'male':
            return 'Male'
        elif x.lower() == 'female':
            return 'Female'
        else:
            return x


class FeaturewiseFileValidator(Validator):

    """Validates a file with rows for features and columns for ids and samples.

    The first few columns (defined in the REQUIRED_HEADERS attribute)
    identify the features/genes, and the rest correspond to the samples.

    Subclasses should define a checkValue(self, value, col_index) function
    to check a value in a sample column, and check the required columns
    by overriding checkLine(self, line), which returns the list of values
    found on the line.
    """

    REQUIRE_COLUMN_ORDER = True

    def checkHeader(self, line):
        """Validate the header and read sample IDs from it.

        Return the number of fatal errors.
        """
        num_errors = super(FeaturewiseFileValidator, self).checkHeader(line)
        self.setSampleIdsFromColumns()
        return num_errors

    def checkLine(self, line):
        """Check the values in a data line."""
        data = super(FeaturewiseFileValidator, self).checkLine(line)
        for column_index, value in enumerate(data):
            if column_index >= len(self.REQUIRED_HEADERS):  # pylint: disable=no-member
                # checkValue() should be implemented by subclasses
                self.checkValue(value, column_index)  # pylint: disable=no-member
        return data

    def setSampleIdsFromColumns(self):
        """Extracts sample IDs from column headers and set self.sampleIds."""
        # `REQUIRED_HEADERS` should have been set by a subclass
        num_nonsample_headers = len(self.REQUIRED_HEADERS)  # pylint: disable=no-member
        self.sampleIds = self.cols[num_nonsample_headers:]


class GenewiseFileValidator(FeaturewiseFileValidator):

    REQUIRED_HEADERS = ['Hugo_symbol', 'Entrez_Gene_Id']

    def __init__(self, *args, **kwargs):
        super(GenewiseFileValidator, self).__init__(*args, **kwargs)
        self.entrez_missing = False

    def checkHeader(self,line):
        """Validate the header and read sample IDs from it.

        Return the number of fatal errors.
        """
        num_errors = super(GenewiseFileValidator, self).checkHeader(line)
        if self.numCols < 2 or self.cols[1] != self.REQUIRED_HEADERS[1]:
            self.entrez_missing = True
            # if fixing, do not count a missing Entrez column as a fatal error
            if self.fix:
                num_errors -= 1
                # override REQUIRED_HEADERS with a copy in the instance
                self.REQUIRED_HEADERS = list(self.REQUIRED_HEADERS)
                # do not expect the Entrez ID column from now on
                del self.REQUIRED_HEADERS[1]
        return num_errors

    def checkLine(self, line):
        """Check the values in a data line."""
        data = super(GenewiseFileValidator, self).checkLine(line)
        for column_index, value in enumerate(data):
            if column_index == 0 and len(self.hugo_entrez_map) > 0:
                if value not in self.hugo_entrez_map:
                    self.logger.warning(
                        'Hugo symbol appears incorrect',
                        extra={'line_number': self.line_number,
                               'column_number': column_index + 1,
                               'cause': value.strip()})
            elif not self.entrez_missing and column_index == 1:
                if not self.checkInt(value.strip()) and not value.strip() == 'NA':
                    self.logger.warning(
                        'Invalid Data Type: Entrez_Gene_Id must be integer or NA',
                        extra={'column_number': column_index + 1,
                               'line_number': self.line_number,
                               'cause': value.strip()})
        return data

    def writeNewLine(self, data):
        if self.entrez_missing:
            data.insert(1,self.hugo_entrez_map.get(data[0],'NA'))
        super(GenewiseFileValidator, self).writeNewLine(data)

    def writeHeader(self, data):
        if self.entrez_missing:
            data.insert(1,'Entrez_Gene_Id')
        super(GenewiseFileValidator, self).writeHeader(data)


class CNAValidator(GenewiseFileValidator):

    """Sub-class CNA validator."""

    ALLOWED_VALUES = ['-2','-1','0','1','2','','NA']

    # TODO refactor so subclasses don't have to override for the final call
    def validate(self):
        super(CNAValidator,self).validate()
        self.printComplete()

    # TODO refactor so subclasses don't have to override for the final call
    def checkHeader(self,line):
        """Header validation for CNA files."""
        super(CNAValidator,self).checkHeader(line)
        if self.fix:
            self.writeHeader(self.cols)

    # TODO refactor so subclasses don't have to override for the final call
    def checkLine(self,line):
        """Line validation for CNA files - checks that values are correct type."""
        data = super(CNAValidator,self).checkLine(line)
        if self.fix:
            self.writeNewLine(data)

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

    class Factory(object):
        def create(self,filename,hugo_entrez_map,fix,logger,stableId):
            return CNAValidator(filename,hugo_entrez_map,fix,logger,stableId)

class MutationsExtendedValidator(Validator):

    """Sub-class mutations_extended validator."""

    MAF_HEADERS = [
        'Hugo_Symbol',
        'Entrez_Gene_Id',
        'Center',
        'NCBI_Build',
        'Chromosome',
        'Start_Position',
        'End_Position',
        'Strand',
        'Variant_Classification',
        'Variant_Type',
        'Reference_Allele',
        'Tumor_Seq_Allele1',
        'Tumor_Seq_Allele2',
        'dbSNP_RS',
        'dbSNP_Val_Status',
        'Tumor_Sample_Barcode',
        'Matched_Norm_Sample_Barcode',
        'Match_Norm_Seq_Allele1',
        'Match_Norm_Seq_Allele2',
        'Tumor_Validation_Allele1',
        'Tumor_Validation_Allele2',
        'Match_Norm_Validation_Allele1',
        'Match_Norm_Validation_Allele2',
        'Verification_Status',
        'Validation_Status',
        'Mutation_Status',
        'Sequencing_Phase',
        'Sequence_Source',
        'Validation_Method',
        'Score',
        'BAM_File',
        'Sequencer']
    CUSTOM_HEADERS = [
        't_alt_count',
        't_ref_count',
        'n_alt_count',
        'n_ref_count']
    REQUIRED_HEADERS = MAF_HEADERS + CUSTOM_HEADERS
    REQUIRE_COLUMN_ORDER = True

    # Used for mapping column names to the corresponding function that does a check on the value.
    # This can be done for other filetypes besides maf - not currently implemented.
    CHECK_FUNCTION_MAP = {
        'Hugo_Symbol':'checkValidHugo',
        'Entrez_Gene_Id':'checkValidEntrez',
        'Center':'checkCenter',
        'NCBI_Build':'checkNCBIbuild',
        'Chromosome':'checkChromosome',
        'Start_Position':'checkStartPosition',
        'End_Position':'checkEndPosition',
        'Strand':'checkStrand',
        'Variant_Classification':'checkVariantClassification',
        'Variant_Type':'checkVariantType',
        'Reference_Allele':'checkRefAllele',
        'Tumor_Seq_Allele1':'checkTumorSeqAllele',
        'Tumor_Seq_Allele2':'checkTumorSeqAllele',
        'dbSNP_RS':'checkdbSNP_RS',
        'dbSNP_Val_Status':'check_dbSNPValStatus',
        'Tumor_Sample_Barcode':'checkTumorSampleBarcode',
        'Matched_Norm_Sample_Barcode':'checkMatchedNormSampleBarcode',
        'Match_Norm_Seq_Allele1':'checkMatchNormSeqAllele',
        'Match_Norm_Seq_Allele2':'checkMatchNormSeqAllele',
        'Tumor_Validation_Allele1':'checkTumorValidationAllele',
        'Tumor_Validation_Allele2':'checkTumorValidationAllele',
        'Match_Norm_Validation_Allele1':'checkMatchNormValidationAllele',
        'Match_Norm_Validation_Allele2':'checkMatchNormValidationAllele',
        'Verification_Status':'checkVerificationStatus',
        'Validation_Status':'checkValidationStatus',
        'Mutation_Status':'checkMutationStatus',
        'Sequencing_Phase':'checkSequencingPhase',
        'Sequence_Source':'checkSequenceSource',
        'Validation_Method':'checkValidationMethod',
        'Score':'checkScore',
        'BAM_File':'checkBAMFile',
        'Sequencer':'checkSequencer',
        't_alt_count':'check_t_alt_count',
        't_ref_count':'check_t_ref_count',
        'n_alt_count':'check_n_alt_count',
        'n_ref_count':'check_n_ref_count'}

    def __init__(self,filename,hugo_entrez_map,fix,logger,stableId):
        super(MutationsExtendedValidator,self).__init__(filename,hugo_entrez_map,fix,logger,stableId)
        # TODO parse the version number in the comment on the first line,
        # and reject unsupported versions (and/or override REQUIRED_HEADERS)
        self.mafValues = {}
        self.entrez_missing = False
        self.extraCols = []
        self.extra_exists = False
        self.extra = ''
        # TODO remove the attributes below, they violate the MAF standard
        self.toplinecount = 0
        self.sampleIdsHeader = set()
        self.headerPresent = False

    def validate(self):
        super(MutationsExtendedValidator,self).validate()
        self.printComplete()

    def checkHeader(self,line):
        super(MutationsExtendedValidator,self).checkHeader(line)
        if self.fix:
            self.writeHeader(line)

    def checkLine(self,line):

        """Each value in each line is checked individually.

        From the column name (stored in self.cols), the
        corresponding function to check the value is selected from
        CHECK_FUNCTION_MAP. Will emit a generic warning
        message if this function returns False. If the function sets
        self.extra_exists to True, self.extra will be used in this
        message.
        """

        data = super(MutationsExtendedValidator,self).checkLine(line)

        for col_index, value in enumerate(data[:len(self.REQUIRED_HEADERS)]):
            # get the checking method for this column if available, or None
            checking_function = getattr(
                self,
                self.CHECK_FUNCTION_MAP.get(self.REQUIRED_HEADERS[col_index], ''),
                None)
            # if it is actually a method, and not None
            if callable(checking_function):
                if not checking_function(value):
                    self.printDataInvalidStatement(value, col_index)
                elif self.extra_exists or self.extra != '':
                    raise ValueError(('Checking function %s set a warning '
                                      'message but reported no warning') %
                                     checking_function.__name__)
            self.mafValues[self.REQUIRED_HEADERS[col_index]] = value

        if self.fix:
            self.writeNewLine(data)

    def processTopLine(self,line):
        """Processes the top line, which contains sample ids used in study."""
        # TODO remove this function, it violates the MAF standard
        self.headerPresent = True
        topline = [x.strip() for x in line.split(' ') if '#' not in x]

        self.toplinecount += 1
        for sampleId in topline:
            self.sampleIdsHeader.add(sampleId)

        if self.fix:
            self.correctedFile.write(line)

    def printDataInvalidStatement(self, value, col_index):
        """Prints out statement for invalid values detected."""
        message = ("Value in column '%s' appears invalid" %
                   self.REQUIRED_HEADERS[col_index])
        if self.extra_exists:
            message = self.extra
            self.extra = ''
            self.extra_exists = False
        self.logger.warning(
            message,
            extra={'line_number': self.line_number,
                   'column_number': col_index + 1,
                   'cause': value})
        exitcode = 0

    def writeNewLine(self,data):
        newline = []
        for col in self.REQUIRED_HEADERS:
            newline.append(self.mafValues.get(col,'NA'))
        if self.entrez_missing:
            newline[1] = self.hugo_entrez_map.get(newline[0],'NA')
        super(MutationsExtendedValidator, self).writeNewLine(newline)

    def writeHeader(self, data):
        super(MutationsExtendedValidator, self).writeHeader(
            self.REQUIRED_HEADERS)

    # These functions check values of the MAF according to their name.
    # The mapping of which function checks which value is a global value
    # at the top of the script. If any other checks need to be added for
    # another field name, add the map in the global corresponding to
    # the function name that is created to check it.

    def checkValidHugo(self,value):
        """Checks if a value is a valid Hugo symbol listed in the NCBI file.

        If no NCBI file given at runtime, does nothing.
        """
        return (self.hugo_entrez_map == {}) or (value in self.hugo_entrez_map)

    def checkValidEntrez(self, value):
        """Checks if a value is a valid entrez id for the given hugo - needs to be present and match."""
        if self.entrez_missing:
            raise ValueError('Tried to check an Entrez id in a file without '
                             'Entrez ids')
        if value == '':
            self.entrez_missing = True
        elif (
                self.hugo_entrez_map != {} and
                value not in self.hugo_entrez_map.values()):
            return False
        elif (
                self.hugo_entrez_map != {} and
                'Hugo_Symbol' in self.mafValues and
                (self.hugo_entrez_map.get(self.mafValues['Hugo_Symbol']) !=
                    value)):
            self.extra =  \
                'Entrez gene id does not match Hugo symbol ({} -> {})'.format(
                    self.mafValues['Hugo_Symbol'],
                    self.hugo_entrez_map[self.mafValues['Hugo_Symbol']])
            self.extra_exists = True
            return False
        return True

    def checkCenter(self, value):
        return True

    def checkChromosome(self, value):
        if self.checkInt(value):
            if 1 <= int(value) <= 22:
                return True
            return False
        elif value in ('X', 'Y', 'M'):
            return True
        return False
    
    def checkStartPosition(self, value):
        return True
 
    def checkEndPosition(self, value):
        return True
   
    def checkTumorSampleBarcode(self, value):
        self.sampleIds.add(value.strip())
        if self.headerPresent and value not in self.sampleIdsHeader:
            self.extra = 'Tumor sample id not in sample ids from header'
            self.extra_exists = True
            return False
        return True

    def checkNCBIbuild(self, value):
        if self.checkInt(value) and value != '':
            if int(value) != NCBI_BUILD_NUMBER:
                return False
        return True
    
    def checkStrand(self, value):
        if value != '+':
            return False
        return True
    
    def checkVariantClassification(self, value):
        return True

    def checkVariantType(self, value):
        return True
    
    def checkRefAllele(self, value):
        return True

    def checkTumorSeqAllele(self, value):
        return True
    
    def check_dbSNPRS(self, value):
        return True

    def check_dbSNPValStatus(self, value):
        return True
    
    def checkMatchedNormSampleBarcode(self, value):
        if value != '':
            if self.headerPresent and value not in self.sampleIdsHeader:
                self.extra = 'Normal sample id not in sample ids from header'
                self.extra_exists = True
                return False
        return True
    
    def checkMatchedNormSampleBarcodehNormSeqAllele(self, value):
        return True
    
    def checkTumorValidationAllele(self, value):
        return True
    
    def checkMatchNormValidationAllele(self, value):
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
    
    def checkMutationStatus(self, value):
        return True
    
    def checkSequencingPhase(self, value):
        return True
    
    def checkSequenceSource(self, value):
        return True
    
    def checkValidationMethod(self, value):
        return True
    
    def checkScore(self, value):
        return True
    
    def checkBAMFile(self, value):
        return True
    
    def checkSequencer(self, value):
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

    class Factory(object):
        def create(self,filename,hugo_entrez_map,fix,logger,stableId):
            return MutationsExtendedValidator(filename,hugo_entrez_map,fix,logger,stableId)

class ClinicalValidator(Validator):

    """Validator for clinical data files."""

    REQUIRED_HEADERS = [
        'PATIENT_ID',
        'SAMPLE_ID',
        'OS_MONTHS',
        'OS_STATUS',
        'DFS_MONTHS',
        'DFS_STATUS']
    REQUIRE_COLUMN_ORDER = True

    def validate(self):
        super(ClinicalValidator,self).validate()
        self.printComplete()

    # TODO validate the content of the comment lines before the column header

    def checkHeader(self,line):
        super(ClinicalValidator,self).checkHeader(line)
        for col_name in self.cols:
            if not col_name.isupper():
                self.logger.warning(
                    "Clinical header not in all caps",
                    extra={'line_number': self.line_number,
                           'cause': col_name})
                exitcode = 0
        self.cols = [s.upper() for s in self.cols]
        if self.fix:
            self.writeHeader(self.cols)

    def checkLine(self,line):
        data = super(ClinicalValidator,self).checkLine(line)
        for col_index, value in enumerate(data):
            # TODO check the values in the other cols, required and optional
            try:
                if col_index == self.cols.index(self.REQUIRED_HEADERS[1]):
                    self.sampleIds.add(value.strip())
            except ValueError:
                continue
        if self.fix:
            self.writeNewLine(data)

    def writeHeader(self,data):
        self.correctedFile.write('\t'.join(data) + '\n')

    class Factory(object):
        def create(self,filename,hugo_entrez_map,fix,logger,stableId):
            return ClinicalValidator(filename,hugo_entrez_map,fix,logger,stableId)


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

    def __init__(self,filename,hugo_entrez_map,fix,logger,stableId):
        super(SegValidator,self).__init__(filename,hugo_entrez_map,fix,logger,stableId)
        self.sampleIds = set()

    def validate(self):
        super(SegValidator,self).validate()
        self.printComplete()

    def checkHeader(self,line):
        super(SegValidator,self).checkHeader(line)
        if self.fix:
            self.writeHeader(self.cols)

    def checkLine(self,line):
        data = super(SegValidator,self).checkLine(line)

        # if present, add sample id to set for later checks
        for col_index, value in enumerate(data):
            try:
                if col_index == self.cols.index(self.REQUIRED_HEADERS[0]):
                    self.sampleIds.add(value.strip())
            except ValueError:
                continue

        if self.fix:
            self.writeNewLine(data)


    class Factory(object):
        def create(self,filename,hugo_entrez_map,fix,logger,stableId):
            return SegValidator(filename,hugo_entrez_map,fix,logger,stableId)


class Log2Validator(GenewiseFileValidator):

    def validate(self):
        super(Log2Validator,self).validate()
        self.printComplete()

    def checkHeader(self,line):
        super(Log2Validator,self).checkHeader(line)
        if self.fix:
            self.writeHeader(self.cols)

    def checkLine(self,line):
        data = super(Log2Validator,self).checkLine(line)
        if self.fix:
            self.writeNewLine(data)

    def checkValue(self, value, col_index):
        """Check a value in a sample column."""
        # TODO check these values
        pass

    class Factory(object):
        def create(self,filename,hugo_entrez_map,fix,logger,stableId):
            return Log2Validator(filename,hugo_entrez_map,fix,logger,stableId)


class ExpressionValidator(GenewiseFileValidator):

    def validate(self):
        super(ExpressionValidator,self).validate()
        self.printComplete()

    def checkHeader(self,line):
        super(ExpressionValidator,self).checkHeader(line)
        if self.fix:
            self.writeHeader(self.cols)

    def checkLine(self,line):
        data = super(ExpressionValidator,self).checkLine(line)
        if self.fix:
            self.writeNewLine(data)

    def checkValue(self, value, col_index):
        """Check a value in a sample column."""
        # TODO check these values
        pass

    class Factory(object):
        def create(self,filename,hugo_entrez_map,fix,logger,stableId):
            return ExpressionValidator(filename,hugo_entrez_map,fix,logger,stableId)


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

    def validate(self):
        super(FusionValidator,self).validate()
        self.printComplete()

    def checkHeader(self,line):
        super(FusionValidator,self).checkHeader(line)

        if self.fix:
            self.writeHeader(self.cols)

    def checkLine(self,line):
        data = super(FusionValidator,self).checkLine(line)

        if self.fix:
            self.writeNewLine(data)

    class Factory(object):
        def create(self,filename,hugo_entrez_map,fix,logger,stableId):
            return FusionValidator(filename,hugo_entrez_map,fix,logger,stableId)


class MethylationValidator(GenewiseFileValidator):

    def validate(self):
        super(MethylationValidator,self).validate()
        self.printComplete()

    def checkHeader(self,line):
        super(MethylationValidator,self).checkHeader(line)
        if self.fix:
            self.writeHeader(self.cols)

    def checkLine(self,line):
        data = super(MethylationValidator,self).checkLine(line)
        if self.fix:
            self.writeNewLine(data)

    def checkValue(self, value, col_index):
        """Check a value in a sample column."""
        # TODO check these values
        pass

    class Factory(object):
        def create(self,filename,hugo_entrez_map,fix,logger,stableId):
            return MethylationValidator(filename,hugo_entrez_map,fix,logger,stableId)


class RPPAValidator(FeaturewiseFileValidator):

    REQUIRED_HEADERS = ['Composite.Element.REF']

    def validate(self):
        super(RPPAValidator,self).validate()
        self.printComplete()

    def checkHeader(self,line):
        super(RPPAValidator,self).checkHeader(line)
        if self.fix:
            self.writeHeader(self.cols)

    def checkLine(self,line):
        data = super(RPPAValidator,self).checkLine(line)
        # TODO check the values in the first column
        # for rppa, first column should be hugo|antibody, everything after should be sampleIds
        if self.fix:
            self.writeNewLine(data)


    def checkValue(self, value, col_index):
        """Check a value in a sample column."""
        # TODO check these values
        pass

    class Factory(object):
        def create(self,filename,hugo_entrez_map,fix,logger,stableId):
            return RPPAValidator(filename,hugo_entrez_map,fix,logger,stableId)


class TimelineValidator(Validator):

    REQUIRED_HEADERS = [
        'PATIENT_ID',
        'START_DATE',
        'STOP_DATE',
        'EVENT_TYPE']
    REQUIRE_COLUMN_ORDER = True

    def validate(self):
        super(TimelineValidator,self).validate()
        self.printComplete()

    def checkHeader(self,line):
        super(TimelineValidator,self).checkHeader(line)
        if self.fix:
            self.writeHeader(self.cols)

    def checkLine(self,line):
        data = super(TimelineValidator,self).checkLine(line)
        # TODO check the values
        if self.fix:
            self.writeNewLine(data)

    class Factory(object):
        def create(self,filename,hugo_entrez_map,fix,logger,stableId):
            return TimelineValidator(filename,hugo_entrez_map,fix,logger,stableId)

# ------------------------------------------------------------------------------
# Functions

def processMetafile(filename):
    """Process a metafile. returns a dictionary of values in the file."""
    metafile = open(filename,'rU')
    metaDictionary = {}
    for line in metafile:
        metaDictionary[line.split(':')[0]]=''.join(line.split(':')[1:])

    return metaDictionary


def checkSampleIds(sampleIdSets,clinical_validator):
    """Checks that all ids seen in other genomic files are also present in the clinical file."""
    # TODO - refactor to take a list of ids instead of each individually

    idsSeen = set()

    # construct set of all ids seen across files
    for idSet in sampleIdSets:
        for idseen in idSet:
            idsSeen.add(idseen)

    # check if these ids were found in the clinical data file
    for idseen in idsSeen:
        if idseen not in clinical_validator.sampleIds and idseen != '':
            clinical_validator.logger.error(
                'Missing a sample ID found in the study',
                extra={'cause': idseen})

def segMetaCheck(segvalidator,filenameCheck):
    """Checks meta file vs segment file on the name."""
    if filenameCheck != '':
        if not filenameCheck == segvalidator.filenameShort:
            segvalidator.logger.error(
                "Wrong .seg file name; '%s' specified in meta file",
                filenameCheck)
            exitcode = 1

def getFileFromFilepath(f):
    return os.path.basename(f.strip())

def processCaseListDirectory(caseListDir,sampleIdSets, logger):

    logger.info('Validating case lists')

    case_lists = [os.path.join(caseListDir, x) for x in os.listdir(caseListDir)]

    for case in case_lists:

        case_data = processMetafile(case)

        for cd in case_data:
            if cd.strip() not in CASE_LIST_FIELDS and cd.strip() != '':
                logger.warning(
                    'Unrecognized field found in case list file',
                    extra={'data_filename': getFileFromFilepath(case),
                           'cause': cd})
                exitcode = 0

        sampleIds = case_data.get('case_list_ids')
        if sampleIds is not None:
            sampleIds = set([x.strip() for x in sampleIds.split('\t')])
            sampleIdSets.append(sampleIds)

    logger.info('Validation of case lists complete')

def usage():
    """Displays program usage (invalid args)."""
    print >> sys.stderr, (
        'validateData.py'
        ' -v (verbose output)'
        ' -c (create corrected files)'
        ' --directory=[path to directory]'
        ' --hugo-entrez-map=[download or filename, optional]\n'
        'For output of warnings, use -v\n'
        'To generate corrected files, use -c'
        '\n##############################################\n'
        'Follow file naming conventions in the github wiki:\n'
        'https://github.com/cBioPortal/cbioportal/wiki/File-Formats')

# ------------------------------------------------------------------------------

def main():

    """Main function."""

    # get a logger to emit messages
    logger = logging.getLogger(__name__)
    logger.setLevel(logging.ERROR)

    # parse command line options
    try:
        opts, args = getopt.getopt(sys.argv[1:], 'vc', ['directory=','hugo-entrez-map='])
    except getopt.GetoptError, msg:
        print >> sys.stderr, msg
        usage()
        sys.exit(2)

    # process the options
    study_dir = ''
    hugo = ''
    fix = False

    hugo_entrez_map = {}


    for o, a in opts:
        if o == '--directory':
            study_dir = a
        elif o == '--hugo-entrez-map':
            hugo = a
        elif o == '-c':
            fix = True
        elif o == '-v':
            logger.setLevel("INFO")

    # handlers and formatters that output different formats could be set here
    text_handler = logging.StreamHandler(sys.stdout)
    text_handler.setFormatter(SimpleHtmlTableFormatter())
    collapsing_text_handler = CollapsingLogMessageHandler(
        capacity=3e6,
        flushLevel=logging.CRITICAL,
        target=text_handler)

    logger.addHandler(collapsing_text_handler)

    if study_dir == '' or fix == '':
        usage()
        sys.exit(2)

    # check existence of directory
    if not os.path.exists(study_dir):
        print >> sys.stderr, 'directory cannot be found: ' + study_dir
        sys.exit(2)

    if hugo == 'download' and hugoEntrezMapPresent:
        hugo_entrez_map = ftp_NCBI()
    elif hugo != '' and hugoEntrezMapPresent:
        try:
            ncbi_file = open(hugo,'r')
        except IOError:
            print >> sys.stderr, 'file cannot be found: ' + hugo
            sys.exit(2)

        hugo_entrez_map = parse_ncbi_file(ncbi_file)

    # Get all files in study_dir
    filenames = [os.path.join(study_dir, x) for x in os.listdir(study_dir)]
    cancerStudyId = ''
    filenameMetaStringCheck = ''
    filenameStringCheck = ''


    # Create validators based on filenames
    validators = []

    metafiles = []
    sampleIdSets = []

    stableids = {}

    clinvalidator = None

    for f in filenames:
        metafile = False

        # process case list directory if found
        if os.path.isdir(f) and getFileFromFilepath(f) == 'case_lists':
            processCaseListDirectory(f, sampleIdSets, logger)

        # metafile validation and information gathering. Simpler than the big files, so no classes.
        # just need to get some values out, and also verify that no extra fields are specified
        for pattern in META_PATTERNS:
            if pattern in f:
                meta = processMetafile(f)
                metafile = True

                for field in meta:
                    if field not in META_FIELD_MAP[pattern]:
                        logger.warning(
                            'Unrecognized field in meta file',
                            extra={'data_filename': getFileFromFilepath(f),
                                   'cause': field})
                        exitcode = 0

                # check that cancer study identifiers across files so far are consistent.
                if cancerStudyId == '':
                    cancerStudyId = meta['cancer_study_identifier'].strip()
                elif cancerStudyId != meta['cancer_study_identifier'].strip():
                    logger.error(
                        "Cancer study identifier is not consistent across "
                        "files, expected '%s'",
                        cancerStudyId.strip(),
                        extra={'data_filename': getFileFromFilepath(f),
                               'cause': meta['cancer_study_identifier'].strip()})
                    exitcode = 1

                stableid = meta.get('stable_id','corrected')

                # check filenames for seg meta file, and get correct filename for the actual
                if pattern == SEG_META_PATTERN: 
                    metafiles.append(SEG_META_PATTERN)
                    filenameMetaStringCheck = cancerStudyId + '_meta_cna_' + GENOMIC_BUILD_COUNTERPART + '_seg.txt'
                    filenameStringCheck = cancerStudyId + '_data_cna_' + GENOMIC_BUILD_COUNTERPART + '.seg'
                    if filenameMetaStringCheck != os.path.basename(f):
                        logger.error(
                            "Meta file for .seg file named incorrectly, expected '%s'",
                            filenameMetaStringCheck,
                            extra={'cause': f})
                        exitcode = 1

                    if (
                            meta.get('reference_genome_id').strip() !=
                            GENOMIC_BUILD_COUNTERPART.strip()):
                        logger.error(
                            'Reference_genome_id is not %s',
                            GENOMIC_BUILD_COUNTERPART,
                            extra={'data_filename': os.path.basename(f.strip()),
                                   'cause': meta.get('reference_genome_id').strip()})
                        exitcode = 1

                metafiles.append(pattern)

    for f in filenames:
        # TODO refactor this needlessly duplicated loop
        metafile = False
        for pattern in META_PATTERNS:
            if pattern in f:
                metafile = True

        # TODO determine data file type based on associated meta file;
        # that way, the only file pattern necessary is /\bmeta\b/. As it is
        # now, 'meta_RNA_Seq_v2_expression_median_normals.txt' is assumed to
        # be a data file because it doesn't match /meta_expression/.

        # create the validator objects
        for pattern in FILE_PATTERNS:
            if pattern in f and not metafile:
                stableid = stableids.get(VALIDATOR_META_MAP[VALIDATOR_IDS[pattern]],'corrected')
                validators.append(ValidatorFactory.createValidator(VALIDATOR_IDS[pattern],f,hugo_entrez_map,fix,logger,stableid))

    # validate all the files
    for validator in validators:
        validator.validate()
        sampleIdSets.append(validator.sampleIds)

        # check if metafile exists for given file type (except clinical) and that the stable ids match
        if VALIDATOR_META_MAP.get(type(validator).__name__) not in metafiles:
            logger.error('Missing metafile',
                         extra={'data_filename': validator.filenameShort})
            exitcode = 1

        # check meta and file names match for seg files
        if type(validator).__name__ == 'SegValidator':
            segMetaCheck(validator,filenameStringCheck)

        # get all the ids in the clinical validator for the check below
        if type(validator).__name__ == 'ClinicalValidator':
            if clinvalidator is not None:
                logger.error(
                    'Found multiple clinical data files',
                    extra={'data_filename':
                           (clinvalidator.filenameShort + ', ' +
                            validator.filenameShort)})
            clinvalidator = validator

    # make sure that lla samples seen across all files are present in the clinical file
    logger.info('Checking sample identifiers')
    if clinvalidator is not None:
        checkSampleIds(sampleIdSets,clinvalidator)
    else:
        logger.error('No clinical file detected')
        errorcode = 1

    logger.info('Validation complete')


# ------------------------------------------------------------------------------
# vamanos 

if __name__ == '__main__':
    main()
    # TODO base the return code on whether any error messages were emitted
    # and remove the dysfunctional exitcode and errorcode variables
    sys.exit(exitcode)
