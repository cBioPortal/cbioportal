var PlotsMenu = (function () {
    var content = {
            plots_type : {
                "mrna_copyNo" : {
                    value : "mrna_vs_copy_no",
                    text : "mRNA vs. Copy Number"
                },
                "mrna_methylation" : {
                    value : "mrna_vs_dna_methylation",
                    text : "mRNA vs. DNA Methylation"

                },
                "rppa_mrna" : {
                    value : "rppa_protein_level_vs_mrna",
                    text : "RPPA Protein Level vs. mRNA"
                },
                "mrna_clinical" : {
                    value : "mrna_vs_clinical",
                    text : "mRNA vs. Clinical attributes"
                }
            },
            data_type : {
                "mrna" : {
                    label: "- mRNA -",
                    value: "data_type_mrna",
                    genetic_profile : []
                },
                "copy_no" : {
                    label: "- Copy Number -",
                    value: "data_type_copy_no",
                    genetic_profile : []
                },
                "dna_methylation" : {
                    label: "- DNA Methylation -",
                    value: "data_type_dna_methylation",
                    genetic_profile : []

                },
                "rppa" : {
                    label: "- RPPA Protein Level -",
                    value: "data_type_rppa",
                    genetic_profile : []
                },
                "clinical" : {
                    label: "- Clinical Attributes - ",
                    value: "data_type_clinical",
                    attributes: []
                }
            }
        },
        status = {
            has_mrna : false,
            has_dna_methylation : false,
            has_rppa : false,
            has_copy_no : false,
            has_clinical_data : false
        };

    var Util = (function() {

        function appendDropDown(divId, value, text) {
            $(divId).append("<option value='" + value + "'>" + text + "</option>");
        }

        function toggleVisibilityX(elemId) {
            var e = document.getElementById(elemId);
            e.style.display = 'block';
            $("#" + elemId).append("<div id='one_gene_log_scale_x_div'></div>");
        }

        function toggleVisibilityY(elemId) {
            var e = document.getElementById(elemId);
            e.style.display = 'block';
            $("#" + elemId).append("<div id='one_gene_log_scale_y_div'></div>");
        }

        function toggleVisibilityHide(elemId) {
            var e = document.getElementById(elemId);
            e.style.display = 'none';
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

        return {
            appendDropDown: appendDropDown,
            toggleVisibilityX: toggleVisibilityX,
            toggleVisibilityY: toggleVisibilityY,
            toggleVisibilityHide: toggleVisibilityHide,
            generateList: generateList
        };

    }());

    function drawMenu() {

        $("#one_gene_type_specification").show();
        $("#plots_type").empty();
        $("#one_gene_platform_select_div").empty();
        //Plots Type field
        if (status.has_mrna && status.has_copy_no) {
            Util.appendDropDown(
                '#plots_type',
                content.plots_type.mrna_copyNo.value,
                content.plots_type.mrna_copyNo.text
            );
        }
        if (status.has_mrna && status.has_dna_methylation) {
            Util.appendDropDown(
                '#plots_type',
                content.plots_type.mrna_methylation.value,
                content.plots_type.mrna_methylation.text
            );
        }
        if (status.has_mrna && status.has_rppa) {
            Util.appendDropDown(
                '#plots_type',
                content.plots_type.rppa_mrna.value,
                content.plots_type.rppa_mrna.text
            );
        }
        if (status.has_mrna && status.has_clinical_data) {
            Util.appendDropDown(
                '#plots_type',
                content.plots_type.mrna_clinical.value,
                content.plots_type.mrna_clinical.text
            );
        }
        //Data Type Field : profile/attributes
        for (var key in content.data_type) {
            var singleDataTypeObj = content.data_type[key];
            $("#one_gene_platform_select_div").append(
                "<div id='" + singleDataTypeObj.value + "_dropdown' style='padding:5px;'>" +
                    "<label for='" + singleDataTypeObj.value + "'>" + singleDataTypeObj.label + "</label><br>" +
                    "<select id='" + singleDataTypeObj.value + "' onchange='PlotsView.init();PlotsMenu.updateLogScaleOption();' class='plots-select'></select></div>"
            );
            if (singleDataTypeObj.hasOwnProperty("genetic_profile")) {
                $.each(singleDataTypeObj.genetic_profile, function(index, item_profile) {
                    $("#" + singleDataTypeObj.value).append(
                        "<option value='" + item_profile[0] + "|" + item_profile[2] + "'>" + item_profile[1] + "</option>");
                });               
            } else if (singleDataTypeObj.hasOwnProperty("attributes")) { //for clinical data, we don't name it profile but attributes
                $.each(singleDataTypeObj.attributes, function(index, item_attribute) {
                    $("#" + singleDataTypeObj.value).append(
                        "<option value='" + item_attribute.attr_id + "|" + item_attribute.description + "'>" + item_attribute.display_name + "</option>");
                });
            }
        }
    }

    function drawErrMsgs() {
        $("#one_gene_type_specification").hide();
        $("#menu_err_msg").append("<h5>Profile data missing for generating this view.</h5>");
    }

    function setDefaultCopyNoSelection() {
        //-----Priority: discretized(gistic, rae), continuous
        //TODO: refactor
        $('#data_type_copy_no > option').each(function() {
            if (this.text.toLowerCase().indexOf("(rae)") !== -1) {
                $(this).prop('selected', true);
                return false;
            }
        });
        $("#data_type_copy_no > option").each(function() {
            if (this.text.toLowerCase().indexOf("gistic") !== -1) {
                $(this).prop('selected', true);
                return false;
            }
        });
        var userSelectedCopyNoProfile = "";
        $.each(geneticProfiles.split(/\s+/), function(index, value){
            if (value.indexOf("cna") !== -1 || value.indexOf("log2") !== -1 ||
                value.indexOf("CNA")!== -1 || value.indexOf("gistic") !== -1) {
                userSelectedCopyNoProfile = value;
                return false;
            }
        });
        $("#data_type_copy_no > option").each(function() {
            if (this.value === userSelectedCopyNoProfile){
                $(this).prop('selected', true);
                return false;
            }
        });
    }

    function setDefaultMrnaSelection() {
        var userSelectedMrnaProfile = "";  //from main query
        //geneticProfiles --> global variable, passing user selected profile IDs
        $.each(geneticProfiles.split(/\s+/), function(index, value){
            if (value.indexOf("mrna") !== -1) {
                userSelectedMrnaProfile = value;
                return false;
            }
        });

        //----Priority List: User selection, RNA Seq V2, RNA Seq, Z-scores
        $("#data_type_mrna > option").each(function() {
            if (this.text.toLowerCase().indexOf("z-scores") !== -1){
                $(this).prop('selected', true);
                return false;
            }
        });
        $("#data_type_mrna > option").each(function() {
            if (this.text.toLowerCase().indexOf("rna seq") !== -1 &&
                this.text.toLowerCase().indexOf("z-scores") === -1){
                $(this).prop('selected', true);
                return false;
            }
        });
        $("#data_type_mrna > option").each(function() {
            if (this.text.toLowerCase().indexOf("rna seq v2") !== -1 &&
                this.text.toLowerCase().indexOf("z-scores") === -1){
                $(this).prop('selected', true);
                return false;
            }
        });
        $("#data_type_mrna > option").each(function() {
            if (this.value === userSelectedMrnaProfile){
                $(this).prop('selected', true);
                return false;
            }
        });
    }

    function setDefaultMethylationSelection() {
        $('#data_type_dna_methylation > option').each(function() {
            if (this.text.toLowerCase().indexOf("hm450") !== -1) {
                $(this).prop('selected', true);
                return false;
            }
        });
    }

    function updateVisibility() {
        $("#one_gene_log_scale_x_div").remove();
        $("#one_gene_log_scale_y_div").remove();
        var currentPlotsType = $('#plots_type').val();
        if (currentPlotsType.indexOf("copy_no") !== -1) {
            Util.toggleVisibilityX("data_type_copy_no_dropdown");
            Util.toggleVisibilityY("data_type_mrna_dropdown");
            Util.toggleVisibilityHide("data_type_dna_methylation_dropdown");
            Util.toggleVisibilityHide("data_type_rppa_dropdown");
            Util.toggleVisibilityHide("data_type_clinical_dropdown");
        } else if (currentPlotsType.indexOf("dna_methylation") !== -1) {
            Util.toggleVisibilityX("data_type_dna_methylation_dropdown");
            Util.toggleVisibilityY("data_type_mrna_dropdown");
            Util.toggleVisibilityHide("data_type_copy_no_dropdown");
            Util.toggleVisibilityHide("data_type_rppa_dropdown");
            Util.toggleVisibilityHide("data_type_clinical_dropdown");
        } else if (currentPlotsType.indexOf("rppa") !== -1) {
            Util.toggleVisibilityX("data_type_mrna_dropdown");
            Util.toggleVisibilityY("data_type_rppa_dropdown");
            Util.toggleVisibilityHide("data_type_copy_no_dropdown");
            Util.toggleVisibilityHide("data_type_dna_methylation_dropdown");
            Util.toggleVisibilityHide("data_type_clinical_dropdown");
        } else if (currentPlotsType.indexOf("clinical") !== -1) {
            Util.toggleVisibilityX("data_type_clinical_dropdown");
            Util.toggleVisibilityY("data_type_mrna_dropdown");
            Util.toggleVisibilityHide("data_type_copy_no_dropdown");
            Util.toggleVisibilityHide("data_type_rppa_dropdown");
            Util.toggleVisibilityHide("data_type_dna_methylation_dropdown");
        }
        updateLogScaleOption();
    }

    function updateLogScaleOption() {
        $("#one_gene_log_scale_x_div").empty();
        $("#one_gene_log_scale_y_div").empty();
        var _str_x = "<input type='checkbox' id='log_scale_option_x' checked onchange='PlotsView.applyLogScaleX();'/> log scale";
        var _str_y = "<input type='checkbox' id='log_scale_option_y' checked onchange='PlotsView.applyLogScaleY();'/> log scale";
        if ($("#plots_type").val() === content.plots_type.mrna_copyNo.value) {
            if ($("#data_type_mrna option:selected").val().toUpperCase().indexOf(("rna_seq").toUpperCase()) !== -1 &&
                $("#data_type_mrna option:selected").val().toUpperCase().indexOf(("zscores").toUpperCase()) === -1) {
                $("#one_gene_log_scale_y_div").append(_str_y);
            }
        } else if ($("#plots_type").val() === content.plots_type.mrna_methylation.value) {
            if ($("#data_type_mrna option:selected").val().toUpperCase().indexOf(("rna_seq").toUpperCase()) !== -1 &&
                $("#data_type_mrna option:selected").val().toUpperCase().indexOf(("zscores").toUpperCase()) === -1) {
                $("#one_gene_log_scale_y_div").append(_str_y);
            }
        } else if ($("#plots_type").val() === content.plots_type.rppa_mrna.value) {
            if ($("#data_type_mrna option:selected").val().toUpperCase().indexOf(("rna_seq").toUpperCase()) !== -1 &&
                $("#data_type_mrna option:selected").val().toUpperCase().indexOf(("zscores").toUpperCase()) === -1) {
                $("#one_gene_log_scale_x_div").append(_str_x);
            }
        }
    }

    function fetchFrameContent(selectedGene) {
        content.data_type.mrna.genetic_profile = Plots.getGeneticProfiles(selectedGene).genetic_profile_mrna;
        content.data_type.copy_no.genetic_profile = Plots.getGeneticProfiles(selectedGene).genetic_profile_copy_no;
        content.data_type.dna_methylation.genetic_profile = Plots.getGeneticProfiles(selectedGene).genetic_profile_dna_methylation;
        content.data_type.rppa.genetic_profile = Plots.getGeneticProfiles(selectedGene).genetic_profile_rppa;
        content.data_type.clinical.attributes = Plots.getClinicalAttributes();
        status.has_mrna = (content.data_type.mrna.genetic_profile.length !== 0);
        status.has_copy_no = (content.data_type.copy_no.genetic_profile.length !== 0);
        status.has_dna_methylation = (content.data_type.dna_methylation.genetic_profile.length !== 0);
        status.has_rppa = (content.data_type.rppa.genetic_profile.length !== 0);
        status.has_clinical_data = (content.data_type.clinical.attributes.length !== 0);
    }

    return {
        init: function () {
            $("#menu_err_msg").empty();
            fetchFrameContent(gene_list[0]);
            Util.generateList("gene", gene_list);
            if(status.has_mrna && (status.has_copy_no || status.has_dna_methylation || status.has_rppa || status.has_clinical_data)) {
                drawMenu();
                setDefaultMrnaSelection();
                setDefaultCopyNoSelection();
                setDefaultMethylationSelection();
                updateVisibility();
            } else {
                drawErrMsgs();
            }
        },
        updateMenu: function() {
            $("#menu_err_msg").empty();
            fetchFrameContent(document.getElementById("gene").value);
            if(status.has_mrna && (status.has_copy_no || status.has_dna_methylation || status.has_rppa || status.has_clinical_data)) {
                drawMenu();
                setDefaultMrnaSelection();
                setDefaultCopyNoSelection();
                setDefaultMethylationSelection();
                updateVisibility();
            } else {
                drawErrMsgs();
            }
        },
        updateDataType: function() {
            setDefaultMrnaSelection();
            setDefaultCopyNoSelection();
            setDefaultMethylationSelection();
            updateVisibility();
        },
        updateLogScaleOption: updateLogScaleOption,
        getStatus: function() {
            return status;
        }
    };
}()); //Closing PlotsMenu