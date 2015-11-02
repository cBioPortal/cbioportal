var QueryByGeneTextArea  = (function() {
    //
    var geneList = new Array();
    var successBannerId;
    var areaId;

    function setFocusOutText(){
        var focusOutText="query genes - click to expand";
        if(geneList.length==0) hideSuccessBanner();
        if(geneList.length==1) focusOutText = geneList[0];
        else if(geneList.length>1) focusOutText = geneList[0] + " and "+(geneList.length-1)+" more";
        $(areaId).val(focusOutText);
    }

    function setFocusInText(){
        $(areaId).val(geneList.join(" "));
    }

    //function hideShowArea(){
    //    if(hideWhenEmpty){
    //        if(isEmpty()) $(areaId).parent().hide();
    //        else $(areaId).parent().show();
    //    }
    //}

    function getNrGenes(){
        return geneList.length;
    }

    function isEmpty(){
        return geneList.length==0;
    }

    function getGenes(){
        return geneList.join(" ");
    }

    function showSuccessBanner(gene){
        if(successBannerId!=undefined) {
            $(successBannerId).text(gene+" added to your query");
            $(successBannerId).show();
        }
    }

    function hideSuccessBanner(){
        $(successBannerId).fadeOut(2000, "linear");
    }


    function addGene (gene){
        if(geneList.indexOf(gene)==-1) {
            geneList.push(gene);
            setFocusOutText();
            showSuccessBanner(gene);
            //hideShowArea();
        }
    }

    var validateGenes = _.debounce(function(e) {
        performGeneValidation();
    }, 3000); // Maximum run of once per 3 seconds

    function removeEmptyElements(array){
        return array.filter(function(el){ return el !== "" });
    }

    function updateGeneList(){
        console.log("Updating Gene List");
        performGeneValidation();
        // split the values that are in the textArea and remove the empty elements
        // TNF; IRF5 now becomes ["TNF", "IRF5"]
        // Problematic if e.g. "-" is allowed in a gene name
        geneList = $.unique(removeEmptyElements($(areaId).val().split(/\W/))).reverse();
    }

    function performGeneValidation(){
        console.log("no validation yet");
    }

    function initEvents(){
        $(areaId).focusin(function () {
            $(this).switchClass("expandFocusOut", "expandFocusIn", 500);
            setFocusInText();
        });

        $(areaId).focusout(function () {
            $(this).switchClass("expandFocusIn", "expandFocusOut", 500);
            updateGeneList();
            setFocusOutText();
            //hideShowArea();
        });

        $(areaId).bind('input propertychange', validateGenes);
    }

    function init(areaIdP, successBannerIdP){
        areaId = areaIdP;
        successBannerId = successBannerIdP;
        setFocusOutText();
        initEvents();
    }

    return{
        init: init,
        addGene: addGene,
        getGenes: getGenes,
        getNrGenes: getNrGenes,
        isEmpty: isEmpty
    }

})();

