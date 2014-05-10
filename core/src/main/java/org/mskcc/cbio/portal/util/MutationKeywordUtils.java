/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/
package org.mskcc.cbio.portal.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author jgao
 */
public final class MutationKeywordUtils {
    private MutationKeywordUtils() {}
    
    public static String guessOncotatorMutationKeyword(String aminoAcidChange, String mutationType) {
        if (mutationType.equals("Nonsense_Mutation") ||
            mutationType.equals("Splice_Site") || 
            mutationType.startsWith("Frame_Shift_") || 
            mutationType.equals("Nonstop_Mutation") ||
            mutationType.equals("Translation_Start_Site")) {
            return "truncating";
        }
        
        if (mutationType.equals("Missense_Mutation")) {
            if (aminoAcidChange.matches("M1[A-LN-Z][A-Z]*")) { // how about indels on the first position?
                // non-start
                return "truncating";
            }
            
            Pattern p = Pattern.compile("([A-Z][0-9]+)[A-Z]");
            Matcher m = p.matcher(aminoAcidChange);
            if (m.matches()) {
                return m.group(1) + " missense";
            }
            
            p = Pattern.compile("([0-9]+)_([0-9]+)[A-Z]+>[A-Z]+");
            m = p.matcher(aminoAcidChange);
            if (m.matches()) {
                return m.group(1) + "-" + m.group(2) + " missense";
            }
        }
        
        if (mutationType.equals("In_Frame_Ins")) {
            if (aminoAcidChange.matches("[0-9]+_[0-9]+[A-Z]*((ins)|(>)).*\\*.*")) {
                // insertion of *
                return "truncating";
            }
            
            if (aminoAcidChange.matches("1_1M>?[A-LN-Z][A-Z]*")
                    || aminoAcidChange.matches("1_2ins[A-LN-Z][A-Z]*")) {// insertion in the start position
                return "truncating";
            }
            
            Pattern p = Pattern.compile("([0-9]+)_([0-9]+)[A-Z]*((ins)|(>))[A-Z]+");
            Matcher m = p.matcher(aminoAcidChange);
            if (m.find()) {
               return "" + m.group(1) + " insertion";
            }
        }
        
        if (mutationType.equals("In_Frame_Del")) {
            if (aminoAcidChange.matches("M[A-Z]*1del")
                    || aminoAcidChange.matches("1_[0-9]+M[A-Z]*>[A-LN-Z][A-Z]*")) {
                // deletion of M1
                return "truncating";
            }
            
            if (aminoAcidChange.matches("[A-Z]*\\*[0-9]+del")
                    || aminoAcidChange.matches("[0-9]+_[0-9]+[A-Z]*\\*>[A-Z]+")) {
                // deletion of *
                return "truncating";
            }
            
            if (aminoAcidChange.matches("[0-9]+_[0-9]+[A-Z]*>.*\\*.*")) {
                // deletion of *
                return "truncating";
            }
            
            // only the first deleted residue was considered
            Pattern p = Pattern.compile("([A-Z]+)([0-9]+)del");
            Matcher m = p.matcher(aminoAcidChange);
            if (m.matches()) {
               return "" + m.group(2) + "-" + (Integer.parseInt(m.group(2))+m.group(1).length()-1) + " deletion";
            }
            
            p = Pattern.compile("([0-9]+)_([0-9]+)[A-Z]*>[A-Z]+");
            m = p.matcher(aminoAcidChange);
            if (m.find()) {
               return m.group(1) + "-" + m.group(2) + " deletion";
            }
        }
        
        if (mutationType.equals("Silent")) {
            Pattern p = Pattern.compile("([0-9]+)");
            Matcher m = p.matcher(aminoAcidChange);
            if (m.find()) {
               return "" + m.group(1) + "silent";
            }
        }
        
        if (mutationType.equals("Fusion")
                || mutationType.equals("Exon skipping")
                || mutationType.equals("vIII deletion")) {
            return aminoAcidChange;
        }
        
        if (mutationType.equals("NA")) {
            return guessCosmicKeyword(aminoAcidChange);
        }
            
        // how about RNA or Translation_Start_Site
        
        return null;
    }
    
