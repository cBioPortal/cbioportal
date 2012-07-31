
// observer/listener pattern handling selection event on plots
function CaseSelectObserver() {
    this.funcs = {};
    this.caseId = null;
}
CaseSelectObserver.prototype = {
    subscribe: function(listener,func,fire) {
        this.funcs[listener] = func;
        if (fire)
            func.call(window,this.caseId);
    },
    fireSelection: function(caseId,source,targets,forceFire) {
        if (!forceFire&&compareAssociativeArrays(caseId,this.caseId)) return;
        //$('#submit-patient-btn').attr("disabled", caseId==null);
        
        this.caseId = caseId;
        for (var listener in this.funcs) {
            if (targets!=null&&(targets!=listener&&!targets[listener])) continue;
            if (listener==source) continue;
            var func = this.funcs[listener];
            func.call(window,caseId);
        }
    }
};
var csObs = new CaseSelectObserver();

function nrKeysAssociativeArrays(a) {
    if (a==null) return 0;
    var i = 0;
    for (var key in a) i++;
    return i;
}

function compareAssociativeArrays(a, b) {
    if (a == b) return true;
    if ((typeof a)!=(typeof {})||(typeof b)!=(typeof {})) return false;
    if (nrKeysAssociativeArrays(a) != nrKeysAssociativeArrays(b)) return false;
    for (var key in a) {     
        if (a[key] != b[key]) {
            return false;
        }
    }
    return true;
}
 
// replot all
function resetAllPlots(dt) {
    var headerMap = getHeaderMap(dt);
    var caseMap = getCaseMap(dt);

    $('#clinical-data-loading-wait').hide();
    $('#summary-plot-table').show();
    
    drawDataTable('clinical-data-table',dt,caseMap);

    var colCna = headerMap['copy_number_altered_fraction'];
    var colMut = headerMap['mutation_count'];
    plotMutVsCna('mut-cna-scatter-plot',dt,colCna,colMut,caseMap,false,false);

    $('#mut-cna-config').show();

    $(".mut-cna-axis-log").change(function() {
        mutCnaAxisScaleChanged(dt,colCna,colMut,caseMap);
    });

    resetSmallPlots(dt);
}

// replot small
function resetSmallPlots(dt) {
    var divNum = 1;
    for (var i=1; i<dt.getNumberOfColumns(); i++) {
        if (decideToPlot(dt,i))
            plotData(divNum++,dt,i,null);
    }
}

function decideToPlot(dt,col) {
    var c = dt.getColumnLabel(col);
    return c.toLowerCase().match(/(^age)|(survival)|(grade)|(stage)|(histology)/);
}

// draw datatable
function drawDataTable(divId,dt,caseMap) {
    var tableDataView = new google.visualization.DataView(dt);

    var idFormatter = new google.visualization.PatternFormat(formatPatientLink('{0}'));
    idFormatter.format(dt, [0]);
    
    var numFormatter = new google.visualization.NumberFormat({pattern:'#.##'});
    for (var i=0, len=dt.getNumberOfColumns(); i<len; i++) {
        numFormatter.format(dt, i);
    }
    
    var table = new google.visualization.Table(document.getElementById(divId));
    var options = {
        allowHtml: true,
        showRowNumber: true
    };

    google.visualization.events.addListener(table, 'select', function(e){
        var s = table.getSelection();
        var caseId;
        if (s.length==0) caseId = null;
        else if (s.length==1) caseId = dt.getValue(s[0].row,0);
        else {
            caseId = {};
            for (var i=0; i<s.length; i++) {
                caseId[dt.getValue(s[i].row,0)] = true;
            }
        }

        csObs.fireSelection(caseId,divId);
    });
    google.visualization.events.addListener(table, 'ready', function(e){
        csObs.subscribe(divId,function(caseId) {
            if (caseId==null) {
                table.setSelection();
            } else if ((typeof caseId)==(typeof '')) {
                var ix = caseMap[caseId];
                table.setSelection(ix==null?null:[{'row': ix}]);
            } else if ((typeof caseId)==(typeof {})) {
                var rows = [];
                for (var id in caseId) {
                    var row = caseMap[id];
                    if (row!=null)
                        rows.push({'row':caseMap[id]});
                }
                table.setSelection(rows);
            }
        },false);
    });

    table.draw(tableDataView,options);
}

