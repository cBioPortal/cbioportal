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
import csv
import os
import string

import ftplib
import gzip
import StringIO
import inspect
import re


# ------------------------------------------------------------------------------
# globals

# some file/buffer descriptors
ERROR_BUFFER = StringIO.StringIO()
OUTPUT_BUFFER = StringIO.StringIO()

ERROR_FILE = sys.stderr
OUTPUT_FILE = sys.stdout

# allows script to run with or without the hugoEntrezMap module
hugoEntrezMapPresent = True
try:
    from hugoEntrezMap import *
except ImportError:
    print >> OUTPUT_BUFFER, 'Skipping hugoEntrezMap'
    hugoEntrezMapPresent = False

# Current NCBI build and build counterpart - used in one of the maf checks as well as .seq filename check
NCBI_BUILD_NUMBER = 37
GENOMIC_BUILD_COUNTERPART='hg19'


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

CNA_HEADERS = ['Hugo_symbol', 'Entrez_Gene_Id']
CNA_VALUES = ['-2','-1','0','1','2','','NA']

SEG_HEADERS = ['ID',
    'chrom',
    'loc.start',
    'loc.end',
    'num.mark',
    'seg.mean'
]

CLINICAL_HEADERS = ['SAMPLE_ID','PATIENT_ID']

LOG2_HEADERS = ['Hugo_Symbol','Entrez_Gene_Id']
EXPRESSION_HEADERS = ['Hugo_Symbol','Entrez_Gene_Id']
FUSION_HEADERS = ['Hugo_Symbol','Entrez_Gene_Id']
METHYLATION_HEADERS = ['Hugo_Symbol','Entrez_Gene_Id']
RPPA_HEADERS = ['Composite.Element.REF']
TIMELINE_HEADERS = [
    'PATIENT_ID',
    'START_DATE',
    'STOP_DATE',
    'EVENT_TYPE'
]

MUTATIONS_HEADERS_ORDER = ['Hugo_Symbol',
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
    'Sequencer',
    't_alt_count',
    't_ref_count',
    'n_alt_count',
    'n_ref_count'
]

