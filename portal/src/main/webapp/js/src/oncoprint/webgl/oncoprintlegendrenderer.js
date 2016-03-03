var svgfactory = require('./svgfactory.js');

var OncoprintLegendView = (function() {
    function OncoprintLegendView($div, base_width, base_height) {
	this.$div = $div;
	this.$svg = $(svgfactory.svg(200,200)).appendTo(this.$div);
	this.base_width = base_width;
	this.base_height = base_height;
	this.rendering_suppressed = false;
	
	this.rule_set_label_config = {
	    weight: 'bold',
	    size: 12,
	    font: 'Arial'
	};
	this.rule_label_config = {
	    weight: 'normal',
	    size: 12,
	    font: 'Arial'
	};
	
	this.padding_after_rule_set_label = 10;
	this.padding_between_rules = 20;
	this.padding_between_rule_set_rows = 10;
    }
    
    var getMaximumLabelWidth = function(view, model) {
	var rule_sets = model.getRuleSets();
	var maximum = 0;
	for (var i=0; i<rule_sets.length; i++) {
	    if (rule_sets[i].exclude_from_legend || typeof rule_sets[i].legend_label === 'undefined') {
		continue;
	    }
	    
	    var label = svgfactory.text(rule_sets[i].legend_label, 0, 0, 
					view.rule_set_label_config.size, 
					view.rule_set_label_config.font,
					view.rule_set_label_config.weight);
	    view.$svg[0].appendChild(label);
	    maximum = Math.max(maximum, label.getBBox().width);
	    label.parentNode.removeChild(label);
	}
	return maximum;
    };
    var renderLegend = function(view, model, target_svg) {
	if (view.rendering_suppressed) {
	    return;
	}
	if (typeof target_svg === 'undefined') {
	    target_svg = view.$svg[0];
	}
	$(target_svg).empty();
	
	var everything_group = svgfactory.group(0,0);
	target_svg.appendChild(everything_group);
	
	var rule_sets = model.getRuleSets();
	var y = 0;
	var rule_start_x = getMaximumLabelWidth(view, model);
	for (var i=0; i<rule_sets.length; i++) {
	    if (rule_sets[i].exclude_from_legend) {
		continue;
	    }
	    var rule_set_group = svgfactory.group(0,y);
	    everything_group.appendChild(rule_set_group);
	    (function addLabel() {
		if (rule_sets[i].legend_label && rule_sets[i].legend_label.length > 0) {
		    var label = svgfactory.text(rule_sets[i].legend_label, 0, y, 12, 'Arial', 'bold');
		    rule_set_group.appendChild(label);
		}
	    })();
	    
	    var x = rule_start_x + view.padding_after_rule_set_label;
	    
	    var rules = model.getActiveRules(rule_sets[i].rule_set_id);
	    for (var j=0; j<rules.length; j++) {
		var rule = rules[j].rule;
		if (rule.exclude_from_legend) {
		    continue;
		}
		var group = ruleToSVGGroup(rule, view, model);
		group.setAttribute('transform', 'translate('+x+','+y+')');
		rule_set_group.appendChild(group);
		x += group.getBBox().width;
		x += view.padding_between_rules;
	    }
	    y += rule_set_group.getBBox().height;
	    y += view.padding_between_rule_set_rows;
	}
	var everything_box = everything_group.getBBox();
	view.$svg[0].setAttribute('width', everything_box.width);
	view.$svg[0].setAttribute('height', everything_box.height);
    };
    
    var ruleToSVGGroup = function(rule, view, model) {
	var root = svgfactory.group(0,0);
	var config = rule.getLegendConfig();
	if (config.type === 'rule') {
	    var concrete_shapes = rule.apply(config.target, model.getCellWidth(true), view.base_height);
	    for (var i=0; i<concrete_shapes.length; i++) {
		root.appendChild(svgfactory.fromShape(concrete_shapes[i], 0, 0));
	    }
	    if (typeof rule.legend_label !== 'undefined') {
		root.appendChild(svgfactory.text(rule.legend_label, model.getCellWidth(true) + 5, view.base_height/2, 12, 'Arial', 'normal'));
	    }
	} else if (config.type === 'number') {
	    root.appendChild(svgfactory.text(config.range[0], 0, 0, 12, 'Arial', 'normal'));
	    root.appendChild(svgfactory.text(config.range[1], 50, 0, 12, 'Arial', 'normal'));
	    root.appendChild(svgfactory.polygon('5,20 45,20 45,0', config.color));
	}
	return root;
    };
    
    OncoprintLegendView.prototype.removeTrack = function(model) {
	renderLegend(this, model);
    }
    
    OncoprintLegendView.prototype.addTracks = function(model) {
	renderLegend(this, model);
    }
    
    OncoprintLegendView.prototype.shareRuleSet = function(model) {
	renderLegend(this, model);
    }
    
    OncoprintLegendView.prototype.setRuleSet = function(model) {
	renderLegend(this, model);
    }
    
    OncoprintLegendView.prototype.hideTrackLegend = function(model) {
	renderLegend(this, model);
    }
    
    OncoprintLegendView.prototype.showTrackLegend = function(model) {
	renderLegend(this, model);
    }
    
    OncoprintLegendView.prototype.suppressRendering = function() {
	this.rendering_suppressed = true;
    }
    
    OncoprintLegendView.prototype.releaseRendering = function(model) {
	this.rendering_suppressed = false;
	renderLegend(this, model);
    }
    
    OncoprintLegendView.prototype.toSVGGroup = function(model, offset_x, offset_y) {
	var root = svgfactory.group((offset_x || 0), (offset_y || 0));
	this.$svg.append(root);
	renderLegend(this, model, root);
	root.parentNode.removeChild(root);
	return root;
    }
    
    return OncoprintLegendView;
})();

module.exports = OncoprintLegendView;