package org.cbioportal.domain.alteration;

public class ProfiledCountByStudy {
    private String studyId;
    private int profiledCount;

    public String getStudyId() {
        return studyId;
    }

    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public int getProfiledCount() {
        return profiledCount;
    }

    public void setProfiledCount(int profiledCount) {
        this.profiledCount = profiledCount;
    }
}
