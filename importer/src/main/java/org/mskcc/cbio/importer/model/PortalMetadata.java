/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

// package
package org.mskcc.cbio.importer.model;

// imports
import org.mskcc.cbio.importer.util.MetadataUtils;

import java.net.URL;

/**
 * Class which contains portal metadata.
 */
public class PortalMetadata {

	// all cases indicator
	public static final String PUBLIC_PORTAL = "public-portal";

	// bean properties
    private String name;
    private String stagingDirectory;
    private String overrideDirectory;
	private URL igvSegFileLinkingLocation;

    /**
     * Create a PortalMetadata instance with properties in given array.
	 * Its assumed order of properties is that from google worksheet.
     *
	 * @param properties String[]
     */
    public PortalMetadata(String[] properties) {

		if (properties.length < 4) {
            throw new IllegalArgumentException("corrupt properties array passed to contructor");
		}

        this.name = properties[0].trim();
		this.stagingDirectory = MetadataUtils.getCanonicalPath(properties[1].trim());
		this.overrideDirectory = MetadataUtils.getCanonicalPath(properties[2].trim());
		try {
			this.igvSegFileLinkingLocation = new URL(properties[3].trim());
		}
		catch (Exception e) {
			throw new IllegalArgumentException("corrupt properties array passed to constructor");
		}
	}

	public String getName() { return name; }
	public String getStagingDirectory() { return stagingDirectory; }
	public String getOverrideDirectory() { return overrideDirectory; }
	public URL getIGVSegFileLinkingLocation() { return igvSegFileLinkingLocation; }
}
