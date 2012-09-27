// tools for parsing text inside of Gene Set box
// includes parsing for oncoQuery
//
//
// Gideon Dresdner
// August 2012
GeneSet = function(raw_genes_str) {

    if (typeof(raw_genes_str) !== 'string') {
        throw "Error: GeneSet only takes strings, given type '"
                    + typeof(raw_genes_str) + "'";
        return -1;
    }

    // {{{ helpers
    var type = function(o) {
        // thanks James Padolsey    http://james.padolsey.com/javascript/checking-types-in-javascript/
        return !!o && Object.prototype.toString.call(o).match(/(\w+)\]/)[1];
    };

    var flatten_helper = function(l, flat_l) {
        l.forEach(function(i) {
            if (type(i) === 'Array') {
                flatten_helper(i, flat_l);
            } else {
                flat_l.push(i);
            }
        });

        return flat_l;
    };

    // flattens an array of arrays into one array
    var flattenArray = function(l) {
        return flatten_helper(l, []);
    };

    // restaurant slang : 86 means kill
    // todo: is this extremely slow?
    var split_86emptystr = function(str, split_str) {
        return str.split(split_str).filter(function(i) {
            return i !== '';
        });
    };

    // }}}

    // gene statement   :
    //                  | gene (HUGO)
    //                  | onco query
    var GeneStmt = function(gene_str, is_onco_query) {


        var str = gene_str;
        var onco = is_onco_query;
        // todo : it'd be nice if I could 'overload' this function and get rid
        // of is_onco_query as a parameter

        return {
            getStmt: function() { return str; },
            isOncoQuery: function() {return onco; }
        };
    };

    var OncoQuery = function(str, is_datatype) {
        // inherits GeneStmt,
        // adds functionality for the special DATATYPES keyword

        var geneStmt = GeneStmt(str, true);

        if (is_datatype) {
            var split = str.split(';'),
                query = split[0],
                _genes = split_86emptystr(split[1], ' ');

            _genes = _genes.map(function(i) {
                return split_86emptystr(i, '\n');
            });

            _genes = flattenArray(_genes);

            geneStmt.getQuery = function() { return query; };
            geneStmt.getGenes = function () { return _genes; };
        }

        geneStmt.isDatatype = function() { return is_datatype; };

        return geneStmt;
    };

    // internal state variables
    var genes,
        all_genes,
        onco_queries;

    var geneStmts;
    // {{{ parse the raw gene set string

    if (/DATATYPES:/i.test(raw_genes_str)) {
        // datatype keyword dominates everything else,
        // if it is there, then the raw string consists entirely of things
        // related to this one statement.

        geneStmts = [OncoQuery(raw_genes_str, true)];
    }

    else {
        geneStmts = raw_genes_str.split(';');

        geneStmts = geneStmts.map(function(i) {
            return split_86emptystr(i, '\n');
        });

        geneStmts = geneStmts.filter(function(i) {
            return i.length !== 0;
        });

        geneStmts = flattenArray(geneStmts);

        // map strings to GeneStmts
        geneStmts = geneStmts.map(function(i) {
            if ((/:/).test(i)) {
                return OncoQuery(i, false);
            } else {
                return GeneStmt(i, false);
            }
        });

        geneStmts = geneStmts.map(function(i) {
            if (i.isOncoQuery()) {
                return i;
            } else {
                var genes = split_86emptystr(i.getStmt(), ' ');

                genes = genes.map(function(i) {
                    return GeneStmt(i.replace(/\s+/,''), false);
                });

                return genes;
            }
        });

        geneStmts = flattenArray(geneStmts);
    }

    //geneStmts.forEach(function(i) {
    //    console.log(i.getStmt());
    //});

    // }}}

    // filter out the onco query statements
    onco_queries = geneStmts.filter(function(x) {
        return x.isOncoQuery();
    });
    onco_queries = uniqueElementsOfArray(onco_queries);

    // filter out the gene statements
    genes = geneStmts.filter(function(x) {
        return !x.isOncoQuery();
    });
    genes = uniqueElementsOfArray(genes);

    // take all genes, parse out the gene from an onco query
    all_genes = geneStmts.map(function(x) {
        if (!x.isOncoQuery()) {     // gene, not onco query
            return x.getStmt();
        } else {                    // onco query
            if (x.isDatatype()) {
                return x.getGenes();
            } else {
                // 1st capturing group : the gene
                // 2nd capturing group : the colon
                var gene_regexp = /([0-9A-Za-z]+)(:)/;
                return x.getStmt().match(gene_regexp)[1];
            }
        }
    });

    all_genes = uniqueElementsOfArray(all_genes);

    // return GeneSet object
    return {
        getRawGeneString: function() {
            return raw_genes_str;
        },

        getOncoQueries: function() {
            // don't return DATATYPES
            // perhaps return a special Onco Query Object that deals with DATATYPES functionality
            return onco_queries.map(function(x) {
                if (x.isDatatype()) {
                    return {
                        isDataType: true,
                        query: x.getQuery(),
                        genes: x.getGenes() };
                }
                return x.getStmt();
            });
        },

        getGenes: function() {
        // returns a list of genes that are not in an onco query
            return genes.map(function(x) {      // extract the statement
                return x.getStmt();
            });
        },

        getAllGenes: function() {
        // returns a list of all genes, including ones in an onco query
            return all_genes;
        },

        filterOut: function(gene_str) {
            // cleans out the GeneSet of all gene statements of gene
            // gene_str
            onco_queries = onco_queries.filter(function(i) {
                return !i.getStmt().match(gene_str);
            });

            genes = genes.filter(function(i) {
                return i.getStmt() !== gene_str;
            });
        },

        toString: function() {
            return (this.getOncoQueries() + ' ' + this.getGenes()).trim();
        },

        test: function() {
            // most simple test
            // print out everything you've got
            console.log("rawGeneStr:");
            console.log(this.getRawGeneString());
            console.log("OncoQueries: " + this.getOncoQueries());
            console.log("gene set: " + this.getGenes());
            console.log("all genes: " + this.getAllGenes());

        }
    };
};

