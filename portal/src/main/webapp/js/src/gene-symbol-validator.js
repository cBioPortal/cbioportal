/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

var GeneSymbolValidator = (function($) {
    var initialize = function() {
        $("#gene_list").data("timerid", null).bind('keyup', function(e) {
            clearTimeout(jQuery(this).data('timerid'));
            jQuery(this).data('timerid', setTimeout(validateGenes, 500));
        });

        if($("#gene_list").val().length > 0) {
            validateGenes();
        }
    };

    var validateGenes = function() {
        $("#gene_list").val($("#gene_list").val().replace("  ", " ").toUpperCase());
        $("#genestatus").html("<img src='images/ajax-loader2.gif'> <small>Validating gene symbols...</small>");
        $("#main_submit").attr("disabled", "disabled");

        var genes = [];
        var genesStr = $("#gene_list").val();
        $.post(
            'CheckGeneSymbol.json',
            { 'genes': genesStr },
            function(symbolResults) {
                $("#genestatus").html("");
                var stateList = $("<ul>").addClass("ui-widget icon-collection validation-list");
                var allValid = true;

                // If the number of genes is more than 100, show an error
                if(symbolResults.length > 100) {
                    var invalidState = $("<li>").addClass("ui-state-default ui-corner-all");
                    var invalidSpan = $("<span>")
                        .addClass("ui-icon ui-icon-notice");
                    var invalidText = $("<span>").addClass("text");
                    invalidText.html("<b>You have entered more than 100 genes.</b>");

                    invalidState.attr("title", "Please enter fewer genes for better performance.").tipTip();

                    invalidSpan.appendTo(invalidState);
                    invalidText.insertAfter(invalidSpan);
                    invalidState.addClass("ui-state-active");
                    invalidState.prependTo(stateList);

                    $("<br>").appendTo(stateList);
                    $("<br>").appendTo(stateList);

                    stateList.appendTo("#genestatus");

                    return;
                }


                for(var j=0; j < symbolResults.length; j++) {
                    var aResult = symbolResults[j];
                    var multiple = false;
                    var foundSynonym = false;
                    var valid = true;
                    var symbols = [];
                    var gene = aResult.name;

                    if( aResult.symbols.length == 1 ) {
                        multiple = false;
                        if(aResult.symbols[0].toUpperCase() != aResult.name.toUpperCase()) {
                            foundSynonym = true;
                        } else {
                            continue;
                        }
                    } else if( aResult.symbols.length > 1 ) {
                        multiple = true;
                        symbols = aResult.symbols;
                    } else {
                        allValid = false;
                    }

                    if(foundSynonym || multiple)
                        allValid = false;

                    if(multiple) {
                        var state = $("<li>").addClass("ui-state-default ui-corner-all ui-validator");
                        var stateSpan = $("<span>").addClass("ui-icon ui-icon-help");
                        var stateText = $("<span>").addClass("text");

                        stateText.html(gene + ": ");
                        var nameSelect = $("<select>").addClass("geneSelectBox").attr("name", gene);
                        $("<option>").attr("value", "")
                            .html("select a symbol")
                            .appendTo(nameSelect);
                        for(var k=0; k < symbols.length; k++) {
                            var aSymbol = symbols[k];
                            var anOption = $("<option>").attr("value", aSymbol).html(aSymbol);
                            anOption.appendTo(nameSelect);
                        }
                        nameSelect.appendTo(stateText);
                        nameSelect.change(function() {
                            var trueSymbol = $(this).attr('value');
                            var geneName = $(this).attr("name");
                            $("#gene_list").val($("#gene_list").val().replace(geneName, trueSymbol));
                            setTimeout(validateGenes, 500);
                        });

                        stateSpan.appendTo(state);
                        stateText.insertAfter(stateSpan);
                        state.attr("title",
                            "Ambiguous gene symbol. Click on one of the alternatives to replace it."
                        );
                        state.attr("name", gene);
                        state.appendTo(stateList);
                    } else if( foundSynonym ) {
                        var state = $("<li>").addClass("ui-state-default ui-corner-all ui-validator");
                        var trueSymbol = aResult.symbols[0];

                        state.click(function(){
                            $(this).toggleClass('ui-state-active');
                            var names = $(this).attr("name").split(":");
                            var geneName = names[0];
                            var symbol = names[1];
                            $("#gene_list").val($("#gene_list").val().replace(geneName, symbol));
                            setTimeout(validateGenes, 500);
                        });

                        var stateSpan = $("<span>").addClass("ui-icon ui-icon-help");
                        var stateText = $("<span>").addClass("text");
                        stateText.html("<b>" + gene + "</b>: " + trueSymbol);
                        stateSpan.appendTo(state);
                        stateText.insertAfter(stateSpan);
                        state.attr("title",
                            "'" + gene + "' is a synonym for '" + trueSymbol + "'. "
                                + "Click here to replace it with the official symbol."
                        );
                        state.attr("name", gene + ":" + trueSymbol);
                        state.appendTo(stateList);
                    } else {
                        var state = $("<li>").addClass("ui-state-default ui-corner-all ui-validator");
                        state.click(function(){
                            $(this).toggleClass('ui-state-active');
                            geneName = $(this).attr("name");
                            $("#gene_list").val($("#gene_list").val().replace(" " + geneName, ""));
                            $("#gene_list").val($("#gene_list").val().replace(geneName + " ", ""));
                            $("#gene_list").val($("#gene_list").val().replace(geneName + "\n", ""));
                            $("#gene_list").val($("#gene_list").val().replace(geneName, ""));
                            setTimeout(validateGenes, 500);
                        });
                        var stateSpan = $("<span>").addClass("ui-icon ui-icon-circle-close");
                        var stateText = $("<span>").addClass("text");
                        stateText.html(gene);
                        stateSpan.appendTo(state);
                        stateText.insertAfter(stateSpan);
                        state.attr("title",
                            "Could not find gene symbol. Click to remove it from the gene list."
                        );
                        state.attr("name", gene);
                        state.appendTo(stateList);
                    }
                }

                stateList.appendTo("#genestatus");

                $('.ui-state-default').hover(
                    function(){ $(this).addClass('ui-state-hover'); },
                    function(){ $(this).removeClass('ui-state-hover'); }
                );

                $('.ui-validator').tipTip();

                if( allValid ) {
                    $("#main_submit").removeAttr("disabled").removeAttr("title")

                    if( symbolResults.length > 0
                        && !(symbolResults[0].name == "" && symbolResults[0].symbols.length == 0) ) {

                        var validState = $("<li>").addClass("ui-state-default ui-corner-all");
                        var validSpan = $("<span>")
                            .addClass("ui-icon ui-icon-circle-check");
                        var validText = $("<span>").addClass("text");
                        validText.html("All gene symbols are valid.");

                        validSpan.appendTo(validState);
                        validText.insertAfter(validSpan);
                        validState.addClass("ui-state-active");
                        validState.appendTo(stateList);
                        validState.attr("title", "You can now submit the list").tipTip();
                        $("<br>").appendTo(stateList);
                        $("<br>").appendTo(stateList);
                    }

                } else {
                    var invalidState = $("<li>").addClass("ui-state-default ui-corner-all");
                    var invalidSpan = $("<span>")
                        .addClass("ui-icon ui-icon-notice");
                    var invalidText = $("<span>").addClass("text");
                    invalidText.html("<b>Invalid gene symbols.</b>");

                    invalidState.attr("title", "Please edit the gene symbols").tipTip();

                    invalidSpan.appendTo(invalidState);
                    invalidText.insertAfter(invalidSpan);
                    invalidState.addClass("ui-state-active");
                    invalidState.prependTo(stateList);

                    $("<br>").appendTo(stateList);
                    $("<br>").appendTo(stateList);
                }
            },
            'json'
        );
    };

    return {
        initialize: initialize,
        validateGenes: validateGenes
    }

})(jQuery);

