
<%@ page import="org.mskcc.cbio.portal.servlet.MutSigJSON" %>
<%@ page import="org.mskcc.cbio.portal.util.SkinUtil" %>

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
                    allowHtml: true,
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
                var value = mutSigData[i][headers[j]];
                if (headers[j]==='gene_symbol')
                    var value = '<a href="<%=SkinUtil.getCbioPortalUrl()%>index.do?Action=Submit&genetic_profile_ids='
                            +mutationProfileId+'&case_set_id='+studyId+'_all&cancer_study_id='+studyId
                            +'&gene_list='+value+'&tab_index=tab_visualize&#mutation_details">'+value+'</a>';
                row.push(value);
            }
            ret.push(row);
        }
        return ret;
    }
</script>

<div id="mut-sig-msg"><img src="images/ajax-loader.gif"/></div><br/>
<div id="mut-sig-div"></div>