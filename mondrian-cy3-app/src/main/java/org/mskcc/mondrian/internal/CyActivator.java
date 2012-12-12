package org.mskcc.mondrian.internal;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

/**
 * Cytoscape App Activator
 * 
 * @author Dazhi Jiao
 */
public class CyActivator extends AbstractCyActivator {
	@Override
	public void start(BundleContext bc) throws Exception {
		CySwingApplication desktop = getService(bc, CySwingApplication.class);
		CyNetworkManager networkManager = getService(bc, CyNetworkManager.class);
		DialogTaskManager taskManager = getService(bc, DialogTaskManager.class);
		CyApplicationManager manager = getService(bc, CyApplicationManager.class);
		CyTableFactory tableFactory = getService(bc, CyTableFactory.class);
		CyTableManager tableManager = getService(bc, CyTableManager.class);
		VisualMappingFunctionFactory continuousVmfFactory = getService(bc, VisualMappingFunctionFactory.class,
				"(mapping.type=continuous)");
		VisualMappingFunctionFactory discreteVmfFactory = getService(bc, VisualMappingFunctionFactory.class,
				"(mapping.type=discrete)");
		VisualMappingFunctionFactory passthroughVmfFactory = getService(bc, VisualMappingFunctionFactory.class,
				"(mapping.type=passthrough)");
		VisualMappingManager visualMappingManager = getService(bc, VisualMappingManager.class);
		VisualStyleFactory visualStyleFactory = getService(bc, VisualStyleFactory.class);
		CyNetworkTableManager networkTableManager = getService(bc, CyNetworkTableManager.class);

		// create our gui
		MondrianApp container = MondrianApp.getInstance(desktop, networkManager, taskManager, manager, tableFactory,
				tableManager, networkTableManager, continuousVmfFactory, discreteVmfFactory, passthroughVmfFactory,
				visualMappingManager, visualStyleFactory);

		// TODO: populate the properties
		registerService(bc, container.getDataTypesPane(), CytoPanelComponent.class, new Properties());
		registerService(bc, container.getControlPane(), CytoPanelComponent.class, new Properties());
		registerService(bc, container, CyAction.class, new Properties());
		registerService(bc, container.getHeatmapPane(), SetCurrentNetworkViewListener.class, new Properties());
		registerService(bc, container.getHeatmapPane(), NetworkAboutToBeDestroyedListener.class, new Properties());
	}

}
