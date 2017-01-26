var optSpec = (function() {
    
	/**
	 * Check if there is COPY_NUMBER_ALTERATION data for the x or y axis gene. Returns true
	 * if this is the case. 
	 */
	var hasCNA = function() {
		var elt_x = document.getElementById(ids.sidebar.x.gene);
	    var elt_y = document.getElementById(ids.sidebar.y.gene);
	    //TODO - these two should be read from a model instead (once there is a model) - see also TODO's in plotUtils.js
	    var gene_x = elt_x.options[elt_x.selectedIndex].value;
	    var gene_y = elt_y.options[elt_y.selectedIndex].value;
	    
		var profiles = metaData.getGeneticProfilesMeta(gene_x).concat(metaData.getGeneticProfilesMeta(gene_y));
		for (var i = 0; i < profiles.length; i++) {
			if (profiles[i].type === "COPY_NUMBER_ALTERATION") {
				return true;
			}
		}
		return false;
	}
	
    var append_view_switch_opt = function() {        
    	//if these conditions apply, we want to show an extra switch to allow different visualization of the data:
        if (genetic_vs_genetic() && isSameGene() && hasCNA()) {
        	if ($("#" + ids.sidebar.util.view_switch).is(':empty')) {
                $("#" + ids.sidebar.util.view_switch).append(
                    "<div class='form-inline'>" +
                    "<h5>View</h5>" +
                    "<label class='radio-inline'><input type='radio' value='mutation_details' name='"+ids.sidebar.util.view_switch+"' checked>Mutation Type</label>" +
                    "<label class='radio-inline'><input type='radio' value='gistic' name='"+ids.sidebar.util.view_switch+"' >Copy-number</label>" +
                    "</div>"
                );
            }
        }
    	//switch is not applicable in this case, so remove from UI:
        else {
        	$("#" + ids.sidebar.util.view_switch).empty();
        }
    };
    
    var append_download_buttons = function() {
        $("#" + ids.sidebar.util.download_buttons).empty();

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
            append_view_switch_opt();
            append_download_buttons();
        }
    };
    
}());