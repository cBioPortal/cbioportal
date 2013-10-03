#! /usr/bin/env python
# script to generate PDB-Uniprot residue mapping file from MutationAssessor database.

import re
import os
import sys
import getopt
import MySQLdb

# example: "-99 -5--1 0 1-3 7-200 204 208 10001"
range_re = re.compile('(-?[0-9]+)(-(-?[0-9]+))?')
def range_to_list(range_str):
    idx = []
    ranges = range_str.split(' ')
    for r in ranges:
        re_result = range_re.match(r)
        start = int(re_result.group(1))
        end = int(re_result.group(3)) if re_result.group(3)!=None else start
        if end<start:
            print >>f, 'end smaller than start'
            sys.exit(2)
        idx.extend(range(start, end+1))
    return idx

def export_row(row, f):
    pdb_id = row[0]
    pdb_ch = row[1]
    uniprot_id = row[2]
    mbegin = row[3]
    pdb_res_map = row[4]
    uniprot_align = row[5]
    pdb_align = row[6]
    uniprot_from = row[7]
    pdb_from = row[8]

    length_align = uniprot_align.__len__()
    ix_uniprot = mbegin + uniprot_from -1
    ix_pdb = pdb_from
    for i in range(length_align):
        if uniprot_align[i] == pdb_align[i]:
            print >>f, "%s\t%s\t%i\t%s\t%i" % (pdb_id, pdb_ch, range_to_list(pdb_res_map)[ix_pdb-1], uniprot_id, ix_uniprot)
        if uniprot_align[i] != '-':
            ix_uniprot = ix_uniprot + 1
        if pdb_align[i] != '-':
            ix_pdb = ix_pdb + 1
    
    #print >>f, "%s\t%s\t%i\t%s\t%i" % (row[0], row[1], range_to_list(row[5])[row[2]-1], row[3], row[4])
    
def main():

    # parse command line options
    try:
        opts, args = getopt.getopt(sys.argv[1:], '', ['host=', 'user=', 'passwd=', 'db=', 'output='])
    except getopt.error, msg:
        print >>f, msg
        sys.exit(2)

    # process the options
    host = ''
    user = ''
    passwd = ''
    db = ''
    output = ''
    for o, a in opts:
        if o == '--host':
            host = a
        elif o == '--user':
            user = a
        elif o == '--passwd':
            passwd = a
        elif o == '--db':
            db = a
        elif o == '--output':
            output = a
    identpThreshold = '90'
        
    f = open(output, 'w')
    db = MySQLdb.connect(host,user,passwd,db)
    cursor = db.cursor()
    cursor.execute("select distinct pp.pdbid, pp.chcode, mb.seqID, mb.mbegin, pmr.res, pp.query, pp.hit, pp.qfrom, pp.hfrom "+
                   "from pdb_prot pp, msa_built mb, pdb_mol pm, pdb_molr pmr "+
                   "where pp.msaid=mb.id and pp.pdbid=pm.pdbid and pp.chcode=pm.chain and "+
                   "pp.pdbid=pmr.pdbid and pm.molid=pmr.molid and pm.type='protein' and mb.seqID like '%_HUMAN' "+
                   "and pp.identp>="+identpThreshold+";")
    print >>f, "#pdb_id\tchain\tpdb_res\tuniprot_id\tuniprot_res"
    for row in cursor:
        export_row(row, f)
    db.close()
    f.close()

if __name__ == '__main__':
    main()