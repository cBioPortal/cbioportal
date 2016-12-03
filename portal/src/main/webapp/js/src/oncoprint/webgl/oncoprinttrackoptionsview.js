var OncoprintTrackOptionsView = (function() {
    function OncoprintTrackOptionsView($div, moveUpCallback, moveDownCallback, removeCallback, sortChangeCallback) {
	// removeCallback: function(track_id)
	var position = $div.css('position');
	if (position !== 'absolute' && position !=='relative') {
	    console.log("WARNING: div passed to OncoprintTrackOptionsView must be absolute or relative positioned - layout problems will occur");
	}
	
	this.moveUpCallback = moveUpCallback;
	this.moveDownCallback = moveDownCallback;
	this.removeCallback = removeCallback; // function(track_id) { ... }
	this.sortChangeCallback = sortChangeCallback; // function(track_id, dir) { ... }
	
	this.$div = $div;
	
	this.img_size;
	
	this.rendering_suppressed = false;
	
	this.track_options_$elts = {};
	
	this.menu_shown = {};
	
	var self = this;
	$(document).click(function () {
	    for (var track_id in self.track_options_$elts) {
		if (self.track_options_$elts.hasOwnProperty(track_id)) {
		    hideTrackMenu(self, track_id);
		}
	    }
	});
	
	this.interaction_disabled = false;
    }
    
    var renderAllOptions = function(view, model) {
	if (this.rendering_suppressed) {
	    return;
	}
	view.$div.empty();
	
	var tracks = model.getTracks();
	var minimum_track_height = Number.POSITIVE_INFINITY;
	for (var i=0; i<tracks.length; i++) {
	    minimum_track_height = Math.min(minimum_track_height, model.getTrackHeight(tracks[i]));
	}
	view.img_size = Math.floor(minimum_track_height*0.75);
	
	for (var i=0; i<tracks.length; i++) {
	    renderTrackOptions(view, model, tracks[i]);
	}
    };
    
    var hideTrackMenu = function(view, track_id) {
	view.menu_shown[track_id] = false;
	var $elts = view.track_options_$elts[track_id];
	$elts.$div.css({'z-index': 1});
	$elts.$dropdown.css({'border': '1px solid rgba(125,125,125,0)'});
	$elts.$img.css({'border': '1px solid rgba(125,125,125,0)'});
	$elts.$dropdown.fadeOut(100);
    };
    
    var showTrackMenu = function(view, track_id) {
	view.menu_shown[track_id] = true;
	var $elts = view.track_options_$elts[track_id];
	$elts.$div.css({'z-index': 10});
	$elts.$dropdown.css({'border': '1px solid rgba(125,125,125,1)'});
	$elts.$img.css({'border': '1px solid rgba(125,125,125,1)'});
	$elts.$dropdown.fadeIn(100);
    };
    
    var hideMenusExcept = function(view, track_id) {
	track_id = track_id.toString();
	for (var other_track_id in view.track_options_$elts) {
	    if (view.track_options_$elts.hasOwnProperty(other_track_id)) {
		if (other_track_id === track_id) {
		    continue;
		}
		hideTrackMenu(view, other_track_id);
	    }
	}
    };
    
    var $makeDropdownOption = function(text, weight, callback) {
	return $('<li>').text(text).css({'font-weight': weight, 'font-size': 12, 'cursor': 'pointer', 'border-bottom':'1px solid rgba(0,0,0,0.3)'})
		.click(callback)
		.hover(function () {
		    $(this).css({'background-color': 'rgb(200,200,200)'});
		}, function () {
		    $(this).css({'background-color': 'rgba(255,255,255,0)'});
		});
    };
    var $makeDropdownSeparator = function() {
	return $('<li>').css({'border-top': '1px solid black'});
    };
    
    var renderTrackOptions = function(view, model, track_id) {
	var $div,$img,$dropdown;
	//if (model.isTrackRemovable(track_id) || model.isTrackSortDirectionChangeable(track_id)) {
	    $div = $('<div>').appendTo(view.$div).css({'position': 'absolute', 'left': '0px', 'top': model.getTrackTops(track_id) + 'px'});
	    $img = $('<img/>').appendTo($div).attr({'src': 'images/menudots.svg', 'width': view.img_size, 'height': view.img_size, 'alt': 'Track options'}).css({'float': 'left', 'cursor': 'pointer', 'border': '1px solid rgba(125,125,125,0)'});
	    $dropdown = $('<ul>').appendTo($div).css({'width': 120, 'display': 'none', 'list-style-type': 'none', 'padding-left': '6', 'padding-right': '6', 'float': 'right', 'background-color': 'rgb(255,255,255)'});
	    view.track_options_$elts[track_id] = {'$div': $div, '$img': $img, '$dropdown': $dropdown};
	    
	    $img.hover(function(evt) {
		if (!view.menu_shown[track_id]) {
		    $(this).css({'border': '1px solid rgba(125,125,125,0.3)'});
		}
	    }, function(evt) {
		if (!view.menu_shown[track_id]) {
		    $(this).css({'border': '1px solid rgba(125,125,125,0)'});
		}
	    });
	    $img.click(function(evt) {
		evt.stopPropagation();
		if ($dropdown.is(":visible")) {
		    hideTrackMenu(view, track_id);
		} else {
		    showTrackMenu(view, track_id);
		}
		hideMenusExcept(view, track_id);
	    });
	//}

	$dropdown.append($makeDropdownOption('Move up', 'normal', function(evt) {
	    evt.stopPropagation();
	    view.moveUpCallback(track_id);
	}));
	$dropdown.append($makeDropdownOption('Move down', 'normal', function(evt) {
	    evt.stopPropagation();
	    view.moveDownCallback(track_id);
	}));
	if (model.isTrackRemovable(track_id)) {	    
	    $dropdown.append($makeDropdownOption('Remove track', 'normal', function(evt) {
		evt.stopPropagation();
		view.removeCallback(track_id);
	    }));
	}
	if (model.isTrackSortDirectionChangeable(track_id)) {
	    $dropdown.append($makeDropdownSeparator());
	    var $sort_inc_li;
	    var $sort_dec_li;
	    var $dont_sort_li;
	    $sort_inc_li = $makeDropdownOption('Sort a-Z', (model.getTrackSortDirection(track_id) === 1 ? 'bold' : 'normal'), function(evt) {
		evt.stopPropagation();
		$sort_inc_li.css('font-weight', 'bold');
		$sort_dec_li.css('font-weight', 'normal');
		$dont_sort_li.css('font-weight', 'normal');
		view.sortChangeCallback(track_id, 1);
	    });
	    $sort_dec_li = $makeDropdownOption('Sort Z-a', (model.getTrackSortDirection(track_id) === -1 ? 'bold' : 'normal'), function(evt) {
		evt.stopPropagation();
		$sort_inc_li.css('font-weight', 'normal');
		$sort_dec_li.css('font-weight', 'bold');
		$dont_sort_li.css('font-weight', 'normal');
		view.sortChangeCallback(track_id, -1);
	    });
	    $dont_sort_li = $makeDropdownOption('Don\'t sort track', (model.getTrackSortDirection(track_id) === 0 ? 'bold' : 'normal'), function(evt) {
		evt.stopPropagation();
		$sort_inc_li.css('font-weight', 'normal');
		$sort_dec_li.css('font-weight', 'normal');
		$dont_sort_li.css('font-weight', 'bold');
		view.sortChangeCallback(track_id, 0);
	    });
	    $dropdown.append($sort_inc_li);
	    $dropdown.append($sort_dec_li);
	    $dropdown.append($dont_sort_li);
	}
    };
    
    OncoprintTrackOptionsView.prototype.enableInteraction = function() {
	this.interaction_disabled = false;
    }
    OncoprintTrackOptionsView.prototype.disableInteraction = function() {
	this.interaction_disabled = true;
    }
    OncoprintTrackOptionsView.prototype.suppressRendering = function() {
	this.rendering_suppressed = true;
    }
    OncoprintTrackOptionsView.prototype.releaseRendering = function(model) {
	this.rendering_suppressed = false;
	renderAllOptions(this, model);
    }
    OncoprintTrackOptionsView.prototype.getWidth = function() {
	return 10 + this.img_size;
    }
    OncoprintTrackOptionsView.prototype.addTracks = function(model) {
	renderAllOptions(this, model);
    }
    OncoprintTrackOptionsView.prototype.moveTrack = function(model) {
	renderAllOptions(this, model);
    }
    OncoprintTrackOptionsView.prototype.setTrackGroupOrder = function(model) {
	renderAllOptions(this, model);
    }
    OncoprintTrackOptionsView.prototype.removeTrack = function(model, track_id) {
	delete this.track_options_$elts[track_id];
	renderAllOptions(this, model);
    }
    return OncoprintTrackOptionsView;
})();

module.exports = OncoprintTrackOptionsView;