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

//        // don't want to setup the zoom slider multiple times
//        var zoomSetup_once = _.once(OncoprintUtils.zoomSetup);

//        var $new_zoomer_el = $('.oncoprinter-diagram-toolbar-buttons .oncoprint_diagram_slider_icon');
        var zoom;
        
        var oncoprint;
        var cases;
        var oncoprint_el = document.getElementById("oncoprint");
        var $oncoprint_el = $(oncoprint_el);
        var mutationColorControl = 'singleColor';
        

    
        var reset_zoom = function() {
            
            var $new_zoomer_el = $('.oncoprinter-diagram-toolbar-buttons .oncoprint_diagram_slider_icon');
            $new_zoomer_el.empty();
            zoom = OncoprintUtils.zoomSetup($new_zoomer_el, oncoprint.zoom);

            $('#oncoprint_zoom_slider').hover(
            function () {
            $(this).css('fill', '#0000FF');
            $(this).css('font-size', '18px');
            $(this).css('cursor', 'pointer');
            },
            function () {
            $(this).css('fill', '#87CEFA');
            $(this).css('font-size', '12px');
            });
            $('#oncoprint_zoom_slider').qtip({
                content: {text: 'zoom in and out oncoprint'},
                position: {my:'left bottom', at:'top middle', viewport: $(window)},
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
                show: {event: "mouseover"},
                hide: {fixed: true, delay: 100, event: "mouseout"}
            });

            $('#oncoprint_legend div').mouseover(function(){
                        if($(this).width()<$(this).children().width())
                        {
                            $(this)[0].style.overflowX='auto';
                        }
                        else
                        {
                            $(this)[0].style.overflowX='hidden';
                        } 
                    }) 
                    .mouseout(function(){
                        $(this)[0].style.overflowX='hidden';
                    });


            return zoom;
        };
    
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
                var extraTracks=[];
                $oncoprint_el.empty();    // clear out the div each time
                oncoprint = Oncoprint(oncoprint_el, params,extraTracks);
                oncoprint.memoSort(genes);
            }

            main(params);
            
            // show controls when there's data
            $('#oncoprint_controls').show();
            
            zoom = reset_zoom();
            
            // remove text: "Copy number alterations are putative."
//            $('#oncoprint_legend p').remove();
//            $('.attribute_name').hover(
//                function(){
//                $(this).css('cursor','move');
//                },
//                function () {
//                $(this).css('cursor', 'default');
//            }); 
//            $('.attribute_name').qtip({
//                content: {text: 'Click to drag '},
//                position: {my:'left bottom', at:'top right', viewport: $(window)},
//                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
//                show: {event: "mouseover"},
//                hide: {fixed: true, delay: 100, event: "mouseout"}
//            });

            // set up the controls
//            var zoom = zoomSetup_once($('#oncoprint_controls #zoom'), oncoprint.zoom);
            

//            var sortBy = $('#oncoprint_controls #sort_by');     // NB hard coded
//            sortBy.chosen({width: "240px", disable_search: true });

//            // *NB* to be the best of my knowledge,
//            // the user-defined case list is going to depend on the cna file
//            $('#oncoprint_controls #sort_by').change(function() {
//                oncoprint.sortBy(sortBy.val(), cases);
//            });

