var OncoprintModel = require('./oncoprintmodel.js');
var OncoprintSVGCellView = require('./oncoprintsvgcellview.js');
var OncoprintWebGLCellView = require('./oncoprintwebglcellview.js');
var OncoprintLabelView = require('./oncoprintlabelview.js');
var OncoprintRuleSet = require('./oncoprintruleset.js');
var OncoprintTrackOptionsView = require('./oncoprinttrackoptionsview.js');
var OncoprintLegendView = require('./oncoprintlegendrenderer.js');//TODO: rename
var OncoprintToolTip = require('./oncoprinttooltip.js');
var OncoprintTrackInfoView = require('./oncoprinttrackinfoview.js');

var svgfactory = require('./svgfactory.js');

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
	this.ctr_selector = ctr_selector;
	
	var $oncoprint_ctr = $('<span></span>')
			    .css({'position':'relative', 'display':'inline-block'})
			    .appendTo(ctr_selector);
	
	var $label_canvas = $('<canvas></canvas>')
			    .css({'display':'inline-block', 
				'position':'absolute', 
				'left':'0px', 
				'top':'0px'})
			    .addClass("noselect")
			    .attr({'width':'150', 'height':'250'});
		    
	var $track_options_div = $('<div></div>')
				.css({'position':'absolute', 
					'left':'150px', 
					'top':'0px'})
				.addClass("noselect")
				.attr({'width':'50', 'height':'250'});
			
	var $legend_div = $('<div></div>')
			    .css({'position':'absolute', 
				    'top':'250px'})
			    .addClass("noselect");
	
	var $cell_div = $('<div>')
			.css({'width':width,
			    'overflow-x':'scroll', 
			    'overflow-y':'hidden', 
			    'display':'inline-block', 
			    'position':'absolute', 
			    'left':'200px', 
			    'top':'0px'})
			.addClass("noselect");
		
	var $cell_canvas = $('<canvas></canvas>')
			    .attr('width', width)
			    .css({'position':'absolute', 'top':'0px', 'left':'0px'})
			    .addClass("noselect");
		    
	var $dummy_scroll_div = $('<div>')
				.css({'position':'absolute', 
				    'top':'0', 
				    'left':'0px', 
				    'height':'1px'});
				
	var $cell_overlay_canvas = $('<canvas></canvas>')
				    .attr('width', width)
				    .css({'position':'absolute', 
					    'top':'0px', 
					    'left':'0px'})
				    .addClass("noselect");
			    
	var $track_info_div = $('<div>')
				.css({'position':'absolute'});
	
	$label_canvas.appendTo($oncoprint_ctr);
	$cell_div.appendTo($oncoprint_ctr);
	$track_options_div.appendTo($oncoprint_ctr);
	$track_info_div.appendTo($oncoprint_ctr);
	$legend_div.appendTo($oncoprint_ctr);

	
	$cell_canvas.appendTo($cell_div);
	$dummy_scroll_div.appendTo($cell_div);
	$cell_overlay_canvas.appendTo($cell_div);
	
	this.$container = $oncoprint_ctr;
	this.$cell_div = $cell_div;
	this.$legend_div = $legend_div;
	this.$track_options_div = $track_options_div;
	this.$track_info_div = $track_info_div;
	
	this.model = new OncoprintModel();
	// Precisely one of the following should be uncommented
	// this.cell_view = new OncoprintSVGCellView($svg_dev);
	this.cell_view = new OncoprintWebGLCellView($cell_div, $cell_canvas, $cell_overlay_canvas, $dummy_scroll_div, this.model, new OncoprintToolTip($oncoprint_ctr), function(left, right) {
	    var curr_zoom = self.model.getHorzZoom();
	    var unzoomed_left = left/curr_zoom;
	    var unzoomed_right = right/curr_zoom;
	    var new_zoom = Math.min(1, self.cell_view.visible_area_width / (unzoomed_right-unzoomed_left));
	    self.setHorzZoom(new_zoom);
	    self.$cell_div.scrollLeft(unzoomed_left*new_zoom);
	});
	
	this.track_options_view = new OncoprintTrackOptionsView($track_options_div, 
								function(track_id) { self.removeTrack(track_id); }, 
								function(track_id, dir) { self.setTrackSortDirection(track_id, dir); });
	this.track_info_view = new OncoprintTrackInfoView($track_info_div);
								
	//this.track_info_view = new OncoprintTrackInfoView($track_info_div);

	this.label_view = new OncoprintLabelView($label_canvas, this.model, new OncoprintToolTip($oncoprint_ctr));
	this.label_view.setDragCallback(function(target_track, new_previous_track) {
	    self.moveTrack(target_track, new_previous_track);
	});
	
	this.legend_view = new OncoprintLegendView($legend_div, 10, 20);
	
	this.rendering_suppressed = false;
	
	this.keep_sorted = false;
	// We need to handle scrolling this way because for some reason huge 
	//  canvas elements have terrible resolution.
	var cell_view = this.cell_view;
	var model = this.model;
	$cell_div.scroll(function() {
	    var scroll_left = $cell_div.scrollLeft();
	    $cell_canvas.css('left', scroll_left);
	    $cell_overlay_canvas.css('left', scroll_left);
	    cell_view.scroll(model, scroll_left);
	});
	
	this.horz_zoom_callbacks = [];
	
	
	$(window).resize(function() {
	    resizeAndOrganize(self);
	});
    }

    var resizeLegendAfterTimeout = function(oncoprint) {
	setTimeout(function() {
	    oncoprint.$container.css({'min-height':oncoprint.model.getCellViewHeight() + oncoprint.$legend_div.height() + 20});
	    oncoprint.$legend_div.css({'top':oncoprint.model.getCellViewHeight() + 20});
	}, 0);
    };
    var resizeAndOrganize = function(oncoprint) {
	var ctr_width = $(oncoprint.ctr_selector).width();
	oncoprint.$container.css({'min-height':oncoprint.model.getCellViewHeight() + oncoprint.$legend_div.height() + 20});
	oncoprint.$track_options_div.css({'left':oncoprint.label_view.getWidth()});
	oncoprint.$track_info_div.css({'left':oncoprint.label_view.getWidth() + oncoprint.track_options_view.getWidth()});
	var cell_div_left = oncoprint.label_view.getWidth() + oncoprint.track_options_view.getWidth() + oncoprint.track_info_view.getWidth();
	oncoprint.$cell_div.css({'left':cell_div_left});
	oncoprint.cell_view.setWidth(ctr_width - cell_div_left-20, oncoprint.model);
	oncoprint.$legend_div.css({'top':oncoprint.model.getCellViewHeight() + 20});
    };
    
    var resizeAndOrganizeAfterTimeout = function(oncoprint) {
	setTimeout(function() {
	    resizeAndOrganize(oncoprint);
	}, 0);
    };
    
    
    Oncoprint.prototype.scrollTo = function(left) {
	this.$cell_div.scrollLeft(left);
    }
    Oncoprint.prototype.onHorzZoom = function(callback) {
	this.horz_zoom_callbacks.push(callback);
    }
    Oncoprint.prototype.moveTrack = function(target_track, new_previous_track) {
	this.model.moveTrack(target_track, new_previous_track);
	this.cell_view.moveTrack(this.model);
	this.label_view.moveTrack(this.model);
	this.track_options_view.moveTrack(this.model);
	this.track_info_view.moveTrack(this.model);
	
	if (this.keep_sorted) {
	    this.sort();
	}
	
	resizeAndOrganizeAfterTimeout(this);
    }
    
    Oncoprint.prototype.keepSorted = function(keep_sorted) {
	this.keep_sorted = (typeof keep_sorted === 'undefined' ? true : keep_sorted);
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
	this.track_options_view.addTracks(this.model);
	this.track_info_view.addTracks(this.model);
	this.legend_view.addTracks(this.model);
	
	if (this.keep_sorted) {
	    this.sort();
	}
	if (!this.rendering_suppressed) {
	    resizeAndOrganizeAfterTimeout(this);
	}
	return track_ids;
    }

    Oncoprint.prototype.removeTrack = function (track_id) {
	// Update model
	this.model.removeTrack(track_id);
	// Update views
	this.cell_view.removeTrack(this.model, track_id);
	this.label_view.removeTrack(this.model, track_id);
	this.track_options_view.removeTrack(this.model, track_id);
	this.track_info_view.removeTrack(this.model);
	this.legend_view.removeTrack(this.model);
	
	if (this.keep_sorted) {
	    this.sort();
	}
	resizeAndOrganizeAfterTimeout(this);
    }

    Oncoprint.prototype.getZoomToFitHorz = function(ids) {
	var width_to_fit_in;
	if (typeof ids === 'undefined') {
	    width_to_fit_in = this.cell_view.getTotalWidth(this.model, true);
	} else {
	    var furthest_right_id_index = -1;
	    var furthest_right_id;
	    var id_to_index_map = this.model.getIdToIndexMap();
	    for (var i=0; i<ids.length; i++) {
		if (id_to_index_map[ids[i]] > furthest_right_id_index) {
		    furthest_right_id_index = id_to_index_map[ids[i]];
		    furthest_right_id = ids[i];
		}
	    }
	    width_to_fit_in = this.model.getColumnLeft(furthest_right_id) + this.model.getCellWidth(true);
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
	this.cell_view.setHorzZoom(this.model);

	for (var i=0; i<this.horz_zoom_callbacks.length; i++) {
	    this.horz_zoom_callbacks[i](this.model.getHorzZoom());
	}
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
	
	resizeAndOrganizeAfterTimeout(this);
	return this.model.getVertZoom();
    }

    Oncoprint.prototype.setTrackData = function (track_id, data, data_id_key) {
	this.model.setTrackData(track_id, data, data_id_key);
	this.cell_view.setTrackData(this.model, track_id);
	
	if (this.keep_sorted) {
	    this.sort();
	}
	resizeAndOrganizeAfterTimeout(this);
    }

    Oncoprint.prototype.setTrackGroupSortPriority = function(priority) {
	this.model.setTrackGroupSortPriority(priority);
	this.cell_view.setTrackGroupSortPriority(this.model);
	
	if (this.keep_sorted) {
	    this.sort();
	}
	resizeAndOrganizeAfterTimeout(this);
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
    
    Oncoprint.prototype.setTrackSortComparator = function(track_id, sortCmpFn) {
	this.model.setTrackSortComparator(track_id, sortCmpFn);
	if (this.keep_sorted) {
	    this.sort();
	}
    }
    
    Oncoprint.prototype.cycleTrackSortDirection = function(track_id) {
	var curr_dir = this.model.getTrackSortDirection(track_id);
	var next_dir;
	if (curr_dir === 1) {
	    next_dir = -1;
	} else if (curr_dir === -1) {
	    next_dir = 0;
	} else if (curr_dir === 0) {
	    next_dir = 1;
	}
	this.setTrackSortDirection(track_id, next_dir);
    }
    
    Oncoprint.prototype.getTrackSortDirection = function(track_id) {
	return this.model.getTrackSortDirection(track_id);
    }
    
    Oncoprint.prototype.setTrackInfo = function(track_id, msg) {
	this.model.setTrackInfo(track_id, msg);
	this.track_info_view.setTrackInfo(this.model);
    }
    
    Oncoprint.prototype.sort = function() {
	this.model.sort();
	this.cell_view.sort(this.model);
    }
    
    Oncoprint.prototype.shareRuleSet = function(source_track_id, target_track_id) {
	this.model.shareRuleSet(source_track_id, target_track_id);
	this.cell_view.shareRuleSet(this.model, target_track_id);
	this.legend_view.shareRuleSet(this.model);
    }
    
    Oncoprint.prototype.setRuleSet = function(track_id, rule_set_params) {
	this.model.setRuleSet(track_id, OncoprintRuleSet(rule_set_params));
	this.cell_view.setRuleSet(this.model, track_id);
	this.legend_view.setRuleSet(this.model);
	resizeAndOrganizeAfterTimeout(this);
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
    
    Oncoprint.prototype.disableInteraction = function() {
	//this.label_view.disableInteraction();
	//this.cell_view.disableInteraction();
	this.track_options_view.disableInteraction();
	//this.track_info_view.disableInteraction();
	//this.legend_view.disableInteraction();
    }
    Oncoprint.prototype.enableInteraction = function() {
	//this.label_view.enableInteraction();
	//this.cell_view.enableInteraction();
	this.track_options_view.enableInteraction();
	//this.track_info_view.enableInteraction();
	//this.legend_view.enableInteraction();
    }
    Oncoprint.prototype.suppressRendering = function() {
	this.rendering_suppressed = true;
	this.label_view.suppressRendering();
	this.cell_view.suppressRendering();
	this.track_options_view.suppressRendering();
	this.track_info_view.suppressRendering();
	this.legend_view.suppressRendering();
    }
    
    Oncoprint.prototype.releaseRendering = function() {
	this.rendering_suppressed = false;
	this.label_view.releaseRendering(this.model);
	this.cell_view.releaseRendering(this.model);
	this.track_options_view.releaseRendering(this.model);
	this.track_info_view.releaseRendering(this.model);
	this.legend_view.releaseRendering(this.model);
	resizeAndOrganizeAfterTimeout(this);
    }
    
    Oncoprint.prototype.hideIds = function(to_hide, show_others) {
	this.model.hideIds(to_hide, show_others);
	this.cell_view.hideIds(this.model);
    }
    
    Oncoprint.prototype.hideTrackLegend = function(track_id) {
	this.model.hideTrackLegend(track_id);
	this.legend_view.hideTrackLegend(this.model);
	resizeLegendAfterTimeout(this);
    }
    
    Oncoprint.prototype.showTrackLegend = function(track_id) {
	this.model.showTrackLegend(track_id);
	this.legend_view.showTrackLegend(this.model);
	resizeLegendAfterTimeout(this);
    }
    
    Oncoprint.prototype.setCellPaddingOn = function(cell_padding_on) {
	this.model.setCellPaddingOn(cell_padding_on);
	this.cell_view.setCellPaddingOn(this.model);
    }
    
    Oncoprint.prototype.toSVG = function() {
	var root = svgfactory.svg(10, 10);
	this.$container.append(root);
	var everything_group = svgfactory.group(0,0);
	root.appendChild(everything_group);
	var label_view_group = this.label_view.toSVGGroup(this.model, true, 0, 0);
	everything_group.appendChild(label_view_group);
	var track_info_group_x = label_view_group.getBBox().width + 30;
	var track_info_group = this.track_info_view.toSVGGroup(this.model, track_info_group_x, 0);
	everything_group.appendChild(track_info_group);
	var cell_view_group_x = track_info_group_x + track_info_group.getBBox().width + 10;
	everything_group.appendChild(this.cell_view.toSVGGroup(this.model, cell_view_group_x, 0));
	everything_group.appendChild(this.legend_view.toSVGGroup(this.model, 0, label_view_group.getBBox().y + label_view_group.getBBox().height+20));
	
	var everything_box = everything_group.getBBox();
	root.setAttribute('width', everything_box.x + everything_box.width);
	root.setAttribute('height', everything_box.y + everything_box.height);
	root.parentNode.removeChild(root);
	
	return root;
    }
    
    Oncoprint.prototype.getIdOrder = function(all) {
	return this.model.getIdOrder(all);
    }
    
    return Oncoprint;
})();
module.exports = Oncoprint;