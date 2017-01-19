/*
 * Copyright (c) 2017 Memorial Sloan-Kettering Cancer Center.
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
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @author Selcuk Onur Sumer
 */
function Hotspots3dDataProxy(options)
{
	var self = this;

	// map of <gene, data[]> pairs
	var _hotspotDataCache = {};

	// default options
	var _defaultOpts = {
		servletName: "http://3dhotspots.org/3d/api",
        subService : {
		    hotspotsByGene: "hotspots/3d",
            variantsByGene: "variants"
        }
	};

	// merge options with default options to use defaults for missing values
	var _options = jQuery.extend(true, {}, _defaultOpts, options);

	// call super constructor to init options and other params
	AbstractDataProxy.call(this, _options);
	_options = self._options;

	/**
	 * Initializes with full annotation data. Once initialized with full data,
	 * this proxy class assumes that there will be no additional data.
	 *
	 * @param options   data proxy options
	 */
	function fullInit(options)
	{
        _hotspotDataCache = options.data;
	}

	/**
	 * Returns the mutation data for the given gene(s).
	 *
	 * @param geneList  list of genes as an array or a comma separated string
	 * @param callback  callback function to be invoked after retrieval
	 */
	function getHotspotsByGene(geneList, callback)
	{
		var genes = geneList;
		
		if (_.isString(geneList)) {
            genes = geneList.trim().split(",");
        }
        
		var genesToQuery = [];

		// get previously grabbed data (if any)
		var hotspotData = [];

		// process each gene in the given list
		_.each(genes, function(gene, idx) {
			gene = gene.toUpperCase();

			var data = _hotspotDataCache[gene];

			if (data == undefined || _.isEmpty(data))
			{
				// hotspot data does not exist for this gene, add it to the list
				genesToQuery.push(gene);
			}
			else
			{
				// data is already cached for this gene, update the data array
				hotspotData = hotspotData.concat(data);
			}
		});

		// all data is already retrieved (full init)
		if (self.isFullInit())
		{
			// just forward the call the callback function
			callback(hotspotData);
		}
		// we need to retrieve missing data (lazy init)
		else
		{
			var process = function(data) {
				// cache data (assuming data is an array)
				_.each(data, function(hotspot, idx) {
					if (hotspot.hugoSymbol)
					{
						var key = hotspot.hugoSymbol.toUpperCase();
					    
					    if (!_hotspotDataCache[key]) {
                            _hotspotDataCache[key] = [];
                        }

                        _hotspotDataCache[key].push(hotspot);
					}
				});

				// concat new data with already cached data,
				// and forward it to the callback function
				hotspotData = hotspotData.concat(data);
				callback(hotspotData);
			};

			// some (or all) data is missing,
			// send ajax request for missing genes
			if (genesToQuery.length > 0)
			{
				var url = _options.servletName + "/" + _options.subService.hotspotsByGene;
			    var geneList = genesToQuery.join(",");
				
			    // retrieve data from the server
				//$.post(_options.servletName, servletParams, process, "json");
				var ajaxOpts = {
					type: "POST",
					url: url,
					data: {hugoSymbols: geneList},
					success: process,
					error: function() {
						console.log("[Hotspots3dDataProxy.getHotspotsByGene] " +
						            "error retrieving hotspots data for genes: " +
						            geneList);
						process([]);
					},
					dataType: "json"
				};

				self.requestData(ajaxOpts);
			}
			// data for all requested genes already cached
			else
			{
				// just forward the data to the callback function
				callback(hotspotData);
			}
		}
	}

	// override required base functions
	self.fullInit = fullInit;

	// class specific functions
	self.getHotspotsByGene = getHotspotsByGene;
    //self.getVariantsByGene = getVariantsByGene;
}

// Hotspots3dDataProxy extends AbstractDataProxy...
Hotspots3dDataProxy.prototype = new AbstractDataProxy();
Hotspots3dDataProxy.prototype.constructor = Hotspots3dDataProxy;