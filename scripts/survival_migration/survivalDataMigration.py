import csv
import pandas as pd
import numpy as np
import os
import sys
import argparse
import shutil
import atexit
import ntpath

# constant values
PARENT_PARSER_NAME = 'survival data migration tool'
NULL_VALUES = ["[not applicable]", "[not available]", "[pending]", "[discrepancy]", "[completed]", "[null]", "", "na"]
MAPPING_TO_ONE_COLUMN = "MAPPING_TO_ONE"
MAPPING_TO_ZERO_COLUMN = "MAPPING_TO_ZERO"
# program values
tmpFile = None
newFile = None
isMigrationCompleted = False

@atexit.register
def cleanUp():
    if not isMigrationCompleted and (tmpFile != None or newFile != None):
        print("Cleanup:")
        if tmpFile != None and os.path.exists(tmpFile):
            os.remove(tmpFile)
            print("temporary file removed.")
        if newFile != None and os.path.exists(newFile):
            os.remove(newFile)
            print("new file removed.")

def validateAttibuteValue(value, vocabulariesMappingToOne, vocabulariesMappingToZero, noMappingVocabularies):
    if value.strip().lower() in NULL_VALUES or value.strip().lower() in vocabulariesMappingToOne or value.strip().lower() in vocabulariesMappingToZero:
        return value
    else:
        if value not in noMappingVocabularies:
            noMappingVocabularies.append(value)

def migrateAttibuteValue(value, vocabulariesMappingToOne, vocabulariesMappingToZero):
    if value.strip().lower() in NULL_VALUES:
        return value
    if value.strip().lower() in vocabulariesMappingToOne:
        return "1:" + value
    elif value.strip().lower() in vocabulariesMappingToZero:
        return "0:" + value
    else:
        sys.exit("cannot find the mapping for vocabulary {} , please provide the the mapping rules with option -a".format(value))

def generateVocabularies(vocabulariesMappingToOne, vocabulariesMappingToZero, vocabulariesFile):
    df = pd.read_csv(vocabulariesFile, sep='\t')
    df = df.replace(np.nan, '', regex=True)
    oneValues = df[MAPPING_TO_ONE_COLUMN].tolist()
    for unparsedValue in oneValues:
        if unparsedValue != None and unparsedValue != '':
            for parsedValue in unparsedValue.split(","):
                vocabulariesMappingToOne.append(parsedValue)
    zeroValues = df[MAPPING_TO_ZERO_COLUMN].tolist()
    for unparsedValue in zeroValues:
        if unparsedValue != None and unparsedValue != '':
            for parsedValue in unparsedValue.split(","):
                vocabulariesMappingToZero.append(parsedValue)

def splitAdditionalVocabularies(voc, oneVoc, zeroVoc, vocabulariesFile):
    zeroAndOneSplit = voc.split('#')
    newVocAddingToZero = []
    newVocAddingToOne = []
    # just defined one or zero
    for splitString in zeroAndOneSplit:
        mapAndValuesSplit = splitString.split(':')
        if (len(mapAndValuesSplit) == 2):
            mapId = mapAndValuesSplit[0]
            values = mapAndValuesSplit[1].split(',')
            if mapId == '0':
                for value in values:
                    if value.strip().lower() not in zeroVoc:
                        zeroVoc.append(value.strip().lower())
                        newVocAddingToZero.append(value.strip().lower())
            elif mapId == '1':
                for value in values:
                    if value.strip().lower() not in oneVoc:
                        oneVoc.append(value.strip().lower())
                        newVocAddingToOne.append(value.strip().lower())
        else:
            print("additionalVocabularies format is incorrect, please check the example by using -h option")

    # save vocabularies to file
    separator = ','
    oneUpdateString = separator.join(newVocAddingToOne)
    zeroUpdateString = separator.join(newVocAddingToZero)
    df = pd.read_csv(vocabulariesFile, sep='\t')

    if len(newVocAddingToZero) > 0 or len(newVocAddingToOne) > 0:
        # get vocabularies
        df2 = pd.DataFrame([[oneUpdateString, zeroUpdateString]], columns=[MAPPING_TO_ONE_COLUMN, MAPPING_TO_ZERO_COLUMN])
        df = df.append(df2, ignore_index=True)
        if len(newVocAddingToOne) > 0:
            print("vocabulary {} has been added to one mapping".format(oneUpdateString))
        if len(newVocAddingToZero) > 0:
            print("vocabulary {} has been added to zero mapping".format(zeroUpdateString))

    df.to_csv(vocabulariesFile, index=False, sep='\t', header=True)

