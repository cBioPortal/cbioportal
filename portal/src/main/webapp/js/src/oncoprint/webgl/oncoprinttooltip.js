var OncoprintToolTip = (function() {
    function OncoprintToolTip($container) {
	this.$container = $container;
	this.$div = $('<div></div>').appendTo($container).css({'background-color':'rgba(255,255,255,1)', 'position':'absolute', 'display':'none', 'border':'1px solid black'});
    }
    OncoprintToolTip.prototype.show = function(page_x, page_y, html_str) {
	this.$div.show();
	this.$div.html(html_str);
	var container_offset = this.$container.offset();
	var x = page_x - container_offset.left;
	var y = page_y - container_offset.top - 100;
	this.$div.css({'top':y, 'left':x});
    }
    OncoprintToolTip.prototype.hide = function() {
	this.$div.hide();
    }
    return OncoprintToolTip;
})();

module.exports = OncoprintToolTip;