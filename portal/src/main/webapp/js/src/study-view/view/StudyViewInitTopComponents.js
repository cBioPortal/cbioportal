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


var StudyViewInitTopComponents = (function() {
    function liClickCallBack(_id, _text) {
        StudyViewInitCharts.createNewChart(_id, _text);
    };
    
        
    function addEvents() {
        
        $('#study-view-header-left-2').unbind('click');
        $('#study-view-header-left-2').click(function (){
            // clear all breadcrumbs
            BreadCrumbs.clearAllBreadCrumbs();

            var i,
                _charts = StudyViewInitCharts.getCharts(),
                _chartsLength = _charts.length;
            
            StudyViewInitScatterPlot.setclearFlag(true);
            
            //Previous using dc.filterAll(), but this will redraw word cloud
            //several times based on the number of charts. Right now, only
            //redraw word cloud if the chart has filter
            for( i = 0; i < _chartsLength; i++){
                if(_charts[i] !== "" && 
                        _charts[i].getChart().filter() !== null){
                    
                    _charts[i].getChart().filter(null);
                }
            }
            
            //If set the filter to null the update scatterplot in charts do
            //not work, so need to update scatter plot here
            //StudyViewInitCharts.redrawScatter();
            
            dc.redrawAll();
            StudyViewInitTables.clearAllSelected();
            StudyViewInitCharts.resetBars();
            StudyViewInitCharts.redrawSpecialPlots();
            setTimeout(function() {
                StudyViewInitScatterPlot.setclearFlag(false);
            }, StudyViewParams.summaryParams.transitionDuration);
            StudyViewInitCharts.changeHeader();
        });
        
        
        $("#study-view-case-select-custom-submit-btn").click(function() {
            var ids = $('#study-view-case-select-custom-input').val().trim().split(/\s+/);
            var convertedIds = convertIds(ids);
            $('#study-view-header-right-1').qtip('toggle');
            if(_.isArray(convertedIds) && convertedIds.length > 0) {
                StudyViewInitCharts.filterChartsByGivingIDs(convertedIds);
                BreadCrumbs.updateSelectCaseIDdBreadCrumb('study-view-select-case', 'Custom', 'User defined cases', ids);
            }
        });
        
        $("#study-view-tutorial").click(function() {
            StudyViewInitIntroJS.init();
        });

        $("#study-view-header-left-5").click(function() {
            var _url;
            var _selectedCaseIds = StudyViewInitCharts.getSelectedCasesID();
            var _selectedPatientIds = StudyViewProxy.getPatientIdsBySampleIds(_selectedCaseIds);
            
            _selectedPatientIds = _selectedPatientIds.sort();
            _url =  "case.do?cancer_study_id="+
                    StudyViewParams.params.studyId+
                    "&case_id="+_selectedPatientIds[0]+
                    "#nav_case_ids="+_selectedPatientIds.join(",");
            
            window.open(_url);
        });

        // handle the space key to make the span act like a button
        $("#study-view-header-left-5").keyup(function(event) {
            event = event || window.event;
            if (event.keyCode === 32) {  // the Space key
                event.stopPropagation();
                event.target.click();
            }
        });
        // do not scroll when the space key is depressed
        $("#study-view-header-left-5").keydown(function(event) {
            event = event || window.event;
            if (event.keyCode === 32) {  // the Space key
                event.preventDefault();
                return false;
            }
        });

        $("#study-view-header-left-6").click(function () {
            var content = '';
            var sampleIds = StudyViewInitCharts.getSelectedCasesID();
            var attr = StudyViewProxy.getAttrData();
            var arr = [];
            var attrL = 0, arrL = 0;
            var strA = [];

            if (sampleIds.length === StudyViewProxy.getSampleIds().length) {
                arr = StudyViewProxy.getArrData();
            } else {
                arr = StudyViewProxy.getArrDataBySampleIds(sampleIds);
            }

            attrL = attr.length;
            for (var i = 0; i < attrL; i++) {
                strA.push(attr[i].display_name || 'Unknown');
            }
            content = strA.join('\t');
            strA.length =0;

            arrL = arr.length;

            for (var i = 0; i < arrL; i++) {
                strA.length = 0;
                for (var j = 0; j < attrL; j++) {
                    strA.push(arr[i][attr[j].attr_id]);
                }
                content += '\r\n' + strA.join('\t');
            }

            var downloadOpts = {
                filename: StudyViewParams.params.studyId + "_clinical_data.txt",
                contentType: "text/plain;charset=utf-8",
                preProcess: false
            };

            cbio.download.initDownload(content, downloadOpts);
        });

        // handle the space key to make the span act like a button
        $("#study-view-header-left-6").keyup(function(event) {
            event = event || window.event;
            if (event.keyCode === 32) {  // the Space key
                event.stopPropagation();
                event.target.click();
            }
        })
        // do not scroll when the space key is depressed
        $("#study-view-header-left-6").keydown(function(event) {
            event = event || window.event;
            if (event.keyCode === 32) {  // the Space key
                event.preventDefault();
                return false;
            }
        })


        $("#study-view-form").click(function(event){
            // add the necessary fields to the form
            if(!QueryByGeneTextArea.isEmpty()) {
                event.preventDefault();
                QueryByGeneTextArea.validateGenes(decideSubmit, false);
            }
        });


        $("#study-view-header-left-1").hover(function () {
            $("#query-by-gene-textarea").css("outline", "-webkit-focus-ring-color auto 5px");
            $("#study-view-header-left-5").css("outline", "-webkit-focus-ring-color auto 5px");
        }, function() {
            $("#query-by-gene-textarea").css("outline", "");
            $("#study-view-header-left-5").css("outline", "");
        });

        $("#study-view-header-left-5").hover(function () {
            $("#study-view-header-left-5").css("outline", "-webkit-focus-ring-color auto 5px");
        }, function() {
            $("#study-view-header-left-5").css("outline", "");
        });

        $("#study-view-header-left-6").hover(function () {
            $("#study-view-header-left-5").css("outline", "-webkit-focus-ring-color auto 5px");
        }, function() {
            $("#study-view-header-left-5").css("outline", "");
        });

        $('#study-view-header-left-5').qtip({
            prerender: true,
            content: {text: 'Click to view the selected cases' },
            style: { classes: 'qtip-light qtip-rounded qtip-shadow' },
            show: {event: 'mouseover'},
            hide: {fixed:true, delay: 100, event: 'mouseout'},
            position: {my:'bottom center', at:'top center', viewport: $(window)}
        });

        $('#study-view-header-left-6').qtip({
            prerender: true,
            content: {text: 'Click to download the selected cases' },
            style: { classes: 'qtip-light qtip-rounded qtip-shadow' },
            show: {event: 'mouseover'},
            hide: {fixed:true, delay: 100, event: 'mouseout'},
            position: {my:'bottom center', at:'top center', viewport: $(window)}
        })
    }

    // decide whether to proceed with the submit of the form
    function decideSubmit(allValid){
        // if all genes are valid, submit, otherwise show a notification
        if(allValid){
            new QueryByGeneUtil().addStudyViewFields();
            $("#study-view-form").trigger("submit");
        }
        else {
            new Notification().createNotification("There were problems with the selected genes. Please fix.", {message_type: "danger"});
            $("#query-by-gene-textarea").focus();
        }
    }

    //The selected id should be sample based. Check patient list if unidentified id exists.
    function convertIds(ids) {
        var radioVal = $('input[name=study-view-case-select-custom-radio]:checked').val();
        var sampleIds = [];
        var unmapped = [];
        if (radioVal === 'patient') {
            _.each(ids, function (id) {
                var mappedId = StudyViewProxy.getSampleIdsByPatientIds([id]);
                if (_.isUndefined(mappedId) || _.isEmpty(mappedId)) {
                    unmapped.push(id);
                } else {
                    sampleIds = sampleIds.concat(mappedId);
                }
            });
        } else {
            _.each(ids, function (id) {
                if (StudyViewProxy.sampleIdExist(id)) {
                    sampleIds.push(id);
                } else {
                    unmapped.push(id);
                }
            });
        }

        if (unmapped.length > 0) {
            new Notification().createNotification(sampleIds.length +
                ' samples selected. The following ' + (radioVal === 'patient' ? 'patient' : 'sample') +
                ' ID' + (unmapped.length === 1 ? ' was' : 's were') + ' not found in this study: ' +
                unmapped.join(', '), {message_type: 'warning'});
        } else {
            new Notification().createNotification(sampleIds.length + ' samples selected.', {message_type: 'info'});
        }

        return sampleIds;
    }
    
    function changeHeader(_filteredResult, _numOfCases, _removedChart){
        var _caseID = [],
            _resultLength = _filteredResult.length,
            _charts = StudyViewInitCharts.getCharts(),
            
            //Check whether page has been scrolled or not, The position of 
            //left-3 will be different
            windowScolled = false; 
    
        for(var i=0; i<_filteredResult.length ; i++){
            _caseID.push(_filteredResult[i].CASE_ID);
        }
     

        $("#study-view-header-left-1").css('display','block');

        $("#study-view-header-left-3").css('display','block')
        $("#study-view-header-left-3").text("Samples selected: ");
        $("#study-view-header-left-5").css('display','block');
        $("#study-view-header-left-5").text(_resultLength);

        if(_resultLength !== _numOfCases){
            if(_resultLength === 0){
                $("#study-view-header-left-1").css('display','none');
                //$("#study-view-header-left-4").css('display','none');
            }
        }
        $("#study-view-header-left-case-ids").val(_caseID.join(" "));
    }
    
    function initAddCharts(target) {
        AddCharts.init(target);
        AddCharts.initAddChartsButton(StudyViewInitCharts.getShowedChartsInfo());
        AddCharts.liClickCallback(liClickCallBack);
    }
    
    function createDiv() {
        var _newElement = StudyViewBoilerplate.headerDiv(),
            _customDialogQtip = jQuery.extend(true, {}, StudyViewBoilerplate.headerCaseSelectCustomDialog);
        
        $("#study-view-header-function").append(_newElement);
        $("#study-view-header-function").append(StudyViewBoilerplate.customDialogDiv);
        $("#study-view-header-left-cancer_study-ids").val(StudyViewParams.params.studyId);
        $("#study-view-header-left-case-ids").val(StudyViewParams.params.sampleIds.join(" "));
        //$("#study-view-header-function").append(StudyViewBoilerplate.tutorialDiv);
        _customDialogQtip.position.target = $(window);
        _customDialogQtip.content.text = $('#study-view-case-select-custom-dialog');
        $('#study-view-header-right-1').qtip(_customDialogQtip);

        initAddCharts("#study-view-header-right");
        // ensure header has proper values
        StudyViewInitCharts.changeHeader();
    }



    return {
        init: function() {
            createDiv();
            addEvents();
            QueryByGeneTextArea.init('#query-by-gene-textarea', StudyViewInitTables.updateGeneHighlights);
        },
        
        changeHeader: changeHeader,
    };
})();