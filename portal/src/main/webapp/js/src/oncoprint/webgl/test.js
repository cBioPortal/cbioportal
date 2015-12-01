$(document).ready(function() {
	var Oncoprint = require('./oncoprint.js');
	console.log($('#svg'));
	var o = new Oncoprint($('#svg'), $('#canvas'));
	var data = [{sample:'a', data:5}, {sample:'b', data:10}];
	while (data.length < 1000) {
		data = data.concat(data);
	}
	var rule_set_params = {
		type: 'bar',
		value_key: 'data',
		value_range:[0,10]
	};
	o.addTrack({'data':data, 'rule_set_params': rule_set_params});
	o.addTrack({'data':data, 'rule_set_params': rule_set_params});
	o.addTrack({'data':data, 'rule_set_params': rule_set_params});
	o.addTrack({'data':data, 'rule_set_params': rule_set_params, 'target_group':1});
	window.o = o;
});