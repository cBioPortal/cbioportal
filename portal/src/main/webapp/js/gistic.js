// namespace for this program
var Gistic = {};

// synced with the java Gistic.class
Gistic.AMPLIFIED = true;
Gistic.DELETED = false;

// set global variables
$(document).ready(function() {
    Gistic.table = document.getElementById('gistic_table');       // avoid embedding this in the html
    Gistic.table = new google.visualization.Table(Gistic.table);

    Gistic.data = {};
});

Gistic.initDialog = function() {

    // set up modal dialog box for gistic table (step 3)
    $('#gistic_dialog').dialog({autoOpen: false,
        resizable: false,
        modal: true,
//        height: 500,
        //minWidth: 636,
        //overflow: 'hidden',
        open: function() {
            // sets the scrollbar to the top of the table
            // todo: downside -- doesn't save scroll location
            $(this).scrollTop(0);
        }
        });
};

// google table
google.load('visualization', '1', {packages:['table']});

Gistic.GISTICS = "";
Gistic.get = function(cancerStudyId) {

    // data to be sent to the server
    var data = {'selected_cancer_type': cancerStudyId };

    // global variables that match the order of the columns below
    Gistic.AMPDEL_COL = 0;
    Gistic.CHR_COL = 1;
    Gistic.PEAK_START_COL = 2;
    Gistic.PEAK_END_COL = 3;
    Gistic.QVAL_COL = 4;
    Gistic.RES_QVAL_COL = 5;
    Gistic.GENES_COL = 6;
    Gistic.NO_GENES_COL = 7;


    // server request
    $.get('Gistic.json', data, function(gistics) {

        var dataTable = new google.visualization.DataTable();

        // process response
        // make a DataTable object and save to the Gistic object
        dataTable.addColumn('number', '<span style="color:red">Amp</span>' +
            '<span style="color:blue">Del</span>');
        dataTable.addColumn('number', 'Chr');
        dataTable.addColumn('number', 'Peak Start');
        dataTable.addColumn('number', 'Peak End');
        dataTable.addColumn('number', 'Q-Value');
        dataTable.addColumn('number', 'Residual Q-Value');
        dataTable.addColumn('string', 'Genes');
        dataTable.addColumn('number', 'No Genes');

        var i,
        row,
        len = gistics.length;
        for (i = 0; i < len; i+=1) {
            row = [gistics[i].ampdel ? 1 : 0,       // amp = true ; del = false
                gistics[i].chromosome,
                gistics[i].peakStart,
                gistics[i].peakEnd,
                gistics[i].qval,
                gistics[i].res_qval,
                gistics[i].genes_in_ROI.join(" "),
                gistics[i].genes_in_ROI.length ];
            dataTable.addRow(row);
        }

        // save the data in the Gistic object
        Gistic.data = dataTable;
    });
};

// returns a google vis View
// renders Genes column based on what is currently in the Gene Set (makes genes bold)
// renders AmpDel Column with colors
Gistic.makeView = function(data, genes_list) {

    var view = new google.visualization.DataView(data);

    var genes_column_view = {
        calc: function(dt, row) {
                var genes_in_ROI = dt.getValue(row, Gistic.GENES_COL),
                    j,
                    len = genes_list.length;

                //{{{ disable boldening for now
                //for (j = 0 ; j < len ; j+=1) {
                //    var gene = genes_list[j];

                //    // matches the gene followed by any type of space
                //    // (avoid matching part of a gene symbol string)
                //    // 1st capturing group : leading space or beginning of line
                //    // 2st capturing group : just the gene symbol
                //    // 3st capturing group : the space or EOL
                //    var gene_regexp = "(\\s|^)(" + gene + ")" + "(\\s|$)";
                //    gene_regexp = new RegExp(gene_regexp, "i");

                //    // put bolds around the gene symbol only,
                //    // not the space
                //    genes_in_ROI = genes_in_ROI.replace(gene_regexp,
                //            '$1' + "<b>" + '$2'.toUpperCase() + "</b>" + '$3');
                //}
                //}}}

                // finds the last \s character before a cutoff in a str
                // unless the str is shorter than the cutoff, in which case,
                // return length of str
                var maximal_space = function(str, cutoff) {

                    //str = str.replace(/<b>/g,'').replace(/<\/b>/g, '');               // hack

                    var str_len = str.split("").length;                                 // hack

                    if (str_len < cutoff) { return str_len; }

                    if (str.charAt(cutoff) === ' ') {
                        return cutoff;
                    } else if (cutoff === 0) {
                        return str_len;
                    } else {
                        return maximal_space(str, cutoff - 1);
                    }
                };

                var cutoff = 48;        // === 42 + 6, see H2G2, this is the magic number for the window
                return genes_in_ROI.slice(0, maximal_space(genes_in_ROI, 48));
        },
        label: 'Genes',
        type: 'string'};

    // omitting Residual Q-Values, Peak Start, and Peak End
    // hopefully we can display chromosome locations as a mini-IQV picture

    view.setColumns([Gistic.AMPDEL_COL,
            Gistic.CHR_COL,
            Gistic.QVAL_COL,
            Gistic.NO_GENES_COL]);
            //genes_column_view ]);
            // disable boldening for now

    return view;
};

