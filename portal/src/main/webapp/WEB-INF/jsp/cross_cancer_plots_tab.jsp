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

<style>
    #cc-plots-sidebar {
        height: 25px;
        width: 1160px;
    }
    #cc-plots-sidebar h5 {
        margin-left: 20px;
        padding-left: 5px;
        padding-right: 5px;
        display: inline-block;
        margin-bottom: 10px;
        font-weight: bold;
        color: grey;
    }
    #cc_plots_box {
        border: 1px solid #aaaaaa;
        border-radius: 4px;
        margin: 15px;
        width: 1160px;
        height: 840px;
    }
</style>

<table>
    <tr>
        <div id="cc-plots-sidebar">
            <h5>Gene</h5>
            <span id="cc_plots_gene_list_select" onchange="ccPlots.update_gene();">
                <select id="cc_plots_gene_list"></select>
            </span>
            <h5>Sort By</h5>
            <input type="radio" name="cc_plots_profile_order_opt" onchange="ccPlots.update_profile_order()" value="alphabetic" checked/> Cancer Study
            <input type="radio" name="cc_plots_profile_order_opt" onchange="ccPlots.update_profile_order()" value="median"/> Median
            <h5>Log Scale</h5>
            <input type="checkbox" id="cc_plots_log_scale" onchange="ccPlots.toggle_log_scale()" checked/>
        </div>
    </tr>
    <tr>
        <div id="cc_plots_box"><img style='padding:300px;' src='images/ajax-loader.gif'></div>
    </tr>
</table>





