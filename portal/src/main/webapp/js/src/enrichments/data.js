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


var enrichmentsTabData = function() {

    var data = [], retrieved = false, table_id, profile_id;

    function convert_data(_input, _profile_type) {

        var table_arr = [];

        $.each(_input, function(_index, _obj) {

            if (_obj !== null) {
                var _unit = [];
                if (_profile_type === enrichmentsTabSettings.profile_type.copy_num) {
                    _unit[enrichmentsTabSettings.col_index.copy_num.gene] = _obj["Gene"];
                    _unit[enrichmentsTabSettings.col_index.copy_num.cytoband] = _obj["Cytoband"];
                    var _vals_altered_group = _obj["percentage of alteration in altered group"].split("////"); //count & percentage
                    _unit[enrichmentsTabSettings.col_index.copy_num.altered_pct] = _vals_altered_group[0] + " (" + (_vals_altered_group[1] * 100).toFixed(2) + "%)";
                    var _vals_unaltered_group = _obj["percentage of alteration in unaltered group"].split("////"); //count & percentage
                    _unit[enrichmentsTabSettings.col_index.copy_num.unaltered_pct] = _vals_unaltered_group[0] + " (" + (_vals_unaltered_group[1] * 100).toFixed(2) + "%)";
                    _unit[enrichmentsTabSettings.col_index.copy_num.log_ratio] = (_obj["Log Ratio"] !== ">10" && _obj["Log Ratio"] !== "<-10")? parseFloat(_obj["Log Ratio"]).toFixed(2): _obj["Log Ratio"];
                    _unit[enrichmentsTabSettings.col_index.copy_num.direction] = define_direction(_profile_type, _obj["p-Value"], _obj["q-Value"], _obj["Log Ratio"]);
                    _unit[enrichmentsTabSettings.col_index.copy_num.p_val] = trim_p_val_copy_num(_obj["p-Value"]);
                    _unit[enrichmentsTabSettings.col_index.copy_num.q_val] = trim_p_val_copy_num(_obj["q-Value"]);
                } else if (_profile_type === enrichmentsTabSettings.profile_type.mutations) {
                    _unit[enrichmentsTabSettings.col_index.mutations.gene] = _obj["Gene"];
                    _unit[enrichmentsTabSettings.col_index.mutations.cytoband] = _obj["Cytoband"];
                    var _vals_altered_group = _obj["percentage of alteration in altered group"].split("////"); //count & percentage
                    _unit[enrichmentsTabSettings.col_index.mutations.altered_pct] = _vals_altered_group[0] + " (" + (_vals_altered_group[1] * 100).toFixed(2) + "%)";
                    var _vals_unaltered_group = _obj["percentage of alteration in unaltered group"].split("////"); //count & percentage
                    _unit[enrichmentsTabSettings.col_index.mutations.unaltered_pct] = _vals_unaltered_group[0] + " (" + (_vals_unaltered_group[1] * 100).toFixed(2) + "%)";
                    _unit[enrichmentsTabSettings.col_index.mutations.log_ratio] = (_obj["Log Ratio"] !== ">10" && _obj["Log Ratio"] !== "<-10")? parseFloat(_obj["Log Ratio"]).toFixed(2): _obj["Log Ratio"];
                    _unit[enrichmentsTabSettings.col_index.mutations.direction] = define_direction(_profile_type, _obj["p-Value"], _obj["q-Value"], _obj["Log Ratio"]);
                    _unit[enrichmentsTabSettings.col_index.mutations.p_val] = trim_p_val_mutations(_obj["p-Value"]);
                    _unit[enrichmentsTabSettings.col_index.mutations.q_val] = trim_p_val_mutations(_obj["q-Value"]);
                } else if (_profile_type === enrichmentsTabSettings.profile_type.mrna) {
                    // moved the styling of the gene to view.js mDataProp
                    _unit[enrichmentsTabSettings.col_index.mrna.gene] = _obj["Gene"];
                    _unit[enrichmentsTabSettings.col_index.mrna.cytoband] = _obj["Cytoband"];
                    _unit[enrichmentsTabSettings.col_index.mrna.altered_mean] = parseFloat(_obj["mean of alteration in altered group"]).toFixed(2);
                    _unit[enrichmentsTabSettings.col_index.mrna.unaltered_mean] = parseFloat(_obj["mean of alteration in unaltered group"]).toFixed(2);
                    _unit[enrichmentsTabSettings.col_index.mrna.altered_stdev] = parseFloat(_obj["standard deviation of alteration in altered group"]).toFixed(2);
                    _unit[enrichmentsTabSettings.col_index.mrna.unaltered_stdev] = parseFloat(_obj["standard deviation of alteration in unaltered group"]).toFixed(2);
                    _unit[enrichmentsTabSettings.col_index.mrna.p_val] = trim_p_val_mrna(_obj["mean of alteration in altered group"], _obj["mean of alteration in unaltered group"], _obj["p-Value"]);
                    _unit[enrichmentsTabSettings.col_index.mrna.q_val] = trim_p_val_mrna(_obj["mean of alteration in altered group"], _obj["mean of alteration in unaltered group"], _obj["q-Value"]);
                } else if (_profile_type === enrichmentsTabSettings.profile_type.protein_exp) {
                    // moved the styling of the gene to view.js mDataProp
                    _unit[enrichmentsTabSettings.col_index.protein_exp.gene] = _obj["Gene"];
                    _unit[enrichmentsTabSettings.col_index.protein_exp.cytoband] = _obj["Cytoband"];
                    _unit[enrichmentsTabSettings.col_index.protein_exp.altered_mean] = parseFloat(_obj["mean of alteration in altered group"]).toFixed(2);
                    _unit[enrichmentsTabSettings.col_index.protein_exp.unaltered_mean] = parseFloat(_obj["mean of alteration in unaltered group"]).toFixed(2);
                    _unit[enrichmentsTabSettings.col_index.protein_exp.altered_stdev] = parseFloat(_obj["standard deviation of alteration in altered group"]).toFixed(2);
                    _unit[enrichmentsTabSettings.col_index.protein_exp.unaltered_stdev] = parseFloat(_obj["standard deviation of alteration in unaltered group"]).toFixed(2);
                    _unit[enrichmentsTabSettings.col_index.protein_exp.p_val] = trim_p_val_mrna(_obj["mean of alteration in altered group"], _obj["mean of alteration in unaltered group"], _obj["p-Value"]);
                    _unit[enrichmentsTabSettings.col_index.protein_exp.q_val] = trim_p_val_mrna(_obj["mean of alteration in altered group"], _obj["mean of alteration in unaltered group"], _obj["q-Value"]);
                }
                table_arr.push(_unit);
            }

        });
        return table_arr;
    }

    function trim_p_val_mutations(_input_str) {
        return cbio.util.toPrecision(parseFloat(_input_str), 3, 0.01);
    }

    function trim_p_val_copy_num(_input_str) {
        return cbio.util.toPrecision(parseFloat(_input_str), 3, 0.01);
    }

    function trim_p_val_mrna(_param1, _param2, _input_str) {
        var _result_str = cbio.util.toPrecision(parseFloat(_input_str), 3, 0.01);

        if (parseFloat(_param1) > parseFloat(_param2)) {
            _result_str += "<img src=\"images/up1.png\"/>";
        } else {
            _result_str += "<img src=\"images/down1.png\"/>";
        }
        return _result_str;
    }

    function define_direction(_profile_type, _p_val, _q_val, _log_ratio) {

        var _result_str = "";

        if (_profile_type === enrichmentsTabSettings.profile_type.copy_num) {

            if (_log_ratio === ">10") {
                _result_str += enrichmentsTabSettings.text.cooccurrence;
            } else if (_log_ratio === "<-10") {
                _result_str += enrichmentsTabSettings.text.mutex;
            } else if (_log_ratio > 0) {
                _result_str += enrichmentsTabSettings.text.cooccurrence;
            } else if (_log_ratio < 0) {
                _result_str += enrichmentsTabSettings.text.mutex;
            } else if (_log_ratio === 0) {
                _result_str += "--"
            }

        } else if (_profile_type === enrichmentsTabSettings.profile_type.mutations) {
            if (_log_ratio === ">10") {
                _result_str += enrichmentsTabSettings.text.cooccurrence;
            } else if (_log_ratio === "<-10") {
                _result_str += enrichmentsTabSettings.text.mutex;
            } else if (_log_ratio > 0) {
                _result_str += enrichmentsTabSettings.text.cooccurrence;
            } else if (_log_ratio < 0) {
                _result_str += enrichmentsTabSettings.text.mutex;
            } else if (_log_ratio === 0) {
                _result_str += "--"
            }
        }

        if (_p_val < enrichmentsTabSettings.settings.p_val_threshold && _q_val < enrichmentsTabSettings.settings.p_val_threshold && _result_str !== "--") {
            _result_str += "&nbsp;&nbsp;&nbsp;<span class='label label-or-analysis-significant'>Significant</span>";
        }

        return _result_str;
    }

    return {
        init: function(_param, _table_id) {
            profile_id = _param.profile_id;
            table_id = _table_id;
            $.ajax({
                url: "oranalysis.do",
                method: "POST",
                data: _param
            })
            .done(function(result) {
                data = result;
                retrieved = true;
            })
            .fail(function( jqXHR, textStatus ) {
                alert( "Request failed: " + textStatus );
            });
        },
        get: function(callback_func, _div_id, _table_div, _table_id, _table_title, _profile_type, _profile_id, _last_profile, data_type) {
            var tmp = setInterval(function () { timer(); }, 1000);
            function timer() {
                if (retrieved) {
                    clearInterval(tmp);
                    callback_func(data, convert_data(data, _profile_type), _div_id, _table_div, _table_id, _table_title, _profile_type, _profile_id, _last_profile, data_type);
                }
            }
        }
    };

};
