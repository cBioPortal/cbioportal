var Oncoprint = function(params) {

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

        that.sort = function(new_indices) {
            svg.selectAll('rect')
                .transition()
                .duration(1000)
                .attr('x', function(d, i) {
                    var sample_id = this.getAttribute('id');

                    var pos = x(new_indices[sample_id] % no_samples);
                    return toggleRectPadding === 0 ? pos : pos * rectPadding;
                });
        };

        that.drawTracks(svg);

        that.memoSort = function() {
            // todo: save the memoSort somewhere so that you don't have to redo the sort
            var memoSort = MemoSort(params.data, "BRCA2");
            var sorted = memoSort.sort();
            that.sort(sorted);

            return sorted;
        };
    };


    return that;
};
