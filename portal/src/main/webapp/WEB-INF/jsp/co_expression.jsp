<script type="text/javascript" src="js/src/coexp-view/coExpression.js"></script>
<script type="text/javascript" src="js/src/coexp-view/simplePlots.js"></script>
<style>
    #coexp .dataTables_info {
        display : inline;
        width: 200px;
        padding-left: 5px;
        float:left;
    }
    #coexp .div.datatable-paging {
        width: auto;
        float: right;
    }
    #coexp .datatable-filter {
        width: 200px;
        float: right;
    }
    #coexp .datatable-length {
        float: left;
    }
    #coexp .coexp-tabs{
        /*height: 685px; */
    }
    #coexp .coexp-tabs-ref{
        font-size: 11px !important;
    }
    #coexp .coexp-table-filter-custom {
        width: 250px;
        margin: 0px;
        display: inline;
    }
</style>
<div class="section" id="coexp">
    <div id="coexp-tabs" class="coexp-tabs">
        <ul id='coexp-tabs-list'></ul>
        <div id='coexp-tabs-content'></div>
    </div>
</div>

<script>
    $(document).ready( function() {
        $(window).resize();
        CoExpTable.initTabs();
        CoExpTable.initView();
    });
</script>
