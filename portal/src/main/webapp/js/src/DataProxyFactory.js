/**
 * Singleton utility class for global data proxy instances.
 *
 * This class is designed to provide global access to Data Proxy instances
 * where we need to share the same data among multiple view components.
 *
 * @author Selcuk Onur Sumer
 */
var DataProxyFactory = (function()
{
	// singleton mutation data proxy instance
	var _defaultMutationDataProxy = null;

	/**
	 * Initializes the default MutationDataProxy instance (if not initialized yet)
	 * based on the current genetic profiles and case list.
	 *
	 * @return singleton instance of MutationDataProxy class
	 */
	function getDefaultMutationDataProxy()
	{
		// init default mutation data proxy only once
		if (_defaultMutationDataProxy == null)
		{
			// get global params
			var servletParams = {geneticProfiles: PortalGlobals.getGeneticProfiles(),
				caseList: PortalGlobals.getCases()};

			// default servlet name for mutation data
			var servletName = "getMutationData.json";

			// init mutation data proxy with the data servlet config
			var proxy = new MutationDataProxy(PortalGlobals.getGeneListString());
			proxy.initWithoutData(servletName, servletParams);

			// update singleton reference
			_defaultMutationDataProxy = proxy;
		}

		return _defaultMutationDataProxy;
	}

	return {
		getDefaultMutationDataProxy: getDefaultMutationDataProxy
	}
})();
