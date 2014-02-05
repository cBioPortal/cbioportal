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
    var all = "";
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
        var row = [];//Displayed attributes info, dataType: STRING, NUMBER, OR BOOLEAN 
        var rowKeys = []; //Different keys in one attribute among all data, 2D array
        var bar = []; //Displayed attributes info, dataType: NUMBER
        var combine = []; //Combine charts which created for unrecognizable data type
        var varName = []; //Store all attributes in all data
        var HTMLtagsMapUID = []; //The relationshio between "The ID of div standing for each Chart in HTML" and "The unique ID number in whole page"
        var attrNameMapUID = []; //The relationshio between "The unique attribute name" and "The unique ID number in whole page"
        var displayedID = []; //Displayed Charts ID number
        var varDisplay = []; //Displayed Charts Name -- the display_name in each attribute       
        var varType = []; //Could be displayed Charts Type 'pie,bar or row'. Mix combination, seperate by comma
        var distanceMinMaxArray = []; //Only for NUMBER dataType, store min, max and difference value
        
    
        dataA = dataObtained.attr;
        dataB = dataObtained.dataObjectM;
        
         
        //Calculate the number of pie, row, bar charts
        //Initial varName, varType, distanceMinMaxArray, varDisplay, rowKeys
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
                continue;
            }else if(dataA[i]["datatype"] === "NUMBER" || dataA[i]["datatype"] === "BOOLEAN"){                
                if(selectedCol(dataA[i]["attr_id"])){                    
                    if(keys.length>10)
                        bar.push(dataA[i]);
                    else
                        pie.push(dataA[i]);
                }

                if(keys.length > 10)
                    varType[dataA[i]["attr_id"]] = "bar,row";
                else if(keys.length > 30)
                    varType[dataA[i]["attr_id"]] = "bar";
                else
                    varType[dataA[i]["attr_id"]] = "pie,row";
                
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

                varType[dataA[i]["attr_id"]] = "pie,row";
            }else 
                combine.push(dataA[i]);

            varDisplay.push(dataA[i]["display_name"]);                
            varName.push(dataA[i]["attr_id"]);
            rowKeys[dataA[i]["attr_id"]] = keys;
        }
        
        

        ndx = crossfilter(dataB);
        all = ndx.groupAll();
            
        var totalCharts = pie.length + bar.length + row.length;
        var createdChartID = 0;

        for(var i=0; i< pie.length ; i++){
            initPieChart(createdChartID,'study-view-dc-chart study-view-pie-chart',pie[i]["attr_id"],pie[i]["display_name"]);
            HTMLtagsMapUID["study-view-dc-chart-" + createdChartID] = createdChartID;
            attrNameMapUID[pie[i]["attr_id"]] = createdChartID;
            displayedID.push(pie[i]["attr_id"]);
            createdChartID++;
        }

        for(var i=0; i< row.length; i++){
            initRowChart(createdChartID,'study-view-dc-chart study-view-row-chart',row[i]["attr_id"],row[i]["display_name"],rowKeys);
            HTMLtagsMapUID["study-view-dc-chart-" + createdChartID] = createdChartID;
            attrNameMapUID[row[i]["attr_id"]] = createdChartID;
            displayedID.push(row[i]["attr_id"]);
            createdChartID++;
        }

        for(var i=0; i< bar.length ; i++){
            initBarChart(createdChartID,'study-view-dc-chart study-view-bar-chart',bar[i]["attr_id"],bar[i]["display_name"],distanceMinMaxArray);
            HTMLtagsMapUID["study-view-dc-chart-" + createdChartID] = createdChartID;
            attrNameMapUID[bar[i]["attr_id"]] = createdChartID;
            displayedID.push(bar[i]["attr_id"]);            
            createdChartID++;
        }

        if(createdChartID !== totalCharts){
            console.log("Initial charts function error: the number of created charts not equal to number of totalCharts. --1");
            return false;
        }

        initDataTable(varName);

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
                axisTitleGroup: ""
            },
            text: {
                xTitle: "Fraction of copy number altered genome",
                yTitle: "# of mutations Fraction of copy",
                title: "Mutation Count vs Copy Number Alterations",
                fileName: "",
                xTitleHelp: "",
                yTitleHelp: ""
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
        
        ScatterPlots.init(scatterPlotOptions, scatterPlotArr, scatterPlotDataAttr);


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
        /*
        $('#study-view-selectChartType').change(function(){

            if($('#study-view-selectChartType').find(":selected").val() !== "" && $('#study-view-selectAttr').find(":selected").val())
                $('#study-view-add-chart-button').removeAttr('disabled');
        });
        */
        $('.study-view-dc-chart-change').click(function(e){
            //var tagID = $(this).parent().parent().attr("id");
            var valueA = $(this).parent().parent().attr("value").split(',');
            var attrID = valueA[0];
            var tmpChartType = valueA[2];
            
            if($('#tmpChartSelection').length > 0)
                $('#tmpChartSelection').remove()
            
            var positionX = $(this).position().left+5;
            var positionY = $(this).position().top+20;

            if(positionX + 150 > $(this).width())
                positionX -= 150;
            
            var div = $("<div />");
            div.attr({id: 'tmpChartSelection'});
            div.css({
                position:'absolute', 
                top: positionY, 
                left: positionX,
                'z-index':99,
                width:'150px', 
                float: 'center',
                'background-color': 'white',
                'border-radius': '10px',
                '-webkit-border-radius': '10px',
                '-moz-border-radius': '10px',
                'border-style': 'solid' , 
                'border-width': '1px', 
                'border-color':'lightgrey'})
            
            var chartType = varType[attrID].split(',');
            div.append("<div style=\"width:100%;height:20px; color:grey;\">Select Chart Type<span style='float:right; cursor:pointer;margin-right:10px' id='change-tmpClose'>X</span></div><hr / >");
            var tmpDiv = $("<div />");
            tmpDiv.css({width:'100%'});    
            $.each(chartType, function(key, value) {
                if(value != tmpChartType){
                    var tmpButton = $("<input />")
                    tmpButton.attr({value:value+' chart', type:'button', class:'study-view-dc-chart-change-button' })
                            .css({float:'center', 'margin-bottom':'10px', height: '10px'});
                    tmpDiv.append(tmpButton);               
                    tmpDiv.append('<br />');
                }
            });
            
            div.append(tmpDiv);
            $(this).parent().append(div);
            
            $('#change-tmpClose').click(function(event){
                var id = $(this).parent().parent().attr("id");
                $("#"+id).remove(); 
            });   
            
            $('.study-view-dc-chart-change-button').click(function(e){
                var id = $(this).parent().parent().parent().parent().attr("id");
                var valueA = $(this).parent().parent().parent().parent().attr("value").split(',');
                var attrID = valueA[0];
                var attrName = valueA[1];
                var valueB = $(this).val().split(' ');
                var changeToChartType = valueB[0];
                $("#"+id).remove("svg"); 
                varChart[HTMLtagsMapUID[id]].filterAll();
                dc.redrawAll();
                dc.deregisterChart(varChart[HTMLtagsMapUID[id]]);
                
                if(changeToChartType == 'pie')
                    initPieChart(HTMLtagsMapUID[id],'study-view-dc-chart study-view-pie-chart',attrID,attrName);
                else if(changeToChartType == 'row')
                    initRowChart(HTMLtagsMapUID[id],'study-view-dc-chart study-view-row-chart',attrID,attrName,rowKeys);
                else if(changeToChartType == 'bar')
                    initBarChart(HTMLtagsMapUID[id],'study-view-dc-chart study-view-bar-chart',attrID,attrName,distanceMinMaxArray);
                
                msnry.destroy();
                var container = document.querySelector('#study-view-charts');
                msnry = new Masonry( container, {
                  columnWidth: 190,
                  itemSelector: '.study-view-dc-chart',
                  gutter:1
                });

                varChart[HTMLtagsMapUID[id]].render();
                msnry.layout();
                
                var id1 = $(this).parent().parent().attr("id");
                $("#"+id1).remove(); 
            });
        });

        $('.study-view-dc-chart-delete').click(function(event){
                var id = $(this).parent().parent().attr("id");
                var valueA = $(this).parent().parent().attr("value").split(',');
                var attrID = valueA[0];
                var attrName = valueA[1];
                $("div").remove("#" + id); 
                varChart[HTMLtagsMapUID[id]].filterAll();
                dc.redrawAll();
                dc.deregisterChart(varChart[HTMLtagsMapUID[id]]);
                msnry.layout();
                $('#study-view-selectAttr')
                    .append($("<option></option>")
                        .attr("value",attrID)
                        .text(attrName));
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
                initPieChart(chartTmpID,'study-view-dc-chart study-view-pie-chart',selectedAttr,selectedAttrDisplay);       
            }else if(selectedChartType == 'row'){
                initRowChart(chartTmpID,'study-view-dc-chart study-view-row-chart',selectedAttr,selectedAttrDisplay,rowKeys);
            }else{
                initBarChart(chartTmpID,'study-view-dc-chart study-view-bar-chart',selectedAttr,selectedAttrDisplay,distanceMinMaxArray);                
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
                $("div").remove("#" + id); 
                varChart[HTMLtagsMapUID[id]].filterAll();
                dc.redrawAll();
                dc.deregisterChart(varChart[HTMLtagsMapUID[id]]);
                msnry.layout();
                $('#study-view-selectAttr')
                    .append($("<option></option>")
                        .attr("value",attrID)
                        .text(attrName));
            });            
            
            $('#study-view-dc-chart-'+ chartTmpID +' img.study-view-dc-chart-change').click(function(e){
                //var tagID = $(this).parent().parent().attr("id");
                var valueA = $(this).parent().parent().attr("value").split(',');
                var attrID = valueA[0];
                var tmpChartType = valueA[2];

                if($('#tmpChartSelection').length > 0)
                    $('#tmpChartSelection').remove()

                var positionX = $(this).position().left+5;
                var positionY = $(this).position().top+20;

                if(positionX + 150 > $(this).width())
                    positionX -= 150;

                var div = $("<div />");
                div.attr({id: 'tmpChartSelection'});
                div.css({
                    position:'absolute', 
                    top: positionY, 
                    left: positionX,
                    'z-index':99,
                    width:'150px', 
                    float: 'center',
                    'background-color': 'white',
                    'border-radius': '10px',
                    '-webkit-border-radius': '10px',
                    '-moz-border-radius': '10px',
                    'border-style': 'solid' , 
                    'border-width': '1px', 
                    'border-color':'lightgrey'})

                var chartType = varType[attrID].split(',');
                div.append("<div style=\"width:100%;height:20px; color:grey;\">Select Chart Type<span style='float:right; cursor:pointer;margin-right:10px' id='change-tmpClose'>X</span></div><hr / >");
                var tmpDiv = $("<div />");
                tmpDiv.css({width:'100%'});    
                $.each(chartType, function(key, value) {
                    if(value != tmpChartType){
                        var tmpButton = $("<input />")
                        tmpButton.attr({value:value+' chart', type:'button', class:'study-view-dc-chart-change-button' })
                                .css({float:'center', 'margin-bottom':'10px', height: '10px'});
                        tmpDiv.append(tmpButton);               
                        tmpDiv.append('<br />');
                    }
                });

                div.append(tmpDiv);
                $(this).parent().append(div);

                $('#change-tmpClose').click(function(event){
                    var id = $(this).parent().parent().attr("id");
                    $("#"+id).remove(); 
                });   

                $('.study-view-dc-chart-change-button').click(function(e){
                    var id = $(this).parent().parent().parent().parent().attr("id");
                    var valueA = $(this).parent().parent().parent().parent().attr("value").split(',');
                    var attrID = valueA[0];
                    var attrName = valueA[1];
                    var valueB = $(this).val().split(' ');
                    var changeToChartType = valueB[0];
                    $("#"+id).remove("svg"); 
                    varChart[HTMLtagsMapUID[id]].filterAll();
                    dc.redrawAll();
                    dc.deregisterChart(varChart[HTMLtagsMapUID[id]]);

                    if(changeToChartType == 'pie')
                        initPieChart(HTMLtagsMapUID[id],'study-view-dc-chart study-view-pie-chart',attrID,attrName);
                    else if(changeToChartType == 'row')
                        initRowChart(HTMLtagsMapUID[id],'study-view-dc-chart study-view-row-chart',attrID,attrName,rowKeys);
                    else if(changeToChartType == 'bar')
                        initBarChart(HTMLtagsMapUID[id],'study-view-dc-chart study-view-bar-chart',attrID,attrName,distanceMinMaxArray);
                    
                    msnry.destroy();
                    var container = document.querySelector('#study-view-charts');
                    msnry = new Masonry( container, {
                      columnWidth: 190,
                      itemSelector: '.study-view-dc-chart',
                      gutter:1
                    });

                    varChart[HTMLtagsMapUID[id]].render();
                    msnry.layout();

                    var id1 = $(this).parent().parent().attr("id");
                    $("#"+id1).remove(); 
                });
            });
        });
        
        return varName;
    }

    function initPieChart(_chartID,_className,_selectedAttr,_selectedAttrDisplay) {
        if($("#study-view-dc-chart-" + _chartID).length > 0){
            $("#study-view-dc-chart-" + _chartID).attr({value: _selectedAttr + "," + _selectedAttrDisplay + ",pie",class:_className});
        }else
            $("#study-view-charts").append("<div id=\"study-view-dc-chart-" + _chartID + "\" class='" + _className + "'  value='"+ _selectedAttr + "," + _selectedAttrDisplay + ",pie'><div style='width:100%; float:left'><pieH4>" + _selectedAttrDisplay + "<a class='reset' href='javascript:varChart[" + _chartID + "].filterAll();dc.redrawAll();' style='display: none;'>  reset</a></pieH4><span class='study-view-dc-chart-delete'>x</span><img src='images/more_12px.jpg' title='Change Chart Type' class='study-view-dc-chart-change'></img></div></div>");
        
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
    
    function initRowChart(_chartID,_className,_selectedAttr,_selectedAttrDisplay,_rowKeys) {
        
        var rowChartHeight = _rowKeys[_selectedAttr].length * 25 +50;            
        var keyMaxLength = 0;
        var chartWidth = 0;
        for(var i=0 ; i < _rowKeys[_selectedAttr].length ; i++){
            if(_rowKeys[_selectedAttr][i].length > keyMaxLength)
                keyMaxLength = _rowKeys[_selectedAttr][i].length;
        }
        
        if($("#study-view-dc-chart-" + _chartID).length > 0){
            $("#study-view-dc-chart-" + _chartID).attr({
                value: _selectedAttr + "," + _selectedAttrDisplay + ",row"
            });
            if(keyMaxLength > 30){
                $("#study-view-dc-chart-" + _chartID).attr({
                    class: _className + " w3"
                });
            }else if(keyMaxLength > 20){
                $("#study-view-dc-chart-" + _chartID).attr({
                    class: _className + " w2"
                });
            }else{
                $("#study-view-dc-chart-" + _chartID).attr({
                    class: _className + " w1"
                });
            } 
        }else{
            if(keyMaxLength > 30){
                $("#study-view-charts").append("<div id=\"study-view-dc-chart-" + _chartID + "\" class='" + _className + " w3' value='" + _selectedAttr + "," + _selectedAttrDisplay + ",row'><div style='width:100%; float:left'><pieH4>" + _selectedAttrDisplay + "<a class='reset' href='javascript:varChart[" + _chartID + "].filterAll();dc.redrawAll();' style='display: none;'>  reset</a></pieH4><span class='study-view-dc-chart-delete'>x</span><img src='images/more_12px.jpg' title='Change Chart Type' class='study-view-dc-chart-change'></img></div></div>");
            }else if(keyMaxLength > 20){
                $("#study-view-charts").append("<div id=\"study-view-dc-chart-" + _chartID + "\" class='" + _className + " w2' value='" + _selectedAttr + "," + _selectedAttrDisplay + ",row'><div style='width:100%; float:left'><pieH4>" + _selectedAttrDisplay + "<a class='reset' href='javascript:varChart[" + _chartID + "].filterAll();dc.redrawAll();' style='display: none;'>  reset</a></pieH4><span class='study-view-dc-chart-delete'>x</span><img src='images/more_12px.jpg' title='Change Chart Type' class='study-view-dc-chart-change'></img></div></div>");
            }else{
                $("#study-view-charts").append("<div id=\"study-view-dc-chart-" + _chartID + "\" class='" + _className + " w1' value='" + _selectedAttr + "," + _selectedAttrDisplay + ",row'><div style='width:100%; float:left'><pieH4>" + _selectedAttrDisplay + "<a class='reset' href='javascript:varChart[" + _chartID + "].filterAll();dc.redrawAll();' style='display: none;'>  reset</a></pieH4><span class='study-view-dc-chart-delete'>x</span><img src='images/more_12px.jpg' title='Change Chart Type' class='study-view-dc-chart-change'></img></div></div>");
            } 
        }
        
        var offsetX = 10;
        if(keyMaxLength > 30){
            chartWidth = 560;
            offsetX= 450;           
        }else if(keyMaxLength > 20){
            chartWidth = 370;
            offsetX= 250;
        }else{
            chartWidth = 180;  
            offsetX= 100;         
        }
        
        varChart[_chartID] = "";
        varCluster[_chartID] = "";
        varGroup[_chartID] = "";
        
        varChart[_chartID] = dc.rowChart("#study-view-dc-chart-" + _chartID);
        varCluster[_chartID] = ndx.dimension(function (d) {
            if(!d[_selectedAttr])
                return "NA";
            return d[_selectedAttr];
        });
        varGroup[_chartID] = varCluster[_chartID].group();

        varChart[_chartID]
        .width(chartWidth)
        .margins({top: 10, right: 10, bottom: 20, left: 10})
        .height(rowChartHeight)
        .labelOffsetX(offsetX)
        .dimension(varCluster[_chartID])
        .group(varGroup[_chartID])
        .transitionDuration(1200)
        .elasticX(false)
        .ordinalColors(chartColors)
        .label(function (d) {
            if(d.key.length > 10)
                return d.key.substring(0,9) + "...";
            else
                return d.key;
        });
        
        
        varChart[_chartID].xAxis().ticks(4);
        varChart[_chartID].xAxis().tickFormat(d3.format("d"));
    }
    
    function initBarChart(_chartID,_className,_selectedAttr,_selectedAttrDisplay,distanceMinMaxArray) {
        if($("#study-view-dc-chart-" + _chartID).length > 0){
            $("#study-view-dc-chart-" + _chartID).attr({value: _selectedAttr + "," + _selectedAttrDisplay + ",bar",class:_className});
        }else
            $("#study-view-charts").append("<div id=\"study-view-dc-chart-" + _chartID + "\" class='"+ _className +"' value='" + _selectedAttr + "," + _selectedAttrDisplay + ",bar'><div style='width:100%; float:left'><pieH4>" + _selectedAttrDisplay + "<a class='reset' href='javascript:varChart[" + _chartID + "].filterAll();dc.redrawAll();' style='display: none;'>  reset</a></pieH4><span class='study-view-dc-chart-delete'>x</span><img src='images/more_12px.jpg' title='Change Chart Type' class='study-view-dc-chart-change'></img></div></div>");
        
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
            .width(560)
            .height(180)
            .margins({top: 10, right: 10, bottom: 30, left: 40})
            .dimension(varCluster[_chartID])
            .group(varGroup[_chartID])
            .gap(5)
            .centerBar(true)
            .elasticY(false)
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

    function initDataTable(varName) {
        dataTableDC = dc.dataTableDataOnly("#dataTable","group1");
        var CASEID = ndx.dimension(function (d) {
                return d.CASE_ID;
        });	
        dc.dataCount(".dc-data-count")
                                .dimension(ndx)
                                .group(all);


        var dataTableColumns = [];

        for(var i=0; i< varChart.length ; i++){
            dataTableColumns[i] = function (d) {
                return d[varName[i]];
            };
        }


        dataTableDC
        .dimension(CASEID)
        .group(function (d) {
                return 1 ;
        })
        .columns([
                function (d) {
                    return d.CASE_ID;
                },
                function (d) {
                    return d.SUBTYPE;
                },
                function (d) {
                    return d.GENDER;
                },
                function (d) {
                    return d.AGE;
                },
                function (d) {
                    return d.TUMOR_STAGE_2009;
                },
                function (d) {
                    return d.HISTOLOGY;
                },
                function (d) {
                    return d.TUMOR_GRADE;
                },
                function (d) {
                    return d.MSI_STATUS_7_MARKER_CALL;
                },
                function (d) {
                    return d.MSI_STATUS_5_MARKER_CALL;
                },
                function (d) {
                    return d.DATA_MAF;
                },
                function (d) {
                    return d.DATA_GISTIC;
                },
                function (d) {
                    return d.DATA_RNASEQ;
                },
                function (d) {
                    return d.DATA_CORE_SAMPLE;
                },
                function (d) {
                    return d.MRNA_EXPRESSION_CLUSTER;
                },
                function (d) {
                    return d.METHYLATION_CLUSTER;
                },
                function (d) {
                    return d.MLH1_SILENCING;
                },
                function (d) {
                    return d.CNA_CLUSTER_K4;
                },
                function (d) {
                    return d.MUTATION_RATE_CLUSTER;
                },
                function (d) {
                    return d.MICRO_RNA_CLUSTER;
                },
                function (d) {
                    return d.MICRO_RNA_SCORE;
                },
                function (d) {
                    return d.OS_STATUS;
                },
                function (d) {
                    return d.OS_MONTHS;
                },
                function (d) {
                    return d.DFS_STATUS;
                },
                function (d) {
                    return d.DFS_MONTHS;
                },
                function (d) {
                    return d.MUTATION_COUNT;
                },
                function (d) {
                    return d.COPY_NUMBER_ALTERATIONS;
                }
        ])
        .size(2000)
        .sortBy(function (d) {
                return d.CASE_ID;
        })
        .transitionDuration(1200);
    }
    
    function restyle(columnNameSelected,columnNameTotal) {
        var dataTable1 = $('#dataTable').dataTable({
            "sScrollX": "1200px",
            "sScrollY": "300px",
            "bPaginate": false,
            "bFilter":true,
            "bScrollCollapse": true
        });

        var keyIndex = [];
        for(var i =0 ; i< columnNameSelected.length ; i++){
            var key = columnNameTotal.indexOf(columnNameSelected[i]);
            if(key>=0){
                keyIndex.push(key);
            }
        }
        for(var i =0 ; i< columnNameTotal.length ; i++){
            if(keyIndex.indexOf(i) === -1) {
                removeKeyIndex.push(i);
                dataTable1.fnSetColumnVis(i,false);
            }
        }
        $("#dataTable_filter label input").attr("value","");
        $('#study-view-dataTable-header').click(function(){
            if($("#dataTable_filter label input").val() !== ""){			
                    console.log("Inside...1");

                    var items=[];
                    $('#dataTable>tbody>tr>td:nth-child(1)').each( function(){
                       items.push( $(this).text() );       
                    });
                    var items = $.unique( items );
                    dataTableDC.filter(null);
                    dataTableDC.filter([items]);
                    dc.redrawAll();
            }else{
                    dataTableDC.filter(null);
                    dc.redrawAll();
            }
        });
        $('#study-view-dataTable-updateTable').click(function(){
            dc.redrawAllDataTable("group1"); 
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
    
    function getParameterByName(name) {
        name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
        var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
            results = regex.exec(location.search);
        return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
    }

    function selectedCol(col) {
        return col.toLowerCase().match(/(^age)|(gender)|(os_status)|(os_months)|(dfs_status)|(dfs_months)|(race)|(ethnicity)|(.*grade.*)|(.*stage.*)|(histology)|(tumor_type)|(subtype)|(tumor_site)|(.*score.*)|(mutation_count)|(copy_number_alterations)/);
    }

    return {
        init: function(o,data){
            initParameters(o);
            var columnNameSelected = initCharts(data);
            columnNameSelected.unshift("CASE_ID");
            restyle(columnNameSelected,columnNameTotal);
        }
    };
})();