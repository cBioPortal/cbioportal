//
//
// Gideon Dresdner <dresdnerg@cbio.mskcc.org> June 2013
//
//
//
;
// util class for handling some of data processing
// and other odds and ends that are needed in the Oncoprint
var OncoprintUtils = (function() {
    var is_discrete = function(val) {
        return isNaN(parseInt(val));
    };

    var is_gene = function(d) {
        return d.gene !== undefined;
    };

    var is_clinical = function(d) {
        return d.attr_id !== undefined;
    };

    // params: data, list of data as specified elsewhere
    // TODO: where exactly is this specified??
    //
    // returns: data nested by the key, "sample"
    var nest_data = function(data) {
        return d3.nest()
            .key(function(d) { return d.sample; })
            .entries(data);
    };

    // returns the gene name or the attr_id, whatever the piece of data has
    // throws Error
    var get_attr = function(d) {
        var to_return =  d.gene || d.attr_id;

        if (to_return === undefined) {
            throw new Error("datum has neither a gene nor an attr_id: "
                + JSON.stringify(d));
        }
        return to_return;
    };

    // params: list of data, list of attributes to filter by
    // returns: a filtered list, a list of
    var filter_by_attributes = function(data, attributes) {
        var trues = _.range(0, attributes.length);
        trues = _.map(trues, function() { return true; });

        var attribute_set = _.object(attributes, trues);

        return _.filter(data, function(datum) {
            return attribute_set[get_attr(datum)];
        });
    };

    // params: element of a nested list, list of attributes
    // finds all the missing attributes in the values and creates them with
    // attr_val = "NA"
    var normalize_nested_values = function(key_values, attributes) {
        var attrs = key_values.values.map(function(value) { return get_attr(value); });

        // set minus
        var attribues_minus_attrs = attributes.filter(function(attr) {
            return attrs.indexOf(attr) === -1;
        });

        var new_values = attribues_minus_attrs.map(function(str) {
            return {
                sample: key_values.key,
                attr_id: str,
                attr_val: "NA"
            };
        });

        return key_values.values.concat(new_values);
    };

    // params: list of gene data (not nested),
    // list of clinical data (not nested),
    // list of clinical attributes (string)
    //
    // returns list of raw clinical data
    var normalize_clinical_attributes = function(nested_data, attributes) {
        var no_attributes = attributes.length;

        var normalized = nested_data.map(function(key_values) {
            if (key_values.values.length !== no_attributes) {
                key_values.values = normalize_nested_values(key_values, attributes);
            }
            return key_values;
        });

        return normalized;
    };

    // composes the functions nest_data, filter_by_attributes, and normalize_clinical_attributes
    var process_data = function(data, attributes) {
        var processed = nest_data(filter_by_attributes(data, attributes));
        processed = normalize_clinical_attributes(processed, attributes);

        return processed;
    };

    // params: [list of raw clinical data]
    //
    // returns: map of an attribute id to its respective range
    // where a range is a 2-ple if the correspondings attribute values are numerical
    // and a list of values otherwise
    var attr2range = function(clinicalData) {
        var attr2range_builder = function(prev, curr) {
            prev[curr.attr_id] = prev[curr.attr_id] || [];      // initialize

            var a2r =  prev[curr.attr_id];
            var val = curr.attr_val;

            // an attribute whose value is "NA" for all samples has no range
            if (val === "NA") {
                return prev;
            }
            if (is_discrete(val)) {
                if (a2r.indexOf(val) === -1) { a2r.push(val); }       // keep a set of unique elements
            }
            else {
                // just keep the min and max -- an interval of values
                val = parseInt(val);
                var min = a2r[0],
                    max = a2r[1];

                if (max === undefined || val > max) {
                    a2r[1] = val;
                }
                if (min === undefined || val < min) {
                    a2r[0] = val;
                }
            }

            prev[curr.attr_id] = a2r;
            return prev;
        };

        return _.reduce(clinicalData, attr2range_builder, {});
    };

    // params: [list of raw gene data]
    //
    // returns a map of gene_data type (e.g. cna, mutations, mrna, rppa)
    // to all the values these data types take on respectively in the data
    //
    // if there is no data, then the range is undefined
    var gene_data_type2range = function(raw_gene_data) {
        var extract_unique = function(raw_data, datatype) {
            return _.chain(raw_gene_data)
                .map(function(d) {
                    return d[datatype];
                })
                .unique()
                .value();
        };

        var cnas = extract_unique(raw_gene_data, 'cna');
        var mutations = extract_unique(raw_gene_data, 'mutation');
        var mrnas = extract_unique(raw_gene_data, 'mrna');
        var rppas = extract_unique(raw_gene_data, 'rppa');

        var there_is_data = function(list) {
            return !(list.length === 1) || !(list[0] === undefined);
        };

        var to_return = {};

        if (there_is_data(cnas)) {
            to_return.cna = cnas;
        }

        if (there_is_data(mutations)) {
            to_return.mutation = mutations;
        }

        if (there_is_data(mrnas)) {
            to_return.mrna = mrnas;
        }

        if (there_is_data(rppas)) {
            to_return.rppa = rppas;
        }

        return to_return;
    };

    // these colors could be passed somewhow as a parameter
    var colors = {
        continuous: '#A62459',
        white: '#FFFAF0',
        discrete: '#404040',
        red: '#FF0000',
        grey: '#D3D3D3'
    };

    var googlecharts_colors =
        ["#3366cc","#dc3912","#ff9900","#109618",
        "#990099","#0099c6","#dd4477","#66aa00",
        "#b82e2e","#316395","#994499","#22aa99",
        "#aaaa11","#6633cc","#e67300","#8b0707",
        "#651067","#329262","#5574a6","#3b3eac",
        "#b77322","#16d620","#b91383","#f4359e",
        "#9c5935","#a9c413","#2a778d","#668d1c",
        "#bea413","#0c5922","#743411"];

    // takes a map attr2range, and transforms the ranges into d3 scales
    // it does something very simplistic :
    //      string -> discrete , number -> continuous
    var attr2range_to_d3scale = function(attr2range) {
        for (var a2r in attr2range) {
            var range = attr2range[a2r];
            var discrete = is_discrete(range[0]);
            var range_vals;

            if (!discrete) {
                range_vals = [colors.white, colors.continuous];
            } else if (range.length > 2) {
                range_vals = googlecharts_colors.slice(0,range.length);
            } else {
                range_vals = [colors.discrete, colors.white];
            }

            var new_scale = discrete ? d3.scale.ordinal() : d3.scale.linear();
            new_scale.domain(range);
            new_scale.range(range_vals);

            attr2range[a2r] = new_scale;
        }

        return attr2range;
    };

    // composition of attr2range_to_d3scale with attr2range
    var attr_to_d3scale = function(clinicalData) {
        return attr2range_to_d3scale(attr2range(clinicalData));
    };

    // params: list of clinical attribute object literals,
    // list of raw clinical data
    //
    // the clinical attribute literals must have fields
    // <datatype> and <attr_id>
    //
    // returns a map of attr_id to d3 scale
    var make_attribute2scale = function(attrs, raw_clinical_data) {
        var attrId2range = attr2range(raw_clinical_data);

        var slice_googlecolors = function(attr_id) {
            return googlecharts_colors.slice(0, attrId2range[attr_id].length);
        };

        return _.chain(attrs)
            .map(function(attr) {
                // attr -> [attr_id, d3 scale]
                var scale;

                // manually set the colors for certain attributes
                if (attr.attr_id.toUpperCase() === "SEX"
                    || attr.attr_id.toUpperCase() === "GENDER") {
                    scale = d3.scale.ordinal()
                        .domain(["MALE", "male", "FEMALE", "female"])
                        .range(["#3790d6", "#3790d6", "pink", "pink"]);

                    return [attr.attr_id, scale];
                }

                // calculate the proper colors for all other attributes
                if (attr.datatype.toUpperCase() === "BOOLEAN") {
                    scale = d3.scale.ordinal()
                        .range([colors.discrete, colors.white]);
                }

                else if (attr.datatype.toUpperCase() === "NUMBER") {
                    scale = d3.scale.linear()
                        .range([colors.white, colors.continuous]);
                }

                else if (attr.datatype.toUpperCase() === "STRING") {
                    scale = d3.scale.ordinal()
                        .range(slice_googlecolors(attr.attr_id));
                }

                else {
                    // defaults to discrete scale
                    scale = d3.scale.ordinal()
                        .range( slice_googlecolors(attr.attr_id));
                }
                scale.domain(attrId2range[attr.attr_id]);

                return [attr.attr_id, scale];
            })
            .object()
            .value();
    };

    // params: sample
    // returns: boolean, is the sample altered in a particular gene?
    var altered_gene = function(sample_gene) {
        return !(sample_gene.cna === undefined
                && sample_gene.mutation === undefined
                && sample_gene.mrna === undefined
                && sample_gene.rppa === undefined);
    };

    // params: nested gene data
    //
    // returns: d3.set of sample_ids that *have* genetic alterations
    var filter_altered = function(nested_data) {

        if (nested_data[0].key === undefined) {
            throw new Error("the first element of nested_data does not have a 'key' attribute, therefore I do not think this is nested data.");
        }

        var altered_sample_set = d3.set();

        nested_data.forEach(function(sample) {
            sample.values.forEach(function(sample_gene) {
                if (altered_gene(sample_gene)) {
                    altered_sample_set.add(sample.key);
                }
            });
        });

        return altered_sample_set;
    };

    // params: list of raw gene data (unnested)
    // return: map of gene name to percent altered
    var percent_altered = function(raw_gene_data) {
        var data = d3.nest()
            .key(function(d) { return get_attr(d); })
            .entries(raw_gene_data);

        var attr2percent = {};

        data.forEach(function(gene) {
            var total = gene.values.length;
            var altered = _.chain(gene.values)
                .map(function(sample_gene) {
                    return altered_gene(sample_gene) ? 1 : 0;
                })
                .reduce(function(sum, zero_or_one) {
                    return sum + zero_or_one;
                }, 0)
                .value();

            var percent = (altered / total) * 100;
            attr2percent[gene.key] = Math.round(percent);
        });

        return attr2percent;
    };

    // params: array of strings (names of attributes)
    // returns: length (number)
    //
    // calculates the length of the longest label for a particular list of
    // attributes (strings) by temporarily appending them as text, calculating
    // their width, and taking the maximum.
    var label_width = function(strings) {
        var tmp = d3.select('body').append('svg');

        strings.forEach(function(attr) {
            tmp.append('text')
                .append('tspan')
                .text(attr);
        });

        var max = d3.max(
                tmp.selectAll('text')[0]
                .map(function(text, i) {return text.getBBox().width; })
                );

        tmp.remove();
        // bye bye tmp

        return 42 + max; // http://goo.gl/iPzfU
    };

    //TODO:
    var createId2ClinicalAttr = function(clinical_attrs) {
        return id2ClinicalAttr = clinical_attrs.reduce(
            function(name2attr, attr) {
                name2attr[attr.attr_id] = attr;
                return name2attr;
            }, {});
    };

    //TODO:
    var map_display_name = function(map, d) {
        var clinical_attr = map[d];
        if (clinical_attr === undefined) {      // we have a gene
            return d;
        }

        return clinical_attr.display_name;
    };

    var cna_fills = {
        undefined: colors.grey,
        AMPLIFIED: colors.red,
        GAINED: '#FFB6C1',
        DIPLOID: '#D3D3D3',
        HEMIZYGOUSLYDELETED: '#8FD8D8',
        HOMODELETED: '#0000FF'
    };

    // puts a legend in the div according to range for each datatype.  If the
    // range for a datatype is undefined, then it doesn't get represented in
    // the legend
    //
    // *signature:* `DOM el, { string : [string] }, number -> DOM el`
    // * accepts an optional DOM element containing an item template
    var legend = function(el, datatype2range, left_adjust, item_template) {

        // *signature:* object -> string (html)
        // options can be:
        // * bg_color
        // * display_mutation
        // * display_down_rppa
        // * display_up_rppa
        // * display_down_mrna
        // * display_up_mrna
        var item_templater = function(options) {
            var item_template = item_template || document.getElementById('glyph_template');
            var t = _.template(item_template.innerHTML);

            // set defaults
            options.bg_color = options.bg_color || colors.grey;
            options.display_mutation = options.display_mutation || "none";
            options.display_down_rppa = options.display_down_rppa || "none";
            options.display_up_rppa = options.display_up_rppa || "none";
            options.display_down_mrna = options.display_down_mrna || "none";
            options.display_up_mrna = options.display_up_mrna || "none";

            return t(options);
        };

        // text values that explain the glyph
        var captions = {
            cna: {
                     AMPLIFIED: "Amplification",
                     GAINED: "Gain",
                     DIPLOID: "Diploid",
                     HEMIZYGOUSLYDELETED: "Heterozygous Deletion",
                     HOMODELETED: "Homozygous Deletion"
                 },
            mrna: {
                      UPREGULATED: "mRNA Upregulation",
                      DOWNREGULATED: "mRNA Downregulation"
                  },
            rppa: {
                      UPREGULATED: "RPPA Upregulation",
                      DOWNREGULATED: "RPPA Downregulation"
                  },
            mutation: "Mutation"
        };

        var val2template = {
            mrna: {
                      "UPREGULATED": item_templater({display_up_mrna: "inherit", text: captions.mrna.UPREGULATED}),
                      "DOWNREGULATED": item_templater({display_down_mrna: "inherit", text: captions.mrna.DOWNREGULATED})
                  },
            rppa: {
                      "UPREGULATED": item_templater({display_up_rppa: "inherit", text: captions.rppa.UPREGULATED}),
                      "DOWNREGULATED": item_templater({display_down_rppa: "inherit", text: captions.rppa.DOWNREGULATED})
                  }
        };

        // build up an array of templates from the values in the dataset
        // N.B. order matters here --- so cna is to the left, then comes
        // mutation, etc.
        var templates = [];
        if (datatype2range.cna !== undefined) {
            templates = templates.concat(
                    _.map(datatype2range.cna, function(val) {
                        if (val !== undefined && val !== "DIPLOID") {
                            return item_templater({
                                bg_color: cna_fills[val],
                                text: captions.cna[val]
                            });
                        }
                    }).filter(function(x) { return x !== undefined; })
                );
        }

        if (datatype2range.mutation !== undefined) {
            templates = templates.concat(
                    item_templater({ display_mutation: "inherit", text: captions.mutation})
                );
        }

        if (datatype2range.mrna !== undefined) {
            templates = templates.concat(
                    _.map(datatype2range.mrna, function(val) {
                        return val2template.mrna[val];
                    }).filter(function(x) { return x !== undefined; })
                );
        }

        if (datatype2range.rppa !== undefined) {
            templates = templates.concat(
                    _.map(datatype2range.rppa, function(val) {
                        return val2template.rppa[val];
                    }).filter(function(x) { return x !== undefined; })
                );
        }

        var row = _.chain(templates)
            .map(function(t) {
                return "<td style='padding-right:10px;'>" + t + "</td>";
            })
            .join("")
            .value();

        el.style.paddingLeft = left_adjust + 4 + "px";;

        el.innerHTML = "<table><tr>" + row  + "</tr></table>"
            + "<p style='margin-top: 5px; margin-bottom: 5px;'>Copy number alterations are putative.</p>";

        return el;
    };

    return {
        is_discrete: is_discrete,
        nest_data: nest_data,
        get_attr: get_attr,
        filter_by_attributes: filter_by_attributes,
        process_data: process_data,
        attr2range: attr2range,
        attr2range_to_d3scale: attr2range_to_d3scale,
        attr_to_d3scale: attr_to_d3scale,
        filter_altered: filter_altered,
        percent_altered: percent_altered,
        label_width: label_width,
        createId2ClinicalAttr: createId2ClinicalAttr,
        map_display_name: map_display_name,
        normalize_clinical_attributes: normalize_clinical_attributes,
        normalize_nested_values: normalize_nested_values,
        legend: legend,
        make_attribute2scale: make_attribute2scale,
        gene_data_type2range: gene_data_type2range,
        is_gene: is_gene,
        is_clinical: is_clinical,
        cna_fills: cna_fills,
        colors: colors
    };
}());

