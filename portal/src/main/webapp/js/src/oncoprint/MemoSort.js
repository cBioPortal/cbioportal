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

//
//
// Gideon Dresdner <dresdnerg@cbio.mskcc.org> May 2013
//
//
;
// data is assumed to be in the form that is used in the OncoPrint.
// Eventually this can be modified so that it can take different json formats.
//
// attributes : the ordering of the attributes.  Omission basically amounts to "smallest"
//
// returns the data, sorted
define(function() {
    return function(data, attributes,mutationColorControl,mutationColorSort,sortStatus,sortByElement) {
        // compares two objects that have gene data (cna, mutation, mrna, rppa).
        // Returns a number that indicates the order.
        var comp_genes = function(attr1, attr2,mutationcontrol) {
            var cna_order = {AMPLIFIED:4, HOMODELETED:3, GAINED:2, HEMIZYGOUSLYDELETED:1, DIPLOID: 0, undefined: 0},
                regulated_order = {UPREGULATED: 2, DOWNREGULATED: 1, undefined: 0},
                mutation_order_f = function(m) { 
                    
//                    if(m !== undefined)//multiple mutations
//                    {
//                        m = m.split(',');
//
//                        if(m.length > 1)
//                        {
//                            for(var i = 0; i < m.length; i++)
//                            {
//                                if((/^[A-z]([0-9]+)[A-z]$/g).test(m[i]))
//                                {
//                                    continue;
//                                }
//                                else
//                                {
//                                    return 8;
//                                }
//                            }
//
//                            return 2;
//                        }
//                    }
//                    
//                    if((/^[A-z]([0-9]+)[A-z]$/g).test(m))
//                    {
//                        return 2;
//                    }
//                    else if(m !== undefined)
//                    {
////                        if((/^[A-Z]([0-9]+)[*]$/g).test(m))//Nonsense_Mutation
////                        {return 8;}
////                        if((/^[A-z*]([0-9]+)[A-z]{2}$/g).test(m))//Frame_shift_del
////                        {return 7;}
////                        if((/^([A-Z]+)([0-9]+)del$/g).test(m))//IN_frame_del
////                        {return 6;}
////                        if((/^[A-Z]([0-9]+)_splice$/g).test(m))//Splice_Site
////                        {return 5;}
////                        if((/^([A-Z]+)([0-9]+)del$/g).test(m))//IN_frame_del
////                        {return 4;}
//
//                        if((/^([A-Z]+)([0-9]+)del$/g).test(m))//IN_frame_del
//                        {return 3;}
//                        return 4; // need to modified by dong li
//                    }
//                    
//                    return m === undefined ? 0 : (/\bfusion\b/i.test(m)?2:1); 
                    if(mutationcontrol === 'multiColor'|| mutationcontrol === undefined)
                    {
                        if(m === undefined) 
                        {
                            return 0;
                        }

                        if(m !== undefined)//multiple mutations
                        {
                            m = m.split(',');

                            if(m.length > 1)
                            {
                                var hasIndel = false;
                                for(var i = 0; i < m.length; i++)
                                {
                                    if(!/\bfusion\b/i.test(m[i]) && !(/^[A-z]([0-9]+)[A-z]$/g).test(m[i]))
                                    {
                                        return 3;
                                    }

                                    if ((/^([A-Z]+)([0-9]+)((del)|(ins))([a-zA-Z]+)$/g).test(m[i])) {
                                        hasIndel = true;
                                    }
                                }

                                return hasIndel ? 2 : 1;
                            }
                        }

                        if((/^[A-z]([0-9]+)[A-z]$/g).test(m))
                        {
                            return 1;//Missense_mutation
                        }
                        else if((/^([A-Z]+)([0-9]+)((del)|(ins))([a-zA-Z]+)$/g).test(m) )
                        {
                            return 2;//inframe
                        }
                        else 
                        {
                            return 3;
                        }
                    }
                    else
                    {
                        return m === undefined ? 0 : 1;
                    }
                };

            var cna_diff = cna_order[attr2.cna] - cna_order[attr1.cna];
            if (cna_diff !== 0) {
                return cna_diff;
            }

            var mutation_diff = mutation_order_f(attr2.mutation) - mutation_order_f(attr1.mutation);
            if (mutation_diff !== 0) {
                return mutation_diff;
            }

            var mrna_diff = regulated_order[attr2.mrna] - regulated_order[attr1.mrna];
            if (mrna_diff !== 0) {
                return mrna_diff;
            }

            var rppa_diff = regulated_order[attr2.rppa] - regulated_order[attr1.rppa];
            if (rppa_diff !== 0) {
                return rppa_diff;
            }

            return 0;       // they are equal in every way
        };
        var comp_insideGenes = function(attr1, attr2){
                var cna_order = {AMPLIFIED:4, HOMODELETED:3, GAINED:2, HEMIZYGOUSLYDELETED:1, DIPLOID: 0, undefined: 0},
                regulated_order = {UPREGULATED: 2, DOWNREGULATED: 1, undefined: 0},
                mutation_order_f = function(m) { 
                        if(m === undefined) 
                        {
                            return 0;
                        }

                        if(m !== undefined)//multiple mutations
                        {
                            m = m.split(',');

                            if(m.length > 1)
                            {
                                var hasIndel = false;
                                for(var i = 0; i < m.length; i++)
                                {
                                    if(!/\bfusion\b/i.test(m[i]) && !(/^[A-z]([0-9]+)[A-z]$/g).test(m[i]))
                                    {
                                        return 3;
                                    }

                                    if ((/^([A-Z]+)([0-9]+)((del)|(ins))([a-zA-Z]+)$/g).test(m[i])) {
                                        hasIndel = true;
                                    }
                                }

                                return hasIndel ? 2 : 1;
                            }
                        }

                        if((/^[A-z]([0-9]+)[A-z]$/g).test(m))
                        {
                            return 1;//Missense_mutation
                        }
                        else if((/^([A-Z]+)([0-9]+)((del)|(ins))([a-zA-Z]+)$/g).test(m) )
                        {
                            return 2;//inframe
                        }
                        else 
                        {
                            return 3;
                        }
                }

//                var cna_diff = cna_order[attr2.cna] - cna_order[attr1.cna];
//                if (cna_diff !== 0) {
//                    return cna_diff;
//                }
                
                var mutation_diff = mutation_order_f(attr2.mutation) - mutation_order_f(attr1.mutation);
                if (mutation_diff !== 0) {
                    return mutation_diff;
                }
                return 0;
        };
        // compares two objects of clinical data (attr_ids and attr_vals)
        // returns a *number* that indicates which one is larger
        var comp_clinical = function(attr1, attr2,desc) {
            var discrete = isNaN(parseInt(val1));
            var val1 = attr1.attr_val;
            var val2 = attr2.attr_val;
            var ret;

            // "NA" value goes to the end
            if (val1 === "NA") {
                return val2 === "NA" ? 0 : 1;
            } else if (val2 === "NA") {
                return val1 === "NA" ? 0 : -1;
            } else if (discrete) {
                if (val1 < val2) {
                    ret = 1;
                } else if (val2 < val1) {
                    ret = -1;
                } else {
                    ret = 0;
                }
            } else {  // continuous value type
                ret = val2 - val1;
            }
            
            return desc ? ret : -ret;
        };

        var getAttr = function(d) {
            return d.gene || d.attr_id;
        };

        // throws an error without dying
        var assert = function(bool) {
            if (!bool) {
                throw new Error("Assertion failure " + assert.caller());
            }
        };

        // a little bit of optimization
        var attr2index = (function() {
            var toReturn = {};
            for (var i = 0; i < attributes.length; i+=1) {
                toReturn[attributes[i]] = i;
            }
            return toReturn;
        }());

        var comp = function(x,y) {
            // sort attributes according to the order specified by the user
//            var x_attrs = x.values
//                .sort(function(x,y) { return attr2index[getAttr(x)] - attr2index[getAttr(y)]; });
//            x_attrs=_.filter(x_attrs,function(e){return (e.attr_id in attr2index || e.gene in attr2index)});
//
//            var y_attrs = y.values
//                .sort(function(x,y) { return attr2index[getAttr(x)] - attr2index[getAttr(y)]; });
//            y_attrs=_.filter(y_attrs,function(e){return (e.attr_id in attr2index || e.gene in attr2index)});

            var x_attrs=_.filter(x.values,function(e){return (e.attr_id in attr2index || e.gene in attr2index)});
            x_attrs = x_attrs.sort(function(x,y) { return attr2index[getAttr(x)] - attr2index[getAttr(y)]; });

            var y_attrs=_.filter(y.values,function(e){return (e.attr_id in attr2index || e.gene in attr2index)});
            y_attrs = y_attrs.sort(function(x,y) { return attr2index[getAttr(x)] - attr2index[getAttr(y)]; });

            
            // this is a hack
            // if there is missing data, just put the one with less data to the right
            if (x_attrs.length !== y_attrs.length) {
                return y_attrs.length - x_attrs.length;
            }

            // iterate over the attributes of x and y in the user defined
            // order, comparing along the way
            if( mutationColorSort===undefined|| mutationColorSort === "mutationcolornonsort")
            {
                var genesLength = x_attrs.length - sortStatus.length;
                
                for (var j = 0; j < x_attrs.length; j+=1) 
                {
                    var xj = x_attrs[j];
                    var yj = y_attrs[j];
                    
                    var indexValue;
                    if(sortByElement==='genes')
                    {
                        indexValue = j - genesLength;
                    }
                    else
                    {
                        indexValue = j;
                    }
                    var descValue = sortStatus[indexValue]==="decreSort"? false:true;

                    assert(xj.gene === yj.gene);        // what we are comparing are comparable
                    assert(xj.attr_id === yj.attr_id);

                    var diff = (xj.gene === undefined
                        ? comp_clinical(xj, yj, descValue)
                        :  comp_genes(xj, yj,mutationColorControl));

                    // return the first nonzero diff
                    if (diff !== 0) {
                        return diff;
                    }
                }
            }
            else
            {
                var genesLength = x_attrs.length - sortStatus.length;
                
                for (var j = 0; j < x_attrs.length; j+=1) 
                {
                    var xj = x_attrs[j];
                    var yj = y_attrs[j];
                    
                    var indexValue;
                    if(sortByElement==='genes')
                    {
                        indexValue = j - genesLength;
                    }
                    else
                    {
                        indexValue = j;
                    }
                    var descValue = sortStatus[indexValue]==="decreSort"? false:true;
                    
                    assert(xj.gene === yj.gene);        // what we are comparing are comparable
                    assert(xj.attr_id === yj.attr_id);

                    var diff = (xj.gene === undefined
                        ? comp_clinical(xj, yj, descValue)
                        :  comp_genes(xj, yj,"singleColor"));

                    // return the first nonzero diff
                    if (diff !== 0) {
                        return diff;
                    }
                }

                for (var j = 0; j < x_attrs.length; j+=1) 
                {
                    var xj = x_attrs[j];
                    var yj = y_attrs[j];
                    
                    var indexValue;
                    if(sortByElement==='genes')
                    {
                        if(x_attrs.length === x.values.length)
                        {
                            indexValue = j - genesLength;
                        }
                        else
                        {
                            indexValue = j;
                        }
                    }
                    else
                    {
                        indexValue = j;
                    }
                    var descValue = sortStatus[indexValue]==="decreSort"? false:true;
                    
                    assert(xj.gene === yj.gene);        // what we are comparing are comparable
                    assert(xj.attr_id === yj.attr_id);

                    var diff = (xj.gene === undefined
                        ? comp_clinical(xj, yj, descValue)
                        :  comp_genes(xj, yj,mutationColorControl));

                    // return the first nonzero diff
                    if (diff !== 0) {
                        return diff;
                    }
                }
            }
            // if they are equal in all diffs, then they are truly equal.
//            return x_attrs[0].sample.localeCompare(y_attrs[0].sample);
            if(x_attrs[0].patient === undefined)
            {
                return x_attrs[0].sample.localeCompare(y_attrs[0].sample);
            }
            else
            {
                return x_attrs[0].patient.localeCompare(y_attrs[0].patient);
            }
        };

        return data.sort(comp);
    };
});
