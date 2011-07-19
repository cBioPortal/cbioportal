<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>

<%
    String genes = geneList.replaceAll("\n","\\\\n");
%>

<link href="css/network/jquery-ui-1.8.14.custom.css" type="text/css" rel="stylesheet"/>
<link href="css/network/network_ui.css" type="text/css" rel="stylesheet"/>

<script type="text/javascript" src="js/cytoscape_web/json2.min.js"></script>
<script type="text/javascript" src="js/cytoscape_web/AC_OETags.min.js"></script>
<script type="text/javascript" src="js/cytoscape_web/cytoscapeweb.min.js"></script>

<script type="text/javascript" src="js/network/jquery-ui-1.8.14.custom.min.js"></script>
<script type="text/javascript" src="js/network/network-ui.js"></script>

<script type="text/javascript">
            function send2cytoscapeweb(graphml) {
                var div_id = "cytoscapeweb";

                var visual_style = {
                    global: {
                        backgroundColor: "#fefefe"
                    },
                    nodes: {
                        shape: "ELLIPSE",
					// TODO two different shapes for two different node types
//                        shape: {
//                           discreteMapper: {
//                                attrName: "type",
//                                entries: [
//                                    { attrValue: "type1", value: "ELLIPSE" },
//                                    { attrValue: "type2", value: "DIAMOND" }
//                                ]
//                            }
//                        }, 
                        borderWidth: 1,
                        borderColor: "#000000",
                        size: {
                            defaultValue: 25,
                            continuousMapper: { attrName: "weight", minValue: 25, maxValue: 75 }
                        },
                        color: {
                            discreteMapper: {
                                attrName: "id",
                                entries: [
                                    { attrValue: 1, value: "#0B94B1" },
                                    { attrValue: 2, value: "#9A0B0B" },
                                    { attrValue: 3, value: "#dddd00" }
                                ]
                            }
                        },
                        labelHorizontalAnchor: "center",
                        labelVerticalAnchor: "middle",
						labelXOffset: 0,
						labelYOffset: -10,
                        labelFontSize: 10
                    },
                    edges: {
                        width: 1,
                        color: "#0B94B1"
					// TODO 3(or 4?) different colors for different each types
//                        color: {
//                        discreteMapper: {
//                            attrName: "targetArrowShape",
//                            entries: [
//                                { attrValue: "T", value: "#C3844C" },
//                                { attrValue: "none", value: "#6261FC" },
//                               { attrValue: "delta", value: "#6261FC" },
//								{ attrValue: "circle", value: "#68BBC1" }
//                            ]
//                        }
//                    },
                    }
                };

                // initialization options
                var options = {
                    swfPath: "swf/CytoscapeWeb",
                    flashInstallerPath: "swf/playerProductInstall"
                };

                var vis = new org.cytoscapeweb.Visualization(div_id, options);

                vis.ready(function() {
                    // set the style programmatically
                    document.getElementById("color").onclick = function(){
                        vis.visualStyle(visual_style);
                    };
                });

                var draw_options = {
                    // your data goes here
                    network: graphml,
                    edgeLabelsVisible: false,
                    layout: "ForceDirected",
                    visualStyle: visual_style,
                    panZoomControlVisible: true
                };

                vis.draw(draw_options);

                initNetworkUI(vis);
            };
            
            window.onload = function() {
                //send2cytoscapeweb(graphml);
                //(new XMLSerializer()).serializeToString(graphml)
                $.post("network.do", {gene_list:'<%=genes%>'},
                    function(graphml){
                        if (typeof data !== "string") { 
                            if (window.ActiveXObject) { // IE 
                                    graphml = graphml.xml; 
                            } else { // Other browsers 
                                    graphml = (new XMLSerializer()).serializeToString(graphml); 
                            } 
                        } 
                        //alert(graphml);
                        send2cytoscapeweb(graphml);
                    }
                );
            }
        </script>

        
<div class="section" id="network">
	<table>
		<tr>
			<td>
				<div id="vis_content">
					<jsp:include page="network_menu.jsp"/>
		        	<div id="cytoscapeweb">
		            	Cytoscape Web will replace the contents of this div with your graph.
		        	</div>
		        </div>
			</td>
			<td>
				<jsp:include page="network_tabs.jsp"/>
			</td>
		</tr>
	</table>
</div>