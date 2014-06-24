/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
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