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
    pdb_res_list = range_to_list(row[4])
    uniprot_align = row[5]
    pdb_align = row[6]
    midline_align = row[7]
    uniprot_from = row[8] + mbegin - 1
    pdb_from = row[9] - 1
    uniprot_to = row[10] + mbegin - 1
    pdb_to = row[11] - 1
    evalue = row[12]
    identity = row[13]
    identp = row[14]

    print >>f, ">%s\t%s\t%s\t%i\t%i\t%i\t%i\t%f\t%f\t%f\t%s\t%s\t%s" % (pdb_id, pdb_ch, uniprot_id, pdb_res_list[pdb_from], pdb_res_list[pdb_to], uniprot_from, uniprot_to, evalue, identity, identp, uniprot_align, pdb_align, midline_align)

    length_align = uniprot_align.__len__()
    ix_uniprot = uniprot_from
    ix_pdb = pdb_from
    for i in range(length_align):
        if uniprot_align[i] != '-' and pdb_align[i] != '-':
            print >>f, "%s\t%s\t%s%i\t%s\t%s%i\t%s" % (pdb_id, pdb_ch, pdb_align[i], pdb_res_list[ix_pdb], uniprot_id, uniprot_align[i], ix_uniprot, midline_align[i])
        if uniprot_align[i] != '-':
            ix_uniprot = ix_uniprot + 1
        if pdb_align[i] != '-':
            ix_pdb = ix_pdb + 1
        
def main():

    # parse command line options
    try:
        opts, args = getopt.getopt(sys.argv[1:], '', ['host=', 'port=', 'user=', 'passwd=', 'db=', 'output='])
    except getopt.error, msg:
        print >>f, msg
        sys.exit(2)

    # process the options
    host = ''
    port = 3306
    user = ''
    passwd = ''
    db = ''
    output = ''
    for o, a in opts:
        if o == '--host':
            host = a
        if o == '--port':
            port = int(a)
        elif o == '--user':
            user = a
        elif o == '--passwd':
            passwd = a
        elif o == '--db':
            db = a
        elif o == '--output':
            output = a
    #identpThreshold = '90'
        
    db = MySQLdb.connect(host=host,port=port,user=user,passwd=passwd,db=db)

    f = open(output, 'w')
    cursor = db.cursor()
    cursor.execute("select distinct pp.pdbid, pp.chcode, mb.seqID, mb.mbegin, pmr.res, pp.query, pp.hit, pp.midline, pp.qfrom, pp.hfrom, pp.qto, pp.hto, pp.evalue, pp.identity, pp.identp "+
                   "from pdb_prot pp, msa_built mb, pdb_mol pm, pdb_molr pmr "+
                   "where pp.msaid=mb.id and pp.pdbid=pm.pdbid and pp.chcode=pm.chain and "+
                   "pp.pdbid=pmr.pdbid and pm.molid=pmr.molid and pm.type='protein' and mb.seqID like '%_HUMAN' ORDER BY mb.seqID ASC, pp.pdbid ASC, pp.chcode ASC, pp.evalue DESC, pp.identp DESC, pp.identity DESC; ");
                   #"and pp.identp>="+identpThreshold+";")
    print >>f, "#>pdb_id\tchain\tuniprot_id\tpdb_from\tpdb_to\tuniprot_from\tuniprot_to\tevalue\tidentity\tidentp\tuniprot_seq\tpdb_seq\tmidline"
    print >>f, "#pdb_id\tchain\tpdb_res\tuniprot_id\tuniprot_res\tmidline"
    for row in cursor:
        export_row(row, f)
    f.close()

    db.close()

if __name__ == '__main__':
    main()