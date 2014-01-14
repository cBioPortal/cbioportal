var CoexpPlotsView = (function() {

    var options = {
        style: "",
        canvas: "",
        elem: "",
        names: {
            div: "",
            header: "",
            body: ""
        },
        text: {
            xTitle: "",
            yTitle: "",
            title: "",
            fileName: "",
        }
    };

    function settings(_divName, _geneX, _geneY, _dataAttr) {
        //css style
        options.style = jQuery.extend(true, {}, PlotsBoilerplate.style);
        //positions
        options.canvas = jQuery.extend(true, {}, PlotsBoilerplate.canvas);
        //svg elements
        options.elem = jQuery.extend(true, {}, PlotsBoilerplate.elem);
        //div ids
        options.names = jQuery.extend(true, {}, PlotsBoilerplate.names);
        options.names.div = _divName;
        options.names.header = _divName + options.names.header;
        options.names.body = _divName + options.names.body;   //the actual svg plots
        //construct axis titles
        options.text.xTitle = _geneX + ", " + _dataAttr.profile_name;
        options.text.yTitle = _geneY + ", " + _dataAttr.profile_name;
        options.text.title = "Co-expression in mRNA Expression: " + _geneX + " vs. " + _geneY + "  ";
        options.text.fileName = "co_expression_result-" + _geneX + "-" + _geneY;

    }

    function layout() {
        $("#" + options.names.div).append(
            "<div id='" + options.names.header + 
            "' style='padding-left: " + options.canvas.xLeft + "px; padding-top: 20px;'>" + 
            "</div>");
        $("#" + options.names.div).append("<div id='" + options.names.body + "'></div>");
    }

    function show(_dataArr, _dataAttr) {
        PlotsHeader.init(options.names.header, options.text.title, options.text.fileName, options.names.body);
        ScatterPlots.init(options, _dataArr, _dataAttr);
    }

    function update() {
       // ScatterPlots.update();
    }

    return {
        init: function(_divName, _geneX, _geneY, _dataArr, _dataAttr) {
            $("#" + _divName).empty();
            settings(_divName, _geneX, _geneY, _dataAttr);
            layout();
            show(_dataArr, _dataAttr);
        },
        show: show,
        update: update
    }

}());