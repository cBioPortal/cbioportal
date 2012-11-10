package org.mskcc.mondrian.internal;

import java.awt.event.ActionEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.mskcc.mondrian.internal.configuration.ConfigurationChangedEvent;
import org.mskcc.mondrian.internal.configuration.MondrianConfiguration;
import org.mskcc.mondrian.internal.configuration.MondrianConfigurationListener;
import org.mskcc.mondrian.internal.gui.DataTypesPanel;
import org.mskcc.mondrian.internal.gui.heatmap.HeatmapPanel;

/**
 * The Mondrian App
 *
 * @author Dazhi Jiao
 */
public class MondrianApp extends AbstractCyAction implements MondrianConfigurationListener {
	private static final long serialVersionUID = 1935118515626512995L;
	private CySwingApplication desktopApp;
	private CyNetworkManager networkManager;
	private DialogTaskManager taskManager;
	private CyApplicationManager appManager;
	private CyTableFactory tableFactory;
	private CyTableManager tableManager;
	private CyNetworkTableManager networkTableMangager;
	private MondrianConfiguration mondrianConfiguration;
	private HeatmapPanel heatmapPane;
	private DataTypesPanel dataTypesPane;
	private VisualMappingFunctionFactory continuousVmfFactory;
	private VisualMappingFunctionFactory discreteVmfFactory;
	private VisualMappingFunctionFactory passthroughVmfFactory;
	private VisualMappingManager visualMappingManager;
	private VisualStyleFactory visualStyleFactory;
	
	protected static MondrianApp instance;
	
	public static MondrianApp getInstance() {
		return instance;
	}
	
	public static MondrianApp getInstance(CySwingApplication desktopApp, CyNetworkManager networkManager, DialogTaskManager taskManager, 
			CyApplicationManager appManager, CyTableFactory tableFactory, CyTableManager tableManager,
			CyNetworkTableManager networkTableManager, VisualMappingFunctionFactory continuousVmfFactory, 
			VisualMappingFunctionFactory discreteVmfFactory, VisualMappingFunctionFactory passthroughVmfFactory,
			VisualMappingManager visualMappingManager, VisualStyleFactory visualStyleFactory) {
		if (instance == null) {
			instance = new MondrianApp(desktopApp, networkManager, taskManager, appManager, tableFactory, tableManager, networkTableManager, 
					continuousVmfFactory, discreteVmfFactory, passthroughVmfFactory, visualMappingManager, visualStyleFactory);
		}
		return instance;
	}

	private MondrianApp(CySwingApplication desktopApp, CyNetworkManager networkManager, DialogTaskManager taskManager, CyApplicationManager appManager, 
			CyTableFactory tableFactory, CyTableManager tableManager, CyNetworkTableManager networkTableManager, VisualMappingFunctionFactory continuousVmfFactory, 
			VisualMappingFunctionFactory discreteVmfFactory, VisualMappingFunctionFactory passthroughVmfFactory, 
			VisualMappingManager visualMappingManager, VisualStyleFactory visualStyleFactory) {
		super("Mondrian");
		setPreferredMenu("Apps");
		this.desktopApp = desktopApp;
		this.networkManager = networkManager;
		this.taskManager = taskManager;
		this.appManager = appManager;
		this.tableFactory = tableFactory;
		this.tableManager = tableManager;
		this.networkTableMangager = networkTableManager;
		this.continuousVmfFactory = continuousVmfFactory;
		this.discreteVmfFactory = discreteVmfFactory;
		this.passthroughVmfFactory = passthroughVmfFactory;
		this.visualMappingManager = visualMappingManager;
		this.visualStyleFactory = visualStyleFactory;
		
		mondrianConfiguration = new MondrianConfiguration();

		this.heatmapPane = new HeatmapPanel();
		this.dataTypesPane = new DataTypesPanel();
		
		mondrianConfiguration.addConfigurationListener(heatmapPane);
		

		/*
		CyNetwork network = appManager.getCurrentNetwork();
		CyTable table = network.getDefaultNetworkTable();
		Collection<CyColumn> cols = table.getColumns();
		for (CyColumn cyColumn : cols) {
			System.out.println(cyColumn.getName());
		}
		*/
	}

