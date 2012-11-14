var OncoPrint = function(params) {

//    var getLaneOffset = function() {
//        // returns the width of the label which someone else computes
//        return $('#' + params.cancer_study_id + '.oncoprint #labels')[0].getBoundingClientRect().width;
//    };

    var samples = params.data.samples,
        no_samples = samples.length,
        genes = params.data.genes,
        no_genes = Object.keys(genes).length;

    var rectPadding = 1.1;
    var rectWidth = 5.5;
    var trackPadding = 3;
    var rectHeight = 23;

    var calcLaneWidth = function(rectWidth, rectPadding, no_samples) {
        // lane : the rectangles
        // track : the label and the rectangles together, this is just my naming convention
        return no_samples * (rectWidth + rectPadding);
    };

    var calcSvgHeight = function(rectHeight, trackPadding, no_tracks) {
        return (rectHeight + trackPadding) * no_tracks;
    };

    var laneWidth = calcLaneWidth(rectWidth, 2, no_samples);
    var height = calcSvgHeight(rectHeight, trackPadding, no_genes);

    var x = d3.scale.linear()
        .domain([0, no_samples])
        .range([0, laneWidth]);

    var y = d3.scale.linear().range([0, height]);

    // include whatever isn't included in options from defaults
    // for keys that are in both, choose the options
    var overrideDefaults = function(options, defaults) {
        for (var _default in defaults) {
            if (!options[_default]) {
                options[_default] = defaults[_default];
            }
        }

        return options;
    };

    var that = {};

    that.drawCNA = function(cna_data, samples, g_el, trackNum) {
        // draw CNA layer

        var dy = trackNum === 0 ? 0 : (rectHeight * trackNum) + trackPadding;
        console.log("dy", dy);

        g_el.selectAll('rect.cna')
            .data(cna_data)
            .enter()
            .append('rect')
            .attr('class', function(d, i) {
                // d is one of (AMPLIFIED, GAINED, DIPLOID, HEMIZYGOUSLYDELETED, HOMODELETED, null)
                return 'cna ' + (d === null ? "NONE" : d);

            })
            .attr('id', function(d, i) {
                return samples[i];      // index back into the array of samples
            })
            .attr('width', rectWidth)
            .attr('height', rectHeight)
//            .attr('fill', colorCNA)
            .attr('x', function(d, i) {
                return  x(i) * rectPadding;
            })
            .attr('y', dy);
    };

//    that.drawMutation = function(mutation_data, samples, g_el, options, trackNum) {
//        // draw the mutation layer
//
//        var _options = overrideDefaults(options, scaled);
//
//        var littleRectHeight = _options.rectHeight / 3;
//
//        g_el.selectAll('rect.mutation')
//            .data(mutation_data)
//            .enter()
//            .append('rect')
//            .attr('class', function(d) {
//                return 'mutation ' + (d !== null ? "mut" : "none");
//            })
//            .attr('id', function(d, i) {
//                return samples[i];
//            })
//            .attr('width', _options.littleRectWidth)
//            .attr('height', littleRectHeight)
//            .attr('x', function(d, i) {
////                var padding =  i * (_options.rectWidth + _options.rectPadding)
////                    - (_options.littleRectWidth - _options.rectWidth) / 2;
//////                    + (_options.littleRectWidth / 2);
//
//                return x(i) * 2;
//            })
//            .attr('y', _options.trackHeight * trackNum  // displace down for each track
//            + (_options.rectHeight - littleRectHeight) / 2);
//    };

    that.drawTrack = function(params, trackNum) {

        var g_el = params.svg.append('g')
            .attr('class', params.label);

//        var labels = params.svg.append('text')
//            .attr('class', params.label)
////            .attr('width', 200)
//            .attr('id', 'labels');
////            .attr('left', 0)
////            .attr('right', 0)
////            .attr('top', 'auto')
////            .attr('bottom', 0)
////            .attr('position', 'fixed');
//
//        var label_dy = (trackNum * settings.trackHeight) + settings.trackHeight / 2;
//
//        // draw name
//        labels.append('tspan')
//            .text(params.label)
//            .attr('font-size', '12px')
//            .attr('fill', 'black')
//            .attr('font-family', 'sans-serif')
////            .attr('text-anchor', 'end')
//            .attr('y', label_dy);
//
//        // draw the percent changed
//        labels.append('tspan')
//            .text(params.gene_data.percent_altered)
//            .attr('font-size', '12px')
//            .attr('fill', 'black')
//            .attr('font-family', 'sans-serif')
//            .attr('text-anchor', 'end')
//            .attr('dx', labelWidth)
//            .attr('y', label_dy);

        var cna = params.gene_data.cna;
        that.drawCNA(cna, samples, g_el, trackNum);

        var mutation_data = params.gene_data.mutations;
//        that.drawMutation(mutation_data, samples, g_el, settings, trackNum);

//        that.drawMRNA(mrna_data, svg, trackSettings);
    };

    that.drawTracks = function(svg, genes, trackSettings) {
        var trackNum = 0;

        genes.forEach(function(gene) {
            // draw each track
            that.drawTrack({
                label: gene.hugo,
                svg: svg,
                gene_data: gene
            }, trackNum);

            trackNum +=1;

            // todo: is it really that bad that these settings are
            // going to be passed to all subsequent functions that don't need them?
        });
    };


    that.insertFullOncoPrint = function(div) {
        var oncoPrintDiv = $('<div/>', {
            class: "oncoprint", id: params.cancer_study_id
        }).appendTo(div);

        var getSVG = $('<p/>', {
            text: "Get OncoPrint"
        }).append($("<input type='submit' value='SVG'>"));

        var customizeOncoPrint =  $('<p/>', {
            text: "Customize OncoPrint:"
        });

        var customizeDiv = $('<div>', {
            id: "customize"
        });

        $('<span>', { text: "White Space"
        }).appendTo(customizeDiv);

        $('<input>', {
            type: "checkbox"
//            onclick: OncoPrint.UI.hideWhiteSpace($(''))   ...blah blah
        }).appendTo(customizeDiv);

        // slider
//        $('<div>', { id: "width_slider", width: "100", display:"inline"})
//            .slider({
//                min: 0,
////                max: 7,
//                max: scaled.rectPadding * 2,
//                step: scaled.rectPadding * .2,
//                value: scaled.rectPadding ,
////                create: function(event, ui) {
////                    return change(event, ui);
////                },
//                change: function(event, ui) {
////                    console.log(ui.value);
//
//                    d3.selectAll('#oncoprints #' + params.cancer_study_id + ' rect.cna')
//                        .transition()
//                        .duration(200)
//                        .attr('x', function(d, i) {
//                            var padding =
//                                 (i % no_samples) * (scaled.rectWidth + ui.value);
//
//                            return padding;
//                        });
//
//                    d3.selectAll('#oncoprints #' + params.cancer_study_id + ' rect.mutation')
//                        .transition()
//                        .duration(200)
//                        .attr('x', function(d, i) {
//                            var padding =
//                                 (i % no_samples) * (scaled.rectWidth + ui.value)
//                                + (scaled.rectWidth - scaled.littleRectWidth) / 2;
//
//                            return padding;
//                        })
//                        .attr('width', function(d, i) {
//                            if (ui.value === 0) {
//                                return scaled.rectWidth;
//                            } else {
//                                return scaled.littleRectWidth;
//                            }
//                        });
//
//                    var _defaults = overrideDefaults({}, defaults);
//                    _defaults.rectPadding = ui.value;
//                    var width = calcLaneWidth(_defaults, no_samples);
//
//                    d3.selectAll('#oncoprints svg')
//                        .attr('width', width);
//
////                    console.log(d3.selectAll('#oncoprints svg');
////                    console.log(defaults.rectPadding);
//                }
//            })
//            .appendTo(customizeDiv);

        // Toggle altered cases
        $('<span>', {
            text: "Only Show Altered Cases"
        }).appendTo(customizeDiv);

        // scale oncoprint
        $('<span>', { text: "scale oncoprint"
        }).appendTo(customizeDiv);

        // scale oncoprint
        $('<div>', { id: "width_slider", width: "100", display:"inline"})
            .slider({
                min: 1,
//                max: 7,
                max: rectWidth * 2,
                step: .5,
                value: rectWidth,
                change: function(event, ui) {
                    console.log(ui.value);

                    // todo: need to fix this
                    var width = calcLaneWidth(ui.value, 2, no_samples);

                    var x = d3.scale.linear()
                        .domain([0, no_samples])
                        .range([0, width]);

                    d3.selectAll('#oncoprints #' + params.cancer_study_id + ' rect.cna')
                        .transition()
                        .duration(200)
                        .attr('width', function(d, i) {
                            return ui.value;
                        })
                        .attr('x', function(d, i) {
                            return x(i % no_samples) * rectPadding;
                        });

                    d3.selectAll('#oncoprints #' + params.cancer_study_id + ' rect.mutation')
                        .transition()
                        .duration(200)
                        .attr('width', function(d, i) {
                            return ui.value + 1;
                        })
                        .attr('x', function(d, i) {
                            return x(i % no_samples) * rectPadding - .5;
                        });

//                    d3.selectAll('#oncoprints svg')
//                        .transition()
//                        .duration(200)
//                        .attr('width', width);
                }
            })
            .appendTo(customizeDiv);

        $('<input>', {
            type: "checkbox"
        })
        .click(function() {
            console.log($('#oncoprints #' + params.cancer_study_id + ' rect'));
        }).appendTo(customizeDiv);


        var caseSet =  $('<p/>', {
            text: params.case_set_str
        });

        var alteredIn = $('<p/>', {
            text: "Altered in "
                + params.num_cases_affected
                + " (" + params.percent_cases_affected + ")"
                + " of cases"
        });

        oncoPrintDiv.append(getSVG);
        oncoPrintDiv.append(customizeOncoPrint);
        oncoPrintDiv.append(customizeDiv);
        oncoPrintDiv.append(caseSet);
        oncoPrintDiv.append(alteredIn);

        var wrapper = $('<div>', {
            class: 'wrapper',
            style: 'width: 1205px; overflow:scroll;'
        }).appendTo(oncoPrintDiv);

        var svg = d3.select(wrapper[0]).append('svg')
            .attr('width', laneWidth)
//            .attr('viewBox', "0 0 600 600")
            .attr('height', height)      // 50 for the key at the bottom
            .style('overflow', 'hidden')
            .attr('xmlns',  'http://www.w3.org/2000/svg');

        that.drawTracks(svg, genes);
    };

//    holding off on this until I have a visualization.  It will make things much easier
//
//    that.sort = function() {
//        // sort the samples according to the order that the genes are in,
//        // in params.geneAlterations_l
//
//        var sort_helper = function(alt1, alt2) {
//
//        };
//
//    };

    return that;
};

