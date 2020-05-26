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

package org.mskcc.cbio.portal.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;


/**
 *
 * @author jgao
 */
public class TestMutationKeywordUtils {
    
	@Test
    public void testGuessOncotatorKeyword() throws Exception {
        // missense
        assertEquals("K13 missense", MutationKeywordUtils.guessOncotatorMutationKeyword("K13R", "Missense_Mutation"));
        assertEquals("M19 missense", MutationKeywordUtils.guessOncotatorMutationKeyword("M19K", "Missense_Mutation"));
        assertEquals("569-570 missense", MutationKeywordUtils.guessOncotatorMutationKeyword("569_570HG>QW", "Missense_Mutation"));
        
        // ins
        assertEquals("425 insertion", MutationKeywordUtils.guessOncotatorMutationKeyword("425_426insC", "In_Frame_Ins"));
        assertEquals("77 insertion", MutationKeywordUtils.guessOncotatorMutationKeyword("77_77T>NP", "In_Frame_Ins"));
        assertEquals("1583 insertion", MutationKeywordUtils.guessOncotatorMutationKeyword("E1583_Q1584insPVELMPPE", "NA"));
        assertEquals("1720 insertion", MutationKeywordUtils.guessOncotatorMutationKeyword("A1720_V1721ins14", "NA"));
        assertEquals("600 insertion", MutationKeywordUtils.guessOncotatorMutationKeyword("D600_L601ins10", "NA"));
        
        // del
        assertEquals("359-359 deletion", MutationKeywordUtils.guessOncotatorMutationKeyword("E359del", "In_Frame_Del"));
        assertEquals("517-521 deletion", MutationKeywordUtils.guessOncotatorMutationKeyword("517_521ESSTR>G", "In_Frame_Del"));
        assertEquals("274-341 deletion", MutationKeywordUtils.guessOncotatorMutationKeyword("W274_F341del", "NA"));
        assertEquals("188-191 deletion", MutationKeywordUtils.guessOncotatorMutationKeyword("L188_P191delLAPP", "NA"));
        assertEquals("485-490 deletion", MutationKeywordUtils.guessOncotatorMutationKeyword("L485_P490>Y", "NA"));
        
        // nonsense
        assertEquals("truncating", MutationKeywordUtils.guessOncotatorMutationKeyword("S371*", "Nonsense_Mutation"));
        assertEquals("truncating", MutationKeywordUtils.guessOncotatorMutationKeyword("2219_2220insVR**", "In_Frame_Ins"));
        
        // nonstart
        assertEquals("truncating", MutationKeywordUtils.guessOncotatorMutationKeyword("M1I", "Missense_Mutation"));
        assertEquals("truncating", MutationKeywordUtils.guessOncotatorMutationKeyword("1_1M>IA", "In_Frame_Ins"));
        assertEquals("truncating", MutationKeywordUtils.guessOncotatorMutationKeyword("1_2insL", "In_Frame_Ins"));
        assertEquals("truncating", MutationKeywordUtils.guessOncotatorMutationKeyword("M1del", "In_Frame_Del"));
        assertEquals("truncating", MutationKeywordUtils.guessOncotatorMutationKeyword("MPEEMD1del", "In_Frame_Del"));
        assertEquals("truncating", MutationKeywordUtils.guessOncotatorMutationKeyword("1_15MPPKVTSELLRQLRQ>K", "In_Frame_Del"));
        
        // nonstop
        assertEquals("truncating", MutationKeywordUtils.guessOncotatorMutationKeyword("*128Q", "Nonstop_Mutation"));
        assertEquals("truncating", MutationKeywordUtils.guessOncotatorMutationKeyword("473_477TVAS*>R", "In_Frame_Del"));
        assertEquals("truncating", MutationKeywordUtils.guessOncotatorMutationKeyword("904_905SE>*", "In_Frame_Del"));
        assertEquals("truncating", MutationKeywordUtils.guessOncotatorMutationKeyword("G165_*404del", "NA"));
        
        // fs
        assertEquals("truncating", MutationKeywordUtils.guessOncotatorMutationKeyword("M253fs", "Frame_Shift_Ins"));
        assertEquals("truncating", MutationKeywordUtils.guessOncotatorMutationKeyword("Y122fs", "Frame_Shift_Del"));
        assertEquals("truncating", MutationKeywordUtils.guessOncotatorMutationKeyword("S2751fs*27", "NA"));
        
        // sp
        assertEquals("truncating", MutationKeywordUtils.guessOncotatorMutationKeyword("R31_splice", "Splice_Site"));
        
        // others
        assertEquals("Exon skipping", MutationKeywordUtils.guessOncotatorMutationKeyword("Exon skipping", "Exon skipping"));
        assertEquals("vIII deletion", MutationKeywordUtils.guessOncotatorMutationKeyword("vIII deletion", "vIII deletion"));
        assertEquals("Fusion", MutationKeywordUtils.guessOncotatorMutationKeyword("Fusion", "Fusion"));
    }
    