function plotMutVsCna(divId,dt,colCna,colMut,caseMap,hLog,vLog) {
        var scatterDataView = new google.visualization.DataView(dt);
        scatterDataView.setColumns(
            [colCna,
                colMut,
                {calc:function(dt,row){
                        return dt.getValue(row,0)+'\n('+(dt.getValue(row,colCna)*100).toFixed(1)+'%, '+dt.getValue(row,colMut)+')';
                    },type:'string',role:'tooltip'}]);
        var scatter = new google.visualization.ScatterChart(document.getElementById(divId));
        google.visualization.events.addListener(scatter, 'select', function(e){
            var s = scatter.getSelection();
            if (s.length>1) return;
            var caseId = s.length==0 ? null : dt.getValue(s[0].row,0);
            $('#case-id-div').html(formatPatientLink(caseId));
            csObs.fireSelection(caseId, 'scatter-plot');
            resetSmallPlots(dt);
        });

        google.visualization.events.addListener(scatter, 'ready', function(e){
            csObs.subscribe('scatter-plot',function(caseId) {
                if (caseId==null) {
                    scatter.setSelection(null);
                    $('#case-id-div').html("");
                }
                if ((typeof caseId)==(typeof "")) {
                    var ix = caseMap[caseId];
                    scatter.setSelection(ix==null?null:[{'row': ix}]);
                    $('#case-id-div').html(formatPatientLink(caseId));
                } else if ((typeof caseId)==(typeof {})) {
                    var rows = [];
                    for (var id in caseId) {
                        var row = caseMap[id];
                        if (row!=null)
                            rows.push({'row':caseMap[id]});
                    }
                    scatter.setSelection(rows);
                    $('#case-id-div').html(rows.length==1?formatPatientLink(id):"");
                } 
            },true);
        });
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

function getHeaderMap(dataTable) {
    var map = {};
    var cols = dataTable.getNumberOfColumns();
    for (var i=0; i<cols; i++) {
        map[dataTable.getColumnLabel(i)] = i;
    }
    return map;
}

function getCaseMap(dataTable) {
    var map = {};
    var rows = dataTable.getNumberOfRows();
    for (var i=0; i<rows; i++) {
        map[dataTable.getValue(i,0)] = i;
    }
    return map;
}

function makeContInxArray(start,end) {
    var ix = [];
    for (var i=start; i<=end; i++) {
        ix.push(i);
    }
    return ix;
}

function calcHistogram(dt,col,bins,caseIdsFilter) {
    var hist = [[dt.getColumnLabel(col),'# patients']];
    var caseIdHistMap = {};
    var histCaseIdMap = {};
    var rows = dt.getNumberOfRows();
    var t = dt.getColumnType(col);
    if (t=="number") {
        if (bins==null) bins = 10;
        if ((typeof bins)==(typeof 1)) {
            var r = dt.getColumnRange(col);
            var step = (r.max-r.min)/(bins-1);
            var n = bins;
            bins = [];
            for (var i=0; i<n-1; i++) {
                bins.push(r.min+i*step);
            }
        }

        var count = [];
        for (var i=0; i<=bins.length; i++) {
            count.push([]);
        }
        count.push([]); //unknown

        for (var r=0; r<rows; r++) {
            var caseId = dt.getValue(r,0);
            if (caseIdsFilter && !caseIdsFilter[caseId]) continue;
            var v = dt.getValue(r,col);
            var i;
            if (v==null) {
                i = bins.length+1;
            } else {
                for (i=0; i<bins.length; i++) {
                    if (bins[i]>=v) break;
                }
            }
            count[i].push(caseId);
            caseIdHistMap[caseId]=i;
        }

        hist.push(['<='+bins[0],count[0].length]);
        histCaseIdMap[0]=count[0];
        var i=1;
        for (; i<bins.length; i++) {
            hist.push([bins[i-1]+'~'+bins[i],count[i].length]);
            histCaseIdMap[i]=count[i];
        }
        hist.push(['>'+bins[bins.length-1],count[i].length]);
        histCaseIdMap[i]=count[i];
        if (count[++i].length>0) { // including unknow if positive
            hist.push(['Unknown',count[i].length]);
            histCaseIdMap[i]=count[i];
        }
    } else {
        var count = {};
        for (var r=0; r<rows; r++) {
            var caseId = dt.getValue(r,0);
            if (caseIdsFilter && !caseIdsFilter[caseId]) continue;
            var v = dt.getValue(r,col);
            if (v==null) v="Unknown";
            if(count[v]==null) count[v] = [];
            count[v].push(caseId);
        }
        var row=0;
        for (var key in count) {
            var c = count[key];
            hist.push([key,c.length]);
            histCaseIdMap[row]=c;
            for (var i=0; i<c.length; i++)
                caseIdHistMap[c[i]]=row;
            row++;
        }
    }
    return [hist,caseIdHistMap,histCaseIdMap];
}

function plotData(divNum,dt,col,caseIds) {
    var c = dt.getColumnLabel(col);
    var div = getSmallPlotDiv(divNum,c);
    var t = dt.getColumnType(col);
    var nCases = nrKeysAssociativeArrays(caseIds);
    var hist = calcHistogram(dt,col,getBins(dt,col),nCases>0?caseIds:null);
    var columnChart = t=='number';
    var chart = columnChart ? 
        new google.visualization.ColumnChart(div) :
        new google.visualization.PieChart(div);
    google.visualization.events.addListener(chart, 'select', function(e){
        var s = chart.getSelection();
        var caseIds = {};
        var empty = true;
        for (var i=0; i<s.length; i++) {
            var ids = hist[2][s[i].row];
            for (var j=0; j<ids.length; j++) {
                caseIds[ids[j]] = true;
                empty = false;
            }
        }

        // if empty, update all plots including myself
        csObs.fireSelection(caseIds,empty?null:div.id);
    });
    google.visualization.events.addListener(chart, 'ready', function(e){
        csObs.subscribe(div.id,function(caseId) {
            if (caseId==null) {
                chart.setSelection();
            } else if ((typeof caseId)==(typeof '')) {
                var ix = hist[1][caseId];
                chart.setSelection(ix==null?null:[{'row': ix}]);
            } else if ((typeof caseId)==(typeof {})) {
                plotData(divNum,dt,col,caseId);
            }
        },caseIds==null);
    });
    var options = columnChart ?
        {
            hAxis: {title: dt.getColumnLabel(col)},
            vAxis: {title: '# of Patients'},
            legend: {position: 'none'}
        }
        :
        {'pieSliceText' : 'value'}
        ;
    chart.draw(google.visualization.arrayToDataTable(hist[0]),options);
    return chart;
}

function getBins(dt,col) {
    var c = dt.getColumnLabel(col);
    if (c=='age_at_diagnosis') {
        return [20,30,40,50,60,70,80];
    }
    if (c.match(/_months$/)) {
        return [12,24,36,48,60,72,84,96];
    }
    return null;
}

function getSmallPlotDiv(divNum,legend) {
    var div = document.getElementById('small-plot-div-'+divNum);
    if (div!=null) return div;

    var td = $('#small-plot-td-'+divNum);
    if (td.length==0) {
        var irow = Math.ceil(divNum/4);
        if (divNum%4==1) {
            $('#summary-plot-table').append('<tr id="small-plot-tr-'+irow+'"><tr>');
            $('#small-plot-tr-'+irow).html('<td id="small-plot-td-'+divNum+'"></td>');
        } else {
            $('#small-plot-tr-'+irow).append('<td id="small-plot-td-'+divNum+'"></td>');
        }
        td = $('#small-plot-td-'+divNum);
    }

    td.html('<fieldset>'
            +'<legend style="color:blue;font-weight:bold;">'+legend+'</legend>'
            +'<div class="small-plot-div" id="small-plot-div-'+divNum+'">'
            +'</div>'
            + '</fieldset>');
    return document.getElementById('small-plot-div-'+divNum);
}