describe("QueryGeneData", function() {
    var AMPLIFIED = "AMPLIFIED",
        DELETED = "DELETED";

    var UPREGULATED = "UPREGULATED",
        DOWNREGULATED = "DOWNREGULATED";

    var GENE1 = { hugo: "GENE1",
        hugo: "GENE1",
        cna: [AMPLIFIED, DELETED, null],
        mutations: [null, ["a"], null],
        mrna: [UPREGULATED, DOWNREGULATED, null],
        rppa: [null, null, null],
        percent_altered: "10%"
    };

    var GENE2 = { hugo: "GENE2",
        hugo: "GENE2",
        cna: [AMPLIFIED, DELETED, null],
        mutations: [null, ["a"], null],
        mrna: [UPREGULATED, DOWNREGULATED, null],
        rppa: [null, null, null],
        percent_altered: "10%"
    };

    var genes = [GENE1, GENE2],
        hugo_to_gene_index = {"GENE1": 0, "GENE2": 1},
        samples = {"CASE1": 0, "CASE2": 1, "CASE3":2};

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
                rppa: null
            },
            "GENE2": {
                mutation: null,
                cna: AMPLIFIED,
                mrna: UPREGULATED,
                rppa: null
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

    it("and, should be able to return a list of samples (.getSampleList).", function() {

        var sample_list = ["CASE1", "CASE2", "CASE3"];
        expect(query.getSampleList()).toEqual(sample_list);
    });

    it('should return whether or not a sample is altered (.isSampleAltered)', function() {
        expect(query.isSampleAltered("CASE1")).toBe(true);
        expect(query.isSampleAltered("CASE3")).toBe(false);
    });

    it('should return a list of unaltered samples (.getUnalteredSamples)', function() {
        expect(query.unaltered_samples).toEqual(["CASE3"]);
    });

    it('also should return a list of altered samples (.getUnalteredSamples)', function() {
        expect(query.altered_samples).toEqual(["CASE1", "CASE2"]);
    });

    it('get non null data types', function() {
        expect(query.data_types).toEqual(["cna", "mutation", "mrna"]);
    })
});
