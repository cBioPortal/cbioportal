var MemoSort = function(geneAlterations, hugo_l) {
    // geneAlterations:  data structure from GeneAlterationsJSON

    // hugo_l: list of hugo gene symbol **in the order that you would like them sorted**!

    // sorting order : amplification, deletion, mutation, mrna, rppa

    // mutation > 0
    // amp > del > 0
    //

    //todo: this depends on d3, would require a little bit more work to extract those values

    var samples_map = d3.map(geneAlterations.samples);

    var samples = d3.map(geneAlterations.samples).keys()
        .sort(function(a,b) {
            return samples_map[b] - samples_map[a];
        });

    var gene_data = geneAlterations.gene_data;

    var query = GeneAlterations.query(geneAlterations);

    var sort_helper = function(s1, s2) {

        var v1 = query.bySampleId(s1);      // vector 1, i.e. a column
        var v2 = query.bySampleId(s2);      // vector 2
    };

    samples.sort(sort_helper);
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
                    rppa: [null, "rppa1", "rppa2"],
                    mutations: ["mut1", null, "mut2"],
                    mrna: ["mrna1", "mrna2", "mrna3"] },

                { hugo: "gene2",
                    percent_altered: "100%",
                    cna: ["AMPLIFIED", "HOMODELETED", null],
                    rppa: [null, "rppa1", "rppa2"],
                    mutations: ["mut1", null, "mut2"],
                    mrna: ["mrna1", "mrna2", "mrna3"] }
            ]
        };

    testData = testData || dumbData;

    MemoSort(testData, ["gene1", "gene2"]);

    console.log("====END====");
};

$(document).ready(function() {
    MemoSort.test();
});
