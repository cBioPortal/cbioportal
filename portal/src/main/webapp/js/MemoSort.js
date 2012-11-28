var MemoSort = function(geneAlterations, sort_by) {

    var query = GeneAlterations.query(geneAlterations);

    var comparator = function(s1, s2) {
        // list of genes with corresponding alteration data
        var sample1 = query.bySampleId(s1),
            sample2 = query.bySampleId(s2);

        // alterations for the gene we want to sort by
        sample1 = sample1[sort_by];
        sample2 = sample2[sort_by];
//        console.log('sample', sample1);

        var cna_order = {AMPLIFIED: 2, DELETED: 1, null: 0},
            regulated_order = {UPREGULATED: 2, DOWNREGULATED: 1, null: 0};

        // diffs
        var cna = cna_order[sample1.cna] - cna_order[sample2.cna],
            mutation,
            mrna = regulated_order[sample1.mrna] - regulated_order[sample2.mrna],
            rppa = regulated_order[sample1.rppa] - regulated_order[sample2.rppa];

        // figure out the mutation diff
        if ((sample1.mutation === null) === (sample2.mutation === null)) {
            mutation = 0;
        } else if (sample1.mutation !== null) {
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
        // cna > mutation > mrna > rppa

        if (cna !== 0) {
            return cna;
        }

        if (mutation !== 0) {
            return mutation;
        }

        if (mrna !== 0) {
            return mrna;
        }

        if (rppa !== 0) {
            return rppa;
        }

        return 0;


//        // cna
//        if (cna > 0) {
//            return 1;
//        }
//        else if (cna < 0) {
//            return -1;
//        }
//        else {
//            // mutation
//            if (mutation > 0) {
//                return 1;
//            }
//            else if (mutation < 0) {
//                return -1;
//            }
//            else {
//                // mrna
//                if (mrna > 0) {
//                    return 1;
//                }
//                else if (mrna < 0) {
//                    return -1;
//                }
//                else {
//                    // rppa
//                    if (rppa > 0) {
//                        return 1;
//                    }
//                    else if (rppa < 0) {
//                        return -1;
//                    }
//                    else {
//                        return 0;
//                    }
//                }
//            }
//        }
    };

    var sort = function() {
        // geneAlterations:  data structure from GeneAlterationsJSON

        // the hugo gene symbol to sort by

        // sorting order : amplification, deletion, mutation, mrna, rppa
        // mutation > 0
        // amp > del > 0
        //

        var samples = geneAlterations.samples;

        var query = GeneAlterations.query(geneAlterations);

        // get the array of samples in the defined order
        var samples_l = query.getSampleList();

//    that.comparator = function(s1, s2) {
//        var sample1 = query.bySampleId(s1),
//            sample2 = query.bySampleId(s2);
//
//        sample1 = sample1[sort_by];
//        sample2 = sample2[sort_by];
//
//        return order[sample1.cna] - order[sample2.cna];
//    };

        samples_l.sort(that.comparator);        // samples_l is now sorted, is this bad functional programming?

        // copy the mapping
        var sorted_samples =  $.extend({}, samples);

        // reindex the mapping according to the new sorting
        samples_l.forEach(function(val, i) {
            sorted_samples[val] = i;
        });

        return sorted_samples;
    };

    var that = {
        comparator: comparator,
        sort: sort
    };

    return that;
};

    MemoSort.test = function() {
        console.log("====MemoSort Test====");

        var dumbData_cna = {
            hugo_to_gene_index: {"EGFR": 0},
            samples: {"TCGA-1": 0, "TCGA-2":1, "TCGA-3":2},
            gene_data: [
                { hugo: "EGFR",
                    cna: ["AMPLIFIED", "DELETED", null],
                    mutations: [null, null, null],
                    rppa: [null, null, null],
                    mrna: [null, null, null] }
            ]
        };

        var dumbData_cna2 = {
            hugo_to_gene_index: {"EGFR": 0},
            samples: {"TCGA-1": 0, "TCGA-2":1, "TCGA-3":2},
            gene_data: [
                { hugo: "EGFR",
                    cna: ["AMPLIFIED", "DELETED", null],
                    mutations: [['a'], null, null],
                    rppa: [null, null, "UPREGULATED"],
                    mrna: [null, null, null] }
            ]
        };

        var dumbData_mut = {
            hugo_to_gene_index: {"EGFR": 0},
            samples: {"TCGA-1": 0, "TCGA-2":1},
            gene_data: [
                { hugo: "EGFR",
                    cna: [null, null],
                    mutations: [['a'], null],
                    rppa: [null, null],
                    mrna: [null, null] }
            ]
        };

        var dumbData_mut2 = {
            hugo_to_gene_index: {"EGFR": 0},
            samples: {"TCGA-1": 0, "TCGA-2":1},
            gene_data: [
                { hugo: "EGFR",
                    cna: ["AMPLIFIED", "DELETED"],
                    mutations: [['a'], null],
                    rppa: [null, null],
                    mrna: [null, null] }
            ]
        };
//    var query = GeneAlterations.query(dumbData);
//
//    var one = query.bySampleId("TCGA-1").EGFR;
//    var two = query.bySampleId("TCGA-2").EGFR;
//    var three = query.bySampleId("TCGA-3").EGFR;
//    var four = query.bySampleId("TCGA-4").EGFR;
//    console.log('one', one,
//        '\ntwo', two,
//        '\nthree', three,
//        '\nfour', four);

        // test the comparator
//    var memoSort = MemoSort(testData, "EGFR");
//    var comparatorReturn = memoSort.comparator("TCGA-1", "TCGA-2");
//    console.log('TCGA-1', 'TCGA-2', comparatorReturn);
//    comparatorReturn = memoSort.comparator("TCGA-2", "TCGA-3");
//    console.log('TCGA-2', 'TCGA-3', comparatorReturn);
//    comparatorReturn = memoSort.comparator("TCGA-3", "TCGA-4");
//    console.log('TCGA-3', 'TCGA-4', comparatorReturn);
//
//    console.log("cna");
//    var sorted = MemoSort(dumbData_cna, "EGFR").sort();
//    console.log('TCGA-1', sorted['TCGA-1'] === 0 || sorted['TCGA-1']);
//    console.log('TCGA-2', sorted['TCGA-2'] === 1 || sorted['TCGA-2']);
//    console.log('TCGA-3', sorted['TCGA-3'] === 2 || sorted['TCGA-3']);
//    sorted = MemoSort(dumbData_cna2, "EGFR").sort();
//    console.log('TCGA-1', sorted['TCGA-1'] === 0 || sorted['TCGA-1']);
//    console.log('TCGA-2', sorted['TCGA-2'] === 1 || sorted['TCGA-2']);
//    console.log('TCGA-3', sorted['TCGA-3'] === 2 || sorted['TCGA-3']);
//
//    console.log("mutation");
//    sorted = MemoSort(dumbData_mut, "EGFR").sort();
//    console.log('TCGA-1', sorted['TCGA-1'] === 0 || sorted['TCGA-1']);
//    console.log('TCGA-2', sorted['TCGA-2'] === 1 || sorted['TCGA-2']);
//    sorted = MemoSort(dumbData_mut2, "EGFR").sort();
//    console.log('TCGA-1', sorted['TCGA-1'] === 0 || sorted['TCGA-1']);
//    console.log('TCGA-2', sorted['TCGA-2'] === 1 || sorted['TCGA-2']);

//    console.log("====END====");
    };

//$(document).ready(function() {
//    MemoSort.test();
//});
