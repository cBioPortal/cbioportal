var QueryByGeneTextArea  = (function() {
    //
    var geneList = new Array();
    var areaId;
    var updateGeneCallBack;
    var geneValidator;
    var emptyAreaText = "query genes - click to expand";

    function createFocusOutText(){
        var focusOutText = geneList[0];
        var stringLength = focusOutText.length;

        for(var i=1; i<geneList.length; i++){
            stringLength+=geneList[i].length+2;
            if(stringLength>15) {
                focusOutText+= " and "+(geneList.length-i)+" more"; //12
                break;
            }
            focusOutText+=", "+geneList[i];
        }
        return focusOutText;
    }

    function setFocusOutText(){
        var focusOutText=emptyAreaText;
        if(geneList.length>0) focusOutText = createFocusOutText();
        setTextColour();
        $(areaId).val(focusOutText);
    }

    function setTextColour(){
        if(geneList.length>0) $(areaId).css("color", "black");
        else $(areaId).css("color", "darkgrey");
    }

    function setFocusInText(){
        $(areaId).val(geneList.join(" "));
    }

    function getNrGenes(){
        return geneList.length;
    }

    function isEmpty(){
        return geneList.length==0;
    }

    function getGenes(){
        return geneList.join(" ");
    }

    function addRemoveGene (gene){
        if(geneList.indexOf(gene)==-1) {
            geneList.push(gene);
            new Notification().createNotification(gene+" added to your query");
        }
        else{
            var index = geneList.indexOf(gene);
            geneList.splice(index, 1);
            new Notification().createNotification(gene+" removed from your query");
        }
        setFocusOutText();
        if(updateGeneCallBack != undefined) updateGeneCallBack(geneList);
    }

    //var validateGenes = _.debounce(function(e) {
    //    performGeneValidation();
    //}, 3000); // Maximum run of once per 3 seconds



    function removeEmptyElements(array){
        return array.filter(function(el){ return el !== "" });
    }


    function updateGeneList(){
        console.log("Updating Gene List");
        //performGeneValidation();
        // split the values that are in the textArea and remove the empty elements
        // TNF; IRF5 now becomes ["TNF", "IRF5"]
        // Problematic if e.g. "-" is allowed in a gene name

        //geneList = $.unique(removeEmptyElements($(areaId).val().toUpperCase().split(/\W/))).reverse();

        //geneList = $(areaId).val().toUpperCase().split(/\W/);

        setFocusOut();

        //if(updateGeneCallBack != undefined) updateGeneCallBack(geneList);
    }

    function setFocusOut(){
        //geneList = $.unique(removeEmptyElements($(areaId).val().toUpperCase().split(/\W/))).reverse();
        geneList = $.unique(removeEmptyElements($(areaId).val().toUpperCase().split(/[^a-zA-Z0-9-]/))).reverse();
        if(geneValidator.noActiveNotifications() && !$(areaId).is(":focus")){
            $(areaId).switchClass("expandFocusIn", "expandFocusOut", 500);
            setFocusOutText();
        }
        if(updateGeneCallBack != undefined) updateGeneCallBack(geneList);
    }

    // focusOut
    // if banner exists --> stay big
    // else go small
    //  if small --> change text
    // always update genelists
    // always update tables

    // geneValidator
    // updateCallback

    //function performGeneValidation(){
    //    console.log("no validation yet");
    //    GeneValidator.validateGenes();
    //}

    function initEvents(){
        $(areaId).focusin(function () {
            $(this).switchClass("expandFocusOut", "expandFocusIn", 500);
            setFocusInText();
        });

        // maybe not required; add to the updateList Callback?
        $(areaId).focusout(function () {
            //geneList = $(areaId).val().toUpperCase().split(/\W/);
            setFocusOut();
            //if(updateGeneCallBack != undefined) updateGeneCallBack(geneList);

            // if no banner do this.
            //$(this).switchClass("expandFocusIn", "expandFocusOut", 500);
            //updateGeneList();
            //setFocusOutText();
            //if(updateGeneCallBack != undefined) updateGeneCallBack(geneList);
        });

        //$(areaId).bind('input propertychange', validateGenes);

        geneValidator = new GeneValidator(areaId, emptyAreaText, updateGeneList);
        //geneValidator.init();
        //geneValidator.validateGenes();
    }

    function init(areaIdP, updateGeneCallBackP){
        areaId = areaIdP;
        updateGeneCallBack = updateGeneCallBackP;
        setFocusOutText();
        initEvents();
    }

    return{
        init: init,
        addRemoveGene: addRemoveGene,
        getGenes: getGenes,
        getNrGenes: getNrGenes,
        isEmpty: isEmpty
    }

})();

