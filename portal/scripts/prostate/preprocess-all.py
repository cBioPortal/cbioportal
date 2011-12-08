#! /usr/bin/env python

# ------------------------------------------------------------------------------
# imports

import os
import sys

def processMRNA (originalFileName):
	finalFileName = getFinalFileName (originalFileName)
	if not os.path.exists(originalFileName):
		sys.exit("error: cannot find file:  " + originalFile)
	else:
		print "Preparing mRNA Data file:  %s --> %s " % (originalFileName, finalFileName)
		os.system(BIN_DIR + "preprocess-mrna.py " + GENE2ACCESSION + " " + originalFileName + " > "  + finalFileName)
		
def getFinalFileName (originalFileName):
	return originalFileName.replace("data_", "processed_")
	
def convertCaseFile (caseInFile, caseOutFile):
	print "Converting case file:  %s --> %s."  % (caseInFile, caseOutFile)
	in_file = open (caseInFile)
	out_file = open (caseOutFile, "w")
	for line in in_file:
		if line.startswith("case_list_ids:"):
			line = line.replace ("case_list_ids:", "")
			parts = line.split()
			for part in parts:
				print >> out_file, part.strip()

# ------------------------------------------------------------------------------
# check for cgds environment var
CGDS_HOME = "CGDS_HOME"
CGDS_DATA_HOME = "CGDS_DATA_HOME";
cgds_home_found = 0
for key in os.environ.keys():
	if key == CGDS_HOME:
		cgds_home_found = 1
		CGDS_HOME = os.environ[key]
	if key == CGDS_DATA_HOME:
		CGDS_DATA_HOME = os.environ[key]
if not cgds_home_found:
	sys.exit("error: " + CGDS_HOME + " environment variable needs to be set")

# ------------------------------------------------------------------------------
# some globals/constants

BIN_DIR = CGDS_HOME + "/scripts/prostate/"
DATA_DIR = CGDS_DATA_HOME + "/prostate/"
GENE2ACCESSION  = DATA_DIR + "gene2accession-cooked.txt"

# ------------------------------------------------------------------------------
# pre-process RAE CNA file

rae_original_file = DATA_DIR + "data_CNA.txt"
rae_final_file = getFinalFileName (rae_original_file)

if not os.path.exists(rae_original_file):
	sys.exit("error: cannot find " + rae_original_file + ".")
else:
	print "Preparing CNA RAE file:  %s --> %s " % (rae_original_file, rae_final_file)
	os.system(BIN_DIR + "preprocess-rae.py " + GENE2ACCESSION + " " + rae_original_file + " > " + rae_final_file)

# pre-process the necessary case lists required for the mutation data
# for this, we need cases_all_list.txt and cases_sequenced_with_normal.txt
#convertCaseFile (DATA_DIR + "case_lists/cases_all_list.txt", DATA_DIR + "processed_all_cases.txt")
#convertCaseFile (DATA_DIR + "case_lists/cases_sequenced_rd1.txt", DATA_DIR + "processed_cases_sequenced_rd1.txt")
#convertCaseFile (DATA_DIR + "case_lists/cases_sequenced_rd2.txt", DATA_DIR + "processed_cases_sequenced_rd2.txt")

