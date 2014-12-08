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

(function(){
    var menuApp = angular.module('menu', []);
    menuApp.controller('MenuController', function($scope) {
        
        //init -- retrieve data
        init = function() {
            var paramsGetProfiles = {
                cancer_study_id: window.PortalGlobals.getCancerStudyId(),
                case_set_id: window.PortalGlobals.getCaseSetId(),
                case_ids_key: window.PortalGlobals.getCaseIdsKey(),
                gene_list: window.PortalGlobals.getGeneListString()
            };
            $.post("getGeneticProfile.json", paramsGetProfiles, getGeneticProfileCallback, "json");  
            
            function getGeneticProfileCallback(profileDataResult) {
                //get gene list
                $.each(window.PortalGlobals.getGeneList(), function(index, value) {
                    $scope.gene.options.push({'label': value, 'value': value});
                }); 
                //get profile names & ids
                $.each(profileDataResult[window.PortalGlobals.getGeneList()[0]], function(key, obj) {
                    $scope.profileName.options.push({
                        'label': obj.NAME,
                        'value': obj.STABLE_ID
                    });
                });
                //get clinical attributes
                var paramsGetClinicalAttributes = {
                    cmd : "getAllClinicalData",
                    cancer_study_id: window.PortalGlobals.getCancerStudyId(),
                    case_set_id : window.PortalGlobals.getCaseSetId(),
                    format : "json"
                };
                $.post("webservice.do", paramsGetClinicalAttributes, getClinicalAttrCallBack, "json");
                function getClinicalAttrCallBack(clinicalDataResult) {
                    $.each(clinicalDataResult.attributes, function(key, obj) {
                        $scope.clinicalAttr.options.push(
                            {'label': obj.display_name, 'value': obj.attr_id}
                        );
                    });
                    //register form variables
                    $scope.form = {
                        plotsTypeX: $scope.plotsType.options[0].value,
                        plotsTypeY: $scope.plotsType.options[0].value,
                        profileTypeX: $scope.profileType.options[0].value,
                        profileTypeY: $scope.profileType.options[0].value,
                        profileNameX: $scope.profileName.options[0].value,
                        profileNameY: $scope.profileName.options[0].value,
                        geneX: $scope.gene.options[0].value,
                        geneY: $scope.gene.options[0].value,
                        clinicalAttrX: $scope.clinicalAttr.options[0].value,
                        clinicalAttrY: $scope.clinicalAttr.options[0].value
                    };
                }
            }
        };
        init();
        
        //general options
        $scope.plotsType = {
            'title': "Plots Type",
            "options": [
                {'label': "Genomic Profile", 'value': "genomic_profile" },
                {'label': "Clinical Attributes", 'value': "clinical_attr"}
            ]
        };
        //genomic profile options
        $scope.profileType = {
            'title': "Profile Type",
            "options": [
                {'label': "mRNA", 'value': "mRNA"},
                {'label': "Copy Number", 'value': "copy-number"},
                {'label': "DNA Methylation", 'value': "DNA Methylation"},
                {'label': "RPPA Protein Level", 'value': "RPPA"}
            ]
        };
        $scope.profileName = { 
            'title': "Profile Name",
            "options": []
        };
        $scope.gene = {
            'title': "Gene",
            "options": []
        };
        //clinical attributes options
        $scope.clinicalAttr = {
            "title": "Clinical Attributes",
            "options": []
        };


        
    });

})();

var PlotsTabSidebar = (function() {
    
    function fetchProfileMetaData() {
        var paramsGetProfiles = {
            cancer_study_id: window.PortalGlobals.getCancerStudyId(),
            case_set_id: window.PortalGlobals.getCaseSetId(),
            case_ids_key: window.PortalGlobals.getCaseIdsKey(),
            gene_list: window.PortalGlobals.getGeneListString()
        };
        $.post("getGeneticProfile.json", paramsGetProfiles, getGeneticProfileCallback, "json");  
    }

    function fetchClinicalAttrMetaData(profileMetaDataResult) {
        var paramsGetClinicalAttributes = {
            cmd : "getAllClinicalData",
            cancer_study_id: window.PortalGlobals.getCancerStudyId(),
            case_set_id : window.PortalGlobals.getCaseSetId(),
            format : "json"
        };
        $.post("webservice.do", paramsGetClinicalAttributes, getClinicalAttrCallBack(profileMetaDataResult), "json");
    }

    function mergeMetaData(clinicalAttrMetaDataResult, profileMetaDataResult) {
        console.log(clinicalAttrMetaDataResult);
        console.log(profileMetaDataResult);
    }

    return {
        init: function() {
            fetchProfileMetaData(); //invoke fetch other meta data in a chain
        }
    }

}());