    /**
     * 
     * @param hugo
     * @param aminoAcidChange
     * @param cds
     * @return 
     */
    public static String guessCosmicKeyword(String aminoAcidChange) {
        if (aminoAcidChange.matches("\\(?[A-Z\\*]?[0-9]+\\)?fs\\*?>?\\??[0-9]*") // frameshift
                || aminoAcidChange.equals("?fs") // frameshift
                || aminoAcidChange.matches("[A-Z][0-9]+>?\\*") // non sense
                || aminoAcidChange.matches("M1>?[A-LN-Z]") // non start
                || aminoAcidChange.matches("M1delM") // non start, deletion of M1
                || aminoAcidChange.matches("M1_M1ins[A-LN-Z][A-Z]*") // non start, insertion of non M at start
                || aminoAcidChange.matches("([A-Z][0-9]+_)?\\*[0-9]+>?[A-Z]") // non stop
                || aminoAcidChange.matches("([A-Z][0-9]+_)\\*[0-9]+del([A-Z]+\\*)?") // delete *
                || aminoAcidChange.matches("\\*[0-9]+del\\*") // delete *
                || aminoAcidChange.matches("[A-Z][0-9]+(_[A-Z][0-9]+)?((ins)|(>)).*\\*.*") // inserting a stop
                || aminoAcidChange.matches("[A-Z][0-9]+_[A-Z][0-9]+>.*\\*.*") // repleacing/inserting/deletion a stop
                || aminoAcidChange.matches("M1_[A-Z][0-9]+>[A-LN-Z][A-Z]*") // repleacing/inserting/deletion the first codon
                ) {
            return "truncating";
        }
        
        Pattern p = Pattern.compile("(([A-Z\\*])[0-9]+)>?\\2");
        Matcher m = p.matcher(aminoAcidChange);
        if (m.matches()) {
            return m.group(1)+" silent";
        }
        
        p = Pattern.compile("([A-Z]([0-9]+))>?([A-Z]+)");
        m = p.matcher(aminoAcidChange);
        if (m.matches()) {
            if (m.group(3).length()>1) {
                return m.group(2)+" insertion";
            }
            return m.group(1)+" missense";
        }
        
        p = Pattern.compile("[A-Z]?([0-9]+)_[A-Z]?[0-9]+ins(([A-Z]+)|([0-9]+))");
        m = p.matcher(aminoAcidChange);
        if (m.matches()) {
            return m.group(1)+" insertion";
        }
        
        p = Pattern.compile("[A-Z]?([0-9]+)>[A-Z][A-Z]+");
        m = p.matcher(aminoAcidChange);
        if (m.matches()) {
            return m.group(1)+" insertion";
        }
        
        p = Pattern.compile("\\*([0-9]+)_\\*[0-9]+ins[A-Z]+\\*?");
        m = p.matcher(aminoAcidChange);
        if (m.matches()) {
            return m.group(1)+" insertion";
        }
        
        p = Pattern.compile("[A-Z]([0-9]+)del[A-Z]?");
        m = p.matcher(aminoAcidChange);
        if (m.matches()) {
            return m.group(1) + "-" + m.group(1) + " deletion";
        }
        
        p = Pattern.compile("[A-Z]([0-9]+)_[A-Z]([0-9]+)del(([A-Z]+)|([0-9]+))?");
        m = p.matcher(aminoAcidChange);
        if (m.matches()) {
            return m.group(1) + "-" + m.group(2) + " deletion";
        }
        
        p = Pattern.compile("[A-Z]([0-9]+)_[A-Z]([0-9]+)>([A-Z]+)"); // this is actually similar to missense mutation for more than 1 amino acid
        m = p.matcher(aminoAcidChange);
        if (m.matches()) {
            int n1 = Integer.parseInt(m.group(2)) - Integer.parseInt(m.group(1)) + 1;
            int n2 = m.group(3).length();
            if (n1==n2) {
                return m.group(1) + "-" + m.group(2) + " missense";
            }
            
            if (n1 > n2) {
                return m.group(1) + "-" + m.group(2) + " deletion";
            }
            
            return m.group(1) + " insertion";
        }
        
        return null;
    }
    
}
