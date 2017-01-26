var $ = require('jquery');

exports = {
	init: function(selector) {
		var container = $(selector).empty().addClass('oncoprint-container noselect');
		var content = container.append('div').addClass('oncoprint-content');
		
		
	}
};