describe("OncoprintUtils", function() {

    describe("is_discrete", function() {

        it("should be false if it is passed a number", function() {
            expect(OncoprintUtils.is_discrete(1)).toBe(false);
        });

        it("should be true if it is passed a string (actually, anything that is not a number)", function() {
            expect(OncoprintUtils.is_discrete("a")).toBe(true);
            expect(OncoprintUtils.is_discrete("")).toBe(true);
            expect(OncoprintUtils.is_discrete(undefined)).toBe(true);
            expect(OncoprintUtils.is_discrete([])).toBe(true);
            expect(OncoprintUtils.is_discrete({})).toBe(true);
        });
    });

    describe("nest_data", function() {

        it("nests on the key 'sample'", function() {
            var sample = { "sample": "sample_0",
                "rppa": "UPREGULATED",
                "gene": "GeneA",
                "cna": "HEMIZYGOUSLYDELETED" };

            var samples = [ sample ];
            expect(OncoprintUtils.nest_data(samples))
                .toEqual([ {key: "sample_0", values: [ sample ]} ]);
        });
    });

    describe("get_attr", function() {

        it("extracts a gene if it's given a piece of genomic data,", function() {
            expect(OncoprintUtils.get_attr({gene: "EGFR", blahblah: "foobar"}))
                .toBe("EGFR");
        });

        it("even if there's no actual data", function() {
            expect(OncoprintUtils.get_attr({gene: "EGFR"}))
                .toBe("EGFR");
        });

        it("and an attr_id if it is a general attribute (e.g. clinical data)", function() {
            expect(OncoprintUtils.get_attr({attr_id: "VITAL_STATUS", helloworld: "blah"}))
                .toBe("VITAL_STATUS");
        });

        it("again, even if there's no actual value", function() {
            expect(OncoprintUtils.get_attr({attr_id: "VITAL_STATUS"}))
                .toBe("VITAL_STATUS");
        });

        describe("and even does a little bit of data validation", function() {

            it("throws an error if there are neither an attr_id or a gene", function() {
                expect(function() {
                    OncoprintUtils.get_attr({});
                }).toThrow("datum has neither a gene nor an attr_id: " + JSON.stringify({}));

                expect(function() {
                    OncoprintUtils.get_attr({a: 1});
                }).toThrow("datum has neither a gene nor an attr_id: " + JSON.stringify({a: 1}));
            });
        });
    });

    describe("filter_by_attributes", function() {
    });
});

    //describe("", function() {});
    //it("", function() {});
