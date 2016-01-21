window.CreateCBioPortalOncoprint = function(ctr_selector) {
    $('#oncoprint #everything').show();
    var oncoprint = new window.Oncoprint(ctr_selector, 1050);
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
		oncoprint.addTracks([{'data':data_by_gene[gene], 'rule_set_params': rule_set_params, 'data_id_key':'sample', 'label':gene}]);
	    }
	}
	oncoprint.releaseRendering();
    });
    window.oncoprint = oncoprint;
}


$(document).ready(function() {
    CreateCBioPortalOncoprint('#oncoprint #everything');
});