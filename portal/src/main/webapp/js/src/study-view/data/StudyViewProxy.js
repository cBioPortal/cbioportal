/* 
 * This class is used to retrive data from webservice, mutaton and cna servlets
 * including all date and date attributes
 */


var StudyViewProxy = (function() {
    
    var parObject = {
        studyId: "",
        caseIds: "",
        cnaProfileId: "",
        mutationProfileId: "",
        caseSetId: ""
    };

    var caseIdStr = '',
        webserviceData = {},
        clinicalAttributesData = {},
        mutationsData = {},
        cnaData = {},
        survivalData = {},
        obtainDataObject = [];
        
    obtainDataObject['attr'] = [];
    obtainDataObject['arr'] = [];
    
    function initLocalParameters(o){
        parObject.studyId = o.studyId;
        parObject.caseIds = o.caseIds;
        parObject.cnaProfileId = o.cnaProfileId;
        parObject.mutationProfileId = o.mutationProfileId;        
        parObject.caseSetId = o.caseSetId;
        caseIdStr = parObject.caseIds.join(' ');
    }
    
    //By using POST method in Ajax, the Ajax should be created.
    function initAjaxParameters(){
        webserviceData = {
            cmd: "getClinicalData",
            format: "json",
            cancer_study_id: parObject.studyId,
            case_set_id: parObject.caseSetId
        };
        clinicalAttributesData = {
            cancer_study_id: parObject.studyId,
            case_list: caseIdStr
        };
        mutationsData = {
            cmd: "count_mutations",
            cases_ids: caseIdStr,
            mutation_profile: parObject.mutationProfileId
        };
        cnaData = {
            cmd: "get_cna_fraction",
            case_ids: caseIdStr,
            cancer_study_id: parObject.studyId
        };
        survivalData = {
            case_set_id: caseSetId,
            case_ids_key: caseIds,
            cancer_study_id: cancerStudyId
        };
    }
    
    function getDataFunc(callbackFunc){
         $.when(  
                $.ajax({type: "POST", url: "webservice.do", data: webserviceData}), 
                $.ajax({type: "POST", url: "mutations.json", data: mutationsData}),
                $.ajax({type: "POST", url: "cna.json", data: cnaData}),
                $.ajax({type: "POST", url: "getSurvivalData.json", data: survivalData}))

            .done(function(a1, a2, a3, a4){
                var _dataAttrMapArr = [], //Map attrbute value with attribute name for each datum
                    _keyNumMapping = [],
                    _data = a1[0]['data'],
                    _dataAttrOfa1 = a1[0]['attributes'],
                    _dataLength = _data.length,
                    _globalCaseIdsLength = parObject.caseIds.length;

                //Reorganize data into wanted format datum[ caseID ][ Attribute Name ] = Attribute Value
                //The original data structure is { attr_id: , attr_va: , sample}
                for(var i = 0; i < _dataLength; i++){
                    if(_data[i]["sample"] in _dataAttrMapArr){
                        _dataAttrMapArr[_data[i]["sample"]][_data[i]["attr_id"]] = _data[i]["attr_val"];
                    }
                    else{
                        _dataAttrMapArr[_data[i]["sample"]] = [];
                        _dataAttrMapArr[_data[i]["sample"]][_data[i]["attr_id"]] = _data[i]["attr_val"];
                    }
                }

                //Initial data array, not all of cases has MUTAION COUND OR COPY NUMBER ALTERATIONS.
                for(var j = 0; j <  _globalCaseIdsLength; j++){
                    var _caseDatum = {};
                    _caseDatum["CASE_ID"] = parObject.caseIds[j];
                    _caseDatum["MUTATION_COUNT"] = "NA";
                    _caseDatum["COPY_NUMBER_ALTERATIONS"] = "NA";
                    _keyNumMapping[parObject.caseIds[j]] = j;
                    $.each(_dataAttrOfa1,function(key,value){
                        _caseDatum[value['attr_id']] = "NA";
                    });
                    obtainDataObject['arr'].push(_caseDatum);
                }
                
                for(var key in _dataAttrMapArr){
                    for (var i = 0 ; i < _dataAttrOfa1.length ; i++){
                        var tmpValue = _dataAttrMapArr[key][_dataAttrOfa1[i]['attr_id']];
                        if(tmpValue === '' || tmpValue === undefined || tmpValue === 'na' || tmpValue === 'NA'){
                            tmpValue = 'NA';
                            obtainDataObject['arr'][_keyNumMapping[key]][_dataAttrOfa1[i]['attr_id']] = tmpValue;
                        }else
                            obtainDataObject['arr'][_keyNumMapping[key]][_dataAttrOfa1[i]['attr_id']] = _dataAttrMapArr[key][_dataAttrOfa1[i]['attr_id']];
                    }
                       
                }
                
                obtainDataObject['attr'] = a1[0]['attributes'];
                
                //Filter extra data
                var filteredA2 = removeExtraData(parObject.caseIds,a2[0]);
                var filteredA3 = removeExtraData(parObject.caseIds,a3[0]);
                
                //Add new attribute MUTATIOIN COUNT for each case if have any
                if(Object.keys(filteredA2).length !== 0){
                    var _newAttr = {};
                    _newAttr.attr_id = 'MUTATION_COUNT';
                    _newAttr.display_name = 'Mutation Count';
                    _newAttr.description = 'Mutation Count';
                    _newAttr.datatype = 'NUMBER';                        

                    jQuery.each(filteredA2, function(i,val){
                        if(val === undefined)
                            val = 'NA';
                        obtainDataObject['arr'][_keyNumMapping[i]]['MUTATION_COUNT'] = val;
                    });    
                    obtainDataObject['attr'].push(_newAttr);
                }

                //Add new attribute COPY NUMBER ALTERATIONS for each case if have any
                if(Object.keys(filteredA3).length !== 0){
                    var _newAttri = {};
                    _newAttri.attr_id = 'COPY_NUMBER_ALTERATIONS';
                    _newAttri.display_name = 'Copy Number Alterations';
                    _newAttri.description = 'Copy Number Alterations';
                    _newAttri.datatype = 'NUMBER';

                    jQuery.each(filteredA3, function(i,val){
                        if(val === undefined)
                            val = 'NA';
                        obtainDataObject['arr'][_keyNumMapping[i]]['COPY_NUMBER_ALTERATIONS'] = val;
                    }); 
                    obtainDataObject['attr'].push(_newAttri);
                }
                
                //Attribute CASE_ID will be treated as identifier in Study View
                //If the case data does not have CASE_ID column, new CASE_ID attribute
                //should be created.d
                var caseidExist = false;
                for(var i=0 ; i<obtainDataObject['attr'].length; i++){
                    if(obtainDataObject['attr'][i].attr_id === 'CASE_ID'){
                        caseidExist = true;
                    }
                }
                if(!caseidExist){
                    var _newAttr = {};
                    _newAttr.attr_id = 'CASE_ID';
                    _newAttr.display_name = 'patient';
                    _newAttr.description = 'patient';
                    _newAttr.datatype = 'NUMBER';
                    obtainDataObject['attr'].push(_newAttr);
                }
                console.log(a4);
                obtainDataObject.survivalData = a4[0];
                callbackFunc(obtainDataObject);
            });
    };
    
    //Webservice may retrun extra cases including there data
    //This function is designed to elimate data based on case id
    //which not inlcuded in globle caseIds Array
    function removeExtraData(_caseIds,_data){
        var _newData = {};
        for(var i=0; i< _caseIds.length ; i++){
            _newData[_caseIds[i]] = _data[_caseIds[i]];
        }
        return _newData;
    }
    
    return {
        init: function(o,callbackFunc){
            initLocalParameters(o);
            initAjaxParameters();
            getDataFunc(callbackFunc);
        }
    };
}());