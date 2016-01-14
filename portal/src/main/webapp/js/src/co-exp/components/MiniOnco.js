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
        // find the numbers in the table
        var nrAltInAlt = 0;
        var nrAltInUnalt = 0;
        for (var i = 0; i < originalData.length; i++) {
            if (current_gene == originalData[i]["Gene"]) {
                // the cell contains something like 30////0.2345, so the first entry gives us our number
                nrAltInAlt = Number(originalData[i]["percentage of alteration in altered group"].split("\/")[0]);
                nrAltInUnalt = Number(originalData[i]["percentage of alteration in unaltered group"].split("\/")[0]);
                break;
            }
        }

        // retrieve the total number of samples and calculate the setsizes
        var totalSetSize = window.QuerySession.getSampleIds().length;
        var setSizeUnalt = totalSetSize - setSizeAlt;

        // create the bardata
        var bardata = [{
            barName: current_gene,
            barPieceName: "QGenes Unaltered, "+current_gene+" Unaltered",
            barPieceValue: setSizeUnalt - nrAltInUnalt,
            color: "lightgrey"
        }, {
            barName: current_gene,
            barPieceName: "QGenes Unaltered, "+current_gene+" Altered",
            barPieceValue: nrAltInUnalt,
            color: "#58ACFA"
        }, {
            barName: current_gene,
            barPieceName: "QGenes Altered, "+current_gene+" Altered",
            barPieceValue: nrAltInAlt,
            color: "#58ACFA"
        }, {
            barName: current_gene,
            barPieceName: "QGenes Altered, "+current_gene+" Unaltered",
            barPieceValue: setSizeAlt - nrAltInAlt,
            color: "lightgrey"
        }, {
            barName: "Query Genes",
            barPieceName: "QGenes Unaltered",
            barPieceValue: setSizeUnalt,
            color: "lightgrey"
        },{
            barName: "Query Genes",
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
