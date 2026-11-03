package org.mskcc.cbio.portal.validate;

import org.mskcc.cbio.portal.util.CaseList;

public class CaseListValidator {

    /**
     * Fields that are used during case list update
     * @param caseList
     */
    public static void validateIdFields(CaseList caseList) {
        if (caseList.getStableId() == null) {
            throw new IllegalArgumentException("stable id is not specified.");
        }
        if (caseList.getStableId().matches(".*\\s.*")) {
            throw new IllegalArgumentException(String.format("stable id cannot contain white space(s): '%s'", caseList.getStableId()));
        }
        if (caseList.getCancerStudyIdentifier() == null) {
            throw new IllegalArgumentException("cancer study identifier is not specified.");
        }
        if (caseList.getCancerStudyIdentifier().matches(".*\\s.*")) {
            throw new IllegalArgumentException(String.format("cancer study identifier cannot contain white space(s): '%s'", caseList.getStableId()));
        }
        if (caseList.getSampleIds() == null || caseList.getSampleIds().isEmpty()) {
            throw new IllegalArgumentException("sample ids are not specified.");
        }
    }

    /**
     * Fields that are used during case list creation
     * @param caseList
     */
    public static void validateDescriptionFields(CaseList caseList) {
        if (caseList.getName() == null) {
            throw new IllegalArgumentException("case list name is not specified.");
        }
        if (caseList.getDescription() == null) {
            throw new IllegalArgumentException("case list description is not specified.");
        }
    }

    public static void validateAll(CaseList caseList) {
        validateIdFields(caseList);
        validateDescriptionFields(caseList);
    }


}
