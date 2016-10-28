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

<%@ page import="org.mskcc.cbio.portal.model.CancerStudyStats" %>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %> 
<%@ page import="org.mskcc.cbio.portal.util.DataSetsUtil" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>

<%
   String dataSetsHtml = GlobalProperties.getProperty("data_sets");
   String siteTitle = GlobalProperties.getTitle();
   String dataSetsHeader = GlobalProperties.getDataSetsHeader();
   String dataSetsFooter = GlobalProperties.getDataSetsFooter();
   String segFileURL = GlobalProperties.getProperty("segfile.url");
   DataSetsUtil dataSetsUtil = new DataSetsUtil();

   // get list of cancer study stats to process
   List<CancerStudyStats> cancerStudyStats = null;
   try {
	   cancerStudyStats = dataSetsUtil.getCancerStudyStats();
   }
   catch (Exception e) {
	   cancerStudyStats = new ArrayList<CancerStudyStats>();
   }

   // we may have to insert num cancer studies in header
   if (dataSetsHeader != null && dataSetsHeader.indexOf("<NUM_CANCER_STUDIES>") != -1) {
      dataSetsHeader = dataSetsHeader.replace("<NUM_CANCER_STUDIES>", Integer.toString(cancerStudyStats.size()));
   }
%>

<% request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::Data Sets"); %>
<jsp:include page="WEB-INF/jsp/global/header.jsp" flush="true" />
<div id="main">
    <table cellspacing="2px">
        <tr>
            <td width="100%">
                <h1>Data Sets</h1>
           <div class="markdown">
			  <%
			     if (dataSetsHeader != null) {
				     out.println("<p>" + dataSetsHeader + "<br></p>");
                 }
				 out.println("<table style='display:none;' cellpadding='0' cellspacing='0' border='0' class='display' id='data-set-table'>");
				 out.println("<thead><tr>");
				 out.println("<th scope='col'>CancerStudy</th>");
				 out.println("<th scope='col'>Reference</th>");
				 out.println("<th scope='col'>All</th>");
				 out.println("<th scope='col'>Sequenced</th>");
				 out.println("<th scope='col'>CNA</th>");
				 out.println("<th scope='col'>Tumor mRNA (RNA-Seq V2)</th>");
				 out.println("<th scope='col'>Tumor mRNA (microarray)</th>");
				 //out.println("<th scope='col'>Normal mRNA</th>");
				 out.println("<th scope='col'>Tumor miRNA</th>");
				 out.println("<th scope='col'>Methylation (HM27)</th>");
				 out.println("<th scope='col'>RPPA</th>");
				 out.println("<th scope='col'>Complete</th>");
				 out.println("</tr></thead>");
				 out.println("<tbody>");
				 // iterate over all cancer study attributes
				 int lc = 0;
				 for (CancerStudyStats stats : cancerStudyStats) {
					 // alternate every other row color
					 out.println(((++lc % 2) == 0) ? "<tr>" : "<tr class=\"rowcolor\">");
					 // cancer study name
					 String stableID = stats.getStableID();
					 String studyName = stats.getStudyName();
                     String htmlStudyName = "<a href='"
                         + GlobalProperties.getLinkToCancerStudyView(stableID)
                         + "'>" + studyName + "</a>";
                     String studyDataDownloadForm = "<form class='datasets-page-study-download' studyid='" + stableID
                         + "' method='get' action=''>"
                         + "<input type='hidden' name='raw' value='true'>"
                         + "<button class='btn btn-default btn-xs'><i class='fa fa-download'></i></button></form>";
                     String reference = stats.getReference();
                     out.println("<td style=\"text-align: left;\"><b>"
                         + htmlStudyName + "</b>" + studyDataDownloadForm + "</td>");
                     out.println("<td style=\"text-align: left;\"><b>" + reference + "</b></td>");
                     // all
                     out.println("<td style=\"text-align: center;\"><b>" + stats.getAll() + "</b></td>");
                     // sequenced
					 String sequenced = (stats.getSequenced() != 0) ? stats.getSequenced().toString() : "";
					 out.println("<td style=\"text-align: center;\">" + sequenced + "</td>");
					 // cna
					 String cna = (stats.getCNA() != 0) ? stats.getCNA().toString() : "";
					 out.println("<td style=\"text-align: center;\">" + cna  + "</td>");
					 // RNA-Seq
					 String rnaSEQ = (stats.getRNASEQ() != 0) ? stats.getRNASEQ().toString() : "";
					 out.println("<td style=\"text-align: center;\">" + rnaSEQ + "</td>");
					 // tumor mRNA
					 String mRNA = (stats.getTumorMRNA() != 0) ? stats.getTumorMRNA().toString() : "";
					 out.println("<td style=\"text-align: center;\">" + mRNA + "</td>");
					 // normal
					 /*
					 String normal = stats.getNormal().toString();
					 /*
					 if (stableID.equals("prad_mskcc")) {
						 normal = "30";
					 }
					 else if (stableID.equals("sarc_mskcc")) {
						 normal = "8";
					 }
					 out.println("<td style=\"text-align: center;\">" + normal + "</td>");
					 */
					 // tumor miRNA
					 String miRNA = (stats.getTumorMIRNA() != 0) ? stats.getTumorMIRNA().toString() : "";
					 out.println("<td style=\"text-align: center;\">" + miRNA + "</td>");
					 // methlyation
					 String meth = (stats.getMethylation() != 0) ? stats.getMethylation().toString() : "";
					 out.println("<td style=\"text-align: center;\">" + meth + "</td>");
					 // rppa
					 String rppa = (stats.getRPPA() != 0) ? stats.getRPPA().toString() : "";
					 out.println("<td style=\"text-align: center;\">" + rppa + "</td>");
					 // complete
					 String complete = (stats.getComplete() != 0) ? stats.getComplete().toString() : "";
					 out.println("<td style=\"text-align: center;\"><b>" + complete + "<b></td>");
				     out.println("</tr>");
				 }
				 out.println("</tbody></table>");
				 //out.println("<br>Total number of samples: <b>" + dataSetsUtil.getTotalNumberOfSamples() + "</b>");
				 if (dataSetsFooter != null) {
				     out.println("<br><br>" + dataSetsFooter + "</p>");
                 }
			  %>
          </div> 
            </td>
        </tr>
    </table>
