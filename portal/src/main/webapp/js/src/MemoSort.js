//
//
// Gideon Dresdner <dresdnerg@cbio.mskcc.org> May 2013
//
//
;
// data is assumed to be in the form that is used in the OncoPrint.
// Eventually this can be modified so that it can take different json formats.
//
// attributes : the ordering of the attributes.  Omission basically amounts to "smallest"
//
// returns the data, sorted
var MemoSort = function(data, attributes) {

    // compares two objects that have gene data (cna, mutation, mrna, rppa).
    // Returns a number that indicates the order.
    var comp_genes = function(attr1, attr2) {
        var cna_order = {AMPLIFIED:4, HOMODELETED:3, GAINED:2, HEMIZYGOUSLYDELETED:1, DIPLOID: 0, undefined: 0},
            regulated_order = {UPREGULATED: 2, DOWNREGULATED: 1, undefined: 0},
            mutation_order_f = function(m) { return m === undefined ? 0 : 1; };

        var cna_diff = cna_order[attr2.cna] - cna_order[attr1.cna];
        if (cna_diff !== 0) {
            return cna_diff;
        }

        var mutation_diff = mutation_order_f(attr2.mutation) - mutation_order_f(attr1.mutation);
        if (mutation_diff !== 0) {
            return mutation_diff;
        }

        var mrna_diff = regulated_order[attr2.mrna] - regulated_order[attr1.mrna];
        if (mrna_diff !== 0) {
            return mrna_diff;
        }

        var rppa_diff = regulated_order[attr2.rppa] - regulated_order[attr1.rppa];
        if (rppa_diff !== 0) {
            return rppa_diff;
        }

        return 0;       // they are equal in every way
    };

    // compares two objects of clinical data (attr_ids and attr_vals)
    // returns a *number* that indicates which one is larger
    var comp_clinical = function(attr1, attr2) {
        var discrete = isNaN(parseInt(val1));
        var val1 = attr1.attr_val;
        var val2 = attr2.attr_val;

        // "NA" value goes to the end
        if (val1 === "NA") {
            return val2 === "NA" ? 0 : 1;
        }
        if (val2 === "NA") {
            return val1 === "NA" ? 0 : -1;
        }

        // must return a number
        if (discrete) {
            if (val1 < val2) {
                return 1;
            } else if (val2 < val1) {
                return -1;
            } else {
                return 0;
            }
        }
        else {  // continuous value type
            return val2 - val1;
        }
    };

    var getAttr = function(d) {
        return d.gene || d.attr_id;
    };

    // throws an error without dying
    var assert = function(bool) {
        if (!bool) {
            throw new Error("Assertion failure " + assert.caller());
        }
    };

    // a little bit of optimization
    var attr2index = (function() {
        var toReturn = {};
        for (var i = 0; i < attributes.length; i+=1) {
            toReturn[attributes[i]] = i;
        }
        return toReturn;
    }());

    var comp = function(x,y) {
        for (var i = 0; i < x.values.length; i+=1) {

            // sort attributes according to the order specified by the user
            var x_attrs = x.values
                .sort(function(x,y) { return attr2index[getAttr(x)] - attr2index[getAttr(y)]; });
            var y_attrs = y.values
                .sort(function(x,y) { return attr2index[getAttr(x)] - attr2index[getAttr(y)]; });

            // this is a hack
            // if there is missing data, just put the one with less data to the right
            if (x_attrs.length !== y_attrs.length) {
                return y_attrs.length - x_attrs.length;
            }

            // iterate over the attributes of x and y in the user defined
            // order, comparing along the way
            for (var j = 0; j < x_attrs.length; j+=1) {

                var xj = x_attrs[j];
                var yj = y_attrs[j];

                assert(xj.gene === yj.gene);        // what we are comparing are comparable
                assert(xj.attr_id === yj.attr_id);

                var diff = (xj.gene === undefined
                    ? comp_clinical(xj, yj)
                    :  comp_genes(xj, yj));

                // return the first nonzero diff
                if (diff !== 0) {
                    return diff;
                }
            }
        }
        // if they are equal in all diffs, then they are truly equal.
        return 0;
    };

    return data.sort(comp);
};

var _MemoSort = function(geneAlterations, samples_list, gene_list) {

    // filter the samples map
    var _geneAlterations = jQuery.extend(true, {}, geneAlterations);        // deep copy
    var samples_unfiltered = _geneAlterations.samples;
    var samples_filtered = {};

    samples_list.forEach(function(sample) {
        samples_filtered[sample] = samples_unfiltered[sample];
        // samples_list should always be contained in the samples map of geneAlterations
    });

    _geneAlterations['samples'] = samples_filtered;

    var query = QueryGeneData(_geneAlterations);

    var comparator_helper = function(s1, s2, gene_list) {

        var sort_by = gene_list.pop();

        // base case, they are equal
        if (sort_by === undefined) {
            return 0;
        }

        // list of genes with corresponding alteration data
        var sample1 = query.bySampleId(s1),
            sample2 = query.bySampleId(s2);

        // get the gene object
        sample1 = sample1[sort_by];
        sample2 = sample2[sort_by];

        // diploid === null?
        var cna_order = {AMPLIFIED:4, HOMODELETED:3, GAINED:2, HEMIZYGOUSLYDELETED:1, DIPLOID: 0, null:0},
            regulated_order = {UPREGULATED: 2, DOWNREGULATED: 1, null: 0};

        // diffs
        var cna = cna_order[sample2.cna] - cna_order[sample1.cna],
            mutation,
            mrna = regulated_order[sample2.mrna] - regulated_order[sample1.mrna],
            rppa = regulated_order[sample2.rppa] - regulated_order[sample1.rppa];

        // figure out the mutation diff
        if ((sample2.mutation === null) === (sample1.mutation === null)) {
            mutation = 0;
        } else if (sample2.mutation !== null) {
            mutation = 1;
        } else {
            mutation = -1;
        }

        // sanity check
        if (cna === undefined
            || mutation === undefined
            || mrna === undefined
            || rppa === undefined) {
            console.log("cna: " + cna
                + " mutation: " + mutation
                + " mrna: " + mrna
                + " rppa: " + rppa);
            return;
        }

        // do some logic
        // cna > mrna > mutation > rppa

        if (cna !== 0) {
            return cna;
        }

        if (mrna !== 0) {
            return mrna;
        }

        if (rppa !== 0) {
            return rppa;
        }

        if (mutation !== 0) {
            return mutation;
        }

        return comparator_helper(s1, s2, gene_list);
    };

    var comparator = function(s1, s2) {
        // make a copy of gene_list
        var gene_list_copy = [];
        gene_list.forEach(function(i) { gene_list_copy.push(i); });
        gene_list_copy.reverse();

        return comparator_helper(s1, s2, gene_list_copy);
    };

    var sort = function() {
        // geneAlterations:  data structure from GeneDataJSON

        // the hugo gene symbol to sort by

        // sorting order : amplification, deletion, mutation, mrna, rppa
        // mutation > 0
        // amp > del > 0
        //

        // get the array of samples in the defined order
        var sorted_samples_l = query.getSampleList();

        sorted_samples_l.sort(comparator);
        // samples_l is now sorted, is this bad functional programming?

        return sorted_samples_l;
    };

    return {
        comparator: comparator,
        sort: sort
    };
};
