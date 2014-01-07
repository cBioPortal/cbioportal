/**
 * Utility class to create segments from a merged alignment.
 * (See PdbChainModel.mergeAlignments function for details of merged alignments)
 *
 * @param mergedAlignment   merged alignment object (see PdbChainModel.mergedAlignment field)
 * @constructor
 *
 * @author Selcuk Onur Sumer
 */
var MergedAlignmentSegmentor = function(mergedAlignment)
{
	var _mergedAlignment = mergedAlignment;

	// start position (initially zero)
	var _start = 0;

	/**
	 * Checks if there are more segments in this merged alignment.
	 *
	 * @return {boolean}
	 */
	function hasNextSegment()
	{
		return (_start < _mergedAlignment.mergedString.length);
	}

	/**
	 * Extracts the next segment from the merged alignment string. Returns
	 * the segment as an object with the actual segment string,
	 * start (uniprot) position, and end (uniprot) position.
	 *
	 * @return {object} segment with string, start, and end info
	 */
	function getNextSegment()
	{
		var str = _mergedAlignment.mergedString;

		var segment = {};
		segment.start = _start + _mergedAlignment.uniprotFrom;
		var symbol = str[_start];
		var end = _start;

		// for each special symbol block, a new segment is created
		if (isSpecialSymbol(symbol))
		{
			segment.type = symbol;

			while (str[end] == symbol &&
			       end <= str.length)
			{
				end++;
			}
		}
		else
		{
			segment.type = "regular";

			while (!isSpecialSymbol(str[end]) &&
			       end <= str.length)
			{
				end++;
			}
		}

		segment.end = end + _mergedAlignment.uniprotFrom;
		segment.str = str.substring(_start, end);

		// update start for the next segment
		_start = end;

		return segment;
	}

	function isSpecialSymbol(symbol)
	{
		// considering symbols other than GAP as special
		// results in too many segments...
//		return (symbol == PdbDataUtil.ALIGNMENT_GAP) ||
//		       (symbol == PdbDataUtil.ALIGNMENT_MINUS) ||
//		       (symbol == PdbDataUtil.ALIGNMENT_PLUS) ||
//		       (symbol == PdbDataUtil.ALIGNMENT_SPACE);

		return (symbol == PdbDataUtil.ALIGNMENT_GAP);
	}

	return {
		hasNextSegment: hasNextSegment,
		getNextSegment: getNextSegment
	};
};
