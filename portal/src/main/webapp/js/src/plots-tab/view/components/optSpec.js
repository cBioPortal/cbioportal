var optSpec = (function() {
    
    var append_view_switch_opt = function() {
        var stat = plotsData.stat();
        var tmp = setInterval(function () {timer();}, 1000);
        function timer() {
            if (metaData.getRetrieveStatus() !== -1 && stat.retrieved) {
                clearInterval(tmp);
                append();
            }
        }
        function append() {
            $("#" + ids.sidebar.util.view_switch + "-div").empty();
            if (genetic_vs_genetic()) {
                if(isSameGene()) {
                    if (stat.hasCnaAnno) {
                        $("#" + ids.sidebar.util.view_switch + "-div").append(
                            "<h5>View</h5>" + 
                            "<input type='radio' value='mutation_details' name='mutation_details_vs_gistic_view' checked>Mutation Type" + 
                            "<input type='radio' value='gistic' name='mutation_details_vs_gistic_view' >Copy-number"
                        );                    
                    }
                }                
            }
        }
    };
    
    var append_download_buttons = function() {

        $("#" + ids.sidebar.util.download_buttons).append("<button id='svg_download'>SVG</button>");
        $("#svg_download").click(function() {
              var xmlSerializer = new XMLSerializer();
              var download_str = cbio.download.addSvgHeader(xmlSerializer.serializeToString($("#" + ids.main_view.div + " svg")[0]));
              cbio.download.clientSideDownload([download_str], "result.svg", "application/svg+xml");
        });

        $("#" + ids.sidebar.util.download_buttons).append("<button id='pdf_download'>PDF</button>");
        $("#pdf_download").click(function() {
            var downloadOptions = {
                filename: "result.pdf",
                contentType: "application/pdf",
                servletName: "svgtopdf.do",
                preProcess: trimHelpIcon 
            };
            function trimHelpIcon(svg_str) {
                //<image xmlns:xlink="http://www.w3.org/1999/xlink" xlink:href="images/help.png" class="plots-y-axis-title-help" x="44" y="79" width="16" height="16" data-hasqtip="1312"/>
                var _begin_index_1 = svg_str.indexOf("<image");
                var _end_index_1 = svg_str.indexOf("data-hasqtip") + 21;
                var _trimmed_str_1 = svg_str.substring(0, _begin_index_1) + svg_str.substring(_end_index_1, svg_str.length);
                var _begin_index_2 = _trimmed_str_1.indexOf("<image");
                var _end_index_2 = _trimmed_str_1.indexOf("data-hasqtip") + 21;
                var _trimmed_str_2 = _trimmed_str_1.substring(0, _begin_index_2) + _trimmed_str_1.substring(_end_index_2, svg_str.length);
                return _trimmed_str_2;
            }
            cbio.download.initDownload(
                    $("#" + ids.main_view.div + " svg")[0], downloadOptions);

        });
        
            
    };
 
    return {
        init: function() {
            if (!document.getElementById(ids.sidebar.util.view_switch) + "-div") {
                $("#" + ids.sidebar.util.div).append("<div id='" + 
                        ids.sidebar.util.view_switch + "-div'></div>");
            } else {
                $("#" + ids.sidebar.util.view_switch + "-div").empty();
            }
            append_view_switch_opt();

            $("#" + ids.sidebar.util.download_buttons + "").empty();
            append_download_buttons();
        }
    };
    
}());