// namespace for functions that relate to the User Interface of the Oncoprint.
var OncoprintUI = (function() {

    // params: select_el (a DOM <select> element), clinical_attributes (list of
    // clinical attribute object literals);
    var populate_clinical_attr_select = function(select_el, clinical_attributes) {

        clinical_attributes = [{display_name: 'none', attr_id: undefined}].concat(clinical_attributes);

        var select_el = d3.select(select_el);

        select_el.selectAll('option')
            .data(clinical_attributes)
            .enter()
            .append('option')
            .text(function(d) { return d.display_name; });
    };

    // formating for mouseovers
    var format = (function() {

        var standard_cna_values = {
            "AMPLIFIED": "AMP",
            "DELETED": "DEL",
            "GAINED": "GAIN",
            "HOMODELETED": "HOMDEL"
        };

        return {
            mutation: function(d) {
                return d.mutation ?
                    "Mutation: <b>" + d.mutation + "</b><br/>"
                    : "";
            },

            cna: function(d) {
                var standardized_cna = (standard_cna_values[d.cna] || d.cna);

                return d.cna ?
                    "Copy Number Alteration: <b>" + standardized_cna + "</b><br/>"
                    : "";
            },

            mrna: function(d) {
                return d.mrna ?
                    "MRNA: <b>" + d.mrna + "</b><br/>"
                    : "";
            },

            rppa: function(d) {
                return d.rppa ?
                    "RPPA: <b>" + d.rppa + "</b><br/>"
                    : "";
            },

            clinical: function(d) {
                return "value: <b>" + d.attr_val + "</b><br/>";
            }
        };
    }());

    var patientViewUrl = function(sample_id) {
        // helper function
        var href = "case.do?case_id=" + sample_id
            + "&cancer_study_id=" + window.cancer_study_id_selected;        // N.B.

        return "<a href='" + href + "'>" + sample_id + "</a>";
    };

    // params: els, list of d3 selected elements with either gene data or
    // clinical bound to them
    var make_mouseover = function(els) {
        els.each(function(d) {
            $(this).qtip({
                content: {text: 'oncoprint qtip failed'},
                position: {my:'left bottom', at:'top right'},
                style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow' },
                hide: { fixed: true, delay: 100 },
                events: {
                    render: function(event, api) {
                        var content;
                        if (d.attr_id) {
                            content = '<font size="2">'
                                + format.clinical(d)
                                + patientViewUrl(d.sample) + '</font>';
                        } else {
                            content = '<font size="2">'
                                + format.mutation(d)
                                + format.cna(d)
                                + format.mrna(d)
                                + format.rppa(d)
                                + patientViewUrl(d.sample) + '</font>';

                        }
                        api.set('content.text', content);
                    }
                }
            });
        });
    };

    return {
        populate_clinical_attr_select: populate_clinical_attr_select,
        make_mouseover: make_mouseover
    };
}());

