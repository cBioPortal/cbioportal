/* Rule:
 * 
 * condition: function from datum to boolean
 * shapes - a list of Shapes
 * legend_label
 * exclude_from_legend
 * 
 * Shape:
 * type
 * x
 * y
 * ... shape-specific attrs ...
 * 
 * Attrs by shape:
 * 
 * rectangle: x, y, width, height, stroke, stroke-width, fill
 * triangle: x1, y1, x2, y2, x3, y3, stroke, stroke-width, fill
 * ellipse: x, y, width, height, stroke, stroke-width, fill
 * line: x1, y1, x2, y2, stroke, stroke-width
 */

var Shape = require('./oncoprintshape.js');

function ifndef(x, val) {
    return (typeof x === "undefined" ? val : x);
}

function makeIdCounter() {
    var id = 0;
    return function () {
	id += 1;
	return id;
    };
}

function shallowExtend(target, source) {
    var ret = {};
    for (var key in target) {
	if (target.hasOwnProperty(key)) {
	    ret[key] = target[key];
	}
    }
    for (var key in source) {
	if (source.hasOwnProperty(key)) {
	    ret[key] = source[key];
	}
    }
    return ret;
}


var NA_SHAPES = [
    {
	'type': 'rectangle',
	'fill': 'rgba(125, 125, 125, 1)',
	'z': 0,
    },
    /*{
	'type': 'line',
	'x1': '0%',
	'y1': '0%',
	'x2': '100%',
	'y2': '100%',
	'stroke': 'rgba(85, 85, 85, 0.7)',
	'stroke-width': '1',
	'z': '1',
    },*/
];
var NA_STRING = "na";
var NA_LABEL = "N/A";

var extractRGBA = function (str) {
    var ret = [0, 0, 0, 1];
    if (str[0] === "#") {
	// hex, convert to rgba
	var r = parseInt(str[1] + str[2], 16);
	var g = parseInt(str[3] + str[4], 16);
	var b = parseInt(str[5] + str[6], 16);
	str = 'rgba('+r+','+g+','+b+',1)';
    }
    var match = str.match(/^[\s]*rgba\([\s]*([0-9]+)[\s]*,[\s]*([0-9]+)[\s]*,[\s]*([0-9]+)[\s]*,[\s]*([0-9.]+)[\s]*\)[\s]*$/);
    if (match.length === 5) {
	ret = [parseFloat(match[1]) / 255,
	    parseFloat(match[2]) / 255,
	    parseFloat(match[3]) / 255,
	    parseFloat(match[4])];
    }
    return ret;
};

var colorToHex = function(str) {
    var r;
    var g;
    var b;
    var rgba_match = str.match(/^[\s]*rgba\([\s]*([0-9]+)[\s]*,[\s]*([0-9]+)[\s]*,[\s]*([0-9]+)[\s]*,[\s]*([0-9.]+)[\s]*\)[\s]*$/);
    if (rgba_match && rgba_match.length === 5) {
	r = parseInt(rgba_match[1]).toString(16);
	g = parseInt(rgba_match[2]).toString(16);
	b = parseInt(rgba_match[3]).toString(16);
	if (r.length === 1) {
	    r = '0' + r;
	}
	if (g.length === 1) {
	    g = '0' + g;
	}
	if (b.length === 1) {
	    b = '0' + b;
	}
	return '#' + r + g + b;
    }
    
    var rgb_match = str.match(/^[\s]*rgb\([\s]*([0-9]+)[\s]*,[\s]*([0-9]+)[\s]*,[\s]*([0-9]+)[\s]*\)[\s]*$/);
    if (rgb_match && rgb_match.length === 4) {
	r = parseInt(rgb_match[1]).toString(16);
	g = parseInt(rgb_match[2]).toString(16);
	b = parseInt(rgb_match[3]).toString(16);
	if (r.length === 1) {
	    r = '0' + r;
	}
	if (g.length === 1) {
	    g = '0' + g;
	}
	if (b.length === 1) {
	    b = '0' + b;
	}
	return '#' + r + g + b;
    }
    
    return str;
};

