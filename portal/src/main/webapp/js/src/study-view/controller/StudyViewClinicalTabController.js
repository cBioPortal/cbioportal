
/*
 * This class is designed to control the logic for Clinial Tab in Study View
 * 
 * @autor Hongxin Zhang
 * 
 */


var StudyViewClinicalTabController = (function(){
    function init(){
        StudyViewInitClinicalTab.init(
            'clinical_table',
            'clinical-data-table-div',
            {
                "arr": StudyViewProxy.getArrData(),
                "attr": StudyViewProxy.getAttrData()
            });
    }
    
    return {
        init: init
    };
})();