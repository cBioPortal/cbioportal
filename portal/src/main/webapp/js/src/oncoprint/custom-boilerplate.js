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


        var oncoprint_el = document.getElementById("oncoprint");

        // bind away
        var bindings = function(oncoprint) {
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

            return false;
        };

        $(document).ready(function() {
            $('#create_sample').click(function() {
                var data = EchoedDataUtils.munge_mutation($('#mutation-file-example').val())
                    .concat(
                        EchoedDataUtils.munge_cna($('#cna-file-example').val()))

                data = EchoedDataUtils.join(data);

                // set up oncoprint params
                var genes = _.chain(data).map(function(d){ return d.gene; }).uniq().value();
                var params = { geneData: data, genes:genes };
                params.legend =  document.getElementById("oncoprint_legend");

                $(oncoprint_el).empty();    // clear out the div each time

                // exec
                oncoprint = Oncoprint(oncoprint_el, params);
            });
        });

        // don't want to setup the zoom slider multiple times
        var zoomSetup_once = _.once(OncoprintUtils.zoomSetup);

        var data;
        var oncoprint;
        var cases;
        $('#submit').click(function() {
            var formData = new FormData($('form')[0]);

            $.ajax({
                url: 'echofile',
                type: 'POST',
                xhr: function() {  // custom xhr
                    var myXhr = $.ajaxSettings.xhr();
                    if(myXhr.upload){ // check if upload property exists
                        myXhr.upload.addEventListener('progress', progressHandlingFunction, false); // for handling the progress of the upload
                    }
                    return myXhr;
                },
                //Ajax events
//                beforeSend: beforeSendHandler,
                success: function(res) {
                    // console.log(res);

                    var parsers = {
                        cna: EchoedDataUtils.munge_cna,
                        mutation: EchoedDataUtils.munge_mutation
                    };

                    data = _.chain(res)
                        .map(function(d, type) {
                            return parsers[type](d);
                        })
                        .flatten()
                        .value();

                    data = EchoedDataUtils.join(data, 'sample', 'gene');

                    cases = EchoedDataUtils.samples(data);

                    // set up oncoprint params
                    var genes = _.chain(data).map(function(d){ return d.gene; }).uniq().value();
                    var params = { geneData: data, genes:genes };
                    params.legend =  document.getElementById("oncoprint_legend");

                    $(oncoprint_el).empty();    // clear out the div each time

                    // exec
                    oncoprint = Oncoprint(oncoprint_el, params);

                    oncoprint.memoSort(genes);

                    // remove text: "Copy number alterations are putative."
                    $('#oncoprint_legend p').remove();

                    zoomSetup_once($('#oncoprint_controls #zoom'), oncoprint.zoom);

                    // show controls when there's data
                    $('#oncoprint_controls').show();

                    bindings(oncoprint);

                    return false;
                },
//                error: errorHandler,
                // Form data
                data: formData,
                //Options to tell JQuery not to process data or worry about content-type
                cache: false,
                contentType: false,
                processData: false
            });
        });

        function progressHandlingFunction(e){
            if(e.lengthComputable){
                $('progress').attr({value:e.loaded,max:e.total});
            }
        }

        $('#oncoprint_controls').html($('#custom-controls-template').html()) // populate with template html
            .hide(); // hide until there's data
});
