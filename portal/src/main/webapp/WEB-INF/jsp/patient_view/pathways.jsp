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

<%
request.setAttribute("include_network_help_tab", Boolean.FALSE);
request.setAttribute("include_network_legend", Boolean.FALSE);
%>
<!--link href="css/network/jquery-ui-1.8.14.custom.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet"/-->
<link href="css/network/network_ui.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet"/>
<script type="text/javascript" src="js/lib/cytoscape_js/cytoscape.js?<%=GlobalProperties.getAppVersion()%>"></script>

<!--script type="text/javascript" src="js/src/network/jquery-ui.min.js?<%=GlobalProperties.getAppVersion()%>"></script-->
<!--script type="text/javascript" src="js/src/network/network-ui.js?<%=GlobalProperties.getAppVersion()%>"></script-->
<script type="text/javascript" src="js/src/network/network-viz2.js?<%=GlobalProperties.getAppVersion()%>"></script>

<script type="text/javascript">
    function buildCytoscapeWeb() {
        var genes = [];
        genes.push(genomicEventObs.cnas.overviewEventGenes);
        genes.push(genomicEventObs.mutations.overviewEventGenes);

        var networkParams = {<%=org.mskcc.cbio.portal.servlet.QueryBuilder.GENE_LIST%>:genes.join(','),
                netsize:'small'
            };
        $.post("network.do",
            networkParams,
            function(graphml){
                if (typeof data !== "string")
                {
                  var gml2jsonConverter = new GraphMLToJSon(graphml);
                  var json = gml2jsonConverter.toJSON();
                  send2cytoscapeweb(json, "cytoscapeweb", "network");

                $("#network-resubmit-query").remove();
                $("#slider_area").remove();
                $('select#drop_down_select>option:eq(1)').attr('selected', true);
               } 
        );
    }

    $(document).ready(function(){
        genomicEventObs.subscribeMutCna(buildCytoscapeWeb);
    }
    );
</script>

<jsp:include page="../network_div.jsp"/>
