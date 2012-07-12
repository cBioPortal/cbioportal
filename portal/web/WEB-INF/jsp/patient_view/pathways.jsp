
<script type="text/javascript">
    function buildCytoscapeWeb() {
        var genes = [];
        if (overviewCnaGenes)
            genes.push(overviewCnaGenes);
        if (overviewMutGenes)
            genes.push(overviewMutGenes);
        
        var networkParams = {<%=org.mskcc.portal.servlet.QueryBuilder.GENE_LIST%>:genes.join(','),
                netsize:'small'
            };
        $.post("network.do", 
            networkParams,
            function(graphml){
                if (typeof data !== "string") { 
                    if (window.ActiveXObject) { // IE 
                            graphml = graphml.xml; 
                    } else { // Other browsers 
                            graphml = (new XMLSerializer()).serializeToString(graphml); 
                    } 
                }
                send2cytoscapeweb(graphml,"cytoscapeweb");
            }
        );
    }
    
    $(document).ready(function(){
        geObs.subscribeMutCna(buildCytoscapeWeb);
    }
</script>

<table id="network_wrapper">
        <tr>
                <td id="vis_content">
                        <div id="cytoscapeweb">
                                <img src="images/ajax-loader.gif"/>
                        </div>
                </td>
        </tr>
</table>