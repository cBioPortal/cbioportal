/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.biojava.bio.structure.AminoAcid;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.ResidueNumber;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.align.util.AtomCache;
import org.biojava.bio.structure.io.FileParsingParameters;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.biojava3.core.sequence.compound.AminoAcidCompoundSet;
import org.biojava3.core.sequence.loader.UniprotProxySequenceReader;
import org.mskcc.cbio.portal.dao.DaoPdbUniprotResidueMapping;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoUniProtIdMapping;
import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.model.PdbUniprotAlignment;
import org.mskcc.cbio.portal.model.PdbUniprotResidueMapping;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.FileUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;

/**
 *
 * @author jgao
 */
public final class ImportPdbUniprotResidueMappingFromMA {
    private ImportPdbUniprotResidueMappingFromMA() {}

    /**
     * 
     *
     * @param mappingFile pdb-uniprot-residue-mapping.txt.
     * @param pMonitor Progress Monitor.
     */
    public static void importMutationAssessorData(File mappingFile, double identpThrehold,
            ProgressMonitor pMonitor) throws DaoException, IOException {
        MySQLbulkLoader.bulkLoadOn();
        FileReader reader = new FileReader(mappingFile);
        BufferedReader buf = new BufferedReader(reader);
        String line = buf.readLine();
        int alignId = DaoPdbUniprotResidueMapping.getLargestAlignmentId();
        PdbUniprotAlignment pdbUniprotAlignment = new PdbUniprotAlignment();
        List<PdbUniprotResidueMapping> pdbUniprotResidueMappings = Collections.emptyList();
        Map<Integer, Integer> mappingUniPdbProtein = Collections.emptyMap();
        Map<Integer, Integer> mappingUniPdbAlignment = Collections.emptyMap();
        Map<Integer, Integer> mappingPdbUniProtein = Collections.emptyMap();
        Map<Integer, Integer> mappingPdbUniAlignment = Collections.emptyMap();
        
        while (line != null) {
            if (!line.startsWith("#")) {
                String parts[] = line.split("\t",-1);
                if (line.startsWith(">")) {
                    // alignment line, eg. >1a37   A       1433B_HUMAN     1       32      3       34      0.000000        29.000000       90.625000       MDKSELVQKAKLAEQAERYDDMAAAMKAVTEQ        MDKNELVQKAKLAEQAERYDDMAACMKSVTEQ        MDK+ELVQKAKLAEQAERYDDMAA MK+VTEQ
                    
                    if (!pdbUniprotResidueMappings.isEmpty()) {
                        if (pdbUniprotAlignment.getIdentityPerc()>=identpThrehold) {
                            DaoPdbUniprotResidueMapping.addPdbUniprotAlignment(pdbUniprotAlignment);
                            for (PdbUniprotResidueMapping mapping : pdbUniprotResidueMappings) {
                                DaoPdbUniprotResidueMapping.addPdbUniprotResidueMapping(mapping);
                            }
                            mappingUniPdbProtein.putAll(mappingUniPdbAlignment);
                            mappingPdbUniProtein.putAll(mappingPdbUniAlignment);
                        }
                    }
                    
                    String pdbId = parts[0].substring(1);
                    if (!pdbId.equals(pdbUniprotAlignment.getPdbId())
                            || !parts[1].equals(pdbUniprotAlignment.getChain())
                            || !parts[2].equals(pdbUniprotAlignment.getUniprotId())) {
                        mappingUniPdbProtein = new HashMap<Integer, Integer>();
                        mappingPdbUniProtein = new HashMap<Integer, Integer>();
                    }
                    
                    pdbUniprotAlignment.setAlignmentId(++alignId);
                    
                    pdbUniprotAlignment.setPdbId(pdbId);
                    pdbUniprotAlignment.setChain(parts[1]);
                    pdbUniprotAlignment.setUniprotId(parts[2]);
                    
                    pdbUniprotAlignment.setPdbFrom(parts[3]);
                    pdbUniprotAlignment.setPdbTo(parts[4]);
                    pdbUniprotAlignment.setUniprotFrom(Integer.parseInt(parts[5]));
                    pdbUniprotAlignment.setUniprotTo(Integer.parseInt(parts[6]));
                    pdbUniprotAlignment.setEValue(Float.parseFloat(parts[7]));
                    pdbUniprotAlignment.setIdentity(Float.parseFloat(parts[8]));
                    pdbUniprotAlignment.setIdentityPerc(Float.parseFloat(parts[9]));
                    pdbUniprotAlignment.setUniprotAlign(parts[10]);
                    pdbUniprotAlignment.setPdbAlign(parts[11]);
                    pdbUniprotAlignment.setMidlineAlign(parts[12]);
                    
                    pdbUniprotResidueMappings = new ArrayList<PdbUniprotResidueMapping>();
                    mappingUniPdbAlignment = new HashMap<Integer, Integer>();
                    mappingPdbUniAlignment = new HashMap<Integer, Integer>();
                    
                } else {
                    // residue mapping line, e.g. 1a37    A       M1      1433B_HUMAN     M3      M
                    int pdbPos = Integer.parseInt(parts[2].substring(1));
                    int uniprotPos = Integer.parseInt(parts[4].substring(1));
                    Integer prePdb = mappingUniPdbProtein.get(uniprotPos);
                    Integer preUni = mappingPdbUniProtein.get(pdbPos);
                    if ((prePdb!=null && prePdb!=pdbPos) || (preUni!=null && preUni!=uniprotPos)) {
                        // mismatch
                        pdbUniprotResidueMappings.clear();
                        while (line !=null && !line.startsWith(">")) {
                            line = buf.readLine();
                            pMonitor.incrementCurValue();
                            ConsoleUtil.showProgress(pMonitor);
                        }
                        continue;
                    }
                    
                    mappingUniPdbAlignment.put(uniprotPos, pdbPos);
                    mappingPdbUniAlignment.put(pdbPos, uniprotPos);
                    
                    String match = parts[5].length()==0 ? " " : parts[5];
                    PdbUniprotResidueMapping pdbUniprotResidueMapping = new PdbUniprotResidueMapping(alignId, pdbPos, null, uniprotPos, match);
                    pdbUniprotResidueMappings.add(pdbUniprotResidueMapping);
                }

            }
            
            line = buf.readLine();
            
            pMonitor.incrementCurValue();
            ConsoleUtil.showProgress(pMonitor);
        }
        
        // last one
        if (!pdbUniprotResidueMappings.isEmpty()) {
            if (pdbUniprotAlignment.getIdentityPerc()>=identpThrehold) {
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
    
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("command line usage: importPdbUniprotResidueMappingMA.pl pdb-uniprot-residue-mapping.txt");
            return;
        }
        
        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(true);
        
        double identpThrehold = 50;
        try {
            File file = new File(args[0]);
            System.out.println("Reading PDB-UniProt residue mapping from:  " + file.getAbsolutePath());
            int numLines = FileUtil.getNumLines(file);
            System.out.println(" --> total number of lines:  " + numLines);
            pMonitor.setMaxValue(numLines);
            importMutationAssessorData(file, identpThrehold, pMonitor);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DaoException e) {
            e.printStackTrace();
        } finally {
            ConsoleUtil.showWarnings(pMonitor);
            System.err.println("Done.");
        }
    }
}
