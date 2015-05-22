$(document).ready(function() {
	$.getJSON("portal_meta_data.json", function(json) { 
		window.portalMetaData = json;
	});
});