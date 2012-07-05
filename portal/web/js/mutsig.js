// AJAX and Dynamic jQuery for MutSig table
//
// AJAX :
//           Connects to portal via AJAX and downloads JSON document containg MutSig information:
//              gene symbol, num mutations, q-value
//
// Dynamic jQuery:
//          Handles changes in select_cancer_type:
//              shows/hides "Recurrently Mutated Genes" button
//              shows/hides table containing MutSig information.
//          Takes user-selected mutated genes and adds them to the Gene Symbol box.
//
// Gideon Dresdner
// April 2012


// variable for the return DOM object returned by dataTable library
var DATATABLE_FORMATTED = -1;

// what is the previous cancer study that has been selected?
// this way we don't have to repeat queries
var CANCER_STUDY_SELECTED = -1;

// todo: factor out #mutsig_dialog

// initialize and bind for
// mutsig toggle button and mutsig dialog box
var initMutsigDialogue = function() {
    "use strict";

    // initialize mutsig button
    // as hidden, and with JQuery UI style
    $('#toggle_mutsig_dialog').hide();
    $('#toggle_mutsig_dialog').button();

    // set up modal dialog box for mutsig table (step 3)
    $('#mutsig_dialog').dialog({autoOpen: false,
        resizable: false,
        modal: true,
        minHeight: 315,
        minWidth: 636
        });

    // set listener for mutsig select button
    $('#select_mutsig').click(function() {
        $('#mutsig_dialog').dialog('close');
    });

    // set listener for mutsig cancel button
    $('#cancel_mutsig').click(function() {

        // close dialog box
        $('#mutsig_dialog').dialog('close');

        // clear all checks
        $('.MutSig :checkbox').attr('checked', false);
    });

    // make checkall check all
    $('#mutsig_dialog .checkall').live('click', function() {
        $(this).parents().find('.MutSig input').attr('checked', this.checked);
    });

    // bind UI for mutsig table -> gene list
    $('#select_mutsig').click(updateGeneList);

    // bind UI for gene list -> mutsig table
    // todo: is this not working right for some reason?
    $('#gene_list').change(function () {
        updateMutSigTable();
    });

    listenCancerStudy();
}

// listen for a change in the selected cancer study and
// handle appropriately
// todo: refactor this
var listenCancerStudy = function() {
    $('#select_cancer_type').change( function() {

        // get the cancer study (i.e. tcga_gbm)
        var cancerStudyId = $('#select_cancer_type').val();

        // if the selected cancer study has mutsig data,
        // show the mutsig button
        if (window.json.cancer_studies[cancerStudyId].has_mutsig_data) {
            $('#toggle_mutsig_dialog').show();
        } else {
            $('#toggle_mutsig_dialog').hide();
        }
    });
}

//todo: rewrite this function. it is unnecessarily complex
var mutsig_to_tr = function(mutsig) {
    "use strict";
    var click_append = $('<input>');
    var tr = $('<tr>');
    var td = $('<td>');

    click_append.attr('type', 'checkbox');
    click_append.attr('value', mutsig.gene_symbol);


    td.append(mutsig.gene_symbol);
    tr.append(td);

    td = $('<td>');
    td.append(mutsig.num_muts);
    tr.append(td);

    td = $('<td>');
    td.append(mutsig.qval);
    tr.append(td);

    tr.append($('<td>').append(click_append));

    return tr;
};

// Displays the modal dialog for the mutsig table
var promptMutsigTable = function() {
    "use strict";

    // open the dialog box
    $('#mutsig_dialog').dialog('open');

    // this was the last cancer study selected,
    // no need to redo the query
    if (CANCER_STUDY_SELECTED === cancerStudyId) {
        $('#mutsig_dialog #loader-img').hide();
        return;
    }

    // hide everything but the loader image
    $('#mutsig_dialog').children().hide();
    $('#mutsig_dialog #loader-img').show();

    // grab data to be sent to the server
    var cancerStudyId = $('#select_cancer_type').val();

    // save the selected cancer study for later
    CANCER_STUDY_SELECTED = cancerStudyId;

    // prepare data to be sent to server
    var data = {'selected_cancer_type': cancerStudyId };

    // reset the mutsig table if it has already been formatted
    if (DATATABLE_FORMATTED !== -1) {

        // remove dataTables formatting
        DATATABLE_FORMATTED.fnDestroy();

        // delete all elements
        $('.MutSig tbody').empty();
    }

    // do AJAX
    $.get('MutSig.json', data, function(mutsigs) {
        var i;
        var len = mutsigs.length;

        // append MutSig data to table
        for (i = 0; i < len; i += 1) {
            $('.MutSig tbody').append(mutsig_to_tr(mutsigs[i]));
        }

        // show everything but the loader image
        $('#mutsig_dialog').children().show();
        $('#mutsig_dialog #loader-img').hide();

        // use dataTable jQuery plugin
        // to style and add nice functionalities
        DATATABLE_FORMATTED = $('.MutSig').dataTable( {
            "sScrollY": "200px",
            "bJQueryUI": true,
            "aaSorting": [],
            "bAuthWidth": false,
            "aoColumns": [
                null,
                null,
                null,
                { "bSortable" : false, "sWidth": "5%" }
            ],
            "bPaginate": false,
            "bFilter": false,
            "iDisplayLength": 5,
            "bRetrieve": true,
            "bDestroy": true
        } );

        // force columns to align correctly
        DATATABLE_FORMATTED.fnDraw();
        DATATABLE_FORMATTED.fnDraw();
    });

    // show everything but loader image
    $('#mutsig_dialog').children().show();
    $('#mutsig_dialog #loader-img').hide();

    return;
};

