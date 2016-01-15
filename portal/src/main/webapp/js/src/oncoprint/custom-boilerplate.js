/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

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
        var mutationColorControl = 'multiColor';
        var sortStatus = new Array();
        var ClinicalAttributes = new Array();
        var genesDatas = new Array();
        var genesAttributes = new Array();
        var extraTracks = new Array();
        var Attributes = new Array();
        var gapSpaceGeneClinic = 10;

    
        var reset_zoom = function() {
            
            var $new_zoomer_el = $('#oncoprinter-diagram-toolbar-buttons #oncoprint_diagram_slider_icon');
            $new_zoomer_el.empty();
            zoom = OncoprintUtils.zoomSetup($new_zoomer_el, oncoprint.zoom);

            $('#oncoprint_zoom_slider').qtip({
                content: {text: 'zoom in and out oncoprint'},
                position: {my:'bottom middle', at:'top middle', viewport: $(window)},
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
    
//        //delete clinicalAttribute added before
//        var removeClinicalAttribute = function()
//        {
//
//            oncoprint = Oncoprint(document.getElementById('oncoprint_body'), {
//                geneData: geneDataColl.toJSON(),
//                clinicalData: extraGenes,
//                genes: genes,
//                clinical_attrs: extraAttributes,
//                legend: document.getElementById('oncoprint_legend'),
//                sortStatus:sortStatus,
//                mutationColor:mutationColorControl
//            },extraTracks);
//
//            oncoprint.sortBy(sortBy.val(), cases.split(" "));
//
//            totalAttrs = recordAttrs.slice(0);
//
//            for(var n = 0; n < extraAttributes.length; n++)
//            {
//                for(var m=0; m<totalAttrs.length; m++) 
//                {
//                    if(totalAttrs[m].display_name === extraAttributes[n].display_name)
//                    {
//                        totalAttrs.splice(m,1);
//                    }
//                }  
//            }
//
//            oncoprint.sortBy(sortBy.val(), cases.split(" "));
//
//            zoom = reset_zoom();
//            functionFunctions();
//        }
    
        function exec(data, clinic, clinicAttributes,tracks) {
            InitDragDrop();
            var data_thresholded = (function() {
                var cna_threshold_mapping = {
                    "AMP": "AMPLIFIED",
                    "GAIN": "GAINED",
                    "DIPLOID": "DIPLOID",
                    "HETLOSS": "HEMIZYGOUSLYDELETED",
                    "HOMDEL": "HOMODELETED"
                };
                
                var mrna_threshold_mapping = {
                    "UP": "UPREGULATED",
                    "DOWN": "DOWNREGULATED"
                };
                
                var rppa_threshold_mapping = {
                    "PROT-UP": "UPREGULATED",
                    "PROT-DOWN": "DOWNREGULATED"
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
                
                function mrna_threshold(d) {
                    if (!d.mrna) { return d; }
                    var e = _.clone(d);
                    e.mrna = mrna_threshold_mapping[e.mrna];

                    return e;
                }
                
                function rppa_threshold(d) {
                    if (!d.rppa) { return d; }
                    var e = _.clone(d);
                    e.rppa = rppa_threshold_mapping[e.rppa];

                    return e;
                }
                var aftercna = _.map(data, cna_threshold);
                var aftermrnd = _.map(aftercna, mrna_threshold);
                var result = _.map(aftermrnd, rppa_threshold);
                return result;
            }());

            // set up oncoprint params
            var genes = _.chain(data_thresholded).map(function(d){ return d.gene; }).uniq().value();

            genesDatas = data_thresholded;
            genesAttributes = genes;
            var params = { geneData: data_thresholded, genes:genesAttributes };
            params.legend =  document.getElementById("oncoprint_legend");
            
            ClinicalAttributes = clinic;
            Attributes = clinicAttributes;
//            params.clinicalData = clinic;
//            params.clinical_attrs = clinicAttributes;
            params.clinicalData = ClinicalAttributes;
            params.clinical_attrs = Attributes;
            
            for(var i=0; i<clinicAttributes.length; i++)
            {
                sortStatus.push('increSort');
            }
            params.sortStatus = sortStatus;
            
            function main(params,sortbyfirst) {
                extraTracks = tracks;
                $oncoprint_el.empty();    // clear out the div each time
                oncoprint = Oncoprint(oncoprint_el, params,extraTracks);
//                oncoprint.memoSort(genes);
                if(sortbyfirst === undefined)
                {
                    oncoprint.sortBy('genes','','',[],params.sortStatus);
                }
                else
                {
                    oncoprint.sortBy(sortbyfirst,'','',[],params.sortStatus);
                }
                
                //sort button
                $(".oncoprint_Sort_Button").qtip({
                        content: {text: 'Click to sort '},
                        position: {my:'bottom middle', at:'top middle', viewport: $(window)},
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                        show: {event: "mouseover"},
                        hide: {fixed: true, delay: 100, event: "mouseout"}
                    });
                $('.oncoprint_Sort_Button').hover(
                    function () {
                    $(this).css('fill', '#0000FF');
                    $(this).css('font-size', '18px');
                    $(this).css('cursor', 'pointer');
                    },
                    function () {
                    $(this).css('fill', '#87CEFA');
                    $(this).css('font-size', '12px');
                    });
                $('.oncoprint_Sort_Button').click(function() {
                    var sortButtonYValue = $(this)[0].attributes.y.value;
                    var indexSortButton=parseInt(sortButtonYValue/29);
                    if($(this)[0].attributes.href.value==="images/increaseSort.svg")
                    {
                        sortStatus[indexSortButton] = 'nonSort';
                    }
                    else if($(this)[0].attributes.href.value==="images/nonSort.svg")
                    {
                        sortStatus[indexSortButton] ='increSort';
                    }
                    
                    params.sortStatus = sortStatus;

                    if($('.oncoprinter_sortfirst_icon')[0].attributes.src.value === 'images/cool.svg')
                    {
                        main(params,'genes');
                    }
                    else
                    {
                        main(params,'clinical');
                    }

                    oncoprint.zoom(zoom.val());
                    oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
                    oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
                    OncoprintUtils.make_mouseover(d3.selectAll('.sample rect'),{linkage:false});        // hack =(
                });
                
                
                $('.special_delete').click(function() {
                var attr = $(this).attr("alt");
                var indexNum = extraTracks.indexOf(attr);
                var sampleNumbers = ClinicalAttributes.length/Attributes.length;
                extraTracks.splice(indexNum, 1);
                ClinicalAttributes.splice(indexNum*sampleNumbers, sampleNumbers);
                Attributes.splice(indexNum, 1);
                sortStatus.splice(indexNum, 1);
                               
                params.sortStatus = sortStatus;
                if($('.oncoprinter_sortfirst_icon')[0].attributes.src.value === 'images/cool.svg')
                {
                    main(params,'genes');
                }
                else
                {
                    main(params,'clinical');
                }
                oncoprint.zoom(zoom.val());
                oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
                oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
                OncoprintUtils.make_mouseover(d3.selectAll('.sample rect'),{linkage:false});        // hack =(
            });// enable delete symbol "x" function

            //tooltip for the track deletion function
            $('.special_delete').qtip({
                        content: {text: 'click here to delete this track!'},
                        position: {my:'bottom middle', at:'top right', viewport: $(window)},
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                        show: {event: "mouseover"},
                        hide: {fixed: true, delay: 100, event: "mouseout"}
                        });
            $('.special_delete').hover(
                        function () {
                        $(this).css('fill', '#0000FF');
                        $(this).css('font-size', '18px');
                        $(this).css('cursor', 'pointer');
                        },
                        function () {
                        $(this).css('fill', '#87CEFA');
                        $(this).css('font-size', '12px');
                        });
                        
//            $('.oncoprinter_zoomout').click(function() {
//                var tempValue = $('.oncoprint_zoom_slider')[0].value - 0.05;
//                $('.oncoprint_zoom_slider')[0].value = tempValue < 1.0 ? tempValue : 0.1;
//            });
//            
//            $('.oncoprinter_zoomin').click(function() {
//                var tempValue = $('.oncoprint_zoom_slider')[0].value + 0.05;
//                $('.oncoprint_zoom_slider')[0].value = tempValue > 1.0 ? tempValue : 1.0;
//            });
            
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
//                position: {my:'bottom middle', at:'top right', viewport: $(window)},
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

            
            
//
//            $('.special_delete').click(function() {
//                var attr = $(this).attr("alt");
//                var indexNum = extraTracks.indexOf(attr);
//                var sampleNumbers = extraGenes.length/extraAttributes.length;
//                extraTracks.splice(indexNum, 1);
//                extraGenes.splice(indexNum*sampleNumbers, sampleNumbers);
//                extraAttributes.splice(indexNum, 1);
//                sortStatus.splice(indexNum, 1);
//                removeClinicalAttribute();
//            });// enable delete symbol "x" function
        
            $('#oncoprinter_diagram_sortby_group' ).on( 'click', '.dropdown-menu li', function( event ) {
                var $target = $( event.currentTarget );

                $target.closest( '.btn-group' )
                   .find( '[data-bind="label"]' ).text( $target.text() )
                      .end()
                   .children( '.dropdown-toggle' ).dropdown( 'toggle' );

                return false;

             }); 
             
            $('#oncoprinter_zoomout').click(function() {
                var tempValue = parseFloat($('#oncoprint_zoom_slider')[0].value) - 0.05;
                $('#oncoprint_zoom_slider')[0].value = tempValue > 0.1 ? tempValue : 0.1;
                oncoprint.zoom(zoom.val());
            });
            
            $('#oncoprinter_zoomin').click(function() {
                var tempValue = parseFloat($('#oncoprint_zoom_slider')[0].value) + 0.05;
                $('#oncoprint_zoom_slider')[0].value = tempValue < 1.0 ? tempValue : 1.0;
                oncoprint.zoom(zoom.val());
            });

            $('#oncoprinter-diagram-removeUCases-icon').click(function(){
              if($('#oncoprinter-diagram-removeUCases-icon img')[0].attributes.src.value === 'images/removeUCases.svg')
              {
                oncoprint.toggleUnalteredCases();
                OncoprintUtils.make_mouseover(d3.selectAll('.sample rect'),{linkage:false});     // hack =(
                $('#oncoprinter-diagram-removeUCases-icon img')[0].attributes.src.value = 'images/unremoveUCases.svg';
                oncoprint.zoom(zoom.val());
                zoom = reset_zoom();
                oncoprint.zoom(zoom.val());
                
              }
              else
              {
                oncoprint.toggleUnalteredCases();
                OncoprintUtils.make_mouseover(d3.selectAll('.sample rect'),{linkage:false});     // hack =(
                $('#oncoprinter-diagram-removeUCases-icon img')[0].attributes.src.value = 'images/removeUCases.svg';
                oncoprint.zoom(zoom.val());
                zoom = reset_zoom();
                oncoprint.zoom(zoom.val());
                
              }
            });
            $('#oncoprinter-diagram-removeUCases-icon').hover(
            function () {
            $(this).css('fill', '#0000FF');
            $(this).css('font-size', '18px');
            $(this).css('cursor', 'pointer');
            },
            function () {
            $(this).css('fill', '#87CEFA');
            $(this).css('font-size', '12px');
            });
            $('#oncoprinter-diagram-removeUCases-icon').qtip({
            content: {text: 
                        function(){
                        if($('#oncoprinter-diagram-removeUCases-icon img')[0].attributes.src.value === 'images/removeUCases.svg')
                        {return 'remove unaltered cases';}
                        else
                        {
                            return 'show unaltered cases';
                        }
                    }
                },
            position: {my:'bottom middle', at:'top right', viewport: $(window)},
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
            show: {event: "mouseover"},
            hide: {fixed: true, delay: 100, event: "mouseout"}
            });
            
            $('#oncoprinter-diagram-removeWhitespace-icon').click(function(){
              if($('#oncoprinter-diagram-removeWhitespace-icon img')[0].attributes.src.value === 'images/removeWhitespace.svg')
              {
                  oncoprint.toggleWhiteSpace();
                  $('#oncoprinter-diagram-removeWhitespace-icon img')[0].attributes.src.value = 'images/unremoveWhitespace.svg';
              }
              else
              {
                 oncoprint.toggleWhiteSpace();
                 $('#oncoprinter-diagram-removeWhitespace-icon img')[0].attributes.src.value = 'images/removeWhitespace.svg'; 
              }
              oncoprint.zoom(zoom.val());
            });
            $('#oncoprinter-diagram-removeWhitespace-icon').hover(
            function () {
            $(this).css('fill', '#0000FF');
            $(this).css('font-size', '18px');
            $(this).css('cursor', 'pointer');
            },
            function () {
            $(this).css('fill', '#87CEFA');
            $(this).css('font-size', '12px');
            });
            $('#oncoprinter-diagram-removeWhitespace-icon').qtip({
            content: {text: 
                        function(){
                        if($('#oncoprinter-diagram-removeWhitespace-icon img')[0].attributes.src.value === 'images/removeWhitespace.svg')
                        {return 'remove whitespace';}
                        else
                        {
                            return 'show whitespace between cases';
                        }
                    }
            },
            position: {my:'bottom middle', at:'top right', viewport: $(window)},
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
            show: {event: "mouseover"},
            hide: {fixed: true, delay: 100, event: "mouseout"}
            }); 

//            // setup the download buttons
//            $(".oncoprint-download").click(function() {
//                                var fileType = $(this).attr("type");
//                                var params = {
//                                    filetype: fileType,
//                                    filename:"oncoprint." + fileType,
//                                    svgelement: oncoprint.getPdfInput()
//                                };
//
//                                cbio.util.requestDownload("svgtopdf.do", params);
//                            });
//            
//            $(".sample-download").click(function() {
//                                var samples = "Sample order in the Oncoprint is: \n";
//                                var genesValue = oncoprint.getData();
//                                for(var i = 0; i< genesValue.length; i++)
//                                {
//                                    samples= samples + genesValue[i].key+"\n";
//                                }
//                                var a=document.createElement('a');
//                                a.href='data:text/plain;base64,'+btoa(samples);
//                                a.textContent='download';
//                                a.download='OncoPrintSamples.txt';
//                                a.click();
//                                a.delete();
//                            });
                            
            $('.oncoprinter-diagram-downloads-icon').qtip({
                //id: "#oncoprint-diagram-downloads-icon-qtip",
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite'  },
                show: {event: "mouseover"},
                hide: {fixed:true, delay: 100, event: "mouseout"},
                position: {my:'top center',at:'bottom center', viewport: $(window)},
                content: {
                    text:   "<button class='oncoprint-download' type='pdf' style='cursor:pointer;width:80px;'>PDF</button> <br/>"+
                            "<button class='oncoprint-download' type='svg' style='cursor:pointer;width:80px;'>SVG</button> <br/>"+
                            "<button class='sample-download'  type='txt' style='cursor:pointer;width:80px;'>Samples</button>"
                },
                events:{
                    render:function(event){     
                            $('.oncoprint-download').click(function() {
//                            var fileType = $(this).attr("type");
//                            var params = {
//                                filetype: fileType,
//                                filename:"oncoprint." + fileType,
//                                svgelement: oncoprint.getPdfInput()
//                            };
//
//                            cbio.util.requestDownload("svgtopdf.do", params);

                            var fileType = $(this).attr("type");
                            if(fileType === 'pdf')
                            {
                               var downloadOptions = {
                                    filename: "oncoprint.pdf",
                                    contentType: "application/pdf",
                                    servletName: "svgtopdf.do"
                                    };

                                cbio.download.initDownload(oncoprint.getPdfInput(), downloadOptions); 
                            }
                            else if(fileType === 'svg')
                            {
                                cbio.download.initDownload(oncoprint.getPdfInput(), {filename: "oncoprint.svg"});
                            }
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
            $('#oncoprinter-diagram-showlegend-icon').click(function(){
              if($('#oncoprinter-diagram-showlegend-icon img')[0].attributes.src.value === 'images/showlegend.svg')
              {
                $("#oncoprint_legend .mutation_legend_table").css("display","inline");
                $('#oncoprinter-diagram-showlegend-icon img')[0].attributes.src.value = 'images/hidelegend.svg';
              }
              else
              {
                $("#oncoprint_legend .mutation_legend_table").css("display","none");
                $('#oncoprinter-diagram-showlegend-icon img')[0].attributes.src.value = 'images/showlegend.svg'; 
              }
            });
            $('#oncoprinter-diagram-showlegend-icon').hover(
            function () {
            $(this).css('fill', '#0000FF');
            $(this).css('font-size', '18px');
            $(this).css('cursor', 'pointer');
            },
            function () {
            $(this).css('fill', '#87CEFA');
            $(this).css('font-size', '12px');
            });
            $('#oncoprinter-diagram-showlegend-icon').qtip({
            content: {text:function(){
                        if($('#oncoprinter-diagram-showlegend-icon img')[0].attributes.src.value === 'images/showlegend.svg')
                        {return 'show legends';}
                        else
                        {
                            return 'hide legends';
                        }
                    }
            },
            position: {my:'bottom middle', at:'top right', viewport: $(window)},
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
            show: {event: "mouseover"},
            hide: {fixed: true, delay: 100, event: "mouseout"}
            });
            
            //sort by first button
            $('#genesfirst').click(function(){
                main(params,'genes');
                oncoprint.zoom(zoom.val());
                oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
                oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
                OncoprintUtils.make_mouseover(d3.selectAll('.sample rect'),{linkage:false});        // hack =(
//                $(this)[0].attributes.src.value = 'images/cool.svg'; 
            });
            $('#clinicalfirst').click(function(){
                main(params,'clinical');
                oncoprint.zoom(zoom.val());
                oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
                oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
                OncoprintUtils.make_mouseover(d3.selectAll('.sample rect'),{linkage:false});        // hack =(
//                $(this)[0].attributes.src.value = 'images/cool2.svg'; 
            });
            
//            $('.oncoprinter_sortfirst_icon').click(function(){
//              if($(this)[0].attributes.src.value === 'images/cool.svg')
//              {
//                main(params,'clinical');
//                oncoprint.zoom(zoom.val());
//                oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
//                oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
//                OncoprintUtils.make_mouseover(d3.selectAll('.sample rect'),{linkage:false});        // hack =(
//                $(this)[0].attributes.src.value = 'images/cool2.svg';
//              }
//                else if($(this)[0].attributes.src.value === 'images/cool2.svg')
//              {
//                main(params,'genes');
//                oncoprint.zoom(zoom.val());
//                oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
//                oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
//                OncoprintUtils.make_mouseover(d3.selectAll('.sample rect'),{linkage:false});        // hack =(
//                $(this)[0].attributes.src.value = 'images/cool.svg'; 
//              }
//            });
//            $('.oncoprinter_sortfirst_icon').hover(
//            function () {
//            $(this).css('fill', '#0000FF');
//            $(this).css('font-size', '18px');
//            $(this).css('cursor', 'pointer');
//            },
//            function () {
//            $(this).css('fill', '#87CEFA');
//            $(this).css('font-size', '12px');
//            });
//            $('.oncoprinter_sortfirst_icon').qtip({
//            content: {text: 
//                        function(){
//                        if($(this)[0].attributes.src.value === 'images/cool.svg')
//                        {return 'sort by genes first now';}
//                        else
//                        {
//                            return 'sort by clinical first now';
//                        }
//                    }
//            },
//            position: {my:'bottom middle', at:'top right', viewport: $(window)},
//            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
//            show: {event: "mouseover"},
//            hide: {fixed: true, delay: 100, event: "mouseout"}
//            });
            
            var selectsortby = function()
            {
                if($('#oncoprinter_sortbyfirst_dropdonw span')[0].innerHTML === 'Genes first' || $('#oncoprinter_sortbyfirst_dropdonw span')[0].innerHTML === 'Sort by')
                {
                    main(params,'genes');
                }
                else if($('#oncoprinter_sortbyfirst_dropdonw span')[0].innerHTML === 'Clinical first')
                {
                    main(params,'clinical');
                }
                else if($('#oncoprinter_sortbyfirst_dropdonw span')[0].innerHTML === 'alphabetically by case id')
                {
                    main(params,'alphabetical');
                }
                else
                {
                    main(params,'custom');
                }
            };
            
            //color different mutation with different color
            $('#oncoprinter_diagram_showmutationcolor_icon').click(function(){
              if($('#oncoprinter_diagram_showmutationcolor_icon img')[0].attributes.src.value === 'images/uncolormutations.svg')
              {
                mutationColorControl = 'singleColor';
                params.mutationColor = mutationColorControl;
                selectsortby();
//                refreshOncoPrint();
//                main(params);
//                zoom = reset_zoom();
//                // sync
                oncoprint.zoom(zoom.val());
                oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
                oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
                OncoprintUtils.make_mouseover(d3.selectAll('.sample rect'),{linkage:false});        // hack =(
                $('#oncoprinter_diagram_showmutationcolor_icon img')[0].attributes.src.value = 'images/colormutations.svg';
                
                $('#oncoprinter-diagram-showlegend-icon img')[0].attributes.src.value = 'images/showlegend.svg';
                $('#oncoprinter-diagram-removeUCases-icon img')[0].attributes.src.value = 'images/removeUCases.svg';
                $('#oncoprinter-diagram-removeWhitespace-icon img')[0].attributes.src.value = 'images/removeWhitespace.svg';
                
                $('.legend_missense_name').text("Mutation") ;
              }
                else if($('#oncoprinter_diagram_showmutationcolor_icon img')[0].attributes.src.value === 'images/colormutations.svg')
              {
                mutationColorControl = 'multiColor';
                params.mutationColor = mutationColorControl;
                selectsortby();
//                refreshOncoPrint();
//                main(params);
//                zoom = reset_zoom();
//                // sync
                oncoprint.zoom(zoom.val());
                oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
                oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
                OncoprintUtils.make_mouseover(d3.selectAll('.sample rect'),{linkage:false});        // hack =(
                $('#oncoprinter_diagram_showmutationcolor_icon img')[0].attributes.src.value = 'images/uncolormutations.svg'; 
                
                $('#oncoprinter-diagram-showlegend-icon img')[0].attributes.src.value = 'images/showlegend.svg';
                $('#oncoprinter-diagram-removeUCases-icon img')[0].attributes.src.value = 'images/removeUCases.svg';
                $('#oncoprinter-diagram-removeWhitespace-icon img')[0].attributes.src.value = 'images/removeWhitespace.svg';
                
                $('.legend_missense_name').text("Missense Mutation");
                $('.legend_nonmissense').css("display","inline");
              }
            });
            $('#oncoprinter_diagram_showmutationcolor_icon').hover(
            function () {
            $(this).css('fill', '#0000FF');
            $(this).css('font-size', '18px');
            $(this).css('cursor', 'pointer');
            },
            function () {
            $(this).css('fill', '#87CEFA');
            $(this).css('font-size', '12px');
            });
            $('#oncoprinter_diagram_showmutationcolor_icon').qtip({
            content: {text: 
                        function(){
                        if($('#oncoprinter_diagram_showmutationcolor_icon img')[0].attributes.src.value === 'images/uncolormutations.svg')
                        {return 'show all mutations in the same color';}
                        else
                        {
                            return 'color-code different mutation types';
                        }
                    }
            },
            position: {my:'bottom middle', at:'top right', viewport: $(window)},
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
            
            
            //mouse select and move genes and clinical attributes functions
            var _startX = 0;            // mouse starting positions
            var _startY = 0;
            var _endX=0;                // mouse ending positions
            var _endY=0;
            var _offsetX = 0;           // current element offset
            var _offsetY = 0;
            var _dragElement;           // needs to be passed from OnMouseDown to OnMouseMove
            var _dragElementIndex;      //index of the selected title
            var spaceHeight = 0;
            var selectedNotMutation= false;

            function ExtractNumber(value)
            {
                var n = parseInt(value);

                return n == null || isNaN(n) ? 0 : n;
            }

            function refreshOncoPrint()
            {
                //var params = { geneData: genesDatas, genes:genesAttributes };
                //params.legend =  document.getElementById("oncoprint_legend");
                params.genes = genesAttributes;
                params.clinicalData = ClinicalAttributes;
                params.clinical_attrs = Attributes;
//                if($('.oncoprinter_sortfirst_icon')[0].attributes.src.value === 'images/cool.svg')
                if($('#oncoprinter_sortbyfirst_dropdonw span')[0].attributes.src.value === 'Clinical first')
                {
                    main(params,'clinical');
                }
                else
                {
                    main(params,'genes');
                }
            }

            function calculateGeneMovement(yMovement)
            {
                var tem = genesAttributes[yMovement];
                genesAttributes[yMovement] = genesAttributes[_dragElementIndex];
                genesAttributes[_dragElementIndex]=tem;
                refreshOncoPrint();
                zoom = reset_zoom();
                // sync
                oncoprint.zoom(zoom.val());
                oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
                oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
                OncoprintUtils.make_mouseover(d3.selectAll('.sample rect'),{linkage:false});
            }

            function calculateClinicMovement(yMovement)
            {
                var sizeOfSamples = ClinicalAttributes.length/Attributes.length;//calculate length of samples

                //shift clinical attrs samples
                for(var i=0; i<sizeOfSamples; i++)
                {
                    var temClinic = ClinicalAttributes[yMovement*sizeOfSamples+i];
                    ClinicalAttributes[yMovement*sizeOfSamples+i]=ClinicalAttributes[_dragElementIndex*sizeOfSamples+i];
                    ClinicalAttributes[_dragElementIndex*sizeOfSamples+i] = temClinic;
                }

                //shift clinical attrs names
                var tempClinicAttribute = extraTracks[yMovement];
                extraTracks[yMovement]=extraTracks[_dragElementIndex];
                extraTracks[_dragElementIndex] = tempClinicAttribute;

                var tempClinicAttrs = Attributes[yMovement];
                Attributes[yMovement]=Attributes[_dragElementIndex];
                Attributes[_dragElementIndex] = tempClinicAttrs;

                var tempSortStatus = sortStatus[yMovement];
                sortStatus[yMovement]=sortStatus[_dragElementIndex];
                sortStatus[_dragElementIndex] = tempSortStatus;

                refreshOncoPrint();
                zoom = reset_zoom();
                // sync
                oncoprint.zoom(zoom.val());
                oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
                oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
                OncoprintUtils.make_mouseover(d3.selectAll('.sample rect'),{linkage:false});
            }

            function OnMouseDown(e)
            {
                // IE is retarded and doesn't pass the event object
                if (e == null) 
                    e = window.event; 

                // grab the mouse position
                _startX = e.clientX;
                _startY = e.clientY;

                // IE uses srcElement, others use target
                var target = e.target != null ? e.target : e.srcElement;

                //alert('mouse position is:X '+_startX+'Y '+_startY);
                //
                // for IE, left click == 1
                // for Firefox, left click == 0
                if ((e.button == 1 && window.event != null || e.button == 0)&& target.className.animVal==="attribute_name")
                {        
                    target.attributes.fill.value = "red";

                    // grab the clicked element's position
                    _offsetX = ExtractNumber(target.parentElement.attributes.x.value);
                    _offsetY = ExtractNumber(target.parentElement.attributes.y.value);

        //            selectedNotMutation = false;
        //            _dragElementIndex=undefined;

                    for(m in genesAttributes) 
                    {
                        if(genesAttributes[m] === target.textContent)
                        {
                            _dragElementIndex = parseInt(m); 
                            break;
                        }
                    }

                    if(_dragElementIndex === undefined)
                    {
                        selectedNotMutation = true;
                        for(n in Attributes)
                        {
                            if(Attributes[n].display_name === target.textContent)
                            {
                                _dragElementIndex = parseInt(n);
                                break;
                            }
                        }
                    }

                    spaceHeight=(ExtractNumber(target.parentElement.parentElement.children[2].attributes.y.value)-ExtractNumber(target.parentElement.parentElement.children[0].attributes.y.value))/2; //get the height of each table row

                    // bring the clicked element to the front while it is being dragged
                    _oldZIndex = target.style.zIndex;
                    target.style.zIndex = 10000;

                    // we need to access the element in OnMouseMove
                    _dragElement = target;

                    // tell our code to start moving the element with the mouse
                    document.onmousemove = OnMouseMove;

                    // cancel out any text selections
                    document.body.focus();

                    // prevent text selection in IE
                    document.onselectstart = function () { return false; };
                    // prevent IE from trying to drag an image
                    target.ondragstart = function() { return false; };

                    // prevent text selection (except IE)
                    return false;
                }
            }

            function OnMouseUp(e)
            {
                $('.attribute_name').attr('fill','black');

                var yPosition=_offsetY + e.clientY - _startY;

                if(selectedNotMutation)
                {
                    if(yPosition > (Attributes.length*spaceHeight - 7))
                    {
                        yPosition = Attributes.length*spaceHeight - 7;
                    }
                    else if(yPosition<10)
                    {
                        yPosition = 10;
                    }
                }
                else
                {

                    if(Attributes.length>0)
                    {
                        if(yPosition > (Attributes.length*spaceHeight - gapSpaceGeneClinic + genesAttributes.length*spaceHeight - 7))
                        {
                            yPosition = Attributes.length*spaceHeight - gapSpaceGeneClinic + genesAttributes.length*spaceHeight - 7;
                        }
                        else if(yPosition<(Attributes.length*spaceHeight - gapSpaceGeneClinic +10))
                        {
                            yPosition = Attributes.length*spaceHeight - gapSpaceGeneClinic + 10;
                        }
                    }
                    else
                    {
                        if(yPosition > (Attributes.length*spaceHeight + genesAttributes.length*spaceHeight - 7))
                        {
                            yPosition = Attributes.length*spaceHeight + genesAttributes.length*spaceHeight - 7;
                        }
                        else if(yPosition<(Attributes.length*spaceHeight+10))
                        {
                            yPosition = Attributes.length*spaceHeight + 10;
                        }
                    }
                }

                var indexValue;

                if(selectedNotMutation)
                {
                   indexValue = parseInt(yPosition/spaceHeight); 
                }
                else
                {
                    if(Attributes.length>0)
                    {
                        indexValue = parseInt((yPosition-Attributes.length * spaceHeight - gapSpaceGeneClinic)/spaceHeight);
                    }
                    else
                    {
                        indexValue = parseInt((yPosition-Attributes.length * spaceHeight)/spaceHeight);
                    }
                }

                if(indexValue != _dragElementIndex && !isNaN(indexValue))
                {
                    if(selectedNotMutation)
                    {
                        calculateClinicMovement(indexValue);
                    }
                    else
                    {
                        calculateGeneMovement(indexValue);
                    }
                }
                else
                {
                    if(_dragElement!=undefined)
                    {
                        _dragElement.parentElement.attributes.y.value=_offsetY.toString();
                    }
                }

                if (_dragElement != null)
                {
                    _dragElement.style.zIndex = _oldZIndex;

                    // we're done with these events until the next OnMouseDown
                    document.onmousemove = null;
                    document.onselectstart = null;
                    _dragElement.ondragstart = null;

                    // this is how we know we're not dragging      
                    _dragElement = null;

                    _startX = 0;            // mouse starting positions
                    _startY = 0;
                    _endX=0;                // mouse ending positions
                    _endY=0;
                    _offsetX = 0;           // current element offset
                    _offsetY = 0;
                    _dragElement=undefined;           // needs to be passed from OnMouseDown to OnMouseMove
                    _dragElementIndex=undefined;      //index of the selected title
                    spaceHeight = 0;
                    selectedNotMutation= false;
                }
            }

            function OnMouseMove(e)
            {
                if (e == null) 
                    var e = window.event; 

                // this is the actual "drag code"
                var yPosition=_offsetY + e.clientY - _startY;

                if(selectedNotMutation)
                {
                    if(yPosition > (Attributes.length*spaceHeight - 7))
                    {
                        yPosition = Attributes.length*spaceHeight - 7;
                    }
                    else if(yPosition<10)
                    {
                        yPosition = 10;
                    }
                }
                else
                {
                    if(Attributes.length>0)
                    {
                        if(yPosition > (Attributes.length*spaceHeight - gapSpaceGeneClinic + genesAttributes.length*spaceHeight - 7))
                        {
                            yPosition = Attributes.length*spaceHeight - gapSpaceGeneClinic + genesAttributes.length*spaceHeight - 7;
                        }
                        else if(yPosition<(Attributes.length*spaceHeight - gapSpaceGeneClinic +10))
                        {
                            yPosition = Attributes.length*spaceHeight - gapSpaceGeneClinic + 10;
                        }
                    }
                    else
                    {
                        if(yPosition > (Attributes.length*spaceHeight + genesAttributes.length*spaceHeight - 7))
                        {
                            yPosition = Attributes.length*spaceHeight + genesAttributes.length*spaceHeight - 7;
                        }
                        else if(yPosition<(Attributes.length*spaceHeight+10))
                        {
                            yPosition = Attributes.length*spaceHeight + 10;
                        }
                    }

                    console.log(yPosition);
                }

                _dragElement.parentElement.attributes.y.value = yPosition.toString(); 
            }

            function InitDragDrop()
            {
                document.onmousedown = OnMouseDown;
                document.onmouseup = OnMouseUp;
            }
            //mouse functions end
            
            
//            cbio.util.autoHideOnMouseLeave($("#oncoprint_table"), $(".oncoprinter-diagram-toolbar-buttons"));
//            cbio.util.autoHideOnMouseLeave($("#oncoprint_controls"), $(".oncoprinter-diagram-toolbar-buttons"));
            
            return false;
        };

        // populate with template html
        $('#oncoprint_controls').html($('#custom-controls-template').html()).hide(); // hide until there's data

        var $cnaForm = $('#cna-form');
        var $mutationForm = $('#mutation-form');
        var $mutation_file_example = $('#mutation-file-example');
        var $clinic_file_example = $('#clinic-file-example');
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
            
            var getClinicAttributes = function(clinicData)
            {
                var clinicAttributes = new Array(); 
//                for(var j = 1; j<clinicData.length; j++)
//                {
//                    if(_.indexOf(clinicAttributes, clinicData[j].attr_id) < 0)
//                    {
//                        clinicAttributes.push(clinicData[j].attr_id);
//                    }
//                }
                clinicAttributes.push(clinicData[0].attr_id);
                var Attributes = new Array(); 
                var clinicAttriObject = {attr_id: clinicData[0].attr_id,
                                        datatype: clinicData[0].datatype,
                                        description: clinicData[0].attr_id,
                                        display_name: clinicData[0].attr_id};
                                    
                Attributes.push(clinicAttriObject);  
                
                for(var j = 1; j<clinicData.length; j++)
                {
                    if(_.indexOf(clinicAttributes, clinicData[j].attr_id) < 0)
                    {
                        var temp = {attr_id: clinicData[j].attr_id,
                                    datatype: clinicData[j].datatype,
                                    description: clinicData[j].attr_id,
                                    display_name: clinicData[j].attr_id};
                        Attributes.push(temp);
                        clinicAttributes.push(clinicData[j].attr_id);
                    }
                }
                
                return Attributes; 
            }
            
            var getTracks = function(clinicData)
            {
                var clinicAttributes = new Array(); 
                clinicAttributes.push(clinicData[0].attr_id);
                
                for(var j = 1; j<clinicData.length; j++)
                {
                    if(_.indexOf(clinicAttributes, clinicData[j].attr_id) < 0)
                    {
                        clinicAttributes.push(clinicData[j].attr_id);
                    }
                }
                
                return clinicAttributes;
            };
            
            var padClinicData = function(clinicData,geneData)
            {
                var clinicAttributes = new Array(); 
                clinicAttributes.push(clinicData[0].attr_id);
                var geneAttributes = new Array(); 
                geneAttributes.push(geneData[0].sample);
                
                var cliniclength = 1;
                var geneLength = 1;
                for(var j = 1; j<clinicData.length; j++)
                {
                    if(_.indexOf(clinicAttributes, clinicData[j].attr_id) < 0)
                    {
                        clinicAttributes.push(clinicData[j].attr_id);
                        cliniclength++;
                    }
                }
                
                for(var j = 1; j<geneData.length; j++)
                {
                    if(_.indexOf(geneAttributes, geneData[j].sample) < 0)
                    {
                        geneAttributes.push(geneData[j].sample);
                        geneLength++;
                    }
                }
                
                var newClinicData = new Array();
                for(var m = 0; m < cliniclength; m++)
                {
                    for(var n = 0; n < geneLength; n++)
                    {
                        var temp = {sample:geneAttributes[n],attr_id:clinicAttributes[m],attr_val:'NA'};
                        newClinicData.push(temp);
                    }
                }
                
                for(var k=0; k < clinicData.length; k++)
                {
                    var indexOfClinic = _.indexOf(geneAttributes,clinicData[k].sample);
                    for(var p = 0; p < cliniclength; p++)
                    {
                        if(newClinicData[p*geneLength + indexOfClinic].attr_id === clinicData[k].attr_id)
                        {
                            newClinicData[p*geneLength + indexOfClinic].attr_val = clinicData[k].attr_val;
                            break;
                        }
                    }
                }
                
                return newClinicData;
                
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
                mutationTextAreaString = mutationTextAreaString.replace(/ /g,'\t');//replace all whitespace with tab
                mutationTextAreaString = mutationTextAreaString.replace(/\t\t/g,'');//remove extra tab
                var clinicTextAreaString = $clinic_file_example.val().trim();
                var filterExample = $filter_example.val().trim();

                var filterString = filterExample.split(/[\s,]+/); 

                var cnaTextAreaString = "";

                var rawMutationString = _.isEmpty(mutationResponse) ? mutationTextAreaString : mutationResponse.mutation;
                var rawClinicString = _.isEmpty(mutationResponse) ? clinicTextAreaString : mutationResponse.mutation;;
                function firstToUpperCase( str ) {
                    return str.substr(0, 1).toUpperCase() + str.substr(1);
                    }
                var titleValues = rawMutationString.slice(0,22).toLowerCase();
                titleValues = titleValues.replace(titleValues[0],titleValues[0].toUpperCase());
                titleValues = titleValues.replace(titleValues[7],titleValues[7].toUpperCase());
                titleValues = titleValues.replace(titleValues.substr(12),firstToUpperCase(titleValues.substr(12)));
                
                rawMutationString = titleValues + rawMutationString.substr(22);
                
                var mutation_data = InputData.munge_the_mutation(rawMutationString);
                var clinic_data = InputData.munge_the_clinic(rawClinicString);
                
//                   var rawCnaString = _.isEmpty(cnaResponse) ? cnaTextAreaString : cnaResponse.cna;
                var rawCnaString = cnaTextAreaString;
                var cna_data = EchoedDataUtils.munge_cna(rawCnaString);

                var data = concatData(mutation_data,cna_data);

                var clinicData = clinic_data.length>0 ? padClinicData(clinic_data,data) : clinic_data;
                var clinicAttributes = clinic_data.length>0 ? getClinicAttributes(clinic_data):[];
                var tracks = clinic_data.length>0 ? getTracks(clinic_data) : [];

                if(filterString[0] !== "")
                {
                    data = filterData(filterString,data);
                }

                cases = EchoedDataUtils.samples(data);

                var $error_box = $('#error-box');
//                try {
                    exec(data,clinicData,clinicAttributes,tracks);
                    $error_box.hide();
                    oncoprint_loader_img.hide();
                    //$('#download_oncoprint').show();
//                } catch(e) {
//                    // catch all errors and console.log them,
//                    // make sure that nothing is shown in the oncoprint box
//                    console.log("error creating oncoprint ", e);
//                    $oncoprint_el.empty();
//                    $error_box.show();
//                }
            });
        });
});
