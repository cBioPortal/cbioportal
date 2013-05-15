<%@ page import="org.mskcc.cbio.portal.util.SkinUtil" %>

<div id="footer">
<a href="http://cbioportal.org">cBioPortal</a> |
<a href="http://www.mskcc.org/mskcc/html/44.cfm">MSKCC</a> |
<a href="http://cancergenome.nih.gov/">TCGA</a>
<br/>
Questions and feedback:  <%= SkinUtil.getEmailContact() %>
<% if (SkinUtil.getAppName().equalsIgnoreCase("public-portal")) { %>
 | <a href="http://groups.google.com/group/cbioportal">User discussion group</a>
<% } %>
</div>