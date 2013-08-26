// boilerplate for the "make your own oncoprint page"
//
// Gideon Dresdner July 2013

requirejs(  [   'Oncoprint',    'OncoprintUtils', 'EchoedDataUtils'],
    function(   Oncoprint,      OncoprintUtils, EchoedDataUtils) {

        // This is for the moustache-like templates
        // prevents collisions with JSP tags
        _.templateSettings = {
            interpolate : /\{\{(.+?)\}\}/g
        };

        var oncoprint;
        var oncoprint_el = document.getElementById("oncoprint");
        var $oncoprint_el = $(oncoprint_el);
        function exec(data) {
//            data = EchoedDataUtils.join(data, 'sample', 'gene');

            // set up oncoprint params
            var genes = _.chain(data).map(function(d){ return d.gene; }).uniq().value();
            var params = { geneData: data, genes:genes };
            params.legend =  document.getElementById("oncoprint_legend");

            $oncoprint_el.empty();    // clear out the div each time

            // exec
            oncoprint = Oncoprint(oncoprint_el, params);

            oncoprint.memoSort(genes);

            // remove text: "Copy number alterations are putative."
            $('#oncoprint_legend p').remove();

            // set up the controls
            zoomSetup_once($('#oncoprint_controls #zoom'), oncoprint.zoom);

            var sortBy = $('#oncoprint_controls #sort_by');     // NB hard coded
            sortBy.chosen({width: "240px", disable_search: true });

            // *NB* to be the best of my knowledge,
            // the user-defined case list is going to depend on the cna file
            $('#oncoprint_controls #sort_by').change(function() {
                oncoprint.sortBy(sortBy.val(), cases);
            });

            $('#toggle_unaltered_cases').click(function() {
                oncoprint.toggleUnalteredCases();
                OncoprintUtils.make_mouseover(d3.selectAll('.sample rect'));     // hack =(
//            oncoprint.sortBy(sortBy.val());
            });

            $('#toggle_whitespace').click(function() {
                oncoprint.toggleWhiteSpace();
            });

            // show controls when there's data
            $('#oncoprint_controls').show();

            return false;
        };

        // don't want to setup the zoom slider multiple times
        var zoomSetup_once = _.once(OncoprintUtils.zoomSetup);

        var cases;
        $('#create_oncoprint').click(function() {
            var cnaForm = $('#cna-form');
            var mutationForm = $('#mutation-form');

            var postFile = function(url, formData, callback) {
                $.ajax({
                    url: url,
                    type: 'POST',
                    success: callback,
                    data: formData,
                    //Options to tell jQuery not to process data or worry about content-type.
                    cache: false,
                    contentType: false,
                    processData: false
                });
            };

            postFile('echofile', new FormData(cnaForm[0]), function(cnaResponse) {
                postFile('echofile', new FormData(mutationForm[0]), function(mutationResponse) {

                    var mutationTextAreaString = $('#mutation-file-example').val().trim(),
                        cnaTextAreaString = $('#cna-file-example').val().trim();

                    var rawMutationString = _.isEmpty(mutationResponse) ? mutationTextAreaString : mutationResponse.mutation;
                    mutation_data = EchoedDataUtils.munge_mutation(rawMutationString);

                    var rawCnaString = _.isEmpty(cnaResponse) ? cnaTextAreaString : cnaResponse.cna;
                    cna_data = EchoedDataUtils.munge_cna(rawCnaString);

                    var data = mutation_data.concat(cna_data);

                    cases = EchoedDataUtils.samples(data);

                    var $error_box = $('#error-box');
                    try {
                        exec(data);
                        $error_box.hide();
                    } catch(e) {
                        // catch all errors and console.log them,
                        // make sure that nothing is shown in the oncoprint box
                        console.log("error creating oncoprint ", e);
                        $oncoprint_el.empty();
                        $error_box.show();
                    }
                });
            });
        });

        $('#oncoprint_controls').html($('#custom-controls-template').html()) // populate with template html
            .hide(); // hide until there's data
});
