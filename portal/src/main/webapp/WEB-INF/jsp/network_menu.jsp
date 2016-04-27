<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 -
 - This library is distributed in the hope that it will be useful, but WITHOUT
 - ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 - FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 - is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 - obligations to provide maintenance, support, updates, enhancements or
 - modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 - liable to any party for direct, indirect, special, incidental or
 - consequential damages, including lost profits, arising out of the use of this
 - software and its documentation, even if Memorial Sloan-Kettering Cancer
 - Center has been advised of the possibility of such damage.
 --%>

<%--
 - This file is part of cBioPortal.
 -
 - cBioPortal is free software: you can redistribute it and/or modify
 - it under the terms of the GNU Affero General Public License as
 - published by the Free Software Foundation, either version 3 of the
 - License.
 -
 - This program is distributed in the hope that it will be useful,
 - but WITHOUT ANY WARRANTY; without even the implied warranty of
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 - GNU Affero General Public License for more details.
 -
 - You should have received a copy of the GNU Affero General Public License
 - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%
Boolean includeNetLegend = (Boolean)request.getAttribute("include_network_legend");
if (includeNetLegend==null) {
    includeNetLegend = Boolean.TRUE;
}
%>
<div id="network_menu_div" class="hidden-network-ui">
	<ul id="network_menu">
	    <li>
	    	<a id="network_menu_file">File</a>
	    	<ul id="file_menu">
	    		<li>
	    			<a id="save_as_png">
	    				Save as Image (PNG)
	    			</a>
	    		</li>
	    	</ul>
	    </li>
	    <li>
	    	<a id="network_menu_topology">Topology</a>
	    	<ul id="topology_menu">
	    		<li>
	    			<a id="hide_selected">
	    				Hide Selected
	    			</a>
	    		</li>
	    		<li>
	    			<a id="hide_non_selected">
	    				Show Only Selected
	    			</a>
	    		</li>
	    		<li>
	    			<a id="unhide_all">
	    				Show All
	    			</a>
	    		</li>
	    		<li>
	    			<a id="remove_disconnected" alt="remove_disconnected">
	    				Remove Disconnected Nodes on Hide
	    			</a>
	    		</li>
	    	</ul>
	    </li>
	    <li>
	    	<a id="network_menu_view">View</a>
	    	<ul id="view_menu">
	    		<li>
	    			<a id="show_profile_data">
	    				Always Show Profile Data
	    			</a>
	    		</li>
	    		<li>
	    			<a id="merge_links">
	    				Merge Interactions
	    			</a>
	    		</li>
	    		<li>
	    			<a id="show_node_labels">
	    				Show Node Labels
	    			</a>
	    		</li>
	    		<li>
	    			<a id="show_pan_zoom_control">
	    				Show Pan-Zoom Control
	    			</a>
	    		</li>
	    		<li>
	    			<a id="highlight_neighbors">
	    				Highlight Neighbors
	    			</a>
	    		</li>
	    		<li>
	    			<a id="remove_highlights">
	    				Remove Highlights
	    			</a>
	    		</li>
	    	</ul>
	    </li>
	    <li>
	    	<a id="network_menu_layout">Layout</a>
	    	<ul id="layout_menu">
	    		<li>
	    			<a id="perform_layout">
	    				Perform Layout
	    			</a>
	    		</li>
	    		<li>
	    			<a id="layout_properties">
	    				Layout Properties ...
	    			</a>
	    		</li>
	    		<li>
	    			<a id="auto_layout">
	    				Auto Layout on Changes
	    			</a>
	    		</li>
	    	</ul>
	    </li>
            <%if(includeNetLegend){%>
	    <li>
	    	<a id="network_menu_legends">Legends</a>
	    	<ul id="legends_menu">
	    		<li>
	    			<a id="show_node_legend">
	    				Gene Legend
	    			</a>
	    		</li>
	    		<li>
	    			<a id="show_drug_legend">
	    				Drug Legend
	    			</a>
	    		</li>
	    		<li>
	    			<a id="show_edge_legend">
	    				Interaction Legend
	    			</a>
	    		</li>
	    	</ul>
	    </li>
            <%}%>
	</ul>
</div>

<div id="quick_info_div" class="hidden-network-ui">
	<label id="quick_info_label">Double-click nodes/edges for details. Right-click edges for detailed process-level network view.</label>
</div>
