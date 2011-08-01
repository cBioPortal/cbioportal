<div id="network_menu_div" class="hidden-network-ui">
	<ul id="network_menu">
	    <li>
	    	<a id="network_menu_file">File</a>
	    	<ul>
	    		<li>
	    			<a id="save_as_png" onclick="handleMenuEvent('save_as_png')">
	    				Save as image (PNG)
	    			</a>
	    		</li>
	    		<li>
	    			<a id="joker_button" onclick="handleMenuEvent('joker_button')">
	    				TEMP..
	    			</a>
	    		</li>
	    	</ul>
	    </li>
	    <li>
	    	<a id="network_menu_topology">Topology</a>
	    	<ul>
	    		<li>
	    			<a id="hide_selected" onclick="handleMenuEvent('hide_selected')">
	    				Hide Selected
	    			</a>
	    		</li>
	    		<li>
	    			<a id="unhide_all" onclick="handleMenuEvent('unhide_all')">
	    				Unhide All
	    			</a>
	    		</li>
	    		<li>
	    			<a id="auto_layout" onclick="handleMenuEvent('auto_layout')">
	    				Auto Layout on Changes
	    			</a>
	    		</li>
	    	</ul>
	    </li>
	    <li>
	    	<a id="network_menu_view">View</a>
	    	<ul>
	    		<li>
	    			<a id="perform_layout" onclick="handleMenuEvent('perform_layout')">
	    				Perform Layout
	    			</a>
	    		</li>
	    		<li>
	    			<a id="layout_properties" onclick="handleMenuEvent('layout_properties')">
	    				Layout Properties ...
	    			</a>
	    		</li>
	    		<li>
	    			<a id="show_profile_data" onclick="handleMenuEvent('show_profile_data')">
	    				Always Show Profile Data
	    			</a>
	    		</li>
	    		<li>
	    			<a id="highlight_neighbors" onclick="handleMenuEvent('highlight_neighbors')">
	    				Highlight Neighbors
	    			</a>
	    		</li>
	    		<li>
	    			<a id="remove_highlights" onclick="handleMenuEvent('remove_highlights')">
	    				Remove Highlights
	    			</a>
	    		</li>
	    		<li>
	    			<a id="merge_links" onclick="handleMenuEvent('merge_links')">
	    				Merge Interactions
	    			</a>
	    		</li>
	    		<li>
	    			<a id="show_node_labels" onclick="handleMenuEvent('show_node_labels')">
	    				Show Node Labels
	    			</a>
	    		</li>
	    		<li>
	    			<a id="show_pan_zoom_control" onclick="handleMenuEvent('show_pan_zoom_control')">
	    				Show Pan-Zoom Control
	    			</a>
	    		</li>
	    	</ul>
	    </li>
	</ul>
</div>

<div id="settings_dialog" class="hidden-network-ui" title="Layout Properties">
	<div id="fd_layout_settings" class="content ui-widget-content">
		<table align="center">
			<tr title="The gravitational constant. Negative values produce a repulsive force.">
				<td align="right">
					<label>Gravitation</label>
				</td>
				<td>
					<input type="text" id="gravitation" value=""/>
				</td>
			</tr>
			<tr title="The default mass value for nodes.">
				<td align="right">
					<label>Node mass</label>
				</td>
				<td>
					<input type="text" id="mass" value=""/>
				</td>
			</tr>
			<tr title="The default spring tension for edges.">
				<td align="right">
					<label>Edge tension</label>
				</td>
				<td>
					<input type="text" id="tension" value=""/>
				</td>
			</tr>
			<tr title="The default spring rest length for edges.">
				<td align="right">
					<label>Edge rest length</label>
				</td>
				<td>
					<input type="text" id="restLength" value=""/>
				</td>
			</tr>
			<tr title="The co-efficient for frictional drag forces.">
				<td align="right">
					<label>Drag co-efficient</label>
				</td>
				<td>
					<input type="text" id="drag" value=""/>
				</td>
			</tr>
			<tr title="The minimum effective distance over which forces are exerted.">
				<td align="right">
					<label>Minimum distance</label>
				</td>
				<td>
					<input type="text" id="minDistance" value=""/>
				</td>
			</tr>
			<tr title="The maximum distance over which forces are exerted.">
				<td align="right">
					<label>Maximum distance</label>
				</td>
				<td>
					<input type="text" id="maxDistance" value=""/>
				</td>
			</tr>
			<tr title="The name of the edge attribute that contains the weights.">
				<td align="right">
					<label>Weight Attribute</label>
				</td>
				<td>
					<input type="text" id="weightAttr" value=""/>
				</td>
			</tr>
			<tr title="How to interpret weight values.">
				<td align="right">
					<label>Weight Normalization</label>
				</td>				
				<td>
					<select id="weightNorm" size="1">
						<option id="norm_linear" value="linear">linear</option>
						<option id="norm_invlinear" value="invlinear">invlinear</option>
						<option id="norm_log" value="log">log</option>
					</select>
				</td>
			</tr>
			<tr title="The number of iterations to run the simulation.">
				<td align="right">
					<label>Iterations</label>
				</td>
				<td>
					<input type="text" id="iterations" value=""/>
				</td>
			</tr>
			<tr title="The maximum time to run the simulation, in milliseconds.">
				<td align="right">
					<label>Maximum Time</label>
				</td>
				<td>
					<input type="text" id="maxTime" value=""/>
				</td>
			</tr>
			<tr title="If checked, Cytoscape Web automatically tries to stabilize results that seems unstable after running the regular iterations.">
				<td align="right">
					<label>Auto Stabilize</label>
				</td>
				<td align="left">
					<input type="checkbox" id="autoStabilize" value="true" checked="checked"/>
				</td>
			</tr>
		</table>
	</div>
	<div class="footer">
		<input type="button" id="save_layout_settings" class="ui-state-default" value="Save"/>
		<input type="button" id="default_layout_settings" class="ui-state-default" value="Default"/>
	</div>
</div>