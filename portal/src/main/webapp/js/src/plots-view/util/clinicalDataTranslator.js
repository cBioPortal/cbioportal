var clinicalDataTranslator = (function() {
	
	var txt_num = {
		"DFS_STATUS": {
			"DiseaseFree": "0",
			"Recurred/Progressed": "1"
		},
		"SEQUENCED": {
			"YES": "0",
			"NO": "1"
		},
		"COMPLETE_DATA": {
			"YES": "0",
			"NO": "1"
		},
		"MRNA_DATA": {
			"YES": "0",
			"NO": "1"
		},
		"PRIMARY_THERAPY_OUTCOME_SUCCESS": {
			"YES": "0",
			"NO": "1"
		},
		"TUMOR_RESIDUAL_DISEASE": {
			"YES": "0",
			"NO": "1"
		},
		"OS_STATUS": {
			"YES": "0",
			"NO": "1"
		},
		"TUMOR_STAGE_2009": {
			"YES": "0",
			"NO": "1"
		},		
		"PLATINUM_STATUS": {
			"YES": "0",
			"NO": "1"
		},		
		"GRADE": {
			"YES": "0",
			"NO": "1"
		},
		"ACGH_DATA": {
			"YES": "0",
			"NO": "1"
		}
	};

	function translateText(attrId, attrTxt) {
		return txt_num[attrId][attrTxt];
	}

	function translateNum(attrId, attrNum) {
		for (var attrTxt in txt_num[attrId]) {
			if (txt_num[attrId][attrTxt] === attrNum) {
				return attrTxt;
			}
		}
		return null;
	}

	return {
		translateText: translateText,
		translateNum: translateNum
	}

}()); //closing clinical data translator