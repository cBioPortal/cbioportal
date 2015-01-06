/* 
 * This class is used to retrive data from webservice, mutaton and cna servlets
 * including all date and date attributes
 */


var StudyViewProxy = (function() {
    
    var parObject = {},
        sampleIdStr = '',
        patientIdStr = '',
        samplePatientMapping = {},
        ajaxParameters = {},
        obtainDataObject = [];
        
    obtainDataObject['attr'] = [];
    obtainDataObject['arr'] = [];
    
    function initLocalParameters(callBack){
        parObject = jQuery.extend(true, {}, StudyViewParams.params);
        patientIdStr = parObject.caseIds.join(' ');
        $.ajax({
            type: "POST", 
            url: "webservice.do", 
            data: {
                cmd: "getPatientSampleMapping",
                format: "json",
                cancer_study_id: parObject.studyId,
                case_set_id: parObject.caseSetId}
        }).done(function(d){
            var sampleIds = [],
                patientIds = [];
            parObject.samplePatientMapping = d;
            
            for(var key in d) {
                patientIds.push(key);
                for(var i = 0; i< d[key].length; i++){
                    if(sampleIds.indexOf(d[key][i]) === -1) {
                        sampleIds.push(d[key][i]);
                        samplePatientMapping[d[key][i]] = key;
                    }
                }
            }
            StudyViewParams.params.sampleIds = sampleIds;
            StudyViewParams.params.patientIds = patientIds;
            sampleIdStr = sampleIds.join(' ');
            callBack();
        });
    }
    
    function initAjaxParameters(){
        ajaxParameters = {
            webserviceData: {
                cmd: "getClinicalData",
                format: "json",
                cancer_study_id: parObject.studyId,
                case_set_id: parObject.caseSetId
            },
            caseLists: {
                cmd: "getCaseLists",
                cancer_study_id: parObject.studyId
            },
            clinicalAttributesData: {
                cancer_study_id: parObject.studyId,
                case_list: sampleIdStr
            },
            mutationsData: {
                cmd: "count_mutations",
                cases_ids: sampleIdStr,
                mutation_profile: parObject.mutationProfileId
            },
            cnaFraction: {
                cmd: "get_cna_fraction",
                case_ids: patientIdStr,
                cancer_study_id: parObject.studyId
            },
            mutatedGenesData: {
                cmd: 'get_smg',
                mutation_profile: parObject.mutationProfileId
            },
            gisticData: {
                selected_cancer_type: parObject.studyId
            },
            cnaData: {
                sample_id: sampleIdStr,
                cna_profile: parObject.cnaProfileId,
                cbio_genes_filter: true
            }
        };
    }
    
    function getDataFunc(callbackFunc){
         $.when(  
                $.ajax({type: "POST", url: "webservice.do", data: ajaxParameters.webserviceData}), 
                $.ajax({type: "POST", url: "mutations.json", data: ajaxParameters.mutationsData}),
                $.ajax({type: "POST", url: "cna.json", data: ajaxParameters.cnaFraction}),
                $.ajax({type: "POST", url: "mutations.json", data: ajaxParameters.mutatedGenesData}),
                $.ajax({type: "POST", url: "Gistic.json", data: ajaxParameters.gisticData}),
                $.ajax({type: "POST", url: "webservice.do", data: ajaxParameters.caseLists}))
            .done(function(a1, a2, a3, a4, a5, a6){
                var _dataAttrMapArr = {}, //Map attrbute value with attribute name for each datum
                    _keyNumMapping = {},
                    _data = a1[0]['data'],
                    _dataAttrOfa1 = a1[0]['attributes'],
                    _dataLength = _data.length,
                    _sampleIds = Object.keys(samplePatientMapping),
                    _sequencedSampleIds = [],
                    _locks=0;
                    
                //Reorganize data into wanted format datum[ caseID ][ Attribute Name ] = Attribute Value
                //The original data structure is { attr_id: , attr_va: , sample}
                for(var i = 0; i < _dataLength; i++){
                    if(_data[i]["sample"] in _dataAttrMapArr){
                        _dataAttrMapArr[_data[i]["sample"]][_data[i]["attr_id"].toString().toUpperCase()] = _data[i]["attr_val"];
                    }else{
                        _dataAttrMapArr[_data[i]["sample"]] = [];
                        _dataAttrMapArr[_data[i]["sample"]][_data[i]["attr_id"].toString().toUpperCase()] = _data[i]["attr_val"];
                    }
                    if(_sampleIds.indexOf(_data[i]["sample"]) === -1) {
                        console.log('Unknown sample exists in clincial data.');
                    }
                }
                
                //Initial data array, not all of cases has MUTAION COUND OR COPY NUMBER ALTERATIONS.
                for(var j = 0; j <  _sampleIds.length; j++){
                    var _caseDatum = {};
                    _caseDatum["CASE_ID"] = _sampleIds[j];
                    _caseDatum["PATIENT_ID"] = samplePatientMapping[_sampleIds[j]];
                    _caseDatum["MUTATION_COUNT"] = "NA";
                    _caseDatum["COPY_NUMBER_ALTERATIONS"] = "NA";
                    _keyNumMapping[_sampleIds[j]] = j;
                    $.each(_dataAttrOfa1,function(key,value){
                        value['attr_id'] = value['attr_id'].toUpperCase();
                        _dataAttrOfa1[key]['attr_id'] = value['attr_id'];
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
                    
                    value['display_name'] = toPascalCase(value['display_name']);
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
                var filteredA2 = removeExtraData(_sampleIds,a2[0]);
                var filteredA3 = removeExtraData(_sampleIds,a3[0]);
                
                //Find sequenced sample Ids
                if(a6[0]) {
                    var _lists = a6[0].split('\n');
                    for(var i = 0; i < _lists.length; i++) {
                        if(_lists[i].indexOf('sequenced samples') !== -1) {
                            var _info = _lists[i].split('\t');
                            if(_info.length === 5) {
                                _sequencedSampleIds = _info[4].split(' ');
                            }
                            break;
                        }
                    }
                }
                
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
                        if(isNaN(val) && _sequencedSampleIds.indexOf(i) !== -1) {
                            console.log(i, 'has been sequenced but does not have data. Changed mutation count to 0.');
                            val = 0;
                        }
                        
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
                    obtainDataObject['attr'].push({
                        attr_id: 'CASE_ID',
                        display_name: 'SAMPLE ID',
                        description: 'Sample Identifier',
                        datatype: 'STRING'
                    });
                }
                obtainDataObject['mutatedGenes'] = a4[0];
                obtainDataObject['gistic'] = a5[0];
                
                if (!patientidExist) {
                    obtainDataObject['attr'].push({
                        attr_id: 'PATIENT_ID',
                        display_name: 'PATIENT ID',
                        description: 'Patient Identifier',
                        datatype: 'STRING'
                    });
                }
               
                if(ajaxParameters.cnaData.cna_profile) {
                    _locks++;
                    $.ajax({type: "POST", url: "cna.json", data: ajaxParameters.cnaData})
                        .then(function(data){
                            obtainDataObject['cna'] = data;
                            _locks--;
                        }, function(){
                            obtainDataObject['cna'] = '';
                            _locks--;
                        });
                }
                
                lockSolved();
                
                function lockSolved() {
                    setTimeout(function(){
                        if(_locks > 0) {
                            lockSolved();
                        }else {
                            callbackFunc(obtainDataObject);
                        }
                    }, 200);
                }
            });
    };
    
    function getPatientIdsBySampleIds(_sampleIds) {
        var _patientIds = [];
        
        for(var i = 0, _sampleIdsL = _sampleIds.length; i < _sampleIdsL; i++) {
            if(_patientIds.indexOf(_sampleIds[i]) === -1) {
                _patientIds.push(samplePatientMapping[_sampleIds[i]]);
            }
        }
        return _.uniq(_patientIds);
    }
    
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
    
    function toPascalCase(str) {
        var arr = str.split(/\s|_/);
//        for(var i=0,l=arr.length; i<l; i++) {
//            arr[i] = arr[i].substr(0,1).toUpperCase() + 
//                     (arr[i].length > 1 ? arr[i].substr(1).toLowerCase() : "");
//        } 
        return arr.join(" ");
    }

    return {
        init: function(callbackFunc){
            initLocalParameters(function(){
                initAjaxParameters();
                getDataFunc(callbackFunc);
            });
        },
        
        getArrData: function(){ return obtainDataObject['arr'];},
        getAttrData: function(){ return obtainDataObject['attr'];},
        getMutatedGenesData: function(){ return obtainDataObject['mutatedGenes'];},
        getGisticData: function(){return obtainDataObject['gistic'];},
        getCNAData: function(){return obtainDataObject['cna'];},
        getSampleidToPatientidMap: function(){return obtainDataObject['sampleidToPatientidMap'];},
        getPatientIdsBySampleIds: getPatientIdsBySampleIds
    };
}());