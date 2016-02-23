var OncoprintTrackOptionsView = (function() {
    function OncoprintTrackOptionsView($div, removeCallback, sortChangeCallback) {
	// removeCallback: function(track_id)
	var position = $div.css('position');
	if (position !== 'absolute' && position !=='relative') {
	    console.log("WARNING: div passed to OncoprintTrackOptionsView must be absolute or relative positioned - layout problems will occur");
	}
	
	this.removeCallback = removeCallback;
	this.sortChangeCallback = sortChangeCallback;
	
	this.$div = $div;
	this.img_size;
	this.width = 0;
	
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
	var width_contributions = [];
	if (model.isTrackRemovable(track_id)) {
	    width_contributions.push(view.img_size);
	    var $div = $('<div>').appendTo(view.$div).css({'position':'absolute', 'left':'0px', 'top':model.getTrackTops(track_id)+'px'});
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
	if (model.isTrackSortDirectionChangeable(track_id)) {
	    width_contributions.push(5);
	    width_contributions.push(view.img_size);
	    var $svg = $(makeSVGElement('svg')).appendTo(view.$div).attr({'width':view.img_size, 'height':view.img_size}).css({'position':'absolute', 'left':(view.img_size+5)+'px', 'top':model.getTrackTops(track_id)+'px', 'cursor':'pointer'});
	    var increasing_points = [[0, view.img_size], [view.img_size, view.img_size], [view.img_size, 0.25 * view.img_size]].map(function (a) {
		return a[0] + ',' + a[1];
	    }).join(' ');
	    var decreasing_points = [[0, 0.25 * view.img_size], [0, view.img_size], [view.img_size, view.img_size]].map(function (a) {
		return a[0] + ',' + a[1];
	    }).join(' ');
	    var none_points = [[0, 0.5 * view.img_size], [0, view.img_size], [view.img_size, view.img_size], [view.img_size, 0.5 * view.img_size]].map(function (a) {
		return a[0] + ',' + a[1];
	    }).join(' ');
	    
	    var selected_color = 'rgba(255,179,100,1)';
	    var hover_color = 'rgba(255,179,100,0.6)';
	    
	    var $triangle = $(makeSVGElement('polygon', {
		points: increasing_points,
		fill: +selected_color,
		stroke: 'rga(0,0,0,0.7)'
	    })).appendTo($svg);
	    
	    var updateTriangle = function(hover_direction) {
		var hover;
		var direction;
		if (typeof hover_direction !== 'undefined') {
		    hover = true;
		    direction = hover_direction;
		} else {
		    hover = false;
		    direction = model.getTrackSortDirection(track_id);
		}
		var points = (direction === 0 ? none_points : (direction === 1 ? increasing_points : decreasing_points));
		var fill = (hover ? hover_color : selected_color);
		$triangle.attr({'points':points, 'fill':fill});
	    };
	    
	    updateTriangle();
	    
	    $svg.hover(function() {
		var curr_direction = model.getTrackSortDirection(track_id);
		var display_direction;
		if (curr_direction === 1) {
		    display_direction = -1;
		} else if (curr_direction === -1) {
		    display_direction = 0;
		} else if (curr_direction === 0) {
		    display_direction = 1;
		}
		updateTriangle(display_direction);
	    },
	    function() {
		updateTriangle();
	    });
	    
	    $svg.click(function() {
		view.sortChangeCallback(track_id);
		updateTriangle();
	    });
	}
	view.width = Math.max(view.width, width_contributions.reduce(function(acc, curr) {
	    return acc + curr;
	}, 0));
    };
    
    var makeSVGElement = function(tag, attrs) {
	var el = document.createElementNS('http://www.w3.org/2000/svg', tag);
	for (var k in attrs) {
	    if (attrs.hasOwnProperty(k)) {
		el.setAttribute(k, attrs[k]);
	    }
	}
	return el;
    };
    
    OncoprintTrackOptionsView.prototype.getWidth = function() {
	return this.width;
    }
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