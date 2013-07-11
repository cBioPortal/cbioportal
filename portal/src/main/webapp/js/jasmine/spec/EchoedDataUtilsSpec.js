describe("EchoedDataUtils", function() {
    var utils = EchoedDataUtils;

    it("mangles data into a list of objects indexed by sample_id", function() {

        var inward = [
            {value: "-1", sample_id: "TCGA-BL-A0C8", datatype: "cna", hugo: "GENE1"},
            {value: "A123B", sample_id: "TCGA-BL-A0C8", datatype: "mutation", hugo: "GENE1"}
        ];

        var outward = [ {sample_id: "TCGA-BL-A0C8", cna: "-1", mutation: "A123B", hugo: "GENE1"} ];

        expect(utils.compress(inward)).toEqual(_.chain(outward));
    });

    it("knows how to make data oncoprint friendly", function() {

        var inward = [ {sample_id: "TCGA-BL-A0C8", cna: "-1", mutation: "A123B", hugo: "GENE1"} ];
        var outward = [ {sample: "TCGA-BL-A0C8", cna: "HEMIZYGOUSLYDELETED", mutation: "A123B", hugo: "GENE1"} ];

        expect(utils.oncoprint_wash_inner(_.chain(inward))).toEqual(outward);
    })
});
