var ids = {
    sidebar: {
        "x": {
            div: "plots-sidebar-x-div",
            data_type: "plots-x-data-type",
            spec_div : "plots-x-spec",
            gene : "plots-tab-x-gene",
            profile_type: "plots-tab-x-profile-type",
            profile_name: "plots-tab-x-profile-name",
            log_scale: "plots-tab-x-log-scale",
            clin_attr: "plots-tab-x-clinical-attribute"
        },
        "y": {
            div: "plots-sidebar-y-div",
            data_type: "plots-y-data-type",
            spec_div : "plots-y-spec",
            gene : "plots-tab-y-gene",
            profile_type: "plots-tab-y-profile-type",
            profile_name: "plots-tab-y-profile-name",
            log_scale: "plots-tab-y-log-scale",
            clin_attr: "plots-tab-y-clinical-attribute"
        }
    },
    main_view: {
        div: "plots-box"
    }
};

var d3_class = {
    x : {
        axis: "plots-x-axis-class",
        axis_title: "plots-x-axis-title",
        title_help: "plots-x-axis-title-help"
    },
    y: {
        axis: "plots-y-axis-class",
        axis_title: "plots-y-axis-title",
        title_help: "plots-y-axis-title-help"
    },
    box_plots: "box-plots"

};

var vals = {
    data_type : {
        genetic: "genetic_profile",
        clin: "clinical_attribute"
    },
    profile_type : {
        "MUTATION_EXTENDED": "mutation",
        "COPY_NUMBER_ALTERATION": "Copy Number",
        "MRNA_EXPRESSION": "mRNA",
        "PROTEIN_ARRAY_PROTEIN_LEVEL": "RPPA Protein Level",
        "METHYLATION": "DNA Methylation"
    }
};




