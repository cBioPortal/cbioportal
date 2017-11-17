'use strict';
// Move vcSession initialization to here since the sessionEvent.js
// is the first one to be called in the dependency list.
window.vcSession = window.vcSession ? window.vcSession : {};

(function(vcSession, _) {
  if (!_.isObject(vcSession)) {
    vcSession = {};
  }
  vcSession.events = (function() {
    return {
      saveCohort: function(stats, name, description) {
        var def = new $.Deferred();
        $.when(vcSession.utils.buildVCObject(stats.filters, stats.studies,
          name, description)).done(function(_virtualCohort) {
          vcSession.model.saveSession(_virtualCohort)
            .done(function(response) {
              def.resolve(response);
            })
            .fail(function() {
              def.reject();
            });
        });
        return def.promise();
      },
      removeVirtualCohort: function(virtualCohort) {
        vcSession.model.removeSession(virtualCohort);
      },
      editVirtualCohort: function(virtualCohort) {
        vcSession.model.editSession(virtualCohort);
      },
      addSampleToVirtualCohort: function(virtualCohortID, cancerStudyID,
                                         sampleID) {
        var _returnString = 'error';
        var _virtualCohorts = vcSession.utils.getVirtualCohorts();
        var _studyMatch = _.findWhere(_virtualCohorts, {
          virtualCohortID: virtualCohortID
        });
        if (typeof _studyMatch === 'undefined') {
          /*
           TODO : if virtual cohort is not present in local storage
           */
          console.log('virtual cohort not found');
        } else {
          var _match = _.findWhere(_studyMatch.studies, {
            id: cancerStudyID
          });
          if (typeof _match === 'undefined') {
            var _selectedCases = vcSession.utils.buildCaseListObject(
              _studyMatch.studies, cancerStudyID, sampleID);
            _studyMatch.studies = _selectedCases;
            // TODO: this is the question I have for a while, should we have
            // individual length property? I understand it's convenient
            // but also easy to get out of sync with the samples array.
            _returnString = 'success';
          } else if (_.contains(_match.samples, sampleID)) {
            _returnString = 'warn';
          } else {
            _match.samples.push(sampleID);
            _returnString = 'success';
          }
          this.editVirtualCohort(_studyMatch);
        }
        return _returnString;
      }
    };
  })();
})(window.vcSession,
  window._);

'use strict';

(function(vcSession, _, $) {
  if (!_.isObject(vcSession)) {
    vcSession = {};
  }
  vcSession.model = (function() {
    var localStorageAdd_ = function(virtualCohort) {
      var _virtualCohorts = vcSession.utils.getVirtualCohorts();
      _virtualCohorts.push(virtualCohort);
      vcSession.utils.setVirtualCohorts(_virtualCohorts);
    };

    var localStorageDelete_ = function(virtualCohort) {
      var _virtualCohorts = vcSession.utils.getVirtualCohorts();
      _virtualCohorts = _.without(_virtualCohorts, _.findWhere(_virtualCohorts,
        {virtualCohortID: virtualCohort.virtualCohortID}));
      vcSession.utils.setVirtualCohorts(_virtualCohorts);
    };

    var localStorageEdit_ = function(updateVirtualCohort) {
      var _virtualCohorts = vcSession.utils.getVirtualCohorts();
      _.extend(_.findWhere(_virtualCohorts, {
        virtualCohortID: updateVirtualCohort.virtualCohortID
      }), updateVirtualCohort);
      vcSession.utils.setVirtualCohorts(_virtualCohorts);
    };

    return {
      saveSession: function(virtualCohort) {
        var def = new $.Deferred();
        $.ajax({
          type: 'POST',
          url: vcSession.URL,
          contentType: 'application/json;charset=UTF-8',
          data: JSON.stringify(virtualCohort)
        }).done(function(response) {
          if (virtualCohort.userID === 'DEFAULT') {
            virtualCohort.virtualCohortID = response.id;
            localStorageAdd_(virtualCohort);
          }
          def.resolve(response);
        }).fail(function() {
          virtualCohort.virtualCohortID = vcSession.utils.generateUUID();
          localStorageAdd_(virtualCohort);
          def.reject();
        });
        return def.promise();
      },
      saveSessionWithoutWritingLocalStorage: function(_virtualCohort, _callbackFunc) {
        $.ajax({
          type: 'POST',
          url: vcSession.URL,
          contentType: 'application/json;charset=UTF-8',
          data: JSON.stringify(_virtualCohort)
        }).done(function(response) {
          if (_virtualCohort.userID === 'DEFAULT') {
            _virtualCohort.virtualCohortID = response.id;
            _callbackFunc(response.id);
          }
        }).fail(function() {
          _virtualCohort.virtualCohortID = vcSession.utils.generateUUID();
          _callbackFunc(response.id);
        });
      },
      removeSession: function(_virtualCohort) {
        // Delete cohort just from browser localstorage
        localStorageDelete_(_virtualCohort);
      },
      editSession: function(_virtualCohort) {
        $.ajax({
          type: 'PUT',
          url: vcSession.URL + '/' + _virtualCohort.virtualCohortID,
          contentType: 'application/json;charset=UTF-8',
          data: JSON.stringify(_virtualCohort)
        }).done(function() {
          if (_virtualCohort.userID === 'DEFAULT') {
            localStorageEdit_(_virtualCohort);
          }
        }).fail(function(jqXHR) {
          // TODO: should we delete the virtual cohort if no record found
          // in the database? Should we add it into database?
          if (jqXHR.status === 404) {
            localStorageDelete_(_virtualCohort);
            vcSession.model.saveSession(_virtualCohort);
          } else {
            localStorageEdit_(_virtualCohort);
          }
        });
      },
      
      // TODO: should we send request without validating userID?
      loadUserVirtualCohorts: function() {
        var def = new $.Deferred();
        $.ajax({
          type: 'GET',
          url: vcSession.URL + '/get-user-cohorts',
          contentType: 'application/json;charset=UTF-8'
        }).done(function(response) {
          var _virtualCohorts = [];
          $.each(response, function(key, val) {
            var _virtualCohort = val.data;
            _virtualCohort.virtualCohortID = val.id;
            _virtualCohorts.push(_virtualCohort);
          });
          def.resolve(_virtualCohorts);
        }).fail(function() {
          def.resolve([]);
        });
        return def.promise();
      }
    };
  })();
})(window.vcSession,
  window._,
  window.$ || window.jQuery);

