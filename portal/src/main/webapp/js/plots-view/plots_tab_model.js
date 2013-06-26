/**
 * Generate the plots tab data object
 * and cache every generated data set in a global variable.
 * @type {*}
 */

var Plots = (function(){

    var genetic_profiles = {
            genetic_profile_mutations : [],
            genetic_profile_mrna : [],
            genetic_profile_copy_no : [],
            genetic_profile_rppa : [],
            genetic_profile_dna_methylation : []
        };

    var paramsGetProfiles = {
        cancer_study_id: cancer_study_id
    };


    var getGeneticProfileCallback = function(result) {

        for (var key in result) {
            var obj = result[key];
            var profile_type = obj.GENETIC_ALTERATION_TYPE;
            if (profile_type === "MUTATION_EXTENDED") {
                genetic_profiles.genetic_profile_mutations.push([obj.STABLE_ID, obj.NAME]);
            } else if(profile_type === "COPY_NUMBER_ALTERATION") {
                genetic_profiles.genetic_profile_copy_no.push([obj.STABLE_ID, obj.NAME]);
            } else if(profile_type === "MRNA_EXPRESSION") {
                genetic_profiles.genetic_profile_mrna.push([obj.STABLE_ID, obj.NAME]);
            } else if(profile_type === "METHYLATION") {
                genetic_profiles.genetic_profile_dna_methylation.push([obj.STABLE_ID, obj.NAME]);
            } else if(profile_type === "PROTEIN_ARRAY_PROTEIN_LEVEL") {
                genetic_profiles.genetic_profile_rppa.push([obj.STABLE_ID, obj.NAME]);
            }
        }

        PlotsMenu.init();
        PlotsMenu.update();
        PlotsTwoGenesMenu.init();
        PlotsTwoGenesMenu.update();
        PlotsCustomMenu.init();
        PlotsCustomMenu.update();

        var paramsGetProfileData = {
            gene_list: gene_list_str,
            genetic_profile_id: "prad_tcga_mutations prad_tcga_gistic",
            case_set_id: case_set_id
        };
        $.post("getProfileData.json", paramsGetProfileData, getProfileDataCallback, "json");
    }
    var getProfileDataCallback = function(result) {
    }

    return {
        init: function() {
            $.post("getGeneticProfile.json", paramsGetProfiles, getGeneticProfileCallback, "json");
        },
        getGeneticProfiles: function() {
            return genetic_profiles;
        }
    };


}());









