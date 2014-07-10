<%@ page import="org.mskcc.cbio.portal.stats.OddsRatio" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneWithScore" %>
<%@ page import="org.apache.commons.lang.math.DoubleRange" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.io.IOException" %>

<script type="text/javascript" src="js/src/mutex/dataProxy.js?<%=GlobalProperties.getAppVersion()%>"></script>


<script>
    $(document).ready( function() {
        MutexData.init();
    });    
</script>
