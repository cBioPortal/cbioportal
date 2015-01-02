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

var isDiscretized = function(axis) {
    var discretizedDataList = [
        "gistic"
    ];
    
    var elt = document.getElementById(ids.sidebar[axis].profile_name);
    var _profile_name = elt.options[elt.selectedIndex].value;
    var _token = _profile_name.replace(window.PortalGlobals.getCancerStudyId() + "_", "");
    if ($.inArray(_token, discretizedDataList) !== -1) return true;
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

function discretizedCNAProfile() {
    
}

var discretized_cna_profile_keywords = [
    "_cna",
    "_cna_rae",
    "_gistic"
];
