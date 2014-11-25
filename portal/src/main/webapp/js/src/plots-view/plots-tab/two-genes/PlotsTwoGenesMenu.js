var PlotsTwoGenesMenu = (function(){

    var content = {
        plots_type_list : {
            "mrna" : { value : "mrna", name :  "mRNA Expression" },
            "copy_no" : { value : "copy_no", name :  "Copy Number Alteration" },
            "methylation" : { value : "methylation", name :  "DNA Methylation" },
            "rppa" : { value : "rppa", name :  "RPPA Protein Level" }
        },
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

    function mergeList(arrX, arrY) {
        var result = [];
        var _arrY = [];
        $.each(arrY, function(index, val) {
            _arrY.push(val[0]);
        });
        $.each(arrX, function(index, val) {
            if (_arrY.indexOf(val[0]) !== -1) {
                result.push(arrX[index]);
            }
        });
        return result;
    }

    function fetchFrameData(geneX, geneY) {
        content.genetic_profile_mutations = Plots.getGeneticProfiles(geneX).genetic_profile_mutations;
        content.genetic_profile_mrna = mergeList(
            Plots.getGeneticProfiles(geneX).genetic_profile_mrna,
            Plots.getGeneticProfiles(geneY).genetic_profile_mrna
        );
        content.genetic_profile_copy_no = mergeList(
            Plots.getGeneticProfiles(geneX).genetic_profile_copy_no,
            Plots.getGeneticProfiles(geneY).genetic_profile_copy_no
        );
        content.genetic_profile_dna_methylation = mergeList(
            Plots.getGeneticProfiles(geneX).genetic_profile_dna_methylation,
            Plots.getGeneticProfiles(geneY).genetic_profile_dna_methylation
        );
        content.genetic_profile_rppa = mergeList(
            Plots.getGeneticProfiles(geneX).genetic_profile_rppa,
            Plots.getGeneticProfiles(geneY).genetic_profile_rppa
        );
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

    function setPlatFormDefaultSelection() {
        //----mRNA Priority List: RNA Seq V2, RNA Seq, Z-scores
        //TODO: Changed hard coded html value
        if ($("#two_genes_plots_type").val() === "mrna") {
            $("#two_genes_platform > option").each(function() {
                if (this.text.toLowerCase().indexOf("z-scores")){
                    $(this).prop('selected', true);
                    return false;
                }
            });
            $("#two_genes_platform > option").each(function() {
                if (this.text.toLowerCase().indexOf("rna seq") !== -1 &&
                    this.text.toLowerCase().indexOf("z-scores") === -1){
                    $(this).prop('selected', true);
                    return false;
                }
            });
            $("#two_genes_platform > option").each(function() {
                if (this.text.toLowerCase().indexOf("rna seq v2") !== -1 &&
                    this.text.toLowerCase().indexOf("z-scores") === -1){
                    $(this).prop('selected', true);
                    return false;
                }
            });
        }

        //----DNA methylation priority list: hm450, hm27
        if ($("#two_genes_plots_type").val() === "methylation") {
            $('#two_genes_platform > option').each(function() {
                if (this.text.toLowerCase().indexOf("hm450") !== -1) {
                    $(this).prop('selected', true);
                    return false;
                }
            });
        }
    }

    function drawPlatFormList() {
        $("#two_genes_platform_select_div").empty();
        $("#two_genes_platform_select_div").append(
            "<select id='two_genes_platform' onchange='PlotsTwoGenesView.init();PlotsTwoGenesMenu.updateLogScaleCheckBox();' class='plots-select'>");

        if ($("#two_genes_plots_type").val() === "mrna") {
            content.genetic_profile_mrna.forEach (function (profile) {
                $("#two_genes_platform")
                    .append("<option value='" + profile[0] + "|" + profile[2] + "'>" + profile[1] + "</option>");
            });
            setPlatFormDefaultSelection();
        } else if ($("#two_genes_plots_type").val() === "copy_no") {
            content.genetic_profile_copy_no.forEach (function (profile) {
                if (!dataIsDiscretized(profile[1])) {
                    $("#two_genes_platform")
                        .append("<option value='" + profile[0] + "|" + profile[2] + "'>" + profile[1] + "</option>");
                }
            });
        } else if ($("#two_genes_plots_type").val() === "methylation") {
            content.genetic_profile_dna_methylation.forEach (function (profile) {
                $("#two_genes_platform")
                    .append("<option value='" + profile[0] + "|" + profile[2] + "'>" + profile[1] + "</option>");
            });
        } else if ($("#two_genes_plots_type").val() === "rppa") {
            content.genetic_profile_rppa.forEach (function (profile) {
                $("#two_genes_platform")
                    .append("<option value='" + profile[0] + "|" + profile[2] + "'>" + profile[1] + "</option>");
            });
        }
        $("#two_genes_platform_select_div").append("</select>");
    }

    function updateLogScaleCheckBox() {
        $("#two_genes_apply_log_scale_div_x").empty();
        $("#two_genes_apply_log_scale_div_y").empty();
        var _str_x = "<input type='checkbox' id='two_genes_log_scale_option_x' checked onchange='PlotsTwoGenesView.updateLogScaleX();'/> log scale - x axis";
        var _str_y = "<input type='checkbox' id='two_genes_log_scale_option_y' checked onchange='PlotsTwoGenesView.updateLogScaleY();'/> log scale - y axis";
        if (($("#two_genes_plots_type").val() === "mrna" &&
             $("#two_genes_platform option:selected").val().toUpperCase().indexOf(("rna_seq").toUpperCase()) !== -1 &&
             $("#two_genes_platform option:selected").val().toUpperCase().indexOf(("zscores").toUpperCase()) === -1)) {
            $("#two_genes_apply_log_scale_div_x").append(_str_x);
            $("#two_genes_apply_log_scale_div_y").append(_str_y);
        }
    }

    function generatePlotsTypeList() {
        $("#two_genes_plots_type").empty();
        appendDropDown("#two_genes_plots_type", content.plots_type_list.mrna.value, content.plots_type_list.mrna.name);
        if (content.genetic_profile_copy_no.length !== 0) {
            var _flag = false;
            $.each(content.genetic_profile_copy_no, function(index, val) {
                if (!dataIsDiscretized(val[1])) {
                    _flag = true;
                }
            });     //If contains continuous data type
            if (_flag) {
                appendDropDown("#two_genes_plots_type", content.plots_type_list.copy_no.value, content.plots_type_list.copy_no.name);
            }
        }
        if (content.genetic_profile_dna_methylation.length !== 0) {
            appendDropDown("#two_genes_plots_type", content.plots_type_list.methylation.value, content.plots_type_list.methylation.name);
        }
        if (content.genetic_profile_rppa.length !== 0) {
            appendDropDown("#two_genes_plots_type", content.plots_type_list.rppa.value, content.plots_type_list.rppa.name);
        }
    }

    function generateGeneList() {
        //TODO: Enable this view only when there are >2 genes!
        //TODO: Always make sure these are two different genes
        generateList("geneX", gene_list);
        //shift the genelist (temporary solution)
        var tmp_gene_list = jQuery.extend(true, [], gene_list);
        var tmp_gene_holder = tmp_gene_list.pop();
        tmp_gene_list.unshift(tmp_gene_holder);
        generateList("geneY", tmp_gene_list);
    }

    return {
        init: function() {
            generateGeneList();
            fetchFrameData(document.getElementById("geneX").value, document.getElementById("geneY").value);
            generatePlotsTypeList();
            drawPlatFormList();
            updateLogScaleCheckBox();
        },
        updateMenu: function() {
            fetchFrameData(document.getElementById("geneX").value, document.getElementById("geneY").value);
            generatePlotsTypeList();
            drawPlatFormList();
            updateLogScaleCheckBox();
        },
        updateDataType: function() {
            fetchFrameData(document.getElementById("geneX").value, document.getElementById("geneY").value);
            drawPlatFormList();
            updateLogScaleCheckBox();
        },
        updateLogScaleCheckBox: updateLogScaleCheckBox
    };
}());      //Closing PlotsTwoGenesMenu
