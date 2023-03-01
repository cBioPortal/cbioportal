/*
 * Copyright (c) 2015 - 2016 Memorial Sloan-Kettering Cancer Center.
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

import java.util.ArrayList;
import java.util.HashSet;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.CallOncoPrintSpecParser;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.GeneticDataTypes;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.OncoPrintGeneDisplaySpec;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.OncoPrintLangException;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.OncoPrintSpecification;
import org.mskcc.cbio.portal.oncoPrintSpecLanguage.ParserOutput;
import org.mskcc.cbio.portal.model.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;

public class OncoPrintSpecificationDriver {

    /**
     * A wrapper for the OncoPrintSpec parser, which includes the list of genetic
     * data types input in the user's checkboxes. Performs two complementary
     * functions:
     * <ul>
     * <LI>LimitLanguageDefaultToProfile: Limit the specification returned by the
     * language to the genetic data types that the user selects in the
     * checkboxes.
     * <LI>LanguageCannotOverrunProfile: If the user explicitly selects a data
     * type in the language, but does not choose it in the checkboxes, add an
     * error.
     * </ul>
     * <p>
     *
     * @param geneListStr
     *           an OncoPrintSpec specification
     * @param geneticProfileIdSet
     *           profile strings entered by the user
     * @param profileList
     *           all profiles for the selected cancer type
     * @return output from parsing geneListStr
     */
    static public ParserOutput callOncoPrintSpecParserDriver(String geneListStr, HashSet<String> geneticProfileIdSet,
            ArrayList<GeneticProfile> profileList, double zScoreThreshold, double rppaScoreThreshold) {
        // I. LimitLanguageDefaultToProfile: Create an OncoPrintGeneDisplaySpec
        // from the geneticProfileIdSet, and use that as the default for the parse
        // make an OncoPrintGeneDisplaySpec, from geneticProfileIdSet, profileList and zScoreThreshold
        OncoPrintGeneDisplaySpec checkboxInputOncoPrintGeneDisplaySpec = new OncoPrintGeneDisplaySpec();
        for (GeneticProfile geneticProfile : profileList) {
            if (geneticProfileIdSet.contains(geneticProfile.getStableId())) {
                if (geneticProfile.getGeneticAlterationType() == GeneticAlterationType.COPY_NUMBER_ALTERATION) {
                    checkboxInputOncoPrintGeneDisplaySpec.setDefault( GeneticDataTypes.CopyNumberAlteration );
                }
                if (geneticProfile.getGeneticAlterationType() == GeneticAlterationType.MRNA_EXPRESSION) {
                    checkboxInputOncoPrintGeneDisplaySpec.setDefaultExpression(zScoreThreshold, GeneticDataTypes.Expression);
                }
                if (geneticProfile.getGeneticAlterationType() == GeneticAlterationType.PROTEIN_LEVEL) {
                    checkboxInputOncoPrintGeneDisplaySpec.setDefaultExpression(rppaScoreThreshold, GeneticDataTypes.RPPA);
                }
                if (geneticProfile.getGeneticAlterationType() == GeneticAlterationType.PROTEIN_ARRAY_PROTEIN_LEVEL) {
                    checkboxInputOncoPrintGeneDisplaySpec.setDefaultExpression(rppaScoreThreshold, GeneticDataTypes.RPPA);
                }
                if (geneticProfile.getGeneticAlterationType() == GeneticAlterationType.MUTATION_EXTENDED) {
                    checkboxInputOncoPrintGeneDisplaySpec.setDefault( GeneticDataTypes.Mutation );
                }
            }
        }
        ParserOutput theOncoPrintSpecParserOutput = CallOncoPrintSpecParser.callOncoPrintSpecParser(geneListStr, checkboxInputOncoPrintGeneDisplaySpec);
        // II. LanguageCannotOverrunProfile: If the OncoSpec asked for data types
        // not in the geneticProfileIdSet, add semantics error(s)
        OncoPrintSpecification anOncoPrintSpecification = theOncoPrintSpecParserOutput.getTheOncoPrintSpecification();
        ArrayList<OncoPrintLangException> theSemanticsErrors = theOncoPrintSpecParserOutput.getSemanticsErrors();
        OncoPrintGeneDisplaySpec unionOncoPrintGeneDisplaySpec = anOncoPrintSpecification.getUnionOfPossibleLevels();
        for (GeneticDataTypes aGeneticDataType : GeneticDataTypes.values()) {
            if (unionOncoPrintGeneDisplaySpec.typeDifference(checkboxInputOncoPrintGeneDisplaySpec, aGeneticDataType)) {
                theSemanticsErrors.add(new OncoPrintLangException("Error: " +  aGeneticDataType +
                        " specified in the list of genes, but not selected in the Genetic Profile Checkboxes."));
            }
        }
        return theOncoPrintSpecParserOutput;
    }

    /**
     * A wrapper for the OncoPrintSpec parser, which includes the list of genetic
     * data types input in the user's checkboxes. Performs two complementary
     * functions:
     * <ul>
     * <LI>LimitLanguageDefaultToProfile: Limit the specification returned by the
     * language to the genetic data types that the user selects in the
     * checkboxes.
     * <LI>LanguageCannotOverrunProfile: If the user explicitly selects a data
     * type in the language, but does not choose it in the checkboxes, add an
     * error.
     * </ul>
     * <p>
     *
     * @param geneListStr an OncoPrintSpec specification
     * @return output from parsing geneListStr
     */
    static public ParserOutput callOncoPrintSpecParserDriver(String geneListStr) {
        OncoPrintGeneDisplaySpec checkboxInputOncoPrintGeneDisplaySpec = new OncoPrintGeneDisplaySpec();
        ParserOutput theOncoPrintSpecParserOutput = CallOncoPrintSpecParser.callOncoPrintSpecParser(geneListStr, checkboxInputOncoPrintGeneDisplaySpec);
        // II. LanguageCannotOverrunProfile: If the OncoSpec asked for data types
        // not in the geneticProfileIdSet, add semantics error(s)
        OncoPrintSpecification anOncoPrintSpecification = theOncoPrintSpecParserOutput.getTheOncoPrintSpecification();
        ArrayList<OncoPrintLangException> theSemanticsErrors = theOncoPrintSpecParserOutput.getSemanticsErrors();
        OncoPrintGeneDisplaySpec unionOncoPrintGeneDisplaySpec = anOncoPrintSpecification.getUnionOfPossibleLevels();
        for (GeneticDataTypes aGeneticDataType : GeneticDataTypes.values()) {
            if (unionOncoPrintGeneDisplaySpec.typeDifference(checkboxInputOncoPrintGeneDisplaySpec, aGeneticDataType)) {
                theSemanticsErrors.add( new OncoPrintLangException("Error: " +  aGeneticDataType +
                        " specified in the list of genes, but not selected in the Genetic Profile Checkboxes."));
            }
        }
        return theOncoPrintSpecParserOutput;
    }
}
