var OncoprintTrackInfoView = (function() {
    function OncoprintTrackInfoView($div) {
	this.$div = $div;
	this.font_size = 12;
	this.font_family = 'serif';
	this.font_weight = 'bold';
	this.width = 0;
    }
    var renderAllInfo = function(view, model) {
	view.$div.empty();
	var tracks = model.getTracks();
	var minimum_track_height = Number.POSITIVE_INFINITY;
	for (var i=0; i<tracks.length; i++) {
	    minimum_track_height = Math.min(minimum_track_height, model.getTrackHeight(tracks[i]));
	}
	view.font_size = minimum_track_height;
	
	view.width = 0;
	var label_tops = model.getLabelTops();
	for (var i=0; i<tracks.length; i++) {
	    var $new_label = $('<span>').css({'font-family':view.font_family, 'font-weight':view.font_weight, 'font-size':view.font_size});
	    $new_label.text(model.getTrackInfo(tracks[i]));
	    $new_label.appendTo(view.$div).css({'top':label_tops[tracks[i]]});
	    view.width = Math.max(view.width, $new_label.width());
	}
    };
    OncoprintTrackInfoView.prototype.getWidth = function() {
	return this.width;
    }
    OncoprintTrackInfoView.prototype.addTracks = function(model) {
	renderAllInfo(this, model);
    }
    OncoprintTrackInfoView.prototype.moveTrack = function(model) {
	renderAllInfo(this, model);
    }
    OncoprintTrackInfoView.prototype.removeTrack = function(model) {
	renderAllInfo(this, model);
    }
    return OncoprintTrackInfoView;
})();

module.exports = OncoprintTrackInfoView;