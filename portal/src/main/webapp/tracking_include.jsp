<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<%
    String sentryEndpoint = GlobalProperties.getFrontendSentryEndpoint();
%>
<% if (sentryEndpoint != null && sentryEndpoint != "null") { %>
<script src="https://browser.sentry-cdn.com/4.1.1/bundle.min.js" crossorigin="anonymous"></script>
<script>
Sentry.init({ dsn: '<%=sentryEndpoint.trim()%>' });
</script>   
<% } %> 

        
        