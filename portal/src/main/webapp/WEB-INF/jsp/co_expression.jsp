<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 -
 - This library is distributed in the hope that it will be useful, but WITHOUT
 - ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 - FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 - is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 - obligations to provide maintenance, support, updates, enhancements or
 - modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 - liable to any party for direct, indirect, special, incidental or
 - consequential damages, including lost profits, arising out of the use of this
 - software and its documentation, even if Memorial Sloan-Kettering Cancer
 - Center has been advised of the possibility of such damage.
 --%>

<%--
 - This file is part of cBioPortal.
 -
 - cBioPortal is free software: you can redistribute it and/or modify
 - it under the terms of the GNU Affero General Public License as
 - published by the Free Software Foundation, either version 3 of the
 - License.
 -
 - This program is distributed in the hope that it will be useful,
 - but WITHOUT ANY WARRANTY; without even the implied warranty of
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 - GNU Affero General Public License for more details.
 -
 - You should have received a copy of the GNU Affero General Public License
 - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

<script type="text/javascript" src="js/src/co-exp/coExpression.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/co-exp/components/PlotsHeader.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/co-exp/components/ScatterPlots.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/co-exp/data/CoexpPlotsProxy.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/co-exp/view/CoexpPlotsView.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/co-exp/view/components/CoexpPlots.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/co-exp/view/components/PlotsBoilerplate.js?<%=GlobalProperties.getAppVersion()%>"></script>

<style>
    #coexp .coexp-table-filter-custom {
        width: 400px;
        float: left;
    }
    #coexp .datatables_filter {
        width: 300px;
        float: left;
        margin-left: 0px;
        text-align: left;
        font-size: 11px;
        padding-left: 6px;
    }
    #coexp .dataTables_paginate {
        float: right;
        padding: 3px;
    }
    #coexp .paging_full_numbers .ui-button {
        border: 1px solid #aaa;
        -webkit-border-radius: 5px;
        -moz-border-radius: 5px;
        padding: 2px 5px;
        margin: 0 3px;
        cursor: hand;
        text-align: left;
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
    .ui-state-disabled {
        display: none;
    }  

</style>

<div class="section" id="coexp">
    <p>
        <div id='coexp-profile-selector-dropdown' style="margin-top:10px;"></div>
        This table lists the genes with the highest expression correlation with the query genes. Click on a row to see the corresponding correlation plot. 
        <img src='images/help.png' id='coexp-help' alt='help'>
    </p>
    <div id="coexp-tabs" class="coexp-tabs">
        <ul id='coexp-tabs-list'></ul>
        <div id='coexp-tabs-content'></div>
    </div>
</div>

<script>
    $(document).ready( function() {
        var coexp_tab_init = false;
        if ($("#coexp").is(":visible")) {
            CoExpView.init();
            coexp_tab_init = true;
        } else {
            $(window).trigger("resize");
        }
        $("#tabs").bind("tabsactivate", function(event, ui) {
            if (ui.newTab.text().trim().toLowerCase() === "co-expression") {
                if (coexp_tab_init === false) {
                    CoExpView.init();
                    coexp_tab_init = true;
                    $(window).trigger("resize");
                } else {
                    $(window).trigger("resize");
                }
            }
        });
    });
    $("#coexp-help").qtip({
        content: { text:'Pearson correlations are computed first. For genes with a correlation greater than 0.3 or less than -0.3, the Spearman correlations are also computed. By default, only gene pairs with values > 0.3 or < -0.3 in both measures are shown.'},
        style: { classes: 'ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-lightyellow' },
        show: {event: "mouseover"},
        hide: {fixed:true, delay: 100, event: "mouseout"},
        position: {my:'left bottom',at:'top right',viewport: $(window)}
    })
</script>
