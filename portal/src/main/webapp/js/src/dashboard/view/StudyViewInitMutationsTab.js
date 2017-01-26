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

var StudyViewInitMutationsTab = (function(){
    var data = [];
    var dataTable;
    
    function init(_data){
        data = _data;
        loadMutatedGenes(
            StudyViewParams.params.studyId, 
            StudyViewParams.params.mutationProfileId, 
            StudyViewParams.params.hasMutSig
        );
    }
    
    function loadMutatedGenes(cancerStudyId, mutationProfileId, hasMutSig) {
       
        if (data===null || (typeof data) !== (typeof []) || data.length===0) {
            $("#mut-sig-msg").html("No recurrently mutated genes (SMGs) are detected in this cancer study.");
            return;
        }
        $("#mut-sig-msg").hide();

        var ix = [];
        var n = data.length;
        for (var i=0; i<n; i++) {
            ix.push([i]);
        }

        dataTable = $('#smg_table').dataTable( {
            "sScrollY": "500px",
            "bPaginate": false,
            "bScrollCollapse": true,
            "sScrollX": "100%",
            "sScrollXInner": "100%",
            "sDom": '<<"H"<"smg-table-name">fr>t>',
            "bJQueryUI": true,
            "bDestroy": true,
            "aaData": ix,
            "aoColumnDefs":[
                {// data
                    "aTargets": [ 0 ],
                    "bVisible": false,
                    "mData" : 0
                },
                {// gene
                    "aTargets": [ 1 ],
                    "mDataProp": function(source,type,value) {
                        if (type==='set') {
                            return;
                        } else if (type==='display') {
                            var gene = data[source[0]]['gene_symbol'];
                            return '<a href="index.do?Action=Submit&genetic_profile_ids='
                                +StudyViewParams.params.mutationProfileId+'&case_set_id='+StudyViewParams.params.caseSetId+'&cancer_study_id='+StudyViewParams.params.studyId
                                +'&gene_list='+gene+'&tab_index=tab_visualize&#mutation_details">'+gene+'</a>';
                        } else {
                            return data[source[0]]['gene_symbol'];
                        }
                    }
                },
                {// cytoband
                    "aTargets": [ 2 ],
                    "mDataProp": function(source,type,value) {
                        if (type==='set') {
                            return;
                        } else {
                            return data[source[0]]['cytoband'];
                        }
                    },
                },
                {// gene size
                    "aTargets": [ 3 ],
                    "sClass": "right-align-td",
                    "mDataProp": function(source,type,value) {
                        if (type==='set') {
                            return;
                        } else if (type==='display') {
                            var length = data[source[0]]['length'];
                            return length ? length : "";
                        } else if (type==='sort') {
                            var length = data[source[0]]['length'];
                            return length ? length : 0;
                        } else if (type==='type') {
                            return 0.0;
                        } else {
                            var length = data[source[0]]['length'];
                            return length ? length : 0;
                        }
                    }
                },
                {// #muts
                    "aTargets": [ 4 ],
                    "sClass": "right-align-td",
                    "mDataProp": function(source,type,value) {
                        if (type==='set') {
                            return;
                        } else {
                            return data[source[0]]['num_muts'];
                        }
                    },
                    "asSorting": ["desc", "asc"]
                },
                {// #muts / bp
                    "aTargets": [ 5 ],
                    "sClass": "right-align-td",
                    "mDataProp": function(source,type,value) {
                        if (type==='set') {
                            return;
                        } else if (type==='display') {
                            var muts = data[source[0]]['num_muts'];
                            var length = data[source[0]]['length'];
                            return length ? cbio.util.toPrecision(muts/length, 3, 0.01) : "";
                        } else if (type==='sort') {
                            var muts = data[source[0]]['num_muts'];
                            var length = data[source[0]]['length'];
                            return length ? muts/length : 0;
                        } else if (type==='type') {
                            return 0.0;
                        } else {
                            var muts = data[source[0]]['num_muts'];
                            var length = data[source[0]]['length'];
                            return length ? muts/length : 0;
                        }
                    },
                    "asSorting": ["desc", "asc"]
                },
                {// mutsig qval
                    "aTargets": [ 6 ],
                    "bVisible": hasMutSig,
                    "sClass": "right-align-td",
                    "mDataProp": function(source,type,value) {
                        if (type==='set') {
                            return;
                        } else if (type==='display') {
                            var qval = data[source[0]]['qval'];
                            return cbio.util.checkNullOrUndefined(qval) ? "":cbio.util.toPrecision(qval, 3, 0.01);
                        } else if (type==='sort') {
                            var qval = data[source[0]]['qval'];
                            return cbio.util.checkNullOrUndefined(qval) ? 1 : qval;
                        } else if (type==='type') {
                            return 0.0;
                        } else {
                            var qval = data[source[0]]['qval'];
                            return cbio.util.checkNullOrUndefined(qval) ? 1 : qval;;
                        }
                    },
                    "asSorting": ["asc", "desc"]
                }
            ],
            "aaSorting": [[6,'asc'],[5,'desc']],
            "oLanguage": {
                "sInfo": "&nbsp;&nbsp;(_START_ to _END_ of _TOTAL_)&nbsp;&nbsp;",
                "sInfoFiltered": "",
                "sLengthMenu": "Show _MENU_ per page"
            }
        });

        dataTable.css("width","100%");

        $('.smg-table-name').html(n+" mutated genes <img id='mutations-summary-help' src='images/help.png' title='Genes that <ul><li>are in the top 500 (randed by mutations per nucleotide) recurrently mutated (2 or more mutations)</li><li>or are cancer genes</li><li>or are detected by MutSig</li></ul>.' alt='help' />");
        $('#mutations-summary-help').qtip({
            content: { attr: 'title' },
            style: { classes: 'qtip-light qtip-rounded' },
            position: { my:'top center',at:'bottom center' }
        });

        $('.smg-table-name').addClass('data-table-name');
        
        $('#smg_wrapper_table').show();
    }
    
    return{
        init: init,
        getDataTable: function () {
            return dataTable;
        }
    };
})();
