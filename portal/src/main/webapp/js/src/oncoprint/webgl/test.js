$(document).ready(function() {
	/*var o = new Oncoprint($('#svg'), $('#canvas'));
	var data = [{sample:'a', data:5}, {sample:'b', data:10}];
	while (data.length < 1000) {
		data = data.concat(data);
	}
	var rule_set_params = {
		type: 'bar',
		value_key: 'data',
		value_range:[0,10]
	};
	o.addTracks([{'data':data, 'rule_set_params': rule_set_params},
		    {'data':data, 'rule_set_params': rule_set_params},
		    {'data':data, 'rule_set_params': rule_set_params},
		    {'data':data, 'rule_set_params': rule_set_params, 'target_group':1},
		    {'data':data, 'rule_set_params':rule_set_params, 'target_group':2}]);
		
	window.addTracks = function() {
	    var tracks_to_add = [];
	    for (var i=0; i<30; i++) {
		tracks_to_add.push({'data':data, 'rule_set_params':rule_set_params, 'target_group':3});
	    }
	    o.addTracks(tracks_to_add);
	}*/
});

window.Oncoprint = require('./oncoprint.js');