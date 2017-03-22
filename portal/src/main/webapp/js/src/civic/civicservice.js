
function CivicService() {
    
    // All results are stored here
    var civicGenes = {};
    
    function getCivicGenesBatch(ids) {
        return $.ajax({
            type: 'GET',
            url: civicUrl + 'genes/' + ids,
            dataType: 'json',
            data: {
                identifier_type: 'entrez_symbol'
            }
        }).then(function(result) {
            if (_.isString(result)) {
                result = $.parseJSON(result);
            }

            // Wrap the result in an Array if it is not already one
            if (!(result instanceof Array)) {
                result = [result];
            }
            if (result) {
                result.forEach(function(record) {
                    var civicGene = {
                        id: record.id,
                        name: record.name,
                        description: record.description,
                        url: 'https://civic.genome.wustl.edu/#/events/genes/'
                        + record.id + '/summary',
                        variants: {}
                    };
                    civicGenes[record.name] = civicGene;
                    if (record.variants && record.variants.length > 0) {
                        var variants = civicGene.variants;
                        record.variants.forEach(function(variant) {
                            variants[variant.name] = {
                                id: variant.id,
                                name: variant.name,
                                geneId: civicGene.id
                            };
                        });
                    }
                });
            }
            return civicGenes;
        });
    }
    
    
    var service = {
        
        getCivicGenes: function(geneSymbols) {

            // Assemble a list of promises, each of which will retrieve a batch of genes
            var promises = [];
            var ids = '';
            geneSymbols.forEach(function(geneSymbol) {
                // Check if we already have it in the cache
                if (civicGenes.hasOwnProperty(geneSymbol)) {
                    return;
                }

                // Add the symbol to the list
                if (ids.length > 0) {
                    ids += ',';
                }
                ids += geneSymbol;

                // To prevent the request from growing too large, we send it off
                // when it reaches this limit and start a new one
                if (ids.length >= 1900) {
                    promises.push(getCivicGenesBatch(ids));
                    ids = '';
                }
            });
            if (ids.length > 0) {
                promises.push(getCivicGenesBatch(ids));
            }

            // We're explicitly waiting for all promises to finish (done or fail).
            // We are wrapping them in another promise separately, to make sure we also 
            // wait in case one of the promises fails and the other is still busy.
            var mainPromise = $.when.apply($, $.map(promises, function(promise) {
                var wrappingDeferred = $.Deferred();
                promise.always(function (result) {
                    wrappingDeferred.resolve();
                });
                return wrappingDeferred.promise();
            }));

            return mainPromise.then(function() {
                return civicGenes;
            });
        },

        getMatchingCivicVariants: function(civicVariants, proteinChange) {
            var matchingCivicVariants = [];
            
            // If present, add the exact match first
            var civicVariant = civicVariants[proteinChange];
            if (typeof civicVariant !== 'undefined') {
                matchingCivicVariants.push(civicVariant);
            }

            // Match any other variants after splitting the name on + or /
            $.each(civicVariants, function(name, civicVariant) {
                var split = name.split(/[+\/]/);
                if (split.length > 1 && split.indexOf(proteinChange) >= 0) {
                    matchingCivicVariants.push(civicVariant);
                }
            });
            
            return matchingCivicVariants;
        },
        
        getCivicVariant: function(civicVariant) {
            var deferred = $.Deferred();

            if (civicVariant.hasOwnProperty('description')) {
                // Variant info has already been loaded
                deferred.resolve();
            }
            else {
                $.ajax({
                        type: 'GET',
                        url: civicUrl + 'variants/' + civicVariant.id,
                        dataType: 'json'
                    })
                    .done(function (result) {
                        if (_.isString(result)) {
                            result = $.parseJSON(result);
                        }
                        civicVariant.description = result.description;

                        civicVariant.url = 'https://civic.genome.wustl.edu/#/events/genes/' + civicVariant.geneId +
                            '/summary/variants/' + civicVariant.id + '/summary#variant';

                        // Aggregate evidence items per type
                        civicVariant.evidence = {};
                        var evidence = civicVariant.evidence;
                        result.evidence_items.forEach(function (evidenceItem) {
                            var evidenceType = evidenceItem.evidence_type;
                            if (evidence.hasOwnProperty(evidenceType)) {
                                evidence[evidenceType] += 1;
                            }
                            else {
                                evidence[evidenceType] = 1;
                            }
                        });
                        deferred.resolve();
                    })
                    .fail(function () {
                        deferred.reject();
                    });
            }

            return deferred.promise();
        }
    };

    return service;
}
