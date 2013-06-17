<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<div id="oncoprint" style="padding-top:10px; padding-bottom:10px; padding-left:10px; border: 1px solid #CCC;">
    <img class="loader_img" src="images/ajax-loader.gif"/>
    <div style="display:none;" id="everything">
        <h4>OncoPrint
            <small>(<a href="faq.jsp#what-are-oncoprints">What are OncoPrints?</a>)</small>
        </h4>

        <form id="oncoprintForm" action="oncoprint_converter.svg" enctype="multipart/form-data" method="POST"
              onsubmit="this.elements['xml'].value=oncoprint.getOncoPrintBodyXML(); return true;" target="_blank">
            <input type="hidden" name="xml">
            <input type="hidden" name="longest_label_length">
            <input type="hidden" name="format" value="svg">
            <p>Download OncoPrint:&nbsp;&nbsp;&nbsp;<input type="submit" value="SVG"></p>
        </form>

        <div id="oncoprint_controls">
            <style>
                .onco-customize {
                    color:#2153AA; font-weight: bold; cursor: pointer;
                }
                .onco-customize:hover { text-decoration: underline; }
            </style>
            <p onclick="$('#oncoprint_controls #main').toggle(); $('#oncoprint_controls .triangle').toggle();"
               style="margin-bottom: 0px;">
                <span class='triangle ui-icon ui-icon-triangle-1-e' style='float:left;'></span>
                <span class='triangle ui-icon u.jquery.mini-icon-triangle-1-s' style='float:left; display:none;'></span>
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
                            <span>Sort By: </span>
                            <select id="sort_by" style="width: 200px;">
                                <option selected="selected" value="genes">gene data</option>
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
            <script type="text/javascript" src="js/oncoprint.js"></script>
            <img class="loader_img" style="display:hidden;" src="images/ajax-loader.gif"/>

            <script type="text/javascript">
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
                    genes: GeneSet(gene_list).getAllGenes().join(" "),
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
                        $(select_clinical_attributes_id).chosen({width: "100%", "font-size": "12px"});
                    }
                });

                var oncoprint;
                var zoom;
                geneDataColl.fetch({
                    type: "POST",
                    success: function(data) {
                        oncoprint = Oncoprint(document.getElementById('oncoprint_body'),
                                { geneData: data.toJSON(), genes: geneDataColl.genes.split(" ") });
                        $('#oncoprint .loader_img').hide();
                        $('#oncoprint #everything').show();

                        zoom = oncoprintZoomSetup($('#oncoprint_controls #zoom'));
                    }
                });

                var select_clinical_attributes_id = '#select_clinical_attributes';
                var oncoprintClinicals;
                var sortBy = $('#oncoprint_controls #sort_by');

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
                    sortBy.attr('disabled', enable_disable);
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
                            genes: geneDataColl.genes.split(" ")
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
                                    clinical_attrs: response.attributes()
                                });

                                // enable the option to sort by clinical data
                                $(sortBy.add('option[value="clinical"]')[1]).prop('disabled', false);
                                sortBy.val('genes');        // sort by genes by default

                                toggleControls(true);

                                // set the zoom to be whatever the slider currently says it is
                                oncoprint.zoom(zoom.slider("value"));
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
    </div>
</div>
