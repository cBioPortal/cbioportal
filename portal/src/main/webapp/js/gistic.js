var Gistic = function(gistics) {
    // store the DataTable object once it has been created
    Gistic.dt = '';

    Gistic.gene_list_el = $('#gene_list');
    Gistic.dialog_el = $('#gistic_dialog');

    var sort_by_cytoband = function(x,y) {
        // sorts two cytobands,
        // where a cytoband in an array of strings, e.g. [12, p, 11, 5] ~ 12p11.5

        if (parseInt(x[1]) - parseInt(y[1]) !== 0) {
            return parseInt(x[1]) - parseInt(y[1]);
        } else if (x[2] === 'p' && y[2] === 'q') {
            return -1;
        } else if (x[2] === 'q' && y[2] === 'p') {
            return 1;
        } else if (x[2] === y[2]) {
            return parseInt(x[3]) - parseInt(y[3]);
        } else {
            console.log('error: cytoband sorting logic fell through');
        }
    };

    var drawGenes = function(genes, enteredGenes, search) {
        // - genes :            the set of genes to deal with
        // - enteredGenes :     the genes to make bold
        // - search :           the filter string, maybe someday we'll want to incorporate
        //                      this
        //
        // draws the cells in the gene column with all of their special UI
        // features and CSS bindings
        // also makes the appropriate genes bold

        search = search || '';

        genes = $.map(genes, function(g, i) {
            // bind ioGeneSet to each gene
            // bold ones that are already in the gene list

            var bold = '';
            if ($.inArray(g, enteredGenes) !== -1) {
                bold = ' gistic_selected_gene';
            }

            var highlight = '';
            if (search !== '') {
                var search_regex = RegExp('^' + search, 'i');
                if (search_regex.test(g)) {
                    highlight = ' gistic_filter_highlight';
                }
            }

            return "<span class='gistic_gene" + bold + highlight + "'" +
                "onClick=Gistic.UI.ioGeneSet(this);>" + g + "</span>";
        });

        if (genes.length > 5) {

            genes = genes.slice(0,5)    // visible genes
            .concat(" <a href='javascript:void(0)' style='color:blue' id='gistic_more'" +
                    "onclick=Gistic.UI.expandGisticGenes(this);>+" +
                    (genes.length - 5)  + " more</a>")
            .concat("<a href='javascript:void(0)' id='gistic_less' style='color:blue; display:none;' " +
                    "onclick=Gistic.UI.expandGisticGenes(this);> less</a>")

            .concat("<div id='gistic_hidden' style='display:none;'>") // hidden genes div
            .concat(genes.slice(5))
            .concat("</div>")
        }

        return genes.join(" ");
    }

    $.extend( $.fn.dataTableExt.oSort, {
        // bind the cytoband sorting function
        "cytoband-asc": sort_by_cytoband,

        "cytoband-desc": function(x,y) {
            return -1 * sort_by_cytoband(x,y);
        }
    } );

    $.fn.dataTableExt.afnFiltering.push( function(oSettings, aData, iDataIndex) {
        // filter by the beginning of the gene only.  Do not match the middle
        // of a gene

        var search = $('#gistic_table_filter input').val();

        if (search === '') {
            return true;
        }

        data = Gistic.dt.fnGetData(),                   // gistic objects
            no_data = data.length,
            nodes = Gistic.dt.fnGetNodes();             // DOM elementsk

        search = new RegExp('^' + search, 'i');

        var genes_l = aData[0],
        _len = genes_l.length;

        for (var i = 0 ; i < _len; i += 1) {
            if (search.test(genes_l[i])) {
                return true;
            }
        }

        return false;
    });

    var self = {
        drawTable : function(table_el, enteredGenes, options) {
            // draws a DataTable in the specific DOM element, table_el
            // with the specified DataTable options

            var aaData = gistics;

            var aoColumnDefs = [
                {"sTitle": "<div style='color:red'>Amp</div>" +
                    "<div style='color:blue'>Del</div>",
                    "sWidth": '5px',
                    "bSearchable": false,
                    "aTargets": [0],
                    "mDataProp": function(source, type, val) {
                        if (type === 'display') {
                            if (source.ampdel) {     // true means amplified
                                // mark amps/dels as reds and blues
                                return "<div class=\"gistic_amp\"></div>";
                            } else {
                                return "<div class=\"gistic_del\"></div>";
                            }
                        }
                        return source.ampdel;
                    }
            },

            {"sTitle": "Chr", "aTargets":[1], "bSearchable": false,
                "mDataProp": function(source, type, val) {
                    if (type === 'display') {
                        return source.chromosome;
                    }
                    return source.chromosome;
                }
            },

            {"sTitle": "Cytoband", "aTargets":[2], "sType": "cytoband", "bSearchable": false,
                "mDataProp": function(source, type, val) {
                    var cyto = source.cytoband;
                    if (type === 'display') {
                        return cyto;
                    }
                    else if (type === 'sort') {
                        // eg. 17p12.1
                        var regexp = "([0-9]{1,2})" +   // match the chr
                            "([pq])" +                  // match the arm
                            "([0-9]{1,2})" +            // match the first coordinate
                            "(?:\.?)" +                 // noncapturing, optional,
                            // match the decimal point
                            "([0-9]{0,2})";             // optional, match the 2nd coordinate
                        regexp = "^" + regexp + "$";
                        regexp = new RegExp(regexp);

                        return cyto.match(regexp);
                    }
                    return source;
                }
            },

            {"sTitle": "#", "aTargets":[3], "sType": "numeric", "sClass": 'gistic_center_col', "bSearchable": false,
                "mDataProp": function(source, type, val) {
                    return source.nonSangerGenes.length + source.sangerGenes.length;
                }
            },

            {"sTitle": "Genes",
                "aTargets":[4],
                "sType": "numeric",
                "sClass": "gistic_gene_cell",
                "mDataProp": function(source, type, val) {
                    var all_genes = source.sangerGenes.concat(source.nonSangerGenes);

                    if (type === 'display') {
                        return drawGenes(all_genes, enteredGenes);
                    }

                    else if (type === 'sort') {
                        return all_genes.length;
                    }

                    return all_genes;
                }
            },
            {"sTitle": "Q Value",
                "sType": "numeric",
                "sClass": "gistic_right_col",
                "bSearchable": false,
                "aTargets":[5],
                "mDataProp": function(source, type, val) {

                    var rounded = cbio.util.toPrecision(source.qval, 2, 0.1);

                    if (type === 'display') {
                        return rounded;
                    }
                    return rounded;
                }
            } ];

            options.aaSorting = [[ 5, "asc" ]];     // sort Q-Value column on load
            options.oLanguage = {'sSearch': 'Filter by Gene:'};
            options.aaData = aaData;
            options.aoColumnDefs = aoColumnDefs;

            options.fnDrawCallback = function() {
                var search = $('#gistic_table_filter input').val();

                var dt = $('#gistic_table').dataTable();
            };

            Gistic.dt = table_el.dataTable(options);

            // center cols
            $('.gistic_center_col').css('text-align', 'center');

            // right cols
            $('.gistic_right_col').css('text-align', 'right');

            // {{{todo: maybe we'll want this someday
            // bind double clicking
            //Gistic.dt.fnGetNodes().forEach(function(i) {
            //    $(i).find('.gistic_gene_cell').
            //        select(Gistic.UI.select_all_genes);
            //});
            //}}}

            // put in the help box
            $('#gistic_table_filter').parent().
                prepend('<span id="gistic_msg_box">' +
                        'Click on a gene to <span>select</span> it</span>');

            $('#gistic_close').show();
            return;
        },

        getGistics: function() {
            return gistics;
        }
    };

    return self;
};

