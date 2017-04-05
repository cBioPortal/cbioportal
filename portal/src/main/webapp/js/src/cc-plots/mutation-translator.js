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

var mutationStyle = (function() {  //Key and "typeName" are always identical
    var styleSheet = [
        {
            typeName : "Frameshift",
            symbol : "triangle-down",
            fill : "#1C1C1C",
            stroke : "#B40404",
            legendText : "Frameshift"
        },
        {
            typeName: "Nonsense",
            symbol : "diamond",
            fill : "#1C1C1C",
            stroke : "#B40404",
            legendText : "Nonsense"
        },
        {
            typeName : "Splice",
            symbol : "triangle-up",
            fill : "#A4A4A4",
            stroke : "#B40404",
            legendText : "Splice"
        },
        {
            typeName : "In_frame",
            symbol : "square",
            fill : "#DF7401",
            stroke : "#B40404",
            legendText : "In_frame"
        },
        {
            typeName : "Nonstart",
            symbol : "cross",
            fill : "#DF7401",
            stroke : "#B40404",
            legendText : "Nonstart"
        },
        {
            typeName : "Nonstop",
            symbol : "triangle-up",
            fill : "#1C1C1C",
            stroke : "#B40404",
            legendText : "Nonstop"
        },
        {
            typeName : "Missense",
            symbol : "circle",
            fill : "#DF7401",
            stroke : "#B40404",
            legendText : "Missense"
        },
        {
            typeName: "Other",
            symbol: "square",
            fill : "#1C1C1C",
            stroke : "#B40404",
            legendText : "Other"
        },
        {
            typeName : "non",
            symbol : "circle",
            fill : "#00AAF8",
            stroke : "#0089C6",
            legendText : "Not mutated"
        },
        {
            typeName: "one_mut",
            symbol : "circle",
            fill : "#DBA901",
            stroke : "#886A08",
            legendText : "One Gene mutated"
        },
        {
            typeName : "both_mut",
            symbol : "circle",
            fill : "#FF0000",
            stroke : "#B40404",
            legendText : "Both mutated"
        },
        {
            typeName : "non_mut",
            symbol : "circle",
            fill : "#00AAF8",
            stroke : "#0089C6",
            legendText : "Neither mutated"
        },
        {
            typeName: "non_sequenced",
            symbol : "circle",
            fill : "white",
            stroke : "gray",
            legendText : "Not sequenced"
        }

    ];

    return {
        getSymbol: function(_typeName) {
            var _result = "";
            $.each(styleSheet, function(index, obj) {
                if (obj.typeName === _typeName) {
                    _result = obj.symbol;
                }
            });
            return _result;
        },
        getFill: function(_typeName) {
            var _result = "";
            $.each(styleSheet, function(index, obj) {
                if (obj.typeName === _typeName) {
                    _result = obj.fill;
                }
            });
            return _result;
        },
        getStroke: function(_typeName) {
            var _result = "";
            $.each(styleSheet, function(index, obj) {
                if (obj.typeName === _typeName) {
                    _result = obj.stroke;
                }
            });
            return _result;
        },
        getText: function(_typeName) {
            var _result = "";
            $.each(styleSheet, function(index, obj) {
                if (obj.typeName === _typeName) {
                    _result = obj.legendText;
                }
            });
            return _result;
        },
        getGlyph: function(_typeName) { //retrieve the whole object
            var _result = {};
            $.each(styleSheet, function(index, obj) {
                if (obj.typeName === _typeName) {
                    _result = obj;
                }
            });
            return _result;
        }
    };

}());