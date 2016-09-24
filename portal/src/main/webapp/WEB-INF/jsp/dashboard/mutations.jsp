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


<%@ page import="org.mskcc.cbio.portal.servlet.MutSigJSON" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>  

<div id="mut-sig-msg"><img src="images/ajax-loader.gif" alt="loading" /></div><br/>
<table cellpadding="0" cellspacing="0" border="0" id="smg_wrapper_table" width="60%">
    <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="smg_table">
                <thead>
                    <tr valign="bottom">
                        <th><b>data</b></th>
                        <th><b>Gene</b></th>
                        <th><b>Cytoband</b></th>
                        <th><b>Gene size (Nucleotides)</b></th>
                        <th><b># Mutations</b></th>
                        <th><b># Mutations / Nucleotide</b></th>
                        <th><b>Mutsig Q-value</b></th>
                    </tr>
                </thead>
            </table>
        </td>
    </tr>
</table>