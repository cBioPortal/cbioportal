package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class DataAccessTokenMixin {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date expiration;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date creation;
}
