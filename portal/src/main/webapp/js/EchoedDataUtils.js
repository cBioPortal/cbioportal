var EchoedDataUtils = (function() {

    // Takes data of the form: {value sample_id datatype gene}, nests and
    // mangles to get a list of data of the form indexed by sample_id, i.e. a
    // list of the form {sample_id [cna mutation etc]}
    //
    // *signature:* `[list of {objects}] -> _.chain([list of {objects}])`
    var compress = function(data) {
        var nestBy = function(key) {
            return function(acc, d) {
                acc[d[key]] = acc[d[key]] || [];
                acc[d[key]].push(d);

                return acc;
            };
        };

        return _.chain(data)
                    .groupBy(function(d) {
                        return d.sample_id;
                    })
                    .values()
                    .map(function(datas) {
                        return _.reduce(datas, function(acc, d) {
                            acc.gene = d.gene;      // resetting on every iteration, ...not good
                            acc.sample_id = d.sample_id;
                            acc[d.datatype] = d.value;

                            return acc;
                        }, {});
                    });
    };

    // joins mutations from a particular gene a particular patient into a
    // string where the delimiter is the `on` parameter
    //
    // *signature:* `string _.chain -> _.chain`
    var join_mutations = function(on, chain) {

        return chain.groupBy(function(d) { return d.sample; })
            .values()
            .map(function(sample) {
                return _.chain(sample)
                    .groupBy(function(d) {return d.gene; })
                    .values()
                    .value();
            })
            .map(function(sample_gene_group) {
                return sample_gene_group[0].reduce(function(acc, d) {

                    var joined_mutation = acc.mutation === undefined ? d.mutation : acc.mutation + "," + d.mutation;

                    // initialize
                    acc = _.extend(acc, d);

                    // update
                    acc = _.extend(acc, {mutation: joined_mutation})

                    return acc;
                }, {});
            })
            .flatten();
    };

    var num2cna = {
        "2": "AMPLIFIED",
        "1": "GAINED",
        "0": "DIPLOID",
        "-1": "HEMIZYGOUSLYDELETED",
        "-2": "HOMODELETED"
    };

    var replace_key = function(obj, original, replace) {
        obj[replace] = obj[original];
        delete obj[original];

        return obj;
    };

    // inner functions, not for external use
    var inner = {
        // evalues the chain after doing some key/value replacements
        // *signature:* `_.chain([list of {objects}]) -> _.chain([list of {objects}])`
        oncoprint_wash : function(chain) {
            return chain.map(function(d) {
                d = replace_key(d, 'sample_id', 'sample');

                if (d.cna !== undefined) {
                    d.cna = num2cna[d.cna];
                }

                return d;
            })
        },
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

    var oncoprint_wash = function(data) {
        return remove_undefined(
                join_mutations(",",
                    inner.oncoprint_wash(
                        compress(data))))
            .value();
    };

    return {
        compress: compress,
        inner: inner,
        oncoprint_wash: oncoprint_wash,
        join_mutations: join_mutations,
        remove_undefined: remove_undefined
    };
}());