# Used for mapping column names to the corresponding function that does a check on the value.
# This can be done for other filetypes besides maf - not currently implemented.
MUTATIONS_CHECK_FUNCTION_MAP = {
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
    'n_ref_count':'check_n_ref_count'
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

# Factory for creating validation objects of various types
class ValidatorFactory:
    factories = {}
    
    @staticmethod
    def addFactory(id,validatorFactory):
        ValidatorFactory.factories.put[id] = validatorFactory

    @staticmethod
    def createValidator(id,filename,hugo_entrez_map,fix,verbose,stableId):
        if not ValidatorFactory.factories.has_key(id):
            ValidatorFactory.factories[id] = eval(id + '.Factory()')
        return ValidatorFactory.factories[id].create(filename,hugo_entrez_map,fix,verbose,stableId)

# basic validator obect
class Validator(object):
    def __init__(self,filename,hugo_entrez_map,fix,verbose,stableId):
        self.filename = filename
        self.filenameShort = filename.split('/')[-1]
        self.file = open(filename, 'rU')
        self.lineCount = 0
        self.sampleIds = set()
        self.cols = []
        self.numCols = 0
        self.invalidNumCols = False
        self.hugo_entrez_map = hugo_entrez_map
        self.lineEndings = ''
        self.fileRead = self.file.read()
        self.file.seek(0,0)
        self.end = False
        self.fix = fix
        self.addEntrez = False
        self.studyId = ''
        self.headerWritten = False
        self.verbose = verbose
        self.blankColumns = set()
        self.stableId = stableId
        self.blankLines = 0
        self.badChars = [' ']

        if fix:
            self.correctedFilename = self.filename.split('/')[-1][:-4]+'_'+self.stableId+'.txt'
            self.correctedFile = open(self.correctedFilename,'w')

    # validate method - initiates validation of file
    def validate(self):
        print >> OUTPUT_BUFFER, 'Validating ' + self.filename.split('/')[-1]
        #lines = re.split("[\r\n]+",self.file.read())

        self.checkLineBreaks()
        self.checkQuotes()

        for line in self.file:
            if not line.startswith('#'):
                self.lineCount += 1
                if self.lineCount == 1:
                    self.checkHeader(line)
                elif not self.end:
                    self.checkLine(line)
            else:
                try:
                    self.processTopLine(line)
                except AttributeError:
                    continue

        self.checkBlankCells()
        self.checkBlankLines()

        self.file.close()
        if self.fix:
            self.correctedFile.close()
        

        
    def printComplete(self):
        print >> OUTPUT_BUFFER, 'Validation of ' + self.filename.split('/')[-1] + ' complete\n'

    # Header check function. Checks that header has the correct items, removes any quotes
    def checkHeader(self,line):
        self.cols = [x.strip().replace('"','').replace('\'','') for x in line.strip().split('\t')]
        self.numCols = len(self.cols)

        self.checkRepeatedColumns()

        self.checkBadChar()

        missing = []
        for x in self.headers:
            if not x in self.cols:
                missing.append(x)

        if len(missing) > 0:
            print >> OUTPUT_BUFFER, '\tWARNING: Missing columns'
            for m in missing:
                print >> OUTPUT_BUFFER,'\t\t' + m
            exitcode = 0

    # Checks lines after header, removing quotes
    def checkLine(self,line):
        data = [x.strip().replace('"','').replace('\'','') for x in line.split('\t')]

        if all(x == '' for x in data):
            self.blankLines += 1

        if data == self.cols or data[0:2] == self.cols[0:2]:
            print >> OUTPUT_BUFFER, '\tFATAL: Repeated header'
            exitcode = 1
        
        if len(data) != self.numCols and not self.invalidNumCols:
            print >> OUTPUT_BUFFER, '\tFATAL: Expected ' + str(self.numCols) + ' columns based on header. Found ' + str(len(data)) + '\n' + \
                '\t\tLine: ' + str(self.lineCount)
            self.invalidNumCols = True
            exitcode = 1

        for i,x in enumerate(data):
            if x == '':
                try:
                    self.blankColumns.add(self.cols[i])
                except IndexError:
                    self.blankColumns.add('Column ' + str(i))

        data = [self.fixCase(x) for x in data]

        return data

    def checkQuotes(self):
        if '"' in self.fileRead or '\'' in self.fileRead:
            print >> OUTPUT_BUFFER, '\tFATAL: detected quotation marks in file\n' + \
                '\t\tFile:\t' + self.filenameShort
            exitcode = 1

    # checks line breaks, reports to user
    def checkLineBreaks(self):
        print >> OUTPUT_BUFFER, '\tChecking line breaks'
        if "\r\n" in self.fileRead:
            self.lineEndings = "\r\n"
            exitcode = 1
            print >> OUTPUT_BUFFER, '\t\tDOS line breaks detected (\\r\\n)'
            if self.fix:
                print >> OUTPUT_BUFFER, '\t\tCorrected file will have Unix (\\n) line breaks'
            else:
                print >> OUTPUT_BUFFER, '\t\tPlease correct line breaks to Unix style (\\n) before importing to cBioPortal to avoid errors'
        elif "\r" in self.fileRead:
            self.lineEndings = "\r"
            exitcode = 1
            print >> OUTPUT_BUFFER, '\t\tMac line breaks detected (\\r)'
            if self.fix:
                print >> OUTPUT_BUFFER, '\t\tCorrected file will have Unix (\\n) line breaks'
            else:
                print >> OUTPUT_BUFFER, '\t\tPlease correct line breaks to Unix style (\\n) before importing to cBioPortal to avoid errors'
        elif "\n" in self.fileRead:
            self.lineEndings = "\n"
            print >> OUTPUT_BUFFER, '\t\tUnix line breaks detected (\\n) (Correct)'
        else:
            print >> ERROR_BUFFER, '\t\tInvalid or no line breaks.'
            exitcode = 1

    # if sample Ids are columns, extracts them and sets the sampleIds var
    def setSampleIdsFromColumns(self):
        for i,col in enumerate(self.cols):
            if i != 0 and 'hugo_symbol' not in col.lower() and 'entrez' not in col.lower() and col != '':
                self.sampleIds.add(col.strip())

    # checks if a value is an integer
    def checkInt(self,value):
        try:
            int(value)
            return True
        except ValueError:
            return False

    def writeNewLine(self,data):

        # replace blanks with 'NA'
        data = [x if x != '' else 'NA' for x in data]

        if self.addEntrez:
            data.insert(1,self.hugo_entrez_map.get(data[0],'NA'))
            self.correctedFile.write('\t'.join(data) + '\n')
        else:
            self.correctedFile.write('\t'.join(data) + '\n')

    def writeHeader(self,data):
        if self.addEntrez:
            data.insert(1,'Entrez_Gene_Id')
            self.correctedFile.write('\t'.join(data) + '\n')
        else:
            self.correctedFile.write('\t'.join(data) + '\n')

    def checkRepeatedColumns(self):
        seen = set()
        for col in self.cols:
            if col not in seen:
                seen.add(col)
            else:
                print >> OUTPUT_BUFFER,'\tFATAL: Repeated column header\n' + \
                    '\t\tColumn:\t' + col
                exitcode = 1

    def checkBlankCells(self):
        if len(self.blankColumns) > 0:
            print >> OUTPUT_BUFFER, '\tWARNING: Blank cells detected'
            exitcode = 0
            for blank in self.blankColumns:
                print >> OUTPUT_BUFFER, '\t\t' + blank

    def checkBlankLines(self):
        if self.blankLines > 0:
            print >> OUTPUT_BUFFER,'\tFATAL: ' + str(self.blankLines) + ' blank lines detected'
            exitcode = 1

    # Check for bad things in a header, such as spaces, etc.
    def checkBadChar(self):
        bad_cols = [colName for bc in self.badChars for colName in self.cols if bc in colName]
        if len(bad_cols) > 0:
            print >> OUTPUT_BUFFER, '\tFATAL: bad characters detected in header:'
            for bc in bad_cols:
                print >> OUTPUT_BUFFER, '\t\t' + bc
            exitcode = 1

    # Correct yes no to Yes and No. 
    def fixCase(self,x):
        if x.lower() == 'yes':
            return 'Yes'
        elif x.lower() == 'no':
            return 'No'
        elif x.lower() == 'male':
            return 'Male'
        elif x.lower() == 'female':
            return 'Female'
        else: return x

# sub-class CNA validator
class CNAValidator(Validator):
    def __init__(self,filename,hugo_entrez_map,fix,verbose,stableId):
        super(CNAValidator,self).__init__(filename,hugo_entrez_map,fix,verbose,stableId)
        self.headers = CNA_HEADERS
        self.entrez_present = True
        self.badHugos = []
        self.badValues = []

    def validate(self):
        super(CNAValidator,self).validate()

        self.printBadValues('Hugo symbol',self.badHugos)
        self.printBadValues('CNA value',self.badValues)

        self.printComplete()

    # header validation for CNA files
    def checkHeader(self,line):
        super(CNAValidator,self).checkHeader(line)
        
        if not self.headers[0] == self.cols[0]:
            print >> OUTPUT_BUFFER, "\tWARNING: Invalid Header:\t" + self.headers[0] + " should be in column 1"
            exitcode = 0
        if not self.headers[1] == self.cols[1]:
            print >> OUTPUT_BUFFER, "\tWARNING: Invalid Header:\t" + self.headers[1] + " should be in column 2"
            self.entrez_present = False
            self.addEntrez = True
            exitcode = 0

        self.setSampleIdsFromColumns()

        if self.fix:
            self.writeHeader(self.cols)

    # line validation for CNA files - checks that values are correct type
    def checkLine(self,line):
        data = super(CNAValidator,self).checkLine(line)

        for i,d in enumerate(data):
            if d.strip() not in CNA_VALUES and i != 0 and not (i == 1 and self.entrez_present):
                self.badValues.append((d,self.lineCount))
            elif i == 1 and self.entrez_present:
                if not self.checkInt(d.strip()) and not d.strip() == 'NA':
                    print >> OUTPUT_BUFFER, '\tWARNING: Invalid Data Type:\tColumn ' + str(i+1) + ' Line ' + str(self.lineCount) + 'Entrez_Gene_Id must be integer or NA'
                    exitcode = 0
            elif i == 0 and len(self.hugo_entrez_map) > 0:
                if not d in self.hugo_entrez_map and len(self.hugo_entrez_map) != {}:
                    self.badHugos.append((d,self.lineCount))

        if self.fix:
            self.writeNewLine(data)
            

    def printBadValues(self,name,bads):
        if len(bads) > 0:
            print >> OUTPUT_BUFFER, '\tWARNING: ' + name + ' appears incorrect ' + str(len(bads)) + ' time(s) on line(s):'
            exitcode = 0
            for bad in bads:
                print >> OUTPUT_BUFFER, '\t\t' + str(bad[1]) + '\t' + str(bad[0])

    class Factory:
        def create(self,filename,hugo_entrez_map,fix,verbose,stableId): return CNAValidator(filename,hugo_entrez_map,fix,verbose,stableId)

#sub-class mutations_extended validator
class MutationsExtendedValidator(Validator):
    def __init__(self,filename,hugo_entrez_map,fix,verbose,stableId):
        super(MutationsExtendedValidator,self).__init__(filename,hugo_entrez_map,fix,verbose,stableId)
        self.headers = MUTATIONS_HEADERS_ORDER
        self.sampleIdsHeader = set()
        self.mafValues = {}
        self.entrez_present = True
        self.extra_exists = False
        self.extra = ''
        self.hugo_warning_lines = []
        self.functionList = inspect.getmembers(MutationsExtendedValidator,predicate=inspect.ismethod)
        self.toplinecount = 0
        self.headerPresent = False

    def validate(self):
        super(MutationsExtendedValidator,self).validate()
        self.print_hugo_warnings()
        self.printComplete()


    def checkHeader(self,line):     
        super(MutationsExtendedValidator,self).checkHeader(line)  

        if self.cols[0:32] != self.headers[0:32]:
            print >> OUTPUT_BUFFER, '\tWARNING: Invalid Header:\tMust have following columns in specified order'
            exitcode = 0
            for h in self.headers[0:32]:
                print >> OUTPUT_BUFFER, '\t\t' + h

        if self.fix:
            self.writeHeader(self.cols)

    # Each value in each line is checked individually. From the column name (stored in self.cols), the corresponding 
    #   function to check the value is selected out of the MUTATIONS_CHECK_FUNCTION_MAP (index based).

    def checkLine(self,line):
        data = super(MutationsExtendedValidator,self).checkLine(line)
        
        data = data[0:len(self.headers)-1]
        for i,d in enumerate(data):
            for f in self.functionList:
                try:
                    if f[0] == MUTATIONS_CHECK_FUNCTION_MAP.get(self.cols[i]):
                        functionCall = f[1]
                        if not functionCall(self,d):
                            self.printDataInvalidStatement(d,i) 
                        self.mafValues[MUTATIONS_HEADERS_ORDER[i]] = d
                    else:
                        self.mafValues[self.cols[i]] = d
                except IndexError:
                    print >> OUTPUT_BUFFER, '\tFATAL: row ' + self.lineCount + ' has too many values'
                    exitcode = 1

        if self.fix:
            self.writeNewLine(data)

                    
    # processes the top line of, which contains sample ids used in study.
    def processTopLine(self,line):
        self.headerPresent = True
        topline = [x.strip() for x in line.split(' ') if '#' not in x]

        self.toplinecount += 1
        for sampleId in topline:
            self.sampleIdsHeader.add(sampleId)

        if self.fix:
            self.correctedFile.write(line)

    # prints out statement for invalid values detected
    def printDataInvalidStatement(self,value,col):
        print >> OUTPUT_BUFFER, '\tWARNING: Column ' + str(col + 1) +' ' + MUTATIONS_HEADERS_ORDER[col] + ' line ' + str(self.lineCount + self.toplinecount) + ' appears incorrect\n\tValue: ' + value
        exitcode = 1
        if self.extra_exists:
            print >> OUTPUT_BUFFER, '\t\t' + self.extra
            self.extra_exists = False

    def writeNewLine(self,data):
        newline = []
        for col in self.headers:
            newline.append(self.mafValues.get(col,'NA'))
        if not self.entrez_present:
            newline[1] = self.hugo_entrez_map.get(newline[0],'NA')

        newline = [x if x != '' else 'NA' for x in newline]
        self.correctedFile.write('\t'.join(newline) + '\n')

    def writeHeader(self,data):
        extraCols = [x for x in self.cols if x not in self.headers]
        for ec in extraCols:
            self.headers.append(ec)
        self.correctedFile.write('\t'.join(self.headers) + '\n')

    # checks if a value is a valid hugo - present in NCBI file. If no NCBI file given at runtime, does nothing
    def checkValidHugo(self,value):
        if not value in self.hugo_entrez_map and self.hugo_entrez_map != {}:
            self.hugo_warning_lines.append((value,self.lineCount))
        return True

    # checks if a value is a valid entrez id for the given hugo - needs to be present and match. 
    def checkValidEntrez(self, value):
        if self.entrez_present:
            if value == '':
                print >> OUTPUT_BUFFER, '\tWARNING: Missing entrez IDs'
                self.entrez_present = False
                self.addEntrez = True
                exitcode = 0
            elif self.entrez_present and not value in self.hugo_entrez_map.values() and self.hugo_entrez_map != {}:
                return False
            elif self.hugo_entrez_map.get(self.mafValues['Hugo_Symbol'])!= value and self.hugo_entrez_map != {}:
                print >> OUTPUT_BUFFER, '\tWARNING: Line ' + str(self.lineCount) + ' Entrez gene ID does not match Hugo symbol'
                exitcode = 0
        return True
    

    # These functions check values of the MAF according to their name. The mapping of which function checks which value is a global value
    #   at the top of the script. If any other checks need to be added for another field name, add the map in the global corresponding to
    #   the function name that is created to check it.

    def checkCenter(self, value):
        return True

    def checkChromosome(self, value):
        if self.checkInt(value):
            v = int(value)
            if int(v) >=1 and int(v) <=22:
                return True
            return False
        elif value.lower() == 'x' or value.lower() == 'y' or value == '':
            return True
        return False
    
    def checkStartPosition(self, value):
        return True
 
    def checkEndPosition(self, value):
        return True
   
    def checkTumorSampleBarcode(self, value):
        good = True
        if self.headerPresent and value not in self.sampleIdsHeader:
            self.extra = 'Value not in sample ids from header'
            self.extra_exists = True
            good = False
        self.sampleIds.add(value.strip())
        return good

    def checkNCBIbuild(self, value):
        if self.checkInt(value) and value != '':
            if int(value) != NCBI_BUILD_NUMBER:
                return False
        return True
    
    def checkStrand(self, value):
        if value != '+' and value != '-' and value != '':
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
                self.extra = 'Value not in sample ids from header'
                self.extra_exists =  True
                return False
        return True
    
    def checkMatchedNormSampleBarcodehNormSeqAllele(self, value):
        return True
    
    def checkTumorValidationAllele(self, value):
        return True
    
    def checkMatchNormValidationAllele(self, value):
        return True
    
    def checkVerificationStatus(self, value):
        if value != '' and value.lower() != 'verified' and value.lower() != 'unknown':
            return False
        return True
    
    def checkValidationStatus(self, value):
        if value == '':
            return True
        if value.lower() != 'valid' and value.lower() != 'unknown' and value.lower() != 'na' and value.lower() != 'untested':
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


    # serves to print compilations of errors after every line is checked.
    
    def print_hugo_warnings(self):
        if len(self.hugo_warning_lines) > 0:
            print >> OUTPUT_BUFFER, "\tWARNING: Hugo symbols appear incorrect " + str(len(self.hugo_warning_lines)) + ' time(s) on lines:'
            for hugo_warning in self.hugo_warning_lines:
                print >> OUTPUT_BUFFER, '\t\t' + str(hugo_warning[1] + self.toplinecount) + '\t' + str(hugo_warning[0])
            exitcode = 0

    class Factory:
        def create(self,filename,hugo_entrez_map,fix,verbose,stableId): return MutationsExtendedValidator(filename,hugo_entrez_map,fix,verbose,stableId)

# validator for clinical data files
class ClinicalValidator(Validator):
    def __init__(self,filename,hugo_entrez_map,fix,verbose,stableId):
        super(ClinicalValidator,self).__init__(filename,hugo_entrez_map,fix,verbose,stableId)
        self.headers = CLINICAL_HEADERS

    def validate(self):
        super(ClinicalValidator,self).validate()
        self.printComplete()

    def checkHeader(self,line):
        super(ClinicalValidator,self).checkHeader(line)

        missing = []
        for col in self.headers:
            if col not in self.cols:
                missing.append(col)

        if len(missing) > 0:
            print >> OUTPUT_BUFFER, '\tWARNING: header missing following columns:\n'
            exitcode = 0
            for m in missing:
                print >> OUTPUT_BUFFER, '\t\t' + m

        if self.cols[0:1] != self.headers[0:1]:
            print >> OUTPUT_BUFFER, '\tFATAL: clinical header should begin SAMPLE_ID PATIENT_ID'
            exitcode = 1

        notUpper = []
        for col in self.cols:
            if not col.isupper():
                notUpper.append(col)

        if len(notUpper) > 0:
            print >> OUTPUT_BUFFER, '\tWARNING: Headers found not all caps:'
            exitcode = 0
            for nu in notUpper:
                print >> OUTPUT_BUFFER, '\t\t' + nu

        if self.fix:
            self.writeHeader(self.cols)

        self.cols = map(str.lower,self.cols)
            


    def checkLine(self,line):
        data = super(ClinicalValidator,self).checkLine(line)
        for i,d in enumerate(data):
            try:
                if i == self.cols.index(self.headers[0].lower()):
                    self.sampleIds.add(d.strip())
            except ValueError:
                continue

        if self.fix:
            self.writeNewLine(data)

    def writeHeader(self,data):
        self.correctedFile.write('\t'.join(map(str.upper,data)) + '\n')


    class Factory:
        def create(self,filename,hugo_entrez_map,fix,verbose,stableId): return ClinicalValidator(filename,hugo_entrez_map,fix,verbose,stableId)

# validator for .seg files
class SegValidator(Validator):
    def __init__(self,filename,hugo_entrez_map,fix,verbose,stableId):
        super(SegValidator,self).__init__(filename,hugo_entrez_map,fix,verbose,stableId)
        self.headers = SEG_HEADERS
        self.sampleIds = set()

    def validate(self):
        super(SegValidator,self).validate()
        self.printComplete()

    def checkHeader(self,line):
        super(SegValidator,self).checkHeader(line)

        if self.cols != self.headers:
            print >> OUTPUT_BUFFER, '\tWARNING:Invalid Header:\tMust have following columns in specified order'
            exitcode = 0
            for h in self.headers:
                print >> OUTPUT_BUFFER, '\t\t' + h

        if self.fix:
            self.writeHeader(self.cols)

    def checkLine(self,line):
        data = super(SegValidator,self).checkLine(line)

        # add sampleIds to set for later checks
        for i,d in enumerate(data):
            if i == self.cols.index(self.headers[0]):
                self.sampleIds.add(d.strip())

        if self.fix:
            self.writeNewLine(data)


    class Factory:
        def create(self,filename,hugo_entrez_map,fix,verbose,stableId): return SegValidator(filename,hugo_entrez_map,fix,verbose,stableId)

class Log2Validator(Validator):
    def __init__(self,filename,hugo_entrez_map,fix,verbose,stableId):
        super(Log2Validator,self).__init__(filename,hugo_entrez_map,fix,verbose,stableId)
        self.headers = LOG2_HEADERS

    def validate(self):
        super(Log2Validator,self).validate()
        self.printComplete()

    def checkHeader(self,line):
        super(Log2Validator,self).checkHeader(line)
        self.setSampleIdsFromColumns()

        if self.fix:
            self.writeHeader(self.cols)

    def checkLine(self,line):
        data = super(Log2Validator,self).checkLine(line)

        if self.fix:
            self.writeNewLine(data)

    class Factory:
        def create(self,filename,hugo_entrez_map,fix,verbose,stableId): return Log2Validator(filename,hugo_entrez_map,fix,verbose,stableId)

class ExpressionValidator(Validator):
    def __init__(self,filename,hugo_entrez_map,fix,verbose,stableId):
        super(ExpressionValidator,self).__init__(filename,hugo_entrez_map,fix,verbose,stableId)
        self.headers = EXPRESSION_HEADERS

    def validate(self):
        super(ExpressionValidator,self).validate()
        self.printComplete()

    def checkHeader(self,line):
        super(ExpressionValidator,self).checkHeader(line)
        self.setSampleIdsFromColumns()

        if self.fix:
            self.writeHeader(self.cols)

    def checkLine(self,line):
        data = super(ExpressionValidator,self).checkLine(line)

        if self.fix:
            self.writeNewLine(data)

    class Factory:
        def create(self,filename,hugo_entrez_map,fix,verbose,stableId): return ExpressionValidator(filename,hugo_entrez_map,fix,verbose,stableId)

class FusionValidator(Validator):
    def __init__(self,filename,hugo_entrez_map,fix,verbose,stableId):
        super(FusionValidator,self).__init__(filename,hugo_entrez_map,fix,verbose,stableId)
        self.headers = FUSION_HEADERS

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

    class Factory:
        def create(self,filename,hugo_entrez_map,fix,verbose,stableId): return FusionValidator(filename,hugo_entrez_map,fix,verbose,stableId)

class MethylationValidator(Validator):
    def __init__(self,filename,hugo_entrez_map,fix,verbose,stableId):
        super(MethylationValidator,self).__init__(filename,hugo_entrez_map,fix,verbose,stableId)
        self.headers = METHYLATION_HEADERS

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

    class Factory:
        def create(self,filename,hugo_entrez_map,fix,verbose,stableId): return MethylationValidator(filename,hugo_entrez_map,fix,verbose,stableId)

class RPPAValidator(Validator):
    def __init__(self,filename,hugo_entrez_map,fix,verbose,stableId):
        super(RPPAValidator,self).__init__(filename,hugo_entrez_map,fix,verbose,stableId)
        self.headers = RPPA_HEADERS

    def validate(self):
        super(RPPAValidator,self).validate()
        self.printComplete()

    def checkHeader(self,line):
        super(RPPAValidator,self).checkHeader(line)

        # for rppa, first column should be hugo|antibody, everything after should be sampleIds
        self.sampleIds = [x.strip() for x in self.cols[1:] if self.cols[0] == RPPA_HEADERS[0]]

        if self.fix:
            self.writeHeader(self.cols)

    def checkLine(self,line):
        data = super(RPPAValidator,self).checkLine(line)

        if self.fix:
            self.writeNewLine(data)
    class Factory:
        def create(self,filename,hugo_entrez_map,fix,verbose,stableId): return RPPAValidator(filename,hugo_entrez_map,fix,verbose,stableId)

class TimelineValidator(Validator):
    def __init__(self,filename,hugo_entrez_map,fix,verbose,stableId):
        super(TimelineValidator,self).__init__(filename,hugo_entrez_map,fix,verbose,stableId)
        self.headers = TIMELINE_HEADERS

    def validate(self):
        super(TimelineValidator,self).validate()
        self.printComplete()

    def checkHeader(self,line):
        super(TimelineValidator,self).checkHeader(line)

        if self.fix:
            self.writeHeader(self.cols)

    def checkLine(self,line):
        data = super(TimelineValidator,self).checkLine(line)

        if self.fix:
            self.writeNewLine(data)

    class Factory:
        def create(self,filename,hugo_entrez_map,fix,verbose,stableId): return TimelineValidator(filename,hugo_entrez_map,fix,verbose,stableId)

# ------------------------------------------------------------------------------
# Functions

# process a metafile. returns a dictionary of values in the file
def processMetafile(filename):
    metafile = open(filename,'rU')
    metaDictionary = {}
    for line in metafile:
        metaDictionary[line.split(':')[0]]=''.join(line.split(':')[1:])

    return metaDictionary

# check that the names match up in a segment file

def checkSegFileMatch(meta,segvalidator):
    if filenameCheck == segvalidator.filenameShort:
        return True
    return False

# checks that all ids seen in other genomic files are also present in the clinical file
# TODO - refactor to take a list of ids instead of each individually

def checkSampleIds (sampleIdSets,clinIds,cname):
    badIds = []

    idsSeen = set()

    # construct set of all ids seen across files
    for idSet in sampleIdSets:
        for idseen in idSet:
            idsSeen.add(idseen)

    # check if these ids were found in the clinical data file
    for idseen in idsSeen:
        if idseen not in clinIds and idseen != '':
            badIds.append(idseen)

    if len(badIds) > 0:
        printBadIds(cname,badIds)

# helper function for checkSampleids - just prints out the IDs it finds
def printBadIds(cname,badIds):
    print >> OUTPUT_BUFFER, 'StudyIds missing in  ' + cname + '\n' + \
        'Missing IDs:'
    for bid in badIds:
        print >> OUTPUT_BUFFER, '\t' + str(bid)
    exitcode = 1

# checks meta fle vs segment file on the name

def segMetaCheck(segvalidator,filenameCheck):
    if filenameCheck != '':
        if not filenameCheck == segvalidator.filenameShort:
            print >> OUTPUT_BUFFER, 'Seg file name appears invalid \n' + \
                '\tMeta filename:\t' + filenameCheck + '\n' + \
                '\tSeg filename:\t' + segvalidator.filenameShort
            exitcode = 1

def getFileFromFilepath(f):
    return f.split('/')[-1].strip()

def processCaseListDirectory(caseListDir,sampleIdSets):
    print >> OUTPUT_BUFFER, 'Validating Case_Lists'
    case_lists = [caseListDir + '/' + x for x in os.listdir(caseListDir)]

    for case in case_lists:
        case_data = processMetafile(case)

        for cd in case_data:
            if cd.strip() not in CASE_LIST_FIELDS and cd.strip() != '':
                print >> OUTPUT_BUFFER, 'WARNING: Unexpected field found in case list file\n' + \
                    '\tFile:\t' + getFileFromFilepath(case) + '\n' + \
                    '\tField:\t' + cd
                exitcode = 0

        sampleIds = case_data.get('case_list_ids')
        if sampleIds is not None:
            sampleIds = set([x.strip() for x in sampleIds.split('\t')])
            sampleIdSets.append(sampleIds)
    print >> OUTPUT_BUFFER, 'Validation of Case_Lists complete\n'

# displays program usage (invalid args)

def usage():
    print >> OUTPUT_BUFFER, 'validateData.py -v (verbose output) -c (create corrected files) --directory=[path to directory] --hugo-entrez-map=[download or filename, optional]\n' + \
        'For output of warnings, use -v\n' + \
        'To generate corrected files, use -c' + \
        '\n##############################################\n' + \
        'Follow file naming conventions in the github wiki:\n' + \
        'https://github.com/cBioPortal/cbioportal/wiki/File-Formats'

# ------------------------------------------------------------------------------
# main function

def main():
    # parse command line options
    try:
        opts, args = getopt.getopt(sys.argv[1:], 'vc', ['directory=','hugo-entrez-map='])
    except getopt.error, msg:
        print >> ERROR_BUFFER, msg
        usage()
        print >> ERROR_FILE, ERROR_BUFFER.getvalue()
        print >> OUTPUT_FILE, OUTPUT_BUFFER.getvalue()
        sys.exit(2)

    # process the options (fp - filepath)
    fp = ''
    hugo = ''
    fix = False

    final_output = 0

    hugo_entrez_map = {}
    verbose = False


    for o, a in opts:
        if o == '--directory':
            fp = a
        elif o == '--hugo-entrez-map':
            hugo = a
        elif o in  ('-c'):
            fix = True
        elif o == '-v':
            verbose = True

    if fp == '' or fix == '':
        usage()
        print >> ERROR_FILE, ERROR_BUFFER.getvalue()
        print >> OUTPUT_FILE, OUTPUT_BUFFER.getvalue()
        sys.exit(2)

    # check existence of directory
    if not os.path.exists(fp):
        print >> ERROR_BUFFER, 'directory cannot be found: ' + fp
        print >> ERROR_FILE, ERROR_BUFFER.getvalue()
        print >> OUTPUT_FILE, OUTPUT_BUFFER.getvalue()
        sys.exit(2)

    if hugo == 'download' and hugoEntrezMapPresent:
        hugo_entrez_map = ftp_NCBI()
    elif hugo != '' and hugoEntrezMapPresent:
        try:
            ncbi_file = open(hugo,'r')
        except IOError:
            print >> ERROR_BUFFER, 'file cannot be found: ' + hugo
            print >> ERROR_FILE, ERROR_BUFFER.getvalue()
            print >> OUTPUT_FILE, OUTPUT_BUFFER.getvalue()
            sys.exit(2)

        hugo_entrez_map = parse_ncbi_file(ncbi_file)

    # Get all files in fp (filepath)
    filenames = [fp + '/' + x for x in os.listdir(fp)]
    cancerStudyId = ''
    filenameMetaStringCheck = ''
    filenameStringCheck = ''
    check = True
    seg_data_filename = ''


    # Create validators based on filenames
    validators = []

    metafiles = []
    sampleIdSets = []
    clinIds = set()

    stableids = {}

    clinvalidatorname = ''

    for f in filenames:
        metafile = False

        # process case list directory if found
        if os.path.isdir(f) and getFileFromFilepath(f) == 'case_lists':
            processCaseListDirectory(f,sampleIdSets)

        # metafile validation and information gathering. Simpler than the big files, so no classes.
        # just need to get some values out, and also verify that no extra fields are specified
        for pattern in META_PATTERNS:
            if pattern in f:
                meta = processMetafile(f)
                metafile = True

                for field in meta:
                    if field not in META_FIELD_MAP[pattern]:
                        print >> OUTPUT_BUFFER, 'WARNING: Field in metafile ' + getFileFromFilepath(f) + ' not present in schema\n' + \
                            '\tField: ' + field
                        exitcode = 0

                # check that cancer study identifiers across files so far are consistent.
                if cancerStudyId == '':
                    cancerStudyId = meta['cancer_study_identifier'].strip()
                elif cancerStudyId != meta['cancer_study_identifier'].strip():
                    print >> OUTPUT_BUFFER, 'FATAL: Cancer study identifier is not consistent across files.\n\t' + \
                        cancerStudyId.strip() + '\t\n\t' + meta['cancer_study_identifier'].strip() 
                    exitcode = 1
                
                stableid = meta.get('stable_id','corrected')

                # check filenames for seg meta file, and get correct filename for the actual
                if pattern == SEG_META_PATTERN: 
                    metafiles.append(SEG_META_PATTERN)
                    filenameMetaStringCheck = cancerStudyId + '_meta_cna_' + GENOMIC_BUILD_COUNTERPART + '_seg.txt'
                    filenameStringCheck = cancerStudyId + '_data_cna_' + GENOMIC_BUILD_COUNTERPART + '.seg'
                    if filenameMetaStringCheck != f.split('/')[-1]:
                        print >> OUTPUT_BUFFER, 'Meta file for .seg named incorrectly.\n' + \
                            '\tExpected:\t' + filenameMetaStringCheck + '\n' + \
                            '\tFound:\t' + f
                        exitcode = 1

                    seg_data_filename = meta['data_filename']
                    if meta.get('reference_genome_id').strip() != GENOMIC_BUILD_COUNTERPART.strip():
                        print >> OUTPUT_BUFFER, 'FATAL: reference_genome_id in ' + f.split('/')[-1].strip() + \
                            ' incorrect\n\t\tExpected:\t' + GENOMIC_BUILD_COUNTERPART + \
                            '\n\t\tFound:\t' + meta.get('reference_genome_id').strip()
                        exitcode = 1

                metafiles.append(pattern)

    for f in filenames:
        metafile = False
        for pattern in META_PATTERNS:
            if pattern in f:
                metafile = True

        # create the validator objects
        for pattern in FILE_PATTERNS:
            if pattern in f and not metafile:
                stableid = stableids.get(VALIDATOR_META_MAP[VALIDATOR_IDS[pattern]],'corrected')
                validators.append(ValidatorFactory.createValidator(VALIDATOR_IDS[pattern],f,hugo_entrez_map,fix,verbose,stableid))

    # validate all the files
    for validator in validators:
        validator.validate()
        sampleIdSets.append(validator.sampleIds)

        # check if metafile exists for given file type (except clinical) and that the stable ids match
        if VALIDATOR_META_MAP.get(type(validator).__name__) not in metafiles:
            print >> OUTPUT_BUFFER, 'FATAL: missing metafile for ' + validator.filenameShort + '\n'
            exitcode = 1
        
        # check meta and file names match for seg files
        if type(validator).__name__ == 'SegValidator':
            segMetaCheck(validator,filenameStringCheck)
            segvalidatorname = validator.filenameShort

        # get all the ids in the clinical validator for the check below
        if type(validator).__name__ == 'ClinicalValidator':
            clinIds = validator.sampleIds
            clinvalidatorname = validator.filenameShort

    # make sure that lla samples seen across all files are present in the clinical file
    print >> OUTPUT_BUFFER, '\nDoing sampleID checks'
    if clinvalidatorname != '':
        checkSampleIds(sampleIdSets,clinIds,clinvalidatorname)
    else:
        print >> OUTPUT_BUFFER, '\tWARNING: No clinical file detected'
        errorcode = 0

    print >> OUTPUT_BUFFER, '\nValidation complete'

    if verbose:
        print >> OUTPUT_FILE, OUTPUT_BUFFER.getvalue()
        print >> ERROR_FILE, ERROR_BUFFER.getvalue()


	
# ------------------------------------------------------------------------------
# vamanos 

if __name__ == '__main__':
    main()
    sys.exit(exitcode)
