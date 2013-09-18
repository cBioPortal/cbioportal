<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<div id="footer">
<a href="http://cbioportal.org">cBioPortal</a> |
<a href="http://www.mskcc.org/mskcc/html/44.cfm">MSKCC</a> |
<a href="http://cancergenome.nih.gov/">TCGA</a>
<br/>
Questions and feedback:  <%= GlobalProperties.getEmailContact() %>
<% if (GlobalProperties.getAppName().equalsIgnoreCase("public-portal")) { %>
 | <a href="http://groups.google.com/group/cbioportal">User discussion group</a>
<% } %>
</div>