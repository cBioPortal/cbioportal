//
// Gideon Dresdner
// June 2013
//
// util functions for the Oncoprint.  Basically allows for "static" methods to
// be tested without having to create an Oncoprint on a DOM element.
define("OncoprintUtils", (function() {
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
    // where a range is a 2-ple if the corresponding attribute values are numerical
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
        var extract_unique = function(raw_data, datatype, filter) {
            return _.chain(raw_gene_data)
                .map(function(d) {
                    return d[datatype];
                })
                .unique()
                .filter( function(d) {
                    return d !== undefined && (!filter || filter(d));
                })
                .value();
        };

        var cnas = extract_unique(raw_gene_data, 'cna');
        var mutations = extract_unique(raw_gene_data, 'mutation', function(d){
            var aas = d.split(",");// e.g. A32G,fusion
            for (var i=0, n=aas.length; i<n; i++) {
                if (!/fusion$/i.test(aas[i])) return true;
            }
            return false;
        });
        var fusions = extract_unique(raw_gene_data, 'mutation', function(d){return /fusion($|,)/i.test(d);});
        var mrnas = extract_unique(raw_gene_data, 'mrna');
        var rppas = extract_unique(raw_gene_data, 'rppa');

        var there_is_data = function(list) {
            return list.length > 0;
        };

        var to_return = {};

        if (there_is_data(cnas)) {
            to_return.cna = cnas;

            // sort the order that the cnas show up in the legend
            var cna_order = {
                "AMPLIFIED": 0,
                "GAINED": 1,
                "DIPLOID": 2,
                "HEMIZYGOUSLYDELETED": 3,
                "HOMODELETED": 4
            };

            to_return.cna = to_return.cna.sort(function(x, y) {
                return cna_order[x] - cna_order[y];
            });
        }

        if (there_is_data(mutations)) {
            to_return.mutation = mutations;
        }
        
        if (there_is_data(fusions)) {
            to_return.fusion = fusions;
        }

        if (there_is_data(mrnas)) {
            to_return.mrna = mrnas;
        }

        if (there_is_data(rppas)) {
            to_return.rppa = rppas;
        }

        return to_return;
    };

    var colors = {
        continuous: '#A62459',
        white: '#FFFAF0',
        discrete: '#404040',
        red: '#FF0000',
        grey: '#D3D3D3',
        google: ["#3366cc","#dc3912","#ff9900","#109618",
        "#990099","#0099c6","#dd4477","#66aa00",
        "#b82e2e","#316395","#994499","#22aa99",
        "#aaaa11","#6633cc","#e67300","#8b0707",
        "#651067","#329262","#5574a6","#3b3eac",
        "#b77322","#16d620","#b91383","#f4359e",
        "#9c5935","#a9c413","#2a778d","#668d1c",
        "#bea413","#0c5922","#743411"]
    };

    // takes a map attr2range, and transforms the ranges into d3 scales
    // it does something very simplistic :
    //  *signature:* `string -> discrete , number -> continuous`
    var attr2range_to_d3scale = function(attr2range) {
        for (var a2r in attr2range) {
            var range = attr2range[a2r];
            var discrete = is_discrete(range[0]);
            var range_vals;

            if (!discrete) {
                range_vals = [colors.white, colors.continuous];
            } else if (range.length > 2) {
                range_vals = colors.google.slice(0,range.length);
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
            return colors.google.slice(0, attrId2range[attr_id].length);
        };

        return _.chain(attrs)
            .map(function(attr) {
                // attr -> [attr_id, d3 scale]
                var scale;

                // manually override colors for sex (if only De Beauvoir could witness this)
                if (attr.attr_id.toUpperCase() === "SEX"
                    || attr.attr_id.toUpperCase() === "GENDER") {
                    scale = d3.scale.ordinal()
                .domain(["MALE", "male", "FEMALE", "female"])
                .range(["#3790d6", "#3790d6", "pink", "pink"]);

                    return [attr.attr_id, scale];
                }

                // manually override colors for genomic subtypes to match the endometrial paper (doi:10.1038/nature12113)
                if (attr.attr_id.toUpperCase() === "SUBTYPE") {
                    scale = d3.scale.ordinal()
                        .domain(["POLE (Ultra-mutated)", "MSI (Hyper-mutated)", "Copy-number low (Endometriod)", "Copy-number high (Serous-like)"])
                        .range(["#3366cc", "#109618", "#ff9900", "#dc3912"]);

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

        return !(
                (sample_gene.cna === undefined || sample_gene.cna === "DIPLOID")
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

    // takes a list of clinical attribute objects and returns a map that maps
    // `attr_id` to the corresponding clinical attribute object
    //
    // *signature:* `[array] -> {object}`
    var createId2ClinicalAttr = function(clinical_attrs) {
        return id2ClinicalAttr = clinical_attrs.reduce(
                function(name2attr, attr) {
                    name2attr[attr.attr_id] = attr;
                    return name2attr;
                }, {});
    };

    // takes a map and returns a function that checks whether the input key is
    // in the map or not.
    // If it is then return the value, otherwise return the key right back
    // Problem : what if key === value ?  At least this function suffices for
    // our purposes
    //
    // *signature:* `{object} -> function(key)`
    var maybe_map = function(map) {
        return function(key) {
            var value = map[key];
            if (value === undefined) {
                return key;
            }
            else {
                return value;
            }
        };
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
            options.display_fusion = options.display_fusion || "none";
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
            mutation: "Mutation",
            fusion: "Fusion"
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

        if (datatype2range.fusion !== undefined) {
            templates = templates.concat(
                    item_templater({ display_fusion: "inherit", text: captions.fusion})
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
                if (d.mutation) {
                    if (/fusion($|,)/i.test(d.mutation)) return "<b>" + d.mutation + "</b><br/>";
                    else return "Mutation: <b>" + d.mutation + "</b><br/>";
                }
                return "";
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
        var href = cbio.util.getLinkToSampleView(window.cancer_study_id_selected,sample_id);
        return "<a href='" + href + "'>" + sample_id + "</a>";
    };

    // params: els, list of d3 selected elements with either gene data or
    // clinical bound to them
    var make_mouseover = function(els,params) {
        els.each(function(d) {
            $(this).qtip({
                content: {text: 'oncoprint qtip failed'},
                position: {my:'left bottom', at:'top right', viewport: $(window)},
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
	            show: {event: "mouseover"},
                hide: {fixed: true, delay: 100, event: "mouseout"},
                events: {
                    render: function(event, api) {
                        var content;
                        var sampleLink = params.linkage?patientViewUrl(d.sample):d.sample;
                        if (d.attr_id) {
                            content = '<font size="2">'
                                + format.clinical(d)
                                + '<font color="blue">' +sampleLink+'</font>' + '</font>';
                        } else {
                            content = '<font size="2">'
                                + format.mutation(d)
                                + format.cna(d)
                                + format.mrna(d)
                                + format.rppa(d)
                                +'<font color="blue">' +sampleLink+'</font>' + '</font>';

                        }
                        api.set('content.text', content);
                    }
                }
            });
        });
    };

    // takes a div and creates a zoombar on it.
    // It calls the function fun on the change event of the zoombar
    //
    // *signature:* `DOM el, function -> DOM el`
    var zoomSetup = function(div, fun) {
        return $('<div>', { id: "width_slider", width: "100"})
            .slider({ text: "Adjust Width ", min: .1, max: 1, step: .01, value: 1,
                change: function(event, ui) {
                    fun(ui.value, 'animation');       // N.B.
                }}).appendTo($(div));
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
        maybe_map: maybe_map,
        normalize_clinical_attributes: normalize_clinical_attributes,
        normalize_nested_values: normalize_nested_values,
        legend: legend,
        make_attribute2scale: make_attribute2scale,
        gene_data_type2range: gene_data_type2range,
        is_gene: is_gene,
        is_clinical: is_clinical,
        cna_fills: cna_fills,
        colors: colors,
        populate_clinical_attr_select: populate_clinical_attr_select,
        make_mouseover: make_mouseover,
        zoomSetup: zoomSetup
    };
})()
);
