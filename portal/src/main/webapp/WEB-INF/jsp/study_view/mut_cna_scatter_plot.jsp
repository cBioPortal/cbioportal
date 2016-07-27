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

<style type="text/css">
.large-plot-div {
    width:540px;
    height:400px;
    display:block;
}
</style>

<fieldset style="padding:0px 1px">
    <legend style="color:blue;font-weight:bold;">Mutation Count vs CNA</legend>
    <div id="case-id-div" style="float:right;position:relative;z-index:1;margin-right:10px;"></div>
    <div id="mut-cna-scatter-plot" class="large-plot-div">
        <img src="images/ajax-loader.gif" alt="loading" />
    </div>
    <table style="display:none;width:100%;" class="mut-cna-config">
        <tr width="100%">
                <td align="left">
                    H-Axis scale: <input type="radio" name="mut-cna-haxis-log" class="mut-cna-axis-log" value="normal" checked="checked"/>linear &nbsp;
                    <input type="radio" name="mut-cna-haxis-log" class="mut-cna-axis-log" value="log" id="mut-cna-haxis-log"/>log<br/>
                    V-Axis scale: <input type="radio" name="mut-cna-vaxis-log" class="mut-cna-axis-log" value="normal" checked="checked"/>linear &nbsp;
                    <input type="radio" name="mut-cna-vaxis-log" class="mut-cna-axis-log" value="log" id="mut-cna-vaxis-log"/>log
                </td>
                <td id="case-id-div" align="right">
                </td>
        </tr>
    </table>
</fieldset>