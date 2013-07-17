<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<div id="oncoprint" style="padding-top:10px; padding-bottom:10px; padding-left:10px; border: 1px solid #CCC;">
    <img class="loader_img" src="images/ajax-loader.gif"/>
    <div style="display:none;" id="everything">
        <h4 style="display:inline;">OncoPrint
            <small>(<a href="faq.jsp#what-are-oncoprints">What are OncoPrints?</a>)</small>
        </h4>

        <span>
            <form style="display:inline;" action="svgtopdf.do" method="post" onsubmit="this.elements['svgelement'].value=oncoprint.getPdfInput();">

                <input type="hidden" name="svgelement">
                <input type="hidden" name="filetype" value="pdf">
                <input type="submit" value="PDF">
            </form>

            <form style="display:inline;" action="oncoprint_converter.svg" enctype="multipart/form-data" method="POST"
                  onsubmit="this.elements['xml'].value=oncoprint.getPdfInput(); return true;" target="_blank">
                <input type="hidden" name="xml">
                <input type="hidden" name="longest_label_length">
                <input type="hidden" name="format" value="svg">
                <input type="submit" value="SVG">
            </form>
        </span>

        <div id="oncoprint_controls" style="margin-top:10px; margin-bottom:20px;">
            <style>
                .onco-customize {
                    color:#2153AA; font-weight: bold; cursor: pointer;
                }
                .onco-customize:hover { text-decoration: underline; }
            </style>
            <p onclick="$('#oncoprint_controls #main').toggle(); $('#oncoprint_controls .triangle').toggle();"
               style="margin-bottom: 0px;">
                <span class="triangle ui-icon ui-icon-triangle-1-e" style="float: left; display: block;"></span>
                <span class="triangle ui-icon ui-icon-triangle-1-s" style="float: left; display: none;"></span>
                <span class='onco-customize'>Customize</span>
            </p>

            <div id="main" style="display:none;">
                <table style="padding-left:13px; padding-top:5px">
                    <tr>
                        <td style="padding-right: 15px;"><span>Zoom</span><div id="zoom" style="display: inline-table;"></div></td>
                        <td><input id='toggle_unaltered_cases' type='checkbox' onclick='oncoprint.toggleUnalteredCases();'>Remove Unaltered Cases</td>
                        <td><input id='toggle_whitespace' type='checkbox' onclick='oncoprint.toggleWhiteSpace();'>Remove Whitespace</td>
                    </tr>
                    <tr>
                        <td>
                            <div id="disable_select_clinical_attributes" style="display: none; z-index: 1000; opacity: 0.7; background-color: grey; width: 22.5%; height: 6%; position: absolute;"></div>
                            <select data-placeholder="add clinical attribute track" id="select_clinical_attributes" style="width: 350px;">
                                <option value=""></option>
                            </select>
                        </td>
                        <td>
                            <span>Sort by: </span>
                            <select id="sort_by" style="width: 200px;">
                                <option value="genes">gene data</option>
                                <option value="clinical" disabled>clinical data</option>
                                <option value="alphabetical">alphabetically by case id</option>
                                <option value="custom">user-defined case list / default</option>
                            </select>
                        </td>
                    </tr>
                </table>
            </div>

        </div>
        <div id="oncoprint_body">
            <script type="text/javascript" src="js/src/oncoprint.js"></script>

            <script type="text/javascript">

                // This is for the moustache-like templates
                // prevents collisions with JSP tags
                _.templateSettings = {
                    interpolate : /\{\{(.+?)\}\}/g
                };

                var oncoPrintParams = {
                    geneData: undefined,
                    cancer_study_id: "<%=cancerTypeId%>",
                    case_set_str: "<%=StringEscapeUtils.escapeHtml(OncoPrintUtil.getCaseSetDescription(caseSetId, caseSets))%>",
                    num_cases_affected: "<%=dataSummary.getNumCasesAffected()%>",
                    percent_cases_affected: "<%=OncoPrintUtil.alterationValueToString(dataSummary.getPercentCasesAffected())%>",
                    vis_key: true,
                    customize: true
                };

                var geneDataColl = new GeneDataColl({
                    cancer_study_id: cancer_study_id_selected,
                    genes: typeof gene_list === "string" ? GeneSet(gene_list).getAllGenes().join(" ") : GeneSet(gene_list.innerHTML).getAllGenes().join(" "),
                    case_list: cases,
                    genetic_profiles: genetic_profiles,
                    z_score_threshold: zscore_threshold,
                    rppa_score_threshold: rppa_score_threshold
                });

                // takes a div and creates a zoombar on it.  Inside it refers
                // to a global var called `oncoprint` on which it zooms.
                var oncoprintZoomSetup = function(div) {
                    return $('<div>', { id: "width_slider", width: "100"})
                            .slider({ text: "Adjust Width ", min: .1, max: 1, step: .01, value: 1,
                                change: function(event, ui) {
                                    oncoprint.zoom(ui.value, 'animation');       // N.B.
                                }}).appendTo($(div));
                };

                var clinicalAttributes = new ClinicalAttributesColl({case_list: cases});

                clinicalAttributes.fetch({
                    success: function(attrs) {
                        OncoprintUI.populate_clinical_attr_select(document.getElementById('select_clinical_attributes'), attrs.toJSON());
                        $(select_clinical_attributes_id).chosen({width: "240px", "font-size": "12px"});
                    }
                });

                var oncoprint;
                var zoom;
                geneDataColl.fetch({
                    type: "POST",
                    success: function(data) {
                        oncoprint = Oncoprint(document.getElementById('oncoprint_body'), {
                            geneData: data.toJSON(),
                            genes: geneDataColl.genes.split(" "),
                            legend: document.getElementById('oncoprint_legend')
                        });
                        $('#oncoprint .loader_img').hide();
                        $('#oncoprint #everything').show();

                        zoom = oncoprintZoomSetup($('#oncoprint_controls #zoom'));
                    }
                });

                var select_clinical_attributes_id = '#select_clinical_attributes';
                var oncoprintClinicals;
                var sortBy = $('#oncoprint_controls #sort_by');
                $('#oncoprint_controls #sort_by').chosen({width: "240px", disable_search: true });

                // params: bool
                // enable or disable all the various oncoprint controls
                // true -> enable
                // false -> disable
                var toggleControls = function(bool) {
                    var whitespace = $('#toggle_whitespace');
                    var unaltered = $('#toggle_unaltered_cases');
                    var select_clinical_attributes =  $(select_clinical_attributes_id);

                    var enable_disable = !bool;

                    whitespace.attr('disabled', enable_disable);
                    unaltered.attr('disabled', enable_disable);
                    select_clinical_attributes.prop('disabled', enable_disable).trigger("liszt:updated");
                    zoom.attr('disabled', enable_disable);
                    sortBy.prop('disabled', enable_disable).trigger("liszt:updated");
                };

                // handler for when user selects a clinical attribute to visualization
                var clinicalAttributeSelected = function() {
                    oncoprint.remove_oncoprint();
                    $('#oncoprint_body .loader_img').show();
                    toggleControls(false);

                    var clinicalAttribute = $(select_clinical_attributes_id + ' option:selected')[0].__data__;

                    if (clinicalAttribute.attr_id === undefined) {      // selected "none"
                        $('#oncoprint_body .loader_img').hide();

                        oncoprint = Oncoprint(document.getElementById('oncoprint_body'), {
                            geneData: geneDataColl.toJSON(),
                            genes: geneDataColl.genes.split(" "),
                            legend: document.getElementById('oncoprint_legend')
                        });

                        // disable the option to sort by clinical data
                        $(sortBy.add('option[value="clinical"]')[1]).prop('disabled', true);
                    } else {
                        oncoprintClinicals = new ClinicalColl({
                            cancer_study_id: cancer_study_id_selected,
                            attr_id: clinicalAttribute.attr_id,
                            case_list: cases
                        });

                        oncoprintClinicals.fetch({
                            type: "POST",
                            success: function(response) {
                                $('#oncoprint_body .loader_img').hide();

                                oncoprint = Oncoprint(document.getElementById('oncoprint_body'), {
                                    geneData: geneDataColl.toJSON(),
                                    clinicalData: response.toJSON(),
                                    genes: geneDataColl.genes.split(" "),
                                    clinical_attrs: response.attributes(),
                                    legend: document.getElementById('oncoprint_legend')
                                });

                                // enable the option to sort by clinical data
                                $(sortBy.add('option[value="clinical"]')[1]).prop('disabled', false);

                                // sort by genes by default
                                sortBy.val('genes');

                                toggleControls(true);

                                // set the zoom to be whatever the slider currently says it is
                                oncoprint.zoom(zoom.slider("value"));
                                oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
                                oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
                                OncoprintUI.make_mouseover(d3.selectAll('.sample rect'))        // hack =(
                            }
                        });
                    }
                };
                $(select_clinical_attributes_id).change(clinicalAttributeSelected);

                $(document).ready(function() {
                    // bind away
                    $('#oncoprint_controls #sort_by').change(function() {
                        oncoprint.sortBy(sortBy.val(), cases.split(" "));
                    });
                });
            </script>
        </div>

        <div id="oncoprint_legend"></div>
        <script type="text/template" id="glyph_template">
            <svg height="23" width="6">
            <rect fill="{{bg_color}}" width="5.5" height="23"></rect>

            <rect display="{{display_mutation}}" fill="#008000" y="7.666666666666667" width="5.5" height="7.666666666666667"></rect>

            <path display="{{display_down_rppa}}" d="M0,2.182461848650375L2.5200898716287647,-2.182461848650375 -2.5200898716287647,-2.182461848650375Z" transform="translate(2.75,2.3000000000000003)"></path>
            <path display="{{display_up_rppa}}" d="M0,-2.182461848650375L2.5200898716287647,2.182461848650375 -2.5200898716287647,2.182461848650375Z" transform="translate(2.75,20.909090909090907)" aria-describedby="ui-tooltip-838"></path>

            <rect display="{{display_down_mrna}}" height="23" width="5.5" stroke-width="2" stroke-opacity="1" stroke="#6699CC" fill="none" aria-describedby="ui-tooltip-732"></rect>
            <rect display="{{display_up_mrna}}" height="23" width="5.5" stroke-width="2" stroke-opacity="1" stroke="#FF9999" fill="none" aria-describedby="ui-tooltip-576"></rect>
            </svg>
            <span style="position: relative; bottom: 6px;">{{text}}</span>
        </script>

    </div>
</div>
