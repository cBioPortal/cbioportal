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

<p class="heading">
	About the Network View
<p>
<p class="regular">
	The network view shows the genes you entered (referred to as <span class="italic">seed nodes</span>) in the context of biological interactions derived from public pathway databases.
	Each gene in the network view is color-coded with multi-dimensional genomic data derived from the cancer study you have selected.
</p>

<p class="heading">
	Source of Pathway Data
<p>
<p class="regular">
	Pathway and interaction data is from <a href="http://www.hprd.org/" target="_blank">HPRD</a>,
	<a href="http://www.reactome.org/" target="_blank">Reactome</a>,
	<a href="http://pid.nci.nih.gov/" target="_blank">NCI-Nature Pathway Interaction Database</a>,
	and the <a href="http://cancer.cellmap.org/cellmap/" target="_blank">MSKCC Cancer Call Map</a>,
	as derived from <a href="http://pathwaycommons.org" target="_blank">Pathway Commons</a>.
</p>

<p class="heading">
	Source of Drug Data
<p>
<p class="regular">
	Drug data is derived from <a href="https://bitbucket.org/armish/pihelper" target="_blank">PiHelper</a>.
</p>

<p class="heading">
	Seed Nodes vs. Linker Nodes
</p>
<p class="regular">
	A <span class="italic">seed node</span> represents a gene that you have entered.
	A <span class="italic">linker node</span> represents a gene that connects to one or more of your seed genes.
</p>
<p class="regular">
	<span class="italic">Seed nodes</span> are represented with a thick border:<br></br>
	<img class="seed_node_img" src="images/network/seed_node.png" alt='seed node'></img>
</p>
<p class="regular">
	<span class="italic">Linker nodes</span> are represented with a thin border:<br></br>
	<img class="linker_node_img" src="images/network/linker_node.png" alt='linker node'></img>
</p>

<p class="heading">
	Visualization Summary of Genomic Data
</p>
<p class="regular">
	The exact genomic data displayed on the network depends on the genomic profiles you have selected.
	For example, you can chose to include mutation, copy number and mRNA expression profiles.
</p>
<p class="regular">
	By default, each node is color coded along a white to red color gradient,
	indicating the total frequency of alteration across the selected case set
	(deeper red indicates higher frequency of alteration).
</p>
<p class="regular">
	For example, EGFR is frequently altered in glibolastoma:<br></br>
	<img class="altered_node_img" src="images/network/altered_node.png" alt='altered node'></img>
</p>
<p class="regular">
	By contrast, STAT3 is not altered at all in glioblastoma:<br></br>
	<img class="not_altered_node_img" src="images/network/not_altered_node.png" alt='not altered node'></img>
</p>

<p class="regular">
	If you mouse over a node, or select "View::Always Show Profile Data",
	you will see additional details regarding the genomic alterations affecting the gene.
	This breaks down into mutation, copy number and mRNA expression changes affecting the gene across all cases.
</p>
<p class="regular">
	Click <a id="show_node_legend">here</a> to see the gene legend.
</p>

<p class="heading">
	Drug Information
</p>
<p class="regular">
	Drugs targeting genes in the network are hidden by default. If you would like
	to see them, select "Show All Drugs" or, "Show FDA Approved Drugs" or "Show Cancer Drugs"
	from the drop-down box under the "Genes &amp; Drugs" tab.
</p>
<p class="regular">
	Number of Genes Targeted shown in the drug inspector refers to the total number of genes
	 (regardless of whether or not any such gene is in the current network of interest) targeted by this drug.
</p>
<p class="regular">
	Click <a id="show_drug_legend">here</a> to see the drug legend.
</p>

<p class="heading">
	Understanding Interaction and Edge Types
</p>
<p class="regular">
	The interaction types are derived from the <a href="http://biopax.org" target="_blank">BioPAX</a> to binary interaction mapping rules defined within Pathway Commons.
	They are encoded by different edge colors and can be seleted on the "Interactions" tab to the right of the network.
	In addition, if selected, drug-gene interactions are shown as edges in the network. The interaction types are:
	<ul>
		<li>
		  <span class="bold">Controls-state-change-of:
		  </span>First protein controls a reaction that changes the state of the second protein.
		</li>
		<li>
		  <span class="bold">Controls-transport-of:
		  </span>First protein controls a reaction that changes the cellular location of the second protein.
		</li>
		<li>
		  <span class="bold">Controls-phosphorylation-of:
		  </span>First protein controls a reaction that changes the phosphorylation status of the second protein.
		</li>
		<li>
		  <span class="bold">Controls-expression-of:
		  </span>First protein controls a conversion or a template reaction that changes expression of the second protein.
		</li>
		<li>
		  <span class="bold">Catalysis-precedes:
		  </span>First protein controls a reaction whose output molecule is input to another reaction controled by the second protein.
		</li>
		<li>
		  <span class="bold">In-complex-with:
		  </span>Proteins are members of the same complex.
		</li>
		<li>
		  <span class="bold">Interacts-with:
		  </span>Proteins are participants of the same MolecularInteraction.
		</li>
		<li>
		  <span class="bold">Neighbor-of:
		  </span>Proteins are participants or controlers of the same interaction.
		</li>
		<li>
		  <span class="bold">Consumption-controled-by:
		  </span>The small molecule is consumed by a reaction that is controled by a protein
		</li>
		<li>
		  <span class="bold">Controls-production-of:
		  </span>The protein controls a reaction of which the small molecule is an output.
		</li>
		<li>
		  <span class="bold">Controls-transport-of-chemical:
		  </span>The protein controls a reaction that changes cellular location of the small molecule.
		</li>
		<li>
		  <span class="bold">Chemical-affects:
		  </span>A small molecule has an effect on the protein state.
		</li>
		<li>
		  <span class="bold">Reacts-with:
		  </span>Small molecules are input to a biochemical reaction.
		</li>
		<li>
		  <span class="bold">Used-to-produce:
		  </span>A reaction consumes a small molecule to produce another small molecule.
		</li>
	</ul>
