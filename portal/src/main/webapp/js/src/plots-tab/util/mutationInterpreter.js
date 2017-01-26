var mutationInterpreter = (function() {
    
    return {
        getSymbol: function(obj) {
            if (genetic_vs_genetic()) {
                if (isSameGene()) { //x and y showing the same gene
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
                } else if (isTwoGenes()) {
                    if (Object.keys(obj.mutation).length === 2) {
                        if(!scatterPlots.isGlyphExist("both_mut")) {
                            scatterPlots.addGlyph(mutationStyle.getGlyph("both_mut"));
                        }
                        return mutationStyle.getSymbol("both_mut");
                    } else if (Object.keys(obj.mutation).length === 1) {
                       if(!scatterPlots.isGlyphExist("one_mut")) {
                            scatterPlots.addGlyph(mutationStyle.getGlyph("one_mut"));
                        }
                        return mutationStyle.getSymbol("one_mut");
                    } else if (Object.keys(obj.mutation).length === 0) {
                       if(!scatterPlots.isGlyphExist("non_mut")) {
                            scatterPlots.addGlyph(mutationStyle.getGlyph("non_mut"));
                        }
                        return mutationStyle.getSymbol("non_mut");
                    }
                }
            } else if (genetic_vs_clinical()) {
                var _gene_axis = ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.genetic)? "x": "y";
                var _elt = document.getElementById(ids.sidebar[_gene_axis].gene);
                var _gene_symbol = _elt.options[_elt.selectedIndex].value;
                if (Object.keys(obj.mutation).length !== 0 && obj.mutation.hasOwnProperty(_gene_symbol)) {
                    if(!scatterPlots.isGlyphExist(obj.mutation[_gene_symbol].type)) {
                        scatterPlots.addGlyph(mutationStyle.getGlyph(obj.mutation[_gene_symbol].type));
                    }
                    return mutationStyle.getSymbol(obj.mutation[_gene_symbol].type);
                } else {
                    if(!scatterPlots.isGlyphExist("non")) {
                        scatterPlots.addGlyph(mutationStyle.getGlyph("non"));
                    }
                    return mutationStyle.getSymbol("non");
                }                
            } else if (clinical_vs_clinical()) {
                
            }
        },
        getFill: function(obj) {
            if (genetic_vs_genetic()) {
                if (isSameGene()) { //x and y showing the same gene
                    if (Object.keys(obj.mutation).length !== 0 && obj.mutation.hasOwnProperty(getYGene())) {
                        return mutationStyle.getFill(obj.mutation[getYGene()].type);
                    } else {
                        return mutationStyle.getFill("non");
                    }
                } else if (isTwoGenes()) {
                    if (Object.keys(obj.mutation).length === 2) {
                        return mutationStyle.getFill("both_mut");
                    } else if (Object.keys(obj.mutation).length === 1) {
                        return mutationStyle.getFill("one_mut");
                    } else if (Object.keys(obj.mutation).length === 0) {
                        return mutationStyle.getFill("non_mut");
                    }
                }                
            } else if (genetic_vs_clinical()) {
                var _gene_axis = ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.genetic)? "x": "y";
                var _elt = document.getElementById(ids.sidebar[_gene_axis].gene);
                var _gene_symbol = _elt.options[_elt.selectedIndex].value;
                if (Object.keys(obj.mutation).length !== 0 && obj.mutation.hasOwnProperty(_gene_symbol)) {
                    return mutationStyle.getFill(obj.mutation[_gene_symbol].type);
                } else {
                    return mutationStyle.getFill("non");
                }                  
            } else if (clinical_vs_clinical()) {
            	return mutationStyle.getFill("clin_clin");
            }

        },
        getOpacity: function() {
        	if (clinical_vs_clinical()) {
            	return mutationStyle.getOpacity("clin_clin");
            }
        	else {
        		return 1.0;
        	}
        },
        getStroke: function(obj) {
            if (genetic_vs_genetic()) {
                if (isSameGene()) { //x and y showing the same gene
                    if (Object.keys(obj.mutation).length !== 0 && obj.mutation.hasOwnProperty(getYGene())) {
                        return mutationStyle.getStroke(obj.mutation[getYGene()].type);
                    } else {
                        return mutationStyle.getStroke("non");
                    }
                } else if (isTwoGenes()) {
                    if (Object.keys(obj.mutation).length === 2) {
                        return mutationStyle.getStroke("both_mut");
                    } else if (Object.keys(obj.mutation).length === 1) {
                        return mutationStyle.getStroke("one_mut");
                    } else if (Object.keys(obj.mutation).length === 0) {
                        return mutationStyle.getStroke("non_mut");
                    }
                }
            } else if (genetic_vs_clinical()) {
                var _gene_axis = ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val()=== vals.data_type.genetic)? "x": "y";
                var _elt = document.getElementById(ids.sidebar[_gene_axis].gene);
                var _gene_symbol = _elt.options[_elt.selectedIndex].value;
                if (Object.keys(obj.mutation).length !== 0 && obj.mutation.hasOwnProperty(_gene_symbol)) {
                    return mutationStyle.getStroke(obj.mutation[_gene_symbol].type);
                } else {
                    return mutationStyle.getStroke("non");
                }                  
            } else if (clinical_vs_clinical()) {
            	return mutationStyle.getStroke("clin_clin");
            }
        }
    };
    
}());