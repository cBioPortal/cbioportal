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
        $.when(vcSession.utils.buildVCObject(stats,
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
        var url = window.cbioURL+'api-legacy/proxy/session/virtual_study'+ (addToUserStudies ? '/save' : '');
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
      studies: '',
      origin: ''
    };

    var buildVCObject_ = function(stats, name,
                                  description) {
      var def = new $.Deferred();
      var _virtualCohort = $.extend(true, {}, virtualCohort_);
      _virtualCohort.filters = stats.filters;

      _virtualCohort.studies = stats.studies.map(function(studyObj) {
        return {
          id: studyObj.id,
          samples: studyObj.samples
        };
      });
      _virtualCohort.origin = stats.origin;
      if (name) {
        _virtualCohort.name = name;
      } else {
        _virtualCohort.name = getVSDefaultName();
      }
      _virtualCohort.description = description || '';
      def.resolve(_virtualCohort);
      return def.promise();
    };

    var getNumOfSelectedSamplesFromStudyMap = function(studyMap) {
      var _numOfSamples = {
        sampleCounts__: 0,
        studies: {}
      };
      _.each(studyMap, function(_study) {
        _numOfSamples.studies[_study.id] = _study.samples.length;
        _numOfSamples.sampleCounts__ += _study.samples.length;
      });
      return _numOfSamples;
    };

    var generateVSDescription_ = function(_studies, _filters) {
      var _desp = '';
      if (_studies.studies.length > 0) {
        _desp = _studies.count + (_studies.count > 1 ? ' samples ' : ' sample ')
          + 'from ' + _studies.studies.length +
          (_studies.studies.length > 1 ? ' studies' : ' study') + ':';

        _.each(_studies.studies, function(_study) {
          _desp += '\n- ' + _study.name + ' ('
            + _study.count + ' sample' + (_study.count > 1 ? 's' : '') + ')';
        });

        if (_filters.length > 0) {
          _desp += '\n\nFilter' + (_filters.length > 1 ? 's' : '') + ':';
          _filters.sort(function(a, b) {
            return a.attrName.localeCompare(b.attrName);
          });
          _.each(_filters, function(_filter) {
            _desp += '\n- ' + _filter.attrName + ': ';
            if (_filter.viewType === 'bar_chart') {
              _desp += iViz.util.getDisplayBarChartBreadCrumb(_filter.filter);
            } else if (_filter.viewType === 'table'
              && ['mutated_genes', 'cna_details'].indexOf(_filter.attrId) !== -1) {
              _.each(_filter.filter, function(subSelection) {
                _desp += '\n  - ' + subSelection;
              });
            } else if (_filter.viewType === 'scatter_plot' || _filter.viewType === 'custom') {
              _desp += _filter.filter.length + ' sample'
                + (_filter.filter.length > 1 ? 's' : '');
            } else {
              _desp += _filter.filter.join(', ');
            }
          });
        }

        _desp += '\n\nCreated on  ' + getCurrentDate();

        if (window.userEmailAddress && window.userEmailAddress !== 'anonymousUser') {
          _desp += ' by ' + window.userEmailAddress;
        }
      }
      return _desp;
    };

    var getCurrentDate = function() {
      var _date = new Date();
      var strArr = [_date.getFullYear(), _date.getMonth() + 1, _date.getDate()];
      return strArr.join('-');
    };

    var getVSDefaultName = function(studyMap) {
      var _numOfSamples = getNumOfSelectedSamplesFromStudyMap(studyMap);
      return 'Selected ' + (_numOfSamples.sampleCounts__ > 1 ? 'samples' : 'sample')
        + ' (' + getCurrentDate() + ')';
    };

    return {
      buildVCObject: buildVCObject_,
      VSDefaultName: getVSDefaultName,
      generateVSDescription: generateVSDescription_
    };
  })();
})(window.vcSession,
  window._,
  window.$ || window.jQuery);

// http://bootstrap-notify.remabledesigns.com/
function Notification() {

    // default settings
    var settings = {
        message_type: "success", //success, warning, danger, info
        allow_dismiss: false,
        newest_on_top: false,
        placement_from: "top",
        placement_align: "right",
        spacing: 10,
        delay: 5000,
        timer: 1000,
        custom_class:"geneAddedNotification"
    };

    // create a notification
    this.createNotification = function(notificationMessage, options) {
        //if the options isn’t null extend defaults with user options.
        if (options) $.extend(settings, options);

        // create the notification
        $.notify({
            message: notificationMessage,
        }, {
            // settings
            element: 'body',
            type: settings.message_type,
            allow_dismiss: settings.allow_dismiss,
            newest_on_top: settings.newest_on_top,
            showProgressbar: false,
            placement: {
                from: settings.placement_from,
                align: settings.placement_align
            },
            spacing: settings.spacing,
            z_index: 1031,
            delay: settings.delay,
            timer: settings.timer,
            animate: {
                enter: 'animated fadeInDown',
                exit: 'animated fadeOutUp'
            },
            template: '<div data-notify="container" class="col-xs-11 col-sm-3 alert alert-{0} '+settings.custom_class+'" role="alert">' +
            '<button type="button" style="display: none" aria-hidden="true" class="close" data-notify="dismiss" >×</button>' +
            '<span data-notify="icon"></span> ' +
            '<span data-notify="title">{1}</span> ' +
            '<span data-notify="message">{2}</span>' +
            '<div class="progress" data-notify="progressbar">' +
            '<div class="progress-bar progress-bar-{0}" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100" style="width: 0%;"></div>' +
            '</div>' +
            '<a href="{3}" target="{4}" data-notify="url"></a>' +
            '</div>'
        });


    }
}

// based on gene-symbol-validator.js
//function GeneValidator(geneAreaId, emptyAreaMessage, updateGeneCallback){
function GeneValidator(geneAreaId, geneModel){
    var self = this;
    var nrOfNotifications=0;

    var showNotification=true;

    this.init = function(){
        console.log(new Date() + " init called for "+geneAreaId);
        // create a debounced validator
        var debouncedValidation = _.debounce(this.validateGenes, 1000);
        $(geneAreaId).bind('input propertychange', debouncedValidation);
    }

    this.validateGenes = function(callback, show){
        console.log(new Date() + " validating genes in "+geneAreaId);

        // store whether to show notifications
        showNotification=(show===undefined)?true:show;

        // clear all existing notifications
        if(showNotification) clearAllNotifications();

        // clean the textArea string, removing doubles and non-word characters (except -)
        var genesStr = geneModel.getCleanGeneString(",");

        var genes = [];
        var allValid = true;

        $.post(window.cbioURL + 'CheckGeneSymbol.json', { 'genes': genesStr })
            .done(function(symbolResults) {
                // If the number of genes is more than 100, show an error
                if(symbolResults.length > 100) {
                    addNotification("<b>You have entered more than 100 genes.</b><br>Please enter fewer genes for better performance", "danger");
                    allValid=false;
                    $("#iviz-header-left-1").attr("disabled", true);
                }

                // handle each symbol found
                for(var j=0; j < symbolResults.length; j++) {
                    var valid = handleSymbol(symbolResults[j])
                    if(!valid) {
                        allValid = false;
                        $("#iviz-header-left-1").attr("disabled", true);
                    }
                }
                if (allValid) {
                  $("#iviz-header-left-1").attr("disabled", false);
                }
            })
            .fail(function(xhr,  textStatus, errorThrown){
                addNotification("There was a problem: "+errorThrown, "danger");
                allValid=false;
            })
            .always(function(){
                // if not all valid, focus on the gene array for focusin trigger
                if(!allValid) {
                  $(geneAreaId).focus();
                  $("#iviz-header-left-1").attr("disabled", true);
                }
                // in case a submit was pressed, use the callback
                if($.isFunction(callback)) callback(allValid);
            });
    }

    // return whether there are any active notifications
    this.noActiveNotifications = function(){
        return nrOfNotifications===0;
    }

    this.replaceAreaValue = function(geneName, newValue){
        var regexp = new RegExp("\\b"+geneName+"\\b","g");
        var genesStr = geneModel.getCleanGeneString();
        geneModel.set("geneString", genesStr.replace(regexp, newValue).trim());
    }

    // create a notification of a certain type
    function addNotification(message, message_type){
        notificationSettings.message_type = message_type;
        new Notification().createNotification(message, notificationSettings);
        nrOfNotifications = $(".alert").length;
    }

    function clearAllNotifications(){
        // select the notifications of interest
        // kill their animations to prevent them from blocking space, destroy any qtips remaining and call click to
        // make the notifications disappear
        $(".geneValidationNotification").css("animation-iteration-count", "0");
        $(".geneValidationNotification").qtip("destroy");
        $(".geneValidationNotification").find("button").click();
        nrOfNotifications=0;
    }

    // handle one symbol
    function handleSymbol(aResult){
        var valid = false;

        // 1 symbol
        if(aResult.symbols.length == 1) {
            if(aResult.symbols[0].toUpperCase() != aResult.name.toUpperCase() && showNotification)
                handleSynonyms(aResult);
            else
                valid=true;
        }
        else if(aResult.symbols.length > 1 && showNotification)
            handleMultiple(aResult)
        else if(showNotification)
            handleSymbolNotFound(aResult);

        return valid;
    }

    // case where we're dealing with an ambiguous gene symbol
    function handleMultiple(aResult){
        var gene = aResult.name;
        var symbols = aResult.symbols;

        var tipText = "Ambiguous gene symbol. Click on one of the alternatives to replace it.";
        var notificationHTML="<span>Ambiguous gene symbol - "+gene+" ";

        // create the dropdown
        var nameSelect = $("<select id="+gene+">").addClass("geneSelectBox").attr("name", gene);
        $("<option>").attr("value", "").html("select a symbol").appendTo(nameSelect);
        for(var k=0; k < symbols.length; k++) {
            var aSymbol = symbols[k];
            // add class and data-notify to allow us to dismiss the notification
            var anOption = $("<option class='close' data-notify='dismiss'>").attr("value", aSymbol).html(aSymbol);
            anOption.appendTo(nameSelect);
        }

        notificationHTML+=nameSelect.prop('outerHTML')+"</span>";
        addNotification(notificationHTML, "warning");

        // when the dropdown is changed
        $("#"+gene).change(function() {
            nrOfNotifications--;
            // replace the value in the text area
            self.replaceAreaValue($(this).attr("name"), $(this).val());

            // destroy the qtip if it's still there
            $(this).qtip("destroy");

            // emulate a click on the selected child to dismiss the notification
            this.children[this.selectedIndex].click();
        });

        addQtip(gene, tipText);
    }


    // case when the symbol has synonyms
    function handleSynonyms(aResult){
        var gene = aResult.name;
        var trueSymbol = aResult.symbols[0];
        var tipText = "'" + gene + "' is a synonym for '" + trueSymbol + "'. "
            + "Click here to replace it with the official symbol.";

        var notificationHTML=$("<span>Symbol synonym found - "+gene + ":" + trueSymbol+"</span>");
        notificationHTML.attr({
                'id': gene,
                'symbol': trueSymbol,
                'class':'close',
                'data-notify':'dismiss'
            });

        addNotification(notificationHTML.prop('outerHTML'), "warning");

        // add click event to our span
        // due to the class and data-notify, the click also removes the notification
        $("#"+gene).click(function(){
            nrOfNotifications--;
            // replace the value in the text area
            self.replaceAreaValue($(this).attr("id"), $(this).attr("symbol"));

            // destroy the qtip if it's still here
            $(this).qtip("destroy");
        });

        addQtip(gene, tipText);
    }

    // case when the symbol was not found
    function handleSymbolNotFound(aResult){
        var gene = aResult.name;
        var tipText = "Could not find gene symbol "+gene+". Click to remove it from the gene list.";

        var notificationHTML=$("<span>Symbol not found - "+gene+"</span>");
        notificationHTML.attr({
            'id': gene,
            'class':'close',
            'data-notify':'dismiss'
        });

        addNotification(notificationHTML.prop('outerHTML'), "warning");

        // add click event to our span
        // due to the class and data-notify, the click also removes the notification
        $("#"+gene).click(function(){
            nrOfNotifications--;
            // replace the value in the text area
            self.replaceAreaValue($(this).attr("id"), "");

            // destroy the qtip if it's still here
            $(this).qtip("destroy");
        });

        addQtip(gene, tipText);
    }

    // add a qtip to some identifier
    function addQtip(id, tipText){
        $("#"+id).qtip({
            content: {text: tipText},
            position: {my: 'top center', at: 'bottom center', viewport: $(window)},
            style: {classes: 'qtip-light qtip-rounded qtip-shadow'},
            show: {event: "mouseover"},
            hide: {fixed: true, delay: 100, event: "mouseout"}
        });
    }


    // notification settings
    var notificationSettings = {
        message_type: "warning",
        custom_class: "geneValidationNotification",
        allow_dismiss: true,
        spacing: 10,
        delay: 0,
        timer: 0
    };

    // when new object is created, called init();
    this.init();
}
window.QueryByGeneUtil = (function() {
  // add the field
  function submitForm(url, fields) {
    var formOpts = {
      action: url,
      method: 'post',
      // Could not figure out why Sarafi won't allow to open a new tab for query page
      target: !cbio.util.browser.safari ? '_blank' : ''
    };
    var $form = $('<form>', formOpts);

    $.each(fields, function(key, val) {
      $('<input>').attr({
        type: 'hidden',
        name: key,
        value: val
      }).appendTo($form);
    });

    // Firefox requires form to be attached to document body.
    $form.appendTo(document.body);

    $form.submit();
  }

  return {
    /*
    * Input parameter
    * cohortIds : list of input queried id
    * stats : object containing study statistics(filters, selected study samples)
    * geneIds : selected genes
    * includeCases : indicates whether to include custom samples in the form.
    *                this is true for shared virtual study
    */
    query: function(cohortIds, stats, geneIds, includeCases) {
      var formOps = {
        cancer_study_list: cohortIds,
        cancer_study_id:'all'
      }

      if(geneIds !== undefined && geneIds !== ''){
        formOps['tab_index'] = 'tab_visualize';
        formOps['Action'] = 'Submit';
        formOps['data_priority'] = 0;
        formOps['gene_list'] = encodeURIComponent(geneIds);

        var physicalStudies = _.pluck(stats.studies,'id')

        if(cohortIds.length === 1 && physicalStudies.length === 1 && physicalStudies[0] === cohortIds[0]){
          //TODO: what if window.mutationProfileId is null
          formOps['genetic_profile_ids_PROFILE_MUTATION_EXTENDED'] = window.mutationProfileId;
          //TODO: what if window.cnaProfileId is null
          formOps['genetic_profile_ids_PROFILE_COPY_NUMBER_ALTERATION'] = window.cnaProfileId;
          formOps['case_set_id'] = cohortIds[0]+'_all'
        } else {
          formOps['case_set_id'] = 'all'
        }
      }

      //check if there are filters
      if ((JSON.stringify(stats.filters) !== JSON.stringify({patients:{},samples:{}})) || includeCases) {
        var studySamples = [];
        _.each(stats.studies, function(study) {
          _.each(study.samples, function(sampleId) {
            studySamples.push(study.id + ":" + sampleId);
          });
        });
        formOps['case_set_id'] = -1;
        formOps['case_ids'] = studySamples.join('+');
      }
      if(includeCases){
        formOps['cancer_study_list'] = _.pluck(stats.studies,'id')
      }
      submitForm(window.cbioURL + 'index.do', formOps);
    }
  }
})();


var GenelistModel = Backbone.Model.extend({
    defaults: {
        geneString: ""
    },

    isEmptyModel: function(){
       return this.get("geneString").length==0;
    },

    getCleanGeneString: function(delim){
        delim = delim || " ";
        return this.getCleanGeneArray().join(delim);
    },
  
    getCleanGeneArray: function() {
      var _arr = this.removeEmptyElements(this.get("geneString")
        .toUpperCase().split(/[^a-zA-Z0-9-]/));
      _arr = _arr.filter(function(item, pos) {
        return _arr.indexOf(item) === pos;
      });
      return _arr;
    },

    removeEmptyElements: function (array){
        return array.filter(function(el){ return el !== "" });
    }
});

var QueryByGeneTextArea  = (function() {
    var geneModel = new GenelistModel();
    var areaId;
    var updateGeneCallBack;
    var geneValidator;
    var emptyAreaText = "query genes - click to expand";

    // when the textarea does not have focus, the text shown in the (smaller) textarea
    // is gene1, gene2 and x more
    function createFocusOutText(){
        var geneList = geneModel.getCleanGeneArray();
        var focusOutText = geneList[0];
        var stringLength = focusOutText.length;

        // build the string to be shown
        for(var i=1; i<geneList.length; i++){
            stringLength+=geneList[i].length+2;
            // if the string length is bigger than 15 characters add the "and x more"
            if(stringLength>15) {
                focusOutText+= " and "+(geneList.length-i)+" more";
                break;
            }
            focusOutText+=", "+geneList[i];
        }
        return focusOutText;
    }

    // set the textarea text when focus is lost (and no notifications are open)
    function setFocusOutText(){
        var focusOutText=emptyAreaText;
        // if there are genes build the focusText
        if(!geneModel.isEmptyModel()) focusOutText = createFocusOutText();
        setFocusoutColour();
        $(areaId).val(focusOutText);
    }

    // if the geneList is empty, we use a gray colour, otherwise black
    function setFocusoutColour(){
        if(!geneModel.isEmptyModel()) $(areaId).css("color", "black");
        else $(areaId).css("color", "darkgrey");
    }

    // when the textarea has focus, the contents is the geneList's contents separated by spaces
    function setFocusInText(){
        $(areaId).css("color", "black");
        $(areaId).val(geneModel.getCleanGeneString());
    }

    function isEmpty(){
        return geneModel.isEmptyModel();
    }

    function getGenes(){
        return geneModel.getCleanGeneString();
    }

    // addRemoveGene is used when someone clicks on a gene in a table (StudyViewInitTables)
    function addRemoveGene (gene){
        var geneList = geneModel.getCleanGeneArray();

        // if the gene is not yet in the list, add it and create a notification
        if(geneList.indexOf(gene)==-1) {
            geneList.push(gene);
            geneModel.set("geneString", geneModel.getCleanGeneString()+" "+gene);
            new Notification().createNotification(gene+" added to your query");
        }
        // if the gene is in the list, remove it and create a notification
        else{
            var index = geneList.indexOf(gene);
            geneList.splice(index, 1);
            geneModel.set("geneString", geneList.join(" "));
            new Notification().createNotification(gene+" removed from your query");
        }
        // if there are active notifications, the textarea is still expanded and the contents
        // should reflect this
        if(geneValidator.noActiveNotifications()) setFocusOutText();
        else setFocusInText();

        // update the highlighting in the tables
        //if(updateGeneCallBack != undefined) updateGeneCallBack(geneList);
    }

    // used by the focusOut event and by the updateTextArea
    function setFocusOut(){
        // if there are no active notifications and the textarea does not have focus
        if(geneValidator.noActiveNotifications() && !$(areaId).is(":focus")){
            // switch from focusIn to focusOut and set the focus out text
            $(areaId).switchClass("expandFocusIn", "expandFocusOut", 500);
            setFocusOutText();
        }

        // update the gene tables for highlighting
        if(updateGeneCallBack != undefined) updateGeneCallBack(geneModel.getCleanGeneArray());
    }

    function validateGenes(callback){
        geneValidator.validateGenes(callback, false);
    }

    function updateTextArea(){
        // set display text - this will not fire the input propertychange
        $(areaId).val(geneModel.get("geneString"));
        setFocusOut();
    }

    function updateModel(){
        // check whether the model actually has to be updated
        if(geneModel.get("geneString")!=$(areaId).val()) {
            geneModel.set("geneString", $(areaId).val());
        }
    }

    // initialise events
    function initEvents(){
        // when user types in the textarea, update the model
        $(areaId).bind('input propertychange', function() {
          setTimeout(updateModel, 1000);
        });

        // when the model is changed, update the textarea
        geneModel.on("change", updateTextArea);

        // add the focusin event
        $(areaId).focusin(function () {
            $(this).switchClass("expandFocusOut", "expandFocusIn", 500);
            setFocusInText();
        });

        // add the focusout event
        $(areaId).focusout(function () {
            setFocusOut();
        });

        // create the gene validator
        geneValidator = new GeneValidator(areaId, geneModel);
    }


    function init(areaIdP, updateGeneCallBackP){
        areaId = areaIdP;
        updateGeneCallBack = updateGeneCallBackP;
        setFocusOutText();
        initEvents();
    }

    return{
        init: init,
        addRemoveGene: addRemoveGene,
        getGenes: getGenes,
        isEmpty: isEmpty,
        validateGenes: validateGenes
    }

})();


'use strict';
window.iViz = (function(_, $, cbio, QueryByGeneUtil, QueryByGeneTextArea) {
  var data_;
  var vm_;
  var tableData_ = [];
  var groupFiltersMap_ = {};
  var groupNdxMap_ = {};
  var charts = {};
  var includeCases= true;
  var configs_ = {
    styles: {
      vars: {
        width: {
          one: 195,
          two: 400
        },
        height: {
          one: 170,
          two: 350
        },
        chartHeader: 17,
        borderWidth: 2,
        scatter: {
          width: 398,
          height: 331
        },
        survival: {
          width: 398,
          height: 331
        },
        specialTables: {
          width: 398,
          height: 306
        },
        piechart: {
          width: 140,
          height: 140
        },
        barchart: {
          width: 398,
          height: 134
        }
      }
    }
  };
  var URLlenLimit = 1800;

  function getNavCaseIdsStr(selectedCasesMap, selectedCaseIds, underURLLimit) {
    var result = {
      str: '',
      limit: -1
    };
    var targetList = selectedCaseIds;
    if (Object.keys(selectedCasesMap).length > 1) {
      targetList = [];
      _.each(selectedCasesMap, function(patientIds, studyId) {
        _.each(patientIds, function(patientId, index) {
          targetList.push(studyId + ":" + patientId);
        });
      });
    }
    if (underURLLimit) {
      _.every(targetList, function(id, index) {
        if (index === 0) {
          result.str = id;
          return true;
        }
        var _str = result.str += ',' + id;
        if (_str.length > URLlenLimit) {
          result.limit = index;
          return false;
        } else {
          result.str = _str;
          return true;
        }
      })
    } else {
      result.str = targetList.join(',');
    }

    return result;
  }

  return {

    init: function(_rawDataJSON, configs,_selectableIds) {
      vm_ = iViz.vue.manage.getInstance();
      var selectableIdsSet = {}
      _.each(_selectableIds, function(id){
        selectableIdsSet[id] = true;
      });

      var cohortIds = window.cohortIdsList;
      for (var i = 0; i < cohortIds.length; i++) {
        if(selectableIdsSet[cohortIds[i]]){
          includeCases = false;
          break;
        }
      }

      data_ = _rawDataJSON;

      if (_.isObject(configs)) {
        configs_ = $.extend(true, configs_, configs);
      }

      var chartsCount = 0;
      var patientGroupAttrs = [];
      var sampleGroupAttrs = [];
      var groups = [];

      _.each(data_.groups.patient.attr_meta, function(attrData) {
        attrData.group_type = 'patient';
        charts[attrData.attr_id] = attrData;
        if (attrData.view_type === 'survival' && attrData.show) {
          vm_.numOfSurvivalPlots++;
        }
      });
      _.each(data_.groups.sample.attr_meta, function(attrData) {
        attrData.group_type = 'sample';
        charts[attrData.attr_id] = attrData;
      });

      _.each(iviz.datamanager.sortByNumOfStudies(
        data_.groups.patient.attr_meta.concat(data_.groups.sample.attr_meta))
        , function(attrData) {
          if (chartsCount < iViz.opts.numOfChartsLimit) {
            if (attrData.show) {
              if (attrData.group_type === 'patient') {
                patientGroupAttrs.push(attrData);
              } else {
                sampleGroupAttrs.push(attrData);
              }
              chartsCount++;
            }
          } else {
            attrData.show = false;
          }
        });
      groups.push({
        type: 'patient',
        id: vm_.groupCount.toString(),
        selectedcases: [],
        hasfilters: false,
        attributes: _.map(patientGroupAttrs, function(attr) {
          attr.group_id = vm_.groupCount.toString();
          return attr;
        })
      });
      vm_.groupCount += 1;

      groups.push({
        type: 'sample',
        id: vm_.groupCount.toString(),
        selectedcases: [],
        hasfilters: false,
        attributes: _.map(sampleGroupAttrs, function(attr) {
          attr.group_id = vm_.groupCount.toString();
          return attr;
        })
      });
      vm_.groupCount += 1;

      var _self = this;
      var requests = groups.map(function(group) {
        var _def = new $.Deferred();
        _self.createGroupNdx(group).then(function() {
          _def.resolve();
        }).fail(function() {
          _def.reject();
        });
        return _def.promise();
      });
      $.when.apply($, requests).then(function() {
        vm_.isloading = false;
        vm_.selectedsampleUIDs = _.pluck(data_.groups.sample.data, 'sample_uid');
        vm_.selectedpatientUIDs = _.pluck(data_.groups.patient.data, 'patient_uid');
        vm_.groups = groups;
        vm_.charts = charts;

        //Show unknown samples error message, whenthe initial(pie and bar) charts are loaded
        if (window.iviz.datamanager.unknownSamples.length > 0) {
          var str = ''
          window.iviz.datamanager.unknownSamples.forEach(function(obj){
            obj.samples.forEach(function(sample){
              str = str+'<br/>'+obj.studyId+':'+sample
            })
          })
          new Notification().createNotification('Following sample(s) might have been deleted/updated with the recent data updates<br/>'+str, {
            message_type: 'danger',
            delay:10000
          });
        }
      });
    }, // ---- close init function ----groups
    createGroupNdx: function(group) {
      var def = new $.Deferred();
      var _caseAttrId = group.type === 'patient' ? 'patient_uid' : 'sample_uid';
      if (_caseAttrId === 'sample_uid') {
        //add 'sample_id' to get mutation count and cna fraction for scatter plot
        var _attrIds = [_caseAttrId, 'sample_id', 'study_id'];
      } else {
        var _attrIds = [_caseAttrId, 'study_id'];
      }
      _attrIds = _attrIds.concat(_.pluck(group.attributes, 'attr_id'));
      $.when(iViz.getDataWithAttrs(group.type, _attrIds)).then(function(selectedData_) {
        groupNdxMap_[group.id] = {};
        groupNdxMap_[group.id].type = group.type;
        groupNdxMap_[group.id].data = selectedData_;
        groupNdxMap_[group.id].attributes = _attrIds;
        def.resolve();
      });
      return def.promise();
    },
    updateGroupNdx: function(groupId, attrId) {
      var def = new $.Deferred();
      var groupNdxData_ = groupNdxMap_[groupId];
      var attrIds = groupNdxData_.attributes;
      if (attrIds.indexOf(attrId) > -1) {
        def.resolve(false);
      } else {
        attrIds.push(attrId);
        $.when(iViz.getDataWithAttrs(groupNdxData_.type, attrIds)).then(function(selectedData_) {
          groupNdxData_.data = selectedData_;
          def.resolve(true);
        });
      }
      return def.promise();
    },
    getGroupNdx: function(groupId) {
      return groupNdxMap_[groupId].data;
    },
    setGroupFilteredCases: function(groupId_, type_, filters_) {
      groupFiltersMap_[groupId_] = {};
      groupFiltersMap_[groupId_].type = type_;
      groupFiltersMap_[groupId_].cases = filters_;
    },
    getGroupFilteredCases: function(groupId_) {
      if (groupId_ !== undefined) {
        return groupFiltersMap_[groupId_];
      }
      return groupFiltersMap_;
    },
    deleteGroupFilteredCases: function(groupId_) {
      groupFiltersMap_[groupId_] = undefined;
    },
    getDataWithAttrs: function(type, attrIds) {
      var def = new $.Deferred();
      var isPatientAttributes = (type === 'patient');
      var hasAttrDataMap = isPatientAttributes ? data_.groups.patient.has_attr_data : data_.groups.sample.has_attr_data;
      var attrDataToGet = [];
      var updatedAttrIds = [];

      _.each(attrIds, function(_attrId) {
        if (charts[_attrId] === undefined) {
          updatedAttrIds = updatedAttrIds.concat(_attrId);
        } else {
          updatedAttrIds = updatedAttrIds.concat(charts[_attrId].attrList);
        }
      });
      updatedAttrIds = iViz.util.unique(updatedAttrIds);
      _.each(updatedAttrIds, function(attrId) {
        if (hasAttrDataMap[attrId] === undefined) {
          attrDataToGet.push(attrId);
        }
      });
      var _def = new $.Deferred();
      $.when(_def).done(function() {
        var _data = isPatientAttributes ? data_.groups.patient.data : data_.groups.sample.data;
        var toReturn = [];
        _.each(_data, function(_caseData, _index) {
          toReturn[_index] = _.pick(_caseData, updatedAttrIds);
        });
        def.resolve(toReturn);
      });
      if (attrDataToGet.length > 0) {
        $.when(this.updateDataObject(type, attrDataToGet))
          .then(function() {
            _def.resolve();
          }, function() {
            _def.reject();
          });
      } else {
        _def.resolve();
      }
      return def.promise();
    },
    fetchCompleteData: function(_type, _processData) {
      var _def = new $.Deferred();
      var _attrIds = _.pluck(_.filter(charts, function(_chart) {
        return _chart.group_type === _type;
      }), 'attr_id');
      if (_processData) {
        $.when(iViz.getDataWithAttrs(_type, _attrIds))
          .then(function() {
            _def.resolve();
          }, function() {
            _def.reject();
          });
      } else {
        $.when(window.iviz.datamanager.getClinicalData(_attrIds, (_type === 'patient')))
          .then(function() {
            _def.resolve();
          }, function() {
            _def.reject();
          });
      }
      return _def.promise();
    },
    updateDataObject: function(type, attrIds) {
      var def = new $.Deferred();
      var self_ = this;
      var isPatientAttributes = (type === 'patient');
      var _data = isPatientAttributes ? data_.groups.patient.data : data_.groups.sample.data;
      var hasAttrDataMap = isPatientAttributes ?
        data_.groups.patient.has_attr_data : data_.groups.sample.has_attr_data;

      $.when(
        window.iviz.datamanager.getClinicalData(attrIds, isPatientAttributes))
        .then(function(clinicalData) {
          iViz.vue.manage.getInstance().increaseStudyViewSummaryPagePBStatus();
          var idType = isPatientAttributes ? 'patient_id' : 'sample_id';
          var type = isPatientAttributes ? 'patient' : 'sample';
          var attrsFromServer = {};
          _.each(clinicalData, function(_clinicalAttributeData, _attrId) {
            var selectedAttrMeta = charts[_attrId];

            hasAttrDataMap[_attrId] = '';
            selectedAttrMeta.keys = {};
            selectedAttrMeta.numOfDatum = 0;

            _.each(_clinicalAttributeData, function(_dataObj) {
              var caseIndex = self_.getCaseIndex(type, _dataObj.study_id, _dataObj[idType]);

              // Filter 'undefined' case index		
              if (caseIndex !== undefined) {
                _data[caseIndex] = _data[caseIndex] || {};
                _data[caseIndex][_dataObj.attr_id] = _dataObj.attr_val;
              }

              if (!selectedAttrMeta.keys
                  .hasOwnProperty(_dataObj.attr_val)) {
                selectedAttrMeta.keys[_dataObj.attr_val] = 0;
              }
              ++selectedAttrMeta.keys[_dataObj.attr_val];
              ++selectedAttrMeta.numOfDatum;
            });

            // Hide chart which only has no more than one category.
            var numOfKeys = Object.keys(selectedAttrMeta.keys).length;
            if (numOfKeys <= 1
              && ['CANCER_TYPE', 'CANCER_TYPE_DETAILED'].indexOf(_attrId) === -1) {
              selectedAttrMeta.show = false;
              attrIds = attrIds.filter(function(obj) {
                return obj !== _attrId;
              });
            } else {
              // If there is clinical data returned from server side.
              attrsFromServer[_attrId] = 1;
            }

            if (selectedAttrMeta.datatype === 'STRING' &&
              numOfKeys > iViz.opts.pie2TableLimit) {
              // Change pie chart to table if the number of categories
              // more then the pie2TableLimit configuration
              var uids = isPatientAttributes ?
                Object.keys(data_.groups.group_mapping.patient_to_sample) :
                Object.keys(data_.groups.group_mapping.sample_to_patient);

              selectedAttrMeta.view_type = 'table';
              selectedAttrMeta.layout = [1, 4];
              selectedAttrMeta.type = 'pieLabel';
              selectedAttrMeta.options = {
                allCases: uids,
                sequencedCases: uids
              };
            }
          });

          // Hide all attributes if no data available for selected cases.
          // Basically all NAs
          _.each(_.difference(attrIds, Object.keys(attrsFromServer)), function(_attrId) {
            var selectedAttrMeta = charts[_attrId];

            hasAttrDataMap[_attrId] = '';
            selectedAttrMeta.keys = {};
            selectedAttrMeta.numOfDatum = 0;
            selectedAttrMeta.show = false;
          });

          def.resolve();
        }, function() {
          def.reject();
        });
      return def.promise();
    },
    extractMutationData: function(_mutationData, _allSamples) {
      var _mutGeneMeta = {};
      var _allMutGenes = _.pluck(_mutationData, 'gene_symbol');
      var _mutGeneMetaIndex = 0;
      var self = this;
      _.each(_mutationData, function(_mutGeneDataObj) {
        var _uniqueId = _mutGeneDataObj.gene_symbol;
        _.each(_mutGeneDataObj.caseIds, function(_caseId) {
          var _caseUIdIndex = self.getCaseIndex('sample', _mutGeneDataObj.study_id, _caseId);
          if (_mutGeneMeta[_uniqueId] === undefined) {
            _mutGeneMeta[_uniqueId] = {};
            _mutGeneMeta[_uniqueId].gene = _uniqueId;
            _mutGeneMeta[_uniqueId].num_muts = 1;
            _mutGeneMeta[_uniqueId].case_ids = [_caseUIdIndex];
            _mutGeneMeta[_uniqueId].qval = (window.iviz.datamanager.getCancerStudyIds().length === 1 && _mutGeneDataObj.hasOwnProperty('qval')) ? _mutGeneDataObj.qval : null;
            _mutGeneMeta[_uniqueId].index = _mutGeneMetaIndex;
            if (data_.groups.sample.data[_caseUIdIndex].mutated_genes === undefined) {
              data_.groups.sample.data[_caseUIdIndex].mutated_genes = [_mutGeneMetaIndex];
            } else {
              data_.groups.sample.data[_caseUIdIndex].mutated_genes.push(_mutGeneMetaIndex);
            }
            _mutGeneMetaIndex += 1;
          } else {
            _mutGeneMeta[_uniqueId].num_muts += 1;
            _mutGeneMeta[_uniqueId].case_ids.push(_caseUIdIndex);
            if (data_.groups.sample.data[_caseUIdIndex].mutated_genes === undefined) {
              data_.groups.sample.data[_caseUIdIndex].mutated_genes = [_mutGeneMeta[_uniqueId].index];
            } else {
              data_.groups.sample.data[_caseUIdIndex].mutated_genes.push(_mutGeneMeta[_uniqueId].index);
            }
          }
        });
      });

      _.each(_mutGeneMeta, function(content) {
        content.case_uids = iViz.util.unique(content.case_ids);
      });

      tableData_.mutated_genes = {};
      tableData_.mutated_genes.geneMeta = _mutGeneMeta;
      tableData_.mutated_genes.allGenes = _allMutGenes;
      tableData_.mutated_genes.allSamples = [];

      _.each(_allSamples, function(samples, studyId) {
        _.each(samples, function(sampleId) {
          tableData_.mutated_genes.allSamples.push({
            "molecularProfileId": window.iviz.datamanager.getMutationProfileIdByStudyId(studyId),
            "sampleId": sampleId
          })
        })
      });
      return tableData_.mutated_genes;
    },
    extractCnaData: function(_cnaData, _allSamples) {
      var _cnaMeta = {};
      var _allCNAGenes = {};
      var _cnaMetaIndex = 0;
      var self = this;
      $.each(_cnaData, function(_studyId, _cnaDataPerStudy) {
        $.each(_cnaDataPerStudy.caseIds, function(_index, _caseIdsPerGene) {
          var _geneSymbol = _cnaDataPerStudy.gene[_index];
          var _altType = '';
          _allCNAGenes[_geneSymbol] = 1;
          switch (_cnaDataPerStudy.alter[_index]) {
          case -2:
            _altType = 'DEL';
            break;
          case 2:
            _altType = 'AMP';
            break;
          default:
            break;
          }
          var _uniqueId = _geneSymbol + '-' + _altType;
          _.each(_caseIdsPerGene, function(_caseId) {
            var _caseIdIndex = self.getCaseIndex('sample', _studyId, _caseId);
            if (_cnaMeta[_uniqueId] === undefined) {
              _cnaMeta[_uniqueId] = {};
              _cnaMeta[_uniqueId].gene = _geneSymbol;
              _cnaMeta[_uniqueId].cna = _altType;
              _cnaMeta[_uniqueId].cytoband = _cnaDataPerStudy.cytoband[_index];
              _cnaMeta[_uniqueId].case_ids = [_caseIdIndex];
              if ((window.iviz.datamanager.getCancerStudyIds().length !== 1) || _cnaDataPerStudy.gistic[_index] === null) {
                _cnaMeta[_uniqueId].qval = null;
              } else {
                _cnaMeta[_uniqueId].qval = _cnaDataPerStudy.gistic[_index][0];
              }
              _cnaMeta[_uniqueId].index = _cnaMetaIndex;
              if (data_.groups.sample.data[_caseIdIndex].cna_details === undefined) {
                data_.groups.sample.data[_caseIdIndex].cna_details = [_cnaMetaIndex];
              } else {
                data_.groups.sample.data[_caseIdIndex].cna_details.push(_cnaMetaIndex);
              }
              _cnaMetaIndex += 1;
            } else {
              _cnaMeta[_uniqueId].case_ids.push(_caseIdIndex);
              if (data_.groups.sample.data[_caseIdIndex].cna_details === undefined) {
                data_.groups.sample.data[_caseIdIndex].cna_details = [_cnaMeta[_uniqueId].index];
              } else {
                data_.groups.sample.data[_caseIdIndex].cna_details.push(_cnaMeta[_uniqueId].index);
              }
            }
          });
        });
      });

      _.each(_cnaMeta, function(content) {
        content.case_uids = iViz.util.unique(content.case_ids);
      });

      tableData_.cna_details = {};
      tableData_.cna_details.geneMeta = _cnaMeta;
      tableData_.cna_details.allGenes = Object.keys(_allCNAGenes);
      tableData_.cna_details.allSamples = [];

      _.each(_allSamples, function(samples, studyId) {
        _.each(samples, function(sampleId) {
          tableData_.cna_details.allSamples.push({
            "molecularProfileId": window.iviz.datamanager.getCNAProfileIdByStudyId(studyId),
            "sampleId": sampleId
          })
        })
      });
      return tableData_.cna_details;
    },
    getTableData: function(attrId, progressFunc) {
      var def = new $.Deferred();
      var self = this;
      if (tableData_[attrId] === undefined) {
        if (attrId === 'mutated_genes') {
          $.when(window.iviz.datamanager.getMutData(progressFunc))
            .then(function(_data) {
              def.resolve(self.extractMutationData(_data, window.iviz.datamanager.getAllMutatedGeneSamples()));
            }, function() {
              def.reject();
            });
        } else if (attrId === 'cna_details') {
          $.when(window.iviz.datamanager.getCnaData(progressFunc))
            .then(function(_data) {
              def.resolve(self.extractCnaData(_data, window.iviz.datamanager.getAllCNASamples()));
            }, function() {
              def.reject();
            });
        }
      } else {
        def.resolve(tableData_[attrId]);
      }
      return def.promise();
    },
    getScatterData: function(_self) {
      var def = new $.Deferred();
      var self = this;
      var data = {};

      $.when(window.iviz.datamanager.getSampleClinicalData(['MUTATION_COUNT', 'FRACTION_GENOME_ALTERED']))
        .then(function(_clinicalData) {
          var groupId = _self.attributes.group_id;
          data = self.getGroupNdx(groupId);
          def.resolve(data);
        }, function() {
          def.reject();
        });
      return def.promise();
    },
    getCasesMap: function(type) {
      if (type === 'sample') {
        return data_.groups.group_mapping.sample_to_patient;
      }
      return data_.groups.group_mapping.patient_to_sample;
    },
    getCaseUIDs: function(type) {
      return Object.keys(this.getCasesMap(type));
    },
    getCaseIndex: function(type, study_id, case_id) {
      if (!data_.groups.group_mapping.studyMap[study_id]) {
        return undefined;
      }
      if (type === 'sample') {
        return data_.groups.group_mapping.studyMap[study_id].sample_to_uid[case_id];
      }
      return data_.groups.group_mapping.studyMap[study_id].patient_to_uid[case_id];
    },
    getCaseUID: function(type, case_id) {
      return Object.keys(data_.groups.group_mapping.studyMap).reduce(function(a, b) {
        var _uid = data_.groups.group_mapping.studyMap[b][type + '_to_uid'][case_id];
        return (_uid === undefined) ? a : a.concat(_uid);
      }, []);
    },
    getCaseIdUsingUID: function(type, case_uid) {
      if (type === 'sample') {
        return data_.groups.sample.data[parseInt(case_uid, 10)].sample_id;
      }
      return data_.groups.patient.data[parseInt(case_uid, 10)].patient_id;
    },
    getPatientUIDs: function(sampleUID) {
      return this.getCasesMap('sample')[sampleUID];
    },
    getSampleUIDs: function(patientUID) {
      return this.getCasesMap('patient')[patientUID];
    },
    getPatientId: function(studyId, sampleId) {
      return data_.groups.group_mapping.studyMap[studyId].sample_to_patient[sampleId];
    },
    getSampleIds: function(studyId, patientId) {
      return data_.groups.group_mapping.studyMap[studyId].patient_to_sample[patientId];
    },
    getStudyCacseIdsUsingUIDs: function(type, uids) {
      var ids = [];
      _.each(uids, function(uid) {
        ids.push({
          studyId: data_.groups[type].data[uid].study_id,
          caseId: data_.groups[type].data[uid].sample_id
        });
      });
      return ids;
    },
    openCases: function() {
      var _selectedCasesMap = {};
      var _patientData = data_.groups.patient.data;
      $.each(vm_.selectedpatientUIDs, function(key, patientUID) {
        var _caseDataObj = _patientData[patientUID];
        if (!_selectedCasesMap[_caseDataObj.study_id]) {
          _selectedCasesMap[_caseDataObj.study_id] = [];
        }
        _selectedCasesMap[_caseDataObj.study_id].push(_caseDataObj.patient_id);
      });

      var _study_id = Object.keys(_selectedCasesMap)[0];
      var _selectedCaseIds = _selectedCasesMap[_study_id].sort();
      var _url = '';

      _url = window.cbioURL +
        'case.do#/patient?studyId=' +
        _study_id +
        '&caseId=' +
        _selectedCaseIds[0] +
        '#navCaseIds=' + getNavCaseIdsStr(_selectedCasesMap, _selectedCaseIds, false).str;

      // The IE URL limitation is 2083
      // https://blogs.msdn.microsoft.com/ieinternals/2014/08/13/url-length-limits/
      // But for safe, we decrease the limit to 1800
      if (_url.length > URLlenLimit) {
        var browser = cbio.util.browser;
        if (browser.msie || browser.edge) {
          var limit = getNavCaseIdsStr(_selectedCasesMap, _selectedCaseIds, true).limit;
          var limit = limit > 50 ?
            (Math.floor(limit / 50) * 50) : Math.floor(limit / 5) * 5;
          var browserName = 'Internet Explorer';
          if (browser.edge) {
            browserName = 'Microsoft Edge'
          }
          new Notification().createNotification(
            'Too many selected samples to browse due to URL length limit of' +
            ' ' + browserName + '. ' +
            ' Please select less than ' + limit + ' samples, or use another browser.',
            {message_type: 'danger'});
        } else {
          window.open(_url);
        }
      } else {
        window.open(_url);
      }
    },
    downloadCaseData: function() {
      var _def = new $.Deferred();
      var sampleUIds_ = vm_.selectedsampleUIDs;
      var attr = {};
      var self = this;

      $.when(this.fetchCompleteData('patient', true), this.fetchCompleteData('sample', true)).then(function() {
        attr.CANCER_TYPE_DETAILED = 'Cancer Type Detailed';
        attr.CANCER_TYPE = 'Cancer Type';
        attr.study_id = 'Study ID';
        attr.patient_id = 'Patient ID';
        attr.sample_id = 'Sample ID';

        var arr = [];
        var strA = [];

        var sampleAttr_ = data_.groups.sample.attr_meta;
        var patientAttr_ = data_.groups.patient.attr_meta;

        _.each(sampleAttr_, function(_attr) {
          if (attr[_attr.attr_id] === undefined &&
            _attr.view_type !== 'scatter_plot') {
            attr[_attr.attr_id] = _attr.display_name;
          }
        });

        _.each(patientAttr_, function(_attr) {
          if (attr[_attr.attr_id] === undefined &&
            _attr.view_type !== 'survival') {
            attr[_attr.attr_id] = _attr.display_name;
          }
        });

        _.each(attr, function(displayName) {
          strA.push(displayName || 'Unknown');
        });
        var content = strA.join('\t');
        strA.length = 0;
        _.each(sampleUIds_, function(sampleUId) {
          var temp = data_.groups.sample.data[sampleUId];
          var temp1 = $.extend({}, temp,
            data_.groups.patient.data[self.getPatientUIDs(sampleUId)[0]]);
          arr.push(temp1);
        });

        for (var i = 0; i < arr.length; i++) {
          strA.length = 0;
          strA = iViz.util.getAttrVal(attr, arr[i]);
          content += '\r\n' + strA.join('\t');
        }

        var downloadOpts = {
          filename: 'study_view_clinical_data.txt',
          contentType: 'text/plain;charset=utf-8',
          preProcess: false
        };

        cbio.download.initDownload(content, downloadOpts);
        _def.resolve();
      }, function() {
        // TODO: give warning/error message to user if the download is failed
        _def.resolve();
      });
      return _def.promise();
    },
    submitForm: function(cohortIdsList) {
      // Remove all hidden inputs
      $('#iviz-form input:not(:first)').remove();

      
          QueryByGeneUtil. query (cohortIdsList ? cohortIdsList: window.cohortIdsList, this.stat(),
          QueryByGeneTextArea.getGenes(), includeCases)
    },
    stat: function() {
      var _result = {};
      _result.origin = window.cohortIdsList;
      _result.filters = {};
      var self = this;

      // extract and reformat selected cases
      var _studies = [];
      var _selectedStudyCasesMap = {};
      var _sampleData = data_.groups.sample.data;

      $.each(vm_.selectedsampleUIDs, function(key, sampleUID) {
        var _caseDataObj = _sampleData[sampleUID];
        if (!_selectedStudyCasesMap[_caseDataObj.study_id]) {
          _selectedStudyCasesMap[_caseDataObj.study_id] = {};
          _selectedStudyCasesMap[_caseDataObj.study_id].id = _caseDataObj.study_id;
          _selectedStudyCasesMap[_caseDataObj.study_id].samples = [];
          _selectedStudyCasesMap[_caseDataObj.study_id].patients = {};
        }
        _selectedStudyCasesMap[_caseDataObj.study_id].samples.push(_caseDataObj.sample_id);
        var _patientId = self.getPatientId(_caseDataObj.study_id, _caseDataObj.sample_id);
        _selectedStudyCasesMap[_caseDataObj.study_id].patients[_patientId] = 1;
      });
      $.each(_selectedStudyCasesMap, function(key, val) {
        val.patients = Object.keys(val.patients);
        _studies.push(val);
      });
      _result.filters.patients = [];
      _result.filters.samples = [];
      _.each(vm_.groups, function(group) {
        var filters_ = [];
        var temp;
        var array;

        if (group.type === 'patient') {
          _.each(group.attributes, function(attributes) {
            if (attributes.filter.length > 0) {
              filters_[attributes.attr_id] = attributes.filter;
            }
          });
          temp = $.extend(true, _result.filters.patients, filters_);
          array = $.extend(true, {}, temp);
          _result.filters.patients = array;
        } else if (group.type === 'sample') {
          _.each(group.attributes, function(attributes) {
            if (attributes.filter.length > 0) {
              filters_[attributes.attr_id] = attributes.filter;
              if (attributes.attr_id === 'MUT_CNT_VS_CNA') {
                filters_[attributes.attr_id] =
                  _.map(self.getStudyCacseIdsUsingUIDs('sample', filters_[attributes.attr_id]), function(item) {
                    return item.studyId + ':' + item.caseId;
                  });
              }
            }
          });
          temp = $.extend(true, _result.filters.samples, filters_);
          array = $.extend(true, {}, temp);
          _result.filters.samples = array;
        }
      });

      if (vm_.customfilter.sampleUids.length > 0
        || vm_.customfilter.patientUids.length > 0) {
        var type = vm_.customfilter.type === 'sample' ? 'samples' : 'patients';
        var uidsType = type === 'samples' ? 'sampleUids' : 'patientUids';
        _result.filters[type][vm_.customfilter.id] =
          _.map(self.getStudyCacseIdsUsingUIDs(vm_.customfilter.type, vm_.customfilter[uidsType]), function(item) {
            return item.studyId + ':' + item.caseId;
          });
      }
      _result.studies = _studies;
      return _result;
    },

    vm: function() {
      return vm_;
    },
    view: {
      component: {}
    },
    util: {},
    opts: configs_,
    data: {},
    styles: configs_.styles,
    applyVC: function(_vc) {
      var _selectedSamples = [];
      var _selectedPatients = [];
      _.each(_.pluck(_vc.studies, 'samples'), function(_arr) {
        _selectedSamples = _selectedSamples.concat(_arr);
      });
      _.each(_.pluck(_vc.studies, 'patients'), function(_arr) {
        _selectedPatients = _selectedPatients.concat(_arr);
      });
      iViz.init(data_, _selectedSamples, _selectedPatients);
    }
  };
})(window._,
  window.$,
  window.cbio,
  window.QueryByGeneUtil,
  window.QueryByGeneTextArea);

