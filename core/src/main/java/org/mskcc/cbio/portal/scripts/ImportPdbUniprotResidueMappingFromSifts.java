/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.scripts;

import java.io.*;
import java.util.*;
import org.biojava.nbio.structure.*;
import org.biojava.nbio.structure.align.util.AtomCache;
import org.biojava.nbio.structure.io.FileParsingParameters;
import org.biojava.nbio.core.sequence.compound.*;
import org.biojava.nbio.core.sequence.loader.UniprotProxySequenceReader;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.util.*;

/**
 *
 * @author jgao
 */
public final class ImportPdbUniprotResidueMappingFromSifts {
    private ImportPdbUniprotResidueMappingFromSifts() {}

    /**
     * 
     *
     * @param mappingFile pdb-uniprot-residue-mapping.txt.
     */
    public static void importSiftsData(File mappingFile, Set<String> humanChains,
            String pdbCacheDir, double identp_threhold)
            throws DaoException, IOException {
        MySQLbulkLoader.bulkLoadOn();
        FileReader reader = new FileReader(mappingFile);
        BufferedReader buf = new BufferedReader(reader);
        int alignId = DaoPdbUniprotResidueMapping.getLargestAlignmentId();
        
        String line = buf.readLine();
        while (line.startsWith("#")) {
            line = buf.readLine();
        }
        
            
        AtomCache atomCache = getAtomCache(pdbCacheDir);
            
        buf.readLine(); // skip head
        
        for (; line != null; line = buf.readLine()) {
            ProgressMonitor.incrementCurValue();
            ConsoleUtil.showProgress();
            
            String[] parts = line.split("\t");
            String pdbId = parts[0];
            String chainId = parts[1];
            
            if (!humanChains.contains(pdbId+"."+chainId)) {
                continue;
            }
            
            System.out.println("processing "+line);
            
            String uniprotAcc = parts[2];
            
            int pdbSeqResBeg = Integer.parseInt(parts[3]);
            int pdbSeqResEnd = Integer.parseInt(parts[4]);
            int uniprotResBeg = Integer.parseInt(parts[7]);
            int uniprotResEnd = Integer.parseInt(parts[8]);
            
            if (pdbSeqResBeg-pdbSeqResEnd != uniprotResBeg-uniprotResEnd) {
                System.err.println("*** Lengths not equal");
                continue;
            }
            
//            String pdbAtomResBeg = parts[5]; // could have insertion code
//            String pdbAtomResEnd = parts[6]; // could have insertion code
            
            
            PdbUniprotAlignment pdbUniprotAlignment = new PdbUniprotAlignment();
            List<PdbUniprotResidueMapping> pdbUniprotResidueMappings = new ArrayList<PdbUniprotResidueMapping>();
            
            if (processPdbUniprotAlignment(pdbUniprotAlignment, pdbUniprotResidueMappings,
                    ++alignId, pdbId, chainId, uniprotAcc, uniprotResBeg,
                    uniprotResEnd, pdbSeqResBeg, pdbSeqResEnd, identp_threhold, atomCache)) {
                DaoPdbUniprotResidueMapping.addPdbUniprotAlignment(pdbUniprotAlignment);
                for (PdbUniprotResidueMapping mapping : pdbUniprotResidueMappings) {
                    DaoPdbUniprotResidueMapping.addPdbUniprotResidueMapping(mapping);
                }
            }
        }

        //  Flush database
        if (MySQLbulkLoader.isBulkLoad()) {
           MySQLbulkLoader.flushAll();
        }
    }
    
