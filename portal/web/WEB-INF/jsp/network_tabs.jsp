<div id="network_tabs" class="hidden-network-ui">
    <ul>
        <li><a href="#genes_tab"><span>Genes & Drugs</span></a></li>
        <li><a href="#relations_tab"><span>Interactions</span></a></li>
        <li><a href="#help_tab"><span>Help</span></a></li>
    </ul>
    <div id="genes_tab">
	    <div class="header">
		  <span class="title"><label >Drugs</label></span><br><br>
	      <div class="combo">
			<select id="drop_down_select">
			  <option value="SHOW_ALL">Show All Drugs</option>
			  <option value="HIDE_DRUGS">Hide Drugs</option>
			  <option value="SHOW_FDA"> Show FDA Approved Drugs</option>
			</select>
		  </div>
		    <span class="title"><label>Genes</label></span><br><br>
	    	<div id="slider_area">
	    		<label>Filter Neighbors by Alteration (%)</label>
	    		<div id="weight_slider_area">
		    		<span class="slider-value">
		    			<input id="weight_slider_field" type="text" value="0"/>
		    		</span>
		    		<span class="slider-min"><label>0</label></span>
		    		<span class="slider-max"><label>MAX</label></span>
		    		<div id="weight_slider_bar"></div>
	    		</div>
	    		
	    		<div id="affinity_slider_area" class="hidden-network-ui">
	    			<span class="slider-value">
	    				<input id="affinity_slider_field" type="text" value="0.80"/>
	    			</span>
	    			<span class="slider-min"><label>0</label></span>
		    		<span class="slider-max"><label>1.0</label></span>
		    		<div id="affinity_slider_bar"></div>
	    		</div>
    		</div>
    		<div id="control_area">
    			<table>
    			<tr>
    				<td>
						<button id="filter_genes" class="tabs-button" title="Hide Selected"></button>
					</td>
					<td>
						<button id="crop_genes" class="tabs-button" title="Show Only Selected"></button>
					</td>
					<td>
						<button id="unhide_genes" class="tabs-button" title="Show All"></button>
					</td>
					<td>					
						<input type="text" id="search_box" value=""/>
					</td>
					<td>
						<button id="search_genes" class="tabs-button" title="Search"></button>
					</td>
				</tr>
				</table>
				<table>
					<tr>
	        			<td>
	        				<label class="button-text">Submit New Query</label>
	        			</td>
	        			<td>
	        				<button id="re-submit_query" class="tabs-button" title="Submit New Query with Genes Selected Below"></button>
	        			</td>
	        		</tr>
        		</table>
			</div>			
		</div>
		<div id="gene_list_area">
		</div>
    </div>
    <div id="relations_tab">
		<div>
	        <table id="edge_type_filter">
	        	<tr class="edge-type-header">
	        		<td>
	        			<label class="heading">Type:</label>
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
	        	<tr class="targeted-by-drug">
		        	<td class="edge-type-checkbox">
		        		<input type="checkbox" checked="checked">
		        		<label>Targeted By Drug</label>
		        	</td>
	        	</tr>
	        	<tr class="targeted-by-drug">
	        		<td>
	        			<div class="percent-bar"></div>	        			
	        		</td>
	        		<td>
	        			<div class="percent-value"></div>
	        		</td>
	        	</tr>
	        	<tr class="other">
		        	<td class="edge-type-checkbox">
		        		<input type="checkbox" checked="checked">
		        		<label>Other</label>
		        	</td>
	        	</tr>
	        	<tr class="other">
	        		<td>
	        			<div class="percent-bar"></div>	        			
	        		</td>
	        		<td>
	        			<div class="percent-value"></div>
	        		</td>
	        	</tr>
	        </table>
	        <table id="edge_source_filter">
	        	<tr class="edge-source-header">
	        		<td>
	        			<label class="heading">Source:</label>
	        		</td>
	        	</tr>
	        </table>
	    </div>
        <div class="footer">
        	<table>
        		<tr>
        			<td>
        				<label class="button-text">Update</label>
        			</td>
        			<td> 
        				<button id="update_edges" class="tabs-button" title="Update"></button>
        			</td>
        		</tr>
        	</table>
		</div>
    </div>
    <div id="help_tab">
        <jsp:include page="network_help.jsp"></jsp:include>
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

<div id="node_legend" class="hidden-network-ui" title="Gene Legend">
	<div id="node_legend_content" class="content ui-widget-content">
		<img src="images/network/gene_legend.png"/>
	</div>
</div>

<div id="edge_inspector" class="hidden-network-ui" title="Edge Inspector">
	<div id="edge_inspector_content" class="content ui-widget-content">
		<table class="data"></table>
		<table class="xref"></table>
	</div>
</div>

<div id="edge_legend" class="hidden-network-ui" title="Interaction Legend">
	<div id="edge_legend_content" class="content ui-widget-content">
		<img src="images/network/interaction_legend.png"/>
	</div>
</div>

<% /*
<div id="edge_legend" class="hidden-network-ui" title="Interaction Legend">
	<div id="edge_legend_content" class="content ui-widget-content">
		<table id="edge_type_legend">
			<tr class="edge-type-header">
	        	<td>
	        		<strong>Edge Types:</strong>
	        	</td>
	        </tr>
        	<tr class="in-same-component">
        		<td class="label-cell">
        			<div class="type-label">In Same Component</div>
        		</td>
        		<td class="color-cell">
        			<div class="color-bar"></div>
        		</td>
        	</tr>
        	<tr class="reacts-with">
        		<td class="label-cell">
        			<div class="type-label">Reacts With</div>
        		</td>
        		<td class="color-cell">
        			<div class="color-bar"></div>
        		</td>
        	</tr>
        	<tr class="state-change">
        		<td class="label-cell">
        			<div class="type-label">State Change</div>
        		</td>
        		<td class="color-cell">
        			<div class="color-bar"></div>
        		</td>
        	</tr>
        	<tr class="other">
        		<td class="label-cell">
        			<div class="type-label">Other</div>
        		</td>
        		<td class="color-cell">
        			<div class="color-bar"></div>
        		</td>
        	</tr>
        	<tr class="merged-edge">
        		<td class="label-cell">
        			<div class="type-label">Merged (with different types) </div>
        		</td>
        		<td class="color-cell">
        			<div class="color-bar"></div>
        		</td>
        	</tr>
        </table>
	</div>
</div>
*/ %>
