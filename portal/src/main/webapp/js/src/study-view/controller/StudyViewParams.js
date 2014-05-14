/* 
 * This class store all of parameters which will be used for different tab
 * or in all tabs
 * 
 * 
 * @author: Hongxin Zhang
 * @Date: Apr. 2014
 */


var StudyViewParams = {
    //Global used parameters in StudyView
    params : {
        studyId: "",
        caseIds: "",
        cnaProfileId: "",
        mutationProfileId: "",
        caseSetId: "",
        hasMutSig: ""
    },
    
    summaryParams: {
        //This is dc charts transition duration, also set as timeout for redraw
        //special charts(survival mainly)
        transitionDuration: 600
    }, // For Summary Tab
    mutationsParams: {}, // For Mutations Tab
    clinicalParams: {}, // For Clinical Tab
    cnaParams: {} // For CNA Tab
};