</p>
<p class="regular">
	Click <a id="show_edge_legend">here</a> to see the color codes.
</p>
<p class="regular">
	Complete details are available on the <a href="http://www.pathwaycommons.org/pc2/formats" target="_blank">Pathway Commons web site</a>.
</p>
<p class="regular">
	By default, redundant interactions are merged are merged into a single edge.
	To see all interactions, uncheck "Merge Interactions" in the "View" menu.
</p>

<p class="heading">
	Complexity Management
</p>
<p class="regular">
	There are a number of options to better deal with complex networks:
	<ul>
		<li>
			<span class="bold">Hide Selected/Crop:</span>
			Selected nodes can be hidden using "Topology::Hide Selected".
			Alternatively, you can select the set of nodes that you would like to view and hide the rest
			of the network using "Topology::Show Only Selected".
			Alternatively, buttons are available for these operations on the "Genes &amp; Drugs" tab.
		</li>
		<li>
			<span class="bold">Filter by Interaction Type or Source:</span>
			If you are interested in only certain types of interactions or interactions from selected sources,
			you may use the filtering mechanisms on the "Interactions" tab by checking the corresponding check boxes and clicking "Update".
		</li>
		<li>
			<span class="bold">Filter by Total Alteration:</span>
			Networks can be filtered based on alteration frequencies of individual nodes using a slider under the "Genes &amp; Drugs" tab.
			You can specify a threshold of total alteration frequency - nodes with alteration frequencies below the threshold will be filtered out,
			but seed nodes are always kept in the network.
		</li>
		<li>
			<span class="bold">Filter Drugs by FDA Approval:</span>
			Networks can be filtered based on whether drugs associated with genes of this network are FDA approved or not.
		</li>
		<li>
			<span class="bold">Filter Cancer Drugs:</span>
			Networks can be filtered based on whether drugs associated with genes of this network are cancer drugs or not.
			Notice that all cancer drugs are FDA approved.
		</li>
	</ul>
</p>
<p class="regular">
	All filtering can be undone by clicking "Unhide" in the "Topology" menu.
</p>
<p class="regular">
	When the flag "Remove Disconnected Nodes on Hide" in the "Topology" menu is checked,
	an automatic layout is performed upon all changes to the network topology.
</p>

<p class="heading">
	Performing Layout
</p>
<p class="regular">
	A Force-Directed layout algorithm is used by default.
	However, you may choose to re-perform the layout with different parameters (by selecting "Layout::Layout Properties ...")  or
	after the topology of the network changes with operations such as hiding or filtering.
	If you would like the layout to be performed automatically upon such operations
	simply check "Layout::Auto Layout on Changes".
</p>

<p class="heading">
	Exporting Networks
</p>
<p class="regular">
	You can export a network to a PNG file.
	To do so, select "File::Save as Image (PNG)".
	We do not currently support export to PDF.
</p>

<p class="heading">
	Detailed Process Level (SBGN) View
</p>
<p class="regular">
	When you are interested in process level details of an interaction,
  you may either right-click on that interaction or click the "Detailed Process (SBGN)" button
  in the Details tab while inspecting the interaction.
  This will pop up a detailed process view in <a href="http://www.sbgn.org/Main_Page" target="_blank">SBGN Process Description Language</a>.
  The shown network corresponds to all paths between source and target genes of that interaction as returned by <a href="http://www.pathwaycommons.org/pc2/" target="_blank">Pathway Commons' web service</a>.
	SBGN view allows users to modify the process level view in many ways, including changing its layout and topology through complexity management techniques such as hiding or collapsing.
  You may also store the current network as a static image or in <a href="http://www.sbgn.org/LibSBGN/Exchange_Format" target="_blank">SBGN-ML format</a>. For further help, please refer to the Help menu of this view.
</p>
<p class="heading">
	Technology
</p>
<p class="regular">
	Network visualization is powered by <a href="http://js.cytoscape.org/" target="_blank">Cytoscape.js</a> and <a href="https://github.com/iVis-at-Bilkent/sbgnviz-js" target="_blank">SBGNViz.js</a>.
</p>
