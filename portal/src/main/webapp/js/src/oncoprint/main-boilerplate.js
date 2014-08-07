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

    // add in controls from template
    document.getElementById('oncoprint_controls').innerHTML
        = _.template(document.getElementById('main-controls-template').innerHTML)();

    var clinicalAttributes = new ClinicalAttributesColl();

    var $zoom_el = $('#oncoprint_controls #zoom');
    var zoom;

    // basically a hack to prevent the zoom function from a particular oncoprint
    // from getting bound to the UI slider forever
    var reset_zoom = function() {
        $zoom_el.empty();
        zoom = utils.zoomSetup($zoom_el, oncoprint.zoom);

        return zoom;
    };

    clinicalAttributes.fetch({
        type: 'POST',
        data: { cancer_study_id: cancer_study_id_selected,
            case_list: window.PortalGlobals.getCases() },
        success: function(attrs) {
            var totalAttrs = attrs.toJSON();
            if(window.PortalGlobals.gerMutationProfileId()!==null){
                var tem={attr_id: "mutations", datatype: "NUMBER",description: "Number of mutation", display_name: "Mutations"};
                totalAttrs.unshift(tem);
            }
            utils.populate_clinical_attr_select(document.getElementById('select_clinical_attributes'), totalAttrs);
            $(select_clinical_attributes_id).chosen({width: "240px", "font-size": "12px", search_contains: true});
        }
    });

    var oncoprint;
    
    var extraTracks=[]; // used to record tracks add customized
    var extraGenes=[]; // used to record genes add customized
    var extraAttributes=[]; // used to record attributes add customized
    var cases = window.PortalGlobals.getCases();
    var genes = window.PortalGlobals.getGeneListString().split(" ");

    var outer_loader_img = $('#oncoprint #outer_loader_img');
    var inner_loader_img = $('#oncoprint #inner_loader_img');

    var geneDataColl = new GeneDataColl();
    geneDataColl.fetch({
        type: "POST",
        data: {
            cancer_study_id: cancer_study_id_selected,
            oql: $('#gene_list').val(),
            case_list: cases,
            geneticProfileIds: window.PortalGlobals.getGeneticProfiles(),
            z_score_threshold: window.PortalGlobals.getZscoreThreshold(),
            rppa_score_threshold: window.PortalGlobals.getRppaScoreThreshold()
        },
        success: function(data) {
            oncoprint = Oncoprint(document.getElementById('oncoprint_body'), {
                geneData: data.toJSON(),
                genes: genes,
                legend: document.getElementById('oncoprint_legend')
            },extraTracks);
            outer_loader_img.hide();
            $('#oncoprint #everything').show();

            oncoprint.sortBy(sortBy.val(), cases.split(" "));

            zoom = reset_zoom();
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

    //delete clinicalAttribute added before
    var removeClinicalAttribute = function()
    {
        oncoprint.remove_oncoprint();
        inner_loader_img.show();
        toggleControls(false); //disable toggleControls

        inner_loader_img.hide();

        oncoprint = Oncoprint(document.getElementById('oncoprint_body'), {
            geneData: geneDataColl.toJSON(),
            clinicalData: extraGenes,
            genes: genes,
            clinical_attrs: extraAttributes,
            legend: document.getElementById('oncoprint_legend')
        },extraTracks);

        oncoprint.sortBy(sortBy.val(), cases.split(" "));
        toggleControls(true);
        $('.special_delete').click(function() {
            var attr = $(this).attr("alt");
            var indexNum = extraTracks.indexOf(attr);
            extraTracks.splice(indexNum, 1);
            extraGenes.splice(indexNum, 1);
            extraAttributes.splice(indexNum, 1);
            removeClinicalAttribute();
        });// enable delete symbol "x" function
        //
        //tooltip for the track deletion function
        $('.special_delete').qtip({
                    content: {text: 'click here to delete this track!'},
                    position: {my:'left bottom', at:'top right', viewport: $(window)},
                    style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                    show: {event: "mouseover"},
                    hide: {fixed: true, delay: 100, event: "mouseout"}
                    });
        $('.special_delete').hover(
                    function () {
                    $(this).css('fill', '#0000FF');
                    $(this).css('font-size', '18px');
                    },
                    function () {
                    $(this).css('fill', '#87CEFA');
                    $(this).css('font-size', '12px');
                    });
        oncoprint.sortBy(sortBy.val(), cases.split(" "));
//        // disable the option to sort by clinical data
//        $(sortBy.add('option[value="clinical"]')[1]).prop('disabled', true);
    }

    // handler for when user selects a clinical attribute to visualization
    var clinicalAttributeSelected = function() {
        oncoprint.remove_oncoprint();
        inner_loader_img.show();
        toggleControls(false);

        var clinicalAttribute = $(select_clinical_attributes_id + ' option:selected')[0].__data__;

        if (clinicalAttribute.attr_id === undefined) {      // selected "none"
            inner_loader_img.hide();

            oncoprint = Oncoprint(document.getElementById('oncoprint_body'), {
                geneData: geneDataColl.toJSON(),
                genes: genes,
                legend: document.getElementById('oncoprint_legend')
            },extraTracks);

            oncoprint.sortBy(sortBy.val(), cases.split(" "));

            // disable the option to sort by clinical data
            $(sortBy.add('option[value="clinical"]')[1]).prop('disabled', true);
        } else {
            if(clinicalAttribute.attr_id === "mutations")
            {
                    oncoprintClinicals = new ClinicalMutationColl();
                    oncoprintClinicals.fetch({
                    type: "POST",

                    data: {
                            mutation_profile: window.PortalGlobals.gerMutationProfileId(),
                            cmd: "count_mutations",
                            case_ids: cases
                    },
                    success: function(response) {
                        inner_loader_img.hide();
                        
                        extraTracks = extraTracks.concat(response.attributes().map(function(attr) { return attr.attr_id; }));
                        extraGenes = extraGenes.concat(response.toJSON());
                        extraAttributes=extraAttributes.concat(response.attributes());
                        oncoprint = Oncoprint(document.getElementById('oncoprint_body'), {
                            geneData: geneDataColl.toJSON(),
                            clinicalData: extraGenes,
                            genes: genes,
                            clinical_attrs: extraAttributes,
                            legend: document.getElementById('oncoprint_legend')
                        },extraTracks);
                             
                        oncoprint.sortBy(sortBy.val(), cases.split(" "));

                        // enable the option to sort by clinical data
                        $(sortBy.add('option[value="clinical"]')[1]).prop('disabled', false);

//                        // sort by genes by default
//                        sortBy.val('genes');

                        toggleControls(true);
                        
                        $('.special_delete').click(function() {
                            var attr = $(this).attr("alt");
                            var indexNum = extraTracks.indexOf(attr);
                            extraTracks.splice(indexNum, 1);
                            extraGenes.splice(indexNum, 1);
                            extraAttributes.splice(indexNum, 1);
                            removeClinicalAttribute();
                        });// enable delete symbol "x" function
                        
                        zoom = reset_zoom();

                        // sync
                        oncoprint.zoom(zoom.slider("value"));
                        oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
                        oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
                        utils.make_mouseover(d3.selectAll('.sample rect'),{linkage:true});        // hack =(


                        //tooltip for the track deletion function
                        $('.special_delete').qtip({
                                    content: {text: 'click here to delete this track!'},
                                    position: {my:'left bottom', at:'top right', viewport: $(window)},
                                    style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                                    show: {event: "mouseover"},
                                    hide: {fixed: true, delay: 100, event: "mouseout"}
                                });
                                
                        $('.special_delete').hover(
                                    function () {
                                    $(this).css('fill', '#0000FF');
                                    $(this).css('font-size', '18px');
                                    },
                                    function () {
                                    $(this).css('fill', '#87CEFA');
                                    $(this).css('font-size', '12px');
                                    });
                    }
                });
            }
            else
            {
                oncoprintClinicals = new ClinicalColl();
                    oncoprintClinicals.fetch({
                    type: "POST",

                    data: {
                        cancer_study_id: cancer_study_id_selected,
                        attribute_id: clinicalAttribute.attr_id,
                        case_list: cases
                    },
                    success: function(response) {
                        inner_loader_img.hide();
                        
                        extraTracks = extraTracks.concat(response.attributes().map(function(attr) { return attr.attr_id; }));
                        extraGenes = extraGenes.concat(response.toJSON());
                        extraAttributes=extraAttributes.concat(response.attributes());
                        oncoprint = Oncoprint(document.getElementById('oncoprint_body'), {
                            geneData: geneDataColl.toJSON(),
                            clinicalData: extraGenes,
                            genes: genes,
                            clinical_attrs: extraAttributes,
                            legend: document.getElementById('oncoprint_legend')
                        },extraTracks);

                        oncoprint.sortBy(sortBy.val(), cases.split(" "));

                        // enable the option to sort by clinical data
                        $(sortBy.add('option[value="clinical"]')[1]).prop('disabled', false);

//                        // sort by genes by default
//                        sortBy.val('genes');

                        toggleControls(true);

                        $('.special_delete').click(function() {
                            var attr = $(this).attr("alt");
                            var indexNum = extraTracks.indexOf(attr);
                            extraTracks.splice(indexNum, 1);
                            extraGenes.splice(indexNum, 1);
                            extraAttributes.splice(indexNum, 1);
                            removeClinicalAttribute();
                            //removeClinicalAttribute($(this).attr("alt"));
                            //resetClinicalAttribute
                        });// enable delete symbol "x" function

                        zoom = reset_zoom();

                        // sync
                        oncoprint.zoom(zoom.slider("value"));
                        oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
                        oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
                        utils.make_mouseover(d3.selectAll('.sample rect'),{linkage:true});        // hack =(

                        //tooltip for the track deletion function
                        $('.special_delete').qtip({
                                    content: {text: 'click here to delete this track!'},
                                    position: {my:'left bottom', at:'top right', viewport: $(window)},
                                    style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                                    show: {event: "mouseover"},
                                    hide: {fixed: true, delay: 100, event: "mouseout"}
                                });
                                
                        $('.special_delete').hover(
                                    function () {
                                    $(this).css('fill', '#0000FF');
                                    $(this).css('font-size', '18px');
                                    },
                                    function () {
                                    $(this).css('fill', '#87CEFA');
                                    $(this).css('font-size', '12px');
                                    });
                    }
                });
            }
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
            utils.make_mouseover(d3.selectAll('.sample rect'),{linkage:true});     // hack =(
//            oncoprint.sortBy(sortBy.val());
        });

        $('#toggle_whitespace').click(function() {
            oncoprint.toggleWhiteSpace();
        });

        $('.oncoprint-diagram-download').click(function() {
            var fileType = $(this).attr("type");
            var params = {
                filetype: fileType,
                filename:"oncoprint." + fileType,
                svgelement: oncoprint.getPdfInput()
            };

            cbio.util.requestDownload("svgtopdf.do", params);
        });
        
        $('.oncoprint-sample-download').click(function() {
            var samples = "Sample order in the Oncoprint is: \n";
            var genesValue = oncoprint.getData();
            for(var i = 0; i< genesValue.length; i++)
            {
                samples= samples + genesValue[i].key+"\n";
            }
            var a=document.createElement('a');
            a.href='data:text/plain;base64,'+btoa(samples);
            a.textContent='download';
            a.download='OncoPrintSamples.txt';
            a.click();
            //a.delete();
        });
        
        cbio.util.autoHideOnMouseLeave($("#oncoprint_whole_body"), $(".oncoprint-diagram-toolbar-buttons"));
    });
});
