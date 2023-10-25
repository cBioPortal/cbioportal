package org.cbioportal.model;

import java.io.Serializable;
import jakarta.validation.constraints.NotNull;

public class Info implements Serializable {
    
    @NotNull
    private String portalVersion;
    @NotNull
    private String dbVersion;
    @NotNull
    private String gitBranch;
    @NotNull
    private String gitCommitId;
    @NotNull
    private String gitCommitIdAbbrev;
    @NotNull
    private String gitCommitIdDescribe;
    @NotNull
    private String gitCommitIdDescribeShort;
    @NotNull
    private String gitCommitMessageFull;
    @NotNull
    private String gitCommitMessageShort;
    @NotNull
    private String gitCommitMessageUserEmail;
    @NotNull
    private String gitCommitMessageUserName;
    @NotNull
    private Boolean gitDirty;

    public String getGitBranch() {
        return this.gitBranch;
    }

    public void setGitBranch(String gitBranch) {
        this.gitBranch = gitBranch;
    }

    public String getGitCommitId() {
        return this.gitCommitId;
    }

    public void setGitCommitId(String gitCommitId) {
        this.gitCommitId = gitCommitId;
    }

    public String getGitCommitIdAbbrev() {
        return this.gitCommitIdAbbrev;
    }

    public void setGitCommitIdAbbrev(String gitCommitIdAbbrev) {
        this.gitCommitIdAbbrev = gitCommitIdAbbrev;
    }

    public String getGitCommitIdDescribe() {
        return this.gitCommitIdDescribe;
    }

    public void setGitCommitIdDescribe(String gitCommitIdDescribe) {
        this.gitCommitIdDescribe = gitCommitIdDescribe;
    }

    public String getGitCommitIdDescribeShort() {
        return this.gitCommitIdDescribeShort;
    }

    public void setGitCommitIdDescribeShort(String gitCommitIdDescribeShort) {
        this.gitCommitIdDescribeShort = gitCommitIdDescribeShort;
    }

    public String getGitCommitMessageFull() {
        return this.gitCommitMessageFull;
    }

    public void setGitCommitMessageFull(String gitCommitMessageFull) {
        this.gitCommitMessageFull = gitCommitMessageFull;
    }

    public String getGitCommitMessageShort() {
        return this.gitCommitMessageShort;
    }

    public void setGitCommitMessageShort(String gitCommitMessageShort) {
        this.gitCommitMessageShort = gitCommitMessageShort;
    }

    public String getGitCommitMessageUserEmail() {
        return this.gitCommitMessageUserEmail;
    }

    public void setGitCommitMessageUserEmail(String gitCommitMessageUserEmail) {
        this.gitCommitMessageUserEmail = gitCommitMessageUserEmail;
    }

    public String getGitCommitMessageUserName() {
        return this.gitCommitMessageUserName;
    }

    public void setGitCommitMessageUserName(String gitCommitMessageUserName) {
        this.gitCommitMessageUserName = gitCommitMessageUserName;
    }

    public Boolean getGitDirty() {
        return this.gitDirty;
    }

    public void isGitDirty(Boolean gitDirty) {
        this.gitDirty = gitDirty;
    }


    public String getPortalVersion() {
        return portalVersion;
    }

    public void setPortalVersion(String portalVersion) {
        this.portalVersion = portalVersion;
    }

    public String getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(String dbVersion) {
        this.dbVersion = dbVersion;
    }
}
