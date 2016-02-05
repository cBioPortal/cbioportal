var OncoprintTrackOptionsView = (function() {
    function OncoprintTrackOptionsView($div, removeCallback) {
	// removeCallback: function(track_id)
	var position = $div.css('position');
	if (position !== 'absolute' && position !=='relative') {
	    console.log("WARNING: div passed to OncoprintTrackOptionsView must be absolute or relative positioned - layout problems will occur");
	}
	
	this.removeCallback = removeCallback;
	this.$div = $div;
	this.img_size;
	
	this.track_options_$elts = {};
	
	var self = this;
	$(document).click(function () {
	    for (var track_id in self.track_options_$elts) {
		if (self.track_options_$elts.hasOwnProperty(track_id)) {
		    hideTrackMenu(self, track_id);
		}
	    }
	});
    }
    
    var renderAllOptions = function(view, model) {
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
	var $elts = view.track_options_$elts[track_id];
	$elts.$div.css({'z-index': 1});
	$elts.$dropdown.css({'border': '1px solid rgba(125,125,125,0)'});
	$elts.$img.css({'border': '1px solid rgba(125,125,125,0)'});
	$elts.$dropdown.fadeOut(100);
    };
    
    var showTrackMenu = function(view, track_id) {
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
    
    var renderTrackOptions = function(view, model, track_id) {
	if (model.isTrackRemovable(track_id) || model.isTrackSortDirectionChangeable(track_id)) {
	    
	    var $div = $('<div>').appendTo(view.$div).css({'position':'absolute', 'left':'0px', 'top':model.getTrackTop(track_id)+'px'});
	    var $img = $('<img/>').appendTo($div).attr({'src':'images/menudots.svg', 'width':view.img_size, 'height':view.img_size}).css({'float':'left', 'cursor':'pointer','border':'1px solid rgba(125,125,125,0)'});
	    var $dropdown = $('<ul>').appendTo($div).css({'display':'none', 'list-style-type':'none', 'padding-left':'6', 'padding-right':'6', 'float':'right','background-color':'rgb(255,255,255)'});
	    
	    view.track_options_$elts[track_id] = {'$div':$div, '$img':$img, '$dropdown':$dropdown};
	    
	    var $remove_track_option = $('<li>').text("Remove track").css({'cursor':'pointer'}).appendTo($dropdown).click(function (evt) {
		evt.stopPropagation();
		view.removeCallback(track_id);
	    }).hover(function() {
		$(this).css({'background-color':'rgb(200,200,200)'});
	    }, function() {
		$(this).css({'background-color':'rgba(255,255,255,0)'});
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
	}
    };
    
    OncoprintTrackOptionsView.prototype.addTracks = function(model) {
	renderAllOptions(this, model);
    }
    OncoprintTrackOptionsView.prototype.moveTrack = function(model) {
	renderAllOptions(this, model);
    }
    OncoprintTrackOptionsView.prototype.removeTrack = function(model, track_id) {
	delete this.track_options_$elts[track_id];
	renderAllOptions(this, model);
    }
    return OncoprintTrackOptionsView;
})();

module.exports = OncoprintTrackOptionsView;