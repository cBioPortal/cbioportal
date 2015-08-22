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
window.oncoprint_events = {
	ADD_TRACK: 'add_track.oncoprint',
	REMOVE_TRACK: 'remove_track.oncoprint',
	MOVE_TRACK: 'move_track.oncoprint',
	SORT: 'sort.oncoprint',
	SET_CELL_PADDING: 'set_cell_padding.oncoprint',
	SET_CELL_WIDTH: 'set_cell_width.oncoprint',
	SET_TRACK_DATA: 'set_track_data.oncoprint',
	SET_ID_ORDER: 'set_id_order.oncoprint',
	CELL_CLICK: 'cell_click.oncoprint',
	CELL_MOUSEENTER: 'cell_mouseenter.oncoprint',
	CELL_MOUSELEAVE: 'cell_mouseleave.oncoprint',
	ONCOPRINT_MOUSEENTER: 'oncoprint_mouseenter.oncoprint',
	ONCOPRINT_MOUSELEAVE: 'oncoprint_mouseleave.oncoprint',
	SET_PRE_TRACK_PADDING: 'set_pre_track_padding.oncoprint',
	TRACK_INIT: 'init.track.oncoprint',
	UPDATE_RENDER_RULES: 'update_render_rules.cell_renderer.oncoprint',
	FINISHED_RENDERING: 'finished_rendering.oncoprint',
	FINISHED_POSITIONING: 'finished_positioning.renderer.oncoprint',
	SET_ZOOM: 'set_zoom.oncoprint',
	SET_SORT_DIRECTION: 'set_sort_direction.oncoprint',
	SET_VISIBLE_ID_ORDER: 'set_visible_ids.oncoprint'
};
