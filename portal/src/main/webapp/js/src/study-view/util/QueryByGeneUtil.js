var QueryByGeneUtil  = (function() {
    function addFormField(formId, itemName, itemValue){
        //  event.preventDefault();
        //var newItem = document.createElement("input");
        //newItem.name = itemName;
        //newItem.type = "text";
        //newItem.value = itemValue;
        //
        //$(formId).append(newItem);

        $('<input>').attr({
            type: 'hidden',
            //id: itemName,
            value: itemValue,
            name: itemName
        }).appendTo(formId)

    }

    function addStudyViewFields(){
        var formId = "#study-view-form";
        addFormField(formId,"gene_set_choice","user-defined-list");
        addFormField(formId,"gene_list",QueryByGeneTextArea.getGenes());

        addFormField(formId,"cancer_study_list", window.cancerStudyId);
        addFormField(formId,"Z_SCORE_THRESHOLD", 2.0);
        addFormField(formId,"genetic_profile_ids_PROFILE_MUTATION_EXTENDED",window.mutationProfileId);
        addFormField(formId,"genetic_profile_ids_PROFILE_COPY_NUMBER_ALTERATION",window.cnaProfileId);
        addFormField(formId,"clinical_param_selection",null);
        addFormField(formId,"data_priority",0);
        addFormField(formId,"tab_index","tab_visualize");
        addFormField(formId,"Action","Submit");
    }

    return{
        addStudyViewFields: addStudyViewFields
    }

})();