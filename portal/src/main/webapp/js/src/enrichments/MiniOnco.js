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
        var bardata;
        // retrieve the total number of samples and calculate the setsizes
        var totalSetSize = window.QuerySession.getSampleIds().length;
        var setSizeUnalt = totalSetSize - setSizeAlt;

        if(!stackedHistogram){
            bardata = createEmptyBar(setSizeUnalt, setSizeAlt);
            //bardata = createSelectedGeneData(current_gene, setSizeUnalt, setSizeAlt);
        }
        else{
            bardata = createSelectedGeneData(current_gene, setSizeUnalt, setSizeAlt);
        }

        bardata.push({
            barName: "Query Genes",
            barPieceName: "Query Genes Unaltered: "+setSizeUnalt,
            barPieceValue: setSizeUnalt,
            color: "lightgrey"
        });

        bardata.push({
            barName: "Query Genes",
            barPieceName: "Query Genes Altered: "+setSizeAlt,
            barPieceValue: setSizeAlt,
            color: "#58ACFA"
        });

        return bardata;
    }

    /**
     * Creates an empty row for when the mini-onco is created
     * @param setSizeUnalt
     * @param setSizeAlt
     * @returns {*[]}
     */
    function createEmptyBar(setSizeUnalt, setSizeAlt){
        return [{
            barName: "None selected",
            barPieceName: "Select gene in table or volcano plot",
            barPieceValue: setSizeUnalt + setSizeAlt,
            color: "lightgrey",
            opacity: 0
        },{
            barName: "None selected",
            barPieceName: "dummy1",
            barPieceValue: 0,
            color: "#58ACFA",
            opacity: 0
        },{
            barName: "None selected",
            barPieceName: "dummy2",
            barPieceValue: 0,
            color: "#58ACFA",
            opacity: 0
        },{
            barName: "None selected",
            barPieceName: "dummy3",
            barPieceValue: 0,
            color: "lightgrey",
            opacity: 0
        }
        ];
    }

    function createSelectedGeneData(current_gene, setSizeUnalt, setSizeAlt){
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

        var selectedGeneData = [{
            barName: current_gene,
            barPieceName: "Query Genes Unaltered, "+current_gene+" Unaltered: "+(setSizeUnalt - nrAltInUnalt),
            barPieceValue: setSizeUnalt - nrAltInUnalt,
            color: "lightgrey"
        }, {
            barName: current_gene,
            barPieceName: "Query Genes Unaltered, "+current_gene+" Altered: "+nrAltInUnalt,
            barPieceValue: nrAltInUnalt,
            color: "#58ACFA"
        }, {
            barName: current_gene,
            barPieceName: "Query Genes Altered, "+current_gene+" Altered: "+nrAltInAlt,
            barPieceValue: nrAltInAlt,
            color: "#58ACFA"
        }, {
            barName: current_gene,
            barPieceName: "Query Genes Altered, "+current_gene+" Unaltered: "+(setSizeAlt - nrAltInAlt),
            barPieceValue: setSizeAlt - nrAltInAlt,
            color: "lightgrey"
        }];
        return selectedGeneData;
    }

    // create or update the histogram
    function buildMiniOnco(bardata){
        // if there is no mini-onco yet, append the div and create the histogram
        if (!stackedHistogram) {
            $("#" + plotDiv).append("<div id='" + miniOncoDiv + "' style='margin-left: -20px;'/>");
            stackedHistogram = new stacked_histogram("#" + miniOncoDiv);
            stackedHistogram.createStackedHistogram(bardata);
            stackedHistogram.addTextToLane("None selected", "Select gene in table or volcano plot");

        }
        else{
            stackedHistogram.removeTextFromLane("None selected");
            stackedHistogram.updateStackedHistogram(bardata);
            $("#"+miniOncoDiv).css("display", "block");
        }
    }

}
