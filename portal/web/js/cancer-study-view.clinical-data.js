
var clinicalData;
function loadClinicalData(cancerStudyId,caseSetId) {
    var params = {cmd:'getClinicalData',
                 case_set_id:caseSetId,
                 include_free_form:1};
    $.get("webservice.do",
          params,
          function(data){
              $('#summary').html("<table><tr><td>"+data.replace(/^#[^\r\n]+[\r\n]+/g,"").replace(/\tNA([\t\r\n])/g,"\t$1").replace(/\tNA([\t\r\n])/g,"\t$1").replace(/[\r\n]+/g,"</td></tr><tr><td>").replace(/\t/g,"</td><td>")+"</td></tr></table>");
              var rows = data.replace(/^#[^\r\n]+[\r\n]+/g,"").match(/[^\r\n]+/g);
              
          })
}
