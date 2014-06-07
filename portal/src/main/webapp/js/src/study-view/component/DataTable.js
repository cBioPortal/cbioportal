/*
 * DataTable component.
 * 
 * @param _data -- TWO PROPERTIES
 *                 attr: data attributes, each attribute is an object which
 *                       include attr_id, datatype, description and dispaly_name
 *                 arr: data contents, each object of this array stand for one
 *                      case. Each case should include all attributes as key and
 *                      their relative value. If attribute of this case doesn't
 *                      exist, the NA value will still give to this attribute.
 *                  
 * @interface: getDataTable -- return DataTable Object.
 * @interface: updateTable -- giving filteredResultList, the dateTable will be
 *                            refreshed.
 * @interface: rowClickCallback -- pass a function to dataTable. It will be
 *                                 called when one row is clicked.
 * @interface: rowShiftClickCallback -- pass a function to dataTable. It will be
 *                                      called when one row is clicked and
 *                                      ShiftKeys is pressed at same time.
 * @interface: resizeTable: will be used when width of dataTable changed.
 *     
 * @note: The 'string' sorting function of datatable has been rewrited in
 *        FnGetColumnData.js                                     
 *                                                                                                           
 * @authur: Hongxin Zhang
 * @date: Mar. 2014
 * 
 */
        
