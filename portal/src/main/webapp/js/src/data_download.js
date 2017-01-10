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
	altered_samples = [];
        
    var _isRendered = false;

    var strs = { //strings for the text areas
        alt_freq: "",
        alt_type: "",
        case_affected: "",
        case_matrix: ""
    };

    var calc_alt_freq = function() {
            strs.alt_freq = "GENE_SYMBOL" + "\t" + "NUM_CASES_ALTERED" + "\t" + "PERCENT_CASES_ALTERED" + "\n";
	    var num_samples = window.QuerySession.getSampleIds().length;
	    for (var i=0; i<data.length; i++) {
		var oql = data[i].oql_line;
		var num_altered = data[i].altered_samples.length;
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
				    if (datum.data[k].genetic_alteration_type === "MUTATION_EXTENDED") {
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
			}
		    sample_to_line_to_alt_type[sample].push(alt_type);
		}
	    }
	    strs.alt_type += ["Case ID"].concat(data.map(function(line) { return line.oql_line; })).join("\t") + "\n";
	    var sample_ids = window.QuerySession.getSampleIds();
	    sample_ids = sample_ids.sort(function(a,b) {
		return a.localeCompare(b);
	    });
	    for (var i=0; i<sample_ids.length; i++) {
		strs.alt_type += sample_ids[i] + "\t";
		var alt_types = sample_to_line_to_alt_type[sample_ids[i]];
		alt_types && (strs.alt_type += alt_types.join("\t"));
		strs.alt_type += "\n";
	    }
        },
        calc_case_affected = function() {
	    strs.case_affected = altered_samples.sort(function(a,b) { return a.localeCompare(b); }).join("\n");
        },
        calc_case_matrix = function() {
	    var altered_samples_set = {};
	    for (var i=0; i<altered_samples.length; i++) {
		altered_samples_set[altered_samples[i]] = true;
	    }
	    var sample_ids = window.QuerySession.getSampleIds();
	    sample_ids = sample_ids.sort(function(a,b) {
		return a.localeCompare(b);
	    });
	    for (var i=0; i<sample_ids.length; i++ ) {
		strs.case_matrix += sample_ids[i] + "\t" + (altered_samples_set[sample_ids[i]] ? "1" : "0") + "\n";
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

        $.each(window.QuerySession.getGeneticProfileIds(), function(index, val) {
            var _str = "<li>" + profiles[val].NAME + ": "; 
            $.each(_formats, function(inner_index, inner_obj) {
                // var _href_str = "<a href='#' onclick=\"DataDownloadTab.onClick('" + val + "', '" + inner_obj.value + "');\">" + inner_obj.name + "</a>";
                // $("#data_download_links_li").append(_href_str);                 
                var _download_form =
                    "<form name='download_tab_form_" + val + "_" + inner_obj.value + "' style='display:inline-block' action='getProfileData.json' method='post' target='_blank'>" +
                        "<input type='hidden' name='cancer_study_id' value='" + window.QuerySession.getCancerStudyIds()[0] + "'>" +
                        "<input type='hidden' name='case_set_id' value='" + window.QuerySession.getCaseSetId() + "'>" +
                        "<input type='hidden' name='case_ids_key' value='" + window.QuerySession.getCaseIdsKey() + "'>" + 
                        "<input type='hidden' name='genetic_profile_id' value='" + val + "'>" +
                        "<input type='hidden' name='gene_list' value='" + window.QuerySession.getQueryGenes().join(" ") + "'>" +
                        "<input type='hidden' name='force_download' value='true'>" +
                        "<input type='hidden' name='file_name' value='" + window.QuerySession.getCancerStudyIds()[0] + "_" + val + ".txt'>" +
                        "<input type='hidden' name='format' value='"  + inner_obj.value + "'>" +
                        "<a href='#' onclick=\"document.forms['download_tab_form_" + val + "_" + inner_obj.value + "'].submit();return false;\"> [ " + inner_obj.name + " ]</a>" + 
                        "</form>&nbsp;&nbsp;&nbsp;";
                _str += _download_form;                 
            });      
            _str += "</li>";
            $("#data_download_links_li").append(_str);
        });

        //configure the download link (link back to the home page download data tab)
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
                    "gene_list=" + window.QuerySession.getQueryGenes().join(" ") + "&" + 
                    "tab_index=tab_download";
        $("#data_download_redirect_home_page").append(
            "<a href='" + _link + "' target='_blank' style='margin-left:20px;'>Click to download data with other genetic profiles ...</a>");
    }

    function renderTextareas() {
        $("#text_area_gene_alteration_freq").append(strs.alt_freq);
        $("#text_area_gene_alteration_type").append(strs.alt_type);
        $("#text_area_case_affected").append(strs.case_affected);
        $("#text_area_case_matrix").append(strs.case_matrix);
    }

    return {
	setOncoprintData: function(_data) {
	    data = _data;
	},
	setAlteredSamples: function(_samples_list) {
	    altered_samples = _samples_list;
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
        }
    };

}());

$(document).ready( function() {

    //Sign up getting oncoprint data
    $.when(window.QuerySession.getOncoprintSampleGenomicEventData(), window.QuerySession.getAlteredSamples()).then(function(oncoprint_data, altered_samples) {
	DataDownloadTab.setOncoprintData(oncoprint_data);
	DataDownloadTab.setAlteredSamples(altered_samples);
        //AJAX call to grab relevant data
        var _paramsGetProfiles = {
            cancer_study_id: window.QuerySession.getCancerStudyIds()[0]
        };
        $.post("getGeneticProfile.json", _paramsGetProfiles, getGeneticProfileCallback, "json");

        function getGeneticProfileCallback(result) {
            DataDownloadTab.setProfiles(result);
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
});


