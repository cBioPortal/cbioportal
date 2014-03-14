
var LoadingJS = (function(){
    //Tmp include public libraries in here, will change JSarray to empty array
    //before merge study view to default branch
    var JSPublic = ['dc','crossfilter','dataTables.fixedColumns','util/StudyViewBoilerplate'
                    ];
    
    //As input for RequireJS
    var JSarray = [];
    
    //Callback Function which is used to run after loadding all JS files
    var callbackFunc = "";
    
    //Put all self created js files into array
    function ConstructJSarray() {
        var _key;
        
        var _folder = {
                component: ['ScatterPlots','PieChart', 'BarChart', 'DataTable','AddCharts'],
                data: ['StudyViewProxy'],
                util: [
                    'DcRedrawAllDataTable', 
                    'FnGetColumnData',
                    'FnColumnFilter'
                ],
                view: [
                        'StudyViewInitCharts', 
                        'StudyViewInitDataTable',
                        'StudyViewInitMiddleComponents',
                        'StudyViewInitTopComponents',
                        'StudyViewInitScatterPlot'],
                controller: ['StudyViewControl']
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
        ConstructJSarray();

        //After loding JS files, run Study View Controller
        require(JSPublic,function(){
             require(JSarray, function(){
                 if(callbackFunc !== ''){
                     callbackFunc();
                 }else{
                     console.log('Error... No Callback Function Initialized.');
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