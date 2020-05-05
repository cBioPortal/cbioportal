import csv
import pandas as pd
import numpy as np
import os
import sys
import argparse
import shutil

PARENT_PARSER_NAME = 'survival data migration tool'
NULL_VALUES = ["[not applicable]", "[not available]", "[pending]", "[discrepancy]", "[completed]", "[null]", "", "na"]
ZERO_MAPPING_FILE_PATH = os.getcwd() + '/survivalStatusVocabulariesMappingToZero.txt'
ONE_MAPPING_FILE_PATH = os.getcwd() + '/survivalStatusVocabulariesMappingToOne.txt'


def migrateAttibuteValue(value, vocabulariesMappingToOne, vocabulariesMappingToZero):
    if value.lower() in NULL_VALUES:
        return value
    if value.lower() in vocabulariesMappingToOne:
        return "1:" + value
    elif value.lower() in vocabulariesMappingToZero:
        return "0:" + value
    else:
        sys.exit("cannot find the mapping for vocabulary {} , please provide the the mapping rules with option -a".format(value))
        return value

def splitAdditionalVocabularies(voc, oneVoc, zeroVoc):
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
                    if value.lower() not in zeroVoc:
                        zeroVoc.append(value.lower())
                        newVocAddingToZero.append(value.lower())
            elif mapId == '1':
                for value in values:
                    if value.lower() not in oneVoc:
                        oneVoc.append(value.lower())
                        newVocAddingToOne.append(value.lower())
        else:
            print("additionalVocabularies format is incorrect, please check the example by using -h option")
    if len(newVocAddingToZero) > 0:
        # get vocabularies
        with open(ZERO_MAPPING_FILE_PATH, 'a') as f:
            writer = csv.writer(f)
            for voc in newVocAddingToZero:
                writer.writerow(voc.split())
                print("vocabulary {} has been added to zero mapping".format(voc))
    if len(newVocAddingToOne) > 0:
        # get vocabularies
        with open(ONE_MAPPING_FILE_PATH, 'a') as f:
            writer = csv.writer(f)
            for voc in newVocAddingToOne:
                writer.writerow(voc.split())
                print("vocabulary {} has been added to one mapping".format(voc))

def migrate_file(args):
    clinicalFile = args.clinicalFile
    shouldOverrive = args.override
    newFile = os.path.dirname(args.clinicalFile) + "/new_data_clinical_patients.txt"
    tmpFile = os.path.dirname(args.clinicalFile) + "/tmp_data_clinical_patients.txt"

    # get vocabularies
    with open(ONE_MAPPING_FILE_PATH) as f:
        vocabulariesMappingToOne = f.read().splitlines()
    with open(ZERO_MAPPING_FILE_PATH) as f:
        vocabulariesMappingToZero = f.read().splitlines()

    if args.additionalVocabularies != None and args.additionalVocabularies != '':
        splitAdditionalVocabularies(args.additionalVocabularies, vocabulariesMappingToOne, vocabulariesMappingToZero)

    print("vocabularies will be mapping to one: ")
    print(vocabulariesMappingToOne)
    print("vocabularies will be mapping to zero: ")
    print(vocabulariesMappingToZero)

    # write files
    input = open(clinicalFile, 'rb')
    output = open(newFile, 'wb')
    tmp_output = open(tmpFile, 'wb')

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

    # find attributes candidates
    candidates = list(filter(lambda column: column.endswith('_STATUS'), df.columns))
    targetAttributes = list(filter(lambda candidate: (candidate[:candidate.index("_STATUS")] + '_MONTHS') in df.columns, candidates))

    for targetAttribute in targetAttributes:
        df[targetAttribute] = df.apply(lambda row: migrateAttibuteValue(row[targetAttribute], vocabulariesMappingToOne, vocabulariesMappingToZero), axis=1)


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
    # remove tmp file anyway
    os.remove(tmpFile)   

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

    if clinicalFile != None:
        check_dir(clinicalFile)
    if newFile != None:
        check_new_file_path(newFile)
    migrate_file(args)


if __name__ == '__main__':
    parsed_args = interface()
    main(parsed_args)
