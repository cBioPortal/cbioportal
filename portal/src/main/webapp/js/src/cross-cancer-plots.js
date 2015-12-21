/**
 * Created by suny1 on 9/2/15.
 */

var ccPlots = (function ($, _, Backbone, d3) {

    var data = (function () {

        var retrieved_genes = [], //list of genes that already retrieved data and stored here
            mut_proxy = "", mut_obj = {};

        var median_vals = []; //{gene: {profileId:"", median:0}}

        var ProfileMeta = Backbone.Model.extend({
            defaults: {
                GENETIC_ALTERATION_TYPE: "",
                NAME: "",
                DESCRIPTION: "",
                STABLE_ID: "",
                CANCER_STUDY_STABLE_ID: "",
                CASE_SET_ID: "",
                CANCER_STUDY_NAME: "",
                CANCER_STUDY_SHORT_NAME: "",
                SEQ_CASE_IDS: []
            }
        });

        var ProfileMetaListTmp = Backbone.Collection.extend({
            defaults: {cancer_study_id: ""},
            model: ProfileMeta,
            urlRoot: "getGeneticProfile.json",
            url: function () {
                return this.urlRoot + "?cancer_study_id=" + this.cancer_study_id;
            },
            initialize: function (_study_id) {
                this.cancer_study_id = _study_id;
                this.models.CANCER_STUDY_STABLE_ID = _study_id;
            },
            parse: function (response) {
                return _.filter(_(response).toArray(), function (obj) {
                    return (obj.STABLE_ID.indexOf("_rna_seq_v2_mrna") !== -1 &&
                    obj.STABLE_ID.toLowerCase().indexOf("zscore") === -1);
                });
            }
        });

        var ProfileMetaList = Backbone.Collection.extend({model: ProfileMeta});

        var ProfileData = Backbone.Model.extend({
            default: {
                gene: "",
                caseId: "",
                profileId: "",
                value: "",
                mutation: "",
                mutation_type: ""
            }
        });

        var ProfileDataListTmp = Backbone.Collection.extend({
            defaults: {is_last_study: false},
            model: ProfileData,
            urlRoot: "getProfileData.json",
            url: function () {
                return this.urlRoot + "?cancer_study_id=" + this.cancer_study_id +
                    "&gene_list=" + this.gene_list +
                    "&genetic_profile_id=" + this.genetic_profile_id +
                    "&case_set_id=" + this.case_set_id +
                    "&case_ids_key=" + this.case_ids_key
            },
            initialize: function (_study_id, _gene_list, _profile_id, _case_set_id, _case_ids_key) {
                this.cancer_study_id = _study_id;
                this.gene_list = _gene_list;
                this.genetic_profile_id = _profile_id;
                this.case_set_id = _case_set_id;
                this.case_ids_key = _case_ids_key;
            },
            parse: function (response) {
                var _results = [];
                var genes = _.keys(response);

                _.each(genes, function (gene) {
                    _.each(Object.keys(response[gene]), function (_case_id) {
                        var _profile_id = Object.keys(response[gene][_case_id])[0];
                        var _value = response[gene][_case_id][_profile_id];
                        var _result_obj = {};
                        _result_obj.gene = gene;
                        _result_obj.caseId = _case_id;
                        _result_obj.profileId = _profile_id;
                        _result_obj.value = _value;
                        _result_obj.mutation = "";
                        _result_obj.mutation_type = "";
                        _results.push(_result_obj);
                    });
                });
                return _results;
            }
        });

        var ProfileDataList = Backbone.Collection.extend({model: ProfileData});

        var profileMetaList = new ProfileMetaList();
        var profileDataList = new ProfileDataList();

        function findMedian(data) {
            var m = data.map(function(v) {
                return v;
            }).sort(function(a, b) {
                return a - b;
            });
            var middle = Math.floor((m.length - 1) / 2); // NB: operator precedence
            if (m.length % 2) {
                return m[middle];
            } else {
                return (m[middle] + m[middle + 1]) / 2.0;
            }
        }

        return {
            init: function (callback_func) {
                var tmp = setInterval(function () {timer();}, 1000);
                function timer() {
                    if (window.studies !== undefined) {
                        clearInterval(tmp);
                        //retrieve profiles meta
                        $.each(_.pluck(window.studies.models, "attributes"), function (_study_index, _study_obj) {
                            var _cancer_study_name = window.PortalMetaData["cancer_studies"][_study_obj.studyId].name;

                            var _include_study = false; //exclude certain studies
                            if (_study_obj.studyId.indexOf("esca") === -1 && _study_obj.studyId.indexOf("stad") === -1) {
                                if (_cancer_study_name.indexOf("TCGA") !== -1) {
                                    if (_cancer_study_name.toLowerCase().indexOf("provisional") !== -1) _include_study = true;
                                } else {
                                    _include_study = true;
                                }
                            }
                            if (_include_study) {
                                window.cbioportal_client.getPatientLists({patient_list_ids: [_study_obj.studyId + "_sequenced"]}).then(function(d) {
                                    var profileMetaListTmp = new ProfileMetaListTmp(_study_obj.studyId);
                                    profileMetaListTmp.fetch({
                                        success: function (profileMetaListTmp) {
                                            _.each(_.pluck(profileMetaListTmp.models, "attributes"), function (_profile_obj) {
                                                _profile_obj.CANCER_STUDY_STABLE_ID = profileMetaListTmp.cancer_study_id;
                                                _profile_obj.CASE_SET_ID = profileMetaListTmp.cancer_study_id + "_all";
                                                _profile_obj.CANCER_STUDY_NAME = window.PortalMetaData["cancer_studies"][_study_obj.studyId].name;
                                                _profile_obj.CANCER_STUDY_SHORT_NAME = window.PortalMetaData.cancer_studies[_study_obj.studyId].short_name.replace("(TCGA)", "");
                                                if (d[0] === undefined) {
                                                    _profile_obj.SEQ_CASE_IDS = [];
                                                } else {
                                                    _profile_obj.SEQ_CASE_IDS = d[0].patient_ids;
                                                }
                                            });
                                            profileMetaList.add(profileMetaListTmp.models);
                                            if (_study_index + 1 === window.studies.length) { //reach the end of the iteration
                                                var _tmp = setInterval(function () {timer();}, 1000);
                                                function timer() {
                                                    if (window.crossCancerMutationProxy !== undefined) {
                                                        clearInterval(_tmp);
                                                        mut_proxy = window.crossCancerMutationProxy;
                                                        function _mutation_call_back(_mut_obj) {
                                                            mut_obj = _mut_obj;
                                                            callback_func();
                                                        }
                                                        mut_proxy.getMutationData(window.ccQueriedGenes.join(" "), _mutation_call_back);
                                                    }
                                                }
                                            }
                                        }
                                    });
                                });
                            }
                        });
                    }
                }
            },
            get: function (_gene, callback_func) {
                if ($.inArray(_gene, retrieved_genes) === -1) {
                    $.each(_.pluck(profileMetaList.models, "attributes"), function (_profile_index, _profile_obj) {
                        var profileDataListTmp =
                            new ProfileDataListTmp(_profile_obj.CANCER_STUDY_STABLE_ID, _gene, _profile_obj.STABLE_ID, _profile_obj.CASE_SET_ID, "-1");
                        profileDataListTmp.fetch({
                            success: function (profileDataListTmp) {
                                _.each(mut_obj, function(_mut_obj) {
                                    _.each(profileDataListTmp.models, function(_model) {
                                        if(_mut_obj.caseId === _model.attributes.caseId && _mut_obj.geneSymbol === _gene) {
                                            _model.attributes.mutation += _mut_obj.proteinChange + ";";
                                            _model.attributes.mutation_type += mutationTranslator(_mut_obj.mutationType) + ";";
                                        }
                                    });
                                })

                                //calculate median value
                                var _values = [];
                                _.each(profileDataListTmp.models, function(_model) {
                                    if (_model.attributes.value !== "NaN") {
                                        _values.push(parseFloat(_model.attributes.value));
                                    }
                                });
                                var _tmp_obj = {
                                    profile_id: _profile_obj.STABLE_ID,
                                    median: findMedian(_values)
                                };
                                if (median_vals.hasOwnProperty(_gene)) {
                                    median_vals[_gene].push(_tmp_obj);
                                } else {
                                    median_vals[_gene] = [_tmp_obj];
                                }

                                profileDataList.add(profileDataListTmp.models);

                                var _profileDataList_gene = _.filter(_.pluck(profileDataList.models, "attributes"), function(item) { return item.gene === $("#cc_plots_gene_list").val(); });
                                var _profile_length = _.uniq(_.pluck(_profileDataList_gene, "profileId")).length;
                                if (_profile_length === profileMetaList.length) {
                                    retrieved_genes.push(_gene);
                                    callback_func(_.filter(profileDataList.models, function (model) {
                                        return model.get("gene") === _gene;
                                    }));
                                }
                            }
                        });
                    });
                } else {
                    callback_func(_.filter(profileDataList.models, function (model) {
                        return model.get("gene") === _gene;
                    }));
                }
            },
            get_meta: function(_opt) {
                var _arr = _.filter(_.pluck(profileMetaList.models, "attributes"), function(profile_obj) {
                    return (profile_obj.NAME.toLowerCase().indexOf("rsem") !== -1);
                });
                if (_opt === "alphabetic") {
                    _arr = _.sortBy(_arr, "CANCER_STUDY_SHORT_NAME");
                    return _arr;
                } else if (_opt === "median") {
                    _.each(_arr, function(_obj) {
                        _.each(median_vals[$("#cc_plots_gene_list").val()], function(_median_val_obj) {
                            if (_median_val_obj.profile_id === _obj.STABLE_ID) {
                                _obj["MEDIAN"] = _median_val_obj.median;
                            }
                        });
                    });
                    _arr = _.sortBy(_arr, "MEDIAN");
                    return _arr;
                }
            },
            get_profile_name: function(_profile_id) {
                var _profile_obj = _.filter(_.pluck(profileMetaList.models, "attributes"), function(_profile_item) { return _profile_item.STABLE_ID === _profile_id; });
                return _profile_obj[0].NAME;
            },
            get_cancer_study_name: function(_profile_id) {
                var _profile_obj = _.filter(_.pluck(profileMetaList.models, "attributes"), function(_profile_item) { return _profile_item.STABLE_ID === _profile_id; });
                return _profile_obj[0].CANCER_STUDY_NAME;
            },
            is_sequenced: function(_profile_id, _sample_id) {
                var _profile_obj = _.filter(_.pluck(profileMetaList.models, "attributes"), function(profile_obj) {
                    return (profile_obj.STABLE_ID === _profile_id);
                })[0];
                //TODO: should return list of samples instead list of patients.
                return $.inArray(_sample_id.substring(0, _sample_id.length - 3), _profile_obj.SEQ_CASE_IDS) !== -1;
            }
        }

    }());

    var view = (function () {

        var elem = {
                svg: "",
                x: {
                    scale: "",
                    axis: ""
                },
                y: {
                    scale: "",
                    axis: ""
                },
                dots: "",
                title: "",
                box_plots: ""
            },
            settings = {
                canvas_width: 0,
                canvas_height: 615,
                log_scale: {
                    threshold_down : 0.17677669529,  //-2.5 to 10
                    threshold_up : 1.2676506e+30
                }
            },
            init_sidebar = function () {

                //SVG & PDF buttons
                $("#cc_plots_svg_download").click(function() {
                    var xmlSerializer = new XMLSerializer();
                    var download_str = cbio.download.addSvgHeader(xmlSerializer.serializeToString($("#cc-plots-box svg")[0]));
                    cbio.download.clientSideDownload([download_str], "cross-cancer-plots-download.svg", "application/svg+xml");
                });
                $("#cc_plots_pdf_download").click(function() {
                    var downloadOptions = {
                        filename: "cross-cancer-plots-download.pdf",
                        contentType: "application/pdf",
                        servletName: "svgtopdf.do"
                    };
                    cbio.download.initDownload(
                        $("#cc-plots-box svg")[0], downloadOptions);
                });
                $("#cc_plots_data_download").click(function() {
                    cbio.download.clientSideDownload([ccPlots.get_tab_delimited_data()], "plots-data.txt");
                });
            },
            init_canvas = function() {
                if (_.pluck(data.get_meta($('input[name=cc_plots_profile_order_opt]:checked').val()), "STABLE_ID").length < 8) {
                    settings.canvas_width = _.pluck(data.get_meta($('input[name=cc_plots_profile_order_opt]:checked').val()), "STABLE_ID").length * 100 + 270;
                } else {
                    settings.canvas_width = 1090;
                }

                elem.svg = d3.select("#cc-plots-box")
                    .append("svg")
                    .attr("id", "cc-plots-canvas")
                    .attr("width", settings.canvas_width)
                    .attr("height", settings.canvas_height);
            },
            init_box = function (_input) {

                $("#cc_plots_loading").remove();

                //data
                var _data = _.filter(_.pluck(_input, "attributes"), function(item) {
                    return item.value !== "NaN"
                });
                _.each(_data, function(_data_item) {
                    _data_item.mutation = _.uniq(_data_item.mutation.split(";")).join(", ");
                    if (_data_item.mutation.indexOf(",") !== -1) _data_item.mutation = _data_item.mutation.substring(0, _data_item.mutation.length - 2);
                    _data_item.mutation_type = _data_item.mutation_type.split(";")[0];
                    if (_data_item.mutation === "") { _data_item.mutation = "non"; }
                    if (_data_item.mutation_type === "" ) { _data_item.mutation_type = "non"; }
                });

                function bubble_up(_arr, _index) {
                    for (var i = _index; i > 0; i--) {
                        var _tmp_obj = _arr[i - 1];
                        _arr[i - 1] = _arr[i];
                        _arr[i] = _tmp_obj;
                    }
                }
                $.each(_data, function(_index, _data_item) {
                    if ((data.is_sequenced(_data_item.profileId, _data_item.caseId)) && _data_item.mutation === "non") {
                        bubble_up(_data, _index);
                    }
                });

                //x axis
                var x_axis_right = 0;
                if (_.pluck(data.get_meta($('input[name=cc_plots_profile_order_opt]:checked').val()), "STABLE_ID").length < 8) {
                    x_axis_right = (_.pluck(data.get_meta($('input[name=cc_plots_profile_order_opt]:checked').val()), "STABLE_ID").length * 100 + 160);
                } else {
                    x_axis_right = 980;
                }

                elem.x.scale = d3.scale.ordinal()
                    .domain(_.pluck(data.get_meta($('input[name=cc_plots_profile_order_opt]:checked').val()), "STABLE_ID"))
                    .rangeRoundBands([160, x_axis_right]);

                elem.x.axis = d3.svg.axis()
                    .scale(elem.x.scale)
                    .orient("bottom");

                elem.svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("class", "x-axis")
                    .attr("transform", "translate(0, 520)")
                    .call(elem.x.axis.ticks(data.get_meta($('input[name=cc_plots_profile_order_opt]:checked').val()).length))
                    .selectAll("text")
                    .data(data.get_meta($('input[name=cc_plots_profile_order_opt]:checked').val()))
                    .attr("class", "x-axis-label")
                    .style("font-family", "sans-serif")
                    .style("font-size", "12px")
                    .style("stroke-width", 0.5)
                    .style("stroke", "black")
                    .style("fill", "black")
                    .style("text-anchor", "end")
                    .attr("transform", function() { return "rotate(-30)"; })
                    .text(function(d) {
                        return d.CANCER_STUDY_SHORT_NAME;
                    });
                elem.svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(0, 20)")
                    .call(elem.x.axis.tickFormat("").ticks(0).tickSize(0));

                elem.svg.selectAll(".x-axis-label").each(function(d) {
                    $(this).qtip(
                        {
                            content: {text: "<font size=2>" + d.CANCER_STUDY_NAME + "</font>" },
                            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                            show: {event: "mouseover"},
                            hide: {fixed:true, delay: 100, event: "mouseout"},
                            position: {my:'left bottom',at:'top right', viewport: $(window)}
                        }
                    );
                });

                //y axis
                var _y_str_arr = _.filter(_.pluck(_.pluck(_input, "attributes"), "value"), function(item) { return item !== "NaN"});
                var _y_arr = _.map(_y_str_arr, function(item) {
                    return parseInt(item, 10);
                });
                var _min_y = _.min(_y_arr) - 0.1 * (_.max(_y_arr) - _.min(_y_arr));
                var _max_y = _.max(_y_arr) + 0.1 * (_.max(_y_arr) - _.min(_y_arr));
                elem.y.scale = d3.scale.linear()
                    .domain([_min_y, _max_y])
                    .range([520, 20]);

                elem.y.axis = d3.svg.axis()
                    .scale(elem.y.scale)
                    .orient("left");

                elem.svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(160, 0)")
                    .attr("class", "y-axis")
                    .call(elem.y.axis.ticks(5))
                    .selectAll("text")
                    .style("font-family", "sans-serif")
                    .style("font-size", "12px")
                    .style("stroke-width", 0.5)
                    .style("stroke", "black")
                    .style("fill", "black")
                    .style("text-anchor", "end");
                elem.svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(" + x_axis_right + ", 0)")
                    .call(elem.y.axis.ticks(0));

                //y axis title
                d3.select("#ccPlots").select("y title").remove();
                elem.title = elem.svg.append("svg:g");
                elem.title.append("text")
                    .attr("class", "y-title")
                    .attr("transform", "rotate(-90)")
                    .attr("x", -250)
                    .attr("y", 70)
                    .style("text-anchor", "middle")
                    .style("font-weight","bold")
                    .text($("#cc_plots_gene_list").val() + " expression -- RNA-Seq V2");

                //draw dots
                var _plots_data = _data;
                $.each(_plots_data, function(index, obj) { //sort data array (to plot the mutated dots last)
                    if (obj.mutation === "non") {
                        bubble_up(_plots_data, index);
                    }
                });
                elem.dots = elem.svg.append("svg:g");
                elem.dots.selectAll("path").remove();
                elem.dots.selectAll("path")
                    .data(_plots_data)
                    .enter()
                    .append("svg:path")
                    .attr("class", "dot")
                    .attr("d", d3.svg.symbol()
                        .size(20)
                        .type(function(d) {
                            data.is_sequenced(d.profileId, d.caseId);
                            $(this).attr("size", 20);
                            $(this).attr("ori_shape", mutationStyle.getSymbol(d.mutation_type));
                            $(this).attr("case_id", d.caseId);
                            $(this).attr("mutation", d.mutation);
                            $(this).attr("x_val", d.profileId);
                            $(this).attr("y_val", d.value);
                            return mutationStyle.getSymbol(d.mutation_type);
                        }))
                    .attr("fill", function(d) {
                        if (d.mutation !== "non") {
                            $(this).attr("ori_fill", mutationStyle.getFill(d.mutation_type));
                            $(this).attr("class", "sequenced-sample");
                            return mutationStyle.getFill(d.mutation_type);
                        } else {
                            if (data.is_sequenced(d.profileId, d.caseId)) {
                                $(this).attr("ori_fill", mutationStyle.getFill(d.mutation_type));
                                $(this).attr("class", "sequenced-sample");
                                return mutationStyle.getFill(d.mutation_type);
                            } else {
                                $(this).attr("ori_fill", "none");
                                $(this).attr("class", "not-sequenced-sample")
                                return "none";
                            }
                        }
                    })
                    .attr("stroke", function(d) {
                        if (d.mutation !== "non") {
                            $(this).attr("ori_stroke", mutationStyle.getStroke(d.mutation_type));
                            return mutationStyle.getStroke(d.mutation_type);
                        } else {
                            if (data.is_sequenced(d.profileId, d.caseId)) {
                                $(this).attr("ori_stroke", mutationStyle.getStroke(d.mutation_type));
                                return mutationStyle.getStroke(d.mutation_type);
                            } else {
                                $(this).attr("ori_stroke", "gray");
                                return "gray";
                            }
                        }

                    })
                    .attr("stroke-width", 1.2)
                    .attr("transform", function(d) {
                        var _x = elem.x.scale(d.profileId) + elem.x.scale.rangeBand() / 2 + _.random(elem.x.scale.rangeBand() / 5 * (-1), elem.x.scale.rangeBand()/5);
                        var _y = elem.y.scale(parseInt(d.value));
                        $(this).attr("x_pos", _x);
                        $(this).attr("y_pos", _y);
                        return "translate(" + _x + ", " + _y + ")";
                    });

                //add glyphs
                var _mutation_types = [];
                _mutation_types.push("non_sequenced");
                _mutation_types.push("non");
                _mutation_types = _mutation_types.concat(_.uniq(_.pluck(_.filter(_data, function(_item) { return _item.mutation !== "non"; }), "mutation_type")));
                var _glyph_objs = [];

                _.each(_mutation_types, function(_type) {
                    var _tmp = {};
                    _tmp.symbol = mutationStyle.getSymbol(_type);
                    _tmp.fill = mutationStyle.getFill(_type);
                    _tmp.stroke = mutationStyle.getStroke(_type);
                    _tmp.text = mutationStyle.getText(_type);
                    _glyph_objs.push(_tmp);
                });

                var legend = elem.svg.selectAll(".legend")
                    .data(_glyph_objs)
                    .enter().append("g")
                    .attr("class", function(d) {
                        if (d.text === "Not sequenced") {
                            return "not-sequenced-legend"
                        } else if (d.text === "Not mutated") {
                            return "no-mutation-legend";
                        } else {
                            return "other-legend";
                        }
                    })
                    .attr("transform", function(d, i) {
                        if (_.pluck(data.get_meta($('input[name=cc_plots_profile_order_opt]:checked').val()), "STABLE_ID").length < 8) {
                            return "translate(" + (_.pluck(data.get_meta($('input[name=cc_plots_profile_order_opt]:checked').val()), "STABLE_ID").length * 100 + 170) + ", " + (25 + i * 15) + ")";
                        } else {
                            return "translate(990, " + (25 + i * 15) + ")";
                        }
                    });

                legend.append("path")
                    .attr("width", 18)
                    .attr("height", 18)
                    .attr("d", d3.svg.symbol()
                        .size(30)
                        .type(function(d) { return d.symbol; }))
                    .attr("fill", function (d) { return d.fill; })
                    .attr("stroke", function (d) { return d.stroke; })
                    .attr("stroke-width", 1.1);

                legend.append("text")
                    .attr("dx", ".75em")
                    .attr("dy", ".35em")
                    .style("text-anchor", "front")
                    .text(function(d){return d.text;});

                //add mouseover
                elem.dots.selectAll("path").each(function(d) {
                    var _content = "<strong><a href='" +
                        cbio.util.getLinkToSampleView(d.profileId.substring(0, d.profileId.indexOf("_rna_seq_v2_mrna")), d.caseId) +
                        "' target = '_blank'>" + d.caseId +
                        "</strong></a><br>mRNA expression: <strong>" + d.value + "</strong>";
                    if (d.mutation !== "non") {
                        _content += "<br>Mutation: <strong>" + d.mutation + "</strong>";
                    }

                    $(this).qtip(
                        {
                            content: {text: _content},
                            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                            show: {event: "mouseover"},
                            hide: {fixed:true, delay: 100, event: "mouseout"},
                            position: {my:'left bottom',at:'top right', viewport: $(window)}
                        }
                    );
                });
                var mouseOn = function() {
                    var dot = d3.select(this);
                    dot.transition()
                        .ease("elastic")
                        .duration(600)
                        .delay(100)
                        .attr("d", d3.svg.symbol()
                            .size(200)
                            .type(function(d) {
                                if ($("#cc_plots_show_mut").is(':checked')) {
                                    return $(this).attr("ori_shape");
                                } else {
                                    mutationStyle.getSymbol("non");
                                }
                            })
                    );
                };
                var mouseOff = function() {
                    var dot = d3.select(this);
                    dot.transition()
                        .ease("elastic")
                        .duration(600)
                        .delay(100)
                        .attr("d", d3.svg.symbol()
                            .size(function(d) {
                                return $(this).attr("size");
                            })
                            .type(function(d) {
                                if ($("#cc_plots_show_mut").is(':checked')) {
                                    return $(this).attr("ori_shape");
                                } else {
                                    mutationStyle.getSymbol("non");
                                }
                            })
                    );
                };

                elem.dots.selectAll("path").on("mouseover", mouseOn);
                elem.dots.selectAll("path").on("mouseout", mouseOff);

                //add box plots
                var _box_plots_datum = {
                        x_val: "",
                        y_val: []
                    },
                    _box_plots_data_arr = [];
                _.each(_.pluck(data.get_meta($('input[name=cc_plots_profile_order_opt]:checked').val()), "STABLE_ID"), function(_profile_id) {
                    var _tmp_y_val_arr = [];
                    _.each(_data, function(_data_item) {
                        if (_data_item.profileId === _profile_id) {
                            _tmp_y_val_arr.push(parseFloat(_data_item.value));
                        }
                    });
                    _tmp_y_val_arr.sort(function(a, b) { return (a - b); });
                    var _datum = jQuery.extend(true, {}, _box_plots_datum);
                    _datum.x_val = _profile_id;
                    _datum.y_val = _tmp_y_val_arr;
                    _box_plots_data_arr.push(_datum);
                });
                add_box_plots(_box_plots_data_arr);

                if (document.getElementById("cc_plots_log_scale").checked) {
                    apply_log_scale(_input);
                }
            }; //close init_box()

        function add_box_plots(_box_plots_data_arr) {
            d3.select("#cc-plots-box").select(".cc_plots_box_plots").remove();
            elem.box_plots = elem.svg.append("svg:g").attr("class", "cc_plots_box_plots");
            var _box_plots_color = "#000000",
                _box_plots_opacity = 0.2;
            $.each(_box_plots_data_arr, function(index, obj) {
                if (obj.y_val.length !== 0) {
                    if (obj.y_val.length === 1) { //just one simple line

                    } else if (obj.y_val.length === 2) {

                    } else { //regular box
                        var top, bottom, quan1, quan2, mean, IQR, midLine, width;
                        var yl = obj.y_val.length;
                        var _data = obj.y_val;

                        width = elem.x.scale.rangeBand() / 2 - 5;
                        midLine = elem.x.scale(obj.x_val) + elem.x.scale.rangeBand() / 2;
                        if (yl % 2 === 0) {
                            mean = elem.y.scale((_data[(yl / 2)-1] + _data[yl / 2]) / 2);
                            if (yl % 4 === 0) {
                                quan1 = elem.y.scale((_data[(yl / 4)-1] + _data[yl / 4]) / 2);
                                quan2 = elem.y.scale((_data[(3*yl / 4)-1] + _data[3 * yl / 4]) / 2);
                            } else {
                                quan1 = elem.y.scale(_data[Math.floor(yl / 4)]);
                                quan2 = elem.y.scale(_data[Math.floor(3 * yl / 4)]);
                            }
                        } else {
                            mean = elem.y.scale(_data[Math.floor(yl / 2)]);
                            var tmp_yl = Math.floor(yl / 2) + 1;
                            if (tmp_yl % 2 === 0) {
                                quan1 = elem.y.scale((_data[tmp_yl / 2 - 1] + _data[tmp_yl / 2]) / 2);
                                quan2 = elem.y.scale((_data[(3 * tmp_yl / 2) - 2] + _data[(3 * tmp_yl / 2) - 1]) / 2);
                            } else {
                                quan1 = elem.y.scale(_data[Math.floor(tmp_yl / 2)]);
                                quan2 = elem.y.scale(_data[tmp_yl - 1 + Math.floor(tmp_yl / 2)]);
                            }
                        }
                        var _scaled_arr = [];
                        $.each(_data, function(index, value) {
                            _scaled_arr.push(elem.y.scale(value));
                        });
                        _scaled_arr.sort(function(a,b) { return (a - b); });
                        IQR = Math.abs(quan2 - quan1);
                        var index_top = searchIndexTop(_scaled_arr, (quan2 - 1.5 * IQR));
                        top = _scaled_arr[index_top];
                        var index_bottom = searchIndexBottom(_scaled_arr, (quan1 + 1.5 * IQR));
                        bottom = _scaled_arr[index_bottom];


                        elem.box_plots.append("rect")
                            .attr("x", midLine - width)
                            .attr("y", quan2)
                            .attr("width", width * 2)
                            .attr("height", IQR)
                            .attr("fill", "none")
                            .attr("stroke-width", 1)
                            .attr("stroke", _box_plots_color)
                            .attr('opacity', _box_plots_opacity);
                        elem.box_plots.append("line")
                            .attr("x1", midLine - width)
                            .attr("x2", midLine + width)
                            .attr("y1", mean)
                            .attr("y2", mean)
                            .attr("stroke-width", 2)
                            .attr("stroke", _box_plots_color)
                            .attr('opacity', _box_plots_opacity);
                        elem.box_plots.append("line")
                            .attr("x1", midLine - width)
                            .attr("x2", midLine + width)
                            .attr("y1", top)
                            .attr("y2", top)
                            .attr("stroke-width", 1)
                            .attr("stroke", _box_plots_color)
                            .attr('opacity', _box_plots_opacity);
                        elem.box_plots.append("line")
                            .attr("x1", midLine - width)
                            .attr("x2", midLine + width)
                            .attr("y1", bottom)
                            .attr("y2", bottom)
                            .attr("stroke", _box_plots_color)
                            .style("stroke-width", 1)
                            .attr('opacity', _box_plots_opacity);
                        elem.box_plots.append("line")
                            .attr("x1", midLine)
                            .attr("x2", midLine)
                            .attr("y1", quan1)
                            .attr("y2", bottom)
                            .attr("stroke", _box_plots_color)
                            .attr("stroke-width", 1)
                            .attr('opacity', _box_plots_opacity);
                        elem.box_plots.append("line")
                            .attr("x1", midLine)
                            .attr("x2", midLine)
                            .attr("y1", quan2)
                            .attr("y2", top)
                            .attr("stroke", _box_plots_color)
                            .style("stroke-width", 1)
                            .attr('opacity', _box_plots_opacity);
                    }
                }
            });
        }

        function apply_log_scale(_input) {

            //y axis
            var _y_str_arr = _.filter(_.pluck(_.pluck(_input, "attributes"), "value"), function(item) { return item !== "NaN"});
            var _scaled_y_arr = _.map(_y_str_arr, function(item) {
                var _orig_val = parseInt(item, 10);
                var _scaled_val = 0;
                if (_orig_val <= settings.log_scale.threshold_down) {
                    _scaled_val = Math.log(settings.log_scale.threshold_down) / Math.log(2);
                } else if (_orig_val >= settings.log_scale.threshold_up) {
                    _scaled_val = Math.log(settings.log_scale.threshold_up) / Math.log(2);
                } else {
                    _scaled_val = Math.log(_orig_val) / Math.log(2);
                }
                return _scaled_val;
            });
            var _min_y = _.min(_scaled_y_arr) - 0.1 * (_.max(_scaled_y_arr) - _.min(_scaled_y_arr));
            var _max_y = _.max(_scaled_y_arr) + 0.1 * (_.max(_scaled_y_arr) - _.min(_scaled_y_arr));
            elem.y.scale = d3.scale.linear()
                .domain([_min_y, _max_y])
                .range([520, 20]);

            elem.y.axis = d3.svg.axis()
                .scale(elem.y.scale)
                .orient("left");

            d3.select("#cc-plots-box").select(".y-axis").remove();
            var x_axis_right = 0;
            if (_.pluck(data.get_meta($('input[name=cc_plots_profile_order_opt]:checked').val()), "STABLE_ID").length < 8 ) {
                x_axis_right = (_.pluck(data.get_meta($('input[name=cc_plots_profile_order_opt]:checked').val()), "STABLE_ID").length * 100 + 160);
            } else {
                x_axis_right = 980;
            }
            elem.svg.append("g")
                .style("stroke-width", 1.5)
                .style("fill", "none")
                .style("stroke", "grey")
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(160, 0)")
                .attr("class", "y-axis")
                .call(elem.y.axis.ticks(5))
                .selectAll("text")
                .style("font-family", "sans-serif")
                .style("font-size", "12px")
                .style("stroke-width", 0.5)
                .style("stroke", "black")
                .style("fill", "black")
                .style("text-anchor", "end");
            elem.svg.append("g")
                .style("stroke-width", 1.5)
                .style("fill", "none")
                .style("stroke", "grey")
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(" + x_axis_right + ", 0)")
                .call(elem.y.axis.ticks(0));

            //y axis title
            d3.select("#cc-plots-box").select(".y-title").remove();
            elem.title = elem.svg.append("svg:g");
            elem.title.append("text")
                .attr("class", "y-title")
                .attr("transform", "rotate(-90)")
                .attr("x", -250)
                .attr("y", 70)
                .style("text-anchor", "middle")
                .style("font-weight","bold")
                .text($("#cc_plots_gene_list").val() + " expression -- RNA-Seq V2 (log2)");

            //move dots
            elem.dots.selectAll("path")
                .transition().duration(300)
                .attr("transform", function() {
                    var _y_val = (d3.select(this).attr("y_val") <= settings.log_scale.threshold_down)? Math.log(settings.log_scale.threshold_down)/Math.log(2):Math.log(d3.select(this).attr("y_val")) / Math.log(2);
                    var _y = elem.y.scale(_y_val);
                    $(this).attr("y_pos", _y);
                    return "translate(" + d3.select(this).attr("x_pos") + ", " + _y + ")";
                });

            //box plots
            var _data = _.filter(_.pluck(_input, "attributes"), function(item) {
                return item.value !== "NaN"
            });
            var _box_plots_datum = {
                    x_val: "",
                    y_val: []
                },
                _box_plots_data_arr = [];
            _.each(_.pluck(data.get_meta($('input[name=cc_plots_profile_order_opt]:checked').val()), "STABLE_ID"), function(_profile_id) {
                var _tmp_y_val_arr = [];
                _.each(_data, function(_data_item) {
                    if (_data_item.profileId === _profile_id) {
                        var _orig_val = parseFloat(_data_item.value), _scaled_val = 0;
                        if (_orig_val <= settings.log_scale.threshold_down) {
                            _scaled_val = Math.log(settings.log_scale.threshold_down) / Math.log(2);
                        } else if (_orig_val >= settings.log_scale.threshold_up) {
                            _scaled_val = Math.log(settings.log_scale.threshold_up) / Math.log(2);
                        } else {
                            _scaled_val = Math.log(_orig_val) / Math.log(2);
                        }
                        _tmp_y_val_arr.push(parseFloat(_scaled_val));
                    }
                });
                _tmp_y_val_arr.sort(function(a, b) { return (a - b); });
                var _datum = jQuery.extend(true, {}, _box_plots_datum);
                _datum.x_val = _profile_id;
                _datum.y_val = _tmp_y_val_arr;
                _box_plots_data_arr.push(_datum);
            });
            add_box_plots(_box_plots_data_arr);

        }

        function remove_log_scale(_input) {
            //y axis
            var _y_str_arr = _.filter(_.pluck(_.pluck(_input, "attributes"), "value"), function(item) { return item !== "NaN"});
            var _y_arr = _.map(_y_str_arr, function(item) {
                return parseInt(item, 10);
            });
            var _min_y = _.min(_y_arr) - 0.1 * (_.max(_y_arr) - _.min(_y_arr));
            var _max_y = _.max(_y_arr) + 0.1 * (_.max(_y_arr) - _.min(_y_arr));
            elem.y.scale = d3.scale.linear()
                .domain([_min_y, _max_y])
                .range([520, 20]);

            elem.y.axis = d3.svg.axis()
                .scale(elem.y.scale)
                .orient("left");

            d3.select("#cc-plots-box").select(".y-axis").remove();
            var x_axis_right = 0;
            if (_.pluck(data.get_meta($('input[name=cc_plots_profile_order_opt]:checked').val()), "STABLE_ID").length < 8) {
                x_axis_right = (_.pluck(data.get_meta($('input[name=cc_plots_profile_order_opt]:checked').val()), "STABLE_ID").length * 100 + 160);
            } else {
                x_axis_right = 980;
            }
            elem.svg.append("g")
                .style("stroke-width", 1.5)
                .style("fill", "none")
                .style("stroke", "grey")
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(160, 0)")
                .attr("class", "y-axis")
                .call(elem.y.axis.ticks(5))
                .selectAll("text")
                .style("font-family", "sans-serif")
                .style("font-size", "12px")
                .style("stroke-width", 0.5)
                .style("stroke", "black")
                .style("fill", "black")
                .style("text-anchor", "end");
            elem.svg.append("g")
                .style("stroke-width", 1.5)
                .style("fill", "none")
                .style("stroke", "grey")
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(" + x_axis_right + ", 0)")
                .call(elem.y.axis.ticks(0));

            //y axis title
            d3.select("#cc-plots-box").select(".y-title").remove();
            elem.title = elem.svg.append("svg:g");
            elem.title.append("text")
                .attr("class", "y-title")
                .attr("transform", "rotate(-90)")
                .attr("x", -250)
                .attr("y", 70)
                .style("text-anchor", "middle")
                .style("font-weight","bold")
                .text($("#cc_plots_gene_list").val() + " expression -- RNA-Seq V2");

            //move dots
            elem.dots.selectAll("path")
                .transition().duration(300)
                .attr("transform", function() {
                    var _y = elem.y.scale(d3.select(this).attr("y_val"));
                    $(this).attr("y_pos", _y);
                    return "translate(" + d3.select(this).attr("x_pos") + ", " + _y + ")";
                });

            //box plots
            var _data = _.filter(_.pluck(_input, "attributes"), function(item) {
                return item.value !== "NaN"
            });
            var _box_plots_datum = {
                    x_val: "",
                    y_val: []
                },
                _box_plots_data_arr = [];
            _.each(_.pluck(data.get_meta($('input[name=cc_plots_profile_order_opt]:checked').val()), "STABLE_ID"), function(_profile_id) {
                var _tmp_y_val_arr = [];
                _.each(_data, function(_data_item) {
                    if (_data_item.profileId === _profile_id) {
                        _tmp_y_val_arr.push(parseFloat(_data_item.value));
                    }
                });
                _tmp_y_val_arr.sort(function(a, b) { return (a - b); });
                var _datum = jQuery.extend(true, {}, _box_plots_datum);
                _datum.x_val = _profile_id;
                _datum.y_val = _tmp_y_val_arr;
                _box_plots_data_arr.push(_datum);
            });
            add_box_plots(_box_plots_data_arr);

        }

        function update_profile_order(_input) {
            //remove x axis
            d3.select("#cc-plots-box").select(".x-axis").remove();
            d3.select("#cc-plots-box").select(".x-axis-label").remove();
            //redraw x axis
            var x_axis_right = 0;
            if (_.pluck(data.get_meta($('input[name=cc_plots_profile_order_opt]:checked').val()), "STABLE_ID").length < 8) {
                x_axis_right = (_.pluck(data.get_meta($('input[name=cc_plots_profile_order_opt]:checked').val()), "STABLE_ID").length * 100 + 160);
            } else {
                x_axis_right = 980;
            }
            elem.x.scale = d3.scale.ordinal()
                .domain(_.pluck(data.get_meta($('input[name=cc_plots_profile_order_opt]:checked').val()), "STABLE_ID"))
                .rangeRoundBands([160, x_axis_right]);
            elem.x.axis = d3.svg.axis()
                .scale(elem.x.scale)
                .orient("bottom");
            elem.svg.append("g")
                .style("stroke-width", 1.5)
                .style("fill", "none")
                .style("stroke", "grey")
                .style("shape-rendering", "crispEdges")
                .attr("class", "x-axis")
                .attr("transform", "translate(0, 520)")
                .call(elem.x.axis.ticks(data.get_meta($('input[name=cc_plots_profile_order_opt]:checked').val()).length))
                .selectAll("text")
                .data(data.get_meta($('input[name=cc_plots_profile_order_opt]:checked').val()))
                .attr("class", "x-axis-label")
                .style("font-family", "sans-serif")
                .style("font-size", "12px")
                .style("stroke-width", 0.5)
                .style("stroke", "black")
                .style("fill", "black")
                .style("text-anchor", "end")
                .attr("transform", function() { return "rotate(-30)"; })
                .text(function(d) {
                    return d.CANCER_STUDY_SHORT_NAME;
                });
            elem.svg.append("g")
                .style("stroke-width", 1.5)
                .style("fill", "none")
                .style("stroke", "grey")
                .style("shape-rendering", "crispEdges")
                .attr("transform", "translate(0, 20)")
                .call(elem.x.axis.tickFormat("").ticks(0).tickSize(0));
            elem.svg.selectAll(".x-axis-label").each(function(d) {
                $(this).qtip(
                    {
                        content: {text: "<font size=2>" + d.CANCER_STUDY_NAME + "</font>" },
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                        show: {event: "mouseover"},
                        hide: {fixed:true, delay: 100, event: "mouseout"},
                        position: {my:'left bottom',at:'top right', viewport: $(window)}
                    }
                );
            });
            //shift dots
            elem.dots.selectAll("path")
                .transition().duration(300)
                .attr("transform", function() {
                    var _x = elem.x.scale(d3.select(this).attr("x_val")) + elem.x.scale.rangeBand() / 2 + _.random(elem.x.scale.rangeBand() / 5 * (-1), elem.x.scale.rangeBand()/5);
                    $(this).attr("x_pos", _x);
                    return "translate(" + _x + ", " + d3.select(this).attr("y_pos") + ")";
                });
            //box plots
            var _data = _.filter(_.pluck(_input, "attributes"), function(item) {
                return item.value !== "NaN"
            });
            var _box_plots_datum = {
                    x_val: "",
                    y_val: []
                },
                _box_plots_data_arr = [];
            _.each(_.pluck(data.get_meta($('input[name=cc_plots_profile_order_opt]:checked').val()), "STABLE_ID"), function(_profile_id) {
                var _tmp_y_val_arr = [];
                if (document.getElementById("cc_plots_log_scale").checked) {
                    _.each(_data, function(_data_item) {
                        if (_data_item.profileId === _profile_id) {
                            var _orig_val = parseFloat(_data_item.value), _scaled_val = 0;
                            if (_orig_val <= settings.log_scale.threshold_down) {
                                _scaled_val = Math.log(settings.log_scale.threshold_down) / Math.log(2);
                            } else if (_orig_val >= settings.log_scale.threshold_up) {
                                _scaled_val = Math.log(settings.log_scale.threshold_up) / Math.log(2);
                            } else {
                                _scaled_val = Math.log(_orig_val) / Math.log(2);
                            }
                            _tmp_y_val_arr.push(parseFloat(_scaled_val));
                        }
                    });
                } else {
                    _.each(_data, function(_data_item) {
                        if (_data_item.profileId === _profile_id) {
                            _tmp_y_val_arr.push(parseFloat(_data_item.value));
                        }
                    });
                }
                _tmp_y_val_arr.sort(function(a, b) { return (a - b); });
                var _datum = jQuery.extend(true, {}, _box_plots_datum);
                _datum.x_val = _profile_id;
                _datum.y_val = _tmp_y_val_arr;
                _box_plots_data_arr.push(_datum);
            });
            add_box_plots(_box_plots_data_arr);
        }

        function update_show_mut(_show) {
            if (_show) {
                elem.dots.selectAll("path")
                    .attr("d", d3.svg.symbol()
                        .size(20)
                        .type(function(d) {
                            return d3.select(this).attr("ori_shape");
                        }))
                    .attr("fill", function(d) {
                        return d3.select(this).attr("ori_fill");
                    })
                    .attr("stroke", function(d) {
                        return d3.select(this).attr("ori_stroke");
                    });
                d3.selectAll(".no-mutation-legend")
                    .attr("opacity", 1);
                d3.selectAll(".not-sequenced-legend")
                    .attr("opacity", 1);
                d3.selectAll(".other-legend")
                    .attr("opacity", 1);
            } else {
                elem.dots.selectAll("path")
                    .attr("d", d3.svg.symbol()
                        .size(20)
                        .type(function() {
                            return mutationStyle.getSymbol("non");
                        }))
                    .attr("fill", function(d) {
                        //if (d3.select(this).attr("ori_fill") !== "none") {
                            return mutationStyle.getFill("non");
                        //} else {
                        //    return d3.select(this).attr("ori_fill");
                        // }
                    })
                    .attr("stroke", mutationStyle.getStroke("non"));
                d3.selectAll(".no-mutation-legend")
                    .attr("opacity", 0);
                d3.selectAll(".not-sequenced-legend")
                    .attr("opacity", 0);
                d3.selectAll(".other-legend")
                    .attr("opacity", 0);
            }
        }

        function update_show_sequenced() {

            if (!$("#cc_plots_show_mut").is(':checked')) {
                d3.selectAll(".not-sequenced-legend")
                    .attr("opacity", 0);
                d3.selectAll(".no-mutation-legend")
                    .attr("opacity", 0);
                d3.selectAll(".other-legend")
                    .attr("opacity", 0);
                if ($("#cc_plots_show_sequenced_only").is(':checked')) {
                    d3.selectAll(".not-sequenced-sample")
                        .attr("opacity", 0);
                } else {
                    d3.selectAll(".not-sequenced-sample")
                        .attr("opacity", 1);
                }
            } else {
                if ($("#cc_plots_show_sequenced_only").is(':checked')) {
                    d3.selectAll(".not-sequenced-sample")
                        .attr("opacity", 0);
                    d3.selectAll(".not-sequenced-legend")
                        .attr("opacity", 0);
                } else {
                    d3.selectAll(".not-sequenced-sample")
                        .attr("opacity", 1);
                    d3.selectAll(".not-sequenced-legend")
                        .attr("opacity", 1);
                }
            }
        }

        return {
            init: function () {
                init_sidebar();
                init_canvas();
                data.get($("#cc_plots_gene_list").val(), init_box);
            },
            update_gene: function() {
                init_canvas();
                data.get($("#cc_plots_gene_list").val(), init_box);
            },
            apply_log_scale: apply_log_scale,
            remove_log_scale: remove_log_scale,
            init_sidebar: init_sidebar,
            init_box: init_box,
            update_profile_order: update_profile_order,
            update_show_mut: update_show_mut,
            update_show_sequenced: update_show_sequenced
        }
    }()); //close view

    var search_mutation = function() {
        var searchToken = document.getElementById("mutation_search_keyword").value;
        d3.select("#cc-plots-box").selectAll("path").each(
            function() {
                var mutation_details = $(this).attr("mutation");
                if (typeof mutation_details !== 'undefined' && mutation_details !== false && mutation_details !== "domain") {
                    if ( searchToken.length >= 3 ) {
                        if (mutation_details.toUpperCase().indexOf(searchToken.toUpperCase()) !== -1) {
                            $(this).attr("d", d3.svg.symbol()
                                .size(d3.select(this).attr("size") + 5)
                                .type(d3.select(this).attr("ori_shape")));
                        } else {
                            $(this).attr("d", d3.svg.symbol()
                                .size(d3.select(this).attr("size"))
                                .type(d3.select(this).attr("ori_shape")));
                        }
                    } else {
                        $(this).attr("d", d3.svg.symbol()
                            .size(d3.select(this).attr("size"))
                            .type(d3.select(this).attr("ori_shape")));
                    }
                }
            }
        );
    };

    var search_case_id = function() {
        var searchToken = document.getElementById("case_id_search_keyword").value;
        d3.select("#cc-plots-box").selectAll("path").each(
            function() {
                var _case_id = $(this).attr("case_id");
                if (typeof _case_id !== 'undefined' && _case_id !== false && _case_id !== "domain") {
                    if ( searchToken.length >= 4 ) {
                        if ( _case_id.toUpperCase().indexOf(searchToken.toUpperCase()) !== -1 &&
                            (searchToken.toUpperCase()) !== "TCGA" && (searchToken.toUpperCase()) !== "TCGA-") {
                            $(this).attr("d", d3.svg.symbol()
                                .size(d3.select(this).attr("size") + 5)
                                .type(d3.select(this).attr("ori_shape")));
                        } else {
                            $(this).attr("d", d3.svg.symbol()
                                .size(d3.select(this).attr("size"))
                                .type(d3.select(this).attr("ori_shape")));
                        }
                    } else {
                        $(this).attr("d", d3.svg.symbol()
                            .size(d3.select(this).attr("size"))
                            .type(d3.select(this).attr("ori_shape")));
                    }
                }
            }
        );
    };

    function get_tab_delimited_data() {
        var result_str = "";
        result_str += "Sample Id" + "\t" + "Cancer Study" + "\t" + "Profile Name" + "\t" + "Mutation" + "\t" + "Value" + "\n";
        var assemble = function(result) {
            _.each(_.pluck(result, "attributes"), function(item) {
                if (data.is_sequenced(item.profileId, item.caseId)) {
                    if (item.mutation === "non" || item.mutation === "") {
                        result_str += item.caseId + "\t" + data.get_cancer_study_name(item.profileId) + "\t" +
                            data.get_profile_name(item.profileId) + "\t" + "Not Mutated" + "\t" + item.value + "\n";
                    } else {
                        result_str += item.caseId + "\t" + data.get_cancer_study_name(item.profileId) + "\t" +
                            data.get_profile_name(item.profileId) + "\t" + item.mutation + "\t" + item.value + "\n";
                    }
                } else {
                    result_str += item.caseId + "\t" + data.get_cancer_study_name(item.profileId) + "\t" +
                        data.get_profile_name(item.profileId) + "\t" + "Not Sequenced" + "\t" + item.value + "\n";
                }

            });
        }
        data.get($("#cc_plots_gene_list").val(), assemble);
        return result_str;
    }

    return {
        init: function () {
            var cc_plots_time_out = setInterval(function () {
                cc_plots_timer();
            }, 1000);
            function cc_plots_timer() {
                if (window.PortalMetaData !== undefined) {
                    clearInterval(cc_plots_time_out);
                    data.init(view.init);
                }
            }
        },
        update_gene: function() {
            d3.select("#cc-plots-box").select("svg").remove();
            $("#cc-plots-box").append("<img src='images/ajax-loader.gif' id='cc_plots_loading' style='padding:200px;'/>");
            view.update_gene();
        },
        search_mutation: search_mutation,
        search_case_id: search_case_id,
        get_tab_delimited_data: get_tab_delimited_data,
        toggle_log_scale: function() {
            if (document.getElementById("cc_plots_log_scale").checked) {
                data.get($("#cc_plots_gene_list").val(), view.apply_log_scale);
            } else {
                data.get($("#cc_plots_gene_list").val(), view.remove_log_scale);
            }
        },
        update_profile_order: function() {
            data.get($("#cc_plots_gene_list").val(), view.update_profile_order);
        },
        toggle_show_mut: function() {
            view.update_show_mut($("#cc_plots_show_mut").is(':checked'));
        },
        toggle_show_sequenced: function() {
            view.update_show_sequenced();
        }
    }

}(window.jQuery, window._, window.Backbone, window.d3));
