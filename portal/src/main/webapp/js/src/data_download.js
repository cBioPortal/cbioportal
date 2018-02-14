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

var proportionToPercentString = function(p) {
    var percent = 100 * p;
    if (p < 0.03) {
        // if less than 3%, use one decimal figure
        percent = Math.round(10 * percent) / 10;
    } else {
        percent = Math.round(percent);
    }
    return percent + '%';
};
var DataDownloadTab = (function() {

    var _rawDataObj = [],
        _rawStatObj = {};

    var data = [],
        stat = {},
        profiles = {},
        downloadDataModel = {},
        uid_maps = {};
    altered_study_sample_map = {}

    var _isRendered = false;

    var strs = { //strings for the text areas
        alt_freq: "",
        alt_type: "",
        case_affected: "",
        case_matrix: ""
    };

    var calc_alt_freq = function() {
            strs.alt_freq = "GENE_SYMBOL" + "\t" + "NUM_CASES_ALTERED" + "\t" + "PERCENT_CASES_ALTERED" + "\n";
            //TODO: need to change this
            var studySampleMap = window.QuerySession.getStudySampleMap()
            var num_samples = 0;
            Object.keys(studySampleMap).forEach(function(key) {
                num_samples += studySampleMap[key].length
            });
            for (var i=0; i<data.length; i++) {
                var oql = data[i].oql_line;
                var num_altered = data[i].altered_sample_uids.length;
                var percent_altered = proportionToPercentString(num_altered/num_samples);
                strs.alt_freq += oql + "\t" + num_altered + "\t" + percent_altered + "\n";
            }
        },
        calc_alt_type = function() {
            var sample_to_line_to_alt_type = {};
            for (var i=0; i<data.length; i++) {
                var oncoprint_data = data[i].oncoprint_data;
                for (var j=0; j<oncoprint_data.length; j++) {
                    var datum = oncoprint_data[j];
                    var sample = datum.sample;
                    var alt_type = "";
                    sample_to_line_to_alt_type[sample] = sample_to_line_to_alt_type[sample] || [];
                    if (datum.na) {
                        alt_type = "N/S";
                    } else {
                        if (typeof datum.disp_mut !== "undefined") {
                            alt_type += "MUT: ";
                            var mutations = [];
                            for (var k = 0; k < datum.data.length; k++) {
                                if (datum.data[k].genetic_alteration_type === "MUTATION_EXTENDED" &&
					datum.data[k].oncoprint_mutation_type !== "fusion") {
                                    mutations.push(datum.data[k].amino_acid_change);
                                }
                            }
                            alt_type += mutations.join(",");
                            alt_type += ";";
                        }
                        if (typeof datum.disp_cna !== "undefined") {
                            alt_type += datum.disp_cna.toUpperCase() + ";";
                        }
                        if (typeof datum.disp_mrna !== "undefined") {
                            alt_type += datum.disp_mrna.toUpperCase() + ";";
                        }
                        if (typeof datum.disp_prot !== "undefined") {
                            alt_type += "RPPA-" + datum.disp_prot.toUpperCase() + ";";
                        }
			if (typeof datum.disp_fusion !== "undefined") {
			    alt_type += "FUSION: ";
			    var fusions = [];
			    for (var k = 0; k < datum.data.length; k++) {
				if (datum.data[k].genetic_alteration_type === "MUTATION_EXTENDED" &&
					datum.data[k].oncoprint_mutation_type === "fusion") {
				    fusions.push(datum.data[k].amino_acid_change);
				}
			    }
			    alt_type += fusions.join(",");
			    alt_type += ";";
			}
                    }
                    sample_to_line_to_alt_type[sample].push(alt_type);
                }
            }
            strs.alt_type += ["STUDY_ID", "Case ID"].concat(data.map(function(line) { return line.oql_line; })).join("\t") + "\n";
            var studySampleMap = window.QuerySession.getStudySampleMap();
            var studyIds = Object.keys(studySampleMap);
            studyIds = studyIds.sort(function(a,b) {
                return a.localeCompare(b);
            });
            for (var i=0; i<studyIds.length; i++) {
                var sample_ids = studySampleMap[studyIds[i]];
                sample_ids = sample_ids.sort(function(a,b) {
                    return a.localeCompare(b);
                });
                for (var j=0; j<sample_ids.length; j++) {
                    strs.alt_type += studyIds[i] + ":";
                    strs.alt_type += sample_ids[j] + "\t";
                    var alt_types = sample_to_line_to_alt_type[sample_ids[j]];
                    alt_types && (strs.alt_type += alt_types.join("\t"));
                    strs.alt_type += "\n";
                }
            }
        },
        calc_case_affected = function() {
            var studyIds = Object.keys(altered_study_sample_map);
            studyIds = studyIds.sort(function(a,b) {
                return a.localeCompare(b);
            });
            for (var i=0; i<studyIds.length; i++) {
                var sample_ids = Object.keys(altered_study_sample_map[studyIds[i]]);
                sample_ids = sample_ids.sort(function(a,b) {
                    return a.localeCompare(b);
                });
                for (var j=0; j<sample_ids.length; j++) {
                    strs.case_affected += studyIds[i] + ":";
                    strs.case_affected += sample_ids[j] + "\t";
                    strs.case_affected += "\n";
                }
            }
        },
        calc_case_matrix = function() {
            var study_sample_map = window.QuerySession.getStudySampleMap();
            var studyIds = Object.keys(study_sample_map);
            studyIds = studyIds.sort(function(a,b) {
                return a.localeCompare(b);
            });
            for (var i=0; i<studyIds.length; i++) {
                var sample_ids = study_sample_map[studyIds[i]];
                sample_ids = sample_ids.sort(function(a,b) {
                    return a.localeCompare(b);
                });
                var altered_study_sample = altered_study_sample_map[studyIds[i]];
                for (var j=0; j<sample_ids.length; j++) {
                    if (_.keys(altered_study_sample).length > 0) {
                        strs.case_matrix += studyIds[i] + ":";
                        strs.case_matrix += sample_ids[j] + "\t";
                        strs.case_matrix += (altered_study_sample[sample_ids[j]] ? "1" : "0") + "\t";
                        strs.case_matrix += "\n";
                    }
                }
            }
        };

    function processData() {

        //Calculation and configuration of the textarea strings
        calc_alt_freq();
        calc_alt_type();
        calc_case_affected();
        calc_case_matrix();
    }

    function renderDownloadLinks() {
        var _formats = [
            { name: "Tab-delimited Format", value: "tab"},
            { name: "Transposed Matrix", value: "matrix"}
        ];
        var _profileNames = {
            "MUTATION_EXTENDED":"Mutations",
            "COPY_NUMBER_ALTERATION":"Copy-number alterations"
        }

        window.QuerySession.getGeneticProfiles().then(function(response){
            var  _processedProfiles = [];
            $.each(response, function(index,profile){
                profiles[profile.id] = profile;
                var _p = {name:profile.name, profileIds:profile.id, alterationType:profile.genetic_alteration_type, fileName:profile.study_id+'_'+profile.id+'.txt'}
                _processedProfiles.push(_p);
            });

            // if(window.isVirtualStudy) {
            //     _processedProfiles = [];
            //     $.each(_.groupBy(profiles, function(_profile){ return _profile.genetic_alteration_type}), function(k,v){
            //         var _p = {name:_profileNames[k], profileIds:v.map(function(d) { return d.id; }).join(','), alterationType:k,fileName:k.toLowerCase()}
            //         _processedProfiles.push(_p);
            //     });
            // }
            
            // Hide download file links for multi-studies.
            if (QuerySession.getCancerStudyIds().length > 1) {
                $('#data_download_tab_links_li_div').css('display','none');
                $('#data_download_tab_text_areas').css('margin-top','10px');
            } else {
                $.each(_processedProfiles, function(index, val) {
                    var _str = "<li style=\"margin-bottom: 1em;\">" + val.name + ": ";
                    _formats.forEach(function(inner_obj) {
                        _str += "<a href='#' onclick=\"DataDownloadTab.onClickDownload('"+val.profileIds+"','" +val.alterationType+"','" + inner_obj.value + "','" + val.fileName + "')\"> [ " + inner_obj.name + " ]</a>&nbsp;&nbsp;&nbsp;"
                    });
                    _str += "</li>";
                    $("#data_download_links_li").append(_str);
                });
            }
        });

        //configure the download link (link back to the home page download data tab)
        if(!window.QuerySession.isVirtualStudy) {
            var _sample_ids_str = "";
            if (!(window.QuerySession.getCaseSetId() !== "" ||
                window.QuerySession.getCaseIdsKey() !== "" ||
                window.QuerySession.getCaseSetId() !== null ||
                window.QuerySession.getCaseIdsKey() !== null)) {
                $.each(window.QuerySession.getSampleIds(), function(index, val) {
                    _sample_ids_str += val + "+";
                });
                _sample_ids_str = _sample_ids_str.substring(0, (_sample_ids_str.length - 1));
            }
            var _link = "index.do?" +
                "cancer_study_id=" + window.QuerySession.getCancerStudyIds()[0] + "&" +
                "case_ids_key=" + window.QuerySession.getCaseIdsKey() + "&" +
                "case_set_id=" + window.QuerySession.getCaseSetId() + "&" +
                "case_ids=" + _sample_ids_str + "&" +
                "gene_list=" + encodeURIComponent(window.QuerySession.getQueryGenes().join(" ")) + "&" +
                "tab_index=tab_download";
            $("#data_download_redirect_home_page").append(
                "<a href='" + _link + "' target='_blank' style='margin-left:20px;'>Click to download data with other genetic profiles ...</a>");
        }
    }

    function renderTextareas() {
        $("#text_area_gene_alteration_freq").append(strs.alt_freq);
        $("#text_area_gene_alteration_type").append(strs.alt_type);
        $("#text_area_case_affected").append(strs.case_affected);
        $("#text_area_case_matrix").append(strs.case_matrix);
    }
    function tabDelimitedData(_data) {
        var resultData = {'LINE_1':['GENE_ID','COMMON'],'LINE_2':['STUDY_ID','-']};
        //var resultData = {'STUDY_ID':['STUDY_ID','-']};
        var def = new $.Deferred();
        $.when(window.cbioportal_client.getGenes({'hugo_gene_symbols':window.QuerySession.getQueryGenes()})).then(function(genes){
            var genesEntrezIdMap = genes.reduce(function(result, item){
                result[item.hugo_gene_symbol] = item.entrez_gene_id;
                return result;
            },{});
            var geneObject = window.QuerySession.getQueryGenes().reduce(function(result, item) {
                result[item.toUpperCase()] = [item.toUpperCase(),genesEntrezIdMap[item.toUpperCase()]];
                return result;
            }, resultData);
            $.each(_data, function(_studyId,_obj1){
                $.each(_obj1, function(_sampleId,_obj2){
                    resultData['LINE_1'].push(_sampleId);
                    resultData['LINE_2'].push(_studyId);
                    $.each(_obj2, function(_geneId,_obj3){
                        resultData[_geneId].push(_obj3['profile_data']);
                    });
                });
            });
            var content = '';
            $.each(resultData,function(key,downloadRow){
                content += downloadRow.join('\t') + '\r\n' ;
            });

            def.resolve(content);
        }).fail(function(){
            def.reject();
        });

        return def.promise();
    }
    function transposedMatrixData(_data) {
        var resultData = {'LINE_1':['STUDY_ID','GENE_ID'],'LINE_2':['-','COMMON']};
        var _genes = [];
        var def = new $.Deferred();
        $.when(window.cbioportal_client.getGenes({'hugo_gene_symbols':window.QuerySession.getQueryGenes()})).then(function(genes){
            $.each(genes,function(index,item){
                resultData['LINE_1'].push(item.hugo_gene_symbol.toUpperCase());
                _genes.push(item.hugo_gene_symbol.toUpperCase())
                resultData['LINE_2'].push(item.entrez_gene_id);
            });
            $.each(_data, function(_studyId,_obj1){
                $.each(_obj1, function(_sampleId,_obj2){
                    resultData[_sampleId] = [_studyId];
                    resultData[_sampleId].push(_sampleId);
                    $.each(_genes, function(_index,_geneId){
                        resultData[_sampleId].push(_obj2[_geneId]['profile_data']);
                    });
                });
            });
            var content = '';
            $.each(resultData,function(key,downloadRow){
                content += downloadRow.join('\t') + '\r\n' ;
            });
            def.resolve(content);
        }).fail(function(){
            def.reject();
        });

        return def.promise();
    }

    return {
        setOncoprintData: function(_data) {
            data = _data;
        },
        setAlteredStudySampleMap : function(_altered_study_sample_map) {
            altered_study_sample_map = _altered_study_sample_map;
        },
        setUIDMaps: function(_uid_maps) {
            uid_maps = _uid_maps;
        },
        setInput: function(_inputData) {
            _rawDataObj = _inputData;
        },
        setStat: function(_inputData) {
            _rawStatObj = _inputData;
        },
        setProfiles: function(_inputData) {
            profiles = _inputData;
        },
        init: function() {
            processData();
            renderDownloadLinks();
            renderTextareas();
            _isRendered = true;
        },
        isRendered: function() {
            return _isRendered;
        },
        setDownloadModelObject: function(_inputData) {
            downloadDataModel = _inputData;
        },
        onClickDownload: function(_profileIds,_profileType , _formatType, _fileName) {
            var profile_ids = _profileIds.split(',');
            var all_data = [];
            $.when(window.QuerySession.getStudySampleMap()).then(function(studySampleMap){
                var promises = [];
                $.each(profile_ids, function(k,genetic_profile_id){
                    var def = new $.Deferred();
                    promises.push(def)
                    $.when(window.cbioportal_client.getGeneticProfileDataBySample({
                        'genetic_profile_ids': [genetic_profile_id],
                        'genes': window.QuerySession.getQueryGenes().map(function(x) { return x.toUpperCase();}),
                        'sample_ids': studySampleMap[profiles[genetic_profile_id]['study_id']]
                    })).then(function(_data){
                        all_data = all_data.concat(_data);
                        def.resolve();
                    }).fail(function(){
                        def.reject();
                    });
                });

                $.when.apply($, promises).then(function() {
                    var newObject = jQuery.extend(true, {}, downloadDataModel);
                    $.each(all_data,function(key,_data){
                        newObject[_data['study_id']][_data['sample_id']][_data['hugo_gene_symbol']]['profile_data'] = _profileType === 'MUTATION_EXTENDED'?_data['amino_acid_change']:_data['profile_data'];
                    });

                    var downloadOpts = {
                        filename: _fileName,
                        contentType: 'text/plain;charset=utf-8',
                        preProcess: false
                    };
                    $.when(_formatType==='tab'?tabDelimitedData(newObject):transposedMatrixData(newObject)).then(function(content){
                        cbio.download.initDownload(content, downloadOpts);
                    })

                });
            });
        }
    };

}());


