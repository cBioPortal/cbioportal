var is_numeric = function(_arr) {
    var _result = true;
    var regex = /^-?[0-9]+(\.[0-9]+)?$/;
    $.each(_arr, function(index, val) {
        if (!regex.test(val)) {
            _result = false;
        }
    });
    return _result;
};

var is_clinical_data_discretized = function(_arr) {
    var _result;
    if (is_numeric(_arr)) {
        _result = false;   
    } else {
        var _tmp = [];
        $.each(_arr, function(index, val) {
           if ($.inArray(val, _tmp) === -1) _tmp.push(val); 
        });   
        if (_tmp.length > 15) {
            _result = false;
        } else _result = true;
    }
    return _result;
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
    if ($("#" + ids.sidebar.x.data_type).val() === $("#" + ids.sidebar.y.data_type).val() && 
        $("#" + ids.sidebar.x.data_type).val() === vals.data_type.genetic) {
        return true;
    } return false;   
};

var genetic_vs_clinical = function() {
    var _type_x = $("#" + ids.sidebar.x.data_type).val();
    var _type_y = $("#" + ids.sidebar.y.data_type).val();
    if (_type_x !== _type_y) {
        return true;
    } return false;
};

var clinical_vs_clinical = function() {
    if ($("#" + ids.sidebar.x.data_type).val() === $("#" + ids.sidebar.y.data_type).val() && 
        $("#" + ids.sidebar.x.data_type).val() === vals.data_type.clin) {
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

var appendLoadingImg = function(div) {
    $("#" + div).append("<img style='padding-top:200px; padding-left:300px;' src='images/ajax-loader.gif'>");
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
    "_gistic"
];

var isDiscretized = function(axis) {
    var elt = document.getElementById(ids.sidebar[axis].profile_name);
    var _profile_name = elt.options[elt.selectedIndex].value;
    var _token = _profile_name.replace(window.PortalGlobals.getCancerStudyId(), "");
    //if ($.inArray(_token, discretizedDataList) !== -1) return true;
    if ($.inArray(_token, discretized_cna_profile_keywords) !== -1) return true;
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


