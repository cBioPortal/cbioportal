'use strict';

/**
 * General functions for patient view.
 */
window.PatientViewUtil = (function() {
  /**
   * Format strings similar to printf
   * http://stackoverflow.com/questions/610406/
   * @example
   * // returns "a b"
   * PatientViewUtil.stringFormat('{0} {1}', 'a', 'b')
   * @returns {string} returns the formatted string
   */ 
  function stringFormat(format) {
    var args = Array.prototype.slice.call(arguments, 1);
    return format.replace(/{(\d+)}/g, function(match, number) { 
      return typeof args[number] != 'undefined'
        ? args[number] 
        : match;
    });
  }

  return {
    stringFormat: stringFormat
  };
})();
