
var BreadCrumbs = (function() {
    // clear all removes all the breadcrumbs from the container
    // it is called by the reset all button
    function clearAll(){
        removeChartCrumbs($("#breadcrumbs_container").children());
    }

    // delete crumbs by chartId removes the crumbs for a single chart
    // it is called when someone removes a graph
    function deleteCrumbsByChartId(chartId){
        removeChartCrumbs($("#breadcrumbs_container").find("."+chartId));
    }

    // update scatterplot breadcrumb
    // currently there is only one scatterplot with a hard-coded id
    // we're using this id as our link to the scatterplot
    // if at some point in the future multiple scatterplots may exist, this has to be changed
    function updateScatterPlotBreadCrumb(chartId, crumbTitle, crumbTipText){
        var crumbID = getCrumbId(chartId, crumbTitle);

        // find the number of selected cases
        // a case is selected if has "shiftClicked" or "both" as its clicked state
        var nrCases = $("#study-view-scatter-plot").find("[clicked='shiftClicked']").length + $("#study-view-scatter-plot").find("[clicked='both']").length;

        // there are two cases for this breadcrumb:
        // 1. the breadcrumb does not yet exist and the chart contains selected cases
        // create the crumb
        if ($("#"+crumbID).length == 0 && nrCases>0){
            addBreadCrumb(chartId, crumbTitle, crumbTipText, "scatterplot", "", removeScatterPlotFiltering);
        }
        // remove the crumb
        //
        else if($("#"+crumbID).length>0 && nrCases==0){
            removeChartCrumb(chartId, crumbTitle);
            // if any filters still exist, re-apply them
            applyExistingFilters();
        }
        // all other cases can in this case be ignored
    }

    function applyExistingFilters(){
        // find all the breadcrumbs and re-apply the filters
        $("#breadcrumbs_container").find(".breadcrumb_remove").each(function () {
            var chartId = $(this).attr("chartID");
            var chartFilter = $(this).attr("chartFilter");
            var chartType = $(this).attr("chartType");

            if(chartType=="bar"){
                var chart = StudyViewInitCharts.getChartsByID(chartId-1).getChart();
                chart.filter(chartFilter.split(","))
            }
            else if(chartType=="pie"){
                var chart = StudyViewInitCharts.getChartsByID(chartId-1).getChart();
                chart.filter(chartFilter);
            }
            else if(chartType=="table"){
                //?
            }
        });

        dc.redrawAll();

        // set the clearflag of the scatterplot to false to allow
        // StudyViewInitCharts' postFilterCallbackFunc to be executed
        StudyViewInitScatterPlot.setclearFlag(false);
    }

    // update piechart breadcrumb
    function updatePieChartBreadCrumb(chartId, chartFilter, chartAttribute, chartType){
        var crumbTipText = chartAttribute+": "+chartFilter;

        // if the crumbTitle is null we have to remove all the filters for this piechart
        if(chartFilter==null) removeChartCrumbs($("#breadcrumbs_container").find("."+chartId));
        else {
            // find the proper div
            var crumbID = getCrumbId(chartId, chartFilter);
            var breadcrumbDiv = $("#breadcrumbs_container").find("#" + crumbID);

            // the div doesn't exist, so the click on the chart implies we have to create the breadcrumb
            if (breadcrumbDiv.length == 0)
                addBreadCrumb(chartId, chartFilter, crumbTipText, chartType, chartFilter, removePieFiltering);
            // the div does exist, so the click on the chart implies we have to remove the breadcrumb
            else
                //$(breadcrumbDiv).remove();
                removeChartCrumb(chartId, chartFilter);
        }
    }

    // update barchart breadcrumb
    function updateBarChartBreadCrumb(chartId, chartFilter, chartAttribute, chartType){
    //function updateBarChartBreadCrumb(chartId, crumbTitle, crumbTipText, chartFilter, chartType){
        // find the proper div
        var crumbTipText = getBarChartTipText(chartFilter, chartAttribute);
        var crumbID = getCrumbId(chartId, chartAttribute);
        var breadcrumbDiv = $("#breadcrumbs_container").find("#"+crumbID);

        // the div doesn't exist and the user selected a valid range; add the breadcrumb
        if(breadcrumbDiv.length==0 && crumbTipText!==""){
            addBreadCrumb(chartId, chartAttribute, crumbTipText, chartType, chartFilter,removeBarFiltering);
        }
        // the div does exist and the user selected a valid range; update the breadcrumb
        else if(breadcrumbDiv.length>0 && crumbTipText!==""){
            changeBreadCrumb(chartId, chartAttribute, crumbTipText, chartFilter);
        }
        // remove the breadcrumb
        else{
            //$(breadcrumbDiv).remove();
            removeChartCrumb(chartId, chartAttribute);
        }
    }

    // get the tiptext for the barchart based on the filter and the attribute
    // if the filter is null we return the empty string, otherwise something like
    // age: 4.02 - 8.05
    function getBarChartTipText(chartFilter, chartAttribute){
        return chartFilter==null?"":chartAttribute+": "+roundToTwo(chartFilter[0])+" - "+roundToTwo(chartFilter[1]);
    }

    // round a number to two decimals
    function roundToTwo(num) {
        return +(Math.round(num + "e+2")  + "e-2");
    }


    //function updateBarChartBreadCrumb(chartId, chartFilter, chartAttribute, chartType){
    // update table breadcrumb
    //function updateTableBreadCrumb(chartId, crumbTitle, crumbTipText, tableRowId, chartFilter, shiftClicked){
    function updateTableBreadCrumb(chartId, chartFilter, chartType, tableRowId, shiftClicked, crumbTipText){
        // find the proper div
        //var crumbID = chartId+"_"+crumbTitle;
        //var breadcrumbDiv = $("#breadcrumbs_container").find("#"+crumbID);

        var crumbTitle = chartFilter;

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
                createTableCrumb(chartId, crumbTitle, crumbTipText, chartFilter, tableRowId);
            }
        }
        else{
            if (!shiftClicked) {
                if (!highlighted) {
                    // some breadcrumbs already exist; shift is not pressed and our new item was not yet highlighted
                    // this means that we have to remove all the existing breadcrumbs for the table and create a new
                    // one for the clicked item
                    removeChartCrumbs(tableBreadCrumbs);
                    createTableCrumb(chartId, crumbTitle, crumbTipText, chartFilter, tableRowId);
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
                    createTableCrumb(chartId, crumbTitle, crumbTipText, chartFilter, tableRowId);
                }
                else{
                    // some breadcrumbs already exist and the currently clicked item was not yet highlighted
                    // as shift is pressed, we just remove the clicked breadcrumb
                    removeChartCrumb(chartId, crumbTitle);
                }

            }
        }
    }


    function createTableCrumb(chartId, crumbTitle, crumbTipText, chartFilter, tableRowId){
        var crumbID = getCrumbId(chartId, crumbTitle);
        addBreadCrumb(chartId, crumbTitle, crumbTipText, "table", chartFilter, removeTableFiltering);

        //$("#"+crumbID).attr("class", chartId)
        $("#"+crumbID+"_img").attr("cellID", tableRowId);
    }


    // remove all provided chartCrumbs
    function removeChartCrumbs(chartCrumbs){
        if(chartCrumbs.length>0) {
            $(chartCrumbs).each(function () {
                $(this).remove();
            });
            //dc.redrawAll();
            // reset the css
            if($("#breadcrumbs_container").children().length==0) {
                removeChartCrumbResetCSS();
            }
        }
    }

    // remove a single breadcrumb
    function removeChartCrumb(chartId, crumbTitle){
        var crumbID = getCrumbId(chartId, crumbTitle);
        var breadcrumbDiv = $("#breadcrumbs_container").find("#"+crumbID);
        removeChartCrumbs(breadcrumbDiv);
    }

    // stop showing the container and restore the top-wrapper height
    function removeChartCrumbResetCSS(){
        $("#breadcrumbs_container").css('display','none');
        $("#study-view-top-wrapper").css("height", 50)
    }


    // get an identifier based on the chartId and the crumbTitle
    // remove any non-alphanumeric characters
    function getCrumbId(chartId, crumbTitle){
        return ("crumb_"+chartId+"_"+crumbTitle).replace(/[^a-z0-9]/gi, '_');
    }

    // change the existing breadcrumb
    function changeBreadCrumb(chartId, crumbTitle, crumbTipText, chartFilter){
        var crumbID = getCrumbId(chartId, crumbTitle);
        // update the qtip to the new values
        $("#"+crumbID+"_item").qtip('option', 'content.text', crumbTipText);
        $("#"+crumbID+"_img").attr("chartFilter", chartFilter);
    }



    //function addBreadCrumb(chartId, crumbTitle, crumbTipText, chartType, removeFiltering){
    function addBreadCrumb(chartId, crumbTitle, crumbTipText, chartType, chartFilter, removeFiltering) {
        var crumbID = getCrumbId(chartId, crumbTitle);

        var breadCrumbDiv = StudyViewBoilerplate.breadCrumbDiv;
        $("#breadcrumbs_container").append(breadCrumbDiv);
        breadCrumbDiv = $("#breadcrumbs_container").children().last();

        // settings for a single breadcrumb
        // set the class to the chartId to be able to easily find all the breadcrumbs for the chart
        $(breadCrumbDiv).attr("id", crumbID);
        $(breadCrumbDiv).addClass(chartId.toString());

        // settings for the breadcrumb item
        var breadcrumbItem = $(breadCrumbDiv).find(".breadcrumb_item");
        $(breadcrumbItem).attr("id", crumbID + "_item");
        $(breadcrumbItem).text(crumbTitle);
        $(breadcrumbItem).qtip({
            content: {text: crumbTipText},
            position: {my: 'left bottom', at: 'top right', viewport: $(window)},
            style: {classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow'},
            show: {event: "mouseover"},
            hide: {fixed: true, delay: 100, event: "mouseout"}
        });

        // settings for the breadcrumb image
        var breadcrumbImg = $(breadCrumbDiv).find(".breadcrumb_remove");
        $(breadcrumbImg).attr("id", crumbID + "_img");
        $(breadcrumbImg).attr("chartID", chartId);
        $(breadcrumbImg).attr("crumbTitle", crumbTitle);
        $(breadcrumbImg).attr("chartType", chartType);
        $(breadcrumbImg).attr("chartFilter", chartFilter);
        $("#" + crumbID).on("click", "#" + crumbID + "_img", removeFiltering);

        // if this is the first breadcrumb, show the container and slightly increase the space for visualisation
        if ($("#breadcrumbs_container").children().length == 1) {
            $("#breadcrumbs_container").css('display', 'block');
            $("#study-view-top-wrapper").css("height", 70)
        }

    }

    function removeScatterPlotFiltering(){
        //StudyViewInitCharts.clearScatterPlot();
        StudyViewInitScatterPlot.clearScatterPlot();
    }

    function removePieFiltering(){
        var chartId = $(this).attr("chartID");
        var chartFilter = $(this).attr("chartFilter");
        var chart = StudyViewInitCharts.getChartsByID(chartId-1).getChart();
        chart.filter(chartFilter);
        dc.redrawAll();
    }

    function removeBarFiltering(){
        var chartId = $(this).attr("chartID");
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
        updateScatterPlotBreadCrumb: updateScatterPlotBreadCrumb,
        updatePieChartBreadCrumb: updatePieChartBreadCrumb,
        updateBarChartBreadCrumb: updateBarChartBreadCrumb,
        updateTableBreadCrumb: updateTableBreadCrumb,
        clearAll: clearAll,
        deleteCrumbsByChartId:deleteCrumbsByChartId
    };

})();
