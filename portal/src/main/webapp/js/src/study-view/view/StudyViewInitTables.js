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



var StudyViewInitTables = (function() {
    var workers = [];
    
    function init(input,callback) {
        initData(input);
        initTables();
        if(typeof callback === 'function'){
            callback();
        }
    }
    
    function initData(input) {
        var attr = input.data.attr,
            numOfCases = input.numOfCases;
        
        attr.forEach(function(e, i) {
            var _worker = {};
            
            _worker.opts = {};
            _worker.data = {};
            _worker.callbacks = {};
            
            switch (e.name) {
                case 'mutatedGenes':
                    _worker.data.attr = [{
                            name: 'gene',
                            displayName: 'Gene'
                        },{
                            name: 'numOfMutations',
                            displayName: '# Mut'
                        },{
                            name: 'samples',
                            displayName: '#',
                            qtip: 'Number of samples'
                        },{
                            name: 'sampleRate',
                            displayName: 'Freq'
                        },{
                            name: 'qval',
                            displayName: 'MutSig',
                            hidden: true
                        },{
                        displayName: 'Sample IDs',
                            name: 'caseIds',
                            hidden: true
                        },{
                            name: 'uniqueId',
                            hidden: true
                        }
                    ];
                    _worker.data.getData = function (callback, workerId){
                        StudyViewProxy.getMutatedGenesData().then(
                            function( data ) {
                                callback(mutatedGenesData(data, numOfCases), workerId);
                            },
                            function( status ) {
                                callback(mutatedGenesData(null, numOfCases), workerId);
                                console.log( status + ", you fail this time" );
                            },
                            function( status ) {
                                console.log(status);
                            }
                        );
                    };
                    break;
                case 'cna':
                    _worker.data.attr = [{
                            name: 'gene',
                            displayName: 'Gene'
                        },{
                            name: 'cytoband',
                            displayName: 'Cytoband'
                        },{
                            name: 'altType',
                            displayName: 'CNA'
                        },{
                            name: 'samples',
                            displayName: '#',
                            qtip: 'Number of samples'
                        },{
                            name: 'altrateInSample',
                            displayName: 'Freq'
                        },{
                            name: 'qval',
                            displayName: 'Gistic',
                            hidden: true
                        },{
                            displayName: 'Sample IDs',
                            name: 'caseIds',
                            hidden: true
                        },{
                            name: 'uniqueId',
                            hidden: true
                        }
                    ];
                    _worker.data.getData = function (callback, workerId){
                        StudyViewProxy.getCNAData().then(
                            function( data ) {
                                callback(cnaData(data, numOfCases), workerId);
                            },
                            function( status ) {
                                callback(cnaData(null, numOfCases), workerId);
                                console.log( status + ", you fail this time" );
                            },
                            function( status ) {
                                console.log(status);
                            }
                        );
                    };
                    break;
                default:
                    _worker.opts.title = 'Unknown';
                    break;
            }
            _worker.data.selected = [];
            _worker.opts.title = e.displayName || '';
            _worker.opts.name = e.name;
            _worker.opts.tableId = 'study-view-table-' + e.name;
            _worker.opts.parentId = 'study-view-charts';
            _worker.opts.webService = e.webService;
            _worker.callbacks.deleteTable = deleteTable;
            _worker.callbacks.rowClick = rowClick;
            workers.push(_worker);
        });
    }
    
    function rowClick(tableId, data, clickedRowData, rowSelected) {
        var dcCharts = StudyViewInitCharts.getCharts(),
            dcChartsL = dcCharts.length,
            worker = '',
            workerIndex = -1,
            numOfSelectedRows = data.length,
            selectedSamples = [],
            Ids = [],
            exceptionId = [],
            caseIdChartIndex = StudyViewInitCharts.getCaseIdChartIndex();;
        
        //Find reletive table data
        for(var i = 0; i < workers.length; i++) {
            if(workers[i].opts.tableId === tableId)  {
                worker = workers[i];
                workerIndex = i;
                break;
            }
        }
        
        switch (worker.opts.name) {
            case 'mutatedGenes':
                for(var i=0; i < numOfSelectedRows; i++) {
                    Ids.push({gene: data[i][0]});
                }
                break;
            case 'cna':
                for(var i=0; i < numOfSelectedRows; i++) {
                    Ids.push({gene: data[i][0], altType: data[i][2]});
                }
                break;
            default:
                break;
        }
        
        workers[workerIndex].data.selected= Ids;
        
        if(Ids.length === 0) {
            workers[workerIndex].data.selectedSamples.length=0;
            workers.forEach(function(e, i){
                if(e.data.hasOwnProperty('selectedSamples')) {
                    selectedSamples = StudyViewUtil.arrayDeDuplicate(selectedSamples.concat(e.data.selectedSamples));
                }
            });
        }else {
            exceptionId.push(tableId);
            worker.data.arr.forEach(function(e1, i1){
                if(e1.hasOwnProperty('caseIds')) {
                    worker.data.selected.forEach(function(e2, i2){
                        var match = true;
                        for(var key in e2) {
                            if(e2.hasOwnProperty(key)) {
                                if(e2[key] !== e1[key]) {
                                    match = false;
                                    break;
                                }
                            }
                        }
                        if(match && e1.hasOwnProperty('caseIds')) {
                            selectedSamples = StudyViewUtil.arrayDeDuplicate(selectedSamples.concat(e1.caseIds));
                        }
                    });
                }
            });
            workers[workerIndex].data.selectedSamples = selectedSamples;
        }

        dcCharts[caseIdChartIndex].getChart().filterAll();
        if(selectedSamples.length > 0){
            dcCharts[caseIdChartIndex].getChart().filter([selectedSamples]);
        }
        dc.redrawAll();

        updateBreadCrumb(workerIndex, clickedRowData, rowSelected);
        StudyViewInitCharts.resetBars();
        StudyViewInitCharts.redrawScatter();
        StudyViewInitCharts.redrawWSCharts(exceptionId);
    }

    //function updateBreadCrumb(clickedCell, shiftClicked){
    function updateBreadCrumb(workerIndex, rowData, rowSelected){
        // we need the id to be able to trigger the click event when the x from the breadcrumb is clicked
        var worker = workers[workerIndex];
        var chartId = worker.opts.tableId;

        if(rowData) {
            if(rowData.length ===0 && worker.data.selectedSamples.length === 0 && worker.data.selected.length === 0) {
                BreadCrumbs.deleteBreadCrumbsByChartId(chartId);
            }else{
                var cellId = chartId + '-';
                var chartFilter;
                var crumbTipText = worker.opts.title+": ";
                switch (worker.opts.name) {
                    case 'mutatedGenes':
                        cellId += rowData[6];
                        chartFilter = rowData[6];
                        crumbTipText += rowData[6];
                        break;
                    case 'cna':
                        cellId += rowData[7];
                        chartFilter = rowData[7];
                        crumbTipText += rowData[7];
                        break;
                    default:
                        break;
                }
                BreadCrumbs.updateTableBreadCrumb(chartId, chartFilter, "table", cellId, crumbTipText, rowSelected);
            }
        }
    }

    function deleteTable(tableId, title) {
        $('#' + tableId + '-main').css('display','none');
        $('#study_view_add_chart_chzn').css('display','block');
        $('#study-view-add-chart')
                .append($('<option></option>')
                    .attr('id',tableId + '-main' + '-option')
                    .text(title));
        // delete breadcrumbs
        workers.forEach(function (e, i) {
            if(e.opts.tableId === tableId) {
                e.data.selectedSamples = [];
                e.data.selected = [];
            }
        });
        rowClick(tableId, [], [], false);
        StudyViewInitCharts.bondDragForLayout();
        AddCharts.bindliClickFunc();
    }
    
    function initTables() {
        StudyViewUtil.addCytobandSorting();
        workers.forEach(function(e, i){
            workers[i].tableInstance = new Table();
            workers[i].tableInstance.initDiv(e)
            workers[i].data.getData(function(data, workerId){
                workers[workerId].data.arr = data;
                workers[workerId].tableInstance.draw(workers[workerId].data);
            }, i);
        });
    }
    
    function mutatedGenesData(data, numOfCases) {
        var genes = [];

        if(data) {
            for(var i = 0, dataL = data.length; i < dataL; i++){
                var datum = {},
                    caseIds = data[i].caseIds;

                datum.gene = data[i].gene_symbol;
                datum.numOfMutations = Number(data[i].num_muts);
                datum.samples = caseIds.filter(function(elem, pos) {
                    return caseIds.indexOf(elem) === pos;
                }).length;
                datum.sampleRate =
                    (datum.samples / Number(numOfCases)* 100).toFixed(1) + '%';

                if( data[i].hasOwnProperty('qval') && !isNaN(data[i].qval)){
                    var qval = Number(data[i].qval);
                    if(qval === 0) {
                        datum.qval = 0;
                    }else{
                        datum.qval = qval.toExponential(1);
                    }
                }else{
                    datum.qval = '';
                }

                datum.caseIds = data[i].caseIds;
                datum.uniqueId = datum.gene;
                genes.push(datum);
            }
        }
        return genes;
    }
    
    function cnaData(data, numOfCases) {

        var genes = [];

        if(data) {
            for(var i = 0, dataL = data.gene.length; i < dataL; i++){
                var datum = {},
                    _altType = '';

                switch(data.alter[i]) {
                    case -2:
                        _altType = 'DEL';
                        break;
                    case 2:
                        _altType = 'AMP';
                        break;
                    default:
                        break;
                }
                datum.gene = data.gene[i];
                datum.cytoband = data.cytoband[i];
                datum.altType = _altType;
                datum.samples = data.caseIds[i].length;
                datum.altrateInSample = (datum.samples / numOfCases * 100).toFixed(1) + '%';
                if(data.gistic[i] && (data.gistic[i] instanceof Array) && !isNaN(data.gistic[i][0])){
                    var qval = Number(data.gistic[i][0]);
                    if(qval === 0) {
                        datum.qval = 0;
                    }else{
                        datum.qval = qval.toExponential(1);
                    }
                }else{
                    datum.qval = '';
                }
                datum.caseIds = data.caseIds[i];
                datum.uniqueId = datum.gene + '-' + datum.altType;
                genes.push(datum);
            }
        }
        return genes;
    }
    
    function redraw(data){
        var numSelectedCasesL = data.selectedCases.length,
            exceptionIds = [];
    
        if(data.hasOwnProperty('exceptionIds') && typeof data.exceptionIds === 'object'){
            exceptionIds = data.exceptionIds;
        }
        //Start loaders
        workers.forEach(function(e, i){
            if(exceptionIds.indexOf(e.opts.tableId) === -1){
                e.tableInstance.startLoading();
            }
        });
        
        workers.forEach(function(e, i){
            if(exceptionIds.indexOf(e.opts.tableId) === -1){
                if(numSelectedCasesL.length !== 0){
                    $.ajax(data.webService[e.opts.name])
                        .done(function(d){
                            switch (e.opts.name) {
                                case 'mutatedGenes':
                                    workers[i].data.arr = mutatedGenesData(d, numSelectedCasesL);
                                    break;
                                case 'cna':
                                    workers[i].data.arr = cnaData(d, numSelectedCasesL);
                                    break;
                                default:
                                    break;
                            }
                            e.tableInstance.redraw(workers[i].data, function(){
                                e.tableInstance.stopLoading();
                            });
                        });
                }else{
                    workers[i].data.arr = [];
                    e.tableInstance.redraw(workers[i].data);;
                }
            }
        });
    }
    
    function clearAllSelected() {
        workers.forEach(function(e, i){
            if(workers[i].data.hasOwnProperty('selectedSamples')){
                workers[i].data.selectedSamples.length = 0;
            }
            if(workers[i].data.hasOwnProperty('selected')){
                workers[i].data.selected.length = 0;
            }
        });
    }
    
    return {
        init: init,
        redraw: redraw,
        clearAllSelected: clearAllSelected,
        getInitStatus: function() {
            if(workers.length > 0) {
                return true;
            }else {
                return false;
            }
        },
        resizeTable: function() {
            workers.forEach(function(e, i) {
                e.tableInstance.resize();
            });
        }
    };
    
})();