</div>
    </td>
  </tr>
  <tr>
    <td colspan="3">
	<jsp:include page="WEB-INF/jsp/global/footer.jsp" flush="true" />
    </td>
  </tr>
</table>
</center>
</div>
</form>
<jsp:include page="WEB-INF/jsp/global/xdebug.jsp" flush="true" />

<style type="text/css" title="currentStyle"> 
        @import "css/data_table_jui.css?<%=GlobalProperties.getAppVersion()%>";
        @import "css/data_table_ColVis.css?<%=GlobalProperties.getAppVersion()%>";
        .ColVis {
                float: left;
                margin-bottom: 0
        }
        .dataTables_length {
                width: auto;
                float: right;
        }
        .dataTables_info {
                clear: none;
                width: auto;
                float: right;
        }
        .div.datatable-paging {
                width: auto;
                float: right;
        }
        table.display thead th{
            border: 1px solid #d3d3d3;
            background: #e6e6e6 url(images/ui-bg_glass_75_e6e6e6_1x400.png) 50% 50% repeat-x;
            font-weight: bold;
            color: #555555;
        }

        .datasets-page-study-download {
            float: right;
            display: none;
        }

        .datasets-page-study-download button {
            padding: 0;
            background: transparent;
            border: 0;
            font-size: 11px;
            line-height: 12px;
            margin: 0 4px;
        }

        .datasets-page-study-download button:hover {
            background: transparent;
            background-color: transparent;
            border: 0;
        }
</style>

<script type="text/javascript">
    $(document).ready(function() {
        $('#data-set-table').dataTable({
            "sDom": '<"H"fr>t<"F"<"datatable-paging"pil>>', // selectable columns
            "bJQueryUI": true,
            "bDestroy": true,
            "oLanguage": {
                "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                "sInfoFiltered": "",
                "sLengthMenu": "Show _MENU_ per page"
            },
            "iDisplayLength": -1,
            "aLengthMenu": [[5,10, 25, 50, 100, -1], [5, 10, 25, 50, 100, "All"]]
        }).show();
        window.cbio.util.getDatahubStudiesList()
            .then(function(data) {
                if(_.isObject(data)) {
                    $('.datasets-page-study-download').each(function(index, element) {
                        var jEle = $(element);
                        var studyId = jEle.attr('studyid');
                        if (data.hasOwnProperty(studyId)) {
                            jEle.attr('action', data[studyId].htmlURL);
                            jEle.css('display', 'block');
                            jEle.qtip({
                                content: {text: 'Download all genomic and clinical data files of this study.'},
                                style: {classes: 'qtip-light qtip-rounded qtip-shadow'},
                                show: {event: 'mouseover'},
                                hide: {fixed: true, delay: 100, event: 'mouseout'},
                                position: {
                                    my: 'bottom center',
                                    at: 'top center',
                                    viewport: $(window)
                                }
                            });
                        }
                    });
                }
            }).fail(function(error) {
            console.log(error);
        });
    })
</script>

</body>
</html>