// updates the gene_list based on what happens in the MutSig table.
// mutsig table (within mutsig dialog box) -> gene list
var updateGeneList = function() {
    "use strict";

    // push all the genes in the gene_list onto a list
    var gene_list = $('#gene_list').val();

    // if gene_list is currently empty put all the checked mutsig genes into it.
    if (gene_list === "") {

        gene_list = [];
        $('.MutSig :not(.checkall):checked').map(function() {     // don't select the Select All checkbox
            gene_list.push($(this).val());
        });
        gene_list = gene_list.join(" ");
    }

    else {
        // look for the selected mutsigs in gene_list
        // if they're not there, append them
        $('.MutSig :not(.checkall):checked').each(function() {
            var checked = $(this).val();

            if ( gene_list.search(new RegExp(checked, "i")) === -1 ) {
                gene_list = gene_list.replace(/ $/ig, "");              // delete trailing space
                //todo: $.trim
                checked = " " + checked;
                gene_list += checked;
            }
        });

        // look for the unselected mutsigs in the gene_list
        // if they're there, delete them
        // you should be forced to know that your gene is recurrently mutated
        $('.MutSig input:not(.checkall):not(:checked)').each(function() {
            var unchecked = $(this).val();
            if ( gene_list.search(new RegExp(unchecked, "i")) !== -1) {

                // likely to be Onco Query
                if (gene_list.search(':') !== -1) {
                    var unchecked_regexp = new RegExp(new RegExp(unchecked).source + /\s*:\s*.*(\;|\n)/.source, "ig");
                    gene_list = gene_list.replace(unchecked_regexp, "");
                    console.log(unchecked_regexp);
                }

                // still want to remove the gene even if it is not part of a (nontrivial) onco query statement
                var unchecked_regexp = new RegExp(new RegExp(unchecked).source + /\s?/.source, "ig");    // regexp of unchecked + \s
                gene_list = gene_list.replace(unchecked_regexp, "");
            }
        });
    }

    $('#gene_list').val(gene_list);

    // remove spaces in gene_list
    gene_list = gene_list.replace(/\s{2,}/, "");            // delete 2 or more spaces in a row
    gene_list = gene_list.replace(/^ /ig, "");              // delete leading space
};

// updates the MutSig table based on what happens in the gene_list
// namely, a user's deletions or additions of genes
// gene_list -> MutSig table
var updateMutSigTable = function() {
    var gene_list = $('#gene_list').val();

    // don't want to even look at Onco Queries like these,
    gene_list = gene_list.replace(/DATATYPES.*(;|\n)\s?/g, "");

    // likely to be Onco Query
    if (gene_list.search(/:/) !== -1) {
        var commands = gene_list.split("\n"),
            commands_len = commands.length,
            genes = [],
            i;

        for (i = 0; i < commands_len; i += 1) {
            _commands = commands[i].split(";");

            var _commands_len = _commands.length, j;
            for (j = 0; j < _commands_len; j += 1) {
                genes.push(_commands[j]
                        .replace(/:.*/g, ""));
            }
        }
        gene_list = genes;
    }

    else {
        gene_list = gene_list.split(" ");
    }

    // clear all checks
    $('#mutsig_dialog .MutSig :checkbox').attr('checked', false);

    // if genes in the gene_list are added
    // check them in the mutsig table
    var i,
        gene_list_len = gene_list.length;
    for (i = 0; i < gene_list_len; i += 1) {
        // select mutsig checkboxes that are in the gene list
        $('#mutsig_dialog .MutSig :checkbox[value=' +  gene_list[i].toUpperCase() + ']').
            attr('checked', true);

        //$('#mutsig_dialog .MutSig :checkbox:[value=' +  gene_list[i].toUpperCase() + ']').
            //attr('checked', true);
    }
    return false;
}

// todo: refactor this and put it in with other init functions in step3.json
$(document).ready( function () {
    "use strict";
    initMutsigDialogue();
});
