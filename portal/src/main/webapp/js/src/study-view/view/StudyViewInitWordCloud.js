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
        fontSize = [],
        parObject = {
            studyId: "",
            caseIds: "",
            cnaProfileId: "",
            mutationProfileId: "",
            caseSetId: ""
        };
        
    function initData(_data){
        words = _data.names;
        fontSize = _data.size;
    }
    
    function initParams(_params){
        parObject.studyId = _params.studyId;
        parObject.caseIds = _params.caseIds;
        parObject.cnaProfileId = _params.cnaProfileId;
        parObject.mutationProfileId = _params.mutationProfileId;
        parObject.caseSetId = _params.caseSetId;
    }
    
    function initDiv(){
        $("#study-view-charts").append(StudyViewBoilerplate.wordCloudDiv);
    }
    
    //This function is inspired by Jason's daw function.
    function draw(words){
        var fill = d3.scale.category20();

        d3.select("#study-view-word-cloud").append("svg")
            .attr("width", 180)
            .attr("height", 180)
          .append("g")
            .attr("transform", "translate(90,90)")
          .selectAll("text")
            .data(words)
          .enter().append("text")
            .style("font-size", function(d) { return d.size + "px"; })
            .style("font-family", "Impact")
            //.style("fill", function(d, i) { return fill(i); })
            .style("fill", 'green')
            .style('cursor', 'pointer')
            .attr("text-anchor", "middle")
            .attr("transform", function(d) {
              return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")";
            })
            
            .text(function(d) { return d.text; });
    
            $("#study-view-word-cloud svg text").click(function(){
                var _text = $(this).text();
                window.open("index.do?Action=Submit&"+
                            "genetic_profile_ids="+parObject.mutationProfileId+"&" +
                            "case_set_id="+parObject.caseSetId+"&" +
                            "cancer_study_id="+parObject.studyId+"&" +
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
            .padding(5)
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
        init: function(_params, _data){
            initParams(_params);
            initData(_data);
            initDiv();
            initD3Cloud();
        },
        
        redraw: redraw
    };
})();
