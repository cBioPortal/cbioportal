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


function GeneValidator(geneAreaId, emptyAreaMessage, updateGeneCallback){
    var self = this;
    var nrOfNotifications=0;

    this.init = function(){
        console.log("init called for "+geneAreaId);
        $(geneAreaId).bind('input propertychange', validateGenes);
    }

    var validateGenes = _.debounce(function(e){
        console.log("validating "+geneAreaId);

        if(emptyAreaMessage===$(geneAreaId).val()) return;

        clearAllBanners();
        cleanArea();

        var genes = [];
        var genesStr = $(geneAreaId).val();

        $.post('CheckGeneSymbol.json', { 'genes': genesStr })
            .done(function(symbolResults) {
                var allValid = true;

                // If the number of genes is more than 100, show an error
                if(symbolResults.length > 100) {
                    addNotification("<b>You have entered more than 100 genes.</b><br>Please enter fewer genes for better performance", "danger");
                    return;
                }

                for(var j=0; j < symbolResults.length; j++) {
                    var valid = handleSymbol(symbolResults[j])
                    if(!valid) allValid = false;
                }

                //if(allValid) updateGeneCallback();
                updateGeneCallback();
            })
            .fail(function(xhr,  textStatus, errorThrown){

            });
    }, 3000);


    this.noActiveNotifications = function(){
        return nrOfNotifications===0;
    }

    this.validatorCallBack = function(){
        console.log("callback!")
        nrOfNotifications--;
        updateGeneCallback();
    }


    function addNotification(message, message_type){
        notificationSettings.message_type = message_type;
        new Notification().createNotification(message, notificationSettings);
        nrOfNotifications = $(".alert").length;
    }


    function clearAllBanners(){
        $(".alert").css("animation-iteration-count", "0");
        $(".alert").qtip("destroy");
        $(".alert").find("button").click();
        nrOfNotifications=0;
    }

    function removeEmptyElements(array){
        return array.filter(function(el){ return el !== "" });
    }

    function cleanArea(){
        $(geneAreaId).val($.unique(removeEmptyElements($(geneAreaId).val().toUpperCase().split(/[^a-zA-Z0-9-]/))).reverse().join(" "));
    }

    function handleSymbol(aResult){
        var multiple = false;
        var foundSynonym = false;
        var valid = false;

        // only 1 symbol
        if( aResult.symbols.length == 1 ) {
            multiple = false;
            if(aResult.symbols[0].toUpperCase() != aResult.name.toUpperCase()) {
                foundSynonym = true;
            } else {
                return true;
            }
        } else if( aResult.symbols.length > 1 ) {
            multiple = true;
        }


        if(multiple) {
            handleMultiple(aResult);

        } else if( foundSynonym ) {
            handleSynonyms(aResult);

        } else {
            handleSymbolNotFound(aResult);
        }

        return valid;

    }

    function handleMultiple(aResult){
        var gene = aResult.name;
        var symbols = aResult.symbols;

        var tipText = "Ambiguous gene symbol. Click on one of the alternatives to replace it.";
        var notificationHTML="<span>Ambiguous gene symbol - "+gene+" ";

        var nameSelect = $("<select id="+gene+">").addClass("geneSelectBox").attr("name", gene);
        $("<option>").attr("value", "")
            .html("select a symbol")
            .appendTo(nameSelect);
        for(var k=0; k < symbols.length; k++) {
            var aSymbol = symbols[k];
            //var anOption = $("<option>").attr("value", aSymbol).html(aSymbol);
            var anOption = $("<option class='close' data-notify='dismiss'>").attr("value", aSymbol).html(aSymbol);
            anOption.appendTo(nameSelect);
        }

        notificationHTML+=nameSelect.prop('outerHTML')+"</span>";

        addNotification(notificationHTML, "warning");

        $("#"+gene).change(function() {
            var trueSymbol = $(this).attr('value');
            var geneName = $(this).attr("name");
            $(geneAreaId).val($(geneAreaId).val().replace(geneName, trueSymbol));

            $(this).qtip("destroy");
            self.validatorCallBack();

            this.children[this.selectedIndex].click();
        });

        addQtip(gene, tipText);
    }


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

        $("#"+gene).click(function(){
            $(geneAreaId).val($(geneAreaId).val().replace($(this).attr("id"), $(this).attr("symbol")));
            //setTimeout(validateGenes, 500);
            $(this).qtip("destroy");
            self.validatorCallBack();
        });

        addQtip(gene, tipText);

    }

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

        $("#"+gene).click(function(){
            var geneName = $(this).attr("id");
            var regexp = new RegExp("\\b"+geneName+"\\b","g")

            $(geneAreaId).val($(geneAreaId).val().replace(regexp, "").trim());

            $(this).qtip("destroy");
            self.validatorCallBack();
        });

        addQtip(gene, tipText);
    }

    function addQtip(id, tipText){
        $("#"+id).qtip({
            content: {text: tipText},
            position: {my: 'top center', at: 'bottom center', viewport: $(window)},
            style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow'},
            show: {event: "mouseover"},
            hide: {fixed: true, delay: 100, event: "mouseout"}
        });
    }


    var notificationSettings = {
        message_type: "warning",
        custom_class: "geneValidationNotification",
        allow_dismiss: true,
        placement_align: "right",
        spacing: 10,
        delay: 0,
        timer: 0
    };


    this.init();

    //return {
    //    validateGenes: validateGenes
    //};







}