/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Created by zhangh2 on 12/2/15.
 */


var OncoKB = (function(_, $) {
    var self = {};
    self.url = '';
    self.accessible = false;
    self.instances = [];
    self.customObject = {};
    self.levels = {
        sensitivity: ['4', '3B', '3A', '2B', '2A', '1', '0'],
        resistance: ['R3', 'R2', 'R1'],
        all: ['4', 'R3', '3B', '3A', 'R2', '2B', '2A', 'R1', '1', '0']
    };
    self.levelColors = {
        '1': '#008D14',
        '2A': '#019192',
        '2B': '#2A5E8E',
        '3A': '#794C87',
        '3B': '#9B7EB6',
        '4': '#000000',
        'R1': '#FF0000'
    };
    self.levelsInfo = {
        '1': '<span><b>FDA-recognized</b> biomarker predictive of response to an <b>FDA-approved</b> drug <b>in this indication</b></span>',
        '2A': '<span><b>Standard care</b> biomarker predictive of response to an <b>FDA-approved</b> drug <b>in this indication</b></span>',
        '2B': '<span><b>Standard care</b> biomarker predictive of response to an <b>FDA-approved</b> drug <b>in another indication</b>, but not standard care for this indication</span>',
        '3A': '<span><b>Compelling clinical evidence</b> supports the biomarker as being predictive of response to a drug <b>in this indication</b>, but neither biomarker and drug are standard care</span>',
        '3B': '<span><b>Compelling clinical evidence</b> supports the biomarker as being predictive of response to a drug <b>in another indication</b>, but neither biomarker and drug are standard care</span>',
        '4': '<span><b>Compelling biological evidence</b> supports the biomarker as being predictive of response to a drug, but neither biomarker and drug are standard care</span>',
        'R1': '<span><b>Standard care</b> biomarker predictive of <b>resistance</b> to an <b>FDA-approved</b> drug <b>in this indication</b></span>',
    };
    self.instanceManagers = {};

    self.oncogenic = ['Unknown', 'Inconclusive', 'Likely Neutral', 'Predicted Oncogenic', 'Likely Oncogenic', 'Oncogenic'];

    _.templateSettings = {
        evaluate: /<@([\s\S]+?)@>/g,
        interpolate: /\{\{(.+?)\}\}/g
    };

    function InstanceManager(id) {
        this.id = id || 'OncoKB-InstanceManager-' + new Date().getTime(); //Manager ID, maybe used to distinguish between different managers
        this.instances = {}; //key is the instance id
    }

    InstanceManager.prototype = {
        addInstance: function(instanceId) {
            var instance = new Instance(instanceId);
            this.instances[instance.getId()] = instance;
            return instance;
        },
        removeInstance: function(instanceId) {
            if (this.instances.hasOwnProperty(instanceId)) {
                delete this.instances[instanceId];
                return true;
            } else {
                return false;
            }
        },
        getInstance: function(instanceId) {
            return this.instances[instanceId];
        },
        getId: function() {
            return this.id;
        }
    };

    function VariantPair() {
        this.id = '';
        this.gene = '';
        this.entrezGeneId = '';
        this.alteration = '';
        this.geneStatus = '';
        this.tumorType = '';//tumor type
        this.consequence = '';
        this.proteinStart = '';
        this.proteinEnd = '';
        this.evidenceType = '';
        this.source = 'cbioportal';
        this.evidence = new OncoKB.Evidence();
        this.cosmicCount = '';
        this.isHotspot = false;
        this.isVUS = false;
        this.highestSensitiveLevel = '';
        this.highestResistanceLevel = '';
        this.hasGene = true;
        this.hasVariant = false;
        this.hasAllele = false;
        this.updatedEvidence = false; //Parameter indicates whether this variant has received evidence from OncoKB service.
    }

    VariantPair.prototype.getVariantId = function() {
        return this.gene + this.alteration + this.tumorType
            + this.consequence + this.proteinStart + this.proteinEnd;
    };

    function Evidence() {
        this.id = '';
        this.gene = {};
        this.alteration = [];
        this.prevalence = [];
        this.progImp = [];
        this.treatments = {
            sensitivity: [],
            resistance: []
        }; //separated by level type
        this.trials = [];
        this.oncogenic = '';
        this.oncogenicRefs = [];
        this.mutationEffect = {};
        this.mutationEffectRefs = [];
        this.summary = '';
        this.drugs = {
            sensitivity: {
                current: [],
                inOtherTumor: []
            },
            resistance: []
        }
    }

    function Instance(id) {
        this.initialized = false;
        this.dataReady = false;
        this.tumorType = ''; //Global tumor types
        this.source = 'cbioportal';
        this.geneStatus = 'Complete';
        this.evidenceTypes = 'GENE_SUMMARY,GENE_BACKGROUND,ONCOGENIC,MUTATION_EFFECT,VUS,STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_SENSITIVITY,STANDARD_THERAPEUTIC_IMPLICATIONS_FOR_DRUG_RESISTANCE,INVESTIGATIONAL_THERAPEUTIC_IMPLICATIONS_DRUG_SENSITIVITY';
        this.evidenceLevels = ['LEVEL_1', 'LEVEL_2A', 'LEVEL_2B', 'LEVEL_3A', 'LEVEL_3B', 'LEVEL_4', 'LEVEL_R1'];
        this.variants = {};
        this.evidence = {};
        this.id = id || 'OncoKB-Instance-' + new Date().getTime();
        this.variantUniqueIds = {}; // Unique variant list.
        this.civicService = CivicService();
        this.civicGenes = {};
    }

    function EvidenceRequestItem(variant) {
        this.ids = [];
        this.hugoSymbol = variant.gene || '';
        this.alteration = variant.alteration || '';
        if (variant.tumorType) {
            this.tumorType = variant.tumorType || '';//tumor type
        }
        if (variant.consequence) {
            this.consequence = variant.consequence || '';//tumor type
        }
        if (variant.proteinStart) {
            this.proteinStart = variant.proteinStart || '';//tumor type
        }
        if (variant.proteinEnd) {
            this.proteinEnd = variant.proteinEnd || '';//tumor type
        }
    }

    function addInstanceManager(instanceManagerId) {
        var manager = new InstanceManager(instanceManagerId);
        self.instanceManagers[manager.getId()] = manager;
        return manager;
    }

    self.utils = (function() {
        /**
         * Insert PUBMED and Clinical Trial link into input string
         * @param str
         * @returns {*}
         */
        function findRegex(str, type) {

            if (typeof str === 'string' && str) {
                var regex = [/PMID:\s*([0-9]+,*\s*)+/ig, /NCT[0-9]+/ig],
                    links = ['http://www.ncbi.nlm.nih.gov/pubmed/',
                        'http://clinicaltrials.gov/show/'];
                for (var j = 0, regexL = regex.length; j < regexL; j++) {
                    var result = str.match(regex[j]);

                    if (result) {
                        var uniqueResult = result.filter(function(elem, pos) {
                            return result.indexOf(elem) === pos;
                        });

                        // In order to avoid the shorter match may exist in 
                        // longer match, replace the longer text first.
                        uniqueResult.sort(function(a, b) {
                            return b.length - a.length;
                        });

                        for (var i = 0, resultL = uniqueResult.length; i < resultL; i++) {
                            var _datum = uniqueResult[i];

                            switch (j) {
                                case 0:
                                    var _number = _datum.split(':')[1].trim();
                                    _number = _number.replace(/\s+/g, '');
                                    if (type === 'refs-icon') {
                                        str = str.replace(new RegExp(_datum, 'g'), '<i class="fa fa-book" qtip-content="' + _number + '" style="color:black"></i>');
                                    } else {
                                        str = str.replace(new RegExp(_datum, 'g'), '<a class="withUnderScore" target="_blank" href="' + links[j] + _number + '">' + _datum + '</a>');
                                    }
                                    break;
                                default:
                                    str = str.replace(_datum, '<a class="withUnderScore" target="_blank" href="' + links[j] + _datum + '">' + _datum + '</a>');
                                    break;
                            }

                        }
                    }
                }
            } else {
                str = '';
            }
            return str;
        }

        /**
         * Convert cBioPortal consequence to OncoKB consequence
         *
         * @param consequence cBioPortal consequence
         * @returns
         */
        function consequenceConverter(consequence) {
            var matrix = {
                '3\'Flank': ['any'],
                '5\'Flank ': ['any'],
                'Targeted_Region': ['inframe_deletion', 'inframe_insertion'],
                'COMPLEX_INDEL': ['inframe_deletion', 'inframe_insertion'],
                'ESSENTIAL_SPLICE_SITE': ['feature_truncation'],
                'Exon skipping': ['inframe_deletion'],
                'Frameshift deletion': ['frameshift_variant'],
                'Frameshift insertion': ['frameshift_variant'],
                'FRAMESHIFT_CODING': ['frameshift_variant'],
                'Frame_Shift_Del': ['frameshift_variant'],
                'Frame_Shift_Ins': ['frameshift_variant'],
                'Fusion': ['fusion'],
                'Indel': ['frameshift_variant', 'inframe_deletion', 'inframe_insertion'],
                'In_Frame_Del': ['inframe_deletion'],
                'In_Frame_Ins': ['inframe_insertion'],
                'Missense': ['missense_variant'],
                'Missense_Mutation': ['missense_variant'],
                'Nonsense_Mutation': ['stop_gained'],
                'Nonstop_Mutation': ['stop_lost'],
                'Splice_Site': ['splice_region_variant'],
                'Splice_Site_Del': ['splice_region_variant'],
                'Splice_Site_SNP': ['splice_region_variant'],
                'splicing': ['splice_region_variant'],
                'Translation_Start_Site': ['start_lost'],
                'vIII deletion': ['any']
            };
            if (matrix.hasOwnProperty(consequence)) {
                return matrix[consequence].join(',');
            } else {
                return 'any';
            }
        }

        function getLevel(level) {
            if (level) {
                var _level = level.match(/LEVEL_(R?\d[AB]?)/);
                if (_level instanceof Array && _level.length >= 2) {
                    return _level[1];
                } else {
                    return level;
                }
            } else {
                return "";
            }
        }

        function getNumberLevel(level) {
            if (level) {
                var _level = level.match(/LEVEL_R?(\d)[AB]?/);
                if (_level instanceof Array && _level.length >= 2) {
                    return _level[1];
                } else {
                    return level;
                }
            } else {
                return "";
            }
        }

        //Compare the highest level in x and y
        function compareHighestLevel(x, y, type) {
            var highestLevelIndexX = OncoKB.levels[type].indexOf(getLevel(x));
            var highestLevelIndexY = OncoKB.levels[type].indexOf(getLevel(y));
            return highestLevelIndexX === highestLevelIndexY ? 0 : (highestLevelIndexX < highestLevelIndexY ? 1 : -1);
        }

        function getTumorTypeFromClinicalDataMap(clinicalDataMap) {
            if (typeof clinicalDataMap === 'object') {
                var keys = Object.keys(clinicalDataMap);
                if (keys.length > 0 && clinicalDataMap[keys[0]].hasOwnProperty('CANCER_TYPE') && clinicalDataMap[keys[0]].CANCER_TYPE) {
                    return clinicalDataMap[keys[0]].CANCER_TYPE;
                }
            }
            return '';
        }

        function compareOncogenic(x, y) {
            var indexX = getOncogenicIndex(x);
            var indexY = getOncogenicIndex(y);
            return indexX === indexY ? 0 : (indexX < indexY ? 1 : -1);
        }

        function getOncogenicIndex(oncogenic) {
            if (oncogenic === 'Likely Oncogenic' ||
                oncogenic === 'Predicted Oncogenic') {
                oncogenic = 'Oncogenic'; //Group likely oncogenic with oncogenic
            }
            return OncoKB.oncogenic.indexOf(oncogenic);
        }

        function compareIcons(category, x, y, type, levelType) {
            var xWeight = -1, yWeight = 1;

            if (type === 'desc') {
                xWeight = 1;
                yWeight = -1;
            }

            if (category === 'oncogenic') {
                if (!x.oncokb) {
                    if (!y.oncokb) {
                        return 0;
                    }
                    return yWeight;
                }
                if (!y.oncokb) {
                    return xWeight;
                }

                if (!x.oncokb.hasOwnProperty('evidence') || !x.oncokb.evidence.hasOwnProperty('oncogenic') || OncoKB.utils.getOncogenicIndex(x.oncokb.evidence.oncogenic) === -1) {
                    if (!y.oncokb.hasOwnProperty('evidence') || !y.oncokb.evidence.hasOwnProperty('oncogenic') || OncoKB.utils.getOncogenicIndex(y.oncokb.evidence.oncogenic) === -1) {
                        if (!x.oncokb.isVUS) {
                            if (!y.oncokb.isVUS) {
                                return 0;
                            }
                            return yWeight;
                        }
                        if (!y.oncokb.isVUS) {
                            return xWeight;
                        }
                        return 0;
                    }
                    return yWeight;
                }
                if (!y.oncokb.hasOwnProperty('evidence') || !y.oncokb.evidence.hasOwnProperty('oncogenic') || OncoKB.utils.getOncogenicIndex(y.oncokb.evidence.oncogenic) === -1) {
                    return xWeight;
                }
                return -xWeight * OncoKB.utils.compareOncogenic(x.oncokb.evidence.oncogenic, y.oncokb.evidence.oncogenic);
            }

            if (category === 'oncokb') {
                if (!x.oncokb) {
                    if (!y.oncokb) {
                        return 0;
                    }
                    return yWeight;
                }
                if (!y.oncokb) {
                    return xWeight;
                }
                if (!x.oncokb.hasOwnProperty('evidence') || !x.oncokb.hasOwnProperty(levelType) || !x.oncokb[levelType]) {
                    if (!y.oncokb || !y.oncokb.hasOwnProperty(levelType) || !y.oncokb[levelType]) {
                        return 0;
                    }
                    return yWeight;
                }
                if (!y.oncokb.hasOwnProperty('evidence') || !y.oncokb.hasOwnProperty(levelType) || !y.oncokb[levelType]) {
                    return xWeight;
                }
                if (levelType === 'highestSensitiveLevel') {
                    return -xWeight * OncoKB.utils.compareHighestLevel(x.oncokb[levelType], y.oncokb[levelType], 'sensitivity');
                } else if (levelType === 'highestSensitiveLevel') {
                    return -xWeight * OncoKB.utils.compareHighestLevel(x.oncokb[levelType], y.oncokb[levelType], 'resistance');
                }
            }
            if (category === 'mycancergenome') {
                if (!hasMyCancerGenomeInfo(x.mutation)) {
                    if (!hasMyCancerGenomeInfo(y.mutation)) {
                        return 0;
                    }
                    return yWeight;
                }
                if (!hasMyCancerGenomeInfo(y.mutation)) {
                    return xWeight;
                }
                return 0;
            }
            if (category === 'hotspot') {
                if (!isHotspot(x.mutation)) {
                    if (!isHotspot(y.mutation)) {
                        return 0;
                    }
                    return yWeight;
                }
                if (!isHotspot(y.mutation)) {
                    return xWeight;
                }
                return 0;
            }
        }

        function hasMyCancerGenomeInfo(mutation) {
            try {
                if(mutation.get("myCancerGenome").length > 0) {
                    return true;
                }else {
                    return false;
                }
            }catch (e){
                return false;
            }
        }

        function isHotspot(mutation) {
            try {
                if(mutation.get("isHotspot") || mutation.get("is3dHotspot")) {
                    return true;
                }else {
                    return false;
                }
            }catch (e){
                return false;
            }
        }
        
        function processEvidence(evidences) {
            var result = {}; //id based.
            if (evidences && evidences.length > 0) {
                evidences.forEach(function(record) {
                    var id = record.query.id;
                    var datum = new OncoKB.Evidence();
                    var hasHigherLevelEvidence = false;
                    var sensitivityTreatments = [];
                    var resistanceTreatments = [];

                    record.evidences.forEach(function(evidence) {
                        var description = '';
                        if (evidence.shortDescription) {
                            description = evidence.shortDescription;
                        } else {
                            description = evidence.description;
                        }
                        if (evidence.evidenceType === 'GENE_SUMMARY') {
                            datum.gene.summary = OncoKB.utils.findRegex(description);
                        } else if (evidence.evidenceType === 'GENE_BACKGROUND') {
                            datum.gene.background = OncoKB.utils.findRegex(description);
                        } else if (evidence.evidenceType === 'ONCOGENIC') {
                            if (evidence.articles) {
                                datum.oncogenicRefs = evidence.articles;
                            }
                        } else if (evidence.evidenceType === 'MUTATION_EFFECT') {
                            var _datum = {};
                            if (evidence.knownEffect) {
                                _datum.knownEffect = evidence.knownEffect;
                            }
                            if (evidence.articles) {
                                _datum.refs = evidence.articles;
                            }
                            if (description) {
                                _datum.description = OncoKB.utils.findRegex(description, 'refs-icon');
                            }
                            datum.alteration.push(_datum);
                        } else if (evidence.levelOfEvidence) {
                            //if evidence has level information, that means this is treatment evidence.
                            if (['LEVEL_0'].indexOf(evidence.levelOfEvidence) === -1) {
                                var _treatment = {};
                                _treatment.alterations = evidence.alterations;
                                _treatment.articles = evidence.articles;
                                _treatment.tumorType = OncoKB.utils.getTumorTypeFromEvidence(evidence);
                                _treatment.level = evidence.levelOfEvidence;
                                _treatment.content = evidence.treatments;
                                _treatment.description = OncoKB.utils.findRegex(description, 'refs-icon') || 'No yet curated';

                                if (OncoKB.levels.sensitivity.indexOf(OncoKB.utils.getLevel(evidence.levelOfEvidence)) !== -1) {
                                    sensitivityTreatments.push(_treatment);
                                } else {
                                    resistanceTreatments.push(_treatment);
                                }

                                if (_treatment.level === 'LEVEL_1' || _treatment.level === 'LEVEL_2A') {
                                    hasHigherLevelEvidence = true;
                                }
                            }
                        }
                    });

                    if (datum.alteration.length > 0) {
                        datum.mutationEffect = datum.alteration[0];
                    }

                    if (hasHigherLevelEvidence) {
                        sensitivityTreatments.forEach(function(treatment, index) {
                            if (treatment.level !== 'LEVEL_2B') {
                                datum.treatments.sensitivity.push(treatment);
                            }
                        });
                    } else {
                        datum.treatments.sensitivity = sensitivityTreatments;
                    }
                    datum.treatments.resistance = resistanceTreatments;
                    datum.treatments.sensitivity.forEach(function(treatment, index) {
                        if (treatment.level === 'LEVEL_2B') {
                            datum.drugs.sensitivity.inOtherTumor.push(treatment);
                        } else if (treatment.level === 'LEVEL_2A' || treatment.level === 'LEVEL_1') {
                            datum.drugs.sensitivity.current.push(treatment);
                        }
                    });
                    datum.treatments.resistance.forEach(function(treatment, index) {
                        if (treatment.level === 'LEVEL_R1') {
                            datum.drugs.resistance.push(treatment);
                        }
                    });
                    id.split('*ONCOKB*').forEach(function(_id) {
                        result[_id] = datum;
                    })
                });
            }

            return result;
        }

        function getTumorTypeFromEvidence(evidence) {
            var tumorType = _.isObject(evidence.tumorType) ? evidence.tumorType.name : (evidence.subtype || evidence.cancerType);
            var oncoTreeTumorType = '';

            if(_.isObject(evidence.oncoTreeType)) {
                oncoTreeTumorType = evidence.oncoTreeType.subtype ? evidence.oncoTreeType.subtype : evidence.oncoTreeType.cancerType;
            }
            
            if(oncoTreeTumorType) {
                tumorType = oncoTreeTumorType;
            }
                
            return tumorType;
        }
        return {
            findRegex: findRegex,
            consequenceConverter: consequenceConverter,
            getLevel: getLevel,
            getNumberLevel: getNumberLevel,
            compareHighestLevel: compareHighestLevel,
            compareOncogenic: compareOncogenic,
            compareIcons: compareIcons,
            getOncogenicIndex: getOncogenicIndex,
            getTumorTypeFromClinicalDataMap: getTumorTypeFromClinicalDataMap,
            processEvidence: processEvidence,
            getTumorTypeFromEvidence: getTumorTypeFromEvidence,
            attachLinkInStr: function(str, matches) {
                if (_.isArray(matches)) {
                    _.each(matches, function(match) {
                        if (str.indexOf(match.keyword) !== -1) {
                            str = str.replace(
                                new RegExp(match.keyword, 'g'),
                                '<a href="' + match.link + '" ' +
                                'target="' + (match.target || '_blank') + '">' +
                                match.keyword +
                                '</a>');
                        }
                    })
                }
                return str;
            }
        };
    })();

    /**
     * OncoKB web services. Each service should always return jQuery promise
     * @type {{init, oncokbAccess, getEvidence}}
     */
    self.connector = (function() {

        /**
         * Use to decide whether OncoKB web service is accessible
         * @returns {*|{then, catch, finally}} jQuery promise
         */
        function access() {
            var deferred = $.Deferred();
            if (self.url) {
                $.get('api-legacy/proxy/oncokbAccess', function() {
                    OncoKB.accessible = true;
                    deferred.resolve();
                })
                    .fail(function() {
                        deferred.reject();
                    });
            } else {
                deferred.reject();
            }

            return deferred.promise();
        }


        return {
            access: access
        };
    })();

    self.str = (function() {

        function treatmentsToStr(data) {
            if (_.isArray(data)) {
                var treatments = [];

                data.forEach(function(treatment) {
                    treatments.push(drugToStr((treatment.drugs)));
                });

                return treatments.join(', ');
            }
        }

        function drugToStr(data) {
            var drugs = [];

            data.forEach(function(drug) {
                drugs.push(drug.drugName);
            });

            return drugs.join('+');
        }

        /**
         * Get gene qtip content.
         * @param gene {Object} It should include summary and background info.
         * @returns {string} Gene qtip content
         */
        function getGeneSummaryBackground(gene) {
            var tooltip = '';

            if (_.isObject(gene)) {
                if (gene.summary) {
                    tooltip += '<b>Gene Summary</b><br/>' + gene.summary;
                }
                if (gene.background) {
                    tooltip += '<br/><div><span class="oncokb_gene_moreInfo"><br/><a>More Info</a><i style="float:right">Powered by OncoKB</i></span><br/><span class="oncokb_gene_background" style="display:none"><b>Gene Background</b><br/>' + gene.background + '<br/><i style="float:right">Powered by OncoKB</i></span></div>';
                }
            }

            return tooltip;
        }

        function getNCBIGeneLink(entrezGeneId) {
            if (_.isNumber(entrezGeneId)) {
                return "<a href=\"http://www.ncbi.nlm.nih.gov/gene/"
                    + entrezGeneId + "\">NCBI Gene</a>";
            } else {
                return "";
            }
        }

        return {
            getGeneSummaryBackground: getGeneSummaryBackground,
            getNCBIGeneLink: getNCBIGeneLink,
            treatmentsToStr: treatmentsToStr
        };
    })();

    self.svgs = (function() {

        function createOncogenicImage(target, oncogenic, isVUS, highestSensitiveLevel, highestResistanceLevel) {
            var iconType = ["", "unknown-oncogenic"];

            var sl = OncoKB.utils.getLevel(highestSensitiveLevel);
            var rl = OncoKB.utils.getLevel(highestResistanceLevel);

            if (!rl) {
                if(sl) {
                    iconType[0] = 'level' + sl;
                }
            } else {
                if (!sl) {
                    if(rl) {
                        iconType[0] = 'level' + rl;
                    }
                } else {
                    iconType[0] = 'level' + sl + 'R';
                }
            }

            switch (oncogenic) {
                case 'Likely Neutral':
                    iconType[1] = 'likely-neutral';
                    break;
                case 'Unknown':
                    iconType[1] = 'unknown-oncogenic';
                    break;
                case 'Inconclusive':
                    iconType[1] = 'unknown-oncogenic';
                    break;
                case 'Predicted Oncogenic':
                    iconType[1] = 'oncogenic';
                    break;
                case 'Likely Oncogenic':
                    iconType[1] = 'oncogenic';
                    break;
                case 'Oncogenic':
                    iconType[1] = 'oncogenic';
                    break;
                default:
                    iconType[1] = 'no-info-oncogenic';
                    break;
            }
            
            if(iconType[1] === 'no-info-oncogenic' &&
                _.isBoolean(isVUS) && isVUS) {
                iconType[1] = 'vus';
            }
            
            var icon = $(target).append('<i class="oncogenic-icon-image ' + iconType.join(' ') + '"/>');
        }

        return {
            createOncogenicImage: createOncogenicImage
        };
    })();

    return {
        VariantPair: VariantPair,
        Instance: Instance,
        addInstanceManager: addInstanceManager,
        EvidenceRequestItem: EvidenceRequestItem,
        Evidence: Evidence,
        access: self.connector.access,
        getAccess: function() {
            return self.accessible;
        },
        levels: self.levels,
        levelColors: self.levelColors,
        levelsInfo: self.levelsInfo,
        oncogenic: self.oncogenic,
        utils: self.utils,
        connector: self.connector,
        str: self.str,
        svgs: self.svgs,
        setUrl: function(url) {
            self.url = url;
            if (url) {
                self.accessible = true;
            }
        }
    };
})(window._, (window.$ || window.jQuery));

