var svgfactory = require('./svgfactory.js');

var OncoprintLabelView = (function () {
    function OncoprintLabelView($canvas, model, tooltip) {
	var view = this;
	this.$canvas = $canvas;
	this.base_font_size = 14;
	this.model = model;
	this.tooltip = tooltip;
	this.tooltip.center = false;
	// stuff from model
	this.label_tops = {};
	this.labels = {};
	this.tracks = [];
	this.minimum_track_height = Number.POSITIVE_INFINITY;
	this.maximum_label_width = Number.NEGATIVE_INFINITY;
	
	this.maximum_label_length = 18;
	this.rendering_suppressed = false;
	
	setUpContext(this);
	
	(function setUpDragging(view) {
	    view.drag_callback = function(target_track, new_previous_track) {};
	    view.dragged_label_track_id = null;
	    view.drag_mouse_y = null;
	    
	    view.$canvas.on("mousedown", function(evt) {
		view.tooltip.hide();
		var track_id = isMouseOnLabel(view, evt.offsetY);
		if (track_id !== null && model.getContainingTrackGroup(track_id).length > 1) {
		    startDragging(view, track_id, evt.offsetY);
		}
	    });
	    
	    view.$canvas.on("mousemove", function(evt) {
		if (view.dragged_label_track_id !== null) {
		    var track_group = model.getContainingTrackGroup(view.dragged_label_track_id);
		    view.drag_mouse_y = Math.min(evt.pageY - view.$canvas.offset().top, view.track_tops[track_group[track_group.length-1]] + model.getTrackHeight(track_group[track_group.length-1]));
		    view.drag_mouse_y = Math.max(view.drag_mouse_y, view.track_tops[track_group[0]]-5);
		    renderAllLabels(view);
		} else {
		    var hovered_track = isMouseOnLabel(view, evt.pageY - view.$canvas.offset().top);
		    if (hovered_track !== null && model.getContainingTrackGroup(hovered_track).length > 1) {
			view.$canvas.css('cursor', 'move');
			var offset = view.$canvas.offset();
			var tooltip_html = "<b>hold to drag</b>";
			if (isNecessaryToShortenLabel(view, view.labels[hovered_track])) {
			    tooltip_html = view.labels[hovered_track] + '<br>' + tooltip_html;
			}
			view.tooltip.fadeIn(200, renderedLabelWidth(view, view.labels[hovered_track]) + offset.left, view.label_tops[hovered_track] + offset.top, tooltip_html);
		    } else {
			view.$canvas.css('cursor', 'auto');
			view.tooltip.hide();
		    }
		}
	    });
	    
	    view.$canvas.on("mouseup mouseleave", function(evt) {
		if (view.dragged_label_track_id !== null) {
		    var track_group = model.getContainingTrackGroup(view.dragged_label_track_id);
		    var previous_track_id = getLabelAbove(view, track_group, evt.offsetY, view.dragged_label_track_id);
		    stopDragging(view, previous_track_id);
		}
		view.tooltip.hide();
	    });
	})(this);
	
    }
    var renderedLabelWidth = function(view, label) {
	return view.ctx.measureText(shortenLabelIfNecessary(view, label)).width;
    };
    var updateFromModel = function(view, model) {
	var track_tops = model.getTrackTops();
	var label_tops = model.getLabelTops();
	view.track_tops = track_tops;
	view.label_tops = label_tops;
	view.tracks = model.getTracks();
	
	view.ctx.font = 'bold '+view.getFontSize()+'px Arial';
	view.minimum_track_height = Number.POSITIVE_INFINITY;
	view.maximum_label_width = 0;
	for (var i=0; i<view.tracks.length; i++) {
	    view.minimum_track_height = Math.min(view.minimum_track_height, model.getTrackHeight(view.tracks[i]));
	    var shortened_label = shortenLabelIfNecessary(view, view.labels[view.tracks[i]]);
	    view.maximum_label_width = Math.max(view.maximum_label_width, view.ctx.measureText(shortened_label).width);
	}
    }
    var setUpContext = function(view) {
	view.ctx = view.$canvas[0].getContext('2d');
	view.ctx.textAlign="start";
	view.ctx.textBaseline="top";
    }
    var resizeAndClear = function(view, model) {
	view.$canvas[0].height = model.getCellViewHeight();
	view.$canvas[0].width = view.getWidth();
	setUpContext(view);
    }
    var isNecessaryToShortenLabel = function(view, label) {
	return label.length > view.maximum_label_length;
    };
    var shortenLabelIfNecessary = function(view, label) {
	if (isNecessaryToShortenLabel(view, label)) {
	    return label.substring(0, view.maximum_label_length-3) + '...';
	} else {
	    return label;
	}
    };
    var renderAllLabels = function(view) {
	if (view.rendering_suppressed) {
	    return;
	}
	var font_size = view.getFontSize();
	view.ctx.font = 'bold '+font_size +'px Arial';
	view.ctx.clearRect(0,0,view.$canvas[0].width,view.$canvas[0].height);
	view.ctx.fillStyle = 'black';
	var tracks = view.tracks;
	for (var i=0; i<tracks.length; i++) {
	    view.ctx.fillText(shortenLabelIfNecessary(view, view.labels[tracks[i]]), 0, view.label_tops[tracks[i]]);
	}
	if (view.dragged_label_track_id !== null) {
	    view.ctx.fillStyle = 'rgba(255,0,0,0.95)';
	    view.ctx.fillText(shortenLabelIfNecessary(view, view.labels[view.dragged_label_track_id]), 0, view.drag_mouse_y-font_size/2);
	    view.ctx.fillStyle = 'rgba(0,0,0,0.15)';
	    var group = view.model.getContainingTrackGroup(view.dragged_label_track_id);
	    var label_above_mouse = getLabelAbove(view, group, view.drag_mouse_y, null);
	    var label_below_mouse = getLabelBelow(view, group, view.drag_mouse_y, null);
	    var rect_y, rect_height;
	    if (label_above_mouse === view.dragged_label_track_id || label_below_mouse === view.dragged_label_track_id) {
		return;
	    }
	    if (label_above_mouse !== null && label_below_mouse !== null) {
		rect_y = view.label_tops[label_above_mouse] + view.ctx.measureText("m").width;
		rect_height = view.label_tops[label_below_mouse] - rect_y;
	    } else if (label_above_mouse === null) {
		rect_y = view.label_tops[group[0]] - view.ctx.measureText("m").width;
		rect_height = view.ctx.measureText("m").width;
	    } else if (label_below_mouse === null) {
		rect_y = view.label_tops[group[group.length-1]] + view.ctx.measureText("m").width;;
		rect_height = view.ctx.measureText("m").width;
	    }
	    view.ctx.fillRect(0, rect_y, view.getWidth(), rect_height);
	}
    }
    
    var isMouseOnLabel = function(view, mouse_y) {
	var candidate_track = getLabelAbove(view, view.tracks, mouse_y, null);
	if (candidate_track === null) {
	    return null;
	}
	if (mouse_y <= view.label_tops[candidate_track] + view.getFontSize()) {
	    return candidate_track;
	} else {
	    return null;
	}
    }
    var getLabelAbove = function(view, track_ids, y, track_to_exclude) {
	if (y < view.label_tops[track_ids[0]]) {
	    return null;
	} else {
	    var candidate_track = null;
	    for (var i=0; i<track_ids.length; i++) {
		if (track_to_exclude !== null && track_to_exclude === track_ids[i]) {
		    continue;
		}
		if (view.label_tops[track_ids[i]] > y) {
		    break;
		} else {
		    candidate_track = track_ids[i];
		}
	    }
	    return candidate_track;
	}
    }
    var getLabelBelow = function(view, track_ids, y, track_to_exclude) {
	if (y > view.label_tops[track_ids[track_ids.length-1]]) {
	    return null;
	} else {
	    var candidate_track = null;
	    for (var i=track_ids.length-1; i>=0; i--) {
		if (track_to_exclude !== null && track_to_exclude === track_ids[i]) {
		    continue;
		}
		if (view.label_tops[track_ids[i]] < y) {
		    break;
		} else {
		    candidate_track = track_ids[i];
		}
	    }
	    return candidate_track;
	}
    }
    
    var startDragging = function(view, track_id, mouse_y) {
	view.dragged_label_track_id = track_id;
	view.drag_mouse_y = mouse_y;
	renderAllLabels(view);
    }
    var stopDragging = function(view, new_previous_track_id) {
	view.drag_callback(view.dragged_label_track_id, new_previous_track_id);
	view.dragged_label_track_id = null;
	renderAllLabels(view);
    }
    OncoprintLabelView.prototype.getWidth = function() {
	//return this.maximum_label_width + 20;
	return Math.max(this.maximum_label_width + 10, 70);
    }
    OncoprintLabelView.prototype.getFontSize = function() {
	return Math.max(Math.min(this.base_font_size, this.minimum_track_height), 7);
	
    }
    OncoprintLabelView.prototype.setDragCallback = function(callback) {
	this.drag_callback = callback;
    }
    OncoprintLabelView.prototype.removeTrack = function (model, track_id) {
	updateFromModel(this, model);
	resizeAndClear(this, model);
	renderAllLabels(this, model);
    }
    OncoprintLabelView.prototype.moveTrack = function (model) {
	updateFromModel(this, model);
	resizeAndClear(this, model);
	renderAllLabels(this, model);
    }
    OncoprintLabelView.prototype.addTracks = function (model, track_ids) {
	for (var i=0; i<track_ids.length; i++) {
	    this.labels[track_ids[i]] = model.getTrackLabel(track_ids[i]);
	}
	updateFromModel(this, model);
	resizeAndClear(this, model);
	renderAllLabels(this, model);
    }
    OncoprintLabelView.prototype.setVertZoom = function(model) {
	updateFromModel(this, model);
	resizeAndClear(this, model);
	renderAllLabels(this, model);
    }
    
    OncoprintLabelView.prototype.suppressRendering = function() {
	this.rendering_suppressed = true;
    }
    
    OncoprintLabelView.prototype.releaseRendering = function() {
	this.rendering_suppressed = false;
	renderAllLabels(this);
    }
    
    OncoprintLabelView.prototype.toSVGGroup = function(model, full_labels, offset_x, offset_y) {
	var root = svgfactory.group((offset_x || 0), (offset_y || 0));
	var label_tops = model.getLabelTops();
	var tracks = model.getTracks();
	for (var i=0; i<tracks.length; i++) {
	    var track_id = tracks[i];
	    var y = label_tops[track_id];
	    var label = model.getTrackLabel(track_id);
	    var text_elt = svgfactory.text((full_labels ? label : shortenLabelIfNecessary(this, label)), 0, y, this.getFontSize(), 'Arial', 'bold'); 
	    root.appendChild(text_elt);
	}
	return root;
    }

    return OncoprintLabelView;
})();

module.exports = OncoprintLabelView;