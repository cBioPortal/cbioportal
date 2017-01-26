var Oncoprint = require('./oncoprint.js');
$(document).ready(function() {
	window.oncoprint = new Oncoprint('#oncoprint');
	var data = [];
	while (data.length < 1000) {
		data.push({'sample':Math.random(), data:Math.random()*10});
	}
	var rule_set_params = {
		type: 'bar',
		value_key: 'data',
		value_range:[0,10],
		legend_label: 'Data'
	};
	window.oncoprint.addTracks([{'data':data, 'rule_set_params': rule_set_params, 'data_id_key':'sample'},
		    {'data':data, 'rule_set_params': rule_set_params, 'data_id_key':'sample'},
		    {'data':data, 'rule_set_params': rule_set_params, 'data_id_key':'sample'},
		    {'data':data, 'rule_set_params': rule_set_params, 'target_group':1, 'data_id_key':'sample'},
		    {'data':data, 'rule_set_params':rule_set_params, 'target_group':2, 'data_id_key':'sample'}]);
		
	window.addTracks = function() {
	    var tracks_to_add = [];
	    for (var i=0; i<30; i++) {
		tracks_to_add.push({'data':data, 'rule_set_params':rule_set_params, 'target_group':3, 'data_id_key':'sample'});
	    }
	    window.oncoprint.addTracks(tracks_to_add);
	}
});
