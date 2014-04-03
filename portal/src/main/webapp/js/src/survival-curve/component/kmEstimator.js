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
 
var KmEstimator = function() {

    return {
        calc: function(inputGrp) { //calculate the survival rate for each time point
            //each item in the input already has fields: time, num at risk, event/status(0-->censored)
            var _prev_value = 1;  //cache for the previous value
            for (var i in inputGrp) {
                var _case = inputGrp[i];
                if (_case.status === "1") {
                    _case.survival_rate = _prev_value * ((_case.num_at_risk - 1) / _case.num_at_risk) ;
                    _prev_value = _case.survival_rate;
                } else if (_case.status === "0") {
                    _case.survival_rate = _prev_value; //survival rate remain the same if the event is "censored"
                } else {
                    //TODO: error handling
                }
            }
        }
    }

}; //Close KmEstimator