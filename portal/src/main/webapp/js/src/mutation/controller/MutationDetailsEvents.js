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
	var _pdbLinkClicked = "mutationTablePdbLinkClicked";
	var _chainSelected = "pdbPanelChainSelected";
	var _geneTabSelected = "mutationDetailsGeneTabSelected";
	var _geneTabsCreated = "mutationDetailsGeneTabsCreated";
	var _3dPanelClosed = "mutation3dPanelClosed";

	return {
		LOLLIPOP_SELECTED: _lollipopSelected,
		LOLLIPOP_DESELECTED: _lollipopDeselected,
		LOLLIPOP_MOUSEOVER: _lollipopMouseover,
		LOLLIPOP_MOUSEOUT: _lollipopMouseout,
		ALL_LOLLIPOPS_DESELECTED: _allLollipopsDeselected,
		DIAGRAM_PLOT_UPDATED: _diagramPlotUpdated,
		DIAGRAM_PLOT_RESET: _diagramPlotReset,
		MUTATION_TABLE_FILTERED: _mutationTableFiltered,
		PDB_LINK_CLICKED: _pdbLinkClicked,
		CHAIN_SELECTED: _chainSelected,
		GENE_TAB_SELECTED: _geneTabSelected,
		GENE_TABS_CREATED: _geneTabsCreated,
		VIEW_3D_PANEL_CLOSED: _3dPanelClosed
	};
})();
