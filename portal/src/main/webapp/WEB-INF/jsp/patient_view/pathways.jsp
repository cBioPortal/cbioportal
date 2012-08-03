<%
request.setAttribute("include_network_help_tab", Boolean.FALSE);
request.setAttribute("include_network_legend", Boolean.FALSE);
%>
<link href="css/network/jquery-ui-1.8.14.custom.css" type="text/css" rel="stylesheet"/>
<link href="css/network/network_ui.css" type="text/css" rel="stylesheet"/>

<script type="text/javascript" src="js/cytoscape_web/AC_OETags.min.js"></script>
<script type="text/javascript" src="js/cytoscape_web/cytoscapeweb.min.js"></script>

<script type="text/javascript" src="js/network/jquery-ui-1.8.14.custom.min.js"></script>
<script type="text/javascript" src="js/network/network-ui.js"></script>
<script type="text/javascript" src="js/network/network-viz.js"></script>

<script type="text/javascript">
    function buildCytoscapeWeb() {
        var genes = [];
        if (overviewCnaGenes)
            genes.push(overviewCnaGenes);
        if (overviewMutGenes)
            genes.push(overviewMutGenes);
        
        var networkParams = {<%=org.mskcc.cbio.portal.servlet.QueryBuilder.GENE_LIST%>:genes.join(','),
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
                
                $("#network-resubmit-query").remove();
                $("#slider_area").remove();
                $('select#drop_down_select>option:eq(2)').attr('selected', true);
            }
        );
    }
    
    $(document).ready(function(){
        geObs.subscribeMutCna(buildCytoscapeWeb);
    }
    );
</script>

<jsp:include page="network_div.jsp"/>