OncoKB.Instance.prototype = {
    getHash: function(gene, mutation, tt, consequence) {

    },

    addVariant: function(id, entrezGeneId, gene, mutation, tt, consequence, cosmicCount, isHotspot, proteinStart, proteinEnd) {
        var _variant = new OncoKB.VariantPair();

        if (!isNaN(id)) {
            id = id.toString();
        }

        _variant.id = id;
        _variant.entrezGeneId = Number(entrezGeneId);
        _variant.gene = gene || '';
        _variant.alteration = mutation || '';
        _variant.consequence = _.isString(consequence) ? OncoKB.utils.consequenceConverter(consequence) : '';
        _variant.proteinStart = proteinStart || '';
        _variant.proteinEnd = proteinEnd || '';

        if (tt) {
            _variant.tumorType = tt;
        } else if (this.tumorType) {
            _variant.tumorType = this.tumorType;
        }
        if (!isNaN(cosmicCount)) {
            _variant.cosmicCount = cosmicCount;
        }
        if (isHotspot) {
            _variant.isHotspot = true;
        }
        this.variants[_variant.id] = _variant;
        return _variant.id;
    },

    removeVariant: function(id) {
        if (this.variants.hasOwnProperty(id)) {
            delete this.variants[id];
        }
    },

    getVariant: function(id) {
        if (this.variants.hasOwnProperty(id)) {
            return this.variants[id];
        } else {
            return null;
        }
    },

    getVariantStr: function() {
        var gene = [], mutation = [], consequence = [];
        for (var variant in this.variants) {
            gene.push(variant.gene);
            mutation.push(variant.mutation);
            consequence.push(variant.consequence);
        }
        return {
            gene: gene.join(','),
            mutation: mutation.join(','),
            consequence: consequence.join(',')
        };
    },

    /**
     * Get OncoKB basic info including Oncogenic, hasGene, highest resistance
     * level, highest sensitive level, is VUS and hasVariant
     * @param variants
     * @returns {*|{then, catch, finally}} jQuery promise
     */
    getIndicator: function() {
        var oncokbServiceData = {
            'geneStatus': this.geneStatus,
            'source': this.source,
            'evidenceTypes': this.evidenceTypes,
            'queries': [],
            'levels': this.evidenceLevels
        };
        var oncokbEvidenceRequestItems = {};
        var oncokbSummaryData = {};
        var str = this.getVariantStr();
        var self = this;

        self.setVariantUniqueIds();

        var geneSymbols = []
        for (var key in this.variants) {
            var variant = this.variants[key];
            var uniqueStr = variant.gene + variant.alteration + variant.tumorType + variant.consequence;
            if (!oncokbEvidenceRequestItems.hasOwnProperty(uniqueStr)) {
                oncokbEvidenceRequestItems[uniqueStr] = new OncoKB.EvidenceRequestItem(variant);
            }
            oncokbEvidenceRequestItems[uniqueStr].ids.push(variant.id);
            if (geneSymbols.indexOf(variant.gene) == -1) {
                geneSymbols.push(variant.gene);
            }
        }

        oncokbServiceData.queries = $.map(oncokbEvidenceRequestItems, function(value, index) {
            value.id = value.ids.join('*ONCOKB*');
            delete value.ids;
            return value;
        });

        var oncokbPromise = $.ajax({
            type: 'POST',
            url: 'api-legacy/proxy/oncokb',
            dataType: 'json',
            contentType: 'application/json',
            data: JSON.stringify(oncokbServiceData)
        }).done(function(d1) {
            if(_.isString(d1)) {
                d1 = $.parseJSON(d1);
            }
            if (d1 && d1.length > 0) {
                d1.forEach(function(record) {
                    var id = record.query.id;
                    var datum = new OncoKB.Evidence();

                    datum.oncogenic = record.oncogenic;

                    id.split('*ONCOKB*').forEach(function(_id) {
                        if (self.variants.hasOwnProperty(_id)) {
                            self.variants[_id].hasGene = record.geneExist;
                            self.variants[_id].highestResistanceLevel = record.highestResistanceLevel;
                            self.variants[_id].highestSensitiveLevel = record.highestSensitiveLevel;
                            self.variants[_id].isVUS = record.vus || false;
                            self.variants[_id].hasVariant = record.variantExist;
                            self.variants[_id].hasAllele = record.alleleExist;
                            self.variants[_id].evidence = $.extend(self.variants[_id].evidence, datum);
                            self.variants[_id].evidence.geneSummary = record.geneSummary || '';
                            self.variants[_id].evidence.variantSummary = record.variantSummary || '';
                            self.variants[_id].evidence.tumorTypeSummary = record.tumorTypeSummary || '';
                        }
                    })
                });
            }
            self.dataReady = true;
        })
            .fail(function() {
                console.log('POST failed.');
            });

        var promises = [oncokbPromise];
        if (showCivic) {
            var civicPromise = self.civicService.getCivicGenes(geneSymbols)
                .then(function (result) {
                    self.civicGenes = result;
                });
            promises.push(civicPromise);
        }
        
        // We're explicitly waiting for all promises to finish (done or fail).
        // We are wrapping them in another promise separately, to make sure we also 
        // wait in case one of the promises fails and the other is still busy.
        var mainPromise = $.when.apply($, $.map(promises, function(promise) {
            var wrappingDeferred = $.Deferred();
            promise.always(function () {
                wrappingDeferred.resolve();
            });
            return wrappingDeferred.promise();
        }));
        return mainPromise;
    },
    setVariantUniqueIds: function() {
        var uniqueIds = {};
        var self = this;

        for (var key in self.variants) {
            var variant = self.variants[key];
            var variantId = variant.getVariantId();
            if (!uniqueIds.hasOwnProperty(variantId)) {
                uniqueIds[variantId] = [];
            }
            uniqueIds[variantId].push(variant.id);
        }

        _.each(uniqueIds, function(item, key) {
            var str = item.join('*ONCOKB*');
            _.each(item, function(variant, index) {
                self.variantUniqueIds[variant] = str;
            })
        });
    },
    getVariantUniqueIds: function(oncokbId) {
        return this.variantUniqueIds[oncokbId];
    },
    getEvidence: function(oncokbId) {
        var deferred = $.Deferred();
        var self = this;

        var variant = self.variants[oncokbId];
        var query = new OncoKB.EvidenceRequestItem(variant);
        var oncokbServiceData = {
            'geneStatus': self.geneStatus,
            'source': self.source,
            'evidenceTypes': self.evidenceTypes,
            'queries': [],
            'levels': self.evidenceLevels
        };

        query.id = self.getVariantUniqueIds(oncokbId);
        delete query.ids;

        oncokbServiceData.queries.push(query);

        if (variant.updatedEvidence) {
            deferred.resolve();
        } else {
            $.ajax({
                type: 'POST',
                url: 'api-legacy/proxy/oncokbEvidence',
                dataType: 'json',
                contentType: 'application/json',
                data: JSON.stringify(oncokbServiceData)
            })
                .done(function(d1) {
                    if(_.isString(d1)) {
                        d1 = $.parseJSON(d1);
                    }
                    var result = OncoKB.utils.processEvidence(d1);
                    _.each(result, function(item, index) {
                        self.variants[index].evidence.gene = item.gene;
                        self.variants[index].evidence.alteration = item.alteration;
                        self.variants[index].evidence.treatments = item.treatments;
                        self.variants[index].evidence.drugs = item.drugs;
                        self.variants[index].evidence.oncogenicRefs = item.oncogenicRefs;
                        self.variants[index].evidence.mutationEffect = item.mutationEffect;
                        self.variants[index].evidence.mutationEffectRefs = item.mutationEffect.refs;
                    });
                    variant.updatedEvidence = true;
                    deferred.resolve();
                })
                .fail(function() {
                    variant.updatedEvidence = true;
                    deferred.reject();
                });

        }

        return deferred.promise();
    },

    getSummary: function(oncokbId) {
        var deferred = $.Deferred();
        var self = this;
        var variant = self.variants[oncokbId];
        var oncokbSummaryData = {
            'source': 'cbioportal',
            'type': 'variantCustomized',
            'queries': [{
                id: self.getVariantUniqueIds(oncokbId),
                hugoSymbol: variant.gene,
                alteration: variant.alteration,
                tumorType: variant.tumorType,
            }]
        };


        if (variant.consequence) {
            oncokbSummaryData.queries[0].consequence = variant.consequence;
        }
        if (variant.proteinStart) {
            oncokbSummaryData.queries[0].proteinStart = variant.proteinStart;
        }
        if (variant.proteinEnd) {
            oncokbSummaryData.queries[0].proteinEnd = variant.proteinEnd;//tumor type
        }

        if (self.variants[oncokbId].evidence.summary) {
            deferred.resolve();
        } else {
            $.ajax({
                type: 'POST',
                url: 'api-legacy/proxy/oncokbSummary',
                dataType: 'json',
                contentType: 'application/json',
                data: JSON.stringify(oncokbSummaryData)
            })
                .done(function(d) {
                    if(_.isString(d)) {
                        d = $.parseJSON(d);
                    }
                    var data = _.isArray(d) && _.isObject(d[0]) ? d[0] : null;
                    if (data && data.summary) {
                        if (data.id) {
                            data.id.split('*ONCOKB*').forEach(function(_id) {
                                self.variants[_id].evidence.summary = data.summary;
                            })
                        } else {
                            self.variants[oncokbId].evidence.summary = data.summary;
                        }
                    } else {
                        self.variants[oncokbId].evidence.summary = '';
                    }
                    deferred.resolve();
                })
                .fail(function() {
                    deferred.reject();
                });
        }
        return deferred.promise();
    },
    setTumorType: function(tumorType) {
        this.tumorType = tumorType;
    },

    setGeneStatus: function(geneStatus) {
        this.geneStatus = geneStatus;
    },

    setEvidenceType: function(evidenceType) {
        this.evidenceType = evidenceType;
    },

    addEvents: function(target, type) {
        var self = this;
        if (self.dataReady) {
            if (typeof  type === 'undefined' || type === 'gene') {
                $(target).find('.oncokb_gene').each(function() {
                    var oncokbId = $(this).attr('oncokbId');
                    if(_.isObject(self.variants[oncokbId])) {
                    var gene = self.variants[oncokbId].evidence.gene;
                    var hasGene = self.variants[oncokbId].hasGene;
                    var _tip = '';

                    if (_.isObject(gene) && Object.keys(gene).length > 0) {
                        _tip = OncoKB.str.getGeneSummaryBackground(gene);
                    } else if (hasGene) {
                        _tip = '<span class="oncogenic-loading"><img src="images/loader.gif" alt="loading"/></span>'
                    } else {
                        _tip = OncoKB.str.getNCBIGeneLink(self.variants[oncokbId].entrezGeneId);
                    }
                    if (_tip !== '') {
                        $(this).css('display', '');
                        $(this).one('mouseenter', function() {
                            $(this).qtip({
                                content: {text: _tip},
                                show: {ready: true},
                                hide: {fixed: true, delay: 100},
                                style: {
                                    classes: 'qtip-light qtip-rounded qtip-shadow',
                                    tip: true
                                },
                                position: {
                                    my: 'top left',
                                    at: 'bottom right',
                                    viewport: $(window)
                                },
                                events: {
                                    render: function(event, api) {
                                        self.getEvidence(oncokbId)
                                            .done(function() {
                                                var variant = self.variants[oncokbId];
                                                var tooltip = OncoKB.str.getGeneSummaryBackground(variant.evidence.gene);
                                                var ncbiLink = OncoKB.str.getNCBIGeneLink(self.variants[oncokbId].entrezGeneId);
                                                api.set('content.text', tooltip ? tooltip : (ncbiLink ? ncbiLink : '<b>No information.</b>'));
                                                api.elements.content.find(".oncokb_gene_moreInfo").click(function() {
                                                    $(this).css('display', 'none');
                                                    $(this).parent().find('.oncokb_gene_background').css('display', '');
                                                });
                                            })
                                            .fail(function() {
                                                api.set('content.text', 'OncoKB service is not available at this moment.');
                                            });
                                    }
                                }
                            });
                        });
                    }
                    }
                });
                $(target).find('.annotation-item.civic-cna').each(function() {
                    var geneSymbol = $(this).attr('geneSymbol');
                    $(this).empty(); // remove spinner image
                    if (geneSymbol != null) {
                        var civicGene = self.civicGenes[geneSymbol];
                        if (civicGene) {

                            // Determine which CNAs are available
                            var cnas = ['AMPLIFICATION', 'DELETION'];
                            var matchingCivicVariants = [];
                            cnas.forEach(function(cna) {
                                if (civicGene.variants.hasOwnProperty(cna)) {
                                    matchingCivicVariants.push(civicGene.variants[cna]);
                                }
                            });

                            // Show an icon if
                            // * there is CNA info or
                            // * the gene has a description (grayed-out icon)
                            if (matchingCivicVariants.length > 0 || civicGene.description) {
                                self.createCivicIcon($(this), civicGene, matchingCivicVariants);
                            }
                        }
                    }
                });
            }

            if (typeof  type === 'undefined' || type === 'alteration') {
                $(target).find('.oncokb_alteration').each(function() {
                    var oncokbId = $(this).attr('oncokbId');

                    $(this).empty();
                    if (self.variants.hasOwnProperty(oncokbId)) {
                        var _tip = '', _oncogenicTip = '<span class="oncogenic-loading"><img src="images/loader.gif" alt="loading" /></span>', _hotspotTip = '';
                        var isVus = self.variants[oncokbId].isVUS;

                        if (self.variants[oncokbId].evidence.hasOwnProperty('oncogenic')) {
                            OncoKB.svgs.createOncogenicImage(
                                this, self.variants[oncokbId].evidence.oncogenic,
                                isVus,
                                self.variants[oncokbId].highestSensitiveLevel,
                                self.variants[oncokbId].highestResistanceLevel
                            );
                        } else {
                            OncoKB.svgs.createOncogenicImage(this, -1, false);
                        }

                        _hotspotTip = cbio.util.getHotSpotDesc(true);


                        if ($(this).hasClass('oncogenic')) {
                            _tip = _oncogenicTip;
                        } else if ($(this).hasClass('hotspot')) {
                            _tip = _hotspotTip;
                        }

                        if (_tip !== '') {
                            $(this).css('display', '');
                            $(this).one('mouseenter', function() {
                                $(this).qtip({
                                    content: {text: _tip},
                                    show: {ready: true},
                                    hide: {fixed: true, delay: 500},
                                    style: {
                                        classes: 'qtip-light qtip-shadow oncokb-card-qtip',
                                        tip: true
                                    },
                                    position: {
                                        my: 'center left',
                                        at: 'center right'
                                    },
                                    events: {
                                        render: function(event, api) {
                                            $.when(self.getEvidence(oncokbId))
                                                .done(function() {
                                                    var tooltip = '';
                                                    var variant = self.variants[oncokbId];
                                                    var treatments = [];
                                                    var meta = {
                                                        title: variant.gene + ' ' + variant.alteration + ' in ' + variant.tumorType,
                                                        gene: variant.hasGene ? variant.gene : '',
                                                        additionalInfo: variant.hasGene ? '' : 'There is currently no information about this gene in OncoKB.',
                                                        oncogenicity: variant.evidence.oncogenic,
                                                        oncogenicityCitations: _.isArray(variant.evidence.oncogenicRefs) ?
                                                            variant.evidence.oncogenicRefs.map(function(article) {
                                                                return Number(article.pmid);
                                                            }).sort().join(', ') : '',
                                                        mutationEffect: variant.evidence.mutationEffect.knownEffect,
                                                        mutationEffectPmids: _.isArray(variant.evidence.mutationEffectRefs) ?
                                                            variant.evidence.mutationEffectRefs.map(function(article) {
                                                                return Number(article.pmid);
                                                            }).sort().join(', ') : '',
                                                        clinicalSummary: '<div>' + variant.evidence.geneSummary +
                                                        '</div><div style="margin-top: 6px">' +
                                                        OncoKB.utils.attachLinkInStr(variant.evidence.variantSummary, [{
                                                            keyword: 'Chang et al. 2016',
                                                            link: 'https://www.ncbi.nlm.nih.gov/pubmed/26619011'
                                                        }]) +
                                                        '</div><div style="margin-top: 6px">' + variant.evidence.tumorTypeSummary +
                                                        '</div>',
                                                        biologicalSummary: variant.evidence.mutationEffect.description,
                                                        treatments: []
                                                    };
                                            
                                                    _.each(variant.evidence.treatments, function(content, type) {
                                                        _.each(content, function(item, index) {
                                                            var _level = OncoKB.utils.getLevel(item.level);
                                                            var _treatment = OncoKB.str.treatmentsToStr(item.content);
                                                            var _tumorType = item.tumorType;
                                                            var _alterations = item.alterations.map(function(alt) {
                                                                return alt.alteration;
                                                            }).join(',');
                                                            if (!treatments.hasOwnProperty(_level)) {
                                                                treatments[_level] = {};
                                                            }
                                                            if (!treatments[_level].hasOwnProperty(_alterations)) {
                                                                treatments[_level][_alterations] = {};
                                                            }
                                            
                                                            if (!treatments[_level][_alterations].hasOwnProperty(_treatment)) {
                                                                treatments[_level][_alterations][_treatment] = {};
                                                            }
                                            
                                                            if (!treatments[_level][_alterations][_treatment].hasOwnProperty(_tumorType)) {
                                                                treatments[_level][_alterations][_treatment][_tumorType] = {
                                                                    articles: [],
                                                                    tumorType: _tumorType,
                                                                    alterations: item.alterations,
                                                                    level: _level,
                                                                    treatment: _treatment
                                                                };
                                                            }
                                                            treatments[_level][_alterations][_treatment][_tumorType].articles = _.union(treatments[_level][_alterations][_treatment][_tumorType].articles, item.articles);
                                                        });
                                                    });
                                                    _.each(_.keys(treatments).sort(function(a, b) {
                                                        return OncoKB.levels.all.indexOf(a) > OncoKB.levels.all.indexOf(b) ? -1 : 1;
                                                    }), function(level) {
                                                        _.each(_.keys(treatments[level]).sort(), function(_alteration) {
                                                            _.each(_.keys(treatments[level][_alteration]).sort(), function(_treatment) {
                                                                _.each(_.keys(treatments[level][_alteration][_treatment]).sort(), function(_tumorType) {
                                                                    var content = treatments[level][_alteration][_treatment][_tumorType];
                                                                    meta.treatments.push({
                                                                        level: content.level,
                                                                        variant: content.alterations.map(function(alteration) {
                                                                            return alteration.alteration;
                                                                        }),
                                                                        treatment: _treatment,
                                                                        pmids: content.articles.filter(function(article) {
                                                                            return !isNaN(article.pmid);
                                                                        }).map(function(article) {
                                                                            return Number(article.pmid);
                                                                        }).sort().join(', '),
                                                                        abstracts: content.articles.filter(function(article) {
                                                                            return _.isString(article.abstract);
                                                                        }).map(function(article) {
                                                                            return {
                                                                                abstract: article.abstract,
                                                                                link: article.link
                                                                            };
                                                                        }),
                                                                        cancerType: content.tumorType
                                                                    });
                                                                });
                                                            });
                                                        });
                                                    });
                                            
                                                    OncoKBCard.init(meta, '#qtip-' + api.get('id') + '-content');
                                                   
                                                    //Remove canvas tip from qtip2
                                                    $('#qtip-' + api.get('id') + ' .qtip-tip').remove();
                                            
                                                    var user = userName === 'anonymousUser' ? '' : userName;
                                                    var dialog = new BootstrapDialog({
                                                        message: function(dialogRef) {
                                                            var div = $('<div></div>');
                                                            var closeIcon = $('<div><span class="bootstrap-dialog-close">x</span></div>');
                                                            var message = $('<div><iframe src="https://docs.google.com/forms/d/1lt6TtecxHrhIE06gAKVF_JW4zKFoowNFzxn6PJv4g7A/viewform?' +
                                                                'entry.1744186665=' + self.variants[oncokbId].gene +
                                                                '&entry.1671960263=' + self.variants[oncokbId].alteration +
                                                                '&entry.118699694&entry.1568641202&entry.1381123986=' + user +
                                                                '&entry.1083850662=' + encodeURIComponent(window.location.href) +
                                                                '&embedded=true" width="550" height="500" frameborder="0" marginheight="0" marginwidth="0">Loading...</iframe>');
                                                            closeIcon
                                                                .find('.bootstrap-dialog-close')
                                                                .on('click', {dialogRef: dialogRef}, function(event) {
                                                                    event.data.dialogRef.close();
                                                                });
                                                            div.append(closeIcon);
                                                            div.append(message);
                                                            return div;
                                                        },
                                                        cssClass: 'oncokb-feedback',
                                                        closable: true
                                                    });
                                            
                                                    api.elements.content.find('.oncokb-card-feedback-btn').click(function() {
                                                        api.hide();
                                                        dialog.realize();
                                                        dialog.getModalHeader().hide();
                                                        dialog.getModalFooter().hide();
                                                        dialog.open();
                                                    });
                                            
                                                    api.reposition(null, false);
                                                })
                                                .fail(function() {
                                                    api.set('content.text', 'OncoKB service is not available at this moment.');
                                                });
                                        },
                                        show:function(event, api) {
                                            $('#qtip-' + api.get('id') + '-content' + ' a.oncogenicity[data-toggle="tab"]').tab('show');
                                        }
                                    }
                                });
                            });
                        }
                    }
                });
                $(target).find('.annotation-item.civic').each(function() {
                    var geneSymbol = $(this).attr('geneSymbol');
                    var proteinChange = $(this).attr('proteinChange');
                    $(this).empty(); // remove spinner image
                    if (geneSymbol != null && proteinChange != null) {

                        var civicGene = self.civicGenes[geneSymbol];
                        if (civicGene) {
                            // Look up matching civic variants
                            var matchingCivicVariants = self.civicService.getMatchingCivicVariants(
                                civicGene.variants, proteinChange);

                            if (matchingCivicVariants.length > 0) {
                                self.createCivicIcon($(this), civicGene, matchingCivicVariants);
                            }
                        }
                    }
                });
            }
        }
    },

    getId: function() {
        return this.id;
    },

    createCivicIcon: function(target, civicGene, matchingCivicVariants) {
        var self = this;

        // Construct element for icon
        var imageClass = 'civic-image';
        if (matchingCivicVariants.length == 0) {
            // Show a grayed-out icon when no CNA info is available
            imageClass += ' civic-image-disabled';
        }
        target.append('<i class="' + imageClass + '"></i>');

        // Create popup on mouse-over
        target.one('mouseenter', function () {
            target.qtip({
                content: {text: '<span><img src="images/loader.gif" alt="loading" /></span>'},
                show: {ready: true},
                hide: {fixed: true, delay: 500},
                style: {
                    classes: 'qtip-light qtip-shadow oncokb-card-qtip',
                    tip: true
                },
                position: {
                    my: 'center left',
                    at: 'center right'
                },
                events: {
                    render: function (event, api) {
                        // Load variant information for all matching civicVariants,
                        // after which we construct the html for the qtip
                        var promises = matchingCivicVariants.map(self.civicService.getCivicVariant, self);
                        // Use apply, because 'when' doesn't support arrays
                        $.when.apply($, promises)
                            .done(function () {
                                var url = matchingCivicVariants.length > 0 ?
                                    matchingCivicVariants[0].url : civicGene.url;

                                // Build html for list of variants
                                var variantsHTML = matchingCivicVariants.length > 0 ?
                                    '' : _.template($('#civic-qtip-variant-item-not-available').html())();
                                matchingCivicVariants.forEach(function(variant) {

                                    // Build entry types text
                                    var entries = [];
                                    $.each(variant.evidence, function (key, value) {
                                        entries.push(key.toLowerCase() + ': ' + value);
                                    });
                                    variant.entryTypes = entries.join(', ') + '.';

                                    // Build the html from the variant and the template
                                    var templateFn = _.template($('#civic-qtip-variant-item').html());
                                    variantsHTML += templateFn(variant);
                                });

                                //Build main html and update the qtip
                                var vars = {
                                    title: "CIViC Variants",
                                    gene: civicGene,
                                    variantsHTML: variantsHTML,
                                    url: url
                                };
                                var templateFn = _.template($('#civic-qtip').html());
                                var civicHTML = templateFn(vars);
                                api.set('content.text', civicHTML);
                            })
                            .fail(function() {
                                api.set('content.text', 'Civic service is not available at this moment.');
                            });
                    }
                }
            });
        });
    }
};

