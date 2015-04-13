/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
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

 
var ConfidenceIntervals = function() {

    var arr = [],
        n = 0,
        mean = 0,
        sd = 0,
        se = 0,
        lcl = 0,
        ucl = 0;

    function adaptor(inputArr) {
        var _index = 0;
        for(var i in inputArr) {
            if (inputArr[i].status === "1")  {
                arr[_index] = inputArr[i].time;
                _index += 1;
            }
        }
        n = arr.length;
    }

    function calcMean() {
        var _sum = 0;
        for(var i in arr) { _sum += arr[i]; }
        mean = _sum / n;
    }

    function calcStandardDeviation() {
        var _sum = 0;
        for(var i in arr) { _sum += (arr[i] - mean) * (arr[i] - mean); }
        sd = Math.sqrt(_sum / (n-1));
    }

    function calcStandardError() {
        se = sd / Math.sqrt(n);
    }

    function calcControlLimits() {  //apply 0.95 percentage
        lcl = mean - 1.962 * se;
        ucl = mean + 1.962 * se;
        return {
            "lcl": lcl,
            "ucl": ucl
        };
    }

    return {
        calc: function(inputArr) {
            n = 0;
            arr.length = 0;
            adaptor(inputArr);
            calcMean();
            calcStandardDeviation();
            calcStandardError();
            calcControlLimits();
        }
    }
};