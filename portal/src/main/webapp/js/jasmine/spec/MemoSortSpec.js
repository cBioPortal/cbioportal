describe("MemoSort", function() {

    var AMPLIFIED = "AMPLIFIED",
        DELETED = "DELETED";

    var UPREGULATED = "UPREGULATED",
        DOWNREGULATED = "DOWNREGULATED";

    var setup = function(GENE1) {

        var gene_data = [GENE1],
            hugo_to_gene_index = {"GENE1": 0},
            samples = { "CASE1": 0,
                "CASE2": 1,
                "CASE3": 2,
                "CASE4": 3 };

        var geneAlterations =  { gene_data: gene_data,
            hugo_to_gene_index: hugo_to_gene_index,
            samples: samples
        };

        return MemoSort(geneAlterations, "GENE1");
    };


    it(".comparator cna", function() {

        var GENE1 = { hugo: "GENE1",
            cna: [AMPLIFIED, DELETED, null],
            mutations: [null, null, null],
            mrna: [null, null, null],
            rppa: [null, null, null],
            percent_altered: "10%"
        };

        var comparator = setup(GENE1).comparator;

        expect(comparator("CASE1", "CASE1")).toBe(0);
        expect(comparator("CASE1", "CASE2")).toBe(1);
        expect(comparator("CASE2", "CASE1")).toBe(-1);

        expect(comparator("CASE2", "CASE3")).toBe(1);
    });

    it(".comparator mutation", function() {
        var GENE1 = { hugo: "GENE1",
            cna: [null, null, null],
            mutations: [['a'], null, null],
            mrna: [null, null, null],
            rppa: [null, null, null],
            percent_altered: "10%"
        };

        var comparator = setup(GENE1).comparator;

        expect(comparator("CASE2", "CASE2")).toBe(0);
        expect(comparator("CASE1", "CASE2")).toBe(1);
        expect(comparator("CASE2", "CASE1")).toBe(-1);
    });

    it(".comparator mrna", function() {
        var GENE1 = { hugo: "GENE1",
            cna: [null, null, null],
            mutations: [null, null, null],
            mrna: [UPREGULATED, DOWNREGULATED, null],
            rppa: [null, null, null],
            percent_altered: "10%"
        };

        var comparator = setup(GENE1).comparator;

        expect(comparator("CASE2", "CASE2")).toBe(0);
        expect(comparator("CASE1", "CASE2")).toBe(1);
        expect(comparator("CASE2", "CASE3")).toBe(1);
        expect(comparator("CASE3", "CASE2")).toBe(-1);
    });

    it(".comparator rppa", function() {
        var GENE1 = { hugo: "GENE1",
            cna: [null, null, null],
            mutations: [null, null, null],
            mrna: [null, null, null],
            rppa: [UPREGULATED, DOWNREGULATED, null],
            percent_altered: "10%"
        };

        var comparator = setup(GENE1).comparator;

        expect(comparator("CASE2", "CASE2")).toBe(0);
        expect(comparator("CASE1", "CASE2")).toBe(1);
        expect(comparator("CASE2", "CASE3")).toBe(1);
        expect(comparator("CASE3", "CASE2")).toBe(-1);
    });

    it(".sort", function() {
        var GENE1 = { hugo: "GENE1",
            hugo: "GENE1",
            cna:        [AMPLIFIED, DELETED, null, null],
            mutations:  [['a mutation'], null, ['a mutation'], null],
            mrna:       [null, null, null, null],
            rppa:       [null, null, null, null],
            percent_altered: "10%"
        };

        var memoSort = setup(GENE1);

        expect(memoSort.sort()).toEqual({
            "CASE1": 3,
            "CASE2": 2,
            "CASE3": 1,
            "CASE4": 0
        });
    });
});
