<script type="text/javascript" src="js/src/coexp-view/coExpression.js"></script>
<script type="text/javascript" src="js/src/coexp-view/simplePlots.js"></script>
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
        float:left;
    }
    #coexp .coexp-tabs-ref{
        font-size: 11px !important;
    }
    #coexp p{
        font-size: 11px;
    }

</style>
<div class="section" id="coexp">
    The Gene Coexpression table lists the top 250 genes that are correlated in mRNA expression.
    Calculated with Pearson product-moment correlation method. (Threshold: +/-0.3)
    <img class='profile_help' src='images/help.png'
         title='A measure of the degree of linear dependence between two variables, giving a value between +1 and -1 inclusive,
                    where 1 is total positive correlation, 0 is no correlation, and -1 is total negative correlation. '>
    <br>Click on specific row to explore the plots of samples distribution.
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
