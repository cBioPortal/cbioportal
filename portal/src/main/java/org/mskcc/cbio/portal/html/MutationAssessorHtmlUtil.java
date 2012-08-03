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
                return HtmlUtil.createLink(urlPdb, "Structure");
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
                return HtmlUtil.createLink(urlMsa, "Alignment");
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
        omaImpactWordMap.put("H", "High");
        omaImpactWordMap.put("M", "Medium");
        omaImpactWordMap.put("L", "Low");
        omaImpactWordMap.put("N", "Neutral");
    }
}