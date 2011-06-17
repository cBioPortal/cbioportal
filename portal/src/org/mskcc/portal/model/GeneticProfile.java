package org.mskcc.portal.model;

/**
 * Encapsulates Genetic Profile Information.
 * Metadata about the process for collecting the genetic information.
 */
public class GeneticProfile {
    private String id;
    private String name;
    private String description;
    private GeneticAlterationType alterationType;
    private boolean showProfileInAnalysisTab;

    /**
     * Constructor.
     *
     * @param id                Genetic Profile ID.
     * @param name              Genetic Profile Name.
     * @param description       Genetic Profile Description.
     * @param alterationType    Genetic Alteration Type.
     * @param showInAnalysisTab Show Profile In Analysis Tab.
     */
    public GeneticProfile(String id, String name, String description,
                          GeneticAlterationType alterationType, boolean showInAnalysisTab) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.alterationType = alterationType;
        this.showProfileInAnalysisTab = showInAnalysisTab;
    }

    /**
     * Constructor.
     *
     * @param id                Genetic Profile ID.
     * @param name              Genetic Profile Name.
     * @param description       Genetic Profile Description.
     * @param alterationType    Genetic Alteration Type.
     * @param showInAnalysisTab Show Profile In Analysis Tab.
     */
    public GeneticProfile(String id, String name, String description, String alterationType,
                          boolean showInAnalysisTab) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.alterationType = GeneticAlterationType.getType(alterationType);
        this.showProfileInAnalysisTab = showInAnalysisTab;
    }

    /**
     * Gets the Genetic Profile ID.
     *
     * @return Genetic Profile ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the Genetic Profile Name.
     *
     * @return Genetic Profile Name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the Genetic Profile Description.
     *
     * @return Genetic Profile Description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the Genetic Alteration Type.
     *
     * @return GeneticAlterationType Object.
     */
    public GeneticAlterationType getAlterationType() {
        return alterationType;
    }

    /**
     * Show this profile in the analysis tab?
     *
     * @return true or false.
     */
    public boolean showProfileInAnalysisTab() {
        return showProfileInAnalysisTab;
    }

    /**
     * Should we should this profile in the analysis tab?
     *
     * @param showProfileInAnalysisTab true or false.
     */
    public void setShowProfileInAnalysisTab(boolean showProfileInAnalysisTab) {
        this.showProfileInAnalysisTab = showProfileInAnalysisTab;
    }
}
