var PlotsTwoGenesMenu = (function(){

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

    function generateList(selectId, options) {
        var select = document.getElementById(selectId);
        options.forEach(function(option){
            var el = document.createElement("option");
            el.textContent = option;
            el.value = option;
            select.appendChild(el);
        });
    }

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

    return {
        init: function() {
            generateList("gene1", gene_list);
            generateList("gene2", gene_list);
            fetchFrameData();
            content.plots_type_list.forEach( function(plots_type) {
                appendDropDown("#two_genes_plots_type", plots_type.value, plots_type.name);
            });
        },
        update: function() {

            $("#two_genes_platform_select_div").empty();
            $("#two_genes_platform_select_div").append("<select id='two_genes_platform'>");

            if($("#two_genes_plots_type").val() === "mrna"){
                content.genetic_profile_mrna.forEach (function (profile) {
                    $("#two_genes_platform")
                        .append("<option value='" + profile[0] + "'>" + profile[1] + "</option>");
                });
            } else if($("#two_genes_plots_type").val() === "copy_no"){
                content.genetic_profile_copy_no.forEach (function (profile) {
                    $("#two_genes_platform")
                        .append("<option value='" + profile[0] + "'>" + profile[1] + "</option>");
                });
            } else if($("#two_genes_plots_type").val() === "methylation"){
                content.genetic_profile_dna_methylation.forEach (function (profile) {
                    $("#two_genes_platform")
                        .append("<option value='" + profile[0] + "'>" + profile[1] + "</option>");
                });
            } else if($("#two_genes_plots_type").val() === "rppa"){
                content.genetic_profile_rppa.forEach (function (profile) {
                    $("#two_genes_platform")
                        .append("<option value='" + profile[0] + "'>" + profile[1] + "</option>");
                });
            }
            $("#two_genes_platform_select_div").append("</select>");


        }
    };
}());
