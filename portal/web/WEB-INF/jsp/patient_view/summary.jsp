<%@ page import="org.mskcc.portal.servlet.CnaJSON" %>

<script type="text/javascript">
    $(document).ready(function(){
        $('#mutation_summary_wrapper_table').hide();
        $('#cna_summary_wrapper_table').hide();
        initGenomicsOverview();
    });

    function initGenomicsOverview() {
        var chmInfo = new ChmInfo();
        var config = new GenomicOverviewConfig((geObs.hasMut?1:0)+(geObs.hasCna?1:0));
        var paper = createRaphaelCanvas("genomics-overview", config);
        plotChromosomes(paper,config,chmInfo);
        if (geObs.hasMut) {
            geObs.subscribeMut(function(){
                var muts = $('#mutation_table').dataTable().fnGetData();
                plotMuts(paper,config,chmInfo,geObs.hasCna?1:0,muts,mutTableIndices['chr'],mutTableIndices['start'],mutTableIndices['end']);
            });
        }
        
        if (geObs.hasCna) {
            plotCopyNumberOverview(paper,config,chmInfo);
        }
    }
    
    function plotCopyNumberOverview(paper,config,chmInfo) {
        var params = {
            <%=CnaJSON.CMD%>:'<%=CnaJSON.GET_SEGMENT_CMD%>',
            <%=PatientView.PATIENT_ID%>:'<%=patient%>'
        };

        $.post("cna.json", 
            params,
            function(segs){
                plotCnSegs(paper,config,chmInfo,0,segs,1,2,3,5);
            }
            ,"json"
        );
    }
</script>


<%if(showPlaceHoder){%>
<br/>Clinical timeline goes here...
<br/><br/>
<%}%>

<%if(showGenomicOverview){%>
<div id="genomics-overview"></div>
<br/>
<%}%>
        
<%if(showMutations){%>
<div id="mutation_summary_wait"><img src="images/ajax-loader.gif"/> Loading mutations ...</div>
<table cellpadding="0" cellspacing="0" border="0" id="mutation_summary_wrapper_table" width="100%">
    <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="mutation_summary_table">
                <%@ include file="mutations_table_template.jsp"%>
            </table>
        </td>
    </tr>
</table>
<br/>
<%}%>

<%if(showCNA){%>
<div id="cna_summary_wait"><img src="images/ajax-loader.gif"/> Loading copy number alterations ...</div>
<table cellpadding="0" cellspacing="0" border="0" id="cna_summary_wrapper_table" width="100%">
    <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="cna_summary_table">
                <%@ include file="cna_table_template.jsp"%>
            </table>
        </td>
    </tr>
</table>
<br/>
<%}%>
