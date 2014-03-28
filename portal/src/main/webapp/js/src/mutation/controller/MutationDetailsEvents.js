/**
 * Singleton utility class to define custom events triggered by
 * Mutation Details components.
 *
 * @author Selcuk Onur Sumer
 */
var MutationDetailsEvents = (function()
{
	var _lollipopSelected = "mutationDiagramLollipopSelected";
	var _lollipopDeselected = "mutationDiagramLollipopDeselected";
	var _allLollipopsDeselected = "mutationDiagramAllDeselected";
	var _lollipopMouseover = "mutationDiagramLollipopMouseover";
	var _lollipopMouseout = "mutationDiagramLollipopMouseout";
	var _diagramPlotUpdated = "mutationDiagramPlotUpdated";
	var _diagramPlotReset = "mutationDiagramPlotReset";
	var _mutationTableFiltered = "mutationTableFiltered";
	var _proteinChangeLinkClicked = "mutationTableProteinChangeLinkClicked";
	var _pdbLinkClicked = "mutationTablePdbLinkClicked";
	var _pdbPanelResizeStarted = "mutationPdbPanelResizeStarted";
	var _pdbPanelResizeEnded = "mutationPdbPanelResizeEnded";
	var _panelChainSelected = "mutationPdbPanelChainSelected";
	var _tableChainSelected = "mutationPdbTableChainSelected";
	var _tableChainMouseout = "mutationPdbTableChainMouseout";
	var _tableChainMouseover = "mutationPdbTableChainMouseover";
	var _pdbTableReady = "mutationPdbTableReady";
	var _geneTabSelected = "mutationDetailsGeneTabSelected";
	var _geneTabsCreated = "mutationDetailsGeneTabsCreated";
	var _3dPanelClosed = "mutation3dPanelClosed";
	var _3dStructureReloaded = "mutation3dStructureReloaded";

	return {
		LOLLIPOP_SELECTED: _lollipopSelected,
		LOLLIPOP_DESELECTED: _lollipopDeselected,
		LOLLIPOP_MOUSEOVER: _lollipopMouseover,
		LOLLIPOP_MOUSEOUT: _lollipopMouseout,
		ALL_LOLLIPOPS_DESELECTED: _allLollipopsDeselected,
		DIAGRAM_PLOT_UPDATED: _diagramPlotUpdated,
		DIAGRAM_PLOT_RESET: _diagramPlotReset,
		MUTATION_TABLE_FILTERED: _mutationTableFiltered,
		PROTEIN_CHANGE_LINK_CLICKED: _proteinChangeLinkClicked,
		PDB_LINK_CLICKED: _pdbLinkClicked,
		PDB_PANEL_RESIZE_STARTED: _pdbPanelResizeStarted,
		PDB_PANEL_RESIZE_ENDED: _pdbPanelResizeEnded,
		PANEL_CHAIN_SELECTED: _panelChainSelected,
		TABLE_CHAIN_SELECTED: _tableChainSelected,
		TABLE_CHAIN_MOUSEOUT: _tableChainMouseout,
		TABLE_CHAIN_MOUSEOVER: _tableChainMouseover,
		PDB_TABLE_READY: _pdbTableReady,
		GENE_TAB_SELECTED: _geneTabSelected,
		GENE_TABS_CREATED: _geneTabsCreated,
		VIEW_3D_STRUCTURE_RELOADED: _3dStructureReloaded,
		VIEW_3D_PANEL_CLOSED: _3dPanelClosed
	};
})();
