<%@ page import="org.json.simple.JSONValue"%>
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

<jsp:include page="../global/header.jsp" flush="true" />

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
        
    <li id="li-1"><a href='#dc-plots' id='study-tab-dc-plots-a' class='study-tab' title='Study Summary'>Study Summary</a></li>
    <!--<li><a href='#clinical-plots' class='study-tab' title='DC Plots'>Study Summary</a></li>-->
    <li><a href='#clinical' id='study-tab-clinical-a' class='study-tab' title='Clinical Data'>Clinical Data</a></li>
    
    <%if(showMutationsTab){%>
    <li><a href='#mutations' id='study-tab-mutations-a' class='study-tab' title='Mutations'>Mutated Genes</a></li>
    <%}%>
    
    <%if(showCNATab){%>
    <li><a href='#cna' id='study-tab-cna-a' class='study-tab' title='Copy Number Alterations'>Copy Number Alterations</a></li>
    <%}%>
    
    </ul>
    
    <div class="study-section" id="dc-plots">
        <%@ include file="dcplots.jsp" %>
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
	<jsp:include page="../global/footer.jsp" flush="true" />
    </td>
</tr>

</table>
</center>
</div>
<jsp:include page="../global/xdebug.jsp" flush="true" />    

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
var cancer_study_id = cancerStudyId; //Some components using this as global ID
var appVersion = <%=GlobalProperties.getAppVersion()%>;

$("#study-tabs").tabs({disabled: true});
$("#study-tabs").tabs("enable", 0);

$('#study-tab-clinical-a').click(function(){
    if (!$(this).parent().hasClass('ui-state-disabled') && !$(this).hasClass("tab-clicked")) {
        //First time: adjust the width of data table;
        $("#clinical-data-table-loading-wait").css('display', 'block');
        $("#clinical-data-table-div").css('display','none');
        setTimeout(function () {
            $("#clinical-data-table-div").css('display','block');
            StudyViewClinicalTabController.init();
            $("#clinical-data-table-loading-wait").css('display', 'none');
            $('#study-tab-clinical-a').addClass("tab-clicked");
        }, 200);
    }
});

$('#study-tab-mutations-a').click(function(){
    if (!$(this).parent().hasClass('ui-state-disabled') && !$(this).hasClass("tab-clicked")) {
        StudyViewMutationsTabController.init();
        $(this).addClass("tab-clicked");
    }
});

$('#study-tab-cna-a').click(function(){
    if (!$(this).parent().hasClass('ui-state-disabled') && !$(this).hasClass("tab-clicked")) {
        StudyViewCNATabController.init();
        $(this).addClass("tab-clicked");
    }
});
</script>

</body>
</html>
