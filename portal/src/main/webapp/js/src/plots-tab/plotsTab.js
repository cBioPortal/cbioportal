/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

var plotsTab = (function() {
    
    var append_loading_img = function(div) {
        $("#" + div).append("<img style='padding-top:200px; padding-left:300px;' src='images/ajax-loader.gif'>");
    };
    
    var regenerate_plots = function(_axis) {
        $("#" + ids.main_view.div).empty();
        append_loading_img(ids.main_view.div);
        plotsData.fetch(_axis);
        append_view_switch_opt();
        plotsbox.init();        
    };
    
    var mutation_copy_no_view_switch = function() {
        $("#" + ids.main_view.div).empty();
        append_loading_img(ids.main_view.div);
        plotsData.fetch("x");
        plotsData.fetch("y");
        plotsbox.init();        
    };
    
    return {
        init: function() {
            
            $("#" + ids.main_view.div).empty();
            append_loading_img(ids.main_view.div);

            metaData.fetch(); 
            sidebar.init();
            plotsData.fetch("x");
            plotsData.fetch("y");
            plotsbox.init();
            
            $("#" + ids.sidebar.x.data_type).bind("change", function() { regenerate_plots("x"); });
            $("#" + ids.sidebar.x.gene).bind("change", function() { regenerate_plots("x"); });
            $("#" + ids.sidebar.x.profile_type).bind("change", function() { regenerate_plots("x"); });
            $("#" + ids.sidebar.x.profile_name).bind("change", function() { regenerate_plots("x"); });
            //TODO: somehow binding event listener to update log scale doesn't work here -- moved to profile spec init.
            $("#" + ids.sidebar.y.data_type).bind("change", function() { regenerate_plots("y"); });
            $("#" + ids.sidebar.y.gene).bind("change", function() { regenerate_plots("y"); });
            $("#" + ids.sidebar.y.profile_type).bind("change", function() { regenerate_plots("y"); });
            $("#" + ids.sidebar.y.profile_name).bind("change", function() { regenerate_plots("y"); });
            $("#" + ids.sidebar.util.view_switch).bind("change", function() { mutation_copy_no_view_switch(); });
            
        }
        
    };
}());
