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
Boolean includeHelpTab = (Boolean)request.getAttribute("include_network_help_tab");
if (includeHelpTab==null) {
    includeHelpTab = Boolean.TRUE;
}
%>

<div id="network_tabs" class="hidden-network-ui">
    <ul>
        <li><a href="#genes_tab" class="network-tab-ref" title="Genes & Drugs (Nodes)"><span>Genes</span></a></li>
        <li><a href="#relations_tab" class="network-tab-ref"
               title="Edges between nodes"><span>Interactions</span></a></li>
	    <li><a href="#element_details_tab" class="network-tab-ref"
	           title="Node details"><span>Details</span></a></li>
        <%if(includeHelpTab){%>
        <li><a href="#help_tab" class="network-tab-ref" title="About & Help"><span>Help</span></a></li>
        <%}%>
    </ul>
    <div id="genes_tab">
	    <div class="header">
		  <span class="title"><label >Drugs of Specified Genes</label></span><br><br>
	      <div class="combo">
			<select id="drop_down_select" title="Show drugs options">
			  <option value="HIDE_DRUGS">Hide Drugs</option>
              <option value="SHOW_CANCER"> Show Cancer Drugs</option>
              <option value="SHOW_FDA"> Show FDA Approved Drugs</option>
			  <option value="SHOW_ALL">Show All Drugs</option>
			</select>
		  </div>
		    <span class="title"><label>Genes</label></span><br><br>
	    	<div id="slider_area">
	    		<label>Filter Neighbors by Alteration (%)</label>
	    		<div id="weight_slider_area">
		    		<span class="slider-value">
		    			<input id="weight_slider_field" type="text" value="0" title="weight slider"/>
		    		</span>
		    		<span class="slider-min"><label>0</label></span>
		    		<span class="slider-max"><label>MAX</label></span>
		    		<div id="weight_slider_bar"></div>
	    		</div>

	    		<div id="affinity_slider_area" class="hidden-network-ui">
	    			<span class="slider-value">
	    				<input id="affinity_slider_field" type="text" value="0.80" title="affinity slider"/>
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
						<button id="filter_genes" class="tabs-button" title="Hide Selected">
							<span class="ui-button-icon-primary ui-icon ui-icon-circle-minus"></span>
						</button>
					</td>
					<td>
						<button id="crop_genes" class="tabs-button" title="Show Only Selected">
							<span class="ui-button-icon-primary ui-icon ui-icon-crop"></span>
						</button>
					</td>
					<td>
						<button id="unhide_genes" class="tabs-button" title="Show All">
							<span class="ui-button-icon-primary ui-icon ui-icon-circle-plus"></span>
						</button>
					</td>
					<td>
						<input type="text" id="search_box" value="" title="search genes"/>
					</td>
					<td>
						<button id="search_genes" class="tabs-button" title="Search">
							<span class="ui-button-icon-primary ui-icon ui-icon-search"></span>
						</button>
					</td>
				</tr>
				</table>
				<table id="network-resubmit-query">
					<tr>
	        			<td>
	        				<label class="button-text">Submit New Query</label>
	        			</td>
	        			<td>
	        				<button id="re-submit_query" class="tabs-button" title="Submit New Query with Genes Selected Below">
						        <span class="ui-button-icon-primary ui-icon ui-icon-play"></span>
	        				</button>
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
	        </table>
	        <table id="edge_source_filter">
	        </table>
	    </div>
        <!-- <div class="footer">
        	<table>
        		<tr>
        			<td>
        				<label class="button-text">Update</label>
        			</td>
        			<td>
        				<button id="update_edges" class="tabs-button" title="Update">
					        <span class="ui-button-icon-primary ui-icon ui-icon-refresh"></span>
        				</button>
        			</td>
        		</tr>
        	</table>
		</div> -->
    </div>
	<div id="element_details_tab">
		<div class="error">
      Currently there is no selected node/edge. Please, select a node/edge to see details.		</div>
		<div class="genomic-profile-content"></div>
		<div class="biogene-content"></div>
		<div class="drug-info-content"></div>
		<div class="edge-inspector-content"></div>
	</div>
    <%if(includeHelpTab){%>
    <div id="help_tab">
        <jsp:include page="network_help.jsp"></jsp:include>
    </div>
    <%}%>
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