var darkenHexColor = function(str) {
    var r = str[1] + str[2];
    var g = str[3] + str[4];
    var b = str[5] + str[6];
    var darkenHexChannel = function(c) {
	c = parseInt(c, 16);
	c *= 0.95;
	c = Math.round(c);
	c = c.toString(16);
	if (c.length === 1) {
	    c = '0' + c;
	}
	return c;
    };
    r = darkenHexChannel(r);
    g = darkenHexChannel(g);
    b = darkenHexChannel(b);
    return '#' + r + g + b;
};

var RuleSet = (function () {
    var getRuleSetId = makeIdCounter();
    var getRuleId = makeIdCounter();

    function RuleSet(params) {
	/* params:
	 * - legend_label
	 * - exclude_from_legend
	 */
	this.rule_set_id = getRuleSetId();
	this.legend_label = params.legend_label;
	this.exclude_from_legend = params.exclude_from_legend;
	this.active_rule_ids = {};
	this.rules_with_id = [];

    }

    RuleSet.prototype.getLegendLabel = function () {
	return this.legend_label;
    }

    RuleSet.prototype.getRuleSetId = function () {
	return this.rule_set_id;
    }

    RuleSet.prototype.addRules = function (list_of_params) {
	var self = this;
	return list_of_params.map(function (params) {
	    return self.addRule(params);
	});
    }

    RuleSet.prototype.addRule = function (params) {
	var rule_id = getRuleId();
	this.rules_with_id.push({id: rule_id, rule: new Rule(params)});
	return rule_id;
    }

    RuleSet.prototype.removeRule = function (rule_id) {
	var index = -1;
	for (var i = 0; i < this.rules_with_id.length; i++) {
	    if (this.rules_with_id[i].id === rule_id) {
		index = i;
		break;
	    }
	}
	if (index > -1) {
	    this.rules_with_id.splice(index, 1);
	}
	delete this.active_rule_ids[rule_id];
    }

    RuleSet.prototype.getRuleWithId = function (rule_id) {
	var ret = null;
	for (var i = 0; i < this.rules_with_id.length; i++) {
	    if (this.rules_with_id[i].id === rule_id) {
		ret = this.rules_with_id[i];
		break;
	    }
	}
	return ret;
    }

    RuleSet.prototype.isExcludedFromLegend = function () {
	return this.exclude_from_legend;
    }

    RuleSet.prototype.getRecentlyUsedRules = function () {
	var self = this;
	return Object.keys(this.active_rule_ids).map(
		function (rule_id) {
		    return self.getRule(rule_id);
		});
    }

    RuleSet.prototype.applyRulesToDatum = function (rules_with_id, datum, cell_width, cell_height) {
	var shapes = [];
	var rules_len = rules_with_id.length;
	for (var j = 0; j < rules_len; j++) {
	    shapes = shapes.concat(rules_with_id[j].rule.apply(datum, cell_width, cell_height));
	}
	return shapes;
    }
    RuleSet.prototype.apply = function (data, cell_width, cell_height, out_active_rules) {
	// Returns a list of lists of concrete shapes, in the same order as data
	var ret = [];
	for (var i = 0; i < data.length; i++) {
	    var rules = this.getRulesWithId(data[i]);
	    if (typeof out_active_rules !== 'undefined') {
		for (var j = 0; j < rules.length; j++) {
		    out_active_rules[rules[j].id] = true;
		}
	    }
	    ret.push(this.applyRulesToDatum(rules, data[i], cell_width, cell_height));
	}
	return ret;
    }

    return RuleSet;
})();

