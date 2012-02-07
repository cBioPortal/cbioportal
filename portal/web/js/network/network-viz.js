
// send graphml to cytoscape web for visualization
function send2cytoscapeweb(graphml, div_id) {
    var visual_style = {
        global: {
            backgroundColor: "#fefefe", //#F7F6C9 //#F3F7FE
            tooltipDelay: 250
        },
        nodes: {
                                    shape: {
                                       discreteMapper: {
                                                    attrName: "type",
                                                    entries: [
                                                            { attrValue: "Protein", value: "ELLIPSE" },
                                                            { attrValue: "SmallMolecule", value: "DIAMOND" },
                                                            { attrValue: "Drug", value: "HEXAGON" },
                                                            { attrValue: "Unknown", value: "TRIANGLE" }
                                                    ]
                                            }
                                    },
            borderWidth: 1,
                                    borderColor: {
                                            discreteMapper: {
                                                    attrName: "type",
                                                    entries: [
                                                            { attrValue: "Protein", value: "#000000" },
                                                            { attrValue: "SmallMolecule", value: "#000000" },
                                                            { attrValue: "Drug", value: "#000000" },
                                                            { attrValue: "Unknown", value: "#000000" }
                                                    ]
                                            }
                                    },
            size: {
                defaultValue: 25,
                continuousMapper: { attrName: "weight", minValue: 25, maxValue: 75 }
            },
            color: {
                discreteMapper: {
                    attrName: "type",
                    entries: [
                                                            { attrValue: "Protein", value: "#FFFFFF" },
                                                            { attrValue: "SmallMolecule", value: "#FFFFFF" }, //#D7AC85
                                                            { attrValue: "Drug", value: "#FFA500" },
                                                            { attrValue: "Unknown", value: "#FFFFFF" } //#69A19E
                    ]
                }
            },
            labelHorizontalAnchor: "center",
            labelVerticalAnchor: "bottom",
            labelFontSize: 10,
            selectionGlowColor: "#f6f779",
                                    selectionGlowOpacity: 0.8,
                                    hoverGlowColor: "#cbcbcb", //#ffff33
                                    hoverGlowOpacity: 1.0,
                                    hoverGlowStrength: 8,
                                    tooltipFont: "Verdana",
                tooltipFontSize: 12,
                tooltipFontColor: "#EE0505",
                tooltipBackgroundColor: "#000000",
                tooltipBorderColor: "#000000"
        },
        edges: {
            width: 1,
                                    mergeWidth: 2,
                                    mergeColor: "#666666",
                                    targetArrowShape: {
                    defaultValue: "NONE",
                                            discreteMapper: {
                                                    attrName: "type",
                                                    entries: [
                                                            { attrValue: "STATE_CHANGE", value: "DELTA" },
                                                            { attrValue: "DRUG_TARGET", value: "T" }]
                                    }
            },
                                    color: {
                                            defaultValue: "#A583AB", // color of all other types
                                            discreteMapper: {
                                                    attrName: "type",
                                                    entries: [
                                                            { attrValue: "IN_SAME_COMPONENT", value: "#CD976B" },
                                                            { attrValue: "REACTS_WITH", value: "#7B7EF7" },
                                                            { attrValue: "DRUG_TARGET", value: "#A52A2A" },
                                                            { attrValue: "STATE_CHANGE", value: "#67C1A9" } ]
                                    }
                            }                    	
                            }
                    };

    // initialization options
    var options = {
        swfPath: "swf/CytoscapeWeb",
        flashInstallerPath: "swf/playerProductInstall"
    };

    var vis = new org.cytoscapeweb.Visualization(div_id, options);

    vis.ready(function() {
            // init UI of the network tab
            initNetworkUI(vis);

        // set the style programmatically
        document.getElementById("color").onclick = function(){
            vis.visualStyle(visual_style);
        };
    });

    var draw_options = {
        // your data goes here
        network: graphml,
        edgeLabelsVisible: false,
        edgesMerged: true,
        layout: "ForceDirected",
        visualStyle: visual_style,
        panZoomControlVisible: true
    };

    vis.draw(draw_options);
};