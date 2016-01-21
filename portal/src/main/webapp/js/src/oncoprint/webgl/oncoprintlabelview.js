// FIRST PASS: no optimization
var OncoprintLabelView = (function () {
    function OncoprintLabelView($canvas) {
	this.$canvas = $canvas;
	this.ctx = $canvas[0].getContext('2d');
	this.ctx.font = "20px serif";
	this.ctx.textAlign="start";
	this.ctx.textBaseline="top";
    }
    var renderAllLabels = function(view, model) {
	view.ctx.clearRect(0,0,view.$canvas[0].width,view.$canvas[0].height);
	var tracks = model.getTracks();
	for (var i=0; i<tracks.length; i++) {
	    view.ctx.fillText(model.getTrackLabel(tracks[i]), 0, model.getTrackTop(tracks[i]));
	}
    }
    OncoprintLabelView.prototype.removeTrack = function (model, track_id) {
	renderAllLabels(this, model);
    }
    OncoprintLabelView.prototype.moveTrack = function (model) {
	renderAllLabels(this, model);
    }
    OncoprintLabelView.prototype.addTracks = function (model, track_ids) {
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