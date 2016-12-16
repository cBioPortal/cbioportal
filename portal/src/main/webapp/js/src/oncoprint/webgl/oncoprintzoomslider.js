var OncoprintZoomSlider = (function() {
    var VERTICAL = "v";
    var HORIZONTAL = "h";
    
    var clamp = function(x) {
	return Math.max(Math.min(x, 1), 0);
    };
    
    var initialize = function(component, params) {
	var $ctr = component.$div;
	var icon_size = Math.round(params.btn_size * 0.7);
	var icon_padding = Math.round((params.btn_size - icon_size)/2);
	var $slider_bar = $('<div>').css({'position':'absolute',
					'background-color':'#ffffff',
					'outline': 'solid 1px black'}).appendTo($ctr);
	var $slider = $('<div>').css({'position':'absolute',
				    'background-color':'#ffffff',
				    'border': 'solid 1px black',
				    'border-radius': '3px',
				    'cursor': 'pointer'}).appendTo($ctr);
				
	var $plus_btn = $('<div>').css({'position':'absolute',
					    'min-height': params.btn_size,
					    'min-width': params.btn_size,
					    'background-color':'#ffffff',
					    'border': 'solid 1px black',
					    'border-radius': '3px',
					    'cursor': 'pointer'})
						.appendTo($ctr);
	$('<span>').addClass("icon fa fa-plus").css({'position':'absolute', 
							'top':icon_padding,
							'left':icon_padding,
							'min-width':icon_size,
							'min-height':icon_size})
						.appendTo($plus_btn);
	var $minus_btn = $('<div>').css({'position':'absolute',
					    'min-height': params.btn_size,
					    'min-width': params.btn_size,
					    'background-color':'#ffffff',
					    'border': 'solid 1px black',
					    'border-radius': '3px',
					    'cursor': 'pointer'})
						.appendTo($ctr);
	$('<span>').addClass("icon fa fa-minus").css({'position':'absolute', 
							'top':icon_padding,
							'left':icon_padding,
							'min-width':icon_size,
							'min-height':icon_size})
						.appendTo($minus_btn);
	if (params.vertical) {
	    $slider_bar.css({'min-height': params.height - 2 * params.btn_size,
		'min-width': Math.round(params.btn_size / 5)});
	    $slider.css({'min-height': Math.round(params.btn_size / 2),
		'min-width': params.btn_size});

	    $plus_btn.css({'top': 0, 'left': 0});
	    $minus_btn.css({'top': params.height - params.btn_size, 'left': 0});
	    $slider_bar.css({'top': params.btn_size, 'left': 0.4 * params.btn_size});
	    $slider.css({'left': 0});
	    component.orientation = VERTICAL;
	} else {
	    $slider_bar.css({'min-height': Math.round(params.btn_size / 5),
		'min-width': params.width - 2 * params.btn_size});
	    $slider.css({'min-height': params.btn_size,
		'min-width': Math.round(params.btn_size / 2)});
	    
	    $plus_btn.css({'top': 0, 'left': params.width - params.btn_size});
	    $minus_btn.css({'top': 0, 'left': 0});
	    $slider_bar.css({'top': 0.4*params.btn_size, 'left': params.btn_size});
	    $slider.css({'top': 0});
	    component.orientation = HORIZONTAL;
	}
	
	$plus_btn.click(function() {
	    component.value /= 0.7;
	    params.onChange(component.value);
	});				
	$minus_btn.click(function() {
	    component.value *= 0.7;
	    params.onChange(component.value);
	});
	
	[$slider, $plus_btn, $minus_btn].map(function($btn) { $btn.hover(function() {
	    $(this).css({'background-color':'#cccccc'});
	}, function() {
	    $(this).css({'background-color': '#ffffff'});
	}); });
    
    
    
	component.$slider = $slider;
	component.$plus_btn = $plus_btn;
	component.$minus_btn = $minus_btn;
	
	(function setUpSliderDrag() {
	    var start_mouse;
	    var start_val;
	    var dragging;
	    var handleSliderDrag = function (evt) {
		evt.stopPropagation();
		evt.preventDefault();
		var delta_mouse;
		if (component.orientation === VERTICAL) {
		    delta_mouse = start_mouse - evt.pageY; // vertical zoom, positive is up, but CSS positive is down, so we need to invert
		} else {
		    delta_mouse = evt.pageX - start_mouse;
		}
		var delta_val = delta_mouse / component.slider_bar_size;
		component.setSliderValue(start_val + delta_val);
	    };
	    var stopSliderDrag = function () {
		if (dragging && start_val !== component.value) {
		    component.onChange(component.value);
		}
		dragging = false;
	    };
	    component.$slider.on("mousedown", function (evt) {
		if (component.orientation === VERTICAL) {
		    start_mouse = evt.pageY;
		} else {
		    start_mouse = evt.pageX;
		}
		start_val = component.value;
		dragging = true;
		$(document).on("mousemove", handleSliderDrag);
	    });
	    $(document).on("mouseup click", function () {
		$(document).off("mousemove", handleSliderDrag);
		stopSliderDrag();
	    });
	})()
    };
    
    var setSliderPos = function(component, proportion) {
	var $slider = component.$slider;
	var bounds = getSliderBounds(component);
	if (component.orientation === VERTICAL) {
	    $slider.css('top', bounds.bottom*(1-proportion) + bounds.top*proportion);
	} else if (component.orientation === HORIZONTAL) {
	    $slider.css('left', bounds.left*(1-proportion) + bounds.right*proportion);
	}
    };
    
    var getSliderBounds = function(component) {
	if (component.orientation === VERTICAL) {
	    return {bottom: parseInt(component.$minus_btn.css('top'), 10) - parseInt(component.$slider.css('min-height'), 10),
		    top: parseInt(component.$plus_btn.css('top'), 10) + parseInt(component.$plus_btn.css('min-height'), 10)};
	} else { 
	    return {left: parseInt(component.$minus_btn.css('left'), 10) + parseInt(component.$minus_btn.css('min-width'), 10),
		    right: parseInt(component.$plus_btn.css('left'), 10) - parseInt(component.$slider.css('min-width'), 10)};
	}
    };
    
    var updateSliderPos = function(component) {
	setSliderPos(component, component.value);
    };
    
    function OncoprintZoomSlider($container, params) {
	this.$div = $('<div>').css({'position':'absolute',
				    'top': params.top || 0,
				    'left': params.left || 0}).appendTo($container);
	params = params || {};
	params.btn_size = params.btn_size || 13;
	this.onChange = params.onChange || function() {};
	initialize(this, params);
	this.value = params.init_val || 0.5;
	this.slider_bar_size = (this.orientation === VERTICAL ? params.height : params.width) - 2*params.btn_size;
	updateSliderPos(this);
    }
    
    OncoprintZoomSlider.prototype.setSliderValue = function(proportion, trigger_callback) {
	this.value = clamp(proportion);
	updateSliderPos(this);
    }
    
    
    return OncoprintZoomSlider;
    })();
    
module.exports = OncoprintZoomSlider;