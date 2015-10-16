#! /usr/bin/env python

# ------------------------------------------------------------------------------
# Script which maps hugo to entrez ids
# ------------------------------------------------------------------------------
# imports
import sys
import getopt
import csv
import os
import string

import ftplib
import gzip
import StringIO




# ------------------------------------------------------------------------------
# globals


FTP_USER = 'anonymous'
FTP_PASS = 'cbioportalValidator@'
NCBI_SERVER = 'ftp.ncbi.nih.gov'
NCBI_FILE = 'gene/DATA/GENE_INFO/Mammalia/Homo_sapiens.gene_info.gz'

# some file descriptors
ERROR_FILE = sys.stderr
OUTPUT_FILE = sys.stdout

# global for callback function use
NCBI_DATA = []

# ------------------------------------------------------------------------------
# functions

# establishes connection to ncbi ftp server and downloads the gene_info file for entrez gene id mapping
def ftp_NCBI():
    ftp = ftplib.FTP(NCBI_SERVER)
    ftp.login(FTP_USER,FTP_PASS)

    ftp.retrbinary('RETR ' + NCBI_FILE, callback=handle_binary)

    zippedData = ''.join(NCBI_DATA)
    ncbi_zipped_file = StringIO.StringIO(zippedData)
    
    return parse_ncbi_file(ncbi_zipped_file)

# ------------------------------------------------------------------------------
# helper function for downloading binary from ncbi
def handle_binary(data):
    NCBI_DATA.append(data)

# ------------------------------------------------------------------------------
# parses and creates dictionary mapping of hugo - entrez gene ids
def parse_ncbi_file(ncbi_zipped_file):
    hugo_entrez_map = {}

    unzipping = gzip.GzipFile(fileobj=ncbi_zipped_file)
    ncbi_unzipped_file = unzipping.read()
    ncbi_unzipped_file = ncbi_unzipped_file.strip()
    lines = ncbi_unzipped_file.split('\n')
    for line in lines:
        if line[0] == '#':
            continue
        columns = line.split('\t')
        hugo_entrez_map[columns[2]] = columns[1]
    return hugo_entrez_map

# ------------------------------------------------------------------------------
# looks up entrez ID from hugo gene
def getEntrezID(hugo):
    entrezID = HUGO_ENTREZ_MAP.get(hugo,'NA')
    return entrezID

# ------------------------------------------------------------------------------
# displays program usage (invalid args)

def usage():
    print >> OUTPUT_FILE, 'huge_entrez_map.py --get-map-from-ncbi=[true of false, required] --ncbi-file=[path to zipped ncbi file, optional]'

# ------------------------------------------------------------------------------
# the main!
def main():
    try:
        opts, args = getopt.getopt(sys.argv[1:], '', ['get-map-from-ncbi=','ncbi-file='])
    except getopt.error, msg:
        print >> ERROR_FILE, msg
        usage()
        sys.exit(2)

    # process the options
    download_map = ''
    filepath = ''
    for o, a in opts:
        if o == '--get-map-from-ncbi':
            download_map = a
            if download_map == 'true':
                download_map = True
            elif download_map == 'false':
                download_map = False
        elif o == '--ncbi-file':
            filepath = a

    # check download map option
    if download_map == '' or not type(download_map) is bool:
        usage()
        sys.exit(2)

    # Do the mapping via ftp or via file
    if download_map:
        hugo_entrez_map = ftp_NCBI()
    else:
        # check if file exists
        try:
            ncbi_file = open(filepath,'r')
        except IOError:
            print >> ERROR_FILE, 'file cannot be found: ' + filepath
            sys.exit(2)

        hugo_entrez_map = parse_ncbi_file(ncbi_file)
            

    print hugo_entrez_map

# ------------------------------------------------------------------------------
# vamos 

if __name__ == '__main__':
    main()
