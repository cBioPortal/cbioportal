/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


/* HotspotSet
 * 
 * This class is designed for quickly querying whether a mutation is a hotspot.
 * It's supported to query by a single residue position, or a range of residue positions,
 *  and it does a binary search to quickly check whether the input range or position
 *  overlaps with any hotspot regions specified in the constructor.
 * 
 * The class does not support telling you exactly which input intervals are overlapped-with,
 *  that information is lost. But it will tell you true or false: whether there was some input interval
 *  that is overlapped-with.
 */
HotspotSet = (function() {
    function HotspotSet(intervals) {
	// intervals: [L:number, U:number][] where L <= U
	this.regions = createHotspotRegions(intervals);
    }
    
    function createHotspotRegions(intervals) {
	// in: intervals:[L:number, U:number][] where L <= U
	// out: a list of intervals (type [L:number, U:number][] where L <= U) such that 
	//	every interval is disjoint and the list is in sorted order
	if (intervals.length === 0) {
	    return [];
	}
	
	// First, sort the intervals by lower bound
	intervals.sort(function(a,b) {
	    return ((a[0] < b[0]) ? -1 : 1);
	});
	// Then, consolidate them
	var ret = [];
	var currentCombinedInterval = intervals[0].slice();
	var currentInterval;
	var i = 1;
	while (i<=intervals.length) {
	    if (i === intervals.length) {
		ret.push(currentCombinedInterval);
	    } else {
		currentInterval = intervals[i];
		if (currentInterval[0] > currentCombinedInterval[1]) {
		    // disjoint, should move on
		    ret.push(currentCombinedInterval);
		    currentCombinedInterval = intervals[i].slice();
		} else {
		    // overlaps, should combine
		    // by the sort order, we know that currentCombinedInterval[0] <= currentInterval[0],
		    //	so to combine we just need to take the max upper bound value
		    currentCombinedInterval[1] = Math.max(currentCombinedInterval[1], currentInterval[1]);
		}
	    }
	    i++;
	}
	
	return ret;
    }
    
    HotspotSet.prototype.check = function(x, y) {
	// in: x, a number
	//     y (optional), a number
	// out: boolean
	//	if only x given, check if x lies in a hotspot
	//	if x and y given, check if [x,y] overlaps with a hotspot,
	//	    meaning is there a region [A,B] with (A<=y && B>=x)
	
	if (typeof y === "undefined") {
	    y = x;
	}
	
	var regions = this.regions;
	var lowerIndexIncl = 0;
	var upperIndexExcl = regions.length;
	var testRegionIndex;
	var testRegion;
	var success = false;
	while (lowerIndexIncl < upperIndexExcl) {
	    testRegionIndex = Math.floor((lowerIndexIncl + upperIndexExcl) / 2);
	    testRegion = regions[testRegionIndex];
	    if (testRegion[0] > y) {
		// too big
		upperIndexExcl = testRegionIndex;
	    } else if (testRegion[1] < x) {
		// too small
		lowerIndexIncl = testRegionIndex + 1;
	    } else {
		// both requirements met - success!
		success = true;
		break;
	    }
	}
	return success;
    }
    
    HotspotSet.prototype._getHotspotRegions = function() {
	// Do not use - for testing only
	return this.regions;
    }
    
    return HotspotSet;
})();

if (module) {
    module.exports = HotspotSet;
}