var LookupRuleSet = (function () {
    function LookupRuleSet(params) {
	RuleSet.call(this, params);
	this.lookup_map_by_key_and_value = {};
	this.lookup_map_by_key = {};
	this.universal_rules = [];

	this.rule_id_to_conditions = {};

	this.addRule(NA_STRING, true, {
	    shapes: NA_SHAPES,
	    legend_label: NA_LABEL,
	    exclude_from_legend: false,
	    legend_config: {'type': 'rule', 'target': {'na': true}}
	});
    }
    LookupRuleSet.prototype = Object.create(RuleSet.prototype);

    LookupRuleSet.prototype.getRulesWithId = function (datum) {
	if (typeof datum === 'undefined') {
	    return this.rules_with_id;
	}
	var ret = [];
	ret = ret.concat(this.universal_rules);
	for (var key in datum) {
	    if (typeof datum[key] !== 'undefined') {
		var key_rule = this.lookup_map_by_key[key];
		if (typeof key_rule !== 'undefined') {
		    ret.push(key_rule);
		}
		var key_and_value_rule = (this.lookup_map_by_key_and_value[key] && this.lookup_map_by_key_and_value[key][datum[key]]) || undefined;
		if (typeof key_and_value_rule !== 'undefined') {
		    ret.push(key_and_value_rule);
		}
	    }
	}
	return ret;
    }

    var indexRuleForLookup = function (rule_set, condition_key, condition_value, rule_with_id) {
	if (condition_key === null) {
	    rule_set.universal_rules.push(rule_with_id);
	} else {
	    if (condition_value === null) {
		rule_set.lookup_map_by_key[condition_key] = rule_with_id;
	    } else {
		rule_set.lookup_map_by_key_and_value[condition_key] = rule_set.lookup_map_by_key_and_value[condition_key] || {};
		rule_set.lookup_map_by_key_and_value[condition_key][condition_value] = rule_with_id;
	    }
	}
	rule_set.rule_id_to_conditions[rule_with_id.id] = rule_set.rule_id_to_conditions[rule_with_id.id] || [];
	rule_set.rule_id_to_conditions[rule_with_id.id].push({key: condition_key, value: condition_value});
    };

    LookupRuleSet.prototype.addRule = function (condition_key, condition_value, params) {
	var rule_id = RuleSet.prototype.addRule.call(this, params);

	indexRuleForLookup(this, condition_key, condition_value, this.getRuleWithId(rule_id));

	return rule_id;
    }

    LookupRuleSet.prototype.linkExistingRule = function (condition_key, condition_value, existing_rule_id) {
	indexRuleForLookup(this, condition_key, condition_value, this.getRuleWithId(existing_rule_id));
    }

    LookupRuleSet.prototype.removeRule = function (rule_id) {
	RuleSet.prototype.removeRule.call(this, rule_id);

	while (this.rule_id_to_conditions[rule_id].length > 0) {
	    var condition = this.rule_id_to_conditions[rule_id].pop();
	    if (condition.key === null) {
		var index = -1;
		for (var i = 0; i < this.universal_rules.length; i++) {
		    if (this.universal_rules[i].id === rule_id) {
			index = i;
			break;
		    }
		}
		if (index > -1) {
		    this.universal_rules.splice(index, 1);
		}
	    } else {
		if (condition.value === null) {
		    delete this.lookup_map_by_key[condition.key];
		} else {
		    delete this.lookup_map_by_key_and_value[condition.key][condition.value];
		}
	    }
	}
	delete this.rule_id_to_conditions[rule_id];
    }
    return LookupRuleSet;
})();

var ConditionRuleSet = (function () {
    function ConditionRuleSet(params) {
	RuleSet.call(this, params);
	this.rule_id_to_condition = {};

	this.addRule(function (d) {
	    return d[NA_STRING] === true;
	},
		{shapes: NA_SHAPES,
		    legend_label: NA_LABEL,
		    exclude_from_legend: false,
		    legend_config: {'type': 'rule', 'target': {'na': true}}
		});
    }
    ConditionRuleSet.prototype = Object.create(RuleSet.prototype);

    ConditionRuleSet.prototype.getRulesWithId = function (datum) {
	if (typeof datum === 'undefined') {
	    return this.rules_with_id;
	}
	var ret = [];
	for (var i = 0; i < this.rules_with_id.length; i++) {
	    if (this.rule_id_to_condition[this.rules_with_id[i].id](datum)) {
		ret.push(this.rules_with_id[i]);
	    }
	}
	return ret;
    }

    ConditionRuleSet.prototype.addRule = function (condition, params) {
	var rule_id = RuleSet.prototype.addRule.call(this, params);
	this.rule_id_to_condition[rule_id] = condition;
	return rule_id;
    }

    ConditionRuleSet.prototype.removeRule = function (rule_id) {
	RuleSet.prototype.removeRule.call(this, rule_id);
	delete this.rule_id_to_condition[rule_id];
    }

    return ConditionRuleSet;
})();

