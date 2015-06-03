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

    var showmutationflag = true;
    var showalteredcaseflag = true;
    var showwhitespaceflag = true;
    var increasesortflag = true;
    var showPatients = true;
    // add in controls from template
    document.getElementById('oncoprint_controls').innerHTML
        = _.template(document.getElementById('main-controls-template').innerHTML)();

    var clinicalAttributes = new ClinicalAttributesColl();

    var $zoom_el = $('#oncoprint_controls #zoom');
    var $new_zoom_el = $('#oncoprint_whole_body #oncoprint_diagram_slider_icon');
    var zoom;
    var totalAttrs=[];
    var recordAttrs;//make a record of all attrs
    
    var sort_by_values = {
            "gene data first": "gene",
            "clinical data first": "clinical",
            "alphabetically by case id": "alphabetical",
            "user-defined case list / default": "custom"
        };
    
    var gapSpaceGeneClinic = 10;// Gap between gene data and clinic 
    var mutationColorControl = 'multiColor';
    var mutationColorSort = 'mutationcolorsort';
    
    // basically a hack to prevent the zoom function from a particular oncoprint
    // from getting bound to the UI slider forever
    var reset_zoom = function() {
        //$zoom_el.empty();//got problem here by dong 
        $new_zoom_el.empty();
        zoom = utils.zoomSetup($new_zoom_el, oncoprint.zoom);
        
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
            position: {my:'bottom middle', at:'top middle', viewport: $(window)},
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
            show: {event: "mouseover"},
            hide: {fixed: true, delay: 100, event: "mouseout"}
        });
              
        return zoom;
    };

    clinicalAttributes.fetch({
        type: 'POST',
        data: { cancer_study_id: cancer_study_id_selected,
            case_list: window.PortalGlobals.getCases() },
        success: function(attrs) {
            totalAttrs = attrs.toJSON();
            totalAttrs = _.sortBy(totalAttrs, function(o) { return o.display_name; })
            if(window.PortalGlobals.getMutationProfileId()!==null){
                var tem={attr_id: "# mutations", datatype: "NUMBER",description: "Number of mutation", display_name: "# mutations"};
                totalAttrs.unshift(tem);
            }
            
            if(window.PortalGlobals.getCancerStudyId()!==null){
                var tem={attr_id: "FRACTION_GENOME_ALTERED", datatype: "NUMBER",description: "Fraction Genome Altered", display_name: "Fraction Genome Altered"};
                totalAttrs.unshift(tem);
            }
            
            recordAttrs=totalAttrs.slice(0);// record the original total attributes
            utils.populate_clinical_attr_select(document.getElementById('select_clinical_attributes'), totalAttrs);
            $(select_clinical_attributes_id).chosen({width: "330px", "font-size": "12px", search_contains: true});
        
            $('#select_clinical_attributes_chzn .chzn-search input').click(
                function(e){
                    e.stopPropagation();
                }
            );
        
            $("#select_clinical_attributes_chzn").mouseenter(function() {
                $("#select_clinical_attributes_chzn .chzn-search input").focus();
            });
        }
    });

    var oncoprint;
    
    var extraTracks=[]; // used to record clinical attributes added
    var extraGenes=[]; // used to record genes add customized
    var extraAttributes=[]; // used to record attributes names add customized
    var sortStatus=[];

    var cases = window.PortalGlobals.getCases();
    var genes = window.PortalGlobals.getGeneListString().split(" ");

    var outer_loader_img = $('#oncoprint #outer_loader_img');
    var inner_loader_img = $('#oncoprint #inner_loader_img');

    var geneDataColl = new GeneDataColl();
    
    var selectsortby = function()
    {
        if(sortBy1[0].innerHTML === 'gene data first')
        {
            oncoprint.sortBy("genes", cases.split(" "),mutationColorControl,mutationColorSort,sortStatus);  
        }
        else if(sortBy1[0].innerHTML === 'clinical data first')
        {
            oncoprint.sortBy("clinical", cases.split(" "),mutationColorControl,mutationColorSort,sortStatus); 
        }
        else if(sortBy1[0].innerHTML === 'alphabetically by case id')
        {
            oncoprint.sortBy("alphabetical", cases.split(" "),mutationColorControl,mutationColorSort,sortStatus); 
        }
        else
        {
            oncoprint.sortBy("custom", cases.split(" "),mutationColorControl,mutationColorSort,sortStatus);
        }
    };
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
            //for add clinical attributes in url to data start
            var urlValueNow = window.location.href;//get current page url
            var beginOfDeleteElementNow = urlValueNow.indexOf('clinicallist=');
            
            if(urlValueNow.indexOf("&show_samples=true")>-1)
            {
                showPatients =false;
            }
            
            if(beginOfDeleteElementNow > 0)
            {
//                var clinicallistStringNow = urlValueNow.substring(beginOfDeleteElementNow + ('&clinicallist=').length,urlValueNow.length);
                var clinicallistStringNow = urlValueNow.substring(beginOfDeleteElementNow);
                clinicallistStringNow = (clinicallistStringNow.split("&"))[0];
                clinicallistStringNow = (clinicallistStringNow.split("="))[1];
                var clinicallistArrayNow = clinicallistStringNow.split('+');

                var clinicalAttributeArray = [];
                var clinicalExtraAttributes = [];
                var clinicalSortStatus = [];
                var clinicalExtraTracks = [];
                var gainClinicalData = function(clinicalElements,clinicalElementsArray)
                {
                        if(clinicalElements === "# mutations")
                        {
                                var oncoprintClinicalsMutation = new ClinicalMutationColl();
                                clinicalElementsArray.push(
                                oncoprintClinicalsMutation.fetch({
                                type: "POST",

                                data: {
                                        mutation_profile: window.PortalGlobals.getMutationProfileId(),
                                        cmd: "count_mutations",
                                        case_ids: cases
                                },
                                success: function(response) {
                                    inner_loader_img.hide();

//                                    extraTracks = extraTracks.concat(response.attributes().map(function(attr) { return attr.attr_id; }));
                                    clinicalExtraTracks['mutation'] = response.attributes().map(function(attr) { return attr.attr_id; });
//                                    extraGenes = extraGenes.concat(response.toJSON());
                                    clinicalAttributeArray["mutation"]= response.toJSON();
//                                    extraAttributes=extraAttributes.concat(response.attributes());
                                    clinicalExtraAttributes ["mutation"]= response.attributes();
                                    clinicalSortStatus["mutation"] = 'decreSort';
                                }
                            }));
                        }
                        else if(clinicalElements === "FRACTION_GENOME_ALTERED")
                        {
                            var oncoprintClinicalsCNA = new ClinicalCNAColl();
                            clinicalElementsArray.push(
                            oncoprintClinicalsCNA.fetch({
                            type: "POST",
                            data: {
                                    cancer_study_id:window.PortalGlobals.getCancerStudyId(),
                                    cmd: "get_cna_fraction",
                                    case_ids: cases
                            },
                            success: function(response){
                                        inner_loader_img.hide();

//                                        extraTracks = extraTracks.concat(response.attributes().map(function(attr) { return attr.attr_id; }));
                                        clinicalExtraTracks[clinicalElements] = response.attributes().map(function(attr) { return attr.attr_id; });
//                                        extraGenes = extraGenes.concat(response.toJSON());
                                        clinicalAttributeArray[clinicalElements]= response.toJSON();
//                                        extraAttributes=extraAttributes.concat(response.attributes());
//                                        sortStatus = sortStatus.concat('decreSort');
                                        clinicalExtraAttributes[clinicalElements]= response.attributes();
                                        clinicalSortStatus[clinicalElements] = 'decreSort';
                                    }
                                }));
                        }
                        else
                        {
                            var oncoprintClinicals = new ClinicalColl();
                            clinicalElementsArray.push(
                            oncoprintClinicals.fetch({
                            type: "POST",

                            data: {
                                cancer_study_id: cancer_study_id_selected,
                                attribute_id: clinicalElements,
                                case_list: cases
                            },
                            success: function(response) {
                                inner_loader_img.hide();

//                                extraTracks = extraTracks.concat(response.attributes().map(function(attr) { return attr.attr_id; }));
                                clinicalExtraTracks[clinicalElements] = response.attributes().map(function(attr) { return attr.attr_id; });
//                                extraGenes = extraGenes.concat(response.toJSON());
                                clinicalAttributeArray[clinicalElements]= response.toJSON();
//                                extraAttributes=extraAttributes.concat(response.attributes());
//                                sortStatus = sortStatus.concat('decreSort'); 
                                clinicalExtraAttributes[clinicalElements]= response.attributes();
                                clinicalSortStatus[clinicalElements] = 'decreSort';
                            }
                            }));
                        }
                };

                var clinicalElementsArray = [];
                
                for(var i=0; i < clinicallistArrayNow.length; i++)
                {
                    var clinicalElementsValue;
                    clinicalElementsValue = clinicallistArrayNow[i];
                    if(clinicallistArrayNow[i] === 'mutation')
                    {
                       clinicalElementsValue = '# mutations';   
                    }

                    gainClinicalData(clinicalElementsValue,clinicalElementsArray);
                }

                $.when.apply(null, clinicalElementsArray).done(function() {
                        for(var j=0; j < clinicallistArrayNow.length; j++)
                        {
                            extraGenes = extraGenes.concat(clinicalAttributeArray[clinicallistArrayNow[j]]);
                            extraTracks = extraTracks.concat(clinicalExtraTracks[clinicallistArrayNow[j]]);
                            extraAttributes = extraAttributes.concat(clinicalExtraAttributes[clinicallistArrayNow[j]]);
                            sortStatus = sortStatus.concat(clinicalSortStatus[clinicallistArrayNow[j]]);
                        }
                        oncoprint = Oncoprint(document.getElementById('oncoprint_body'), {
                            geneData: data.toJSON(),
                            clinicalData: extraGenes,
                            genes: genes,
                            clinical_attrs: extraAttributes,
                            legend: document.getElementById('oncoprint_legend'),
                            sortStatus:sortStatus,
                            mutationColor:mutationColorControl
                            },extraTracks,showPatients);

                            outer_loader_img.hide();
                            $('#oncoprint #everything').show();

                            if($('#oncoprint_sortbyfirst_dropdonw span')[0].innerHTML === 'Sort by')
                            {
                                oncoprint.sortBy("genes", cases.split(" "),mutationColorControl,mutationColorSort,sortStatus);
                            }
                            else
                            {
                                selectsortby();
                            }

                            $('.attribute_name').qtip({
                                content: {text: 'hold to drag'},
                                position: {my:'middle right', at:'middle left', viewport: $(window)},
                                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                                show: {event: "mouseover"}
                            });  

                            zoom = reset_zoom();
                            invokeDataManager(); 

                            if(showPatients)
                            {
                                var AlteredPatientsNum= calculatePatientNum(PortalGlobals.getAlteredSampleIdList(),PortalGlobals.getPatientSampleIdMap());
                                var UnalteredPatientsNum= calculatePatientNum(PortalGlobals.getUnalteredSampleIdList(),PortalGlobals.getPatientSampleIdMap());
                                var totalPatientsNum = _.union(AlteredPatientsNum,UnalteredPatientsNum);
                                var percentOfAlteredPatients = Math.ceil((AlteredPatientsNum.length/totalPatientsNum.length * 100).toFixed(1));
                                $('#altered_value').text("Altered in "+ AlteredPatientsNum.length + " ("+ percentOfAlteredPatients +"%) of "+ totalPatientsNum.length +" patients/cases");
                            }
                            else
                            {
                                $('#oncoprint_diagram_topatientid_icon img')[0].attributes.src.value = 'images/cool.svg';
                                $('#switchPatientSample').text("Show patients in OncoPrint"); 
                                $('#switchPatientSample')[0].attributes.valuetype.value = "samples";
                                $('#altered_value').text("Altered in "+ PortalGlobals.getNumOfAlteredCases() + " ("+ Math.ceil(PortalGlobals.getPercentageOfAlteredCases()) +"%) of "+ PortalGlobals.getNumOfTotalCases() + " samples"); 
                            }
                            
                        if($('#oncoprint_sortbyfirst_dropdonw span')[0].innerHTML === 'Sort by')
                        {
                            oncoprint.sortBy("genes", cases.split(" "),mutationColorControl,mutationColorSort,sortStatus);
                        }
                        else
                        {
                            selectsortby();
                        }

                        // enable the option to sort by clinical data
                        $('#oncoprint-diagram-toolbar-buttons #clinical_first')[0].style.display = "inline";

                        for(attributeElemValue in extraAttributes)
                        {
                            var attributeElemValueIndex;
                            
                            for(var m= 0; m<totalAttrs.length;m++) 
                            {
                                if(totalAttrs[m].display_name === extraAttributes[attributeElemValue].display_name)
                                {
                                    attributeElemValueIndex=m; 
                                    totalAttrs.splice(attributeElemValueIndex,1);
                                }
                            }  
                        }

                        utils.populate_clinical_attr_select(document.getElementById('select_clinical_attributes'), totalAttrs);
                        
                        toggleControls(true);
                        
                        functionFunctions();
                        
                        zoom = reset_zoom();
                        
                        // sync
                        $('#oncoprint_diagram_showmorefeatures_icon').qtip({
                        content: {text:'add another clinical attribute track'},
                        position: {my:'bottom middle', at:'top middle', viewport: $(window)},
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
                        show: {event: "mouseover"},
                        hide: {fixed: true, delay: 100, event: "mouseout"}
                        });
                        oncoprint.zoom(zoom.val());
                        oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
                        oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
                        $('#oncoprint-diagram-showlegend-icon img')[0].attributes.src.value = 'images/showlegend.svg';
                        $('#oncoprint-diagram-removeUCases-icon img')[0].attributes.src.value = 'images/removeUCases.svg';
                        $('#oncoprint-diagram-removeWhitespace-icon img')[0].attributes.src.value = 'images/removeWhitespace.svg';
                        utils.make_mouseover(d3.selectAll('.sample rect'),{linkage:true});        // hack =(
                        invokeDataManager();
                    });
            }
            else
            {
                oncoprint = Oncoprint(document.getElementById('oncoprint_body'), {
                    geneData: data.toJSON(),
                    genes: genes,
                    legend: document.getElementById('oncoprint_legend')
                },extraTracks,showPatients);

                outer_loader_img.hide();
                $('#oncoprint #everything').show();

                if($('#oncoprint_sortbyfirst_dropdonw span')[0].innerHTML === 'Sort by')
                {
                    oncoprint.sortBy("genes", cases.split(" "),mutationColorControl,mutationColorSort,sortStatus);
                }
                else
                {
                    selectsortby();
                }

                $('.attribute_name').qtip({
                    content: {text: 'hold to drag'},
                    position: {my:'middle right', at:'middle left', viewport: $(window)},
                    style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                    show: {event: "mouseover"}
                });  

                zoom = reset_zoom();
                invokeDataManager();                   
                if(showPatients)
                {
                    var AlteredPatientsNum= calculatePatientNum(PortalGlobals.getAlteredSampleIdList(),PortalGlobals.getPatientSampleIdMap());
                    var UnalteredPatientsNum= calculatePatientNum(PortalGlobals.getUnalteredSampleIdList(),PortalGlobals.getPatientSampleIdMap());
                    var totalPatientsNum = _.union(AlteredPatientsNum,UnalteredPatientsNum);
                    var percentOfAlteredPatients = Math.ceil((AlteredPatientsNum.length/totalPatientsNum.length * 100).toFixed(1));
                    $('#altered_value').text("Altered in "+ AlteredPatientsNum.length + " ("+ percentOfAlteredPatients +"%) of "+ totalPatientsNum.length +" patients/cases");
                }
                else
                {
                    $('#oncoprint_diagram_topatientid_icon img')[0].attributes.src.value = 'images/cool.svg';
                    $('#switchPatientSample').text("Show patients in OncoPrint"); 
                    $('#switchPatientSample')[0].attributes.valuetype.value = "samples";
                    $('#altered_value').text("Altered in "+ PortalGlobals.getNumOfAlteredCases() + " ("+ Math.ceil(PortalGlobals.getPercentageOfAlteredCases()) +"%) of "+ PortalGlobals.getNumOfTotalCases() + " samples"); 
                }                
            }
        }
    });

    var select_clinical_attributes_id = '#select_clinical_attributes';
    var oncoprintClinicals;
