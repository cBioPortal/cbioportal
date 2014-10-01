<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<%
String googleAnalyticsProfileId = GlobalProperties.getGoogleAnalyticsProfileId();
if (googleAnalyticsProfileId!=null && !googleAnalyticsProfileId.isEmpty()) {
%>
<!-- Google Analytics -->
<script type="text/javascript">

  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', '<%=googleAnalyticsProfileId%>']);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();
</script>
<% } %>

<!-- De-obfuscate All Email Addresses -->
<script type="text/javascript">
    <!-- When the document is ready, de-obfuscate the email addresses -->
    $(document).ready(function() {
        $('span.mailme').mailme();
    });
</script>