var CategoricalRuleSet = (function () {
    function CategoricalRuleSet(params) {
	/* params
	 * - category_key
	 * - categoryToColor
	 */
	LookupRuleSet.call(this, params);
	
	this.colors = ["#3366cc", "#dc3912", "#ff9900", "#109618",
	"#990099", "#0099c6", "#dd4477", "#66aa00",
	"#b82e2e", "#316395", "#994499", "#22aa99",
	"#aaaa11", "#6633cc", "#e67300", "#8b0707",
	"#651067", "#329262", "#5574a6", "#3b3eac",
	"#b77322", "#16d620", "#b91383", "#f4359e",
	"#9c5935", "#a9c413", "#2a778d", "#668d1c",
	"#bea413", "#0c5922", "#743411"]; // Source: D3
	this.colors_index = 0;
	this.used_colors = {};
	
	this.category_key = params.category_key;
	this.category_to_color = ifndef(params.category_to_color, {});
	for (var category in this.category_to_color) {
	    if (this.category_to_color.hasOwnProperty(category)) {
		var color = this.category_to_color[category];
		addCategoryRule(this, category, color);
		this.used_colors[colorToHex(color)] = true;
	    }
	}
    }
    CategoricalRuleSet.prototype = Object.create(LookupRuleSet.prototype);

    var addCategoryRule = function (ruleset, category, color) {
	var legend_rule_target = {};
	legend_rule_target[ruleset.category_key] = category;
	var rule_params = {
	    shapes: [{
		    type: 'rectangle',
		    fill: color,
		}],
	    legend_label: category,
	    exclude_from_legend: false,
	    legend_config: {'type': 'rule', 'target': legend_rule_target}
	};
	ruleset.addRule(ruleset.category_key, category, rule_params);
    };

    var getUnusedColor = function(rule_set) {
	var next_color = rule_set.colors[rule_set.colors_index % rule_set.colors.length];
	while (rule_set.used_colors[next_color]) {
	    var darker_next_color = darkenHexColor(next_color);
	    if (darker_next_color === next_color) {
		break;
	    }
	    next_color = darker_next_color;
	}
	rule_set.used_colors[next_color] = true;
	rule_set.colors_index += 1;
	
	return next_color;
    };
    CategoricalRuleSet.prototype.apply = function (data, cell_width, cell_height, out_active_rules) {
	// First ensure there is a color for all categories
	for (var i = 0, data_len = data.length; i < data_len; i++) {
	    if (data[i][NA_STRING]) {
		continue;
	    }
	    var category = data[i][this.category_key];
	    if (!this.category_to_color.hasOwnProperty(category)) {
		var color = getUnusedColor(this);
		
		this.category_to_color[category] = color;
		addCategoryRule(this, category, color);
	    }
	}
	// Then propagate the call up
	return LookupRuleSet.prototype.apply.call(this, data, cell_width, cell_height, out_active_rules);
    };

    return CategoricalRuleSet;
})();

