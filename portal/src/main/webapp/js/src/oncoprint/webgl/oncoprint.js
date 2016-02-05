var OncoprintModel = require('./oncoprintmodel.js');
var OncoprintSVGCellView = require('./oncoprintsvgcellview.js');
var OncoprintWebGLCellView = require('./oncoprintwebglcellview.js');
var OncoprintLabelView = require('./oncoprintlabelview.js');
var OncoprintRuleSet = require('./oncoprintruleset.js');
var OncoprintTrackOptionsView = require('./oncoprinttrackoptionsview.js');

var Oncoprint = (function () {
    // this is the controller
    var nextTrackId = (function () {
	var ctr = 0;
	return function () {
	    ctr += 1;
	    return ctr;
	}
    })();
    function Oncoprint(ctr_selector, width) {
	var self = this;
	var $oncoprint_ctr = $('<span></span>').css({'position':'relative', 'display':'inline-block'}).appendTo(ctr_selector);
	
	var $label_canvas = $('<canvas width="150" height="250"></canvas>').css({'display':'inline-block', 'position':'absolute', 'left':'0px', 'top':'0px'}).addClass("noselect");
	var $track_options_div = $('<div width="50" height="250"></div>').css({'position':'absolute', 'left':'150px', 'top':'0px'}).addClass("noselect");
	
	var $cell_div = $('<div>').css({'width':width, 'height':'250', 'overflow-x':'scroll', 'overflow-y':'hidden', 'display':'inline-block', 'position':'absolute', 'left':'200px', 'top':'0px'}).addClass("noselect");
	var $cell_canvas = $('<canvas width="'+width+'" height="250"></canvas>').css({'position':'absolute', 'top':'0px', 'left':'0px'}).addClass("noselect");
	var $dummy_scroll_div = $('<div>').css({'width':'20000', 'position':'absolute', 'top':'0', 'left':'0px', 'height':'1px'});
	
	$label_canvas.appendTo($oncoprint_ctr);
	$cell_div.appendTo($oncoprint_ctr);
	$track_options_div.appendTo($oncoprint_ctr);

	
	$cell_canvas.appendTo($cell_div);
	$dummy_scroll_div.appendTo($cell_div);
	
	this.$container = $oncoprint_ctr;
	
	this.model = new OncoprintModel();

	// Precisely one of the following should be uncommented
	// this.cell_view = new OncoprintSVGCellView($svg_dev);
	this.cell_view = new OncoprintWebGLCellView($cell_div, $cell_canvas, $dummy_scroll_div);
	
	this.track_options_view = new OncoprintTrackOptionsView($track_options_div, function(track_id) { self.removeTrack(track_id); });

	this.label_view = new OncoprintLabelView($label_canvas);
	this.label_view.setDragCallback(function(target_track, new_previous_track) {
	    self.moveTrack(target_track, new_previous_track);
	});
	
	this.keep_sorted = true;
	// We need to handle scrolling this way because for some reason huge 
	//  canvas elements have terrible resolution.
	var cell_view = this.cell_view;
	var model = this.model;
	$cell_div.scroll(function() {
	    var scroll_left = $cell_div.scrollLeft();
	    $cell_canvas.css('left', scroll_left);
	    cell_view.scroll(model, scroll_left);
	});
    }

    var resizeContainer = function(oncoprint) {
	oncoprint.$container.css({'min-height':oncoprint.model.getViewHeight()});
    }
    Oncoprint.prototype.moveTrack = function(target_track, new_previous_track) {
	this.model.moveTrack(target_track, new_previous_track);
	this.cell_view.moveTrack(this.model);
	this.label_view.moveTrack(this.model);
	this.track_options_view.moveTrack(this.model);
	
	if (this.keep_sorted) {
	    this.sort();
	}
	
	resizeContainer(this);
    }
    Oncoprint.prototype.addTracks = function (params_list) {
	// Update model
	var track_ids = [];
	params_list = params_list.map(function (o) {
	    o.track_id = nextTrackId();
	    o.rule_set = OncoprintRuleSet(o.rule_set_params);
	    track_ids.push(o.track_id);
	    return o;
	});
	
	this.suppressRendering();
	this.model.addTracks(params_list);
	// Update views
	this.cell_view.addTracks(this.model, track_ids);
	this.label_view.addTracks(this.model, track_ids);
	this.track_options_view.addTracks(this.model);
	
	if (this.keep_sorted) {
	    this.sort();
	}
	this.releaseRendering();
	resizeContainer(this);
	return track_ids;
    }

    Oncoprint.prototype.removeTrack = function (track_id) {
	// Update model
	this.model.removeTrack(track_id);
	// Update views
	this.cell_view.removeTrack(this.model, track_id);
	this.label_view.removeTrack(this.model, track_id);
	this.track_options_view.removeTrack(this.model, track_id);
	
	if (this.keep_sorted) {
	    this.sort();
	}
	resizeContainer(this);
    }

    Oncoprint.prototype.getZoomToFitHorz = function(ids) {
	var width_to_fit_in;
	if (typeof ids === 'undefined') {
	    width_to_fit_in = this.cell_view.getWidth(this.model, true);
	} else {
	    var furthest_right_id = -1;
	    var id_to_index_map = this.model.getIdToIndexMap();
	    for (var i=0; i<ids.length; i++) {
		furthest_right_id = Math.max(id_to_index_map[ids[i]], furthest_right_id);
	    }
	    width_to_fit_in = ((furthest_right_id + 3) * (this.model.getCellWidth(true) + this.model.getCellPadding(true)));
	}
	var zoom = Math.min(1, this.cell_view.visible_area_width / width_to_fit_in);
	return zoom;
    }
    Oncoprint.prototype.getHorzZoom = function () {
	return this.model.getHorzZoom();
    }
    
    Oncoprint.prototype.getMinZoom = function() {
	return this.model.getMinZoom();
    }

    Oncoprint.prototype.setHorzZoom = function (z) {
	// Update model
	this.model.setHorzZoom(z);
	// Update views
	this.cell_view.setHorzZoom(this.model, z);

	return this.model.getHorzZoom();
    }
    
    Oncoprint.prototype.getVertZoom = function () {
	return this.model.getVertZoom();
    }

    Oncoprint.prototype.setVertZoom = function (z) {
	// Update model
	this.model.setVertZoom(z);
	// Update views
	this.cell_view.setVertZoom(this.model, z);
	this.label_view.setVertZoom(this.model, z);
	
	resizeContainer(this);
	return this.model.getVertZoom();
    }

    Oncoprint.prototype.setTrackData = function (track_id, data) {
	this.model.setTrackData(track_id, data);
	this.cell_view.setTrackData(this.model, track_id);
	
	if (this.keep_sorted) {
	    this.sort();
	}
	resizeContainer(this);
    }

    Oncoprint.prototype.setTrackGroupSortPriority = function(priority) {
	this.model.setTrackGroupSortPriority(priority);
	this.cell_view.setTrackGroupSortPriority(this.model);
	
	if (this.keep_sorted) {
	    this.sort();
	}
    }

    Oncoprint.prototype.setTrackSortDirection = function(track_id, dir) {
	if (this.model.isTrackSortDirectionChangeable(track_id)) {
	    this.model.setTrackSortDirection(track_id, dir);
	    
	    if (this.keep_sorted) {
		this.sort();
	    }
	}
	return this.model.getTrackSortDirection(track_id);
    }
    
    Oncoprint.prototype.getTrackSortDirection = function(track_id) {
	return this.model.getTrackSortDirection(track_id);
    }
    
    Oncoprint.prototype.toggleSortBy = function(track_id) {
    }
    
    Oncoprint.prototype.sort = function() {
	this.model.sort();
	this.cell_view.sort(this.model);
    }
    
    Oncoprint.prototype.setRuleSet = function(track_id, rule_set_params) {
	this.model.setRuleSet(track_id, OncoprintRuleSet(rule_set_params));
	this.cell_view.setRuleSet(this.model);
    }
    
    Oncoprint.prototype.setSortConfig = function(params) {
	this.model.setSortConfig(params);
	this.cell_view.setSortConfig(this.model);
	
	if (this.keep_sorted) {
	    this.sort();
	}
    }
    Oncoprint.prototype.setIdOrder = function(ids) {
	// Update model
	this.model.setIdOrder(ids);
	// Update views
	this.cell_view.setIdOrder(this.model, ids);
	
	if (this.keep_sorted) {
	    this.sort();
	}
    }
    
    Oncoprint.prototype.suppressRendering = function() {
	this.cell_view.suppressRendering();
    }
    
    Oncoprint.prototype.releaseRendering = function() {
	this.cell_view.releaseRendering(this.model);
    }
    
    Oncoprint.prototype.hideIds = function(to_hide, show_others) {
	this.model.hideIds(to_hide, show_others);
	this.cell_view.hideIds(this.model);
    }
    
    Oncoprint.prototype.setCellPaddingOn = function(cell_padding_on) {
	this.model.setCellPaddingOn(cell_padding_on);
	this.cell_view.setCellPaddingOn(this.model);
    }
    
    return Oncoprint;
})();
module.exports = Oncoprint;