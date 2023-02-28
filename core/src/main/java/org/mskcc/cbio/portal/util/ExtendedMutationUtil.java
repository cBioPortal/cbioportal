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

import org.mskcc.cbio.maf.MafRecord;
import org.mskcc.cbio.maf.TabDelimitedFileUtil;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.ExtendedMutation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class related to ExtendedMutation.
 */
public class ExtendedMutationUtil {
    // originally NA (case ignored) was the only value for empty values in this column,
    // but [Not Available] occurs in many TCGA studies.
    // TODO if this list grows, create NotAvailableValues enum like in ImportClinicalData.java
    public static final List <String> NOT_AVAILABLE = Arrays.asList("NA", "[Not Available]");

    public static String getCaseId(String barCode) {
        // process bar code
        // an example bar code looks like this:  TCGA-13-1479-01A-01W

        String barCodeParts[] = barCode.split("-");

        String caseId = null;

        try {
            caseId = barCodeParts[0] + "-" + barCodeParts[1] + "-" + barCodeParts[2];

            // the following condition was prompted by case ids coming from
            // private cancer studies (like SKCM_BROAD) with case id's of
            // the form MEL-JWCI-WGS-XX or MEL-Ma-Mel-XX or MEL-UKRV-Mel-XX
            if (!barCode.startsWith("TCGA") &&
                barCodeParts.length == 4) {
                caseId += "-" + barCodeParts[3];
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            caseId = barCode;
        }

        return caseId;
    }

    /**
     * Determines the most accurate protein change value for the given mutation.
     *
     * If there is an annotator value, returns that value.
     * If no annotator value, then tries Amino Acid Change value.
     * If none of the above is valid then returns "MUTATED".
     *
     * @param parts     current mutation as split parts of the line
     * @param record    MAF record for the current line
     * @return          most accurate protein change
     */
    public static String getProteinChange(String[] parts, MafRecord record) {
        // try annotator value first
        String proteinChange = record.getProteinChange();

        // if protein change is not valid, try amino acid change value
        if (!isValidProteinChange(proteinChange)) {
            proteinChange = record.getAminoAcidChange();
        }

        // if none is valid, then use the string "MUTATED"
        if (!isValidProteinChange(proteinChange)) {
            proteinChange = "MUTATED";
        }

        // also remove the starting "p." string if any
        proteinChange = normalizeProteinChange(proteinChange);

        return proteinChange;
    }

    /**
     * Removes the starting "p." (if any) from the given
     * amino acid change string.
     *
     * @param aminoAcidChange   aa change string to be normalized
     * @return                  normalized aa change string
     */
    public static String normalizeProteinChange(String aminoAcidChange) {
        String pDot = "p.";

        // remove the starting "p." string if any
        if (aminoAcidChange.startsWith(pDot)) {
            aminoAcidChange = aminoAcidChange.substring(pDot.length());
        }

        return aminoAcidChange;
    }

    public static int getProteinPosStart(String proteinPosition, String proteinChange) {
        // parts[0] is the protein start-end positions, parts[1] is the length
        String[] parts = proteinPosition.split("/");

        int position = TabDelimitedFileUtil.getPartInt(0, parts[0].split("-"));

        // there is a case where the protein change is "-"
        if (position == TabDelimitedFileUtil.NA_INT) {
            // try to extract it from protein change value
            Map<String, Integer> annotation = annotateProteinChange(proteinChange);

            if (annotation.get("start") != null) {
                position = annotation.get("start");
            }
        }

        return position;
    }

    public static int getProteinPosEnd(String proteinPosition, String proteinChange) {
        // parts[0] is the protein start-end positions, parts[1] is the length
        String[] parts = proteinPosition.split("/");

        int end = TabDelimitedFileUtil.getPartInt(1, parts[0].split("-"));

        // if no end position is provided,
        // then use start position as end position
        if (end == -1) {
            Map<String, Integer> annotation = annotateProteinChange(proteinChange);
            if (annotation.get("end") != null) {
                end = annotation.get("end");
            }
        }

        return end;
    }

    public static boolean isValidProteinChange(String proteinChange) {
        boolean invalid = proteinChange == null ||
                proteinChange.length() == 0 ||
                proteinChange.equalsIgnoreCase("NULL") ||
                NOT_AVAILABLE.stream().anyMatch(s -> s.equalsIgnoreCase(proteinChange));
        return !invalid;
    }

    public static boolean isAcceptableMutation(String mutationType) {
        // check for null or NA values
        if (mutationType == null ||
                mutationType.length() == 0 ||
                mutationType.equals("NULL") ||
                mutationType.equals(TabDelimitedFileUtil.NA_STRING)) {
            return false;
        }

        // check for the type
        boolean silent = mutationType.toLowerCase().startsWith("silent");
        boolean loh = mutationType.toLowerCase().startsWith("loh");
        boolean wildtype = mutationType.toLowerCase().startsWith("wildtype");
        boolean utr3 = mutationType.toLowerCase().startsWith("3'utr");
        boolean utr5 = mutationType.toLowerCase().startsWith("5'utr");
        boolean flank5 = mutationType.toLowerCase().startsWith("5'flank");
        boolean igr = mutationType.toLowerCase().startsWith("igr");
        boolean rna = mutationType.equalsIgnoreCase("rna");

        return !(silent || loh || wildtype || utr3 || utr5 || flank5 || igr || rna);
    }

    public static String getMutationType(MafRecord record) {
        String mutationType = record.getOncotatorVariantClassification();

        if (mutationType == null ||
                mutationType.length() == 0 ||
                mutationType.equals("NULL") ||
                mutationType.equals(TabDelimitedFileUtil.NA_STRING)) {
            mutationType = record.getVariantClassification();
        }

        return mutationType;
    }

    public static Integer getTumorAltCount(MafRecord record) {
        Integer result = null;

        if (record.getTumorAltCount() != TabDelimitedFileUtil.NA_INT) {
            result = record.getTumorAltCount();
        } else if(record.getTVarCov() != TabDelimitedFileUtil.NA_INT) {
            result = record.getTVarCov();
        } else if((record.getTumorDepth() != TabDelimitedFileUtil.NA_INT) &&
                (record.getTumorVaf() != TabDelimitedFileUtil.NA_INT)) {
            result = Math.round(record.getTumorDepth() * record.getTumorVaf());
        }

        return result;
    }

    public static Integer getTumorRefCount(MafRecord record) {
        Integer result = null;

        if (record.getTumorRefCount() != TabDelimitedFileUtil.NA_INT) {
            result = record.getTumorRefCount();
        } else if((record.getTVarCov() != TabDelimitedFileUtil.NA_INT) &&
                (record.getTTotCov() != TabDelimitedFileUtil.NA_INT)) {
            result = record.getTTotCov()-record.getTVarCov();
        } else if((record.getTumorDepth() != TabDelimitedFileUtil.NA_INT) &&
                (record.getTumorVaf() != TabDelimitedFileUtil.NA_INT)) {
            result = record.getTumorDepth() - Math.round(record.getTumorDepth() * record.getTumorVaf());
        }

        return result;
    }

    public static Integer getNormalAltCount(MafRecord record) {
        Integer result = null;

        if (record.getNormalAltCount() != TabDelimitedFileUtil.NA_INT) {
            result = record.getNormalAltCount();
        } else if(record.getNVarCov() != TabDelimitedFileUtil.NA_INT) {
            result = record.getNVarCov();
        } else if((record.getNormalDepth() != TabDelimitedFileUtil.NA_INT) &&
                (record.getNormalVaf() != TabDelimitedFileUtil.NA_INT)) {
            result = Math.round(record.getNormalDepth() * record.getNormalVaf());
        }

        return result;
    }

    public static Integer getNormalRefCount(MafRecord record) {
        Integer result = null;

        if (record.getNormalRefCount() != TabDelimitedFileUtil.NA_INT) {
            result = record.getNormalRefCount();
        } else if((record.getNVarCov() != TabDelimitedFileUtil.NA_INT) &&
                (record.getNTotCov() != TabDelimitedFileUtil.NA_INT)) {
            result = record.getNTotCov()-record.getNVarCov();
        } else if((record.getNormalDepth() != TabDelimitedFileUtil.NA_INT) &&
                (record.getNormalVaf() != TabDelimitedFileUtil.NA_INT)) {
            result = record.getNormalDepth() - Math.round(record.getNormalDepth() * record.getNormalVaf());
        }

        return result;
    }

    /**
     * Generates a new ExtendedMutation instance with default values.
     * The mutation instance returned by this method will not have
     * any field with a "null" value.
     *
     * @return  a mutation instance initialized by default values
     */
    public static ExtendedMutation newMutation() {
        Integer defaultInt = TabDelimitedFileUtil.NA_INT;
        String defaultStr = TabDelimitedFileUtil.NA_STRING;
        //Long defaultLong = TabDelimitedFileUtil.NA_LONG;
        Long defaultLong = -1L;
        Float defaultFloat = TabDelimitedFileUtil.NA_FLOAT;
        CanonicalGene defaultGene = new CanonicalGene("INVALID");

        ExtendedMutation mutation = new ExtendedMutation();

        mutation.setGeneticProfileId(defaultInt);
        mutation.setSampleId(defaultInt);
        mutation.setGene(defaultGene);
        mutation.setSequencingCenter(defaultStr);
        mutation.setSequencer(defaultStr);
        mutation.setProteinChange(defaultStr);
        mutation.setMutationType(defaultStr);
        mutation.setChr(defaultStr);
        mutation.setStartPosition(defaultLong);
        mutation.setEndPosition(defaultLong);
        mutation.setValidationStatus(defaultStr);
        mutation.setMutationStatus(defaultStr);
        mutation.setFunctionalImpactScore(defaultStr);
        mutation.setFisValue(defaultFloat);
        mutation.setLinkXVar(defaultStr);
        mutation.setLinkPdb(defaultStr);
        mutation.setLinkMsa(defaultStr);
        mutation.setNcbiBuild(defaultStr);
        mutation.setStrand(defaultStr);
        mutation.setVariantType(defaultStr);
        mutation.setAllele(defaultStr, defaultStr, defaultStr);
        mutation.setDbSnpRs(defaultStr);
        mutation.setDbSnpValStatus(defaultStr);
        mutation.setMatchedNormSampleBarcode(defaultStr);
        mutation.setMatchNormSeqAllele1(defaultStr);
        mutation.setMatchNormSeqAllele2(defaultStr);
        mutation.setTumorValidationAllele1(defaultStr);
        mutation.setTumorValidationAllele2(defaultStr);
        mutation.setMatchNormValidationAllele1(defaultStr);
        mutation.setMatchNormValidationAllele2(defaultStr);
        mutation.setVerificationStatus(defaultStr);
        mutation.setSequencingPhase(defaultStr);
        mutation.setSequenceSource(defaultStr);
        mutation.setValidationMethod(defaultStr);
        mutation.setScore(defaultStr);
        mutation.setBamFile(defaultStr);
        mutation.setTumorAltCount(defaultInt);
        mutation.setTumorRefCount(defaultInt);
        mutation.setNormalAltCount(defaultInt);
        mutation.setNormalRefCount(defaultInt);
        mutation.setOncotatorDbSnpRs(defaultStr);
        mutation.setOncotatorCodonChange(defaultStr);
        mutation.setOncotatorRefseqMrnaId(defaultStr);
        mutation.setOncotatorUniprotName(defaultStr);
        mutation.setOncotatorUniprotAccession(defaultStr);
        mutation.setOncotatorProteinPosStart(defaultInt);
        mutation.setOncotatorProteinPosEnd(defaultInt);
        mutation.setCanonicalTranscript(true);

        return mutation;
    }

    private static Map<String, Integer> annotateProteinChange(String proteinChange) {
        int start = -1;
        int end = -1;
        Map<String, Integer> annotation = new HashMap<>();
        annotation.put("start", start);
        annotation.put("end", end);

        if (proteinChange == null) {
            return annotation;
        }

        if (proteinChange.startsWith("p.")) {
            proteinChange = proteinChange.substring(2);
        }

        if (proteinChange.indexOf("[") != -1) {
            proteinChange = proteinChange.substring(0, proteinChange.indexOf("["));
        }

        proteinChange = proteinChange.trim();

        Pattern p = Pattern.compile("^([A-Z\\*]+)([0-9]+)([A-Z\\*\\?]*)$");
        Matcher m = p.matcher(proteinChange);
        if (m.matches()) {
            String ref = m.group(1);
            String var = m.group(3);
            Integer refL = ref.length();

            start = Integer.valueOf(m.group(2));

            if (ref.equals(var)) {
                end = start;
            } else if (ref.equals("*")) {
                end = start;
            } else if (var.equals("*")) {
                end = start;
            } else if (start == 1) {
                end = start;
            } else if (var.equals("?")) {
                end = start;
            } else {
                end = start + refL - 1;
            }
        } else {
            p = Pattern.compile("[A-Z]?([0-9]+)(_[A-Z]?([0-9]+))?(delins|ins)([A-Z]+)");
            m = p.matcher(proteinChange);
            if (m.matches()) {
                start = Integer.valueOf(m.group(1));
                if (m.group(3) != null) {
                    end = Integer.valueOf(m.group(3));
                } else {
                    end = start;
                }
            } else {
                p = Pattern.compile("[A-Z]?([0-9]+)(_[A-Z]?([0-9]+))?(_)?splice");
                m = p.matcher(proteinChange);
                if (m.matches()) {
                    start = Integer.valueOf(m.group(1));
                    if (m.group(3) != null) {
                        end = Integer.valueOf(m.group(3));
                    } else {
                        end = start;
                    }
                } else {
                    p = Pattern.compile("[A-Z]?([0-9]+)_[A-Z]?([0-9]+)(.+)");
                    m = p.matcher(proteinChange);
                    if (m.matches()) {
                        start = Integer.valueOf(m.group(1));
                        end = Integer.valueOf(m.group(2));
                    } else {
                        // Check for frameshift variant
                        p = Pattern.compile("([A-Z\\*])([0-9]+)[A-Z]?fs.*");
                        m = p.matcher(proteinChange);
                        if (m.matches()) {
                            start = Integer.valueOf(m.group(2));
                            end = start;
                        } else {
                            // Check for inframe insertion, deletion or duplication
                            p = Pattern.compile("([A-Z]+)?([0-9]+)((ins)|(del)|(dup))");
                            m = p.matcher(proteinChange);
                            if (m.matches()) {
                                start = Integer.valueOf(m.group(2));
                                end = start;
                            }
                        }
                    }
                }
            }
        }

        annotation.put("start", start);
        annotation.put("end", end);
        return annotation;
    }
}
