(function($) {
/*
 * Function: fnGetColumnData
 * Purpose:  Return an array of table values from a particular column.
 * Returns:  array string: 1d data array
 * Inputs:   object:oSettings - dataTable settings object. This is always the last argument past to the function
 *           int:iColumn - the id of the column to extract the data from
 *           bool:bUnique - optional - if set to false duplicated values are not filtered out
 *           bool:bFiltered - optional - if set to false all the table data is used (not only the filtered)
 *           bool:bIgnoreEmpty - optional - if set to false empty values are not filtered from the result array
 * Author:   Benedikt Forchhammer <b.forchhammer /AT\ mind2.de>
 */
$.fn.dataTableExt.oApi.fnGetColumnData = function ( oSettings, iColumn, bUnique, bFiltered, bIgnoreEmpty ) {
    // check that we have a column id
    if ( typeof iColumn == "undefined" ) return new Array();
     
    // by default we only want unique data
    if ( typeof bUnique == "undefined" ) bUnique = true;
     
    // by default we do want to only look at filtered data
    if ( typeof bFiltered == "undefined" ) bFiltered = true;
     
    // by default we do not want to include empty values
    if ( typeof bIgnoreEmpty == "undefined" ) bIgnoreEmpty = true;
     
    // list of rows which we're going to loop through
    var aiRows;
     
    // use only filtered rows
    if (bFiltered == true) aiRows = oSettings.aiDisplay;
    // use all rows
    else aiRows = oSettings.aiDisplayMaster; // all row numbers
 
    // set up data array   
    var asResultData = new Array();
     
    for (var i=0,c=aiRows.length; i<c; i++) {
        iRow = aiRows[i];
        var aData = this.fnGetData(iRow);
        var sValue = aData[iColumn];
         
        // ignore empty values?
        if (bIgnoreEmpty == true && sValue.length == 0) continue;
 
        // ignore unique values?
        else if (bUnique == true && jQuery.inArray(sValue, asResultData) > -1) continue;
         
        // else push the value onto the result data array
        else asResultData.push(sValue);
    }
     
    return asResultData;
}
$.fn.dataTableExt.oSort['allnumeric-asc']  = function(a,b) {
    if(a === 'NA' || a === ''){
        return 1;
    }else if(b === 'NA' || b === ''){
        return -1;
    }else{
        var x = parseFloat(a);
        var y = parseFloat(b);
        return ((x < y) ? -1 : ((x > y) ?  1 : 0));
    }
};

$.fn.dataTableExt.oSort['allnumeric-desc']  = function(a,b) {
    if(a === 'NA' || a === ''){
        return 1;
    }else if(b === 'NA' || b === ''){
        return -1;
    }else{
        var x = parseFloat(a);
        var y = parseFloat(b);
        return ((x < y) ? 1 : ((x > y) ?  -1 : 0));
    }
};

$.fn.dataTableExt.oSort['string-asc']  = function(a,b) {
    if(a === 'NA' || a === ''){
        return 1;
    }else if(b === 'NA' || b === ''){
        return -1;
    }else{
        return ((a < b) ? -1 : ((a > b) ?  1 : 0));
    }
};

$.fn.dataTableExt.oSort['string-desc']  = function(a,b) {
    if(a === 'NA' || a === ''){
        return 1;
    }else if(b === 'NA' || b === ''){
        return -1;
    }else{
        return ((a < b) ? 1 : ((a > b) ?  -1 : 0));
    }
};
}(jQuery));

