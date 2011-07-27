<div id="network_tabs" class="hidden-network-ui">
    <ul>
        <li><a href="#genes_tab"><span>Genes</span></a></li>
        <li><a href="#relations_tab"><span>Relations</span></a></li>
        <li><a href="#help_tab"><span>Help</span></a></li>
    </ul>
    <div id="genes_tab">
	    <div class="header">
			<table>
				<tr>
					<td>
						<input type="button" id="filter_genes" value="Hide"/>
						<input type="button" id="crop_genes" value="Crop"/>
					</td>
					<td>
					</td>
				</tr>
				<tr>
					<td>
						<input type="text" size="18" id="search" value=""/>
					</td>
					<td>
						<input type="button" id="search_genes" value="Search"/>
					</td>
				</tr>
			</table>
		</div>
    </div>
    <div id="relations_tab">
		<div>
	        <table>
	        	<tr class="in-same-component">
		        	<td class="edge-type-checkbox">
		        		<input type="checkbox" checked="checked">
		        		<label>In Same Component</label>
		        	</td>
	        	</tr>
	        	<tr class="in-same-component">
	        		<td>
	        			<div class="percent-bar"></div>	        			
	        		</td>
	        		<td>
	        			<div class="percent-value"></div>
	        		</td>
	        	</tr>
	        	<tr class="reacts-with">
		        	<td class="edge-type-checkbox">
		        		<input type="checkbox" checked="checked">
		        		<label>Reacts With</label>
		        	</td>
	        	</tr>
	        	<tr class="reacts-with">
	        		<td>
	        			<div class="percent-bar"></div>	        			
	        		</td>
	        		<td>
	        			<div class="percent-value"></div>
	        		</td>
	        	</tr>
	        	<tr class="state-change">
		        	<td class="edge-type-checkbox">
		        		<input type="checkbox" checked="checked">
		        		<label>State Change</label>
		        	</td>
	        	</tr>
	        	<tr class="state-change">
	        		<td>
	        			<div class="percent-bar"></div>	        			
	        		</td>
	        		<td>
	        			<div class="percent-value"></div>
	        		</td>
	        	</tr>
	        </table>
	    </div>
        <div class="footer">
			<input type="button" id="update_edges" value="Update"/>
		</div>
    </div>
    <div id="help_tab">
        Help!
    </div>
</div>

<div id="node_inspector" class="hidden-network-ui" title="Node Inspector">
	<div id="node_inspector_content" class="content ui-widget-content">
		<table class="xref">
			<tr><td><strong>Cross References:</strong></td></tr>
		</table>
		<table class="data">
			<tr><td><strong>Data:</strong></td></tr>
		</table>
	</div>
</div>

<div id="edge_inspector" class="hidden-network-ui" title="Edge Inspector">
	<div id="edge_inspector_content" class="content ui-widget-content">
		<table class="xref">
		</table>
		<table class="data">
			<tr><td><strong>Data:</strong></td></tr>
		</table>
	</div>
</div>