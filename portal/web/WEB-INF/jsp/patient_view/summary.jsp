

<script type="text/javascript">
    $(document).ready(function(){
        $('#mutation_summary_wrapper_table').hide();
        $('#cna_summary_wrapper_table').hide();
        initGenomicsOverview();
    });

    function initGenomicsOverview() {
        var width = 1200;
        var height = 100;
        var paper = createRaphaelCanvas("genomics-overview", width, height);
        plotChromosomes(paper, width);
    }
</script>


<%if(showPlaceHoder){%>
<br/>Clinical timeline goes here...
<br/><br/>
<%}%>

<%if(showPlaceHoder){%>
<br/>Genomic overview image goes here... (below is a mockup)<br/>
<img src="http://cbio.mskcc.org/~jgao/genomic-over-mockup.png">
<br/>
<%}%>

<div id="genomics-overview"></div>
<br/>
        
<%if(showMutations){%>
<br/>
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
<br/>
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

<%if(showPlaceHoder){%>
<br/>What else???
<br/><br/>
<%}%>