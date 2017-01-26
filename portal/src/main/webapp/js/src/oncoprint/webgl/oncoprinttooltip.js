var OncoprintToolTip = (function() {
    function OncoprintToolTip($container, params) {
	params = params || {};
	this.$container = $container;
	this.$div = $('<div></div>').appendTo($container).css({'background-color':'rgba(255,255,255,1)', 'position':'absolute', 'display':'none', 'border':'1px solid black', 'max-width':300, 'min-width':150});
	if (params.noselect) {
	    this.$div.addClass("noselect");
	}
	this.hide_timeout_id = undefined;
	this.show_timeout_id = undefined;
	this.center = false;
	
	this.shown = false;
	
	var self = this;
	this.$div.on("mousemove", function(evt) {
	    evt.stopPropagation();
	    cancelScheduledHide(self);
	});
	this.$div.on("mouseleave", function(evt) {
	    evt.stopPropagation();
	    self.hide();
	});
    }
    OncoprintToolTip.prototype.show = function(wait, page_x, page_y, $contents, fade) {
	cancelScheduledHide(this);
	
	if (typeof wait !== 'undefined' && !this.shown) {
	    var self = this;
	    cancelScheduledShow(this);
	    this.show_timeout_id = setTimeout(function() {
		doShow(self, page_x, page_y, $contents, fade);
	    }, wait);
	} else {
	    doShow(this, page_x, page_y, $contents, fade);
	}
    }
    var doShow = function(tt, page_x, page_y, $contents, fade) {
	cancelScheduledShow(tt);
	tt.show_timeout_id = undefined;
	tt.$div.empty();
	tt.$div.append($contents);
	if (!fade) {
	    tt.$div.show();
	} else {
	    tt.$div.stop().fadeIn('fast');
	}
	var container_offset = tt.$container.offset();
	var x = page_x - container_offset.left - (tt.center ? tt.$div.width()/2 : 0);
	var y = page_y - container_offset.top - tt.$div.height();
	tt.$div.css({'top':y, 'left':x, 'z-index':9999});
	tt.shown = true;
    };
    var doHide = function(tt, fade) {
	cancelScheduledHide(tt);
	tt.hide_timeout_id = undefined;
	if (!fade) {
	    tt.$div.hide();
	} else {
	    tt.$div.fadeOut();
	}
	tt.shown = false;
    };
    var cancelScheduledShow = function(tt) {
	clearTimeout(tt.show_timeout_id);
	tt.show_timeout_id = undefined;
    };
    var cancelScheduledHide = function(tt) {
	clearTimeout(tt.hide_timeout_id);
	tt.hide_timeout_id = undefined;
    };
    OncoprintToolTip.prototype.showIfNotAlreadyGoingTo = function(wait, page_x, page_y, $contents) {
	if (typeof this.show_timeout_id === 'undefined') {
	    this.show(wait, page_x, page_y, $contents);
	}
    }
    OncoprintToolTip.prototype.hideIfNotAlreadyGoingTo = function(wait) {
	if (typeof this.hide_timeout_id === 'undefined') {
	    this.hide(wait);
	}
    };
    OncoprintToolTip.prototype.hide = function(wait) {
	cancelScheduledShow(this);
	
	if (!this.shown) {
	    return;
	}
	
	if (typeof wait !== 'undefined') {
	    var self = this;
	    cancelScheduledHide(this);
	    this.hide_timeout_id = setTimeout(function() {
		doHide(self);
	    }, wait);
	} else {
	    doHide(this);
	}
    }
    OncoprintToolTip.prototype.fadeIn = function(wait, page_x, page_y, $contents) {
	this.show(wait, page_x, page_y, $contents, true);
    }
    return OncoprintToolTip;
})();

module.exports = OncoprintToolTip;