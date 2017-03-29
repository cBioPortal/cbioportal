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
                }

                // handle each symbol found
                for(var j=0; j < symbolResults.length; j++) {
                    var valid = handleSymbol(symbolResults[j])
                    if(!valid) {
                        allValid = false;
                    }
                }
            })
            .fail(function(xhr,  textStatus, errorThrown){
                addNotification("There was a problem: "+errorThrown, "danger");
                allValid=false;
            })
            .always(function(){
                // if not all valid, focus on the gene array for focusin trigger
                if(!allValid) $(geneAreaId).focus();
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
            self.replaceAreaValue($(this).attr("name"), $(this).attr("value"));

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
    toMainPage: function(studyId, selectedCases) {
      submitForm(window.cbioURL + 'index.do', {
        'cancer_study_id': studyId,
        'case_ids': selectedCases.join(' '),
        'case_set_id': -1
      });
    },
    toQueryPage: function(studyId, selectedCases,
                          selectedGenes, mutationProfileId, cnaProfileId) {
      submitForm(window.cbioURL + 'index.do', {
        cancer_study_id: studyId,
        case_ids: selectedCases.join(' '),
        case_set_id: -1,
        gene_set_choice: 'user-defined-list',
        gene_list: selectedGenes,
        cancer_study_list: studyId,
        Z_SCORE_THRESHOLD: 2.0,
        genetic_profile_ids_PROFILE_MUTATION_EXTENDED: mutationProfileId,
        genetic_profile_ids_PROFILE_COPY_NUMBER_ALTERATION: cnaProfileId,
        clinical_param_selection: null,
        data_priority: 0,
        tab_index: 'tab_visualize',
        Action: 'Submit'
      });
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

    getCleanGeneArray: function(){
        return $.unique(this.removeEmptyElements(this.get("geneString").toUpperCase().split(/[^a-zA-Z0-9-]/))).reverse();
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
        $(areaId).bind('input propertychange', updateModel);

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
var iViz = (function(_, $, cbio, QueryByGeneUtil, QueryByGeneTextArea) {
  var data_;
  var vm_;
  var tableData_ = [];
  var groupFiltersMap_ = {};
  var groupNdxMap_ = {};
  var hasSampleAttrDataMap_ = {};
  var hasPatientAttrDataMap_ = {};
  var patientData_;
  var sampleData_;
  var charts = {};
  var styles_ = {
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
  };

  function getAttrVal(attrs, arr) {
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
  }

  return {

    init: function(_rawDataJSON, opts) {
      vm_ = iViz.vue.manage.getInstance();

      data_ = _rawDataJSON;

      if (_.isObject(opts)) {
        if (_.isObject(opts.styles)) {
          styles_ = _.extend(styles_, opts.styles);
        }
      }

      hasPatientAttrDataMap_ = data_.groups.patient.hasAttrData;
      hasSampleAttrDataMap_ = data_.groups.sample.hasAttrData;
      patientData_ = data_.groups.patient.data;
      sampleData_ = data_.groups.sample.data;

      var _patientIds = _.keys(data_.groups.patient.data_indices.patient_id);
      var _sampleIds = _.keys(data_.groups.sample.data_indices.sample_id);

      var chartsCount = 0;
      var patientChartsCount = 0;
      var groupAttrs = [];
      var group = {};
      var groups = [];

      // group.data = data_.groups.patient.data;
      group.type = 'patient';
      group.id = vm_.groupCount;
      group.selectedcases = [];
      group.hasfilters = false;
      _.each(data_.groups.patient.attr_meta, function(attrData) {
        attrData.group_type = group.type;
        if (chartsCount < 20 && patientChartsCount < 10) {
          if (attrData.show) {
            attrData.group_id = group.id;
            groupAttrs.push(attrData);
            chartsCount++;
            patientChartsCount++;
          }
        } else {
          attrData.show = false;
        }
        charts[attrData.attr_id] = attrData;
      });
      group.attributes = groupAttrs;
      groups.push(group);

      groupAttrs = [];
      group = {};
      vm_.groupCount += 1;
      // group.data = data_.groups.sample.data;
      group.type = 'sample';
      group.id = vm_.groupCount;
      group.selectedcases = [];
      group.hasfilters = false;
      _.each(data_.groups.sample.attr_meta, function(attrData) {
        attrData.group_type = group.type;
        if (chartsCount < 20) {
          if (attrData.show) {
            attrData.group_id = group.id;
            groupAttrs.push(attrData);
            chartsCount++;
          }
        } else {
          attrData.show = false;
        }
        charts[attrData.attr_id] = attrData;
      });
      vm_.groupCount += 1;
      group.attributes = groupAttrs;
      groups.push(group);
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
        vm_.selectedsamples = _sampleIds;
        vm_.selectedpatients = _patientIds;
        // vm_.patientmap = data_.groups.group_mapping.patient.sample;
        // vm_.samplemap = data_.groups.group_mapping.sample.patient;
        vm_.groups = groups;
        vm_.charts = charts;
        vm_.$nextTick(function() {
          _self.fetchCompleteData('patient');
          _self.fetchCompleteData('sample');
        });
      });
    }, // ---- close init function ----groups
    createGroupNdx: function(group) {
      var def = new $.Deferred();
      var _caseAttrId = group.type === 'patient' ? 'patient_id' : 'sample_id';
      var _attrIds = [_caseAttrId, 'study_id'];
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
      var hasAttrDataMap = isPatientAttributes ? hasPatientAttrDataMap_ : hasSampleAttrDataMap_;
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
        var _data = isPatientAttributes ? patientData_ : sampleData_;
        var toReturn = [];
        _.each(_data, function(_caseData, _index) {
          toReturn[_index] = _.pick(_caseData, updatedAttrIds);
        });
        def.resolve(toReturn);
      });
      if (attrDataToGet.length > 0) {
        $.when(this.updateDataObject(type, attrDataToGet)).then(function() {
          _def.resolve();
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
        $.when(iViz.getDataWithAttrs(_type, _attrIds)).then(function() {
          _def.resolve();
        });
      } else {
        $.when(window.iviz.datamanager.getClinicalData(_attrIds, (_type === 'patient'))).then(function() {
          _def.resolve();
        });
      }
      return _def.promise();
    },
    updateDataObject: function(type, attrIds) {
      var def = new $.Deferred();
      var self_ = this;
      var isPatientAttributes = (type === 'patient');
      var _data = isPatientAttributes ? patientData_ : sampleData_;
      var hasAttrDataMap = isPatientAttributes ?
        hasPatientAttrDataMap_ : hasSampleAttrDataMap_;

      $.when(
        window.iviz.datamanager.getClinicalData(attrIds, isPatientAttributes))
        .then(function(clinicalData) {
          var _caseIdToClinDataMap = {};
          var idType = isPatientAttributes ? 'patient_id' : 'sample_id';
          _.each(clinicalData, function(_clinicalAttributeData, _attrId) {
            var selectedAttrMeta = charts[_attrId];

            hasAttrDataMap[_attrId] = '';
            selectedAttrMeta.keys = {};
            selectedAttrMeta.numOfDatum = 0;

            _.each(_clinicalAttributeData, function(_dataObj) {
              if (_caseIdToClinDataMap[_dataObj[idType]] === undefined) {
                _caseIdToClinDataMap[_dataObj[idType]] = {};
              }
              _caseIdToClinDataMap[_dataObj[idType]][_dataObj.attr_id] =
                _dataObj.attr_val;

              if (!selectedAttrMeta.keys
                  .hasOwnProperty(_dataObj.attr_val)) {
                selectedAttrMeta.keys[_dataObj.attr_val] = 0;
              }
              ++selectedAttrMeta.keys[_dataObj.attr_val];
              ++selectedAttrMeta.numOfDatum;
            });

            // if (selectedAttrMeta.datatype === 'STRING' &&
            //   Object.keys(selectedAttrMeta.keys).length > 20) {
            //   var caseIds = isPatientAttributes ?
            //     Object.keys(data_.groups.group_mapping.patient.sample) :
            //     Object.keys(data_.groups.group_mapping.sample.patient);
            //
            //   selectedAttrMeta.view_type = 'table';
            //   selectedAttrMeta.type = 'pieLabel';
            //   selectedAttrMeta.options = {
            //     allCases: caseIds,
            //     sequencedCases: caseIds
            //   };
            // }
          });
          var type = isPatientAttributes ? 'patient' : 'sample';
          var caseIndices = self_.getCaseIndices(type);
          _.each(_caseIdToClinDataMap, function(_clinicalData, _caseId) {
            var _caseIndex = caseIndices[_caseId];
            _.extend(_data[_caseIndex], _clinicalData);
          });

          def.resolve();
        });
      return def.promise();
    },
    extractMutationData: function(_mutationData) {
      var _mutGeneMeta = {};
      var _mutGeneMetaIndex = 0;
      var _sampleDataIndicesObj = this.getCaseIndices('sample');
      _.each(_mutationData, function(_mutGeneDataObj) {
        var _geneSymbol = _mutGeneDataObj.gene_symbol;
        var _uniqueId = _geneSymbol;
        _.each(_mutGeneDataObj.caseIds, function(_caseId) {
          if (_sampleDataIndicesObj[_caseId] !== undefined) {
            var _caseIdIndex = _sampleDataIndicesObj[_caseId];
            if (_mutGeneMeta[_uniqueId] === undefined) {
              _mutGeneMeta[_uniqueId] = {};
              _mutGeneMeta[_uniqueId].gene = _geneSymbol;
              _mutGeneMeta[_uniqueId].num_muts = 1;
              _mutGeneMeta[_uniqueId].caseIds = [_caseId];
              _mutGeneMeta[_uniqueId].qval = (window.iviz.datamanager.getCancerStudyIds().length === 1 && _mutGeneDataObj.hasOwnProperty('qval')) ? _mutGeneDataObj.qval : null;
              _mutGeneMeta[_uniqueId].index = _mutGeneMetaIndex;
              if (sampleData_[_caseIdIndex].mutated_genes === undefined) {
                sampleData_[_caseIdIndex].mutated_genes = [_mutGeneMetaIndex];
              } else {
                sampleData_[_caseIdIndex].mutated_genes.push(_mutGeneMetaIndex);
              }
              _mutGeneMetaIndex += 1;
            } else {
              _mutGeneMeta[_uniqueId].num_muts += 1;
              _mutGeneMeta[_uniqueId].caseIds.push(_caseId);
              if (sampleData_[_caseIdIndex].mutated_genes === undefined) {
                sampleData_[_caseIdIndex].mutated_genes = [_mutGeneMeta[_uniqueId].index];
              } else {
                sampleData_[_caseIdIndex].mutated_genes.push(_mutGeneMeta[_uniqueId].index);
              }
            }
          }
        });
      });
      tableData_.mutated_genes = {};
      tableData_.mutated_genes.geneMeta = _mutGeneMeta;
      return tableData_.mutated_genes;
    },
    extractCnaData: function(_cnaData) {
      var _cnaMeta = {};
      var _cnaMetaIndex = 0;
      var _sampleDataIndicesObj = this.getCaseIndices('sample');
      $.each(_cnaData.caseIds, function(_index, _caseIdsPerGene) {
        var _geneSymbol = _cnaData.gene[_index];
        var _altType = '';
        switch (_cnaData.alter[_index]) {
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
          if (_sampleDataIndicesObj[_caseId] !== undefined) {
            var _caseIdIndex = _sampleDataIndicesObj[_caseId];
            if (_cnaMeta[_uniqueId] === undefined) {
              _cnaMeta[_uniqueId] = {};
              _cnaMeta[_uniqueId].gene = _geneSymbol;
              _cnaMeta[_uniqueId].cna = _altType;
              _cnaMeta[_uniqueId].cytoband = _cnaData.cytoband[_index];
              _cnaMeta[_uniqueId].caseIds = [_caseId];
              if ((window.iviz.datamanager.getCancerStudyIds().length !== 1) || _cnaData.gistic[_index] === null) {
                _cnaMeta[_uniqueId].qval = null;
              } else {
                _cnaMeta[_uniqueId].qval = _cnaData.gistic[_index][0];
              }
              _cnaMeta[_uniqueId].index = _cnaMetaIndex;
              if (sampleData_[_caseIdIndex].cna_details === undefined) {
                sampleData_[_caseIdIndex].cna_details = [_cnaMetaIndex];
              } else {
                sampleData_[_caseIdIndex].cna_details.push(_cnaMetaIndex);
              }
              _cnaMetaIndex += 1;
            } else {
              _cnaMeta[_uniqueId].caseIds.push(_caseId);
              if (sampleData_[_caseIdIndex].cna_details === undefined) {
                sampleData_[_caseIdIndex].cna_details = [_cnaMeta[_uniqueId].index];
              } else {
                sampleData_[_caseIdIndex].cna_details.push(_cnaMeta[_uniqueId].index);
              }
            }
          }
        });
      });
      tableData_.cna_details = {};
      tableData_.cna_details.geneMeta = _cnaMeta;
      return tableData_.cna_details;
    },
    getTableData: function(attrId) {
      var def = new $.Deferred();
      var self = this;
      if (tableData_[attrId] === undefined) {
        if (attrId === 'mutated_genes') {
          $.when(window.iviz.datamanager.getMutData()).then(function(_data) {
            def.resolve(self.extractMutationData(_data));
          });
        } else if (attrId === 'cna_details') {
          $.when(window.iviz.datamanager.getCnaData()).then(function(_data) {
            def.resolve(self.extractCnaData(_data));
          });
        }
      } else {
        def.resolve(tableData_[attrId]);
      }
      return def.promise();
    },
    getCasesMap: function(type) {
      if (type === 'sample') {
        return data_.groups.group_mapping.sample.patient;
      }
      return data_.groups.group_mapping.patient.sample;
    },
    getCaseIndices: function(type) {
      if (type === 'sample') {
        return data_.groups.sample.data_indices.sample_id;
      }
      return data_.groups.patient.data_indices.patient_id;
    },
    getPatientIds: function(sampleId) {
      var map = this.getCasesMap('sample');
      return map[sampleId];
    },
    getSampleIds: function(patientId) {
      var map = this.getCasesMap('patient');
      return map[patientId];
    },
    openCases: function(type) {
      if (type !== 'patient') {
        type = 'sample';
      }

      var studyId = '';
      var possible = true;
      var selectedCases_ = [];
      var caseIndices_ = {};
      var dataRef = [];

      if (type === 'patient') {
        selectedCases_ = vm_.selectedpatients;
        caseIndices_ = this.getCaseIndices('patient');
        dataRef = patientData_;
      } else {
        selectedCases_ = vm_.selectedsamples;
        caseIndices_ = this.getCaseIndices('sample');
        dataRef = sampleData_;
      }

      $.each(selectedCases_, function(key, caseId) {
        if (key === 0) {
          studyId = dataRef[caseIndices_[caseId]].study_id;
        } else if (studyId !== dataRef[caseIndices_[caseId]].study_id) {
          possible = false;
          return false;
        }
      });
      if (possible) {
        var _selectedCaseIds = selectedCases_.sort();
        var _url = window.cbioURL + 'case.do?cancer_study_id=' +
          studyId + '&' + (type === 'patient' ? 'case_id' : 'sample_id') +
          '=' + _selectedCaseIds[0] +
          '#nav_case_ids=' + _selectedCaseIds.join(',');
        window.open(_url);
      } else {
        new Notification().createNotification(
          'This feature is not available to multiple studies for now!',
          {message_type: 'info'});
      }
    },
    downloadCaseData: function() {
      var sampleIds_ = vm_.selectedsamples;
      var attr = {};
      $.when(this.fetchCompleteData('patient', true), this.fetchCompleteData('sample', true)).then(function() {
        attr.CANCER_TYPE_DETAILED = 'Cancer Type Detailed';
        attr.CANCER_TYPE = 'Cancer Type';
        attr.study_id = 'Study ID';
        attr.patient_id = 'Patient ID';
        attr.sample_id = 'Sample ID';
        attr.mutated_genes = 'With Mutation Data';
        attr.cna_details = 'With CNA Data';

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
        var sampleIndices_ = data_.groups.sample.data_indices.sample_id;
        var patienIndices_ = data_.groups.patient.data_indices.patient_id;
        var samplePatientMapping = data_.groups.group_mapping.sample.patient;
        _.each(sampleIds_, function(sampleId) {
          var temp = sampleData_[sampleIndices_[sampleId]];
          var temp1 = $.extend(true, temp,
            patientData_[patienIndices_[samplePatientMapping[sampleId][0]]]);
          arr.push(temp1);
        });

        var arrL = arr.length;

        for (var i = 0; i < arrL; i++) {
          strA.length = 0;
          strA = getAttrVal(attr, arr[i]);
          content += '\r\n' + strA.join('\t');
        }

        var downloadOpts = {
          filename: 'study_view_clinical_data.txt',
          contentType: 'text/plain;charset=utf-8',
          preProcess: false
        };

        cbio.download.initDownload(content, downloadOpts);
      });
    },
    submitForm: function() {
      var selectedCases_ = vm_.selectedsamples;
      var studyId_ = '';
      var possibleTOQuery = true;

      // Remove all hidden inputs
      $('#iviz-form input:not(:first)').remove();

      _.each(selectedCases_, function(_caseId, key) {
        var index_ = data_.groups.sample.data_indices.sample_id[_caseId];
        if (key === 0) {
          studyId_ = data_.groups.sample.data[index_].study_id;
        } else if (studyId_ !== data_.groups.sample.data[index_].study_id) {
          possibleTOQuery = false;
          return false;
        }
      });
      if (possibleTOQuery) {
        window.studyId = studyId_;
        if (QueryByGeneTextArea.isEmpty()) {
          QueryByGeneUtil.toMainPage(studyId_, selectedCases_);
        } else {
          QueryByGeneTextArea.validateGenes(this.decideSubmit, false);
        }
      } else {
        new Notification().createNotification(
          'Querying multiple studies features is not yet ready!',
          {message_type: 'info'});
      }
    },
    decideSubmit: function(allValid) {
      // if all genes are valid, submit, otherwise show a notification
      if (allValid) {
        QueryByGeneUtil.toQueryPage(window.studyId, vm_.selectedsamples,
          QueryByGeneTextArea.getGenes(), window.mutationProfileId,
          window.cnaProfileId);
      } else {
        new Notification().createNotification(
          'There were problems with the selected genes. Please fix.',
          {message_type: 'danger'});
        $('#query-by-gene-textarea').focus();
      }
    },
    stat: function() {
      var _result = {};
      _result.filters = {};

      // extract and reformat selected cases
      var _selectedCases = [];

      _.each(vm_.selectedsamples, function(_selectedSample) {
        var _index = data_.groups.sample
          .data_indices.sample_id[_selectedSample];
        var _studyId = data_.groups.sample.data[_index].study_id;

        // extract study information
        if ($.inArray(_studyId, _.pluck(_selectedCases, 'studyID')) === -1) {
          _selectedCases.push({
            studyID: _studyId,
            samples: [_selectedSample]
          });
        } else {
          _.each(_selectedCases, function(_resultObj) {
            if (_resultObj.studyID === _studyId) {
              _resultObj.samples.push(_selectedSample);
            }
          });
        }

        // map samples to patients
        _.each(_selectedCases, function(_resultObj) {
          _resultObj.patients = iViz.util.idMapping(
            data_.groups.group_mapping.sample.patient, _resultObj.samples);
        });
      });
      _result.filterspatients = [];
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
            }
          });
          temp = $.extend(true, _result.filters.samples, filters_);
          array = $.extend(true, {}, temp);
          _result.filters.samples = array;
        }
      });
      _result.selected_cases = _selectedCases;
      return _result;
    },

    vm: function() {
      return vm_;
    },
    view: {
      component: {}
    },
    util: {},
    opts: {
      dc: {
        transitionDuration: 400
      }
    },
    data: {},
    styles: styles_,
    applyVC: function(_vc) {
      var _selectedSamples = [];
      var _selectedPatients = [];
      _.each(_.pluck(_vc.selectedCases, 'samples'), function(_arr) {
        _selectedSamples = _selectedSamples.concat(_arr);
      });
      _.each(_.pluck(_vc.selectedCases, 'patients'), function(_arr) {
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
            selectedsamples: [],
            selectedpatients: [],
            selectedgenes: [],
            addNewVC: false,
            selectedPatientsNum: 0,
            selectedSamplesNum: 0,
            hasfilters: false,
            isloading: true,
            redrawgroups: [],
            customfilter: {
              display_name: 'Custom',
              type: '',
              sampleIds: [],
              patientIds: []
            },
            charts: {},
            groupCount: 0,
            updateSpecialCharts: false,
            showSaveButton: true,
            showManageButton: true,
            userid: 'DEFAULT',
            stats: '',
            updateStats: false,
            clearAll: false,
            showScreenLoad: false,
            showDropDown: false
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
            updateStats: function() {
              this.stats = iViz.stat();
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
            selectedsamples: function(newVal, oldVal) {
              if (newVal.length !== oldVal.length) {
                this.selectedSamplesNum = newVal.length;
              }
            },
            selectedpatients: function(newVal, oldVal) {
              if (newVal.length !== oldVal.length) {
                this.selectedPatientsNum = newVal.length;
              }
            }
          }, events: {
            'manage-genes': function(geneList) {
              this.updateGeneList(geneList, false);
            }, 'set-selected-cases': function(selectionType, selectedCases) {
              this.setSelectedCases(selectionType, selectedCases);
            }, 'remove-chart': function(attrId, groupId) {
              this.removeChart(attrId, groupId);
            }
          }, methods: {
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
            openCases: function(type) {
              iViz.openCases(type);
            },
            downloadCaseData: function() {
              iViz.downloadCaseData();
            },
            submitForm: function() {
              iViz.submitForm();
            },
            clearAllCharts: function(includeNextTickFlag) {
              var self_ = this;
              self_.clearAll = true;
              self_.hasfilters = false;
              if (self_.customfilter.patientIds.length > 0 ||
                self_.customfilter.sampleIds.length > 0) {
                self_.customfilter.sampleIds = [];
                self_.customfilter.patientIds = [];
              }
              if (includeNextTickFlag) {
                self_.$nextTick(function() {
                  self_.selectedsamples = _.keys(iViz.getCasesMap('sample'));
                  self_.selectedpatients = _.keys(iViz.getCasesMap('patient'));
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
                    self_.groups[_groupIdToPush].attributes.push(attrData);
                    if (isGroupNdxDataUpdated) {
                      self_.$broadcast('add-chart-to-group', attrData.group_id);
                    }
                    self_.$nextTick(function() {
                      $('#iviz-add-chart').trigger('chosen:updated');
                      self_.showScreenLoad = false;
                    });
                  });
                } else {
                  var newgroup_ = {};
                  var groupAttrs = [];
                  // newgroup_.data = _group.data;
                  newgroup_.type = _group.type;
                  newgroup_.id = self_.groupCount;
                  attrData.group_id = newgroup_.id;
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
            removeChart: function(attrId) {
              var self = this;
              var attrData = self.charts[attrId];
              var attributes = self.groups[attrData.group_id].attributes;
              self.checkForDropDownCharts();
              attributes.$remove(attrData);

              self.$broadcast('remove-grid-item',
                $('#chart-' + attrId + '-div'));

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
              var selectedCaseIds = [];
              var unmappedCaseIds = [];

              if (radioVal === 'patient') {
                var patientIdsList = Object.keys(iViz.getCasesMap('patient'));
                _.each(selectedCases, function(id) {
                  if (patientIdsList.indexOf(id) === -1) {
                    unmappedCaseIds.push(id);
                  } else {
                    selectedCaseIds.push(id);
                  }
                });
              } else {
                var sampleIdsList = Object.keys(iViz.getCasesMap('sample'));
                _.each(selectedCases, function(id) {
                  if (sampleIdsList.indexOf(id) === -1) {
                    unmappedCaseIds.push(id);
                  } else {
                    selectedCaseIds.push(id);
                  }
                });
              }

              if (unmappedCaseIds.length > 0) {
                new Notification().createNotification(selectedCaseIds.length +
                  ' cases selected. The following ' +
                  (radioVal === 'patient' ? 'patient' : 'sample') +
                  ' ID' + (unmappedCaseIds.length === 1 ? ' was' : 's were') +
                  ' not found in this study: ' +
                  unmappedCaseIds.join(', '), {
                    message_type: 'danger'
                  });
              } else {
                new Notification().createNotification(selectedCaseIds.length +
                  ' case(s) selected.', {message_type: 'info'});
              }

              $('#iviz-header-right-1').qtip('toggle');
              if (selectedCaseIds.length > 0) {
                this.clearAllCharts(false);
                var self_ = this;
                Vue.nextTick(function() {
                  _.each(self_.groups, function(group) {
                    if (group.type === radioVal) {
                      self_.hasfilters = true;
                      self_.customfilter.type = group.type;
                      if (radioVal === 'sample') {
                        self_.customfilter.sampleIds = selectedCaseIds;
                        self_.customfilter.patientIds = [];
                      } else {
                        self_.customfilter.patientIds = selectedCaseIds;
                        self_.customfilter.sampleIds = [];
                      }
                      self_.$broadcast('update-custom-filters');
                      return false;
                    }
                  });
                });
              }
            }
          }, ready: function() {
            this.$watch('showVCList', function() {
              if (_.isObject(iViz.session)) {
                this.virtualCohorts = iViz.session.utils.getVirtualCohorts();
              }
            });
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
        width: '30%'
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

  // This is an example to add sample to a virtual cohort from scatter plot
  /*  iViz.vue.vmScatter = (function() {
   var vmInstance_;

   return {
   init: function() {
   vmInstance_ = new Vue({
   el: '#scatter-container',
   data: {
   showList: false,
   virtualCohorts: null,
   sampleID: null,
   cancerStudyID: null,
   addNewVC: false
   }, ready: function() {
   this.$watch('showList', function() {
   if (_.isObject(iViz.session)) {
   this.virtualCohorts = iViz.session.utils.getVirtualCohorts();
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
   }
   };
   })();*/
})(window.Vue, window.iViz, window.dc, window._);

'use strict';
(function(iViz, _, cbio) {
  iViz.util = (function() {
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
      if (filterObj.filterType !== undefined) {
        if (filterObj.filterType === 'RangedFilter') {
          return true;
        }
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
        if($(item).css('fill') === 'transparent') {
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

    content.compare = function(arr1, arr2) {
      if (arr1.length === arr2.length) {
        for (var i = 0; i < arr1.length; i++) {
          if (arr1[i] !== arr2[i]) {
            return false;
          }
        }
      }
      return false;
    };

    content.escape = function(_str) {
      _str = _str.replace(/>/g, '_greater_than_');
      _str = _str.replace(/</g, '_less_than_');
      _str = _str.replace(/\+/g, '_plus_');
      _str = _str.replace(/-/g, '_minus_');
      _str = _str.replace(/\(|\)| /g, '');
      return _str;
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

    return content;
  })();
})(window.iViz,
  window._, window.cbio);
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
    ' v-for="group in groups"></chart-group> ',
    props: [
      'groups', 'selectedsamples', 'selectedpatients', 'hasfilters',
      'redrawgroups', 'customfilter', 'clearAll'
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
        chartsGrid: []
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
        var aName = Number(a.element.attributes['data-number'].nodeValue);
        var bName = Number(b.element.attributes['data-number'].nodeValue);
        return aName - bName;
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
          self_.grid_.items.sort(this.sortByNumber);
          _.each(self_.grid_.getItemElements(), function(_gridItem) {
            var _draggie = new Draggabilly(_gridItem, {
              handle: '.dc-chart-drag'
            });
            self_.grid_.bindDraggabillyEvents(_draggie);
          });
        } else {
          _.each(ChartsIds, function(chartId) {
            self_.grid_.addItems(document.getElementById(chartId));
            var _draggie = new Draggabilly(document.getElementById(chartId), {
              handle: '.dc-chart-drag'
            });
            self_.grid_.bindDraggabillyEvents(_draggie);
          });
        }
        self_.grid_.layout();
      }
    },
    events: {
      'update-grid': function() {
        this.grid_.layout();
      }, 'remove-grid-item': function(item) {
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

        if (self_.customfilter.patientIds.length > 0 ||
          self_.customfilter.sampleIds.length > 0) {
          _hasFilters = true;
          _selectedCasesByFilters = self_.customfilter.patientIds.length > 0 ?
            self_.customfilter.patientIds : self_.customfilter.sampleIds;
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

        _selectedCasesByFilters = _selectedCasesByFilters.sort()

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
            self_.selectedsamples = _resultCounterSelectedCases;
            self_.selectedpatients = iViz.util.intersection(_selectedCasesByFilters, _resultSelectedCases);
          } else {
            self_.selectedsamples = self_.completeSamplesList;
            self_.selectedpatients = self_.completePatientsList;
          }
        } else {
          self_.samplesync = _casesSync;
          self_.patientsync = _counterCasesSync;
          if (self_.hasfilters) {
            self_.selectedsamples = iViz.util.intersection(_selectedCasesByFilters, _resultSelectedCases);;
            self_.selectedpatients = _resultCounterSelectedCases;
          } else {
            self_.selectedsamples = self_.completeSamplesList;
            self_.selectedpatients = self_.completePatientsList;
          }
        }
      },
      'update-custom-filters': function() {
        if (this.customfilter.type === 'patient') {
          this.patientsync = this.customfilter.patientIds;
          this.samplesync = iViz.util.idMapping(iViz.getCasesMap('patient'),
            this.patientsync);
          this.customfilter.sampleIds = this.samplesync;
        } else {
          this.patientsync = iViz.util.idMapping(iViz.getCasesMap('sample'),
            this.customfilter.sampleIds);
          this.samplesync = this.customfilter.sampleIds;
          this.customfilter.patientIds = this.patientsync;
        }

        this.selectedsamples = this.samplesync;
        this.selectedpatients = this.patientsync;
      }
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
    ':clear-chart="clearGroup" :ndx="ndx"   :attributes.sync="attribute"   v-for="attribute in attributes"></div>',
    props: [
      'attributes', 'type', 'id', 'redrawgroups', 'mappedcases', 'clearGroup', 'hasfilters'
    ], created: function() {
      // TODO: update this.data
      var _self = this;
      var ndx_ = crossfilter(iViz.getGroupNdx(this.id));
      this.invisibleBridgeDimension = ndx_.dimension(function(d) {
        return d[_self.type + '_id'];
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
            this.type + '_id').sort();
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
    ':attributes.sync="attributes" :clear-chart="clearChart"></component>',
    props: [
      'ndx', 'attributes', 'clearChart'
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
    template: '<div class="chart-header">' +
    '<div class="chart-title" ' +
    ':class="[showOperations?chartTitleActive:chartTitle]" ' +
    'v-if="hasChartTitle&&((showTableIcon===undefined)||showTableIcon)">' +
    '<span class="chart-title-span" id="{{chartId}}-title">{{displayName}}' +
    '</span></div>' +
    '<div :class="[showOperations?chartOperationsActive:chartOperations]">' +
    '<i v-show="hasFilters" class="fa fa-undo icon hover" ' +
    'aria-hidden="true" @click="reset()"></i>' +
    '<div style="float:left" v-if="showLogScale"></input style="float:left">' +
    '<input type="checkbox" value="" id="" ' +
    'class="bar-x-log" v-model="logChecked">' +
    '<span id="scale-span-{{chartId}}" ' +
    'style="float:left; font-size:10px; margin-right: 15px; color: grey">' +
    'Log Scale X</span></div>' +
    '<i v-if="hasTitleTooltip()" ' +
    'class="fa fa-info-circle icon hover" ' +
    'id="{{chartId}}-description-icon"' +
    'aria-hidden="true"></i>' +
    '<i v-if="showTableIcon" class="fa fa-table icon hover" ' +
    'aria-hidden="true" @click="changeView()"></i>' +
    '<i v-if="showPieIcon" class="fa fa-pie-chart icon hover" ' +
    'aria-hidden="true" @click="changeView()"></i>' +
    '<img v-if="showSurvivalIcon" src="images/survival_icon.svg" ' +
    'class="icon hover"/>' +
    '<div id="{{chartId}}-download-icon-wrapper" class="download">' +
    '<i class="fa fa-download icon hover" alt="download" ' +
    'id="{{chartId}}-download"></i>' +
    '</div>' +
    '<i class="fa fa-arrows dc-chart-drag icon" aria-hidden="true"></i>' +
    '<div style="float:right"><i class="fa fa-times dc-chart-pointer icon" ' +
    '@click="close()"></i></div>' +
    '</div>' +
    '</div>',
    props: [
      'showOperations', 'resetBtnId', 'chartCtrl', 'groupid',
      'hasChartTitle', 'showTable', 'displayName', 'chartId', 'showPieIcon',
      'showTableIcon', 'showLogScale', 'showSurvivalIcon', 'filters',
      'attributes'
    ],
    data: function() {
      return {
        chartOperationsActive: 'chart-operations-active',
        chartOperations: 'chart-operations',
        chartTitle: 'chart-title',
        chartTitleActive: 'chart-title-active',
        logChecked: true,
        hasFilters: false,
        titleTooltip: {
          content: _.isObject(this.attributes) ?
            iViz.util.getClinicalAttrTooltipContent(this.attributes) : ''
        }
      };
    },
    watch: {
      logChecked: function(newVal) {
        this.reset();
        this.$dispatch('changeLogScale', newVal);
      }, filters: function(newVal) {
        this.hasFilters = newVal.length > 0;
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
      changeView: function() {
        this.showTableIcon = !this.showTableIcon;
        this.showPieIcon = !this.showPieIcon;
        this.$dispatch('toTableView');
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

      $('#' + this.chartId + '-download-icon-wrapper').qtip({
        style: {classes: 'qtip-light qtip-rounded qtip-shadow'},
        show: {event: 'mouseover', delay: 0},
        hide: {fixed: true, delay: 300, event: 'mouseout'},
        position: {my: 'bottom left', at: 'top right', viewport: $(window)},
        content: {
          text: 'Download'
        }
      });

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
            var downloadFileTypes = self.chartCtrl.getDownloadFileTypes();
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
(function(Vue) {
  Vue.component('breadCrumb', {
    template: '<span class="breadcrumb_container" ' +
    'v-if="attributes.filter.length > 0">' +
    '<span>{{attributes.display_name}}</span><span ' +
    'v-if="(filtersToSkipShowing.indexOf(attributes.attr_id) === -1) && ' +
    '(specialTables.indexOf(attributes.attr_id) === -1)" class="breadcrumb_items">' +
    '<span v-if="attributes.view_type===\'bar_chart\'">' +
    '<span class="breadcrumb_item">{{filters[0]}} -- {{filters[1]}}</span>' +
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
        filtersToSkipShowing: ['MUT_CNT_VS_CNA', 'sample_id', 'patient_id'],
        specialTables: ['mutated_genes', 'cna_details']
      };
    },
    methods: {
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
})(window.Vue);

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

    content.filtered = function() {
      updateTables();
      isFiltered = true;
      updateQtip = false;
    };

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
      var _labels = {};
      var _currentSampleSize = 0;
      _.each(dcGroup_.top(Infinity), function(label) {
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
        label.sampleRate = (_currentSampleSize <= 0 ? 0 : (Number(label.cases) * 100 / _currentSampleSize).toFixed(1).toString()) + '%';
      });
      labels = _labels;
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

    function drawMarker(_childID, _fatherID) {
      var _path = $('#' + v.opts.chartId + ' svg>g>g:nth-child(' + _childID + ')')
        .find('path');
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

        var _arcID = 'arc-' + _fatherID + '-' + (Number(_childID) - 1);
        var _arc = d3.svg.arc()
          .innerRadius(_r + 3)
          .outerRadius(_r + 5)
          .startAngle(_pointOne)
          .endAngle(_pointTwo);

        d3.select('#' + v.opts.chartId + ' svg g').append('path')
          .attr('d', _arc)
          .attr('fill', _fill)
          .attr('id', _arcID)
          .attr('class', 'mark');
      }
    }

    function pieLabelMouseEnter(data) {
      var childID = Number(data.index) + 1;
      var fatherID = v.opts.chartId;

      $('#' + v.opts.chartId + ' svg>g>g:nth-child(' + childID + ')').css({
        'fill-opacity': '.5',
        'stroke-width': '3'
      });

      drawMarker(childID, fatherID);
    }

    function pieLabelMouseLeave(data) {
      var childID = Number(data.index) + 1;

      $('#' + v.opts.chartId + ' svg>g>g:nth-child(' + childID + ')').css({
        'fill-opacity': '1',
        'stroke-width': '1px'
      });

      removeMarker();
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
    ':data-number="attributes.priority" ' +
    '@mouseenter="mouseEnter($event)" @mouseleave="mouseLeave($event)">' +
    '<chart-operations :has-chart-title="hasChartTitle" ' +
    ':display-name="displayName" :show-table-icon.sync="showTableIcon" ' +
    ' :show-pie-icon.sync="showPieIcon" :chart-id="chartId" ' +
    ':show-operations="showOperations" :groupid="attributes.group_id" ' +
    ':reset-btn-id="resetBtnId" :chart-ctrl="piechart" ' +
    ' :filters.sync="attributes.filter" ' +
    ':attributes="attributes"></chart-operations>' +
    '<div class="dc-chart dc-pie-chart" ' +
    ':class="{view: showPieIcon}" align="center" style="float:none' +
    ' !important;" id={{chartId}} ></div>' +
    '<div id={{chartTableId}} :class="{view: showTableIcon}"></div>' +
    '</div>',
    props: [
      'ndx', 'attributes'
    ],
    data: function() {
      return {
        v: {},
        chartDivId: 'chart-' +
        iViz.util.escape(this.attributes.attr_id) + '-div',
        resetBtnId: 'chart-' +
        iViz.util.escape(this.attributes.attr_id) + '-reset',
        chartId: 'chart-' + iViz.util.escape(this.attributes.attr_id),
        chartTableId: 'table-' + iViz.util.escape(this.attributes.attr_id),
        displayName: this.attributes.display_name,
        chartInst: '',
        component: '',
        showOperations: false,
        cluster: '',
        piechart: '',
        hasChartTitle: true,
        showTableIcon: true,
        showPieIcon: false,
        filtersUpdated: false,
        addingChart: false
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
      }
    },
    methods: {
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
          _self.piechart.filtered();
        }
      });
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
    var data_ = {};// Chart related data. Such as attr_id.
    var colors_;
    var ndx_;
    var hasEmptyValue_ = false;

    var initDc_ = function(logScale) {
      var tickVal = [];
      var barColor = {};
      var i = 0;

      var cluster = ndx_.dimension(function(d) {
        var val = d[data_.attrId];
        if (typeof val === 'undefined' || val === 'NA' || val === '' ||
          val === 'NaN') {
          hasEmptyValue_ = true;
          val = opts_.xDomain[opts_.xDomain.length - 1];
        } else if (logScale) {
          for (i = 1; i < opts_.xDomain.length; i++) {
            if (d[data_.attrId] < opts_.xDomain[i] &&
              d[data_.attrId] >= opts_.xDomain[i - 1]) {
              val = parseInt(Math.pow(10, i / 2 - 0.25), 10);
              break;
            }
          }
        } else if (d[data_.attrId] <= opts_.xDomain[1]) {
          val = opts_.xDomain[0];
        } else if (d[data_.attrId] > opts_.xDomain[opts_.xDomain.length - 3]) {
          val = opts_.xDomain[opts_.xDomain.length - 2];
        } else {
          // minus half of seperateDistance to make the margin values
          // always map to the left side. Thus for any value x, it is in the
          // range of (a, b] which means a < x <= b
          val = Math.ceil((d[data_.attrId] - opts_.startPoint) / opts_.gutter) *
            opts_.gutter + opts_.startPoint - opts_.gutter / 2;
        }

        if (tickVal.indexOf(val) === -1) {
          tickVal.push(Number(val));
        }

        return val;
      });

      tickVal.sort(function(a, b) {
        return a < b ? -1 : 1;
      });

      var tickL = tickVal.length - 1;

      for (i = 0; i < tickL; i++) {
        barColor[tickVal[i]] = colors_[i];
      }

      if (hasEmptyValue_) {
        barColor.NA = '#CCCCCC';
      } else {
        barColor[tickVal[tickL]] = colors_[tickL];
      }

      chartInst_
        .width(opts_.width)
        .height(opts_.height)
        .margins({top: 10, right: 20, bottom: 30, left: 40})
        .dimension(cluster)
        .group(cluster.group())
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

      if (logScale) {
        chartInst_.x(d3.scale.log().nice()
          .domain([0.7, opts_.maxDomain]));
      } else {
        chartInst_.x(d3.scale.linear()
          .domain([
            opts_.xDomain[0] - opts_.gutter,
            opts_.xDomain[opts_.xDomain.length - 1] + opts_.gutter
          ]));
      }

      chartInst_.yAxis().ticks(6);
      chartInst_.yAxis().tickFormat(d3.format('d'));
      chartInst_.xAxis().tickFormat(function(v) {
        return getTickFormat(v, logScale);
      });

      chartInst_.xAxis().tickValues(opts_.xDomain);
      chartInst_.xUnits(function() {
        return opts_.xDomain.length * 1.3 <= 5 ? 5 : opts_.xDomain.length * 1.3;
      });
    };

    function getTickFormat(v, logScale) {
      var _returnValue = v;
      var index = 0;
      if (logScale) {
        if (v === opts_.emptyMappingVal) {
          _returnValue = 'NA';
        } else {
          index = opts_.xDomain.indexOf(v);
          if (index % 2 !== 0) {
            _returnValue = '';
          }
        }
      } else if (opts_.xDomain.length === 1) {
        return 'NA';
      } else if (opts_.xDomain.length === 2) {
        // when there is only one value and NA in the data
        if (v === opts_.xDomain[0]) {
          _returnValue = v;
        } else {
          _returnValue = 'NA';
        }
      } else if (v === opts_.xDomain[0]) {
        return '<=' + opts_.xDomain[1];
      } else if (v === opts_.xDomain[opts_.xDomain.length - 2]) {
        return '>' + opts_.xDomain[opts_.xDomain.length - 3];
      } else if (v === opts_.xDomain[opts_.xDomain.length - 1]) {
        return 'NA';
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
      return _returnValue;
    }

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
        var sampleId = _cases[i].sample_id;
        var patientId = _cases[i].patient_id;
        var row = [];
        if (opts_.groupType === 'patient') {
          sampleId = iViz.getSampleIds(patientId);
          if (_.isArray(sampleId)) {
            sampleId = sampleId.join(', ');
          } else {
            sampleId = '';
          }
          row.push(patientId);
          row.push(sampleId);
        } else {
          patientId = iViz.getPatientIds(sampleId);
          if (_.isArray(patientId)) {
            patientId = patientId.join(', ');
          } else {
            patientId = '';
          }
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
        min: data_.min,
        max: data_.max
      }, opts.logScaleChecked));
      ndx_ = ndx;
      hasEmptyValue_ = false;

      colors_ = $.extend(true, {}, iViz.util.getColors());

      chartInst_ = dc.barChart('#' + opts.chartId, opts.groupid);

      initDc_(opts.logScaleChecked);

      return chartInst_;
    };

    content.redraw = function(logScaleChecked) {
      opts_ = _.extend(opts_, iViz.util.barChart.getDcConfig({
        min: data_.min,
        max: data_.max
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
        divider: 1,
        numOfGroups: 10,
        emptyMappingVal: '',
        gutter: 0.2,
        startPoint: -1,
        maxVal: '',
        maxDomain: 10000 // Design specifically for log scale
      };

      if (!_.isUndefined(data.min) && !_.isUndefined(data.max)) {
        var max = data.max;
        var min = data.min;
        var range = max - min;
        var rangeL = parseInt(range, 10).toString().length - 2;
        var i = 0;
        var _tmpValue;

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
        }

        if (max <= 1 && max > 0 && min >= -1 && min < 0) {
          config.maxVal = (parseInt(max / config.divider, 10) + 1) *
            config.divider;
          config.gutter = 0.2;
          config.startPoint = (parseInt(min / 0.2, 10) - 1) * 0.2;
          // config.emptyMappingVal = config.maxVal + 0.2;
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
              config.emptyMappingVal = Math.pow(10, i + 1);
              config.xDomain.push(config.emptyMappingVal);
              config.maxDomain = Math.pow(10, i + 1.5);
              break;
            }
          }
        } else {
          if (!_.isNaN(range)) {
            for (i = 0; i <= config.numOfGroups; i++) {
              _tmpValue = i * config.gutter + config.startPoint;
              if (config.startPoint < 1500) {
                _tmpValue =
                  Number(cbio.util.toPrecision(Number(_tmpValue), 3, 0.1));
              }

              // If the current tmpValue already bigger than maxmium number, the
              // function should decrease the number of bars and also reset the
              // Mappped empty value.
              if (_tmpValue >= max) {
                // if i = 0 and tmpValue bigger than maximum number, that means
                // all data fall into NA category.
                config.xDomain.push(_tmpValue);
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
            // add marker for NA values
            config.emptyMappingVal =
              config.xDomain[config.xDomain.length - 1] + config.gutter;
            config.xDomain.push(config.emptyMappingVal);
          } else {
            // add marker for greater than maximum
            config.xDomain.push(
              Number(cbio.util.toPrecision(
                Number(config.xDomain[config.xDomain.length - 1] +
                  config.gutter), 3, 0.1)));
            // add marker for NA values
            config.emptyMappingVal =
              Number(cbio.util.toPrecision(
                Number(config.xDomain[config.xDomain.length - 1] +
                  config.gutter), 3, 0.1));
            config.xDomain.push(config.emptyMappingVal);
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
    ':data-number="attributes.priority" @mouseenter="mouseEnter" ' +
    '@mouseleave="mouseLeave">' +
    '<chart-operations :show-log-scale="settings.showLogScale"' +
    ':show-operations="showOperations" :groupid="attributes.group_id" ' +
    ':reset-btn-id="resetBtnId" :chart-ctrl="barChart" ' +
    ':chart-id="chartId" :show-log-scale="showLogScale" ' +
    ':attributes="attributes"' +
    ':filters.sync="attributes.filter"></chart-operations>' +
    '<div class="dc-chart dc-bar-chart" align="center" ' +
    'style="float:none !important;" id={{chartId}} ></div>' +
    '<span class="text-center chart-title-span" ' +
    'id="{{chartId}}-title">{{displayName}}</span>' +
    '</div>',
    props: [
      'ndx', 'attributes'
    ],
    data: function() {
      return {
        chartDivId: 'chart-' + this.attributes.attr_id.replace(/\(|\)| /g, '') +
        '-div',
        resetBtnId: 'chart-' + this.attributes.attr_id.replace(/\(|\)| /g, '') +
        '-reset',
        chartId: 'chart-new-' + this.attributes.attr_id.replace(/\(|\)| /g, ''),
        displayName: this.attributes.display_name,
        chartInst: '',
        barChart: '',
        showOperations: false,
        filtersUpdated: false,
        showSurvivalIcon: true,
        data: {},
        settings: {
          width: 400,
          height: 180,
          showLogScale: false,
          transitionDuration: iViz.opts.dc.transitionDuration
        },
        opts: {},
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
      }
    }, events: {
      closeChart: function() {
        dc.deregisterChart(this.chartInst, this.attributes.group_id);
        this.chartInst.dimension().dispose();
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
      }
    },
    methods: {
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
                  var tempFilters_ = [];
                  tempFilters_[0] = _filter[0].toFixed(2);
                  tempFilters_[1] = _filter[1].toFixed(2);
                  self_.attributes.filter = tempFilters_;
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
      this.barChart = new iViz.view.component.BarChart();
      this.barChart.setDownloadDataTypes(['tsv', 'pdf', 'svg']);
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

      this.data.meta = _.map(_.filter(_.pluck(
        iViz.getGroupNdx(this.opts.groupid), this.opts.attrId), function(d) {
        return d !== 'NA';
      }), function(d) {
        return parseFloat(d);
      });
      var findExtremeResult = cbio.util.findExtremes(this.data.meta);
      this.data.min = findExtremeResult[0];
      this.data.max = findExtremeResult[1];
      this.data.attrId = this.attributes.attr_id;

      if (((this.data.max - this.data.min) > 1000) && (this.data.min > 1)) {
        this.settings.showLogScale = true;
      }
      this.initChart(this.settings.showLogScale);
      this.$dispatch('data-loaded', this.attributes.group_id, this.chartDivId);
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
    var opts_;
    var layout_;
    var getQtipString = function(_data) {
      var toReturn = 'Cancer Study:' + _data.study_id + '<br>Sample Id: ' +
        _data.sample_id + '<br>CNA fraction: ';
      if (isNaN(_data.cna_fraction)) {
        toReturn += _data.cna_fraction;
      } else {
        toReturn += cbio.util.toPrecision(_data.cna_fraction, 2, 0.001);
      }
      toReturn += '<br>Mutation count: ' + _data.mutation_count;
      return toReturn;
    };

    content.init = function(_data, opts) {
      opts_ = $.extend(true, {}, opts);
      chartId_ = opts_.chartId;
      data_ = _.filter(_data, function(datum) {
        return !isNaN(datum.cna_fraction) && !isNaN(datum.mutation_count);
      });
      var _xArr = _.pluck(data_, 'cna_fraction');
      var _yArr = _.pluck(data_, 'mutation_count');
      var _qtips = [];
      _.each(data_, function(_dataObj) {
        _qtips.push(getQtipString(_dataObj));
      });
      var trace = {
        x: _xArr,
        y: _yArr,
        text: _qtips,
        mode: 'markers',
        type: 'scatter',
        hoverinfo: 'text',
        study_id: _.pluck(data_, 'study_id'),
        sample_id: _.pluck(data_, 'sample_id'),
        marker: {
          size: 7,
          color: '#2986e2',
          line: {color: 'white'}
        }
      };
      var data = [trace];
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
      Plotly.plot(document.getElementById(chartId_), data, layout_, {
        displaylogo: false,
        modeBarButtonsToRemove: ['sendDataToCloud', 'pan2d',
          'zoomIn2d', 'zoomOut2d', 'resetScale2d',
          'hoverClosestCartesian', 'hoverCompareCartesian', 'toImage']
      });

      // link to sample view
      var _plotsElem = document.getElementById(chartId_);
      _plotsElem.on('plotly_click', function(data) {
        var _pts_study_id =
          data.points[0].data.study_id[data.points[0].pointNumber];
        var _pts_sample_id =
          data.points[0].data.sample_id[data.points[0].pointNumber];
        window.open(
          cbio.util.getLinkToSampleView(_pts_study_id, _pts_sample_id));
      });

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
        if (_tmpSelectedSampleIdMap.hasOwnProperty(_dataObj.sample_id)) {
          _selectedData.push(_dataObj);
        } else {
          _unselectedData.push(_dataObj);
        }
      });

      document.getElementById(chartId_).data = [];
      var _unselectedDataQtips = [];
      var _selectedDataQtips = [];

      _.each(_unselectedData, function(_dataObj) {
        _unselectedDataQtips.push(getQtipString(_dataObj));
      });
      _.each(_selectedData, function(_dataObj) {
        _selectedDataQtips.push(getQtipString(_dataObj));
      });
      document.getElementById(chartId_).data[0] = {
        x: _.pluck(_unselectedData, 'cna_fraction'),
        y: _.pluck(_unselectedData, 'mutation_count'),
        text: _unselectedDataQtips,
        mode: 'markers',
        type: 'scatter',
        hoverinfo: 'text',
        study_id: _.pluck(data_, 'study_id'),
        sample_id: _.pluck(data_, 'sample_id'),
        marker: {
          size: 6,
          color: '#2986e2',
          line: {color: 'white'}
        }
      };
      document.getElementById(chartId_).data[1] = {
        x: _.pluck(_selectedData, 'cna_fraction'),
        y: _.pluck(_selectedData, 'mutation_count'),
        text: _selectedDataQtips,
        mode: 'markers',
        type: 'scatter',
        hoverinfo: 'text',
        study_id: _.pluck(data_, 'study_id'),
        sample_id: _.pluck(data_, 'sample_id'),
        marker: {
          size: 6,
          color: 'red',
          line: {color: 'white'}
        }
      };

      Plotly.newPlot(document.getElementById(chartId_), document.getElementById(chartId_).data, layout_);

    };

    content.updateDataForDownload = function(fileType) {
      if (['pdf', 'svg'].indexOf(fileType) !== -1) {
        initCanvasDownloadData();
      }
    };

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
    ':data-number="attributes.priority" @mouseenter="mouseEnter" ' +
    '@mouseleave="mouseLeave">' +
    '<chart-operations :show-operations="showOperations"' +
    ' :display-name="displayName" :has-chart-title="true" :groupid="attributes.group_id"' +
    ' :reset-btn-id="resetBtnId" :chart-ctrl="chartInst" ' +
    ':chart-id="chartId"' +
    ' :attributes="attributes" :filters.sync="attributes.filter"></chart-operations>' +
    ' <div :class="{\'start-loading\': showLoad}" ' +
    'class="dc-chart dc-scatter-plot" align="center" ' +
    'style="float:none !important;" id={{chartId}} ></div>' +
    ' <div id="chart-loader"  :class="{\'show-loading\': showLoad}" ' +
    'class="chart-loader" style="top: 30%; left: 30%; display: none;">' +
    ' <img src="images/ajax-loader.gif" alt="loading"></div></div>',
    props: [
      'ndx', 'attributes'
    ],
    data: function() {
      return {
        chartDivId: 'chart-' +
        this.attributes.attr_id.replace(/\(|\)| /g, '') + '-div',
        resetBtnId: 'chart-' +
        this.attributes.attr_id.replace(/\(|\)| /g, '') + '-reset',
        chartId: 'chart-new-' + this.attributes.attr_id.replace(/\(|\)| /g, ''),
        displayName: this.attributes.display_name,
        showOperations: false,
        selectedSamples: [],
        chartInst: {},
        hasFilters: false,
        showLoad: true,
        invisibleDimension: {}
      };
    },
    watch: {
      'attributes.filter': function(newVal) {
        if (newVal.length === 0) {
          this.invisibleDimension.filterAll();
        }
        this.$dispatch('update-filters', true);
      }
    },
    events: {
      'show-loader': function() {
        this.showLoad = true;
      },
      'update-special-charts': function(hasFilters) {
        var attrId =
          this.attributes.group_type === 'patient' ? 'patient_id' : 'sample_id';
        var _selectedCases =
          _.pluck(this.invisibleDimension.top(Infinity), attrId);

        this.selectedSamples = _selectedCases;
        if (hasFilters) {
          this.chartInst.update(_selectedCases);
        } else {
          this.chartInst.update([]);
        }
        this.attachPlotlySelectedEvent();
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
                var _key = _dataObj.cna_fraction + '||' + _dataObj.mutation_count;
                _CnaFracMutCntMap[_key] = _dataObj;
              });
              _.each(_eventData.points, function(_pointObj) {
                if (_pointObj.x) {
                  _selectedData.push(
                    _CnaFracMutCntMap[_pointObj.x + '||' + _pointObj.y]);
                }
              });
              var _selectedCases = _.pluck(_selectedData, 'sample_id').sort();
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
      var _opts = {
        chartId: this.chartId,
        chartDivId: this.chartDivId,
        title: this.attributes.display_name,
        width: window.iViz.styles.vars.scatter.width,
        height: window.iViz.styles.vars.scatter.height
      };
      var attrId =
        this.attributes.group_type === 'patient' ? 'patient_id' : 'sample_id';
      this.invisibleDimension = this.ndx.dimension(function(d) {
        return d[attrId];
      });

      var data = iViz.getGroupNdx(this.attributes.group_id);
      _self.chartInst = new iViz.view.component.ScatterPlot();
      _self.chartInst.init(data, _opts);
      _self.chartInst.setDownloadDataTypes(['pdf', 'svg']);

      _self.attachPlotlySelectedEvent();
      _self.showLoad = false;
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
    var data_ = {};
    var opts_ = {};

    content_.init = function(_data, _opts) {
      opts_ = $.extend(true, {}, _opts);
      $('#' + opts_.chartId).empty();
      data_ = _data;
      var _dataProxy = new iViz.data.SurvivalChartProxy(_data, opts_.attrId);
      this.chartInst_ =
        new iViz.view.component
          .SurvivalCurve(opts_.chartId, _dataProxy.get(), opts_);
      this.chartInst_.addCurve(_dataProxy.get(), 0, '#2986e2');
    };

    // _attrId here indicates chart type (OS or DFS)
    content_.update = function(_selectedPatients, _chartId, _attrId) {
      // remove previous curves
      this.chartInst_.removeCurves();

      // separate selected and unselected data
      var _selectedData = [];
      var _unselectedData = [];
      var _tmpSelectedPatientIdMap = {};
      _.each(_selectedPatients, function(_patientId) {
        _tmpSelectedPatientIdMap[_patientId] = '';
      });
      _.each(Object.keys(iViz.getCaseIndices(opts_.type)),
        function(_patientId) {
          var _index = iViz.getCaseIndices(opts_.type)[_patientId];
          if (_tmpSelectedPatientIdMap.hasOwnProperty(_patientId)) {
            _selectedData.push(data_[_index]);
          } else {
            _unselectedData.push(data_[_index]);
          }
        });

      // settings for different curves
      var _selectedDataProxy =
        new iViz.data.SurvivalChartProxy(_selectedData, _attrId);
      var _unselectedDataProxy =
        new iViz.data.SurvivalChartProxy(_unselectedData, _attrId);

      // add curves
      if (_selectedDataProxy.get().length === 0) {
        this.chartInst_.addCurve(_unselectedDataProxy.get(), 0, '#2986e2');
        this.chartInst_.removePval();
      } else {
        this.chartInst_.addCurve(_selectedDataProxy.get(), 0, 'red');
        this.chartInst_.addCurve(_unselectedDataProxy.get(), 1, '#2986e2');
        this.chartInst_.addPval(
          _selectedDataProxy.get(), _unselectedDataProxy.get());
      }
    };

    content_.updateDataForDownload = function(fileType) {
      if (['pdf', 'svg'].indexOf(fileType) !== -1) {
        initCanvasDownloadData();
      }
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
    ':data-number="attributes.priority" @mouseenter="mouseEnter" ' +
    '@mouseleave="mouseLeave">' +
    '<chart-operations :show-operations="showOperations" ' +
    ':has-chart-title="hasChartTitle" :display-name="displayName" ' +
    ':groupid="attributes.group_id" :reset-btn-id="resetBtnId" :chart-ctrl="chartInst" ' +
    ' :chart-id="chartId" ' +
    ':attributes="attributes"></chart-operations>' +
    '<div :class="{\'start-loading\': showLoad}" ' +
    'class="dc-chart dc-scatter-plot" align="center" ' +
    'style="float:none !important;" id={{chartId}} ></div>' +
    '<div id="chart-loader"  :class="{\'show-loading\': showLoad}" ' +
    'class="chart-loader" style="top: 30%; left: 30%; display: none;">' +
    '<img src="images/ajax-loader.gif" alt="loading"></div></div>',
    props: [
      'ndx', 'attributes'
    ],
    created: function() {
    },
    data: function() {
      return {
        chartDivId: 'chart-' +
        this.attributes.attr_id.replace(/\(|\)| /g, '') + '-div',
        resetBtnId: 'chart-' +
        this.attributes.attr_id.replace(/\(|\)| /g, '') + '-reset',
        chartId: 'chart-new-' + this.attributes.attr_id.replace(/\(|\)| /g, ''),
        displayName: this.attributes.display_name,
        chartInst: '',
        showOperations: false,
        fromWatch: false,
        fromFilter: false,
        hasChartTitle: true,
        showLoad: true,
        invisibleDimension: {}
      };
    },
    events: {
      'show-loader': function() {
        this.showLoad = true;
      },
      'update-special-charts': function(hasFilters) {
        var attrId =
          this.attributes.group_type === 'patient' ? 'patient_id' : 'sample_id';
        var _selectedCases = [];
        if (hasFilters) {
          _selectedCases =
            _.pluck(this.invisibleDimension.top(Infinity), attrId);
        }
        this.chartInst.update(
          _selectedCases, this.chartId, this.attributes.attr_id);
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
      }
    },
    ready: function() {
      var _self = this;
      _self.showLoad = true;
      var attrId =
        this.attributes.group_type === 'patient' ? 'patient_id' : 'sample_id';
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
      _self.chartInst = new iViz.view.component.Survival();
      _self.chartInst.setDownloadDataTypes(['pdf', 'svg']);

      var data = iViz.getGroupNdx(this.attributes.group_id);
      _self.chartInst.init(data, _opts);
      _self.showLoad = false;
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
    _self.opts_ = _opts;
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

  iViz.view.component.SurvivalCurve.prototype.addCurve = function(_data,
                                                                  _curveIndex,
                                                                  _lineColor) {
    var _self = this;

    // add an empty/zero point so the curve starts from zero time point
    if (_data !== null && _data.length !== 0) {
      if (_data[0].time !== 0) {
        _data.unshift({
          status: 0,
          survival_rate: 1,
          time: 0
        });
      }
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
      _selectedData.splice(0, 1);
      _unselectedData.splice(0, 1);
      var _pVal = LogRankTest.calc(_selectedData, _unselectedData);
      _self.elem_.svg.append('text')
        .attr('class', 'pval')
        .attr('x', _self.opts_.width - 30)
        .attr('y', 30)
        .attr('font-size', 10)
        .style('text-anchor', 'end')
        .text('p = ' + _pVal.toPrecision(2));
    };

  iViz.view.component.SurvivalCurve.prototype.removePval = function() {
    var _self = this;
    _self.elem_.svg.selectAll('.pval').remove();
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
    var datum_ = {
      study_id: '',
      patient_id: '',
      time: '', // num of months
      status: '',
      num_at_risk: -1,
      survival_rate: 0
    };
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
        var _datum = jQuery.extend(true, {}, datum_);
        _datum.patient_id = _dataObj.patient_id;
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
    var data_;
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
    var caseIndices = {};
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
        allSamplesIds = _selectedSamples;
        selectedSamples = _selectedSamples;
        selectedSamples.sort();
        sequencedSampleIds = _attributes.options.sequencedCases;
        sequencedSampleIds.sort();
        selectedGenes = _selectedGenes;
        chartId_ = _opts.chartId;
        opts = _opts;
        genePanelMap = _genePanelMap;
        caseIndices = iViz.getCaseIndices(_attributes.group_type);
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
        initReactTable(true);
      };

    content.update = function(_selectedSamples, _selectedRows) {
      var selectedMap_ = {};
      var includeMutationCount = false;
      if (_selectedRows !== undefined) {
        selectedRows = _selectedRows;
      }
      if (selectedRows.length === 0) {
        selectedRowData = [];
      }
      _selectedSamples.sort();
      if ((!initialized) ||
        (!iViz.util.compare(selectedSamples, _selectedSamples))) {
        initialized = true;
        selectedSamples = _selectedSamples;
        if (iViz.util.compare(allSamplesIds, selectedSamples)) {
          initReactTable(true);
        } else {
          _.each(_selectedSamples, function(caseId) {
            var caseIndex_ = caseIndices[caseId];
            var caseData_ = data_[caseIndex_];
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
                  selectedMap_[geneIndex].caseIds = [caseId];
                } else {
                  if (includeMutationCount) {
                    selectedMap_[geneIndex].num_muts += 1;
                  }
                  selectedMap_[geneIndex].caseIds.push(caseId);
                }
              });
            }
          });
          initReactTable(true, selectedMap_, selectedSamples);
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

    function initReactTable(_reloadData, _selectedMap, _selectedSampleIds) {
      if (_reloadData) {
        reactTableData = initReactData(_selectedMap, _selectedSampleIds);
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
              attr_val: key === 'caseIds' ? category.caseIds.join(',') : category[key]
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

    function mutatedGenesData(_selectedGenesMap, _selectedSampleIds) {

      genePanelMap = window.iviz.datamanager.updateGenePanelMap(genePanelMap, _selectedSampleIds);
      
      selectedGeneData.length = 0;
      var numOfCases_ = content.getCases().length;

      if (geneData_) {
        _.each(geneData_, function(item, index) {
          var datum = {};
          var freq = 0;
          datum.gene = item.gene;
          if (_selectedGenesMap === undefined) {
            datum.caseIds = iViz.util.unique(item.caseIds);
            datum.cases = datum.caseIds.length;
            datum.uniqueId = index;
            if (typeof genePanelMap[item.gene] !== 'undefined') {
              freq = iViz.util.calcFreq(datum.cases, genePanelMap[item.gene]["sample_num"]);
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
            datum.caseIds =
              iViz.util.unique(_selectedGenesMap[item.index].caseIds);
            datum.cases = datum.caseIds.length;
            if (typeof genePanelMap[item.gene] !== 'undefined') {
              freq = iViz.util.calcFreq(datum.cases, genePanelMap[item.gene]["sample_num"]);
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

    function initReactData(_selectedMap, _selectedSampleIds) {
      attr_ = iViz.util.tableView.getAttributes(type_);
      var result = {
        data: [],
        attributes: attr_
      };

      if (isMutatedGeneCna) {
        var _mutationData = mutatedGenesData(_selectedMap, _selectedSampleIds);
        _.each(_mutationData, function(item) {
          for (var key in item) {
            if (item.hasOwnProperty(key)) {
              var datum = {
                attr_id: key,
                uniqueId: item.uniqueId,
                attr_val: key === 'caseIds' ? item.caseIds.join(',') : item[key]
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
          return (item.uniqueId !== data.uniqueId);
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
              datatype: 'NUMBER',
              column_width: 95
            },
            {
              attr_id: 'cases',
              display_name: '#',
              datatype: 'NUMBER',
              column_width: 95
            },
            {
              attr_id: 'sampleRate',
              display_name: 'Freq',
              datatype: 'PERCENTAGE',
              column_width: 93
            },
            {
              attr_id: 'caseIds',
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
              datatype: 'NUMBER',
              column_width: 75
            },
            {
              attr_id: 'altrateInSample',
              display_name: 'Freq',
              datatype: 'PERCENTAGE',
              column_width: 78
            },
            {
              attr_id: 'caseIds',
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
    ':class="[\'grid-item\', classTableHeight, \'grid-item-w-2\']" ' +
    ':data-number="attributes.priority" @mouseenter="mouseEnter" ' +
    '@mouseleave="mouseLeave">' +
    '<chart-operations :show-operations="showOperations" ' +
    ':display-name="displayName" :chart-ctrl="chartInst"' +
    ':has-chart-title="true" :groupid="attributes.group_id" ' +
    ':reset-btn-id="resetBtnId" :chart-id="chartId" :attributes="attributes" ' +
    ':filters.sync="attributes.filter"> ' +
    '</chart-operations><div class="dc-chart dc-table-plot" ' +
    ':class="{\'start-loading\': showLoad}" align="center" ' +
    'style="float:none !important;" id={{chartId}} ></div>' +
    '<div id="chart-loader"  :class="{\'show-loading\': showLoad}" ' +
    'class="chart-loader" style="top: 30%; left: 30%; display: none;">' +
    '<img src="images/ajax-loader.gif" alt="loading"></div></div>',
    props: [
      'ndx', 'attributes', 'options'
    ],
    data: function() {
      return {
        chartDivId: 'chart-' +
        this.attributes.attr_id.replace(/\(|\)| /g, '') + '-div',
        resetBtnId: 'chart-' +
        this.attributes.attr_id.replace(/\(|\)| /g, '') + '-reset',
        chartId: 'chart-new-' +
        this.attributes.attr_id.replace(/\(|\)| /g, ''),
        displayName: '',
        showOperations: false,
        chartInst: {},
        showLoad: true,
        selectedRows: [],
        invisibleDimension: {},
        isMutatedGeneCna: false,
        classTableHeight: 'grid-item-h-2',
        madeSelection: false,
        genePanelMap: {}
      };
    },
    watch: {
      'attributes.filter': function(newVal) {
        if (newVal.length === 0) {
          this.invisibleDimension.filterAll();
          this.selectedRows = [];
        }
        this.$dispatch('update-filters', true);
      }
    },
    events: {
      'show-loader': function() {
        if (!this.madeSelection || this.isMutatedGeneCna) {
          this.showLoad = true;
        }
      },
      'gene-list-updated': function(genes) {
        genes = $.extend(true, [], genes);
        this.chartInst.updateGenes(genes);
      },
      'update-special-charts': function() {
        // Do not update chart if the selection is made on itself
        if (this.madeSelection && !this.isMutatedGeneCna) {
          this.madeSelection = false;
        } else {
          var attrId =
            this.attributes.group_type === 'patient' ?
              'patient_id' : 'sample_id';
          var _selectedCases =
            _.pluck(this.invisibleDimension.top(Infinity), attrId);
          this.chartInst.update(_selectedCases, this.selectedRows);
          this.setDisplayTitle(this.chartInst.getCases().length);
          this.showLoad = false;
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
          _.each(_selectedRowData, function(item) {
            var casesIds = item.caseids.split(',');
            selectedSamplesUnion = selectedSamplesUnion.concat(casesIds);
          });
          if (this.attributes.filter.length === 0) {
            this.attributes.filter = selectedSamplesUnion.sort();
          } else {
            this.attributes.filter =
              iViz.util.intersection(this.attributes.filter, selectedSamplesUnion.sort());
          }
        } else {
          this.selectedRows = selectedRowsUids;
          this.attributes.filter = this.selectedRows;
        }
        var filtersMap = {};
        _.each(this.attributes.filter, function(filter) {
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
        this.displayName = this.isMutatedGeneCna ?
          (this.attributes.display_name +
          ' (' + numOfCases + ' profiled samples)') : '';
      },
      processTableData: function(_data) {
        var data = iViz.getGroupNdx(this.attributes.group_id);
        var opts = {
          width: window.iViz.styles.vars.specialTables.width,
          height: window.iViz.styles.vars.specialTables.height,
          chartId: this.chartId
        };
        this.chartInst.init(this.attributes, opts, this.$root.selectedsamples,
          this.$root.selectedgenes, data, {
            addGeneClick: this.addGeneClick,
            submitClick: this.submitClick
          }, this.isMutatedGeneCna ? _data.geneMeta : null, this.invisibleDimension, this.genePanelMap);
        this.setDisplayTitle(this.chartInst.getCases().length);
        if (!this.isMutatedGeneCna &&
          Object.keys(this.attributes.keys).length <= 3) {
          this.classTableHeight = 'grid-item-h-1';
        }
        this.showLoad = false;
      }
    },
    ready: function() {
      var _self = this;
      _self.showLoad = true;
      var callbacks = {};
      var attrId = this.attributes.attr_id;

      this.isMutatedGeneCna =
        ['mutated_genes', 'cna_details']
          .indexOf(_self.attributes.attr_id) !== -1;

      if (this.isMutatedGeneCna) {
        attrId = this.attributes.group_type === 'patient' ?
          'patient_id' : 'sample_id';
      }

      this.invisibleDimension = this.ndx.dimension(function(d) {
        if (typeof d[attrId] === 'undefined' ||
          ['na', 'n/a', 'N/A'].indexOf(d[attrId]) !== -1) {
          d[attrId] = 'NA';
        }
        return d[attrId];
      });
      callbacks.addGeneClick = this.addGeneClick;
      callbacks.submitClick = this.submitClick;
      _self.chartInst = new iViz.view.component.TableView();
      _self.chartInst.setDownloadDataTypes(['tsv']);
      if (_self.isMutatedGeneCna) {
        $.when(iViz.getTableData(_self.attributes.attr_id)).then(function(_tableData) {
          $.when(window.iviz.datamanager.getGenePanelMap()).then(function (_genePanelMap) {
            //create gene panel map
            _self.genePanelMap = _genePanelMap;
            _self.processTableData(_tableData);
          });
        });
      } else {
        _self.processTableData();
      }
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
(function(Vue, dc, iViz, $) {
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
      at: 'center',
      target: ''
    },
    show: {
      event: 'click', // Show it on click...
      solo: true // ...and hide all other tooltips...
    },
    hide: false,
    style: 'qtip-light qtip-rounded qtip-wide'
  };
  Vue.component('customCaseInput', {
    template: '<input type="button" id="iviz-header-right-1" ' +
    'class="iviz-header-button" value="Select cases by IDs"/>' +
    '<div class="iviz-hidden" id="iviz-case-select-custom-dialog">' +
    '<b>Please input IDs (one per line)</b><textarea rows="20" cols="50" ' +
    'id="iviz-case-select-custom-input" v-model="casesIdsList"></textarea>' +
    '<br/><label><input type="radio" v-model="caseSelection" ' +
    'value="sample" checked>By sample ID</label><label><input type="radio" ' +
    'v-model="caseSelection" value="patient">' +
    'By patient ID</label><button type="button" @click="SetCasesSelection()" ' +
    'style="float: right;">Select</button></div>',
    props: [],
    data: function() {
      return {
        caseSelection: '',
        casesIdsList: ''
      };
    },
    events: {},
    methods: {
      SetCasesSelection: function() {
        var caseIds = this.casesIdsList.trim().split(/\s+/);
        this.$dispatch('set-selected-cases', this.caseSelection, _.uniq(caseIds));
      }
    },
    ready: function() {
      var _customDialogQtip =
        $.extend(true, {}, headerCaseSelectCustomDialog);
      _customDialogQtip.position.target = $(window);
      _customDialogQtip.content.text = $('#iviz-case-select-custom-dialog');
      $('#iviz-header-right-1').qtip(_customDialogQtip);
    }
  });
})(
  window.Vue,
  window.dc,
  window.iViz,
  window.$ || window.jQuery
);
