<div id="network_tabs" class="hidden-network-ui">
    <ul>
        <li><a href="#relations_tab"><span>Relations</span></a></li>
        <li><a href="#genes_tab"><span>Genes</span></a></li>
        <li><a href="#help_tab"><span>Help</span></a></li>
    </ul>
    <div id="relations_tab">
		<div>
	        <table>
	        	<tr><td>
	        		<input type="checkbox" id="in_same_component" checked="checked">
	        		<label>In Same Component</label>
	        	</td></tr>
	        	<tr><td>
	        		<input type="checkbox" id="reacts_with" checked="checked">
	        		<label>Reacts With</label>
	        	</td></tr>
	        	<tr><td>
	        		<input type="checkbox" id="state_change" checked="checked">
	        		<label>State Change</label>
	        	</td></tr>
	        </table>
	    </div>
        <div class="footer">
			<input type="button" id="update_edges" class="ui-state-default" value="Update"/>
		</div>
    </div>
    <div id="genes_tab">
	    <div class="header">
			<input type="button" id="filter_genes" class="ui-state-default" value="Filter out"/>
			<input type="button" id="crop_genes" class="ui-state-default" value="Crop"/>
		</div>
    </div>
    <div id="help_tab">
        Some help can be useful :) Some help can be useful :) Some help can be useful :)
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