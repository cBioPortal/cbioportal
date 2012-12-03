describe("QueryGeneData", function() {
    var AMPLIFIED = "AMPLIFIED",
        DELETED = "DELETED";

    var UPREGULATED = "UPREGULATED",
        DOWNREGULATED = "DOWNREGULATED";

    var GENE1 = { hugo: "GENE1",
        hugo: "GENE1",
        cna: [AMPLIFIED, DELETED],
        mutations: [null, ["a"]],
        mrna: [UPREGULATED, DOWNREGULATED],
        rppa: [DOWNREGULATED, null],
        percent_altered: "10%"
    };

    var genes = [GENE1],
        hugo_to_gene_index = {"GENE1": 0},
        samples = {"CASE1": 0, "CASE2": 1};

    var gene_data =  { gene_data: genes,
        hugo_to_gene_index: hugo_to_gene_index,
        samples: samples
    };

    var query;
    beforeEach(function() {
        query = QueryGeneData(gene_data);
    });

    it("should query by hugo gene symbol (.byHugo), i.e. a slice of data", function() {
        expect(query.byHugo("GENE1")).toEqual(GENE1);
    });

    it("and by sample id (.bySampleId), i.e. another slice of data", function() {
        var CASE1 = {
            "GENE1": {
                mutation: null,
                cna: AMPLIFIED,
                mrna: UPREGULATED,
                rppa: DOWNREGULATED
            }
        };
        expect(query.bySampleId("CASE1")).toEqual(CASE1);
    });

    it("and get a particular entry of data (.data)", function() {
        var mutation_case2 = ["a"],
            cna_case2 = DELETED,
            mrna_case2 = DOWNREGULATED,
            rppa_case2 = null;

        expect(query.data("CASE2", "GENE1", "cna")).toBe(cna_case2);
        expect(query.data("CASE2", "GENE1", "mutation")).toEqual(mutation_case2);
        expect(query.data("CASE2", "GENE1", "mrna")).toBe(mrna_case2);
        expect(query.data("CASE2", "GENE1", "rppa")).toBe(rppa_case2);
    });

    it("Also, should be able to return a list of samples (.getSampleList)", function() {

        var sample_list = ["CASE1", "CASE2"];
        expect(query.getSampleList()).toEqual(sample_list);
    });

});
