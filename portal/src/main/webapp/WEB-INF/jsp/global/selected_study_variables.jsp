<%@page import="java.util.stream.Collectors"%>
<%@page import="com.fasterxml.jackson.databind.ObjectMapper"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.HashMap"%>
<%@page import="com.fasterxml.jackson.core.type.TypeReference"%>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>

<%
    Boolean isVirtualStudy = (Boolean)request.getAttribute("is_virtual_study");
    String cancerStudyId = (String)request.getParameter(QueryBuilder.CANCER_STUDY_ID);
    String cancerStudyIdListStr = (String)request.getAttribute(QueryBuilder.CANCER_STUDY_LIST);
    
    TypeReference<HashMap<String,Set<String>>> typeRef = new TypeReference<HashMap<String,Set<String>>>() {};
    ObjectMapper mapper = new ObjectMapper();
    HashMap<String,Set<String>> StudiesMap = mapper.readValue((String)request.getAttribute("STUDY_SAMPLE_MAP"), typeRef); 
    
    String normalizedCancerStudyIdListStr = StudiesMap.keySet().stream().collect(Collectors.joining(","));
    pageContext.setAttribute("normalizedCancerStudyIdListStr", normalizedCancerStudyIdListStr);
%>
<script type="text/javascript">

    var isVirtualStudy = <%=isVirtualStudy%>;
    var cancerStudyIdList = '<%=cancerStudyIdListStr%>'; // empty string if single study
    var cancerStudyId = '<%=cancerStudyId%>'; // if multi-studies, this is always "all"
    window.cohortIdsList = (cancerStudyIdList === 'null')? [cancerStudyId]: cancerStudyIdList.split(',');
    window.isVirtualStudy = <%=isVirtualStudy%>; // true if: vc or multi-studies
</script>