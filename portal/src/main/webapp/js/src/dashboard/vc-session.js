'use strict';

window.vcSession = window.vcSession ? window.vcSession : {};

(function(vcSession, _) {
  if (!_.isObject(vcSession)) {
    vcSession = {};
  }
  vcSession.events = (function() {
    return {
      saveCohort: function(stats, selectedPatientsNum, selectedSamplesNum,
                           userID, name, description) {
        var _virtualCohort = vcSession.utils.buildVCObject(stats.filters,
          selectedPatientsNum, selectedSamplesNum, stats.selected_cases,
          userID,
          name, description);
        vcSession.model.saveSession(_virtualCohort);
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
           TODO : if virtual study is not present in local storage
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
            _studyMatch.samplesLength += 1;
            _returnString = 'success';
          } else if (_.contains(_match.samples, sampleID)) {
            _returnString = 'warn';
          } else {
            _match.samples.push(sampleID);
            _studyMatch.samplesLength += 1;
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


/**
 * Created by Karthik Kalletla on 3/21/16.
 */
'use strict';
window.vcSession = window.vcSession ? window.vcSession : {};

(function(vcSession, _, $) {
  if(!_.isObject(vcSession)) {
    vcSession = {};
  }
  vcSession.utils = (function() {
    var virtualCohort_ = {
      studyName: 'My virtual study',
      description: 'My virtual study - Description',
      userID: 'DEFAULT',
      created: '',
      filters: '',
      samplesLength: '',
      patientsLength: '',
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

    var buildVCObject_ = function(filters, patientsLength,
                                  samplesLength, cases, userID, name,
                                  description) {
      var _virtualCohort = $.extend(true, {}, virtualCohort_);
      _virtualCohort.filters = filters;
      _virtualCohort.selectedCases = cases;
      _virtualCohort.samplesLength = samplesLength;
      _virtualCohort.patientsLength = patientsLength;
      _virtualCohort.created = new Date().getTime();
      if (name) {
        _virtualCohort.studyName = name;
      }
      if (description) {
        _virtualCohort.description = description;
      }
      if (userID) {
        _virtualCohort.userID = userID;
      }
      return _virtualCohort;
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

    return {
      buildVCObject: buildVCObject_,
      setVirtualCohorts: setVirtualCohorts_,
      getVirtualCohorts: getVirtualCohorts_,
      generateUUID: generateUUID_,
      buildCaseListObject: buildCaseListObject_
    };
  })();
})(window.vcSession,
  window._,
  window.$ || window.jQuery);

/**
 * Created by Karthik Kalletla on 3/21/16.
 */

'use strict';
window.vcSession = window.vcSession ? window.vcSession : {};

(function(vcSession, _, $) {
  if(!_.isObject(vcSession)) {
    vcSession = {};
  }
  vcSession.model = (function() {
    var localStorageAdd_ = function(id, virtualCohort) {
      virtualCohort.virtualCohortID = id;
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
        var data = {
          virtualCohort: virtualCohort
        };
        $.ajax({
          type: 'POST',
          url:  vcSession.URL,
          contentType: 'application/json;charset=UTF-8',
          data: JSON.stringify(data)
        }).done(function(response) {
          if(virtualCohort.userID === 'DEFAULT')
          localStorageAdd_(response.id,
            virtualCohort);
        }).fail(function() {
          localStorageAdd_(vcSession.utils.generateUUID(),
            virtualCohort);
        });
      },
      removeSession: function(_virtualCohort) {
        $.ajax({
          type: 'DELETE',
          url:  vcSession.URL + _virtualCohort.virtualCohortID,
          contentType: 'application/json;charset=UTF-8'
        }).done(function() {
          if(_virtualCohort.userID === 'DEFAULT')
          localStorageDelete_(_virtualCohort);
        }).fail(function() {
          localStorageDelete_(_virtualCohort);
        });
      },
      editSession: function(_virtualCohort) {
        var data = {
          virtualCohort: _virtualCohort
        };
        $.ajax({
          type: 'PUT',
          url:  vcSession.URL + _virtualCohort.virtualCohortID,
          contentType: 'application/json;charset=UTF-8',
          data: JSON.stringify(data)
        }).done(function(response) {
          if(_virtualCohort.userID === 'DEFAULT')
          localStorageEdit_(data.virtualCohort);
        }).fail(function(jqXHR) {
          if (jqXHR.status === 404) {
            localStorageDelete_(_virtualCohort);
            vcSession.model.saveSession(_virtualCohort);
          } else {
            localStorageEdit_(_virtualCohort);
          }
        });
      },
      loadUserVirtualCohorts: function(userID) {
        var def = new $.Deferred();
        $.ajax({
          type: 'GET',
          url:  vcSession.URL + 'query/',
          contentType: 'application/json;charset=UTF-8',
          data: { field : 'data.virtualCohort.userID',
            value : userID}
        }).done(function(response) {
          var _virtualCohorts = [];
          $.each(response, function(key, val) {
            var _virtualCohort = val.data.virtualCohort;
            _virtualCohort.virtualCohortID = val.id;
            _virtualCohorts.push(_virtualCohort);
          });
          def.resolve(_virtualCohorts);
          //vcSession.utils.setVirtualCohorts(_virtualCohorts);
        }).fail(function() {
          console.log('unable to get user virtual cohorts');
          def.reject();
        });
        return def.promise();
      },

      getVirtualCohortDetails: function(virtualCohortID) {
       /* $.getJSON( vcSession.URL + virtualCohortID, function(response) {
          iViz.applyVC(_.omit(response.data.virtualCohort,
            ['created', 'userID', 'virtualCohortID']));
          jQuery.notify('Imported Virtual Cohort', 'success');
        }).fail(function() {
          var virtualCohort_ = _.findWhere(vcSession.utils.getVirtualCohorts(), {
            virtualCohortID: virtualCohortID
          });
          if (virtualCohort_ !== undefined) {
            iViz.applyVC(_.omit(virtualCohort_,
              ['created', 'userID', 'virtualCohortID']));
            jQuery.notify('Imported Virtual Cohort', 'success');
          } else {
            jQuery.notify('Error While importing Virtual Cohort', 'error');
          }
        });*/
      }
    };
  })();
})(window.vcSession,
  window._,
  window.$ || window.jQuery);


/**
 * Created by Karthik Kalletla on 3/16/16.
 */

'use strict';
(function(Vue) {
  Vue.component('editableField', {
    // template: '#editable-field',
    props: ['name', 'edit', 'type'],
    template: '<div v-if="edit"><div v-if="type==\'text\'"><input' +
    ' type="text" v-model="name" placeholder="My Virtual Study"/></div><div' +
    ' v-if="type==\'textarea\'"><textarea rows="4" cols="50"' +
    ' v-model="name"></textarea></div></div><div v-else="edit"><span>{{ name' +
    ' }}</span></div>'
  });
})(window.Vue);


/**
 * Created by Karthik Kalletla on 3/16/16.
 */

'use strict';
(function(Vue, vcSession, $, Clipboard) {
  var clipboard = null;
  $(document).on('mouseleave', '.btn-share', function(e) {
    $(e.currentTarget).removeClass('tooltipped tooltipped-s');
    $(e.currentTarget).removeAttr('aria-label');
  });
  Vue.component('editableRow', {
    template: '<tr><td class="text center" ><editable-field' +
    ' :name.sync="data.studyName" :edit="edit" type="text"/></td><td' +
    ' class="text center" ><editable-field :name.sync="data.description"' +
    ' :edit="edit" type="textarea"/></td><td class="text center"' +
    ' ><span>{{data.patientsLength}}</span></td><td class="text center"' +
    ' ><span>{{data.samplesLength}}</span></td><td><div class="buttons"' +
    ' :class="{view: !edit}"><button class="btn btn-info"' +
    ' @click="clickSave(data)"><em class="fa fa-save"></em></button><button' +
    ' class="btn btn-default" @click="clickCancel()"><em class="fa' +
    ' fa-times"></em></button></div><div class="buttons" :class="{view:' +
    ' !share}"><div class="input-group"> ' +
    '<input type="text" id="link-to-share" class="form-control"' +
    'v-model="shortenedLink" disabled><span class="input-group-btn">' +
    '<button class="btn btn-default btn-share" ' +
    ' data-clipboard-action="copy" data-clipboard-text={{shortenedLink}}><em' +
    ' class="fa fa-clipboard" alt="Copy to clipboard"></em></button><button' +
    ' class="btn btn-default" @click="clickCancel()"><em class="fa' +
    ' fa-times"></em></button></span></div></div><div class="buttons"' +
    ' :class="{view: edit||share}"><button class="btn btn-info"' +
    ' @click="clickEdit(data)"><em class="fa' +
    ' fa-pencil"></em></button><button class="btn btn-danger"' +
    ' @click="clickDelete(data)"><em class="fa' +
    ' fa-trash"></em></button><button class="btn btn-success"' +
    ' @click="clickShare(data)"><em class="fa' +
    ' fa-share-alt"></em></button><button class="btn btn-default"' +
    ' @click="clickImport(data)">Visualize</button></div></td></tr>',
    props: [
      'data', 'showmodal'
    ], created: function() {
      this.edit = false,
        this.share = false,
        this.shortenedLink = '---'
  },
    data: function() {
      return {
        edit: false,
        share: false,
        shortenedLink: '---'
      };
    },
    methods: {
      clickEdit: function(_virtualStudy) {
        this.backupName = _virtualStudy.studyName;
        this.backupDesc = _virtualStudy.description;
        this.edit = true;
      },
      clickCancel: function() {
        if (this.edit) {
          this.data.studyName = this.backupName;
          this.data.description = this.backupDesc;
          this.edit = false;
        } else if (this.share) {
          this.share = false;
        }
      },
      clickDelete: function(_virtualStudy) {
        if (_.isObject(vcSession)) {
          this.$dispatch('remove-cohort',_virtualStudy);
          vcSession.events.removeVirtualCohort(_virtualStudy);
        }
      },
      clickSave: function(_virtualStudy) {
        this.edit = false;
        if (_virtualStudy.studyName === '') {
          _virtualStudy.studyName = 'My virtual study';
        }
        if (_.isObject(vcSession)) {
          vcSession.events.editVirtualCohort(_virtualStudy);
        }
      },
      clickImport: function(_virtualStudy) {
        this.showmodal = false;
        //TODO: need to update functionality
        //iViz.applyVC(_virtualStudy);
      },
      clickShare: function(_virtualStudy) {
        // TODO: Create Bitly URL
        var completeURL = window.location.host + '/?vc_id=' +
          _virtualStudy.virtualCohortID;
        this.shortenedLink = completeURL;
        this.share = true;
        // Check if ClipBoard instance is present, If yes re-initialize the
        // instance.
        if (clipboard instanceof Clipboard) {
          clipboard.destroy();
        }
        initializeClipBoard();
      }
    }
  });
  /**
   * This method add tooltip when copy button is clicked
   * @param {Object} elem trigger object
   * @param {String} msg message to show
   */
  function showTooltip(elem, msg) {
    $(elem).addClass('tooltipped tooltipped-s');
    $(elem).attr('aria-label', msg);
  }

  /**
   * Initialize Clipboard instance
   */
  function initializeClipBoard(){
    var classname = document.getElementsByClassName('btn-share');
    clipboard = new Clipboard(classname);
    clipboard.on('success', function(e) {
      showTooltip(e.trigger, 'Copied');
    });
    clipboard.on('error', function(e) {
      showTooltip(e.trigger, 'Unable to copy');
    });
  }
})(window.Vue, window.vcSession,
  window.$ || window.jQuery, window.Clipboard);

/**
 * Created by Karthik Kalletla on 3/21/16.
 */
'use strict';
(function(Vue) {
  Vue.component('modaltemplate', {
    template: '<div class="modal-mask" v-show="show" transition="modal"' +
    ' @click="show = false"><div class="modal-dialog"' +
    ' v-bind:class="size" @click.stop><div class="modal-content"><div' +
    ' class="modal-header"><button type="button" class="close"' +
    ' @click="close"><span>x</span></button><slot' +
    ' name="header"></slot></div><div class="modal-body"><slot' +
    ' name="body"></slot></div><div slot="modal-footer"' +
    ' class="modal-footer"><slot name="footer"></slot></div></div></div></div>',
    props: [
      'show', 'size'
    ],
    methods: {
      close: function() {
        this.show = false;
      }
    },
    ready: function() {
      var _this = this;
      document.addEventListener('keydown', function(e) {
        if (_this.show && e.keyCode === 27) {
          _this.close();
        }
      });
    }
  });
})(window.Vue);


/**
 * Created by Karthik Kalletla on 3/21/16.
 */

'use strict';
(function(Vue, vcSession) {
  Vue.component('addVc', {

    template: '<modaltemplate :show.sync="addNewVc" size="modal-lg"><div' +
    ' slot="header"><h3 class="modal-title">Save Virtual' +
    ' Cohorts</h3></div><div slot="body"><div' +
    ' class="form-group"><label>Number of Samples' +
    ' :&nbsp;</label><span>{{selectedSamplesNum}}</span></div><br><div' +
    ' class="form-group"><label>Number of Patients' +
    ' :&nbsp;</label><span>{{selectedPatientsNum}}</span></div><br><div' +
    ' class="form-group"><label for="name">Name:</label><input type="text"' +
    ' class="form-control" v-model="name"  placeholder="My Virtual Cohort"' +
    ' value="My Virtual Cohort"></div><br><div class="form-group"><label' +
    ' for="description">Decription:</label><textarea class="form-control"' +
    ' rows="4" cols="50" v-model="description"></textarea></div></div><div' +
    ' slot="footer"><button type="button" class="btn btn-default"' +
    ' @click="addNewVc = false">Cancel</button><button type="button"' +
    ' class="btn' +
    ' btn-default"@click="saveCohort()">Save</button></div></modaltemplate>',
    props: [ 'selectedSamplesNum',
      'selectedPatientsNum',
      'userid',
      'stats','addNewVc','updateStats'],
    data: function() {
      return{
        name:'My Virtual Cohort',
        description:''
      }
    },
    watch: {
      addNewVc: function() {
        this.name = 'My Virtual Cohort';
        this.description = '';
      }
    },
    methods: {
      saveCohort: function() {
        if (_.isObject(vcSession)) {
          var self_ = this;
          self_.updateStats = true;
          self_.$nextTick(function(){
            vcSession.events.saveCohort(self_.stats,
              self_.selectedPatientsNum, self_.selectedSamplesNum, self_.userid, self_.name,
              self_.description || '');
            self_.addNewVc = false;
            jQuery.notify('Added to new Virtual Study', 'success');
          })
        }
      }
    }
  });
})(window.Vue, window.vcSession);

/**
 * Created by kalletlak on 7/19/16.
 */
'use strict';
(function (Vue, $, vcSession) {
  Vue.component('sessionComponent', {
    template: '<div id="cohort-component"><button v-if="showSaveButton" type="button" class="btn btn-default" ' +
    '@click="addNewVC = true" id="save_cohort_btn">Save Cohort </button> <button v-if="showManageButton" type="button" ' +
    'class="btn btn-default" @click="manageCohorts()"> <i class="fa fa-bars"></i> </button> ' +
    '<add-vc :add-new-vc.sync="addNewVC" :selected-samples-num="selectedSamplesNum" ' +
    ':selected-patients-num="selectedPatientsNum" :userid="userid" :stats="stats" :update-stats.sync="updateStats"></add-vc> ' +
    '<modaltemplate :show.sync="showVCList" size="modal-xlg"> <div slot="header"> ' +
    '<h4 class="modal-title">Virtual Cohorts</h4> </div> <div slot="body"> ' +
    '<table class="table table-bordered table-hover table-condensed"> ' +
    '<thead> <tr style="font-weight: bold"> <td style="width:20%">Name</td>' +
    ' <td style="width:40%">Description</td> <td style="width:10%">Patients</td> ' +
    '<td style="width:10%">Samples</td> <td style="width:20%">Operations</td> </tr> ' +
    '</thead> <tr is="editable-row" :data="virtualCohort" :showmodal.sync="showVCList" ' +
    'v-for="virtualCohort in virtualCohorts"> </tr> </table> </div> <div slot="footer"> ' +
    '</div> </modaltemplate> </div> </nav> </div>',
    props: [
      'selectedPatientsNum', 'selectedSamplesNum', 'userid', 'showSaveButton', 'showManageButton', 'stats', 'updateStats'
    ],
    data: function () {
      return {
        showVCList: false,
        addNewVC: false,
        virtualCohorts: []
      }
    }, events: {
      'remove-cohort': function (cohort) {
        this.virtualCohorts.$remove(cohort);
      }
    }, methods: {
      manageCohorts: function () {
        this.showVCList = true;
        if (this.userid !== undefined && this.userid !== 'DEFAULT') {
          this.virtualCohorts = vcSession.model.loadUserVirtualCohorts();
        } else {
          this.virtualCohorts = vcSession.utils.getVirtualCohorts();
        }
      }
    }
  });
})(window.Vue,
  window.$ || window.jQuery, window.vcSession);
