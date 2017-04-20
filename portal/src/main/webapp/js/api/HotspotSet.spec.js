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

var HotspotSet = require('./HotspotSet.js');

var allPassed = true;

var fail = function(msg) {
    allPassed = false;
    console.log("FAILED: "+msg);
};

var assertEqualRegions = function(a,b, msg) {
    if (a.length !== b.length) {
	fail(msg + "..expected "+JSON.stringify(b)+" and got "+JSON.stringify(a));
    } else {
	for (var i=0; i<a.length; i++) {
	    if ((a[i][0] !== b[i][0]) || (a[i][1] !== b[i][1])) {
		fail(msg + "..expected "+JSON.stringify(b)+" and got "+JSON.stringify(a));
		break;
	    }
	}
    }
};

var assert = function(b, msg) {
    if (!b) {
	fail(msg);
    }
};
// Test empty
var hss = new HotspotSet([]);
assertEqualRegions(hss._getHotspotRegions(), []);
assert(!hss.check(0));
assert(!hss.check(3951));
assert(!hss.check(-1395813));
assert(!hss.check(0.351));
assert(!hss.check([0,10]));


// one region
hss = new HotspotSet([[0,5]]);
assertEqualRegions(hss._getHotspotRegions(), [[0,5]]);
assert(hss.check(3));
assert(hss.check(0,5));
assert(hss.check(-1,0));
assert(!hss.check(-1));
assert(hss.check(0));
assert(hss.check(5));
assert(hss.check(5,6));
assert(hss.check(5,10));
assert(hss.check(4,6));
assert(hss.check(2,4));
assert(!hss.check(6,6));
assert(!hss.check(6,10));
assert(!hss.check(-5,-1));


// one singleton region
hss = new HotspotSet([[-3,-3]]);
assertEqualRegions(hss._getHotspotRegions(), [[-3,-3]]);
assert(!hss.check(6,6));
assert(!hss.check(6,10));
assert(hss.check(-5,-1));
assert(hss.check(-3));
assert(hss.check(-3,-1));
assert(hss.check(-6,-1));
assert(!hss.check(-4,-3.5));
assert(!hss.check(-4,-4));


// one singleton region and one interval region
hss = new HotspotSet([[-3,-3], [0,5]]);
assertEqualRegions(hss._getHotspotRegions(), [[-3,-3],[0,5]]);
assert(hss.check(3));
assert(hss.check(0,5));
assert(hss.check(-1,0));
assert(!hss.check(-1));
assert(hss.check(0));
assert(hss.check(5));
assert(hss.check(5,6));
assert(hss.check(5,10));
assert(hss.check(4,6));
assert(hss.check(2,4));
assert(!hss.check(6,6));
assert(!hss.check(6,10));
assert(hss.check(-5,-1));
assert(hss.check(-3));
assert(hss.check(-3,-1));
assert(hss.check(-6,-1));
assert(!hss.check(-4,-3.5));
assert(!hss.check(-4,-4));


// one region, consolidated from a few
hss = new HotspotSet([[4,20], [0,5], [2,6], [-5,0], [3,3]]);
assertEqualRegions(hss._getHotspotRegions(), [[-5,20]]);
assert(hss.check(3));
assert(hss.check(0,5));
assert(hss.check(-1,0));
assert(hss.check(-1));
assert(hss.check(0));
assert(hss.check(5));
assert(hss.check(5,6));
assert(hss.check(5,10));
assert(hss.check(4,6));
assert(hss.check(2,4));
assert(!hss.check(21,21));
assert(!hss.check(25,30));
assert(!hss.check(-6,-6));

// two regions, consolidated from several
hss = new HotspotSet([[4,20], [0,5], [30,60], [24,70], [2,6], [-5,0]]);
assertEqualRegions(hss._getHotspotRegions(), [[-5,20], [24,70]]);
assert(hss.check(3));
assert(hss.check(0,5));
assert(hss.check(-1,0));
assert(hss.check(-1));
assert(hss.check(0));
assert(hss.check(5));
assert(hss.check(5,6));
assert(hss.check(5,10));
assert(hss.check(4,6));
assert(hss.check(2,4));
assert(!hss.check(21,21));
assert(hss.check(25,30));
assert(!hss.check(-6,-6));
assert(hss.check(25,71));
assert(hss.check(24));
assert(hss.check(70));
assert(!hss.check(79));
assert(hss.check(69,72));

// three regions, consolidated from several
hss = new HotspotSet([[-50, -30],  [-4, 3], [-4,9], [-4,1], [-30, -10], [100,200]]);
assertEqualRegions(hss._getHotspotRegions(), [[-50,-10], [-4,9], [100,200]]);
assert(hss.check(3));
assert(hss.check(0,5));
assert(hss.check(-1,0));
assert(hss.check(-1));
assert(hss.check(0));
assert(hss.check(5));
assert(hss.check(5,6));
assert(hss.check(5,10));
assert(hss.check(4,6));
assert(hss.check(2,4));
assert(!hss.check(21,21));
assert(!hss.check(25,30));
assert(!hss.check(-6,-6));
assert(!hss.check(25,71));
assert(!hss.check(24));
assert(!hss.check(70));
assert(!hss.check(79));
assert(!hss.check(69,72));
assert(hss.check(100,200));
assert(hss.check(200));
assert(hss.check(100));
assert(hss.check(80,200));
assert(hss.check(80,220));
assert(hss.check(80,140));
assert(hss.check(120,180));
assert(hss.check(180,299));
assert(!hss.check(99));
assert(!hss.check(95,99));

if (allPassed) {
    console.log("All tests passed!");
}