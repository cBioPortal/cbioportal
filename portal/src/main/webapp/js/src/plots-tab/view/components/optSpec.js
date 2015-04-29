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
            $("#" + ids.sidebar.util.view_switch).empty();
            if (genetic_vs_genetic()) {
                if(isSameGene()) {
                    if (stat.hasCnaAnno) {
                        $("#" + ids.sidebar.util.view_switch).append(
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

        //SVG & PDF buttons
        $("#" + ids.sidebar.util.download_buttons).append("<button id='svg_download'>SVG</button>");
        $("#svg_download").click(function() {
            //remove the help icon
            var elem_x_txt = $("." + d3_class.x.title_help).qtip('api').options.content.text;
            var elem_y_txt = $("." + d3_class.y.title_help).qtip('api').options.content.text;
            var elem_x = $("." + d3_class.x.title_help).remove();
            var elem_y = $("." + d3_class.y.title_help).remove(); 
            //send download request
            var xmlSerializer = new XMLSerializer();
            var download_str = cbio.download.addSvgHeader(xmlSerializer.serializeToString($("#" + ids.main_view.div + " svg")[0]));
            cbio.download.clientSideDownload([download_str], "plots-download.svg", "application/svg+xml");
            //add back on the help icons
            $(".axis").append(elem_x);
            $(".axis").append(elem_y);
            $("." + d3_class.x.title_help).qtip({
                content: {text: "<font size=2>" + elem_x_txt + "</font>" },
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                show: {event: "mouseover"},
                hide: {fixed:true, delay: 100, event: "mouseout"},
                position: {my:'left bottom',at:'top right', viewport: $(window)}
            });
            $("." + d3_class.y.title_help).qtip({
                content: {text: "<font size=2>" + elem_y_txt + "</font>" },
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                show: {event: "mouseover"},
                hide: {fixed:true, delay: 100, event: "mouseout"},
                position: {my:'left bottom',at:'top right', viewport: $(window)}
            });
        });
        $("#" + ids.sidebar.util.download_buttons).append("<button id='pdf_download'>PDF</button>");
        $("#pdf_download").click(function() {
            //remove the help icon
            var elem_x_txt = $("." + d3_class.x.title_help).qtip('api').options.content.text;
            var elem_y_txt = $("." + d3_class.y.title_help).qtip('api').options.content.text;
            var elem_x = $("." + d3_class.x.title_help).remove();
            var elem_y = $("." + d3_class.y.title_help).remove();
            //send download request
            var downloadOptions = {
                filename: "plots-download.pdf",
                contentType: "application/pdf",
                servletName: "svgtopdf.do"
            };
            cbio.download.initDownload(
                    $("#" + ids.main_view.div + " svg")[0], downloadOptions);
            //add back on the help icons
            $(".axis").append(elem_x);
            $(".axis").append(elem_y);
            $("." + d3_class.x.title_help).qtip({
                content: {text: "<font size=2>" + elem_x_txt + "</font>" },
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                show: {event: "mouseover"},
                hide: {fixed:true, delay: 100, event: "mouseout"},
                position: {my:'left bottom',at:'top right', viewport: $(window)}
            });
            $("." + d3_class.y.title_help).qtip({
                content: {text: "<font size=2>" + elem_y_txt + "</font>" },
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                show: {event: "mouseover"},
                hide: {fixed:true, delay: 100, event: "mouseout"},
                position: {my:'left bottom',at:'top right', viewport: $(window)}
            });
        });
        
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
        
        //Data download button
        $("#" + ids.sidebar.util.download_buttons).append("<button id='opt_spec_data_download'>Data</button>");
        $("#opt_spec_data_download").click(function() {
            if (genetic_vs_genetic()) {
                if (is_profile_discretized("x") && is_profile_discretized("y")) {
                    cbio.download.clientSideDownload([heat_map.get_tab_delimited_data()], "plots-data.txt");
                } else {
                    cbio.download.clientSideDownload([scatterPlots.get_tab_delimited_data()], "plots-data.txt");
                }
            } else if (genetic_vs_clinical()) {
                var _clin_axis = ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.clin)? "x": "y";
                var _genetic_axis = ($("input:radio[name='" + ids.sidebar.x.data_type + "']:checked").val() === vals.data_type.clin)? "y": "x";
                if (clinical_attr_is_discretized(_clin_axis) && is_profile_discretized(_genetic_axis)) {
                    cbio.download.clientSideDownload([heat_map.get_tab_delimited_data()], "plots-data.txt");
                } else {
                    cbio.download.clientSideDownload([scatterPlots.get_tab_delimited_data()], "plots-data.txt");
                }           
            } else if (clinical_vs_clinical()) {
                if (clinical_attr_is_discretized("x") && clinical_attr_is_discretized("y")) {
                    cbio.download.clientSideDownload([heat_map.get_tab_delimited_data()], "plots-data.txt");
                } else {
                    cbio.download.clientSideDownload([scatterPlots.get_tab_delimited_data()], "plots-data.txt");
                } 
            }
        });
 
    };
 
    return {
        init: function() {

            $("#" + ids.sidebar.util.view_switch).empty();
            append_view_switch_opt();

            $("#" + ids.sidebar.util.download_buttons).empty();
            append_download_buttons();
        }
    };
    
}());