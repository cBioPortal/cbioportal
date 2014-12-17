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
        legendText : "No mutation"
    }];
    
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