'use strict';

(function(vcSession, _, $) {
  if (!_.isObject(vcSession)) {
    vcSession = {};
  }
  vcSession.utils = (function() {
    var virtualCohort_ = {
      name: '',
      description: '',
      filters: '',
      studies: ''
    };

    var studies_ = {
      id: '',
      samples: []
    };

    var generateUUID_ = function() {
      var _d = new Date().getTime();
      if (window.performance && typeof window.performance.now === 'function') {
        _d += window.performance.now();
      }
      var _uuid = 'xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx'.replace(/[xy]/g,
        function(c) {
          var r = (_d + Math.random() * 16) % 16 | 0;
          _d = Math.floor(_d / 16);
          return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
        });
      return _uuid;
    };

    // Get Virtual cohorts from Local Storage
    var getVirtualCohorts_ = function() {
      return JSON.parse(localStorage.getItem('virtual-cohorts')) || [];
    };

    // Set Virtual cohorts in Local Storage
    var setVirtualCohorts_ = function(virtualCohorts) {
      localStorage.setItem('virtual-cohorts', JSON.stringify(virtualCohorts));
    };

    var buildVCObject_ = function(filters, cases, name,
                                  description) {
      var def = new $.Deferred();
      var _virtualCohort = $.extend(true, {}, virtualCohort_);
      _virtualCohort.filters = filters;
      _virtualCohort.studies = cases;
      if (name) {
        _virtualCohort.name = name;
      } else {
        _virtualCohort.name = "Selected " + (cases.length > 1 ? "Studies" : "Study");
      }
      _virtualCohort.description = description || '';
      def.resolve(_virtualCohort);
      return def.promise();
    };
    var buildCaseListObject_ = function(selectedCases, cancerStudyID,
                                        sampleID) {
      var _selectedCases = selectedCases;
      var _studies = $.extend(true, {}, studies_);
      _studies.id = cancerStudyID;
      _studies.samples.push(sampleID);
      _selectedCases.push(_studies);
      return _selectedCases;
    };

    var generateCohortDescription_ = function(_cases) {
      var def = new $.Deferred(), _desp = "";
      $.when(window.iviz.datamanager.getCancerStudyDisplayName(_.pluck(_cases, "id"))).done(function(_studyIdNameMap) {
        _.each(_cases, function (_i) {
          _desp += _studyIdNameMap[_i.id] + ": " + _i.samples.length + " samples\n";
        });
        def.resolve(_desp);
      });
      return def.promise();
    }

    return {
      buildVCObject: buildVCObject_,
      setVirtualCohorts: setVirtualCohorts_,
      getVirtualCohorts: getVirtualCohorts_,
      generateUUID: generateUUID_,
      generateCohortDescription: generateCohortDescription_,
      buildCaseListObject: buildCaseListObject_
    };
  })();
})(window.vcSession,
  window._,
  window.$ || window.jQuery);
