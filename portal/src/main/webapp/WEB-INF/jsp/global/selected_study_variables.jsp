<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>

<%
    Boolean isVirtualStudy = (Boolean)request.getAttribute("is_virtual_study");
    String cancerStudyId = (String)request.getParameter(QueryBuilder.CANCER_STUDY_ID);
    String cancerStudyIdListStr = (String)request.getAttribute(QueryBuilder.CANCER_STUDY_LIST);
    String normalizedCancerStudyIdListStr;
    if (cancerStudyIdListStr == null) {
            normalizedCancerStudyIdListStr = cancerStudyId;
    } else {
            normalizedCancerStudyIdListStr = cancerStudyIdListStr;
    }
    pageContext.setAttribute("normalizedCancerStudyIdListStr", normalizedCancerStudyIdListStr);
%>
<script type="text/javascript">

    var isVirtualStudy = <%=isVirtualStudy%>;
    var cancerStudyIdList = '<%=cancerStudyIdListStr%>'; // empty string if single study
    var cancerStudyId = '<%=cancerStudyId%>'; // if multi-studies, this is always "all"
    window.cohortIdsList = (cancerStudyIdList === 'null')? [cancerStudyId]: cancerStudyIdList.split(',');
    window.isVirtualStudy = <%=isVirtualStudy%>; // true if: vc or multi-studies
</script>