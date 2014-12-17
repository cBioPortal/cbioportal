var mutationInterpreter = (function() {
    
    function isSingleGene() {
        var elt_x = document.getElementById(ids.sidebar.x.gene);
        var elt_y = document.getElementById(ids.sidebar.y.gene);
        if (elt_x.options[elt_x.selectedIndex].value === elt_y.options[elt_y.selectedIndex].value) {
            return true;
        } 
        return false;
    }
    
    function isOneGeneMutated(_obj) {
        var elt_y = document.getElementById(ids.sidebar.y.gene);
        var _gene_symbol = elt_y.options[elt_y.selectedIndex].value;
        if (_obj.hasOwnProperty(_gene_symbol)) {
            return true;
        }
        return false;
    }
    
    function getYGene() {
        var elt_y = document.getElementById(ids.sidebar.y.gene);
        var _gene_symbol = elt_y.options[elt_y.selectedIndex].value;
        return _gene_symbol;
    }
    
    
    return {
        getSymbol: function(obj) {
            if (isSingleGene()) { //x and y showing the same gene
                if (Object.keys(obj.mutation).length !== 0 && obj.mutation.hasOwnProperty(getYGene())) {
                    if(!scatterPlots.isGlyphExist(obj.mutation[getYGene()].type)) {
                        scatterPlots.addGlyph(mutationStyle.getGlyph(obj.mutation[getYGene()].type));
                    }
                    return mutationStyle.getSymbol(obj.mutation[getYGene()].type);
                } else {
                    if(!scatterPlots.isGlyphExist("non")) {
                        scatterPlots.addGlyph(mutationStyle.getGlyph("non"));
                    }
                    return mutationStyle.getSymbol("non");
                }
            }
        },
        getFill: function(obj) {
            if (isSingleGene()) { //x and y showing the same gene
                if (Object.keys(obj.mutation).length !== 0 && obj.mutation.hasOwnProperty(getYGene())) {
                    return mutationStyle.getFill(obj.mutation[getYGene()].type);
                } else {
                    return mutationStyle.getFill("non");
                }
            }
        },
        getStroke: function(obj) {
            if (isSingleGene()) { //x and y showing the same gene
                if (Object.keys(obj.mutation).length !== 0 && obj.mutation.hasOwnProperty(getYGene())) {
                    return mutationStyle.getStroke(obj.mutation[getYGene()].type);
                } else {
                    return mutationStyle.getStroke("non");
                }
            }
        }
    };
    
}());