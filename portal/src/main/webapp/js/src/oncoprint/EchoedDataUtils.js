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

define("EchoedDataUtils", function() {

    // converts cna numbers (-1, 0, ...) into strings (HEMIZYGOUSLYDELETED, DIPLOID, ...)
    // *signature:* `coll -> _.chain( [list of data] )`
    var convert_num2cna = function(coll) {
        var num2cna = {
            "2": "AMPLIFIED",
            "1": "GAINED",
            "0": "DIPLOID",
            "-1": "HEMIZYGOUSLYDELETED",
            "-2": "HOMODELETED"
        };

        coll = _.chain(coll);

        return coll.map(function(d) {
            d.cna = num2cna[d.cna];

            return d;
        });
    };

    // removes all properties that have a value of undefined from a list of
    // objects
    // *signature:* `_.chain([list of {data}]) -> _.chain([list of {data}])`
    var remove_undefined = function(chain) {
        return chain.map(function(d) {
            _.each(_.keys(d), function(key) {
                if (d[key] === undefined) {
                    delete d[key];
                }
            });
            return d;
        });
    };

    // takes a list of data that has an attribute "sample_id"
    // and returns a list of unique samples (presumably in order)
    //
    // *signature:* `list -> list`
    var samples = function(data) {
        return _.chain(data)
            .map(function(d) { return d.sample_id; })
            .uniq()
            .value();
    };

    var tsv = {
        newline : "\n",
        sep : "\t"
    };

    // *signature:* `string -> [{sample_id, gene, cna}]`
    var parse_cna_tsv = function(str) {
        var lines = str.split(tsv.newline);

        lines = lines.map(function(str) { return str.trim(); });

        var sample_ids = _.first(lines)
            .split(tsv.sep)
            .slice(2);

        return _.chain(_.rest(lines))
            .map(function(line) {
                var values = line.split(tsv.sep);
                var gene = values[0];
                var entrez = values[1];
                var cnas = values.slice(2);

                return _.map(cnas, function(cna, index) {
                    return {
                        cna: cna,
                        sample: sample_ids[index],
                        gene: gene
                    };
                });
            })
            .flatten()
            .value();
    };

    // *signature:* `string -> _.chain( [{sample_id, gene, mutation}] )`
    var parse_mutation_tsv = function(str) {
        var data = d3.tsv.parse(str);

        var aliases = {
            protein_change: "mutation",
            sample_id: "sample",
            hugo_symbol: "gene"
        };

        // *signature:* `{mutation datum} -> {datum with renamed keys}`
        var munge = function(d) {
            var toReturn = {};

            // rename keys per aliases
            _.each(d, function(val, key) {
                key = key.toLowerCase();
                key = aliases[key] || key;
                toReturn[key] = val;
            });

            toReturn = _.pick(toReturn, _.values(aliases));

            // remove NaN mutations
            if (toReturn.mutation === "NaN") {
                toReturn = {
                    sample: toReturn.sample,
                    gene: toReturn.gene
                };
            }

            return toReturn;
        };

        return data.map(munge);
    };

    var munge_cna = _.compose(convert_num2cna, parse_cna_tsv);

    // joins on gene and sample to create a single object with
    // a single mutation string with mutations separated by `on`
    //
    // *signature:* `string -> function(coll) -> coll`
    var join_mutations_on = function(on) {
        return function(coll) {
            coll = _.chain(coll);

            var join_mutation_on = function(on) {
                return function(list) {
                    return _.reduce(list, function(curr, acc) {

                        acc = _.extend(acc, curr);

                        var new_mutation_str = _.compact([acc.mutation, curr.mutation]).join(on)    // append mutation to end
                            || undefined;                                                           // or undefined if === ""

                        acc.mutation = new_mutation_str;

                        return acc;
                    }, {});
                };
            };

            return coll.groupBy(function(d) {
                return d.sample + " " + d.gene;
            })
                .values()
                .map(join_mutation_on(","));
        }
    };

    var munge_mutation = _.compose(join_mutations_on(","), parse_mutation_tsv);

    // joins data on a key
    // *signature:* `coll, *keys -> coll`
    var join = function() {

        coll = arguments[0];
        coll = _.chain(coll);

        // takes a list of records and joins them into one record, overriding
        // common keys as it goes
        var _join = function(coll) {
            return coll.reduce(function(curr, acc) {
                return _.extend(acc, curr);
            });
        };

        var keys = _.toArray(arguments).slice(1);

        return coll.groupBy(function(d) {
                return keys
                    .map(function(key) {
                        return d[key];
                    })
                    .join(" ");     // make a key
            })
            .values()               // throw away the key
            .map(_join)
            .value();
    };

    return {
        join_mutations_on: join_mutations_on,
        remove_undefined: remove_undefined,
        samples: samples,
        munge_cna: function(data) { return munge_cna(data).value(); },
        munge_mutation: function(data) { return munge_mutation(data).value(); },
        join: join
    };
});
