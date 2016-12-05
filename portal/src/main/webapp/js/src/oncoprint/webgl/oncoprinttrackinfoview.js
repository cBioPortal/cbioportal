var svgfactory = require('./svgfactory.js');

var OncoprintTrackInfoView = (function() {
    function OncoprintTrackInfoView($div) {
	this.$div = $div;
	this.font_size = 12;
	this.font_family = 'Arial';
	this.font_weight = 'bold';
	this.width = 0;
	
	this.rendering_suppressed = false;
    }
    var renderAllInfo = function(view, model) {
	if (view.rendering_suppressed) {
	    return;
	}
	view.$div.empty();
	var tracks = model.getTracks();
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
	    $new_label.css({'top':label_tops[tracks[i]] + (model.getCellHeight(tracks[i]) - $new_label.outerHeight())/2});
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
    OncoprintTrackInfoView.prototype.setTrackGroupOrder = function(model) {
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
    OncoprintTrackInfoView.prototype.suppressRendering = function() {
	this.rendering_suppressed = true;
    }
    OncoprintTrackInfoView.prototype.releaseRendering = function(model) {
	this.rendering_suppressed = false;
	renderAllInfo(this, model);
	resize(this, model);
    }
    OncoprintTrackInfoView.prototype.toSVGGroup = function(model, offset_x, offset_y) {
	var root = svgfactory.group((offset_x || 0), (offset_y || 0));
	var cell_tops = model.getCellTops();
	var tracks = model.getTracks();
	for (var i=0; i<tracks.length; i++) {
	    var track_id = tracks[i];
	    var y = cell_tops[track_id] + model.getCellHeight(track_id)/2;
	    var info = model.getTrackInfo(track_id);
	    var text_elt = svgfactory.text(info, 0, y, this.font_size, this.font_family, this.font_weight, "bottom");
	    text_elt.setAttribute("dy", "0.35em");
	    root.appendChild(text_elt);
	}
	return root;
    }
    return OncoprintTrackInfoView;
})();

module.exports = OncoprintTrackInfoView;