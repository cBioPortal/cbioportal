exports = {};


/* SHAPE SPEC */
// type: attrs
// rectangle: x, y, width, height, stroke, stroke-width, fill
// triangle: x1, y1, x2, y2, x3, y3, stroke, stroke-width, fill
// ellipse: x, y, width, height, stroke, stroke-width, fill
// line: x1, y1, x2, y2, stroke, stroke-width

function makeIdCounter() {
	var id = 0;
	return function () {
		id += 1;
		return id;
	};
}

/* Rule Params
 condition
 shapes - a list of Shape params
 legend_label
 exclude_from_legend
 
 Shape Params
 type (name of shape)
 ... then any of the following as relevant ...
 x
 y
 z
 width
 height
 vertexes
 stroke
 fill (an rgba value)
 stroke thickness
 radius
 */

var NA_SHAPES = [
	{
		'type': 'rectangle',
		'fill': 'rgba(238, 238, 238, 1)',
		'z': '0',
	},
	{
		'type': 'line',
		'x1': '0%',
		'y1': '0%',
		'x2': '100%',
		'y2': '100%',
		'stroke': 'rgba(85, 85, 85, 1)',
		'stroke-width': '1',
	},
];
var NA_STRING = "na";
var NA_LABEL = "N/A";

var RuleSet = (function () {
	var getRuleSetId = makeIdCounter();
	var getRuleId = makeIdCounter();

	function RuleSet(params) {
		this.rule_map = {};
		this.rule_set_id = getRuleSetId();
		this.z_map = {};
		this.legend_label = params.legend_label;
		this.exclude_from_legend = params.exclude_from_legend;
	}

	RuleSet.prototype.getLegendLabel = function () {
		return this.legend_label;
	}

	RuleSet.prototype.getRuleSetId = function () {
		return this.rule_set_id;
	}

	RuleSet.prototype.addRule = function (params) {
		var rule_id = getRuleId();
		var z = (typeof params.z === "undefined" ? rule_id : params.z);
		this.rule_map[rule_id] = new Rule(params);
		this.z_map[rule_id] = parseFloat(z);
		return rule_id;
	}

	RuleSet.prototype.removeRule = function (rule_id) {
		delete this.rule_map[rule_id];
	}

	RuleSet.prototype.getRule = function (rule_id) {
		return this.rule_map[rule_id];
	}

	RuleSet.prototype.getRulesInRenderOrder = function () {
		var self = this;
		return Object.keys(this.rule_map).sort(function (rule_id1, rule_id2) {
			if (self.z_map[rule_id1] < self.z_map[rule_id2]) {
				return -1;
			} else if (self.z_map[rule_id1] > self.z_map[rule_id2]) {
				return 1;
			} else {
				return 0;
			}
		}).map(function (rule_id) {
			return self.getRule(rule_id);
		});
	}

	RuleSet.prototype.isExcludedFromLegend = function () {
		return this.exclude_from_legend;
	}

	RuleSet.prototype.apply = function (data, cell_width, cell_height) {
		// Returns a list of lists of concrete shapes, in the same order as data
		var rules = this.getRulesInRenderOrder();
		var rules_len = rules.length;
		return data.map(function (d) {
			var concrete_shapes = [];
			for (var j = 0; j < rules_len; j++) {
				concrete_shapes = concrete_shapes.concat(
					rules[j].getConcreteShapesInRenderOrder(d,
					cell_width,
					cell_height));
			}
			return concrete_shapes;
		});
	}
	return RuleSet;
})();

var CategoricalRuleSet = (function () {
	var colors = ["#3366cc", "#dc3912", "#ff9900", "#109618",
		"#990099", "#0099c6", "#dd4477", "#66aa00",
		"#b82e2e", "#316395", "#994499", "#22aa99",
		"#aaaa11", "#6633cc", "#e67300", "#8b0707",
		"#651067", "#329262", "#5574a6", "#3b3eac",
		"#b77322", "#16d620", "#b91383", "#f4359e",
		"#9c5935", "#a9c413", "#2a778d", "#668d1c",
		"#bea413", "#0c5922", "#743411"]; // Source: D3
	
	function CategoricalRuleSet(params) {
		RuleSet.call(this, params);
		this.type = "categorical";
		this.category_key = params.category_key;
		this.categoryToColor = params.categoryToColor;
		for (var category in this.categoryToColor) {
			if (this.categoryToColor.hasOwnProperty(category)) {
				addCategoryRule(this, category, this.categoryToColor[category]);
			}
		}
		this.addRule({
			condition: function(d) {
				return d[params.category_key] === NA_STRING;
			},
			shapes: NA_SHAPES,
			legend_label: NA_LABEL,
			exclude_from_legend: false
		});
	}
	CategoricalRuleSet.prototype = Object.create(RuleSet.prototype);

	var addCategoryRule = function (ruleset, color, category) {
		var rule_params = {
			condition: function (d) {
				return d[ruleset.category_key] === category;
			},
			shapes: [{
					type: 'rectangle',
					fill: color,
				}],
			legend_label: category,
			exclude_from_legend: false
		};
		ruleset.addRule(rule_params);
	};
	
	CategoricalRuleSet.prototype.apply = function(data, cell_width, cell_height) {
		// First ensure there is a color for all categories
		for (var i = 0, data_len = data.length; i<data_len; i++) {
			var category = data[i][this.category_key];
			if (!this.categoryToColor.hasOwnProperty(category)) {
				var color = colors.pop();
				this.categoryToColor[category] = color;
				addCategoryRule(this, category, color);
			}
		}
		// Then propagate the call up
		return RuleSet.prototype.apply.call(this, data, cell_width, cell_height);
	};
	
	return CategoricalRuleSet;
})();