	@Test
    public void testGuessCosmicKeyword() throws Exception {
        assertNull(MutationKeywordUtils.guessCosmicKeyword(""));
        assertNull(MutationKeywordUtils.guessCosmicKeyword("0"));
        assertNull(MutationKeywordUtils.guessCosmicKeyword("?"));
        assertNull(MutationKeywordUtils.guessCosmicKeyword("*730?"));
        assertNull(MutationKeywordUtils.guessCosmicKeyword("(=)"));
        assertNull(MutationKeywordUtils.guessCosmicKeyword("P248?"));
        assertNull(MutationKeywordUtils.guessCosmicKeyword("K267_D268ins?"));
        assertNull(MutationKeywordUtils.guessCosmicKeyword("WQQQSYLD25?"));
        assertNull(MutationKeywordUtils.guessCosmicKeyword("M1>?"));
        
        // silent
        assertEquals("*803 silent", MutationKeywordUtils.guessCosmicKeyword("*803*"));
        assertEquals("A803 silent", MutationKeywordUtils.guessCosmicKeyword("A803A"));
        assertEquals("A803 silent", MutationKeywordUtils.guessCosmicKeyword("A803>A"));
        
        // missense
        assertEquals("A2 missense", MutationKeywordUtils.guessCosmicKeyword("A2S"));
        assertEquals("M19 missense", MutationKeywordUtils.guessCosmicKeyword("M19K"));
        assertEquals("475-477 missense", MutationKeywordUtils.guessCosmicKeyword("Q475_E477>VLQ"));
        
        // insertion
        assertEquals("1069 insertion", MutationKeywordUtils.guessCosmicKeyword("*1069_*1069insWKDN*"));
        assertEquals("574 insertion", MutationKeywordUtils.guessCosmicKeyword("T574_Q575ins12"));
        assertEquals("1593 insertion", MutationKeywordUtils.guessCosmicKeyword("L1593_R1594ins12"));
        assertEquals("1592 insertion", MutationKeywordUtils.guessCosmicKeyword("F1592>LGP"));
        assertEquals("6 insertion", MutationKeywordUtils.guessCosmicKeyword("C6>SG"));
        assertEquals("630 insertion", MutationKeywordUtils.guessCosmicKeyword("D630_I631insRG"));
        assertEquals("299 insertion", MutationKeywordUtils.guessCosmicKeyword("299_300insGRS"));
        assertEquals("976 insertion", MutationKeywordUtils.guessCosmicKeyword("V976_C977ins3"));
        assertEquals("452 insertion", MutationKeywordUtils.guessCosmicKeyword("Y452_Q455>SGGSRIK")); // this is actually missense?
        assertEquals("475 insertion", MutationKeywordUtils.guessCosmicKeyword("Q475_E476>VLQ")); // this is actually missense?
        
        // deletion
        assertEquals("738-738 deletion", MutationKeywordUtils.guessCosmicKeyword("G738delG"));
        assertEquals("400-400 deletion", MutationKeywordUtils.guessCosmicKeyword("K400del"));
        assertEquals("146-148 deletion", MutationKeywordUtils.guessCosmicKeyword("L146_D148delLRD"));
        assertEquals("418-419 deletion", MutationKeywordUtils.guessCosmicKeyword("Y418_D419del"));
        assertEquals("449-459 deletion", MutationKeywordUtils.guessCosmicKeyword("L449_K459del11"));
        assertEquals("454-455 deletion", MutationKeywordUtils.guessCosmicKeyword("T454_Q455>Q")); // actually only one Q got deleted
        assertEquals("456-461 deletion", MutationKeywordUtils.guessCosmicKeyword("F456_R461>S")); // this is actually missense?
        assertEquals("459-460 deletion", MutationKeywordUtils.guessCosmicKeyword("K459_S460>N")); // GENE=PIK3R1;STRAND=+;CDS=c.1377_1379delAAG;AA=p.K459_S460>N;CNT=1
        
        // frameshift
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("D160fs*47"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("R228fs>25"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("G297fs*>3"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("A254fs"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("(2287)fs"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("N342fs?"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("*139fs?"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("(R91)fs"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("215fs"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("(E55)fs*?"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("L617fs*"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("*1069fs*3"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("*257fs*>4"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("557fs*?"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("?fs"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("R412_*413delR*"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("(P1249)fs*?"));
        
        // non sense
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("Q341*"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("P198>*"));
        
            // delete *
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("*707del*"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("S663_*665delSY*"));
        
            // insert *
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("Q1365>H*"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("Y16_Q17>*"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("K664_K665>N*"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("Y74_Y75ins*"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("Q1365>H*"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("D65_E67>***"));
        
        // non start
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("M1T"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("M1delM"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("M1_A2>IP"));
        
        // non stop
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("*90Y"));
        assertEquals("truncating", MutationKeywordUtils.guessCosmicKeyword("T473_*477>R"));
        
    }
}
