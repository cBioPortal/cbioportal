
var BreadCrumbs = (function() {
/*
    // update breadcrumbs
    function updateBreadCrumb(chartId, crumbTitle, crumbTipText, chartType){

        // find the proper div
        var crumbID = chartId+"_"+crumbTitle;
        var breadcrumbDiv = $("#breadcrumbs_container").find("#"+crumbID);

        // if the chart type is a pie, either add the breadcrumb or remove the breadcrumb
        if(chartType==='pie'){
            // the div doesn't exist, so the click on the chart implies we have to create the breadcrumb
            if(breadcrumbDiv.length==0)
                addBreadCrumb(chartId, crumbTitle, crumbTipText, chartType, removePieFiltering);
            // the div does exist, so the click on the chart implies we have to remove the breadcrumb
            else
                $(breadcrumbDiv).remove();
        }

       // if the chart type is a bar, add, update or remove the breadcrumb
        else if(chartType==='bar'){
            // the div doesn't exist and the user selected a valid range; add the breadcrumb
            if(breadcrumbDiv.length==0 && crumbTipText!==""){
                addBreadCrumb(chartId, crumbTitle, crumbTipText, chartType, removeBarFiltering);
            }
            // the div does exist and the user selected a valid range; update the breadcrumb
            else if(breadcrumbDiv.length>0 && crumbTipText!==""){
                changeBreadCrumb(chartId, crumbTitle, crumbTipText);
            }
            // remove the breadcrumb
            else{
                $(breadcrumbDiv).remove();
            }
        }
    }*/

    // update piechart breadcrumb
    function updatePieChartBreadCrumb(chartId, crumbTitle, crumbTipText){
        // if the crumbTitle is null we have to remove all the filters for this piechart
        if(crumbTitle==null) removeChartCrumbs($("#breadcrumbs_container").find("."+chartId));
        else {
            // find the proper div
            var crumbID = getCrumbId(chartId, crumbTitle);
            var breadcrumbDiv = $("#breadcrumbs_container").find("#" + crumbID);

            // the div doesn't exist, so the click on the chart implies we have to create the breadcrumb
            if (breadcrumbDiv.length == 0)
                addBreadCrumb(chartId, crumbTitle, crumbTipText, removePieFiltering);
            // the div does exist, so the click on the chart implies we have to remove the breadcrumb
            else
                $(breadcrumbDiv).remove();
        }
    }

    // update barchart breadcrumb
    function updateBarChartBreadCrumb(chartId, crumbTitle, crumbTipText){
        // find the proper div
        var crumbID = getCrumbId(chartId, crumbTitle);
        var breadcrumbDiv = $("#breadcrumbs_container").find("#"+crumbID);

        // the div doesn't exist and the user selected a valid range; add the breadcrumb
        if(breadcrumbDiv.length==0 && crumbTipText!==""){
            addBreadCrumb(chartId, crumbTitle, crumbTipText, removeBarFiltering);
        }
        // the div does exist and the user selected a valid range; update the breadcrumb
        else if(breadcrumbDiv.length>0 && crumbTipText!==""){
            changeBreadCrumb(chartId, crumbTitle, crumbTipText);
        }
        // remove the breadcrumb
        else{
            $(breadcrumbDiv).remove();
        }
    }

    // update table breadcrumb
    function updateTableBreadCrumb(chartId, crumbTitle, crumbTipText, tableRowId, shiftClicked){
        // find the proper div
        //var crumbID = chartId+"_"+crumbTitle;
        //var breadcrumbDiv = $("#breadcrumbs_container").find("#"+crumbID);

        // was the table row highlighted before the click occurred?
        var highlighted = $("#"+tableRowId).parent().hasClass('highlightRow');

        // find all breadcrumbs for the table
        var tableBreadCrumbs = $("#breadcrumbs_container").find("."+chartId);

        if(tableBreadCrumbs.length==0) {
            if (highlighted) {
                // remove the crumb
                removeChartCrumb(chartId, crumbTitle);
            }
            else{
                // create crumb
                createTableCrumb(chartId, crumbTitle, crumbTipText, tableRowId);
            }
        }
        else{
            if (!shiftClicked) {
                if (!highlighted) {
                    // some breadcrumbs already exist; shift is not pressed and our new item was not yet highlighted
                    // this means that we have to remove all the existing breadcrumbs for the table and create a new
                    // one for the clicked item
                    removeChartCrumbs(tableBreadCrumbs);
                    createTableCrumb(chartId, crumbTitle, crumbTipText, tableRowId);
                }
                else{
                    // some breadcrumbs exist and the currently clicked item was highlighted
                    // as shift is nog pressed, we need to remove all breadcrumbs for the table
                    removeChartCrumbs(tableBreadCrumbs);
                }
            }
            else {
                if (!highlighted) {
                    // some breadcrumbs already exist and the currently clicked item was not yet highlighted
                    // as shift is pressed, we just add another breadcrumb
                    createTableCrumb(chartId, crumbTitle, crumbTipText, tableRowId);
                }
                else{
                    // some breadcrumbs already exist and the currently clicked item was not yet highlighted
                    // as shift is pressed, we just remove the clicked breadcrumb
                    removeChartCrumb(chartId, crumbTitle);
                }

            }
        }
    }


    function createTableCrumb(chartId, crumbTitle, crumbTipText, tableRowId){
        var crumbID = getCrumbId(chartId, crumbTitle);
        addBreadCrumb(chartId, crumbTitle, crumbTipText, removeTableFiltering);
        //$("#"+crumbID).attr("class", chartId)
        $("#"+crumbID+"_img").attr("cellID", tableRowId);
    }


    function removeChartCrumbs(chartCrumbs){
        $(chartCrumbs).each(function() {
            $(this).remove();
        });
        //dc.redrawAll();
    }

    function removeChartCrumb(chartId, crumbTitle){
        var crumbID = getCrumbId(chartId, crumbTitle);
        var breadcrumbDiv = $("#breadcrumbs_container").find("#"+crumbID);
        $(breadcrumbDiv).remove();
        //dc.redrawAll();
    }


    function getCrumbId(chartId, crumbTitle){
        return "crumb_"+chartId+"_"+crumbTitle;
    }

    // change the existing breadcrumb
    function changeBreadCrumb(chartId, crumbTitle, crumbTipText){
        var crumbID = getCrumbId(chartId, crumbTitle);
        // update the qtip to the new values
        $("#"+crumbID+"_item").qtip('option', 'content.text', crumbTipText);
    }



    //function addBreadCrumb(chartId, crumbTitle, crumbTipText, chartType, removeFiltering){
    function addBreadCrumb(chartId, crumbTitle, crumbTipText, removeFiltering){
        var crumbID = getCrumbId(chartId, crumbTitle);

        var breadCrumbDiv = StudyViewBoilerplate.breadCrumbDiv;
        $("#breadcrumbs_container").append(breadCrumbDiv);
        breadCrumbDiv=$("#breadcrumbs_container").children().last();

        // settings for a single breadcrumb
        // set the class to the chartId to be able to easily find all the breadcrumbs for the chart
        $(breadCrumbDiv).attr("id", crumbID);
        $(breadCrumbDiv).attr("class", chartId);

        // settings for the breadcrumb item
        var breadcrumbItem = $(breadCrumbDiv).find(".breadcrumb_item");
        $(breadcrumbItem).attr("id", crumbID+"_item");
        $(breadcrumbItem).text(crumbTitle);
        $(breadcrumbItem).qtip({
            content: {text: crumbTipText},
            position: {my:'left bottom', at:'top right', viewport: $(window)},
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
            show: {event: "mouseover"},
            hide: {fixed: true, delay: 100, event: "mouseout"}
        });

        // settings for the breadcrumb image
        var breadcrumbImg = $(breadCrumbDiv).find(".breadcrumb_remove");
        $(breadcrumbImg).attr("id", crumbID+"_img");
        $(breadcrumbImg).attr("chartID", chartId);
        $(breadcrumbImg).attr("crumbTitle", crumbTitle);
        //$(breadcrumbImg).attr("chartType", chartType);
        //$(breadcrumbImg).attr("chartFilter", chartFilter);
        $("#"+crumbID).on("click", "#"+crumbID+"_img", removeFiltering);

    }

    function removePieFiltering(){
        var chartId = $(this).attr("chartID");
        var crumbTitle = $(this).attr("crumbTitle");
        var chart = StudyViewInitCharts.getChartsByID(chartId-1).getChart();
        chart.filter(crumbTitle);
        dc.redrawAll();
    }

    function removeBarFiltering(){
        var chartId = $(this).attr("chartID");
        var crumbTitle = $(this).attr("crumbTitle");
        var chart = StudyViewInitCharts.getChartsByID(chartId-1).getChart();
        chart.filter(null);
        dc.redrawAll();
    }

    function removeTableFiltering(){
        var cellID = $(this).attr("cellID");

        StudyViewWindowEvents.setShiftDown();

        $("#"+cellID).click();
        //dc.redrawAll();
        StudyViewWindowEvents.setShiftUp();
    }

    return {
        //updateBreadCrumb: updateBreadCrumb,
        updatePieChartBreadCrumb: updatePieChartBreadCrumb,
        updateBarChartBreadCrumb: updateBarChartBreadCrumb,
        updateTableBreadCrumb: updateTableBreadCrumb
    };

})();
