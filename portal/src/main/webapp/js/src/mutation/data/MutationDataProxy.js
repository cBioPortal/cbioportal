/**
 * This class is designed to retrieve mutation data on demand, but it can be also
 * initialized with the full mutation data already retrieved from the server.
 *
 * @param geneList  list of target genes (genes of interest) as a string
 *
 * @author Selcuk Onur Sumer
 */
var MutationDataProxy = function(geneList)
{
	// MutationDetailsUtil instance
	var _util = new MutationDetailsUtil();
	// list of target genes as an array of strings (in the exact input order)
	var _unsortedGeneList = geneList.trim().split(/\s+/);
	// alphabetically sorted list of target genes as an array of strings
	var _geneList = geneList.trim().split(/\s+/).sort();
	// name of the mutation data servlet
	var _servletName;
	// parameters to be sent to the mutation data servlet
	var _servletParams;
	// flag to indicate if the initialization is full or lazy
	var _fullInit;

	/**
	 * Initializes the proxy without actually grabbing anything from the server.
	 * Provided servlet name and servlet parameters will be used for later invocation
	 * of getMutationData function.
	 *
	 * @param servletName   name of the mutation data servlet (used for AJAX query)
	 * @param servletParams servlet (query) parameters
	 */
	function lazyInit(servletName, servletParams)
	{
		_servletName = servletName;
		_servletParams = servletParams;
		_fullInit = false;
	}

	/**
	 * Initializes with full mutation data. Once initialized with full data,
	 * this proxy class assumes that there will be no additional mutation data.
	 *
	 * @param mutationData  full mutation data
	 */
	function fullInit(mutationData)
	{
		var mutations = new MutationCollection(mutationData);
		_util.processMutationData(mutations);
		_fullInit = true;
	}

	function getGeneList()
	{
		// TODO lazy init: to find out genes with mutation data ONLY,
		// we need to query the server before hand. Otherwise,
		// we cannot remove the genes without data from the list until
		// the corresponding gene tab is clicked.
		return _geneList;
	}

	function getUnsortedGeneList()
	{
		return _unsortedGeneList;
	}

	function getMutationUtil()
	{
		return _util;
	}

	/**
	 * Returns the mutation data for the given gene(s).
	 *
	 * @param geneList  list of hugo gene symbols as a whitespace delimited string
	 * @param callback  callback function to be invoked after retrieval
	 */
	function getMutationData(geneList, callback)
	{
		var genes = geneList.trim().split(/\s+/);
		var genesToQuery = [];

		// get previously grabbed data (if any)
		var mutationData = [];
		var mutationMap = _util.getMutationGeneMap();

		// process each gene in the given list
		_.each(genes, function(gene, idx) {
			gene = gene.toUpperCase();

			var data = mutationMap[gene];

			if (data == undefined ||
			    data.length == 0)
			{
				// mutation data does not exist for this gene, add it to the list
				genesToQuery.push(gene);
			}
			else
			{
				// data is already cached for this gene, update the data array
				mutationData = mutationData.concat(data);
			}
		});

		// all data is already retrieved (full init)
		if (_fullInit)
		{
			// just forward the call the callback function
			callback(mutationData);
		}
		// we need to retrieve missing data (lazy init)
		else
		{
			var process = function(data) {
				// process new data retrieved from server
				var mutations = new MutationCollection(data);
				_util.processMutationData(mutations);

				// concat new data with already cached data,
				// and forward it to the callback function
				mutationData = mutationData.concat(data);
				callback(mutationData);
			};

			// some (or all) data is missing,
			// send ajax request for missing genes
			if (genesToQuery.length > 0)
			{
				// add genesToQuery to the servlet params
				_servletParams.geneList = genesToQuery.join(" ");

				// retrieve data from the server
				$.post(_servletName, _servletParams, process, "json");
			}
			// data for all requested genes already cached
			else
			{
				// just forward the data to the callback function
				callback(mutationData);
			}
		}
	}

	/**
	 * Checks if there is mutation data for the current query
	 * (For the current gene list, case list, and cancer study).
	 *
	 * @return {boolean} true if there is mutation data, false otherwise.
	 */
	function hasData()
	{
		// TODO returning true in any case for now
		// we need to query server side for lazy init
		// since initially there is definitely no data
		//return (_util.getMutations().length > 0);
		return true;
	}

	return {
		initWithData : fullInit,
		initWithoutData: lazyInit,
		getMutationData: getMutationData,
		getGeneList: getGeneList,
		getUnsortedGeneList: getUnsortedGeneList,
		getMutationUtil: getMutationUtil,
		hasData: hasData
	};
};