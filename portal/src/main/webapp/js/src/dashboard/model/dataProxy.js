'use strict';
window.DataManagerForIviz = (function($, _) {
  var content = {};

  // Clinical attributes will be transferred into table.
  var configs_;
  content.util = {};

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

  /**
   * Normalize clinical data type to all uppercaes.
   * If data type is not STRING or NUMBER, convert it to STRING
   *
   * @param {string} datatype
   * @return {string}
   */
  content.util.normalizeDataType = function(datatype) {
    var invalid = false;
    if (_.isString(datatype)) {
      datatype = datatype.toUpperCase();
      if (['STRING', 'NUMBER'].indexOf(datatype) === -1) {
        invalid = true;
      }
    } else {
      invalid = true;
    }
    if (invalid) {
      datatype = 'STRING';
    }
    return datatype;
  };

  content.init = function(_portalUrl, _study_cases_map) {
    _study_cases_map = _study_cases_map || {};
    _.map(_study_cases_map, function(item) {
      if (_.isArray(item.samples)) {
        item.samples.sort();
      }
    });

    var initialSetup = function() {
      var _def = new $.Deferred();
      var self = this;
      var vueInstance = iViz.vue.manage.getInstance();
      vueInstance.increaseStudyViewSummaryPagePBStatus();
      $.when(self.getSampleLists()).done(function() {
        vueInstance.increaseStudyViewSummaryPagePBStatus();
        $.when(self.getStudyToSampleToPatientMap(), self.getConfigs()).done(function(_studyToSampleToPatientMap, _configs) {
          vueInstance.increaseStudyViewSummaryPagePBStatus();
          $.when(self.getGeneticProfiles(), self.getCaseLists(),
            self.getClinicalAttributesByStudy())
            .done(function(_geneticProfiles, _caseLists,
                           _clinicalAttributes) {
              vueInstance.increaseStudyViewSummaryPagePBStatus();
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
              var _allStudyIds = self.getCancerStudyIds();
              var ismskimpact = _allStudyIds.indexOf('mskimpact') !== -1;

              iViz.priorityManager.setDefaultClinicalAttrPriorities(_configs.priority);

              _.each(_caseLists, function(caseList, studyId) {
                _.each(caseList.cnaSampleIds, function(sampleId) {
                  _cnaCaseUIDs.push(_studyToSampleToPatientMap[studyId].sample_to_uid[sampleId]);
                });
                _.each(caseList.sequencedSampleIds, function(sampleId) {
                  _sequencedCaseUIDs.push(_studyToSampleToPatientMap[studyId].sample_to_uid[sampleId]);
                });
                _.each(caseList.allSampleIds, function(sampleId) {
                  _allCaseUIDs.push(_studyToSampleToPatientMap[studyId].sample_to_uid[sampleId]);
                });
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
                  if (_sampleAttributes[attr.attr_id]) {
                    _sampleAttributes.study_ids.push.apply(_sampleAttributes.study_ids, attr.study_ids);
                    if (attr.priority !== 1) {
                      if (_sampleAttributes[attr.attr_id].priority > 1) {
                        _sampleAttributes[attr.attr_id].priority = (_sampleAttributes[attr.attr_id].priority + attr.priority ) / 2;
                      } else {
                        _sampleAttributes[attr.attr_id].priority = attr.priority;
                      }
                    }
                  } else {
                    _sampleAttributes[attr.attr_id] = attr;
                  }
                } else {
                  if (_patientAttributes[attr.attr_id]) {
                    _patientAttributes.study_ids.push.apply(_patientAttributes.study_ids, attr.study_ids);
                    if (attr.priority !== 1) {
                      if (_patientAttributes[attr.attr_id].priority > 1) {
                        _patientAttributes[attr.attr_id].priority = (_patientAttributes[attr.attr_id].priority + attr.priority ) / 2;
                      } else {
                        _patientAttributes[attr.attr_id].priority = attr.priority;
                      }
                    }
                  } else {
                    _patientAttributes[attr.attr_id] = attr;
                  }
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
                  display_name: '',
                  priority: iViz.priorityManager.getDefaultPriority(data.attr_id),
                  study_ids: _allStudyIds
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
                priority: ismskimpact ? 0 : iViz.priorityManager.getDefaultPriority('sequenced'),
                description: 'If the sample has mutation data'
              }, 'sample');

              addAttr({
                attr_id: 'has_cna_data',
                display_name: 'With CNA Data',
                priority: ismskimpact ? 0 : iViz.priorityManager.getDefaultPriority('has_cna_data'),
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
                _metaObj.addChartBy = 'default';
                if (!_.isArray(_metaObj.priority)) {
                  iViz.priorityManager
                    .setClinicalAttrPriority(_metaObj.attr_id, Number(_metaObj.priority));
                  _metaObj.priority =
                    iViz.priorityManager
                      .getDefaultPriority(_metaObj.attr_id);
                }
                _metaObj.show = _metaObj.priority !== 0;
                _metaObj.attrList = [_metaObj.attr_id];
                _metaObj.datatype = content.util.normalizeDataType(_metaObj.datatype);
                if (_metaObj.datatype === 'NUMBER') {
                  _metaObj.view_type = 'bar_chart';
                  _metaObj.layout = [-1, 2, 'h'];
                } else if (_metaObj.datatype === 'STRING') {
                  _metaObj.view_type = 'pie_chart';
                  _metaObj.layout = [-1, 1];
                }
                if (configs_.tableAttrs.indexOf(_metaObj.attr_id) !== -1) {
                  _metaObj.view_type = 'table';
                  _metaObj.layout = [-1, 4];
                  _metaObj.type = 'pieLabel';
                  _metaObj.options = {
                    allCases: _caseLists.allCaseUIDs,
                    sequencedCases: _caseLists.allCaseUIDs
                  };
                }
                if (['CANCER_TYPE', 'CANCER_TYPE_DETAILED']
                    .indexOf(_metaObj.attr_id) !== -1) {
                  _metaObj.priority =
                    iViz.priorityManager
                      .getDefaultPriority(_metaObj.attr_id);
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
                _metaObj.addChartBy = 'default';
                if (!_.isArray(_metaObj.priority)) {
                  iViz.priorityManager
                    .setClinicalAttrPriority(_metaObj.attr_id, Number(_metaObj.priority));
                  _metaObj.priority =
                    iViz.priorityManager
                      .getDefaultPriority(_metaObj.attr_id);
                }
                _metaObj.show = _metaObj.priority !== 0;
                _metaObj.attrList = [_metaObj.attr_id];
                _metaObj.datatype = content.util.normalizeDataType(_metaObj.datatype);
                if (_metaObj.datatype === 'NUMBER') {
                  _metaObj.view_type = 'bar_chart';
                  _metaObj.layout = [-1, 2, 'h'];
                } else if (_metaObj.datatype === 'STRING') {
                  _metaObj.view_type = 'pie_chart';
                  _metaObj.layout = [-1, 1];
                }
                if (configs_.tableAttrs.indexOf(_metaObj.attr_id) !== -1) {
                  _metaObj.view_type = 'table';
                  _metaObj.layout = [-1, 4];
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
              var _hasMutationData = self.hasMutationData();
              var _hasCnaSegmentData = self.hasCnaSegmentData();

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

                  if (_hasMutationData) {
                    if (_sequencedCaseUIdsMap[_sampleUID] !== undefined) {
                      _sampleDatum.sequenced = 'YES';
                    }
                    _sampleDatum.mutated_genes = [];
                  }
                  if (_hasCnaSegmentData) {
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
                _cnaAttrMeta.layout = [-1, 4];
                _cnaAttrMeta.display_name = 'CNA Genes';
                _cnaAttrMeta.description = 'This table only shows ' +
                  '<a href="cancer_gene_list.jsp" target="_blank">' +
                  'cBioPortal cancer genes</a> in the cohort.';
                _cnaAttrMeta.attr_id = 'cna_details';
                _cnaAttrMeta.filter = [];
                _cnaAttrMeta.addChartBy = 'default';
                _cnaAttrMeta.keys = {};
                _cnaAttrMeta.numOfDatum = 0;
                _cnaAttrMeta.priority =
                  iViz.priorityManager
                    .getDefaultPriority(_cnaAttrMeta.attr_id);
                _cnaAttrMeta.show = _cnaAttrMeta.priority !== 0;
                _cnaAttrMeta.attrList = [_cnaAttrMeta.attr_id];
                _cnaAttrMeta.options = {
                  allCases: _allCaseUIDs,
                  sequencedCases: _cnaCaseUIDs
                };
                _cnaAttrMeta.study_ids = _allStudyIds;
                _sampleAttributes[_cnaAttrMeta.attr_id] = _cnaAttrMeta;
              }

              // add Gene Mutation Info
              if (self.hasMutationData()) {
                _hasSampleAttrData.mutated_genes = '';
                var _mutDataAttrMeta = {};
                _mutDataAttrMeta.type = 'mutatedGene';
                _mutDataAttrMeta.view_type = 'table';
                _mutDataAttrMeta.layout = [-1, 4];
                _mutDataAttrMeta.display_name = 'Mutated Genes';
                _mutDataAttrMeta.description = 'This table shows ' +
                  '<a href="cancer_gene_list.jsp" target="_blank">' +
                  'cBioPortal cancer genes</a> ' +
                  'with 1 or more mutations, as well as any ' +
                  'gene with 2 or more mutations';
                _mutDataAttrMeta.attr_id = 'mutated_genes';
                _mutDataAttrMeta.filter = [];
                _mutDataAttrMeta.addChartBy = 'default';
                _mutDataAttrMeta.keys = {};
                _mutDataAttrMeta.numOfDatum = 0;
                _mutDataAttrMeta.priority =
                  iViz.priorityManager
                    .getDefaultPriority(_mutDataAttrMeta.attr_id);
                _mutDataAttrMeta.show = _mutDataAttrMeta.priority !== 0;
                _mutDataAttrMeta.attrList = [_mutDataAttrMeta.attr_id];
                _mutDataAttrMeta.options = {
                  allCases: _allCaseUIDs,
                  sequencedCases: _sequencedCaseUIDs
                };
                _mutDataAttrMeta.study_ids = _allStudyIds;
                _sampleAttributes[_mutDataAttrMeta.attr_id] = _mutDataAttrMeta;
              }

              if (_hasDFS) {
                var _dfsSurvivalAttrMeta = {};
                _dfsSurvivalAttrMeta.attr_id = 'DFS_SURVIVAL';
                _dfsSurvivalAttrMeta.datatype = 'SURVIVAL';
                _dfsSurvivalAttrMeta.view_type = 'survival';
                _dfsSurvivalAttrMeta.layout = [-1, 4];
                _dfsSurvivalAttrMeta.description = '';
                _dfsSurvivalAttrMeta.display_name = 'Disease Free Survival';
                _dfsSurvivalAttrMeta.filter = [];
                _dfsSurvivalAttrMeta.addChartBy = 'default';
                _dfsSurvivalAttrMeta.keys = {};
                _dfsSurvivalAttrMeta.numOfDatum = 0;
                _dfsSurvivalAttrMeta.priority =
                  iViz.priorityManager
                    .getDefaultPriority('DFS_SURVIVAL', true);
                _dfsSurvivalAttrMeta.show = _dfsSurvivalAttrMeta.priority !== 0;
                _dfsSurvivalAttrMeta.attrList = ['DFS_STATUS', 'DFS_MONTHS'];
                _dfsSurvivalAttrMeta.study_ids = _allStudyIds;
                _patientAttributes[_dfsSurvivalAttrMeta.attr_id] = _dfsSurvivalAttrMeta;
              }

              if (_hasOS) {
                var _osSurvivalAttrMeta = {};
                _osSurvivalAttrMeta.attr_id = 'OS_SURVIVAL';
                _osSurvivalAttrMeta.datatype = 'SURVIVAL';
                _osSurvivalAttrMeta.view_type = 'survival';
                _osSurvivalAttrMeta.layout = [-1, 4];
                _osSurvivalAttrMeta.description = '';
                _osSurvivalAttrMeta.display_name = 'Overall Survival';
                _osSurvivalAttrMeta.filter = [];
                _osSurvivalAttrMeta.addChartBy = 'default';
                _osSurvivalAttrMeta.keys = {};
                _osSurvivalAttrMeta.numOfDatum = 0;
                _osSurvivalAttrMeta.priority =
                  iViz.priorityManager
                    .getDefaultPriority('OS_SURVIVAL', true);
                _osSurvivalAttrMeta.show = _osSurvivalAttrMeta.priority !== 0;
                _osSurvivalAttrMeta.attrList = ['OS_STATUS', 'OS_MONTHS'];
                _osSurvivalAttrMeta.study_ids = _allStudyIds;
                _patientAttributes[_osSurvivalAttrMeta.attr_id] = _osSurvivalAttrMeta;
              }

              // add Cancer Study
              if (_allStudyIds.length > 1) {
                var _id = 'study_id';
                _patientAttributes.study_id = {
                  datatype: 'STRING',
                  description: '',
                  display_name: 'Cancer Studies',
                  attr_id: _id,
                  view_type: 'pie_chart',
                  layout: [-1, 1],
                  filter: [],
                  keys: [],
                  numOfDatum: 0,
                  priority: iViz.priorityManager.getDefaultPriority(_id),
                  show: true,
                  addChartBy: 'default',
                  attrList: [_id],
                  study_ids: _allStudyIds
                };
                _patientAttributes.study_id.show = _patientAttributes.study_id.priority !== 0;
              }
              // add Copy Number Alterations bar chart
              // TODO : need to set priority
              if (_hasSampleAttrData.copy_number_alterations !== undefined) {
                var _id = 'copy_number_alterations';
                _sampleAttributes.copy_number_alterations = {
                  datatype: 'NUMBER',
                  description: '',
                  display_name: 'Fraction of copy number altered genome',
                  attr_id: _id,
                  view_type: 'bar_chart',
                  layout: [-1, 2, 'h'],
                  priority: iViz.priorityManager.getDefaultPriority(_id),
                  filter: [],
                  attrList: [_id],
                  keys: [],
                  numOfDatum: 0,
                  show: true,
                  addChartBy: 'default',
                  study_ids: _allStudyIds
                };
                _sampleAttributes.copy_number_alterations.show = _sampleAttributes.copy_number_alterations.priority !== 0;
              }

              // Pre-calculate whether clinical attribute is preselected
              _.each(_patientAttributes, function(attr) {
                attr.isPreselectedByRegex = self.isPreSelectedClinicalAttr(attr.attr_id);
              });

              // Pre-calculate whether clinical attribute is preselected
              _.each(_sampleAttributes, function(attr) {
                attr.isPreselectedByRegex = self.isPreSelectedClinicalAttr(attr.attr_id);
              });

              _result.groups = {
                group_mapping: {
                  patient_to_sample: _patientToSampleMap,
                  sample_to_patient: _samplesToPatientMap,
                  studyMap: _studyToSampleToPatientMap
                },
                patient: {
                  data: _patientData,
                  has_attr_data: _hasPatientAttrData
                },
                sample: {
                  data: _sampleData,
                  has_attr_data: _hasSampleAttrData
                }
              };

              var _allSampleSttrs = _.pluck(_sampleAttributes, 'attr_id');
              if (_.all(['MUTATION_COUNT', 'FRACTION_GENOME_ALTERED'], function(item) {
                  return _.includes(_allSampleSttrs, item);
                })) {
                // add Mutation count vs. CNA fraction
                var _mutCntAttrMeta = {};
                _mutCntAttrMeta.attr_id = 'MUT_CNT_VS_CNA';
                _mutCntAttrMeta.datatype = 'SCATTER_PLOT';
                _mutCntAttrMeta.view_type = 'scatter_plot';
                _mutCntAttrMeta.layout = [-1, 4];
                _mutCntAttrMeta.description = '';
                _mutCntAttrMeta.display_name = 'Mutation Count vs. CNA';
                _mutCntAttrMeta.filter = [];
                _mutCntAttrMeta.keys = {};
                _mutCntAttrMeta.numOfDatum = 0;
                _mutCntAttrMeta.priority =
                  iViz.priorityManager
                    .getDefaultPriority('MUT_CNT_VS_CNA', true);
                _mutCntAttrMeta.show = _mutCntAttrMeta.priority !== 0;
                _mutCntAttrMeta.addChartBy = 'default';
                _mutCntAttrMeta.attrList = ['MUTATION_COUNT', 'FRACTION_GENOME_ALTERED'];
                // This attribute is used for getScatterData()
                // This should not be added into attribute meta and should be saved into main.js 
                // (Centralized place storing all data for sharing across directives)
                // This needs to be updated after merging into virtual study branch
                _mutCntAttrMeta.sequencedCaseUIdsMap = _sequencedCaseUIdsMap;
                _mutCntAttrMeta.study_ids = _allStudyIds;
                _sampleAttributes[_mutCntAttrMeta.attr_id] = _mutCntAttrMeta;
              }

              if (_allStudyIds.length > 1) {
                _result.groups.patient.attr_meta =
                  self.sortByNumOfStudies(_.values(_patientAttributes));
                _result.groups.sample.attr_meta =
                  self.sortByNumOfStudies(_.values(_sampleAttributes));
              } else {
                _result.groups.patient.attr_meta =
                  self.sortByClinicalPriority(_.values(_patientAttributes));
                _result.groups.sample.attr_meta =
                  self.sortByClinicalPriority(_.values(_sampleAttributes));
              }

              self.initialSetupResult = _result;
              _def.resolve(_result);
            })
            .fail(function(error) {
              _def.reject(error);
            });
        })
          .fail(function(error) {
            _def.reject(error);
          });
      }).fail(function(error) {
        _def.reject(error);
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
        .done(function(attributes) {
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
          var identifiers = [];

          _.each(studyCasesMap, function(content, studyId) {
            identifiers = identifiers.concat(_.map(content.patients, function(patient) {
              return {
                "entityId": patient,
                "studyId": studyId
              }
            }));
          });

          $.ajax({
            type: 'POST',
            url: window.cbioURL + 'api/clinical-data/fetch?clinicalDataType=PATIENT&projection=SUMMARY',
            data: JSON.stringify({
              "attributeIds": attr_ids,
              "identifiers": identifiers
            }),
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
          }).then(function(data) {
            _.each(data, function(item) {
              var uniqueId = item.uniquePatientKey + item.clinicalAttributeId;
              var _data = {
                study_id: item.studyId,
                patient_id: item.patientId,
                attr_id: item.clinicalAttributeId.toUpperCase(),
                attr_val: item.value
              };
              self.data.clinical.patient[uniqueId] = _data;
              clinical_data[_data.attr_id] = clinical_data[_data.attr_id] || [];
              clinical_data[_data.attr_id].push(_data);
            });
            def.resolve(clinical_data);
          }).fail(function() {
            def.reject('Failed to load patient clinical data.');
          });
        })
        .fail(function(error) {
          def.reject(error);
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

          var identifiers = [];

          _.each(studyCasesMap, function(content, studyId) {
            identifiers = identifiers.concat(_.map(content.samples, function(sample) {
              return {
                "entityId": sample,
                "studyId": studyId
              }
            }));
          });

          $.ajax({
            type: 'POST',
            url: window.cbioURL + 'api/clinical-data/fetch?clinicalDataType=SAMPLE&projection=SUMMARY',
            data: JSON.stringify({
              "attributeIds": attr_ids,
              "identifiers": identifiers
            }), dataType: 'json',
            contentType: "application/json; charset=utf-8",
          }).then(function(data) {
            _.each(data, function(item) {
              var uniqueId = item.uniquePatientKey + item.clinicalAttributeId;
              var _data = {
                study_id: item.studyId,
                sample_id: item.sampleId,
                attr_id: item.clinicalAttributeId.toUpperCase(),
                attr_val: item.value
              };
              self.data.clinical.patient[uniqueId] = _data;
              clinical_data[_data.attr_id] = clinical_data[_data.attr_id] || [];
              clinical_data[_data.attr_id].push(_data);
            });
            def.resolve(clinical_data);
          }).fail(function() {
            def.reject('Failed to load patient clinical data.');
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
      getCNAProfileIdByStudyId: function(studyId) {
        return this.cnaProfileIdsMap[studyId];
      },
      getMutationProfileIdByStudyId: function(studyId) {
        return this.mutationProfileIdsMap[studyId];
      },
      hasMutationData: function() {
        return Object.keys(this.mutationProfileIdsMap).length > 0;
      },
      hasCnaSegmentData: function() {
        return Object.keys(this.cnaProfileIdsMap).length > 0;
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
        studies: {},
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
      apiStatus: {
        allPhysicalStudies: '',
        allVirtualStudies: ''
      },
      unknownSamples: [],
      // The reason to separate style variable into individual json is
      // that the scss file can also rely on this file.
      getConfigs: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          if (_.isObject(configs_)) {
            fetch_promise.resolve(configs_);
          } else {
            $.getJSON(window.cbioResourceURL + 'configs.json?' + window.appVersion)
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
                configs_ = configs;
                fetch_promise.resolve(configs);
              })
              .fail(function() {
                fetch_promise.resolve();
              });
          }
        }),
      getGeneticProfiles: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          $.get(window.cbioURL + 'api/molecular-profiles?projection=SUMMARY&pageSize=100000&pageNumber=0&sortBy=molecularProfileId&direction=ASC')
            .done(function(data) {
              var profiles = {};
              _.each(data, function(profile) {
                var _profile = profiles[profile.studyId] || [];
                _profile.push({
                  id: profile.molecularProfileId,
                  study_id: profile.studyId,
                  datatype: profile.datatype,
                  genetic_alteration_type: profile.molecularAlterationType
                });
                profiles[profile.studyId] = _profile;
              });
              var selectedProfiles = _.pick(profiles, self.getCancerStudyIds());
              _.each(selectedProfiles, function(studyProfiles) {
                _.each(studyProfiles, function(_profile) {
                  if (_profile.genetic_alteration_type === 'COPY_NUMBER_ALTERATION' && _profile.datatype === 'DISCRETE') {
                    self.cnaProfileIdsMap[_profile.study_id] = _profile.id;
                  } else if (_profile.genetic_alteration_type === 'MUTATION_EXTENDED' && (_profile.study_id + '_mutations_uncalled' !== _profile.id)) {
                    self.mutationProfileIdsMap[_profile.study_id] = _profile.id;
                  }
                });
              });
              fetch_promise.resolve(_.flatten(_.values(selectedProfiles), true));
            })
            .fail(function(error) {
              fetch_promise.reject(error);
            });
        }),
      getCaseLists: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          var _responseStudyCaseList = {};
          var _sampleLists = [];

          _.each(self.getCancerStudyIds(), function(studyId) {
            var neededList = [];
            _.each(['all', 'sequenced', 'cna'], function(type) {
              neededList.push(studyId + '_' + type);
            });
            _sampleLists.push.apply(_sampleLists, _.intersection(self.data.sampleLists.lists[studyId] || [], neededList));
          });

          self.getSampleListsData(_sampleLists)
            .done(function(data) {
              _.each(data, function(list) {
                self.data.sampleLists[list.studyId] = self.data.sampleLists[list.studyId] || {};
                self.data.sampleLists[list.studyId][list.sampleListId] = list.sampleIds.sort();
              });

              _.each(self.getCancerStudyIds(), function(studyId) {
                _responseStudyCaseList[studyId] = {
                  sequencedSampleIds: [],
                  cnaSampleIds: [],
                  allSampleIds: []
                };
                // Always check for all lists, the API call may fail partially
                if (_.isArray(self.data.sampleLists[studyId][studyId + '_sequenced'])) {
                  _responseStudyCaseList[studyId].sequencedSampleIds = iViz.util.intersection(self.data.sampleLists[studyId][studyId + '_sequenced'], self.studyCasesMap[studyId].samples);
                  self.data.sampleLists.sequenced[studyId] = _responseStudyCaseList[studyId].sequencedSampleIds;
                }
                if (_.isArray(self.data.sampleLists[studyId][studyId + '_cna'])) {
                  _responseStudyCaseList[studyId].cnaSampleIds = iViz.util.intersection(self.data.sampleLists[studyId][studyId + '_cna'], self.studyCasesMap[studyId].samples);
                  self.data.sampleLists.cna[studyId] = _responseStudyCaseList[studyId].cnaSampleIds;
                }
                if (_.isArray(self.data.sampleLists[studyId][studyId + '_all'])) {
                  _responseStudyCaseList[studyId].allSampleIds = iViz.util.intersection(self.data.sampleLists[studyId][studyId + '_all'], self.studyCasesMap[studyId].samples);
                  self.data.sampleLists.all[studyId] = _responseStudyCaseList[studyId].allSampleIds;
                }
              });
              fetch_promise.resolve(_responseStudyCaseList);
            })
            .fail(function(error) {
              fetch_promise.reject(error);
            });
        }),

      // Server side uses uppercase clinical attribute ID as convention but the rule is not strictly followed yet.
      // Manually convert all IDs in front-end to prevent any discrepancy between clinical meta and clinical sample/patient data
      // In the refactoring effort, this needs to be verified again with backend team.
      getClinicalAttributesByStudy: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          $.get(window.cbioURL + 'api/clinical-attributes?projection=SUMMARY&pageSize=100000&pageNumber=0&direction=ASC')
            .done(function(data) {
              var attributes = {};
              _.each(data, function(attribute) {
                var _attribute = attributes[attribute.studyId] || [];
                _attribute.push({
                  attr_id: attribute.clinicalAttributeId.toUpperCase(),
                  datatype: attribute.datatype,
                  description: attribute.description,
                  display_name: attribute.displayName,
                  is_patient_attribute: attribute.patientAttribute ? "1" : "0",
                  priority: Number(attribute.priority)
                });
                attributes[attribute.studyId] = _attribute;
              });
              var selectedAttributes = _.pick(attributes, self.getCancerStudyIds());
              var clinical_attributes_set = {};
              _.each(selectedAttributes, function(studyAttributes, studyId) {
                _.each(studyAttributes, function(_attribute) {
                  // TODO : Need to update logic incase if multiple studies
                  // have same attribute name but different properties
                  var attrId = _attribute.attr_id;
                  if (clinical_attributes_set[attrId] === undefined) {
                    _attribute.study_ids = [studyId];
                    clinical_attributes_set[attrId] = _attribute;
                  } else {
                    _attribute.study_ids =
                      clinical_attributes_set[attrId]
                        .study_ids.concat(studyId);
                    clinical_attributes_set[attrId] = _attribute;
                  }
                });
              });
              fetch_promise.resolve(clinical_attributes_set);
            })
            .fail(function(error) {
              fetch_promise.reject(error);
            });
        }),
      getStudyToSampleToPatientMap: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          var study_to_sample_to_patient = {};
          var _sample_uid = 0;
          var _patient_uid = 0
          var getSamplesCall = function() {
            var def = new $.Deferred();
            var data = [];
            _.each(self.getCancerStudyIds(), function(studyId) {
              data = data.concat(_.map(self.studyCasesMap[studyId].samples, function(sample) {
                return {
                  sampleId: sample,
                  studyId: studyId
                };
              }));
            });
            $.ajax({
              type: 'POST',
              url: window.cbioURL + 'api/samples/fetch?projection=SUMMARY',
              data: JSON.stringify({
                "sampleIdentifiers": data
              }),
              dataType: 'json',
              contentType: "application/json; charset=utf-8",
            }).done(function(data) {
              var patient_to_sample = {};
              var sample_to_patient = {};
              var sample_uid_to_patient_uid = {};
              var uid_to_sample = {};
              var sample_to_uid = {};
              var patient_to_uid = {};
              var uid_to_patient = {};
              var patientList = {};
              for (var i = 0; i < data.length; i++) {
                var _dataItem = data[i];
                var _studyId = _dataItem.studyId;

                patientList[_studyId] = patientList[_studyId] || {};
                patient_to_sample[_studyId] = patient_to_sample[_studyId] || {};
                sample_to_patient[_studyId] = sample_to_patient[_studyId] || {};
                sample_uid_to_patient_uid[_studyId] = sample_uid_to_patient_uid[_studyId] || {};
                uid_to_sample[_studyId] = uid_to_sample[_studyId] || {};
                uid_to_sample[_studyId] = uid_to_sample[_studyId] || {};
                sample_to_uid[_studyId] = sample_to_uid[_studyId] || {};
                patient_to_uid[_studyId] = patient_to_uid[_studyId] || {};
                uid_to_patient[_studyId] = uid_to_patient[_studyId] || {};

                uid_to_sample[_studyId][_sample_uid] = data[i].sampleId;
                sample_to_uid[_studyId][data[i].sampleId] = _sample_uid.toString();
                if (patient_to_uid[_studyId][data[i].patientId] === undefined) {
                  uid_to_patient[_studyId][_patient_uid] = data[i].patientId;
                  patient_to_uid[_studyId][data[i].patientId] = _patient_uid.toString();
                  _patient_uid++;
                }
                if (!patient_to_sample[_studyId].hasOwnProperty(data[i].patientId)) {
                  patient_to_sample[_studyId][data[i].patientId] = {};
                }
                patient_to_sample[_studyId][data[i].patientId][data[i].sampleId] = 1;
                sample_to_patient[_studyId][data[i].sampleId] = data[i].patientId;
                sample_uid_to_patient_uid[_studyId][_sample_uid] = patient_to_uid[_studyId][data[i].patientId];
                patientList[_studyId][data[i].patientId] = 1;
                _sample_uid++;
              }

              _.each(self.getCancerStudyIds(), function(studyId) {
                // set patient list in studyCasesMap if sample list is
                // passed in the input
                if (_.isArray(self.studyCasesMap[studyId].samples) &&
                  self.studyCasesMap[studyId].samples.length > 0) {
                  self.studyCasesMap[studyId].patients = Object.keys(patientList[studyId]);
                }

                var _resultMap = {};

                _resultMap.uid_to_sample = uid_to_sample[studyId];
                _resultMap.uid_to_patient = uid_to_patient[studyId];
                _resultMap.sample_to_uid = sample_to_uid[studyId];
                _resultMap.patient_to_uid = patient_to_uid[studyId];
                _resultMap.sample_to_patient = sample_to_patient[studyId];
                _resultMap.patient_to_sample = _.mapObject(patient_to_sample[studyId], function(item) {
                  return _.keys(item);
                });
                _resultMap.sample_uid_to_patient_uid = sample_uid_to_patient_uid[studyId];
                study_to_sample_to_patient[studyId] = _resultMap;
              });
              def.resolve(study_to_sample_to_patient);
            }).fail(function(error) {
              def.reject(error);
            });
            return def.promise();
          };

          var _sampleLists = [];
          _.each(self.getCancerStudyIds(), function(studyId) {
            self.data.sampleLists[studyId] = self.data.sampleLists[studyId] || {};
            var _existLists = self.data.sampleLists.lists[studyId] || [];
            if (_existLists.indexOf(studyId + '_all') !== -1) {
              _sampleLists.push(studyId + '_all');
            }
          });

          self.getSampleListsData(_sampleLists)
            .done(function(data) {
              _.each(data, function(list) {
                self.data.sampleLists[list.studyId] = self.data.sampleLists[list.studyId] || {};
                self.data.sampleLists[list.studyId][list.sampleListId] = list.sampleIds.sort();
              });

              _.each(self.getCancerStudyIds(), function(studyId) {
                if (!self.studyCasesMap[studyId]) {
                  self.studyCasesMap[studyId] = {};
                }

                if (!_.isArray(self.studyCasesMap[studyId].samples)) {
                  self.studyCasesMap[studyId].samples =
                    self.data.sampleLists[studyId].hasOwnProperty(studyId + '_all') ?
                      self.data.sampleLists[studyId][studyId + '_all'] : [];
                } else {
                  var studySamples = self.data.sampleLists[studyId].hasOwnProperty(studyId + '_all') ?
                    self.data.sampleLists[studyId][studyId + '_all'] : [];
                  var studySamplesSet = _.reduce(studySamples, function(acc, next) {
                    acc[next] = true;
                    return acc;
                  }, {});
                  var inputStudySamples = self.studyCasesMap[studyId].samples;
                  var unknownSamples = [];
                  var filteredSamples = _.filter(inputStudySamples, function(sample) {
                    if (!studySamplesSet[sample]) {
                      unknownSamples.push(sample);
                    }
                    return studySamplesSet[sample];
                  });

                  if (unknownSamples.length > 0) {
                    self.unknownSamples.push({
                      studyId: studyId,
                      samples: unknownSamples
                    });
                  }
                  self.studyCasesMap[studyId].samples = filteredSamples;
                }
              });
              getSamplesCall()
                .done(function(data) {
                  fetch_promise.resolve(data);
                })
                .fail(function(error) {
                  fetch_promise.reject(error);
                });
            })
            .fail(function() {
              fetch_promise.reject('Failed to load sample list from study.');
            });
        }),
      getSampleListsData: function(sampleLists) {
        var def = new $.Deferred();
        var self = this;
        var data = [];
        if (_.isArray(sampleLists)) {
          _.each(sampleLists, function(list) {
            if (!self.data.sampleLists.hasOwnProperty(list)) {
              data.push(list);
            }
          });
          if (data.length === 0) {
            def.resolve();
          } else {
            $.ajax({
              type: 'POST',
              url: window.cbioURL + 'api/sample-lists/fetch?projection=DETAILED',
              data: JSON.stringify(data),
              dataType: 'json',
              contentType: "application/json; charset=utf-8",
            }).done(function(data) {
              def.resolve(data);
            }).fail(function() {
              def.resolve();
            });
          }
        } else {
          def.reject();
        }
        return def.promise();
      },
      getSampleLists: function() {
        var def = new $.Deferred();
        var self = this;

        if (!self.data.sampleLists.hasOwnProperty('lists')) {
          self.data.sampleLists.lists = {};
        }
        $.get(window.cbioURL + 'api/sample-lists')
          .done(function(data) {
            var lists = {};
            _.each(data, function(list) {
              var _list = lists[list.studyId] || [];
              _list.push(list);
              lists[list.studyId] = _list;
            });
            var selectedLists = _.pick(lists, self.getCancerStudyIds());
            _.each(selectedLists, function(studyLists, studyId) {
              self.data.sampleLists.lists[studyId] = _.pluck(studyLists, 'sampleListId');
            });
            def.resolve();
          })
          .fail(function(error) {
            def.reject(error);
          });
        return def.promise();
      },
      getAllMutatedGeneSamples: function() {
        var samples = {};
        var self = this;
        _.each(Object.keys(self.studyCasesMap), function(studyId) {
          samples[studyId] = self.data.sampleLists.sequenced[studyId] || self.data.sampleLists.all[studyId]
        });
        return samples;
      },
      getAllCNASamples: function() {
        var samples = {};
        var self = this;
        _.each(Object.keys(self.studyCasesMap), function(studyId) {
          samples[studyId] = self.data.sampleLists.cna[studyId] || self.data.sampleLists.all[studyId]
        });
        return samples;
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
                  if (Object.keys(response).length > 0) {
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
          $.when.apply($, fetch_promises)
            .done(function() {
              fetch_promise.resolve(_ajaxCnaFractionData);
            })
            .fail(function() {
              fetch_promise.resolve([]);
            });
        }),
      getCnaData: function(progressFunction) {
        var _ajaxCnaData = {};
        var fetch_promises = [];
        var self = this;
        var _cnaProfiles = self.cnaProfileIdsMap;
        var _studyCasesMap = self.getStudyCasesMap();
        var fetch_promise = new $.Deferred();

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
                _def.notify();
                _def.resolve();
              },
              error: function() {
                fetch_promise.reject();
              }
            });
            return _def.promise();
          }));
        $.when.apply($, fetch_promises)
          .done(function() {
            fetch_promise.resolve(_ajaxCnaData);
          })
          .fail(function() {
            fetch_promise.reject();
          })
          .progress(function(data) {
            if (_.isFunction(progressFunction)) {
              progressFunction(data);
            }
          });
        return fetch_promise.promise();
      },
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
      getMutData: function(progressFunction) {
        var fetch_promise = new $.Deferred();
        var fetch_promises = [];
        var _mutDataStudyIdArr = [];
        var _mutationProfiles = this.mutationProfileIdsMap;
        var _studyCasesMap = this.getStudyCasesMap();
        var self = this;
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
                _def.notify();
                _def.resolve();
              },
              error: function() {
                fetch_promise.reject();
              }
            });
            return _def.promise();
          }));
        $.when.apply($, fetch_promises)
          .done(function() {
            fetch_promise.resolve(_mutDataStudyIdArr);
          })
          .fail(function() {
            fetch_promise.reject();
          })
          .progress(function(data) {
            if (_.isFunction(progressFunction)) {
              progressFunction(data);
            }
          });
        return fetch_promise.promise();
      },
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
      getGenePanelMap: function(hugoSymbols, map) {
        var _def = new $.Deferred();
        var panelSamplesMap = {};
        var geneSampleMap = {};
        var isProfiledPanelId = 'profiled';

        $.ajax({
          type: 'POST',
          url: window.cbioURL + 'api/gene-panel-data/fetch',
          data: JSON.stringify({
            "sampleMolecularIdentifiers": map
          }),
          dataType: 'json',
          contentType: "application/json; charset=utf-8",
        }).then(function(data) {
          _.each(data, function(datum) {
            var _panelId = datum.genePanelId;
            if (!_panelId & datum.profiled) {
              _panelId = isProfiledPanelId;
            }
            if (_panelId) {
              if (!panelSamplesMap[_panelId]) {
                panelSamplesMap[_panelId] = [];
              }
              panelSamplesMap[_panelId].push(iViz.getCaseIndex('sample', datum.studyId, datum.sampleId));
            }
          });
          var _panels = _.filter(Object.keys(panelSamplesMap), function(item) {
            return item !== isProfiledPanelId;
          });

          if (_panels.length > 0) {
            $.ajax({
              type: 'POST',
              url: window.cbioURL + 'api/gene-panels/fetch?projection=DETAILED',
              data: JSON.stringify(_panels),
              dataType: 'json',
              contentType: "application/json; charset=utf-8",
              success: function(panels) {
                _.each(panels, function(panel) {
                  _.each(panel.genes, function(gene) {
                    if (!geneSampleMap[gene.hugoGeneSymbol]) {
                      geneSampleMap[gene.hugoGeneSymbol] = {
                        sampleUids: []
                      }
                    }
                    geneSampleMap[gene.hugoGeneSymbol].sampleUids = geneSampleMap[gene.hugoGeneSymbol].sampleUids.concat(panelSamplesMap[panel.genePanelId]);
                  })
                });
                if (panelSamplesMap.hasOwnProperty(isProfiledPanelId)) {
                  _.each(geneSampleMap, function(item) {
                    item.sampleUids = item.sampleUids.concat(panelSamplesMap[isProfiledPanelId]);
                  });
                }

                var _sortedMap = {};
                _.each(geneSampleMap, function(item) {
                  var _json = JSON.stringify(item.sampleUids);
                  if (_sortedMap.hasOwnProperty(_json)) {
                    item.sampleUids = _sortedMap[_json];
                  } else {
                    item.sampleUids = item.sampleUids.sort();
                    _sortedMap[_json] = item.sampleUids;
                  }
                });
                _def.resolve(geneSampleMap);
              },
              fail: function() {
                _def.reject();
              }
            });
          } else {
            _.each(geneSampleMap, function(item) {
              item.sampleUids = panelSamplesMap[isProfiledPanelId].sort();
            });
            _def.resolve(geneSampleMap);
          }
        }, function() {
          _def.reject();
        });
        return _def.promise();
      },
      updateGenePanelMap: function(_map, _selectedSampleUids) {
        if (_selectedSampleUids) {
          _selectedSampleUids = _selectedSampleUids.sort();
        }

        _.each(_map, function(item) {
          item.sampleNum = content.util.intersection(item.sampleUids, _selectedSampleUids).length;
        });

        return _map;
      },

      /**
       * General pick clinical attributes based on predesigned Regex
       * This filter is the same one which used in previous Google Charts Version,
       * should be revised later.
       *
       * @param {string} attr Clinical attribute ID.
       * @return {boolean} Whether input attribute passed the criteria.
       */
      isPreSelectedClinicalAttr: function(attr) {
        var result = attr.match(/(os_survival)|(dfs_survival)|(mut_cnt_vs_cna)|(mutated_genes)|(cna_details)|(^age)|(gender)|(sex)|(os_status)|(os_months)|(dfs_status)|(dfs_months)|(race)|(ethnicity)|(sample_type)|(histology)|(tumor_type)|(subtype)|(tumor_site)|(mutation_count)|(fraction_genome_altered)|(.*(site|grade|stage).*)/i);
        return _.isArray(result) && result.length > 0;
      },

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
      compareClinicalAvailability: function(a, b) {
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
      },

      /**
       * There are few steps to determine the priority.
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
      sortClinicalAttrs: function(array) {
        var self = this;
        array = array.sort(function(a, b) {
          return self.compareClinicalAttrs(a, b);
        });
        return array;
      },

      compareClinicalAttrs: function(a, b) {
        var priority = 0;
        var self = this;

        if (a.isPreselectedByRegex) {
          if (b.isPreselectedByRegex) {
            priority = self.compareClinicalAvailability(a, b);
          } else {
            priority = -1;
          }
        } else if (b.isPreselectedByRegex) {
          priority = 1;
        } else {
          priority = 0;
        }

        if (priority !== 0) {
          return priority;
        }

        return self.compareClinicalAvailability(a, b);
      },

      /**
       * Sort clinical attributes by priority.
       * @param {array} array Clinical attributes.
       * @return {array} Sorted clinical attributes.
       */
      sortByClinicalPriority: function(array) {
        var self = this;
        if (_.isArray(array)) {
          array = array.sort(function(a, b) {
            var score = iViz.priorityManager.comparePriorities(a.priority, b.priority, false);
            if (score === 0) {
              score = self.compareClinicalAttrs(a, b);
            }
            return score;
          });
        }
        return array;
      },

      sortByNumOfStudies: function(array) {
        var self = this;
        if (_.isArray(array)) {
          array = array.sort(function(a, b) {
            var score = b.study_ids.length - a.study_ids.length;
            if (score === 0) {
              score = iViz.priorityManager.comparePriorities(a.priority, b.priority, false);
              if (score === 0) {
                score = self.compareClinicalAttrs(a, b);
              }
            }
            return score;
          });
        }
        return array;
      },

      getAllVirtualStudies: function() {
        var _def = new $.Deferred();
        var _self = this;
        if (_self.apiStatus.allVirtualStudies === 'fetched') {
          _def.resolve(_.filter(_self.data.studies, function(t) {
            return t.studyType === 'vs';
          }));
        } else {
          $.get(window.cbioURL + 'api-legacy/proxy/session/virtual_study')
            .done(function(virtualStudies) {
              _.each(virtualStudies, function(study) {
                study.studyType = 'vs';
                _self.data.studies[study.id] = study;
              });
              _def.resolve(virtualStudies);
            })
            .fail(function(error) {
              _def.resolve([]);
            })
            .always(function() {
              _self.apiStatus.allVirtualStudies = 'fetched';
            });
        }
        return _def.promise();
      },

      getVirtualStudy: function(id) {
        var def = new $.Deferred();
        var self = this;
        if (self.data.studies[id]) {
          def.resolve(self.data.studies[id]);
        } else {
          $.get(window.cbioURL + 'api-legacy/proxy/session/virtual_study/' + id)
            .done(function(response) {
              response.studyType = 'vs';
              self.data.studies[id] = response;
              def.resolve(response);
            })
            .fail(function(error) {
              def.reject(error);
            });
        }
        return def.promise();
      },

      getAllPhysicalStudies: function() {
        var _def = new $.Deferred();
        var _self = this;
        if (_self.apiStatus.allPhysicalStudies === 'fetched') {
          _def.resolve(_.filter(_self.data.studies, function(t) {
            return t.studyType === 'regular';
          }));
        } else {
          $.get(window.cbioURL + 'api/studies')
            .done(function(response) {
              _.each(response, function(study) {
                study.studyType = 'regular';
                _self.data.studies[study.studyId] = study;
              });
              _def.resolve(response);
            })
            .fail(function(error) {
              _def.reject(error);
            })
            .always(function() {
              _self.apiStatus.allPhysicalStudies = 'fetched';
            });
        }
        return _def.promise();
      },

      getStudyById: function(id) {
        return this.data.studies[id];
      },

      getCancerStudyDisplayName: function(_cancerStudyStableIds) {
        var _map = {};
        var _self = this;
        _.each(_cancerStudyStableIds, function(_csId) {
          if (_self.data.studies.hasOwnProperty(_csId)) {
            var _study = _self.data.studies[_csId];
            var _id = _study.studyType === 'vs' ? _study.id : _study.studyId;
            _map[_id] = _study.studyType === 'vs' ? _study.data.name : _study.name;
          }
        });
        return _map;
      }
    };
  };

  return content;
})(window.$, window._);
