/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

define("InputData", function() {
    // *signature:* `string -> _.chain( [{sample_id, gene, mutation}] )`
    var parse_the_mutation = function(str) {
        var data = d3.tsv.parse(str);
        
        // *signature:* `{mutation datum} -> {datum with renamed keys}`
        var munge = function(d) {
            var toReturn = {};
 
            if(d.Alteration !== undefined)
            {
                var res = d.Alteration.trim();
                if(res === "AMP" || res === "GAIN"||res === "DIPLOID"||res === "HETLOSS"||res === "HOMDEL")
                {
                    toReturn.cna = res;
                }
                else if(res === "UP" || res === "DOWN")
                {
                    toReturn.mrna = res;
                } 
                else
                {
                    toReturn.mutation = res;
                }
            }
            
            toReturn.sample = d.Sample;
            
            if(d.Gene !== undefined)
            {
                toReturn.gene = d.Gene;
            }
            
            return toReturn;
        };

        data = data.map(munge);
        
        for(var i = 0; i<data.length; i++)
        {
            if(data[i].gene == undefined)
            {
                data[i].gene = data[0].gene;
            }
        }

        return data;
    };
    // *signature:* `string -> _.chain( [{sample_id, gene, mutation}] )`
    var parse_the_clinic = function(str) {
        var data = d3.tsv.parse(str);
        var dataLength =data.length;
        var obj = data[0];
        var mutationNames = new Array();
        for(var property in obj)
        {
            mutationNames.push(property);
        }
        var newdata = new Array();
        for(var i = 1; i <mutationNames.length;i++)
        {
            var propertyValue = mutationNames[i];
            for(var j = 1;j< dataLength; j++ )
            {
                var toReturn = {};
                toReturn.attr_id = propertyValue;
                toReturn.attr_val = data[j][propertyValue];
                toReturn.sample = data[j].SAMPLE_ID;
                toReturn.datatype = data[0][propertyValue];
                newdata.push(toReturn);
            }
        }

        return newdata;
    };
    // joins on gene and sample to create a single object with
    // a single mutation string with mutations separated by `on`
    //
    // *signature:* `string -> function(coll) -> coll`
    var join_the_mutations_on = function(on) {
        return function(coll) {
            coll = _.chain(coll);

            var join_mutation_on = function(on) {
                return function(list) {
                    return _.reduce(list, function(curr, acc) {

//                        acc = _.extend(acc, curr);

                        var new_mutation_str = _.compact([curr.mutation, acc.mutation]).join(on)    // append mutation to end
                            || undefined;                                                           // or undefined if === ""

                        acc.mutation = new_mutation_str;

                        return acc;
                    }, {});
                };
            };

//            var result1 = coll.groupBy(function(d) {
//                return d.sample + " " + d.gene;
//            })
//                .values();
            return coll.groupBy(function(d) {
                return d.sample + " " + d.gene;
            })
                .values()
                .map(join_mutation_on(","));
//               var result2 = result1.map(join_mutation_on(","));
//               return result2;
        }
    };
    
    // joins on gene and sample to create a single object with
    // a single mutation string with mutations separated by `on`
    //
    // *signature:* `string -> function(coll) -> coll`
    var join_the_clinics_on = function(on) {
        return function(coll) {
            coll = _.chain(coll);

            var join_mutation_on = function(on) {
                return function(list) {
                    return _.reduce(list, function(curr, acc) {

                        return acc;
                    }, {});
                };
            };
            
//            return coll.groupBy(function(d) {
//                return d.sample + " " + d.gene;
//            })
//                .values()
//                .map(join_mutation_on(","));
        return coll;
//               var result2 = result1.map(join_mutation_on(","));
//               return result2;
        }
    };

    var munge_the_mutation = _.compose(join_the_mutations_on(","), parse_the_mutation);
    var munge_the_clinic = _.compose(join_the_clinics_on(","), parse_the_clinic);
    //var munge_the_mutation = parse_the_mutation;
    return {
        join_the_mutations_on: join_the_mutations_on,
        munge_the_mutation: function(data) { return munge_the_mutation(data).value(); },
        munge_the_clinic: function(data) { return munge_the_clinic(data).value(); }
    };
});
