var selected_cancer_type = 'tcga_gbm';

var SELECTED_CANCER_TYPE_OLD = '';
var SELECTED_CANCER_TYPE_NEW = '';

$(document).ready(function() {
    $('#select_cancer_type').change(function() {
        SELECTED_CANCER_TYPE_NEW = $('#select_cancer_type').val();

        if (window.json.cancer_studies[SELECTED_CANCER_TYPE_NEW].has_gistic_data) {
            $('#toggle_gistic_dialog_button').show();
        } else {
            $('#toggle_gistic_dialog_button').hide();
        }
    });
});

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

    $.extend( $.fn.dataTableExt.oSort, {
        // bind the cytoband sorting function
        "cytoband-asc": sort_by_cytoband,

        "cytoband-desc": function(x,y) {
            return -1 * sort_by_cytoband(x,y);
        }
    } );

    var self = {
        getDt: function() {return dt;},

        drawTable : function(table_el, genes, options) {
            // draws a DataTable in the specific DOM element, table_el
            // with the specified DataTable options

            var aaData = gistics;

            var aoColumnDefs = [
                // todo : ampdel tiptip?
                {"sTitle": "<div id='gistic_AD'><span style='color:red'>A</span>" +
                    "<span style='color:blue'>D</span><img style='width: 32%;' src='images/help.png'></div>",
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
            {"sTitle": "Chr", "aTargets":[1],
                "mDataProp": function(source, type, val) {
                    if (type === 'display') {
                        return source.chromosome;
                    }
                    return source.chromosome;
                }
            },
            {"sTitle": "Cytoband", "aTargets":[2], "sType": "cytoband",
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
                            "([0-9]{0,2})";            // optional, match the 2nd coordinate
                        regexp = "^" + regexp + "$";
                        regexp = new RegExp(regexp);

                        return cyto.match(regexp);
                    }
                    return source;
                }
            },
            {"sTitle": "Genes<img style='width: 4%;' src='images/help.png'>",
                "aTargets":[3], "sType": "numeric", "sClass": "gistic_gene_cell",
                "mDataProp": function(source, type, val) {
                    var all_genes = source.sangerGenes.concat(source.nonSangerGenes);

                    if (type === 'display') {

                        var all_genes = all_genes.map(function(g) {
                            // bind ioGeneSet to each gene
                            // highlight ones that are already in the gene list

                            var highlight = '';

                            if (genes.indexOf(g) !== -1) {
                                highlight = ' gistic_selected_gene';
                            }

                            return "<span class='gistic_gene" + highlight + "'" +
                                "onClick=Gistic.UI.ioGeneSet(this);>" + g + "</span>";
                        });

                        if (all_genes.length > 5) {

                            all_genes = all_genes.slice(0,5)    // visible genes
                            .concat(" <a href='javascript:void(0)' style='color:blue' id='gistic_more'" +
                                    "onclick=Gistic.UI.expandGisticGenes(this);>+" +
                                    (all_genes.length - 5)  + " more</a>")
                            .concat("<a href='javascript:void(0)' id='gistic_less' style='color:blue; display:none;' " +
                                    "onclick=Gistic.UI.expandGisticGenes(this);> less</a>")

                            .concat("<div id='gistic_hidden' style='display:none;'>") // hidden genes div
                            .concat(all_genes.slice(5))
                            .concat("</div>")
                        }

                        return all_genes.join(" ");
                    }
                    else if (type === 'sort') {
                        return all_genes.length;
                    }
                    return all_genes;
                }
            },
            {"sTitle": "Q Value", "aTargets":[4], "sType": "numeric",
                "mDataProp": function(source, type, val) {
                    var rounded = parseFloat(source.qval) .toExponential(1);   // round Q-Values
                    if (type === 'display') {
                        return rounded;
                    }
                    return rounded;
                }
            }
            ];

            options.aaSorting = [[ 4, "asc" ]];     // sort Q-Value column on load
            options.oLanguage = {'sSearch': 'Filter by Gene:'};
            options.aaData = aaData;
            options.aoColumnDefs = aoColumnDefs;

            Gistic.dt = table_el.dataTable(options);

            // everytime you draw
            // update the selected_genes
            Gistic.selected_genes = $('.gistic_selected_gene').
                map(function(i, val) {
                return $(val).html();
            });

            // paint regions red and blue
            $('.gistic_amp').parent().css('background-color', 'red');
            $('.gistic_del').parent().css('background-color', 'blue');

            // add qtip to amp/del column
            $('#gistic_AD').qtip({
                content: "Red means the region is amplified. Blue means the region is deleted",
                position: {
                    my: 'top left',
                    at: 'bottom left',
                },
                show: 'mouseover',
                hide: 'mouseout'
            });

            // add qtip to genes column
            $($('.gistic_gene_cell')[0]).qtip({
                content: "Click a gene to select it, double click to select all genes in a region",
                position: {
                    my: 'top left',
                    at: 'bottom left',
                },
                show: 'mouseover',
                hide: 'mouseout'
            });

            // bind double clicking
            Gistic.dt.fnGetNodes().forEach(function(i) {
                $(i).find('.gistic_gene_cell').dblclick(Gistic.UI.select_all_genes);
                //$(i).hover(Gistic.UI.show_message("Click to highlight, double click to highlight all"));
            });

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
    // the closure is to keep the private GISTIC variable

    var GISTIC = {};

    return {
        open_dialog : function() {

            Gistic.table_el = $('#gistic_table');

            var options = { "sScrollY": "350px", "bPaginate": false, "bDestroy": true};

            $('#gistic_loading').show();
            $('#gistic_dialog').dialog('open');

            var genes = GeneSet($('#gene_list').val()).getAllGenes();

            // hide the Gistic button when there is no gistic data
            // if ajax hasn't already been done...then do an ajax call
            if (SELECTED_CANCER_TYPE_OLD !== SELECTED_CANCER_TYPE_NEW) {

                SELECTED_CANCER_TYPE_OLD = SELECTED_CANCER_TYPE_NEW;

                $.ajax({
                    url: 'Gistic.json',
                    data: {'selected_cancer_type': selected_cancer_type},
                    dataType: 'json',
                    success: function(data) {
                        $('#gistic_loading').hide();

                        GISTIC = Gistic(data);

                        GISTIC.drawTable(Gistic.table_el, genes, options);
                    }
                });
            } else {
                $('#gistic_loading').hide();
                GISTIC.drawTable(Gistic.table_el, genes, options);
            }

            // redraw table
            //Gistic.dt.fnDraw();
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
            var geneSet = GeneSet(Gistic.gene_list_el.val());

            var raw_str = geneSet.getRawGeneString();
            var newline = '';
            if ( (raw_str.length !== 0) &&            // not the empty string
                (raw_str.search(/\n$/) === -1) ) {    // there isn't a new linechar
                newline = '\n';
            }

            var currently_selected = $('.gistic_selected_gene').
                map(function(i, val) { return $(val).html(); });

            var remove_genes = Gistic.selected_genes.filter(function(i) {
                // genes that are not selected and are in the geneset
                return $.inArray(Gistic.selected_genes[i], currently_selected) === -1;
            });

            var new_genes = currently_selected.filter(function(i) {
                // genes that are selected and not in the gene set
                return $.inArray(currently_selected[i], geneSet.getAllGenes()) === -1;
            });

            $.each(remove_genes, function(i,val) {
                // remove remove_genes from geneset
                geneSet.filterOut(val);
            });

            // append new_genes
            var out = geneSet.toString() + newline + $.makeArray(new_genes).join(" ");
            out = out.trim();

            // push to gene set
            Gistic.gene_list_el.val(out);
        },

        show_message: function(msg) {
            var msg_el = $('#gistic_message_box');
            msg_el.html('');
            msg_el.html(msg);
            msg_el.slideToggle('fast');
            msg_el.fadeOut(4500);
        },

        select_all_genes: function(el) {
            var max = 50;       // max no of genes users are allowed to select
            var selection = $(this).find('.gistic_gene');

            if (selection.length > 50) {
                // show error message
                //Gistic.UI.show_message('Cannot Select More Than ' + max + ' Genes');
                return;
            }
            $(this).find('.gistic_gene').toggleClass('gistic_selected_gene');
        }
    };
})();
