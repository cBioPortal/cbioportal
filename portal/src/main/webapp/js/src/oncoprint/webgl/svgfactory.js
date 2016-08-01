var makeSVGElement = require('./makesvgelement.js');
var shapeToSVG = require('./oncoprintshapetosvg.js');

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
    if (match && match.length === 5) {
	ret = [parseFloat(match[1]) / 255,
	    parseFloat(match[2]) / 255,
	    parseFloat(match[3]) / 255,
	    parseFloat(match[4])];
    }
    return ret;
};

var extractColor = function(str) {
    if (str.indexOf("rgb(") > -1) {
	return {
	    'rgb': str,
	    'opacity': 1
	};
    }
    var rgba_arr = extractRGBA(str);
    return {
	'rgb': 'rgb('+rgba_arr[0]*255+','+rgba_arr[1]*255+','+rgba_arr[2]*255+')',
	'opacity': rgba_arr[3]
    };
};

module.exports = {
    text: function(content,x,y,size,family,weight,alignment_baseline) {
	size = size || 12;
	var alignment_baseline_y_offset = size;
	if (alignment_baseline === "middle") {
	    alignment_baseline_y_offset = size/2;
	} else if (alignment_baseline === "bottom") {
	    alignment_baseline_y_offset = 0;
	}
	var elt = makeSVGElement('text', {
	    'x':(x || 0),
	    'y':(y || 0) + alignment_baseline_y_offset,
	    'font-size':size,
	    'font-family':(family || 'serif'),
	    'font-weight':(weight || 'normal'),
	    'text-anchor':'start',
	});
	elt.textContent = content + '';
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
    wrapText: function(in_dom_text_svg_elt, width) {
	var text = in_dom_text_svg_elt.textContent;
	in_dom_text_svg_elt.textContent = "";
	
	var words = text.split(" ");
	var dy = 0;
	var tspan = makeSVGElement('tspan', {'x':'0', 'dy':dy});
	in_dom_text_svg_elt.appendChild(tspan);
	
	var curr_tspan_words = [];
	for (var i=0; i<words.length; i++) {
	    curr_tspan_words.push(words[i]);
	    tspan.textContent = curr_tspan_words.join(" ");
	    if (tspan.getComputedTextLength() > width) {
		tspan.textContent = curr_tspan_words.slice(0, curr_tspan_words.length-1).join(" ");
		dy = in_dom_text_svg_elt.getBBox().height;
		curr_tspan_words = [words[i]];
		tspan = makeSVGElement('tspan', {'x':'0', 'dy':dy});
		in_dom_text_svg_elt.appendChild(tspan);
		tspan.textContent = words[i];
	    }
	}
    },
    fromShape: function(oncoprint_shape_computed_params, offset_x, offset_y) {
	return shapeToSVG(oncoprint_shape_computed_params, offset_x, offset_y);
    },
    polygon: function(points, fill) {
	return makeSVGElement('polygon', {'points': points, 'fill':fill});
    },
    rect: function(x,y,width,height,fill) {
	return makeSVGElement('rect', {'x':x, 'y':y, 'width':width, 'height':height, 'fill':fill});
    },
    bgrect: function(width, height, fill) {
	return makeSVGElement('rect', {'width':width, 'height':height, 'fill':fill});
    },
    path: function(points, stroke, fill) {
	points = points.map(function(pt) { return pt.join(","); });
	points[0] = 'M'+points[0];
	for (var i=1; i<points.length; i++) {
	    points[i] = 'L'+points[i];
	}
	stroke = extractColor(stroke);
	fill = extractColor(fill);
	return makeSVGElement('path', {
	    'd': points.join(" "),
	    'stroke': stroke.rgb,
	    'stroke-opacity': stroke.opacity,
	    'fill': fill.rgb,
	    'fill-opacity': fill.opacity
	});
    }
};


