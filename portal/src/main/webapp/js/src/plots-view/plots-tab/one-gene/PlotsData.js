    var PlotsData = (function() {

        var caseSetLength = 0,
            dotsGroup = [],
            singleDot = {
                caseId : "",
                xVal : "",
                yVal : "",
                mutationDetail : "",  //Mutation ID
                mutationType : "",
                gisticType : "" //Discretized(GISTIC/RAE) Annotation
            },   //Template for single dot
            status = {
                xHasData: false,
                yHasData: false,
                combineHasData: false
            },
            attr = {
                min_x: 0,
                max_x: 0,
                min_y: 0,
                max_y: 0,
                pearson: 0,
                spearman: 0
            };

        function fetchPlotsData(profileDataResult) {

            var resultObj = profileDataResult[PlotsView.getUserSelection().gene];
            for (var key in resultObj) {  //key is case id
                caseSetLength += 1;
                var _obj = resultObj[key];
                var _singleDot = jQuery.extend(true, {}, singleDot);
                _singleDot.caseId = key;
                //TODO: remove hard-coded menu content
                if (Util.plotsTypeIsCopyNo()) {
                    _singleDot.xVal = _obj[PlotsView.getUserSelection().copy_no_type];
                    _singleDot.yVal = _obj[PlotsView.getUserSelection().mrna_type];
                } else if (Util.plotsTypeIsMethylation()) {
                    _singleDot.xVal = _obj[PlotsView.getUserSelection().dna_methylation_type];
                    _singleDot.yVal = _obj[PlotsView.getUserSelection().mrna_type];
                } else if (Util.plotsTypeIsRPPA()) {
                    _singleDot.xVal = _obj[PlotsView.getUserSelection().mrna_type];
                    _singleDot.yVal = _obj[PlotsView.getUserSelection().rppa_type];
                } else if (Util.plotsTypeIsClinical()) {
                    var _xVal = Plots.getClinicalData(key, PlotsView.getUserSelection().clinical_attribute);
                    if (Util.plotsIsDiscretized()) {
                        _singleDot.xVal = clinicalDataTranslator.translateText(PlotsView.getUserSelection().clinical_attribute, _xVal)
                    } else {
                        _singleDot.xVal = _xVal;
                    }
                    _singleDot.yVal = _obj[PlotsView.getUserSelection().mrna_type];
                }
                if (_obj.hasOwnProperty(cancer_study_id + "_mutations")) {
                    _singleDot.mutationDetail = _obj[cancer_study_id + "_mutations"];
                    _singleDot.mutationType = _obj[cancer_study_id + "_mutations"]; //Translate into type later
                } else {
                    _singleDot.mutationType = "non";
                }
                if (!Util.isEmpty(_obj[PlotsView.getDiscretizedDataTypeIndicator()])) {
                    _singleDot.gisticType = PlotsView.getText().gistic_txt_val[_obj[PlotsView.getDiscretizedDataTypeIndicator()]];
                } else {
                    _singleDot.gisticType = "NaN";
                }
                //Set Data Status
                if (!Util.isEmpty(_singleDot.xVal)) {
                    status.xHasData = true;
                }
                if (!Util.isEmpty(_singleDot.yVal)) {
                    status.yHasData = true;
                }
                //Push into the dots array
                if (!Util.isEmpty(_singleDot.xVal) &&
                    !Util.isEmpty(_singleDot.yVal)) {
                    dotsGroup.push(_singleDot);
                    status.combineHasData = true;
                }
            }
        }

        function translateMutationType(mutationTypeResult) {
            //Map mutation type for each individual cases
            var mutationDetailsUtil =
                new MutationDetailsUtil(new MutationCollection(mutationTypeResult));
            var mutationMap = mutationDetailsUtil.getMutationCaseMap();
            $.each(dotsGroup, function(index, dot) {
                if (!mutationMap.hasOwnProperty(dot.caseId.toLowerCase())) {
                    dot.mutationType = mutationStyle.non.typeName;
                } else {
                    var _mutationTypes = []; //one case can have multi-mutations
                    $.each(mutationMap[dot.caseId.toLowerCase()], function (index, val) {
                        if ((val.mutationType === "Frame_Shift_Del")||(val.mutationType === "Frame_Shift_Ins")) {
                            _mutationTypes.push(mutationStyle.frameshift.typeName);
                        } else if ((val.mutationType === "In_Frame_Del")||(val.mutationType === "In_Frame_Ins")) {
                            _mutationTypes.push(mutationStyle.in_frame.typeName);
                        } else if ((val.mutationType === "Missense_Mutation")||(val.mutationType === "Missense")) {
                            _mutationTypes.push(mutationStyle.missense.typeName);
                        } else if ((val.mutationType === "Nonsense_Mutation")||(val.mutationType === "Nonsense")) {
                            _mutationTypes.push(mutationStyle.nonsense.typeName);
                        } else if ((val.mutationType === "Splice_Site")||(val.mutationType === "Splice_Site_SNP")) {
                            _mutationTypes.push(mutationStyle.splice.typeName);
                        } else if (val.mutationType === "NonStop_Mutation") {
                            _mutationTypes.push(mutationStyle.nonstop.typeName);
                        } else if (val.mutationType === "Translation_Start_Site") {
                            _mutationTypes.push(mutationStyle.nonstart.typeName);
                        } else { //Fusion etc. new mutation types
                            _mutationTypes.push(mutationStyle.other.typeName);
                        }
                    });
                    //Re-order mutations in one case based on priority list
                    var mutationPriorityList = [];
                    mutationPriorityList[mutationStyle.frameshift.typeName] = "0";
                    mutationPriorityList[mutationStyle.in_frame.typeName] = "1";
                    mutationPriorityList[mutationStyle.missense.typeName] = "2";
                    mutationPriorityList[mutationStyle.nonsense.typeName] = "3";
                    mutationPriorityList[mutationStyle.splice.typeName] = "4";
                    mutationPriorityList[mutationStyle.nonstop.typeName] = "5";
                    mutationPriorityList[mutationStyle.nonstart.typeName] = "6";
                    mutationPriorityList[mutationStyle.other.typeName] = "7";
                    mutationPriorityList[mutationStyle.non.typeName] = "8";
                    var _primaryMutation = _mutationTypes[0];
                    $.each(_mutationTypes, function(index, val) {
                        if (mutationPriorityList[_primaryMutation] > mutationPriorityList[val]) {
                            _primaryMutation = val;
                        }
                    });
                    dot.mutationType = _primaryMutation;
                }
            });
        }

        function prioritizeMutatedCases() {
            var nonMutatedData = [];
            var mutatedData= [];
            var dataBuffer = [];
            dotsGroup.forEach (function(entry) {
                if (!Util.isEmpty(entry.mutationDetail)) {
                    mutatedData.push(entry);
                } else {
                    nonMutatedData.push(entry);
                }
            });
            nonMutatedData.forEach (function(entry) {
                dataBuffer.push(entry);
            });
            mutatedData.forEach (function(entry) {
                dataBuffer.push(entry);
            });
            dotsGroup = dataBuffer;
        }

        function analyseData() {
            var tmp_xData = [];
            var tmp_xIndex = 0;
            var tmp_yData = [];
            var tmp_yIndex = 0;
            for (var j = 0; j < dotsGroup.length; j++){
                if (!Util.isEmpty(dotsGroup[j].xVal) &&
                    !Util.isEmpty(dotsGroup[j].yVal)) {
                    tmp_xData[tmp_xIndex] = dotsGroup[j].xVal;
                    tmp_xIndex += 1;
                    tmp_yData[tmp_yIndex] = dotsGroup[j].yVal;
                    tmp_yIndex += 1;
                }
            }
            attr.min_x = Math.min.apply(Math, tmp_xData);
            attr.max_x = Math.max.apply(Math, tmp_xData);
            attr.min_y = Math.min.apply(Math, tmp_yData);
            attr.max_y = Math.max.apply(Math, tmp_yData);

            //Calculate the co-express/correlation scores
            //(When data is continuous)
            if (!Util.plotsIsDiscretized()) {
                var tmpGeneXcoExpStr = "",
                    tmpGeneYcoExpStr = "";
                $.each(PlotsData.getDotsGroup(), function(index, obj) {
                    tmpGeneXcoExpStr += obj.xVal + " ";
                    tmpGeneYcoExpStr += obj.yVal + " ";
                });
                var paramsCalcCoexp = {
                    gene_x : tmpGeneXcoExpStr,
                    gene_y : tmpGeneYcoExpStr
                };
                $.post("calcCoExp.do", paramsCalcCoexp, getCalcCoExpCallBack, "json");
            } else {
                $('#view_title').show();
                $('#plots_box').show();
                $('#loading-image').hide();
                View.init();                
            }
        }

        function getCalcCoExpCallBack(result) {
            //Parse the coexp scoring result
            var tmpArrCoexpScores = result.split(" ");
            attr.pearson = parseFloat(tmpArrCoexpScores[0]).toFixed(3);
            attr.spearman = parseFloat(tmpArrCoexpScores[1]).toFixed(3);
            $('#view_title').show();
            $('#plots_box').show();
            $('#loading-image').hide();
            View.init();
        }

        return {
            init: function(profileDataResult, mutationTypeResult) {
                status.xHasData = false;
                status.yHasData = false;
                status.combineHasData = false;
                caseSetLength = 0;
                dotsGroup.length = 0;
                fetchPlotsData(profileDataResult);
                if (mutationTypeResult !== "") {
                    translateMutationType(mutationTypeResult);
                    prioritizeMutatedCases();
                }
                analyseData();
            },
            getDotsGroup: function() { return dotsGroup; },
            getDataStatus: function() { return status; },
            getDataAttr: function() { return attr; }
        };

    }());
