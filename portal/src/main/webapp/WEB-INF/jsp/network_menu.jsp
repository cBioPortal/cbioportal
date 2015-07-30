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

  <nav id="myNavbar" class="navbar navbar-default my-nav-bar" role="navigation">
    <div class="container">
    	<ul id="network_menu" class="nav navbar-nav">
    	    <li class="dropdown">
    	    	<a href="#" data-toggle="dropdown" class="dropdown-toggle" >File <b class="caret"></b></a>
    	    	<ul class="dropdown-menu">
    	    		<li>
    	    			<a id="save_as_png"href="#">
    	    				Save as Image (PNG)
    	    			</a>
    	    		</li>
    	    	</ul>
    	    </li>
    	    <li class="dropdown">
    	    	<a href="#" data-toggle="dropdown" class="dropdown-toggle">Topology <b class="caret"></b></a>
    	    	<ul class="dropdown-menu">
    	    		<li>
    	    			<a id="hide_selected" href="#">
    	    				Hide Selected
    	    			</a>
    	    		</li>
              <li>
    	    			<a id="hide_non_selected" href="#">
    	    				Show Only Selected
    	    			</a>
    	    		</li>
    	    		<li>
    	    			<a id="unhide_all" href="#">
    	    				Show All Nodes
    	    			</a>
    	    		</li>
    	    		<li>
    	    			<a id="remove_disconnected" alt="remove_disconnected" href="#">
    	    				Remove Disconnected Nodes on Hide
    	    			</a>
    	    		</li>
    	    	</ul>
    	    </li>
    	    <li class="dropdown">
    	    	<a href="#" data-toggle="dropdown" class="dropdown-toggle">View <b class"caret"></b></a>
    	    	<ul class="dropdown-menu">
    	    		<li>
    	    			<a id="show_profile_data" href="#">
    	    				Always Show Profile Data
    	    			</a>
    	    		</li>
    	    		<li>
    	    			<a id="merge_links" href="#">
    	    				Merge Interactions
    	    			</a>
    	    		</li>
    	    		<li>
    	    			<a id="show_node_labels" href= "#">
    	    				Show Node Labels
    	    			</a>
    	    		</li>
    	    		<li>
    	    			<a id="show_pan_zoom_control" herf="#">
    	    				Show Pan-Zoom Control
    	    			</a>
    	    		</li>
              <li class="divider"></li>
    	    		<li>
    	    			<a id="highlight_neighbors" href="#">
    	    				Highlight Neighbors
    	    			</a>
    	    		</li>
    	    		<li>
    	    			<a id="remove_highlights" href="#">
    	    				Remove Highlights
    	    			</a>
    	    		</li>
    	    	</ul>
    	    </li>
    	    <li class="dropdown">
    	    	<a href="#" data-toggle="dropdown" class="dropdown-toggle">Layout<b class="caret"></b></a>
    	    	<ul class="dropdown-menu">
    	    		<li>
    	    			<a id="perform_layout" href="#">
    	    				Perform Layout
    	    			</a>
    	    		</li>
    	    		<li>
    	    			<a id="layout_properties" href="#">
    	    				Layout Properties ...
    	    			</a>
    	    		</li>
    	    		<li>
    	    			<a id="auto_layout" href="#">
    	    				Auto Layout on Changes
    	    			</a>
    	    		</li>
    	    	</ul>
    	    </li>
                <%if(includeNetLegend){%>
    	    <li class="dropdown">
    	    	<a href="#" data-toggle="dropdown" class="dropdown-toggle">Legends <b class="caret"></b></a>
    	    	<ul class="dropdown-menu">
    	    		<li>
    	    			<a id="show_node_legend" href="#">
    	    				Gene Legend
    	    			</a>
    	    		</li>
    	    		<li>
    	    			<a id="show_drug_legend" href="#">
    	    				Drug Legend
    	    			</a>
    	    		</li>
    	    		<li>
    	    			<a id="show_edge_legend" href="#">
    	    				Interaction Legend
    	    			</a>
    	    		</li>
    	    	</ul>
    	    </li>
                <%}%>
    	</ul>
    </div>
  </nav>

<div id="quick_info_div" class="hidden-network-ui">
	<label id="quick_info_label">Double-click nodes/edges for details</label>
</div>