var initializeDownload = function(){
    
        downloadInit = true;    
    
        //Sign up getting oncoprint data
        $.when(window.QuerySession.getOncoprintSampleGenomicEventData(), window.QuerySession.getAlteredSampleUIDs(), window.QuerySession.computeUIDMaps()).then(function(oncoprint_data, altered_sample_uids, uid_maps) {

            DataDownloadTab.setOncoprintData(oncoprint_data);
            var alteredStudySampleMap = {};
            var studyIds = Object.keys(uid_maps['case_to_uid']);
            function getMappedStudyId(case_id, case_uid) {
                for (var i=0; i<studyIds.length; i++) {
                    if(uid_maps['case_to_uid'][studyIds[i]][case_id] === case_uid)
                        return studyIds[i];
                }
                return '';
            }

            for (var i=0; i<altered_sample_uids.length; i++) {
                var sampleid = uid_maps['uid_to_case'][altered_sample_uids[i]];
                var studyId = getMappedStudyId(sampleid, altered_sample_uids[i]);
                if(!alteredStudySampleMap[studyId]) {
                    alteredStudySampleMap[studyId] = {}
                }
                alteredStudySampleMap[studyId][sampleid]=true
            }
            DataDownloadTab.setUIDMaps(uid_maps);
            DataDownloadTab.setAlteredStudySampleMap(alteredStudySampleMap);
            var downloadDataModel = {};
            var geneObject = window.QuerySession.getQueryGenes().reduce(function(result, item) {
                result[item.toUpperCase()] = {'profile_data': 'NA'};
                return result;
            }, {});
            $.when(window.QuerySession.getStudySampleMap()).then(function(studySampleMap){
                $.each(studySampleMap, function(studyId,sampleIds){
                    downloadDataModel[studyId]={};
                    $.each(sampleIds, function(key,sampleId){
                        downloadDataModel[studyId][sampleId]=geneObject
                    });
                });
                getGeneticProfileCallback(downloadDataModel);
            });

            function getGeneticProfileCallback(result) {
                DataDownloadTab.setDownloadModelObject(result);
                //DataDownloadTab.init();
                //Bind tab clicking event listener
                $("#tabs").bind("tabsactivate", function(event, ui) {
                    if (ui.newTab.text().trim().toLowerCase() === "download") {
                        if (!DataDownloadTab.isRendered()) {
                            DataDownloadTab.init();
                        }
                    }
                });
                if ($("#data_download").is(":visible")) {
                    if (!DataDownloadTab.isRendered()) {
                        DataDownloadTab.init();
                    }
                }
            }
        });
    
};



var downloadInit = false;

$(document).ready(function(){
    if ($("#data_download").is(":visible")) {
        if (downloadInit === false) {
            fireQuerySession();
            initializeDownload();
        }
    }

    $("#tabs").bind("tabsactivate", function(event, ui) {
        if ($("#data_download").is(":visible")) {
            if (downloadInit === false) {
                fireQuerySession();
                initializeDownload();
            }
        }
    });
    
    
});