	public void actionPerformed(ActionEvent e) {
		CytoPanel cytoPanelWest = this.desktopApp.getCytoPanel(CytoPanelName.EAST);
		// If the state of the cytoPanelWest is HIDE, show it
		if (cytoPanelWest.getState() == CytoPanelState.HIDE) {
			cytoPanelWest.setState(CytoPanelState.DOCK);
		}	

		// Select my panel
		int index = cytoPanelWest.indexOfComponent(dataTypesPane);
		if (index == -1) {
			return;
		}
		cytoPanelWest.setSelectedIndex(index);		
	}

	@Override
	public void configurationChanged(ConfigurationChangedEvent evt) {
		// TODO Auto-generated method stub
		
	}
	
	public HeatmapPanel getControlPane() {
		return heatmapPane;
	}

	public void setControlPane(HeatmapPanel controlPane) {
		this.heatmapPane = controlPane;
	}

	public DataTypesPanel getDataTypesPane() {
		return dataTypesPane;
	}

	public void setDataTypesPane(DataTypesPanel dataTypesPane) {
		this.dataTypesPane = dataTypesPane;
	}

	public DialogTaskManager getTaskManager() {
		return taskManager;
	}

	public void setTaskManager(DialogTaskManager taskManager) {
		this.taskManager = taskManager;
	}

	public CySwingApplication getDesktopApp() {
		return desktopApp;
	}

	public void setDesktopApp(CySwingApplication desktopApp) {
		this.desktopApp = desktopApp;
	}

	public MondrianConfiguration getMondrianConfiguration() {
		return mondrianConfiguration;
	}

	public void setMondrianConfiguration(MondrianConfiguration mondrianConfiguration) {
		this.mondrianConfiguration = mondrianConfiguration;
	}

	public CyApplicationManager getAppManager() {
		return appManager;
	}

	public void setAppManager(CyApplicationManager appManager) {
		this.appManager = appManager;
	}

	public CyTableFactory getTableFactory() {
		return tableFactory;
	}

	public void setTableFactory(CyTableFactory tableFactory) {
		this.tableFactory = tableFactory;
	}

	public CyTableManager getTableManager() {
		return tableManager;
	}

	public void setTableManager(CyTableManager tableManager) {
		this.tableManager = tableManager;
	}

	public CyNetworkTableManager getNetworkTableMangager() {
		return networkTableMangager;
	}

	public void setNetworkTableMangager(CyNetworkTableManager networkTableMangager) {
		this.networkTableMangager = networkTableMangager;
	}
	
	public void addConfigurationListener(MondrianConfigurationListener listener) {
		this.mondrianConfiguration.addConfigurationListener(listener);
	}

	public CyNetworkManager getNetworkManager() {
		return networkManager;
	}

	public void setNetworkManager(CyNetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	public HeatmapPanel getHeatmapPane() {
		return heatmapPane;
	}

	public void setHeatmapPane(HeatmapPanel heatmapPane) {
		this.heatmapPane = heatmapPane;
	}

	public VisualMappingFunctionFactory getContinuousVmfFactory() {
		return continuousVmfFactory;
	}

	public void setContinuousVmfFactory(
			VisualMappingFunctionFactory continuousVmfFactory) {
		this.continuousVmfFactory = continuousVmfFactory;
	}

	public VisualMappingFunctionFactory getDiscreteVmfFactory() {
		return discreteVmfFactory;
	}

	public void setDiscreteVmfFactory(
			VisualMappingFunctionFactory discreteVmfFactory) {
		this.discreteVmfFactory = discreteVmfFactory;
	}

	public VisualMappingFunctionFactory getPassthroughVmfFactory() {
		return passthroughVmfFactory;
	}

	public void setPassthroughVmfFactory(
			VisualMappingFunctionFactory passthroughVmfFactory) {
		this.passthroughVmfFactory = passthroughVmfFactory;
	}

	public VisualMappingManager getVisualMappingManager() {
		return visualMappingManager;
	}

	public void setVisualMappingManager(VisualMappingManager visualMappingManager) {
		this.visualMappingManager = visualMappingManager;
	}

	public VisualStyleFactory getVisualStyleFactory() {
		return visualStyleFactory;
	}

	public void setVisualStyleFactory(VisualStyleFactory visualStyleFactory) {
		this.visualStyleFactory = visualStyleFactory;
	}

}
