var halfsqrt2 = Math.sqrt(2) / 2;

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

var addVertexColor = function (vertex_color_array, rgba_str, n_times) {
    var color = extractRGBA(rgba_str);
    for (var h = 0; h < n_times; h++) {
	vertex_color_array.push(color[0], color[1], color[2], color[3]);
    }
};
    
var rectangleToVertexes = function(params, z_index, target_position_array, target_color_array) {
    // Stroke
    var x = parseFloat(params.x), y = parseFloat(params.y), height = parseFloat(params.height), width = parseFloat(params.width);
    var stroke_width = parseFloat(params['stroke-width']);

    target_position_array.push(x, y, z_index);
    target_position_array.push(x + width, y, z_index);
    target_position_array.push(x + width, y + height, z_index);

    target_position_array.push(x, y, z_index);
    target_position_array.push(x + width, y + height, z_index);
    target_position_array.push(x, y + height, z_index);

    addVertexColor(target_color_array, params.fill, 6);

    if (stroke_width > 0) {
	// left side
	target_position_array.push(x, y, z_index);
	target_position_array.push(x + stroke_width, y, z_index);
	target_position_array.push(x + stroke_width, y + height, z_index);

	target_position_array.push(x, y, z_index);
	target_position_array.push(x + stroke_width, y + height, z_index);
	target_position_array.push(x, y + height, z_index);

	// right side
	target_position_array.push(x + width, y, z_index);
	target_position_array.push(x + width - stroke_width, y, z_index);
	target_position_array.push(x + width - stroke_width, y + height, z_index);

	target_position_array.push(x + width, y, z_index);
	target_position_array.push(x + width - stroke_width, y + height, z_index);
	target_position_array.push(x + width, y + height, z_index);

	// top side
	target_position_array.push(x, y, z_index);
	target_position_array.push(x + width, y, z_index);
	target_position_array.push(x + width, y + stroke_width, z_index);

	target_position_array.push(x, y, z_index);
	target_position_array.push(x + width, y + stroke_width, z_index);
	target_position_array.push(x, y + stroke_width, z_index);

	// bottom side
	target_position_array.push(x, y + height, z_index);
	target_position_array.push(x + width, y + height, z_index);
	target_position_array.push(x + width, y + height - stroke_width, z_index);

	target_position_array.push(x, y + height, z_index);
	target_position_array.push(x + width, y + height - stroke_width, z_index);
	target_position_array.push(x, y + height - stroke_width, z_index);

	addVertexColor(target_color_array, params.stroke, 6 * 4);
    }
};
var triangleToVertexes = function(params, z_index, target_position_array, target_color_array) {
    target_position_array.push(parseFloat(params.x1), parseFloat(params.y1), z_index);
    target_position_array.push(parseFloat(params.x2), parseFloat(params.y2), z_index);
    target_position_array.push(parseFloat(params.x3), parseFloat(params.y3), z_index);

    addVertexColor(target_color_array, params.fill, 3);
};
var ellipseToVertexes = function(params, z_index, target_position_array, target_color_array) {
    var center = {x: parseFloat(params.x) + parseFloat(params.width) / 2, y: parseFloat(params.y) + parseFloat(params.height) / 2};
    var horzrad = parseFloat(params.width) / 2;
    var vertrad = parseFloat(params.height) / 2;

    target_position_array.push(center.x, center.y, z_index);
    target_position_array.push(center.x + horzrad, center.y, z_index);
    target_position_array.push(center.x + halfsqrt2 * horzrad, center.y + halfsqrt2 * vertrad, z_index);

    target_position_array.push(center.x, center.y, z_index);
    target_position_array.push(center.x + halfsqrt2 * horzrad, center.y + halfsqrt2 * vertrad, z_index);
    target_position_array.push(center.x, center.y + vertrad, z_index);

    target_position_array.push(center.x, center.y, z_index);
    target_position_array.push(center.x, center.y + vertrad, z_index);
    target_position_array.push(center.x - halfsqrt2 * horzrad, center.y + halfsqrt2 * vertrad, z_index);

    target_position_array.push(center.x, center.y, z_index);
    target_position_array.push(center.x - halfsqrt2 * horzrad, center.y + halfsqrt2 * vertrad, z_index);
    target_position_array.push(center.x - horzrad, center.y, z_index);

    target_position_array.push(center.x, center.y, z_index);
    target_position_array.push(center.x - horzrad, center.y, z_index);
    target_position_array.push(center.x - halfsqrt2 * horzrad, center.y - halfsqrt2 * vertrad, z_index);

    target_position_array.push(center.x, center.y, z_index);
    target_position_array.push(center.x - halfsqrt2 * horzrad, center.y - halfsqrt2 * vertrad, z_index);
    target_position_array.push(center.x, center.y - vertrad, z_index);

    target_position_array.push(center.x, center.y, z_index);
    target_position_array.push(center.x, center.y - vertrad, z_index);
    target_position_array.push(center.x + halfsqrt2 * horzrad, center.y - halfsqrt2 * vertrad, z_index);

    target_position_array.push(center.x, center.y, z_index);
    target_position_array.push(center.x + halfsqrt2 * horzrad, center.y - halfsqrt2 * vertrad, z_index);
    target_position_array.push(center.x + horzrad, center.y, z_index);

    addVertexColor(target_color_array, params.fill, 3 * 8);
};
var lineToVertexes = function(params, z_index, target_position_array, target_color_array) {
    // For simplicity of dealing with webGL we'll implement lines as thin triangle pairs
    var x1 = parseFloat(params.x1);
    var x2 = parseFloat(params.x2);
    var y1 = parseFloat(params.y1);
    var y2 = parseFloat(params.y2);

    if (x1 !== x2) {
	// WLOG make x1,y1 the one on the left
	if (Math.min(x1, x2) === x2) {
	    var tmpx1 = x1;
	    var tmpy1 = y1;
	    x1 = x2;
	    y1 = y2;
	    x2 = tmpx1;
	    y2 = tmpy1;
	}
    }

    var perpendicular_vector = [y2 - y1, x1 - x2];
    var perpendicular_vector_length = Math.sqrt(perpendicular_vector[0] * perpendicular_vector[0] + perpendicular_vector[1] * perpendicular_vector[1]);
    var unit_perp_vector = [perpendicular_vector[0] / perpendicular_vector_length, perpendicular_vector[1] / perpendicular_vector_length];

    var half_stroke_width = parseFloat(params['stroke-width']) / 2;
    var direction1 = [unit_perp_vector[0] * half_stroke_width, unit_perp_vector[1] * half_stroke_width];
    var direction2 = [direction1[0] * -1, direction1[1] * -1];
    var A = [x1 + direction1[0], y1 + direction1[1]];
    var B = [x1 + direction2[0], y1 + direction2[1]];
    var C = [x2 + direction1[0], y2 + direction1[1]];
    var D = [x2 + direction2[0], y2 + direction2[1]];

    target_position_array.push(A[0], A[1], z_index);
    target_position_array.push(B[0], B[1], z_index);
    target_position_array.push(C[0], C[1], z_index);

    target_position_array.push(C[0], C[1], z_index);
    target_position_array.push(D[0], D[1], z_index);
    target_position_array.push(B[0], B[1], z_index);

    addVertexColor(target_color_array, params.stroke, 3 * 2);
};
module.exports = function(oncoprint_shape_computed_params, z_index, target_position_array, target_color_array) {
    // target_position_array is an array with 3-d float vertexes
    // target_color_array is an array with rgba values in [0,1]
    // We pass them in to save on concatenation costs
    
    var type = oncoprint_shape_computed_params.type;
    if (type === "rectangle") {
	return rectangleToVertexes(oncoprint_shape_computed_params, z_index, target_position_array, target_color_array);
    } else if (type === "triangle") {
	return triangleToVertexes(oncoprint_shape_computed_params, z_index, target_position_array, target_color_array);
    } else if (type === "ellipse") {
	return ellipseToVertexes(oncoprint_shape_computed_params, z_index, target_position_array, target_color_array);
    } else if (type === "line") {
	return lineToVertexes(oncoprint_shape_computed_params, z_index, target_position_array, target_color_array);
    }
}