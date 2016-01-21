window.CreateCBioPortalOncoprint = function(ctr_selector) {
    $('#oncoprint #everything').show();
    var canvas = $(ctr_selector).append('<canvas width="2000" height="1500"></canvas>').css({'border': '1px black solid'});
    var oncoprint = new window.Oncoprint(null, $(ctr_selector +' canvas'));
    QuerySession.getGenomicEventData().then(function(data) {
	var data_by_gene = {};
	for (var i=0; i<data.length; i++) {
	    var d = data[i];
	    if (!data_by_gene[d.gene]) {
		data_by_gene[d.gene] = [];
	    }
	    data_by_gene[d.gene].push(d);
	}
	var rule_set_params = {
		type: 'gene',
	};
	oncoprint.suppressRendering();
	for (var gene in data_by_gene) {
	    if (data_by_gene.hasOwnProperty(gene)) {
		oncoprint.addTracks([{'data':data_by_gene[gene], 'rule_set_params': rule_set_params, 'data_id_key':'sample'}]);
	    }
	}
	oncoprint.releaseRendering();
    });
    window.oncoprint = oncoprint;
}


$(document).ready(function() {
    CreateCBioPortalOncoprint('#oncoprint #everything');
});