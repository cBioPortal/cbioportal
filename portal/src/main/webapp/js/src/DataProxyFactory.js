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
			// set servlet params by using global params
			// note that gene list is not set as a servlet param, it is passed a constructor parameter
			var servletParams = {};

			//servletParams.geneticProfiles = PortalGlobals.getGeneticProfiles();
			servletParams.geneticProfiles = PortalGlobals.getCancerStudyId() + "_mutations";

			var caseSetId = PortalGlobals.getCaseSetId();
			var caseIdsKey = PortalGlobals.getCaseIdsKey();

			// first, try to retrieve mutation data by using a predefined case set id
			if (caseSetId &&
				caseSetId.length > 0 &&
				caseSetId != "-1")
			{
				servletParams.caseSetId = caseSetId;
			}
			// second, try to use a custom case set defined by a hash key
			else if (caseIdsKey &&
			         caseIdsKey.length > 0)
			{
				servletParams.caseIdsKey = caseIdsKey;
			}
			// last resort: send the actual case list as a long string
			else
			{
				servletParams.caseList = PortalGlobals.getCases();
			}

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
