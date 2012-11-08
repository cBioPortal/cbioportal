package org.mskcc.mondrian.internal.configuration;

/**
 * Class which encapsulates changes the current configuration.
 * 
 * @author Benjamin Gross
 * @author Dazhi Jiao
 */
public class ConfigurationChangedEvent extends java.util.EventObject {
	private static final long serialVersionUID = 5674382874417010725L;
	
	public enum Type {
		RANGE_CHANGED,
		NETWORK_CHANGED,
		COLOR_THEME_CHANGED,
		DATA_TYPE_CHANGED,
		HEATMAP_PANEL_CONFIG_CHANGED,
		CLINICAL_DATA_CHANGED,
		APPLY_VIZ_STYLE,
		CBIO_DATA_IMPORTED
	}
	
	private Type type;
	
	public ConfigurationChangedEvent(Object source, Type type) {
		super(source);
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
}
