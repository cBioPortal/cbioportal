/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Created by suny1 on 11/18/15.
 */

var ccPlots = (function (Plotly, _, $) {

    var study_ids = [], study_meta = [], mrna_profiles = [], profile_data = {};

    var data = []; //for rendering

    var gene = [], apply_log_scale = false, study_order;

    var threshold_down = 0.17677669529,  //-2.5 to 10
        threshold_up = 1.2676506e+30;

    var fetch_profile_data = function(_queried_study_ids) {

        var _param_mrna_profile_arr = _.map(_queried_study_ids, function(_study_id) { return _study_id + "_rna_seq_v2_mrna"; });
        var _param_mut_profile_arr = _.map(_queried_study_ids, function(_study_id) { return _study_id + "_mutations"; });
        var _get_genetic_profile_params = {
            genes: gene,
            genetic_profile_ids: _param_mrna_profile_arr.concat(_param_mut_profile_arr)
        };

        window.cbioportal_client.getGeneticProfileDataBySample(_get_genetic_profile_params).then(
            function(_result) {

                profile_data = _result;

                //calculate median profile value for each study
                var _study_id_median_val_objs = [];
                var _profile_data_objs = _.filter(_result, function(_obj) { return !(_obj.hasOwnProperty("mutation_status")); })
                var _study_groups = _.groupBy(_profile_data_objs, "study_id");
                _.each(_study_groups, function(_study_group) {
                    var _vals = _.filter(_.pluck(_study_group, "profile_data"), function(_val) { return _val !== "NaN"; });
                    _vals = _.map(_vals, function(_val) { return parseFloat(_val); });
                    var _median_val = findMedian(_vals);
                    _study_id_median_val_objs.push({study_id: _study_group[0].study_id, median_val: _median_val});
                });
                function findMedian(_input_data) {
                    var m = _input_data.map(function(v) {
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

                var _get_study_params = {
                    study_ids : _.uniq(_.pluck(_profile_data_objs, "study_id"))
                };
                window.cbioportal_client.getStudies(_get_study_params).then(
                    function(_study_meta) {
                        study_meta = _study_meta;

                        //map study full name to each sample
                        _.each(_.filter(profile_data, function(_obj) { return !(_obj.hasOwnProperty("mutation_status")); }), function(_profile_data_obj) {
                            _.each(study_meta, function(_study_meta_obj) {
                                if(_study_meta_obj.id === _profile_data_obj.study_id) {
                                    _profile_data_obj.study_name = _study_meta_obj.name;
                                    _profile_data_obj.study_description = _study_meta_obj.description;
                                    _profile_data_obj.study_short_name = _study_meta_obj.short_name;
                                }
                            });
                        });

                        //sort by study short name or median
                        if (study_order === "median") {
                            _study_id_median_val_objs = _.sortBy(_study_id_median_val_objs, "median_val");
                            study_ids = _.pluck(_study_id_median_val_objs, "study_id");
                            study_meta = _.sortBy(study_meta, function(_meta_obj){ return study_ids.indexOf(_meta_obj.id); });
                        } else {
                            study_ids = _.uniq(_.pluck(_.sortBy(profile_data, "study_short_name"), "study_id"));
                            study_meta = _.sortBy(study_meta, "short_name");
                        }

                        //TODO: apply legit ways to extract profiles, now it's a hack, assuming every study has ONE rna seq v2 profile, and named under the SAME convention
                        mrna_profiles =  _.map(study_ids, function(_study_id) { return _study_id + "_rna_seq_v2_mrna"});

                        //get sequenced sample lists
                        var _sample_list_ids = _.map(study_ids, function(_study_id) { return _study_id + "_sequenced"; });
                        window.cbioportal_client.getSampleLists({sample_list_ids: _sample_list_ids}).then(function(_sequenced_sample_lists) {

                            //merge genomic profile data into mutation profile data for mutated samples
                            var _mut_data = _.filter(profile_data, function(_obj) { return _obj.hasOwnProperty("mutation_status"); });
                            var _tmp_profile_group = _.filter(profile_data, function(_obj) { return !(_obj.hasOwnProperty("mutation_status")); }); //profile data only
                            _.each(_tmp_profile_group, function(_profile_obj) {
                                var mutation_type = "non";
                                var mutation_details = "";
                                _.each(_mut_data, function(_mut_obj) {
                                    if (_profile_obj.study_id === _mut_obj.study_id &&
                                        _profile_obj.sample_id === _mut_obj.sample_id) {
                                        mutation_type = _mut_obj.mutation_type; //TODO: set a priority list for mutation type
                                        mutation_details += _mut_obj.amino_acid_change + ", ";
                                    }
                                });
                                _profile_obj.mutation_type = mutation_type;
                                _profile_obj.mutation_details = mutation_details.substring(0, mutation_details.length - 2);
                            });

                            //separate groups
                            var _non_mut_or_not_sequenced_group = _.filter(_tmp_profile_group, function(_obj) { return _obj.mutation_type === "non"; });
                            var _mix_mut_group = _.filter(_tmp_profile_group, function(_obj) { return _obj.mutation_type !== "non"; });

                            //calculate log values
                            _.map(_non_mut_or_not_sequenced_group, function(_non_mut_obj){
                                var _ori_val = _non_mut_obj.profile_data;
                                if (_ori_val <= threshold_down) {
                                    _non_mut_obj.logged_profile_data = Math.log(threshold_down) / Math.log(2);
                                } else if (_ori_val >= threshold_up) {
                                    _non_mut_obj.logged_profile_data = Math.log(threshold_up) / Math.log(2);
                                } else {
                                    _non_mut_obj.logged_profile_data = Math.log(_ori_val) / Math.log(2);
                                }
                                return _non_mut_obj;
                            });
                            _.map(_mix_mut_group, function(_mut_obj){
                                var _ori_val = _mut_obj.profile_data;
                                if (_ori_val <= threshold_down) {
                                    _mut_obj.logged_profile_data = Math.log(threshold_down) / Math.log(2);
                                } else if (_ori_val >= threshold_up) {
                                    _mut_obj.logged_profile_data = Math.log(threshold_up) / Math.log(2);
                                } else {
                                    _mut_obj.logged_profile_data = Math.log(_ori_val) / Math.log(2);
                                }
                                return _mut_obj;
                            });

                            //mark sequenced/non-sequenced samples
                            var _non_mut_study_groups = _.groupBy(_non_mut_or_not_sequenced_group, "study_id"); //only samples without mutation can be possibly not sequenced, therefore skip the mix_mut_group
                            _.each(_non_mut_study_groups, function(_non_mut_study_group) {
                                _.each(_sequenced_sample_lists, function(_sequenced_sample_list) {
                                    if (_sequenced_sample_list.study_id === _non_mut_study_group[0].study_id) {
                                        var _sequenced_sample_ids = _sequenced_sample_list.sample_ids;
                                        _.each(_non_mut_study_group, function(_non_mut_obj) {
                                            if(_.contains(_sequenced_sample_ids, _non_mut_obj.sample_id)) {
                                                _non_mut_obj.sequenced = true;
                                            } else {
                                                _non_mut_obj.sequenced = false;
                                            }
                                        });
                                    }
                                });
                            });
                            _.each(_mix_mut_group, function(_mut_obj) {
                                _mut_obj.sequenced = true;
                            });
                            var _non_mut_group = _.filter(_non_mut_or_not_sequenced_group, function(_obj) { return _obj.sequenced === true; });
                            var _not_sequenced_group = _.filter(_non_mut_or_not_sequenced_group, function(_obj) { return _obj.sequenced === false; });

                            //exclude non provisional study
                            _non_mut_group = _.filter(_non_mut_group, function(_obj) { return _obj.study_id.indexOf("tcga_pub") === -1; });
                            _not_sequenced_group = _.filter(_not_sequenced_group, function(_obj) { return _obj.study_id.indexOf("tcga_pub") === -1; });
                            _mix_mut_group = _.filter(_mix_mut_group, function(_obj) { return _obj.study_id.indexOf("tcga_pub") === -1; });
                            study_meta = _.filter(study_meta, function(_obj) { return _obj.id.indexOf("tcga_pub") === -1; });
                            study_ids = _.filter(study_ids, function(study_id) { return study_id.indexOf("tcga_pub") === -1 });
                            mrna_profiles = _.filter(mrna_profiles, function(mrna_profile) { return mrna_profile.indexOf("tcga_pub") === -1; });

                            //exclude esophagus
                            var _tmp_target_study_obj = _.filter(study_meta, function(obj) { return obj.name.indexOf("Esophageal") !== -1 });
                            _non_mut_group = _.filter(_non_mut_group, function(_obj) { return _obj.study_name.indexOf("Esophageal") === -1; });
                            _not_sequenced_group = _.filter(_not_sequenced_group, function(_obj) { return _obj.study_name.indexOf("Esophageal") === -1; });
                            _mix_mut_group = _.filter(_mix_mut_group, function(_obj) { return _obj.study_name.indexOf("Esophageal") === -1; });
                            study_meta = _.filter(study_meta, function(_obj) { return _obj.name.indexOf("Esophageal") === -1; });
                            study_ids = _.filter(study_ids, function(study_id) { return study_id.indexOf(_tmp_target_study_obj[0].id) === -1; });
                            mrna_profiles = _.filter(mrna_profiles, function(mrna_profile) { return mrna_profile.indexOf(_tmp_target_study_obj[0].id) === -1; });

                            render(_non_mut_group, _not_sequenced_group, _mix_mut_group);

                        });

                    }
                );
            });

    }

    var render = function(_non_mut_group, _not_sequenced_group, _mix_mut_group) {

        // ---- clean up data array ----
        data = [];
        data.length = 0;

        // ---- define tracks ----

        //not sequenced track
        var _qtips = []; //assemble array of qtip text
        _.each(_not_sequenced_group, function(_obj) {
            _qtips.push("Study: " +  _obj.study_name + "<br>" +"Sample Id: " + _obj.sample_id + "<br>" + "Expression: " + _obj.profile_data);
        });
        var _y = []; //assemble y axis values
        if (apply_log_scale) {
            _y = _.pluck(_not_sequenced_group, "logged_profile_data");
        } else {
            _y = _.pluck(_not_sequenced_group, "profile_data");
        }
        var not_sequenced_track = {
            x: _.map(_.pluck(_not_sequenced_group, "study_id"), function(_study_id){ return study_ids.indexOf(_study_id) + Math.random() * 0.3 - 0.15; }),
            y: _y,
            mode: 'markers',
            type: 'scatter',
            name: 'Not Sequenced',
            text: _qtips,
            opacity: 0.6,
            marker: {
                size: 5,
                color: 'white',
                line: {color: 'grey', width: 1.2}
            },
            hoverinfo: "text",
            study_id: _.pluck(_not_sequenced_group, "study_id"),
            sample_id: _.pluck(_not_sequenced_group, "sample_id")
        };
        data.push(not_sequenced_track);

        // no mutation track
        var _qtips = []; //assemble array of qtip text
        _.each(_non_mut_group, function(_non_mut_obj) {
            _qtips.push("Study: " +  _non_mut_obj.study_name + "<br>" +"Sample Id: " + _non_mut_obj.sample_id + "<br>" + "Expression: " + _non_mut_obj.profile_data);
        });
        var _y = []; //assemble y axis values
        if (apply_log_scale) {
            _y = _.pluck(_non_mut_group, "logged_profile_data");
        } else {
            _y = _.pluck(_non_mut_group, "profile_data");
        }
        var non_mut_track = {
            x: _.map(_.pluck(_non_mut_group, "study_id"), function(_study_id){ return study_ids.indexOf(_study_id) + Math.random() * 0.3 - 0.15; }),
            y: _y,
            mode: 'markers',
            type: 'scatter',
            name: 'No Mutation',
            text: _qtips,
            marker: {
                color: '#00AAF8',
                size: 5,
                line: {color: '#0089C6', width: 1.2}
            },
            hoverinfo: "text",
            study_id: _.pluck(_non_mut_group, "study_id"),
            sample_id: _.pluck(_non_mut_group, "sample_id")
        };
        data.push(non_mut_track);

        //mutated tracks
        var _mut_types = _.uniq(_.map(_.uniq(_.pluck(_mix_mut_group, "mutation_type")), function(_ori_type) { return mutationTranslator(_ori_type); }));
        $.each(_mut_types, function(_index, _mut_type) {
            var _mut_group = _.filter(_mix_mut_group, function(_obj) { return mutationTranslator(_obj.mutation_type) === _mut_type; });
            //assemble array of qtip text
            var _qtips = [];
            _.each(_mut_group, function(_mut_obj) {
                _qtips.push("Study: " + _mut_obj.study_name + "<br>" + "Sample Id: " + _mut_obj.sample_id + "<br>" + "Expression: " + _mut_obj.profile_data + "<br>" + "Mutation Type: " + _mut_obj.mutation_type + "<br>" + "Mutation Details: " + _mut_obj.mutation_details);
            });
            var _y = [];
            if (apply_log_scale) {
                _y = _.pluck(_mut_group, "logged_profile_data");
            } else {
                _y = _.pluck(_mut_group, "profile_data");
            }
            var _mut_track = {
                x: _.map(_.pluck(_mut_group, "study_id"), function(_study_id){ return study_ids.indexOf(_study_id) + Math.random() * 0.3 - 0.15; }),
                y: _y,
                mode: 'markers',
                type: 'scatter',
                name: mutationStyle.getText(_mut_type),
                text: _qtips,
                marker: {
                    color: mutationStyle.getFill(_mut_type),
                    size: 6,
                    line: {color: mutationStyle.getStroke(_mut_type), width: 1.2},
                    symbol: mutationStyle.getSymbol(_mut_type)
                },
                hoverinfo: "text",
                study_id: _.pluck(_mut_group, "study_id"),
                sample_id: _.pluck(_mut_group, "sample_id")
            };
            data.push(_mut_track);
        });

        //box plots
        var _joint_profile_group = _not_sequenced_group.concat(_non_mut_group.concat(_mix_mut_group));
        _.each(mrna_profiles, function(_profile_id) {
            var _y = [];
            if (apply_log_scale) {
                _y = _.pluck(_.filter(_joint_profile_group, function(_result_obj) { return _result_obj.genetic_profile_id == _profile_id; }), "logged_profile_data");
            } else {
                _y = _.pluck(_.filter(_joint_profile_group, function(_result_obj) { return _result_obj.genetic_profile_id == _profile_id; }), "profile_data");
            }
            var _box = {
                y: _y,
                x0: mrna_profiles.indexOf(_profile_id),
                type: 'box',
                opacity: 0.7,
                marker: {
                    color: 'grey',
                    size: 7
                },
                line: { width: 1},
                boxpoints: false,
                showlegend: false
            };
            data.push(_box);
        });

        // ---- define layout ----
        var vals = [];
        for (var i = 0 ; i < mrna_profiles.length; i++) {
            vals.push(i);
        }
        var layout = {
            hovermode:'closest',
            margin: {
                t: 20,
                b: 200,
                l: 110
            },
            xaxis: {
                tickmode: "array",
                ticktext: _.pluck(study_meta, "short_name"),
                tickvals: vals,
                tickangle: 45
            },
            yaxis: {
                title: apply_log_scale?gene + ' Expression --- RNA Seq V2 (log)':gene + " Expression --- RNA Seq V2"
            }
        };

        $("#cc_plots_box").empty();
        Plotly.newPlot('cc_plots_box', data, layout, {showLink: false});

        //link to sample view
        var ccPlotsElem = document.getElementById('cc_plots_box');
        ccPlotsElem.on('plotly_click', function(data){
            var _pts_study_id = data.points[0].data.study_id[data.points[0].pointNumber];
            var _pts_sample_id = data.points[0].data.sample_id[data.points[0].pointNumber];
            window.open(cbio.util.getLinkToSampleView(_pts_study_id, _pts_sample_id));
        });

    }

    return {
        init: function() {
            var tmp = setInterval(function () {timer();}, 1000);
            function timer() {
                if (window.studies !== undefined) {
                    clearInterval(tmp);

                    //default settings
                    gene = [];
                    gene.length = 0;
                    gene.push($("#cc_plots_gene_list").val());
                    apply_log_scale = document.getElementById("cc_plots_log_scale").checked;
                    study_order = $('input[name=cc_plots_study_order_opt]:checked').val();

                    fetch_profile_data(_.pluck(_.pluck(window.studies.models, "attributes"), "studyId"));
                }
            }
        },
        update_gene: function() {
            gene = [];
            gene.length = 0;
            gene.push($("#cc_plots_gene_list").val());
            $("#cc_plots_box").empty();
            $("#cc_plots_box").append("<img src='images/ajax-loader.gif' id='cc_plots_loading' style='padding:200px;'/>");
            fetch_profile_data(_.pluck(_.pluck(window.studies.models, "attributes"), "studyId"));
        },
        update_study_order: function() {
            study_order = $('input[name=cc_plots_study_order_opt]:checked').val();
            $("#cc_plots_box").empty();
            $("#cc_plots_box").append("<img src='images/ajax-loader.gif' id='cc_plots_loading' style='padding:200px;'/>");
            fetch_profile_data(_.pluck(_.pluck(window.studies.models, "attributes"), "studyId"));
        },
        toggle_log_scale: function() {
            apply_log_scale = document.getElementById("cc_plots_log_scale").checked;
            $("#cc_plots_box").empty();
            $("#cc_plots_box").append("<img src='images/ajax-loader.gif' id='cc_plots_loading' style='padding:200px;'/>");
            fetch_profile_data(_.pluck(_.pluck(window.studies.models, "attributes"), "studyId"));
        }
    };

}(window.Plotly, window._, window.jQuery));

var mutationStyle = (function() {  //Key and "typeName" are always identical
    var styleSheet = [
        {
            typeName : "Frameshift",
            symbol : "triangle-down",
            fill : "#1C1C1C",
            stroke : "#B40404",
            legendText : "Frameshift"
        },
        {
            typeName: "Nonsense",
            symbol : "diamond",
            fill : "#1C1C1C",
            stroke : "#B40404",
            legendText : "Nonsense"
        },
        {
            typeName : "Splice",
            symbol : "triangle-up",
            fill : "#A4A4A4",
            stroke : "#B40404",
            legendText : "Splice"
        },
        {
            typeName : "In_frame",
            symbol : "square",
            fill : "#DF7401",
            stroke : "#B40404",
            legendText : "In_frame"
        },
        {
            typeName : "Nonstart",
            symbol : "cross",
            fill : "#DF7401",
            stroke : "#B40404",
            legendText : "Nonstart"
        },
        {
            typeName : "Nonstop",
            symbol : "triangle-up",
            fill : "#1C1C1C",
            stroke : "#B40404",
            legendText : "Nonstop"
        },
        {
            typeName : "Missense",
            symbol : "circle",
            fill : "#DF7401",
            stroke : "#B40404",
            legendText : "Missense"
        },
        {
            typeName: "Other",
            symbol: "square",
            fill : "#1C1C1C",
            stroke : "#B40404",
            legendText : "Other"
        },
        {
            typeName : "non",
            symbol : "circle",
            fill : "#00AAF8",
            stroke : "#0089C6",
            legendText : "Not mutated"
        },
        {
            typeName: "one_mut",
            symbol : "circle",
            fill : "#DBA901",
            stroke : "#886A08",
            legendText : "One Gene mutated"
        },
        {
            typeName : "both_mut",
            symbol : "circle",
            fill : "#FF0000",
            stroke : "#B40404",
            legendText : "Both mutated"
        },
        {
            typeName : "non_mut",
            symbol : "circle",
            fill : "#00AAF8",
            stroke : "#0089C6",
            legendText : "Neither mutated"
        },
        {
            typeName: "non_sequenced",
            symbol : "circle",
            fill : "white",
            stroke : "gray",
            legendText : "Not sequenced"
        }

    ];

    return {
        getSymbol: function(_typeName) {
            var _result = "";
            $.each(styleSheet, function(index, obj) {
                if (obj.typeName === _typeName) {
                    _result = obj.symbol;
                }
            });
            return _result;
        },
        getFill: function(_typeName) {
            var _result = "";
            $.each(styleSheet, function(index, obj) {
                if (obj.typeName === _typeName) {
                    _result = obj.fill;
                }
            });
            return _result;
        },
        getStroke: function(_typeName) {
            var _result = "";
            $.each(styleSheet, function(index, obj) {
                if (obj.typeName === _typeName) {
                    _result = obj.stroke;
                }
            });
            return _result;
        },
        getText: function(_typeName) {
            var _result = "";
            $.each(styleSheet, function(index, obj) {
                if (obj.typeName === _typeName) {
                    _result = obj.legendText;
                }
            });
            return _result;
        },
        getGlyph: function(_typeName) { //retrieve the whole object
            var _result = {};
            $.each(styleSheet, function(index, obj) {
                if (obj.typeName === _typeName) {
                    _result = obj;
                }
            });
            return _result;
        }
    };

}());

var mutationTranslator = function(mutationDetail) {

    vocabulary = {
        frameshift : {
            type : "Frameshift",
            vals: [
                "Frame_Shift_Del",
                "Frame_Shift_Ins",
                "frameshift insertion",
                "frameshift",
                "frameshift_insertion",
                "Frameshift deletion",
                "FRAMESHIFT_CODING"
            ]
        },
        nonsense : {
            type : "Nonsense",
            vals: ["Nonsense_Mutation", "Nonsense"]
        },
        splice : {
            type : "Splice",
            vals : [
                "Splice_Site",
                "Splice_Site_SNP",
                "splicing",
                "splice",
                "ESSENTIAL_SPLICE_SITE"
            ]
        },
        in_frame : {
            type : "In_frame",
            vals : [
                "In_Frame_Del",
                "In_Frame_Ins"
            ]
        },
        nonstart : {
            type : "Nonstart",
            vals : ["Translation_Start_Site"]
        },
        nonstop : {
            type : "Nonstop",
            vals : ["NonStop_Mutation"]
        },
        missense : {
            type : "Missense",
            vals : [
                "Missense_Mutation",
                "Missense"
            ]
        },
        other: {
            type : "Other",
            vals : [
                "COMPLEX_INDEL",
                "5'Flank",
                "Fusion",
                "vIII deletion",
                "Exon skipping",
                "exon14skip"
            ]
        }
    };

    for(var key in vocabulary) {
        if ($.inArray(mutationDetail, vocabulary[key].vals) !== -1) {
            return vocabulary[key].type;
        }
    }
    return vocabulary.other.type; //categorize all other mutations as other

};

