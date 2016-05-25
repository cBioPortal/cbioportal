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
            window.PlotsTab = {};
            clear_plot_box();
            if(window.QuerySession.getCancerStudyIds().length>1){
                profileSpec.appendStudies().then(function(){
                    $("#" + ids.sidebar.study.study).change(function() {
                        window.PlotsTab.cancerStudyId = $("#" + ids.sidebar.study.study).val();
                        window.PlotsTab.caseIds = window.QuerySession.getStudySampleMap()[window.PlotsTab.cancerStudyId];
                        window.PlotsTab.CaseSetId =  '';
                        window.PlotsTab.CaseIdsKey = '';

                         $("#" + ids.sidebar.x.spec_div).empty();
                        $("#" + ids.sidebar.y.spec_div).empty();
                        $("#" + ids.sidebar.x.data_type).show();
                        $("#" + ids.sidebar.y.data_type).show();
                        $("input:radio[name='" + ids.sidebar.x.data_type + "'][value='" + vals.data_type.genetic + "']").attr('checked', 'checked');
                        $("input:radio[name='" + ids.sidebar.y.data_type + "'][value='" + vals.data_type.genetic + "']").attr('checked', 'checked');
                        DataProxyFactory.clearDefaultMutationDataProxy();
                        plotsTabInit();
                    });
                    window.PlotsTab.cancerStudyId = $("#" + ids.sidebar.study.study).val();
                    window.PlotsTab.caseIds = window.QuerySession.getStudySampleMap()[window.PlotsTab.cancerStudyId];
                    window.PlotsTab.CaseSetId =  '';
                    window.PlotsTab.CaseIdsKey = '';
                    plotsTabInit();
                })
            }else{
                window.PlotsTab.cancerStudyId = window.QuerySession.getCancerStudyIds()[0];
                window.PlotsTab.caseIds = window.QuerySession.getStudySampleMap()[window.PlotsTab.cancerStudyId];
                window.PlotsTab.CaseSetId =  window.QuerySession.getCaseSetId();
                window.PlotsTab.CaseIdsKey = window.QuerySession.getCaseIdsKey();
                plotsTabInit()
            }

            function plotsTabInit(){
                clear_plot_box();
                metaData.fetch(
                    //fetch data, and then continue with callback below:
                    function () { 
                        sidebar.init();
                        plotsData.fetch("x", function () {
                            plotsData.fetch("y", plotsbox.init);
                        });
                    });
            }
        }
        
    };
}());
