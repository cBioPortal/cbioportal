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

var SurvivalCurveView = function(_opts) {

    //Instances of calculators
    var dataInst = "", //Not raw data but the ones being calculated by KM and log-tank already!
        opts = _opts,
        survivalCurve = "",
        kmEstimator = "",
        logRankTest = "",
        //confidenceIntervals = "",
        //data instances for each group
        alteredGroup = [],
        unalteredGroup = [],
        inputArr = [];

    var pValCallBackFunc = function(_pVal) {
        opts.vals.pVal = _pVal;
        survivalCurve = new SurvivalCurve();
        survivalCurve.init(inputArr, opts);
    };
    
    var getResultInit = function(_caseLists, _data) {
        //Init all the calculators
        kmEstimator = new KmEstimator(); 
        logRankTest = new LogRankTest();   
        //confidenceIntervals = new ConfidenceIntervals();   
        
        //Split the data into different(altered/unaltered) groups  
        var patientSampleIdMap = window.PortalGlobals.getPatientSampleIdMap();
        for (var key in _caseLists) {  
            if (_caseLists[key] === "altered") {
                var tmpPatientId = patientSampleIdMap[key];        
                alteredGroup.push(tmpPatientId);
            }
            else if (_caseLists[key] === "unaltered") {
                var tmpPatientId = patientSampleIdMap[key];  
                unalteredGroup.push(tmpPatientId);    
            }
        }

        //Init data instances for different groups
        var alteredDataInst = new SurvivalCurveProxy();
        var unalteredDataInst = new SurvivalCurveProxy();
        alteredDataInst.init(_data, alteredGroup, kmEstimator, logRankTest);
        unalteredDataInst.init(_data, unalteredGroup, kmEstimator, logRankTest);

        //Individual settings 
        var unalteredSettingsInst = jQuery.extend(true, {}, SurvivalCurveBroilerPlate.subGroupSettings);
        unalteredSettingsInst.line_color = "blue";
        unalteredSettingsInst.mouseover_color = "#81BEF7";
        unalteredSettingsInst.legend = "Cases without Alteration(s) in Query Gene(s)";
        var alteredSettingsInst = jQuery.extend(true, {}, SurvivalCurveBroilerPlate.subGroupSettings);
        alteredSettingsInst.line_color = "red";
        alteredSettingsInst.mouseover_color = "#F5BCA9";
        alteredSettingsInst.legend = "Cases with Alteration(s) in Query Gene(s)";
        
        //Assemble the input
        var alteredInputInst = {},
            unalteredInputInst = {};
        alteredInputInst.data = alteredDataInst;
        alteredInputInst.settings = alteredSettingsInst;
        unalteredInputInst.data = unalteredDataInst;
        unalteredInputInst.settings = unalteredSettingsInst;

        //render the curve
        inputArr = [alteredInputInst, unalteredInputInst];
        logRankTest.calc(inputArr[0].data.getData(), inputArr[1].data.getData(), pValCallBackFunc);
    };

    return {
        getResultInit: getResultInit,
        pValCallBackFunc: pValCallBackFunc
    };
}; // Close SurvivalCurveView