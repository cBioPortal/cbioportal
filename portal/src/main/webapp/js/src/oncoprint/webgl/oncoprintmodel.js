function ifndef(x, val) {
	return (typeof x === "undefined" ? val : x);
}

var OncoprintModel = (function() {
	function OncoprintModel(init_cell_padding, init_cell_padding_on, 
				init_zoom, init_cell_width,
				init_track_group_padding) {
		this.ids = [];
		this.visible_ids = {};
		this.track_groups = [];
		this.zoom = ifndef(init_zoom, 1);
		
		this.cell_padding = ifndef(init_cell_padding, 10);
		this.cell_padding_on = ifndef(init_cell_padding_on, true);
		this.cell_width = ifndef(init_cell_width, 10);
		this.track_group_padding = ifndef(init_track_group_padding, 10);
		
		this.track_data = {};
		this.track_rule_set = {};
		this.track_label = {};
		this.track_height = {};
		this.track_padding = {};
		this.track_data_id_key = {};
		this.track_tooltip_fn = {};
		this.track_removable = {};
		this.track_sort_cmp_fn = {};
		this.track_sort_direction_changeable = {};
	}
	
	OncoprintModel.prototype.toggleCellPadding = function() {
		this.cell_padding_on = !this.cell_padding_on;
		return this.cell_padding_on;
	}
	
	OncoprintModel.prototype.getCellPadding = function() {
		return (this.cell_padding*this.zoom)*(+this.cell_padding_on);
	}
	
	OncoprintModel.prototype.getZoom = function() {
		return this.zoom;
	}
	
	OncoprintModel.prototype.setZoom = function(z) {
		if (z <= 1 && z >= 0) {
			this.zoom = z;
		}
		return this.zoom;
	}
	
	OncoprintModel.prototype.getCellWidth = function() {
		return this.cell_width * this.zoom;
	}
	
	OncoprintModel.prototype.getTrackHeight = function(track_id) {
		return this.track_height[track_id];
	}
	
	OncoprintModel.prototype.getTrackPadding = function(track_id) {
		return this.track_padding[track_id];
	}
	
	OncoprintModel.prototype.getIds = function() {
		return this.ids;
	}
	
	OncoprintModel.prototype.getVisibleIds = function() {
		var visible_ids = this.visible_ids;
		return this.ids.filter(function(id) {
			return !!visible_ids[id];
		});
	}
	
	OncoprintModel.prototype.setIds = function(ids) {
		this.ids = ids.slice();
	}
	
	OncoprintModel.prototype.hideIds = function(to_hide, show_others) {
		var ids = this.ids;
		if (show_others) {
			for (var i=0, len=ids.length; i<len; i++) {
				this.visible_ids[ids[i]] = true;
			}
		}
		for (var j=0, len=to_hide.length; j<len; j++) {
			this.visible_ids[to_hide[j]] = false;
		}
	}
	
	OncoprintModel.prototype.moveTrackGroup = function(from_index, to_index) {
		var new_groups = [];
		var group_to_move = this.track_groups[from_index];
		for (var i=0; i<this.track_groups.length; i++) {
			if (i !== from_index && i !== to_index) {
				new_groups.push(this.track_groups[i]);
			}
			if (i === to_index) {
				new_groups.push(group_to_move);
			}
		}
		this.track_groups = new_groups;
		return this.track_groups;
	}
	
	OncoprintModel.prototype.addTrack = function(track_id, target_group,
						track_height, track_padding,
						data_id_key, tooltipFn,
						removable, label,
						sortCmpFn, sort_direction_changeable,
						data, rule_set) {
		this.track_label[track_id] = ifndef(label, "Label");
		this.track_height[track_id] = ifndef(track_height, 20);
		this.track_padding[track_id] = ifndef(track_padding, 5);
		this.track_data_id_key[track_id] = ifndef(data_id_key, 'id');
		this.track_tooltip_fn[track_id] = ifndef(tooltipFn, function(d) { return d+''; });
		this.track_removable[track_id] = ifndef(removable, false);
		this.track_sort_cmp_fn[track_id] = ifndef(sortCmpFn, function(a,b) { return 0; });
		this.track_sort_direction_changeable[track_id] = ifndef(sort_direction_changeable, false);
		this.track_data[track_id] = ifndef(data, []);
		this.track_rule_set[track_id] = ifndef(rule_set, undefined);
		
		target_group = ifndef(target_group, 0);
		while (target_group >= this.track_groups.length) {
			this.track_groups.push([]);
		}
		this.track_groups[target_group].push(track_id);
	}
	
	var _getContainingTrackGroup = function(oncoprint_model, track_id, return_reference) {
		var group;
		for (var i=0; i<oncoprint_model.track_groups.length; i++) {
			if (oncoprint_model.track_groups[i].indexOf(track_id) > -1) {
				group = oncoprint_model.track_groups[i];
				break;
			}
		}
		if (group) {
			return (return_reference ? group : group.slice());
		} else {
			return undefined;
		}
	}
	
	OncoprintModel.prototype.removeTrack = function(track_id) {
		delete this.track_data[track_id];
		delete this.track_rule_set[track_id];
		delete this.track_label[track_id];
		delete this.track_height[track_id];
		delete this.track_padding[track_id];
		delete this.track_data_id_key[track_id];
		delete this.track_tooltip_fn[track_id];
		delete this.track_removable[track_id];
		delete this.track_sort_cmp_fn[track_id];
		delete this.track_sort_direction_changeable[track_id];
		
		var containing_track_group = _getContainingTrackGroup(this, track_id, true);
		if (containing_track_group) {
			containing_track_group.splice(
				containing_track_group.indexOf(track_id), 1);
		}
	}
	
	OncoprintModel.prototype.getContainingTrackGroup = function(track_id) {
		return _getContainingTrackGroup(this, track_id, false);
	}
	
	OncoprintModel.prototype.getTrackGroups = function() {
		// TODO: make read-only
		return this.track_groups;
	}
	
	OncoprintModel.prototype.getTracks = function() {
		var ret = [];
		for (var i=0; i<this.track_groups.length; i++) {
			for (var j=0; j<this.track_groups[i].length; j++) {
				ret.push(this.track_groups[i][j]);
			}
		}
		return ret;
	}
	
	OncoprintModel.prototype.moveTrack = function(track_id, new_position) {
		var track_group = _getContainingTrackGroup(this, track_id, true);
		if (track_group) {
			track_group.splice(track_group.indexOf(track_id), 1);
			track_group.splice(new_position, 0, track_id);
		}
	}
	
	OncoprintModel.prototype.getTrackLabel = function(track_id) {
		return this.track_label[track_id];
	}
	
	OncoprintModel.prototype.getTrackTooltipFn = function(track_id) {
		return this.track_tooltip_fn[track_id];
	}
	
	OncoprintModel.prototype.getTrackDataIdKey = function(track_id) {
		return this.track_data_id_key[track_id];
	}
	
	OncoprintModel.prototype.getTrackGroupPadding = function() {
		return this.track_group_padding;
	}
	
	OncoprintModel.prototype.isTrackRemovable = function(track_id) {
		return this.track_removable[track_id];
	}
	
	OncoprintModel.prototype.getRuleSet = function(track_id) {
		return this.track_rule_set[track_id];
	}
	
	OncoprintModel.prototype.setRuleSet = function(track_id, rule_set) {
		this.track_rule_set[track_id] = rule_set;
	}
	
	OncoprintModel.prototype.getTrackData = function(track_id) {
		return this.track_data[track_id];
	}
	
	OncoprintModel.prototype.setTrackData = function(track_id, data) {
		this.track_data[track_id] = data;
	}

	return OncoprintModel;
})();

module.exports = OncoprintModel;