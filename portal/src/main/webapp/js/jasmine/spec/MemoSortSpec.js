describe("MemoSort", function() {

    var AMPLIFIED = "AMPLIFIED",
        DELETED = "DELETED",
        HOMODELETED = "HOMODELETED";

    var UPREGULATED = "UPREGULATED",
        DOWNREGULATED = "DOWNREGULATED";

    var setup = function(gene_data) {

        var hugo_to_gene_index = {};

        gene_data.forEach(function(gene, i) {
            hugo_to_gene_index[gene.hugo] = i;
        });

        var samples = { "CASE1": 0,
            "CASE2": 1,
            "CASE3": 2,
            "CASE4": 3 };

        var gene_data =  { gene_data: gene_data,
            hugo_to_gene_index: hugo_to_gene_index,
            samples: samples
        };

        return MemoSort(gene_data,
            QueryGeneData(gene_data).getGeneList());
    };

    it(".comparator cna", function() {

        var GENE1 = { hugo: "GENE1",
            cna: [AMPLIFIED, HOMODELETED, null],
            mutations: [null, null, null],
            mrna: [null, null, null],
            rppa: [null, null, null],
            percent_altered: "10%"
        };

        var gene_data = [GENE1];

        var comparator = setup(gene_data).comparator;

        expect(comparator("CASE1", "CASE1")).toBe(0);
        expect(comparator("CASE1", "CASE2")).toBe(-1);
        expect(comparator("CASE2", "CASE1")).toBe(1);

        expect(comparator("CASE2", "CASE3")).toBe(-1);
    });

    it(".comparator mutation", function() {
        var GENE1 = { hugo: "GENE1",
            cna: [null, null, null],
            mutations: [['a'], null, null],
            mrna: [null, null, null],
            rppa: [null, null, null],
            percent_altered: "10%"
        };

        var gene_data = [GENE1];

        var comparator = setup(gene_data).comparator;

        expect(comparator("CASE2", "CASE2")).toBe(0);
        expect(comparator("CASE1", "CASE2")).toBe(-1);
        expect(comparator("CASE2", "CASE1")).toBe(1);
    });

    it(".comparator mrna", function() {
        var GENE1 = { hugo: "GENE1",
            cna: [null, null, null],
            mutations: [null, null, null],
            mrna: [UPREGULATED, DOWNREGULATED, null],
            rppa: [null, null, null],
            percent_altered: "10%"
        };

        var gene_data = [GENE1];

        var comparator = setup(gene_data).comparator;

        expect(comparator("CASE2", "CASE2")).toBe(0);
        expect(comparator("CASE1", "CASE2")).toBe(-1);
        expect(comparator("CASE2", "CASE3")).toBe(-1);
        expect(comparator("CASE3", "CASE2")).toBe(1);
    });

    it(".comparator rppa", function() {
        var GENE1 = { hugo: "GENE1",
            cna: [null, null, null],
            mutations: [null, null, null],
            mrna: [null, null, null],
            rppa: [UPREGULATED, DOWNREGULATED, null],
            percent_altered: "10%"
        };

        var gene_data = [GENE1];

        var comparator = setup(gene_data).comparator;

        expect(comparator("CASE2", "CASE2")).toBe(0);
        expect(comparator("CASE1", "CASE2")).toBe(-1);
        expect(comparator("CASE2", "CASE3")).toBe(-1);
        expect(comparator("CASE3", "CASE2")).toBe(1);
    });

    it(".sort", function() {
        var GENE1 = {
            hugo: "GENE1",
            cna:        [AMPLIFIED, HOMODELETED, null, null],
            mutations:  [['a mutation'], null, ['a mutation'], null],
            mrna:       [null, null, null, null],
            rppa:       [null, null, null, null],
            percent_altered: "10%"
        };

        var gene_data = [GENE1];

        var memoSort = setup(gene_data);


        expect(memoSort.sort()).toEqual(
            ['CASE1', 'CASE2', 'CASE3', 'CASE4']
        );
    });

    it("should sort cases for multiple genes", function() {
        var GENE1 = {
            hugo: "GENE1",
            cna:        [AMPLIFIED, HOMODELETED, null, null],
            mutations:  [null, null, null, null],
            mrna:       [null, null, null, null],
            rppa:       [null, null, null, null],
            percent_altered: "10%"
        };

        var GENE2 = {
            hugo: "GENE2",
            cna:        [null, null, HOMODELETED, AMPLIFIED],
            mutations:  [null, null, null, null],
            mrna:       [null, null, null, null],
            rppa:       [null, null, null, null],
            percent_altered: "10%"
        };

        var gene_data = [GENE1, GENE2];

        var memoSort = setup(gene_data);

        expect(memoSort.sort()).toEqual(
            ['CASE1', 'CASE2', 'CASE4', 'CASE3']
        );
    });
});
