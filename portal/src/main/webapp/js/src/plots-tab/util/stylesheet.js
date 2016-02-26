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
    },
    {
        typeName : "clin_clin",
        symbol : "circle",
        fill : "#00AAF8",
        stroke : "grey",
        opacity: 0.6
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
        getOpacity: function(_typeName) {
            var _result = "";
            $.each(styleSheet, function(index, obj) {
                if (obj.typeName === _typeName) {
                    _result = obj.opacity;
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

var gisticStyle = (function() {
   var stylesheet = [
    {
        type: "Amplification",
        value: "2",
        stroke : "#FF0000",
        symbol : "circle",
        legendText : "Amplification",
        fill: "transparent"
    },
    {
        type: "Gain",
        value: "1",
        stroke : "#FF69B4",
        symbol : "circle",
        legendText : "Gain",
        fill: "transparent"
    },
    {
        type: "Diploid",
        value: "0",
        stroke : "#000000",
        symbol : "circle",
        legendText : "Diploid",
        fill: "transparent"
    },
    {
        type: "Shallow Deletion",
        value: "-1",
        stroke : "#00BFFF",
        symbol : "circle",
        legendText : "Shallow Deletion",
        fill: "transparent"
    },
    {
         type: "Deep Deletion",
         value: "-2",
         stroke : "#00008B",
         symbol : "circle",
         legendText : "Deep Deletion",
         fill: "transparent"
     }
   ];

    return {
        getSymbol: function(_value) {
            var _result = "";
            $.each(stylesheet, function(index, obj) {
                if (obj.value === _value) {
                    _result = obj.symbol;
                }
            });
            return _result;
        },
        getStroke: function(_value) {
            var _result = "";
            $.each(stylesheet, function(index, obj) {
                if (obj.value === _value) {
                    _result = obj.stroke;
                }
            });
            return _result;
        },
        getGlyph: function(_value) {
            var _result = {};
            $.each(stylesheet, function(index, obj) {
                if (obj.value === _value) {
                    _result = obj;
                }
            });
            return _result;
        }
    };

}());
