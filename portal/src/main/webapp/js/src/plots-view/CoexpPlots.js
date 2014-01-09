var CoexpPlots = (function() {

    function init(divName, geneX, geneY)  {
        getAlterationData(divName, geneX, geneY);
    }

    function getAlterationData(divName, geneX, geneY) {
        var paramsGetAlterationData = {
            cancer_study_id: window.PortalGlobals.getCancerStudyId(),
            gene_list: geneX + " " + geneY,
            case_set_id: window.PortalGlobals.getCaseSetId(),
            case_ids_key: window.PortalGlobals.getCaseIdsKey()
        };
        $.post("getAlterationData.json", paramsGetAlterationData, getAlterationDataCallBack(divName, geneX, geneY), "json");
    }

    function getAlterationDataCallBack(divName, geneX, geneY) {
        return function(result) {
            CoexpPlotsProxy.init(result, geneX, geneY);
            CoexpPlotsView.init(divName, geneX, geneY);
            initCanvas(divName);
            appendHeader(gene1, gene2);
            initScales();
            initAxis();
            drawAxis(gene1, gene2);
            drawPlots();
            addQtips();
        }
    }

    return {
        init: init
    }

}());