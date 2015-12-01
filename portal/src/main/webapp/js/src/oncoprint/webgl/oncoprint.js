var OncoprintModel = require('./oncoprintmodel.js');
var OncoprintSVGCellView = require('./oncoprintsvgcellview.js');
//var OncoprintWebGLCellView = require('oncoprintwebglcellview.js');
var OncoprintLabelView = require('./oncoprintlabelview.js');
var OncoprintRuleSet = require('./oncoprintruleset.js');

var Oncoprint = (function() {
	// this is the controller
	var nextTrackId = (function() {
		var ctr = 0;
		return function() {
			ctr += 1;
			return ctr;
		}
	})();
	function Oncoprint($svg_dev, $canvas_dev) {
		this.model = new OncoprintModel();
		
		// Precisely one of the following should be uncommented
		this.cell_view = new OncoprintSVGCellView($svg_dev);
		// this.cell_view = new OncoprintWebGLCellView($canvas_dev)
		
		this.label_view = new OncoprintLabelView();
	}
	
	Oncoprint.prototype.addTrack = function(params) {
		// Update model
		var track_id = nextTrackId();
		this.model.addTrack(track_id, params.target_group,
				params.track_height, params.track_padding,
				params.data_id_key, params.tooltipFn,
				params.removable, params.label,
				params.sortCmpFn, params.sort_direction_changeable,
				params.data, OncoprintRuleSet(params.rule_set_params));
		// Update views
		this.cell_view.addTrack(this.model, track_id);
		this.label_view.addTrack(this.model, track_id);
				
		return track_id;
	}
	
	Oncoprint.prototype.removeTrack = function(track_id) {
		// Update model
		this.model.removeTrack(track_id);
		// Update views
		this.cell_view.removeTrack(this.model, track_id);
		this.label_view.removeTrack(this.model, track_id);
	}
	
	Oncoprint.prototype.getZoom = function() {
		return this.model.getZoom();
	}
	
	Oncoprint.prototype.setZoom = function(z) {
		// Update model
		this.model.setZoom(z);
		// Update views
		this.cell_view.setZoom(this.model, z);
	}
	
	Oncoprint.prototype.setTrackData = function(track_id, data) {
		this.model.setTrackData(track_id, data);
		this.cell_view.setTrackData(this.model, track_id);
	}
	
	Oncoprint.prototype.setRuleSet = function(track_id, rule_set_params) {
		this.model.setRuleSet(track_id, OncoprintRuleSet(rule_set_params));
		this.cell_view.setTrackData(this.model, track_id);
	}
	
	
	return Oncoprint;
})();
module.exports = Oncoprint;