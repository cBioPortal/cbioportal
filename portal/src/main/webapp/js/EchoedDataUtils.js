var EchoedDataUtils = (function() {

    // Takes data of the form: {value sample_id datatype hugo}, nests and
    // mangles to get a list of data of the form indexed by sample_id, i.e. of
    // the form {sample_id [cna mutation etc]}
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
                            acc.hugo = d.hugo;      // resetting on every iteration, ...not good
                            acc.sample_id = d.sample_id;
                            acc[d.datatype] = d.value;
                            return acc;
                        }, {});
                    });
    };

    var num2cna= {
        "2": "AMPLIFIED",
        "1": "GAINED",
        "0": "DIPLOID",
        "-1": "HEMIZYGOUSLYDELETED",
        "-2": "HOMODELETED"
    };

    // *signature:* `_.chain([list of {objects}]) -> [list of {objects}]`
    var oncoprint_wash_inner = function(chain) {
        return chain.map(function(d) {
            d.sample = d.sample_id;
            delete d.sample_id;

            d.cna = num2cna[d.cna];

            return d;
        })
        .value();
    };

    var oncoprint_wash = function(data) {
        return oncoprint_wash_inner(compress(data));
    };

    return {
        compress: compress,
        oncoprint_wash_inner: oncoprint_wash_inner,
        oncoprint_wash: oncoprint_wash
    };
}());
