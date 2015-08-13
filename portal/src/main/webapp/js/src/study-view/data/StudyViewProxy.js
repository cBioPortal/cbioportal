/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */



var StudyViewProxy = (function() {

    var parObject = {},
        sampleIdStr = '',
        patientIdStr = '',
        samplePatientMapping = {},
        ajaxParameters = {},
        obtainDataObject = {};

    obtainDataObject.attr = [];
    obtainDataObject.arr = [];

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
                //sample_id: sampleIdStr,
                cna_profile: parObject.cnaProfileId,
                cbio_genes_filter: true
            }
        };
    }

    function CaseDatum() {
        this.CASE_ID = 'NA';
        this.PATIENT_ID = 'NA';
        this.MUTATION_COUNT = 'NA';
        this.COPY_NUMBER_ALTERATIONS = 'NA';
    }

    function CaseAttr() {
        this.attr_id = '';
        this.datatype = ''; //STRING, NUMBER
        this.description = '';
        this.display_name = '';
        this.keys = ['NA'];
        this.numOfNoneEmpty = 0;
    }

    function getDataFunc(callbackFunc){
        $.when(
            $.ajax({type: "POST", url: "webservice.do", data: ajaxParameters.webserviceData}),
            $.ajax({type: "POST", url: "mutations.json", data: ajaxParameters.mutationsData}),
            $.ajax({type: "POST", url: "cna.json", data: ajaxParameters.cnaFraction}),
            $.ajax({type: "POST", url: "Gistic.json", data: ajaxParameters.gisticData}),
            $.ajax({type: "POST", url: "webservice.do", data: ajaxParameters.caseLists})
            //$.ajax({type: "POST", url: "mutations.json", data: ajaxParameters.mutatedGenesData})
        )
            .done(function(a1, a2, a3, a4, a5){
                var _dataAttrMapArr = {}, //Map attrbute value with attribute name for each datum
                    _keyNumMapping = {},
                    _data = a1[0].data,
                    _dataAttrOfa1 = {},
                    _dataLength = _data.length,
                    _sampleIds = Object.keys(samplePatientMapping),
                    _sequencedSampleIds = [],
                    _locks=0;

                //Uppercase all attr_id
                for(var i= 0; i < a1[0].attributes.length; i++){
                    var caseAttr = new CaseAttr();
                    caseAttr.attr_id =  a1[0].attributes[i].attr_id.toUpperCase();
                    if(! a1[0].attributes[i].hasOwnProperty('display_name') ||  a1[0].attributes[i].display_name){
                        caseAttr.display_name =  a1[0].attributes[i].attr_id;
                    }
                    caseAttr.display_name = toPascalCase(caseAttr.display_name);
                    caseAttr.datatype = a1[0].attributes[i].datatype;
                    CaseDatum.prototype[caseAttr.attr_id] = 'NA';
                    _dataAttrOfa1[caseAttr.attr_id] = caseAttr;
                }

                //Initial data array, not all of cases has MUTAION COUND OR COPY NUMBER ALTERATIONS.
                for(var j = 0; j <  _sampleIds.length; j++){
                    var _caseDatum =  new CaseDatum();
                    _caseDatum.CASE_ID = _sampleIds[j];
                    _caseDatum.PATIENT_ID = samplePatientMapping[_sampleIds[j]];
                    _keyNumMapping[_sampleIds[j]] = j;
                    obtainDataObject.arr.push(_caseDatum);
                }

                //Reorganize data into wanted format datum[ caseID ][ Attribute Name ] = Attribute Value
                //The original data structure is { attr_id: , attr_va: , sample}
                for(var i = 0; i < _dataLength; i++){
                    var _sampleId = _data[i].sample;
                    var _attrId = _data[i].attr_id.toUpperCase();
                    var _attrVal = _data[i].attr_val;

                    if(cbio.util.checkNullOrUndefined(_attrVal) || _attrVal === '' || _attrVal === 'na'){
                        _attrVal = 'NA';
                    }else if(_attrVal !== 'NA'){
                        ++_dataAttrOfa1[_attrId].numOfNoneEmpty;
                        if(_dataAttrOfa1[_attrId].keys.indexOf(_attrVal) === -1) {
                            _dataAttrOfa1[_attrId].keys.push(_attrVal);
                        }
                    }

                    obtainDataObject.arr[_keyNumMapping[_sampleId]][_attrId] = _attrVal;
                }

                for(var key in _dataAttrOfa1) {
                    //Remove NA if all attribtues data available in one case
                    if(_dataAttrOfa1[key].numOfNoneEmpty === _sampleIds.length) {
                        _dataAttrOfa1[key].keys.shift();
                    }
                    obtainDataObject.attr.push(_dataAttrOfa1[key]);
                }

                //Filter extra data
                var filteredA2 = removeExtraData(_sampleIds,a2[0]);
                var filteredA3 = removeExtraData(_sampleIds,a3[0]);

                //Find sequenced sample Ids
                if(a5[0]) {
                    var _lists = a5[0].split('\n');
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
                    var _newAttr = new CaseAttr();
                    _newAttr.attr_id = 'MUTATION_COUNT';
                    _newAttr.display_name = 'Mutation Count';
                    _newAttr.description = 'Mutation Count';
                    _newAttr.datatype = 'NUMBER';
                    var _keys = {};

                    for(var sampleId in filteredA2){
                        var val = filteredA2[sampleId];

                        if(isNaN(val)) {
                            if(_sequencedSampleIds.indexOf(sampleId) !== -1){
                                //console.log(sampleId, 'has been sequenced but does not have data. Changed mutation count to 0.');
                                val = 0;
                                ++_newAttr.numOfNoneEmpty;
                            }else{
                                val = 'NA';
                            }
                        }else{
                            ++_newAttr.numOfNoneEmpty;
                        }

                        obtainDataObject.arr[_keyNumMapping[sampleId]].MUTATION_COUNT = val;
                        _keys[val] = 0;
                    }
                    _newAttr.keys = Object.keys(_keys);
                    obtainDataObject.attr.push(_newAttr);
                }else {
                    var cnaLength = obtainDataObject.arr.length;
                    for(var i = 0; i < cnaLength; i++) {
                        if(obtainDataObject.arr[i].hasOwnProperty('MUTATION_COUNT')) {
                            delete obtainDataObject.arr[i].MUTATION_COUNT;
                        }
                    }
                }

                //Add new attribute COPY NUMBER ALTERATIONS for each case if have any
                if(Object.keys(filteredA3).length !== 0){
                    var _newAttri = new CaseAttr();
                    _newAttri.attr_id = 'COPY_NUMBER_ALTERATIONS';
                    _newAttri.display_name = 'Copy Number Alterations';
                    _newAttri.description = 'Copy Number Alterations';
                    _newAttri.datatype = 'NUMBER';
                    var _keys = {};

                    for(var sampleId in filteredA3){
                        var val = filteredA3[sampleId];
                        if(cbio.util.checkNullOrUndefined(val) || val === '' || val === 'na'){
                            val = 'NA';
                        }else{
                            ++_newAttri.numOfNoneEmpty;
                        }
                        _keys[val] = 0;
                        obtainDataObject.arr[_keyNumMapping[sampleId]].COPY_NUMBER_ALTERATIONS = val;
                    }
                    _newAttri.keys = Object.keys(_keys);
                    obtainDataObject.attr.push(_newAttri);
                }else {
                    var cnaLength = obtainDataObject.arr.length;
                    for(var i = 0; i < cnaLength; i++) {
                        if(obtainDataObject.arr[i].hasOwnProperty('COPY_NUMBER_ALTERATIONS')) {
                            delete obtainDataObject.arr[i].COPY_NUMBER_ALTERATIONS;
                        }
                    }
                }

                //Attribute CASE_ID will be treated as identifier in Study View
                //If the case data does not have CASE_ID column, new CASE_ID attribute
                //should be created.d
                var caseidExist = false;
                var patientidExist = false;
                for(var i=0 ; i<obtainDataObject.attr.length; i++){
                    if(obtainDataObject.attr[i].attr_id === 'CASE_ID'){
                        caseidExist = true;
                    }
                    if(obtainDataObject.attr[i].attr_id === 'PATIENT_ID'){
                        patientidExist = true;
                    }
                }

                if(!caseidExist){
                    var caseAttr = new CaseAttr();
                    caseAttr.attr_id = 'CASE_ID';
                    caseAttr.display_name = 'CASE_ID';
                    caseAttr.description = 'Sample Identifier';
                    caseAttr.datatype = 'STRING';
                    caseAttr.keys =  StudyViewParams.params.sampleIds;
                    obtainDataObject.attr.push(caseAttr);
                }
                //obtainDataObject['mutatedGenes'] = a4[0];
                obtainDataObject.gistic = a4[0];

                if (!patientidExist) {
                    var caseAttr = new CaseAttr();
                    caseAttr.attr_id = 'PATIENT_ID';
                    caseAttr.display_name = 'PATIENT_ID';
                    caseAttr.description = 'Patient Identifier';
                    caseAttr.datatype = 'STRING';
                    caseAttr.keys =  StudyViewParams.params.patientIds;
                    obtainDataObject.attr.push(caseAttr);
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
    }

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

    function getCNAData(){
        var deferred = $.Deferred();

        if(obtainDataObject.hasOwnProperty('cna') && obtainDataObject.cna){
            deferred.resolve(obtainDataObject.cna);
        }else{
            if(hasCNA) {
                $.ajax({type: "POST", url: "cna.json", data: ajaxParameters.cnaData})
                    .then(function(data){
                        obtainDataObject.cna = data;
                        deferred.resolve(obtainDataObject.cna);
                    }, function(status){
                        obtainDataObject.cna = '';
                        deferred.reject(null);
                    });
            }else{
                deferred.reject(null);
            }
        }
        return deferred.promise();
    }

    function getMutatedGenesData(){
        var deferred = $.Deferred();

        if(obtainDataObject.hasOwnProperty('mutatedGenes') && obtainDataObject.mutatedGenes){
            deferred.resolve(obtainDataObject.mutatedGenes);
        }else{
            if(hasMutation){
                $.ajax({type: "POST", url: "mutations.json", data: ajaxParameters.mutatedGenesData})
                    .then(function(data){
                        obtainDataObject.mutatedGenes = data;
                        deferred.resolve(obtainDataObject.mutatedGenes);
                    }, function(status){
                        obtainDataObject.mutatedGenes = '';
                        deferred.reject(null);
                    });
            }else{
                deferred.reject(null);
            }
        }
        return deferred.promise();
    }

    return {
        init: function(callbackFunc){
            initLocalParameters(function(){
                initAjaxParameters();
                getDataFunc(callbackFunc);
            });
        },

        getArrData: function(){ return obtainDataObject.arr;},
        getAttrData: function(){ return obtainDataObject.attr;},
        getMutatedGenesData: getMutatedGenesData,
        getGisticData: function(){return obtainDataObject.gistic;},
        getCNAData: getCNAData,
        getSampleidToPatientidMap: function(){return obtainDataObject.sampleidToPatientidMap;},
        getPatientIdsBySampleIds: getPatientIdsBySampleIds
    };
}());