var LinearInterpRuleSet = (function () {
    function LinearInterpRuleSet(params) {
	/* params
	 * - log_scale
	 * - value_key
	 * - value_range
	 */
	ConditionRuleSet.call(this, params);
	this.value_key = params.value_key;
	this.value_range = params.value_range;
	this.log_scale = params.log_scale; // boolean
	this.inferred_value_range;

	this.makeInterpFn = function () {
	    var range = this.getEffectiveValueRange();
	    
	    if (this.log_scale) {
		var shift_to_make_pos = Math.abs(range[0]) + 1;
		var log_range = Math.log(range[1] + shift_to_make_pos) - Math.log(range[0] + shift_to_make_pos);
		var log_range_lower = Math.log(range[0] + shift_to_make_pos);
		return function(val) {
		    val = parseFloat(val);
		    return (Math.log(val + shift_to_make_pos) - log_range_lower)/log_range;
		};
	    } else {
		var range_spread = range[1] - range[0];
		var range_lower = range[0];
		return function (val) {
		    val = parseFloat(val);
		    return (val - range_lower) / range_spread;
		};
	    }
	};
    }
    LinearInterpRuleSet.prototype = Object.create(ConditionRuleSet.prototype);

    LinearInterpRuleSet.prototype.getEffectiveValueRange = function () {
	var ret = (this.value_range && this.value_range.slice()) || [undefined, undefined];
	if (typeof ret[0] === "undefined") {
	    ret[0] = this.inferred_value_range[0];
	}
	if (typeof ret[1] === "undefined") {
	    ret[1] = this.inferred_value_range[1];
	}
	if (ret[0] === ret[1]) {
	    // Make sure non-empty interval
	    ret[0] -= ret[0] / 2;
	    ret[1] += ret[1] / 2;
	}
	return ret;
    };

    LinearInterpRuleSet.prototype.apply = function (data, cell_width, cell_height, out_active_rules) {
	// First find value range
	var value_min = Number.POSITIVE_INFINITY;
	var value_max = Number.NEGATIVE_INFINITY;
	for (var i = 0, datalen = data.length; i < datalen; i++) {
	    var d = data[i];
	    if (isNaN(d[this.value_key])) {
		continue;
	    }
	    value_min = Math.min(value_min, d[this.value_key]);
	    value_max = Math.max(value_max, d[this.value_key]);
	}
	if (value_min === Number.POSITIVE_INFINITY) {
	    value_min = 0;
	}
	if (value_max === Number.NEGATIVE_INFINITY) {
	    value_max = 0;
	}
	this.inferred_value_range = [value_min, value_max];
	this.updateLinearRules();

	// Then propagate the call up
	return ConditionRuleSet.prototype.apply.call(this, data, cell_width, cell_height, out_active_rules);
    };

    LinearInterpRuleSet.prototype.updateLinearRules = function () {
	throw "Not implemented in abstract class";
    };

    return LinearInterpRuleSet;
})();

var GradientRuleSet = (function () {
    function GradientRuleSet(params) {
	/* params
	 * - color_range
	 */
	LinearInterpRuleSet.call(this, params);
	this.color_range;
	(function setUpColorRange(self) {
	    var color_start;
	    var color_end;
	    try {
		color_start = params.color_range[0]
			.match(/rgba\(([\d.,]+)\)/)
			.split(',')
			.map(parseFloat);
		color_end = params.color_range[1]
			.match(/rgba\(([\d.,]+)\)/)
			.split(',')
			.map(parseFloat);
		if (color_start.length !== 4 || color_end.length !== 4) {
		    throw "wrong number of color components";
		}
	    } catch (err) {
		color_start = [0, 0, 0, 1];
		color_end = [255, 0, 0, 1];
	    }
	    self.color_range = color_start.map(function (c, i) {
		return [c, color_end[i]];
	    });
	})(this);
	this.gradient_rule;
    }
    GradientRuleSet.prototype = Object.create(LinearInterpRuleSet.prototype);

    GradientRuleSet.prototype.updateLinearRules = function () {
	if (typeof this.gradient_rule !== "undefined") {
	    this.removeRule(this.gradient_rule);
	}
	var interpFn = this.makeInterpFn();
	var value_key = this.value_key;
	var color_range = this.color_range;
	this.gradient_rule = this.addRule(function (d) {
	    return d[NA_STRING] !== true;
	},
		{shapes: [{
			    type: 'rectangle',
			    fill: function (d) {
				var t = interpFn(d[value_key]);
				return "rgba(" + color_range.map(
					function (arr) {
					    return (1 - t) * arr[0]
						    + t * arr[1];
					}).join(",") + ")";
			    }
			}],
		    exclude_from_legend: false,
		    legend_config: {'type': 'gradient', 'range': this.getEffectiveValueRange()}
		});
    };

    return GradientRuleSet;
})();

