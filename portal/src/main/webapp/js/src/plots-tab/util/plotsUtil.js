var clinical_attr_is_discretized = function(_axis) {
    var _type = metaData.getClinicalAttrType($("#" + ids.sidebar[_axis].clin_attr).val());
    if (_type === "STRING") return true;
    else return false;
};

var isEmpty = function(inputVal) {
    if (inputVal !== "NaN" && inputVal !== "NA") {
        return false;
    }
    return true;
};

var isSameGene = function () {
    var elt_x = document.getElementById(ids.sidebar.x.gene);
    var elt_y = document.getElementById(ids.sidebar.y.gene);
    if (elt_x.options[elt_x.selectedIndex].value === elt_y.options[elt_y.selectedIndex].value) {
        return true;
    } 
    return false;
};

var isTwoGenes = function () {
    var elt_x = document.getElementById(ids.sidebar.x.gene);
    var elt_y = document.getElementById(ids.sidebar.y.gene);
    if (elt_x.options[elt_x.selectedIndex].value !== elt_y.options[elt_y.selectedIndex].value) {
        return true;
    } 
    return false;
};

var genetic_vs_genetic = function() {
    if ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === $("input:radio[name='" + ids.sidebar.y.data_type + "']:checked").val() && 
        $("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.genetic) {
        return true;
    } return false;   
};

