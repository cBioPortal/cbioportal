oql = (function () {
	var query_defaults = -1;
	var placeholder_defaults = ["MUT"]; // hack to use parsing internally without actual defaults
	function getGeneList(query) {
		var parse_result = parseQuery(query, placeholder_defaults);
		if (parse_result.result === 0) {
			var genes = {};
			for (var i=0, _len = parse_result.return.length; i<_len; i++) {
				genes[parse_result.return[i].gene] = true;
			}
			return Object.keys(genes);
		} else {
			return false;
		}
	}

	function getQueryDefaults() {
		var dfd = new $.Deferred();
		if (query_defaults === -1) {
			dataman.getUniqueProfileTypesByStableId(window.PortalGlobals.getGeneticProfiles().split(" ")).then(function (uniqueProfileTypes) {
				var default_gene_settings = [];
				$.each(uniqueProfileTypes, function (ind, key) {
					if (key === "MUTATION_EXTENDED") {
// default settings for a mutation profile
						default_gene_settings.push("MUT");
					} else if (key === "COPY_NUMBER_ALTERATION") {
// default settings for a CNA profile
						default_gene_settings.push("AMP");
					} else if (key === "MRNA_EXPRESSION") {
// default settings for an MRNA profile
						default_gene_settings.push("EXP >= " + window.PortalGlobals.getZscoreThreshold() + " EXP <= -" + window.PortalGlobals.getZscoreThreshold());
					} else if (key === "PROTEIN_ARRAY_PROTEIN_LEVEL") {
// default settings for an RPPA profile
						default_gene_settings.push("PROT >= " + window.PortalGlobals.getRppaScoreThreshold() + " PROT <= -" + window.PortalGlobals.getRppaScoreThreshold());
					}
				});
				dfd.resolve(default_gene_settings);
			});
		} else {
			dfd.resolve(query_defaults);
		}
		return dfd.promise();
	}
	
	function sanitizeSingleGeneQuery(query, query_defaults) {
		query = query.trim();
		if (query.length === 0) {
			return '';
		}
		if (query.indexOf(":") === -1) {
			var tokens = query.split(/\s+/);
			if (tokens.length === 1) {
				return tokens[0] + " : " + query_defaults.join(" ");
			} else {
				return tokens[0] + " : " + tokens.slice(1).join(" ");
			}
		} else {
			if (query[query.length - 1] === ":") {
				return query + " " + query_defaults.join(" ");
			} else {
				return query;
			}
		}
	}

	function sanitizeQuery(query, query_defaults) {
		var lines = query.split(/[\n;]/);
		var sanitized_lines = [];
		$.each(lines, function (ind, l) {
			sanitized_lines.push(sanitizeSingleGeneQuery(l, query_defaults));
		});
		return sanitized_lines;
	}

	function parseQuery(query, query_defaults) {
		var sanitized_lines = sanitizeQuery(query, query_defaults);
		var ret = [];
		var errors = [];
		$.each(sanitized_lines, function (ind, line) {
			if (line.length === 0) {
				return 1;
			}
			try {
				ret.push(oql_parser.parse(line));
			} catch (err) {
				errors.push({"line": ind, "msg": err});
			}
		});
		if (errors.length > 0) {
			return {"result": 1, "return": errors};
		}
		return {"result": 0, "return": ret};
	}
	
	function parseQueryWithDefaults(query) {
		var dfd = new $.Deferred();
		getQueryDefaults().then(function(defaults) {
			dfd.resolve(parseQuery(query, defaults));
			
		});
		return dfd.promise();
	}

	function matchingMutations(test, target_mutation_map) {
		// TODO: complete this map
		var mut_class_map = {'MISSENSE': ['Missense_Mutation'],
			'NONSENSE': ['Nonsense_Mutation'],
			'NONSTART': [],
			'NONSTOP': [],
			'FRAMESHIFT': ['Frame_Shift_Ins', 'Frame_Shift_Del'],
			'INFRAME': [],
			'SPLICE': ['Splice_Site'],
			'TRUNC': []}//maps oql terminology to db terminology; TODO: adama, hacky...fix later
		var ret = [];
		if (test.type === "class") {
			var mutation_class;
			for (var mutation in target_mutation_map) {
				if (target_mutation_map.hasOwnProperty(mutation)) {
					mutation_class = target_mutation_map[mutation];
					if (mut_class_map[test.value].indexOf(mutation_class) > -1) {
						ret.push(mutation);
					}
				}
			}
		} else if (test.type === "name") {
// TODO: how to match mutations by position, not specific name
			if (isNaN(test.value)) {
				// not position
				for (var mutation in target_mutation_map) {
					if (target_mutation_map.hasOwnProperty(mutation)) {
						if (test.value === mutation) {
							ret.push(mutation);
						}
					}
				}
			} else {
				// position
				// TODO: how to grab position?
			}	
		}
		return ret;
	}


	function reduceFilterTree(tree) {
		// TODO: proper filtered data format?
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
// TODO: proper filtered data format?
		var oql_to_cna_code = {'AMP': 2, 'GAIN': 1, 'HETLOSS': -1, 'HOMDEL': -2};
		var oql_to_attr_name = {'AMP': 'AMPLIFIED', 'GAIN': 'GAINED', 'HETLOSS': 'HEMIZYGOUSLYDELETED', 'HOMDEL': 'HOMODELETED', 'EXP': 'mrna', 'PROT': 'rppa'}; // maps oql terminology to oncoprint terminology
		if (cmds.type === "AND" || cmds.type === "OR") {
			return {"type": cmds.type, "left": createFilterTree(cmds["left"], gene_attrs), "right": createFilterTree(cmds["right"], gene_attrs)};
		} else if (cmds.type === "NOT") {
			return {"type": "NOT", "child": createFilterTree(cmds["child"], gene_attrs)};
		} else {
// non-logical type
			var ret;
			if (cmds.type === "AMP" || cmds.type === "HOMDEL" || cmds.type === "GAIN" || cmds.type === "HETLOSS") {
				// TODO: inequality
				ret = (gene_attrs.cna === oql_to_attr_name[cmds.type] ? {cna: oql_to_attr_name[cmds.type]} : false);
			} else if (cmds.type === "EXP" || cmds.type === "PROT") {
				var attr_name = oql_to_attr_name[cmds.type];
				var level = gene_attrs[attr_name];
				ret = {};
				if (cmds.constrType === "<") {
					if (typeof level !== 'undefined' && level < cmds.constrVal) {
						ret[attr_name] = 'DOWNREGULATED';
					}
				} else if (cmds.constrType === "<=") {
					if (typeof level !== 'undefined' && level <= cmds.constrVal) {
						ret[attr_name] = 'DOWNREGULATED';
					}
				} else if (cmds.constrType === ">") {
					if (typeof level !== 'undefined' && level > cmds.constrVal) {
						ret[attr_name] = 'UPREGULATED';
					}
				} else if (cmds.constrType === ">=") {
					if (typeof level !== 'undefined' && level >= cmds.constrVal) {
						ret[attr_name] = 'UPREGULATED';
					}
				}
				if (Object.keys(ret).length === 0) {
					ret = false;
				}
			} else if (cmds.type === "MUT") {
				if (cmds.constrType === "=") {
					ret = matchingMutations(cmds.constrVal, gene_attrs.mutations, true).join(",");
				} else if (cmds.constrType === "!=") {
					ret = matchingMutations(cmds.constrVal, gene_attrs.mutations, false).join(",");
				} else if (cmds.constrType === false) {
// match any mutation
					ret = Object.keys(gene_attrs.mutations).join(",");
					if (ret.length === 0) {
						ret = false;
					}
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

	function filterOncoprint(query, samples) {
// IN: Javascript object representing an OQL query, a list of samples
// OUT: The indexes 'samples' for which at least one
//		line of query returns true
		var ret = [];
		for (var i = 0; i < samples.length; i++) {
			var sample = samples[i];
			var newsample = {sample: sample.sample, gene: sample.gene};
			for (var j = 0; j < query.length; j++) {
				var filteredSample = filterSample(query[j], sample);
				newsample = $.extend({}, newsample, (filteredSample || {}));
			}
			ret.push(newsample);
		}
		return ret;
	}

	function filter(query, samples) {
		var oncoprintData = filterOncoprint(query, samples);
		var ret = [];
		for (var i = 0; i < oncoprintData.length; i++) {
			if (Object.keys(oncoprintData[i]).length > 2) {
				ret.push(i);
			}
		}
		return ret;
	}

	return {getGeneList: getGeneList, parseQuery: parseQuery, filter: filter, filterOncoprint: filterOncoprint, parseQueryWithDefaults: parseQueryWithDefaults};
})();