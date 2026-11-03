/*
 * Copyright (c) 2016 The Hyve B.V.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.mskcc.cbio.portal.scripts;

import java.util.*;

@SuppressWarnings("serial")
/**
 * This class overrides getProperties method to return the trimmed 
 * value of the property. 
 * 
 * @author pieterlukasse
 *
 */
public class TrimmedProperties extends Properties {

	@Override
	public String getProperty(String key) {
		if (super.getProperty(key) == null)
			return null;
		else
			return super.getProperty(key).trim();
	}

}
