var PlotsCustomMenu = (function(){

    var content = {
        plots_type_list : [
            { value : "mrna", name :  "mRNA Expression" },
            { value : "copy_no", name :  "Copy Number Alteration" },
            { value : "methylation", name :  "DNA Methylation" },
            { value : "rppa", name :  "RPPA Protein Level" }
        ],
        genetic_profile_mutations : [],
        genetic_profile_mrna : [],
        genetic_profile_copy_no : [],
        genetic_profile_rppa : [],
        genetic_profile_dna_methylation : []
    };

    function fetchFrameData() {
        content.genetic_profile_mutations = Plots.getGeneticProfiles().genetic_profile_mutations;
        content.genetic_profile_mrna = Plots.getGeneticProfiles().genetic_profile_mrna;
        content.genetic_profile_copy_no = Plots.getGeneticProfiles().genetic_profile_copy_no;
        content.genetic_profile_dna_methylation = Plots.getGeneticProfiles().genetic_profile_dna_methylation;
        content.genetic_profile_rppa = Plots.getGeneticProfiles().genetic_profile_rppa;
    }

    function appendDropDown(divId, value, text) {
        $(divId).append("<option value='" + value + "'>" + text + "</option>");
    }

    function generateList(selectId, options) {
        var select = document.getElementById(selectId);
        options.forEach(function(option){
            var el = document.createElement("option");
            el.textContent = option;
            el.value = option;
            select.appendChild(el);
        });
    }

    function updateXselection() {
        $("#custom_platform_select_div_x").empty();
        $("#custom_platform_select_div_x").append("<select id='custom_platform_x'>");

        if($("#custom_plots_type_x").val() === "mrna"){
            content.genetic_profile_mrna.forEach (function (profile) {
                $("#custom_platform_x")
                    .append("<option value='" + profile[0] + "'>" + profile[1] + "</option>");
            });
        } else if($("#custom_plots_type_x").val() === "copy_no"){
            content.genetic_profile_copy_no.forEach (function (profile) {
                $("#custom_platform_x")
                    .append("<option value='" + profile[0] + "'>" + profile[1] + "</option>");
            });
        } else if($("#custom_plots_type_x").val() === "methylation"){
            content.genetic_profile_dna_methylation.forEach (function (profile) {
                $("#custom_platform_x")
                    .append("<option value='" + profile[0] + "'>" + profile[1] + "</option>");
            });
        } else if($("#custom_plots_type_x").val() === "rppa"){
            content.genetic_profile_rppa.forEach (function (profile) {
                $("#custom_platform_x")
                    .append("<option value='" + profile[0] + "'>" + profile[1] + "</option>");
            });
        }

    }

    function updateYselection() {
        $("#custom_platform_select_div_y").empty();
        $("#custom_platform_select_div_y").append("<select id='custom_platform_y'>");

        if($("#custom_plots_type_y").val() === "mrna"){
            content.genetic_profile_mrna.forEach (function (profile) {
                $("#custom_platform_y")
                    .append("<option value='" + profile[0] + "'>" + profile[1] + "</option>");
            });
        } else if($("#custom_plots_type_y").val() === "copy_no"){
            content.genetic_profile_copy_no.forEach (function (profile) {
                $("#custom_platform_y")
                    .append("<option value='" + profile[0] + "'>" + profile[1] + "</option>");
            });
        } else if($("#custom_plots_type_y").val() === "methylation"){
            content.genetic_profile_dna_methylation.forEach (function (profile) {
                $("#custom_platform_y")
                    .append("<option value='" + profile[0] + "'>" + profile[1] + "</option>");
            });
        } else if($("#custom_plots_type_y").val() === "rppa"){
            content.genetic_profile_rppa.forEach (function (profile) {
                $("#custom_platform_y")
                    .append("<option value='" + profile[0] + "'>" + profile[1] + "</option>");
            });
        }
    }

    return {
        init: function() {
            fetchFrameData();
            generateList("custom_gene1", gene_list);
            generateList("custom_gene2", gene_list);
            content.plots_type_list.forEach( function(plots_type) {
                appendDropDown("#custom_plots_type_x", plots_type.value, plots_type.name);
            });
            content.plots_type_list.forEach( function(plots_type) {
                appendDropDown("#custom_plots_type_y", plots_type.value, plots_type.name);
            });
        },
        update: function(){
            updateXselection();
            updateYselection();
        }
    };
}());