var genetic_vs_clinical = function() {
    var _type_x = $("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val();
    var _type_y = $("input:radio[name='" + ids.sidebar.y.data_type + "']:checked").val();
    if (_type_x !== _type_y) {
        return true;
    } return false;
};

var clinical_vs_clinical = function() {
    if ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === $("input:radio[name='" + ids.sidebar.y.data_type + "']:checked").val() && 
        $("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.clin) {
        return true;
    } return false;
};

var getXGene = function() {
    var elt_x = document.getElementById(ids.sidebar.x.gene);
    var _gene_symbol = elt_x.options[elt_x.selectedIndex].value;
    return _gene_symbol;
};

var getYGene = function() {
    var elt_y = document.getElementById(ids.sidebar.y.gene);
    var _gene_symbol = elt_y.options[elt_y.selectedIndex].value;
    return _gene_symbol;
};

var mutationTranslator = function(mutationDetail) {
    
     vocabulary = {  
            frameshift : {
                type : "Frameshift",
                vals: [
                    "Frame_Shift_Del", 
                    "Frame_Shift_Ins", 
                    "frameshift insertion", 
                    "frameshift", 
                    "frameshift_insertion", 
                    "Frameshift deletion",
                    "FRAMESHIFT_CODING"
                ]
            },
            nonsense : {
                type : "Nonsense",
                vals: ["Nonsense_Mutation", "Nonsense"]
            },
            splice : {
                type : "Splice",
                vals : [
                    "Splice_Site", 
                    "Splice_Site_SNP", 
                    "splicing", 
                    "splice", 
                    "ESSENTIAL_SPLICE_SITE"
                ]
            },
            in_frame : {
                type : "In_frame",
                vals : [
                    "In_Frame_Del", 
                    "In_Frame_Ins"
                ]
            },
            nonstart : {
                type : "Nonstart",
                vals : ["Translation_Start_Site"]
            },
            nonstop : {
                type : "Nonstop",
                vals : ["NonStop_Mutation"]
            },
            missense : {
                type : "Missense",
                vals : [
                    "Missense_Mutation", 
                    "Missense"
                ]
            },
            other: {
                type : "Other",
                vals : [
                    "COMPLEX_INDEL", 
                    "5'Flank", 
                    "Fusion", 
                    "vIII deletion", 
                    "Exon skipping", 
                    "exon14skip"
                ]
            }
        };
        
    for(var key in vocabulary) {
        if ($.inArray(mutationDetail, vocabulary[key].vals) !== -1) {
            return vocabulary[key].type;
        }
    }
    return vocabulary.other.type; //categorize all other mutations as other 

};

var discretized_cna_profile_keywords = [
    "_cna",
    "_cna_rae",
    "_gistic",
    "_cna_consensus"
];

var is_profile_discretized = function(axis) {
    var elt = document.getElementById(ids.sidebar[axis].profile_name);
    var _profile_name = elt.options[elt.selectedIndex].value;
    var _token = _profile_name.replace(window.PortalGlobals.getCancerStudyId(), "");
    //if ($.inArray(_token, discretizedDataList) !== -1) return true;
    if ($.inArray(_token.toLowerCase(), discretized_cna_profile_keywords) !== -1) return true;
    return false;
};

function searchIndexBottom(arr, ele) {
    for(var i = 0; i < arr.length; i++) {
        if (parseFloat(ele) > parseFloat(arr[i])) {
            continue ;
        } else if (parseFloat(ele) === parseFloat(arr[i])) {
            return i;
        } else {
            return i - 1;
        }
    }
    return arr.length - 1 ;
};

function searchIndexTop(arr, ele) {
    for(var i = 0; i < arr.length; i++) {
        if (ele <= arr[i]) {
            return i;
        } else {
            continue;
        }
    }
    return arr.length - 1;
};

function bubble_up(_arr, _index) {
    for (var i = _index; i > 0; i--) {
        var _tmp_obj = _arr[i - 1];
        _arr[i - 1] = _arr[i];
        _arr[i] = _tmp_obj;
    }
}

var append_loading_img = function(div) {
    $("#" + div).append("<img style='padding-top:200px; padding-left:300px;' src='images/ajax-loader.gif'>");
};

var clear_plot_box = function() {
    $("#" + ids.main_view.div).empty();
    append_loading_img(ids.main_view.div);
};

var regenerate_plots = function(_axis) {
    if (_axis === "x" || _axis === "y") {
        clear_plot_box();
        optSpec.init();
        plotsData.fetch(_axis);
        plotsbox.init();
    } else if (_axis === "xy") {
        clear_plot_box();
        optSpec.init();
        plotsData.fetch("x");
        plotsData.fetch("y");
        plotsbox.init();

    }

};

var search_mutation = function() {
    var searchToken = document.getElementById("mutation_search_keyword").value;
    d3.select("#" + ids.main_view.div).selectAll("path").each(
        function() {
            var mutation_details = $(this).attr("mutation_details");
            if (typeof mutation_details !== 'undefined' && mutation_details !== false && mutation_details !== "domain") {
                if ( searchToken.length >= 3 ) {
                    if (mutation_details.toUpperCase().indexOf(searchToken.toUpperCase()) !== -1) {
                        $(this).attr("d", d3.svg.symbol()
                            .size(d3.select(this).attr("size") + 5)
                            .type(d3.select(this).attr("shape")));
                    } else {
                        $(this).attr("d", d3.svg.symbol()
                            .size(d3.select(this).attr("size"))
                            .type(d3.select(this).attr("shape")));
                    }
                } else {
                    $(this).attr("d", d3.svg.symbol()
                        .size(d3.select(this).attr("size"))
                        .type(d3.select(this).attr("shape")));
                }
            }
        }
    );   
};

var search_case_id = function() {
    var searchToken = document.getElementById("case_id_search_keyword").value;
    d3.select("#" + ids.main_view.div).selectAll("path").each(
        function() {
            var _case_id = $(this).attr("case_id");
            if (typeof _case_id !== 'undefined' && _case_id !== false && _case_id !== "domain") {
                if ( searchToken.length >= 4 ) {
                    if ( _case_id.toUpperCase().indexOf(searchToken.toUpperCase()) !== -1 &&
                        (searchToken.toUpperCase()) !== "TCGA" && (searchToken.toUpperCase()) !== "TCGA-") {
                        $(this).attr("d", d3.svg.symbol()
                            .size(d3.select(this).attr("size") + 5)
                            .type(d3.select(this).attr("shape")));
                    } else {
                        $(this).attr("d", d3.svg.symbol()
                            .size(d3.select(this).attr("size"))
                            .type(d3.select(this).attr("shape")));
                    }
                } else {
                    $(this).attr("d", d3.svg.symbol()
                        .size(d3.select(this).attr("size"))
                        .type(d3.select(this).attr("shape")));
                }
            }
        }
    );     
};

var is_discretized = function(axis) {
    if ($("input:radio[name='" + ids.sidebar[axis].data_type + "']:checked").val() === vals.data_type.clin) {
        if (clinical_attr_is_discretized(axis)) return true;
        return false;
    } else if ($("input:radio[name='" + ids.sidebar[axis].data_type + "']:checked").val() === vals.data_type.genetic) {
        if (is_profile_discretized(axis)) return true;
        return false;
    }
};



