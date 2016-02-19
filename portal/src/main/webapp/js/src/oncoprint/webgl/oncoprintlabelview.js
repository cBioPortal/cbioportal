var OncoprintLabelView = (function () {
    function OncoprintLabelView($canvas, model) {
	var view = this;
	this.$canvas = $canvas;
	this.base_font_size = 14;
	this.model = model;
	
	// stuff from model
	this.label_tops = {};
	this.labels = {};
	this.tracks = [];
	this.minimum_track_height = Number.POSITIVE_INFINITY;
	this.maximum_label_width = Number.NEGATIVE_INFINITY;
	
	this.rendering_suppressed = false;
	
	setUpContext(this);
	
	(function setUpDragging(view) {
	    view.drag_callback = function(target_track, new_previous_track) {};
	    view.dragged_label_track_id = null;
	    view.drag_mouse_y = null;
	    
	    view.$canvas.on("mousedown", function(evt) {
		var track_id = isMouseOnLabel(view, evt.offsetY);
		if (track_id !== null) {
		    startDragging(view, track_id, evt.offsetY);
		}
	    });
	    
	    view.$canvas.on("mousemove", function(evt) {
		if (view.dragged_label_track_id !== null) {
		    var track_group = model.getContainingTrackGroup(view.dragged_label_track_id);
		    view.drag_mouse_y = Math.min(evt.pageY - view.$canvas.offset().top, view.label_tops[track_group[track_group.length-1]] + model.getTrackHeight(track_group[track_group.length-1]));
		    view.drag_mouse_y = Math.max(view.drag_mouse_y, view.label_tops[track_group[0]]-5);
		    renderAllLabels(view);
		} else {
		    if (isMouseOnLabel(view, evt.pageY - view.$canvas.offset().top) !== null) {
			view.$canvas.css('cursor', 'move');
		    } else {
			view.$canvas.css('cursor', 'auto');
		    }
		}
	    });
	    
	    view.$canvas.on("mouseup mouseleave", function(evt) {
		if (view.dragged_label_track_id !== null) {
		    var track_group = model.getContainingTrackGroup(view.dragged_label_track_id);
		    var previous_track_id = getLabelAbove(view, track_group, evt.offsetY, view.dragged_label_track_id);
		    stopDragging(view, previous_track_id);
		}
	    });
	})(this);
	
    }
    var updateFromModel = function(view, model) {
	var track_tops = model.getTrackTops();
	var label_tops = track_tops;
	/*for (var track_id in label_tops) {
	    if (label_tops.hasOwnProperty(track_id)) {
		label_tops[track_id] += model.getTrackPadding(track_id);
	    }
	}*/
	view.label_tops = label_tops;
	view.tracks = model.getTracks();
	
	view.minimum_track_height = Number.POSITIVE_INFINITY;
	view.maximum_label_width = Number.NEGATIVE_INFINITY;
	for (var i=0; i<view.tracks.length; i++) {
	    view.minimum_track_height = Math.min(view.minimum_track_height, model.getTrackHeight(view.tracks[i]));
	    view.maximum_label_width = Math.max(view.maximum_label_width, view.ctx.measureText(view.labels[view.tracks[i]]).width);
	}
    }
    var setUpContext = function(view) {
	view.ctx = view.$canvas[0].getContext('2d');
	view.ctx.textAlign="start";
	view.ctx.textBaseline="top";
    }
    var resizeAndClear = function(view, model) {
	view.$canvas[0].height = model.getViewHeight();
	view.$canvas[0].width = view.getWidth();
	setUpContext(view);
    }
    var renderAllLabels = function(view) {
	if (view.rendering_suppressed) {
	    return;
	}
	var font_size = view.getFontSize();
	view.ctx.font = 'bold '+font_size +'px serif';
	view.ctx.clearRect(0,0,view.$canvas[0].width,view.$canvas[0].height);
	view.ctx.fillStyle = 'black';
	var tracks = view.tracks;
	for (var i=0; i<tracks.length; i++) {
	    view.ctx.fillText(view .labels[tracks[i]], 0, view.label_tops[tracks[i]]);
	}
	if (view.dragged_label_track_id !== null) {
	    view.ctx.strokeText(view.labels[view.dragged_label_track_id], 0, view.drag_mouse_y-font_size/2);
	    view.ctx.fillStyle = 'rgba(0,0,0,0.6)';
	    var group = view.model.getContainingTrackGroup(view.dragged_label_track_id);
	    var label_above_mouse = getLabelAbove(view, group, view.drag_mouse_y, null);
	    var label_below_mouse = getLabelBelow(view, group, view.drag_mouse_y, null);
	    var rect_y;
	    if (label_above_mouse === view.dragged_label_track_id || label_below_mouse === view.dragged_label_track_id) {
		return;
	    }
	    if (label_above_mouse !== null && label_below_mouse !== null) {
		rect_y = (view.label_tops[label_above_mouse] + view.label_tops[label_below_mouse])/2;
	    } else if (label_above_mouse === null) {
		rect_y = view.label_tops[group[0]];
	    } else if (label_below_mouse === null) {
		rect_y = view.label_tops[group[group.length-1]] + view.model.getTrackHeight(group[group.length-1]);
	    }
	    view.ctx.fillRect(0, rect_y, view.ctx.measureText(view.labels[view.dragged_label_track_id]).width, view.minimum_track_height);
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
	return this.maximum_label_width + 40;
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

    return OncoprintLabelView;
})();

module.exports = OncoprintLabelView;