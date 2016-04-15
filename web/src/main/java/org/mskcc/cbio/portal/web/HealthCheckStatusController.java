package org.mskcc.cbio.portal.web;

import org.mskcc.cbio.portal.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static java.util.Arrays.asList;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * This class verify a health check status. If Database is UP the whole app is UP too.
 */
@Controller
@RequestMapping("/health")
public class HealthCheckStatusController {

    @Autowired
    private ApiService apiService;

    @RequestMapping(method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(OK)
    @ResponseBody
    public HealthCheckStatus health() {

        boolean dbStatus = checkDBStatus();

        HealthCheckStatus response;
        if(dbStatus) {
            response = new HealthCheckStatus(HealthCheckStatus.Status.UP, HealthCheckStatus.Status.UP);
        } else {
            response = new HealthCheckStatus(HealthCheckStatus.Status.DOWN, HealthCheckStatus.Status.DOWN);
        }
        return response;
    }

    private boolean checkDBStatus() {
        try {
            apiService.getStudies(asList("-1"));
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }
}
