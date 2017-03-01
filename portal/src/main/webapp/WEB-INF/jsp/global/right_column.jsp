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

<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<%@ page import="org.mskcc.cbio.portal.util.DataSetsUtil" %> 
<%@ page import="org.mskcc.cbio.portal.model.CancerStudyStats" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>



<div id="right_side">

    <!-- replaced the hard-coded what's new with a call to GlobalProperties
    <!-- Should the "What's New" itself stay? -->
    <div id="whats-new" class="sidebar-box">
        <h3>What's New</h3>
        <%= GlobalProperties.getRightNavWhatsNewBlurb() %>
    </div>


    <% if (GlobalProperties.showRightNavDataSets()) { %>
        <div class="sidebar-box">
            <h3>Data Sets</h3>
            <p id="portal_data_stats_copy"></p>
            <div id='rightmenu-stats-box'></div>
            <script type="text/javascript">
    		    $(document).ready( function() {
                        var plotTree = function(json) {
                            var totalNumSamples = Object.keys(json.cancer_studies).map(function(x) { 
                                return (x === 'all' ? 0 : json.cancer_studies[x].num_samples);
                            }).reduce(function(acc, currVal) {
                                return acc+currVal;
                            }, 0);
                            var numStudies = Object.keys(json.cancer_studies).length - 1; // subtract one for the cross-cancer "study"
                            $("#portal_data_stats_copy").html("The Portal contains <b>" + 
				    numStudies + " cancer studies.</b> [<a href='data_sets.jsp'>Details</a>]</p>");
                            RightMenuStudyStatsUtil.plotTree(json);
			};
                        if (window.metaDataPromise) {
                            window.metaDataPromise.then(plotTree);
                        } else {
                            $.getJSON("portal_meta_data.json?partial_studies=true&partial_genesets=true", plotTree);
                        }
    		        });
    	    </script>
        </div>
    <% } %>

<% if (GlobalProperties.showRightNavExamples()) {%>
    <div class="sidebar-box">
        <h3>Example Queries</h3>
        <%@ include file="/content/examples.html" %>
    </div>
<% } %>

<% if (GlobalProperties.showRightNavTestimonials()) {%>
    <div class="sidebar-box">
        <div id="rotating_testimonials">
            <h3>What People are Saying</h3>
            <jsp:include page="../testimonials.jsp" flush="true" />
        </div>
    </div>
<% } %>
</div>
