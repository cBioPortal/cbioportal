package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.cbioportal.model.Info;
import org.cbioportal.web.config.annotation.InternalApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@InternalApi
@RestController
@Validated
@Api(tags = "Info", description = " ")
public class InfoController {

    @Value("${portal.version}")
    private String portalVersion;
    
    @Value("${db.version}")
    private String dbVersion;

    @Value("${git.branch}")
    private String gitBranch;

    @Value("${git.commit.id}")
    private String gitCommitId;

    @Value("${git.commit.id.abbrev}")
    private String gitCommitIdAbbrev;

    @Value("${git.commit.id.describe}")
    private String gitCommitIdDescribe;

    @Value("${git.commit.id.describe-short}")
    private String gitCommitIdDescribeShort;

    @Value("${git.commit.message.full}")
    private String gitCommitMessageFull;

    @Value("${git.commit.message.short}")
    private String gitCommitMessageShort;

    @Value("${git.commit.user.email}")
    private String gitCommitMessageUserEmail;

    @Value("${git.commit.user.name}")
    private String gitCommitMessageUserName;

    @Value("${git.dirty}")
    private String gitDirty;

    @RequestMapping(value = "/info", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get information about the running instance")
    public ResponseEntity<Info> getInfo() {
        
        Info info = new Info();
        info.setPortalVersion(portalVersion);
        info.setDbVersion(dbVersion);
        info.setGitBranch(gitBranch);
        info.setGitCommitId(gitCommitId);
        info.setGitCommitIdDescribe(gitCommitIdDescribe);
        info.setGitCommitIdDescribeShort(gitCommitIdDescribeShort);
        info.setGitCommitMessageShort(gitCommitMessageShort);
        info.setGitCommitMessageFull(gitCommitMessageFull);
        info.setGitCommitMessageUserEmail(gitCommitMessageUserEmail);
        info.setGitCommitMessageUserName(gitCommitMessageUserName);
        info.isGitDirty(Boolean.valueOf(gitDirty));
        return new ResponseEntity<>(info, HttpStatus.OK);
    }
}
