<script type="text/javascript" src="js/src/plots-view/PlotsBoilerplate.js"></script>
<script type="text/javascript" src="js/src/plots-view/data/CoexpPlotsProxy.js"></script>
<script type="text/javascript" src="js/src/plots-view/view/CoexpPlotsView.js"></script>
<script type="text/javascript" src="js/src/plots-view/CoexpPlots.js"></script>
<script type="text/javascript" src="js/src/coexp-view/coExpression.js"></script>
<script type="text/javascript" src="js/src/plots-view/component/ScatterPlots.js"></script>
<script type="text/javascript" src="js/src/plots-view/component/PlotsHeader.js"></script>

<style>
    #coexp .coexp-table-filter-custom {
        width: 200px;
        float: left;
    }
    #coexp .datatable-filter {
        width: 200px;
        float: right;
    }

    #coexp .dataTables_info {
        float: left;
        width: auto;
    }
    #coexp .coexp-tabs-ref {
        font-size: 11px !important;
    }
    #coexp .coexp-table {
        width: 100%;
    }
    #coexp .coexp-plots {
        float: left;
    }
    #coexp p {
        font-size: 12px;
        display: block;
        text-align: left;
        font-family: Verdana,Arial,sans-serif;
        margin-bottom: 12px;
    }
</style>

<div class="section" id="coexp">
    <p>
        The co-expression table below lists the top <strong>250</strong><img class='profile_help' src='images/help.png' title='If found more than 250 highly related gene, we would only display the the top-ranked 250 genes.'> genes that are highly co-expressed in selected mRNA expression profile,<img class='profile_help' src='images/help.png' title='We prefer RNA seq profile, if none for certain query, then we would use other mRNA profiles.'> 
        among the applied cancer study and case set. <br>
        Scores calculated by <strong>Pearson's product-moment correlation</strong> (Threshold: <strong>+/-0.3</strong>).
        <img class='profile_help' src='images/help.png'
             title='A measure of the degree of linear dependence between two variables, giving a value between +1 and -1 inclusive,
                        where 1 is total positive correlation, 0 is no correlation, and -1 is total negative correlation.
                        (Scores below -0.3 or above 0.3 are not listed).'>
        Click on any single row for related plots view.
    </p>
    <div id="coexp-tabs" class="coexp-tabs">
        <ul id='coexp-tabs-list'></ul>
        <div id='coexp-tabs-content'>
        </div>
    </div>
</div>

<script>
    $(document).ready( function() {
        var coexp_tab_init = false;
        $("#tabs").bind("tabsactivate", function(event, ui) {
            if (ui.newTab.text().trim().toLowerCase() === "co-expression") {
                if (coexp_tab_init === false) {
                    CoExpTable.initTabs();
                    CoExpTable.initView();
                    coexp_tab_init = true;
                } else {
                    $(window).trigger("resize");
                }
            }
        });
    });
</script>
