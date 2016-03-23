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
        attr = [],
        arr = [],
        attrL = 0,
        arrL = 0,
        selectedSamples = [],
        dataTable = '',
        callbacks = {},
        checkboxChildId = -1,
        initStatus = false;
    
    function init(input) {
        initStatus = true;
        try {
            initData(input);
            initDiv();
            initTable(input.data);
            initDataTable();
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
            divs.headerId = tableId + '-header';
            divs.reloadId = tableId + '-reload-icon';
            divs.downloadId = tableId + '-download-icon';
            divs.downloadWrapperId = tableId + '-download-icon-wrapper';
            divs.loaderId = tableId + '-loader';
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
                    "<img id='"+divs.reloadId+"' class='study-view-title-icon study-view-hidden hover' src='images/reload-alt.svg'/>"+
                        "<div id='"+divs.downloadWrapperId+"' class='study-view-download-icon'>" +
                        "<img id='"+divs.downloadId+"' style='float:left' src='images/in.svg'/>"+
                        "</div>"+
                    "<img class='study-view-drag-icon' src='images/move.svg'/>"+
                    "<span id='"+divs.deleteIconId+"' class='study-view-tables-delete'>x</span>"+
                "</div>"+
                "<chartTitleH4 id='"+divs.titleId+"'>"+divs.title+"</chartTitleH4>" +
            "</div>"+
            "<div id='"+divs.tableId+"'>"+
            "</div>"+
            "<div id='"+divs.loaderId+"' class='study-view-loader' style='top:30%;left:30%'><img src='images/ajax-loader.gif'/></div>"+
        "</div>";
        $('#' + divs.attachedId).append(_div);
        showHideDivision('#' + divs.mainId,['#' + divs.titleWrapperId],  0);
        reset();
        startLoading();
    }

    function datumIsSelected(selected, datum) {
        var contain = false;
        for(var i = 0; i < selected.length; i ++){
            var allSame = true;
            for(var key in selected[i]) {
                if(datum[key] !== selected[i][key]) {
                    allSame = false;
                }
            }
            if(allSame) {
                contain = true;
            }
        }
        return contain;
    }

    function initTable(data) {
        var table = $('#' + divs.tableId);
        var tableHeaderStr = [];
        var tableBodyStr = [];
        var i = 0, j = 0;
        var hasSelected = false;
        var selectedKeys = [];

        if(typeof data === 'object' && data.selected instanceof Array && data.selected.length > 0) {
            hasSelected = true;
        }

        if(typeof data === 'object' && data.hasOwnProperty('attr') && data.hasOwnProperty('arr')) {
            arr = data.arr;
            attr = data.attr;
            arrL = arr.length;
            attrL = attr.length;
        }
        if(typeof data === 'object' && data.hasOwnProperty('selectedSamples')) {
            selectedSamples = data.selectedSamples;
        }
        var tableHtml = ['<table><thead><tr>']; //

        
        //Append table header
        for(i=0; i< attrL; i++){
            tableHeaderStr.push('<th style=" white-space: nowrap;" ');
            if(attr[i].hasOwnProperty('qtip')) {
                tableHeaderStr.push(' class="hasQtip" qtip="' + attr[i].qtip + '"');
            }
            tableHeaderStr.push('>'+ attr[i].displayName||'Unknown' +'</th>');
        }
        tableHtml.push(tableHeaderStr.join(''));
        tableHtml.push('</tr></thead><tbody>');//
        
        //Append table body
        for(i = 0; i < arrL; i++){

            if(typeof data === 'object' && data.selected instanceof Array && data.selected.length > 0 && datumIsSelected(data.selected, arr[i])) {
                tableBodyStr.push('<tr class="highlightRow" style="white-space: nowrap;">');
            }else{
                tableBodyStr.push('<tr style="white-space: nowrap;">');
            }

            for(j = 0; j < attrL; j++) {
                // added an id for the clickable component
                tableBodyStr.push('<td' + ( (attr[j].name === 'samples' && +arr[i].hasOwnProperty('uniqueId')) ? ' id='+ divs.tableId + '-' + arr[i].uniqueId : '') + '>' + arr[i][attr[j].name] + '</td>');
            }
            tableBodyStr.push('</tr>');
        }
        tableHtml.push(tableBodyStr.join(''));
        tableHtml.push('</tbody></table>');

        table.html(tableHtml.join(''));
        if(selectedSamples.length === 0){
            hideReload();
        }
    }

    function initDataTable() {
        var dataTableOpts = {
            "sDom": 'rt<f>',
            "sScrollY": '270',
            "bPaginate": false,
            "aaSorting": [],
            "bAutoWidth": true,
            "aoColumnDefs": [],
            "fnInitComplete": function(oSettings, json) {
                $('#'+ divs.tableId +' .dataTables_filter')
                        .find('label')
                        .contents()
                        .filter(function(){
                            return this.nodeType === 3;
                        }).remove();

                $('#'+ divs.tableId +' .dataTables_filter')
                        .find('input')
                        .attr('placeholder', 'Search...');
            }
        };
        
        var geneIndex = -1,
            altTypeIndex = -1,
            cytobandIndex = -1,
            samplesIndex = -1,
            qvalIndex = -1,
            unvisiable = [];
        
        attr.forEach(function(e, i){
            if(e.name === 'gene') {
                geneIndex = i;
            }
            if(e.name === 'altType') {
                altTypeIndex = i;
            }
            if(e.name === 'cytoband') {
                cytobandIndex = i;
            }
            if(e.name === 'samples') {
                samplesIndex = i;
                checkboxChildId = i+1;
            }
            if(e.name === 'qval') {
                qvalIndex = i;
            }
            if(e.hidden){
                unvisiable.push(i);
            }
        });
        
        if(unvisiable.length > 0) {
            dataTableOpts.aoColumnDefs.push({
                "targets": unvisiable,
                'visible': false
            });
        }
        
        if(samplesIndex !== -1) {
            dataTableOpts.aoColumnDefs.push({
                "aTargets": [samplesIndex],
                "mDataProp": function(source,type) {
                    var _samplesType = source[samplesIndex];
                    if (type==='display') {
                        return '<span>'+_samplesType+'</span><input type="checkbox" style="float:right; margin: 3px 0;">';
                    }
                    return _samplesType;
                }
            });
            dataTableOpts.aaSorting.push([samplesIndex, 'desc']); 
        }
        
        if(altTypeIndex !== -1) {
            dataTableOpts.aoColumnDefs.push({
                "aTargets": [altTypeIndex],
                "mDataProp": function(source,type) {
                    var _altType = source[altTypeIndex];
                    if (type==='display') {
                        var str = '';
                        if(_altType === 'AMP') {
                            str += '<span style="color:red;font-weight:bold">'+_altType+'</span>';
                        }else {
                            str += '<span style="color:blue;font-weight:bold">'+_altType+'</span>';
                        }
                        return str;
                    }
                    return _altType;
                }
            });
            dataTableOpts.aaSorting.push([3, 'desc']);
        }
        
        if(cytobandIndex !== -1) {
            dataTableOpts.aoColumnDefs.push({
                "aTargets": [cytobandIndex],
                "sType": 'cytoband-base',
                "mDataProp": function(source,type) {
                    var _cytoband = source[cytobandIndex];
                    if (type==='display') {
                        var str = '';
                        if(_cytoband.toString().length > 8) {
                            str += '<span class="hasQtip" qtip="'+_cytoband+'">'+_cytoband.substring(0,6) + '...'+'</span>';
                        }else {
                            str = _cytoband;
                        }
                        return str;
                    }
                    return _cytoband;
                }
            });
        }
        if(geneIndex !== -1) {
            dataTableOpts.aoColumnDefs.push({
                "aTargets": [geneIndex],
                "mDataProp": function(source,type) {
                    var _gene = source[geneIndex];
                    if (type==='display') {
                        var str = '';

                        // add qtip for selecting the gene
                        str += '<span class="hasQtip selectHighlight" qtip="Click '+_gene+' to add to your query">';
                        if(_gene.toString().length > 6) {
                            str += _gene.substring(0,4) + '...'+'</span>';
                        }else {
                            str += _gene+'</span>';
                        }

                        if(qvalIndex !== -1 && attr[qvalIndex].displayName && source[qvalIndex]) {
                            var _displayName = attr[qvalIndex].displayName.toString().toLowerCase();
                            str += '<span class="hasQtip" qtip="<b>'+ attr[qvalIndex].displayName +'</b><br/><i>Q-value</i>: ' + source[qvalIndex] + '"><svg width="14" height="14"><g transform="translate(8, 8)"><circle r="5" stroke="#55C" fill="none"></circle><text x="-3" y="3" font-size="7" fill="#66C">';
                            if(_displayName.indexOf('mutsig') !== -1 && source[qvalIndex]){
                                str += 'M';
                            }else if(_displayName.indexOf('gistic') !== -1 && source[qvalIndex]){
                                str += 'G';
                            }else {
                                str += 'Q';
                            }
                            str += '</text></g></svg></span>';
                        }

                        return str;
                    }
                    return _gene;
                }
            });
              
            dataTableOpts.fnDrawCallback = function() {
                $('#'+ divs.tableId).find('.hasQtip').each(function(e, i) {
                    $(this).qtip('destroy', true);
                    qtip(this, $(this).attr('qtip'));
                });

                $('#'+ divs.tableId).find('table tbody tr').each(function(e, i) {
                    if($(this).hasClass('highlightRow')){
                        $(this).find('td').addClass('highlightRow');
                        $(this).find('td:nth-child('+checkboxChildId+') input:checkbox').attr('checked', true);
                    }
                });

                checkboxClick();
                // add functionality for when the add gene icon is clicked
                addGeneClickSetup();
            };
        }
        dataTable = $('#'+ divs.tableId +' table').dataTable(dataTableOpts);
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
        dataTable = null;
        $('#' + divs.tableId).empty();
        initTable(data);
        initDataTable();
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
                            content += e.name === 'uniqueId' ? '' : ((e.displayName||'Unknown') + '\t');
                        });
                        content = content.slice(0,-1);

                        arr.forEach(function(e){
                            content += '\r\n';
                            attr.forEach(function(e1){
                                content += e1.name === 'uniqueId' ? '' : (e[e1.name] + '\t');
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
    }


    function addGeneClickSetup(){
        $('#'+divs.tableId+' table tbody tr td:first-child span').unbind('click');
        $('#'+divs.tableId+' table tbody tr td:first-child span').click(function () {

            if(callbacks.hasOwnProperty('addGeneClick')) {
                // call addGeneClick with this row's data
                callbacks.addGeneClick(dataTable.api().row($(this).parent().parent()).data());
            }
        });
    }

    function checkboxClick() {
        $('#' + divs.tableId + ' table tbody tr td:nth-child('+checkboxChildId+') input:checkbox').unbind('change');
        $('#' + divs.tableId + ' table tbody tr td:nth-child('+checkboxChildId+') input:checkbox').change(function () {
            $(this).parent().siblings().addBack().toggleClass('highlightRow');
            $(this).parent().parent().toggleClass('highlightRow');

            clickFunc(dataTable.api().row($(this).parent().parent()).data() , $(this).prop("checked"));
        });
    }

    function clickFunc(clickedRowData, rowChecked) {
        var highlightedRowsData = dataTable.api().rows('.highlightRow').data();

        if(highlightedRowsData.length === 0) {
            hideReload();
        }else {
            showReload();
        }

        if(callbacks.hasOwnProperty('rowClick')) {
            callbacks.rowClick(divs.tableId, highlightedRowsData, clickedRowData, rowChecked);
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
            $('#' + divs.tableId + ' tbody').find('.highlightRow').removeClass('highlightRow');
            $('#' + divs.tableId + ' tbody tr td:nth-child('+checkboxChildId+') input:checkbox').attr('checked', false);
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

    function resize() {
        dataTable.fnAdjustColumnSizing();
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
        draw: function(data){
            initTable(data);
            initDataTable();
            addEvents();
            stopLoading();
        },
        getDataTable: function() {
            return dataTable;
        },
        redraw: redraw,
        getInitStatus: getInitStatus,
        resize: resize,
        startLoading: startLoading,
        stopLoading: stopLoading,
        show: show,
        hide: hide
    };
};