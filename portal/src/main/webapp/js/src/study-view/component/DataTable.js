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
        forzedLeftCol = null,
        dataTableScrollHeight = 500,
        tableId,
        tableContainerId,
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
        noLeftColumnFlag = false,                    
        aoColumns = [], //DataTable Title Data
        aaData = [], //DataTable Content Data
        columnIndexMappingColumnId = [],
        selectorData = [],
        filters = {};
    
    function initParam(_tableId, _tableContainerId, _data) {
        var i;
        
        tableId = _tableId;
        tableContainerId = _tableContainerId;
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
        selectorData = tfootData();
    }
    
    function numberBetween(value, min, max) {
        return value > min && value < max;
    }
    
    function tfootData() {
        var tfootInfo = [];
        var filteredAAData = [];
        var attrL = attr.length;
        
        for(var  i= 0; i < attrL; i++) {
            tfootInfo.push([]);
        }
        
        if($.isEmptyObject(filters)) {
            filteredAAData = aaData;
        }else {
            for(var i = 0 , aaDataL = aaData.length; i < aaDataL; i++) {
                var statisfy = true;
                for(var key in filters) {
                    var index = Number(key);
                    var keyword = "";
                    
                    if(typeof filters[key] === "object") {
                        var start = Number(filters[key]["start"]);
                        var end = Number(filters[key]["end"]);
                        if(!numberBetween(Number(aaData[i][index]), start, end)) {
                            statisfy = false;
                        }
                    }else {
                        keyword = filters[key].toString();
                        if(aaData[i][index].toString().toLowerCase() !== keyword.toString().toLowerCase()) {
                            statisfy = false;
                        }
                    }
                }
                if(statisfy) {
                    filteredAAData.push(aaData[i]);
                }
            }
        }
        
        
        for(var i = 0, filteredL = filteredAAData.length; i < filteredL; i++) {
            for(var j = 0; j < attrL; j++) {
                if(tfootInfo[j].indexOf(filteredAAData[i][j]) === -1 && filteredAAData[i][j] !== "") {
                    tfootInfo[j].push(filteredAAData[i][j]);
                }
            }
        }
        
        return tfootInfo;
    }
    
    //Initialize aoColumns Data
    function initColumnsTitleData() {
        var i,
            _permenentDisabledAttrs =  ['CASE_ID', 
                                        'PATIENT_ID'];
        
        aoColumns.length = 0;
        
        aoColumns.push({
            dataTable: {sTitle:"CASE ID",sType:'string',sClass:'nowrap'},
            fullDisplay: 'CASE ID',
            attrId: "CASE_ID"
        });
        for( i = 0; i < attr.length; i++ ){
            if( attr[i].attr_id !== 'CASE_ID' ){
                var _tmp = {};
                
                _tmp.dataTable = {};
                _tmp.attrId = attr[i].attr_id;
                
                if(attr[i].attr_id === 'COPY_NUMBER_ALTERATIONS'){
                    _tmp.dataTable.sTitle = 'CNA';
                    _tmp.fullDisplay = 'CNA';
                }else{
                    if(attr[i].display_name.toString().length > 15) {
                        _tmp.dataTable.sTitle = attr[i].display_name.toString().substring(0,15) + "...";
                    }else {
                        _tmp.dataTable.sTitle = attr[i].display_name;
                    }
                    _tmp.fullDisplay = attr[i].display_name;
                }
                _tmp.dataTable.sType = dataType[attr[i].attr_id];
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
            if(_permenentDisabledAttrs.indexOf(a.attrId) !== -1) {
                return -1;
            }else if(_permenentDisabledAttrs.indexOf(b.attrId) !== -1) {
                return 1;
            }else{
                var _a = a.dataTable.sTitle.toLowerCase(),
                    _b = b.dataTable.sTitle.toLowerCase();
                    
                if(_a < _b) {
                    return -1;
                }else {
                    return 1;
                }
            }
        });
        
        for( var i = 0; i < aoColumnsLength; i++) {
            if(_permenentDisabledAttrs.indexOf(aoColumns[i].attrId) !== -1) {
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
                var _attrId = aoColumns[j].attrId,
                    _selectedString,
                    _specialCharLength,
                    _tmpValue ='',
                    _specialChar = ['(',')','/','?','+'];

                if ( _attrId === 'CASE_ID'){
                    _tmpValue = "<a href='case.do?case_id=" + 
                    _value['CASE_ID'] + "&cancer_study_id=" +
                    StudyViewParams.params.studyId + "' target='_blank'><span style='color: #2986e2'>" + 
                    _value['CASE_ID'] + "</span></a></strong>";
                }else if ( _attrId === 'PATIENT_ID' && _value['PATIENT_ID'] !== 'NA'){
                    _tmpValue = "<a href='case.do?cancer_study_id=" +
                    StudyViewParams.params.studyId + "&patient_id="+
                    _value['PATIENT_ID'] +
                    "' target='_blank'><span style='color: #2986e2'>" + 
                    _value['PATIENT_ID'] + "</span></a></strong>";
                }else{
                    _tmpValue = _value[aoColumns[j].attrId];
                }
                if(!isNaN(_tmpValue) && (_tmpValue % 1 !== 0)){
                    _tmpValue = cbio.util.toPrecision(Number(_tmpValue),3,0.01);
                }
                
                
                _selectedString = _tmpValue.toString();
                _specialCharLength = _specialChar.length;
                
                //Only usded for columns without URL link
                if ( _attrId === 'CASE_ID' && _attrId === 'PATIENT_ID' ){
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
        var dataTableaoColumns = [];
        
        for(var i = 0; i < aoColumnsLength; i++) {
            dataTableaoColumns.push(aoColumns[i].dataTable);
        }
        
        var dataTableSettings = {
            "scrollX": "100%",
            "scrollY": dataTableScrollHeight,
            "paging": false,
            "scrollCollapse": true,
            "aoColumns": dataTableaoColumns,
            "aaData": aaData,
            "bJQueryUI": true,
            "autoWidth": true,
            "sDom": '<"H"TCi<"dataTableReset">f>rt',
            "tableTools": {
                "aButtons": [
                    {
                        "sExtends": "copy",
                        "bFooter": false
                    },
                    {
                        "sExtends": "csv",
                        "bFooter": false
                    }
                ],
                "sSwfPath": "swf/copy_csv_xls_pdf.swf"
            },
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
                    $.fn.dataTable.ext.search = [];
                    disableFiltId = jQuery.extend(true, [], permenentDisabledId); 
                    refreshSelectionInDataTable();
                    $(".dataTableReset span").css('display','none');
                }
                updateTableHeight();
                updateFrozedColStyle();
            }
        };
        
        if(arrLength > 500) {
            delete dataTableSettings.scrollY;
            dataTableSettings.paging = true;
            dataTableSettings.sPaginationType = "two_button";
            dataTableSettings.iDisplayLength = 30;
            dataTableSettings.sDom = '<"H"TpCi<"dataTableReset">f>rt';
        }
        
        dataTable = $('#' + tableId).dataTable(dataTableSettings);
        dataTable.fnSetFilteringDelay(1000);
        refreshSelectionInDataTable();
        forzedLeftCol = new $.fn.dataTable.FixedColumns( dataTable, {
            "sHeightMatch": "none"
        } );
        
        $('#' + tableId + "_wrapper .dataTables_scroll .dataTables_scrollHeadInner thead").find('th').each(function(index) {
            if(aoColumns[index].fullDisplay.toString() !== aoColumns[index].dataTable.sTitle.toString()){
                StudyViewUtil.addQtip(aoColumns[index].fullDisplay, $(this), {my:'bottom left',at:'top left', viewport: $(window)});
            }
        });
        dataTableaoColumns = null;
    }
    
    //Add th tags based on number of attributes
    function initDataTableTfoot() {
        for( var i = 0; i < aoColumnsLength; i++ ){
            $("#" + tableId+" tfoot tr").append("<td></td>");
            columnIndexMappingColumnId[i] = i;
        }
    }
    
    //Add all HTML events by using JQUERY
    function addEvents() {
        var windowResize = false;
        
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
    
        $("#" + tableContainerId + " .dataTableReset span").click(function(){
            
            $("#" + tableId + "-search input").val("");
            
            for(var key in dataTableNumericFilter) {
                dataTableNumericFilter[key] = '';
            }
            $.fn.dataTable.ext.search = [];
            var oSettings = dataTable.fnSettings();
            for(var iCol = 0; iCol < oSettings.aoPreSearchCols.length; iCol++) {
                oSettings.aoPreSearchCols[ iCol ].sSearch = '';
            }
            dataTable.fnFilter('');
            filters = {};
            disableFiltId = jQuery.extend(true, [], permenentDisabledId);
            showDataTableReset();
            refreshSelectionInDataTable();
            modifyTableStyle();
        });

        if(forzedLeftCol) {
            $(".DTFC_LeftBodyLiner").css("overflow-y","hidden");
            $(".DTFC_LeftBodyLiner").css("overflow-x","hidden");
            $(".DTFC_LeftHeadWrapper").css("background-color","white");
            $(".DTFC_LeftFootWrapper").css('background-color','white');

            //After resizing left column, the width of DTFC_LeftWrapper is different
            //with width DTFC_LeftBodyLiner, need to rewrite the width of
            //DTFC_LeftBodyLiner width
            var _widthLeftWrapper = $('.DTFC_LeftWrapper').width();
            $('.DTFC_LeftBodyLiner').css('width', _widthLeftWrapper+4);
        }
        
        $(window).resize(function(){
            if(windowResize !== false)
                clearTimeout(windowResize);
            windowResize = setTimeout(function() {
                updateFrozedColStyle(); 
            }, 200); //200 is time in miliseconds
        });
    }
    
    function modifyTableStyle() {
        dataTable.api().columns.adjust();
        updateTableHeight();
        forzedLeftCol.fnUpdate();
        updateFrozedColStyle();
    }
    
    function updateTableHeight() {
        if($("#" + tableContainerId+ " .dataTables_scrollBody").height() < dataTableScrollHeight) {
            $("#" + tableContainerId+ " .dataTables_scrollBody").height("100%");
        }
    }
    function updateFrozedColStyle() {
        var _heightBody = $("#" + tableContainerId+ " .dataTables_scrollBody").height(),
            _widthBody = $("#" + tableContainerId+ " tbody>tr:nth-child(2)>td:nth-child(1)").width();
        
        if(_widthBody === null) {
            $(".DTFC_LeftWrapper").css('display', 'none');
        }else {
            _widthBody = _widthBody + 22;
            if(forzedLeftCol) {
                $(".DTFC_LeftWrapper").css('display', 'block');
                if(_heightBody < 0) {
                    $(".DTFC_LeftBodyLiner").height('');
                }else {
                    $(".DTFC_LeftBodyLiner").height(_heightBody - 15);
                    
                    //Changed from _heightBody, 15px was designed for
                    //horizontal scroller
                    $(".DTFC_LeftBodyWrapper").height(_heightBody - 15); 
                }
                $(".DTFC_LeftWrapper").width(_widthBody);
                $(".DTFC_LeftBodyLiner").width(_widthBody);
//                $(".DTFC_LeftBodyLiner").css('background-color','white');
                $(".DTFC_LeftFootWrapper").css('top', '15px');
            }
        }
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
    
    function showDataTableReset( ){
        var _showedColumnNumber = dataTable.fnSettings().fnRecordsDisplay(),
            _totalColumnNumber = dataTable.fnSettings().fnRecordsTotal();
        
        if(_showedColumnNumber !== _totalColumnNumber){
            $(".dataTableReset span").css('display','block');
            $(".ColVis.TableTools").css('display','none');
        }else{
            $(".dataTableReset span").css('display','none');
            $(".ColVis.TableTools").css('display','block');
        }
    }
    
    function updateDataTableNumericFilter(){
        $.fn.dataTable.ext.search = [];
        for(var i = 0, filterL = dataTableNumericFilter.length; i < filterL; i++ ){
            if(dataTableNumericFilter[i] !== ''){
                $.fn.dataTable.ext.search.push(dataTableNumericFilter[i]);
            }
        }
        dataTable.api().draw();
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
                _iCurrent = Number(aData[columnIndexMappingColumnId[_i]]);
            
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
        filters[columnIndexMappingColumnId[_i]] = {
            start: _min,
            end: _max
        };
        updateDataTableNumericFilter();
//        dataTable.fnSort([ [columnIndexMappingColumnId[_i],'asc']]);
        pushDisableFiltId(_i);
        showDataTableReset();
        $("#dataTable-" + _i + "-reset").css('display','block');
        refreshSelectionInDataTable();
        modifyTableStyle();
    }
    
    function pushDisableFiltId(Id) {
        if(disableFiltId.indexOf(Id) === -1) {
            disableFiltId.push(Id);
        }
    }
    
    function refreshSelectionInDataTable(){
        selectorData = tfootData();
        $(".dataTables_scrollFoot tfoot td").each( function ( i ) {
            
            if(disableFiltId.indexOf(i) === -1){               
                $(this).css('z-index','1500');
                
                this.innerHTML = fnCreateSelect( selectorData[columnIndexMappingColumnId[i]], i, this);
                
                var _drag = d3.behavior.drag()
                        .on("drag", selectorDragMove)
                        .on("dragend",selectorDragEnd);
                
                d3.select("#dataTable-" + i + "-left")
                                .call(_drag);
                d3.select("#dataTable-" + i + "-right")
                                .call(_drag);
                        
                $("#dataTable-" + i + "-reset").unbind('click');
                $("#dataTable-" + i + "-reset").click(function(){
                    delete filters[columnIndexMappingColumnId[i]];
                    dataTableNumericFilter[columnIndexMappingColumnId[i]] = '';
                    updateDataTableNumericFilter();
                    disableFiltId.splice(disableFiltId.indexOf(i),1);
                    showDataTableReset();
                    $("#dataTable-" + i + "-reset").css('display','none');
                    refreshSelectionInDataTable();
                    modifyTableStyle();
                });
                
                $('select', this).change( function () {
                    if($(this).val() === ''){
                        dataTable.fnFilter($(this).val(), columnIndexMappingColumnId[i]);
                        disableFiltId.splice(disableFiltId.indexOf(i),1);
                        delete filters[columnIndexMappingColumnId[i]];
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
                        pushDisableFiltId(i);
                        filters[columnIndexMappingColumnId[i]] = $(this).val();
                    }
                    
                    showDataTableReset();
                    refreshSelectionInDataTable();
                    if($(this).val() !== ''){
                        $(window).resize();
                    }
                    modifyTableStyle();
                });
            }
        });
    }
    
    return {
        init: function(_tableId, _tableContainerId, _data) {
            initParam(_tableId, _tableContainerId, _data);
            initDataTableTfoot();
            initDataTable();
            addEvents();
        },
        
        updateFrozedColStyle: updateFrozedColStyle,
        getColumnData: function() {
            return aoColumns;
        }
    };
};

TableTools.prototype._fnGetDataTablesData = function ( oConfig )
{
        var i, iLen, j, jLen;
        var aRow, aData=[], sLoopData='', arr;
        var dt = this.s.dt, tr, child;
        var regex = new RegExp(oConfig.sFieldBoundary, "g"); /* Do it here for speed */
        var aColumnsInc = this._fnColumnTargets( oConfig.mColumns );
        var bSelectedOnly = (typeof oConfig.bSelectedOnly != 'undefined') ? oConfig.bSelectedOnly : false;
        var aoColumns = StudyViewInitClinicalTab.getDataTable().getColumnData();
        
        /*
         * Header
         */
        if ( oConfig.bHeader )
        {
                aRow = [];

                for ( i=0, iLen=dt.aoColumns.length ; i<iLen ; i++ )
                {
                        if ( aColumnsInc[i] )
                        {
                                sLoopData = aoColumns[i].fullDisplay.replace(/\n/g," ").replace( /<.*?>/g, "" ).replace(/^\s+|\s+$/g,"");
                                sLoopData = this._fnHtmlDecode( sLoopData );

                                aRow.push( this._fnBoundData( sLoopData, oConfig.sFieldBoundary, regex ) );
                        }
                }

                aData.push( aRow.join(oConfig.sFieldSeperator) );
        }

        /*
         * Body
         */
        var aSelected = this.fnGetSelected();
        bSelectedOnly = this.s.select.type !== "none" && bSelectedOnly && aSelected.length !== 0;

        var api = $.fn.dataTable.Api;
        var aDataIndex = api ?
                new api( dt ).rows( oConfig.oSelectorOpts ).indexes().flatten().toArray() :
                dt.oInstance
                        .$('tr', oConfig.oSelectorOpts)
                        .map( function (id, row) {
                                // If "selected only", then ensure that the row is in the selected list
                                return bSelectedOnly && $.inArray( row, aSelected ) === -1 ?
                                        null :
                                        dt.oInstance.fnGetPosition( row );
                        } )
                        .get();

        for ( j=0, jLen=aDataIndex.length ; j<jLen ; j++ )
        {
                tr = dt.aoData[ aDataIndex[j] ].nTr;
                aRow = [];

                /* Columns */
                for ( i=0, iLen=dt.aoColumns.length ; i<iLen ; i++ )
                {
                        if ( aColumnsInc[i] )
                        {
                                /* Convert to strings (with small optimisation) */
                                var mTypeData = dt.oApi._fnGetCellData( dt, aDataIndex[j], i, 'display' );
                                if ( oConfig.fnCellRender )
                                {
                                        sLoopData = oConfig.fnCellRender( mTypeData, i, tr, aDataIndex[j] )+"";
                                }
                                else if ( typeof mTypeData == "string" )
                                {
                                        /* Strip newlines, replace img tags with alt attr. and finally strip html... */
                                        sLoopData = mTypeData.replace(/\n/g," ");
                                        sLoopData =
                                            sLoopData.replace(/<img.*?\s+alt\s*=\s*(?:"([^"]+)"|'([^']+)'|([^\s>]+)).*?>/gi,
                                                '$1$2$3');
                                        sLoopData = sLoopData.replace( /<.*?>/g, "" );
                                }
                                else
                                {
                                        sLoopData = mTypeData+"";
                                }

                                /* Trim and clean the data */
                                sLoopData = sLoopData.replace(/^\s+/, '').replace(/\s+$/, '');
                                sLoopData = this._fnHtmlDecode( sLoopData );

                                /* Bound it and add it to the total data */
                                aRow.push( this._fnBoundData( sLoopData, oConfig.sFieldBoundary, regex ) );
                        }
                }

                aData.push( aRow.join(oConfig.sFieldSeperator) );

                /* Details rows from fnOpen */
                if ( oConfig.bOpenRows )
                {
                        arr = $.grep(dt.aoOpenRows, function(o) { return o.nParent === tr; });

                        if ( arr.length === 1 )
                        {
                                sLoopData = this._fnBoundData( $('td', arr[0].nTr).html(), oConfig.sFieldBoundary, regex );
                                aData.push( sLoopData );
                        }
                }
        }

        /*
         * Footer
         */
        if ( oConfig.bFooter && dt.nTFoot !== null )
        {
                aRow = [];

                for ( i=0, iLen=dt.aoColumns.length ; i<iLen ; i++ )
                {
                        if ( aColumnsInc[i] && dt.aoColumns[i].nTf !== null )
                        {
                                sLoopData = dt.aoColumns[i].nTf.innerHTML.replace(/\n/g," ").replace( /<.*?>/g, "" );
                                sLoopData = this._fnHtmlDecode( sLoopData );

                                aRow.push( this._fnBoundData( sLoopData, oConfig.sFieldBoundary, regex ) );
                        }
                }

                aData.push( aRow.join(oConfig.sFieldSeperator) );
        }

        var _sLastData = aData.join( this._fnNewline(oConfig) );
        return _sLastData;
}