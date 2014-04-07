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

/****************************************************************************************************
 * Creating overall survival and disease free curves for the survival tab
 * @author Yichao Sun
 * @date Sep 2013
 *
 * This code performs the following functions:
 * 1. Calculate the survival rates for each time point using kaplan-meier estimator
 * 2. Generate the curves using d3 line charts w/ mouse over for each time point
 * 3. Display basic information of main query: gene set, nubmer of cases
 * 4. Calculate interested values from the curve: p-value(log-rank test), median, 0.95lcl, 0.95ucl
 *
 ****************************************************************************************************/

var SurvivalCurveView = function() {

    //Instances of calculators
    var data = "",
        opts = "",
        survivalCurve = "",
        kmEstimator = "",
        logRankTest = "";
        //confidenceIntervals = "";
    var divId = "",
        opts = {};

    var dataInitCallBack = function(_pVal) {
        _pVal = parseFloat(_pVal).toFixed(6);
        data.getStats().pVal = _pVal; //Fill out the missing p-value
        //Import default settings
        survivalCurve.init(data, divId, opts);
    }

    return {
        init: function(_caseLists, _dataType, _divId, _opts) {
            //Place the paramteres
            divId = _divId;
            opts = jQuery.extend(true, {}, _opts); //deep copy!
            //Get Survival Data
            var paramsGetSurvivalData = {
                case_set_id: case_set_id,
                case_ids_key: case_ids_key,
                cancer_study_id: cancer_study_id,
                data_type: _dataType
            };
            $.post("getSurvivalData.json", paramsGetSurvivalData, getResultInit(_caseLists), "json");
            //Survival data callback
            function getResultInit(_caseLists) {
                return function(result) {
                    //Init all the calculators
                    data = new SurvivalCurveProxy();
                    survivalCurve = new SurvivalCurve();
                    kmEstimator = new KmEstimator(); 
                    logRankTest = new LogRankTest();   
                    //confidenceIntervals = new ConfidenceIntervals();      
                    //Init Date and then Init view in the data's callback         
                    data.init(result, _caseLists, kmEstimator, logRankTest, dataInitCallBack);
                }
            }
        },
        dataInitCallBack: dataInitCallBack
    }
}; // Close SurvivalCurveView