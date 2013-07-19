// boilerplate for the main portal page
//
// Gideon Dresdner July 2013

requirejs(  [   'Oncoprint',    'OncoprintUtils', 'EchoedDataUtils'],
    function(   Oncoprint,      OncoprintUtils, EchoedDataUtils) {

        // This is for the moustache-like templates
        // prevents collisions with JSP tags
        _.templateSettings = {
            interpolate : /\{\{(.+?)\}\}/g
        };

        // some hacks to get the oncoprint spiffy for this page.
        // Perhaps these should be properly incorporated as options.
        var hacks_on_callback = function() {
            // remove text: "Copy number alterations are putative."
            $('#oncoprint_legend p').remove();
        };

        var data;
        $(':button').click(function(){
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
                    data = res; console.log(res);

                    data = EchoedDataUtils.oncoprint_wash(data);
                    var genes = _.chain(data).map(function(d){ return d.gene; }).uniq().value();
                    var params = { geneData: data, genes:genes };
                    params.legend =  document.getElementById("oncoprint_legend");

                    Oncoprint(document.getElementById("oncoprint"), params);

                    hacks_on_callback();
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
});
