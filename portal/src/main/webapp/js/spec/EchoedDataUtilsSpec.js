require("EchoedDataUtils", function() {

    describe("EchoedDataUtils", function() {
        var utils = EchoedDataUtils;

        it("mangles data into a list of objects indexed by sample_id", function() {

            var inward = [
        {value: "-1", sample_id: "TCGA-BL-A0C8", datatype: "cna", gene: "GENE1"},
            {value: "A123B", sample_id: "TCGA-BL-A0C8", datatype: "mutation", gene: "GENE1"}
        ];

        var outward = [ {sample_id: "TCGA-BL-A0C8", cna: "-1", mutation: "A123B", gene: "GENE1"} ];

        expect(utils.compress(inward)).toEqual(_.chain(outward));
        });

        it("knows how to make data oncoprint friendly", function() {

            var inward = [ {sample_id: "TCGA-BL-A0C8", cna: "-1", mutation: "A123B", gene: "GENE1"} ];

            // this is what oncoprint data looks like
            var outward = [ {sample: "TCGA-BL-A0C8", cna: "HEMIZYGOUSLYDELETED", mutation: "A123B", gene: "GENE1"} ];

            expect(utils.inner.oncoprint_wash(_.chain(inward))).toEqual(_.chain(outward));
        });

        it("knows how to join mutation together for a particular sample and gene *on oncoprint data*" , function() {
            var inward = [
        {sample: "TCGA-BL-A0C8", mutation: "A123B", gene: "GENE1"},
            {sample: "TCGA-BL-A0C8", mutation: "B123C", gene: "GENE1"},
            {sample: "sample2", mutation: "A123B", gene: "GENE1"},
            {sample: "sample2", mutation: "B123C", gene: "GENE1"}
        ];

        expect(utils.join_mutations(",", _.chain(inward))).toEqual(_.chain([{
            sample: "TCGA-BL-A0C8",
            mutation:"A123B,B123C",
            gene: "GENE1"
        },
        {
            sample: "sample2",
            mutation:"A123B,B123C",
            gene: "GENE1"
        }
        ]));
        });

        it("removes undefined values from objects", function() {
            expect(utils.remove_undefined(_.chain([{sample: "sample1", mutation: undefined}]))).toEqual(
                _.chain([{sample: "sample1"}]));
        })

        it("knows how to make a cna tsv string into data", function() {

            var str_in = "Hugo_Symbol\tEntrez_Gene_Id\tTCGA-BL-A0C8\tTCGA-BL-A13I\nBRCA2\t675\t0\t0";

            var data_out = [ {sample_id: "TCGA-BL-A0C8", gene: "BRCA2", cna: "0"},
                {sample_id: "TCGA-BL-A13I", gene: "BRCA2", cna: "0"} ];

            expect(utils.parse_cna_tsv(str_in)).toEqual(data_out);
        });
    });

});
