var gisticInterpreter = (function() {

    return {
        getSymbol: function(d) {
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
            var _mutated_case_glyph = {
                value: "mutated",
                symbol: "circle",
                fill: "orange",
                stroke: "none",
                legendText: "Mutated"
            };
            if (!scatterPlots.isGisticGlyphExist("mutated")) {
                scatterPlots.addGlyph(_mutated_case_glyph);
            }
            if(!scatterPlots.isGisticGlyphExist(d.cna_anno)) {
                scatterPlots.addGlyph(gisticStyle.getGlyph(d.cna_anno));
            }
            return gisticStyle.getStroke(d.cna_anno);
        }
    };
    
}());