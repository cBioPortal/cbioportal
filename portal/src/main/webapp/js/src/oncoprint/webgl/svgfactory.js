var makeSVGElement = require('./makesvgelement.js');
var shapeToSVG = require('./oncoprintshapetosvg.js');
module.exports = {
    text: function(content,x,y,size,family,weight) {
	var elt = makeSVGElement('text', {
	    'x':(x || 0),
	    'y':(y || 0),
	    'font-size':(size || 12),
	    'font-family':(family || 'serif'),
	    'font-weight':(weight || 'normal'),
	    'text-anchor':'start',
	    'alignment-baseline':'text-before-edge',
	});
	elt.textContent = content;
	return elt;
    },
    group: function(x,y) {
	x = x || 0;
	y = y || 0;
	return makeSVGElement('g', {
	    'transform':'translate('+x+','+y+')',
	    'x':x,
	    'y':y
	});
    },
    svg: function(width, height) {
	return makeSVGElement('svg', {
	    'width':(width || 0), 
	    'height':(height || 0),
	});
    },
    fromShape: function(oncoprint_shape_computed_params, offset_x, offset_y) {
	return shapeToSVG(oncoprint_shape_computed_params, offset_x, offset_y);
    },
    polygon: function(points, fill) {
	return makeSVGElement('polygon', {'points': points, 'fill':fill});
    },
};


