/**
 * Singleton utility class for pileup related tasks.
 */
var PileupUtil = (function()
{
	/**
	 * Processes a Pileup instance, and creates a map of
	 * <mutation type, mutation array> pairs.
	 *
	 * @param pileup    a pileup instance
	 * @return {object} map of mutations (keyed on mutation type)
	 * @private
	 */
	var generateTypeMap = function(pileup)
	{
		var mutations = pileup.mutations;
		var mutationMap = {};

		// process raw data to group mutations by types
		for (var i=0; i < mutations.length; i++)
		{
			var type = mutations[i].mutationType.toLowerCase();

			if (mutationMap[type] == undefined)
			{
				mutationMap[type] = [];
			}

			mutationMap[type].push(mutations[i]);
		}

		return mutationMap;
	};

	/**
	 * Processes a Pileup instance, and creates an array of
	 * <mutation type, count> pairs. The final array is sorted
	 * by mutation count.
	 *
	 * @param pileup    a pileup instance
	 * @return {Array}  array of mutation type and count pairs
	 */
	var generateTypeArray = function (pileup)
	{
		var map = generateTypeMap(pileup);
		var typeArray = [];

		// convert to array and sort by length (count)
		for (var key in map)
		{
			typeArray.push({type: key, count: map[key].length});
		}

		typeArray.sort(function(a, b) {
			// descending sort
			return b.count - a.count;
		});

		return typeArray;
	};

	/**
	 * Processes a Pileup instance, and creates an array of
	 * <mutation type group, count> pairs. The final array
	 * is sorted by mutation count.
	 *
	 * @param pileup    a pileup instance
	 * @return {Array}  array of mutation type group and count pairs
	 */
	var generateTypeGroupArray = function (pileup)
	{
		// TODO a very similar mapping is also used in the mutation table view
		// ...it might be better to merge these two mappings to avoid duplication
		var typeToGroupMap = {
			missense_mutation: "missense_mutation",
			nonsense_mutation: "trunc_mutation",
			nonstop_mutation: "trunc_mutation",
			frame_shift_del: "trunc_mutation",
			frame_shift_ins: "trunc_mutation",
			in_frame_ins: "inframe_mutation",
			in_frame_del: "inframe_mutation",
			splice_site: "trunc_mutation",
			other: "other_mutation"
		};

		var typeMap = generateTypeMap(pileup);
		var groupArray = [];
		var groupCountMap = {};

		// group mutation types by using the type map
		// and count number of mutations in a group

		for (var type in typeMap)
		{
			var group = typeToGroupMap[type];

			if (group == undefined)
			{
				group = typeToGroupMap.other;
			}

			if (groupCountMap[group] == undefined)
			{
				// init count
				groupCountMap[group] = 0;
			}

			groupCountMap[group]++;
		}

		// convert to array and sort by length (count)

		for (var group in groupCountMap)
		{
			groupArray.push({group: group, count: groupCountMap[group]});
		}

		groupArray.sort(function(a, b) {
			// descending sort
			return b.count - a.count;
		});

		return groupArray;
	};

	return {
		getMutationTypeMap: generateTypeMap,
		getMutationTypeArray: generateTypeArray,
		getMutationTypeGroups: generateTypeGroupArray
	};
})();