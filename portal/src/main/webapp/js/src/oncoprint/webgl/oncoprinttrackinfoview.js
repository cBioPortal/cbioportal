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
	//view.font_size = minimum_track_height;
	
	view.width = 0;
	var label_tops = model.getLabelTops();
	for (var i=0; i<tracks.length; i++) {
	    var $new_label = $('<span>').css({'position':'absolute', 
					    'font-family':view.font_family, 
					    'font-weight':view.font_weight, 
					    'font-size':view.font_size})
					.addClass('noselect');
	    $new_label.text(model.getTrackInfo(tracks[i]));
	    $new_label.appendTo(view.$div);
	    $new_label.css({'top':label_tops[tracks[i]] + (model.getCellHeight(tracks[i]) - $new_label[0].clientHeight)/2});
	    view.width = Math.max(view.width, $new_label[0].clientWidth);
	}
    };
    var resize = function(view, model) {
	view.$div.css({'width':view.getWidth(), 'height':model.getCellViewHeight()});
    };
    OncoprintTrackInfoView.prototype.getWidth = function() {
	return this.width + 10;
    }
    OncoprintTrackInfoView.prototype.addTracks = function(model) {
	renderAllInfo(this, model);
	resize(this, model);
    }
    OncoprintTrackInfoView.prototype.moveTrack = function(model) {
	renderAllInfo(this, model);
	resize(this, model);
    }
    OncoprintTrackInfoView.prototype.removeTrack = function(model) {
	renderAllInfo(this, model);
	resize(this, model);
    }
    OncoprintTrackInfoView.prototype.setTrackInfo = function(model) {
	renderAllInfo(this, model);
	resize(this, model);
    }
    return OncoprintTrackInfoView;
})();

module.exports = OncoprintTrackInfoView;