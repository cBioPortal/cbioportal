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

<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.ByteArrayInputStream" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.io.InputStreamReader" %>
<%@ page import="java.util.zip.GZIPInputStream" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<%@ page import="org.mskcc.cbio.portal.util.FileUploadRequestWrapper" %>
<%@ page import="org.apache.commons.text.StringEscapeUtils" %>
<%@ page import="org.apache.commons.fileupload.FileItem" %>
<%@ page import="org.apache.commons.codec.binary.Base64" %>
<%@ page import="org.mskcc.cbio.portal.util.XssRequestWrapper" %>

<html>
<body>

    <jsp:include page="src/main/webapp/jsp/global/css_include.jsp" flush="true" />

<link href="css/network/network_ui.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet"/>


<script type="text/javascript" src="js/lib/jquery.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery-migrate.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery.tipTip.minified.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery.address.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery-ui.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/responsiveslides.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery.quovolver.mini.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/jquery.expander.min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/underscore-min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/backbone-min.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/lib/cytoscape_js/cytoscape.js?<%=GlobalProperties.getAppVersion()%>"></script>


<script type="text/javascript" src="js/src/network/network-visualization.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/network/network-viz2.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/network/cytoscape.renderer.canvas.portal-renderer.js?<%=GlobalProperties.getAppVersion()%>"></script>

<jsp:include page="src/main/webapp/jsp/network_views.jsp"/>

<table width="100%" cellspacing="0px" cellpadding="2px" border="0px">
	<tr valign="middle">
		<td valign="middle" width="25%">
			<a href="http://www.mskcc.org"><img src="images/mskcc_logo_3d_grey.jpg" height="50px" alt="MSKCC Logo"></a>
		</td>
		<td valign="middle" width="50%">
			<span id="header_site_name">
                            Network Visualization Service
			</span>
		</td>
		<td valign="middle" width="25%">
			Powered by:<br/><a href="https://www.cbioportal.org/"><%= GlobalProperties.getTitle() %></a>
		</td>
	</tr>
</table>
<script type="text/javascript">
    //alert('< %=org.mskcc.cbio.portal.util.ResponseUtil.getResponseString(request.getInputStream())%>');

    // TODO duplicate code, see js_include.jsp
	// This is for the moustache-like templates
	// prevents collisions with JSP tags
	_.templateSettings = {
		interpolate : /\{\{(.+?)\}\}/g
	};

</script>

<%
String format = request.getParameter("format");
String graphml = null;

