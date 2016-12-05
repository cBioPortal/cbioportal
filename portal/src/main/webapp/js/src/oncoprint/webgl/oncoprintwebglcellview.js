var gl_matrix = require('gl-matrix');
var svgfactory = require('./svgfactory.js');
var extractRGBA = require('./extractrgba.js');

// TODO: antialiasing

var getNewCanvas = function(view) {
    var old_canvas = view.$canvas[0];
    var new_canvas = old_canvas.cloneNode();
    var parent_node = old_canvas.parentNode;
    parent_node.removeChild(old_canvas);
    parent_node.insertBefore(new_canvas, view.$overlay_canvas[0]);
    view.$canvas = $(new_canvas);
};
var getWebGLCanvasContext = function (view) {
    try {
	var canvas = view.$canvas[0];
	var ctx = canvas.getContext("experimental-webgl", {alpha: false, antialias: view.antialias});
	ctx.clearColor(1.0, 1.0, 1.0, 1.0);
	ctx.clear(ctx.COLOR_BUFFER_BIT | ctx.DEPTH_BUFFER_BIT);
	ctx.viewportWidth = canvas.width;
	ctx.viewportHeight = canvas.height;
	ctx.viewport(0, 0, ctx.viewportWidth, ctx.viewportHeight);
	ctx.enable(ctx.DEPTH_TEST);
	ctx.enable(ctx.BLEND);
	ctx.blendEquation(ctx.FUNC_ADD);
	ctx.blendFunc(ctx.SRC_ALPHA, ctx.ONE_MINUS_SRC_ALPHA);
	ctx.depthMask(false);
	
	return ctx;
    } catch (e) {
	return null;
    }
};

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
};
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
};

