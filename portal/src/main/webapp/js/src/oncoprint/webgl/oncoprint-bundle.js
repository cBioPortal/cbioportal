(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
var OncoprintModel = require('./oncoprintmodel.js');
var OncoprintSVGCellView = require('./oncoprintsvgcellview.js');
//var OncoprintWebGLCellView = require('oncoprintwebglcellview.js');
var OncoprintLabelView = require('./oncoprintlabelview.js');
var OncoprintRuleSet = require('./oncoprintruleset.js');

var Oncoprint = (function() {
	// this is the controller
	var nextTrackId = (function() {
		var ctr = 0;
		return function() {
			ctr += 1;
			return ctr;
		}
	})();
	function Oncoprint($svg_dev, $canvas_dev) {
		this.model = new OncoprintModel();
		
		// Precisely one of the following should be uncommented
		this.cell_view = new OncoprintSVGCellView($svg_dev);
		// this.cell_view = new OncoprintWebGLCellView($canvas_dev)
		
		this.label_view = new OncoprintLabelView();
	}
	
	Oncoprint.prototype.addTrack = function(params) {
		// Update model
		var track_id = nextTrackId();
		this.model.addTrack(track_id, params.target_group,
				params.track_height, params.track_padding,
				params.data_id_key, params.tooltipFn,
				params.removable, params.label,
				params.sortCmpFn, params.sort_direction_changeable,
				params.data, OncoprintRuleSet(params.rule_set_params));
		// Update views
		this.cell_view.addTrack(this.model, track_id);
		this.label_view.addTrack(this.model, track_id);
				
		return track_id;
	}
	
	Oncoprint.prototype.removeTrack = function(track_id) {
		// Update model
		this.model.removeTrack(track_id);
		// Update views
		this.cell_view.removeTrack(this.model, track_id);
		this.label_view.removeTrack(this.model, track_id);
	}
	
	Oncoprint.prototype.getZoom = function() {
		return this.model.getZoom();
	}
	
	Oncoprint.prototype.setZoom = function(z) {
		// Update model
		this.model.setZoom(z);
		// Update views
		this.cell_view.setZoom(this.model, z);
	}
	
	Oncoprint.prototype.setTrackData = function(track_id, data) {
		this.model.setTrackData(track_id, data);
		this.cell_view.setTrackData(this.model, track_id);
	}
	
	Oncoprint.prototype.setRuleSet = function(track_id, rule_set_params) {
		this.model.setRuleSet(track_id, OncoprintRuleSet(rule_set_params));
		this.cell_view.setTrackData(this.model, track_id);
	}
	
	
	return Oncoprint;
})();
module.exports = Oncoprint;
},{"./oncoprintlabelview.js":2,"./oncoprintmodel.js":3,"./oncoprintruleset.js":4,"./oncoprintsvgcellview.js":5}],2:[function(require,module,exports){
// FIRST PASS: no optimization
var OncoprintLabelView = (function() {
	function OncoprintLabelView() {
		//TODO: what parameters
		// TODO: implementation
	}
	OncoprintLabelView.prototype.removeTrack = function(model, track_id) {
		// TODO: what parameters
		// TODO: implementation
	}
	OncoprintLabelView.prototype.moveTrack = function() {
		// TODO: what parameters
		// TODO: implementation
	}
	OncoprintLabelView.prototype.addTrack = function(model, track_id) {
		// TODO: what parameters
		// TODO: implementation
	}
	OncoprintLabelView.prototype.setTrackData = function() {
		// TODO: what parameters
		// TODO: implementation
	}
	OncoprintLabelView.prototype.setCellPadding = function() {
		// TODO: what parameters
		// TODO: implementation
	}
	OncoprintLabelView.prototype.setZoom = function() {
		// TODO: what parameters
		// TODO: implementation
	}
	OncoprintLabelView.prototype.setOrder = function() {
		// TODO: what parameters
		// TODO: implementation
	}
	
	return OncoprintLabelView;
})();

module.exports = OncoprintLabelView;
},{}],3:[function(require,module,exports){
function ifndef(x, val) {
	return (typeof x === "undefined" ? val : x);
}

var OncoprintModel = (function() {
	function OncoprintModel(init_cell_padding, init_cell_padding_on, 
				init_zoom, init_cell_width,
				init_track_group_padding) {
		this.ids = [];
		this.visible_ids = {};
		this.track_groups = [];
		this.zoom = ifndef(init_zoom, 1);
		
		this.cell_padding = ifndef(init_cell_padding, 10);
		this.cell_padding_on = ifndef(init_cell_padding_on, true);
		this.cell_width = ifndef(init_cell_width, 10);
		this.track_group_padding = ifndef(init_track_group_padding, 10);
		
		this.track_data = {};
		this.track_rule_set = {};
		this.track_label = {};
		this.track_height = {};
		this.track_padding = {};
		this.track_data_id_key = {};
		this.track_tooltip_fn = {};
		this.track_removable = {};
		this.track_sort_cmp_fn = {};
		this.track_sort_direction_changeable = {};
	}
	
	OncoprintModel.prototype.toggleCellPadding = function() {
		this.cell_padding_on = !this.cell_padding_on;
		return this.cell_padding_on;
	}
	
	OncoprintModel.prototype.getCellPadding = function() {
		return (this.cell_padding*this.zoom)*(+this.cell_padding_on);
	}
	
	OncoprintModel.prototype.getZoom = function() {
		return this.zoom;
	}
	
	OncoprintModel.prototype.setZoom = function(z) {
		if (z <= 1 && z >= 0) {
			this.zoom = z;
		}
		return this.zoom;
	}
	
	OncoprintModel.prototype.getCellWidth = function() {
		return this.cell_width * this.zoom;
	}
	
	OncoprintModel.prototype.getTrackHeight = function(track_id) {
		return this.track_height[track_id];
	}
	
	OncoprintModel.prototype.getTrackPadding = function(track_id) {
		return this.track_padding[track_id];
	}
	
	OncoprintModel.prototype.getIds = function() {
		return this.ids;
	}
	
	OncoprintModel.prototype.getVisibleIds = function() {
		var visible_ids = this.visible_ids;
		return this.ids.filter(function(id) {
			return !!visible_ids[id];
		});
	}
	
	OncoprintModel.prototype.setIds = function(ids) {
		this.ids = ids.slice();
	}
	
	OncoprintModel.prototype.hideIds = function(to_hide, show_others) {
		var ids = this.ids;
		if (show_others) {
			for (var i=0, len=ids.length; i<len; i++) {
				this.visible_ids[ids[i]] = true;
			}
		}
		for (var j=0, len=to_hide.length; j<len; j++) {
			this.visible_ids[to_hide[j]] = false;
		}
	}
	
	OncoprintModel.prototype.moveTrackGroup = function(from_index, to_index) {
		var new_groups = [];
		var group_to_move = this.track_groups[from_index];
		for (var i=0; i<this.track_groups.length; i++) {
			if (i !== from_index && i !== to_index) {
				new_groups.push(this.track_groups[i]);
			}
			if (i === to_index) {
				new_groups.push(group_to_move);
			}
		}
		this.track_groups = new_groups;
		return this.track_groups;
	}
	
	OncoprintModel.prototype.addTrack = function(track_id, target_group,
						track_height, track_padding,
						data_id_key, tooltipFn,
						removable, label,
						sortCmpFn, sort_direction_changeable,
						data, rule_set) {
		this.track_label[track_id] = ifndef(label, "Label");
		this.track_height[track_id] = ifndef(track_height, 20);
		this.track_padding[track_id] = ifndef(track_padding, 5);
		this.track_data_id_key[track_id] = ifndef(data_id_key, 'id');
		this.track_tooltip_fn[track_id] = ifndef(tooltipFn, function(d) { return d+''; });
		this.track_removable[track_id] = ifndef(removable, false);
		this.track_sort_cmp_fn[track_id] = ifndef(sortCmpFn, function(a,b) { return 0; });
		this.track_sort_direction_changeable[track_id] = ifndef(sort_direction_changeable, false);
		this.track_data[track_id] = ifndef(data, []);
		this.track_rule_set[track_id] = ifndef(rule_set, undefined);
		
		target_group = ifndef(target_group, 0);
		while (target_group >= this.track_groups.length) {
			this.track_groups.push([]);
		}
		this.track_groups[target_group].push(track_id);
	}
	
	var _getContainingTrackGroup = function(oncoprint_model, track_id, return_reference) {
		var group;
		for (var i=0; i<oncoprint_model.track_groups.length; i++) {
			if (oncoprint_model.track_groups[i].indexOf(track_id) > -1) {
				group = oncoprint_model.track_groups[i];
				break;
			}
		}
		if (group) {
			return (return_reference ? group : group.slice());
		} else {
			return undefined;
		}
	}
	
	OncoprintModel.prototype.removeTrack = function(track_id) {
		delete this.track_data[track_id];
		delete this.track_rule_set[track_id];
		delete this.track_label[track_id];
		delete this.track_height[track_id];
		delete this.track_padding[track_id];
		delete this.track_data_id_key[track_id];
		delete this.track_tooltip_fn[track_id];
		delete this.track_removable[track_id];
		delete this.track_sort_cmp_fn[track_id];
		delete this.track_sort_direction_changeable[track_id];
		
		var containing_track_group = _getContainingTrackGroup(this, track_id, true);
		if (containing_track_group) {
			containing_track_group.splice(
				containing_track_group.indexOf(track_id), 1);
		}
	}
	
	OncoprintModel.prototype.getContainingTrackGroup = function(track_id) {
		return _getContainingTrackGroup(this, track_id, false);
	}
	
	OncoprintModel.prototype.getTrackGroups = function() {
		// TODO: make read-only
		return this.track_groups;
	}
	
	OncoprintModel.prototype.getTracks = function() {
		var ret = [];
		for (var i=0; i<this.track_groups.length; i++) {
			for (var j=0; j<this.track_groups[i].length; j++) {
				ret.push(this.track_groups[i][j]);
			}
		}
		return ret;
	}
	
	OncoprintModel.prototype.moveTrack = function(track_id, new_position) {
		var track_group = _getContainingTrackGroup(this, track_id, true);
		if (track_group) {
			track_group.splice(track_group.indexOf(track_id), 1);
			track_group.splice(new_position, 0, track_id);
		}
	}
	
	OncoprintModel.prototype.getTrackLabel = function(track_id) {
		return this.track_label[track_id];
	}
	
	OncoprintModel.prototype.getTrackTooltipFn = function(track_id) {
		return this.track_tooltip_fn[track_id];
	}
	
	OncoprintModel.prototype.getTrackDataIdKey = function(track_id) {
		return this.track_data_id_key[track_id];
	}
	
	OncoprintModel.prototype.getTrackGroupPadding = function() {
		return this.track_group_padding;
	}
	
	OncoprintModel.prototype.isTrackRemovable = function(track_id) {
		return this.track_removable[track_id];
	}
	
	OncoprintModel.prototype.getRuleSet = function(track_id) {
		return this.track_rule_set[track_id];
	}
	
	OncoprintModel.prototype.setRuleSet = function(track_id, rule_set) {
		this.track_rule_set[track_id] = rule_set;
	}
	
	OncoprintModel.prototype.getTrackData = function(track_id) {
		return this.track_data[track_id];
	}
	
	OncoprintModel.prototype.setTrackData = function(track_id, data) {
		this.track_data[track_id] = data;
	}

	return OncoprintModel;
})();

module.exports = OncoprintModel;
},{}],4:[function(require,module,exports){
/* SHAPE SPEC
{
	'type':..,
	'x':..,
	'y':..,
	...
}
*/
// type: attrs
// rectangle: x, y, width, height, stroke, stroke-width, fill
// triangle: x1, y1, x2, y2, x3, y3, stroke, stroke-width, fill
// ellipse: x, y, width, height, stroke, stroke-width, fill
// line: x1, y1, x2, y2, stroke, stroke-width

/* Rule Params
 condition
 shapes - a list of Shape params
 legend_label
 exclude_from_legend
 
 Shape Params
 type (name of shape)
 ... then parameters from above ...
 */

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

var DEFAULT_GENETIC_ALTERATION_PARAMS = {
	'*': {
		shapes: [{
			'type': 'rectangle',
			'fill': 'rgba(211, 211, 211, 1)',
			}],
		exclude_from_legend: true,
		z: -1
	},
	'cna': {
		'AMPLIFIED': {
			shapes: [{
					'type': 'rectangle',
					'fill': 'rgba(255,0,0,1)',
					'x': '0%',
					'y': '0%',
					'width': '100%',
					'height': '100%',
				}],
			legend_label: 'Amplification',
			z: 0
		},
		'GAINED': {
			shapes: [{
					'type': 'rectangle',
					'fill': 'rgba(255,182,193,1)',
					'x': '0%',
					'y': '0%',
					'width': '100%',
					'height': '100%',
				}],
			legend_label: 'Gain',
			z: 0
		},
		'HOMODELETED': {
			shapes: [{
					'type': 'rectangle',
					'fill': 'rgba(0,0,255,1)',
					'x': '0%',
					'y': '0%',
					'width': '100%',
					'height': '100%',
				}],
			legend_label: 'Deep Deletion', 
			z: 0
		},
		'HEMIZYGOUSLYDELETED': {
			shapes: [{
					'type': 'rectangle',
					'fill': 'rgba(143, 216, 216,1)',
					'x': '0%',
					'y': '0%',
					'width': '100%',
					'height': '100%',
				}],
			legend_label: 'Shallow Deletion',
			z: 0
		}
	},
	'mrna': {
		'UPREGULATED': {
			shapes: [{
					'type': 'rectangle',
					'fill': 'rgba(0, 0, 0, 0)',
					'stroke': 'rgba(255, 153, 153, 1)',
					'x': '0%',
					'y': '0%',
					'width': '100%',
					'height': '100%',
				}],
			legend_label: 'mRNA Upregulation',
			z: 1
		},
		'DOWNREGULATED': {
			shapes: [{
					'type': 'rectangle',
					'fill': 'rgba(0, 0, 0, 0)',
					'stroke': 'rgba(102, 153, 204, 1)',
					'x': '0%',
					'y': '0%',
					'width': '100%',
					'height': '100%',
				}],
			legend_label: 'mRNA Downregulation',
			z: 1
		},
	},
	'rppa': {
		'UPREGULATED': {
			shapes: [{
					'type': 'triangle',
					'x1': '50%',
					'y1': '0%',
					'x2': '100%',
					'y2': '33.33%',
					'x3': '0%',
					'y3': '33.33%',
					'fill': 'rgba(0,0,0,1)'
				}],
			legend_label: 'Protein Upregulation',
			z: 2
		},
		'DOWNREGULATED': {
			shapes: [{
					'type': 'triangle',
					'x1': '50%',
					'y1': '100%',
					'x2': '100%',
					'y2': '66.66%',
					'x3': '0%',
					'y3': '66.66%',
					'fill': 'rgba(0,0,0,1)'
				}],
			legend_label: 'Protein Downregulation',
			z: 2
		}
	},
	'mut': {
		'MISSENSE': {
			shapes: [{
					'type': 'rectangle',
					'fill': 'rgba(0, 255, 0, 1)',
					'x': '0%',
					'y': '33.33%',
					'width': '100%',
					'height': '33.33%',
				}],
			legend_label: 'Missense Mutation',
			z: 3
		},
		'INFRAME': {
			shapes: [{
					'type': 'rectangle',
					'fill': 'rgba(159, 129, 112, 1)',
					'x': '0%',
					'y': '33.33%',
					'width': '100%',
					'height': '33.33%',
				}],
			legend_label: 'Inframe Mutation',
			z: 3
		},
		'TRUNC': {
			shapes: [{
					'type': 'rectangle',
					'fill': 'rgba(0, 0, 0, 1)',
					'x': '0%',
					'y': '33.33%',
					'width': '100%',
					'height': '33.33%',
				}],
			legend_label: 'Truncating Mutation',
			z: 3
		},
		'FUSION': {
			shapes: [{
					'type': 'triangle',
					'fill': 'rgba(0, 0, 0, 1)',
					'x1': '0%',
					'y1': '0%',
					'x2': '100%',
					'y2': '50%',
					'x3': '0%',
					'y3': '100%',
				}],
			legend_label: 'Fusion',
			z: 3
		}
	}
};

var RuleSet = (function () {
	var getRuleSetId = makeIdCounter();
	var getRuleId = makeIdCounter();

	function RuleSet(params) {
		/* params:
		 * - legend_label
		 * - exclude_from_legend
		 */
		this.rule_map = {};
		this.rule_set_id = getRuleSetId();
		this.z_map = {};
		this.legend_label = params.legend_label;
		this.exclude_from_legend = params.exclude_from_legend;
		this.recently_used_rule_ids = {};
	}

	RuleSet.prototype.getLegendLabel = function () {
		return this.legend_label;
	}

	RuleSet.prototype.getRuleSetId = function () {
		return this.rule_set_id;
	}
	
	RuleSet.prototype.addRules = function (list_of_params) {
		var self = this;
		return list_of_params.map(function(params) {
			return self.addRule(params);
		});
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
			return {id: rule_id, rule: self.getRule(rule_id)};
		});
	}

	RuleSet.prototype.isExcludedFromLegend = function () {
		return this.exclude_from_legend;
	}

	RuleSet.prototype.clearRecentlyUsedRules = function() {
		this.recently_used_rule_ids = {};
	}
	
	RuleSet.prototype.markRecentlyUsedRule = function(rule_id) {
		this.recently_used_rule_ids[rule_id] = true;
	}
	
	RuleSet.prototype.getRecentlyUsedRules = function() {
		var self = this;
		return Object.keys(this.recently_used_rule_ids).map(
			function(rule_id) {
				return self.getRule(rule_id);
			});
	}
	
	RuleSet.prototype.apply = function (data, cell_width, cell_height) {
		// Returns a list of lists of concrete shapes, in the same order as data
		this.clearRecentlyUsedRules();
		
		var rules = this.getRulesInRenderOrder();
		var rules_len = rules.length;
		var self = this;
		
		return data.map(function (d) {
			var concrete_shapes = [];
			for (var j = 0; j < rules_len; j++) {
				var rule_concrete_shapes = 
					rules[j].rule.getConcreteShapesInRenderOrder(
					d, cell_width, cell_height);
				if (rule_concrete_shapes.length > 0) {
					self.markRecentlyUsedRule(rules[j].id);
				}
				concrete_shapes = concrete_shapes.concat(
					rule_concrete_shapes);
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
		/* params
		 * - category_key
		 * - categoryToColor
		 */
		RuleSet.call(this, params);
		this.category_key = params.category_key;
		this.category_to_color = params.category_to_color;
		for (var category in this.category_to_color) {
			if (this.category_to_color.hasOwnProperty(category)) {
				addCategoryRule(this, category, this.category_to_color[category]);
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

	var addCategoryRule = function (ruleset, category, color) {
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
			if (!this.category_to_color.hasOwnProperty(category)) {
				var color = colors.pop();
				this.category_to_color[category] = color;
				addCategoryRule(this, category, color);
			}
		}
		// Then propagate the call up
		return RuleSet.prototype.apply.call(this, data, cell_width, cell_height);
	};
	
	return CategoricalRuleSet;
})();

var LinearInterpRuleSet = (function() {
	function LinearInterpRuleSet(params) {
		/* params
		 * - value_key
		 * - value_range
		 */
		RuleSet.call(this, params);
		this.value_key = params.value_key;
		this.value_range = params.value_range;
		this.inferred_value_range;
		
		this.addRule({
			condition: function(d) {
				return isNaN(d[params.value_key]);
			},
			shapes: NA_SHAPES,
			legend_label: NA_LABEL,
			exclude_from_legend: false
		});
		
		this.makeInterpFn = function() {
			var range = getEffectiveValueRange(this);
			if (range[0] === range[1]) {
				// Make sure non-zero denominator
				range[0] -= range[0]/2;
				range[1] += range[1]/2;
			}
			var range_spread = range[1] - range[0];
			var range_lower = range[0];
			return function(val) {
				return (val - range_lower) / range_spread;
			};
		};
	}
	LinearInterpRuleSet.prototype = Object.create(RuleSet.prototype);
	
	var getEffectiveValueRange = function(ruleset) {
		var ret = [ruleset.value_range[0], ruleset.value_range[1]];
		if (typeof ret[0] === "undefined") {
			ret[0] = ruleset.inferred_value_range[0];
		}
		if (typeof ret[1] === "undefined") {
			ret[1] = ruleset.inferred_value_range[1];
		}
		return ret;
	};
	
	LinearInterpRuleSet.prototype.apply = function(data, cell_width, cell_height) {
		// First find value range
		var value_min = Number.POSITIVE_INFINITY;
		var value_max = Number.NEGATIVE_INFINITY;
		for (var i = 0, datalen = data.length; i < datalen; i++) {
			var d = data[i];
			value_min = Math.min(value_min, d[this.value_key]);
			value_max = Math.max(value_max, d[this.value_key]);
		}
		this.inferred_value_range = [value_min, value_max];
		this.updateLinearRules();
		
		// Then propagate the call up
		return RuleSet.prototype.apply.call(this, data, cell_width, cell_height);
	};
	
	LinearInterpRuleSet.prototype.updateLinearRules = function() {
		throw "Not implemented in abstract class";
	};
	
	return LinearInterpRuleSet;
})();

var GradientRuleSet = (function() {
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
				color_start = [0,0,0,1];
				color_end = [255,0,0,1];
			}
			self.color_range = color_start.map(function(c, i) {
				return [c, color_end[i]];
			});
		})(this);
		console.log(this.color_range);
		this.gradient_rule;
		this.updateLinearRules();
			
	}
	GradientRuleSet.prototype = Object.create(LinearInterpRuleSet.prototype);
	
	GradientRuleSet.prototype.updateLinearRules = function() {
		if (typeof this.gradient_rule !== "undefined") {
			this.removeRule(this.gradient_rule);
		}
		var interpFn = this.makeInterpFn();
		var value_key = this.value_key;
		var color_range = this.color_range;
		this.gradient_rule = this.addRule({
			condition: function(d) {
				return !isNaN(d[value_key]);
			},
			shapes: [{
					type: 'rectangle',
					fill: function(d) {
						var t = interpFn(d[value_key]);
						return "rgba("+color_range.map(
							function(arr) {
								return (1-t)*arr[0] 
								+ t*arr[1];
						}).join(",")+")";
					}
				}],
			exclude_from_legend: false
		});
	};
	
	return GradientRuleSet;
})();

var BarRuleSet = (function() {
	function BarRuleSet(params) {
		LinearInterpRuleSet.call(this, params);
		this.bar_rule;
		this.fill = params.fill || 'rgba(0,0,255,1)';
		this.updateLinearRules();
	}
	BarRuleSet.prototype = Object.create(LinearInterpRuleSet.prototype);
	
	BarRuleSet.prototype.updateLinearRules = function() {
		if (typeof this.bar_rule !== "undefined") {
			this.removeRule(this.bar_rule);
		}
		var interpFn = this.makeInterpFn();
		var value_key = this.value_key;
		this.bar_rule = this.addRule({
			condition: function(d) {
				return !isNaN(d[value_key]);
			},
			shapes: [{
					type: 'rectangle',
					y: function(d) {
						var t = interpFn(d[value_key]);
						return (1-t)*100 + "%";
					},
					height: function(d) {
						var t = interpFn(d[value_key]);
						return t*100 + "%";
					},
					fill: this.fill
				}],
			exclude_from_legend: false
		});
	};
	
	return BarRuleSet;
})();

var GeneticAlterationRuleSet = (function() {
	function GeneticAlterationRuleSet(params) {
		/* params:
		 * - rule_params
		 */
		RuleSet.call(this, params);
		this.addRule({
			condition: function(d) {
				return d.hasOwnProperty(NA_STRING);
			},
			shapes: NA_SHAPES,
			legend_label: NA_LABEL,
			exclude_from_legend: false
		});
		(function addRules() {
			var rule_params = params.rule_params;
			for (var key in rule_params) {
				if (rule_params.hasOwnProperty(key)) {
					var key_rule_params = rule_params[key];
					if (key === '*') {
						this.addRule(rule_params['*']);
					} else {
						for (var value in key_rule_params) {
							if (key_rule_params.hasOwnProperty(value)) {
								var condition = (value === '*' ?
										function(d) { return d.hasOwnProperty(key); } :
										function(d) { return d[key] === value; });
								this.addRule(
									shallowExtend(key_rule_params[value], 
									{'condition': condition}));
							}
						}
					}
				}
			}
		})();
	}
	GeneticAlterationRuleSet.prototype = Object.create(RuleSet.prototype);
	
	return GeneticAlterationRuleSet;
})();

var Rule = (function () {
	function Rule(params) {
		this.condition = params.condition || function (d) {
			return true;
		};
		this.shapes = params.shapes.map(addDefaultAbstractShapeParams);
		this.legend_label = params.legend_label || "";
		this.exclude_from_legend = params.exclude_from_legend;
	}
	var addDefaultAbstractShapeParams = function (shape_params) {
		var default_values = {'width': '100%', 'height': '100%', 'x': '0%', 'y': '0%', 'z': 0,
			'x1': '0%', 'x2': '0%', 'x3': '0%', 'y1': '0%', 'y2': '0%', 'y3': '0%',
			'stroke': 'rgba(0,0,0,0)', 'fill': 'rgba(23,23,23,1)', 'stroke-width': '0'};
		var required_parameters_by_type = {
			'rectangle': ['width', 'height', 'x', 'y', 'stroke', 'fill', 'stroke-width'],
			'triangle': ['x1', 'x2', 'x3', 'y1', 'y2', 'y3', 'stroke', 'fill', 'stroke-width'],
			'ellipse': ['width', 'height', 'x', 'y', 'stroke', 'fill', 'stroke-width'],
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
		complete_shape_params.type = shape_params.type;
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

module.exports = function(params) {
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
},{}],5:[function(require,module,exports){
// FIRST PASS: no optimization
var OncoprintSVGCellView = (function() {
	function OncoprintSVGCellView($svg) {
		this.$svg = $svg;
		this.track_shapes = {};
	}
	OncoprintSVGCellView.prototype.removeTrack = function(model, track_id) {
		// TODO: what parameters
		// TODO: implementation
	}
	OncoprintSVGCellView.prototype.moveTrack = function() {
		// TODO: what parameters
		// TODO: implementation
	}
	
	var renderTrack = function(cell_view, model, track_id) {
		cell_view.track_shapes[track_id] = cell_view.track_shapes[track_id] || [];
		var track_shapes = cell_view.track_shapes[track_id];
		while (track_shapes.length > 0) {
			var elt = track_shapes.pop();
			elt.parentNode.removeChild(elt);
		}
		
		var y = cell_view.getTrackTop(model, track_id) + model.getTrackPadding(track_id);
		// Now y is the top of the cells
		var cell_width = model.getCellWidth();
		var cell_padding = model.getCellPadding();
		var cell_height = model.getTrackHeight(track_id);
		
		var shape_list_list = model.getRuleSet(track_id).apply(
						model.getTrackData(track_id),
						cell_width,
						cell_height);
		for (var i=0; i<shape_list_list.length; i++) {
			var x = i*(cell_width + cell_padding);
			var shape_list = shape_list_list[i];
			for (var j=0; j<shape_list.length; j++) {
				track_shapes.push(cell_view.renderShape(shape_list[j], x, y)); 
			}
		}
	}
	
	var renderTracks = function(cell_view, model) {
		var tracks = model.getTracks();
		for (var i=0; i<tracks.length; i++) {
			renderTrack(cell_view, model, tracks[i]);
		}
	}
	
	OncoprintSVGCellView.prototype.addTrack = function(model, track_id) {
		renderTrack(this, model, track_id);
	}
	
	OncoprintSVGCellView.prototype.renderShape = function(shape, x, y) {
		var tag;
		if (shape.type === 'rectangle') {
			tag = this.renderRectangle(shape, x, y);
		} else if (shape.type === 'triangle') {
			tag = this.renderTriangle(shape, x, y);
		} else if (shape.type === 'ellipse') {
			tag = this.renderEllipse(shape, x, y);
		} else if (shape.type === 'line') {
			tag = this.renderLine(shape, x, y);
		}
		return tag;
	}
	
	var makeSVGTag = function(tag, attrs) {
		var el= document.createElementNS('http://www.w3.org/2000/svg', tag);
		for (var k in attrs) {
			if (attrs.hasOwnProperty(k)) {
				el.setAttribute(k, attrs[k]);
			}
		}
		return el;
	}
	OncoprintSVGCellView.prototype.renderRectangle = function(rectangle, x, y) {
		var new_rect = makeSVGTag('rect', {
			'x': x+parseFloat(rectangle.x),
			'y': y+parseFloat(rectangle.y),
			'width': rectangle.width,
			'height': rectangle.height,
			'stroke': rectangle.stroke,
			'stroke-width': rectangle['stroke-width'],
			'fill': rectangle.fill
		});
		this.$svg[0].appendChild(new_rect);
		return new_rect;
	}
	
	OncoprintSVGCellView.prototype.renderTriangle = function(rectangle, x, y,
							cell_width, cell_height) {
		// TODO: implement
	}
	
	OncoprintSVGCellView.prototype.renderEllipse = function(rectangle, x, y,
							cell_width, cell_height) {
		// TODO: implement
	}
	
	OncoprintSVGCellView.prototype.renderLine = function(rectangle, x, y,
							cell_width, cell_height) {
		// TODO: implement
	}
	
	OncoprintSVGCellView.prototype.getTrackTop = function(model, track_id) {
		var groups = model.getTrackGroups();
		var y = 0;
		for (var i=0; i<groups.length; i++) {
			var group = groups[i];
			var found = false;
			for (var j=0; j<group.length; j++) {
				if (group[j] === track_id) {
					found = true;
					break;
				}
				y += 2*model.getTrackPadding(group[j]);
				y += model.getTrackHeight(group[j]);
			}
			y += model.getTrackGroupPadding();
			if (found) {
				break;
			}
		}
		return y;
	}
	OncoprintSVGCellView.prototype.setCellPadding = function() {
		// TODO: what parameters
		// TODO: implementation
	}
	
	
	OncoprintSVGCellView.prototype.setZoom = function(model, z) {
		renderTracks(this, model);
	}
	OncoprintSVGCellView.prototype.setOrder = function() {
		// TODO: what parameters
		// TODO: implementation
	}
	
	OncoprintSVGCellView.prototype.setTrackData = function(model, track_id) {
		renderTrack(this, model, track_id);
	}
	
	OncoprintSVGCellView.prototype.setRuleSet = function(model, track_id) {
		renderTrack(this, model, track_id);
	}
	
	return OncoprintSVGCellView;
})();

module.exports = OncoprintSVGCellView;
},{}],6:[function(require,module,exports){
$(document).ready(function() {
	var Oncoprint = require('./oncoprint.js');
	console.log($('#svg'));
	var o = new Oncoprint($('#svg'), $('#canvas'));
	var data = [{sample:'a', data:5}, {sample:'b', data:10}];
	while (data.length < 1000) {
		data = data.concat(data);
	}
	var rule_set_params = {
		type: 'bar',
		value_key: 'data',
		value_range:[0,10]
	};
	o.addTrack({'data':data, 'rule_set_params': rule_set_params});
	o.addTrack({'data':data, 'rule_set_params': rule_set_params});
	o.addTrack({'data':data, 'rule_set_params': rule_set_params});
	o.addTrack({'data':data, 'rule_set_params': rule_set_params, 'target_group':1});
	window.o = o;
});
},{"./oncoprint.js":1}]},{},[6]);
