package org.mskcc.mondrian.internal.configuration;

import java.util.EventListener;

/**
 * Interface implemented by class(es) interested in data mapper configuration changes.
 */
public interface MondrianConfigurationListener extends EventListener {
	/**
	 * Method is called whenever the configuration changes
	 *
	 * @param evt ConfigurationChangedEvent
	 */
	public void configurationChanged(ConfigurationChangedEvent evt);
}
