

function plotMutVsCna(csObs,divId,caseIdDiv,dt,colCna,colMut,caseMap,hLog,vLog) {
        var scatterDataView = new google.visualization.DataView(dt);
        scatterDataView.setColumns(
            [colCna,
                colMut,
                {calc:function(dt,row){
                        return dt.getValue(row,0)+'\n('+(dt.getValue(row,colCna)*100).toFixed(1)+'%, '+dt.getValue(row,colMut)+')';
                    },type:'string',role:'tooltip'}]);
        var scatter = new google.visualization.ScatterChart(document.getElementById(divId));
        
        if (csObs) {
            google.visualization.events.addListener(scatter, 'select', function(e){
                var s = scatter.getSelection();
                if (s.length>1) return;
                var caseId = s.length==0 ? null : dt.getValue(s[0].row,0);
                $('#'+caseIdDiv).html(formatPatientLink(caseId));
                csObs.fireSelection(caseId, divId);
                resetSmallPlots(dt);
            });

            google.visualization.events.addListener(scatter, 'ready', function(e){
                csObs.subscribe(divId,function(caseId) {
                    if (caseId==null) {
                        scatter.setSelection(null);
                        $('#'+caseIdDiv).html("");
                    }
                    if ((typeof caseId)==(typeof "")) {
                        var ix = caseMap[caseId];
                        scatter.setSelection(ix==null?null:[{'row': ix}]);
                        $('#'+caseIdDiv).html(formatPatientLink(caseId));
                    } else if ((typeof caseId)==(typeof {})) {
                        var rows = [];
                        for (var id in caseId) {
                            var row = caseMap[id];
                            if (row!=null)
                                rows.push({'row':caseMap[id]});
                        }
                        scatter.setSelection(rows);
                        $('#'+caseIdDiv).html(rows.length==1?formatPatientLink(id):"");
                    } 
                },true);
            });
        }
        
        var options = {
            hAxis: {title: "Copy number alteration fraction", logScale:hLog, format:'#%'},
            vAxis: {title: "# of mutations", logScale:vLog, format:'#,###'},
            legend: {position:'none'}
        };
        scatter.draw(scatterDataView,options);
        return scatter;
}

function formatPatientLink(caseId) {
    return caseId==null?"":'<a title="Go to patient-centric view" href="patient.do?case_id='+caseId+'">'+caseId+'</a>'
}