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
window.Oncoprint = (function() {
	var events = oncoprint_events;
	var utils = oncoprint_utils;
	var RuleSet = oncoprint_RuleSet;

	var defaultOncoprintConfig = {
		cell_width: 6,
		cell_padding: 2.5,
		legend: true,
	};

	var hiddenOncoprintConfig = {
		pre_track_padding: 0,
	};

	var defaultTrackConfig = {
		label: 'Gene',
		datum_id_key: 'patient',
		cell_height: 23,
		track_height: 20,
		track_padding: 5,
		sort_cmp: undefined,
		tooltip: function(d) {
			return d['patient'];
		},
		removable: false,
		sort_direction_changable: false
	}; 



	function Oncoprint(config) {
		var self = this;
		var getTrackId = utils.makeIdCounter();

		self.config = config;

		self.id_order = [];
		self.inverted_id_order = {};
		self.visible_id_order = [];
		self.visible_inverted_id_order = {};
		self.hidden_ids = {};
		self.track_groups = [[],[]];
		self.track_group_sort_order = [0,1];
		self.sort_direction = {};
		self.tracks = {};
		self.sort_config = {type: 'track'};

		self.cell_padding_on = true;
		self.true_cell_width = config.cell_width;

		self.zoomed_cell_width = self.true_cell_width;
		self.zoom = 1;

		// Cell Padding
		self.toggleCellPadding = function() {
			self.cell_padding_on = !self.cell_padding_on;
			$(self).trigger(events.SET_CELL_PADDING);
		};
		self.getCellPadding = function() {
			return Math.ceil(self.config.cell_padding*self.getZoom())*(+self.cell_padding_on);
		};

		// Zoom
		self.getZoom = function() {
			return self.zoom;
		};
		self.setZoom = function(z) {
			self.zoom = utils.clamp(z, 0, 1);
			updateZoomedCellWidth();
			updateZoom();
			$(self).trigger(events.SET_ZOOM);
			return self.zoom;
		};
		var updateZoom = function() {
			// maps {1, ... , true_cell_width} to [0,1]
			self.zoom = (self.zoomed_cell_width-1)/(self.true_cell_width - 1);
		};
		var updateZoomedCellWidth = function() {
			// maps [0,1] to {1, ... , true_cell_width}
			self.zoomed_cell_width = Math.round(self.zoom*(self.true_cell_width-1) + 1);
		};
		self.increaseZoom = function() {
			self.zoomed_cell_width = utils.clamp(self.zoomed_cell_width+1, 1, self.true_cell_width);
			updateZoom();
			$(self).trigger(events.SET_ZOOM);
			return self.zoom;
		};
		self.decreaseZoom = function() {
			self.zoomed_cell_width = utils.clamp(self.zoomed_cell_width-1, 1, self.true_cell_width);
			updateZoom();
			$(self).trigger(events.SET_ZOOM);
			return self.zoom;
		};

		// Cell Width
		self.getFullCellWidth = function() {
			return self.true_cell_width;
		};
		self.getZoomedCellWidth = function() {
			return self.zoomed_cell_width;
		};

		// Cell Height
		self.getCellHeight = function(track_id) {
			return self.tracks[track_id].config.cell_height;
		};

		// Track Height
		self.getTrackHeight = function(track_id) {
			return self.tracks[track_id].config.track_height;
		};

		// Track Padding
		self.getTrackPadding = function(track_id) {
			return self.tracks[track_id].config.track_padding;
		};

		// Id Order
		self.getFilteredIdOrder = function(data_filter_fn, track_ids) {
			var tracks = track_ids || self.getTracks();
			return _.filter(self.id_order, function(id) {
				var d = _.map(tracks, function(track_id) {
					return self.getTrackDatum(track_id, id);
				});
				return data_filter_fn(d);
			});
		};
		self.getIdOrder = function() {
			return self.id_order;
		};
		self.getInvertedIdOrder = function() {
			return self.inverted_id_order;
		};
		self.getVisibleIdOrder = function() {
			return self.visible_id_order;
		};
		self.getVisibleInvertedIdOrder = function() {
			return self.visible_inverted_id_order;
		};
		var updateVisibleIdOrder = function() {
			self.visible_id_order = _.filter(self.id_order, function(id) {
				return !self.hidden_ids[id];
			});
			self.visible_inverted_id_order = utils.invert_array(self.visible_id_order);
			$(self).trigger(events.SET_VISIBLE_ID_ORDER);
		};
		self.setIdOrder = function(id_order) {
			self.id_order = id_order.slice();
			self.inverted_id_order = utils.invert_array(self.id_order);
			updateVisibleIdOrder();
			$(self).trigger(events.SET_ID_ORDER);
		};
		// Hide Ids
		self.hideIds = function(ids, clear_existing) {
			if (clear_existing) {
				self.hidden_ids = {};
			}
			_.each(ids, function(id) {
				self.hidden_ids[id] = true;
			});
			updateVisibleIdOrder();
		};
		self.showIds = function(ids) {
			if (!ids) {
				self.hidden_ids = {};
			} else {
				_.each(ids, function(id) {
					delete self.hidden_ids[id];
				});
			}
			updateVisibleIdOrder();
		};

		// Sorting
		self.getTopmostTrack = function() {
			return (self.track_groups[0].length > 0 ? self.track_groups[0][0] : self.track_groups[1][0]);
		};
		self.setTrackSortComparator = function(track_id, cmp) {
			self.tracks[track_id].config.sort_cmp = cmp;
		};
		self.getTrackSortComparator = function(track_id) {
			return self.tracks[track_id].config.sort_cmp;
		};
		self.getTrackSortDirection = function(track_id) {
			return self.sort_direction[track_id];
		};
		self.setTrackSortDirection = function(track_id, dir) {
			self.sort_direction[track_id] = dir;
			self.sort();
		};
		self.setTrackGroupSortOrder = function(order) {
			self.track_group_sort_order = order.slice();
		};
		self.getTrackGroupSortOrder = function() {
			return self.track_group_sort_order.slice();
		};
		self.getTrackSortOrder = function() {
			var ret = [];
			var track_groups = self.getTrackGroups();
			_.each(self.getTrackGroupSortOrder(), function(group_id) {
				ret = ret.concat(track_groups[group_id]);
			});
			return ret;
		};
		self.setSortConfig = function(config) {
			self.sort_config = config;
		};
		var sortById = function(desc) {
			var ret = _.sortBy(self.getIdOrder(), _.identity);
			if (desc) {
				ret.reverse();
			}
			self.setIdOrder(ret);
		};
		var sortByTrack = function() {
			var track_id_list = self.getTrackSortOrder();
			var cmp_list = _.map(track_id_list, function(track_id) { 
				return self.getTrackSortComparator(track_id);
			});
			var data = {};
			var id_order = self.getIdOrder();
			_.each(id_order, function(id) {
				data[id] = {};
				_.each(track_id_list, function(track_id) {
					data[id][track_id] = self.getTrackDatum(track_id, id);
				});
			});
			var lexicographically_ordered_cmp = function(id1,id2) {
				var cmp_result = 0;
				for (var i=0, _len = track_id_list.length; i<_len; i++) {
					var track_id = track_id_list[i];
					var cmp = cmp_list[i];
					var d1 = data[id1][track_id];
					var d2 = data[id2][track_id];
					var d1_undef = (typeof d1 === "undefined");
					var d2_undef = (typeof d2 === "undefined");
					if (!d1_undef && !d2_undef) {
						cmp_result = cmp(d1, d2);
					} else if (d1_undef && d2_undef) {
						cmp_result = 0;
					} else if (d1_undef) {
						cmp_result = 1;
					} else {
						cmp_result = -1;
					}
					if (isFinite(cmp_result)) {
						// reverse direction unless infinite, which is a signal that an NA is involved
						cmp_result *= self.sort_direction[track_id];
					}
					if (cmp_result !== 0) {
						break;
					}
				}
				return cmp_result;
			};
			self.setIdOrder(utils.stableSort(self.getIdOrder(), lexicographically_ordered_cmp));
		};
		self.sort = function() {
			var config = self.sort_config;
			if (config.type === 'track') {
				sortByTrack();
			} else if (config.type === 'id') {
				sortById(config.desc);
			}
		};

		// Track Creation/Destruction
		self.addTrack = function(config, group) {
			group = utils.ifndef(group, 1);
			var track_id = getTrackId();
			self.tracks[track_id] ={id: track_id, 
						data: [], 
						config: $.extend({}, defaultTrackConfig, config),
						id_data_map: {}};
			self.track_groups[group].push(track_id);
			self.sort_direction[track_id] = 1;

			$(self).trigger(events.ADD_TRACK, {track_id: track_id});
			return track_id;
		};
		self.removeTrack = function(track_id) {
			var track = self.tracks[track_id];
			delete self.tracks[track_id];
			delete self.sort_direction[track_id];

			var track_group = self.getContainingTrackGroup(track_id, true);
			if (!track_group) {
				return false;
			} else {
				var old_position = track_group.indexOf(track_id);
				track_group.splice(old_position, 1);

				$(self).trigger(events.REMOVE_TRACK, {track: track, track_id: track_id});
				return true;	
			}
		};

		// Track Ordering
		self.getTrackGroups = function(reference) {
			return (reference === true ? self.track_groups : $.extend(true, [], self.track_groups));			
		};
		self.getTracks = function() {
			return _.flatten(self.getTrackGroups());
		};
		self.getContainingTrackGroup = function(track_id, reference) {
			var group = false;
			_.find(self.track_groups, function(grp) {
				if (grp.indexOf(track_id) > -1) {
					group = grp;
					return true;
				}
				return false;
			});
			return (reference === true ? group : group.slice());
		};
		self.moveTrack = function(track_id, new_position) {
			var track_group = self.getContainingTrackGroup(track_id, true);
			if (!track_group) {
				return false;
			}
			var old_position = track_group.indexOf(track_id);
			new_position = utils.clamp(new_position, 0, track_group.length-1);
			track_group.splice(old_position, 1);
			track_group.splice(new_position, 0, track_id);
			var moved_tracks = track_group.slice(Math.min(old_position, new_position), Math.max(old_position, new_position) + 1);
			$(self).trigger(events.MOVE_TRACK, {moved_tracks: moved_tracks});
		};


		// Track Label
		self.getTrackLabel = function(track_id) {
			return self.tracks[track_id].config.label;
		};

		// Track Tooltip
		self.getTrackTooltip = function(track_id) {
			return self.tracks[track_id].config.tooltip;
		};
		self.setTrackTooltip = function(track_id, tooltip) {
			self.tracks[track_id].config.tooltip = tooltip;
		};

		// Track Data
		self.getTrackData = function(track_id) {
			return self.tracks[track_id].data;
		};
		self.setTrackData = function(track_id, data) {
			var id_accessor = self.getTrackDatumIdAccessor(track_id);

			self.tracks[track_id].data = data;

			var current_id_order = self.getIdOrder();
			var current_inverted_id_order = self.getInvertedIdOrder();
			_.each(_.map(data, id_accessor), function(id) {
				if (!(id in current_inverted_id_order)) {
					current_id_order.push(id);
				}
			});
			self.setIdOrder(current_id_order);
			
			self.tracks[track_id].id_data_map = {};
			var id_data_map = self.tracks[track_id].id_data_map;
			_.each(self.tracks[track_id].data, function(datum) {
				id_data_map[id_accessor(datum)] = datum;
			});
			$(self).trigger(events.SET_TRACK_DATA, {track_id: track_id});
		};
		self.getTrackDatum = function(track_id, datum_id) {
			return self.tracks[track_id].id_data_map[datum_id];
		};
		self.getTrackDatumDataKey = function(track_id) {
			return self.tracks[track_id].config.datum_data_key;
		};

		// Track Datum Id
		self.getTrackDatumIdAccessor = function(track_id) {
			var key = self.getTrackDatumIdKey(track_id);
			return function(d) {
				return d[key];
			};
		};
		self.getTrackDatumIdKey = function(track_id) {
			return self.tracks[track_id].config.datum_id_key;
		};
		self.setTrackDatumIdKey = function(track_id, key) {
			self.tracks[track_id].config.datum_id_key = key;
		};

		// Track info
		self.isTrackRemovable = function(track_id) {
			return self.tracks[track_id].config.removable;
		};
		self.isTrackSortDirectionChangable = function(track_id) {
			return self.tracks[track_id].config.sort_direction_changable;
		};

		// Clearing
		self.clearData = function() {
			_.each(self.getTracks(), function(track_id) {
				self.setTrackData(track_id, []);
			});
			self.setIdOrder([]);
		}
	}

	return { 
		CATEGORICAL_COLOR: RuleSet.CATEGORICAL_COLOR,
		GRADIENT_COLOR: RuleSet.GRADIENT_COLOR,
		GENETIC_ALTERATION: RuleSet.GENETIC_ALTERATION,
		BAR_CHART: RuleSet.BAR_CHART,
		create: function CreateOncoprint(container_selector_string, config) {
			config = $.extend({}, defaultOncoprintConfig, config || {});
			config = $.extend(config, hiddenOncoprintConfig);
			var oncoprint = new Oncoprint(config);
			var renderer = new OncoprintSVGRenderer(container_selector_string, oncoprint, {label_font: 'Arial', legend:config.legend});
			var ret = {
				onc_dev: oncoprint,
				ren_dev: renderer,
				addTrack: function(config, group) {
					var track_id = oncoprint.addTrack(config, group);
					return track_id;
				},
				removeTrack: function(track_id) {
					oncoprint.removeTrack(track_id);
				},
				moveTrack: function(track_id, position) {
					oncoprint.moveTrack(track_id, position);
				},
				setTrackDatumIdKey: function(track_id, key) {
					oncoprint.setTrackDatumIdKey(track_id, key);
				},
				setTrackData: function(track_id, data) {
					oncoprint.setTrackData(track_id, data);
				},
				setRuleSet: function(track_id, type, params) {
					renderer.setRuleSet(track_id, type, params);
				},
				useSameRuleSet: function(target_track_id, source_track_id) {
					renderer.useSameRuleSet(target_track_id, source_track_id);
				},
				toggleCellPadding: function() {
					oncoprint.toggleCellPadding();
				},
				toSVG: function() {
					return renderer.toSVG();
				},
				setTrackGroupSortOrder: function(order) {
					oncoprint.setTrackGroupSortOrder(order);
				},
				sort: function() {
					oncoprint.sort();
				},
				setSortConfig: function(config) {
					oncoprint.setSortConfig(config);
				},
				setIdOrder: function(id_order) {
					oncoprint.setIdOrder(id_order);
				},
				getTrackSortDirection: function(track_id) {
					return oncoprint.getTrackSortDirection(track_id);
				},
				setTrackSortDirection: function(track_id, dir) {
					oncoprint.setTrackSortDirection(track_id, dir);
				},
				setZoom: function(z) {
					return oncoprint.setZoom(z);
				},
				increaseZoom: function() {
					return oncoprint.increaseZoom();
				},
				decreaseZoom: function() {
					return oncoprint.decreaseZoom();
				},
				suppressRendering: function() {
					renderer.suppressRendering();
				},
				releaseRendering: function() {
					renderer.releaseRendering();
				},
				setLegendVisible: function(track_ids, visible) {
					renderer.setLegendVisible(track_ids, visible);
				},
				getFilteredIdOrder: function(data_filter_fn, track_ids) {
					return oncoprint.getFilteredIdOrder(data_filter_fn, track_ids);
				},
				getVisibleIdOrder: function() {
					return oncoprint.getVisibleIdOrder();
				},
				hideIds: function(ids) {
					oncoprint.hideIds(ids);
				},
				showIds: function(ids) {
					oncoprint.showIds(ids);
				},
				clearData: function() {
					oncoprint.clearData();
				},
				setTrackTooltip: function(track_id, tooltip) {
					oncoprint.setTrackTooltip(track_id, tooltip);
				}
			};
			$(oncoprint).on(events.MOVE_TRACK, function() {
				$(ret).trigger(events.MOVE_TRACK);
			});
			$(renderer).on(events.FINISHED_RENDERING, function() {
				$(ret).trigger(events.FINISHED_RENDERING);
			});
			$(oncoprint).on(events.REMOVE_TRACK, function(evt, data) {
				$(ret).trigger(events.REMOVE_TRACK, {track_id: data.track_id});
			});
			$(renderer).on(events.CONTENT_AREA_MOUSEENTER, function(evt, data) {
				$(ret).trigger(events.CONTENT_AREA_MOUSEENTER);
			});
			$(renderer).on(events.CONTENT_AREA_MOUSELEAVE, function(evt, data) {
				$(ret).trigger(events.CONTENT_AREA_MOUSELEAVE);
			});
			return ret;
		}
	};
})();
