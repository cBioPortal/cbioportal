var OncoKBCard = (function(_, $) {
    var templateCache = {};
    var levels = ['1', '2A', '2B', '3A', '3B', '4', 'R1'];
    var levelDes = {
        '1': '<b>FDA-recognized</b> biomarker predictive of response to an <b>FDA-approved</b> drug <b>in this indication</b>',
        '2A': '<b>Standard care</b> biomarker predictive of response to an <b>FDA-approved</b> drug <b>in this indication</b>',
        '2B': '<b>Standard care</b> biomarker predictive of response to an <b>FDA-approved</b> drug <b>in another indication</b>, but not standard care for this indication',
        '3A': '<b>Compelling clinical evidence</b> supports the biomarker as being predictive of response to a drug <b>in this indication</b>, but neither biomarker and drug are standard care',
        '3B': '<b>Compelling clinical evidence</b> supports the biomarker as being predictive of response to a drug <b>in another indication</b>, but neither biomarker and drug are standard care',
        '4': '<b>Compelling biological evidence</b> supports the biomarker as being predictive of response to a drug, but neither biomarker and drug are standard care',
        'R1': '<b>Standard care</b> biomarker predictive of <b>resistance</b> to an <b>FDA-approved</b> drug <b>in this indication</b>'
    };
    var status = {
        mutationRefInitialized: false,
        oncogenicityInitialized: false
    };

    /**
     * Compiles the template for the given template id
     * by using underscore template function.
     *
     * @param {string} templateId - html id of the template content
     * @return {function} - compiled template function
     */
    function compileTemplate(templateId) {
        return _.template($('#' + templateId).html());
    }

    /**
     * Gets the template function corresponding to the given template id.
     *
     * @param {string} templateId - html id of the template content
     * @return {function} - template function
     */
    function getTemplateFn(templateId) {
        // try to use the cached value first
        var templateFn = templateCache[templateId];

        // compile if not compiled yet
        if (!templateFn) {
            templateFn = compileTemplate(templateId);
            templateCache[templateId] = templateFn;
        }

        return templateFn;
    }

    /**
     * Return combined alterations name, separated by comma.
     * Same location variant will be truncated into AALocationAllele e.g. V600E/K
     *
     * @param {Array} alterations - List of alterations
     * @return {string} - Truncated alteration name
     */
    function concatAlterations(alterations) {
        var positions = {};
        var regular = [];
        var regExp = new RegExp('^([A-Z])([0-9]+)([A-Z]$)');

        _.each(alterations, function(alteration) {
            var result = regExp.exec(alteration);
            if (_.isArray(result) && result.length === 4) {
                if (!positions.hasOwnProperty(result[2])) {
                    positions[result[2]] = {};
                }
                if (!positions[result[2]].hasOwnProperty(result[1])) {
                    // Avoid duplication, use object instead of array
                    positions[result[2]][result[1]] = {};
                }
                positions[result[2]][result[1]][result[3]] = 1;
            } else {
                regular.push(alteration);
            }
        });

        _.each(_.keys(positions).map(function(e) {
            return Number(e);
        }).sort(), function(position) {
            _.each(_.keys(positions[position]).sort(), function(aa) {
                regular.push(aa + position + _.keys(positions[position][aa]).sort().join('/'));
            });
        });
        return regular.join(', ');
    }

    /**
     * Initialization function
     * @param {object} data - initial data, please see the sample data in index.html
     * @param {string} target - HTML class name, id, tag or everything acceptable by jQuery
     */
    function init(data, target) {
        var treatmentTemplates = [];
        var levelTemplates = [];

        _.each(data.treatments, function(treatment, index) {
            var treatmentFn = getTemplateFn('oncokb-card-treatment-row');

            if (treatment.level) {
                treatment.levelDes = levelDes[treatment.level];
            }
            if (_.isArray(treatment.variant)) {
                treatment.variant = concatAlterations(treatment.variant);
            }
            treatment.treatmentIndex = index;
            treatmentTemplates.push(treatmentFn(treatment));
        });

        _.each(levels, function(level) {
            var levelFn = getTemplateFn('oncokb-card-level-list-item');
            levelTemplates.push(levelFn({
                level: level,
                levelDes: levelDes[level]
            }));
        });

        var cardMainTemplateFn = getTemplateFn('oncokb-card');
        var cardMainTemplateMeta = {
            title: data.title,
            gene: data.gene,
            additionalInfo: data.additionalInfo || '',
            oncogenicity: data.oncogenicity || 'Unknown',
            oncogenicityPmids: data.oncogenicityPmids,
            mutationEffect: data.mutationEffect || '',
            mutationEffectPmids: data.mutationEffectPmids,
            clinicalSummary: data.clinicalSummary,
            biologicalSummary: data.biologicalSummary,
            treatmentRows: treatmentTemplates.join(''),
            levelRows: levelTemplates.join('')
        };

        if (!cardMainTemplateMeta.mutationEffect) {
            if (cardMainTemplateMeta.oncogenicity.toLowerCase().indexOf('oncogenic') !== -1) {
                cardMainTemplateMeta.mutationEffect = 'Pending curation';
            } else {
                cardMainTemplateMeta.mutationEffect = 'Unknown';
            }
        }

        var cardMainTemplate = cardMainTemplateFn(cardMainTemplateMeta);

        // Have to cache template in here. After Ajax call, we lost the template
        getTemplateFn('oncokb-card-pmid-item');
        getTemplateFn('oncokb-card-abstract-item');

        $(target).html(cardMainTemplate);

        // Remove tabs, level, disclaimer if gene is not available.
        if (!_.isString(data.gene) || !data.gene) {
            $(target + ' .tabs-wrapper').remove();
            $(target + ' .levels-wrapper').remove();
            $(target + ' .disclaimer').remove();
        }

        // Remove table element if there is no treatment available
        if (!_.isArray(data.treatments) || data.treatments.length === 0) {
            $(target + ' .oncogenicity table').remove();
        }

        // If no oncogenicity available, grey out the oncogenicity section
        if (!data.oncogenicity) {
            $(target + ' a.oncogenicity').addClass('grey-out');
            $(target + ' a.oncogenicity').addClass('tab-disabled');
        }

        // If no mutation effect available, grey out the mutation effect section
        if (!data.mutationEffect) {
            $(target + ' a.mutation-effect').addClass('grey-out');
        }

        if (!(data.biologicalSummary || data.mutationEffectPmids)) {
            $(target + ' .tab-pane.mutation-effect').remove();
            $(target + ' a.mutation-effect').removeAttr('href');
            $(target + ' a.oncogenicity').removeAttr('href');
            $(target + ' a.mutation-effect').addClass('tab-disabled');
            $(target + ' .enable-hover').each(function() {
                $(this).removeClass('enable-hover');
            });
        } else if (data.biologicalSummary) {
            $(target + ' .tab-pane.mutation-effect .refs').remove();
        }

        $(target + ' .oncokb-card .collapsible').on('click.collapse', '> li > .collapsible-header', function() {
            $(this).find('i.glyphicon-chevron-down').toggle();
            $(this).find('i.glyphicon-chevron-up').toggle();
        });

        // Remove additional info section if no data presents
        if (!data.additionalInfo) {
            $(target + ' .additional-info').remove();
        }

        // Initialize alll qtips on element which has non-empty qtip-content attribute
        $(target + ' .oncokb-card [qtip-content]').each(function() {
            var element = $(this);
            var content = element.attr('qtip-content');
            var classes = 'qtip-light qtip-shadow';

            if (content) {
                if (element.hasClass('fa-book')) {
                    content = '<img src="images/loader.gif" />';
                    classes += ' qtip-oncokb-card-refs';
                }
                if (element.hasClass('level-icon')) {
                    classes += ' qtip-oncokb-card-levels';
                }
                element.qtip({
                    content: content,
                    hide: {
                        fixed: true,
                        delay: 400,
                        event: 'mouseleave'
                    },
                    style: {
                        classes: classes,
                        tip: true
                    },
                    show: {
                        event: 'mouseover',
                        delay: 0,
                        ready: false
                    },
                    position: {
                        my: element.attr('position-my') || 'center left',
                        at: element.attr('position-at') || 'center right',
                        viewport: $(window)
                    },
                    events: {
                        render: function(event, api) {
                            if (element.hasClass('fa-book')) {
                                var _pmids = '';
                                var _abstracts = [];
                                if (element.attr('qtip-treatment-index')) {
                                    var _treatment = data.treatments[Number(element.attr('qtip-treatment-index'))];
                                    _pmids = _treatment.pmids;
                                    _abstracts = _treatment.abstracts;
                                } else {
                                    _pmids = element.attr('qtip-content');
                                }
                                $.when(getReferenceRows(_pmids, _abstracts))
                                    .then(function(result) {
                                        api.set({
                                            'content.text': result
                                        });
                                        api.reposition(null, false);
                                    }, function() {
                                        api.set({
                                            'content.text': ''
                                        });
                                    });
                            }
                        }
                    }
                });
            } else {
                $(this).remove();
            }
        });

        // Attach event when mutation effect tab is clicked
        $(target + ' a.mutation-effect[data-toggle="tab"]').on('shown.bs.tab', function() {
            var classname = 'mutation-effect';
            var initialKey = 'mutationRefInitialized';
            var citationKey = 'mutationEffectPmids';

            if (data.mutationEffectPmids && !data.biologicalSummary && !status[initialKey]) {
                if (data[citationKey]) {
                    $.when(getReferenceRows(data[citationKey]))
                        .then(function(data) {
                            if (data) {
                                $(target + ' .tab-pane.' + classname + ' .refs').html(data);
                            } else {
                                $(target + ' .tab-pane.' + classname + ' .refs').remove();
                            }
                        }, function(error) {
                            $(target + ' .tab-pane.' + classname + ' .refs').remove();
                        }, function() {
                            status[initialKey] = true;
                        });
                } else {
                    $(target + ' .tab-pane.' + classname + ' .refs').remove();
                }
            }
        });

        $(target + ' a.oncogenicity[data-toggle="tab"]').tab('show');
    }

    /**
     * Construct references HTML based on reference list.
     *
     * @param {Array} refs - Reference list
     * @return {promise} - jQuery promise
     */
    function getReferenceRows(pmids, abstracts) {
        var dfd = $.Deferred();
        var refsTemplates = [];
        refsTemplates = ['<ul class="list-group" style="margin-bottom: 0">'];
        if (pmids) {
            $.when(getReferenceInfoCall(pmids))
                .then(function(data) {
                    var articlesData = data.result;

                    if (articlesData !== undefined && _.isArray(articlesData.uids) && articlesData.uids.length > 0) {
                        _.each(articlesData.uids, function(uid) {
                            var refsFn = getTemplateFn('oncokb-card-pmid-item');
                            var articleContent = articlesData[uid];
                            refsTemplates.push(refsFn({
                                pmid: articleContent.uid,
                                title: articleContent.title,
                                author: (_.isArray(articleContent.authors) && articleContent.authors.length > 0) ? (articleContent.authors[0].name + ' et al.') : 'Unknown',
                                source: articleContent.source,
                                date: (new Date(articleContent.pubdate)).getFullYear()
                            }));
                        });
                    }

                    refsTemplates = _.extend(refsTemplates, getAbstractHtml(abstracts));
                    refsTemplates.push('</ul>');

                    dfd.resolve(refsTemplates.join(''));
                }, function(error) {
                    dfd.reject(error);
                }, function(status) {

                });
        } else if (_.isArray(abstracts) && abstracts.length > 0) {
            refsTemplates = _.extend(refsTemplates, getAbstractHtml(abstracts));
            refsTemplates.push('</ul>');
            dfd.resolve(refsTemplates.join(''));
        } else {
            dfd.resolve('');
        }

        return dfd.promise();
    }

    function getAbstractHtml(abstracts) {
        var refsTemplates = [];
        if (_.isArray(abstracts)) {
            _.each(abstracts, function(abstract) {
                var refsFn = getTemplateFn('oncokb-card-abstract-item');
                refsTemplates.push(refsFn(abstract));
            });
        }
        return refsTemplates;
    }

    /**
     * Get reference information from NCBI webservice.
     * @param {Array} refs - Reference list
     * @return {promise} - jQuery promise
     */
    function getReferenceInfoCall(refs) {
        var dfd = $.Deferred();

        $.get('https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pubmed&retmode=json&id=' + refs).then(
            function(articles) {
                dfd.resolve(articles);
            },
            function(error) {
                dfd.reject(error);
            },
            function(status) {
            });
        return dfd.promise();
    }

    return {
        getTemplateFn: getTemplateFn,
        init: init
    };
})(window._, window.$);
