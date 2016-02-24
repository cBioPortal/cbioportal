var OncoprintToolTip = (function() {
    function OncoprintToolTip($container) {
	this.$container = $container;
	this.$div = $('<div></div>').appendTo($container).css({'background-color':'rgba(255,255,255,1)', 'position':'absolute', 'display':'none', 'border':'1px solid black', 'max-width':300, 'min-width':150});
	this.hide_timeout_id = undefined;
    }
    OncoprintToolTip.prototype.show = function(page_x, page_y, html_str) {
	cancelScheduledHide(this);
	this.$div.html(html_str);
	this.$div.show();
	var container_offset = this.$container.offset();
	var x = page_x - container_offset.left - this.$div.width()/2;
	var y = page_y - container_offset.top - this.$div.height()-5;
	this.$div.css({'top':y, 'left':x, 'z-index':9999});
	
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
    var doHide = function(tt) {
	cancelScheduledHide(tt);
	tt.hide_timeout_id = undefined;
	tt.$div.hide();
    };
    var cancelScheduledHide = function(tt) {
	clearTimeout(tt.hide_timeout_id);
    };
    OncoprintToolTip.prototype.hideIfNotAlreadyGoingTo = function(wait) {
	if (typeof this.hide_timeout_id === 'undefined') {
	    this.hide(wait);
	}
    };
    OncoprintToolTip.prototype.hide = function(wait) {
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
    return OncoprintToolTip;
})();

module.exports = OncoprintToolTip;