var BarRuleSet = (function () {
    function BarRuleSet(params) {
	LinearInterpRuleSet.call(this, params);
	this.bar_rule;
	this.fill = params.fill || 'rgba(156,123,135,1)';
    }
    BarRuleSet.prototype = Object.create(LinearInterpRuleSet.prototype);

    BarRuleSet.prototype.updateLinearRules = function () {
	if (typeof this.bar_rule !== "undefined") {
	    this.removeRule(this.bar_rule);
	}
	var interpFn = this.makeInterpFn();
	var value_key = this.value_key;
	this.bar_rule = this.addRule(function (d) {
	    return d[NA_STRING] !== true;
	},
		{shapes: [{
			    type: 'rectangle',
			    y: function (d) {
				var t = interpFn(d[value_key]);
				return (1 - t) * 100 + "%";
			    },
			    height: function (d) {
				var t = interpFn(d[value_key]);
				return t * 100 + "%";
			    },
			    fill: this.fill,
			}],
		    exclude_from_legend: false,
		    legend_config: {'type': 'number', 
				    'range': this.getEffectiveValueRange(), 
				    'color': this.fill,
				    'interpFn': interpFn}
		});
    };

    return BarRuleSet;
})();

var GeneticAlterationRuleSet = (function () {
    function GeneticAlterationRuleSet(params) {
	/* params:
	 * - rule_params
	 */
	LookupRuleSet.call(this, params);
	(function addRules(self) {
	    var rule_params = params.rule_params;
	    for (var key in rule_params) {
		if (rule_params.hasOwnProperty(key)) {
		    var key_rule_params = rule_params[key];
		    if (key === '*') {
			self.addRule(null, null, shallowExtend(rule_params['*'], {'legend_config': {'type': 'rule', 'target': {}}}));
		    } else {
			for (var value in key_rule_params) {
			    if (key_rule_params.hasOwnProperty(value)) {
				var equiv_values = value.split(",");
				var legend_rule_target = {};
				legend_rule_target[equiv_values[0]] = value;
				var rule_id = self.addRule(key, (equiv_values[0] === '*' ? null : equiv_values[0]), shallowExtend(key_rule_params[value], {'legend_config': {'type': 'rule', 'target': legend_rule_target}}));
				for (var i = 1; i < equiv_values.length; i++) {
				    self.linkExistingRule(key, (equiv_values[i] === '*' ? null : equiv_values[i]), rule_id);
				}
			    }
			}
		    }
		}
	    }
	})(this);
    }
    GeneticAlterationRuleSet.prototype = Object.create(LookupRuleSet.prototype);

    return GeneticAlterationRuleSet;
})();

var Rule = (function () {
    function Rule(params) {
	this.shapes = params.shapes.map(function (shape) {
	    if (shape.type === 'rectangle') {
		return new Shape.Rectangle(shape);
	    } else if (shape.type === 'triangle') {
		return new Shape.Triangle(shape);
	    } else if (shape.type === 'ellipse') {
		return new Shape.Ellipse(shape);
	    } else if (shape.type === 'line') {
		return new Shape.Line(shape);
	    }
	});
	this.legend_label = typeof params.legend_label === "undefined" ? "" : params.legend_label;
	this.exclude_from_legend = params.exclude_from_legend;
	this.legend_config = params.legend_config;// {'type':'rule', 'target': {'mut_type':'MISSENSE'}} or {'type':'number', 'color':'rgba(1,2,3,1), 'range':[lower, upper]} or {'type':'gradient', 'color_range':['rgba(...)' or '#...', 'rgba(...)' or '#...'], 'number_range':[lower, upper]}
    }
    Rule.prototype.getLegendConfig = function () {
	return this.legend_config;
    }
    Rule.prototype.apply = function (d, cell_width, cell_height) {
	// Gets concrete shapes (i.e. computed
	// real values from percentages)
	var concrete_shapes = [];
	for (var i = 0, shapes_len = this.shapes.length; i < shapes_len; i++) {
	    concrete_shapes.push(this.shapes[i].getComputedParams(d, cell_width, cell_height));
	}
	return concrete_shapes;
    }

    Rule.prototype.isExcludedFromLegend = function () {
	return this.exclude_from_legend;
    }

    return Rule;
})();

module.exports = function (params) {
    if (params.type === 'categorical') {
	return new CategoricalRuleSet(params);
    } else if (params.type === 'gradient') {
	return new GradientRuleSet(params);
    } else if (params.type === 'bar') {
	return new BarRuleSet(params);
    } else if (params.type === 'gene') {
	return new GeneticAlterationRuleSet(params);
    }
}