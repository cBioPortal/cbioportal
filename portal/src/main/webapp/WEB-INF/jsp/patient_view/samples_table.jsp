<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 -
 - This library is distributed in the hope that it will be useful, but WITHOUT
 - ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 - FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 - is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 - obligations to provide maintenance, support, updates, enhancements or
 - modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 - liable to any party for direct, indirect, special, incidental or
 - consequential damages, including lost profits, arising out of the use of this
 - software and its documentation, even if Memorial Sloan-Kettering Cancer
 - Center has been advised of the possibility of such damage.
 --%>

<%--
 - This file is part of cBioPortal.
 -
 - cBioPortal is free software: you can redistribute it and/or modify
 - it under the terms of the GNU Affero General Public License as
 - published by the Free Software Foundation, either version 3 of the
 - License.
 -
 - This program is distributed in the hope that it will be useful,
 - but WITHOUT ANY WARRANTY; without even the implied warranty of
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 - GNU Affero General Public License for more details.
 -
 - You should have received a copy of the GNU Affero General Public License
 - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%--
  ~ Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
  ~ This library is free software; you can redistribute it and/or modify it
  ~ under the terms of the GNU Lesser General Public License as published
  ~ by the Free Software Foundation; either version 2.1 of the License, or
  ~ any later version.
  ~
  ~ This library is distributed in the hope that it will be useful, but
  ~ WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  ~ MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  ~ documentation provided hereunder is on an "as is" basis, and
  ~ Memorial Sloan-Kettering Cancer Center
  ~ has no obligations to provide maintenance, support,
  ~ updates, enhancements or modifications.  In no event shall
  ~ Memorial Sloan-Kettering Cancer Center
  ~ be liable to any party for direct, indirect, special,
  ~ incidental or consequential damages, including lost profits, arising
  ~ out of the use of this software and its documentation, even if
  ~ Memorial Sloan-Kettering Cancer Center
  ~ has been advised of the possibility of such damage.  See
  ~ the GNU Lesser General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Lesser General Public License
  ~ along with this library; if not, write to the Free Software Foundation,
  ~ Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
  --%>

<%@ page import="org.mskcc.cbio.portal.dao.DaoTypeOfCancer" %>
<%@ page import="org.mskcc.cbio.portal.model.TypeOfCancer" %>
<%@ page import="org.mskcc.cbio.portal.dao.DaoException" %><%
    String cancerTypeId = cancerStudy.getTypeOfCancerId().trim();
    TypeOfCancer typeOfCancerById = DaoTypeOfCancer.getTypeOfCancerById(cancerTypeId);
    String trialKeywords = typeOfCancerById.getClinicalTrialKeywords();
%>

