/*
 * This is the implementation of d3-cloud coming from Jason Davies

The following statment is coming from the Lisense of Jason Davies's d3-cloud

Copyright (c) 2013, Jason Davies.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.

  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

  * The name Jason Davies may not be used to endorse or promote products
    derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL JASON DAVIES BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 */

var StudyViewInitWordCloud = (function() {
    //The length of words should be same with the length of fontSize.
    var words = [],
        fontSize = [];
    var WIDTH = 180,
        HEIGHT = 180;
    
    var initStatus = false;
    
    function initData(_data){
        words = _data.names;
        fontSize = _data.size;
    }
    
    function initDiv(){
        $("#study-view-charts").append(StudyViewBoilerplate.wordCloudDiv);
        $("#study-view-word-cloud-pdf-name").val("Word_Cloud_"+ StudyViewParams.params.studyId +".pdf");
        $("#study-view-word-cloud-svg-name").val("Word_Cloud_"+ StudyViewParams.params.studyId +".svg");
    }
    
    //Add all listener events
    function addEvents() {
        $("#study-view-word-cloud-pdf").submit(function(){
            setSVGElementValue("study-view-word-cloud",
               "study-view-word-cloud-pdf-value");
        });
        $("#study-view-word-cloud-svg").submit(function(){
            setSVGElementValue("study-view-word-cloud",
                "study-view-word-cloud-svg-value");
        });
        
        StudyViewUtil.showHideDivision(
                "#study-view-word-cloud", 
                "#study-view-word-cloud-side"
        );
                                    
        StudyViewUtil.showHideDivision(
                "#study-view-word-cloud", 
                "#study-view-word-cloud .study-view-drag-icon"
        );
    }
    
    function setSVGElementValue(_svgParentDivId,_idNeedToSetValue){
        var svgElement;
        
        //Remove x/y title help icon first.
        svgElement = $("#" + _svgParentDivId + " svg").html();
        $("#" + _idNeedToSetValue)
                .val("<svg width='200' height='200'>"+
                    "<g><text x='100' y='20'  style='font-weight: bold; "+
                    "text-anchor: middle'>"+
                    "Mutated Genes</text></g><g transform='translate(10, 20)'>"+
                    svgElement + "</g></svg>");
    }
    
    //This function is inspired by Jason's daw function.
    function draw(words){
        var fill = d3.scale.category20();
        var startX = 0, startY = 0;
        
        d3.select("#study-view-word-cloud").append("svg")
            .attr("width", WIDTH)
            .attr("height", HEIGHT)
          .append("g")
            .attr("transform", "translate(10,40)")
          .selectAll("text")
            .data(words)
          .enter().append("text")
            .style("font-size", function(d) { return d.size + "px"; })
            .style("font-family", "Impact")
            //.style("fill", function(d, i) { return fill(i); })
            .style("fill", 'green')
            .style('cursor', 'pointer')
            //.attr("text-anchor", "middle")
            .attr("transform", function(d, i) {
                //TODO: A constant didider for width and height of each text 
                //are calculated based on multiple testing. This should be
                //changed later.
                var _translate = "translate(" + [startX, startY] + ")";
                
                //d.width - d.size is so called constant divider for width
                var _width = d.width - d.size;
                
                startX += _width;
                
                if(startX > (WIDTH-10)){
                    startX = 0;
                    
                    //1.3 is so called constant divider for height 
                    startY += d.y1 * 1.3;
                    _translate = "translate(" + [startX, startY] + ")";
                    startX += _width;
                }
                
                return _translate;
              //return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")";
            })
            
            .text(function(d) { return d.text; });
            
            $("#study-view-word-cloud svg text").click(function(){
                var _text = $(this).text();
                window.open("index.do?Action=Submit&"+
                            "genetic_profile_ids="+StudyViewParams.params.mutationProfileId+"&" +
                            "case_set_id="+StudyViewParams.params.caseSetId+"&" +
                            "cancer_study_id="+StudyViewParams.params.studyId+"&" +
                            "gene_list="+ _text +"&tab_index=tab_visualize&" +
                            "#mutation_details");
            });
    }
    
    //Changed based on Jason's example file.
    function initD3Cloud() {
        d3.layout.cloud().size([180, 180])
            .words(words.map(function(d, index) {
                return {text: d, size: fontSize[index]};
            }))
            .padding(0)
            .rotate(function() { return ~~0; })
            .font("Impact")
            .fontSize(function(d) { return d.size; })
            .on("end", draw)
            .start();
    }
    
    function redraw(_data){
        $("#study-view-word-cloud").find('svg').remove();
        initData(_data);
        initD3Cloud();
    }
    
    return {
        init: function(_data){
            initData(_data);
            initDiv();
            initD3Cloud();
            addEvents();
            initStatus = true;
        },
        
        redraw: redraw,
        getInitStatus: function(){
            return initStatus;
        }
    };
})();
