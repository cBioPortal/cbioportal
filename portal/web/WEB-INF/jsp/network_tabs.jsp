<div id="network_tabs" class="hidden-network-ui">
    <ul>
        <li><a href="#genes_tab"><span>Genes</span></a></li>
        <li><a href="#relations_tab"><span>Interactions</span></a></li>
        <li><a href="#help_tab"><span>Help</span></a></li>
    </ul>
    <div id="genes_tab">
	    <div class="header">
			<table>
				<tr>
					<td>
						<input type="button" id="filter_genes" value="Hide"/>
						<input type="button" id="crop_genes" value="Crop"/>
						<input type="button" id="unhide_genes" value="Unhide"/>						
					</td>
				</tr>
				<tr>
					<td>
						<input type="text" size="17" id="search" value=""/>
						<input type="button" id="search_genes" value="Search"/>
					</td>
				</tr>
			</table>
		</div>
    </div>
    <div id="relations_tab">
		<div>
	        <table>
	        	<tr class="edge-type-header">
	        		<td>
	        			<strong>Type:</strong>
	        		</td>
	        	</tr>
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
	        	<tr class="edge-source-header">
	        		<td>
	        			<strong>Source:</strong>
	        		</td>
	        	</tr>
	        	<tr class="reactome">
	        		<td class="edge-source-checkbox">
		        		<input type="checkbox" checked="checked">
		        		<label>Reactome</label>
		        	</td>
	        	</tr>
	        	<tr class="nci">
	        		<td class="edge-source-checkbox">
		        		<input type="checkbox" checked="checked">
		        		<label>NCI</label>
		        	</td>
	        	</tr>
	        	<tr class="unknown">
	        		<td class="edge-source-checkbox">
		        		<input type="checkbox" checked="checked">
		        		<label>Unknown</label>
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
		<table class="data"></table>
		<table class="profile-header"></table>
		<table class="profile"></table>
		<table class="xref"></table>
	</div>
</div>

<div id="edge_inspector" class="hidden-network-ui" title="Edge Inspector">
	<div id="edge_inspector_content" class="content ui-widget-content">
		<table class="data"></table>
		<table class="xref"></table>
	</div>
</div>