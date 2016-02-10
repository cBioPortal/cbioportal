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
    function OncoprintLegendView($svg, base_width, base_height) {
	this.$svg = $svg;
	this.base_width = base_width;
	this.base_height = base_height;
    }
    
    var renderLegend = function(view, model) {
	view.$svg.empty();
	var rule_sets = model.getRuleSets();
	var y = 0;
	for (var i=0; i<rule_sets.length; i++) {
	    var x = 0;
	    if (rule_sets[i].exclude_from_legend) {
		continue;
	    }
	    if (typeof rule_sets[i].legend_label !== 'undefined') {
		var rule_set_label = makeSVGElement('text', {'x':x, 'y':y+5});
		rule_set_label.textContent = rule_sets[i].legend_label;
		view.$svg.append(rule_set_label);
	    }
	    var rules = model.getActiveRules(rule_sets[i].rule_set_id);
	    for (var j=0; j<rules.length; j++) {
		var rule = rules[j].rule;
		if (rule.exclude_from_legend) {
		    continue;
		}
		var config = rule.getLegendConfig();
		if (config.type === 'rule') {
		    var concrete_shapes = rule.apply(rules[j].target, model.getCellWidth(true), view.base_height);
		    var svg_elts = concrete_shapes.map(function(shape) {
			return shapeToSVG(shape, x, y);
		    });
		    if (typeof rule.legend_label !== 'undefined') {
			var rule_label = makeSVGElement('text', {'x':x, 'y':y+5});
			rule_label.textContent = rule.legend_label;
			view.$svg.append(rule_label);
		    }
		    for (var h=0; h<svg_elts.length; h++) {
			view.$svg.append($(svg_elts[h]));
		    }
		} else if (config.type === 'number') {
		    
		}
		x += 20;
	    }
	    y += 20;
	}
    };
    
    OncoprintLegendView.prototype.addTracks = function(model) {
	renderLegend(this, model);
    }
    
    OncoprintLegendView.prototype.shareRuleSet = function(model) {
	renderLegend(this, model);
    }
    
    return OncoprintLegendView;
})();

module.exports = OncoprintLegendView;