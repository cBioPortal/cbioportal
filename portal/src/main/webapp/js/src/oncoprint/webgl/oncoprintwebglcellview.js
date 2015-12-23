var gl_matrix = require('gl-matrix');

var getCanvasContext = function ($canvas) {
    try {
	var canvas = $canvas[0];
	var ctx = canvas.getContext("experimental-webgl");
	ctx.viewportWidth = canvas.width;
	ctx.viewportHeight = canvas.height;
	return ctx;
    } catch (e) {
	return null;
    }
}

var extractRGBA = function (str) {
    var ret = [0, 0, 0, 1];
    var match = str.match(/^rgba\(([0-9]+),([0-9]+),([0-9]+),([0-9.]+)\)$/);
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
    
var OncoprintWebGLCellView = (function() {
    function OncoprintWebGLCellView($canvas) {
	this.$canvas = $canvas;
	this.track_shapes = {};
	this.ctx = getCanvasContext(this.$canvas);
	
	this.vertex_position_buffer = this.ctx.createBuffer();
	this.vertex_color_buffer = this.ctx.createBuffer();
	
	this.vertex_position_array = {};
	this.vertex_color_array = {};
	
	this.ctx.viewport(0,0,this.ctx.viewportWidth,this.ctx.viewportHeight);
	this.ctx.enable(this.ctx.DEPTH_TEST);
	
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
	    gl_matrix.mat4.lookAt(mvMatrix, [0,0,1], [0,0,0], [0,1,0]);
	    self.mvMatrix = mvMatrix;
	    
	    var pMatrix = gl_matrix.mat4.create();
	    gl_matrix.mat4.ortho(pMatrix, 0, self.ctx.viewportWidth, self.ctx.viewportHeight, 0, -1, 100); // y axis inverted so that y increases down like SVG
	    self.pMatrix = pMatrix;
	})(this);
    }
    
    var renderAllTracks = function(view, model) {
	view.ctx.clear(view.ctx.COLOR_BUFFER_BIT | view.ctx.DEPTH_BUFFER_BIT);
	var vertex_position_buffer = view.vertex_position_buffer;
	var vertex_color_buffer = view.vertex_color_buffer;
	
	// Combine all vertex data
	var vertex_position_array = [];
	var vertex_color_array = [];
	for (var track_id in view.vertex_position_array) {
	    if (view.vertex_position_array.hasOwnProperty(track_id)) {
		vertex_position_array = vertex_position_array.concat(view.vertex_position_array[track_id]);
		vertex_color_array = vertex_color_array.concat(view.vertex_color_array[track_id]);
	    }
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
    var computeVertexPositionsAndColors = function(view, model, track_id) {
	var vertex_position_array = [];
	var vertex_color_array = [];
	var shape_list_list = model.getRuleSet(track_id).apply(model.getTrackData(track_id), 10, 10);
	var offset_x_inc = model.getCellPadding() + model.getCellWidth();
	var offset_y = model.getTrackTop(track_id);
	// Compute vertex and color arrays
	for (var i=0; i<shape_list_list.length; i++) {
	    var shape_list = shape_list_list[i];
	    var offset_x = offset_x_inc * i;
	    for (var j=0; j<shape_list.length; j++) {
		var shape = shape_list[j];
		if (shape.type === "rectangle") {
		    var x = parseFloat(shape.x) + offset_x, y = parseFloat(shape.y) + offset_y, height=parseFloat(shape.height), width=parseFloat(shape.width);
		    vertex_position_array.push(x, y, j);
		    vertex_position_array.push(x+width, y, j);
		    vertex_position_array.push(x+width, y+height, j);
		    
		    vertex_position_array.push(x, y, j);
		    vertex_position_array.push(x+width, y+height, j);
		    vertex_position_array.push(x, y+height, j);
		    // todo: stroke-width
		    
		    var fill = extractRGBA(shape.fill);
		    for (var h=0; h<6; h++) {
			vertex_color_array.push(fill[0], fill[1], fill[2], fill[3]);
		    }
		} else if (shape.type === "triangle") {
		    vertex_position_array.push(shape.x1, shape.y1, j);
		    vertex_position_array.push(shape.x2, shape.y2, j);
		    vertex_position_array.push(shape.x3, shape.y3, j);
		    
		    var fill = extractRGBA(shape.fill);
		    for (var h=0; h<3; h++) {
			vertex_color_array.push(fill[0], fill[1], fill[2], fill[3]);
		    }
		} else if (shape.type === "ellipse") {
		} else if (shape.type === "line") {
		}
	    }
	}
	view.vertex_position_array[track_id] = vertex_position_array;
	view.vertex_color_array[track_id] = vertex_color_array;
    }
    OncoprintWebGLCellView.prototype.isUsable = function() {
	return this.ctx !== null;
    }
    OncoprintWebGLCellView.prototype.removeTrack = function(model, track_id) {
    }
    OncoprintWebGLCellView.prototype.moveTrack = function(model, track_id) {
    }
    OncoprintWebGLCellView.prototype.addTracks = function(model, track_ids) {
	for (var i=0; i<track_ids.length; i++) {
	    computeVertexPositionsAndColors(this, model, track_ids[i]);
	}
	renderAllTracks(this, model);
    }
    return OncoprintWebGLCellView;
})();

module.exports = OncoprintWebGLCellView;