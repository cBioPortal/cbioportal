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
	var $label_canvas = $('<canvas width="150" height="250"></canvas>').appendTo(ctr_selector).css({'display':'inline-block'});
	var $cell_canvas = $('<canvas width="'+width+'" height="250"></canvas>').css({'position':'absolute', 'top':'0px', 'left':'0px'});
	var $cell_div = $('<div>').css({'width':width+'px', 'height':'250px', 'overflow-x':'scroll', 'overflow-y':'hidden', 'display':'inline-block', 'position':'relative'}).appendTo(ctr_selector);
	$cell_canvas.appendTo($cell_div);
	$('<div>').css({'width':'20000px', 'position':'absolute', 'top':'250px', 'left':'0px', 'height':'1px'}).appendTo($cell_div);
	
	this.model = new OncoprintModel();

	// Precisely one of the following should be uncommented
	// this.cell_view = new OncoprintSVGCellView($svg_dev);
	this.cell_view = new OncoprintWebGLCellView($cell_canvas);

	this.label_view = new OncoprintLabelView($label_canvas);
	
	
	// We need to handle scrolling this way because for some reason huge 
	//  canvas elements have terrible resolution.
	var cell_view = this.cell_view;
	$cell_div.scroll(function() {
	    var scroll_left = $cell_div.scrollLeft();
	    $cell_canvas.css('left', scroll_left);
	    cell_view.scroll(scroll_left);
	});
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

	return track_ids;
    }

    Oncoprint.prototype.removeTrack = function (track_id) {
	// Update model
	this.model.removeTrack(track_id);
	// Update views
	this.cell_view.removeTrack(this.model, track_id);
	this.label_view.removeTrack(this.model, track_id);
    }

    Oncoprint.prototype.getZoom = function () {
	return this.model.getZoom();
    }

    Oncoprint.prototype.setZoom = function (z) {
	// Update model
	this.model.setZoom(z);
	// Update views
	this.cell_view.setZoom(this.model, z);
    }

    Oncoprint.prototype.setTrackData = function (track_id, data) {
	this.model.setTrackData(track_id, data);
	this.cell_view.setTrackData(this.model, track_id);
    }

    Oncoprint.prototype.setRuleSet = function (track_id, rule_set_params) {
	this.model.setRuleSet(track_id, OncoprintRuleSet(rule_set_params));
	this.cell_view.setTrackData(this.model, track_id);
    }
    
    Oncoprint.prototype.setTrackGroupSortPriority = function(priority) {
	this.model.setTrackGroupSortPriority(priority);
	this.cell_view.setTrackGroupSortPriority(this.model);
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
    }
    Oncoprint.prototype.setIdOrder = function(ids) {
	// Update model
	this.model.setIdOrder(ids);
	// Update views
	this.cell_view.setIdOrder(this.model, ids);
    }
    
    Oncoprint.prototype.suppressRendering = function() {
	this.cell_view.suppressRendering();
    }
    
    Oncoprint.prototype.releaseRendering = function() {
	this.cell_view.releaseRendering();
    }
    
    Oncoprint.prototype.hideIds = function(to_hide, show_others) {
	this.model.hideIds(to_hide, show_others);
	this.cell_view.hideIds(this.model);
    }
    
    return Oncoprint;
})();
module.exports = Oncoprint;