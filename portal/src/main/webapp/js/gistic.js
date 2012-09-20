var selected_cancer_type = 'tcga_gbm';

$.get('Gistic.json', {'selected_cancer_type': selected_cancer_type}, function(data) {
    // get data, make gistic object, draw table

    var gistic = Gistic(data);

    var gistic_table_el = $('#gistic_table');

    gistic.drawTable(gistic_table_el, { "sScrollY": "75%", "bPaginate": false });

});

var Gistic = function(gistics) {

    var dt = '';
    // hold the DataTable object once its been rendered

    $('#gistic_dialog')
    .dialog({autoOpen: false,
            // set up modal dialog box for gistic table (step 3)
            //resizable: false,
            modal: true,
            width: 'auto',
            //height: 500,
            //minWidth: 636,
            //overflow: 'hidden',
            open: function() {
                // sets the scrollbar to the top of the table
                $(this).scrollTop(0);

                // workaround to prevent auto focus
                //$(this).add('input').blur();
            }
    });

    window.expandGisticGenes = function(el) {
        // shows/hides additional genes in the genes column
        // currently initializing to hiding all non-Sanger genes
        el = $(el).parents()[0];

        var spans = $(el).children();
        spans = $(spans).select('span');

        $(spans).toggle();
    };

    var sort_by_cytoband = function(x,y) {
        // compares two cytobands in the cytoband column
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

    window.ioGeneSet = function(el) {
        // synchs between selected genes and the get set on the main page
        var val = $(el).html();
        console.log(val);
    };

    var get_aaData = function() {
        // returns the aaData for DataTables

        return gistics.map(function(i) {
            // map over every gistic object

            var hidden_genes = '';
            if (i.nonSangerGenes.length !== 0) {

                var hidden_genes = i.nonSangerGenes.map(function(g) {
                    // bind ioGeneSet to each nonSanger gene
                    return "<span class='gistic_gene' onClick=ioGeneSet(this);>" + g + "</span>";
                });

                // set up hidden genes
                hidden_genes = "<div style='display:none;'>" + hidden_genes.join(" ") + "</div>";
                hidden_genes = '<span onClick=expandGisticGenes(this);> +' + i.nonSangerGenes.length  + ' more</span>'
                    + '<span style="display:none;" onClick=expandGisticGenes(this);> less</span>'
                    + hidden_genes;
            }

            var all_genes = i.sangerGenes.map(function(g) {
                // bind ioGeneSet to each Sanger gene
                return "<span onClick=ioGeneSet(this);>" + g + "</span>";
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

            dt = table_el.dataTable(options);

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
