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

// based on gene-symbol-validator.js
//function GeneValidator(geneAreaId, emptyAreaMessage, updateGeneCallback){
function GeneValidator(geneAreaId, geneModel){
    var self = this;
    var nrOfNotifications=0;

    var showNotification=true;

    this.init = function(){
        console.log(new Date() + " init called for "+geneAreaId);
        // create a debounced validator
        var debouncedValidation = _.debounce(this.validateGenes, 1000);
        $(geneAreaId).bind('input propertychange', debouncedValidation);
    }

    this.validateGenes = function(callback, show){
        console.log(new Date() + " validating genes in "+geneAreaId);

        // store whether to show notifications
        showNotification=(show===undefined)?true:show;

        // clear all existing notifications
        if(showNotification) clearAllNotifications();

        // clean the textArea string, removing doubles and non-word characters (except -)
        var genesStr = geneModel.getCleanGeneString();
            //self.cleanAreaString(geneModel.get("geneString")).join(" ")
        //$(geneAreaId).val(genesStr);

        var genes = [];
        var allValid = true;

        $.post('CheckGeneSymbol.json', { 'genes': genesStr })
            .done(function(symbolResults) {
                // If the number of genes is more than 100, show an error
                if(symbolResults.length > 100) {
                    addNotification("<b>You have entered more than 100 genes.</b><br>Please enter fewer genes for better performance", "danger");
                    allValid=false;
                }

                // handle each symbol found
                for(var j=0; j < symbolResults.length; j++) {
                    var valid = handleSymbol(symbolResults[j])
                    if(!valid) {
                        allValid = false;
                    }
                }
            })
            .fail(function(xhr,  textStatus, errorThrown){
                addNotification("There was a problem: "+errorThrown, "danger");
                allValid=false;
            })
            .always(function(){
                // if not all valid, focus on the gene array for focusin trigger
                if(!allValid) $(geneAreaId).focus();
                // in case a submit was pressed, use the callback
                if($.isFunction(callback)) callback(allValid);
            });
    }

    // return whether there are any active notifications
    this.noActiveNotifications = function(){
        return nrOfNotifications===0;
    }

    this.replaceAreaValue = function(geneName, newValue){
        var regexp = new RegExp("\\b"+geneName+"\\b","g");
        var genesStr = geneModel.getCleanGeneString();
        geneModel.set("geneString", genesStr.replace(regexp, newValue).trim());
    }

    // create a notification of a certain type
    function addNotification(message, message_type){
        //if(showNotification) {
            notificationSettings.message_type = message_type;
            new Notification().createNotification(message, notificationSettings);
        //}
        nrOfNotifications = $(".alert").length;
    }


    function clearAllNotifications(){
        // select the notifications of interest
        // kill their animations to prevent them from blocking space, destroy any qtips remaining and call click to
        // make the notifications disappear
        $(".geneValidationNotification").css("animation-iteration-count", "0");
        $(".geneValidationNotification").qtip("destroy");
        $(".geneValidationNotification").find("button").click();
        nrOfNotifications=0;
    }

    // handle one symbol
    function handleSymbol(aResult){
        var valid = false;

        // 1 symbol
        if(aResult.symbols.length == 1) {
            if(aResult.symbols[0].toUpperCase() != aResult.name.toUpperCase() && showNotification)
                handleSynonyms(aResult);
            else
                valid=true;
        }
        else if(aResult.symbols.length > 1 && showNotification)
            handleMultiple(aResult)
        else if(showNotification)
            handleSymbolNotFound(aResult);

        return valid;
    }

    // case where we're dealing with an ambiguous gene symbol
    function handleMultiple(aResult){
        var gene = aResult.name;
        var symbols = aResult.symbols;

        var tipText = "Ambiguous gene symbol. Click on one of the alternatives to replace it.";
        var notificationHTML="<span>Ambiguous gene symbol - "+gene+" ";

        // create the dropdown
        var nameSelect = $("<select id="+gene+">").addClass("geneSelectBox").attr("name", gene);
        $("<option>").attr("value", "").html("select a symbol").appendTo(nameSelect);
        for(var k=0; k < symbols.length; k++) {
            var aSymbol = symbols[k];
            // add class and data-notify to allow us to dismiss the notification
            var anOption = $("<option class='close' data-notify='dismiss'>").attr("value", aSymbol).html(aSymbol);
            anOption.appendTo(nameSelect);
        }

        notificationHTML+=nameSelect.prop('outerHTML')+"</span>";
        addNotification(notificationHTML, "warning");

        // when the dropdown is changed
        $("#"+gene).change(function() {
            nrOfNotifications--;
            // replace the value in the text area
            self.replaceAreaValue($(this).attr("name"), $(this).attr("value"));

            // destroy the qtip if it's still there
            $(this).qtip("destroy");

            // emulate a click on the selected child to dismiss the notification
            this.children[this.selectedIndex].click();
        });

        addQtip(gene, tipText);
    }


    // case when the symbol has synonyms
    function handleSynonyms(aResult){
        var gene = aResult.name;
        var trueSymbol = aResult.symbols[0];
        var tipText = "'" + gene + "' is a synonym for '" + trueSymbol + "'. "
            + "Click here to replace it with the official symbol.";

        var notificationHTML=$("<span>Symbol synonym found - "+gene + ":" + trueSymbol+"</span>");
        notificationHTML.attr({
                'id': gene,
                'symbol': trueSymbol,
                'class':'close',
                'data-notify':'dismiss'
            });

        addNotification(notificationHTML.prop('outerHTML'), "warning");

        // add click event to our span
        // due to the class and data-notify, the click also removes the notification
        $("#"+gene).click(function(){
            nrOfNotifications--;
            // replace the value in the text area
            self.replaceAreaValue($(this).attr("id"), $(this).attr("symbol"));

            // destroy the qtip if it's still here
            $(this).qtip("destroy");
        });

        addQtip(gene, tipText);
    }

    // case when the symbol was not found
    function handleSymbolNotFound(aResult){
        var gene = aResult.name;
        var tipText = "Could not find gene symbol "+gene+". Click to remove it from the gene list.";

        var notificationHTML=$("<span>Symbol not found - "+gene+"</span>");
        notificationHTML.attr({
            'id': gene,
            'class':'close',
            'data-notify':'dismiss'
        });

        addNotification(notificationHTML.prop('outerHTML'), "warning");

        // add click event to our span
        // due to the class and data-notify, the click also removes the notification
        $("#"+gene).click(function(){
            nrOfNotifications--;
            // replace the value in the text area
            self.replaceAreaValue($(this).attr("id"), "");

            // destroy the qtip if it's still here
            $(this).qtip("destroy");
        });

        addQtip(gene, tipText);
    }

    // add a qtip to some identifier
    function addQtip(id, tipText){
        $("#"+id).qtip({
            content: {text: tipText},
            position: {my: 'top center', at: 'bottom center', viewport: $(window)},
            style: {classes: 'qtip-light qtip-rounded qtip-shadow'},
            show: {event: "mouseover"},
            hide: {fixed: true, delay: 100, event: "mouseout"}
        });
    }


    // notification settings
    var notificationSettings = {
        message_type: "warning",
        custom_class: "geneValidationNotification",
        allow_dismiss: true,
        spacing: 10,
        delay: 0,
        timer: 0
    };

    // when new object is created, called init();
    this.init();
}