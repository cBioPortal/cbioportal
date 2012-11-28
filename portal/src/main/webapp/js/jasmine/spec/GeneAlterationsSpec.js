describe("GeneAlterations", function() {
    var sendData = {
        cancer_study_id: "cancer",
        genes: "GENE1",
        cases: "CASE1 CASE2",
        geneticProfileIds: "profileId1"
    };

//    var gA = GeneAlterations(sendData);

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

    var gene_data = [GENE1],
        hugo_to_gene_index = {"GENE1": 0},
        samples = {"CASE1": 0, "CASE2": 1};

    var return_data =  { gene_data: gene_data,
        hugo_to_gene_index: hugo_to_gene_index,
        samples: samples
    };

    var query;
    beforeEach(function() {
        query = GeneAlterations.query(return_data);
    });

    it("should also test it's management of AJAX calls...but doesn't right now");

    it(".query.geneByHugo", function() {
        expect(query.geneByHugo("GENE1")).toEqual(GENE1);
    });

    it(".query.bySampleId", function() {
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

    it(".query.getSampleList", function() {

        var sample_list = ["CASE1", "CASE2"];
        expect(query.getSampleList()).toEqual(sample_list);
    });

    it(".query.data", function() {
        var mutation_case2 = ["a"],
            cna_case2 = DELETED,
            mrna_case2 = DOWNREGULATED,
            rppa_case2 = null;

        expect(query.data("CASE2", "GENE1", "cna")).toBe(cna_case2);
        expect(query.data("CASE2", "GENE1", "mutation")).toEqual(mutation_case2);
        expect(query.data("CASE2", "GENE1", "mrna")).toBe(mrna_case2);
        expect(query.data("CASE2", "GENE1", "rppa")).toBe(rppa_case2);
    });
});