    private static boolean processPdbUniprotAlignment(
            PdbUniprotAlignment pdbUniprotAlignment, List<PdbUniprotResidueMapping> pdbUniprotResidueMappings,
            int alignId, String pdbId, String chainId, String uniprotAcc, int uniprotResBeg,
            int uniprotResEnd, int pdbSeqResBeg, int pdbSeqResEnd, double identp_threhold, AtomCache atomCache) throws DaoException {

        String uniprotSeq = getUniprotSequence(uniprotAcc, uniprotResBeg, uniprotResEnd);
        if (uniprotSeq==null) {
            System.err.println("Could not read UniProt Sequence");
            return false;
        }
        
        List<Group>pdbResidues = getPdbResidues(atomCache, pdbId, chainId, pdbSeqResBeg, pdbSeqResEnd);
        
        int len = uniprotResEnd-uniprotResBeg+1;
        
        if (pdbResidues.size()!=len) {
            System.err.println("*** Lengths not correct from structure");
            return false;
        }
        
        int start = 0;
        for (; start<len; start++) {
            if (pdbResidues.get(start).getResidueNumber()!=null) {
                break;
            }
        }
        
        if (start==len) {
            System.err.print("No atom residues");
            return false;
        }
        
        int end;
        for (end=len; end>start; end--) {
            if (pdbResidues.get(end-1).getResidueNumber()!=null) {
                break;
            }
        }
        
        int identity = 0;
        StringBuilder midline = new StringBuilder();
        StringBuilder pdbAlign = new StringBuilder();
        for (int i=start; i<end; i++) {
            Group pdbResidue = pdbResidues.get(i);
            if (!(pdbResidue instanceof AminoAcid)) {
                System.err.println("*** Non amino acid");
                return false;
            }
            
            ResidueNumber rn = pdbResidue.getResidueNumber();
            
            char pdbAA = ((AminoAcid)pdbResidue).getAminoType();
            char uniprotAA = uniprotSeq.charAt(i);
            char match = ' ';
            
            if (rn==null) { // if not a atom residue
                match = '-';
            } else if (pdbAA == uniprotAA) {
                identity++;
                match = pdbAA;
            }
            
            midline.append(match);
            pdbAlign.append(match);
            
            if (rn!=null) {
                PdbUniprotResidueMapping pdbUniprotResidueMapping = 
                        new PdbUniprotResidueMapping(alignId, rn.getSeqNum(),
                        rn.getInsCode()==null?null:rn.getInsCode().toString(),
                        uniprotResBeg+i, ""+match);
                pdbUniprotResidueMappings.add(pdbUniprotResidueMapping);
            }
        }
        
        double identp = identity*100.0/(end-start);
        
        if (identp < identp_threhold) {
            System.out.print("*** low identp: "+identp);
            return false;
        }
        
        pdbUniprotAlignment.setAlignmentId(alignId);

        pdbUniprotAlignment.setPdbId(pdbId);
        pdbUniprotAlignment.setChain(chainId);
        
        String uniprotId = DaoUniProtIdMapping.mapFromUniprotAccessionToUniprotId(uniprotAcc);
        if (uniprotId==null) {
            System.out.println("could not mapping uniprotacc " + uniprotAcc);
            return false;
        }
        pdbUniprotAlignment.setUniprotId(uniprotId);
        
        ResidueNumber startRes = pdbResidues.get(start).getResidueNumber();
        ResidueNumber endRes = pdbResidues.get(end-1).getResidueNumber();
        pdbUniprotAlignment.setPdbFrom(Integer.toString(startRes.getSeqNum())
                +(startRes.getInsCode()==null?"":startRes.getInsCode()));
        pdbUniprotAlignment.setPdbTo(Integer.toString(endRes.getSeqNum())
                +(endRes.getInsCode()==null?"":endRes.getInsCode()));
        pdbUniprotAlignment.setUniprotFrom(uniprotResBeg+start);
        pdbUniprotAlignment.setUniprotTo(uniprotResBeg+end-1);
//        pdbUniprotAlignment.setEValue(null);
        pdbUniprotAlignment.setUniprotAlign(uniprotSeq.substring(start, end));
        
        pdbUniprotAlignment.setIdentity((float)identity);
        pdbUniprotAlignment.setIdentityPerc((float)(identp));
        pdbUniprotAlignment.setPdbAlign(pdbAlign.toString());
        pdbUniprotAlignment.setMidlineAlign(midline.toString());
        
        return true;
}
    
    private static AtomCache getAtomCache(String dirCache) {
        AtomCache atomCache = new AtomCache(dirCache, true);
        FileParsingParameters params = new FileParsingParameters();
        params.setAlignSeqRes(true);
        params.setParseSecStruc(false);
        params.setUpdateRemediatedFiles(false);
        atomCache.setFileParsingParams(params);
        atomCache.setAutoFetch(true);
        return atomCache;
    }
    
    private static List<Group> getPdbResidues(AtomCache atomCache, String pdbId, String chainId, int start, int end) {
        try {
            Structure struc = atomCache.getStructure(pdbId);
            
            if (struc!=null) {
                Chain chain = struc.getChainByPDB(chainId);
                return chain.getSeqResGroups().subList(start-1, end);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
            return Collections.emptyList();
    }
    
    private static String getUniprotSequence(String uniportAcc, int start, int end) {
        try {
            UniprotProxySequenceReader<AminoAcidCompound> uniprotSequence
                    = new UniprotProxySequenceReader<AminoAcidCompound>(uniportAcc, AminoAcidCompoundSet.getAminoAcidCompoundSet());
            return uniprotSequence.getSequenceAsString().substring(start-1, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static Set<String> readHumanChains(String file) throws IOException {
        Set<String> humanChains = new HashSet<String>();
        FileReader reader = new FileReader(file);
        BufferedReader buf = new BufferedReader(reader);
        for (String line = buf.readLine(); line != null; line = buf.readLine()) {
            String[] parts = line.split("\t");
            humanChains.add(parts[0]+"."+parts[1]);
        }
        return humanChains;
    }
    
    public static void main(String[] args) throws Exception {        
        if (args.length < 2) {
            System.out.println("command line usage:  importPdbUniprotResidueMapping.pl <pdb_chain_uniprot.tsv> <pdb_chain_human.tsv> <pdb-cache-dir>");
            return;
        }
        
        String pdbCacheDir = args.length>2 ? args[2] : System.getProperty("java.io.tmpdir");
    
        ProgressMonitor.setConsoleMode(true);

		SpringUtil.initDataSource();
        
        double identpThrehold = 80;

        try {
            Set<String> humanChains = readHumanChains(args[1]);
            
            File file = new File(args[0]);
            System.out.println("Reading PDB-UniProt residue mapping from:  " + file.getAbsolutePath());
            int numLines = FileUtil.getNumLines(file);
            System.out.println(" --> total number of lines:  " + numLines);
            ProgressMonitor.setMaxValue(numLines);
            importSiftsData(file, humanChains, pdbCacheDir, identpThrehold);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DaoException e) {
            e.printStackTrace();
        } finally {
            ConsoleUtil.showWarnings();
            System.err.println("Done.");
        }
    }
}
