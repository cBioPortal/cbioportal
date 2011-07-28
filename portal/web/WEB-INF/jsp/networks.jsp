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
                        backgroundColor: "#fefefe" //#F7F6C9 //#F3F7FE
                    },
                    nodes: {
						shape: {
						   discreteMapper: {
								attrName: "type",
								entries: [
									{ attrValue: "Protein", value: "ELLIPSE" },
									{ attrValue: "SmallMolecule", value: "DIAMOND" },
									{ attrValue: "Unknown", value: "TRIANGLE" }
								]
							}
						},
                        borderWidth: 1,
						borderColor: {
							discreteMapper: {
								attrName: "type",
								entries: [
									{ attrValue: "Protein", value: "#000000" },
									{ attrValue: "SmallMolecule", value: "#000000" },
									{ attrValue: "Unknown", value: "#000000" }
								]
							}
						},
                        size: {
                            defaultValue: 25,
                            continuousMapper: { attrName: "weight", minValue: 25, maxValue: 75 }
                        },
                        color: {
                            discreteMapper: {
                                attrName: "type",
                                entries: [
									{ attrValue: "Protein", value: "#FFFFFF" },
									{ attrValue: "SmallMolecule", value: "#FFFFFF" }, //#D7AC85
									{ attrValue: "Unknown", value: "#FFFFFF" } //#69A19E
                                ]
                            }
                        },
                        labelHorizontalAnchor: "center",
                        labelVerticalAnchor: "bottom",
                        labelFontSize: 10,
                        selectionGlowColor: "#f6f779",
						selectionGlowOpacity: 0.8,
						hoverGlowColor: "#cbcbcb", //#ffff33
						hoverGlowOpacity: 1.0,
						hoverGlowStrength: 8
                    },
                    edges: {
                        width: 1,
						mergeWidth: 2,
						color: {
							discreteMapper: {
								attrName: "type",
								entries: [
									{ attrValue: "IN_SAME_COMPONENT", value: "#CD976B" },
									{ attrValue: "REACTS_WITH", value: "#7B7EF7" },
									{ attrValue: "STATE_CHANGE", value: "#67C1A9" } ]
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
                        $("div.cytoscapeweb_menu").show();
                        send2cytoscapeweb(graphml);
                    }
                );
            }
        </script>

<div class="section" id="network">
	<table>
		<tr><td>
			<div class="cytoscapeweb_menu">
				<jsp:include page="network_menu.jsp"/>
			</div>
		</td></tr>
		<tr>
			<td id="vis_content">
				<div id="cytoscapeweb">
					<img src="images/ajax-loader.gif"/>
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