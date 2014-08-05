// boilerplate for the "make your own oncoprint page"
//
// Gideon Dresdner July 2013

requirejs(  [   'Oncoprint',    'OncoprintUtils', 'EchoedDataUtils', 'InputData'],
    function(   Oncoprint,      OncoprintUtils, EchoedDataUtils, InputData) {

        // This is for the moustache-like templates
        // prevents collisions with JSP tags
        _.templateSettings = {
            interpolate : /\{\{(.+?)\}\}/g
        };

        // don't want to setup the zoom slider multiple times
        var zoomSetup_once = _.once(OncoprintUtils.zoomSetup);

        var oncoprint;
        var cases;
        var oncoprint_el = document.getElementById("oncoprint");
        var $oncoprint_el = $(oncoprint_el);
        function exec(data) {

            var data_thresholded = (function() {
                var cna_threshold_mapping = {
                    "AMP": "AMPLIFIED",
                    "GAIN": "GAINED",
                    "DIPLOID": "DIPLOID",
                    "HETLOSS": "HEMIZYGOUSLYDELETED",
                    "HOMDEL": "HOMODELETED"
                };

                // maps cna values of GAINED, HEMIZYGOUSLYDELETED to DIPLOID, using the above map,
                // returning a new object with modified cna values
                // *signature:* obj -> obj
                function cna_threshold(d) {
                    if (!d.cna) { return d; }
                    var e = _.clone(d);
                    e.cna = cna_threshold_mapping[e.cna];
                    return e;
                }

                return _.map(data, cna_threshold);
            }());

            // set up oncoprint params
            var genes = _.chain(data_thresholded).map(function(d){ return d.gene; }).uniq().value();
            var params = { geneData: data_thresholded, genes:genes };
            params.legend =  document.getElementById("oncoprint_legend");

            function main(params) {
                $oncoprint_el.empty();    // clear out the div each time
                oncoprint = Oncoprint(oncoprint_el, params);
                oncoprint.memoSort(genes);
            }

            main(params);

            // remove text: "Copy number alterations are putative."
            $('#oncoprint_legend p').remove();

            // set up the controls
            var zoom = zoomSetup_once($('#oncoprint_controls #zoom'), oncoprint.zoom);

//            var sortBy = $('#oncoprint_controls #sort_by');     // NB hard coded
//            sortBy.chosen({width: "240px", disable_search: true });

//            // *NB* to be the best of my knowledge,
//            // the user-defined case list is going to depend on the cna file
//            $('#oncoprint_controls #sort_by').change(function() {
//                oncoprint.sortBy(sortBy.val(), cases);
//            });

            $('#toggle_unaltered_cases').click(function() {
                oncoprint.toggleUnalteredCases();
                OncoprintUtils.make_mouseover(d3.selectAll('.sample rect'), {linkage:false});     // hack =(
//            oncoprint.sortBy(sortBy.val());
            });

            $('#toggle_whitespace').click(function() {
                oncoprint.toggleWhiteSpace();
            });

            // show controls when there's data
            $('#oncoprint_controls').show();

            // setup the download buttons
            $(".oncoprint-download").click(function() {
                                var fileType = $(this).attr("type");
                                var params = {
                                    filetype: fileType,
                                    filename:"oncoprint." + fileType,
                                    svgelement: oncoprint.getPdfInput()
                                };

                                cbio.util.requestDownload("svgtopdf.do", params);
                            });
            $(".sample-download").click(function() {
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
                                a.delete();
                            });

            var $all_cna_levels_checkbox = $('#all_cna_levels');
            function update_oncoprint_cna_levels() {
                var bool = $all_cna_levels_checkbox.is(":checked");
                if (bool) {
                    params.geneData = data;
                    main(params);
                }
                else {
                    params.geneData = data_thresholded;
                    main(params);
                }
            }

            $all_cna_levels_checkbox.click(function() {
                update_oncoprint_cna_levels();
            });

            // sync controls with oncoprint
            update_oncoprint_cna_levels();
            oncoprint.zoom(zoom.slider("value"));
            oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
            oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
//            oncoprint.sortBy(sortBy.val());
            OncoprintUtils.make_mouseover(d3.selectAll('.sample rect'), {linkage:false});        // hack =(

            return false;
        };

        // populate with template html
        $('#oncoprint_controls').html($('#custom-controls-template').html()).hide(); // hide until there's data

        var $cnaForm = $('#cna-form');
        var $mutationForm = $('#mutation-form');
        var $mutation_file_example = $('#mutation-file-example');
        var $cna_file_example = $('#cna-file-example');
        var $filter_example = $('#filter_example');
        
        var oncoprint_loader_img = $('#oncoprint_loader_img');

        // delete text when a file is selected
        $cnaForm.find("#cna").change(function() { $cna_file_example.html(""); });
        $mutationForm.find("#mutation").change(function() { $mutation_file_example.html(""); });

        $('#create_oncoprint').click(function() {
            oncoprint_loader_img.show();
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
            
            var padData = function(data){
                    var datasamples = new Array(); datasamples.push(data[0].sample);
                    var datagene = new Array(); datagene.push(data[0].gene);
                    for(var i = 0; i<data.length;i++)
                    {
                        var samplelength = datasamples.length;
                        var genelength = datagene.length;
                        
                        var samplefalse = false;                         
//                        var samplefound = _.find(datasamples, function(index) { return datasamples[index] === data[i].sample});
//                        if(samplefound) samplefalse = true;
                        for(var j=0;j<samplelength;j++)
                        {

                            if(data[i].sample === datasamples[j])
                            {
                                samplefalse = true;
                                break;
                            }
                        }
                        
                        if(!samplefalse) datasamples.push(data[i].sample);
                        
                        var genefalse = false;
//                        var genefound = _.find(datasamples, function(index) { return datagene[index] === data[i].gene});
//                        if(genefound) genefalse = true;
                        for(var k=0;k<genelength;k++)
                        {
                            if(data[i].gene === datagene[k])
                            {
                                genefalse = true;
                                break;
                            }
                        }
                        
                        if(!genefalse) datagene.push(data[i].gene);
                    }
                    
                    for(var j=0;j<datasamples.length;j++)
                    {
                        for(var i=0; i<datagene.length;i++)
                        {
                            var datafalse = false;
                            for(var n=0; n<data.length;n++)
                            {
                                if(data[n].gene === datagene[i] && data[n].sample === datasamples[j])
                                {
                                    datafalse = true;
                                    break;
                                }
                            }
                            
                            if(!datafalse)
                            {
                                var newdata = new Object();
                                newdata.gene = datagene[i];
                                newdata.sample = datasamples[j];
                                data.push(newdata);
                            }
                        }
                    }    
            };
            
            var  reduceData= function (inputData)
            {
                for( var i = 1; i < inputData.length-1; i++ )
                {
                    for(var j= i +1 ; j < inputData.length; j++)
                    {
                        if(inputData[i].gene === inputData[j].gene && inputData[i].sample === inputData[j].sample)
                        {
                            if(inputData[j].mutation !== undefine )
                            {
                                inputData[i].mutation = inputData[j].mutation;
                                inputData.splice(j,1);
                                break;
                            }
                            
                            if(inputData[j].cna !== undefine )
                            {
                                inputData[i].cna = inputData[j].cna;
                                inputData.splice(j,1);
                                break;
                            }
                        }
                    }
                }
            };
            
            function concatData(mutationdata,data){
                var mutationEmpty = _.isEmpty(mutationdata);
                var dataEmpty = _.isEmpty(data);
                var resultdata;
                
                if(mutationEmpty) 
                {
                    resultdata = data;
                    return resultdata;
                }
                
                padData(mutationdata);
                if(dataEmpty) 
                {
                    resultdata = mutationdata;
                    return resultdata;
                }
                
                for(var i = 0 ; i < data.length; i++)
                {
                    var datafalse = true;
                    var mutationvalue;
                    for(var j=0; j < mutationdata.length; j++)
                    {
                        if(data[i].gene === mutationdata[j].gene && data[i].sample === mutationdata[j].sample)
                        {
                            datafalse = false;
                            mutationvalue = mutationdata[j].mutation;
                            mutationdata.splice(j,1);
                            break;
                        }
                    }
                    
                    if(!datafalse)
                    {
                        data[i].mutation = mutationvalue;
                    }
                }

                return data.concat(mutationdata);
            };
            
            function filterData(filterElement, filterData){
                
                var afterFilter = [];
                
                for(var i=0; i < filterElement.length; i++)
                {
                    for(var j=0; j< filterData.length; j++)
                    {
                        if(filterData[j].gene === filterElement[i])
                        {
                            afterFilter.push(filterData[j]);
                        }
                    }
                }

                return afterFilter;
            } 
 //           postFile('echofile', new FormData($cnaForm[0]), function(cnaResponse) {
                postFile('echofile', new FormData($mutationForm[0]), function(mutationResponse) {

                    var mutationTextAreaString = $mutation_file_example.val().trim();
                    var filterExample = $filter_example.val().trim();
                    
                    var filterString = filterExample.split(/[\s,]+/); 
                    
                    var cnaTextAreaString = "";

                    var rawMutationString = _.isEmpty(mutationResponse) ? mutationTextAreaString : mutationResponse.mutation;
                    //mutation_data = EchoedDataUtils.munge_mutation(rawMutationString);
                    mutation_data = InputData.munge_the_mutation(rawMutationString);
                    
 //                   var rawCnaString = _.isEmpty(cnaResponse) ? cnaTextAreaString : cnaResponse.cna;
                    var rawCnaString = cnaTextAreaString;
                    cna_data = EchoedDataUtils.munge_cna(rawCnaString);
                    
                    var data = concatData(mutation_data,cna_data);

                    if(filterString[0] !== "")
                    {
                        data = filterData(filterString,data);
                    }

                    cases = EchoedDataUtils.samples(data);

                    var $error_box = $('#error-box');
                    try {
                        exec(data);
                        $error_box.hide();
                        oncoprint_loader_img.hide();
                        $('#download_oncoprint').show();
                    } catch(e) {
                        // catch all errors and console.log them,
                        // make sure that nothing is shown in the oncoprint box
                        console.log("error creating oncoprint ", e);
                        $oncoprint_el.empty();
                        $error_box.show();
                    }
                });
//            });
        });
});
