var QueryByGeneTextArea  = (function() {
    //
    var geneList = new Array();
    var hideWhenEmpty=true;
    var areaId;

    function hideShowArea(){
        if(hideWhenEmpty){
            if(isEmpty()) $(areaId).parent().hide();
            else $(areaId).parent().show();
        }
    }

    function updateGeneAreaContent(){
        $(areaId).val(geneList.join(" "));
    }

    function isEmpty(){
        return geneList.length==0;
    }

    function getGenes(){
        return geneList.join(" ");
    }

    function addGene (gene){
        if(geneList.indexOf(gene)==-1) {
            geneList.push(gene);
            updateGeneAreaContent();
            hideShowArea();
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
        geneList = $.unique(removeEmptyElements($(areaId).val().split(/\W/)));
        updateGeneAreaContent();
    }

    function performGeneValidation(){
        console.log("no validation yet");
    }

    function initEvents(){
        $(areaId).focusin(function () {
            //$(this).animate({ height: "5em" }, 500);
            $(this).switchClass("expandFocusOut", "expandFocusIn", 500);
        });

        $(areaId).focusout(function () {
            //$(this).animate({ height: "2em" }, 500);
            $(this).switchClass("expandFocusIn", "expandFocusOut", 500);
            updateGeneList();
            hideShowArea();
        });

        $(areaId).bind('input propertychange', validateGenes);
    }

    function init(areaIdP){
        areaId = areaIdP;
    //this.init = function(areaId){
        initEvents();

    }

    return{
        init: init,
        addGene: addGene,
        getGenes: getGenes,
        isEmpty: isEmpty
    }

})();

