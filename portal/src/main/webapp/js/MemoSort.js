var MemoSort = function(geneAlterations, samples_list, gene_list) {

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

        // base case
        if (sort_by === undefined) {
            return 0;
        }

        // list of genes with corresponding alteration data
        var sample1 = query.bySampleId(s1),
            sample2 = query.bySampleId(s2);

        // get the gene object
        sample1 = sample1[sort_by];
        sample2 = sample2[sort_by];

        var cna_order = {AMPLIFIED: 2, DELETED: 1, HOMODELETED: 1, null: 0},
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
