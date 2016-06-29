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


var StudyViewClinicalTabController = (function () {
    function init() {
        var arr = StudyViewProxy.getArrData(), attr = $.extend(true, [], StudyViewProxy.getAttrData()), testElement, data = [];

        _.each(arr, function (datum) {
            for (var key in datum) {
                if(key !== 'CASE_ID') {
                    data.push({
                        attr_id: key,
                        attr_val: datum[key],
                        CASE_ID: datum.CASE_ID
                    });
                }
            }
        });
        
        _.each(attr, function(item) {
            if (item.attr_id === 'CASE_ID' || item.attr_id === 'PATIENT_ID') {
                item.fixed = true;
                if(StudyViewParams.params.studyId === 'mskimpact') {
                    if (item.attr_id === 'CASE_ID') {
                        item.column_width = 200;
                    }
                    if (item.attr_id === 'PATIENT_ID') {
                        item.column_width = 160;
                    }
                }
            }
        });
        
        testElement = React.createElement(EnhancedFixedDataTable, {
            input: {
                data: data,
                attributes: attr
            },
            filter: "ALL",
            download: "ALL",
            downloadFileName: StudyViewParams.params.studyId + '_clinical_data.tsv',
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

        ReactDOM.render(testElement, document.getElementById('clinical-data-table-div'));
    }

    return {
        init: init
    };
})();
