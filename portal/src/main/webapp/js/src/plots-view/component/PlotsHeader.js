var PlotsHeader = (function() {

    var divName = "", 
        title = "",
        fileName = "",
        targetDiv = "";

    function appendTitle() {
        $("#" + divName).append("<b>" + title + "</b>");
    }

    function appendPdfConverter() {
        var pdfConverterForm =
            "<form style='display:inline-block' action='svgtopdf.do' method='post' " +
                "onsubmit=\"this.elements['svgelement'].value=PlotsHeader.loadSvg('" + targetDiv + "');\">" +
                "<input type='hidden' name='svgelement'>" +
                "<input type='hidden' name='filetype' value='pdf'>" +
                "<input type='hidden' name='filename' value='" + fileName + ".pdf'>" +
                "<input type='submit' value='PDF'></form>";
        $("#" + divName).append(pdfConverterForm);
    }

    function appendSvgConverter() {
        var svgConverterForm =
            "<form style='display:inline-block' action='svgtopdf.do' method='post' " +
                "onsubmit=\"this.elements['svgelement'].value=PlotsHeader.loadSvg('" + targetDiv + "');\">" +
                "<input type='hidden' name='svgelement'>" +
                "<input type='hidden' name='filetype' value='svg'>" +
                "<input type='hidden' name='filename' value='" + fileName + ".svg'>" +
                "<input type='submit' value='SVG'></form>";
        $("#" + divName).append(svgConverterForm);
    }

    return {
        init: function(_divName, _title, _fileName, _targetDiv) {
            divName = _divName;
            title = _title;
            fileName = _fileName;
            targetDiv = _targetDiv;
            appendTitle();
            appendPdfConverter();
            appendSvgConverter();
        },
        loadSvg: function(_divName) {
            return $("#" + _divName).html();
        }
    }

}());