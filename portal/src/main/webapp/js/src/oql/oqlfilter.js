// Requires parser to be window object, window.oql_parser
// Heavily dependent on OQL PEGjs specification

window.OQL = (function () {

    var parseOQLQuery = function (oql_query, opt_default_oql) {
	/*	In: - oql_query, a string, an OQL query
		    - opt_default_oql, a string, default OQL to add to any empty line
		Out: An array, with each element being a parsed OQL line, with 
		    all 'DATATYPES' lines applied to subsequent lines and removed.
	*/
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
	    }
	    ;
	}

	return parsed.filter(function (parsed_line) {
	    return parsed_line.gene.toLowerCase() !== "datatypes";
	});
    };
    
    var parsedOQLAlterationToSourceOQL = function(alteration) {
	if (alteration.alteration_type === "cna") {
	    if (alteration.constr_rel === "=") {
		return alteration.constr_val;
	    } else {
		return ["CNA",alteration.constr_rel,alteration.constr_val].join("");
	    }
	} else if (alteration.alteration_type === "mut") {
	    if (alteration.constr_rel) {
		if (alteration.constr_type === "position") {
		    return ["MUT",alteration.constr_rel,alteration.info.amino_acid,alteration.constr_val].join("");
		} else {
		    return ["MUT",alteration.constr_rel,alteration.constr_val].join("");
		}
	    } else {
		return "MUT";
	    }
	} else if (alteration.alteration_type === "exp") {
	    return "EXP" + alteration.constr_rel + alteration.constr_val;
	} else if (alteration.alteration_type === "prot") {
	    return "PROT" + alteration.constr_rel + alteration.constr_val;
	} else if (alteration.alteration_type === "fusion") {
	    return "FUSION";
	}
    };
    var unparseOQLQueryLine = function (parsed_oql_query_line) {
	var ret = "";
	var gene = parsed_oql_query_line.gene;
	var alterations = parsed_oql_query_line.alterations;
	ret += gene;
	if (alterations.length > 0) {
	    ret += ": " + alterations.map(parsedOQLAlterationToSourceOQL).join(" ");
	}
	ret += ";";
	return ret;
    };
    
    /* For the methods isDatumWantedByOQL, ..., the accessors argument is as follows:
	     * accessors = {
	     *	'gene': function(d) {
	     *	    // returns lower case gene symbol
	     *	},
	     *	'cna': function(d) {
	     *	    // returns 'amp', 'homdel', 'hetloss', or 'gain',
	     *	    //  or null
	     *	},
	     *	'mut_type': function(d) {
	     *	    // returns 'missense', 'nonsense', 'nonstart', 'nonstop', 'frameshift', 'inframe', 'splice', 'trunc', or 'promoter'
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
	     *	},
	     *	'fusion': function(d) {
	     *	    // returns true, false, or null
	     *	}
	     * }
	     */
    var isDatumWantedByOQL = function (parsed_oql_query, datum, accessors) {
	/*  In: - parsed_oql_query, the result of parseOQLQuery above
	 *	- datum, a datum
	 *	- accessors, an object as described above with methods that apply to datum
	 *  Out: Boolean, whether datum is wanted by this OQL query
	 */
	
	var gene = accessors.gene(datum).toUpperCase();
	// if the datum doesn't have a gene associated with it, it's unwanted.
	if (!gene) {
	    return false;
	}
	// Otherwise, a datum is wanted if it's wanted by at least one line.
	return parsed_oql_query
		.map(function(query_line) {
		    return isDatumWantedByOQLLine(query_line, datum, gene, accessors);
		})
		.reduce(function(acc, next) { 
		    return acc || next; 
		}, false);
    };
    
    var isDatumWantedByOQLLine = function(query_line, datum, datum_gene, accessors, opt_mark_oql_regulation_direction) {
	/*  Helper method for isDatumWantedByOQL
	 *  In: - query_line, one element of a parseOQLQuery output array
	 *	- datum, see isDatumWantedByOQL
	 *	- datum_gene, the lower case gene in datum - passed instead of reaccessed as an optimization
	 *	- accessors, see isDatumWantedByOQL
	 */
	var line_gene = query_line.gene.toUpperCase();
	// If the line doesn't have the same gene, the datum is not wanted by this line
	if (line_gene !== datum_gene) {
	    return false;
	}
	// Otherwise, a datum is wanted iff it's wanted by at least one command.
	if (!query_line.alterations) {
	    return 1;
	}
	return (query_line.alterations
		.map(function(alteration_cmd) {
		    return isDatumWantedByOQLAlterationCommand(alteration_cmd, datum, accessors, opt_mark_oql_regulation_direction);
		})
		.reduce(function(acc, next) {
		    if (next === 1) {
			// if it's wanted by this command, its wanted
			return 1;
		    } else if (next === 0) {
			// if this command doesn't address it, go with what currently decided
			return acc;
		    } else if (next === -1) {
			// if this command addresses and rejects it, then if its
			//  not already wanted, then for now its unwanted
			if (acc === 1) {
			    return 1;
			} else {
			    return -1;
			}
		    }
		}, -1) // start off with unwanted
		=== 1);
    };
    
    var isDatumWantedByOQLAlterationCommand = function(alt_cmd, datum, accessors, opt_mark_oql_regulation_direction) {
	/*  Helper method for isDatumWantedByOQLLine
	 *  In: - alt_cmd, a parsed oql alteration
	 *	- datum, see isDatumWantedByOQL
	 *	- accessors, see isDatumWantedByOQL
	 *  Out: 1 if the datum is addressed by this command and wanted,
	 *	0 if the datum is not addressed by this command,
	 *	-1 if the datum is addressed by this command and rejected
	 */
	if (alt_cmd.alteration_type === 'cna') {
	    return isDatumWantedByOQLCNACommand(alt_cmd, datum, accessors);
	} else if (alt_cmd.alteration_type === 'mut') {
	    return isDatumWantedByOQLMUTCommand(alt_cmd, datum, accessors);
	} else if (alt_cmd.alteration_type === 'exp' || alt_cmd.alteration_type === 'prot') {
	    return isDatumWantedByOQLEXPOrPROTCommand(alt_cmd, datum, accessors, opt_mark_oql_regulation_direction);
	} else if (alt_cmd.alteration_type === 'fusion') {
	    return isDatumWantedByFUSIONCommand(alt_cmd, datum, accessors);
	}
    };
    
    var isDatumWantedByFUSIONCommand = function(alt_cmd, datum, accessors) {
	/* Helper method for isDatumWantedByOQLAlterationCommand
	 * In/Out: See isDatumWantedByOQLAlterationCommand
	 */
	var d_fusion = accessors.fusion(datum);
	if (d_fusion === null) {
	    // If no fusion data, it's not addressed
	    return 0;
	} else {
	    return 2*(+d_fusion) - 1;
	}
    };
    
    var isDatumWantedByOQLCNACommand = function(alt_cmd, datum, accessors) {
	/*  Helper method for isDatumWantedByOQLAlterationCommand
	 *  In/Out: See isDatumWantedByOQLAlterationCommand
	 */
	var d_cna = accessors.cna(datum);
	if (!d_cna) {
	    // If no cna data on the datum, it's not addressed
	    return 0;
	} else {
	    // Otherwise, return -1 if it doesnt match, 1 if it matches
	    var match;
	    if (alt_cmd.constr_rel === "=") {
		match = +(d_cna === alt_cmd.constr_val.toLowerCase());
	    } else {
		var integer_copy_number = {"amp":2, "gain":1, "hetloss":-1, "homdel":-2};
		var d_int_cna = integer_copy_number[d_cna];
		var alt_int_cna = integer_copy_number[alt_cmd.constr_val.toLowerCase()];
		if (alt_cmd.constr_rel === ">") {
		    match = +(d_int_cna > alt_int_cna);
		} else if (alt_cmd.constr_rel === ">=") {
		    match = +(d_int_cna >= alt_int_cna);
		} else if (alt_cmd.constr_rel === "<") {
		    match = +(d_int_cna < alt_int_cna);
		} else if (alt_cmd.constr_rel === "<=") {
		    match = +(d_int_cna <= alt_int_cna);
		}
	    }
	    return 2 * match - 1; // map 0,1 to -1,1
	}
    };
    var isDatumWantedByOQLMUTCommand = function(alt_cmd, datum, accessors) {
	/*  Helper method for isDatumWantedByOQLAlterationCommand
	 *  In/Out: See isDatumWantedByOQLAlterationCommand
	 */
	var d_mut_type = accessors.mut_type(datum);
	if (!d_mut_type) {
	    // If no mut data on the datum, it's not addressed
	    return 0;
	} else {
	    d_mut_type = d_mut_type.toLowerCase();
	    // If no constraint relation ('=' or '!='), then every mutation matches
	    if (!alt_cmd.constr_rel) {
		return 1;
	    }
	    // Decide based on what type of mutation specification
	    if (alt_cmd.constr_type === 'class') {
		// Matching on type
		var target_type = alt_cmd.constr_val.toLowerCase();
		// It matches if the type of mutation matches, or if 
		//  the target is truncating and the mutation is anything but missense or inframe
		var matches = (d_mut_type === target_type) || 
			    (target_type === 'trunc' && d_mut_type !== 'missense' && d_mut_type !== 'inframe');
		if (alt_cmd.constr_rel === '!=') {
		    // If '!=', then we want 1 if it DOESNT match
		    matches = !matches;
		}
		return 2*(+matches) - 1;
	    } else if (alt_cmd.constr_type === 'position') {
		// Matching on position
		var d_mut_range = accessors.mut_position(datum);
		if (!d_mut_range) {
		    // If no position data, reject
		    return -1;
		}
		var target_position = alt_cmd.constr_val;
		var matches = (target_position >= d_mut_range[0] && target_position <= d_mut_range[1]);
		if (alt_cmd.constr_rel === '!=') {
		    matches = !matches;
		}
		return 2*(+matches) - 1;
	    } else if (alt_cmd.constr_type === 'name') {
		// Matching on amino acid change code
		var d_mut_name = accessors.mut_amino_acid_change(datum).toLowerCase();
		if (!d_mut_name) {
		    // If no amino acid change data, reject
		    return -1;
		}
		var target_name = alt_cmd.constr_val.toLowerCase();
		var matches = (target_name === d_mut_name);
		if (alt_cmd.constr_rel === '!=') {
		    matches = !matches;
		}
		return 2*(+matches) - 1;
	    }
	}
	    
    };
    var isDatumWantedByOQLEXPOrPROTCommand = function(alt_cmd, datum, accessors, opt_mark_oql_regulation_direction) {
	/*  Helper method for isDatumWantedByOQLAlterationCommand
	 *  In/Out: See isDatumWantedByOQLAlterationCommand
	 */
	var level = accessors[(alt_cmd.alteration_type === "exp" ? 'exp' : 'prot')](datum);
	if (level === null) {
	    // If no data, it's not addressed
	    return 0;
	} else {
	    // Otherwise, check it in relation to target
	    var target_level = alt_cmd.constr_val;
	    var target_rel = alt_cmd.constr_rel;
	    var match;
	    var direction = undefined;
	    if ((target_rel === '<' && level < target_level) || (target_rel === '<=' && level <= target_level)) {
		match = 1;
		direction = -1;
	    } else if ((target_rel === '>' && level > target_level) || (target_rel === '>=' && level >= target_level)) {
		match = 1;
		direction = 1;
	    } else {
		match = -1;
	    }
	    if (opt_mark_oql_regulation_direction) {
		datum.oql_regulation_direction = (typeof datum.oql_regulation_direction === "undefined" ? direction : datum.oql_regulation_direction);
	    }
	    return match;
	}
    };
    
    var filterData = function (oql_query, data, _accessors, opt_default_oql, opt_by_oql_line, opt_mark_oql_regulation_direction) {
	/* In:	- oql_query, a string
	 *	- data, a list of data
	 *	- accessors, accessors as defined above,
	 *	- opt_default_oql, an optional argument, string, default oql to insert to empty oql lines
	 *	- opt_by_oql_line, optional argument, boolean, see Out for description
	 *  Out: the given data, filtered by the given oql query. 
	 *	* If opt_by_oql_line is true, then the result is a list of lists, 
	 *	    where out[i] = the result of filtering the given data by oql_query 
	 *	    line i (after removing 'DATATYPES' lines).
	 *	* If opt_by_oql_line is false, then the result is just a flat list,
	 *	    the data that is wanted by at least one oql line.
	 */
	data = $.extend(true, [], data); // deep copy, because of any modifications it will make during filtration
	var null_fn = function () {
	    return null;
	};
	var required_accessors = ['gene', 'cna', 'mut_type', 'mut_position',
	    'mut_amino_acid_change', 'exp', 'prot', 'fusion'];
	// default every non-given accessor function to null
	var accessors = {};
	for (var i = 0; i < required_accessors.length; i++) {
	    accessors[required_accessors[i]] = _accessors[required_accessors[i]] || null_fn;
	}

	opt_default_oql = opt_default_oql || "";
	var parsed_query = parseOQLQuery(oql_query, opt_default_oql)
		.map(function (q_line) {
		    q_line.gene = q_line.gene.toUpperCase();
		    return q_line;
		});

	if (opt_by_oql_line) {
	    return parsed_query.map(function (query_line) {
		return {
		    'gene': query_line.gene,
		    'parsed_oql_line': query_line,
		    'oql_line': unparseOQLQueryLine(query_line),
		    'data': data.filter(function (datum) {
			return isDatumWantedByOQLLine(query_line, datum, accessors.gene(datum).toUpperCase(), accessors, opt_mark_oql_regulation_direction);
		    })
		};
	    });
	} else {
	    return data.filter(function (datum) {
		return isDatumWantedByOQL(parsed_query, datum, accessors);
	    });
	}
    };
    return {
	'filterCBioPortalWebServiceData': function(oql_query, data, opt_default_oql, opt_by_oql_line, opt_mark_oql_regulation_direction) {
	    /* Wrapper method for filterData that has the cBioPortal default accessor functions
	     * Note that for use, the input data must have the field 'genetic_alteration_type,' which
	     * takes one of the following values: 
	     *	- MUTATION_EXTENDED
	     *	- COPY_NUMBER_ALTERATION
	     *	- MRNA_EXPRESSION
	     *	- PROTEIN_LEVEL
	     */
	    var cna_profile_data_to_string = {
		"-2": "homdel",
		"-1": "hetloss",
		"0": null,
		"1": "gain",
		"2": "amp"
	    };
	    var accessors = {
		'gene': function(d) { return d.hugo_gene_symbol; },
		'cna': function(d) {
		    if (d.genetic_alteration_type === 'COPY_NUMBER_ALTERATION') {
			return cna_profile_data_to_string[d.profile_data];
		    } else {
			return null;
		    }
		},
		'mut_type': function(d) {
		    if (d.genetic_alteration_type === 'MUTATION_EXTENDED') {
			if (d.simplified_mutation_type === "fusion") {
			    return null;
			} else if (d.amino_acid_change.toLowerCase() === "promoter") {
			    return "promoter";
			} else {
			    return d.simplified_mutation_type;
			}
		    } else {
			return null;
		    }
		},
		'mut_position': function(d) {
		    if (d.genetic_alteration_type === 'MUTATION_EXTENDED') {
			var start = d.protein_start_position;
			var end = d.protein_end_position;
			if (start !== null && end !== null) {
			    return [parseInt(start, 10), parseInt(end, 10)];
			} else {
			    return null;
			}
		    } else {
			return null;
		    }
		},
		'mut_amino_acid_change': function(d) {
		    if (d.genetic_alteration_type === 'MUTATION_EXTENDED') {
			return d.amino_acid_change;
		    } else {
			return null;
		    }
		},
		'exp': function(d) {
		    if (d.genetic_alteration_type === 'MRNA_EXPRESSION') {
			return parseFloat(d.profile_data);
		    } else {
			return null;
		    }
		},
		'prot': function(d) {
		    if (d.genetic_alteration_type === 'PROTEIN_LEVEL') {
			return parseFloat(d.profile_data);
		    } else {
			return null;
		    }
		},
		'fusion': function(d) {
		    if (d.genetic_alteration_type === 'MUTATION_EXTENDED') {
			return (d.simplified_mutation_type === "fusion");
		    } else {
			return null;
		    }
		}
	    };
	    return filterData(oql_query, data, accessors, opt_default_oql, opt_by_oql_line, opt_mark_oql_regulation_direction);
	},
	'genes': function (oql_query) {
	    var parse_result = parseOQLQuery(oql_query);
	    var genes = parse_result.filter(function (q_line) {
		return q_line.gene.toLowerCase() !== "datatypes";
	    }).map(function (q_line) {
		return q_line.gene.toUpperCase();
	    });
	    var unique_genes_set = {};
	    for (var i=0; i<genes.length; i++) {
		unique_genes_set[genes[i]] = true;
	    }
	    return Object.keys(unique_genes_set);
	},
	'isValid': function (oql_query) {
	    var ret = true;
	    try {
		window.oql_parser.parse(oql_query);
	    } catch (e) {
		ret = false;
	    }
	    return ret;
	}
    }
})();