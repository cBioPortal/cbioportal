var gl_matrix = require('gl-matrix');

var getCanvasContext = function ($canvas) {
    try {
	var canvas = $canvas[0];
	var ctx = canvas.getContext("experimental-webgl", {alpha: false});
	ctx.viewportWidth = canvas.width;
	ctx.viewportHeight = canvas.height;
	return ctx;
    } catch (e) {
	return null;
    }
}

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
    if (match.length === 5) {
	ret = [parseFloat(match[1]) / 255,
	    parseFloat(match[2]) / 255,
	    parseFloat(match[3]) / 255,
	    parseFloat(match[4])];
    }
    return ret;
}
var createShaderProgram = function (view, vertex_shader, fragment_shader) {
    var program = view.ctx.createProgram();
    view.ctx.attachShader(program, vertex_shader);
    view.ctx.attachShader(program, fragment_shader);

    view.ctx.linkProgram(program);

    var success = view.ctx.getProgramParameter(program, view.ctx.LINK_STATUS);
    if (!success) {
	var msg = view.ctx.getProgramInfoLog(program);
	view.ctx.deleteProgram(program);
	throw "Unable to link shader program: " + msg;
    }

    return program;
}
var createShader = function (view, source, type) {
    var shader = view.ctx.createShader(view.ctx[type]);
    view.ctx.shaderSource(shader, source);
    view.ctx.compileShader(shader);

    var success = view.ctx.getShaderParameter(shader, view.ctx.COMPILE_STATUS);
    if (!success) {
	var msg = view.ctx.getShaderInfoLog(shader);
	view.ctx.deleteShader(shader);
	throw "Unable to compile shader: " + msg;
    }

    return shader;
}

