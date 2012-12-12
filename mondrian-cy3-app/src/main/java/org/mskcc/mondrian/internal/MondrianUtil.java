package org.mskcc.mondrian.internal;

import org.cytoscape.model.CyNetwork;

public class MondrianUtil {
	public static CyNetwork getCurrentNetwork() {
		return MondrianApp.getInstance().getAppManager().getCurrentNetwork();
	}
}
