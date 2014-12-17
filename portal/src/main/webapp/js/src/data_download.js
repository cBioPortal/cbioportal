var DataDownloadTab = (function() {

    var _rawDataObj = [],
        _rawStatObj = {};

    var data = [],
        stat = {},
        profiles = {};
        
    var _isRendered = false;

    var strs = { //strings for the text areas
        alt_freq: "",
        alt_type: "",
        case_affected: "",
        case_matrix: ""
    };

    var calc_alt_freq = function() {
            strs.alt_freq = "GENE_SYMBOL" + "\t" + "NUM_CASES_ALTERED" + "\t" + "PERCENT_CASES_ALTERED" + "\n";
            $.each(stat, function(key, value) {
                strs.alt_freq += key + "\t" + value.total_alter_num + "\t" + value.percent + "%" + "\n";
            });        
        },
        calc_alt_type = function() {
            strs.alt_type = "Case ID" + "\t";
            $.each(Object.keys(stat), function(index, val) {
                strs.alt_type += val + "\t";    
            });
            strs.alt_type += "\n";
            $.each(data, function(outer_index, outer_obj) {
                strs.alt_type += outer_obj.key + "\t";
                $.each(outer_obj.values, function(inner_key, inner_obj) {
                    if (Object.keys(inner_obj).length === 2) {
                        strs.alt_type += "  " + "\t";
                    } else {
                        if (inner_obj.hasOwnProperty("mutation")) {
                            strs.alt_type += "MUT;";
                        }
                        if (inner_obj.hasOwnProperty("cna")) {
                            if (inner_obj.cna === "AMPLIFIED") {
                                strs.alt_type += "AMP;";
                            } else if (inner_obj.cna === "GAINED") {
                                strs.alt_type += "GAIN;";
                            } else if (inner_obj.cna === "HEMIZYGOUSLYDELETED") {
                                strs.alt_type += "HETLOSS;";
                            } else if (inner_obj.cna === "HOMODELETED") {
                                strs.alt_type += "HOMDEL;";
                            }
                        }
                        if (inner_obj.hasOwnProperty("mrna")) {
                            if (inner_obj.mrna === "UPREGULATED") {
                                strs.alt_type += "UP;";
                            } else if (inner_obj.mrna === "DOWNREGULATED") {
                                strs.alt_type += "DOWN;";
                            }
                        }
                        if (inner_obj.hasOwnProperty("rppa")) {
                            if (inner_obj.rppa === "UPREGULATED") {
                                strs.alt_type += "RPPA-UP;";
                            } else if (inner_obj.rppa === "DOWNREGULATED") {
                                strs.alt_type += "RPPA-DOWN;";
                            }
                        }
                        strs.alt_type += "\t";
                    }
                });
                strs.alt_type += "\n";
            });
        },
        calc_case_affected = function() {
            $.each(data, function(outer_index, outer_obj) {
                $.each(outer_obj.values, function(inner_index, inner_obj) {
                    if (Object.keys(inner_obj).length !== 2) {
                        strs.case_affected += outer_obj.key + "\n";
                        return false;
                    }
                });
            });
        },
        calc_case_matrix = function() {
            $.each(data, function(outer_index, outer_obj) {
                var _affected = false;
                $.each(outer_obj.values, function(inner_index, inner_obj) {
                    if (Object.keys(inner_obj).length !== 2) {
                        _affected = true;
                        return false;
                    } 

                });
                if (_affected) strs.case_matrix += outer_obj.key + "\t" + "1" + "\n";
                else strs.case_matrix += outer_obj.key + "\t" + "0" + "\n";
            });
        };

    function processData() {
        //sort the data arra by original sample order
        $.each(window.PortalGlobals.getSampleIds(), function(index, sampleId) {
            $.grep(_rawDataObj, function( n, i ) {
                if (n.key === sampleId) {
                    data.push(n);
                }
            });
        });
        //set status object
        stat = _rawStatObj;

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

        $.each(window.PortalGlobals.getGeneticProfiles().split(" "), function(index, val) {
            var _str = "<li>" + profiles[val].NAME + ": "; 
            $.each(_formats, function(inner_index, inner_obj) {
                // var _href_str = "<a href='#' onclick=\"DataDownloadTab.onClick('" + val + "', '" + inner_obj.value + "');\">" + inner_obj.name + "</a>";
                // $("#data_download_links_li").append(_href_str);                 
                var _download_form =
                    "<form name='download_tab_form_" + val + "_" + inner_obj.value + "' style='display:inline-block' action='getProfileData.json' method='post' target='_blank'>" +
                        "<input type='hidden' name='cancer_study_id' value='" + window.PortalGlobals.getCancerStudyId() + "'>" +
                        "<input type='hidden' name='case_set_id' value='" + window.PortalGlobals.getCaseSetId() + "'>" +
                        "<input type='hidden' name='genetic_profile_id' value='" + val + "'>" +
                        "<input type='hidden' name='gene_list' value='" + window.PortalGlobals.getGeneListString() + "'>" +
                        "<input type='hidden' name='force_download' value='true'>" +
                        "<input type='hidden' name='file_name' value='" + window.PortalGlobals.getCancerStudyId() + "_" + val + ".txt'>" +
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
        if (!(window.PortalGlobals.getCaseSetId() !== "" ||
            window.PortalGlobals.getCaseIdsKey() !== "" ||
            window.PortalGlobals.getCaseSetId() !== null ||
            window.PortalGlobals.getCaseIdsKey() !== null)) {
            $.each(window.PortalGlobals.getSampleIds(), function(index, val) {
                _sample_ids_str += val + "+";
            });
            _sample_ids_str = _sample_ids_str.substring(0, (_sample_ids_str.length - 1));
        }
        var _link = "index.do?" + 
                    "cancer_study_id=" + window.PortalGlobals.getCancerStudyId() + "&" + 
                    "case_ids_key=" + window.PortalGlobals.getCaseIdsKey() + "&" + 
                    "case_set_id=" + window.PortalGlobals.getCaseSetId() + "&" +
                    "case_ids=" + _sample_ids_str + "&" + 
                    "gene_list=" + window.PortalGlobals.getGeneListString()+ "&" + 
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
    PortalDataCollManager.subscribeOncoprint(function() {
        DataDownloadTab.setInput(PortalDataColl.getOncoprintData());
    });
    PortalDataCollManager.subscribeOncoprintStat(function() {
        DataDownloadTab.setStat(PortalDataColl.getOncoprintStat()); 
        //AJAX call to grab relevant data
        var _paramsGetProfiles = {
            cancer_study_id: window.PortalGlobals.getCancerStudyId()
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


