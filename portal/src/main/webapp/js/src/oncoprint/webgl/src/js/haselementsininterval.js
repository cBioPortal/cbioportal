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


module.exports = function(sorted_list, valueFn, lower_inc_val, upper_exc_val) {
    // in: sorted_list, a list sorted in increasing order of valueFn
    //     valueFn, a function that takes an element of sorted_list and returns a number
    //     lower_inc and upper_ex: define a half-open interval [lower_inc, upper_exc)
    // out: boolean, true iff there are any elements whose image under valueFn is in [lower_inc, upper_exc)
    
    var test_lower_inc = 0;
    var test_upper_exc = sorted_list.length;
    var middle, middle_val;
    var ret = false;
    while (true) {
	if (test_lower_inc >= test_upper_exc) {
	    break;
	}
	middle = Math.floor((test_lower_inc + test_upper_exc) / 2)
	middle_val = valueFn(sorted_list[middle]);
	if (middle_val >= upper_exc_val) {
	    test_upper_exc = middle;
	} else if (middle_val < lower_inc_val) {
	    test_lower_inc = middle + 1;
	} else {
	    // otherwise, the middle value is inside the interval, 
	    // so there's at least one value inside the interval
	    ret = true;
	    break;
	}
    }
    return ret;
};