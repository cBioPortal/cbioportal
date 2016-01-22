var OncoprintLabelView = (function () {
    function OncoprintLabelView($canvas) {
	var view = this;
	this.$canvas = $canvas;
	this.font_size = 14;
	this.label_tops = {};
	this.labels = {};
	this.tracks = [];
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
		    view.drag_mouse_y = evt.offsetY;
		    renderAllLabels(view);
		} else {
		    if (isMouseOnLabel(view, evt.offsetY) !== null) {
			view.$canvas.css('cursor', 'move');
		    } else {
			view.$canvas.css('cursor', 'auto');
		    }
		}
	    });
	    
	    view.$canvas.on("mouseup mouseleave", function(evt) {
		if (view.dragged_label_track_id !== null) {
		    var previous_track_id = getLabelAboveMouse(view, evt.offsetY, view.dragged_label_track_id);
		    stopDragging(view, previous_track_id);
		}
	    });
	})(this);
	
    }
    var updateFromModel = function(view, model) {
	var track_tops = model.getTrackTops();
	var label_tops = track_tops;
	for (var track_id in label_tops) {
	    if (label_tops.hasOwnProperty(track_id)) {
		label_tops[track_id] += model.getTrackPadding(track_id);
	    }
	}
	view.label_tops = label_tops;
	view.tracks = model.getTracks();
    }
    var setUpContext = function(view) {
	view.ctx = view.$canvas[0].getContext('2d');
	view.ctx.font = view.font_size+'px serif';
	view.ctx.textAlign="start";
	view.ctx.textBaseline="top";
    }
    var resizeAndClear = function(view, model) {
	var tracks = model.getTracks();
	var last_track = tracks[tracks.length-1];
	var height = model.getTrackTop(last_track)+model.getTrackHeight(last_track)+2*model.getTrackPadding(last_track)
		    + model.getBottomPadding();
	view.$canvas[0].height = height;
	setUpContext(view);
    }
    var renderAllLabels = function(view) {
	view.ctx.clearRect(0,0,view.$canvas[0].width,view.$canvas[0].height);
	var tracks = view.tracks;
	for (var i=0; i<tracks.length; i++) {
	    view.ctx.fillText(view.labels[tracks[i]], 0, view.label_tops[tracks[i]]);
	}
	if (view.dragged_label_track_id !== null) {
	    view.ctx.strokeText(view.labels[view.dragged_label_track_id], 0, view.drag_mouse_y-view.font_size/2);
	}
    }
    
    var isMouseOnLabel = function(view, mouse_y) {
	var candidate_track = getLabelAboveMouse(view, mouse_y);
	if (candidate_track === null) {
	    return null;
	}
	if (mouse_y <= view.label_tops[candidate_track] + view.font_size) {
	    return candidate_track;
	} else {
	    return null;
	}
    }
    var getLabelAboveMouse = function(view, mouse_y, track_to_exclude) {
	var track_ids = view.tracks;
	if (mouse_y < view.label_tops[track_ids[0]]) {
	    return null;
	} else {
	    var candidate_track = null;
	    for (var i=0; i<track_ids.length; i++) {
		if (track_to_exclude !== null && track_to_exclude === track_ids[i]) {
		    continue;
		}
		if (view.label_tops[track_ids[i]] > mouse_y) {
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
    OncoprintLabelView.prototype.setTrackData = function () {
	// TODO: what parameters
	// TODO: implementation
    }
    OncoprintLabelView.prototype.setCellPadding = function () {
	// TODO: what parameters
	// TODO: implementation
    }
    OncoprintLabelView.prototype.setZoom = function () {
	// TODO: what parameters
	// TODO: implementation
    }

    return OncoprintLabelView;
})();

module.exports = OncoprintLabelView;