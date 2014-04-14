
/*
 * This class is designed to control the logic for Clinial Tab in Study View
 * 
 * @autor Hongxin Zhang
 * 
 */


var StudyViewClinicalTabController = (function(){
    
    function init(){
        
        StudyViewInitClinicalTab
                .init(
                    'clinical_table',
                    StudyViewInitDataTable.getDataTable().getTableHeader(),
                    StudyViewInitDataTable.getDataTable().getTableContent()
                );
    }
    
    return {
        init: init
    };
})();