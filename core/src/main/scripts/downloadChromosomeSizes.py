#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tue Aug 11 14:40:04 2020

@author: Sander Rodenburg, The Hyve
"""

from pandas import DataFrame, read_csv
import json
from sys import argv, exit

if len(argv) == 1:
    outfile = 'importer/chromosome_sizes.json'
elif len(argv) == 2:
    outfile = argv[1]
else:
    exit('Usage: downloadChromosomeSizes.py [output.json]')

build_sizes = {}
for build in ['hg19','hg38','mm10']:
    sizes = read_csv('http://hgdownload.cse.ucsc.edu/goldenPath/%s/bigZips/%s.chrom.sizes' % (build, build), 
                     sep='\t', header=None, index_col=False, names=['chromosome','size'])
    # extract chr[0-9] and XY
    sizes = sizes[sizes['chromosome'].str.match('chr([0-9]{1,2}|[XY])$')]
    
    # remove prefix
    sizes['chromosome'] = sizes['chromosome'].str.replace('chr','')
    
    # parse to json
    sizes = sizes.set_index('chromosome').to_json(orient='columns')
    
    build_sizes[build] = json.loads(sizes)['size']

with open(outfile,'w') as f:
    json.dump(build_sizes, f)