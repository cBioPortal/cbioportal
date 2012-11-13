var OncoPrint = function(params) {

    var labelPadding = 40,
        labelSize = 50;

    var samples = params.data.samples,
        no_samples = samples.length,
        genes = params.data.genes,
        no_genes = Object.keys(genes).length;

    // make some calculations based on an existing oncoprint (a_oncoprint)
    var a_width = 1493,
        a_height = 75,
        a_trackHeight = 1 / no_genes,
        a_rectHeight = .8 * a_trackHeight,
        a_rectWidth = 5 / (a_width - labelSize),
        a_littleRectWidth = 7 / (a_width - labelSize),
        a_rectPadding = 3.5 / (a_width - labelSize);

    var scaled_dims = function(width, height) {

        var w = d3.interpolate(0, width);
        var h = d3.interpolate(0, height);

        return {
            trackHeight: h(a_trackHeight),
            rectHeight: h(a_rectHeight),
            rectWidth: w(a_rectWidth),
            littleRectWidth: w(a_littleRectWidth),
            rectPadding: w(a_rectPadding)
        };
    };

    var defaults = scaled_dims(a_width, a_height);

    var calcSvgWidth = function(defaults, no_samples) {
        return no_samples * (defaults.rectWidth + defaults.rectPadding)
            + labelSize + labelPadding;
    };

    var calcSvgHeight = function(defaults, no_tracks) {
        return defaults.trackHeight * no_tracks;
    };

//    var width = 3000;
    var width = (params.width + labelSize + labelPadding) || calcSvgWidth(defaults, no_samples);
    var height = (params.height + labelSize + labelPadding) || calcSvgHeight(defaults, no_genes);

//    var x = d3.scale.linear()
//        .domain([0, no_samples])
//        .range([0, width]);

    var x = function(kth_sample) {
        return kth_sample / no_samples * width;
    };

    var y = d3.scale.linear().range([0, height]);

    var scaled = scaled_dims(width, height);

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

    that.drawCNA = function(cna_data, samples, g_el, options, trackNum) {
        // draw CNA layer

        var _options = overrideDefaults(options, scaled);

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
            .attr('width', _options.rectWidth)
            .attr('height', _options.rectHeight)
//            .attr('fill', colorCNA)
            .attr('x', function(d, i) {
                return labelPadding + labelSize + x(i);
            })
            .attr('y', _options.trackHeight * trackNum);
    };

    that.drawMutation = function(mutation_data, samples, g_el, options, trackNum) {
        // draw the mutation layer

        var _options = overrideDefaults(options, scaled);

        var littleRectHeight = _options.rectHeight / 3;

        g_el.selectAll('rect.mutation')
            .data(mutation_data)
            .enter()
            .append('rect')
            .attr('class', function(d) {
                return 'mutation ' + (d !== null ? "mut" : "none");
            })
            .attr('id', function(d, i) {
                return samples[i];
            })
            .attr('width', _options.littleRectWidth)
            .attr('height', littleRectHeight)
            .attr('x', function(d, i) {
                var padding = labelSize + labelPadding + i * (_options.rectWidth + _options.rectPadding)
                    - (_options.littleRectWidth - _options.rectWidth) / 2;
//                    + (_options.littleRectWidth / 2);

                return padding;
            })
            .attr('y', _options.trackHeight * trackNum  // displace down for each track
            + (_options.rectHeight - littleRectHeight) / 2);
    };

    that.drawTrack = function(params, trackNum) {

        var settings = overrideDefaults(params, scaled);

        var g_el = params.svg.append('g')
            .attr('class', params.label);

        var labels = params.svg.append('text')
            .attr('class', params.label)
//            .attr('width', 200)
            .attr('id', 'labels');
//            .attr('left', 0)
//            .attr('right', 0)
//            .attr('top', 'auto')
//            .attr('bottom', 0)
//            .attr('position', 'fixed');

        var label_dy = (trackNum * settings.trackHeight) + settings.trackHeight / 2;

        // draw name
        labels.append('tspan')
            .text(params.label)
            .attr('font-size', '12px')
            .attr('fill', 'black')
            .attr('font-family', 'sans-serif')
//            .attr('text-anchor', 'end')
            .attr('y', label_dy);

        // draw the percent changed
        labels.append('tspan')
            .text(params.gene_data.percent_altered)
            .attr('font-size', '12px')
            .attr('fill', 'black')
            .attr('font-family', 'sans-serif')
            .attr('text-anchor', 'end')
            .attr('dx', labelSize)
            .attr('y', label_dy);

        var cna = params.gene_data.cna;
        that.drawCNA(cna, samples, g_el, settings, trackNum);

        var mutation_data = params.gene_data.mutations;
        that.drawMutation(mutation_data, samples, g_el, settings, trackNum);

//        that.drawMRNA(mrna_data, svg, trackSettings);
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

        // slider
        $('<div>', { id: "width_slider", width: "100", display:"inline"})
            .slider({
                min: 0,
//                max: 7,
                max: scaled.rectPadding * 2,
                step: scaled.rectPadding * .2,
                value: scaled.rectPadding ,
                change: function(event, ui) {
//                    console.log(ui.value);

                    d3.selectAll('#oncoprints #' + params.cancer_study_id + ' rect.cna')
                        .transition()
                        .duration(200)
                        .attr('x', function(d, i) {
                            var padding = labelSize + labelPadding
                                + (i % no_samples) * (scaled.rectWidth + ui.value);

                            return padding;
                        });

                    d3.selectAll('#oncoprints #' + params.cancer_study_id + ' rect.mutation')
                        .transition()
                        .duration(200)
                        .attr('x', function(d, i) {
                            var padding = labelSize + labelPadding
                                + (i % no_samples) * (scaled.rectWidth + ui.value)
                                + (scaled.rectWidth - scaled.littleRectWidth) / 2;

                            return padding;
                        })
                        .attr('width', function(d, i) {
                            if (ui.value === 0) {
                                return scaled.rectWidth;
                            } else {
                                return scaled.littleRectWidth;
                            }
                        });

                    var _defaults = overrideDefaults({}, defaults);
                    _defaults.rectPadding = ui.value;
                    var width = calcSvgWidth(_defaults, no_samples);

                    d3.selectAll('#oncoprints svg')
                        .attr('width', width);

//                    console.log(d3.selectAll('#oncoprints svg');
//                    console.log(defaults.rectPadding);
                }
            })
            .appendTo(customizeDiv);

        // Toggle altered cases
        $('<span>', {
            text: "Only Show Altered Cases"
        }).appendTo(customizeDiv);

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
            .attr('width', width)
            .attr('height', height)      // 50 for the key at the bottom
            .style('overflow', 'hidden')
            .attr('xmlns',  'http://www.w3.org/2000/svg');

        // todo: eventually this will be refactored into it's own function
        var trackNum = 0;

        // todo: is it really that bad that these settings are
        // going to be passed to all subsequent functions that don't need them?
        genes.forEach(function(gene) {
            // draw each track
            that.drawTrack({
                label: gene.hugo,
                svg: svg,
                trackSettings: scaled,
                gene_data: gene
            }, trackNum);

            trackNum +=1;

        });
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

