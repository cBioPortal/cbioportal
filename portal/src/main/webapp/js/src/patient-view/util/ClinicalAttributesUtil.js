'use strict';

/**
 * Functions for dealing with clinical attributes.
 */
window.ClinicalAttributesUtil = (function() {
  /**
   * Clean clinical attributes. Useful for rounding numbers, or other types of
   * data cleanup steps. Probably differs per institution.
   * @param {object} clinicalData - key/value pairs of clinical data
   */
  function clean(clinicalData) {
    // Shallow Copy clinicalData
    var cleanClinicalData = $.extend({}, clinicalData);
    var NULL_VALUES = [
      'not applicable',
      'not available',
      'pending',
      'discrepancy',
      'completed',
      '',
      'null',
      'unknown',
      'na',
      'n/a',
      '[unkown]',
      '[not submitted]',
      '[not evaluated]',
      '[not applicable]',
      '[not available]',
      '[undefined]'
    ];

    for (var key in clinicalData) {
      var cleanValue;
      var value;

      value = clinicalData[key];

      // Remove null values
      if (NULL_VALUES.indexOf(value.toLowerCase()) > -1) {
        delete cleanClinicalData[key];
      } else {
        // Change values for certain attributes, e.g. rounding
        switch (key) {
          case 'OS_MONTHS':
          case 'DFS_MONTHS':
            if ($.isNumeric(value)) {
              value = Math.round(value);
            }
            cleanClinicalData[key] = value;
            break;
        }
      }
    }
    return cleanClinicalData;
  }

  /**
   * Get first key found in object. Otherwise return null.
   * @param {object} object - object with key/value pairs
   * @param {array} keys - array of keys
   */
  function getFirstKeyFound(object, keys) {
      if (!object) {
        return null;
      }

      for (var i=0; i< keys.length; i++) {
          var value = object[keys[i]];
          if (typeof value !== 'undefined' && value !== null) {
            return value;
          }
      }
      return null;
  }


  /**
   * Derive clinical attributes from existing clinical attributes .e.g. age based
   * on a date of birth. TODO: Now only includes a funky hack to keep current
   * derived clinical attributes working.
   * @param {object} clinicalData - key/value pairs of clinical data
   */
  function derive(clinicalData) {
    var derivedClinicalAttributes = $.extend({}, clinicalData);
    
    /**
     * TODO: Pretty funky function to get a normalized case type. This should
     * probably also be a clinical attribute with a restricted vocabulary. Once
     * the database has been changed to include normalized case types, this
     * function should be removed.
     * @param {object} clinicalData - key/value pairs of clinical data
     * @param {string} caseTypeAttrs - TUMOR_TYPE or SAMPLE_TYPE value to normalize
     */
    function normalizedCaseType(clinicalData, caseTypeAttrs) {
      var caseTypeNormalized = null;
      var caseType;
      var caseTypeLower;
      var attr;
      var i;

      for (i = 0; i < caseTypeAttrs.length; i++) {
        caseType = clinicalData[caseTypeAttrs[i]];

        if (!cbio.util.checkNullOrUndefined(caseType)) {
          caseTypeLower = caseType.toLowerCase();

          if (caseTypeLower.indexOf('metasta') >= 0) {
            caseTypeNormalized = 'metastasis';
          } else if (caseTypeLower.indexOf('recurr') >= 0) {
            caseTypeNormalized = 'recurrence';
          } else if (caseTypeLower.indexOf('progr') >= 0) {
            caseTypeNormalized = 'progressed';
          } else if (caseTypeLower.indexOf('xeno') >= 0) {
            caseTypeNormalized = 'xenograft';
          } else if (caseTypeLower.indexOf('cfdna') >= 0) {
            caseTypeNormalized = 'cfdna';
          } else if (caseTypeLower.indexOf('prim') >= 0 || 
                    caseTypeLower.indexOf('prim') >= 0) {
            caseTypeNormalized = 'primary';
          }
          if (!cbio.util.checkNullOrUndefined(caseTypeNormalized)) {
            break;
          }
        }
      }

      return caseTypeNormalized;
    }

    var caseTypeNormalized = normalizedCaseType(clinicalData, ['SAMPLE_CLASS', 'SAMPLE_TYPE', 'TUMOR_TISSUE_SITE', 'TUMOR_TYPE']);
    if (caseTypeNormalized !== null) {
      var loc;

      derivedClinicalAttributes.DERIVED_NORMALIZED_CASE_TYPE = caseTypeNormalized;

      // TODO: DERIVED_SAMPLE_LOCATION should probably be a clinical attribute.
      if (derivedClinicalAttributes.DERIVED_NORMALIZED_CASE_TYPE === 'metastasis') {
          loc = getFirstKeyFound(clinicalData, ['METASTATIC_SITE', 'TUMOR_SITE']);
      } else if (derivedClinicalAttributes.DERIVED_NORMALIZED_CASE_TYPE === 'primary') {
          loc = getFirstKeyFound(clinicalData, ['PRIMARY_SITE', 'TUMOR_SITE']);
      } else {
          loc = getFirstKeyFound(clinicalData, ['TUMOR_SITE']);
      }
      if (loc !== null) {
        derivedClinicalAttributes.DERIVED_SAMPLE_LOCATION = loc;
      }
    }

    return derivedClinicalAttributes;
  }

  /**
   * Run both clean and derive on the clinicalData.
   */
  function cleanAndDerive(clinicalData) {
    return derive(clean(clinicalData));
  }

  /**
   * Return string of spans representing the clinical attributes. The spans
   * have been made specifically to add clinical attribute information as
   * attributes to allow for easy styling with CSS.
   * @param {object} clinicalData   - key/value pairs of clinical data
   * @param {string} cancerStudyId  - short name of cancer study
   */
  function getSpans(clinicalData, cancerStudyId) {
    var spans = '';
    var clinicalAttributesCleanDerived = cleanAndDerive(clinicalData);
    var spanTemplate = '<span class="clinical-attribute" attr-id="{0}" attr-value="{1}" study="{2}">{1}</span>';

    for (var key in clinicalAttributesCleanDerived) {
      var value = clinicalAttributesCleanDerived[key];
      spans += PatientViewUtil.stringFormat(spanTemplate, key, value, cancerStudyId);
    }
    
    return spans;
  }

  /*
   * Add .first-order class to all elements with the lowest order attribute.
   * This way the first element can be styled in a different manner. If flex
   * order attributes were working properly in CSS, one would be able to say
   * .clinical-attribute:first, this is unfortunately not the case, therefore
   * this hack is required. See clinical-attributes.css to see how this is
   * used.
   */
  function addFirstOrderClass() {
    $(".sample-record-inline, #more-patient-info").each(function(x) {
      var orderSortedAttributes = _.sortBy($(this).find("a > .clinical-attribute"), function(y) {
        var order = parseInt($(y).css("order"));
        if (isNaN(order)) {
          console.log("Warning: No order attribute found in .clinical-attribute.");
        }
        return order;
      });
      $(orderSortedAttributes[0]).addClass("first-order");
    });
  }

  return {
    cleanAndDerive: cleanAndDerive,
    getSpans: getSpans,
    addFirstOrderClass: addFirstOrderClass
  };
})();
