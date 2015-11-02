var QueryByGeneTextArea  = (function() {
    //
    var geneList = new Array();
    var successBannerId;
    var areaId;
    var updateGeneCallBack;

    function setFocusOutText(){
        var focusOutText="query genes - click to expand";
        if(geneList.length==1) focusOutText = geneList[0];
        else if(geneList.length>1) focusOutText = geneList[0] + " and "+(geneList.length-1)+" more";
        $(areaId).val(focusOutText);
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

    function showSuccessBanner(gene){
        if(successBannerId!=undefined) {
            $(successBannerId).text(gene+" added to your query");
            $(successBannerId).show().delay(3000).fadeOut(1000, "linear");
        }
    }

    function addGene (gene){
        if(geneList.indexOf(gene)==-1) {
            geneList.push(gene);
            setFocusOutText();
            showSuccessBanner(gene);

            if(updateGeneCallBack != undefined) updateGeneCallBack(geneList);
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
        //geneList = $.unique(removeEmptyElements($(areaId).val().split(/\W/))).reverse();
        geneList = $.unique(removeEmptyElements($(areaId).val().toUpperCase().split(/\W/))).reverse();
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
            if(updateGeneCallBack != undefined) updateGeneCallBack(geneList);
        });

        $(areaId).bind('input propertychange', validateGenes);
    }

    function init(areaIdP, successBannerIdP, updateGeneCallBackP){
        areaId = areaIdP;
        successBannerId = successBannerIdP;
        updateGeneCallBack = updateGeneCallBackP;
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

