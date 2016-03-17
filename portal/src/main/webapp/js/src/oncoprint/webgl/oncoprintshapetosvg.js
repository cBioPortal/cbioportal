var makeSVGElement = function (tag, attrs) {
    var el = document.createElementNS('http://www.w3.org/2000/svg', tag);
    for (var k in attrs) {
	if (attrs.hasOwnProperty(k)) {
	    el.setAttribute(k, attrs[k]);
	}
    }
    return el;
};

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

var rectangleToSVG = function (params, offset_x, offset_y) {
    var stroke_color = extractColor(params.stroke);
    var fill_color = extractColor(params.fill);
    return makeSVGElement('rect', {
	width: params.width,
	height: params.height,
	x: parseFloat(params.x) + offset_x,
	y: parseFloat(params.y) + offset_y,
	stroke: stroke_color.rgb,
	'stroke-opacity': stroke_color.opacity,
	'stroke-width': params['stroke-width'],
	fill: fill_color.rgb,
	'fill-opacity': fill_color.opacity
    });
};

var triangleToSVG = function (params, offset_x, offset_y) {
    var stroke_color = extractColor(params.stroke);
    var fill_color = extractColor(params.fill);
    return makeSVGElement('polygon', {
	points: [[parseFloat(params.x1) + offset_x, parseFloat(params.y1) + offset_y], [parseFloat(params.x2) + offset_x, parseFloat(params.y2) + offset_y], [parseFloat(params.x3) + offset_x, parseFloat(params.y3) + offset_y]].map(function (a) {
	    return a[0] + ',' + a[1];
	}).join(' '),
	stroke: stroke_color.rgb,
	'stroke-opacity': stroke_color.opacity,
	'stroke-width': params['stroke-width'],
	fill: fill_color.rgb,
	'fill-opacity': fill_color.opacity
    });
};

var ellipseToSVG = function (params, offset_x, offset_y) {
    var stroke_color = extractColor(params.stroke);
    var fill_color = extractColor(params.fill);
    return makeSVGElement('ellipse', {
	rx: parseFloat(params.width) / 2,
	height: parseFloat(params.height) / 2,
	cx: parseFloat(params.x) + offset_x,
	cy: parseFloat(params.y) + offset_y,
	stroke: stroke_color.rgb,
	'stroke-opacity': stroke_color.opacity,
	'stroke-width': params['stroke-width'],
	fill: fill_color.rgb,
	'fill-opacity': fill_color.opacity
    });
};

var lineToSVG = function (params, offset_x, offset_y) {
    var stroke_color = extractColor(params.stroke);
    return makeSVGElement('line', {
	x1: parseFloat(params.x1) + offset_x,
	y1: parseFloat(params.y1) + offset_y,
	x2: parseFloat(params.x2) + offset_x,
	y2: parseFloat(params.y2) + offset_y,
	stroke: stroke_color.rgb,
	'stroke-opacity': stroke_color.opacity,
	'stroke-width': params['stroke-width'],
    });
};

module.exports = function(oncoprint_shape_computed_params, offset_x, offset_y) {
    var type = oncoprint_shape_computed_params.type;
    if (type === 'rectangle') {
	return rectangleToSVG(oncoprint_shape_computed_params, offset_x, offset_y);
    } else if (type === 'triangle') {
	return triangleToSVG(oncoprint_shape_computed_params, offset_x, offset_y);
    } else if (type === 'ellipse') {
	return ellipseToSVG(oncoprint_shape_computed_params, offset_x, offset_y);
    } else if (type === 'line') {
	return lineToSVG(oncoprint_shape_computed_params, offset_x, offset_y);
    }
};