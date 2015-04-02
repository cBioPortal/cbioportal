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


var LoadingJS = (function(){
    //Tmp include public libraries in here, will change JSarray to empty array
    //before merge study view to default branch
    var JSPublic = [
                    'util/StudyViewBoilerplate',
                    'js/src/survival-tab/survivalCurveProxy.js',
                    'js/src/survival-tab/component/survivalCurve.js',
                    'js/src/survival-tab/component/confidenceIntervals.js',
                    'js/src/survival-tab/component/kmEstimator.js',
                    'js/src/survival-tab/component/logRankTest.js',
                    'js/src/survival-tab/component/boilerPlate.js',
                    'js/lib/FileSaver.min.js'
                ];
    
    //As input for RequireJS
    var JSarray = [];
    
    //Callback Function which is used to run after loadding all JS files
    var callbackFunc = "";
    
    //Put all self created js files into array
    function constructJSarray() {
        var _key;
        
        var _folder = {
                component: [
                    'ScatterPlots',
                    'PieChart', 
                    'BarChart', 
                    'DataTable',
                    'AddCharts',
                    'Table'
                ],
                data: ['StudyViewProxy'],
                util: [
                    'FnGetColumnData',
                    'StudyViewUtil',
                    'StudyViewPrototypes'
                ],
                view: [
                    'StudyViewInitCharts', 
                    'StudyViewInitDataTable',
                    'StudyViewInitTopComponents',
                    'StudyViewInitScatterPlot',
                    'StudyViewInitIntroJS',
                    'StudyViewInitWordCloud',
                    'StudyViewWindowEvents',
                    'StudyViewInitMutationsTab',
                    'StudyViewInitCNATab',
                    'StudyViewInitClinicalTab',
                    'StudyViewSurvivalPlotView',
                    'StudyViewInitTables'
                ],
                controller: [
                    'StudyViewMainController',
                    'StudyViewSummaryTabController',
                    'StudyViewMutationsTabController',
                    'StudyViewCNATabController',
                    'StudyViewClinicalTabController',
                    'StudyViewParams'
                ]
            };
            
        for(_key in _folder){
            var _currentLength = _folder[_key].length;
            
            for(var j = 0; j < _currentLength; j++){
                var _file = _key + "/" + _folder[_key][j];
                JSarray.push(_file);
            }
        }    
    }
    
    function main(){
        constructJSarray();
        
        //Add appVerion after all included js files
        require.config({
            urlArgs: appVersion
        });
        
        //After loding JS files, run Study View Controller
        require(JSPublic,function(){
             require(JSarray, function(){
                 if(callbackFunc !== ''){
                     callbackFunc();
                 }else{
                     console.log('%c Error: No Callback Function Initialized.', 
                                    "color:red");
                 }
            });
        });
    }
    
    return {
        init: function(_callbackFunc){
            callbackFunc = _callbackFunc;
            main();
        }
    };
})();
