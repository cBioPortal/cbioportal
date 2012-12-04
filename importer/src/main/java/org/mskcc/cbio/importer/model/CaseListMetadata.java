/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

// package
package org.mskcc.cbio.importer.model;

// imports
import java.util.Set;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;

/**
 * Class which contains caselist metadata.
 */
public final class CaseListMetadata {

	// if either delimiter changes, update ConverterImpl

	// delimiter between download archive pairs
	public static final String CASE_LIST_UNION_DELIMITER = "|";
	// delimiter between download archive pairs
	public static final String CASE_LIST_INTERSECTION_DELIMITER = "&";

	// bean properties
	private String caseListFilename;
    private String stagingFilenames;
    private String metaStableID;
    private String metaCaseListCategory;
    private String metaCancerStudyIdentifier;
    private String metaCaseListName;
	private String metaCaseListDescription;

    /**
     * Create a CaseListMetadata instance with specified properties.
     *
	 * @param caseListFilename String
     * @param stagingFilenames String
     * @param metaStableID String
	 * @param metaCaseListCategory String
	 * @param metaCancerStudyIdentifier String
	 * @param metaCaseListName String
     * @param metaCaseListDescription String
     */
    public CaseListMetadata(final String caseListFilename, final String stagingFilenames,
							final String metaStableID, final String metaCaseListCategory,
							final String metaCancerStudyIdentifier, final String metaCaseListName,
							final String metaCaseListDescription) {

		if (caseListFilename == null) {
            throw new IllegalArgumentException("caseListFilename must not be null");
		}
		this.caseListFilename = caseListFilename;

		if (stagingFilenames == null) {
            throw new IllegalArgumentException("stagingFilenames must not be null");
		}
		this.stagingFilenames = stagingFilenames;

		if (metaStableID == null) {
            throw new IllegalArgumentException("metaStableID must not be null");
		}
		this.metaStableID = metaStableID;

		if (metaCaseListCategory == null) {
            throw new IllegalArgumentException("metaCaseListCategory must not be null");
		}
		this.metaCaseListCategory = metaCaseListCategory;

		if (metaCancerStudyIdentifier == null) {
            throw new IllegalArgumentException("metaCancerStudyIdentifier must not be null");
		}
		this.metaCancerStudyIdentifier = metaCancerStudyIdentifier;

		if (metaCaseListName == null) {
            throw new IllegalArgumentException("metaCaseListName must not be null");
		}
		this.metaCaseListName = metaCaseListName;

		if (metaCaseListDescription == null) {
            throw new IllegalArgumentException("metaCaseListDescription must not be null");
		}
		this.metaCaseListDescription = metaCaseListDescription;
	}

	public String getCaseListFilename() { return caseListFilename; }
	public String getStagingFilenames() { return stagingFilenames; }
	public String getMetaStableID() { return metaStableID; }
	public String getMetaCaseListCategory() { return metaCaseListCategory; }
	public String getMetaCancerStudyIdentifier() { return metaCancerStudyIdentifier; }
	public String getMetaCaseListName() { return metaCaseListName; }
	public String getMetaCaseListDescription() { return metaCaseListDescription; }
}