//            $('#toggle_unaltered_cases').click(function() {
//                oncoprint.toggleUnalteredCases();
//                OncoprintUtils.make_mouseover(d3.selectAll('.sample rect'), {linkage:false});     // hack =(
////            oncoprint.sortBy(sortBy.val());
//            });
//
//            $('#toggle_whitespace').click(function() {
//                oncoprint.toggleWhiteSpace();
//            });



            $('.oncoprinter-diagram-removeUCases-icon').click(function(){
              if($(this)[0].attributes.src.value === 'images/removeUCases.svg')
              {
                oncoprint.toggleUnalteredCases();
                OncoprintUtils.make_mouseover(d3.selectAll('.sample rect'),{linkage:true});     // hack =(
                $(this)[0].attributes.src.value = 'images/unremoveUCases.svg';
                oncoprint.zoom(zoom.val());
                zoom = reset_zoom();
                oncoprint.zoom(zoom.val());
                
              }
              else
              {
                oncoprint.toggleUnalteredCases();
                OncoprintUtils.make_mouseover(d3.selectAll('.sample rect'),{linkage:true});     // hack =(
                $(this)[0].attributes.src.value = 'images/removeUCases.svg';
                oncoprint.zoom(zoom.val());
                zoom = reset_zoom();
                oncoprint.zoom(zoom.val());
                
              }
            });
            $('.oncoprinter-diagram-removeUCases-icon').hover(
            function () {
            $(this).css('fill', '#0000FF');
            $(this).css('font-size', '18px');
            $(this).css('cursor', 'pointer');
            },
            function () {
            $(this).css('fill', '#87CEFA');
            $(this).css('font-size', '12px');
            });
            $('.oncoprinter-diagram-removeUCases-icon').qtip({
            content: {text: 
                        function(){
                        if($(this)[0].attributes.src.value === 'images/removeUCases.svg')
                        {return 'remove unaltered cases';}
                        else
                        {
                            return 'show unaltered cases';
                        }
                    }
                },
            position: {my:'left bottom', at:'top right', viewport: $(window)},
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
            show: {event: "mouseover"},
            hide: {fixed: true, delay: 100, event: "mouseout"}
            });
            
            $('.oncoprinter-diagram-removeWhitespace-icon').click(function(){
              if($(this)[0].attributes.src.value === 'images/removeWhitespace.svg')
              {
                  oncoprint.toggleWhiteSpace();
                  $(this)[0].attributes.src.value = 'images/unremoveWhitespace.svg';
              }
              else
              {
                 oncoprint.toggleWhiteSpace();
                 $(this)[0].attributes.src.value = 'images/removeWhitespace.svg'; 
              }
              oncoprint.zoom(zoom.val());
            });
            $('.oncoprinter-diagram-removeWhitespace-icon').hover(
            function () {
            $(this).css('fill', '#0000FF');
            $(this).css('font-size', '18px');
            $(this).css('cursor', 'pointer');
            },
            function () {
            $(this).css('fill', '#87CEFA');
            $(this).css('font-size', '12px');
            });
            $('.oncoprinter-diagram-removeWhitespace-icon').qtip({
            content: {text: 
                        function(){
                        if($(this)[0].attributes.src.value === 'images/removeWhitespace.svg')
                        {return 'remove whitespace';}
                        else
                        {
                            return 'show whitespace between cases';
                        }
                    }
            },
            position: {my:'left bottom', at:'top right', viewport: $(window)},
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
            show: {event: "mouseover"},
            hide: {fixed: true, delay: 100, event: "mouseout"}
            }); 

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
                            
            $('.oncoprinter-diagram-downloads-icon').qtip({
                //id: "#oncoprint-diagram-downloads-icon-qtip",
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite'  },
                show: {event: "mouseover"},
                hide: {fixed:true, delay: 100, event: "mouseout"},
                position: {my:'top center',at:'bottom center', viewport: $(window)},
                content: {
                    text:   "<button class='oncoprint-download' type='pdf' style='cursor:pointer'>PDF</button>"+
                            "<button class='oncoprint-download' type='svg' style='cursor:pointer'>SVG</button>"+
                            "<button class='sample-download'  type='txt' style='cursor:pointer'>Samples</button>"
                },
                events:{
                    render:function(event){     
                            $('.oncoprint-download').click(function() {
                            var fileType = $(this).attr("type");
                            var params = {
                                filetype: fileType,
                                filename:"oncoprint." + fileType,
                                svgelement: oncoprint.getPdfInput()
                            };

                            cbio.util.requestDownload("svgtopdf.do", params);
                        });

                        $('.sample-download').click(function() {
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
                        });
                    }
                }
            });
            
            //show or hide legends of oncoprint
            $('.oncoprinter-diagram-showlegend-icon').click(function(){
              if($(this)[0].attributes.src.value === 'images/showlegend.svg')
              {
                $("#oncoprint_legend").css("display","inline");
                $(this)[0].attributes.src.value = 'images/hidelegend.svg';
              }
              else
              {
                $("#oncoprint_legend").css("display","none");
                $(this)[0].attributes.src.value = 'images/showlegend.svg'; 
              }
            });
            $('.oncoprinter-diagram-showlegend-icon').hover(
            function () {
            $(this).css('fill', '#0000FF');
            $(this).css('font-size', '18px');
            $(this).css('cursor', 'pointer');
            },
            function () {
            $(this).css('fill', '#87CEFA');
            $(this).css('font-size', '12px');
            });
            $('.oncoprinter-diagram-showlegend-icon').qtip({
            content: {text:function(){
                        if($(this)[0].attributes.src.value === 'images/showlegend.svg')
                        {return 'show legends';}
                        else
                        {
                            return 'hide legends';
                        }
                    }
            },
            position: {my:'left bottom', at:'top right', viewport: $(window)},
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
            show: {event: "mouseover"},
            hide: {fixed: true, delay: 100, event: "mouseout"}
            });
            
            //color different mutation with different color
            $('.oncoprinter_diagram_showmutationcolor_icon').click(function(){
              if($(this)[0].attributes.src.value === 'images/colormutations.svg')
              {
                mutationColorControl = 'singleColor';
                params.mutationColor = mutationColorControl;
//                refreshOncoPrint();
                main(params);
//                zoom = reset_zoom();
//                // sync
                oncoprint.zoom(zoom.val());
                oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
                oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
                OncoprintUtils.make_mouseover(d3.selectAll('.sample rect'),{linkage:true});        // hack =(
                $(this)[0].attributes.src.value = 'images/uncolormutations.svg';
                $('.legend_missense_name').text("mutation") ;
              }
                else if($(this)[0].attributes.src.value === 'images/uncolormutations.svg')
              {
                mutationColorControl = 'multiColor';
                params.mutationColor = mutationColorControl;
//                refreshOncoPrint();
                main(params);
//                zoom = reset_zoom();
//                // sync
                oncoprint.zoom(zoom.val());
                oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
                oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
                OncoprintUtils.make_mouseover(d3.selectAll('.sample rect'),{linkage:true});        // hack =(
                $(this)[0].attributes.src.value = 'images/colormutations.svg'; 
                $('.legend_missense_name').text("missense mutation");
                $('.legend_nonmissense').css("display","inline");
              }
            });
            $('.oncoprinter_diagram_showmutationcolor_icon').hover(
            function () {
            $(this).css('fill', '#0000FF');
            $(this).css('font-size', '18px');
            $(this).css('cursor', 'pointer');
            },
            function () {
            $(this).css('fill', '#87CEFA');
            $(this).css('font-size', '12px');
            });
            $('.oncoprinter_diagram_showmutationcolor_icon').qtip({
            content: {text: 
                        function(){
                        if($(this)[0].attributes.src.value === 'images/uncolormutations.svg')
                        {return 'show all mutations in the same color';}
                        else
                        {
                            return 'color-code different mutation types';
                        }
                    }
            },
            position: {my:'left bottom', at:'top right', viewport: $(window)},
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
            show: {event: "mouseover"},
            hide: {fixed: true, delay: 100, event: "mouseout"}
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
//            oncoprint.zoom(zoom.slider("value"));// old vision modified by dong
            oncoprint.zoom(zoom.val());//new vision added by dong
            oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
            oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
//            oncoprint.sortBy(sortBy.val());
            OncoprintUtils.make_mouseover(d3.selectAll('.sample rect'), {linkage:false});        // hack =(
            
            cbio.util.autoHideOnMouseLeave($("#oncoprint_table"), $(".oncoprinter-diagram-toolbar-buttons"));
            cbio.util.autoHideOnMouseLeave($("#oncoprint_controls"), $(".oncoprinter-diagram-toolbar-buttons"));
            
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

            postFile('echofile', new FormData($mutationForm[0]), function(mutationResponse) {

                var mutationTextAreaString = $mutation_file_example.val().trim();
                var filterExample = $filter_example.val().trim();

                var filterString = filterExample.split(/[\s,]+/); 

                var cnaTextAreaString = "";

                var rawMutationString = _.isEmpty(mutationResponse) ? mutationTextAreaString : mutationResponse.mutation;
                //mutation_data = EchoedDataUtils.munge_mutation(rawMutationString);
                var mutation_data = InputData.munge_the_mutation(rawMutationString);

//                   var rawCnaString = _.isEmpty(cnaResponse) ? cnaTextAreaString : cnaResponse.cna;
                var rawCnaString = cnaTextAreaString;
                var cna_data = EchoedDataUtils.munge_cna(rawCnaString);

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
                    //$('#download_oncoprint').show();
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
