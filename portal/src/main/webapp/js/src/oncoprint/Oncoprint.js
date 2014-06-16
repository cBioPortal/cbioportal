//
//
// Gideon Dresdner <dresdnerg@cbio.mskcc.org> June 2013
//
;
// Creates an oncoprint on the div.
// The parameters is an object that contains:
// clinicalData, clinical_attrs (list), genes (list), geneData,
//
// and width (number) for the width of the oncoprint
// and legend (undefined or div element)
define("Oncoprint",
        [           "OncoprintUtils",  "MemoSort"],
        function(   utils,              MemoSort) {
            return function(div, params) {
                params.clinicalData = params.clinicalData || [];        // initialize
                params.clinical_attrs = params.clinical_attrs || [];

                // make strings of numbers into numbers
                var clinicalData = params.clinicalData.map(function(i) {
                    if (!utils.is_discrete(i.attr_val)) {
                        i.attr_val = parseInt(i.attr_val);
                    }
                    return i;
                });


                var data = clinicalData.concat(params.geneData);

                var clinical_attrs = params.clinical_attrs      // extract attr_ids
                    .map(function(attr) { return attr.attr_id; });

                // handle the case when a list of strings is passed instead of proper
                // clinical attribute objects
                clinical_attrs = clinical_attrs.filter(function(i) { return i !== undefined; });
                clinical_attrs = clinical_attrs || params.clinical_attrs;

                var attributes = clinical_attrs.concat(params.genes);

                data = utils.process_data(data, attributes);
                PortalDataColl.setOncoprintData(data); 

                // keeps track of the order specified by the user (translates to vertical
                // order in the visualization)
                var attr2index = (function() {
                    var to_return = {};

                    attributes.forEach(function(attr, i) {
                        to_return[attr] = i;
                    });

                    return to_return;
                }());

                // returns the vertical position of the attr
                var vertical_pos = function(attr) {
                    return dims.vert_space * attr2index[attr];
                };

                if (clinicalData === []
                        && params.clinical_attrs !== undefined) {
                    throw {
                        name: "Data Mismatch Error",
                            message: "There are clinical attributes for nonexistant clinical data"
                    }
                }

                var id2ClinicalAttr = utils.createId2ClinicalAttr(params.clinical_attrs);

                var dims = (function() {
                    var rect_height = 23;
                    var mut_height = rect_height / 3;
                    var vert_padding = 6;
                    var label_width = utils.label_width(attributes.map(
                            function(attr) {
                                var maybe = utils.maybe_map(id2ClinicalAttr);
                                var value = maybe(attr);
                                return value === attr ? value : value.display_name;

                            }));

                    var clinical_height = (2/3) * rect_height;
                    var clinical_offset = (1/6) * rect_height;

                    return {
                        width: data.length * (5.5 + 3),
                    height: (rect_height + vert_padding) * attributes.length,
                    rect_height: rect_height,
                    rect_width: 5.5,
                    vert_padding: vert_padding,
                    vert_space: rect_height + vert_padding,
                    hor_padding: 3,
                    mut_height: mut_height,
                    label_width: label_width,
                    clinical_height: clinical_height,
                    clinical_offset: clinical_offset
                    };
                }());
                //    var margin = { top: 80, right: 80, left: 80, bottom: 80 };

                // make labels and set up the table for proper scrolling, etc.
                var table = d3.select(div)
                    .append('table')
                    .append('tr')
                    .attr('valign','top');

                var remove_oncoprint = function() {
                    d3.select("#" + div.id + ' table').remove();
                };

                // hack to get the label flush with the tracks in Firefox
                // the discrepancy is due to the difference in the way browsers display
                // <table>.  Assume that other browsers behave like chrome and that Firefox
                // is the exception.
                var is_firefox = navigator.userAgent.indexOf("Firefox") !== -1;
                var browser_offset = is_firefox ? 16 : 0;

                var label_svg = table
                    .append('td')
                    .append('svg')
                    .attr('height', dims.height + browser_offset)
                    .attr('width', '' + dims.label_width)
                    .attr('id', 'label');

                var label = label_svg.selectAll('text')
                    .data(attributes)
                    .enter()
                    .append('text')
                    .attr('font-size', '12px')
                    .attr('x', 0)
                    .attr('y', function(d) {
                        return (dims.vert_space / 1.80) + vertical_pos(d); });

                var gene2percent = utils.percent_altered(params.geneData);

                label.append('tspan')       // name
                    .attr('text-anchor', 'start')
                    .attr('font-weight', 'bold')
                    .text(function(d) {
                        var maybe = utils.maybe_map(id2ClinicalAttr);
                        var value = maybe(d);
                        return value === d ? value : value.display_name;
                    });
                label.append('tspan')       // percent_altered
                    .text(function(d) {
                        return (d in gene2percent) ? gene2percent[d].toString() + "%" : ""; })
                    .attr('x', '' + dims.label_width)
                    .attr('text-anchor', 'end')
                    // remove the tspan that would have contained the percent altered
                    // because it messes with the label placement in the pdf download
                    .filter(function(d) { return gene2percent[d] === undefined; }).remove();


                var container_width = $('#td-content').width();              // snatch this from the main portal page
                container_width = (container_width ? container_width : params.width);    // see if this has specified by user
                container_width = (container_width ? container_width : 1250);            // default setting
                var main_svg = table
                    .append('td')
                    .append('div')      // control overflow to the right
                    .style('width', container_width - 70 - dims.label_width + 'px') // buffer of, say, 70
                    .style('display', 'inline-block')
                    .style('overflow-x', 'auto')
                    .style('overflow-y', 'hidden')
                    .append("svg")
                    .attr('width', dims.width)
                    .attr('height', dims.height);

                var colors = utils.colors;     // alias

                // helper function
                // *signature:* number, number -> string
                var translate = function(x,y) {
                    return "translate(" + x + "," + y + ")";
                };

                // params: data
                // updating the samples based on the data (entering and exiting)
                var update = function(data) {

                    // toss in the samples
                    var columns = main_svg.selectAll('g')               // array of arrays
                        .data(data, function(d) { return d.key; });     // N.B.

                    // throw them in, 100 (i.e. way off) to the right
                    columns.enter()
                        .append('g')
                        .attr('class', 'sample')
                        //        .attr('transform', function(d,i) { return translate(x(d.key), 0); })
                        .attr('transform', translate(dims.width + 100, 0));

                    // simply remove columns on exit
                    columns.exit().remove();

                    var els = columns.selectAll('.oncoprint-els')
                        .data(function(d) {
                            return d.values;
                        });

                    els.exit().remove();

                    var enter = els.enter();

                    var attr2range = utils.make_attribute2scale(params.clinical_attrs, clinicalData);

                    // N.B. fill doubles as cna
                    var fill = enter.append('rect')
                        .attr('fill', function(d) {
                            if (utils.is_gene(d)) {
                                return utils.cna_fills[d.cna];
                            }
                            else if (utils.is_clinical(d)) {
                                return d.attr_val === "NA"
                            ? colors.grey       // attrs with value of NA are colored grey
                            : attr2range[d.attr_id](d.attr_val);
                            }
                        })
                    .attr('height', function(d) {
                        return d.attr_id === undefined ? dims.rect_height : dims.clinical_height;
                        //     return dims.rect_height;
                    })
                    .attr('width', dims.rect_width)
                        .attr('y', function(d) {
                            return d.attr_id === undefined
                            ? vertical_pos(utils.get_attr(d))
                            : vertical_pos(utils.get_attr(d)) + dims.clinical_offset;
                        //     return vertical_pos(utils.get_attr(d));
                        });

                    var fusion = enter.append('path')
                        .attr('d', "M0,0L0,"+dims.rect_height+" "+dims.rect_width+","+dims.rect_height/2+"Z")
                        .attr('transform',function(d) {return 'translate(0,'+(vertical_pos(utils.get_attr(d)))+')';});
                    fusion.filter(function(d) {
                        return d.mutation === undefined || !/fusion($|,)/i.test(d.mutation.toLowerCase());
                    }).remove();

                    var mut = enter.append('rect')
                        .attr('fill', 'green')
                        .attr('height', dims.mut_height)
                        .attr('width', dims.rect_width)
                        .attr('y', function(d) {
                            return dims.mut_height + vertical_pos(utils.get_attr(d)); });
                    mut.filter(function(d) {
                        if (d.mutation === undefined) return true;
                        var aas = d.mutation.split(","); // e.g. A32G,fusion
                        for (var i=0, n=aas.length; i<n; i++) {
                            if (!/fusion$/i.test(aas[i])) return false;
                        }
                        return true;
                    }).remove();
                    
                    var sym = d3.svg.symbol().size(dims.rect_width * 3);
                    var rppa = enter.append('path')
                        .attr('d', sym.type(function(d) {
                            return d.rppa === "UPREGULATED" ? "triangle-up" : "triangle-down"; }))
                        .attr('transform', function(d) {
                            // put the triangle in the right spot: at the top if
                            // UNREGULATED, at the bottom otherwise
                            var dy = dims.rect_height;
                            dy = d.rppa === "UPREGULATED" ? dy * 0.1 : dy / 1.1;
                            return translate(dims.rect_width / 2, dy + vertical_pos(utils.get_attr(d))); });
                        rppa.filter(function(d) {
                            return d.rppa === undefined;
                        }).remove();

                    var mrna = enter.append('rect')
                        .attr('y', function(d) { return vertical_pos(utils.get_attr(d)); })
                        .attr('height', dims.rect_height)
                        .attr('width', dims.rect_width)
                        .attr('stroke-width', 2)
                        .attr('stroke-opacity', 1)
                        .attr('stroke', function(d) { return d.mrna === "UPREGULATED" ? '#FF9999' : '#6699CC' })
                        .attr('fill', 'none');
                    mrna.filter(function(d) {
                        return d.mrna === undefined;
                    }).remove();
                };

                update(data);

                var altered
                    = utils.filter_altered(utils.nest_data(params.geneData));
                // TODO: nesting again, quick and dirty

                // The State object is our representation of the state of the oncoprint.
                // It is encapsulates a representation of the state of the oncoprint (is
                // whitespace on / off, what subset of the data is currently being
                // visualized, etc) and returns a set of functions for manipulating that
                // state.
                //
                // This object is what is eventually returned to the user for manipulating
                // the oncoprint
                var State = (function() {

                    // initialize state variables
                    var state = {
                        data: data,
                        whitespace: true,
                        rect_width: dims.rect_width,
                        hor_padding: dims.hor_padding,
                        attrs: params.genes.concat(clinical_attrs)
                    };

                    // takes a list of samples and returns an object that contains
                    // a function f,
                    // a map, sample2index
                    // and a constant, svg_width
                    //
                    // f : sample id --> x-position in oncoprint
                    var xscale = function(samples) {
                        var sample2index = {};  // quick indexing
                        for (var i = 0; i < samples.length; i+=1) {
                            sample2index[samples[i]] = i;
                        }

                        // params: i, sample index
                        // returns: the width of the svg to contain those samples.
                        var xpos = function(i) {
                            return i * (state.rect_width + (state.whitespace ? state.hor_padding : 0));
                        };

                        var svg_width_offset = 50;
                        return {
                            scale: function(d) { return xpos(sample2index[d]); },
                                sample2index: sample2index,
                                svg_width: svg_width_offset + xpos(samples.length)
                        };
                    };

                    // takes data and return a list of sample_ids in order
                    var pick_sample_id = function(internal_data) {
                        return internal_data.map(function(i) { return i.key; });
                    };

                    // composition of xscale and pick_sample
                    var data2xscale = function(data) {
                        return xscale(pick_sample_id(data));
                    };

                    // params:  duration
                    //              how long the transition should last.  If omitted, does
                    //              no animation
                    //          direction
                    //              'left' or 'right' . specify the direction to do the
                    //              animation.  Read as "from the <direction>." Defaults to
                    //              'left'
                    //
                    // puts all the samples in the correct horizontal position
                    var horizontal_translate = function(duration, direction) {
                        // re-sort
                        var x = data2xscale(state.data);

                        // resize the svg
                        var main_svg_transition = duration ? main_svg.transition(duration) : main_svg;
                        main_svg_transition.attr('width', x.svg_width);

                        d3.selectAll('.sample rect').transition()
                            .attr('width', state.rect_width);

                        var sample_transition = d3.selectAll('.sample').transition();

                        direction = direction || 'left';            // default
                        direction = direction.toLowerCase();        // defense
                        if (duration) {
                            if (direction === 'right') {
                                sample_transition.duration(function(d) {
                                    // reverse the index
                                    return duration + state.data.length - x.sample2index[d.key] * 4;
                                });
                            }
                            else if (direction === 'left') {
                                sample_transition.duration(function(d) {
                                    return duration + x.sample2index[d.key] * 4;
                                });
                            }
                            else {      // only support 'left' and 'right'
                                throw new Error("invalid direction specified to the transition");
                            }
                        }

                        // do the transition to all samples
                        sample_transition.attr('transform', function(d) { return translate(x.scale(d.key),0); });
                    };

                    // if bool === true, show unaltered cases, otherwise, don't
                    var show_unaltered_bool = true;     // saves state for toggleUnalteredCases
                    var showUnalteredCases = function(bool) {
                        show_unaltered_bool = bool;     // set the state

                        if (bool) {
                            state.data = data;
                            update(state.data);
                        } else {
                            var altered_data = data.filter(function(d) { return altered.has(d.key); });
                            state.data = altered_data;
                            update(state.data);
                        }
                        horizontal_translate(1);

                        return state.data;
                    };

                    var ANIMATION_DURATION = 750;

                    // params: [bool].  If bool is passed as a parameter,
                    // whitespace is set to the bool, otherwise, flip it from whatever it currently is
                    var toggleWhiteSpace =  function(bool) {
                        state.whitespace = bool === undefined ? !state.whitespace : bool;
                        horizontal_translate(ANIMATION_DURATION, state.whitespace ? 'right' : 'left');
                    };

                    // params:
                    // <by> is either 'genes', 'clinical', or 'alphabetical', 'custom'
                    // indicating how to sort the oncoprint.  Either by gene data first,
                    // clinical data first, or alphabetically.
                    //
                    // [cases] optional for 'genes', 'clinical', 'alphabetical', but REQUIRED for 'custom.
                    // Lists of cases to sort the data by
                    //
                    // returns the sorted data
                    //
                    // throws unsupported sort option if something other than the 3 options
                    // above is given.
                    var sortBy = function(by, cases) {
                        if (by === 'genes') {
                            state.attrs = params.genes.concat(clinical_attrs);
                            state.data = MemoSort(state.data, state.attrs);
                        }
                        else if (by === 'clinical') {
                            state.attrs = clinical_attrs.concat(params.genes);
                            state.data = MemoSort(state.data, state.attrs);
                        }
                        else if (by === 'alphabetical') {
                            state.data = state.data.sort(function(x,y) {
                                return x.key < y.key;
                            });
                        }
                        else if (by === 'custom') {
                            if (cases === undefined) {
                                throw new Error("cannot sort by custom cases set order with a list of cases");
                            }

                            // map of case id -> index in array
                            // just a small optimization so that you don't call indexOf
                            var case2index = cases.reduce(function(c2index, c, index) {
                                c2index[c] = index;
                                return c2index;
                            }, {});

                            state.data = state.data.sort(function(x,y) {
                                return case2index[x.key] - case2index[y.key];
                            });
                        }
                        else {
                            throw new Error("unsupported sort option: ") + JSON.stringify(by);
                        }
                        horizontal_translate(ANIMATION_DURATION);
                        return state.data;
                    };

                    // create a legend if user asked for it
                    if (params.legend) {
                        utils.legend(params.legend,
                                utils.gene_data_type2range(params.geneData), dims.label_width);
                    }

                    var memoSort = function(attributes, animation) {
                        state.data = MemoSort(state.data, attributes);
                        if (animation) { horizontal_translate(ANIMATION_DURATION); }
                        else { horizontal_translate(); }

                        return state.data;
                    };

                    var randomMemoSort = function() {
                        // randomly shuffle an array
                        var shuffle = function(array) {
                            var m = array.length, t, i;
                            while (m)  {
                                i = Math.floor(Math.random() * m--);
                                t = array[m], array[m] = array[i], array[i] = t;
                            }
                            return array;
                        };

                        state.attrs = shuffle(attributes);
                        state.data = MemoSort(state.data, state.attrs);
                        horizontal_translate(ANIMATION_DURATION);
                        return state.attrs;
                    };

                    var zoom = function(scalar, animation) {
                        // save state
                        old_rect_width = state.rect_width;

                        // change state
                        state.rect_width = scalar * dims.rect_width;
                        state.hor_padding = scalar * dims.hor_padding;

                        // which direction we are zooming in?
                        var direction = old_rect_width - state.rect_width > 0 ? 'left' : 'right';
                        if (animation) {
                            horizontal_translate(ANIMATION_DURATION, direction);
                        } else {
                            horizontal_translate();
                        }
                    };

                    // takes an oncoprint object and returns a serialized string
                    //
                    // *signature:* `undefined -> string`
                    var getPdfInput = function() {
                        var width = dims.width + dims.label_width;
                        var height = dims.height;
                        var svg = main_svg[0][0];
                        var x = data2xscale(state.data);

                        // helper function
                        // takes a DOM element and does the xml serializer thing
                        var serialize = function(el) {
                            return  (new XMLSerializer()).serializeToString(el);
                        };

                        // helper function
                        // maps a jquery array on the function fun (use the jquery callback signature),
                        // converts to javascript array and join on ""
                        var map_join = function($array, fun) {
                            return $array.map(fun).toArray().join("");
                        };

                        var out = map_join($(svg).children(),
                                function(index, sample_el) {
                                    var sample_id = d3.select(sample_el).data()[0].key;
                                    var transformed = $(sample_el).clone();
                                    transformed = transformed.attr('transform', translate(dims.label_width + x.scale(sample_id), 0));

                                    return serialize(transformed[0]);
                                });

                        var labels = $('#oncoprint svg#label').children().clone();
                        labels = map_join(labels, function(index, label) {
                            return serialize(label);
                        });

                        out += labels;

                        return "<svg height=\"" + height + "\" width=\"" + width + "\">" + out + "</svg>";
                    };

                    return {
                        remove_oncoprint: remove_oncoprint,
                        memoSort: memoSort,
                        randomMemoSort: randomMemoSort,
                        getData: function() { return state.data; },
                        toggleWhiteSpace: toggleWhiteSpace,
                        zoom: zoom,
                        showUnalteredCases: showUnalteredCases,
                        toggleUnalteredCases: function() {
                            show_unaltered_bool = !show_unaltered_bool;
                            showUnalteredCases(show_unaltered_bool);
                        },
                        sortBy: sortBy,
                        getPdfInput: getPdfInput
                    };
                })();

                utils.make_mouseover(d3.selectAll('#' + div.id + ' .sample *'));

                return State;
            };

        });
