/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

// AJAX and Dynamic jQuery for MutSig table
//
// AJAX :
//           Connects to portal via AJAX and downloads JSON document containg MutSig information:
//              gene symbol, num mutations, q-value
//
// Dynamic jQuery:
//          Handles changes in select_single_study:
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

    // bind UI for mutsig table -> gene list
    $('#select_mutsig').click(updateGeneList);

    // bind UI for gene list -> mutsig table
    $('#gene_list').change( function() {
        if ($('mutsig_dialog').dialog('isOpen')) {
            updateMutSigTable();
        }
    });

    listenCancerStudy();
};

// listen for a change in the selected cancer study and
// handle appropriately
// todo: refactor this
var listenCancerStudy = function() {
    $('#select_single_study').change( function() {

        // get the cancer study (i.e. tcga_gbm)
        var cancerStudyId = $('#select_single_study').val();

        // if the selected cancer study has mutsig data,
        // show the mutsig button
        if (window.json.cancer_studies[cancerStudyId].has_mutsig_data) {
            $('#mutsit-gistic-div').show();
        } else {
            $('#toggle_mutsig_dialog').hide();
        }
    });
};

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

    // grab data to be sent to the server
    var cancerStudyId = $('#select_single_study').val();

    // open the dialog box
    $('#mutsig_dialog').dialog('open');

    // this was the last cancer study selected,
    // no need to redo the query
    if (CANCER_STUDY_SELECTED === cancerStudyId) {
        return;
    }

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
        $('#mutsig_dialog').children().hide();
        $('#mutsig_dialog #loader-img').show();

    // do AJAX
    $.get('MutSig.json', data, function(mutsigs) {
        var i;
        var len = mutsigs.length;

        // hide everything but the loader image
        // this is here because of the *A* in AJAX
        $('#mutsig_dialog').children().hide();
        $('#mutsig_dialog #loader-img').show();

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
                { "sType" : "numeric"},
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

        // bind UI for gene list -> mutsig table
        updateMutSigTable();

        // create bindings for checkall mutsig UI
        checkallMutsig();
    });

    // show everything but loader image
    $('#mutsig_dialog').children().show();
    $('#mutsig_dialog #loader-img').hide();


    return;
};

// binds UI for checkall mutsig checkbox
var checkallMutsig = function() {
    // make checkall check all
    $('#mutsig_dialog .checkall').click(function() {
        $(this).parents().find('.MutSig input').attr('checked', this.checked);
    });

    // checkall is checked iff. all mutsigs are checked
    $('.MutSig :not(.checkall):checkbox').click(function() {
        // if a box is unchecked (namely *this* box)
        // the checkall is unchecked
        if (!this.checked) {
            $('.MutSig .checkall').attr('checked', false);
            return;
        }
        // check if the number of unchecked boxes equals 0
        // if so, checkall should be checked
        if ($('.MutSig input:not(.checkall):not(:checked)').length === 0) {
            $('.MutSig .checkall').attr('checked', true);
        }
    });
}

// updates the gene_list based on what happens in the MutSig table.
// mutsig table (within mutsig dialog box) -> gene list
var updateGeneList = function() {
    "use strict";

    // push all the genes in the gene_list onto a list
    var gene_list = $('#gene_list').text();

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
                gene_list = $.trim(gene_list);
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
                // delete the entire OncoQuery statement associated with an unselected gene
                if (gene_list.search(':') !== -1) {
                    var unchecked_regexp = new RegExp(new RegExp(unchecked).source + /\s*:\s*.*(\;|\n)/.source, "ig");
                    console.log(unchecked_regexp);
                    gene_list = gene_list.replace(unchecked_regexp, "");
                }

                // still want to remove the gene even if it is not part of a (nontrivial) onco query statement
                var unchecked_regexp = new RegExp(new RegExp(unchecked).source + /\s?/.source, "ig");    // regexp of unchecked + \s
                gene_list = gene_list.replace(unchecked_regexp, "");
            }
        });
    }

    $('#gene_list').val(gene_list);

    // remove spaces in gene_list
    gene_list = $.trim(gene_list);
    gene_list = gene_list.replace(/\s{2,}/, "");            // delete 2 or more spaces in a row
};

// updates the MutSig table based on what happens in the gene_list
// namely, a user's deletions or additions of genes
// gene_list -> MutSig table
var updateMutSigTable = function() {
    var gene_list = $('#gene_list').text();

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
        // split on " " and "\n"
        gene_list = gene_list.split(" ").map(function(i) { return i.split("\n"); });
        // flatten the list of lists
        gene_list = gene_list.reduce(function(x,y) { return x.concat(y); });
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
    }
}

// todo: refactor this and put it in with other init functions in step3.json
$(document).ready( function () {
    "use strict";
    initMutsigDialogue();
});
