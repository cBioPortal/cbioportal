/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


var StudyViewInitCharts = (function(){
    var parObject = {
        studyId: "",
        caseIds: "",
        cnaProfileId: "",
        mutationProfileId: "",
    };

    
    var ndx = "";
    var attrNameMapUID = []; //The relationshio between "The unique attribute name" and "The unique ID number in whole page"        
    var varCluster = []; //Clusters of displayed charts -- DC.JS require
    var varGroup = []; //Groups of displayed charts -- DC.JS require
    var chartColors = ["#3366cc","#dc3912","#ff9900","#109618",
        "#990099","#0099c6","#dd4477","#66aa00",
        "#b82e2e","#316395","#994499","#22aa99",
        "#aaaa11","#6633cc","#e67300","#8b0707",
        "#651067","#329262","#5574a6","#3b3eac",
        "#b77322","#16d620","#b91383","#f4359e",
        "#9c5935","#a9c413","#2a778d","#668d1c",
        "#bea413","#0c5922","#743411"]; // Color scale from GOOGLE charts
    
    var columnNameTotal =  ["CASE_ID","SUBTYPE","GENDER","AGE",
                            "TUMOR_STAGE_2009","HISTOLOGY","TUMOR_GRADE",
                            "MSI_STATUS_7_MARKER_CALL","MSI_STATUS_5_MARKER_CALL","DATA_MAF",
                            "DATA_GISTIC","DATA_CORE_SAMPLE","DATA_RNASEQ",
                            "MRNA_EXPRESSION_CLUSTER","METHYLATION_CLUSTER",
                            "MLH1_SILENCING","CNA_CLUSTER_K4",
                            "MUTATION_RATE_CLUSTER","MICRO_RNA_CLUSTER",
                            "MICRO_RNA_SCORE","OS_STATUS","OS_MONTHS","DFS_STATUS",
                            "DFS_MONTHS","MUTATION_COUNT","COPY_NUMBER_ALTERATIONS"];

    function initParameters(o) {
        parObject.studyId = o.studyId;
        parObject.caseIds = o.caseIds;
        parObject.cnaProfileId = o.cnaProfileId;
        parObject.mutationProfileId = o.mutationProfileId;
    }

    function initCharts(dataObtained) {
        var dataA = []; //Atrributes
        var dataB = []; //All data
        var pie = []; //Displayed attributes info, dataType: STRING, NUMBER, OR BOOLEAN
        var bar = []; //Displayed attributes info, dataType: NUMBER
        var combine = []; //Combine charts which created for unrecognizable data type
        var varName = []; //Store all attributes in all data
        var HTMLtagsMapUID = []; //The relationshio between "The ID of div standing for each Chart in HTML" and "The unique ID number in whole page"
        var displayedID = []; //Displayed Charts ID number
        var varDisplay = []; //Displayed Charts Name -- the display_name in each attribute       
        var varType = []; //Could be displayed Charts Type 'pie,bar'. Mix combination, seperate by comma
        var distanceMinMaxArray = []; //Only for NUMBER dataType, store min, max and difference value
        
    
        dataA = dataObtained.attr;
        dataB = dataObtained.dataObjectM;
        
         
        //Calculate the number of pie, bar charts
        //Initial varName, varType, distanceMinMaxArray, varDisplay
        for(var i=0; i< dataA.length ; i++){
            var varValuesNum = [];
            for(var j=0;j<dataB.length;j++){
                if(varValuesNum.hasOwnProperty(dataB[j][dataA[i]["attr_id"]]))
                    varValuesNum[dataB[j][dataA[i]["attr_id"]]]++;
                else
                    varValuesNum[dataB[j][dataA[i]["attr_id"]]]=0;
            }
            var keys = Object.keys(varValuesNum);

            if(dataA[i]["attr_id"] === "CASE_ID"){
                pie.push(dataA[i]);
                //continue;
            }else if(dataA[i]["datatype"] === "NUMBER" || dataA[i]["datatype"] === "BOOLEAN"){                
                if(selectedCol(dataA[i]["attr_id"])){                    
                    if(keys.length>10)
                        bar.push(dataA[i]);
                    else
                        pie.push(dataA[i]);
                }

                if(keys.length > 10)
                    varType[dataA[i]["attr_id"]] = "bar";
                else if(keys.length > 30)
                    varType[dataA[i]["attr_id"]] = "bar";
                else
                    varType[dataA[i]["attr_id"]] = "pie";
                
                if(dataA[i]["datatype"] === "NUMBER"){
                    var varValues = [];
                    for(var j=0;j<dataB.length;j++){
                        if(dataB[j][dataA[i]["attr_id"]] && dataB[j][dataA[i]["attr_id"]]!=="NA" && dataB[j][dataA[i]["attr_id"]]!=="")
                            varValues.push(dataB[j][dataA[i]["attr_id"]]);                    
                    }

                    distanceMinMaxArray[dataA[i]["attr_id"]] = {
                        distance : Math.max.apply( Math, varValues ) - Math.min.apply( Math, varValues ),
                        min: Math.min.apply( Math, varValues ),
                        max:Math.max.apply( Math, varValues )
                    };
                }

            }else if(dataA[i]["datatype"] === "STRING"){
                if(selectedCol(dataA[i]["attr_id"])){
                    pie.push(dataA[i]);
                }

                varType[dataA[i]["attr_id"]] = "pie";
            }else 
                combine.push(dataA[i]);

            varDisplay.push(dataA[i]["display_name"]);                
            varName.push(dataA[i]["attr_id"]);
        }

        ndx = crossfilter(dataB);
            
        var totalCharts = pie.length + bar.length;
        var createdChartID = 0;

        for(var i=0; i< pie.length ; i++){
            initPieChart(createdChartID,'study-view-pie-chart',pie[i]["attr_id"],pie[i]["display_name"]);
            HTMLtagsMapUID["study-view-dc-chart-" + createdChartID] = createdChartID;
            attrNameMapUID[pie[i]["attr_id"]] = createdChartID;
            displayedID.push(pie[i]["attr_id"]);
            createdChartID++;
        }

        for(var i=0; i< bar.length ; i++){
            initBarChart(createdChartID,'study-view-bar-chart',bar[i]["attr_id"],bar[i]["display_name"],distanceMinMaxArray);
            HTMLtagsMapUID["study-view-dc-chart-" + createdChartID] = createdChartID;
            attrNameMapUID[bar[i]["attr_id"]] = createdChartID;
            displayedID.push(bar[i]["attr_id"]);            
            createdChartID++;
        }

        if(createdChartID !== totalCharts){
            console.log("Initial charts function error: the number of created charts not equal to number of totalCharts. --1");
            return false;
        }

        dc.renderAll();
        dc.renderAll("group1");

        $('#study-view-selectAttr')
            .find('option:gt(0)')
            .remove()
            .end();

        $.each(varName, function(key, value) {
            if(displayedID.indexOf(value) == -1){
                $('#study-view-selectAttr')
                    .append($("<option></option>")
                        .attr("value",value)
                        .text(varDisplay[key]));
            }
        });

        
        var scatterPlotDataAttr = {
            min_x: distanceMinMaxArray['COPY_NUMBER_ALTERATIONS'].min,
            max_x: distanceMinMaxArray['COPY_NUMBER_ALTERATIONS'].max,
            min_y: distanceMinMaxArray['MUTATION_COUNT'].min,
            max_y: distanceMinMaxArray['MUTATION_COUNT'].max,
            mut_x : false, //have case(s) mutated in only gene x
            mut_y : false,  //have case(s) mutated in only gene y
            mut_both: false //have case(s) mutated in both genes
        };
        
        var scatterPlotOptions = {
            canvas : {  //position of components
                width: 750,
                height: 400,
                xLeft: 100,     //The left/starting point for x axis
                xRight: 630,   //The right/ending point for x axis
                yTop: 10,      //The top/ending point for y axis
                yBottom: 320   //The bottom/starting point for y axis
            },
            style : { //Default style setting
                fill: "#3366cc", //light blue
                stroke: "#0174DF", //dark blue
                stroke_width: "1.2",
                size: "20",
                shape: "circle" //default, may vary for different mutation types
            },

            names: { 
                div: "study-view-scatter-plot",
                header: "study-view-scatter-plot-header",
                body: "study-view-scatter-plot-body",
                loading_img: "study-view-scatter-plot-loading-img",
                control_panel: "study-view-scatter-plot-control-panel"
            },
            elem : {
                svg: "",
                xScale: "",
                yScale: "",
                xAxis: "",
                yAxis: "",
                dotsGroup: "",
                axisGroup: "",
                axisTitleGroup: "",
                brush: ""
            },
            text: {
                xTitle: "Fraction of copy number altered genome",
                yTitle: "# of mutations",
                title: "Mutation Count vs Copy Number Alterations",
                fileName: "",
                xTitleHelp: "Fraction of genome that has log2 copy number value above 0.2 or bellow -0.2",
                yTitleHelp: "Number of sometic non-synonymous mutations"
            },
            legends: [{
                fill: "#3366cc", //light blue
                stroke: "#0174DF", //dark blue
                stroke_width: "1.2",
                size: "20",
                shape: "circle", //default, may vary for different mutation types
                text: "Neither mutated"
            }]
        };    
        var scatterPlotDatum = {              
            x_val: "",
            y_val: "",
            case_id: "",
            qtip: "",
            stroke: "",
            fill: ""
        };
        var scatterPlotArr = [];

        $.each(dataB, function(i,value) {
            if(!isNaN(value['COPY_NUMBER_ALTERATIONS']) && !isNaN(value['MUTATION_COUNT']) && value['COPY_NUMBER_ALTERATIONS'] !="" && value['MUTATION_COUNT']!="") {                  
                var scatterPlotDatumTmp= {};
                scatterPlotDatumTmp.x_val = value['COPY_NUMBER_ALTERATIONS'];
                scatterPlotDatumTmp.y_val = value['MUTATION_COUNT'];
                scatterPlotDatumTmp.case_id = value['CASE_ID'];
                scatterPlotDatumTmp.qtip = "Case ID: <strong><a href='tumormap.do?case_id=" + 
                     value['CASE_ID'] + "&cancer_study_id=" +
                     parObject.studyId + "' target='_blank'>" + 
                     value['CASE_ID'] + "</a></strong>";
                scatterPlotArr.push(scatterPlotDatumTmp);
            }
        });
        
        var scatterStudyView = new ScatterPlots();
        scatterStudyView.init(scatterPlotOptions, scatterPlotArr, scatterPlotDataAttr,true);
        scatterStudyView.jointBrushCallback(scatterPlotCallBack);

        var container = document.querySelector('#study-view-charts');
        var msnry = new Masonry( container, {
            columnWidth: 190,
            itemSelector: '.study-view-dc-chart',
            gutter:1
        });


        $('#study-view-selectAttr').change(function(){
            if($("#study-view-selectAttr")[0].selectedIndex != 0){
                $('#study-view-selectChartType').removeAttr('disabled');

                var chartType = varType[$(this).val()].split(',');

                $('#study-view-selectChartType')
                        .find('option')
                        .remove()
                        .end();

                $.each(chartType, function(key, value) {
                    $('#study-view-selectChartType')
                        .append($("<option></option>")
                            .attr("value",value)
                            .text(value + " chart"));
                });
                if($('#study-view-selectAttr').find(":selected").val())
                    $('#study-view-add-chart-button').removeAttr('disabled');  
            }else{
                $('#study-view-selectChartType')
                        .find('option')
                        .remove()
                        .end()
                        .append('<option>No chart type could be selected</option>')
                        .attr('disabled','disabled');
                $('#study-view-add-chart-button').attr('disabled','disabled');
            }
        });
        
        $('.study-view-dc-chart-delete').click(function(event){
                var id = $(this).parent().parent().attr("id");
                var valueA = $(this).parent().parent().attr("value").split(',');
                var attrID = valueA[0];
                var attrName = valueA[1];
                $("div").remove("#" + id + "-main"); 
                varChart[HTMLtagsMapUID[id]].filterAll();
                dc.redrawAll();
                dc.deregisterChart(varChart[HTMLtagsMapUID[id]]);
                msnry.layout();
                $('#study-view-selectAttr')
                    .append($("<option></option>")
                        .attr("value",attrID)
                        .text(attrName));
                
                var filteredResult = varCluster[attrNameMapUID['CASE_ID']].top(Infinity);
                var filterString = "";
                for(var i=0 ; i<filteredResult.length ; i++){
                    filterString += filteredResult[i].CASE_ID + '|';
                }
                filterString = filterString.substr(0,filterString.length-1);
                var dataTable1 = $('#dataTable').dataTable();
                dataTable1.fnFilter(filterString,0,true);
        });

        $('#study-view-add-chart-button').click(function(){
            var selectedAttr = $('#study-view-selectAttr').find(":selected").val();
            var selectedAttrDisplay = $('#study-view-selectAttr').find(":selected").text();
            var selectedChartType = $('#study-view-selectChartType').find(":selected").val();
            var chartTmpID = -1;
            
            if(Object.keys(attrNameMapUID).indexOf(selectedAttr) !== -1){
                chartTmpID = attrNameMapUID[selectedAttr];
            }else{
                chartTmpID = totalCharts;
                HTMLtagsMapUID["study-view-dc-chart-" + totalCharts] = totalCharts;
                attrNameMapUID[selectedAttr] = totalCharts;
                totalCharts++;       
            }
            if(selectedChartType == 'pie'){
                initPieChart(chartTmpID,'study-view-pie-chart',selectedAttr,selectedAttrDisplay);       
            }else{
                initBarChart(chartTmpID,'study-view-bar-chart',selectedAttr,selectedAttrDisplay,distanceMinMaxArray);                
            }

            $("#study-view-selectAttr option[value='"+ selectedAttr +"']").remove();
            
            msnry.destroy();
            var container = document.querySelector('#study-view-charts');
            msnry = new Masonry( container, {
              columnWidth: 190,
              itemSelector: '.study-view-dc-chart',
              gutter:1
            });
            
            varChart[chartTmpID].render();
            msnry.layout();
            
            $('#study-view-selectChartType')
                    .find('option')
                    .remove()
                    .end()
                    .append('<option>No chart type could be selected</option>')
                    .attr('disabled', 'disabled');
            
            $('#study-view-add-chart-button').attr("disabled", "disabled");
            
            //Register the delete fucntion after adding new chart
            $('#study-view-dc-chart-'+ chartTmpID +' .study-view-dc-chart-delete').click(function(event){
                var id = $(this).parent().parent().attr("id");
                var valueA = $(this).parent().parent().attr("value").split(',');
                var attrID = valueA[0];
                var attrName = valueA[1];
                $("div").remove("#" + id + "-main"); 
                varChart[HTMLtagsMapUID[id]].filterAll();
                dc.redrawAll();
                dc.deregisterChart(varChart[HTMLtagsMapUID[id]]);
                msnry.layout();
                $('#study-view-selectAttr')
                    .append($("<option></option>")
                        .attr("value",attrID)
                        .text(attrName));
                
                var filteredResult = varCluster[attrNameMapUID['CASE_ID']].top(Infinity);
                var filterString = "";
                for(var i=0 ; i<filteredResult.length ; i++){
                    filterString += filteredResult[i].CASE_ID + '|';
                }
                filterString = filterString.substr(0,filterString.length-1);
                var dataTable1 = $('#dataTable').dataTable();
                dataTable1.fnFilter(filterString,0,true);
            });            
        });
    }

    function initPieChart(_chartID,_className,_selectedAttr,_selectedAttrDisplay) {
        if(_selectedAttr === 'CASE_ID'){
            $("#study-view-charts").append("<div id=\"study-view-dc-chart-" + _chartID + "-main\" class='study-view-dc-chart study-view-pie-main' style='display:none'><div id=\"study-view-dc-chart-" + _chartID + "\"></div></div>");
        }else
            $("#study-view-charts").append("<div id=\"study-view-dc-chart-" + _chartID + "-main\" class='study-view-dc-chart study-view-pie-main'><div id=\"study-view-dc-chart-" + _chartID + "\" class='" + _className + "'  value='"+ _selectedAttr + "," + _selectedAttrDisplay + ",pie'><div style='width:180px; float:right'><span class='study-view-dc-chart-delete'>x</span><a href='javascript:varChart[" + _chartID + "].filterAll();dc.redrawAll();'><img src='images/refresh_12px.jpg' title='Reset Chart' class='study-view-dc-chart-change'></img></a></div></div><div style='width:180px; float:center;text-align:center;'><pieH4>" + _selectedAttrDisplay + "</pieH4></div></div>");
        
        varChart[_chartID] = dc.pieChart("#study-view-dc-chart-" + _chartID);
        varCluster[_chartID] = ndx.dimension(function (d) {
            if(!d[_selectedAttr] || d[_selectedAttr].toLowerCase()==="unknown" || d[_selectedAttr].toLowerCase()==="none")
                return "NA";
            return d[_selectedAttr];
        });
        varGroup[_chartID] = varCluster[_chartID].group();

        var pieWidth = 180;
        var pieRadius = (pieWidth - 60) /2;
        varChart[_chartID]
        .width(pieWidth)
        .height(pieWidth)
        .radius(pieRadius)
        .dimension(varCluster[_chartID])
        .group(varGroup[_chartID])
        .transitionDuration(1200)
        .ordinalColors(chartColors)
        .label(function (d) {
            return d.key + ":" + d.value;
        });
    }
    
    function initBarChart(_chartID,_className,_selectedAttr,_selectedAttrDisplay,distanceMinMaxArray) {
        $("#study-view-charts").append("<div id=\"study-view-dc-chart-" + _chartID + "-main\" class='study-view-dc-chart study-view-bar-main'><div id=\"study-view-dc-chart-" + _chartID + "\" class='"+ _className +"'  value='" + _selectedAttr + "," + _selectedAttrDisplay + ",bar'><div style='width:100%; float:right'><span class='study-view-dc-chart-delete'>x</span><a href='javascript:varChart[" + _chartID + "].filterAll();dc.redrawAll();'><img src='images/refresh_12px.jpg' title='Reset Chart' class='study-view-dc-chart-change'></img></a></div></div><div style='width:100%; float:center;text-align:center;'><pieH4>" + _selectedAttrDisplay + "</pieH4></div></div>");
        
        varChart[_chartID] = dc.barChart("#study-view-dc-chart-" + _chartID);
        var distanceMinMax = distanceMinMaxArray[_selectedAttr].distance;

        varCluster[_chartID] = ndx.dimension(function (d) {
            var returnValue = d[_selectedAttr]; 
            if(d[_selectedAttr] % 1 !== 0 && decimalPlaces(d[_selectedAttr]) > 3)
                if(distanceMinMax < 2){
                    returnValue = d3.round(d[_selectedAttr],2);
                }
                else
                    returnValue = d3.round(d[_selectedAttr]);
            if(returnValue === "NA" || returnValue === '')
                returnValue = distanceMinMaxArray[_selectedAttr].min -100;
            return returnValue;
        });

        var barScale = 50;

        varGroup[_chartID] = varCluster[_chartID].group();

        varChart[_chartID]
            .width(370)
            .height(180)
            .margins({top: 10, right: 10, bottom: 30, left: 40})
            .dimension(varCluster[_chartID])
            .group(varGroup[_chartID])
            .centerBar(true)
            .elasticY(true)
            .turnOnControls(true)
            .mouseZoomable(false)
            .brushOn(true)
            .transitionDuration(1200)
            .renderHorizontalGridLines(true)
            .renderVerticalGridLines(true);

        if(distanceMinMax < 1){
            varChart[_chartID].x(d3.scale.linear().nice([distanceMinMaxArray[_selectedAttr].min-distanceMinMax/barScale, distanceMinMaxArray[_selectedAttr].max +distanceMinMax/barScale]));
            varChart[_chartID].yAxis().tickFormat(d3.format("d"));
            varChart[_chartID].xAxis().ticks(10);
            varChart[_chartID].xUnits(function(){return barScale;});
        }else if(distanceMinMax < 2){
            varChart[_chartID].x(d3.scale.linear().domain([distanceMinMaxArray[_selectedAttr].min-distanceMinMax/barScale, distanceMinMaxArray[_selectedAttr].max +distanceMinMax/barScale]));
            varChart[_chartID].yAxis().tickFormat(d3.format("d"));
            varChart[_chartID].xAxis().ticks(10);
            varChart[_chartID].xUnits(function(){return barScale;});
        }else if(_selectedAttrDisplay.search(/month/i) != -1){                
            varChart[_chartID].x(d3.scale.linear().domain([distanceMinMaxArray[_selectedAttr].min-distanceMinMax/barScale, distanceMinMaxArray[_selectedAttr].max +distanceMinMax/barScale]));
            varChart[_chartID].xAxis().ticks(10);
            varChart[_chartID].yAxis().tickFormat(d3.format("d"));
            varChart[_chartID].xAxis().tickFormat(d3.format("d"));
        }else{
            varChart[_chartID].x(d3.scale.linear().domain([distanceMinMaxArray[_selectedAttr].min-distanceMinMax/barScale, distanceMinMaxArray[_selectedAttr].max +distanceMinMax/barScale]));
            varChart[_chartID].yAxis().tickFormat(d3.format("d"));
            varChart[_chartID].xAxis().tickFormat(d3.format("d"));
            varChart[_chartID].xAxis().ticks(10);
            varChart[_chartID].xUnits(function(){return barScale;});
        }

    }

    function restyle(data) {
        var tmpA = [];
        var tmpB = [];
        
        tmpA.push({sTitle:"CASE ID"});
        for(var i=0; i< data.attr.length;i++){
            if(data.attr[i].attr_id !== 'CASE_ID'){
                var tmp = {};
                if(data.attr[i].attr_id === 'COPY_NUMBER_ALTERATIONS')
                    tmp.sTitle = 'CNA';
                else
                    tmp.sTitle = data.attr[i].attr_id.replace(/[_]/g,' ');
                tmpA.push(tmp);
            }
        }
        
        $.each(data.dataObjectM, function(key,value){ 
            tmpB[key] = [];
            $.each(tmpA, function(key1,value1){
                var tmpValue ='';
                
                if(value1.sTitle === 'CNA')
                    tmpValue = value['COPY_NUMBER_ALTERATIONS'];                
                else
                    tmpValue = value[value1.sTitle.replace(/[ ]/g,'_')];
                
                if(!isNaN(tmpValue) && (tmpValue % 1 != 0))
                    tmpValue = cbio.util.toPrecision(Number(tmpValue),2,0.01);
                tmpB[key].push(tmpValue);
            });
        });
        
        var dataTable1 = $('#dataTable').dataTable({
            "sScrollX": "1200px",
            "sWidth": "1200px",
            "sScrollY": "300px",
            "bPaginate": false,
            "bFilter":true,
            "bScrollCollapse": true,
            "aoColumns": tmpA,
            "aaData":tmpB
        });
        
        /*       
        $(".dataTables_scrollFoot tfoot th").each( function ( i ) {
            this.innerHTML = fnCreateSelect( dataTable1.fnGetColumnData(i) );
            //console.log(this);
            $(this).attr('class','dataTableSelect');
            $('select', this).change( function () {
                dataTable1.fnFilter( $(this).val(), i );
            } );
        } );
        */
        //$(".dataTables_scrollFoot>tfoot>th:nth-child(1)").chosen();
        $("#dataTable_filter label input").attr("value","");
        $('#study-view-dataTable-header').click(function(){
            if($("#dataTable_filter label input").val() !== ""){
                    var items=[];
                    $('#dataTable>tbody>tr>td:nth-child(1)').each( function(){
                       items.push( $(this).text() );       
                    });
                    var items = $.unique( items );
                    
                    varChart[attrNameMapUID['CASE_ID']].filterAll();
                    varChart[attrNameMapUID['CASE_ID']].filter([items]);
                    dc.redrawAll();
            }else{
                    varChart[attrNameMapUID['CASE_ID']].filterAll();
                    dc.redrawAll();
            }
        });
        
        $('#study-view-dataTable-updateTable').click(function(){
            var filteredResult = varCluster[attrNameMapUID['CASE_ID']].top(Infinity);
            var filterString = "";
            for(var i=0 ; i<filteredResult.length ; i++){
                filterString += filteredResult[i].CASE_ID + '|';
            }
            filterString = filterString.substr(0,filterString.length-1);
            dataTable1.fnFilter(filterString,0,true);
        });
    }  
 
    function decimalPlaces(num) {
        var match = (''+num).match(/(?:\.(\d+))?(?:[eE]([+-]?\d+))?$/);
        if (!match) { return 0; }
        return Math.max(
            0,
            // Number of digits right of decimal point.
            (match[1] ? match[1].length : 0)
            // Adjust for scientific notation.
            - (match[2] ? +match[2] : 0));
    }

    function selectedCol(col) {
        return col.toLowerCase().match(/(^age)|(gender)|(os_status)|(os_months)|(dfs_status)|(dfs_months)|(race)|(ethnicity)|(.*grade.*)|(.*stage.*)|(histology)|(tumor_type)|(subtype)|(tumor_site)|(.*score.*)|(mutation_count)|(copy_number_alterations)/);
    }
    
    function resizeTable(){                 
        $('#dc-plots-loading-wait').hide();
        $('#study-view-main').show();

        var rotationAngle = 315;
        var radians = Math.PI * (rotationAngle/180);
        var numColumns = 0;
        var maxX =0;
        $('table.dataTable>thead>tr>th').each(function(){
            numColumns++;
        });
        
        for(var i =1;i<=numColumns ; i++){
            var rotatedX = $("table.dataTable>thead>tr>th:nth-child("+i+")").width();
            if(rotatedX > maxX)
                maxX = rotatedX;
        }
        maxX -= 28;
        for(var i =1;i<=numColumns ; i++){
            $("table.dataTable>thead>tr>th:nth-child("+i+")").height(maxX/Math.cos(radians));
        }
        
        var oTable = $('#dataTable').dataTable();        
        $('#dataTable').dataTable().columnFilter();
        oTable.fnAdjustColumnSizing();
        new FixedColumns( oTable);
        $(".DTFC_LeftBodyLiner").css("overflow-y","hidden");
        $(".dataTables_scroll").css("overflow-x","scroll");
        $(".DTFC_LeftHeadWrapper").css("background-color","white");
    }
    
    function scatterPlotCallBack(_brushedCaseIds) {
       if(_brushedCaseIds.length > 0){
            varChart[attrNameMapUID['CASE_ID']].filterAll();
            varChart[attrNameMapUID['CASE_ID']].filter([_brushedCaseIds]);
            dc.redrawAll();
        }
    }
    
    function fnCreateSelect( aData )
    {
        var r='<select><option value=""></option>', i, iLen=aData.length;
        for ( i=0 ; i<iLen ; i++ )
        {
            r += '<option value="'+aData[i]+'">'+aData[i]+'</option>';
        }
        return r+'</select>';
    }

    return {
        init: function(o,data){
            initParameters(o);
            initCharts(data);
            restyle(data);
            resizeTable();
        }
    };
})();