<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<%@ page import="org.mskcc.cbio.portal.util.DataSetsUtil" %> 
<%@ page import="org.mskcc.cbio.portal.model.CancerStudyStats" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>

<%
   String examplesHtml = GlobalProperties.getProperty("examples_right_column");
   if (examplesHtml == null) {
	   examplesHtml = "../../../content/examples.html";
   } else {
	   examplesHtml = "../../../content/" + examplesHtml;
   }

   DataSetsUtil dataSetsUtil = null;
   List<CancerStudyStats> cancerStudyStats = null;
   if (GlobalProperties.showRightNavDataSets()) {
	   dataSetsUtil = new DataSetsUtil();
	   try {
		   cancerStudyStats = dataSetsUtil.getCancerStudyStats();
	   }
	   catch (Exception e) {
		   cancerStudyStats = new ArrayList<CancerStudyStats>();
	   }
   }
%>


<div id="right_side">
    
    
    <h3>What's New</h3>

    <p>
    &bull;<a href="news.jsp"> <b>New data and features released</b></a><br/>
    &bull;<a href="tools.jsp"> <b>New tools released</b></a>
    </p>
    
    <form action="http://groups.google.com/group/cbioportal-news/boxsubscribe">
      &nbsp;&nbsp;&nbsp;&nbsp;<b>Sign up for low-volume email news alerts:</b></br>
      &nbsp;&nbsp;&nbsp;&nbsp;<input type="text" name="email">
      <input type="submit" name="sub" value="Subscribe">
    </form>
    
    &nbsp;&nbsp;&nbsp;&nbsp;<b>Or follow us <a href="http://www.twitter.com/cbioportal"><i>@cbioportal</i></a> on Twitter</b>
    <%
if (GlobalProperties.showRightNavDataSets()) {
%>
    <h3>Data Sets</h3>
    <p id="portal_data_stats_copy"></p>
<%
    // TODO: whats going on here? why are the numbers different?
    /*out.println("<P>The Portal contains data for <b>" + dataSetsUtil.getTotalNumberOfSamples() + " tumor samples from " +
                     cancerStudyStats.size() + " cancer studies.</b> [<a href='data_sets.jsp'>Details.</a>]</p>");*/
%>
    <div id='rightmenu-stats-box'></div>
	<script type="text/javascript">
		$(document).ready( function() {
			$.getJSON("portal_meta_data.json?partial_studies=true&partial_genesets=true", function(json) {
                            var totalNumSamples = Object.keys(json.cancer_studies).map(function(x) { 
                                return (x === 'all' ? 0 : json.cancer_studies[x].num_samples);
                            }).reduce(function(acc, currVal) {
                                return acc+currVal;
                            }, 0);
                            var numStudies = Object.keys(json.cancer_studies).length - 1; // subtract one for the cross-cancer "study"
                            $("#portal_data_stats_copy").html("The Portal contains data for <b>" + totalNumSamples + " tumor samples from " +
                                    numStudies + " cancer studies.</b> [<a href='data_sets.jsp'>Details</a>]</p>");
                            RightMenuStudyStatsUtil.plotTree(json);
			});
		});
	</script>
<%
    } // if showRightNavDataSets
%>
<% if (GlobalProperties.showRightNavExamples()) {%>
    <h3>Example Queries</h3>
    <jsp:include page="<%= examplesHtml %>" flush="true" />
<% } %>

<% if (GlobalProperties.showRightNavTestimonials()) {%>
    <div id="rotating_testimonials">
        <h3>What People are Saying</h3>
        <jsp:include page="../testimonials.jsp" flush="true" />

    </div>
<% } %>
</div>
