/**
 * Generate the plots tab data object
 * and cache every generated data set in a global variable.
 * @type {*}
 */

var PlotsData = (function(){
    var paramsGetProfiles = {
        cancer_study_id: cancer_study_id
    };

    var paramsGetProfileData = {
        gene_list: gene_list_str,
        genetic_profile_id: "ov_tcga_mutations",
        case_set_id: case_set_id
    };

    console.log(paramsGetProfileData);

    var getGeneticProfileCallback = function(result) {
        console.log("getGeneticProfileCallback");
        console.log(result);
    }
    var getProfileDataCallback = function(result) {
        console.log("getProfileDataCallback");
        console.log(result);
    }

    return {
        init: function() {
            $.post("getGeneticProfile.json", paramsGetProfiles, getGeneticProfileCallback, "json");
            $.post("getProfileData.json", paramsGetProfileData, getProfileDataCallback, "json");
        }
    };

}());









