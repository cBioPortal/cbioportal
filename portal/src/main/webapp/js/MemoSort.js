var MemoSort = function(geneAlterations, sort_by) {
    // geneAlterations:  data structure from GeneAlterationsJSON

    // the hugo gene symbol to sort by

    // sorting order : amplification, deletion, mutation, mrna, rppa
    // mutation > 0
    // amp > del > 0
    //

    //todo: this depends on d3, would require a little bit more work to extract those values

//    console.log('geneAlterations', geneAlterations);

    var gene_index = geneAlterations.hugo_to_gene_index[sort_by],
        gene_data = geneAlterations.gene_data[gene_index];

    var samples = geneAlterations.samples,
        samples_l = d3.map(samples).keys()
            .sort(function(a, b) { return samples[a] - samples[b];});

    console.log("samples_l sanity check: ", samples[samples_l[0]]===0);

    // lets zip all the data together into one list of objects
    var zipped = d3.zip(gene_data.cna, gene_data.mutations, gene_data.mrna, gene_data.rppa, samples_l);

    var zipped = zipped.map(function(i) {
        return {
            cna: i[0],
            mutation: i[1],
            mrna: i[2],
            rppa: i[3],
            sample_id: i[4]
        };
    });

    var cna_order = {null: 0, "DELETED": 0, "AMPLIFIED": 1},
        regulated_order = {null: 0, "DOWNREGULATED ": 1, "UPREGULATED": 2},
        mutation_order = function(a, b) {
            if (a !== null && b !== null) {
                return 0;
            }
            if (a === null && b === null) {
                return 0
            }
            if (a === null) {
                return -1;
            }
            if (b === null) {
                return 1;
            } else {
                throw "fell through";
            }
        };

    NEST = d3.nest()
        .key(function(d) { return d.cna; }).sortKeys(function(a, b) {return cna_order[b] - cna_order[a]})
        .key(function(d) { return d.mutation; }).sortKeys(function(a,b) { return mutation_order(a,b);})
        .key(function(d) { return d.mrna; }).sortKeys(function(a,b) {return regulated_order[a] - regulated_order[b]})
        .key(function(d) { return d.rppa; }).sortKeys(function(a,b) {return regulated_order[a] - regulated_order[b]})
        .rollup(function(d) { return d.sample_id; })
        .entries(zipped);

//    console.log("zipped", zipped, zipped.length, d3.map(geneAlterations.samples).keys().length);
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
