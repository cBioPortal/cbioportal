/**
 * Created by suny1 on 9/2/15.
 */

var ccPlots = (function ($, _, Backbone, d3) {

    var data = (function () {

        var retrieved_genes = []; //list of genes that already retrieved data and stored here

        var ProfileMeta = Backbone.Model.extend({
            defaults: {
                GENETIC_ALTERATION_TYPE: "",
                NAME: "",
                DESCRIPTION: "",
                STABLE_ID: "",
                CANCER_STUDY_STABLE_ID: "",
                CASE_SET_ID: "",
                CANCER_STUDY_NAME: ""
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

                //var tmp = setInterval(function () {timer();}, 1000);
                //function timer() {
                //    if (window.crossCancerMutationProxy !== undefined) {
                        //clearInterval(tmp);
                        //var _proxy = window.crossCancerMutationProxy;
                        //_proxy.getMutationData(genes[0], _mutation_callback);
                        //function _mutation_callback(_mutation_obj) {
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
                        //}
                  //  }
                //}
            }
        });

        var ProfileDataList = Backbone.Collection.extend({model: ProfileData});

        var profileMetaList = new ProfileMetaList();
        var profileDataList = new ProfileDataList();

        return {
            init: function (callback_func) {

                var tmp = setInterval(function () {timer();}, 1000);
                function timer() {
                    if (window.studies !== undefined) {
                        clearInterval(tmp);
                        //retrieve profiles meta
                        $.each(_.pluck(window.studies.models, "attributes"), function (_study_index, _study_obj) {
                            var profileMetaListTmp = new ProfileMetaListTmp(_study_obj.studyId);
                            profileMetaListTmp.fetch({
                                success: function (profileMetaListTmp) {
                                    _.each(_.pluck(profileMetaListTmp.models, "attributes"), function (_profile_obj) {
                                        _profile_obj.CANCER_STUDY_STABLE_ID = profileMetaListTmp.cancer_study_id;
                                        _profile_obj.CASE_SET_ID = _study_obj.caseSetId;
                                        _profile_obj.CANCER_STUDY_NAME = window.metaDataJson["cancer_studies"][_study_obj.studyId].name;
                                    });
                                    profileMetaList.add(profileMetaListTmp.models);
                                    if (_study_index + 1 === window.studies.length) { //reach the end of the iteration
                                        callback_func();
                                    }
                                }
                            });
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
                                var _proxy = window.crossCancerMutationProxy;
                                _proxy.getMutationData(_gene, _mutation_callback);
                                function _mutation_callback(_mut_obj_arr) {
                                    _.each(_mut_obj_arr, function(_mut_obj) {
                                        _.each(profileDataListTmp.models, function(_model) {
                                            if(_mut_obj.caseId === _model.attributes.caseId) {
                                                _model.attributes.mutation += _mut_obj.proteinChange + ";";
                                                _model.attributes.mutation_type += mutationTranslator(_mut_obj.mutationType) + ";";
                                            }
                                        });
                                    })
                                    profileDataList.add(profileDataListTmp.models);
                                    if (_profile_index + 1 === profileMetaList.length) {
                                        retrieved_genes.push(_gene);
                                        callback_func(_.filter(profileDataList.models, function (model) {
                                            return model.get("gene") === _gene;
                                        }));
                                    }
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
            get_meta: function() { return profileMetaList; }
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
                title: ""
            },
            init_sidebar = function () {
                $("#cc_plots_gene_list_select").append("<select id='cc_plots_gene_list'>");
                _.each(window.studies.gene_list.split(/\s+/), function (_gene) {
                    $("#cc_plots_gene_list").append(
                        "<option value='" + _gene + "'>" + _gene + "</option>");
                });
            },
            init_canvas = function() {
                elem.svg = d3.select("#cc-plots-box")
                    .append("svg")
                    .attr("width", _.pluck(_.pluck(data.get_meta().models, "attributes"), "STABLE_ID").length * 100 + 400)
                    .attr("height", 900);
            },
            init_box = function (_input) {

                $("#cc_plots_loading").hide();

                //data
                var _data = _.filter(_.pluck(_input, "attributes"), function(item) {
                    return item.value !== "NaN"
                }) ;
                _.each(_data, function(_data_item) {
                    _data_item.mutation = _.uniq(_data_item.mutation.split(";")).join(", ");
                    _data_item.mutation = _data_item.mutation.substring(0, _data_item.mutation.length - 2);
                    _data_item.mutation_type = _data_item.mutation_type.split(";")[0];
                    if (_data_item.mutation === "") { _data_item.mutation = "non"; }
                    if (_data_item.mutation_type === "" ) { _data_item.mutation_type = "non"; }
                });


                //x axis
                var x_axis_right = (_.pluck(_.pluck(data.get_meta().models, "attributes"), "STABLE_ID").length * 100 + 300);
                elem.x.scale = d3.scale.ordinal()
                    .domain(_.pluck(_.pluck(data.get_meta().models, "attributes"), "STABLE_ID"))
                    .rangeRoundBands([300, x_axis_right]);

                elem.x.axis = d3.svg.axis()
                    .scale(elem.x.scale)
                    .orient("bottom");

                elem.svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("class", "x axis")
                    .attr("transform", "translate(0, 520)")
                    .call(elem.x.axis.ticks(_.pluck(data.get_meta().models, "attributes").length))
                    .selectAll("text")
                    .data(_.pluck(data.get_meta().models, "attributes"))
                    .style("font-family", "sans-serif")
                    .style("font-size", "12px")
                    .style("stroke-width", 0.5)
                    .style("stroke", "black")
                    .style("fill", "black")
                    .style("text-anchor", "end")
                    .attr("transform", function() { return "rotate(-30)"; })
                    .text(function(d) { return d["CANCER_STUDY_NAME"]; });
                elem.svg.append("g")
                    .style("stroke-width", 1.5)
                    .style("fill", "none")
                    .style("stroke", "grey")
                    .style("shape-rendering", "crispEdges")
                    .attr("transform", "translate(0, 20)")
                    .call(elem.x.axis.tickFormat("").ticks(0).tickSize(0));

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
                    .attr("transform", "translate(300, 0)")
                    .attr("class", "y axis")
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
                    .attr("class", "y title")
                    .attr("transform", "rotate(-90)")
                    .attr("x", -250)
                    .attr("y", 220)
                    .style("text-anchor", "middle")
                    .style("font-weight","bold")
                    .text($("#cc_plots_gene_list").val() + " expression -- RNA-Seq V2");

                //draw dots
                elem.dots = elem.svg.append("svg:g");
                elem.dots.selectAll("path").remove();
                elem.dots.selectAll("path")
                    .data((_.filter(_data, function(_item) { return _item.mutation === "non"; })).concat(_.filter(_data, function(_item) { return _item.mutation !== "non"; })))
                    .enter()
                    .append("svg:path")
                    .attr("class", "dot")
                    .attr("d", d3.svg.symbol()
                        .size(20)
                        .type(function(d) {
                            $(this).attr("size", 20);
                            $(this).attr("shape", mutationStyle.getSymbol(d.mutation_type));
                            return mutationStyle.getSymbol(d.mutation_type);
                        }))
                    .attr("fill", function(d) {
                        return mutationStyle.getFill(d.mutation_type);
                    })
                    .attr("stroke", function(d) {
                        return mutationStyle.getStroke(d.mutation_type);
                    })
                    .attr("stroke-width", 1.2)
                    .attr("transform", function(d) {
                        var _x = elem.x.scale(d.profileId) + elem.x.scale.rangeBand() / 2 + _.random(elem.x.scale.rangeBand() / 3 * (-1), elem.x.scale.rangeBand()/3);
                        var _y = elem.y.scale(parseInt(d.value));
                        return "translate(" + _x + ", " + _y + ")";
                    });

                //add glyphs
                var _mutation_types = _.uniq(_.pluck(_.filter(_data, function(_item) { return _item.mutation !== "non"; }), "mutation_type"));
                _mutation_types.push("non");
                var _glyph_objs = [];
                _.each(_mutation_types, function(_type) {
                    var _tmp = {};
                    _tmp.symbol = mutationStyle.getSymbol(_type);
                    _tmp.fill = mutationStyle.getFill(_type);
                    _tmp.stroke = mutationStyle.getStroke(_type);
                    _tmp.text = _type;
                    _glyph_objs.push(_tmp);
                });
                var legend = elem.svg.selectAll(".legend")
                    .data(_glyph_objs)
                    .enter().append("g")
                    .attr("class", "legend")
                    .attr("transform", function(d, i) {
                        return "translate(" + (_.pluck(_.pluck(data.get_meta().models, "attributes"), "STABLE_ID").length * 100 + 310) + ", " + (25 + i * 15) + ")";
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
                                return $(this).attr("shape");
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
                                return $(this).attr("shape");
                            })
                    );
                };

                elem.dots.selectAll("path").on("mouseover", mouseOn);
                elem.dots.selectAll("path").on("mouseout", mouseOff);

            };

        return {
            init: function () {
                init_sidebar();
                init_canvas();
                data.get($("#cc_plots_gene_list").val(), init_box);
            },
            update: function() {
                init_canvas();
                data.get($("#cc_plots_gene_list").val(), init_box);
            },
            init_sidebar: init_sidebar,
            init_box: init_box
        }
    }());

    return {
        init: function () {
            data.init(view.init);
        },
        update: function() {
            view.update();
        }
    }

}(window.jQuery, window._, window.Backbone, window.d3));