//    var sortBy = $('#oncoprint-diagram-toolbar-buttons #sort_by');
    var sortBy1 = $('#oncoprint-diagram-toolbar-buttons #oncoprint_sortbyfirst_dropdonw span');
//     $('#oncoprint_controls #sort_by').chosen({width: "240px", disable_search: true });

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
//        sortBy.prop('disabled', enable_disable).trigger("liszt:updated");
    };
    
    var selectedTitle;
    //set all the icons status to default value
    var controlIconsStatusReset = function()
    {
    };
    
    var functionFunctions = function()
    {
        $('.special_delete').click(function() {
            var urlValue = window.location.href;//get current page url for remove from url
            var attr = $(this).attr("alt");
            var indexNum = extraTracks.indexOf(attr);
            var sampleNumbers = extraGenes.length/extraAttributes.length;
            extraTracks.splice(indexNum, 1);
            extraGenes.splice(indexNum*sampleNumbers, sampleNumbers);
            
            var recordDeleteElement = extraAttributes[indexNum]; //for delete it in url
            var positionOfDeleteElement = urlValue.indexOf(recordDeleteElement.attr_id); //for delete it in url
            if(recordDeleteElement.attr_id === '# mutation')
            {
                positionOfDeleteElement = urlValue.indexOf('mutation'); //for delete it in url
            }
            
            var beginOfDeleteElement = urlValue.indexOf('clinicallist='); //for delete it in url
            
//            var clinicallistString = urlValue.substring(beginOfDeleteElement+('&clinicallist=').length,urlValue.length);
            var clinicallistString = urlValue.substring(beginOfDeleteElement);
            clinicallistString = (clinicallistString.split("&"))[0];
            var clinicallistArray = (clinicallistString.split("="))[1].split('+');
            if(clinicallistArray.length > 1)
            {
                var DeleteElementAttr_id = recordDeleteElement.attr_id;
                if(DeleteElementAttr_id === '# mutations')
                {
                    DeleteElementAttr_id = 'mutation';
                }
                
                if(DeleteElementAttr_id === clinicallistArray[0])
                {
                    var newUrl = urlValue.replace(DeleteElementAttr_id + '+','');
                    window.history.pushState({"html":window.location.html,"pageTitle":window.location.pageTitle},"", newUrl);
                }
                else
                {
                    var newUrl = urlValue.replace('+'+DeleteElementAttr_id,'');
                    window.history.pushState({"html":window.location.html,"pageTitle":window.location.pageTitle},"", newUrl);
                }
            }
            else
            {
//                var newUrl = urlValue.slice(0,beginOfDeleteElement);
                var newUrl = urlValue.replace("&"+clinicallistString,'');
                window.history.pushState({"html":window.location.html,"pageTitle":window.location.pageTitle},"", newUrl);
            }
            
            extraAttributes.splice(indexNum, 1);
            sortStatus.splice(indexNum, 1);
            removeClinicalAttribute();
        });// enable delete symbol "x" function

        //tooltip for the track deletion function
        $('.special_delete').qtip({
                    content: {text: 'click to remove'},
                    position: {my:'bottom middle', at:'top middle', viewport: $(window)},
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
                    
        $('.attribute_name').qtip({
//                content: {text: 'click to drag '},
                content: {text: function(){
                            if($(this)[0].attributes.attributename.value.length <= 20)
                            {
                                return 'hold to drag';
                            }
                            else
                            {
                                return $(this)[0].attributes.attributename.value + '<br/> hold to drag';
                            }
                        }
                    },
                position: {my:'middle right', at:'middle left', viewport: $(window)},
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow' },
                show: {event: "mouseover"}
            });
            
        $(".oncoprint_Sort_Button").qtip({
                content: {text:
                        function(){
                        if($(this)[0].attributes.href.value === 'images/increaseSort.svg')
                        {
                            return 'Disable sort';
                        }
                        else if($(this)[0].attributes.href.value === 'images/nonSort.svg')
                        {
                            return 'Enable increase sort';
                        }
                        else
                        {
                            return 'Enable decrease sort';
                        }
                    }
                },
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
        
        $('.oncoprint_Sort_Button').click(function() {
            
            var sortButtonYValue = $(this)[0].attributes.y.value;
            var indexSortButton=parseInt(sortButtonYValue/29);
            if($(this)[0].attributes.href.value ==="images/increaseSort.svg")
            {
                sortStatus[indexSortButton] = 'nonSort';
            }
            else if($(this)[0].attributes.href.value ==="images/nonSort.svg")
            {
//                sortStatus[indexSortButton] ='increSort';
                sortStatus[indexSortButton] = 'decreSort'; 
            }
            else if($(this)[0].attributes.href.value ==="images/decreaseSort.svg")
            {
//                sortStatus[indexSortButton] = 'decreSort'; 
                sortStatus[indexSortButton] ='increSort';
            }
            
            oncoprint.remove_oncoprint();
            inner_loader_img.show();
            toggleControls(false); //disable toggleControls

            inner_loader_img.hide();

            oncoprint = Oncoprint(document.getElementById('oncoprint_body'), {
                geneData: geneDataColl.toJSON(),
                clinicalData: extraGenes,
                genes: genes,
                clinical_attrs: extraAttributes,
                legend: document.getElementById('oncoprint_legend'),
                sortStatus:sortStatus,
                mutationColor:mutationColorControl
            },extraTracks,showPatients);
            
            //            oncoprint.sortBy(sortBy.val(), cases.split(" "));
            if($('#oncoprint_sortbyfirst_dropdonw span')[0].innerHTML === 'Sort by')
            {
                oncoprint.sortBy("genes", cases.split(" "),mutationColorControl,mutationColorSort,sortStatus);
            }
            else
            {
                selectsortby();
            }
            functionFunctions();
            toggleControls(true);
            
//            zoom = reset_zoom();
//            oncoprint.zoom(zoom.val());
            var zoomvalue = $('#oncoprint_zoom_slider')[0].value;
            zoom = reset_zoom();
            $('#oncoprint_zoom_slider')[0].value = zoomvalue;
            // sync
            oncoprint.zoom(zoomvalue);
            $('#oncoprint-diagram-showlegend-icon img')[0].attributes.src.value = 'images/showlegend.svg'; // === or =
            $('#oncoprint-diagram-removeUCases-icon img')[0].attributes.src.value = 'images/removeUCases.svg';
            $('#oncoprint-diagram-removeWhitespace-icon img')[0].attributes.src.value = 'images/removeWhitespace.svg';
            oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
            oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
            utils.make_mouseover(d3.selectAll('.sample rect'),{linkage:true});        // hack =(
        });
        
        $('.attribute_name').click(
                    function() {
                    selectedTitle =$(this);
                    $(this).attr('fill', 'red');
                    });
                    
        controlIconsStatusReset();
    }

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
            legend: document.getElementById('oncoprint_legend'),
            sortStatus:sortStatus,
            mutationColor:mutationColorControl
        },extraTracks,showPatients);
               
        if(extraAttributes.length < 1)
        {
            $('#oncoprint-diagram-toolbar-buttons #clinical_first')[0].style.display = "none";
            sortBy1[0].innerHTML = 'gene data first';
        }
        
        if($('#oncoprint_sortbyfirst_dropdonw span')[0].innerHTML === 'Sort by')
        {
            oncoprint.sortBy("genes", cases.split(" "),mutationColorControl,mutationColorSort,sortStatus);
        }
        else
        {
            selectsortby();
        }
        
        totalAttrs = recordAttrs.slice(0);

        for(var n = 0; n < extraAttributes.length; n++)
        {
            for(var m=0; m<totalAttrs.length; m++) 
            {
                if(totalAttrs[m].display_name === extraAttributes[n].display_name)
                {
                    totalAttrs.splice(m,1);
                    break;
                }
            }  
        }
        
        utils.populate_clinical_attr_select(document.getElementById('select_clinical_attributes'), totalAttrs);
        
//        functionFunctions();
                    
        if(extraAttributes.length>1)
        {
            $('.oncoprint-diagram-top').css("display","inline");
        }
        else
        {
            $('.oncoprint-diagram-top').css("display","none");
            if(extraAttributes.length<1)
            {
                $('.select_clinical_attributes_from').attr("data-placeholder","Add a clinical attribute track");
            }
        }
        
        $('#oncoprint_diagram_showmorefeatures_icon').qtip({
            content: {text:
                        function()
                        { 
                            if(extraAttributes.length>=1)
                            {
                                return 'add another clinical attribute track';
                            }
                            else
                            {
                                return 'add clinical attribute track';
                            }
                        }
                    },
            position: {my:'bottom middle', at:'top middle', viewport: $(window)},
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
            show: {event: "mouseover"},
            hide: {fixed: true, delay: 100, event: "mouseout"}
            });
            
//        oncoprint.sortBy(sortBy.val(), cases.split(" "));
        if($('#oncoprint_sortbyfirst_dropdonw span')[0].innerHTML === 'Sort by')
        {
            oncoprint.sortBy("genes", cases.split(" "),mutationColorControl,mutationColorSort,sortStatus);
        }
        else
        {
            selectsortby();
        }
        toggleControls(true);
//        // disable the option to sort by clinical data
        zoom = reset_zoom();
        functionFunctions();
        $('#oncoprint-diagram-showlegend-icon img')[0].attributes.src.value = 'images/showlegend.svg'; // === or =
        $('#oncoprint-diagram-removeUCases-icon img')[0].attributes.src.value = 'images/removeUCases.svg';
        $('#oncoprint-diagram-removeWhitespace-icon img')[0].attributes.src.value = 'images/removeWhitespace.svg';
    }

    var refreshOncoPrint = function(topatient){
        oncoprint.remove_oncoprint();
        inner_loader_img.show();
        toggleControls(false); //disable toggleControls

        inner_loader_img.hide()
        
        var topatientValue;
        if(topatient !== undefined)
        {
            topatientValue = true;
        }
        oncoprint = Oncoprint(document.getElementById('oncoprint_body'), {
            geneData: geneDataColl.toJSON(),
            clinicalData: extraGenes,
            genes: genes,
            clinical_attrs: extraAttributes,
            legend: document.getElementById('oncoprint_legend'),
            sortStatus:sortStatus,
            mutationColor:mutationColorControl
        },extraTracks,topatientValue);

        functionFunctions();
//        oncoprint.sortBy(sortBy.val(), cases.split(" "));  
        if($('#oncoprint_sortbyfirst_dropdonw span')[0].innerHTML === 'Sort by')
        {
            oncoprint.sortBy("genes", cases.split(" "),mutationColorControl,mutationColorSort,sortStatus);
        }
        else
        {
            selectsortby();
        }
        toggleControls(true);
    }

    // handler for when user selects a clinical attribute to visualization
    var clinicalAttributeSelected = function() {
        $('#clinical_dropdown').dropdown( 'toggle' );
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

//            oncoprint.sortBy(sortBy.val(), cases.split(" "));
            if($('#oncoprint_sortbyfirst_dropdonw span')[0].innerHTML === 'Sort by')
            {
                oncoprint.sortBy("genes", cases.split(" "),mutationColorControl,mutationColorSort,sortStatus);
            }
            else
            {
                selectsortby();
            }

            // disable the option to sort by clinical data
//            $(sortBy.add('option[value="clinical"]')[1]).prop('disabled', true);
        } else {
            
            $('.select_clinical_attributes_from').attr("data-placeholder","Add another clinical attribute track");
            
            if(clinicalAttribute.attr_id === "# mutations")
            {
                    var urlValue = window.location.href;//get current page url
                    oncoprintClinicals = new ClinicalMutationColl();
                    oncoprintClinicals.fetch({
                    type: "POST",

                    data: {
                            mutation_profile: window.PortalGlobals.getMutationProfileId(),
                            cmd: "count_mutations",
                            case_ids: cases
                    },
                    success: function(response) {
                        inner_loader_img.hide();
                        
                        extraTracks = extraTracks.concat(response.attributes().map(function(attr) { return attr.attr_id; }));
                        extraGenes = extraGenes.concat(response.toJSON());
                        extraAttributes=extraAttributes.concat(response.attributes());
                        sortStatus = sortStatus.concat('decreSort');
//                        sortStatus['# mutations'] = 'decreSort';
                        oncoprint = Oncoprint(document.getElementById('oncoprint_body'), {
                            geneData: geneDataColl.toJSON(),
                            clinicalData: extraGenes,
                            genes: genes,
                            clinical_attrs: extraAttributes,
                            legend: document.getElementById('oncoprint_legend'),
                            sortStatus:sortStatus,
                            mutationColor:mutationColorControl
                        },extraTracks,showPatients);

                        if($('#oncoprint_sortbyfirst_dropdonw span')[0].innerHTML === 'Sort by')
                        {
                            oncoprint.sortBy("genes", cases.split(" "),mutationColorControl,mutationColorSort,sortStatus);
                        }
                        else
                        {
                            selectsortby();
                        }

                        // enable the option to sort by clinical data
                        $('#oncoprint-diagram-toolbar-buttons #clinical_first')[0].style.display = "inline";

                        for(attributeElemValue in extraAttributes)
                        {
                            var attributeElemValueIndex;
                            
                            for(var m= 0; m<totalAttrs.length;m++) 
                            {
                                if(totalAttrs[m].display_name === extraAttributes[attributeElemValue].display_name)
                                {
                                    attributeElemValueIndex=m; 
                                    totalAttrs.splice(attributeElemValueIndex,1);
                                }
                            }  
                        }

                        utils.populate_clinical_attr_select(document.getElementById('select_clinical_attributes'), totalAttrs);
                        
                        toggleControls(true);
                        
                        functionFunctions();
                        
                        zoom = reset_zoom();
                        
                        
                        var positionOfSubmit = urlValue.indexOf("clinicallist");
                        if(positionOfSubmit < 0)
                        {
                            var newUrl = urlValue + "&clinicallist=" + "mutation";
                            window.history.pushState({"html":window.location.html,"pageTitle":window.location.pageTitle},"", newUrl);
                        }
                        else
                        {
                            var stringAfterClinical = urlValue.substring(positionOfSubmit);
                            var stringArrayAfterClinical = stringAfterClinical.split("&");
                            var clinicalString = stringArrayAfterClinical[0] + "+mutation";
                            var newUrl = urlValue.replace(stringArrayAfterClinical[0],clinicalString);
                            window.history.pushState({"html":window.location.html,"pageTitle":window.location.pageTitle},"", newUrl);
                        }
                        
                        // sync
                        $('#oncoprint_diagram_showmorefeatures_icon').qtip({
                        content: {text:'add another clinical attribute track'},
                        position: {my:'bottom middle', at:'top middle', viewport: $(window)},
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
                        show: {event: "mouseover"},
                        hide: {fixed: true, delay: 100, event: "mouseout"}
                        });
                        oncoprint.zoom(zoom.val());
                        oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
                        oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
                        $('#oncoprint-diagram-showlegend-icon img')[0].attributes.src.value = 'images/showlegend.svg';
                        $('#oncoprint-diagram-removeUCases-icon img')[0].attributes.src.value = 'images/removeUCases.svg';
                        $('#oncoprint-diagram-removeWhitespace-icon img')[0].attributes.src.value = 'images/removeWhitespace.svg';
                        utils.make_mouseover(d3.selectAll('.sample rect'),{linkage:true});        // hack =(
                        invokeDataManager();
                        if(showPatients)
                        {
                            var AlteredPatientsNum= calculatePatientNum(PortalGlobals.getAlteredSampleIdList(),PortalGlobals.getPatientSampleIdMap());
                            var UnalteredPatientsNum= calculatePatientNum(PortalGlobals.getUnalteredSampleIdList(),PortalGlobals.getPatientSampleIdMap());
                            var totalPatientsNum = _.union(AlteredPatientsNum,UnalteredPatientsNum);
                            var percentOfAlteredPatients = Math.ceil((AlteredPatientsNum.length/totalPatientsNum.length * 100).toFixed(1));
                            $('#altered_value').text("Altered in "+ AlteredPatientsNum.length + " ("+ percentOfAlteredPatients +"%) of "+ totalPatientsNum.length +" patients/cases");
                        }
                        else
                        {
                            $('#altered_value').text("Altered in "+ PortalGlobals.getNumOfAlteredCases() + " ("+ Math.ceil(PortalGlobals.getPercentageOfAlteredCases()) +"%) of "+ PortalGlobals.getNumOfTotalCases() + " samples"); 
                        }    
                    }
                });
            }
            else if(clinicalAttribute.attr_id === "FRACTION_GENOME_ALTERED")
            {
                    var urlValue = window.location.href;//get current page url
                    oncoprintClinicals = new ClinicalCNAColl();
                    oncoprintClinicals.fetch({
                    type: "POST",

                    data: {
                            cancer_study_id:window.PortalGlobals.getCancerStudyId(),
                            cmd: "get_cna_fraction",
                            case_ids: cases
                    },
                    success: function(response) {
                        inner_loader_img.hide();
                        
                        extraTracks = extraTracks.concat(response.attributes().map(function(attr) { return attr.attr_id; }));
                        extraGenes = extraGenes.concat(response.toJSON());
                        extraAttributes=extraAttributes.concat(response.attributes());
                        sortStatus = sortStatus.concat('decreSort');
//                        sortStatus['FRACTION_GENOME_ALTERED'] = 'decreSort';
                        oncoprint = Oncoprint(document.getElementById('oncoprint_body'), {
                            geneData: geneDataColl.toJSON(),
                            clinicalData: extraGenes,
                            genes: genes,
                            clinical_attrs: extraAttributes,
                            legend: document.getElementById('oncoprint_legend'),
                            sortStatus:sortStatus,
                            mutationColor:mutationColorControl
                        },extraTracks,showPatients);
                                                        
//                        oncoprint.sortBy(sortBy.val(), cases.split(" "));
                        if($('#oncoprint_sortbyfirst_dropdonw span')[0].innerHTML === 'Sort by')
                        {
                            oncoprint.sortBy("genes", cases.split(" "),mutationColorControl,mutationColorSort,sortStatus);
                        }
                        else
                        {
                            selectsortby();
                        }

                        // enable the option to sort by clinical data
                        $('#oncoprint-diagram-toolbar-buttons #clinical_first')[0].style.display = "inline";

//                        // sort by genes by default
//                        sortBy.val('genes');
                        for(attributeElemValue in extraAttributes)
                        {
                            var attributeElemValueIndex;
                            
                            for(var m= 0; m<totalAttrs.length;m++) 
                            {
                                if(totalAttrs[m].display_name === extraAttributes[attributeElemValue].display_name)
                                {
                                    attributeElemValueIndex=m; 
                                    totalAttrs.splice(attributeElemValueIndex,1);
                                }
                            }  
                        }

                        utils.populate_clinical_attr_select(document.getElementById('select_clinical_attributes'), totalAttrs);
                        
                        toggleControls(true);
                        
                        functionFunctions();
                        
                        zoom = reset_zoom();

                        var positionOfSubmit = urlValue.indexOf("clinicallist");
                        if(positionOfSubmit < 0)
                        {
                            var newUrl = urlValue + "&clinicallist=" + "FRACTION_GENOME_ALTERED";
                            window.history.pushState({"html":window.location.html,"pageTitle":window.location.pageTitle},"", newUrl);
                        }
                        else
                        {
                            var stringAfterClinical = urlValue.substring(positionOfSubmit);
                            var stringArrayAfterClinical = stringAfterClinical.split("&");
                            var clinicalString = stringArrayAfterClinical[0] + "+FRACTION_GENOME_ALTERED";
                            var newUrl = urlValue.replace(stringArrayAfterClinical[0],clinicalString);
//                            var newUrl = urlValue + "+FRACTION_GENOME_ALTERED";
                            window.history.pushState({"html":window.location.html,"pageTitle":window.location.pageTitle},"", newUrl);
                        }
                        
                        // sync
                        $('#oncoprint_diagram_showmorefeatures_icon').qtip({
                        content: {text:'add another clinical attribute track'},
                        position: {my:'bottom middle', at:'top middle', viewport: $(window)},
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
                        show: {event: "mouseover"},
                        hide: {fixed: true, delay: 100, event: "mouseout"}
                        });
                        oncoprint.zoom(zoom.val());
                        oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
                        oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
                        $('#oncoprint-diagram-showlegend-icon img')[0].attributes.src.value = 'images/showlegend.svg';
                        $('#oncoprint-diagram-removeUCases-icon img')[0].attributes.src.value = 'images/removeUCases.svg';
                        $('#oncoprint-diagram-removeWhitespace-icon img')[0].attributes.src.value = 'images/removeWhitespace.svg';
                        utils.make_mouseover(d3.selectAll('.sample rect'),{linkage:true});        // hack =(
                        invokeDataManager();
                        if(showPatients)
                        {
                            var AlteredPatientsNum= calculatePatientNum(PortalGlobals.getAlteredSampleIdList(),PortalGlobals.getPatientSampleIdMap());
                            var UnalteredPatientsNum= calculatePatientNum(PortalGlobals.getUnalteredSampleIdList(),PortalGlobals.getPatientSampleIdMap());
                            var totalPatientsNum = _.union(AlteredPatientsNum,UnalteredPatientsNum);
                            var percentOfAlteredPatients = Math.ceil((AlteredPatientsNum.length/totalPatientsNum.length * 100).toFixed(1));
                            $('#altered_value').text("Altered in "+ AlteredPatientsNum.length + " ("+ percentOfAlteredPatients +"%) of "+ totalPatientsNum.length +" patients/cases");
                        }
                        else
                        {
                            $('#altered_value').text("Altered in "+ PortalGlobals.getNumOfAlteredCases() + " ("+ Math.ceil(PortalGlobals.getPercentageOfAlteredCases()) +"%) of "+ PortalGlobals.getNumOfTotalCases() + " samples"); 
                        }
                    }
                });
            }
            else
            {
                var urlValue = window.location.href;//get current page url
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
                        sortStatus = sortStatus.concat('decreSort');
//                        sortStatus[clinicalAttribute.attr_id] = 'decreSort';
                        oncoprint = Oncoprint(document.getElementById('oncoprint_body'), {
                            geneData: geneDataColl.toJSON(),
                            clinicalData: extraGenes,
                            genes: genes,
                            clinical_attrs: extraAttributes,
                            legend: document.getElementById('oncoprint_legend'),
                            sortStatus:sortStatus,
                            mutationColor:mutationColorControl
                        },extraTracks,showPatients);
                        
                        if($('#oncoprint_sortbyfirst_dropdonw span')[0].innerHTML === 'Sort by')
                        {
                            oncoprint.sortBy("genes", cases.split(" "),mutationColorControl,mutationColorSort,sortStatus);
                        }
                        else
                        {
                            selectsortby();
                        }

                        // enable the option to sort by clinical data
                        $('#oncoprint-diagram-toolbar-buttons #clinical_first')[0].style.display = "inline";

//                        // sort by genes by default
//                        sortBy.val('genes');

                        for(attributeElemValue in extraAttributes)
                        {
                            var attributeElemValueIndex;
                            
                            for(var m= 0; m<totalAttrs.length;m++) 
                            {
                                if(totalAttrs[m].display_name === extraAttributes[attributeElemValue].display_name)
                                {
                                    attributeElemValueIndex=m; 
                                    totalAttrs.splice(attributeElemValueIndex,1);
                                }
                            }
                            
                        }

                        utils.populate_clinical_attr_select(document.getElementById('select_clinical_attributes'), totalAttrs);
                        
                        toggleControls(true);

                        functionFunctions();
                        
                        zoom = reset_zoom();
                        var positionOfSubmit = urlValue.indexOf("clinicallist");
                        if(positionOfSubmit < 0)
                        {
                            var newUrl = urlValue + "&clinicallist=" + extraTracks[extraTracks.length-1];
                            window.history.pushState({"html":window.location.html,"pageTitle":window.location.pageTitle},"", newUrl);
                        }
                        else
                        {
                            var stringAfterClinical = urlValue.substring(positionOfSubmit);
                            var stringArrayAfterClinical = stringAfterClinical.split("&");
                            var clinicalString = stringArrayAfterClinical[0] + "+" + extraTracks[extraTracks.length-1];
                            var newUrl = urlValue.replace(stringArrayAfterClinical[0],clinicalString);
//                            var newUrl = urlValue + "+" + extraTracks[extraTracks.length-1];
                            window.history.pushState({"html":window.location.html,"pageTitle":window.location.pageTitle},"", newUrl);
                        }
                        
                        // sync
                        $('#oncoprint_diagram_showmorefeatures_icon').qtip({
                        content: {text:'add another clinical attribute track'},
                        position: {my:'bottom middle', at:'top middle', viewport: $(window)},
                        style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
                        show: {event: "mouseover"},
                        hide: {fixed: true, delay: 100, event: "mouseout"}
                        });
                        oncoprint.zoom(zoom.val());
                        oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
                        oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
                        $('#oncoprint-diagram-showlegend-icon img')[0].attributes.src.value = 'images/showlegend.svg';
                        $('#oncoprint-diagram-removeUCases-icon img')[0].attributes.src.value = 'images/removeUCases.svg';
                        $('#oncoprint-diagram-removeWhitespace-icon img')[0].attributes.src.value = 'images/removeWhitespace.svg';
                        utils.make_mouseover(d3.selectAll('.sample rect'),{linkage:true});        // hack =(
                        
                        invokeDataManager();
                        
                        if(showPatients)
                        {
                            var AlteredPatientsNum= calculatePatientNum(PortalGlobals.getAlteredSampleIdList(),PortalGlobals.getPatientSampleIdMap());
                            var UnalteredPatientsNum= calculatePatientNum(PortalGlobals.getUnalteredSampleIdList(),PortalGlobals.getPatientSampleIdMap());
                            var totalPatientsNum = _.union(AlteredPatientsNum,UnalteredPatientsNum);
                            var percentOfAlteredPatients = Math.ceil((AlteredPatientsNum.length/totalPatientsNum.length * 100).toFixed(1));
                            $('#altered_value').text("Altered in "+ AlteredPatientsNum.length + " ("+ percentOfAlteredPatients +"%) of "+ totalPatientsNum.length +" patients/cases");
                        }
                        else
                        {
                            $('#altered_value').text("Altered in "+ PortalGlobals.getNumOfAlteredCases() + " ("+ Math.ceil(PortalGlobals.getPercentageOfAlteredCases()) +"%) of "+ PortalGlobals.getNumOfTotalCases() + " samples"); 
                        }
                    }
                });
            }
