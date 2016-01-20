window.CreateCBioPortalOncoprint = function(ctr_selector) {
    $('#oncoprint #everything').show();
    var canvas = $(ctr_selector).append('<canvas width="2000" height="1500"></canvas>').css({'border': '1px black solid'});
    var oncoprint = new window.Oncoprint(null, $(ctr_selector +' canvas'));
    QuerySession.getGenomicEventData().then(function(data) {
	var firstgene = data[0].gene;
	data = data.filter(function(d) { return d.gene === firstgene; });
	var rule_set_params = {
		type: 'categorical',
		category_key: 'mut_type'
	};
	oncoprint.addTracks([{'data':data, 'rule_set_params': rule_set_params, 'data_id_key':'sample'}]);
    });
}


$(document).ready(function() {
    CreateCBioPortalOncoprint('#oncoprint #everything');
});