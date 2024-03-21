package org.cbioportal.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.cbioportal.model.Info;
import org.cbioportal.web.config.PublicApiTags;
import org.cbioportal.web.config.annotation.PublicApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@PublicApi
@RestController
@Validated
@Tag(name = PublicApiTags.INFO, description = " ")
public class InfoController {

    @Value("${portal.version}")
    private String portalVersion;

    @Value("${db.version}")
    private String dbVersion;

    @Value("${git.branch:not set}")
    private String gitBranch;

    @Value("${git.commit.id.full:not set}")
    private String gitCommitId;

    @Value("${git.commit.id.abbrev:not set}")
    private String gitCommitIdAbbrev;

    @Value("${git.commit.id.describe:not set}")
    private String gitCommitIdDescribe;

    @Value("${git.commit.id.describe-short:not set}")
    private String gitCommitIdDescribeShort;

    @Value("${git.commit.message.full:not set}")
    private String gitCommitMessageFull;

    @Value("${git.commit.message.short:not set}")
    private String gitCommitMessageShort;

    @Value("${git.commit.user.email:not set}")
    private String gitCommitMessageUserEmail;

    @Value("${git.commit.user.name:not set}")
    private String gitCommitMessageUserName;

    @Value("${git.dirty:not set}")
    private String gitDirty;

    @RequestMapping(value = "/api/info", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get information about the running instance")
    @ApiResponse(responseCode = "200", description = "OK",
        content = @Content(schema = @Schema(implementation = Info.class)))
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
