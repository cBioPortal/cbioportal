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

    var study_meta = [], profile_data = {};

    var gene = [], apply_log_scale = false, study_order, show_mutations = false;

    var threshold_down = 0.17677669529,  //-2.5 to 10
        threshold_up = 1.2676506e+30,
        jitter_value = 0.4;
    
    var fetchProfileData_ = function(_queriedStudyIds) {
        
        var _def = new $.Deferred();
        
        $.when(ccPlots.util.getGeneticProfiles_(_queriedStudyIds)).then(function(_profiles) {
            
            var _param_mrna_profile_arr = _.reduce(_queriedStudyIds, function (memo, _sid) {
                if ($("#cc_plots_profile_list").val() === 'v1') {
                    memo.push( _sid + "_rna_seq_mrna" );
                    //return _sid + ""
                } else if ($("#cc_plots_profile_list").val() === 'v2') {
                    //return _sid + "_rna_seq_v2_mrna";
                    memo.push(_sid + "_rna_seq_v2_mrna");
                    memo.push(_sid + "_rna_seq_v2_mrna_median");
                    //return _sid + "_rna_seq_v2_mrna_median";
                }
                return memo;
            },[]);
            var _param_mut_profile_arr = _.map(_queriedStudyIds, function (_sid) {
                return _sid + "_mutations";
            });
            
            var _get_genetic_profile_params = {
                genes: gene,
                genetic_profile_ids: _.intersection(_param_mrna_profile_arr.concat(_param_mut_profile_arr), _.pluck(_profiles, "id"))
            };
            window.cbioportal_client.getGeneticProfileDataBySample(_get_genetic_profile_params).then(
                function (_result) {

                    profile_data = _result;

                    //calculate median profile value for each study
                    var _study_id_median_val_objs = [];
                    var _profile_data_objs = _.filter(profile_data, function (_obj) {
                        // filter out mutation data
                        return !(_obj.hasOwnProperty("mutation_status")); 
                    })
                    var _study_groups = _.groupBy(_profile_data_objs, "study_id");
                    _.each(_study_groups, function (_study_group) {
                        var _vals = _.filter(_.pluck(_study_group, "profile_data"), function (_val) {
                            return _val !== "NaN";
                        });
                        _vals = _.map(_vals, function (_val) {
                            return parseFloat(_val);
                        });
                        var _median_val = findMedian(_vals);
                        _study_id_median_val_objs.push({study_id: _study_group[0].study_id, median_val: _median_val});
                    });
                    function findMedian(_input_data) {
                        var m = _input_data.map(function (v) {
                            return v;
                        }).sort(function (a, b) {
                            return a - b;
                        });
                        var middle = Math.floor((m.length - 1) / 2); // NB: operator precedence
                        if (m.length % 2) {
                            return m[middle];
                        } else {
                            return (m[middle] + m[middle + 1]) / 2.0;
                        }
                    }

                    // get study meta data
                    var _get_study_params = {
                        study_ids: _.uniq(_.pluck(_profile_data_objs, "study_id"))
                    };
                    window.cbioportal_client.getStudies(_get_study_params).then(
                        function (_study_meta) {

                            var _sids = [];
                            study_meta = _study_meta;
                            
                            //sort by study short name or median
                            if (study_order === "median") {
                                _study_id_median_val_objs = _.sortBy(_study_id_median_val_objs, "median_val");
                                _sids = _.pluck(_study_id_median_val_objs, "study_id");
                                study_meta = _.sortBy(study_meta, function (_meta_obj) {
                                    return _sids.indexOf(_meta_obj.id);
                                });
                            } else {
                                _sids = _.uniq(_.pluck(_.sortBy(profile_data, "study_short_name"), "study_id"));
                                study_meta = _.sortBy(study_meta, "short_name");
                            }

                            //map study full name to each sample
                            _.each(_.filter(profile_data, function (_obj) {
                                return !(_obj.hasOwnProperty("mutation_status"));
                            }), function (_profile_data_obj) {
                                _.each(study_meta, function (_study_meta_obj) {
                                    if (_study_meta_obj.id === _profile_data_obj.study_id) {
                                        _profile_data_obj.study_name = _study_meta_obj.name;
                                        _profile_data_obj.study_description = _study_meta_obj.description;
                                        _profile_data_obj.study_short_name = _study_meta_obj.short_name;
                                    }
                                });
                            });

                            //get sequenced sample lists
                            var _sample_list_ids = _.map(_sids, function (_study_id) {
                                return _study_id + "_sequenced";
                            });
                            window.cbioportal_client.getSampleLists({sample_list_ids: _sample_list_ids}).then(function (_sequenced_sample_lists) {

                                //merge genomic profile data into mutation profile data for mutated samples
                                var _mut_data = _.filter(profile_data, function (_obj) {
                                    return _obj.hasOwnProperty("mutation_status");
                                });
                                var _tmp_profile_group = _.filter(profile_data, function (_obj) {
                                    return !(_obj.hasOwnProperty("mutation_status"));
                                }); //profile data only
                                _.each(_tmp_profile_group, function (_profile_obj) {
                                    var mutation_type = "non";
                                    var mutation_details = "";
                                    _.each(_mut_data, function (_mut_obj) {
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
                                var _non_mut_or_not_sequenced_group = _.filter(_tmp_profile_group, function (_obj) {
                                    return _obj.mutation_type === "non";
                                }); // samples w/o mut or not sequenced
                                var mixMutGrp_ = _.filter(_tmp_profile_group, function (_obj) {
                                    return _obj.mutation_type !== "non";
                                }); // samples with mutations

                                //calculate log values
                                _.map(_non_mut_or_not_sequenced_group, function (_non_mut_obj) {
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
                                _.map(mixMutGrp_, function (_mut_obj) {
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
                                _.each(_non_mut_study_groups, function (_non_mut_study_group) {
                                    //for studies that non of the samples are sequenced
                                    if ($.inArray(_non_mut_study_group[0].study_id, _.uniq(_.pluck(_sequenced_sample_lists, "study_id"))) === -1) {
                                        _.each(_non_mut_study_group, function (_non_mut_obj) {
                                            _non_mut_obj.sequenced = false;
                                        });
                                    }
                                    //for studies that part of the samples are sequenced
                                    _.each(_sequenced_sample_lists, function (_sequenced_sample_list) {
                                        if (_sequenced_sample_list.study_id === _non_mut_study_group[0].study_id) {
                                            var _sequenced_sample_ids = _sequenced_sample_list.sample_ids;
                                            _.each(_non_mut_study_group, function (_non_mut_obj) {
                                                if (_.contains(_sequenced_sample_ids, _non_mut_obj.sample_id)) {
                                                    _non_mut_obj.sequenced = true;
                                                } else {
                                                    _non_mut_obj.sequenced = false;
                                                }
                                            });
                                        }
                                    });
                                });
                                _.each(mixMutGrp_, function (_mut_obj) {
                                    _mut_obj.sequenced = true;
                                });
                                var nonMutGrp_ = _.filter(_non_mut_or_not_sequenced_group, function (_obj) {
                                    return _obj.sequenced === true;
                                });
                                var nonSeqGrp_ = _.filter(_non_mut_or_not_sequenced_group, function (_obj) {
                                    return _obj.sequenced === false;
                                });

                                _def.resolve([nonMutGrp_, mixMutGrp_, nonSeqGrp_]);

                            });
                        }
                    );
                }
            );
        });
        return _def.promise();
    }

    var renderPlots = function(_d) {

        // data for rendering
        var data4render = []; 

        // raw data
        var nonMutGrp_ = _d[0];
        var mixMutGrp_ = _d[1];
        var nonSeqGrp_ = _d[2];
        var formatted_data = nonMutGrp_.concat(mixMutGrp_, nonSeqGrp_);
        var studyIds_ = _.uniq(_.pluck(formatted_data, "study_id"));
        
        // ---- define tracks ----
        if (show_mutations) { //show mutations
            //not sequenced track
            var _qtips = []; //assemble array of qtip text
            _.each(nonSeqGrp_, function(_obj) {
                _qtips.push("Study: " +  _obj.study_name + "<br>" +"Sample Id: " + _obj.sample_id + "<br>" + "Expression: " + _obj.profile_data);
            });
            var _y = []; //assemble y axis values
            if (apply_log_scale) {
                _y = _.pluck(nonSeqGrp_, "logged_profile_data");
            } else {
                _y = _.pluck(nonSeqGrp_, "profile_data");
            }
            var not_sequenced_track = {
                x: _.map(_.pluck(nonSeqGrp_, "study_id"), function(_study_id){ return _.pluck(study_meta, "id").indexOf(_study_id) + Math.random() * jitter_value - jitter_value / 2; }),
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
                study_id: _.pluck(nonSeqGrp_, "study_id"),
                sample_id: _.pluck(nonSeqGrp_, "sample_id")
            };
            data4render.push(not_sequenced_track);

            // no mutation track
            var _qtips = []; //assemble array of qtip text
            _.each(nonMutGrp_, function(_non_mut_obj) {
                _qtips.push("Study: " +  _non_mut_obj.study_name + "<br>" +"Sample Id: " + _non_mut_obj.sample_id + "<br>" + "Expression: " + _non_mut_obj.profile_data);
            });
            var _y = []; //assemble y axis values
            if (apply_log_scale) {
                _y = _.pluck(nonMutGrp_, "logged_profile_data");
            } else {
                _y = _.pluck(nonMutGrp_, "profile_data");
            }
            var non_mut_track = {
                x: _.map(_.pluck(nonMutGrp_, "study_id"), function(_study_id){ return _.pluck(study_meta, "id").indexOf(_study_id) + Math.random() * jitter_value - jitter_value / 2; }),
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
                study_id: _.pluck(nonMutGrp_, "study_id"),
                sample_id: _.pluck(nonMutGrp_, "sample_id")
            };
            data4render.push(non_mut_track);

            //mutated tracks
            var _mut_types = _.uniq(_.map(_.uniq(_.pluck(mixMutGrp_, "mutation_type")), function(_ori_type) { return mutationTranslator(_ori_type); }));
            $.each(_mut_types, function(_index, _mut_type) {
                var _mut_group = _.filter(mixMutGrp_, function(_obj) { return mutationTranslator(_obj.mutation_type) === _mut_type; });
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
                    x: _.map(_.pluck(_mut_group, "study_id"), function(_study_id){ return _.pluck(study_meta, "id").indexOf(_study_id) + Math.random() * jitter_value - jitter_value / 2; }),
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
                data4render.push(_mut_track);
            });
        } else { //not showing mutations
            var _qtips = []; //assemble array of qtip text
            _.each(formatted_data, function(_obj) {
                _qtips.push("Study: " +  _obj.study_name + "<br>" +"Sample Id: " + _obj.sample_id + "<br>" + "Expression: " + _obj.profile_data);
            });
            var _y = []; //assemble y axis values
            if (apply_log_scale) {
                _y = _.pluck(formatted_data, "logged_profile_data");
            } else {
                _y = _.pluck(formatted_data, "profile_data");
            }

            var plain_track = {
                x: _.map(_.pluck(formatted_data, "study_id"), function(_study_id){ return _.pluck(study_meta, "id").indexOf(_study_id) + Math.random() * jitter_value - jitter_value / 2; }),
                y: _y,
                mode: 'markers',
                type: 'scatter',
                name: '',
                text: _qtips,
                marker: {
                    color: '#00AAF8',
                    size: 5,
                    line: {color: '#0089C6', width: 1.2}
                },
                hoverinfo: "text",
                study_id: _.pluck(formatted_data, "study_id"),
                sample_id: _.pluck(formatted_data, "sample_id")
            };
            data4render.push(plain_track);
        }

        //box plots
        var _profileIds = _.map(_.pluck(study_meta, "id"), function (_sid) {
            if ($("#cc_plots_profile_list").val() === 'v1') {
                return _sid + "_rna_seq_mrna";
            } else if ($("#cc_plots_profile_list").val() === 'v2') {
                return _sid + "_rna_seq_v2_mrna";
            }
        });
        _.each(_profileIds, function(_profile_id) {
            var _y = [];
            if (apply_log_scale) {
                _y = _.pluck(_.filter(formatted_data, function(_result_obj) { return _result_obj.genetic_profile_id === _profile_id; }), "logged_profile_data");
            } else {
                _y = _.pluck(_.filter(formatted_data, function(_result_obj) { return _result_obj.genetic_profile_id === _profile_id; }), "profile_data");
            }
           
            var _box = {
                y: _y,
                x0: _profileIds.indexOf(_profile_id),
                type: 'box',
                opacity: 1,
                marker: {
                    color: 'grey',
                    size: 1,
                    outlierwidth: 0,
                    outliercolor: 'white'
                },
                line: { width: 1, outliercolor: 'white'},
                boxpoints: 'outliers',
                showlegend: false,
                whiskerwidth: 1
            };
            data4render.push(_box);
        });

        // ---- define layout ----
        var vals = [];
        for (var i = 0 ; i < studyIds_.length; i++) {
            vals.push(i);
        }

        var layout = {
            hovermode:'closest',
            showlegend: show_mutations?true:false,
            margin: {
                t: 20,
                b: 200,
                l: 110
            },
            xaxis: {
                tickmode: "array",
                ticktext: _.pluck(study_meta, "short_name"),
                tickvals: vals,
                tickangle: 45,
                linecolor: "#A9A9A9",
                linewidth: 2,
                titlefont: {
                    color: "#000"
                },
                mirror: 'all'
            },
            yaxis: {
                title: apply_log_scale?gene + ' Expression --- RNA Seq V2 (log)':gene + " Expression --- RNA Seq V2",
                linecolor: "#A9A9A9",
                linewidth: 2,
                mirror: 'all'
            }
        };

        $("#cc_plots_box").empty();
        Plotly.newPlot('cc_plots_box', data4render, layout, {showLink: false});

        //link to sample view
        var ccPlotsElem = document.getElementById('cc_plots_box');
        ccPlotsElem.on('plotly_click', function(_d){
            var _pts_study_id = _d.points[0]._d.study_id[_d.points[0].pointNumber];
            var _pts_sample_id = _d.points[0]._d.sample_id[_d.points[0].pointNumber];
            window.open(cbio.util.getLinkToSampleView(_pts_study_id, _pts_sample_id));
        });
        
    }
    
    var renderStudySelBox = function() {
            // generate the content of the study selection expendable section
            $("#cc_plots_study_selection_btn").attr("data-toggle", "collapse");
            $("#cc_plots_study_selection_btn").removeClass("disabled");
            if($("#cc_plots_select_study_box").is(":empty")) {

                // html 
                $("#cc_plots_select_study_box").append("select <a href='#' id='cc_plots_select_tcga_provisional'>TCGA provisional</a> / <a href='#' id='cc_plots_select_all'>all</a> / <a href='#' id='cc_plots_select_none'>none</a><br><br>");
                var _fg = false; // no studies is selected in the box
                
                var onlyPanCan = _.every(study_meta,(study)=>/pan_can_atlas/.test(study.id));
               
                _.each(study_meta, function(_study_meta_obj) {
                    var _checked = ''; //by default select only TCGA provisional studies
                    // if we don't only have pan can, then we're going to default all provisional studies
                    if (!onlyPanCan &&_study_meta_obj.name.toLowerCase().indexOf("tcga") !== -1 && _study_meta_obj.name.toLowerCase().indexOf("provisional") !== -1) {
                        _checked = 'checked';
                        _fg = true;
                    }
                    // if we're only pan can, turn all pan on
                    if (onlyPanCan) {
                        _checked = "checked";
                        _fg = true;
                    }
                    $("#cc_plots_select_study_box").append("<input type='checkbox' id='cc_plots_" + _study_meta_obj.id + "_sel' name='cc_plots_selected_studies' value='" + _study_meta_obj.id + "' title='Select "+_study_meta_obj.name+"' " + _checked + ">" + _study_meta_obj.name + "<br>");
                });
                if (!_fg) $("#cc_plots_select_study_collapse").collapse('show');

                // event listener
                $("#cc_plots_select_tcga_provisional").click(function() {
                    _.each(document.getElementsByName("cc_plots_selected_studies"), function(elem) {
                        if (elem.id.toLowerCase().indexOf("tcga") !== -1 && elem.title.toLowerCase().indexOf("provisional") !== -1) {
                            elem.checked = true;
                        } else elem.checked = false;
                    });
                    ccPlots.update();
                });
                $("#cc_plots_select_all").click(function() {
                    _.each(document.getElementsByName("cc_plots_selected_studies"), function(elem) { elem.checked = true; });
                    ccPlots.update();
                });
                $("#cc_plots_select_none").click(function() {
                    _.each(document.getElementsByName("cc_plots_selected_studies"), function(elem) { elem.checked = false; });
                    ccPlots.update();
                });
                $("input[name='cc_plots_selected_studies']").change(function() {
                    ccPlots.update();
                });

                // exclude certain studies
                var _tmp_study_obj = _.filter(study_meta, function(obj) { return obj.id === 'esca_tcga'; })[0];
                if (_tmp_study_obj !== undefined) {
                    document.getElementById("cc_plots_" + _tmp_study_obj.id + "_sel").checked = false;
                }
                _tmp_study_obj = _.filter(study_meta, function(obj) { return obj.id === 'stad_tcga'; })[0];
                if (_tmp_study_obj !== undefined) {
                    document.getElementById("cc_plots_" + _tmp_study_obj.id + "_sel").checked = false;
                }

            }
    }

    return {
        init: function() {
            //var tmp = setInterval(function () {timer();}, 1000);
            function timer() {
                if (window.QuerySession.cancer_study_ids.length > 0) {

                    return;
                    
                    clearInterval(tmp);

                    $.when(ccPlots.util.getGeneticProfiles_(window.QuerySession.cancer_study_ids)).then(function(_profiles) {

                        // if no v2 profile (data) available, set profile selection to v1
                        if($("#cc_plots_profile_list option").length === 0) {
                            if (_.some(_.pluck(_profiles, "id"), function(_pid) { return _pid.indexOf("rna_seq_v2_mrna") !== -1; })) 
                                $("#cc_plots_profile_list").append("<option value='v2'>RNA Seq V2</option><option value='v1'>RNA Seq</option>");
                            else $("#cc_plots_profile_list").append("<option value='v1'>RNA Seq</option><option value='v2'>RNA Seq V2</option>");                            
                        }
                            
                        // default menu settings
                        document.getElementById("cc_plots_gene_list").disabled = false;
                        gene.length = 0;
                        gene.push($("#cc_plots_gene_list").val());
                        apply_log_scale = document.getElementById("cc_plots_log_scale").checked;
                        show_mutations = document.getElementById("cc_plots_show_mutations").checked;
                        study_order = $('input[name=cc_plots_study_order_opt]:checked').val();
                        $("#cc_plots_select_study_box").empty();

                        // data fetching
                        $.when(fetchProfileData_(window.QuerySession.cancer_study_ids)).then(function(_d) {

                            // -- init download buttons --
                            $("#cc_plots_svg_download").unbind();
                            $("#cc_plots_pdf_download").unbind();
                            $("#cc_plots_data_download").unbind();
                            $("#cc_plots_svg_download").click(function() {
                                var xmlSerializer = new XMLSerializer();
                                var main_plots_str = xmlSerializer.serializeToString($("#cc_plots_box svg")[0]);
                                main_plots_str = main_plots_str.substring(0, main_plots_str.length - 6);
                                var legend_str = xmlSerializer.serializeToString($("#cc_plots_box svg")[2]);
                                legend_str = legend_str.substring(legend_str.indexOf(">") + 1, legend_str.length);
                                cbio.download.clientSideDownload([main_plots_str + legend_str], "cross-cancer-plots-download.svg", "application/svg+xml");
                            });
                            $("#cc_plots_pdf_download").click(function() {
                                var xmlSerializer = new XMLSerializer();
                                var main_plots_str = xmlSerializer.serializeToString($("#cc_plots_box svg")[0]);
                                main_plots_str = main_plots_str.substring(0, main_plots_str.length - 6);
                                var legend_str = xmlSerializer.serializeToString($("#cc_plots_box svg")[2]);
                                legend_str = legend_str.substring(legend_str.indexOf(">") + 1, legend_str.length);
                                var final_pdf_str = main_plots_str + legend_str;

                                final_pdf_str = final_pdf_str.replace(/"/g, "'");
                                final_pdf_str = final_pdf_str.replace("xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'", "");
                                final_pdf_str = final_pdf_str.replace(/text-anchor='end'/g, "");
                                final_pdf_str = final_pdf_str.replace(/text-anchor='start'/g, "");
                                final_pdf_str = final_pdf_str.replace(/text-anchor='middle'/g, "");
                                final_pdf_str = final_pdf_str.replace(/text-anchor: start;/g, "");
                                final_pdf_str = final_pdf_str.replace(/font-family: 'Open Sans',/g, "");
                                final_pdf_str = final_pdf_str.replace(/fill: transparent;/g, "fill-opacity: 0;");
                                final_pdf_str = final_pdf_str.replace(/'/g, "\"");

                                var downloadOptions = {
                                    filename: "cross-cancer-plots.pdf",
                                    contentType: "application/pdf",
                                    servletName: "svgtopdf.do"
                                };
                                cbio.download.initDownload(final_pdf_str, downloadOptions);

                            });
                            $("#cc_plots_data_download").click(function() {
                                var _profileTxt = "";
                                if ($("#cc_plots_profile_list").val() === 'v1') {
                                    _profileTxt = "RNA Seq";
                                } else if ($("#cc_plots_profile_list").val() === 'v2') {
                                    _profileTxt = "RNA Seq V2";
                                }
                                var get_tab_delimited_data = function() {
                                    var result_str = "Sample Id" + "\t" + "Cancer Study" + "\t" + "Profile Name" + "\t" + "Gene" + "\t" + "Mutation" + "\t" + "Value" + "\n";
                                    _.each(_d[0].concat(_d[1], _d[2]), function(_obj) {
                                        if ( _obj.sequenced) {
                                            if (_obj.mutation_type === "non" ) {
                                                result_str += _obj.sample_id + "\t" + _obj.study_name + "\t" + _profileTxt + "\t" + gene[0] + "\t" + "Not Mutated" + "\t" + _obj.profile_data + "\n";
                                            } else {
                                                result_str += _obj.sample_id + "\t" + _obj.study_name + "\t" + _profileTxt + "\t" + gene[0] + "\t" + _obj.mutation_details + "\t" + _obj.profile_data + "\n";
                                            }
                                        } else {
                                            result_str += _obj.sample_id + "\t" + _obj.study_name + "\t" + _profileTxt + "\t" + gene[0] + "\t" + "Not Sequenced" + "\t" + _obj.profile_data + "\n";
                                        }
                                    });
                                    return result_str;
                                };
                                cbio.download.clientSideDownload([get_tab_delimited_data()], "plots-data.txt");
                            });

                            if (study_meta.length !== 0) {
                                renderStudySelBox();
                                renderPlots(_d);
                                ccPlots.update(); //update in accordance to initial selection                        
                            } else {
                                $("#cc_plots_select_study_collapse").collapse('hide');
                                $("#cc_plots_study_selection_btn").addClass("disabled");
                                $("#cc_plots_box").empty();
                                $("#cc_plots_box").append("<div style='color:grey;padding:150px;'>No profile data available.</div>");
                            }
                        });
                    });
                }
            }
        },
        update: function() {
            
            // menu selection status
            gene = [];
            gene.length = 0;
            gene.push($("#cc_plots_gene_list").val());
            study_order = $('input[name=cc_plots_study_order_opt]:checked').val();
            apply_log_scale = document.getElementById("cc_plots_log_scale").checked;
            show_mutations = document.getElementById("cc_plots_show_mutations").checked;
            $("#cc_plots_box").empty();
            $("#cc_plots_box").append("<img src='images/ajax-loader.gif' id='cc_plots_loading' style='padding:250px;' alt='loading' />");
            var _selSids = $("input[name=cc_plots_selected_studies]:checked").map(function() { return this.value; }).get();
            
            // re-generate the view
            $.when(fetchProfileData_(_selSids)).then(function(_d) {
                renderPlots(_d); // render main plots
            });
        },
        include_all: function() {
            $("#cc_plots_study_selection_btn").click();
        }, util: {}
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

ccPlots.util.getGeneticProfiles_ = function(_sids) {
    var def_ = new $.Deferred();
    var fetchPromises_ = [];
    var profiles_ = [];
    fetchPromises_ = fetchPromises_.concat(
        _sids.map(function(_sid) {
            var _def = new $.Deferred();
            window.cbioportal_client.getGeneticProfiles({study_id: [_sid]}).then(function(_ret) {
                profiles_ = profiles_.concat(_ret);
                _def.resolve();
            }, function() { _def.resolve([]); });
            return _def.promise();
        })
    );
    $.when.apply($, fetchPromises_).done(function() {
        def_.resolve(profiles_);
    });
    return def_.promise();
}

