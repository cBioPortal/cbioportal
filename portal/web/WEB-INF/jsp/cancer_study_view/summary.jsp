
<%@ page import="org.mskcc.portal.servlet.MutationsJSON" %>
<%@ page import="org.mskcc.portal.servlet.CnaJSON" %>
<%@ page import="org.mskcc.portal.servlet.PatientView" %>

<style type="text/css">
.small-plot-div {
    width:270px;
    height:200px;
    display:block;
}
.large-plot-div {
    width:540px;
    height:400px;
    display:block;
}

</style>

<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript">   
    google.load('visualization', '1', {packages:['table','corechart']}); 
    $(document).ready(function(){
        $('#summary-plot-table').hide();
        $('#submit-patient-btn').attr("disabled", true);
        //setupCaseSelect(caseIds);
        loadClinicalData(caseSetId);
        loadMutationCount(mutationProfileId,caseIds);
        loadCnaFraction(caseIds);
        csObs.fireSelection(getRefererCaseId(),null);
    });
    
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
        fireSelection: function(caseId,source) {
            if (compareAssociativeArrays(caseId,this.caseId)) return;
            //$('#submit-patient-btn').attr("disabled", caseId==null);

            this.caseId = caseId;
            for (var listener in this.funcs) {
                if (listener==source) continue;
                var func = this.funcs[listener];
                func.call(window,caseId);
            }
        }
    };
    var csObs = new CaseSelectObserver();
    
    function nrKeys(a) {
        var i = 0;
        for (var key in a) i++;
        return i;
    }
    function compareAssociativeArrays(a, b) {
        if (a == b) return true;
        if ((typeof a)!=(typeof {})||(typeof b)!=(typeof {})) return false;
        if (nrKeys(a) != nrKeys(b)) return false;
        for (var key in a) {     
            if (a[key] != b[key]) {
                return false;
            }
        }
        return true;
    }
    
    function setupCaseSelect(caseIds) {
        var caseSelect = $('#case-select');
        for (var i=0; i<caseIds.length; i++) {
            caseSelect
                .append($("<option></option>")
                .attr("value",caseIds[i])
                .attr("id",caseIds[i]+"_select")
                .text(caseIds[i]));
        }
        csObs.subscribe('case-select',function(caseId){
            var op = caseId ? $("#"+caseId+"_select") : $("#null_case_select");
            op.attr("selected","selected");
        },true);
        caseSelect.change(function(e) {
            var caseId = $('#case-select  option:selected').attr('value');
            if (caseId=="") caseId=null;
            csObs.fireSelection(caseId,'case-select');
        });
    }
    
    var clincialDataTableWrapper = null;
    function loadClinicalData(caseSetId) {
        var params = {cmd:'getClinicalData',
                    case_set_id:caseSetId,
                    include_free_form:1};
        $.get("webservice.do",
            params,
            function(data){
                var rows = data.replace(/^#[^\r\n]+[\r\n]+/g,"")
                        .replace(/\tNA([\t\r\n])/g,"\t$1").replace(/\tNA([\t\r\n])/g,"\t$1")
                        .match(/[^\r\n]+/g);
                var matrix = [];
                for (var i=0; i<rows.length; i++) {
                    matrix.push(rows[i].split('\t'));
                }

                clincialDataTableWrapper = new DataTableWrapper();
                clincialDataTableWrapper.setDataMatrixAndFixTypes(matrix);
                mergeTablesAndVisualize();
            })
    }

    var mutDataTableWrapper = null;
    function loadMutationCount(mutationProfileId,caseIds) {
        if (mutationProfileId==null) return;
        var params = {
            <%=MutationsJSON.CMD%>: '<%=MutationsJSON.COUNT_MUTATIONS_CMD%>',
            <%=QueryBuilder.CASE_IDS%>: caseIds.join(' '),
            <%=PatientView.MUTATION_PROFILE%>: mutationProfileId
        };

        $.post("mutations.json", 
            params,
            function(mutationCounts){
                mutDataTableWrapper = new DataTableWrapper();
                mutDataTableWrapper.setDataMap(mutationCounts,['case_id','mutation_count']);
                mergeTablesAndVisualize();
            }
            ,"json"
        );
    }

    var cnaDataTableWrapper = null;
    function loadCnaFraction(caseIds) {
        if (cnaProfileId==null) return;
        var params = {
            <%=CnaJSON.CMD%>: '<%=CnaJSON.GET_CNA_FRACTION_CMD%>',
            <%=QueryBuilder.CASE_IDS%>: caseIds.join(' ')
        };

        $.post("cna.json", 
            params,
            function(cnaFracs){
                cnaDataTableWrapper = new DataTableWrapper();
                // TODO: what if no segment available
                cnaDataTableWrapper.setDataMap(cnaFracs,['case_id','copy_number_altered_fraction']);
                mergeTablesAndVisualize();
            }
            ,"json"
        );
    }
    
    function mergeTablesAndVisualize() {
        var dt = mergeDataTables();
        if (dt) {
            resetAllPlots(dt);
        }
    }
    
    function resetAllPlots(dt) {
        var headerMap = getHeaderMap(dt);
        var caseMap = getCaseMap(dt);

        $('#clinical-data-loading-wait').hide();
        $('#summary-plot-table').show();

        var formatter = new google.visualization.PatternFormat(formatPatientLink('{0}'));
        formatter.format(dt, [0]);
        drawDataTable('clinical-data-table',dt);

        var colCna = headerMap['copy_number_altered_fraction'];
        var colMut = headerMap['mutation_count'];
        plotMutVsCna('mut-cna-scatter-plot',dt,colCna,colMut,caseMap,false,false);

        $('#mut-cna-config').show();

        $(".mut-cna-axis-log").change(function() {
            mutCnaAxisScaleChanged(dt,colCna,colMut,caseMap);
        });

        resetSmallPlots(dt);
    }
    
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
    
    function mutCnaAxisScaleChanged(dt,colCna,colMut,caseMap) {
        var hLog = $('#mut-cna-haxis-log').is(":checked");
        var vLog = $('#mut-cna-vaxis-log').is(":checked");
        var scatter = plotMutVsCna('mut-cna-scatter-plot',dt,colCna,colMut,caseMap,hLog,vLog);
        var ix = caseMap[csObs.caseId];
        scatter.setSelection(ix==null?null:[{'row': ix}]);
    }
    
    function drawDataTable(divId,dt) {
            var tableDataView = new google.visualization.DataView(dt);
            var table = new google.visualization.Table(document.getElementById(divId));
            var options = {
                allowHtml: true,
                showRowNumber: true,
                page: 'enable',
                pageSize: 100
            };
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
                        $('#case-id-div').html("");
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
        return caseId==null?"":'<a title="Go to patient-centric view" href="patient.do?<%=PatientView.PATIENT_ID%>='+caseId+'">'+caseId+'</a>'
    }
    
    function mergeDataTables() {
        if (clincialDataTableWrapper==null ||
            (mutationProfileId!=null && mutDataTableWrapper==null) ||
            (cnaProfileId!=null && cnaDataTableWrapper==null)) {
            return null;
        }
        
        var dt = clincialDataTableWrapper.dataTable;
        
        if (mutDataTableWrapper!=null) {
            dt = google.visualization.data.join(dt, mutDataTableWrapper.dataTable,
                    'full', [[0,0]], makeContInxArray(1,dt.getNumberOfColumns()-1),[1]);
        }
        
        if (cnaDataTableWrapper!=null) {
            dt = google.visualization.data.join(dt, cnaDataTableWrapper.dataTable,
                    'full', [[0,0]], makeContInxArray(1,dt.getNumberOfColumns()-1),[1]);
        }
        
        clincialDataTableWrapper = null;
        mutDataTableWrapper = null;
        
        return dt;
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
            
            for (var r=0; r<rows; r++) {
                var caseId = dt.getValue(r,0);
                if (caseIdsFilter!=null && !caseIdsFilter[caseId]) continue;
                var v = dt.getValue(r,col);
                if (v==null) continue;
                var i=0;
                for (; i<bins.length; i++) {
                    if (bins[i]>=v) break;
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
        } else {
            var count = {};
            for (var r=0; r<rows; r++) {
                var caseId = dt.getValue(r,0);
                if (caseIdsFilter!=null && !caseIdsFilter[caseId]) continue;
                var v = dt.getValue(r,col);
                if (v==null) continue;
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
    
    function plotData(divNum,dt,col,caseIdsFilter) {
        var c = dt.getColumnLabel(col);
        var div = getSmallPlotDiv(divNum,c);
        var t = dt.getColumnType(col);
        var hist = calcHistogram(dt,col,getBins(dt,col),caseIdsFilter);
        var columnChart = t=='number';
        var chart = columnChart ? 
            new google.visualization.ColumnChart(div) :
            new google.visualization.PieChart(div);
        var options = !columnChart ? {} : 
            {
                hAxis: {title: dt.getColumnLabel(col)},
                vAxis: {title: '# of Patients'},
                legend: {position: 'none'}
            };
        google.visualization.events.addListener(chart, 'select', function(e){
            var s = chart.getSelection();
            var caseIds = {};
            for (var i=0; i<s.length; i++) {
                var ids = hist[2][s[i].row];
                for (var j=0; j<ids.length; j++) {
                    caseIds[ids[j]] = true;
                }
            }
            
            csObs.fireSelection(caseIds,div.id);
        });
        google.visualization.events.addListener(chart, 'ready', function(e){
            csObs.subscribe(div.id,function(caseId) {
                if ((typeof caseId)==(typeof '')) {
                    var ix = hist[1][caseId];
                    chart.setSelection(ix==null?null:[{'row': ix}]);
                } else if ((typeof caseId)==(typeof {})) {
                    plotData(divNum,dt,col,caseId);
                }
            },caseIdsFilter==null);
        });
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
</script>

<div id="clinical-data-loading-wait">
    <img src="images/ajax-loader.gif"/>
</div>

<table id="summary-plot-table">
    <tr>
        <td id="small-plot-td-1"></td>
        <td id="small-plot-td-2"></td>
        <td rowspan="2" colspan="2">
            <fieldset style="padding:0px 1px">
                <legend style="color:blue;font-weight:bold;">Mutation Count VS. Copy Number Alteration</legend>
                <div style="display:none">
                    <form name="input" action="patient.do" method="get">
                        <select id="case-select" name="<%=PatientView.PATIENT_ID%>"><option id="null_case_select"></option></select>
                        <input type="submit" id="submit-patient-btn" value="More About This Case" />
                    </form>
                </div>
                <div id="mut-cna-scatter-plot" class="large-plot-div">
                    <img src="images/ajax-loader.gif"/>
                </div>
                <table style="display:none;width:100%;" id="mut-cna-config">
                    <tr width="100%">
                            <td>
                                H-Axis scale: <input type="radio" name="mut-cna-haxis-log" class="mut-cna-axis-log" value="normal" checked="checked"/>Normal &nbsp;
                                <input type="radio" name="mut-cna-haxis-log" class="mut-cna-axis-log" value="log" id="mut-cna-haxis-log"/>log<br/>
                                V-Axis scale: <input type="radio" name="mut-cna-vaxis-log" class="mut-cna-axis-log" value="normal" checked="checked"/>Normal &nbsp;
                                <input type="radio" name="mut-cna-vaxis-log" class="mut-cna-axis-log" value="log" id="mut-cna-vaxis-log"/>log
                            </td>
                            <td id="case-id-div" align="right">
                            </td>
                    </tr>
                </table>
            </fieldset>
        </td>
    </tr>
    <tr>
        <td id="small-plot-td-3"></td>
        <td id="small-plot-td-4"></td>
    </tr>
    
</table>

<div id="clinicalTable"></div>