oql = (function () {
	var queryDefaults = -1;
    function getGeneList(query) {
        // isolate gene ids from query
        var splitq = query.split(/[\n;]+/);
        var lines = [];
	for (var i=0; i<splitq.length; i++) {
            if ($.trim(splitq[i]) !== "") {
                lines.push(splitq[i]);
            }
        }
        var genes = [];
	for (var i=0; i<lines.length; i++) {
            var gene = $.trim(lines[i].split(/[:]/)[0]);
            if (gene !== "") {
                genes.push(gene);
            }
        }
        return genes;
    }
    
    /* PARSING */
    function getQueryDefaults() {
	var dfd = new $.Deferred();
	if (queryDefaults === -1) {
		dataman.getUniqueProfileTypesByStableId(window.PortalGlobals.getGeneticProfiles().split(" ")).then(function (uniqueProfileTypes) {
			var defaultGeneSettings = [];
			$.each(uniqueProfileTypes, function (ind, key) {
				if (key === "MUTATION_EXTENDED") {
					// default settings for a mutation profile
					defaultGeneSettings.push("MUT");
				} else if (key === "COPY_NUMBER_ALTERATION") {
					// default settings for a CNA profile
					defaultGeneSettings.push("AMP");
				} else if (key === "MRNA_EXPRESSION") {
					// default settings for an MRNA profile
					defaultGeneSettings.push("EXP >= " + window.PortalGlobals.getZscoreThreshold() + " EXP <= -" + window.PortalGlobals.getZscoreThreshold());
				} else if (key === "PROTEIN_ARRAY_PROTEIN_LEVEL") {
					// default settings for an RPPA profile
					defaultGeneSettings.push("PROT >= " + window.PortalGlobals.getRppaScoreThreshold() + " PROT <= -" + window.PortalGlobals.getRppaScoreThreshold());
				}
			});
			dfd.resolve(defaultGeneSettings);
		});
	} else {
		dfd.resolve(queryDefaults);
	}
	return dfd.promise();
    }
    
    function sanitizeSingleGeneQuery(query) {
	var dfd = new $.Deferred();
	query = query.trim();
	if (query.length === 0) {
		dfd.resolve('');
	}
	if (query.indexOf(":") === -1) {
		var tokens = query.split(/\s+/);
		if (tokens.length === 1) {
			getQueryDefaults().then(function(defaults) {
				dfd.resolve(tokens[0] + " : " + defaults.join(" ")+";");
			});
		} else {
			dfd.resolve(tokens[0] + " : " + tokens.slice(1).join(" ")+";");
		}
	} else {
		if (query[query.length-1] === ":") {
			getQueryDefaults().then(function(defaults) {
				dfd.resolve(query+" "+defaults.join(" ")+";");
			});
		} else {
			dfd.resolve(query);
		}
	}
	return dfd.promise();
    }
    
    function sanitizeQuery(query) {
	var dfd = new $.Deferred();
	var lines = query.split(/[\n;]/);
	var sanitizePromises = [];
	var sanitizedQueries = [];
	$.each(lines, function(ind, l) {
		var newPromise = sanitizeSingleGeneQuery(l);
		sanitizePromises.push(newPromise);
		newPromise.then(function(sanitizedQuery) {
			sanitizedQueries.push(sanitizedQuery);
		});
	});
	$.when.apply($, sanitizePromises).then(function() {
		dfd.resolve(sanitizedQueries);
	});
	return dfd.promise();
    }
    
	function parseQuery(query) {
		var dfd = new $.Deferred();
		sanitizeQuery(query).then(function (sanitizedLines) {
			var ret = [];
			var errors = [];
			$.each(sanitizedLines, function (ind, line) {
				if (line.length === 0) {
					return 1;
				}
				try {
					ret.push(oqlParser.parse(line));
				} catch (err) {
					errors.push({"line": i, "msg": err});
				}
			});
			if (errors.length > 0) {
				dfd.resolve({"result": 1, "return": errors});
			}
			dfd.resolve({"result": 0, "return": ret});
		});
		return dfd.promise();
	}

    /* FILTERING */
    function mutationMatch(test, targets) {
           if (test.type === "class") {
            //TODO: get mutation type info to make this work
            for (var i = 0; i < targets.length; i++) {
                if (targets[i].class === test.value) {
                    return true;
                }
            }
            return false;
        } else {
            // TODO: how to match mutations by position, not specific name
            for (var i = 0; i < targets.length; i++) {
                if (targets[i].name === test.value) {
                    return true;
                }
            }
            return false;
        }
    }

 
    function reduceFilterTree(tree) {
        // Recursive
        if (tree.type === "LEAF") {
            return tree.value;
        } else {
	var left, right;
            if (tree.type === "NOT") {
		    left = reduceFilterTree(tree.child);
		    if (!left) {
			    return {};
		    } else {
			    return false;
		    }
            } else if (tree.type === "AND") {
		    left = reduceFilterTree(tree.left);
		    right = reduceFilterTree(tree.right);
		    if (!left || !right) {
			    return false;
		    } else {
			    return $.extend({}, left, right);
		    }
            } else if (tree.type === "OR") {
		    left = reduceFilterTree(tree.left);
		    right = reduceFilterTree(tree.right);
		    if (!left && !right) {
			    return false;
		    } else if (!left) {
			    return right;
		    } else if (!right) {
			    return left;
		    } else {
			    return $.extend({}, left, right);
		    }
            }
        }
    }

    
    function createFilterTree(cmds, gene_attrs) {
        // IN: a query's command tree, a sample's gene attributes
        // OUT: a tree as described in 'reduceBooleanTree'
        //		in which each command is converted to the boolean
        //		value representing whether the given gene attributes complies with it
	var attr_map = {'AMP':'AMPLIFIED', 'GAIN':'GAINED', 'HETLOSS':'HEMIZYGOUSLYDELETED', 'HOMDEL':'HOMODELETED', 'EXP':'mrna', 'PROT':'rppa'};
        if (cmds.type === "AND" || cmds.type === "OR") {
            return {"type": cmds.type, "left": createFilterTree(cmds["left"], gene_attrs), "right": createFilterTree(cmds["right"], gene_attrs)};
        } else if (cmds.type === "NOT") {
            return {"type": "NOT", "child": createFilterTree(cmds["child"], gene_attrs)};
        } else {
            // non-logical type
            var ret;
            if (cmds.type === "AMP" || cmds.type === "HOMDEL" || cmds.type === "GAIN" || cmds.type === "HETLOSS") {
                ret = (gene_attrs.cna === attr_map[cmds.type] ? {cna: attr_map[cmds.type]} : false);
            } else if (cmds.type === "EXP" || cmds.type === "PROT") {
		    var attr_name = attr_map[cmds.type];
		    var level = gene_attrs[attr_name];
		    ret = {};
                if (cmds.constrType === "<") {
                    if (level !== undefined && level < cmds.constrVal) {
			    ret[attr_name] = 'DOWNREGULATED';
		    }
                } else if (cmds.constrType === "<=") {
                    if (level !== undefined && level <= cmds.constrVal) {
			    ret[attr_name] = 'DOWNREGULATED';
		    }
                } else if (cmds.constrType === ">") {
                    if (level !== undefined && level > cmds.constrVal) {
			    ret[attr_name] = 'UPREGULATED';
		    }
                } else if (cmds.constrType === ">=") {
                    if (level !== undefined && level >= cmds.constrVal) {
			    ret[attr_name] = 'UPREGULATED';
		    }
                }
		if (Object.keys(ret).length === 0) {
			ret = false;
		}
            } else if (cmds.type === "MUT") {
                if (cmds.constrType === "=") {
                    ret = (mutationMatch(cmds.constrVal, gene_attrs.mutation) ? {mutation:gene_attrs.mutation} : false);
                } else if (cmds.constrType === "!=") {
                    ret = (!(mutationMatch(cmds.constrVal, gene_attrs.mutation)) ? {mutation:gene_attrs.mutation} : false);
                } else if (cmds.constrType === false) {
                    // match any mutation
                    ret = (gene_attrs.mutation && (gene_attrs.mutation.length > 0) ? {mutation: gene_attrs.mutation} : false);
                }
            }
            return {"type": "LEAF", "value": ret};
        }
    }

    function filterSample(single_query, sample) {
        if (single_query.gene !== sample.gene) {
            // no records on this gene, return false by default
            return false;
        }
        var filterTree = createFilterTree(single_query.cmds, sample);
        return reduceFilterTree(filterTree);
    }

    function filter(query, samples) {
        // IN: Javascript object representing an OQL query, a list of samples
        // OUT: The indexes 'samples' for which at least one
        //		line of query returns true
        var ret = [];
        for (var i = 0; i < samples.length; i++) {
            var sample = samples[i];
	    var newsample = {sample: sample.sample, gene:sample.gene};
            for (var j = 0; j < query.length; j++) {
		    var filteredSample = filterSample(query[j], sample);
		    newsample = $.extend({}, newsample, (filteredSample || {}));
            }
	    ret.push(newsample);
        }
        return ret;
    }
    
    return {getGeneList: getGeneList, parseQuery: parseQuery, filter: filter};
})();