var InterpRuleSet = (function() {
	function InterpRuleSet(params) {
	}
})();

// now gradient and bar chart can simply extend InterpRuleSet with hardcoded params
var GradientRuleSet = (function() {
	function GradientRuleSet(params) {
		RuleSet.call(this, params);
		var color_range;
		var value_spread = params.value_range[1] - params.value_range[0];
		var value_range_lower = params.value_range[0];
		(function setUpColorRange() {
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
				color_start = [0,0,0,0];
				color_end = [255,255,255,1];
			}
			color_range = color_start.map(function(c, i) {
				return [c, color_end[i]];
			});
		})();
		this.addRule({
			condition: function(d) {
				return isNaN(d[params.value_key]);
			},
			shapes: NA_SHAPES,
			legend_label: NA_LABEL,
			exclude_from_legend: false
		});
		
		this.addRule({
			condition: function(d) {
				return !isNaN(d[params.value_key]);
			},
			shapes: [{
					type: 'rectangle',
					fill: function(d) {
						var t = (d[params.value_key] - 
							value_range_lower)/
							value_spread;
						return "rgba("+color_range.map(
							function(arr) {
								return t*arr[0] 
								+ (1-t)*arr[1];
						}).join(",")+")";
					}
				}],
			legend_label: params.legend_label,
			exclude_from_legend: false
		});
			
	}
	GradientRuleSet.prototype = Object.create(RuleSet.prototype);
	
	return GradientRuleSet;
})();

var Rule = (function () {
	function Rule(params) {
		this.condition = params.condition || function (d) {
			return true;
		};
		this.shapes = params.shapes.map(addDefaultAbstractShapeParams);
		this.legend_label = params.legend_label;
		this.exclude_from_legend = params.exclude_from_legend;
	}
	var addDefaultAbstractShapeParams = function (shape_params) {
		var default_values = {'width': '100%', 'height': '100%', 'x': '0%', 'y': '0%', 'z': 0,
			'x1': '0%', 'x2': '0%', 'x3': '0%', 'y1': '0%', 'y2': '0%', 'y3': '0%',
			'stroke': 'rgba(0,0,0,0)', 'fill': 'rgba(23,23,23,1)', 'stroke-width': '0',
			'radius': 'width 50%'};
		var required_parameters_by_type = {
			'rectangle': ['width', 'height', 'x', 'y', 'stroke', 'fill', 'stroke-width'],
			'triangle': ['x1', 'x2', 'x3', 'y1', 'y2', 'y3', 'stroke', 'fill', 'stroke-width'],
			'circle': ['radius', 'x', 'y', 'stroke', 'fill', 'stroke-width'],
			'line': ['x1', 'x2', 'y1', 'y2', 'stroke', 'stroke-width']
		};
		var complete_shape_params = {};
		var required_parameters = required_parameters_by_type[shape_params.type];
		for (var i = 0; i < required_parameters.length; i++) {
			var required_param = required_parameters[i];
			if (shape_params.hasOwnProperty(required_param)) {
				complete_shape_params[required_param] = shape_params[required_param];
			} else {
				complete_shape_params[required_param] = default_values[required_param];
			}
		}
		return complete_shape_params;
	};
	Rule.prototype.getConcreteShapesInRenderOrder = function (d, cell_width, cell_height) {
		// Turns abstract shapes into concrete shapes (i.e. computes
		// real values from percentages), and returns them in z-order; 
		// or returns empty list if the rule condition is not met.
		if (!this.condition(d)) {
			return [];
		}
		var concrete_shapes = [];
		var width_axis_attrs = {"x": true, "x1": true, "x2": true, "x3": true, "width": true};
		var height_axis_attrs = {"y": true, "y1": true, "y2": true, "y3": true, "height": true};
		for (var i = 0, shapes_len = this.shapes.length; i < shapes_len; i++) {
			var shape_spec = this.shapes[i];
			var attrs = Object.keys(shape_spec);
			var concrete_shape = {};
			for (var j = 0, attrs_len = attrs.length; j < attrs_len; j++) {
				var attr_name = attrs[j];
				var attr_val = shape_spec[attr_name];
				if (typeof attr_val === 'function') {
					attr_val = attr_val(d);
				}
				var percent = (typeof attr_val === 'string') && attr_val.match(/([\d.]+)%/);
				percent = percent && percent.length > 1 && percent[1];
				if (percent) {
					var multiplier = parseFloat(percent) / 100.0;
					if (width_axis_attrs.hasOwnProperty(attr_name)) {
						attr_val = multiplier * cell_width;
					} else if (height_axis_attrs.hasOwnProperty(attr_name)) {
						attr_val = multiplier * cell_height;
					}
				}
				concrete_shape[attr_name] = attr_val + '';
			}
			concrete_shapes.push(concrete_shape);
		}
		return concrete_shapes.sort(function (shape1, shape2) {
			if (shape1.z < shape2.z) {
				return -1;
			} else if (shape1.z > shape2.z) {
				return 1;
			} else {
				return 0;
			}
		});
	}

	Rule.prototype.isExcludedFromLegend = function () {
		return this.exclude_from_legend;
	}
	
	return Rule;
})();
