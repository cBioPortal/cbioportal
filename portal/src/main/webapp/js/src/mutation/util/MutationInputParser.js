/**
 * Utility class to parse the custom mutation input data.
 *
 * @author Selcuk Onur Sumer
 */
function MutationInputParser ()
{
	var _data = null;
	var _geneList = null;
	var _sampleList = null;
	var _idCounter = 0;

	// TODO add missing values
	// map of mutation model field names to input header names
	var _headerMap = {
		"proteinPosEnd": "",
		"uniprotId": "",
		"cancerType": "",
		"tumorType": "",
		"cancerStudyLink": "",
		"codonChange": "",
		"proteinPosStart": "",
		"linkToPatientView": "",
		"geneticProfileId": "",
		"mutationCount": "",
		"mutationType": "mutation_type",
		"referenceAllele": "",
		"uniprotAcc": "",
		"fisValue": "",
		"functionalImpactScore": "",
		"cancerStudy": "",
		"normalRefCount": "",
		"ncbiBuildNo": "",
		"normalFreq": "",
		"cancerStudyShort": "",
		"msaLink": "",
		"mutationStatus": "",
		"cna": "",
		"proteinChange": "protein_change",
		"endPos": "end_position",
		"refseqMrnaId": "",
		"geneSymbol": "hugo_symbol",
		"tumorFreq": "",
		"startPos": "start_position",
		"keyword": "",
		"cosmic": "",
		"validationStatus": "",
		"mutationSid": "",
		//"canonicalTranscript": "",
		"normalAltCount": "",
		"variantAllele": "",
		//"mutationEventId": "",
		"mutationId": "",
		"caseId": "sample_id",
		"xVarLink": "",
		"pdbLink": "",
		"tumorAltCount": "",
		"tumorRefCount": "",
		"sequencingCenter": "",
		"chr": "chromosome"
	};

	function initMutation()
	{
		// return a default mutation object with all data fields are
		// initialized as empty strings...
		return {
			"proteinPosEnd": "",
			"uniprotId": "",
			"cancerType": "",
			"tumorType": "",
			"cancerStudyLink": "",
			"codonChange": "",
			"proteinPosStart": "",
			"linkToPatientView": "",
			"geneticProfileId": "",
			"mutationCount": "",
			"mutationType": "",
			"referenceAllele": "",
			"uniprotAcc": "",
			"fisValue": "",
			"functionalImpactScore": "",
			"cancerStudy": "",
			"normalRefCount": "",
			"ncbiBuildNo": "",
			"normalFreq": "",
			"cancerStudyShort": "",
			"msaLink": "",
			"mutationStatus": "",
			"cna": "",
			"proteinChange": "",
			"endPos": "",
			"refseqMrnaId": "",
			"geneSymbol": "",
			"tumorFreq": "",
			"startPos": "",
			"keyword": "",
			"cosmic": "",
			"validationStatus": "",
			"mutationSid": "",
			//"canonicalTranscript": "",
			"normalAltCount": "",
			"variantAllele": "",
			//"mutationEventId": "",
			"mutationId": "",
			"caseId": "",
			"xVarLink": "",
			"pdbLink": "",
			"tumorAltCount": "",
			"tumorRefCount": "",
			"sequencingCenter": "",
			"chr": ""
		};
	}

	function parseInput(input)
	{
		var mutationData = [];

		console.log(input);

		var lines = input.split("\n");

		if (lines.length > 0)
		{
			// assuming first line is a header
			var indexMap = buildIndexMap(lines[0]);

			// rest should be data
			for (var i=1; i < lines.length; i++)
			{
				// skip empty lines
				if (lines[i].length > 0)
				{
					mutationData.push(parseLine(lines[i], indexMap));
				}
			}
		}

		_data = mutationData;

		return mutationData;
	}

	function parseLine(line, indexMap)
	{
		// init mutation fields
		var mutation = initMutation();

		// assuming values are separated by tabs
		var values = line.split("\t");

		// find the corresponding column for each field, and set the value
		_.each(_.keys(mutation), function(key) {
			mutation[key] = parseValue(key, values, indexMap);
		});

		mutation.mutationId = nextId();

		// TODO mutationSid?
		mutation.mutationSid = mutation.mutationId;

		// TODO this is to prevent invalid case id links to be removed
		// we should override the case id render function for the table...
		mutation.linkToPatientView = "#";

		//mutation = jQuery.extend(true, {}, initMutation(), mutation);
		return mutation;
	}

	function parseValue(field, values, indexMap)
	{
		// get the column name for the given field name
		var column = _headerMap[field];
		var index = indexMap[column];
		var value = "";

		if (index != null)
		{
			value = values[index] || "";
		}

		return value;
	}

	function buildIndexMap(header)
	{
		var columns = header.split("\t");
		var map = {};

		_.each(columns, function(column, index) {
			map[column.toLowerCase()] = index;
		});

		return map;
	}

	function getSampleArray()
	{
		if (_data == null)
		{
			return [];
		}

		if (_sampleList == null)
		{
			var sampleSet = {};

			_.each(_data, function(mutation, idx) {
				if (mutation.caseId != null &&
				    mutation.caseId.length > 0)
				{
					sampleSet[mutation.caseId] = mutation.caseId;
				}
			});

			_sampleList = _.values(sampleSet);
		}

		return _sampleList;
	}

	function getGeneList()
	{
		if (_data == null)
		{
			return [];
		}

		if (_geneList == null)
		{
			var geneSet = {};

			_.each(_data, function(mutation, idx) {
				if (mutation.geneSymbol != null &&
				    mutation.geneSymbol.length > 0)
				{
					geneSet[mutation.geneSymbol.toUpperCase()] =
						mutation.geneSymbol.toUpperCase();
				}
			});

			_geneList = _.values(geneSet);
		}

		return _geneList;
	}

	function nextId()
	{
	    _idCounter++;

		return "stalone_mut_" + _idCounter;
	}

	return {
		parseInput: parseInput,
		getSampleArray: getSampleArray,
		getGeneList: getGeneList
	};
}
