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
    #cc-plots .cc-plots {
        border: 1px solid #aaaaaa;
        border-radius: 4px;
        margin: 15px;
    }

    #cc-plots-sidebar {
        height: 25px;
        width: 1160px;
    }

    #cc-plots-sidebar h4 {
        margin: 15px;
        font-size: 12px;
        color: grey;
        background-color: white;
        margin-top: -6px;
        display: table;
        padding: 5px;
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

    #cc-plots-sidebar select {
        max-width: 180px;
    }

    #cc-plots-box {
        width: 1160px;
        height: 640px;
    }

</style>

<table>
    <tr>
        <div id="cc-plots-sidebar">
            <h5>Gene</h5>
            <span id="cc_plots_gene_list_select" onchange="ccPlots.update_gene();">
                <select id="cc_plots_gene_list"></select>
            </span>
            <h5>Log Scale</h5>
            <input type="checkbox" id="cc_plots_log_scale" onchange="ccPlots.toggle_log_scale()" checked/>
            <h5>Download</h5>
            <button id='cc_plots_svg_download'>SVG</button>
            <button id='cc_plots_pdf_download'>PDF</button>
            <button id='cc_plots_data_download'>Data</button>
            <h5>Sort By</h5>
            <input type="radio" name="cc_plots_profile_order_opt" onchange="ccPlots.update_profile_order()" value="alphabetic" checked/> Alphabetical
            <input type="radio" name="cc_plots_profile_order_opt" onchange="ccPlots.update_profile_order()" value="median"/> Median
            <h5>Show Mutations</h5>
            <input type="checkbox" id="cc_plots_show_mut" onchange="ccPlots.toggle_show_mut()" checked/>
            <h5>Show Sequenced Samples Only</h5>
            <input type="checkbox" id="cc_plots_show_sequenced_only" onchange="ccPlots.toggle_show_sequenced()"/>
        </div>
    </tr>
    <tr>
        <div id="cc-plots-box" class="cc-plots" style="overflow: scroll;">
            <img src="images/ajax-loader.gif" id="cc_plots_loading" style="padding:200px;"/>
        </div>
    </tr>
</table>





