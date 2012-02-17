package org.mskcc.cgds.model;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.cgds.dao.DaoMutation;
import org.mskcc.cgds.util.EqualsUtil;
import org.mskcc.portal.remote.GetGeneticProfiles;

import java.util.ArrayList;

/**
 * This class encapsulates cancer study stats displayed on the Data Sets Page.
 *
 * @author Benjamin Gross
 */
public class CancerStudyStats {

	private String stableID;
	private String studyName;
	private Integer all;
	private Integer sequenced;
	private Integer aCGH;
	private Integer rnaSEQ;
	private Integer tumorMRNA;
	private Integer normal;
	private Integer tumorMIRNA;
	private Integer methylation;
	private Integer rppa;
	private Integer complete;

	/**
	 * Constructor.
	 *
	 */
	public CancerStudyStats(String stableID, String studyName, Integer all, Integer sequenced,
							Integer aCGH, Integer rnaSEQ, Integer tumorMRNA, Integer normal,
							Integer tumorMIRNA, Integer methylation, Integer rppa, Integer complete) {

		this.stableID = stableID;
		this.studyName = studyName;
		this.all = all;
		this.sequenced = sequenced;
		this.aCGH = aCGH;
		this.rnaSEQ = rnaSEQ;
		this.tumorMRNA = tumorMRNA;
		this.normal = normal;
		this.tumorMIRNA = tumorMIRNA;
		this.methylation = methylation;
		this.rppa = rppa;
		this.complete = complete;
	}

	// accessors
	public String getStableID() { return this.stableID; }
	public String getStudyName() { return this.studyName; }
	public Integer getAll() { return this.all; }
	public Integer getSequenced() { return this.sequenced; }
	public Integer getACGH() { return this.aCGH; }
	public Integer getRNASEQ() { return this.rnaSEQ; }
	public Integer getTumorMRNA() { return this.tumorMRNA; }
	public Integer getNormal() { return this.normal; }
	public Integer getTumorMIRNA() { return this.tumorMIRNA; }
	public Integer getMethylation() { return this.methylation; }
	public Integer getRPPA() { return this.rppa; }
	public Integer getComplete() { return this.complete; }
}