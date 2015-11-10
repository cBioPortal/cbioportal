var QueryByGeneTextArea  = (function() {
    //
    var geneList = new Array();
    var areaId, submitButtonId;
    var updateGeneCallBack;
    var geneValidator;
    var emptyAreaText = "query genes - click to expand";

    // when the textarea does not have focus, the text shown in the (smaller) textarea
    // is gene1, gene2 and x more
    function createFocusOutText(){
        var focusOutText = geneList[0];
        var stringLength = focusOutText.length;

        // build the string to be shown
        for(var i=1; i<geneList.length; i++){
            stringLength+=geneList[i].length+2;
            // if the string length is bigger than 15 characters,
            // add the "and x more"
            if(stringLength>15) {
                focusOutText+= " and "+(geneList.length-i)+" more";
                break;
            }
            focusOutText+=", "+geneList[i];
        }
        return focusOutText;
    }

    // set the textarea text when focus is lost (and no notifications are open)
    function setFocusOutText(){
        var focusOutText=emptyAreaText;
        // if there are genes build the focusText
        if(geneList.length>0) focusOutText = createFocusOutText();
        setTextColour();
        $(areaId).val(focusOutText);
    }

    // if the geneList is empty, we use a gray colour, otherwise black
    function setTextColour(){
        if(geneList.length>0) $(areaId).css("color", "black");
        else $(areaId).css("color", "darkgrey");
    }

    // when the textarea has focus, the contents is the geneList's contents separated by spaces
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

    // addRemoveGene is used when someone clicks on a gene in a table (StudyViewInitTables)
    function addRemoveGene (gene){
        // if the gene is not yet in the list, add it and create a notification
        if(geneList.indexOf(gene)==-1) {
            geneList.push(gene);
            new Notification().createNotification(gene+" added to your query");
        }
        // if the gene is in the list, remove it and create a notification
        else{
            var index = geneList.indexOf(gene);
            geneList.splice(index, 1);
            new Notification().createNotification(gene+" removed from your query");
        }
        // if there are active notifications, the textarea is still expanded and the contents
        // should reflect this
        if(geneValidator.noActiveNotifications()) setFocusOutText();
        else setFocusInText();

        // update the highlighting in the tables
        if(updateGeneCallBack != undefined) updateGeneCallBack(geneList);
    }

    // used by the focusOut event and by the GeneValidator as callback
    function setFocusOut(){
        // update the geneList with a cleaned area string
        geneList = geneValidator.cleanAreaString();

        // if there are no active notifications and the textarea does not have focus
        if(geneValidator.noActiveNotifications() && !$(areaId).is(":focus")){
            // switch from focusIn to focusOut and set the focus out text
            $(areaId).switchClass("expandFocusIn", "expandFocusOut", 500);
            setFocusOutText();
            enableSubmitButton();
        }
        // update the gene tables for highlighting
        if(updateGeneCallBack != undefined) updateGeneCallBack(geneList);
    }

    function enableSubmitButton(){
        $(submitButtonId).removeAttr('disabled').removeClass("disabled");
    }

    function disableSubmitButton(){
        $(submitButtonId).attr('disabled','disabled').addClass("disabled");
    }

    // initialise events
    function initEvents(){
        // add the focusin event
        $(areaId).focusin(function () {
            $(this).switchClass("expandFocusOut", "expandFocusIn", 500);
            disableSubmitButton();
            setFocusInText();
        });

        // add the focusout event
        $(areaId).focusout(function () {
            setFocusOut();
        });

        // create the gene validator
        geneValidator = new GeneValidator(areaId, emptyAreaText, setFocusOut);
    }


    function init(areaIdP, submitButtonIdP, updateGeneCallBackP){
        areaId = areaIdP;
        submitButtonId = submitButtonIdP;
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

