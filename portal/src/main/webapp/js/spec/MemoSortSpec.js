define(["src/oncoprint/MemoSort"], function(MemoSort) {
    describe("MemoSort", function() {

        it("sort samples with multiple genes", function() {

            var sample1 = { "sample": "sample_1",
                "gene": "GeneA",
                "cna": "AMPLIFIED" };

            var sample2 = {
                "sample": "sample_2",
                "gene": "GeneA",
                "cna": "AMPLIFIED" };

            var data = d3.nest()
                .key(function(d) { return d.sample; })
                .entries(
                    [
                    { "sample": "sample_1", "gene": "GeneA", "cna": "AMPLIFIED" },
                    { "sample": "sample_1", "gene": "GeneB"},
                    { "sample": "sample_2", "gene": "GeneA", "cna": "AMPLIFIED" },
                    { "sample": "sample_2", "gene": "GeneB", "cna": "AMPLIFIED" }
                    ]);

            expect(MemoSort(data, ["GeneA", "GeneB"]).map(function(i) { return i.key })).toEqual(["sample_2", "sample_1"])
        });
    });
});
