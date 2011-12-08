#! /usr/bin/env python

# ------------------------------------------------------------------------------
# imports

import sys

# ------------------------------------------------------------------------------
# globals

OUTPUT_FILE = sys.stdout
WARNINGS_FILE_NAME = "./warnings-mrna.txt"
WARNINGS_FILE = open(WARNINGS_FILE_NAME, 'w')

if len(sys.argv) < 2:
    sys.exit("usage: ./gen-mrna.py <gene2accession-cooked file> <raw exon file>")
else:
    GENE_2_ACCESSION_COOKED_FILE = open(sys.argv[1], 'r')
    RAW_EXON_FILE = open(sys.argv[2], 'r')

# ------------------------------------------------------------------------------
# create map of refseq (found in rae) to entrez gene id (used in cgds)

REFSEQ_TO_ACCESSION_MAP = {}

for line in GENE_2_ACCESSION_COOKED_FILE:
	# per our input spec, record is tab delimited, column one entrez gene id, col 2 refseq id
	parsedNCBIRecord = line.split("\t");
	# chop off version suffix
	parsedRefSeqID = parsedNCBIRecord[1].split(".");
	if len(parsedRefSeqID[0]) == 0:
		refseqID = parsedNCBIRecord[1]
	else:
		refseqID = parsedRefSeqID[0]
	# add to map
	REFSEQ_TO_ACCESSION_MAP[refseqID] = parsedNCBIRecord[0]

# manual add a few
REFSEQ_TO_ACCESSION_MAP["NM_001114103"] = "54991"
REFSEQ_TO_ACCESSION_MAP["NM_199358"] = "286380"

# ------------------------------------------------------------------------------
# interate through the raw mrna file:
# remove "Gene" from col header
# - parse column one, replace chr1:357510-358460|NM_001005277|OR4F16 with gene symbol OR4F16

line_count = 0
num_warnings = 0
for line in RAW_EXON_FILE:
	# increment line count
	line_count = line_count + 1
	# first line is header
	if line_count == 1:
		line = line.replace("Gene", "")
		print >> OUTPUT_FILE, line,
	else:
		parsed_line = line.split("\t")
		# first component is of the form: chr1:357510-358460|NM_001005277|OR4F16
		# or of the form:  chr17:7059158-7069672|+|NM_000018|ACADVL
		components = parsed_line[0].split("|")
		if len(components) == 3:
			refseq_id = components[1]
		elif len(components) == 4:
			refseq_id = components[2]
		else:
			num_warnings += 1
			print >> WARNINGS_FILE, "warning, cannot extract refseq id from following string: %s" % parsed_line[0]
			continue
		
		if REFSEQ_TO_ACCESSION_MAP.has_key(refseq_id):
			entrez_gene_id = REFSEQ_TO_ACCESSION_MAP[refseq_id]
			line = line.replace(parsed_line[0], entrez_gene_id)
			print >> OUTPUT_FILE, line,
		else:
			print >> WARNINGS_FILE, "warning, cannot find entrez gene id for refseq id: %s" % refseq_id
			num_warnings += 1
print >> sys.stderr, "Total number of warnings:  %d.  Check file:  %s" % (num_warnings, WARNINGS_FILE_NAME)
