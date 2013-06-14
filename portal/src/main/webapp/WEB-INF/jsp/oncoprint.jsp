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
                        <td><input type='checkbox' onclick='oncoprint.toggleUnalteredCases();'>Remove Unaltered Cases</td>
                        <td>
                            <select data-placeholder="select a clinical attribute" id="select_clinical_attributes" style="width: 350px;">
                                <option value=""></option>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding-right: 15px;"><span>Zoom</span><div id="zoom" style="display: inline-table;"></div></td>
                        <td>
                            <span>Sort By: </span>
                            <select id="sort_by" style="width: 200px;">
                                <option selected="selected" value="genes">Gene Data</option>
                                <option value="clinical" disabled>Clinical Data</option>
                                <option value="alphabetical">Alphabetically by Case Id</option>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td><input type='checkbox' onclick='oncoprint.toggleWhiteSpace();'>Remove Whitespace</td>
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
                    genes: GeneSet(gene_list).getAllGenes(),
                    case_list: cases,
                    genetic_profiles: genetic_profiles,
                    z_score_threshold: zscore_threshold,
                    rppa_score_threshold: rppa_score_threshold
                });

                // takes a div and creates a zoombar on it.  Inside it refers
                // to a global var called `oncoprint` on which it zooms.
                var oncoprintZoomSetup = function(div) {
                    $('<div>', { id: "width_slider", width: "100"})
                            .slider({ text: "Adjust Width ", min: .1, max: 1, step: .01, value: 1,
                                change: function(event, ui) {
                                    oncoprint.zoom(ui.value);       // N.B.
                                }}).appendTo($(div));
                };

                var clinicalAttributes = new ClinicalAttributesColl({case_list: cases});

                clinicalAttributes.fetch({
                    success: function(attrs) {
                        OncoprintUI.populate_clinical_attr_select(document.getElementById('select_clinical_attributes'), attrs.toJSON());
                        $(select_clinical_attributes_id).chosen({width: "100%", "font-size": "12px", color: "black"});
                    }
                });

                var oncoprint;
                geneDataColl.fetch({
                    type: "POST",
                    success: function(data) {
                        oncoprint = Oncoprint(document.getElementById('oncoprint_body'),
                                { geneData: data.toJSON(), genes: geneDataColl.genes.split(" ") });
                        $('#oncoprint .loader_img').hide();
                        $('#oncoprint #everything').show();

                        oncoprintZoomSetup($('#oncoprint_controls #zoom'));
                    }
                });

                var select_clinical_attributes_id = '#select_clinical_attributes';
                var oncoprintClinicals;
                var sortBy = $('#oncoprint_controls #sort_by');

                // handler for when user selects a clinical attribute to visualization
                var clinicalAttributeSelected = function() {
                    oncoprint.remove_oncoprint();
                    $('#oncoprint_body .loader_img').show();

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
                            }
                        });
                    }
                };
                $(select_clinical_attributes_id).change(clinicalAttributeSelected);

                // bind away
                $('#oncoprint_controls #sort_by').change(function() {
                    oncoprint.sortBy(sortBy.val());
                });

                <%--var geneDataQuery = {--%>
                    <%--cancer_study_id: "<%=cancerTypeId%>",--%>
                    <%--genes: genes,--%>
                    <%--geneticProfileIds: geneticProfiles,--%>
                    <%--z_score_threshold: <%=zScoreThreshold%>,--%>
                    <%--rppa_score_threshold: <%=rppaScoreThreshold%>--%>
                <%--};--%>
                <%--geneDataQuery = injectCaseSet(geneDataQuery);--%>

                <%--var oncoprint;      // global--%>
                <%--$.post(DataManagerFactory.getGeneDataJsonUrl(), geneDataQuery, function(geneData) {--%>

                    <%--oncoPrintParams['geneData'] = geneData;--%>

                    <%--var clinicals = new ClinicalColl([], {--%>
                        <%--case_set_id: "<%=caseSetId%>"--%>
                    <%--});--%>
                    <%--clinicals.fetch({--%>
                        <%--"success": function(clinicalData) {--%>
                            <%--oncoPrintParams['clinicalData'] = clinicalData.toJSON();--%>

                            <%--oncoPrintParams['genes'] = genes.split(" ");--%>
                            <%--oncoPrintParams['clinical_attrs'] = ["VITAL_STATUS", "DAYS_TO_DEATH"];--%>

                            <%--oncoprint = Oncoprint($('#oncoprint_body')[0], oncoPrintParams);--%>
                            <%--$('#oncoprint #loader_img').hide();--%>
                            <%--$('#oncoprint #everything').show();--%>

                        <%--}--%>
                    <%--});--%>
                <%--});--%>
            </script>
        </div>

        <div id="oncoprint_legend"></div>
    </div>
</div>
