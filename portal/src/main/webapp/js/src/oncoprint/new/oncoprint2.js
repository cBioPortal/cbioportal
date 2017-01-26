exports = {};

var events = require('events');
var utils = require('utils');

function makeIdCounter() {
	var id = 0;
	return function () {
		id += 1;
		return id;
	};
}

var Oncoprint = (function() {
	var default_config = {
		cell_width: 6,
		cell_padding: 2.5,
		legend: true,
		track_padding: 5,
		track_section_padding: 10
	};
	
	var default_track_config = {
		label: 'Gene',
		cell_height: 23,
		default_sort: undefined,
		is_removable: false,
		sort_direction_is_changeable: false,
	};
	
	function Oncoprint(config) {
		var self = this;
		var g
	}
})();