var DataTable = function() {
    
    var attr,
        arr,
        attrLength,
        arrLength,
        aoColumnsLength,
        aaDataLength,
        dataTable,
        tableId,
        dataType = [],
        dataTableNumericFilter = [],
        permenentDisabledId = [], //Define which column is perment diabled

        /*
         * This array will be updated when user select key in column selector,
         * Selected column selector will not be updated when user select other
         * selectors. When resetting datatable, this array will be empty and 
         * always get a deep copy from permenentDisabledId.
        */
        disableFiltId = [],
                            
        aoColumns = [], //DataTable Title Data
        aaData = [], //DataTable Content Data
        columnIndexMappingColumnId = [],
        noLeftColumnFlag = true,
        displayMapName = {};
    
    var rowClickCallback,
        rowShiftClickCallback;
    
    function initParam(_tableId, _data) {
        var i;
        
        tableId = _tableId;
        
        attr = _data.attr;
        arr = _data.arr;
        
        attrLength = attr.length;
        arrLength = arr.length;
        
        for( i = 0; i < attrLength; i++ ){
            if(attr[i]["datatype"] === "NUMBER"){
                dataType[attr[i]["attr_id"]] = 'allnumeric';
            }else{
                dataType[attr[i]["attr_id"]] = 'string';
            }
            dataTableNumericFilter[i] = '';
        }
        
        initColumnsTitleData();
        initContentData();
    }
    
    //Initialize aoColumns Data
    function initColumnsTitleData() {
        var i,
            _permenentDisabledTitles =  ['CASE ID', 
                                        'PATIENT_ID', 
                                        'Patient Identifier'];
        
        aoColumns.length = 0;
        
        aoColumns.push({sTitle:"CASE ID",sType:'string',sClass:'nowrap'});
        displayMapName['CASE ID'] = 'CASE_ID';
        for( i = 0; i < attr.length; i++ ){
            if( attr[i].attr_id !== 'CASE_ID' ){
                var _tmp = {};
                
                if(attr[i].attr_id === 'COPY_NUMBER_ALTERATIONS'){
                    _tmp.sTitle = 'CNA';
                }else{
                    _tmp.sTitle = attr[i].display_name;
                }
                displayMapName[_tmp.sTitle] = attr[i].attr_id;
                _tmp.sType = dataType[attr[i].attr_id];
                aoColumns.push(_tmp);
            }
        }
        
        aoColumnsLength = aoColumns.length;
        
        //Sort table columns based on display name. If title is in
        //permenentDisabledTitles, put it to front of table.
        aoColumns.sort(function(a, b) {
            //Case ID is the first element of permenentDisabledTitles,
            //It will always be treated as first column.
            //
            //TODO: Need second sorting function for sorting pre disabled
            //predisabled columns if needed.
            if(_permenentDisabledTitles.indexOf(a.sTitle) !== -1) {
                return -1;
            }else if(_permenentDisabledTitles.indexOf(b.sTitle) !== -1) {
                return 1;
            }else{
                var _a = a.sTitle.toLowerCase(),
                    _b = b.sTitle.toLowerCase();
                    
                if(_a < _b) {
                    return -1;
                }else {
                    return 1;
                }
            }
        });
        
        for( var i = 0; i < aoColumnsLength; i++) {
            if(_permenentDisabledTitles.indexOf(aoColumns[i].sTitle) !== -1) {
                permenentDisabledId.push(i);
            }
        }
        disableFiltId = jQuery.extend(true, [], permenentDisabledId);
    }
    
    //Initialize aaData Data
    function initContentData() {
        var _arrKeys = Object.keys(arr),
            _arrKeysLength = _arrKeys.length;
        aaData.length = 0;
        for ( var i = 0; i< _arrKeysLength; i++) {
            var _key = _arrKeys[i],
                _value = arr[_key],
                _aoColumnsLength = aoColumns.length;
            
            aaData[_key] = [];
            
            for ( var j = 0; j < _aoColumnsLength; j++) {
                var _valueAo = aoColumns[j],
                    _selectedString,
                    _specialCharLength,
                    _tmpValue ='',
                    _specialChar = ['(',')','/','?','+'];

                if(_valueAo.sTitle === 'CNA'){
                    _tmpValue = _value['COPY_NUMBER_ALTERATIONS'];                
                }else if ( _valueAo.sTitle === 'COMPLETE (ACGH, MRNA, SEQUENCING)'){
                    _tmpValue = _value[_valueAo.sTitle];
                }else if ( _valueAo.sTitle === 'CASE ID'){
                    _tmpValue = "<a href='"
                            + cbio.util.getLinkToSampleView(StudyViewParams.params.studyId, _value['CASE_ID'])
                            + "' target='_blank'><span style='color: #2986e2'>" + 
                    _value['CASE_ID'] + "</span></a></strong>";
                }else if ( (_valueAo.sTitle === 'Patient Identifier' || _valueAo.sTitle === 'PATIENT_ID') && _value['PATIENT_ID'] !== 'NA'){
                    _tmpValue = "<a href='"
                            + cbio.util.getLinkToPatientView(StudyViewParams.params.studyId, _value['PATIENT_ID'])
                            + "' target='_blank'><span style='color: #2986e2'>" + _value['PATIENT_ID'] + "</span></a></strong>";
                }else{
                    _tmpValue = _value[displayMapName[_valueAo.sTitle]];
                }
                if(!isNaN(_tmpValue) && (_tmpValue % 1 !== 0)){
                    _tmpValue = cbio.util.toPrecision(Number(_tmpValue),3,0.01);
                }
                
                
                _selectedString = _tmpValue.toString();
                _specialCharLength = _specialChar.length;
                
                //Only usded for columns without URL link
                if ( _valueAo.sTitle !== 'CASE ID' && _valueAo.sTitle !== 'Patient Identifier' && _valueAo.sTitle !== 'PATIENT_ID' ){
                    for( var z = 0; z < _specialCharLength; z++){
                        if(_selectedString.indexOf(_specialChar[z]) !== -1){
                            var _re = new RegExp("\\" + _specialChar[z], "g");
                            
                            _selectedString = _selectedString.replace(_re, _specialChar[z] + " ");
                        } 
                    }
                }
                
                if(_selectedString === 'NA'){
                    _selectedString = '';
                }
                aaData[_key].push(_selectedString);
            }
        }
        aaDataLength = aaData.length;
    }
    
    //Initialize the basic dataTable component by using jquery.dataTables.min.js
    function initDataTable() {
        dataTable = $('#' + tableId).dataTable({
            "sScrollX": "1200px",
            "sScrollY": "500px",
            "bPaginate": false,
            "bScrollCollapse": true,
            "aoColumns": aoColumns,
            "aaData": aaData,
            "bJQueryUI": true,
            "sDom": '<"H"Ci<"dataTableReset">f>rt',
            "oLanguage": {
                "sInfo": "&nbsp;&nbsp;Showing _TOTAL_ samples&nbsp;",
                "sInfoFiltered": "(filtered from _MAX_ samples)",
            },
            "fnDrawCallback": function(oSettings){
                //Only used when select or unselect showed columns
                if($(".ColVis_collection.TableTools_collection").css('display') === 'block'){
                    var _currentIndex= 0;
                    
                    columnIndexMappingColumnId = {};
                    //Recalculate the column and Id mapping
                    $.each(dataTable.fnSettings().aoColumns, function(c){
                        if(dataTable.fnSettings().aoColumns[c].bVisible === true){
                            columnIndexMappingColumnId[_currentIndex] = c;
                            _currentIndex++;
                        }
                    });
                    $("#clinical_table_filter label input").val("");
                    $.fn.dataTableExt.afnFiltering = [];
                    disableFiltId = jQuery.extend(true, [], permenentDisabledId);     
                    refreshSelectionInDataTable();
                    resizeLeftColumn();    
                    $(".dataTableReset span").css('display','none');
                }
            }
        }).fnSetFilteringDelay();
    }
    
    //Add th tags based on number of attributes
    function initDataTableTfoot() {
        for( var i = 0; i < aoColumnsLength; i++ ){
            $("#" + tableId+" tfoot tr").append("<th></th>");
            columnIndexMappingColumnId[i] = i;
        }
    }
    
    //Add all HTML events by using JQUERY
    function addEvents() {
        
        $(".ColVis_MasterButton").click(function() {
            $('.ColVis_collection.TableTools_collection')
                .find('button')
                .first()
                .prop('disabled',true);
        
            $('.ColVis_collection.TableTools_collection')
                .find('button')
                .first()
                .find('input')
                .prop('disabled',true);
         
        });
        
        $(".dataTableReset")
            .append("<a><span class='hidden' title='Reset Chart'>RESET</span></a>");
    
        $("#clinical-data-table-div .dataTableReset span").click(function(){
            $(this).css({'cursor': 'wait'});
            $("#clinical_table_filter label input").val("");
            $.fn.dataTableExt.afnFiltering = [];
            updateTable([]);          
            refreshSelectionInDataTable();
            dataTable.fnAdjustColumnSizing();
            resizeLeftColumn();
            showDataTableReset();
            $(this).css({'cursor': 'default'});
        });
        
        var inputDelay = (function(){
            var timer = 0;
            return function(callback, ms){
              clearTimeout (timer);
              timer = setTimeout(callback, ms);
            };
        })();
        
        $("#clinical_table_filter label input").keyup(function() {
            inputDelay(function(){
                showDataTableReset(dataTable);
                refreshSelectionInDataTable();
                resizeLeftColumn();
            }, 500 );
        });
        
        if ($('#study-tab-clinical-a').hasClass('selected')) {
            dataTable.fnAdjustColumnSizing();
            $('#study-tab-clinical-a').addClass("tab-clicked")
        }
        
        $('#study-tab-clinical-a').click(function(){
            if (!$(this).hasClass("tab-clicked")) {
                //First time: adjust the width of data table;
                dataTable.fnAdjustColumnSizing();
                if($("#" + tableId).width() > 1200) {
                    noLeftColumnFlag = false;
                    new FixedColumns(dataTable);
                    $(".DTFC_LeftBodyLiner").css("overflow-y","hidden");
                    //$(".dataTables_scroll").css("overflow-x","scroll");
                    $(".DTFC_LeftHeadWrapper").css("background-color","white");
                    $(".DTFC_LeftFootWrapper").css('background-color','white');

                    //After resizing left column, the width of DTFC_LeftWrapper is different
                    //with width DTFC_LeftBodyLiner, need to rewrite the width of
                    //DTFC_LeftBodyLiner width
                    var _widthLeftWrapper = $('.DTFC_LeftWrapper').width();
                    $('.DTFC_LeftBodyLiner').css('width', _widthLeftWrapper+4);
                }
                //dataTable.fnFilter('', 0);
                showDataTableReset();
                refreshSelectionInDataTable();
                //Sencond time: adjust the width of table foot;
                dataTable.fnAdjustColumnSizing();
                if(!noLeftColumnFlag) {
                    resizeLeftColumn();
                    $(window).resize();
                }
                $(this).addClass("tab-clicked");
            }
        });
        
        //Set mouse down timeout to seperate click and mousedown and hold
//        var _timeOut = 0;
//        $("#" + tableId+" tbody").mousedown(function(event){
//            var _d = new Date();
//            _timeOut= _d.getTime();
//            //Tried couple times, only prevent default function in here works.
//            //The shiftKey click function should be originally combined with
//            //mousedown function.
//            if(event.shiftKey){
//                event.preventDefault();
//            }
//        }).bind('mouseup', function(event) {
//            var _d = new Date();
//            _timeOut= _d.getTime()-_timeOut;
//            
//            if(_timeOut < 500){
//                mouseDownFunc(event);
//            }
//        });;
        
//        function mouseDownFunc(event) {
//            var _selectedRowCaseId = [],
//                _deSelect = false;
//        
//            if(event.shiftKey){
//                event.preventDefault();
//
//                if($(event.target.parentNode).hasClass('row_selected')){
//                    $(event.target.parentNode).removeClass('row_selected');
//                    if($(event.target.parentNode).hasClass('odd')){
//                       $(event.target.parentNode).css('background-color','#E2E4FF'); 
//                    }else{
//                        $(event.target.parentNode).css('background-color','white');
//                    }
//                }else{
//                    $(event.target.parentNode).addClass('row_selected');
//                    $(event.target.parentNode).css('background-color','lightgray');
//                }
//                
//                _selectedRowCaseId = getRowSelectedCases();
//                rowShiftClickCallback(_selectedRowCaseId);
//                
//            }else{
//                if($(event.target.parentNode).hasClass('row_selected')){
//                    $(event.target.parentNode).removeClass('row_selected');
//                    if($(event.target.parentNode).hasClass('odd')){
//                       $(event.target.parentNode).css('background-color','#E2E4FF'); 
//                    }else{
//                        $(event.target.parentNode).css('background-color','white');
//                    }
//                    _deSelect = true;
//                }else{
//                    $(dataTable.fnSettings().aoData).each(function (){
//                        if($(this.nTr).hasClass('row_selected')){
//                            $(this.nTr).removeClass('row_selected');
//                            if($(this.nTr).hasClass('odd')){
//                               $(this.nTr).css('background-color','#E2E4FF'); 
//                            }else{
//                                $(this.nTr).css('background-color','white');
//                            }
//                        }
//                    });
//
//                    $(event.target.parentNode).addClass('row_selected');
//                    $(event.target.parentNode).css('background-color','lightgray');
//                }
//                
//                _selectedRowCaseId = getRowSelectedCases();
//                rowClickCallback(_deSelect, _selectedRowCaseId);
//            }
//        }
    
    }
    
    function getRowSelectedCases() {
        var _selectedRowCaseIds = [],
            _returnValue = fnGetSelected(),
            _returnValueLength = _returnValue.length;
        
        for( var i = 0; i < _returnValueLength; i++ ){
            _selectedRowCaseIds.push($(_returnValue[i]).find('td').first().text());
        }
        
        return _selectedRowCaseIds;
    }
    
    function updateTable(_exceptionColumns){
        var _oSettings = dataTable.fnSettings();
        
        for(var iCol = 0; iCol < _oSettings.aoPreSearchCols.length; iCol++) {
            if(_exceptionColumns.indexOf(iCol) === -1){
                _oSettings.aoPreSearchCols[ iCol ].sSearch = '';
            }
        }
        _oSettings.oPreviousSearch.sSearch = '';
        dataTable.fnDraw();
    }
    
    //Return the selected nodes
    function fnGetSelected(){
        var i,
            _aReturn = [],
            _aTrs = dataTable.fnGetNodes();

        for ( i = 0; i < _aTrs.length; i++ ){
                if ( $(_aTrs[i]).hasClass('row_selected') ){
                        _aReturn.push( _aTrs[i] );
                }
        }
        return _aReturn;
    }
    
    //Create Regular Selector or Numeric Selector based on data type.
    function fnCreateSelect( aData, index, _this ){
        var _isNumericArray = true,
            _hasNullValue = false,
            _numOfKeys = aData.length,
            _width = '100%';
//            _width = $(_this).width() - 4;
    
        for(var i=0;i<aData.length;i++){
            if(isNaN(aData[i])){
                if(aData[i] !== 'NA'){
                    _isNumericArray = false;
                    break;
                }else{
                    _hasNullValue = true;
                }
            }
        }
        
        if(_isNumericArray && _hasNullValue){
            var _index = aData.indexOf("NA");
            if (_index > -1) {
                aData.splice(_index, 1);
            }
        }
        
        if(_isNumericArray){            
            aData.sort(function(a,b) {
                return Number(a) - Number(b);
            });
        }else{
            aData.sort();
        }
        
        if(!_isNumericArray || aData.length === 0 || (_isNumericArray && _numOfKeys < 10)){
            var r='<select style="width: '+_width+'px"><option value=""></option>', i, iLen=aData.length;
            if(iLen === 0){
                return "";
            }else{
                for ( i=0 ; i<iLen ; i++ )
                {
                    r += '<option value="'+aData[i]+'">'+aData[i]+'</option>';
                }
                return r+'</select>';
            }
        }else{
            var _min = aData[0],
                _max = aData[aData.length-1],
                _x1 = 15,
                _x2 = 65,
                _textH = 30,
                _resetH = 32,
                _triangelTop = 34,
                _lineH = 33,
                _triangelBottom = _triangelTop + 8,
                _fontSize = '8px';
            
            var _leftTriangelCoordinates = (_x1-5) + ","+_triangelBottom+ " "+ (_x1+5)+","+_triangelBottom+ " "+_x1+","+_triangelTop,
                _rightTriangelCoordinates = (_x2-5) + ","+_triangelBottom+ " "+ (_x2+5)+","+_triangelBottom+ " "+_x2+","+_triangelTop,
                _leftText = "x='"+_x1+"' y='"+_textH+"'",
                _rightText = "x='"+_x2+"' y='"+_textH+"'",
                _resetText = "x='"+(_x2+15)+"' y='"+_resetH+"'";
           
            var _svgLine = "<svg width='110' height='50' start='"+ _min +"' end='"+ _max +"'>" + 
                    "<g><line x1='"+ _x1 +"' y1='"+_lineH+"' x2='"+ _x2 +"' y2='"+_lineH+"' style='stroke:black;stroke-width:2' /></g>"+
                    "<g id='dataTable-"+ index +"-right' class='clickable right'><polygon points='"+_rightTriangelCoordinates+"' style='fill:grey'></polygon>"+
                    "<text "+_rightText+" fill='black' transform='rotate(-30 "+_x2+" "+_textH+")' style='font-size:"+ _fontSize +"'>"+ _max +"</text></g>" +
                    "<g id='dataTable-"+ index +"-left' class='clickable left'><polygon points='"+_leftTriangelCoordinates+"' style='fill:grey'></polygon>"+
                    "<text "+_leftText+" fill='black' transform='rotate(-30 "+_x1+" "+_textH+")' style='font-size:"+ _fontSize +"'>"+ _min +"</text></g>" + 
                    "<text "+ _resetText +" id='dataTable-"+ index +"-reset' class='clickable hidden'  fill='black' style='font-size:"+ _fontSize +"'>RESET</text>" + 
                    "</svg>";

            return _svgLine;
        }
    }
    
    //This function will be called when the dataTable has been resized
    function resizeLeftColumn(){
        if(!noLeftColumnFlag) {
            var _heightBody = $(".dataTables_scrollBody").height(),
                _heightTable = $('.dataTables_scroll').height(),
                _widthBody = $("#" + tableId + " tbody>tr:nth-child(2)>td:nth-child(1)").width();
            
            if(_widthBody === null) {
                $(".DTFC_LeftWrapper").css('display', 'none');
            }else {
                _widthBody = _widthBody + 22;
                if($("#" + tableId).width() > 1200) {
                    $(".DTFC_LeftWrapper").css('display', 'block');
                    $(".DTFC_LeftBodyLiner").height(_heightBody - 15);
                    $(".DTFC_LeftBodyWrapper").height(_heightBody - 15); 
                    $(".DTFC_LeftWrapper").width(_widthBody);
                    $(".DTFC_LeftBodyLiner").width(_widthBody);
                    $(".DTFC_LeftBodyLiner").css('background-color','white');
                }
            }
            //When selecting or unselecting columns in table of summary tab,
            //the column width will be stretched, the columns width will be changed
            //automatically, but the width of left column needs to be changed by
            //using following two statements.
            $(".DTFC_ScrollWrapper").height(_heightTable);
        }
    }
    
    function showDataTableReset( ){
        var _showedColumnNumber = dataTable.fnSettings().fnRecordsDisplay(),
            _totalColumnNumber = dataTable.fnSettings().fnRecordsTotal();
        
        if(_showedColumnNumber !== _totalColumnNumber){
            $(".dataTableReset span").css('display','block');
            $(".ColVis.TableTools").css('display','none');
        }else{
            $(".dataTableReset span").css('display','none');
            $(".ColVis.TableTools").css('display','block');
            disableFiltId = jQuery.extend(true, [], permenentDisabledId);
            refreshSelectionInDataTable();
        }
    }
    
    function updateDataTableNumericFilter(){
        var i,
            _dataTableNumericFilterLength = dataTableNumericFilter.length;
        
        $.fn.dataTableExt.afnFiltering = [];
        for( i = 0; i < _dataTableNumericFilterLength; i++ ){
            if(dataTableNumericFilter[i] !== ''){
                $.fn.dataTableExt.afnFiltering.push(dataTableNumericFilter[i]);
            }
        }
        dataTable.fnDraw();
    }
    
    function selectorDragMove() {
        var _start = Number($(this).parent().attr('start')),
            _end = Number($(this).parent().attr('end')),
            _xMoved = d3.event.x - 5,
            _lineLength = Number($(this).parent().find('line').attr('x2')) - Number($(this).parent().find('line').attr('x1'));

        if(_start > _end){
            var _tmp = _end;

            _end = _start;
            _start = _tmp;
        }

        if(_xMoved >= 0 && _xMoved <= _lineLength){
            var _text = (_end-_start) * _xMoved / _lineLength + _start;

            _text = cbio.util.toPrecision(Number(_text),3,0.1);

            if($(this).attr('id').toString().indexOf('left') !== -1){
                d3.select(this)
                    .attr("transform", "translate(" +_xMoved +",0)");
            }else{
                _xMoved -= _lineLength;
                d3.select(this)
                    .attr("transform", "translate(" +_xMoved +",0)");
            }
            $(this).find('text').text(_text);
        }
    }
    
    function selectorDragEnd() {
        var _min = Number($(this).parent().find('g.left').find('text').text()),
            _max = Number($(this).parent().find('g.right').find('text').text()),
            _id = $(this).attr('id').split('-'),
            _i = Number(_id[1]);

        if(_min > _max){
            var _tmp = _max;

            _max = _min;
            _min = _tmp;
        }

        dataTableNumericFilter[columnIndexMappingColumnId[_i]] = function( oSettings, aData, iDataIndex ) {
            var _iMin = _min,
                _iMax = _max,
                _iCurrent = aData[columnIndexMappingColumnId[_i]];

            if ( _iMin === "" && _iMax === "" ){
                    return true;
            }else if ( _iMin === "" && _iCurrent <= _iMax ){
                    return true;
            }else if ( _iMin <= _iCurrent && "" === _iMax ){
                    return true;
            }else if ( _iMin <= _iCurrent && _iCurrent <= _iMax ){
                    return true;
            }

            return false;
        };

        updateDataTableNumericFilter();
        dataTable.fnSort([ [columnIndexMappingColumnId[_i],'asc']]);
        disableFiltId.push(_i);
        showDataTableReset();
        $("#dataTable-" + _i + "-reset").css('display','block');
        refreshSelectionInDataTable();
        dataTable.fnAdjustColumnSizing();
        resizeLeftColumn();  
    }
    
    function refreshSelectionInDataTable(){
        $(".dataTables_scrollFoot tfoot th").each( function ( i ) {
            
            if(disableFiltId.indexOf(i) === -1){               
                $(this).css('z-index','1500');
                this.innerHTML = fnCreateSelect( dataTable.fnGetColumnData(columnIndexMappingColumnId[i]), i, this);
                
                var _drag = d3.behavior.drag()
                        .on("drag", selectorDragMove)
                        .on("dragend",selectorDragEnd);
                
                d3.select("#dataTable-" + i + "-left")
                                .call(_drag);
                d3.select("#dataTable-" + i + "-right")
                                .call(_drag);
                        
                $("#dataTable-" + i + "-reset").unbind('click');
                $("#dataTable-" + i + "-reset").click(function(){
                    dataTableNumericFilter[columnIndexMappingColumnId[i]] = '';
                    updateDataTableNumericFilter();
                    disableFiltId.splice(disableFiltId.indexOf(i),1);
                    showDataTableReset();
                    $("#dataTable-" + i + "-reset").css('display','none');
                    refreshSelectionInDataTable();
                    dataTable.fnAdjustColumnSizing();
                    resizeLeftColumn();  
                });
                
                $('select', this).change( function () {
                    if($(this).val() === ''){
                        dataTable.fnFilter($(this).val(), columnIndexMappingColumnId[i]);
                        disableFiltId.splice(disableFiltId.indexOf(i),1);
                    }else{
                        //Need to process special charector which can no be
                        //treated as special charector in regular expression.
                        var j,
                            _selectedString = $(this).val().toString(),
                            _specialChar = ['(',')','/','?','+'],
                            _specialCharLength = _specialChar.length;
                        for( j = 0; j < _specialCharLength; j++){
                            if(_selectedString.indexOf(_specialChar[j]) !== -1){
                                var re = new RegExp("\\" + _specialChar[j],"g");
                                _selectedString = _selectedString.replace(re ,"\\"+ _specialChar[j]);
                            } 
                        }
                        dataTable.fnFilter("^"+_selectedString+"$", columnIndexMappingColumnId[i], true);
                        disableFiltId.push(i);
                    }
                    
                    showDataTableReset();
                    refreshSelectionInDataTable();
                    dataTable.fnAdjustColumnSizing();
                    resizeLeftColumn();
                });
            }
        });
    }
    
    function deleteChartResetDataTable(_filteredResult) {
        var _filterArray = [];

        for(var i=0 ; i<_filteredResult.length ; i++){
            _filterArray.push(_filteredResult[i].CASE_ID);
        }

        $.fn.dataTableExt.afnFiltering = [function( oSettings, aData, iDataIndex ) {
            var _data = aData[0],
                _dataContent = $(_data).text();
                
            if ( _filterArray.indexOf(_dataContent) !== -1){
                return true;
            }
            return false;
        }];
        dataTable.fnDraw();
    }
    
    //Will be used after initializing datatable. This function is called from
    //StudyViewSummaryTabController
    function resizeTable(){
        //Before resize data table, the window should be showed first
//        $('#dc-plots-loading-wait').hide();
//        $('#study-view-main').show();
         
        refreshSelectionInDataTable();
        
        //Resize column size first, then add left column
//        dataTable.fnAdjustColumnSizing();
//        console.log($("#" + tableId).width());
//        if($("#" + tableId).width() > 1200) {
            new FixedColumns(dataTable);

            //Have to add in there
            $(".DTFC_LeftBodyLiner").css("overflow-y","hidden");
            //$(".dataTables_scroll").css("overflow-x","scroll");
            $(".DTFC_LeftHeadWrapper").css("background-color","white");
            $(".DTFC_LeftFootWrapper").css('background-color','white');

            //After resizing left column, the width of DTFC_LeftWrapper is different
            //with width DTFC_LeftBodyLiner, need to rewrite the width of
            //DTFC_LeftBodyLiner width
            var _widthLeftWrapper = $('.DTFC_LeftWrapper').width();
            $('.DTFC_LeftBodyLiner').css('width', _widthLeftWrapper+4);//Column has table spacing
//        }else {
//            $('#clinical-data-table-div .dataTables_scrollBody').css('overflow-x', 'hidden');
//        }
        dataTable.fnAdjustColumnSizing();
//        resizeLeftColumn();
    }
    
    return {
        init: function(_tableId, _data) {
            initParam(_tableId, _data);
            initDataTableTfoot();
            initDataTable();
            //resizeTable();
            addEvents();
        },
        
        getDataTable: function() {
            return dataTable;
        },
        
        updateTable: function(_filteredResult) {
            if( $("#clinical_table_filter label input").val() !== ''){
                dataTable.fnFilter('');
            }
            deleteChartResetDataTable(_filteredResult);
             refreshSelectionInDataTable();
            dataTable.fnAdjustColumnSizing();
            $("#clinical_table_filter label input").val("");
            showDataTableReset();
            resizeLeftColumn();
        },
        
        rowClickCallback: function(_callback) {
            rowClickCallback = _callback;
        },
        
        rowShiftClickCallback: function(_callback) {
            rowShiftClickCallback = _callback;
        },
        
        resizeTable: resizeTable,
        getTableHeader: function() { return aoColumns;},
        getTableContent: function() { return aaData;}
    };
};