var gl_matrix = require('gl-matrix');
var svgfactory = require('./svgfactory.js');
var shapeToVertexes = require('./oncoprintshapetovertexes.js');
var CachedProperty = require('./CachedProperty.js');
var Shape = require('./oncoprintshape.js');

var sgndiff = function(a,b) {
    if (a < b) {
	return -1;
    } else if (a > b) {
	return 1;
    } else {
	return 0;
    }
};

var arrayFindIndex = function(arr, callback, start_index) {
    start_index = start_index || 0;
    for (var i=start_index; i<arr.length; i++) {
	if (callback(arr[i])) {
	    return i;
	}
    }
    return -1;
};

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
    function OncoprintWebGLCellView($container, $canvas, $overlay_canvas, $dummy_scroll_div_contents, model, tooltip, highlight_area_callback, cell_over_callback) {
	this.$container = $container;
	this.$canvas = $canvas;
	this.$overlay_canvas = $overlay_canvas;
	
	this.supersampling_ratio = 2;
	
	this.antialias = true;
	this.antialias_on_cell_width_thresh = 5;
	
	this.position_bit_pack_base = 128;
	
	getWebGLContextAndSetUpMatrices(this);
	setUpShaders(this);
	getOverlayContextAndClear(this);
	this.visible_area_width = $canvas[0].width;
	
	var self = this;
	
	this.tooltip = tooltip;
	this.tooltip.center = true;
	
	this.scroll_x = 0;
	this.scroll_y = 0;
	this.$dummy_scroll_div_contents = $dummy_scroll_div_contents;
	this.dummy_scroll_div_client_size = new CachedProperty({'width':$dummy_scroll_div_contents.parent()[0].clientWidth, 'height':$dummy_scroll_div_contents.parent()[0].clientHeight}, function() {
	    return {'width':$dummy_scroll_div_contents.parent()[0].clientWidth, 'height':$dummy_scroll_div_contents.parent()[0].clientHeight};
	});

	this.identified_shape_list_list = {};

	this.vertex_data = {}; // track_id -> {vertex_array: list of index in vertex bank, vertex_bank: flat list of concatenated position and color}
	this.vertex_column_array = {}; // track_id -> number list (float list, item size 1)
	
	this.vertex_position_buffer = {}; // track_id -> gl.createBuffer()
	this.vertex_color_buffer = {}; // track_id -> gl.createBuffer()
	this.vertex_column_buffer = {}; // track_id -> gl.createBuffer()
	this.color_texture = {}; // track_id -> gl.createTexture()
	
	this.id_to_first_vertex_index = {}; // track_id -> id -> vertex index of first vertex corresponding to this id

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
	    
	    var mouseInOverlayCanvas = function(mouse_x, mouse_y) {
		var offset = self.$overlay_canvas.offset();
		var width = self.$overlay_canvas.width();
		var height = self.$overlay_canvas.height();
		return (mouse_x >= offset.left && mouse_x < width + offset.left && mouse_y >= offset.top && mouse_y < height + offset.top);
	    };
	    $(document).on("mousemove", function(evt) {
		if (!mouseInOverlayCanvas(evt.pageX, evt.pageY)) {
		    clearOverlay(self);
		    tooltip.hide();
		    cell_over_callback(null);
		}
	    });
	    self.$overlay_canvas.on("mouseout mouseleave", function(evt) {
		clearOverlay(self);
		tooltip.hide();
	    });
	    self.$overlay_canvas.on("mousemove", function(evt) {
		if (self.rendering_suppressed) {
		    return;
		}
		clearOverlay(self);
		var offset = self.$overlay_canvas.offset();
		var mouseX = evt.pageX - offset.left;
		var mouseY = evt.pageY - offset.top;
		if (!dragging) {
		    var overlapping_cell = model.getOverlappingCell(mouseX + self.scroll_x, mouseY + self.scroll_y);
		    var overlapping_datum = (overlapping_cell === null ? null : model.getTrackDatum(overlapping_cell.track, overlapping_cell.id));
		    var cell_width = model.getCellWidth();
		    var cell_padding = model.getCellPadding();
		    if (overlapping_datum !== null) {
			cell_over_callback(overlapping_cell.track, overlapping_cell.id);
			var left = model.getZoomedColumnLeft(overlapping_cell.id) - self.scroll_x;
			overlayStrokeRect(self, left, model.getCellTops(overlapping_cell.track) - self.scroll_y, model.getCellWidth() + (model.getTrackHasColumnSpacing(overlapping_cell.track) ? 0 : cell_padding), model.getCellHeight(overlapping_cell.track), "rgba(0,0,0,1)");
			var tracks = model.getTracks();
			for (var i=0; i<tracks.length; i++) {
			    if (model.getTrackDatum(tracks[i], overlapping_cell.id) !== null) {
				overlayStrokeRect(self, left, model.getCellTops(tracks[i]) - self.scroll_y, model.getCellWidth() + (model.getTrackHasColumnSpacing(tracks[i]) ? 0 : cell_padding), model.getCellHeight(tracks[i]), "rgba(0,0,0,0.5)");
			    }
			}
			tooltip.show(250, model.getZoomedColumnLeft(overlapping_cell.id) + model.getCellWidth() / 2 + offset.left - self.scroll_x, model.getCellTops(overlapping_cell.track) + offset.top - self.scroll_y, model.getTrackTooltipFn(overlapping_cell.track)(overlapping_datum));
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
		if (overlapping_cell === null) {
		    cell_over_callback(null);
		}
	    });
	    
	    self.$overlay_canvas.on("mousedown", function(evt) {
		if (!mouseInOverlayCanvas(evt.pageX, evt.pageY)) {
		    return;
		}
		dragging = true;
		drag_start_x = evt.pageX - self.$overlay_canvas.offset().left;
		drag_end_x = drag_start_x;
		
		tooltip.hide();
	    });
	    self.$overlay_canvas.on("mouseup", function(evt) {
		if (!mouseInOverlayCanvas(evt.pageX, evt.pageY)) {
		    return;
		}
		executeDrag();
	    });
	    self.$overlay_canvas.on("mouseleave", function(evt) {
		executeDrag();
	    });
	    
	})(this);
	
	$dummy_scroll_div_contents.parent().scroll(function() {
	    clearOverlay(self);
	});
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
    var setUpShaders = function(self) {
	var vertex_shader_source = ['precision highp float;',
		'attribute float aPosVertex;',
		'attribute float aColVertex;',
		'attribute float aVertexOncoprintColumn;',
		'uniform float columnWidth;',
		'uniform float scrollX;',
		'uniform float zoomX;',
		'uniform float scrollY;',
		'uniform float zoomY;',
		'uniform mat4 uMVMatrix;',
		'uniform mat4 uPMatrix;',
		'uniform float offsetY;',
		'uniform float supersamplingRatio;',
		'uniform float positionBitPackBase;', 
		'uniform float texSize;',
		'varying float texCoord;',
		
		'vec3 unpackVec3(float packedVec3, float base) {',
		'	float pos0 = floor(packedVec3 / (base*base));',
		'	float pos0Contr = pos0*base*base;',
		'	float pos1 = floor((packedVec3 - pos0Contr)/base);',
		'	float pos1Contr = pos1*base;',
		'	float pos2 = packedVec3 - pos0Contr - pos1Contr;',
		'	return vec3(pos0, pos1, pos2);',
		'}',
		
		'void main(void) {',
		'	gl_Position = vec4(unpackVec3(aPosVertex, positionBitPackBase), 1.0);',
		'	gl_Position[0] += aVertexOncoprintColumn*columnWidth;',
		'	gl_Position *= vec4(zoomX, zoomY, 1.0, 1.0);',
		'	gl_Position[1] += offsetY;', // offset is given zoomed
		'	gl_Position -= vec4(scrollX, scrollY, 0.0, 0.0);',
		'	gl_Position[0] *= supersamplingRatio;',
		'	gl_Position[1] *= supersamplingRatio;',
		'	gl_Position = uPMatrix * uMVMatrix * gl_Position;',
		
		'	texCoord = (aColVertex + 0.5) / texSize;',
		'}'].join('\n');
	    var fragment_shader_source = ['precision mediump float;',
		'varying float texCoord;',
		'uniform sampler2D uSampler;',
		'void main(void) {',
		'   gl_FragColor = texture2D(uSampler, vec2(texCoord, 0.5));',
		'}'].join('\n');
	    var vertex_shader = createShader(self, vertex_shader_source, 'VERTEX_SHADER');
	    var fragment_shader = createShader(self, fragment_shader_source, 'FRAGMENT_SHADER');

	    var shader_program = createShaderProgram(self, vertex_shader, fragment_shader);
	    shader_program.vertexPositionAttribute = self.ctx.getAttribLocation(shader_program, 'aPosVertex');
	    self.ctx.enableVertexAttribArray(shader_program.vertexPositionAttribute);
	    shader_program.vertexColorAttribute = self.ctx.getAttribLocation(shader_program, 'aColVertex');
	    self.ctx.enableVertexAttribArray(shader_program.vertexColorAttribute);
	    shader_program.vertexOncoprintColumnAttribute = self.ctx.getAttribLocation(shader_program, 'aVertexOncoprintColumn');
	    self.ctx.enableVertexAttribArray(shader_program.vertexOncoprintColumnAttribute);

	    shader_program.samplerUniform = self.ctx.getUniformLocation(shader_program, 'uSampler');
	    shader_program.pMatrixUniform = self.ctx.getUniformLocation(shader_program, 'uPMatrix');
	    shader_program.mvMatrixUniform = self.ctx.getUniformLocation(shader_program, 'uMVMatrix');
	    shader_program.columnWidthUniform = self.ctx.getUniformLocation(shader_program, 'columnWidth');
	    shader_program.scrollXUniform = self.ctx.getUniformLocation(shader_program, 'scrollX');
	    shader_program.scrollYUniform = self.ctx.getUniformLocation(shader_program, 'scrollY');
	    shader_program.zoomXUniform = self.ctx.getUniformLocation(shader_program, 'zoomX');
	    shader_program.zoomYUniform = self.ctx.getUniformLocation(shader_program, 'zoomY');
	    shader_program.offsetYUniform = self.ctx.getUniformLocation(shader_program, 'offsetY');
	    shader_program.supersamplingRatioUniform = self.ctx.getUniformLocation(shader_program, 'supersamplingRatio');
	    shader_program.positionBitPackBaseUniform = self.ctx.getUniformLocation(shader_program, 'positionBitPackBase');
	    shader_program.texSizeUniform = self.ctx.getUniformLocation(shader_program, 'texSize');

	    self.shader_program = shader_program;
    };

    var resizeAndClear = function(view, model) {
	var height = model.getCellViewHeight();
	var total_width = view.getTotalWidth(model);
	var visible_area_width = view.visible_area_width;
	var scrollbar_slack = 20;
	view.$dummy_scroll_div_contents.css({'min-width':total_width, 'min-height':model.getOncoprintHeight()});
	view.$dummy_scroll_div_contents.parent().css({'height': height + scrollbar_slack, 'width': visible_area_width + scrollbar_slack}); // add space for scrollbars
	view.dummy_scroll_div_client_size.update();
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
	getWebGLContextAndSetUpMatrices(view);
	setUpShaders(view);
	getOverlayContextAndClear(view);
    };
    var renderAllTracks = function (view, model, dont_resize) {
	if (view.rendering_suppressed) {
	    return;
	}
	
	var scroll_x = view.scroll_x;
	var scroll_y = view.scroll_y;
	var zoom_x = model.getHorzZoom();
	var zoom_y = model.getVertZoom();
	
	var viewport = view.getViewportOncoprintSpace(model);
	var window_left = viewport.left;
	var window_right = viewport.right;
	var window_top = viewport.top;
	var window_bottom = viewport.bottom;
	var id_to_left = model.getColumnLeft();
	var id_order = model.getIdOrder();
	var horz_first_id_in_window_index = arrayFindIndex(id_order, function(id) { return id_to_left[id] >= window_left; });
	var horz_first_id_after_window_index = arrayFindIndex(id_order, function(id) { return id_to_left[id] > window_right; }, horz_first_id_in_window_index+1);
	var horz_first_id_in_window = (horz_first_id_in_window_index < 1 ? id_order[0] : id_order[horz_first_id_in_window_index - 1]);
	var horz_first_id_after_window = (horz_first_id_after_window_index === -1 ? null : id_order[horz_first_id_after_window_index]);
	
	if (!dont_resize) {
	    resizeAndClear(view, model);
	}
	view.ctx.clearColor(1.0,1.0,1.0,1.0);
	view.ctx.clear(view.ctx.COLOR_BUFFER_BIT | view.ctx.DEPTH_BUFFER_BIT);
	
	var tracks = model.getTracks();
	for (var i = 0; i < tracks.length; i++) {
	    var track_id = tracks[i];
	    var cell_top = model.getCellTops(track_id);
	    var cell_height = model.getCellHeight(track_id);
	    if ((cell_top / zoom_y) >= window_bottom || ((cell_top + cell_height)/zoom_y) < window_top) {
		// vertical clipping
		continue;
	    }
	    var buffers = getTrackBuffers(view, track_id);
	    if (buffers.position.numItems === 0) {
		continue;
	    }
	    var first_index = view.id_to_first_vertex_index[track_id][horz_first_id_in_window];
	    var first_index_out = horz_first_id_after_window === null ? buffers.position.numItems : view.id_to_first_vertex_index[track_id][horz_first_id_after_window];
	    
	    view.ctx.useProgram(view.shader_program);
	    view.ctx.bindBuffer(view.ctx.ARRAY_BUFFER, buffers.position);
	    view.ctx.vertexAttribPointer(view.shader_program.vertexPositionAttribute, buffers.position.itemSize, view.ctx.FLOAT, false, 0, 0);
	    view.ctx.bindBuffer(view.ctx.ARRAY_BUFFER, buffers.color);
	    view.ctx.vertexAttribPointer(view.shader_program.vertexColorAttribute, buffers.color.itemSize, view.ctx.FLOAT, false, 0, 0);
	    
	    view.ctx.bindBuffer(view.ctx.ARRAY_BUFFER, buffers.column);
	    view.ctx.vertexAttribPointer(view.shader_program.vertexOncoprintColumnAttribute, buffers.column.itemSize, view.ctx.FLOAT, false, 0, 0);

	    view.ctx.activeTexture(view.ctx.TEXTURE0);
	    view.ctx.bindTexture(view.ctx.TEXTURE_2D, buffers.color_tex.texture);
	    view.ctx.uniform1i(view.shader_program.samplerUniform, 0);
	    view.ctx.uniform1f(view.shader_program.texSizeUniform, buffers.color_tex.size);
	    
	    view.ctx.uniformMatrix4fv(view.shader_program.pMatrixUniform, false, view.pMatrix);
	    view.ctx.uniformMatrix4fv(view.shader_program.mvMatrixUniform, false, view.mvMatrix);
	    view.ctx.uniform1f(view.shader_program.columnWidthUniform, model.getCellWidth(true) + model.getCellPadding(true));
	    view.ctx.uniform1f(view.shader_program.scrollXUniform, scroll_x);
	    view.ctx.uniform1f(view.shader_program.scrollYUniform, scroll_y);
	    view.ctx.uniform1f(view.shader_program.zoomXUniform, zoom_x);
	    view.ctx.uniform1f(view.shader_program.zoomYUniform, zoom_y);
	    view.ctx.uniform1f(view.shader_program.offsetYUniform, cell_top);
	    view.ctx.uniform1f(view.shader_program.supersamplingRatioUniform, view.supersampling_ratio);
	    view.ctx.uniform1f(view.shader_program.positionBitPackBaseUniform, view.position_bit_pack_base);
	    
	    view.ctx.drawArrays(view.ctx.TRIANGLES, first_index, first_index_out - first_index);
	}
    };
    
    var clearTrackPositionAndColorBuffers = function(view, model, track_id) {
	var tracks_to_clear;
	if (typeof track_id === 'undefined') {
	    tracks_to_clear = model.getTracks();
	} else {
	    tracks_to_clear = [track_id];
	}
	for (var i=0; i<tracks_to_clear.length; i++) {
	    if (view.vertex_position_buffer[tracks_to_clear[i]]) {
		view.ctx.deleteBuffer(view.vertex_position_buffer[tracks_to_clear[i]]);
		delete view.vertex_position_buffer[tracks_to_clear[i]];
	    }
	    if (view.vertex_color_buffer[tracks_to_clear[i]]) {
		view.ctx.deleteBuffer(view.vertex_color_buffer[tracks_to_clear[i]]);
		delete view.vertex_color_buffer[tracks_to_clear[i]];
	    }
	    if (view.color_texture[tracks_to_clear[i]]) {
		view.ctx.deleteTexture(view.color_texture[tracks_to_clear[i]].texture);
		delete view.color_texture[tracks_to_clear[i]];
	    }
	}
    };
    
     var clearTrackColumnBuffers = function(view, model, track_id) {
	var tracks_to_clear;
	if (typeof track_id === 'undefined') {
	    tracks_to_clear = model.getTracks();
	} else {
	    tracks_to_clear = [track_id];
	}
	for (var i=0; i<tracks_to_clear.length; i++) {
	    if (view.vertex_column_buffer[tracks_to_clear[i]]) {
		view.ctx.deleteBuffer(view.vertex_column_buffer[tracks_to_clear[i]]);
		delete view.vertex_column_buffer[tracks_to_clear[i]];
	    }
	}
    };
    
    
    var getTrackBuffers = function(view, track_id) {
	if (typeof view.vertex_position_buffer[track_id] === 'undefined') {
	    var pos_buffer = view.ctx.createBuffer();
	    var pos_array = view.vertex_data[track_id].pos_array;
	    
	    view.ctx.bindBuffer(view.ctx.ARRAY_BUFFER, pos_buffer);
	    view.ctx.bufferData(view.ctx.ARRAY_BUFFER, new Float32Array(pos_array), view.ctx.STATIC_DRAW);
	    pos_buffer.itemSize = 1;
	    pos_buffer.numItems = pos_array.length / pos_buffer.itemSize;

	    view.vertex_position_buffer[track_id] = pos_buffer;
	}
	
	if (typeof view.vertex_color_buffer[track_id] === 'undefined') {
	    var col_buffer = view.ctx.createBuffer();
	    var col_array = view.vertex_data[track_id].col_array;
	    
	    view.ctx.bindBuffer(view.ctx.ARRAY_BUFFER, col_buffer);
	    view.ctx.bufferData(view.ctx.ARRAY_BUFFER, new Float32Array(col_array), view.ctx.STATIC_DRAW);
	    col_buffer.itemSize = 1;
	    col_buffer.numItems = col_array.length / col_buffer.itemSize;

	    view.vertex_color_buffer[track_id] = col_buffer;
	}
	
	if (typeof view.color_texture[track_id] === "undefined") {
	    var tex = view.ctx.createTexture();
	    view.ctx.bindTexture(view.ctx.TEXTURE_2D, tex);
	    
	    var color_bank = view.vertex_data[track_id].col_bank;
	    var width = Math.pow(2,Math.ceil(Math.log2(color_bank.length / 4)));
	    while (color_bank.length < 4*width) {
		color_bank.push(0);
	    }
	    var height = 1;
	    view.ctx.texImage2D(view.ctx.TEXTURE_2D, 0, view.ctx.RGBA, width, height, 0, view.ctx.RGBA, view.ctx.UNSIGNED_BYTE, new Uint8Array(color_bank));
	    view.ctx.texParameteri(view.ctx.TEXTURE_2D, view.ctx.TEXTURE_MIN_FILTER, view.ctx.NEAREST);
	    view.ctx.texParameteri(view.ctx.TEXTURE_2D, view.ctx.TEXTURE_MAG_FILTER, view.ctx.NEAREST);
	    view.color_texture[track_id] = {'texture': tex, 'size':width};
	}
	
	if (typeof view.vertex_column_buffer[track_id] === 'undefined') {
	    var vertex_column_buffer = view.ctx.createBuffer();
	    var vertex_column_array = view.vertex_column_array[track_id];
	    view.ctx.bindBuffer(view.ctx.ARRAY_BUFFER, vertex_column_buffer);
	    view.ctx.bufferData(view.ctx.ARRAY_BUFFER, new Float32Array(vertex_column_array), view.ctx.STATIC_DRAW);
	    vertex_column_buffer.itemSize = 1;
	    vertex_column_buffer.numItems = vertex_column_array.length / vertex_column_buffer.itemSize;
	    
	    view.vertex_column_buffer[track_id] = vertex_column_buffer;
	}
	return {'position':view.vertex_position_buffer[track_id],
		'color': view.vertex_color_buffer[track_id],
		'color_tex': view.color_texture[track_id],
		'column': view.vertex_column_buffer[track_id]};
    };
    
    var computeVertexColumns = function(view, model, track_id) {
	if (view.rendering_suppressed) {
	    return;
	}
	var num_items = view.vertex_data[track_id].pos_array.length;
	var id_to_first_vertex_index = view.id_to_first_vertex_index[track_id];
	var id_to_index = model.getVisibleIdToIndexMap();
	var id_and_first_vertex = Object.keys(id_to_first_vertex_index).map(function(id) { return [id, id_to_first_vertex_index[id]]; })
				.sort(function(a,b) { return sgndiff(a[1], b[1]); });
	var vertex_column_array = [];
	for (var i=0; i<id_and_first_vertex.length; i++) {
	    var num_to_add = (i === id_and_first_vertex.length - 1 ? num_items : id_and_first_vertex[i+1][1]) - id_and_first_vertex[i][1];
	    var column = id_to_index[id_and_first_vertex[i][0]];
	    for (var j=0; j<num_to_add; j++) {
		vertex_column_array.push(column);
	    }
	}
	view.vertex_column_array[track_id] = vertex_column_array;
	clearTrackColumnBuffers(view, model, track_id);
    };
    
    var computeVertexPositionsAndVertexColors = function (view, model, track_id) {
	if (view.rendering_suppressed) {
	    return;
	}
	var identified_shape_list_list = view.identified_shape_list_list[track_id];
	var id_to_index = model.getIdToIndexMap();
	identified_shape_list_list.sort(function(a, b) {
	    return sgndiff(id_to_index[a.id], id_to_index[b.id]);
	});
	// Compute vertex array
	var vertex_pos_array = [];
	var vertex_col_array = [];
	var id_to_first_vertex_index = {};
	
	var color_bank = [];
	var color_bank_index = {};
	
	var hashVector = function(vert) {
	    return vert.join(",");
	};
	
	var position_bit_pack_base = view.position_bit_pack_base;
	var packPos = function(pos) {
	    // values must be in [0,255] (integer)
	    return position_bit_pack_base*position_bit_pack_base*pos[0] + position_bit_pack_base*pos[1] + pos[2];
	};
	
	var vertexifiedShapes = {};
	
	
	for (var i = 0; i < identified_shape_list_list.length; i++) {
	    var shape_list = identified_shape_list_list[i].shape_list;
	    var id = identified_shape_list_list[i].id;

	    id_to_first_vertex_index[id] = vertex_pos_array.length;
	    
	    for (var j = 0; j < shape_list.length; j++) {
		var shape = shape_list[j];
		var hash = Shape.Shape.hashComputedShape(shape, j);
		if (!vertexifiedShapes.hasOwnProperty(hash)) {
		    vertexifiedShapes[hash] = {position:[], color:[]};
		    var position = vertexifiedShapes[hash].position;
		    var color = vertexifiedShapes[hash].color;
		    shapeToVertexes(shape, j, function(pos, col) {
			pos = pos.map(Math.round);
			col = col.map(function(x) { return Math.round(x*255);});
			
			position.push(packPos(pos));
			
			var col_hash = hashVector(col);
			var col_index = color_bank_index[col_hash];
			if (typeof col_index === "undefined") {
			    col_index = color_bank.length;
			    color_bank.push(col);
			    color_bank_index[col_hash] = col_index;
			}
			color.push(col_index);
		    });
		}
		vertex_pos_array.push.apply(vertex_pos_array, vertexifiedShapes[hash].position);
		vertex_col_array.push.apply(vertex_col_array, vertexifiedShapes[hash].color);
	    }
	}
	color_bank = color_bank.reduce(function(arr, next) { return arr.concat(next); }, []);
	// minimum color bank to avoid webGL texture errors
	if (color_bank.length === 0) {
	    color_bank.push(0,0,0,0);
	}
	view.vertex_data[track_id] = {
	    pos_array: vertex_pos_array,
	    col_array: vertex_col_array,
	    col_bank: color_bank
	};
	view.id_to_first_vertex_index[track_id] = id_to_first_vertex_index;
	
	clearTrackPositionAndColorBuffers(view, model, track_id);
    };
    
    var getShapes = function(view, model, track_id) {
	if (view.rendering_suppressed) {
	    return;
	}
	view.identified_shape_list_list[track_id] = model.getIdentifiedShapeListList(track_id, true, true);
    };
    
    var refreshCanvas = function(view, model) {
	clearTrackPositionAndColorBuffers(view, model); // whenever you get a new context, you have to get new buffers
	clearTrackColumnBuffers(view, model);
	getNewCanvas(view);
	getWebGLContextAndSetUpMatrices(view, model);
	setUpShaders(view);
    };
    
    OncoprintWebGLCellView.prototype.getViewportOncoprintSpace = function(model) {
	var scroll_x = this.scroll_x;
	var scroll_y = this.scroll_y;
	var zoom_x = model.getHorzZoom();
	var zoom_y = model.getVertZoom();
	
	var window_left = Math.round(scroll_x / zoom_x);
	var window_right = Math.round((scroll_x + this.visible_area_width) / zoom_x);
	var window_top = Math.round(scroll_y / zoom_y);
	var window_bottom = Math.round((scroll_y + model.getCellViewHeight()) / zoom_y);
	
	return {
	    'top': window_top,
	    'bottom': window_bottom,
	    'left': window_left,
	    'right': window_right
	};
    }
    OncoprintWebGLCellView.prototype.isUsable = function () {
	return this.ctx !== null;
    }
    OncoprintWebGLCellView.prototype.removeTrack = function (model, track_id) {
	if (this.rendering_suppressed) {
	    return;
	};
	delete this.identified_shape_list_list[track_id];
	delete this.vertex_data[track_id];
	delete this.vertex_column_array[track_id];
	delete this.id_to_first_vertex_index[track_id];
	
	clearTrackPositionAndColorBuffers(this, model, track_id);
	clearTrackColumnBuffers(this, model, track_id);
	
	renderAllTracks(this, model);
    }
    OncoprintWebGLCellView.prototype.moveTrack = function (model) {
	if (this.rendering_suppressed) {
	    return;
	};
	renderAllTracks(this, model);
    }
    OncoprintWebGLCellView.prototype.setTrackGroupOrder = function(model) {
	clearZoneBuffers(this, model);
	renderAllTracks(this, model);
    }
    
    OncoprintWebGLCellView.prototype.addTracks = function (model, track_ids) {
	if (this.rendering_suppressed) {
	    return;
	};
	for (var i=0; i<track_ids.length; i++) {
	    getShapes(this, model, track_ids[i]);
	    computeVertexPositionsAndVertexColors(this, model, track_ids[i]);
	    computeVertexColumns(this, model, track_ids[i]);
	}
	renderAllTracks(this, model);
    }
    OncoprintWebGLCellView.prototype.setIdOrder = function(model, ids) {
	if (this.rendering_suppressed) {
	    return;
	};
	var track_ids = model.getTracks();
	for (var i=0; i<track_ids.length; i++) {
	    computeVertexColumns(this, model, track_ids[i]);
	}
	renderAllTracks(this, model);
    }
    OncoprintWebGLCellView.prototype.setTrackGroupSortPriority = function(model) {
	if (this.rendering_suppressed) {
	    return;
	};
	renderAllTracks(this, model);
    }
    OncoprintWebGLCellView.prototype.sort = function(model) {
	if (this.rendering_suppressed) {
	    return;
	};
	var track_ids = model.getTracks();
	for (var i=0; i<track_ids.length; i++) {
	    computeVertexPositionsAndVertexColors(this, model, track_ids[i]); // need to recompute because the vertexes are in sorted order for clipping
	    computeVertexColumns(this, model, track_ids[i]);
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
	    computeVertexColumns(this, model, track_ids[i]);
	}
	renderAllTracks(this, model);
    }
    OncoprintWebGLCellView.prototype.hideIds = function(model) {
	if (this.rendering_suppressed) {
	    return;
	};
	var track_ids = model.getTracks();
	for (var i=0; i<track_ids.length; i++) {
	    computeVertexColumns(this, model, track_ids[i]);
	}
	renderAllTracks(this, model);
    }
    OncoprintWebGLCellView.prototype.setTrackData = function(model, track_id) {
	if (this.rendering_suppressed) {
	    return;
	};
	getShapes(this, model, track_id);
	computeVertexPositionsAndVertexColors(this, model, track_id);
	computeVertexColumns(this, model, track_id);
	renderAllTracks(this, model);
    }
     OncoprintWebGLCellView.prototype.setRuleSet = function(model, target_track_id) {
	 if (this.rendering_suppressed) {
	    return;
	};
	getShapes(this, model, target_track_id);
	computeVertexPositionsAndVertexColors(this, model, target_track_id);
	renderAllTracks(this, model);
    }
    OncoprintWebGLCellView.prototype.shareRuleSet = function(model, target_track_id) {
	if (this.rendering_suppressed) {
	    return;
	};
	getShapes(this, model, target_track_id);
	computeVertexPositionsAndVertexColors(this, model, target_track_id);
	renderAllTracks(this, model);
    }
    OncoprintWebGLCellView.prototype.setSortConfig = function(model) {
	if (this.rendering_suppressed) {
	    return;
	};
	this.sort(model);
    }
    
    OncoprintWebGLCellView.prototype.setHorzScroll = function(model) {
	if (this.rendering_suppressed) {
	    return;
	};
	this.setScroll(model);
    }
    
    OncoprintWebGLCellView.prototype.setVertScroll = function(model) {
	if (this.rendering_suppressed) {
	    return;
	};
	this.setScroll(model);
    }
    
    OncoprintWebGLCellView.prototype.setScroll = function(model) {
	if (this.rendering_suppressed) {
	    return;
	};
	this.scroll_x = model.getHorzScroll();
	this.scroll_y = model.getVertScroll();
	renderAllTracks(this, model, true);
    }
    
    var updateAntialiasSetting = function(view, model) {
	var cell_width = model.getCellWidth();
	if (cell_width < view.antialias_on_cell_width_thresh) {
	    if (!view.antialias) {
		view.antialias = true;
		refreshCanvas(view, model);
	    }
	} else {
	    if (view.antialias) {
		view.antialias = false;
		refreshCanvas(view, model);
	    }
	}
    };
    
    OncoprintWebGLCellView.prototype.setZoom = function(model) {
	if (this.rendering_suppressed) {
	    return;
	};
	updateAntialiasSetting(this, model);
	renderAllTracks(this, model);
    }
    
    OncoprintWebGLCellView.prototype.setHorzZoom = function(model) {
	if (this.rendering_suppressed) {
	    return;
	};
	updateAntialiasSetting(this, model);
	renderAllTracks(this, model);
    }
    
    OncoprintWebGLCellView.prototype.setVertZoom = function(model) {
	if (this.rendering_suppressed) {
	    return;
	};
	renderAllTracks(this, model);
    }
    
    OncoprintWebGLCellView.prototype.setViewport = function(model) {
	if (this.rendering_suppressed) {
	    return;
	};
	this.scroll_x = model.getHorzScroll();
	this.scroll_y = model.getVertScroll();
	updateAntialiasSetting(this, model);
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
	if (this.rendering_suppressed) {
	    return;
	};
	renderAllTracks(this, model); // in the process it will call resizeAndClear
    }
    
    OncoprintWebGLCellView.prototype.setCellPaddingOn = function(model) {
	if (this.rendering_suppressed) {
	    return;
	};
	var track_ids = model.getTracks();
	for (var i=0; i<track_ids.length; i++) {
	    if (!model.getTrackHasColumnSpacing(track_ids[i])) {
		// We need to recompute shapes for tracks that don't have column spacing,
		// because for those we're redefining the base width for shape generation.
		getShapes(this, model, track_ids[i]);
		computeVertexPositionsAndVertexColors(this, model, track_ids[i]);
	    }
	    computeVertexColumns(this, model, track_ids[i]);
	}
	renderAllTracks(this, model);
    }
    
    OncoprintWebGLCellView.prototype.getDummyScrollDivClientSize = function() {
	return this.dummy_scroll_div_client_size.get();
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
