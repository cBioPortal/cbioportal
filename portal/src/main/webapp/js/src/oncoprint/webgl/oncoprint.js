var OncoprintModel = require('./oncoprintmodel.js');
var OncoprintSVGCellView = require('./oncoprintsvgcellview.js');
var OncoprintWebGLCellView = require('./oncoprintwebglcellview.js');
var OncoprintLabelView = require('./oncoprintlabelview.js');
var OncoprintRuleSet = require('./oncoprintruleset.js');

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
	
	var $label_canvas = $('<canvas width="150" height="250"></canvas>').css({'display':'inline-block', 'position':'absolute', 'left':'0px', 'top':'0px'});
	var $cell_div = $('<div>').css({'width':width, 'height':'250', 'overflow-x':'scroll', 'overflow-y':'hidden', 'display':'inline-block', 'position':'absolute', 'left':'150px', 'top':'0px'});
	var $cell_canvas = $('<canvas width="'+width+'" height="250"></canvas>').css({'position':'absolute', 'top':'0px', 'left':'0px'});
	var $loading_overlay = $('<canvas width="'+width+'" height="250"></canvas>').css({'position':'absolute', 'top':'0px', 'left':'0px'});
	var $dummy_scroll_div = $('<div>').css({'width':'20000', 'position':'absolute', 'top':'0', 'left':'0px', 'height':'1px'});
	
	$label_canvas.appendTo($oncoprint_ctr);
	$cell_div.appendTo($oncoprint_ctr);
	$loading_overlay.appendTo($oncoprint_ctr);
	
	$cell_canvas.appendTo($cell_div);
	$dummy_scroll_div.appendTo($cell_div);
	
	this.$container = $oncoprint_ctr;
	window.$container = this.$container;
	this.$loading_overlay = $loading_overlay;
	
	this.model = new OncoprintModel();

	// Precisely one of the following should be uncommented
	// this.cell_view = new OncoprintSVGCellView($svg_dev);
	this.cell_view = new OncoprintWebGLCellView($cell_div, $cell_canvas, $dummy_scroll_div);

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
	
	showLoadingOverlay(this);
    }

    var showLoadingOverlay = function(oncoprint, percent_completed, msg) {
	oncoprint.$loading_overlay.show();
	
	var width = oncoprint.$container.width();
	var height = oncoprint.$container.height();
	oncoprint.$loading_overlay.width(width);
	oncoprint.$loading_overlay.height(height);
	
	var ctx = oncoprint.$loading_overlay[0].getContext('2d');
	ctx.fillStyle = 'rgba(255,255,255,0.8)';
	ctx.fillRect(0,0,width,height);
    }
    var hideLoadingOverlay = function(oncoprint) {
    }
    Oncoprint.prototype.moveTrack = function(target_track, new_previous_track) {
	this.model.moveTrack(target_track, new_previous_track);
	this.cell_view.moveTrack(this.model);
	this.label_view.moveTrack(this.model);
	
	if (this.keep_sorted) {
	    this.sort();
	}
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
	this.model.addTracks(params_list);
	// Update views
	this.cell_view.addTracks(this.model, track_ids);
	this.label_view.addTracks(this.model, track_ids);
	
	if (this.keep_sorted) {
	    this.sort();
	}

	return track_ids;
    }

    Oncoprint.prototype.removeTrack = function (track_id) {
	// Update model
	this.model.removeTrack(track_id);
	// Update views
	this.cell_view.removeTrack(this.model, track_id);
	this.label_view.removeTrack(this.model, track_id);
	
	if (this.keep_sorted) {
	    this.sort();
	}
    }

    Oncoprint.prototype.getHorzZoom = function () {
	return this.model.getHorzZoom();
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
	
	return this.model.getVertZoom();
    }

    Oncoprint.prototype.setTrackData = function (track_id, data) {
	this.model.setTrackData(track_id, data);
	this.cell_view.setTrackData(this.model, track_id);
	
	if (this.keep_sorted) {
	    this.sort();
	}
    }

    Oncoprint.prototype.setRuleSet = function (track_id, rule_set_params) {
	this.model.setRuleSet(track_id, OncoprintRuleSet(rule_set_params));
	this.cell_view.setTrackData(this.model, track_id);
    }
    
    Oncoprint.prototype.setTrackGroupSortPriority = function(priority) {
	this.model.setTrackGroupSortPriority(priority);
	this.cell_view.setTrackGroupSortPriority(this.model);
	
	if (this.keep_sorted) {
	    this.sort();
	}
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