/**
 * Created by suny1 on 11/18/15.
 */
var PlotlyCCplots = (function (Plotly, _, $) {

    return {
        init: function() {

            //TODO: retrieve through API
            var study_arr = [
                "acc_tcga", "blca_tcga", "brca_tcga", "cesc_tcga", "chol_tcga", "coadread_tcga", "laml_tcga",
                "lgg_tcga", "gbm_tcga", "hnsc_tcga", "kich_tcga", "kirc_tcga", "kirp_tcga", "lihc_tcga",
                "luad_tcga", "lusc_tcga", "dlbc_tcga", "meso_tcga", "ov_tcga", "paad_tcga", "pcpg_tcga",
                "prad_tcga", "skcm_tcga", "sarc_tcga", "tgct_tcga", "thym_tcga", "thca_tcga", "ucec_tcga",
                "ucs_tcga", "uvm_tcga", "brca_tcga_pub2015"
            ];
            //TODO: split by profile type attribute
            var mrna_profile_arr = _.map(study_arr, function(_study_id) { return _study_id + "_rna_seq_v2_mrna"});
            var mut_profile_arr = _.map(study_arr, function(_study_id) { return _study_id + "_mutations"});

            var params = {
                genes: ["SOX9"],
                genetic_profile_ids: mrna_profile_arr.concat(mut_profile_arr)
            };

            window.cbioportal_client.getGeneticProfileData(params).then(
                function(_result) {

                    var _non_mut_group = _.filter(_result, function(_result_obj) { return !(_result_obj.hasOwnProperty("mutation_status")); });
                    var _mut_mixed_group = _.filter(_result, function(_result_obj) { return _result_obj.hasOwnProperty("mutation_status"); });

                    //categorize mutations
                    //TODO: join for cases with multiple mutations
                    var _mut_groups = {};
                    _.each(_mut_mixed_group, function(_mut_obj) {
                        if (!_mut_groups.hasOwnProperty(_mut_obj.mutation_type)) {
                            _mut_groups[_mut_obj.mutation_type] = [];
                        }
                        _mut_groups[_mut_obj.mutation_type].push(_mut_obj);
                    });

                    //get profile data for mutated cases
                    _.each(_mut_mixed_group, function(_mut_obj) {
                        _.each(_non_mut_group, function(_non_mut_obj) {
                            if (_non_mut_obj.sample_id === _mut_obj.sample_id) {
                                _mut_obj.profile_data = _non_mut_obj.profile_data;
                            }
                        });
                    });

                    //remove all the mutated cases from non-mut group
                    //TODO: numbers doesn't match, check again
                    _non_mut_group = _.filter(_non_mut_group, function(_result_obj) { return ($.inArray( _result_obj.sample_id, _.pluck(_mut_mixed_group, "sample_id")) == -1); });

                    var data = [];

                    var non_mut_track = {
                        x: _.map(_.pluck(_non_mut_group, "study_id"), function(_study_id){ return study_arr.indexOf(_study_id) + Math.random() * 0.3 - 0.15; }),
                        y: _.pluck(_non_mut_group, "profile_data"),
                        mode: 'markers',
                        type: 'scatter',
                        name: 'No Mutation',
                        marker: {
                            size: 6,
                            symbol: 'circle',
                            color: "#00AAF8",
                            opacity: 0.7,
                            line: {
                                color: "#0089C6",
                                width: 0.1
                            }
                        },
                        hoverinfo: "x+y"
                    };
                    data.push(non_mut_track);

                    $.each(Object.keys(_mut_groups), function(_index, _mut_type) {

                        var shapes = ['diamond', 'triangle-down', 'circle', 'pentagon', 'square'];
                        var _mut_group = _mut_groups[_mut_type];

                        var _mut_track = {
                            x: _.map(_.pluck(_mut_group, "study_id"), function(_study_id){ return study_arr.indexOf(_study_id) + Math.random() * 0.3 - 0.15; }),
                            y: _.pluck(_mut_group, "profile_data"),
                            mode: 'markers',
                            type: 'scatter',
                            name: _mut_group[0].mutation_type,
                            marker: {
                                size: 10,
                                symbol: shapes[_index],
                                color: '#FF3333'
                            }
                        };
                        data.push(_mut_track);
                    });

                    _.each(mrna_profile_arr, function(_profile_id) {
                        var _box = {
                            y: _.pluck(_.filter(_non_mut_group, function(_result_obj) { return _result_obj.genetic_profile_id == _profile_id; }), "profile_data"),
                            x0: mrna_profile_arr.indexOf(_profile_id),
                            type: 'box',
                            opacity: 0.6,
                            boxpoints: false,
                            showlegend: false
                        }
                        data.push(_box);
                    });

                    var layout = {
                        yaxis: {
                            range: [ Math.min(_.pluck(_result, "profile_data")), Math.max(_.pluck(_result, "profile_data")) ],
                            title: 'SOX9 Expression -- RNA Seq V2'
                        }
                    };

                    Plotly.newPlot('plotly_cc_plots_box', data, layout);

                });
        }
    }

}(window.Plotly, window._, window.jQuery));