Gistic.UI = ( function() {
    // dump of all sorts of UI functions
    // the closure is to keep the GISTIC variable private

    var GISTIC = {};

    return {
        open_dialog : function() {

            Gistic.table_el = $('#gistic_table');

            var options = { "sScrollY": "350px",
                "bPaginate": false,
                "bJQueryUI": true,
                "bDestroy": true};

            $('#gistic_msg_box').hide();
            $('#gistic_cancel').hide();

            $('#gistic_loading').show();
            $('#gistic_dialog').dialog('open');

            var genes = GeneSet($('#gene_list').val()).getAllGenes();

            var current_selection = $('#select_cancer_type').val();

            // if Gistic has never been run then Gistic.last_selection =
            // undefined, otherwise, check to prevent multiple AJAXs for the
            // same cancer study
            if (Gistic.last_selection !== current_selection) {

                // save this for later comparision
                Gistic.last_selection = current_selection;

                // hide the table while new data loads
                $('#gistic_table_wrapper').hide();
                $('#gistic_dialog_footer').hide();

                $.ajax({
                    url: 'Gistic.json',
                    data: {'selected_cancer_type': current_selection},
                    dataType: 'json',
                    success: function(data) {
                        GISTIC = Gistic(data);

                        GISTIC.drawTable(Gistic.table_el, genes, options);

                        // table is ready to be shown!
                        $('#gistic_loading').hide();
                        $('#gistic_table_wrapper').show();
                        $('#gistic_dialog_footer').show();
                    }
                });
            } else {
                $('#gistic_loading').hide();
                // want to redraw everytime you open in order to make the genes
                // bold 
                GISTIC.drawTable(Gistic.table_el, genes, options);
            }

            // update the selected_genes everytime you open the dialog
            Gistic.selected_genes = $.map($('.gistic_selected_gene'),
                function(val, i) {
                return $(val).html();
            });
        },

        expandGisticGenes : function(el) {
            // shows/hides additional genes in the genes column
            // currently initializing to hiding all non-Sanger genes

            el = $(el).parents()[0];

            // grab all the elements
            var more = $(el).children('#gistic_more');
            var less = $(el).children('#gistic_less');
            var hidden = $(el).children('#gistic_hidden');

            // and toggle them
            $(more).toggle();
            $(less).toggle();
            $(hidden).slideToggle('slow');
        },

        ioGeneSet : function(el) {
            $(el).toggleClass('gistic_selected_gene');
        },

        updateGenes: function() {
            // updates the genes in the Gene Set textarea
            //
            var geneSet = GeneSet(Gistic.gene_list_el.val());

            var currently_selected = $.map($('.gistic_selected_gene'),
                function(val, i) { return $(val).html(); });

            var remove_genes = $.grep(Gistic.selected_genes, function(i) {
                // genes that are not selected but are in the geneset
                return $.inArray(Gistic.selected_genes[i], currently_selected) === -1;
            });

            var new_genes = $.grep(currently_selected, function(i) {
                // genes that are selected and not in the gene set
                return $.inArray(currently_selected[i], geneSet.getAllGenes()) === -1;
            });

            $.each(remove_genes, function(i,val) {
                // remove remove_genes from geneset
                geneSet.filterOut(val);
            });

            // append new_genes
            var out = geneSet.toString() + '\n' + $.makeArray(new_genes).join(" ");
            out = $.trim(out);

            // push to gene set
            Gistic.gene_list_el.val(out);
        },
        // {{{ todo: maybe we'll want this someday
        select_all_genes: function(el) {
            var max = 50;       // max no of genes users are allowed to select
            var selection = $(this).find('.gistic_gene');

            if (selection.length > 50) {
                // show error message
                $('#gistic_msg_box').show();
                return;
            } else {
                $('#gistic_msg_box').hide();
            }

            if (selection.length > 5) {
                // expand the genes if there are genes to be expanded
                $(this).find('#gistic_more').click();
            }
            selection.toggleClass('gistic_selected_gene');
        }
        // }}}
    };
})();
