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
// returns a list of samples
var MemoSort = function(data, attributes) {

    var comp_genes = function(attr1, attr2) {
        var cna_order = {AMPLIFIED:4, HOMODELETED:3, GAINED:2, HEMIZYGOUSLYDELETED:1, DIPLOID: 0, undefined: 0},
            regulated_order = {UPREGULATED: 2, DOWNREGULATED: 1, undefined: 0};

        var cna_diff = cna_order[attr2.cna] - cna_order[attr1.cna];
        if (cna_diff !== 0) {
            return cna_diff;
        }

        // figure out the mutation_diff
        var mutation_diff;
        if ((attr2.mutation === undefined) === (attr1.mutation === undefined)) {
            mutation_diff = 0;
        } else if (attr2.mutation !== undefined) {
            mutation_diff = 1;
        } else {
            mutation_diff = -1;
        }
        // do the mutation diff
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

    var getAttr = function(d) {
        return d.gene || d.attr_id;
    };

    var assert = function(bool) {
        if (!bool) {
            throw new Error("Assertion failure");
        }
    };

    var comp = function(x,y) {
        for (var i = 0; i < x.values.length; i+=1) {
            var x_attrs = x.values
                .sort(function(x,y) { return attributes.indexOf(getAttr(x)) - attributes.indexOf(getAttr(y)); });

            var y_attrs = y.values
                .sort(function(x,y) { return attributes.indexOf(getAttr(x)) - attributes.indexOf(getAttr(y)); });

            for (var j = 0; j < x_attrs.length; j+=1) {
                assert(x_attrs[j].gene === y_attrs[j].gene);
                assert(x_attrs[j].attr_id === y_attrs[j].attr_id);

                var diff = x_attrs[j].gene !== undefined
                    ? comp_genes(x_attrs[j], y_attrs[j])
//                    : comp_clinical(x_attrs[j], y_attrs[j]);
                    : 0;

                if (diff !== 0) {
                    return diff;
                }
            }
        }
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
