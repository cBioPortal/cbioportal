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

    function init(divName, entityX, entityY, pearson, spearman, profile1Id, profile2Id)  {
        getAlterationData(divName, entityX, entityY, pearson, spearman, profile1Id, profile2Id);
    }

    function getAlterationData(divName, entityX, entityY, pearson, spearman, profile1Id, profile2Id) {
        var paramsGetAlterationData = {
            cancer_study_id: window.QuerySession.getCancerStudyIds()[0],
            entity_x: entityX, 
            entity_y: entityY,
            case_set_id: window.QuerySession.getCaseSetId(),
            case_ids_key: window.QuerySession.getCaseIdsKey(),
            entity_x_profile: profile1Id,
            entity_y_profile: profile2Id
        };
        //Check if the comparison contains gene sets
        entityXIsGeneset = false;
        entityYIsGeneset = false;
        if (profile1Id.indexOf("mrna") == -1) {
        	entityXIsGeneset = true;
        }
        if (profile2Id.indexOf("mrna") == -1) {
        	entityYIsGeneset = true;
        }
        $.post(
            "getAlterationData.json", 
            paramsGetAlterationData, 
            getAlterationDataCallBack(divName, entityX, entityY, pearson, spearman, entityXIsGeneset, entityYIsGeneset), 
            "json");
    }

    function getAlterationDataCallBack(_divName, _entityX, _entityY, _pearson, _spearman, entityXIsGeneset, entityYIsGeneset) {
        return function(result) {
            var alteration_data_result = jQuery.extend(result, {}, true);
            //get mutation data only for the genes (and not for the gene sets)
            if (CoExpView.has_mutation_data()) {
            	var proxy = DataProxyFactory.getDefaultMutationDataProxy();
                var _genes = null;
            	if (entityXIsGeneset) {
            		if (entityYIsGeneset) { //Both are Genesets: do a pseudo callback as if there were no mutations
            			pseudo_callback(
                                alteration_data_result, 
                                _divName, 
                                _entityX, 
                                _entityY, 
                                _pearson, 
                                _spearman
                            );
            		} else { //EntityX is Gene Set and EntityY is Gene
            			_genes = _entityY;
            			proxy.getMutationData(
            					_genes, 
                                getMutationDataCallBack(
                                    alteration_data_result, 
                                    _divName, 
                                    _entityX, 
                                    _entityY, 
                                    _pearson, 
                                    _spearman
                                )
                            );
            		}
            	} else {
            		if (entityYIsGeneset) { //EntityX is Gene and EntityY is Gene Set
            			_genes = _entityX;
            			proxy.getMutationData(
                        		_genes, 
                                getMutationDataCallBack(
                                    alteration_data_result, 
                                    _divName, 
                                    _entityX, 
                                    _entityY, 
                                    _pearson, 
                                    _spearman
                                )
                            );
            		} else { //Both are genes
                        var _genes = _entityX + " " + _entityY;
                        proxy.getMutationData(
                        		_genes, 
                                getMutationDataCallBack(
                                    alteration_data_result, 
                                    _divName, 
                                    _entityX, 
                                    _entityY, 
                                    _pearson, 
                                    _spearman
                                )
                            );
            		}
            	}                
            } else {
                pseudo_callback(
                    alteration_data_result, 
                    _divName, 
                    _entityX, 
                    _entityY, 
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
            coexpPlotsView.init(_divName, _geneX, _geneY, CoexpPlotsProxy.getData(), CoexpPlotsProxy.getDataAttr(), entityXIsGeneset, entityYIsGeneset);
        };
    }
    
    function pseudo_callback(_alteration_data_result, _divName, _geneX, _geneY, _pearson, _spearman) {
        CoexpPlotsProxy.init(_alteration_data_result, _geneX, _geneY, _pearson, _spearman);
        var coexpPlotsView = new CoexpPlotsView();
        coexpPlotsView.init(_divName, _geneX, _geneY, CoexpPlotsProxy.getData(), CoexpPlotsProxy.getDataAttr(), entityXIsGeneset, entityYIsGeneset);
    }

    return {
        init: init
    };

};