// dev
$(document).ready(function() {
    Gistic.initDialog();
    Gistic.get("tcga_gbm");     // eventually this will be a button
});

// -- Gistic UI -- //
Gistic.UI = {};

// box : a jquery element
Gistic.UI.get_genes_in_box = function(box) {
    var str = box.val();

    // normally genes are separated by white space
    var genes = str.split(/\s+/);

    // sometimes an empty string will get split out
    // this breaks other functionality
    genes = genes.filter(function(x) {
        return x !== "";
    });

    // remove duplicates
    genes = uniqueElementsOfArray(genes);

    // todo : deal with OQL and other tricky things here

    return genes;
};

Gistic.UI.open_gistic_dialog = function() {

    var table = Gistic.table;

    // Q-Value rounding
    var qval_format = { pattern:'#.#E0' };
    qval_format = new google.visualization.NumberFormat(qval_format);
    qval_format.format(Gistic.data, Gistic.QVAL_COL);

    // Amp del coloring
    var ampdel_format = new google.visualization.ColorFormat();
    ampdel_format.addRange(-0.5, 0.5, 'blue', 'blue');    // deleted
    ampdel_format.addRange(0.5, 1.5, 'red', 'red');      // amplified
    ampdel_format.format(Gistic.data, Gistic.AMPDEL_COL);

    // if you fix the height of the google visualization,
    // then you must open the dialog first and draw later
    // why?
    $('#gistic_dialog').dialog('open');

    var raw_gene_str = $('#gene_list').val(),
        genes = GeneSet(raw_gene_str) || [];

    genes = genes.getAllGenes();

    var view = Gistic.makeView(Gistic.data, genes);
    var options = {'allowHtml': true,
        'height': '430px',
        'sortColumn': 2};

    table.draw(view, options);

    // nothing is selected when the table first opens
    table.setSelection(null);

    return false;
};

// concatenates the selected rows in the gene list into one string
// rows are separated by newlines
Gistic.UI.catSelected = function(data, table, genes_col_no) {
    var selected = table.getSelection(),
        len = selected.length;
    var i,
        genes = '';
    for (i = 0 ; i < len; i+=1) {
        genes += data.getValue(selected[i].row, genes_col_no) + '\n';
    }

    return genes;
};

// action to be taken upon clicking of the select button
Gistic.UI.select_button = function() {

    var genes_str = Gistic.UI.catSelected(Gistic.data, Gistic.table, Gistic.GENES_COL);

    // if nothing is selected, do nothing
    if (genes_str === '') {
        return;
    }

    // add selected genes to gene_list.
    // todo: might want to do some filtering to prevent duplicates...
    var raw_geneSet_str = $('#gene_list').val(),
        prefix = '';
    if (raw_geneSet_str !== '') {       // put added genes on a new line
        prefix = '\n';
    }

    genes_str = $('#gene_list').val() + prefix + genes_str;
    $('#gene_list').val(genes_str);

    $('#gistic_dialog').dialog('close');
};

// action to be taken upon clicking of the cancel button
// note : this is hard wired to the html *id* tag of the dialog
Gistic.UI.cancel_button = function(dialog) {
    $('#gistic_dialog').dialog('close');
    return;
};

//Gistic.GO = function(where, hg) {
//// where is DOM object
//// hg is a list of chromosome lengths in order 
//// (chr#1, chr#2, ... , chr#X, chr#Y) with zero in front,
//// i.e. hg[0] = 0
//
//    make
//
//};

// make the gistic button a JQueryUI button
$(document).ready(function() {
    $('#toggle_gistic_dialog_button').button();
});
