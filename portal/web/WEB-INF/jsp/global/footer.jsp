<%@ page import="org.mskcc.portal.util.PhpCache" %><%
    PhpCache cache = PhpCache.getInstance();
    String footer = cache.getFooter();
%>
<div class="left">
<p><small>Build:  1.13 (Feb 25, 2011 9:05 am).</small></p>
</div>

<%= footer %>