// todo: OncoPrint.drawTrack, make this publicly available so that you can draw tracks without having to initialize,
// or include all UI



//OncoPrint.drawTrack.longestLabel = ;

OncoPrint.help = function() {
//  div is the HTML div element to append an oncoprint to
//
//  params is an object literal like this:
// { cancer_study_id,
// case_set_str,
// num_cases_affected,
// percent_cases_affected,
// geneAlterations_l }
};


//OncoPrint.DEFAULTS = (function() {
//    // defaults and settings for standard oncoprint
//    // and which are paired with the standard oncoprint data structure
//    var private = {
//        // size of genetic alteration
//        'ALTERATION_WIDTH'                  : 7,
//        'ALTERATION_HEIGHT'                 : 21, // if this changes, MakeOncoPrint.CELL_HEIGHT needs to change
//        // padding between genetic alteration boxes
//        'ALTERATION_VERTICAL_PADDING'       : 1,
//        'ALTERATION_HORIZONTAL_PADDING'     : 1,
//        // cna styles
//        'CNA_AMPLIFIED_COLOR'               : "#FF0000",
//        'CNA_GAINED_COLOR'                  : "#FFB6C1",
//        'CNA_DIPLOID_COLOR'                 : "#D3D3D3",
//        'CNA_HEMIZYGOUSLYDELETED_COLOR'     : "#8FD8D8",
//        'CNA_HOMODELETED_COLOR'             : "#0000FF",
//        'CNA_NONE_COLOR'                    : "#D3D3D3",
//        // mrna styles
//        'MRNA_WIREFRAME_WIDTH_SCALE_FACTOR' : 1/6,
//        'MRNA_WIREFRAME_WIDTH_SCALE_FACTOR2': 1/4,
//        'MRNA_UPREGULATED_COLOR'            : "#FF9999",
//        'MRNA_DOWNREGULATED_COLOR'          : "#6699CC",
//        'MRNA_NOTSHOWN_COLOR'               : "#FFFFFF",
//        // mutation styles
//        'MUTATION_COLOR'                    : "#008000",
//        'MUTATION_HEIGHT_SCALE_FACTOR'      : 1/3,
//        // rppa styles
//        'RPPA_COLOR'                        : "#000000",
//        'RPPA_HOMDEL_COLOR'                        : "#FFFFFF",
//        // labels
//        'LABEL_COLOR'                       : "#666666",
//        'LABEL_FONT'                        : "normal 12px verdana",
//        'LABEL_SPACING'                     : 15, // space between gene and percent altered_
//        'LABEL_PADDING'                     : 3,  // padding (in pixels) between label and first genetic alteration
//        'CASE_SET_DESCRIPTION_LABEL'        : "Case Set:i",
//        // legend
//        'LEGEND_SPACING'                    : 5,  // space between alteration and description
//        'LEGEND_PADDING'                    : 15, // space between alteration / descriptions pairs
//        'LEGEND_FOOTNOTE_SPACING'           : 10, // space between alteration / descriptions pairs and footnote
//        // tooltip region
//        'TOOLTIP_REGION_WIDTH'              : 400, // width of tooltip region
//        'TOOLTIP_REGION_HEIGHT'             : 60,  // height of tooltip region
//        'TOOLTIP_TEXT_REGION_Y'             : 20,  // start of the rect within the tooltip region
//        'TOOLTIP_HORIZONTAL_PADDING'        : 10,  // space between header region and tooltip region
//        'TOOLTIP_TEXT_FONT'                 : "normal 12px arial",
//        'TOOLTIP_TEXT_COLOR'                : "#000000",
//        'TOOLTIP_FILL_COLOR'                : "#EEEEEE",
//        'TOOLTIP_MARGIN'                    : 10,
//        'TOOLTIP_TEXT'                      : "Move the mouse pointer over the OncoPrint below for more details\nabout cases and alterations.",
//        'ALT_TOOLTIP_TEXT'                  : "Details about cases and alterations are not available when the whitespace\nhas been removed from the OncoPrint.",
//        // header
//        'HEADER_VERTICAL_SPACING'           : 15, // space between sentences that wrap
//        'HEADER_VERTICAL_PADDING'           : 25, // space between header sentences
//        // general sample properties
//        'ALTERED_SAMPLES_ONLY'              : false,
//        // scale factor
//        'SCALE_FACTOR_X'                    : 1.0,
//        // remove genomics alteration padding
//        'REMOVE_GENOMIC_ALTERATION_HPADDING': false,
//
//        // These bits are passed as "AlterationSettings" in DrawAlteration -
//        // they corresponded to names found in org.mskcc.cbio.portal.model.GeneticEventImpl
//        // CNA bits
//        'CNA_AMPLIFIED'           : (1<<0),
//        'CNA_GAINED'              : (1<<1),
//        'CNA_DIPLOID'             : (1<<2),
//        'CNA_HEMIZYGOUSLYDELETED' : (1<<3),
//        'CNA_HOMODELETED'         : (1<<4),
//        'CNA_NONE'                : (1<<5),
//        // MRNA bits (normal is in GeneticEventImpl, but never used)
//        'MRNA_UPREGULATED'        : (1<<6),
//        'MRNA_DOWNREGULATED'      : (1<<7),
//        'MRNA_NOTSHOWN'           : (1<<8),
//        // RPPA bits (we should distinguish bet. normal & notshow - same with MRNA)
//        'RPPA_UPREGULATED'        : (1<<9),
//        'RPPA_NORMAL'             : (1<<10),
//        'RPPA_DOWNREGULATED'      : (1<<11),
//        'RPPA_NOTSHOWN'           : (1<<12),
//        // MUTATION bits
//        'MUTATED'                 : (1<<13),
//        'NORMAL'                  : (1<<14)
//    };
//    return {
//        get: function(name) { return private[name]; }
//    };
//})();

