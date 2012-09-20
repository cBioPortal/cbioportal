var selected_cancer_type = 'tcga_gbm';

$.get('Gistic.json', {'selected_cancer_type': selected_cancer_type}, function(data) {
    // get data, make gistic object, draw table

    var gistic = Gistic(data);

    var gistic_table_el = $('#gistic_table');

    gistic.drawTable(gistic_table_el, { "sScrollY": "200px", "bPaginate": false});

});

var Gistic = function(gistics) {

    // store the DataTable object once it has been created
    Gistic.dt = '';

    Gistic.gene_list_el = $('#gene_list');

    // initialize Gistic's internal tracking of the Gene Set
    Gistic.geneSet = GeneSet(Gistic.gene_list_el.val());

    // set up modal dialog box for gistic table (step 3)
    $('#gistic_dialog').dialog( {autoOpen: false,
            modal: true,
            overflow: 'hidden',
            minWidth: 636,
            //resizable: false,
            //height: 500,
            // width: 'auto',
            open: function() {
                // sets the scrollbar to the top of the table
                $(this).scrollTop(0);

                // workaround to prevent auto focus
                //$(this).add('input').blur();
            }
    });

    var sort_by_cytoband = function(x,y) {
        // sorts two cytobands,
        // returns 1 if x < y, -1 if x > y, and 0 if x == y
        var _x = x.match(/^([1-9]{1,2})([pq])([1-9]{1,2})(?:\.?)([0-9]{0,2})$/);
        var _y = y.match(/^([1-9]{1,2})([pq])([1-9]{1,2})(?:\.?)([0-9]{0,2})$/);

        if (parseInt(_x[1]) - parseInt(_y[1]) !== 0) {
            return parseInt(_x[1]) - parseInt(_y[1]);
        } else if (_x[2] === 'p' && _y[2] === 'q') {
            return -1;
        } else if (_x[2] === 'q' && _y[2] === 'p') {
            return 1;
        } else if (_x[2] === _y[2]) {
            return parseInt(x[3]) - parseInt(y[3]);
        } else {
            console.log('error: cytoband sorting logic fell through');
        }
    };

    $.extend( $.fn.dataTableExt.oSort, {
        // bind the cytoband sorting function
        "cytoband-asc": sort_by_cytoband,

        "cytoband-desc": function(x,y) {
            return -1 * sort_by_cytoband(x,y);
        }
    } );

    var get_aaData = function() {
        // returns the aaData for DataTables

        return gistics.map(function(i) {
            // map over every gistic object

            var hidden_genes = '';
            if (i.nonSangerGenes.length !== 0) {

                var hidden_genes = i.nonSangerGenes.map(function(g) {
                    // bind ioGeneSet to each nonSanger gene
                    return "<span class='gistic_gene' onClick=Gistic.UI.ioGeneSet(this);>" + g + "</span>";
                });

                // set up hidden genes
                hidden_genes = "<div style='display:none;'>" + hidden_genes.join(" ") + "</div>";
                hidden_genes = ' <a href="javascript:void(0)" onclick=Gistic.UI.expandGisticGenes(this);>+' + i.nonSangerGenes.length  + ' more</a>'
                    + '<a href="javascript:void(0)" style="display:none;" onclick=Gistic.UI.expandGisticGenes(this);> less</a>'
                    + hidden_genes;
            }

            var all_genes = i.sangerGenes.map(function(g) {
                // bind ioGeneSet to each Sanger gene
                return "<span onClick=Gistic.UI.ioGeneSet(this);>" + g + "</span>";
            })
                .join(" ");

            all_genes = all_genes + hidden_genes;

            return [i.ampdel, i.chromosome, i.cytoband, i.peakStart, i.peakEnd, all_genes, i.qval, i.res_qval];
        });
    };

    var self = {
        get_aaData : get_aaData,

        getDt: function() {return dt;},

        drawTable : function(table_el, options) {
            // draws a DataTable in the specific DOM element, table_el
            // with the specified DataTable options

            var aaData = get_aaData();

            var aoColumns = [
                {"sTitle": "AD",    // todo : ampdel tiptip?
                    "fnRender": function(obj) {
                        var sReturn = obj.aData[obj.iDataColumn];

                        if (sReturn === true) {     // true means amplified
                            // mark amps/dels as reds and blues
                            return "<div class=\"gistic_amp\"></div>"
                        } else {
                            return "<div class=\"gistic_del\"></div>"
                        }
                    },
                    "sWidth": '10px'
                },
                {"sTitle": "Chr", "bSearchable": false},
                {"sTitle": "Cytoband", "bSearchable": false, "sType": "cytoband"},
                {"sTitle": "Peak Start", "bVisible": false, "bSearchable": false},
                {"sTitle": "Peak End", "bVisible": false, "bSearchable": false},
                {"sTitle": "Genes"},
                {"sTitle": "Q-Value", "sWidth": "100px", "bSearchable": false,
                    "fnRender": function(obj) {
                        var sReturn = obj.aData[obj.iDataColumn];
                        return parseFloat(sReturn) .toExponential(1);   // round Q-Values
                    }
                },
                {"sTitle": "Res Q-Value", "bVisible": false, "bSearchable": false},
            ];

            options.aaSorting = [[ 6, "asc" ]];     // sort Q-Value column on load
            options.oLanguage = {'sSearch': 'Filter by Gene:'};
            options.aaData = aaData;
            options.aoColumns = aoColumns;

            Gistic.dt = table_el.dataTable(options);

            // paint regions red and blue
            $('.gistic_amp').parent().css('background-color', 'red');
            $('.gistic_del').parent().css('background-color', 'blue');
        },

        getGistics: function() {
            return gistics;
        }
    };

    return self;
};

Gistic.UI = {
    // dump of all sorts of UI functions

    open_dialog : function() {
        var gistic_dialog_el = $('#gistic_dialog');

        var gs = GeneSet(Gistic.gene_list_el.val());

        console.log(gs);
        console.log(gs.getAllGenes());

        gistic_dialog_el.dialog('open');

        // redraw table
        Gistic.dt.fnDraw();

    },

    expandGisticGenes : function(el) {
        // shows/hides additional genes in the genes column
        // currently initializing to hiding all non-Sanger genes

        el = $(el).parents()[0];
        el = $(el).children();

        // grab all the elements
        var plusXmore = el[1];
        var less = el[2];
        var hidden = el[3];

        // and toggle them
        $(plusXmore).toggle();
        $(less).toggle();
        $(hidden).toggle('slow');
    },

    ioGeneSet : function(el) {
        // synchs between selected genes and the get set on the main page
        var val = $(el).html();

        // toggle bold
        if (val.match(/<b>/) === null) {
            $(el).html("<b>" + val + "</b>");
        } else {
            $(el).html(val.replace(/<b>/, '').replace(/<\/b>/, ''));
        }
    },

    updateGenes: function() {

        console.log('hello world');
    }
};
