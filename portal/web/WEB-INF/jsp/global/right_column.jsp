<%@ page import="org.mskcc.portal.util.Config" %>
<%@ page import="org.mskcc.portal.util.SkinUtil" %>
<%@ page import="org.mskcc.portal.util.DataSetsUtil" %> 
<%@ page import="org.mskcc.cgds.model.CancerStudyStats" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%
   Config globalConfig = Config.getInstance();
   String examplesHtml = globalConfig.getProperty("examples_right_column");
   if (examplesHtml == null) {
	   examplesHtml = "../../../content/examples.html";
   } else {
	   examplesHtml = "../../../content/" + examplesHtml;
   }

   DataSetsUtil dataSetsUtil = null;
   List<CancerStudyStats> cancerStudyStats = null;
   if (SkinUtil.showRightNavDataSets()) {
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
<%
if (SkinUtil.showRightNavDataSets()) {
%>
    <h3>Data Sets</h3>
<%
    out.println("<P>The Portal contains data for <b>" + dataSetsUtil.getTotalNumberOfSamples() + " tumor samples from " +
                     cancerStudyStats.size() + " cancer studies.</b> [<a href='data_sets.jsp'>Details.</a>]</p>");
%>
    <script type='text/javascript' src='https://www.google.com/jsapi'></script>
    <script type='text/javascript'>
    google.load('visualization', '1.0', {'packages':['corechart']});
    google.setOnLoadCallback(drawChart);
    function drawChart() {
        var data = new google.visualization.DataTable();
        data.addColumn('string', 'Cancer Study');
        data.addColumn('number', 'Samples');
<%
    out.println("data.addRows([");
    for (CancerStudyStats stats : cancerStudyStats) {
        out.println("['" + stats.getStudyName() + "', " + stats.getAll() + "],");
    }
    out.println("]);");
%>
    var options = {
        'backgroundColor' : '#F1F6FE',
        'is3D' : false,
        'pieSliceText' : 'value',
        'tooltip':{'text' : 'value'},
        'width' : 300,
        'legend' : {'position' : 'none'},
        'left' : 0,'top' : 0,
        'height' : 300
    };
    var chart = new google.visualization.PieChart(document.getElementById('chart_div1'));
    chart.draw(data, options);
}
    </script>
    <div id='chart_div1'></div>
<%
    } // if showRightNavDataSets
%>
<% if (SkinUtil.showRightNavExamples()) {%>
    <h3>Example Queries</h3>
    <jsp:include page="<%= examplesHtml %>" flush="true" />

    <h3>What's New</h3>

    <P>The Portal paper has just been published:  Ethan Cerami, et. al.  <b>The cBio Cancer Genomics Portal: An Open Platform for Exploring Multidimensional Cancer Genomics Data.</b>  <i>Cancer Discovery</i>. May 2012 2; 401.
    [<a href="http://cancerdiscovery.aacrjournals.org/content/2/5/401.abstract">Abstract</a>].</P>

<% } %>

<% if (SkinUtil.showRightNavTestimonials()) {%>
    <div id="rotating_testimonials">
        <h3>What People are Saying</h3>
        <jsp:include page="../testimonials.jsp" flush="true" />

    </div>
<% } %>
</div>
