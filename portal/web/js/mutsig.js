//
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

var studySelected = function() {
    "use strict";
    var cancerStudyId = $('#select_cancer_type').val();

    if (json.cancer_studies[cancerStudyId].has_mutsig_data) {
        $('#MutSig_view').show();
        $('.MutSig_wrapper').show();
    }
    else {
        $('#MutSig_view').hide();
        $('.MutSig_wrapper').hide();
    }

    // redo the query for every nontrivial selection
    $('.MutSig_wrapper').before('<table class="MutSig"></table>');
    $('.MutSig_wrapper').remove();

    // if you select another cancer study, reset the arrow
    $($('#MutSig_view > .ui-icon')[0]).show();
    $($('#MutSig_view > .ui-icon')[1]).hide();

    // ignores '<', e.g. '<1E-8' === '1E-8'
    $.fn.dataTableExt.oSort['scientific-asc'] = function(a,b) {
        a = $('<div />').html(a).text();    // parse html entity &lt;
        b = $('<div />').html(b).text();

        a = parseFloat(a.replace(/^[<>]/g,""));
        b = parseFloat(b.replace(/^[<>]/g,""));

        return ((a < b) ? -1 : ((a > b) ? 1 : 0));
    };

    $.fn.dataTableExt.oSort['scientific-desc'] = function(a,b) {
        return $.fn.dataTableExt.oSort['scientific-asc'](b,a);
    };

    $.get('MutSig.json',
            {'selected_cancer_type': cancerStudyId},
            function(mutsigs) {
                    var i;
                    var len = mutsigs.length;

                    // header
                    $('.MutSig').html('<thead><tr>'
                                    + '<th>Gene Symbol</th>'
                                    + '<th>Num Mutations</th>'
                                    + '<th>Q-Value</th>'
                                    + '<td><input class="checkall" type="checkbox"></td>'
                                    + '</thead></tr>');

                // append MutSig data to table
                    for (i = 0; i < len; i += 1) {
                        $('.MutSig').append(mutsig_to_tr(mutsigs[i]));
                    }

                // use dataTable jQuery plugin
                // to style and add nice functionalities
                    $.fn.dataTableExt.oStdClasses.sWrapper = "MutSig_wrapper";

                    $('.MutSig').dataTable( {
                        "sScrollY": "200px",
                        "aaSorting": [],     // disable autosorting
                        "aoColumns": [
                            null,
                            null,
                            { "bSortable" : true, "sType" : "scientific" },
                            { "bSortable" : false, "sWidth": "5%" }
                        ],
                        "bPaginate": false,
                        "bFilter": false,
                        "iDisplayLength": 5,
                        "bRetrieve": true,
                        "bDestroy": true
                    } );

                    $('.MutSig_wrapper').hide();        // hide MutSig table initially
                    $('.MutSig_wrapper').css('padding-bottom', '25px');
                    $('.MutSig').css('width', '0%');    // hack. keep columns in line


                return false;
            });
};

// updates the gene_list based on what happens in the MutSig table.
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
    gene_list = gene_list.replace(/\s{2,}/, "");             // delete 2 or more spaces in a row
    gene_list = gene_list.replace(/^ /ig, "");              // delete leading space
};

// updates the MutSig table based on what happens in the gene_list
// namely, a user's deletions or additions of genes
var updateMutSigTable = function() {

    var gene_list = $('#gene_list').val();

    gene_list = gene_list.replace(/DATATYPES.*(;|\n)\s?/g, "");      // don't want to even look at Onco Queries like these

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
    $('.MutSig :checkbox').attr('checked', false);

    // if genes in the gene_list are added
    // check them in the mutsig table
    var i,
        gene_list_len = gene_list.length;
    for (i = 0; i < gene_list_len; i += 1) {
        // select mutsig checkboxes that are in the gene list
        $('.MutSig :checkbox:[value=' +  gene_list[i].toUpperCase() + ']').
            attr('checked', true);

    }
    return false;
}

$(document).ready( function () {
    "use strict";

// -- initialize --
// bind handlers to events
    $('#MutSig_view').hide();   // MutSig toggle and table is initialized as hidden
    $('.MutSig_wrapper').hide();

    // make checkall check all
    $('.checkall').live('click', function() {
        $(this).parents().find('.MutSig input').attr('checked', this.checked);
    });

    $('.MutSig input').live('click', updateGeneList);
    updateGeneList();

    $('#gene_list').change(function () {
        if ( $('.MutSig').is(':visible') ) {
            updateMutSigTable();
        }
    });

    $('#toggle_mutsig').click(function() {
        $($('#MutSig_view > .ui-icon')[0]).toggle();
        $($('#MutSig_view > .ui-icon')[1]).toggle();
        updateMutSigTable();
        $('.MutSig_wrapper').slideToggle('slow');
        return false;
    });

    // make sure that the ui-icon is in the right place
    $('#gene_list').keyup(function() {
        if ( $('.MutSig').is(':visible') ) {
            $('.MutSig_wrapper').slideUp('slow');

            $($('#MutSig_view > .ui-icon')[0]).show();      // right arrow
            $($('#MutSig_view > .ui-icon')[1]).hide();      // down arrow
        }
    });
// -- end initialize --

    $('#select_cancer_type').change( function() {
        studySelected();
    });
});
