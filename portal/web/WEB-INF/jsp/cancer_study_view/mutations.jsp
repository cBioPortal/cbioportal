
<%@ page import="org.mskcc.portal.servlet.MutSigJSON" %>

<style type="text/css">
#mut-sig-div {
    width: 1200px;
    overflow-x:scroll;
}
</style>

<script type="text/javascript">   
    $(document).ready(function(){
        loadMutSigData(studyId);
    });
    
    function loadMutSigData(cancerStudyId) {
        var params = {<%=MutSigJSON.SELECTED_CANCER_STUDY%>: cancerStudyId};
        $.get("MutSig.json",
            params,
            function(data){
                if (data==null || (typeof data) != (typeof []) || data.length==0) {
                    $("#mut-sig-msg").html("MutSig data is not available for this cancer study.");
                    return;
                }
                $("#mut-sig-msg").html(data.length+" significantly mutated genes were identified in <%=cancerStudy.getName()%> by <a href='https://confluence.broadinstitute.org/display/CGATools/MutSig'>MutSig</a>.");
                var json = formatMutSigJson(data);
                var dataTable = google.visualization.arrayToDataTable(json);
                var table = new google.visualization.Table(document.getElementById("mut-sig-div"));
                var options = {
                    showRowNumber: true,
                    width: 400,
                    page: 'enable',
                    pageSize: 100
                };
                table.draw(dataTable,options);
            })
    }
    
    function formatMutSigJson(mutSigData) {
        var ret = [];
        var headers = [];
        for (var h in mutSigData[0]) {
            headers.push(h);
        }
        ret.push(headers);
        
        for (var i=0; i<mutSigData.length; i++) {
            var row = [];
            for (var j=0; j<headers.length; j++) {
                row.push(mutSigData[i][headers[j]]);
            }
            ret.push(row);
        }
        return ret;
    }
</script>

<div id="mut-sig-msg"><img src="images/ajax-loader.gif"/></div><br/>
<div id="mut-sig-div"></div>