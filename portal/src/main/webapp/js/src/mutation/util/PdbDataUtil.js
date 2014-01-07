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
	 */
	function mergeSortedAlignments(alignments)
	{
		var mergedAlignment = {mergedString: "", uniprotFrom: -1, uniprotTo: -1};
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
		mergedAlignment.mergedString = mergedStr;
		mergedAlignment.score = calcScore(mergedStr);

		return mergedAlignment;
	}

	/**
	 * Calculates an alignment score based on mismatch ratio.
	 *
	 * @param mergedStr merged alignment string
	 */
	function calcScore(mergedStr)
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
	 * Creates a sorted array of chain datum (a {pdbId, PdbChainModel} pair).
	 * The highest ranked chain will be the first element of the returned
	 * data array.
	 *
	 * @param pdbColl   a PdbCollection instance
	 * @return {Array}  an array of sorted chain data
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

		chains.sort(compareChains);

		return chains;
	}

	/**
	 * Comparison function for chains.
	 */
	function compareChains(a, b)
	{
		var result = 0;

		// first, try to sort by alignment score
		if (result == 0)
		{
			result = compareScore(a, b);
		}

		// second, try to sort by alignment length (longest string comes first)
		if (result == 0)
		{
			result = compareMergedLength(a, b);
		}

		return result;
	}

	function compareScore(a, b)
	{
		// higher score should comes first
		return (b.chain.mergedAlignment.score -
		        a.chain.mergedAlignment.score);
	}

	function compareMergedLength(a, b)
	{
		// longer string should comes first in the sorted array
		return (b.chain.mergedAlignment.mergedString.length -
		        a.chain.mergedAlignment.mergedString.length);
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
		getSortedChainData: sortChainsDesc,
		mergeAlignments: mergeAlignments
	};
})();