var OncoprintWebGLCellView = (function () {
    function OncoprintWebGLCellView($container, $canvas, $overlay_canvas, $dummy_scroll_div, model, tooltip, highlight_area_callback) {
	this.$container = $container;
	this.$canvas = $canvas;
	this.$overlay_canvas = $overlay_canvas;
	
	this.supersampling_ratio = 2;
	
	this.antialias = true;
	this.antialias_on_cell_width_thresh = 5;
	
	getWebGLContextAndSetUpMatricesAndShaders(this);
	getOverlayContextAndClear(this);
	this.visible_area_width = $canvas[0].width;
	
	var self = this;
	this.$container.scroll(function() {
	    var scroll_left = self.$container.scrollLeft();
	    self.$canvas.css('left', scroll_left);
	    self.$overlay_canvas.css('left', scroll_left);
	    self.scroll(model, scroll_left);
	});
	
	this.tooltip = tooltip;
	this.tooltip.center = true;
	
	this.scroll_x = 0;
	this.scroll_y = 0;
	this.$dummy_scroll_div = $dummy_scroll_div;

	this.identified_shape_list_list = {};

	this.vertex_position_buffer_by_zone = {}; // track_id -> zone_id -> gl.createBuffer()
	this.vertex_color_buffer_by_zone = {}; // track_id -> zone_id -> gl.createBuffer()

	this.vertex_position_array = {}; // track_id -> zone_id -> vertex list
	this.vertex_color_array = {}; // track_id -> zone_id -> vertex list

	this.rendering_suppressed = false;
	
	this.highlight_area_callback = (typeof highlight_area_callback === 'undefined' ? function() {} : highlight_area_callback); // function(left, right) { ... }
	

	(function initializeOverlayEvents(self) {
	    var dragging = false;
	    var drag_diff_minimum = 10;
	    var drag_start_x;
	    var drag_end_x;
	    var prev_overlapping_cell = null;
	    
	    var dragIsValid = function(drag_start_x, drag_end_x) {
		return Math.abs(drag_start_x - drag_end_x) >= drag_diff_minimum;
	    };
	    var executeDrag = function() {
		if (!dragging) {
		    return;
		}
		dragging = false;
		
		if (!dragIsValid(drag_start_x, drag_end_x)) {
		    return;
		}
		var left = Math.min(drag_start_x, drag_end_x);
		var right = Math.max(drag_start_x, drag_end_x);
		self.highlight_area_callback(left+self.scroll_x, right+self.scroll_x);
	    };
	    
	    $(document).on("mousemove", function () {
		if (self.rendering_suppressed) {
		    return;
		}
		clearOverlay(self);
		tooltip.hide();
	    });
	    self.$overlay_canvas.on("mousemove", function(evt) {
		evt.stopPropagation();
		if (self.rendering_suppressed) {
		    return;
		}
		clearOverlay(self);
		var offset = self.$overlay_canvas.offset();
		var mouseX = evt.pageX - offset.left;
		var mouseY = evt.pageY - offset.top;
		if (!dragging) {
		    var overlapping_cell = model.getOverlappingCell(mouseX + self.scroll_x, mouseY);
		    var overlapping_datum = (overlapping_cell === null ? null : model.getTrackDatum(overlapping_cell.track, overlapping_cell.id));
		    var cell_width = model.getCellWidth();
		    var cell_padding = model.getCellPadding();
		    if (overlapping_datum !== null) {
			var left = model.getZoomedColumnLeft(overlapping_cell.id) - self.scroll_x;
			overlayStrokeRect(self, left, model.getCellTops(overlapping_cell.track), cell_width + (model.getTrackHasColumnSpacing(overlapping_cell.track) ? 0 : cell_padding), model.getCellHeight(overlapping_cell.track), "rgba(0,0,0,1)");
			var tracks = model.getTracks();
			for (var i=0; i<tracks.length; i++) {
			    if (model.getTrackDatum(tracks[i], overlapping_cell.id) !== null) {
				overlayStrokeRect(self, left, model.getCellTops(tracks[i]), cell_width + (model.getTrackHasColumnSpacing(tracks[i]) ? 0 : cell_padding), model.getCellHeight(tracks[i]), "rgba(0,0,0,0.5)");
			    }
			}
			tooltip.show(250, model.getZoomedColumnLeft(overlapping_cell.id) + model.getCellWidth() / 2 + offset.left - self.scroll_x, model.getCellTops(overlapping_cell.track) + offset.top, model.getTrackTooltipFn(overlapping_cell.track)(overlapping_datum));
			prev_overlapping_cell = overlapping_cell;
		    } else {
			tooltip.hideIfNotAlreadyGoingTo(150);
			overlapping_cell = null;
		    }
		} else {
		    overlapping_cell = null;
		    drag_end_x = mouseX;
		    var left = Math.min(mouseX, drag_start_x);
		    var right = Math.max(mouseX, drag_start_x);
		    var drag_rect_fill = dragIsValid(drag_start_x, drag_end_x) ? 'rgba(0,0,0,0.3)' : 'rgba(0,0,0,0.2)';
		    overlayFillRect(self, left, 0, right-left, model.getCellViewHeight(), drag_rect_fill);
		}
	    });
	    
	    self.$overlay_canvas.on("mousedown", function(evt) {
		dragging = true;
		drag_start_x = evt.pageX - self.$overlay_canvas.offset().left;
		drag_end_x = drag_start_x;
		
		tooltip.hide();
	    });
	    self.$overlay_canvas.on("mouseup", function(evt) {
		executeDrag();
	    });
	    self.$overlay_canvas.on("mouseleave", function(evt) {
		executeDrag();
	    });
	    
	})(this);
    }
    
    var overlayStrokeRect = function(view, x, y, width, height, color) {
	var ctx = view.overlay_ctx;
	ctx.strokeStyle = color;
	ctx.strokeWidth = 10;
	ctx.strokeRect(view.supersampling_ratio*x, view.supersampling_ratio*y, view.supersampling_ratio*width, view.supersampling_ratio*height);
    };
    
    var overlayFillRect = function(view, x, y, width, height, color) {
	var ctx = view.overlay_ctx;
	ctx.fillStyle = color;
	ctx.fillRect(view.supersampling_ratio*x, view.supersampling_ratio*y, view.supersampling_ratio*width, view.supersampling_ratio*height);
    };
    
    var clearOverlay = function(view) {
	view.overlay_ctx.fillStyle = "rgba(0,0,0,0)";
	view.overlay_ctx.clearRect(0,0,view.$overlay_canvas[0].width, view.$overlay_canvas[0].height);
    };
    
    var getOverlayContextAndClear = function(view) {
	view.overlay_ctx = view.$overlay_canvas[0].getContext('2d');
	clearOverlay(view);
    };
    
    var getWebGLContextAndSetUpMatrices = function(view) {
	view.ctx = getWebGLCanvasContext(view);
	(function initializeMatrices(self) {
	    var mvMatrix = gl_matrix.mat4.create();
	    gl_matrix.mat4.lookAt(mvMatrix, [0, 0, 1], [0, 0, 0], [0, 1, 0]);
	    self.mvMatrix = mvMatrix;

	    var pMatrix = gl_matrix.mat4.create();
	    gl_matrix.mat4.ortho(pMatrix, 0, self.ctx.viewportWidth, self.ctx.viewportHeight, 0, -5, 1000); // y axis inverted so that y increases down like SVG
	    self.pMatrix = pMatrix;
	})(view);
    };
    var getWebGLContextAndSetUpMatricesAndShaders = function(view) {
	getWebGLContextAndSetUpMatrices(view);
	(function initializeShaders(self) {// Initialize shaders
	    var vertex_shader_source = ['attribute vec3 aVertexPosition;',
		'attribute vec4 aVertexColor;',
		'',
		'uniform float scrollX;',
		'uniform float zoomX;',
		'uniform float scrollY;',
		'uniform float zoomY;',
		'uniform mat4 uMVMatrix;',
		'uniform mat4 uPMatrix;',
		'uniform float offsetY;',
		'uniform float supersamplingRatio;',
		'varying vec4 vColor;',
		'void main(void) {',
		'	gl_Position = vec4(aVertexPosition, 1.0);',
		'	gl_Position[1] += offsetY;',
		'	gl_Position[0] *= zoomX;',
		'	gl_Position -= vec4(scrollX, 0.0, 0.0, 0.0);',
		'	gl_Position[0] *= supersamplingRatio;',
		'	gl_Position[1] *= supersamplingRatio;',
		'	gl_Position = uPMatrix * uMVMatrix * gl_Position;',
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
	    shader_program.scrollXUniform = self.ctx.getUniformLocation(shader_program, 'scrollX');
	    shader_program.zoomXUniform = self.ctx.getUniformLocation(shader_program, 'zoomX');
	    shader_program.offsetYUniform = self.ctx.getUniformLocation(shader_program, 'offsetY');
	    shader_program.supersamplingRatioUniform = self.ctx.getUniformLocation(shader_program, 'supersamplingRatio');

	    self.shader_program = shader_program;
	})(view);
    };

    var resizeAndClear = function(view, model) {
	var height = model.getCellViewHeight();
	var total_width = view.getTotalWidth(model);
	var visible_area_width = view.visible_area_width;
	view.$dummy_scroll_div.css('width', total_width);
	view.$canvas[0].height = view.supersampling_ratio*height;
	view.$canvas[0].style.height = height + 'px';
	view.$overlay_canvas[0].height = view.supersampling_ratio*height;
	view.$overlay_canvas[0].style.height = height + 'px';
	view.$canvas[0].width = view.supersampling_ratio*visible_area_width;
	view.$canvas[0].style.width = visible_area_width + 'px';
	view.$overlay_canvas[0].width = view.supersampling_ratio*visible_area_width;
	view.$overlay_canvas[0].style.width = visible_area_width + 'px';
	view.$container.css('height', height);
	view.$container.css('width', visible_area_width);
	view.$container.scrollLeft(Math.min(view.$container.scrollLeft(),total_width-view.visible_area_width))
	getWebGLContextAndSetUpMatrices(view);
	getOverlayContextAndClear(view);
    };
    var renderAllTracks = function (view, model) {
	if (view.rendering_suppressed) {
	    return;
	}
	
	var scroll_x = view.scroll_x;
	var zoom_x = model.getHorzZoom();
	var horz_zone_id = Math.floor(scroll_x / view.visible_area_width);
	
	resizeAndClear(view, model);
	view.ctx.clearColor(1.0,1.0,1.0,1.0);
	view.ctx.clear(view.ctx.COLOR_BUFFER_BIT | view.ctx.DEPTH_BUFFER_BIT);
	
	var tracks = model.getTracks();
	for (var i = 0; i < tracks.length; i++) {
	    var track_id = tracks[i];
	    var cell_top = model.getCellTops(track_id);
	    var buffers = getZoneBuffers(view, track_id, horz_zone_id);
	    if (buffers.position.numItems === 0) {
		continue;
	    }
	    view.ctx.useProgram(view.shader_program);
	    view.ctx.bindBuffer(view.ctx.ARRAY_BUFFER, buffers.position);
	    view.ctx.vertexAttribPointer(view.shader_program.vertexPositionAttribute, buffers.position.itemSize, view.ctx.FLOAT, false, 0, 0);

	    view.ctx.bindBuffer(view.ctx.ARRAY_BUFFER, buffers.color);
	    view.ctx.vertexAttribPointer(view.shader_program.vertexColorAttribute, buffers.color.itemSize, view.ctx.FLOAT, false, 0, 0);

	    view.ctx.uniformMatrix4fv(view.shader_program.pMatrixUniform, false, view.pMatrix);
	    view.ctx.uniformMatrix4fv(view.shader_program.mvMatrixUniform, false, view.mvMatrix);
	    view.ctx.uniform1f(view.shader_program.scrollXUniform, scroll_x);
	    view.ctx.uniform1f(view.shader_program.zoomXUniform, zoom_x);
	    view.ctx.uniform1f(view.shader_program.offsetYUniform, cell_top);
	    view.ctx.uniform1f(view.shader_program.supersamplingRatioUniform, view.supersampling_ratio);
	    view.ctx.drawArrays(view.ctx.TRIANGLES, 0, buffers.position.numItems);
	}
    };
    var addVertexColor = function (vertex_color_array, rgba_str, n_times) {
	var color = extractRGBA(rgba_str);
	for (var h = 0; h < n_times; h++) {
	    vertex_color_array.push(color[0], color[1], color[2], color[3]);
	}
    };
    
    var clearZoneBuffers = function(view, model, track_id) {
	var tracks_to_clear;
	if (typeof track_id === 'undefined') {
	    tracks_to_clear = model.getTracks();
	} else {
	    tracks_to_clear = [track_id];
	}
	for (var i=0; i<tracks_to_clear.length; i++) {
	    delete view.vertex_position_buffer_by_zone[tracks_to_clear[i]];
	    delete view.vertex_color_buffer_by_zone[tracks_to_clear[i]];
	}
    };
    var getZoneBuffers = function(view, track_id, zone_id) {
	view.vertex_position_buffer_by_zone[track_id] = view.vertex_position_buffer_by_zone[track_id] || {};
	view.vertex_color_buffer_by_zone[track_id] = view.vertex_color_buffer_by_zone[track_id] || {};
	if (typeof view.vertex_position_buffer_by_zone[track_id][zone_id] === 'undefined') {
	    var vertex_position_buffer = view.ctx.createBuffer();
	    var vertex_color_buffer = view.ctx.createBuffer();
	    var vertex_position_array = [];
	    var vertex_color_array = [];
	    for (var z = 0; z < 2; z++) {
		if (view.vertex_position_array[track_id].hasOwnProperty(zone_id + z) && view.vertex_color_array[track_id].hasOwnProperty(zone_id + z)) {
		    vertex_position_array = vertex_position_array.concat(view.vertex_position_array[track_id][zone_id + z]);
		    vertex_color_array = vertex_color_array.concat(view.vertex_color_array[track_id][zone_id + z]);
		}
	    }
	    view.ctx.bindBuffer(view.ctx.ARRAY_BUFFER, vertex_position_buffer);
	    view.ctx.bufferData(view.ctx.ARRAY_BUFFER, new Float32Array(vertex_position_array), view.ctx.STATIC_DRAW);
	    vertex_position_buffer.itemSize = 3;
	    vertex_position_buffer.numItems = vertex_position_array.length / vertex_position_buffer.itemSize;

	    view.ctx.bindBuffer(view.ctx.ARRAY_BUFFER, vertex_color_buffer);
	    view.ctx.bufferData(view.ctx.ARRAY_BUFFER, new Float32Array(vertex_color_array), view.ctx.STATIC_DRAW);
	    vertex_color_buffer.itemSize = 4;
	    vertex_color_buffer.numItems = vertex_color_array.length / vertex_color_buffer.itemSize;

	    view.vertex_position_buffer_by_zone[track_id][zone_id] = vertex_position_buffer;
	    view.vertex_color_buffer_by_zone[track_id][zone_id] = vertex_color_buffer;
	}
	return {'position':view.vertex_position_buffer_by_zone[track_id][zone_id],
		'color': view.vertex_color_buffer_by_zone[track_id][zone_id]};
    };
    var computeVertexPositionsAndVertexColors = function (view, model, track_id) {
	if (view.rendering_suppressed) {
	    return;
	}
	var zone_to_vertex_color_array = {};
	var zone_to_vertex_position_array = {};
	
	var identified_shape_list_list = view.identified_shape_list_list[track_id];
	var id_to_left = model.getColumnLeft();
	var halfsqrt2 = Math.sqrt(2) / 2;
	// Compute vertex and color arrays
	var vertex_position_array;
	var vertex_color_array;
	for (var i = 0; i < identified_shape_list_list.length; i++) {
	    var shape_list = identified_shape_list_list[i].shape_list;
	    var id = identified_shape_list_list[i].id;
	    if (typeof id_to_left[id] === 'undefined') {
		continue;
	    }
	    var offset_x = id_to_left[id];
	    var horz_zone_id = Math.floor(offset_x*model.getHorzZoom() / view.visible_area_width);
	    
	    zone_to_vertex_position_array[horz_zone_id] = zone_to_vertex_position_array[horz_zone_id] || [];
	    vertex_position_array = zone_to_vertex_position_array[horz_zone_id];
	    zone_to_vertex_color_array[horz_zone_id] = zone_to_vertex_color_array[horz_zone_id] || [];
	    vertex_color_array = zone_to_vertex_color_array[horz_zone_id];
	    for (var j = 0; j < shape_list.length; j++) {
		var shape = shape_list[j];
		if (shape.type === "rectangle") {
		    // Stroke
		    var x = parseFloat(shape.x) + offset_x, y = parseFloat(shape.y),  height = parseFloat(shape.height), width = parseFloat(shape.width);
		    var stroke_width = parseFloat(shape['stroke-width']);
		     		    
		    vertex_position_array.push(x, y, j);
		    vertex_position_array.push(x + width, y, j);
		    vertex_position_array.push(x + width, y + height, j);

		    vertex_position_array.push(x, y, j);
		    vertex_position_array.push(x + width, y + height, j);
		    vertex_position_array.push(x, y + height, j);

		    addVertexColor(vertex_color_array, shape.fill, 6);
		    
		    if (stroke_width > 0) {
			// left side
			vertex_position_array.push(x, y, j);
			vertex_position_array.push(x + stroke_width, y, j);
			vertex_position_array.push(x + stroke_width, y + height, j);

			vertex_position_array.push(x, y, j);
			vertex_position_array.push(x + stroke_width, y + height, j);
			vertex_position_array.push(x, y + height, j);
			
			// right side
			vertex_position_array.push(x + width, y, j);
			vertex_position_array.push(x + width - stroke_width, y, j);
			vertex_position_array.push(x + width - stroke_width, y + height, j);

			vertex_position_array.push(x + width, y, j);
			vertex_position_array.push(x + width - stroke_width, y + height, j);
			vertex_position_array.push(x + width, y + height, j);
			
			// top side
			vertex_position_array.push(x, y, j);
			vertex_position_array.push(x+width, y, j);
			vertex_position_array.push(x+width, y+stroke_width, j);
			
			vertex_position_array.push(x, y, j);
			vertex_position_array.push(x+width, y+stroke_width, j);
			vertex_position_array.push(x, y+stroke_width, j);
			
			// bottom side
			vertex_position_array.push(x, y+height, j);
			vertex_position_array.push(x+width, y+height, j);
			vertex_position_array.push(x+width, y+height-stroke_width, j);
			
			vertex_position_array.push(x, y+height, j);
			vertex_position_array.push(x+width, y+height-stroke_width, j);
			vertex_position_array.push(x, y+height-stroke_width, j);

			addVertexColor(vertex_color_array, shape.stroke, 6*4);
		    }

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
		    // For simplicity of dealing with webGL we'll implement lines as thin triangle pairs
		    var x1 = parseFloat(shape.x1) + offset_x;
		    var x2 = parseFloat(shape.x2) + offset_x;
		    var y1 = parseFloat(shape.y1);
		    var y2 = parseFloat(shape.y2);
		    
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
		    var perpendicular_vector_length = Math.sqrt(perpendicular_vector[0]*perpendicular_vector[0] + perpendicular_vector[1]*perpendicular_vector[1]);
		    var unit_perp_vector = [perpendicular_vector[0]/perpendicular_vector_length, perpendicular_vector[1]/perpendicular_vector_length];
		    
		    var half_stroke_width = parseFloat(shape['stroke-width'])/2;
		    var direction1 = [unit_perp_vector[0]*half_stroke_width, unit_perp_vector[1]*half_stroke_width];
		    var direction2 = [direction1[0]*-1, direction1[1]*-1];
		    var A = [x1 + direction1[0], y1 + direction1[1]];
		    var B = [x1 + direction2[0], y1 + direction2[1]];
		    var C = [x2 + direction1[0], y2 + direction1[1]];
		    var D = [x2 + direction2[0], y2 + direction2[1]];
		    
		    vertex_position_array.push(A[0], A[1], j);
		    vertex_position_array.push(B[0], B[1], j);
		    vertex_position_array.push(C[0], C[1], j);
		    
		    vertex_position_array.push(C[0], C[1], j);
		    vertex_position_array.push(D[0], D[1], j);
		    vertex_position_array.push(B[0], B[1], j);
		    
		    addVertexColor(vertex_color_array, shape.stroke, 3*2);
		}
	    }
	}
	view.vertex_position_array[track_id] = zone_to_vertex_position_array;
	view.vertex_color_array[track_id] = zone_to_vertex_color_array;
    };
    var getShapes = function(view, model, track_id) {
	if (view.rendering_suppressed) {
	    return;
	}
	view.identified_shape_list_list[track_id] = model.getIdentifiedShapeListList(track_id, true, true);
    };
    
        
    var refreshCanvas = function(view) {
	getNewCanvas(view);
	getWebGLContextAndSetUpMatricesAndShaders(view);
    };
    
    OncoprintWebGLCellView.prototype.isUsable = function () {
	return this.ctx !== null;
    }
    OncoprintWebGLCellView.prototype.removeTrack = function (model, track_id) {
	clearZoneBuffers(this, model);
	delete this.identified_shape_list_list[track_id];
	delete this.vertex_position_array[track_id];
	delete this.vertex_color_array[track_id];
	
	renderAllTracks(this, model);
    }
    OncoprintWebGLCellView.prototype.moveTrack = function (model) {
	clearZoneBuffers(this, model);
	renderAllTracks(this, model);
    }
    OncoprintWebGLCellView.prototype.setTrackGroupOrder = function(model) {
	clearZoneBuffers(this, model);
	renderAllTracks(this, model);
    }
    
    OncoprintWebGLCellView.prototype.addTracks = function (model, track_ids) {
	clearZoneBuffers(this, model);
	for (var i=0; i<track_ids.length; i++) {
	    getShapes(this, model, track_ids[i]);
	    computeVertexPositionsAndVertexColors(this, model, track_ids[i]);
	}
	renderAllTracks(this, model);
    }
    OncoprintWebGLCellView.prototype.setIdOrder = function(model, ids) {
	clearZoneBuffers(this, model);
	var track_ids = model.getTracks();
	for (var i=0; i<track_ids.length; i++) {
	    computeVertexPositionsAndVertexColors(this, model, track_ids[i]);
	}
	renderAllTracks(this, model);
    }
    OncoprintWebGLCellView.prototype.setTrackGroupSortPriority = function(model) {
	clearZoneBuffers(this, model);
	renderAllTracks(this, model);
    }
    OncoprintWebGLCellView.prototype.sort = function(model) {
	clearZoneBuffers(this, model);
	var track_ids = model.getTracks();
	for (var i=0; i<track_ids.length; i++) {
	    computeVertexPositionsAndVertexColors(this, model, track_ids[i]);
	}
	renderAllTracks(this, model);
    }
    OncoprintWebGLCellView.prototype.suppressRendering = function() {
	this.rendering_suppressed = true;
    }
    OncoprintWebGLCellView.prototype.releaseRendering = function(model) {
	this.rendering_suppressed = false;
	var track_ids = model.getTracks();
	for (var i=0; i<track_ids.length; i++) {
	    getShapes(this, model, track_ids[i]);
	    computeVertexPositionsAndVertexColors(this, model, track_ids[i]);
	}
	renderAllTracks(this, model);
    }
    OncoprintWebGLCellView.prototype.hideIds = function(model) {
	clearZoneBuffers(this, model);
	var track_ids = model.getTracks();
	for (var i=0; i<track_ids.length; i++) {
	    computeVertexPositionsAndVertexColors(this, model, track_ids[i]);
	}
	renderAllTracks(this, model);
    }
    OncoprintWebGLCellView.prototype.setTrackData = function(model, track_id) {
	clearZoneBuffers(this, model, track_id);
	getShapes(this, model, track_id);
	computeVertexPositionsAndVertexColors(this, model, track_id);
	renderAllTracks(this, model);
    }
     OncoprintWebGLCellView.prototype.setRuleSet = function(model, target_track_id) {
	clearZoneBuffers(this, model, target_track_id);
	getShapes(this, model, target_track_id);
	computeVertexPositionsAndVertexColors(this, model, target_track_id);
	renderAllTracks(this, model);
    }
    OncoprintWebGLCellView.prototype.shareRuleSet = function(model, target_track_id) {
	clearZoneBuffers(this, model, target_track_id);
	getShapes(this, model, target_track_id);
	computeVertexPositionsAndVertexColors(this, model, target_track_id);
	renderAllTracks(this, model);
    }
    OncoprintWebGLCellView.prototype.setSortConfig = function(model) {
	this.sort(model);
    }
    
    OncoprintWebGLCellView.prototype.scroll = function(model, offset) {
	this.scroll_x = offset;
	renderAllTracks(this, model);
    }
    
    OncoprintWebGLCellView.prototype.setHorzZoom = function(model) {
	var cell_width = model.getCellWidth();
	if (cell_width < this.antialias_on_cell_width_thresh) {
	    if (!this.antialias) {
		this.antialias = true;
		refreshCanvas(this);
	    }
	} else {
	    if (this.antialias) {
		this.antialias = false;
		refreshCanvas(this);
	    }
	}
	clearZoneBuffers(this, model);
	var track_ids = model.getTracks();
	for (var i=0; i<track_ids.length; i++) {
	    // need to recompute this only because of rezoning for scrolls
	    computeVertexPositionsAndVertexColors(this, model, track_ids[i]);
	}
	renderAllTracks(this, model);
    }
    
    OncoprintWebGLCellView.prototype.setVertZoom = function(model) {
	clearZoneBuffers(this, model);
	var track_ids = model.getTracks();
	for (var i=0; i<track_ids.length; i++) {
	    getShapes(this, model, track_ids[i]);
	    computeVertexPositionsAndVertexColors(this, model, track_ids[i]);
	}
	renderAllTracks(this, model);
    }
    
    OncoprintWebGLCellView.prototype.getTotalWidth = function(model, base) {
	return (model.getCellWidth(base) + model.getCellPadding(base))*model.getIdOrder().length;
    }
    
    OncoprintWebGLCellView.prototype.getWidth = function() {
	return this.visible_area_width;
    }
    
    OncoprintWebGLCellView.prototype.setWidth = function(w, model) {
	this.visible_area_width = w;
	
	// need to rezone for new visible area width
	clearZoneBuffers(this, model);
	var track_ids = model.getTracks();
	for (var i=0; i<track_ids.length; i++) {
	    // need to recompute this only because of rezoning for scrolls
	    computeVertexPositionsAndVertexColors(this, model, track_ids[i]);
	}
	renderAllTracks(this, model); // in the process it will call resizeAndClear
    }
    
    OncoprintWebGLCellView.prototype.setCellPaddingOn = function(model) {
	clearZoneBuffers(this, model);
	var track_ids = model.getTracks();
	for (var i=0; i<track_ids.length; i++) {
	    if (!model.getTrackHasColumnSpacing(track_ids[i])) {
		// We need to recompute shapes for tracks that don't have column spacing,
		// because for those we're redefining the base width for shape generation.
		getShapes(this, model, track_ids[i]);
	    }
	    computeVertexPositionsAndVertexColors(this, model, track_ids[i]);
	}
	renderAllTracks(this, model);
    }
    
    OncoprintWebGLCellView.prototype.toSVGGroup = function(model, offset_x, offset_y) {
	var root = svgfactory.group((offset_x || 0), (offset_y || 0));
	var cell_tops = model.getCellTops();
	var tracks = model.getTracks();
	var zoomedColumnLeft = model.getZoomedColumnLeft();
	for (var i=0; i<tracks.length; i++) {
	    var track_id = tracks[i];
	    var offset_y = cell_tops[track_id];
	    var identified_shape_list_list = model.getIdentifiedShapeListList(track_id, false, true);
	    for (var j=0; j<identified_shape_list_list.length; j++) {
		var id_sl = identified_shape_list_list[j];
		var id = id_sl.id;
		var sl = id_sl.shape_list;
		var offset_x = zoomedColumnLeft[id];
		if (typeof offset_x === 'undefined') {
		    // hidden id
		    continue;
		}
		for (var h=0; h<sl.length; h++) {
		    root.appendChild(svgfactory.fromShape(sl[h], offset_x, offset_y));
		}
	    }
	}
	return root;
    }
    return OncoprintWebGLCellView;
})();

module.exports = OncoprintWebGLCellView;
