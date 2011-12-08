#! /usr/bin/env python

# ------------------------------------------------------------------------------
# imports

import sys

# ------------------------------------------------------------------------------
# globals

OUTPUT_FILE = sys.stdout
WARNINGS_FILE_NAME = "./warnings-rae.txt"
WARNINGS_FILE = open(WARNINGS_FILE_NAME, 'w')

if len(sys.argv) < 2:
    sys.exit("usage: ./gen-rae.py <gene2accession-cooked file> <raw rae file>")
else:
    GENE_2_ACCESSION_COOKED_FILE = open(sys.argv[1], 'r')
    RAW_RAE_FILE = open( sys.argv[2], 'r')

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
# interate through the raw rae file:
# - remove all records that contain NA values,
# - remove duplicate isoforms 
# - replace refseq id with entrez gene id
# - insert "method"

line_count = 0
prev_symbol_list = []
num_warnings = 0
for line in RAW_RAE_FILE:
	# increment line count
	line_count = line_count + 1
	# first line is header
	if line_count == 1:
		line = line.replace("Isoform", "Method\tEntrez_Gene_ID")
		# process header
		header_parts = line.split("\t")
		parts_ct = 0
		for part in header_parts:
			parts_ct = parts_ct + 1
			if part == "Method" or part == "Entrez_Gene_ID":
				print >> OUTPUT_FILE, part + "\t",
			else:
				part = part.replace("WG_", "")
				if parts_ct < len(header_parts):
					print >> OUTPUT_FILE, part + "\t",
				else:
					print >> OUTPUT_FILE, part,
	else:
		parsed_line = line.split("\t")
		# first component is of the form: NM_001005484:OR4F5
		if (parsed_line[0].find(":") > -1):
			parsed_refseq_id = parsed_line[0].split(":");
			# compare current gene with previous gene, only process first isoform
			if parsed_refseq_id[1] in prev_symbol_list:
				continue
			else:
				# given refseq id, get entrez gene id
				if REFSEQ_TO_ACCESSION_MAP.has_key(parsed_refseq_id[0]):
					entrez_gene_id = REFSEQ_TO_ACCESSION_MAP[parsed_refseq_id[0]]
					# if we have an entrez gene id, replace "NM_001005484:OR4F5" with entrez gene id
					line = line.replace(parsed_line[0], entrez_gene_id)
					print >> OUTPUT_FILE, "RAE\t" + line,
				# if we dont have an entrez gene id, write out a warning
				else:
					print >> WARNINGS_FILE, "warning, cannot find entrez gene id for refseq id: %s" % parsed_refseq_id[0]
					num_warnings += 1
				# update previous gene with currentgene
				prev_symbol_list.append(parsed_refseq_id[1])
		#  This is probably a microRNA
		else:
			print >> OUTPUT_FILE, "RAE\t" + line,
print >> sys.stderr, "Total number of warnings:  %d.  Check file:  %s" % (num_warnings, WARNINGS_FILE_NAME)
