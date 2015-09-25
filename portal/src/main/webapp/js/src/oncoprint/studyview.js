// This is for the moustache-like templates
// prevents collisions with JSP tags
$(document).ready(function() {
	_.templateSettings = {
		interpolate: /\{\{(.+?)\}\}/g
	};
	$('#oncoprint_controls').html(_.template($('#main-controls-template').html())());
        
        var clinical_data_promises = []
        var GenePanelData = [];
        var gene_panel_clinical_coll = new ClinicalColl();
        clinical_data_promises.push(
        gene_panel_clinical_coll.fetch({
        type: "POST",
        data: {
           cancer_study_id: cancer_study_id_selected,
           attribute_id: "GENE_PANEL",
           case_list: cases
        },
        success: function(response) {
               GenePanelData = response.toJSON();
           }
        }));
        
        //fetch genepanel
        var gene_panel_files = [{path:"api/genepanel/IMPACT341", file:"IMPACT341"},{path:"api/genepanel/IMPACT410",file:"IMPACT410"}];
        var genePanel = [];
        var gainGenepanel = function(filename,datafetchArray){
            datafetchArray.push(
            $.getJSON(filename,function(result){
                genePanel[(filename.split("/"))[2]] = result;
            }));
        }
        
        
        for(var i=0; i<gene_panel_files.length; i++)
        {
            var gene_panel_file = gene_panel_files[i];
            var gene_panel_file_name = gene_panel_files.file;
            clinical_data_promises.push($.getJSON(gene_panel_file.path, function(result) { gene_panel_file_data[gene_panel_file_name] = result; }));
        
        }
        //fetch genepanel data end 
            
        $.when.apply(null, clinical_data_promises).done(function() {
            //process genepanel attribute to patient format
//            var GenePanelDataPatient = [];
//            if(GenePanelData.length>0)
//            {
//                if(typeof(window.PortalGlobals) !== 'undefined')
//                {
//                    var SampleIdMapPatientId = window.PortalGlobals.getPatientSampleIdMap();
//                }
//
//                for(var i = 0; i < GenePanelData.length; i++)
//                {
//                    var patiendId = SampleIdMapPatientId[GenePanelData[i].sample];
//
//                    var findIndexValue = function(){
//                        for(var j=0; j < GenePanelDataPatient.length; j++)
//                        {
//                            if(patiendId === GenePanelDataPatient[j].patient)
//                            {
//                                return j;
//                            }
//                        }
//                        return -1;
//                    };
//
//                    var positionValue = findIndexValue();
//                    if(positionValue > -1)
//                    {
//                       if(GenePanelDataPatient[positionValue].attr_val !== GenePanelData[i].attr_val)
//                       {
//                           GenePanelDataPatient[positionValue].attr_val = GenePanelDataPatient[positionValue].attr_val+ ","+GenePanelData[i].attr_val;
//                       }
//                    }
//                    else
//                    {
//                      var genepanelAttibuteDataPatient = {attr_id:"GENE_PANEL",patient:patiendId}; 
//                      genepanelAttibuteDataPatient.attr_val = GenePanelData[i].attr_val;
//                      GenePanelDataPatient.push(genepanelAttibuteDataPatient);
//                    }
//                }
//            }
            // process end

            var genepanelValues; 
            genepanelValues = {
                genepaneldata:GenePanelData,
                //genepaneldatapatient:GenePanelDataPatient,
                genepanel:genePanel
            };
            
            var geneDataColl = new GeneDataColl();

            geneDataColl.fetch({
		type: "POST",
		data: {
			cancer_study_id: cancer_study_id_selected,
			oql: $('#gene_list').val(),
			case_list: window.PortalGlobals.getCases(),
			geneticProfileIds: window.PortalGlobals.getGeneticProfiles(),
			z_score_threshold: window.PortalGlobals.getZscoreThreshold(),
			rppa_score_threshold: window.PortalGlobals.getRppaScoreThreshold()
		},
		success: function (response) {
			(function invokeDataManager() {
				var genes = {};
				_.each(response.models, function(d) {
					genes[d.attributes.gene] = true;
				});
				genes = Object.keys(genes);
				window.PortalGlobals.setGeneData(geneDataColl.toJSON());
				window.PortalDataColl.setOncoprintData(utils.process_data(response.toJSON(), genes));
				PortalDataColl.setOncoprintStat(utils.alteration_info(geneDataColl.toJSON()));
			})();
			$('#outer_loader_img').hide();
			$('#oncoprint #everything').show();
                        
                        var gene_data = response.toJSON();
                        //get to process data from gene panel
                        if(genepanelValues.genepaneldata.length>0){
                            for(var i=0; i < gene_data.length; i++)
                            {
                                var geneIndexValue;
                                var genepanelStableId;
                                if(genepanelValues.genepaneldata[i] !== undefined)
                                {
                                    genepanelStableId = genepanelValues.genepaneldata[i].attr_val;
                                }
                                else
                                {
                                    genepanelStableId = undefined;
                                }

                                for(var j= 0; j < config.gene_order.length; j++)
                                {
                                    if(genepanelStableId !== undefined)
                                    {
                                        geneIndexValue = _.find(genepanelValues.genepanel[genepanelStableId].geneList, function(gene){ return gene === gene_data[i+j*gene_data.length].gene; }); 
                                    }
                                    else
                                    {
                                        geneIndexValue = false;
                                    }

                                    if(gene_data[i+j*gene_data.length].gene !== undefined && !geneIndexValue && gene_data[i+j*gene_data.length].mutation === undefined && gene_data[i+j*gene_data.length].cna === undefined)
                                    {
                                        gene_data[i+j*gene_data.length].NA = true; 
                                    }
                                }
                            }
                        }
                        //process gene panel end
                        
			window.onc_obj = setUpOncoprint('oncoprint_body', {
				sample_to_patient: window.PortalGlobals.getPatientSampleIdMap(),
				gene_data: gene_data,
				toolbar_selector: '#oncoprint-diagram-toolbar-buttons',
				toolbar_fade_hitzone_selector: '#oncoprint',
				sample_list: window.PortalGlobals.getCases().trim().split(/\s+/),
				cancer_study_id: cancer_study_id_selected,
				gene_order: window.PortalGlobals.getGeneListString().split(/\s+/),

				load_clinical_tracks: true,
				swap_patient_sample: true,
				sort_by: true,

				link_out_in_tooltips: true,
				percent_altered_indicator_selector: '#altered_value',
			});
		}
	});
    });
});