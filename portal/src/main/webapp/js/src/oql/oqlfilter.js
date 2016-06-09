// Requires parser to be window object, window.oql_parser
// Heavily dependent on structure/specification of OQL

window.OQL = (function() {
    
    function OQL() {
    }
    var parseOQLQuery = function(oql_query, opt_default_oql) {
	opt_default_oql = opt_default_oql || "";
	var parsed = window.oql_parser.parse(oql_query);

	var datatypes_alterations = false;
	for (var i = 0; i < parsed.length; i++) {
	    if (parsed[i].gene.toLowerCase() === "datatypes") {
		datatypes_alterations = parsed[i].alterations;
	    } else if (datatypes_alterations && !parsed[i].alterations) {
		parsed[i].alterations = datatypes_alterations;
	    }
	}

	if (opt_default_oql.length > 0) {
	    for (var i = 0; i < parsed.length; i++) {
		if (!parsed[i].alterations) {
		    parsed[i].alterations = window.oql_parser.parse("DUMMYGENE:" + opt_default_oql + ";")[0].alterations;
		}
	    };
	}

	return parsed.filter(function (parsed_line) {
	    return parsed_line.gene.toLowerCase() !== "datatypes";
	});
    };
    OQL.prototype.filter = function(oql_query, data, accessors, opt_default_oql, opt_by_oql_line) {
	/*
	 * accessors = {
	 *	'gene': function(d) {
	 *	    // returns lower case gene symbol
	 *	},
	 *	'cna': function(d) {
	 *	    // returns 'amp', 'homdel', 'hetloss', or 'gain',
	 *	    //  or null
	 *	},
	 *	'mut_type': function(d) {
	 *	    // returns 'missense', 'nonsense', 'nonstart', 'nonstop', 'frameshift', 'inframe', 'splice', or 'trunc',
	 *	    //  or null
	 *	},
	 *	'mut_position': function(d) {
	 *	    // returns a 2-element array of integers, the start position to the end position
	 *	    // or null
	 *	},
	 *	'mut_amino_acid_change': function(d) {
	 *	    // returns a string, the amino acid change,
	 *	    // or null
	 *	},
	 *	'exp': function(d) {
	 *	    // returns a double, mrna expression,
	 *	    // or null
	 *	},
	 *	'prot': function(d) {
	 *	    // returns a double, protein expression,
	 *	    // or null
	 *	}
	 * }
	 */
	opt_default_oql = opt_default_oql || "";
	var _parsed_query = parseOQLQuery(oql_query, opt_default_oql)
				.map(function(q_line) {
				    q_line.gene = q_line.gene.toLowerCase();
				    return q_line;
				});
	
	var filterDatum = function (datum, parsed_query) {
	    var wanted = false;
	    for (var i = 0; i < parsed_query.length; i++) {
		var wanted_for_line = true;
		var query_line = parsed_query[i];
		if (accessors.gene(datum) === query_line.gene) {
		    for (var j = 0; j < query_line.alterations.length; j++) {
			var alteration_cmd = query_line.alterations[j];
			if (alteration_cmd.alteration_type === "cna") {
			    var d_cna = accessors.cna(datum);
			    if (d_cna && (d_cna !== alteration_cmd.constr_val)) {
				wanted_for_line = false;
			    }
			} else if (alteration_cmd.alteration_type === "mut") {
			    if (alteration_cmd.constr_type === "class") {
				var mut_type = accessors.mut_type(datum).toLowerCase();
				if (mut_type) {
				    var target_type = alteration_cmd.constr_val.toLowerCase();
				    var matches = (mut_type === target_type) || (target_type === "trunc" && mut_type !== "missense" && mut_type !== "inframe");
				    if (alteration_cmd.constr_rel === "!=") {
					matches = !matches;
				    }
				    if (!matches) {
					wanted_for_line = false;
				    }
				}
			    } else if (alteration_cmd.constr_type === "position") {
				var mut_range = accessors.mut_position(datum);
				if (mut_range) {
				    var target_position = alteration_cmd.constr_val;
				    var matches = (target_position < mut_range[0] || target_position > mut_range[1]);
				    if (alteration_cmd.constr_rel === "!=") {
					matches = !matches;
				    }
				    if (!matches) {
					wanted_for_line = false;
				    }
				}
			    } else if (alteration_cmd.constr_type === "name") {
				var mut_name = accessors.mut_amino_acid_change(datum).toLowerCase();
				if (mut_name) {
				    var target_name = alteration_cmd.constr_val.toLowerCase();
				    var matches = (target_name === mut_name);
				    if (alteration_cmd.constr_rel === "!=") {
					matches = !matches;
				    }
				    if (!matches) {
					wanted_for_line = false;
				    }
				}
			    }
			} else if (alteration_cmd.alteration_type === "exp" || alteration_cmd.alteration_type === "prot") {
			    var level = accessors[(alteration_cmd.alteration_type === "exp" ? 'exp' : 'prot')](datum);
			    if (level !== null) {
				var target_level = alteration_cmd.constr_val;
				if ((alteration_cmd.constr_rel === "<" && level >= target_level) ||
					(alteration_cmd.constr_rel === "<=" && level > target_level) ||
					(alteration_cmd.constr_rel === ">" && level <= target_level) ||
					(alteration_cmd.constr_rel === ">=" && level < target_level)) {
				    wanted_for_line = false;
				}
			    }
			}
			if (!wanted_for_line) {
			    break;
			}
		    }
		}
		if (wanted_for_line) {
		    wanted = true;
		    break;
		}
	    }
	    return wanted;
	};
	if (opt_by_oql_line) {
	    return _parsed_query.map(function(query_line) {
		return data.filter(function(datum) {
		    return filterDatum(datum, [query_line]);
		});
	    });
	} else {
	    return data.filter(function(datum) {
		return filterDatum(datum, _parsed_query);
	    });
	}
    };
    OQL.prototype.genes = function(oql_query) {
	var parse_result = parseOQLQuery(oql_query);
	return parse_result.filter(function(q_line) {
	    return q_line.gene.toLowerCase() !== "datatypes";
	}).map(function(q_line) {
	    return q_line.gene;
	});
    };
    
    OQL.prototype.isValid = function(oql_query) {
	var ret = true;
	try {
	    window.oql_parser.parse(oql_query);
	} catch (e) {
	    ret = false;
	}
	return ret;
    };
	    
    return OQL;
})();