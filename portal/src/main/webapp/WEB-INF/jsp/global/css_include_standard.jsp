<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 -
 - This library is distributed in the hope that it will be useful, but WITHOUT
 - ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 - FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 - is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 - obligations to provide maintenance, support, updates, enhancements or
 - modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 - liable to any party for direct, indirect, special, incidental or
 - consequential damages, including lost profits, arising out of the use of this
 - software and its documentation, even if Memorial Sloan-Kettering Cancer
 - Center has been advised of the possibility of such damage.
 --%>

<%--
 - This file is part of cBioPortal.
 -
 - cBioPortal is free software: you can redistribute it and/or modify
 - it under the terms of the GNU Affero General Public License as
 - published by the Free Software Foundation, either version 3 of the
 - License.
 -
 - This program is distributed in the hope that it will be useful,
 - but WITHOUT ANY WARRANTY; without even the implied warranty of
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 - GNU Affero General Public License for more details.
 -
 - You should have received a copy of the GNU Affero General Public License
 - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<!-- Include Global Style Sheets -->
<link rel="icon" href="images/cbioportal_icon.png"/>
<link href="css/responsiveslides.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<link href="css/tipTip.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<link href="css/smoothness/jquery-ui-1.10.3.custom.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<link href="css/bootstrap.min.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<link href="css/bootstrap-dialog.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<link href="css/data_table.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<link href="css/dataTables.tableTools.min.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet" />
<link href="css/ui.dropdownchecklist.themeroller.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet"/>
<link href="css/jquery.qtip.min.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet"/>
<link href="css/chosen.css?<%=GlobalProperties.getAppVersion()%>" type="text/css" rel="stylesheet"/>
<link rel="stylesheet" href="//maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome.min.css">

<%
    String global_style = GlobalProperties.getProperty("global_css");
    String special_style = GlobalProperties.getProperty("special_css");
    if (global_style == null) {
        global_style = "css/global_portal.css?"+GlobalProperties.getAppVersion();
    } else {
        global_style = "css/" + global_style+"?"+GlobalProperties.getAppVersion();
    }
    if (special_style != null) {
        special_style = "css/" + special_style+"?"+GlobalProperties.getAppVersion();
    }
%>
<link href="<%= global_style %>" type="text/css" rel="stylesheet" />
<% if (special_style != null) { %>
<link href="<%= special_style %>" type="text/css" rel="stylesheet" />
<% } %>