$(document).ready(function() {
    // dev
    //GeneSet("CCNE1:  CNA >= GAIN;\n TP53\n PTEN;\n").test();
    //GeneSet("CCNE1 RB1 CDKN2A").test();
    //GeneSet("CCNE1: AMP").test();
    //GeneSet("CCNE1:  CNA >= GAIN").test();
    //GeneSet("CCNE1: AMP MUTATED\nRB1: HOMDEL MUTATED\nCDKN2A: HOMDEL EXP < -1").test();
    //GeneSet("DATATYPES: AMP GAIN HOMDEL EXP > 1.5 EXP<=-1.5; CDKN2A MDM2 TP53").test();
    //GeneSet("DATATYPES: AMP GAIN HOMDEL EXP > 1.5 EXP<=-1.5; CDKN2A MDM2 TP53").test();
    //GeneSet("CCNE1:  CNA >= GAIN;\n TP53\n PTEN;\n" +
    //"CCNE1 RB1 CDKN2A\n" +
    //"CCNE1: AMP\n" +
    //"CCNE1:  CNA >= GAIN\n" +
    //"CCNE1: AMP MUTATED\nRB1: HOMDEL MUTATED\nCDKN2A: HOMDEL EXP < -1\n").test();
    //
    //GeneSet("CCNE1:  CNA >= GAIN;\n TP53\n PTEN;\n" +
    //"CCNE1 RB1 CDKN2A\n" +
    //"CCNE1: AMP\n" +
    //"CCNE1:  CNA >= GAIN\n" +
    //"CCNE1: AMP MUTATED\nRB1: HOMDEL MUTATED\nCDKN2A: HOMDEL EXP < -1\n").toString();
});
