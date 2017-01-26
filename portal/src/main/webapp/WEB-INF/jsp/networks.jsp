<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 -
 - This library is distributed in the hope that it will be useful, but WITHOUT
 - ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 - FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 - is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 - obligations to provide maintenance, support, updates, enhancements or
 - modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 - liable to any party for direct, indirect, special, incidental or
 - consequential damages, including lost profits, arising out of the use of this
 - software and its documentation, even if Memorial Sloan-Kettering Cancer
 - Center has been advised of the possibility of such damage.
 --%>

<%--
 - This file is part of cBioPortal.
 -
 - cBioPortal is free software: you can redistribute it and/or modify
 - it under the terms of the GNU Affero General Public License as
 - published by the Free Software Foundation, either version 3 of the
 - License.
 -
 - This program is distributed in the hope that it will be useful,
 - but WITHOUT ANY WARRANTY; without even the implied warranty of
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 - GNU Affero General Public License for more details.
 -
 - You should have received a copy of the GNU Affero General Public License
 - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Set" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<%
    String zScoreThesholdStr4Network =
		    xssUtil.getCleanerInput(request.getAttribute(QueryBuilder.Z_SCORE_THRESHOLD).toString());
	String useXDebug = request.getParameter("xdebug");
    if (useXDebug==null)
        useXDebug = "0";
    String netSrc = request.getParameter("netsrc");
    if (netSrc==null)
        netSrc = "cgds";
    String netSize = request.getParameter("netsize");
    if (netSize==null)
        netSize = "large";
    String nLinker = request.getParameter("linkers");
    if (nLinker==null)
        nLinker = "50";
    String diffusion = request.getParameter("diffusion");
    if (diffusion==null)
        diffusion = "0";
%>

<link href="css/network/network_ui.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet"/>
<link href="css/network/cytoscape.js-panzoom.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet"/>


<script type="text/javascript" src="js/lib/json2.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/cytoscape_js/cytoscape.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/cytoscape_js/cytoscape.js-panzoom.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/cytoscape_js/layout.cose-bilkent.js?<%=GlobalProperties.getAppVersion()%>"></script>

<!-- <script type="text/javascript" src="js/src/network/network-ui.js?<%=GlobalProperties.getAppVersion()%>"></script> -->
<script type="text/javascript" src="js/src/network/network-visualization.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/network/GraphMLIO.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/network/network-viz2.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/network/cytoscape.renderer.canvas.portal-renderer.js?<%=GlobalProperties.getAppVersion()%>"></script>
<!-- for genomic data post request -->
<script type="text/javascript" src="js/lib/d3.min.js?<%=GlobalProperties.getAppVersion()%>"></script>

<script type="text/javascript">
    $(document).ready( function() {
    	//whether this tab has already been initialized or not:
    	var tab_init = false;
    	//function that will listen to tab changes and init this one when applicable:
    	function tabsUpdate() {
    		if ($("#network").is(":visible")) {
	    		if (tab_init === false) {
		        	showNetwork();
		            tab_init = true;
		        }
		        $(window).trigger("resize");
	    	}
    	}
        //this is for the scenario where the tab is open by default (as part of URL >> #tab_name at the end of URL):
    	tabsUpdate();
        //this is for the scenario where the user navigates to this tab:
        $("#tabs").bind("tabsactivate", function(event, ui) {
        	tabsUpdate();
        });
    });




			var genomicData = {};
			// Send genomic data query again
		    var geneDataQuery = {
                cancer_study_id: window.QuerySession.getCancerStudyIds()[0],
		        genes: window.QuerySession.getQueryGenes().join(" "),
		        geneticProfileIds: window.QuerySession.getGeneticProfileIds(),
		        z_score_threshold: <%=zScoreThreshold%>,
		        rppa_score_threshold: <%=rppaScoreThreshold%>
		    };

            // show messages in graphml
            function showNetworkMessage(graphml, divNetMsg) {
                var msgbegin = "<!--messages begin:";
                var ix1 = graphml.indexOf(msgbegin);
                if (ix1==-1) {
                    $(divNetMsg).hide();
                } else {
                    ix1 += msgbegin.length;
                    var ix2 = graphml.indexOf("messages end-->",ix1);
                    var msgs = $.trim(graphml.substring(ix1,ix2));
                    if (msgs) {
                        $(divNetMsg).append(msgs.replace(/\n/g,"<br/>\n"));
                    }
                }
            }

            function showXDebug(graphml) {
                if (<%=useXDebug%>) {
                    var xdebugsbegin = "<!--xdebug messages begin:";
                    var ix1 = xdebugsbegin.length+graphml.indexOf(xdebugsbegin);
                    var ix2 = graphml.indexOf("xdebug messages end-->",ix1);
                    var xdebugmsgs = $.trim(graphml.substring(ix1,ix2));
                    $("#cytoscapeweb").css('height','70%');
                    $("#vis_content").append("\n<div id='network_xdebug'>"
                        +xdebugmsgs.replace(/\n/g,"<br/>\n")+"</div>");
                }
            }

            var showNetwork = function() {
                var networkParams = {
                    <%=QueryBuilder.GENE_LIST%>:window.QuerySession.getQueryGenes().join(" "),
                     <%=QueryBuilder.GENETIC_PROFILE_IDS%>:window.QuerySession.getGeneticProfileIds().join(" "),
                     <%=QueryBuilder.CANCER_STUDY_ID%>:window.QuerySession.getCancerStudyIds()[0],
                     <%=QueryBuilder.CASE_IDS_KEY%>:window.QuerySession.getCaseIdsKey(),
                     <%=QueryBuilder.CASE_SET_ID%>:window.QuerySession.getCaseSetId(),
                     <%=QueryBuilder.Z_SCORE_THRESHOLD%>:'<%=zScoreThesholdStr4Network%>',
                     heat_map:$("#heat_map").html(),
                     xdebug:'<%=useXDebug%>',
                     netsrc:'<%=netSrc%>',
                     linkers:'<%=nLinker%>',
                     netsize:'<%=netSize%>',
                     diffusion:'<%=diffusion%>',
                    };
                // get the graphml data from the server
                $.post("network.do",
                    networkParams,
                    function(graphml){
                        var gml2jsonConverter = new GraphMLToJSon(graphml);
                        var json = gml2jsonConverter.toJSON();
                        window.networkGraphJSON = json;



                        if (typeof graphml !== "string")
                        {
                          if (window.ActiveXObject) { // IE
                                  graphml = (new XMLSerializer()).serializeToString(graphml);
                          } else { // Other browsers
                                  graphml = (new XMLSerializer()).serializeToString(graphml);
                          }
                        }

                        //show debug message !
                        showXDebug(graphml);
                        showNetworkMessage(graphml, "#network #netmsg");

                        // when the data is available call send2cytoscapeweb
                        //send2cytoscapeweb(window.networkGraphJSON, "cytoscapeweb", "network");
                    });
            }

        </script>

<jsp:include page="network_views.jsp"/>
<jsp:include page="network_div.jsp"/>
