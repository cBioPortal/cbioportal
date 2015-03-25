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

<%@page import="org.json.simple.JSONValue"%>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.cbio.portal.servlet.CancerStudyView" %>
<%@ page import="org.mskcc.cbio.portal.servlet.PatientView" %>
<%@ page import="org.mskcc.cbio.portal.model.CancerStudy" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneticProfile" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<%@ page import="java.util.List" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.json.simple.JSONValue" %>

<%
request.setAttribute("tumormap", true);
String isDemoMode = request.getParameter("demo");
boolean showPlaceHoder;
if (isDemoMode!=null) {
    showPlaceHoder = isDemoMode.equalsIgnoreCase("on");
} else {
    showPlaceHoder = GlobalProperties.showPlaceholderInPatientView();
}

CancerStudy cancerStudy = (CancerStudy)request.getAttribute(CancerStudyView.CANCER_STUDY);
String cancerStudyViewError = (String)request.getAttribute(CancerStudyView.ERROR);

String caseSetId = (String)request.getAttribute(QueryBuilder.CASE_SET_ID);
List<String> caseIds = (List<String>)request.getAttribute(QueryBuilder.CASE_IDS);
String jsonCaseIds = JSONValue.toJSONString(caseIds);

GeneticProfile mutationProfile = (GeneticProfile)request.getAttribute(CancerStudyView.MUTATION_PROFILE);
boolean hasMutation = mutationProfile!=null;

boolean hasMutSig = cancerStudy!=null && cancerStudy.hasMutSigData();
boolean showMutationsTab = hasMutation;

GeneticProfile cnaProfile = (GeneticProfile)request.getAttribute(CancerStudyView.CNA_PROFILE);
boolean hasCNA = cnaProfile!=null;

boolean hasGistic = cancerStudy!=null && cancerStudy.hasGisticData();
boolean showCNATab = hasGistic;

String mutationProfileStableId = null;
String cnaProfileStableId = null;
if (mutationProfile!=null) {
    mutationProfileStableId = mutationProfile.getStableId();
}
if (cnaProfile!=null) {
    cnaProfileStableId = cnaProfile.getStableId();
}

boolean hasCnaSegmentData = cancerStudy!=null && cancerStudy.hasCnaSegmentData();

if (cancerStudyViewError!=null) {
    out.print(cancerStudyViewError);
} else {
%>

<jsp:include page="../../global/header.jsp" flush="true" />

<table width="100%">
    <tr>
        <td>
            <form method="post" action="index.do">
                <b><u><%=cancerStudy.getName()%></u></b>
                <input type="hidden" name="cancer_study_id" value="<%=cancerStudy.getCancerStudyStableId()%>">
                <input type="submit" value="Query this study">
            </form>
        </td>
    </tr>
    <tr>
        <td id="study-desc"><%=cancerStudy.getDescription()%>
            <%if (null!=cancerStudy.getPmid()) {%>
            &nbsp;<a href="http://www.ncbi.nlm.nih.gov/pubmed/<%=cancerStudy.getPmid()%>">PubMed</a>
            <%}%>
        </td>
    </tr>
</table>


<div id="study-tabs">
    <ul>
        
    <li><a href='#clinical-plots' class='study-tab' title='Plots'>Study Summary</a></li>
    <li><a href='#clinical' class='study-tab' title='Clinical Data'>Clinical Data</a></li>
    
    <%if(showMutationsTab){%>
    <li><a href='#mutations' class='study-tab' title='Mutations'>Mutated Genes</a></li>
    <%}%>
    
    <%if(showCNATab){%>
    <li><a href='#cna' class='study-tab' title='Copy Number Alterations'>Copy Number Alterations</a></li>
    <%}%>
    
    </ul>

    <div class="study-section" id="clinical-plots">
        <%@ include file="plots.jsp" %>
    </div>

    <div class="study-section" id="clinical">
        <%@ include file="clinical.jsp" %>
    </div>

    <%if(showMutationsTab){%>
    <div class="study-section" id="mutations">
        <%@ include file="mutations.jsp" %>
    </div>
    <%}%>

    <%if(showCNATab){%>
    <div class="study-section" id="cna">
        <%@ include file="cna.jsp" %>
    </div>
    <%}%>

</div>
<%  
}
%>
        </div>
    </td>
</tr>

<tr>
    <td colspan="3">
	<jsp:include page="../../global/footer.jsp" flush="true" />
    </td>
</tr>

</table>
</center>
</div>
<jsp:include page="../../global/xdebug.jsp" flush="true" />    

<style type="text/css">
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
        .dataTables_filter {
                width: 40%;
        }
        .div.datatable-paging {
                width: auto;
                float: right;
        }
        .data-table-name {
                float: left;
                font-weight: bold;
                font-size: 120%;
                vertical-align: middle;
        }
</style>

<script type="text/javascript" src="js/src/cancer-study-view/load-clinical-data.js?<%=GlobalProperties.getAppVersion()%>"></script>

<script type="text/javascript">
var cancerStudyId = '<%=cancerStudy.getCancerStudyStableId()%>';
var mutationProfileId = <%=mutationProfileStableId==null%>?null:'<%=mutationProfileStableId%>';
var cnaProfileId = <%=cnaProfileStableId==null%>?null:'<%=cnaProfileStableId%>';
var hasCnaSegmentData = <%=hasCnaSegmentData%>;
var hasMutSig = <%=hasMutSig%>;
var caseSetId = '<%=caseSetId%>';
var caseIds = <%=jsonCaseIds%>;

$(document).ready(function(){
    setUpStudyTabs();
    initTabs();
});

function setUpStudyTabs() {
    $('#study-tabs').tabs();
    $('#study-tabs').show();
}

function initTabs() {
    var tabContainers = $('.study-section');
    tabContainers.hide().filter(':first').show();

    $('.study-tab').click(function () {
            tabContainers.hide();
            tabContainers.filter(this.hash).show();
            $('.study-tab').removeClass('selected');
            $(this).addClass('selected');
            return false;
    }).filter(':first').click();   
}

function switchToTab(toTab) {
    $('.study-section').hide();
    $('.study-section#'+toTab).show();
    $('#study-tabs').tabs("option",
		"active",
		$('#study-tabs ul a[href="#'+toTab+'"]').parent().index());
}

function getRefererCaseId() {
    //var match = /case_id=([^&]+)/.exec(document.referrer);
    //return match ? match[1] : null;
    var idStr = /^#?case_ids=(.+)/.exec(location.hash);
    if (!idStr) return null;
    var ids = {};
    idStr[1].split(/[ ,]+/).forEach(function(id) {
        ids[id] = true;
    });
    return ids;
}

</script>

</body>
</html>
