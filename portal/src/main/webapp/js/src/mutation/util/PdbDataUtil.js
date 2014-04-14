/**
 * Singleton utility class for PDB data related tasks.
 *
 * @author Selcuk Onur Sumer
 */
var PdbDataUtil = (function()
{
	// constants
	var ALIGNMENT_GAP = "*";
	var ALIGNMENT_PLUS = "+";
	var ALIGNMENT_MINUS = "-";
	var ALIGNMENT_SPACE = " ";

	/**
	 * Processes the pdb data (received from the server) to create
	 * a collection of PdbModel instances.
	 *
	 * @param data  pdb alignment data with a position map
	 * @return {PdbCollection}   PdbModel instances representing the processed data
	 */
	function processPdbData(data)
	{
		var alignmentModel = null;
		var pdbList = [];
		var pdbMap = {};

		_.each(data, function(alignment, idx) {
			alignmentModel = new PdbAlignmentModel(alignment);

			if (pdbMap[alignmentModel.pdbId] == undefined)
			{
				pdbMap[alignmentModel.pdbId] = {};
			}

			if (pdbMap[alignmentModel.pdbId][alignmentModel.chain] == undefined)
			{
				pdbMap[alignmentModel.pdbId][alignmentModel.chain] = [];
			}

			pdbMap[alignmentModel.pdbId][alignmentModel.chain].push(alignmentModel);
		});

		// instantiate chain models
		for (var pdbId in pdbMap)
		{
			var chains = [];

			for (var chain in pdbMap[pdbId])
			{
				var chainModel = new PdbChainModel({chainId: chain,
					alignments: pdbMap[pdbId][chain]});

				chains.push(chainModel);
			}

			var pdbModel = new PdbModel({pdbId: pdbId,
				chains: chains});

			pdbList.push(pdbModel);
		}

		// return new pdb model
		return new PdbCollection(pdbList);
	}

	/**
	 * Generates a pdb info summary for the given full pdb info object
	 * and the chain id.
	 *
	 * @param pdbInfo   pdb info data (retrieved from server)
	 * @param chainId   chain id as a string
	 * @returns {Object} pdb summary for the given chain
	 */
	function generatePdbInfoSummary(pdbInfo, chainId)
	{
		var summary = {};
		summary.title = pdbInfo.title;

		// TODO cache?

		// get chain specific molecule info
		for (var key in pdbInfo.compound)
		{
			var mol = pdbInfo.compound[key];

			if (mol.molecule &&
			    _.indexOf(mol.chain, chainId.toLowerCase()) != -1)
			{
				// chain is associated with this mol,
				// get the organism info from the source

				summary.molecule = mol.molecule;
				break;
			}
		}

		return summary;
	}

	/**
	 * Finds the organism for the given full pdb info object
	 * and the chain id.
	 *
	 * @param pdbInfo   pdb info data (retrieved from server)
	 * @param chainId   chain id as a string
	 * @returns {String} organism data corresponding to the given chain
	 */
	function getOrganism(pdbInfo, chainId)
	{
		var organism = "NA";

		// TODO cache?
		for (var key in pdbInfo.compound)
		{
			var mol = pdbInfo.compound[key];

			if (_.indexOf(mol.chain, chainId.toLowerCase()) != -1 &&
			    pdbInfo.source[mol.mol_id] != null)
			{
				// chain is associated with this mol,
				// get the organism info from the source
				organism = pdbInfo.source[mol.mol_id].organism_scientific ||
				           organism;

				break;
			}
		}

		return organism;
	}

	/**
	 * Merge alignments in the given array.
	 *
	 * @param alignments    an array of PdbAlignmentModel instances
	 */
	function mergeAlignments(alignments)
	{
		// TODO merge without assuming it is sorted (write a new algorithm)
		return mergeSortedAlignments(alignments);
	}

	/**
	 * Merge alignments in the given array, assuming that
	 * they are sorted by uniprotFrom field.
	 *
	 * @param alignments    an array of PdbAlignmentModel instances
	 * @return {Object}     merged alignment object
	 */
	function mergeSortedAlignments(alignments)
	{
		var mergedAlignment = {mergedString: "", uniprotFrom: -1, uniprotTo: -1, pdbFrom: -1};
		var mergedStr = "";
		var end = -1;
		var prev;

		if (alignments.length > 0)
		{
			mergedStr += alignments[0].alignmentString;
			end = alignments[0].uniprotTo;
			prev = alignments[0];
		}
		else
		{
			return mergedAlignment;
		}

		_.each(alignments, function(alignment, idx) {
			var distance = alignment.uniprotFrom - end - 1;

			var str = alignment.alignmentString;

			// check for overlapping uniprot positions...

			// no overlap, and the next alignment starts exactly after the current merge
			if (distance == 0)
			{
				// just concatenate two strings
				mergedStr += str;
			}
			// no overlap, but there is a gap
			else if (distance > 0)
			{
				var gap = [];

				// add gap characters (character count = distance)
				for (var i=0; i<distance; i++)
				{
					gap.push(ALIGNMENT_GAP);
				}

				// also add the actual string
				gap.push(str);

				mergedStr += gap.join("");

			}
			// overlapping
			else
			{
				var overlap = [];
				var subLength = Math.min(-1 * distance, str.length);

				overlap.push(mergedStr.substr(mergedStr.length + distance, subLength));
				overlap.push(str.substr(0, subLength));

				if (overlap[0] != overlap[1])
				{
					console.log("[warning] alignment mismatch: " +
					            prev.alignmentId + " & " + alignment.alignmentId);
					console.log(overlap[0]);
					console.log(overlap[1]);
				}

				// merge two strings
				mergedStr += str.substr(-1 * distance);
			}

			// update the end position
			end = Math.max(end, alignment.uniprotTo);

			if (end == alignment.uniprotTo)
			{
				// keep reference to the previous alignment
				prev = alignment;
			}
		});

		mergedAlignment.uniprotFrom = alignments[0].uniprotFrom;
		mergedAlignment.uniprotTo = mergedAlignment.uniprotFrom + mergedStr.length;
		mergedAlignment.pdbFrom = alignments[0].pdbFrom;
		mergedAlignment.mergedString = mergedStr;
		mergedAlignment.identityPerc = calcIdentityPerc(mergedStr);
		mergedAlignment.identity = calcIdentity(mergedStr);

		return mergedAlignment;
	}

	/**
	 * Finds the first matching pdb id & chain for the given mutation and
	 * row of chains.
	 *
	 * @param mutation  a MutationModel instance
	 * @param rowData   ranked chain data (2D array)
	 * @return {Object} {pdbId, chainId}
	 */
	function mutationToPdb(mutation, rowData)
	{
		var pdbMatch = null;

		var location = mutation.getProteinStartPos();
		var type = mutation.mutationType.trim().toLowerCase();

		// skip fusions or invalid locations
		if (location == null ||
		    type == "fusion")
		{
			return pdbMatch;
		}

		// iterate all chains to find the first matching position
		for (var i=0;
		     i < rowData.length && !pdbMatch;
		     i++)
		{
			var allocation = rowData[i];

			for (var j=0;
			     j < allocation.length && !pdbMatch;
			     j++)
			{
				var datum = allocation[j];

				var alignment = datum.chain.mergedAlignment;

				// use merged alignment to see if there is a match
				var rangeWithin = location >= alignment.uniprotFrom &&
				                  location <= alignment.uniprotTo;

				// check for match condition
				if (rangeWithin && alignmentMatch(alignment, location))
				{
					pdbMatch = {pdbId: datum.pdbId,
						chainId: datum.chain.chainId};

					// found a matching pdb residue, break the inner loop
					break;
				}
			}

			if (pdbMatch)
			{
				// found a matching pdb residue, break the outer loop
				break;
			}
		}

		return pdbMatch;
	}

	/**
	 * Checks for a match for the specified location on the
	 * given merged alignment.
	 *
	 * @param alignment merged alignment
	 * @param location  protein change location
	 * @return {boolean}    true if match, false otherwise
	 */
	function alignmentMatch(alignment, location)
	{
		var index = location - alignment.uniprotFrom;

		var symbol = alignment.mergedString[index];

		var mismatch = (symbol == ALIGNMENT_GAP);

		return !mismatch;
	}

	/**
	 * Calculates the identity percentage of the given alignment string
	 * based on mismatch ratio.
	 *
	 * @param mergedStr merged alignment string
	 * @return {Number} identity percentage value
	 */
	function calcIdentityPerc(mergedStr)
	{
		var gap = 0;
		var mismatch = 0;

		for (var count=0; count < mergedStr.length; count++)
		{
			var symbol = mergedStr[count];
			if (symbol == ALIGNMENT_GAP)
			{
				// increment gap count (gaps excluded from ratio calculation)
				gap++;
			}
			else if (symbol == ALIGNMENT_MINUS ||
				symbol == ALIGNMENT_PLUS ||
				symbol == ALIGNMENT_SPACE)
			{
				// any special symbol other than a gap is considered as a mismatch
				// TODO is it better to assign a different weight for each symbol?
				mismatch++;
			}
		}

		return 1.0 - (mismatch / (count - gap));
	}

	/**
	 * Calculates the identity (number of matches) for
	 * the given alignment string.
	 *
	 * @param mergedStr merged alignment string
	 * @return {Number} identity value
	 */
	function calcIdentity(mergedStr)
	{
		mergedStr = mergedStr.toLowerCase();

		var match = 0;

		for (var count=0; count < mergedStr.length; count++)
		{
			var symbol = mergedStr[count];

			if (symbol.match(/[a-z]/))
			{
				match++;
			}
		}

		return match;
	}

	/**
	 * Creates row data by allocating position for each chain.
	 * A row may have multiple chains if there is no overlap
	 * between chains.
	 *
	 * @param pdbColl   a PdbCollection instance
	 * @return {Array}  a 2D array of chain allocation
	 */
	function allocateChainRows(pdbColl)
	{
		// sort chains by rank (high to low)
		var chainData = sortChainsDesc(pdbColl);

		var rows = [];

		_.each(chainData, function(datum, idx) {
			var chain = datum.chain;

			if (chain.alignments.length > 0)
			{
				var inserted = false;

				// find the first available row for this chain
				for (var i=0; i < rows.length; i++)
				{
					var row = rows[i];
					var conflict = false;

					// check for conflict for this row
					for (var j=0; j < row.length; j++)
					{
						if (overlaps(chain, row[j].chain))
						{
							// set the flag, and break the loop
							conflict = true;
							break;
						}
					}

					// if there is space available in this row,
					// insert the chain into the current row
					if (!conflict)
					{
						// insert the chain, set the flag, and break the loop
						row.push(datum);
						inserted = true;
						break;
					}
				}

				// if there is no available space in any row,
				// then insert the chain to the next row
				if (!inserted)
				{
					var newAllocation = [];
					newAllocation.push(datum);
					rows.push(newAllocation);
				}
			}
		});

		// sort alignments in each row by start position (lowest comes first)
//		_.each(rows, function(allocation, idx) {
//			allocation.sort(function(a, b){
//				return (a.chain.mergedAlignment.uniprotFrom -
//				        b.chain.mergedAlignment.uniprotFrom);
//			});
//		});

		// sort alignments in the first row by alignment length
		if (rows.length > 0)
		{
			rows[0].sort(function(a, b){
				return (b.chain.mergedAlignment.mergedString.length -
				        a.chain.mergedAlignment.mergedString.length);
			});
		}

		return rows;
	}

	/**
	 * Checks if the given two chain alignments (positions) overlaps
	 * with each other.
	 *
	 * @param chain1    first chain
	 * @param chain2    second chain
	 * @return {boolean}    true if intersects, false if distinct
	 */
	function overlaps(chain1, chain2)
	{
		var overlap = true;

		if (chain1.mergedAlignment.uniprotFrom >= chain2.mergedAlignment.uniprotTo ||
		    chain2.mergedAlignment.uniprotFrom >= chain1.mergedAlignment.uniprotTo)
		{
			// no conflict
			overlap = false;
		}

		return overlap;
	}

	/**
	 * Creates a sorted array of chain datum (a {pdbId, PdbChainModel} pair).
	 * The highest ranked chain will be the first element of the returned
	 * data array.
	 *
	 * @param pdbColl   a PdbCollection instance
	 * @return {Array}  an array of <pdb id, PdbChainModel> pairs
	 */
	function sortChainsDesc(pdbColl)
	{
		var chains = [];

		// put all chains in a single array
		pdbColl.each(function(pdb, idx) {
			// create rectangle(s) for each chain
			pdb.chains.each(function(chain, idx) {
				var datum = {pdbId: pdb.pdbId, chain: chain};
				chains.push(datum);
			});
		});

		// rank the chains
		sortChains(chains, [
			compareIdentity, // first, sort by identity
			compareMergedLength, // then by length
			compareIdentityPerc, // then by identity percentage
			comparePdbId, // then by pdb id (A-Z)
			compareChainId // then by chain id (A-Z)
		]);

		return chains;
	}

	/**
	 * Sort chains wrt the given comparator functions.
	 *
	 * @param chains        an array of PDB chain data
	 * @param comparators   an array of comparator functions
	 */
	function sortChains(chains, comparators)
	{
		// compare using given comparator functions
		chains.sort(function(a, b) {
			var result = 0;

			// continue to compare until the result is different than zero
			for (var i=0;
			     i < comparators.length && result == 0;
			     i++)
			{
				var fn = comparators[i];
				result = fn(a, b);
			}

			return result;
		});
	}

	function chainKey(pdbId, chainId)
	{
		return pdbId + ":" + chainId;
	}

	function compareIdentity(a, b)
	{
		// higher value should comes first
		return (b.chain.mergedAlignment.identity -
		        a.chain.mergedAlignment.identity);
	}

	function compareIdentityPerc(a, b)
	{
		// higher value should comes first
		return (b.chain.mergedAlignment.identityPerc -
		        a.chain.mergedAlignment.identityPerc);
	}

	function compareMergedLength(a, b)
	{
		// longer string should comes first in the sorted array
		return (b.chain.mergedAlignment.mergedString.length -
		        a.chain.mergedAlignment.mergedString.length);
	}

	function comparePdbId(a, b)
	{
		// A-Z sort
		if (b.pdbId > a.pdbId) {
			return -1;
		} else if (b.pdbId < a.pdbId) {
			return 1;
		} else {
			return 0;
		}

		//return (a.pdbId - b.pdbId);
	}

	function compareChainId(a, b)
	{
		// A-Z sort
		if (b.chain.chainId > a.chain.chainId) {
			return -1;
		} else if (b.chain.chainId < a.chain.chainId) {
			return 1;
		} else {
			return 0;
		}

		//return (a.chain.chainId - b.chain.chainId);
	}

	function compareEValue(a, b)
	{
		// lower e value should comes first in the sorted array
		return (getMinValue(a.chain.alignments, "eValue") -
		        getMinValue(b.chain.alignments, "eValue"));
	}

	function compareIdentP(a, b)
	{
		// higher percentage should comes first in the sorted array
		return (getMinValue(b.chain.alignments, "identityPerc") -
		        getMinValue(a.chain.alignments, "identityPerc"));
	}

	/**
	 * Calculates total number of chains for the given PDB data.
	 *
	 * @param data      PDB data (collection of PdbModel instances)
	 * @return {number} total number of chains
	 */
	function calcChainCount(data)
	{
		var chainCount = 0;

		data.each(function(pdb, idx) {
			chainCount += pdb.chains.length;
		});

		return chainCount;
	}

	function getMinValue(alignments, field)
	{
		var min = Infinity;

		alignments.each(function(ele, idx) {
			if (ele[field] < min)
			{
				min = ele[field];
			}
		});

		return min;
	}

	function getMaxValue(alignments, field)
	{
		var max = -Infinity;

		alignments.each(function(ele, idx) {
			if (ele[field] > max)
			{
				max = ele[field];
			}
		});

		return max;
	}

	return {
		// public constants
		ALIGNMENT_GAP: ALIGNMENT_GAP,
		ALIGNMENT_PLUS: ALIGNMENT_PLUS,
		ALIGNMENT_MINUS: ALIGNMENT_MINUS,
		ALIGNMENT_SPACE: ALIGNMENT_SPACE,
		// public functions
		processPdbData: processPdbData,
		mutationToPdb: mutationToPdb,
		allocateChainRows: allocateChainRows,
		mergeAlignments: mergeAlignments,
		generatePdbInfoSummary: generatePdbInfoSummary,
		getOrganism: getOrganism,
		chainKey: chainKey
	};
})();