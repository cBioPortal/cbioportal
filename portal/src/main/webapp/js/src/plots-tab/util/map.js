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
            clin_attr: "plots-tab-y-clinical-attribute",
            lock_gene: "plots-tab-y-lock-gene"
        },
        "util": {
            div: "plots-sidebar-util-div",
            case_id_search: "case_id_search_keyword",
            mutation_search: "mutation_search_keyword",
            view_switch: "mutation_details_vs_gistic_view",
            download_buttons: "download_buttons"
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
    box_plots: "box-plots",
    dots: "dots"

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
        "PROTEIN_LEVEL": "Protein Level",
        "METHYLATION": "DNA Methylation"
    }
};

var genetic_profile_type_priority_list = [ //from low to high
    "MUTATION_EXTENDED",
    "METHYLATION",
    "PROTEIN_LEVEL",
    "COPY_NUMBER_ALTERATION",
    "MRNA_EXPRESSION"    
];

var genetic_profile_name_priority_list = [ //from low to high
    "hm450", //dna methylation 
    "gistic", //copy no
    "Zscores", "rna_seq", "rna_seq_v2" //mrna
];