<style type="text/css">
@import "css/data_table_jui.css?<%=GlobalProperties.getAppVersion()%>";
@import "css/data_table_ColVis.css?<%=GlobalProperties.getAppVersion()%>";
.clinical-attr-table .ColVis {
        float: left;
        margin-bottom: 0
}
.clinical-attr-table .dataTables_length {
        width: auto;
        float: right;
}
.clinical-attr-table .dataTables_info {
        clear: none;
        width: auto;
        float: right;
}
.clinical-attr-table .dataTables_filter {
        width: 40%;
}
.clinical-attr-table .div.datatable-paging {
        width: auto;
        float: right;
}
.clinical-attr-table .data-table-name {
        float: left;
        font-weight: bold;
        font-size: 120%;
        vertical-align: middle;
}
.clinical-attr-table .ColVis_collection {
    width: 500px;
}
.clinical-attr-table .ColVis_Button {
    white-space: nowrap;
}
.clinical-attr-table .ColVis_Button.TableTools_Button.ColVis_MasterButton{
    outline: none;
    background-color: white;
/*    color: #2986e2;
    border: 1px solid #2986e2;
    border-radius: 5px;
    -webkit-border-radius: 5px;
    -moz-border-radius: 5px;*/
    cursor: pointer;
    height: 23px;
    padding: 0;
}
.clinical-attr-table .ColVis_Button.TableTools_Button.ColVis_MasterButton span{
    padding: 2px 6px 3px 6px;
}
.clinical-attr-table #dataTables_filter {
    width:auto;
    float: right;
}
.clinical-attr-table .dataTables_filter label input {
    appearance: searchfield;
    -moz-appearance: searchfield;
    -webkit-appearance: searchfield;
}
.clinical-attr-table table.dataTable>tbody>tr>td {
    white-space: pre-wrap;
    max-width: 800px;
}
.clinical-attr-table .DTTT_container.ui-buttonset.ui-buttonset-multi a {
    width: 50px;
    height: 20px;
    line-height: 20px;
}
.clinical-attr-table .DTTT_container.ui-buttonset.ui-buttonset-multi {
    float: left;
}
.table-link {
    color: #428bca;
    cursor: pointer;
}
</style>
<script type="text/javascript" src="js/lib/jquery.highlight-4.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript">
    /* Get attribute to display name mapping */
    /*
    var xmlhttp = new XMLHttpRequest();
    var url = "js/src/patient-view/norm2display.json";

    xmlhttp.onreadystatechange = function() {
        if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
            var myArr = JSON.parse(xmlhttp.responseText);
            myFunction(myArr);
        }
    }
    xmlhttp.open("GET", url, true);
    xmlhttp.send();*/
   
    // Build the table
    var populateSamplesTable = function() {
        $("#samples_table_wait").hide();
        
        table_text = '<table id="samples-table"></table>';
        var arrayUnique = function(a) {
            return a.reduce(function(p, c) {
                if (p.indexOf(c) < 0) p.push(c);
                return p;
            }, []);
        };
        var all_keys = [];
        all_keys = arrayUnique(($.map(clinicalDataMap, function (o) {return Object.keys(o)})));
        var samples = Object.keys(clinicalDataMap);
        clinicalData = all_keys.map(function(k) {
            clicopy = {};
            clicopy["ATTR"] = clinicalAttributes[k]["displayName"];
            Object.keys(samples).forEach(function (i) {
                clicopy[i]
            })
            for (var i=0; i<samples.length; ++i) {
                clicopy[i] =  clinicalDataMap[samples[i]][k] || "N/A";
            }
            return clicopy;
        });
        // Columns for datatable
        var columns = [];
        for (var i=0; i<samples.length; ++i) {
            columns.push({"sTitle":samples[i],"mData":i});
        }
        var samplesDataTable = $("#samples-table").dataTable({
            "bSort": false,
            "sDom": '<"H"TC<"dataTableReset">f>rt',
            "bJQueryUI": true,
            "bDestroy": true,
            "autoWidth": true,
            "aaData": clinicalData,
            "aoColumns": [{"sTitle":"Attribute","mData":"ATTR"}].concat(columns),
            "oLanguage": {
                "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                "sInfoFiltered": "",
                "sLengthMenu": "Show _MENU_ per page",
                "sEmptyTable": "Could not find any clinical information on samples."
            },
            tableTools: {
                "sSwfPath": "/swf/copy_csv_xls_pdf.swf",
                "aButtons": [
                    "copy",
                    "csv"
                ]
            },
            "iDisplayLength": -1
        }); 
        samplesDataTable.css("width","100%");
        $("#samples-table_wrapper").addClass("clinical-attr-table");
    };
    
    var populatePatientTable = function() {
        $("#patient_table_wait").hide();
        
        clinicalData = [];
        for (var key in patientInfo) {
            clinicalData.push([(key in clinicalAttributes && clinicalAttributes[key]["displayName"]) || key, patientInfo[key]]);
        }
        table_text = '<table id="patient-table"></table>';
        var patientDataTable = $("#patient-table").dataTable({
            "bSort": false,
            "sDom": '<"H"TC<"dataTableReset">f>rt',
            "bJQueryUI": true,
            "bDestroy": true,
            "autoWidth": true,
            "aaData": clinicalData,
            "aoColumnDefs": [
                {
                    "sTitle": "Attribute",
                    "aTargets": [ 0 ],
                    "sClass": "left-align-td",
                },
                {
                    "sTitle": "Value",
                    "aTargets": [ 1 ],
                    "sClass": "left-align-td",
                    "bSortable": false
                }
            ],
            "aaSorting": [[0,'asc']],
            "oLanguage": {
                "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                "sInfoFiltered": "",
                "sLengthMenu": "Show _MENU_ per page",
                "sEmptyTable": "Could not find any clinical information on patient."
            },
            tableTools: {
                "sSwfPath": "/swf/copy_csv_xls_pdf.swf",
                "aButtons": [
                    "copy",
                    "csv"
                ]
            },
            "iDisplayLength": -1
        });
        patientDataTable.css("width","100%");
        $("#patient-table_wrapper").addClass("clinical-attr-table");
    };
    
    var patientTableLoaded = false;
    function loadPatientTable() {
        if (patientTableLoaded) return;
        populatePatientTable();
        patientTableLoaded = true;
    }
    
    var samplesTableLoaded = false;
    function loadSamplesTable() {
        if (samplesTableLoaded) return;
        populateSamplesTable();
        samplesTableLoaded = true;
    }
    
    $("#link-samples-table").click( function() {
        loadSamplesTable();
        loadPatientTable();

        $("#load-samples-table").click( function() {
            $("#patient-table_wrapper").hide();
            $("#samples-table_wrapper").show();
            $(this).css("color", "black");
            $(this).css("font-weight", "bold");
            $(this).css("cursor", "text");
            $("#load-patient-table").css("color", "#428bca");
            $("#load-patient-table").css("cursor", "pointer");
            $("#load-patient-table").css("font-weight", "normal");
        });
        $("#load-patient-table").click( function() {
            $("#samples-table_wrapper").hide();
            $("#patient-table_wrapper").show();
            $(this).css("color", "black");
            $(this).css("font-weight", "bold");
            $(this).css("cursor", "text");
            $("#load-samples-table").css("color", "#428bca");
            $("#load-samples-table").css("cursor", "pointer");
            $("#load-samples-table").css("font-weight", "normal");
        });
        $("#load-patient-table").click();
    });
</script>

<h3 style="color: black;">Clinical Information</h3>
<a id="load-patient-table" class="table-link activated">Patient</a> / <a id="load-samples-table" class="table-link">Samples</a>
<table id="samples-table">
</table>
<div id="samples_table_wait"><img src="images/ajax-loader.gif" alt="loading" /></div>
<table id="patient-table">
</table>
<div id="patient_table_wait"><img src="images/ajax-loader.gif" alt="loading" /></div>
