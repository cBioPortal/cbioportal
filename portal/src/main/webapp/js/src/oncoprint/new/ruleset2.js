exports = {};


/* SHAPE SPEC */
// type: attrs
// rectangle: x, y, width, height, stroke, stroke-width, fill
// triangle: x1, y1, x2, y2, x3, y3, stroke, stroke-width, fill
// ellipse: x, y, width, height, stroke, stroke-width, fill
// line: x1, y1, x2, y2, stroke, stroke-width

function makeIdCounter() {
  var id = 0;
  return function() {
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
*/
var RuleSet = (function() {
  var getRuleSetId = makeIdCounter();
  var getRuleId = makeIdCounter();

  function RuleSet(params) {
    this.rule_map = {};
    this.rule_set_id = getRuleSetId();
    this.z_map = {};
    this.legend_label = params.legend_label;
    this.exclude_from_legend = params.exclude_from_legend;
  }

  RuleSet.prototype.getLegendLabel = function() {
    return this.legend_label;
  }

  RuleSet.prototype.getRuleSetId = function() {
    return this.rule_set_id;
  }

  RuleSet.prototype.addRule = function(params) {
    var rule_id = getRuleId();
    var z = (typeof params.z === "undefined" ? rule_id : params.z);
    this.rule_map[rule_id] = new Rule(params);
    this.z_map[rule_id] = z;
    return rule_id;
  }

  RuleSet.prototype.removeRule = function(rule_id) {
    delete this.rule_map[rule_id];
  }

  RuleSet.prototype.getRule = function(rule_id) {
    return this.rule_map[rule_id];
  }

  RuleSet.prototype.getRulesInRenderOrder = function() {
    var self = this;
    return Object.keys(this.rule_map).sort(function(rule_id1, rule_id2) {
      if (self.z_map[rule_id1] < self.z_map[rule_id2]) {
        return -1;
      } else if (self.z_map[rule_id1] > self.z_map[rule_id2]) {
        return 1;
      } else {
        return 0;
      }
    }).map(function(rule_id) {
      return self.getRule(rule_id);
    });
  }

  RuleSet.prototype.isExcludedFromLegend = function() {
    return this.exclude_from_legend;
  }

  RuleSet.prototype.apply = function(data, cell_width, cell_height) {
    // Returns a list of lists of drawable shapes, in the same order as data
    var rules = this.getRulesInRenderOrder();
    var rules_len = rules.length;
    return data.map(function(d) {
      var drawable_shapes = [];
      for (var j = 0; j < rules_len; j++) {
        drawable_shapes = drawable_shapes.concat(
              rules[j].getDrawableShapesInRenderOrder(data[i],
                                                      cell_width,
                                                      cell_height));
      }
      return drawable_shapes;
    });
  }
  return RuleSet;
})();

var CategoricalRuleSet = (function() {
  var colors = ["#3366cc","#dc3912","#ff9900","#109618",
      "#990099","#0099c6","#dd4477","#66aa00",
      "#b82e2e","#316395","#994499","#22aa99",
      "#aaaa11","#6633cc","#e67300","#8b0707",
      "#651067","#329262","#5574a6","#3b3eac",
      "#b77322","#16d620","#b91383","#f4359e",
      "#9c5935","#a9c413","#2a778d","#668d1c",
      "#bea413","#0c5922","#743411"]; // Source: D3
  function CategoricalRuleSet(params) {
    RuleSet.call(this, params);
    this.type = "categorical";
    this.getCategory = params.getCategory;
    this.categoryToColor = params.categoryToColor;
  }

  CategoricalRuleSet.
})();

var Rule = (function() {
  function Rule(params) {
      this.condition = params.condition || function(d) { return true; };
      this.shapes = params.shapes;
      this.legend_label = params.legend_label;
      this.exclude_from_legend = params.exclude_from_legend
  }

  Rule.prototype.getDrawableShapesInRenderOrder = function(d, cell_width, cell_height) {
    // Returns a list of drawable shapes in z-order, or none if the condition is not met
    if (!this.condition(d)) {
      return [];
    }
    var drawable_shapes = [];
    var width_axis_attrs = {"x":true,"x1":true,"x2":true,"x3":true,"width":true};
    var height_axis_attrs = {"y":true,"y1":true,"y2":true,"y3":true,"height":true};
    for (var i = 0, shapes_len = this.shapes.length; i < shapes_len; i++) {
      var shape_spec = this.shapes[i];
      var attrs = Object.keys(shape_spec);
      var drawable_shape = {};
      for (var j = 0, attrs_len = attrs.length; j < attrs_len; j++) {
          var attr_name = attrs[j];
          var attr_val = shape_spec[attr_name];
          if (typeof attr_val === 'function') {
            attr_val = attr_val(d);
          }
          var percent = (typeof attr_val === 'string') && attr_val.match(/([\d.]+)%/);
          percent = percent && percent.length > 1 && percent[1];
          if (percent) {
            var multiplier = parseFloat(percent)/100.0;
            if (width_axis_attrs.hasOwnProperty(attr_name)) {
              attr_val = multiplier * cell_width;
            } else if (height_axis_attrs.hasOwnProperty(attr_name)) {
              attr_val = multiplier * cell_height
            }
          }
          drawable_shape[attr_name] = attr_val+'';
      }
      drawable_shapes.push(drawable_shape);
    }
    return drawable_shapes.sort(function(shape1, shape2) {
      if (shape1.z < shape2.z) {
        return -1;
      } else if (shape1.z > shape2.z) {
        return 1;
      } else {
        return 0;
      }
    });
  }

  Rule.prototype.isExcludedFromLegend = function() {
    return this.exclude_from_legend;
  }
  return Rule;
})();
