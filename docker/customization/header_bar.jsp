<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<table width="100%" cellspacing="0px" cellpadding="2px" border="0px">
        <tr valign="middle">
                <td valign="middle" width="25%">
                    <img src="images/cbioportal_logo.png" height="50px" alt="cBioPortal Logo">
                </td>
                <td valign="middle" align="center" width="50%">
                        <img src="images/cbiologo.png" alt="Custom Branding Logo" style="max-height: 50px;">
                </td>
                <td valign="middle" align="right" width="25%">
                        <a href="http://www.mskcc.org"><img src="images/mskcc_logo_3d_grey.jpg" height="50px" alt="MSKCC Logo"></a>
                </td>
        </tr>
    <%
       if (GlobalProperties.usersMustAuthenticate()) {
    %>
    <% } %>
</table>

