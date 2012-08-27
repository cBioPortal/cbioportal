// wrapper for this program
var Gistic = {};

Gistic.gisticDialog_init = function() {

    // set up modal dialog box for gistic table (step 3)
    $('#gistic_dialog').dialog({autoOpen: false,
        resizable: false,
        modal: true,
        minHeight: 315,
        minWidth: 636
        });

}

Gistic.get = function(cancerStudyId) {

    // data to be sent to the server
    var data = {'selected_cancer_type': cancerStudyId };

    // do a get request
    var gs;
    $.get('Gistic.json', data, function(gistics) {
        // ... do something with this data ...
        gs = gistics;
    });

    return gs;
};
