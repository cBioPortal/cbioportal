var svgfactory = require('./svgfactory.js');

var nodeIsVisible = function(node) {
    var ret = true;
    while (node.tagName.toLowerCase() !== "html") {
	if ($(node).css('display') === 'none') {
	    ret = false;
	    break;
	}
	node = node.parentNode;
    }
    return ret;
};

var OncoprintLegendView = (function() {
    function OncoprintLegendView($div, base_width, base_height) {
	this.$div = $div;
	this.$svg = $(svgfactory.svg(200,200)).appendTo(this.$div);
	this.base_width = base_width;
	this.base_height = base_height;
	this.rendering_suppressed = false;
	
	this.width = $div.width();
	
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
    
    var renderLegend = function(view, model, target_svg, show_all) {
	if (view.rendering_suppressed) {
	    return;
	}
	if (typeof target_svg === 'undefined') {
	    target_svg = view.$svg[0];
	}
	if (!nodeIsVisible(target_svg)) {
	    return;
	}
	$(target_svg).empty();
	var defs = svgfactory.defs();
	target_svg.appendChild(defs);
	
	var everything_group = svgfactory.group(0,0);
	target_svg.appendChild(everything_group);
	
	var rule_sets = model.getRuleSets();
	var y = 0;
	var rule_start_x = 200;
	for (var i=0; i<rule_sets.length; i++) {
	    if (rule_sets[i].exclude_from_legend && !show_all) {
		continue;
	    }
	    var rule_set_group = svgfactory.group(0,y);
	    everything_group.appendChild(rule_set_group);
	    (function addLabel() {
		if ((typeof rule_sets[i].legend_label !== 'undefined') && rule_sets[i].legend_label.length > 0) {
		    var label = svgfactory.text(rule_sets[i].legend_label, 0, 0, 12, 'Arial', 'bold');
		    rule_set_group.appendChild(label);
		    svgfactory.wrapText(label, rule_start_x);
		}
	    })();
	    
	    var x = rule_start_x + view.padding_after_rule_set_label;
	    var in_group_y_offset = 0;
	    
	    var rules = model.getActiveRules(rule_sets[i].rule_set_id);
	    for (var j=0; j<rules.length; j++) {
		var rule = rules[j].rule;
		if (rule.exclude_from_legend) {
		    continue;
		}
		var group = ruleToSVGGroup(rule, view, model, target_svg, defs);
		group.setAttribute('transform', 'translate('+x+','+in_group_y_offset+')');
		rule_set_group.appendChild(group);
		if (x + group.getBBox().width > view.width) {
		    x = rule_start_x + view.padding_after_rule_set_label;
		    in_group_y_offset = rule_set_group.getBBox().height + view.padding_between_rule_set_rows;
		    group.setAttribute('transform', 'translate('+x+','+in_group_y_offset+')');
		}
		x += group.getBBox().width;
		x += view.padding_between_rules;
	    }
	    y += rule_set_group.getBBox().height;
	    y += 3*view.padding_between_rule_set_rows;
	}
	var everything_box = everything_group.getBBox();
	view.$svg[0].setAttribute('width', everything_box.width);
	view.$svg[0].setAttribute('height', everything_box.height);
    };
    
    var ruleToSVGGroup = function(rule, view, model, target_svg, target_defs) {
	var root = svgfactory.group(0,0);
	var config = rule.getLegendConfig();
	if (config.type === 'rule') {
	    var concrete_shapes = rule.apply(config.target, model.getCellWidth(true), view.base_height);
	    for (var i=0; i<concrete_shapes.length; i++) {
		root.appendChild(svgfactory.fromShape(concrete_shapes[i], 0, 0));
	    }
	    if (typeof rule.legend_label !== 'undefined') {
		var font_size = 12;
		var text_node = svgfactory.text(rule.legend_label, model.getCellWidth(true) + 5, view.base_height/2, font_size, 'Arial', 'normal');
		target_svg.appendChild(text_node);
		var height = text_node.getBBox().height;
		text_node.setAttribute('y', parseFloat(text_node.getAttribute('y')) - height/2);
		target_svg.removeChild(text_node);
		root.appendChild(text_node);
	    }
	} else if (config.type === 'number') {
	    var num_decimal_digits = 2;
	    var display_range = config.range.map(function(x) {
		var num_digit_multiplier = Math.pow(10, num_decimal_digits);
		return Math.round(x * num_digit_multiplier) / num_digit_multiplier;
	    });
	    root.appendChild(svgfactory.text(display_range[0], 0, 0, 12, 'Arial', 'normal'));
	    root.appendChild(svgfactory.text(display_range[1], 50, 0, 12, 'Arial', 'normal'));
	    var mesh = 100;
	    var points = [];
	    points.push([5, 20]);
	    for (var i=0; i<mesh; i++) {
		var t = i/mesh;
		var h = config.interpFn((1-t)*config.range[0] + t*config.range[1]);
		var height = 20*h;
		points.push([5 + 40*i/mesh, 20-height]);
	    }
	    points.push([45, 20]);
	    root.appendChild(svgfactory.path(points, config.color, config.color));
	} else if (config.type === 'gradient') {
	    var num_decimal_digits = 2;
	    var display_range = config.range.map(function(x) {
		var num_digit_multiplier = Math.pow(10, num_decimal_digits);
		return Math.round(x * num_digit_multiplier) / num_digit_multiplier;
	    });
	    var gradient = svgfactory.gradient(config.colorFn);
	    var gradient_id = gradient.getAttribute("id");
	    target_defs.appendChild(gradient);
	    root.appendChild(svgfactory.text(display_range[0], 0, 0, 12, 'Arial', 'normal'));
	    root.appendChild(svgfactory.text(display_range[1], 120, 0, 12, 'Arial', 'normal'));
	    root.appendChild(svgfactory.rect(30,0,60,20,"url(#"+gradient_id+")"));
	}
	return root;
    };
    
    OncoprintLegendView.prototype.setWidth = function(w, model) {
	this.width = w;
	renderLegend(this, model);
    }
    OncoprintLegendView.prototype.removeTrack = function(model) {
	renderLegend(this, model);
    }
    
    OncoprintLegendView.prototype.addTracks = function(model) {
	renderLegend(this, model);
    }
    
    OncoprintLegendView.prototype.setTrackData = function(model) {
	renderLegend(this, model);
    }
    
    OncoprintLegendView.prototype.shareRuleSet = function(model) {
	renderLegend(this, model);
    }
    
    OncoprintLegendView.prototype.setRuleSet = function(model) {
	renderLegend(this, model);
    }
    
    OncoprintLegendView.prototype.hideTrackLegends = function(model) {
	renderLegend(this, model);
    }
    
    OncoprintLegendView.prototype.showTrackLegends = function(model) {
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
	renderLegend(this, model, root, true);
	root.parentNode.removeChild(root);
	return root;
    }
    
    return OncoprintLegendView;
})();

module.exports = OncoprintLegendView;