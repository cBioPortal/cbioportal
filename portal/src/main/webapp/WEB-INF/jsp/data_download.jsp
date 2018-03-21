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

<%@ page import="org.mskcc.cbio.portal.model.GeneWithScore" %>
<%@ page import="org.mskcc.cbio.portal.util.ValueParser" %>
<%@ page import="java.util.TreeSet" %>
<%@ page import="org.mskcc.cbio.portal.model.DownloadLink" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>

<div class="section" id="data_download">
</div><!-- end data download div -->

<script>

    $(document).ready( function() {
        //whether this tab has already been initialized or not:
        var tab_init = false;
        //function that will listen to tab changes and init this one when applicable:
        function tabsUpdate() {
            if ($("#data_download").is(":visible")) {
                if (tab_init === false) {
                    window.onReactAppReady(function(){
                        window.renderDownloadTab(document.getElementById('data_download'));
                    });
                    tab_init = true;
                }
            }
        }
        
        //this is for the scenario where the tab is open by default (as part of URL >> #tab_name at the end of URL),
        tabsUpdate();

        //this is for the scenario where the user navigates to this tab:
        $("#tabs").bind("tabsactivate", function(event, ui) {
            tabsUpdate();
        });
    });

</script>


<%
    String debugStr = request.getParameter("xdebug");
    boolean debug = false;
    if (debugStr != null && debugStr.equals("1")) {
        debug = true;
    }
    String textOnly = request.getParameter("text_only");
%>