def migrate_file(args):
    global newFile
    global tmpFile
    global isMigrationCompleted

    clinicalFile = args.clinicalFile
    vocabulariesFile = args.vocabulariesFile
    shouldOverrive = args.override
    newFile = os.path.dirname(clinicalFile) + "/migrated_" + ntpath.basename(clinicalFile)
    tmpFile = os.path.dirname(clinicalFile) + "/tmp_" + ntpath.basename(clinicalFile)

    # get vocabularies
    vocabulariesMappingToOne = []
    vocabulariesMappingToZero = []
    generateVocabularies(vocabulariesMappingToOne, vocabulariesMappingToZero, vocabulariesFile)

    if args.additionalVocabularies != None and args.additionalVocabularies != '':
        splitAdditionalVocabularies(args.additionalVocabularies, vocabulariesMappingToOne, vocabulariesMappingToZero, vocabulariesFile)

    print("vocabularies will be mapping to one: ")
    print(vocabulariesMappingToOne)
    print("vocabularies will be mapping to zero: ")
    print(vocabulariesMappingToZero)

    # write files
    input = open(clinicalFile, 'rt')
    output = open(newFile, 'wt')
    tmp_output = open(tmpFile, 'wt')

    writer = csv.writer(output)
    tmpwritter = csv.writer(tmp_output)
    for row in csv.reader(input):
        if row[0].startswith('#'):
            writer.writerow(row)
        else:
            tmpwritter.writerow(row)
    input.close()
    output.close()
    tmp_output.close()

    # read file and replace nan
    df = pd.read_csv(tmpFile, sep='\t')
    df = df.replace(np.nan, '', regex=True)

    # remove tmp file
    os.remove(tmpFile)   

    # find attributes candidates
    candidates = list(filter(lambda column: column.endswith('_STATUS'), df.columns))
    targetAttributes = list(filter(lambda candidate: (candidate[:candidate.index("_STATUS")] + '_MONTHS') in df.columns, candidates))

    # check if all values are mappable
    noMappingVocabularies = []
    for targetAttribute in targetAttributes:
        df.apply(lambda row: validateAttibuteValue(row[targetAttribute], vocabulariesMappingToOne, vocabulariesMappingToZero, noMappingVocabularies), axis=1)
    # throw warning and exit if vocabularies cannot be found
    if len(noMappingVocabularies) > 0:
        print("The following vocabularies cannot find mapping, please provide the the mapping rules with option -a")
        for vocabulary in noMappingVocabularies:
            print("Vocabulary cannot find mapping: " + vocabulary)
        exit(2)

    # migrate attribute value
    for targetAttribute in targetAttributes:
        df[targetAttribute] = df.apply(lambda row: migrateAttibuteValue(row[targetAttribute], vocabulariesMappingToOne, vocabulariesMappingToZero), axis=1)

    # write migrated result
    if len(targetAttributes) > 0:
        df.to_csv(newFile, mode='a', index=False, sep='\t', header=True)
        # copy and remove files, print messages
        if shouldOverrive == True:
            shutil.copy(newFile, clinicalFile)
            os.remove(newFile)
            print("File is overridden at {}".format(clinicalFile))
        elif args.newFile != None:
            shutil.copy(newFile, args.newFile)
            os.remove(newFile)
            print("New file is generated at {}".format(args.newFile))
        else:
            print("New file is generated at {}".format(newFile))
        # set isMigrationCompleted to true
        isMigrationCompleted = True
    else:
        print("No survival status attributes exist in this file. No migration proceed.")

def check_dir(file):
    # check existence of directory
    if not os.path.exists(file) and file != '':
        print('file cannot be found: ' + file)
        sys.exit(2)

def check_new_file_path(newFile):
    if not os.access(os.path.dirname(newFile), os.W_OK):
        print('new file path is not valid: ' + newFile)
        sys.exit(2)

def str2bool(v):
    if isinstance(v, bool):
       return v
    if v.lower() in ('yes', 'true', 't', 'y', '1'):
        return True
    elif v.lower() in ('no', 'false', 'f', 'n', '0'):
        return False
    else:
        raise argparse.ArgumentTypeError('Boolean value expected.')

def interface():
    parser = argparse.ArgumentParser(description=PARENT_PARSER_NAME)

    parser.add_argument('-f', '--clinicalFile', type=str, required=True, 
                        help='absolute path to the file that need to be replaced')
    parser.add_argument('-v', '--vocabulariesFile', type=str, required=True, 
                        help='absolute path to the custom vocabularies file to map the value')
    parser.add_argument('-o', '--override', type=str2bool, required=False, default=False,
                        help='override the old file or not')
    parser.add_argument('-n', '--newFile', type=str, required=False, 
                        help='absolute path to save the new migrated file')
    parser.add_argument('-a', '--additionalVocabularies', type=str, required=False, 
                        help='additional vocabularies to map the value, example format: 1:mapToOne_1,mapToOne2#0:mapToZero_1,mapToZero2')
    parser = parser.parse_args()

    return parser


def main(args):
    clinicalFile = args.clinicalFile
    newFile = args.newFile
    vocabulariesFile = args.vocabulariesFile

    if clinicalFile != None:
        check_dir(clinicalFile)
    if newFile != None:
        check_new_file_path(newFile)
    if vocabulariesFile != None:
        check_dir(vocabulariesFile)
    migrate_file(args)

if __name__ == '__main__':
    parsed_args = interface()
    main(parsed_args)