// Creates an oncoprint on the div.
// The parameters is an object that contains:
// clinicalData, clinical_attrs (list), genes (list), geneData,
// and legend (undefined or div element)
var Oncoprint = function(div, params) {
    params.clinicalData = params.clinicalData || [];        // initialize
    params.clinical_attrs = params.clinical_attrs || [];

    // make strings of numbers into numbers
    var clinicalData = params.clinicalData.map(function(i) {
        if (!OncoprintUtils.is_discrete(i.attr_val)) {
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

    data = OncoprintUtils.process_data(data, attributes);

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

    var id2ClinicalAttr = OncoprintUtils.createId2ClinicalAttr(params.clinical_attrs);

    var dims = (function() {
        var rect_height = 23;
        var mut_height = rect_height / 3;
        var vert_padding = 6;
        var label_width = OncoprintUtils.label_width(attributes.map(
            function(attr) {        // curry
                return OncoprintUtils.map_display_name(id2ClinicalAttr, attr);
            }));

        var clinical_height = (2/3) * rect_height;
        var clinical_offset = (1/6) * rect_height;

        return {
            width: data.length * (5.5 + 3),
            height: (23 + 5) * attributes.length,
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
        .append('tr');

    var remove_oncoprint = function() {
        d3.select("#" + div.id + ' table').remove();
    };

    var label_svg = table
        .append('td')
        .append('svg')
        .attr('height', dims.height)
        .attr('width', '' + dims.label_width)
        .attr('id', 'label');

    var label = label_svg.selectAll('text')
        .data(attributes)
        .enter()
        .append('text')
        .attr('x', 0)
        .attr('y', function(d) {
            return (dims.vert_space / 1.80) + vertical_pos(d); });

    var gene2percent = OncoprintUtils.percent_altered(params.geneData);

    label.append('tspan')       // name
        .attr('text-anchor', 'start')
        .attr('font-weight', 'bold')
        .text(function(d) { return OncoprintUtils.map_display_name(id2ClinicalAttr, d); });
    label.append('tspan')       // percent_altered
        .text(function(d) {
            return gene2percent[d] ? gene2percent[d].toString() + "%" : ""; })
        .attr('x', '' + dims.label_width)
        .attr('text-anchor', 'end')
        // remove the tspan that would have contained the percent altered
        // because it messes with the label placement in the pdf download
        .filter(function(d) { return gene2percent[d] === undefined; }).remove();

    var main_svg = table
        .append('td')
            .append('div')      // control overflow to the right
            .style('width', $('#td-content').width() - 70 - dims.label_width + 'px') // buffer of, say, 70
            .style('display', 'inline-block')
            .style('overflow-x', 'auto')
            .style('overflow-y', 'hidden')
        .append("svg")
        .attr('width', dims.width)
        .attr('height', dims.height);

    var colors = OncoprintUtils.colors;     // alias

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

        var attr2range = OncoprintUtils.make_attribute2scale(params.clinical_attrs, clinicalData);

        // N.B. fill doubles as cna
        var fill = enter.append('rect')
            .attr('fill', function(d) {
                if (OncoprintUtils.is_gene(d)) {
                    return OncoprintUtils.cna_fills[d.cna];
                }
                else if (OncoprintUtils.is_clinical(d)) {
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
                ? vertical_pos(OncoprintUtils.get_attr(d))
                : vertical_pos(OncoprintUtils.get_attr(d)) + dims.clinical_offset;
           //     return vertical_pos(OncoprintUtils.get_attr(d));
            });

        var mut = enter.append('rect')
            .attr('fill', 'green')
            .attr('height', dims.mut_height)
            .attr('width', dims.rect_width)
            .attr('y', function(d) {
                return dims.mut_height + vertical_pos(OncoprintUtils.get_attr(d)); });
        mut.filter(function(d) {
            return d.mutation === undefined;
        }).remove();

        var sym = d3.svg.symbol().size(dims.rect_width * 2);
        var rppa = enter.append('path')
                .attr('d', sym.type(function(d) {
                    return d.rppa === "UPREGULATED" ? "triangle-up" : "triangle-down" }))
                .attr('transform', function(d) {
                    // put the triangle in the right spot: at the top if
                    // UNREGULATED, at the bottom otherwise
                    var dy = dims.rect_height;
                    dy = d.rppa === "UPREGULATED" ? dy / 1.1 : dy * .1;
                    return translate(dims.rect_width / 2, dy + vertical_pos(OncoprintUtils.get_attr(d))); })
        rppa.filter(function(d) {
            return d.rppa === undefined;
        }).remove();

        var mrna = enter.append('rect')
            .attr('y', function(d) { return vertical_pos(OncoprintUtils.get_attr(d)); })
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
        = OncoprintUtils.filter_altered(OncoprintUtils.nest_data(params.geneData));
    // TODO: nesting again, quick and dirty

    // The State object is our representation of the state of the oncoprint.
    // Every change made to State, is reflected in a change in the oncoprint, and visa-versa.
    // This object is returned to the user for the UI
    var State = (function() {
        var internal_data = data;
        var whitespace = true;                      // show white space?
        var internal_rect_width = dims.rect_width;  // initialize
        var internal_scalar = 1;

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
                return internal_scalar * i * (internal_rect_width + (whitespace ? dims.hor_padding : 0));
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

        // params: <duration> -- how long the transition should last.  If omitted, does no animation

        // puts all the samples in the correct horizontal position
        var horizontal_translate = function(duration) {
            // re-sort
            var x = data2xscale(internal_data);

            // resize the svg
            var main_svg_transition = duration ? main_svg.transition(duration) : main_svg;
            main_svg_transition.attr('width', x.svg_width);

            var sample_transition = duration ?
                d3.selectAll('.sample').transition().duration(function(d) { return duration + x.sample2index[d.key] * 4; })
                : d3.selectAll('.sample');

            // do the transition to all samples
            sample_transition.attr('transform', function(d) { return translate(x.scale(d.key),0); });
        };

        // if bool === true, show unaltered cases, otherwise, don't
        var show_unaltered_bool = true;     // saves state for toggleUnalteredCases
        var showUnalteredCases = function(bool) {
            show_unaltered_bool = bool;     // set the state

            if (bool) {
//                internal_data = MemoSort(data, attributes);
                internal_data = data;
                update(internal_data);
            } else {
                var altered_data = data.filter(function(d) { return altered.has(d.key); });
//                internal_data = MemoSort(altered_data, attributes);
                internal_data = altered_data;
                update(internal_data);
            }
            horizontal_translate(1);

            return internal_data;
        };

        var ANIMATION_DURATION = 750;

        // params: [bool].  If bool is passed as a parameter,
        // whitespace is set to the bool, otherwise, flip it from whatever it currently is
        var toggleWhiteSpace =  function(bool) {
            whitespace = bool === undefined ? !whitespace : bool;
            horizontal_translate(ANIMATION_DURATION);
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
            var attrs;
            if (by === 'genes') {
                attrs = params.genes.concat(clinical_attrs);
                internal_data = MemoSort(internal_data, attrs);
            }
            else if (by === 'clinical') {
                attrs = clinical_attrs.concat(params.genes);
                internal_data = MemoSort(internal_data, attrs);
            }
            else if (by === 'alphabetical') {
                internal_data = internal_data.sort(function(x,y) {
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

                internal_data = internal_data.sort(function(x,y) {
                    return case2index[x.key] - case2index[y.key];
                });
            }
            else {
                throw new Error("unsupported sort option: ") + JSON.stringify(by);
            }
            horizontal_translate(ANIMATION_DURATION);
            return internal_data;
        };

        if (params.legend) {
            OncoprintUtils.legend(params.legend, OncoprintUtils.gene_data_type2range(params.geneData), dims.label_width);
        }

        return {
            remove_oncoprint: remove_oncoprint,
            memoSort: function(attributes, animation) {
                internal_data = MemoSort(internal_data, attributes);
                if (animation) { horizontal_translate(ANIMATION_DURATION); }
                else { horizontal_translate(); }

                return internal_data;
            },

            randomMemoSort: function() {
                // randomly shuffle an array
                var shuffle = function(array) {
                    var m = array.length, t, i;
                    while (m)  {
                        i = Math.floor(Math.random() * m--);
                        t = array[m], array[m] = array[i], array[i] = t;
                    }
                    return array;
                };

                var attrs = shuffle(attributes);
                internal_data = MemoSort(internal_data, attrs);
                horizontal_translate(ANIMATION_DURATION);
                return attrs;
            },

            getData: function() { return internal_data; },

            toggleWhiteSpace: toggleWhiteSpace,

            zoom: function(scalar, animation) {
                internal_scalar = scalar;
                //internal_rect_width = scalar * dims.rect_width;

                if (animation) {
                    horizontal_translate(ANIMATION_DURATION);
                } else {
                    horizontal_translate();
                }

                d3.selectAll('.sample rect')
                    .transition()
                    .duration(ANIMATION_DURATION)
                    .attr('width', internal_rect_width);
//                if (scalar >= .5) {
//                    toggleWhiteSpace(true);
//                } else {
//                    toggleWhiteSpace(false);
//                }
            },

            showUnalteredCases: showUnalteredCases,

            toggleUnalteredCases: function() {
                show_unaltered_bool = !show_unaltered_bool;
                showUnalteredCases(show_unaltered_bool);
            },

            sortBy: sortBy,

            // takes an oncoprint object and returns a seralized string
            //
            // returns string
            getPdfInput: function() {
                var width = main_svg.attr('width') + dims.label_width;
                var height = main_svg.attr('height');
                var svg = main_svg[0][0];
                var x = data2xscale(internal_data);

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
            }
        };
    })();

    // sort by gene data initially
    State.memoSort(params.genes.concat(clinical_attrs));

    OncoprintUI.make_mouseover(d3.selectAll('#' + div.id + ' .sample *'));

    return State;
};

///{{{
var _Oncoprint = function(wrapper, params) {
    var that = {};

    var RECT_HEIGHT = 23;
    var TRACK_PADDING = 4;
    var LABEL_PADDING = 40;
    var LITTLE_RECT_HEIGHT = RECT_HEIGHT / 3;
    var MRNA_STROKE_WIDTH = 2;
    var UPREGULATED = "UPREGULATED";
    var DOWNREGULATED = "DOWNREGULATED";
    var MRNA_UP_COLOR = "#FF9999";
    var MRNA_DOWN_COLOR = "#6699CC";
    var mrna_fills = {
        UPREGULATED: "#FF9999",
        DOWNREGULATED: "#6699CC"
    };
    var MUT_COLOR = "#008000";
    var RPPA_LIGHT = "#D3D3D3";
    var RPPA_DARK = "#000000";
    var cna_fills = {
        none: '#D3D3D3',
        AMPLIFIED: '#FF0000',
        GAINED: '#FFB6C1',
        DIPLOID: '#D3D3D3',
        HEMIZYGOUSLYDELETED: '#8FD8D8',
        HOMODELETED: '#0000FF'
    };

    var geneData = params.geneData;
    var query = QueryGeneData(geneData);
    var genes_list = query.getGeneList();
    var gene_data = geneData.gene_data;
    var no_genes = gene_data.length;
    var samples_all = query.getSampleList();

    var translate = function(x,y) {
        return "translate(" + x + "," + y + ")";
    };

    var cleanHugo = function(hugo) {
        // can't have '/' in DOM id
        return hugo.replace("/", "_");
    };

    // global state of the oncoprint
    var state = {
        padding: true,
        width_scalar: 1,
        show_unaltered: true,
        memo_sort: true
    };

    var getVisualizedSamples = function() {
        // get state of samples
        var samples_copy = samples_all.map(function(i) { return i;});

        // todo: note that you must sort first!
        // MemoSort behaves differently when it has different lists, that is,
        // it does not deterministically deal with samples that are equal
        if (state.memo_sort) {
            samples_copy = MemoSort(geneData, samples_copy, genes_list).sort();
        }

        if (!state.show_unaltered) {
            samples_copy = samples_copy.filter(query.isSampleAltered);
        }

        return samples_copy;
    };

    var getRectWidth = function() {
        var unscaled = 5.5;
        return state.width_scalar * unscaled;
    };

    var getTrianglePath = function(rect_width, up) {

        var getTriangleBase = function() {
            return rect_width / 2;
        };

        var base = getTriangleBase();

        if (up) {
            return "M 0 " + LITTLE_RECT_HEIGHT + " l "  + base + " -" + LITTLE_RECT_HEIGHT
                + " l " + base + " " + LITTLE_RECT_HEIGHT + " l 0 0";
//                return "M 0 7 l 2.75 -7 l 2.75 7 l 0 0";
        }
//        return "M 0 16 l " + base + " 7 l " + base + " -7 l 0 0";
        return "M 0 15 l " + base + " " + LITTLE_RECT_HEIGHT + " l "
            + base + " -" + LITTLE_RECT_HEIGHT + " l 0 0";
//            return "M 0 16 l 2.75 7 l 2.75 -7 l 0 0";
    };

    var getRectPadding = function() {
        var unscaled = 3;
        return state.padding ? (state.width_scalar * unscaled) : 0;
    };

    var getXScale = function(no_samples) {
        return (getRectWidth() + getRectPadding()) * no_samples;
    };

    var getHeight = function() {
        return (RECT_HEIGHT + TRACK_PADDING) * no_genes;
//        return RECT_HEIGHT * no_genes;
        // for some reason, the CSS in the portal creates space between tracks
    };

    // scales
    var x = d3.scale.ordinal().rangeBands([MRNA_STROKE_WIDTH, getXScale(samples_all.length)], 0);

    var y = d3.scale.ordinal().rangeBands([0, getHeight()], 0)
        .domain(genes_list);

    that.getData = function() {
        return params.data;
    };

    var redraw = function(samples_visualized, track, hugo) {
        var join_with_hugo = samples_visualized.map(function(i) {
            return {
                sample: i,
                hugo: hugo
            };
        });

        var sample = track.selectAll('.sample')
            .data(join_with_hugo, function(d) { return d.sample;});

//        // update
//        sample.transition()
//            .duration(500)
//            .attr('transform', function(d) {
//                return translate(x(d.sample), y(hugo));
//            });

        // enter
        var sample_enter = sample.enter()
            .append('g')
                .attr('class', 'sample')
                .attr('transform', function(d) {
                    return translate(x(d.sample), y(hugo));
                });

        var rect_width = getRectWidth();

        var cna = sample_enter.append('rect')
            .attr('class', 'cna')
            .attr('fill', function(d) {
                var cna = query.data(d.sample, hugo, 'cna');
                cna = cna === null ? 'none' : cna;
                return cna_fills[cna];
            })
            .attr('width', rect_width)
            .attr('height', RECT_HEIGHT);

        var mrna = sample_enter.append('rect')
            .attr('class', 'mrna')
            .attr('fill', 'none')
            .attr('stroke-width', MRNA_STROKE_WIDTH)
            .attr('stroke-opacity', 1)
            .attr('width', rect_width)
            .attr('height', RECT_HEIGHT)
            .attr('stroke', function(d) {
                var mrna = query.data(d.sample, hugo, 'mrna');
                if (mrna === UPREGULATED) {
                    return MRNA_UP_COLOR;
                } return MRNA_DOWN_COLOR;
            });

        // remove all the null mrna squares
        mrna.filter(function(d) {
            var mrna = query.data(d.sample, hugo, 'mrna');
            return mrna === null;
        }).remove();

        var mut = sample_enter.append('rect')
            .attr('class', 'mut')
            .attr('fill', MUT_COLOR)
            .attr('y', LITTLE_RECT_HEIGHT)
            .attr('width', rect_width)
//            .attr('width', mutation_width)
            .attr('height', LITTLE_RECT_HEIGHT);

        // remove all the null mutation squares
        mut.filter(function(d) {
            var mutation = query.data(d.sample, hugo, 'mutation');
            return mutation === null;
        }).remove();

        var up_triangle = getTrianglePath(rect_width, true);
        var down_triangle = getTrianglePath(rect_width, false);

        var rppa = sample_enter.append('path')
            .attr('class', 'rppa')
            .attr('fill', function(d) {
                if (query.data(d.sample, hugo, 'cna') === null) {
                    return RPPA_DARK;
                }
                return RPPA_LIGHT;
            })
            .attr('d', function(d) {
                var rppa = query.data(d.sample, hugo, 'rppa');

                if (rppa === UPREGULATED) {
                    return up_triangle;
                }
                if (rppa === DOWNREGULATED) {
                    return down_triangle;
                }
                if (rppa === null) {
                    return 'M 0 0';
                }
            });

        rppa.filter(function(d) {
            var rppa = query.data(d.sample, hugo, 'rppa');

            return rppa === null;
        }).remove();

        // exit
        var sample_exit = sample.exit().remove();
    };

    var svg;        // global scope
    that.getSvg = function() { return svg; };

    // oncoprint legend
    //
    var legend = function(data_types) {

        var captions = {
            cna: {
                AMPLIFIED: "Amplification",
                GAINED: "Gain",
                DIPLOID: "Diploid",
                HEMIZYGOUSLYDELETED: "Heterozygous Deletion",
                HOMODELETED: "Homozygous Deletion"
            },
            mrna: {
                UPREGULATED: "mRNA Upregulation",
                DOWNREGULATED: "mRNA Downregulation"
            },
            rppa: {
                UPREGULATED: "RPPA Upregulation",
                DOWNREGULATED: "RPPA Downregulation"
            },
            mutation: "Mutation"
        };


        var text_padding = 10;

        var legend_el = d3.select('#oncoprint_legend');
        legend_el.style('margin-left', getRectWidth() + label_width + 2 + "px");

        var getSvg = function(label_str) {
            var svg = legend_el.append('svg');

            var el_width = function(label_str) {
                var l = label_str.split("");
                var scalar = 7;
                var affine = 25;
                return affine + scalar * l.length;
            };

            svg.attr('height', RECT_HEIGHT);
            svg.attr('width', el_width(label_str));
            return svg;
        };

        var range = query.getDataRange();

        var rect_width = getRectWidth();

        var cna_order = {AMPLIFIED:4, HOMODELETED:3, GAINED:2, HEMIZYGOUSLYDELETED:1, DIPLOID: 0, null:0};
        var cnas = _.keys(range.cna);
        cnas = cnas.sort(function(a,b) {
            return cna_order[b] - cna_order[a];
        });

        cnas.forEach(function(cna) {
            var svg = getSvg(captions.cna[cna]);

            svg.append('rect')
                .attr('fill', function(d) {
                    return cna_fills[cna];
                })
                .attr('width', rect_width)
                .attr('height', RECT_HEIGHT);

            var text = svg.append('text')
                .attr('fill', 'black')
                .attr('x', text_padding)
                .attr('y', .75 * RECT_HEIGHT)
                .text(captions.cna[cna]);
        });

        for (var mrna in range.mrna) {
            var svg = getSvg(captions.mrna[mrna]);

            svg.append('rect')
                .attr('fill', cna_fills['none'])
                .attr('stroke-width', MRNA_STROKE_WIDTH)
                .attr('stroke-opacity', 1)
                .attr('width', rect_width)
                .attr('height', RECT_HEIGHT)
                .attr('stroke', mrna_fills[mrna]);

            var text = svg.append('text')
                .attr('fill', 'black')
                .attr('x', text_padding)
                .attr('y', .75 * RECT_HEIGHT)
                .text(captions.mrna[mrna]);
        }

        if (!$.isEmptyObject(range.mutations)) {
            var svg = getSvg(captions.mutation);

            // background of none
            svg.append('rect')
                .attr('fill', cna_fills['none'])
                .attr('width', rect_width)
                .attr('height', RECT_HEIGHT);

            // little mutation square
            svg.append('rect')
                .attr('fill', MUT_COLOR)
                .attr('y', LITTLE_RECT_HEIGHT)
                .attr('width', rect_width)
                .attr('height', LITTLE_RECT_HEIGHT);

            var text = svg.append('text')
                .attr('fill', 'black')
                .attr('x', text_padding)
                .attr('y', .75 * RECT_HEIGHT)
                .text(captions.mutation);
        }

        for (var rppa in range.rppa) {
            var svg = getSvg(captions.rppa[rppa]);

            var up_triangle = getTrianglePath(rect_width, true);
            var down_triangle = getTrianglePath(rect_width, false);

            // background of none
            svg.append('rect')
                .attr('fill', cna_fills['none'])
                .attr('width', rect_width)
                .attr('height', RECT_HEIGHT);

            svg.append('path')
                .attr('fill', 'black')
                .attr('d', function(d) {
                    if (rppa === UPREGULATED) {
                        return up_triangle;
                    }
                    if (rppa === DOWNREGULATED) {
                        return down_triangle;
                    }
                    if (rppa === null) {
                        return 'M 0 0';
                    }
                });

            var text = svg.append('text')
                .attr('fill', 'black')
                .attr('x', text_padding)
                .attr('y', .75 * RECT_HEIGHT)
                .text(captions.rppa[rppa]);
        }

        legend_el.append('p')
            .style('font-size', '12px')
            .style('margin-bottom', 0)
            .style('margin-top', 7 + 'px')
            .text('Copy number alterations are putative.');
    };
    //
    // end oncoprint legend

    var makeQtip = function() {
        var formatMutation = function(sample, hugo) {
            // helper function
            var mutation = query.data(sample, hugo, 'mutation');

            if (mutation !== null) {
                return "Mutation: <b>" + mutation + "</b><br/>";
            }
            return "";
        };

        var patientViewUrl = function(sample_id) {
            // helper function
            var href = "case.do?case_id=" + sample_id
                + "&cancer_study_id=" + params.cancer_study_id;

            return "<a href='" + href + "'>" + sample_id + "</a>";
        };


        // make qtip
        d3.selectAll('.sample').each(function(d, i) {
            $(this).qtip({
                content: {text: 'oncoprint qtip failed'},
                events: {
                    render: function(event, api) {
                        var content = '<font size="2">' + formatMutation(d.sample, d.hugo) + patientViewUrl(d.sample) + '</font>';
                        api.set('content.text', content);
                    }
                },
                hide: { fixed: true, delay: 100 },
                style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow' },
                //position: {my:'left top',at:'bottom center'}
                position: {my:'left bottom',at:'top right'}
            });
        });
    };

    that.draw = function() {

        label_width = (function() {
            // calculate the length of the longest label,
            // save it in the global variable for later use

            var label_svg = d3.select('body').append('svg');
            // put in a temporary svg for the sake of calculating text width

            gene_data.forEach(function(gene_obj) {
                var label = label_svg.append('text');
                var cleaned_hugo = cleanHugo(gene_obj.hugo);

                label.append('tspan')
                    .text(cleaned_hugo);
            });

          var longest = d3.max(
              label_svg.selectAll('text')[0]
                    .map(function(text, i) {return text.getBBox().width; })
            );

            label_svg.remove();
            // bye bye pretend svg

            return LABEL_PADDING + longest;

        })();

        var $header = $('<div>', {id:'oncoprint_header'});
        $(wrapper).append($header);

        $header.append(
            '<p>Case Set: ' + params.case_set_str + '</p></div>'
                + '<p>Altered in ' + query.altered_samples.length + ' (' + d3.format("%")(query.percent_altered) + ')'
                + ' of cases</p></div>');

        var visualized_samples = getVisualizedSamples();

        x.domain(visualized_samples);

//        var table_wrap = d3.select(wrapper).insert('table', ':first-child').append('tr');
        var table_wrap = d3.select(wrapper).append('table').append('tr');

        var label_svg = table_wrap.insert('td').insert('svg', ':first-child')
            .attr('id', "label")
            .attr('width', label_width)
            .attr('height', getHeight());

        // td-content is some parent td
        var body_wrap = table_wrap.append('td').append('div')
            .style('width', $('#td-content').width() - 70 - label_width + 'px') // buffer of, say, 70
            .style('display', 'inline-block')
            .style('overflow-x', 'auto')
            .style('overflow-y', 'hidden');

        svg = body_wrap.append('svg')
            .attr('id', 'body')
            .attr('width', getXScale(samples_all.length))
            .attr('height', getHeight());

        gene_data.forEach(function(gene_obj) {

            var hugo = gene_obj.hugo;
            var cleaned_hugo = cleanHugo(hugo);

            var track = svg.append('g')
                .attr('class', 'track');

            var label = label_svg.append('text')
                .attr('x', 0)
                .attr('y', y(hugo) + .75 * RECT_HEIGHT);

            label.append('tspan')
                .attr('font-weight', 'bold')
                .attr('text-anchor', 'start')
                .text(cleaned_hugo);

            label.append('tspan')
                .attr('text-anchor', 'end')
                .attr('x', label_width)
                .text(gene_obj.percent_altered);

            redraw(visualized_samples, track, hugo);
        });

        makeQtip();

        if (params.vis_key) {       // toggle the key to the visualization
            legend();
        }

        if (params.customize) {         // toggle the setup of the customization controls
            widthScrollerSetup();
        }
    };

    var transition = function() {
        // helper function

        var samples_visualized = getVisualizedSamples();
        var no_samples = samples_visualized.length;

        x.domain(samples_visualized);
        x.rangeBands([MRNA_STROKE_WIDTH, getXScale(no_samples)]);

        svg.selectAll('.track')[0].forEach(function(val, i) {

            var hugo = genes_list[i];

//            var transition = d3.select(val).transition();
            var transition = d3.select(val);

            transition.selectAll('.sample')
//                .transition()
//                .duration(1000)
                .attr('transform', function(d) {
                    return translate(x(d.sample), y(hugo));
                });

            var rect_width = getRectWidth();
            transition.selectAll('rect')
//                .transition()
//                .duration(1000)
                .attr('width', rect_width);

            var up_triangle = getTrianglePath(rect_width, true);
            var down_triangle = getTrianglePath(rect_width, false);
            transition.selectAll('.rppa')
//                .transition()
//                .duration(1000)
                .attr('d', function(d) {
                    var rppa = query.data(d.sample, hugo, 'rppa');

                    if (rppa === UPREGULATED) {
                        return up_triangle;
                    }
                    if (rppa === DOWNREGULATED) {
                        return down_triangle;
                    }
                });
        });

        svg.transition().duration(1000).style('width', getXScale(no_samples));
    };

    that.memoSort = function() {

        if (state.memo_sort) {
            return;
        }

        state.memo_sort = true;

        transition();
    };

    that.defaultSort = function() {

        if (!state.memo_sort) {
            return;
        }

        state.memo_sort = false;

        transition();
    };

    that.toggleWhiteSpace = function() {
        state.padding = !state.padding;

        transition();
    };

    that.scaleWidth = function(scalar) {
        state.width_scalar = scalar;
        transition();
    };

    that.toggleUnaltered = function() {
        state.show_unaltered = !state.show_unaltered;

        var samples_visualized = getVisualizedSamples();

        gene_data.forEach(function(gene, i) {
            var track = d3.select(d3.select(wrapper).selectAll('.track')[0][i]);
            redraw(samples_visualized, track, gene.hugo);
            transition();
        });

        makeQtip();
    };

//  For the given oncoprint reference, returns the SVG Dom as string
//  for the body canvas.
    that.getOncoPrintBodyXML = function() {
        // hard coding this for now

        var labels = $('#oncoprint svg#label').children().clone();
        var tracks = $('#oncoprint svg#body').children().clone();

        tracks.each(function(track_i, track) {
            // for each track loop over the samples
            $(track).children().each(function(sample_i, sample) {
                $(sample).attr('transform', translate(x(sample_i) + label_width, y(track_i)));
            });
        });

        var number_of_samples = $(tracks[0]).children().length;

        var export_svg = $('<svg>')
            .attr('width', getXScale(number_of_samples) + label_width)
            .attr('height', getHeight());

        export_svg
            .append(labels)
            .append(tracks);
//            .append(visualizedKeys(query.data_types));

        return (new XMLSerializer()).serializeToString(export_svg[0])
            .replace(' xmlns="http://www.w3.org/1999/xhtml"', '');
        // Firefox and safari implementations of XMLSerializer are different.
        // For some reason (actually a very good one) they think that this is
        // XHTML and give it the proper namespace.
    };

    return that;
};
//}}}
