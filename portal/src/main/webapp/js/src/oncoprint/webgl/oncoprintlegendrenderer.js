var shapeToSVG = require('./oncoprintshapetosvg.js');

var makeSVGElement = function (tag, attrs) {
    var el = document.createElementNS('http://www.w3.org/2000/svg', tag);
    for (var k in attrs) {
	if (attrs.hasOwnProperty(k)) {
	    el.setAttribute(k, attrs[k]);
	}
    }
    return el;
};

var OncoprintLegendView = (function() {
    function OncoprintLegendView($table, base_width, base_height) {
	this.$table = $table;
	this.base_width = base_width;
	this.base_height = base_height;
	this.rendering_suppressed = false;
    }
    
    var renderLegend = function(view, model) {
	if (view.rendering_suppressed) {
	    return;
	}
	view.$table.empty();
	var rule_sets = model.getRuleSets();
	for (var i=0; i<rule_sets.length; i++) {
	    if (rule_sets[i].exclude_from_legend) {
		continue;
	    }
	    var $row = $('<tr></tr>').appendTo(view.$table);
	    var $label_td = $('<td></td>').appendTo($row);
	    if (typeof rule_sets[i].legend_label !== 'undefined') {
		$('<p></p>').appendTo($label_td).css({'font-weight':'bold'}).text(rule_sets[i].legend_label);
	    }
	    var rules = model.getActiveRules(rule_sets[i].rule_set_id);
	    for (var j=0; j<rules.length; j++) {
		var rule = rules[j].rule;
		if (rule.exclude_from_legend) {
		    continue;
		}
		var $rule_td = $('<td></td>').appendTo($row);
		var $rule_svg = $('<svg width="'+view.base_width+'" height="'+view.base_height+'"></svg>').appendTo($rule_td);
		var config = rule.getLegendConfig();
		if (config.type === 'rule') {
		    var concrete_shapes = rule.apply(rules[j].target, model.getCellWidth(true), view.base_height);
		    var svg_elts = concrete_shapes.map(function(shape) {
			return shapeToSVG(shape, 0, 0);
		    });
		    if (typeof rule.legend_label !== 'undefined') {
			$('<p></p>').appendTo($rule_td).html(rule.legend_label);
		    }
		    for (var h=0; h<svg_elts.length; h++) {
			$rule_svg.append($(svg_elts[h]));
		    }
		} else if (config.type === 'number') {
		    $('<p></p>').appendTo($rule_td).html(config.range[0]);
		    $rule_svg.append(makeSVGElement('polygon', {'points':'0,20 40,20 40,0', 'fill':config.color}));
		    $('<p></p>').appendTo($rule_td).html(config.range[1]);
		}
	    }
	}
    };
    
    OncoprintLegendView.prototype.addTracks = function(model) {
	renderLegend(this, model);
    }
    
    OncoprintLegendView.prototype.shareRuleSet = function(model) {
	renderLegend(this, model);
    }
    
    OncoprintLegendView.prototype.suppressRendering = function() {
	this.rendering_suppressed = true;
    }
    
    OncoprintLegendView.prototype.releaseRendering = function(model) {
	this.rendering_suppressed = false;
	renderLegend(this, model);
    }
    
    return OncoprintLegendView;
})();

module.exports = OncoprintLegendView;