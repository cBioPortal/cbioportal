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
    
    return {
        init: function() {
            
            //init logic
            $("#" + ids.main_view.div).empty();
            appendLoadingImg(ids.main_view.div);

            metaData.fetch(); 
            sidebar.init();
            plotsData.fetch("x");
            plotsData.fetch("y");
            plotsbox.init();
            
            //apply event listening logic
            $( "#" + ids.sidebar.x.div ).bind({
                change: function() {
                    $("#" + ids.main_view.div).empty();
                    appendLoadingImg(ids.main_view.div);
                    plotsData.fetch("x");
                    plotsbox.init();
                }
            });
            $( "#" + ids.sidebar.y.div ).bind({
                change: function() {
                    $("#" + ids.main_view.div).empty();
                    appendLoadingImg(ids.main_view.div);
                    plotsData.fetch("y");
                    plotsbox.init();
                }
            });
            $("#" + ids.sidebar.util.view_switch).bind({
                change: function() {
                    $("#" + ids.main_view.div).empty();
                    appendLoadingImg(ids.main_view.div);
                    plotsData.fetch("x");
                    plotsData.fetch("y");
                    if (isSameGene()) {
                        var stat = plotsData.stat();
                        if (!stat.hasCnaAnno) {
                            $("#" + ids.sidebar.util.view_switch).hide();
                        } else {
                            $("#" + ids.sidebar.util.view_switch).show();
                        }                        
                    } else {
                        $("#" + ids.sidebar.util.view_switch).hide();
                    }
                    plotsbox.init();
                }
            });
            
        }
        
    };
}());
