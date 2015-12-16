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

    var study_ids = [], mrna_profiles = [], profile_data = {};

    var fetch_profile_data = function(_queried_study_ids) {

        var _param_mrna_profile_arr = _.map(_queried_study_ids, function(_study_id) { return _study_id + "_rna_seq_v2_mrna"});
        var _param_mut_profile_arr = _.map(_queried_study_ids, function(_study_id) { return _study_id + "_mutations"});

        var params = {
            genes: ["SOX9"],
            genetic_profile_ids: _param_mrna_profile_arr.concat(_param_mut_profile_arr)
        };

        window.cbioportal_client.getGeneticProfileData(params).then(
            function(_result) {
                profile_data = _result;
                study_ids = _.uniq(_.pluck(_result, "study_id"));
                mrna_profiles =  _.map(study_ids, function(_study_id) { return _study_id + "_rna_seq_v2_mrna"});
                render();
            });

    }

    var render = function() {


        var data = [];

        // ---- define tracks ----
        // no mutation
        var _non_mut_group = _.filter(profile_data, function(_obj) { return !(_obj.hasOwnProperty("mutation_status")); });
        var non_mut_track = {
            x: _.map(_.pluck(_non_mut_group, "study_id"), function(_study_id){ return study_ids.indexOf(_study_id) + Math.random() * 0.3 - 0.15; }),
            y: _.pluck(_non_mut_group, "profile_data"),
            mode: 'markers',
            type: 'scatter',
            name: 'No Mutation',
            marker: {
                color: '#00AAF8',
                size: 5,
                line: {color: '#0089C6', width: 1.2}
            },
            hoverinfo: "x+y"
        };
        data.push(non_mut_track);

        //box plots
        _.each(mrna_profiles, function(_profile_id) {
            var _box = {
                y: _.pluck(_.filter(_non_mut_group, function(_result_obj) { return _result_obj.genetic_profile_id == _profile_id; }), "profile_data"),
                x0: mrna_profiles.indexOf(_profile_id),
                type: 'box',
                opacity: 0.6,
                marker: {
                    color: 'grey'
                },
                line: { width: 1},
                boxpoints: false,
                showlegend: false
            }
            data.push(_box);
        });

                //var _mut_mixed_group = _.filter(_result, function(_result_obj) { return _result_obj.hasOwnProperty("mutation_status"); });

                ////categorize mutations
                ////TODO: join for cases with multiple mutations
                //var _mut_groups = {};
                //_.each(_mut_mixed_group, function(_mut_obj) {
                //    if (!_mut_groups.hasOwnProperty(_mut_obj.mutation_type)) {
                //        _mut_groups[_mut_obj.mutation_type] = [];
                //    }
                //    _mut_groups[_mut_obj.mutation_type].push(_mut_obj);
                //});
                //
                ////get profile data for mutated cases
                //_.each(_mut_mixed_group, function(_mut_obj) {
                //    _.each(_non_mut_group, function(_non_mut_obj) {
                //        if (_non_mut_obj.sample_id === _mut_obj.sample_id) {
                //            _mut_obj.profile_data = _non_mut_obj.profile_data;
                //        }
                //    });
                //});

                ////remove all the mutated cases from non-mut group
                ////TODO: numbers doesn't match, check again
                //_non_mut_group = _.filter(_non_mut_group, function(_result_obj) { return ($.inArray( _result_obj.sample_id, _.pluck(_mut_mixed_group, "sample_id")) == -1); });





                //$.each(Object.keys(_mut_groups), function(_index, _mut_type) {
                //
                //    var shapes = ['diamond', 'triangle-down', 'circle', 'pentagon', 'square'];
                //    var _mut_group = _mut_groups[_mut_type];
                //
                //    var _mut_track = {
                //        x: _.map(_.pluck(_mut_group, "study_id"), function(_study_id){ return study_arr.indexOf(_study_id) + Math.random() * 0.3 - 0.15; }),
                //        y: _.pluck(_mut_group, "profile_data"),
                //        mode: 'markers',
                //        type: 'scatter',
                //        name: _mut_group[0].mutation_type,
                //        marker: {
                //            size: 10,
                //            symbol: shapes[_index],
                //            color: '#FF3333'
                //        }
                //    };
                //    data.push(_mut_track);
                //});



        // ---- define layout ----
        var vals = [];
        for (var i = 0 ; i < mrna_profiles.length; i++) {
            vals.push(i);
        }
        var layout = {
            margin: {
                t: 20,
                b: 200,
                l: 100
            },
            xaxis: {
                tickmode: "array",
                ticktext: study_ids,
                tickvals: vals,
                tickangle: 45
            },
            yaxis: {
                range: [ Math.min(_.pluck(profile_data, "profile_data")), Math.max(_.pluck(profile_data, "profile_data")) ],
                title: 'SOX9 Expression -- RNA Seq V2'
            }
        };

        Plotly.newPlot('cc_plots_box', data, layout);

    }



    return {
        init: function() {

            var tmp = setInterval(function () {timer();}, 1000);
            function timer() {
                if (window.studies !== undefined) {
                    clearInterval(tmp);
                    $("#cc_plots_box").empty();
                    fetch_profile_data(_.pluck(_.pluck(window.studies.models, "attributes"), "studyId"));
                }
            }

        }
    }

}(window.Plotly, window._, window.jQuery));

