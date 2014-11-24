var clinicalDataTranslator = (function() {
	
	var txt_num = {
		"DFS_STATUS": {
			"DiseaseFree": "0",
			"Recurred/Progressed": "1"
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