'use strict';
(function(Vue, iViz, dc, _) {
  iViz.vue = {};

  iViz.vue.manage = (function() {
    var vmInstance_;

    return {
      init: function() {
        vmInstance_ = new Vue({
          el: '#complete-screen',
          data: {
            groups: [],
            selectedsampleUIDs: [],
            selectedpatientUIDs: [],
            selectedgenes: [],
            addNewVC: false,
            selectedPatientsNum: 0,
            selectedSamplesNum: 0,
            hasfilters: false,
            isloading: true,
            redrawgroups: [],
            customfilter: {
              id: 'selectById',
              display_name: 'Select by IDs',
              type: '',
              sampleUids: [],
              patientUids: []
            },
            charts: {},
            downloadingSelected: false,
            groupCount: 0,
            updateSpecialCharts: false,
            showSaveButton: false,
            showShareButton: false,
            loadUserSpecificCohorts: false,
            stats: {},
            updateStats: false,
            clearAll: false,
            showScreenLoad: false,
            showDropDown: false,
            numOfSurvivalPlots: 0,
            showedSurvivalPlot: false,
            userMovedChart: false,
            studyViewSummaryPagePBStatus: 0,
            failedToInit: {
              status: false,
              message: 'Failed to open the study.' + (iViz.opts.emailContact ? (' Please contact ' + iViz.opts.emailContact + '.') : '')
            },
          }, watch: {
            charts: function() {
              this.checkForDropDownCharts();
            },
            updateSpecialCharts: function() {
              var self_ = this;
              // TODO: need to update setting timeout
              var interval = setTimeout(function() {
                clearInterval(interval);
                var _attrs = [];
                _.each(self_.groups, function(group) {
                  _attrs = _attrs.concat(group.attributes);
                });
                self_.$broadcast('update-special-charts', self_.hasfilters);
              }, 500);
            },
            updateStats: function(newVal) {
              if (newVal) {
                this.stats = iViz.stat();
                this.updateStats = false;
              }
            },
            redrawgroups: function(newVal) {
              if (newVal.length > 0) {
                this.$broadcast('show-loader');
                _.each(newVal, function(groupid) {
                  dc.redrawAll(groupid);
                });
                this.redrawgroups = [];
                var self_ = this;
                this.$nextTick(function() {
                  self_.updateSpecialCharts = !self_.updateSpecialCharts;
                });
              }
            },
            selectedsampleUIDs: function(newVal, oldVal) {
              if (newVal.length !== oldVal.length) {
                this.selectedSamplesNum = newVal.length;
              }
            },
            selectedpatientUIDs: function(newVal, oldVal) {
              if (newVal.length !== oldVal.length) {
                this.selectedPatientsNum = newVal.length;
              }
            },
            isloading: function() {
              if (!this.isloading) {
                this.studyViewSummaryPagePBStatus = 1;
              }
            },
            numOfSurvivalPlots: function(newVal) {
              if (!newVal || newVal <= 0) {
                this.showedSurvivalPlot = false;
              } else {
                this.showedSurvivalPlot = true;
              }
            }
          }, events: {
            'manage-genes': function(geneList) {
              this.updateGeneList(geneList, false);
            }, 'set-selected-cases': function(selectionType, selectedCases) {
              this.setSelectedCases(selectionType, selectedCases);
            }, 'remove-chart': function(attrId, groupId) {
              this.removeChart(attrId, groupId);
            },
            'user-moved-chart': function() {
              this.userMovedChart = true;
            },
            'fail-during-init': function(message) {
              this.failedToInit.status = true;
              this.failedToInit.message = message;
            }
          }, methods: {
            increaseStudyViewSummaryPagePBStatus: function(text) {
              if (this.studyViewSummaryPagePBStatus < 0.6) {
                this.studyViewSummaryPagePBStatus += 0.2;
              } else if (this.studyViewSummaryPagePBStatus < 1) {
                this.studyViewSummaryPagePBStatus += (1 - this.studyViewSummaryPagePBStatus) / 4
              } else {
                this.studyViewSummaryPagePBStatus = 1;
              }
            },
            checkForDropDownCharts: function() {
              var showDropDown = false;
              _.each(this.charts, function(_chart) {
                if (!_chart.show) {
                  showDropDown = true;
                  return false;
                }
              });
              this.showDropDown = showDropDown;
            },
            openCases: function() {
              iViz.openCases();
            },
            downloadCaseData: function() {
              var _self = this;
              _self.downloadingSelected = true;
              iViz.downloadCaseData()
                .always(function() {
                  _self.downloadingSelected = false;
                });
            },
            submitForm: function() {
              iViz.submitForm();
            },
            clearAllCharts: function(includeNextTickFlag) {
              var self_ = this;
              self_.clearAll = true;
              self_.hasfilters = false;
              if (self_.customfilter.patientUids.length > 0 ||
                self_.customfilter.sampleUids.length > 0) {
                self_.customfilter.sampleUids = [];
                self_.customfilter.patientUids = [];
              }
              if (includeNextTickFlag) {
                self_.$nextTick(function() {
                  self_.selectedsampleUIDs = _.keys(iViz.getCasesMap('sample'));
                  self_.selectedpatientUIDs = _.keys(iViz.getCasesMap('patient'));
                  self_.$broadcast('update-special-charts', self_.hasfilters);
                  self_.clearAll = false;
                  _.each(this.groups, function(group) {
                    dc.redrawAll(group.id);
                  });
                });
              } else {
                self_.clearAll = false;
              }
            },
            addChart: function(attrId) {
              var self_ = this;
              var attrData = self_.charts[attrId];
              var _attrAdded = false;
              var _group = {};
              var _groupIdToPush = 0;
              self_.checkForDropDownCharts();
              _.every(self_.groups, function(group) {
                if (group.type === attrData.group_type) {
                  if (group.attributes.length < 30) {
                    attrData.group_id = group.id;
                    _groupIdToPush = group.id;
                    _attrAdded = true;
                    return false;
                  }
                  _group = group;
                  return true;
                }
                return true;
              });
              self_.showScreenLoad = true;
              self_.$nextTick(function() {
                if (_attrAdded) {
                  $.when(iViz.updateGroupNdx(attrData.group_id, attrData.attr_id)).then(function(isGroupNdxDataUpdated) {
                    attrData.addChartBy = 'user';
                    attrData.show = true;
                    self_.groups[_groupIdToPush].attributes.push(attrData);
                    if (isGroupNdxDataUpdated) {
                      self_.$broadcast('add-chart-to-group', attrData.group_id);
                    }
                    if (attrData.view_type === 'survival') {
                      self_.numOfSurvivalPlots++;
                    }
                    self_.$nextTick(function() {
                      $('#iviz-add-chart').trigger('chosen:updated');
                      self_.showScreenLoad = false;
                      $.notify({
                        // options
                        message: 'Chart has been added at the bottom of the page.'
                      },{
                        // settings
                        type: 'info',
                        delay: '1000'
                      });
                    });
                  });
                } else {
                  var newgroup_ = {};
                  var groupAttrs = [];
                  // newgroup_.data = _group.data;
                  newgroup_.type = _group.type;
                  newgroup_.id = self_.groupCount;
                  attrData.group_id = newgroup_.id;
                  attrData.show = true;
                  self_.groupCount += 1;
                  groupAttrs.push(attrData);
                  newgroup_.attributes = groupAttrs;
                  $.when(iViz.createGroupNdx(newgroup_)).then(function() {
                    self_.groups.push(newgroup_);
                    self_.$nextTick(function() {
                      $('#iviz-add-chart').trigger('chosen:updated');
                      self_.showScreenLoad = false;
                    });
                  });
                }
              });
            },
            getChartsByAttrIds: function(attrIds) {
              return _.pick(this.charts, attrIds);
            },
            removeChart: function(attrId) {
              var self = this;
              var attrData = self.charts[attrId];
              var attributes = self.groups[attrData.group_id].attributes;
              self.checkForDropDownCharts();
              attributes.$remove(attrData);

              self.$broadcast('remove-grid-item',
                $('#chart-' + attrId + '-div'));

              if (attrData.view_type === 'survival') {
                this.numOfSurvivalPlots--;
              }

              self.$nextTick(function() {
                $('#iviz-add-chart').trigger('chosen:updated');
              });
            },
            updateGeneList: function(geneList, reset) {
              var self_ = this;
              if (reset) {
                self_.selectedgenes = geneList;
              } else {
                _.each(geneList, function(gene) {
                  var index = self_.selectedgenes.indexOf(gene);
                  if (index === -1) {
                    self_.selectedgenes.push(gene);
                  } else {
                    self_.selectedgenes.splice(index, 1);
                  }
                });
              }
              this.$broadcast('gene-list-updated', self_.selectedgenes);
            },
            setSelectedCases: function(selectionType, selectedCases) {
              var radioVal = selectionType;
              var selectedCaseUIDs = [];
              var unmappedCaseIDs = [];

              _.each(selectedCases, function(id) {
                var caseUIDs = [];
                var pair = id
                  .split(':')
                  .map(function(t) {
                    return t.trim();
                  });
                if (pair.length == 2) {
                  var caseId = iViz.getCaseIndex(selectionType, pair[0], pair[1]);
                  if (caseId) {
                    caseUIDs.push(caseId);
                  }
                } else {
                  caseUIDs = iViz.getCaseUID(selectionType, id);
                }

                if (caseUIDs.length === 0) {
                  unmappedCaseIDs.push(id);
                } else {
                  selectedCaseUIDs = selectedCaseUIDs.concat(caseUIDs);
                }
              });

              if (unmappedCaseIDs.length > 0) {
                new Notification().createNotification(selectedCaseUIDs.length +
                  ' cases selected. The following ' +
                  (radioVal === 'patient' ? 'patient' : 'sample') +
                  ' ID' + (unmappedCaseIDs.length === 1 ? ' was' : 's were') +
                  ' not found in this study: ' +
                  unmappedCaseIDs.join(', '), {
                  message_type: 'danger'
                });
              } else {
                new Notification().createNotification(selectedCaseUIDs.length +
                  ' case(s) selected.', {message_type: 'info'});
              }
              if (selectedCaseUIDs.length > 0) {
                this.clearAllCharts(false);
                var self_ = this;
                Vue.nextTick(function() {
                  _.each(self_.groups, function(group) {
                    if (group.type === radioVal) {
                      self_.hasfilters = true;
                      self_.customfilter.type = group.type;
                      if (radioVal === 'sample') {
                        self_.customfilter.sampleUids = selectedCaseUIDs.sort();
                        self_.customfilter.patientUids = [];
                      } else {
                        self_.customfilter.patientUids = selectedCaseUIDs.sort();
                        self_.customfilter.sampleUids = [];
                      }
                      self_.$broadcast('update-custom-filters');
                      return false;
                    }
                  });
                });
              }
            }
          }, ready: function() {
            $('#iviz-header-left-patient-select').qtip({
              content: {text: 'View the selected patients.'},
              style: {classes: 'qtip-light qtip-rounded qtip-shadow'},
              show: {event: 'mouseover'},
              hide: {fixed: true, delay: 100, event: 'mouseout'},
              position: {
                my: 'bottom center',
                at: 'top center',
                viewport: $(window)
              }
            });
            $('#iviz-header-left-case-download').qtip({
              content: {text: 'Download clinical data for the selected cases.'},
              style: {classes: 'qtip-light qtip-rounded qtip-shadow'},
              show: {event: 'mouseover'},
              hide: {fixed: true, delay: 100, event: 'mouseout'},
              position: {
                my: 'bottom center',
                at: 'top center',
                viewport: $(window)
              }
            });
            $('#iviz-form').qtip({
              content: {text: 'Query the selected samples.'},
              style: {classes: 'qtip-light qtip-rounded qtip-shadow'},
              show: {event: 'mouseover'},
              hide: {fixed: true, delay: 100, event: 'mouseout'},
              position: {
                my: 'bottom center',
                at: 'top center',
                viewport: $(window)
              }
            });
          }
        });
      },
      getInstance: function() {
        if (typeof vmInstance_ === 'undefined') {
          this.init();
        }
        return vmInstance_;
      },
      setSelectedCases: function(selectionType, selectedCases) {
        vmInstance_.setSelectedCases(selectionType, selectedCases);
      },
      setGeneList: function(geneList) {
        vmInstance_.updateGeneList(geneList, true);
      },
      getGeneList: function() {
        return vmInstance_.selectedgenes;
      }
    };
  })();

  Vue.directive('select', {
    twoWay: true,
    params: ['charts'],
    paramWatchers: {
      charts: function() {
        $('#iviz-add-chart').trigger('chosen:updated');
      }
    },
    bind: function() {
      var self = this;
      $(this.el).chosen({
        width: '30%',
        search_contains: true
      })
        .change(
          function() {
            var value = self.el.value;
            self.params.charts[value].show = true;
            self.vm.addChart(this.el.value);
          }.bind(this)
        );
    }
  });

})(window.Vue, window.iViz, window.dc, window._);

