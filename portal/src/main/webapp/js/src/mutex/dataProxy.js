var MutexData = (function() {

	var getData = function() {
		console.log(PortalDataColl.getOncoprintData());
	}

	return {
		init: function() {
	        PortalDataCollManager.subscribeOncoprint(getData);
		}
	}

}());