var OncoprintWebGLCellView = (function () {
    function OncoprintWebGLCellView($canvas) {
	this.$canvas = $canvas;
	this.ctx = getCanvasContext(this.$canvas);
	this.visible_area_width = $canvas[0].width;
	
	this.scroll_x = 0;

	this.identified_shape_list_list = {};

	this.vertex_position_buffer = this.ctx.createBuffer();
	this.vertex_color_buffer = this.ctx.createBuffer();

	this.vertex_position_array_without_y_offset = {}; // track_id -> zone_id -> vertex list
	this.vertex_position_array = {}; // track_id -> zone_id -> vertex list
	this.vertex_color_array = {}; // track_id -> zone_id -> vertex list

	this.ctx.viewport(0, 0, this.ctx.viewportWidth, this.ctx.viewportHeight);
	this.ctx.enable(this.ctx.DEPTH_TEST);
	this.ctx.enable(this.ctx.BLEND);
	this.ctx.blendEquation(this.ctx.FUNC_ADD);
	this.ctx.blendFunc(this.ctx.SRC_ALPHA, this.ctx.ONE_MINUS_SRC_ALPHA);
	this.ctx.depthMask(false);
	
	(function initializeShaders(self) {// Initialize shaders
	    var vertex_shader_source = ['attribute vec3 aVertexPosition;',
		'attribute vec4 aVertexColor;',
		'',
		'uniform mat4 uMVMatrix;',
		'uniform mat4 uPMatrix;',
		'varying vec4 vColor;',
		'void main(void) {',
		'	gl_Position = uPMatrix * uMVMatrix * vec4(aVertexPosition, 1.0);',
		'	vColor = aVertexColor;',
		'}'].join('\n');
	    var fragment_shader_source = ['precision mediump float;',
		'varying vec4 vColor;',
		'',
		'void main(void) {',
		'   gl_FragColor = vColor;',
		'}'].join('\n');
	    var vertex_shader = createShader(self, vertex_shader_source, 'VERTEX_SHADER');
	    var fragment_shader = createShader(self, fragment_shader_source, 'FRAGMENT_SHADER');

	    var shader_program = createShaderProgram(self, vertex_shader, fragment_shader);
	    shader_program.vertexPositionAttribute = self.ctx.getAttribLocation(shader_program, 'aVertexPosition');
	    self.ctx.enableVertexAttribArray(shader_program.vertexPositionAttribute);
	    shader_program.vertexColorAttribute = self.ctx.getAttribLocation(shader_program, 'aVertexColor');
	    self.ctx.enableVertexAttribArray(shader_program.vertexColorAttribute);

	    shader_program.pMatrixUniform = self.ctx.getUniformLocation(shader_program, 'uPMatrix');
	    shader_program.mvMatrixUniform = self.ctx.getUniformLocation(shader_program, 'uMVMatrix');

	    self.shader_program = shader_program;
	})(this);

	(function initializeMatrices(self) {
	    var mvMatrix = gl_matrix.mat4.create();
	    gl_matrix.mat4.lookAt(mvMatrix, [0, 0, 1], [0, 0, 0], [0, 1, 0]);
	    self.mvMatrix = mvMatrix;

	    var pMatrix = gl_matrix.mat4.create();
	    gl_matrix.mat4.ortho(pMatrix, 0, self.ctx.viewportWidth, self.ctx.viewportHeight, 0, -1, 100); // y axis inverted so that y increases down like SVG
	    self.pMatrix = pMatrix;
	})(this);
    }

    var renderAllTracks = function (view, model) {
	if (view.rendering_suppressed) {
	    return;
	}
	
	view.ctx.clearColor(1.0,1.0,1.0,1.0);
	view.ctx.clear(view.ctx.COLOR_BUFFER_BIT | view.ctx.DEPTH_BUFFER_BIT);
	
	var vertex_position_buffer = view.vertex_position_buffer;
	var vertex_color_buffer = view.vertex_color_buffer;

	// Combine all vertex data, clipping any triangle which are totally out
	var vertex_position_array = [];
	var vertex_color_array = [];
	
	var scroll_x = view.scroll_x;
	var horz_zone_id = Math.floor(scroll_x / view.visible_area_width);
	
	for (var track_id in view.vertex_position_array) {
	    if (view.vertex_position_array.hasOwnProperty(track_id)) {
		for (var z = 0; z<2; z++) {
		    if (view.vertex_position_array[track_id].hasOwnProperty(horz_zone_id + z) 
			&& view.vertex_color_array[track_id].hasOwnProperty(horz_zone_id + z)) {
			    vertex_position_array = vertex_position_array.concat(view.vertex_position_array[track_id][horz_zone_id+z]);
			    vertex_color_array = vertex_color_array.concat(view.vertex_color_array[track_id][horz_zone_id+z]);
		    }
		}
	    }
	}
	for (var i=0; i<vertex_position_array.length; i+=3) {
	    vertex_position_array[i] -= scroll_x;
	}
	// Populate buffers
	view.ctx.bindBuffer(view.ctx.ARRAY_BUFFER, vertex_position_buffer);
	view.ctx.bufferData(view.ctx.ARRAY_BUFFER, new Float32Array(vertex_position_array), view.ctx.STATIC_DRAW);
	vertex_position_buffer.itemSize = 3;
	vertex_position_buffer.numItems = vertex_position_array.length / vertex_position_buffer.itemSize;

	view.ctx.bindBuffer(view.ctx.ARRAY_BUFFER, vertex_color_buffer);
	view.ctx.bufferData(view.ctx.ARRAY_BUFFER, new Float32Array(vertex_color_array), view.ctx.STATIC_DRAW);
	vertex_color_buffer.itemSize = 4;
	vertex_color_buffer.numItems = vertex_color_array.length / vertex_color_buffer.itemSize;

	view.ctx.useProgram(view.shader_program);
	view.ctx.bindBuffer(view.ctx.ARRAY_BUFFER, vertex_position_buffer);
	view.ctx.vertexAttribPointer(view.shader_program.vertexPositionAttribute, vertex_position_buffer.itemSize, view.ctx.FLOAT, false, 0, 0);

	view.ctx.bindBuffer(view.ctx.ARRAY_BUFFER, vertex_color_buffer);
	view.ctx.vertexAttribPointer(view.shader_program.vertexColorAttribute, vertex_color_buffer.itemSize, view.ctx.FLOAT, false, 0, 0);

	view.ctx.uniformMatrix4fv(view.shader_program.pMatrixUniform, false, view.pMatrix);
	view.ctx.uniformMatrix4fv(view.shader_program.mvMatrixUniform, false, view.mvMatrix);
	view.ctx.drawArrays(view.ctx.TRIANGLES, 0, vertex_position_buffer.numItems);
    }
    var addVertexColor = function (vertex_color_array, rgba_str, n_times) {
	var color = extractRGBA(rgba_str);
	for (var h = 0; h < n_times; h++) {
	    vertex_color_array.push(color[0], color[1], color[2], color[3]);
	}
    }
    var computeVertexPositionsWithYOffset = function(view, model, track_id) {
	var offset_y = model.getTrackTop(track_id);
	var positions_with_y_offset = {};

	for (var horz_zone_id in view.vertex_position_array_without_y_offset[track_id]) {
	    if (view.vertex_position_array_without_y_offset[track_id].hasOwnProperty(horz_zone_id)) {
		positions_with_y_offset[horz_zone_id] = [];
		var positions_without_y_offset = view.vertex_position_array_without_y_offset[track_id][horz_zone_id];
		var zone_positions_with_y_offset = positions_with_y_offset[horz_zone_id];
		for (var i = 0; i < positions_without_y_offset.length; i++) {
		    if (i % 3 === 1) {
			zone_positions_with_y_offset.push(positions_without_y_offset[i] + offset_y);
		    } else {
			zone_positions_with_y_offset.push(positions_without_y_offset[i]);
		    }
		}
	    }
	}
	view.vertex_position_array[track_id] = positions_with_y_offset;
    }
    var computeVertexPositionsWithoutYOffsetAndVertexColors = function (view, model, track_id) {
	view.vertex_position_array_without_y_offset[track_id] = view.vertex_position_array_without_y_offset[track_id] || {};
	view.vertex_color_array[track_id] = view.vertex_color_array[track_id] || {};
	
	var identified_shape_list_list = view.identified_shape_list_list[track_id];
	var id_order = model.getIdOrder();
	var id_to_index = {};
	for (var i=0; i<id_order.length; i++) {
	    id_to_index[id_order[i]] = i;
	}
	var offset_x_inc = model.getCellPadding() + model.getCellWidth();
	var halfsqrt2 = Math.sqrt(2) / 2;
	// Compute vertex and color arrays
	var vertex_position_array;
	var vertex_color_array;
	for (var i = 0; i < identified_shape_list_list.length; i++) {
	    var shape_list = identified_shape_list_list[i].shape_list;
	    var id = identified_shape_list_list[i].id;
	    var offset_x = offset_x_inc * id_to_index[id];
	    var horz_zone_id = Math.floor(offset_x / view.visible_area_width);
	    
	    view.vertex_position_array_without_y_offset[track_id][horz_zone_id] = view.vertex_position_array_without_y_offset[track_id][horz_zone_id] || [];
	    vertex_position_array = view.vertex_position_array_without_y_offset[track_id][horz_zone_id];
	    view.vertex_color_array[track_id][horz_zone_id] = view.vertex_color_array[track_id][horz_zone_id] || [];
	    vertex_color_array = view.vertex_color_array[track_id][horz_zone_id];
	    for (var j = 0; j < shape_list.length; j++) {
		var shape = shape_list[j];
		if (shape.type === "rectangle") {
		    // Stroke
		    var x = parseFloat(shape.x) + offset_x, y = parseFloat(shape.y), height = parseFloat(shape.height), width = parseFloat(shape.width);
		    var stroke_width = parseFloat(shape['stroke-width']);
		    if (stroke_width > 0) {
			vertex_position_array.push(x - stroke_width, y - stroke_width, -3);
			vertex_position_array.push(x + width + stroke_width, y - stroke_width, -3);
			vertex_position_array.push(x + width + stroke_width, y + height + stroke_width, -3);

			vertex_position_array.push(x - stroke_width, y - stroke_width, -3);
			vertex_position_array.push(x + width + stroke_width, y + height + stroke_width, -3);
			vertex_position_array.push(x - stroke_width, y + height + stroke_width, -3);

			addVertexColor(vertex_color_array, shape.stroke, 6);
		    }
		    
		    vertex_position_array.push(x, y, j);
		    vertex_position_array.push(x + width, y, j);
		    vertex_position_array.push(x + width, y + height, j);

		    vertex_position_array.push(x, y, j);
		    vertex_position_array.push(x + width, y + height, j);
		    vertex_position_array.push(x, y + height, j);

		    addVertexColor(vertex_color_array, shape.fill, 6);
		} else if (shape.type === "triangle") {
		    vertex_position_array.push(offset_x + parseFloat(shape.x1), parseFloat(shape.y1), j);
		    vertex_position_array.push(offset_x + parseFloat(shape.x2), parseFloat(shape.y2), j);
		    vertex_position_array.push(offset_x + parseFloat(shape.x3), parseFloat(shape.y3), j);

		    addVertexColor(vertex_color_array, shape.fill, 3);
		} else if (shape.type === "ellipse") {
		    var center = {x: offset_x + parseFloat(shape.x) + parseFloat(shape.width) / 2, y: parseFloat(shape.y) + parseFloat(shape.height) / 2};
		    var horzrad = parseFloat(shape.width) / 2;
		    var vertrad = parseFloat(shape.height) / 2;

		    vertex_position_array.push(center.x, center.y, j);
		    vertex_position_array.push(center.x + horzrad, center.y, j);
		    vertex_position_array.push(center.x + halfsqrt2 * horzrad, center.y + halfsqrt2 * vertrad, j);

		    vertex_position_array.push(center.x, center.y, j);
		    vertex_position_array.push(center.x + halfsqrt2 * horzrad, center.y + halfsqrt2 * vertrad, j);
		    vertex_position_array.push(center.x, center.y + vertrad, j);

		    vertex_position_array.push(center.x, center.y, j);
		    vertex_position_array.push(center.x, center.y + vertrad, j);
		    vertex_position_array.push(center.x - halfsqrt2 * horzrad, center.y + halfsqrt2 * vertrad, j);

		    vertex_position_array.push(center.x, center.y, j);
		    vertex_position_array.push(center.x - halfsqrt2 * horzrad, center.y + halfsqrt2 * vertrad, j);
		    vertex_position_array.push(center.x - horzrad, center.y, j);

		    vertex_position_array.push(center.x, center.y, j);
		    vertex_position_array.push(center.x - horzrad, center.y, j);
		    vertex_position_array.push(center.x - halfsqrt2 * horzrad, center.y - halfsqrt2 * vertrad, j);

		    vertex_position_array.push(center.x, center.y, j);
		    vertex_position_array.push(center.x - halfsqrt2 * horzrad, center.y - halfsqrt2 * vertrad, j);
		    vertex_position_array.push(center.x, center.y - vertrad, j);

		    vertex_position_array.push(center.x, center.y, j);
		    vertex_position_array.push(center.x, center.y - vertrad, j);
		    vertex_position_array.push(center.x + halfsqrt2 * horzrad, center.y - halfsqrt2 * vertrad, j);

		    vertex_position_array.push(center.x, center.y, j);
		    vertex_position_array.push(center.x + halfsqrt2 * horzrad, center.y - halfsqrt2 * vertrad, j);
		    vertex_position_array.push(center.x + horzrad, center.y, j);

		    addVertexColor(vertex_color_array, shape.fill, 3 * 8);
		} else if (shape.type === "line") {
		    // TODO: implement line
		}
	    }
	}
    }
    
    var computeVertexesAndRenderTracks = function(view, model) {
	var track_ids = model.getTracks();
	for (var i = 0; i<track_ids.length; i++) {
	    computeVertexPositionsWithoutYOffsetAndVertexColors(view, model, track_ids[i]);
	}
	renderAllTracks(view);
    }
    var computeIdentifiedShapeListList = function(view, model, track_id) {
	var track_data = model.getTrackData(track_id);
	var track_data_id_key = model.getTrackDataIdKey(track_id);
	var shape_list_list = model.getRuleSet(track_id).apply(track_data, model.getCellWidth(), model.getTrackHeight(track_id));
	view.identified_shape_list_list[track_id] = shape_list_list.map(function (list, index) {
	    return {
		id: track_data[index][track_data_id_key],
		shape_list: list
	    };
	});
    }
    OncoprintWebGLCellView.prototype.isUsable = function () {
	return this.ctx !== null;
    }
    OncoprintWebGLCellView.prototype.removeTrack = function (model, track_id) {
	delete this.vertex_position_array[track_id];
	delete this.vertex_color_array[track_id];
	computeVertexesAndRenderTracks(this, model);
    }
    OncoprintWebGLCellView.prototype.moveTrack = function (model) {
	computeVertexesAndRenderTracks(this, model);
    }
    OncoprintWebGLCellView.prototype.addTracks = function (model, track_ids) {
	for (var i=0; i<track_ids.length; i++) {
	    computeIdentifiedShapeListList(this, model, track_ids[i]);
	    computeVertexPositionsWithoutYOffsetAndVertexColors(this, model, track_ids[i]);
	    computeVertexPositionsWithYOffset(this, model, track_ids[i]);
	}
	renderAllTracks(this);
    }
    OncoprintWebGLCellView.prototype.setIdOrder = function(model, ids) {
	var track_ids = model.getTracks();
	for (var i=0; i<track_ids; i++) {
	    computeVertexPositionsWithoutYOffsetAndVertexColors(this, model, track_ids[i]);
	    computeVertexPositionsWithYOffset(this, model, track_ids[i]);
	}
	renderAllTracks(this);
    }
    OncoprintWebGLCellView.prototype.setTrackGroupSortPriority = function(model) {
	var track_ids = model.getTracks();
	for (var i=0; i<track_ids; i++) {
	    computeVertexPositionsWithYOffset(this, model, track_ids[i]);
	}
	renderAllTracks(this);
    }
    OncoprintWebGLCellView.prototype.sort = function(model) {
	var track_ids = model.getTracks();
	for (var i=0; i<track_ids; i++) {
	    computeVertexPositionsWithoutYOffsetAndVertexColors(this, model, track_ids[i]);
	    computeVertexPositionsWithYOffset(this, model, track_ids[i]);
	}
	renderAllTracks(this);
    }
    OncoprintWebGLCellView.prototype.suppressRendering = function() {
	this.rendering_suppressed = true;
    }
    OncoprintWebGLCellView.prototype.releaseRendering = function() {
	this.rendering_suppressed = false;
	renderAllTracks(this);
    }
    OncoprintWebGLCellView.prototype.hideIds = function(model) {
	var track_ids = model.getTracks();
	for (var i=0; i<track_ids; i++) {
	    computeVertexPositionsWithoutYOffsetAndVertexColors(this, model, track_ids[i]);
	    computeVertexPositionsWithYOffset(this, model, track_ids[i]);
	}
	renderAllTracks(this);
    }
    OncoprintWebGLCellView.prototype.setTrackData = function(model, track_id) {
	computeIdentifiedShapeListList(this, model, track_id);
	computeVertexPositionsWithoutYOffsetAndVertexColors(this, model, track_id);
	computeVertexPositionsWithYOffset(this, model, track_id);
	renderAllTracks(this);
    }
    OncoprintWebGLCellView.prototype.setSortConfig = function(model) {
	this.sort(model);
    }
    OncoprintWebGLCellView.prototype.setRuleSet = function(model) {
	// TODO: implement
    }
    
    OncoprintWebGLCellView.prototype.scroll = function(offset) {
	this.scroll_x = offset;
	renderAllTracks(this);
    }
    return OncoprintWebGLCellView;
})();

module.exports = OncoprintWebGLCellView;
