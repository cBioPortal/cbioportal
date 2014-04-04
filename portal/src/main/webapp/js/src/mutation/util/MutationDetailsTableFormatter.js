/**
 * Singleton utility class to format Mutation Details Table View content.
 *
 * @author Selcuk Onur Sumer
 */
var MutationDetailsTableFormatter = (function()
{
	var _visualStyleMaps = MutationViewsUtil.getVisualStyleMaps();

	var _mutationTypeMap = _visualStyleMaps.mutationType;
	var _validationStatusMap = _visualStyleMaps.validationStatus;
	var _mutationStatusMap = _visualStyleMaps.mutationStatus;
	var _omaScoreMap = _visualStyleMaps.omaScore;
	var _cnaMap = _visualStyleMaps.cna;

	// TODO identify similar get functions to avoid code duplication

	function getCNA(value)
	{
		return _getCNA(_cnaMap, value);
	}

	function _getCNA(map, value)
	{
		var style, label, tip;

		if (map[value] != null)
		{
			style = map[value].style;
			label = map[value].label;
			tip = map[value].tooltip;
		}
		else
		{
			style = map.unknown.style;
			label = map.unknown.label;
			tip = map.unknown.tooltip;
		}

		return {style: style, tip: tip, text: label};
	}

    /**
     * Returns the text content, the css class, and the tooltip
     * for the given case id value. If the length of the actual
     * case id string is too long, then creates a short form of
     * the case id ending with an ellipsis.
     *
     * @param caseId    actual case id string
     * @return {{style: string, text: string, tip: string}}
     * @private
     */
	function getCaseId(caseId)
	{
		// TODO customize this length?
		var maxLength = 16;

		var text = caseId;
		var style = ""; // no style for short case id strings
		var tip = caseId; // display full case id as a tip

		// no need to bother with clipping the text for 1 or 2 chars.
		if (caseId.length > maxLength + 2)
		{
			text = caseId.substring(0, maxLength) + "...";
			style = "simple-tip"; // enable tooltip for long strings
		}

		return {style: style, tip: tip, text: text};
	}

	function getMutationType(value)
	{
		return _getMutationType(_mutationTypeMap, value);
	}

    /**
     * Returns the text content and the css class for the given
     * mutation type value.
     *
     * @param map   map of <mutationType, {label, style}>
     * @param value actual string value of the mutation type
     * @return {{style: string, text: string}}
     * @private
     */
	function _getMutationType(map, value)
	{
		var style, text;
		value = value.toLowerCase();

		if (map[value] != null)
		{
			style = map[value].style;
			text = map[value].label;
		}
		else
		{
			style = map.other.style;
			text = value;
		}

		return {style: style, text: text};
	}

	function getMutationStatus(value)
	{
		return _getMutationStatus(_mutationStatusMap, value);
	}

	/**
     * Returns the text content, the css class, and the tooltip
	 * for the given mutation type value.
     *
     * @param map   map of <mutationStatus, {label, style, tooltip}>
     * @param value actual string value of the mutation status
     * @return {{style: string, text: string, tip: string}}
     * @private
     */
	function _getMutationStatus(map, value)
	{
		var style = "simple-tip";
		var text = value;
		var tip = "";
		value = value.toLowerCase();

		if (map[value] != null)
		{
			style = map[value].style;
			text = map[value].label;
			tip = map[value].tooltip;
		}

		return {style: style, tip: tip, text: text};
	}

	function getValidationStatus(value)
	{
		return _getValidationStatus(_validationStatusMap, value);
	}

	/**
	 * Returns the text content, the css class, and the tooltip
	 * for the given validation status value.
	 *
	 * @param map   map of <validationStatus, {label, style, tooltip}>
	 * @param value actual string value of the validation status
	 * @return {{style: string, text: string, tip: string}}
	 * @private
	 */
	function _getValidationStatus(map, value)
	{
		var style, label, tip;
		value = value.toLowerCase();

		if (map[value] != null)
		{
			style = map[value].style;
			label = map[value].label;
			tip = map[value].tooltip;
		}
		else
		{
			style = map.unknown.style;
			label = map.unknown.label;
			tip = map.unknown.tooltip;
		}

		return {style: style, tip: tip, text: label};
	}

	function getFis(fis, fisValue)
	{
		return _getFis(_omaScoreMap, fis, fisValue);
	}

	/**
	 * Returns the text content, the css classes, and the tooltip
	 * for the given string and numerical values of a
	 * functional impact score.
	 *
	 * @param map       map of <FIS, {label, style, tooltip}>
	 * @param fis       string value of the functional impact (h, l, m or n)
	 * @param fisValue  numerical value of the functional impact score
	 * @return {{fisClass: string, omaClass: string, value: string, text: string}}
	 * @private
	 */
	function _getFis(map, fis, fisValue)
	{
		var text = "";
		var fisClass = "";
		var omaClass = "";
		var value = "";
		fis = fis.toLowerCase();

		if (map[fis] != null)
		{
			value = map[fis].tooltip;

			if (fisValue != null)
			{
				value = fisValue.toFixed(2);
			}

			text = map[fis].label;
			fisClass = map[fis].style;
			omaClass = "oma_link";
		}

		return {fisClass: fisClass, omaClass: omaClass, value: value, text: text};
	}

	/**
	 * Returns the text content, the css classes, and the total
	 * allele count for the given allele frequency.
	 *
	 * @param frequency allele frequency
	 * @param alt       alt allele count
	 * @param ref       ref allele count
	 * @param tipClass  css class for the tooltip
	 * @return {{text: string, total: number, style: string, tipClass: string}}
	 * @private
	 */
	function getAlleleFreq(frequency, alt, ref, tipClass)
	{
		var text = "NA";
		var total = alt + ref;
		var style = "";
		var tipStyle = "";

		if (frequency)
		{
			style = "mutation_table_allele_freq";
			text = frequency.toFixed(2);
			tipStyle = tipClass;
		}

		return {text: text, total: total, style: style, tipClass: tipStyle};
	}

	function getPdbMatchLink(mutation)
	{
		return getLink(mutation.pdbMatch);
	}

	function getIgvLink(mutation)
	{
		return getLink(mutation.igvLink);
	}

	function getLink(value)
	{
		if (value)
		{
			// this is not a real link,
			// actual action is performed by an event listener
			// "#" indicates that this is a valid link
			return "#";
		}
		else
		{
			// an empty string indicates that this is not a valid link
			// invalid links are removed by the view itself after rendering
			return "";
		}
	}

	function getProteinChange(mutation)
	{
		var style = "mutation-table-protein-change";
		var tip = "click to highlight the position on the diagram";

		// TODO disabled temporarily, enable when isoform support completely ready
//        if (!mutation.canonicalTranscript)
//        {
//            style = "best_effect_transcript " + style;
//            // TODO find a better way to display isoform information
//            tip = "Specified protein change is for the best effect transcript " +
//                "instead of the canonical transcript.<br>" +
//                "<br>RefSeq mRNA id: " + "<b>" + mutation.refseqMrnaId + "</b>" +
//                "<br>Codon change: " + "<b>" + mutation.codonChange + "</b>" +
//                "<br>Uniprot id: " + "<b>" + mutation.uniprotId + "</b>";
//        }

		return {text: mutation.proteinChange,
			style : style,
			tip: tip};
	}

	function getTumorType(mutation)
	{
		var style = "tumor_type";
		var tip = "";

		return {text: mutation.tumorType,
			style : style,
			tip: tip};
	}

	/**
	 * Returns the css class and text for the given cosmic count.
	 *
	 * @param count number of occurrences
	 * @return {{style: string, count: string}}
	 * @private
	 */
	function getCosmic(count)
	{
		var style = "";
		var text = "";

		if (count > 0)
		{
			style = "mutation_table_cosmic";
			text = count;
		}

		return {style: style,
			count: text};
    }

	/**
	 * Returns the text and css class values for the given integer value.
	 *
	 * @param value an integer value
	 * @return {{text: *, style: string}}
	 * @private
	 */
	function getIntValue(value)
	{
		var text = value;
		var style = "mutation_table_int_value";

		if (value == null)
		{
			text = "NA";
			style = "";
		}

		return {text: text, style: style};
	}

	/**
	 * Returns the text and css class values for the given allele count value.
	 *
	 * @param count an integer value
	 * @return {{text: *, style: string}}
	 * @private
	 */
	function getAlleleCount(count)
	{
		var text = count;
		var style = "mutation_table_allele_count";

		if (count == null)
		{
			text = "NA";
			style = "";
		}

		return {text: text, style: style};
    }


	/**
	 * Helper function for predicted impact score sorting.
	 */
	function assignValueToPredictedImpact(text, score)
	{
		// using score by itself may be sufficient,
		// but sometimes we have no numerical score value

		var value;
		text = text.toLowerCase();

		if (text == "low" || text == "l") {
			value = 2;
		} else if (text == "medium" || text == "m") {
			value = 3;
		} else if (text == "high" || text == "h") {
			value = 4;
		} else if (text == "neutral" || text == "n") {
			value = 1;
		} else {
			value = -1;
		}

		if (value > 0 && !isNaN(score))
		{
			//assuming FIS values cannot exceed 1000
			value += score / 1000;
		}

		return value;
	}

	function assignIntValue(value)
	{
		var val = parseInt(value);

		if (isNaN(val))
		{
			val = -Infinity;
		}

		return val;
	}

	function assignFloatValue(value)
	{
		var val = parseFloat(value);

		if (isNaN(val))
		{
			val = -Infinity;
		}

		return val;
	}

	return {
		getCaseId: getCaseId,
		getProteinChange: getProteinChange,
		getPdbMatchLink: getPdbMatchLink,
		getIgvLink: getIgvLink,
		getAlleleCount: getAlleleCount,
		getAlleleFreq: getAlleleFreq,
		getCNA: getCNA,
		getMutationType: getMutationType,
		getMutationStatus: getMutationStatus,
		getValidationStatus: getValidationStatus,
		getFis: getFis,
		getTumorType: getTumorType,
		getCosmic: getCosmic,
		getIntValue: getIntValue,
		assignValueToPredictedImpact: assignValueToPredictedImpact,
		assignIntValue: assignIntValue,
		assignFloatValue: assignFloatValue
	}
})();

