/* 
 * This class is used to retrive data from webservice, mutaton and cna servlets
 * including all date and date attributes
 */


var StudyViewProxy = (function() {
    
    var parObject = {},
        caseIdStr = '',
        ajaxParameters = {},
        obtainDataObject = [];
        
    obtainDataObject['attr'] = [];
    obtainDataObject['arr'] = [];
    
    function initLocalParameters(){
        parObject = jQuery.extend(true, {}, StudyViewParams.params);
        caseIdStr = parObject.caseIds.join(' ');
    }
    
    function initAjaxParameters(){
        ajaxParameters = {
            webserviceData: {
                cmd: "getClinicalData",
                format: "json",
                cancer_study_id: parObject.studyId,
                case_set_id: parObject.caseSetId
            },
            clinicalAttributesData: {
                cancer_study_id: parObject.studyId,
                case_list: caseIdStr
            },
            mutationsData: {
                cmd: "count_mutations",
                cases_ids: caseIdStr,
                mutation_profile: parObject.mutationProfileId
            },
            cnaData: {
                cmd: "get_cna_fraction",
                case_ids: caseIdStr,
                cancer_study_id: parObject.studyId
            },
            mutatedGenesData: {
                cmd: 'get_smg',
                case_list: caseIdStr,
                mutation_profile: parObject.mutationProfileId
            },
            gisticData: {
                selected_cancer_type: parObject.studyId
            }
        };
    }
    
    function getDataFunc(callbackFunc){
         $.when(  
                $.ajax({type: "POST", url: "webservice.do", data: ajaxParameters.webserviceData}), 
                $.ajax({type: "POST", url: "mutations.json", data: ajaxParameters.mutationsData}),
                $.ajax({type: "POST", url: "cna.json", data: ajaxParameters.cnaData}),
                $.ajax({type: "POST", url: "mutations.json", data: ajaxParameters.mutatedGenesData}),
                $.ajax({type: "POST", url: "Gistic.json", data: ajaxParameters.gisticData}))
            .done(function(a1, a2, a3, a4, a5){
                var _dataAttrMapArr = {}, //Map attrbute value with attribute name for each datum
                    _keyNumMapping = {},
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
                        if(value['attr_id'] !== 'CASE_ID'){
                            _caseDatum[value['attr_id']] = "NA";
                        }
                    });
                    obtainDataObject['arr'].push(_caseDatum);
                }
                
                $.each(_dataAttrOfa1,function(key,value){
                    if(!value['display_name']){
                        value['display_name'] = value['attr_id'];
                    }
                });
                
                for(var key in _dataAttrMapArr){
                    for (var i = 0 ; i < _dataAttrOfa1.length ; i++){
                        var tmpValue = _dataAttrMapArr[key][_dataAttrOfa1[i]['attr_id']];
                        if(tmpValue === '' || tmpValue === undefined || tmpValue === 'na' || tmpValue === 'NA'){
                            tmpValue = 'NA';
                        }
                        obtainDataObject['arr'][_keyNumMapping[key]][_dataAttrOfa1[i]['attr_id']] = tmpValue;
                    }
                       
                }
                
                obtainDataObject['attr'] = _dataAttrOfa1;
                
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
                }else {
                    var cnaLength = obtainDataObject['arr'].length;
                    for(var i = 0; i < cnaLength; i++) {
                        if(obtainDataObject['arr'][i].hasOwnProperty('MUTATION_COUNT')) {
                            delete obtainDataObject['arr'][i]['MUTATION_COUNT'];
                        }
                    }
                }

                //Add new attribute COPY NUMBER ALTERATIONS for each case if have any
                if(Object.keys(filteredA3).length !== 0){
                    var _newAttri = {};
                    _newAttri.attr_id = 'COPY_NUMBER_ALTERATIONS';
                    _newAttri.display_name = 'Copy Number Alterations';
                    _newAttri.description = 'Copy Number Alterations';
                    _newAttri.datatype = 'NUMBER';

                    jQuery.each(filteredA3, function(i,val){
                        if(val === undefined){
                            val = 'NA';
                        }
                        obtainDataObject['arr'][_keyNumMapping[i]]['COPY_NUMBER_ALTERATIONS'] = val;
                    }); 
                    obtainDataObject['attr'].push(_newAttri);
                }else {
                    var cnaLength = obtainDataObject['arr'].length;
                    for(var i = 0; i < cnaLength; i++) {
                        if(obtainDataObject['arr'][i].hasOwnProperty('COPY_NUMBER_ALTERATIONS')) {
                            delete obtainDataObject['arr'][i]['COPY_NUMBER_ALTERATIONS'];
                        }
                    }
                }
                
                //Attribute CASE_ID will be treated as identifier in Study View
                //If the case data does not have CASE_ID column, new CASE_ID attribute
                //should be created.d
                var caseidExist = false;
                var patientidExist = false;
                for(var i=0 ; i<obtainDataObject['attr'].length; i++){
                    if(obtainDataObject['attr'][i].attr_id === 'CASE_ID'){
                        caseidExist = true;
                    }
                    if(obtainDataObject['attr'][i].attr_id === 'PATIENT_ID'){
                        patientidExist = true;
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
                obtainDataObject['mutatedGenes'] = a4[0];
                obtainDataObject['cna'] = a5[0];
                
                if (patientidExist) {
                    obtainDataObject['sampleidToPatientidMap'] = _.reduce(obtainDataObject['arr'],
                        function(memo, sampleObj) {
                            if ('PATIENT_ID' in sampleObj) {
                                memo[sampleObj['CASE_ID']] = sampleObj['PATIENT_ID'];
                                return memo;
                            }
                        }
                        ,{}); 
               }
                
                callbackFunc(obtainDataObject);
            });
    };
    
    //Webservice may retrun extra cases including there data
    //This function is designed to elimate data based on case id
    //which not inlcuded in globle caseIds Array
    function removeExtraData(_caseIds,_data){
        var _newData = {},
            _hasValue = false;
        for(var i=0; i< _caseIds.length ; i++){
            _newData[_caseIds[i]] = _data[_caseIds[i]];
            if(_data[_caseIds[i]] !== undefined){
                _hasValue = true;
            }
        }
        if(_hasValue) {
            return _newData;
        }else {
            return [];
        }
    }
    
    return {
        init: function(callbackFunc){
            initLocalParameters();
            initAjaxParameters();
            getDataFunc(callbackFunc);
        },
        
        getArrData: function(){ return obtainDataObject['arr'];},
        getAttrData: function(){ return obtainDataObject['attr'];},
        getMutatedGenesData: function(){ return obtainDataObject['mutatedGenes'];},
        getCNAData: function(){return obtainDataObject['cna'];},
        getSampleidToPatientidMap: function(){return obtainDataObject['sampleidToPatientidMap'];}
    };
}());