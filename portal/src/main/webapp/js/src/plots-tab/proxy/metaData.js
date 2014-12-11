var metaData = (function() {
    
    var datum_genetic_profile_meta = {
            type: "",
            id: "",
            name: "",
            description: ""
        },
        datum_clinical_attr_meta = {
            id: "",
            name: "",
            description: ""
        },
        geneticProfiles = [],
        clinicalAttrs = [],
        retrieve_status = -1; //data not yet retrieved (-1), retrieved (1)

    function fetchProfileMetaData() {
        var paramsGetProfiles = {
            cancer_study_id: window.PortalGlobals.getCancerStudyId(),
        };
        $.post("getGeneticProfile.json", paramsGetProfiles, fetchClinicalAttrMetaData, "json");  
    }

    function fetchClinicalAttrMetaData(profileMetaDataResult) {
        var paramsGetClinicalAttributes = {
            cmd : "getAllClinicalData",
            cancer_study_id: window.PortalGlobals.getCancerStudyId(),
            case_set_id : window.PortalGlobals.getCaseSetId(),
            format : "json"
        };
        $.post("webservice.do", paramsGetClinicalAttributes, function(result) {
            registerMetaData(result.attributes, profileMetaDataResult);
        }, "json");
    }

    function registerMetaData(clinicalAttrMetaDataResult, profileMetaDataResult) {
        for (var key in profileMetaDataResult) {
            var obj = profileMetaDataResult[key]; 
            var _datum = jQuery.extend(true, {}, datum_genetic_profile_meta);
            _datum.type = obj.GENETIC_ALTERATION_TYPE;
            _datum.id = obj.STABLE_ID;
            _datum.name = obj.NAME;
            _datum.description = obj.DESCRIPTION;
            geneticProfiles.push(_datum);
        }
        $.each(clinicalAttrMetaDataResult, function(index, obj) {
            var _datum = jQuery.extend(true, {}, datum_clinical_attr_meta);
            _datum.id = obj.attr_id;
            _datum.name = obj.display_name;
            _datum.description = obj.description;
            clinicalAttrs.push(_datum);
        });
        retrieve_status = 1;
    }
    
    return {
        fetch: function() {
            retrieve_status = -1;
            fetchProfileMetaData();
        },
        getClinAttrsMeta: function() {
            return clinicalAttrs;
        },
        getGeneticProfilesMeta: function() {
            return geneticProfiles;
        },
        getRetrieveStatus: function() {
            return retrieve_status;
        }
    };
    
}());
