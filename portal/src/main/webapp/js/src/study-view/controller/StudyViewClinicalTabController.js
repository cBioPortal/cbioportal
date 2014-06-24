
/*
 * This class is designed to control the logic for Clinial Tab in Study View
 * 
 * @autor Hongxin Zhang
 * 
 */


var StudyViewClinicalTabController = (function(){
    var attr,
        arr,
        attrLength,
        arrLength,
        aaData = [], 
        aoColumns = [],
        aoColumnsLength,
        aaDataLength,
        dataType = [],
        dataTableNumericFilter = [];
    
    function init(_data){
//        initParam(_data);
        
//        StudyViewInitClinicalTab
//                .init(
//                    'clinical_table',
//                    aoColumns,
//                    aaData
//                );

        StudyViewInitClinicalTab.init('clinical_table', 'clinical-data-table-div', _data);
    }
    
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
                    _tmpValue = "<a href='case.do?case_id=" + 
                    _value['CASE_ID'] + "&cancer_study_id=" +
                    StudyViewParams.params.studyId + "' target='_blank'><span style='color: #2986e2'>" + 
                    _value['CASE_ID'] + "</span></a></strong>";
                }else{
                    _tmpValue = _value[_valueAo.sTitle.replace(/[ ]/g,'_')];
                }
                if(!isNaN(_tmpValue) && (_tmpValue % 1 !== 0)){
                    _tmpValue = cbio.util.toPrecision(Number(_tmpValue),3,0.01);
                }
                
               
                _selectedString = _tmpValue.toString();
                _specialCharLength = _specialChar.length;
                
                if ( _valueAo.sTitle !== 'CASE ID'){
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
    
    //Initialize aoColumns Data
    function initColumnsTitleData() {
        var i;
        
        aoColumns.length = 0;
        
        aoColumns.push({sTitle:"CASE ID",sType:'string'});

        for( i = 0; i < attr.length; i++ ){
            if( attr[i].attr_id !== 'CASE_ID' ){
                var _tmp = {};
                
                if(attr[i].attr_id === 'COPY_NUMBER_ALTERATIONS'){
                    _tmp.sTitle = 'CNA';
                }else{
                    _tmp.sTitle = attr[i].attr_id.replace(/[_]/g,' ');
                }
                
                _tmp.sType = dataType[attr[i].attr_id];
                aoColumns.push(_tmp);
            }
        }
        aoColumnsLength = aoColumns.length;
    }
    
    function initParam(_data) {
        var i;
        
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
    
    return {
        init: init
    };
})();