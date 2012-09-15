// package
package org.mskcc.cbio.importer.model;

/**
 * Class which contains datatype metadata.
 */
public final class DatatypeMetadata {

	// bean properties
	private String datatype;
    private String stagingFileFactoryClassname;
    private String importerClassname;
	private String overrideFilename;
    private String dataFilename;
    private String metaFilename;
    private String metaStableID;
    private String metaGeneticAlterationType;
    private Boolean metaShowProfileInAnalysisTab;
    private String metaProfileName;

    /**
     * Create a DatatypeMetadata instance with specified properties.
     *
     * @param datatype String
     * @param stagingFileFactoryClassname String
     * @param importerClassname String
     * @param overrideFilename String
     * @param dataFilename String
     * @param metaFilename String
     * @param metaStableID String
     * @param metaGeneticAlterationType String
     * @param metaShowProfileInAnalyisTab Boolean
     * @param metaProfileName String
     */
    public DatatypeMetadata(final String datatype, final String stagingFileFactoryClassname,
                            final String importerClassname, final String overrideFilename,
                            final String dataFilename, final String metaFilename,
                            final String metaStableID, final String metaGeneticAlterationType,
                            final Boolean metaShowProfileInAnalysisTab, final String metaProfileName) {

		if (datatype == null) {
            throw new IllegalArgumentException("datatype must not be null");
		}
		this.datatype = datatype;

		if (stagingFileFactoryClassname == null) {
            throw new IllegalArgumentException("stagingFileFactoryClassname must not be null");
		}
		this.stagingFileFactoryClassname = stagingFileFactoryClassname;

		if (importerClassname == null) {
            throw new IllegalArgumentException("importerClassname must not be null");
		}
		this.importerClassname = importerClassname;

		if (overrideFilename == null) {
			this.overrideFilename = "";
		}
		else {
			this.overrideFilename = overrideFilename;
		}

		if (dataFilename == null) {
            throw new IllegalArgumentException("dataFilename must not be null");
		}
		this.dataFilename = dataFilename;

		if (metaFilename == null) {
            throw new IllegalArgumentException("metaFilename must not be null");
		}
		this.metaFilename = metaFilename;

		if (metaStableID == null) {
            throw new IllegalArgumentException("metaStableID must not be null");
		}
		this.metaStableID = metaStableID;

		if (metaGeneticAlterationType == null) {
            throw new IllegalArgumentException("metaGeneticAlterationType must not be null");
		}
		this.metaGeneticAlterationType = metaGeneticAlterationType;

		if (metaShowProfileInAnalysisTab == null) {
            throw new IllegalArgumentException("metaShowProfileInAnalysisTab must not be null");
		}
		this.metaShowProfileInAnalysisTab = metaShowProfileInAnalysisTab;

		if (metaProfileName == null) {
            throw new IllegalArgumentException("metaProfileName must not be null");
		}
		this.metaProfileName = metaProfileName;
	}

	public String getDatatype() { return datatype; }
	public String getStagingFileFactoryClassname() { return stagingFileFactoryClassname; }
	public String getImporterClassname() { return importerClassname; }
	public String getOverrideFilename() { return overrideFilename; }
	public String getDataFilename() { return dataFilename; }
	public String getMetaFilename() { return metaFilename; }
	public String getMetaStableID() { return metaStableID; }
	public String getMetaGeneticAlterationType() { return metaGeneticAlterationType; }
	public Boolean getMetaShowProfileInAnalysisTab() { return metaShowProfileInAnalysisTab; }
	public String getMetaProfileName() { return metaProfileName; }
}
