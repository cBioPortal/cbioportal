package org.cbioportal.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.cbioportal.model.Patient;
import org.cbioportal.web.parameter.PagingConstants;
import org.cbioportal.web.parameter.PatientIdentifier;
import org.cbioportal.web.parameter.Projection;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api(tags = "Patients", description = " ")
public class PatientController {

    @RequestMapping(value = "/studies/{studyId}/patients", method = RequestMethod.GET)
    @ApiOperation("Get all patients in a study")
    public ResponseEntity<List<Patient>> getAllPatientsInStudy(@PathVariable String studyId,
                                                                                @RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber) {

        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/studies/{studyId}/patients/{patientId}", method = RequestMethod.GET)
    @ApiOperation("Get a patient in a study")
    public ResponseEntity<Patient> getPatientInStudy(@PathVariable String studyId,
                                                            @PathVariable String patientId) {

        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/patients/fetch", method = RequestMethod.POST)
    @ApiOperation("Fetch patients by ID")
    public ResponseEntity<List<Patient>> fetchPatients(@RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                        @RequestBody List<PatientIdentifier> patientIdentifiers) {

        throw new UnsupportedOperationException();
    }

    @RequestMapping(value = "/patients/query", method = RequestMethod.POST)
    @ApiOperation("Query patients by example")
    public ResponseEntity<List<Patient>> queryPatientsByExample(@RequestParam(defaultValue = "SUMMARY") Projection projection,
                                                                                 @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_SIZE) Integer pageSize,
                                                                                 @RequestParam(defaultValue = PagingConstants.DEFAULT_PAGE_NUMBER) Integer pageNumber,
                                                                                 @RequestBody Patient examplePatient) {

        throw new UnsupportedOperationException();
    }
}
