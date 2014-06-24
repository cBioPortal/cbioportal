#! /usr/bin/env python

import sys

file = open (sys.argv[1])
for line in file:
	line = line.strip()
	parts = line.split("\t")
	if len(parts) == 3:
		mutation = parts[2]
		if (mutation != "0"):
			print line
