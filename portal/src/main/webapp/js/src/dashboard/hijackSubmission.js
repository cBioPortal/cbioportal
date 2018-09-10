
function handleLegacySubmission(url, query){

    query.cancer_study_list = query.cancer_study_list.join(',');
    
    const fixedQuery = JSON.stringify(query);
    
    try {
        localStorage.setItem("legacyStudySubmission", fixedQuery);
    } catch (ex){ // probably because too much in localstorage
        localStorage.clear();
        localStorage.setItem("legacyStudySubmission", fixedQuery);
    }
   
    if (query.Action) {
        
        window.open(window.location.protocol + "//" +  window.frontendConfig.baseUrl + "/results/legacy_submission");
        
    } else {
        
        window.location.href = "//" + window.frontendConfig.baseUrl;
    
    }
    
}


$(document).ready(function(){

    function submitForm(){
        handleLegacySubmission.apply(this,arguments);
    };

    if (window.QueryByGeneUtil && window.QueryByGeneUtil.query) {
        eval("window.QueryByGeneUtil.query =" + window.QueryByGeneUtil.query.toString());
    }
    
});

// proxy window.open to fix patient cohort linking 
var oldOpen = window.open;
window.open = function(url){
    if (/navCaseIds=/.test(url)) {
        const fixedUrl = url.replace(/case.do#\//,"");
        oldOpen(fixedUrl);
    } else {
        oldOpen.apply(this,arguments);
    }
};