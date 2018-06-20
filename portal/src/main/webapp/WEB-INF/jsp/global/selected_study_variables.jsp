<%@page import="java.util.stream.Collectors"%>
<%@page import="com.fasterxml.jackson.databind.ObjectMapper"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.HashMap"%>
<%@page import="com.fasterxml.jackson.core.type.TypeReference"%>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>

<%
    String cancerStudyId = (String)request.getAttribute(QueryBuilder.CANCER_STUDY_ID);
    String cancerStudyIdListStr = (String)request.getAttribute(QueryBuilder.CANCER_STUDY_LIST);
    
    TypeReference<HashMap<String,Set<String>>> typeRef = new TypeReference<HashMap<String,Set<String>>>() {};
    ObjectMapper mapper = new ObjectMapper();
    HashMap<String,Set<String>> StudiesMap = new HashMap<String,Set<String>>();
    if((String)request.getAttribute("STUDY_SAMPLE_MAP") != null)
    		StudiesMap = mapper.readValue((String)request.getAttribute("STUDY_SAMPLE_MAP"), typeRef);
    
    //consider virtual study with one real study as regular study and show all the tabs if caseSetId is -1
    String _sampleSetId = (String) request.getAttribute(QueryBuilder.CASE_SET_ID);
    Boolean isVirtualStudy = StudiesMap.size() > 1;
    if(StudiesMap.size() == 1 && cancerStudyIdListStr != null  && cancerStudyIdListStr.split(",").length>1 && !_sampleSetId.equals("-1")){
    	    isVirtualStudy = true;
    }
    
    String normalizedCancerStudyIdListStr = StudiesMap.keySet().stream().collect(Collectors.joining(","));
    pageContext.setAttribute("normalizedCancerStudyIdListStr", normalizedCancerStudyIdListStr);
%>
<script type="text/javascript">

    window.cancerStudyIdList = '<%=cancerStudyIdListStr%>'; // empty string if single study
    window.cancerStudyId = '<%=cancerStudyId%>'; // if multi-studies, this is always "all"
    window.cohortIdsList = (window.cancerStudyIdList === 'null')? [window.cancerStudyId]: window.cancerStudyIdList.split(',');
    window.isVirtualStudy = <%=isVirtualStudy%>; // true if: vc or multi-studies
</script>
