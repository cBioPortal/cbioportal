/**
 * Singleton utility class for pileup related tasks.
 *
 * @author Selcuk Onur Sumer
 */
var PileupUtil = (function()
{
	var _idCounter = 0;

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
		var mutationTypeMap = MutationViewsUtil.getVisualStyleMaps().mutationType;

		var typeMap = generateTypeMap(pileup);
		var groupArray = [];
		var groupCountMap = {};

		// group mutation types by using the type map
		// and count number of mutations in a group

		for (var type in typeMap)
		{
			// grouping mutations by the style (not by the type)
			var group = mutationTypeMap[type].style;

			if (group == undefined)
			{
				group = mutationTypeMap.other.style;
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

	var nextId = function()
	{
		_idCounter++;

		return "pileup_" + _idCounter;
	};

	/**
	 * Creates a map of <mutation sid>, <pileup id> pairs.
	 *
	 * @param pileups   list of pileups
	 * @return {Object} <mutation sid> to <pileup id> map
	 */
	var mapToMutations = function(pileups)
	{
		var map = {};

		// map each mutation sid to its corresponding pileup
		_.each(pileups, function(pileup) {
			_.each(pileup.mutations, function(mutation) {
				map[mutation.mutationSid] = pileup.pileupId;
			})
		});

		return map;
	};

	return {
		nextId: nextId,
		mapToMutations: mapToMutations,
		getMutationTypeMap: generateTypeMap,
		getMutationTypeArray: generateTypeArray,
		getMutationTypeGroups: generateTypeGroupArray
	};
})();