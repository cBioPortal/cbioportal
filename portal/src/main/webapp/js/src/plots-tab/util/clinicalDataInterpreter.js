var clinical_data_interpreter = (function() {
    
    var text_val_map = {
            x: {}, 
            y: {}
        },
        text_labels = {
            x: [],
            y: []
        };
    
    var extract_discretized_vals = function(_arr) {
        var _result = [];
        $.each(_arr, function(index, val) {
           if ($.inArray(val, _result) === -1) _result.push(val); 
        });
        _result.sort(function(a, b){
            return a === b ? 0 : a < b ? -1 : 1;
        });
        return _result;
    };
    
    var translate_to_numeric = function(_arr, axis) {
        //create text-value pair map
        var datum = {
            numeric_val: -1,
            real_val: ""
        };
        var numeric_val_index = 0;
        text_labels[axis] = []; 
        text_labels[axis].length = 0;
        $.each(_arr, function(index, val) {
            var _datum = jQuery.extend(true, {}, datum);
            _datum.real_val = val;
            _datum.numeric_val = numeric_val_index;
            text_val_map[axis][val] = _datum;
            numeric_val_index += 1;
            if ($.inArray(val, text_labels[axis]) === -1) text_labels[axis].push(val);
        });
    };
    
    return {
        process: function(data, axis) {
            text_val_map[axis] = {};
            var _key = (axis === "x")? "xVal": "yVal";
            var _arr = [];
            _arr.length = 0;
            for(var key in data) {
                _arr.push(data[key][_key]);
            }
            translate_to_numeric(extract_discretized_vals(_arr), axis);
        },
        convert_to_numeric: function(text_val, axis) {
            var _val = text_val_map[axis][text_val].numeric_val;
            return _val.toString();
        },
        convert_to_text: function(numeric_val, axis) {
            for (var key in text_val_map[axis]) {
                if (parseFloat(text_val_map[axis][key].numeric_val) === parseFloat(numeric_val)) {
                    return text_val_map[axis][key].real_val;
                }
            }
        },
        get_text_labels: function(axis) {
            return text_labels[axis];
        }
    };
    
}());