var OncoPrint = function(params) {

//    var getLaneOffset = function() {
//        // returns the width of the label which someone else computes
//        return $('#' + params.cancer_study_id + '.oncoprint #labels')[0].getBoundingClientRect().width;
//    };

    var query = GeneAlterations.query(params.data);

    var samples = d3.map(params.data.samples),
        no_samples = samples.keys().length,
        sorted_samples = samples.keys().sort(function(x,y) { return samples.get(y) - samples.get(x);}),
        gene_indexes = d3.map(samples.huo_to_gene_index),
        gene_data = params.data.gene_data,
        no_genes = gene_data.length;

//    console.log(genes);

    var selector_id = '#oncoprints #' + params.cancer_study_id;

    var rectPadding = 1.5;
    var rectWidth = 5.5;
    var calcLittleRectWidth = function(width) { return (rectWidth + 1) / rectWidth * width;};
    var trackPadding = 3;
    var rectHeight = 23;
    var littleRectHeight = rectHeight / 3;

    // global boolean (reflects a state)
    var toggleRectPadding = rectPadding;

    var calcLaneWidth = function(rectWidth, no_samples) {
        // lane : the rectangles
        // track : the label and the rectangles together, this is just my naming convention,
        return no_samples * rectWidth;
    };

    var calcSvgHeight = function(rectHeight, trackPadding, no_tracks) {
        return (rectHeight + trackPadding) * no_tracks;
    };

    var laneWidth = calcLaneWidth(rectWidth, no_samples);
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

    // helper functions
    var xpos_cna =  function(d, i) {
        var pos = x(i % no_samples);
        return toggleRectPadding === 0 ? pos : pos * rectPadding;
    };

    var xpos_mutation = function(d, i) {
        var pos = x(i % no_samples) - .5;
        return toggleRectPadding === 0 ? pos : pos * rectPadding;
    };

    var redrawCNA = function(width) {
        // redrawing the CNA bars
        d3.selectAll(selector_id + ' rect.cna')
            .transition()
//            .ease('cubic-in-out')
            .attr('width', width)
            .attr('x', xpos_cna);
    };

    var redrawMutation = function(width) {
        d3.selectAll(selector_id + ' rect.mutation')
            .transition()
//            .ease('cubic-in-out')
            .attr('width', width)
            .attr('x', xpos_mutation);
    };

    var that = {};

    that.drawCNA = function(cna_data, g_el, trackNum) {
        // draw CNA layer

        var dy = trackNum === 0 ? 0 : (rectHeight + trackPadding) * trackNum;
//        console.log("dy", dy);

        g_el.selectAll('rect.cna')
            .data(cna_data)
            .enter()
            .append('rect')
            .attr('class', function(d, i) {
                // d is one of (AMPLIFIED, GAINED, DIPLOID, HEMIZYGOUSLYDELETED, HOMODELETED, null)
                return 'cna ' + (d === null ? "NONE" : d);

            })
            .attr('id', function(d, i) {
                return sorted_samples[i];      // index back into the array of samples
            })
            .attr('width', rectWidth)
            .attr('height', rectHeight)
            .attr('x', xpos_cna)
            .attr('y', dy);
    };

    that.drawMutation = function(mutation_data, g_el, trackNum) {
        // draw the mutation layer

        var dy = (rectHeight - littleRectHeight) / 2;
        dy = trackNum === 0 ? dy : dy + (rectHeight + trackPadding) * trackNum;

        var littleRectWidth = calcLittleRectWidth(rectWidth);

        g_el.selectAll('rect.mutation')
            .data(mutation_data)
            .enter()
            .append('rect')
            .attr('class', function(d) {
                return 'mutation ' + (d !== null ? "mut" : "none");
            })
            .attr('id', function(d, i) {
                return sorted_samples[i];
            })
            .attr('width', littleRectWidth)
            .attr('height', littleRectHeight)
            .attr('x', xpos_mutation)
            .attr('y', dy);
    };

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
        that.drawCNA(cna, g_el, trackNum);

        var mutation_data = params.gene_data.mutations;
        that.drawMutation(mutation_data, g_el, trackNum);

//        that.drawMRNA(mrna_data, svg, trackSettings);
    };

    that.drawTracks = function(svg) {
        var trackNum = 0;

        gene_data.forEach(function(gene) {
            // draw each track
            that.drawTrack({
                label: gene.hugo,
                svg: svg,
                gene_data: gene
            }, trackNum);

            trackNum +=1;
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

        $('<div>', {
            text: "Remove White Space"
        }).appendTo(customizeDiv);

        // toggle white space checkbox
        $('<input>', {
            type: "checkbox",
            id: "toggle_whitespace"
        }).click(function() {
            var isChecked = $(this).is(':checked');
            var rectWidth = $(selector_id + ' #width_slider').slider('value');
            var littleRectWidth = calcLittleRectWidth(rectWidth);

            if (isChecked) {
                // remove white space
                toggleRectPadding = 0;
                redrawCNA(rectWidth);
                redrawMutation(rectWidth);
            } else {
                // make white space
                toggleRectPadding = rectPadding;
                redrawCNA(rectWidth);
                redrawMutation(littleRectWidth);
            }
        }).appendTo(customizeDiv);

        $('<div>', {
            text: "Only Show Altered Cases"
        }).appendTo(customizeDiv);

        // toggle unaltered checkbox
        $('<input>', {
            type: 'checkbox',
            id: "toggle_unaltered"
        }).appendTo(customizeDiv);

        // scale oncoprint
        $('<div>', { text: "scale oncoprint"
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
//                    console.log(ui.value);

                    var width = calcLaneWidth(ui.value, no_samples);
                    var littleRectWidth = calcLittleRectWidth(ui.value);

                    // update this global state variable
                    x = d3.scale.linear()
                        .domain([0, no_samples])
                        .range([0, width]);

                    // redraw
                    redrawCNA(ui.value);
                    redrawMutation(littleRectWidth);

                    // todo: modify size of the svg element
//                    d3.selectAll('#oncoprints svg')
//                        .transition()
//                        .duration(200)
//                        .attr('width', width);
                }
            })
            .appendTo(customizeDiv);

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

        that.sort = function(new_indexes) {
            svg.selectAll('rect')
                .transition()
                .duration(1000)
                .attr('x', function(d, i) {
                    var sample_id = this.getAttribute('id');

                    var pos = x(new_indexes[sample_id] % no_samples);
                    return toggleRectPadding === 0 ? pos : pos * rectPadding;
                });
        };

        that.drawTracks(svg);

        that.memoSort = function() {
            // todo: save the memoSort somewhere so that you don't have to redo the sort
            var memoSort = MemoSort(params.data, "BRCA1");
            var sorted = memoSort.sort();
            that.sort(sorted);

            return sorted;
        };
    };


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

