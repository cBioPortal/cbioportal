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
    return attr.toLowerCase().match(/(os_survival)|(dfs_survival)|(mut_cnt_vs_cna)|(mutated_genes)|(cna_details)|(^age)|(gender)|(sex)|(os_status)|(os_months)|(dfs_status)|(dfs_months)|(race)|(ethnicity)|(.*type.*)|(.*site.*)|(.*grade.*)|(.*stage.*)|(histology)|(tumor_type)|(subtype)|(tumor_site)|(.*score.*)|(mutation_count)|(copy_number_alterations)/);
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

  content.init = function(_portalUrl, _study_cases_map) {
    var initialSetup = function() {
      var _def = new $.Deferred();
      var self = this;
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
            var _indexSample = 0;
            var _sampleDataIndicesObj = {};
            var _indexPatient = 0;
            var _patientDataIndicesObj = {};
            var _hasDFS = false;
            var _hasOS = false;
            var _hasPatientAttrData = {};
            var _hasSampleAttrData = {};
            var _hasDfsStatus = false;
            var _hasDfsMonths = false;
            var _hasOsStatus = false;
            var _hasOsMonths = false;
            var _cnaCasesMap = {};
            var _sequencedCasesMap = {};
            var _cnaCases = _caseLists.cnaSampleIds.length > 0 ? _caseLists.cnaSampleIds : _caseLists.allSampleIds;
            var _sequencedCases = _caseLists.sequencedSampleIds.length > 0 ? _caseLists.sequencedSampleIds : _caseLists.allSampleIds;
            _.each(_cnaCases, function(_sampleId) {
              _cnaCasesMap[_sampleId] = _sampleId;
            });
            _.each(_sequencedCases, function(_sampleId) {
              _sequencedCasesMap[_sampleId] = _sampleId;
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
                  allCases: _caseLists.allSampleIds,
                  sequencedCases: _caseLists.allSampleIds
                };
              }
              if (['CANCER_TYPE', 'CANCER_TYPE_DETAILED']
                  .indexOf(_metaObj.attr_id) !== -1) {
                if (_.intersection(['mskimpact', 'genie', 'mskimpact_heme'],
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
                  allCases: _caseLists.allSampleIds,
                  sequencedCases: _caseLists.allSampleIds
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

            _hasSampleAttrData.sample_id = '';
            _hasSampleAttrData.study_id = '';
            _hasSampleAttrData.sequenced = '';
            _hasSampleAttrData.has_cna_data = '';
            _.each(_studyToSampleToPatientMap, function(_sampleToPatientMap, _studyId) {
              _.each(_sampleToPatientMap, function(_patientId, _sampleId) {
                if (_samplesToPatientMap[_sampleId] === undefined) {
                  _samplesToPatientMap[_sampleId] = [_patientId];
                }
                if (_patientToSampleMap[_patientId] === undefined) {
                  _patientToSampleMap[_patientId] = [_sampleId];
                } else {
                  _patientToSampleMap[_patientId].push(_sampleId);
                }

                if (_patientDataIndicesObj[_patientId] === undefined) {
                  // create datum for each patient
                  var _patientDatum = {};
                  _patientDatum.patient_id = _patientId;
                  _patientDatum.study_id = _studyId;
                  _hasPatientAttrData.patient_id = '';
                  _hasPatientAttrData.study_id = '';
                  _patientData.push(_patientDatum);
                  _patientDataIndicesObj[_patientId] = _indexPatient;
                  _indexPatient += 1;
                }

                // create datum for each sample
                var _sampleDatum = {};
                _sampleDatum.sample_id = _sampleId;
                _sampleDatum.study_id = _studyId;
                _sampleDatum.has_cna_data = 'NO';
                _sampleDatum.sequenced = 'NO';

                if (self.hasMutationData()) {
                  if (_sequencedCasesMap[_sampleDatum.sample_id] !== undefined) {
                    _sampleDatum.sequenced = 'YES';
                  }
                  _sampleDatum.mutated_genes = [];
                }
                if (self.hasCnaSegmentData()) {
                  if (_cnaCasesMap[_sampleDatum.sample_id] !== undefined) {
                    _sampleDatum.has_cna_data = 'YES';
                  }
                  _sampleDatum.cna_details = [];
                }
                _sampleData.push(_sampleDatum);
                // indices
                _sampleDataIndicesObj[_sampleId] = _indexSample;
                _indexSample += 1;
              });
            });

            // Add sample_count_patient data
            _.each(_patientData, function(datum) {
              _hasPatientAttrData.sample_count_patient = '';
              if (_patientToSampleMap.hasOwnProperty(datum.patient_id)) {
                datum.sample_count_patient = _patientToSampleMap[datum.patient_id].length.toString();
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
                'cbio cancer genes in the cohort.';
              _cnaAttrMeta.attr_id = 'cna_details';
              _cnaAttrMeta.filter = [];
              _cnaAttrMeta.show = true;
              _cnaAttrMeta.keys = {};
              _cnaAttrMeta.numOfDatum = 0;
              _cnaAttrMeta.priority = 4;
              _cnaAttrMeta.attrList = [_cnaAttrMeta.attr_id];
              _cnaAttrMeta.options = {
                allCases: _caseLists.allSampleIds,
                sequencedCases: _cnaCases
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
              _mutDataAttrMeta.description = 'This table shows cbio ' +
                'cancer genes with 1 or more mutations, as well as any ' +
                'gene with 2 or more mutations';
              _mutDataAttrMeta.attr_id = 'mutated_genes';
              _mutDataAttrMeta.filter = [];
              _mutDataAttrMeta.show = true;
              _mutDataAttrMeta.keys = {};
              _mutDataAttrMeta.numOfDatum = 0;
              _mutDataAttrMeta.priority = 3;
              _mutDataAttrMeta.attrList = [_mutDataAttrMeta.attr_id];
              _mutDataAttrMeta.options = {
                allCases: _caseLists.allSampleIds,
                sequencedCases: _sequencedCases
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

            _result.groups = {};
            _result.groups.patient = {};
            _result.groups.sample = {};
            _result.groups.group_mapping = {};
            _result.groups.patient.data = _patientData;
            _result.groups.patient.hasAttrData = _hasPatientAttrData;
            _result.groups.sample.data = _sampleData;
            _result.groups.sample.hasAttrData = _hasSampleAttrData;
            _result.groups.patient.data_indices = {};
            _result.groups.sample.data_indices = {};
            _result.groups.patient.data_indices.patient_id =
              _patientDataIndicesObj;
            _result.groups.sample.data_indices.sample_id =
              _sampleDataIndicesObj;
            _result.groups.group_mapping.sample = {};
            _result.groups.group_mapping.patient = {};
            _result.groups.group_mapping.sample.patient =
              _samplesToPatientMap;
            _result.groups.group_mapping.patient.sample =
              _patientToSampleMap;

            $.when(self.getCnaFractionData(),
              self.getMutationCount(),
              self.getSampleClinicalData(_.keys(_sampleAttributes)),
              self.getPatientClinicalData(_.keys(_patientAttributes)))
              .then(function(_cnaFractionData, _mutationCountData, sampleInitData, patientInitData) {
                var _hasCNAFractionData = _.keys(_cnaFractionData).length > 0;
                var _hasMutationCountData = _.keys(_mutationCountData).length > 0;

                _.each(_result.groups.sample.data, function(_sampleDatum) {
                  var _sampleId = _sampleDatum.sample_id;

                  // mutation count
                  if (_hasMutationCountData) {
                    _hasSampleAttrData.mutation_count = '';
                    if (_mutationCountData[_sampleId] === undefined ||
                      _mutationCountData[_sampleId] === null) {
                      if (_sequencedCasesMap[_sampleDatum.sample_id] === undefined) {
                        _sampleDatum.mutation_count = 'NA';
                      } else {
                        _sampleDatum.mutation_count = 0;
                      }
                    } else {
                      _sampleDatum.mutation_count = _mutationCountData[_sampleId];
                    }
                  }
                  // cna fraction
                  if (_hasCNAFractionData) {
                    _hasSampleAttrData.copy_number_alterations = '';
                    _hasSampleAttrData.cna_fraction = '';
                    if (_cnaFractionData[_sampleId] === undefined ||
                      _cnaFractionData[_sampleId] === null) {
                      _sampleDatum.cna_fraction = 'NA';
                      _sampleDatum.copy_number_alterations = 'NA';
                    } else {
                      _sampleDatum.cna_fraction = _cnaFractionData[_sampleId];
                      _sampleDatum.copy_number_alterations = _cnaFractionData[_sampleId];
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
                  _sampleAttributes[_MutationCountMeta.attr_id] = (_MutationCountMeta);
                }

                // add CNA Table
                if (self.hasCnaSegmentData()) {
                  _hasSampleAttrData.cna_details = '';
                  var _cnaAttrMeta = {};
                  _cnaAttrMeta.type = 'cna';
                  _cnaAttrMeta.view_type = 'table';
                  _cnaAttrMeta.display_name = 'CNA Genes';
                  _cnaAttrMeta.description = 'This table only shows ' +
                    'cbio cancer genes in the cohort.';
                  _cnaAttrMeta.attr_id = 'cna_details';
                  _cnaAttrMeta.filter = [];
                  _cnaAttrMeta.show = true;
                  _cnaAttrMeta.keys = {};
                  _cnaAttrMeta.numOfDatum = 0;
                  _cnaAttrMeta.priority = 4;
                  _cnaAttrMeta.attrList = [_cnaAttrMeta.attr_id];
                  _cnaAttrMeta.options = {
                    allCases: _caseLists.allSampleIds,
                    sequencedCases: _cnaCases
                  };
                  _sampleAttributes[_cnaAttrMeta.attr_id] = _cnaAttrMeta;
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
          cna: {}
        }
      },
      // The reason to separate style variable into individual json is
      // that the scss file can also rely on this file.
      getStyleVars: window.cbio.util.makeCachedPromiseFunction(
        function(self, fetch_promise) {
          $.getJSON(window.cbioResourceURL + 'vars.json')
            .then(function(data) {
              var styles = {
                vars: {}
              };
              styles.vars.width = {
                one: content.util.pxStringToNumber(data['grid-w-1']) || 195,
                two: content.util.pxStringToNumber(data['grid-w-2']) || 400
              };
              styles.vars.height = {
                one: content.util.pxStringToNumber(data['grid-h-1']) || 170,
                two: content.util.pxStringToNumber(data['grid-h-2']) || 350
              };
              styles.vars.chartHeader = 17;
              styles.vars.borderWidth = 2;
              styles.vars.scatter = {
                width: (
                styles.vars.width.two -
                styles.vars.borderWidth) || 400,
                height: (
                styles.vars.height.two -
                styles.vars.chartHeader -
                styles.vars.borderWidth) || 350
              };
              styles.vars.survival = {
                width: styles.vars.scatter.width,
                height: styles.vars.scatter.height
              };
              styles.vars.specialTables = {
                width: styles.vars.scatter.width,
                height: styles.vars.scatter.height - 25
              };
              styles.vars.piechart = {
                width: 140,
                height: 140
              };
              styles.vars.barchart = {
                width: (
                styles.vars.width.two -
                styles.vars.borderWidth) || 400,
                height: (
                styles.vars.height.one -
                styles.vars.chartHeader * 2 -
                styles.vars.borderWidth) || 130
              };
              fetch_promise.resolve(styles);
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
          var _allSampleIds = [];
          var _sequencedSampleIds = [];
          var _cnaSampleIds = [];
          var requests = self.getCancerStudyIds().map(
            function(cancer_study_id) {
              var def = new $.Deferred();
              var sampleListCalls = [];
              self.getSampleListsData(['all', 'sequenced', 'cna'], cancer_study_id)
                .done(function() {
                  if (_.isArray(self.data.sampleLists.sequenced[cancer_study_id])) {
                    _sequencedSampleIds = _sequencedSampleIds.concat(self.data.sampleLists.sequenced[cancer_study_id]);
                  }
                  if (_.isArray(self.data.sampleLists.cna[cancer_study_id])) {
                    _cnaSampleIds = _cnaSampleIds.concat(self.data.sampleLists.cna[cancer_study_id]);
                  }
                  if (_.isArray(self.data.sampleLists.all[cancer_study_id])) {
                    _allSampleIds = _allSampleIds.concat(self.data.sampleLists.all[cancer_study_id]);
                  }
                  def.resolve();
                }).fail(function() {
                fetch_promise.reject();
              });
              return def.promise();
            });
          $.when.apply($, requests).then(function() {
            var _completeSampleLists = {};
            _completeSampleLists.allSampleIds = _allSampleIds.sort();
            _completeSampleLists.sequencedSampleIds =
              _sequencedSampleIds.sort();
            _completeSampleLists.cnaSampleIds = _cnaSampleIds.sort();
            fetch_promise.resolve(_completeSampleLists);
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
          var getSamplesCall = function(cancerStudyId) {
            var def = new $.Deferred();
            window.cbioportal_client.getSamples({
              study_id: [cancerStudyId],
              sample_ids: self.studyCasesMap[cancerStudyId].samples
            }).then(function(data) {
              var sample_to_patient = {};
              var patientList = {};
              for (var i = 0; i < data.length; i++) {
                sample_to_patient[data[i].id] = data[i].patient_id;
                patientList[data[i].patient_id] = '';
              }
              // set patient list in studyCasesMap if sample list is
              // passed in the input
              if (_.isArray(self.studyCasesMap[cancerStudyId].samples) &&
                self.studyCasesMap[cancerStudyId].samples.length > 0) {
                self.studyCasesMap[cancerStudyId].patients = Object.keys(patientList);
              }
              study_to_sample_to_patient[cancerStudyId] = sample_to_patient;
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
            if (!self.data.sampleLists.hasOwnProperty(list)) {
              self.data.samplesLists[list] = {};
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
                  _ajaxCnaFractionData = $.extend({}, response, _ajaxCnaFractionData);
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
          _ajaxCnaData.gene = [];
          _ajaxCnaData.gistic = [];
          _ajaxCnaData.cytoband = [];
          _ajaxCnaData.alter = [];
          _ajaxCnaData.caseIds = [];
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
                  _ajaxCnaData.gene = _ajaxCnaData.gene.concat(response.gene);
                  _ajaxCnaData.gistic = _ajaxCnaData.gistic.concat(response.gistic);
                  _ajaxCnaData.cytoband = _ajaxCnaData.cytoband.concat(response.cytoband);
                  _ajaxCnaData.alter = _ajaxCnaData.alter.concat(response.alter);
                  _ajaxCnaData.caseIds = _ajaxCnaData.caseIds.concat(response.caseIds);
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
                    _ajaxMutationCountData = $.extend({}, response, _ajaxMutationCountData);
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
          var _mutDataStudyIdArr = {};
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
                  _mutDataStudyIdArr = $.extend({}, response, _mutDataStudyIdArr);
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
                contentType: "application/json",
                data: ["profile_id=" + _studyId + "_mutations", "genes="].join("&"),
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
              _map[_panelMeta.stableId]["samples"] = (_panelMeta.samples);
              _map[_panelMeta.stableId]["sel_samples"] = (_panelMeta.samples);
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
                  contentType: "application/json",
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
                return responseArr[0]
              });
              var _map = {};
              _.each(_panelMetaArr, function(_panelMeta) {
                _.each(_panelMeta["genes"], function(_gene) {
                  if (!_map.hasOwnProperty(_gene.hugoGeneSymbol)) {
                    _map[_gene.hugoGeneSymbol] = {};
                    _map[_gene.hugoGeneSymbol]["panel_id"] = [];
                    _map[_gene.hugoGeneSymbol]["sample_num"] = 0;
                  }
                  _map[_gene.hugoGeneSymbol]["panel_id"].push(_panelMeta.stableId);
                  _map[_gene.hugoGeneSymbol]["sample_num"] += _panelSampleMap[_panelMeta.stableId]["samples"].length;
                });
              });
              fetch_promise.resolve(_map);
            }).fail(function() {
              fetch_promise.reject();
            });
          });
        }
      ),
      updateGenePanelMap: function(_map, _selectedSampleIds) {
        var _self = this;
        if (typeof _selectedSampleIds !== 'undefined') {
          //update panel sample count map
          _.each(Object.keys(_self.panelSampleMap), function(_panelId) {
            _self.panelSampleMap[_panelId]["sel_samples"] = _.intersection(_self.panelSampleMap[_panelId]["samples"], _selectedSampleIds);
          });
          _.each(Object.keys(_map), function(_gene) {
            var _sampleNumPerGene = 0;
            _.each(_map[_gene]["panel_id"], function(_panelId) {
              _sampleNumPerGene += _self.panelSampleMap[_panelId]["sel_samples"].length;
            });
            _map[_gene]["sample_num"] = _sampleNumPerGene;
          });
          return _map;
        } else {
          return _map
        }
      }
    };
  };

  return content;
})(window.$, window._);
