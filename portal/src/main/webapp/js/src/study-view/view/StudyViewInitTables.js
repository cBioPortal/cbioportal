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
                                callback(mutatedGenesData(data, input.numberOfSamples.numberOfSequencedSamples), workerId);
                            },
                            function( status ) {
                                callback(mutatedGenesData(null, input.numberOfSamples.numberOfSequencedSamples), workerId);
                                console.log( status + ", you fail this time" );
                            },
                            function( status ) {
                                console.log(status);
                            }
                        );
                    };
                    _worker.callbacks.addGeneClick = addGeneClick;
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
                                callback(cnaData(data, input.numberOfSamples.numberOfCnaSamples), workerId);
                            },
                            function( status ) {
                                callback(cnaData(null, input.numberOfSamples.numberOfCnaSamples), workerId);
                                console.log( status + ", you fail this time" );
                            },
                            function( status ) {
                                console.log(status);
                            }
                        );
                    };
                    _worker.callbacks.addGeneClick = addGeneClick;
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
            _worker.callbacks.deleteTable = deleteTable;
            _worker.callbacks.rowClick = rowClick;
            workers.push(_worker);
        });
    }

    function addGeneClick(clickedRowData){
        // clickedRowData[0] contains the gene
        QueryByGeneTextArea.addRemoveGene(clickedRowData[0]);
    }

    var curSelectedGenes=Array();
    function updateGeneHighlights(geneArray){
        // loop the tables
        for(var i = 0; i < workers.length; i++) {
            // if the table supports addGeneClick, update the highlights
            if(workers[i].callbacks.addGeneClick != undefined){
                updateGeneHighlightTable(workers[i], geneArray);
            }
        }
        // store the current geneArray as the current selected genes
        curSelectedGenes = geneArray;
    }

    function updateGeneHighlightTable(worker, geneArray){
        // get the dataTable, determine the deselected genes and the selected genes
        var dataTable = worker.tableInstance.getDataTable();
        var deselectGenes = _.difference(curSelectedGenes, geneArray);
        var selectGenes = _.difference(geneArray, curSelectedGenes);

        // update the highlighting
        doUpdateGeneHighlightTable(dataTable, worker, deselectGenes, true);
        doUpdateGeneHighlightTable(dataTable, worker, selectGenes, false);
    }

    function doUpdateGeneHighlightTable(dataTable, worker, array, deselect){
        var item;
        // for all the genes
        for(var i=0; i<array.length; i++) {
            // find the appropriate item
            if (worker.opts.name == "cna")
                item = dataTable.$("td[id*='-" + array[i] + "-']").parent().find(".selectHighlight");
            else
                item = dataTable.$("td[id$='-" + array[i] + "']").parent().find(".selectHighlight");

            // change class and qtip
            if(deselect) {
                item.removeClass("geneSelected");
                item.qtip('option', 'content.text', 'Click '+array[i]+' to add to your query');
            }
            else{
                item.addClass("geneSelected");
                item.qtip('option', 'content.text', 'Click '+array[i]+' to remove from your query');
            }
        }
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
            $('#number-of-selected-sequenced-samples').html(numOfCases);
            for(var i = 0, dataL = data.length; i < dataL; i++){
                var datum = {},
                    caseIds = data[i].caseIds;

                datum.gene = data[i].gene_symbol;
                datum.numOfMutations = data[i].num_muts;
                datum.samples = _.uniq(caseIds).length;
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
            $('#number-of-selected-cna-samples').html(numOfCases);
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



    function redrawSingleTable(data, worker){
        var numSelectedCasesL = data.selectedCases.length,
            exceptionIds = data.exceptionIds;

        // find the worker
        // if the worker is a string, we first need to find the actual worker
        if(typeof worker === 'string'){
            worker = findWorker(worker);
        }

        if (exceptionIds.indexOf(worker.opts.tableId) === -1) {
            if (exceptionIds.indexOf(worker.tableId) === -1) {
                if (numSelectedCasesL.length !== 0) {
                    switch (worker.opts.name) {
                        case 'mutatedGenes':
                            var selectedSequencedSamples = _.intersection(StudyViewProxy.getSequencedSampleIds(), data.selectedCases);
                            worker.data.arr = mutatedGenesData(StudyViewProxy.getMutatedGeneDataBasedOnSampleIds(data.selectedCases), selectedSequencedSamples.length);
                            break;
                        case 'cna':
                            var selectedCnaSamples = _.intersection(StudyViewProxy.getCnaSampleIds(), data.selectedCases);
                            worker.data.arr = cnaData(StudyViewProxy.getCNABasedOnSampleIds(data.selectedCases), selectedCnaSamples.length);
                            break;
                        default:
                            break;
                    }
                    worker.tableInstance.redraw(worker.data, function () {
                        worker.tableInstance.stopLoading();
                    });
                } else {
                    worker.data.arr = [];
                    worker.tableInstance.redraw(worker.data);
                }
            }
        }
    }

    function redraw(data){
        workers.forEach(function (e, i) {
            // check whether table is visible before doing work
            if(isTableVisible(e.opts.tableId)) {
                redrawSingleTable(data, e.opts.tableId);
            }
        });
    }

    // show loading icon for all workers
    function showLoadingIcon(data){
        //Start loaders
        workers.forEach(function(e, i){
            showSingleLoadingIcon(data, e);
        });
    }

    // show loading icon for a single worker
    function showSingleLoadingIcon(data, worker){
        var exceptionIds = data.exceptionIds;

        // if the worker is a string, we first need to find the actual worker
        if(typeof worker === 'string'){
            worker = findWorker(worker);
        }

        // check whether the current table is in the exceptions; if not show the loading image
        if (exceptionIds.indexOf(worker.opts.tableId) === -1) {
            worker.tableInstance.startLoading();
        }

    }

    // find the worker for a tableId
    function findWorker(tableId){
        for(var i=0; i<workers.length; i++){
            if (workers[i].opts.tableId === tableId) {
                return workers[i];
            }
        }
    }

    // check whether a table is visible
    function isTableVisible(tableId){
        return $("#"+tableId+"-main").css("display")!="none";
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
        updateGeneHighlights: updateGeneHighlights,
        showLoadingIcon: showLoadingIcon,
        showSingleLoadingIcon: showSingleLoadingIcon,
        redrawSingleTable: redrawSingleTable,
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
