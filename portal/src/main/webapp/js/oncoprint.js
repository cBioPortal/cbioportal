
var OncoPrint = function(params) {

    OncoPrint.DEFAULTS = (function() {
        // defaults and settings for standard oncoprint
        // and which are paired with the standard oncoprint data structure
        var private = {
            // size of genetic alteration
            'ALTERATION_WIDTH'                  : 7,
            'ALTERATION_HEIGHT'                 : 21, // if this changes, MakeOncoPrint.CELL_HEIGHT needs to change
            // padding between genetic alteration boxes
            'ALTERATION_VERTICAL_PADDING'       : 1,
            'ALTERATION_HORIZONTAL_PADDING'     : 1,
            // cna styles
            'CNA_AMPLIFIED_COLOR'               : "#FF0000",
            'CNA_GAINED_COLOR'                  : "#FFB6C1",
            'CNA_DIPLOID_COLOR'                 : "#D3D3D3",
            'CNA_HEMIZYGOUSLYDELETED_COLOR'     : "#8FD8D8",
            'CNA_HOMODELETED_COLOR'             : "#0000FF",
            'CNA_NONE_COLOR'                    : "#D3D3D3",
            // mrna styles
            'MRNA_WIREFRAME_WIDTH_SCALE_FACTOR' : 1/6,
            'MRNA_WIREFRAME_WIDTH_SCALE_FACTOR2': 1/4,
            'MRNA_UPREGULATED_COLOR'            : "#FF9999",
            'MRNA_DOWNREGULATED_COLOR'          : "#6699CC",
            'MRNA_NOTSHOWN_COLOR'               : "#FFFFFF",
            // mutation styles
            'MUTATION_COLOR'                    : "#008000",
            'MUTATION_HEIGHT_SCALE_FACTOR'      : 1/3,
            // rppa styles
            'RPPA_COLOR'                        : "#000000",
            'RPPA_HOMDEL_COLOR'                        : "#FFFFFF",
            // labels
            'LABEL_COLOR'                       : "#666666",
            'LABEL_FONT'                        : "normal 12px verdana",
            'LABEL_SPACING'                     : 15, // space between gene and percent altered_
            'LABEL_PADDING'                     : 3,  // padding (in pixels) between label and first genetic alteration
            'CASE_SET_DESCRIPTION_LABEL'        : "Case Set:i",
            // legend
            'LEGEND_SPACING'                    : 5,  // space between alteration and description
            'LEGEND_PADDING'                    : 15, // space between alteration / descriptions pairs
            'LEGEND_FOOTNOTE_SPACING'           : 10, // space between alteration / descriptions pairs and footnote
            // tooltip region
            'TOOLTIP_REGION_WIDTH'              : 400, // width of tooltip region
            'TOOLTIP_REGION_HEIGHT'             : 60,  // height of tooltip region
            'TOOLTIP_TEXT_REGION_Y'             : 20,  // start of the rect within the tooltip region
            'TOOLTIP_HORIZONTAL_PADDING'        : 10,  // space between header region and tooltip region
            'TOOLTIP_TEXT_FONT'                 : "normal 12px arial",
            'TOOLTIP_TEXT_COLOR'                : "#000000",
            'TOOLTIP_FILL_COLOR'                : "#EEEEEE",
            'TOOLTIP_MARGIN'                    : 10,
            'TOOLTIP_TEXT'                      : "Move the mouse pointer over the OncoPrint below for more details\nabout cases and alterations.",
            'ALT_TOOLTIP_TEXT'                  : "Details about cases and alterations are not available when the whitespace\nhas been removed from the OncoPrint.",
            // header
            'HEADER_VERTICAL_SPACING'           : 15, // space between sentences that wrap
            'HEADER_VERTICAL_PADDING'           : 25, // space between header sentences
            // general sample properties
            'ALTERED_SAMPLES_ONLY'              : false,
            // scale factor
            'SCALE_FACTOR_X'                    : 1.0,
            // remove genomics alteration padding
            'REMOVE_GENOMIC_ALTERATION_HPADDING': false,

            // These bits are passed as "AlterationSettings" in DrawAlteration -
            // they corresponded to names found in org.mskcc.cbio.portal.model.GeneticEventImpl
            // CNA bits
            'CNA_AMPLIFIED'           : (1<<0),
            'CNA_GAINED'              : (1<<1),
            'CNA_DIPLOID'             : (1<<2),
            'CNA_HEMIZYGOUSLYDELETED' : (1<<3),
            'CNA_HOMODELETED'         : (1<<4),
            'CNA_NONE'                : (1<<5),
            // MRNA bits (normal is in GeneticEventImpl, but never used)
            'MRNA_UPREGULATED'        : (1<<6),
            'MRNA_DOWNREGULATED'      : (1<<7),
            'MRNA_NOTSHOWN'           : (1<<8),
            // RPPA bits (we should distinguish bet. normal & notshow - same with MRNA)
            'RPPA_UPREGULATED'        : (1<<9),
            'RPPA_NORMAL'             : (1<<10),
            'RPPA_DOWNREGULATED'      : (1<<11),
            'RPPA_NOTSHOWN'           : (1<<12),
            // MUTATION bits
            'MUTATED'                 : (1<<13),
            'NORMAL'                  : (1<<14)
    };
        return {
            get: function(name) { return private[name]; }
        };
    })();

    var that = {};

    that.drawTrack = function(geneAlterationsObj, svg, options) {

        var colorCNA = function(d) {
            // helper function

            if (d.alteration & OncoPrint.DEFAULTS.get('CNA_AMPLIFIED')) {
                return DEFAULTS.get('CNA_AMPLIFIED_COLOR');
            }
            else if (d.alteration & OncoPrint.DEFAULTS.get('CNA_GAINED')) {
                return  DEFAULTS.get('CNA_GAINED_COLOR');
            }
            else if (d.alteration & OncoPrint.DEFAULTS.get('CNA_DIPLOID')) {
                return DEFAULTS.get('CNA_DIPLOID_COLOR');
            }
            else if (d.alteration & OncoPrint.DEFAULTS.get('CNA_HEMIZYGOUSLYDELETED')) {
                return DEFAULTS.get('CNA_HEMIZYGOUSLYDELETED_COLOR');
            }
            else if (d.alteration & OncoPrint.DEFAULTS.get('CNA_HOMODELETED')) {
                return DEFAULTS.get('CNA_HOMODELETED_COLOR');
            }
            else if (d.alteration & OncoPrint.DEFAULTS.get('CNA_NONE')) {
                return DEFAULTS.get('CNA_NONE_COLOR');
            }
            else {
                console.log("colorCNA fell through ", d.alteration);
            }
        };

        var label = geneAlterationsObj.hugoGeneSymbol,
            alts = geneAlterationsObj.alterations;

        var g_el = svg
            .append('g')
            .attr('class', label);

        var trackHeight = options.trackNum * options.trackHeight;

        // draw name
        g_el.append('text')
            .text(label)
            .attr('font-size', '12px')
            .attr('fill', 'black')
            .attr('font-family', 'sans-serif')
            .attr('y', 16 + trackHeight);

        // draw the percent changed
        g_el.append('text')
            .text(geneAlterationsObj.percent_altered)
            .attr('font-size', '12px')
            .attr('fill', 'black')
            .attr('font-family', 'sans-serif')
            .attr('x', options.labelPadding)
            .attr('y', 16 + trackHeight);

        var rectHeight = 19;
        var rectWidth =  5;

        // draw CNA layer
        g_el.selectAll('rect.cna')
            .data(alts)
            .enter()
            .append('rect')
            .attr('class', 'cna')
            .attr('id', function(d) {
                return d.sample;
            })
            .attr('width', rectWidth)
            .attr('height', rectHeight)
            .attr('fill', colorCNA)
            .attr('x', function(d, i) {
                return options.labelPadding + options.percentPadding + i * (5 + options.rectPadding);
            })
            .attr('y', trackHeight);

        var littleRectHeight = 7;
        var littleRectWidth = options.rectPadding === 0 ? rectWidth : 7;

        // draw the mutation layer
        g_el.selectAll('rect.mutation')
            .data(alts)
            .enter()
            .append('rect')
            .attr('class', 'mutation')
            .attr('width', littleRectWidth)
            .attr('height', littleRectHeight)
            .attr('fill', function(d) {
                if (d.alteration & MUTATED) {
                    return DEFAULTS.get('MUTATION_COLOR');
                }
                else {
                    return 'transparent';
                }
            })
            .attr('x', function(d, i) {
                return options.labelPadding + options.percentPadding + i * (5 + options.rectPadding) -  // todo: this is code duplication
                    (options.rectPadding === 0 ? 0 : 1);   // if padding is zero, don't center the little rect
            })
            .attr('y', trackHeight + (rectHeight - littleRectHeight) / 2);
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

        // slider
        $('<div>', { id: "width_slider" })
            .slider({
                change: function(event, ui) {
                    var num_alterations = params.geneAlterations_l[0].alterations.length;

                    d3.selectAll('#oncoprints #' + params.cancer_study_id + ' rect')
                        .transition()
//                        .duration(100)
                        .attr('x', function(d, i) {
                            var mutation_padding = 0;

                            return ((i % num_alterations) * ui.value)
                                - (d.mutation === undefined ? 0 : 20)
                        });
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
            .attr('width', 3559)
            .attr('height', 25 * params.geneAlterations_l.length + 50)      // 50 for the key at the bottom
            .style('overflow', 'hidden')
            .attr('xmlns',  'http://www.w3.org/2000/svg');

        // draw each track
        var trackSettings = {
            rectPadding: 3.5,
            labelPadding: 50,
            percentPadding: 27,
            trackHeight: 25
        };

        $.each(params.geneAlterations_l, function(i, val) {
            trackSettings.trackNum = i;     // increment the track number
            that.drawTrack(val, svg, trackSettings);
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

