// boilerplate for the main portal page
//
// Gideon Dresdner July 2013
requirejs(  [         'Oncoprint',    'OncoprintUtils'],
            function(   Oncoprint,      utils) {

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
                     utils.populate_clinical_attr_select(document.getElementById('select_clinical_attributes'), attrs.toJSON());
                     $(select_clinical_attributes_id).chosen({width: "240px", "font-size": "12px"});
                 }
    });

    var oncoprint;
    var zoom;

    var genes = window.gene_list;
    try {
        if (_.isArray(genes)) {
            genes = genes.join(" ");
        }
        if (_.isElement(genes)) {
            genes = GeneSet(window.gene_list.innerHTML).getAllGenes().join(" ");
        }
    } catch (err) {
        throw new Error(err);
    }

    var geneDataColl = new GeneDataColl({
        cancer_study_id: cancer_study_id_selected,
        genes: genes,
        case_list: cases,
        genetic_profiles: genetic_profiles,
        z_score_threshold: zscore_threshold,
        rppa_score_threshold: rppa_score_threshold
    });

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
                    utils.make_mouseover(d3.selectAll('.sample rect'))        // hack =(
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

        $('#toggle_unaltered_cases').click(function() {
            oncoprint.toggleUnalteredCases();
        });

        $('#toggle_whitespace').click(function() {
            oncoprint.toggleWhiteSpace();
        });
    });
});
