
var clinicalData;
google.load('visualization', '1', {packages:['table']});
function loadClinicalData(cancerStudyId,caseSetId) {
    var params = {cmd:'getClinicalData',
                 case_set_id:caseSetId,
                 include_free_form:1};
    $.get("webservice.do",
          params,
          function(data){
              //$('#summary').html("<table><tr><td>"+data.replace(/^#[^\r\n]+[\r\n]+/g,"").replace(/\tNA([\t\r\n])/g,"\t$1").replace(/\tNA([\t\r\n])/g,"\t$1").replace(/[\r\n]+/g,"</td></tr><tr><td>").replace(/\t/g,"</td><td>")+"</td></tr></table>");
              var rows = data.replace(/^#[^\r\n]+[\r\n]+/g,"").replace(/\tNA([\t\r\n])/g,"\t$1").replace(/\tNA([\t\r\n])/g,"\t$1").match(/[^\r\n]+/g);
              var matrix = [];
              for (var i=0; i<rows.length; i++) {
                  matrix.push(rows[i].split('\t'));
              }
              
              var types = convertTypes(matrix);
              
              clinicalData = google.visualization.arrayToDataTable(matrix);
              var table = new google.visualization.Table(document.getElementById('summary'));
              table.draw(clinicalData, {showRowNumber: true});
          })
}

function convertTypes(dataMatrix) {
    var types = determineColumnTypes(dataMatrix);
    var headers = dataMatrix[0];
    for (var c=0; c<headers.length; c++) {
        if (types[headers[c]]=='number') {
            for (var r=1; r<dataMatrix.length; r++) {
                dataMatrix[r][c] = parseFloat(dataMatrix[r][c]);
            }
        } else if (types[headers[c]]=='boolean') {
            for (var r=1; r<dataMatrix.length; r++) {
                var dl = dataMatrix[r][c].toLowerCase()
                dataMatrix[r][c] = dl=='true' || dl=='y';
            }
        }
    }
    return types;
}

function determineColumnTypes(dataMatrix) {
    var rows = dataMatrix.length;
    var headers = dataMatrix[0];
    var types = {};
    var cols = headers.length;
    for (var c=0; c<cols; c++) {
        var type = headers[c].match(/_cluster$/) ? 'string' : null;
        for (var r=1; r<rows; r++) {
            if (type=='string') break;
            var d = dataMatrix[r][c];
            if (d.length==0) continue;
            if (type==null)
                type = getType(d);
            else if (type!=getType(d))
                type = 'string';
        }
        types[headers[c]] = type;
    }
    return types;
}

function getType(str) {
    if (isNum(str))
        return 'number';
    if (isBool(str))
        return 'boolean';
    return 'string';
}

function isNum(str) {
    this.regex = new RegExp('^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$');
    return this.regex.test(str);
}

function isBool(str) {
    this.regex = new RegExp('^(true)|(false)|(y)|(n)$');
    return this.regex.test(str.toLowerCase());
}