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
      saveCohort: function(stats, name, description, addToUserStudies) {
        var def = new $.Deferred();
        $.when(vcSession.utils.buildVCObject(stats.filters, stats.studies,
          name, description)).done(function(_virtualCohort) {
          vcSession.model.saveSession(_virtualCohort, addToUserStudies)
            .done(function(response) {
              def.resolve(response);
            })
            .fail(function() {
              def.reject();
            });
        });
        return def.promise();
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

    return {
      saveSession: function(virtualCohort, addToUserStudies) {
        var def = new $.Deferred();
        var url = addToUserStudies ? vcSession.URL+"/save" : vcSession.URL;
        $.ajax({
          type: 'POST',
          url: url,
          contentType: 'application/json;charset=UTF-8',
          data: JSON.stringify(virtualCohort)
        }).done(function(response) {
          if (virtualCohort.userID === 'DEFAULT') {
            virtualCohort.virtualCohortID = response.id;
          }
          def.resolve(response);
        }).fail(function() {
          def.reject();
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

    var buildVCObject_ = function(filters, cases, name,
                                  description) {
      var def = new $.Deferred();
      var _virtualCohort = $.extend(true, {}, virtualCohort_);
      _virtualCohort.filters = filters;
      
      _virtualCohort.studies = cases.map(function(studyObj) {
        return {
          id: studyObj.id,
          samples: studyObj.samples
        };
      });
      if (name) {
        _virtualCohort.name = name;
      } else {
        _virtualCohort.name = cases.length > 1 ? "Combined Study" : "Selected Study";
      }
      _virtualCohort.description = description || '';
      def.resolve(_virtualCohort);
      return def.promise();
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
      generateCohortDescription: generateCohortDescription_
    };
  })();
})(window.vcSession,
  window._,
  window.$ || window.jQuery);
