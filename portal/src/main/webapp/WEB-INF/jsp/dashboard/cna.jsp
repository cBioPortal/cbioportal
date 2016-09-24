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


<%@ page import="org.mskcc.cbio.portal.servlet.GisticJSON" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<div id="gistic-msg"><img src="images/ajax-loader.gif" alt="loading" /></div><br/>
<table cellpadding="0" cellspacing="0" border="0" id="gistic_wrapper_table" width="100%">
    <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="gistic_table">
                <thead>
                    <tr valign="bottom">
                        <th><b>data</b></th>
                        <th><b><font color="red">AMP</font>/<font color="blue">DEL</font></b></th>
                        <th><b>Chr</b></th>
                        <th><b>Cytoband</b></th>
                        <th><b># Genes</b></th>
                        <th><b>Genes</b></th>
                        <th><b>Q-value</b></th>
                    </tr>
                </thead>
            </table>
        </td>
    </tr>
</table>