//            alert(extraAttributes.length);
            if(extraAttributes.length>0)
            {
                $('.oncoprint-diagram-top').css("display","inline");
            }
        }
    };
    
    $(select_clinical_attributes_id).change(clinicalAttributeSelected);
    
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
    
    function calculateGeneMovement(yMovement)
    {
        var tem = genes[yMovement];
        genes[yMovement] = genes[_dragElementIndex];
        genes[_dragElementIndex]=tem;
        refreshOncoPrint();
        zoom = reset_zoom();
        // sync
        oncoprint.zoom(zoom.val());
        //reset all icons
        $('#oncoprint-diagram-showlegend-icon img')[0].attributes.src.value = 'images/showlegend.svg'; // === or =
        $('#oncoprint-diagram-removeUCases-icon img')[0].attributes.src.value = 'images/removeUCases.svg';
        $('#oncoprint-diagram-removeWhitespace-icon img')[0].attributes.src.value = 'images/removeWhitespace.svg';
        oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
        oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
        utils.make_mouseover(d3.selectAll('.sample rect'),{linkage:true});
    }
    
    function calculateClinicMovement(yMovement)
    {
        var sizeOfSamples = extraGenes.length/extraAttributes.length;//calculate length of samples

        //shift clinical attrs samples
        for(var i=0; i<sizeOfSamples; i++)
        {
            var temClinic = extraGenes[yMovement*sizeOfSamples+i];
            extraGenes[yMovement*sizeOfSamples+i]=extraGenes[_dragElementIndex*sizeOfSamples+i];
            extraGenes[_dragElementIndex*sizeOfSamples+i] = temClinic;
        }
        
        //shift clinical attrs names
        var tempClinicAttribute = extraTracks[yMovement];
        extraTracks[yMovement]=extraTracks[_dragElementIndex];
        extraTracks[_dragElementIndex] = tempClinicAttribute;
        
        var tempClinicAttrs = extraAttributes[yMovement];
        extraAttributes[yMovement]=extraAttributes[_dragElementIndex];
        extraAttributes[_dragElementIndex] = tempClinicAttrs;
        
        var tempSortStatus = sortStatus[yMovement];
        sortStatus[yMovement]=sortStatus[_dragElementIndex];
        sortStatus[_dragElementIndex] = tempSortStatus;
        
        refreshOncoPrint(showPatients);
        zoom = reset_zoom();
        // sync
        oncoprint.zoom(zoom.val());
        //reset all icons
        $('#oncoprint-diagram-showlegend-icon img')[0].attributes.src.value = 'images/showlegend.svg'; // === or =
        $('#oncoprint-diagram-removeUCases-icon img')[0].attributes.src.value = 'images/removeUCases.svg';
        $('#oncoprint-diagram-removeWhitespace-icon img')[0].attributes.src.value = 'images/removeWhitespace.svg';
        oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
        oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
        utils.make_mouseover(d3.selectAll('.sample rect'),{linkage:true});
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
        
        if ((e.button == 1 && window.event != null || e.button == 0)&& target.className.animVal==="attribute_name")
        {        
            target.attributes.fill.value = "red";
            
            // grab the clicked element's position
            _offsetX = ExtractNumber(target.parentElement.attributes.x.value);
            _offsetY = ExtractNumber(target.parentElement.attributes.y.value);
            
            for(m in genes) 
            {
//                if(genes[m] === target.textContent)
                if(genes[m] === target.attributes.attributename.value)
                {
                    _dragElementIndex = parseInt(m); 
                    break;
                }
            }
            
            if(_dragElementIndex === undefined)
            {
                selectedNotMutation = true;
                for(n in extraAttributes)
                {
//                    if(extraAttributes[n].display_name === target.textContent)
                    if(extraAttributes[n].display_name === target.attributes.attributename.value)
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
            if(yPosition > (extraAttributes.length*spaceHeight - 7))
            {
                yPosition = extraAttributes.length*spaceHeight - 7;
            }
            else if(yPosition<10)
            {
                yPosition = 10;
            }
        }
        else
        {
            
            if(extraAttributes.length>0)
            {
                if(yPosition > (extraAttributes.length*spaceHeight - gapSpaceGeneClinic + genes.length*spaceHeight - 7))
                {
                    yPosition = extraAttributes.length*spaceHeight - gapSpaceGeneClinic + genes.length*spaceHeight - 7;
                }
                else if(yPosition<(extraAttributes.length*spaceHeight - gapSpaceGeneClinic +10))
                {
                    yPosition = extraAttributes.length*spaceHeight - gapSpaceGeneClinic + 10;
                }
            }
            else
            {
                if(yPosition > (extraAttributes.length*spaceHeight + genes.length*spaceHeight - 7))
                {
                    yPosition = extraAttributes.length*spaceHeight + genes.length*spaceHeight - 7;
                }
                else if(yPosition<(extraAttributes.length*spaceHeight+10))
                {
                    yPosition = extraAttributes.length*spaceHeight + 10;
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
            if(extraAttributes.length>0)
            {
                indexValue = parseInt((yPosition-extraAttributes.length * spaceHeight - gapSpaceGeneClinic)/spaceHeight);
            }
            else
            {
                indexValue = parseInt((yPosition-extraAttributes.length * spaceHeight)/spaceHeight);
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
            if(yPosition > (extraAttributes.length*spaceHeight - 7))
            {
                yPosition = extraAttributes.length*spaceHeight - 7;
            }
            else if(yPosition<10)
            {
                yPosition = 10;
            }
        }
        else
        {
            if(extraAttributes.length>0)
            {
                if(yPosition > (extraAttributes.length*spaceHeight - gapSpaceGeneClinic + genes.length*spaceHeight - 7))
                {
                    yPosition = extraAttributes.length*spaceHeight - gapSpaceGeneClinic + genes.length*spaceHeight - 7;
                }
                else if(yPosition<(extraAttributes.length*spaceHeight - gapSpaceGeneClinic +10))
                {
                    yPosition = extraAttributes.length*spaceHeight - gapSpaceGeneClinic + 10;
                }
            }
            else
            {
                if(yPosition > (extraAttributes.length*spaceHeight + genes.length*spaceHeight - 7))
                {
                    yPosition = extraAttributes.length*spaceHeight + genes.length*spaceHeight - 7;
                }
                else if(yPosition<(extraAttributes.length*spaceHeight+10))
                {
                    yPosition = extraAttributes.length*spaceHeight + 10;
                }
            }
        }
        
        _dragElement.parentElement.attributes.y.value = yPosition.toString(); 
    }

    function InitDragDrop()
    {
        document.onmousedown = OnMouseDown;
        document.onmouseup = OnMouseUp;
    }

    var invokeDataManager = function() {
        //TODO: tmp solution for re-using data
        window.PortalGlobals.setGeneData(geneDataColl.toJSON());
        PortalDataColl.setOncoprintData(oncoprint.getOncoprintData()); 
        var alterInfo = utils.alteration_info(geneDataColl.toJSON());
        PortalDataColl.setOncoprintStat(alterInfo);
    };
    
    function findIndexInArray(elementValue, arrayValue,patientMap)
    {
        for(var i = 0; i < arrayValue.length; i++)
        {
            if(patientMap[elementValue] === arrayValue[i])
            {
                return i;
            }  
        }
        
        return -1;
    }
    
    function calculatePatientNum(samples,patientsMap)
    {
        var PatientsList = [];
        samples = samples.split(" ");
        for(var i = 0; i < samples.length; i++)
        {
            if(patientsMap[samples[i]]!==undefined)
            {
                if(findIndexInArray(samples[i],PatientsList,patientsMap) === -1)
                {
                   PatientsList.push(patientsMap[samples[i]]); 
                }
            }
        }
        
        return PatientsList.sort();
    }

    $(document).ready(function() {
        $('#oncoprint-diagram-toolbar-buttons #genes_first_a').click(function(){
            oncoprint.sortBy('genes', cases.split(" "),mutationColorControl,mutationColorSort,sortStatus);
        });

        $('#oncoprint-diagram-toolbar-buttons #clinical_first_a').click(function(){
            oncoprint.sortBy('clinical', cases.split(" "),mutationColorControl,mutationColorSort,sortStatus);
        });
        $('#oncoprint-diagram-toolbar-buttons #alphabetically_first_a').click(function(){
            oncoprint.sortBy('alphabetical', cases.split(" "),mutationColorControl,mutationColorSort,sortStatus);
        });
        $('#oncoprint-diagram-toolbar-buttons #user_defined_first_a').click(function(){
            oncoprint.sortBy('custom', cases.split(" "),mutationColorControl,mutationColorSort,sortStatus);
        });

        $('#oncoprint_diagram_showmorefeatures_icon').click(function(){
            $('#select_clinical_attributes_chzn').addClass("chzn-with-drop");
        });
        
        $('#oncoprint_diagram_showmorefeatures_icon').qtip({
            content: {text:'add clinical attribute track'},
            position: {my:'bottom middle', at:'top middle', viewport: $(window)},
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
            show: {event: "mouseover"},
            hide: {fixed: true, delay: 100, event: "mouseout"}
            });
            
        $('#oncoprint_diagram_slider_icon').css('height',function(){ 
            var is_firefox = navigator.userAgent.indexOf("Firefox") !== -1;
            var result = is_firefox ? '30px' : '16px';
            return result;
        }); 
        
        $('#oncoprint_diagram_slider_icon')[0].style.display = cbio.util.browser.msie ? "none":"inline";
        
        $('#oncoprint_zoomout').click(function() {
            var tempValue = parseFloat($('#oncoprint_zoom_slider')[0].value) - 0.05;
            $('#oncoprint_zoom_slider')[0].value = tempValue > 0.1 ? tempValue : 0.1;
            oncoprint.zoom(zoom.val());
        });
        $('#oncoprint_zoomin').click(function() {
            var tempValue = parseFloat($('#oncoprint_zoom_slider')[0].value) + 0.05;
            $('#oncoprint_zoom_slider')[0].value = tempValue < 1.0 ? tempValue : 1.0;
            oncoprint.zoom(zoom.val());
        });
            
        $('#toggle_unaltered_cases').click(function() {
            oncoprint.toggleUnalteredCases();
            utils.make_mouseover(d3.selectAll('.sample rect'),{linkage:true});     // hack =(
        });

        $('#toggle_whitespace').click(function() {
            oncoprint.toggleWhiteSpace();
        });

            $('#oncoprint-diagram-removeUCases-icon').click(function(){
              if($('#oncoprint-diagram-removeUCases-icon img')[0].attributes.src.value === 'images/removeUCases.svg')
              {
                oncoprint.toggleUnalteredCases();
                utils.make_mouseover(d3.selectAll('.sample rect'),{linkage:true});     // hack =(
                $('#oncoprint-diagram-removeUCases-icon img')[0].attributes.src.value = 'images/unremoveUCases.svg';
              }
              else
              {
                oncoprint.toggleUnalteredCases();
                utils.make_mouseover(d3.selectAll('.sample rect'),{linkage:true});     // hack =(
                $('#oncoprint-diagram-removeUCases-icon img')[0].attributes.src.value = 'images/removeUCases.svg';
              }
            });
            $('#oncoprint-diagram-removeUCases-icon').hover(
            function () {
            $(this).css('fill', '#0000FF');
            $(this).css('font-size', '18px');
            $(this).css('cursor', 'pointer');
            },
            function () {
            $(this).css('fill', '#87CEFA');
            $(this).css('font-size', '12px');
            });
            $('#oncoprint-diagram-removeUCases-icon').qtip({
            content: {text: 
                        function(){
                        if($('#oncoprint-diagram-removeUCases-icon img')[0].attributes.src.value === 'images/removeUCases.svg')
                        {return 'remove unaltered cases';}
                        else
                        {
                            return 'show unaltered cases';
                        }
                    }
                },
            position: {my:'bottom middle', at:'top middle', viewport: $(window)},
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
            show: {event: "mouseover"},
            hide: {fixed: true, delay: 100, event: "mouseout"}
            });


            $('#oncoprint-diagram-removeWhitespace-icon').click(function(){
              if($('#oncoprint-diagram-removeWhitespace-icon img')[0].attributes.src.value === 'images/removeWhitespace.svg')
              {
                  oncoprint.toggleWhiteSpace();
                  $('#oncoprint-diagram-removeWhitespace-icon img')[0].attributes.src.value = 'images/unremoveWhitespace.svg';
              }
              else
              {
                 oncoprint.toggleWhiteSpace();
                 $('#oncoprint-diagram-removeWhitespace-icon img')[0].attributes.src.value = 'images/removeWhitespace.svg'; 
              }
            });
            $('#oncoprint-diagram-removeWhitespace-icon').hover(
            function () {
            $(this).css('fill', '#0000FF');
            $(this).css('font-size', '18px');
            $(this).css('cursor', 'pointer');
            },
            function () {
            $(this).css('fill', '#87CEFA');
            $(this).css('font-size', '12px');
            });
            $('#oncoprint-diagram-removeWhitespace-icon').qtip({
            content: {text: 
                        function(){
                        if($('#oncoprint-diagram-removeWhitespace-icon img')[0].attributes.src.value === 'images/removeWhitespace.svg')
                        {return 'remove whitespace between cases';}
                        else
                        {
                            return 'show whitespace between cases';
                        }
                    }
            },
            position: {my:'bottom middle', at:'top middle', viewport: $(window)},
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
            show: {event: "mouseover"},
            hide: {fixed: true, delay: 100, event: "mouseout"}
            });   
            
            //show or hide legends of oncoprint
            $('#oncoprint-diagram-showlegend-icon').click(function(){
              if($('#oncoprint-diagram-showlegend-icon img')[0].attributes.src.value === 'images/showlegend.svg')
              {
//                $("#oncoprint_legend").css("display","inline");
                $("#oncoprint_legend .mutation_legend_table").css("display","inline");
                $('#oncoprint-diagram-showlegend-icon img')[0].attributes.src.value = 'images/hidelegend.svg';
              }
              else
              {
//                $("#oncoprint_legend").css("display","inline");
                $("#oncoprint_legend .mutation_legend_table").css("display","none");
                $('#oncoprint-diagram-showlegend-icon img')[0].attributes.src.value = 'images/showlegend.svg'; 
              }
            });
            $('#oncoprint-diagram-showlegend-icon').hover(
            function () {
            $(this).css('fill', '#0000FF');
            $(this).css('font-size', '18px');
            $(this).css('cursor', 'pointer');
            },
            function () {
            $(this).css('fill', '#87CEFA');
            $(this).css('font-size', '12px');
            });
            $('#oncoprint-diagram-showlegend-icon').qtip({
            content: {text:function(){
                        if($('#oncoprint-diagram-showlegend-icon img')[0].attributes.src.value === 'images/showlegend.svg')
                        {
                            return 'show legends for clinical attribute tracks';
                        }
                        else
                        {
                            return 'hide legends for clinical attribute tracks';
                        }
                    }
            },
            position: {my:'bottom middle', at:'top middle', viewport: $(window)},
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
            show: {event: "mouseover"},
            hide: {fixed: true, delay: 100, event: "mouseout"}
            }); 
            
            $('#oncoprint_diagram_topatientid_icon').click(function(){
              inner_loader_img.show();
                if($('#switchPatientSample')[0].attributes.valuetype.value === "patients")
                {
                   $('#switchPatientSample').text("Show patients in OncoPrint"); 
                   $('#switchPatientSample')[0].attributes.valuetype.value = "samples";
                }
                else
                {
                    $('#switchPatientSample').text("Show samples in OncoPrint")
                    $('#switchPatientSample')[0].attributes.valuetype.value = "patients";
                }
                
              if($('#oncoprint_diagram_topatientid_icon img')[0].attributes.src.value === 'images/cool.svg')
              { 
                var urlValueNow = window.location.href;
                var newUrl = urlValueNow.replace("&show_samples=true","");
                window.history.pushState({"html":window.location.html,"pageTitle":window.location.pageTitle},"", newUrl);
                if($('#oncoprint_diagram_showmutationcolor_icon img')[0].attributes.src.value === 'images/mutationcolorsort.svg')
                {
                    mutationColorControl = 'singleColor';
                    mutationColorSort = 'mutationcolorsort';
                }
                else if($('#oncoprint_diagram_showmutationcolor_icon img')[0].attributes.src.value === 'images/colormutations.svg')
                {
                    mutationColorControl = 'multiColor';
                    mutationColorSort = 'mutationcolornonsort';
                }
                else if($('#oncoprint_diagram_showmutationcolor_icon img')[0].attributes.src.value === 'images/uncolormutations.svg')
                {                
                    mutationColorControl = 'multiColor';
                    mutationColorSort = 'mutationcolorsort';  
                }
                
                var AlteredPatientsNum= calculatePatientNum(PortalGlobals.getAlteredSampleIdList(),PortalGlobals.getPatientSampleIdMap());
                var UnalteredPatientsNum= calculatePatientNum(PortalGlobals.getUnalteredSampleIdList(),PortalGlobals.getPatientSampleIdMap());
                
                var totalPatientsNum = _.union(AlteredPatientsNum,UnalteredPatientsNum);
                var percentOfAlteredPatients = Math.ceil((AlteredPatientsNum.length/totalPatientsNum.length * 100).toFixed(1));
                
                $('#altered_value').text("Altered in "+ AlteredPatientsNum.length + " ("+ percentOfAlteredPatients +"%) of "+ totalPatientsNum.length +" cases/patients");

                showPatients = true;
                refreshOncoPrint(showPatients);

                var zoomvalue = $('#oncoprint_zoom_slider')[0].value;
                zoom = reset_zoom();
                $('#oncoprint_zoom_slider')[0].value = zoomvalue;
                // sync
                oncoprint.zoom(zoomvalue);
                
                oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
                if($('#oncoprint-diagram-removeWhitespace-icon img')[0].attributes.src.value === 'images/removeWhitespace.svg')
                {
                    oncoprint.toggleWhiteSpace(true);
                }
                else
                {
                    oncoprint.toggleWhiteSpace(false);
                }
                
                utils.make_mouseover(d3.selectAll('.sample rect'),{linkage:true});        // hack =(
                $('#oncoprint_diagram_topatientid_icon img')[0].attributes.src.value = 'images/cool2.svg';
                
                $('#oncoprint-diagram-showlegend-icon img')[0].attributes.src.value = 'images/showlegend.svg';
                $('#oncoprint-diagram-removeUCases-icon img')[0].attributes.src.value = 'images/removeUCases.svg';
                
                $('.legend_missense_name').text("Missense Mutation") ;
              }
                else if($('#oncoprint_diagram_topatientid_icon img')[0].attributes.src.value === 'images/cool2.svg')
              {
                var urlValueNow = window.location.href;
                var newUrl = urlValueNow + "&show_samples=true";
                window.history.pushState({"html":window.location.html,"pageTitle":window.location.pageTitle},"", newUrl);
                
                if($('#oncoprint_diagram_showmutationcolor_icon img')[0].attributes.src.value === 'images/mutationcolorsort.svg')
                {
                    mutationColorControl = 'singleColor';
                    mutationColorSort = 'mutationcolorsort';
                }
                else if($('#oncoprint_diagram_showmutationcolor_icon img')[0].attributes.src.value === 'images/colormutations.svg')
                {
                    mutationColorControl = 'multiColor';
                    mutationColorSort = 'mutationcolornonsort';
                }
                else if($('#oncoprint_diagram_showmutationcolor_icon img')[0].attributes.src.value === 'images/uncolormutations.svg')
                {                
                    mutationColorControl = 'multiColor';
                    mutationColorSort = 'mutationcolorsort';  
                }
                showPatients = false;
                refreshOncoPrint();

                $('#altered_value').text("Altered in "+ PortalGlobals.getNumOfAlteredCases() + " ("+ Math.ceil(PortalGlobals.getPercentageOfAlteredCases()) +"%) of "+ PortalGlobals.getNumOfTotalCases() + " samples");
                
                var zoomvalue = $('#oncoprint_zoom_slider')[0].value;
                zoom = reset_zoom();
                $('#oncoprint_zoom_slider')[0].value = zoomvalue;
                // sync
                oncoprint.zoom(zoomvalue);
 
                oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
                if($('#oncoprint-diagram-removeWhitespace-icon img')[0].attributes.src.value === 'images/removeWhitespace.svg')
                {
                    oncoprint.toggleWhiteSpace(true);
                }
                else
                {
                    oncoprint.toggleWhiteSpace(false);
                }
                
                utils.make_mouseover(d3.selectAll('.sample rect'),{linkage:true});        // hack =(
                $('#oncoprint_diagram_topatientid_icon img')[0].attributes.src.value = 'images/cool.svg';
                
                $('#oncoprint-diagram-showlegend-icon img')[0].attributes.src.value = 'images/showlegend.svg';
                $('#oncoprint-diagram-removeUCases-icon img')[0].attributes.src.value = 'images/removeUCases.svg';
                
                $('.legend_missense_name').text("Missense Mutation");
                $('.legend_nonmissense').css("display","inline");
              }
              inner_loader_img.hide();
            });
            
            
            
            $('#switchPatientSample').click(function(){
                inner_loader_img.show();
                if($('#switchPatientSample')[0].attributes.valuetype.value === "patients")
                {
                   $('#switchPatientSample').text("Show patients in OncoPrint"); 
                   $('#switchPatientSample')[0].attributes.valuetype.value = "samples";
                }
                else
                {
                    $('#switchPatientSample').text("Show samples in OncoPrint")
                    $('#switchPatientSample')[0].attributes.valuetype.value = "patients";
                }
              
              if($('#oncoprint_diagram_topatientid_icon img')[0].attributes.src.value === 'images/cool.svg')
              {  
                var urlValueNow = window.location.href;
                var newUrl = urlValueNow.replace("&show_samples=true","");
                window.history.pushState({"html":window.location.html,"pageTitle":window.location.pageTitle},"", newUrl);
                if($('#oncoprint_diagram_showmutationcolor_icon img')[0].attributes.src.value === 'images/mutationcolorsort.svg')
                {
                    mutationColorControl = 'singleColor';
                    mutationColorSort = 'mutationcolorsort';
                }
                else if($('#oncoprint_diagram_showmutationcolor_icon img')[0].attributes.src.value === 'images/colormutations.svg')
                {
                    mutationColorControl = 'multiColor';
                    mutationColorSort = 'mutationcolornonsort';
                }
                else if($('#oncoprint_diagram_showmutationcolor_icon img')[0].attributes.src.value === 'images/uncolormutations.svg')
                {                
                    mutationColorControl = 'multiColor';
                    mutationColorSort = 'mutationcolorsort';  
                }
                
                var AlteredPatientsNum= calculatePatientNum(PortalGlobals.getAlteredSampleIdList(),PortalGlobals.getPatientSampleIdMap());
                var UnalteredPatientsNum= calculatePatientNum(PortalGlobals.getUnalteredSampleIdList(),PortalGlobals.getPatientSampleIdMap());
                
                var totalPatientsNum = _.union(AlteredPatientsNum,UnalteredPatientsNum);
                var percentOfAlteredPatients = Math.ceil((AlteredPatientsNum.length/totalPatientsNum.length * 100).toFixed(1));
                
                $('#altered_value').text("Altered in "+ AlteredPatientsNum.length + " ("+ percentOfAlteredPatients +"%) of "+ totalPatientsNum.length +" patients/cases");
                
                showPatients = true;
                refreshOncoPrint(showPatients);

                var zoomvalue = $('#oncoprint_zoom_slider')[0].value;
                zoom = reset_zoom();
                $('#oncoprint_zoom_slider')[0].value = zoomvalue;
                // sync
                oncoprint.zoom(zoomvalue);
                
                oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
                if($('#oncoprint-diagram-removeWhitespace-icon img')[0].attributes.src.value === 'images/removeWhitespace.svg')
                {
                    oncoprint.toggleWhiteSpace(true);
                }
                else
                {
                    oncoprint.toggleWhiteSpace(false);
                }
                
                utils.make_mouseover(d3.selectAll('.sample rect'),{linkage:true});        // hack =(
                $('#oncoprint_diagram_topatientid_icon img')[0].attributes.src.value = 'images/cool2.svg';
                
                $('#oncoprint-diagram-showlegend-icon img')[0].attributes.src.value = 'images/showlegend.svg';
                $('#oncoprint-diagram-removeUCases-icon img')[0].attributes.src.value = 'images/removeUCases.svg';
                
                $('.legend_missense_name').text("Missense Mutation") ;
              }
                else if($('#oncoprint_diagram_topatientid_icon img')[0].attributes.src.value === 'images/cool2.svg')
              {
                var urlValueNow = window.location.href;
                var newUrl = urlValueNow + "&show_samples=true";
                window.history.pushState({"html":window.location.html,"pageTitle":window.location.pageTitle},"", newUrl);
                if($('#oncoprint_diagram_showmutationcolor_icon img')[0].attributes.src.value === 'images/mutationcolorsort.svg')
                {
                    mutationColorControl = 'singleColor';
                    mutationColorSort = 'mutationcolorsort';
                }
                else if($('#oncoprint_diagram_showmutationcolor_icon img')[0].attributes.src.value === 'images/colormutations.svg')
                {
                    mutationColorControl = 'multiColor';
                    mutationColorSort = 'mutationcolornonsort';
                }
                else if($('#oncoprint_diagram_showmutationcolor_icon img')[0].attributes.src.value === 'images/uncolormutations.svg')
                {                
                    mutationColorControl = 'multiColor';
                    mutationColorSort = 'mutationcolorsort';  
                }
                showPatients = false;
                refreshOncoPrint();

                $('#altered_value').text("Altered in "+ PortalGlobals.getNumOfAlteredCases() + " ("+ Math.ceil(PortalGlobals.getPercentageOfAlteredCases()) +"%) of "+ PortalGlobals.getNumOfTotalCases() + " samples");

                var zoomvalue = $('#oncoprint_zoom_slider')[0].value;
                zoom = reset_zoom();
                $('#oncoprint_zoom_slider')[0].value = zoomvalue;
                // sync
                oncoprint.zoom(zoomvalue);
                
                oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
                if($('#oncoprint-diagram-removeWhitespace-icon img')[0].attributes.src.value === 'images/removeWhitespace.svg')
                {
                    oncoprint.toggleWhiteSpace(true);
                }
                else
                {
                    oncoprint.toggleWhiteSpace(false);
                }
                
                utils.make_mouseover(d3.selectAll('.sample rect'),{linkage:true});        // hack =(
                $('#oncoprint_diagram_topatientid_icon img')[0].attributes.src.value = 'images/cool.svg';
                
                $('#oncoprint-diagram-showlegend-icon img')[0].attributes.src.value = 'images/showlegend.svg';
                $('#oncoprint-diagram-removeUCases-icon img')[0].attributes.src.value = 'images/removeUCases.svg';
                
                $('.legend_missense_name').text("Missense Mutation");
                $('.legend_nonmissense').css("display","inline");
              }
              inner_loader_img.hide();
            });            
            
            
            
            
            $('#oncoprint_diagram_topatientid_icon').hover(
            function () {
            $(this).css('fill', '#0000FF');
            $(this).css('font-size', '18px');
            $(this).css('cursor', 'pointer');
            },
            function () {
            $(this).css('fill', '#87CEFA');
            $(this).css('font-size', '12px');
            });
            
            $('#oncoprint_diagram_topatientid_icon').qtip({
            content: {text: 
                        function(){
                        if($('#oncoprint_diagram_topatientid_icon img')[0].attributes.src.value === 'images/cool.svg')
                        {
                            return 'Show events per patient';
                        }
                        else if($('#oncoprint_diagram_topatientid_icon img')[0].attributes.src.value === 'images/cool2.svg')
                        {
                           return 'Show events per sample';
                        }
                    }
            },
            position: {my:'bottom middle', at:'top middle', viewport: $(window)},
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
            show: {event: "mouseover"},
            hide: {fixed: true, delay: 100, event: "mouseout"}
            }); 

            //color different mutation with different color
            $('#oncoprint_diagram_showmutationcolor_icon').click(function(){
              inner_loader_img.show();
              if($('#oncoprint_diagram_showmutationcolor_icon img')[0].attributes.src.value === 'images/uncolormutations.svg')
              {
//                $(this).qtip({
//                content: {text: 'color-code different mutation types'},
//                position: {my:'bottom middle', at:'top middle', viewport: $(window)},
//                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
//                show: {event: "mouseover"}
//                });
                
                mutationColorControl = 'singleColor';
                mutationColorSort = 'mutationcolorsort';
                refreshOncoPrint();
                //the code below is that after color mutation zoom to the largest value 
//                zoom = reset_zoom();
//                // sync
//                oncoprint.zoom(zoom.val());

                var zoomvalue = $('#oncoprint_zoom_slider')[0].value;
                zoom = reset_zoom();
                $('#oncoprint_zoom_slider')[0].value = zoomvalue;
                // sync
                oncoprint.zoom(zoomvalue);

                oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
                //oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
                if($('#oncoprint-diagram-removeWhitespace-icon img')[0].attributes.src.value === 'images/removeWhitespace.svg')
                {
                    oncoprint.toggleWhiteSpace(true);
                }
                else
                {
                    oncoprint.toggleWhiteSpace(false);
                }
                
                utils.make_mouseover(d3.selectAll('.sample rect'),{linkage:true});        // hack =(
                $('#oncoprint_diagram_showmutationcolor_icon img')[0].attributes.src.value = 'images/mutationcolorsort.svg';
                
                $('#oncoprint-diagram-showlegend-icon img')[0].attributes.src.value = 'images/showlegend.svg';
                $('#oncoprint-diagram-removeUCases-icon img')[0].attributes.src.value = 'images/removeUCases.svg';
                //$('#oncoprint-diagram-removeWhitespace-icon img')[0].attributes.src.value = 'images/removeWhitespace.svg';
                
                $('.legend_missense_name').text("Mutation") ;
              }
                else if($('#oncoprint_diagram_showmutationcolor_icon img')[0].attributes.src.value === 'images/colormutations.svg')
              {
//                $(this).qtip({
//                content: {text: 'show all mutations in the same color'},
//                position: {my:'bottom middle', at:'top middle', viewport: $(window)},
//                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
//                show: {event: "mouseover"}
//                });
                
                mutationColorControl = 'multiColor';
                mutationColorSort = 'mutationcolornonsort';
                refreshOncoPrint();
                //the code below is that after color mutation zoom to the largest value 
//                zoom = reset_zoom();
//                // sync
//                oncoprint.zoom(zoom.val());

                var zoomvalue = $('#oncoprint_zoom_slider')[0].value;
                zoom = reset_zoom();
                $('#oncoprint_zoom_slider')[0].value = zoomvalue;
                // sync
                oncoprint.zoom(zoomvalue);
 
                oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
                //oncoprint.toggleWhiteSpace(!$('#toggle_whitespace').is(":checked"));
                if($('#oncoprint-diagram-removeWhitespace-icon img')[0].attributes.src.value === 'images/removeWhitespace.svg')
                {
                    oncoprint.toggleWhiteSpace(true);
                }
                else
                {
                    oncoprint.toggleWhiteSpace(false);
                }
                
                utils.make_mouseover(d3.selectAll('.sample rect'),{linkage:true});        // hack =(
                $('#oncoprint_diagram_showmutationcolor_icon img')[0].attributes.src.value = 'images/uncolormutations.svg';
                
                $('#oncoprint-diagram-showlegend-icon img')[0].attributes.src.value = 'images/showlegend.svg';
                $('#oncoprint-diagram-removeUCases-icon img')[0].attributes.src.value = 'images/removeUCases.svg';
                //$('#oncoprint-diagram-removeWhitespace-icon img')[0].attributes.src.value = 'images/removeWhitespace.svg';
                
                $('.legend_missense_name').text("Missense Mutation");
                $('.legend_nonmissense').css("display","inline");
              }
              else
              {
//                $(this).qtip({
//                content: {text: 'color-code different mutation types with sorting order'},
//                position: {my:'bottom middle', at:'top middle', viewport: $(window)},
//                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
//                show: {event: "mouseover"}
//                });
                
                mutationColorControl = 'multiColor';
                mutationColorSort = 'mutationcolorsort';
                refreshOncoPrint();

                var zoomvalue = $('#oncoprint_zoom_slider')[0].value;
                zoom = reset_zoom();
                $('#oncoprint_zoom_slider')[0].value = zoomvalue;
                // sync
                oncoprint.zoom(zoomvalue);
 
                oncoprint.showUnalteredCases(!$('#toggle_unaltered_cases').is(":checked"));
                if($('#oncoprint-diagram-removeWhitespace-icon img')[0].attributes.src.value === 'images/removeWhitespace.svg')
                {
                    oncoprint.toggleWhiteSpace(true);
                }
                else
                {
                    oncoprint.toggleWhiteSpace(false);
                }
                
                utils.make_mouseover(d3.selectAll('.sample rect'),{linkage:true});        // hack =(
                $('#oncoprint_diagram_showmutationcolor_icon img')[0].attributes.src.value = 'images/colormutations.svg';
                
                $('#oncoprint-diagram-showlegend-icon img')[0].attributes.src.value = 'images/showlegend.svg';
                $('#oncoprint-diagram-removeUCases-icon img')[0].attributes.src.value = 'images/removeUCases.svg';
                
                $('.legend_missense_name').text("Missense Mutation");
                $('.legend_nonmissense').css("display","inline");
              }
              inner_loader_img.hide();
            });            
            
            $('#oncoprint_diagram_showmutationcolor_icon').hover(
            function () {
            $(this).css('fill', '#0000FF');
            $(this).css('font-size', '18px');
            $(this).css('cursor', 'pointer');
            },
            function () {
            $(this).css('fill', '#87CEFA');
            $(this).css('font-size', '12px');
            });
            
            $('#oncoprint_diagram_showmutationcolor_icon').qtip({
            content: {text: 
                        function(){
                        if($('#oncoprint_diagram_showmutationcolor_icon img')[0].attributes.src.value === 'images/colormutations.svg')
                        {
                            return 'Show mutations with different colors by type and sort accordingly';
                        }
                        else if($('#oncoprint_diagram_showmutationcolor_icon img')[0].attributes.src.value === 'images/mutationcolorsort.svg')
                        {
                            return 'Show mutations with different colors by type';
                        }
                        else
                        {
                            return 'Show mutations with the same color';
                        }
                    }
            },
            position: {my:'bottom middle', at:'top middle', viewport: $(window)},
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite' },
            show: {event: "mouseover"},
            hide: {fixed: true, delay: 100, event: "mouseout"}
            }); 
            
            
            $('#oncoprint-diagram-downloads-icon').qtip({
            //id: "#oncoprint-diagram-downloads-icon-qtip",
            style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightwhite'  },
            show: {event: "mouseover"},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'top center',at:'bottom center', viewport: $(window)},
            content: {
                text:   "<button class='oncoprint-diagram-download' type='pdf' style='cursor:pointer;width:90px;'>PDF</button> <br/>"+
                        "<button class='oncoprint-diagram-download' type='svg' style='cursor:pointer;width:90px;'>SVG</button> <br/>"+
                        "<button class='oncoprint-sample-download'  type='txt' style='cursor:pointer;width:90px;'>Sample order</button>"
            },
            events:{
                render:function(event){     
                        $('.oncoprint-diagram-download').click(function() {
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

                    $('.oncoprint-sample-download').click(function() {
                        var samples = "Sample order in the Oncoprint is: \n";
                        var genesValue = oncoprint.getData();
                        for(var i = 0; i< genesValue.length; i++)
                        {
                            samples= samples + genesValue[i].key+"\n";
                        }
                        var downloadOpts = {
				filename: 'OncoPrintSamples.txt',
				contentType: "text/plain;charset=utf-8",
				preProcess: false};

			// send download request with filename & file content info
			cbio.download.initDownload(samples, downloadOpts);
                    });
                }
            }
        });
        
        $('.oncoprint-diagram-Shift').click(function() {
            shiftGeneData();
        });

        $('.oncoprint-diagram-top').click(function() {
            shiftClinicData();
        });
        
        cbio.util.autoHideOnMouseLeave($("#oncoprint_whole_body"), $("#oncoprint-diagram-toolbar-buttons"));
        
        InitDragDrop();
    });
});
