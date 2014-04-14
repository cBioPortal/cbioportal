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

		// TODO this is a sample data parse the actual input string...
		var sample = {
			//"proteinPosEnd": 390,
			//"uniprotId": "ANDR_HUMAN",
			//"cancerType": "Prostate Adenocarcinoma",
			//"tumorType": null,
			//"cancerStudyLink": "study.do?cancer_study_id=prad_su2c",
			//"codonChange": "c.(1162-1170)AAGCTGGAGdel",
			//"proteinPosStart": 388,
			//"linkToPatientView": "case.do?cancer_study_id=prad_su2c&case_id=MO_1084-Tumor",
			//"geneticProfileId": "prad_su2c_mutations",
			//"mutationCount": 62,
			//"mutationType": "In_Frame_Del",
			//"specialGeneData": null,
			//"referenceAllele": "AAGCTGGAG",
			//"uniprotAcc": "P10275",
			//"fisValue": null,
			//"cancerStudy": "Prostate Adenocarcinoma, Metastatic (SU2C)",
			//"normalRefCount": null,
			//"ncbiBuildNo": "hg19",
			//"normalFreq": null,
			//"cancerStudyShort": "Prostate (SU2C)",
			//"msaLink": "",
			//"mutationStatus": "Somatic",
			//"cna": "-1",
			"proteinChange": "KLE388del",
			"endPos": 66766158,
			//"refseqMrnaId": "NM_000044",
			"geneSymbol": "AR",
			//"tumorFreq": 0.8304597701149425,
			"startPos": 66766150,
			//"keyword": "AR 388-390 deletion",
			//"cosmic": null,
			//"validationStatus": "---",
			"mutationSid": "ARMO_1084-TumorKLE388del",
			"canonicalTranscript": true,
			//"normalAltCount": null,
			//"variantAllele": "-",
			//"mutationEventId": 823263,
			"mutationId": "m-989793671",
			//"caseId": "MO_1084-Tumor",
			//"xVarLink": "",
			//"pdbLink": "",
			//"tumorAltCount": 289,
			//"tumorRefCount": 59,
			//"sequencingCenter": "Broad",
			"chr": "chr23"
			//"igvLink": "igvlinking.json?cancer_study_id=prad_su2c&case_id=MO_1084-Tumor&locus=chr23%3A66766150-66766158"
		};

		var mutation = initMutation();
		mutation = jQuery.extend(true, {}, mutation, sample);
		mutationData.push(mutation);

		_data = mutationData;

		return mutationData;
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

	return {
		parseInput: parseInput,
		getSampleArray: getSampleArray,
		getGeneList: getGeneList
	};
}
