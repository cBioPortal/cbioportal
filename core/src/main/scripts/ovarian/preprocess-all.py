#! /usr/bin/env python

# ------------------------------------------------------------------------------
# imports

import os
import sys

def processMRNA (originalFileName):
	finalFileName = getFinalFileName (originalFileName)
	if not os.path.exists(originalFileName):
		sys.exit("error: cannot find file:  " + originalFileName)
	else:
		print "Preparing mRNA Data file:  %s --> %s " % (originalFileName, finalFileName)
		os.system(BIN_DIR + "preprocess-mrna.py " + originalFileName + " > "  + finalFileName)

def processCNA (originalFileName):
	rae_original_file = originalFileName;
	rae_final_file = getFinalFileName (rae_original_file)

	if not os.path.exists(rae_original_file):
		sys.exit("error: cannot find " + rae_original_file + ".")
	else:
		print "Preparing CNA RAE file:  %s --> %s " % (rae_original_file, rae_final_file)
		os.system(BIN_DIR + "preprocess-rae.py " + rae_original_file + " > " + rae_final_file)
		
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
PORTAL_DATA_HOME = "PORTAL_DATA_HOME";
cgds_home_found = 0
for key in os.environ.keys():
	if key == CGDS_HOME:
		cgds_home_found = 1
		CGDS_HOME = os.environ[key]
	if key == PORTAL_DATA_HOME:
		PORTAL_DATA_HOME = os.environ[key]
if not cgds_home_found:
	sys.exit("error: " + CGDS_HOME + " environment variable needs to be set")

# ------------------------------------------------------------------------------
# some globals/constants

BIN_DIR = CGDS_HOME + "/scripts/ovarian/"
DATA_DIR = PORTAL_DATA_HOME + "/ovarian/"
GENE2ACCESSION  = DATA_DIR + "gene2accession-cooked.txt"

# ------------------------------------------------------------------------------
# pre-process CNA file
#processCNA (DATA_DIR + "data_CNA.txt");
#processCNA (DATA_DIR + "data_CNA_GISTIC_all.txt");
#processCNA (DATA_DIR + "data_CNA_GISTIC_focal.txt");

# ------------------------------------------------------------------------------
# pre-process mrna expression files

#processMRNA (DATA_DIR + "data_mRNA_unified.txt")
#processMRNA (DATA_DIR + "data_mRNA_unified_outliers.txt")

#processMRNA (DATA_DIR + "data_mRNA_unified_ZbyNormals.txt")
#processMRNA (DATA_DIR + "data_mRNA_unified_ZbyTumors.txt")

#processMRNA (DATA_DIR + "data_mRNA_median.txt")
#processMRNA (DATA_DIR + "data_mRNA_median_outliers.txt")

#processMRNA (DATA_DIR + "data_mRNA_median_ZbyNormals.txt")
#processMRNA (DATA_DIR + "data_mRNA_median_ZbyTumors.txt")

# pre-process the necessary case lists required for the mutation data
# for this, we need cases_all_list.txt and cases_sequenced_with_normal.txt
convertCaseFile (DATA_DIR + "case_lists/cases_all.txt", DATA_DIR + "processed_all_cases.txt")
convertCaseFile (DATA_DIR + "case_lists/cases_sequenced.txt", DATA_DIR + "processed_sequenced_cases.txt")
