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

package org.mskcc.cbio.portal.html;

import org.apache.log4j.Logger;
import org.mskcc.cbio.cgds.model.ExtendedMutation;
import org.mskcc.cbio.portal.util.OmaLinkUtil;

import java.net.MalformedURLException;
import java.util.HashMap;

/**
 * Utility Class for Creating Links to the Online Mutation Assessor (OMA).
 *
 * @author Ethan Cerami.
 */
public class MutationAssessorHtmlUtil {
    private static Logger logger = Logger.getLogger(MutationAssessorHtmlUtil.class);
    private HashMap<String, String> omaCssStyleMap = new HashMap<String, String>();
    private HashMap<String, String> omaImpactWordMap = new HashMap<String, String>();
    private ExtendedMutation mutation;
    private static final String NA = "NA";
    private static final String OMA_LINK_BASE_STYLE = "oma_link";
    private String functionalImpactScoreKeyword;
	private static final String STRUCTURE_IMG = "<img border='0' src='images/mutation/pdb.png'>";
	private static final String ALIGNMENT_IMG = "<img border='0' src='images/mutation/msa.png'>";

    public MutationAssessorHtmlUtil(ExtendedMutation mutation) {
        this.mutation = mutation;
        initOmaCssStyleMap();
        initOmaImpactWordMap();
        functionalImpactScoreKeyword = mutation.getFunctionalImpactScore();
    }

    public String getOmaImpactCssStyle() {
        return omaCssStyleMap.get(functionalImpactScoreKeyword);
    }

    public String getOmaImpactWord() {
        return omaImpactWordMap.get(functionalImpactScoreKeyword);
    }

    //  Create Link to PDB Structure.  A safe spacer is returned if any errors/exceptions occur.
    public String getPdbStructureLink() {
        if (linkIsValid(mutation.getLinkPdb())) {
            try {
                String urlPdb = OmaLinkUtil.createOmaRedirectLink(mutation.getLinkPdb());
                return HtmlUtil.createLink(urlPdb, STRUCTURE_IMG);
            } catch (MalformedURLException e) {
                logger.error("Could not parse OMA URL:  " + e.getMessage());
                return HtmlUtil.createEmptySpacer();
            }
        } else {
            return HtmlUtil.createEmptySpacer();
        }
    }

    //  Create Link to MulitpleS Sequence Alignment.
    //  A safe spacer is returned if any errors/exceptions occur.
    public String getMultipleSequenceAlignmentLink() {
        if (linkIsValid(mutation.getLinkMsa())) {
            try {
                String urlMsa = OmaLinkUtil.createOmaRedirectLink(mutation.getLinkMsa());
                return HtmlUtil.createLink(urlMsa, ALIGNMENT_IMG);
            } catch (MalformedURLException e) {
                logger.error("Could not parse OMA URL:  " + e.getMessage());
                return HtmlUtil.createEmptySpacer();
            }
        } else {
            return HtmlUtil.createEmptySpacer();
        }
    }

    //  Create Link to Functional Impact Score.
    //  A safe spacer is returned if any errors/exceptions occur.
    public String getFunctionalImpactLink() {
        try {
            String impactStyle = getOmaImpactCssStyle();
            String impactWord = getOmaImpactWord();
            if (impactStyle != null && impactWord != null) {
                return createFunctionalImpactLink(impactStyle, impactWord);
            } else {
                logger.error("Could not parse OMA Functional Impact Score Keyword:  "
                        + functionalImpactScoreKeyword);
                return HtmlUtil.createEmptySpacer();
            }
        } catch (MalformedURLException e) {
            logger.error("Could not parse OMA URL:  " + e.getMessage());
            return HtmlUtil.createEmptySpacer();
        }
    }

    private String createFunctionalImpactLink(String impactStyle, String impactWord)
            throws MalformedURLException {
        if (linkIsValid(mutation.getLinkXVar())) {
            String xVarLink = OmaLinkUtil.createOmaRedirectLink(mutation.getLinkXVar());
            return HtmlUtil.createLinkWithinSpan(xVarLink, impactWord,
                OMA_LINK_BASE_STYLE, impactStyle);
        } else {
            return HtmlUtil.createLinkWithinSpan(impactWord, impactStyle);
        }
    }

    private boolean linkIsValid(String link) {
        if (link != null && link.length() > 0 && !link.equalsIgnoreCase(NA)) {
            return true;
        } else {
            return false;
        }
    }

    private void initOmaCssStyleMap() {
        //  Map between OMA Keywords, and CSS Styles
        omaCssStyleMap.put("H", "oma_high");
        omaCssStyleMap.put("M", "oma_medium");
        omaCssStyleMap.put("L", "oma_low");
        omaCssStyleMap.put("N", "oma_neutral");
    }

    private void initOmaImpactWordMap() {
        //  Map between OMA Keywords, and Words to Display to End-User
        omaImpactWordMap.put("H", "H");
        omaImpactWordMap.put("M", "M");
        omaImpactWordMap.put("L", "L");
        omaImpactWordMap.put("N", "N");
    }
}