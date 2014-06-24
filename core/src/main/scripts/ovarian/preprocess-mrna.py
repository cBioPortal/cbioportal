#! /usr/bin/env python
import sys

OUTPUT_FILE = sys.stdout

if len(sys.argv) < 1:
    sys.exit("usage: ./preprocess-rae.py <raw rae file>")

RAW_RAE_FILE = open(sys.argv[1], 'r')
line_count = 0

#  Read in Raw RAE File
for line in RAW_RAE_FILE:
	line = line.strip()
	line_count = line_count + 1
	# first line is header
	if line_count == 1:
		print line
	else:
		parts = line.split("\t")
		# ids look like this:  A4GALT|53947
		# we only want the last part, which is the entrez gene id
		ids = parts[0].split("|");
		entrez_gene_id = ids[1]
		print "%s\t" % (entrez_gene_id),
		the_rest = parts[1:]
		for col in the_rest: 
			print (col + "\t"),
		print
