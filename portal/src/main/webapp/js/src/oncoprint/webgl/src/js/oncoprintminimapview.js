var gl_matrix = require('gl-matrix');
var OncoprintZoomSlider = require('./oncoprintzoomslider.js');

var arrayFindIndex = function (arr, callback, start_index) {
    start_index = start_index || 0;
    for (var i = start_index; i < arr.length; i++) {
	if (callback(arr[i])) {
	    return i;
	}
    }
    return -1;
};

var getWebGLCanvasContext = function (view) {
    try {
	var canvas = view.$canvas[0];
	var ctx = canvas.getContext("experimental-webgl", {alpha: false, antialias: true});
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
var getWebGLContextAndSetUpMatrices = function (view) {
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
var setUpShaders = function(self, vertex_bank_size) {
    var vertex_shader_source = ['precision highp float;',
	'attribute float aPosVertex;',
	'attribute float aColVertex;',
	'attribute float aVertexOncoprintColumn;',
	'uniform float columnWidth;',
	'uniform float zoomX;',
	'uniform float zoomY;',
	'uniform mat4 uMVMatrix;',
	'uniform mat4 uPMatrix;',
	'uniform float offsetY;',
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
	'	gl_Position[1] += offsetY;',
	'	gl_Position *= vec4(zoomX, zoomY, 1.0, 1.0);',
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
	    shader_program.zoomXUniform = self.ctx.getUniformLocation(shader_program, 'zoomX');
	    shader_program.zoomYUniform = self.ctx.getUniformLocation(shader_program, 'zoomY');
	    shader_program.offsetYUniform = self.ctx.getUniformLocation(shader_program, 'offsetY');
	    shader_program.positionBitPackBaseUniform = self.ctx.getUniformLocation(shader_program, 'positionBitPackBase');
	    shader_program.texSizeUniform = self.ctx.getUniformLocation(shader_program, 'texSize');

	    self.shader_program = shader_program;
};

var clamp = function(x, lower, upper) {
    return Math.max(lower, Math.min(upper, x));
};

var OncoprintMinimapView = (function () {

    function OncoprintMinimapView($div, $canvas, $overlay_canvas, model, cell_view, width, height, drag_callback, viewport_callback, horz_zoom_callback, vert_zoom_callback, zoom_to_fit_callback, close_callback) {
	this.$div = $div;
	this.$canvas = $canvas;
	this.$overlay_canvas = $overlay_canvas;

	var self = this;
	var padding = 4;
	var vertical_zoom_area_width = 20;
	var horizontal_zoom_area_height = 20;
	var window_bar_height = 20;

	this.layout_numbers = {
	    window_width: padding + width + padding + vertical_zoom_area_width,
	    window_height: window_bar_height + padding + height + padding + horizontal_zoom_area_height,
	    vertical_zoom_area_width: vertical_zoom_area_width,
	    horizontal_zoom_area_height: horizontal_zoom_area_height,
	    padding: padding,
	    window_bar_height: window_bar_height,
	    canvas_left: padding,
	    canvas_top: window_bar_height + padding,
	};
	this.$div.css({'min-width': this.layout_numbers.window_width,
			'min-height': this.layout_numbers.window_height,
			'outline':'solid 1px black', 'background-color':'#ffffff'});
		    
	this.$window_bar = $('<div>').css({'position':'absolute', 
					    'min-width': this.layout_numbers.window_width,
					    'min-height': this.layout_numbers.window_bar_height,
					    'background-color':'#cccccc'})
						.appendTo(this.$div);
					
	this.$close_btn = $('<div>').css({'position':'absolute',
					    'top': 3,
					    'left': 3,
					    'min-width': this.layout_numbers.window_bar_height - 6,
					    'min-height': this.layout_numbers.window_bar_height - 6,
					    'cursor': 'pointer'})
						.appendTo(this.$div);
	$('<span>').addClass("icon fa fa-times-circle").css('font-size', (this.layout_numbers.window_bar_height - 6) + "px").appendTo(this.$close_btn);
	
	this.$close_btn.click(close_callback || function(){});
	
	this.$canvas[0].width = width;
	this.$canvas[0].height = height;
	this.$canvas.css({'top': this.layout_numbers.canvas_top, 'left':this.layout_numbers.canvas_left});
	this.$overlay_canvas[0].width = width;
	this.$overlay_canvas[0].height = width;
	this.$overlay_canvas.css({'top': this.layout_numbers.canvas_top, 'left':this.layout_numbers.canvas_left, 'outline':'solid 1px #444444'});
	
	this.horizontal_zoom = new OncoprintZoomSlider(this.$div, {'btn_size': this.layout_numbers.horizontal_zoom_area_height - padding,
								    'horizontal': true,
								    'width': width,
								    'init_val': model.getHorzZoom(),
								    'left': padding,
								    'top': this.layout_numbers.canvas_top + height + padding,
								    'onChange': function(val) { horz_zoom_callback(val); }});
	this.vertical_zoom = new OncoprintZoomSlider(this.$div, {'btn_size': this.layout_numbers.vertical_zoom_area_width - padding,
								    'vertical': true,
								    'height': height,
								    'init_val': model.getVertZoom(),
								    'left': this.layout_numbers.canvas_left + width + padding,
								    'top': this.layout_numbers.window_bar_height + padding,
								    'onChange': function(val) { vert_zoom_callback(val); }});							
	
	(function setUpZoomToFitButton() {
	    var btn_height = self.layout_numbers.horizontal_zoom_area_height - padding;
	    var btn_width = self.layout_numbers.vertical_zoom_area_width - padding;
	    var $btn = $('<div>').css({'position': 'absolute',
		'min-height': btn_height,
		'min-width': btn_width,
		'outline': 'solid 1px black',
		'left': self.layout_numbers.canvas_left + width + padding,
		'top': self.layout_numbers.canvas_top + height + padding,
		'background-size': (btn_width - 4) + 'px '+ (btn_height - 4) + 'px',
		'background-position': '2px 2px',
		'cursor': 'pointer'}).addClass('oncoprint-zoomtofit-btn')
		    .appendTo($div);
	    $btn.hover(function () {
		$(this).css({'background-color': '#cccccc'});
	    }, function () {
		$(this).css({'background-color': '#ffffff'});
	    });
	    
	    zoom_to_fit_callback = zoom_to_fit_callback || function() {};
	    $btn.click(zoom_to_fit_callback);
	})();
	getWebGLContextAndSetUpMatrices(this);
	setUpShaders(this);
	this.overlay_ctx = $overlay_canvas[0].getContext("2d");

	this.img = new Image();
	this.current_rect = {'top': 0, 'left': 0, 'width': 0, 'height': 0, 'col':0, 'num_cols':0};

	var self = this;
	this.img.onload = function () {
	    self.ctx.drawImage(img, 0, 0);
	};

	// Set up dragging
	var resize_hit_zone = 5;
	var mouseInRectDragZone = function (x, y) {
	    return ((x >= self.current_rect.left + resize_hit_zone) &&
		    (x <= self.current_rect.left + self.current_rect.width - resize_hit_zone) &&
		    (y >= self.current_rect.top + resize_hit_zone) &&
		    (y <= self.current_rect.top + self.current_rect.height - resize_hit_zone));
	};
	var mouseInsideRectHitZone = function(x,y) {
	    return (x >= self.current_rect.left - resize_hit_zone) && 
		    (x <= self.current_rect.left + self.current_rect.width + resize_hit_zone) &&
		    (y >= self.current_rect.top - resize_hit_zone) && 
		    (y <= self.current_rect.top + self.current_rect.height + resize_hit_zone);
	};
	var mouseInRightHorzResizeZone = function (x,y) {
	    return !mouseInTopLeftResizeZone(x,y) && !mouseInTopRightResizeZone(x,y) &&
		    !mouseInBottomLeftResizeZone(x,y) && !mouseInBottomRightResizeZone(x,y) &&
		    mouseInsideRectHitZone(x,y) &&
		    (Math.abs(x - (self.current_rect.left + self.current_rect.width)) < resize_hit_zone);
	};
	var mouseInLeftHorzResizeZone = function (x,y) {
	    return !mouseInTopLeftResizeZone(x,y) && !mouseInTopRightResizeZone(x,y) &&
		    !mouseInBottomLeftResizeZone(x,y) && !mouseInBottomRightResizeZone(x,y) &&
		    mouseInsideRectHitZone(x,y) &&
		    (Math.abs(x - self.current_rect.left) < resize_hit_zone);
	};
	var mouseInTopVertResizeZone = function (x,y) {
	    return !mouseInTopLeftResizeZone(x,y) && !mouseInTopRightResizeZone(x,y) &&
		    !mouseInBottomLeftResizeZone(x,y) && !mouseInBottomRightResizeZone(x,y) &&
		    mouseInsideRectHitZone(x,y) &&
		    (Math.abs(y - self.current_rect.top) < resize_hit_zone);
	};
	var mouseInBottomVertResizeZone = function (x, y) {
	    return !mouseInTopLeftResizeZone(x, y) && !mouseInTopRightResizeZone(x, y) &&
		    !mouseInBottomLeftResizeZone(x, y) && !mouseInBottomRightResizeZone(x, y) &&
		    mouseInsideRectHitZone(x,y) &&
		    (Math.abs(y - (self.current_rect.top + self.current_rect.height)) < resize_hit_zone);
	};
	var mouseInTopLeftResizeZone = function(x,y) {
	    return (Math.abs(y - self.current_rect.top) < resize_hit_zone) &&
		    (Math.abs(x - self.current_rect.left) < resize_hit_zone);
	};
	var mouseInBottomLeftResizeZone = function(x,y) {
	    return (Math.abs(y - (self.current_rect.top + self.current_rect.height)) < resize_hit_zone) &&
		    (Math.abs(x - self.current_rect.left) < resize_hit_zone);
	};
	var mouseInTopRightResizeZone = function(x,y) {
	    return (Math.abs(y - self.current_rect.top) < resize_hit_zone) &&
		    (Math.abs(x - (self.current_rect.left + self.current_rect.width)) < resize_hit_zone);
	};
	var mouseInBottomRightResizeZone = function(x,y) {
	    return (Math.abs(y - (self.current_rect.top + self.current_rect.height)) < resize_hit_zone) &&
		    (Math.abs(x - (self.current_rect.left + self.current_rect.width)) < resize_hit_zone);
	};
	
	this.resize_hover = false;
	var updateRectResizeHoverLocation = function(x,y) {
	    if (typeof x === "undefined") {
		self.resize_hover = false;
	    } else {
		if (mouseInRightHorzResizeZone(x, y)) {
		    self.resize_hover = "r";
		} else if (mouseInLeftHorzResizeZone(x, y)) {
		    self.resize_hover = "l";
		} else if (mouseInTopVertResizeZone(x, y)) {
		    self.resize_hover = "t";
		} else if (mouseInBottomVertResizeZone(x, y)) {
		    self.resize_hover = "b";
		} else if (mouseInTopLeftResizeZone(x, y)) {
		    self.resize_hover = "tl";
		} else if (mouseInBottomRightResizeZone(x, y)) {
		    self.resize_hover = "br";
		} else if (mouseInBottomLeftResizeZone(x, y)) {
		    self.resize_hover = "bl";
		} else if (mouseInTopRightResizeZone(x, y)) {
		    self.resize_hover = "tr";
		} else {
		    self.resize_hover = false;
		}
	    }
	};
	var updateCSSCursor = function(x, y) {
	    var cursor_val;
	    if (typeof x === "undefined") {
		cursor_val = 'auto';
	    } else {
		if (mouseInRectDragZone(x, y)) {
		    cursor_val = 'move';
		} else if (mouseInRightHorzResizeZone(x, y) || mouseInLeftHorzResizeZone(x, y)) {
		    cursor_val = 'ew-resize';
		} else if (mouseInTopVertResizeZone(x, y) || mouseInBottomVertResizeZone(x, y)) {
		    cursor_val = 'ns-resize';
		} else if (mouseInTopLeftResizeZone(x, y) || mouseInBottomRightResizeZone(x, y)) {
		    cursor_val = 'nwse-resize';
		} else if (mouseInBottomLeftResizeZone(x, y) || mouseInTopRightResizeZone(x, y)) {
		    cursor_val = 'nesw-resize';
		} else {
		    cursor_val = 'auto';
		}
	    }
	    $div.css('cursor', cursor_val);
	};
	var getCanvasMouse = function(view, div_mouse_x, div_mouse_y) {
	    var canv_top = parseInt(view.$canvas[0].style.top, 10);
	    var canv_left = parseInt(view.$canvas[0].style.left, 10);
	    var canv_width = parseInt(view.$canvas[0].width, 10);
	    var canv_height = parseInt(view.$canvas[0].height, 10);
	    
	    var mouse_x = div_mouse_x - canv_left;
	    var mouse_y = div_mouse_y - canv_top;
	    
	    var outside = mouse_x < 0 || mouse_x >= canv_width || mouse_y < 0 || mouse_y >= canv_height;
	    
	    return {'mouse_x': mouse_x,
		    'mouse_y': mouse_y,
		    'outside': outside};
	};
	var dragging = false;
	var drag_type = false;
	var drag_start_col = -1;
	var drag_start_vert_scroll = -1;
	var drag_start_x = -1;
	var drag_start_y = -1;
	var drag_start_vert_zoom = -1;
	var y_ratio = -1;
	$(document).on("mousedown", function (evt) {
	    var offset = self.$div.offset();
	    var overlay_mouse_x = evt.pageX - offset.left;
	    var overlay_mouse_y = evt.pageY - offset.top;
	    var mouse = getCanvasMouse(self, overlay_mouse_x, overlay_mouse_y);
	    
	    if (!mouse.outside) {
		var mouse_x = mouse.mouse_x;
		var mouse_y = mouse.mouse_y;
		dragging = false;
		drag_type = false;


		y_ratio = model.getOncoprintHeight() / parseInt(self.$canvas[0].height, 10);
		if (mouseInRectDragZone(mouse_x, mouse_y)) {
		    drag_type = "move";
		} else if (mouseInRightHorzResizeZone(mouse_x, mouse_y)) {
		    drag_type = "resize_r";
		} else if (mouseInLeftHorzResizeZone(mouse_x, mouse_y)) {
		    drag_type = "resize_l";
		} else if (mouseInTopVertResizeZone(mouse_x, mouse_y)) {
		    drag_type = "resize_t";
		} else if (mouseInBottomVertResizeZone(mouse_x, mouse_y)) {
		    drag_type = "resize_b";
		} else if (mouseInTopRightResizeZone(mouse_x, mouse_y)) {
		    drag_type = "resize_tr";
		} else if (mouseInBottomRightResizeZone(mouse_x, mouse_y)) {
		    drag_type = "resize_br";
		} else if (mouseInTopLeftResizeZone(mouse_x, mouse_y)) {
		    drag_type = "resize_tl";
		} else if (mouseInBottomLeftResizeZone(mouse_x, mouse_y)) {
		    drag_type = "resize_bl";
		}
		if (drag_type !== false) {
		    dragging = true;
		    drag_start_x = mouse_x;
		    drag_start_y = mouse_y;
		    drag_start_col = Math.floor(model.getHorzScroll() / (model.getCellWidth() + model.getCellPadding()));
		    drag_start_vert_scroll = model.getVertScroll();
		    drag_start_vert_zoom = model.getVertZoom();
		    drag_start_rect = self.current_rect;
		}
	    }
	});
	$(document).on("mousemove", function (evt) {
	    var offset = self.$div.offset();
	    var overlay_mouse_x = evt.pageX - offset.left;
	    var overlay_mouse_y = evt.pageY - offset.top;
	    var mouse = getCanvasMouse(self, overlay_mouse_x, overlay_mouse_y);
	    var mouse_x = mouse.mouse_x;
	    var mouse_y = mouse.mouse_y;
	    var zoom = getZoom(self, model);
	    var cell_width = model.getCellWidth(true)*zoom.x;
	    if (dragging) {
		evt.preventDefault();
		var delta_col = Math.floor(mouse_x / cell_width) - Math.floor(drag_start_x / cell_width);
		var delta_y = mouse_y - drag_start_y;
		if (drag_type === "move") {
		    var delta_y_scroll = delta_y * y_ratio;
		    drag_callback((drag_start_col + delta_col)*(model.getCellWidth() + model.getCellPadding()), drag_start_vert_scroll + delta_y_scroll);
		} else {
		    var render_rect;
		    var zoom = getZoom(self, model);
		    var max_num_cols = model.getIdOrder().length;
		    var min_num_cols = Math.ceil(cell_view.getWidth() / (model.getCellWidth(true) + model.getCellPadding(true, true)));
		    var max_height = model.getOncoprintHeight(true) * zoom.y;
		    var min_height = model.getCellViewHeight() * zoom.y;
		    var drag_start_right_col = drag_start_rect.col + drag_start_rect.num_cols;
		    var drag_start_bottom = drag_start_rect.top + drag_start_rect.height;
		    if (drag_type === "resize_r") {
			// Width must be valid
			delta_col = clamp(delta_col, 
					min_num_cols - drag_start_rect.num_cols,
					max_num_cols - drag_start_rect.num_cols);
			// Right must be valid
			delta_col = Math.min(delta_col, max_num_cols - drag_start_right_col);
			render_rect = {
			    'top': drag_start_rect.top,
			    'col': drag_start_rect.col,
			    'num_cols': drag_start_rect.num_cols + delta_col,
			    'height': drag_start_rect.height
			};
		    } else if (drag_type === "resize_l") {
			// Width must be valid
			delta_col = clamp(delta_col,
					drag_start_rect.num_cols - max_num_cols,
					drag_start_rect.num_cols - min_num_cols);
			// Left must be valid
			delta_col = Math.max(delta_col, -drag_start_rect.col);
			render_rect = {
			    'top': drag_start_rect.top,
			    'col': drag_start_rect.col + delta_col,
			    'num_cols': drag_start_rect.num_cols - delta_col,
			    'height': drag_start_rect.height
			};
		    } else if (drag_type === "resize_t") {
			// Height must be valid
			delta_y = clamp(delta_y,
					drag_start_rect.height - max_height,
					drag_start_rect.height - min_height);
			// Top must be valid
			delta_y = Math.max(delta_y, -drag_start_rect.top);
			render_rect = {
			    'top': drag_start_rect.top + delta_y,
			    'col': drag_start_rect.col,
			    'num_cols': drag_start_rect.num_cols,
			    'height': drag_start_rect.height - delta_y
			};
		    } else if (drag_type === "resize_b") {
			// Height must be valid
			delta_y = clamp(delta_y,
					min_height - drag_start_rect.height,
					max_height - drag_start_rect.height);
			// Bottom must be valid
			delta_y = Math.min(delta_y, max_height - drag_start_bottom);
			render_rect = {
			    'top': drag_start_rect.top,
			    'col': drag_start_rect.col,
			    'num_cols': drag_start_rect.num_cols,
			    'height': drag_start_rect.height + delta_y
			};
		    } else if (drag_type === "resize_tr") {
			// Width must be valid
			delta_col = clamp(delta_col, 
					min_num_cols - drag_start_rect.num_cols,
					max_num_cols - drag_start_rect.num_cols);
			// Right must be valid
			delta_col = Math.min(delta_col, max_num_cols - drag_start_right_col);
			// Height must be valid
			delta_y = clamp(delta_y,
					drag_start_rect.height - max_height,
					drag_start_rect.height - min_height);
			// Top must be valid
			delta_y = Math.max(delta_y, -drag_start_rect.top);
			render_rect = {
			    'top': drag_start_rect.top + delta_y,
			    'col': drag_start_rect.col,
			    'num_cols': drag_start_rect.num_cols + delta_col,
			    'height': drag_start_rect.height - delta_y
			};
		    } else if (drag_type === "resize_tl") {
			// Width must be valid
			delta_col = clamp(delta_col,
					drag_start_rect.num_cols - max_num_cols,
					drag_start_rect.num_cols - min_num_cols);
			// Left must be valid
			delta_col = Math.max(delta_col, -drag_start_rect.col);
			// Height must be valid
			delta_y = clamp(delta_y,
					drag_start_rect.height - max_height,
					drag_start_rect.height - min_height);
			// Top must be valid
			delta_y = Math.max(delta_y, -drag_start_rect.top);
			render_rect = {
			    'top': drag_start_rect.top + delta_y,
			    'col': drag_start_rect.col + delta_col,
			    'num_cols': drag_start_rect.num_cols - delta_col,
			    'height': drag_start_rect.height - delta_y
			};
		    } else if (drag_type === "resize_br") {
			// Height must be valid
			delta_y = clamp(delta_y,
					min_height - drag_start_rect.height,
					max_height - drag_start_rect.height);
			// Bottom must be valid
			delta_y = Math.min(delta_y, max_height - drag_start_bottom);
			// Width must be valid
			delta_col = clamp(delta_col, 
					min_num_cols - drag_start_rect.num_cols,
					max_num_cols - drag_start_rect.num_cols);
			// Right must be valid
			delta_col = Math.min(delta_col, max_num_cols - drag_start_right_col);
			render_rect = {
			    'top': drag_start_rect.top,
			    'col': drag_start_rect.col,
			    'num_cols': drag_start_rect.num_cols + delta_col,
			    'height': drag_start_rect.height + delta_y
			};
		    } else if (drag_type === "resize_bl") {
			// Height must be valid
			delta_y = clamp(delta_y,
					min_height - drag_start_rect.height,
					max_height - drag_start_rect.height);
			// Bottom must be valid
			delta_y = Math.min(delta_y, max_height - drag_start_bottom);		
			// Width must be valid
			delta_col = clamp(delta_col,
					drag_start_rect.num_cols - max_num_cols,
					drag_start_rect.num_cols - min_num_cols);
			// Left must be valid
			delta_col = Math.max(delta_col, -drag_start_rect.col);
			render_rect = {
			    'top': drag_start_rect.top,
			    'col': drag_start_rect.col + delta_col,
			    'num_cols': drag_start_rect.num_cols - delta_col,
			    'height': drag_start_rect.height + delta_y
			};
		    }
		    var cell_width = model.getCellWidth(true)*zoom.x;
		    // Compute render left and width
		    render_rect.left = render_rect.col * cell_width;
		    render_rect.width = render_rect.num_cols * cell_width;
		    drawOverlayRect(self, null, null, render_rect);
		}
	    } else {
		if (mouse.outside) {
		    updateCSSCursor();
		    updateRectResizeHoverLocation();
		} else {
		    updateCSSCursor(mouse_x, mouse_y);
		    updateRectResizeHoverLocation(mouse_x, mouse_y);
		}
		drawOverlayRect(self, model, cell_view);
	    }
	});
	var endDrag = function() {
	    if (dragging) {
		if (["resize_t", "resize_b", "resize_l", "resize_r",
		    "resize_tl", "resize_tr", "resize_bl", "resize_br"].indexOf(drag_type) > -1) {
		    viewport_callback({
			'col': self.current_rect.col,
			'scroll_y_proportion': (self.current_rect.top / parseInt(self.$canvas[0].height, 10)),
			'num_cols': self.current_rect.num_cols,
			'zoom_y': (drag_start_rect.height / self.current_rect.height) * drag_start_vert_zoom
		    });
		}
		dragging = false;
		drag_type = false;
	    }
	};
	$(document).on("mouseup", function (evt) {
	    var offset = self.$div.offset();
	    var overlay_mouse_x = evt.pageX - offset.left;
	    var overlay_mouse_y = evt.pageY - offset.top;
	    endDrag();
	    var mouse = getCanvasMouse(self, overlay_mouse_x, overlay_mouse_y);
	    if (!mouse.outside) {
		var mouse_x = mouse.mouse_x;
		var mouse_y = mouse.mouse_y;
		updateCSSCursor(mouse_x, mouse_y);
		updateRectResizeHoverLocation(mouse_x, mouse_y);
	    } else {
		updateCSSCursor();
		updateRectResizeHoverLocation();
	    }
	    drawOverlayRect(self, model, cell_view);
	});
	
	(function setUpWindowDrag() {
	    var start_mouse_x;
	    var start_mouse_y;
	    var start_left;
	    var start_top;
	    var handleDrag = function(evt) {
		evt.preventDefault();
		var delta_mouse_x = evt.pageX - start_mouse_x;
		var delta_mouse_y = evt.pageY - start_mouse_y;
		self.setWindowPosition(start_left + delta_mouse_x, start_top + delta_mouse_y);
	    };
	    self.$window_bar.hover(function() {
		$(this).css({'cursor':'move'});
	    }, function() {
		$(this).css({'cursor':'auto'});
	    });
	    
	    self.$window_bar.on("mousedown", function (evt) {
		start_mouse_x = evt.pageX;
		start_mouse_y = evt.pageY;
		start_left = parseInt(self.$div.css('left'), 10);
		start_top = parseInt(self.$div.css('top'), 10);
		$(document).on("mousemove", handleDrag);
	    });
	    $(document).on("mouseup click", function () {
		$(document).off("mousemove", handleDrag);
	    });
	})();
	
    }

    var getTrackBuffers = function (view, cell_view, track_id) {
	var pos_buffer = view.ctx.createBuffer();
	var pos_array = cell_view.vertex_data[track_id].pos_array;

	view.ctx.bindBuffer(view.ctx.ARRAY_BUFFER, pos_buffer);
	view.ctx.bufferData(view.ctx.ARRAY_BUFFER, new Float32Array(pos_array), view.ctx.STATIC_DRAW);
	pos_buffer.itemSize = 1;
	pos_buffer.numItems = pos_array.length / pos_buffer.itemSize;
	
	var col_buffer = view.ctx.createBuffer();
	var col_array = cell_view.vertex_data[track_id].col_array;

	view.ctx.bindBuffer(view.ctx.ARRAY_BUFFER, col_buffer);
	view.ctx.bufferData(view.ctx.ARRAY_BUFFER, new Float32Array(col_array), view.ctx.STATIC_DRAW);
	col_buffer.itemSize = 1;
	col_buffer.numItems = col_array.length / col_buffer.itemSize;

	var tex = view.ctx.createTexture();
	view.ctx.bindTexture(view.ctx.TEXTURE_2D, tex);

	var color_bank = cell_view.vertex_data[track_id].col_bank;
	var width = Math.pow(2, Math.ceil(Math.log2(color_bank.length / 4)));
	while (color_bank.length < 4 * width) {
	    color_bank.push(0);
	}
	var height = 1;
	view.ctx.texImage2D(view.ctx.TEXTURE_2D, 0, view.ctx.RGBA, width, height, 0, view.ctx.RGBA, view.ctx.UNSIGNED_BYTE, new Uint8Array(color_bank));
	view.ctx.texParameteri(view.ctx.TEXTURE_2D, view.ctx.TEXTURE_MIN_FILTER, view.ctx.NEAREST);
	view.ctx.texParameteri(view.ctx.TEXTURE_2D, view.ctx.TEXTURE_MAG_FILTER, view.ctx.NEAREST);
	
	var color_texture = {'texture': tex, 'size': width};

	var vertex_column_buffer = view.ctx.createBuffer();
	var vertex_column_array = cell_view.vertex_column_array[track_id];
	view.ctx.bindBuffer(view.ctx.ARRAY_BUFFER, vertex_column_buffer);
	view.ctx.bufferData(view.ctx.ARRAY_BUFFER, new Float32Array(vertex_column_array), view.ctx.STATIC_DRAW);
	vertex_column_buffer.itemSize = 1;
	vertex_column_buffer.numItems = vertex_column_array.length / vertex_column_buffer.itemSize;

	return {'position': pos_buffer,
		'color': col_buffer,
		'color_tex': color_texture,
		'column': vertex_column_buffer};
    };

    var drawOncoprint = function (view, model, cell_view) {
	if (view.rendering_suppressed) {
	    return;
	}

	var zoom = getZoom(view, model);

	view.ctx.clearColor(1.0, 1.0, 1.0, 1.0);
	view.ctx.clear(view.ctx.COLOR_BUFFER_BIT | view.ctx.DEPTH_BUFFER_BIT);

	var tracks = model.getTracks();
	for (var i = 0; i < tracks.length; i++) {
	    var track_id = tracks[i];
	    var cell_top = model.getCellTops(track_id, true);
	    var buffers = getTrackBuffers(view, cell_view, track_id);
	    if (buffers.position.numItems === 0) {
		continue;
	    }
	    
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
	    view.ctx.uniform1f(view.shader_program.columnWidthUniform, model.getCellWidth(true));
	    view.ctx.uniform1f(view.shader_program.zoomXUniform, zoom.x);
	    view.ctx.uniform1f(view.shader_program.zoomYUniform, zoom.y);
	    view.ctx.uniform1f(view.shader_program.offsetYUniform, cell_top);
	    view.ctx.uniform1f(view.shader_program.positionBitPackBaseUniform, cell_view.position_bit_pack_base);

	    view.ctx.drawArrays(view.ctx.TRIANGLES, 0, buffers.position.numItems);
	}
    };
    var getZoom = function (view, model) {
	var zoom_x = parseInt(view.$canvas[0].width, 10) / model.getOncoprintWidthNoColumnPadding(true);
	var zoom_y = parseInt(view.$canvas[0].height, 10) / model.getOncoprintHeight(true);
	zoom_x = Math.max(0, Math.min(1, zoom_x));
	zoom_y = Math.max(0, Math.min(1, zoom_y));
	return {
	    x: zoom_x,
	    y: zoom_y
	};
    };
    
    var drawOverlayRect = function (view, model, cell_view, opt_rect) {
	if (view.rendering_suppressed) {
	    return;
	}

	var left, width, top, height, col, num_cols;
	if (opt_rect) {
	    left = opt_rect.left;
	    width = opt_rect.width;
	    top = opt_rect.top;
	    height = opt_rect.height;
	    col = opt_rect.col;
	    num_cols = opt_rect.num_cols;
	} else {
	    var cell_width = model.getCellWidth(true);
	    var cell_padding = model.getCellPadding(true);
	    var viewport = cell_view.getViewportOncoprintSpace(model);

	    var zoom = getZoom(view, model);
	    col = Math.floor(viewport.left / (cell_width + cell_padding));
	    num_cols = Math.min(model.getIdOrder().length - col,
				Math.floor(viewport.right / (cell_width + cell_padding)) - Math.floor(viewport.left / (cell_width + cell_padding)));
	    left = col * cell_width * zoom.x;
	    width = num_cols * cell_width * zoom.x;
	    top = viewport.top * zoom.y;
	    height = (viewport.bottom - viewport.top) * zoom.y;
	}

	var ctx = view.overlay_ctx;
	var canv = view.$overlay_canvas[0];
	var canv_width = parseInt(canv.width, 10);
	var canv_height = parseInt(canv.height, 10);
	
	// Clear
	ctx.fillStyle = "rgba(0,0,0,0)";
	ctx.clearRect(0, 0, canv_width, canv_height);
	// Draw rectangle
	ctx.fillStyle = "rgba(255,255,255,0.4)";
	ctx.fillRect(left, top, width, height);
	// Draw border line by line
	var unhover_color = "rgba(0,0,0,0.75)";
	var hover_color = "rgba(255,0,0,1)";
	var unhover_width = 1;
	var hover_width = 2;
	var top_is_hovered = view.resize_hover === "t" || view.resize_hover === "tr" || view.resize_hover === "tl";
	var right_is_hovered = view.resize_hover === "r" || view.resize_hover === "tr" || view.resize_hover === "br";
	var bottom_is_hovered = view.resize_hover === "b" || view.resize_hover === "br" || view.resize_hover === "bl";
	var left_is_hovered = view.resize_hover === "l" || view.resize_hover === "tl" || view.resize_hover === "bl";
	// Draw top border
	ctx.beginPath();
	ctx.moveTo(left, top);
	ctx.strokeStyle = top_is_hovered ? hover_color : unhover_color;
	ctx.lineWidth = top_is_hovered ? hover_width : unhover_width;
	ctx.lineTo(left+width, top);
	ctx.stroke();
	// Draw right border
	ctx.beginPath();
	ctx.moveTo(left+width, top);
	ctx.strokeStyle = right_is_hovered ? hover_color : unhover_color;
	ctx.lineWidth = right_is_hovered ? hover_width : unhover_width;
	ctx.lineTo(left+width, top+height);
	ctx.stroke();
	// Draw bottom border
	ctx.beginPath();
	ctx.moveTo(left+width, top+height);
	ctx.strokeStyle = bottom_is_hovered ? hover_color : unhover_color;
	ctx.lineWidth = bottom_is_hovered ? hover_width : unhover_width;
	ctx.lineTo(left, top+height);
	ctx.stroke();
	// Draw left border
	ctx.beginPath();
	ctx.moveTo(left, top+height);
	ctx.strokeStyle = left_is_hovered ? hover_color : unhover_color;
	ctx.lineWidth = left_is_hovered ? hover_width : unhover_width;
	ctx.lineTo(left, top);
	ctx.stroke();
	
	view.current_rect = {
	    'top':top,
	    'left':left,
	    'width':width,
	    'height':height,
	    'col': col,
	    'num_cols': num_cols
	};
    };
    var drawOncoprintAndOverlayRect = function (view, model, cell_view) {
	if (view.rendering_suppressed) {
	    return;
	}
	drawOncoprint(view, model, cell_view);
	drawOverlayRect(view, model, cell_view);
    };

    OncoprintMinimapView.prototype.moveTrack = function (model, cell_view) {
	drawOncoprintAndOverlayRect(this, model, cell_view);
    }
    OncoprintMinimapView.prototype.addTracks = function (model, cell_view) {
	drawOncoprintAndOverlayRect(this, model, cell_view);
    }
    OncoprintMinimapView.prototype.removeTrack = function (model, cell_view) {
	drawOncoprintAndOverlayRect(this, model, cell_view);
    }
    OncoprintMinimapView.prototype.setHorzZoom = function (model, cell_view) {
	drawOverlayRect(this, model, cell_view);
	this.horizontal_zoom.setSliderValue(model.getHorzZoom());
    }
    OncoprintMinimapView.prototype.setVertZoom = function (model, cell_view) {
	drawOverlayRect(this, model, cell_view);
	this.vertical_zoom.setSliderValue(model.getVertZoom());
    }
    OncoprintMinimapView.prototype.setZoom = function(model, cell_view) {
	drawOverlayRect(this, model, cell_view);
	this.horizontal_zoom.setSliderValue(model.getHorzZoom());
	this.vertical_zoom.setSliderValue(model.getVertZoom());
    }
    OncoprintMinimapView.prototype.setScroll = function (model, cell_view) {
	drawOverlayRect(this, model, cell_view);
    }
    OncoprintMinimapView.prototype.setHorzScroll = function (model, cell_view) {
	drawOverlayRect(this, model, cell_view);
    }
    OncoprintMinimapView.prototype.setVertScroll = function (model, cell_view) {
	drawOverlayRect(this, model, cell_view);
    }
    OncoprintMinimapView.prototype.setViewport = function (model, cell_view) {
	drawOverlayRect(this, model, cell_view);
	this.horizontal_zoom.setSliderValue(model.getHorzZoom());
	this.vertical_zoom.setSliderValue(model.getVertZoom());
    }
    OncoprintMinimapView.prototype.sort = function (model, cell_view) {
	drawOncoprintAndOverlayRect(this, model, cell_view);
    }
    OncoprintMinimapView.prototype.setTrackData = function (model, cell_view) {
	drawOncoprintAndOverlayRect(this, model, cell_view);
    }
    OncoprintMinimapView.prototype.shareRuleSet = function (model, cell_view) {
	drawOncoprintAndOverlayRect(this, model, cell_view);
    }
    OncoprintMinimapView.prototype.setRuleSet = function (model, cell_view) {
	drawOncoprintAndOverlayRect(this, model, cell_view);
    }
    OncoprintMinimapView.prototype.setIdOrder = function (model, cell_view) {
	drawOncoprintAndOverlayRect(this, model, cell_view);
    }
    OncoprintMinimapView.prototype.suppressRendering = function () {
	this.rendering_suppressed = true;
    }
    OncoprintMinimapView.prototype.releaseRendering = function (model, cell_view) {
	this.rendering_suppressed = false;
	drawOncoprintAndOverlayRect(this, model, cell_view);
    }
    OncoprintMinimapView.prototype.hideIds = function (model, cell_view) {
	drawOncoprintAndOverlayRect(this, model, cell_view);
    }

    OncoprintMinimapView.prototype.setWindowPosition = function(x, y) {
	this.$div.css({'top': y, 'left': x});
    }
    
    OncoprintMinimapView.prototype.setWidth = function (w, model, cell_view) {
	this.$canvas[0].width = w;
	this.$overlay_canvas[0].width = w;
	getWebGLContextAndSetUpMatrices(this);
	setUpShaders(this);
	this.overlay_ctx = this.$overlay_canvas[0].getContext("2d");

	drawOncoprintAndOverlayRect(this, model, cell_view);
    }
    return OncoprintMinimapView;
})();

module.exports = OncoprintMinimapView;