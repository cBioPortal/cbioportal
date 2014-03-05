/**
 * This class is designed to retrieve PDB data on demand.
 *
 * @param mutationUtil  an instance of MutationDetailsUtil class
 *
 * @author Selcuk Onur Sumer
 */
var PdbDataProxy = function(mutationUtil)
{
	// TODO get servlet name as a param?
	var _servletName = "get3dPdb.json";

	var _util = mutationUtil;

	// cache for PDB data:

	// map of <uniprot id, PdbCollection> pairs
	var _pdbDataCache = {};

	// map of <uniprot id, PdbChain[][]> pairs
	var _pdbRowDataCache = {};

	// map of <pdb id, pdb info> pairs
	var _pdbInfoCache = {};

	// map of <uniprot id, pdb data summary> pairs
	var _pdbDataSummaryCache = {};

	/**
	 * Retrieves the position map for the given gene and chain.
	 * Invokes the given callback function after retrieving the data.
	 *
	 * @param gene          hugo gene symbol
	 * @param chain         a PdbChainModel instance
	 * @param callbackFn    function to be invoked after data retrieval
	 */
	function getPositionMap(gene, chain, callbackFn)
	{
		// collection of alignments (PdbAlignmentCollection)
		var alignments = chain.alignments;

		// TODO use a proper cache instead of checking/reflecting a chain attribute?
		// do not retrieve data if it is already there
		if (chain.positionMap != undefined)
		{
			callbackFn(chain.positionMap);
			return;
		}

		// get protein positions for current mutations
		var positions = _util.getProteinPositions(gene);

		// populate position data array
		// first create as an object (map),
		// then convert to an array to avoid duplicate positions
		var positionObj = {};

		// only add positions which fall between chain start & end positions
		_.each(positions, function(ele, i) {
			if (ele.start > -1 &&
			    ele.start >= chain.mergedAlignment.uniprotFrom &&
			    ele.start <= chain.mergedAlignment.uniprotTo)
			{
				positionObj[ele.start] = ele.start;
			}

			if (ele.end > ele.start &&
			    ele.end >= chain.mergedAlignment.uniprotFrom &&
			    ele.end <= chain.mergedAlignment.uniprotTo)
			{
				positionObj[ele.end] = ele.end;
			}
		});

		// convert object to array
		var positionData = [];

		for (var key in positionObj)
		{
			positionData.push(positionObj[key]);
		}

		// populate alignment data array
		var alignmentData = [];

		alignments.each(function(ele, i) {
			alignmentData.push(ele.alignmentId);
		});

		// callback function for the AJAX call
		var processData = function(data) {
			var positionMap = {};
			var mutations = _util.getMutationGeneMap()[gene];

			if (data.positionMap != null)
			{
				// re-map mutation ids with positions by using the raw position map
				for(var i=0; i < mutations.length; i++)
				{
					var start = data.positionMap[mutations[i].getProteinStartPos()];
					var end = start;

					var type = mutations[i].mutationType;

					// ignore end position for mutation other than in frame del
					if (type != null &&
						type.toLowerCase() === "in_frame_del")
					{
						end = data.positionMap[mutations[i].proteinPosEnd] || end;
					}

					// if no start and end position found for this mutation,
					// then it means this mutation position is not in this chain
					if (start != null &&
					    end != null)
					{
						positionMap[mutations[i].mutationId] =
							{start: start, end: end};
					}
				}
			}

			// call the callback function with the updated position map
			callbackFn(positionMap);
		};

		// check if there are positions to map
		if (positionData.length > 0)
		{
			// get pdb data for the current mutations
			$.getJSON(_servletName,
		          {positions: positionData.join(" "),
			          alignments: alignmentData.join(" ")},
		          processData);
		}
		// no position data: no need to query the server
		else
		{
			// just forward to callback with empty data
			callbackFn({});
		}
	}

	/**
	 * Retrieves the PDB data for the provided uniprot id. Passes
	 * the retrieved data as a parameter to the given callback function
	 * assuming that the callback function accepts a single parameter.
	 *
	 * @param uniprotId     uniprot id
	 * @param callback      callback function to be invoked
	 */
	function getPdbData(uniprotId, callback)
	{
		// retrieve data from the server if not cached
		if (_pdbDataCache[uniprotId] == undefined)
		{
			// process & cache the raw data
			var processData = function(data) {
				var pdbColl = PdbDataUtil.processPdbData(data);
				_pdbDataCache[uniprotId] = pdbColl;

				// forward the processed data to the provided callback function
				callback(pdbColl);
			};

			// retrieve data from the servlet
			$.getJSON(_servletName,
					{uniprotId: uniprotId},
					processData);
		}
		else
		{
			// data is already cached, just forward it
			callback(_pdbDataCache[uniprotId]);
		}
	}

	/**
	 * Retrieves the PDB data for the provided uniprot id, and creates
	 * a 2D-array of pdb chains ranked by length and other criteria.
	 *
	 * Forwards the processed data to the given callback function
	 * assuming that the callback function accepts a single parameter.
	 *
	 * @param uniprotId     uniprot id
	 * @param callback      callback function to be invoked
	 */
	function getPdbRowData(uniprotId, callback)
	{
		// retrieve data if not cached yet
		if (_pdbRowDataCache[uniprotId] == undefined)
		{
			getPdbData(uniprotId, function(pdbColl) {
				// get the data & cache
				var rowData = PdbDataUtil.allocateChainRows(pdbColl);
				_pdbRowDataCache[uniprotId] = rowData;

				// forward to the callback
				callback(rowData);
			});
		}
		else
		{
			// data is already cached, just forward it
			callback(_pdbRowDataCache[uniprotId]);
		}
	}

	/**
	 * Retrieves the PDB data summary for the provided uniprot id. Passes
	 * the retrieved data as a parameter to the given callback function
	 * assuming that the callback function accepts a single parameter.
	 *
	 * @param uniprotId     uniprot id
	 * @param callback      callback function to be invoked
	 */
	function getPdbDataSummary(uniprotId, callback)
	{
		// retrieve data from the server if not cached
		if (_pdbDataSummaryCache[uniprotId] == undefined)
		{
			// process & cache the raw data
			var processData = function(data) {
				_pdbDataSummaryCache[uniprotId] = data;

				// forward the processed data to the provided callback function
				callback(data);
			};

			// retrieve data from the servlet
			$.getJSON(_servletName,
					{uniprotId: uniprotId, type: "summary"},
					processData);
		}
		else
		{
			// data is already cached, just forward it
			callback(_pdbDataSummaryCache[uniprotId]);
		}
	}

	/**
	 * Checks if there is structure (PDB) data available for the provided
	 * uniprot id. Passes a boolean parameter to the given callback function
	 * assuming that the callback function accepts a single parameter.
	 *
	 * @param uniprotId     uniprot id
	 * @param callback      callback function to be invoked
	 */
	function hasPdbData(uniprotId, callback)
	{
		var processData = function(data) {
			var hasData = data && (data.alignmentCount > 0);
			callback(hasData);
		};

		getPdbDataSummary(uniprotId, processData);
	}

	/**
	 * Retrieves the PDB information for the provided PDB id(s). Passes
	 * the retrieved data as a parameter to the given callback function
	 * assuming that the callback function accepts a single parameter.
	 *
	 * @param pdbIdList list of PDB ids as a white-space delimited string
	 * @param callback  callback function to be invoked
	 */
	function getPdbInfo(pdbIdList, callback)
	{
		var pdbIds = pdbIdList.trim().split(/\s+/);
		var pdbToQuery = [];

		// get previously grabbed data (if any)

		var pdbData = {};

		// process each pdb id in the given list
		_.each(pdbIds, function(pdbId, idx) {
			//pdbId = pdbId.toLowerCase();

			var data = _pdbInfoCache[pdbId];

			if (data == undefined ||
			    data.length == 0)
			{
				// data does not exist for this pdb, add it to the list
				pdbToQuery.push(pdbId);
			}
			else
			{
				// data is already cached for this pdb id, update the data object
				pdbData[pdbId] = data;
			}
		});

		var servletParams = {};

		// some (or all) data is missing,
		// send ajax request for missing ids
		if (pdbToQuery.length > 0)
		{
			// process & cache the raw data
			var processData = function(data) {

				_.each(pdbIds, function(pdbId, idx) {
					if (data[pdbId] != null)
					{
						_pdbInfoCache[pdbId] = data[pdbId];

						// concat new data with already cached data
						pdbData[pdbId] = data[pdbId];
					}
				});

				// forward the final data to the callback function
				callback(pdbData);
			};

			// add pdbToQuery to the servlet params
			servletParams.pdbIds = pdbToQuery.join(" ");

			// retrieve data from the server
			$.post(_servletName, servletParams, processData, "json");
			//$.getJSON(_servletName, servletParams, processData, "json");
		}
		// data for all requested chains already cached
		else
		{
			// just forward the data to the callback function
			callback(pdbData);
		}
	}

	return {
		hasPdbData: hasPdbData,
		getPdbData: getPdbData,
		getPdbRowData: getPdbRowData,
		getPdbInfo: getPdbInfo,
		getPositionMap: getPositionMap
	};
};
