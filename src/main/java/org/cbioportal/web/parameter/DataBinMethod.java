package org.cbioportal.web.parameter;

/**
 *  STATIC and DYNAMIC binning relate to the way bin boundaries are derived from numerical data of a sample cohort
 *  defined by a Study View Filter (SVF).
 *  
 *  When binning in STATIC mode, the binning ignores any filter in the SVF on the attribute that is the subject of 
 *  binning. In effect, it bins all values of the cohort. This mode is important to drive frontend behavior where
 *  histograms show bins related to the entire data range, but highlight the currently selected/filtered data in blue 
 *  (non-selected data bins in grey). In effect, this way the non-selected data influences the boundaries of the data
 *  bins, irrespective of the selected attribute range. 
 *  
 *  When binning in DYNAMIC mode, only the attribute data is binned that is defined by the SVF. The non-selected 
 *  rest of the data does not influence the bin boundaries.
 */
public enum DataBinMethod {
    STATIC,
    DYNAMIC
}
