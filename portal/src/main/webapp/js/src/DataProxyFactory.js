/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
			var servletParamsPromise = (function() {
			    var def = new $.Deferred();
			    window.QuerySession.getMutationProfileIds().then(function(mutation_profile_ids) {
				var servletParams = {};
				servletParams.geneticProfiles = mutation_profile_ids[0];
				// first, try to retrieve mutation data by using a predefined case set id
				var caseSetId = window.QuerySession.getCaseSetId();
				var caseIdsKey = window.QuerySession.getCaseIdsKey();

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
				    servletParams.caseList = QuerySession.getSampleIds();
				}
				def.resolve(servletParams);
			    });
			    return def.promise();
			})();
			
			// default servlet name for mutation data
			var servletName = "getMutationData.json";

			// init mutation data proxy with the data servlet config
			var proxy = new MutationDataProxy({
				servletName: servletName,
				geneList: window.QuerySession.getQueryGenes().join(" "),
				paramsPromise: servletParamsPromise
			});
			proxy.init();

			// update singleton reference
			_defaultMutationDataProxy = proxy;
		}

		return _defaultMutationDataProxy;
	}

	return {
		getDefaultMutationDataProxy: getDefaultMutationDataProxy
	}
})();
