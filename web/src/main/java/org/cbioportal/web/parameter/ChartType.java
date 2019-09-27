package org.cbioportal.web.parameter;

/**
 * This is same as ChartType defined in frontend repo
 * https://github.com/cBioPortal/cbioportal-frontend/blob/34dd97eafc73551816b8dd8b1b837e21640dad0e/src/pages/studyView/StudyViewUtils.tsx#L38
 */
public enum ChartType {

    PIE_CHART,
    BAR_CHART,
    SURVIVAL,
    TABLE,
    SCATTER,
    MUTATED_GENES_TABLE,
    CNA_GENES_TABLE,
    NONE;
}
