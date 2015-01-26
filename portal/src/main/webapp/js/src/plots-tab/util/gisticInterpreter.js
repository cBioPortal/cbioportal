var gisticInterpreter = (function() {
    
    var _mutated_case_glyph = {
        value: "mutated",
        symbol: "circle",
        fill: "orange",
        stroke: "none",
        legendText: "Mutated"
    };
    var _no_cna_data_glyph = {
        value: "no_cna",
        symbol: "circle",
        fill: "transparent",
        stroke: "grey",
        legendText: "No CNA data"
    };
    var _text_val_pair = {
        "-2": "Homdel",
        "-1": "Hetloss",
        "0": "Diploid",
        "1": "Gain",
        "2": "Amp"
    }; 

    return {
        getSymbol: function(d) {
            if (isNaN(d.cna_anno)) {
                if (!scatterPlots.isGisticGlyphExist("no_cna")) {
                    scatterPlots.addGlyph(_no_cna_data_glyph);
                }
                return _no_cna_data_glyph.stroke;
            }
            return gisticStyle.getSymbol(d.cna_anno);
        },
        getFill: function(d) {
            if (Object.keys(d.mutation).length === 0) {
                return "transparent";
            } else {
                return "orange";
            }
        },
        getStroke: function(d) {

            if (isNaN(d.cna_anno)) {
                if (!scatterPlots.isGisticGlyphExist("no_cna")) {
                    scatterPlots.addGlyph(_no_cna_data_glyph);
                }
                return _no_cna_data_glyph.stroke;
            }

            if (!scatterPlots.isGisticGlyphExist("mutated")) {
                scatterPlots.addGlyph(_mutated_case_glyph);
            }
            if(!scatterPlots.isGisticGlyphExist(d.cna_anno)) {
                scatterPlots.addGlyph(gisticStyle.getGlyph(d.cna_anno));
            }
            return gisticStyle.getStroke(d.cna_anno);
        },
        convert_to_val: function(numeric_val) {
            return _text_val_pair[numeric_val.toString()];
        }
    };
    
}());