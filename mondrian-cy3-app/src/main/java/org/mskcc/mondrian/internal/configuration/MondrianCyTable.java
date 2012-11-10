package org.mskcc.mondrian.internal.configuration;

import org.cytoscape.model.CyTable;
import org.mskcc.mondrian.client.CancerStudy;
import org.mskcc.mondrian.client.CaseList;
import org.mskcc.mondrian.client.GeneticProfile;

public class MondrianCyTable {
	private CancerStudy study;
	private GeneticProfile profile;
	private CaseList caseList;
	private CyTable table;
	
	public MondrianCyTable(CancerStudy study, GeneticProfile profile,
			CaseList caseList, CyTable table) {
		super();
		this.study = study;
		this.profile = profile;
		this.caseList = caseList;
		this.table = table;
	}
	public CancerStudy getStudy() {
		return study;
	}
	public void setStudy(CancerStudy study) {
		this.study = study;
	}
	public GeneticProfile getProfile() {
		return profile;
	}
	public void setProfile(GeneticProfile profile) {
		this.profile = profile;
	}
	public CaseList getCaseList() {
		return caseList;
	}
	public void setCaseList(CaseList caseList) {
		this.caseList = caseList;
	}
	public CyTable getTable() {
		return table;
	}
	public void setTable(CyTable table) {
		this.table = table;
	}
	
}
