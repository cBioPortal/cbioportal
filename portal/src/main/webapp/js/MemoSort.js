var MemoSort = function(geneAlterations, sort_by) {
    // geneAlterations:  data structure from GeneAlterationsJSON

    // the hugo gene symbol to sort by

    // sorting order : amplification, deletion, mutation, mrna, rppa
    // mutation > 0
    // amp > del > 0
    //

    var that = {};

    //todo: this depends on d3, would require a little bit more work to extract those values

    var samples = geneAlterations.samples;
    var samples_map = d3.map(samples);

    // get the array of samples in the defined order
    var samples_l = samples_map.keys()
        .sort(function(a,b) {
            return samples_map.get(a) - samples_map.get(b);
        });

    console.log("samples_l sanity check: ",
        samples[samples_l[0]] === 0,
        samples[samples_l[1]] === 1,
        samples[samples_l[2]] === 2,
        samples[samples_l[3]] === 3);

//    var sanity = true;
//    for (var i = 0; i < samples.length; i++) {
//        if (samples[sample_l[i]] !== i) {
//            sanity = false;
//            break;
//        }
//    }
//    console.log('sanity ', sanity);

    var query = GeneAlterations.query(geneAlterations);

    that.comparator = function(s1, s2) {
        return -1;
    };


//    that.comparator = function(s1, s2) {
//        var cna_order = {"AMPLIFIED": 2, "DELETED": 1, null: 0},
//            regulated_order = {"UPREGULATED": 2, "DOWNREGULATED": 1, null: 0};
//
//        // list of genes with corresponding alteration data
//        var sample1 = query.bySampleId(s1),
//            sample2 = query.bySampleId(s2);
//
//        // alterations for the gene we want to sort by
//        sample1 = sample1[sort_by];
//        sample2 = sample2[sort_by];
////        console.log('sample', sample1);
//
//        // diffs
//        var cna = cna_order[sample1.cna] - cna_order[sample2.cna],
//            mutation,
//            mrna = regulated_order[sample1.mrna] - regulated_order[sample2.mrna],
//            rppa = regulated_order[sample1.rppa] - regulated_order[sample2.rppa];
//
//        // figure out the mutation diff
//        if (sample1.mutation === null && sample2.mutation === null) {
//            mutation = 0;
//        } else if (sample1.mutation === null) {
//            mutation = -1;
//        } else {
//            mutation = 1;
//        }
//
//        // do some logic
//        // cna > mutation > mrna > rppa
//
//        if (s1 === "TCGA-06-0151" || s2 === "TCGA-06-0151") {
//            console.log(sample1, sample2);
//        }
//
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
//    };

    that.sort = function() {
        samples_l.sort(that.comparator);        // samples_l is now sorted, is this bad functional programming?

        // copy the mapping
        var sorted_samples =  $.extend({}, geneAlterations.samples);

        // reindex the mapping according to the new sorting
        samples_l.forEach(function(val, i) {
            sorted_samples[val] = i;
        });

        console.log('sanity check: ', sorted_samples[samples_l[0]] === 0);

//        return sorted_samples;
        return sorted_samples;
    };


    return that;
 };

MemoSort.test = function(testData) {
    console.log("====MemoSort Test====");

    var dumbData = {
        hugo_to_gene_index: {"EGFR": 0, "TP53": 1},
        samples: {"TCGA-1": 0, "TCGA-2":1, "TCGA-3":2, "TCGA-4":3},
        gene_data: [
            { hugo: "EGFR",
                percent_altered: "100%",
                cna: ["AMPLIFIED", "AMPLIFIED", null, null],
                mutations: [["mut-1", "mut-2"], null, "mut2", "mut1"],
                rppa: [null, "UPREGULATED", "UPREGULATED", "DOWNREGULATED"],
                mrna: ["UPREGULATED", null, "DOWNREGULATED", null, null] }

//            { hugo: "TP53",
//                percent_altered: "100%",
//                cna: ["AMPLIFIED", "HOMODELETED", null],
//                rppa: [null, "UPREGULATED", "UPREGULATED"],
//                mutations: [["mut-1", "mut-2"], null, "mut2"],
//                mrna: ["UPREGULATED", null, "DOWNREGULATED"] }
        ]
    };

    testData = testData || dumbData;

    var query = GeneAlterations.query(dumbData);

    var one = query.bySampleId("TCGA-1").EGFR;
    var two = query.bySampleId("TCGA-2").EGFR;
    var three = query.bySampleId("TCGA-3").EGFR;
    var four = query.bySampleId("TCGA-4").EGFR;
    console.log('one', one,
        '\ntwo', two,
        '\nthree', three,
        '\nfour', four);

    // test the comparator
    var memoSort = MemoSort(testData, "EGFR");
    var comparatorReturn = memoSort.comparator("TCGA-1", "TCGA-2");
    console.log('TCGA-1', 'TCGA-2', comparatorReturn);
    comparatorReturn = memoSort.comparator("TCGA-2", "TCGA-3");
    console.log('TCGA-2', 'TCGA-3', comparatorReturn);
    comparatorReturn = memoSort.comparator("TCGA-3", "TCGA-4");
    console.log('TCGA-3', 'TCGA-4', comparatorReturn);

    console.log("====END====");
};

$(document).ready(function() {
    MemoSort.test();
});
