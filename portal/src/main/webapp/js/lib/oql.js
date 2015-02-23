// NOTE: Documentation can be generated from these annotations with jsdoc (http://usejsdoc.org/)
/**
 * @fileOverview Library for OQL functionality
 * @author <a href="mailto:adama@cbio.mskcc.org">Adam Abeshouse</a>
 * @namespace oql
 * @example
 * You probably want to call 
 * filter(parseQuery(query), samples)
 */
oql = (function () {
    /**
     * @memberOf oql
     * @param {string} query The query we wish to extract genes ids from
     * @returns {Array.<string>} A list of genes found in the query
     */
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
    // main method is parseQuery(query)
    /**
     * @memberOf oql
     * @param {string} query The query we wish to sanitize
     * @returns {string} A 'sanitized' query, ie transformed from user input so that
     * our code can process it as valid oncoquery language.
     */
    function sanitizeQuery(query) {
        // IN: text query, as from user
        // OUT: "sanitized", i.e. with a few adjustments made to put
        //		into valid OQL
        // These adjustments are: - capitalize everything except case-sensitive strings like mutation names (TODO)
        //						  - insert defaults from cbioportal interface (TODO)
        var ret = query;
        //ret = ret.toUpperCase();
        return ret;
    }

    /**
     * @memberOf oql
     * @param {string} query The multiline query we wish to parse
     * @returns {Object} {result: resultCode, return: array}
     * On success, resultCode = 0, array = a list of maps containing parsed query objects, one per line
     * On failure, resultCode = 1, array = a list of maps {line:lineNumber, msg:errorMessage} corresponding to the syntax errors.
     */
    function parseQuery(query) {
        var lines = sanitizeQuery(query).split(/[\n;]/);
        var ret = [];
        var errors = [];
        for (var i = 0; i < lines.length; i++) {
            var line = lines[i];
            if (line.length === 0) {
                continue;
            }
            try {
                ret.push(oqlParser.parse(line));
            } catch (err) {
                errors.push({"line": i, "msg": err});
            }
        }
        if (errors.length > 0) {
            return {"result": 1, "return": errors};
        }
        return {"result": 0, "return": ret};
    }

    /* FILTERING */
    /**
     * @memberOf oql
     * @description Helper function for createFilterTree
     * @see createFilterTree
     * @param {Object} test Either,e.g., {type:'class', value:'MISSENSE'} or,e.g., {type:'name', value:'V600E'}
     * @param {Array.<Object>} targets An array of Objects that look like {name:'V600E', class:'MISSENSE'}
     * @returns {boolean} Whether there is some element of 'targets' that matches 'test'
     */
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

    /**
     * @memberOf oql
     * @description Helper function for filterSample. Takes in a tree created by createFilterTree, and
     * reduces it recursively into a single boolean value.
     * @see createFilterTree
     * @see filterSample
     * @param tree A "filter tree" produced by createFilterTree
     * @returns {boolean} The single boolean value that 'tree' reduces to.
     */
    function reduceFilterTree(tree) {
        // Recursive
        if (tree.type === "LEAF") {
            return tree.value;
        } else {
            if (tree.type === "NOT") {
                return !reduceFilterTree(tree.child);
            } else if (tree.type === "AND") {
                return reduceFilterTree(tree.left) && reduceFilterTree(tree.right);
            } else if (tree.type === "OR") {
                return reduceFilterTree(tree.left) || reduceFilterTree(tree.right);
            }
        }
    }

    /**
     * @memberOf oql
     * @description Helper function for filterSample. Creates a "filter tree", which is basically
     * the result of visiting a query's parse tree and converting each command into a boolean 
     * of whether the given genotype fulfills that requirement.
     * @param {Object} cmds The attribute commands portion of the result of parseQuery. This is a parse tree.
     * @param {Object} gene_attrs The genotype of a particular gene in a given sample.
     * @see filterSample
     * @returns {Object} A nested object that represents a tree in the following way:
     * Where T is a subtree and B is a boolean literal value, a leaf looks like
     * {type:'LEAF', value:B}
     * and a node looks like one of the following three:
     * • {type: 'NOT', child: T}
     * • {type: 'AND', left: T1, right: T2}
     * • {type: 'OR', left:T1, right:T2}
     */
    function createFilterTree(cmds, gene_attrs) {
        // IN: a query's command tree, a sample's gene attributes
        // OUT: a tree as described in 'reduceBooleanTree'
        //		in which each command is converted to the boolean
        //		value representing whether the given gene attributes complies with it
	var attr_map = {'AMP':'AMPLIFIED', 'GAIN':'GAINED', 'HETLOSS':'HEMIZYGOUSLYDELETED', 'HOMDEL':'HOMODELETED'};
        if (cmds.type === "AND" || cmds.type === "OR") {
            return {"type": cmds.type, "left": createFilterTree(cmds["left"], gene_attrs), "right": createFilterTree(cmds["right"], gene_attrs)};
        } else if (cmds.type === "NOT") {
            return {"type": "NOT", "child": createFilterTree(cmds["child"], gene_attrs)};
        } else {
            // non-logical type
            var ret = false;
            if (cmds.type === "AMP" || cmds.type === "HOMDEL" || cmds.type === "GAIN" || cmds.type === "HETLOSS") {
                ret = (gene_attrs.cna === attr_map[cmds.type]);
            } else if (cmds.type === "EXP" || cmds.type === "PROT") {
                if (cmds.constrType === "<") {
                    ret = gene_attrs[cmds.type] && (gene_attrs[cmds.type] !== false) && (gene_attrs[cmds.type] < cmds.constrVal);
                } else if (cmds.constrType === "<=") {
                    ret = gene_attrs[cmds.type] && (gene_attrs[cmds.type] !== false) && (gene_attrs[cmds.type] <= cmds.constrVal);
                } else if (cmds.constrType === ">") {
                    ret = gene_attrs[cmds.type] && (gene_attrs[cmds.type] !== false) && (gene_attrs[cmds.type] > cmds.constrVal);
                } else if (cmds.constrType === ">=") {
                    ret = gene_attrs[cmds.type] && (gene_attrs[cmds.type] !== false) && (gene_attrs[cmds.type] >= cmds.constrVal);
                }
            } else if (cmds.type === "MUT") {
                if (cmds.constrType === "=") {
                    ret = mutationMatch(cmds.constrVal, gene_attrs.mutation);
                } else if (cmds.constrType === "!=") {
                    ret = !(mutationMatch(cmds.constrVal, gene_attrs.mutation));
                } else if (cmds.constrType === false) {
                    // match any mutation
                    ret = gene_attrs.mutation && (gene_attrs.mutation.length > 0);
                }
            }
            return {"type": "LEAF", "value": ret};
        }
    }

    /**
     * @memberOf oql
     * @description Helper function for filter
     * @see filter
     * @param {Object} single_query One-line query, i.e. one element of the array returned by parseQuery
     * @param {Object} sample A sample
     * @see angular.query-page-module.DataManager._samples
     * @returns {boolean} Whether the given sample passes the given one-line query
     */
    function filterSample(single_query, sample) {
        if (single_query.gene !== sample.gene) {
            // no records on this gene, return false by default
            return false;
        }
        var filterTree = createFilterTree(single_query.cmds, sample);
        return reduceFilterTree(filterTree);
    }

    /**
     * @memberOf oql
     * @param {Array.<Object>} query The result of parseQuery
     * @param {Object.<string, Object>} samples
     * @see angular.query-page-module.DataManager._samples
     * @returns {Array.<string>} A list of ids of samples (which are in subset, if specified) which pass at least one of the lines of the given query.
     */
    function filter(query, samples) {
        // IN: Javascript object representing an OQL query, a list of samples
        // OUT: The indexes 'samples' for which at least one
        //		line of query returns true
        var ret = [];
        for (var i = 0; i < samples.length; i++) {
            var sample = samples[i];
            var matchesNone = true;
            for (var j = 0; j < query.length; j++) {
                if (filterSample(query[j], sample)) {
                    matchesNone = false;
                    break;
                }
            }
            if (matchesNone) {
                ret.push(i);
            }
        }
        return ret;
    }
    
    /**
     * @memberOf oql
     * @param {Array.<Object>} profileData Profile data resulting from a call to getProfileData
     * @returns {Array.<Object>} A list of samples, where a sample is {'sample':<id>, 'data':<dict>}, where <dict> = {'AMP':<1 or 0>, 'HOMDEL':<1 or 0>, 'GAIN':<1 or 0>, 'HETLOSS':<1 or 0>, 'MUT':[...], 'PROT':<float>}
     */
    function makeSamples(profileData) {
	    // TODO
    }
    
    return {getGeneList: getGeneList, parseQuery: parseQuery, filter: filter, compileSamples: makeSamples};
})();