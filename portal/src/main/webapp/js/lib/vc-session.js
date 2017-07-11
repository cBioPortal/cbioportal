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
        $.when(vcSession.utils.buildVCObject(stats.filters, stats.selectedCases,
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
          var _match = _.findWhere(_studyMatch.selectedCases, {
            studyID: cancerStudyID
          });
          if (typeof _match === 'undefined') {
            var _selectedCases = vcSession.utils.buildCaseListObject(
              _studyMatch.selectedCases, cancerStudyID, sampleID);
            _studyMatch.selectedCases = _selectedCases;
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
  vcSession.utils = (function() {
    var virtualCohort_ = {
      studyName: '',
      description: '',
      userID: 'DEFAULT',
      created: '',
      filters: '',
      selectedCases: ''
    };

    var selectedCase_ = {
      studyID: '',
      samples: [],
      patients: []
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
      _virtualCohort.selectedCases = cases;
      _virtualCohort.created = new Date().getTime();
      if (name) {
        _virtualCohort.studyName = name;
      } else {
        _virtualCohort.studyName = "Custom Cohort (" + new Date().toISOString().replace(/T/, ' ') + ")";
      }
      if (description) {
        _virtualCohort.description = description;
        def.resolve(_virtualCohort);
      } else {
        $.when(generateCohortDescription_(cases)).done(function(_desp) {
          _virtualCohort.description = _desp;
          def.resolve(_virtualCohort);
        });
      }
      return def.promise();
    };
    var buildCaseListObject_ = function(selectedCases, cancerStudyID,
                                        sampleID) {
      var _selectedCases = selectedCases;
      var _selectedCase = $.extend(true, {}, selectedCase_);
      _selectedCase.studyID = cancerStudyID;
      _selectedCase.samples.push(sampleID);
      _selectedCases.push(_selectedCase);
      return _selectedCases;
    };

    var generateCohortDescription_ = function(_cases) {
      var def = new $.Deferred(), _desp = "";
      $.when(window.iviz.datamanager.getCancerStudyDisplayName(_.pluck(_cases, "studyID"))).done(function(_studyIdNameMap) {
        _.each(_cases, function (_i) {
          _desp += _studyIdNameMap[_i.studyID] + ": " + _i.samples.length + " samples / " + _i.patients.length + " patients\n";
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
       /* $.ajax({
          type: 'DELETE',
          url: vcSession.URL + '/' + _virtualCohort.virtualCohortID,
          contentType: 'application/json;charset=UTF-8'
        }).done(function() {
          if (_virtualCohort.userID === 'DEFAULT') {
            localStorageDelete_(_virtualCohort);
          }
        }).fail(function() {
          localStorageDelete_(_virtualCohort);
        });*/
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
      /*
      This method would be used in cbio to get user specific cohorts
       // TODO: should we send request without validating userID?
       */
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
(function(Vue, $, vcSession) {
  Vue.component('sessionComponent', {
    template: 
    '<div v-if="showSaveButton" class="btn btn-default iviz-header-button"><span type="button" ' +
    'class="save-cohort-btn ">' +
    '<i class="fa fa-floppy-o" alt="Save Cohort"></i></span></div>',
    props: [
      'selectedPatientsNum', 'selectedSamplesNum',
      'stats', 'updateStats', 'showSaveButton'
    ],
    data: function() {
      return {
        savedVC: null
      };
    }, methods: {
      saveCohort: function() {
        var _self = this;
        _self.updateStats = true;
        _self.$nextTick(function() {
          _self.addNewVC = true;
        });
      }
    }, ready: function() {
      var self_ = this;
      if (this.showSaveButton) {
        $('.iviz-header-button').qtip({
          style: {
            classes: 'qtip-light qtip-rounded qtip-shadow'
          },
          show: {event: 'mouseover', ready: false},
          hide: {fixed: true, delay: 200, event: 'mouseleave'},
          position: {
            my: 'bottom center',
            at: 'top center',
            viewport: $(window)
          },
          content: 'Save Cohort'
        });
        $('.save-cohort-btn').qtip({
          style: {
            classes: 'qtip-light qtip-rounded qtip-shadow ' +
            'iviz-save-cohort-btn-qtip'
          },
          show: {event: 'click', ready: false},
          hide: false,
          position: {
            my: 'top center',
            at: 'bottom center',
            viewport: $(window)
          },
          events: {
            render: function(event, api) {
              var tooltip = $('.iviz-save-cohort-btn-qtip .qtip-content');
              tooltip.find('.save-cohort').click(function() {
                tooltip.find('.saving').css('display', 'block');
                tooltip.find('.close-dialog').css('display', 'none');
                tooltip.find('.saved').css('display', 'none');
                tooltip.find('.dialog').css('display', 'none');
                api.reposition();

                var cohortName = tooltip.find('.cohort-name').val();
                var cohortDescription =
                  tooltip.find('.cohort-description').val();
                if (_.isObject(vcSession)) {
                  self_.updateStats = true;
                  self_.$nextTick(function() {
                    var _selectedSamplesNum = 0;
                    var _selectedPatientsNum = 0;
                    if (_.isObject(self_.stats.selectedCases)) {
                      _.each(self_.stats.selectedCases, function(studyCasesMap) {
                        _selectedSamplesNum += studyCasesMap.samples.length;
                        _selectedPatientsNum += studyCasesMap.patients.length;
                      });
                      self_.selectedSamplesNum = _selectedSamplesNum;
                      self_.selectedPatientsNum = _selectedPatientsNum;
                    }

                    vcSession.events.saveCohort(self_.stats,
                      cohortName, cohortDescription || '')
                      .done(function(response) {
                        self_.savedVC = response;
                        tooltip.find('.savedMessage').text(
                          'Added to new Virtual Cohort');
                      })
                      .fail(function() {
                        tooltip.find('.savedMessage').html(
                          '<i class="fa fa-exclamation-triangle"></i>' +
                          'Failed to save virtual cohort, ' +
                          'please try again later.');
                      })
                      .always(function() {
                        tooltip.find('.close-dialog')
                          .css('display', 'inline-block');
                        tooltip.find('.saved').css('display', 'block');
                        tooltip.find('.saving').css('display', 'none');
                        tooltip.find('.dialog').css('display', 'none');
                        tooltip.find('.cohort-name').val('');
                        tooltip.find('.cohort-description').val('');
                        tooltip.find('.save-cohort')
                          .attr('disabled', true);
                        api.reposition();
                      });
                  });
                }
              });
              tooltip.find('.query').click(function() {
                if(_.isObject(self_.savedVC) && self_.savedVC.id) {
                  window.open(window.cbioURL + 'study?cohorts=' + self_.savedVC.id);
                }
              });
              tooltip.find('.close-dialog i').click(function() {
                api.hide();
              });
              tooltip.find('.cohort-name')
                .keyup(function() {
                  if (tooltip.find('.cohort-name').val() === '') {
                    tooltip.find('.save-cohort')
                      .attr('disabled', true);
                  } else {
                    tooltip.find('.save-cohort')
                      .attr('disabled', false);
                  }
                });
            },
            show: function() {
              var tooltip = $('.iviz-save-cohort-btn-qtip .qtip-content');
              self_.updateStats = true;
              self_.$nextTick(function() {
                // If user hasn't specific description only.
                if (!tooltip.find('.cohort-description').val()) {
                  $.when(vcSession.utils.generateCohortDescription(self_.stats.selectedCases))
                    .then(function(_desp) {
                      // If user hasn't specific description only.
                      if (!tooltip.find('.cohort-description').val()) {
                        tooltip.find('.cohort-description').val(_desp);
                      }
                    });
                }
              });
              tooltip.find('.close-dialog').css('display', 'inline-block');
              tooltip.find('.dialog').css('display', 'block');
              tooltip.find('.saving').css('display', 'none');
              tooltip.find('.saved').css('display', 'none');
            }
          },
          content: '<div><div class="close-dialog">' +
          '<i class="fa fa-times-circle-o"></i></div>' +
          '<div class="dialog"><div class="input-group">' +
          '<input type="text" class="form-control cohort-name" ' +
          'placeholder="New Cohort Name"> <span class="input-group-btn">' +
          '<button class="btn btn-default save-cohort" ' +
          'type="button" disabled>Save</button></span>' +
          '</div><div>' +
          '<textarea class="form-control cohort-description" rows="5" ' +
          'placeholder="New Cohort Description (Optional)"></textarea>' +
          '</div></div>' +
          '<div class="saving" style="display: none;">' +
          '<i class="fa fa-spinner fa-spin"></i> Saving virtual cohort</div>' +
          '<div class="saved" style="display: none;">' +
          '<span class="savedMessage"></span>' +
          '<button class="btn btn-default btn-sm query"' +
          '>Query Virtual Cohort</button></div>' +
          '</div>'
        });
      }
    }
  });
})(window.Vue,
  window.$ || window.jQuery, window.vcSession);