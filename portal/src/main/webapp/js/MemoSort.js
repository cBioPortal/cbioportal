var MemoSort = function(geneAlterations, sort_by) {
    // geneAlterations:  data structure from GeneAlterationsJSON

    // the hugo gene symbol to sort by

    // sorting order : amplification, deletion, mutation, mrna, rppa
    // mutation > 0
    // amp > del > 0
    //

    var that = {};

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

        };

    that.comparator = function(s1, s2) {
        var cna_order = {"AMPLIFIED": 2, "DELETED": 1, null: 0},
            regulated_order = {"UPREGULATED": 2, "DOWNREGULATED": 1, null: 0};

        // list of genes with corresponding alteration data
        var sample1 = query.bySampleId(s1),
            sample2 = query.bySampleId(s2);

        // alterations for the gene we want to sort by
        sample1 = sample1[sort_by];
        sample2 = sample2[sort_by];
//        console.log('sample', sample1);

        // diffs
        var cna = cna_order[sample1.cna] - cna_order[sample2.cna],
            mutation,
            mrna = regulated_order[sample1.mrna] - regulated_order[sample2.mrna],
            rppa = regulated_order[sample1.rppa] - regulated_order[sample2.rppa];

        // figure out the mutation diff
        if (sample1.mutation === null && sample2 === null) {
            mutation = 0;
        } else if (sample1.mutation === null) {
            mutation = 1;
        } else {
            mutation = -1;
        }

        // do some logic
        // cna > mutation > mrna > rppa
        if (cna > 0) {
            return 1;
        }
        else if (cna < 0) {
            return -1;
        }
        else {
            if (mutation > 0) {
                return 1;
            }
            else if (mutation < 0) {
                return -1;
            }
            else {
                if (mrna > 0) {
                    return 1;
                }
                else if (mrna < 0) {
                    return -1;
                }
                else {
                    if (rppa > 0) {
                        return 1;
                    }
                    else if (rppa < 0) {
                        return -1;
                    }
                    else {
                        return 0;
                    }
                }
            }
        }
    };

    that.sort = function() {
        var sorted_samples = samples.sort(that.comparator);
        var toReturn = geneAlterations.samples;

        var index = 0;
        sorted_samples.forEach(function(i) {
            toReturn[i] = index;
            index += 1;
        });

        return toReturn;
    };

    return that;
 };

MemoSort.test = function(testData) {
    console.log("====MemoSort Test====");

    var dumbData = {
        hugo_to_gene_index: {"EGFR": 0, "TP53": 1},
        samples: {"TCGA-1": 0, "TCGA-2":1, "TCGA-3":2},
        gene_data: [
            { hugo: "EGFR",
                percent_altered: "100%",
                cna: ["AMPLIFIED", "AMPLIFIED", null],
                mutations: [["mut-1", "mut-2"], null, "mut2"],
                rppa: [null, "UPREGULATED", "UPREGULATED"],
                mrna: ["UPREGULATED", null, "DOWNREGULATED"] },

            { hugo: "TP53",
                percent_altered: "100%",
                cna: ["AMPLIFIED", "HOMODELETED", null],
                rppa: [null, "UPREGULATED", "UPREGULATED"],
                mutations: [["mut-1", "mut-2"], null, "mut2"],
                mrna: ["UPREGULATED", null, "DOWNREGULATED"] }
        ]
    };

    testData = testData || dumbData;

    var query = GeneAlterations.query(dumbData);

    var one = query.bySampleId("TCGA-1").EGFR;
    var two = query.bySampleId("TCGA-2").EGFR;
    var three = query.bySampleId("TCGA-3").EGFR;
    console.log('one', one, '\ntwo', two, '\nthree', three);

    // test the comparator
    var memoSort = MemoSort(testData, "EGFR");
    var comparatorReturn = memoSort.comparator("TCGA-1", "TCGA-2");
    console.log('TCGA-1', 'TCGA-2', comparatorReturn);
    comparatorReturn = memoSort.comparator("TCGA-2", "TCGA-3");
    console.log('TCGA-2', 'TCGA-3', comparatorReturn);


    console.log("====END====");
};

$(document).ready(function() {
    MemoSort.test();
});
