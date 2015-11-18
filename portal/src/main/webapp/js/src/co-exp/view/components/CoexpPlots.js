/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
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


/**
 *
 * JS functions for generating the detailed plots for selected row
 * under co-expression view, mostly for grabbing data (through two AJAX calls)s
 * send to Data to "formatting" proxy (CoexpPlotsProxy)
 * and init the overall view (CoexpPlotsView).
 *
 * @author: yichao S
 * @date: Jan 2014
 *
 */

var CoexpPlots = function() {

    function init(divName, geneX, geneY, pearson, spearman, profileId)  {
        getAlterationData(divName, geneX, geneY, pearson, spearman, profileId);
    }

    function getAlterationData(divName, geneX, geneY, pearson, spearman, profileId) {
        var paramsGetAlterationData = {
            cancer_study_id: window.QuerySession.getCancerStudyIds()[0],
            gene_list: geneX + " " + geneY,
            case_set_id: window.QuerySession.getCaseSetId(),
            case_ids_key: window.QuerySession.getCaseIdsKey(),
            profile_id: profileId
        };
        $.post(
            "getAlterationData.json", 
            paramsGetAlterationData, 
            getAlterationDataCallBack(divName, geneX, geneY, pearson, spearman), 
            "json");
    }

    function getAlterationDataCallBack(_divName, _geneX, _geneY, _pearson, _spearman) {
        return function(result) {
            var alteration_data_result = jQuery.extend(result, {}, true);
            //get mutation data
            if (CoExpView.has_mutation_data()) {
                var proxy = DataProxyFactory.getDefaultMutationDataProxy();
                var _genes = _geneX + " " + _geneY;
                proxy.getMutationData(
                    _genes, 
                    getMutationDataCallBack(
                        alteration_data_result, 
                        _divName, 
                        _geneX, 
                        _geneY, 
                        _pearson, 
                        _spearman
                    )
                );                
            } else {
                pseudo_callback(
                    alteration_data_result, 
                    _divName, 
                    _geneX, 
                    _geneY, 
                    _pearson, 
                    _spearman
                );
            }

        };
    }

    function getMutationDataCallBack(_alteration_data_result, _divName, _geneX, _geneY, _pearson, _spearman) {
        return function(result) {
            CoexpPlotsProxy.init(_alteration_data_result, _geneX, _geneY, _pearson, _spearman);
            var coexpPlotsView = new CoexpPlotsView();
            coexpPlotsView.init(_divName, _geneX, _geneY, CoexpPlotsProxy.getData(), CoexpPlotsProxy.getDataAttr());
        };
    }
    
    function pseudo_callback(_alteration_data_result, _divName, _geneX, _geneY, _pearson, _spearman) {
        CoexpPlotsProxy.init(_alteration_data_result, _geneX, _geneY, _pearson, _spearman);
        var coexpPlotsView = new CoexpPlotsView();
        coexpPlotsView.init(_divName, _geneX, _geneY, CoexpPlotsProxy.getData(), CoexpPlotsProxy.getDataAttr());
    }

    return {
        init: init
    };

};