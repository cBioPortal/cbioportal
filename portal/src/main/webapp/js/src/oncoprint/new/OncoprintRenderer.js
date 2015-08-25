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
window.OncoprintRenderer = (function() {
	var events = oncoprint_events;
	var utils = oncoprint_utils;
	var RuleSet = oncoprint_RuleSet;

	function OncoprintRenderer(oncoprint, config) {
		this.rule_sets = {};
		this.clipping = true;
		this.oncoprint = oncoprint;
		this.config = config;
		this.upper_padding = utils.ifndef(config.upper_padding, 0);
		this.max_label_length = utils.ifndef(config.max_label_length, 20);
		this.track_group_separation = 12;

		(function computeLabelAreaWidth(self) {
			var label_font = self.getLabelFont();
			var max_label_width = utils.textWidth((Math.pow(10,self.max_label_length)-1).toString(), label_font);
			var max_percent_altered_width = utils.textWidth('100%', label_font);
			var buffer_width = 20;
			self.label_area_width = max_label_width + buffer_width + max_percent_altered_width;
		})(this);
	};
	OncoprintRenderer.prototype.getCellCSSClass = function() {
		return 'oncoprint-cell';	
	};
	OncoprintRenderer.prototype.getTrackCellCSSClass = function(track_id) {
		return this.getCellCSSClass()+track_id;
	};
	OncoprintRenderer.prototype.getTrackLabelCSSClass = function(track_id) {
		return 'oncoprint-track-label oncoprint-track-label'+track_id;
	};
	OncoprintRenderer.prototype.getTrackLabelCSSSelector = function(track_id) {
		// TODO: replace with utils.cssClassToSelector
		return "."+this.getTrackLabelCSSClass(track_id).split(" ").join(".");
	};
	OncoprintRenderer.prototype.getTrackCellCtrCSSClass = function(track_id) {
		return 'oncoprint-track-cell-ctr'+track_id;
	};
	OncoprintRenderer.prototype.getLabelFont = function() {
		return this.config.label_font;
	};
	OncoprintRenderer.prototype.setRuleSet = function(track_id, type, params) {
		var new_rule_set = RuleSet.makeRuleSet(type, params);
		this.rule_sets[track_id] = new_rule_set;
		if (new_rule_set.sort_cmp) {
			this.oncoprint.setTrackSortComparator(track_id, new_rule_set.sort_cmp);
		}
	};
	OncoprintRenderer.prototype.useSameRuleSet = function(target_track_id, source_track_id) {
		var rule_set = this.rule_sets[source_track_id];
		this.rule_sets[target_track_id] = rule_set;
		if (rule_set.sort_cmp) {
			this.oncoprint.setTrackSortComparator(target_track_id, rule_set.sort_cmp);
		}
	};
	OncoprintRenderer.prototype.getRuleSet = function(track_id) {
		return this.rule_sets[track_id];
	};
	OncoprintRenderer.prototype.getTrackTops = function() {
		var ret = {};
		var y = this.upper_padding;
		var self = this;
		_.each(this.oncoprint.getTrackGroups(), function(group) {
			if (group.length === 0) {
				return;
			}
			_.each(group, function(id) {
				ret[id] = y;
				y+= self.getRenderedTrackHeight(id);
			});
			y += self.track_group_separation;
		});
		return ret;
	};
	OncoprintRenderer.prototype.getTrackCellTops = function() {
		return this.track_cell_tops || this.computeTrackCellTops();
	};
	OncoprintRenderer.prototype.computeTrackCellTops = function() {
		var tops = this.getTrackTops();
		var self = this;
		_.each(tops, function(top, id) {
			tops[id] = top + self.oncoprint.getTrackPadding(id);
		});
		this.track_cell_tops = tops;
		return tops;
	};
	OncoprintRenderer.prototype.getTrackLabelTops = function() {
		return this.getTrackCellTops();
	};
	OncoprintRenderer.prototype.getRenderedTrackHeight = function(track_id) {
		return this.oncoprint.getTrackHeight(track_id) + 2*this.oncoprint.getTrackPadding(track_id);
	};
	OncoprintRenderer.prototype.getCellX = function(index) {
		return (typeof index === 'number' ? index*(this.oncoprint.getZoomedCellWidth()+this.oncoprint.getCellPadding()) : -1);
	};
	OncoprintRenderer.prototype.getCellXArray = function(length) {
		var cell_unit = this.oncoprint.getZoomedCellWidth() + this.oncoprint.getCellPadding();
		return _.map(_.range(0,length), function(x) { return x*cell_unit; });
	};
	OncoprintRenderer.prototype.getCellAreaWidth = function() {
		return this.oncoprint.getVisibleIdOrder().length*(this.oncoprint.getZoomedCellWidth() + this.oncoprint.getCellPadding());
	};
	OncoprintRenderer.prototype.getCellAreaHeight = function() {
		var track_tops = this.getTrackTops();
		var track_order = this.oncoprint.getTracks();
		if (track_order.length === 0) {
			return 0;
		} else {
			var last_track = track_order[track_order.length-1];
			return track_tops[last_track] + this.getRenderedTrackHeight(last_track);
		}
	};
	OncoprintRenderer.prototype.getLabelAreaWidth = function() {
		return this.label_area_width;
	};
	OncoprintRenderer.prototype.getLabelAreaHeight = function() {
		return this.getCellAreaHeight();
	};
	OncoprintRenderer.prototype.render = function() {
		throw "not implemented in abstract class";
	}
	return OncoprintRenderer;
})();