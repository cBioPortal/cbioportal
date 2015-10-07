#!/usr/bin/python
import os
import sys

os.system("pegjs "+sys.argv[1]+" oql-parser-tmp.js")

f = open("oql-parser-tmp.js","r")
nf = open("oql-parser.js","w")

f.readline()
nf.write("oql_parser = (function() {\n")

for line in f:
	nf.write(line)

os.system("rm oql-parser-tmp.js")
