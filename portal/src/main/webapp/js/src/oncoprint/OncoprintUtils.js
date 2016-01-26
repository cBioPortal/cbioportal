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
// Gideon Dresdner
// June 2013
//
// util functions for the Oncoprint.  Basically allows for "static" methods to
// be tested without having to create an Oncoprint on a DOM element.
window.OncoprintUtils = (function() {
    var is_discrete = function(val) {
        return isNaN(parseInt(val));
    };

    var is_gene = function(d) {
        return d.gene !== undefined;
    };

    var is_clinical = function(d) {
        return d.attr_id !== undefined;
    };

    // params: data, list of data as specified elsewhere
    // TODO: where exactly is this specified??
    //
    // returns: data nested by the key, "sample"
    var nest_data = function(data) {
        var result = d3.nest()
//    .key(function(d) { return d.sample; })
    .key(function(d) { 
            if(d.patient === undefined)
            {
                return d.sample; 
            }
            else
            {
               return d.patient; 
            }
        })
    .entries(data);
        return result;
    };

    // returns the gene name or the attr_id, whatever the piece of data has
    // throws Error
    var get_attr = function(d) {
        var to_return =  d.gene || d.attr_id;

        if (to_return === undefined) {
            throw new Error("datum has neither a gene nor an attr_id: "
                    + JSON.stringify(d));
        }
        return to_return;
    };

    // params: list of data, list of attributes to filter by
    // returns: a filtered list, a list of
    var filter_by_attributes = function(data, attributes) {
        var trues = _.range(0, attributes.length);
        trues = _.map(trues, function() { return true; });

        var attribute_set = _.object(attributes, trues);
        var result = _.filter(data, function(datum) {
            return attribute_set[get_attr(datum)];
        });
        return result;
    };

    // params: element of a nested list, list of attributes
    // finds all the missing attributes in the values and creates them with
    // attr_val = "NA"
    var normalize_nested_values = function(key_values, attributes) {
        var attrs = key_values.values.map(function(value) { return get_attr(value); });

        // set minus
        var attribues_minus_attrs = attributes.filter(function(attr) {
            return attrs.indexOf(attr) === -1;
        });

        var new_values = attribues_minus_attrs.map(function(str) {
            return {
                sample: key_values.key,
            attr_id: str,
            attr_val: "NA"
            };
        });

        return key_values.values.concat(new_values);
    };

    // params: list of gene data (not nested),
    // list of clinical data (not nested),
    // list of clinical attributes (string)
    //
    // returns list of raw clinical data
    var normalize_clinical_attributes = function(nested_data, attributes) {
        var no_attributes = attributes.length;

        var normalized = nested_data.map(function(key_values) {
            if (key_values.values.length !== no_attributes) {
                key_values.values = normalize_nested_values(key_values, attributes);
            }
            return key_values;
        });

        return normalized;
    };

    // composes the functions nest_data, filter_by_attributes, and normalize_clinical_attributes
    var process_data = function(data, attributes) {
        var processed = nest_data(filter_by_attributes(data, attributes));
        processed = normalize_clinical_attributes(processed, attributes);

        return processed;
    };

    // params: [list of raw clinical data]
    //
    // returns: map of an attribute id to its respective range
    // where a range is a 2-ple if the corresponding attribute values are numerical
    // and a list of values otherwise
    var attr2range = function(clinicalData,attrs) {
        var attr2range_builder = function(prev, curr) {
            prev[curr.attr_id] = prev[curr.attr_id] || [];      // initialize

            var a2r =  prev[curr.attr_id];
            var val = curr.attr_val;

            // an attribute whose value is "NA" for all samples has no range
            if (val === "NA") {
                return prev;
            }
            
            var attrsDatatype;
            for(var i = 0; i < attrs.length; i++)
            {
                if(curr.attr_id === attrs[i].attr_id)
                {
                   attrsDatatype = attrs[i].datatype; 
                }
            }
            
            if(attrsDatatype.toUpperCase() === "NUMBER") {
                // just keep the min and max -- an interval of values
                //val = parseInt(val);
                var min = a2r[0],
                    max = a2r[1];

                if (max === undefined || val > max) {
                    a2r[1] = val;
                }
                if (min === undefined || val < min) {
                    a2r[0] = val;
                }
            }
            else
            {
                if (a2r.indexOf(val) === -1) { a2r.push(val); }       // keep a set of unique elements

            }

            prev[curr.attr_id] = a2r;
            return prev;
        };

        return _.reduce(clinicalData, attr2range_builder, {});
    };

    // params: [list of raw gene data]
    //
    // returns a map of gene_data type (e.g. cna, mutations, mrna, rppa)
    // to all the values these data types take on respectively in the data
    //
    // if there is no data, then the range is undefined
    var gene_data_type2range = function(raw_gene_data) {
        var extract_unique = function(raw_data, datatype, filter) {
            return _.chain(raw_gene_data)
                .map(function(d) {
                    return d[datatype];
                })
                .unique()
                .filter( function(d) {
                    return d !== undefined && (!filter || filter(d));
                })
                .value();
        };

        var cnas = extract_unique(raw_gene_data, 'cna');
        var mutations = extract_unique(raw_gene_data, 'mutation', function(d){
            var aas = d.split(",");// e.g. A32G,fusion
            for (var i=0, n=aas.length; i<n; i++) {
                if (!/\bfusion\b/i.test(aas[i])) return true;
            }
            return false;
        });

        var fusions = extract_unique(raw_gene_data, 'mutation', function(d){return /\bfusion\b/i.test(d);});
        var mrnas = extract_unique(raw_gene_data, 'mrna');
        var rppas = extract_unique(raw_gene_data, 'rppa');

        var there_is_data = function(list) {
            return list.length > 0;
        };

        var to_return = {};

        if (there_is_data(cnas)) {
            to_return.cna = cnas;

            // sort the order that the cnas show up in the legend
            var cna_order = {
                "AMPLIFIED": 0,
                "GAINED": 1,
                "DIPLOID": 2,
                "HEMIZYGOUSLYDELETED": 3,
                "HOMODELETED": 4
            };

            to_return.cna = to_return.cna.sort(function(x, y) {
                return cna_order[x] - cna_order[y];
            });
        }

        if (there_is_data(mutations)) {
            to_return.mutation = mutations;
        }
        
        if (there_is_data(fusions)) {
            to_return.fusion = fusions;
        }

        if (there_is_data(mrnas)) {
            to_return.mrna = mrnas;
        }

        if (there_is_data(rppas)) {
            to_return.rppa = rppas;
        }

        return to_return;
    };
    
    var attr_data_type2range = function(raw_attr_and_gene_data,attrs_number,raw_clinical_attr,addmixlegend)
    {
        var extract_unique = function(raw_data, length, raw_clinical_attributes,addmixlegendValue ,filter) {
            
            var finalAfterProcess = [];
            var testFinalAfterProcess = [];
            var per_attr_data_length = raw_data.length/length;
            for(var i = 0; i < length; i++)
            {
                var seperate_raw_data = raw_data.slice(i*per_attr_data_length, (i+1)*per_attr_data_length);
                var afterProcess = _.uniq(seperate_raw_data,function (item) { return item.attr_id + item.attr_val;});
                afterProcess = afterProcess.filter( function(d) {
                        return d !== undefined && (!filter || filter(d));
                    });
                
                var afterprocessDatatype = false;
                for(var j=0; j<afterProcess.length;j++)
                {
                   //determin the datatype of afterProcess data
                   if(typeof(afterProcess[j].attr_val) === 'number')
                   {
                      afterprocessDatatype = true;
                   }
                   
                   for(var n=0; n<raw_clinical_attr.length;n++)
                   {
                       if(afterProcess[j].attr_id === raw_clinical_attr[n].attr_id)
                       {
                          afterProcess[j].display_name = raw_clinical_attr[n].display_name;
                       }
                   }
                }
                
                if(afterprocessDatatype)// if data type is number
                {
                    if(afterProcess.length > 12)
                    {
//                        var min = _.min(afterProcess,function(x){return x.attr_val;});
                        var min = jQuery.extend(true, {}, _.min(afterProcess,function(x){return x.attr_val;}));
                        min.attr_val_tpye = 'continuous';
                        var max = _.max(afterProcess,function(x){return x.attr_val;});
                        max.attr_val_tpye = 'continuous';
                        
                        if(min.attr_id === '# mutations' || min.attr_id === 'FRACTION_GENOME_ALTERED')// specify minimum value of 'mutations' and 'FRACTION_GENOME_ALTERED' to 0
                        {
                            min.attr_val = 0;
                        }
                        afterProcess = [min,max];
                    }
                    else
                    {
                        if(addmixlegendValue)
                        {
                            var newMixlegend = {};
                            newMixlegend.attr_id = afterProcess[afterProcess.length-1].attr_id;
                            newMixlegend.attr_val = "Mixed";
                            newMixlegend.display_name = "Mix Legend";
                            newMixlegend.sample = "Mix_LegendSampleID";
                            afterProcess.push(newMixlegend);
                        }
                    }
                }
                else
                {
                    if(addmixlegendValue)
                    {
                        var newMixlegend = {};
                        newMixlegend.attr_id = afterProcess[afterProcess.length-1].attr_id;
                        newMixlegend.attr_val = "Mixed";
                        newMixlegend.display_name = "Mix Legend";
                        newMixlegend.sample = "Mix_LegendSampleID";
                        afterProcess.push(newMixlegend);
                    }
                }
                
                finalAfterProcess = finalAfterProcess.concat(afterProcess);
                testFinalAfterProcess[i] = afterProcess;
                raw_data = raw_data.concat(seperate_raw_data);
            }
            return testFinalAfterProcess;
        };
        
        var attrs = extract_unique(raw_attr_and_gene_data,attrs_number,raw_clinical_attr,addmixlegend);
        if(attrs.length>0)
        {
            attrs[0]= _.sortBy(attrs[0],function(m){return m.attr_val;});
        }
        return attrs;
    }
    var colors = {
        continuous: '#A62459',
        white: '#FFFAF0',
        discrete: '#404040',
        red: '#FF0000',
        grey: '#D3D3D3',
        google: ["#3366cc","#dc3912","#ff9900","#109618",
        "#990099","#0099c6","#dd4477","#66aa00",
        "#b82e2e","#316395","#994499","#22aa99",
        "#aaaa11","#6633cc","#e67300","#8b0707",
        "#651067","#329262","#5574a6","#3b3eac",
        "#b77322","#16d620","#b91383","#f4359e",
        "#9c5935","#a9c413","#2a778d","#668d1c",
        "#bea413","#0c5922","#743411"]
    };

    // takes a map attr2range, and transforms the ranges into d3 scales
    // it does something very simplistic :
    //  *signature:* `string -> discrete , number -> continuous`
    var attr2range_to_d3scale = function(attr2range) {
        for (var a2r in attr2range) {
            var range = attr2range[a2r];
            var discrete = is_discrete(range[0]);
            var range_vals;

            if (!discrete) {
                range_vals = [colors.white, colors.continuous];
            } else if (range.length > 2) {
                range_vals = colors.google.slice(0,range.length);
            } else {
                range_vals = [colors.discrete, colors.white];
            }

            var new_scale = discrete ? d3.scale.ordinal() : d3.scale.linear();
            new_scale.domain(range);
            new_scale.range(range_vals);

            attr2range[a2r] = new_scale;
        }

        return attr2range;
    };

    // composition of attr2range_to_d3scale with attr2range
    var attr_to_d3scale = function(clinicalData) {
        return attr2range_to_d3scale(attr2range(clinicalData));
    };

    // params: list of clinical attribute object literals,
    // list of raw clinical data
    //
    // the clinical attribute literals must have fields
    // <datatype> and <attr_id>
    //
    // returns a map of attr_id to d3 scale
    var make_attribute2scale = function(attrs, raw_clinical_data) {

        var attrId2range = attr2range(raw_clinical_data,attrs);

        var slice_googlecolors = function(attr_id) {
            return colors.google.slice(0, attrId2range[attr_id].length);
        };

        var result = _.chain(attrs)
            .map(function(attr) {
                // attr -> [attr_id, d3 scale]
                var scale;

                // manually override colors for sex (if only De Beauvoir could witness this)
                if (attr.attr_id.toUpperCase() === "SEX"
                    || attr.attr_id.toUpperCase() === "GENDER") {
                    scale = d3.scale.ordinal()
                .domain(["MALE", "male","M","m", "FEMALE", "female","F","f"])
                .range(["#3790d6", "#3790d6",  "#3790d6", "#3790d6", "pink", "pink", "pink", "pink"]);

                    return [attr.attr_id, scale];
                }

                // calculate the proper colors for all other attributes
                if (attr.datatype.toUpperCase() === "BOOLEAN") {
                    scale = d3.scale.ordinal()
                .range([colors.discrete, colors.white]);
                }

                else if (attr.datatype.toUpperCase() === "NUMBER") {
                        scale = d3.scale.linear()
                            .range([colors.white, colors.continuous]);
                }

                else if (attr.datatype.toUpperCase() === "STRING"||attr.attr_id.toUpperCase() === "SUBTYPE") {
                    scale = d3.scale.ordinal()
                        .range(slice_googlecolors(attr.attr_id));
                }

                else {
                    // defaults to discrete scale
                    scale = d3.scale.ordinal()
                        .range( slice_googlecolors(attr.attr_id));
                }
                
                scale.domain(attrId2range[attr.attr_id]);
                return [attr.attr_id, scale];
            })
        .object()
            .value();
            
            return result;
    };

    // params: sample
    // returns: boolean, is the sample altered in a particular gene?
    var altered_gene = function(sample_gene) {

        return !(
                (sample_gene.cna === undefined || sample_gene.cna === "DIPLOID")
                && sample_gene.mutation === undefined
                && sample_gene.mrna === undefined
                && sample_gene.rppa === undefined);
    };

    // params: nested gene data
    //
    // returns: d3.set of sample_ids that *have* genetic alterations
    var filter_altered = function(nested_data) {
        if (nested_data[0].key === undefined) {
            throw new Error("the first element of nested_data does not have a 'key' attribute, therefore I do not think this is nested data.");
        }

        var altered_sample_set = d3.set();

        nested_data.forEach(function(sample) {
            sample.values.forEach(function(sample_gene) {
                if (altered_gene(sample_gene)) {
                    altered_sample_set.add(sample.key);
                }
            });
        });

        return altered_sample_set;
    };

    // params: list of raw gene data (unnested)
    // return: map of gene name to percent altered
    var percent_altered = function(raw_gene_data) {
        var data = d3.nest()
            .key(function(d) { return get_attr(d); })
            .entries(raw_gene_data);

        var attr2percent = {};

        data.forEach(function(gene) {
            var total = gene.values.length;
            var altered = _.chain(gene.values)
            .map(function(sample_gene) {
                return altered_gene(sample_gene) ? 1 : 0;
            })
        .reduce(function(sum, zero_or_one) {
            return sum + zero_or_one;
        }, 0)
        .value();

        var percent = (altered / total) * 100;
        //attr2percent[gene.key] = Math.round(percent);

        if(percent>=1.0)
        {
            attr2percent[gene.key] = Math.round(percent);
        }
        else
        {
            if(percent>0)
            {
                attr2percent[gene.key] = percent.toFixed(1);
            }
            else
            {
                attr2percent[gene.key] = 0;
            }
        }
        });

        return attr2percent;
    };

    var alteration_info = function(raw_gene_data) {
        var data = d3.nest()
            .key(function(d) { return get_attr(d); })
            .entries(raw_gene_data);

        var alterinfo = {};

        data.forEach(function(gene) {
            var total = gene.values.length;
            var altered = _.chain(gene.values)
            .map(function(sample_gene) {
                return altered_gene(sample_gene) ? 1 : 0;
            })
            .reduce(function(sum, zero_or_one) {
                return sum + zero_or_one;
            }, 0)
            .value();

            var percent = (altered / total) * 100;
            alterinfo[gene.key] = {
                "total_alter_num" : altered,
                "percent" : Math.round(percent)
            }
        });

        return alterinfo;
    };


    // params: array of strings (names of attributes)
    // returns: length (number)
    //
    // calculates the length of the longest label for a particular list of
    // attributes (strings) by temporarily appending them as text, calculating
    // their width, and taking the maximum.
    var label_width = function(strings) {
        var tmp = d3.select('body').append('svg');

        strings.forEach(function(attr) {
            tmp.append('text')
            .append('tspan')
            .text(attr);
        });

        var max = d3.max(
                tmp.selectAll('text')[0]
                .map(function(text, i) {return text.getBBox().width; })
                );

        tmp.remove();
        // bye bye tmp

        return 42 + 20 + max; // http://goo.gl/iPzfU
    };

    // takes a list of clinical attribute objects and returns a map that maps
    // `attr_id` to the corresponding clinical attribute object
    //
    // *signature:* `[array] -> {object}`
    var createId2ClinicalAttr = function(clinical_attrs) {
        return id2ClinicalAttr = clinical_attrs.reduce(
                function(name2attr, attr) {
                    name2attr[attr.attr_id] = attr;
                    return name2attr;
                }, {});
    };

    // takes a map and returns a function that checks whether the input key is
    // in the map or not.
    // If it is then return the value, otherwise return the key right back
    // Problem : what if key === value ?  At least this function suffices for
    // our purposes
    //
    // *signature:* `{object} -> function(key)`
    var maybe_map = function(map) {
        return function(key) {
            var value = map[key];
            if (value === undefined) {
                return key;
            }
            else {
                return value;
            }
        };
    };

    var cna_fills = {
        undefined: colors.grey,
        AMPLIFIED: colors.red,
        GAINED: '#FFB6C1',
        DIPLOID: '#D3D3D3',
        HEMIZYGOUSLYDELETED: '#8FD8D8',
        HOMODELETED: '#0000FF'
    };
      
    var CreateLegendII = function (datas,attr2rangeFuntion)
    {
        var dims = (function() {
            var rect_height = 23;
            var mut_height = rect_height / 3;
            var vert_padding = 6;
            var Legend_label_width = label_width(datas.map(
                    function(attr) {
                        return  attr.attr_id;
                    }));

            var clinical_height = (2/3) * rect_height;
            var clinical_offset = (1/6) * rect_height;

            return {
            width: datas.length * (5.5 + 3),
            height: (rect_height + vert_padding) * datas.length,
            rect_height: rect_height,
            rect_width: 5.5,
            vert_padding: vert_padding,
            vert_space: rect_height + vert_padding,
            hor_padding: 3,
            character_length:7.5,
            mut_height: mut_height,
            legend_width: Legend_label_width,
            clinical_height: clinical_height,
            clinical_offset: clinical_offset
            };
        }());

        var calculateMaxLabelLength = function ()
        {
            var labelNumbers = datas.length;
            var maxlength = datas[0][0].attr_id.length*dims.character_length;
            for(var i = 0; i < labelNumbers; i ++)
            {
                datas[i][0].attr_id.length*dims.character_length;
                maxlength = (datas[i][0].attr_id.length*dims.character_length) > maxlength ? (datas[i][0].attr_id.length*dims.character_length) : maxlength;
            }
            var lengestStringLength = 20;//we define truncated the string to 20 characters
            maxlength = lengestStringLength * dims.character_length
            return maxlength;
        };
        
        var maxLabelLength = calculateMaxLabelLength() + 60;

        var calculateHeight = function (valueName)
        {
            var indexValue = _.indexOf(datas,valueName);
            var totalHeight = 1;
            for(var i = 1; i < indexValue; i++)
            {
                if(datas[i].attr_id !== datas[i-1].attr_id)
                {
                    totalHeight +=1;
                }
                else
                {
                    totalHeight +=1;
                    return totalHeight;
                }
            }
            
            return totalHeight;
        }
            
        var calculateDistance = function (idName,valueName)
        {
            var indexValue;
            var dataIndexValue;

            for(var n = 0; n < datas.length; n++)
            {
                if(datas[n][0].attr_id === idName)
                {
                    indexValue = n;
                    dataIndexValue = datas[n].length;
                    break;
                }
            }
            var totalLength = 0;
            for(var i = 0 ; i < dataIndexValue; i++)
            {
                if(datas[indexValue][i].attr_val!==valueName)
                {
                    totalLength += datas[indexValue][i].attr_val.toString().length *dims.character_length + dims.rect_width * 5;
                }
                else
                {
                    break;
                }
            }
            return totalLength;
        };
            
        var calculateLongestLegend = function()
        {
            var longestLegendLength = 0;
            
            for( i = 0; i < datas.length; i++ )
            {
                var longestEachData = 0;
                longestEachData = calculateDistance(datas[i][datas[i].length - 1].attr_id, datas[i][datas[i].length - 1].attr_val) + datas[i][datas[i].length - 1].attr_val.toString().length *dims.character_length + dims.rect_width * 5;
                longestLegendLength= longestEachData > longestLegendLength ? longestEachData: longestLegendLength;
            }
//            var lengstStringLength = 20;
//            longestLegendLength = calculateDistance(datas[i][datas[i].length - 1].attr_id, datas[i][datas[i].length - 1].attr_val) + lengstStringLength * dims.character_length + dims.rect_width * 5;
            return longestLegendLength;
        }; 
        
        var calculateIndividualLegend = function(legendIndex)
        {
            var i = legendIndex;
            var longestEachData = 0;
            longestEachData = calculateDistance(datas[i][datas[i].length - 1].attr_id, datas[i][datas[i].length - 1].attr_val) + datas[i][datas[i].length - 1].attr_val.toString().length *dims.character_length + dims.rect_width * 5;
            
            return longestEachData;
        }; 
        
        var translate = function(x,y) { return "translate(" + x + "," + y + ")"; };
        var calculateDistance = function (idName,valueName)
            {
                var indexValue;
                var dataIndexValue;
                
                for(var n = 0; n < datas.length; n++)
                {
                    if(datas[n][0].attr_id === idName)
                    {
                        indexValue = n;
                        dataIndexValue = datas[n].length;
                        break;
                    }
                }
                var totalLength = 0;
                for(var i = 0 ; i < dataIndexValue; i++)
                {
                    if(datas[indexValue][i].attr_val!==valueName)
                    {
                        totalLength += datas[indexValue][i].attr_val.toString().length *7.5 + dims.rect_width * 5;
                    }
                    else
                    {
                        break;
                    }
                }
                return totalLength;
            };
            
            
        for(var ii = 0; ii< datas.length;ii++)
        {
        var inteData = datas[ii];
        var table = d3.select(document.getElementById('oncoprint_legend'))
        .append('table')
        .attr('id','legend_table')
        .attr('class','mutation_legend_table')
        .attr('height', dims.vert_space)
        .attr('valign','top')
        .style('display','none');

        // hack to get the label flush with the tracks in Firefox
        // the discrepancy is due to the difference in the way browsers display
        // <table>.  Assume that other browsers behave like chrome and that Firefox
        // is the exception.
        var is_firefox = navigator.userAgent.indexOf("Firefox") !== -1;
        var browser_offset = is_firefox ? 16 : 0;

        var label_svg = table
            .append('td')
            .append('svg')
            .attr('height', dims.vert_space)
            .attr('width', function(){return maxLabelLength>120 ? maxLabelLength : 120;})
            .attr('id', 'legend');
            
        var label = label_svg.append('text')
            .attr('font-size', '12px')
            .attr('x', 0)
            .attr('y', dims.rect_height);

        label.append('tspan')       // name
            .attr('text-anchor', 'start')
            .attr('font-weight', 'bold')
            .attr('fill','gray')
            .attr('class','attribute_legend')
            .text(function() {
                var display_name_value = inteData[0].display_name;
                if(display_name_value.length > 20)
                {
                    display_name_value = display_name_value.slice(0,17) + "...";
                }
//                return inteData[0].display_name.toString().toLowerCase();
                return display_name_value.toString().toLowerCase();
            });
            
        var container_width = $('#oncoprint_table div').width();              // default setting 
        var longest_legend_width = calculateLongestLegend();
        var this_legend_width = calculateIndividualLegend(ii)
        var legend_td = table.append('td')
                            .append('div')      // control overflow to the right
                            .style('width', container_width + 'px') // buffer of, say, 70
                            .style('display', 'inline-block')
                            .style('overflow-x', 'hidden')
                            .style('overflow-y', 'hidden'); 
        
        var gradientLegendColorbarLength = 100;
        
        var legend_rec_text =legend_td.append('svg')
                            .attr('height', dims.vert_space)
                            .attr('width', this_legend_width + gradientLegendColorbarLength)
                            .attr('id', 'legend_svg');

        var legend_svg_main = legend_rec_text.append('g')
            .attr('transform',function(){ return translate( 0, 0 );})
            .attr('height', dims.vert_space);
        
        if(inteData[0].attr_val_tpye === 'continuous')
        {
            var legend_svg=legend_svg_main.append('g');
            
            var label = legend_svg.append('text')
                .attr('font-size', '12px')
                .attr('width', function()
                {
                    return cbio.util.toPrecision(inteData[0].attr_val,4,0.00001).toString().length * 6.5;
                })
                .attr('x', function(){
                    return calculateDistance(inteData[0].attr_id,inteData[0].attr_val);} )
                .attr('y', function() {
                    return dims.rect_height; });

            label.append('tspan')       // name
                .attr('text-anchor', 'start')
                .attr('fill','black')
                .attr('class','legend_name')
                .text(function() {
                    if(typeof(inteData[0].attr_val) === 'number')
                    {
                        return cbio.util.toPrecision(inteData[0].attr_val,4,0.00001);
                    }
                    else{
                        return inteData[0].attr_val;
                    }
                }); 
                
           var gradient = legend_svg
                .append("linearGradient")
                .attr("x1", function(){return cbio.util.toPrecision(inteData[0].attr_val,4,0.00001).toString().length * 6.5 + dims.rect_width*3;})
                .attr("x2", function(){return cbio.util.toPrecision(inteData[0].attr_val,4,0.00001).toString().length * 6.5 + dims.rect_width*3 + 100;})
                .attr("y1", "0")
                .attr("y2", "0")
                .attr("id", "gradient")
                .attr("gradientUnits", "userSpaceOnUse");

            gradient
                .append("stop")
                .attr("offset", "0")
                .attr("stop-color", function(){
                if (is_gene(inteData[0])) {
                    return cna_fills[inteData[0].cna];
                }
                else if (is_clinical(inteData[0])) {

                    var result = attr2rangeFuntion[inteData[0].attr_id](inteData[0].attr_val);

                    return inteData[0].attr_val === "NA"
                ? colors.grey       // attrs with value of NA are colored grey
                : result;
                }
            });

            gradient
                .append("stop")
                .attr("offset", "0.5")
                .attr("stop-color",  function(){
                if (is_gene(inteData[1])) {
                    return cna_fills[inteData[1].cna];
                }
                else if (is_clinical(inteData[1])) {

                    var result = attr2rangeFuntion[inteData[1].attr_id](inteData[1].attr_val);

                    return inteData[1].attr_val === "NA"
                ? colors.grey       // attrs with value of NA are colored grey
                : result;
                }
            });



            legend_svg.append("rect")
                .attr("x", function(){
                    return cbio.util.toPrecision(inteData[0].attr_val,4,0.00001).toString().length * 6.5 + dims.rect_width*3;})
                .attr("y", function(){return 6;})
                .attr("width", function(){return 100;})
                .attr("height", 20)
                .attr("fill", "url(#gradient)");
        
            var label = legend_svg.append('text')
                .attr('font-size', '12px')
                .attr('width', function()
                {
                    return cbio.util.toPrecision(inteData[1].attr_val,4,0.00001).toString().length * 6.5;
                })
                .attr('x', function(){
                    return cbio.util.toPrecision(inteData[0].attr_val,4,0.00001).toString().length * 6.5 + 2 * dims.rect_width*3 + 100;} )
                .attr('y', function() {
                    return dims.rect_height; });

            label.append('tspan')       // name
                .attr('text-anchor', 'start')
                .attr('fill','black')
                .attr('class','legend_name')
                .text(function() {
                    if(typeof(inteData[1].attr_val) === 'number')
                    {
                        if(inteData[1].attr_val%1 === 0)
                        {
                            return inteData[1].attr_val;
                        }
                        
                        return cbio.util.toPrecision(inteData[1].attr_val,4,0.00001);
                    }
                    else{
                        return inteData[1].attr_val;
                    }
                }); 
        }
        else
        {
        var legend_svg=legend_svg_main.selectAll('g')
            .data(inteData)
            .enter()
            .append('g');

        // N.B. fill doubles as cna
        var fill = legend_svg.append('rect')
                .attr('fill', function(d) {
                if (is_gene(d)) {
                    return cna_fills[d.cna];
                }
                else if (is_clinical(d)) {

                    var result = attr2rangeFuntion[d.attr_id](d.attr_val);
                    if(d.attr_val === "Mixed")//Mixed Legend reture black color
                    {
                        result = "black";
                    }
                    return d.attr_val === "NA"
                ? colors.grey       // attrs with value of NA are colored grey
                : result;
                }
            })
            .attr('height', function(d) {
            return d.attr_id === undefined ? dims.rect_height : dims.clinical_height;
            })
            .attr('width', dims.rect_width)
            .attr('x', function(d){
                return calculateDistance(d.attr_id,d.attr_val);})
            .attr('y', function() {
                    return dims.vert_padding + 4;
            });

            var label = legend_svg.append('text')
                .attr('font-size', '12px')
                .attr('width', function(d)
                {
                    return d.attr_val.toString().length * 6.5;
                })
                .attr('x', function(d){
                    return calculateDistance(d.attr_id,d.attr_val) + dims.rect_width*3;} )
                .attr('y', function() {
                    return dims.rect_height; });

            label.append('tspan')       // name
                .attr('text-anchor', 'start')
                .attr('fill','black')
                .attr('class','legend_name')
                .text(function(d) {
                    if(typeof(d.attr_val) === 'number')
                    {
                        return cbio.util.toPrecision(d.attr_val,4,0.00001);
                    }
                    else{
                        return d.attr_val;
                    }
                });
        }
        }
    }
    
    // puts a legend in the div according to range for each datatype.  If the
    // range for a datatype is undefined, then it doesn't get represented in
    // the legend
    //
    // *signature:* `DOM el, { string : [string] }, number -> DOM el`
    // * accepts an optional DOM element containing an item template
    var legend = function(el, datatype2range, left_adjust, attrtype2range,attr2rangeFuntion,item_template ) {

        // *signature:* object -> string (html)
        // options can be:
        // * bg_color
        // * display_mutation
        // * display_down_rppa
        // * display_up_rppa
        // * display_down_mrna
        // * display_up_mrna
        var item_templater = function(options) {
            var item_template = item_template || document.getElementById('glyph_template');
            var t = _.template(item_template.innerHTML);

            // set defaults
            options.bg_color = options.bg_color || colors.grey;
            options.display_mutation = options.display_mutation || "none";
            options.display_fusion = options.display_fusion || "none";
            options.display_down_rppa = options.display_down_rppa || "none";
            options.display_up_rppa = options.display_up_rppa || "none";
            options.display_down_mrna = options.display_down_mrna || "none";
            options.display_up_mrna = options.display_up_mrna || "none";

            return t(options);
        };

        // text values that explain the glyph
        var captions = {
            cna: {
                     AMPLIFIED: "Amplification",
                     GAINED: "Gain",
                     DIPLOID: "Diploid",
                     HEMIZYGOUSLYDELETED: "Shallow Deletion",
                     HOMODELETED: "Deep Deletion"
                 },
            mrna: {
                      UPREGULATED: "mRNA Upregulation",
                      DOWNREGULATED: "mRNA Downregulation"
                  },
            rppa: {
                      UPREGULATED: "Protein Upregulation",
                      DOWNREGULATED: "Protein Downregulation"
                  },
            mutation: "mutation",
            fusion: "Fusion"
        };

//        var val2template = {
//            mrna: {
//                      "UPREGULATED": item_templater({display_up_mrna: "inherit", text: captions.mrna.UPREGULATED}),
//                      "DOWNREGULATED": item_templater({display_down_mrna: "inherit", text: captions.mrna.DOWNREGULATED})
//                  },
//            rppa: {
//                      "UPREGULATED": item_templater({display_up_rppa: "inherit", text: captions.rppa.UPREGULATED}),
//                      "DOWNREGULATED": item_templater({display_down_rppa: "inherit", text: captions.rppa.DOWNREGULATED})
//                  }
//        };

        // build up an array of templates from the values in the dataset
        // N.B. order matters here --- so cna is to the left, then comes
        // mutation, etc.
        var templates = [];
//        if (datatype2range.cna !== undefined) {
//            templates = templates.concat(
//                    _.map(datatype2range.cna, function(val) {
//                        if (val !== undefined && val !== "DIPLOID") {
//                            return item_templater({
//                                bg_color: cna_fills[val],
//                                text: captions.cna[val]
//                            });
//                        }
//                    }).filter(function(x) { return x !== undefined; })
//                    );
//        }
//
//        if (datatype2range.mutation !== undefined) {
//            templates = templates.concat(
//                    item_templater({ display_mutation: "inherit", text: captions.mutation})
//                    );
//        }
//
//        if (datatype2range.fusion !== undefined) {
//            templates = templates.concat(
//                    item_templater({ display_fusion: "inherit", text: captions.fusion})
//                    );
//        }
//
//        if (datatype2range.mrna !== undefined) {
//            templates = templates.concat(
//                    _.map(datatype2range.mrna, function(val) {
//                        return val2template.mrna[val];
//                    }).filter(function(x) { return x !== undefined; })
//                    );
//        }
//
//        if (datatype2range.rppa !== undefined) {
//            templates = templates.concat(
//                    _.map(datatype2range.rppa, function(val) {
//                        return val2template.rppa[val];
//                    }).filter(function(x) { return x !== undefined; })
//                    );
//        }     
//
//        var row = _.chain(templates)
//            .map(function(t) {
//                return "<td style='padding-right:10px;'>" + t + "</td>";
//            })
//            .join("")
//            .value();
//        row = "<tr>" +row+ "</tr>";
        
        d3.selectAll("#legend_table").remove();
        
        var table = d3.select(document.getElementById('oncoprint_legend'))
            .append('table')
            .attr('height',function(){
                return 23+6;
            })
            .attr('id','legend_table')
            .attr('class','genetic_legend_table')
            .attr('display','inline')
            .attr('valign','top');

        var calculateMaxLabelLength = function (datas)
        {
            var labelNumbers = datas.length;
            var character_length = 7.5;
            if(labelNumbers === 0 ) 
            {
                return 0;
            }
            var maxlength = datas[0][0].attr_id.length*character_length;
            for(var i = 0; i < labelNumbers; i ++)
            {
                datas[i][0].attr_id.length*character_length;
                maxlength = (datas[i][0].attr_id.length*character_length) > maxlength ? (datas[i][0].attr_id.length*character_length) : maxlength;
            }
            var lengestStringLength = 20;//we define truncated the string to 20 characters
            maxlength = lengestStringLength * character_length;
            return maxlength;
        };
        
        var maxLabelLength = calculateMaxLabelLength(attrtype2range) + 60;

        var label_svg = table
            .append('td')
            .append('svg')
            .attr('height', 23+6)
            .attr('width', function(){return maxLabelLength > 120 ? maxLabelLength :120;})
            .attr('id', 'legend');

        var label = label_svg
            .append('text')
            .attr('font-size', '12px')
            .attr('x', 0)
            .attr('y', 20);

        label.append('tspan')       // name
            .attr('text-anchor', 'start')
            .attr('font-weight', 'bold')
            .attr('fill','gray')
            .attr('class','attribute_legend')
            .text('genetic alteration');
        
        var tabledata = table.append('td')
                            .append('div');
        if (datatype2range.cna !== undefined && datatype2range.cna !== "DIPLOID") 
        {
//            var inter_item_templater;
//            inter_item_templater = item_templater({ bg_color: cna_fills[datatype2range.cna],text: captions.cna[datatype2range.cna]});
            for(var i = 0; i < datatype2range.cna.length; i++ )
            {
                var legend_svg = tabledata
                            .append('svg')
                            .attr('height', 23 )
                            .attr('width', captions.cna[datatype2range.cna[i]].length * 7.5 + 5.5*3 )
                            .attr('x', 0)
                            .attr('id', 'legend_svg')
                            .attr('class', 'legend_cna')
                            .append('g');

                legend_svg.append('rect')
                            .attr('height', 23)
                            .attr('width', 5.5)
                            .attr('fill', cna_fills[datatype2range.cna[i]]);

                var label = legend_svg.append('text')
                    .attr('font-size', '12px')
                    .attr('width', function()
                    {
                        return captions.cna[datatype2range.cna[i]].length * 6.5;
                    })
                    .attr('x', 5.5*3)
                    .attr('y', 19);

                label.append('tspan')       // name
                    .attr('text-anchor', 'start')
                    .attr('fill','black')
                    .attr('class','legend_name')
                    .text(captions.cna[datatype2range.cna[i]]);
            }
        }   
        
//        if(datatype2range.cna !== undefined && datatype2range.mutation !== undefined)
        var findProperMutation = function(source, specialtype)
        {
            switch(specialtype){
                case 1:
                    var findResult = _.find(source,function(element){ return (/^[A-z]([0-9]+)[A-z]$/g).test(element);});
                    if(findResult !== undefined)
                    {
                        return true;
                    }
                    return false;
                case 3:
                    var findResult = _.find(source,function(element){return (/^([A-Z]+)([0-9]+)((del)|(ins))([a-zA-Z]+)$/g).test(element)});
                    if(findResult !== undefined)
                    {
                        return true;
                    }
                    return false;// need to modified by dong li
                case 2:
//                    var findResult = _.find(source,function(element){return (/^([A-Z]+)([0-9]+)del$/g).test(element)});
                    for(var i = 0; i<source.length; i++)
                    {
                        if((/^([A-Z]+)([0-9]+)((del)|(ins))([a-zA-Z]+)$/g).test(source[i]))
                        {
                            continue;
                        }
                        else if((/^[A-z]([0-9]+)[A-z]$/g).test(source[i]))
                        {
                            continue;
                        }
                        else
                        {
                            return true;
                        }
                    }
                    return false;// need to modified by dong li
            }
        }
        var translate = function(x,y) {
            return "translate(" + x + "," + y + ")";
        };


        if (datatype2range.mrna !== undefined) 
        {
                var legend_svg = tabledata
                            .append('svg')
                            .attr('height', 23 )
                            .attr('width', ('mRNA Downregulation').length * 7.5 + 5.5*3 )
                            .attr('x', 0)
                            .attr('id', 'legend_svg')
                            .attr('class', 'legend_cna')
                            .append('g');

                legend_svg.append('rect')
                            .attr('height', 23)
                            .attr('width', 5.5)
                            .attr('fill', colors.grey);
                legend_svg.append('rect')
                            .attr('height', 23)
                            .attr('width', 5.5)
                            .attr('stroke-width',2)
                            .attr('stroke-opacity',1)
                            .attr('stroke','#6699CC')
                            .attr('fill', 'none');

                var label = legend_svg.append('text')
                    .attr('font-size', '12px')
                    .attr('width', function()
                    {
                        return ('mRNA Downregulation').length * 6.5;
                    })
                    .attr('x', 5.5*3)
                    .attr('y', 19);

                label.append('tspan')       // name
                    .attr('text-anchor', 'start')
                    .attr('fill','black')
                    .attr('class','legend_name')
                    .text('mRNA Downregulation');
            
            
                var legend_svg = tabledata
                            .append('svg')
                            .attr('height', 23 )
                            .attr('width', ('mRNA Upregulation').length * 7.5 + 5.5*3 )
                            .attr('x', 0)
                            .attr('id', 'legend_svg')
                            .attr('class', 'legend_cna')
                            .append('g');

                legend_svg.append('rect')
                            .attr('height', 23)
                            .attr('width', 5.5)
                            .attr('fill', colors.grey);
                legend_svg.append('rect')
                            .attr('height', 23)
                            .attr('width', 5.5)
                            .attr('stroke-width',2)
                            .attr('stroke-opacity',1)
                            .attr('stroke','#FF9999')
                            .attr('fill', 'none');

                var label = legend_svg.append('text')
                    .attr('font-size', '12px')
                    .attr('width', function()
                    {
                        return ('mRNA Upregulation').length * 6.5;
                    })
                    .attr('x', 5.5*3)
                    .attr('y', 19);

                label.append('tspan')       // name
                    .attr('text-anchor', 'start')
                    .attr('fill','black')
                    .attr('class','legend_name')
                    .text('mRNA Upregulation');
        }
        
        if (datatype2range.rppa !== undefined) 
        {
                var legend_svg = tabledata
                            .append('svg')
                            .attr('height', 23 )
                            .attr('width', ('RPPA Upregulation').length * 7.5 + 5.5*3 )
                            .attr('x', 0)
                            .attr('id', 'legend_svg')
                            .attr('class', 'legend_cna')
                            .append('g');

                legend_svg.append('rect')
                            .attr('height', 23)
                            .attr('width', 5.5)
                            .attr('fill', colors.grey);
                    
                var sym = d3.svg.symbol().size(5.5 * 3);
                // need to be modified
                var rppa = legend_svg.append('path')
                        .attr('d', sym.type(function(d) {
                            return "triangle-up"; }))
                        .attr('transform', function(d) {
                            // put the triangle in the right spot: at the top if
                            // UNREGULATED, at the bottom otherwise
                            var dy = 23;
                            dy =  dy * 0.1;
                            return translate( 5.5 / 2, dy); });
//                        rppa.filter(function(d) {
//                            return d.rppa === undefined;
//                        });

                var label = legend_svg.append('text')
                    .attr('font-size', '12px')
                    .attr('width', function()
                    {
                        return ('RPPA Upregulation').length * 6.5;
                    })
                    .attr('x', 5.5*3)
                    .attr('y', 19);

                label.append('tspan')       // name
                    .attr('text-anchor', 'start')
                    .attr('fill','black')
                    .attr('class','legend_name')
                    .text('RPPA Upregulation');
            
            
                var legend_svg = tabledata
                            .append('svg')
                            .attr('height', 23 )
                            .attr('width', ('RPPA Downregulation').length * 7.5 + 5.5*3 )
                            .attr('x', 0)
                            .attr('id', 'legend_svg')
                            .attr('class', 'legend_cna')
                            .append('g');

                legend_svg.append('rect')
                            .attr('height', 23)
                            .attr('width', 5.5)
                            .attr('fill', colors.grey);
                var rppa = legend_svg.append('path')
                        .attr('d', sym.type(function(d) {
                            return "triangle-down"; }))
                        .attr('transform', function(d) {
                            // put the triangle in the right spot: at the top if
                            // UNREGULATED, at the bottom otherwise
                            var dy = 23;
                            dy =  dy / 1.1;
                            return translate( 5.5 / 2, dy); });

                var label = legend_svg.append('text')
                    .attr('font-size', '12px')
                    .attr('width', function()
                    {
                        return ('RPPA Downregulation').length * 6.5;
                    })
                    .attr('x', 5.5*3)
                    .attr('y', 19);

                label.append('tspan')       // name
                    .attr('text-anchor', 'start')
                    .attr('fill','black')
                    .attr('class','legend_name')
                    .text('RPPA Downregulation');
        }
        
        if(datatype2range.mutation !== undefined)
        {   
//            if($('#oncoprint_diagram_showmutationcolor_icon')[0].attributes.src.value === 'images/uncolormutations.svg')
//            {
                var legend_svg = tabledata.append('svg')
                            .attr('height', 23 )
                            .attr('width', ('Missense Mutation').length * 7.5 + 5.5*3 )
                            .attr('id', 'legend_svg')
                            .attr('class','legend_missense')
                            .append('g');

                legend_svg.append('rect')
                            .attr('height', 23)
                            .attr('width', 5.5)
                            .attr('fill', colors.grey);

                legend_svg.append('rect')
                        .attr('display',"inherit")
                        .attr('height', 7.666666666666667)
                        .attr('width', 5.5)
                        .attr('y',7.666666666666667)
                        .attr('fill', '#008000');      
                var label = legend_svg.append('text')
                .attr('font-size', '12px')
                .attr('width', function()
                {
                    return ('Missense Mutation').length * 6.5;
                })
                .attr('x', 5.5*3)
                .attr('y', 19);

                label.append('tspan')       // name
                    .attr('text-anchor', 'start')
                    .attr('fill','black')
                    .attr('class','legend_missense_name')
                    .text(captions.mutation); 
                
                if(findProperMutation(datatype2range.mutation,2))
                {
                    var legend_svg = tabledata.append('svg')
                                .attr('height', 23 )
                                .attr('display','none')
                                .attr('width', ('Truncating Mutation').length * 7.5 + 5.5*3 )
                                .attr('id', 'legend_svg')
                                .attr('class', 'legend_nonmissense')
                                .append('g');

                    legend_svg.append('rect')
                                .attr('height', 23)
                                .attr('width', 5.5)
                                .attr('fill', colors.grey);

                    legend_svg.append('rect')
                            .attr('display',"inherit")
                            .attr('height', 7.666666666666667)
                            .attr('width', 5.5)
                            .attr('y',7.666666666666667)
                            .attr('fill', '#000000');      
                    var label = legend_svg.append('text')
                    .attr('font-size', '12px')
                    .attr('width', function()
                    {
                        return ('Truncating Mutation').length * 6.5;
                    })
                    .attr('x', 5.5*3)
                    .attr('y', 19);

                    label.append('tspan')       // name
                        .attr('text-anchor', 'start')
                        .attr('fill','black')
                        .attr('class','legend_name')
                        .text('Truncating Mutation'); 
                }
                
                if(findProperMutation(datatype2range.mutation,3))
                {
                    var legend_svg = tabledata.append('svg')
                                .attr('height', 23 )
                                .attr('display','none')
                                .attr('width', ('inframe mutation').length * 7.5 + 5.5*3 )
                                .attr('id', 'legend_svg')
                                .attr('class', 'legend_nonmissense')
                                .append('g');

                    legend_svg.append('rect')
                                .attr('height', 23)
                                .attr('width', 5.5)
                                .attr('fill', colors.grey);

                    legend_svg.append('rect')
                            .attr('display',"inherit")
                            .attr('height', 7.666666666666667)
                            .attr('width', 5.5)
                            .attr('y',7.666666666666667)
                            .attr('fill', '#9F8170');      
                    var label = legend_svg.append('text')
                    .attr('font-size', '12px')
                    .attr('width', function()
                    {
                        return ('inframe mutation').length * 6.5;
                    })
                    .attr('x', 5.5*3)
                    .attr('y', 19);

                    label.append('tspan')       // name
                        .attr('text-anchor', 'start')
                        .attr('fill','black')
                        .attr('class','legend_name')
                        .text('Inframe Mutation'); 
                }
        }
        if (datatype2range.fusion !== undefined)
        {
                var legend_svg = tabledata
                            .append('svg')
                            .attr('height', 23 )
                            .attr('width', ('Fusion').length * 7.5 + 5.5*3 )
                            .attr('x', 0)
                            .attr('id', 'legend_svg')
                            .attr('class', 'legend_fusion')
                            .append('g');

                legend_svg.append('rect')
                            .attr('height', 23)
                            .attr('width', 5.5)
                            .attr('fill', colors.grey);
                    
                //var sym = d3.svg.symbol().size(5.5 * 3);
                // need to be modified
                var fusion = legend_svg.append('path')
                    .attr('d', "M0,0L0,"+ 23 + " " + 5.5+"," + 23/2 + "Z")
                    .attr('transform',function(d) {
                            var dy = 23;
                            dy =  dy / 23;
                            return translate( 0, 0);
                        });

                var label = legend_svg.append('text')
                    .attr('font-size', '12px')
                    .attr('width', function()
                    {
                        return ('Fusion').length * 6.5;
                    })
                    .attr('x', 5.5*3)
                    .attr('y', 19);

                label.append('tspan')       // name
                    .attr('text-anchor', 'start')
                    .attr('fill','black')
                    .attr('class','legend_name')
                    .text('Fusion');
        }
        if(attrtype2range.length > 0)
        {
            CreateLegendII(attrtype2range,attr2rangeFuntion);
        }
    };

    // params: select_el (a DOM <select> element), clinical_attributes (list of
    // clinical attribute object literals);
    var populate_clinical_attr_select = function(select_el, clinical_attributes) {

        clinical_attributes = [{display_name: 'none', attr_id: undefined}].concat(clinical_attributes);

        var select_el = d3.select(select_el);
        select_el.html("<option value=\"\"></option>");
        select_el.selectAll('option')
            .data(clinical_attributes)
            .enter()
            .append('option')
//            .css('background-color','rgba(255,255,255,.8)')
            .text(function(d) { return d.display_name; });
    };

    // formating for mouseovers
    var format = (function() {

        var standard_cna_values = {
            "AMPLIFIED": "AMP",
            "DELETED": "DEL",
            "GAINED": "GAIN",
            "HOMODELETED": "HOMDEL"
        };

        return {
            mutation: function(d) {
                if (d.mutation) {
                    if (/\bfusion\b/i.test(d.mutation)) return "<b>" + d.mutation + "</b><br/>";
                    else return "Mutation: <b>" + d.mutation + "</b><br/>";
                }
                return "";
            },

            cna: function(d) {
                var standardized_cna = (standard_cna_values[d.cna] || d.cna);

                return d.cna ?
                    "Copy Number Alteration: <b>" + standardized_cna + "</b><br/>"
                    : "";
            },

            mrna: function(d) {
                return d.mrna ?
                    "MRNA: <b>" + d.mrna + "</b><br/>"
                    : "";
            },

            rppa: function(d) {
                return d.rppa ?
                    "RPPA: <b>" + d.rppa + "</b><br/>"
                    : "";
            },

            clinical: function(d) {
                if(typeof d.attr_val === "number"){
                    if(d.patient===undefined)
                    {
                        if(d.attr_val%1 === 0)
                        {
                           return "value: <b>" + d.attr_val + "</b><br/>"; 
                        }

                        return "value: <b>" + cbio.util.toPrecision(d.attr_val,4,0.00001) + "</b><br/>";
                    }
                    else
                    {
                        if(d.attr_val%1 === 0)
                        {
                           return "value: <b>" + d.attr_val + "</b><br/>" + "there are <b>" + d.sample_num + "</b> samples<br/>"; 
                        }

                        return "value: <b>" + cbio.util.toPrecision(d.attr_val,4,0.00001) + "</b><br/>" + "there are <b>" + d.sample_num + "</b> samples<br/>"; 
                    }
                }
                
//                return "value: <b>" + d.attr_val + "</b><br/>";
                if(d.patient===undefined)
                {
                    return "value: <b>" + d.attr_val + "</b><br/>";
                }
                else
                {   if((typeof(d.attr_val)).toUpperCase() === "NUMBER")
                    {
                        return "value: <b>" + d.attr_val + "</b><br/>" + "there are <b>" + d.sample_num + "</b> samples<br/>";
                    }
                    else
                    {
                        return "there are <b>" + d.sample_num + "</b> samples<br/>" + d.tooltip;
                    }
                }
            }
        };
    }());

    var patientViewUrl = function(patient_id) {
        // helper function
        var href = cbio.util.getLinkToPatientView(window.cancer_study_id_selected,patient_id);
        return "<a href='" + href + "'>" + patient_id + "</a>";
    };

    // params: els, list of d3 selected elements with either gene data or
    // clinical bound to them
    var make_mouseover = function(els,params) {
        els.each(function(d) {
            $(this).qtip({
                content: {text: 'oncoprint qtip failed'},
                position: {my:'left bottom', at:'top right', viewport: $(window)},
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
	            show: {event: "mouseover"},
                hide: {fixed: true, delay: 100, event: "mouseout"},
                events: {
                    render: function(event, api) {
                        var content;
//                        var sampleLink = params.linkage?patientViewUrl(d.sample):d.sample;
                        if(d.patient === undefined)
                        {
                            var sampleLink = params.linkage?patientViewUrl(d.sample):d.sample;
                        }
                        else
                        {
                            var sampleLink = params.linkage?patientViewUrl(d.patient):d.patient;
                        }
                        if (d.attr_id) {
                            content = '<font size="2">'
                                + format.clinical(d)
                                + '<font color="blue">' +sampleLink+'</font>' + '</font>';
                        } else {
                            content = '<font size="2">'
                                + format.mutation(d)
                                + format.cna(d)
                                + format.mrna(d)
                                + format.rppa(d)
                                +'<font color="blue">' +sampleLink+'</font>' + '</font>';

                        }
                        api.set('content.text', content);
                    }
                }
            });
        });
    };

    // takes a div and creates a zoombar on it.
    // It calls the function fun on the change event of the zoombar
    //
    // *signature:* `DOM el, function -> DOM el`
//    var zoomSetup = function(div, fun) {
//        return $('<div>', { id: "width_slider", width: "100"})
//            .slider({ text: "Adjust Width ", min: .1, max: 1, step: .01, value: 1,
//                change: function(event, ui) {
//                    fun(ui.value, 'animation');       // N.B.
//                }}).appendTo($(div));
//    };

    var addShowLegendIcon = function(div){
        return $('<img>', { id: "oncoprint-diagram-showlegend-icon",class: "oncoprint-diagram-showlegend-icon",checked:'0', width: "16px",height:"16px", src:"images/showlegend.svg"}).appendTo($(div));
    };
    var zoomSetup = function(div, fun) {
        
        return $('<input>', { id: "oncoprint_zoom_slider",type:'range', min: .1, max: 1, step: .01, value: 1, change: function(event, ui) {
                    fun(this.value, 'animation');       // N.B.
                }}).appendTo($(div));
    };
    return {
        is_discrete: is_discrete,
        nest_data: nest_data,
        get_attr: get_attr,
        filter_by_attributes: filter_by_attributes,
        process_data: process_data,
        attr2range: attr2range,
        attr2range_to_d3scale: attr2range_to_d3scale,
        attr_to_d3scale: attr_to_d3scale,
        filter_altered: filter_altered,
        percent_altered: percent_altered,
        label_width: label_width,
        createId2ClinicalAttr: createId2ClinicalAttr,
        maybe_map: maybe_map,
        normalize_clinical_attributes: normalize_clinical_attributes,
        normalize_nested_values: normalize_nested_values,
        CreateLegendII: CreateLegendII,
        legend: legend,
        make_attribute2scale: make_attribute2scale,
        gene_data_type2range: gene_data_type2range,
        attr_data_type2range: attr_data_type2range,
        is_gene: is_gene,
        is_clinical: is_clinical,
        cna_fills: cna_fills,
        colors: colors,
        populate_clinical_attr_select: populate_clinical_attr_select,
        make_mouseover: make_mouseover,
        addShowLegendIcon:addShowLegendIcon,
        zoomSetup: zoomSetup,
        alteration_info: alteration_info
    };
})();
