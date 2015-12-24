function MiniOnco(plotDiv, miniOncoDiv, originalData){
    var stackedHistogram;

    // build a mini oncoprint
    // the mini oncoprint works as follows
    // Bar1: Query Genes
    //      - Altered Samples
    //      - Unaltered Samples
    // Bar2: Selected Gene
    //      - Query Genes Altered and Selected Gene Unaltered Samples
    //      - Query Genes Altered and Selected Gene Altered Samples
    //      - Query Genes Unaltered and Selected Gene Altered Samples
    //      - Query Genes Unaltered and Selected Gene Unaltered Samples
    this.render = function(current_gene){
        // fetch the alteredSamples
        window.QuerySession.getAlteredSamples().then(
            function (setAlt) {
                // when we have the data, create bardata and build the mini oncoprint
                var bardata = createBarData(setAlt.length, current_gene);
                buildMiniOnco(bardata);
            }
        )
    }

    // create the bar data which will be used to create the stacked histogram
    function createBarData(setSizeAlt, current_gene){
        // find the ratios from the table
        var ratioAltInAlt = 0;
        var ratioAltInUnalt = 0;
        for (var i = 0; i < originalData.length; i++) {
            if (current_gene == originalData[i]["Gene"]) {
                ratioAltInAlt = originalData[i]["percentage of alteration in altered group"];
                ratioAltInUnalt = originalData[i]["percentage of alteration in unaltered group"];
                break;
            }
        }

        if (ratioAltInAlt === "NaN"){
            ratioAltInAlt = 0;
        }
        if (ratioAltInUnalt === "NaN"){
            ratioAltInUnalt = 0;
        }

        // retrieve the total number of samples and calculate the setsizes
        var totalSetSize = window.QuerySession.getSampleIds().length;
        var setSizeUnalt = totalSetSize - setSizeAlt;
        var setSizeAltInAlt = setSizeAlt * ratioAltInAlt;
        var setSizeAltInUnalt = setSizeUnalt * ratioAltInUnalt;

        // create the bardata
        var bardata = [{
            barName: current_gene,
            barPieceName: "QGenes Unaltered, "+current_gene+" Unaltered",
            barPieceValue: setSizeUnalt - setSizeAltInUnalt,
            color: "lightgrey"
        }, {
            barName: current_gene,
            barPieceName: "QGenes Unaltered, "+current_gene+" Altered",
            barPieceValue: setSizeAltInUnalt,
            color: "#58ACFA"
        }, {
            barName: current_gene,
            barPieceName: "QGenes Altered, "+current_gene+" Altered",
            barPieceValue: setSizeAltInAlt,
            color: "#58ACFA"
        }, {
            barName: current_gene,
            barPieceName: "QGenes Altered, "+current_gene+" Unaltered",
            barPieceValue: setSizeAlt - setSizeAltInAlt,
            color: "lightgrey"
        }, {
            barName: "QGenes",
            barPieceName: "QGenes Unaltered",
            barPieceValue: setSizeUnalt,
            color: "lightgrey"
        },{
            barName: "QGenes",
            barPieceName: "QGenes Altered",
            barPieceValue: setSizeAlt,
            color: "#58ACFA"
        }];

        return bardata;
    }

    // create or update the histogram
    function buildMiniOnco(bardata){
        // if there is no mini-onco yet, append the div and create the histogram
        if (!stackedHistogram) {
            $("#" + plotDiv).append("<div id='" + miniOncoDiv + "'/>");
            stackedHistogram = new stacked_histogram("#" + miniOncoDiv);
            stackedHistogram.createStackedHistogram(bardata);
        }
        else{
            stackedHistogram.updateStackedHistogram(bardata);
            $("#"+miniOncoDiv).css("display", "block")
        }
    }

}
