var PlotsCustomMenu = (function(){

    var content = {
        plots_type_list : {
            "mrna" : { value : "mrna", name :  "mRNA Expression" },
            "copy_no" : { value : "copy_no", name :  "Copy Number Alteration" },
            "methylation" : { value : "methylation", name :  "DNA Methylation" },
            "rppa" : { value : "rppa", name :  "RPPA Protein Level" }
        },
        geneX_genetic_profiles: {
            genetic_profile_mutations : [],
            genetic_profile_mrna : [],
            genetic_profile_copy_no : [],
            genetic_profile_rppa : [],
            genetic_profile_dna_methylation : []
        },
        geneY_genetic_profiles: {
            genetic_profile_mutations : [],
            genetic_profile_mrna : [],
            genetic_profile_copy_no : [],
            genetic_profile_rppa : [],
            genetic_profile_dna_methylation : []
        }
    };

    function fetchFrameData(geneX, geneY) {
        //geneX
        content.geneX_genetic_profiles.genetic_profile_mutations = Plots.getGeneticProfiles(geneX).genetic_profile_mutations;
        content.geneX_genetic_profiles.genetic_profile_mrna = Plots.getGeneticProfiles(geneX).genetic_profile_mrna;
        content.geneX_genetic_profiles.genetic_profile_copy_no = Plots.getGeneticProfiles(geneX).genetic_profile_copy_no;
        content.geneX_genetic_profiles.genetic_profile_dna_methylation = Plots.getGeneticProfiles(geneX).genetic_profile_dna_methylation;
        content.geneX_genetic_profiles.genetic_profile_rppa = Plots.getGeneticProfiles(geneX).genetic_profile_rppa;
        //geneY
        content.geneY_genetic_profiles.genetic_profile_mutations = Plots.getGeneticProfiles(geneY).genetic_profile_mutations;
        content.geneY_genetic_profiles.genetic_profile_mrna = Plots.getGeneticProfiles(geneY).genetic_profile_mrna;
        content.geneY_genetic_profiles.genetic_profile_copy_no = Plots.getGeneticProfiles(geneY).genetic_profile_copy_no;
        content.geneY_genetic_profiles.genetic_profile_dna_methylation = Plots.getGeneticProfiles(geneY).genetic_profile_dna_methylation;
        content.geneY_genetic_profiles.genetic_profile_rppa = Plots.getGeneticProfiles(geneY).genetic_profile_rppa;
    }

    function appendDropDown(divId, value, text) {
        $(divId).append("<option value='" + value + "'>" + text + "</option>");
    }

    function dataIsDiscretized(profileText) {
        if (profileText.indexOf("GISTIC") !== -1 ||
            profileText.indexOf("RAE") !== -1 ||
            profileText.indexOf("discretization") !== -1) {
            return true;
        }
        return false;
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

    function setPlatFormDefaultSelection(plotsTypeId, platformId) {
        //----mRNA Priority List: RNA Seq V2, RNA Seq, Z-scores
        //TODO: Changed hard coded html value
        if ($(plotsTypeId).val() === "mrna") {
            $(platformId + " > option").each(function() {
                if (this.text.toLowerCase().indexOf("z-scores")){
                    $(this).prop('selected', true);
                    return false;
                }
            });
            $(platformId + " > option").each(function() {
                if (this.text.toLowerCase().indexOf("rna seq") !== -1 &&
                    this.text.toLowerCase().indexOf("z-scores") === -1){
                    $(this).prop('selected', true);
                    return false;
                }
            });
            $(platformId + " > option").each(function() {
                if (this.text.toLowerCase().indexOf("rna seq v2") !== -1 &&
                    this.text.toLowerCase().indexOf("z-scores") === -1){
                    $(this).prop('selected', true);
                    return false;
                }
            });
        }

        //----dna methylation priority list: hm450, hm27
        if ($(plotsTypeId).val() === "methylation") {
            $(platformId + " > option").each(function() {
                if (this.text.toLowerCase().indexOf("hm450") !== -1) {
                    $(this).prop('selected', true);
                    return false;
                }
            });
        }
    }

    function updateXselection() {
        $("#custom_platform_select_div_x").empty();
        $("#custom_platform_select_div_x").append(
            "<select id='custom_platform_x' onchange='PlotsCustomView.init();PlotsCustomMenu.updateLogScaleCheckBoxX();' class='plots-select'>");
        if($("#custom_plots_type_x").val() === "mrna"){
            content.geneX_genetic_profiles.genetic_profile_mrna.forEach (function (profile) {
                $("#custom_platform_x")
                    .append("<option value='" + profile[0] + "|" + profile[2] + "'>" + profile[1] + "</option>");
            });
            setPlatFormDefaultSelection("#custom_plots_type_x", "#custom_platform_x");
        } else if($("#custom_plots_type_x").val() === "copy_no"){
            content.geneX_genetic_profiles.genetic_profile_copy_no.forEach (function (profile) {
                if (!dataIsDiscretized(profile[1])) {  //No listing of discretized data type (profile)
                    $("#custom_platform_x")
                        .append("<option value='" + profile[0] + "|" + profile[2] + "'>" + profile[1] + "</option>");
                }
            });
        } else if($("#custom_plots_type_x").val() === "methylation"){
            content.geneX_genetic_profiles.genetic_profile_dna_methylation.forEach (function (profile) {
                $("#custom_platform_x")
                    .append("<option value='" + profile[0] + "|" + profile[2] + "'>" + profile[1] + "</option>");
            });
            setPlatFormDefaultSelection("#custom_plots_type_x", "#custom_platform_x");
        } else if($("#custom_plots_type_x").val() === "rppa"){
            content.geneX_genetic_profiles.genetic_profile_rppa.forEach (function (profile) {
                $("#custom_platform_x")
                    .append("<option value='" + profile[0] + "|" + profile[2] + "'>" + profile[1] + "</option>");
            });
        }
    }

    function updateYselection() {
        $("#custom_platform_select_div_y").empty();
        $("#custom_platform_select_div_y").append(
            "<select id='custom_platform_y' onchange='PlotsCustomView.init();PlotsCustomMenu.updateLogScaleCheckBoxY();' class='plots-select'>");

        if($("#custom_plots_type_y").val() === "mrna"){
            content.geneY_genetic_profiles.genetic_profile_mrna.forEach (function (profile) {
                $("#custom_platform_y")
                    .append("<option value='" + profile[0] + "|" + profile[2] + "'>" + profile[1] + "</option>");
            });
            setPlatFormDefaultSelection("#custom_plots_type_y", "#custom_platform_y");
        } else if($("#custom_plots_type_y").val() === "copy_no"){
            content.geneY_genetic_profiles.genetic_profile_copy_no.forEach (function (profile) {
                if (!dataIsDiscretized(profile[1])) {  //No listing of discretized data type (profile)
                    $("#custom_platform_y")
                        .append("<option value='" + profile[0] + "|" + profile[2] + "'>" + profile[1] + "</option>");
                }
            });
        } else if($("#custom_plots_type_y").val() === "methylation"){
            content.geneY_genetic_profiles.genetic_profile_dna_methylation.forEach (function (profile) {
                $("#custom_platform_y")
                    .append("<option value='" + profile[0] + "|" + profile[2] + "'>" + profile[1] + "</option>");
            });
            setPlatFormDefaultSelection("#custom_plots_type_y", "#custom_platform_y");
        } else if($("#custom_plots_type_y").val() === "rppa"){
            content.geneY_genetic_profiles.genetic_profile_rppa.forEach (function (profile) {
                $("#custom_platform_y")
                    .append("<option value='" + profile[0] + "|" + profile[2] + "'>" + profile[1] + "</option>");
            });
        }
    }

    function updateLogScaleCheckBoxX() {
        var _str_x = "<input type='checkbox' id='custom_genes_log_scale_option_x' checked onchange='PlotsCustomView.updateLogScaleX();' /> log scale";
        $("#custom_genes_apply_log_scale_div_x").empty();
        if (($("#custom_plots_type_x").val() === "mrna" &&
            $("#custom_platform_x option:selected").val().toUpperCase().indexOf(("rna_seq").toUpperCase()) !== -1 &&
            $("#custom_platform_x option:selected").val().toUpperCase().indexOf(("zscores").toUpperCase()) === -1)) {
               $("#custom_genes_apply_log_scale_div_x").append(_str_x);
            }
    }

    function updateLogScaleCheckBoxY() {
        var _str_y = "<input type='checkbox' id='custom_genes_log_scale_option_y' checked onchange='PlotsCustomView.updateLogScaleY();' /> log scale";
        $("#custom_genes_apply_log_scale_div_y").empty();
        if (($("#custom_plots_type_y").val() === "mrna" &&
            $("#custom_platform_y option:selected").val().toUpperCase().indexOf(("rna_seq").toUpperCase()) !== -1 &&
            $("#custom_platform_y option:selected").val().toUpperCase().indexOf(("zscores").toUpperCase()) === -1)) {
            $("#custom_genes_apply_log_scale_div_y").append(_str_y);
        }
    }

    function generateGeneList() {
        generateList("custom_geneX", gene_list);
        var tmp_gene_list = jQuery.extend(true, [], gene_list);
        var tmp_gene_holder = tmp_gene_list.pop();
        tmp_gene_list.unshift(tmp_gene_holder);
        generateList("custom_geneY", tmp_gene_list);
    }

    function generatePlotsTypeList() {
        appendDropDown("#custom_plots_type_x", content.plots_type_list.mrna.value, content.plots_type_list.mrna.name);
        appendDropDown("#custom_plots_type_y", content.plots_type_list.mrna.value, content.plots_type_list.mrna.name);
        if (content.geneX_genetic_profiles.genetic_profile_copy_no.length !== 0) {
            var _flag = false;
            $.each(content.geneX_genetic_profiles.genetic_profile_copy_no, function(index, val) {
                if (!dataIsDiscretized(val[1])) {
                    _flag = true;
                }
            });     //If contains continuous data type
            if (_flag) {
                appendDropDown("#custom_plots_type_x", content.plots_type_list.copy_no.value, content.plots_type_list.copy_no.name);
            }
        }
        if (content.geneY_genetic_profiles.genetic_profile_copy_no.length !== 0) {
            var _flag = false;
            $.each(content.geneX_genetic_profiles.genetic_profile_copy_no, function(index, val) {
                if (!dataIsDiscretized(val[1])) {
                    _flag = true;
                }
            });     //If contains continuous data type
            if (_flag) {
                appendDropDown("#custom_plots_type_y", content.plots_type_list.copy_no.value, content.plots_type_list.copy_no.name);
            }
        }
        if (content.geneX_genetic_profiles.genetic_profile_dna_methylation.length !== 0) {
            appendDropDown("#custom_plots_type_x", content.plots_type_list.methylation.value, content.plots_type_list.methylation.name);
        }
        if (content.geneY_genetic_profiles.genetic_profile_dna_methylation.length !== 0) {
            appendDropDown("#custom_plots_type_y", content.plots_type_list.methylation.value, content.plots_type_list.methylation.name);
        }
        if (content.geneX_genetic_profiles.genetic_profile_rppa.length !== 0) {
            appendDropDown("#custom_plots_type_x", content.plots_type_list.rppa.value, content.plots_type_list.rppa.name);
        }
        if (content.geneY_genetic_profiles.genetic_profile_rppa.length !== 0) {
            appendDropDown("#custom_plots_type_y", content.plots_type_list.rppa.value, content.plots_type_list.rppa.name);
        }
    }

    return {
        init: function() {
            generateGeneList();
            fetchFrameData(document.getElementById("geneX").value, document.getElementById("geneY").value);
            generatePlotsTypeList();
            updateXselection();
            updateYselection();
            updateLogScaleCheckBoxX();
            updateLogScaleCheckBoxY();
        },
        updateX: function(){
            fetchFrameData(document.getElementById("geneX").value, document.getElementById("geneY").value);
            updateXselection();
            updateLogScaleCheckBoxX();
        },
        updateY: function(){
            fetchFrameData(document.getElementById("geneX").value, document.getElementById("geneY").value);
            updateYselection();
            updateLogScaleCheckBoxY();
        },
        updateLogScaleCheckBoxX: updateLogScaleCheckBoxX,
        updateLogScaleCheckBoxY: updateLogScaleCheckBoxY

    };
}());   //Closing PlotsCustomMenu
