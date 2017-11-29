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

<%--
 - This is the main page for loading the pan-cancer study view,
 - where "pan-cancer study" = a study comprising more than 1 cancer type.
 --%>


<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<!-- The pancer_study_summary files: -->
<link href="css/pancancer_study_summary/pancancer_study_summary.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<%@ include file="pancancer_study_summary/pancancer_study_summary_templates.html" %>
<!-- js files: -->
<script type="text/javascript" src="js/src/pancancer_study_summary/pancancer_study_summary.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/pancancer_study_summary/pancancer_study_summary_histogram.js?<%=GlobalProperties.getAppVersion()%>"></script>


<div class="section cbioportal-frontend" id="pancancer_study_summary"><!-- this div id "pancancer_study_summary" is referenced back in visualize.jsp -->
	<!-- main container -->
</div>

<!-- Initialization script -->
<script>
	// This is for the moustache-like templates
	// prevents collisions with JSP tags
	_.templateSettings = {
	    interpolate : /\{\{(.+?)\}\}/g
	};

	//Initialize the pancancer study summary object which triggers the creation of the sub tabs,
	//models and respective views, one per gene:
    $(document).ready( function() {
    	//whether this tab has already been initialized or not:
    	var tab_init = false;
    	//function that will listen to tab changes and init this one when applicable:
    	function tabsUpdate() {
	        if ($("#pancancer_study_summary").is(":visible")) {
		    	if (tab_init === false) {
                   window.onReactAppReady(function(){
                                           window.renderCancerTypeSummary(document.getElementById('pancancer_study_summary'));
                                       });
                    tab_init = true;
		    		<%--var pancancerStudySummary = new PancancerStudySummary();--%>
                    <%--pancancerStudySummary.init();--%>
		            <%--tab_init = true;--%>
		            <%--console.log("pancancer_study_summary tab initialized");--%>
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
	
</script>