$.fn.dataTableExt.oSort['oncokb-level-asc'] = function(x, y) {
    var xIndex = OncoKB.levels.all.indexOf(x);
    var yIndex = OncoKB.levels.all.indexOf(y);
    if (xIndex < yIndex) {
        return 1;
    } else {
        return -1;
    }
};

$.fn.dataTableExt.oSort['oncokb-level-desc'] = function(x, y) {
    var xIndex = OncoKB.levels.all.indexOf(x);
    var yIndex = OncoKB.levels.all.indexOf(y);
    if (xIndex < yIndex) {
        return -1;
    } else {
        return 1;
    }
};

$.fn.dataTableExt.oSort['sort-icons-asc'] = function(x, y) {
    var categories = ['oncogenic', 'oncokb-sensitivity', 'oncokb-resistance', 'mycancergenome', 'hotspot'];
    var categoryL = categories.length;

    for (var i = 0; i < categoryL; i++) {
        var item = categories[i];
        var result;

        if (item === 'oncokb-sensitivity') {
            result = OncoKB.utils.compareIcons('oncokb', x, y, 'asc', 'highestSensitiveLevel')
        } else if (item === 'oncokb-resistance') {
            result = OncoKB.utils.compareIcons('oncokb', x, y, 'asc', 'highestResistanceLevel')
        } else {
            result = OncoKB.utils.compareIcons(item, x, y, 'asc');
        }

        if (result !== 0) {
            return result;
        }
    }

    return 0;
};

$.fn.dataTableExt.oSort['sort-icons-desc'] = function(x, y) {
    var categories = ['oncogenic', 'oncokb-sensitivity', 'oncokb-resistance', 'mycancergenome', 'hotspot'];
    var categoryL = categories.length;

    for (var i = 0; i < categoryL; i++) {
        var item = categories[i];
        var result;

        if (item === 'oncokb-sensitivity') {
            result = OncoKB.utils.compareIcons('oncokb', x, y, 'desc', 'highestSensitiveLevel')
        } else if (item === 'oncokb-resistance') {
            result = OncoKB.utils.compareIcons('oncokb', x, y, 'desc', 'highestResistanceLevel')
        } else {
            result = OncoKB.utils.compareIcons(item, x, y, 'desc');
        }

        if (result !== 0) {
            return result;
        }
    }

    return 0;
};
