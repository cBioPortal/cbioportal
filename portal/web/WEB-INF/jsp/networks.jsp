<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<%
    String genes4Network = StringUtils.join((List)request.getAttribute(QueryBuilder.GENE_LIST)," ");
    String geneticProfileIds4Network = StringUtils.join(geneticProfileIdSet," ");
    String cancerTypeId4Network = (String)request.getAttribute(QueryBuilder.CANCER_STUDY_ID);
    String caseIds4Network = (String)request.getAttribute(QueryBuilder.CASE_IDS);
    String zScoreThesholdStr4Network = request.getParameter(QueryBuilder.Z_SCORE_THRESHOLD);
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
                        labelVerticalAnchor: "bottom",
						//labelXOffset: 0,
						//labelYOffset: -10,
                        labelFontSize: 10
                    },
                    edges: {
                        width: 1,
                        // color: "#0B94B1"
       					// TODO 3(or 4?) different colors for different each types
						color: {
							discreteMapper: {
								attrName: "type",
								entries: [
									{ attrValue: "IN_SAME_COMPONENT", value: "#C3844C" },
									{ attrValue: "REACTS_WITH", value: "#6261FC" },
									{ attrValue: "STATE_CHANGE", value: "#68BBC1" } ]
        					}
        				}
					}
				};

                // initialization options
                var options = {
                    swfPath: "swf/CytoscapeWeb",
                    flashInstallerPath: "swf/playerProductInstall"
                };

                var vis = new org.cytoscapeweb.Visualization(div_id, options);

                vis.ready(function() {
                	// init UI of the network tab
                	initNetworkUI(vis);
                	
                    // set the style programmatically
                    document.getElementById("color").onclick = function(){
                        vis.visualStyle(visual_style);
                    };
                });

                var draw_options = {
                    // your data goes here
                    network: graphml,
                    edgeLabelsVisible: false,
                    edgesMerged: true,
                    layout: "ForceDirected",
                    visualStyle: visual_style,
                    panZoomControlVisible: true
                };

                vis.draw(draw_options);
            };
            
            window.onload = function() {
                //send2cytoscapeweb(graphml);
                //(new XMLSerializer()).serializeToString(graphml)
                $("div.cytoscapeweb_menu").hide();
                $.post("network.do", 
                    {<%=QueryBuilder.GENE_LIST%>:'<%=genes4Network%>',
                     <%=QueryBuilder.GENETIC_PROFILE_IDS%>:'<%=geneticProfileIds4Network%>',
                     <%=QueryBuilder.CANCER_STUDY_ID%>:'<%=cancerTypeId4Network%>',
                     <%=QueryBuilder.CASE_IDS%>:'<%=caseIds4Network%>',
                     <%=QueryBuilder.Z_SCORE_THRESHOLD%>:'<%=zScoreThesholdStr4Network%>'
                    },
                    function(graphml){
                        if (typeof data !== "string") { 
                            if (window.ActiveXObject) { // IE 
                                    graphml = graphml.xml; 
                            } else { // Other browsers 
                                    graphml = (new XMLSerializer()).serializeToString(graphml); 
                            } 
                        } 
                        //$("p#networktest").html(graphml);
                        $("div.cytoscapeweb_menu").show();
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
					<div class="cytoscapeweb_menu">
					    <jsp:include page="network_menu.jsp"/>
					</div>
					<div id="cytoscapeweb">
						<p>
						    <font size="5">Please wait while the network is being retrieved...</font>
						</p>
					</div>
				</div>
			</td>
			<td>
				<div class="cytoscapeweb_menu">
				    <jsp:include page="network_tabs.jsp"/>
				</div>
			</td>
		</tr>
	</table>
</div>
                        
                        <!--p id="networktest"></p-->