if (format!=null) {
    graphml = request.getParameter("graphml");

	if (request instanceof XssRequestWrapper)
	{
		graphml = ((XssRequestWrapper)request).getRawParameter("graphml");
	}

    if (graphml!=null&&!graphml.isEmpty()) {
        if (!format.equalsIgnoreCase("graphml")) {
            try {
                byte[] bytes = null;
                if (format.equalsIgnoreCase("graphml.gz")) {
                    bytes = graphml.getBytes();
                } else if (format.equalsIgnoreCase("graphml.gz.base64")) {
                    bytes = Base64.decodeBase64(graphml.getBytes());
                }

                if (bytes!=null) {
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(new GZIPInputStream(
                                new ByteArrayInputStream(bytes))));
                    StringBuilder builder = new StringBuilder();
                    for (String line=bufferedReader.readLine(); line!=null;
                            line=bufferedReader.readLine()) {
                        builder.append(line);
                    }
                    graphml = builder.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    } else if (request instanceof FileUploadRequestWrapper) {
        // if graphml is not posted, look for uploaded file
        FileUploadRequestWrapper fileUploadRequestWrapper = (FileUploadRequestWrapper)request;
        FileItem fileItem = fileUploadRequestWrapper.getFileItem("graphml");
        if (fileItem!=null) {
            if (format.equalsIgnoreCase("graphml")) {
                graphml = fileItem.getString();
            } else {
                try {
                    InputStream is = null;
                    if (format.equalsIgnoreCase("graphml.gz")) {
                        is = fileItem.getInputStream();
                    } else if (format.equalsIgnoreCase("graphml.gz.base64")) {
                        is = new ByteArrayInputStream(Base64.decodeBase64(fileItem.getString().getBytes()));
                    }

                    if (is!=null) {
                        BufferedReader bufferedReader = new BufferedReader(
                                new InputStreamReader(new GZIPInputStream(is)));
                        StringBuilder builder = new StringBuilder();
                        for (String line=bufferedReader.readLine(); line!=null;
                                line=bufferedReader.readLine()) {
                            builder.append(line);
                        }
                        graphml = builder.toString();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

if (graphml!=null&&!graphml.isEmpty()) {
    graphml = StringEscapeUtils.escapeEcmaScript(graphml);
    boolean showProfileData = "true".equalsIgnoreCase(request.getParameter("show_profile_data"));

    String msgs = request.getParameter("msg");
//    if (msgs!=null) {
//        msgs = StringEscapeUtils.escapeEcmaScript(msgs);
//    }
%>

<script type="text/javascript">
    //alert('< %=ResponseUtil.getResponseString(request.getInputStream())%>');
    //alert('< %=graphml%>');
    $(document).ready(function(){
        var graphml = "<%=graphml%>";
        //graphml = '<graphml><graph><node id="n0"/></graph></graphml>';
        send2cytoscapeweb(graphml, "cytoscapeweb", "network");

        <%if(msgs!=null){%>
        var msgs = "<br/><%=msgs%>";
        $("#network #netmsg").append(msgs);
        <%}%>

        $("#network-resubmit-query").remove();

        if (<%=showProfileData%>) {
            var nre = setInterval(function() {
                if (!$("#network #network_menu_div").hasClass("hidden-network-ui")) {
                    $('#network #show_profile_data').click();
                    clearInterval(nre);
                }
            },1000);
        }
    });
</script>

<jsp:include page="src/main/webapp/jsp/network_div.jsp"/>
<%
} else {
%>
<div align="left">
<form name="input" action="netviz.jsp" enctype="multipart/form-data" method="post">
<br/>
Messages:
<textarea rows="4" cols="30" id="msg" name="msg">
<b>Below is a sample network.</b>
</textarea>
<br/>
GraphML:<br>
<textarea rows="10" cols="530" id="graphml" name="graphml">
&lt;graphml&gt;
&lt;key id="label" for="node" attr.name="label" attr.type="string"/&gt;
&lt;key id="type" for="all" attr.name="type" attr.type="string"/&gt;
&lt;key id="IN_QUERY" for="node" attr.name="IN_QUERY" attr.type="string"/&gt;
&lt;key id="RELATIONSHIP_XREF" for="node" attr.name="RELATIONSHIP_XREF" attr.type="string"/&gt;
&lt;key id="PERCENT_MUTATED" for="node" attr.name="PERCENT_MUTATED" attr.type="double"/&gt;
&lt;key id="PERCENT_CNA_HOMOZYGOUSLY_DELETED" for="node" attr.name="PERCENT_CNA_HOMOZYGOUSLY_DELETED" attr.type="double"/&gt;
&lt;key id="PERCENT_CNA_AMPLIFIED" for="node" attr.name="PERCENT_CNA_AMPLIFIED" attr.type="double"/&gt;
&lt;key id="PERCENT_ALTERED" for="node" attr.name="PERCENT_ALTERED" attr.type="double"/&gt;
&lt;key id="EXPERIMENTAL_TYPE" for="edge" attr.name="EXPERIMENTAL_TYPE" attr.type="string"/&gt;
&lt;key id="INTERACTION_DATA_SOURCE" for="edge" attr.name="INTERACTION_DATA_SOURCE" attr.type="string"/&gt;
&lt;key id="INTERACTION_PUBMED_ID" for="edge" attr.name="INTERACTION_PUBMED_ID" attr.type="string"/&gt;
&lt;graph edgedefault="undirected"&gt;
&lt;node id="604"&gt;
&lt;data key="label"&gt;BCL6&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:BCL6;Entrez Gene:604&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.09006928406466513&lt;/data&gt;
&lt;data key="PERCENT_CNA_HOMOZYGOUSLY_DELETED"&gt;0.0023094688221709007&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0023094688221709007&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.09468822170900693&lt;/data&gt;
&lt;/node&gt;
&lt;node id="174"&gt;
&lt;data key="label"&gt;AFP&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:AFP;Entrez Gene:174&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.03695150115473441&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.03695150115473441&lt;/data&gt;
&lt;/node&gt;
&lt;node id="6597"&gt;
&lt;data key="label"&gt;SMARCA4&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:SMARCA4;Entrez Gene:6597&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.07159353348729793&lt;/data&gt;
&lt;data key="PERCENT_CNA_HOMOZYGOUSLY_DELETED"&gt;0.004618937644341801&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.009237875288683603&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.08314087759815242&lt;/data&gt;
&lt;/node&gt;
&lt;node id="8626"&gt;
&lt;data key="label"&gt;TP63&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:TP63;Entrez Gene:8626&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.09006928406466513&lt;/data&gt;
&lt;data key="PERCENT_CNA_HOMOZYGOUSLY_DELETED"&gt;0.004618937644341801&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.006928406466512702&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.10161662817551963&lt;/data&gt;
&lt;/node&gt;
&lt;node id="5925"&gt;
&lt;data key="label"&gt;RB1&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:RB1;Entrez Gene:5925&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_HOMOZYGOUSLY_DELETED"&gt;0.03002309468822171&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.02771362586605081&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.057736720554272515&lt;/data&gt;
&lt;/node&gt;
&lt;node id="94241"&gt;
&lt;data key="label"&gt;TP53INP1&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:TP53INP1;Entrez Gene:94241&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.04849884526558892&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.04849884526558892&lt;/data&gt;
&lt;/node&gt;
&lt;node id="29102"&gt;
&lt;data key="label"&gt;DROSHA&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:DROSHA;Entrez Gene:29102&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.03464203233256351&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.006928406466512702&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.04157043879907621&lt;/data&gt;
&lt;/node&gt;
&lt;node id="5300"&gt;
&lt;data key="label"&gt;PIN1&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:PIN1;Entrez Gene:5300&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.06928406466512702&lt;/data&gt;
&lt;data key="PERCENT_CNA_HOMOZYGOUSLY_DELETED"&gt;0.0023094688221709007&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.07159353348729793&lt;/data&gt;
&lt;/node&gt;
&lt;node id="10987"&gt;
&lt;data key="label"&gt;COPS5&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:COPS5;Entrez Gene:10987&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.053117782909930716&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.053117782909930716&lt;/data&gt;
&lt;/node&gt;
&lt;node id="51147"&gt;
&lt;data key="label"&gt;ING4&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:ING4;Entrez Gene:51147&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.057736720554272515&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0023094688221709007&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.06004618937644342&lt;/data&gt;
&lt;/node&gt;
&lt;node id="10782"&gt;
&lt;data key="label"&gt;ZNF274&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:ZNF274;Entrez Gene:10782&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.025404157043879907&lt;/data&gt;
&lt;data key="PERCENT_CNA_HOMOZYGOUSLY_DELETED"&gt;0.013856812933025405&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0023094688221709007&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.04157043879907621&lt;/data&gt;
&lt;/node&gt;
&lt;node id="675"&gt;
&lt;data key="label"&gt;BRCA2&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:BRCA2;Entrez Gene:675&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.004618937644341801&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.09699769053117784&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.10161662817551963&lt;/data&gt;
&lt;/node&gt;
&lt;node id="199745"&gt;
&lt;data key="label"&gt;THAP8&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:THAP8;Entrez Gene:199745&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.08083140877598152&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0023094688221709007&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.08314087759815242&lt;/data&gt;
&lt;/node&gt;
&lt;node id="672"&gt;
&lt;data key="label"&gt;BRCA1&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:BRCA1;Entrez Gene:672&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.0023094688221709007&lt;/data&gt;
&lt;data key="PERCENT_CNA_HOMOZYGOUSLY_DELETED"&gt;0.006928406466512702&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.12240184757505773&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.12933025404157045&lt;/data&gt;
&lt;/node&gt;
&lt;node id="28996"&gt;
&lt;data key="label"&gt;HIPK2&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:HIPK2;Entrez Gene:28996&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.046189376443418015&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.004618937644341801&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.050808314087759814&lt;/data&gt;
&lt;/node&gt;
&lt;node id="4802"&gt;
&lt;data key="label"&gt;NFYC&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:NFYC;Entrez Gene:4802&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.06466512702078522&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0023094688221709007&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.06697459584295612&lt;/data&gt;
&lt;/node&gt;
&lt;node id="4170"&gt;
&lt;data key="label"&gt;MCL1&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:MCL1;Entrez Gene:4170&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.057736720554272515&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.057736720554272515&lt;/data&gt;
&lt;/node&gt;
&lt;node id="2185"&gt;
&lt;data key="label"&gt;PTK2B&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:PTK2B;Entrez Gene:2185&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.004618937644341801&lt;/data&gt;
&lt;data key="PERCENT_CNA_HOMOZYGOUSLY_DELETED"&gt;0.03002309468822171&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.006928406466512702&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.04157043879907621&lt;/data&gt;
&lt;/node&gt;
&lt;node id="8178"&gt;
&lt;data key="label"&gt;ELL&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:ELL;Entrez Gene:8178&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.04849884526558892&lt;/data&gt;
&lt;data key="PERCENT_CNA_HOMOZYGOUSLY_DELETED"&gt;0.0023094688221709007&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.050808314087759814&lt;/data&gt;
&lt;/node&gt;
&lt;node id="10397"&gt;
&lt;data key="label"&gt;NDRG1&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:NDRG1;Entrez Gene:10397&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.2471131639722864&lt;/data&gt;
&lt;data key="PERCENT_CNA_HOMOZYGOUSLY_DELETED"&gt;0.0023094688221709007&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.006928406466512702&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.2540415704387991&lt;/data&gt;
&lt;/node&gt;
&lt;node id="2064"&gt;
&lt;data key="label"&gt;ERBB2&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:ERBB2;Entrez Gene:2064&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;true&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.03926096997690531&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.009237875288683603&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.025404157043879907&lt;/data&gt;
&lt;data key="PERCENT_CNA_HOMOZYGOUSLY_DELETED"&gt;0.004618937644341801&lt;/data&gt;
&lt;/node&gt;
&lt;node id="2065"&gt;
&lt;data key="label"&gt;ERBB3&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:ERBB3;Entrez Gene:2065&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.03002309468822171&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.011547344110854504&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.04157043879907621&lt;/data&gt;
&lt;/node&gt;
&lt;node id="11244"&gt;
&lt;data key="label"&gt;ZHX1&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:ZHX1;Entrez Gene:11244&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.23787528868360278&lt;/data&gt;
&lt;data key="PERCENT_CNA_HOMOZYGOUSLY_DELETED"&gt;0.004618937644341801&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.004618937644341801&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.2471131639722864&lt;/data&gt;
&lt;/node&gt;
&lt;node id="255488"&gt;
&lt;data key="label"&gt;RNF144B&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:RNF144B;Entrez Gene:255488&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.053117782909930716&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.053117782909930716&lt;/data&gt;
&lt;/node&gt;
&lt;node id="4904"&gt;
&lt;data key="label"&gt;YBX1&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:YBX1;Entrez Gene:4904&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.05542725173210162&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.006928406466512702&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.06004618937644342&lt;/data&gt;
&lt;/node&gt;
&lt;node id="9518"&gt;
&lt;data key="label"&gt;GDF15&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:GDF15;Entrez Gene:9518&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.050808314087759814&lt;/data&gt;
&lt;data key="PERCENT_CNA_HOMOZYGOUSLY_DELETED"&gt;0.0023094688221709007&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.053117782909930716&lt;/data&gt;
&lt;/node&gt;
&lt;node id="79370"&gt;
&lt;data key="label"&gt;BCL2L14&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:BCL2L14;Entrez Gene:79370&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.04157043879907621&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0023094688221709007&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.04387990762124711&lt;/data&gt;
&lt;/node&gt;
&lt;node id="5747"&gt;
&lt;data key="label"&gt;PTK2&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:PTK2;Entrez Gene:5747&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.23556581986143188&lt;/data&gt;
&lt;data key="PERCENT_CNA_HOMOZYGOUSLY_DELETED"&gt;0.0023094688221709007&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.004618937644341801&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.24018475750577367&lt;/data&gt;
&lt;/node&gt;
&lt;node id="91768"&gt;
&lt;data key="label"&gt;CABLES1&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:CABLES1;Entrez Gene:91768&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.04157043879907621&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0023094688221709007&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.04387990762124711&lt;/data&gt;
&lt;/node&gt;
&lt;node id="4609"&gt;
&lt;data key="label"&gt;MYC&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:MYC;Entrez Gene:4609&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.30484988452655887&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.30484988452655887&lt;/data&gt;
&lt;/node&gt;
&lt;node id="1457"&gt;
&lt;data key="label"&gt;CSNK2A1&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:CSNK2A1;Entrez Gene:1457&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.03464203233256351&lt;/data&gt;
&lt;data key="PERCENT_CNA_HOMOZYGOUSLY_DELETED"&gt;0.0023094688221709007&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0023094688221709007&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.03926096997690531&lt;/data&gt;
&lt;/node&gt;
&lt;node id="23113"&gt;
&lt;data key="label"&gt;CUL9&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:CUL9;Entrez Gene:23113&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.03002309468822171&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.013856812933025405&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.04387990762124711&lt;/data&gt;
&lt;/node&gt;
&lt;node id="10498"&gt;
&lt;data key="label"&gt;CARM1&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:CARM1;Entrez Gene:10498&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.07390300230946882&lt;/data&gt;
&lt;data key="PERCENT_CNA_HOMOZYGOUSLY_DELETED"&gt;0.0023094688221709007&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.004618937644341801&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.08083140877598152&lt;/data&gt;
&lt;/node&gt;
&lt;node id="1387"&gt;
&lt;data key="label"&gt;CREBBP&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:CREBBP;Entrez Gene:1387&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.004618937644341801&lt;/data&gt;
&lt;data key="PERCENT_CNA_HOMOZYGOUSLY_DELETED"&gt;0.009237875288683603&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.025404157043879907&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.03926096997690531&lt;/data&gt;
&lt;/node&gt;
&lt;node id="1907"&gt;
&lt;data key="label"&gt;EDN2&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:EDN2;Entrez Gene:1907&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.06466512702078522&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.06466512702078522&lt;/data&gt;
&lt;/node&gt;
&lt;node id="4585"&gt;
&lt;data key="label"&gt;MUC4&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:MUC4;Entrez Gene:4585&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.10854503464203233&lt;/data&gt;
&lt;data key="PERCENT_CNA_HOMOZYGOUSLY_DELETED"&gt;0.0023094688221709007&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.011547344110854504&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.12009237875288684&lt;/data&gt;
&lt;/node&gt;
&lt;node id="4582"&gt;
&lt;data key="label"&gt;MUC1&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:MUC1;Entrez Gene:4582&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.03695150115473441&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.03695150115473441&lt;/data&gt;
&lt;/node&gt;
&lt;node id="598"&gt;
&lt;data key="label"&gt;BCL2L1&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:BCL2L1;Entrez Gene:598&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.04849884526558892&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.04849884526558892&lt;/data&gt;
&lt;/node&gt;
&lt;node id="5566"&gt;
&lt;data key="label"&gt;PRKACA&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:PRKACA;Entrez Gene:5566&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.09930715935334873&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0023094688221709007&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.10161662817551963&lt;/data&gt;
&lt;/node&gt;
&lt;node id="5058"&gt;
&lt;data key="label"&gt;PAK1&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:PAK1;Entrez Gene:5058&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.07159353348729793&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0023094688221709007&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.07390300230946882&lt;/data&gt;
&lt;/node&gt;
&lt;node id="7534"&gt;
&lt;data key="label"&gt;YWHAZ&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:YWHAZ;Entrez Gene:7534&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.09699769053117784&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.09699769053117784&lt;/data&gt;
&lt;/node&gt;
&lt;node id="50484"&gt;
&lt;data key="label"&gt;RRM2B&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:RRM2B;Entrez Gene:50484&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.08545034642032333&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.004618937644341801&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.08775981524249422&lt;/data&gt;
&lt;/node&gt;
&lt;node id="22948"&gt;
&lt;data key="label"&gt;CCT5&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:CCT5;Entrez Gene:22948&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.04849884526558892&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.006928406466512702&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.05542725173210162&lt;/data&gt;
&lt;/node&gt;
&lt;node id="6502"&gt;
&lt;data key="label"&gt;SKP2&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:SKP2;Entrez Gene:6502&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.03695150115473441&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.004618937644341801&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.03926096997690531&lt;/data&gt;
&lt;/node&gt;
&lt;node id="5591"&gt;
&lt;data key="label"&gt;PRKDC&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:PRKDC;Entrez Gene:5591&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.025404157043879907&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.018475750577367205&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.04387990762124711&lt;/data&gt;
&lt;/node&gt;
&lt;node id="8797"&gt;
&lt;data key="label"&gt;TNFRSF10A&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:TNFRSF10A;Entrez Gene:8797&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_HOMOZYGOUSLY_DELETED"&gt;0.03002309468822171&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.006928406466512702&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.03695150115473441&lt;/data&gt;
&lt;/node&gt;
&lt;node id="50813"&gt;
&lt;data key="label"&gt;COPS7A&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:COPS7A;Entrez Gene:50813&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.057736720554272515&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0023094688221709007&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.057736720554272515&lt;/data&gt;
&lt;/node&gt;
&lt;node id="7157"&gt;
&lt;data key="label"&gt;TP53&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:TP53;Entrez Gene:7157&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;true&lt;/data&gt;
&lt;data key="PERCENT_CNA_HOMOZYGOUSLY_DELETED"&gt;0.0023094688221709007&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.9076212471131639&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.9076212471131639&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.0023094688221709007&lt;/data&gt;
&lt;/node&gt;
&lt;node id="1020"&gt;
&lt;data key="label"&gt;CDK5&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:CDK5;Entrez Gene:1020&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.046189376443418015&lt;/data&gt;
&lt;data key="PERCENT_CNA_HOMOZYGOUSLY_DELETED"&gt;0.0023094688221709007&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.04849884526558892&lt;/data&gt;
&lt;/node&gt;
&lt;node id="57103"&gt;
&lt;data key="label"&gt;C12ORF5&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:C12ORF5;Entrez Gene:57103&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.05542725173210162&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.05542725173210162&lt;/data&gt;
&lt;/node&gt;
&lt;node id="81928"&gt;
&lt;data key="label"&gt;CABLES2&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:CABLES2;Entrez Gene:81928&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.057736720554272515&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.0&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.057736720554272515&lt;/data&gt;
&lt;/node&gt;
&lt;node id="5296"&gt;
&lt;data key="label"&gt;PIK3R2&lt;/data&gt;
&lt;data key="type"&gt;Protein&lt;/data&gt;
&lt;data key="RELATIONSHIP_XREF"&gt;HGNC:PIK3R2;Entrez Gene:5296&lt;/data&gt;
&lt;data key="IN_QUERY"&gt;false&lt;/data&gt;
&lt;data key="PERCENT_CNA_AMPLIFIED"&gt;0.04387990762124711&lt;/data&gt;
&lt;data key="PERCENT_CNA_HOMOZYGOUSLY_DELETED"&gt;0.0023094688221709007&lt;/data&gt;
&lt;data key="PERCENT_MUTATED"&gt;0.004618937644341801&lt;/data&gt;
&lt;data key="PERCENT_ALTERED"&gt;0.050808314087759814&lt;/data&gt;
&lt;/node&gt;
&lt;edge source="7157" target="6597" directed="false"&gt;
&lt;data key="type"&gt;CO_CONTROL&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="5925" target="4609" directed="true"&gt;
&lt;data key="type"&gt;METABOLIC_CATALYSIS&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="6597" target="7157" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;11950834&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="5566" target="2064" directed="true"&gt;
&lt;data key="type"&gt;STATE_CHANGE&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="672" target="675" directed="false"&gt;
&lt;data key="type"&gt;REACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;REACTOME&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="10987" target="7157" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;11285227,17879958&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="1907" directed="true"&gt;
&lt;data key="type"&gt;METABOLIC_CATALYSIS&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="672" target="675" directed="false"&gt;
&lt;data key="type"&gt;IN_SAME_COMPONENT&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;REACTOME&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="50813" target="7157" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;11285227&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="10987" directed="false"&gt;
&lt;data key="type"&gt;REACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="29102" target="7157" directed="false"&gt;
&lt;data key="type"&gt;IN_SAME_COMPONENT&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="4609" target="10397" directed="true"&gt;
&lt;data key="type"&gt;METABOLIC_CATALYSIS&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="2185" target="2065" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;15499613&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="5591" target="7157" directed="false"&gt;
&lt;data key="type"&gt;REACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="2065" target="4582" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;11278868,12939402&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="672" target="4609" directed="false"&gt;
&lt;data key="type"&gt;CO_CONTROL&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="4609" target="7157" directed="true"&gt;
&lt;data key="type"&gt;METABOLIC_CATALYSIS&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="29102" directed="false"&gt;
&lt;data key="type"&gt;REACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="91768" target="7157" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;14637168,11706030&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="10498" target="7157" directed="false"&gt;
&lt;data key="type"&gt;REACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="2185" target="5747" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;16760434&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="23113" target="7157" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;12526791&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="4904" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;11175333,11973333&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="4585" target="2064" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;12434309,11687512,11598901&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="5058" target="1457" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;10938077&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="5925" target="6502" directed="false"&gt;
&lt;data key="type"&gt;IN_SAME_COMPONENT&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="5566" target="10397" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;17220478&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="672" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;9482880,9926942,14710355,9582019,14978302&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="50484" target="7157" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;12615712&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="10397" directed="true"&gt;
&lt;data key="type"&gt;METABOLIC_CATALYSIS&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="10782" target="7157" directed="true"&gt;
&lt;data key="type"&gt;STATE_CHANGE&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="174" directed="true"&gt;
&lt;data key="type"&gt;METABOLIC_CATALYSIS&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="5591" directed="false"&gt;
&lt;data key="type"&gt;REACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="5300" directed="false"&gt;
&lt;data key="type"&gt;REACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="1387" target="4609" directed="true"&gt;
&lt;data key="type"&gt;METABOLIC_CATALYSIS&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="5296" target="2064" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;1334406&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="675" target="672" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;9774970,11477095&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="28996" target="7157" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;11925430,11780126,11875057,15896780&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="1020" target="7157" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;11483158,12064478,10884347&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="5747" target="2064" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;11807823&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="1387" target="1457" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;9685505&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="51147" target="7157" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;12750254&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="6597" target="7157" directed="false"&gt;
&lt;data key="type"&gt;CO_CONTROL&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="2065" target="2064" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;11173924,7538656,10799311&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="2064" target="2065" directed="false"&gt;
&lt;data key="type"&gt;REACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="8797" directed="true"&gt;
&lt;data key="type"&gt;METABOLIC_CATALYSIS&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="4609" target="5925" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;7838535&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="5591" target="7157" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;
10470151,9765199,11551930,11709713,10673501,10747897,11883897,10951572
&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="4802" directed="false"&gt;
&lt;data key="type"&gt;REACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="5300" target="7157" directed="false"&gt;
&lt;data key="type"&gt;IN_SAME_COMPONENT&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="672" target="675" directed="true"&gt;
&lt;data key="type"&gt;STATE_CHANGE&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;REACTOME&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="94241" target="7157" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;11511362,12851404&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="2185" target="2064" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;10713673&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="4609" target="672" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;9788437,12646176&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo;yeast 2-hybrid&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="675" target="672" directed="false"&gt;
&lt;data key="type"&gt;REACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;REACTOME&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="2065" target="5566" directed="true"&gt;
&lt;data key="type"&gt;STATE_CHANGE&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="5300" target="7157" directed="false"&gt;
&lt;data key="type"&gt;REACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="255488" directed="true"&gt;
&lt;data key="type"&gt;METABOLIC_CATALYSIS&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="2065" target="5296" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;16729043&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="1387" target="7157" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;11782467,10848610,9194564&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="81928" target="1020" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;11955625&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="2064" target="4582" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;11278868,12939402&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="4802" target="4609" directed="false"&gt;
&lt;data key="type"&gt;IN_SAME_COMPONENT&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="10498" directed="false"&gt;
&lt;data key="type"&gt;REACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="1020" target="2065" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;12824184&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="5925" target="6502" directed="false"&gt;
&lt;data key="type"&gt;REACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="8626" directed="true"&gt;
&lt;data key="type"&gt;METABOLIC_CATALYSIS&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="5300" directed="false"&gt;
&lt;data key="type"&gt;IN_SAME_COMPONENT&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="5591" target="7157" directed="false"&gt;
&lt;data key="type"&gt;IN_SAME_COMPONENT&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="675" target="672" directed="true"&gt;
&lt;data key="type"&gt;STATE_CHANGE&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;REACTOME&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="5925" target="672" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;10220405&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="79370" target="598" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;11054413&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="6502" target="5925" directed="false"&gt;
&lt;data key="type"&gt;REACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="79370" directed="true"&gt;
&lt;data key="type"&gt;METABOLIC_CATALYSIS&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="29102" directed="false"&gt;
&lt;data key="type"&gt;IN_SAME_COMPONENT&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="4170" target="598" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;15077116&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="6597" target="5925" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;7923370&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vivo;yeast 2-hybrid&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="81928" target="7157" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;14637168&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="5591" directed="false"&gt;
&lt;data key="type"&gt;IN_SAME_COMPONENT&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="6502" target="5925" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;15469821&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="604" directed="true"&gt;
&lt;data key="type"&gt;METABOLIC_CATALYSIS&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="4170" target="7157" directed="false"&gt;
&lt;data key="type"&gt;REACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="8626" target="7157" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;11238924&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="4802" target="7157" directed="false"&gt;
&lt;data key="type"&gt;REACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="2064" target="2065" directed="false"&gt;
&lt;data key="type"&gt;IN_SAME_COMPONENT&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="94241" directed="true"&gt;
&lt;data key="type"&gt;METABOLIC_CATALYSIS&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="598" target="7157" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;12667443,16151013&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="2065" target="2064" directed="false"&gt;
&lt;data key="type"&gt;IN_SAME_COMPONENT&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="1457" target="672" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;10403822&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="10498" directed="false"&gt;
&lt;data key="type"&gt;IN_SAME_COMPONENT&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="5300" target="7157" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;12397362,12388558,12397361&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="29102" target="7157" directed="false"&gt;
&lt;data key="type"&gt;REACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="5925" directed="true"&gt;
&lt;data key="type"&gt;METABOLIC_CATALYSIS&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="2064" target="5566" directed="true"&gt;
&lt;data key="type"&gt;STATE_CHANGE&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="4802" target="4609" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;10446203,11282029&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="5747" target="7157" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;15855171&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="94241" target="28996" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;12851404&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="2065" target="2064" directed="false"&gt;
&lt;data key="type"&gt;REACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="57103" directed="true"&gt;
&lt;data key="type"&gt;METABOLIC_CATALYSIS&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="4609" target="4802" directed="false"&gt;
&lt;data key="type"&gt;IN_SAME_COMPONENT&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="1457" target="7157" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;
10747897,10348343,10884347,11709713,11883897,10951572,12628923
&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="1387" target="672" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;10655477&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="672" directed="false"&gt;
&lt;data key="type"&gt;REACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="4609" target="1387" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;12776737&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo;yeast 2-hybrid&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="8178" target="7157" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;10358050,8016121&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo;yeast 2-hybrid&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="10498" target="7157" directed="false"&gt;
&lt;data key="type"&gt;IN_SAME_COMPONENT&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="4802" target="4609" directed="false"&gt;
&lt;data key="type"&gt;REACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="6502" target="7157" directed="true"&gt;
&lt;data key="type"&gt;STATE_CHANGE&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="5747" target="2065" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;11807823&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="4609" target="6502" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;17157259&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="91768" target="1020" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;10896159,11733001,11955625&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vivo;yeast 2-hybrid&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="50484" directed="true"&gt;
&lt;data key="type"&gt;METABOLIC_CATALYSIS&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="1457" target="4609" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;2663470&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="10987" target="7157" directed="false"&gt;
&lt;data key="type"&gt;REACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="4802" directed="false"&gt;
&lt;data key="type"&gt;IN_SAME_COMPONENT&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="5300" target="4609" directed="true"&gt;
&lt;data key="type"&gt;STATE_CHANGE&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="4802" target="4609" directed="false"&gt;
&lt;data key="type"&gt;CO_CONTROL&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="9518" directed="true"&gt;
&lt;data key="type"&gt;METABOLIC_CATALYSIS&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="1457" target="11244" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;16169070&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;yeast 2-hybrid&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="4170" directed="false"&gt;
&lt;data key="type"&gt;REACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="28996" target="1387" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;11740489&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="675" target="7157" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;9811893&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="199745" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;16169070&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;yeast 2-hybrid&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="4802" target="7157" directed="false"&gt;
&lt;data key="type"&gt;IN_SAME_COMPONENT&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="1020" target="5058" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;11604394&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="6502" target="5925" directed="false"&gt;
&lt;data key="type"&gt;IN_SAME_COMPONENT&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="10498" target="1387" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;12374746&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="4609" target="4802" directed="false"&gt;
&lt;data key="type"&gt;REACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="5566" target="2065" directed="true"&gt;
&lt;data key="type"&gt;STATE_CHANGE&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="6597" target="672" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;10943845,15034933&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="675" target="672" directed="false"&gt;
&lt;data key="type"&gt;IN_SAME_COMPONENT&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;REACTOME&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="672" target="7157" directed="false"&gt;
&lt;data key="type"&gt;REACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7534" target="7157" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;9620776,16376338&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="50813" target="10987" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;11285227,11337588&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="5566" target="7534" directed="true"&gt;
&lt;data key="type"&gt;STATE_CHANGE&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="11244" target="7157" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;15383276&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;yeast 2-hybrid&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="5300" target="1457" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;11940573&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="672" target="7157" directed="false"&gt;
&lt;data key="type"&gt;IN_SAME_COMPONENT&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="22948" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;16169070&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;yeast 2-hybrid&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="5058" target="2064" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;9774445&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vivo&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="6502" target="4609" directed="true"&gt;
&lt;data key="type"&gt;STATE_CHANGE&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="4609" target="4802" directed="false"&gt;
&lt;data key="type"&gt;CO_CONTROL&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="4609" target="2064" directed="true"&gt;
&lt;data key="type"&gt;METABOLIC_CATALYSIS&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="4609" target="672" directed="false"&gt;
&lt;data key="type"&gt;CO_CONTROL&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="7157" target="672" directed="false"&gt;
&lt;data key="type"&gt;IN_SAME_COMPONENT&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="28996" target="7157" directed="true"&gt;
&lt;data key="type"&gt;STATE_CHANGE&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;NA&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;NCI_NATURE&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;NA&lt;/data&gt;
&lt;/edge&gt;
&lt;edge source="28996" target="8626" directed="false"&gt;
&lt;data key="type"&gt;INTERACTS_WITH&lt;/data&gt;
&lt;data key="INTERACTION_PUBMED_ID"&gt;11925430&lt;/data&gt;
&lt;data key="INTERACTION_DATA_SOURCE"&gt;HPRD&lt;/data&gt;
&lt;data key="EXPERIMENTAL_TYPE"&gt;in vitro;yeast 2-hybrid&lt;/data&gt;
&lt;/edge&gt;
&lt;/graph&gt;
&lt;/graphml&gt;
</textarea><br>
Or upload a file in format of graphml or gzipped graphml<br>
<input type="file" name="graphml"/><br>
Format:<br/>
<select name="format">
  <option value="graphml">GraphML</option>
  <option value="graphml.gz.base64">GraphML.GZ.Base64</option>
  <option value="graphml.gz">GraphML.GZ (only for uploaded file)</option>
</select>
<br/>
<input type="checkbox" name="show_profile_data" value="true" /> Show genomic profile data by default<br />
<input type="submit" name="submit" value="Submit" />
</form>
</div>
<%
}
%>

</body>
</html>
