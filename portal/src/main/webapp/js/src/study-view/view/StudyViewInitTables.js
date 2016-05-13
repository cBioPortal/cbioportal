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

    function init(input, callback) {
        initData(input);
        initTables();
        if (typeof callback === 'function') {
            callback();
        }
    }

    function initData(input) {
        var attr = input.data.attr,
            numOfCases = input.numOfCases;

        attr.forEach(function(e, i) {
            var _worker = {};

            _worker.initStatus = 'unknown'; //unknown, initializing, initialized, failed
            _worker.index = workers.length;
            _worker.opts = {};
            _worker.data = {};
            _worker.callbacks = {};

            switch (e.name) {
                case 'mutatedGenes':
                    _worker.data.type = 'mutatedGene';
                    _worker.data.attr = [{
                        "attr_id": "gene",
                        "display_name": "Gene",
                        "datatype": "STRING",
                        "column_width": 100
                    }, {
                        "attr_id": "numOfMutations",
                        "display_name": "# Mut",
                        "datatype": "NUMBER",
                        "column_width": 90
                    },
                        {
                            "attr_id": "samples",
                            "display_name": "#",
                            "datatype": "NUMBER",
                            "column_width": 90
                        },
                        {
                            "attr_id": "sampleRate",
                            "display_name": "Freq",
                            "datatype": "PERCENTAGE",
                            "column_width": 95
                        },
                        {
                            "attr_id": "caseIds",
                            "display_name": "Cases",
                            "datatype": "STRING",
                            "show": false
                        },
                        {
                            "attr_id": "uniqueId",
                            "display_name": "uniqueId",
                            "datatype": "STRING",
                            "show": false
                        },
                        {
                            "attr_id": "qval",
                            "datatype": "NUMBER",
                            "display_name": "MutSig",
                            "show": false
                        }];
                    _worker.data.getData = function(callback, workerId) {
                        StudyViewProxy.getMutatedGenesData().then(
                            function(data) {
                                callback(mutatedGenesData(data, input.numberOfSamples.numberOfSequencedSamples), workerId);
                            },
                            function(status) {
                                callback(mutatedGenesData(null, input.numberOfSamples.numberOfSequencedSamples), workerId);
                                console.log(status + ", you fail this time");
                            },
                            function(status) {
                                console.log(status);
                            }
                        );
                    };
                    // store message to be used for the table header's qtip
                    _worker.opts.headerQtip = "This table shows cbio cancer genes with 1 or more mutations, as well as any gene with 2 or more mutations";
                    _worker.callbacks.addGeneClick = addGeneClick;
                    break;
                case 'cna':
                    _worker.data.type = 'cna';
                    _worker.data.attr = [{
                        "attr_id": "gene",
                        "display_name": "Gene",
                        "datatype": "STRING",
                        "column_width": 80
                    },
                        {
                            "attr_id": "cytoband",
                            "display_name": "Cytoband",
                            "datatype": "STRING",
                            "column_width": 90
                        },
                        {
                            "attr_id": "altType",
                            "display_name": "CNA",
                            "datatype": "STRING",
                            "column_width": 55
                        },
                        {
                            "attr_id": "samples",
                            "display_name": "#",
                            "datatype": "NUMBER",
                            "column_width": 70
                        },
                        {
                            "attr_id": "altrateInSample",
                            "display_name": "Freq",
                            "datatype": "PERCENTAGE",
                            "column_width": 80
                        },
                        {
                            "attr_id": "caseIds",
                            "display_name": "Cases",
                            "datatype": "STRING",
                            "show": false
                        },
                        {
                            "attr_id": "uniqueId",
                            "display_name": "uniqueId",
                            "datatype": "STRING",
                            "show": false
                        },
                        {
                            "attr_id": "qval",
                            "datatype": "NUMBER",
                            "display_name": "Gistic",
                            "show": false
                        }
                    ];
                    _worker.data.getData = function(callback, workerId) {
                        StudyViewProxy.getCNAData().then(
                            function(data) {
                                callback(cnaData(data, input.numberOfSamples.numberOfCnaSamples), workerId);
                            },
                            function(status) {
                                callback(cnaData(null, input.numberOfSamples.numberOfCnaSamples), workerId);
                                console.log(status + ", you fail this time");
                            },
                            function(status) {
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
            _worker.opts.tableClassName = 'mutated-gene-cna';
            _worker.opts.parentId = 'study-view-charts';
            _worker.callbacks.deleteTable = deleteTable;
            _worker.callbacks.rowClick = rowClick;
            workers.push(_worker);
        });
    }

    function addGeneClick(clickedRowData) {
        // clickedRowData[0] contains the gene
        QueryByGeneTextArea.addRemoveGene(clickedRowData.gene);
    }

    var curSelectedGenes = Array();

    function updateGeneHighlights(geneArray) {
        // loop the tables
        for (var i = 0; i < workers.length; i++) {
            // if the table supports addGeneClick, update the highlights
            if (workers[i].callbacks.addGeneClick != undefined) {
                updateGeneHighlightTable(workers[i], geneArray);
            }
        }
        // store the current geneArray as the current selected genes
        curSelectedGenes = geneArray;
    }

    function updateGeneHighlightTable(worker, geneArray) {
        // get the dataTable, determine the deselected genes and the selected genes
        var deselectGenes = _.difference(curSelectedGenes, geneArray);
        var selectGenes = _.difference(geneArray, curSelectedGenes);

        // update the highlighting
        worker.tableInstance.updateSelectedGenes(worker.data, geneArray);
    }

    function doUpdateGeneHighlightTable(dataTable, worker, array, deselect) {
        var item;
        // for all the genes
        for (var i = 0; i < array.length; i++) {
            // find the appropriate item
            if (worker.opts.name == "cna") {
                item = dataTable.$("td[id*='-" + array[i] + "-']").parent().find(".selectHighlight");
            } else {
                item = dataTable.$("td[id$='-" + array[i] + "']").parent().find(".selectHighlight");
            }

            // change class and qtip
            if (deselect) {
                item.removeClass("geneSelected");
                item.qtip('option', 'content.text', 'Click ' + array[i] + ' to add to your query');
            }
            else {
                item.addClass("geneSelected");
                item.qtip('option', 'content.text', 'Click ' + array[i] + ' to remove from your query');
            }
        }
    }

    function rowClick(tableId, data, clickedRowData, rowSelected) {
        var dcCharts = StudyViewInitCharts.getCharts(),
            worker = findWorker(tableId),
            workerIndex = -1,
            numOfSelectedRows = data.length,
            selectedSamples = [],
            Ids = [],
            exceptionId = [],
            caseIdChartIndex = StudyViewInitCharts.getCaseIdChartIndex();

        if (worker) {
            workerIndex = worker.index;

            for (var i = 0; i < numOfSelectedRows; i++) {
                Ids.push(data[i]);
            }

            workers[workerIndex].data.selected = Ids;

            if (Ids.length !== 0) {
                exceptionId.push(tableId);
                workers[workerIndex].data.selectedSamples = selectedSamples = findSelectedSamples(worker);
            }else {
                workers[workerIndex].data.selectedSamples = [];
                selectedSamples = findSelectedSamples(worker);
            }

            dcCharts[caseIdChartIndex].getChart().filterAll();
            if (selectedSamples.length > 0) {
                dcCharts[caseIdChartIndex].getChart().filter([selectedSamples]);
            }

            redrawOtherCharts(workerIndex, clickedRowData, rowSelected, exceptionId);
        } else {
            console.log('Worker is not available.');
        }
    }

    function findSelectedSamples(worker) {
        var selectedSamples = [];
        if (worker) {
            if (worker.data.selected.length === 0) {
                workers.forEach(function(e, i) {
                    if (e.data.hasOwnProperty('selectedSamples')) {
                        selectedSamples = _.union(selectedSamples, e.data.selectedSamples);
                    }
                });
            } else {
                worker.data.arr.forEach(function(e1, i1) {
                    if (e1.hasOwnProperty('caseIds')) {
                        worker.data.selected.forEach(function(e2) {
                            var match = e2.uniqueId === e1.uniqueId;
                            if (match && e1.hasOwnProperty('caseIds')) {
                                selectedSamples = _.union(selectedSamples, e1.caseIds);
                            }
                        });
                    }
                });
            }
        }
        return selectedSamples;
    }

    function removeTableFilter(tableId, filter) {
        var worker = findWorker(tableId);
        var selectedSamples = [];
        var dcCharts = StudyViewInitCharts.getCharts();
        var caseIdChartIndex = StudyViewInitCharts.getCaseIdChartIndex();
        var selectedRowData = {};

        worker.data.selected = _.reject(worker.data.selected, function(item) {
            if (item.uniqueId === filter) {
                selectedRowData = item;
                return true;
            } else {
                return false;
            }
        })

        worker.data.selectedSamples = findSelectedSamples(worker);

        dcCharts[caseIdChartIndex].getChart().filterAll();
        if (selectedSamples.length > 0) {
            dcCharts[caseIdChartIndex].getChart().filter([selectedSamples]);
        }
        workers[worker.index] == worker;

        redrawOtherCharts(worker.index, selectedRowData, false);
    }

    function redrawOtherCharts(workerIndex, rowData, rowSelected, exceptionId) {
        dc.redrawAll();
        updateBreadCrumb(workerIndex, rowData, rowSelected);
        StudyViewInitCharts.resetBars();
        StudyViewInitCharts.redrawScatter();
        StudyViewInitCharts.redrawWSCharts(exceptionId);
    }

    //function updateBreadCrumb(clickedCell, shiftClicked){
    function updateBreadCrumb(workerIndex, rowData, rowSelected) {
        // we need the id to be able to trigger the click event when the x from the breadcrumb is clicked
        var worker = workers[workerIndex];
        var chartId = worker.opts.tableId;

        if (rowData) {
            if (Object.keys(rowData).length === 0 && worker.data.selectedSamples.length === 0 && worker.data.selected.length === 0) {
                BreadCrumbs.deleteBreadCrumbsByChartId(chartId);
            } else {
                var cellId = chartId + '-';
                var chartFilter;
                var crumbTipText = worker.opts.title + ": ";
                cellId += rowData.uniqueId;
                chartFilter = rowData.uniqueId;
                crumbTipText += rowData.uniqueId;
                BreadCrumbs.updateTableBreadCrumb(chartId, chartFilter, "table", cellId, crumbTipText, rowSelected);
            }
        }
    }

    function deleteTable(tableId, title) {
        $('#' + tableId + '-main').css('display', 'none');
        $('#study_view_add_chart_chzn').css('display', 'block');
        $('#study-view-add-chart')
            .append($('<option></option>')
                .attr('id', tableId + '-main' + '-option')
                .text(title));
        // delete breadcrumbs
        workers.forEach(function(e, i) {
            if (e.opts.tableId === tableId) {
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
        workers.forEach(function(e, i) {
            workers[i].tableInstance = new Table();
            workers[i].initStatus = 'initializing';
            workers[i].tableInstance.initDiv(e)
            workers[i].data.getData(function(data, workerId) {
                workers[workerId].data.arr = data;
                workers[workerId]
                    .tableInstance
                    .draw(workerId, workers[workerId].data, function(id, status) {
                        workers[id].initStatus = status;
                    });
            }, i);
        });
    }

    function mutatedGenesData(data, numOfCases) {
        var genes = [];

        if (data) {
            $('#number-of-selected-sequenced-samples').html(numOfCases);
            for (var i = 0, dataL = data.length; i < dataL; i++) {
                var datum = {},
                    caseIds = data[i].caseIds;

                datum.gene = data[i].gene_symbol;
                datum.numOfMutations = data[i].num_muts;
                datum.samples = _.uniq(caseIds).length;
                datum.sampleRate = (numOfCases <= 0 ? 0 :
                        ((datum.samples / Number(numOfCases) * 100).toFixed(1))) + '%';

                if (data[i].hasOwnProperty('qval') && !isNaN(data[i].qval)) {
                    var qval = Number(data[i].qval);
                    if (qval === 0) {
                        datum.qval = 0;
                    } else {
                        datum.qval = qval.toExponential(1);
                    }
                } else {
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

        if (data) {
            $('#number-of-selected-cna-samples').html(numOfCases);
            for (var i = 0, dataL = data.gene.length; i < dataL; i++) {
                var datum = {},
                    _altType = '';

                switch (data.alter[i]) {
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
                datum.altrateInSample = ((numOfCases <= 0 ? 0 :
                        (datum.samples / numOfCases * 100).toFixed(1))) + '%';
                if (data.gistic[i] && (data.gistic[i] instanceof Array) && !isNaN(data.gistic[i][0])) {
                    var qval = Number(data.gistic[i][0]);
                    if (qval === 0) {
                        datum.qval = 0;
                    } else {
                        datum.qval = qval.toExponential(1);
                    }
                } else {
                    datum.qval = '';
                }
                datum.caseIds = data.caseIds[i];
                datum.uniqueId = datum.gene + '-' + datum.altType;
                genes.push(datum);
            }
        }
        return genes;
    }


    function redrawSingleTable(data, worker) {
        var numSelectedCasesL = data.selectedCases.length,
            exceptionIds = data.exceptionIds;

        // find the worker
        // if the worker is a string, we first need to find the actual worker
        if (typeof worker === 'string') {
            worker = findWorker(worker);
        }

        if (exceptionIds.indexOf(worker.opts.tableId) === -1) {
            if (exceptionIds.indexOf(worker.tableId) === -1) {
                if (numSelectedCasesL.length !== 0) {
                    switch (worker.opts.name) {
                        case 'mutatedGenes':
                            var selectedSequencedSamples = StudyViewUtil.intersection(StudyViewProxy.getSequencedSampleIds(), data.selectedCases);
                            worker.data.arr = mutatedGenesData(StudyViewProxy.getMutatedGeneDataBasedOnSampleIds(data.selectedCases), selectedSequencedSamples.length);
                            break;
                        case 'cna':
                            var selectedCnaSamples = StudyViewUtil.intersection(StudyViewProxy.getCnaSampleIds(), data.selectedCases);
                            worker.data.arr = cnaData(StudyViewProxy.getCNABasedOnSampleIds(data.selectedCases), selectedCnaSamples.length);
                            break;
                        default:
                            break;
                    }
                    worker.tableInstance.redraw(worker.data, function() {
                        worker.tableInstance.stopLoading();
                    });
                } else {
                    worker.data.arr = [];
                    worker.tableInstance.redraw(worker.data);
                }
            }
        }
    }

    function redraw(data) {
        workers.forEach(function(e, i) {
            // check whether table is visible before doing work
            if (isTableVisible(e.opts.tableId)) {
                if (e.initStatus !== 'initializing') {
                    redrawSingleTable(data, e.opts.tableId);
                } else {
                    var interval = setInterval(function() {
                        console.log(e.opts.title + ' is initializing...');
                        if (e.initStatus !== 'initializing') {
                            clearInterval(interval);
                            redrawSingleTable(data, e.opts.tableId);
                        }
                    }, 500);
                }
            }
        });
    }

    // show loading icon for all workers
    function showLoadingIcon(data) {
        //Start loaders
        workers.forEach(function(e, i) {
            showSingleLoadingIcon(data, e);
        });
    }

    // show loading icon for a single worker
    function showSingleLoadingIcon(data, worker) {
        var exceptionIds = data.exceptionIds;

        // if the worker is a string, we first need to find the actual worker
        if (typeof worker === 'string') {
            worker = findWorker(worker);
        }

        // check whether the current table is in the exceptions; if not show the loading image
        if (exceptionIds.indexOf(worker.opts.tableId) === -1) {
            worker.tableInstance.startLoading();
        }

    }

    // find the worker for a tableId
    function findWorker(tableId) {
        for (var i = 0; i < workers.length; i++) {
            if (workers[i].opts.tableId === tableId) {
                return workers[i];
            }
        }
    }

    // check whether a table is visible
    function isTableVisible(tableId) {
        return $("#" + tableId + "-main").css("display") != "none";
    }

    function clearAllSelected() {
        workers.forEach(function(e, i) {
            if (workers[i].data.hasOwnProperty('selectedSamples')) {
                workers[i].data.selectedSamples.length = 0;
            }
            if (workers[i].data.hasOwnProperty('selected')) {
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
            if (workers.length > 0) {
                return true;
            } else {
                return false;
            }
        },
        resizeTable: function() {
            workers.forEach(function(e, i) {
                e.tableInstance.resize();
            });
        },
        removeTableFilter: removeTableFilter
    };

})();
