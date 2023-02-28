package org.mskcc.cbio.portal.model.virtualstudy;

public class VirtualStudy {

	private String id;
	private String source;
	private String type;
	private VirtualStudyData data;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public VirtualStudyData getData() {
		return data;
	}

	public void setData(VirtualStudyData data) {
		this.data = data;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}


