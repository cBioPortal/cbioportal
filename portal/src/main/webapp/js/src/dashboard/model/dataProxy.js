'use strict';
window.DataManagerForIviz = (function($, _) {
  var content = {};

  // Clinical attributes will be transfered into table.
  var tableAttrs_ = ['CANCER_TYPE', 'CANCER_TYPE_DETAILED'];
  content.util = {};

  var clinAttrs_ = {
    general: {
      priority: [
        'CANCER_TYPE',
        'CANCER_TYPE_DETAILED',
        'GENDER',
        'SEX',
        'AGE',
        'sequenced',
        'has_cna_data',
        'sample_count_patient'
      ],
      hidden: []
    }
  };

  /**
   * Priorities (number before 10 are reserved for manually assign)
   * TODO: priority manager after 10
   *
   * Survival Plots: 1; Scatter Plot: 2;
   * Mutated Genes table: 3; CNA table: 4;
   * Mutated Count bar chart: 5; CNA bar chart:6
   * Cancer Type/Cancer Type details: 4.1 but mskimpact/genie: 0.9
   */

  /**
   * General pick clinical attributes based on predesigned Regex
   * This filter is the same one which used in previous Google Charts Version,
   * should be revised later.
   *
   * @param {string} attr Clinical attribute ID.
   * @return {boolean} Whether input attribute passed the criteria.
   */
  content.util.isPreSelectedClinicalAttr = function(attr) {
    return attr.toLowerCase().match(/(os_survival)|(dfs_survival)|(mut_cnt_vs_cna)|(mutated_genes)|(cna_details)|(^age)|(gender)|(sex)|(os_status)|(os_months)|(dfs_status)|(dfs_months)|(race)|(ethnicity)|(sample_type)|(.*site.*)|(.*grade.*)|(.*stage.*)|(histology)|(tumor_type)|(subtype)|(tumor_site)|(mutation_count)|(copy_number_alterations)/);
  };

  /**
   * This is the function to define study specific priority clinical
   * attributes list.
   * TODO: need to work with priority.
   *
   * @param {string} attr Clinical attribute ID
   * @param {string} studyId Study ID.
   * @return {boolean} Whether input attribute is prioritized clinical attribute.
   */
  content.util.isPriorityClinicalAttr = function(attr, studyId) {
    if (_.isString(attr)) {
      if (_.isString(studyId) &&
        clinAttrs_.studies.hasOwnProperty(studyId) &&
        _.isArray(clinAttrs_.studies[studyId].priority) &&
        clinAttrs_.studies[studyId].priority.indexOf(attr) !== -1) {
        return true;
      } else if (_.isArray(studyId)) {
        var sameStudies = _.intersection(
          Object.keys(clinAttrs_.studies), studyId);
        if (sameStudies.length > 0) {
          var contain = false;
          _.every(sameStudies, function(study) {
            if (_.isArray(clinAttrs_.studies[study].priority) &&
              clinAttrs_.studies[study].priority.indexOf(attr) !== -1) {
              contain = true;
              return true;
            }
          });
          if (contain) {
            return true;
          }
        }
      }
      if (clinAttrs_.general.priority.indexOf(attr) !== -1) {
        return true;
      }
    }

    return false;
  };

  /**
   * Compare based on data availability.
   * Notice that: attribute with only one category will be moved to end.
   * Number of keys in this attribute is more than half numOfDatum
   * will be moved to end as well.
   *
   * @param {object} a Attribute meta item A.
   * @param {object} b Attribute meta item B.
   * @return {number} Indicator which item is selected.
   */
  content.util.compareClinicalAvailability = function(a, b) {
    if (!a.keys || !a.numOfDatum) {
      return 1;
    }
    if (!b.keys || !b.numOfDatum) {
      return -1;
    }

    var numOfKeysA = Object.keys(a.keys).length;
    var numOfKeysB = Object.keys(b.keys).length;
    if (numOfKeysA === 1 && numOfKeysB !== 1) {
      return 1;
    }
    if (numOfKeysA !== 1 && numOfKeysB === 1) {
      return -1;
    }

    if (numOfKeysA / a.numOfDatum > 0.5 && numOfKeysB / b.numOfDatum <= 0.5) {
      return 1;
    }
    if (numOfKeysA / a.numOfDatum <= 0.5 && numOfKeysB / b.numOfDatum > 0.5) {
      return -1;
    }

    return b.numOfDatum - a.numOfDatum;
  };

  /**
   * Compare items within clinical priority list.
   *
   * @param {string} a Attribute ID A.
   * @param {string} b Attribute ID B.
   * @return {number} Indicator which item is selected.
   */
  content.util.compareClinicalAttrsPriority = function(a, b) {
    if (!_.isString(a)) {
      return 1;
    }
    if (!_.isString(b)) {
      return -1;
    }
    var aI = clinAttrs_.general.priority.indexOf(a);
    var bI = clinAttrs_.general.priority.indexOf(b);
    return aI - bI;
  };

  /**
   * There are few steps to detemine the priority.
   * Step 1: whether it is in clinAttrs_.general.priority list
   * Step 2: whether it will pass preSelectedAttr Regex check
   * Step 3: Sort the rest based on data availability. Notice that: at this
   * Step, attribute with only one category will be moved to end. Number of
   * keys in this attribute is more than half numOfDatum will be moved to end
   * as well.
   *
   * @param {array} array All clinical attributes.
   * @return {array} Sorted clinical attributes.
   */
  content.util.sortClinicalAttrs = function(array) {
    array = array.sort(function(a, b) {
      var priority = 0;
      if (content.util.isPriorityClinicalAttr(a.attr_id, a.study_ids)) {
        if (content.util.isPriorityClinicalAttr(b.attr_id, b.study_ids)) {
          priority =
            content.util.compareClinicalAttrsPriority(a.attr_id, b.attr_id);
        } else {
          priority = -1;
        }
      } else if (content.util.isPriorityClinicalAttr(b.attr_id, b.study_ids)) {
        priority = 1;
      } else {
        priority = 0;
      }

      if (priority !== 0) {
        return priority;
      }

      if (content.util.isPreSelectedClinicalAttr(a.attr_id)) {
        if (content.util.isPreSelectedClinicalAttr(b.attr_id)) {
          priority = content.util.compareClinicalAvailability(a, b);
        }
      } else if (content.util.isPreSelectedClinicalAttr(b.attr_id)) {
        priority = 1;
      } else {
        priority = 0;
      }

      if (priority !== 0) {
        return priority;
      }

      return content.util.compareClinicalAvailability(a, b);
    });
    return array;
  };

  /**
   * Sort clinical attributes by priority.
   * @param {array} array Clinical attributes.
   * @return {array} Sorted clinical attibutes.
   */
  content.util.sortByClinicalPriority = function(array) {
    if (_.isArray(array)) {
      array = array.sort(function(a, b) {
        var priorityA = a.priority || -1;
        var priorityB = b.priority || -1;

        if (priorityA === -1) {
          return 1;
        }
        if (priorityB === -1) {
          return -1;
        }

        return priorityA - priorityB;
      });
    }
    return array;
  };

  content.util.pxStringToNumber = function(_str) {
    var result;
    if (_.isString(_str)) {
      var tmp = _str.split('px');
      if (tmp.length > 0) {
        result = Number(tmp[0]);
      }
    }
    return result;
  };

  content.util.getHiddenAttrs = function() {
    var hiddenAttrs = {};
    if (_.isArray(clinAttrs_.general.hidden)) {
      _.each(clinAttrs_.general.hidden, function(attr) {
        if (!hiddenAttrs.hasOwnProperty(attr)) {
          hiddenAttrs[attr] = [];
        }
        hiddenAttrs[attr].push('general');
      });
    }
    _.each(clinAttrs_.studies, function(item, studyId) {
      if (_.isArray(item.hidden)) {
        _.each(item.hidden, function(attr) {
          if (!hiddenAttrs.hasOwnProperty(attr)) {
            hiddenAttrs[attr] = [];
          }
          hiddenAttrs[attr].push(studyId);
        });
      }
    });
    return hiddenAttrs;
  };

  /**
   * Finds the intersection elements between two arrays in a simple fashion.
   * Should have O(n) operations, where n is n = MIN(a.length, b.length)
   *
   * @param {array} a First array, must already be sorted
   * @param {array} b Second array, must already be sorted
   * @return {array} The interaction elements between a and b
   */
  content.util.intersection = function(a, b) {
    var result = [];
    var i = 0;
    var j = 0;
    var aL = a.length;
    var bL = b.length;
    while (i < aL && j < bL) {
      if (a[i] < b[j]) {
        ++i;
      } else if (a[i] > b[j]) {
        ++j;
      } else {
        result.push(a[i]);
        ++i;
        ++j;
      }
    }

    return result;
  };

  content.init = function(_portalUrl, _study_cases_map) {
    var initialSetup = function() {
      var _def = new $.Deferred();
      var self = this;
      $.when(self.getSampleLists()).then(function() {
        $.when(self.getStudyToSampleToPatientdMap(), self.getAttrs()).then(function(_studyToSampleToPatientMap) {
          $.when(self.getGeneticProfiles(), self.getCaseLists(),
            self.getClinicalAttributesByStudy())
            .then(function(_geneticProfiles, _caseLists,
                           _clinicalAttributes) {
              var _result = {};
              var _patientData = [];
              var _sampleAttributes = {};
              var _patientAttributes = {};
              var _sampleData = [];
              var _hasDFS = false;
              var _hasOS = false;
              var _hasPatientAttrData = {};
              var _hasSampleAttrData = {};
              var _hasDfsStatus = false;
              var _hasDfsMonths = false;
              var _hasOsStatus = false;
              var _hasOsMonths = false;
              var _cnaCaseUIdsMap = {};
              var _sequencedCaseUIdsMap = {};
              var _cnaCaseUIDs = [];
              var _sequencedCaseUIDs = [];
              var _allCaseUIDs = [];

              $.each(_caseLists, function(studyId, caseList) {
                if (caseList.cnaSampleIds.length > 0) {
                  $.each(caseList.cnaSampleIds, function(index, sampleId) {
                    _cnaCaseUIDs.push(_studyToSampleToPatientMap[studyId].sample_to_uid[sampleId]);
                  });
                }
                if (caseList.sequencedSampleIds.length > 0) {
                  $.each(caseList.sequencedSampleIds, function(index, sampleId) {
                    _sequencedCaseUIDs.push(_studyToSampleToPatientMap[studyId].sample_to_uid[sampleId]);
                  });
                }
                if (caseList.allSampleIds.length > 0) {
                  $.each(caseList.allSampleIds, function(index, sampleId) {
                    _allCaseUIDs.push(_studyToSampleToPatientMap[studyId].sample_to_uid[sampleId]);
                  });
                }
              });
              _cnaCaseUIDs = _cnaCaseUIDs.length > 0 ? _cnaCaseUIDs : _allCaseUIDs;
              _sequencedCaseUIDs = _sequencedCaseUIDs.length > 0 ? _sequencedCaseUIDs : _allCaseUIDs;
              _.each(_cnaCaseUIDs, function(_sampleUid) {
                _cnaCaseUIdsMap[_sampleUid] = _sampleUid;
              });
              _.each(_sequencedCaseUIDs, function(_sampleUid) {
                _sequencedCaseUIdsMap[_sampleUid] = _sampleUid;
              });

              _.each(_clinicalAttributes, function(attr) {
                if (attr.is_patient_attribute === '0') {
                  _sampleAttributes[attr.attr_id] = attr;
                } else {
                  _patientAttributes[attr.attr_id] = attr;
                }
              });

              var addAttr = function(data, group) {
                if (!_.isObject(data) || !data.attr_id || !group) {
                  return null;
                }

                var datum = {
                  attr_id: '',
                  datatype: 'STRING',
                  description: '',
                  display_name: ''
                };

                datum = _.extend(datum, data);

                if (group === 'patient') {
                  _patientAttributes[datum.attr_id] = datum;
                } else if (group === 'sample') {
                  _sampleAttributes[datum.attr_id] = datum;
                }
              };

              // Add three additional attributes for all studies.
              addAttr({
                attr_id: 'sequenced',
                display_name: 'With Mutation Data',
                description: 'If the sample got sequenced'
              }, 'sample');

              addAttr({
                attr_id: 'has_cna_data',
                display_name: 'With CNA Data',
                description: 'If the sample has CNA data'
              }, 'sample');

              addAttr({
                attr_id: 'sample_count_patient',
                display_name: '# of Samples Per Patient',
                description: ''
              }, 'patient');

              // TODO : temporary fix to show/hide charts
              // define view type from data type
              _.each(_sampleAttributes, function(_metaObj) {
                _metaObj.filter = [];
                _metaObj.keys = {};
                _metaObj.numOfDatum = 0;
                _metaObj.priority = -1;
                _metaObj.show = true;
                _metaObj.attrList = [_metaObj.attr_id];
                if (_metaObj.datatype === 'NUMBER') {
                  _metaObj.view_type = 'bar_chart';
                } else if (_metaObj.datatype === 'STRING') {
                  _metaObj.view_type = 'pie_chart';
                }
                if (tableAttrs_.indexOf(_metaObj.attr_id) !== -1) {
                  _metaObj.view_type = 'table';
                  _metaObj.type = 'pieLabel';
                  _metaObj.options = {
                    allCases: _caseLists.allCaseUIDs,
                    sequencedCases: _caseLists.allCaseUIDs
                  };
                }
                if (['CANCER_TYPE', 'CANCER_TYPE_DETAILED']
                    .indexOf(_metaObj.attr_id) !== -1) {
                  if (_.intersection(['mskimpact', 'genie', 'mskimpact_heme', 'mixedpact'],
                      Object.keys(_studyToSampleToPatientMap)).length === 0) {
                    _metaObj.priority = _metaObj.attr_id === 'CANCER_TYPE' ?
                      4.1 : 4.2;
                  } else {
                    _metaObj.priority = _metaObj.attr_id === 'CANCER_TYPE' ?
                      0.8 : 0.9;
                  }
                }
              });
              _.each(_patientAttributes, function(_metaObj) {
                switch (_metaObj.attr_id) {
                  case 'DFS_STATUS':
                    _hasDfsStatus = true;
                    break;
                  case 'DFS_MONTHS':
                    _hasDfsMonths = true;
                    break;
                  case 'OS_STATUS':
                    _hasOsStatus = true;
                    break;
                  case 'OS_MONTHS':
                    _hasOsMonths = true;
                    break;
                  default :
                    break;
                }
                _metaObj.filter = [];
                _metaObj.keys = {};
                _metaObj.numOfDatum = 0;
                _metaObj.show = true;
                _metaObj.priority = -1;
                _metaObj.attrList = [_metaObj.attr_id];
                if (_metaObj.datatype === 'NUMBER') {
                  _metaObj.view_type = 'bar_chart';
                } else if (_metaObj.datatype === 'STRING') {
                  _metaObj.view_type = 'pie_chart';
                }
                if (tableAttrs_.indexOf(_metaObj.attr_id) !== -1) {
                  _metaObj.view_type = 'table';
                  _metaObj.type = 'pieLabel';
                  _metaObj.options = {
                    allCases: _caseLists.allCaseUIDs,
                    sequencedCases: _caseLists.allCaseUIDs
                  };
                }
              });

              if (_hasDfsStatus && _hasDfsMonths) {
                _hasDFS = true;
              }
              if (_hasOsStatus && _hasOsMonths) {
                _hasOS = true;
              }

              var _samplesToPatientMap = {};
              var _patientToSampleMap = {};

              _hasSampleAttrData.sample_uid = '';
              _hasSampleAttrData.sample_id = '';
              _hasSampleAttrData.study_id = '';
              _hasSampleAttrData.sequenced = '';
              _hasSampleAttrData.has_cna_data = '';
              _.each(_studyToSampleToPatientMap, function(_sampleToPatientMap, _studyId) {
                _.each(_sampleToPatientMap.sample_uid_to_patient_uid, function(_patientUID, _sampleUID) {
                  if (_samplesToPatientMap[_sampleUID] === undefined) {
                    _samplesToPatientMap[_sampleUID] = [_patientUID];
                  }
                  if (_patientToSampleMap[_patientUID] === undefined) {
                    _patientToSampleMap[_patientUID] = [_sampleUID];
                  } else {
                    _patientToSampleMap[_patientUID].push(_sampleUID);
                  }

                  if (_patientData[_patientUID] === undefined) {
                    // create datum for each patient
                    var _patientDatum = {};
                    _patientDatum.patient_uid = _patientUID;
                    _patientDatum.patient_id = _sampleToPatientMap.uid_to_patient[_patientUID];
                    _patientDatum.study_id = _studyId;
                    _hasPatientAttrData.patient_id = '';
                    _hasPatientAttrData.patient_uid = '';
                    _hasPatientAttrData.study_id = '';
                    _patientData[_patientUID] = _patientDatum;
                  }

                  // create datum for each sample
                  var _sampleDatum = {};
                  _sampleDatum.sample_id = _sampleToPatientMap.uid_to_sample[_sampleUID];
                  _sampleDatum.sample_uid = _sampleUID;
                  _sampleDatum.study_id = _studyId;
                  _sampleDatum.has_cna_data = 'NO';
                  _sampleDatum.sequenced = 'NO';

                  if (self.hasMutationData()) {
                    if (_sequencedCaseUIdsMap[_sampleUID] !== undefined) {
                      _sampleDatum.sequenced = 'YES';
                    }
                    _sampleDatum.mutated_genes = [];
                  }
                  if (self.hasCnaSegmentData()) {
                    if (_cnaCaseUIdsMap[_sampleUID] !== undefined) {
                      _sampleDatum.has_cna_data = 'YES';
                    }
                    _sampleDatum.cna_details = [];
                  }
                  _sampleData[_sampleUID] = _sampleDatum;
                });
              });

              // Add sample_count_patient data
              _.each(_patientData, function(datum, patientUID) {
                _hasPatientAttrData.sample_count_patient = '';
                if (_patientToSampleMap.hasOwnProperty(patientUID)) {
                  datum.sample_count_patient = _patientToSampleMap[patientUID].length.toString();
                }
              });

              // add CNA Table
              if (self.hasCnaSegmentData()) {
                _hasSampleAttrData.cna_details = '';
                var _cnaAttrMeta = {};
                _cnaAttrMeta.type = 'cna';
                _cnaAttrMeta.view_type = 'table';
                _cnaAttrMeta.display_name = 'CNA Genes';
                _cnaAttrMeta.description = 'This table only shows ' +
                  '<a href="cancer_gene_list.jsp" target="_blank">' +
                  'cBioPortal cancer genes</a> in the cohort.';
                _cnaAttrMeta.attr_id = 'cna_details';
                _cnaAttrMeta.filter = [];
                _cnaAttrMeta.show = true;
                _cnaAttrMeta.keys = {};
                _cnaAttrMeta.numOfDatum = 0;
                _cnaAttrMeta.priority = 4;
                _cnaAttrMeta.attrList = [_cnaAttrMeta.attr_id];
                _cnaAttrMeta.options = {
                  allCases: _allCaseUIDs,
                  sequencedCases: _cnaCaseUIDs
                };
                _sampleAttributes[_cnaAttrMeta.attr_id] = _cnaAttrMeta;
              }

              // add Gene Mutation Info
              if (self.hasMutationData()) {
                _hasSampleAttrData.mutated_genes = '';
                var _mutDataAttrMeta = {};
                _mutDataAttrMeta.type = 'mutatedGene';
                _mutDataAttrMeta.view_type = 'table';
                _mutDataAttrMeta.display_name = 'Mutated Genes';
                _mutDataAttrMeta.description = 'This table shows ' +
                  '<a href="cancer_gene_list.jsp" target="_blank">' +
                  'cBioPortal cancer genes</a> ' +
                  'with 1 or more mutations, as well as any ' +
                  'gene with 2 or more mutations';
                _mutDataAttrMeta.attr_id = 'mutated_genes';
                _mutDataAttrMeta.filter = [];
                _mutDataAttrMeta.show = true;
                _mutDataAttrMeta.keys = {};
                _mutDataAttrMeta.numOfDatum = 0;
                _mutDataAttrMeta.priority = 3;
                _mutDataAttrMeta.attrList = [_mutDataAttrMeta.attr_id];
                _mutDataAttrMeta.options = {
                  allCases: _allCaseUIDs,
                  sequencedCases: _sequencedCaseUIDs
                };
                _sampleAttributes[_mutDataAttrMeta.attr_id] = _mutDataAttrMeta;
              }

              if (_hasDFS) {
                var _dfsSurvivalAttrMeta = {};
                _dfsSurvivalAttrMeta.attr_id = 'DFS_SURVIVAL';
                _dfsSurvivalAttrMeta.datatype = 'SURVIVAL';
                _dfsSurvivalAttrMeta.view_type = 'survival';
                _dfsSurvivalAttrMeta.description = '';
                _dfsSurvivalAttrMeta.display_name = 'Disease Free Survival';
                _dfsSurvivalAttrMeta.filter = [];
                _dfsSurvivalAttrMeta.show = true;
                _dfsSurvivalAttrMeta.keys = {};
                _dfsSurvivalAttrMeta.numOfDatum = 0;
                _dfsSurvivalAttrMeta.priority = 1;
                _dfsSurvivalAttrMeta.attrList = ['DFS_STATUS', 'DFS_MONTHS'];
                _patientAttributes[_dfsSurvivalAttrMeta.attr_id] = _dfsSurvivalAttrMeta;
              }

              if (_hasOS) {
                var _osSurvivalAttrMeta = {};
                _osSurvivalAttrMeta.attr_id = 'OS_SURVIVAL';
                _osSurvivalAttrMeta.datatype = 'SURVIVAL';
                _osSurvivalAttrMeta.view_type = 'survival';
                _osSurvivalAttrMeta.description = '';
                _osSurvivalAttrMeta.display_name = 'Overall Survival';
                _osSurvivalAttrMeta.filter = [];
                _osSurvivalAttrMeta.show = true;
                _osSurvivalAttrMeta.keys = {};
                _osSurvivalAttrMeta.numOfDatum = 0;
                _osSurvivalAttrMeta.priority = 1;
                _osSurvivalAttrMeta.attrList = ['OS_STATUS', 'OS_MONTHS'];
                _patientAttributes[_osSurvivalAttrMeta.attr_id] = _osSurvivalAttrMeta;
              }

              // add Cancer Study
              if (self.getCancerStudyIds().length > 1) {
                _patientAttributes.study_id = {
                  datatype: 'STRING',
                  description: '',
                  display_name: 'Cancer Studies',
                  attr_id: 'study_id',
                  view_type: 'pie_chart',
                  filter: [],
                  keys: [],
                  numOfDatum: 0,
                  priority: -1,
                  show: true,
                  attrList: ['study_id']
                };
              }
              // add Copy Number Alterations bar chart
              // TODO : need to set priority
              if (_hasSampleAttrData.copy_number_alterations !== undefined) {
                _sampleAttributes.copy_number_alterations = {
                  datatype: 'NUMBER',
                  description: '',
                  display_name: 'Fraction of copy number altered genome',
                  attr_id: 'copy_number_alterations',
                  view_type: 'bar_chart',
                  priority: 6,
                  filter: [],
                  attrList: ['copy_number_alterations'],
                  keys: [],
                  numOfDatum: 0,
                  show: true
                };
              }

              _result.groups = {
                group_mapping: {
                  patient_to_sample: _patientToSampleMap,
                  sample_to_patient: _samplesToPatientMap,
                  studyMap: _studyToSampleToPatientMap
                },
                patient: {
                  attr_meta: content.util
                    .sortByClinicalPriority(_.values(_patientAttributes)),
                  data: _patientData,
                  has_attr_data: _hasPatientAttrData
                },
                sample: {
                  attr_meta: content.util
                    .sortByClinicalPriority(_.values(_sampleAttributes)),
                  data: _sampleData,
                  has_attr_data: _hasSampleAttrData
                }
              };

              $.when(self.getCnaFractionData(),
                self.getMutationCount())
                .then(function(_cnaFractionData, _mutationCountData) {
                  var _hasCNAFractionData = _.keys(_cnaFractionData).length > 0;
                  var _hasMutationCountData = _.keys(_mutationCountData).length > 0;

                  _.each(_result.groups.sample.data, function(_sampleDatum) {
                    // mutation count
                    if (_hasMutationCountData) {
                      _hasSampleAttrData.mutation_count = '';
                      if (_mutationCountData[_sampleDatum.study_id] === undefined ||
                        _mutationCountData[_sampleDatum.study_id][_sampleDatum.sample_id] === undefined ||
                        _mutationCountData[_sampleDatum.study_id][_sampleDatum.sample_id] === null) {
                        if (_sequencedCaseUIdsMap[_sampleDatum.sample_uid] === undefined) {
                          _sampleDatum.mutation_count = 'NA';
                        } else {
                          _sampleDatum.mutation_count = 0;
                        }
                      } else {
                        _sampleDatum.mutation_count = _mutationCountData[_sampleDatum.study_id][_sampleDatum.sample_id];
                      }
                    }
                    // cna fraction
                    if (_hasCNAFractionData) {
                      _hasSampleAttrData.copy_number_alterations = '';
                      _hasSampleAttrData.cna_fraction = '';
                      if (_cnaFractionData[_sampleDatum.study_id] === undefined ||
                        _cnaFractionData[_sampleDatum.study_id][_sampleDatum.sample_id] === undefined ||
                        _cnaFractionData[_sampleDatum.study_id][_sampleDatum.sample_id] === null) {
                        _sampleDatum.cna_fraction = 'NA';
                        _sampleDatum.copy_number_alterations = 'NA';
                      } else {
                        _sampleDatum.cna_fraction = _cnaFractionData[_sampleDatum.study_id][_sampleDatum.sample_id];
                        _sampleDatum.copy_number_alterations = _cnaFractionData[_sampleDatum.study_id][_sampleDatum.sample_id];
                      }
                    }
                  });

                  // add Mutation count vs. CNA fraction
                  if (_hasSampleAttrData.mutation_count !== undefined && _hasSampleAttrData.cna_fraction !== undefined) {
                    var _mutCntAttrMeta = {};
                    _mutCntAttrMeta.attr_id = 'MUT_CNT_VS_CNA';
                    _mutCntAttrMeta.datatype = 'SCATTER_PLOT';
                    _mutCntAttrMeta.view_type = 'scatter_plot';
                    _mutCntAttrMeta.description = '';
                    _mutCntAttrMeta.display_name = 'Mutation Count vs. CNA';
                    _mutCntAttrMeta.filter = [];
                    _mutCntAttrMeta.show = true;
                    _mutCntAttrMeta.keys = {};
                    _mutCntAttrMeta.numOfDatum = 0;
                    _mutCntAttrMeta.priority = 2;
                    _mutCntAttrMeta.attrList = ['cna_fraction'];
                    _sampleAttributes[_mutCntAttrMeta.attr_id] = _mutCntAttrMeta;
                  }

                  // add mutation count
                  if (_hasSampleAttrData.mutation_count !== undefined) {
                    var _MutationCountMeta = {};
                    _MutationCountMeta.datatype = 'NUMBER';
                    _MutationCountMeta.description = '';
                    _MutationCountMeta.display_name = 'Mutation Count';
                    _MutationCountMeta.attr_id = 'mutation_count';
                    _MutationCountMeta.view_type = 'bar_chart';
                    _MutationCountMeta.filter = [];
                    _MutationCountMeta.keys = {};
                    _MutationCountMeta.numOfDatum = 0;
                    _MutationCountMeta.priority = 5;
                    _MutationCountMeta.show = true;
                    _MutationCountMeta.attrList = [_MutationCountMeta.attr_id];
                    _sampleAttributes[_MutationCountMeta.attr_id] = _MutationCountMeta;
                  }

                  var hiddenAttrs = content.util.getHiddenAttrs();
                  _.each(content.util.sortClinicalAttrs(
                    _.values(_.extend({}, _patientAttributes, _sampleAttributes))
                  ), function(attr, index) {
                    var attrId = attr.attr_id;
                    var groupRef = _sampleAttributes;

                    if (_patientAttributes.hasOwnProperty(attrId)) {
                      groupRef = _patientAttributes;
                    }

                    if (hiddenAttrs.hasOwnProperty(attrId) &&
                      (hiddenAttrs[attrId].indexOf('general') !== -1 ||
                      _.intersection(hiddenAttrs[attrId],
                        Object.keys(_studyToSampleToPatientMap)).length !== 0)) {
                      groupRef[attrId].priority = 1000;
                    } else if (attr.priority === -1) {
                      groupRef[attrId].priority = 10 + index;
                    }
                  });

                  _result.groups.patient.attr_meta =
                    content.util
                      .sortByClinicalPriority(_.values(_patientAttributes));
                  _result.groups.sample.attr_meta =
                    content.util
                      .sortByClinicalPriority(_.values(_sampleAttributes));

                  self.initialSetupResult = _result;
                  _def.resolve(_result);
                });
            });
        });
      });
      return _def.promise();
    };

    // Borrowed from cbioportal-client.js
    var getApiCallPromise = function(endpt, args) {
      var arg_strings = [];
      for (var k in args) {
        if (args.hasOwnProperty(k)) {
          arg_strings.push(k + '=' + [].concat(args[k]).join(','));
        }
      }
      var arg_string = arg_strings.join('&') || '?';
      return $.ajax({
        type: 'POST',
        url: window.cbioURL + endpt,
        data: arg_string,
        dataType: 'json'
      });
    };

    var getPatientClinicalData = function(self, attr_ids) {
      var def = new $.Deferred();
      var fetch_promises = [];
      var clinical_data = {};
      if (_.isArray(attr_ids)) {
        attr_ids = attr_ids.slice();
      }
      $.when(self.getClinicalAttributesByStudy())
        .then(function(attributes) {
          var studyCasesMap = self.getStudyCasesMap();
          var studyAttributesMap = {};
          if (!_.isArray(attr_ids)) {
            attr_ids = Object.keys(attributes);
          }
          _.each(attr_ids, function(_attrId) {
            var attrDetails = attributes[_attrId];
            if (attrDetails !== undefined) {
              _.each(attrDetails.study_ids, function(studyId) {
                if (studyAttributesMap[studyId] === undefined) {
                  studyAttributesMap[studyId] = [attrDetails.attr_id];
                } else {
                  studyAttributesMap[studyId].push(attrDetails.attr_id);
                }
              });
            }
          });

          fetch_promises = fetch_promises.concat(Object.keys(studyAttributesMap).map(function(_studyId) {
            var _def = new $.Deferred();
            // Bypass cBioPortal client for clinical data call.
            // Checking whether patient clinical data is available takes too much
            // time. This is temporary solution, should be replaced with
            // better solution.
            var uniqueId = _studyId + studyAttributesMap[_studyId].sort().join('') + studyCasesMap[_studyId].patients.sort().join('');
            if (self.data.clinical.patient.hasOwnProperty(uniqueId)) {
              var data = self.data.clinical.patient[uniqueId];
              for (var i = 0; i < data.length; i++) {
                data[i].attr_id = data[i].attr_id.toUpperCase();
                var attr_id = data[i].attr_id;
                clinical_data[attr_id] = clinical_data[attr_id] || [];
                clinical_data[attr_id].push(data[i]);
              }
              _def.resolve();
            } else {
              getApiCallPromise('api-legacy/clinicaldata/patients', {
                study_id: [_studyId],
                attribute_ids: studyAttributesMap[_studyId],
                patient_ids: studyCasesMap[_studyId].patients
              }).then(function(data) {
                self.data.clinical.patient[uniqueId] = data;
                for (var i = 0; i < data.length; i++) {
                  data[i].attr_id = data[i].attr_id.toUpperCase();
                  var attr_id = data[i].attr_id;
                  clinical_data[attr_id] = clinical_data[attr_id] || [];
                  clinical_data[attr_id].push(data[i]);
                }
                _def.resolve();
              }).fail(
                function() {
                  def.reject();
                });
            }
            return _def.promise();
          }));
          $.when.apply($, fetch_promises).then(function() {
            def.resolve(clinical_data);
          });
        });
      return def.promise();
    };

    var getSampleClinicalData = function(self, attr_ids) {
      var def = new $.Deferred();
      var fetch_promises = [];
      var clinical_data = {};
      if (_.isArray(attr_ids)) {
        attr_ids = attr_ids.slice();
      }
      $.when(self.getClinicalAttributesByStudy())
        .then(function(attributes) {
          var studyCasesMap = self.getStudyCasesMap();
          var studyAttributesMap = {};
          if (!_.isArray(attr_ids)) {
            attr_ids = Object.keys(attributes);
          }

          _.each(attr_ids, function(_attrId) {
            var attrDetails = attributes[_attrId];
            if (attrDetails !== undefined) {
              _.each(attrDetails.study_ids, function(studyId) {
                if (studyAttributesMap[studyId] === undefined) {
                  studyAttributesMap[studyId] = [attrDetails.attr_id];
                } else {
                  studyAttributesMap[studyId].push(attrDetails.attr_id);
                }
              });
            }
          });

          fetch_promises = fetch_promises.concat(Object.keys(studyAttributesMap)
            .map(function(_studyId) {
              var _def = new $.Deferred();
              // Bypass cBioPortal client for clinical data call.
              // Checking whether sample clinical data is available takes too much
              // time. This is temporary solution, should be replaced with
              // better solution.
              var uniqueId = _studyId + studyAttributesMap[_studyId].sort().join('') + studyCasesMap[_studyId].samples.sort().join('');
              if (self.data.clinical.sample.hasOwnProperty(uniqueId)) {
                var data = self.data.clinical.sample[uniqueId];
                for (var i = 0; i < data.length; i++) {
                  data[i].attr_id = data[i].attr_id.toUpperCase();
                  var attr_id = data[i].attr_id;
                  clinical_data[attr_id] = clinical_data[attr_id] || [];
                  clinical_data[attr_id].push(data[i]);
                }
                _def.resolve();
              } else {
                getApiCallPromise('api-legacy/clinicaldata/samples', {
                  study_id: [_studyId],
                  attribute_ids: studyAttributesMap[_studyId],
                  sample_ids: studyCasesMap[_studyId].samples
                }).then(function(data) {
                  self.data.clinical.sample[uniqueId] = data;
                  for (var i = 0; i < data.length; i++) {
                    data[i].attr_id = data[i].attr_id.toUpperCase();
                    var attr_id = data[i].attr_id;
                    clinical_data[attr_id] = clinical_data[attr_id] || [];
                    clinical_data[attr_id].push(data[i]);
                  }
                  _def.resolve();
                }).fail(
                  function() {
                    def.reject();
                  });
              }
              return _def.promise();
            }));
          $.when.apply($, fetch_promises).then(function() {
            def.resolve(clinical_data);
          });
        });
      return def.promise();
    };

    return {
      initialSetupResult: '',
      cancerStudyIds: [],
      mutationProfileIdsMap: {},
      cnaProfileIdsMap: {},
      panelSampleMap: {},
      portalUrl: _portalUrl,
      studyCasesMap: _study_cases_map,
      initialSetup: initialSetup,
      hasMutationData: function() {
        return _.keys(this.mutationProfileIdsMap).length > 0;
      },
      hasCnaSegmentData: function() {
        return _.keys(this.cnaProfileIdsMap).length > 0;
      },
      getCancerStudyIds: function() {
        if (this.cancerStudyIds.length === 0) {
          this.cancerStudyIds = _.keys(this.studyCasesMap);
        }
        return this.cancerStudyIds;
      },
      getStudyCasesMap: function() {
        return window.cbio.util.deepCopyObject(this.studyCasesMap);
      },
      data: {
        clinical: {
          sample: {},
          patient: {}
        },
        sampleLists: {
          all: {},
          sequenced: {},
          cna: {},
          lists: {}
        }
      },
      // The reason to separate style variable into individual json is
      // that the scss file can also rely on this file.
      getConfigs: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          $.getJSON(window.cbioResourceURL + 'configs.json')
            .then(function(data) {
              var configs = {
                styles: {
                  vars: {}
                }
              };
              configs = $.extend(true, configs, data);
              configs.styles.vars.width = {
                one: content.util.pxStringToNumber(data['grid-w-1']) || 195,
                two: content.util.pxStringToNumber(data['grid-w-2']) || 400
              };
              configs.styles.vars.height = {
                one: content.util.pxStringToNumber(data['grid-h-1']) || 170,
                two: content.util.pxStringToNumber(data['grid-h-2']) || 350
              };
              configs.styles.vars.chartHeader = 17;
              configs.styles.vars.borderWidth = 2;
              configs.styles.vars.scatter = {
                width: (
                configs.styles.vars.width.two -
                configs.styles.vars.borderWidth) || 400,
                height: (
                configs.styles.vars.height.two -
                configs.styles.vars.chartHeader -
                configs.styles.vars.borderWidth) || 350
              };
              configs.styles.vars.survival = {
                width: configs.styles.vars.scatter.width,
                height: configs.styles.vars.scatter.height
              };
              configs.styles.vars.specialTables = {
                width: configs.styles.vars.scatter.width,
                height: configs.styles.vars.scatter.height - 25
              };
              configs.styles.vars.piechart = {
                width: 140,
                height: 140
              };
              configs.styles.vars.barchart = {
                width: (
                configs.styles.vars.width.two -
                configs.styles.vars.borderWidth) || 400,
                height: (
                configs.styles.vars.height.one -
                configs.styles.vars.chartHeader * 2 -
                configs.styles.vars.borderWidth) || 130
              };
              fetch_promise.resolve(configs);
            })
            .fail(function() {
              fetch_promise.resolve();
            });
        }),
      getAttrs: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          $.getJSON(window.cbioResourceURL + 'attributes.json')
            .then(function(data) {
              if (_.isObject(data)) {
                if (_.isObject(data.clinicalAttrs)) {
                  clinAttrs_ = data.clinicalAttrs;
                }
                if (_.isObject(data.tableAttrs)) {
                  tableAttrs_ = data.tableAttrs;
                }
              }
              fetch_promise.resolve();
            })
            .fail(function() {
              // TODO: maybe move the predefined attributes to here
              fetch_promise.resolve();
            });
        }),
      getGeneticProfiles: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          var _profiles = [];
          var requests = self.getCancerStudyIds().map(
            function(cancer_study_id) {
              var def = new $.Deferred();
              window.cbioportal_client
                .getGeneticProfiles({study_id: [cancer_study_id]})
                .then(function(profiles) {
                  _profiles = _profiles.concat(profiles);
                  def.resolve();
                }).fail(
                function() {
                  fetch_promise.reject();
                });
              return def.promise();
            });
          $.when.apply($, requests).then(function() {
            _.each(_profiles, function(_profile) {
              if (_profile.genetic_alteration_type === 'COPY_NUMBER_ALTERATION' && _profile.datatype === 'DISCRETE') {
                self.cnaProfileIdsMap[_profile.study_id] = _profile.id;
              } else if (_profile.genetic_alteration_type === 'MUTATION_EXTENDED') {
                self.mutationProfileIdsMap[_profile.study_id] = _profile.id;
              }
            });
            fetch_promise.resolve(_profiles);
          }).fail(function() {
            fetch_promise.reject();
          });
        }),
      getCaseLists: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          var _responseStudyCaseList = {};
          var requests = self.getCancerStudyIds().map(
            function(cancer_study_id) {
              var def = new $.Deferred();
              self.getSampleListsData(['all', 'sequenced', 'cna'], cancer_study_id)
                .done(function() {
                  var studyCaseList = {sequencedSampleIds:[],
                    cnaSampleIds: [],
                    allSampleIds: []
                  };
                  if (_.isArray(self.data.sampleLists.sequenced[cancer_study_id])) {
                    studyCaseList.sequencedSampleIds = self.data.sampleLists.sequenced[cancer_study_id];
                  }
                  if (_.isArray(self.data.sampleLists.cna[cancer_study_id])) {
                    studyCaseList.cnaSampleIds = self.data.sampleLists.cna[cancer_study_id];
                  }
                  if (_.isArray(self.data.sampleLists.all[cancer_study_id])) {
                    studyCaseList.allSampleIds = self.data.sampleLists.all[cancer_study_id];
                  }
                  _responseStudyCaseList[cancer_study_id] = studyCaseList;
                  def.resolve();
                }).fail(function() {
                fetch_promise.reject();
              });
              return def.promise();
            });
          $.when.apply($, requests).then(function() {
            fetch_promise.resolve(_responseStudyCaseList);
          }).fail(function() {
            fetch_promise.reject();
          });
        }),
      getClinicalAttributesByStudy: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          var clinical_attributes_set = {};
          var requests = self.getCancerStudyIds().map(
            function(cancer_study_id) {
              var def = new $.Deferred();
              window.cbioportal_client.getClinicalAttributesByStudy({
                study_id: [cancer_study_id]
              }).then(function(attrs) {
                for (var i = 0; i < attrs.length; i++) {
                  // TODO : Need to update logic incase if multiple studies
                  // have same attribute name but different properties
                  attrs[i].attr_id = attrs[i].attr_id.toUpperCase();
                  if (clinical_attributes_set[attrs[i].attr_id] === undefined) {
                    attrs[i].study_ids = [cancer_study_id];
                    clinical_attributes_set[attrs[i].attr_id] = attrs[i];
                  } else {
                    attrs[i].study_ids =
                      clinical_attributes_set[attrs[i].attr_id]
                        .study_ids.concat(cancer_study_id);
                    clinical_attributes_set[attrs[i].attr_id] = attrs[i];
                  }
                }
                def.resolve();
              }).fail(function() {
                fetch_promise.reject();
              });
              return def.promise();
            });
          $.when.apply($, requests).then(function() {
            fetch_promise.resolve(clinical_attributes_set);
          }).fail(function() {
            fetch_promise.reject();
          });
        }),
      getStudyToSampleToPatientdMap: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          var study_to_sample_to_patient = {};
          var _sample_uid = 0;
          var _patient_uid = 0
          var getSamplesCall = function(cancerStudyId) {
            var def = new $.Deferred();
            window.cbioportal_client.getSamples({
              study_id: [cancerStudyId],
              sample_ids: self.studyCasesMap[cancerStudyId].samples
            }).then(function(data) {
              var patient_to_sample = {};
              var sample_to_patient = {};
              var sample_uid_to_patient_uid = {};
              var uid_to_sample = {};
              var sample_to_uid = {};
              var patient_to_uid = {};
              var uid_to_patient = {};
              var resultMap = {};
              var patientList = [];
              for (var i = 0; i < data.length; i++) {
                uid_to_sample[_sample_uid] = data[i].id;
                sample_to_uid[data[i].id] = _sample_uid.toString();
                if (patient_to_uid[data[i].patient_id] === undefined) {
                  uid_to_patient[_patient_uid] = data[i].patient_id;
                  patient_to_uid[data[i].patient_id] = _patient_uid.toString();
                  _patient_uid++;
                }
                if (!patient_to_sample.hasOwnProperty(data[i].patient_id)) {
                  patient_to_sample[data[i].patient_id] = {};
                }
                patient_to_sample[data[i].patient_id][data[i].id] = 1;
                sample_to_patient[data[i].id] = data[i].patient_id;
                sample_uid_to_patient_uid[_sample_uid] = patient_to_uid[data[i].patient_id];
                patientList.push(data[i].patient_id);
                _sample_uid++;
              }
              // set patient list in studyCasesMap if sample list is
              // passed in the input
              if (_.isArray(self.studyCasesMap[cancerStudyId].samples) &&
                self.studyCasesMap[cancerStudyId].samples.length > 0) {
                self.studyCasesMap[cancerStudyId].patients = _.unique(patientList);
              }
              resultMap.uid_to_sample = uid_to_sample;
              resultMap.uid_to_patient = uid_to_patient;
              resultMap.sample_to_uid = sample_to_uid;
              resultMap.patient_to_uid = patient_to_uid;
              resultMap.sample_to_patient = sample_to_patient;
              resultMap.patient_to_sample = _.mapObject(patient_to_sample, function(item) {
                return _.keys(item);
              });
              resultMap.sample_uid_to_patient_uid = sample_uid_to_patient_uid;
              study_to_sample_to_patient[cancerStudyId] = resultMap;
              def.resolve();
            }).fail(function() {
              def.reject();
            });
            return def.promise();
          };

          var requests = self.getCancerStudyIds().map(
            function(cancer_study_id) {
              var def = new $.Deferred();
              if (!self.studyCasesMap.hasOwnProperty(cancer_study_id)) {
                self.studyCasesMap[cancer_study_id] = {};
              }
              if (_.isArray(self.studyCasesMap[cancer_study_id].samples)) {
                getSamplesCall(cancer_study_id)
                  .then(function() {
                    def.resolve();
                  })
                  .fail(function() {
                    fetch_promise.reject();
                  });
              } else {
                self.getSampleListsData(['all'], cancer_study_id)
                  .done(function() {
                    if (_.isArray(self.data.sampleLists.all[cancer_study_id])) {
                      self.studyCasesMap[cancer_study_id].samples =
                        self.data.sampleLists.all[cancer_study_id];
                    }
                    getSamplesCall(cancer_study_id)
                      .then(function() {
                        def.resolve();
                      })
                      .fail(function() {
                        fetch_promise.reject();
                      });
                  }).fail(function() {
                  fetch_promise.reject();
                });
              }
              return def.promise();
            });
          $.when.apply($, requests).then(function() {
            fetch_promise.resolve(study_to_sample_to_patient);
          });
        }),
      getSampleListsData: function(lists, studyId) {
        var def = new $.Deferred();
        var self = this;
        var promises = [];
        if (_.isArray(lists)) {
          _.each(lists, function(list) {
            var _def = new $.Deferred();
            if (list && studyId) {
              if (self.data.sampleLists.lists.hasOwnProperty(studyId)
                && _.isArray(self.data.sampleLists.lists[studyId])
                && self.data.sampleLists.lists[studyId].indexOf(studyId + '_' + list) !== -1) {
                if (!self.data.sampleLists.hasOwnProperty(list)) {
                  self.data.sampleLists[list] = {};
                }
                if (self.data.sampleLists[list].hasOwnProperty(studyId)) {
                  _def.resolve(self.data.sampleLists[list][studyId]);
                } else {
                  $.ajax({
                    url: window.cbioURL + 'api/sample-lists/' +
                    studyId + '_' + list + '/sample-ids',
                    contentType: "application/json",
                    type: 'GET'
                  }).done(function(data) {
                    self.data.sampleLists[list][studyId] = data;
                    _def.resolve(data);
                  }).fail(function() {
                    _def.reject();
                  });
                }
                promises.push(_def.promise());
              }
            }
          });
          $.when.apply($, promises)
            .then(function() {
              def.resolve();
            })
            .fail(function() {
              def.reject();
            });
        } else {
          def.reject();
        }
        return def.promise();
      },
      getSampleLists: function() {
        var def = new $.Deferred();
        var self = this;
        var fetch_promises = [];
        fetch_promises = fetch_promises.concat(self.getCancerStudyIds().map(
          function(studyId) {
            var _def = new $.Deferred();
            if (!self.data.sampleLists.hasOwnProperty('lists')) {
              self.data.sampleLists.lists = {};
            }
            if (self.data.sampleLists.lists.hasOwnProperty(studyId)) {
              _def.resolve(self.data.sampleLists.lists[studyId]);
            } else {
              $.ajax({
                url: window.cbioURL + 'api/studies/' + studyId + '/sample-lists',
                contentType: "application/json",
                type: 'GET'
              }).done(function(data) {
                self.data.sampleLists.lists[studyId] = _.pluck(data, 'sampleListId');
                _def.resolve(data);
              }).fail(function() {
                _def.reject();
              });
              return _def.promise();
            }
          }));
        $.when.apply($, fetch_promises).then(function() {
          def.resolve();
        });
        return def.promise();
      },
      getCnaFractionData: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          var _ajaxCnaFractionData = {};
          var cancer_study_ids = self.getCancerStudyIds();
          var _studyCasesMap = self.getStudyCasesMap();
          var fetch_promises = [];
          fetch_promises = fetch_promises.concat(cancer_study_ids.map(
            function(_studyId) {
              var _def = new $.Deferred();
              var _data = {cmd: 'get_cna_fraction', cancer_study_id: _studyId};
              if (_studyCasesMap[_studyId].samples !== undefined) {
                _data.case_ids = _studyCasesMap[_studyId].samples.join(' ');
              }
              $.ajax({
                method: 'POST',
                url: self.portalUrl + 'cna.json?',
                data: _data,
                success: function(response) {
                  if(Object.keys(response).length > 0) {
                    _ajaxCnaFractionData[_studyId] = response;
                  }
                  _def.resolve();
                },
                error: function() {
                  fetch_promise.reject();
                }
              });
              return _def.promise();
            }));
          $.when.apply($, fetch_promises).then(function() {
            fetch_promise.resolve(_ajaxCnaFractionData);
          });
        }),
      getCnaData: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          var _ajaxCnaData = {};
          var fetch_promises = [];
          var _cnaProfiles = self.cnaProfileIdsMap;
          var _studyCasesMap = self.getStudyCasesMap();

          fetch_promises = fetch_promises.concat(_.map(_cnaProfiles,
            function(_profileId, _studyId) {
              var _def = new $.Deferred();
              var _samples = _studyCasesMap[_studyId].samples;
              var _data = {
                cbio_genes_filter: true,
                cna_profile: _profileId
              };
              if (_samples !== undefined) {
                _data.sample_id = _samples.join(' ');
              }
              $.ajax({
                method: 'POST',
                url: self.portalUrl + 'cna.json?',
                data: _data,
                success: function(response) {
                  _ajaxCnaData[_studyId] = response;
                  _def.resolve();
                },
                error: function() {
                  fetch_promise.reject();
                }
              });
              return _def.promise();
            }));
          $.when.apply($, fetch_promises).then(function() {
            fetch_promise.resolve(_ajaxCnaData);
          });
        }),
      getMutationCount: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          var fetch_promises = [];
          var _ajaxMutationCountData = {};
          $.when(self.getGeneticProfiles()).then(function(_profiles) {
            var _mutationProfiles = _.filter(_profiles, function(_profile) {
              return _profile.study_id + '_mutations' === _profile.id;
            });
            var _studyCasesMap = self.getStudyCasesMap();
            fetch_promises = fetch_promises.concat(_mutationProfiles.map(
              function(_mutationProfile) {
                var _def = new $.Deferred();
                var _samples = _studyCasesMap[_mutationProfile.study_id].samples;
                var _data = {
                  cmd: 'count_mutations',
                  mutation_profile: _mutationProfile.id
                };
                if (_samples !== undefined) {
                  _data.case_ids = _samples.join(' ');
                }
                $.ajax({
                  method: 'POST',
                  url: self.portalUrl + 'mutations.json?',
                  data: _data,
                  success: function(response) {
                    if (Object.keys(response).length > 0) {
                      _ajaxMutationCountData[_mutationProfile.study_id] = response;
                    }
                    _def.resolve();
                  },
                  error: function() {
                    fetch_promise.reject();
                  }
                });
                return _def.promise();
              }));
            $.when.apply($, fetch_promises).then(function() {
              fetch_promise.resolve(_ajaxMutationCountData);
            });
          });
        }),
      getMutData: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          var fetch_promises = [];
          var _mutDataStudyIdArr = [];
          var _mutationProfiles = self.mutationProfileIdsMap;
          var _studyCasesMap = self.getStudyCasesMap();
          fetch_promises = fetch_promises.concat(_.map(_mutationProfiles,
            function(_mutationProfileId, _studyId) {
              var _def = new $.Deferred();
              var _samples = _studyCasesMap[_studyId].samples;
              var _data = {
                cmd: 'get_smg',
                mutation_profile: _mutationProfileId
              };
              if (_samples !== undefined) {
                _data.case_list = _samples.join(' ');
              }
              $.ajax({
                method: 'POST',
                url: self.portalUrl + 'mutations.json?',
                data: _data,
                success: function(response) {
                  _.each(response, function(element) {
                    _.extend(element, {study_id: _studyId});
                  });
                  _mutDataStudyIdArr = _mutDataStudyIdArr.concat(response);
                  _def.resolve();
                },
                error: function() {
                  fetch_promise.reject();
                }
              });
              return _def.promise();
            }));
          $.when.apply($, fetch_promises).then(function() {
            fetch_promise.resolve(_mutDataStudyIdArr);
          }, function() {
            fetch_promise.reject();
          });
        }),
      getSampleClinicalData: function(attribute_ids) {
        return getSampleClinicalData(this, attribute_ids);
      },
      getPatientClinicalData: function(attribute_ids) {
        return getPatientClinicalData(this, attribute_ids);
      },
      getClinicalData: function(attribute_ids, isPatientAttributes) {
        return isPatientAttributes ? this.getPatientClinicalData(attribute_ids) :
          this.getSampleClinicalData(attribute_ids);
      },
      getAllGenePanelSampleIds: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          var _map = {};
          var asyncAjaxCalls = [];
          var responses = [];
          _.each(self.getCancerStudyIds(), function(_studyId) {
            asyncAjaxCalls.push(
              $.ajax({
                url: window.cbioURL + 'api-legacy/genepanel/data',
                contentType: 'application/json',
                data: ['profile_id=' + _studyId + '_mutations', 'genes='].join('&'),
                type: 'GET',
                success: function(_res) {
                  responses.push(_res);
                }
              })
            );
          });
          $.when.apply($, asyncAjaxCalls).done(function() {
            var _panelMetaArr = _.flatten(responses);
            _.each(_panelMetaArr, function(_panelMeta) {
              _map[_panelMeta.stableId] = {};
              var _sorted = (_panelMeta.samples).sort();
              _map[_panelMeta.stableId].samples = _sorted;
              _map[_panelMeta.stableId].sel_samples = _sorted;
            });
            fetch_promise.resolve(_map);
          }).fail(function() {
            fetch_promise.reject();
          });
        }
      ),
      getGenePanelMap: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          self.getAllGenePanelSampleIds().then(function(_panelSampleMap) {
            self.panelSampleMap = _panelSampleMap;
            var asyncAjaxCalls = [];
            var responses = [];
            _.each(Object.keys(_panelSampleMap), function(_panelId) {
              asyncAjaxCalls.push(
                $.ajax({
                  url: window.cbioURL + 'api-legacy/genepanel',
                  contentType: 'application/json',
                  data: {panel_id: _panelId},
                  type: 'GET',
                  success: function(_res) {
                    responses.push(_res);
                  }
                })
              );
            });
            $.when.apply($, asyncAjaxCalls).done(function() {
              var _panelMetaArr = _.map(responses, function(responseArr) {
                return responseArr[0];
              });
              var _map = {};
              _.each(_panelMetaArr, function(_panelMeta) {
                _.each(_panelMeta.genes, function(_gene) {
                  if (!_map.hasOwnProperty(_gene.hugoGeneSymbol)) {
                    _map[_gene.hugoGeneSymbol] = {};
                    _map[_gene.hugoGeneSymbol].panel_id = [];
                    _map[_gene.hugoGeneSymbol].sample_num = 0;
                  }
                  _map[_gene.hugoGeneSymbol].panel_id.push(_panelMeta.stableId);
                  _map[_gene.hugoGeneSymbol].sample_num += _panelSampleMap[_panelMeta.stableId].samples.length;
                });
              });
              fetch_promise.resolve(_map);
            }).fail(function() {
              fetch_promise.reject();
            });
          }, function() {
            fetch_promise.reject();
          });
        }
      ),
      updateGenePanelMap: function(_map, _selectedSampleIds) {
        var _self = this;
        if (typeof _selectedSampleIds === 'undefined') {
          return _map;
        }
        // update panel sample count map
        _selectedSampleIds = _selectedSampleIds.sort();
        _.each(Object.keys(_self.panelSampleMap), function(_panelId) {
          _self.panelSampleMap[_panelId].sel_samples =
            content.util.intersection(_self.panelSampleMap[_panelId].samples, _selectedSampleIds);
        });
        _.each(Object.keys(_map), function(_gene) {
          var _sampleNumPerGene = 0;
          _.each(_map[_gene].panel_id, function(_panelId) {
            _sampleNumPerGene += _self.panelSampleMap[_panelId].sel_samples.length;
          });
          _map[_gene].sample_num = _sampleNumPerGene;
        });
        return _map;
      }
    };
  };

  return content;
})(window.$, window._);
