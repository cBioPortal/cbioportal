package org.cbioportal.url_shortener;

import fr.plaisance.bitly.Bit;
import fr.plaisance.bitly.Bitly;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// TODO Consider creating separate DispatcherServlets as in the original web.xml
// See: https://stackoverflow.com/a/30686733/11651683
@RestController
public class URLShortenerController {

    @Value("${bitly.access.token}")
    private String bitlyAccessToken;

    private Bitly bitly ;
    private UrlValidator urlValidator = new UrlValidator();

    @RequestMapping(path = "/api/url-shortener", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<URLShortenerResponse> urlShortener(@RequestParam String url) {

        if (urlValidator.isValid(url)) {
            if (bitly == null) {
                bitly = Bit.ly(bitlyAccessToken);
            }
            return new ResponseEntity<>(new URLShortenerResponse(bitly.shorten(url), null), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new URLShortenerResponse(null, "Invalid URL"), HttpStatus.BAD_REQUEST);
        }
    }
}
