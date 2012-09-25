var selected_cancer_type = 'tcga_gbm';

var SELECTED_CANCER_TYPE_OLD = '';
var SELECTED_CANCER_TYPE_NEW = '';

$(document).ready(function() {
    $('#select_cancer_type').change(function() {
        SELECTED_CANCER_TYPE_NEW = $('#select_cancer_type').val();
        console.log(SELECTED_CANCER_TYPE_NEW);
    });
});

//if (window.json.cancer_studies[cancerStudyId].has_gistic_data) {


var Gistic = function(gistics) {

    // store the DataTable object once it has been created
    Gistic.dt = '';

    Gistic.gene_list_el = $('#gene_list');
    Gistic.dialog_el = $('#gistic_dialog');

    // initialize Gistic's internal tracking of the Gene Set
    Gistic.geneSet = GeneSet(Gistic.gene_list_el.val());

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

            Gistic.UI.selected_genes.set(genes);

            var aaData = gistics;

            var aoColumnDefs = [
                {"sTitle": "AD",    // todo : ampdel tiptip?
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
            {"sTitle": "Genes", "aTargets":[3], "sType": "numeric",
                "mDataProp": function(source, type, val) {
                    var all_genes = source.sangerGenes.concat(source.nonSangerGenes);

                    if (type === 'display') {

                        var all_genes_str = all_genes.map(function(g) {
                            // bind ioGeneSet to each gene
                            // highlight ones that are already in the gene list

                            var highlight = '';

                            if (genes.indexOf(g) !== -1) {
                                highlight = ' highlight';
                            }

                            return "<span class='gistic_gene" + highlight + "'" +
                                "onClick=Gistic.UI.ioGeneSet(this);>" + g + "</span>";
                        });

                        if (all_genes_str.length > 5) {

                            all_genes_str = all_genes_str.slice(0,5)    // visible genes
                            .concat(" <a href='javascript:void(0)' id='gistic_more'" +
                                    "onclick=Gistic.UI.expandGisticGenes(this);>+" +
                                    (all_genes_str.length - 5)  + " more</a>")
                            .concat("<a href='javascript:void(0)' id='gistic_less' style='display:none;' " +
                                    "onclick=Gistic.UI.expandGisticGenes(this);> less</a>")
                            .concat("<div id='gistic_hidden' style='display:none;'>")
                            .concat(all_genes_str.slice(5))             // hidden genes
                            .concat("</div>")
                        }

                        all_genes_str = all_genes_str.join(" ");
                        return all_genes_str;
                    }
                    else if (type === 'sort') {
                        return all_genes.length;
                    }
                    return all_genes;
                }
            },
            {"sTitle": "Q Value", "aTargets":[4],
                "mDataProp": function(source, type, val) {
                    var rounded = parseFloat(source.qval) .toExponential(1);   // round Q-Values
                    if (type === 'display') {
                        return rounded;
                    }
                    return rounded;
                }
            }
            ];


            options.aaSorting = [[ 6, "asc" ]];     // sort Q-Value column on load
            options.oLanguage = {'sSearch': 'Filter by Gene:'};
            options.aaData = aaData;
            options.aoColumnDefs = aoColumnDefs;

            Gistic.dt = table_el.dataTable(options);

            // paint regions red and blue
            $('.gistic_amp').parent().css('background-color', 'red');
            $('.gistic_del').parent().css('background-color', 'blue');
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

    var GISTIC = {};

    return {
        open_dialog : function() {

            Gistic.table_el = $('#gistic_table');

            var options = { "sScrollY": "50%", "bPaginate": false, "bDestroy": true};

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
            $(hidden).toggle('slow');
        },

        ioGeneSet : function(el) {
            $(el).toggleClass('highlight');

            var gene = $(el).html();
            gene.trim();        // just to be safe

            Gistic.UI.selected_genes.update(gene);
        },

        updateGenes: function() {
            var geneSet = GeneSet(Gistic.gene_list_el.val());
            var gene_list_str = geneSet.getRawGeneString();

            var newline = '';
            if ( (gene_list_str.length !== 0) &&            // not the empty string
                (gene_list_str.search(/\n$/) === -1) ) {    // there isn't a new linechar
                newline = '\n';
            }

            var genes_toPush = Gistic.UI.selected_genes.getGenes();
            var gene_list = geneSet.getAllGenes();

            // filter out genes that are already in the gene list
            genes_toPush = genes_toPush.filter(function(i) {
                return gene_list.indexOf(i) === -1;
            });

            Gistic.gene_list_el.val(gene_list_str + newline
                                    + genes_toPush.join(" "));
        }
    };
})();

Gistic.UI.selected_genes = function() {
    var genes = [];

    return {
        getGenes: function() {
            return genes;
        },
        update: function(gene) {
            // remove if clicked twice.
            var i = genes.indexOf(gene);
            if (i === -1) {
                genes.push(gene);
            } else {
                genes.splice(i,1);
            }
        },
        set: function(genes_l) {
            // sets genes to genes_l
            // without asking any questions
            genes = genes_l;
        },
        reset: function() {
            genes = [];
        }
    }
}();

