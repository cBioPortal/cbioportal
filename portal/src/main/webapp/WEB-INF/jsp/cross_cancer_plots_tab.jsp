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
        width: 220px;
    }

    #cc-plots-sidebar-y-div {
        width: inherit;
        height: 222px;
    }

    #cc-plots-sidebar-util-div {
        width: inherit;
        height: 412px;
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
    }

    #cc-plots-sidebar select {
        max-width: 180px;
    }

    #cc-plots-box {
        width: 950px;
        height: 670px;
        float: right;
    }

    #cc-plots-sidebar input[type="text"] {
        width: 150px;
        display: inline;
        margin-bottom: 20px;
        margin-left: 30px;
    }

</style>


<table>
    <tr>
        <td>
            <div id="cc-plots-sidebar">
                <div id="cc-plots-sidebar-y-div" class="cc-plots">
                    <h4>Vertical Axis</h4>
                    <h5>Gene</h5><span id="cc_plots_gene_list_select" onchange="ccPlots.update();"></span>
                </div>
                <div id="cc-plots-sidebar-util-div" class="cc-plots">
                    <h4>Utilities</h4>
                    <h5>Search Case(s)</h5>
                    <input type="text" id="case_id_search_keyword" name="case_id_search_keyword"
                           placeholder="Case ID.." onkeyup="search_case_id();"><br>
                    <h5>Search Mutation(s)</h5>
                    <input type="text" id="mutation_search_keyword"
                           name="mutation_search_keyword" placeholder="Protein Change.."
                           onkeyup="search_mutation();"><br>
                    <div id="mutation_details_vs_gistic_view" class="mutation_details_vs_gistic_view"
                         style="display:inline;"></div>
                    <h5>Download</h5>
                    <div id="cc_plots_download_buttons" style="display: inline;">
                        <br><button id='cc_plots_svg_download' style="margin-left:30px;">SVG</button>
                        <button id='cc_plots_pdf_download'>PDF</button>
                        <button id='cc_plots_data_download'>Data</button>
                    </div>

                </div>
            </div>
        </td>
        <td>
            <div id="cc-plots-box" class="cc-plots" style="overflow: scroll;">
                <img src="images/ajax-loader.gif" id="cc_plots_loading" style="padding:200px;"/>
            </div>
        </td>
    </tr>
</table>





