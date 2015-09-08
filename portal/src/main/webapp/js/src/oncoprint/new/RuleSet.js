window.oncoprint_RuleSet = (function() {
	var utils = oncoprint_utils;
	var defaults = oncoprint_defaults;

	var CATEGORICAL_COLOR = 'categorical_color';
	var GRADIENT_COLOR = 'gradient_color'; 
	var GENETIC_ALTERATION = 'genetic_alteration';
	var BAR_CHART = 'bar_chart';

	var CELL = "cell";
	var ANY = '*';

	var getRuleSetId = utils.makeIdCounter();

	var numericalNaNSort = function(d1, d2) {
		var f1 = parseFloat(d1[this.data_key], 10);
		var f2 = parseFloat(d2[this.data_key], 10);
		var f1_isNaN = isNaN(f1);
		var f2_isNaN = isNaN(f2);
		if (f1_isNaN && f2_isNaN) {
			return 0;
		} else if (!f1_isNaN && !f2_isNaN) {
			if (f1 < f2) {
				return -1;
			} else if (f1 > f2) {
				return 1;
			} else {
				return 0;
			}	
		} else if (f1_isNaN) {
			return Number.POSITIVE_INFINITY;
		} else {
			return Number.NEGATIVE_INFINITY;
		}
	};
	var makeNARuleParams = function(condition, label) {
		return {
				condition: condition,
				shape: utils.makeD3SVGElement('rect'),
				attrs: {fill: '#eeeeee', width: '100%', height:'100%'},
				legend_label: label,
				children: [{
					condition: condition,
					shape: utils.makeD3SVGElement('path'),
					attrs: {d: "m 0% 0% L 100% 100%"},
					styles: {'stroke-width':'1px', 'stroke':'#555555'},
					legend_label: label,
				}],
			};
	};
	var D3SVGRuleSet = (function() {
		function D3SVGRuleSet(params) {
			this.rule_map = {};
			this.rule_set_id = getRuleSetId();
			this.legend_label = params.legend_label;
			this.exclude_from_legend = false;
		};
		var getRuleId = utils.makeIdCounter();

		D3SVGRuleSet.prototype.getLegendLabel = function() {
			return this.legend_label;
		};
		D3SVGRuleSet.prototype.getRuleSetId = function() {
			return this.rule_set_id;
		};
		D3SVGRuleSet.prototype.addRule = function(params) {
			var rule_id = getRuleId();
			this.rule_map[rule_id] = new D3SVGRule(params, rule_id);
			return rule_id;
		}
		D3SVGRuleSet.prototype.addStaticRule = function(params) {
			var rule_id = getRuleId();
			this.rule_map[rule_id] = new D3SVGStaticRule(params, rule_id);
			return rule_id;
		};
		D3SVGRuleSet.prototype.addGradientRule = function(params) {
			var rule_id = getRuleId();
			this.rule_map[rule_id] = new D3SVGGradientRule(params, rule_id);
			return rule_id;
		};
		D3SVGRuleSet.prototype.addBarChartRule = function(params) {
			var rule_id = getRuleId();
			this.rule_map[rule_id] = new D3SVGBarChartRule(params, rule_id);
			return rule_id;
		};
		D3SVGRuleSet.prototype.removeRule = function(rule_id) {
			delete this.rule_map[rule_id];
		};
		D3SVGRuleSet.prototype.getRules = function() {
			var self = this;
			var rule_ids = Object.keys(this.rule_map);
			var rules = _.map(rule_ids, function(id) { return self.rule_map[id]; });
			var sorted_rules = _.sortBy(rules, function(r) { return r.z_index; });
			return sorted_rules;
		};
		D3SVGRuleSet.prototype.apply = function(g, cell_width, cell_height) {
			var active_rules = {};
			_.each(this.getRules(), function(rule) {
				var affected_groups = rule.filter(g);
				if (affected_groups[0].length > 0) {
					active_rules[rule.rule_id] = true;
				}
				rule.apply(affected_groups, cell_width, cell_height);
			});
			return active_rules;
		};
		D3SVGRuleSet.prototype.getRule = function(rule_id) {
			return this.rule_map[rule_id];
		};
		return D3SVGRuleSet;
	})();

	function D3SVGCategoricalColorRuleSet(params) {
		D3SVGRuleSet.call(this, params);
		this.type = CATEGORICAL_COLOR;
		var self = this;
		var d3_colors = ["#3366cc","#dc3912","#ff9900","#109618",
				"#990099","#0099c6","#dd4477","#66aa00",
				"#b82e2e","#316395","#994499","#22aa99",
				"#aaaa11","#6633cc","#e67300","#8b0707",
				"#651067","#329262","#5574a6","#3b3eac",
				"#b77322","#16d620","#b91383","#f4359e",
				"#9c5935","#a9c413","#2a778d","#668d1c",
				"#bea413","#0c5922","#743411"];/*_.shuffle(_.filter(d3.scale.category20().range().concat(d3.scale.category20b().range()).concat(d3.scale.category20c().range()),
									function(color) {
										var rgb = d3.rgb(color);
										return !(rgb.r === rgb.g && rgb.g === rgb.b);
									}));*/
		var addColorRule = function(color, category) {
			var colored_rect = utils.makeD3SVGElement('rect').attr('fill', color);
			var condition = (function(cat) {
				return function(d) {
					return params.getCategory(d) === cat;
				};
			})(category);
			self.addStaticRule({
				condition: condition,
				shape: colored_rect,
				legend_label: category
			});
		};
		params.color = params.color || {};
		_.each(params.color, function(color, category) {
			addColorRule(color, category);
		});
		self.addStaticRule(makeNARuleParams(function(d) {
			return params.getCategory(d) === 'NA';
		}, 'NA'));

		this.sort_cmp = params.sort_cmp || function(d1,d2) {
			var cat1 = params.getCategory(d1);
			var cat2 = params.getCategory(d2);
			if (typeof cat1 !== 'string') {
				cat1 = cat1.toString();
			}
			if (typeof cat2 !== 'string') {
				cat2 = cat2.toString();
			}
			if (cat1 === cat2) {
				return 0;
			} else if (cat1 === 'NA') {
				return Number.POSITIVE_INFINITY;
			} else if (cat2 === 'NA') {
				return Number.NEGATIVE_INFINITY;
			} else {
				return cat1.localeCompare(cat2);
			}
		};
		self.apply = function(g, cell_width, cell_height) {
			g.each(function(d,i) {
				var category = params.getCategory(d);
				if (!params.color.hasOwnProperty(category) && category !== "NA") {
					var new_color = d3_colors.pop();
					params.color[category] = new_color;
					addColorRule(new_color, category);
				}
			});
			return D3SVGRuleSet.prototype.apply.call(this, g, cell_width, cell_height);
		};

		self.getLegendDiv = function(active_rules, cell_width, cell_height) {
			var div = d3.select(document.createElement('div'));
			_.each(self.getRules(), function(rule) {
				if (active_rules[rule.rule_id]) {
					var legend_div = rule.getLegendDiv(cell_width, cell_height);
					if (legend_div) {
						div.node().appendChild(legend_div);
					}
				}
			});
			utils.d3SelectChildren(div, '*').style('padding-right', '20px');
			return div.node();
		};
	}
	D3SVGCategoricalColorRuleSet.prototype = Object.create(D3SVGRuleSet.prototype);

	function D3SVGGradientColorRuleSet(params) {
		D3SVGRuleSet.call(this, params);
		this.type = GRADIENT_COLOR;
		this.data_key = params.data_key;
		var rule = this.addGradientRule({
			shape: utils.makeD3SVGElement('rect'),
			data_key: params.data_key,
			data_range: params.data_range,
			color_range: params.color_range,
			scale: params.scale,
			na_color: params.na_color
		});
		this.addStaticRule(makeNARuleParams(function(d) {
			return isNaN(d[params.data_key]);
		}, 'NA'));
		this.sort_cmp = params.sort_cmp || $.proxy(numericalNaNSort, this);
		this.getLegendDiv = function(active_rules, cell_width, cell_height) {
			return (active_rules[rule] && this.rule_map[rule].getLegendDiv(cell_width, cell_height)) || $('<div>')[0];
		};
	}
	D3SVGGradientColorRuleSet.prototype = Object.create(D3SVGRuleSet.prototype);

	function D3SVGBarChartRuleSet(params) {
		D3SVGRuleSet.call(this, params);
		var self = this;
		self.type = BAR_CHART;
		self.data_key = params.data_key;
		var rule = this.addBarChartRule({
			data_key: params.data_key,
			data_range: params.data_range,
			scale: params.scale,
			fill: params.fill,
			na_color: params.na_color
		});
		this.addStaticRule(makeNARuleParams(function(d) {
			return isNaN(d[params.data_key]);
		}, 'NA'));
		this.sort_cmp = params.sort_cmp || $.proxy(numericalNaNSort, this);
		this.getLegendDiv = function(active_rules, cell_width, cell_height) {
			return (active_rules[rule] && this.rule_map[rule].getLegendDiv(cell_width, cell_height)) || $('<div>')[0];
		};
	}
	D3SVGBarChartRuleSet.prototype = Object.create(D3SVGRuleSet.prototype);

	function D3SVGGeneticAlterationRuleSet(params) {
		if (params && params.dont_distinguish_mutation_color) {
			params = $.extend({}, params, defaults.genetic_alteration_config_nondistinct_mutations);
		} else {
			params = $.extend({}, params, defaults.genetic_alteration_config);
		}
		if (params && params.distinguish_mutation_order) {
			this.sort_cmp = defaults.genetic_alteration_comparator;
		} else {
			this.sort_cmp = defaults.genetic_alteration_comparator_nondistinct_mutations;
		}
		D3SVGRuleSet.call(this, params);
		var vocab = ['full-rect', 'middle-rect', 'large-right-arrow', 'small-up-arrow', 'small-down-arrow'];
		var self = this;
		self.type = GENETIC_ALTERATION;

		var makeStaticShapeRule = function(rule_spec, key, value) {
			var condition = typeof key !== 'undefined' && typeof value !== 'undefined' ? (function(_key, _value) {
				if (_value === ANY) {
					return function(d) {
						return typeof d[_key] !== 'undefined';
					}
				} else {
					return function(d) {
						return d[_key] === _value;
					};
				}
			})(key, value) : undefined;
			var shape, attrs, styles, z_index;
			switch (rule_spec.shape) {
				case 'full-rect':
					shape = utils.makeD3SVGElement('rect');
					attrs = {fill: rule_spec.color, width: '100%', height: '100%'};
					styles = {};
					z_index = utils.ifndef(rule_spec.z_index, 0);
					break;
				case 'middle-rect':
					shape = utils.makeD3SVGElement('rect');
					attrs = {fill: rule_spec.color, width: '100%', height: '33.33%', y: '33.33%'};
					styles = {};
					z_index = utils.ifndef(rule_spec.z_index, 1);
					break;
				case 'large-right-arrow':
					shape = utils.makeD3SVGElement('polygon');
					attrs = {points: "0%,0% 100%,50% 0%,100%"};
					styles = {'stroke-width':'0px', 'fill': rule_spec.color};
					z_index = utils.ifndef(rule_spec.z_index, 2);
					break;
				case 'small-up-arrow':
					shape = utils.makeD3SVGElement('polygon');
					attrs = {points: "50%,0% 100%,25% 0%,25%"};
					styles = {'stroke-width':'0px', 'fill': rule_spec.color};
					z_index = utils.ifndef(rule_spec.z_index, 3);
					break;
				case 'small-down-arrow':
					shape = utils.makeD3SVGElement('polygon');
					attrs = {points: "50%,100% 100%,75% 0%,75%"};
					styles = {'stroke-width':'0px', 'fill': rule_spec.color};
					z_index = utils.ifndef(rule_spec.z_index, 4);
					break;
				case 'outline':
					shape = CELL;
					styles = {'outline-color':rule_spec.color, 'outline-style':'solid', 'outline-width':'2px'};
					z_index = utils.ifndef(rule_spec.z_index, 5);
					break;
			}
			var new_rule = self.addStaticRule({
				condition: condition,
				shape: shape,
				attrs: attrs,
				styles: styles,
				z_index: z_index,
				legend_label: rule_spec.legend_label,
				exclude_from_legend: (typeof rule_spec.legend_label === "undefined")
			});
			return new_rule;
		};
		var altered_rules = [];
		_.each(params.altered, function(values, key) {
			_.each(values, function(rule_spec, value) {
				altered_rules.push(makeStaticShapeRule(rule_spec, key, value));
			});
		});
		_.each(params.default, function(rule_spec) {
			makeStaticShapeRule(rule_spec);
		});
		self.getLegendDiv = function(active_rules, cell_width, cell_height) {
			var div = d3.select(document.createElement('div'));
			_.each(self.getRules(), function(rule) {
				if (active_rules[rule.rule_id]) {
					var legend_div = rule.getLegendDiv(cell_width, cell_height);
					if (legend_div) {
						div.node().appendChild(legend_div);
					}
				}
			});
			utils.d3SelectChildren(div, '*').style('padding-right', '20px');
			return div.node();
		};
		self.alteredData = function(data) {
			var altered_data = [];
			_.each(altered_rules, function(rule_id) {
				altered_data = altered_data.concat(self.getRule(rule_id).filterData(data));
			});
			return _.uniq(altered_data);
		};
	}
	D3SVGGeneticAlterationRuleSet.prototype = Object.create(D3SVGRuleSet.prototype);

	var D3SVGRule = (function() {
		function D3SVGRule(params, rule_id) {
			this.rule_id = rule_id;
			this.condition = params.condition || function(d) { return true; };
			this.shape = typeof params.shape === 'undefined' ? utils.makeD3SVGElement('rect') : params.shape;
			this.z_index = typeof params.z_index === 'undefined' ? this.rule_id : params.z_index;
			this.legend_label = params.legend_label;
			this.exclude_from_legend = params.exclude_from_legend;

			this.attrs = params.attrs || {};
			this.attrs.width = utils.ifndef(this.attrs.width, '100%');
			this.attrs.height = utils.ifndef(this.attrs.height, '100%');
			this.attrs.x = utils.ifndef(this.attrs.x, 0);
			this.attrs.y = utils.ifndef(this.attrs.y, 0);

			this.styles = params.styles || {};

			this.children = _.map(params.children, function(p) {
				return new D3SVGRule(p);
			});
		}

		var percentToPx = function(attr_val, attr_name, cell_width, cell_height) {
			// convert a percentage to a local pixel coordinate
			var width_like = ['width', 'x'];
			var height_like = ['height', 'y'];
			attr_val = parseFloat(attr_val, 10)/100;
			if (width_like.indexOf(attr_name) > -1) {
				attr_val = attr_val*cell_width;
			} else if (height_like.indexOf(attr_name) > -1) {
				attr_val = attr_val*cell_height;
			} 
			return attr_val+'';
		};

		var convertAttr = function(d, i, attr_val, attr_name, cell_width, cell_height) {
			var ret = attr_val;
			if (typeof ret === 'function') {
				ret = ret(d,i);
			}
			if (typeof ret === 'string' && ret.indexOf('%') > -1) {
				if (attr_name === 'points') {
					ret = _.map(ret.split(" "), function(pt) {
						var split_pt = pt.split(",");
						var pt_x = percentToPx(split_pt[0], 'x', cell_width, cell_height);
						var pt_y = percentToPx(split_pt[1], 'y', cell_width, cell_height);
						return pt_x+","+pt_y;
					}).join(" ");
				} else if (attr_name === 'd') {
					var split = ret.split(/\s+/);
					for (var i=0, _len = split.length; i<_len; i++) {
						var c = split[i].toLowerCase();
						if (c === 'm' || c === 'l') {
							split[i+1] = percentToPx(split[i+1], 'x', cell_width, cell_height);
							split[i+2] = percentToPx(split[i+2], 'y', cell_width, cell_height);
							i += 2;
						}
					}
					return split.join(" ");
				} else {
					ret = percentToPx(ret, attr_name, cell_width, cell_height);
				}
			}
			return ret;
		};

		D3SVGRule.prototype.apply = function(g, cell_width, cell_height) {
			var shape = this.shape;
			var elts = shape === CELL ? g : utils.appendD3SVGElement(shape, g);
			var styles = this.styles;
			var attrs = this.attrs;
			attrs.x = attrs.x || 0;
			attrs.y = attrs.y || 0;
			_.each(attrs, function(val, key) {
				elts.attr(key, function(d,i) {
					return convertAttr(d, i, val, key, cell_width, cell_height);
				});
			});
			_.each(styles, function(val, key) {
				elts.style(key, val);
			});
			_.each(this.children, function(r) {
				r.apply(g, cell_width, cell_height);
			});
		}
		D3SVGRule.prototype.filter = function(g) {
			return g.filter(this.condition);
		};
		D3SVGRule.prototype.filterData = function(data) {
			return data.filter(this.condition);
		};
		D3SVGRule.prototype.showInLegend = function() {
			return !this.exclude_from_legend;
		};
		return D3SVGRule;
	})();
	

	function D3SVGBarChartRule(params, rule_id) {
		D3SVGRule.call(this, params, rule_id);
		this.data_key = params.data_key;
		this.data_range = params.data_range;
		this.inferred_data_range;
		this.attrs.fill = function(d) {
			if (isNaN(d[params.data_key])) {
				return params.na_color;
			}  else {
				return params.fill;
			}
		};
		this.na_color = params.na_color;

		var scale = function(x) {
			if (params.scale === 'log') {
				return Math.log10(Math.max(Math.abs(x), 0.1)); 
			} else {
				return x;
			}
		};
		var makeDatum = function(x) {
			var ret = {};
			ret[params.data_key] = x;
			return ret;
		};

		this.setUpHelperFunctions = function(data_range) {
			var scaled_data_range = _.map(data_range, scale);
			var height_helper = function(d) {
				var datum = scale(d[params.data_key]);
				var distance = Math.abs(datum-scaled_data_range[0]) / Math.abs(scaled_data_range[1]-scaled_data_range[0]);
				return distance * 100;
			};
			var y_function = function(d) {
				return (isNaN(d[params.data_key]) ? "0" : (100 - height_helper(d))) + '%';
			};
			var height_function = function(d) { 
				return (isNaN(d[params.data_key]) ? "100" : height_helper(d)) + '%';
			};
			this.attrs.height = height_function;
			this.attrs.y = y_function;
		};

		this.inferDataRange = function(g) {
			var self = this;
			var min = Number.POSITIVE_INFINITY;
			var max = Number.NEGATIVE_INFINITY;
			g.each(function(d,i) {
				var datum = d[self.data_key];
				var datumIsNaN = isNaN(datum);
				min = Math.min(min, datumIsNaN ? Number.POSITIVE_INFINITY : datum);
				max = Math.max(max, datumIsNaN ? Number.NEGATIVE_INFINITY : datum);
			});
			return [min, max];
		};

		this.getEffectiveDataRange = function() {
			if (typeof this.data_range === "undefined") {
				return this.inferred_data_range;
			} else {
				var ret = [];
				ret[0] = (typeof this.data_range[0] === 'undefined' ? this.inferred_data_range[0] : this.data_range[0]);
				ret[1] = (typeof this.data_range[1] === 'undefined' ? this.inferred_data_range[1] : this.data_range[1]);
				return ret;
			}
		};
		this.getLegendDiv = function(cell_width, cell_height) {
			if (!this.showInLegend()) {
				return;
			}
			var div = d3.select(document.createElement('div'));
			var data_range = this.getEffectiveDataRange();
			if (!data_range) {
				return div.node();
			}
			var display_data_range = _.map(data_range, function(x) { 
				var num_digit_multiplier = Math.pow(10, utils.ifndef(params.legend_num_decimal_digits,2));
				return Math.round(x * num_digit_multiplier) / num_digit_multiplier;
			});
			div.append('span').text(display_data_range[0]).classed('oncoprint-legend-label oncoprint-legend-element', true)
					.style('position', 'relative').style('bottom', '0px');
			var mesh = 50;
			var svg = div.append('svg').attr('width', mesh+'px').attr('height', cell_height+'px').classed('oncoprint-legend-element', true)
			for (var i=0; i<=mesh; i++) {
				var t = i/mesh;
				var d = (1-t)*data_range[0] + t*data_range[1];
				var datum = makeDatum(d);
				var height = cell_height*parseInt(this.attrs.height(datum))/100;
				svg.append('rect')
					.attr('width', '1px')
					.attr('height', height+'px')
					.attr('y', (cell_height-height)+'px')
					.attr('fill', params.fill)
					.attr('x', i+'px');
			}
			div.append('span').text(display_data_range[1]).classed('oncoprint-legend-label oncoprint-legend-element', true)
					.style('position', 'relative').style('bottom', cell_height - 3 + 'px');
			utils.d3SelectChildren(div, '*').style('padding-right', '10px');
			return div.node();
		};
		this.apply = function(g, cell_width, cell_height) {
			if (g[0].length === 0) {
				return;
			}
			this.inferred_data_range = this.inferDataRange(g);
			this.setUpHelperFunctions(this.getEffectiveDataRange());
			D3SVGRule.prototype.apply.call(this, g, cell_width, cell_height);
		};

	}
	D3SVGBarChartRule.prototype = Object.create(D3SVGRule.prototype);

	function D3SVGGradientRule(params, rule_id) {
		D3SVGRule.call(this, params, rule_id);
		this.data_key = params.data_key;
		this.data_range = params.data_range;
		this.inferred_data_range;
		this.color_range = params.color_range;
		this.na_color = params.na_color;

		var makeDatum = function(x) {
			var ret = {};
			ret[params.data_key] = x;
			return ret;
		};
		var scale = function(x) {
			if (params.scale === 'log') {
				return Math.log10(Math.max(x, 0.1)); 
			} else {
				return x;
			}
		};

		this.setUpHelperFunctions = function(data_range) {
			var scaled_data_range = _.map(data_range, scale);
			var fill_function = function(d) {
				if (isNaN(d[params.data_key])) {
					return params.na_color;
				}
 				var datum = scale(d[params.data_key]);
				var data_range = [scaled_data_range[0], scaled_data_range[1]];
				var distance = (datum-scaled_data_range[0]) / (scaled_data_range[1]-scaled_data_range[0]);
				color_range = [d3.rgb(params.color_range[0]).toString(),
						d3.rgb(params.color_range[1]).toString()];
				return utils.lin_interp(distance, params.color_range[0], params.color_range[1]);
			};
			this.attrs.fill = fill_function;
		};

		this.inferDataRange = function(g) {
			var self = this;
			var min = Number.POSITIVE_INFINITY;
			var max = Number.NEGATIVE_INFINITY;
			g.each(function(d,i) {
				var datum = d[self.data_key];
				var datumIsNaN = isNaN(datum);
				min = Math.min(min, datumIsNaN ? Number.POSITIVE_INFINITY : datum);
				max = Math.max(max, datumIsNaN ? Number.NEGATIVE_INFINITY : datum);
			});
			return [min, max];
		};

		this.getLegendDiv = function(cell_width, cell_height) {
			if (!this.showInLegend()) {
				return;
			}
			var div = d3.select(document.createElement('div'));
			var data_range = this.data_range || this.inferred_data_range;
			if (!data_range) {
				return div.node();
			}
			var display_data_range = _.map(data_range, function(x) { 
				var num_digit_multiplier = Math.pow(10, utils.ifndef(params.legend_num_decimal_digits,2));
				return Math.round(x * num_digit_multiplier) / num_digit_multiplier;
			});
			div.append('span').text(display_data_range[0]).classed('oncoprint-legend-label oncoprint-legend-element', true)
					.style('position', 'relative').style('bottom', cell_height / 2 - 3 + 'px');
			var mesh = 50;
			var svg = div.append('svg').attr('width', mesh+'px').attr('height', cell_height+'px').classed('oncoprint-legend-element', true);
			for (var i=0; i<=mesh; i++) {
				var t = i/mesh;
				var d = (1-t)*data_range[0] + t*data_range[1];
				var datum = makeDatum(d);
				svg.append('rect')
					.attr('width', '1px')
					.attr('height', cell_height+'px')
					.attr('fill', this.attrs.fill(datum))
					.attr('x', i+'px');
			}
			div.append('span').text(display_data_range[1]).classed('oncoprint-legend-label oncoprint-legend-element', true)
					.style('position', 'relative').style('bottom', cell_height / 2 - 3 + 'px');
			utils.d3SelectChildren(div, '*').style('padding-right', '10px');
			return div.node();
		};
		this.apply = function(g, cell_width, cell_height) {
			this.setUpHelperFunctions(this.data_range || (this.inferred_data_range = this.inferDataRange(g)));
			D3SVGRule.prototype.apply.call(this, g, cell_width, cell_height);
		};
	}
	D3SVGGradientRule.prototype = Object.create(D3SVGRule.prototype);

	function D3SVGStaticRule(params, rule_id) {
		D3SVGRule.call(this, params, rule_id);

		this.getLegendDiv = function(cell_width, cell_height) {
			if (!this.showInLegend()) {
				return;
			}
			var div = d3.select(document.createElement('div'));
			var svg_ctr = div.append('div').classed('oncoprint-legend-block', true);
			var svg = svg_ctr.append('svg').attr('width', cell_width+'px').attr('height', cell_height+'px').classed('oncoprint-legend-element', true);
			this.apply(svg, cell_width, cell_height);
			if (this.legend_label) {
				div.append('span').text(this.legend_label).classed('oncoprint-legend-label oncoprint-legend-element', true)
						.style('position', 'relative').style('bottom', cell_height / 2 - 3 + 'px');
			}
			utils.d3SelectChildren(div, '*').style('padding-right', '10px');
			return div.node();
		};
	}
	D3SVGStaticRule.prototype = Object.create(D3SVGRule.prototype);

	return {
		CATEGORICAL_COLOR: CATEGORICAL_COLOR,
		GRADIENT_COLOR: GRADIENT_COLOR,
		GENETIC_ALTERATION: GENETIC_ALTERATION,
		BAR_CHART: BAR_CHART,
		makeRuleSet: function(type, params) {
			if (type === CATEGORICAL_COLOR) {
				return new D3SVGCategoricalColorRuleSet(params);
			} else if (type === GRADIENT_COLOR) {
				return new D3SVGGradientColorRuleSet(params);
			} else if (type === GENETIC_ALTERATION) {
				return new D3SVGGeneticAlterationRuleSet(params);
			} else if (type === BAR_CHART) {
				return new D3SVGBarChartRuleSet(params);
			} else {
				return new D3SVGRuleSet();
			}
		}
	};
})();