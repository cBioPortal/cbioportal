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

	// TODO add column name alternatives?
	// map of <mutation model field name, input header name> pairs
	var _headerMap = {
		"proteinPosEnd": "protein_position_end",
		"uniprotId": "uniprot_id",
		"cancerType": "cancer_type",
		"tumorType": "tumor_type",
		"cancerStudyLink": "cancer_study_link",
		"codonChange": "codon_change",
		"proteinPosStart": "protein_position_start",
		"linkToPatientView": "patient_view_link",
		"geneticProfileId": "genetic_profile_id",
		"mutationCount": "mutation_count",
		"mutationType": "mutation_type", // "variant_classification"
		"referenceAllele": "reference_allele",
		"uniprotAcc": "uniprot_accession",
		"fisValue": "fis_value",
		"functionalImpactScore": "fis",
		"cancerStudy": "cancer_study",
		"normalRefCount": "normal_ref_count",
		"ncbiBuildNo": "ncbi_build",
		"normalFreq": "normal_frequency",
		"cancerStudyShort": "cancer_study_short",
		"msaLink": "msa_link",
		"mutationStatus": "mutation_status",
		"cna": "copy_number",
		"proteinChange": "protein_change",
		"endPos": "end_position",
		//"refseqMrnaId": "",
		"geneSymbol": "hugo_symbol",
		"tumorFreq": "tumor_frequency",
		"startPos": "start_position",
		"keyword": "keyword",
		"cosmic": "cosmic",
		"validationStatus": "validation_status",
		"mutationSid": "mutation_sid",
		//"canonicalTranscript": "",
		"normalAltCount": "normal_alt_count",
		"variantAllele": "variant_allele",
		//"mutationEventId": "",
		"mutationId": "mutation_id",
		"caseId": "sample_id", // "tumor_sample_barcode"
		"xVarLink": "xvar_link",
		"pdbLink": "pdb_link",
		"tumorAltCount": "tumor_alt_count",
		"tumorRefCount": "tumor_ref_count",
		"sequencingCenter": "center",
		"chr": "chromosome"
	};

	/**
	 * Initializes a default mutation object where all data fields are empty strings.
	 *
	 * @returns {Object}    a default "empty" mutation object
	 */
	function initMutation()
	{
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

	/**
	 * Parses the entire input data and creates an array of mutation objects.
	 *
	 * @param input     input string/file.
	 * @returns {Array} an array of mutation objects.
	 */
	function parseInput(input)
	{
		var mutationData = [];

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

	/**
	 * Parses a single line of the input and returns a new mutation object.
	 *
	 * @param line      single line of the input data
	 * @param indexMap  map of <header name, index> pairs
	 * @returns {Object}    a mutation object
	 */
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

		mutation.mutationId = mutation.mutationId || nextId();

		// TODO mutationSid?
		mutation.mutationSid = mutation.mutationSid || mutation.mutationId;

		return mutation;
	}

	/**
	 * Parses the value of a single input cell.
	 *
	 * @param field     name of the mutation model field
	 * @param values    array of values for a single input line
	 * @param indexMap  map of <header name, index> pairs
	 * @returns {string}    data value for the given field name.
	 */
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

	/**
	 * Builds a map of <header name, index> pairs, to use header names
	 * instead of index constants.
	 *
	 * @param header    header line (first line) of the input
	 * @returns map of <header name, index> pairs
	 */
	function buildIndexMap(header)
	{
		var columns = header.split("\t");
		var map = {};

		_.each(columns, function(column, index) {
			map[column.toLowerCase()] = index;
		});

		return map;
	}

	/**
	 * Processes the input data and creates a list of sample (case) ids.
	 *
	 * @returns {Array} an array of sample ids
	 */
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
