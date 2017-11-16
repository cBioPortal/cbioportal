/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


/*
 * This class is designed to control the logic for Clinial Tab in Study View
 *
 * @autor Hongxin Zhang
 *
 */


var StudyViewClinicalTabController = (function() {
    function init(callback) {
        $.when(
            window.iviz.datamanager.getClinicalAttributesByStudy(),
            window.iviz.datamanager.getPatientClinicalData(),
            window.iviz.datamanager.getSampleClinicalData(),
            window.iviz.datamanager.getStudyToSampleToPatientdMap())
            .done(function(ca, pd, sd, map) {
                var attr = _.extend(ca);
                var arr = _.extend(pd, sd);
                var data = [];

                attr['CASE_ID'] = {
                    attr_id: 'CASE_ID',
                    datatype: 'STRING',
                    description: 'Sample ID',
                    display_name: 'Sample ID'
                };

                attr['PATIENT_ID'] = {
                    attr_id: 'PATIENT_ID',
                    datatype: 'STRING',
                    description: 'Patient ID',
                    display_name: 'Patient ID'
                };

                attr.study_id = {
                    datatype: 'STRING',
                    description: '',
                    display_name: 'Cancer Studies',
                    attr_id: 'study_id'
                };

                _.each(arr, function(datum) {
                    _.each(datum, function(item) {
                        if (item.attr_id !== 'CASE_ID') {
                            if (item.hasOwnProperty('patient_id')) {
                                var _studyId = item.study_id;
                                if (map.hasOwnProperty(_studyId)) {
                                    var patientToSample = map[_studyId].patient_to_sample;
                                    _.each(patientToSample[item.patient_id],
                                        function(sample_id) {
                                            data.push({
                                                attr_id: item.attr_id,
                                                attr_val: item.attr_val,
                                                CASE_ID: sample_id
                                            });
                                        });
                                } else {
                                    // TODO: how to handle case without study id?
                                }
                            } else {
                                data.push({
                                    attr_id: item.attr_id,
                                    attr_val: item.attr_val,
                                    CASE_ID: item.sample_id
                                });
                            }
                        }
                    });
                });

                _.each(map, function(mapping, studyId) {
                    _.each(mapping.sample_to_patient, function(patientId, sampleId) {
                        if (sampleId !== 'Composite.Element.Ref') {
                            data.push({
                                attr_id: 'study_id',
                                attr_val: studyId,
                                CASE_ID: sampleId
                            });
                            data.push({
                                attr_id: 'PATIENT_ID',
                                attr_val: patientId,
                                CASE_ID: sampleId
                            });
                        }
                    });
                });

                _.each(attr, function(item) {
                    if (item.attr_id === 'CASE_ID' ||
                        item.attr_id === 'PATIENT_ID') {
                        item.fixed = true;
                        if (StudyViewParams.params.studyId === 'mskimpact') {
                            if (item.attr_id === 'CASE_ID') {
                                item.column_width = 200;
                            }
                            if (item.attr_id === 'PATIENT_ID') {
                                item.column_width = 160;
                            }
                        }
                    }
                });

                initTable(_.values(attr), data);
            })
            .fail(function() {
                console.log('Failed');
            })
            .always(function() {
                if (_.isFunction(callback)) {
                    callback();
                }
            })
    }

    function initTable(attr, arr) {
        var table = React.createElement(EnhancedFixedDataTable, {
            input: {
                data: arr,
                attributes: attr
            },
            filter: "ALL",
            download: "ALL",
            downloadFileName: StudyViewParams.params.studyId +
            '_clinical_data.tsv',
            showHide: true,
            hideFilter: true,
            scroller: true,
            resultInfo: true,
            groupHeader: true,
            fixedChoose: true,
            uniqueId: "CASE_ID",
            rowHeight: 30,
            tableWidth: 1200,
            maxHeight: 500,
            headerHeight: 30,
            groupHeaderHeight: 40,
            autoColumnWidth: false,
            columnMaxWidth: 300,
            columnSorting: true,
            isResizable: true
        });

        ReactDOM.render(table,
            document.getElementById('clinical-data-table-div'));
    }

    return {
        init: init
    };
})();
