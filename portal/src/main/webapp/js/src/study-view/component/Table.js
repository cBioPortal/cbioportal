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


var Table = function() {
    var divs = {},
        type = '',
        attr = [],
        arr = [],
        attrL = 0,
        arrL = 0,
        selectedRows = [],
        selectedGenes = [],
        selectedSamples = [],
        callbacks = {},
        headerQtip,
        initStatus = false;
    
    function init(input) {
        initStatus = true;
        try {
            initData(input);
            initDiv();
            initTable(input.data);
            initReactTables();
            addEvents();
        }catch(e) {
            initStatus = false;
            throw e;
        }
        if(callbacks.hasOwnProperty('callback')) {
            callbacks.callback();
        }
    }
    
    function initData(input) {
        if(input.hasOwnProperty('opts') && input.opts.hasOwnProperty('parentId')) {
            var tableId = input.opts.hasOwnProperty('tableId')?input.opts.tableId:createTableId();

            divs.attachedId = input.opts.parentId;
            divs.mainId = tableId + '-main';
            divs.title = input.opts.title || 'Unknown';
            divs.titleId = tableId + '-title';
            divs.titleWrapperId = divs.titleId + '-wrapper';
            divs.deleteIconId = divs.titleId + '-delete';
            divs.tableId = tableId;
            divs.tableClassName = input.opts.tableClassName || '';
            divs.headerId = tableId + '-header';
            divs.reloadId = tableId + '-reload-icon';
            divs.downloadId = tableId + '-download-icon';
            divs.downloadWrapperId = tableId + '-download-icon-wrapper';
            divs.loaderId = tableId + '-loader';
            // store the header qtip if available
            headerQtip = input.opts.hasOwnProperty('headerQtip')?input.opts.headerQtip:undefined;
        }else {
            initStatus = false;
        }
        
        if(input.hasOwnProperty('callbacks')) {
            callbacks = input.callbacks;
        }
    }
    
    function createTableId() {
        var randomId = 'special-table-' + Math.floor((Math.random() * 100) + 1);
        if($('#' + randomId).length === 0) {
            return randomId;
        }else {
            return createTableId();
        }
    }
    
    function initDiv() {
        var _div = "<div id='"+divs.mainId+"' class='study-view-dc-chart study-view-tables h1half w2'>"+
            "<div id='"+divs.headerId+"'style='height: 16px; width:100%; float:left; text-align:center;'>"+
                "<div class='titleWrapper' id='"+divs.titleWrapperId+"'>"+
                    "<img id='"+divs.reloadId+"' class='study-view-title-icon study-view-hidden hover' src='images/reload-alt.svg' alt='reload' />"+
                        "<div id='"+divs.downloadWrapperId+"' class='study-view-download-icon'>" +
                        "<img id='"+divs.downloadId+"' style='float:left' src='images/in.svg' alt='download' />"+
                        "</div>"+
                    "<img class='study-view-drag-icon' src='images/move.svg' alt='move' />"+
                    "<span id='"+divs.deleteIconId+"' class='study-view-tables-delete'>x</span>"+
                "</div>"+
                "<chartTitleH4 id='"+divs.titleId+"'>"+divs.title+"</chartTitleH4>" +
            "</div>"+
            "<div id='"+divs.tableId+"' class='" + divs.tableClassName + "'>"+
            "</div>"+
            "<div id='"+divs.loaderId+"' class='study-view-loader' style='top:30%;left:30%'><img src='images/ajax-loader.gif' alt='loading' /></div>"+
        "</div>";
        $('#' + divs.attachedId).append(_div);
        showHideDivision('#' + divs.mainId,['#' + divs.titleWrapperId],  0);
        reset();
        startLoading();
    }

    function initTable(data) {
        if(typeof data === 'object' && data.selected instanceof Array && data.selected.length > 0) {
            selectedRows = data.selected.map(function(item) {
                return item.uniqueId;
            });
        }else {
            selectedRows = [];
        }

        if(typeof data === 'object' && data.hasOwnProperty('attr') && data.hasOwnProperty('arr')) {
            arr = data.arr;
            attr = data.attr;
            type = data.type;
            arrL = arr.length;
            attrL = attr.length;
        }
        if(typeof data === 'object' && data.hasOwnProperty('selectedSamples')) {
            selectedSamples = data.selectedSamples;
        }
        if(selectedSamples.length === 0){
            hideReload();
        }
    }
    
    function initReactTableData() {
        var data = {
            data: [],
            attributes: attr
        };

        _.each(arr, function (item, index) {
            for(var key in item) {
                // if(key !== 'uniqueId') {
                var datum = {
                    attr_id : key,
                    uniqueId: item.uniqueId,
                    attr_val: key === 'caseIds' ? item.caseIds.join(',') : item[key]
                };
                data.data.push(datum);
                // }
            }
        });
        
        return data;
    }
    
    function initReactTables() {
        var data = initReactTableData();
        var testElement = React.createElement(EnhancedFixedDataTableSpecial, {
            input: data,
            filter: "ALL",
            download: "NONE",
            downloadFileName: "data.txt",
            showHide: false,
            hideFilter: true,
            scroller: true,
            resultInfo: false,
            groupHeader: false,
            fixedChoose: false,
            uniqueId: "uniqueId",
            rowHeight: 25,
            tableWidth: 375,
            maxHeight: 290,
            headerHeight: 26,
            groupHeaderHeight: 40,
            autoColumnWidth: false,
            columnMaxWidth: 300,
            columnSorting: false,
            selectedRow: selectedRows,
            selectedGene: selectedGenes,
            rowClickFunc: reactRowClickCallback,
            geneClickFunc: reactGeneClickCallback,
            tableType: type}//mutatedGene or cna
        );

        ReactDOM.render(testElement, document.getElementById( divs.tableId));
    }

    function qtip(el, tip) {
        $(el).qtip({
            content: {text: tip},
            show: {event: "mouseover"},
            hide: {fixed: true, delay: 200, event: "mouseout"},
            style: { classes: 'qtip-light qtip-rounded' },
            // changed positioning of the qtips
            position: {my:'center right',at:'center left',viewport: $(window)}
        });
    }
    
    function redraw(data, callback) {
        initTable(data);
        initReactTables();
        addEvents();
        if(typeof callback === 'function') {
            callback();
        }
    }
    
    function addEvents() {
        $('#' + divs.tableId + '-download-icon').qtip('destroy', true);
        $('#' + divs.tableId + '-download-icon-wrapper').qtip('destroy', true);

        $('#' + divs.tableId + '-download-icon-wrapper').qtip({
            style: { classes: 'qtip-light qtip-rounded qtip-shadow'  },
            show: {event: "mouseover", delay: 0},
            hide: {fixed:true, delay: 100, event: "mouseout"},
            position: {my:'bottom left',at:'top right', viewport: $(window)},
            content: {
                text:   "Download"
            }
        });

        $('#' + divs.tableId + '-download-icon').qtip({
            style: { classes: 'qtip-light qtip-rounded qtip-shadow'  },
            show: {event: "click", delay: 0},
            hide: {fixed:true, delay: 100, event: "mouseout "},
            position: {my:'top center',at:'bottom center', viewport: $(window)},
            content: {
                text:
                "<div style='display:inline-block;'>"+
                "<button id='"+divs.tableId+"-tsv' style=\"width:50px\">TXT</button>"+
                "</div>"
            },
            events: {
                show: function() {
                    $('#' + divs.tableId + '-download-icon-wrapper').qtip('api').hide();
                },
                render: function() {
                    $("#"+divs.tableId+"-tsv").click(function(){
                        var content = '';

                        attr.forEach(function(e) {
                            content += e.attr_id === 'uniqueId' ? '' : ((e.display_name||'Unknown') + '\t');
                        });
                        content = content.slice(0,-1);

                        arr.forEach(function(e){
                            content += '\r\n';
                            attr.forEach(function(e1){
                                content += e1.attr_id === 'uniqueId' ? '' : (e[e1.attr_id] + '\t');
                            });
                            content = content.slice(0,-1);
                        });

                        var downloadOpts = {
//                            filename: cancerStudyName + "_" + divs.title + ".txt",
                            filename: StudyViewParams.params.studyId + "_" + divs.title + ".txt",
                            contentType: "text/plain;charset=utf-8",
                            preProcess: false
                        };

                        cbio.download.initDownload(content, downloadOpts);
                    });
                }
            }
        });
        deleteTable();
        addTableHeaderQtip();
    }

    function reactRowClickCallback(data, selected, selectedRows) {
        if(selectedRows.length === 0) {
            hideReload();
        }else {
            showReload();
        }

        if(callbacks.hasOwnProperty('rowClick')) {
            callbacks.rowClick(divs.tableId, selectedRows, data, selected);
        }
    }
    
    function reactGeneClickCallback(selectedRow, selected) {
        callbacks.addGeneClick(selectedRow);
    }
    
    // if there's a qtip for the table header, add it
    function addTableHeaderQtip(){
        if(!_.isUndefined(headerQtip)) {
            qtip('#'+ divs.titleId, headerQtip);
        }
    }
    
    function deleteTable() {
        $('#'+ divs.deleteIconId).unbind('click');
        $('#'+ divs.deleteIconId).click(function() {
            if(callbacks.hasOwnProperty('deleteTable')) {
                callbacks.deleteTable(divs.tableId, divs.title);
            }else {
                redraw();
            }
        });
    }
    
    function reset() {
        $('#'+ divs.reloadId).unbind('click');
        $('#'+ divs.reloadId).click(function() {
            if(callbacks.hasOwnProperty('rowClick')) {
                callbacks.rowClick(divs.tableId, [], [], false);
            }
            redraw();
            hideReload();
        });
    }
    
    function showReload() {
        $('#' + divs.reloadId).css('display', 'block');
        $('#' + divs.mainId).css({'border-width':'2px', 'border-style':'inset'});
    }
    
    function hideReload() {
        $('#' + divs.reloadId).css('display', 'none');
        $('#' + divs.mainId).css({'border-width':'1px', 'border-style':'solid'});
    }
    
    function showHideDivision(_listenedDiv, _targetDiv, _time){
        var _targetLength = _targetDiv.length;
        for ( var i = 0; i < _targetLength; i++) {
            $(_targetDiv[i]).css('display', 'none');
        }
        $(_listenedDiv).hover(function(){
            $(_listenedDiv).css('z-index', '1');
            for ( var i = 0; i < _targetLength; i++) {
                $(_targetDiv[i]).stop().fadeIn(_time, function(){
                    $(this).css('display', 'block');
                });
            }
        }, function(){
            $(_listenedDiv).css('z-index', '0');
            for ( var i = 0; i < _targetLength; i++) {
                $(_targetDiv[i]).stop().fadeOut(_time, function(){
                    $(this).css('display', 'none');
                });
            }
        });
    }

    function getInitStatus(){
        return initStatus;
    }

    function startLoading() {
        $('#' + divs.loaderId).css('display', 'block');
        $('#' + divs.tableId).css('opacity', '0.3');
    }

    function stopLoading() {
        $('#' + divs.loaderId).css('display', 'none');
        $('#' + divs.tableId).css('opacity', '1');
    }

    function show() {
        $('#' + divs.mainId ).css('display', 'block');
    }

    function hide() {
        $('#' + divs.mainId ).css('display', 'none');
    }

    return {
        init: init,
        initDiv: function(input){
            initData(input);
            initDiv();
        },
        draw: function(id, data, callback) {
            try {
                initTable(data);
                initReactTables();
                addEvents();
                stopLoading();
                initStatus = true;
            } catch (e) {
                initStatus = false;
            }
            if (_.isFunction(callback)) {
                callback(id, initStatus ? 'initialized' : 'failed');
            }
        },
        redraw: redraw,
        getInitStatus: getInitStatus,
        startLoading: startLoading,
        stopLoading: stopLoading,
        show: show,
        hide: hide,
        updateSelectedGenes: function(data, genes) {
            initTable(data);
            selectedGenes = genes;
            initReactTables();
        }
    };
};