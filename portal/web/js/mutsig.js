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
    if (cancerStudyId === null || cancerStudyId === "all") {
        $('#MutSig_view').hide();
        $('.MutSig_wrapper').hide();    // if MutSig table already exists
        return;
    }

    // redo the query for every nontrivial selection
    // obvious downside: doesn't cache previous selections
    // perhaps the browser does this for us!
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
                if (mutsigs.length !== 0) {
                    $('#MutSig_view').show();

                    var i;
                    var len = mutsigs.length;

                    // header
                    $('.MutSig').html('<thead><tr>'
                                    + '<th>Gene Symbol</th>'
                                    + '<th>Num Mutations</th>'
                                    + '<th>Q-Value</th>'
                                    + '<td><input class="checkall" type="checkbox">&nbspSelect All</td>'
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
                        "aoColumns": [
                            null,
                            null,
                            { "bSortable" : true, "sType" : "scientific" },
                            { "bSortable" : false }
                        ],
                        "aaSorting": [ [2,'asc'] ],
                        "bPaginate": false,
                        "bFilter": false,
                        "iDisplayLength": 5,
                        "bRetrieve": true,
                        "bDestroy": true
                    } );

                    $('.MutSig_wrapper').hide();        // hide MutSig table initially
                    $('.MutSig_wrapper').css('padding-bottom', '25px');
                    $('.MutSig').css('width', '0%');    // hack. keep columns in line
                }

                else {    // there are no MutSigs
                    $('#MutSig_view').hide();
                }

                // initially set table to hide
                // wait for user to toggle
            $('.MutSig_wrapper').hide();

                return false;
            });
};


var updateGeneList = function() {
    "use strict";
    var genes = [];
    $('.MutSig :not(.checkall):checked').each(function(i) {
        genes.push((i !== 0 ? " " : "") + $(this).val());
    });
    $('#gene_list').val((genes.toString()).replace(/,/g,''));
};


$(document).ready( function () {
    "use strict";

// initialize
// bind handlers to events
    $('.checkall').live('click', function() {
        $(this).parents().find('.MutSig input').attr('checked', this.checked);
    });

    $('.MutSig input').live('click', updateGeneList);
    updateGeneList();

    $('#MutSig_view').hide();
    $('#toggle_mutsig').click(function() {
        $($('#MutSig_view > .ui-icon')[0]).toggle();
        $($('#MutSig_view > .ui-icon')[1]).toggle();
        $('.MutSig_wrapper').toggle();
        return false;
    });
// end initialize

    $('#select_cancer_type').change( function() {
        studySelected();
    });
});
