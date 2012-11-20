var MemoSort = function(geneAlterations, sort_by) {
    // geneAlterations:  data structure from GeneAlterationsJSON

    // the hugo gene symbol to sort by

    // sorting order : amplification, deletion, mutation, mrna, rppa
    // mutation > 0
    // amp > del > 0
    //

    //todo: this depends on d3, would require a little bit more work to extract those values

    var samples_map = d3.map(geneAlterations.samples);

    // get the array of samples in their proper order
    var samples = d3.map(geneAlterations.samples).keys()
        .sort(function(a,b) {
            return samples_map[b] - samples_map[a];
        });

    var gene_data = geneAlterations.gene_data;

    var query = GeneAlterations.query(geneAlterations);

    var elm_order = function(elm1, elm2) {
        // helper function that orders elements of the oncoprint matrix (i.e. a single rectangle position)
        //
        // elm1 and elm2 look like this:
        // { mut: ~, cna: ~, mrna: ~, rppa: ~ }
        // where '~' stands for some data value

        var UPREGULATED = "UPREGULATED",
            DOWNREGULATED = "DOWNREGULATED",
            AMPLIFIED = "AMPLIFIED",
            DELETED = "HOMODELETED";

        var cna1 = elm1.cna,
            cna2 = elm2.cna,
            mut1 = elm1.mutation,
            mut2 = elm2.mutation,
            mrna1 = elm1.mrna,
            mrna2 = elm2.mrna,
            rppa1 = elm1.rppa,
            rppa2 = elm2.rppa;

        // the functions below are merely scopes that serve to factor the logic of ordering
        var order_mut = function() {
            if (mut1 !== null && mut2 !== null) {
                if (mrna1 === mrna2) {
                    if (rppa1 === rppa2) {
                        return 0;
                    } else {
                        orderUpDownRegulation(rppa1, rppa2);
                    }
                } else {
                    orderUpDownRegulation(mrna1, mrna2);
                }
            } else if (mut1 === null) {
                return 1;
            } else if (mut2 === null) {
                return -1;
            } else {
                // this is never executed
                console.log("function order_mut ", "fell through");
            }
        };

        var orderUpDownRegulation = function(x1, x2) {
            if (x1 === UPREGULATED) {
                return -1;
            } else if (x2 === UPREGULATED) {
                return 1;
            } else if (x1 === DOWNREGULATED) {
                return -1;
            } else if (x2 === DOWNREGULATED) {
                return 1;
            }  else {
                // this is never executed
                console.log("function orderUpDownRegulation ", "fell through");
            }
        };

        if (cna1 === cna2) {
            order_mut();
        } else if (cna1 === AMPLIFIED) {
            return -1;
        } else if (cna2 === AMPLIFIED) {
            return 1;
        } else if (cna1 === DELETED) {
            return -1;
        } else if (cna2 === DELETED) {
            return 1;
        } else {
            // this is never executed
            console.log("this shouldn't have been executed!");
        }
    };

    var sort_helper = function(s1, s2) {

        var sample_1 = query.bySampleId(s1);
        var sample_2 = query.bySampleId(s2);

//        hugo_l.forEach(function(i) {
//
//            var order = elm_order(sample_1[i], sample_2[i]);
//
//            if (order !== 0) { return order; }
//        });

        elm_order(sample_1[hugo_l[0]], sample_1[hugo_l[1]]);

        return 0;
    };

    var sorted_samples = samples.sort(sort_helper);

    var toReturn = geneAlterations.samples;

    var index = 0;
    sorted_samples.forEach(function(i) {
        toReturn[i] = index;
        index += 1;
    });

    return toReturn;
 };

MemoSort.test = function(testData) {
    console.log("====MemoSort Test====");

    var dumbData = {
            hugo_to_gene_index: {"gene1": 0, "gene2": 1},
            samples: {"TCGA1": 0, "TCGA2":1, "TCGA3":2},
            gene_data: [
                { hugo: "gene1",
                    percent_altered: "100%",
                    cna: ["AMPLIFIED", "HOMODELETED", null],
                    rppa: [null, "UPREGULATED", "UPREGULATED"],
                    mutations: ["mut1", null, "mut2"],
                    mrna: ["UPREGULATED", null, "DOWNREGULATED"] },

                { hugo: "gene2",
                    percent_altered: "100%",
                    cna: ["AMPLIFIED", "HOMODELETED", null],
                    rppa: [null, "UPREGULATED", "UPREGULATED"],
                    mutations: ["mut1", null, "mut2"],
                    mrna: ["UPREGULATED", null, "DOWNREGULATED"] },
            ]
        };

    testData = testData || dumbData;

    MemoSort(testData, ["gene1", "gene2"]);

    console.log("====END====");
};

//$(document).ready(function() {
//    MemoSort.test();
//});