'use strict';
var util = (function(_, cbio) {
  return (function() {
    function tableDownload(fileType, content) {
      switch (fileType) {
        case 'tsv':
          csvDownload(content.fileName, content.data);
          break;
        default:
          break;
      }
    }

    function pieChartDownload(fileType, content) {
      switch (fileType) {
        case 'tsv':
          csvDownload(content.fileName || 'data', content.data);
          break;
        case 'svg':
          pieChartCanvasDownload(content, {
            filename: content.fileName + '.svg'
          });
          break;
        case 'pdf':
          pieChartCanvasDownload(content, {
            filename: content.fileName + '.pdf',
            contentType: 'application/pdf',
            servletName: window.cbioURL + 'svgtopdf.do'
          });
          break;
        default:
          break;
      }
    }

    function getPieWidthInfo(data) {
      var length = data.title.length;
      var labels = _.values(data.labels);
      var labelMaxName = _.last(_.sortBy(_.pluck(labels, 'name'),
        function(item) {
          return item.toString().length;
        })).toString().length;
      var labelMaxNumber = _.last(_.sortBy(_.pluck(labels, 'cases'),
        function(item) {
          return item.toString().length;
        })).toString().length;
      var labelMaxFreq = _.last(_.sortBy(_.pluck(labels, 'sampleRate'),
        function(item) {
          return item.toString().length;
        })).toString().length;

      if (labelMaxName > length) {
        length = labelMaxName;
      }
      length = length * 10 + labelMaxNumber * 10 + labelMaxFreq * 10 + 30;

      return {
        svg: length,
        name: labelMaxName > data.title.length ? labelMaxName :
          data.title.length,
        number: labelMaxNumber,
        freq: labelMaxFreq
      };
    }

    function pieChartCanvasDownload(data, downloadOpts) {
      var _svgElement = '';

      var _width = getPieWidthInfo(data);
      var _pieLabelString = '';
      var _pieLabelYCoord = 0;
      var _svg = $('#' + data.chartId + ' svg');
      var _svgClone = _svg.clone();
      var _previousHidden = false;

      if ($('#' + data.chartDivId).css('display') === 'none') {
        _previousHidden = true;
        $('#' + data.chartDivId).css('display', 'block');
      }

      var _svgHeight = _svg.height();
      var _text = _svgClone.find('text');
      var _textLength = _text.length;
      var _slice = _svgClone.find('g .pie-slice');
      var _sliceLength = _slice.length;
      var _pieLabel = _.sortBy(_.values(data.labels), function(item) {
        return -item.cases;
      });
      var _pieLabelLength = _pieLabel.length;
      var i = 0;

      if (_previousHidden) {
        $('#' + data.chartDivId).css('display', 'none');
      }

      // Change pie slice text styles
      for (i = 0; i < _textLength; i++) {
        $(_text[i]).css({
          'fill': 'white',
          'font-size': '14px',
          'stroke': 'white',
          'stroke-width': '1px'
        });
      }

      // Change pie slice styles
      for (i = 0; i < _sliceLength; i++) {
        $($(_slice[i]).find('path')[0]).css({
          'stroke': 'white',
          'stroke-width': '1px'
        });
      }

      if (_width.svg < 180) {
        _width.svg = 180;
      }

      // Draw sampleSize header
      _pieLabelString += '<g transform="translate(0, ' +
        _pieLabelYCoord + ')"><text x="13" y="10" ' +
        'style="font-size:12px; font-weight:bold">' +
        data.title + '</text>' +
        '<text x="' + _width.name * 10 + '" y="10" ' +
        'style="font-size:12px; font-weight:bold">#</text>' +
        '<text x="' + (_width.name + _width.number) * 10 + '" y="10" ' +
        'style="font-size:12px; font-weight:bold">Freq</text>' +
        '<line x1="0" y1="14" x2="' +
        ((_width.name + _width.number) * 10 - 20) + '" y2="14" ' +
        'style="stroke:black;stroke-width:2"></line>' +
        '<line x1="' + (_width.name * 10 - 10) + '" y1="14" x2="' +
        (_width.svg - 20) + '" y2="14" ' +
        'style="stroke:black;stroke-width:2"></line>' +
        '<line x1="' + ((_width.name + _width.number) * 10 - 10) +
        '" y1="14" x2="' + (_width.svg - 20) + '" y2="14" ' +
        'style="stroke:black;stroke-width:2"></line>' +
        '</g>';

      _pieLabelYCoord += 18;

      // Draw pie label into output
      for (i = 0; i < _pieLabelLength; i++) {
        var _label = _pieLabel[i];

        _pieLabelString += '<g transform="translate(0, ' +
          _pieLabelYCoord + ')"><rect height="10" width="10" fill="' +
          _label.color + '"></rect><text x="13" y="10" ' +
          'style="font-size:15px">' + _label.name + '</text>' +
          '<text x="' + _width.name * 10 + '" y="10" ' +
          'style="font-size:15px">' + _label.cases + '</text>' +
          '<text x="' + (_width.name + _width.number) * 10 + '" y="10" ' +
          'style="font-size:15px">' + _label.sampleRate + '</text>' +
          '</g>';

        _pieLabelYCoord += 15;
      }

      _svgClone.children().each(function(i, e) {
        _svgElement += cbio.download.serializeHtml(e);
      });

      var svg = '<svg xmlns="http://www.w3.org/2000/svg" ' +
        'version="1.1" width="' + _width.svg + '" height="' +
        (180 + _pieLabelYCoord) + '">' +
        '<g><text x="' + (_width.svg / 2) + '" y="20" ' +
        'style="font-weight: bold;" text-anchor="middle">' +
        data.title + '</text></g>' +
        '<g transform="translate(' + (_width.svg / 2 - 65) + ', 20)">' +
        _svgElement + '</g>' +
        '<g transform="translate(10, ' + (_svgHeight + 20) + ')">' +
        _pieLabelString + '</g></svg>';

      cbio.download.initDownload(svg, downloadOpts);
    }

    function barChartCanvasDownload(data, downloadOpts) {
      var _svgElement = '';
      var _svg = $('#' + data.chartId + '>svg').clone();
      var _svgWidth = Number(_svg.attr('width'));
      var _svgHeight = Number(_svg.attr('height')) + 20;
      var _brush = _svg.find('g.brush');
      var _brushWidth = Number(_brush.find('rect.extent').attr('width'));
      var i = 0;

      // Remove brush if the width is zero(no rush presents)
      // Otherwise width 0 brush will still show in the PDF
      if (_brushWidth === 0) {
        _brush.remove();
      }

      _brush.find('rect.extent')
        .css({
          'fill-opacity': '0.2',
          'fill': '#2986e2'
        });

      _brush.find('.resize path')
        .css({
          fill: '#eee',
          stroke: '#666'
        });

      // Change deselected bar chart
      var _chartBody = _svg.find('.chart-body');
      var _deselectedCharts = _chartBody.find('.bar.deselected');
      var _deselectedChartsLength = _deselectedCharts.length;

      for (i = 0; i < _deselectedChartsLength; i++) {
        $(_deselectedCharts[i]).css({
          stroke: '',
          fill: '#ccc'
        });
      }

      // Change axis style
      var _axis = _svg.find('.axis');
      var _axisDomain = _axis.find('.domain');
      var _axisDomainLength = _axisDomain.length;
      var _axisTick = _axis.find('.tick.major line');
      var _axisTickLength = _axisTick.length;

      for (i = 0; i < _axisDomainLength; i++) {
        $(_axisDomain[i]).css({
          'fill': 'white',
          'fill-opacity': '0',
          'stroke': 'black'
        });
      }

      for (i = 0; i < _axisTickLength; i++) {
        $(_axisTick[i]).css({
          stroke: 'black'
        });
      }

      // Remove clip-path from chart-body. Clip-path causes issue when
      // generating pdf and useless in here.
      // Related topic: https://github.com/dc-js/dc.js/issues/730
      _chartBody.attr('clip-path', '');

      // Change x/y axis text size
      var _chartText = _svg.find('.axis text');
      var _chartTextLength = _chartText.length;

      for (i = 0; i < _chartTextLength; i++) {
        $(_chartText[i]).css({
          'font-size': '12px'
        });
      }

      _svg.children().each(function(i, e) {
        _svgElement += cbio.download.serializeHtml(e);
      });

      var svg = '<svg xmlns="http://www.w3.org/2000/svg" version="1.1" ' +
        'width="' + _svgWidth + '" height="' + _svgHeight + '">' +
        '<g><text x="' + (_svgWidth / 2) + '" y="20" ' +
        'style="font-weight: bold; text-anchor: middle">' +
        data.title + '</text></g>' +
        '<g transform="translate(0, 20)">' + _svgElement + '</g></svg>';

      cbio.download.initDownload(
        svg, downloadOpts);

      _brush.css('display', '');
    }

    function survivalChartDownload(fileType, content) {
      switch (fileType) {
        case 'svg':
          survivalChartCanvasDownload(content, {
            filename: content.fileName + '.svg'
          });
          break;
        case 'pdf':
          survivalChartCanvasDownload(content, {
            filename: content.fileName + '.pdf',
            contentType: 'application/pdf',
            servletName: window.cbioURL + 'svgtopdf.do'
          });
          break;
      case 'tsv':
        csvDownload(content.fileName, content.data);
        break;
        default:
          break;
      }
    }

    function survivalChartCanvasDownload(data, downloadOpts) {
      var _svgElement;
      var _svgTitle;
      var _labelTextMaxLength = 0;
      var _numOfLabels = 0;
      var _svg = $('#' + data.chartDivId).clone();
      var _svgWidth = Number($('#' + data.chartDivId + ' svg').attr('width')) + 50;
      var _svgheight = Number($('#' + data.chartDivId + ' svg').attr('height')) + 50;

      // This is for PDF download. fill transparent will be treated as black.
      _svg.find('rect').each(function(index, item) {
        if ($(item).css('fill') === 'transparent') {
          $(item).css('fill', 'white');
        }
      });
      _svgElement = cbio.download.serializeHtml(_svg.find('svg')[0]);

      _svgWidth += _labelTextMaxLength * 14;

      if (_svgheight < _numOfLabels * 20) {
        _svgheight = _numOfLabels * 20 + 40;
      }

      _svgTitle = '<g><text text-anchor="middle" x="210" y="30" ' +
        'style="font-weight:bold">' + data.title + '</text></g>';

      _svgElement = '<svg xmlns="http://www.w3.org/2000/svg" ' +
        'version="1.1" width="' + _svgWidth + 'px" height="' + _svgheight +
        'px" style="font-size:14px">' +
        _svgTitle + '<g transform="translate(0,40)">' +
        _svgElement + '</g>' +
        '</svg>';

      cbio.download.initDownload(
        _svgElement, downloadOpts);
    }

    function csvDownload(fileName, content) {
      fileName = fileName || 'test';
      var downloadOpts = {
        filename: fileName + '.txt',
        contentType: 'text/plain;charset=utf-8',
        preProcess: false
      };

      cbio.download.initDownload(content, downloadOpts);
    }

    function barChartDownload(fileType, content) {
      switch (fileType) {
      case 'tsv':
        csvDownload(content.fileName || 'data', content.data);
        break;
      case 'svg':
        barChartCanvasDownload(content, {
          filename: content.fileName + '.svg'
        });
        break;
      case 'pdf':
        barChartCanvasDownload(content, {
          filename: content.fileName + '.pdf',
          contentType: 'application/pdf',
          servletName: window.cbioURL + 'svgtopdf.do'
        });
        break;
      default:
        break;
      }
    }

    /**
     * @author Adam Abeshouse
     * @param {number | string} a
     * @param {number | string} b
     * @param {boolean} asc
     * @return {number} result
     */
    function compareValues(a, b, asc) {
      var ret = 0;
      if (a !== b) {
        if (a === null) {
          // a sorted to end
          ret = 1;
        } else if (b === null) {
          // b sorted to end
          ret = -1;
        } else {
          // neither are null
          if (typeof a === "number") {
            // sort numbers
            if (a < b) {
              ret = (asc ? -1 : 1);
            } else {
              // we know a !== b here so this case is a > b
              ret = (asc ? 1 : -1);
            }
          } else if (typeof a === "string") {
            // sort strings
            ret = (asc ? 1 : -1) * (a.localeCompare(b));
          }
        }
      }
      return ret;
    }

    var content = {};

    /**
     * Convert number to specific precision end.
     * @param {number} number The number you want to convert.
     * @param {integer} precision Significant figures.
     * @param {number} threshold The upper bound threshold.
     * @return {number} Converted number.
     */
    content.toPrecision = function(number, precision, threshold) {
      if (number >= 0.000001 && number < threshold) {
        return number.toExponential(precision);
      }
      return number.toPrecision(precision);
    };

    /**
     * iViz color schema.
     * @return {string[]} Color array.
     */
    content.getColors = function() {
      return [
        '#2986e2', '#dc3912', '#f88508', '#109618',
        '#990099', '#0099c6', '#dd4477', '#66aa00',
        '#b82e2e', '#316395', '#994499', '#22aa99',
        '#aaaa11', '#6633cc', '#e67300', '#8b0707',
        '#651067', '#329262', '#5574a6', '#3b3eac',
        '#b77322', '#16d620', '#b91383', '#f4359e',
        '#9c5935', '#a9c413', '#2a778d', '#668d1c',
        '#bea413', '#0c5922', '#743411', '#743440',
        '#9986e2', '#6c3912', '#788508', '#609618',
        '#790099', '#5099c6', '#2d4477', '#76aa00',
        '#882e2e', '#916395', '#794499', '#92aa99',
        '#2aaa11', '#5633cc', '#667300', '#100707',
        '#751067', '#229262', '#4574a6', '#103eac',
        '#177322', '#66d620', '#291383', '#94359e',
        '#5c5935', '#29c413', '#6a778d', '#868d1c',
        '#5ea413', '#6c5922', '#243411', '#103440',
        '#2886e2', '#d93912', '#f28508', '#110618',
        '#970099', '#0109c6', '#d10477', '#68aa00',
        '#b12e2e', '#310395', '#944499', '#24aa99',
        '#a4aa11', '#6333cc', '#e77300', '#820707',
        '#610067', '#339262', '#5874a6', '#313eac',
        '#b67322', '#13d620', '#b81383', '#f8359e',
        '#935935', '#a10413', '#29778d', '#678d1c',
        '#b2a413', '#075922', '#763411', '#773440',
        '#2996e2', '#dc4912', '#f81508', '#104618',
        '#991099', '#0049c6', '#dd2477', '#663a00',
        '#b84e2e', '#312395', '#993499', '#223a99',
        '#aa1a11', '#6673cc', '#e66300', '#8b5707',
        '#656067', '#323262', '#5514a6', '#3b8eac',
        '#b71322', '#165620', '#b99383', '#f4859e',
        '#9c4935', '#a91413', '#2a978d', '#669d1c',
        '#be1413', '#0c8922', '#742411', '#744440',
        '#2983e2', '#dc3612', '#f88808', '#109518',
        '#990599', '#0092c6', '#dd4977', '#66a900',
        '#b8282e', '#316295', '#994199', '#22a499',
        '#aaa101', '#66310c', '#e67200', '#8b0907',
        '#651167', '#329962', '#5573a6', '#3b37ac',
        '#b77822', '#16d120', '#b91783', '#f4339e',
        '#9c5105', '#a9c713', '#2a710d', '#66841c',
        '#bea913', '#0c5822', '#743911', '#743740',
        '#298632', '#dc3922', '#f88588', '#109658',
        '#990010', '#009916', '#dd4447', '#66aa60',
        '#b82e9e', '#316365', '#994489', '#22aa69',
        '#aaaa51', '#66332c', '#e67390', '#8b0777',
        '#651037', '#329232', '#557486', '#3b3e4c',
        '#b77372', '#16d690', '#b91310', '#f4358e',
        '#9c5910', '#a9c493', '#2a773d', '#668d5c',
        '#bea463', '#0c5952', '#743471', '#743450',
        '#2986e3', '#dc3914', '#f88503', '#109614',
        '#990092', '#0099c8', '#dd4476', '#66aa04',
        '#b82e27', '#316397', '#994495', '#22aa93',
        '#aaaa14', '#6633c1', '#e67303', '#8b0705',
        '#651062', '#329267', '#5574a1', '#3b3ea5'
      ];
    };

    content.idMapping = function(mappingObj, inputCases) {
      var _selectedMappingCases = {};

      _.each(inputCases, function(_case) {
        _.each(mappingObj[_case], function(_case) {
          _selectedMappingCases[_case] = '';
        });
      });

      return Object.keys(_selectedMappingCases);
    };

    content.unique = function(arr_) {
      var tempArr_ = {};
      _.each(arr_, function(obj_) {
        if (tempArr_[obj_] === undefined) {
          tempArr_[obj_] = true;
        }
      });
      return Object.keys(tempArr_);
    };

    content.isRangeFilter = function(filterObj) {
      if (filterObj.filterType !== undefined
        && filterObj.filterType === 'RangedFilter') {
        return true;
      }
      return false;
    };

    content.sortByAttribute = function(objs, attrName) {
      function compare(a, b) {
        if (a[attrName] < b[attrName]) {
          return -1;
        }
        if (a[attrName] > b[attrName]) {
          return 1;
        }
        return 0;
      }

      objs.sort(compare);
      return objs;
    };

    content.download = function(chartType, fileType, content) {
      switch (chartType) {
      case 'pieChart':
        pieChartDownload(fileType, content);
        break;
      case 'barChart':
        barChartDownload(fileType, content);
        break;
      case 'survivalPlot':
        survivalChartDownload(fileType, content);
        break;
      case 'scatterPlot':
        survivalChartDownload(fileType, content);
        break;
      case 'table':
        tableDownload(fileType, content);
        break;
      default:
        break;
      }
    };

    content.restrictNumDigits = function(str) {
      if (!isNaN(str)) {
        var num = Number(str);
        if (num % 1 !== 0) {
          num = num.toFixed(2);
          str = num.toString();
        }
      }
      return str;
    };

    /**
     * Get a random color hex.
     *
     * @return {string} Color HEX
     */
    content.getRandomColor = function() {
      var letters = '0123456789abcdef';
      var color = '#';
      for (var i = 0; i < 6; i++) {
        color += letters[Math.floor(Math.random() * 16)];
      }
      return color;
    };

    /**
     * Get a random color hex out of Colors in getColors function;
     *
     * @return {string} Color HEX
     */
    content.getRandomColorOutOfLib = function() {
      var color;
      while (!color || content.getColors().indexOf(color) !== -1) {
        color = content.getRandomColor();
      }
      return color;
    };

    content.calcFreq = function(fraction, numerator, toFixed) {
      var freq = 0;
      toFixed = isNaN(toFixed) ? 2 : Number(toFixed);
      if (numerator > 0) {
        freq = fraction / numerator * 100;
        freq = freq % 1 === 0 ? freq : freq.toFixed(toFixed);
      }
      return freq + '%';
    };

    /**
     * Remove illegal characters for DOM id
     * @param {string} str Original DOM id string
     * @return {string} trimmed id
     */
    content.trimDomId = function(str) {
      if (str) {
        str = str.replace(/>/g, '_greater_than_');
        str = str.replace(/</g, '_less_than_');
        str = str.replace(/\+/g, '_plus_');
        str = str.replace(/-/g, '_minus_');
        str = str.replace(/^[^a-z]+|[^\w:.-]+/gi, '');
      }
      return str;
    };

    /**
     * Generate default DOM ids
     * @param {string} type Available types are: chartDivId, resetBtnId, chartId, chartTableId
     * @param {string} attrId
     * @return {string} Default DOM id
     */
    content.getDefaultDomId = function(type, attrId) {
      var domId = '';
      if (type && attrId) {
        var attrId = this.trimDomId(attrId);
        switch (type) {
        case 'chartDivId':
          domId = 'chart-' + attrId + '-div';
          break;
        case 'resetBtnId':
          domId = 'chart-' + attrId + '-reset';
          break;
        case 'chartId':
          domId = 'chart-new-' + attrId;
          break;
        case 'chartTableId':
          domId = 'table-' + attrId;
          break;
        case 'progressBarId':
          domId = attrId + '-pb';
          break;
        }
      }
      // TODO: DOM id pool. Ideally id shouldn't be repeated
      return domId;
    };

    /**
     * Finds the intersection elements between two arrays in a simple fashion.
     * Should have O(n) operations, where n is n = MIN(a.length, b.length)
     *
     * @param {array} a First array, must already be sorted
     * @param {array} b Second array, must already be sorted
     * @return {array} The interaction elements between a and b
     */
    content.intersection = function(a, b) {
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
     * Returns a copy of the array with values from array that are not present in the other array.
     *
     * @param {array} a Array, must already be sorted
     * @param {array} b The other array, must already be sorted
     * @return {array} The difference values
     */
    content.difference = function(a, b) {
      var result = [];
      var i = 0;
      var j = 0;
      var aL = a.length;
      var bL = b.length;
      while (i < aL && j < bL) {
        if (a[i] < b[j]) {
          result.push(a[i]);
          ++i;
        } else if (a[i] > b[j]) {
          ++j;
        } else {
          ++i;
          ++j;
        }
      }

      return result;
    };

    content.compare = function(arr1, arr2) {
      return JSON.stringify(arr1) === JSON.stringify(arr2);
    };

    content.getClinicalAttrTooltipContent = function(attribute) {
      var string = [];
      if (attribute.display_name) {
        string.push('<b>' + attribute.display_name + '</b>');
      }
      if (attribute.description) {
        string.push(attribute.description);
      }
      return string.join('<br/>');
    };

    content.getCaseIdsGroupByCategories = function(groupType, dcDimension, attrId) {
      var _cases = [];
      var _caseIds = {};

      if (!groupType || !dcDimension) {
        return _caseIds;
      }

      _cases = dcDimension.top(Infinity);

      for (var i = 0; i < _cases.length; i++) {
        var _key = _cases[i][attrId];

        if (!_caseIds.hasOwnProperty(_key)) {
          _caseIds[_key] = [];
        }
        var _groupKey = groupType === 'patient' ? 'patient_uid' : 'sample_uid';
        _caseIds[_key].push(_cases[i][_groupKey]);
      }

      return _caseIds;
    };

    content.getAttrVal = function(attrs, arr) {
      var str = [];
      _.each(attrs, function(displayName, attrId) {
        if (attrId === 'cna_details' || attrId === 'mutated_genes') {
          var temp = 'No';
          if (arr[attrId] !== undefined) {
            temp = arr[attrId].length > 0 ? 'Yes' : 'No';
          }
          str.push(temp);
        } else {
          str.push(arr[attrId] ? arr[attrId] : 'NA');
        }
      });
      return str;
    };

    /**
     * If input is na, NA, NaN, n/a, null or undefined, return true
     * Else, return false.
     * @param str
     * @param includeEmptyStr Whether empty string should be treated as NA
     * @returns {boolean}
     */
    content.strIsNa = function(str, includeEmptyStr) {
      var status = false;
      includeEmptyStr = _.isBoolean(includeEmptyStr) ? includeEmptyStr : false;
      if (_.isString(str)) {
        if (['na', 'nan', 'n/a'].indexOf(str.toLowerCase()) > -1 ||
          (includeEmptyStr && !str)) {
          status = true;
        }
      } else if (typeof str === 'undefined' || str === null) {
        status = true;
      }
      return status;
    };

    content.getTickFormat = function(v, logScale, data_, opts_) {
      var _returnValue = v;
      var index = 0;
      var e = d3.format('.1e');// convert small data to scientific notation format
      var formattedValue = '';

      if (data_.noGrouping) {
        if (v === opts_.emptyMappingVal) {
          _returnValue = 'NA';
        } else {
          // When noGrouping is true, the number of unique data points is less than 6.
          // The distance maybe big between points, so we use xFakeDomain to set ticks.
          // The value of ticks shown on chart is gotten from xDomain.
          if (data_.smallDataFlag) {
            _returnValue = e(opts_.xDomain[opts_.xFakeDomain.indexOf(v)]);
          } else {
            _returnValue = opts_.xDomain[opts_.xFakeDomain.indexOf(v)];
          }
        }
      } else if (logScale) {
        if (v === opts_.emptyMappingVal) {
          _returnValue = 'NA';
        } else {
          index = opts_.xDomain.indexOf(v);
          if (index % 2 !== 0) {
            _returnValue = '';
          }
        }
      } else if (v === opts_.emptyMappingVal || opts_.xDomain.length === 1) {
        return 'NA';
      } else if (v === opts_.xDomain[0]) {
        formattedValue = opts_.xDomain[1];
        if (data_.smallDataFlag) {
          return '<=' + e(formattedValue);
        }
        return '<=' + formattedValue;
      } else if ((v === opts_.xDomain[opts_.xDomain.length - 2] && data_.hasNA) ||
        (v === opts_.xDomain[opts_.xDomain.length - 1] && !data_.hasNA)) {
        if (data_.hasNA) {
          formattedValue = opts_.xDomain[opts_.xDomain.length - 3];
        } else {
          formattedValue = opts_.xDomain[opts_.xDomain.length - 2];
        }
        if (data_.smallDataFlag) {
          return '>' + e(formattedValue);
        }
        return '>' + formattedValue;
      } else if (data_.min > 1500 &&
        opts_.xDomain.length > 7) {
        // this is the special case for printing out year
        index = opts_.xDomain.indexOf(v);
        if (index % 2 === 0) {
          _returnValue = v;
        } else {
          _returnValue = '';
        }
      } else {
        _returnValue = v;
      }

      if (data_.smallDataFlag && !data_.noGrouping) {
        _returnValue = e(_returnValue);
        var _tempValue = e(v).toString();
        if (_tempValue.charAt(0) !== '1') {// hide tick values whose format is not 1.0e-N
          _returnValue = '';
        }
      }
      return _returnValue;
    };

    content.defaultQtipConfig = function(content) {
      var configuration = {
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
        content: content
      };

      return configuration;
    };

    content.getDataErrorMessage = function(type) {
      var message = 'Failed to load data';
      switch (type) {
      case 'dataInvalid':
        message = 'Data Invalid' + (iViz.opts.emailContact ?
          ('<span v-if="emailContact">' +
            ', please contact <span v-html="emailContact"></span></span>') : '');
        break;
      case 'noData':
        message = 'No data available';
        break;
      case 'failedToLoadData':
        message = 'Failed to load data, refresh the page may help';
        break;
      }
      return message;
    };

    content.getHypotenuse = function(a, b) {
      return Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
    };

    content.isAgeClinicalAttr = function(attrId) {
      var isRelated = false;
      if (attrId) {
        var result = attrId.match(/(^AGE$)|(^AGE_.*)|(.*_AGE_.*)|(.*_AGE&)/);
        isRelated = _.isArray(result) && result.length > 0;
      }
      return isRelated;
    };

    /**
     *
     * @param filters Array Filters should contain two elements
     */
    content.getDisplayBarChartBreadCrumb = function(filters) {
      var str = '';
      if (filters[0] && filters[1]) {
        str = filters[0] + ' ~ ' + filters[1];
      } else if (filters[0]) {
        str = filters[0];
      } else if (filters[1]) {
        str = filters[1];
      }
      return str;
    };

    return content;
  })();
})(window._, window.cbio);

window.iViz.util = util;

//
// Expose the module.
//
window.module = window.module || {};

// export util.getTickFormat for testing
module.exports = {
  getTickFormat: util.getTickFormat
};
/**
 * @author Hongxin Zhang on 5/12/17.
 */

/**
 * Please see Study-View.md under cBioPortal repository for more information 
 * about priority and layout.
 */


'use strict';
(function(iViz, _) {
  iViz.priorityManager = (function() {
    var content = {};
    var clinicalAttrsPriority = {};
    var defaultPriority = 1;

    /**
     * Calculate combination chart priority
     * @param {string} id Clinical attribute ID.
     * @return {array}
     */
    function getCombinationPriority(id) {
      var priority = _.clone(defaultPriority);
      if (id) {
        switch (id) {
        case 'DFS_SURVIVAL':
          var _dfsStatus = getPriority('DFS_STATUS');
          var _dfsMonths = getPriority('DFS_MONTHS');
          if (_dfsStatus === 0 || _dfsMonths === 0) {
            priority = 0;
          } else {
            priority = (_dfsMonths + _dfsStatus ) / 2;
            priority = priority > 1 ? priority :
              clinicalAttrsPriority['DFS_SURVIVAL'];
          }
          break;
        case 'OS_SURVIVAL':
          var _osStatus = getPriority('OS_STATUS');
          var _osMonths = getPriority('OS_MONTHS');
          if (_osStatus === 0 || _osMonths === 0) {
            priority = 0;
          } else {
            priority = (_osStatus + _osMonths ) / 2;
            priority = priority > 1 ? priority :
              clinicalAttrsPriority['OS_SURVIVAL'];
          }
          break;
        case 'MUT_CNT_VS_CNA':
          priority = clinicalAttrsPriority['MUT_CNT_VS_CNA'];
          break;
        }
      }
      return priority;
    }

    /**
     * Get priority by clinical attribute.
     * @param{string} id Clinical attribute ID.
     * @return {number}
     */
    function getPriority(id) {
      return clinicalAttrsPriority.hasOwnProperty(id)
        ? clinicalAttrsPriority[id] : 1;
    }

    content.comparePriorities = function(_a, _b, asc) {
      return asc ? (_a - _b) : (_b - _a);
    };

    content.getDefaultPriority = function(id, isCombinationChart) {
      var priority = _.clone(defaultPriority);
      if (!_.isBoolean(isCombinationChart)) {
        isCombinationChart = false;
      }
      if (id) {
        if (isCombinationChart) {
          priority = getCombinationPriority(id);
        } else {
          priority = clinicalAttrsPriority.hasOwnProperty(id) ?
            clinicalAttrsPriority[id] : priority;
        }
      }
      return priority;
    };

    content.setClinicalAttrPriority = function(attr, priority) {
      if (attr) {
        if (priority !== 1 || !clinicalAttrsPriority.hasOwnProperty(attr)) {
          clinicalAttrsPriority[attr] = priority;
        }
      }
    };

    content.setDefaultClinicalAttrPriorities = function(priorities) {
      if (_.isObject(priorities)) {
        _.extend(clinicalAttrsPriority, priorities);
      }
    };

    return content;
  })();
})(window.iViz,
  window._);
/**
 * Created by Karthik Kalletla on 4/13/16.
 */
'use strict';
(function(Vue, dc, iViz, Packery, Draggabilly, _) {
  Vue.component('mainTemplate', {
    template: '<chart-group :redrawgroups.sync="redrawgroups" ' +
    ':hasfilters="hasfilters" :id="group.id" :type="group.type" ' +
    ':mappedcases="group.type==\'patient\'?patientsync:samplesync" ' +
    ' :attributes.sync="group.attributes" :clear-group="clearAll"' +
    ' v-for="group in groups" :showed-survival-plot="showedSurvivalPlot"></chart-group> ',
    props: [
      'groups', 'selectedsampleUIDs', 'selectedpatientUIDs', 'hasfilters',
      'redrawgroups', 'customfilter', 'clearAll', 'showedSurvivalPlot'
    ], data: function() {
      return {
        patientsync: [],
        samplesync: [],
        grid_: '',
        completePatientsList: [],
        completeSamplesList: [],
        selectedPatientsByFilters: [],
        selectedSamplesByFilters: [],
        initialized: false,
        renderGroups: [],
        chartsGrid: [],
        windowResizeTimeout: ''
      };
    }, watch: {
      groups: function() {
        if (!this.initialized) {
          this.initialized = true;
          this.selectedPatientsByFilters =
            _.keys(iViz.getCasesMap('patient')).sort();
          this.selectedSamplesByFilters =
            _.keys(iViz.getCasesMap('sample')).sort();
          this.completePatientsList =
            _.keys(iViz.getCasesMap('patient')).sort();
          this.completeSamplesList =
            _.keys(iViz.getCasesMap('sample')).sort();
        }
      },
      renderGroups: function(groupIds) {
        var _keys = window.cbio.util.uniqueElementsOfArray(groupIds);
        if (_keys.length > 0) {
          _.each(_keys, function(groupid) {
            dc.renderAll(groupid);
          });
          this.renderGroups = [];
        }
      },
      chartsGrid: function(ChartsIds) {
        var _keys = window.cbio.util.uniqueElementsOfArray(ChartsIds);
        if (_keys.length > 0) {
          this.updateGrid(_keys);
          this.chartsGrid = [];
        }
      },
      clearAll: function(flag) {
        if (flag) {
          this.selectedPatientsByFilters = [];
          this.selectedSamplesByFilters = [];
        }
      }
    }, methods: {
      sortByNumber: function(a, b) {
        var _a = this.$root.charts[a.element.attributes['attribute-id'].nodeValue].layout[0];
        var _b = this.$root.charts[b.element.attributes['attribute-id'].nodeValue].layout[0];
        return _b - _a;
      },
      updateGrid: function(ChartsIds) {
        var self_ = this;
        if (this.grid_ === '') {
          self_.grid_ = new Packery(document.querySelector('.grid'), {
            itemSelector: '.grid-item',
            columnWidth: window.iViz.styles.vars.width.one + 5,
            rowHeight: window.iViz.styles.vars.height.one + 5,
            gutter: 5,
            initLayout: false
          });
          self_.updateLayoutMatrix();
          self_.grid_.items.sort(this.sortByNumber);
          _.each(self_.grid_.getItemElements(), function(_gridItem) {
            var _draggie = new Draggabilly(_gridItem, {
              handle: '.dc-chart-drag'
            });
            self_.grid_.bindDraggabillyEvents(_draggie);
          });
          self_.grid_.on( 'dragItemPositioned', function() {
            self_.$dispatch('user-moved-chart');
          });
        } else {
          var chartDivIds = _.pluck(self_.grid_.getItemElements(), 'id');
          _.each(ChartsIds, function(chartId) {
            // make sure that async charts' divId not in current grids
            if (!_.includes(chartDivIds, chartId)) {
              self_.grid_.addItems(document.getElementById(chartId));
              var _draggie = new Draggabilly(document.getElementById(chartId), {
                handle: '.dc-chart-drag'
              });
              self_.grid_.bindDraggabillyEvents(_draggie);
            }
          });
        }
        self_.grid_.layout();
      },
      updateLayoutMatrix: function() {
        var self_ = this;
        var _charts = _.values(this.$root.charts);
        _charts.sort(function(a, b) {
          return iViz.priorityManager.comparePriorities(a.priority, b.priority, false);
        });

        // Group attributes into 2*2 matrix
        var layoutMatrix = [];
        _.each(_charts, function(chart) {
          if (chart.show) {
            layoutMatrix = self_.getLayoutMatrix(layoutMatrix, chart);
          }
        });

        // Layout group base on window width.
        var layoutAttrs = [];
        var layoutB = [];
        var browserWidth = $('#main-grid').width() || 1200;
        var groupsPerRow = Math.floor(browserWidth / 400);
        
        // One group will be at least displayed on the page;
        // Lower than 1 will also create infinite loop of following function
        groupsPerRow = groupsPerRow < 1 ? 1 : groupsPerRow;

        for (var i = 0; i < layoutMatrix.length;) {
          var _group = [];
          for (var j = 0; j < groupsPerRow; j++) {
            _group.push(layoutMatrix[i + j]);
          }
          layoutAttrs.push(_group);
          i = i + groupsPerRow;
        }

        _.each(layoutAttrs, function(group) {
          // Plot first two elements
          _.each(group, function(item) {
            if (item) {
              for (var j = 0; j < 2; j++) {
                layoutB.push(item.matrix[j]);
              }
            }
          });
          // Plot rest third and forth elements
          _.each(group, function(item) {
            if (item) {
              for (var j = 2; j < 4; j++) {
                layoutB.push(item.matrix[j]);
              }
            }
          });
        });
        _.each(_.filter(_.uniq(layoutB).reverse(), function(item) {
          return _.isString(item);
        }), function(attrId, index) {
          self_.$root.charts[attrId].layout[0] = index;
        });
      },
      updateLayout: function() {
        this.updateLayoutMatrix();
        if (_.isObject(this.grid_)) {this.grid_.items.sort(this.sortByNumber);
        this.grid_.layout();}
      },
      getLayoutMatrix: function(layoutMatrix, chart) {
        var self_ = this;
        var neighborIndex;
        var foundSpace = false;
        var layout = chart.layout;
        var space = layout[1];
        var direction = 'h'; // h or v

        _.some(layoutMatrix, function(layoutItem) {
          if (foundSpace) {
            return true;
          }
          if (layoutItem.notFull) {
            var _matrix = layoutItem.matrix;
            _.some(_matrix, function(item, _matrixIndex) {
              if (space === 2) {
                var _validIndex = false;
                if (direction === 'v') {
                  neighborIndex = _matrixIndex + 2;
                  if (_matrixIndex < 2) {
                    _validIndex = true;
                  }
                } else {
                  neighborIndex = _matrixIndex + 1;
                  if (_matrixIndex % 2 === 0) {
                    _validIndex = true;
                  }
                }
                if (neighborIndex < _matrix.length && _validIndex) {
                  if (item === -1 && _matrix[neighborIndex] === -1) {
                    // Found a place for chart
                    _matrix[_matrixIndex] = _matrix[neighborIndex] = chart.attr_id;
                    foundSpace = true;
                    layoutItem.notFull = !self_.matrixIsFull(_matrix);
                    return true;
                  }
                }
              } else if (space === 1) {
                if (item === -1) {
                  // Found a place for chart
                  _matrix[_matrixIndex] = chart.attr_id;
                  foundSpace = true;
                  if (_matrixIndex === _matrix.length - 1) {
                    layoutItem.notFull = false;
                  }
                  return true;
                }
              } else if (space === 4) {
                if (item === -1 && _matrix[0] === -1 && _matrix[1] === -1 && _matrix[2] === -1 && _matrix[3] === -1) {
                  // Found a place for chart
                  _matrix = [chart.attr_id, chart.attr_id, chart.attr_id, chart.attr_id];
                  layoutItem.notFull = false;
                  foundSpace = true;
                  return true;
                }
              }
            });
            layoutItem.matrix = _matrix;
          }
        });

        if (!foundSpace) {
          layoutMatrix.push({
            notFull: true,
            matrix: [-1, -1, -1, -1]
          });
          layoutMatrix = self_.getLayoutMatrix(layoutMatrix, chart);
        }
        return layoutMatrix;
      },
      matrixIsFull: function(matrix) {
        var full = true;
        _.some(matrix, function(item) {
          if (item === -1) {
            full = false;
            return true;
          }
        });
        return full;
      },
    },
    events: {
      'update-grid': function() {
        this.grid_.layout();
      }, 'remove-grid-item': function(item) {
        var self_ = this;
        if (self_.grid_ === '') {
          self_.grid_ = new Packery(document.querySelector('.grid'), {
            itemSelector: '.grid-item',
            columnWidth: window.iViz.styles.vars.width.one + 5,
            rowHeight: window.iViz.styles.vars.height.one + 5,
            gutter: 5,
            initLayout: false
          });
          self_.updateLayoutMatrix();
          self_.grid_.items.sort(this.sortByNumber);
          _.each(self_.grid_.getItemElements(), function(_gridItem) {
            var _draggie = new Draggabilly(_gridItem, {
              handle: '.dc-chart-drag'
            });
            self_.grid_.bindDraggabillyEvents(_draggie);
          });
          self_.grid_.on( 'dragItemPositioned', function() {
            self_.$dispatch('user-moved-chart');
          });
        }
        this.grid_.remove(item);
        this.grid_.layout();
      },
      'data-loaded': function(groupId, chartDivId) {
        this.chartsGrid.push(chartDivId);
        this.renderGroups.push(groupId);
      },
      /*
       * This method is to find out the selected cases and the cases
       * to be synced between groups.
       *
       * STEPS involved
       * 1. Check if there are any custom case filter.
       * If yes update filter case list.
       * 2. Loop thorough groups and do the following
       *   a. Check for filters in each attribute and set _hasFilters flag
       *   b. Capture all filters for that(input) particular
       *      type group(patient/sample)
       * 3. Check of empty selected and counter selected cases
       *    and update accordingly
       * 4. Get the counter mapped cases for the selected cases
       * 5. Find the result counter selected cases
       * 6. Find the result selected cases
       * 7. Find the cases to sync
       * 8. Set the results according to the type of the update(patient/sample)
       *
       * Note: If the filter update is from patient group then its counter
       * would be sample and if filter update is from sample group then
       * its counter would be patient
       */
      'update-all-filters': function(updateType_) {
        var _selectedCasesByFilters = [];
        var _counterSelectedCasesByFilters = [];
        var self_ = this;
        var _hasFilters = false;
        var _caseType = (updateType_ === 'patient') ? 'patient' : 'sample';
        var _counterCaseType =
          (updateType_ === 'patient') ? 'sample' : 'patient';

        if (self_.customfilter.patientUids.length > 0 ||
          self_.customfilter.sampleUids.length > 0) {
          _hasFilters = true;
          _selectedCasesByFilters = (updateType_ === 'patient') ?
            self_.customfilter.patientUids : self_.customfilter.sampleUids;
        }
        _.each(self_.groups, function(group) {
          _.each(group.attributes, function(attributes) {
            if (attributes.show) {
              if (attributes.filter.length > 0) {
                _hasFilters = true;
              }
            }
          });
          if (group.type === updateType_) {
            var _groupFilteredCases =
              iViz.getGroupFilteredCases(group.id) === undefined ?
                [] : iViz.getGroupFilteredCases(group.id).cases;
            if (_groupFilteredCases.length > 0) {
              if (_selectedCasesByFilters.length === 0) {
                _selectedCasesByFilters = _groupFilteredCases;
              } else {
                _selectedCasesByFilters =
                  iViz.util.intersection(_selectedCasesByFilters,
                    _groupFilteredCases);
              }
            }
          }
        });

        if (_selectedCasesByFilters.length === 0) {
          _selectedCasesByFilters = (updateType_ === 'patient') ?
            self_.completePatientsList : self_.completeSamplesList;
        }
        self_.hasfilters = _hasFilters;

        _selectedCasesByFilters = _selectedCasesByFilters.sort();

        if (updateType_ === 'patient') {
          self_.selectedPatientsByFilters = _selectedCasesByFilters;
          // _selectedCasesByFilters = _selectedCasesByFilters.length === 0 ?
          //   self_.completePatientsList : _selectedCasesByFilters;
          _counterSelectedCasesByFilters =
            this.selectedSamplesByFilters.length === 0 ?
              self_.completeSamplesList : this.selectedSamplesByFilters;
        } else {
          self_.selectedSamplesByFilters = _selectedCasesByFilters;
          // _selectedCasesByFilters = _selectedCasesByFilters.length === 0 ?
          //   self_.completeSamplesList : _selectedCasesByFilters;
          _counterSelectedCasesByFilters =
            this.selectedPatientsByFilters.length === 0 ?
              self_.completePatientsList : this.selectedPatientsByFilters;
        }

        var _mappedCounterSelectedCases =
          iViz.util.idMapping(iViz.getCasesMap(_caseType),
            _selectedCasesByFilters);
        _mappedCounterSelectedCases.sort();
        var _resultCounterSelectedCases =
          iViz.util.intersection(_mappedCounterSelectedCases,
            _counterSelectedCasesByFilters);
        var _resultSelectedCases =
          iViz.util.idMapping(iViz.getCasesMap(_counterCaseType),
            _resultCounterSelectedCases).sort();
        var _casesSync = iViz.util.idMapping(iViz.getCasesMap(_counterCaseType),
          _counterSelectedCasesByFilters);
        var _counterCasesSync = _mappedCounterSelectedCases;

        if (updateType_ === 'patient') {
          self_.patientsync = _casesSync;
          self_.samplesync = _counterCasesSync;
          if (self_.hasfilters) {
            self_.selectedsampleUIDs = _resultCounterSelectedCases;
            self_.selectedpatientUIDs = iViz.util.intersection(_selectedCasesByFilters, _resultSelectedCases);
          } else {
            self_.selectedsampleUIDs = self_.completeSamplesList;
            self_.selectedpatientUIDs = self_.completePatientsList;
          }
        } else {
          self_.samplesync = _casesSync;
          self_.patientsync = _counterCasesSync;
          if (self_.hasfilters) {
            self_.selectedsampleUIDs = iViz.util.intersection(_selectedCasesByFilters, _resultSelectedCases);
            self_.selectedpatientUIDs = _resultCounterSelectedCases;
          } else {
            self_.selectedsampleUIDs = self_.completeSamplesList;
            self_.selectedpatientUIDs = self_.completePatientsList;
          }
        }
      },
      'update-custom-filters': function() {
        if (this.customfilter.type === 'patient') {
          this.patientsync = this.customfilter.patientUids;
          this.samplesync = iViz.util.idMapping(iViz.getCasesMap('patient'),
            this.patientsync);
          this.customfilter.sampleUids = this.samplesync;
        } else {
          this.patientsync = iViz.util.idMapping(iViz.getCasesMap('sample'),
            this.customfilter.sampleUids);
          this.samplesync = this.customfilter.sampleUids;
          this.customfilter.patientUids = this.patientsync;
        }

        this.selectedsampleUIDs = this.samplesync;
        this.selectedpatientUIDs = this.patientsync;
      },
      'create-rainbow-survival': function(opts) {
        this.$broadcast('create-rainbow-survival', opts);
        this.$broadcast('resetBarColor', [opts.attrId]);
      },
      'remove-rainbow-survival': function() {
        this.$broadcast('resetBarColor', []);
      }
    },
    ready: function() {
      var self_ = this;
      // Register window resize event.
      $(window).resize(function() {
        clearTimeout(self_.windowResizeTimeout);
        self_.windowResizeTimeout = setTimeout(function() {
          if (!self_.$root.userMovedChart) {
            self_.updateLayout();
          }
        }, 500);
      });
    }
  });
})(window.Vue,
  window.dc,
  window.iViz,
  window.Packery,
  window.Draggabilly,
  window._);

/**
 * Created by Karthik Kalletla on 4/6/16.
 */
'use strict';
(function(Vue, dc, iViz, crossfilter, _) {
  Vue.component('chartGroup', {
    template: ' <div is="individual-chart" ' +
    ':clear-chart="clearGroup" :ndx="ndx"   :attributes.sync="attribute" ' +
    ':showed-survival-plot="showedSurvivalPlot"  v-for="attribute in attributes"></div>',
    props: [
      'attributes', 'type', 'id', 'redrawgroups', 'mappedcases', 'clearGroup', 'hasfilters', 'showedSurvivalPlot'
    ], created: function() {
      // TODO: update this.data
      var _self = this;
      var ndx_ = crossfilter(iViz.getGroupNdx(this.id));
      this.invisibleBridgeDimension = ndx_.dimension(function(d) {
        return d[_self.type + '_uid'];
      });
      this.ndx = ndx_;
      this.invisibleChartFilters = [];

      if (this.mappedcases !== undefined && this.mappedcases.length > 0) {
        this.$nextTick(function() {
          _self.updateInvisibleChart(_self.mappedcases);
        });
      }
    }, destroyed: function() {
      dc.chartRegistry.clear(this.groupid);
    },
    data: function() {
      return {
        syncCases: true
      };
    },
    watch: {
      mappedcases: function(val) {
        if (this.syncCases) {
          this.updateInvisibleChart(val);
        } else {
          this.syncCases = true;
        }
      },
      clearGroup: function(flag) {
        if (flag) {
          var self_ = this;
          self_.invisibleBridgeDimension.filterAll();
          self_.invisibleChartFilters = [];
          iViz.deleteGroupFilteredCases(self_.id);
        }
      }
    },
    events: {
      'add-chart-to-group': function(groupId) {
        if (this.id === groupId) {
          this.$broadcast('addingChart', this.id, true);
          if (this.invisibleChartFilters.length > 0) {
            this.invisibleBridgeDimension.filterAll();
          }
          this.ndx.remove();
          this.ndx.add(iViz.getGroupNdx(this.id));
          if (this.invisibleChartFilters.length > 0) {
            var filtersMap = {};
            _.each(this.invisibleChartFilters, function(filter) {
              if (filtersMap[filter] === undefined) {
                filtersMap[filter] = true;
              }
            });
            this.invisibleBridgeDimension.filterFunction(function(d) {
              return (filtersMap[d] !== undefined);
            });
          }
          this.$broadcast('addingChart', this.id, false);
        }
      },
      /*
       *This event is invoked whenever there is a filter update on any chart
       * STEPS involved
       *
       * 1. Clear filters on invisible group bridge chart
       * 2. Get all the filtered cases fot that particular chart group
       * 3. If those filtered cases length not same as original cases length
       *    then save that case list in the groupFilterMap
       * 4. Apply back invisible group bridge chart filters
       */
      'update-filters': function(redrawGroup) {
        if (!this.clearGroup) {
          if (redrawGroup) {
            dc.redrawAll(this.id);
          }
          this.syncCases = false;
          if (this.invisibleChartFilters.length > 0) {
            this.invisibleBridgeDimension.filterAll();
          }
          var filteredCases = _.pluck(
            this.invisibleBridgeDimension.top(Infinity),
            this.type + '_uid').sort();
          // Hacked way to check if filter selected filter cases is same
          // as original case list

          var _hasFilter = false;
          _.every(this.attributes, function(attribute) {
            if (attribute.filter.length > 0) {
              _hasFilter = true;
              return false;
            }
            return true;
          });
          if (_hasFilter) {
            iViz.setGroupFilteredCases(this.id, this.type, filteredCases);
          } else {
            iViz.deleteGroupFilteredCases(this.id);
          }

          if (this.invisibleChartFilters.length > 0) {
            var filtersMap = {};
            _.each(this.invisibleChartFilters, function(filter) {
              if (filtersMap[filter] === undefined) {
                filtersMap[filter] = true;
              }
            });
            this.invisibleBridgeDimension.filterFunction(function(d) {
              return (filtersMap[d] !== undefined);
            });
          }
          this.$dispatch('update-all-filters', this.type);
        }
      }
    },
    methods: {
      updateInvisibleChart: function(val) {
        var _groupCases = iViz.getGroupFilteredCases();
        var _selectedCases = val;
        var _self = this;
        _.each(_groupCases, function(_group, id) {
          if (_group !== undefined && _group.type === _self.type &&
            (_self.id.toString() !== id)) {
            _selectedCases =
              iViz.util.intersection(_selectedCases, _group.cases);
          }
        });
        this.invisibleChartFilters = [];
        this.invisibleBridgeDimension.filterAll();
        if (this.hasfilters) {
          this.invisibleChartFilters = _selectedCases;
          var filtersMap = {};
          _.each(_selectedCases, function(filter) {
            if (filtersMap[filter] === undefined) {
              filtersMap[filter] = true;
            }
          });
          this.invisibleBridgeDimension.filterFunction(function(d) {
            return (filtersMap[d] !== undefined);
          });
        }
        this.redrawgroups.push(this.id);
      }
    }
  });
})(
  window.Vue,
  window.dc,
  window.iViz,
  window.crossfilter,
  window._
);

/**
 * Created by Karthik Kalletla on 4/6/16.
 */
'use strict';
(function(Vue) {
  Vue.component('individualChart', {
    template: '<component :is="currentView" v-if="attributes.show" :clear-chart="clearChart" :ndx="ndx" ' +
    ':attributes.sync="attributes" :clear-chart="clearChart" :showed-survival-plot="showedSurvivalPlot"></component>',
    props: [
      'ndx', 'attributes', 'clearChart', 'showedSurvivalPlot'
    ],
    data: function() {
      var currentView = '';
      this.attributes.filter = [];
      switch (this.attributes.view_type) {
        case 'pie_chart':
          currentView = 'pie-chart';
          break;
        case 'bar_chart':
          currentView = 'bar-chart';
          break;
        case 'scatter_plot':
          currentView = 'scatter-plot';
          break;
        case 'survival':
          currentView = 'survival';
          break;
        case 'table':
          currentView = 'table-view';
          break;
        default:
          currentView = 'pie-chart';
          break;
      }
      return {
        currentView: currentView
      };
    },
    watch: {
      clearChart: function(val) {
        if (val && this.attributes.filter.length > 0) {
          this.attributes.filter = [];
        }
      }
    },
    events: {
      close: function() {
        this.attributes.show = false;
        this.$dispatch('remove-chart',
          this.attributes.attr_id, this.attributes.group_id);
      }
    }
  });
})(window.Vue);

/**
 * @author Hongxin Zhang on 6/21/16.
 */
(function(iViz, _) {
  // iViz pie chart component. It includes DC pie chart.
  iViz.view.component.GeneralChart = function(chartType) {
    'use strict';
    this.chartType = chartType || 'generalChart';
    this.dataForDownload = {};
    this.getCurrentCategories = function() {
      return [];
    };
    this.getChartType = function() {
      return this.chartType;
    };
    this.setChartType = function(chartType) {
      this.chartType = chartType;
    };
    this.getDownloadData = function(fileType) {
      if (_.isFunction(this.updateDataForDownload)) {
        this.updateDataForDownload(fileType);
      }
      return this.dataForDownload[fileType];
    };
    this.setDownloadData = function(type, content) {
      this.dataForDownload[type] = content;
    };
    this.getDownloadFileTypes = function() {
      return Object.keys(this.dataForDownload);
    };
    this.setDownloadDataTypes = function(types) {
      var _self = this;
      _.each(types, function(type) {
        if (!_self.dataForDownload.hasOwnProperty(type)) {
          _self.dataForDownload[type] = '';
        }
      });
    };
  };
})(window.iViz, window._);

/**
 * Created by Karthik Kalletla on 4/14/16.
 */
'use strict';
(function(Vue, iViz, dc, $, _) {
  Vue.component('chartOperations', {
    template: '<div class="chart-header" id="{{chartId}}-chart-header">' +
    '<div class="chart-title" ' +
    ':class="[showOperations?chartTitleActive:chartTitle]" ' +
    'v-if="hasChartTitle">' +
    '<span class="chart-title-span" id="{{chartId}}-title">{{displayName}}' +
    '</span></div>' +
    '<div :class="[showOperations?chartOperationsActive:chartOperations]">' +
    '<div class="checkbox-div" v-if="showLogScale && chartInitialed"">' +
    '<input type="checkbox" value="" id="" ' +
    'class="checkbox" v-model="logChecked">' +
    '<span id="scale-span-{{chartId}}">' +
    'Log Scale X</span></div>' +
    '<i v-show="hasFilters" class="fa fa-undo icon hover" ' +
    'aria-hidden="true" @click="reset()"></i>' +
    '<i v-if="hasTitleTooltip()" ' +
    'class="fa fa-info-circle icon hover" ' +
    'id="{{chartId}}-description-icon"' +
    'aria-hidden="true"></i>' +
    '<i v-if="showTableIcon && chartInitialed" class="fa fa-table icon hover" ' +
    'aria-hidden="true" @click="changeView()" alt="Convert pie chart to table"></i>' +
    '<i v-if="showPieIcon && chartInitialed"" class="fa fa-pie-chart icon hover" ' +
    'aria-hidden="true" @click="changeView()" alt="Convert table to pie chart"></i>' +
    '<div class="dc-survival-icon" style="float: left;"><img v-if="showSurvivalIcon && chartInitialed" src="images/survival_icon.svg" ' +
    'class="icon hover" @click="getRainbowSurvival" alt="Survival Analysis"/></div>' +
    '<div v-if="showDownloadIcon && chartInitialed"" id="{{chartId}}-download-icon-wrapper" class="download">' +
    '<i class="fa fa-download icon hover" alt="download" ' +
    'id="{{chartId}}-download"></i>' +
    '</div>' +
    '<i class="fa fa-arrows dc-chart-drag icon" aria-hidden="true" alt="Move chart"></i>' +
    '<div style="float:right"><i class="fa fa-times dc-remove-chart-icon icon" ' +
    '@click="close()" alt="Delete chart"></i></div>' +
    '</div>' +
    '</div>',
    props: {
      showOperations: {
        type: Boolean,
        default: true
      }, resetBtnId: {
        type: String
      }, chartCtrl: {
        type: Object
      }, groupid: {
        type: String
      }, hasChartTitle: {
        type: Boolean,
        default: false
      }, showTable: {
        type: Boolean
      }, displayName: {
        type: String
      }, chartId: {
        type: String
      }, showPieIcon: {
        type: Boolean
      }, showTableIcon: {
        type: Boolean
      }, showLogScale: {
        type: Boolean,
        default: false
      }, showSurvivalIcon: {
        type: Boolean,
        default: false
      }, filters: {
        type: Array
      }, attributes: {
        type: Object
      }, showDownloadIcon: {
        type: Boolean,
        default: true
      }, chartInitialed: {
        type: Boolean,
        default: true
      }
    },
    data: function() {
      return {
        chartOperationsActive: 'chart-operations-active',
        chartOperations: 'chart-operations',
        chartTitle: 'chart-title',
        chartTitleActive: 'chart-title-active chart-title-active-' + 3,
        logChecked: true,
        hasFilters: false,
        titleIconQtipOpts: {
          style: {classes: 'qtip-light qtip-rounded qtip-shadow'},
          show: {event: 'mouseover', delay: 0},
          hide: {fixed: true, delay: 300, event: 'mouseout'},
          position: {my: 'bottom left', at: 'top right', viewport: $(window)},
          content: {}
        },
        titleTooltip: {
          content: _.isObject(this.attributes) ?
            iViz.util.getClinicalAttrTooltipContent(this.attributes) : ''
        },
        numOfIcons: 3
      };
    },
    watch: {
      logChecked: function(newVal) {
        this.reset();
        this.$dispatch('changeLogScale', newVal);
      },
      filters: function(newVal) {
        this.hasFilters = newVal.length > 0;
      },
      showSurvivalIcon: function(newVal) {
        if (newVal) {
          this.numOfIcons++;
        } else {
          this.numOfIcons--;
        }
      },
      showDownloadIcon: function(newVal) {
        if (newVal) {
          this.numOfIcons++;
        } else {
          this.numOfIcons--;
        }
      },
      numOfIcons: function(newVal) {
        this.chartTitleActive = 'chart-title-active chart-title-active-' + newVal;
      }
    },
    methods: {
      reset: function() {
        if (this.filters.length > 0) {
          this.filters = [];
        }
      },
      close: function() {
        if (this.filters && this.filters.length > 0) {
          this.filters = [];
        }
        var self_ = this;
        self_.$nextTick(function() {
          self_.$dispatch('closeChart');
        });
      },
      updateChartTypeIconTooltip: function() {
        $('#' + this.chartId + '-chart-header .fa-table').qtip('destroy', true);
        $('#' + this.chartId + '-chart-header .fa-pie-chart').qtip('destroy', true);

        if (this.showTableIcon) {
          this.$nextTick(function() {
            $('#' + this.chartId + '-chart-header .fa-table').qtip($.extend(true, this.titleIconQtipOpts, {
              content: {
                text: 'Convert pie chart to table'
              }
            }));
          });
        }

        if (this.showPieIcon) {
          this.$nextTick(function() {
            $('#' + this.chartId + '-chart-header .fa-pie-chart').qtip($.extend(true, this.titleIconQtipOpts, {
              content: {
                text: 'Convert table to pie chart'
              }
            }));
          });
        }
      },
      changeView: function() {
        this.showTableIcon = !this.showTableIcon;
        this.showPieIcon = !this.showPieIcon;
        this.updateChartTypeIconTooltip();
        this.$dispatch('toTableView');
      },
      getRainbowSurvival: function() {
        this.$dispatch('getRainbowSurvival');
      },
      hasTitleTooltip: function() {
        return _.isObject(this.attributes) ?
          (['survival'].indexOf(this.attributes.view_type) === -1 &&
            _.isObject(this.titleTooltip) && this.titleTooltip.content) : false;
      }
    },
    ready: function() {
      $('#' + this.chartId + '-download').qtip('destroy', true);
      $('#' + this.chartId + '-download-icon-wrapper').qtip('destroy', true);
      $('#' + this.chartId + '-title').qtip('destroy', true);
      var chartId = this.chartId;
      var self = this;

      if (this.hasTitleTooltip()) {
        var target = ['#' + this.chartId + '-description-icon'];
        if (this.hasChartTitle) {
          target.push('#' + this.chartId + '-title');
        }
        $(target).qtip({
          id: this.chartId + '-title-qtip',
          content: {
            text: this.titleTooltip.content
          },
          style: {classes: 'qtip-light qtip-rounded qtip-shadow'},
          show: {event: 'mouseover'},
          hide: {fixed: true, delay: 100, event: 'mouseout'},
          position: {my: 'right bottom', at: 'top left', viewport: $(window)}
        });
      }

      $('#' + this.chartId + '-download-icon-wrapper').qtip($.extend(true, this.titleIconQtipOpts, {
        content: {
          text: 'Download'
        }
      }));

      $('#' + this.chartId + '-chart-header .dc-chart-drag').qtip($.extend(true, this.titleIconQtipOpts, {
        content: {
          text: 'Move chart'
        }
      }));

      $('#' + this.chartId + '-chart-header .dc-remove-chart-icon').qtip($.extend(true, this.titleIconQtipOpts, {
        content: {
          text: 'Delete chart'
        }
      }));

      $('#' + this.chartId + '-chart-header .dc-survival-icon').qtip($.extend(true, this.titleIconQtipOpts, {
        content: {
          text: 'Survival Analysis'
        }
      }));

      this.updateChartTypeIconTooltip();

      $('#' + this.chartId + '-download').qtip({
        id: '#' + this.chartId + '-download-qtip',
        style: {classes: 'qtip-light qtip-rounded qtip-shadow'},
        show: {event: 'click', delay: 0},
        hide: {fixed: true, delay: 300, event: 'mouseout'},
        position: {my: 'top center', at: 'bottom center', viewport: $(window)},
        content: {
          text: ''
        }, events: {
          show: function() {
            $('#' + chartId + '-download-icon-wrapper').qtip('api').hide();
          },
          render: function(event, api) {
            var downloadFileTypes = self.chartCtrl.getDownloadFileTypes().sort(function(a, b) {
              a = a === 'tsv' ? 'data' : a;
              b = b === 'tsv' ? 'data' : b;
              return a > b;
            });
            var content = [];
            _.each(downloadFileTypes, function(item) {
              content.push('<div style="display:inline-block;"><button id="' + self.chartId + '-' + item + '" style="width:50px">' + (item === 'tsv' ? 'DATA' : item.toUpperCase()) + '</button></div>');
            });

            api.set('content.text', content.join('<br/>'));
            $('#' + chartId + '-pdf', api.elements.tooltip).click(function() {
              iViz.util.download(self.chartCtrl.getChartType(), 'pdf', self.chartCtrl.getDownloadData('pdf'));
            });
            $('#' + chartId + '-svg', api.elements.tooltip).click(function() {
              iViz.util.download(self.chartCtrl.getChartType(), 'svg', self.chartCtrl.getDownloadData('svg'));
            });
            $('#' + chartId + '-tsv').click(function() {
              iViz.util.download(self.chartCtrl.getChartType(), 'tsv', self.chartCtrl.getDownloadData('tsv'));
            });
          }
        }
      });

      var _numOfIcons = this.numOfIcons;

      if (self.showPieIcon) {
        _numOfIcons++;
      }

      if (self.showTableIcon) {
        _numOfIcons++;
      }

      if (self.showSurvivalIcon) {
        _numOfIcons++;
      }

      if (self.hasTitleTooltip()) {
        _numOfIcons++;
      }

      if (self.showLogScale) {
        _numOfIcons++;
      }

      if (self.showDownloadIcon) {
        _numOfIcons++;
      }

      if (self.attributes.view_type
        && self.attributes.view_type === 'survival') {
        _numOfIcons += 5;
      }

      this.numOfIcons = _numOfIcons;
    }
  });
})(window.Vue,
  window.iViz,
  window.dc,
  window.$ || window.jQuery,
  window._);

/**
 * Created by Karthik Kalletla on 4/20/16.
 */
'use strict';
(function(Vue, iViz, $) {
  Vue.component('manageCharts', {
    template: '<option id="{{data.attr_id}}" v-if="!data.show" ' +
    'value="{{data.attr_id}}">{{data.display_name}}</option>',
    props: [
      'data'
    ], ready: function() {
      $('#iviz-add-chart').trigger('chosen:updated');
    }
  });
})(
  window.Vue,
  window.iViz,
  window.$ || window.jQuery
);

/**
 * Created by Karthik Kalletla on 4/18/16.
 */
'use strict';
(function(Vue, iViz) {
  Vue.component('breadCrumb', {
    template: '<span class="breadcrumb_container" ' +
    'v-if="attributes.filter.length > 0">' +
    '<span>{{attributes.display_name}}</span><span ' +
    'v-if="(filtersToSkipShowing.indexOf(attributes.attr_id) === -1) && ' +
    '(specialTables.indexOf(attributes.attr_id) === -1)" class="breadcrumb_items">' +
    '<span v-if="attributes.view_type===\'bar_chart\'">' +
    '<span class="breadcrumb_item">{{getBarChartFilterString(filters)}}</span>' +
    '<i class="fa fa-times breadcrumb_remove" @click="removeFilter()"></i>' +
    '</span>' +
    '<template v-else>' +
    '<span v-for="filter in filters" style="display:inline-block;">' +
    '<span class="breadcrumb_item">{{filter}}</span>' +
    '<i class="fa fa-times breadcrumb_remove" ' +
    '@click="removeFilter(filter)"></i></span></template></span>' +
    '<template v-else>' +
    '<i class="fa fa-times breadcrumb_remove" @click="removeFilter()"></i>' +
    '</template></span>',
    props: [
      'filters', 'attributes'
    ], data: function() {
      return {
        filtersToSkipShowing: ['MUT_CNT_VS_CNA', 'sample_uid', 'patient_uid'],
        specialTables: ['mutated_genes', 'cna_details']
      };
    },
    methods: {
      getBarChartFilterString: function(filters) {
        return iViz.util.getDisplayBarChartBreadCrumb(filters);
      },
      removeFilter: function(val) {
        if (this.attributes.view_type === 'bar_chart') {
          this.filters = [];
        } else if (this.attributes.view_type === 'pie_chart') {
          if (
            this.filtersToSkipShowing.indexOf(this.attributes.attr_id) === -1) {
            this.filters.$remove(val);
          } else {
            this.filters = [];
          }
        } else if (this.attributes.view_type === 'scatter_plot') {
          this.filters = [];
        } else if (this.attributes.view_type === 'table') {
          if (this.specialTables.indexOf(this.attributes.attr_id) === -1) {
            this.filters.$remove(val);
          } else {
            this.filters = [];
          }
        }
      }
    }
  });
})(window.Vue, window.iViz);

/**
 * @author Hongxin Zhang on 3/10/16.
 */

'use strict';
(function(iViz, d3, dc, _, $, React, ReactDOM, EnhancedFixedDataTableSpecial) {
  // iViz pie chart component. It includes DC pie chart.
  iViz.view.component.PieChart = function(ndx, attributes, opts, cluster) {
    var content = this;
    var v = {};

    v.chart = '';

    v.data = {
      color: $.extend(true, [], iViz.util.getColors()),
      category: ''
    };

    /* HTML options*/
    v.opts = {};

    v.opts = $.extend(true, v.opts, opts);
    v.data = $.extend(true, v.data, attributes);
    v.data.ndx = ndx;

    var labels = {};
    var reactTableData = {};
    reactTableData.attributes = [{
      attr_id: 'name',
      display_name: v.data.display_name,
      datatype: 'STRING',
      column_width: 235
    }, {
      attr_id: 'color',
      display_name: 'Color',
      datatype: 'STRING',
      show: false
    }, {
      attr_id: 'cases',
      display_name: '#',
      datatype: 'NUMBER',
      column_width: 70
    }, {
      attr_id: 'sampleRate',
      display_name: 'Freq',
      datatype: 'PERCENTAGE',
      column_width: 90
    }, {
      attr_id: 'caseIds',
      display_name: 'Cases',
      datatype: 'STRING',
      show: false
    }, {
      attr_id: 'uniqueId',
      display_name: 'uniqueId',
      datatype: 'STRING',
      show: false
    }];
    var currentView = 'pie';
    var updateQtip = false;
    var qtipRendered = false;
    var isFiltered = false;

    /**
     * Only will be initialized at first time. Label name as key, contains color.
     * @type {{}}
     */
    var labelInitData = {};

    var dcGroup_ = '';
    var dcDimension_ = '';

    initDCPieChart();

    content.getChart = function() {
      return v.chart;
    };

    content.changeView = function(vm, toTableView) {
      currentView = toTableView ? 'table' : 'pie';
      var chartDivDom = $('#' + v.opts.chartDivId);
      chartDivDom.css('z-index', 16000);

      // qtip wont be needed in table view
      chartDivDom.qtip('destroy', true);

      if (currentView === 'table') {
        if (qtipRendered) {
          updateReactTable();
        } else {
          updatePieLabels();
        }
        animateTable('#' + v.opts.chartDivId, 'table', function() {
          vm.$dispatch('update-grid');
          $('#' + v.opts.chartDivId).css('z-index', '');
        });
      } else {
        animateTable('#' + v.opts.chartDivId, 'pie', function() {
          vm.$dispatch('update-grid');
          $('#' + v.opts.chartDivId).css('z-index', '1');
        });
        content.initMainDivQtip();
      }
    };

    content.initMainDivQtip = function() {
      $('#' + v.opts.chartDivId).qtip({
        id: v.opts.chartDivId + '-qtip',
        style: {
          classes: 'qtip-light qtip-rounded qtip-shadow forceZindex qtip-max-width iviz-pie-qtip iviz-pie-label-qtip'
        },
        show: {event: 'mouseover', delay: 300, ready: true},
        hide: {fixed: true, delay: 300, event: 'mouseleave'},
        // hide: false,
        position: {my: 'left center', at: 'center right', viewport: $(window)},
        content: '<div id="qtip-' + v.opts.chartDivId + '-content-react">Loading....</div>',
        events: {
          show: function() {
            if (updateQtip) {
              updateQtip = false;
              updatePieLabels();
            }
          },
          render: function() {
            qtipRendered = true;
            updatePieLabels();
          }
        }
      });
    };

    content.updateDataForDownload = function(fileType) {
      if (fileType === 'tsv') {
        initTsvDownloadData();
      } else if (['pdf', 'svg'].indexOf(fileType) !== -1) {
        initCanvasDownloadData();
      }
    };

    content.filtered = function(filters) {
      updateTables();
      isFiltered = _.isArray(filters) && filters.length > 0;
    };

    content.getCurrentCategories = function(sortBy) {
      var categories = [];

      sortBy = sortBy || 'value';
      _.each(_.sortBy(dcGroup_.top(Infinity), sortBy), function(label) {
        var _labelDatum = {};
        var _labelValue = Number(label.value);
        if (_labelValue > 0) {
          _labelDatum.id = labelInitData[label.key].id;
          _labelDatum.index = labelInitData[label.key].index;
          _labelDatum.name = label.key;
          _labelDatum.color = labelInitData[label.key].color;
          _labelDatum.cases = _labelValue;
          categories.push(_labelDatum);
        }
      });
      return categories;
    };

    function getCurrentSampleSizeFromCategories(categories) {
      var currentSampleSize = 0;
      for (var key in categories) {
        if (categories.hasOwnProperty(key)) {
          currentSampleSize += categories[key].cases;
        }
      }
      return currentSampleSize;
    }

    /**
     * This is the function to initialize dc pie chart instance.
     */
    function initDCPieChart() {
      if (v.opts.hasOwnProperty('chartId') &&
        v.data.hasOwnProperty('ndx') &&
        v.data.hasOwnProperty('attr_id')) {
        var width = v.opts.width || 130;
        var height = v.opts.height;
        var radius = (width - 20) / 2;
        var color = $.extend(true, [], v.data.color);

        v.chart = dc.pieChart('#' + v.opts.chartId, v.opts.groupid);

        v.data.attrKeys = cluster.group().all().map(function(d) {
          return d.key;
        });

        v.data.category = iViz.util.pieChart.getCategory(v.data.attr_id,
          v.data.attrKeys);

        v.data.attrKeys.sort(function(a, b) {
          return a < b ? -1 : 1;
        });

        var NAIndex = v.data.attrKeys.indexOf('NA');
        if (NAIndex !== -1) {
          color.splice(NAIndex, 0, '#CCCCCC');
        }

        // Initial labels data with colors.
        _.each(v.data.attrKeys, function(attr, index) {
          labelInitData[attr] = {
            attr: attr,
            color: color[index],
            id: attr,
            index: index
          };
        });

        dcDimension_ = cluster;
        dcGroup_ = cluster.group();

        v.chart
          .width(width)
          .height(height)
          .radius(radius)
          .dimension(dcDimension_)
          .group(dcGroup_)
          .transitionDuration(v.opts.transitionDuration || 400)
          .ordinalColors(color)
          .label(function(d) {
            return d.value;
          })
          .ordering(function(d) {
            return d.key;
          });
        v.chart.on('preRedraw', function() {
          removeMarker();
        });
        v.chart.on('postRedraw', function() {
          // TODO:commented this because this is taking much time to redraw
          // after applying filter, need to find different way
          if (isFiltered) {
            updateQtip = false;
            isFiltered = false;
          } else {
            updateQtip = true;
            if (currentView === 'table') {
              updatePieLabels();
            }
          }
        });
      } else {
        // TODO:
        /**
         * Need a handler if no dimension ID passed.
         */
      }
    }

    function initTsvDownloadData() {
      var data = [v.data.display_name + '\tCount'];

      _.each(labels, function(label, key) {
        data.push(label.name + '\t' + label.cases);
      });

      content.setDownloadData('tsv', {
        fileName: v.data.display_name || 'Pie Chart',
        data: data.join('\n')
      });
    }

    function initCanvasDownloadData() {
      content.setDownloadData('svg', {
        title: v.data.display_name,
        chartDivId: v.opts.chartDivId,
        chartId: v.opts.chartId,
        fileName: v.data.display_name,
        labels: labels
      });
      content.setDownloadData('pdf', {
        title: v.data.display_name,
        chartDivId: v.opts.chartDivId,
        chartId: v.opts.chartId,
        fileName: v.data.display_name,
        labels: labels
      });
    }

    function animateTable(target, view, callback) {
      var width = window.iViz.styles.vars.width.one;
      var height = window.iViz.styles.vars.height.one;

      if (view === 'table') {
        width = window.iViz.styles.vars.width.two;
        height = window.iViz.styles.vars.height.two;
        if (Object.keys(labels).length <= 3) {
          height = window.iViz.styles.vars.height.one;
        }
      }

      $(target).animate({
        height: height,
        width: width,
        duration: 300,
        queue: false
      }, 300, function() {
        if (_.isFunction(callback)) {
          callback();
        }
      });
    }

    function updatePieLabels() {
      updateCurrentLabels();
      initReactData();
      updateTables();
    }

    function updateTables() {
      if (currentView === 'pie' && qtipRendered) {
        updateQtipReactTable();
      }
      if (currentView === 'table') {
        updateReactTable();
      }
    }

    function updateReactTable() {
      var data = $.extend(true, {}, reactTableData);
      initReactTable(v.opts.chartTableId, data, {
        tableWidth: window.iViz.styles.vars.specialTables.width
      });
    }

    function updateQtipReactTable() {
      var data = $.extend(true, {}, reactTableData);
      data.attributes[0].column_width = 140;
      initReactTable('qtip-' + v.opts.chartDivId + '-content-react', data, {
        tableWidth: 300,
        pieLabelMouseEnterFunc: pieLabelMouseEnter,
        pieLabelMouseLeaveFunc: pieLabelMouseLeave
      });
    }

    function updateCurrentLabels() {
      var _labels = content.getCurrentCategories();
      var _currentSampleSize = getCurrentSampleSizeFromCategories(_labels);

      labels = {};
      _.each(_labels, function(label) {
        label.sampleRate = (_currentSampleSize <= 0 ? 0 : (Number(label.cases) * 100 / _currentSampleSize).toFixed(1).toString()) + '%';
        labels[label.id] = label;
      });
    }

    function initReactData() {
      var _data = [];
      _.each(labels, function(item) {
        for (var key in item) {
          if (item.hasOwnProperty(key)) {
            var datum = {
              attr_id: key,
              uniqueId: item.id,
              attr_val: item[key]
            };
            _data.push(datum);
          }
        }
      });
      reactTableData.data = _data;
    }

    function removeMarker() {
      $('#' + v.opts.chartId).find('svg g .mark').remove();
    }

    function drawMarker(_slice) {
      var _path = $(_slice).find('path');
      var _pointsInfo = _path
        .attr('d')
        .split(/[\s,MLHVCSQTAZ]/);

      var _pointsInfo1 = _path
        .attr('d')
        .split(/[A]/);

      var _fill = _path
        .attr('fill');

      var _x1 = Number(_pointsInfo[1]);
      var _y1 = Number(_pointsInfo[2]);
      var _x2 = Number(_pointsInfo[8]);
      var _y2 = Number(_pointsInfo[9]);
      var _r = Number(_pointsInfo[3]);

      if ((_x1 - _x2 !== 0 || _y1 - _y2 !== 0) && _pointsInfo1.length === 2) {
        var _pointOne = Math.atan2(_y1, _x1);
        var _pointTwo = Math.atan2(_y2, _x2);

        if (_pointOne < -Math.PI / 2) {
          _pointOne = Math.PI / 2 + Math.PI * 2 + _pointOne;
        } else {
          _pointOne = Math.PI / 2 + _pointOne;
        }

        if (_pointTwo < -Math.PI / 2) {
          _pointTwo = Math.PI / 2 + Math.PI * 2 + _pointTwo;
        } else {
          _pointTwo = Math.PI / 2 + _pointTwo;
        }

        // The value of point two should always bigger than the value
        // of point one. If the point two close to 12 oclick, we should
        // change it value close to 2PI instead of close to 0
        if (_pointTwo > 0 && _pointTwo < 0.0000001) {
          _pointTwo = 2 * Math.PI - _pointTwo;
        }

        if (_pointTwo < _pointOne) {
          console.log('%cError: the end angle should always bigger' +
            ' than start angle.', 'color: red');
        }

        var _arc = d3.svg.arc()
          .innerRadius(_r + 3)
          .outerRadius(_r + 5)
          .startAngle(_pointOne)
          .endAngle(_pointTwo);

        d3.select('#' + v.opts.chartId + ' svg g').append('path')
          .attr('d', _arc)
          .attr('fill', _fill)
          .attr('class', 'mark');
      }
    }

    function pieLabelMouseEnter(data) {
      var _slice = getPieSlice(data);
      
      $(_slice).css({
        'fill-opacity': '.5',
        'stroke-width': '3'
      });

      drawMarker(_slice);
    }

    function pieLabelMouseLeave(data) {
      var _slice = getPieSlice(data);

      $(_slice).css({
        'fill-opacity': '1',
        'stroke-width': '1px'
      });
      
      removeMarker();
    }

    function getPieSlice(data) {
      var _color = data.color;
      var _slice;

      $('#' + v.opts.chartId + ' svg g.pie-slice').each(function(index, item) {
        var _sliceColor = $(item).find('path').attr('fill');
        if (_sliceColor === _color) {
          _slice = item;
          $(item).css({
            'fill-opacity': '1',
            'stroke-width': '1px'
          });
        }
      });
      return _slice;
    }

    function initReactTable(targetId, inputData, opts) {
      var selectedRows = v.chart.filters();

      var opts_ = $.extend({
        input: inputData,
        filter: 'ALL',
        download: 'NONE',
        downloadFileName: 'data.txt',
        showHide: false,
        hideFilter: true,
        scroller: true,
        resultInfo: false,
        groupHeader: false,
        fixedChoose: false,
        uniqueId: 'uniqueId',
        rowHeight: 25,
        tableWidth: 373,
        maxHeight: 290,
        headerHeight: 26,
        groupHeaderHeight: 40,
        autoColumnWidth: false,
        columnMaxWidth: 300,
        columnSorting: false,
        tableType: 'pieLabel',
        elementId: targetId + '-table',
        selectedRows: selectedRows,
        rowClickFunc: pieLabelClick
      }, opts);

      // Check whether the react table has been initialized
      if (v.renderedReactTable) {
        // Get sort settings from the initialized react table
        var sort_ = v.renderedReactTable.getCurrentSort();
        opts_ = $.extend(opts_, sort_);
      }

      var testElement = React.createElement(EnhancedFixedDataTableSpecial, opts_);

      v.renderedReactTable = ReactDOM.render(testElement, document.getElementById(targetId));
    }

    function pieLabelClick(selectedData) {
      v.chart.onClick({
        key: labels[selectedData.uniqueid].name,
        value: labels[selectedData.uniqueid].value
      });
    }
  };

  iViz.view.component.PieChart.prototype = new iViz.view.component.GeneralChart('pieChart');
  iViz.view.component.PieChart.constructor = iViz.view.component.PieChart;

  // Utils designed for pie chart.
  iViz.util.pieChart = (function() {
    var util = {};
    var v = {};

    v.category = ['w1', 'h1']; // Size class name for chart

    v.labelLT = 5; // Label length threshold
    v.labelHeaderLT = 4; // Label header length threshold

    // If the name lenght bigger the threshold, it will be truncated.
    v.labelWLT = 30; // Label length threshold for wider table
    v.labelHeaderWLT = 20; // Label header length threshold for wider table

    util.getCategory = function(attr, attrKeys) {
      var category = $.extend(true, {}, v.category);
      var maxAttrL = 0;

      _.each(attrKeys, function(key) {
        if (key.length > maxAttrL) {
          maxAttrL = key.length;
        }
      });

      category[0] = maxAttrL <= v.labelLT ? 'w1' : 'w2';

      // Default settings for special attribtues.
      if (['CANCER_TYPE', 'CANCER_TYPE_DETAILED'].indexOf(attr) !== -1) {
        category[0] = 'w2';
      }

      category[1] = attrKeys.length > 10 ? 'h2' : 'h1';

      return category;
    };

    return util;
  })();
})(window.iViz,
  window.d3,
  window.dc,
  window._,
  window.$ || window.jQuery,
  window.React,
  window.ReactDOM,
  window.EnhancedFixedDataTableSpecial
);
/**
 * Created by Karthik Kalletla on 4/6/16.
 */
'use strict';
(function(Vue, dc, iViz, $, _) {
  Vue.component('pieChart', {
    template: '<div id={{chartDivId}} ' +
    'class="grid-item grid-item-h-1 grid-item-w-1" ' +
    ':attribute-id="attributes.attr_id" ' +
    '@mouseenter="mouseEnter($event)" @mouseleave="mouseLeave($event)">' +
    '<chart-operations :has-chart-title="hasChartTitle" ' +
    ':display-name="displayName" :show-table-icon.sync="showTableIcon" ' +
    ' :show-pie-icon.sync="showPieIcon" :chart-id="chartId" ' +
    ':show-operations="showOperations" :groupid="attributes.group_id" ' +
    ':show-survival-icon.sync="showSurvivalIcon"' +
    ':reset-btn-id="resetBtnId" :chart-ctrl="piechart" ' +
    ' :filters.sync="attributes.filter" ' +
    ':attributes="attributes"></chart-operations>' +
    '<div class="dc-chart dc-pie-chart" ' +
    ':class="{view: showPieIcon}" align="center" style="float:none' +
    ' !important;" id={{chartId}} ></div>' +
    '<div id={{chartTableId}} :class="{view: showTableIcon}"></div>' +
    '</div>',
    props: [
      'ndx', 'attributes', 'showedSurvivalPlot'
    ],
    data: function() {
      return {
        v: {},
        chartDivId:
          iViz.util.getDefaultDomId('chartDivId', this.attributes.attr_id),
        resetBtnId:
          iViz.util.getDefaultDomId('resetBtnId', this.attributes.attr_id),
        chartId:
          iViz.util.getDefaultDomId('chartId', this.attributes.attr_id),
        chartTableId:
          iViz.util.getDefaultDomId('chartTableId', this.attributes.attr_id),
        displayName: this.attributes.display_name,
        chartInst: '',
        component: '',
        showOperations: false,
        cluster: '',
        piechart: {},
        hasChartTitle: true,
        showTableIcon: true,
        showPieIcon: false,
        filtersUpdated: false,
        addingChart: false,
        numOfSurvivalCurveLimit: iViz.opts.numOfSurvivalCurveLimit || 20,
        showSurvivalIcon: false
      };
    },
    watch: {
      'attributes.filter': function(newVal) {
        if (this.filtersUpdated) {
          this.filtersUpdated = false;
        } else {
          this.filtersUpdated = true;
          if (newVal.length === 0) {
            this.chartInst.filterAll();
          } else {
            this.chartInst.replaceFilter([newVal]);
          }
          this.$dispatch('update-filters', true);
        }
      },
      'showedSurvivalPlot': function() {
        this.updateShowSurvivalIcon();
      }
    },
    events: {
      toTableView: function() {
        this.piechart.changeView(this, !this.showTableIcon);
      },
      closeChart: function() {
        $('#' + this.chartDivId).qtip('destroy');
        dc.deregisterChart(this.chartInst, this.attributes.group_id);
        this.chartInst.dimension().dispose();
        this.$dispatch('close');
      },
      addingChart: function(groupId, val) {
        if (this.attributes.group_id === groupId) {
          if (this.attributes.filter.length > 0) {
            if (val) {
              this.addingChart = val;
              this.chartInst.filter(null);
            } else {
              this.chartInst.filter([this.attributes.filter]);
              this.addingChart = val;
            }
          }
        }
      },
      getRainbowSurvival: function() {
        var groups = [];
        var categories = this.piechart.getCurrentCategories('key');
        var dataForCategories = iViz.util.getCaseIdsGroupByCategories(
          this.attributes.group_type,
          this.chartInst.dimension(),
          this.attributes.attr_id
        );
        _.each(categories, function(category) {
          if (dataForCategories.hasOwnProperty(category.name)) {
            groups.push({
              name: category.name,
              caseIds: dataForCategories[category.name],
              curveHex: category.color
            });
          }
        });
        this.$dispatch('create-rainbow-survival', {
          attrId: this.attributes.attr_id,
          subtitle: ' (' + this.attributes.display_name + ')',
          groups: groups,
          groupType: this.attributes.group_type
        });
      }
    },
    methods: {
      updateShowSurvivalIcon: function() {
        if (this.showedSurvivalPlot &&
          this.piechart.getCurrentCategories().length >= 2 &&
          this.piechart.getCurrentCategories().length <= this.numOfSurvivalCurveLimit) {
          this.showSurvivalIcon = true;
        } else {
          this.showSurvivalIcon = false;
        }
      },
      mouseEnter: function() {
        this.showOperations = true;
        this.$emit('initMainDivQtip');
      }, mouseLeave: function(event) {
        if (event.relatedTarget === null) {
          this.showOperations = false;
        }
        if ((event.relatedTarget !== null) &&
          (event.relatedTarget.nodeName !== 'CANVAS')) {
          this.showOperations = false;
        }
      }, initMainDivQtip: function() {
        this.piechart.initMainDivQtip();
      }
    },
    ready: function() {
      var _self = this;
      var _attrId = _self.attributes.attr_id;
      var _cluster = _self.ndx.dimension(function(d) {
        if (typeof d[_attrId] === 'undefined') {
          d[_attrId] = 'NA';
        }
        return d[_attrId];
      });

      _self.$once('initMainDivQtip', _self.initMainDivQtip);
      var opts = {
        chartId: _self.chartId,
        chartDivId: _self.chartDivId,
        groupid: _self.attributes.group_id,
        chartTableId: _self.chartTableId,
        transitionDuration: iViz.opts.dc.transitionDuration,
        width: window.iViz.styles.vars.piechart.width,
        height: window.iViz.styles.vars.piechart.height
      };
      _self.piechart = new iViz.view.component.PieChart(
        _self.ndx, _self.attributes, opts, _cluster);
      _self.piechart.setDownloadDataTypes(['tsv', 'pdf', 'svg']);
      _self.chartInst = _self.piechart.getChart();
      _self.chartInst.on('filtered', function(_chartInst, _filter) {
        if (!_self.addingChart) {
          if (_self.filtersUpdated) {
            _self.filtersUpdated = false;
          } else {
            _self.filtersUpdated = true;

            if (_filter === null) {
              _self.attributes.filter = [];
            } else if ($.inArray(_filter, _self.attributes.filter) === -1) {
              _self.attributes.filter.push(_filter);
            } else {
              _self.attributes.filter = _.filter(_self.attributes.filter, function(d) {
                return d !== _filter;
              });
            }
            _self.$dispatch('update-filters');
          }
          // Trigger pie chart filtered event.
          _self.piechart.filtered(_self.attributes.filter);
        }
      });

      _self.updateShowSurvivalIcon();
      _self.$dispatch('data-loaded', this.attributes.group_id, this.chartDivId);
    }
  });
})(
  window.Vue,
  window.dc,
  window.iViz,
  window.$ || window.jQuery, window._);
'use strict';
(function(iViz, dc, _, $, d3, cbio) {
  // iViz pie chart component. It includes DC pie chart.
  iViz.view.component.BarChart = function() {
    var content = this;

    var chartInst_;// DC chart instance.
    var opts_ = {};// Chart configuration options
    var data_ = {};// Chart related data. Such as attrId.
    var colors_;
    var ndx_;
    var dcDimension;
    var mapTickToCaseIds = {}; // Save the related caseIds under tick value.

    var initDc_ = function(logScale) {
      var tickVal = [];
      var i = 0;
      var barSet = {};

      dcDimension = ndx_.dimension(function(d) {
        var val = d[data_.attrId];
        var _min;
        var _max;
        var endNumIndex = opts_.xDomain.length - 1; // when data doesn't have NA

        // isNaN(val) treats string in data as 'NA', such as "withheld" and "cannotReleaseHIPAA"
        if (iViz.util.strIsNa(val, true) || isNaN(val)) {
          var smallerOutlierPattern = new RegExp('^<|(>=?)$');
          var greaterOutlierPattern = new RegExp('^>|(<=?)$');
          if (smallerOutlierPattern.test(val)) {
            val = opts_.xDomain[0];
          } else if (greaterOutlierPattern.test(val)) {
            val = data_.hasNA ? opts_.xDomain[opts_.xDomain.length - 2] : opts_.xDomain[opts_.xDomain.length - 1];
          } else {
            val = opts_.xDomain[opts_.xDomain.length - 1];
          }
        } else if (logScale) {
          for (i = 1; i < opts_.xDomain.length; i++) {
            if (d[data_.attrId] < opts_.xDomain[i] &&
              d[data_.attrId] >= opts_.xDomain[i - 1]) {
              val = parseInt(Math.pow(10, i / 2 - 0.25), 10);
              _min = opts_.xDomain[i - 1];
              _max = opts_.xDomain[i];
              break;
            }
          }
        } else if (data_.noGrouping) {
          val = opts_.xFakeDomain[opts_.xDomain.indexOf(Number(d[data_.attrId]))];
        } else {
          if (data_.smallDataFlag) {
            if (d[data_.attrId] <= opts_.xDomain[1]) {
              val = opts_.xDomain[0];
            } else if (d[data_.attrId] > opts_.xDomain[opts_.xDomain.length - 3] && data_.hasNA) {
              val = opts_.xDomain[opts_.xDomain.length - 2];
            } else if (d[data_.attrId] > opts_.xDomain[opts_.xDomain.length - 2] && !data_.hasNA) {
              val = opts_.xDomain[opts_.xDomain.length - 1];
            } else {
              if (data_.hasNA) {
                endNumIndex = opts_.xDomain.length - 2;
              }
              for (i = 2; i < endNumIndex; i++) {
                if (d[data_.attrId] <= opts_.xDomain[i] &&
                  d[data_.attrId] >= opts_.xDomain[i - 1]) {
                  _min = opts_.xDomain[i - 1];
                  _max = opts_.xDomain[i];
                  val = _min + (_max - _min) / 4;
                  break;
                }
              }
            }
          } else {
            if (d[data_.attrId] <= opts_.xDomain[1]) {
              val = opts_.xDomain[0];
            } else if (d[data_.attrId] > opts_.xDomain[opts_.xDomain.length - 3] && data_.hasNA) {
              val = opts_.xDomain[opts_.xDomain.length - 2];
            } else if (d[data_.attrId] > opts_.xDomain[opts_.xDomain.length - 2] && !data_.hasNA) {
              val = opts_.xDomain[opts_.xDomain.length - 1];
            } else if (iViz.util.isAgeClinicalAttr(data_.attrId) && (opts_.xDomain.length >= 2 && opts_.xDomain[1] === 18) && d[data_.attrId] <= 20) {
              val = Math.ceil((d[data_.attrId] - opts_.startPoint) / opts_.gutter) *
                opts_.gutter + opts_.startPoint + opts_.gutter / 2;
            } else {
              // minus half of separateDistance to make the margin values
              // always map to the left side. Thus for any value x, it is in the
              // range of (a, b] which means a < x <= b
              val = Math.ceil((d[data_.attrId] - opts_.startPoint) / opts_.gutter) *
                opts_.gutter + opts_.startPoint - opts_.gutter / 2;
              _min = val - opts_.gutter / 2;
              _max = val + opts_.gutter / 2;
            }
          }
        }

        barSet[val] = 1;
        if (tickVal.indexOf(val) === -1) {
          tickVal.push(Number(val));
        }

        if (!mapTickToCaseIds.hasOwnProperty(val)) {
          mapTickToCaseIds[val] = {
            caseIds: [],
            tick: iViz.util.getTickFormat(val, logScale, data_, opts_)
          };
          if (!_.isUndefined(_min) && !_.isUndefined(_max)) {
            mapTickToCaseIds[val].range = _min + '< ~ <=' + _max;
          }
        }
        mapTickToCaseIds[val].caseIds.push(
          data_.groupType === 'patient' ? d.patient_uid : d.sample_uid);
        return val;
      });

      opts_.xBarValues = Object.keys(barSet);
      opts_.xBarValues.sort(function(a, b) {
        return Number(a) < Number(b) ? -1 : 1;
      });

      tickVal.sort(function(a, b) {
        return a < b ? -1 : 1;
      });


      chartInst_
        .width(opts_.width)
        .height(opts_.height)
        .margins({top: 10, right: 20, bottom: 30, left: 40})
        .dimension(dcDimension)
        .group(dcDimension.group())
        .centerBar(true)
        .elasticY(true)
        .elasticX(false)
        .turnOnControls(true)
        .mouseZoomable(false)
        .brushOn(true)
        .colors('#2986e2')
        .transitionDuration(iViz.opts.transitionDuration || 400)
        .renderHorizontalGridLines(false)
        .renderVerticalGridLines(false);

      if (data_.noGrouping) {
        chartInst_.barPadding(1);// separate continuous bar
      }

      if (logScale || data_.smallDataFlag) {
        chartInst_.xAxis().tickValues(opts_.xDomain);
        chartInst_.x(d3.scale.log().nice()
          .domain([opts_.minDomain, opts_.maxDomain]));
      } else if (data_.noGrouping) {
        chartInst_.xAxis().tickValues(opts_.xFakeDomain);
        chartInst_.x(d3.scale.linear()
          .domain([
            opts_.xFakeDomain[0] - opts_.gutter,
            opts_.xFakeDomain[opts_.xFakeDomain.length - 1] + opts_.gutter
          ]));
      } else {
        chartInst_.xAxis().tickValues(opts_.xDomain);
        chartInst_.x(d3.scale.linear()
          .domain([
            opts_.xDomain[0] - opts_.gutter,
            opts_.xDomain[opts_.xDomain.length - 1] + opts_.gutter
          ]));
      }
      chartInst_.yAxis().ticks(6);
      chartInst_.yAxis().tickFormat(d3.format('d'));
      chartInst_.xAxis().tickFormat(function(v) {
        return iViz.util.getTickFormat(v, logScale, data_, opts_);
      });

      chartInst_.xUnits(function() {
        return opts_.xDomain.length * 1.3 <= 5 ? 5 : opts_.xDomain.length * 1.3;
      });
    };

    function initTsvDownloadData() {
      var data = [];
      var _cases = _.sortBy(chartInst_.dimension().top(Infinity), function(item) {
        return isNaN(item[data_.attrId]) ? Infinity : -item[data_.attrId];
      });
      var header = ['Patient ID', 'Sample ID', opts_.displayName];

      if (opts_.groupType === 'sample') {
        var tmp = header[0];
        header[0] = header[1];
        header[1] = tmp;
      }
      data.push(header.join('\t'));

      for (var i = 0; i < _cases.length; i++) {
        var row = [];
        if (opts_.groupType === 'patient') {
          var patientUID = _cases[i].patient_uid;
          var patientId = iViz.getCaseIdUsingUID('patient', patientUID);
          var sampleIds = iViz.getSampleIds(_cases[i].study_id, patientId);
          if (_.isArray(sampleIds)) {
            sampleIds = sampleIds.join(', ');
          } else {
            sampleIds = '';
          }
          row.push(patientId);
          row.push(sampleIds);
        } else {
          var sampleUID = _cases[i].sample_uid;
          var sampleId = iViz.getCaseIdUsingUID('sample', sampleUID);
          var patientId = iViz.getPatientId(_cases[i].study_id, sampleId);

          row.push(sampleId);
          row.push(patientId);
        }
        row.push(_.isUndefined(_cases[i][data_.attrId]) ? 'NA' :
          iViz.util.restrictNumDigits(_cases[i][data_.attrId]));
        data.push(row.join('\t'));
      }
      content.setDownloadData('tsv', {
        fileName: opts_.displayName,
        data: data.join('\n')
      });
    }

    function initCanvasDownloadData() {
      content.setDownloadData('svg', {
        title: opts_.displayName,
        chartDivId: opts_.chartDivId,
        chartId: opts_.chartId,
        fileName: opts_.displayName
      });
      content.setDownloadData('pdf', {
        title: opts_.displayName,
        chartDivId: opts_.chartDivId,
        chartId: opts_.chartId,
        fileName: opts_.displayName
      });
    }

    content.init = function(ndx, data, opts) {
      opts_ = _.extend({}, opts);
      data_ = data;
      opts_ = _.extend(opts_, iViz.util.barChart.getDcConfig({
        attrId: opts.attrId,
        min: data_.min,
        max: data_.max,
        meta: data_.meta,
        uniqueSortedData: data_.uniqueSortedData,
        minExponent: data_.minExponent,
        maxExponent: data_.maxExponent,
        smallDataFlag: data_.smallDataFlag,
        noGrouping: data_.noGrouping,
        hasNA: data_.hasNA
      }, opts.logScaleChecked));
      ndx_ = ndx;

      colors_ = $.extend(true, {}, iViz.util.getColors());

      chartInst_ = dc.barChart('#' + opts.chartId, opts.groupid);

      initDc_(opts.logScaleChecked);

      return chartInst_;
    };

    content.rangeFilter = function(logScaleChecked, _filter) {
      var tempFilters_ = [];
      var minNumBarPoint = '';
      var maxNumBarPoint = '';
      var selectedNumBar = [];
      var hasNA = false;
      var hasGreaterOutlier = false;
      var hasSmallerOutlier = false;
      var startNumIndex = 0;
      var endNumIndex = opts_.xDomain.length >= 1 ? opts_.xDomain.length - 1 : 0;

      tempFilters_[0] = '';
      tempFilters_[1] = '';

      if (opts_.xDomain.length === 0) {
        tempFilters_[0] = 'Invalid Selection';
      } else {
        // set start and end indexes for number bars
        if (opts_.xDomain.length >= 2) {
          if (data_.noGrouping) {
            if (data_.hasNA) {
              endNumIndex = opts_.xDomain.length - 2;
            }
          } else {
            if (logScaleChecked) {
              if (data_.hasNA) { // has 'NA' tick
                endNumIndex = opts_.xDomain.length - 2;
              }
            } else {
              startNumIndex = 1;
              if (data_.hasNA) {// has 'NA' tick
                endNumIndex = opts_.xDomain.length >= 3 ? opts_.xDomain.length - 3 : endNumIndex;
              } else {
                endNumIndex = opts_.xDomain.length - 2;
              }
            }
          }
        }

        _.each(opts_.xDomain, function(tick) {
          if (tick >= _filter[0] && tick <= _filter[1]) {
            if (data_.hasNA && tick === opts_.xDomain[opts_.xDomain.length - 1]) {
              hasNA = true;
            } else if (!data_.noGrouping && !logScaleChecked) {
              if (tick === opts_.xDomain[0]) {
                hasSmallerOutlier = true;
              } else if (tick === opts_.xDomain[endNumIndex + 1]) {
                hasGreaterOutlier = true;
              }
            }
          }
        });

        // store all number bars' middle value in selected range
        _.each(opts_.xBarValues, function(middle) {
          if (middle >= _filter[0] && middle <= _filter[1]) {
            if ((data_.noGrouping || logScaleChecked) &&
              (data_.hasNA && middle !== opts_.xDomain[opts_.xDomain.length - 1])) {
              // exclude 'NA' value
              selectedNumBar.push(middle);
            } else {
              // exclude '>max', '<=min' and 'NA' value
              if (middle !== opts_.xDomain[0] || middle !== opts_.xDomain[endNumIndex + 1] ||
                (data_.hasNA && middle !== opts_.xDomain[opts_.xDomain.length - 1])) {
                selectedNumBar.push(middle);
              }
            }
          }
        });

        // make sure x axis has "<=min" and ">max" ticks
        if (!data_.noGrouping && !logScaleChecked && opts_.xDomain.length >= 2) {
          // if left point between '<= min' and 'min'
          if (_filter[0] > opts_.xDomain[0] && _filter[0] <= opts_.xDomain[1]) {
            minNumBarPoint = opts_.xDomain[1];
          }
          // if right point between 'max' and '>max'
          if (_filter[1] >= opts_.xDomain[endNumIndex] && _filter[1] < opts_.xDomain[endNumIndex + 1]) {
            maxNumBarPoint = opts_.xDomain[endNumIndex];
          }
        }

        if (data_.noGrouping) {
          minNumBarPoint = opts_.xFakeDomain[0];
          if (data_.hasNA) {
            maxNumBarPoint = opts_.xFakeDomain[opts_.xFakeDomain.length - 2];
          } else {
            maxNumBarPoint = opts_.xFakeDomain[opts_.xFakeDomain.length - 1];
          }
        } else {
          for (var i = startNumIndex; i <= endNumIndex - 1; i++) {
            if (_filter[0] >= opts_.xDomain[i] && _filter[0] < opts_.xDomain[i + 1]) {// check left range point
              // when there is a bar inside single slot
              if (selectedNumBar[0] >= opts_.xDomain[i + 1] && selectedNumBar[0] < opts_.xDomain[i + 2]) {
                // left point is closer to the upward tick
                minNumBarPoint = opts_.xDomain[i + 1];
              } else {
                if ((_filter[0] - opts_.xDomain[i]) > (opts_.xDomain[i + 1] - _filter[0])) {
                  // left point is closer to the upward tick
                  minNumBarPoint = opts_.xDomain[i + 1];
                } else {
                  // left point is closer to the downward tick
                  minNumBarPoint = opts_.xDomain[i];
                }
              }
            }

            if (_filter[1] >= opts_.xDomain[i] && _filter[1] < opts_.xDomain[i + 1]) {// check right range point
              if (selectedNumBar[selectedNumBar.length - 1] >= opts_.xDomain[i - 1] &&
                selectedNumBar[selectedNumBar.length - 1] < opts_.xDomain[i]) {
                // right point is closer to the downward tick
                maxNumBarPoint = opts_.xDomain[i];
              } else {
                if ((_filter[1] - opts_.xDomain[i]) > (opts_.xDomain[i + 1] - _filter[1])) {
                  // right point is closer to the upward tick
                  maxNumBarPoint = opts_.xDomain[i + 1];
                } else {
                  // right point is closer to the downward tick
                  maxNumBarPoint = opts_.xDomain[i];
                }
              }
            }
          }
        }

        // avoid "min< ~ <=min"
        if (!data_.noGrouping && _.isNumber(minNumBarPoint) && _.isNumber(maxNumBarPoint) && minNumBarPoint === maxNumBarPoint) {
          tempFilters_[0] = 'Invalid Selection';
        } else if ((!data_.noGrouping && _filter[0] < opts_.xDomain[0] && _filter[1] > opts_.xDomain[opts_.xDomain.length - 1]) ||
          (data_.noGrouping && _filter[0] < opts_.xFakeDomain[0] && _filter[1] > opts_.xFakeDomain[opts_.xFakeDomain.length - 1])) {
          tempFilters_[0] = 'All';
        } else {
          if (data_.noGrouping) {
            if (selectedNumBar.length === opts_.xBarValues.length ||
              (_filter[0] <= opts_.xBarValues[0] && _filter[1] >= opts_.xBarValues[opts_.xBarValues.length - 2] &&
                data_.hasNA && !hasNA)) {// select all number bars
              tempFilters_[0] = 'All Numbers';
            } else if (selectedNumBar.length === 0 && hasNA) {// only choose NA bar
              tempFilters_[0] = 'NA';
            } else if (selectedNumBar.length === 1) {// only choose 1 number bar
              if (hasNA) { // chose max num bar and NA bar
                tempFilters_[0] = opts_.xDomain[opts_.xFakeDomain.indexOf(Number(selectedNumBar[0]))] + ', NA';
              } else {
                tempFilters_[0] = opts_.xDomain[opts_.xFakeDomain.indexOf(Number(selectedNumBar[0]))];
              }
            } else {
              _.each(selectedNumBar, function(barValue) {
                tempFilters_[0] += opts_.xDomain[opts_.xFakeDomain.indexOf(Number(barValue))] + ', ';
              });
              if (hasNA) {
                tempFilters_[0] += 'NA';
              } else {
                tempFilters_[0] = tempFilters_[0].slice(0, -2);// remove last coma
              }
            }
          } else {
            if (logScaleChecked) {
              if (!hasNA && _filter[0] < opts_.xDomain[startNumIndex] &&
                _filter[1] > opts_.xDomain[endNumIndex]) {
                tempFilters_[0] = 'All Numbers';
              } else if (!hasNA && minNumBarPoint !== '' && maxNumBarPoint !== '') {
                tempFilters_[0] = minNumBarPoint + '<';
                tempFilters_[1] = '<=' + maxNumBarPoint;
              } else if (!hasNA && minNumBarPoint === '' && maxNumBarPoint !== '') {
                tempFilters_[1] = '<=' + maxNumBarPoint;
              } else if (hasNA && minNumBarPoint !== '' && maxNumBarPoint === '') {
                tempFilters_[0] = minNumBarPoint + '<';
                tempFilters_[1] = '<=' + opts_.xDomain[endNumIndex] + ", NA";
              } else if (hasNA && minNumBarPoint === '' && maxNumBarPoint === '') {//only select "NA" bar
                tempFilters_[0] = 'NA';
              } else {
                tempFilters_[0] = 'Invalid Selection';
              }
            } else {
              if (!hasNA && !hasSmallerOutlier && !hasGreaterOutlier &&
                minNumBarPoint !== '' && maxNumBarPoint !== '') {
                tempFilters_[0] = minNumBarPoint + '<';
                tempFilters_[1] = '<=' + maxNumBarPoint;
              } else if (hasNA && !hasSmallerOutlier) {
                if (hasGreaterOutlier) { // "> Num, NA"
                  if (minNumBarPoint === '') {// only select '>Num' and 'NA' bars
                    tempFilters_[1] = '> ' + opts_.xDomain[opts_.xDomain.length - 3] + ', NA';
                  } else {
                    tempFilters_[1] = '> ' + minNumBarPoint + ', NA';
                  }
                } else {
                  if (minNumBarPoint === '' && maxNumBarPoint === '') {
                    tempFilters_[0] = 'NA';
                  }
                }
              } else if (!hasNA && hasGreaterOutlier) {
                if (hasSmallerOutlier) {// Select all bars excluding NA
                  tempFilters_[0] = 'All Numbers';
                } else {// "> Num"
                  if (minNumBarPoint === '') {
                    tempFilters_[1] = '> ' + opts_.xDomain[endNumIndex];
                  } else {
                    tempFilters_[1] = '> ' + minNumBarPoint;
                  }
                }
              } else if (hasSmallerOutlier && !hasGreaterOutlier && !hasNA) {// "<= Num"
                if (maxNumBarPoint === '') {
                  tempFilters_[1] = '<= ' + opts_.xDomain[1];
                } else {
                  tempFilters_[1] = '<= ' + maxNumBarPoint;
                }
              } else {
                tempFilters_[0] = 'Invalid Selection';
              }
            }
          }
        }
      }

      return tempFilters_;
    };

    content.redraw = function(logScaleChecked) {
      opts_ = _.extend(opts_, iViz.util.barChart.getDcConfig({
        min: data_.min,
        max: data_.max,
        meta: data_.meta,
        uniqueSortedData: data_.uniqueSortedData,
        minExponent: data_.minExponent,
        maxExponent: data_.maxExponent,
        smallDataFlag: data_.smallDataFlag,
        noGrouping: data_.noGrouping,
        hasNA: data_.hasNA
      }, logScaleChecked));

      initDc_(logScaleChecked);
    };

    content.updateDataForDownload = function(fileType) {
      if (fileType === 'tsv') {
        initTsvDownloadData();
      } else if (['pdf', 'svg'].indexOf(fileType) !== -1) {
        initCanvasDownloadData();
      }
    };
    // return content;

    /**
     * sortBy {String} sort category by 'key' (key, value are supported)
     * @return {Array} the list of current bar categories.
     */
    content.getCurrentCategories = function(sortBy) {
      var groups = dcDimension.group().top(Infinity);
      var groupTypeId =
        data_.groupType === 'patient' ? 'patient_uid' : 'sample_uid';
      var selectedCases = _.pluck(dcDimension.top(Infinity), groupTypeId).sort();
      var categories = [];
      var colorCounter = 0;

      sortBy = sortBy || 'value';

      groups = _.sortBy(groups, sortBy);
      _.each(groups, function(group) {
        if (group.value > 0 && mapTickToCaseIds.hasOwnProperty(group.key)) {
          var color;
          var name = mapTickToCaseIds[group.key].range || mapTickToCaseIds[group.key].tick;
          if (name === 'NA') {
            color = '#ccc';
          } else {
            color = colors_[colorCounter];
            colorCounter++;
          }
          categories.push({
            key: group.key,
            name: mapTickToCaseIds[group.key].range || mapTickToCaseIds[group.key].tick,
            color: color,
            caseIds: iViz.util.intersection(selectedCases, mapTickToCaseIds[group.key].caseIds.sort())
          });
        }
      });
      return categories;
    };

    /**
     * Color bar based on categories info.
     * @param {Array} categories The current bar categories, can be calculated by
     * using getCurrentCategories method.
     */
    content.colorBars = function(categories) {
      if (!_.isArray(categories)) {
        categories = this.getCurrentCategories();
      }
      chartInst_.selectAll('g rect').style('fill', function(d) {
        var color = 'grey';
        for (var i = 0; i < categories.length; i++) {
          var category = categories[i];
          if (_.isObject(d.data) && category.key === d.data.key) {
            color = category.color;
            break;
          }
        }
        return color;
      });
    };

    /**
     * Reset bar color by removing fill attribute.
     */
    content.resetBarColor = function() {
      chartInst_.selectAll('g rect').style('fill', '');
    };
  };

  iViz.view.component.BarChart.prototype =
    new iViz.view.component.GeneralChart('barChart');
  iViz.view.component.BarChart.constructor = iViz.view.component.BarChart;

  iViz.util.barChart = (function() {
    var content = {};

    /**
     * Customize the bar chart configuration options according to
     * the data range.
     * @param {object} data Data should include two parameters: min and max.
     * They should all be number.
     * @param {boolean} logScale Whether to generate
     * log scale bar chart options.
     * @return {object} The customized configure options
     */
    content.getDcConfig = function(data, logScale) {
      var config = {
        xDomain: [],
        xFakeDomain: [], // Design for noGrouping Data
        divider: 1,
        numOfGroups: 10,
        emptyMappingVal: '',
        gutter: 0.2,
        startPoint: -1,
        maxVal: '',
        minDomain: 0.7, // Design specifically for log scale
        maxDomain: 10000, // Design specifically for log scale
        xBarValues: [],
      };

      if (!_.isUndefined(data.min) && !_.isUndefined(data.max)) {
        var max = data.max;
        var min = data.min;
        var range = max - min;
        var rangeL = parseInt(range, 10).toString().length - 2;
        var i = 0;
        var _tmpValue;
        var minExponent, maxExponent, exponentRange;

        if (data.smallDataFlag) {
          minExponent = data.minExponent;
          maxExponent = data.maxExponent;
          exponentRange = maxExponent - minExponent;
        }

        // Set divider based on the number m in 10(m)
        for (i = 0; i < rangeL; i++) {
          config.divider *= 10;
        }

        if (max < 100 &&
          max > 50) {
          config.divider = 10;
        } else if (max < 100 &&
          max > 30) {
          config.divider = 5;
        } else if (max < 100 &&
          max > 10) {
          config.divider = 2;
        } else if (data.smallDataFlag) {
          var numberOfTickValues = 4;
          if (exponentRange > 1) {
            config.divider = Math.round(exponentRange / numberOfTickValues);
          } else if (exponentRange === 0) {
            config.divider = 2 * Math.pow(10, minExponent);
          }
        }

        if (range === 0) {
          config.startPoint = min;
        } else if (max <= 1 && max > 0 && min >= -1 && min < 0) {
          config.maxVal = (parseInt(max / config.divider, 10) + 1) *
            config.divider;
          config.gutter = 0.2;
          config.startPoint = (parseInt(min / 0.2, 10) - 1) * 0.2;
          // config.emptyMappingVal = config.maxVal + 0.2;
        } else if (range <= 0.1) {
          config.gutter = 0.01;
        } else if (range <= 1 && min >= 0 && max <= 1) {
          config.gutter = 0.1;
          config.startPoint = 0;
          // config.emptyMappingVal = 1.1;
        } else if (range >= 1) {
          config.gutter = (
            parseInt(range / (config.numOfGroups * config.divider), 10) + 1
          ) * config.divider;
          config.maxVal = (parseInt(max / config.gutter, 10) + 1) *
            config.gutter;
          config.startPoint = parseInt(min / config.gutter, 10) *
            config.gutter;
          // config.emptyMappingVal = config.maxVal + config.gutter;
        } else {
          config.gutter = 0.1;
          config.startPoint = -1;
          // config.emptyMappingVal = config.maxVal + 0.1;
        }

        if (logScale) {
          for (i = 0; ; i += 0.5) {
            _tmpValue = parseInt(Math.pow(10, i), 10);

            config.xDomain.push(_tmpValue);
            if (_tmpValue > data.max) {
              config.xDomain.push(Math.pow(10, i + 0.5));
              if (data.hasNA) {
                config.emptyMappingVal = Math.pow(10, i + 1);
              }
              config.maxDomain = Math.pow(10, i + 1.5);
              break;
            }
          }
        } else if (data.smallDataFlag) {// use decimal scientific ticks for 0 < data < 0.1
          if (exponentRange > 1) {
            config.minDomain = Math.pow(10, minExponent - config.divider * 1.5);
            config.xDomain.push(Math.pow(10, minExponent - config.divider));// add "<=" marker
            for (i = minExponent; i <= maxExponent; i += config.divider) {
              config.xDomain.push(Math.pow(10, i));
            }
            config.xDomain.push(Math.pow(10, maxExponent + config.divider));// add ">=" marker
            if (data.hasNA) {
              config.emptyMappingVal = Math.pow(10, maxExponent + config.divider * 2);// add "NA" marker
            }
            config.maxDomain = Math.pow(10, maxExponent + config.divider * 2.5);
          } else if (exponentRange === 1) {
            config.minDomain = Math.pow(10, minExponent - 1);
            config.xDomain.push(Math.pow(10, minExponent) / 3); // add "<=" marker
            for (i = minExponent; i <= maxExponent + 1; i++) {
              config.xDomain.push(Math.pow(10, i));
              config.xDomain.push(3 * Math.pow(10, i));
            }
            if (data.hasNA) {
              config.emptyMappingVal = Math.pow(10, maxExponent + 2);// add "NA" marker
            }
            config.maxDomain = 3 * Math.pow(10, maxExponent + 2);
          } else { // exponentRange = 0
            config.minDomain = Math.pow(10, minExponent) - config.divider;
            for (i = Math.pow(10, minExponent); i <= Math.pow(10, maxExponent + 1); i += config.divider) {
              config.xDomain.push(i);
            }
            if (data.hasNA) {
              config.emptyMappingVal = Math.pow(10, maxExponent + 1) + config.divider;// add "NA" marker
            }
            config.maxDomain = Math.pow(10, maxExponent + 1) + config.divider * 2;
          }
        } else {
          // for data has at most 5 points
          if (data.noGrouping) {
            _.each(data.uniqueSortedData, function(value) {
              config.xFakeDomain.push(data.uniqueSortedData.indexOf(value) * config.gutter);
              config.xDomain.push(value);
            });
            if (data.hasNA) {
              // add marker for NA values
              config.emptyMappingVal =
                config.xFakeDomain[config.xFakeDomain.length - 1] + config.gutter;
              config.xFakeDomain.push(config.emptyMappingVal);
            }
          } else {
            if (!_.isNaN(range)) {
              for (i = 0; i <= config.numOfGroups; i++) {
                _tmpValue = i * config.gutter + config.startPoint;
                if (config.startPoint < 1500) {
                  _tmpValue =
                    Number(cbio.util.toPrecision(Number(_tmpValue), 3, 0.1));
                }

                // If the current tmpValue already bigger than maximum number, the
                // function should decrease the number of bars and also reset the
                // Mapped empty value.
                if (_tmpValue >= max) {
                  // if i = 0 and tmpValue bigger than maximum number, that means
                  // all data fall into NA category.
                  // config.xDomain.push(_tmpValue);
                  break;
                } else {
                  config.xDomain.push(_tmpValue);
                }
              }
            }

            if (config.xDomain.length === 0) {
              config.xDomain.push(Number(config.startPoint));
            } else if (config.xDomain.length === 1) {
              config.xDomain.push(Number(config.xDomain[0] + config.gutter));
            } else if (Math.abs(min) > 1500) {
              // currently we always add ">max" and "NA" marker
              // add marker for greater than maximum
              config.xDomain.push(
                Number(config.xDomain[config.xDomain.length - 1] +
                  config.gutter));
              if (data.hasNA) {
                // add marker for NA values
                config.emptyMappingVal =
                  config.xDomain[config.xDomain.length - 1] + config.gutter;
              }
            } else {
              // add marker for greater than maximum
              config.xDomain.push(
                Number(cbio.util.toPrecision(
                  Number(config.xDomain[config.xDomain.length - 1] +
                    config.gutter), 3, 0.1)));
              if (data.hasNA) {
                // add marker for NA values
                config.emptyMappingVal =
                  Number(cbio.util.toPrecision(
                    Number(config.xDomain[config.xDomain.length - 1] +
                      config.gutter), 3, 0.1));
              }
            }
          }
        }

        if (config.emptyMappingVal !== '') {
          config.xDomain.push(config.emptyMappingVal);
        }

        if (iViz.util.isAgeClinicalAttr(data.attrId)) {
          // Toning the range when the domains cover 18 and the gutter is under 4
          if (config.xDomain.indexOf(18) === -1 && config.gutter <= 4 && config.gutter >= 2 && config.xDomain[0] < 18 && config.xDomain[config.xDomain.length - 1] > 18) {
            var _closestIndex = -1;
            var _closestDistance = 0;
            _.each(config.xDomain, function(domain, index) {
              // _diff should be always a integer in this range
              var _diff = Math.abs(18 - domain);
              if (_closestDistance === 0 || _diff < _closestDistance) {
                _closestDistance = _diff;
                _closestIndex = index;
              }
            });
            if (_closestIndex > -1) {
              config.xDomain = _.map(config.xDomain, function(domain) {
                return domain + _closestDistance;
              });
              config.xDomain.unshift(config.xDomain[0] - config.gutter);
            }
            if (config.emptyMappingVal !== '') {
              config.emptyMappingVal = config.xDomain[config.xDomain.length - 1];
              if (config.xDomain.length >= 2) {
                config.maxVal = config.xDomain[config.xDomain.length - 2];
              }
            } else {
              config.maxVal = config.xDomain[config.xDomain.length - 1];
            }
            config.startPoint = config.xDomain[0];
          }

          // If the lowest group is 10~20, change the range to 10~18 and 18~30
          if (config.xDomain[0] === 10 && config.xDomain[1] === 20) {
            config.xDomain[1] = 18;
          }

          // if it happens the first sticker is 18, then we keep that group as well.
          if (config.xDomain[0] === 18) {
            config.xDomain.unshift(18 - config.gutter);
          }
        }
      }
      return config;
    };
    return content;
  })();
})(window.iViz,
  window.dc,
  window._,
  window.$ || window.jQuery,
  window.d3,
  window.cbio
);
/**
 * Created by Karthik Kalletla on 4/6/16.
 */
'use strict';
(function(Vue, d3, dc, iViz, _, $, cbio) {
  Vue.component('barChart', {
    template: '<div id={{chartDivId}} ' +
    'class="grid-item grid-item-w-2 grid-item-h-1 bar-chart" ' +
    ':attribute-id="attributes.attr_id" @mouseenter="mouseEnter" ' +
    ':layout-number="attributes.layout" ' +
    '@mouseleave="mouseLeave">' +
    '<chart-operations :show-log-scale="settings.showLogScale"' +
    ':show-operations="showOperations" :groupid="attributes.group_id" ' +
    ':chart-initialed = "!failedToInit"' +
    ':show-survival-icon.sync="showSurvivalIcon"' +
    ':reset-btn-id="resetBtnId" :chart-ctrl="barChart" ' +
    ':chart-id="chartId" :show-log-scale="showLogScale" ' +
    ':attributes="attributes"' +
    ':filters.sync="attributes.filter"></chart-operations>' +
    '<div class="dc-chart dc-bar-chart" align="center" ' +
    'style="float:none !important;" id={{chartId}} >' +
    '<div v-if="failedToInit" class="error-panel" align="center" style="padding-top: 10%;">' +
    '<error v-if="failedToInit" :message="errorMessage"></error>' +
    '</div></div>' +
    '<span class="text-center chart-title-span" ' +
    'id="{{chartId}}-title">{{displayName}}</span>' +
    '</div>',
    props: [
      'ndx', 'attributes', 'showedSurvivalPlot'
    ],
    data: function() {
      return {
        chartDivId:
          iViz.util.getDefaultDomId('chartDivId', this.attributes.attr_id),
        resetBtnId:
          iViz.util.getDefaultDomId('resetBtnId', this.attributes.attr_id),
        chartId:
          iViz.util.getDefaultDomId('chartId', this.attributes.attr_id),
        displayName: this.attributes.display_name,
        chartInst: {},
        barChart: {},
        showOperations: false,
        filtersUpdated: false,
        showSurvivalIcon: false,
        data: {},
        settings: {
          width: 400,
          height: 180,
          showLogScale: false,
          transitionDuration: iViz.opts.dc.transitionDuration
        },
        failedToInit: false,
        errorMessage: '',
        opts: {},
        numOfSurvivalCurveLimit: iViz.opts.numOfSurvivalCurveLimit || 20,
        addingChart: false
      };
    }, watch: {
      'attributes.filter': function(newVal) {
        if (this.filtersUpdated) {
          this.filtersUpdated = false;
        } else {
          this.filtersUpdated = true;
          if (newVal.length === 0) {
            this.chartInst.filterAll();
            this.$dispatch('update-filters', true);
          }
        }
        this.barChart.resetBarColor();
      },
      'showedSurvivalPlot': function() {
        this.updateShowSurvivalIcon();
      }
    }, events: {
      closeChart: function() {
        if (!this.failedToInit) {
          dc.deregisterChart(this.chartInst, this.attributes.group_id);
          this.chartInst.dimension().dispose();
        }
        this.$dispatch('close');
      },
      changeLogScale: function(logScaleChecked) {
        $('#' + this.chartId).find('svg').remove();
        this.chartInst.filterAll();
        this.$dispatch('update-filters', true);
        dc.deregisterChart(this.chartInst, this.attributes.group_id);
        this.initChart(logScaleChecked);
        this.chartInst.render();
      },
      addingChart: function(groupId, val) {
        if (this.attributes.group_id === groupId) {
          if (this.attributes.filter.length > 0) {
            if (val) {
              this.addingChart = val;
              this.chartInst.filter(null);
            } else {
              var filter_ = new dc.filters.RangedFilter(this.attributes.filter[0], this.attributes.filter[1]);
              this.chartInst.filter(filter_);
              this.addingChart = val;
            }
          }
        }
      },
      getRainbowSurvival: function() {
        var groups = [];
        var categories = this.barChart.getCurrentCategories('key');
        _.each(categories, function(category) {
          groups.push({
            name: category.name,
            caseIds: category.caseIds,
            curveHex: category.color
          });
        });
        this.barChart.colorBars(categories);
        this.$dispatch('create-rainbow-survival', {
          attrId: this.attributes.attr_id,
          subtitle: ' (' + this.attributes.display_name + ')',
          groups: groups,
          groupType: this.attributes.group_type
        });
      },
      resetBarColor: function(exceptionAttrIds) {
        if (!this.failedToInit &&
          _.isArray(exceptionAttrIds) && exceptionAttrIds.indexOf(this.attributes.attr_id) === -1) {
          this.barChart.resetBarColor();
        }
      }
    },
    methods: {
      updateShowSurvivalIcon: function() {
        if (this.showedSurvivalPlot) {
          // Disable rainbow survival if only one group present
          if (this.barChart.getCurrentCategories().length < 2 ||
            this.barChart.getCurrentCategories().length > this.numOfSurvivalCurveLimit) {
            this.showSurvivalIcon = false;
          } else {
            this.showSurvivalIcon = true;
          }
        } else {
          this.showSurvivalIcon = false;
        }
      },
      processBarchartData: function(_data) {
        var _self = this;
        var _dataIssue = false;
        var smallerOutlier = {};
        var greaterOutlier = {};
        var dataMetaKeys = {}; // Fast index unique dataMeta instead of using _.unique

        this.data.meta = _.map(_.filter(_.pluck(
          _data, this.opts.attrId), function(d) {
          if (isNaN(d) && !(_.isString(d) && (d.indexOf('>') !== -1 || d.indexOf('<') !== -1))) {
            _self.data.hasNA = true;
            d = 'NA';
          }
          return d !== 'NA';
        }), function(d) {
          var number = d;
          var smallerOutlierPattern = new RegExp('^<|(>=?)$');
          var greaterOutlierPattern = new RegExp('^>|(<=?)$');
          if (isNaN(d)) {
            if (smallerOutlierPattern.test(number)) {
              smallerOutlier[number.replace(/[^0-9.]/g, '')] = 1;
            } else if (greaterOutlierPattern.test(number)) {
              greaterOutlier[number.replace(/[^0-9.]/g, '')] = 1;
            } else {
              _dataIssue = true;
            }
          } else {
            number = parseFloat(d);
          }
          dataMetaKeys[number] = true;
          return number;
        });

        smallerOutlier = Object.keys(smallerOutlier);
        greaterOutlier = Object.keys(greaterOutlier);

        if (_dataIssue) {
          this.errorMessage = iViz.util.getDataErrorMessage('dataInvalid');
          this.failedToInit = true;
        } else {
          // for scientific small number
          if (this.data.meta[Math.ceil((this.data.meta.length * (1 / 2)))] < 0.001 &&
            this.data.meta[Math.ceil((this.data.meta.length * (1 / 2)))] > 0) {
            this.data.smallDataFlag = true;
            this.data.exponents = cbio.util.getDecimalExponents(this.data.meta);
            var findExtremeExponentResult = cbio.util.findExtremes(this.data.exponents);
            this.data.minExponent = findExtremeExponentResult[0];
            this.data.maxExponent = findExtremeExponentResult[1];
          } else {
            this.data.smallDataFlag = false;
          }

          var findExtremeResult = cbio.util.findExtremes(this.data.meta);
          if (iViz.util.isAgeClinicalAttr(this.attributes.attr_id) && _.min(this.data.meta) < 18 && (findExtremeResult[1] - findExtremeResult[0]) / 2 > 18) {
            this.data.min = 18;
          } else {
            this.data.min = findExtremeResult[0];
          }
          this.data.max = findExtremeResult[1];

          // noGrouping is true when number of different values less than or equal to 5. 
          // In this case, the chart sets data value as ticks' value directly. 
          this.data.noGrouping = false;
          if (Object.keys(dataMetaKeys).length <= 5 && this.data.meta.length > 0) {// for data less than 6 points
            this.data.noGrouping = true;
            this.data.uniqueSortedData = _.unique(findExtremeResult[3]);// use sorted value as ticks directly
          }

          if (smallerOutlier.length > 0) {
            this.data.min = Number(_.max(smallerOutlier));
            this.data.smallerOutlier = this.data.min;
          }

          if (greaterOutlier.length > 0) {
            this.data.max = Number(_.min(greaterOutlier));
            this.data.greaterOutlier = this.data.max;
          }

          this.data.attrId = this.attributes.attr_id;
          this.data.groupType = this.attributes.group_type;

          // logScale and noGroup cannot be true at same time
          // logScale and smallDataFlag cannot be true at same time
          if (((this.data.max - this.data.min) > 1000) && (this.data.min > 1) && !this.data.noGrouping) {
            this.settings.showLogScale = true;
          }
          this.barChart = new iViz.view.component.BarChart();
          this.barChart.setDownloadDataTypes(['tsv', 'pdf', 'svg']);
          this.initChart(this.settings.showLogScale);
          this.updateShowSurvivalIcon();
        }
        this.$dispatch('data-loaded', this.attributes.group_id, this.chartDivId);
      },
      mouseEnter: function() {
        this.showOperations = true;
      }, mouseLeave: function() {
        this.showOperations = false;
      }, initChart: function(logScaleChecked) {
        this.opts = _.extend(this.opts, {
          logScaleChecked: logScaleChecked
        });

        this.chartInst = this.barChart.init(this.ndx, this.data, this.opts);
        var self_ = this;
        this.chartInst.on('filtered', function(_chartInst, _filter) {
          // TODO : Right now we are manually checking for brush mouseup event.
          // This should be updated one latest dc.js is released
          // https://github.com/dc-js/dc.js/issues/627
          if (!self_.addingChart) {
            if (self_.filtersUpdated) {
              self_.filtersUpdated = false;
            } else {
              self_.chartInst.select('.brush').on('mouseup', function() {
                self_.filtersUpdated = true;
                if (typeof _filter !== 'undefined' && _filter !== null &&
                  _filter.length > 1 && self_.chartInst.hasFilter()) {
                  self_.attributes.filter = self_.barChart.rangeFilter(logScaleChecked, _filter);
                  self_.$dispatch('update-filters');
                } else if (self_.attributes.filter.length > 0) {
                  self_.attributes.filter = [];
                  self_.$dispatch('update-filters');
                }
              });
            }
          }
        });
      }
    },
    ready: function() {
      var _self = this;
      var _data = [];
      this.settings.width = window.iViz.styles.vars.barchart.width;
      this.settings.height = window.iViz.styles.vars.barchart.height;

      this.opts = _.extend(this.opts, {
        groupType: this.attributes.group_type,
        attrId: this.attributes.attr_id,
        displayName: this.attributes.display_name,
        chartDivId: this.chartDivId,
        chartId: this.chartId,
        groupid: this.attributes.group_id,
        width: this.settings.width,
        height: this.settings.height
      });

      
      _data = iViz.getGroupNdx(this.opts.groupid);
      _self.processBarchartData(_data);
    }
  });
})(
  window.Vue,
  window.d3,
  window.dc,
  window.iViz,
  window._,
  window.$ || window.jQuery,
  window.cbio
);
/**
 * Created by Yichao Sun on 5/11/16.
 */

'use strict';
(function(iViz, _, d3, $, Plotly, cbio) {
  iViz.view.component.ScatterPlot = function() {
    var content = this;
    var chartId_;
    var data_;
    var groups_ = [];
    var opts_;
    var layout_;
    var getQtipString = function(_data) {
      var toReturn = ['Cancer Study:', _data.study_id, '<br>Sample Id: ',
        iViz.getCaseIdUsingUID('sample', _data.sample_uid), '<br>CNA fraction: '];
      if (isNaN(_data.FRACTION_GENOME_ALTERED)) {
        toReturn.push(_data.FRACTION_GENOME_ALTERED);
      } else {
        toReturn.push(cbio.util.toPrecision(Number(_data.FRACTION_GENOME_ALTERED), 2, 0.001));
      }
      toReturn.push('<br>Mutation count: ' + _data.MUTATION_COUNT);
      return toReturn.join('');
    };

    content.init = function(_data, opts) {
      opts_ = $.extend(true, {}, opts);
      chartId_ = opts_.chartId;
      data_ = _.filter(_data, function(datum) {
        return !isNaN(datum.FRACTION_GENOME_ALTERED) && !isNaN(datum.MUTATION_COUNT);
      });
      var _xArr = _.pluck(data_, 'FRACTION_GENOME_ALTERED').map(Number);
      var _yArr = _.pluck(data_, 'MUTATION_COUNT').map(Number);
      var _qtips = [];
      _.each(data_, function(_dataObj) {
        _qtips.push(getQtipString(_dataObj));
      });
      var trace = {
        x: _xArr,
        y: _yArr,
        text: _qtips,
        mode: 'markers',
        type: 'scattergl',
        hoverinfo: 'text',
        study_id: _.pluck(data_, 'study_id'),
        sample_uid: _.pluck(data_, 'sample_uid'),
        marker: {
          size: 7,
          color: '#2986e2',
          line: {color: 'white'}
        }
      };
      var data = [trace];
      opts_.numOfTraces = 1;
      var _marginX = (d3.max(_xArr) - d3.min(_xArr)) * 0.05;
      var _marginY = (d3.max(_yArr) - d3.min(_yArr)) * 0.05;
      layout_ = {
        xaxis: {
          title: 'Fraction of copy number altered genome',
          range: [d3.min(_xArr) - _marginX, d3.max(_xArr) + _marginX],
          zeroline: false,
          showline: true,
          tickangle: -45
        },
        yaxis: {
          title: '# of mutations',
          range: [d3.min(_yArr) - _marginY, d3.max(_yArr) + _marginY],
          zeroline: false,
          showline: true
        },
        hovermode: 'closest',
        dragmode: 'select',
        showlegend: false,
        width: opts_.width || 370,
        height: opts_.height || 320,
        margin: {
          l: 60,
          r: 10,
          b: 50,
          t: 30,
          pad: 0
        }
      };
      Plotly.plot(chartId_, data, layout_, {
        displaylogo: false,
        modeBarButtonsToRemove: ['sendDataToCloud', 'pan2d',
          'zoomIn2d', 'zoomOut2d', 'resetScale2d',
          'hoverClosestCartesian', 'hoverCompareCartesian', 'toImage']
      });

      groups_ = [{
        name: 'Unselected',
        data: _data
      }];

      initCanvasDownloadData();
    };

    // update selected samples (change color)
    content.update = function(_sampleIds) {
      var _selectedData = [];
      var _unselectedData = [];

      var _tmpSelectedSampleIdMap = {};
      _.each(_sampleIds, function(_sampleId) {
        _tmpSelectedSampleIdMap[_sampleId] = '';
      });
      _.each(data_, function(_dataObj) {
        if (_tmpSelectedSampleIdMap.hasOwnProperty(_dataObj.sample_uid)) {
          _selectedData.push(_dataObj);
        } else {
          _unselectedData.push(_dataObj);
        }
      });

      var _unselectedDataQtips = [];
      var _selectedDataQtips = [];

      _.each(_unselectedData, function(_dataObj) {
        _unselectedDataQtips.push(getQtipString(_dataObj));
      });
      _.each(_selectedData, function(_dataObj) {
        _selectedDataQtips.push(getQtipString(_dataObj));
      });

      groups_ = [];
      if (_selectedData.length > 0) {
        groups_.push({
          name: 'Selected',
          data: _selectedData
        })
      }
      if (_unselectedData.length > 0) {
        groups_.push({
          name: 'Unselected',
          data: _unselectedData
        })
      }

      var _traces = [];
      for (var i = 0; i < opts_.numOfTraces; i++) {
        _traces.push(i);
      }
      Plotly.deleteTraces(chartId_, _traces);
      var data = [];
      data.push({
        x: _.pluck(_unselectedData, 'FRACTION_GENOME_ALTERED'),
        y: _.pluck(_unselectedData, 'MUTATION_COUNT'),
        text: _unselectedDataQtips,
        mode: 'markers',
        type: 'scattergl',
        hoverinfo: 'text',
        study_id: _.pluck(data_, 'study_id'),
        sample_uid: _.pluck(data_, 'sample_uid'),
        marker: {
          size: 6,
          color: '#2986e2',
          line: {color: 'white'}
        }
      });
      data.push({
        x: _.pluck(_selectedData, 'FRACTION_GENOME_ALTERED'),
        y: _.pluck(_selectedData, 'MUTATION_COUNT'),
        text: _selectedDataQtips,
        mode: 'markers',
        type: 'scattergl',
        hoverinfo: 'text',
        study_id: _.pluck(data_, 'study_id'),
        sample_uid: _.pluck(data_, 'sample_uid'),
        marker: {
          size: 6,
          color: 'red',
          line: {color: 'white'}
        }
      });
      opts_.numOfTraces = 2;
      Plotly.addTraces(chartId_, data);
    };

    content.updateDataForDownload = function(fileType) {
      if (['pdf', 'svg'].indexOf(fileType) !== -1) {
        initCanvasDownloadData();
      } else if (fileType === 'tsv') {
        initTsvDownloadData();
      }
    };

    function initTsvDownloadData() {
      var _title = opts_.title || 'Mutation Count vs. CNA';
      var _data = ['Patient ID', 'Sample ID', 'Mutation Count', 'CNA', 'Group'];

      _data = [_data.join('\t')];
      _.each(groups_, function(group) {
        _.each(group.data, function(item) {
          var _sampleId = iViz.getCaseIdUsingUID('sample', item.sample_uid);
          var _patientId = iViz.getPatientId(item.study_id, _sampleId);
          var _txt = (_patientId ? _patientId : 'NA') +
            '\t' + _sampleId + '\t' + item.MUTATION_COUNT + '\t' +
            item.FRACTION_GENOME_ALTERED + '\t' + group.name;
          _data.push(_txt);
        });
      });

      content.setDownloadData('tsv', {
        fileName: _title,
        data: _data.join('\n')
      });
    }

    function initCanvasDownloadData() {
      content.setDownloadData('svg', {
        title: opts_.title,
        chartDivId: opts_.chartId,
        fileName: opts_.title
      });
      content.setDownloadData('pdf', {
        title: opts_.title,
        chartDivId: opts_.chartId,
        fileName: opts_.title
      });
    }
  };

  iViz.view.component.ScatterPlot.prototype =
    new iViz.view.component.GeneralChart('scatterPlot');
  iViz.view.component.ScatterPlot.constructor =
    iViz.view.component.ScatterPlot;
  iViz.util.scatterPlot = (function() {
  })();
})(window.iViz,
  window._,
  window.d3,
  window.jQuery || window.$,
  window.Plotly,
  window.cbio
);

/**
 * @author Yichao Sun on 5/11/16.
 */
'use strict';
(function(Vue, dc, iViz, _) {
  Vue.component('scatterPlot', {
    template: '<div id={{chartDivId}} ' +
    'class="grid-item grid-item-h-2 grid-item-w-2" ' +
    ':attribute-id="attributes.attr_id" @mouseenter="mouseEnter" ' +
    '@mouseleave="mouseLeave">' +
    '<chart-operations :show-operations="showOperations"' +
    ' :display-name="displayName" :has-chart-title="true" :groupid="attributes.group_id"' +
    ' :reset-btn-id="resetBtnId" :chart-ctrl="chartInst" ' +
    ':chart-id="chartId"' +
    ' :attributes="attributes" :filters.sync="attributes.filter"></chart-operations>' +
    ' <div v-show="!showLoad" ' +
    'class="dc-chart dc-scatter-plot" align="center" ' +
    ':class="{\'show-loading-content\': showLoad}" id={{chartId}} ></div>' +
    '<div v-show="showLoad" class="progress-bar-parent-div" :class="{\'show-loading-bar\': showLoad}" >' +
    '<progress-bar :div-id="loadingBar.divId" :type="loadingBar.type" :disable="loadingBar.disable" ' +
    ' :status="loadingBar.status" :opts="loadingBar.opts"></progress-bar></div>' +
    '<div v-if="failedToInit" class="error-panel" align="center">' +
    '<error-handle v-if="failedToInit" :error="error"></error-handle>' +
    '</div></div>',
    props: [
      'ndx', 'attributes'
    ],
    data: function() {
      return {
        chartDivId:
          iViz.util.getDefaultDomId('chartDivId', this.attributes.attr_id),
        resetBtnId:
          iViz.util.getDefaultDomId('resetBtnId', this.attributes.attr_id),
        chartId:
          iViz.util.getDefaultDomId('chartId', this.attributes.attr_id),
        displayName: this.attributes.display_name,
        showOperations: false,
        selectedSamples: [],
        chartInst: {},
        showLoad: true,
        error: {
          dataInvalid: false,
          noData: false,
          failedToLoadData: false
        },
        failedToInit: false,
        loadingBar :{
          status: 0,
          type: 'percentage',
          divId: iViz.util.getDefaultDomId('progressBarId', this.attributes.attr_id),
          opts: {},
          disable: false
        },
        invisibleDimension: {}
      };
    },
    watch: {
      'attributes.filter': function(newVal) {
        if (newVal.length === 0) {
          this.invisibleDimension.filterAll();
        }
        this.$dispatch('update-filters', true);
      },
      showLoad: function(newVal) {
        if (newVal) {
          this.initialInfinityLoadingBar();
        } else {
          this.loadingBar.disable = true;
        }
      }
    },
    events: {
      'show-loader': function() {
        this.showLoad = true;
      },
      'update-special-charts': function(hasFilters) {
        if (this.dataLoaded) {
          var attrId =
            this.attributes.group_type === 'patient' ? 'patient_uid' : 'sample_uid';
          var _selectedCases =
            _.pluck(this.invisibleDimension.top(Infinity), attrId);

          this.selectedSamples = _selectedCases;
          if (hasFilters) {
            this.chartInst.update(_selectedCases);
          } else {
            this.chartInst.update([]);
          }
          this.attachPlotlySelectedEvent();
        }
        this.showLoad = false;
      },
      'closeChart': function() {
        this.invisibleDimension.dispose();
        this.$dispatch('close');
      },
      'addingChart': function(groupId, val) {
        if (this.attributes.group_id === groupId) {
          if (this.attributes.filter.length > 0) {
            if (val) {
              this.invisibleDimension.filterAll();
            } else {
              var filtersMap = {};
              _.each(this.attributes.filter, function(filter) {
                if (filtersMap[filter] === undefined) {
                  filtersMap[filter] = true;
                }
              });
              this.invisibleDimension.filterFunction(function(d) {
                return (filtersMap[d] !== undefined);
              });
            }
          }
        }
      }
    },
    methods: {
      mouseEnter: function() {
        this.showOperations = true;
      }, mouseLeave: function() {
        this.showOperations = false;
      },
      initialInfinityLoadingBar: function() {
        this.loadingBar.type = 'infinite';
      },
      attachPlotlySelectedEvent: function() {
        var _self = this;
        var data = iViz.getGroupNdx(_self.attributes.group_id);

        document.getElementById(this.chartId).on('plotly_selected',
          function(_eventData) {
            if (typeof _eventData !== 'undefined') {
              var _selectedData = [];
              // create hash map for (overall) data with cna_fraction + mutation
              // count as key, dataObj as value (performance concern)
              var _CnaFracMutCntMap = {};
              _.each(data, function(_dataObj) {
                var _key = Number(_dataObj.FRACTION_GENOME_ALTERED) + '||' + Number(_dataObj.MUTATION_COUNT);
                _CnaFracMutCntMap[_key] = _dataObj;
              });
              _.each(_eventData.points, function(_pointObj) {
                if (_pointObj.x) {
                  _selectedData.push(
                    _CnaFracMutCntMap[_pointObj.x + '||' + _pointObj.y]);
                }
              });
              var _selectedCases = _.pluck(_selectedData, 'sample_uid').sort();
              _self.selectedSamples = _selectedCases;
              _self.attributes.filter = _selectedCases;

              var filtersMap = {};
              _.each(_selectedCases, function(filter) {
                if (filtersMap[filter] === undefined) {
                  filtersMap[filter] = true;
                }
              });
              _self.invisibleDimension.filterFunction(function(d) {
                return (filtersMap[d] !== undefined);
              });
              dc.redrawAll(_self.attributes.group_id);
            }
          });
      }
    },
    ready: function() {
      var _self = this;
      _self.showLoad = true;

      // make scatterplot can be closed even if ajax fails
      var attrId =
        _self.attributes.group_type === 'patient' ? 'patient_uid' : 'sample_uid';
      _self.invisibleDimension = _self.ndx.dimension(function(d) {
        return d[attrId];
      });
      
      $.when(iViz.getScatterData(_self))
        .then(function(_scatterData) {
          var _opts = {
            chartId: _self.chartId,
            chartDivId: _self.chartDivId,
            title: _self.attributes.display_name,
            width: window.iViz.styles.vars.scatter.width,
            height: window.iViz.styles.vars.scatter.height
          };
          
          _self.chartInst = new iViz.view.component.ScatterPlot();
          _self.chartInst.setDownloadDataTypes(['pdf', 'svg', 'tsv']);
          _self.chartInst.init(_scatterData, _opts);

          _self.dataLoaded = true;
          var _selectedCases =
            _.pluck(_self.invisibleDimension.top(Infinity), attrId);
          if (_self.$root.hasfilters) {
            _self.chartInst.update(_selectedCases);
          }
          _self.attachPlotlySelectedEvent();
         
          _self.showLoad = false;
        }, function() {
          _self.showLoad = false;
          _self.error.failedToLoadData = true;
          _self.failedToInit = true;
        });
      
      this.$dispatch('data-loaded', this.attributes.group_id, this.chartDivId);
    }
  });
})(window.Vue,
  window.dc,
  window.iViz,
  window._
);

/**
 * Created by Yichao Sun on 5/18/16.
 */

'use strict';
(function(iViz, _) {
  iViz.view.component.Survival = function() {
    var content_ = this;
    var opts_ = {
      downloadIsEnabled: true
    };
    var groups_ = [];

    content_.init = function(groups, _data, _opts) {
      opts_ = $.extend(true, {}, _opts);
      $('#' + opts_.chartId).empty();
      var _dataProxy = new iViz.data.SurvivalChartProxy(_data, opts_.attrId);
      this.chartInst_ =
        new iViz.view.component
          .SurvivalCurve(opts_.chartId, _dataProxy.get(), opts_);
      this.update(groups, _opts.chartId, _opts.attrId);
    };

    // _attrId here indicates chart type (OS or DFS)
    content_.update = function(groups, _chartId, _attrId) {
      // remove previous curves
      var _chartInst_ = this.chartInst_;
      var _newGroups = [];
      _chartInst_.removeCurves();
      _chartInst_.removePval();
      _chartInst_.removeNoInfo();

      if (_.isArray(groups)) {

        // Calculate proxy data for each group
        _.each(groups, function(group) {
          group.proxyData =
            new iViz.data.SurvivalChartProxy(group.data, _attrId).get();
          if(_.isArray(group.proxyData) && group.proxyData.length > 0) {
            _newGroups.push(group);
          }
        });

        if (_newGroups.length > 0) {
          if (_newGroups.length === 2) {
            _chartInst_.addPval(_newGroups[0].proxyData, _newGroups[1].proxyData);
          }
          _.each(_newGroups, function(group, index) {
            _chartInst_.addCurve(group.proxyData, index, group.curveHex);
          });
          opts_.downloadIsEnabled = true;
        } else {
          _chartInst_.addNoInfo();
          opts_.downloadIsEnabled = false;
        }
      }
      groups_ = _newGroups;
    };

    content_.updateDataForDownload = function(fileType) {
      if (opts_.downloadIsEnabled && ['pdf', 'svg'].indexOf(fileType) !== -1) {
        initCanvasDownloadData();
      }
    };

    content_.getGroups = function() {
      return groups_;
    };

    content_.highlightCurve = function(curveId) {
      this.chartInst_.highlightCurve(curveId);
    };
    
    content_.downloadIsEnabled = function() {
      return opts_.downloadIsEnabled;
    };

    function initCanvasDownloadData() {
      content_.setDownloadData('svg', {
        title: opts_.title,
        chartDivId: opts_.chartId,
        fileName: opts_.title
      });
      content_.setDownloadData('pdf', {
        title: opts_.title,
        chartDivId: opts_.chartId,
        fileName: opts_.title
      });
    }
  };
  iViz.view.component.Survival.prototype =
    new iViz.view.component.GeneralChart('survivalPlot');
  iViz.view.component.Survival.constructor = iViz.view.component.Survival;
})(
  window.iViz,
  window._
);

/**
 * @author Yichao Sun on 5/18/16.
 */
'use strict';
(function(Vue, dc, iViz, _) {
  Vue.component('survival', {
    template: '<div id={{chartDivId}} ' +
    'class="grid-item grid-item-h-2 grid-item-w-2" ' +
    ':attribute-id="attributes.attr_id" @mouseenter="mouseEnter" ' +
    '@mouseleave="mouseLeave">' +
    '<chart-operations :show-operations="showOperations" ' +
    ':show-download-icon.sync="showDownloadIcon" ' +
    ':has-chart-title="hasChartTitle" :display-name="displayName" ' +
    ':groupid="attributes.group_id" :reset-btn-id="resetBtnId" :chart-ctrl="chartInst" ' +
    ' :chart-id="chartId" ' +
    ':attributes="attributes"></chart-operations>' +
    '<div v-show="!showLoad" :class="{\'show-loading-content\': showLoad}"' +
    'class="dc-chart dc-scatter-plot" align="center" id={{chartId}} ></div>' +
    '<div v-show="showLoad" class="progress-bar-parent-div" :class="{\'show-loading-bar\': showLoad}">' +
    '<progress-bar :div-id="loadingBar.divId" :status="loadingBar.status" :opts="loadingBar.opts" ' +
    ':type="loadingBar.type" :disable="loadingBar.disable" ></progress-bar></div>' +
    '</div>',
    props: [
      'ndx', 'attributes'
    ],
    created: function() {
    },
    data: function() {
      return {
        chartDivId: iViz.util.getDefaultDomId('chartDivId', this.attributes.attr_id),
        resetBtnId: iViz.util.getDefaultDomId('resetBtnId', this.attributes.attr_id),
        chartId: iViz.util.getDefaultDomId('chartId', this.attributes.attr_id),
        displayName: this.attributes.display_name,
        chartInst: {},
        showOperations: false,
        fromWatch: false,
        fromFilter: false,
        hasChartTitle: true,
        showLoad: true,
        excludeNa: true,
        hasFilters: false,
        showDownloadIcon: true,
        showingRainbowSurvival: false,
        groups: [],
        invisibleDimension: {},
        loadingBar: {
          status: 0,
          type: 'percentage',
          divId: iViz.util.getDefaultDomId('progressBarId', this.attributes.attr_id),
          opts: {},
          disable: false
        },
        mainDivQtip: ''
      };
    },
    watch: {
      excludeNa: function() {
        if (this.showingRainbowSurvival) {
          this.updateRainbowSurvival();
        } else {
          this.updatePlotGroups(this.hasFilters);
          this.updatePlot();
          this.$dispatch('remove-rainbow-survival');
        }
      },
      showLoad: function(newVal) {
        if (newVal) {
          this.initialInfinityLoadingBar();
        } else {
          this.loadingBar.disable = true;
        }
      }
    },
    events: {
      'show-loader': function() {
        this.showLoad = true;
      },
      'update-special-charts': function(hasFilters) {
        this.showingRainbowSurvival = false;
        this.updatePlotGroups(hasFilters);
        this.updatePlot();
        this.$dispatch('remove-rainbow-survival');
      },
      'closeChart': function() {
        this.invisibleDimension.dispose();
        this.$dispatch('close');
      },
      'addingChart': function(groupId, val) {
        if (this.attributes.group_id === groupId) {
          if (this.attributes.filter.length > 0) {
            if (val) {
              this.invisibleDimension.filterAll();
            } else {
              var filtersMap = {};
              _.each(this.attributes.filter, function(filter) {
                if (filtersMap[filter] === undefined) {
                  filtersMap[filter] = true;
                }
              });
              this.invisibleDimension.filterFunction(function(d) {
                return (filtersMap[d] !== undefined);
              });
            }
          }
        }
      },
      'create-rainbow-survival': function(opts) {
        var _opts = $.extend(true, {}, opts);
        _opts.groups = this.calcCurvesData(
          _opts.groups, _opts.groupType);
        this.groups = $.extend(true, [], _opts.groups);
        if (_opts.subtitle) {
          this.displayName = this.attributes.display_name + _opts.subtitle;
        }
        this.showingRainbowSurvival = true;
        this.updateRainbowSurvival();
      }
    },
    methods: {
      updateRainbowSurvival: function() {
        var groups = $.extend(true, [], this.groups);
        if (this.excludeNa) {
          groups = _.filter(groups, function(group) {
            return group.name !== 'NA';
          });
        }
        this.updatePlot(groups);
      },
      updatePlot: function(groups) {
        // Display name may be changed due to the rainbow survival
        this.displayName = this.attributes.display_name;

        this.chartInst.update(
          groups ? groups : this.groups, this.chartId, this.attributes.attr_id);
        this.checkDownloadableStatus();
        this.updateQtipContent();
        this.showLoad = false;
      },
      updatePlotGroups: function(hasFilters) {
        var _type = this.attributes.group_type;
        var attrId = _type === 'patient' ? 'patient_uid' : 'sample_uid';
        var groupId = this.attributes.group_id;
        var _selectedCases = [];
        var _nonNaCases = [];
        var _allCases = iViz.getCaseUIDs(_type).sort();
        var groups = [];

        this.hasFilters = hasFilters;

        if (this.hasFilters) {
          var filteredClinicalAttrs = {};
          _.each(this.$root.groups, function(group) {
            var _attrId = group.type === 'patient' ? 'patient_uid' : 'sample_uid';
            if (!filteredClinicalAttrs.hasOwnProperty(group.id)) {
              filteredClinicalAttrs[group.id] = {
                attrId: _attrId,
                attrs: [],
                nonNaCases: []
              };
            }
            filteredClinicalAttrs[group.id].attrs = [];

            // Loop through attrList instead of only using attr_id
            // Combination chart has its own attr_id, but the clinical data
            // it's using are listed under attrList
            _.each(_.filter(group.attributes, function(attr) {
              return attr.filter.length > 0;
            }), function(item) {
              filteredClinicalAttrs[group.id].attrs.push(_.pick(item, 'attr_id', 'attrList'));
            });
          });
          if (this.excludeNa) {
            // Find qualified cases in each group.
            _.each(filteredClinicalAttrs, function(group, _groupId) {
              var data_ = iViz.getGroupNdx(_groupId);
              var nonNaCases = [];

              //Check whether case contains NA value on filtered attrs
              var _attrs = {};
              _.each(group.attrs, function(groupAttr) {
                _.each(groupAttr.attrList, function(listItem) {
                  _attrs[listItem] = 1;
                })
              });
              _attrs = Object.keys(_attrs);

              _.each(data_, function(data) {
                var hasNaWithinAttrs = false;
                _.some(_attrs, function(attr) {
                  // All data has been normalized to NA for different NA values
                  if (data[attr] === undefined || data[attr] === 'NA') {
                    hasNaWithinAttrs = true;
                    return true;
                  }
                });

                if (!hasNaWithinAttrs) {
                  var _caseId = data[group.attrId];
                  if (groupId !== _groupId) {
                    if (_type === 'patient') {
                      _caseId = iViz.getPatientUIDs(_caseId);
                    } else {
                      _caseId = iViz.getSampleUIDs(_caseId)
                    }
                  }
                  if (_.isArray(_caseId)) {
                    nonNaCases.push.apply(nonNaCases, _caseId);
                  } else {
                    nonNaCases.push(_caseId);
                  }
                }
              });
              group.nonNaCases = nonNaCases.sort();
            });

            // Find unique data from each group.
            var _list = _.pluck(filteredClinicalAttrs, 'nonNaCases');
            for (var i = 0; i < _list.length; i++) {
              if (i === 0) {
                _nonNaCases = _list[0];
                continue;
              }
              _nonNaCases = iViz.util.intersection(_nonNaCases, _list[i]);
            }
            _selectedCases = iViz.util.intersection(_nonNaCases, _.pluck(this.invisibleDimension.top(Infinity), attrId).sort());
            _allCases = iViz.util.intersection(_nonNaCases, _allCases);
          } else {
            _selectedCases =
              _.pluck(this.invisibleDimension.top(Infinity), attrId).sort();
          }
        }
        if (_selectedCases.length === 0) {
          groups.push({
            id: 0,
            caseIds: _allCases,
            curveHex: '#2986e2',
            name: 'All Patients'
          });
        } else {
          groups = [{
            id: 0,
            caseIds: _selectedCases,
            curveHex: 'red',
            name: 'Selected Patients'
          }, {
            id: 1,
            caseIds: iViz.util.difference(
              _allCases, _selectedCases),
            curveHex: '#2986e2',
            name: 'Unselected Patients'
          }];
        }
        groups = this.calcCurvesData(groups, _type);
        this.groups = groups;
      },
      updateQtipContent: function() {
        if (this.mainDivQtip) {
          var self_ = this;
          var qtipContent = ['<div>'];
          var groups = this.chartInst.getGroups();
          var api = this.mainDivQtip.qtip('api');
          _.each(groups, function(group) {
            qtipContent.push(
              '<div class="category-item" curve-id="' + group.id + '">' +
              '<svg width="12" height="12">' +
              '<rect height="12" width="12" fill="' +
              group.curveHex + '"></rect>' +
              '</svg><span>' + group.name + '</span></div>');
          });
          qtipContent.push('</div>');

          qtipContent.push('<div class="checkbox-div">' +
            '<input type="checkbox" class="checkbox" ' +
            (this.excludeNa ? 'checked' : '') + '><span>' +
            'Exclude patients with NA for any of the selected attribute(s)</span></div>');
          api.set('content.text', qtipContent.join(''));

          // Tender tooltip after updating content
          // Otherwise, api.elements.tooltip will return null.
          api.render();

          var tooltip = api.elements.tooltip;
          tooltip.find('.category-item').click(function() {
            var curveId = $(this).attr('curve-id');
            self_.chartInst.highlightCurve(curveId);
          });
          tooltip.find('.checkbox-div .checkbox').change(function() {
            self_.excludeNa = this.checked;
          });
        }
      },
      mouseEnter: function() {
        this.showOperations = true;
        this.$emit('initMainDivQtip');
      }, mouseLeave: function() {
        this.showOperations = false;
      },
      calcCurvesData: function(groups, groupType) {
        var data_ = iViz.getGroupNdx(this.attributes.group_id);
        var survivalType = this.attributes.group_type;
        _.each(groups, function(group, index) {
          group.id = index;
          group.data = [];

          // If group type is sample, need to convert sample ID to patient ID.
          if (groupType === 'sample') {
            group.caseIds = iViz.util.idMapping(iViz.getCasesMap('sample'),
              group.caseIds);
          }
          _.each(group.caseIds, function(id) {
            //  var _index = iViz.getCaseIndices(survivalType)[id];
            group.data.push(data_[id]);
          });
        });
        return groups;
      },
      initMainDivQtip: function() {
        var self_ = this;
        var chartDivId = self_.chartDivId;
        self_.mainDivQtip = $('#' + chartDivId).qtip({
          id: chartDivId + '-qtip',
          style: {
            classes: 'qtip-light qtip-rounded qtip-shadow forceZindex qtip-max-width dc-survival-chart-qtip'
          },
          show: {event: 'mouseover', delay: 300},
          hide: {fixed: true, delay: 300, event: 'mouseleave'},
          // hide: false,
          position: {
            my: 'left center',
            at: 'center right',
            viewport: $(window)
          },
          content: '<div>Loading...</div>'
        });
        self_.updateQtipContent();
      },
      initialInfinityLoadingBar: function() {
        this.loadingBar.type = 'infinite';
      },
      checkDownloadableStatus: function() {
        if (this.chartInst.downloadIsEnabled()) {
          this.showDownloadIcon = true;
        } else {
          this.showDownloadIcon = false;
        }
      }
    },
    ready: function() {
      var _self = this;
      var attrId =
        this.attributes.group_type === 'patient' ? 'patient_uid' : 'sample_uid';
      this.invisibleDimension = this.ndx.dimension(function(d) {
        return d[attrId];
      });
      var _opts = {
        width: window.iViz.styles.vars.survival.width,
        height: window.iViz.styles.vars.survival.height,
        chartId: this.chartId,
        attrId: this.attributes.attr_id,
        title: this.attributes.display_name,
        type: this.attributes.group_type
      };
      var _type = this.attributes.group_type;

      _self.chartInst = new iViz.view.component.Survival();
      _self.chartInst.setDownloadDataTypes(['pdf', 'svg']);

      var data = iViz.getGroupNdx(this.attributes.group_id);
      if (this.$root.hasfilters) {
        this.updatePlotGroups(true);
      } else {
        var groups = [{
          id: 0,
          name: 'All Patients',
          curveHex: '#2986e2',
          caseIds: iViz.getCaseUIDs(_type)
        }];
        this.groups = this.calcCurvesData(groups, _type);
      }
      _self.chartInst.init(this.groups, data, _opts);
      _self.checkDownloadableStatus();
      _self.showLoad = false;
      _self.$once('initMainDivQtip', _self.initMainDivQtip);
      this.$dispatch('data-loaded', this.attributes.group_id, this.chartDivId);
    }
  });
})(
  window.Vue,
  window.dc,
  window.iViz,
  window._
);

'use strict';
window.kmEstimator = (function(_) {
  return {
    calc: function(_inputArr) {
      // calculate the survival rate for each time point
      // each item in the input already has fields: time, num at risk,
      // event/status(0-->censored)
      var _prev_value = 1; // cache for the previous value
      _.each(_inputArr, function(_inputObj) {
        if (_inputObj.status === 1) {
          _inputObj.survival_rate = _prev_value *
            ((_inputObj.num_at_risk - 1) / _inputObj.num_at_risk);
          _prev_value = _inputObj.survival_rate;
        } else if (_inputObj.status === 0) {
          // survival rate remain the same if the event is "censored"
          _inputObj.survival_rate = _prev_value;
        } else {
          // TODO: error handling
        }
      });
    }
  };
})(
  window._
); // Close KmEstimator

'use strict';
window.LogRankTest = (function(jStat) {
  var datum = {
    time: '',  // num of months
    num_of_failure_1: 0,
    num_of_failure_2: 0,
    num_at_risk_1: 0,
    num_at_risk_2: 0,
    expectation: 0, // (n1j / (n1j + n2j)) * (m1j + m2j)
    variance: 0
  };
  var mergedArr = [];

  // os: DECEASED-->1, LIVING-->0; dfs: Recurred/Progressed --> 1,
  // Disease Free-->0
  function mergeGrps(inputGrp1, inputGrp2) {
    var _ptr_1 = 0; // index indicator/pointer for group1
    var _ptr_2 = 0; // index indicator/pointer for group2

    // Stop when either pointer reach the end of the array
    while (_ptr_1 < inputGrp1.length && _ptr_2 < inputGrp2.length) {
      var _datum;
      if (inputGrp1[_ptr_1].time < inputGrp2[_ptr_2].time) {
        _datum = jQuery.extend(true, {}, datum);
        _datum.time = inputGrp1[_ptr_1].time;
        if (inputGrp1[_ptr_1].status === 1) {
          _datum.num_of_failure_1 = 1;
          _datum.num_at_risk_1 = inputGrp1[_ptr_1].num_at_risk;
          _datum.num_at_risk_2 = inputGrp2[_ptr_2].num_at_risk;
          _ptr_1 += 1;
        } else {
          _ptr_1 += 1;
          continue;
        }
      } else if (inputGrp1[_ptr_1].time > inputGrp2[_ptr_2].time) {
        _datum = jQuery.extend(true, {}, datum);
        _datum.time = inputGrp2[_ptr_2].time;
        if (inputGrp2[_ptr_2].status === 1) {
          _datum.num_of_failure_2 = 1;
          _datum.num_at_risk_1 = inputGrp1[_ptr_1].num_at_risk;
          _datum.num_at_risk_2 = inputGrp2[_ptr_2].num_at_risk;
          _ptr_2 += 1;
        } else {
          _ptr_2 += 1;
          continue;
        }
      } else { // events occur at the same time point
        _datum = jQuery.extend(true, {}, datum);
        _datum.time = inputGrp1[_ptr_1].time;
        if (inputGrp1[_ptr_1].status === 1 || inputGrp2[_ptr_2].status === 1) {
          if (inputGrp1[_ptr_1].status === 1) {
            _datum.num_of_failure_1 = 1;
          }
          if (inputGrp2[_ptr_2].status === 1) {
            _datum.num_of_failure_2 = 1;
          }
          _datum.num_at_risk_1 = inputGrp1[_ptr_1].num_at_risk;
          _datum.num_at_risk_2 = inputGrp2[_ptr_2].num_at_risk;
          _ptr_1 += 1;
          _ptr_2 += 1;
        } else {
          _ptr_1 += 1;
          _ptr_2 += 1;
          continue;
        }
      }
      mergedArr.push(_datum);
    }
  }

  function calcExpection() {
    $.each(mergedArr, function(index, _item) {
      _item.expectation =
        (_item.num_at_risk_1 / (_item.num_at_risk_1 + _item.num_at_risk_2)) *
        (_item.num_of_failure_1 + _item.num_of_failure_2);
    });
  }

  function calcVariance() {
    $.each(mergedArr, function(index, _item) {
      var _num_of_failures = _item.num_of_failure_1 + _item.num_of_failure_2;
      var _num_at_risk = _item.num_at_risk_1 + _item.num_at_risk_2;
      _item.variance =
        (
          _num_of_failures * (_num_at_risk - _num_of_failures) *
          _item.num_at_risk_1 * _item.num_at_risk_2
        ) / ((_num_at_risk * _num_at_risk) * (_num_at_risk - 1));
    });
  }

  function calcPval() {
    var O1 = 0;
    var E1 = 0;
    var V = 0;
    $.each(mergedArr, function(index, obj) {
      O1 += obj.num_of_failure_1;
      E1 += obj.expectation;
      V += obj.variance;
    });
    var chi_square_score = (O1 - E1) * (O1 - E1) / V;
    var _pVal = 1 - jStat.chisquare.cdf(chi_square_score, 1);
    return _pVal;
  }

  return {
    calc: function(inputGrp1, inputGrp2) {
      mergedArr.length = 0;
      mergeGrps(inputGrp1, inputGrp2);
      calcExpection();
      calcVariance();
      return calcPval();
    }
  };
})(
  window.jStat
);

'use strict';
(function(iViz, dc, _, d3, LogRankTest) {
  iViz.view.component.SurvivalCurve = function(_divId, _data, _opts) {
    var _self = this;

    _self.elem_ = '';
    _self.divId_ = _divId;
    _self.data_ = _data;
    _self.opts_ = {
      curves: {}
    };
    _self.opts_ = $.extend(true, _self.opts_, _opts);
    var formatAsPercentage_ = d3.format('%');

    var leftMargin_ = 60;
    var rightMargin_ = 10;
    var topMargin_ = 15;
    var bottomMargin_ = 60;

    _self.elem_ = d3.select('#' + _self.divId_);
    _self.elem_.svg = _self.elem_.append('svg')
      .attr('width', _opts.width)
      .attr('height', _opts.height);

    // init axis
    _self.elem_.xScale = d3.scale.linear()
      .domain([0,
        d3.max(_.pluck(_self.data_, 'time')) +
        d3.max(_.pluck(_self.data_, 'time')) / 15])
      .range([leftMargin_, _opts.width - rightMargin_]);
    _self.elem_.yScale = d3.scale.linear()
      .domain([-0.03, 1.05]) // fixed to be 0-1
      .range([topMargin_ - bottomMargin_ + _opts.height, topMargin_]);
    _self.elem_.xAxis = d3.svg.axis()
      .scale(_self.elem_.xScale)
      .orient('bottom')
      .tickSize(6, 0, 0);
    _self.elem_.yAxis = d3.svg.axis()
      .scale(_self.elem_.yScale)
      .tickFormat(formatAsPercentage_)
      .orient('left')
      .tickSize(6, 0, 0);

    // draw axis
    _self.elem_.svg.append('g')
      .style('stroke-width', 1)
      .style('fill', 'none')
      .style('stroke', 'black')
      .attr('class', 'survival-curve-x-axis-class')
      .style('shape-rendering', 'crispEdges')
      .attr('transform', 'translate(0, ' +
        (topMargin_ - bottomMargin_ + _opts.height) + ')')
      .call(_self.elem_.xAxis);
    _self.elem_.svg.append('g')
      .style('stroke-width', 1)
      .style('fill', 'none')
      .style('stroke', 'black')
      .style('shape-rendering', 'crispEdges')
      .attr('transform', 'translate(0, ' + topMargin_ + ')')
      .call(_self.elem_.xAxis.orient('bottom').ticks(0));
    _self.elem_.svg.append('g')
      .style('stroke-width', 1)
      .style('fill', 'none')
      .style('stroke', 'black')
      .attr('class', 'survival-curve-y-axis-class')
      .style('shape-rendering', 'crispEdges')
      .attr('transform', 'translate(' + leftMargin_ + ', 0)')
      .call(_self.elem_.yAxis);
    _self.elem_.svg.append('g')
      .style('stroke-width', 1)
      .style('fill', 'none')
      .style('stroke', 'black')
      .style('shape-rendering', 'crispEdges')
      .attr('transform', 'translate(' + (_opts.width - rightMargin_) + ', 0)')
      .call(_self.elem_.yAxis.orient('left').ticks(0));
    _self.elem_.svg.selectAll('text')
      .style('font-family', 'sans-serif')
      .style('font-size', '11px')
      .style('stroke-width', 0.5)
      .style('stroke', 'black')
      .style('fill', 'black');

    // append axis title
    _self.elem_.svg.append('text')
      .attr('class', 'label')
      .attr('x', leftMargin_ + (_opts.width - leftMargin_) / 2)
      .attr('y', (topMargin_ + _opts.height - 25))
      .style('text-anchor', 'middle')
      .style('font-size', '11px')
      .style('font-weight', 'bold')
      .text('Months Survival');
    _self.elem_.svg.append('text')
      .attr('class', 'label')
      .attr('transform', 'rotate(-90)')
      .attr('x', (topMargin_ - _opts.height) / 2)
      .attr('y', leftMargin_ - 45)
      .style('text-anchor', 'middle')
      .style('font-size', '11px')
      .style('font-weight', 'bold')
      .text('Surviving');

    _self.elem_.curves = {};
  };

  iViz.view.component.SurvivalCurve.prototype.addCurve = function(data,
    _curveIndex,
    _lineColor) {
    var _self = this;
    
    // Filter points with negative value 		
    var _data = _.sortBy(_.filter(data, function(point) {
      return point.time >= 0 && point.survival_rate >= 0;
    }), 'time');

    // add an empty/zero point so the curve starts from zero time point
    if (_data !== null && _data.length !== 0) {
      if (_data[0].time !== 0) {
        _data = [{
          status: 0,
          survival_rate: 1,
          time: 0
        }].concat(_data);
      }
    }
    
    var _dataLength = _data.length;

    // Only do the data optimization on more than 5000 data points.
    if (_dataLength > 2000) {
      var _dataTime = _.pluck(_data, 'time');
      var _dataSurvivalRate = _.pluck(_data, 'survival_rate');

      var _dataTimeMax = d3.max(_dataTime);
      var _dataTimeMin = d3.min(_dataTime);

      var _dataSurvivalRateMax = d3.max(_dataSurvivalRate);
      var _dataSurvivalRateeMin = d3.min(_dataSurvivalRate);

      var timeThreshold = (_dataTimeMax - _dataTimeMin) / 200;
      var survivalRateThreshold = (_dataSurvivalRateMax - _dataSurvivalRateeMin) / 200;
      var averageThreshold = iViz.util.getHypotenuse(timeThreshold, survivalRateThreshold);
      var lastDataPoint = {
        x: 0,
        y: 0
      };
      _data = _.filter(_data, function(dataItem, index) {
        if (index == 0) {
          lastDataPoint.x = dataItem.time;
          lastDataPoint.y = dataItem.survival_rate;
          return true;
        }
        var dataItemVal = iViz.util.getHypotenuse(dataItem.time - lastDataPoint.x, dataItem.survival_rate - lastDataPoint.y);
        if (dataItemVal > averageThreshold) {
          lastDataPoint.x = dataItem.time;
          lastDataPoint.y = dataItem.survival_rate;
          return true;
        } else {
          return false;
        }
      });
    }

    if (!_self.elem_.curves.hasOwnProperty(_curveIndex)) {
      _self.elem_.curves[_curveIndex] = {};
      _self.elem_.curves[_curveIndex].curve = _self.elem_.svg.append('g')
        .attr('id', _self.divId_ + '-curve-' + _curveIndex);
      _self.elem_.curves[_curveIndex].line = _self.elem_
        .curves[_curveIndex].curve.append('g')
        .attr('class', 'line');
      _self.elem_.curves[_curveIndex].dots = _self.elem_
        .curves[_curveIndex].curve.append('g')
        .attr('class', 'dots');
      _self.elem_.curves[_curveIndex].invisibleDots = _self.elem_
        .curves[_curveIndex].curve.append('g')
        .attr('class', 'invisibleDots');

      // init line elem
      _self.elem_.curves[_curveIndex].lineElem = d3.svg.line()
        .interpolate('step-after')
        .x(function(d) {
          return _self.elem_.xScale(d.time);
        })
        .y(function(d) {
          return _self.elem_.yScale(d.survival_rate);
        });

      // Init opts for the curve
      _self.opts_.curves[_curveIndex] = {};
    }

    // draw line
    if (_data !== null && _data.length > 0) {
      _self.elem_.curves[_curveIndex].line.append('path')
        .attr('d', _self.elem_.curves[_curveIndex].lineElem(_data))
        .attr('class', 'curve')
        .style('fill', 'none')
        .style('stroke', _lineColor);
    }

    // draw censored dots
    // crossDots specifically for the curve for easier deletion
    // changed two separate lines to a single cross symbol
    _self.elem_.curves[_curveIndex].dots.selectAll('path')
      .data(_data)
      .enter()
      .append('path')
      .filter(function(d) {
        return d.status === 0;
      })
      .attr('transform', function(d) {
        return 'translate(' + _self.elem_.xScale(d.time) + ',' +
          _self.elem_.yScale(d.survival_rate) + ')';
      })
      .attr('d', d3.svg.symbol().type('cross')
        .size(function() {
          return 25;
        })
      )
      .attr('class', 'curve')
      .attr('fill', _lineColor);

    // draw invisible dots
    _self.elem_.curves[_curveIndex].invisibleDots.selectAll('path')
      .data(_data)
      .enter()
      .append('svg:path')
      .on('mouseover', function(d) {
        var dot = d3.select(this);
        var _survivalRate = d3.select(this).attr('survival_rate');
        _survivalRate = parseFloat(_survivalRate).toFixed(2);
        var _time = d3.select(this).attr('time');
        _time = parseFloat(_time).toFixed(2);
        dot.transition()
          .duration(300)
          .style('opacity', '.5');

        $(this).qtip(
          {
            content: {
              text: function() {
                var content =
                  (_.isUndefined(_survivalRate) ? '' :
                    ('Survival Rate: <strong>' +
                    _survivalRate + '</strong><br>')) +
                  (_.isUndefined(_time) ? '' :
                    ('Months: <strong>' + _time + '</strong><br>')) +
                  (d.patient_id ?
                    ('Patient ID: <strong><a href="' + window.cbioURL +
                    window.cbio.util
                      .getLinkToPatientView(d.study_id, d.patient_id) +
                    '" target="_blank">' + d.patient_id + '</a></strong><br>') :
                    '') +
                  (d.study_id ?
                    ('Study: <strong>' + d.study_id + '</strong>') : '');
                return content;
              }
            },
            style: {
              classes: 'qtip-light qtip-rounded qtip-shadow ' +
              'qtip-lightyellow qtip-wide'
            },
            show: {
              event: 'mouseover',
              ready: true
            },
            hide: {fixed: true, delay: 100, event: 'mouseout'},
            position: {my: 'left bottom', at: 'top right'}
          }
        );
      })
      .on('mouseout', function() {
        var dot = d3.select(this);
        dot.transition()
          .duration(300)
          .style('opacity', 0);
      })
      .attr('time', function(d) {
        return d.time;
      })
      .attr('survival_rate', function(d) {
        return d.survival_rate;
      })
      .attr('d', d3.svg.symbol()
        .size(300)
        .type('circle'))
      .attr('transform', function(d) {
        return 'translate(' + _self.elem_.xScale(d.time) + ', ' +
          _self.elem_.yScale(d.survival_rate) + ')';
      })
      .attr('fill', _lineColor)
      .style('opacity', 0)
      .attr('class', 'curve')
      .attr('class', 'invisible_dots');
  };

  iViz.view.component.SurvivalCurve.prototype.removeCurves = function() {
    var _self = this;
    for (var key in _self.elem_.curves) {
      if (_self.elem_.curves.hasOwnProperty(key)) {
        _self.elem_.curves[key].curve.remove();
        delete _self.elem_.curves[key];
      }
    }
  };

  iViz.view.component.SurvivalCurve.prototype.addPval =
    function(_selectedData, _unselectedData) {
      var _self = this;
      _self.elem_.svg.selectAll('.pval').remove();
      var _pVal = LogRankTest.calc(_selectedData, _unselectedData);
      _self.elem_.svg.append('text')
        .attr('class', 'pval')
        .attr('x', _self.opts_.width - 30)
        .attr('y', 30)
        .attr('font-size', 10)
        .style('text-anchor', 'end')
        .text('p = ' + _pVal.toPrecision(2));
    };

  iViz.view.component.SurvivalCurve.prototype.addNoInfo =
    function() {
      var _self = this;
      _self.elem_.svg.selectAll('.noInfo').remove();

      _self.elem_.svg.append('text')
        .attr('class', 'noInfo')
        .attr('x', _self.opts_.width / 2 + 25)
        .attr('y', _self.opts_.height / 2)
        .attr('font-size', 15)
        .style('font-style', 'bold')
        .style('text-anchor', 'middle')
        .text('No data available');
    };

  iViz.view.component.SurvivalCurve.prototype.removeNoInfo = function() {
    var _self = this;
    _self.elem_.svg.selectAll('.noInfo').remove();
  };

  iViz.view.component.SurvivalCurve.prototype.removePval = function() {
    var _self = this;
    _self.elem_.svg.selectAll('.pval').remove();
  };

  iViz.view.component.SurvivalCurve.prototype.highlightCurve =
    function(curveIndex) {
      var _self = this;
      if (_self.elem_.curves.hasOwnProperty(curveIndex)) {
        var opacity = '0.5';
        if (_self.opts_.curves[curveIndex].highlighted) {
          opacity = '0';
          _self.opts_.curves[curveIndex].highlighted = false;
        } else {
          _self.opts_.curves[curveIndex].highlighted = true;
        }
        // Don't highlight the time equal to 0. This is a fake node added
        // in addCurve function
        _self.elem_.curves[curveIndex].invisibleDots
          .selectAll('path:not([time="0"])')
          .style('opacity', opacity);
      }
    };
})(
  window.iViz,
  window.dc,
  window._,
  window.d3,
  window.LogRankTest
);

'use strict';
/**
 * Data proxy for survival chart.
 *
 * @param {array} _data Data
 * @param {string} _attrId Chart type: DFS_SURVIVAL or OS_SURVIVAL
 * @return {object} APIs
 */
(function(iViz, kmEstimator, _) {
  iViz.data.SurvivalChartProxy = function(_data, _attrId) {
    var datumArr_ = [];

    // convert raw data
    var _totalNum = 0;
    _.each(_data, function(_dataObj) {
      var _status;
      var _time;
      if (_attrId === 'DFS_SURVIVAL') {
        _time = _dataObj.DFS_MONTHS;
        _status = _dataObj.DFS_STATUS;
      } else if (_attrId === 'OS_SURVIVAL') {
        _time = _dataObj.OS_MONTHS;
        _status = _dataObj.OS_STATUS;
      }
      if (!isNaN(_time) &&
        _status !== 'NaN' && _status !== 'NA' &&
        typeof _status !== 'undefined' && typeof _time !== 'undefined') {
        var _datum = {
          study_id: '',
          patient_id: '',
          patient_uid: '',
          time: '', // num of months
          status: '',
          num_at_risk: -1,
          survival_rate: 0
        };
        _datum.patient_uid = _dataObj.patient_uid;
        _datum.patient_id = iViz.getCaseIdUsingUID('patient', _dataObj.patient_uid);
        _datum.study_id = _dataObj.study_id;
        _datum.time = parseFloat(_time);
        _datum.status = _status;
        datumArr_.push(_datum);
        _totalNum += 1;
      }
    });

    // convert status from string to number
    // os: DECEASED-->1, LIVING-->0; dfs: Recurred
    // Progressed --> 1, Disease Free-->0
    _.each(datumArr_, function(_datumObj) {
      var _status = _datumObj.status.toString().toLowerCase();
      if (_status === 'deceased' || _status === 'recurred/progressed' ||
        _status === 'recurred' || _status === 1) {
        _datumObj.status = 1;
      } else if (_status === 'living' || _status === 'disease free' ||
        _status === 'diseasefree' || _status === 'alive' || _status === 0) {
        _datumObj.status = 0;
      } else {
        // TODO : by default set status 0 when _status doesn't
        // match to any of the above cases, not sure whether to treat them as
        // living or not
        _datumObj.status = 0;
      }
    });

    // calculate num at risk
    datumArr_ = _.sortBy(datumArr_, 'time');
    for (var i in datumArr_) {
      if (datumArr_.hasOwnProperty(i)) {
        datumArr_[i].num_at_risk = _totalNum;
        _totalNum += -1;
      }
    }

    // calculate survival rate
    kmEstimator.calc(datumArr_);

    return {
      get: function() {
        return datumArr_;
      }
    };
  };
})(
  window.iViz,
  window.kmEstimator,
  window._
);

/**
 * Created by Karthik Kalletla on 6/20/16.
 */
'use strict';
(function(iViz, dc, _, React, ReactDOM, EnhancedFixedDataTableSpecial) {
  // iViz pie chart component. It includes DC pie chart.
  iViz.view.component.TableView = function() {
    var content = this;
    var chartId_;
    var data_ = [];
    var type_ = '';
    var attr_ = [];
    var attributes_ = [];
    var geneData_ = [];
    var selectedRows = [];
    var selectedGenes = [];
    var callbacks_ = {};
    var sequencedSampleIds = [];
    var selectedSamples = [];
    var allSamplesIds = [];
    var reactTableData = {};
    var initialized = false;
    var selectedRowData = [];
    var selectedGeneData = [];
    var displayName = '';
    var categories_ = {};
    var reactData_;
    var isMutatedGeneCna = false;
    var dimension = {};
    var group = {};
    var labelInitData = {};
    var opts = {};
    var genePanelMap = {};
    var renderedReactTable;

    // Category based color assignment. Avoid color changing
    var assignedColors = {
      NA: '#cccccc'
    };
    var colors = $.extend(true, [], iViz.util.getColors());

    content.getCases = function() {
      return iViz.util.intersection(selectedSamples, sequencedSampleIds);
    };

    content.getSelectedRowData = function() {
      return selectedRowData;
    };

    content.clearSelectedRowData = function() {
      selectedRowData = [];
    };
    
    content.init =
      function(_attributes, _opts, _selectedSamples, _selectedGenes,
               _data, _callbacks, _geneData, _dimension, _genePanelMap) {
        initialized = false;
        _selectedSamples.sort();
        allSamplesIds = _selectedSamples;
        selectedSamples = _selectedSamples;
        sequencedSampleIds = _attributes.options.sequencedCases === undefined ? allSamplesIds : _attributes.options.sequencedCases;
        sequencedSampleIds.sort();
        selectedGenes = _selectedGenes;
        chartId_ = _opts.chartId;
        opts = _opts;
        genePanelMap = _genePanelMap;
        data_ = _data;
        geneData_ = _geneData;
        type_ = _attributes.type;
        displayName = _attributes.display_name || 'Table';
        attributes_ = _attributes;
        callbacks_ = _callbacks;
        isMutatedGeneCna = ['mutatedGene', 'cna'].indexOf(type_) !== -1;
        if (!isMutatedGeneCna) {
          dimension = _dimension;
          group = dimension.group();
          initPieTableData();
        }
        initReactTable(true, sequencedSampleIds);
        initialized = true;
      };

    content.update = function(_selectedSampleUIDs, _selectedRows) {
      var selectedMap_ = {};
      var includeMutationCount = false;
      if (_selectedRows !== undefined) {
        selectedRows = _selectedRows;
      }
      if (selectedRows.length === 0) {
        selectedRowData = [];
      }
      _selectedSampleUIDs.sort();
      if ((!initialized) ||
        (!iViz.util.compare(selectedSamples, _selectedSampleUIDs))) {
        initialized = true;
        selectedSamples = _selectedSampleUIDs;
        if (iViz.util.compare(allSamplesIds, selectedSamples)) {
          initReactTable(true, selectedSamples);
        } else {
          _.each(_selectedSampleUIDs, function(caseId) {
            var caseData_ = data_[caseId];
            if (_.isObject(caseData_)) {
              var tempData_ = '';
              switch (type_) {
                case 'mutatedGene':
                  tempData_ = caseData_.mutated_genes;
                  includeMutationCount = true;
                  break;
                case 'cna':
                  tempData_ = caseData_.cna_details;
                  includeMutationCount = false;
                  break;
                default:
                  var category = caseData_[attributes_.attr_id];
                  if (!category) {
                    category = 'NA';
                  }
                  if (!selectedMap_.hasOwnProperty(category)) {
                    selectedMap_[category] = [];
                  }
                  selectedMap_[category].push(caseId);
                  break;
              }
              if (isMutatedGeneCna) {
                _.each(tempData_, function(geneIndex) {
                  if (selectedMap_[geneIndex] === undefined) {
                    selectedMap_[geneIndex] = {};
                    if (includeMutationCount) {
                      selectedMap_[geneIndex].num_muts = 1;
                    }
                    selectedMap_[geneIndex].case_ids = [caseId];
                  } else {
                    if (includeMutationCount) {
                      selectedMap_[geneIndex].num_muts += 1;
                    }
                    selectedMap_[geneIndex].case_ids.push(caseId);
                  }
                });
              }
            }
          });
          
          _.each(selectedMap_, function(content) {
            content.case_uids = iViz.util.unique(content.case_ids);
          });
          
          initReactTable(true, selectedSamples, selectedMap_);
        }
      } else {
        initReactTable(false);
      }
    };

    content.updateGenes = function(genes) {
      selectedGenes = genes;
      initReactTable(false);
    };

    content.updateDataForDownload = function(fileType) {
      if (fileType === 'tsv') {
        initTsvDownloadData();
      }
    };

    content.getCurrentCategories = function() {
      return _.values(categories_);
    };

    function initReactTable(_reloadData, _selectedSampleUids, _selectedMap) {
      if (_reloadData) {
        reactTableData = initReactData(_selectedMap, _selectedSampleUids);
      }
      var _opts = {
        input: reactTableData,
        filter: 'ALL',
        download: 'NONE',
        downloadFileName: 'data.txt',
        showHide: false,
        hideFilter: true,
        scroller: true,
        resultInfo: false,
        groupHeader: false,
        fixedChoose: false,
        uniqueId: 'uniqueId',
        rowHeight: 25,
        tableWidth: opts.width,
        maxHeight: opts.height,
        headerHeight: 26,
        groupHeaderHeight: 40,
        autoColumnWidth: false,
        columnMaxWidth: 300,
        columnSorting: false,
        elementId: chartId_ + '-table',
        sortBy: 'cases',
        selectedRows: selectedRows,
        rowClickFunc: function(data, selected) {
          reactRowClickCallback(data, selected);
          reactSubmitClickCallback();
        },
        // sortBy: 'name',
        // sortDir: 'DESC',
        tableType: type_
      };
      if (isMutatedGeneCna) {
        _opts = _.extend(_opts, {
          rowClickFunc: reactRowClickCallback,
          selectedGene: selectedGenes,
          geneClickFunc: reactGeneClickCallback,
          selectButtonClickCallback: reactSubmitClickCallback
        });
      }

      // Check whether the react table has been initialized
      if (renderedReactTable) {
        // Get sort settings from the initialized react table
        var sort_ = renderedReactTable.getCurrentSort();
        _opts = $.extend(_opts, sort_);
      }

      var testElement = React.createElement(EnhancedFixedDataTableSpecial,
        _opts);

      renderedReactTable = ReactDOM.render(testElement, document.getElementById(chartId_));
    }

    function initRegularTableData() {
      var data = [];
      _.each(categories_, function(category, name) {
        for (var key in category) {
          if (category.hasOwnProperty(key)) {
            var datum = {
              attr_id: key,
              uniqueId: name,
              attr_val: key === 'case_uids' ? category.case_uids.join(',') : category[key]
            };
            data.push(datum);
          }
        }
      });
      reactData_ = data;
    }

    function getColor(key) {
      if (!assignedColors.hasOwnProperty(key)) {
        var _color = colors.shift();
        if (!_color) {
          _color = iViz.util.getRandomColorOutOfLib();
        }
        assignedColors[key] = _color;
      }
      return assignedColors[key];
    }

    function initPieTableData() {
      _.each(group.all(), function(attr, index) {
        labelInitData[attr.key] = {
          attr: attr,
          color: getColor(attr.key),
          id: attr.key,
          index: index
        };
      });
      
    }

    function updateCategories() {
      var _labels = {};
      var _currentSampleSize = 0;
      
      _.each(group.top(Infinity), function(label) {
        var _labelDatum = {};
        var _labelValue = Number(label.value);
        if (_labelValue > 0) {
          _labelDatum.id = labelInitData[label.key].id;
          _labelDatum.index = labelInitData[label.key].index;
          _labelDatum.name = label.key;
          _labelDatum.color = labelInitData[label.key].color;
          _labelDatum.cases = _labelValue;
          _currentSampleSize += _labelValue;
          _labels[_labelDatum.id] = _labelDatum;
        }
      });
      _.each(_labels, function(label) {
        label.caseRate = iViz.util.calcFreq(Number(label.cases), _currentSampleSize);
      });
      categories_ = _labels;
    }

    function mutatedGenesData(_selectedGenesMap, _selectedSampleUids) {
      genePanelMap = window.iviz.datamanager.updateGenePanelMap(genePanelMap, _selectedSampleUids);

      selectedGeneData.length = 0;
      var numOfCases_ = content.getCases().length;

      if (geneData_) {
        _.each(geneData_, function(item, index) {
          var datum = {};
          var freq = 0;
          datum.gene = item.gene;
          if (_selectedGenesMap === undefined) {
            datum.case_uids = item.case_uids;
            datum.cases = datum.case_uids.length;
            datum.uniqueId = index;
            if (typeof genePanelMap[item.gene] !== 'undefined') {
              freq = iViz.util.calcFreq(datum.cases, genePanelMap[item.gene].sampleNum);
            } else {
              freq = iViz.util.calcFreq(datum.cases, numOfCases_);
            }
            switch (type_) {
              case 'mutatedGene':
                datum.numOfMutations = item.num_muts;
                datum.sampleRate = freq;
                break;
              case 'cna':
                datum.cytoband = item.cytoband;
                datum.altType = item.cna;
                datum.altrateInSample = freq;
                break;
              default:
                break;
            }
          } else {
            if (_selectedGenesMap[item.index] === undefined) {
              return;
            }
            datum.case_uids = _selectedGenesMap[item.index].case_uids;
            datum.cases = datum.case_uids.length;
            if (typeof genePanelMap[item.gene] !== 'undefined') {
              freq = iViz.util.calcFreq(datum.cases, genePanelMap[item.gene].sampleNum);
            } else {
              freq = iViz.util.calcFreq(datum.cases, numOfCases_);
            }
            switch (type_) {
              case 'mutatedGene':
                datum.numOfMutations = _selectedGenesMap[item.index].num_muts;
                datum.sampleRate = freq;
                datum.uniqueId = datum.gene;
                break;
              case 'cna':
                datum.cytoband = item.cytoband;
                datum.altType = item.cna;
                datum.altrateInSample = freq;
                datum.uniqueId = datum.gene + '-' + datum.altType;
                break;
              default:
                break;
            }
          }

          if (item.qval === null) {
            datum.qval = '';
          } else {
            var qval = Number(item.qval);
            if (qval === 0) {
              datum.qval = 0;
            } else {
              datum.qval = qval.toExponential(1);
            }
          }
          selectedGeneData.push(datum);
        });
      }
      return selectedGeneData;
    }

    function initReactData(_selectedMap, _selectedSampleUids) {
      attr_ = iViz.util.tableView.getAttributes(type_);
      var result = {
        data: [],
        attributes: attr_
      };

      if (isMutatedGeneCna) {
        var _mutationData = mutatedGenesData(_selectedMap, _selectedSampleUids);
        _.each(_mutationData, function(item) {
          for (var key in item) {
            if (item.hasOwnProperty(key)) {
              var datum = {
                attr_id: key,
                uniqueId: item.uniqueId,
                attr_val: key === 'case_uids' ? item.case_uids.join(',') : item[key]
              };
              result.data.push(datum);
            }
          }
        });
      } else {
        categories_ = {};
        result.attributes[0].display_name = displayName;
        updateCategories(_selectedMap);
        initRegularTableData();
        result.data = reactData_;
      }
      return result;
    }

    function reactSubmitClickCallback() {
      callbacks_.submitClick(selectedRowData);
    }

    function reactRowClickCallback(data, selected) {
      if (selected) {
        selectedRowData.push(data);
      } else {
        selectedRowData = _.filter(selectedRowData, function(item) {
          return (item.uniqueid !== data.uniqueid);
        });
      }
    }

    function reactGeneClickCallback(selectedRow) {
      callbacks_.addGeneClick(selectedRow);
    }

    function initTsvDownloadData() {
      var attrs =
        iViz.util.tableView.getAttributes(type_).filter(function(attr) {
          return attr.attr_id !== 'uniqueId' && (_.isBoolean(attr.show) ? attr.show : true);
        });
      var downloadOpts = {
        fileName: displayName,
        data: ''
      };
      var rowsData;

      if (isMutatedGeneCna) {
        rowsData = selectedGeneData;
      } else {
        rowsData = _.values(categories_);
      }
      rowsData = _.sortBy(rowsData, function(item) {
        return -item.cases;
      });

      if (_.isArray(attrs) && attrs.length > 0) {
        var data = [attrs.map(
          function(attr) {
            if (attr.attr_id === 'name') {
              attr.display_name = displayName;
            }
            return attr.display_name;
          }).join('\t')];

        _.each(rowsData, function(row) {
          var _tmp = [];
          _.each(attrs, function(attr) {
            _tmp.push(row[attr.attr_id] || '');
          });
          data.push(_tmp.join('\t'));
        });

        downloadOpts.data = data.join('\n');
      }
      content.setDownloadData('tsv', downloadOpts);
    }
  };

  iViz.view.component.TableView.prototype =
    new iViz.view.component.GeneralChart('table');

  iViz.util.tableView = (function() {
    var content = {};
    content.compare = function(arr1, arr2) {
      if (arr1.length !== arr2.length) {
        return false;
      }
      for (var i = 0; i < arr2.length; i++) {
        if (arr1.indexOf(arr2[i]) === -1) {
          return false;
        }
      }
      return true;
    };

    content.getAttributes = function(type) {
      var _attr = [];
      switch (type) {
        case 'mutatedGene':
          _attr = [
            {
              attr_id: 'gene',
              display_name: 'Gene',
              datatype: 'STRING',
              column_width: 110
            }, {
              attr_id: 'numOfMutations',
              display_name: '# Mut',
              description: 'Number of mutations in all samples',
              datatype: 'NUMBER',
              column_width: 95
            },
            {
              attr_id: 'cases',
              display_name: '#',
              description: 'Number of samples with mutation',
              datatype: 'NUMBER',
              column_width: 95
            },
            {
              attr_id: 'sampleRate',
              display_name: 'Freq',
              description: '% of samples with this mutation',
              datatype: 'PERCENTAGE',
              column_width: 93
            },
            {
              attr_id: 'case_uids',
              display_name: 'Cases',
              datatype: 'STRING',
              show: false
            },
            {
              attr_id: 'uniqueId',
              display_name: 'uniqueId',
              datatype: 'STRING',
              show: false
            },
            {
              attr_id: 'qval',
              datatype: 'NUMBER',
              display_name: 'MutSig',
              show: false
            }
          ];
          break;
        case 'cna':
          _attr = [
            {
              attr_id: 'gene',
              display_name: 'Gene',
              datatype: 'STRING',
              column_width: 85
            },
            {
              attr_id: 'cytoband',
              display_name: 'Cytoband',
              datatype: 'STRING',
              column_width: 100
            },
            {
              attr_id: 'altType',
              display_name: 'CNA',
              datatype: 'STRING',
              column_width: 55
            },
            {
              attr_id: 'cases',
              display_name: '#',
              description: 'Number of samples with copy number alteration',
              datatype: 'NUMBER',
              column_width: 75
            },
            {
              attr_id: 'altrateInSample',
              display_name: 'Freq',
              description: '% of samples with this copy number alteration',
              datatype: 'PERCENTAGE',
              column_width: 78
            },
            {
              attr_id: 'case_uids',
              display_name: 'Cases',
              datatype: 'STRING',
              show: false
            },
            {
              attr_id: 'uniqueId',
              display_name: 'uniqueId',
              datatype: 'STRING',
              show: false
            },
            {
              attr_id: 'qval',
              datatype: 'NUMBER',
              display_name: 'Gistic',
              show: false
            }
          ];
          break;
        default:
          _attr = [
            {
              attr_id: 'name',
              display_name: 'Unknown',
              datatype: 'STRING',
              column_width: 230
            }, {
              attr_id: 'color',
              display_name: 'Color',
              datatype: 'STRING',
              show: false
            }, {
              attr_id: 'cases',
              display_name: '#',
              datatype: 'NUMBER',
              column_width: 75
            }, {
              attr_id: 'caseRate',
              display_name: 'Freq',
              datatype: 'PERCENTAGE',
              column_width: 90
            }, {
              attr_id: 'case_uids',
              display_name: 'Cases',
              datatype: 'STRING',
              show: false
            }, {
              attr_id: 'uniqueId',
              display_name: 'uniqueId',
              datatype: 'STRING',
              show: false
            }];
          break;
      }
      return _attr;
    };
    return content;
  })();
})(
  window.iViz,
  window.dc,
  window._,
  window.React,
  window.ReactDOM,
  window.EnhancedFixedDataTableSpecial
);

/**
 * Created by Karthik Kalletla on 6/20/16.
 */
'use strict';
(function(Vue, dc, iViz, $, QueryByGeneTextArea, _) {
  Vue.component('tableView', {
    template: '<div id={{chartDivId}} ' +
    ':class="[\'grid-item\', classTableHeight, \'grid-item-w-2\', \'react-table\']" ' +
    ':attribute-id="attributes.attr_id" @mouseenter="mouseEnter" ' +
    '@mouseleave="mouseLeave">' +
    '<chart-operations :show-operations="showOperations" ' +
    ':display-name="displayName" :chart-ctrl="chartInst"' +
    ':has-chart-title="true" :groupid="attributes.group_id" ' +
    ':reset-btn-id="resetBtnId" :chart-id="chartId" :attributes="attributes" ' +
    ':show-survival-icon.sync="showSurvivalIcon"' +
    ':show-download-icon="!failedToInit"' +
    ':filters.sync="attributes.filter"> ' +
    '</chart-operations><div class="dc-chart dc-table-plot" ' +
    'v-show="!showLoad" :class="{\'show-loading-bar\': showLoad}" ' +
    'align="center" id={{chartId}} ></div>' +
    '<div v-show="showLoad" class="progress-bar-parent-div" :class="{\'show-loading-bar\': showLoad}">' +
    '<progress-bar :div-id="loadingBar.divId" :type="loadingBar.type" :disable="loadingBar.disable" ' +
    ':status="loadingBar.status" :opts="loadingBar.opts"></progress-bar></div>' +
    '<div :class="{\'error-init\': failedToInit}" ' +
    'style="display: none;">' +
    '<span class="content">Failed to load data, refresh the page may help</span></div></div>',
    props: [
      'ndx', 'attributes', 'options', 'showedSurvivalPlot'
    ],
    data: function() {
      return {
        chartDivId:
          iViz.util.getDefaultDomId('chartDivId', this.attributes.attr_id),
        resetBtnId:
          iViz.util.getDefaultDomId('resetBtnId', this.attributes.attr_id),
        chartId:
          iViz.util.getDefaultDomId('chartId', this.attributes.attr_id),
        displayName: '',
        showOperations: false,
        chartInst: {},
        failedToInit: false,
        showLoad: true,
        selectedRows: [],
        invisibleDimension: {},
        isMutatedGeneCna: ['mutated_genes', 'cna_details']
          .indexOf(this.attributes.attr_id) !== -1,
        classTableHeight: 'grid-item-h-2',
        madeSelection: false,
        showSurvivalIcon: false,
        genePanelMap: {},
        numOfSurvivalCurveLimit: iViz.opts.numOfSurvivalCurveLimit || 20,
        dataLoaded: false,
        loadedStudies: 0,
        totalNumOfStudies: 0,
        loadingBar :{
          status: 0,
          type: 'percentage',
          divId: iViz.util.getDefaultDomId('progressBarId', this.attributes.attr_id),
          opts: {},
          disable: false
        },
        // this is used to set dc invisibleDimension filters
        // In case of MutatedGeneCna plot this would be case uids
        // and for other talbe charts this would be row uid
        chartFilters:[]
      };
    },
    watch: {
      'attributes.filter': function(newVal) {
        if (newVal.length === 0) {
          this.invisibleDimension.filterAll();
          this.selectedRows = [];
          this.chartFilters = [];
        }
        this.$dispatch('update-filters', true);
      },
      'loadedStudies': function() {
        this.loadingBar.status = (this.loadedStudies / (this.totalNumOfStudies || 1)) * 0.8;
      },
      'showLoad': function(newVal) {
        if (newVal) {
          this.initialInfinityLoadingBar();
        } else {
          this.loadingBar.disable = true;
        }
      },
      'showedSurvivalPlot': function() {
        this.showRainbowSurvival();
      }
    },
    events: {
      'show-loader': function() {
        if (!this.failedToInit && (!this.madeSelection || this.isMutatedGeneCna)) {
          this.showLoad = true;
        }
      },
      'gene-list-updated': function(genes) {
        if(this.isMutatedGeneCna) {
          genes = $.extend(true, [], genes);
          this.chartInst.updateGenes(genes);
        }
      },
      'update-special-charts': function() {
        // Do not update chart if the selection is made on itself
        if (!this.failedToInit) {
          if (this.madeSelection && !this.isMutatedGeneCna) {
            this.madeSelection = false;
          } else if (this.dataLoaded) {
            var attrId =
              this.attributes.group_type === 'patient' ?
                'patient_uid' : 'sample_uid';
            var _selectedCases =
              _.pluck(this.invisibleDimension.top(Infinity), attrId);
            this.chartInst.update(_selectedCases, this.selectedRows);
            this.setDisplayTitle(this.chartInst.getCases().length);
            this.showRainbowSurvival();
            this.showLoad = false;
          }
        }
      },
      'closeChart': function() {
        this.invisibleDimension.dispose();
        this.$dispatch('close', true);
      },
      'addingChart': function(groupId, val) {
        if (this.attributes.group_id === groupId) {
          if (this.attributes.filter.length > 0) {
            if (val) {
              this.invisibleDimension.filterAll();
            } else {
              var filtersMap = {};
              _.each(this.chartFilters, function(filter) {
                if (filtersMap[filter] === undefined) {
                  filtersMap[filter] = true;
                }
              });
              this.invisibleDimension.filterFunction(function(d) {
                return (filtersMap[d] !== undefined);
              });
            }
          }
        }
      },
      getRainbowSurvival: function() {
        var groups = [];
        var categories = this.chartInst.getCurrentCategories();
        var dataForCategories = iViz.util.getCaseIdsGroupByCategories(
          this.attributes.group_type,
          this.invisibleDimension,
          this.attributes.attr_id
        );
        _.each(categories, function(category) {
          if (dataForCategories.hasOwnProperty(category.name)) {
            groups.push({
              name: category.name,
              caseIds: dataForCategories[category.name],
              curveHex: category.color
            });
          }
        });
        this.$dispatch('create-rainbow-survival', {
          attrId: this.attributes.attr_id,
          subtitle: ' (' + this.attributes.display_name + ')',
          groups: groups,
          groupType: this.attributes.group_type
        });
      }
    },
    methods: {
      mouseEnter: function() {
        this.showOperations = true;
      },
      mouseLeave: function() {
        this.showOperations = false;
      },
      submitClick: function(_selectedRowData) {
        var selectedSamplesUnion = [];
        var selectedRowsUids = _.pluck(_selectedRowData, 'uniqueid');

        this.madeSelection = true;

        if (this.isMutatedGeneCna) {
          this.selectedRows = _.union(this.selectedRows, selectedRowsUids);
          this.attributes.filter.push(selectedRowsUids.join(', '))
          _.each(_selectedRowData, function(item) {
            var casesIds = item.case_uids.split(',');
            selectedSamplesUnion = selectedSamplesUnion.concat(casesIds);
          });
          if(this.chartFilters.length === 0) {
            this.chartFilters = selectedSamplesUnion.sort();
          } else {
            this.chartFilters =
              iViz.util.intersection(this.chartFilters, selectedSamplesUnion.sort());
          }
        } else {
          this.selectedRows = selectedRowsUids;
          this.attributes.filter = this.selectedRows;
          this.chartFilters = this.selectedRows
        }
        var filtersMap = {};
        _.each(this.chartFilters, function(filter) {
          if (filtersMap[filter] === undefined) {
            filtersMap[filter] = true;
          }
        });

        this.invisibleDimension.filterFunction(function(d) {
          return (filtersMap[d] !== undefined);
        });
        if (this.isMutatedGeneCna) {
          this.chartInst.clearSelectedRowData();
        }
      },
      addGeneClick: function(clickedRowData) {
        this.$dispatch('manage-gene', clickedRowData.gene);
        QueryByGeneTextArea.addRemoveGene(clickedRowData.gene);
      },
      setDisplayTitle: function(numOfCases) {
        var arr = [];
        if (this.isMutatedGeneCna) {
          arr.push(this.attributes.display_name);
          if (!isNaN(numOfCases)) {
            arr.push(' (' + numOfCases + ' profiled samples)');
          }
        }
        this.displayName = arr.join('');
      },
      processTableData: function(_data) {
        var data = iViz.getGroupNdx(this.attributes.group_id);
        var opts = {
          width: window.iViz.styles.vars.specialTables.width,
          height: window.iViz.styles.vars.specialTables.height,
          chartId: this.chartId
        };

        this.dataLoaded = true;

        this.chartInst.init(this.attributes, opts, this.$root.selectedsampleUIDs,
          this.$root.selectedgenes, data, {
            addGeneClick: this.addGeneClick,
            submitClick: this.submitClick
          }, this.isMutatedGeneCna ? _data.geneMeta : null, this.invisibleDimension, this.genePanelMap);

        var attrId =
          this.attributes.group_type === 'patient' ?
            'patient_uid' : 'sample_uid';
        var _selectedCases =
          _.pluck(this.invisibleDimension.top(Infinity), attrId);
        if (_selectedCases.length > 0) {
          this.chartInst.update(_selectedCases, this.selectedRows);
          this.showRainbowSurvival();
        }

        this.setDisplayTitle(this.chartInst.getCases().length);
        if (!this.isMutatedGeneCna &&
          Object.keys(this.attributes.keys).length <= 3) {
          this.classTableHeight = 'grid-item-h-1';
          this.attributes.layout[1] = 2;
          this.attributes.layout[2] = 'h';
        }
        this.showLoad = false;
      },
      showRainbowSurvival: function() {
        var categories = this.chartInst.getCurrentCategories();
        if (this.showedSurvivalPlot && !this.isMutatedGeneCna && _.isArray(categories) &&
          categories.length > 1 &&
          categories.length <= this.numOfSurvivalCurveLimit) {
          this.showSurvivalIcon = true;
        } else {
          this.showSurvivalIcon = false;
        }
      },
      initialInfinityLoadingBar: function() {
        this.loadingBar.type = 'infinite';
      }
    },
    ready: function() {
      var _self = this;
      var callbacks = {};
      var attrId = this.attributes.attr_id;

      if (this.isMutatedGeneCna) {
        attrId = this.attributes.group_type === 'patient' ?
          'patient_uid' : 'sample_uid';
      }

      this.invisibleDimension = this.ndx.dimension(function(d) {
        if (iViz.util.strIsNa(d[attrId], false)) {
          d[attrId] = 'NA';
        }
        return d[attrId];
      });

      callbacks.addGeneClick = this.addGeneClick;
      callbacks.submitClick = this.submitClick;
      _self.chartInst = new iViz.view.component.TableView();
      _self.chartInst.setDownloadDataTypes(['tsv']);
      if (_self.isMutatedGeneCna) {
        var progressBarText = '';
        if (_self.attributes.attr_id === 'mutated_genes') {
          _self.totalNumOfStudies = Object.keys(iviz.datamanager.mutationProfileIdsMap).length;
          progressBarText = 'mutated genes (';
        } else {
          _self.totalNumOfStudies = Object.keys(iviz.datamanager.cnaProfileIdsMap).length;
          progressBarText = 'copy number alteration genes (';
        }

        _self.loadingBar.opts = {
          step: function(state, bar) {
            bar.setText('Loading ' + progressBarText + Math.round(bar.value() * 100) + '%)');
          }
        };

        $.when(iViz.getTableData(_self.attributes.attr_id, function() {
          _self.loadedStudies++;
        })).then(function(_tableData) {
          $.when(window.iviz.datamanager.getGenePanelMap(_tableData.allGenes, _tableData.allSamples))
            .then(function(_genePanelMap) {
              // create gene panel map
              _self.loadingBar.status = 1;
              setTimeout(function() {
                this.dataLoaded = true;
                _self.genePanelMap = _genePanelMap;
                _self.processTableData(_tableData);
              }, 0);
            }, function() {
              _self.failedToInit = true;
              _self.showLoad = false;
            });
        }, function() {
          _self.setDisplayTitle();
          if (!_self.isMutatedGeneCna &&
            Object.keys(_self.attributes.keys).length <= 3) {
            _self.classTableHeight = 'grid-item-h-1';
          }
          _self.failedToInit = true;
          _self.showLoad = false;
          _self.initialInfinityLoadingBar();
        });
      } else {
        _self.processTableData();
      }

      this.showRainbowSurvival();
      this.$dispatch('data-loaded', this.attributes.group_id, this.chartDivId);
    }
  });
})(
  window.Vue,
  window.dc,
  window.iViz,
  window.$ || window.jQuery,
  window.QueryByGeneTextArea,
  window._
);

/**
 * Created by kalletlak on 7/8/16.
 */
'use strict';
(function(Vue, dc, iViz, _, $) {
  var headerCaseSelectCustomDialog = {
    // Since we're only creating one modal, give it an ID so we can style it
    id: 'iviz-case-select-custom-dialog',
    content: {
      text: '',
      title: {
        text: 'Custom case selection',
        button: true
      }
    },
    position: {
      my: 'center', // ...at the center of the viewport
      at: 'top center',
      target: $(window),
      adjust: {
        y: 400
      }
    },
    show: {
      event: 'click', // Show it on click...
      solo: true // ...and hide all other tooltips...
    },
    hide: false,
    style: 'qtip-light qtip-rounded qtip-wide'
  };
  Vue.component('customCaseInput', {
    template: '<div style="display: inline-flex"><input type="button" id="custom-case-input-button" ' +
    'class="iviz-header-button" value="Select cases by IDs"/>' +
    '<div class="iviz-hidden" id="iviz-case-select-custom-dialog">' +
    '<b>Please input IDs (one per line)</b></br>' +
    '<span @click="updateCaseIds()" ' +
    'style="text-decoration: underline;cursor: pointer">' +
    'Use selected samples/patients</span><br/><br/>' +
    '<textarea rows="20" cols="50" ' +
    'id="iviz-case-select-custom-input" v-model="casesIdsList"></textarea>' +
    '<br/><label><input type="radio" v-model="caseSelection" ' +
    'value="sample" checked @click="clearCaseIds(\'sample\')">By sample ID</label><label><input type="radio" ' +
    'v-model="caseSelection" value="patient" @click="clearCaseIds(\'patient\')">' +
    'By patient ID</label><button type="button" @click="setCasesSelection()" ' +
    'style="float: right;">Select</button></div></div>',
    props: {
      stats: {
        type: Object
      },
      updateStats: {
        type: Boolean,
        default: false
      }
    },
    data: function() {
      return {
        caseSelection: '',
        tooltip: '',
        casesIdsList: ''
      };
    },
    events: {},
    methods: {
      setCasesSelection: function() {
        var caseIds = this.casesIdsList.trim().split(/\n/);
        this.$dispatch('set-selected-cases', this.caseSelection, _.uniq(caseIds));
        this.tooltip.qtip('api').hide();
      },
      clearCaseIds: function() {
        this.casesIdsList = '';
      },
      getCaseIdsList: function(type) {
        var cases = [];
        _.each(this.stats.studies, function(t) {
          var targetGroup = type === 'patient' ? t.patients : t.samples;
          _.each(targetGroup, function(caseId) {
            cases.push(t.id + ':' + caseId);
          });
        });
        return cases.join('\n');;
      },
      updateCaseIds: function(type) {
        this.updateStats = true;
        this.$nextTick(function() {
          if (!type) {
            type = this.caseSelection;
          }
          this.casesIdsList = this.getCaseIdsList(type)
        });
      }
    },
    ready: function() {
      var self_ = this;
      var _customDialogQtip =
        $.extend(true, {}, headerCaseSelectCustomDialog);
      _customDialogQtip.position.target = $(window);
      _customDialogQtip.content.text = $('#iviz-case-select-custom-dialog');
      _customDialogQtip.events = {
        hide: function() {
          self_.casesIdsList = '';
        }
      };
      self_.tooltip = $('#custom-case-input-button').qtip(_customDialogQtip);
    }
  });
})(
  window.Vue,
  window.dc,
  window.iViz,
  window._,
  window.$ || window.jQuery
);

/**
 * Created by Hongxin Zhang on 4/24/17.
 */
'use strict';
(function(Vue) {
  Vue.component('error', {
    template: '<div id="{{containerId}}" >' +
    '<span class="className">{{{message}}}</span>' +
    '</div>',
    props: {
      containerId: {
        type: String,
        default: new Date().getTime() + '-error'
      },
      message: {
        type: String,
        default: ''
      },
      className: {
        type: String,
        default: 'error-message'
      }
    }
  });
})(window.Vue);


'use strict';
(function(Vue, $, vcSession, iViz, _) {
  Vue.component('virtualStudy', {
    template:
    '<div class="virtual-study">' +
    '<div class="virtual-study-btn">' +
    '<i class="fa fa-bookmark" aria-hidden="true"></i></div></div>',
    props: {
      selectedSamplesNum: {
        type: Number,
        default: 0
      },
      stats: {
        type: Object
      },
      updateStats: {
        type: Boolean,
        default: false
      },
      showSaveButton: {
        type: Boolean,
        default: false
      },
      showShareButton: {
        type: Boolean,
        default: false
      },
      createdQtip: {
        type: Boolean,
        default: false
      }
    },
    watch: {
      showSaveButton: function(){
        this.createQtip()
      }
    },
    data: function() {
      return {
        savedVS: null
      };
    },
    methods: {
      styleDisplay: function(el, action) {
        el.css('display', action);
      },
      showDialog: function(tooltip) {
        this.styleDisplay(tooltip.find('.dialog'), 'block');
      },
      hideDialog: function(tooltip) {
        this.styleDisplay(tooltip.find('.dialog'), 'none');
      },
      showLoading: function(tooltip) {
        this.styleDisplay(tooltip.find('.saving'), 'block');
      },
      hideLoading: function(tooltip) {
        this.styleDisplay(tooltip.find('.saving'), 'none');
      },
      showShared: function(tooltip) {
        this.styleDisplay(tooltip.find('.shared'), 'block');
      },
      hideShared: function(tooltip) {
        this.styleDisplay(tooltip.find('.shared'), 'none');
      },
      showSaved: function(tooltip) {
        this.styleDisplay(tooltip.find('.saved'), 'block');
      },
      hideSaved: function(tooltip) {
        this.styleDisplay(tooltip.find('.saved'), 'none');
      },
      showFailedInfo: function(tooltip) {
        this.styleDisplay(tooltip.find('.failed'), 'block');
      },
      hideFailedInfo: function(tooltip) {
        this.styleDisplay(tooltip.find('.failed'), 'none');
      },
      showAfterClipboard: function(tooltip) {
        this.styleDisplay(tooltip.find('.after-clipboard'), 'block');
      },
      hideAfterClipboard: function(tooltip) {
        this.styleDisplay(tooltip.find('.after-clipboard'), 'none');
      },
      updateFailedMessage: function(tooltip, message) {
        tooltip.find('.failed .message')
          .html(message)
      },
      updateSavedMessage: function(tooltip, message) {
        tooltip.find('.saved .message')
          .html(message)
      },
      updateSavingMessage: function(tooltip, message) {
        tooltip.find('.saving .message')
          .html(message)
      },
      updateOriginStudiesDescription: function(tooltip, message) {
        tooltip.find('.origin-studies-frame')
          .html(message);
      },
      disableBtn: function(el, action) {
        el.attr('disabled', action);
      },
      disableSaveCohortBtn: function(tooltip) {
        this.disableBtn(tooltip.find('.save-cohort'), true);
      },
      enableSaveCohortBtn: function(tooltip) {
        this.disableBtn(tooltip.find('.save-cohort'), false);
      },
      disableShareCohortBtn: function(tooltip) {
        this.disableBtn(tooltip.find('.share-cohort'), true);
      },
      enableShareCohortBtn: function(tooltip) {
        this.disableBtn(tooltip.find('.share-cohort'), false);
      },
      getFilteredOriginStudies: function() {
        var selectedStudiesDisplayName = window.iviz.datamanager.getCancerStudyDisplayName(this.stats.origin);
        var filteredOriginStudies = {
          studies: [],
          count: 0
        };
        var selectedStudies = {};
        _.each(this.stats.studies, function(study) {
          selectedStudies[study.id] = study.samples;
          filteredOriginStudies.count += study.samples.length;
        });
        filteredOriginStudies.studies = this.stats.origin.map(function(studyId) {
          var _studyData = iviz.datamanager.getStudyById(studyId);
          var _count = 0;
          if (_.isObject(_studyData) && _studyData.studyType === 'vs') {
            _.each(_studyData.data.studies, function(_study) {
              _count += _.intersection(_study.samples, selectedStudies[_study.id]).length;
            });
          } else {
            _count = selectedStudies.hasOwnProperty(studyId) ? selectedStudies[studyId].length : 0;
          }
          return {
            id: studyId,
            name: selectedStudiesDisplayName[studyId],
            count: _count
          };
        }).filter(function(t) {
          return t.count > 0;
        });
        return filteredOriginStudies;
      },
      getDefaultVSDescription: function(filteredOriginStudies) {
        var self = this;
        var filters = {};
        var vm = iViz.vue.manage.getInstance();
        _.each(self.stats.filters, function(_filters, _type) {
          _.each(_filters, function(filter, attrId) {
            filters[attrId] = {
              filter: filter
            };
          });
        });
        var attrs = vm.getChartsByAttrIds(Object.keys(filters));
        _.each(attrs, function(attr) {
          filters[attr.attr_id].attrId = attr.attr_id;
          filters[attr.attr_id].attrName = attr.display_name;
          filters[attr.attr_id].viewType = attr.view_type;
        });

        if (filters.hasOwnProperty(vm.customfilter.id)) {
          filters[vm.customfilter.id].attrId = vm.customfilter.id;
          filters[vm.customfilter.id].attrName = vm.customfilter.display_name;
          filters[vm.customfilter.id].viewType = 'custom';
        }
        return vcSession.utils.generateVSDescription(filteredOriginStudies, _.values(filters));
      },
      saveCohort: function() {
        var _self = this;
        _self.updateStats = true;
        _self.$nextTick(function() {
          _self.addNewVC = true;
        });
      },
      createQtip: function() {
        var self_ = this;
        var previousSelection = {};
        $('.virtual-study').qtip(iViz.util.defaultQtipConfig(
          (self_.showSaveButton ? 'Save/' : '') + 'Share Virtual Study'));
        $('.virtual-study-btn').qtip({
          style: {
            classes: 'qtip-light qtip-rounded qtip-shadow ' +
            'iviz-virtual-study-btn-qtip'
          },
          show: false,
          hide: false,
          position: {
            my: 'top center',
            at: 'bottom center',
            viewport: $(window)
          },
          events: {
            render: function(event, api) {
              var tooltip = $('.iviz-virtual-study-btn-qtip .qtip-content');
              tooltip.find('.save-cohort').click(function() {
                self_.hideDialog(tooltip);
                self_.showLoading(tooltip);
                self_.updateSavingMessage(tooltip, 'Saving virtual study...');
                self_.hideSaved(tooltip)

                api.reposition();

                var cohortName = tooltip.find('.cohort-name').val() ?
                  tooltip.find('.cohort-name').val() : tooltip.find('.cohort-name').attr('placeholder');
                var cohortDescription =
                  tooltip.find('textarea').val();
                if (_.isObject(vcSession)) {
                  self_.updateStats = true;
                  self_.$nextTick(function() {
                    var _selectedSamplesNum = 0;
                    if (_.isObject(self_.stats.studies)) {
                      _.each(self_.stats.studies, function(studyCasesMap) {
                        _selectedSamplesNum += studyCasesMap.samples.length;
                      });
                      self_.selectedSamplesNum = _selectedSamplesNum;
                    }

                    vcSession.events.saveCohort(self_.stats,
                      cohortName, cohortDescription || '', true)
                      .done(function(response) {
                        self_.savedVS = response;
                        self_.updateSavedMessage(tooltip, '<span>Virtual study <i>' + cohortName +
                          '</i> is saved.</span>' +
                          '<div class="btn-group" role="group">' +
                          '<button type="button" class="btn btn-default btn-xs view-vs">View</button>' +
                          '<button type="button" class="btn btn-default btn-xs query-vs">Query</button>' +
                          '</div>');
                        tooltip.find('.saved .message').find('a').click(function(event) {
                          event.preventDefault();
                          window.open(window.cbioURL + 'study?id=' +
                            self_.savedVS.id);
                        });
                        tooltip.find('.saved .message .view-vs').click(function(event) {
                          event.preventDefault();
                          window.open(window.cbioURL + 'study?id=' +
                            self_.savedVS.id);
                        });
                        tooltip.find('.saved .message .query-vs').click(function(event) {
                          event.preventDefault();
                          window.open(window.cbioURL + 'index.do?cancer_study_id=' + self_.savedVS.id)
                        });
                      })
                      .fail(function() {
                        self_.hideSaved(tooltip);
                        self_.updateFailedMessage(tooltip,
                          '<i class="fa fa-exclamation-triangle"></i>' +
                          '<span class="left-space">' +
                          'Failed to save virtual study, ' +
                          'please try again later.</span>');
                        self_.showFailedInfo(tooltip);
                      })
                      .always(function() {
                        self_.showSaved(tooltip);
                        self_.hideLoading(tooltip);
                        self_.hideDialog(tooltip);

                        tooltip.find('.cohort-name').val('');
                        tooltip.find('textarea').val('');

                        api.reposition();
                      });
                  });
                }
              });
              tooltip.find('.share-cohort').click(function() {
                self_.hideDialog(tooltip);
                self_.hideShared(tooltip);
                self_.showLoading(tooltip);
                self_.updateSavingMessage(tooltip, 'Generating the virtual study link');

                var cohortName = tooltip.find('.cohort-name').val() ?
                  tooltip.find('.cohort-name').val() : tooltip.find('.cohort-name').attr('placeholder');
                var cohortDescription =
                  tooltip.find('textarea').val();
                if (_.isObject(vcSession)) {
                  self_.updateStats = true;

                  self_.$nextTick(function() {
                    if (_.isObject(self_.stats.studies)) {
                      // When a user clicks copy, it will trigger saving the current virtual cohort and return the url 
                      // to the user. When a user want to see the cohort url, he/she needs to click Share button. 
                      // We always show the url to user but we don't need to same virtual cohort every time 
                      // if it is same with the previous saved cohort.
                      var currentSelection = cohortName + cohortDescription + JSON.stringify(self_.stats.studies) + JSON.stringify(self_.stats);

                      if (currentSelection !== previousSelection) {
                        vcSession.events.saveCohort(self_.stats,
                          cohortName, cohortDescription || '', false)
                          .done(function(response) {
                            self_.savedVC = response;
                            tooltip.find('.cohort-link').html(
                              '<a class="virtual-study-link" href="' + window.cbioURL +
                              'study?id=' + self_.savedVC.id + '" onclick="window.open(\'' +
                              window.cbioURL + 'study?id=' + self_.savedVC.id + '\')">' +
                              window.cbioURL + 'study?id=' + self_.savedVC.id + '</a>');

                            self_.hideLoading(tooltip);
                            self_.showShared(tooltip);
                            previousSelection = currentSelection;
                          })
                          .fail(function() {
                            self_.hideLoading(tooltip);
                            self_.hideDialog(tooltip);
                            self_.updateFailedMessage(tooltip,
                              '<i class="fa fa-exclamation-triangle"></i>' +
                              '<span class="left-space">' +
                              'Failed to share virtual study, ' +
                              'please try again later.</span>')
                            self_.showFailedInfo(tooltip);
                          });
                      } else {
                        self_.hideLoading(tooltip);
                        self_.showShared(tooltip);
                      }
                    }
                  });
                }
              });
              tooltip.find('.copy-cohort-btn').click(function() {
                self_.hideDialog(tooltip);
                self_.hideShared(tooltip);
                self_.hideLoading(tooltip);

                api.reposition();

                // Copy virtual study link to clipboard
                var temp = $("<input>");
                $("body").append(temp);
                temp.val(tooltip.find('.virtual-study-link').attr('href')).select();
                // execCommand('copy') allows to run commands to copy the contents of selected editable region.
                document.execCommand("copy");
                // Check if users copy url successfully
                if (temp.val() === tooltip.find('.virtual-study-link').attr('href')) {
                  self_.hideShared(tooltip);
                  self_.showAfterClipboard(tooltip);
                  api.reposition();
                }
                temp.remove();
              });
              this.createdQtip = true;
            },
            show: function() {
              var tooltip = $('.iviz-virtual-study-btn-qtip .qtip-content');
              var showThis = this;
              self_.updateSavingMessage(tooltip, 'Loading');
              self_.updateStats = true;
              self_.$nextTick(function() {
                self_.updateStats = false;
                tooltip.find('.cohort-name').val('');
                tooltip.find('.cohort-name')
                  .attr('placeholder', vcSession.utils.VSDefaultName(self_.stats.studies));
                self_.hideDialog(tooltip);
                self_.showLoading(tooltip);
                var filteredOriginStudies = this.getFilteredOriginStudies();
                var defaultVSDescription = self_.getDefaultVSDescription(filteredOriginStudies);
                tooltip.find('textarea').val(defaultVSDescription);

                if (filteredOriginStudies.studies.length > 0) {
                  filteredOriginStudies.studies.map(function(study) {
                    var studyMetaData = iviz.datamanager.getStudyById(study.id);
                    study.description = studyMetaData.studyType === 'vs' ? studyMetaData.data.description : studyMetaData.description;
                    return study;
                  });
                  self_.updateOriginStudiesDescription(tooltip, cbio.util.getOriginStudiesDescriptionHtml(filteredOriginStudies.studies));
                  $('.origin-studies-frame [data-toggle="collapse"]').click(function(a, b) {
                    $($(this).attr('data-target')).collapse('toggle');
                  });
                  $('.origin-studies-frame .panel-title a').click(function() {
                    window.open($(this).attr('href'));
                  });
                }
                self_.showDialog(tooltip);
                self_.hideLoading(tooltip);
                self_.hideShared(tooltip);
                self_.hideSaved(tooltip);
                self_.hideFailedInfo(tooltip);
                self_.hideAfterClipboard(tooltip);

                // Tell the tip itself to not bubble up clicks on it
                $($(showThis).qtip('api').elements.tooltip).click(function() {
                  return false;
                });
              });
            },
            visible: function() {
              // Tell the document itself when clicked to hide the tip and then unbind
              // the click event (the .one() method does the auto-unbinding after one time)
              $(document).one("click", function() {
                $(".virtual-study-btn").qtip('hide');
              });
            }
          },
          content: '<div><div class="dialog"><div class="input-group">' +
          '<input type="text" class="form-control cohort-name" ' +
          'placeholder="Virtual study Name"> <span class="input-group-btn">' +
          (self_.showSaveButton ? '<button class="btn btn-default save-cohort" type="button">Save</button>' : '') +
          (self_.showShareButton ? '<button class="btn btn-default share-cohort" type="button">Share</button>' : '') +
          '</span>' +
          '</div><div>' +
          '<textarea classe="form-control" rows="10" ' +
          'placeholder="Virtual study description (Optional)"></textarea>' +
          '<div class="origin-studies-frame"></div>' +
          '</div></div>' +
          '<div class="saving" style="display: none;">' +
          '<i class="fa fa-spinner fa-spin"></i>' +
          '<span class="message"></span></div>' +
          '<div class="saved" style="display: none;">' +
          '<span class="message"></span>' +
          '</div>' +
          '<div class="shared" style="display: none;"><span class="cohort-link"></span>' +
          '<button class="btn btn-default btn-xs copy-cohort-btn" ' +
          'type="button">Copy</button></div>' +
          '<div class="after-clipboard" style="display: none;">' +
          '<span class="message">The URL has been copied to clipboard.</span>' +
          '</div>' +
          '<div class="failed" style="display: none;">' +
          '<span class="message"></span></div>' +
          '</div>'
        });
        $('.virtual-study-btn').click(function() {
          $('.virtual-study-btn').qtip('show');
        });
      }
    },
    ready: function() {
      this.createQtip();
    }
  });
})(window.Vue,
  window.$ || window.jQuery, window.vcSession, window.iViz, window._);
/**
 * @author Hongxin Zhang on 11/15/17.
 */
'use strict';
(function(Vue, ProgressBar) {
  Vue.component('progressBar', {
    template:
      '<div id="{{divId}}" class="study-view-progress-bar"></div>',
    props: {
      type: {
        type: String,
        default: 'percentage'
      },
      disable: {
        type: Boolean,
        default: false
      },
      status: {
        type: Number,
        default: 0
      },
      divId: {
        type: String
      },
      opts: {
        default: function() {
          return {};
        },
        type: Object
      }
    },
    data: function() {
      return {
        intervals: {}
      }
    },
    methods: {
      initLine: function() {
        var _self = this;
        var opts = _.extend({
          strokeWidth: 4,
          easing: 'easeInOut',
          duration: 1400,
          color: '#2986e2',
          trailColor: '#eee',
          trailWidth: 1,
          svgStyle: {width: '100%', height: '100%'},
          text: {
            style: {
              // Text color.
              // Default: same as stroke color (options.color)
              color: '#000',
              'text-align': 'center',
              transform: null
            },
            autoStyleContainer: false
          },
          step: function(state, bar) {
            bar.setText(Math.round(bar.value() * 100) + ' %');
          }
        }, _self.opts);
        if (_self.bar) {
          _self.bar.destroy();
        }
        _self.bar = new ProgressBar.Line('#' + _self.divId, opts);
        _self.bar.animate(_self.status);
      },
      cancelAllIntervals: function() {
        _.each(this.intervals, function(interval) {
          window.clearInterval(interval);
          interval = null;
        })
      },
      cancelInterval: function(type) {
        if (this.intervals[type]) {
          window.clearInterval(this.intervals[type]);
          this.intervals[type] = null;
        }
      },
      initialInterval: function() {
        var self = this;
        if (self.type === 'percentage') {
          self.intervals.percentage = window.setInterval(function() {
            self.status += Math.floor(Math.random() * 5) * 0.01;
          }, self.opts.duration || 500);
        } else if (self.type = 'infinite') {
          self.intervals.infinite = window.setInterval(function() {
            self.status += 0.5;
          }, self.opts.duration || 500);
        }
      }
    },
    watch: {
      'status': function(newVal, oldVal) {
        if (this.type === 'percentage' && newVal >= 0.9) {
          this.cancelInterval('percentage');
        }
        if (newVal > this.bar.value()) {
          this.bar.animate(newVal);
        }
      },
      'opts': function() {
        this.initLine();
      },
      'disable': function() {
        this.cancelAllIntervals();
      },
      'type': function(newVal) {
        this.cancelAllIntervals();
        this.initialInterval();
        this.disable = false;
        if (newVal === 'infinite') {
          this.opts = {
            duration: 300,
            step: function(state, bar) {
              bar.setText('Loading...');
            }
          };
          this.status = 0.5;
        }
      }
    },
    ready: function() {
      var self = this;
      self.initLine();
      self.initialInterval();
    }
  });
})(window.Vue,
  window.ProgressBar);