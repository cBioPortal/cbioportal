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
        patientToSampleMapping = {},
        sampleToPatientMapping = {},
        ajaxParameters = {},
        obtainDataObject = {},

    //The mapping between obtainDataObject.arr index and sampleID
        sampleIdArrMapping = {};

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
            patientToSampleMapping = d;

            for(var key in d) {
                patientIds.push(key);
                for(var i = 0; i< d[key].length; i++){
                    if(sampleIds.indexOf(d[key][i]) === -1) {
                        sampleIds.push(d[key][i]);
                        sampleToPatientMapping[d[key][i]] = key;
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
            $.ajax({type: "POST", url: "webservice.do", data: ajaxParameters.caseLists})
            //$.ajax({type: "POST", url: "mutations.json", data: ajaxParameters.mutatedGenesData})
        )
            .done(function(a1, a2, a3, a5){
                var _dataAttrMapArr = {}, //Map attribute value with attribute name for each datum
                    _data = a1[0].data,
                    _dataAttrOfa1 = {},
                    _dataLength = _data.length,
                    _sampleIds = Object.keys(sampleToPatientMapping),
                    _sequencedSampleIds = [],
                    _cnaSampleIds = [],
                    _allSampleIds = [],
                    _locks=0;

                //Keep original data format.
                obtainDataObject.webserviceData = a1[0];

                //Uppercase all attr_id
                for(var i= 0; i < a1[0].attributes.length; i++){
                    var caseAttr = new CaseAttr();
                    caseAttr.attr_id =  a1[0].attributes[i].attr_id.toUpperCase();
                    if(_.isString(a1[0].attributes[i].display_name)){
                        caseAttr.display_name = a1[0].attributes[i].display_name;
                    } else {
                        if (caseAttr.attr_id === 'CASE_ID') {
                            caseAttr.display_name = "Sample ID";
                        } else {
                            //Fallback to using ID if there is no display_name
                            caseAttr.display_name = caseAttr.attr_id;
                        }
                    }
                    caseAttr.display_name = toPascalCase(caseAttr.display_name);
                    caseAttr.datatype = a1[0].attributes[i].datatype;
                    CaseDatum.prototype[caseAttr.attr_id] = 'NA';
                    _dataAttrOfa1[caseAttr.attr_id] = caseAttr;
                }

                //Initial data array, not all of cases has MUTATION COUNT OR COPY NUMBER ALTERATIONS.
                for(var j = 0; j <  _sampleIds.length; j++){
                    var _caseDatum =  new CaseDatum();
                    _caseDatum.CASE_ID = _sampleIds[j];
                    _caseDatum.PATIENT_ID = sampleToPatientMapping[_sampleIds[j]];
                    sampleIdArrMapping[_sampleIds[j]] = j;
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

                    obtainDataObject.arr[sampleIdArrMapping[_sampleId]][_attrId] = _attrVal;
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
                // TODO: this is very hacky, we should switch to the cbioportal-client.js
                if(a5[0]) {
                    var _lists = a5[0].split('\n');
                    for(var i = 0; i < _lists.length; i++) {
                        var _parts = _lists[i].split('\t');
                        if(_parts.length < 5) continue;
                        if (_parts[0] === parObject.studyId+"_sequenced") {
                            _sequencedSampleIds = _parts[4].trim().split(' ');
                        } else if (_parts[0] === parObject.studyId+"_cna") {
                            _cnaSampleIds = _parts[4].trim().split(' ');
                        } else if (_parts[0] === parObject.studyId+"_all") {
                            _allSampleIds = _parts[4].trim().split(' ');
                        }
                    }

                    //For efficient comparing, see StudyViewUtil.intersection
                    _allSampleIds = _allSampleIds.sort();
                    _sequencedSampleIds = _sequencedSampleIds.sort();
                    _cnaSampleIds = _cnaSampleIds.sort();
                    
                    obtainDataObject.sequencedSampleIds = 
                            _sequencedSampleIds.length>0 ? _sequencedSampleIds : _allSampleIds;
                    obtainDataObject.cnaSampleIds = 
                            _cnaSampleIds.length>0 ? _cnaSampleIds : _allSampleIds;
                    
                }

                //Add new attribute MUTATION COUNT for each case if have any
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

                        obtainDataObject.arr[sampleIdArrMapping[sampleId]].MUTATION_COUNT = val;
                        _keys[val] = 0;
                    }
                    _newAttr.keys = Object.keys(_keys);
                    obtainDataObject.attr.push(_newAttr);
                }else {
                    for(var i = 0, arrSize = obtainDataObject.arr.length; i < arrSize; i++) {
                        if (!_.isUndefined(obtainDataObject.arr[i].MUTATION_COUNT)) {
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
                        obtainDataObject.arr[sampleIdArrMapping[sampleId]].COPY_NUMBER_ALTERATIONS = val;
                    }
                    _newAttri.keys = Object.keys(_keys);
                    obtainDataObject.attr.push(_newAttri);
                }else {
                    for(var i = 0, arrSize = obtainDataObject.arr.length; i < arrSize; i++) {
                        if(!_.isUndefined(obtainDataObject.arr[i].COPY_NUMBER_ALTERATIONS)) {
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
                    caseAttr.display_name = 'Sample ID';
                    caseAttr.description = 'Sample Identifier';
                    caseAttr.datatype = 'STRING';
                    caseAttr.keys =  StudyViewParams.params.sampleIds;
                    obtainDataObject.attr.push(caseAttr);
                }

                if (!patientidExist) {
                    var caseAttr = new CaseAttr();
                    caseAttr.attr_id = 'PATIENT_ID';
                    caseAttr.display_name = 'Patient ID';
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
                //add 3 attributes: sample count per patient, the sample is sequenced or not, and the sample has cna data or not
                var sequenced_caseAttr = new CaseAttr();
                sequenced_caseAttr.attr_id = 'SEQUENCED';
                sequenced_caseAttr.display_name = 'With Mutation Data';
                sequenced_caseAttr.description = 'If the sample got sequenced';
                sequenced_caseAttr.datatype = 'STRING';
                
                var has_cna_data_caseAttr = new CaseAttr();
                has_cna_data_caseAttr.attr_id = 'HAS_CNA_DATA';
                has_cna_data_caseAttr.display_name = 'With CNA Data';
                has_cna_data_caseAttr.description = 'If the sample has CNA data';
                has_cna_data_caseAttr.datatype = 'STRING';
                
                var sample_count_patient_caseAttr = new CaseAttr();
                sample_count_patient_caseAttr.attr_id = 'SAMPLE_COUNT_PATIENT';
                sample_count_patient_caseAttr.display_name = '# of Samples Per Patient';
                sample_count_patient_caseAttr.description = '';
                sample_count_patient_caseAttr.datatype = 'STRING';
                sample_count_patient_caseAttr.numOfNoneEmpty =  obtainDataObject.arr.length;

                var currentItem, uniqueCounts = [1], maxCount = 1, sequencedValues = [], hasCNAValues = [];

                for(var i = 0; i < obtainDataObject.arr.length; i++){
                    currentItem = obtainDataObject.arr[i];
                    if(obtainDataObject.sequencedSampleIds.indexOf(currentItem.CASE_ID) !== -1){
                        currentItem.SEQUENCED = 'Yes';
                        sequencedValues.push('Yes');
                    }else{
                        currentItem.SEQUENCED = 'No';
                        sequencedValues.push('No');
                    }
                    if(obtainDataObject.cnaSampleIds.indexOf(currentItem.CASE_ID) !== -1){
                        currentItem.HAS_CNA_DATA = 'Yes';
                        hasCNAValues.push('Yes');
                    }else{
                        currentItem.HAS_CNA_DATA = 'No';
                        hasCNAValues.push('No');
                    }
                    currentItem.SAMPLE_COUNT_PATIENT = patientToSampleMapping[currentItem.PATIENT_ID].length.toString();
                    if(patientToSampleMapping[currentItem.PATIENT_ID].length > maxCount){
                        maxCount = patientToSampleMapping[currentItem.PATIENT_ID].length;
                        uniqueCounts.push(maxCount.toString());
                    }
                }
                sequenced_caseAttr.keys = _.uniq(sequencedValues);
                obtainDataObject.attr.push(sequenced_caseAttr);
                
                has_cna_data_caseAttr.keys = _.uniq(hasCNAValues);
                obtainDataObject.attr.push(has_cna_data_caseAttr);

                sample_count_patient_caseAttr.keys = uniqueCounts;
                obtainDataObject.attr.push(sample_count_patient_caseAttr);
                
            });
    }

    function getPatientIdsBySampleIds(_sampleIds) {
        var _patientIds = [];

        for(var i = 0, _sampleIdsL = _sampleIds.length; i < _sampleIdsL; i++) {
            if(_patientIds.indexOf(_sampleIds[i]) === -1) {
                _patientIds.push(sampleToPatientMapping[_sampleIds[i]]);
            }
        }
        return _.uniq(_patientIds);
    }

    function getSampleIdsByPatientIds(_patientIds) {
        var _sampleIds = [];

        for(var i = 0, _patientIdsL = _patientIds.length; i < _patientIdsL; i++) {
            if(_sampleIds.indexOf(_patientIds[i]) === -1) {
                if(!_.isUndefined(patientToSampleMapping[_patientIds[i]])) {
                    _sampleIds = _sampleIds.concat(patientToSampleMapping[_patientIds[i]]);
                }
            }
        }
        return _.uniq(_sampleIds);
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
        return arr.join(" ");
    }

    function getCNAData(){
        var deferred = $.Deferred();

        if(!_.isUndefined(obtainDataObject.cna)){
            deferred.resolve(obtainDataObject.cna);
        }else{
            if(hasCNA) {
                $.ajax({type: "POST", url: "cna.json", data: ajaxParameters.cnaData})
                    .then(function(data){
                        obtainDataObject.cna = data;
                        obtainDataObject.cnaSampleBased = convertCNAData(data);
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

    /**
     * Convert copy number alteration data into sample ID based object.
     * It will be used to quickly get selected samples' CNA data.
     *
     * @param data  Object  It contains caseIds, alter, cytoban, gene and gistic information. Each attribute is an array.
     *                      Reminder: the length for each attribute should be the same.
     * @returns {{}} Sample ID based CNA data.
     */
    function convertCNAData(data) {
        var converted = {};

        if (data) {
            for (var i = 0, dataL = data.gene.length; i < dataL; i++) {
                var caseIds = data.caseIds[i];

                for (var j = 0, caseIdsL = caseIds.length; j < caseIdsL; j++) {
                    if (_.isUndefined(converted[caseIds[j]])) {
                        converted[caseIds[j]] = [];
                    }
                    converted[caseIds[j]].push({
                        alter: data.alter[i],
                        cytoband: data.cytoband[i],
                        gene: data.gene[i],
                        gistic: data.gistic[i]
                    });
                }
            }
        }
        return converted;
    }

    /**
     * Only return selected samples' copy number alterations data.
     *
     * @param sampleIds Array   List of sample IDs.
     * @returns data    Object  Keep the original CNA data format.
     */
    function getCNABasedOnSampleIds(sampleIds) {
        var data = {
            alter: [],
            caseIds: [],
            cytoband: [],
            gistic: [],
            gene: []
        };
        if (sampleIds.length === this.getSampleIds().length) {
            return obtainDataObject.cna;
        }
        if (!_.isUndefined(obtainDataObject.cnaSampleBased)) {
            var geneSpecific = {};
            var numOfSample = sampleIds.length;
            for (var i = 0; i < numOfSample; i++) {
                if (obtainDataObject.cnaSampleBased[sampleIds[i]]) {
                    for (var j = 0, numOfGenes = obtainDataObject.cnaSampleBased[sampleIds[i]].length; j < numOfGenes; j++) {
                        var key = obtainDataObject.cnaSampleBased[sampleIds[i]][j].gene + obtainDataObject.cnaSampleBased[sampleIds[i]][j].alter;
                        if (_.isUndefined(geneSpecific[key])) {
                            var datum = obtainDataObject.cnaSampleBased[sampleIds[i]][j];
                            geneSpecific[key] = data.caseIds.length;
                            data.alter.push(datum.alter);
                            data.caseIds.push([]);
                            data.cytoband.push(datum.cytoband);
                            data.gistic.push(datum.gistic);
                            data.gene.push(datum.gene);
                        }
                        data.caseIds[geneSpecific[key]].push(sampleIds[i]);
                    }
                }
            }
        }

        return data;
    }

    /**
     * Convert mutated gene data into sample ID based object.
     * It will be used to quickly get selected samples' mutated gene data.
     *
     * @param   data    Array  It is the list of mutated genes. Each item contains caseIds, cytoband, gene_symbol, length and num_muts.
     * @returns {{}}    Object Sample ID based mutated genes data.
     */
    function convertMutatedGeneData(data) {
        var converted = {};

        if (data) {
            for (var i = 0, dataL = data.length; i < dataL; i++) {
                var gene = data[i];
                var geneSymbol = gene.gene_symbol;
                var caseIds = gene.caseIds;

                for (var j = 0, caseIdsL = caseIds.length; j < caseIdsL; j++) {
                    if (_.isUndefined(converted[caseIds[j]])) {
                        converted[caseIds[j]] = {};
                    }
                    if (_.isUndefined(converted[caseIds[j]][geneSymbol])) {
                        converted[caseIds[j]][geneSymbol] = _.extend({}, gene);
                        converted[caseIds[j]][geneSymbol].num_muts = 0;
                    }
                    ++converted[caseIds[j]][geneSymbol].num_muts;
                }
            }
        }
        return converted;
    }

    /**
     * Only return selected samples' mutated gene data.
     *
     * @param sampleIds Array   List of sample IDs.
     * @returns data    Array   List of mutated genes data. Its order is the same with sample IDs.
     */
    function getMutatedGeneDataBasedOnSampleIds(sampleIds) {
        var data = [];
        if (sampleIds instanceof Array) {
            if(sampleIds.length === this.getSampleIds().length) {
                return obtainDataObject.mutatedGenes;
            }
            if(_.isObject(obtainDataObject.mutatedGenesSampleBased)){
                var geneSpecific = {};
                var numOfSample = sampleIds.length;
                for (var i = 0; i < numOfSample; i++) {
                    if (obtainDataObject.mutatedGenesSampleBased[sampleIds[i]]) {
                        var sample = obtainDataObject.mutatedGenesSampleBased[sampleIds[i]];
                        for(var geneSymbol in sample) {
                            if (_.isUndefined(geneSpecific[geneSymbol])) {
                                geneSpecific[geneSymbol] = _.extend({}, sample[geneSymbol]);
                                geneSpecific[geneSymbol].caseIds = [];
                                geneSpecific[geneSymbol].num_muts = 0;
                            }
                            geneSpecific[geneSymbol].num_muts += sample[geneSymbol].num_muts;
                            geneSpecific[geneSymbol].caseIds.push(sampleIds[i]);

                        }
                    }
                }
                data = _.values(geneSpecific);
            }
        }

        return data;
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
                        obtainDataObject.mutatedGenesSampleBased = convertMutatedGeneData(data);
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

    function getGisticData(){
        var deferred = $.Deferred();

        if(obtainDataObject.hasOwnProperty('gistic') && obtainDataObject.gistic){
            deferred.resolve(obtainDataObject.gistic);
        }else{
            if(hasMutation){
                $.ajax({type: "POST", url: "Gistic.json", data: ajaxParameters.gisticData})
                    .then(function(data){
                        obtainDataObject.gistic = data;
                        deferred.resolve(obtainDataObject.gistic);
                    }, function(status){
                        obtainDataObject.gistic = '';
                        deferred.reject(null);
                    });
            }else{
                deferred.reject(null);
            }
        }
        return deferred.promise();
    }

    function ivizLoad() {
        parObject = jQuery.extend(true, {}, StudyViewParams.params);
        initAjaxParameters();
    }
    
    function getArrDataBySampleIds(sampleIds){
        var  _arr = [];
        if(sampleIds instanceof Array) {
            var sampleL = sampleIds.length;

            for(var i = 0; i < sampleL; i++) {
                if(sampleIdArrMapping.hasOwnProperty(sampleIds[i])) {
                    _arr.push(obtainDataObject.arr[sampleIdArrMapping[sampleIds[i]]]);
                }
            }
        }
        return _arr;
    }

    return {
        init: function(callbackFunc){
            initLocalParameters(function(){
                initAjaxParameters();
                getDataFunc(callbackFunc);
            });
        },
        ivizLoad: ivizLoad,
        getArrData: function(){ return obtainDataObject.arr;},
        getArrDataBySampleIds: getArrDataBySampleIds,
        getAttrData: function(){ return obtainDataObject.attr;},
        getMutatedGenesData: getMutatedGenesData,
        getMutatedGeneDataBasedOnSampleIds: getMutatedGeneDataBasedOnSampleIds,
        getCNABasedOnSampleIds: getCNABasedOnSampleIds,
        getGisticData: getGisticData,
        getCNAData: getCNAData,
        getSampleidToPatientidMap: function(){return obtainDataObject.sampleidToPatientidMap;},
        getPatientIdsBySampleIds: getPatientIdsBySampleIds,
        getSampleIdsByPatientIds: getSampleIdsByPatientIds,
        getSequencedSampleIds: function() {return obtainDataObject.sequencedSampleIds;},
        getCnaSampleIds: function() {return obtainDataObject.cnaSampleIds;},
        getPatientIds: function () {
            return Object.keys(patientToSampleMapping);
        },
        getSampleIds: function () {
            return Object.keys(sampleToPatientMapping);
        },
        getAllData: function () {
            return obtainDataObject;
        },
        sampleIdExist: function (id) {
            if (_.isString(id)) {
                return sampleToPatientMapping.hasOwnProperty(id);
            } else if (_.isArray(id)) {
                var exist = true;
                for (var i = 0, size = id.length; i < size; i++) {
                    if (!sampleToPatientMapping.hasOwnProperty(id[i])) {
                        exist = false;
                    }
                    break;
                }
                return exist;
            }
        },
        getWebserviceData: function() {
            return obtainDataObject.webserviceData;
        }
    };
}());
