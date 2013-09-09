describe("AlleleFreqPlotUtils", function() {
    it("calculates frequency given an altered list and a reference list", function(){
        expect(AlleleFreqPlotUtils.process_data([1,2,3],[1,2,3])).toEqual([1/2, 1/2, 1/2]);
        expect(AlleleFreqPlotUtils.process_data([2,12,3],[1,2,3])).toEqual([2/3, 12/14, 1/2]);
    });
});


