/*
 * Copyright (c) 2016 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License,
 * version 3, or (at your option) any later version.
 *
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

// Oleguer Plantalech and Sander Tan, The Hyve
// December 2016

var genesetsSpec = (function() {

    function appendGenesetsList(axis) {
        var $gene_div = $("<div class='form-inline' style='margin-top: 10px;'></div>");
        $("#" + ids.sidebar[axis].spec_div).append($gene_div);
        var $select_div = $("<div class='form-group'></div>");
        $gene_div.append($select_div);
        $select_div.append("<label for='"+ ids.sidebar[axis].gene + "'><h5>Gene Set</h5></label>");
        $select_div.append("<select id='" + ids.sidebar[axis].gene + "'></select>");
        $.each(window.QuerySession.getQueryGenesets(), function(index, value) {
            $("#" + ids.sidebar[axis].gene).append(
                    "<option value='" + value + "'>" + value + "</option>");
        });

        if (axis === "y") {
            $gene_div.append("<div id='" + ids.sidebar.y.lock_gene + "-div'" +
                    "class='checkbox' style='margin-left: 5px'></div>");
        }

        $("#" + ids.sidebar[axis].gene).change(function() {
            if (axis === "y") {
                regenerate_plots("y");
            } else if (axis === "x") {
                if(document.getElementById(ids.sidebar.y.lock_gene) !== null && document.getElementById(ids.sidebar.y.lock_gene).checked) {
                    regenerate_plots("xy");
                } else {
                    regenerate_plots("x");
                }
            }
        });
    }

    function appendPlotValueList(axis) {
        $("#" + ids.sidebar[axis].spec_div).append("<label for='" + ids.sidebar[axis].profile_name + "'><h5>Plot value</h5></label>");
        $("#" + ids.sidebar[axis].spec_div).append("<select id='" + ids.sidebar[axis].profile_name + "'></select>");
        append();

        function append() {
            $.each(metaData.getGenesetsMeta($("#" + ids.sidebar[axis].gene).val()), function(index, obj) {
                $("#" + ids.sidebar[axis].profile_name).append("<option value='" + obj.id + "'>" + obj.name + "</option>");
            });
        };

        $("#" + ids.sidebar[axis].profile_name).change(function() {
            regenerate_plots(axis);
        });    
    }
    
    function updatePlotValueList(axis) {
        $("#" + ids.sidebar[axis].profile_name).empty();
        append();

        function append() {
            $.each(metaData.getGenesetsMeta($("#" + ids.sidebar[axis].gene).val()), function(index, obj) {
                $("#" + ids.sidebar[axis].profile_name).append(
                          "<option value='" + obj.id + "'>" + obj.name + "</option>");
                
            });
        };

        $("#" + ids.sidebar[axis].profile_name).change(function() {
            regenerate_plots(axis);
        });
    }
    
    return {
        init: function(axis) {
            $("#" + ids.sidebar[axis].spec_div).empty();
            appendGenesetsList(axis);
            appendPlotValueList(axis);
        },
        updatePlotValueList: updatePlotValueList
    };
}());